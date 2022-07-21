/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.catalyst.parser

import com.netease.arctic.spark.sql.catalyst.parser.ArcticParserUtils.withOrigin
import com.netease.arctic.spark.sql.parser.{ArcticExtendSparkSqlBaseVisitor, ArcticExtendSparkSqlParser}
import org.antlr.v4.runtime.tree.{ParseTree, RuleNode, TerminalNode}
import org.antlr.v4.runtime.{ParserRuleContext, Token}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.analysis.{MultiAlias, UnresolvedAlias, UnresolvedAttribute, UnresolvedGenerator, UnresolvedHaving, UnresolvedRegex, UnresolvedRelation, UnresolvedSubqueryColumnAliases}
import org.apache.spark.sql.catalyst.catalog.BucketSpec
import org.apache.spark.sql.catalyst.expressions.{Add, Alias, And, AttributeReference, BitwiseAnd, BitwiseNot, BitwiseOr, BitwiseXor, Cast, Concat, CreateNamedStruct, CreateStruct, Cube, Divide, EqualNullSafe, EqualTo, Expression, GreaterThan, GreaterThanOrEqual, In, InSubquery, IntegralDivide, IsNotNull, IsNotUnknown, IsNull, IsUnknown, LessThan, LessThanOrEqual, Like, ListQuery, Literal, Multiply, NamedExpression, Not, Predicate, RLike, Remainder, Rollup, ScalarSubquery, Subtract, UnaryMinus, UnaryPositive, WindowSpec, WindowSpecDefinition, WindowSpecReference}
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser.astBuilder.{cleanTableOptions, cleanTableProperties}
import org.apache.spark.sql.catalyst.parser.ParserUtils.{EnhancedLogicalPlan, checkDuplicateClauses, checkDuplicateKeys, operationNotAllowed, string, validate}
import org.apache.spark.sql.catalyst.parser.SqlBaseParser._
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.plans._
import org.apache.spark.sql.catalyst.{FunctionIdentifier, SQLConfHelper, TableIdentifier}
import org.apache.spark.sql.connector.expressions.{ApplyTransform, BucketTransform, DaysTransform, FieldReference, HoursTransform, IdentityTransform, LiteralValue, MonthsTransform, Transform, YearsTransform, Expression => V2Expression}
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.types._
import org.apache.spark.util.random.RandomSampler

import java.util
import java.util.Locale
import scala.collection.JavaConverters._

class ArcticCreateTablePrimaryKeyAstBuilder(delegate: ParserInterface)
  extends ArcticExtendSparkSqlBaseVisitor[AnyRef] with SQLConfHelper with Logging{
  import com.netease.arctic.spark.sql.catalyst.parser.ArcticParserUtils._
  /* ********************************************************************************************
   * Plan parsing
   * ******************************************************************************************** */
  protected def plan(tree: ParserRuleContext): LogicalPlan = typedVisit(tree)

  override def visitQuery(ctx: ArcticExtendSparkSqlParser.QueryContext): LogicalPlan = withOrigin(ctx) {
    val query = plan(ctx.queryTerm).optionalMap(ctx.queryOrganization)(withQueryResultClauses)

    // Apply ECTs
    query.optionalMap(ctx.ctes)(withCTE)
  }

  override def visitSubquery(ctx: ArcticExtendSparkSqlParser.SubqueryContext): LogicalPlan = withOrigin(ctx) {
    plan(ctx.query)
  }

  override def visitSubqueryExpression(
                                        ctx: ArcticExtendSparkSqlParser.SubqueryExpressionContext): Expression = withOrigin(ctx) {
    ScalarSubquery(plan(ctx.query))
  }

  override def visitFromStatement(ctx: ArcticExtendSparkSqlParser.FromStatementContext): LogicalPlan = withOrigin(ctx) {
    val from = visitFromClause(ctx.fromClause)
    val selects = ctx.fromStatementBody.asScala.map { body =>
      withFromStatementBody(body, from).
        // Add organization statements.
        optionalMap(body.queryOrganization)(withQueryResultClauses)
    }
    // If there are multiple SELECT just UNION them together into one query.
    if (selects.length == 1) {
      selects.head
    } else {
      Union(selects.toSeq)
    }
  }

  override def visitRegularQuerySpecification(
                                               ctx: ArcticExtendSparkSqlParser.RegularQuerySpecificationContext): LogicalPlan = withOrigin(ctx) {
    val from = OneRowRelation().optional(ctx.fromClause) {
      visitFromClause(ctx.fromClause)
    }
    withSelectQuerySpecification(
      ctx,
      ctx.selectClause,
      ctx.lateralView,
      ctx.whereClause,
      ctx.aggregationClause,
      ctx.havingClause,
      ctx.windowClause,
      from
    )
  }

  /**
   * Create a logical plan for a given 'FROM' clause. Note that we support multiple (comma
   * separated) relations here, these get converted into a single plan by condition-less inner join.
   */
  override def visitFromClause(ctx: ArcticExtendSparkSqlParser.FromClauseContext): LogicalPlan = withOrigin(ctx) {
    val from = ctx.relation.asScala.foldLeft(null: LogicalPlan) { (left, relation) =>
      val right = plan(relation.relationPrimary)
      val join = right.optionalMap(left)(Join(_, _, Inner, None, JoinHint.NONE))
      withJoinRelations(join, relation)
    }
    if (ctx.pivotClause() != null) {
      if (!ctx.lateralView.isEmpty) {
        throw new ParseException("LATERAL cannot be used together with PIVOT in FROM clause", ctx)
      }
      withPivot(ctx.pivotClause, from)
    } else {
      ctx.lateralView.asScala.foldLeft(from)(withGenerate)
    }
  }

  override def visitTableName(ctx: ArcticExtendSparkSqlParser.TableNameContext): LogicalPlan = withOrigin(ctx) {
    val tableId = visitMultipartIdentifier(ctx.multipartIdentifier)
    val table = mayApplyAliasPlan(ctx.tableAlias, UnresolvedRelation(tableId))
    table.optionalMap(ctx.sample)(withSample)
  }

  /**
   * Add a [[Sample]] to a logical plan.
   *
   * This currently supports the following sampling methods:
   * - TABLESAMPLE(x ROWS): Sample the table down to the given number of rows.
   * - TABLESAMPLE(x PERCENT): Sample the table down to the given percentage. Note that percentages
   * are defined as a number between 0 and 100.
   * - TABLESAMPLE(BUCKET x OUT OF y): Sample the table down to a 'x' divided by 'y' fraction.
   */
  private def withSample(ctx: ArcticExtendSparkSqlParser.SampleContext, query: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    // Create a sampled plan if we need one.
    def sample(fraction: Double): Sample = {
      // The range of fraction accepted by Sample is [0, 1]. Because Hive's block sampling
      // function takes X PERCENT as the input and the range of X is [0, 100], we need to
      // adjust the fraction.
      val eps = RandomSampler.roundingEpsilon
      validate(fraction >= 0.0 - eps && fraction <= 1.0 + eps,
        s"Sampling fraction ($fraction) must be on interval [0, 1]",
        ctx)
      Sample(0.0, fraction, withReplacement = false, (math.random * 1000).toInt, query)
    }

    if (ctx.sampleMethod() == null) {
      throw new ParseException("TABLESAMPLE does not accept empty inputs.", ctx)
    }

    ctx.sampleMethod() match {
      case ctx: ArcticExtendSparkSqlParser.SampleByRowsContext =>
        Limit(expression(ctx.expression), query)

      case ctx: ArcticExtendSparkSqlParser.SampleByPercentileContext =>
        val fraction = ctx.percentage.getText.toDouble
        val sign = if (ctx.negativeSign == null) 1 else -1
        sample(sign * fraction / 100.0d)

      case ctx: ArcticExtendSparkSqlParser.SampleByBytesContext =>
        val bytesStr = ctx.bytes.getText
        if (bytesStr.matches("[0-9]+[bBkKmMgG]")) {
          throw new ParseException("TABLESAMPLE(byteLengthLiteral) is not supported", ctx)
        } else {
          throw new ParseException(
            bytesStr + " is not a valid byte length literal, " +
              "expected syntax: DIGIT+ ('B' | 'K' | 'M' | 'G')", ctx)
        }

      case ctx: ArcticExtendSparkSqlParser.SampleByBucketContext if ctx.ON() != null =>
        if (ctx.identifier != null) {
          throw new ParseException(
            "TABLESAMPLE(BUCKET x OUT OF y ON colname) is not supported", ctx)
        } else {
          throw new ParseException(
            "TABLESAMPLE(BUCKET x OUT OF y ON function) is not supported", ctx)
        }

      case ctx: ArcticExtendSparkSqlParser.SampleByBucketContext =>
        sample(ctx.numerator.getText.toDouble / ctx.denominator.getText.toDouble)
    }
  }

  /**
   * If aliases specified in a FROM clause, create a subquery alias ([[SubqueryAlias]]) and
   * column aliases for a [[LogicalPlan]].
   */
  private def mayApplyAliasPlan(tableAlias: ArcticExtendSparkSqlParser.TableAliasContext, plan: LogicalPlan): LogicalPlan = {
    if (tableAlias.strictIdentifier != null) {
      val subquery = SubqueryAlias(tableAlias.strictIdentifier.getText, plan)
      if (tableAlias.identifierList != null) {
        val columnNames = visitIdentifierList(tableAlias.identifierList)
        UnresolvedSubqueryColumnAliases(columnNames, subquery)
      } else {
        subquery
      }
    } else {
      plan
    }
  }

  override def visitRelation(ctx: ArcticExtendSparkSqlParser.RelationContext): LogicalPlan = withOrigin(ctx) {
    withJoinRelations(plan(ctx.relationPrimary), ctx)
  }
  /**
   * Create sequence of expressions from the given sequence of contexts.
   */
  private def expressionList(trees: java.util.List[ArcticExtendSparkSqlParser.ExpressionContext]): Seq[Expression] = {
    trees.asScala.map(expression).toSeq
  }

  /**
   * Add a [[Generate]] (Lateral View) to a logical plan.
   */
  private def withGenerate(
                            query: LogicalPlan,
                            ctx: ArcticExtendSparkSqlParser.LateralViewContext): LogicalPlan = withOrigin(ctx) {
    val expressions = expressionList(ctx.expression)
    Generate(
      UnresolvedGenerator(visitFunctionName(ctx.qualifiedName), expressions),
      unrequiredChildIndex = Nil,
      outer = ctx.OUTER != null,
      // scalastyle:off caselocale
      Some(ctx.tblName.getText.toLowerCase),
      // scalastyle:on caselocale
      ctx.colName.asScala.map(_.getText).map(UnresolvedAttribute.apply).toSeq,
      query)
  }

  /**
   * Create a function database (optional) and name pair, for multipartIdentifier.
   * This is used in CREATE FUNCTION, DROP FUNCTION, SHOWFUNCTIONS.
   */
  protected def visitFunctionName(ctx: ArcticExtendSparkSqlParser.MultipartIdentifierContext): FunctionIdentifier = {
    visitFunctionName(ctx, ctx.parts.asScala.map(_.getText).toSeq)
  }

  /**
   * Create a function database (optional) and name pair.
   */
  protected def visitFunctionName(ctx: ArcticExtendSparkSqlParser.QualifiedNameContext): FunctionIdentifier = {
    visitFunctionName(ctx, ctx.identifier().asScala.map(_.getText).toSeq)
  }

  /**
   * Create a function database (optional) and name pair.
   */
  private def visitFunctionName(ctx: ParserRuleContext, texts: Seq[String]): FunctionIdentifier = {
    texts match {
      case Seq(db, fn) => FunctionIdentifier(fn, Option(db))
      case Seq(fn) => FunctionIdentifier(fn, None)
      case other =>
        throw new ParseException(s"Unsupported function name '${texts.mkString(".")}'", ctx)
    }
  }

  /**
   * Add a [[Pivot]] to a logical plan.
   */
  private def withPivot(
                         ctx: ArcticExtendSparkSqlParser.PivotClauseContext,
                         query: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    val aggregates = Option(ctx.aggregates).toSeq
      .flatMap(_.namedExpression.asScala)
      .map(typedVisit[Expression])
    val pivotColumn = if (ctx.pivotColumn.identifiers.size == 1) {
      UnresolvedAttribute.quoted(ctx.pivotColumn.identifier.getText)
    } else {
      CreateStruct(
        ctx.pivotColumn.identifiers.asScala.map(
          identifier => UnresolvedAttribute.quoted(identifier.getText)).toSeq)
    }
    val pivotValues = ctx.pivotValues.asScala.map(visitPivotValue)
    Pivot(None, pivotColumn, pivotValues.toSeq, aggregates, query)
  }

  /**
   * Create a Pivot column value with or without an alias.
   */
  override def visitPivotValue(ctx: ArcticExtendSparkSqlParser.PivotValueContext): Expression = withOrigin(ctx) {
    val e = expression(ctx.expression)
    if (ctx.identifier != null) {
      Alias(e, ctx.identifier.getText)()
    } else {
      e
    }
  }

  /**
   * Join one more [[LogicalPlan]]s to the current logical plan.
   */
  private def withJoinRelations(base: LogicalPlan, ctx: ArcticExtendSparkSqlParser.RelationContext): LogicalPlan = {
    ctx.joinRelation.asScala.foldLeft(base) { (left, join) =>
      withOrigin(join) {
        val baseJoinType = join.joinType match {
          case null => Inner
          case jt if jt.CROSS != null => Cross
          case jt if jt.FULL != null => FullOuter
          case jt if jt.SEMI != null => LeftSemi
          case jt if jt.ANTI != null => LeftAnti
          case jt if jt.LEFT != null => LeftOuter
          case jt if jt.RIGHT != null => RightOuter
          case _ => Inner
        }

        // Resolve the join type and join condition
        val (joinType, condition) = Option(join.joinCriteria) match {
          case Some(c) if c.USING != null =>
            (UsingJoin(baseJoinType, visitIdentifierList(c.identifierList)), None)
          case Some(c) if c.booleanExpression != null =>
            (baseJoinType, Option(expression(c.booleanExpression)))
          case Some(c) =>
            throw new ParseException(s"Unimplemented joinCriteria: $c", ctx)
          case None if join.NATURAL != null =>
            if (baseJoinType == Cross) {
              throw new ParseException("NATURAL CROSS JOIN is not supported", ctx)
            }
            (NaturalJoin(baseJoinType), None)
          case None =>
            (baseJoinType, None)
        }
        Join(left, plan(join.right), joinType, condition, JoinHint.NONE)
      }
    }
  }

  private def withFromStatementBody(
                                     ctx: ArcticExtendSparkSqlParser.FromStatementBodyContext, plan: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    // two cases for transforms and selects
    if (ctx.transformClause != null) {
      withTransformQuerySpecification(
        ctx,
        ctx.transformClause,
        ctx.whereClause,
        plan
      )
    } else {
      withSelectQuerySpecification(
        ctx,
        ctx.selectClause,
        ctx.lateralView,
        ctx.whereClause,
        ctx.aggregationClause,
        ctx.havingClause,
        ctx.windowClause,
        plan
      )
    }
  }

  /**
   * Create a logical plan using a where clause.
   */
  private def withWhereClause(ctx: ArcticExtendSparkSqlParser.WhereClauseContext, plan: LogicalPlan): LogicalPlan = {
    Filter(expression(ctx.booleanExpression), plan)
  }

  protected def expression(ctx: ParserRuleContext): Expression = typedVisit(ctx)

  override def visitExpressionSeq(
                                        ctx: ArcticExtendSparkSqlParser.ExpressionSeqContext): Seq[Expression] = {
    Option(ctx).toSeq
      .flatMap(_.expression.asScala)
      .map(typedVisit[Expression])
  }

  /**
   * Create a predicated expression. A predicated expression is a normal expression with a
   * predicate attached to it, for example:
   * {{{
   *    a + 1 IS NULL
   * }}}
   */
  override def visitPredicated(ctx: ArcticExtendSparkSqlParser.PredicatedContext): Expression = withOrigin(ctx) {
    val e = expression(ctx.valueExpression)
    if (ctx.predicate != null) {
      withPredicate(e, ctx.predicate)
    } else {
      e
    }
  }

  /**
   * Create a [[CreateStruct]] expression.
   */
  override def visitStruct(ctx: ArcticExtendSparkSqlParser.StructContext): Expression = withOrigin(ctx) {
    CreateStruct.create(ctx.argument.asScala.map(expression))
  }

  /**
   * Create a comparison expression. This compares two expressions. The following comparison
   * operators are supported:
   * - Equal: '=' or '=='
   * - Null-safe Equal: '<=>'
   * - Not Equal: '<>' or '!='
   * - Less than: '<'
   * - Less then or Equal: '<='
   * - Greater than: '>'
   * - Greater then or Equal: '>='
   */
  override def visitComparison(ctx: ArcticExtendSparkSqlParser.ComparisonContext): Expression = withOrigin(ctx) {
    val left = expression(ctx.left)
    val right = expression(ctx.right)
    val operator = ctx.comparisonOperator().getChild(0).asInstanceOf[TerminalNode]
    operator.getSymbol.getType match {
      case SqlBaseParser.EQ =>
        EqualTo(left, right)
      case SqlBaseParser.NSEQ =>
        EqualNullSafe(left, right)
      case SqlBaseParser.NEQ | SqlBaseParser.NEQJ =>
        Not(EqualTo(left, right))
      case SqlBaseParser.LT =>
        LessThan(left, right)
      case SqlBaseParser.LTE =>
        LessThanOrEqual(left, right)
      case SqlBaseParser.GT =>
        GreaterThan(left, right)
      case SqlBaseParser.GTE =>
        GreaterThanOrEqual(left, right)
    }
  }

  /**
   * Create a binary arithmetic expression. The following arithmetic operators are supported:
   * - Multiplication: '*'
   * - Division: '/'
   * - Hive Long Division: 'DIV'
   * - Modulo: '%'
   * - Addition: '+'
   * - Subtraction: '-'
   * - Binary AND: '&'
   * - Binary XOR
   * - Binary OR: '|'
   */
  override def visitArithmeticBinary(ctx: ArcticExtendSparkSqlParser.ArithmeticBinaryContext): Expression = withOrigin(ctx) {
    val left = expression(ctx.left)
    val right = expression(ctx.right)
    ctx.operator.getType match {
      case SqlBaseParser.ASTERISK =>
        Multiply(left, right)
      case SqlBaseParser.SLASH =>
        Divide(left, right)
      case SqlBaseParser.PERCENT =>
        Remainder(left, right)
      case SqlBaseParser.DIV =>
        IntegralDivide(left, right)
      case SqlBaseParser.PLUS =>
        Add(left, right)
      case SqlBaseParser.MINUS =>
        Subtract(left, right)
      case SqlBaseParser.CONCAT_PIPE =>
        Concat(left :: right :: Nil)
      case SqlBaseParser.AMPERSAND =>
        BitwiseAnd(left, right)
      case SqlBaseParser.HAT =>
        BitwiseXor(left, right)
      case SqlBaseParser.PIPE =>
        BitwiseOr(left, right)
    }
  }

  /**
   * Create a unary arithmetic expression. The following arithmetic operators are supported:
   * - Plus: '+'
   * - Minus: '-'
   * - Bitwise Not: '~'
   */
  override def visitArithmeticUnary(ctx: ArcticExtendSparkSqlParser.ArithmeticUnaryContext): Expression = withOrigin(ctx) {
    val value = expression(ctx.valueExpression)
    ctx.operator.getType match {
      case SqlBaseParser.PLUS =>
        UnaryPositive(value)
      case SqlBaseParser.MINUS =>
        UnaryMinus(value)
      case SqlBaseParser.TILDE =>
        BitwiseNot(value)
    }
  }

  override def visitLogicalNot(ctx: ArcticExtendSparkSqlParser.LogicalNotContext): Expression = withOrigin(ctx) {
    Not(expression(ctx.booleanExpression()))
  }

  /**
   * Add a predicate to the given expression. Supported expressions are:
   * - (NOT) BETWEEN
   * - (NOT) IN
   * - (NOT) LIKE
   * - (NOT) RLIKE
   * - IS (NOT) NULL.
   * - IS (NOT) (TRUE | FALSE | UNKNOWN)
   * - IS (NOT) DISTINCT FROM
   */
  private def withPredicate(e: Expression, ctx: ArcticExtendSparkSqlParser.PredicateContext): Expression = withOrigin(ctx) {
    // Invert a predicate if it has a valid NOT clause.
    def invertIfNotDefined(e: Expression): Expression = ctx.NOT match {
      case null => e
      case not => Not(e)
    }

    def getValueExpressions(e: Expression): Seq[Expression] = e match {
      case c: CreateNamedStruct => c.valExprs
      case other => Seq(other)
    }

    // Create the predicate.
    ctx.kind.getType match {
      case SqlBaseParser.BETWEEN =>
        // BETWEEN is translated to lower <= e && e <= upper
        invertIfNotDefined(And(
          GreaterThanOrEqual(e, expression(ctx.lower)),
          LessThanOrEqual(e, expression(ctx.upper))))
      case SqlBaseParser.IN if ctx.query != null =>
        invertIfNotDefined(InSubquery(getValueExpressions(e), ListQuery(plan(ctx.query))))
      case SqlBaseParser.IN =>
        invertIfNotDefined(In(e, ctx.expression.asScala.map(expression)))
      case SqlBaseParser.LIKE =>
        val escapeChar = Option(ctx.escapeChar).map(string).map { str =>
          if (str.length != 1) {
            throw new ParseException("Invalid escape string." +
              "Escape string must contains only one character.", ctx)
          }
          str.charAt(0)
        }.getOrElse('\\')
        invertIfNotDefined(Like(e, expression(ctx.pattern), escapeChar))
      case SqlBaseParser.RLIKE =>
        invertIfNotDefined(RLike(e, expression(ctx.pattern)))
      case SqlBaseParser.NULL if ctx.NOT != null =>
        IsNotNull(e)
      case SqlBaseParser.NULL =>
        IsNull(e)
      case SqlBaseParser.TRUE => ctx.NOT match {
        case null => EqualNullSafe(e, Literal(true))
        case _ => Not(EqualNullSafe(e, Literal(true)))
      }
      case SqlBaseParser.FALSE => ctx.NOT match {
        case null => EqualNullSafe(e, Literal(false))
        case _ => Not(EqualNullSafe(e, Literal(false)))
      }
      case SqlBaseParser.UNKNOWN => ctx.NOT match {
        case null => IsUnknown(e)
        case _ => IsNotUnknown(e)
      }
      case SqlBaseParser.DISTINCT if ctx.NOT != null =>
        EqualNullSafe(e, expression(ctx.right))
      case SqlBaseParser.DISTINCT =>
        Not(EqualNullSafe(e, expression(ctx.right)))
    }
  }

  /**
   * Create top level table schema.
   */
  protected def createSchema(ctx: ArcticExtendSparkSqlParser.ColTypeListContext): StructType = {
    StructType(Option(ctx).toSeq.flatMap(visitColTypeList))
  }

  private def withTransformQuerySpecification(
                                               ctx: ParserRuleContext,
                                               transformClause: ArcticExtendSparkSqlParser.TransformClauseContext,
                                               whereClause: ArcticExtendSparkSqlParser.WhereClauseContext,
                                               relation: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    // Add where.
    val withFilter = relation.optionalMap(whereClause)(withWhereClause)

    // Create the transform.
    val expressions = visitExpressionSeq(transformClause.expressionSeq)

    // Create the attributes.
    val (attributes, schemaLess) = if (transformClause.colTypeList != null) {
      // Typed return columns.
      (createSchema(transformClause.colTypeList).toAttributes, false)
    } else if (transformClause.identifierSeq != null) {
      // Untyped return columns.
      val attrs = visitIdentifierSeq(transformClause.identifierSeq).map { name =>
        AttributeReference(name, StringType, nullable = true)()
      }
      (attrs, false)
    } else {
      (Seq(AttributeReference("key", StringType)(),
        AttributeReference("value", StringType)()), true)
    }

    // Create the transform.
    ScriptTransformation(
      expressions,
      string(transformClause.script),
      attributes,
      withFilter,
      withScriptIOSchema(
        ctx,
        transformClause.inRowFormat,
        transformClause.recordWriter,
        transformClause.outRowFormat,
        transformClause.recordReader,
        schemaLess
      )
    )
  }

  /**
   * Create a (Hive based) [[ScriptInputOutputSchema]].
   */
  protected def withScriptIOSchema(
                                    ctx: ParserRuleContext,
                                    inRowFormat: ArcticExtendSparkSqlParser.RowFormatContext,
                                    recordWriter: Token,
                                    outRowFormat: ArcticExtendSparkSqlParser.RowFormatContext,
                                    recordReader: Token,
                                    schemaLess: Boolean): ScriptInputOutputSchema = {
    throw new ParseException("Script Transform is not supported", ctx)
  }

  override def visitNamedExpressionSeq(
                                        ctx: ArcticExtendSparkSqlParser.NamedExpressionSeqContext): Seq[Expression] = {
    Option(ctx).toSeq
      .flatMap(_.namedExpression.asScala)
      .map(typedVisit[Expression])
  }

  override def visitNamedExpression(ctx: ArcticExtendSparkSqlParser.NamedExpressionContext): Expression = withOrigin(ctx) {
    val e = expression(ctx.expression)
    if (ctx.name != null) {
      Alias(e, ctx.name.getText)()
    } else if (ctx.identifierList != null) {
      MultiAlias(e, visitIdentifierList(ctx.identifierList))
    } else {
      e
    }
  }

  override def visitColumnReference(ctx: ArcticExtendSparkSqlParser.ColumnReferenceContext): Expression = withOrigin(ctx) {
    ctx.getStart.getText match {
      case escapedIdentifier(columnNameRegex)
        if conf.supportQuotedRegexColumnName && canApplyRegex(ctx) =>
        UnresolvedRegex(columnNameRegex, None, conf.caseSensitiveAnalysis)
      case _ =>
        UnresolvedAttribute.quoted(ctx.getText)
    }

  }

  private def canApplyRegex(ctx: ParserRuleContext): Boolean = withOrigin(ctx) {
    var parent = ctx.getParent
    while (parent != null) {
      if (parent.isInstanceOf[ArcticExtendSparkSqlParser.NamedExpressionContext]) return true
      parent = parent.getParent
    }
    return false
  }

  /**
   * Add a regular (SELECT) query specification to a logical plan. The query specification
   * is the core of the logical plan, this is where sourcing (FROM clause), projection (SELECT),
   * aggregation (GROUP BY ... HAVING ...) and filtering (WHERE) takes place.
   *
   * Note that query hints are ignored (both by the parser and the builder).
   */
  private def withSelectQuerySpecification(
                                            ctx: ParserRuleContext,
                                            selectClause: ArcticExtendSparkSqlParser.SelectClauseContext,
                                            lateralView: java.util.List[ArcticExtendSparkSqlParser.LateralViewContext],
                                            whereClause: ArcticExtendSparkSqlParser.WhereClauseContext,
                                            aggregationClause: ArcticExtendSparkSqlParser.AggregationClauseContext,
                                            havingClause: ArcticExtendSparkSqlParser.HavingClauseContext,
                                            windowClause: ArcticExtendSparkSqlParser.WindowClauseContext,
                                            relation: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    // Add lateral views.
    val withLateralView = lateralView.asScala.foldLeft(relation)(withGenerate)

    // Add where.
    val withFilter = withLateralView.optionalMap(whereClause)(withWhereClause)

    val expressions = visitNamedExpressionSeq(selectClause.namedExpressionSeq)
    // Add aggregation or a project.
    val namedExpressions = expressions.map {
      case e: NamedExpression => e
      case e: Expression => UnresolvedAlias(e)
    }

    def createProject() = if (namedExpressions.nonEmpty) {
      Project(namedExpressions, withFilter)
    } else {
      withFilter
    }

    val withProject = if (aggregationClause == null && havingClause != null) {
      if (conf.getConf(SQLConf.LEGACY_HAVING_WITHOUT_GROUP_BY_AS_WHERE)) {
        // If the legacy conf is set, treat HAVING without GROUP BY as WHERE.
        val predicate = expression(havingClause.booleanExpression) match {
          case p: Predicate => p
          case e => Cast(e, BooleanType)
        }
        Filter(predicate, createProject())
      } else {
        // According to SQL standard, HAVING without GROUP BY means global aggregate.
        withHavingClause(havingClause, Aggregate(Nil, namedExpressions, withFilter))
      }
    } else if (aggregationClause != null) {
      val aggregate = withAggregationClause(aggregationClause, namedExpressions, withFilter)
      aggregate.optionalMap(havingClause)(withHavingClause)
    } else {
      // When hitting this branch, `having` must be null.
      createProject()
    }

    // Distinct
    val withDistinct = if (
      selectClause.setQuantifier() != null &&
        selectClause.setQuantifier().DISTINCT() != null) {
      Distinct(withProject)
    } else {
      withProject
    }

    // Window
    val withWindow = withDistinct.optionalMap(windowClause)(withWindowClause)

    // Hint
    selectClause.hints.asScala.foldRight(withWindow)(withHints)
  }

  /**
   * Add a [[WithWindowDefinition]] operator to a logical plan.
   */
  private def withWindowClause(
                                ctx: ArcticExtendSparkSqlParser.WindowClauseContext,
                                query: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    // Collect all window specifications defined in the WINDOW clause.
    val baseWindowTuples = ctx.namedWindow.asScala.map {
      wCtx =>
        (wCtx.name.getText, typedVisit[WindowSpec](wCtx.windowSpec))
    }
    baseWindowTuples.groupBy(_._1).foreach { kv =>
      if (kv._2.size > 1) {
        throw new ParseException(s"The definition of window '${kv._1}' is repetitive", ctx)
      }
    }
    val baseWindowMap = baseWindowTuples.toMap

    // Handle cases like
    // window w1 as (partition by p_mfgr order by p_name
    //               range between 2 preceding and 2 following),
    //        w2 as w1
    val windowMapView = baseWindowMap.mapValues {
      case WindowSpecReference(name) =>
        baseWindowMap.get(name) match {
          case Some(spec: WindowSpecDefinition) =>
            spec
          case Some(ref) =>
            throw new ParseException(s"Window reference '$name' is not a window specification", ctx)
          case None =>
            throw new ParseException(s"Cannot resolve window reference '$name'", ctx)
        }
      case spec: WindowSpecDefinition => spec
    }

    // Note that mapValues creates a view instead of materialized map. We force materialization by
    // mapping over identity.
    WithWindowDefinition(windowMapView.map(identity).toMap, query)
  }

  /**
   * Add [[UnresolvedHint]]s to a logical plan.
   */
  private def withHints(
                         ctx: ArcticExtendSparkSqlParser.HintContext,
                         query: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    var plan = query
    ctx.hintStatements.asScala.reverse.foreach { stmt =>
      plan = UnresolvedHint(stmt.hintName.getText,
        stmt.parameters.asScala.map(expression).toSeq, plan)
    }
    plan
  }

  /**
   * Add an [[Aggregate]] or [[GroupingSets]] to a logical plan.
   */
  private def withAggregationClause(
                                     ctx: ArcticExtendSparkSqlParser.AggregationClauseContext,
                                     selectExpressions: Seq[NamedExpression],
                                     query: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    val groupByExpressions = expressionList(ctx.groupingExpressions)

    if (ctx.GROUPING != null) {
      // GROUP BY .... GROUPING SETS (...)
      val selectedGroupByExprs =
        ctx.groupingSet.asScala.map(_.expression.asScala.map(e => expression(e)).toSeq)
      GroupingSets(selectedGroupByExprs.toSeq, groupByExpressions, query, selectExpressions)
    } else {
      // GROUP BY .... (WITH CUBE | WITH ROLLUP)?
      val mappedGroupByExpressions = if (ctx.CUBE != null) {
        Seq(Cube(groupByExpressions))
      } else if (ctx.ROLLUP != null) {
        Seq(Rollup(groupByExpressions))
      } else {
        groupByExpressions
      }
      Aggregate(mappedGroupByExpressions, selectExpressions, query)
    }
  }


  /**
   * Create a logical plan using a having clause.
   */
  private def withHavingClause(
                                ctx: ArcticExtendSparkSqlParser.HavingClauseContext, plan: LogicalPlan): LogicalPlan = {
    // Note that we add a cast to non-predicate expressions. If the expression itself is
    // already boolean, the optimizer will get rid of the unnecessary cast.
    val predicate = expression(ctx.booleanExpression) match {
      case p: Predicate => p
      case e => Cast(e, BooleanType)
    }
    UnresolvedHaving(predicate, plan)
  }
  /**
   * Create a named logical plan.
   *
   * This is only used for Common Table Expressions.
   */
  override def visitNamedQuery(ctx: ArcticExtendSparkSqlParser.NamedQueryContext): SubqueryAlias = withOrigin(ctx) {
    val subQuery: LogicalPlan = plan(ctx.query).optionalMap(ctx.columnAliases)(
      (columnAliases, plan) =>
        UnresolvedSubqueryColumnAliases(visitIdentifierList(columnAliases), plan)
    )
    SubqueryAlias(ctx.name.getText, subQuery)
  }

  private def withCTE(ctx: ArcticExtendSparkSqlParser.CtesContext, plan: LogicalPlan): LogicalPlan = {
    val ctes = ctx.namedQuery.asScala.map { nCtx =>
      val namedQuery = visitNamedQuery(nCtx)
      (namedQuery.alias, namedQuery)
    }
    // Check for duplicate names.
    val duplicates = ctes.groupBy(_._1).filter(_._2.size > 1).keys
    if (duplicates.nonEmpty) {
      throw new ParseException(
        s"CTE definition can't have duplicate names: ${duplicates.mkString("'", "', '", "'")}.",
        ctx)
    }
    With(plan, ctes.toSeq)
  }

  /**
   * Add ORDER BY/SORT BY/CLUSTER BY/DISTRIBUTE BY/LIMIT/WINDOWS clauses to the logical plan. These
   * clauses determine the shape (ordering/partitioning/rows) of the query result.
   */
  private def withQueryResultClauses(
                                      ctx: ArcticExtendSparkSqlParser.QueryOrganizationContext,
                                      query: LogicalPlan): LogicalPlan = withOrigin(ctx) {
    query
  }

  private def typedVisit[T](ctx: ParseTree): T = {
    ctx.accept(this).asInstanceOf[T]
  }

  override def visitSingleDataType(ctx: ArcticExtendSparkSqlParser.SingleDataTypeContext): DataType = withOrigin(ctx) {
    typedVisit[DataType](ctx.dataType)
  }

  /**
   * Override the default behavior for all visit methods. This will only return a non-null result
   * when the context has only one child. This is done because there is no generic method to
   * combine the results of the context children. In all other cases null is returned.
   */
  override def visitChildren(node: RuleNode): AnyRef = {
    if (node.getChildCount == 1) {
      node.getChild(0).accept(this)
    } else {
      null
    }
  }


  override def visitSingleStatement(ctx: ArcticExtendSparkSqlParser.SingleStatementContext): LogicalPlan = withOrigin(ctx) {
    visit(ctx.statement).asInstanceOf[LogicalPlan]
  }

//  override def


  def setPrimaryKeyNotNull(columns: Seq[StructField], primary: Seq[String]): Seq[StructField] = {
    columns.map(c =>
      if (primary.contains(c.name)) {
        StructField(
          name = c.name,
          dataType = c.dataType,
          nullable = false,
          metadata = c.metadata)
      } else {
        c
      }
    )
  }

  override def visitCreateTable(ctx: ArcticExtendSparkSqlParser.CreateTableContext): LogicalPlan = withOrigin(ctx) {
    val (table, temp, ifNotExists, external) = visitCreateTableHeader(ctx.createTableHeader)

    val columns = Option(ctx.colTypeList()).map(visitColTypeList).getOrElse(Nil)
    val provider = Option(ctx.tableProvider).map(_.multipartIdentifier.getText)
    val (partTransforms, partCols, bucketSpec, properties, options, location, comment, serdeInfo, primaryForCtas) =
      visitCreateTableClauses(ctx.createTableClauses())

    if (provider.isDefined && serdeInfo.isDefined) {
      operationNotAllowed(s"CREATE TABLE ... USING ... ${serdeInfo.get.describe}", ctx)
    }

    if (temp) {
      val asSelect = if (ctx.query == null) "" else " AS ..."
      operationNotAllowed(
        s"CREATE TEMPORARY TABLE ...$asSelect, use CREATE TEMPORARY VIEW instead", ctx)
    }

    val partitioning = partitionExpressions(partTransforms, partCols, ctx)

    Option(ctx.query).map(plan) match {
      case Some(_) if columns.nonEmpty =>
        operationNotAllowed(
          "Schema may not be specified in a Create Table As Select (CTAS) statement",
          ctx)

      case Some(_) if partCols.nonEmpty =>
        // non-reference partition columns are not allowed because schema can't be specified
        operationNotAllowed(
          "Partition column types may not be specified in Create Table As Select (CTAS)",
          ctx)

      case Some(query) =>
        val propertiesMap = buildProperties(primaryForCtas.get, properties)
        CreateTableAsSelectStatement(
          table, query, partitioning, bucketSpec, propertiesMap, provider, options, location, comment,
          writeOptions = Map.empty, serdeInfo, external = external, ifNotExists = ifNotExists)

      case _ =>
        // Note: table schema includes both the table columns list and the partition columns
        // with data type.
        if (primaryForCtas.nonEmpty) {
          operationNotAllowed(
            "Primary keys do not allow this input format in a Create Table",
            ctx)
        }
        val primary = visitPrimarySpec(ctx.colTypeList().primarySpec())
        // Setting the primary key not nullable
        val newColumns = setPrimaryKeyNotNull(columns, primary)
        val schema = StructType(newColumns ++ partCols)
        val propertiesMap = buildProperties(primary, properties)
        CreateTableStatement(table, schema, partitioning, bucketSpec, propertiesMap, provider,
          options, location, comment, serdeInfo, external = external, ifNotExists = ifNotExists)
    }
  }

  /**
   * Build a properties with primary key.
   */
  private def buildProperties(primary: Seq[String], properties: Map[String, String]) = {
    var propertiesMap: Map[String, String] = properties
    propertiesMap += ("provider" -> "arctic")
    val primaryMap = seqAsJavaList(primary)
    if (primaryMap != null) {
      propertiesMap += ("primary.keys" -> String.join(",", primaryMap))
    }
    propertiesMap
  }

  /**
   * Create a [[StructType]] from a number of column definitions.
   */
  override def visitColTypeList(ctx: ArcticExtendSparkSqlParser.ColTypeListContext): Seq[StructField] = withOrigin(ctx) {
    ctx.colType().asScala.map(visitColType).toSeq
  }

  /**
   * Create a top level [[StructField]] from a column definition.
   */
  override def visitColType(ctx: ArcticExtendSparkSqlParser.ColTypeContext): StructField = withOrigin(ctx) {

    val builder = new MetadataBuilder
    // Add comment to metadata
    Option(ctx.commentSpec).map(visitCommentSpec).foreach {
      builder.putString("comment", _)
    }

    StructField(
      name = ctx.colName.getText,
      dataType = typedVisit[DataType](ctx.dataType),
      nullable = ctx.NULL() == null,
      metadata = builder.build())
  }

  type TableClauses = (
    Seq[Transform], Seq[StructField], Option[BucketSpec], Map[String, String],
      Map[String, String], Option[String], Option[String], Option[SerdeInfo],  Option[Seq[String]])


  protected def visitPrimarySpecList(ctx: util.List[ArcticExtendSparkSqlParser.PrimarySpecContext]): Option[Seq[String]] = {
    ctx.asScala.headOption.map(visitPrimarySpec)
  }

  override def visitPrimitiveDataType(ctx: ArcticExtendSparkSqlParser.PrimitiveDataTypeContext): DataType = withOrigin(ctx) {
    val dataType = ctx.identifier.getText.toLowerCase(Locale.ROOT)
    (dataType, ctx.INTEGER_VALUE().asScala.toList) match {
      case ("boolean", Nil) => BooleanType
      case ("tinyint" | "byte", Nil) => ByteType
      case ("smallint" | "short", Nil) => ShortType
      case ("int" | "integer", Nil) => IntegerType
      case ("bigint" | "long", Nil) => LongType
      case ("float" | "real", Nil) => FloatType
      case ("double", Nil) => DoubleType
      case ("date", Nil) => DateType
      case ("timestamp", Nil) => TimestampType
      case ("string", Nil) => StringType
      case ("character" | "char", length :: Nil) => CharType(length.getText.toInt)
      case ("varchar", length :: Nil) => VarcharType(length.getText.toInt)
      case ("binary", Nil) => BinaryType
      case ("decimal" | "dec" | "numeric", Nil) => DecimalType.USER_DEFAULT
      case ("decimal" | "dec" | "numeric", precision :: Nil) =>
        DecimalType(precision.getText.toInt, 0)
      case ("decimal" | "dec" | "numeric", precision :: scale :: Nil) =>
        DecimalType(precision.getText.toInt, scale.getText.toInt)
      case ("void", Nil) => NullType
      case ("interval", Nil) => CalendarIntervalType
      case (dt, params) =>
        val dtStr = if (params.nonEmpty) s"$dt(${params.mkString(",")})" else dt
        throw new ParseException(s"DataType $dtStr is not supported.", ctx)
    }
  }

  /**
   * Create a comment string.
   */
  override def visitPrimarySpec(ctx: ArcticExtendSparkSqlParser.PrimarySpecContext): Seq[String] = withOrigin(ctx) {
    visitIdentifierList(ctx.identifierList())
  }


  override def visitCreateTableClauses(ctx: ArcticExtendSparkSqlParser.CreateTableClausesContext): TableClauses = {
    checkDuplicateClauses(ctx.TBLPROPERTIES, "TBLPROPERTIES", ctx)
    checkDuplicateClauses(ctx.OPTIONS, "OPTIONS", ctx)
    checkDuplicateClauses(ctx.PARTITIONED, "PARTITIONED BY", ctx)
    checkDuplicateClauses(ctx.createFileFormat, "STORED AS/BY", ctx)
    checkDuplicateClauses(ctx.rowFormat, "ROW FORMAT", ctx)
    checkDuplicateClauses(ctx.commentSpec(), "COMMENT", ctx)
    checkDuplicateClauses(ctx.bucketSpec(), "CLUSTERED BY", ctx)
    checkDuplicateClauses(ctx.locationSpec, "LOCATION", ctx)

    if (ctx.skewSpec.size > 0) {
      operationNotAllowed("CREATE TABLE ... SKEWED BY", ctx)
    }
    val (partTransforms, partCols) =
      Option(ctx.partitioning).map(visitPartitionFieldList).getOrElse((Nil, Nil))
    val bucketSpec = ctx.bucketSpec().asScala.headOption.map(visitBucketSpec)
    val properties = Option(ctx.tableProps).map(visitPropertyKeyValues).getOrElse(Map.empty)
    val cleanedProperties = cleanTableProperties(ctx, properties)
    val options = Option(ctx.options).map(visitPropertyKeyValues).getOrElse(Map.empty)
    val location = visitLocationSpecList(ctx.locationSpec())
    val (cleanedOptions, newLocation) = cleanTableOptions(ctx, options, location)
    val comment = visitCommentSpecList(ctx.commentSpec())
    val primary = visitPrimarySpecList(ctx.primarySpec())
    val serdeInfo =
      getSerdeInfo(ctx.rowFormat.asScala.toSeq, ctx.createFileFormat.asScala.toSeq, ctx)
    (partTransforms, partCols, bucketSpec, cleanedProperties, cleanedOptions, newLocation, comment,
      serdeInfo, primary)
  }

  protected def getSerdeInfo(
                              rowFormatCtx: Seq[ArcticExtendSparkSqlParser.RowFormatContext],
                              createFileFormatCtx: Seq[ArcticExtendSparkSqlParser.CreateFileFormatContext],
                              ctx: ParserRuleContext,
                              skipCheck: Boolean = false): Option[SerdeInfo] = {
    if (!skipCheck) validateRowFormatFileFormat(rowFormatCtx, createFileFormatCtx, ctx)
    val rowFormatSerdeInfo = rowFormatCtx.map(visitRowFormat)
    val fileFormatSerdeInfo = createFileFormatCtx.map(visitCreateFileFormat)
    (fileFormatSerdeInfo ++ rowFormatSerdeInfo).reduceLeftOption((l, r) => l.merge(r))
  }

  def visitRowFormat(ctx: ArcticExtendSparkSqlParser.RowFormatContext): SerdeInfo = withOrigin(ctx) {
    ctx match {
      case serde: ArcticExtendSparkSqlParser.RowFormatSerdeContext => visitRowFormatSerde(serde)
      case delimited: ArcticExtendSparkSqlParser.RowFormatDelimitedContext => visitRowFormatDelimited(delimited)
    }
  }

  /**
   * Create a [[SerdeInfo]] for creating tables.
   *
   * Format: STORED AS (name | INPUTFORMAT input_format OUTPUTFORMAT output_format)
   */
  override def visitCreateFileFormat(ctx: ArcticExtendSparkSqlParser.CreateFileFormatContext): SerdeInfo = withOrigin(ctx) {
    (ctx.fileFormat, ctx.storageHandler) match {
      // Expected format: INPUTFORMAT input_format OUTPUTFORMAT output_format
      case (c: ArcticExtendSparkSqlParser.TableFileFormatContext, null) =>
        SerdeInfo(formatClasses = Some(FormatClasses(string(c.inFmt), string(c.outFmt))))
      // Expected format: SEQUENCEFILE | TEXTFILE | RCFILE | ORC | PARQUET | AVRO
      case (c: ArcticExtendSparkSqlParser.GenericFileFormatContext, null) =>
        SerdeInfo(storedAs = Some(c.identifier.getText))
      case (null, storageHandler) =>
        operationNotAllowed("STORED BY", ctx)
      case _ =>
        throw new ParseException("Expected either STORED AS or STORED BY, not both", ctx)
    }
  }

  /**
   * Create a delimited row format properties object.
   */
  override def visitRowFormatDelimited(
                                        ctx: ArcticExtendSparkSqlParser.RowFormatDelimitedContext): SerdeInfo = withOrigin(ctx) {
    // Collect the entries if any.
    def entry(key: String, value: Token): Seq[(String, String)] = {
      Option(value).toSeq.map(x => key -> string(x))
    }
    // TODO we need proper support for the NULL format.
    val entries =
      entry("field.delim", ctx.fieldsTerminatedBy) ++
        entry("serialization.format", ctx.fieldsTerminatedBy) ++
        entry("escape.delim", ctx.escapedBy) ++
        // The following typo is inherited from Hive...
        entry("colelction.delim", ctx.collectionItemsTerminatedBy) ++
        entry("mapkey.delim", ctx.keysTerminatedBy) ++
        Option(ctx.linesSeparatedBy).toSeq.map { token =>
          val value = string(token)
          validate(
            value == "\n",
            s"LINES TERMINATED BY only supports newline '\\n' right now: $value",
            ctx)
          "line.delim" -> value
        }
    SerdeInfo(serdeProperties = entries.toMap)
  }

  /**
   * Create SERDE row format name and properties pair.
   */
  override def visitRowFormatSerde(ctx: ArcticExtendSparkSqlParser.RowFormatSerdeContext): SerdeInfo = withOrigin(ctx) {
    SerdeInfo(
      serde = Some(string(ctx.name)),
      serdeProperties = Option(ctx.tablePropertyList).map(visitPropertyKeyValues).getOrElse(Map.empty))
  }

  protected def validateRowFormatFileFormat(
                                             rowFormatCtx: Seq[ArcticExtendSparkSqlParser.RowFormatContext],
                                             createFileFormatCtx: Seq[ArcticExtendSparkSqlParser.CreateFileFormatContext],
                                             parentCtx: ParserRuleContext): Unit = {
    if (rowFormatCtx.size == 1 && createFileFormatCtx.size == 1) {
      validateRowFormatFileFormat(rowFormatCtx, createFileFormatCtx, parentCtx)
    }
  }

  /**
   * Create an optional comment string.
   */
  protected def visitCommentSpecList(ctx: java.util.List[ArcticExtendSparkSqlParser.CommentSpecContext]): Option[String] = {
    ctx.asScala.headOption.map(visitCommentSpec)
  }

  /**
   * Create a comment string.
   */
  override def visitCommentSpec(ctx: ArcticExtendSparkSqlParser.CommentSpecContext): String = withOrigin(ctx) {
    string(ctx.STRING)
  }

  /**
   * Create an optional location string.
   */
  protected def visitLocationSpecList(ctx: java.util.List[ArcticExtendSparkSqlParser.LocationSpecContext]): Option[String] = {
    ctx.asScala.headOption.map(visitLocationSpec)
  }

  /**
   * Create a location string.
   */
  override def visitLocationSpec(ctx: ArcticExtendSparkSqlParser.LocationSpecContext): String = withOrigin(ctx) {
    string(ctx.STRING)
  }

  /**
   * Parse a key-value map from a [[TablePropertyListContext]], assuming all values are specified.
   */
  def visitPropertyKeyValues(ctx: ArcticExtendSparkSqlParser.TablePropertyListContext): Map[String, String] = {
    val props = visitTablePropertyList(ctx)
    val badKeys = props.collect { case (key, null) => key }
    if (badKeys.nonEmpty) {
      operationNotAllowed(
        s"Values must be specified for key(s): ${badKeys.mkString("[", ",", "]")}", ctx)
    }
    props
  }

  /**
   * Convert a table property list into a key-value map.
   * This should be called through [[visitPropertyKeyValues]] or [[visitPropertyKeys]].
   */
  override def visitTablePropertyList(
                                       ctx: ArcticExtendSparkSqlParser.TablePropertyListContext): Map[String, String] = withOrigin(ctx) {
    val properties = ctx.tableProperty.asScala.map { property =>
      val key = visitTablePropertyKey(property.key)
      val value = visitTablePropertyValue(property.value)
      key -> value
    }
    // Check for duplicate property names.
    checkDuplicateKeys(properties.toSeq, ctx)
    properties.toMap
  }

  /**
   * A table property key can either be String or a collection of dot separated elements. This
   * function extracts the property key based on whether its a string literal or a table property
   * identifier.
   */
  override def visitTablePropertyKey(key: ArcticExtendSparkSqlParser.TablePropertyKeyContext): String = {
    if (key.STRING != null) {
      string(key.STRING)
    } else {
      key.getText
    }
  }

  /**
   * A table property value can be String, Integer, Boolean or Decimal. This function extracts
   * the property value based on whether its a string, integer, boolean or decimal literal.
   */
  override def visitTablePropertyValue(value: ArcticExtendSparkSqlParser.TablePropertyValueContext): String = {
    if (value == null) {
      null
    } else if (value.STRING != null) {
      string(value.STRING)
    } else if (value.booleanValue != null) {
      value.getText.toLowerCase(Locale.ROOT)
    } else {
      value.getText
    }
  }

  /**
   * Create a [[BucketSpec]].
   */
  override def visitBucketSpec(ctx: ArcticExtendSparkSqlParser.BucketSpecContext): BucketSpec = withOrigin(ctx) {
    BucketSpec(
      ctx.INTEGER_VALUE.getText.toInt,
      visitIdentifierList(ctx.identifierList),
      Option(ctx.orderedIdentifierList)
        .toSeq
        .flatMap(_.orderedIdentifier.asScala)
        .map { orderedIdCtx =>
          Option(orderedIdCtx.ordering).map(_.getText).foreach { dir =>
            if (dir.toLowerCase(Locale.ROOT) != "asc") {
              operationNotAllowed(s"Column ordering must be ASC, was '$dir'", ctx)
            }
          }

          orderedIdCtx.ident.getText
        })
  }

  /**
   * Create a Sequence of Strings for a parenthesis enclosed alias list.
   */
  override def visitIdentifierList(ctx: ArcticExtendSparkSqlParser.IdentifierListContext): Seq[String] = withOrigin(ctx) {
    visitIdentifierSeq(ctx.identifierSeq)
  }

  /**
   * Create a Sequence of Strings for an identifier list.
   */
  override def visitIdentifierSeq(ctx: ArcticExtendSparkSqlParser.IdentifierSeqContext): Seq[String] = withOrigin(ctx) {
    ctx.ident.asScala.map(_.getText).toSeq
  }

  /**
   * Parse a list of transforms or columns.
   */
  override def visitPartitionFieldList(
                                        ctx: ArcticExtendSparkSqlParser.PartitionFieldListContext):
  (Seq[Transform], Seq[StructField]) = withOrigin(ctx) {
    val (transforms, columns) = ctx.fields.asScala.map {
      case transform: ArcticExtendSparkSqlParser.PartitionTransformContext =>
        (Some(visitPartitionTransform(transform)), None)
      case field: ArcticExtendSparkSqlParser.PartitionColumnContext =>
        (None, Some(visitColType(field.colType)))
    }.unzip

    (transforms.flatten.toSeq, columns.flatten.toSeq)
  }


  override def visitPartitionTransform(
                                        ctx: ArcticExtendSparkSqlParser.PartitionTransformContext): Transform = withOrigin(ctx) {
    def getFieldReference(
                           ctx: ArcticExtendSparkSqlParser.ApplyTransformContext,
                           arg: V2Expression): FieldReference = {
      lazy val name: String = ctx.identifier.getText
      arg match {
        case ref: FieldReference =>
          ref
        case nonRef =>
          throw new ParseException(
            s"Expected a column reference for transform $name: ${nonRef.describe}", ctx)
      }
    }

    def getSingleFieldReference(
                                 ctx: ArcticExtendSparkSqlParser.ApplyTransformContext,
                                 arguments: Seq[V2Expression]): FieldReference = {
      lazy val name: String = ctx.identifier.getText
      if (arguments.size > 1) {
        throw new ParseException(s"Too many arguments for transform $name", ctx)
      } else if (arguments.isEmpty) {
        throw new ParseException(s"Not enough arguments for transform $name", ctx)
      } else {
        getFieldReference(ctx, arguments.head)
      }
    }

    ctx.transform match {
      case identityCtx: ArcticExtendSparkSqlParser.IdentityTransformContext =>
        IdentityTransform(FieldReference(typedVisit[Seq[String]](identityCtx.qualifiedName)))

      case applyCtx: ArcticExtendSparkSqlParser.ApplyTransformContext =>
        val arguments = applyCtx.argument.asScala.map(visitTransformArgument).toSeq

        applyCtx.identifier.getText match {
          case "bucket" =>
            val numBuckets: Int = arguments.head match {
              case LiteralValue(shortValue, ShortType) =>
                shortValue.asInstanceOf[Short].toInt
              case LiteralValue(intValue, IntegerType) =>
                intValue.asInstanceOf[Int]
              case LiteralValue(longValue, LongType) =>
                longValue.asInstanceOf[Long].toInt
              case lit =>
                throw new ParseException(s"Invalid number of buckets: ${lit.describe}", applyCtx)
            }

            val fields = arguments.tail.map(arg => getFieldReference(applyCtx, arg))

            BucketTransform(LiteralValue(numBuckets, IntegerType), fields.toSeq)

          case "years" =>
            YearsTransform(getSingleFieldReference(applyCtx, arguments))

          case "months" =>
            MonthsTransform(getSingleFieldReference(applyCtx, arguments))

          case "days" =>
            DaysTransform(getSingleFieldReference(applyCtx, arguments))

          case "hours" =>
            HoursTransform(getSingleFieldReference(applyCtx, arguments))

          case name =>
            ApplyTransform(name, arguments)
        }
    }
  }

  /**
   * Parse an argument to a transform. An argument may be a field reference (qualified name) or
   * a value literal.
   */
  override def visitTransformArgument(ctx: ArcticExtendSparkSqlParser.TransformArgumentContext): V2Expression = {
    withOrigin(ctx) {
      val reference = Option(ctx.qualifiedName)
        .map(typedVisit[Seq[String]])
        .map(FieldReference(_))
      val literal = Option(ctx.constant)
        .map(typedVisit[Literal])
        .map(lit => LiteralValue(lit.value, lit.dataType))
      reference.orElse(literal)
        .getOrElse(throw new ParseException(s"Invalid transform argument", ctx))
    }
  }

  /**
   * Return a multi-part identifier as Seq[String].
   */
  override def visitQualifiedName(ctx: ArcticExtendSparkSqlParser.QualifiedNameContext): Seq[String] = withOrigin(ctx) {
    ctx.identifier.asScala.map(_.getText).toSeq
  }


  private def partitionExpressions(
                                    partTransforms: Seq[Transform],
                                    partCols: Seq[StructField],
                                    ctx: ParserRuleContext): Seq[Transform] = {
    if (partTransforms.nonEmpty) {
      if (partCols.nonEmpty) {
        val references = partTransforms.map(_.describe()).mkString(", ")
        val columns = partCols
          .map(field => s"${field.name} ${field.dataType.simpleString}")
          .mkString(", ")
        operationNotAllowed(
          s"""PARTITION BY: Cannot mix partition expressions and partition columns:
             |Expressions: $references
             |Columns: $columns""".stripMargin, ctx)

      }
      partTransforms
    } else {
      // columns were added to create the schema. convert to column references
      partCols.map { column =>
        IdentityTransform(FieldReference(Seq(column.name)))
      }
    }
  }

  /**
   * Type to keep track of a table header: (identifier, isTemporary, ifNotExists, isExternal).
   */
  type TableHeader = (Seq[String], Boolean, Boolean, Boolean)

  /**
   * Validate a create table statement and return the [[TableIdentifier]].
   */
  override def visitCreateTableHeader(
                                       ctx: ArcticExtendSparkSqlParser.CreateTableHeaderContext): TableHeader = withOrigin(ctx) {
    val temporary = ctx.TEMPORARY != null
    val ifNotExists = ctx.EXISTS != null
    if (temporary && ifNotExists) {
      operationNotAllowed("CREATE TEMPORARY TABLE ... IF NOT EXISTS", ctx)
    }
    val multipartIdentifier = ctx.multipartIdentifier.parts.asScala.map(_.getText).toSeq
    (multipartIdentifier, temporary, ifNotExists, ctx.EXTERNAL != null)
  }

  /**
   * Converts a multi-part identifier to a TableIdentifier.
   *
   * If the multi-part identifier has too many parts, this will throw a ParseException.
   */
  def tableIdentifier(
                       multipart: Seq[String],
                       command: String,
                       ctx: ParserRuleContext): TableIdentifier = {
    multipart match {
      case Seq(tableName) =>
        TableIdentifier(tableName)
      case Seq(database, tableName) =>
        TableIdentifier(tableName, Some(database))
      case _ =>
        operationNotAllowed(s"$command does not support multi-part identifiers", ctx)
    }
  }



  /**
   * Return a multi-part identifier as Seq[String].
   */
  override def visitMultipartIdentifier(ctx: ArcticExtendSparkSqlParser.MultipartIdentifierContext): Seq[String] = withOrigin(ctx) {
    ctx.parts.asScala.map(_.getText)
  }

}

