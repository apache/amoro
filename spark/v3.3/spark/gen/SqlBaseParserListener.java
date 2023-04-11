// Generated from /Users/jinsilei/arctic/arctic/spark/v3.3/spark/src/main/antlr4/com/netease/arctic/spark/sql/parser/SqlBaseParser.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SqlBaseParserParser}.
 */
public interface SqlBaseParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleStatement}.
	 * @param ctx the parse tree
	 */
	void enterSingleStatement(SqlBaseParserParser.SingleStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleStatement}.
	 * @param ctx the parse tree
	 */
	void exitSingleStatement(SqlBaseParserParser.SingleStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleExpression}.
	 * @param ctx the parse tree
	 */
	void enterSingleExpression(SqlBaseParserParser.SingleExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleExpression}.
	 * @param ctx the parse tree
	 */
	void exitSingleExpression(SqlBaseParserParser.SingleExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleTableIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterSingleTableIdentifier(SqlBaseParserParser.SingleTableIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleTableIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitSingleTableIdentifier(SqlBaseParserParser.SingleTableIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleMultipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterSingleMultipartIdentifier(SqlBaseParserParser.SingleMultipartIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleMultipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitSingleMultipartIdentifier(SqlBaseParserParser.SingleMultipartIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleFunctionIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterSingleFunctionIdentifier(SqlBaseParserParser.SingleFunctionIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleFunctionIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitSingleFunctionIdentifier(SqlBaseParserParser.SingleFunctionIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleDataType}.
	 * @param ctx the parse tree
	 */
	void enterSingleDataType(SqlBaseParserParser.SingleDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleDataType}.
	 * @param ctx the parse tree
	 */
	void exitSingleDataType(SqlBaseParserParser.SingleDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#singleTableSchema}.
	 * @param ctx the parse tree
	 */
	void enterSingleTableSchema(SqlBaseParserParser.SingleTableSchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#singleTableSchema}.
	 * @param ctx the parse tree
	 */
	void exitSingleTableSchema(SqlBaseParserParser.SingleTableSchemaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code statementDefault}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatementDefault(SqlBaseParserParser.StatementDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code statementDefault}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatementDefault(SqlBaseParserParser.StatementDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dmlStatement}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDmlStatement(SqlBaseParserParser.DmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dmlStatement}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDmlStatement(SqlBaseParserParser.DmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code use}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterUse(SqlBaseParserParser.UseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code use}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitUse(SqlBaseParserParser.UseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code useNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterUseNamespace(SqlBaseParserParser.UseNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code useNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitUseNamespace(SqlBaseParserParser.UseNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setCatalog}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetCatalog(SqlBaseParserParser.SetCatalogContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setCatalog}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetCatalog(SqlBaseParserParser.SetCatalogContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateNamespace(SqlBaseParserParser.CreateNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateNamespace(SqlBaseParserParser.CreateNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setNamespaceProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetNamespaceProperties(SqlBaseParserParser.SetNamespacePropertiesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setNamespaceProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetNamespaceProperties(SqlBaseParserParser.SetNamespacePropertiesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setNamespaceLocation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetNamespaceLocation(SqlBaseParserParser.SetNamespaceLocationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setNamespaceLocation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetNamespaceLocation(SqlBaseParserParser.SetNamespaceLocationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropNamespace(SqlBaseParserParser.DropNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropNamespace(SqlBaseParserParser.DropNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showNamespaces}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowNamespaces(SqlBaseParserParser.ShowNamespacesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showNamespaces}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowNamespaces(SqlBaseParserParser.ShowNamespacesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateTable(SqlBaseParserParser.CreateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateTable(SqlBaseParserParser.CreateTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createTableLike}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableLike(SqlBaseParserParser.CreateTableLikeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createTableLike}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableLike(SqlBaseParserParser.CreateTableLikeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code replaceTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterReplaceTable(SqlBaseParserParser.ReplaceTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code replaceTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitReplaceTable(SqlBaseParserParser.ReplaceTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code analyze}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze(SqlBaseParserParser.AnalyzeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code analyze}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze(SqlBaseParserParser.AnalyzeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code analyzeTables}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAnalyzeTables(SqlBaseParserParser.AnalyzeTablesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code analyzeTables}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAnalyzeTables(SqlBaseParserParser.AnalyzeTablesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addTableColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAddTableColumns(SqlBaseParserParser.AddTableColumnsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addTableColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAddTableColumns(SqlBaseParserParser.AddTableColumnsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code renameTableColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRenameTableColumn(SqlBaseParserParser.RenameTableColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code renameTableColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRenameTableColumn(SqlBaseParserParser.RenameTableColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropTableColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropTableColumns(SqlBaseParserParser.DropTableColumnsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropTableColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropTableColumns(SqlBaseParserParser.DropTableColumnsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code renameTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRenameTable(SqlBaseParserParser.RenameTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code renameTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRenameTable(SqlBaseParserParser.RenameTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setTableProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetTableProperties(SqlBaseParserParser.SetTablePropertiesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setTableProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetTableProperties(SqlBaseParserParser.SetTablePropertiesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unsetTableProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterUnsetTableProperties(SqlBaseParserParser.UnsetTablePropertiesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unsetTableProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitUnsetTableProperties(SqlBaseParserParser.UnsetTablePropertiesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterTableAlterColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableAlterColumn(SqlBaseParserParser.AlterTableAlterColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterTableAlterColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableAlterColumn(SqlBaseParserParser.AlterTableAlterColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code hiveChangeColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterHiveChangeColumn(SqlBaseParserParser.HiveChangeColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code hiveChangeColumn}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitHiveChangeColumn(SqlBaseParserParser.HiveChangeColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code hiveReplaceColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterHiveReplaceColumns(SqlBaseParserParser.HiveReplaceColumnsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code hiveReplaceColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitHiveReplaceColumns(SqlBaseParserParser.HiveReplaceColumnsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setTableSerDe}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetTableSerDe(SqlBaseParserParser.SetTableSerDeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setTableSerDe}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetTableSerDe(SqlBaseParserParser.SetTableSerDeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addTablePartition}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAddTablePartition(SqlBaseParserParser.AddTablePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addTablePartition}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAddTablePartition(SqlBaseParserParser.AddTablePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code renameTablePartition}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRenameTablePartition(SqlBaseParserParser.RenameTablePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code renameTablePartition}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRenameTablePartition(SqlBaseParserParser.RenameTablePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropTablePartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropTablePartitions(SqlBaseParserParser.DropTablePartitionsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropTablePartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropTablePartitions(SqlBaseParserParser.DropTablePartitionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setTableLocation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetTableLocation(SqlBaseParserParser.SetTableLocationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setTableLocation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetTableLocation(SqlBaseParserParser.SetTableLocationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code recoverPartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRecoverPartitions(SqlBaseParserParser.RecoverPartitionsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code recoverPartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRecoverPartitions(SqlBaseParserParser.RecoverPartitionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropTable(SqlBaseParserParser.DropTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropTable(SqlBaseParserParser.DropTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropView}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropView(SqlBaseParserParser.DropViewContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropView}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropView(SqlBaseParserParser.DropViewContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createView}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateView(SqlBaseParserParser.CreateViewContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createView}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateView(SqlBaseParserParser.CreateViewContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createTempViewUsing}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateTempViewUsing(SqlBaseParserParser.CreateTempViewUsingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createTempViewUsing}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateTempViewUsing(SqlBaseParserParser.CreateTempViewUsingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterViewQuery}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAlterViewQuery(SqlBaseParserParser.AlterViewQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterViewQuery}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAlterViewQuery(SqlBaseParserParser.AlterViewQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateFunction(SqlBaseParserParser.CreateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateFunction(SqlBaseParserParser.CreateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropFunction(SqlBaseParserParser.DropFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropFunction(SqlBaseParserParser.DropFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code explain}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterExplain(SqlBaseParserParser.ExplainContext ctx);
	/**
	 * Exit a parse tree produced by the {@code explain}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitExplain(SqlBaseParserParser.ExplainContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showTables}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowTables(SqlBaseParserParser.ShowTablesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showTables}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowTables(SqlBaseParserParser.ShowTablesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showTableExtended}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowTableExtended(SqlBaseParserParser.ShowTableExtendedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showTableExtended}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowTableExtended(SqlBaseParserParser.ShowTableExtendedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showTblProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowTblProperties(SqlBaseParserParser.ShowTblPropertiesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showTblProperties}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowTblProperties(SqlBaseParserParser.ShowTblPropertiesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowColumns(SqlBaseParserParser.ShowColumnsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showColumns}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowColumns(SqlBaseParserParser.ShowColumnsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showViews}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowViews(SqlBaseParserParser.ShowViewsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showViews}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowViews(SqlBaseParserParser.ShowViewsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showPartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowPartitions(SqlBaseParserParser.ShowPartitionsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showPartitions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowPartitions(SqlBaseParserParser.ShowPartitionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showFunctions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowFunctions(SqlBaseParserParser.ShowFunctionsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showFunctions}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowFunctions(SqlBaseParserParser.ShowFunctionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCreateTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateTable(SqlBaseParserParser.ShowCreateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCreateTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateTable(SqlBaseParserParser.ShowCreateTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCurrentNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowCurrentNamespace(SqlBaseParserParser.ShowCurrentNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCurrentNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowCurrentNamespace(SqlBaseParserParser.ShowCurrentNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCatalogs}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterShowCatalogs(SqlBaseParserParser.ShowCatalogsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCatalogs}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitShowCatalogs(SqlBaseParserParser.ShowCatalogsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describeFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribeFunction(SqlBaseParserParser.DescribeFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describeFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribeFunction(SqlBaseParserParser.DescribeFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describeNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribeNamespace(SqlBaseParserParser.DescribeNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describeNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribeNamespace(SqlBaseParserParser.DescribeNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describeRelation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribeRelation(SqlBaseParserParser.DescribeRelationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describeRelation}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribeRelation(SqlBaseParserParser.DescribeRelationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describeQuery}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribeQuery(SqlBaseParserParser.DescribeQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describeQuery}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribeQuery(SqlBaseParserParser.DescribeQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code commentNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCommentNamespace(SqlBaseParserParser.CommentNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code commentNamespace}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCommentNamespace(SqlBaseParserParser.CommentNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code commentTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCommentTable(SqlBaseParserParser.CommentTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code commentTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCommentTable(SqlBaseParserParser.CommentTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code refreshTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRefreshTable(SqlBaseParserParser.RefreshTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code refreshTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRefreshTable(SqlBaseParserParser.RefreshTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code refreshFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRefreshFunction(SqlBaseParserParser.RefreshFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code refreshFunction}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRefreshFunction(SqlBaseParserParser.RefreshFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code refreshResource}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRefreshResource(SqlBaseParserParser.RefreshResourceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code refreshResource}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRefreshResource(SqlBaseParserParser.RefreshResourceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code cacheTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCacheTable(SqlBaseParserParser.CacheTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code cacheTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCacheTable(SqlBaseParserParser.CacheTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uncacheTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterUncacheTable(SqlBaseParserParser.UncacheTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uncacheTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitUncacheTable(SqlBaseParserParser.UncacheTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code clearCache}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterClearCache(SqlBaseParserParser.ClearCacheContext ctx);
	/**
	 * Exit a parse tree produced by the {@code clearCache}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitClearCache(SqlBaseParserParser.ClearCacheContext ctx);
	/**
	 * Enter a parse tree produced by the {@code loadData}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterLoadData(SqlBaseParserParser.LoadDataContext ctx);
	/**
	 * Exit a parse tree produced by the {@code loadData}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitLoadData(SqlBaseParserParser.LoadDataContext ctx);
	/**
	 * Enter a parse tree produced by the {@code truncateTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterTruncateTable(SqlBaseParserParser.TruncateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code truncateTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitTruncateTable(SqlBaseParserParser.TruncateTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code repairTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRepairTable(SqlBaseParserParser.RepairTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code repairTable}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRepairTable(SqlBaseParserParser.RepairTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code manageResource}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterManageResource(SqlBaseParserParser.ManageResourceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code manageResource}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitManageResource(SqlBaseParserParser.ManageResourceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code failNativeCommand}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterFailNativeCommand(SqlBaseParserParser.FailNativeCommandContext ctx);
	/**
	 * Exit a parse tree produced by the {@code failNativeCommand}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitFailNativeCommand(SqlBaseParserParser.FailNativeCommandContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setTimeZone}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetTimeZone(SqlBaseParserParser.SetTimeZoneContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setTimeZone}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetTimeZone(SqlBaseParserParser.SetTimeZoneContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setQuotedConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetQuotedConfiguration(SqlBaseParserParser.SetQuotedConfigurationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setQuotedConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetQuotedConfiguration(SqlBaseParserParser.SetQuotedConfigurationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetConfiguration(SqlBaseParserParser.SetConfigurationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetConfiguration(SqlBaseParserParser.SetConfigurationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code resetQuotedConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterResetQuotedConfiguration(SqlBaseParserParser.ResetQuotedConfigurationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code resetQuotedConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitResetQuotedConfiguration(SqlBaseParserParser.ResetQuotedConfigurationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code resetConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterResetConfiguration(SqlBaseParserParser.ResetConfigurationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code resetConfiguration}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitResetConfiguration(SqlBaseParserParser.ResetConfigurationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code createIndex}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCreateIndex(SqlBaseParserParser.CreateIndexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code createIndex}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCreateIndex(SqlBaseParserParser.CreateIndexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dropIndex}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterDropIndex(SqlBaseParserParser.DropIndexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dropIndex}
	 * labeled alternative in {@link SqlBaseParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitDropIndex(SqlBaseParserParser.DropIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#configKey}.
	 * @param ctx the parse tree
	 */
	void enterConfigKey(SqlBaseParserParser.ConfigKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#configKey}.
	 * @param ctx the parse tree
	 */
	void exitConfigKey(SqlBaseParserParser.ConfigKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#configValue}.
	 * @param ctx the parse tree
	 */
	void enterConfigValue(SqlBaseParserParser.ConfigValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#configValue}.
	 * @param ctx the parse tree
	 */
	void exitConfigValue(SqlBaseParserParser.ConfigValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#unsupportedHiveNativeCommands}.
	 * @param ctx the parse tree
	 */
	void enterUnsupportedHiveNativeCommands(SqlBaseParserParser.UnsupportedHiveNativeCommandsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#unsupportedHiveNativeCommands}.
	 * @param ctx the parse tree
	 */
	void exitUnsupportedHiveNativeCommands(SqlBaseParserParser.UnsupportedHiveNativeCommandsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#createTableHeader}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableHeader(SqlBaseParserParser.CreateTableHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#createTableHeader}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableHeader(SqlBaseParserParser.CreateTableHeaderContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#replaceTableHeader}.
	 * @param ctx the parse tree
	 */
	void enterReplaceTableHeader(SqlBaseParserParser.ReplaceTableHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#replaceTableHeader}.
	 * @param ctx the parse tree
	 */
	void exitReplaceTableHeader(SqlBaseParserParser.ReplaceTableHeaderContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#bucketSpec}.
	 * @param ctx the parse tree
	 */
	void enterBucketSpec(SqlBaseParserParser.BucketSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#bucketSpec}.
	 * @param ctx the parse tree
	 */
	void exitBucketSpec(SqlBaseParserParser.BucketSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#skewSpec}.
	 * @param ctx the parse tree
	 */
	void enterSkewSpec(SqlBaseParserParser.SkewSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#skewSpec}.
	 * @param ctx the parse tree
	 */
	void exitSkewSpec(SqlBaseParserParser.SkewSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#locationSpec}.
	 * @param ctx the parse tree
	 */
	void enterLocationSpec(SqlBaseParserParser.LocationSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#locationSpec}.
	 * @param ctx the parse tree
	 */
	void exitLocationSpec(SqlBaseParserParser.LocationSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#commentSpec}.
	 * @param ctx the parse tree
	 */
	void enterCommentSpec(SqlBaseParserParser.CommentSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#commentSpec}.
	 * @param ctx the parse tree
	 */
	void exitCommentSpec(SqlBaseParserParser.CommentSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(SqlBaseParserParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(SqlBaseParserParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code insertOverwriteTable}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void enterInsertOverwriteTable(SqlBaseParserParser.InsertOverwriteTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code insertOverwriteTable}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void exitInsertOverwriteTable(SqlBaseParserParser.InsertOverwriteTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code insertIntoTable}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void enterInsertIntoTable(SqlBaseParserParser.InsertIntoTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code insertIntoTable}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void exitInsertIntoTable(SqlBaseParserParser.InsertIntoTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code insertOverwriteHiveDir}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void enterInsertOverwriteHiveDir(SqlBaseParserParser.InsertOverwriteHiveDirContext ctx);
	/**
	 * Exit a parse tree produced by the {@code insertOverwriteHiveDir}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void exitInsertOverwriteHiveDir(SqlBaseParserParser.InsertOverwriteHiveDirContext ctx);
	/**
	 * Enter a parse tree produced by the {@code insertOverwriteDir}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void enterInsertOverwriteDir(SqlBaseParserParser.InsertOverwriteDirContext ctx);
	/**
	 * Exit a parse tree produced by the {@code insertOverwriteDir}
	 * labeled alternative in {@link SqlBaseParserParser#insertInto}.
	 * @param ctx the parse tree
	 */
	void exitInsertOverwriteDir(SqlBaseParserParser.InsertOverwriteDirContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#partitionSpecLocation}.
	 * @param ctx the parse tree
	 */
	void enterPartitionSpecLocation(SqlBaseParserParser.PartitionSpecLocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#partitionSpecLocation}.
	 * @param ctx the parse tree
	 */
	void exitPartitionSpecLocation(SqlBaseParserParser.PartitionSpecLocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#partitionSpec}.
	 * @param ctx the parse tree
	 */
	void enterPartitionSpec(SqlBaseParserParser.PartitionSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#partitionSpec}.
	 * @param ctx the parse tree
	 */
	void exitPartitionSpec(SqlBaseParserParser.PartitionSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#partitionVal}.
	 * @param ctx the parse tree
	 */
	void enterPartitionVal(SqlBaseParserParser.PartitionValContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#partitionVal}.
	 * @param ctx the parse tree
	 */
	void exitPartitionVal(SqlBaseParserParser.PartitionValContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#namespace}.
	 * @param ctx the parse tree
	 */
	void enterNamespace(SqlBaseParserParser.NamespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#namespace}.
	 * @param ctx the parse tree
	 */
	void exitNamespace(SqlBaseParserParser.NamespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#namespaces}.
	 * @param ctx the parse tree
	 */
	void enterNamespaces(SqlBaseParserParser.NamespacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#namespaces}.
	 * @param ctx the parse tree
	 */
	void exitNamespaces(SqlBaseParserParser.NamespacesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#describeFuncName}.
	 * @param ctx the parse tree
	 */
	void enterDescribeFuncName(SqlBaseParserParser.DescribeFuncNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#describeFuncName}.
	 * @param ctx the parse tree
	 */
	void exitDescribeFuncName(SqlBaseParserParser.DescribeFuncNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#describeColName}.
	 * @param ctx the parse tree
	 */
	void enterDescribeColName(SqlBaseParserParser.DescribeColNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#describeColName}.
	 * @param ctx the parse tree
	 */
	void exitDescribeColName(SqlBaseParserParser.DescribeColNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#ctes}.
	 * @param ctx the parse tree
	 */
	void enterCtes(SqlBaseParserParser.CtesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#ctes}.
	 * @param ctx the parse tree
	 */
	void exitCtes(SqlBaseParserParser.CtesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#namedQuery}.
	 * @param ctx the parse tree
	 */
	void enterNamedQuery(SqlBaseParserParser.NamedQueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#namedQuery}.
	 * @param ctx the parse tree
	 */
	void exitNamedQuery(SqlBaseParserParser.NamedQueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#tableProvider}.
	 * @param ctx the parse tree
	 */
	void enterTableProvider(SqlBaseParserParser.TableProviderContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#tableProvider}.
	 * @param ctx the parse tree
	 */
	void exitTableProvider(SqlBaseParserParser.TableProviderContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#createTableClauses}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableClauses(SqlBaseParserParser.CreateTableClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#createTableClauses}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableClauses(SqlBaseParserParser.CreateTableClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#propertyList}.
	 * @param ctx the parse tree
	 */
	void enterPropertyList(SqlBaseParserParser.PropertyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#propertyList}.
	 * @param ctx the parse tree
	 */
	void exitPropertyList(SqlBaseParserParser.PropertyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(SqlBaseParserParser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(SqlBaseParserParser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#propertyKey}.
	 * @param ctx the parse tree
	 */
	void enterPropertyKey(SqlBaseParserParser.PropertyKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#propertyKey}.
	 * @param ctx the parse tree
	 */
	void exitPropertyKey(SqlBaseParserParser.PropertyKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void enterPropertyValue(SqlBaseParserParser.PropertyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#propertyValue}.
	 * @param ctx the parse tree
	 */
	void exitPropertyValue(SqlBaseParserParser.PropertyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#constantList}.
	 * @param ctx the parse tree
	 */
	void enterConstantList(SqlBaseParserParser.ConstantListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#constantList}.
	 * @param ctx the parse tree
	 */
	void exitConstantList(SqlBaseParserParser.ConstantListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#nestedConstantList}.
	 * @param ctx the parse tree
	 */
	void enterNestedConstantList(SqlBaseParserParser.NestedConstantListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#nestedConstantList}.
	 * @param ctx the parse tree
	 */
	void exitNestedConstantList(SqlBaseParserParser.NestedConstantListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#createFileFormat}.
	 * @param ctx the parse tree
	 */
	void enterCreateFileFormat(SqlBaseParserParser.CreateFileFormatContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#createFileFormat}.
	 * @param ctx the parse tree
	 */
	void exitCreateFileFormat(SqlBaseParserParser.CreateFileFormatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableFileFormat}
	 * labeled alternative in {@link SqlBaseParserParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void enterTableFileFormat(SqlBaseParserParser.TableFileFormatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableFileFormat}
	 * labeled alternative in {@link SqlBaseParserParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void exitTableFileFormat(SqlBaseParserParser.TableFileFormatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericFileFormat}
	 * labeled alternative in {@link SqlBaseParserParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void enterGenericFileFormat(SqlBaseParserParser.GenericFileFormatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericFileFormat}
	 * labeled alternative in {@link SqlBaseParserParser#fileFormat}.
	 * @param ctx the parse tree
	 */
	void exitGenericFileFormat(SqlBaseParserParser.GenericFileFormatContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#storageHandler}.
	 * @param ctx the parse tree
	 */
	void enterStorageHandler(SqlBaseParserParser.StorageHandlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#storageHandler}.
	 * @param ctx the parse tree
	 */
	void exitStorageHandler(SqlBaseParserParser.StorageHandlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#resource}.
	 * @param ctx the parse tree
	 */
	void enterResource(SqlBaseParserParser.ResourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#resource}.
	 * @param ctx the parse tree
	 */
	void exitResource(SqlBaseParserParser.ResourceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleInsertQuery}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void enterSingleInsertQuery(SqlBaseParserParser.SingleInsertQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleInsertQuery}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void exitSingleInsertQuery(SqlBaseParserParser.SingleInsertQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiInsertQuery}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void enterMultiInsertQuery(SqlBaseParserParser.MultiInsertQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiInsertQuery}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void exitMultiInsertQuery(SqlBaseParserParser.MultiInsertQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code deleteFromTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void enterDeleteFromTable(SqlBaseParserParser.DeleteFromTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code deleteFromTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void exitDeleteFromTable(SqlBaseParserParser.DeleteFromTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code updateTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void enterUpdateTable(SqlBaseParserParser.UpdateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code updateTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void exitUpdateTable(SqlBaseParserParser.UpdateTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mergeIntoTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void enterMergeIntoTable(SqlBaseParserParser.MergeIntoTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mergeIntoTable}
	 * labeled alternative in {@link SqlBaseParserParser#dmlStatementNoWith}.
	 * @param ctx the parse tree
	 */
	void exitMergeIntoTable(SqlBaseParserParser.MergeIntoTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#queryOrganization}.
	 * @param ctx the parse tree
	 */
	void enterQueryOrganization(SqlBaseParserParser.QueryOrganizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#queryOrganization}.
	 * @param ctx the parse tree
	 */
	void exitQueryOrganization(SqlBaseParserParser.QueryOrganizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#multiInsertQueryBody}.
	 * @param ctx the parse tree
	 */
	void enterMultiInsertQueryBody(SqlBaseParserParser.MultiInsertQueryBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#multiInsertQueryBody}.
	 * @param ctx the parse tree
	 */
	void exitMultiInsertQueryBody(SqlBaseParserParser.MultiInsertQueryBodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryTermDefault}
	 * labeled alternative in {@link SqlBaseParserParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void enterQueryTermDefault(SqlBaseParserParser.QueryTermDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryTermDefault}
	 * labeled alternative in {@link SqlBaseParserParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void exitQueryTermDefault(SqlBaseParserParser.QueryTermDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setOperation}
	 * labeled alternative in {@link SqlBaseParserParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void enterSetOperation(SqlBaseParserParser.SetOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setOperation}
	 * labeled alternative in {@link SqlBaseParserParser#queryTerm}.
	 * @param ctx the parse tree
	 */
	void exitSetOperation(SqlBaseParserParser.SetOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryPrimaryDefault}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterQueryPrimaryDefault(SqlBaseParserParser.QueryPrimaryDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryPrimaryDefault}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitQueryPrimaryDefault(SqlBaseParserParser.QueryPrimaryDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fromStmt}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterFromStmt(SqlBaseParserParser.FromStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fromStmt}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitFromStmt(SqlBaseParserParser.FromStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code table}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterTable(SqlBaseParserParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code table}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitTable(SqlBaseParserParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inlineTableDefault1}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableDefault1(SqlBaseParserParser.InlineTableDefault1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code inlineTableDefault1}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableDefault1(SqlBaseParserParser.InlineTableDefault1Context ctx);
	/**
	 * Enter a parse tree produced by the {@code subquery}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(SqlBaseParserParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subquery}
	 * labeled alternative in {@link SqlBaseParserParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(SqlBaseParserParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#sortItem}.
	 * @param ctx the parse tree
	 */
	void enterSortItem(SqlBaseParserParser.SortItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#sortItem}.
	 * @param ctx the parse tree
	 */
	void exitSortItem(SqlBaseParserParser.SortItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#fromStatement}.
	 * @param ctx the parse tree
	 */
	void enterFromStatement(SqlBaseParserParser.FromStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#fromStatement}.
	 * @param ctx the parse tree
	 */
	void exitFromStatement(SqlBaseParserParser.FromStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#fromStatementBody}.
	 * @param ctx the parse tree
	 */
	void enterFromStatementBody(SqlBaseParserParser.FromStatementBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#fromStatementBody}.
	 * @param ctx the parse tree
	 */
	void exitFromStatementBody(SqlBaseParserParser.FromStatementBodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code transformQuerySpecification}
	 * labeled alternative in {@link SqlBaseParserParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterTransformQuerySpecification(SqlBaseParserParser.TransformQuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code transformQuerySpecification}
	 * labeled alternative in {@link SqlBaseParserParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitTransformQuerySpecification(SqlBaseParserParser.TransformQuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regularQuerySpecification}
	 * labeled alternative in {@link SqlBaseParserParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterRegularQuerySpecification(SqlBaseParserParser.RegularQuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regularQuerySpecification}
	 * labeled alternative in {@link SqlBaseParserParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitRegularQuerySpecification(SqlBaseParserParser.RegularQuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#transformClause}.
	 * @param ctx the parse tree
	 */
	void enterTransformClause(SqlBaseParserParser.TransformClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#transformClause}.
	 * @param ctx the parse tree
	 */
	void exitTransformClause(SqlBaseParserParser.TransformClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(SqlBaseParserParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(SqlBaseParserParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#setClause}.
	 * @param ctx the parse tree
	 */
	void enterSetClause(SqlBaseParserParser.SetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#setClause}.
	 * @param ctx the parse tree
	 */
	void exitSetClause(SqlBaseParserParser.SetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#matchedClause}.
	 * @param ctx the parse tree
	 */
	void enterMatchedClause(SqlBaseParserParser.MatchedClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#matchedClause}.
	 * @param ctx the parse tree
	 */
	void exitMatchedClause(SqlBaseParserParser.MatchedClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#notMatchedClause}.
	 * @param ctx the parse tree
	 */
	void enterNotMatchedClause(SqlBaseParserParser.NotMatchedClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#notMatchedClause}.
	 * @param ctx the parse tree
	 */
	void exitNotMatchedClause(SqlBaseParserParser.NotMatchedClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#matchedAction}.
	 * @param ctx the parse tree
	 */
	void enterMatchedAction(SqlBaseParserParser.MatchedActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#matchedAction}.
	 * @param ctx the parse tree
	 */
	void exitMatchedAction(SqlBaseParserParser.MatchedActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#notMatchedAction}.
	 * @param ctx the parse tree
	 */
	void enterNotMatchedAction(SqlBaseParserParser.NotMatchedActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#notMatchedAction}.
	 * @param ctx the parse tree
	 */
	void exitNotMatchedAction(SqlBaseParserParser.NotMatchedActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#assignmentList}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentList(SqlBaseParserParser.AssignmentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#assignmentList}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentList(SqlBaseParserParser.AssignmentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(SqlBaseParserParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(SqlBaseParserParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(SqlBaseParserParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(SqlBaseParserParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(SqlBaseParserParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(SqlBaseParserParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#hint}.
	 * @param ctx the parse tree
	 */
	void enterHint(SqlBaseParserParser.HintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#hint}.
	 * @param ctx the parse tree
	 */
	void exitHint(SqlBaseParserParser.HintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#hintStatement}.
	 * @param ctx the parse tree
	 */
	void enterHintStatement(SqlBaseParserParser.HintStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#hintStatement}.
	 * @param ctx the parse tree
	 */
	void exitHintStatement(SqlBaseParserParser.HintStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(SqlBaseParserParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(SqlBaseParserParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#temporalClause}.
	 * @param ctx the parse tree
	 */
	void enterTemporalClause(SqlBaseParserParser.TemporalClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#temporalClause}.
	 * @param ctx the parse tree
	 */
	void exitTemporalClause(SqlBaseParserParser.TemporalClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#aggregationClause}.
	 * @param ctx the parse tree
	 */
	void enterAggregationClause(SqlBaseParserParser.AggregationClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#aggregationClause}.
	 * @param ctx the parse tree
	 */
	void exitAggregationClause(SqlBaseParserParser.AggregationClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(SqlBaseParserParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(SqlBaseParserParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#groupingAnalytics}.
	 * @param ctx the parse tree
	 */
	void enterGroupingAnalytics(SqlBaseParserParser.GroupingAnalyticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#groupingAnalytics}.
	 * @param ctx the parse tree
	 */
	void exitGroupingAnalytics(SqlBaseParserParser.GroupingAnalyticsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#groupingElement}.
	 * @param ctx the parse tree
	 */
	void enterGroupingElement(SqlBaseParserParser.GroupingElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#groupingElement}.
	 * @param ctx the parse tree
	 */
	void exitGroupingElement(SqlBaseParserParser.GroupingElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#groupingSet}.
	 * @param ctx the parse tree
	 */
	void enterGroupingSet(SqlBaseParserParser.GroupingSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#groupingSet}.
	 * @param ctx the parse tree
	 */
	void exitGroupingSet(SqlBaseParserParser.GroupingSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#pivotClause}.
	 * @param ctx the parse tree
	 */
	void enterPivotClause(SqlBaseParserParser.PivotClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#pivotClause}.
	 * @param ctx the parse tree
	 */
	void exitPivotClause(SqlBaseParserParser.PivotClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#pivotColumn}.
	 * @param ctx the parse tree
	 */
	void enterPivotColumn(SqlBaseParserParser.PivotColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#pivotColumn}.
	 * @param ctx the parse tree
	 */
	void exitPivotColumn(SqlBaseParserParser.PivotColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#pivotValue}.
	 * @param ctx the parse tree
	 */
	void enterPivotValue(SqlBaseParserParser.PivotValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#pivotValue}.
	 * @param ctx the parse tree
	 */
	void exitPivotValue(SqlBaseParserParser.PivotValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#lateralView}.
	 * @param ctx the parse tree
	 */
	void enterLateralView(SqlBaseParserParser.LateralViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#lateralView}.
	 * @param ctx the parse tree
	 */
	void exitLateralView(SqlBaseParserParser.LateralViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#setQuantifier}.
	 * @param ctx the parse tree
	 */
	void enterSetQuantifier(SqlBaseParserParser.SetQuantifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#setQuantifier}.
	 * @param ctx the parse tree
	 */
	void exitSetQuantifier(SqlBaseParserParser.SetQuantifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(SqlBaseParserParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(SqlBaseParserParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#joinRelation}.
	 * @param ctx the parse tree
	 */
	void enterJoinRelation(SqlBaseParserParser.JoinRelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#joinRelation}.
	 * @param ctx the parse tree
	 */
	void exitJoinRelation(SqlBaseParserParser.JoinRelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterJoinType(SqlBaseParserParser.JoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitJoinType(SqlBaseParserParser.JoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#joinCriteria}.
	 * @param ctx the parse tree
	 */
	void enterJoinCriteria(SqlBaseParserParser.JoinCriteriaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#joinCriteria}.
	 * @param ctx the parse tree
	 */
	void exitJoinCriteria(SqlBaseParserParser.JoinCriteriaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#sample}.
	 * @param ctx the parse tree
	 */
	void enterSample(SqlBaseParserParser.SampleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#sample}.
	 * @param ctx the parse tree
	 */
	void exitSample(SqlBaseParserParser.SampleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByPercentile}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByPercentile(SqlBaseParserParser.SampleByPercentileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByPercentile}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByPercentile(SqlBaseParserParser.SampleByPercentileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByRows}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByRows(SqlBaseParserParser.SampleByRowsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByRows}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByRows(SqlBaseParserParser.SampleByRowsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByBucket}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByBucket(SqlBaseParserParser.SampleByBucketContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByBucket}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByBucket(SqlBaseParserParser.SampleByBucketContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sampleByBytes}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void enterSampleByBytes(SqlBaseParserParser.SampleByBytesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sampleByBytes}
	 * labeled alternative in {@link SqlBaseParserParser#sampleMethod}.
	 * @param ctx the parse tree
	 */
	void exitSampleByBytes(SqlBaseParserParser.SampleByBytesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(SqlBaseParserParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(SqlBaseParserParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#identifierSeq}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierSeq(SqlBaseParserParser.IdentifierSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#identifierSeq}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierSeq(SqlBaseParserParser.IdentifierSeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#orderedIdentifierList}.
	 * @param ctx the parse tree
	 */
	void enterOrderedIdentifierList(SqlBaseParserParser.OrderedIdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#orderedIdentifierList}.
	 * @param ctx the parse tree
	 */
	void exitOrderedIdentifierList(SqlBaseParserParser.OrderedIdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#orderedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterOrderedIdentifier(SqlBaseParserParser.OrderedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#orderedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitOrderedIdentifier(SqlBaseParserParser.OrderedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#identifierCommentList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierCommentList(SqlBaseParserParser.IdentifierCommentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#identifierCommentList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierCommentList(SqlBaseParserParser.IdentifierCommentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#identifierComment}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierComment(SqlBaseParserParser.IdentifierCommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#identifierComment}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierComment(SqlBaseParserParser.IdentifierCommentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableName}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterTableName(SqlBaseParserParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableName}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitTableName(SqlBaseParserParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code aliasedQuery}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterAliasedQuery(SqlBaseParserParser.AliasedQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code aliasedQuery}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitAliasedQuery(SqlBaseParserParser.AliasedQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code aliasedRelation}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterAliasedRelation(SqlBaseParserParser.AliasedRelationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code aliasedRelation}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitAliasedRelation(SqlBaseParserParser.AliasedRelationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inlineTableDefault2}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableDefault2(SqlBaseParserParser.InlineTableDefault2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code inlineTableDefault2}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableDefault2(SqlBaseParserParser.InlineTableDefault2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code tableValuedFunction}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void enterTableValuedFunction(SqlBaseParserParser.TableValuedFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableValuedFunction}
	 * labeled alternative in {@link SqlBaseParserParser#relationPrimary}.
	 * @param ctx the parse tree
	 */
	void exitTableValuedFunction(SqlBaseParserParser.TableValuedFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#inlineTable}.
	 * @param ctx the parse tree
	 */
	void enterInlineTable(SqlBaseParserParser.InlineTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#inlineTable}.
	 * @param ctx the parse tree
	 */
	void exitInlineTable(SqlBaseParserParser.InlineTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTable(SqlBaseParserParser.FunctionTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTable(SqlBaseParserParser.FunctionTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#tableAlias}.
	 * @param ctx the parse tree
	 */
	void enterTableAlias(SqlBaseParserParser.TableAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#tableAlias}.
	 * @param ctx the parse tree
	 */
	void exitTableAlias(SqlBaseParserParser.TableAliasContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rowFormatSerde}
	 * labeled alternative in {@link SqlBaseParserParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void enterRowFormatSerde(SqlBaseParserParser.RowFormatSerdeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rowFormatSerde}
	 * labeled alternative in {@link SqlBaseParserParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void exitRowFormatSerde(SqlBaseParserParser.RowFormatSerdeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rowFormatDelimited}
	 * labeled alternative in {@link SqlBaseParserParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void enterRowFormatDelimited(SqlBaseParserParser.RowFormatDelimitedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rowFormatDelimited}
	 * labeled alternative in {@link SqlBaseParserParser#rowFormat}.
	 * @param ctx the parse tree
	 */
	void exitRowFormatDelimited(SqlBaseParserParser.RowFormatDelimitedContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierList}.
	 * @param ctx the parse tree
	 */
	void enterMultipartIdentifierList(SqlBaseParserParser.MultipartIdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierList}.
	 * @param ctx the parse tree
	 */
	void exitMultipartIdentifierList(SqlBaseParserParser.MultipartIdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterMultipartIdentifier(SqlBaseParserParser.MultipartIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitMultipartIdentifier(SqlBaseParserParser.MultipartIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierPropertyList}.
	 * @param ctx the parse tree
	 */
	void enterMultipartIdentifierPropertyList(SqlBaseParserParser.MultipartIdentifierPropertyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierPropertyList}.
	 * @param ctx the parse tree
	 */
	void exitMultipartIdentifierPropertyList(SqlBaseParserParser.MultipartIdentifierPropertyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierProperty}.
	 * @param ctx the parse tree
	 */
	void enterMultipartIdentifierProperty(SqlBaseParserParser.MultipartIdentifierPropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#multipartIdentifierProperty}.
	 * @param ctx the parse tree
	 */
	void exitMultipartIdentifierProperty(SqlBaseParserParser.MultipartIdentifierPropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#tableIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterTableIdentifier(SqlBaseParserParser.TableIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#tableIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitTableIdentifier(SqlBaseParserParser.TableIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#functionIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterFunctionIdentifier(SqlBaseParserParser.FunctionIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#functionIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitFunctionIdentifier(SqlBaseParserParser.FunctionIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#namedExpression}.
	 * @param ctx the parse tree
	 */
	void enterNamedExpression(SqlBaseParserParser.NamedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#namedExpression}.
	 * @param ctx the parse tree
	 */
	void exitNamedExpression(SqlBaseParserParser.NamedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#namedExpressionSeq}.
	 * @param ctx the parse tree
	 */
	void enterNamedExpressionSeq(SqlBaseParserParser.NamedExpressionSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#namedExpressionSeq}.
	 * @param ctx the parse tree
	 */
	void exitNamedExpressionSeq(SqlBaseParserParser.NamedExpressionSeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#partitionFieldList}.
	 * @param ctx the parse tree
	 */
	void enterPartitionFieldList(SqlBaseParserParser.PartitionFieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#partitionFieldList}.
	 * @param ctx the parse tree
	 */
	void exitPartitionFieldList(SqlBaseParserParser.PartitionFieldListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionTransform}
	 * labeled alternative in {@link SqlBaseParserParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void enterPartitionTransform(SqlBaseParserParser.PartitionTransformContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionTransform}
	 * labeled alternative in {@link SqlBaseParserParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void exitPartitionTransform(SqlBaseParserParser.PartitionTransformContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionColumn}
	 * labeled alternative in {@link SqlBaseParserParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void enterPartitionColumn(SqlBaseParserParser.PartitionColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionColumn}
	 * labeled alternative in {@link SqlBaseParserParser#partitionField}.
	 * @param ctx the parse tree
	 */
	void exitPartitionColumn(SqlBaseParserParser.PartitionColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identityTransform}
	 * labeled alternative in {@link SqlBaseParserParser#transform}.
	 * @param ctx the parse tree
	 */
	void enterIdentityTransform(SqlBaseParserParser.IdentityTransformContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identityTransform}
	 * labeled alternative in {@link SqlBaseParserParser#transform}.
	 * @param ctx the parse tree
	 */
	void exitIdentityTransform(SqlBaseParserParser.IdentityTransformContext ctx);
	/**
	 * Enter a parse tree produced by the {@code applyTransform}
	 * labeled alternative in {@link SqlBaseParserParser#transform}.
	 * @param ctx the parse tree
	 */
	void enterApplyTransform(SqlBaseParserParser.ApplyTransformContext ctx);
	/**
	 * Exit a parse tree produced by the {@code applyTransform}
	 * labeled alternative in {@link SqlBaseParserParser#transform}.
	 * @param ctx the parse tree
	 */
	void exitApplyTransform(SqlBaseParserParser.ApplyTransformContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#transformArgument}.
	 * @param ctx the parse tree
	 */
	void enterTransformArgument(SqlBaseParserParser.TransformArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#transformArgument}.
	 * @param ctx the parse tree
	 */
	void exitTransformArgument(SqlBaseParserParser.TransformArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SqlBaseParserParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SqlBaseParserParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#expressionSeq}.
	 * @param ctx the parse tree
	 */
	void enterExpressionSeq(SqlBaseParserParser.ExpressionSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#expressionSeq}.
	 * @param ctx the parse tree
	 */
	void exitExpressionSeq(SqlBaseParserParser.ExpressionSeqContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalNot(SqlBaseParserParser.LogicalNotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalNot(SqlBaseParserParser.LogicalNotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code predicated}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterPredicated(SqlBaseParserParser.PredicatedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code predicated}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitPredicated(SqlBaseParserParser.PredicatedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exists}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterExists(SqlBaseParserParser.ExistsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exists}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitExists(SqlBaseParserParser.ExistsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalBinary}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalBinary(SqlBaseParserParser.LogicalBinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalBinary}
	 * labeled alternative in {@link SqlBaseParserParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalBinary(SqlBaseParserParser.LogicalBinaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(SqlBaseParserParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(SqlBaseParserParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code valueExpressionDefault}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterValueExpressionDefault(SqlBaseParserParser.ValueExpressionDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code valueExpressionDefault}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitValueExpressionDefault(SqlBaseParserParser.ValueExpressionDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparison}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparison(SqlBaseParserParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparison}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparison(SqlBaseParserParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arithmeticBinary}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticBinary(SqlBaseParserParser.ArithmeticBinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arithmeticBinary}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticBinary(SqlBaseParserParser.ArithmeticBinaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arithmeticUnary}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticUnary(SqlBaseParserParser.ArithmeticUnaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arithmeticUnary}
	 * labeled alternative in {@link SqlBaseParserParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticUnary(SqlBaseParserParser.ArithmeticUnaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#datetimeUnit}.
	 * @param ctx the parse tree
	 */
	void enterDatetimeUnit(SqlBaseParserParser.DatetimeUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#datetimeUnit}.
	 * @param ctx the parse tree
	 */
	void exitDatetimeUnit(SqlBaseParserParser.DatetimeUnitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code struct}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterStruct(SqlBaseParserParser.StructContext ctx);
	/**
	 * Exit a parse tree produced by the {@code struct}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitStruct(SqlBaseParserParser.StructContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dereference}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterDereference(SqlBaseParserParser.DereferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dereference}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitDereference(SqlBaseParserParser.DereferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timestampadd}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimestampadd(SqlBaseParserParser.TimestampaddContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timestampadd}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimestampadd(SqlBaseParserParser.TimestampaddContext ctx);
	/**
	 * Enter a parse tree produced by the {@code substring}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubstring(SqlBaseParserParser.SubstringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code substring}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubstring(SqlBaseParserParser.SubstringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code cast}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterCast(SqlBaseParserParser.CastContext ctx);
	/**
	 * Exit a parse tree produced by the {@code cast}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitCast(SqlBaseParserParser.CastContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lambda}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambda(SqlBaseParserParser.LambdaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lambda}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambda(SqlBaseParserParser.LambdaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedExpression(SqlBaseParserParser.ParenthesizedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedExpression(SqlBaseParserParser.ParenthesizedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trim}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTrim(SqlBaseParserParser.TrimContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trim}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTrim(SqlBaseParserParser.TrimContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleCase}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleCase(SqlBaseParserParser.SimpleCaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleCase}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleCase(SqlBaseParserParser.SimpleCaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code currentLike}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterCurrentLike(SqlBaseParserParser.CurrentLikeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code currentLike}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitCurrentLike(SqlBaseParserParser.CurrentLikeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code columnReference}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterColumnReference(SqlBaseParserParser.ColumnReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code columnReference}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitColumnReference(SqlBaseParserParser.ColumnReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rowConstructor}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterRowConstructor(SqlBaseParserParser.RowConstructorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rowConstructor}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitRowConstructor(SqlBaseParserParser.RowConstructorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code last}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterLast(SqlBaseParserParser.LastContext ctx);
	/**
	 * Exit a parse tree produced by the {@code last}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitLast(SqlBaseParserParser.LastContext ctx);
	/**
	 * Enter a parse tree produced by the {@code star}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterStar(SqlBaseParserParser.StarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code star}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitStar(SqlBaseParserParser.StarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code overlay}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterOverlay(SqlBaseParserParser.OverlayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code overlay}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitOverlay(SqlBaseParserParser.OverlayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubscript(SqlBaseParserParser.SubscriptContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubscript(SqlBaseParserParser.SubscriptContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timestampdiff}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimestampdiff(SqlBaseParserParser.TimestampdiffContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timestampdiff}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimestampdiff(SqlBaseParserParser.TimestampdiffContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryExpression(SqlBaseParserParser.SubqueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryExpression(SqlBaseParserParser.SubqueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constantDefault}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantDefault(SqlBaseParserParser.ConstantDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constantDefault}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantDefault(SqlBaseParserParser.ConstantDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code extract}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterExtract(SqlBaseParserParser.ExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code extract}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitExtract(SqlBaseParserParser.ExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code percentile}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPercentile(SqlBaseParserParser.PercentileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code percentile}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPercentile(SqlBaseParserParser.PercentileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(SqlBaseParserParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(SqlBaseParserParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code searchedCase}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSearchedCase(SqlBaseParserParser.SearchedCaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code searchedCase}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSearchedCase(SqlBaseParserParser.SearchedCaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code position}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPosition(SqlBaseParserParser.PositionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code position}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPosition(SqlBaseParserParser.PositionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code first}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterFirst(SqlBaseParserParser.FirstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code first}
	 * labeled alternative in {@link SqlBaseParserParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitFirst(SqlBaseParserParser.FirstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(SqlBaseParserParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(SqlBaseParserParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intervalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterIntervalLiteral(SqlBaseParserParser.IntervalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intervalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitIntervalLiteral(SqlBaseParserParser.IntervalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeConstructor}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterTypeConstructor(SqlBaseParserParser.TypeConstructorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeConstructor}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitTypeConstructor(SqlBaseParserParser.TypeConstructorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteral(SqlBaseParserParser.NumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteral(SqlBaseParserParser.NumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(SqlBaseParserParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(SqlBaseParserParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(SqlBaseParserParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(SqlBaseParserParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(SqlBaseParserParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(SqlBaseParserParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#arithmeticOperator}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticOperator(SqlBaseParserParser.ArithmeticOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#arithmeticOperator}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticOperator(SqlBaseParserParser.ArithmeticOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#predicateOperator}.
	 * @param ctx the parse tree
	 */
	void enterPredicateOperator(SqlBaseParserParser.PredicateOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#predicateOperator}.
	 * @param ctx the parse tree
	 */
	void exitPredicateOperator(SqlBaseParserParser.PredicateOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#booleanValue}.
	 * @param ctx the parse tree
	 */
	void enterBooleanValue(SqlBaseParserParser.BooleanValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#booleanValue}.
	 * @param ctx the parse tree
	 */
	void exitBooleanValue(SqlBaseParserParser.BooleanValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#interval}.
	 * @param ctx the parse tree
	 */
	void enterInterval(SqlBaseParserParser.IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#interval}.
	 * @param ctx the parse tree
	 */
	void exitInterval(SqlBaseParserParser.IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#errorCapturingMultiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingMultiUnitsInterval(SqlBaseParserParser.ErrorCapturingMultiUnitsIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#errorCapturingMultiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingMultiUnitsInterval(SqlBaseParserParser.ErrorCapturingMultiUnitsIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#multiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void enterMultiUnitsInterval(SqlBaseParserParser.MultiUnitsIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#multiUnitsInterval}.
	 * @param ctx the parse tree
	 */
	void exitMultiUnitsInterval(SqlBaseParserParser.MultiUnitsIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#errorCapturingUnitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingUnitToUnitInterval(SqlBaseParserParser.ErrorCapturingUnitToUnitIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#errorCapturingUnitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingUnitToUnitInterval(SqlBaseParserParser.ErrorCapturingUnitToUnitIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#unitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void enterUnitToUnitInterval(SqlBaseParserParser.UnitToUnitIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#unitToUnitInterval}.
	 * @param ctx the parse tree
	 */
	void exitUnitToUnitInterval(SqlBaseParserParser.UnitToUnitIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void enterIntervalValue(SqlBaseParserParser.IntervalValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void exitIntervalValue(SqlBaseParserParser.IntervalValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#colPosition}.
	 * @param ctx the parse tree
	 */
	void enterColPosition(SqlBaseParserParser.ColPositionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#colPosition}.
	 * @param ctx the parse tree
	 */
	void exitColPosition(SqlBaseParserParser.ColPositionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterComplexDataType(SqlBaseParserParser.ComplexDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitComplexDataType(SqlBaseParserParser.ComplexDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterYearMonthIntervalDataType(SqlBaseParserParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitYearMonthIntervalDataType(SqlBaseParserParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDayTimeIntervalDataType(SqlBaseParserParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDayTimeIntervalDataType(SqlBaseParserParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveDataType(SqlBaseParserParser.PrimitiveDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link SqlBaseParserParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveDataType(SqlBaseParserParser.PrimitiveDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#qualifiedColTypeWithPositionList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedColTypeWithPositionList(SqlBaseParserParser.QualifiedColTypeWithPositionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#qualifiedColTypeWithPositionList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedColTypeWithPositionList(SqlBaseParserParser.QualifiedColTypeWithPositionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#qualifiedColTypeWithPosition}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedColTypeWithPosition(SqlBaseParserParser.QualifiedColTypeWithPositionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#qualifiedColTypeWithPosition}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedColTypeWithPosition(SqlBaseParserParser.QualifiedColTypeWithPositionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#colTypeList}.
	 * @param ctx the parse tree
	 */
	void enterColTypeList(SqlBaseParserParser.ColTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#colTypeList}.
	 * @param ctx the parse tree
	 */
	void exitColTypeList(SqlBaseParserParser.ColTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#colType}.
	 * @param ctx the parse tree
	 */
	void enterColType(SqlBaseParserParser.ColTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#colType}.
	 * @param ctx the parse tree
	 */
	void exitColType(SqlBaseParserParser.ColTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#complexColTypeList}.
	 * @param ctx the parse tree
	 */
	void enterComplexColTypeList(SqlBaseParserParser.ComplexColTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#complexColTypeList}.
	 * @param ctx the parse tree
	 */
	void exitComplexColTypeList(SqlBaseParserParser.ComplexColTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#complexColType}.
	 * @param ctx the parse tree
	 */
	void enterComplexColType(SqlBaseParserParser.ComplexColTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#complexColType}.
	 * @param ctx the parse tree
	 */
	void exitComplexColType(SqlBaseParserParser.ComplexColTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(SqlBaseParserParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(SqlBaseParserParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowClause(SqlBaseParserParser.WindowClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowClause(SqlBaseParserParser.WindowClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#namedWindow}.
	 * @param ctx the parse tree
	 */
	void enterNamedWindow(SqlBaseParserParser.NamedWindowContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#namedWindow}.
	 * @param ctx the parse tree
	 */
	void exitNamedWindow(SqlBaseParserParser.NamedWindowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code windowRef}
	 * labeled alternative in {@link SqlBaseParserParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void enterWindowRef(SqlBaseParserParser.WindowRefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code windowRef}
	 * labeled alternative in {@link SqlBaseParserParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void exitWindowRef(SqlBaseParserParser.WindowRefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code windowDef}
	 * labeled alternative in {@link SqlBaseParserParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void enterWindowDef(SqlBaseParserParser.WindowDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code windowDef}
	 * labeled alternative in {@link SqlBaseParserParser#windowSpec}.
	 * @param ctx the parse tree
	 */
	void exitWindowDef(SqlBaseParserParser.WindowDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#windowFrame}.
	 * @param ctx the parse tree
	 */
	void enterWindowFrame(SqlBaseParserParser.WindowFrameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#windowFrame}.
	 * @param ctx the parse tree
	 */
	void exitWindowFrame(SqlBaseParserParser.WindowFrameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void enterFrameBound(SqlBaseParserParser.FrameBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void exitFrameBound(SqlBaseParserParser.FrameBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedNameList(SqlBaseParserParser.QualifiedNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedNameList(SqlBaseParserParser.QualifiedNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(SqlBaseParserParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(SqlBaseParserParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(SqlBaseParserParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(SqlBaseParserParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingIdentifier(SqlBaseParserParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingIdentifier(SqlBaseParserParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link SqlBaseParserParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void enterErrorIdent(SqlBaseParserParser.ErrorIdentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link SqlBaseParserParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void exitErrorIdent(SqlBaseParserParser.ErrorIdentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link SqlBaseParserParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void enterRealIdent(SqlBaseParserParser.RealIdentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link SqlBaseParserParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void exitRealIdent(SqlBaseParserParser.RealIdentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(SqlBaseParserParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(SqlBaseParserParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link SqlBaseParserParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterUnquotedIdentifier(SqlBaseParserParser.UnquotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link SqlBaseParserParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitUnquotedIdentifier(SqlBaseParserParser.UnquotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link SqlBaseParserParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifierAlternative(SqlBaseParserParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link SqlBaseParserParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifierAlternative(SqlBaseParserParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifier(SqlBaseParserParser.QuotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifier(SqlBaseParserParser.QuotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exponentLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterExponentLiteral(SqlBaseParserParser.ExponentLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exponentLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitExponentLiteral(SqlBaseParserParser.ExponentLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterDecimalLiteral(SqlBaseParserParser.DecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitDecimalLiteral(SqlBaseParserParser.DecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code legacyDecimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterLegacyDecimalLiteral(SqlBaseParserParser.LegacyDecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code legacyDecimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitLegacyDecimalLiteral(SqlBaseParserParser.LegacyDecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(SqlBaseParserParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(SqlBaseParserParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bigIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterBigIntLiteral(SqlBaseParserParser.BigIntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bigIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitBigIntLiteral(SqlBaseParserParser.BigIntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code smallIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterSmallIntLiteral(SqlBaseParserParser.SmallIntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code smallIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitSmallIntLiteral(SqlBaseParserParser.SmallIntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tinyIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterTinyIntLiteral(SqlBaseParserParser.TinyIntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tinyIntLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitTinyIntLiteral(SqlBaseParserParser.TinyIntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code doubleLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterDoubleLiteral(SqlBaseParserParser.DoubleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code doubleLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitDoubleLiteral(SqlBaseParserParser.DoubleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(SqlBaseParserParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(SqlBaseParserParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bigDecimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterBigDecimalLiteral(SqlBaseParserParser.BigDecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bigDecimalLiteral}
	 * labeled alternative in {@link SqlBaseParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitBigDecimalLiteral(SqlBaseParserParser.BigDecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#alterColumnAction}.
	 * @param ctx the parse tree
	 */
	void enterAlterColumnAction(SqlBaseParserParser.AlterColumnActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#alterColumnAction}.
	 * @param ctx the parse tree
	 */
	void exitAlterColumnAction(SqlBaseParserParser.AlterColumnActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 */
	void enterAnsiNonReserved(SqlBaseParserParser.AnsiNonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 */
	void exitAnsiNonReserved(SqlBaseParserParser.AnsiNonReservedContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#strictNonReserved}.
	 * @param ctx the parse tree
	 */
	void enterStrictNonReserved(SqlBaseParserParser.StrictNonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#strictNonReserved}.
	 * @param ctx the parse tree
	 */
	void exitStrictNonReserved(SqlBaseParserParser.StrictNonReservedContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlBaseParserParser#nonReserved}.
	 * @param ctx the parse tree
	 */
	void enterNonReserved(SqlBaseParserParser.NonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlBaseParserParser#nonReserved}.
	 * @param ctx the parse tree
	 */
	void exitNonReserved(SqlBaseParserParser.NonReservedContext ctx);
}