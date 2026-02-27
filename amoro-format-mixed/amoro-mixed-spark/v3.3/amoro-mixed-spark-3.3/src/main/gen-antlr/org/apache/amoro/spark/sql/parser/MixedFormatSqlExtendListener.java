// Generated from org/apache/amoro/spark/sql/parser/MixedFormatSqlExtend.g4 by ANTLR 4.8
package org.apache.amoro.spark.sql.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MixedFormatSqlExtendParser}.
 */
public interface MixedFormatSqlExtendListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#extendStatement}.
	 * @param ctx the parse tree
	 */
	void enterExtendStatement(MixedFormatSqlExtendParser.ExtendStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#extendStatement}.
	 * @param ctx the parse tree
	 */
	void exitExtendStatement(MixedFormatSqlExtendParser.ExtendStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createTableWithPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableWithPk(MixedFormatSqlExtendParser.CreateTableWithPkContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createTableWithPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableWithPk(MixedFormatSqlExtendParser.CreateTableWithPkContext ctx);
	/**
	 * Enter a parse tree produced by the {@code explain}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterExplain(MixedFormatSqlExtendParser.ExplainContext ctx);
	/**
	 * Exit a parse tree produced by the {@code explain}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitExplain(MixedFormatSqlExtendParser.ExplainContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#createTableHeader}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableHeader(MixedFormatSqlExtendParser.CreateTableHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#createTableHeader}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableHeader(MixedFormatSqlExtendParser.CreateTableHeaderContext ctx);
	/**
	 * Enter a parse tree produced by the {@code colListWithPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#colListAndPk}.
	 * @param ctx the parse tree
	 */
	void enterColListWithPk(MixedFormatSqlExtendParser.ColListWithPkContext ctx);
	/**
	 * Exit a parse tree produced by the {@code colListWithPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#colListAndPk}.
	 * @param ctx the parse tree
	 */
	void exitColListWithPk(MixedFormatSqlExtendParser.ColListWithPkContext ctx);
	/**
	 * Enter a parse tree produced by the {@code colListOnlyPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#colListAndPk}.
	 * @param ctx the parse tree
	 */
	void enterColListOnlyPk(MixedFormatSqlExtendParser.ColListOnlyPkContext ctx);
	/**
	 * Exit a parse tree produced by the {@code colListOnlyPk}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#colListAndPk}.
	 * @param ctx the parse tree
	 */
	void exitColListOnlyPk(MixedFormatSqlExtendParser.ColListOnlyPkContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#primarySpec}.
	 * @param ctx the parse tree
	 */
	void enterPrimarySpec(MixedFormatSqlExtendParser.PrimarySpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#primarySpec}.
	 * @param ctx the parse tree
	 */
	void exitPrimarySpec(MixedFormatSqlExtendParser.PrimarySpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#bucketSpec}.
	 * @param ctx the parse tree
	 */
	void enterBucketSpec(MixedFormatSqlExtendParser.BucketSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#bucketSpec}.
	 * @param ctx the parse tree
	 */
	void exitBucketSpec(MixedFormatSqlExtendParser.BucketSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#skewSpec}.
	 * @param ctx the parse tree
	 */
	void enterSkewSpec(MixedFormatSqlExtendParser.SkewSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#skewSpec}.
	 * @param ctx the parse tree
	 */
	void exitSkewSpec(MixedFormatSqlExtendParser.SkewSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#locationSpec}.
	 * @param ctx the parse tree
	 */
	void enterLocationSpec(MixedFormatSqlExtendParser.LocationSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#locationSpec}.
	 * @param ctx the parse tree
	 */
	void exitLocationSpec(MixedFormatSqlExtendParser.LocationSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#commentSpec}.
	 * @param ctx the parse tree
	 */
	void enterCommentSpec(MixedFormatSqlExtendParser.CommentSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#commentSpec}.
	 * @param ctx the parse tree
	 */
	void exitCommentSpec(MixedFormatSqlExtendParser.CommentSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(MixedFormatSqlExtendParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(MixedFormatSqlExtendParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#ctes}.
	 * @param ctx the parse tree
	 */
	void enterCtes(MixedFormatSqlExtendParser.CtesContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#ctes}.
	 * @param ctx the parse tree
	 */
	void exitCtes(MixedFormatSqlExtendParser.CtesContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#namedQuery}.
	 * @param ctx the parse tree
	 */
	void enterNamedQuery(MixedFormatSqlExtendParser.NamedQueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#namedQuery}.
	 * @param ctx the parse tree
	 */
	void exitNamedQuery(MixedFormatSqlExtendParser.NamedQueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#tableProvider}.
	 * @param ctx the parse tree
	 */
	void enterTableProvider(MixedFormatSqlExtendParser.TableProviderContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#tableProvider}.
	 * @param ctx the parse tree
	 */
	void exitTableProvider(MixedFormatSqlExtendParser.TableProviderContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#createTableClauses}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableClauses(MixedFormatSqlExtendParser.CreateTableClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#createTableClauses}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableClauses(MixedFormatSqlExtendParser.CreateTableClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#propertyList}.
	 * @param ctx the parse tree
	 */
	void enterPropertyList(MixedFormatSqlExtendParser.PropertyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#propertyList}.
	 * @param ctx the parse tree
	 */
	void exitPropertyList(MixedFormatSqlExtendParser.PropertyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(MixedFormatSqlExtendParser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(MixedFormatSqlExtendParser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#propertyKey}.
	 * @param ctx the parse tree
	 */
	void enterPropertyKey(MixedFormatSqlExtendParser.PropertyKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#propertyKey}.
	 * @param ctx the parse tree
	 */
	void exitPropertyKey(MixedFormatSqlExtendParser.PropertyKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void enterPropertyValue(MixedFormatSqlExtendParser.PropertyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void exitPropertyValue(MixedFormatSqlExtendParser.PropertyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#constantList}.
	 * @param ctx the parse tree
	 */
	void enterConstantList(MixedFormatSqlExtendParser.ConstantListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#constantList}.
	 * @param ctx the parse tree
	 */
	void exitConstantList(MixedFormatSqlExtendParser.ConstantListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#nestedConstantList}.
	 * @param ctx the parse tree
	 */
	void enterNestedConstantList(MixedFormatSqlExtendParser.NestedConstantListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#nestedConstantList}.
	 * @param ctx the parse tree
	 */
	void exitNestedConstantList(MixedFormatSqlExtendParser.NestedConstantListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#createFileFormat}.
	 * @param ctx the parse tree
	 */
	void enterCreateFileFormat(MixedFormatSqlExtendParser.CreateFileFormatContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#createFileFormat}.
	 * @param ctx the parse tree
	 */
	void exitCreateFileFormat(MixedFormatSqlExtendParser.CreateFileFormatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableFileFormat}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void enterTableFileFormat(MixedFormatSqlExtendParser.TableFileFormatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableFileFormat}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void exitTableFileFormat(MixedFormatSqlExtendParser.TableFileFormatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericFileFormat}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void enterGenericFileFormat(MixedFormatSqlExtendParser.GenericFileFormatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericFileFormat}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void exitGenericFileFormat(MixedFormatSqlExtendParser.GenericFileFormatContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#storageHandler}.
	 * @param ctx the parse tree
	 */
	void enterStorageHandler(MixedFormatSqlExtendParser.StorageHandlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#storageHandler}.
	 * @param ctx the parse tree
	 */
	void exitStorageHandler(MixedFormatSqlExtendParser.StorageHandlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#queryOrganization}.
	 * @param ctx the parse tree
	 */
	void enterQueryOrganization(MixedFormatSqlExtendParser.QueryOrganizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#queryOrganization}.
	 * @param ctx the parse tree
	 */
	void exitQueryOrganization(MixedFormatSqlExtendParser.QueryOrganizationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryTermDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void enterQueryTermDefault(MixedFormatSqlExtendParser.QueryTermDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryTermDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void exitQueryTermDefault(MixedFormatSqlExtendParser.QueryTermDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setOperation}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void enterSetOperation(MixedFormatSqlExtendParser.SetOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setOperation}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void exitSetOperation(MixedFormatSqlExtendParser.SetOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryPrimaryDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterQueryPrimaryDefault(MixedFormatSqlExtendParser.QueryPrimaryDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryPrimaryDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitQueryPrimaryDefault(MixedFormatSqlExtendParser.QueryPrimaryDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fromStmt}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterFromStmt(MixedFormatSqlExtendParser.FromStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fromStmt}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitFromStmt(MixedFormatSqlExtendParser.FromStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code table}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterTable(MixedFormatSqlExtendParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code table}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitTable(MixedFormatSqlExtendParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inlineTableDefault1}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableDefault1(MixedFormatSqlExtendParser.InlineTableDefault1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code inlineTableDefault1}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableDefault1(MixedFormatSqlExtendParser.InlineTableDefault1Context ctx);
	/**
	 * Enter a parse tree produced by the {@code subquery}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(MixedFormatSqlExtendParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subquery}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(MixedFormatSqlExtendParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#sortItem}.
	 * @param ctx the parse tree
	 */
	void enterSortItem(MixedFormatSqlExtendParser.SortItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#sortItem}.
	 * @param ctx the parse tree
	 */
	void exitSortItem(MixedFormatSqlExtendParser.SortItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#fromStatement}.
	 * @param ctx the parse tree
	 */
	void enterFromStatement(MixedFormatSqlExtendParser.FromStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#fromStatement}.
	 * @param ctx the parse tree
	 */
	void exitFromStatement(MixedFormatSqlExtendParser.FromStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#fromStatementBody}.
	 * @param ctx the parse tree
	 */
	void enterFromStatementBody(MixedFormatSqlExtendParser.FromStatementBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#fromStatementBody}.
	 * @param ctx the parse tree
	 */
	void exitFromStatementBody(MixedFormatSqlExtendParser.FromStatementBodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code transformQuerySpecification}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterTransformQuerySpecification(MixedFormatSqlExtendParser.TransformQuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code transformQuerySpecification}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitTransformQuerySpecification(MixedFormatSqlExtendParser.TransformQuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regularQuerySpecification}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterRegularQuerySpecification(MixedFormatSqlExtendParser.RegularQuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regularQuerySpecification}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitRegularQuerySpecification(MixedFormatSqlExtendParser.RegularQuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#transformClause}.
	 * @param ctx the parse tree
	 */
	void enterTransformClause(MixedFormatSqlExtendParser.TransformClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#transformClause}.
	 * @param ctx the parse tree
	 */
	void exitTransformClause(MixedFormatSqlExtendParser.TransformClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(MixedFormatSqlExtendParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(MixedFormatSqlExtendParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(MixedFormatSqlExtendParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(MixedFormatSqlExtendParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(MixedFormatSqlExtendParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(MixedFormatSqlExtendParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#hint}.
	 * @param ctx the parse tree
	 */
	void enterHint(MixedFormatSqlExtendParser.HintContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#hint}.
	 * @param ctx the parse tree
	 */
	void exitHint(MixedFormatSqlExtendParser.HintContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#hintStatement}.
	 * @param ctx the parse tree
	 */
	void enterHintStatement(MixedFormatSqlExtendParser.HintStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#hintStatement}.
	 * @param ctx the parse tree
	 */
	void exitHintStatement(MixedFormatSqlExtendParser.HintStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(MixedFormatSqlExtendParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(MixedFormatSqlExtendParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#temporalClause}.
	 * @param ctx the parse tree
	 */
	void enterTemporalClause(MixedFormatSqlExtendParser.TemporalClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#temporalClause}.
	 * @param ctx the parse tree
	 */
	void exitTemporalClause(MixedFormatSqlExtendParser.TemporalClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#aggregationClause}.
	 * @param ctx the parse tree
	 */
	void enterAggregationClause(MixedFormatSqlExtendParser.AggregationClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#aggregationClause}.
	 * @param ctx the parse tree
	 */
	void exitAggregationClause(MixedFormatSqlExtendParser.AggregationClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(MixedFormatSqlExtendParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(MixedFormatSqlExtendParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#groupingAnalytics}.
	 * @param ctx the parse tree
	 */
	void enterGroupingAnalytics(MixedFormatSqlExtendParser.GroupingAnalyticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#groupingAnalytics}.
	 * @param ctx the parse tree
	 */
	void exitGroupingAnalytics(MixedFormatSqlExtendParser.GroupingAnalyticsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#groupingElement}.
	 * @param ctx the parse tree
	 */
	void enterGroupingElement(MixedFormatSqlExtendParser.GroupingElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#groupingElement}.
	 * @param ctx the parse tree
	 */
	void exitGroupingElement(MixedFormatSqlExtendParser.GroupingElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#groupingSet}.
	 * @param ctx the parse tree
	 */
	void enterGroupingSet(MixedFormatSqlExtendParser.GroupingSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#groupingSet}.
	 * @param ctx the parse tree
	 */
	void exitGroupingSet(MixedFormatSqlExtendParser.GroupingSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#pivotClause}.
	 * @param ctx the parse tree
	 */
	void enterPivotClause(MixedFormatSqlExtendParser.PivotClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#pivotClause}.
	 * @param ctx the parse tree
	 */
	void exitPivotClause(MixedFormatSqlExtendParser.PivotClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#pivotColumn}.
	 * @param ctx the parse tree
	 */
	void enterPivotColumn(MixedFormatSqlExtendParser.PivotColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#pivotColumn}.
	 * @param ctx the parse tree
	 */
	void exitPivotColumn(MixedFormatSqlExtendParser.PivotColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#pivotValue}.
	 * @param ctx the parse tree
	 */
	void enterPivotValue(MixedFormatSqlExtendParser.PivotValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#pivotValue}.
	 * @param ctx the parse tree
	 */
	void exitPivotValue(MixedFormatSqlExtendParser.PivotValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#lateralView}.
	 * @param ctx the parse tree
	 */
	void enterLateralView(MixedFormatSqlExtendParser.LateralViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#lateralView}.
	 * @param ctx the parse tree
	 */
	void exitLateralView(MixedFormatSqlExtendParser.LateralViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#setQuantifier}.
	 * @param ctx the parse tree
	 */
	void enterSetQuantifier(MixedFormatSqlExtendParser.SetQuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#setQuantifier}.
	 * @param ctx the parse tree
	 */
	void exitSetQuantifier(MixedFormatSqlExtendParser.SetQuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(MixedFormatSqlExtendParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(MixedFormatSqlExtendParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#joinRelation}.
	 * @param ctx the parse tree
	 */
	void enterJoinRelation(MixedFormatSqlExtendParser.JoinRelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#joinRelation}.
	 * @param ctx the parse tree
	 */
	void exitJoinRelation(MixedFormatSqlExtendParser.JoinRelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterJoinType(MixedFormatSqlExtendParser.JoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitJoinType(MixedFormatSqlExtendParser.JoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#joinCriteria}.
	 * @param ctx the parse tree
	 */
	void enterJoinCriteria(MixedFormatSqlExtendParser.JoinCriteriaContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#joinCriteria}.
	 * @param ctx the parse tree
	 */
	void exitJoinCriteria(MixedFormatSqlExtendParser.JoinCriteriaContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#sample}.
	 * @param ctx the parse tree
	 */
	void enterSample(MixedFormatSqlExtendParser.SampleContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#sample}.
	 * @param ctx the parse tree
	 */
	void exitSample(MixedFormatSqlExtendParser.SampleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByPercentile}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByPercentile(MixedFormatSqlExtendParser.SampleByPercentileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByPercentile}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByPercentile(MixedFormatSqlExtendParser.SampleByPercentileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByRows}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByRows(MixedFormatSqlExtendParser.SampleByRowsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByRows}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByRows(MixedFormatSqlExtendParser.SampleByRowsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByBucket}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByBucket(MixedFormatSqlExtendParser.SampleByBucketContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByBucket}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByBucket(MixedFormatSqlExtendParser.SampleByBucketContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByBytes}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByBytes(MixedFormatSqlExtendParser.SampleByBytesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByBytes}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByBytes(MixedFormatSqlExtendParser.SampleByBytesContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(MixedFormatSqlExtendParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(MixedFormatSqlExtendParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#identifierSeq}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierSeq(MixedFormatSqlExtendParser.IdentifierSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#identifierSeq}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierSeq(MixedFormatSqlExtendParser.IdentifierSeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#orderedIdentifierList}.
	 * @param ctx the parse tree
	 */
	void enterOrderedIdentifierList(MixedFormatSqlExtendParser.OrderedIdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#orderedIdentifierList}.
	 * @param ctx the parse tree
	 */
	void exitOrderedIdentifierList(MixedFormatSqlExtendParser.OrderedIdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#orderedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterOrderedIdentifier(MixedFormatSqlExtendParser.OrderedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#orderedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitOrderedIdentifier(MixedFormatSqlExtendParser.OrderedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableName}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterTableName(MixedFormatSqlExtendParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableName}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitTableName(MixedFormatSqlExtendParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code aliasedQuery}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterAliasedQuery(MixedFormatSqlExtendParser.AliasedQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code aliasedQuery}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitAliasedQuery(MixedFormatSqlExtendParser.AliasedQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code aliasedRelation}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterAliasedRelation(MixedFormatSqlExtendParser.AliasedRelationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code aliasedRelation}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitAliasedRelation(MixedFormatSqlExtendParser.AliasedRelationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inlineTableDefault2}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableDefault2(MixedFormatSqlExtendParser.InlineTableDefault2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code inlineTableDefault2}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableDefault2(MixedFormatSqlExtendParser.InlineTableDefault2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code tableValuedFunction}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterTableValuedFunction(MixedFormatSqlExtendParser.TableValuedFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableValuedFunction}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitTableValuedFunction(MixedFormatSqlExtendParser.TableValuedFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#inlineTable}.
	 * @param ctx the parse tree
	 */
	void enterInlineTable(MixedFormatSqlExtendParser.InlineTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#inlineTable}.
	 * @param ctx the parse tree
	 */
	void exitInlineTable(MixedFormatSqlExtendParser.InlineTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTable(MixedFormatSqlExtendParser.FunctionTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTable(MixedFormatSqlExtendParser.FunctionTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#tableAlias}.
	 * @param ctx the parse tree
	 */
	void enterTableAlias(MixedFormatSqlExtendParser.TableAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#tableAlias}.
	 * @param ctx the parse tree
	 */
	void exitTableAlias(MixedFormatSqlExtendParser.TableAliasContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rowFormatSerde}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void enterRowFormatSerde(MixedFormatSqlExtendParser.RowFormatSerdeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rowFormatSerde}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void exitRowFormatSerde(MixedFormatSqlExtendParser.RowFormatSerdeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rowFormatDelimited}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void enterRowFormatDelimited(MixedFormatSqlExtendParser.RowFormatDelimitedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rowFormatDelimited}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void exitRowFormatDelimited(MixedFormatSqlExtendParser.RowFormatDelimitedContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterMultipartIdentifier(MixedFormatSqlExtendParser.MultipartIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitMultipartIdentifier(MixedFormatSqlExtendParser.MultipartIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#namedExpression}.
	 * @param ctx the parse tree
	 */
	void enterNamedExpression(MixedFormatSqlExtendParser.NamedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#namedExpression}.
	 * @param ctx the parse tree
	 */
	void exitNamedExpression(MixedFormatSqlExtendParser.NamedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#namedExpressionSeq}.
	 * @param ctx the parse tree
	 */
	void enterNamedExpressionSeq(MixedFormatSqlExtendParser.NamedExpressionSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#namedExpressionSeq}.
	 * @param ctx the parse tree
	 */
	void exitNamedExpressionSeq(MixedFormatSqlExtendParser.NamedExpressionSeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#partitionFieldList}.
	 * @param ctx the parse tree
	 */
	void enterPartitionFieldList(MixedFormatSqlExtendParser.PartitionFieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#partitionFieldList}.
	 * @param ctx the parse tree
	 */
	void exitPartitionFieldList(MixedFormatSqlExtendParser.PartitionFieldListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void enterPartitionTransform(MixedFormatSqlExtendParser.PartitionTransformContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void exitPartitionTransform(MixedFormatSqlExtendParser.PartitionTransformContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionColumn}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void enterPartitionColumn(MixedFormatSqlExtendParser.PartitionColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionColumn}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void exitPartitionColumn(MixedFormatSqlExtendParser.PartitionColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identityTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#transform}.
	 * @param ctx the parse tree
	 */
	void enterIdentityTransform(MixedFormatSqlExtendParser.IdentityTransformContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identityTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#transform}.
	 * @param ctx the parse tree
	 */
	void exitIdentityTransform(MixedFormatSqlExtendParser.IdentityTransformContext ctx);
	/**
	 * Enter a parse tree produced by the {@code applyTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#transform}.
	 * @param ctx the parse tree
	 */
	void enterApplyTransform(MixedFormatSqlExtendParser.ApplyTransformContext ctx);
	/**
	 * Exit a parse tree produced by the {@code applyTransform}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#transform}.
	 * @param ctx the parse tree
	 */
	void exitApplyTransform(MixedFormatSqlExtendParser.ApplyTransformContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#transformArgument}.
	 * @param ctx the parse tree
	 */
	void enterTransformArgument(MixedFormatSqlExtendParser.TransformArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#transformArgument}.
	 * @param ctx the parse tree
	 */
	void exitTransformArgument(MixedFormatSqlExtendParser.TransformArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(MixedFormatSqlExtendParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(MixedFormatSqlExtendParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#expressionSeq}.
	 * @param ctx the parse tree
	 */
	void enterExpressionSeq(MixedFormatSqlExtendParser.ExpressionSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#expressionSeq}.
	 * @param ctx the parse tree
	 */
	void exitExpressionSeq(MixedFormatSqlExtendParser.ExpressionSeqContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalNot(MixedFormatSqlExtendParser.LogicalNotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalNot(MixedFormatSqlExtendParser.LogicalNotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code predicated}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterPredicated(MixedFormatSqlExtendParser.PredicatedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code predicated}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitPredicated(MixedFormatSqlExtendParser.PredicatedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exists}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterExists(MixedFormatSqlExtendParser.ExistsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exists}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitExists(MixedFormatSqlExtendParser.ExistsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalBinary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalBinary(MixedFormatSqlExtendParser.LogicalBinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalBinary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalBinary(MixedFormatSqlExtendParser.LogicalBinaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(MixedFormatSqlExtendParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(MixedFormatSqlExtendParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code valueExpressionDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterValueExpressionDefault(MixedFormatSqlExtendParser.ValueExpressionDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code valueExpressionDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitValueExpressionDefault(MixedFormatSqlExtendParser.ValueExpressionDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparison}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparison(MixedFormatSqlExtendParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparison}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparison(MixedFormatSqlExtendParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arithmeticBinary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticBinary(MixedFormatSqlExtendParser.ArithmeticBinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arithmeticBinary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticBinary(MixedFormatSqlExtendParser.ArithmeticBinaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arithmeticUnary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticUnary(MixedFormatSqlExtendParser.ArithmeticUnaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arithmeticUnary}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticUnary(MixedFormatSqlExtendParser.ArithmeticUnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#datetimeUnit}.
	 * @param ctx the parse tree
	 */
	void enterDatetimeUnit(MixedFormatSqlExtendParser.DatetimeUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#datetimeUnit}.
	 * @param ctx the parse tree
	 */
	void exitDatetimeUnit(MixedFormatSqlExtendParser.DatetimeUnitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code struct}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterStruct(MixedFormatSqlExtendParser.StructContext ctx);
	/**
	 * Exit a parse tree produced by the {@code struct}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitStruct(MixedFormatSqlExtendParser.StructContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dereference}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterDereference(MixedFormatSqlExtendParser.DereferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dereference}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitDereference(MixedFormatSqlExtendParser.DereferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timestampadd}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimestampadd(MixedFormatSqlExtendParser.TimestampaddContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timestampadd}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimestampadd(MixedFormatSqlExtendParser.TimestampaddContext ctx);
	/**
	 * Enter a parse tree produced by the {@code substring}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubstring(MixedFormatSqlExtendParser.SubstringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code substring}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubstring(MixedFormatSqlExtendParser.SubstringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code cast}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterCast(MixedFormatSqlExtendParser.CastContext ctx);
	/**
	 * Exit a parse tree produced by the {@code cast}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitCast(MixedFormatSqlExtendParser.CastContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lambda}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambda(MixedFormatSqlExtendParser.LambdaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lambda}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambda(MixedFormatSqlExtendParser.LambdaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedExpression(MixedFormatSqlExtendParser.ParenthesizedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedExpression(MixedFormatSqlExtendParser.ParenthesizedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trim}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTrim(MixedFormatSqlExtendParser.TrimContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trim}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTrim(MixedFormatSqlExtendParser.TrimContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleCase}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleCase(MixedFormatSqlExtendParser.SimpleCaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleCase}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleCase(MixedFormatSqlExtendParser.SimpleCaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code currentLike}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterCurrentLike(MixedFormatSqlExtendParser.CurrentLikeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code currentLike}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitCurrentLike(MixedFormatSqlExtendParser.CurrentLikeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code columnReference}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterColumnReference(MixedFormatSqlExtendParser.ColumnReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code columnReference}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitColumnReference(MixedFormatSqlExtendParser.ColumnReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rowConstructor}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterRowConstructor(MixedFormatSqlExtendParser.RowConstructorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rowConstructor}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitRowConstructor(MixedFormatSqlExtendParser.RowConstructorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code last}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterLast(MixedFormatSqlExtendParser.LastContext ctx);
	/**
	 * Exit a parse tree produced by the {@code last}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitLast(MixedFormatSqlExtendParser.LastContext ctx);
	/**
	 * Enter a parse tree produced by the {@code star}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterStar(MixedFormatSqlExtendParser.StarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code star}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitStar(MixedFormatSqlExtendParser.StarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code overlay}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterOverlay(MixedFormatSqlExtendParser.OverlayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code overlay}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitOverlay(MixedFormatSqlExtendParser.OverlayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubscript(MixedFormatSqlExtendParser.SubscriptContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubscript(MixedFormatSqlExtendParser.SubscriptContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timestampdiff}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimestampdiff(MixedFormatSqlExtendParser.TimestampdiffContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timestampdiff}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimestampdiff(MixedFormatSqlExtendParser.TimestampdiffContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryExpression(MixedFormatSqlExtendParser.SubqueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryExpression(MixedFormatSqlExtendParser.SubqueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constantDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantDefault(MixedFormatSqlExtendParser.ConstantDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constantDefault}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantDefault(MixedFormatSqlExtendParser.ConstantDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code extract}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterExtract(MixedFormatSqlExtendParser.ExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code extract}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitExtract(MixedFormatSqlExtendParser.ExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code percentile}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPercentile(MixedFormatSqlExtendParser.PercentileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code percentile}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPercentile(MixedFormatSqlExtendParser.PercentileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(MixedFormatSqlExtendParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(MixedFormatSqlExtendParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code searchedCase}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSearchedCase(MixedFormatSqlExtendParser.SearchedCaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code searchedCase}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSearchedCase(MixedFormatSqlExtendParser.SearchedCaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code position}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPosition(MixedFormatSqlExtendParser.PositionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code position}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPosition(MixedFormatSqlExtendParser.PositionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code first}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterFirst(MixedFormatSqlExtendParser.FirstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code first}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitFirst(MixedFormatSqlExtendParser.FirstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(MixedFormatSqlExtendParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(MixedFormatSqlExtendParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intervalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterIntervalLiteral(MixedFormatSqlExtendParser.IntervalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intervalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitIntervalLiteral(MixedFormatSqlExtendParser.IntervalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeConstructor}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterTypeConstructor(MixedFormatSqlExtendParser.TypeConstructorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeConstructor}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitTypeConstructor(MixedFormatSqlExtendParser.TypeConstructorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteral(MixedFormatSqlExtendParser.NumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteral(MixedFormatSqlExtendParser.NumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(MixedFormatSqlExtendParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(MixedFormatSqlExtendParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(MixedFormatSqlExtendParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(MixedFormatSqlExtendParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(MixedFormatSqlExtendParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(MixedFormatSqlExtendParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#booleanValue}.
	 * @param ctx the parse tree
	 */
	void enterBooleanValue(MixedFormatSqlExtendParser.BooleanValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#booleanValue}.
	 * @param ctx the parse tree
	 */
	void exitBooleanValue(MixedFormatSqlExtendParser.BooleanValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#interval}.
	 * @param ctx the parse tree
	 */
	void enterInterval(MixedFormatSqlExtendParser.IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#interval}.
	 * @param ctx the parse tree
	 */
	void exitInterval(MixedFormatSqlExtendParser.IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingMultiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingMultiUnitsInterval(MixedFormatSqlExtendParser.ErrorCapturingMultiUnitsIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingMultiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingMultiUnitsInterval(MixedFormatSqlExtendParser.ErrorCapturingMultiUnitsIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#multiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void enterMultiUnitsInterval(MixedFormatSqlExtendParser.MultiUnitsIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#multiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void exitMultiUnitsInterval(MixedFormatSqlExtendParser.MultiUnitsIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingUnitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingUnitToUnitInterval(MixedFormatSqlExtendParser.ErrorCapturingUnitToUnitIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingUnitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingUnitToUnitInterval(MixedFormatSqlExtendParser.ErrorCapturingUnitToUnitIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#unitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void enterUnitToUnitInterval(MixedFormatSqlExtendParser.UnitToUnitIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#unitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void exitUnitToUnitInterval(MixedFormatSqlExtendParser.UnitToUnitIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void enterIntervalValue(MixedFormatSqlExtendParser.IntervalValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void exitIntervalValue(MixedFormatSqlExtendParser.IntervalValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterComplexDataType(MixedFormatSqlExtendParser.ComplexDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitComplexDataType(MixedFormatSqlExtendParser.ComplexDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterYearMonthIntervalDataType(MixedFormatSqlExtendParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitYearMonthIntervalDataType(MixedFormatSqlExtendParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDayTimeIntervalDataType(MixedFormatSqlExtendParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDayTimeIntervalDataType(MixedFormatSqlExtendParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveDataType(MixedFormatSqlExtendParser.PrimitiveDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveDataType(MixedFormatSqlExtendParser.PrimitiveDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#colTypeList}.
	 * @param ctx the parse tree
	 */
	void enterColTypeList(MixedFormatSqlExtendParser.ColTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#colTypeList}.
	 * @param ctx the parse tree
	 */
	void exitColTypeList(MixedFormatSqlExtendParser.ColTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#colType}.
	 * @param ctx the parse tree
	 */
	void enterColType(MixedFormatSqlExtendParser.ColTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#colType}.
	 * @param ctx the parse tree
	 */
	void exitColType(MixedFormatSqlExtendParser.ColTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#complexColTypeList}.
	 * @param ctx the parse tree
	 */
	void enterComplexColTypeList(MixedFormatSqlExtendParser.ComplexColTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#complexColTypeList}.
	 * @param ctx the parse tree
	 */
	void exitComplexColTypeList(MixedFormatSqlExtendParser.ComplexColTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#complexColType}.
	 * @param ctx the parse tree
	 */
	void enterComplexColType(MixedFormatSqlExtendParser.ComplexColTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#complexColType}.
	 * @param ctx the parse tree
	 */
	void exitComplexColType(MixedFormatSqlExtendParser.ComplexColTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(MixedFormatSqlExtendParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(MixedFormatSqlExtendParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowClause(MixedFormatSqlExtendParser.WindowClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowClause(MixedFormatSqlExtendParser.WindowClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#namedWindow}.
	 * @param ctx the parse tree
	 */
	void enterNamedWindow(MixedFormatSqlExtendParser.NamedWindowContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#namedWindow}.
	 * @param ctx the parse tree
	 */
	void exitNamedWindow(MixedFormatSqlExtendParser.NamedWindowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code windowRef}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void enterWindowRef(MixedFormatSqlExtendParser.WindowRefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code windowRef}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void exitWindowRef(MixedFormatSqlExtendParser.WindowRefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code windowDef}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void enterWindowDef(MixedFormatSqlExtendParser.WindowDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code windowDef}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void exitWindowDef(MixedFormatSqlExtendParser.WindowDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#windowFrame}.
	 * @param ctx the parse tree
	 */
	void enterWindowFrame(MixedFormatSqlExtendParser.WindowFrameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#windowFrame}.
	 * @param ctx the parse tree
	 */
	void exitWindowFrame(MixedFormatSqlExtendParser.WindowFrameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void enterFrameBound(MixedFormatSqlExtendParser.FrameBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void exitFrameBound(MixedFormatSqlExtendParser.FrameBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(MixedFormatSqlExtendParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(MixedFormatSqlExtendParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(MixedFormatSqlExtendParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(MixedFormatSqlExtendParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingIdentifier(MixedFormatSqlExtendParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingIdentifier(MixedFormatSqlExtendParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void enterErrorIdent(MixedFormatSqlExtendParser.ErrorIdentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void exitErrorIdent(MixedFormatSqlExtendParser.ErrorIdentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void enterRealIdent(MixedFormatSqlExtendParser.RealIdentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void exitRealIdent(MixedFormatSqlExtendParser.RealIdentContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(MixedFormatSqlExtendParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(MixedFormatSqlExtendParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterUnquotedIdentifier(MixedFormatSqlExtendParser.UnquotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitUnquotedIdentifier(MixedFormatSqlExtendParser.UnquotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifierAlternative(MixedFormatSqlExtendParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifierAlternative(MixedFormatSqlExtendParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifier(MixedFormatSqlExtendParser.QuotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifier(MixedFormatSqlExtendParser.QuotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exponentLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterExponentLiteral(MixedFormatSqlExtendParser.ExponentLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exponentLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitExponentLiteral(MixedFormatSqlExtendParser.ExponentLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterDecimalLiteral(MixedFormatSqlExtendParser.DecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitDecimalLiteral(MixedFormatSqlExtendParser.DecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code legacyDecimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterLegacyDecimalLiteral(MixedFormatSqlExtendParser.LegacyDecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code legacyDecimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitLegacyDecimalLiteral(MixedFormatSqlExtendParser.LegacyDecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(MixedFormatSqlExtendParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(MixedFormatSqlExtendParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bigIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterBigIntLiteral(MixedFormatSqlExtendParser.BigIntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bigIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitBigIntLiteral(MixedFormatSqlExtendParser.BigIntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code smallIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterSmallIntLiteral(MixedFormatSqlExtendParser.SmallIntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code smallIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitSmallIntLiteral(MixedFormatSqlExtendParser.SmallIntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tinyIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterTinyIntLiteral(MixedFormatSqlExtendParser.TinyIntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tinyIntLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitTinyIntLiteral(MixedFormatSqlExtendParser.TinyIntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code doubleLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterDoubleLiteral(MixedFormatSqlExtendParser.DoubleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code doubleLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitDoubleLiteral(MixedFormatSqlExtendParser.DoubleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(MixedFormatSqlExtendParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(MixedFormatSqlExtendParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bigDecimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void enterBigDecimalLiteral(MixedFormatSqlExtendParser.BigDecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bigDecimalLiteral}
	 * labeled alternative in {@link MixedFormatSqlExtendParser#number}.
	 * @param ctx the parse tree
	 */
	void exitBigDecimalLiteral(MixedFormatSqlExtendParser.BigDecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 */
	void enterAnsiNonReserved(MixedFormatSqlExtendParser.AnsiNonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 */
	void exitAnsiNonReserved(MixedFormatSqlExtendParser.AnsiNonReservedContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#strictNonReserved}.
	 * @param ctx the parse tree
	 */
	void enterStrictNonReserved(MixedFormatSqlExtendParser.StrictNonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#strictNonReserved}.
	 * @param ctx the parse tree
	 */
	void exitStrictNonReserved(MixedFormatSqlExtendParser.StrictNonReservedContext ctx);
	/**
	 * Enter a parse tree produced by {@link MixedFormatSqlExtendParser#nonReserved}.
	 * @param ctx the parse tree
	 */
	void enterNonReserved(MixedFormatSqlExtendParser.NonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link MixedFormatSqlExtendParser#nonReserved}.
	 * @param ctx the parse tree
	 */
	void exitNonReserved(MixedFormatSqlExtendParser.NonReservedContext ctx);
}