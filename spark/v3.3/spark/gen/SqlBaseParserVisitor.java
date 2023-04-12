// Generated from /Users/jinsilei/arctic/arctic/spark/v3.3/spark/src/main/antlr4/com/netease/arctic/spark/sql/parser/SqlBaseParser.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SqlBaseParserParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SqlBaseParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleStatement(SqlBaseParserParser.SingleStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleExpression(SqlBaseParserParser.SingleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleTableIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTableIdentifier(SqlBaseParserParser.SingleTableIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleMultipartIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleMultipartIdentifier(SqlBaseParserParser.SingleMultipartIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleFunctionIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleFunctionIdentifier(SqlBaseParserParser.SingleFunctionIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleDataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleDataType(SqlBaseParserParser.SingleDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#singleTableSchema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTableSchema(SqlBaseParserParser.SingleTableSchemaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statementDefault}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementDefault(SqlBaseParserParser.StatementDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dmlStatement}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDmlStatement(SqlBaseParserParser.DmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code use}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse(SqlBaseParserParser.UseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code useNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUseNamespace(SqlBaseParserParser.UseNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setCatalog}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetCatalog(SqlBaseParserParser.SetCatalogContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateNamespace(SqlBaseParserParser.CreateNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setNamespaceProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetNamespaceProperties(SqlBaseParserParser.SetNamespacePropertiesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setNamespaceLocation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetNamespaceLocation(SqlBaseParserParser.SetNamespaceLocationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropNamespace(SqlBaseParserParser.DropNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showNamespaces}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowNamespaces(SqlBaseParserParser.ShowNamespacesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTable(SqlBaseParserParser.CreateTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createTableLike}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableLike(SqlBaseParserParser.CreateTableLikeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code replaceTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceTable(SqlBaseParserParser.ReplaceTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code analyze}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze(SqlBaseParserParser.AnalyzeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code analyzeTables}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyzeTables(SqlBaseParserParser.AnalyzeTablesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code addTableColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddTableColumns(SqlBaseParserParser.AddTableColumnsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code renameTableColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameTableColumn(SqlBaseParserParser.RenameTableColumnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropTableColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTableColumns(SqlBaseParserParser.DropTableColumnsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code renameTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameTable(SqlBaseParserParser.RenameTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setTableProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTableProperties(SqlBaseParserParser.SetTablePropertiesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unsetTableProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnsetTableProperties(SqlBaseParserParser.UnsetTablePropertiesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alterTableAlterColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableAlterColumn(SqlBaseParserParser.AlterTableAlterColumnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code hiveChangeColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHiveChangeColumn(SqlBaseParserParser.HiveChangeColumnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code hiveReplaceColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHiveReplaceColumns(SqlBaseParserParser.HiveReplaceColumnsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setTableSerDe}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTableSerDe(SqlBaseParserParser.SetTableSerDeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code addTablePartition}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddTablePartition(SqlBaseParserParser.AddTablePartitionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code renameTablePartition}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameTablePartition(SqlBaseParserParser.RenameTablePartitionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropTablePartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTablePartitions(SqlBaseParserParser.DropTablePartitionsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setTableLocation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTableLocation(SqlBaseParserParser.SetTableLocationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code recoverPartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecoverPartitions(SqlBaseParserParser.RecoverPartitionsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTable(SqlBaseParserParser.DropTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropView}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropView(SqlBaseParserParser.DropViewContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createView}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateView(SqlBaseParserParser.CreateViewContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createTempViewUsing}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTempViewUsing(SqlBaseParserParser.CreateTempViewUsingContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alterViewQuery}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterViewQuery(SqlBaseParserParser.AlterViewQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateFunction(SqlBaseParserParser.CreateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropFunction(SqlBaseParserParser.DropFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code explain}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain(SqlBaseParserParser.ExplainContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showTables}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTables(SqlBaseParserParser.ShowTablesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showTableExtended}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTableExtended(SqlBaseParserParser.ShowTableExtendedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showTblProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTblProperties(SqlBaseParserParser.ShowTblPropertiesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowColumns(SqlBaseParserParser.ShowColumnsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showViews}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowViews(SqlBaseParserParser.ShowViewsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showPartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowPartitions(SqlBaseParserParser.ShowPartitionsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showFunctions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowFunctions(SqlBaseParserParser.ShowFunctionsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showCreateTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateTable(SqlBaseParserParser.ShowCreateTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showCurrentNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCurrentNamespace(SqlBaseParserParser.ShowCurrentNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code showCatalogs}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCatalogs(SqlBaseParserParser.ShowCatalogsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code describeFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribeFunction(SqlBaseParserParser.DescribeFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code describeNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribeNamespace(SqlBaseParserParser.DescribeNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code describeRelation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribeRelation(SqlBaseParserParser.DescribeRelationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code describeQuery}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribeQuery(SqlBaseParserParser.DescribeQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code commentNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentNamespace(SqlBaseParserParser.CommentNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code commentTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentTable(SqlBaseParserParser.CommentTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code refreshTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshTable(SqlBaseParserParser.RefreshTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code refreshFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshFunction(SqlBaseParserParser.RefreshFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code refreshResource}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshResource(SqlBaseParserParser.RefreshResourceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code cacheTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCacheTable(SqlBaseParserParser.CacheTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code uncacheTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUncacheTable(SqlBaseParserParser.UncacheTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code clearCache}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClearCache(SqlBaseParserParser.ClearCacheContext ctx);
	/**
	 * Visit a parse tree produced by the {@code loadData}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadData(SqlBaseParserParser.LoadDataContext ctx);
	/**
	 * Visit a parse tree produced by the {@code truncateTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncateTable(SqlBaseParserParser.TruncateTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code repairTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepairTable(SqlBaseParserParser.RepairTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code manageResource}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitManageResource(SqlBaseParserParser.ManageResourceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code failNativeCommand}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFailNativeCommand(SqlBaseParserParser.FailNativeCommandContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setTimeZone}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTimeZone(SqlBaseParserParser.SetTimeZoneContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setQuotedConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetQuotedConfiguration(SqlBaseParserParser.SetQuotedConfigurationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetConfiguration(SqlBaseParserParser.SetConfigurationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code resetQuotedConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetQuotedConfiguration(SqlBaseParserParser.ResetQuotedConfigurationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code resetConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetConfiguration(SqlBaseParserParser.ResetConfigurationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code createIndex}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateIndex(SqlBaseParserParser.CreateIndexContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dropIndex}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropIndex(SqlBaseParserParser.DropIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#configKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfigKey(SqlBaseParserParser.ConfigKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#configValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfigValue(SqlBaseParserParser.ConfigValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#unsupportedHiveNativeCommands}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnsupportedHiveNativeCommands(SqlBaseParserParser.UnsupportedHiveNativeCommandsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#createTableHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableHeader(SqlBaseParserParser.CreateTableHeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#replaceTableHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceTableHeader(SqlBaseParserParser.ReplaceTableHeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#bucketSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBucketSpec(SqlBaseParserParser.BucketSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#skewSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkewSpec(SqlBaseParserParser.SkewSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#locationSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocationSpec(SqlBaseParserParser.LocationSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#commentSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentSpec(SqlBaseParserParser.CommentSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(SqlBaseParserParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code insertOverwriteTable}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertOverwriteTable(SqlBaseParserParser.InsertOverwriteTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code insertIntoTable}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertIntoTable(SqlBaseParserParser.InsertIntoTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code insertOverwriteHiveDir}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertOverwriteHiveDir(SqlBaseParserParser.InsertOverwriteHiveDirContext ctx);
	/**
	 * Visit a parse tree produced by the {@code insertOverwriteDir}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertOverwriteDir(SqlBaseParserParser.InsertOverwriteDirContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#partitionSpecLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSpecLocation(SqlBaseParserParser.PartitionSpecLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#partitionSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSpec(SqlBaseParserParser.PartitionSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#partitionVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionVal(SqlBaseParserParser.PartitionValContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#namespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamespace(SqlBaseParserParser.NamespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#namespaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamespaces(SqlBaseParserParser.NamespacesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#describeFuncName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribeFuncName(SqlBaseParserParser.DescribeFuncNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#describeColName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribeColName(SqlBaseParserParser.DescribeColNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#ctes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCtes(SqlBaseParserParser.CtesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#namedQuery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedQuery(SqlBaseParserParser.NamedQueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#tableProvider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableProvider(SqlBaseParserParser.TableProviderContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#createTableClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableClauses(SqlBaseParserParser.CreateTableClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#propertyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyList(SqlBaseParserParser.PropertyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(SqlBaseParserParser.PropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#propertyKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyKey(SqlBaseParserParser.PropertyKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#propertyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyValue(SqlBaseParserParser.PropertyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#constantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantList(SqlBaseParserParser.ConstantListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#nestedConstantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedConstantList(SqlBaseParserParser.NestedConstantListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#createFileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateFileFormat(SqlBaseParserParser.CreateFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableFileFormat}
	 * labeled alternative in {@link SqlBaseParserParser#fileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFileFormat(SqlBaseParserParser.TableFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericFileFormat}
	 * labeled alternative in {@link SqlBaseParserParser#fileFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericFileFormat(SqlBaseParserParser.GenericFileFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#storageHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageHandler(SqlBaseParserParser.StorageHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(SqlBaseParserParser.ResourceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code singleInsertQuery}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleInsertQuery(SqlBaseParserParser.SingleInsertQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiInsertQuery}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertQuery(SqlBaseParserParser.MultiInsertQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code deleteFromTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteFromTable(SqlBaseParserParser.DeleteFromTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code updateTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateTable(SqlBaseParserParser.UpdateTableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mergeIntoTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergeIntoTable(SqlBaseParserParser.MergeIntoTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#queryOrganization}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryOrganization(SqlBaseParserParser.QueryOrganizationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#multiInsertQueryBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiInsertQueryBody(SqlBaseParserParser.MultiInsertQueryBodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code queryTermDefault}
	 * labeled alternative in {@link SqlBaseParserParser#queryTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryTermDefault(SqlBaseParserParser.QueryTermDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code setOperation}
	 * labeled alternative in {@link SqlBaseParserParser#queryTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetOperation(SqlBaseParserParser.SetOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code queryPrimaryDefault}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryPrimaryDefault(SqlBaseParserParser.QueryPrimaryDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fromStmt}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStmt(SqlBaseParserParser.FromStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code table}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable(SqlBaseParserParser.TableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inlineTableDefault1}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineTableDefault1(SqlBaseParserParser.InlineTableDefault1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code subquery}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(SqlBaseParserParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#sortItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortItem(SqlBaseParserParser.SortItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#fromStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStatement(SqlBaseParserParser.FromStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#fromStatementBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromStatementBody(SqlBaseParserParser.FromStatementBodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code transformQuerySpecification}
	 * labeled alternative in {@link SqlBaseParserParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformQuerySpecification(SqlBaseParserParser.TransformQuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code regularQuerySpecification}
	 * labeled alternative in {@link SqlBaseParserParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularQuerySpecification(SqlBaseParserParser.RegularQuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#transformClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformClause(SqlBaseParserParser.TransformClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(SqlBaseParserParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#setClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClause(SqlBaseParserParser.SetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#matchedClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchedClause(SqlBaseParserParser.MatchedClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#notMatchedClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotMatchedClause(SqlBaseParserParser.NotMatchedClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#matchedAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchedAction(SqlBaseParserParser.MatchedActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#notMatchedAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotMatchedAction(SqlBaseParserParser.NotMatchedActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#assignmentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentList(SqlBaseParserParser.AssignmentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(SqlBaseParserParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(SqlBaseParserParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(SqlBaseParserParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHint(SqlBaseParserParser.HintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#hintStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHintStatement(SqlBaseParserParser.HintStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(SqlBaseParserParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#temporalClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemporalClause(SqlBaseParserParser.TemporalClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#aggregationClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationClause(SqlBaseParserParser.AggregationClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(SqlBaseParserParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#groupingAnalytics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingAnalytics(SqlBaseParserParser.GroupingAnalyticsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#groupingElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingElement(SqlBaseParserParser.GroupingElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#groupingSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSet(SqlBaseParserParser.GroupingSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#pivotClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivotClause(SqlBaseParserParser.PivotClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#pivotColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivotColumn(SqlBaseParserParser.PivotColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#pivotValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivotValue(SqlBaseParserParser.PivotValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#lateralView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLateralView(SqlBaseParserParser.LateralViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#setQuantifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetQuantifier(SqlBaseParserParser.SetQuantifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(SqlBaseParserParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#joinRelation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinRelation(SqlBaseParserParser.JoinRelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(SqlBaseParserParser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#joinCriteria}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinCriteria(SqlBaseParserParser.JoinCriteriaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#sample}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSample(SqlBaseParserParser.SampleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByPercentile}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByPercentile(SqlBaseParserParser.SampleByPercentileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByRows}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByRows(SqlBaseParserParser.SampleByRowsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByBucket}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByBucket(SqlBaseParserParser.SampleByBucketContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sampleByBytes}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSampleByBytes(SqlBaseParserParser.SampleByBytesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(SqlBaseParserParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#identifierSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierSeq(SqlBaseParserParser.IdentifierSeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#orderedIdentifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedIdentifierList(SqlBaseParserParser.OrderedIdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#orderedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedIdentifier(SqlBaseParserParser.OrderedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#identifierCommentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierCommentList(SqlBaseParserParser.IdentifierCommentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#identifierComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierComment(SqlBaseParserParser.IdentifierCommentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableName}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(SqlBaseParserParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code aliasedQuery}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasedQuery(SqlBaseParserParser.AliasedQueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code aliasedRelation}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasedRelation(SqlBaseParserParser.AliasedRelationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inlineTableDefault2}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineTableDefault2(SqlBaseParserParser.InlineTableDefault2Context ctx);
	/**
	 * Visit a parse tree produced by the {@code tableValuedFunction}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableValuedFunction(SqlBaseParserParser.TableValuedFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#inlineTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineTable(SqlBaseParserParser.InlineTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#functionTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTable(SqlBaseParserParser.FunctionTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#tableAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAlias(SqlBaseParserParser.TableAliasContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rowFormatSerde}
	 * labeled alternative in {@link SqlBaseParserParser#rowFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormatSerde(SqlBaseParserParser.RowFormatSerdeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rowFormatDelimited}
	 * labeled alternative in {@link SqlBaseParserParser#rowFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowFormatDelimited(SqlBaseParserParser.RowFormatDelimitedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifierList(SqlBaseParserParser.MultipartIdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifier(SqlBaseParserParser.MultipartIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierPropertyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifierPropertyList(SqlBaseParserParser.MultipartIdentifierPropertyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifierProperty(SqlBaseParserParser.MultipartIdentifierPropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#tableIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableIdentifier(SqlBaseParserParser.TableIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#functionIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionIdentifier(SqlBaseParserParser.FunctionIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#namedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedExpression(SqlBaseParserParser.NamedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#namedExpressionSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedExpressionSeq(SqlBaseParserParser.NamedExpressionSeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#partitionFieldList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionFieldList(SqlBaseParserParser.PartitionFieldListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code partitionTransform}
	 * labeled alternative in {@link SqlBaseParserParser#partitionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionTransform(SqlBaseParserParser.PartitionTransformContext ctx);
	/**
	 * Visit a parse tree produced by the {@code partitionColumn}
	 * labeled alternative in {@link SqlBaseParserParser#partitionField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionColumn(SqlBaseParserParser.PartitionColumnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identityTransform}
	 * labeled alternative in {@link SqlBaseParserParser#transform}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentityTransform(SqlBaseParserParser.IdentityTransformContext ctx);
	/**
	 * Visit a parse tree produced by the {@code applyTransform}
	 * labeled alternative in {@link SqlBaseParserParser#transform}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplyTransform(SqlBaseParserParser.ApplyTransformContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#transformArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformArgument(SqlBaseParserParser.TransformArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SqlBaseParserParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#expressionSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionSeq(SqlBaseParserParser.ExpressionSeqContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalNot(SqlBaseParserParser.LogicalNotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code predicated}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicated(SqlBaseParserParser.PredicatedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exists}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExists(SqlBaseParserParser.ExistsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalBinary}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalBinary(SqlBaseParserParser.LogicalBinaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(SqlBaseParserParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code valueExpressionDefault}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueExpressionDefault(SqlBaseParserParser.ValueExpressionDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparison}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(SqlBaseParserParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arithmeticBinary}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticBinary(SqlBaseParserParser.ArithmeticBinaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arithmeticUnary}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticUnary(SqlBaseParserParser.ArithmeticUnaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#datetimeUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetimeUnit(SqlBaseParserParser.DatetimeUnitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code struct}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStruct(SqlBaseParserParser.StructContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dereference}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDereference(SqlBaseParserParser.DereferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timestampadd}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampadd(SqlBaseParserParser.TimestampaddContext ctx);
	/**
	 * Visit a parse tree produced by the {@code substring}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstring(SqlBaseParserParser.SubstringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code cast}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast(SqlBaseParserParser.CastContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lambda}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(SqlBaseParserParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedExpression(SqlBaseParserParser.ParenthesizedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trim}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrim(SqlBaseParserParser.TrimContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleCase}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleCase(SqlBaseParserParser.SimpleCaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code currentLike}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCurrentLike(SqlBaseParserParser.CurrentLikeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code columnReference}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnReference(SqlBaseParserParser.ColumnReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rowConstructor}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowConstructor(SqlBaseParserParser.RowConstructorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code last}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLast(SqlBaseParserParser.LastContext ctx);
	/**
	 * Visit a parse tree produced by the {@code star}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStar(SqlBaseParserParser.StarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code overlay}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlay(SqlBaseParserParser.OverlayContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubscript(SqlBaseParserParser.SubscriptContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timestampdiff}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampdiff(SqlBaseParserParser.TimestampdiffContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryExpression(SqlBaseParserParser.SubqueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code constantDefault}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDefault(SqlBaseParserParser.ConstantDefaultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code extract}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtract(SqlBaseParserParser.ExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code percentile}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPercentile(SqlBaseParserParser.PercentileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(SqlBaseParserParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code searchedCase}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchedCase(SqlBaseParserParser.SearchedCaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code position}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition(SqlBaseParserParser.PositionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code first}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst(SqlBaseParserParser.FirstContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullLiteral(SqlBaseParserParser.NullLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intervalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalLiteral(SqlBaseParserParser.IntervalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeConstructor}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeConstructor(SqlBaseParserParser.TypeConstructorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(SqlBaseParserParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(SqlBaseParserParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(SqlBaseParserParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(SqlBaseParserParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#arithmeticOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticOperator(SqlBaseParserParser.ArithmeticOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#predicateOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateOperator(SqlBaseParserParser.PredicateOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#booleanValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanValue(SqlBaseParserParser.BooleanValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(SqlBaseParserParser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#errorCapturingMultiUnitsInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingMultiUnitsInterval(SqlBaseParserParser.ErrorCapturingMultiUnitsIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#multiUnitsInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiUnitsInterval(SqlBaseParserParser.MultiUnitsIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#errorCapturingUnitToUnitInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingUnitToUnitInterval(SqlBaseParserParser.ErrorCapturingUnitToUnitIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#unitToUnitInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnitToUnitInterval(SqlBaseParserParser.UnitToUnitIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#intervalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalValue(SqlBaseParserParser.IntervalValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#colPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColPosition(SqlBaseParserParser.ColPositionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexDataType(SqlBaseParserParser.ComplexDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearMonthIntervalDataType(SqlBaseParserParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayTimeIntervalDataType(SqlBaseParserParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveDataType(SqlBaseParserParser.PrimitiveDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#qualifiedColTypeWithPositionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedColTypeWithPositionList(SqlBaseParserParser.QualifiedColTypeWithPositionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#qualifiedColTypeWithPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedColTypeWithPosition(SqlBaseParserParser.QualifiedColTypeWithPositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#colTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColTypeList(SqlBaseParserParser.ColTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#colType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColType(SqlBaseParserParser.ColTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#complexColTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexColTypeList(SqlBaseParserParser.ComplexColTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#complexColType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexColType(SqlBaseParserParser.ComplexColTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(SqlBaseParserParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#windowClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause(SqlBaseParserParser.WindowClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#namedWindow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedWindow(SqlBaseParserParser.NamedWindowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code windowRef}
	 * labeled alternative in {@link SqlBaseParserParser#windowSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowRef(SqlBaseParserParser.WindowRefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code windowDef}
	 * labeled alternative in {@link SqlBaseParserParser#windowSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowDef(SqlBaseParserParser.WindowDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#windowFrame}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFrame(SqlBaseParserParser.WindowFrameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#frameBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBound(SqlBaseParserParser.FrameBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedNameList(SqlBaseParserParser.QualifiedNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(SqlBaseParserParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(SqlBaseParserParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingIdentifier(SqlBaseParserParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link SqlBaseParserParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorIdent(SqlBaseParserParser.ErrorIdentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link SqlBaseParserParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealIdent(SqlBaseParserParser.RealIdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(SqlBaseParserParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link SqlBaseParserParser#strictIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnquotedIdentifier(SqlBaseParserParser.UnquotedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link SqlBaseParserParser#strictIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuotedIdentifierAlternative(SqlBaseParserParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuotedIdentifier(SqlBaseParserParser.QuotedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentLiteral(SqlBaseParserParser.ExponentLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(SqlBaseParserParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code legacyDecimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLegacyDecimalLiteral(SqlBaseParserParser.LegacyDecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(SqlBaseParserParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bigIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigIntLiteral(SqlBaseParserParser.BigIntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code smallIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSmallIntLiteral(SqlBaseParserParser.SmallIntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tinyIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTinyIntLiteral(SqlBaseParserParser.TinyIntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code doubleLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleLiteral(SqlBaseParserParser.DoubleLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(SqlBaseParserParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bigDecimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigDecimalLiteral(SqlBaseParserParser.BigDecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#alterColumnAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterColumnAction(SqlBaseParserParser.AlterColumnActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnsiNonReserved(SqlBaseParserParser.AnsiNonReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#strictNonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrictNonReserved(SqlBaseParserParser.StrictNonReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlBaseParserParser#nonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReserved(SqlBaseParserParser.NonReservedContext ctx);
}