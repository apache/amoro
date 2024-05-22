// Generated from org/apache/amoro/spark/sql/parser/MixedFormatSqlExtend.g4 by ANTLR 4.8
package org.apache.amoro.spark.sql.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MixedFormatSqlExtendParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MixedFormatSqlExtendVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#extendStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtendStatement(MixedFormatSqlExtendParser.ExtendStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createTableWithPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableWithPk(MixedFormatSqlExtendParser.CreateTableWithPkContext ctx);
	/**
	 * Visit a parse tree produced by the {@code explain}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain(MixedFormatSqlExtendParser.ExplainContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#createTableHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableHeader(MixedFormatSqlExtendParser.CreateTableHeaderContext ctx);
	/**
	 * Visit a parse tree produced by the {@code colListWithPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#colListAndPk}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColListWithPk(MixedFormatSqlExtendParser.ColListWithPkContext ctx);
	/**
	 * Visit a parse tree produced by the {@code colListOnlyPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#colListAndPk}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColListOnlyPk(MixedFormatSqlExtendParser.ColListOnlyPkContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#primarySpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimarySpec(MixedFormatSqlExtendParser.PrimarySpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#bucketSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBucketSpec(MixedFormatSqlExtendParser.BucketSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#skewSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewSpec(MixedFormatSqlExtendParser.SkewSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#locationSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocationSpec(MixedFormatSqlExtendParser.LocationSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#commentSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentSpec(MixedFormatSqlExtendParser.CommentSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(MixedFormatSqlExtendParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#ctes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCtes(MixedFormatSqlExtendParser.CtesContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#namedQuery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedQuery(MixedFormatSqlExtendParser.NamedQueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#tableProvider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableProvider(MixedFormatSqlExtendParser.TableProviderContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#createTableClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableClauses(MixedFormatSqlExtendParser.CreateTableClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#propertyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyList(MixedFormatSqlExtendParser.PropertyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(MixedFormatSqlExtendParser.PropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#propertyKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyKey(MixedFormatSqlExtendParser.PropertyKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#propertyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyValue(MixedFormatSqlExtendParser.PropertyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#constantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantList(MixedFormatSqlExtendParser.ConstantListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#nestedConstantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedConstantList(MixedFormatSqlExtendParser.NestedConstantListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#createFileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateFileFormat(MixedFormatSqlExtendParser.CreateFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableFileFormat}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#fileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFileFormat(MixedFormatSqlExtendParser.TableFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericFileFormat}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#fileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericFileFormat(MixedFormatSqlExtendParser.GenericFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#storageHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageHandler(MixedFormatSqlExtendParser.StorageHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#queryOrganization}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryOrganization(MixedFormatSqlExtendParser.QueryOrganizationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code queryTermDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryTermDefault(MixedFormatSqlExtendParser.QueryTermDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setOperation}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetOperation(MixedFormatSqlExtendParser.SetOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code queryPrimaryDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryPrimaryDefault(MixedFormatSqlExtendParser.QueryPrimaryDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fromStmt}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStmt(MixedFormatSqlExtendParser.FromStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code table}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable(MixedFormatSqlExtendParser.TableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inlineTableDefault1}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineTableDefault1(MixedFormatSqlExtendParser.InlineTableDefault1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code subquery}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(MixedFormatSqlExtendParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#sortItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortItem(MixedFormatSqlExtendParser.SortItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#fromStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStatement(MixedFormatSqlExtendParser.FromStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#fromStatementBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStatementBody(MixedFormatSqlExtendParser.FromStatementBodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code transformQuerySpecification}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformQuerySpecification(MixedFormatSqlExtendParser.TransformQuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code regularQuerySpecification}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularQuerySpecification(MixedFormatSqlExtendParser.RegularQuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#transformClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformClause(MixedFormatSqlExtendParser.TransformClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(MixedFormatSqlExtendParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(MixedFormatSqlExtendParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(MixedFormatSqlExtendParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHint(MixedFormatSqlExtendParser.HintContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#hintStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintStatement(MixedFormatSqlExtendParser.HintStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(MixedFormatSqlExtendParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#temporalClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemporalClause(MixedFormatSqlExtendParser.TemporalClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#aggregationClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationClause(MixedFormatSqlExtendParser.AggregationClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(MixedFormatSqlExtendParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#groupingAnalytics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingAnalytics(MixedFormatSqlExtendParser.GroupingAnalyticsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#groupingElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingElement(MixedFormatSqlExtendParser.GroupingElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#groupingSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSet(MixedFormatSqlExtendParser.GroupingSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#pivotClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivotClause(MixedFormatSqlExtendParser.PivotClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#pivotColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivotColumn(MixedFormatSqlExtendParser.PivotColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#pivotValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivotValue(MixedFormatSqlExtendParser.PivotValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#lateralView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLateralView(MixedFormatSqlExtendParser.LateralViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#setQuantifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetQuantifier(MixedFormatSqlExtendParser.SetQuantifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(MixedFormatSqlExtendParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#joinRelation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinRelation(MixedFormatSqlExtendParser.JoinRelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(MixedFormatSqlExtendParser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#joinCriteria}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinCriteria(MixedFormatSqlExtendParser.JoinCriteriaContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#sample}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSample(MixedFormatSqlExtendParser.SampleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByPercentile}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByPercentile(MixedFormatSqlExtendParser.SampleByPercentileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByRows}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByRows(MixedFormatSqlExtendParser.SampleByRowsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByBucket}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByBucket(MixedFormatSqlExtendParser.SampleByBucketContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByBytes}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByBytes(MixedFormatSqlExtendParser.SampleByBytesContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(MixedFormatSqlExtendParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#identifierSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierSeq(MixedFormatSqlExtendParser.IdentifierSeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#orderedIdentifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedIdentifierList(MixedFormatSqlExtendParser.OrderedIdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#orderedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedIdentifier(MixedFormatSqlExtendParser.OrderedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableName}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(MixedFormatSqlExtendParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code aliasedQuery}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasedQuery(MixedFormatSqlExtendParser.AliasedQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code aliasedRelation}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasedRelation(MixedFormatSqlExtendParser.AliasedRelationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inlineTableDefault2}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineTableDefault2(MixedFormatSqlExtendParser.InlineTableDefault2Context ctx);
	/**
	 * Visit a parse tree produced by the {@code tableValuedFunction}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableValuedFunction(MixedFormatSqlExtendParser.TableValuedFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#inlineTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineTable(MixedFormatSqlExtendParser.InlineTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#functionTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTable(MixedFormatSqlExtendParser.FunctionTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#tableAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAlias(MixedFormatSqlExtendParser.TableAliasContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rowFormatSerde}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#rowFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormatSerde(MixedFormatSqlExtendParser.RowFormatSerdeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rowFormatDelimited}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#rowFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormatDelimited(MixedFormatSqlExtendParser.RowFormatDelimitedContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifier(MixedFormatSqlExtendParser.MultipartIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#namedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedExpression(MixedFormatSqlExtendParser.NamedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#namedExpressionSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedExpressionSeq(MixedFormatSqlExtendParser.NamedExpressionSeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#partitionFieldList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionFieldList(MixedFormatSqlExtendParser.PartitionFieldListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code partitionTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#partitionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionTransform(MixedFormatSqlExtendParser.PartitionTransformContext ctx);
	/**
	 * Visit a parse tree produced by the {@code partitionColumn}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#partitionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionColumn(MixedFormatSqlExtendParser.PartitionColumnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identityTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#transform}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentityTransform(MixedFormatSqlExtendParser.IdentityTransformContext ctx);
	/**
	 * Visit a parse tree produced by the {@code applyTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#transform}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplyTransform(MixedFormatSqlExtendParser.ApplyTransformContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#transformArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformArgument(MixedFormatSqlExtendParser.TransformArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(MixedFormatSqlExtendParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#expressionSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionSeq(MixedFormatSqlExtendParser.ExpressionSeqContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalNot(MixedFormatSqlExtendParser.LogicalNotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code predicated}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicated(MixedFormatSqlExtendParser.PredicatedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exists}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExists(MixedFormatSqlExtendParser.ExistsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalBinary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalBinary(MixedFormatSqlExtendParser.LogicalBinaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(MixedFormatSqlExtendParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code valueExpressionDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueExpressionDefault(MixedFormatSqlExtendParser.ValueExpressionDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparison}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(MixedFormatSqlExtendParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arithmeticBinary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticBinary(MixedFormatSqlExtendParser.ArithmeticBinaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arithmeticUnary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticUnary(MixedFormatSqlExtendParser.ArithmeticUnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#datetimeUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetimeUnit(MixedFormatSqlExtendParser.DatetimeUnitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code struct}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStruct(MixedFormatSqlExtendParser.StructContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dereference}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDereference(MixedFormatSqlExtendParser.DereferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timestampadd}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampadd(MixedFormatSqlExtendParser.TimestampaddContext ctx);
	/**
	 * Visit a parse tree produced by the {@code substring}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstring(MixedFormatSqlExtendParser.SubstringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code cast}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast(MixedFormatSqlExtendParser.CastContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lambda}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(MixedFormatSqlExtendParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedExpression(MixedFormatSqlExtendParser.ParenthesizedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trim}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrim(MixedFormatSqlExtendParser.TrimContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleCase}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleCase(MixedFormatSqlExtendParser.SimpleCaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code currentLike}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCurrentLike(MixedFormatSqlExtendParser.CurrentLikeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code columnReference}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnReference(MixedFormatSqlExtendParser.ColumnReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rowConstructor}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowConstructor(MixedFormatSqlExtendParser.RowConstructorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code last}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLast(MixedFormatSqlExtendParser.LastContext ctx);
	/**
	 * Visit a parse tree produced by the {@code star}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStar(MixedFormatSqlExtendParser.StarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code overlay}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlay(MixedFormatSqlExtendParser.OverlayContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubscript(MixedFormatSqlExtendParser.SubscriptContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timestampdiff}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampdiff(MixedFormatSqlExtendParser.TimestampdiffContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryExpression(MixedFormatSqlExtendParser.SubqueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code constantDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDefault(MixedFormatSqlExtendParser.ConstantDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code extract}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtract(MixedFormatSqlExtendParser.ExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code percentile}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPercentile(MixedFormatSqlExtendParser.PercentileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(MixedFormatSqlExtendParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code searchedCase}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchedCase(MixedFormatSqlExtendParser.SearchedCaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code position}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition(MixedFormatSqlExtendParser.PositionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code first}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst(MixedFormatSqlExtendParser.FirstContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullLiteral(MixedFormatSqlExtendParser.NullLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intervalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalLiteral(MixedFormatSqlExtendParser.IntervalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeConstructor}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeConstructor(MixedFormatSqlExtendParser.TypeConstructorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(MixedFormatSqlExtendParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(MixedFormatSqlExtendParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(MixedFormatSqlExtendParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(MixedFormatSqlExtendParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#booleanValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanValue(MixedFormatSqlExtendParser.BooleanValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(MixedFormatSqlExtendParser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingMultiUnitsInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingMultiUnitsInterval(MixedFormatSqlExtendParser.ErrorCapturingMultiUnitsIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#multiUnitsInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiUnitsInterval(MixedFormatSqlExtendParser.MultiUnitsIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingUnitToUnitInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingUnitToUnitInterval(MixedFormatSqlExtendParser.ErrorCapturingUnitToUnitIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#unitToUnitInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnitToUnitInterval(MixedFormatSqlExtendParser.UnitToUnitIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#intervalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalValue(MixedFormatSqlExtendParser.IntervalValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexDataType(MixedFormatSqlExtendParser.ComplexDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearMonthIntervalDataType(MixedFormatSqlExtendParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayTimeIntervalDataType(MixedFormatSqlExtendParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveDataType(MixedFormatSqlExtendParser.PrimitiveDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#colTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColTypeList(MixedFormatSqlExtendParser.ColTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#colType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColType(MixedFormatSqlExtendParser.ColTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#complexColTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexColTypeList(MixedFormatSqlExtendParser.ComplexColTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#complexColType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexColType(MixedFormatSqlExtendParser.ComplexColTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(MixedFormatSqlExtendParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#windowClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause(MixedFormatSqlExtendParser.WindowClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#namedWindow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedWindow(MixedFormatSqlExtendParser.NamedWindowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code windowRef}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#windowSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowRef(MixedFormatSqlExtendParser.WindowRefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code windowDef}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#windowSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowDef(MixedFormatSqlExtendParser.WindowDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#windowFrame}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFrame(MixedFormatSqlExtendParser.WindowFrameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#frameBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBound(MixedFormatSqlExtendParser.FrameBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(MixedFormatSqlExtendParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(MixedFormatSqlExtendParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingIdentifier(MixedFormatSqlExtendParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorIdent(MixedFormatSqlExtendParser.ErrorIdentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealIdent(MixedFormatSqlExtendParser.RealIdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(MixedFormatSqlExtendParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#strictIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnquotedIdentifier(MixedFormatSqlExtendParser.UnquotedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#strictIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuotedIdentifierAlternative(MixedFormatSqlExtendParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuotedIdentifier(MixedFormatSqlExtendParser.QuotedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentLiteral(MixedFormatSqlExtendParser.ExponentLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(MixedFormatSqlExtendParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code legacyDecimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLegacyDecimalLiteral(MixedFormatSqlExtendParser.LegacyDecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(MixedFormatSqlExtendParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bigIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigIntLiteral(MixedFormatSqlExtendParser.BigIntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code smallIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSmallIntLiteral(MixedFormatSqlExtendParser.SmallIntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tinyIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTinyIntLiteral(MixedFormatSqlExtendParser.TinyIntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code doubleLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleLiteral(MixedFormatSqlExtendParser.DoubleLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(MixedFormatSqlExtendParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bigDecimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigDecimalLiteral(MixedFormatSqlExtendParser.BigDecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnsiNonReserved(MixedFormatSqlExtendParser.AnsiNonReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#strictNonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrictNonReserved(MixedFormatSqlExtendParser.StrictNonReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link MixedFormatSqlExtendParser#nonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReserved(MixedFormatSqlExtendParser.NonReservedContext ctx);
}