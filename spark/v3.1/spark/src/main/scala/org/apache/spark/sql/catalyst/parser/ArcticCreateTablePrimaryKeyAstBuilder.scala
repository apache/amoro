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
import com.netease.arctic.spark.sql.catalyst.plans
import com.netease.arctic.spark.sql.catalyst.plans.CreateArcticTableStatement
import com.netease.arctic.spark.sql.parser.{ArcticExtendSparkSqlBaseVisitor, ArcticExtendSparkSqlParser}
import org.antlr.v4.runtime.tree.{ParseTree, RuleNode}
import org.antlr.v4.runtime.{ParserRuleContext, Token}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.catalog.BucketSpec
import org.apache.spark.sql.catalyst.expressions.Literal
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser.astBuilder.{cleanTableOptions, cleanTableProperties}
import org.apache.spark.sql.catalyst.parser.ParserUtils.{checkDuplicateClauses, checkDuplicateKeys, operationNotAllowed, string, validate}
import org.apache.spark.sql.catalyst.parser.SqlBaseParser._
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.{SQLConfHelper, TableIdentifier}
import org.apache.spark.sql.connector.expressions.{ApplyTransform, BucketTransform, DaysTransform, FieldReference, HoursTransform, IdentityTransform, LiteralValue, MonthsTransform, Transform, YearsTransform, Expression => V2Expression}
import org.apache.spark.sql.types._

import java.util
import java.util.Locale
import scala.collection.JavaConverters._

class ArcticCreateTablePrimaryKeyAstBuilder(delegate: ParserInterface)
  extends ArcticExtendSparkSqlBaseVisitor[AnyRef] with SQLConfHelper with Logging{
  /* ********************************************************************************************
   * Plan parsing
   * ******************************************************************************************** */
  protected def plan(tree: ParserRuleContext): LogicalPlan = typedVisit(tree)

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

  override def visitCreateTable(ctx: ArcticExtendSparkSqlParser.CreateTableContext): CreateArcticTableStatement = withOrigin(ctx) {
    val (table, temp, ifNotExists, external) = visitCreateTableHeader(ctx.createTableHeader)

    val columns = Option(ctx.colTypeList()).map(visitColTypeList).getOrElse(Nil)
    val primary = visitPrimarySpec(ctx.colTypeList().primarySpec())
    // Setting the primary key not nullable
    val newColumns = setPrimaryKeyNotNull(columns, primary)
    val provider = Option(ctx.tableProvider).map(_.multipartIdentifier.getText)
    val (partTransforms, partCols, bucketSpec, properties, options, location, comment, serdeInfo) =
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
      case Some(_) if newColumns.nonEmpty =>
        operationNotAllowed(
          "Schema may not be specified in a Create Table As Select (CTAS) statement",
          ctx)

      case Some(_) if partCols.nonEmpty =>
        // non-reference partition columns are not allowed because schema can't be specified
        operationNotAllowed(
          "Partition column types may not be specified in Create Table As Select (CTAS)",
          ctx)

//      case Some(query) =>
//        CreateTableAsSelectStatement(
//          table, query, partitioning, bucketSpec, properties, provider, options, location, comment,
//          writeOptions = Map.empty, serdeInfo, external = external, ifNotExists = ifNotExists)

      case _ =>
        // Note: table schema includes both the table columns list and the partition columns
        // with data type.
        val schema = StructType(newColumns ++ partCols)
        plans.CreateArcticTableStatement(table, schema, partitioning, bucketSpec, properties, provider,
          options, location, comment, serdeInfo, primary, external = external, ifNotExists = ifNotExists)
    }
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
      Map[String, String], Option[String], Option[String], Option[SerdeInfo])


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
    val serdeInfo =
      getSerdeInfo(ctx.rowFormat.asScala.toSeq, ctx.createFileFormat.asScala.toSeq, ctx)
    (partTransforms, partCols, bucketSpec, cleanedProperties, cleanedOptions, newLocation, comment,
      serdeInfo)
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

