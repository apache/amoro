// Generated from com/netease/arctic/spark/sql/parser/ArcticExtendSparkSql.g4 by ANTLR 4.8
package com.netease.arctic.spark.sql.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ArcticExtendSparkSqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		KEY=1, MIGRATE=2, SEMICOLON=3, LEFT_PAREN=4, RIGHT_PAREN=5, COMMA=6, DOT=7, 
		LEFT_BRACKET=8, RIGHT_BRACKET=9, ADD=10, AFTER=11, ALL=12, ALTER=13, ANALYZE=14, 
		AND=15, ANTI=16, ANY=17, ARCHIVE=18, ARRAY=19, AS=20, ASC=21, AT=22, AUTHORIZATION=23, 
		BETWEEN=24, BOTH=25, BUCKET=26, BUCKETS=27, BY=28, CACHE=29, CASCADE=30, 
		CASE=31, CAST=32, CATALOG=33, CATALOGS=34, CHANGE=35, CHECK=36, CLEAR=37, 
		CLUSTER=38, CLUSTERED=39, CODEGEN=40, COLLATE=41, COLLECTION=42, COLUMN=43, 
		COLUMNS=44, COMMENT=45, COMMIT=46, COMPACT=47, COMPACTIONS=48, COMPUTE=49, 
		CONCATENATE=50, CONSTRAINT=51, COST=52, CREATE=53, CROSS=54, CUBE=55, 
		CURRENT=56, CURRENT_DATE=57, CURRENT_TIME=58, CURRENT_TIMESTAMP=59, CURRENT_USER=60, 
		DAY=61, DAYOFYEAR=62, DATA=63, DATABASE=64, DATABASES=65, DATEADD=66, 
		DATEDIFF=67, DBPROPERTIES=68, DEFINED=69, DELETE=70, DELIMITED=71, DESC=72, 
		DESCRIBE=73, DFS=74, DIRECTORIES=75, DIRECTORY=76, DISTINCT=77, DISTRIBUTE=78, 
		DIV=79, DROP=80, ELSE=81, END=82, ESCAPE=83, ESCAPED=84, EXCEPT=85, EXCHANGE=86, 
		EXISTS=87, EXPLAIN=88, EXPORT=89, EXTENDED=90, EXTERNAL=91, EXTRACT=92, 
		FALSE=93, FETCH=94, FIELDS=95, FILTER=96, FILEFORMAT=97, FIRST=98, FOLLOWING=99, 
		FOR=100, FOREIGN=101, FORMAT=102, FORMATTED=103, FROM=104, FULL=105, FUNCTION=106, 
		FUNCTIONS=107, GLOBAL=108, GRANT=109, GROUP=110, GROUPING=111, HAVING=112, 
		HOUR=113, IF=114, IGNORE=115, IMPORT=116, IN=117, INDEX=118, INDEXES=119, 
		INNER=120, INPATH=121, INPUTFORMAT=122, INSERT=123, INTERSECT=124, INTERVAL=125, 
		INTO=126, IS=127, ITEMS=128, JOIN=129, KEYS=130, LAST=131, LATERAL=132, 
		LAZY=133, LEADING=134, LEFT=135, LIKE=136, ILIKE=137, LIMIT=138, LINES=139, 
		LIST=140, LOAD=141, LOCAL=142, LOCATION=143, LOCK=144, LOCKS=145, LOGICAL=146, 
		MACRO=147, MAP=148, MATCHED=149, MERGE=150, MICROSECOND=151, MILLISECOND=152, 
		MINUTE=153, MONTH=154, MSCK=155, NAMESPACE=156, NAMESPACES=157, NATURAL=158, 
		NO=159, NOT=160, NULL=161, NULLS=162, OF=163, ON=164, ONLY=165, OPTION=166, 
		OPTIONS=167, OR=168, ORDER=169, OUT=170, OUTER=171, OUTPUTFORMAT=172, 
		OVER=173, OVERLAPS=174, OVERLAY=175, OVERWRITE=176, PARTITION=177, PARTITIONED=178, 
		PARTITIONS=179, PERCENTILE_CONT=180, PERCENTILE_DISC=181, PERCENTLIT=182, 
		PIVOT=183, PLACING=184, POSITION=185, PRECEDING=186, PRIMARY=187, PRINCIPALS=188, 
		PROPERTIES=189, PURGE=190, QUARTER=191, QUERY=192, RANGE=193, RECORDREADER=194, 
		RECORDWRITER=195, RECOVER=196, REDUCE=197, REFERENCES=198, REFRESH=199, 
		RENAME=200, REPAIR=201, REPEATABLE=202, REPLACE=203, RESET=204, RESPECT=205, 
		RESTRICT=206, REVOKE=207, RIGHT=208, RLIKE=209, ROLE=210, ROLES=211, ROLLBACK=212, 
		ROLLUP=213, ROW=214, ROWS=215, SECOND=216, SCHEMA=217, SCHEMAS=218, SELECT=219, 
		SEMI=220, SEPARATED=221, SERDE=222, SERDEPROPERTIES=223, SESSION_USER=224, 
		SET=225, SETMINUS=226, SETS=227, SHOW=228, SKEWED=229, SOME=230, SORT=231, 
		SORTED=232, START=233, STATISTICS=234, STORED=235, STRATIFY=236, STRUCT=237, 
		SUBSTR=238, SUBSTRING=239, SYNC=240, SYSTEM_TIME=241, SYSTEM_VERSION=242, 
		TABLE=243, TABLES=244, TABLESAMPLE=245, TBLPROPERTIES=246, TEMPORARY=247, 
		TERMINATED=248, THEN=249, TIME=250, TIMESTAMP=251, TIMESTAMPADD=252, TIMESTAMPDIFF=253, 
		TO=254, TOUCH=255, TRAILING=256, TRANSACTION=257, TRANSACTIONS=258, TRANSFORM=259, 
		TRIM=260, TRUE=261, TRUNCATE=262, TRY_CAST=263, TYPE=264, UNARCHIVE=265, 
		UNBOUNDED=266, UNCACHE=267, UNION=268, UNIQUE=269, UNKNOWN=270, UNLOCK=271, 
		UNSET=272, UPDATE=273, USE=274, USER=275, USING=276, VALUES=277, VERSION=278, 
		VIEW=279, VIEWS=280, WEEK=281, WHEN=282, WHERE=283, WINDOW=284, WITH=285, 
		WITHIN=286, YEAR=287, ZONE=288, EQ=289, NSEQ=290, NEQ=291, NEQJ=292, LT=293, 
		LTE=294, GT=295, GTE=296, PLUS=297, MINUS=298, ASTERISK=299, SLASH=300, 
		PERCENT=301, TILDE=302, AMPERSAND=303, PIPE=304, CONCAT_PIPE=305, HAT=306, 
		COLON=307, ARROW=308, HENT_START=309, HENT_END=310, STRING=311, BIGINT_LITERAL=312, 
		SMALLINT_LITERAL=313, TINYINT_LITERAL=314, INTEGER_VALUE=315, EXPONENT_VALUE=316, 
		DECIMAL_VALUE=317, FLOAT_LITERAL=318, DOUBLE_LITERAL=319, BIGDECIMAL_LITERAL=320, 
		IDENTIFIER=321, BACKQUOTED_IDENTIFIER=322, SIMPLE_COMMENT=323, BRACKETED_COMMENT=324, 
		WS=325, UNRECOGNIZED=326;
	public static final int
		RULE_arcticCommand = 0, RULE_arcticStatement = 1, RULE_createTableWithPrimaryKey = 2, 
		RULE_primarySpec = 3, RULE_colListAndPk = 4, RULE_singleStatement = 5, 
		RULE_singleExpression = 6, RULE_singleTableIdentifier = 7, RULE_singleMultipartIdentifier = 8, 
		RULE_singleFunctionIdentifier = 9, RULE_singleDataType = 10, RULE_singleTableSchema = 11, 
		RULE_statement = 12, RULE_configKey = 13, RULE_configValue = 14, RULE_unsupportedHiveNativeCommands = 15, 
		RULE_createTableHeader = 16, RULE_replaceTableHeader = 17, RULE_bucketSpec = 18, 
		RULE_skewSpec = 19, RULE_locationSpec = 20, RULE_commentSpec = 21, RULE_query = 22, 
		RULE_insertInto = 23, RULE_partitionSpecLocation = 24, RULE_partitionSpec = 25, 
		RULE_partitionVal = 26, RULE_namespace = 27, RULE_namespaces = 28, RULE_describeFuncName = 29, 
		RULE_describeColName = 30, RULE_ctes = 31, RULE_namedQuery = 32, RULE_tableProvider = 33, 
		RULE_createTableClauses = 34, RULE_propertyList = 35, RULE_property = 36, 
		RULE_propertyKey = 37, RULE_propertyValue = 38, RULE_constantList = 39, 
		RULE_nestedConstantList = 40, RULE_createFileFormat = 41, RULE_fileFormat = 42, 
		RULE_storageHandler = 43, RULE_resource = 44, RULE_dmlStatementNoWith = 45, 
		RULE_queryOrganization = 46, RULE_multiInsertQueryBody = 47, RULE_queryTerm = 48, 
		RULE_queryPrimary = 49, RULE_sortItem = 50, RULE_fromStatement = 51, RULE_fromStatementBody = 52, 
		RULE_querySpecification = 53, RULE_transformClause = 54, RULE_selectClause = 55, 
		RULE_setClause = 56, RULE_matchedClause = 57, RULE_notMatchedClause = 58, 
		RULE_matchedAction = 59, RULE_notMatchedAction = 60, RULE_assignmentList = 61, 
		RULE_assignment = 62, RULE_whereClause = 63, RULE_havingClause = 64, RULE_hint = 65, 
		RULE_hintStatement = 66, RULE_fromClause = 67, RULE_temporalClause = 68, 
		RULE_aggregationClause = 69, RULE_groupByClause = 70, RULE_groupingAnalytics = 71, 
		RULE_groupingElement = 72, RULE_groupingSet = 73, RULE_pivotClause = 74, 
		RULE_pivotColumn = 75, RULE_pivotValue = 76, RULE_lateralView = 77, RULE_setQuantifier = 78, 
		RULE_relation = 79, RULE_joinRelation = 80, RULE_joinType = 81, RULE_joinCriteria = 82, 
		RULE_sample = 83, RULE_sampleMethod = 84, RULE_identifierList = 85, RULE_identifierSeq = 86, 
		RULE_orderedIdentifierList = 87, RULE_orderedIdentifier = 88, RULE_identifierCommentList = 89, 
		RULE_identifierComment = 90, RULE_relationPrimary = 91, RULE_inlineTable = 92, 
		RULE_functionTable = 93, RULE_tableAlias = 94, RULE_rowFormat = 95, RULE_multipartIdentifierList = 96, 
		RULE_multipartIdentifier = 97, RULE_multipartIdentifierPropertyList = 98, 
		RULE_multipartIdentifierProperty = 99, RULE_tableIdentifier = 100, RULE_functionIdentifier = 101, 
		RULE_namedExpression = 102, RULE_namedExpressionSeq = 103, RULE_partitionFieldList = 104, 
		RULE_partitionField = 105, RULE_transform = 106, RULE_transformArgument = 107, 
		RULE_expression = 108, RULE_expressionSeq = 109, RULE_booleanExpression = 110, 
		RULE_predicate = 111, RULE_valueExpression = 112, RULE_datetimeUnit = 113, 
		RULE_primaryExpression = 114, RULE_constant = 115, RULE_comparisonOperator = 116, 
		RULE_arithmeticOperator = 117, RULE_predicateOperator = 118, RULE_booleanValue = 119, 
		RULE_interval = 120, RULE_errorCapturingMultiUnitsInterval = 121, RULE_multiUnitsInterval = 122, 
		RULE_errorCapturingUnitToUnitInterval = 123, RULE_unitToUnitInterval = 124, 
		RULE_intervalValue = 125, RULE_colPosition = 126, RULE_dataType = 127, 
		RULE_qualifiedColTypeWithPositionList = 128, RULE_qualifiedColTypeWithPosition = 129, 
		RULE_colTypeList = 130, RULE_colType = 131, RULE_complexColTypeList = 132, 
		RULE_complexColType = 133, RULE_whenClause = 134, RULE_windowClause = 135, 
		RULE_namedWindow = 136, RULE_windowSpec = 137, RULE_windowFrame = 138, 
		RULE_frameBound = 139, RULE_qualifiedNameList = 140, RULE_functionName = 141, 
		RULE_qualifiedName = 142, RULE_errorCapturingIdentifier = 143, RULE_errorCapturingIdentifierExtra = 144, 
		RULE_identifier = 145, RULE_strictIdentifier = 146, RULE_quotedIdentifier = 147, 
		RULE_number = 148, RULE_alterColumnAction = 149, RULE_ansiNonReserved = 150, 
		RULE_strictNonReserved = 151, RULE_nonReserved = 152;
	private static String[] makeRuleNames() {
		return new String[] {
			"arcticCommand", "arcticStatement", "createTableWithPrimaryKey", "primarySpec", 
			"colListAndPk", "singleStatement", "singleExpression", "singleTableIdentifier", 
			"singleMultipartIdentifier", "singleFunctionIdentifier", "singleDataType", 
			"singleTableSchema", "statement", "configKey", "configValue", "unsupportedHiveNativeCommands", 
			"createTableHeader", "replaceTableHeader", "bucketSpec", "skewSpec", 
			"locationSpec", "commentSpec", "query", "insertInto", "partitionSpecLocation", 
			"partitionSpec", "partitionVal", "namespace", "namespaces", "describeFuncName", 
			"describeColName", "ctes", "namedQuery", "tableProvider", "createTableClauses", 
			"propertyList", "property", "propertyKey", "propertyValue", "constantList", 
			"nestedConstantList", "createFileFormat", "fileFormat", "storageHandler", 
			"resource", "dmlStatementNoWith", "queryOrganization", "multiInsertQueryBody", 
			"queryTerm", "queryPrimary", "sortItem", "fromStatement", "fromStatementBody", 
			"querySpecification", "transformClause", "selectClause", "setClause", 
			"matchedClause", "notMatchedClause", "matchedAction", "notMatchedAction", 
			"assignmentList", "assignment", "whereClause", "havingClause", "hint", 
			"hintStatement", "fromClause", "temporalClause", "aggregationClause", 
			"groupByClause", "groupingAnalytics", "groupingElement", "groupingSet", 
			"pivotClause", "pivotColumn", "pivotValue", "lateralView", "setQuantifier", 
			"relation", "joinRelation", "joinType", "joinCriteria", "sample", "sampleMethod", 
			"identifierList", "identifierSeq", "orderedIdentifierList", "orderedIdentifier", 
			"identifierCommentList", "identifierComment", "relationPrimary", "inlineTable", 
			"functionTable", "tableAlias", "rowFormat", "multipartIdentifierList", 
			"multipartIdentifier", "multipartIdentifierPropertyList", "multipartIdentifierProperty", 
			"tableIdentifier", "functionIdentifier", "namedExpression", "namedExpressionSeq", 
			"partitionFieldList", "partitionField", "transform", "transformArgument", 
			"expression", "expressionSeq", "booleanExpression", "predicate", "valueExpression", 
			"datetimeUnit", "primaryExpression", "constant", "comparisonOperator", 
			"arithmeticOperator", "predicateOperator", "booleanValue", "interval", 
			"errorCapturingMultiUnitsInterval", "multiUnitsInterval", "errorCapturingUnitToUnitInterval", 
			"unitToUnitInterval", "intervalValue", "colPosition", "dataType", "qualifiedColTypeWithPositionList", 
			"qualifiedColTypeWithPosition", "colTypeList", "colType", "complexColTypeList", 
			"complexColType", "whenClause", "windowClause", "namedWindow", "windowSpec", 
			"windowFrame", "frameBound", "qualifiedNameList", "functionName", "qualifiedName", 
			"errorCapturingIdentifier", "errorCapturingIdentifierExtra", "identifier", 
			"strictIdentifier", "quotedIdentifier", "number", "alterColumnAction", 
			"ansiNonReserved", "strictNonReserved", "nonReserved"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'KEY'", "'MIGRATE'", "';'", "'('", "')'", "','", "'.'", "'['", 
			"']'", "'ADD'", "'AFTER'", "'ALL'", "'ALTER'", "'ANALYZE'", "'AND'", 
			"'ANTI'", "'ANY'", "'ARCHIVE'", "'ARRAY'", "'AS'", "'ASC'", "'AT'", "'AUTHORIZATION'", 
			"'BETWEEN'", "'BOTH'", "'BUCKET'", "'BUCKETS'", "'BY'", "'CACHE'", "'CASCADE'", 
			"'CASE'", "'CAST'", "'CATALOG'", "'CATALOGS'", "'CHANGE'", "'CHECK'", 
			"'CLEAR'", "'CLUSTER'", "'CLUSTERED'", "'CODEGEN'", "'COLLATE'", "'COLLECTION'", 
			"'COLUMN'", "'COLUMNS'", "'COMMENT'", "'COMMIT'", "'COMPACT'", "'COMPACTIONS'", 
			"'COMPUTE'", "'CONCATENATE'", "'CONSTRAINT'", "'COST'", "'CREATE'", "'CROSS'", 
			"'CUBE'", "'CURRENT'", "'CURRENT_DATE'", "'CURRENT_TIME'", "'CURRENT_TIMESTAMP'", 
			"'CURRENT_USER'", "'DAY'", "'DAYOFYEAR'", "'DATA'", "'DATABASE'", "'DATABASES'", 
			"'DATEADD'", "'DATEDIFF'", "'DBPROPERTIES'", "'DEFINED'", "'DELETE'", 
			"'DELIMITED'", "'DESC'", "'DESCRIBE'", "'DFS'", "'DIRECTORIES'", "'DIRECTORY'", 
			"'DISTINCT'", "'DISTRIBUTE'", "'DIV'", "'DROP'", "'ELSE'", "'END'", "'ESCAPE'", 
			"'ESCAPED'", "'EXCEPT'", "'EXCHANGE'", "'EXISTS'", "'EXPLAIN'", "'EXPORT'", 
			"'EXTENDED'", "'EXTERNAL'", "'EXTRACT'", "'FALSE'", "'FETCH'", "'FIELDS'", 
			"'FILTER'", "'FILEFORMAT'", "'FIRST'", "'FOLLOWING'", "'FOR'", "'FOREIGN'", 
			"'FORMAT'", "'FORMATTED'", "'FROM'", "'FULL'", "'FUNCTION'", "'FUNCTIONS'", 
			"'GLOBAL'", "'GRANT'", "'GROUP'", "'GROUPING'", "'HAVING'", "'HOUR'", 
			"'IF'", "'IGNORE'", "'IMPORT'", "'IN'", "'INDEX'", "'INDEXES'", "'INNER'", 
			"'INPATH'", "'INPUTFORMAT'", "'INSERT'", "'INTERSECT'", "'INTERVAL'", 
			"'INTO'", "'IS'", "'ITEMS'", "'JOIN'", "'KEYS'", "'LAST'", "'LATERAL'", 
			"'LAZY'", "'LEADING'", "'LEFT'", "'LIKE'", "'ILIKE'", "'LIMIT'", "'LINES'", 
			"'LIST'", "'LOAD'", "'LOCAL'", "'LOCATION'", "'LOCK'", "'LOCKS'", "'LOGICAL'", 
			"'MACRO'", "'MAP'", "'MATCHED'", "'MERGE'", "'MICROSECOND'", "'MILLISECOND'", 
			"'MINUTE'", "'MONTH'", "'MSCK'", "'NAMESPACE'", "'NAMESPACES'", "'NATURAL'", 
			"'NO'", null, "'NULL'", "'NULLS'", "'OF'", "'ON'", "'ONLY'", "'OPTION'", 
			"'OPTIONS'", "'OR'", "'ORDER'", "'OUT'", "'OUTER'", "'OUTPUTFORMAT'", 
			"'OVER'", "'OVERLAPS'", "'OVERLAY'", "'OVERWRITE'", "'PARTITION'", "'PARTITIONED'", 
			"'PARTITIONS'", "'PERCENTILE_CONT'", "'PERCENTILE_DISC'", "'PERCENT'", 
			"'PIVOT'", "'PLACING'", "'POSITION'", "'PRECEDING'", "'PRIMARY'", "'PRINCIPALS'", 
			"'PROPERTIES'", "'PURGE'", "'QUARTER'", "'QUERY'", "'RANGE'", "'RECORDREADER'", 
			"'RECORDWRITER'", "'RECOVER'", "'REDUCE'", "'REFERENCES'", "'REFRESH'", 
			"'RENAME'", "'REPAIR'", "'REPEATABLE'", "'REPLACE'", "'RESET'", "'RESPECT'", 
			"'RESTRICT'", "'REVOKE'", "'RIGHT'", null, "'ROLE'", "'ROLES'", "'ROLLBACK'", 
			"'ROLLUP'", "'ROW'", "'ROWS'", "'SECOND'", "'SCHEMA'", "'SCHEMAS'", "'SELECT'", 
			"'SEMI'", "'SEPARATED'", "'SERDE'", "'SERDEPROPERTIES'", "'SESSION_USER'", 
			"'SET'", "'MINUS'", "'SETS'", "'SHOW'", "'SKEWED'", "'SOME'", "'SORT'", 
			"'SORTED'", "'START'", "'STATISTICS'", "'STORED'", "'STRATIFY'", "'STRUCT'", 
			"'SUBSTR'", "'SUBSTRING'", "'SYNC'", "'SYSTEM_TIME'", "'SYSTEM_VERSION'", 
			"'TABLE'", "'TABLES'", "'TABLESAMPLE'", "'TBLPROPERTIES'", null, "'TERMINATED'", 
			"'THEN'", "'TIME'", "'TIMESTAMP'", "'TIMESTAMPADD'", "'TIMESTAMPDIFF'", 
			"'TO'", "'TOUCH'", "'TRAILING'", "'TRANSACTION'", "'TRANSACTIONS'", "'TRANSFORM'", 
			"'TRIM'", "'TRUE'", "'TRUNCATE'", "'TRY_CAST'", "'TYPE'", "'UNARCHIVE'", 
			"'UNBOUNDED'", "'UNCACHE'", "'UNION'", "'UNIQUE'", "'UNKNOWN'", "'UNLOCK'", 
			"'UNSET'", "'UPDATE'", "'USE'", "'USER'", "'USING'", "'VALUES'", "'VERSION'", 
			"'VIEW'", "'VIEWS'", "'WEEK'", "'WHEN'", "'WHERE'", "'WINDOW'", "'WITH'", 
			"'WITHIN'", "'YEAR'", "'ZONE'", null, "'<=>'", "'<>'", "'!='", "'<'", 
			null, "'>'", null, "'+'", "'-'", "'*'", "'/'", "'%'", "'~'", "'&'", "'|'", 
			"'||'", "'^'", "':'", "'->'", "'/*+'", "'*/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "KEY", "MIGRATE", "SEMICOLON", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", 
			"DOT", "LEFT_BRACKET", "RIGHT_BRACKET", "ADD", "AFTER", "ALL", "ALTER", 
			"ANALYZE", "AND", "ANTI", "ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT", 
			"AUTHORIZATION", "BETWEEN", "BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", 
			"CASCADE", "CASE", "CAST", "CATALOG", "CATALOGS", "CHANGE", "CHECK", 
			"CLEAR", "CLUSTER", "CLUSTERED", "CODEGEN", "COLLATE", "COLLECTION", 
			"COLUMN", "COLUMNS", "COMMENT", "COMMIT", "COMPACT", "COMPACTIONS", "COMPUTE", 
			"CONCATENATE", "CONSTRAINT", "COST", "CREATE", "CROSS", "CUBE", "CURRENT", 
			"CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", 
			"DAY", "DAYOFYEAR", "DATA", "DATABASE", "DATABASES", "DATEADD", "DATEDIFF", 
			"DBPROPERTIES", "DEFINED", "DELETE", "DELIMITED", "DESC", "DESCRIBE", 
			"DFS", "DIRECTORIES", "DIRECTORY", "DISTINCT", "DISTRIBUTE", "DIV", "DROP", 
			"ELSE", "END", "ESCAPE", "ESCAPED", "EXCEPT", "EXCHANGE", "EXISTS", "EXPLAIN", 
			"EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIELDS", 
			"FILTER", "FILEFORMAT", "FIRST", "FOLLOWING", "FOR", "FOREIGN", "FORMAT", 
			"FORMATTED", "FROM", "FULL", "FUNCTION", "FUNCTIONS", "GLOBAL", "GRANT", 
			"GROUP", "GROUPING", "HAVING", "HOUR", "IF", "IGNORE", "IMPORT", "IN", 
			"INDEX", "INDEXES", "INNER", "INPATH", "INPUTFORMAT", "INSERT", "INTERSECT", 
			"INTERVAL", "INTO", "IS", "ITEMS", "JOIN", "KEYS", "LAST", "LATERAL", 
			"LAZY", "LEADING", "LEFT", "LIKE", "ILIKE", "LIMIT", "LINES", "LIST", 
			"LOAD", "LOCAL", "LOCATION", "LOCK", "LOCKS", "LOGICAL", "MACRO", "MAP", 
			"MATCHED", "MERGE", "MICROSECOND", "MILLISECOND", "MINUTE", "MONTH", 
			"MSCK", "NAMESPACE", "NAMESPACES", "NATURAL", "NO", "NOT", "NULL", "NULLS", 
			"OF", "ON", "ONLY", "OPTION", "OPTIONS", "OR", "ORDER", "OUT", "OUTER", 
			"OUTPUTFORMAT", "OVER", "OVERLAPS", "OVERLAY", "OVERWRITE", "PARTITION", 
			"PARTITIONED", "PARTITIONS", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENTLIT", 
			"PIVOT", "PLACING", "POSITION", "PRECEDING", "PRIMARY", "PRINCIPALS", 
			"PROPERTIES", "PURGE", "QUARTER", "QUERY", "RANGE", "RECORDREADER", "RECORDWRITER", 
			"RECOVER", "REDUCE", "REFERENCES", "REFRESH", "RENAME", "REPAIR", "REPEATABLE", 
			"REPLACE", "RESET", "RESPECT", "RESTRICT", "REVOKE", "RIGHT", "RLIKE", 
			"ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SECOND", "SCHEMA", 
			"SCHEMAS", "SELECT", "SEMI", "SEPARATED", "SERDE", "SERDEPROPERTIES", 
			"SESSION_USER", "SET", "SETMINUS", "SETS", "SHOW", "SKEWED", "SOME", 
			"SORT", "SORTED", "START", "STATISTICS", "STORED", "STRATIFY", "STRUCT", 
			"SUBSTR", "SUBSTRING", "SYNC", "SYSTEM_TIME", "SYSTEM_VERSION", "TABLE", 
			"TABLES", "TABLESAMPLE", "TBLPROPERTIES", "TEMPORARY", "TERMINATED", 
			"THEN", "TIME", "TIMESTAMP", "TIMESTAMPADD", "TIMESTAMPDIFF", "TO", "TOUCH", 
			"TRAILING", "TRANSACTION", "TRANSACTIONS", "TRANSFORM", "TRIM", "TRUE", 
			"TRUNCATE", "TRY_CAST", "TYPE", "UNARCHIVE", "UNBOUNDED", "UNCACHE", 
			"UNION", "UNIQUE", "UNKNOWN", "UNLOCK", "UNSET", "UPDATE", "USE", "USER", 
			"USING", "VALUES", "VERSION", "VIEW", "VIEWS", "WEEK", "WHEN", "WHERE", 
			"WINDOW", "WITH", "WITHIN", "YEAR", "ZONE", "EQ", "NSEQ", "NEQ", "NEQJ", 
			"LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "ASTERISK", "SLASH", "PERCENT", 
			"TILDE", "AMPERSAND", "PIPE", "CONCAT_PIPE", "HAT", "COLON", "ARROW", 
			"HENT_START", "HENT_END", "STRING", "BIGINT_LITERAL", "SMALLINT_LITERAL", 
			"TINYINT_LITERAL", "INTEGER_VALUE", "EXPONENT_VALUE", "DECIMAL_VALUE", 
			"FLOAT_LITERAL", "DOUBLE_LITERAL", "BIGDECIMAL_LITERAL", "IDENTIFIER", 
			"BACKQUOTED_IDENTIFIER", "SIMPLE_COMMENT", "BRACKETED_COMMENT", "WS", 
			"UNRECOGNIZED"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ArcticExtendSparkSql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	  /**
	   * When false, INTERSECT is given the greater precedence over the other set
	   * operations (UNION, EXCEPT and MINUS) as per the SQL standard.
	   */
	  public boolean legacy_setops_precedence_enabled = false;

	  /**
	   * When false, a literal with an exponent would be converted into
	   * double type rather than decimal type.
	   */
	  public boolean legacy_exponent_literal_as_decimal_enabled = false;

	  /**
	   * When true, the behavior of keywords follows ANSI SQL standard.
	   */
	  public boolean SQL_standard_keyword_behavior = false;

	public ArcticExtendSparkSqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ArcticCommandContext extends ParserRuleContext {
		public ArcticStatementContext arcticStatement() {
			return getRuleContext(ArcticStatementContext.class,0);
		}
		public ArcticCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arcticCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterArcticCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitArcticCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitArcticCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArcticCommandContext arcticCommand() throws RecognitionException {
		ArcticCommandContext _localctx = new ArcticCommandContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_arcticCommand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			arcticStatement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArcticStatementContext extends ParserRuleContext {
		public ArcticStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arcticStatement; }
	 
		public ArcticStatementContext() { }
		public void copyFrom(ArcticStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CreateTableWithPkContext extends ArcticStatementContext {
		public CreateTableWithPrimaryKeyContext createTableWithPrimaryKey() {
			return getRuleContext(CreateTableWithPrimaryKeyContext.class,0);
		}
		public CreateTableWithPkContext(ArcticStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTableWithPk(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTableWithPk(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTableWithPk(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArcticStatementContext arcticStatement() throws RecognitionException {
		ArcticStatementContext _localctx = new ArcticStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_arcticStatement);
		try {
			_localctx = new CreateTableWithPkContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			createTableWithPrimaryKey();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateTableWithPrimaryKeyContext extends ParserRuleContext {
		public CreateTableHeaderContext createTableHeader() {
			return getRuleContext(CreateTableHeaderContext.class,0);
		}
		public ColListAndPkContext colListAndPk() {
			return getRuleContext(ColListAndPkContext.class,0);
		}
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public CreateTableClausesContext createTableClauses() {
			return getRuleContext(CreateTableClausesContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public CreateTableWithPrimaryKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableWithPrimaryKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTableWithPrimaryKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTableWithPrimaryKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTableWithPrimaryKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableWithPrimaryKeyContext createTableWithPrimaryKey() throws RecognitionException {
		CreateTableWithPrimaryKeyContext _localctx = new CreateTableWithPrimaryKeyContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_createTableWithPrimaryKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			createTableHeader();
			setState(311);
			colListAndPk();
			setState(312);
			tableProvider();
			setState(313);
			createTableClauses();
			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 197)) & ~0x3f) == 0 && ((1L << (_la - 197)) & ((1L << (REDUCE - 197)) | (1L << (SELECT - 197)) | (1L << (TABLE - 197)))) != 0) || _la==VALUES || _la==WITH) {
				{
				setState(315);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(314);
					match(AS);
					}
				}

				setState(317);
				query();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimarySpecContext extends ParserRuleContext {
		public TerminalNode PRIMARY() { return getToken(ArcticExtendSparkSqlParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(ArcticExtendSparkSqlParser.KEY, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public PrimarySpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primarySpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPrimarySpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPrimarySpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPrimarySpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimarySpecContext primarySpec() throws RecognitionException {
		PrimarySpecContext _localctx = new PrimarySpecContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_primarySpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			match(PRIMARY);
			setState(321);
			match(KEY);
			setState(322);
			identifierList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColListAndPkContext extends ParserRuleContext {
		public ColListAndPkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colListAndPk; }
	 
		public ColListAndPkContext() { }
		public void copyFrom(ColListAndPkContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ColListOnlyPkContext extends ColListAndPkContext {
		public PrimarySpecContext primarySpec() {
			return getRuleContext(PrimarySpecContext.class,0);
		}
		public ColListOnlyPkContext(ColListAndPkContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterColListOnlyPk(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitColListOnlyPk(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitColListOnlyPk(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ColListWithPkContext extends ColListAndPkContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode COMMA() { return getToken(ArcticExtendSparkSqlParser.COMMA, 0); }
		public PrimarySpecContext primarySpec() {
			return getRuleContext(PrimarySpecContext.class,0);
		}
		public ColListWithPkContext(ColListAndPkContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterColListWithPk(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitColListWithPk(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitColListWithPk(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColListAndPkContext colListAndPk() throws RecognitionException {
		ColListAndPkContext _localctx = new ColListAndPkContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_colListAndPk);
		int _la;
		try {
			setState(333);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LEFT_PAREN:
				_localctx = new ColListWithPkContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(324);
				match(LEFT_PAREN);
				setState(325);
				colTypeList();
				setState(328);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(326);
					match(COMMA);
					setState(327);
					primarySpec();
					}
				}

				setState(330);
				match(RIGHT_PAREN);
				}
				break;
			case PRIMARY:
				_localctx = new ColListOnlyPkContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(332);
				primarySpec();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleStatementContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public List<TerminalNode> SEMICOLON() { return getTokens(ArcticExtendSparkSqlParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(ArcticExtendSparkSqlParser.SEMICOLON, i);
		}
		public SingleStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleStatementContext singleStatement() throws RecognitionException {
		SingleStatementContext _localctx = new SingleStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_singleStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(335);
			statement();
			setState(339);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(336);
				match(SEMICOLON);
				}
				}
				setState(341);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(342);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleExpressionContext extends ParserRuleContext {
		public NamedExpressionContext namedExpression() {
			return getRuleContext(NamedExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public SingleExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleExpressionContext singleExpression() throws RecognitionException {
		SingleExpressionContext _localctx = new SingleExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_singleExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(344);
			namedExpression();
			setState(345);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleTableIdentifierContext extends ParserRuleContext {
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public SingleTableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleTableIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleTableIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleTableIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableIdentifierContext singleTableIdentifier() throws RecognitionException {
		SingleTableIdentifierContext _localctx = new SingleTableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_singleTableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(347);
			tableIdentifier();
			setState(348);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleMultipartIdentifierContext extends ParserRuleContext {
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public SingleMultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleMultipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleMultipartIdentifierContext singleMultipartIdentifier() throws RecognitionException {
		SingleMultipartIdentifierContext _localctx = new SingleMultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_singleMultipartIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(350);
			multipartIdentifier();
			setState(351);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleFunctionIdentifierContext extends ParserRuleContext {
		public FunctionIdentifierContext functionIdentifier() {
			return getRuleContext(FunctionIdentifierContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public SingleFunctionIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleFunctionIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleFunctionIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleFunctionIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleFunctionIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleFunctionIdentifierContext singleFunctionIdentifier() throws RecognitionException {
		SingleFunctionIdentifierContext _localctx = new SingleFunctionIdentifierContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_singleFunctionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(353);
			functionIdentifier();
			setState(354);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleDataTypeContext extends ParserRuleContext {
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public SingleDataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleDataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleDataTypeContext singleDataType() throws RecognitionException {
		SingleDataTypeContext _localctx = new SingleDataTypeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_singleDataType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(356);
			dataType();
			setState(357);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleTableSchemaContext extends ParserRuleContext {
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticExtendSparkSqlParser.EOF, 0); }
		public SingleTableSchemaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableSchema; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleTableSchema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleTableSchema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleTableSchema(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableSchemaContext singleTableSchema() throws RecognitionException {
		SingleTableSchemaContext _localctx = new SingleTableSchemaContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_singleTableSchema);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			colTypeList();
			setState(360);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	 
		public StatementContext() { }
		public void copyFrom(StatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ExplainContext extends StatementContext {
		public TerminalNode EXPLAIN() { return getToken(ArcticExtendSparkSqlParser.EXPLAIN, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode LOGICAL() { return getToken(ArcticExtendSparkSqlParser.LOGICAL, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticExtendSparkSqlParser.FORMATTED, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public TerminalNode CODEGEN() { return getToken(ArcticExtendSparkSqlParser.CODEGEN, 0); }
		public TerminalNode COST() { return getToken(ArcticExtendSparkSqlParser.COST, 0); }
		public ExplainContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterExplain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitExplain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitExplain(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ResetConfigurationContext extends StatementContext {
		public TerminalNode RESET() { return getToken(ArcticExtendSparkSqlParser.RESET, 0); }
		public ResetConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterResetConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitResetConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitResetConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AlterViewQueryContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public AlterViewQueryContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAlterViewQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAlterViewQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAlterViewQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UseContext extends StatementContext {
		public TerminalNode USE() { return getToken(ArcticExtendSparkSqlParser.USE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public UseContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUse(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropNamespaceContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode RESTRICT() { return getToken(ArcticExtendSparkSqlParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(ArcticExtendSparkSqlParser.CASCADE, 0); }
		public DropNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTempViewUsingContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode GLOBAL() { return getToken(ArcticExtendSparkSqlParser.GLOBAL, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public CreateTempViewUsingContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTempViewUsing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTempViewUsing(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTempViewUsing(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTableContext extends StatementContext {
		public MultipartIdentifierContext from;
		public MultipartIdentifierContext to;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode RENAME() { return getToken(ArcticExtendSparkSqlParser.RENAME, 0); }
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public RenameTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRenameTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRenameTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRenameTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FailNativeCommandContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode ROLE() { return getToken(ArcticExtendSparkSqlParser.ROLE, 0); }
		public UnsupportedHiveNativeCommandsContext unsupportedHiveNativeCommands() {
			return getRuleContext(UnsupportedHiveNativeCommandsContext.class,0);
		}
		public FailNativeCommandContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFailNativeCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFailNativeCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFailNativeCommand(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetCatalogContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode CATALOG() { return getToken(ArcticExtendSparkSqlParser.CATALOG, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public SetCatalogContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetCatalog(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetCatalog(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetCatalog(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ClearCacheContext extends StatementContext {
		public TerminalNode CLEAR() { return getToken(ArcticExtendSparkSqlParser.CLEAR, 0); }
		public TerminalNode CACHE() { return getToken(ArcticExtendSparkSqlParser.CACHE, 0); }
		public ClearCacheContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterClearCache(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitClearCache(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitClearCache(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropViewContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public DropViewContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropView(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTablesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode TABLES() { return getToken(ArcticExtendSparkSqlParser.TABLES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public ShowTablesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowTables(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RecoverPartitionsContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode RECOVER() { return getToken(ArcticExtendSparkSqlParser.RECOVER, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticExtendSparkSqlParser.PARTITIONS, 0); }
		public RecoverPartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRecoverPartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRecoverPartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRecoverPartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropIndexContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode INDEX() { return getToken(ArcticExtendSparkSqlParser.INDEX, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public DropIndexContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropIndex(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCatalogsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode CATALOGS() { return getToken(ArcticExtendSparkSqlParser.CATALOGS, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public ShowCatalogsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowCatalogs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowCatalogs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowCatalogs(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCurrentNamespaceContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticExtendSparkSqlParser.CURRENT, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public ShowCurrentNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowCurrentNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowCurrentNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowCurrentNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTablePartitionContext extends StatementContext {
		public PartitionSpecContext from;
		public PartitionSpecContext to;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode RENAME() { return getToken(ArcticExtendSparkSqlParser.RENAME, 0); }
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public List<PartitionSpecContext> partitionSpec() {
			return getRuleContexts(PartitionSpecContext.class);
		}
		public PartitionSpecContext partitionSpec(int i) {
			return getRuleContext(PartitionSpecContext.class,i);
		}
		public RenameTablePartitionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRenameTablePartition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRenameTablePartition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRenameTablePartition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RepairTableContext extends StatementContext {
		public Token option;
		public TerminalNode MSCK() { return getToken(ArcticExtendSparkSqlParser.MSCK, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticExtendSparkSqlParser.REPAIR, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode PARTITIONS() { return getToken(ArcticExtendSparkSqlParser.PARTITIONS, 0); }
		public TerminalNode ADD() { return getToken(ArcticExtendSparkSqlParser.ADD, 0); }
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode SYNC() { return getToken(ArcticExtendSparkSqlParser.SYNC, 0); }
		public RepairTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRepairTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRepairTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRepairTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshResourceContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(ArcticExtendSparkSqlParser.REFRESH, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public RefreshResourceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRefreshResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRefreshResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRefreshResource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCreateTableContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode SERDE() { return getToken(ArcticExtendSparkSqlParser.SERDE, 0); }
		public ShowCreateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowCreateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowNamespacesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public NamespacesContext namespaces() {
			return getRuleContext(NamespacesContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public ShowNamespacesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowNamespaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowNamespaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowNamespaces(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext ns;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public List<TerminalNode> FROM() { return getTokens(ArcticExtendSparkSqlParser.FROM); }
		public TerminalNode FROM(int i) {
			return getToken(ArcticExtendSparkSqlParser.FROM, i);
		}
		public List<TerminalNode> IN() { return getTokens(ArcticExtendSparkSqlParser.IN); }
		public TerminalNode IN(int i) {
			return getToken(ArcticExtendSparkSqlParser.IN, i);
		}
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public ShowColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ReplaceTableContext extends StatementContext {
		public ReplaceTableHeaderContext replaceTableHeader() {
			return getRuleContext(ReplaceTableHeaderContext.class,0);
		}
		public CreateTableClausesContext createTableClauses() {
			return getRuleContext(CreateTableClausesContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public ReplaceTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterReplaceTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitReplaceTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitReplaceTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AnalyzeTablesContext extends StatementContext {
		public TerminalNode ANALYZE() { return getToken(ArcticExtendSparkSqlParser.ANALYZE, 0); }
		public TerminalNode TABLES() { return getToken(ArcticExtendSparkSqlParser.TABLES, 0); }
		public TerminalNode COMPUTE() { return getToken(ArcticExtendSparkSqlParser.COMPUTE, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticExtendSparkSqlParser.STATISTICS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public AnalyzeTablesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAnalyzeTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAnalyzeTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAnalyzeTables(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AddTablePartitionContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArcticExtendSparkSqlParser.ADD, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public List<PartitionSpecLocationContext> partitionSpecLocation() {
			return getRuleContexts(PartitionSpecLocationContext.class);
		}
		public PartitionSpecLocationContext partitionSpecLocation(int i) {
			return getRuleContext(PartitionSpecLocationContext.class,i);
		}
		public AddTablePartitionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAddTablePartition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAddTablePartition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAddTablePartition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetNamespaceLocationContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public SetNamespaceLocationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetNamespaceLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetNamespaceLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetNamespaceLocation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshTableContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(ArcticExtendSparkSqlParser.REFRESH, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public RefreshTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRefreshTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRefreshTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRefreshTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetNamespacePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode DBPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.DBPROPERTIES, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticExtendSparkSqlParser.PROPERTIES, 0); }
		public SetNamespacePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetNamespaceProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetNamespaceProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetNamespaceProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ManageResourceContext extends StatementContext {
		public Token op;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArcticExtendSparkSqlParser.ADD, 0); }
		public TerminalNode LIST() { return getToken(ArcticExtendSparkSqlParser.LIST, 0); }
		public ManageResourceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterManageResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitManageResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitManageResource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetQuotedConfigurationContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public ConfigValueContext configValue() {
			return getRuleContext(ConfigValueContext.class,0);
		}
		public SetQuotedConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetQuotedConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetQuotedConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetQuotedConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AnalyzeContext extends StatementContext {
		public TerminalNode ANALYZE() { return getToken(ArcticExtendSparkSqlParser.ANALYZE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode COMPUTE() { return getToken(ArcticExtendSparkSqlParser.COMPUTE, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticExtendSparkSqlParser.STATISTICS, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public TerminalNode ALL() { return getToken(ArcticExtendSparkSqlParser.ALL, 0); }
		public AnalyzeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAnalyze(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAnalyze(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAnalyze(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateFunctionContext extends StatementContext {
		public Token className;
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticExtendSparkSqlParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public List<ResourceContext> resource() {
			return getRuleContexts(ResourceContext.class);
		}
		public ResourceContext resource(int i) {
			return getRuleContext(ResourceContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public CreateFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class HiveReplaceColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() {
			return getRuleContext(QualifiedColTypeWithPositionListContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public HiveReplaceColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterHiveReplaceColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitHiveReplaceColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitHiveReplaceColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CommentNamespaceContext extends StatementContext {
		public Token comment;
		public TerminalNode COMMENT() { return getToken(ArcticExtendSparkSqlParser.COMMENT, 0); }
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IS() { return getToken(ArcticExtendSparkSqlParser.IS, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public CommentNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCommentNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCommentNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCommentNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ResetQuotedConfigurationContext extends StatementContext {
		public TerminalNode RESET() { return getToken(ArcticExtendSparkSqlParser.RESET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public ResetQuotedConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterResetQuotedConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitResetQuotedConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitResetQuotedConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTableContext extends StatementContext {
		public CreateTableHeaderContext createTableHeader() {
			return getRuleContext(CreateTableHeaderContext.class,0);
		}
		public CreateTableClausesContext createTableClauses() {
			return getRuleContext(CreateTableClausesContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public CreateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DmlStatementContext extends StatementContext {
		public DmlStatementNoWithContext dmlStatementNoWith() {
			return getRuleContext(DmlStatementNoWithContext.class,0);
		}
		public CtesContext ctes() {
			return getRuleContext(CtesContext.class,0);
		}
		public DmlStatementContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDmlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDmlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDmlStatement(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTableLikeContext extends StatementContext {
		public TableIdentifierContext target;
		public TableIdentifierContext source;
		public PropertyListContext tableProps;
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public List<TableIdentifierContext> tableIdentifier() {
			return getRuleContexts(TableIdentifierContext.class);
		}
		public TableIdentifierContext tableIdentifier(int i) {
			return getRuleContext(TableIdentifierContext.class,i);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public List<TableProviderContext> tableProvider() {
			return getRuleContexts(TableProviderContext.class);
		}
		public TableProviderContext tableProvider(int i) {
			return getRuleContext(TableProviderContext.class,i);
		}
		public List<RowFormatContext> rowFormat() {
			return getRuleContexts(RowFormatContext.class);
		}
		public RowFormatContext rowFormat(int i) {
			return getRuleContext(RowFormatContext.class,i);
		}
		public List<CreateFileFormatContext> createFileFormat() {
			return getRuleContexts(CreateFileFormatContext.class);
		}
		public CreateFileFormatContext createFileFormat(int i) {
			return getRuleContext(CreateFileFormatContext.class,i);
		}
		public List<LocationSpecContext> locationSpec() {
			return getRuleContexts(LocationSpecContext.class);
		}
		public LocationSpecContext locationSpec(int i) {
			return getRuleContext(LocationSpecContext.class,i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(ArcticExtendSparkSqlParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public CreateTableLikeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTableLike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTableLike(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTableLike(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UncacheTableContext extends StatementContext {
		public TerminalNode UNCACHE() { return getToken(ArcticExtendSparkSqlParser.UNCACHE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public UncacheTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUncacheTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUncacheTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUncacheTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropFunctionContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticExtendSparkSqlParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public DropFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeRelationContext extends StatementContext {
		public Token option;
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticExtendSparkSqlParser.DESCRIBE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public DescribeColNameContext describeColName() {
			return getRuleContext(DescribeColNameContext.class,0);
		}
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticExtendSparkSqlParser.FORMATTED, 0); }
		public DescribeRelationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDescribeRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDescribeRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDescribeRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LoadDataContext extends StatementContext {
		public Token path;
		public TerminalNode LOAD() { return getToken(ArcticExtendSparkSqlParser.LOAD, 0); }
		public TerminalNode DATA() { return getToken(ArcticExtendSparkSqlParser.DATA, 0); }
		public TerminalNode INPATH() { return getToken(ArcticExtendSparkSqlParser.INPATH, 0); }
		public TerminalNode INTO() { return getToken(ArcticExtendSparkSqlParser.INTO, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public LoadDataContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLoadData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLoadData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLoadData(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowPartitionsContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticExtendSparkSqlParser.PARTITIONS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public ShowPartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowPartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowPartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowPartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeFunctionContext extends StatementContext {
		public TerminalNode FUNCTION() { return getToken(ArcticExtendSparkSqlParser.FUNCTION, 0); }
		public DescribeFuncNameContext describeFuncName() {
			return getRuleContext(DescribeFuncNameContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticExtendSparkSqlParser.DESCRIBE, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public DescribeFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDescribeFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDescribeFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDescribeFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTableColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext from;
		public ErrorCapturingIdentifierContext to;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode RENAME() { return getToken(ArcticExtendSparkSqlParser.RENAME, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public RenameTableColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRenameTableColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRenameTableColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRenameTableColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StatementDefaultContext extends StatementContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public StatementDefaultContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterStatementDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitStatementDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitStatementDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class HiveChangeColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext colName;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode CHANGE() { return getToken(ArcticExtendSparkSqlParser.CHANGE, 0); }
		public ColTypeContext colType() {
			return getRuleContext(ColTypeContext.class,0);
		}
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public HiveChangeColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterHiveChangeColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitHiveChangeColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitHiveChangeColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTimeZoneContext extends StatementContext {
		public Token timezone;
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode TIME() { return getToken(ArcticExtendSparkSqlParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(ArcticExtendSparkSqlParser.ZONE, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public SetTimeZoneContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetTimeZone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetTimeZone(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetTimeZone(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeQueryContext extends StatementContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticExtendSparkSqlParser.DESCRIBE, 0); }
		public TerminalNode QUERY() { return getToken(ArcticExtendSparkSqlParser.QUERY, 0); }
		public DescribeQueryContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDescribeQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDescribeQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDescribeQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TruncateTableContext extends StatementContext {
		public TerminalNode TRUNCATE() { return getToken(ArcticExtendSparkSqlParser.TRUNCATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TruncateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTruncateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTruncateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTruncateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTableSerDeContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode SERDE() { return getToken(ArcticExtendSparkSqlParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public SetTableSerDeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetTableSerDe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetTableSerDe(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetTableSerDe(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateViewContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public IdentifierCommentListContext identifierCommentList() {
			return getRuleContext(IdentifierCommentListContext.class,0);
		}
		public List<CommentSpecContext> commentSpec() {
			return getRuleContexts(CommentSpecContext.class);
		}
		public CommentSpecContext commentSpec(int i) {
			return getRuleContext(CommentSpecContext.class,i);
		}
		public List<TerminalNode> PARTITIONED() { return getTokens(ArcticExtendSparkSqlParser.PARTITIONED); }
		public TerminalNode PARTITIONED(int i) {
			return getToken(ArcticExtendSparkSqlParser.PARTITIONED, i);
		}
		public List<TerminalNode> ON() { return getTokens(ArcticExtendSparkSqlParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(ArcticExtendSparkSqlParser.ON, i);
		}
		public List<IdentifierListContext> identifierList() {
			return getRuleContexts(IdentifierListContext.class);
		}
		public IdentifierListContext identifierList(int i) {
			return getRuleContext(IdentifierListContext.class,i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(ArcticExtendSparkSqlParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public TerminalNode GLOBAL() { return getToken(ArcticExtendSparkSqlParser.GLOBAL, 0); }
		public CreateViewContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateView(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTablePartitionsContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public List<PartitionSpecContext> partitionSpec() {
			return getRuleContexts(PartitionSpecContext.class);
		}
		public PartitionSpecContext partitionSpec(int i) {
			return getRuleContext(PartitionSpecContext.class,i);
		}
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public TerminalNode PURGE() { return getToken(ArcticExtendSparkSqlParser.PURGE, 0); }
		public DropTablePartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropTablePartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropTablePartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropTablePartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetConfigurationContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public SetConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTableContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode PURGE() { return getToken(ArcticExtendSparkSqlParser.PURGE, 0); }
		public DropTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTableExtendedContext extends StatementContext {
		public MultipartIdentifierContext ns;
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public ShowTableExtendedContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowTableExtended(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowTableExtended(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowTableExtended(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeNamespaceContext extends StatementContext {
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticExtendSparkSqlParser.DESCRIBE, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public DescribeNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDescribeNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDescribeNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDescribeNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AlterTableAlterColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext column;
		public List<TerminalNode> ALTER() { return getTokens(ArcticExtendSparkSqlParser.ALTER); }
		public TerminalNode ALTER(int i) {
			return getToken(ArcticExtendSparkSqlParser.ALTER, i);
		}
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public TerminalNode CHANGE() { return getToken(ArcticExtendSparkSqlParser.CHANGE, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public AlterColumnActionContext alterColumnAction() {
			return getRuleContext(AlterColumnActionContext.class,0);
		}
		public AlterTableAlterColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAlterTableAlterColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAlterTableAlterColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAlterTableAlterColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshFunctionContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(ArcticExtendSparkSqlParser.REFRESH, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticExtendSparkSqlParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public RefreshFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRefreshFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRefreshFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRefreshFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CommentTableContext extends StatementContext {
		public Token comment;
		public TerminalNode COMMENT() { return getToken(ArcticExtendSparkSqlParser.COMMENT, 0); }
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IS() { return getToken(ArcticExtendSparkSqlParser.IS, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public CommentTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCommentTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCommentTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCommentTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateIndexContext extends StatementContext {
		public IdentifierContext indexType;
		public MultipartIdentifierPropertyListContext columns;
		public PropertyListContext options;
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(ArcticExtendSparkSqlParser.INDEX, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public MultipartIdentifierPropertyListContext multipartIdentifierPropertyList() {
			return getRuleContext(MultipartIdentifierPropertyListContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public CreateIndexContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateIndex(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UseNamespaceContext extends StatementContext {
		public TerminalNode USE() { return getToken(ArcticExtendSparkSqlParser.USE, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public UseNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUseNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUseNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUseNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateNamespaceContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public List<CommentSpecContext> commentSpec() {
			return getRuleContexts(CommentSpecContext.class);
		}
		public CommentSpecContext commentSpec(int i) {
			return getRuleContext(CommentSpecContext.class,i);
		}
		public List<LocationSpecContext> locationSpec() {
			return getRuleContexts(LocationSpecContext.class);
		}
		public LocationSpecContext locationSpec(int i) {
			return getRuleContext(LocationSpecContext.class,i);
		}
		public List<TerminalNode> WITH() { return getTokens(ArcticExtendSparkSqlParser.WITH); }
		public TerminalNode WITH(int i) {
			return getToken(ArcticExtendSparkSqlParser.WITH, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public List<TerminalNode> DBPROPERTIES() { return getTokens(ArcticExtendSparkSqlParser.DBPROPERTIES); }
		public TerminalNode DBPROPERTIES(int i) {
			return getToken(ArcticExtendSparkSqlParser.DBPROPERTIES, i);
		}
		public List<TerminalNode> PROPERTIES() { return getTokens(ArcticExtendSparkSqlParser.PROPERTIES); }
		public TerminalNode PROPERTIES(int i) {
			return getToken(ArcticExtendSparkSqlParser.PROPERTIES, i);
		}
		public CreateNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTblPropertiesContext extends StatementContext {
		public MultipartIdentifierContext table;
		public PropertyKeyContext key;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public PropertyKeyContext propertyKey() {
			return getRuleContext(PropertyKeyContext.class,0);
		}
		public ShowTblPropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowTblProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowTblProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowTblProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnsetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode UNSET() { return getToken(ArcticExtendSparkSqlParser.UNSET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public UnsetTablePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUnsetTableProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUnsetTableProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUnsetTableProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTableLocationContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public SetTableLocationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetTableLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetTableLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetTableLocation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTableColumnsContext extends StatementContext {
		public MultipartIdentifierListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public DropTableColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDropTableColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDropTableColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDropTableColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowViewsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticExtendSparkSqlParser.VIEWS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public ShowViewsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowViews(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowViews(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowViews(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowFunctionsContext extends StatementContext {
		public MultipartIdentifierContext ns;
		public MultipartIdentifierContext legacy;
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticExtendSparkSqlParser.FUNCTIONS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public ShowFunctionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterShowFunctions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitShowFunctions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitShowFunctions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CacheTableContext extends StatementContext {
		public PropertyListContext options;
		public TerminalNode CACHE() { return getToken(ArcticExtendSparkSqlParser.CACHE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LAZY() { return getToken(ArcticExtendSparkSqlParser.LAZY, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public CacheTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCacheTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCacheTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCacheTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AddTableColumnsContext extends StatementContext {
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArcticExtendSparkSqlParser.ADD, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() {
			return getRuleContext(QualifiedColTypeWithPositionListContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public AddTableColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAddTableColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAddTableColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAddTableColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public SetTablePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetTableProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetTableProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetTableProperties(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_statement);
		int _la;
		try {
			int _alt;
			setState(1163);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,124,_ctx) ) {
			case 1:
				_localctx = new StatementDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(362);
				query();
				}
				break;
			case 2:
				_localctx = new DmlStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(364);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(363);
					ctes();
					}
				}

				setState(366);
				dmlStatementNoWith();
				}
				break;
			case 3:
				_localctx = new UseContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(367);
				match(USE);
				setState(368);
				multipartIdentifier();
				}
				break;
			case 4:
				_localctx = new UseNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(369);
				match(USE);
				setState(370);
				namespace();
				setState(371);
				multipartIdentifier();
				}
				break;
			case 5:
				_localctx = new SetCatalogContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(373);
				match(SET);
				setState(374);
				match(CATALOG);
				setState(377);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(375);
					identifier();
					}
					break;
				case 2:
					{
					setState(376);
					match(STRING);
					}
					break;
				}
				}
				break;
			case 6:
				_localctx = new CreateNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(379);
				match(CREATE);
				setState(380);
				namespace();
				setState(384);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(381);
					match(IF);
					setState(382);
					match(NOT);
					setState(383);
					match(EXISTS);
					}
					break;
				}
				setState(386);
				multipartIdentifier();
				setState(394);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==LOCATION || _la==WITH) {
					{
					setState(392);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(387);
						commentSpec();
						}
						break;
					case LOCATION:
						{
						setState(388);
						locationSpec();
						}
						break;
					case WITH:
						{
						{
						setState(389);
						match(WITH);
						setState(390);
						_la = _input.LA(1);
						if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(391);
						propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(396);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 7:
				_localctx = new SetNamespacePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(397);
				match(ALTER);
				setState(398);
				namespace();
				setState(399);
				multipartIdentifier();
				setState(400);
				match(SET);
				setState(401);
				_la = _input.LA(1);
				if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(402);
				propertyList();
				}
				break;
			case 8:
				_localctx = new SetNamespaceLocationContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(404);
				match(ALTER);
				setState(405);
				namespace();
				setState(406);
				multipartIdentifier();
				setState(407);
				match(SET);
				setState(408);
				locationSpec();
				}
				break;
			case 9:
				_localctx = new DropNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(410);
				match(DROP);
				setState(411);
				namespace();
				setState(414);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
				case 1:
					{
					setState(412);
					match(IF);
					setState(413);
					match(EXISTS);
					}
					break;
				}
				setState(416);
				multipartIdentifier();
				setState(418);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CASCADE || _la==RESTRICT) {
					{
					setState(417);
					_la = _input.LA(1);
					if ( !(_la==CASCADE || _la==RESTRICT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				break;
			case 10:
				_localctx = new ShowNamespacesContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(420);
				match(SHOW);
				setState(421);
				namespaces();
				setState(424);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(422);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(423);
					multipartIdentifier();
					}
				}

				setState(430);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(427);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(426);
						match(LIKE);
						}
					}

					setState(429);
					((ShowNamespacesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 11:
				_localctx = new CreateTableContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(432);
				createTableHeader();
				setState(437);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(433);
					match(LEFT_PAREN);
					setState(434);
					colTypeList();
					setState(435);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(440);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(439);
					tableProvider();
					}
				}

				setState(442);
				createTableClauses();
				setState(447);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 197)) & ~0x3f) == 0 && ((1L << (_la - 197)) & ((1L << (REDUCE - 197)) | (1L << (SELECT - 197)) | (1L << (TABLE - 197)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(444);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(443);
						match(AS);
						}
					}

					setState(446);
					query();
					}
				}

				}
				break;
			case 12:
				_localctx = new CreateTableLikeContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(449);
				match(CREATE);
				setState(450);
				match(TABLE);
				setState(454);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(451);
					match(IF);
					setState(452);
					match(NOT);
					setState(453);
					match(EXISTS);
					}
					break;
				}
				setState(456);
				((CreateTableLikeContext)_localctx).target = tableIdentifier();
				setState(457);
				match(LIKE);
				setState(458);
				((CreateTableLikeContext)_localctx).source = tableIdentifier();
				setState(467);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LOCATION || ((((_la - 214)) & ~0x3f) == 0 && ((1L << (_la - 214)) & ((1L << (ROW - 214)) | (1L << (STORED - 214)) | (1L << (TBLPROPERTIES - 214)) | (1L << (USING - 214)))) != 0)) {
					{
					setState(465);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case USING:
						{
						setState(459);
						tableProvider();
						}
						break;
					case ROW:
						{
						setState(460);
						rowFormat();
						}
						break;
					case STORED:
						{
						setState(461);
						createFileFormat();
						}
						break;
					case LOCATION:
						{
						setState(462);
						locationSpec();
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(463);
						match(TBLPROPERTIES);
						setState(464);
						((CreateTableLikeContext)_localctx).tableProps = propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(469);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 13:
				_localctx = new ReplaceTableContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(470);
				replaceTableHeader();
				setState(475);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
				case 1:
					{
					setState(471);
					match(LEFT_PAREN);
					setState(472);
					colTypeList();
					setState(473);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(478);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(477);
					tableProvider();
					}
				}

				setState(480);
				createTableClauses();
				setState(485);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 197)) & ~0x3f) == 0 && ((1L << (_la - 197)) & ((1L << (REDUCE - 197)) | (1L << (SELECT - 197)) | (1L << (TABLE - 197)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(482);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(481);
						match(AS);
						}
					}

					setState(484);
					query();
					}
				}

				}
				break;
			case 14:
				_localctx = new AnalyzeContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(487);
				match(ANALYZE);
				setState(488);
				match(TABLE);
				setState(489);
				multipartIdentifier();
				setState(491);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(490);
					partitionSpec();
					}
				}

				setState(493);
				match(COMPUTE);
				setState(494);
				match(STATISTICS);
				setState(502);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(495);
					identifier();
					}
					break;
				case 2:
					{
					setState(496);
					match(FOR);
					setState(497);
					match(COLUMNS);
					setState(498);
					identifierSeq();
					}
					break;
				case 3:
					{
					setState(499);
					match(FOR);
					setState(500);
					match(ALL);
					setState(501);
					match(COLUMNS);
					}
					break;
				}
				}
				break;
			case 15:
				_localctx = new AnalyzeTablesContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(504);
				match(ANALYZE);
				setState(505);
				match(TABLES);
				setState(508);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(506);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(507);
					multipartIdentifier();
					}
				}

				setState(510);
				match(COMPUTE);
				setState(511);
				match(STATISTICS);
				setState(513);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(512);
					identifier();
					}
					break;
				}
				}
				break;
			case 16:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(515);
				match(ALTER);
				setState(516);
				match(TABLE);
				setState(517);
				multipartIdentifier();
				setState(518);
				match(ADD);
				setState(519);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(520);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				}
				break;
			case 17:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(522);
				match(ALTER);
				setState(523);
				match(TABLE);
				setState(524);
				multipartIdentifier();
				setState(525);
				match(ADD);
				setState(526);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(527);
				match(LEFT_PAREN);
				setState(528);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(529);
				match(RIGHT_PAREN);
				}
				break;
			case 18:
				_localctx = new RenameTableColumnContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(531);
				match(ALTER);
				setState(532);
				match(TABLE);
				setState(533);
				((RenameTableColumnContext)_localctx).table = multipartIdentifier();
				setState(534);
				match(RENAME);
				setState(535);
				match(COLUMN);
				setState(536);
				((RenameTableColumnContext)_localctx).from = multipartIdentifier();
				setState(537);
				match(TO);
				setState(538);
				((RenameTableColumnContext)_localctx).to = errorCapturingIdentifier();
				}
				break;
			case 19:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 19);
				{
				setState(540);
				match(ALTER);
				setState(541);
				match(TABLE);
				setState(542);
				multipartIdentifier();
				setState(543);
				match(DROP);
				setState(544);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(547);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(545);
					match(IF);
					setState(546);
					match(EXISTS);
					}
				}

				setState(549);
				match(LEFT_PAREN);
				setState(550);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				setState(551);
				match(RIGHT_PAREN);
				}
				break;
			case 20:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(553);
				match(ALTER);
				setState(554);
				match(TABLE);
				setState(555);
				multipartIdentifier();
				setState(556);
				match(DROP);
				setState(557);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(560);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(558);
					match(IF);
					setState(559);
					match(EXISTS);
					}
					break;
				}
				setState(562);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				}
				break;
			case 21:
				_localctx = new RenameTableContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(564);
				match(ALTER);
				setState(565);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(566);
				((RenameTableContext)_localctx).from = multipartIdentifier();
				setState(567);
				match(RENAME);
				setState(568);
				match(TO);
				setState(569);
				((RenameTableContext)_localctx).to = multipartIdentifier();
				}
				break;
			case 22:
				_localctx = new SetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(571);
				match(ALTER);
				setState(572);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(573);
				multipartIdentifier();
				setState(574);
				match(SET);
				setState(575);
				match(TBLPROPERTIES);
				setState(576);
				propertyList();
				}
				break;
			case 23:
				_localctx = new UnsetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(578);
				match(ALTER);
				setState(579);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(580);
				multipartIdentifier();
				setState(581);
				match(UNSET);
				setState(582);
				match(TBLPROPERTIES);
				setState(585);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(583);
					match(IF);
					setState(584);
					match(EXISTS);
					}
				}

				setState(587);
				propertyList();
				}
				break;
			case 24:
				_localctx = new AlterTableAlterColumnContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(589);
				match(ALTER);
				setState(590);
				match(TABLE);
				setState(591);
				((AlterTableAlterColumnContext)_localctx).table = multipartIdentifier();
				setState(592);
				_la = _input.LA(1);
				if ( !(_la==ALTER || _la==CHANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(594);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
				case 1:
					{
					setState(593);
					match(COLUMN);
					}
					break;
				}
				setState(596);
				((AlterTableAlterColumnContext)_localctx).column = multipartIdentifier();
				setState(598);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==COMMENT || _la==DROP || _la==FIRST || _la==SET || _la==TYPE) {
					{
					setState(597);
					alterColumnAction();
					}
				}

				}
				break;
			case 25:
				_localctx = new HiveChangeColumnContext(_localctx);
				enterOuterAlt(_localctx, 25);
				{
				setState(600);
				match(ALTER);
				setState(601);
				match(TABLE);
				setState(602);
				((HiveChangeColumnContext)_localctx).table = multipartIdentifier();
				setState(604);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(603);
					partitionSpec();
					}
				}

				setState(606);
				match(CHANGE);
				setState(608);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
				case 1:
					{
					setState(607);
					match(COLUMN);
					}
					break;
				}
				setState(610);
				((HiveChangeColumnContext)_localctx).colName = multipartIdentifier();
				setState(611);
				colType();
				setState(613);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==FIRST) {
					{
					setState(612);
					colPosition();
					}
				}

				}
				break;
			case 26:
				_localctx = new HiveReplaceColumnsContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(615);
				match(ALTER);
				setState(616);
				match(TABLE);
				setState(617);
				((HiveReplaceColumnsContext)_localctx).table = multipartIdentifier();
				setState(619);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(618);
					partitionSpec();
					}
				}

				setState(621);
				match(REPLACE);
				setState(622);
				match(COLUMNS);
				setState(623);
				match(LEFT_PAREN);
				setState(624);
				((HiveReplaceColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(625);
				match(RIGHT_PAREN);
				}
				break;
			case 27:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(627);
				match(ALTER);
				setState(628);
				match(TABLE);
				setState(629);
				multipartIdentifier();
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(630);
					partitionSpec();
					}
				}

				setState(633);
				match(SET);
				setState(634);
				match(SERDE);
				setState(635);
				match(STRING);
				setState(639);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(636);
					match(WITH);
					setState(637);
					match(SERDEPROPERTIES);
					setState(638);
					propertyList();
					}
				}

				}
				break;
			case 28:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(641);
				match(ALTER);
				setState(642);
				match(TABLE);
				setState(643);
				multipartIdentifier();
				setState(645);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(644);
					partitionSpec();
					}
				}

				setState(647);
				match(SET);
				setState(648);
				match(SERDEPROPERTIES);
				setState(649);
				propertyList();
				}
				break;
			case 29:
				_localctx = new AddTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(651);
				match(ALTER);
				setState(652);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(653);
				multipartIdentifier();
				setState(654);
				match(ADD);
				setState(658);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(655);
					match(IF);
					setState(656);
					match(NOT);
					setState(657);
					match(EXISTS);
					}
				}

				setState(661); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(660);
					partitionSpecLocation();
					}
					}
					setState(663); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==PARTITION );
				}
				break;
			case 30:
				_localctx = new RenameTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(665);
				match(ALTER);
				setState(666);
				match(TABLE);
				setState(667);
				multipartIdentifier();
				setState(668);
				((RenameTablePartitionContext)_localctx).from = partitionSpec();
				setState(669);
				match(RENAME);
				setState(670);
				match(TO);
				setState(671);
				((RenameTablePartitionContext)_localctx).to = partitionSpec();
				}
				break;
			case 31:
				_localctx = new DropTablePartitionsContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(673);
				match(ALTER);
				setState(674);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(675);
				multipartIdentifier();
				setState(676);
				match(DROP);
				setState(679);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(677);
					match(IF);
					setState(678);
					match(EXISTS);
					}
				}

				setState(681);
				partitionSpec();
				setState(686);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(682);
					match(COMMA);
					setState(683);
					partitionSpec();
					}
					}
					setState(688);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(690);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(689);
					match(PURGE);
					}
				}

				}
				break;
			case 32:
				_localctx = new SetTableLocationContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(692);
				match(ALTER);
				setState(693);
				match(TABLE);
				setState(694);
				multipartIdentifier();
				setState(696);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(695);
					partitionSpec();
					}
				}

				setState(698);
				match(SET);
				setState(699);
				locationSpec();
				}
				break;
			case 33:
				_localctx = new RecoverPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 33);
				{
				setState(701);
				match(ALTER);
				setState(702);
				match(TABLE);
				setState(703);
				multipartIdentifier();
				setState(704);
				match(RECOVER);
				setState(705);
				match(PARTITIONS);
				}
				break;
			case 34:
				_localctx = new DropTableContext(_localctx);
				enterOuterAlt(_localctx, 34);
				{
				setState(707);
				match(DROP);
				setState(708);
				match(TABLE);
				setState(711);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
				case 1:
					{
					setState(709);
					match(IF);
					setState(710);
					match(EXISTS);
					}
					break;
				}
				setState(713);
				multipartIdentifier();
				setState(715);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(714);
					match(PURGE);
					}
				}

				}
				break;
			case 35:
				_localctx = new DropViewContext(_localctx);
				enterOuterAlt(_localctx, 35);
				{
				setState(717);
				match(DROP);
				setState(718);
				match(VIEW);
				setState(721);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
				case 1:
					{
					setState(719);
					match(IF);
					setState(720);
					match(EXISTS);
					}
					break;
				}
				setState(723);
				multipartIdentifier();
				}
				break;
			case 36:
				_localctx = new CreateViewContext(_localctx);
				enterOuterAlt(_localctx, 36);
				{
				setState(724);
				match(CREATE);
				setState(727);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(725);
					match(OR);
					setState(726);
					match(REPLACE);
					}
				}

				setState(733);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL || _la==TEMPORARY) {
					{
					setState(730);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==GLOBAL) {
						{
						setState(729);
						match(GLOBAL);
						}
					}

					setState(732);
					match(TEMPORARY);
					}
				}

				setState(735);
				match(VIEW);
				setState(739);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
				case 1:
					{
					setState(736);
					match(IF);
					setState(737);
					match(NOT);
					setState(738);
					match(EXISTS);
					}
					break;
				}
				setState(741);
				multipartIdentifier();
				setState(743);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(742);
					identifierCommentList();
					}
				}

				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==PARTITIONED || _la==TBLPROPERTIES) {
					{
					setState(751);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(745);
						commentSpec();
						}
						break;
					case PARTITIONED:
						{
						{
						setState(746);
						match(PARTITIONED);
						setState(747);
						match(ON);
						setState(748);
						identifierList();
						}
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(749);
						match(TBLPROPERTIES);
						setState(750);
						propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(755);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(756);
				match(AS);
				setState(757);
				query();
				}
				break;
			case 37:
				_localctx = new CreateTempViewUsingContext(_localctx);
				enterOuterAlt(_localctx, 37);
				{
				setState(759);
				match(CREATE);
				setState(762);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(760);
					match(OR);
					setState(761);
					match(REPLACE);
					}
				}

				setState(765);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL) {
					{
					setState(764);
					match(GLOBAL);
					}
				}

				setState(767);
				match(TEMPORARY);
				setState(768);
				match(VIEW);
				setState(769);
				tableIdentifier();
				setState(774);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(770);
					match(LEFT_PAREN);
					setState(771);
					colTypeList();
					setState(772);
					match(RIGHT_PAREN);
					}
				}

				setState(776);
				tableProvider();
				setState(779);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(777);
					match(OPTIONS);
					setState(778);
					propertyList();
					}
				}

				}
				break;
			case 38:
				_localctx = new AlterViewQueryContext(_localctx);
				enterOuterAlt(_localctx, 38);
				{
				setState(781);
				match(ALTER);
				setState(782);
				match(VIEW);
				setState(783);
				multipartIdentifier();
				setState(785);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(784);
					match(AS);
					}
				}

				setState(787);
				query();
				}
				break;
			case 39:
				_localctx = new CreateFunctionContext(_localctx);
				enterOuterAlt(_localctx, 39);
				{
				setState(789);
				match(CREATE);
				setState(792);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(790);
					match(OR);
					setState(791);
					match(REPLACE);
					}
				}

				setState(795);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(794);
					match(TEMPORARY);
					}
				}

				setState(797);
				match(FUNCTION);
				setState(801);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
				case 1:
					{
					setState(798);
					match(IF);
					setState(799);
					match(NOT);
					setState(800);
					match(EXISTS);
					}
					break;
				}
				setState(803);
				multipartIdentifier();
				setState(804);
				match(AS);
				setState(805);
				((CreateFunctionContext)_localctx).className = match(STRING);
				setState(815);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(806);
					match(USING);
					setState(807);
					resource();
					setState(812);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(808);
						match(COMMA);
						setState(809);
						resource();
						}
						}
						setState(814);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				}
				break;
			case 40:
				_localctx = new DropFunctionContext(_localctx);
				enterOuterAlt(_localctx, 40);
				{
				setState(817);
				match(DROP);
				setState(819);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(818);
					match(TEMPORARY);
					}
				}

				setState(821);
				match(FUNCTION);
				setState(824);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
				case 1:
					{
					setState(822);
					match(IF);
					setState(823);
					match(EXISTS);
					}
					break;
				}
				setState(826);
				multipartIdentifier();
				}
				break;
			case 41:
				_localctx = new ExplainContext(_localctx);
				enterOuterAlt(_localctx, 41);
				{
				setState(827);
				match(EXPLAIN);
				setState(829);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CODEGEN || _la==COST || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & ((1L << (EXTENDED - 90)) | (1L << (FORMATTED - 90)) | (1L << (LOGICAL - 90)))) != 0)) {
					{
					setState(828);
					_la = _input.LA(1);
					if ( !(_la==CODEGEN || _la==COST || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & ((1L << (EXTENDED - 90)) | (1L << (FORMATTED - 90)) | (1L << (LOGICAL - 90)))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(831);
				statement();
				}
				break;
			case 42:
				_localctx = new ShowTablesContext(_localctx);
				enterOuterAlt(_localctx, 42);
				{
				setState(832);
				match(SHOW);
				setState(833);
				match(TABLES);
				setState(836);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(834);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(835);
					multipartIdentifier();
					}
				}

				setState(842);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(839);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(838);
						match(LIKE);
						}
					}

					setState(841);
					((ShowTablesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 43:
				_localctx = new ShowTableExtendedContext(_localctx);
				enterOuterAlt(_localctx, 43);
				{
				setState(844);
				match(SHOW);
				setState(845);
				match(TABLE);
				setState(846);
				match(EXTENDED);
				setState(849);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(847);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(848);
					((ShowTableExtendedContext)_localctx).ns = multipartIdentifier();
					}
				}

				setState(851);
				match(LIKE);
				setState(852);
				((ShowTableExtendedContext)_localctx).pattern = match(STRING);
				setState(854);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(853);
					partitionSpec();
					}
				}

				}
				break;
			case 44:
				_localctx = new ShowTblPropertiesContext(_localctx);
				enterOuterAlt(_localctx, 44);
				{
				setState(856);
				match(SHOW);
				setState(857);
				match(TBLPROPERTIES);
				setState(858);
				((ShowTblPropertiesContext)_localctx).table = multipartIdentifier();
				setState(863);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(859);
					match(LEFT_PAREN);
					setState(860);
					((ShowTblPropertiesContext)_localctx).key = propertyKey();
					setState(861);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			case 45:
				_localctx = new ShowColumnsContext(_localctx);
				enterOuterAlt(_localctx, 45);
				{
				setState(865);
				match(SHOW);
				setState(866);
				match(COLUMNS);
				setState(867);
				_la = _input.LA(1);
				if ( !(_la==FROM || _la==IN) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(868);
				((ShowColumnsContext)_localctx).table = multipartIdentifier();
				setState(871);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(869);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(870);
					((ShowColumnsContext)_localctx).ns = multipartIdentifier();
					}
				}

				}
				break;
			case 46:
				_localctx = new ShowViewsContext(_localctx);
				enterOuterAlt(_localctx, 46);
				{
				setState(873);
				match(SHOW);
				setState(874);
				match(VIEWS);
				setState(877);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(875);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(876);
					multipartIdentifier();
					}
				}

				setState(883);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(880);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(879);
						match(LIKE);
						}
					}

					setState(882);
					((ShowViewsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 47:
				_localctx = new ShowPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 47);
				{
				setState(885);
				match(SHOW);
				setState(886);
				match(PARTITIONS);
				setState(887);
				multipartIdentifier();
				setState(889);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(888);
					partitionSpec();
					}
				}

				}
				break;
			case 48:
				_localctx = new ShowFunctionsContext(_localctx);
				enterOuterAlt(_localctx, 48);
				{
				setState(891);
				match(SHOW);
				setState(893);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
				case 1:
					{
					setState(892);
					identifier();
					}
					break;
				}
				setState(895);
				match(FUNCTIONS);
				setState(898);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
				case 1:
					{
					setState(896);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(897);
					((ShowFunctionsContext)_localctx).ns = multipartIdentifier();
					}
					break;
				}
				setState(907);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(901);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
					case 1:
						{
						setState(900);
						match(LIKE);
						}
						break;
					}
					setState(905);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
					case 1:
						{
						setState(903);
						((ShowFunctionsContext)_localctx).legacy = multipartIdentifier();
						}
						break;
					case 2:
						{
						setState(904);
						((ShowFunctionsContext)_localctx).pattern = match(STRING);
						}
						break;
					}
					}
					break;
				}
				}
				break;
			case 49:
				_localctx = new ShowCreateTableContext(_localctx);
				enterOuterAlt(_localctx, 49);
				{
				setState(909);
				match(SHOW);
				setState(910);
				match(CREATE);
				setState(911);
				match(TABLE);
				setState(912);
				multipartIdentifier();
				setState(915);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(913);
					match(AS);
					setState(914);
					match(SERDE);
					}
				}

				}
				break;
			case 50:
				_localctx = new ShowCurrentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 50);
				{
				setState(917);
				match(SHOW);
				setState(918);
				match(CURRENT);
				setState(919);
				namespace();
				}
				break;
			case 51:
				_localctx = new ShowCatalogsContext(_localctx);
				enterOuterAlt(_localctx, 51);
				{
				setState(920);
				match(SHOW);
				setState(921);
				match(CATALOGS);
				setState(926);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(923);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(922);
						match(LIKE);
						}
					}

					setState(925);
					((ShowCatalogsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 52:
				_localctx = new DescribeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 52);
				{
				setState(928);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(929);
				match(FUNCTION);
				setState(931);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(930);
					match(EXTENDED);
					}
					break;
				}
				setState(933);
				describeFuncName();
				}
				break;
			case 53:
				_localctx = new DescribeNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 53);
				{
				setState(934);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(935);
				namespace();
				setState(937);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(936);
					match(EXTENDED);
					}
					break;
				}
				setState(939);
				multipartIdentifier();
				}
				break;
			case 54:
				_localctx = new DescribeRelationContext(_localctx);
				enterOuterAlt(_localctx, 54);
				{
				setState(941);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(943);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
				case 1:
					{
					setState(942);
					match(TABLE);
					}
					break;
				}
				setState(946);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
				case 1:
					{
					setState(945);
					((DescribeRelationContext)_localctx).option = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==EXTENDED || _la==FORMATTED) ) {
						((DescribeRelationContext)_localctx).option = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					break;
				}
				setState(948);
				multipartIdentifier();
				setState(950);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
				case 1:
					{
					setState(949);
					partitionSpec();
					}
					break;
				}
				setState(953);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
				case 1:
					{
					setState(952);
					describeColName();
					}
					break;
				}
				}
				break;
			case 55:
				_localctx = new DescribeQueryContext(_localctx);
				enterOuterAlt(_localctx, 55);
				{
				setState(955);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(957);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUERY) {
					{
					setState(956);
					match(QUERY);
					}
				}

				setState(959);
				query();
				}
				break;
			case 56:
				_localctx = new CommentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 56);
				{
				setState(960);
				match(COMMENT);
				setState(961);
				match(ON);
				setState(962);
				namespace();
				setState(963);
				multipartIdentifier();
				setState(964);
				match(IS);
				setState(965);
				((CommentNamespaceContext)_localctx).comment = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==NULL || _la==STRING) ) {
					((CommentNamespaceContext)_localctx).comment = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 57:
				_localctx = new CommentTableContext(_localctx);
				enterOuterAlt(_localctx, 57);
				{
				setState(967);
				match(COMMENT);
				setState(968);
				match(ON);
				setState(969);
				match(TABLE);
				setState(970);
				multipartIdentifier();
				setState(971);
				match(IS);
				setState(972);
				((CommentTableContext)_localctx).comment = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==NULL || _la==STRING) ) {
					((CommentTableContext)_localctx).comment = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 58:
				_localctx = new RefreshTableContext(_localctx);
				enterOuterAlt(_localctx, 58);
				{
				setState(974);
				match(REFRESH);
				setState(975);
				match(TABLE);
				setState(976);
				multipartIdentifier();
				}
				break;
			case 59:
				_localctx = new RefreshFunctionContext(_localctx);
				enterOuterAlt(_localctx, 59);
				{
				setState(977);
				match(REFRESH);
				setState(978);
				match(FUNCTION);
				setState(979);
				multipartIdentifier();
				}
				break;
			case 60:
				_localctx = new RefreshResourceContext(_localctx);
				enterOuterAlt(_localctx, 60);
				{
				setState(980);
				match(REFRESH);
				setState(988);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
				case 1:
					{
					setState(981);
					match(STRING);
					}
					break;
				case 2:
					{
					setState(985);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,97,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(982);
							matchWildcard();
							}
							} 
						}
						setState(987);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,97,_ctx);
					}
					}
					break;
				}
				}
				break;
			case 61:
				_localctx = new CacheTableContext(_localctx);
				enterOuterAlt(_localctx, 61);
				{
				setState(990);
				match(CACHE);
				setState(992);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LAZY) {
					{
					setState(991);
					match(LAZY);
					}
				}

				setState(994);
				match(TABLE);
				setState(995);
				multipartIdentifier();
				setState(998);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(996);
					match(OPTIONS);
					setState(997);
					((CacheTableContext)_localctx).options = propertyList();
					}
				}

				setState(1004);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 197)) & ~0x3f) == 0 && ((1L << (_la - 197)) & ((1L << (REDUCE - 197)) | (1L << (SELECT - 197)) | (1L << (TABLE - 197)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(1001);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(1000);
						match(AS);
						}
					}

					setState(1003);
					query();
					}
				}

				}
				break;
			case 62:
				_localctx = new UncacheTableContext(_localctx);
				enterOuterAlt(_localctx, 62);
				{
				setState(1006);
				match(UNCACHE);
				setState(1007);
				match(TABLE);
				setState(1010);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
				case 1:
					{
					setState(1008);
					match(IF);
					setState(1009);
					match(EXISTS);
					}
					break;
				}
				setState(1012);
				multipartIdentifier();
				}
				break;
			case 63:
				_localctx = new ClearCacheContext(_localctx);
				enterOuterAlt(_localctx, 63);
				{
				setState(1013);
				match(CLEAR);
				setState(1014);
				match(CACHE);
				}
				break;
			case 64:
				_localctx = new LoadDataContext(_localctx);
				enterOuterAlt(_localctx, 64);
				{
				setState(1015);
				match(LOAD);
				setState(1016);
				match(DATA);
				setState(1018);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1017);
					match(LOCAL);
					}
				}

				setState(1020);
				match(INPATH);
				setState(1021);
				((LoadDataContext)_localctx).path = match(STRING);
				setState(1023);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OVERWRITE) {
					{
					setState(1022);
					match(OVERWRITE);
					}
				}

				setState(1025);
				match(INTO);
				setState(1026);
				match(TABLE);
				setState(1027);
				multipartIdentifier();
				setState(1029);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1028);
					partitionSpec();
					}
				}

				}
				break;
			case 65:
				_localctx = new TruncateTableContext(_localctx);
				enterOuterAlt(_localctx, 65);
				{
				setState(1031);
				match(TRUNCATE);
				setState(1032);
				match(TABLE);
				setState(1033);
				multipartIdentifier();
				setState(1035);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1034);
					partitionSpec();
					}
				}

				}
				break;
			case 66:
				_localctx = new RepairTableContext(_localctx);
				enterOuterAlt(_localctx, 66);
				{
				setState(1037);
				match(MSCK);
				setState(1038);
				match(REPAIR);
				setState(1039);
				match(TABLE);
				setState(1040);
				multipartIdentifier();
				setState(1043);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==DROP || _la==SYNC) {
					{
					setState(1041);
					((RepairTableContext)_localctx).option = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==ADD || _la==DROP || _la==SYNC) ) {
						((RepairTableContext)_localctx).option = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(1042);
					match(PARTITIONS);
					}
				}

				}
				break;
			case 67:
				_localctx = new ManageResourceContext(_localctx);
				enterOuterAlt(_localctx, 67);
				{
				setState(1045);
				((ManageResourceContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ADD || _la==LIST) ) {
					((ManageResourceContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1046);
				identifier();
				setState(1050);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1047);
						matchWildcard();
						}
						} 
					}
					setState(1052);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				}
				}
				break;
			case 68:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 68);
				{
				setState(1053);
				match(SET);
				setState(1054);
				match(ROLE);
				setState(1058);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1055);
						matchWildcard();
						}
						} 
					}
					setState(1060);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				}
				}
				break;
			case 69:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 69);
				{
				setState(1061);
				match(SET);
				setState(1062);
				match(TIME);
				setState(1063);
				match(ZONE);
				setState(1064);
				interval();
				}
				break;
			case 70:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 70);
				{
				setState(1065);
				match(SET);
				setState(1066);
				match(TIME);
				setState(1067);
				match(ZONE);
				setState(1068);
				((SetTimeZoneContext)_localctx).timezone = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==LOCAL || _la==STRING) ) {
					((SetTimeZoneContext)_localctx).timezone = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 71:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 71);
				{
				setState(1069);
				match(SET);
				setState(1070);
				match(TIME);
				setState(1071);
				match(ZONE);
				setState(1075);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1072);
						matchWildcard();
						}
						} 
					}
					setState(1077);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				}
				}
				break;
			case 72:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 72);
				{
				setState(1078);
				match(SET);
				setState(1079);
				configKey();
				setState(1080);
				match(EQ);
				setState(1081);
				configValue();
				}
				break;
			case 73:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 73);
				{
				setState(1083);
				match(SET);
				setState(1084);
				configKey();
				setState(1092);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1085);
					match(EQ);
					setState(1089);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(1086);
							matchWildcard();
							}
							} 
						}
						setState(1091);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
					}
					}
				}

				}
				break;
			case 74:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 74);
				{
				setState(1094);
				match(SET);
				setState(1098);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,114,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1095);
						matchWildcard();
						}
						} 
					}
					setState(1100);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,114,_ctx);
				}
				setState(1101);
				match(EQ);
				setState(1102);
				configValue();
				}
				break;
			case 75:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 75);
				{
				setState(1103);
				match(SET);
				setState(1107);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,115,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1104);
						matchWildcard();
						}
						} 
					}
					setState(1109);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,115,_ctx);
				}
				}
				break;
			case 76:
				_localctx = new ResetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 76);
				{
				setState(1110);
				match(RESET);
				setState(1111);
				configKey();
				}
				break;
			case 77:
				_localctx = new ResetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 77);
				{
				setState(1112);
				match(RESET);
				setState(1116);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1113);
						matchWildcard();
						}
						} 
					}
					setState(1118);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
				}
				}
				break;
			case 78:
				_localctx = new CreateIndexContext(_localctx);
				enterOuterAlt(_localctx, 78);
				{
				setState(1119);
				match(CREATE);
				setState(1120);
				match(INDEX);
				setState(1124);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
				case 1:
					{
					setState(1121);
					match(IF);
					setState(1122);
					match(NOT);
					setState(1123);
					match(EXISTS);
					}
					break;
				}
				setState(1126);
				identifier();
				setState(1127);
				match(ON);
				setState(1129);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
				case 1:
					{
					setState(1128);
					match(TABLE);
					}
					break;
				}
				setState(1131);
				multipartIdentifier();
				setState(1134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(1132);
					match(USING);
					setState(1133);
					((CreateIndexContext)_localctx).indexType = identifier();
					}
				}

				setState(1136);
				match(LEFT_PAREN);
				setState(1137);
				((CreateIndexContext)_localctx).columns = multipartIdentifierPropertyList();
				setState(1138);
				match(RIGHT_PAREN);
				setState(1141);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1139);
					match(OPTIONS);
					setState(1140);
					((CreateIndexContext)_localctx).options = propertyList();
					}
				}

				}
				break;
			case 79:
				_localctx = new DropIndexContext(_localctx);
				enterOuterAlt(_localctx, 79);
				{
				setState(1143);
				match(DROP);
				setState(1144);
				match(INDEX);
				setState(1147);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
				case 1:
					{
					setState(1145);
					match(IF);
					setState(1146);
					match(EXISTS);
					}
					break;
				}
				setState(1149);
				identifier();
				setState(1150);
				match(ON);
				setState(1152);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
				case 1:
					{
					setState(1151);
					match(TABLE);
					}
					break;
				}
				setState(1154);
				multipartIdentifier();
				}
				break;
			case 80:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 80);
				{
				setState(1156);
				unsupportedHiveNativeCommands();
				setState(1160);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1157);
						matchWildcard();
						}
						} 
					}
					setState(1162);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConfigKeyContext extends ParserRuleContext {
		public QuotedIdentifierContext quotedIdentifier() {
			return getRuleContext(QuotedIdentifierContext.class,0);
		}
		public ConfigKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_configKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterConfigKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitConfigKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitConfigKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigKeyContext configKey() throws RecognitionException {
		ConfigKeyContext _localctx = new ConfigKeyContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_configKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1165);
			quotedIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConfigValueContext extends ParserRuleContext {
		public QuotedIdentifierContext quotedIdentifier() {
			return getRuleContext(QuotedIdentifierContext.class,0);
		}
		public ConfigValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_configValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterConfigValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitConfigValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitConfigValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigValueContext configValue() throws RecognitionException {
		ConfigValueContext _localctx = new ConfigValueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_configValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1167);
			quotedIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnsupportedHiveNativeCommandsContext extends ParserRuleContext {
		public Token kw1;
		public Token kw2;
		public Token kw3;
		public Token kw4;
		public Token kw5;
		public Token kw6;
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode ROLE() { return getToken(ArcticExtendSparkSqlParser.ROLE, 0); }
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode GRANT() { return getToken(ArcticExtendSparkSqlParser.GRANT, 0); }
		public TerminalNode REVOKE() { return getToken(ArcticExtendSparkSqlParser.REVOKE, 0); }
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticExtendSparkSqlParser.PRINCIPALS, 0); }
		public TerminalNode ROLES() { return getToken(ArcticExtendSparkSqlParser.ROLES, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticExtendSparkSqlParser.CURRENT, 0); }
		public TerminalNode EXPORT() { return getToken(ArcticExtendSparkSqlParser.EXPORT, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode IMPORT() { return getToken(ArcticExtendSparkSqlParser.IMPORT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(ArcticExtendSparkSqlParser.COMPACTIONS, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(ArcticExtendSparkSqlParser.TRANSACTIONS, 0); }
		public TerminalNode INDEXES() { return getToken(ArcticExtendSparkSqlParser.INDEXES, 0); }
		public TerminalNode LOCKS() { return getToken(ArcticExtendSparkSqlParser.LOCKS, 0); }
		public TerminalNode INDEX() { return getToken(ArcticExtendSparkSqlParser.INDEX, 0); }
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode LOCK() { return getToken(ArcticExtendSparkSqlParser.LOCK, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticExtendSparkSqlParser.DATABASE, 0); }
		public TerminalNode UNLOCK() { return getToken(ArcticExtendSparkSqlParser.UNLOCK, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode MACRO() { return getToken(ArcticExtendSparkSqlParser.MACRO, 0); }
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode CLUSTERED() { return getToken(ArcticExtendSparkSqlParser.CLUSTERED, 0); }
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public TerminalNode SORTED() { return getToken(ArcticExtendSparkSqlParser.SORTED, 0); }
		public TerminalNode SKEWED() { return getToken(ArcticExtendSparkSqlParser.SKEWED, 0); }
		public TerminalNode STORED() { return getToken(ArcticExtendSparkSqlParser.STORED, 0); }
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticExtendSparkSqlParser.DIRECTORIES, 0); }
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode LOCATION() { return getToken(ArcticExtendSparkSqlParser.LOCATION, 0); }
		public TerminalNode EXCHANGE() { return getToken(ArcticExtendSparkSqlParser.EXCHANGE, 0); }
		public TerminalNode PARTITION() { return getToken(ArcticExtendSparkSqlParser.PARTITION, 0); }
		public TerminalNode ARCHIVE() { return getToken(ArcticExtendSparkSqlParser.ARCHIVE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(ArcticExtendSparkSqlParser.UNARCHIVE, 0); }
		public TerminalNode TOUCH() { return getToken(ArcticExtendSparkSqlParser.TOUCH, 0); }
		public TerminalNode COMPACT() { return getToken(ArcticExtendSparkSqlParser.COMPACT, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode CONCATENATE() { return getToken(ArcticExtendSparkSqlParser.CONCATENATE, 0); }
		public TerminalNode FILEFORMAT() { return getToken(ArcticExtendSparkSqlParser.FILEFORMAT, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public TerminalNode START() { return getToken(ArcticExtendSparkSqlParser.START, 0); }
		public TerminalNode TRANSACTION() { return getToken(ArcticExtendSparkSqlParser.TRANSACTION, 0); }
		public TerminalNode COMMIT() { return getToken(ArcticExtendSparkSqlParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(ArcticExtendSparkSqlParser.ROLLBACK, 0); }
		public TerminalNode DFS() { return getToken(ArcticExtendSparkSqlParser.DFS, 0); }
		public UnsupportedHiveNativeCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsupportedHiveNativeCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUnsupportedHiveNativeCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUnsupportedHiveNativeCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUnsupportedHiveNativeCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnsupportedHiveNativeCommandsContext unsupportedHiveNativeCommands() throws RecognitionException {
		UnsupportedHiveNativeCommandsContext _localctx = new UnsupportedHiveNativeCommandsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_unsupportedHiveNativeCommands);
		int _la;
		try {
			setState(1337);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1169);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1170);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1171);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1172);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1173);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(GRANT);
				setState(1175);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
				case 1:
					{
					setState(1174);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1177);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(REVOKE);
				setState(1179);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
				case 1:
					{
					setState(1178);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1181);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1182);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(GRANT);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1183);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1184);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				setState(1186);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
				case 1:
					{
					setState(1185);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(GRANT);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1188);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1189);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(PRINCIPALS);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1190);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1191);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLES);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1192);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1193);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CURRENT);
				setState(1194);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ROLES);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1195);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(EXPORT);
				setState(1196);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1197);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(IMPORT);
				setState(1198);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1199);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1200);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(COMPACTIONS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1201);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1202);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CREATE);
				setState(1203);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TABLE);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1204);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1205);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTIONS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1206);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1207);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEXES);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1208);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1209);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(LOCKS);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1210);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1211);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1212);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1213);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1214);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1215);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1216);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1217);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1218);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1219);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1220);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1221);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1222);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1223);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1224);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1225);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1226);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1227);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1228);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1229);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1230);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1231);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1232);
				tableIdentifier();
				setState(1233);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1234);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(CLUSTERED);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(1236);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1237);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1238);
				tableIdentifier();
				setState(1239);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CLUSTERED);
				setState(1240);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(1242);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1243);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1244);
				tableIdentifier();
				setState(1245);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1246);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SORTED);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(1248);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1249);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1250);
				tableIdentifier();
				setState(1251);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SKEWED);
				setState(1252);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(1254);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1255);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1256);
				tableIdentifier();
				setState(1257);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1258);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(1260);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1261);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1262);
				tableIdentifier();
				setState(1263);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1264);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(STORED);
				setState(1265);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(AS);
				setState(1266);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw6 = match(DIRECTORIES);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(1268);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1269);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1270);
				tableIdentifier();
				setState(1271);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1272);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				setState(1273);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(LOCATION);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(1275);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1276);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1277);
				tableIdentifier();
				setState(1278);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(EXCHANGE);
				setState(1279);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(1281);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1282);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1283);
				tableIdentifier();
				setState(1284);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ARCHIVE);
				setState(1285);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(1287);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1288);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1289);
				tableIdentifier();
				setState(1290);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(UNARCHIVE);
				setState(1291);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(1293);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1294);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1295);
				tableIdentifier();
				setState(1296);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TOUCH);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(1298);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1299);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1300);
				tableIdentifier();
				setState(1302);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1301);
					partitionSpec();
					}
				}

				setState(1304);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(COMPACT);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(1306);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1307);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1308);
				tableIdentifier();
				setState(1310);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1309);
					partitionSpec();
					}
				}

				setState(1312);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CONCATENATE);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1314);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1315);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1316);
				tableIdentifier();
				setState(1318);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1317);
					partitionSpec();
					}
				}

				setState(1320);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1321);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(FILEFORMAT);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1323);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1324);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1325);
				tableIdentifier();
				setState(1327);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1326);
					partitionSpec();
					}
				}

				setState(1329);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(REPLACE);
				setState(1330);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(COLUMNS);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1332);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(START);
				setState(1333);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTION);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1334);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(COMMIT);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1335);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ROLLBACK);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1336);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DFS);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateTableHeaderContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode EXTERNAL() { return getToken(ArcticExtendSparkSqlParser.EXTERNAL, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public CreateTableHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTableHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTableHeader(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTableHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableHeaderContext createTableHeader() throws RecognitionException {
		CreateTableHeaderContext _localctx = new CreateTableHeaderContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_createTableHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1339);
			match(CREATE);
			setState(1341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(1340);
				match(TEMPORARY);
				}
			}

			setState(1344);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTERNAL) {
				{
				setState(1343);
				match(EXTERNAL);
				}
			}

			setState(1346);
			match(TABLE);
			setState(1350);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				{
				setState(1347);
				match(IF);
				setState(1348);
				match(NOT);
				setState(1349);
				match(EXISTS);
				}
				break;
			}
			setState(1352);
			multipartIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReplaceTableHeaderContext extends ParserRuleContext {
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public ReplaceTableHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceTableHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterReplaceTableHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitReplaceTableHeader(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitReplaceTableHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReplaceTableHeaderContext replaceTableHeader() throws RecognitionException {
		ReplaceTableHeaderContext _localctx = new ReplaceTableHeaderContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_replaceTableHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1356);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CREATE) {
				{
				setState(1354);
				match(CREATE);
				setState(1355);
				match(OR);
				}
			}

			setState(1358);
			match(REPLACE);
			setState(1359);
			match(TABLE);
			setState(1360);
			multipartIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BucketSpecContext extends ParserRuleContext {
		public TerminalNode CLUSTERED() { return getToken(ArcticExtendSparkSqlParser.CLUSTERED, 0); }
		public List<TerminalNode> BY() { return getTokens(ArcticExtendSparkSqlParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticExtendSparkSqlParser.BY, i);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode INTO() { return getToken(ArcticExtendSparkSqlParser.INTO, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode BUCKETS() { return getToken(ArcticExtendSparkSqlParser.BUCKETS, 0); }
		public TerminalNode SORTED() { return getToken(ArcticExtendSparkSqlParser.SORTED, 0); }
		public OrderedIdentifierListContext orderedIdentifierList() {
			return getRuleContext(OrderedIdentifierListContext.class,0);
		}
		public BucketSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bucketSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterBucketSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitBucketSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitBucketSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BucketSpecContext bucketSpec() throws RecognitionException {
		BucketSpecContext _localctx = new BucketSpecContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_bucketSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1362);
			match(CLUSTERED);
			setState(1363);
			match(BY);
			setState(1364);
			identifierList();
			setState(1368);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SORTED) {
				{
				setState(1365);
				match(SORTED);
				setState(1366);
				match(BY);
				setState(1367);
				orderedIdentifierList();
				}
			}

			setState(1370);
			match(INTO);
			setState(1371);
			match(INTEGER_VALUE);
			setState(1372);
			match(BUCKETS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SkewSpecContext extends ParserRuleContext {
		public TerminalNode SKEWED() { return getToken(ArcticExtendSparkSqlParser.SKEWED, 0); }
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public ConstantListContext constantList() {
			return getRuleContext(ConstantListContext.class,0);
		}
		public NestedConstantListContext nestedConstantList() {
			return getRuleContext(NestedConstantListContext.class,0);
		}
		public TerminalNode STORED() { return getToken(ArcticExtendSparkSqlParser.STORED, 0); }
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticExtendSparkSqlParser.DIRECTORIES, 0); }
		public SkewSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skewSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSkewSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSkewSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSkewSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SkewSpecContext skewSpec() throws RecognitionException {
		SkewSpecContext _localctx = new SkewSpecContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_skewSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1374);
			match(SKEWED);
			setState(1375);
			match(BY);
			setState(1376);
			identifierList();
			setState(1377);
			match(ON);
			setState(1380);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
			case 1:
				{
				setState(1378);
				constantList();
				}
				break;
			case 2:
				{
				setState(1379);
				nestedConstantList();
				}
				break;
			}
			setState(1385);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
			case 1:
				{
				setState(1382);
				match(STORED);
				setState(1383);
				match(AS);
				setState(1384);
				match(DIRECTORIES);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocationSpecContext extends ParserRuleContext {
		public TerminalNode LOCATION() { return getToken(ArcticExtendSparkSqlParser.LOCATION, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public LocationSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_locationSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLocationSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLocationSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLocationSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocationSpecContext locationSpec() throws RecognitionException {
		LocationSpecContext _localctx = new LocationSpecContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_locationSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1387);
			match(LOCATION);
			setState(1388);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentSpecContext extends ParserRuleContext {
		public TerminalNode COMMENT() { return getToken(ArcticExtendSparkSqlParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public CommentSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCommentSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCommentSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCommentSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentSpecContext commentSpec() throws RecognitionException {
		CommentSpecContext _localctx = new CommentSpecContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_commentSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1390);
			match(COMMENT);
			setState(1391);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QueryContext extends ParserRuleContext {
		public QueryTermContext queryTerm() {
			return getRuleContext(QueryTermContext.class,0);
		}
		public QueryOrganizationContext queryOrganization() {
			return getRuleContext(QueryOrganizationContext.class,0);
		}
		public CtesContext ctes() {
			return getRuleContext(CtesContext.class,0);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(1393);
				ctes();
				}
			}

			setState(1396);
			queryTerm(0);
			setState(1397);
			queryOrganization();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InsertIntoContext extends ParserRuleContext {
		public InsertIntoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertInto; }
	 
		public InsertIntoContext() { }
		public void copyFrom(InsertIntoContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InsertOverwriteHiveDirContext extends InsertIntoContext {
		public Token path;
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticExtendSparkSqlParser.DIRECTORY, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public RowFormatContext rowFormat() {
			return getRuleContext(RowFormatContext.class,0);
		}
		public CreateFileFormatContext createFileFormat() {
			return getRuleContext(CreateFileFormatContext.class,0);
		}
		public InsertOverwriteHiveDirContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInsertOverwriteHiveDir(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInsertOverwriteHiveDir(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInsertOverwriteHiveDir(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertOverwriteDirContext extends InsertIntoContext {
		public Token path;
		public PropertyListContext options;
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticExtendSparkSqlParser.DIRECTORY, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public InsertOverwriteDirContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInsertOverwriteDir(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInsertOverwriteDir(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInsertOverwriteDir(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertOverwriteTableContext extends InsertIntoContext {
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public InsertOverwriteTableContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInsertOverwriteTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInsertOverwriteTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInsertOverwriteTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertIntoTableContext extends InsertIntoContext {
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(ArcticExtendSparkSqlParser.INTO, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public InsertIntoTableContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInsertIntoTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInsertIntoTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInsertIntoTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertIntoContext insertInto() throws RecognitionException {
		InsertIntoContext _localctx = new InsertIntoContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_insertInto);
		int _la;
		try {
			setState(1460);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				_localctx = new InsertOverwriteTableContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1399);
				match(INSERT);
				setState(1400);
				match(OVERWRITE);
				setState(1402);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
				case 1:
					{
					setState(1401);
					match(TABLE);
					}
					break;
				}
				setState(1404);
				multipartIdentifier();
				setState(1411);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1405);
					partitionSpec();
					setState(1409);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IF) {
						{
						setState(1406);
						match(IF);
						setState(1407);
						match(NOT);
						setState(1408);
						match(EXISTS);
						}
					}

					}
				}

				setState(1414);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
				case 1:
					{
					setState(1413);
					identifierList();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new InsertIntoTableContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1416);
				match(INSERT);
				setState(1417);
				match(INTO);
				setState(1419);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
				case 1:
					{
					setState(1418);
					match(TABLE);
					}
					break;
				}
				setState(1421);
				multipartIdentifier();
				setState(1423);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1422);
					partitionSpec();
					}
				}

				setState(1428);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(1425);
					match(IF);
					setState(1426);
					match(NOT);
					setState(1427);
					match(EXISTS);
					}
				}

				setState(1431);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
				case 1:
					{
					setState(1430);
					identifierList();
					}
					break;
				}
				}
				break;
			case 3:
				_localctx = new InsertOverwriteHiveDirContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1433);
				match(INSERT);
				setState(1434);
				match(OVERWRITE);
				setState(1436);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1435);
					match(LOCAL);
					}
				}

				setState(1438);
				match(DIRECTORY);
				setState(1439);
				((InsertOverwriteHiveDirContext)_localctx).path = match(STRING);
				setState(1441);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(1440);
					rowFormat();
					}
				}

				setState(1444);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORED) {
					{
					setState(1443);
					createFileFormat();
					}
				}

				}
				break;
			case 4:
				_localctx = new InsertOverwriteDirContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1446);
				match(INSERT);
				setState(1447);
				match(OVERWRITE);
				setState(1449);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1448);
					match(LOCAL);
					}
				}

				setState(1451);
				match(DIRECTORY);
				setState(1453);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING) {
					{
					setState(1452);
					((InsertOverwriteDirContext)_localctx).path = match(STRING);
					}
				}

				setState(1455);
				tableProvider();
				setState(1458);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1456);
					match(OPTIONS);
					setState(1457);
					((InsertOverwriteDirContext)_localctx).options = propertyList();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartitionSpecLocationContext extends ParserRuleContext {
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public PartitionSpecLocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionSpecLocation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPartitionSpecLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPartitionSpecLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPartitionSpecLocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSpecLocationContext partitionSpecLocation() throws RecognitionException {
		PartitionSpecLocationContext _localctx = new PartitionSpecLocationContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_partitionSpecLocation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1462);
			partitionSpec();
			setState(1464);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOCATION) {
				{
				setState(1463);
				locationSpec();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartitionSpecContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(ArcticExtendSparkSqlParser.PARTITION, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<PartitionValContext> partitionVal() {
			return getRuleContexts(PartitionValContext.class);
		}
		public PartitionValContext partitionVal(int i) {
			return getRuleContext(PartitionValContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public PartitionSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPartitionSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPartitionSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPartitionSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSpecContext partitionSpec() throws RecognitionException {
		PartitionSpecContext _localctx = new PartitionSpecContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_partitionSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1466);
			match(PARTITION);
			setState(1467);
			match(LEFT_PAREN);
			setState(1468);
			partitionVal();
			setState(1473);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1469);
				match(COMMA);
				setState(1470);
				partitionVal();
				}
				}
				setState(1475);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1476);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartitionValContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public PartitionValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPartitionVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPartitionVal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPartitionVal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionValContext partitionVal() throws RecognitionException {
		PartitionValContext _localctx = new PartitionValContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_partitionVal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1478);
			identifier();
			setState(1481);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1479);
				match(EQ);
				setState(1480);
				constant();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamespaceContext extends ParserRuleContext {
		public TerminalNode NAMESPACE() { return getToken(ArcticExtendSparkSqlParser.NAMESPACE, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticExtendSparkSqlParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(ArcticExtendSparkSqlParser.SCHEMA, 0); }
		public NamespaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNamespace(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespaceContext namespace() throws RecognitionException {
		NamespaceContext _localctx = new NamespaceContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_namespace);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1483);
			_la = _input.LA(1);
			if ( !(_la==DATABASE || _la==NAMESPACE || _la==SCHEMA) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamespacesContext extends ParserRuleContext {
		public TerminalNode NAMESPACES() { return getToken(ArcticExtendSparkSqlParser.NAMESPACES, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticExtendSparkSqlParser.DATABASES, 0); }
		public TerminalNode SCHEMAS() { return getToken(ArcticExtendSparkSqlParser.SCHEMAS, 0); }
		public NamespacesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespaces; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNamespaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNamespaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNamespaces(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespacesContext namespaces() throws RecognitionException {
		NamespacesContext _localctx = new NamespacesContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_namespaces);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1485);
			_la = _input.LA(1);
			if ( !(_la==DATABASES || _la==NAMESPACES || _la==SCHEMAS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DescribeFuncNameContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public ArithmeticOperatorContext arithmeticOperator() {
			return getRuleContext(ArithmeticOperatorContext.class,0);
		}
		public PredicateOperatorContext predicateOperator() {
			return getRuleContext(PredicateOperatorContext.class,0);
		}
		public DescribeFuncNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describeFuncName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDescribeFuncName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDescribeFuncName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDescribeFuncName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeFuncNameContext describeFuncName() throws RecognitionException {
		DescribeFuncNameContext _localctx = new DescribeFuncNameContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_describeFuncName);
		try {
			setState(1492);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1487);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1488);
				match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1489);
				comparisonOperator();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1490);
				arithmeticOperator();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1491);
				predicateOperator();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DescribeColNameContext extends ParserRuleContext {
		public IdentifierContext identifier;
		public List<IdentifierContext> nameParts = new ArrayList<IdentifierContext>();
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(ArcticExtendSparkSqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticExtendSparkSqlParser.DOT, i);
		}
		public DescribeColNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describeColName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDescribeColName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDescribeColName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDescribeColName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeColNameContext describeColName() throws RecognitionException {
		DescribeColNameContext _localctx = new DescribeColNameContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_describeColName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1494);
			((DescribeColNameContext)_localctx).identifier = identifier();
			((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
			setState(1499);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(1495);
				match(DOT);
				setState(1496);
				((DescribeColNameContext)_localctx).identifier = identifier();
				((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
				}
				}
				setState(1501);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CtesContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public List<NamedQueryContext> namedQuery() {
			return getRuleContexts(NamedQueryContext.class);
		}
		public NamedQueryContext namedQuery(int i) {
			return getRuleContext(NamedQueryContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public CtesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ctes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCtes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCtes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCtes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CtesContext ctes() throws RecognitionException {
		CtesContext _localctx = new CtesContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_ctes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1502);
			match(WITH);
			setState(1503);
			namedQuery();
			setState(1508);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1504);
				match(COMMA);
				setState(1505);
				namedQuery();
				}
				}
				setState(1510);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedQueryContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext name;
		public IdentifierListContext columnAliases;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public NamedQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNamedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNamedQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNamedQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedQueryContext namedQuery() throws RecognitionException {
		NamedQueryContext _localctx = new NamedQueryContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_namedQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1511);
			((NamedQueryContext)_localctx).name = errorCapturingIdentifier();
			setState(1513);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
			case 1:
				{
				setState(1512);
				((NamedQueryContext)_localctx).columnAliases = identifierList();
				}
				break;
			}
			setState(1516);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(1515);
				match(AS);
				}
			}

			setState(1518);
			match(LEFT_PAREN);
			setState(1519);
			query();
			setState(1520);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableProviderContext extends ParserRuleContext {
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableProviderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableProvider; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableProvider(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableProvider(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableProvider(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableProviderContext tableProvider() throws RecognitionException {
		TableProviderContext _localctx = new TableProviderContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_tableProvider);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1522);
			match(USING);
			setState(1523);
			multipartIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateTableClausesContext extends ParserRuleContext {
		public PropertyListContext options;
		public PartitionFieldListContext partitioning;
		public PropertyListContext tableProps;
		public List<SkewSpecContext> skewSpec() {
			return getRuleContexts(SkewSpecContext.class);
		}
		public SkewSpecContext skewSpec(int i) {
			return getRuleContext(SkewSpecContext.class,i);
		}
		public List<BucketSpecContext> bucketSpec() {
			return getRuleContexts(BucketSpecContext.class);
		}
		public BucketSpecContext bucketSpec(int i) {
			return getRuleContext(BucketSpecContext.class,i);
		}
		public List<RowFormatContext> rowFormat() {
			return getRuleContexts(RowFormatContext.class);
		}
		public RowFormatContext rowFormat(int i) {
			return getRuleContext(RowFormatContext.class,i);
		}
		public List<CreateFileFormatContext> createFileFormat() {
			return getRuleContexts(CreateFileFormatContext.class);
		}
		public CreateFileFormatContext createFileFormat(int i) {
			return getRuleContext(CreateFileFormatContext.class,i);
		}
		public List<LocationSpecContext> locationSpec() {
			return getRuleContexts(LocationSpecContext.class);
		}
		public LocationSpecContext locationSpec(int i) {
			return getRuleContext(LocationSpecContext.class,i);
		}
		public List<CommentSpecContext> commentSpec() {
			return getRuleContexts(CommentSpecContext.class);
		}
		public CommentSpecContext commentSpec(int i) {
			return getRuleContext(CommentSpecContext.class,i);
		}
		public List<TerminalNode> OPTIONS() { return getTokens(ArcticExtendSparkSqlParser.OPTIONS); }
		public TerminalNode OPTIONS(int i) {
			return getToken(ArcticExtendSparkSqlParser.OPTIONS, i);
		}
		public List<TerminalNode> PARTITIONED() { return getTokens(ArcticExtendSparkSqlParser.PARTITIONED); }
		public TerminalNode PARTITIONED(int i) {
			return getToken(ArcticExtendSparkSqlParser.PARTITIONED, i);
		}
		public List<TerminalNode> BY() { return getTokens(ArcticExtendSparkSqlParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticExtendSparkSqlParser.BY, i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(ArcticExtendSparkSqlParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public List<PartitionFieldListContext> partitionFieldList() {
			return getRuleContexts(PartitionFieldListContext.class);
		}
		public PartitionFieldListContext partitionFieldList(int i) {
			return getRuleContext(PartitionFieldListContext.class,i);
		}
		public CreateTableClausesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableClauses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateTableClauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateTableClauses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateTableClauses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableClausesContext createTableClauses() throws RecognitionException {
		CreateTableClausesContext _localctx = new CreateTableClausesContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_createTableClauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1540);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CLUSTERED || _la==COMMENT || ((((_la - 143)) & ~0x3f) == 0 && ((1L << (_la - 143)) & ((1L << (LOCATION - 143)) | (1L << (OPTIONS - 143)) | (1L << (PARTITIONED - 143)))) != 0) || ((((_la - 214)) & ~0x3f) == 0 && ((1L << (_la - 214)) & ((1L << (ROW - 214)) | (1L << (SKEWED - 214)) | (1L << (STORED - 214)) | (1L << (TBLPROPERTIES - 214)))) != 0)) {
				{
				setState(1538);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case OPTIONS:
					{
					{
					setState(1525);
					match(OPTIONS);
					setState(1526);
					((CreateTableClausesContext)_localctx).options = propertyList();
					}
					}
					break;
				case PARTITIONED:
					{
					{
					setState(1527);
					match(PARTITIONED);
					setState(1528);
					match(BY);
					setState(1529);
					((CreateTableClausesContext)_localctx).partitioning = partitionFieldList();
					}
					}
					break;
				case SKEWED:
					{
					setState(1530);
					skewSpec();
					}
					break;
				case CLUSTERED:
					{
					setState(1531);
					bucketSpec();
					}
					break;
				case ROW:
					{
					setState(1532);
					rowFormat();
					}
					break;
				case STORED:
					{
					setState(1533);
					createFileFormat();
					}
					break;
				case LOCATION:
					{
					setState(1534);
					locationSpec();
					}
					break;
				case COMMENT:
					{
					setState(1535);
					commentSpec();
					}
					break;
				case TBLPROPERTIES:
					{
					{
					setState(1536);
					match(TBLPROPERTIES);
					setState(1537);
					((CreateTableClausesContext)_localctx).tableProps = propertyList();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(1542);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<PropertyContext> property() {
			return getRuleContexts(PropertyContext.class);
		}
		public PropertyContext property(int i) {
			return getRuleContext(PropertyContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public PropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyListContext propertyList() throws RecognitionException {
		PropertyListContext _localctx = new PropertyListContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_propertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1543);
			match(LEFT_PAREN);
			setState(1544);
			property();
			setState(1549);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1545);
				match(COMMA);
				setState(1546);
				property();
				}
				}
				setState(1551);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1552);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyContext extends ParserRuleContext {
		public PropertyKeyContext key;
		public PropertyValueContext value;
		public PropertyKeyContext propertyKey() {
			return getRuleContext(PropertyKeyContext.class,0);
		}
		public PropertyValueContext propertyValue() {
			return getRuleContext(PropertyValueContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_property);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1554);
			((PropertyContext)_localctx).key = propertyKey();
			setState(1559);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FALSE || ((((_la - 261)) & ~0x3f) == 0 && ((1L << (_la - 261)) & ((1L << (TRUE - 261)) | (1L << (EQ - 261)) | (1L << (STRING - 261)) | (1L << (INTEGER_VALUE - 261)) | (1L << (DECIMAL_VALUE - 261)))) != 0)) {
				{
				setState(1556);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1555);
					match(EQ);
					}
				}

				setState(1558);
				((PropertyContext)_localctx).value = propertyValue();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyKeyContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(ArcticExtendSparkSqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticExtendSparkSqlParser.DOT, i);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public PropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPropertyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPropertyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyKeyContext propertyKey() throws RecognitionException {
		PropertyKeyContext _localctx = new PropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_propertyKey);
		int _la;
		try {
			setState(1570);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,170,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1561);
				identifier();
				setState(1566);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(1562);
					match(DOT);
					setState(1563);
					identifier();
					}
					}
					setState(1568);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1569);
				match(STRING);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyValueContext extends ParserRuleContext {
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticExtendSparkSqlParser.DECIMAL_VALUE, 0); }
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_propertyValue);
		try {
			setState(1576);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_VALUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1572);
				match(INTEGER_VALUE);
				}
				break;
			case DECIMAL_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1573);
				match(DECIMAL_VALUE);
				}
				break;
			case FALSE:
			case TRUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1574);
				booleanValue();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(1575);
				match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public ConstantListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterConstantList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitConstantList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitConstantList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantListContext constantList() throws RecognitionException {
		ConstantListContext _localctx = new ConstantListContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_constantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1578);
			match(LEFT_PAREN);
			setState(1579);
			constant();
			setState(1584);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1580);
				match(COMMA);
				setState(1581);
				constant();
				}
				}
				setState(1586);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1587);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NestedConstantListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<ConstantListContext> constantList() {
			return getRuleContexts(ConstantListContext.class);
		}
		public ConstantListContext constantList(int i) {
			return getRuleContext(ConstantListContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public NestedConstantListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedConstantList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNestedConstantList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNestedConstantList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNestedConstantList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedConstantListContext nestedConstantList() throws RecognitionException {
		NestedConstantListContext _localctx = new NestedConstantListContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_nestedConstantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1589);
			match(LEFT_PAREN);
			setState(1590);
			constantList();
			setState(1595);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1591);
				match(COMMA);
				setState(1592);
				constantList();
				}
				}
				setState(1597);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1598);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateFileFormatContext extends ParserRuleContext {
		public TerminalNode STORED() { return getToken(ArcticExtendSparkSqlParser.STORED, 0); }
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public FileFormatContext fileFormat() {
			return getRuleContext(FileFormatContext.class,0);
		}
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public StorageHandlerContext storageHandler() {
			return getRuleContext(StorageHandlerContext.class,0);
		}
		public CreateFileFormatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createFileFormat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCreateFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCreateFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCreateFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateFileFormatContext createFileFormat() throws RecognitionException {
		CreateFileFormatContext _localctx = new CreateFileFormatContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_createFileFormat);
		try {
			setState(1606);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1600);
				match(STORED);
				setState(1601);
				match(AS);
				setState(1602);
				fileFormat();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1603);
				match(STORED);
				setState(1604);
				match(BY);
				setState(1605);
				storageHandler();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FileFormatContext extends ParserRuleContext {
		public FileFormatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fileFormat; }
	 
		public FileFormatContext() { }
		public void copyFrom(FileFormatContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TableFileFormatContext extends FileFormatContext {
		public Token inFmt;
		public Token outFmt;
		public TerminalNode INPUTFORMAT() { return getToken(ArcticExtendSparkSqlParser.INPUTFORMAT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(ArcticExtendSparkSqlParser.OUTPUTFORMAT, 0); }
		public List<TerminalNode> STRING() { return getTokens(ArcticExtendSparkSqlParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticExtendSparkSqlParser.STRING, i);
		}
		public TableFileFormatContext(FileFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class GenericFileFormatContext extends FileFormatContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public GenericFileFormatContext(FileFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterGenericFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitGenericFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitGenericFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileFormatContext fileFormat() throws RecognitionException {
		FileFormatContext _localctx = new FileFormatContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_fileFormat);
		try {
			setState(1613);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
			case 1:
				_localctx = new TableFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1608);
				match(INPUTFORMAT);
				setState(1609);
				((TableFileFormatContext)_localctx).inFmt = match(STRING);
				setState(1610);
				match(OUTPUTFORMAT);
				setState(1611);
				((TableFileFormatContext)_localctx).outFmt = match(STRING);
				}
				break;
			case 2:
				_localctx = new GenericFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1612);
				identifier();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StorageHandlerContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public StorageHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_storageHandler; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterStorageHandler(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitStorageHandler(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitStorageHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StorageHandlerContext storageHandler() throws RecognitionException {
		StorageHandlerContext _localctx = new StorageHandlerContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_storageHandler);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1615);
			match(STRING);
			setState(1619);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				{
				setState(1616);
				match(WITH);
				setState(1617);
				match(SERDEPROPERTIES);
				setState(1618);
				propertyList();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResourceContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_resource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1621);
			identifier();
			setState(1622);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DmlStatementNoWithContext extends ParserRuleContext {
		public DmlStatementNoWithContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dmlStatementNoWith; }
	 
		public DmlStatementNoWithContext() { }
		public void copyFrom(DmlStatementNoWithContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DeleteFromTableContext extends DmlStatementNoWithContext {
		public TerminalNode DELETE() { return getToken(ArcticExtendSparkSqlParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public DeleteFromTableContext(DmlStatementNoWithContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDeleteFromTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDeleteFromTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDeleteFromTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleInsertQueryContext extends DmlStatementNoWithContext {
		public InsertIntoContext insertInto() {
			return getRuleContext(InsertIntoContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public SingleInsertQueryContext(DmlStatementNoWithContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSingleInsertQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSingleInsertQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSingleInsertQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class MultiInsertQueryContext extends DmlStatementNoWithContext {
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public List<MultiInsertQueryBodyContext> multiInsertQueryBody() {
			return getRuleContexts(MultiInsertQueryBodyContext.class);
		}
		public MultiInsertQueryBodyContext multiInsertQueryBody(int i) {
			return getRuleContext(MultiInsertQueryBodyContext.class,i);
		}
		public MultiInsertQueryContext(DmlStatementNoWithContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultiInsertQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultiInsertQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultiInsertQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UpdateTableContext extends DmlStatementNoWithContext {
		public TerminalNode UPDATE() { return getToken(ArcticExtendSparkSqlParser.UPDATE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SetClauseContext setClause() {
			return getRuleContext(SetClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public UpdateTableContext(DmlStatementNoWithContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUpdateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUpdateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUpdateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class MergeIntoTableContext extends DmlStatementNoWithContext {
		public MultipartIdentifierContext target;
		public TableAliasContext targetAlias;
		public MultipartIdentifierContext source;
		public QueryContext sourceQuery;
		public TableAliasContext sourceAlias;
		public BooleanExpressionContext mergeCondition;
		public TerminalNode MERGE() { return getToken(ArcticExtendSparkSqlParser.MERGE, 0); }
		public TerminalNode INTO() { return getToken(ArcticExtendSparkSqlParser.INTO, 0); }
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public List<TableAliasContext> tableAlias() {
			return getRuleContexts(TableAliasContext.class);
		}
		public TableAliasContext tableAlias(int i) {
			return getRuleContext(TableAliasContext.class,i);
		}
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public List<MatchedClauseContext> matchedClause() {
			return getRuleContexts(MatchedClauseContext.class);
		}
		public MatchedClauseContext matchedClause(int i) {
			return getRuleContext(MatchedClauseContext.class,i);
		}
		public List<NotMatchedClauseContext> notMatchedClause() {
			return getRuleContexts(NotMatchedClauseContext.class);
		}
		public NotMatchedClauseContext notMatchedClause(int i) {
			return getRuleContext(NotMatchedClauseContext.class,i);
		}
		public MergeIntoTableContext(DmlStatementNoWithContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMergeIntoTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMergeIntoTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMergeIntoTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DmlStatementNoWithContext dmlStatementNoWith() throws RecognitionException {
		DmlStatementNoWithContext _localctx = new DmlStatementNoWithContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_dmlStatementNoWith);
		int _la;
		try {
			int _alt;
			setState(1674);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INSERT:
				_localctx = new SingleInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1624);
				insertInto();
				setState(1625);
				query();
				}
				break;
			case FROM:
				_localctx = new MultiInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1627);
				fromClause();
				setState(1629); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1628);
					multiInsertQueryBody();
					}
					}
					setState(1631); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==INSERT );
				}
				break;
			case DELETE:
				_localctx = new DeleteFromTableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1633);
				match(DELETE);
				setState(1634);
				match(FROM);
				setState(1635);
				multipartIdentifier();
				setState(1636);
				tableAlias();
				setState(1638);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1637);
					whereClause();
					}
				}

				}
				break;
			case UPDATE:
				_localctx = new UpdateTableContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1640);
				match(UPDATE);
				setState(1641);
				multipartIdentifier();
				setState(1642);
				tableAlias();
				setState(1643);
				setClause();
				setState(1645);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1644);
					whereClause();
					}
				}

				}
				break;
			case MERGE:
				_localctx = new MergeIntoTableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1647);
				match(MERGE);
				setState(1648);
				match(INTO);
				setState(1649);
				((MergeIntoTableContext)_localctx).target = multipartIdentifier();
				setState(1650);
				((MergeIntoTableContext)_localctx).targetAlias = tableAlias();
				setState(1651);
				match(USING);
				setState(1657);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
				case 1:
					{
					setState(1652);
					((MergeIntoTableContext)_localctx).source = multipartIdentifier();
					}
					break;
				case 2:
					{
					setState(1653);
					match(LEFT_PAREN);
					setState(1654);
					((MergeIntoTableContext)_localctx).sourceQuery = query();
					setState(1655);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(1659);
				((MergeIntoTableContext)_localctx).sourceAlias = tableAlias();
				setState(1660);
				match(ON);
				setState(1661);
				((MergeIntoTableContext)_localctx).mergeCondition = booleanExpression(0);
				setState(1665);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1662);
						matchedClause();
						}
						} 
					}
					setState(1667);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
				}
				setState(1671);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(1668);
					notMatchedClause();
					}
					}
					setState(1673);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QueryOrganizationContext extends ParserRuleContext {
		public SortItemContext sortItem;
		public List<SortItemContext> order = new ArrayList<SortItemContext>();
		public ExpressionContext expression;
		public List<ExpressionContext> clusterBy = new ArrayList<ExpressionContext>();
		public List<ExpressionContext> distributeBy = new ArrayList<ExpressionContext>();
		public List<SortItemContext> sort = new ArrayList<SortItemContext>();
		public ExpressionContext limit;
		public TerminalNode ORDER() { return getToken(ArcticExtendSparkSqlParser.ORDER, 0); }
		public List<TerminalNode> BY() { return getTokens(ArcticExtendSparkSqlParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticExtendSparkSqlParser.BY, i);
		}
		public TerminalNode CLUSTER() { return getToken(ArcticExtendSparkSqlParser.CLUSTER, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticExtendSparkSqlParser.DISTRIBUTE, 0); }
		public TerminalNode SORT() { return getToken(ArcticExtendSparkSqlParser.SORT, 0); }
		public WindowClauseContext windowClause() {
			return getRuleContext(WindowClauseContext.class,0);
		}
		public TerminalNode LIMIT() { return getToken(ArcticExtendSparkSqlParser.LIMIT, 0); }
		public List<SortItemContext> sortItem() {
			return getRuleContexts(SortItemContext.class);
		}
		public SortItemContext sortItem(int i) {
			return getRuleContext(SortItemContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ALL() { return getToken(ArcticExtendSparkSqlParser.ALL, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public QueryOrganizationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryOrganization; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQueryOrganization(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQueryOrganization(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQueryOrganization(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryOrganizationContext queryOrganization() throws RecognitionException {
		QueryOrganizationContext _localctx = new QueryOrganizationContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_queryOrganization);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1686);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,185,_ctx) ) {
			case 1:
				{
				setState(1676);
				match(ORDER);
				setState(1677);
				match(BY);
				setState(1678);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1683);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1679);
						match(COMMA);
						setState(1680);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1685);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
				}
				}
				break;
			}
			setState(1698);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
			case 1:
				{
				setState(1688);
				match(CLUSTER);
				setState(1689);
				match(BY);
				setState(1690);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1695);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1691);
						match(COMMA);
						setState(1692);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1697);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				}
				}
				break;
			}
			setState(1710);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
			case 1:
				{
				setState(1700);
				match(DISTRIBUTE);
				setState(1701);
				match(BY);
				setState(1702);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1707);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1703);
						match(COMMA);
						setState(1704);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1709);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
				}
				}
				break;
			}
			setState(1722);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,191,_ctx) ) {
			case 1:
				{
				setState(1712);
				match(SORT);
				setState(1713);
				match(BY);
				setState(1714);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1719);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,190,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1715);
						match(COMMA);
						setState(1716);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1721);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,190,_ctx);
				}
				}
				break;
			}
			setState(1725);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				{
				setState(1724);
				windowClause();
				}
				break;
			}
			setState(1732);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
			case 1:
				{
				setState(1727);
				match(LIMIT);
				setState(1730);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
				case 1:
					{
					setState(1728);
					match(ALL);
					}
					break;
				case 2:
					{
					setState(1729);
					((QueryOrganizationContext)_localctx).limit = expression();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultiInsertQueryBodyContext extends ParserRuleContext {
		public InsertIntoContext insertInto() {
			return getRuleContext(InsertIntoContext.class,0);
		}
		public FromStatementBodyContext fromStatementBody() {
			return getRuleContext(FromStatementBodyContext.class,0);
		}
		public MultiInsertQueryBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiInsertQueryBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultiInsertQueryBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultiInsertQueryBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultiInsertQueryBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiInsertQueryBodyContext multiInsertQueryBody() throws RecognitionException {
		MultiInsertQueryBodyContext _localctx = new MultiInsertQueryBodyContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_multiInsertQueryBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1734);
			insertInto();
			setState(1735);
			fromStatementBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QueryTermContext extends ParserRuleContext {
		public QueryTermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryTerm; }
	 
		public QueryTermContext() { }
		public void copyFrom(QueryTermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class QueryTermDefaultContext extends QueryTermContext {
		public QueryPrimaryContext queryPrimary() {
			return getRuleContext(QueryPrimaryContext.class,0);
		}
		public QueryTermDefaultContext(QueryTermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQueryTermDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQueryTermDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQueryTermDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetOperationContext extends QueryTermContext {
		public QueryTermContext left;
		public Token operator;
		public QueryTermContext right;
		public List<QueryTermContext> queryTerm() {
			return getRuleContexts(QueryTermContext.class);
		}
		public QueryTermContext queryTerm(int i) {
			return getRuleContext(QueryTermContext.class,i);
		}
		public TerminalNode INTERSECT() { return getToken(ArcticExtendSparkSqlParser.INTERSECT, 0); }
		public TerminalNode UNION() { return getToken(ArcticExtendSparkSqlParser.UNION, 0); }
		public TerminalNode EXCEPT() { return getToken(ArcticExtendSparkSqlParser.EXCEPT, 0); }
		public TerminalNode SETMINUS() { return getToken(ArcticExtendSparkSqlParser.SETMINUS, 0); }
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public SetOperationContext(QueryTermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryTermContext queryTerm() throws RecognitionException {
		return queryTerm(0);
	}

	private QueryTermContext queryTerm(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		QueryTermContext _localctx = new QueryTermContext(_ctx, _parentState);
		QueryTermContext _prevctx = _localctx;
		int _startState = 96;
		enterRecursionRule(_localctx, 96, RULE_queryTerm, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new QueryTermDefaultContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(1738);
			queryPrimary();
			}
			_ctx.stop = _input.LT(-1);
			setState(1763);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,199,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1761);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,198,_ctx) ) {
					case 1:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1740);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1741);
						if (!(legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "legacy_setops_precedence_enabled");
						setState(1742);
						((SetOperationContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EXCEPT || _la==INTERSECT || _la==SETMINUS || _la==UNION) ) {
							((SetOperationContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1744);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1743);
							setQuantifier();
							}
						}

						setState(1746);
						((SetOperationContext)_localctx).right = queryTerm(4);
						}
						break;
					case 2:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1747);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1748);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1749);
						((SetOperationContext)_localctx).operator = match(INTERSECT);
						setState(1751);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1750);
							setQuantifier();
							}
						}

						setState(1753);
						((SetOperationContext)_localctx).right = queryTerm(3);
						}
						break;
					case 3:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1754);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(1755);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1756);
						((SetOperationContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EXCEPT || _la==SETMINUS || _la==UNION) ) {
							((SetOperationContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1758);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1757);
							setQuantifier();
							}
						}

						setState(1760);
						((SetOperationContext)_localctx).right = queryTerm(2);
						}
						break;
					}
					} 
				}
				setState(1765);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,199,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class QueryPrimaryContext extends ParserRuleContext {
		public QueryPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryPrimary; }
	 
		public QueryPrimaryContext() { }
		public void copyFrom(QueryPrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SubqueryContext extends QueryPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public SubqueryContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSubquery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class QueryPrimaryDefaultContext extends QueryPrimaryContext {
		public QuerySpecificationContext querySpecification() {
			return getRuleContext(QuerySpecificationContext.class,0);
		}
		public QueryPrimaryDefaultContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQueryPrimaryDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQueryPrimaryDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQueryPrimaryDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InlineTableDefault1Context extends QueryPrimaryContext {
		public InlineTableContext inlineTable() {
			return getRuleContext(InlineTableContext.class,0);
		}
		public InlineTableDefault1Context(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInlineTableDefault1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInlineTableDefault1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInlineTableDefault1(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FromStmtContext extends QueryPrimaryContext {
		public FromStatementContext fromStatement() {
			return getRuleContext(FromStatementContext.class,0);
		}
		public FromStmtContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFromStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFromStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFromStmt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TableContext extends QueryPrimaryContext {
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryPrimaryContext queryPrimary() throws RecognitionException {
		QueryPrimaryContext _localctx = new QueryPrimaryContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_queryPrimary);
		try {
			setState(1775);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAP:
			case REDUCE:
			case SELECT:
				_localctx = new QueryPrimaryDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1766);
				querySpecification();
				}
				break;
			case FROM:
				_localctx = new FromStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1767);
				fromStatement();
				}
				break;
			case TABLE:
				_localctx = new TableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1768);
				match(TABLE);
				setState(1769);
				multipartIdentifier();
				}
				break;
			case VALUES:
				_localctx = new InlineTableDefault1Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1770);
				inlineTable();
				}
				break;
			case LEFT_PAREN:
				_localctx = new SubqueryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1771);
				match(LEFT_PAREN);
				setState(1772);
				query();
				setState(1773);
				match(RIGHT_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SortItemContext extends ParserRuleContext {
		public Token ordering;
		public Token nullOrder;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NULLS() { return getToken(ArcticExtendSparkSqlParser.NULLS, 0); }
		public TerminalNode ASC() { return getToken(ArcticExtendSparkSqlParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode LAST() { return getToken(ArcticExtendSparkSqlParser.LAST, 0); }
		public TerminalNode FIRST() { return getToken(ArcticExtendSparkSqlParser.FIRST, 0); }
		public SortItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSortItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSortItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSortItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortItemContext sortItem() throws RecognitionException {
		SortItemContext _localctx = new SortItemContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_sortItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1777);
			expression();
			setState(1779);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,201,_ctx) ) {
			case 1:
				{
				setState(1778);
				((SortItemContext)_localctx).ordering = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
					((SortItemContext)_localctx).ordering = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			setState(1783);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
			case 1:
				{
				setState(1781);
				match(NULLS);
				setState(1782);
				((SortItemContext)_localctx).nullOrder = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==FIRST || _la==LAST) ) {
					((SortItemContext)_localctx).nullOrder = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromStatementContext extends ParserRuleContext {
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public List<FromStatementBodyContext> fromStatementBody() {
			return getRuleContexts(FromStatementBodyContext.class);
		}
		public FromStatementBodyContext fromStatementBody(int i) {
			return getRuleContext(FromStatementBodyContext.class,i);
		}
		public FromStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFromStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFromStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFromStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromStatementContext fromStatement() throws RecognitionException {
		FromStatementContext _localctx = new FromStatementContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_fromStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1785);
			fromClause();
			setState(1787); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1786);
					fromStatementBody();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1789); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,203,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromStatementBodyContext extends ParserRuleContext {
		public TransformClauseContext transformClause() {
			return getRuleContext(TransformClauseContext.class,0);
		}
		public QueryOrganizationContext queryOrganization() {
			return getRuleContext(QueryOrganizationContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public List<LateralViewContext> lateralView() {
			return getRuleContexts(LateralViewContext.class);
		}
		public LateralViewContext lateralView(int i) {
			return getRuleContext(LateralViewContext.class,i);
		}
		public AggregationClauseContext aggregationClause() {
			return getRuleContext(AggregationClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public WindowClauseContext windowClause() {
			return getRuleContext(WindowClauseContext.class,0);
		}
		public FromStatementBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromStatementBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFromStatementBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFromStatementBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFromStatementBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromStatementBodyContext fromStatementBody() throws RecognitionException {
		FromStatementBodyContext _localctx = new FromStatementBodyContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_fromStatementBody);
		try {
			int _alt;
			setState(1818);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1791);
				transformClause();
				setState(1793);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
				case 1:
					{
					setState(1792);
					whereClause();
					}
					break;
				}
				setState(1795);
				queryOrganization();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1797);
				selectClause();
				setState(1801);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1798);
						lateralView();
						}
						} 
					}
					setState(1803);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				}
				setState(1805);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
				case 1:
					{
					setState(1804);
					whereClause();
					}
					break;
				}
				setState(1808);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(1807);
					aggregationClause();
					}
					break;
				}
				setState(1811);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
				case 1:
					{
					setState(1810);
					havingClause();
					}
					break;
				}
				setState(1814);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
				case 1:
					{
					setState(1813);
					windowClause();
					}
					break;
				}
				setState(1816);
				queryOrganization();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuerySpecificationContext extends ParserRuleContext {
		public QuerySpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_querySpecification; }
	 
		public QuerySpecificationContext() { }
		public void copyFrom(QuerySpecificationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RegularQuerySpecificationContext extends QuerySpecificationContext {
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public List<LateralViewContext> lateralView() {
			return getRuleContexts(LateralViewContext.class);
		}
		public LateralViewContext lateralView(int i) {
			return getRuleContext(LateralViewContext.class,i);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public AggregationClauseContext aggregationClause() {
			return getRuleContext(AggregationClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public WindowClauseContext windowClause() {
			return getRuleContext(WindowClauseContext.class,0);
		}
		public RegularQuerySpecificationContext(QuerySpecificationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRegularQuerySpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRegularQuerySpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRegularQuerySpecification(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TransformQuerySpecificationContext extends QuerySpecificationContext {
		public TransformClauseContext transformClause() {
			return getRuleContext(TransformClauseContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public List<LateralViewContext> lateralView() {
			return getRuleContexts(LateralViewContext.class);
		}
		public LateralViewContext lateralView(int i) {
			return getRuleContext(LateralViewContext.class,i);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public AggregationClauseContext aggregationClause() {
			return getRuleContext(AggregationClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public WindowClauseContext windowClause() {
			return getRuleContext(WindowClauseContext.class,0);
		}
		public TransformQuerySpecificationContext(QuerySpecificationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTransformQuerySpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTransformQuerySpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTransformQuerySpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuerySpecificationContext querySpecification() throws RecognitionException {
		QuerySpecificationContext _localctx = new QuerySpecificationContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_querySpecification);
		try {
			int _alt;
			setState(1864);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,223,_ctx) ) {
			case 1:
				_localctx = new TransformQuerySpecificationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1820);
				transformClause();
				setState(1822);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
				case 1:
					{
					setState(1821);
					fromClause();
					}
					break;
				}
				setState(1827);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,212,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1824);
						lateralView();
						}
						} 
					}
					setState(1829);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,212,_ctx);
				}
				setState(1831);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
				case 1:
					{
					setState(1830);
					whereClause();
					}
					break;
				}
				setState(1834);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,214,_ctx) ) {
				case 1:
					{
					setState(1833);
					aggregationClause();
					}
					break;
				}
				setState(1837);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,215,_ctx) ) {
				case 1:
					{
					setState(1836);
					havingClause();
					}
					break;
				}
				setState(1840);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,216,_ctx) ) {
				case 1:
					{
					setState(1839);
					windowClause();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new RegularQuerySpecificationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1842);
				selectClause();
				setState(1844);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,217,_ctx) ) {
				case 1:
					{
					setState(1843);
					fromClause();
					}
					break;
				}
				setState(1849);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,218,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1846);
						lateralView();
						}
						} 
					}
					setState(1851);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,218,_ctx);
				}
				setState(1853);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,219,_ctx) ) {
				case 1:
					{
					setState(1852);
					whereClause();
					}
					break;
				}
				setState(1856);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
				case 1:
					{
					setState(1855);
					aggregationClause();
					}
					break;
				}
				setState(1859);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,221,_ctx) ) {
				case 1:
					{
					setState(1858);
					havingClause();
					}
					break;
				}
				setState(1862);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
				case 1:
					{
					setState(1861);
					windowClause();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TransformClauseContext extends ParserRuleContext {
		public Token kind;
		public RowFormatContext inRowFormat;
		public Token recordWriter;
		public Token script;
		public RowFormatContext outRowFormat;
		public Token recordReader;
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public List<TerminalNode> STRING() { return getTokens(ArcticExtendSparkSqlParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticExtendSparkSqlParser.STRING, i);
		}
		public TerminalNode SELECT() { return getToken(ArcticExtendSparkSqlParser.SELECT, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, i);
		}
		public ExpressionSeqContext expressionSeq() {
			return getRuleContext(ExpressionSeqContext.class,0);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, i);
		}
		public TerminalNode TRANSFORM() { return getToken(ArcticExtendSparkSqlParser.TRANSFORM, 0); }
		public TerminalNode MAP() { return getToken(ArcticExtendSparkSqlParser.MAP, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticExtendSparkSqlParser.REDUCE, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticExtendSparkSqlParser.RECORDWRITER, 0); }
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticExtendSparkSqlParser.RECORDREADER, 0); }
		public List<RowFormatContext> rowFormat() {
			return getRuleContexts(RowFormatContext.class);
		}
		public RowFormatContext rowFormat(int i) {
			return getRuleContext(RowFormatContext.class,i);
		}
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TransformClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transformClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTransformClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTransformClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTransformClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformClauseContext transformClause() throws RecognitionException {
		TransformClauseContext _localctx = new TransformClauseContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_transformClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1885);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(1866);
				match(SELECT);
				setState(1867);
				((TransformClauseContext)_localctx).kind = match(TRANSFORM);
				setState(1868);
				match(LEFT_PAREN);
				setState(1870);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,224,_ctx) ) {
				case 1:
					{
					setState(1869);
					setQuantifier();
					}
					break;
				}
				setState(1872);
				expressionSeq();
				setState(1873);
				match(RIGHT_PAREN);
				}
				break;
			case MAP:
				{
				setState(1875);
				((TransformClauseContext)_localctx).kind = match(MAP);
				setState(1877);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,225,_ctx) ) {
				case 1:
					{
					setState(1876);
					setQuantifier();
					}
					break;
				}
				setState(1879);
				expressionSeq();
				}
				break;
			case REDUCE:
				{
				setState(1880);
				((TransformClauseContext)_localctx).kind = match(REDUCE);
				setState(1882);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,226,_ctx) ) {
				case 1:
					{
					setState(1881);
					setQuantifier();
					}
					break;
				}
				setState(1884);
				expressionSeq();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1888);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROW) {
				{
				setState(1887);
				((TransformClauseContext)_localctx).inRowFormat = rowFormat();
				}
			}

			setState(1892);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECORDWRITER) {
				{
				setState(1890);
				match(RECORDWRITER);
				setState(1891);
				((TransformClauseContext)_localctx).recordWriter = match(STRING);
				}
			}

			setState(1894);
			match(USING);
			setState(1895);
			((TransformClauseContext)_localctx).script = match(STRING);
			setState(1908);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(1896);
				match(AS);
				setState(1906);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,231,_ctx) ) {
				case 1:
					{
					setState(1897);
					identifierSeq();
					}
					break;
				case 2:
					{
					setState(1898);
					colTypeList();
					}
					break;
				case 3:
					{
					{
					setState(1899);
					match(LEFT_PAREN);
					setState(1902);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
					case 1:
						{
						setState(1900);
						identifierSeq();
						}
						break;
					case 2:
						{
						setState(1901);
						colTypeList();
						}
						break;
					}
					setState(1904);
					match(RIGHT_PAREN);
					}
					}
					break;
				}
				}
				break;
			}
			setState(1911);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,233,_ctx) ) {
			case 1:
				{
				setState(1910);
				((TransformClauseContext)_localctx).outRowFormat = rowFormat();
				}
				break;
			}
			setState(1915);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,234,_ctx) ) {
			case 1:
				{
				setState(1913);
				match(RECORDREADER);
				setState(1914);
				((TransformClauseContext)_localctx).recordReader = match(STRING);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectClauseContext extends ParserRuleContext {
		public HintContext hint;
		public List<HintContext> hints = new ArrayList<HintContext>();
		public TerminalNode SELECT() { return getToken(ArcticExtendSparkSqlParser.SELECT, 0); }
		public NamedExpressionSeqContext namedExpressionSeq() {
			return getRuleContext(NamedExpressionSeqContext.class,0);
		}
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public List<HintContext> hint() {
			return getRuleContexts(HintContext.class);
		}
		public HintContext hint(int i) {
			return getRuleContext(HintContext.class,i);
		}
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_selectClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1917);
			match(SELECT);
			setState(1921);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,235,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1918);
					((SelectClauseContext)_localctx).hint = hint();
					((SelectClauseContext)_localctx).hints.add(((SelectClauseContext)_localctx).hint);
					}
					} 
				}
				setState(1923);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,235,_ctx);
			}
			setState(1925);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,236,_ctx) ) {
			case 1:
				{
				setState(1924);
				setQuantifier();
				}
				break;
			}
			setState(1927);
			namedExpressionSeq();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SetClauseContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public SetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetClauseContext setClause() throws RecognitionException {
		SetClauseContext _localctx = new SetClauseContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_setClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1929);
			match(SET);
			setState(1930);
			assignmentList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchedClauseContext extends ParserRuleContext {
		public BooleanExpressionContext matchedCond;
		public TerminalNode WHEN() { return getToken(ArcticExtendSparkSqlParser.WHEN, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticExtendSparkSqlParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(ArcticExtendSparkSqlParser.THEN, 0); }
		public MatchedActionContext matchedAction() {
			return getRuleContext(MatchedActionContext.class,0);
		}
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public MatchedClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchedClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMatchedClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMatchedClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMatchedClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchedClauseContext matchedClause() throws RecognitionException {
		MatchedClauseContext _localctx = new MatchedClauseContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_matchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1932);
			match(WHEN);
			setState(1933);
			match(MATCHED);
			setState(1936);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1934);
				match(AND);
				setState(1935);
				((MatchedClauseContext)_localctx).matchedCond = booleanExpression(0);
				}
			}

			setState(1938);
			match(THEN);
			setState(1939);
			matchedAction();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotMatchedClauseContext extends ParserRuleContext {
		public BooleanExpressionContext notMatchedCond;
		public TerminalNode WHEN() { return getToken(ArcticExtendSparkSqlParser.WHEN, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticExtendSparkSqlParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(ArcticExtendSparkSqlParser.THEN, 0); }
		public NotMatchedActionContext notMatchedAction() {
			return getRuleContext(NotMatchedActionContext.class,0);
		}
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public NotMatchedClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notMatchedClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNotMatchedClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNotMatchedClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNotMatchedClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotMatchedClauseContext notMatchedClause() throws RecognitionException {
		NotMatchedClauseContext _localctx = new NotMatchedClauseContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_notMatchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1941);
			match(WHEN);
			setState(1942);
			match(NOT);
			setState(1943);
			match(MATCHED);
			setState(1946);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1944);
				match(AND);
				setState(1945);
				((NotMatchedClauseContext)_localctx).notMatchedCond = booleanExpression(0);
				}
			}

			setState(1948);
			match(THEN);
			setState(1949);
			notMatchedAction();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchedActionContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(ArcticExtendSparkSqlParser.DELETE, 0); }
		public TerminalNode UPDATE() { return getToken(ArcticExtendSparkSqlParser.UPDATE, 0); }
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticExtendSparkSqlParser.ASTERISK, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public MatchedActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchedAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMatchedAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMatchedAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMatchedAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchedActionContext matchedAction() throws RecognitionException {
		MatchedActionContext _localctx = new MatchedActionContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_matchedAction);
		try {
			setState(1958);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,239,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1951);
				match(DELETE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1952);
				match(UPDATE);
				setState(1953);
				match(SET);
				setState(1954);
				match(ASTERISK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1955);
				match(UPDATE);
				setState(1956);
				match(SET);
				setState(1957);
				assignmentList();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotMatchedActionContext extends ParserRuleContext {
		public MultipartIdentifierListContext columns;
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticExtendSparkSqlParser.ASTERISK, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, i);
		}
		public TerminalNode VALUES() { return getToken(ArcticExtendSparkSqlParser.VALUES, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public NotMatchedActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notMatchedAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNotMatchedAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNotMatchedAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNotMatchedAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotMatchedActionContext notMatchedAction() throws RecognitionException {
		NotMatchedActionContext _localctx = new NotMatchedActionContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_notMatchedAction);
		int _la;
		try {
			setState(1978);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,241,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1960);
				match(INSERT);
				setState(1961);
				match(ASTERISK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1962);
				match(INSERT);
				setState(1963);
				match(LEFT_PAREN);
				setState(1964);
				((NotMatchedActionContext)_localctx).columns = multipartIdentifierList();
				setState(1965);
				match(RIGHT_PAREN);
				setState(1966);
				match(VALUES);
				setState(1967);
				match(LEFT_PAREN);
				setState(1968);
				expression();
				setState(1973);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1969);
					match(COMMA);
					setState(1970);
					expression();
					}
					}
					setState(1975);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1976);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentListContext extends ParserRuleContext {
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public AssignmentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAssignmentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAssignmentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAssignmentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentListContext assignmentList() throws RecognitionException {
		AssignmentListContext _localctx = new AssignmentListContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1980);
			assignment();
			setState(1985);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1981);
				match(COMMA);
				setState(1982);
				assignment();
				}
				}
				setState(1987);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentContext extends ParserRuleContext {
		public MultipartIdentifierContext key;
		public ExpressionContext value;
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1988);
			((AssignmentContext)_localctx).key = multipartIdentifier();
			setState(1989);
			match(EQ);
			setState(1990);
			((AssignmentContext)_localctx).value = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(ArcticExtendSparkSqlParser.WHERE, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1992);
			match(WHERE);
			setState(1993);
			booleanExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HavingClauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(ArcticExtendSparkSqlParser.HAVING, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitHavingClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitHavingClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1995);
			match(HAVING);
			setState(1996);
			booleanExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HintContext extends ParserRuleContext {
		public HintStatementContext hintStatement;
		public List<HintStatementContext> hintStatements = new ArrayList<HintStatementContext>();
		public TerminalNode HENT_START() { return getToken(ArcticExtendSparkSqlParser.HENT_START, 0); }
		public TerminalNode HENT_END() { return getToken(ArcticExtendSparkSqlParser.HENT_END, 0); }
		public List<HintStatementContext> hintStatement() {
			return getRuleContexts(HintStatementContext.class);
		}
		public HintStatementContext hintStatement(int i) {
			return getRuleContext(HintStatementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public HintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitHint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HintContext hint() throws RecognitionException {
		HintContext _localctx = new HintContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_hint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1998);
			match(HENT_START);
			setState(1999);
			((HintContext)_localctx).hintStatement = hintStatement();
			((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
			setState(2006);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,244,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2001);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
					case 1:
						{
						setState(2000);
						match(COMMA);
						}
						break;
					}
					setState(2003);
					((HintContext)_localctx).hintStatement = hintStatement();
					((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
					}
					} 
				}
				setState(2008);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,244,_ctx);
			}
			setState(2009);
			match(HENT_END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HintStatementContext extends ParserRuleContext {
		public IdentifierContext hintName;
		public PrimaryExpressionContext primaryExpression;
		public List<PrimaryExpressionContext> parameters = new ArrayList<PrimaryExpressionContext>();
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public HintStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hintStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterHintStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitHintStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitHintStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HintStatementContext hintStatement() throws RecognitionException {
		HintStatementContext _localctx = new HintStatementContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_hintStatement);
		int _la;
		try {
			setState(2024);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,246,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2011);
				((HintStatementContext)_localctx).hintName = identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2012);
				((HintStatementContext)_localctx).hintName = identifier();
				setState(2013);
				match(LEFT_PAREN);
				setState(2014);
				((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
				((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
				setState(2019);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2015);
					match(COMMA);
					setState(2016);
					((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
					((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
					}
					}
					setState(2021);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2022);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public List<RelationContext> relation() {
			return getRuleContexts(RelationContext.class);
		}
		public RelationContext relation(int i) {
			return getRuleContext(RelationContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public List<LateralViewContext> lateralView() {
			return getRuleContexts(LateralViewContext.class);
		}
		public LateralViewContext lateralView(int i) {
			return getRuleContext(LateralViewContext.class,i);
		}
		public PivotClauseContext pivotClause() {
			return getRuleContext(PivotClauseContext.class,0);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_fromClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2026);
			match(FROM);
			setState(2027);
			relation();
			setState(2032);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,247,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2028);
					match(COMMA);
					setState(2029);
					relation();
					}
					} 
				}
				setState(2034);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,247,_ctx);
			}
			setState(2038);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,248,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2035);
					lateralView();
					}
					} 
				}
				setState(2040);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,248,_ctx);
			}
			setState(2042);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,249,_ctx) ) {
			case 1:
				{
				setState(2041);
				pivotClause();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TemporalClauseContext extends ParserRuleContext {
		public Token version;
		public ValueExpressionContext timestamp;
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode OF() { return getToken(ArcticExtendSparkSqlParser.OF, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(ArcticExtendSparkSqlParser.SYSTEM_VERSION, 0); }
		public TerminalNode VERSION() { return getToken(ArcticExtendSparkSqlParser.VERSION, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(ArcticExtendSparkSqlParser.SYSTEM_TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMP, 0); }
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TemporalClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_temporalClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTemporalClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTemporalClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTemporalClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TemporalClauseContext temporalClause() throws RecognitionException {
		TemporalClauseContext _localctx = new TemporalClauseContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_temporalClause);
		int _la;
		try {
			setState(2058);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,252,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2045);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2044);
					match(FOR);
					}
				}

				setState(2047);
				_la = _input.LA(1);
				if ( !(_la==SYSTEM_VERSION || _la==VERSION) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2048);
				match(AS);
				setState(2049);
				match(OF);
				setState(2050);
				((TemporalClauseContext)_localctx).version = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==STRING || _la==INTEGER_VALUE) ) {
					((TemporalClauseContext)_localctx).version = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2052);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2051);
					match(FOR);
					}
				}

				setState(2054);
				_la = _input.LA(1);
				if ( !(_la==SYSTEM_TIME || _la==TIMESTAMP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2055);
				match(AS);
				setState(2056);
				match(OF);
				setState(2057);
				((TemporalClauseContext)_localctx).timestamp = valueExpression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AggregationClauseContext extends ParserRuleContext {
		public GroupByClauseContext groupByClause;
		public List<GroupByClauseContext> groupingExpressionsWithGroupingAnalytics = new ArrayList<GroupByClauseContext>();
		public ExpressionContext expression;
		public List<ExpressionContext> groupingExpressions = new ArrayList<ExpressionContext>();
		public Token kind;
		public TerminalNode GROUP() { return getToken(ArcticExtendSparkSqlParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public List<GroupByClauseContext> groupByClause() {
			return getRuleContexts(GroupByClauseContext.class);
		}
		public GroupByClauseContext groupByClause(int i) {
			return getRuleContext(GroupByClauseContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode SETS() { return getToken(ArcticExtendSparkSqlParser.SETS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticExtendSparkSqlParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(ArcticExtendSparkSqlParser.CUBE, 0); }
		public TerminalNode GROUPING() { return getToken(ArcticExtendSparkSqlParser.GROUPING, 0); }
		public AggregationClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAggregationClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAggregationClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAggregationClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationClauseContext aggregationClause() throws RecognitionException {
		AggregationClauseContext _localctx = new AggregationClauseContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_aggregationClause);
		int _la;
		try {
			int _alt;
			setState(2099);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,257,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2060);
				match(GROUP);
				setState(2061);
				match(BY);
				setState(2062);
				((AggregationClauseContext)_localctx).groupByClause = groupByClause();
				((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
				setState(2067);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,253,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2063);
						match(COMMA);
						setState(2064);
						((AggregationClauseContext)_localctx).groupByClause = groupByClause();
						((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
						}
						} 
					}
					setState(2069);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,253,_ctx);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2070);
				match(GROUP);
				setState(2071);
				match(BY);
				setState(2072);
				((AggregationClauseContext)_localctx).expression = expression();
				((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
				setState(2077);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,254,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2073);
						match(COMMA);
						setState(2074);
						((AggregationClauseContext)_localctx).expression = expression();
						((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
						}
						} 
					}
					setState(2079);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,254,_ctx);
				}
				setState(2097);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,256,_ctx) ) {
				case 1:
					{
					setState(2080);
					match(WITH);
					setState(2081);
					((AggregationClauseContext)_localctx).kind = match(ROLLUP);
					}
					break;
				case 2:
					{
					setState(2082);
					match(WITH);
					setState(2083);
					((AggregationClauseContext)_localctx).kind = match(CUBE);
					}
					break;
				case 3:
					{
					setState(2084);
					((AggregationClauseContext)_localctx).kind = match(GROUPING);
					setState(2085);
					match(SETS);
					setState(2086);
					match(LEFT_PAREN);
					setState(2087);
					groupingSet();
					setState(2092);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2088);
						match(COMMA);
						setState(2089);
						groupingSet();
						}
						}
						setState(2094);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2095);
					match(RIGHT_PAREN);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupByClauseContext extends ParserRuleContext {
		public GroupingAnalyticsContext groupingAnalytics() {
			return getRuleContext(GroupingAnalyticsContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_groupByClause);
		try {
			setState(2103);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,258,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2101);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2102);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupingAnalyticsContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticExtendSparkSqlParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(ArcticExtendSparkSqlParser.CUBE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public TerminalNode GROUPING() { return getToken(ArcticExtendSparkSqlParser.GROUPING, 0); }
		public TerminalNode SETS() { return getToken(ArcticExtendSparkSqlParser.SETS, 0); }
		public List<GroupingElementContext> groupingElement() {
			return getRuleContexts(GroupingElementContext.class);
		}
		public GroupingElementContext groupingElement(int i) {
			return getRuleContext(GroupingElementContext.class,i);
		}
		public GroupingAnalyticsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupingAnalytics; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterGroupingAnalytics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitGroupingAnalytics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitGroupingAnalytics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingAnalyticsContext groupingAnalytics() throws RecognitionException {
		GroupingAnalyticsContext _localctx = new GroupingAnalyticsContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_groupingAnalytics);
		int _la;
		try {
			setState(2130);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CUBE:
			case ROLLUP:
				enterOuterAlt(_localctx, 1);
				{
				setState(2105);
				_la = _input.LA(1);
				if ( !(_la==CUBE || _la==ROLLUP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2106);
				match(LEFT_PAREN);
				setState(2107);
				groupingSet();
				setState(2112);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2108);
					match(COMMA);
					setState(2109);
					groupingSet();
					}
					}
					setState(2114);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2115);
				match(RIGHT_PAREN);
				}
				break;
			case GROUPING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2117);
				match(GROUPING);
				setState(2118);
				match(SETS);
				setState(2119);
				match(LEFT_PAREN);
				setState(2120);
				groupingElement();
				setState(2125);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2121);
					match(COMMA);
					setState(2122);
					groupingElement();
					}
					}
					setState(2127);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2128);
				match(RIGHT_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupingElementContext extends ParserRuleContext {
		public GroupingAnalyticsContext groupingAnalytics() {
			return getRuleContext(GroupingAnalyticsContext.class,0);
		}
		public GroupingSetContext groupingSet() {
			return getRuleContext(GroupingSetContext.class,0);
		}
		public GroupingElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupingElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterGroupingElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitGroupingElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitGroupingElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingElementContext groupingElement() throws RecognitionException {
		GroupingElementContext _localctx = new GroupingElementContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_groupingElement);
		try {
			setState(2134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,262,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2132);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2133);
				groupingSet();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupingSetContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public GroupingSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupingSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterGroupingSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitGroupingSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitGroupingSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingSetContext groupingSet() throws RecognitionException {
		GroupingSetContext _localctx = new GroupingSetContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_groupingSet);
		int _la;
		try {
			setState(2149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,265,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2136);
				match(LEFT_PAREN);
				setState(2145);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
				case 1:
					{
					setState(2137);
					expression();
					setState(2142);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2138);
						match(COMMA);
						setState(2139);
						expression();
						}
						}
						setState(2144);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2147);
				match(RIGHT_PAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2148);
				expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PivotClauseContext extends ParserRuleContext {
		public NamedExpressionSeqContext aggregates;
		public PivotValueContext pivotValue;
		public List<PivotValueContext> pivotValues = new ArrayList<PivotValueContext>();
		public TerminalNode PIVOT() { return getToken(ArcticExtendSparkSqlParser.PIVOT, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, i);
		}
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public PivotColumnContext pivotColumn() {
			return getRuleContext(PivotColumnContext.class,0);
		}
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, i);
		}
		public NamedExpressionSeqContext namedExpressionSeq() {
			return getRuleContext(NamedExpressionSeqContext.class,0);
		}
		public List<PivotValueContext> pivotValue() {
			return getRuleContexts(PivotValueContext.class);
		}
		public PivotValueContext pivotValue(int i) {
			return getRuleContext(PivotValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public PivotClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPivotClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPivotClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPivotClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotClauseContext pivotClause() throws RecognitionException {
		PivotClauseContext _localctx = new PivotClauseContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_pivotClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2151);
			match(PIVOT);
			setState(2152);
			match(LEFT_PAREN);
			setState(2153);
			((PivotClauseContext)_localctx).aggregates = namedExpressionSeq();
			setState(2154);
			match(FOR);
			setState(2155);
			pivotColumn();
			setState(2156);
			match(IN);
			setState(2157);
			match(LEFT_PAREN);
			setState(2158);
			((PivotClauseContext)_localctx).pivotValue = pivotValue();
			((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
			setState(2163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2159);
				match(COMMA);
				setState(2160);
				((PivotClauseContext)_localctx).pivotValue = pivotValue();
				((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
				}
				}
				setState(2165);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2166);
			match(RIGHT_PAREN);
			setState(2167);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PivotColumnContext extends ParserRuleContext {
		public IdentifierContext identifier;
		public List<IdentifierContext> identifiers = new ArrayList<IdentifierContext>();
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public PivotColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotColumn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPivotColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPivotColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPivotColumn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotColumnContext pivotColumn() throws RecognitionException {
		PivotColumnContext _localctx = new PivotColumnContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_pivotColumn);
		int _la;
		try {
			setState(2181);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,268,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2169);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2170);
				match(LEFT_PAREN);
				setState(2171);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				setState(2176);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2172);
					match(COMMA);
					setState(2173);
					((PivotColumnContext)_localctx).identifier = identifier();
					((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
					}
					}
					setState(2178);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2179);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PivotValueContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public PivotValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPivotValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPivotValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPivotValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotValueContext pivotValue() throws RecognitionException {
		PivotValueContext _localctx = new PivotValueContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_pivotValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2183);
			expression();
			setState(2188);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,270,_ctx) ) {
			case 1:
				{
				setState(2185);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,269,_ctx) ) {
				case 1:
					{
					setState(2184);
					match(AS);
					}
					break;
				}
				setState(2187);
				identifier();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LateralViewContext extends ParserRuleContext {
		public IdentifierContext tblName;
		public IdentifierContext identifier;
		public List<IdentifierContext> colName = new ArrayList<IdentifierContext>();
		public TerminalNode LATERAL() { return getToken(ArcticExtendSparkSqlParser.LATERAL, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode OUTER() { return getToken(ArcticExtendSparkSqlParser.OUTER, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public LateralViewContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lateralView; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLateralView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLateralView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLateralView(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LateralViewContext lateralView() throws RecognitionException {
		LateralViewContext _localctx = new LateralViewContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_lateralView);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2190);
			match(LATERAL);
			setState(2191);
			match(VIEW);
			setState(2193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,271,_ctx) ) {
			case 1:
				{
				setState(2192);
				match(OUTER);
				}
				break;
			}
			setState(2195);
			qualifiedName();
			setState(2196);
			match(LEFT_PAREN);
			setState(2205);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,273,_ctx) ) {
			case 1:
				{
				setState(2197);
				expression();
				setState(2202);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2198);
					match(COMMA);
					setState(2199);
					expression();
					}
					}
					setState(2204);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2207);
			match(RIGHT_PAREN);
			setState(2208);
			((LateralViewContext)_localctx).tblName = identifier();
			setState(2220);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,276,_ctx) ) {
			case 1:
				{
				setState(2210);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,274,_ctx) ) {
				case 1:
					{
					setState(2209);
					match(AS);
					}
					break;
				}
				setState(2212);
				((LateralViewContext)_localctx).identifier = identifier();
				((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
				setState(2217);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,275,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2213);
						match(COMMA);
						setState(2214);
						((LateralViewContext)_localctx).identifier = identifier();
						((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
						}
						} 
					}
					setState(2219);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,275,_ctx);
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SetQuantifierContext extends ParserRuleContext {
		public TerminalNode DISTINCT() { return getToken(ArcticExtendSparkSqlParser.DISTINCT, 0); }
		public TerminalNode ALL() { return getToken(ArcticExtendSparkSqlParser.ALL, 0); }
		public SetQuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setQuantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSetQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSetQuantifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSetQuantifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetQuantifierContext setQuantifier() throws RecognitionException {
		SetQuantifierContext _localctx = new SetQuantifierContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_setQuantifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2222);
			_la = _input.LA(1);
			if ( !(_la==ALL || _la==DISTINCT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationContext extends ParserRuleContext {
		public RelationPrimaryContext relationPrimary() {
			return getRuleContext(RelationPrimaryContext.class,0);
		}
		public TerminalNode LATERAL() { return getToken(ArcticExtendSparkSqlParser.LATERAL, 0); }
		public List<JoinRelationContext> joinRelation() {
			return getRuleContexts(JoinRelationContext.class);
		}
		public JoinRelationContext joinRelation(int i) {
			return getRuleContext(JoinRelationContext.class,i);
		}
		public RelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_relation);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2225);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,277,_ctx) ) {
			case 1:
				{
				setState(2224);
				match(LATERAL);
				}
				break;
			}
			setState(2227);
			relationPrimary();
			setState(2231);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,278,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2228);
					joinRelation();
					}
					} 
				}
				setState(2233);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,278,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinRelationContext extends ParserRuleContext {
		public RelationPrimaryContext right;
		public TerminalNode JOIN() { return getToken(ArcticExtendSparkSqlParser.JOIN, 0); }
		public RelationPrimaryContext relationPrimary() {
			return getRuleContext(RelationPrimaryContext.class,0);
		}
		public JoinTypeContext joinType() {
			return getRuleContext(JoinTypeContext.class,0);
		}
		public TerminalNode LATERAL() { return getToken(ArcticExtendSparkSqlParser.LATERAL, 0); }
		public JoinCriteriaContext joinCriteria() {
			return getRuleContext(JoinCriteriaContext.class,0);
		}
		public TerminalNode NATURAL() { return getToken(ArcticExtendSparkSqlParser.NATURAL, 0); }
		public JoinRelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinRelation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterJoinRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitJoinRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitJoinRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinRelationContext joinRelation() throws RecognitionException {
		JoinRelationContext _localctx = new JoinRelationContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_joinRelation);
		try {
			setState(2251);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANTI:
			case CROSS:
			case FULL:
			case INNER:
			case JOIN:
			case LEFT:
			case RIGHT:
			case SEMI:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(2234);
				joinType();
				}
				setState(2235);
				match(JOIN);
				setState(2237);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,279,_ctx) ) {
				case 1:
					{
					setState(2236);
					match(LATERAL);
					}
					break;
				}
				setState(2239);
				((JoinRelationContext)_localctx).right = relationPrimary();
				setState(2241);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,280,_ctx) ) {
				case 1:
					{
					setState(2240);
					joinCriteria();
					}
					break;
				}
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2243);
				match(NATURAL);
				setState(2244);
				joinType();
				setState(2245);
				match(JOIN);
				setState(2247);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,281,_ctx) ) {
				case 1:
					{
					setState(2246);
					match(LATERAL);
					}
					break;
				}
				setState(2249);
				((JoinRelationContext)_localctx).right = relationPrimary();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinTypeContext extends ParserRuleContext {
		public TerminalNode INNER() { return getToken(ArcticExtendSparkSqlParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(ArcticExtendSparkSqlParser.CROSS, 0); }
		public TerminalNode LEFT() { return getToken(ArcticExtendSparkSqlParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(ArcticExtendSparkSqlParser.OUTER, 0); }
		public TerminalNode SEMI() { return getToken(ArcticExtendSparkSqlParser.SEMI, 0); }
		public TerminalNode RIGHT() { return getToken(ArcticExtendSparkSqlParser.RIGHT, 0); }
		public TerminalNode FULL() { return getToken(ArcticExtendSparkSqlParser.FULL, 0); }
		public TerminalNode ANTI() { return getToken(ArcticExtendSparkSqlParser.ANTI, 0); }
		public JoinTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterJoinType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitJoinType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitJoinType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinTypeContext joinType() throws RecognitionException {
		JoinTypeContext _localctx = new JoinTypeContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_joinType);
		int _la;
		try {
			setState(2277);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,289,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2254);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(2253);
					match(INNER);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2256);
				match(CROSS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2257);
				match(LEFT);
				setState(2259);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2258);
					match(OUTER);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2262);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2261);
					match(LEFT);
					}
				}

				setState(2264);
				match(SEMI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2265);
				match(RIGHT);
				setState(2267);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2266);
					match(OUTER);
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2269);
				match(FULL);
				setState(2271);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2270);
					match(OUTER);
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2273);
					match(LEFT);
					}
				}

				setState(2276);
				match(ANTI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinCriteriaContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public JoinCriteriaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCriteria; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterJoinCriteria(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitJoinCriteria(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitJoinCriteria(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinCriteriaContext joinCriteria() throws RecognitionException {
		JoinCriteriaContext _localctx = new JoinCriteriaContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_joinCriteria);
		try {
			setState(2283);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(2279);
				match(ON);
				setState(2280);
				booleanExpression(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2281);
				match(USING);
				setState(2282);
				identifierList();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SampleContext extends ParserRuleContext {
		public Token seed;
		public TerminalNode TABLESAMPLE() { return getToken(ArcticExtendSparkSqlParser.TABLESAMPLE, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, i);
		}
		public SampleMethodContext sampleMethod() {
			return getRuleContext(SampleMethodContext.class,0);
		}
		public TerminalNode REPEATABLE() { return getToken(ArcticExtendSparkSqlParser.REPEATABLE, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public SampleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sample; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSample(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSample(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSample(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SampleContext sample() throws RecognitionException {
		SampleContext _localctx = new SampleContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_sample);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2285);
			match(TABLESAMPLE);
			setState(2286);
			match(LEFT_PAREN);
			setState(2288);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,291,_ctx) ) {
			case 1:
				{
				setState(2287);
				sampleMethod();
				}
				break;
			}
			setState(2290);
			match(RIGHT_PAREN);
			setState(2295);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,292,_ctx) ) {
			case 1:
				{
				setState(2291);
				match(REPEATABLE);
				setState(2292);
				match(LEFT_PAREN);
				setState(2293);
				((SampleContext)_localctx).seed = match(INTEGER_VALUE);
				setState(2294);
				match(RIGHT_PAREN);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SampleMethodContext extends ParserRuleContext {
		public SampleMethodContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sampleMethod; }
	 
		public SampleMethodContext() { }
		public void copyFrom(SampleMethodContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SampleByRowsContext extends SampleMethodContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ROWS() { return getToken(ArcticExtendSparkSqlParser.ROWS, 0); }
		public SampleByRowsContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSampleByRows(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSampleByRows(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSampleByRows(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByPercentileContext extends SampleMethodContext {
		public Token negativeSign;
		public Token percentage;
		public TerminalNode PERCENTLIT() { return getToken(ArcticExtendSparkSqlParser.PERCENTLIT, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticExtendSparkSqlParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public SampleByPercentileContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSampleByPercentile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSampleByPercentile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSampleByPercentile(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByBucketContext extends SampleMethodContext {
		public Token sampleType;
		public Token numerator;
		public Token denominator;
		public TerminalNode OUT() { return getToken(ArcticExtendSparkSqlParser.OUT, 0); }
		public TerminalNode OF() { return getToken(ArcticExtendSparkSqlParser.OF, 0); }
		public TerminalNode BUCKET() { return getToken(ArcticExtendSparkSqlParser.BUCKET, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(ArcticExtendSparkSqlParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, i);
		}
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public SampleByBucketContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSampleByBucket(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSampleByBucket(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSampleByBucket(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByBytesContext extends SampleMethodContext {
		public ExpressionContext bytes;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SampleByBytesContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSampleByBytes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSampleByBytes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSampleByBytes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SampleMethodContext sampleMethod() throws RecognitionException {
		SampleMethodContext _localctx = new SampleMethodContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_sampleMethod);
		int _la;
		try {
			setState(2321);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,296,_ctx) ) {
			case 1:
				_localctx = new SampleByPercentileContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2298);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2297);
					((SampleByPercentileContext)_localctx).negativeSign = match(MINUS);
					}
				}

				setState(2300);
				((SampleByPercentileContext)_localctx).percentage = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==INTEGER_VALUE || _la==DECIMAL_VALUE) ) {
					((SampleByPercentileContext)_localctx).percentage = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2301);
				match(PERCENTLIT);
				}
				break;
			case 2:
				_localctx = new SampleByRowsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2302);
				expression();
				setState(2303);
				match(ROWS);
				}
				break;
			case 3:
				_localctx = new SampleByBucketContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2305);
				((SampleByBucketContext)_localctx).sampleType = match(BUCKET);
				setState(2306);
				((SampleByBucketContext)_localctx).numerator = match(INTEGER_VALUE);
				setState(2307);
				match(OUT);
				setState(2308);
				match(OF);
				setState(2309);
				((SampleByBucketContext)_localctx).denominator = match(INTEGER_VALUE);
				setState(2318);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(2310);
					match(ON);
					setState(2316);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,294,_ctx) ) {
					case 1:
						{
						setState(2311);
						identifier();
						}
						break;
					case 2:
						{
						setState(2312);
						qualifiedName();
						setState(2313);
						match(LEFT_PAREN);
						setState(2314);
						match(RIGHT_PAREN);
						}
						break;
					}
					}
				}

				}
				break;
			case 4:
				_localctx = new SampleByBytesContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2320);
				((SampleByBytesContext)_localctx).bytes = expression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public IdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierListContext identifierList() throws RecognitionException {
		IdentifierListContext _localctx = new IdentifierListContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_identifierList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2323);
			match(LEFT_PAREN);
			setState(2324);
			identifierSeq();
			setState(2325);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierSeqContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext errorCapturingIdentifier;
		public List<ErrorCapturingIdentifierContext> ident = new ArrayList<ErrorCapturingIdentifierContext>();
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public IdentifierSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIdentifierSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIdentifierSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIdentifierSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierSeqContext identifierSeq() throws RecognitionException {
		IdentifierSeqContext _localctx = new IdentifierSeqContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_identifierSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2327);
			((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
			setState(2332);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,297,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2328);
					match(COMMA);
					setState(2329);
					((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2334);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,297,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrderedIdentifierListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<OrderedIdentifierContext> orderedIdentifier() {
			return getRuleContexts(OrderedIdentifierContext.class);
		}
		public OrderedIdentifierContext orderedIdentifier(int i) {
			return getRuleContext(OrderedIdentifierContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public OrderedIdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedIdentifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterOrderedIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitOrderedIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitOrderedIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedIdentifierListContext orderedIdentifierList() throws RecognitionException {
		OrderedIdentifierListContext _localctx = new OrderedIdentifierListContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_orderedIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2335);
			match(LEFT_PAREN);
			setState(2336);
			orderedIdentifier();
			setState(2341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2337);
				match(COMMA);
				setState(2338);
				orderedIdentifier();
				}
				}
				setState(2343);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2344);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrderedIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext ident;
		public Token ordering;
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode ASC() { return getToken(ArcticExtendSparkSqlParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public OrderedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterOrderedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitOrderedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitOrderedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedIdentifierContext orderedIdentifier() throws RecognitionException {
		OrderedIdentifierContext _localctx = new OrderedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_orderedIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2346);
			((OrderedIdentifierContext)_localctx).ident = errorCapturingIdentifier();
			setState(2348);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(2347);
				((OrderedIdentifierContext)_localctx).ordering = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
					((OrderedIdentifierContext)_localctx).ordering = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierCommentListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<IdentifierCommentContext> identifierComment() {
			return getRuleContexts(IdentifierCommentContext.class);
		}
		public IdentifierCommentContext identifierComment(int i) {
			return getRuleContext(IdentifierCommentContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public IdentifierCommentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierCommentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIdentifierCommentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIdentifierCommentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIdentifierCommentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierCommentListContext identifierCommentList() throws RecognitionException {
		IdentifierCommentListContext _localctx = new IdentifierCommentListContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_identifierCommentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2350);
			match(LEFT_PAREN);
			setState(2351);
			identifierComment();
			setState(2356);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2352);
				match(COMMA);
				setState(2353);
				identifierComment();
				}
				}
				setState(2358);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2359);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierCommentContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public IdentifierCommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierComment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIdentifierComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIdentifierComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIdentifierComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierCommentContext identifierComment() throws RecognitionException {
		IdentifierCommentContext _localctx = new IdentifierCommentContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_identifierComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2361);
			identifier();
			setState(2363);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2362);
				commentSpec();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationPrimaryContext extends ParserRuleContext {
		public RelationPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationPrimary; }
	 
		public RelationPrimaryContext() { }
		public void copyFrom(RelationPrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TableValuedFunctionContext extends RelationPrimaryContext {
		public FunctionTableContext functionTable() {
			return getRuleContext(FunctionTableContext.class,0);
		}
		public TableValuedFunctionContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableValuedFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableValuedFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableValuedFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InlineTableDefault2Context extends RelationPrimaryContext {
		public InlineTableContext inlineTable() {
			return getRuleContext(InlineTableContext.class,0);
		}
		public InlineTableDefault2Context(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInlineTableDefault2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInlineTableDefault2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInlineTableDefault2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AliasedRelationContext extends RelationPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public RelationContext relation() {
			return getRuleContext(RelationContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public AliasedRelationContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAliasedRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAliasedRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAliasedRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AliasedQueryContext extends RelationPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public AliasedQueryContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAliasedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAliasedQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAliasedQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TableNameContext extends RelationPrimaryContext {
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public TemporalClauseContext temporalClause() {
			return getRuleContext(TemporalClauseContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public TableNameContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationPrimaryContext relationPrimary() throws RecognitionException {
		RelationPrimaryContext _localctx = new RelationPrimaryContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_relationPrimary);
		try {
			setState(2392);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,306,_ctx) ) {
			case 1:
				_localctx = new TableNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2365);
				multipartIdentifier();
				setState(2367);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,302,_ctx) ) {
				case 1:
					{
					setState(2366);
					temporalClause();
					}
					break;
				}
				setState(2370);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,303,_ctx) ) {
				case 1:
					{
					setState(2369);
					sample();
					}
					break;
				}
				setState(2372);
				tableAlias();
				}
				break;
			case 2:
				_localctx = new AliasedQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2374);
				match(LEFT_PAREN);
				setState(2375);
				query();
				setState(2376);
				match(RIGHT_PAREN);
				setState(2378);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,304,_ctx) ) {
				case 1:
					{
					setState(2377);
					sample();
					}
					break;
				}
				setState(2380);
				tableAlias();
				}
				break;
			case 3:
				_localctx = new AliasedRelationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2382);
				match(LEFT_PAREN);
				setState(2383);
				relation();
				setState(2384);
				match(RIGHT_PAREN);
				setState(2386);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,305,_ctx) ) {
				case 1:
					{
					setState(2385);
					sample();
					}
					break;
				}
				setState(2388);
				tableAlias();
				}
				break;
			case 4:
				_localctx = new InlineTableDefault2Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2390);
				inlineTable();
				}
				break;
			case 5:
				_localctx = new TableValuedFunctionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2391);
				functionTable();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableContext extends ParserRuleContext {
		public TerminalNode VALUES() { return getToken(ArcticExtendSparkSqlParser.VALUES, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public InlineTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInlineTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInlineTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInlineTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InlineTableContext inlineTable() throws RecognitionException {
		InlineTableContext _localctx = new InlineTableContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_inlineTable);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2394);
			match(VALUES);
			setState(2395);
			expression();
			setState(2400);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,307,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2396);
					match(COMMA);
					setState(2397);
					expression();
					}
					} 
				}
				setState(2402);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,307,_ctx);
			}
			setState(2403);
			tableAlias();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionTableContext extends ParserRuleContext {
		public FunctionNameContext funcName;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public FunctionTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFunctionTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFunctionTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFunctionTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTableContext functionTable() throws RecognitionException {
		FunctionTableContext _localctx = new FunctionTableContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_functionTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2405);
			((FunctionTableContext)_localctx).funcName = functionName();
			setState(2406);
			match(LEFT_PAREN);
			setState(2415);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,309,_ctx) ) {
			case 1:
				{
				setState(2407);
				expression();
				setState(2412);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2408);
					match(COMMA);
					setState(2409);
					expression();
					}
					}
					setState(2414);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2417);
			match(RIGHT_PAREN);
			setState(2418);
			tableAlias();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableAliasContext extends ParserRuleContext {
		public StrictIdentifierContext strictIdentifier() {
			return getRuleContext(StrictIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TableAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableAliasContext tableAlias() throws RecognitionException {
		TableAliasContext _localctx = new TableAliasContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_tableAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2427);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,312,_ctx) ) {
			case 1:
				{
				setState(2421);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,310,_ctx) ) {
				case 1:
					{
					setState(2420);
					match(AS);
					}
					break;
				}
				setState(2423);
				strictIdentifier();
				setState(2425);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,311,_ctx) ) {
				case 1:
					{
					setState(2424);
					identifierList();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RowFormatContext extends ParserRuleContext {
		public RowFormatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rowFormat; }
	 
		public RowFormatContext() { }
		public void copyFrom(RowFormatContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RowFormatSerdeContext extends RowFormatContext {
		public Token name;
		public PropertyListContext props;
		public TerminalNode ROW() { return getToken(ArcticExtendSparkSqlParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticExtendSparkSqlParser.FORMAT, 0); }
		public TerminalNode SERDE() { return getToken(ArcticExtendSparkSqlParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public RowFormatSerdeContext(RowFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRowFormatSerde(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRowFormatSerde(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRowFormatSerde(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RowFormatDelimitedContext extends RowFormatContext {
		public Token fieldsTerminatedBy;
		public Token escapedBy;
		public Token collectionItemsTerminatedBy;
		public Token keysTerminatedBy;
		public Token linesSeparatedBy;
		public Token nullDefinedAs;
		public TerminalNode ROW() { return getToken(ArcticExtendSparkSqlParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticExtendSparkSqlParser.FORMAT, 0); }
		public TerminalNode DELIMITED() { return getToken(ArcticExtendSparkSqlParser.DELIMITED, 0); }
		public TerminalNode FIELDS() { return getToken(ArcticExtendSparkSqlParser.FIELDS, 0); }
		public List<TerminalNode> TERMINATED() { return getTokens(ArcticExtendSparkSqlParser.TERMINATED); }
		public TerminalNode TERMINATED(int i) {
			return getToken(ArcticExtendSparkSqlParser.TERMINATED, i);
		}
		public List<TerminalNode> BY() { return getTokens(ArcticExtendSparkSqlParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticExtendSparkSqlParser.BY, i);
		}
		public TerminalNode COLLECTION() { return getToken(ArcticExtendSparkSqlParser.COLLECTION, 0); }
		public TerminalNode ITEMS() { return getToken(ArcticExtendSparkSqlParser.ITEMS, 0); }
		public TerminalNode MAP() { return getToken(ArcticExtendSparkSqlParser.MAP, 0); }
		public TerminalNode KEYS() { return getToken(ArcticExtendSparkSqlParser.KEYS, 0); }
		public TerminalNode LINES() { return getToken(ArcticExtendSparkSqlParser.LINES, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public TerminalNode DEFINED() { return getToken(ArcticExtendSparkSqlParser.DEFINED, 0); }
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public List<TerminalNode> STRING() { return getTokens(ArcticExtendSparkSqlParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticExtendSparkSqlParser.STRING, i);
		}
		public TerminalNode ESCAPED() { return getToken(ArcticExtendSparkSqlParser.ESCAPED, 0); }
		public RowFormatDelimitedContext(RowFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRowFormatDelimited(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRowFormatDelimited(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRowFormatDelimited(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowFormatContext rowFormat() throws RecognitionException {
		RowFormatContext _localctx = new RowFormatContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_rowFormat);
		try {
			setState(2478);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,320,_ctx) ) {
			case 1:
				_localctx = new RowFormatSerdeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2429);
				match(ROW);
				setState(2430);
				match(FORMAT);
				setState(2431);
				match(SERDE);
				setState(2432);
				((RowFormatSerdeContext)_localctx).name = match(STRING);
				setState(2436);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,313,_ctx) ) {
				case 1:
					{
					setState(2433);
					match(WITH);
					setState(2434);
					match(SERDEPROPERTIES);
					setState(2435);
					((RowFormatSerdeContext)_localctx).props = propertyList();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new RowFormatDelimitedContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2438);
				match(ROW);
				setState(2439);
				match(FORMAT);
				setState(2440);
				match(DELIMITED);
				setState(2450);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,315,_ctx) ) {
				case 1:
					{
					setState(2441);
					match(FIELDS);
					setState(2442);
					match(TERMINATED);
					setState(2443);
					match(BY);
					setState(2444);
					((RowFormatDelimitedContext)_localctx).fieldsTerminatedBy = match(STRING);
					setState(2448);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,314,_ctx) ) {
					case 1:
						{
						setState(2445);
						match(ESCAPED);
						setState(2446);
						match(BY);
						setState(2447);
						((RowFormatDelimitedContext)_localctx).escapedBy = match(STRING);
						}
						break;
					}
					}
					break;
				}
				setState(2457);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,316,_ctx) ) {
				case 1:
					{
					setState(2452);
					match(COLLECTION);
					setState(2453);
					match(ITEMS);
					setState(2454);
					match(TERMINATED);
					setState(2455);
					match(BY);
					setState(2456);
					((RowFormatDelimitedContext)_localctx).collectionItemsTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2464);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,317,_ctx) ) {
				case 1:
					{
					setState(2459);
					match(MAP);
					setState(2460);
					match(KEYS);
					setState(2461);
					match(TERMINATED);
					setState(2462);
					match(BY);
					setState(2463);
					((RowFormatDelimitedContext)_localctx).keysTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2470);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,318,_ctx) ) {
				case 1:
					{
					setState(2466);
					match(LINES);
					setState(2467);
					match(TERMINATED);
					setState(2468);
					match(BY);
					setState(2469);
					((RowFormatDelimitedContext)_localctx).linesSeparatedBy = match(STRING);
					}
					break;
				}
				setState(2476);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,319,_ctx) ) {
				case 1:
					{
					setState(2472);
					match(NULL);
					setState(2473);
					match(DEFINED);
					setState(2474);
					match(AS);
					setState(2475);
					((RowFormatDelimitedContext)_localctx).nullDefinedAs = match(STRING);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultipartIdentifierListContext extends ParserRuleContext {
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public MultipartIdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultipartIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultipartIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultipartIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierListContext multipartIdentifierList() throws RecognitionException {
		MultipartIdentifierListContext _localctx = new MultipartIdentifierListContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_multipartIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2480);
			multipartIdentifier();
			setState(2485);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2481);
				match(COMMA);
				setState(2482);
				multipartIdentifier();
				}
				}
				setState(2487);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultipartIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext errorCapturingIdentifier;
		public List<ErrorCapturingIdentifierContext> parts = new ArrayList<ErrorCapturingIdentifierContext>();
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(ArcticExtendSparkSqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticExtendSparkSqlParser.DOT, i);
		}
		public MultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierContext multipartIdentifier() throws RecognitionException {
		MultipartIdentifierContext _localctx = new MultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_multipartIdentifier);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2488);
			((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
			setState(2493);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2489);
					match(DOT);
					setState(2490);
					((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2495);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultipartIdentifierPropertyListContext extends ParserRuleContext {
		public List<MultipartIdentifierPropertyContext> multipartIdentifierProperty() {
			return getRuleContexts(MultipartIdentifierPropertyContext.class);
		}
		public MultipartIdentifierPropertyContext multipartIdentifierProperty(int i) {
			return getRuleContext(MultipartIdentifierPropertyContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public MultipartIdentifierPropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierPropertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultipartIdentifierPropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultipartIdentifierPropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultipartIdentifierPropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierPropertyListContext multipartIdentifierPropertyList() throws RecognitionException {
		MultipartIdentifierPropertyListContext _localctx = new MultipartIdentifierPropertyListContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_multipartIdentifierPropertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2496);
			multipartIdentifierProperty();
			setState(2501);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2497);
				match(COMMA);
				setState(2498);
				multipartIdentifierProperty();
				}
				}
				setState(2503);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultipartIdentifierPropertyContext extends ParserRuleContext {
		public PropertyListContext options;
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public MultipartIdentifierPropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultipartIdentifierProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultipartIdentifierProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultipartIdentifierProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierPropertyContext multipartIdentifierProperty() throws RecognitionException {
		MultipartIdentifierPropertyContext _localctx = new MultipartIdentifierPropertyContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_multipartIdentifierProperty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2504);
			multipartIdentifier();
			setState(2507);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPTIONS) {
				{
				setState(2505);
				match(OPTIONS);
				setState(2506);
				((MultipartIdentifierPropertyContext)_localctx).options = propertyList();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext db;
		public ErrorCapturingIdentifierContext table;
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
		public TerminalNode DOT() { return getToken(ArcticExtendSparkSqlParser.DOT, 0); }
		public TableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableIdentifierContext tableIdentifier() throws RecognitionException {
		TableIdentifierContext _localctx = new TableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_tableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2512);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,325,_ctx) ) {
			case 1:
				{
				setState(2509);
				((TableIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2510);
				match(DOT);
				}
				break;
			}
			setState(2514);
			((TableIdentifierContext)_localctx).table = errorCapturingIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext db;
		public ErrorCapturingIdentifierContext function;
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
		public TerminalNode DOT() { return getToken(ArcticExtendSparkSqlParser.DOT, 0); }
		public FunctionIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFunctionIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFunctionIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFunctionIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionIdentifierContext functionIdentifier() throws RecognitionException {
		FunctionIdentifierContext _localctx = new FunctionIdentifierContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_functionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2519);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,326,_ctx) ) {
			case 1:
				{
				setState(2516);
				((FunctionIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2517);
				match(DOT);
				}
				break;
			}
			setState(2521);
			((FunctionIdentifierContext)_localctx).function = errorCapturingIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedExpressionContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext name;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public NamedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNamedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNamedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNamedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedExpressionContext namedExpression() throws RecognitionException {
		NamedExpressionContext _localctx = new NamedExpressionContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_namedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2523);
			expression();
			setState(2531);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,329,_ctx) ) {
			case 1:
				{
				setState(2525);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,327,_ctx) ) {
				case 1:
					{
					setState(2524);
					match(AS);
					}
					break;
				}
				setState(2529);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,328,_ctx) ) {
				case 1:
					{
					setState(2527);
					((NamedExpressionContext)_localctx).name = errorCapturingIdentifier();
					}
					break;
				case 2:
					{
					setState(2528);
					identifierList();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedExpressionSeqContext extends ParserRuleContext {
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public NamedExpressionSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedExpressionSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNamedExpressionSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNamedExpressionSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNamedExpressionSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedExpressionSeqContext namedExpressionSeq() throws RecognitionException {
		NamedExpressionSeqContext _localctx = new NamedExpressionSeqContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_namedExpressionSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2533);
			namedExpression();
			setState(2538);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,330,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2534);
					match(COMMA);
					setState(2535);
					namedExpression();
					}
					} 
				}
				setState(2540);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,330,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartitionFieldListContext extends ParserRuleContext {
		public PartitionFieldContext partitionField;
		public List<PartitionFieldContext> fields = new ArrayList<PartitionFieldContext>();
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<PartitionFieldContext> partitionField() {
			return getRuleContexts(PartitionFieldContext.class);
		}
		public PartitionFieldContext partitionField(int i) {
			return getRuleContext(PartitionFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public PartitionFieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionFieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPartitionFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPartitionFieldList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPartitionFieldList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionFieldListContext partitionFieldList() throws RecognitionException {
		PartitionFieldListContext _localctx = new PartitionFieldListContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_partitionFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2541);
			match(LEFT_PAREN);
			setState(2542);
			((PartitionFieldListContext)_localctx).partitionField = partitionField();
			((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
			setState(2547);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2543);
				match(COMMA);
				setState(2544);
				((PartitionFieldListContext)_localctx).partitionField = partitionField();
				((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
				}
				}
				setState(2549);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2550);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartitionFieldContext extends ParserRuleContext {
		public PartitionFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionField; }
	 
		public PartitionFieldContext() { }
		public void copyFrom(PartitionFieldContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PartitionColumnContext extends PartitionFieldContext {
		public ColTypeContext colType() {
			return getRuleContext(ColTypeContext.class,0);
		}
		public PartitionColumnContext(PartitionFieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPartitionColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPartitionColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPartitionColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PartitionTransformContext extends PartitionFieldContext {
		public TransformContext transform() {
			return getRuleContext(TransformContext.class,0);
		}
		public PartitionTransformContext(PartitionFieldContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPartitionTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPartitionTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPartitionTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionFieldContext partitionField() throws RecognitionException {
		PartitionFieldContext _localctx = new PartitionFieldContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_partitionField);
		try {
			setState(2554);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,332,_ctx) ) {
			case 1:
				_localctx = new PartitionTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2552);
				transform();
				}
				break;
			case 2:
				_localctx = new PartitionColumnContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2553);
				colType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TransformContext extends ParserRuleContext {
		public TransformContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transform; }
	 
		public TransformContext() { }
		public void copyFrom(TransformContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IdentityTransformContext extends TransformContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public IdentityTransformContext(TransformContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIdentityTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIdentityTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIdentityTransform(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ApplyTransformContext extends TransformContext {
		public IdentifierContext transformName;
		public TransformArgumentContext transformArgument;
		public List<TransformArgumentContext> argument = new ArrayList<TransformArgumentContext>();
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TransformArgumentContext> transformArgument() {
			return getRuleContexts(TransformArgumentContext.class);
		}
		public TransformArgumentContext transformArgument(int i) {
			return getRuleContext(TransformArgumentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public ApplyTransformContext(TransformContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterApplyTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitApplyTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitApplyTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformContext transform() throws RecognitionException {
		TransformContext _localctx = new TransformContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_transform);
		int _la;
		try {
			setState(2569);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,334,_ctx) ) {
			case 1:
				_localctx = new IdentityTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2556);
				qualifiedName();
				}
				break;
			case 2:
				_localctx = new ApplyTransformContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2557);
				((ApplyTransformContext)_localctx).transformName = identifier();
				setState(2558);
				match(LEFT_PAREN);
				setState(2559);
				((ApplyTransformContext)_localctx).transformArgument = transformArgument();
				((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
				setState(2564);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2560);
					match(COMMA);
					setState(2561);
					((ApplyTransformContext)_localctx).transformArgument = transformArgument();
					((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
					}
					}
					setState(2566);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2567);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TransformArgumentContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public TransformArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transformArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTransformArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTransformArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTransformArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformArgumentContext transformArgument() throws RecognitionException {
		TransformArgumentContext _localctx = new TransformArgumentContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_transformArgument);
		try {
			setState(2573);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,335,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2571);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2572);
				constant();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2575);
			booleanExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionSeqContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public ExpressionSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterExpressionSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitExpressionSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitExpressionSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionSeqContext expressionSeq() throws RecognitionException {
		ExpressionSeqContext _localctx = new ExpressionSeqContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_expressionSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2577);
			expression();
			setState(2582);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2578);
				match(COMMA);
				setState(2579);
				expression();
				}
				}
				setState(2584);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanExpressionContext extends ParserRuleContext {
		public BooleanExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanExpression; }
	 
		public BooleanExpressionContext() { }
		public void copyFrom(BooleanExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class LogicalNotContext extends BooleanExpressionContext {
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public LogicalNotContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLogicalNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLogicalNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLogicalNot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PredicatedContext extends BooleanExpressionContext {
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public PredicatedContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPredicated(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPredicated(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPredicated(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExistsContext extends BooleanExpressionContext {
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public ExistsContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitExists(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LogicalBinaryContext extends BooleanExpressionContext {
		public BooleanExpressionContext left;
		public Token operator;
		public BooleanExpressionContext right;
		public List<BooleanExpressionContext> booleanExpression() {
			return getRuleContexts(BooleanExpressionContext.class);
		}
		public BooleanExpressionContext booleanExpression(int i) {
			return getRuleContext(BooleanExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public LogicalBinaryContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLogicalBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLogicalBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLogicalBinary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanExpressionContext booleanExpression() throws RecognitionException {
		return booleanExpression(0);
	}

	private BooleanExpressionContext booleanExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanExpressionContext _localctx = new BooleanExpressionContext(_ctx, _parentState);
		BooleanExpressionContext _prevctx = _localctx;
		int _startState = 220;
		enterRecursionRule(_localctx, 220, RULE_booleanExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2597);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,338,_ctx) ) {
			case 1:
				{
				_localctx = new LogicalNotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2586);
				match(NOT);
				setState(2587);
				booleanExpression(5);
				}
				break;
			case 2:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2588);
				match(EXISTS);
				setState(2589);
				match(LEFT_PAREN);
				setState(2590);
				query();
				setState(2591);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new PredicatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2593);
				valueExpression(0);
				setState(2595);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,337,_ctx) ) {
				case 1:
					{
					setState(2594);
					predicate();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2607);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,340,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2605);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,339,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2599);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2600);
						((LogicalBinaryContext)_localctx).operator = match(AND);
						setState(2601);
						((LogicalBinaryContext)_localctx).right = booleanExpression(3);
						}
						break;
					case 2:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2602);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2603);
						((LogicalBinaryContext)_localctx).operator = match(OR);
						setState(2604);
						((LogicalBinaryContext)_localctx).right = booleanExpression(2);
						}
						break;
					}
					} 
				}
				setState(2609);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,340,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public Token kind;
		public ValueExpressionContext lower;
		public ValueExpressionContext upper;
		public ValueExpressionContext pattern;
		public Token quantifier;
		public Token escapeChar;
		public ValueExpressionContext right;
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticExtendSparkSqlParser.BETWEEN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RLIKE() { return getToken(ArcticExtendSparkSqlParser.RLIKE, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(ArcticExtendSparkSqlParser.ILIKE, 0); }
		public TerminalNode ANY() { return getToken(ArcticExtendSparkSqlParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(ArcticExtendSparkSqlParser.SOME, 0); }
		public TerminalNode ALL() { return getToken(ArcticExtendSparkSqlParser.ALL, 0); }
		public TerminalNode ESCAPE() { return getToken(ArcticExtendSparkSqlParser.ESCAPE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode IS() { return getToken(ArcticExtendSparkSqlParser.IS, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public TerminalNode TRUE() { return getToken(ArcticExtendSparkSqlParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ArcticExtendSparkSqlParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(ArcticExtendSparkSqlParser.UNKNOWN, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode DISTINCT() { return getToken(ArcticExtendSparkSqlParser.DISTINCT, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_predicate);
		int _la;
		try {
			setState(2692);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,354,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2611);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2610);
					match(NOT);
					}
				}

				setState(2613);
				((PredicateContext)_localctx).kind = match(BETWEEN);
				setState(2614);
				((PredicateContext)_localctx).lower = valueExpression(0);
				setState(2615);
				match(AND);
				setState(2616);
				((PredicateContext)_localctx).upper = valueExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2619);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2618);
					match(NOT);
					}
				}

				setState(2621);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2622);
				match(LEFT_PAREN);
				setState(2623);
				expression();
				setState(2628);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2624);
					match(COMMA);
					setState(2625);
					expression();
					}
					}
					setState(2630);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2631);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2634);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2633);
					match(NOT);
					}
				}

				setState(2636);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2637);
				match(LEFT_PAREN);
				setState(2638);
				query();
				setState(2639);
				match(RIGHT_PAREN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2642);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2641);
					match(NOT);
					}
				}

				setState(2644);
				((PredicateContext)_localctx).kind = match(RLIKE);
				setState(2645);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2647);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2646);
					match(NOT);
					}
				}

				setState(2649);
				((PredicateContext)_localctx).kind = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==LIKE || _la==ILIKE) ) {
					((PredicateContext)_localctx).kind = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2650);
				((PredicateContext)_localctx).quantifier = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==ANY || _la==SOME) ) {
					((PredicateContext)_localctx).quantifier = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2664);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,348,_ctx) ) {
				case 1:
					{
					setState(2651);
					match(LEFT_PAREN);
					setState(2652);
					match(RIGHT_PAREN);
					}
					break;
				case 2:
					{
					setState(2653);
					match(LEFT_PAREN);
					setState(2654);
					expression();
					setState(2659);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2655);
						match(COMMA);
						setState(2656);
						expression();
						}
						}
						setState(2661);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2662);
					match(RIGHT_PAREN);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2667);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2666);
					match(NOT);
					}
				}

				setState(2669);
				((PredicateContext)_localctx).kind = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==LIKE || _la==ILIKE) ) {
					((PredicateContext)_localctx).kind = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2670);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				setState(2673);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,350,_ctx) ) {
				case 1:
					{
					setState(2671);
					match(ESCAPE);
					setState(2672);
					((PredicateContext)_localctx).escapeChar = match(STRING);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2675);
				match(IS);
				setState(2677);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2676);
					match(NOT);
					}
				}

				setState(2679);
				((PredicateContext)_localctx).kind = match(NULL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2680);
				match(IS);
				setState(2682);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2681);
					match(NOT);
					}
				}

				setState(2684);
				((PredicateContext)_localctx).kind = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==FALSE || _la==TRUE || _la==UNKNOWN) ) {
					((PredicateContext)_localctx).kind = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2685);
				match(IS);
				setState(2687);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2686);
					match(NOT);
					}
				}

				setState(2689);
				((PredicateContext)_localctx).kind = match(DISTINCT);
				setState(2690);
				match(FROM);
				setState(2691);
				((PredicateContext)_localctx).right = valueExpression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueExpressionContext extends ParserRuleContext {
		public ValueExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueExpression; }
	 
		public ValueExpressionContext() { }
		public void copyFrom(ValueExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ValueExpressionDefaultContext extends ValueExpressionContext {
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public ValueExpressionDefaultContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterValueExpressionDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitValueExpressionDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitValueExpressionDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ComparisonContext extends ValueExpressionContext {
		public ValueExpressionContext left;
		public ValueExpressionContext right;
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public ComparisonContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ArithmeticBinaryContext extends ValueExpressionContext {
		public ValueExpressionContext left;
		public Token operator;
		public ValueExpressionContext right;
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode ASTERISK() { return getToken(ArcticExtendSparkSqlParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(ArcticExtendSparkSqlParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(ArcticExtendSparkSqlParser.PERCENT, 0); }
		public TerminalNode DIV() { return getToken(ArcticExtendSparkSqlParser.DIV, 0); }
		public TerminalNode PLUS() { return getToken(ArcticExtendSparkSqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public TerminalNode CONCAT_PIPE() { return getToken(ArcticExtendSparkSqlParser.CONCAT_PIPE, 0); }
		public TerminalNode AMPERSAND() { return getToken(ArcticExtendSparkSqlParser.AMPERSAND, 0); }
		public TerminalNode HAT() { return getToken(ArcticExtendSparkSqlParser.HAT, 0); }
		public TerminalNode PIPE() { return getToken(ArcticExtendSparkSqlParser.PIPE, 0); }
		public ArithmeticBinaryContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterArithmeticBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitArithmeticBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitArithmeticBinary(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ArithmeticUnaryContext extends ValueExpressionContext {
		public Token operator;
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(ArcticExtendSparkSqlParser.PLUS, 0); }
		public TerminalNode TILDE() { return getToken(ArcticExtendSparkSqlParser.TILDE, 0); }
		public ArithmeticUnaryContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterArithmeticUnary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitArithmeticUnary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitArithmeticUnary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueExpressionContext valueExpression() throws RecognitionException {
		return valueExpression(0);
	}

	private ValueExpressionContext valueExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ValueExpressionContext _localctx = new ValueExpressionContext(_ctx, _parentState);
		ValueExpressionContext _prevctx = _localctx;
		int _startState = 224;
		enterRecursionRule(_localctx, 224, RULE_valueExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2698);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,355,_ctx) ) {
			case 1:
				{
				_localctx = new ValueExpressionDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2695);
				primaryExpression(0);
				}
				break;
			case 2:
				{
				_localctx = new ArithmeticUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2696);
				((ArithmeticUnaryContext)_localctx).operator = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 297)) & ~0x3f) == 0 && ((1L << (_la - 297)) & ((1L << (PLUS - 297)) | (1L << (MINUS - 297)) | (1L << (TILDE - 297)))) != 0)) ) {
					((ArithmeticUnaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2697);
				valueExpression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2721);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,357,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2719);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,356,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2700);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2701);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==DIV || ((((_la - 299)) & ~0x3f) == 0 && ((1L << (_la - 299)) & ((1L << (ASTERISK - 299)) | (1L << (SLASH - 299)) | (1L << (PERCENT - 299)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2702);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(7);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2703);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(2704);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 297)) & ~0x3f) == 0 && ((1L << (_la - 297)) & ((1L << (PLUS - 297)) | (1L << (MINUS - 297)) | (1L << (CONCAT_PIPE - 297)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2705);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(6);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2706);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(2707);
						((ArithmeticBinaryContext)_localctx).operator = match(AMPERSAND);
						setState(2708);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(5);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2709);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(2710);
						((ArithmeticBinaryContext)_localctx).operator = match(HAT);
						setState(2711);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(4);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2712);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2713);
						((ArithmeticBinaryContext)_localctx).operator = match(PIPE);
						setState(2714);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(3);
						}
						break;
					case 6:
						{
						_localctx = new ComparisonContext(new ValueExpressionContext(_parentctx, _parentState));
						((ComparisonContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2715);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2716);
						comparisonOperator();
						setState(2717);
						((ComparisonContext)_localctx).right = valueExpression(2);
						}
						break;
					}
					} 
				}
				setState(2723);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,357,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class DatetimeUnitContext extends ParserRuleContext {
		public TerminalNode YEAR() { return getToken(ArcticExtendSparkSqlParser.YEAR, 0); }
		public TerminalNode QUARTER() { return getToken(ArcticExtendSparkSqlParser.QUARTER, 0); }
		public TerminalNode MONTH() { return getToken(ArcticExtendSparkSqlParser.MONTH, 0); }
		public TerminalNode WEEK() { return getToken(ArcticExtendSparkSqlParser.WEEK, 0); }
		public TerminalNode DAY() { return getToken(ArcticExtendSparkSqlParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(ArcticExtendSparkSqlParser.DAYOFYEAR, 0); }
		public TerminalNode HOUR() { return getToken(ArcticExtendSparkSqlParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(ArcticExtendSparkSqlParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(ArcticExtendSparkSqlParser.SECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(ArcticExtendSparkSqlParser.MILLISECOND, 0); }
		public TerminalNode MICROSECOND() { return getToken(ArcticExtendSparkSqlParser.MICROSECOND, 0); }
		public DatetimeUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datetimeUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDatetimeUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDatetimeUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDatetimeUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatetimeUnitContext datetimeUnit() throws RecognitionException {
		DatetimeUnitContext _localctx = new DatetimeUnitContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_datetimeUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2724);
			_la = _input.LA(1);
			if ( !(_la==DAY || _la==DAYOFYEAR || ((((_la - 113)) & ~0x3f) == 0 && ((1L << (_la - 113)) & ((1L << (HOUR - 113)) | (1L << (MICROSECOND - 113)) | (1L << (MILLISECOND - 113)) | (1L << (MINUTE - 113)) | (1L << (MONTH - 113)))) != 0) || _la==QUARTER || _la==SECOND || _la==WEEK || _la==YEAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimaryExpressionContext extends ParserRuleContext {
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
	 
		public PrimaryExpressionContext() { }
		public void copyFrom(PrimaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StructContext extends PrimaryExpressionContext {
		public NamedExpressionContext namedExpression;
		public List<NamedExpressionContext> argument = new ArrayList<NamedExpressionContext>();
		public TerminalNode STRUCT() { return getToken(ArcticExtendSparkSqlParser.STRUCT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public StructContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitStruct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitStruct(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DereferenceContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext base;
		public IdentifierContext fieldName;
		public TerminalNode DOT() { return getToken(ArcticExtendSparkSqlParser.DOT, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DereferenceContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDereference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDereference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDereference(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TimestampaddContext extends PrimaryExpressionContext {
		public Token name;
		public DatetimeUnitContext unit;
		public ValueExpressionContext unitsAmount;
		public ValueExpressionContext timestamp;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public DatetimeUnitContext datetimeUnit() {
			return getRuleContext(DatetimeUnitContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode TIMESTAMPADD() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMPADD, 0); }
		public TerminalNode DATEADD() { return getToken(ArcticExtendSparkSqlParser.DATEADD, 0); }
		public TimestampaddContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTimestampadd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTimestampadd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTimestampadd(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubstringContext extends PrimaryExpressionContext {
		public ValueExpressionContext str;
		public ValueExpressionContext pos;
		public ValueExpressionContext len;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode SUBSTR() { return getToken(ArcticExtendSparkSqlParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticExtendSparkSqlParser.SUBSTRING, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public SubstringContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSubstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSubstring(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSubstring(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CastContext extends PrimaryExpressionContext {
		public Token name;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode CAST() { return getToken(ArcticExtendSparkSqlParser.CAST, 0); }
		public TerminalNode TRY_CAST() { return getToken(ArcticExtendSparkSqlParser.TRY_CAST, 0); }
		public CastContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCast(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCast(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LambdaContext extends PrimaryExpressionContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(ArcticExtendSparkSqlParser.ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public LambdaContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesizedExpressionContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public ParenthesizedExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TrimContext extends PrimaryExpressionContext {
		public Token trimOption;
		public ValueExpressionContext trimStr;
		public ValueExpressionContext srcStr;
		public TerminalNode TRIM() { return getToken(ArcticExtendSparkSqlParser.TRIM, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode BOTH() { return getToken(ArcticExtendSparkSqlParser.BOTH, 0); }
		public TerminalNode LEADING() { return getToken(ArcticExtendSparkSqlParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(ArcticExtendSparkSqlParser.TRAILING, 0); }
		public TrimContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTrim(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTrim(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTrim(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SimpleCaseContext extends PrimaryExpressionContext {
		public ExpressionContext value;
		public ExpressionContext elseExpression;
		public TerminalNode CASE() { return getToken(ArcticExtendSparkSqlParser.CASE, 0); }
		public TerminalNode END() { return getToken(ArcticExtendSparkSqlParser.END, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ArcticExtendSparkSqlParser.ELSE, 0); }
		public SimpleCaseContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSimpleCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSimpleCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSimpleCase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CurrentLikeContext extends PrimaryExpressionContext {
		public Token name;
		public TerminalNode CURRENT_DATE() { return getToken(ArcticExtendSparkSqlParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(ArcticExtendSparkSqlParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(ArcticExtendSparkSqlParser.CURRENT_USER, 0); }
		public CurrentLikeContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterCurrentLike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitCurrentLike(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitCurrentLike(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ColumnReferenceContext extends PrimaryExpressionContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ColumnReferenceContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterColumnReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitColumnReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitColumnReference(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RowConstructorContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public RowConstructorContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRowConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRowConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRowConstructor(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LastContext extends PrimaryExpressionContext {
		public TerminalNode LAST() { return getToken(ArcticExtendSparkSqlParser.LAST, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticExtendSparkSqlParser.IGNORE, 0); }
		public TerminalNode NULLS() { return getToken(ArcticExtendSparkSqlParser.NULLS, 0); }
		public LastContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLast(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLast(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StarContext extends PrimaryExpressionContext {
		public TerminalNode ASTERISK() { return getToken(ArcticExtendSparkSqlParser.ASTERISK, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(ArcticExtendSparkSqlParser.DOT, 0); }
		public StarContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterStar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitStar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitStar(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OverlayContext extends PrimaryExpressionContext {
		public ValueExpressionContext input;
		public ValueExpressionContext replace;
		public ValueExpressionContext position;
		public ValueExpressionContext length;
		public TerminalNode OVERLAY() { return getToken(ArcticExtendSparkSqlParser.OVERLAY, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode PLACING() { return getToken(ArcticExtendSparkSqlParser.PLACING, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public OverlayContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterOverlay(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitOverlay(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitOverlay(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubscriptContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext value;
		public ValueExpressionContext index;
		public TerminalNode LEFT_BRACKET() { return getToken(ArcticExtendSparkSqlParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(ArcticExtendSparkSqlParser.RIGHT_BRACKET, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public SubscriptContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TimestampdiffContext extends PrimaryExpressionContext {
		public Token name;
		public DatetimeUnitContext unit;
		public ValueExpressionContext startTimestamp;
		public ValueExpressionContext endTimestamp;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public DatetimeUnitContext datetimeUnit() {
			return getRuleContext(DatetimeUnitContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode TIMESTAMPDIFF() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMPDIFF, 0); }
		public TerminalNode DATEDIFF() { return getToken(ArcticExtendSparkSqlParser.DATEDIFF, 0); }
		public TimestampdiffContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTimestampdiff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTimestampdiff(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTimestampdiff(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubqueryExpressionContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public SubqueryExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSubqueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSubqueryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSubqueryExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ConstantDefaultContext extends PrimaryExpressionContext {
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public ConstantDefaultContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterConstantDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitConstantDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitConstantDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExtractContext extends PrimaryExpressionContext {
		public IdentifierContext field;
		public ValueExpressionContext source;
		public TerminalNode EXTRACT() { return getToken(ArcticExtendSparkSqlParser.EXTRACT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public ExtractContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterExtract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitExtract(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitExtract(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PercentileContext extends PrimaryExpressionContext {
		public Token name;
		public ValueExpressionContext percentage;
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, i);
		}
		public TerminalNode WITHIN() { return getToken(ArcticExtendSparkSqlParser.WITHIN, 0); }
		public TerminalNode GROUP() { return getToken(ArcticExtendSparkSqlParser.GROUP, 0); }
		public TerminalNode ORDER() { return getToken(ArcticExtendSparkSqlParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public SortItemContext sortItem() {
			return getRuleContext(SortItemContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode PERCENTILE_CONT() { return getToken(ArcticExtendSparkSqlParser.PERCENTILE_CONT, 0); }
		public TerminalNode PERCENTILE_DISC() { return getToken(ArcticExtendSparkSqlParser.PERCENTILE_DISC, 0); }
		public TerminalNode OVER() { return getToken(ArcticExtendSparkSqlParser.OVER, 0); }
		public WindowSpecContext windowSpec() {
			return getRuleContext(WindowSpecContext.class,0);
		}
		public PercentileContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPercentile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPercentile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPercentile(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FunctionCallContext extends PrimaryExpressionContext {
		public ExpressionContext expression;
		public List<ExpressionContext> argument = new ArrayList<ExpressionContext>();
		public BooleanExpressionContext where;
		public Token nullsOption;
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticExtendSparkSqlParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, i);
		}
		public TerminalNode FILTER() { return getToken(ArcticExtendSparkSqlParser.FILTER, 0); }
		public TerminalNode WHERE() { return getToken(ArcticExtendSparkSqlParser.WHERE, 0); }
		public TerminalNode NULLS() { return getToken(ArcticExtendSparkSqlParser.NULLS, 0); }
		public TerminalNode OVER() { return getToken(ArcticExtendSparkSqlParser.OVER, 0); }
		public WindowSpecContext windowSpec() {
			return getRuleContext(WindowSpecContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public TerminalNode IGNORE() { return getToken(ArcticExtendSparkSqlParser.IGNORE, 0); }
		public TerminalNode RESPECT() { return getToken(ArcticExtendSparkSqlParser.RESPECT, 0); }
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public FunctionCallContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SearchedCaseContext extends PrimaryExpressionContext {
		public ExpressionContext elseExpression;
		public TerminalNode CASE() { return getToken(ArcticExtendSparkSqlParser.CASE, 0); }
		public TerminalNode END() { return getToken(ArcticExtendSparkSqlParser.END, 0); }
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ArcticExtendSparkSqlParser.ELSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SearchedCaseContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSearchedCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSearchedCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSearchedCase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PositionContext extends PrimaryExpressionContext {
		public ValueExpressionContext substr;
		public ValueExpressionContext str;
		public TerminalNode POSITION() { return getToken(ArcticExtendSparkSqlParser.POSITION, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public PositionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPosition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FirstContext extends PrimaryExpressionContext {
		public TerminalNode FIRST() { return getToken(ArcticExtendSparkSqlParser.FIRST, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticExtendSparkSqlParser.IGNORE, 0); }
		public TerminalNode NULLS() { return getToken(ArcticExtendSparkSqlParser.NULLS, 0); }
		public FirstContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFirst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFirst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFirst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		return primaryExpression(0);
	}

	private PrimaryExpressionContext primaryExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, _parentState);
		PrimaryExpressionContext _prevctx = _localctx;
		int _startState = 228;
		enterRecursionRule(_localctx, 228, RULE_primaryExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2947);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,379,_ctx) ) {
			case 1:
				{
				_localctx = new CurrentLikeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2727);
				((CurrentLikeContext)_localctx).name = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CURRENT_DATE) | (1L << CURRENT_TIMESTAMP) | (1L << CURRENT_USER))) != 0)) ) {
					((CurrentLikeContext)_localctx).name = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				{
				_localctx = new TimestampaddContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2728);
				((TimestampaddContext)_localctx).name = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==DATEADD || _la==TIMESTAMPADD) ) {
					((TimestampaddContext)_localctx).name = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2729);
				match(LEFT_PAREN);
				setState(2730);
				((TimestampaddContext)_localctx).unit = datetimeUnit();
				setState(2731);
				match(COMMA);
				setState(2732);
				((TimestampaddContext)_localctx).unitsAmount = valueExpression(0);
				setState(2733);
				match(COMMA);
				setState(2734);
				((TimestampaddContext)_localctx).timestamp = valueExpression(0);
				setState(2735);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new TimestampdiffContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2737);
				((TimestampdiffContext)_localctx).name = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==DATEDIFF || _la==TIMESTAMPDIFF) ) {
					((TimestampdiffContext)_localctx).name = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2738);
				match(LEFT_PAREN);
				setState(2739);
				((TimestampdiffContext)_localctx).unit = datetimeUnit();
				setState(2740);
				match(COMMA);
				setState(2741);
				((TimestampdiffContext)_localctx).startTimestamp = valueExpression(0);
				setState(2742);
				match(COMMA);
				setState(2743);
				((TimestampdiffContext)_localctx).endTimestamp = valueExpression(0);
				setState(2744);
				match(RIGHT_PAREN);
				}
				break;
			case 4:
				{
				_localctx = new SearchedCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2746);
				match(CASE);
				setState(2748); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2747);
					whenClause();
					}
					}
					setState(2750); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2754);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2752);
					match(ELSE);
					setState(2753);
					((SearchedCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2756);
				match(END);
				}
				break;
			case 5:
				{
				_localctx = new SimpleCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2758);
				match(CASE);
				setState(2759);
				((SimpleCaseContext)_localctx).value = expression();
				setState(2761); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2760);
					whenClause();
					}
					}
					setState(2763); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2767);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2765);
					match(ELSE);
					setState(2766);
					((SimpleCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2769);
				match(END);
				}
				break;
			case 6:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2771);
				((CastContext)_localctx).name = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==CAST || _la==TRY_CAST) ) {
					((CastContext)_localctx).name = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2772);
				match(LEFT_PAREN);
				setState(2773);
				expression();
				setState(2774);
				match(AS);
				setState(2775);
				dataType();
				setState(2776);
				match(RIGHT_PAREN);
				}
				break;
			case 7:
				{
				_localctx = new StructContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2778);
				match(STRUCT);
				setState(2779);
				match(LEFT_PAREN);
				setState(2788);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,363,_ctx) ) {
				case 1:
					{
					setState(2780);
					((StructContext)_localctx).namedExpression = namedExpression();
					((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
					setState(2785);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2781);
						match(COMMA);
						setState(2782);
						((StructContext)_localctx).namedExpression = namedExpression();
						((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
						}
						}
						setState(2787);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2790);
				match(RIGHT_PAREN);
				}
				break;
			case 8:
				{
				_localctx = new FirstContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2791);
				match(FIRST);
				setState(2792);
				match(LEFT_PAREN);
				setState(2793);
				expression();
				setState(2796);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2794);
					match(IGNORE);
					setState(2795);
					match(NULLS);
					}
				}

				setState(2798);
				match(RIGHT_PAREN);
				}
				break;
			case 9:
				{
				_localctx = new LastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2800);
				match(LAST);
				setState(2801);
				match(LEFT_PAREN);
				setState(2802);
				expression();
				setState(2805);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2803);
					match(IGNORE);
					setState(2804);
					match(NULLS);
					}
				}

				setState(2807);
				match(RIGHT_PAREN);
				}
				break;
			case 10:
				{
				_localctx = new PositionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2809);
				match(POSITION);
				setState(2810);
				match(LEFT_PAREN);
				setState(2811);
				((PositionContext)_localctx).substr = valueExpression(0);
				setState(2812);
				match(IN);
				setState(2813);
				((PositionContext)_localctx).str = valueExpression(0);
				setState(2814);
				match(RIGHT_PAREN);
				}
				break;
			case 11:
				{
				_localctx = new ConstantDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2816);
				constant();
				}
				break;
			case 12:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2817);
				match(ASTERISK);
				}
				break;
			case 13:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2818);
				qualifiedName();
				setState(2819);
				match(DOT);
				setState(2820);
				match(ASTERISK);
				}
				break;
			case 14:
				{
				_localctx = new RowConstructorContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2822);
				match(LEFT_PAREN);
				setState(2823);
				namedExpression();
				setState(2826); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2824);
					match(COMMA);
					setState(2825);
					namedExpression();
					}
					}
					setState(2828); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(2830);
				match(RIGHT_PAREN);
				}
				break;
			case 15:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2832);
				match(LEFT_PAREN);
				setState(2833);
				query();
				setState(2834);
				match(RIGHT_PAREN);
				}
				break;
			case 16:
				{
				_localctx = new FunctionCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2836);
				functionName();
				setState(2837);
				match(LEFT_PAREN);
				setState(2849);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,369,_ctx) ) {
				case 1:
					{
					setState(2839);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,367,_ctx) ) {
					case 1:
						{
						setState(2838);
						setQuantifier();
						}
						break;
					}
					setState(2841);
					((FunctionCallContext)_localctx).expression = expression();
					((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
					setState(2846);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2842);
						match(COMMA);
						setState(2843);
						((FunctionCallContext)_localctx).expression = expression();
						((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
						}
						}
						setState(2848);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2851);
				match(RIGHT_PAREN);
				setState(2858);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,370,_ctx) ) {
				case 1:
					{
					setState(2852);
					match(FILTER);
					setState(2853);
					match(LEFT_PAREN);
					setState(2854);
					match(WHERE);
					setState(2855);
					((FunctionCallContext)_localctx).where = booleanExpression(0);
					setState(2856);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(2862);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,371,_ctx) ) {
				case 1:
					{
					setState(2860);
					((FunctionCallContext)_localctx).nullsOption = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==IGNORE || _la==RESPECT) ) {
						((FunctionCallContext)_localctx).nullsOption = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2861);
					match(NULLS);
					}
					break;
				}
				setState(2866);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,372,_ctx) ) {
				case 1:
					{
					setState(2864);
					match(OVER);
					setState(2865);
					windowSpec();
					}
					break;
				}
				}
				break;
			case 17:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2868);
				identifier();
				setState(2869);
				match(ARROW);
				setState(2870);
				expression();
				}
				break;
			case 18:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2872);
				match(LEFT_PAREN);
				setState(2873);
				identifier();
				setState(2876); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2874);
					match(COMMA);
					setState(2875);
					identifier();
					}
					}
					setState(2878); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(2880);
				match(RIGHT_PAREN);
				setState(2881);
				match(ARROW);
				setState(2882);
				expression();
				}
				break;
			case 19:
				{
				_localctx = new ColumnReferenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2884);
				identifier();
				}
				break;
			case 20:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2885);
				match(LEFT_PAREN);
				setState(2886);
				expression();
				setState(2887);
				match(RIGHT_PAREN);
				}
				break;
			case 21:
				{
				_localctx = new ExtractContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2889);
				match(EXTRACT);
				setState(2890);
				match(LEFT_PAREN);
				setState(2891);
				((ExtractContext)_localctx).field = identifier();
				setState(2892);
				match(FROM);
				setState(2893);
				((ExtractContext)_localctx).source = valueExpression(0);
				setState(2894);
				match(RIGHT_PAREN);
				}
				break;
			case 22:
				{
				_localctx = new SubstringContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2896);
				_la = _input.LA(1);
				if ( !(_la==SUBSTR || _la==SUBSTRING) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2897);
				match(LEFT_PAREN);
				setState(2898);
				((SubstringContext)_localctx).str = valueExpression(0);
				setState(2899);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==FROM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2900);
				((SubstringContext)_localctx).pos = valueExpression(0);
				setState(2903);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA || _la==FOR) {
					{
					setState(2901);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==FOR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2902);
					((SubstringContext)_localctx).len = valueExpression(0);
					}
				}

				setState(2905);
				match(RIGHT_PAREN);
				}
				break;
			case 23:
				{
				_localctx = new TrimContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2907);
				match(TRIM);
				setState(2908);
				match(LEFT_PAREN);
				setState(2910);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,375,_ctx) ) {
				case 1:
					{
					setState(2909);
					((TrimContext)_localctx).trimOption = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==BOTH || _la==LEADING || _la==TRAILING) ) {
						((TrimContext)_localctx).trimOption = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					break;
				}
				setState(2913);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,376,_ctx) ) {
				case 1:
					{
					setState(2912);
					((TrimContext)_localctx).trimStr = valueExpression(0);
					}
					break;
				}
				setState(2915);
				match(FROM);
				setState(2916);
				((TrimContext)_localctx).srcStr = valueExpression(0);
				setState(2917);
				match(RIGHT_PAREN);
				}
				break;
			case 24:
				{
				_localctx = new OverlayContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2919);
				match(OVERLAY);
				setState(2920);
				match(LEFT_PAREN);
				setState(2921);
				((OverlayContext)_localctx).input = valueExpression(0);
				setState(2922);
				match(PLACING);
				setState(2923);
				((OverlayContext)_localctx).replace = valueExpression(0);
				setState(2924);
				match(FROM);
				setState(2925);
				((OverlayContext)_localctx).position = valueExpression(0);
				setState(2928);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2926);
					match(FOR);
					setState(2927);
					((OverlayContext)_localctx).length = valueExpression(0);
					}
				}

				setState(2930);
				match(RIGHT_PAREN);
				}
				break;
			case 25:
				{
				_localctx = new PercentileContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2932);
				((PercentileContext)_localctx).name = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==PERCENTILE_CONT || _la==PERCENTILE_DISC) ) {
					((PercentileContext)_localctx).name = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2933);
				match(LEFT_PAREN);
				setState(2934);
				((PercentileContext)_localctx).percentage = valueExpression(0);
				setState(2935);
				match(RIGHT_PAREN);
				setState(2936);
				match(WITHIN);
				setState(2937);
				match(GROUP);
				setState(2938);
				match(LEFT_PAREN);
				setState(2939);
				match(ORDER);
				setState(2940);
				match(BY);
				setState(2941);
				sortItem();
				setState(2942);
				match(RIGHT_PAREN);
				setState(2945);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,378,_ctx) ) {
				case 1:
					{
					setState(2943);
					match(OVER);
					setState(2944);
					windowSpec();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2959);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,381,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2957);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,380,_ctx) ) {
					case 1:
						{
						_localctx = new SubscriptContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((SubscriptContext)_localctx).value = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2949);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(2950);
						match(LEFT_BRACKET);
						setState(2951);
						((SubscriptContext)_localctx).index = valueExpression(0);
						setState(2952);
						match(RIGHT_BRACKET);
						}
						break;
					case 2:
						{
						_localctx = new DereferenceContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((DereferenceContext)_localctx).base = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2954);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(2955);
						match(DOT);
						setState(2956);
						((DereferenceContext)_localctx).fieldName = identifier();
						}
						break;
					}
					} 
				}
				setState(2961);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,381,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ConstantContext extends ParserRuleContext {
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
	 
		public ConstantContext() { }
		public void copyFrom(ConstantContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NullLiteralContext extends ConstantContext {
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public NullLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNullLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNullLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNullLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringLiteralContext extends ConstantContext {
		public List<TerminalNode> STRING() { return getTokens(ArcticExtendSparkSqlParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticExtendSparkSqlParser.STRING, i);
		}
		public StringLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TypeConstructorContext extends ConstantContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TypeConstructorContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTypeConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTypeConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTypeConstructor(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IntervalLiteralContext extends ConstantContext {
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public IntervalLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIntervalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIntervalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIntervalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericLiteralContext extends ConstantContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public NumericLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanLiteralContext extends ConstantContext {
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public BooleanLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_constant);
		try {
			int _alt;
			setState(2974);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,383,_ctx) ) {
			case 1:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2962);
				match(NULL);
				}
				break;
			case 2:
				_localctx = new IntervalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2963);
				interval();
				}
				break;
			case 3:
				_localctx = new TypeConstructorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2964);
				identifier();
				setState(2965);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new NumericLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2967);
				number();
				}
				break;
			case 5:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2968);
				booleanValue();
				}
				break;
			case 6:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2970); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2969);
						match(STRING);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2972); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,382,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(ArcticExtendSparkSqlParser.NEQ, 0); }
		public TerminalNode NEQJ() { return getToken(ArcticExtendSparkSqlParser.NEQJ, 0); }
		public TerminalNode LT() { return getToken(ArcticExtendSparkSqlParser.LT, 0); }
		public TerminalNode LTE() { return getToken(ArcticExtendSparkSqlParser.LTE, 0); }
		public TerminalNode GT() { return getToken(ArcticExtendSparkSqlParser.GT, 0); }
		public TerminalNode GTE() { return getToken(ArcticExtendSparkSqlParser.GTE, 0); }
		public TerminalNode NSEQ() { return getToken(ArcticExtendSparkSqlParser.NSEQ, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2976);
			_la = _input.LA(1);
			if ( !(((((_la - 289)) & ~0x3f) == 0 && ((1L << (_la - 289)) & ((1L << (EQ - 289)) | (1L << (NSEQ - 289)) | (1L << (NEQ - 289)) | (1L << (NEQJ - 289)) | (1L << (LT - 289)) | (1L << (LTE - 289)) | (1L << (GT - 289)) | (1L << (GTE - 289)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArithmeticOperatorContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(ArcticExtendSparkSqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticExtendSparkSqlParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(ArcticExtendSparkSqlParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(ArcticExtendSparkSqlParser.PERCENT, 0); }
		public TerminalNode DIV() { return getToken(ArcticExtendSparkSqlParser.DIV, 0); }
		public TerminalNode TILDE() { return getToken(ArcticExtendSparkSqlParser.TILDE, 0); }
		public TerminalNode AMPERSAND() { return getToken(ArcticExtendSparkSqlParser.AMPERSAND, 0); }
		public TerminalNode PIPE() { return getToken(ArcticExtendSparkSqlParser.PIPE, 0); }
		public TerminalNode CONCAT_PIPE() { return getToken(ArcticExtendSparkSqlParser.CONCAT_PIPE, 0); }
		public TerminalNode HAT() { return getToken(ArcticExtendSparkSqlParser.HAT, 0); }
		public ArithmeticOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arithmeticOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterArithmeticOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitArithmeticOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitArithmeticOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArithmeticOperatorContext arithmeticOperator() throws RecognitionException {
		ArithmeticOperatorContext _localctx = new ArithmeticOperatorContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_arithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2978);
			_la = _input.LA(1);
			if ( !(_la==DIV || ((((_la - 297)) & ~0x3f) == 0 && ((1L << (_la - 297)) & ((1L << (PLUS - 297)) | (1L << (MINUS - 297)) | (1L << (ASTERISK - 297)) | (1L << (SLASH - 297)) | (1L << (PERCENT - 297)) | (1L << (TILDE - 297)) | (1L << (AMPERSAND - 297)) | (1L << (PIPE - 297)) | (1L << (CONCAT_PIPE - 297)) | (1L << (HAT - 297)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateOperatorContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public PredicateOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPredicateOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPredicateOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPredicateOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateOperatorContext predicateOperator() throws RecognitionException {
		PredicateOperatorContext _localctx = new PredicateOperatorContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_predicateOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2980);
			_la = _input.LA(1);
			if ( !(_la==AND || ((((_la - 117)) & ~0x3f) == 0 && ((1L << (_la - 117)) & ((1L << (IN - 117)) | (1L << (NOT - 117)) | (1L << (OR - 117)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanValueContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(ArcticExtendSparkSqlParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ArcticExtendSparkSqlParser.FALSE, 0); }
		public BooleanValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterBooleanValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitBooleanValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitBooleanValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanValueContext booleanValue() throws RecognitionException {
		BooleanValueContext _localctx = new BooleanValueContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_booleanValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2982);
			_la = _input.LA(1);
			if ( !(_la==FALSE || _la==TRUE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntervalContext extends ParserRuleContext {
		public TerminalNode INTERVAL() { return getToken(ArcticExtendSparkSqlParser.INTERVAL, 0); }
		public ErrorCapturingMultiUnitsIntervalContext errorCapturingMultiUnitsInterval() {
			return getRuleContext(ErrorCapturingMultiUnitsIntervalContext.class,0);
		}
		public ErrorCapturingUnitToUnitIntervalContext errorCapturingUnitToUnitInterval() {
			return getRuleContext(ErrorCapturingUnitToUnitIntervalContext.class,0);
		}
		public IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2984);
			match(INTERVAL);
			setState(2987);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,384,_ctx) ) {
			case 1:
				{
				setState(2985);
				errorCapturingMultiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2986);
				errorCapturingUnitToUnitInterval();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorCapturingMultiUnitsIntervalContext extends ParserRuleContext {
		public MultiUnitsIntervalContext body;
		public MultiUnitsIntervalContext multiUnitsInterval() {
			return getRuleContext(MultiUnitsIntervalContext.class,0);
		}
		public UnitToUnitIntervalContext unitToUnitInterval() {
			return getRuleContext(UnitToUnitIntervalContext.class,0);
		}
		public ErrorCapturingMultiUnitsIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCapturingMultiUnitsInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterErrorCapturingMultiUnitsInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitErrorCapturingMultiUnitsInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitErrorCapturingMultiUnitsInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingMultiUnitsIntervalContext errorCapturingMultiUnitsInterval() throws RecognitionException {
		ErrorCapturingMultiUnitsIntervalContext _localctx = new ErrorCapturingMultiUnitsIntervalContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_errorCapturingMultiUnitsInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2989);
			((ErrorCapturingMultiUnitsIntervalContext)_localctx).body = multiUnitsInterval();
			setState(2991);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,385,_ctx) ) {
			case 1:
				{
				setState(2990);
				unitToUnitInterval();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultiUnitsIntervalContext extends ParserRuleContext {
		public IdentifierContext identifier;
		public List<IdentifierContext> unit = new ArrayList<IdentifierContext>();
		public List<IntervalValueContext> intervalValue() {
			return getRuleContexts(IntervalValueContext.class);
		}
		public IntervalValueContext intervalValue(int i) {
			return getRuleContext(IntervalValueContext.class,i);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public MultiUnitsIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiUnitsInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterMultiUnitsInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitMultiUnitsInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitMultiUnitsInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiUnitsIntervalContext multiUnitsInterval() throws RecognitionException {
		MultiUnitsIntervalContext _localctx = new MultiUnitsIntervalContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_multiUnitsInterval);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2996); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2993);
					intervalValue();
					setState(2994);
					((MultiUnitsIntervalContext)_localctx).identifier = identifier();
					((MultiUnitsIntervalContext)_localctx).unit.add(((MultiUnitsIntervalContext)_localctx).identifier);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2998); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,386,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorCapturingUnitToUnitIntervalContext extends ParserRuleContext {
		public UnitToUnitIntervalContext body;
		public MultiUnitsIntervalContext error1;
		public UnitToUnitIntervalContext error2;
		public List<UnitToUnitIntervalContext> unitToUnitInterval() {
			return getRuleContexts(UnitToUnitIntervalContext.class);
		}
		public UnitToUnitIntervalContext unitToUnitInterval(int i) {
			return getRuleContext(UnitToUnitIntervalContext.class,i);
		}
		public MultiUnitsIntervalContext multiUnitsInterval() {
			return getRuleContext(MultiUnitsIntervalContext.class,0);
		}
		public ErrorCapturingUnitToUnitIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCapturingUnitToUnitInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterErrorCapturingUnitToUnitInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitErrorCapturingUnitToUnitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitErrorCapturingUnitToUnitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingUnitToUnitIntervalContext errorCapturingUnitToUnitInterval() throws RecognitionException {
		ErrorCapturingUnitToUnitIntervalContext _localctx = new ErrorCapturingUnitToUnitIntervalContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_errorCapturingUnitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3000);
			((ErrorCapturingUnitToUnitIntervalContext)_localctx).body = unitToUnitInterval();
			setState(3003);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,387,_ctx) ) {
			case 1:
				{
				setState(3001);
				((ErrorCapturingUnitToUnitIntervalContext)_localctx).error1 = multiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(3002);
				((ErrorCapturingUnitToUnitIntervalContext)_localctx).error2 = unitToUnitInterval();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnitToUnitIntervalContext extends ParserRuleContext {
		public IntervalValueContext value;
		public IdentifierContext from;
		public IdentifierContext to;
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public IntervalValueContext intervalValue() {
			return getRuleContext(IntervalValueContext.class,0);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public UnitToUnitIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unitToUnitInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUnitToUnitInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUnitToUnitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUnitToUnitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnitToUnitIntervalContext unitToUnitInterval() throws RecognitionException {
		UnitToUnitIntervalContext _localctx = new UnitToUnitIntervalContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_unitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3005);
			((UnitToUnitIntervalContext)_localctx).value = intervalValue();
			setState(3006);
			((UnitToUnitIntervalContext)_localctx).from = identifier();
			setState(3007);
			match(TO);
			setState(3008);
			((UnitToUnitIntervalContext)_localctx).to = identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntervalValueContext extends ParserRuleContext {
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticExtendSparkSqlParser.DECIMAL_VALUE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode PLUS() { return getToken(ArcticExtendSparkSqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public IntervalValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIntervalValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIntervalValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIntervalValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalValueContext intervalValue() throws RecognitionException {
		IntervalValueContext _localctx = new IntervalValueContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_intervalValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3011);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(3010);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(3013);
			_la = _input.LA(1);
			if ( !(((((_la - 311)) & ~0x3f) == 0 && ((1L << (_la - 311)) & ((1L << (STRING - 311)) | (1L << (INTEGER_VALUE - 311)) | (1L << (DECIMAL_VALUE - 311)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColPositionContext extends ParserRuleContext {
		public Token position;
		public ErrorCapturingIdentifierContext afterCol;
		public TerminalNode FIRST() { return getToken(ArcticExtendSparkSqlParser.FIRST, 0); }
		public TerminalNode AFTER() { return getToken(ArcticExtendSparkSqlParser.AFTER, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public ColPositionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colPosition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterColPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitColPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitColPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColPositionContext colPosition() throws RecognitionException {
		ColPositionContext _localctx = new ColPositionContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_colPosition);
		try {
			setState(3018);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				enterOuterAlt(_localctx, 1);
				{
				setState(3015);
				((ColPositionContext)_localctx).position = match(FIRST);
				}
				break;
			case AFTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(3016);
				((ColPositionContext)_localctx).position = match(AFTER);
				setState(3017);
				((ColPositionContext)_localctx).afterCol = errorCapturingIdentifier();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataTypeContext extends ParserRuleContext {
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
	 
		public DataTypeContext() { }
		public void copyFrom(DataTypeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ComplexDataTypeContext extends DataTypeContext {
		public Token complex;
		public TerminalNode LT() { return getToken(ArcticExtendSparkSqlParser.LT, 0); }
		public List<DataTypeContext> dataType() {
			return getRuleContexts(DataTypeContext.class);
		}
		public DataTypeContext dataType(int i) {
			return getRuleContext(DataTypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(ArcticExtendSparkSqlParser.GT, 0); }
		public TerminalNode ARRAY() { return getToken(ArcticExtendSparkSqlParser.ARRAY, 0); }
		public TerminalNode COMMA() { return getToken(ArcticExtendSparkSqlParser.COMMA, 0); }
		public TerminalNode MAP() { return getToken(ArcticExtendSparkSqlParser.MAP, 0); }
		public TerminalNode STRUCT() { return getToken(ArcticExtendSparkSqlParser.STRUCT, 0); }
		public TerminalNode NEQ() { return getToken(ArcticExtendSparkSqlParser.NEQ, 0); }
		public ComplexColTypeListContext complexColTypeList() {
			return getRuleContext(ComplexColTypeListContext.class,0);
		}
		public ComplexDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterComplexDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitComplexDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitComplexDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YearMonthIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(ArcticExtendSparkSqlParser.INTERVAL, 0); }
		public TerminalNode YEAR() { return getToken(ArcticExtendSparkSqlParser.YEAR, 0); }
		public List<TerminalNode> MONTH() { return getTokens(ArcticExtendSparkSqlParser.MONTH); }
		public TerminalNode MONTH(int i) {
			return getToken(ArcticExtendSparkSqlParser.MONTH, i);
		}
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public YearMonthIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterYearMonthIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitYearMonthIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitYearMonthIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DayTimeIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(ArcticExtendSparkSqlParser.INTERVAL, 0); }
		public TerminalNode DAY() { return getToken(ArcticExtendSparkSqlParser.DAY, 0); }
		public List<TerminalNode> HOUR() { return getTokens(ArcticExtendSparkSqlParser.HOUR); }
		public TerminalNode HOUR(int i) {
			return getToken(ArcticExtendSparkSqlParser.HOUR, i);
		}
		public List<TerminalNode> MINUTE() { return getTokens(ArcticExtendSparkSqlParser.MINUTE); }
		public TerminalNode MINUTE(int i) {
			return getToken(ArcticExtendSparkSqlParser.MINUTE, i);
		}
		public List<TerminalNode> SECOND() { return getTokens(ArcticExtendSparkSqlParser.SECOND); }
		public TerminalNode SECOND(int i) {
			return getToken(ArcticExtendSparkSqlParser.SECOND, i);
		}
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public DayTimeIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDayTimeIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDayTimeIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDayTimeIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimitiveDataTypeContext extends DataTypeContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(ArcticExtendSparkSqlParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public PrimitiveDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterPrimitiveDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitPrimitiveDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitPrimitiveDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_dataType);
		int _la;
		try {
			setState(3066);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,396,_ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3020);
				((ComplexDataTypeContext)_localctx).complex = match(ARRAY);
				setState(3021);
				match(LT);
				setState(3022);
				dataType();
				setState(3023);
				match(GT);
				}
				break;
			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3025);
				((ComplexDataTypeContext)_localctx).complex = match(MAP);
				setState(3026);
				match(LT);
				setState(3027);
				dataType();
				setState(3028);
				match(COMMA);
				setState(3029);
				dataType();
				setState(3030);
				match(GT);
				}
				break;
			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3032);
				((ComplexDataTypeContext)_localctx).complex = match(STRUCT);
				setState(3039);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LT:
					{
					setState(3033);
					match(LT);
					setState(3035);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,390,_ctx) ) {
					case 1:
						{
						setState(3034);
						complexColTypeList();
						}
						break;
					}
					setState(3037);
					match(GT);
					}
					break;
				case NEQ:
					{
					setState(3038);
					match(NEQ);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 4:
				_localctx = new YearMonthIntervalDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3041);
				match(INTERVAL);
				setState(3042);
				((YearMonthIntervalDataTypeContext)_localctx).from = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==MONTH || _la==YEAR) ) {
					((YearMonthIntervalDataTypeContext)_localctx).from = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(3045);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,392,_ctx) ) {
				case 1:
					{
					setState(3043);
					match(TO);
					setState(3044);
					((YearMonthIntervalDataTypeContext)_localctx).to = match(MONTH);
					}
					break;
				}
				}
				break;
			case 5:
				_localctx = new DayTimeIntervalDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(3047);
				match(INTERVAL);
				setState(3048);
				((DayTimeIntervalDataTypeContext)_localctx).from = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==DAY || _la==HOUR || _la==MINUTE || _la==SECOND) ) {
					((DayTimeIntervalDataTypeContext)_localctx).from = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(3051);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,393,_ctx) ) {
				case 1:
					{
					setState(3049);
					match(TO);
					setState(3050);
					((DayTimeIntervalDataTypeContext)_localctx).to = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==HOUR || _la==MINUTE || _la==SECOND) ) {
						((DayTimeIntervalDataTypeContext)_localctx).to = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					break;
				}
				}
				break;
			case 6:
				_localctx = new PrimitiveDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(3053);
				identifier();
				setState(3064);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,395,_ctx) ) {
				case 1:
					{
					setState(3054);
					match(LEFT_PAREN);
					setState(3055);
					match(INTEGER_VALUE);
					setState(3060);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(3056);
						match(COMMA);
						setState(3057);
						match(INTEGER_VALUE);
						}
						}
						setState(3062);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(3063);
					match(RIGHT_PAREN);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedColTypeWithPositionListContext extends ParserRuleContext {
		public List<QualifiedColTypeWithPositionContext> qualifiedColTypeWithPosition() {
			return getRuleContexts(QualifiedColTypeWithPositionContext.class);
		}
		public QualifiedColTypeWithPositionContext qualifiedColTypeWithPosition(int i) {
			return getRuleContext(QualifiedColTypeWithPositionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public QualifiedColTypeWithPositionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedColTypeWithPositionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQualifiedColTypeWithPositionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQualifiedColTypeWithPositionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQualifiedColTypeWithPositionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() throws RecognitionException {
		QualifiedColTypeWithPositionListContext _localctx = new QualifiedColTypeWithPositionListContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_qualifiedColTypeWithPositionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3068);
			qualifiedColTypeWithPosition();
			setState(3073);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3069);
				match(COMMA);
				setState(3070);
				qualifiedColTypeWithPosition();
				}
				}
				setState(3075);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedColTypeWithPositionContext extends ParserRuleContext {
		public MultipartIdentifierContext name;
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public QualifiedColTypeWithPositionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedColTypeWithPosition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQualifiedColTypeWithPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQualifiedColTypeWithPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQualifiedColTypeWithPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedColTypeWithPositionContext qualifiedColTypeWithPosition() throws RecognitionException {
		QualifiedColTypeWithPositionContext _localctx = new QualifiedColTypeWithPositionContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_qualifiedColTypeWithPosition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3076);
			((QualifiedColTypeWithPositionContext)_localctx).name = multipartIdentifier();
			setState(3077);
			dataType();
			setState(3080);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(3078);
				match(NOT);
				setState(3079);
				match(NULL);
				}
			}

			setState(3083);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(3082);
				commentSpec();
				}
			}

			setState(3086);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AFTER || _la==FIRST) {
				{
				setState(3085);
				colPosition();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColTypeListContext extends ParserRuleContext {
		public List<ColTypeContext> colType() {
			return getRuleContexts(ColTypeContext.class);
		}
		public ColTypeContext colType(int i) {
			return getRuleContext(ColTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public ColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeListContext colTypeList() throws RecognitionException {
		ColTypeListContext _localctx = new ColTypeListContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_colTypeList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3088);
			colType();
			setState(3093);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,401,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3089);
					match(COMMA);
					setState(3090);
					colType();
					}
					} 
				}
				setState(3095);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,401,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColTypeContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext colName;
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeContext colType() throws RecognitionException {
		ColTypeContext _localctx = new ColTypeContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_colType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3096);
			((ColTypeContext)_localctx).colName = errorCapturingIdentifier();
			setState(3097);
			dataType();
			setState(3100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,402,_ctx) ) {
			case 1:
				{
				setState(3098);
				match(NOT);
				setState(3099);
				match(NULL);
				}
				break;
			}
			setState(3103);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,403,_ctx) ) {
			case 1:
				{
				setState(3102);
				commentSpec();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComplexColTypeListContext extends ParserRuleContext {
		public List<ComplexColTypeContext> complexColType() {
			return getRuleContexts(ComplexColTypeContext.class);
		}
		public ComplexColTypeContext complexColType(int i) {
			return getRuleContext(ComplexColTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public ComplexColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterComplexColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitComplexColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitComplexColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeListContext complexColTypeList() throws RecognitionException {
		ComplexColTypeListContext _localctx = new ComplexColTypeListContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_complexColTypeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3105);
			complexColType();
			setState(3110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3106);
				match(COMMA);
				setState(3107);
				complexColType();
				}
				}
				setState(3112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComplexColTypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ArcticExtendSparkSqlParser.COLON, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ComplexColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterComplexColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitComplexColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitComplexColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeContext complexColType() throws RecognitionException {
		ComplexColTypeContext _localctx = new ComplexColTypeContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_complexColType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3113);
			identifier();
			setState(3115);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,405,_ctx) ) {
			case 1:
				{
				setState(3114);
				match(COLON);
				}
				break;
			}
			setState(3117);
			dataType();
			setState(3120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(3118);
				match(NOT);
				setState(3119);
				match(NULL);
				}
			}

			setState(3123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(3122);
				commentSpec();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhenClauseContext extends ParserRuleContext {
		public ExpressionContext condition;
		public ExpressionContext result;
		public TerminalNode WHEN() { return getToken(ArcticExtendSparkSqlParser.WHEN, 0); }
		public TerminalNode THEN() { return getToken(ArcticExtendSparkSqlParser.THEN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public WhenClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterWhenClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitWhenClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitWhenClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3125);
			match(WHEN);
			setState(3126);
			((WhenClauseContext)_localctx).condition = expression();
			setState(3127);
			match(THEN);
			setState(3128);
			((WhenClauseContext)_localctx).result = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowClauseContext extends ParserRuleContext {
		public TerminalNode WINDOW() { return getToken(ArcticExtendSparkSqlParser.WINDOW, 0); }
		public List<NamedWindowContext> namedWindow() {
			return getRuleContexts(NamedWindowContext.class);
		}
		public NamedWindowContext namedWindow(int i) {
			return getRuleContext(NamedWindowContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public WindowClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterWindowClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitWindowClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitWindowClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowClauseContext windowClause() throws RecognitionException {
		WindowClauseContext _localctx = new WindowClauseContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_windowClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3130);
			match(WINDOW);
			setState(3131);
			namedWindow();
			setState(3136);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,408,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3132);
					match(COMMA);
					setState(3133);
					namedWindow();
					}
					} 
				}
				setState(3138);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,408,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedWindowContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext name;
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public WindowSpecContext windowSpec() {
			return getRuleContext(WindowSpecContext.class,0);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public NamedWindowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedWindow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNamedWindow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNamedWindow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNamedWindow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedWindowContext namedWindow() throws RecognitionException {
		NamedWindowContext _localctx = new NamedWindowContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_namedWindow);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3139);
			((NamedWindowContext)_localctx).name = errorCapturingIdentifier();
			setState(3140);
			match(AS);
			setState(3141);
			windowSpec();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowSpecContext extends ParserRuleContext {
		public WindowSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowSpec; }
	 
		public WindowSpecContext() { }
		public void copyFrom(WindowSpecContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WindowRefContext extends WindowSpecContext {
		public ErrorCapturingIdentifierContext name;
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public WindowRefContext(WindowSpecContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterWindowRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitWindowRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitWindowRef(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WindowDefContext extends WindowSpecContext {
		public ExpressionContext expression;
		public List<ExpressionContext> partition = new ArrayList<ExpressionContext>();
		public TerminalNode LEFT_PAREN() { return getToken(ArcticExtendSparkSqlParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticExtendSparkSqlParser.RIGHT_PAREN, 0); }
		public TerminalNode CLUSTER() { return getToken(ArcticExtendSparkSqlParser.CLUSTER, 0); }
		public List<TerminalNode> BY() { return getTokens(ArcticExtendSparkSqlParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticExtendSparkSqlParser.BY, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public WindowFrameContext windowFrame() {
			return getRuleContext(WindowFrameContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public List<SortItemContext> sortItem() {
			return getRuleContexts(SortItemContext.class);
		}
		public SortItemContext sortItem(int i) {
			return getRuleContext(SortItemContext.class,i);
		}
		public TerminalNode PARTITION() { return getToken(ArcticExtendSparkSqlParser.PARTITION, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticExtendSparkSqlParser.DISTRIBUTE, 0); }
		public TerminalNode ORDER() { return getToken(ArcticExtendSparkSqlParser.ORDER, 0); }
		public TerminalNode SORT() { return getToken(ArcticExtendSparkSqlParser.SORT, 0); }
		public WindowDefContext(WindowSpecContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterWindowDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitWindowDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitWindowDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowSpecContext windowSpec() throws RecognitionException {
		WindowSpecContext _localctx = new WindowSpecContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_windowSpec);
		int _la;
		try {
			setState(3189);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,416,_ctx) ) {
			case 1:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3143);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				}
				break;
			case 2:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3144);
				match(LEFT_PAREN);
				setState(3145);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				setState(3146);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				_localctx = new WindowDefContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3148);
				match(LEFT_PAREN);
				setState(3183);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLUSTER:
					{
					setState(3149);
					match(CLUSTER);
					setState(3150);
					match(BY);
					setState(3151);
					((WindowDefContext)_localctx).expression = expression();
					((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
					setState(3156);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(3152);
						match(COMMA);
						setState(3153);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						}
						}
						setState(3158);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case RIGHT_PAREN:
				case DISTRIBUTE:
				case ORDER:
				case PARTITION:
				case RANGE:
				case ROWS:
				case SORT:
					{
					setState(3169);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DISTRIBUTE || _la==PARTITION) {
						{
						setState(3159);
						_la = _input.LA(1);
						if ( !(_la==DISTRIBUTE || _la==PARTITION) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3160);
						match(BY);
						setState(3161);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						setState(3166);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==COMMA) {
							{
							{
							setState(3162);
							match(COMMA);
							setState(3163);
							((WindowDefContext)_localctx).expression = expression();
							((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
							}
							}
							setState(3168);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
					}

					setState(3181);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ORDER || _la==SORT) {
						{
						setState(3171);
						_la = _input.LA(1);
						if ( !(_la==ORDER || _la==SORT) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3172);
						match(BY);
						setState(3173);
						sortItem();
						setState(3178);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==COMMA) {
							{
							{
							setState(3174);
							match(COMMA);
							setState(3175);
							sortItem();
							}
							}
							setState(3180);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
					}

					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(3186);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==RANGE || _la==ROWS) {
					{
					setState(3185);
					windowFrame();
					}
				}

				setState(3188);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowFrameContext extends ParserRuleContext {
		public Token frameType;
		public FrameBoundContext start;
		public FrameBoundContext end;
		public TerminalNode RANGE() { return getToken(ArcticExtendSparkSqlParser.RANGE, 0); }
		public List<FrameBoundContext> frameBound() {
			return getRuleContexts(FrameBoundContext.class);
		}
		public FrameBoundContext frameBound(int i) {
			return getRuleContext(FrameBoundContext.class,i);
		}
		public TerminalNode ROWS() { return getToken(ArcticExtendSparkSqlParser.ROWS, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticExtendSparkSqlParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public WindowFrameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrame; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterWindowFrame(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitWindowFrame(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitWindowFrame(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameContext windowFrame() throws RecognitionException {
		WindowFrameContext _localctx = new WindowFrameContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_windowFrame);
		try {
			setState(3207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,417,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3191);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3192);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3193);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3194);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3195);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3196);
				match(BETWEEN);
				setState(3197);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3198);
				match(AND);
				setState(3199);
				((WindowFrameContext)_localctx).end = frameBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3201);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3202);
				match(BETWEEN);
				setState(3203);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3204);
				match(AND);
				setState(3205);
				((WindowFrameContext)_localctx).end = frameBound();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FrameBoundContext extends ParserRuleContext {
		public Token boundType;
		public TerminalNode UNBOUNDED() { return getToken(ArcticExtendSparkSqlParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticExtendSparkSqlParser.PRECEDING, 0); }
		public TerminalNode FOLLOWING() { return getToken(ArcticExtendSparkSqlParser.FOLLOWING, 0); }
		public TerminalNode ROW() { return getToken(ArcticExtendSparkSqlParser.ROW, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticExtendSparkSqlParser.CURRENT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FrameBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFrameBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFrameBound(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFrameBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameBoundContext frameBound() throws RecognitionException {
		FrameBoundContext _localctx = new FrameBoundContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_frameBound);
		int _la;
		try {
			setState(3216);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,418,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3209);
				match(UNBOUNDED);
				setState(3210);
				((FrameBoundContext)_localctx).boundType = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==FOLLOWING || _la==PRECEDING) ) {
					((FrameBoundContext)_localctx).boundType = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3211);
				((FrameBoundContext)_localctx).boundType = match(CURRENT);
				setState(3212);
				match(ROW);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3213);
				expression();
				setState(3214);
				((FrameBoundContext)_localctx).boundType = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==FOLLOWING || _la==PRECEDING) ) {
					((FrameBoundContext)_localctx).boundType = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedNameListContext extends ParserRuleContext {
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticExtendSparkSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticExtendSparkSqlParser.COMMA, i);
		}
		public QualifiedNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQualifiedNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQualifiedNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQualifiedNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameListContext qualifiedNameList() throws RecognitionException {
		QualifiedNameListContext _localctx = new QualifiedNameListContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3218);
			qualifiedName();
			setState(3223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3219);
				match(COMMA);
				setState(3220);
				qualifiedName();
				}
				}
				setState(3225);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionNameContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode FILTER() { return getToken(ArcticExtendSparkSqlParser.FILTER, 0); }
		public TerminalNode LEFT() { return getToken(ArcticExtendSparkSqlParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(ArcticExtendSparkSqlParser.RIGHT, 0); }
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_functionName);
		try {
			setState(3230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,420,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3226);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3227);
				match(FILTER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3228);
				match(LEFT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3229);
				match(RIGHT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(ArcticExtendSparkSqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticExtendSparkSqlParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3232);
			identifier();
			setState(3237);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,421,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3233);
					match(DOT);
					setState(3234);
					identifier();
					}
					} 
				}
				setState(3239);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,421,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorCapturingIdentifierContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ErrorCapturingIdentifierExtraContext errorCapturingIdentifierExtra() {
			return getRuleContext(ErrorCapturingIdentifierExtraContext.class,0);
		}
		public ErrorCapturingIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCapturingIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterErrorCapturingIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitErrorCapturingIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitErrorCapturingIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierContext errorCapturingIdentifier() throws RecognitionException {
		ErrorCapturingIdentifierContext _localctx = new ErrorCapturingIdentifierContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_errorCapturingIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3240);
			identifier();
			setState(3241);
			errorCapturingIdentifierExtra();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorCapturingIdentifierExtraContext extends ParserRuleContext {
		public ErrorCapturingIdentifierExtraContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCapturingIdentifierExtra; }
	 
		public ErrorCapturingIdentifierExtraContext() { }
		public void copyFrom(ErrorCapturingIdentifierExtraContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ErrorIdentContext extends ErrorCapturingIdentifierExtraContext {
		public List<TerminalNode> MINUS() { return getTokens(ArcticExtendSparkSqlParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(ArcticExtendSparkSqlParser.MINUS, i);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public ErrorIdentContext(ErrorCapturingIdentifierExtraContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterErrorIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitErrorIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitErrorIdent(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RealIdentContext extends ErrorCapturingIdentifierExtraContext {
		public RealIdentContext(ErrorCapturingIdentifierExtraContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterRealIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitRealIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitRealIdent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierExtraContext errorCapturingIdentifierExtra() throws RecognitionException {
		ErrorCapturingIdentifierExtraContext _localctx = new ErrorCapturingIdentifierExtraContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_errorCapturingIdentifierExtra);
		try {
			int _alt;
			setState(3250);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,423,_ctx) ) {
			case 1:
				_localctx = new ErrorIdentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3245); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(3243);
						match(MINUS);
						setState(3244);
						identifier();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(3247); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,422,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				_localctx = new RealIdentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public StrictIdentifierContext strictIdentifier() {
			return getRuleContext(StrictIdentifierContext.class,0);
		}
		public StrictNonReservedContext strictNonReserved() {
			return getRuleContext(StrictNonReservedContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_identifier);
		try {
			setState(3255);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,424,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3252);
				strictIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3253);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3254);
				strictNonReserved();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StrictIdentifierContext extends ParserRuleContext {
		public StrictIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strictIdentifier; }
	 
		public StrictIdentifierContext() { }
		public void copyFrom(StrictIdentifierContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class QuotedIdentifierAlternativeContext extends StrictIdentifierContext {
		public QuotedIdentifierContext quotedIdentifier() {
			return getRuleContext(QuotedIdentifierContext.class,0);
		}
		public QuotedIdentifierAlternativeContext(StrictIdentifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQuotedIdentifierAlternative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQuotedIdentifierAlternative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQuotedIdentifierAlternative(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnquotedIdentifierContext extends StrictIdentifierContext {
		public TerminalNode IDENTIFIER() { return getToken(ArcticExtendSparkSqlParser.IDENTIFIER, 0); }
		public AnsiNonReservedContext ansiNonReserved() {
			return getRuleContext(AnsiNonReservedContext.class,0);
		}
		public NonReservedContext nonReserved() {
			return getRuleContext(NonReservedContext.class,0);
		}
		public UnquotedIdentifierContext(StrictIdentifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterUnquotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitUnquotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitUnquotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictIdentifierContext strictIdentifier() throws RecognitionException {
		StrictIdentifierContext _localctx = new StrictIdentifierContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_strictIdentifier);
		try {
			setState(3263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,425,_ctx) ) {
			case 1:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3257);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3258);
				quotedIdentifier();
				}
				break;
			case 3:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3259);
				if (!(SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "SQL_standard_keyword_behavior");
				setState(3260);
				ansiNonReserved();
				}
				break;
			case 4:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3261);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3262);
				nonReserved();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuotedIdentifierContext extends ParserRuleContext {
		public TerminalNode BACKQUOTED_IDENTIFIER() { return getToken(ArcticExtendSparkSqlParser.BACKQUOTED_IDENTIFIER, 0); }
		public QuotedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterQuotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitQuotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitQuotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuotedIdentifierContext quotedIdentifier() throws RecognitionException {
		QuotedIdentifierContext _localctx = new QuotedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_quotedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3265);
			match(BACKQUOTED_IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
	 
		public NumberContext() { }
		public void copyFrom(NumberContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DecimalLiteralContext extends NumberContext {
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticExtendSparkSqlParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public DecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BigIntLiteralContext extends NumberContext {
		public TerminalNode BIGINT_LITERAL() { return getToken(ArcticExtendSparkSqlParser.BIGINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public BigIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterBigIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitBigIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitBigIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TinyIntLiteralContext extends NumberContext {
		public TerminalNode TINYINT_LITERAL() { return getToken(ArcticExtendSparkSqlParser.TINYINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public TinyIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTinyIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTinyIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTinyIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LegacyDecimalLiteralContext extends NumberContext {
		public TerminalNode EXPONENT_VALUE() { return getToken(ArcticExtendSparkSqlParser.EXPONENT_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticExtendSparkSqlParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public LegacyDecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterLegacyDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitLegacyDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitLegacyDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BigDecimalLiteralContext extends NumberContext {
		public TerminalNode BIGDECIMAL_LITERAL() { return getToken(ArcticExtendSparkSqlParser.BIGDECIMAL_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public BigDecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterBigDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitBigDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitBigDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExponentLiteralContext extends NumberContext {
		public TerminalNode EXPONENT_VALUE() { return getToken(ArcticExtendSparkSqlParser.EXPONENT_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public ExponentLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterExponentLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitExponentLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitExponentLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DoubleLiteralContext extends NumberContext {
		public TerminalNode DOUBLE_LITERAL() { return getToken(ArcticExtendSparkSqlParser.DOUBLE_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public DoubleLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterDoubleLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitDoubleLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitDoubleLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IntegerLiteralContext extends NumberContext {
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public IntegerLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FloatLiteralContext extends NumberContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(ArcticExtendSparkSqlParser.FLOAT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public FloatLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitFloatLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SmallIntLiteralContext extends NumberContext {
		public TerminalNode SMALLINT_LITERAL() { return getToken(ArcticExtendSparkSqlParser.SMALLINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticExtendSparkSqlParser.MINUS, 0); }
		public SmallIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterSmallIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitSmallIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitSmallIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_number);
		int _la;
		try {
			setState(3310);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,436,_ctx) ) {
			case 1:
				_localctx = new ExponentLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3267);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3269);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3268);
					match(MINUS);
					}
				}

				setState(3271);
				match(EXPONENT_VALUE);
				}
				break;
			case 2:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3272);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3273);
					match(MINUS);
					}
				}

				setState(3276);
				match(DECIMAL_VALUE);
				}
				break;
			case 3:
				_localctx = new LegacyDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3277);
				if (!(legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "legacy_exponent_literal_as_decimal_enabled");
				setState(3279);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3278);
					match(MINUS);
					}
				}

				setState(3281);
				_la = _input.LA(1);
				if ( !(_la==EXPONENT_VALUE || _la==DECIMAL_VALUE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 4:
				_localctx = new IntegerLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3283);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3282);
					match(MINUS);
					}
				}

				setState(3285);
				match(INTEGER_VALUE);
				}
				break;
			case 5:
				_localctx = new BigIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(3287);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3286);
					match(MINUS);
					}
				}

				setState(3289);
				match(BIGINT_LITERAL);
				}
				break;
			case 6:
				_localctx = new SmallIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(3291);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3290);
					match(MINUS);
					}
				}

				setState(3293);
				match(SMALLINT_LITERAL);
				}
				break;
			case 7:
				_localctx = new TinyIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(3295);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3294);
					match(MINUS);
					}
				}

				setState(3297);
				match(TINYINT_LITERAL);
				}
				break;
			case 8:
				_localctx = new DoubleLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(3299);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3298);
					match(MINUS);
					}
				}

				setState(3301);
				match(DOUBLE_LITERAL);
				}
				break;
			case 9:
				_localctx = new FloatLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(3303);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3302);
					match(MINUS);
					}
				}

				setState(3305);
				match(FLOAT_LITERAL);
				}
				break;
			case 10:
				_localctx = new BigDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(3307);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3306);
					match(MINUS);
					}
				}

				setState(3309);
				match(BIGDECIMAL_LITERAL);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlterColumnActionContext extends ParserRuleContext {
		public Token setOrDrop;
		public TerminalNode TYPE() { return getToken(ArcticExtendSparkSqlParser.TYPE, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public AlterColumnActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterColumnAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAlterColumnAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAlterColumnAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAlterColumnAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterColumnActionContext alterColumnAction() throws RecognitionException {
		AlterColumnActionContext _localctx = new AlterColumnActionContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_alterColumnAction);
		int _la;
		try {
			setState(3319);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(3312);
				match(TYPE);
				setState(3313);
				dataType();
				}
				break;
			case COMMENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(3314);
				commentSpec();
				}
				break;
			case AFTER:
			case FIRST:
				enterOuterAlt(_localctx, 3);
				{
				setState(3315);
				colPosition();
				}
				break;
			case DROP:
			case SET:
				enterOuterAlt(_localctx, 4);
				{
				setState(3316);
				((AlterColumnActionContext)_localctx).setOrDrop = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==DROP || _la==SET) ) {
					((AlterColumnActionContext)_localctx).setOrDrop = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(3317);
				match(NOT);
				setState(3318);
				match(NULL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnsiNonReservedContext extends ParserRuleContext {
		public TerminalNode ADD() { return getToken(ArcticExtendSparkSqlParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(ArcticExtendSparkSqlParser.AFTER, 0); }
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(ArcticExtendSparkSqlParser.ANALYZE, 0); }
		public TerminalNode ANTI() { return getToken(ArcticExtendSparkSqlParser.ANTI, 0); }
		public TerminalNode ARCHIVE() { return getToken(ArcticExtendSparkSqlParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(ArcticExtendSparkSqlParser.ARRAY, 0); }
		public TerminalNode ASC() { return getToken(ArcticExtendSparkSqlParser.ASC, 0); }
		public TerminalNode AT() { return getToken(ArcticExtendSparkSqlParser.AT, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticExtendSparkSqlParser.BETWEEN, 0); }
		public TerminalNode BUCKET() { return getToken(ArcticExtendSparkSqlParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(ArcticExtendSparkSqlParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(ArcticExtendSparkSqlParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(ArcticExtendSparkSqlParser.CASCADE, 0); }
		public TerminalNode CATALOG() { return getToken(ArcticExtendSparkSqlParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(ArcticExtendSparkSqlParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(ArcticExtendSparkSqlParser.CHANGE, 0); }
		public TerminalNode CLEAR() { return getToken(ArcticExtendSparkSqlParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(ArcticExtendSparkSqlParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(ArcticExtendSparkSqlParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(ArcticExtendSparkSqlParser.CODEGEN, 0); }
		public TerminalNode COLLECTION() { return getToken(ArcticExtendSparkSqlParser.COLLECTION, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(ArcticExtendSparkSqlParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(ArcticExtendSparkSqlParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(ArcticExtendSparkSqlParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(ArcticExtendSparkSqlParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(ArcticExtendSparkSqlParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(ArcticExtendSparkSqlParser.CONCATENATE, 0); }
		public TerminalNode COST() { return getToken(ArcticExtendSparkSqlParser.COST, 0); }
		public TerminalNode CUBE() { return getToken(ArcticExtendSparkSqlParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticExtendSparkSqlParser.CURRENT, 0); }
		public TerminalNode DATA() { return getToken(ArcticExtendSparkSqlParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticExtendSparkSqlParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticExtendSparkSqlParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(ArcticExtendSparkSqlParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(ArcticExtendSparkSqlParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(ArcticExtendSparkSqlParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(ArcticExtendSparkSqlParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(ArcticExtendSparkSqlParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(ArcticExtendSparkSqlParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(ArcticExtendSparkSqlParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticExtendSparkSqlParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(ArcticExtendSparkSqlParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticExtendSparkSqlParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticExtendSparkSqlParser.DIRECTORY, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticExtendSparkSqlParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(ArcticExtendSparkSqlParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode ESCAPED() { return getToken(ArcticExtendSparkSqlParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(ArcticExtendSparkSqlParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(ArcticExtendSparkSqlParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(ArcticExtendSparkSqlParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(ArcticExtendSparkSqlParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(ArcticExtendSparkSqlParser.EXTRACT, 0); }
		public TerminalNode FIELDS() { return getToken(ArcticExtendSparkSqlParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(ArcticExtendSparkSqlParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(ArcticExtendSparkSqlParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(ArcticExtendSparkSqlParser.FOLLOWING, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticExtendSparkSqlParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticExtendSparkSqlParser.FORMATTED, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticExtendSparkSqlParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticExtendSparkSqlParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(ArcticExtendSparkSqlParser.GLOBAL, 0); }
		public TerminalNode GROUPING() { return getToken(ArcticExtendSparkSqlParser.GROUPING, 0); }
		public TerminalNode HOUR() { return getToken(ArcticExtendSparkSqlParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticExtendSparkSqlParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(ArcticExtendSparkSqlParser.IMPORT, 0); }
		public TerminalNode INDEX() { return getToken(ArcticExtendSparkSqlParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(ArcticExtendSparkSqlParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(ArcticExtendSparkSqlParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(ArcticExtendSparkSqlParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(ArcticExtendSparkSqlParser.INTERVAL, 0); }
		public TerminalNode ITEMS() { return getToken(ArcticExtendSparkSqlParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(ArcticExtendSparkSqlParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(ArcticExtendSparkSqlParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(ArcticExtendSparkSqlParser.LAZY, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(ArcticExtendSparkSqlParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(ArcticExtendSparkSqlParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(ArcticExtendSparkSqlParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(ArcticExtendSparkSqlParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(ArcticExtendSparkSqlParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(ArcticExtendSparkSqlParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(ArcticExtendSparkSqlParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(ArcticExtendSparkSqlParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(ArcticExtendSparkSqlParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(ArcticExtendSparkSqlParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(ArcticExtendSparkSqlParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticExtendSparkSqlParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(ArcticExtendSparkSqlParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(ArcticExtendSparkSqlParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(ArcticExtendSparkSqlParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(ArcticExtendSparkSqlParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(ArcticExtendSparkSqlParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(ArcticExtendSparkSqlParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(ArcticExtendSparkSqlParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(ArcticExtendSparkSqlParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(ArcticExtendSparkSqlParser.NO, 0); }
		public TerminalNode NULLS() { return getToken(ArcticExtendSparkSqlParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(ArcticExtendSparkSqlParser.OF, 0); }
		public TerminalNode OPTION() { return getToken(ArcticExtendSparkSqlParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public TerminalNode OUT() { return getToken(ArcticExtendSparkSqlParser.OUT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(ArcticExtendSparkSqlParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(ArcticExtendSparkSqlParser.OVER, 0); }
		public TerminalNode OVERLAY() { return getToken(ArcticExtendSparkSqlParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(ArcticExtendSparkSqlParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(ArcticExtendSparkSqlParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticExtendSparkSqlParser.PARTITIONS, 0); }
		public TerminalNode PERCENTLIT() { return getToken(ArcticExtendSparkSqlParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(ArcticExtendSparkSqlParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(ArcticExtendSparkSqlParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(ArcticExtendSparkSqlParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticExtendSparkSqlParser.PRECEDING, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticExtendSparkSqlParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticExtendSparkSqlParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(ArcticExtendSparkSqlParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(ArcticExtendSparkSqlParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(ArcticExtendSparkSqlParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(ArcticExtendSparkSqlParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticExtendSparkSqlParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticExtendSparkSqlParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(ArcticExtendSparkSqlParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticExtendSparkSqlParser.REDUCE, 0); }
		public TerminalNode REFRESH() { return getToken(ArcticExtendSparkSqlParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(ArcticExtendSparkSqlParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticExtendSparkSqlParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(ArcticExtendSparkSqlParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(ArcticExtendSparkSqlParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(ArcticExtendSparkSqlParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(ArcticExtendSparkSqlParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(ArcticExtendSparkSqlParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(ArcticExtendSparkSqlParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(ArcticExtendSparkSqlParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(ArcticExtendSparkSqlParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(ArcticExtendSparkSqlParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticExtendSparkSqlParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(ArcticExtendSparkSqlParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(ArcticExtendSparkSqlParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(ArcticExtendSparkSqlParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(ArcticExtendSparkSqlParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(ArcticExtendSparkSqlParser.SECOND, 0); }
		public TerminalNode SEMI() { return getToken(ArcticExtendSparkSqlParser.SEMI, 0); }
		public TerminalNode SEPARATED() { return getToken(ArcticExtendSparkSqlParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(ArcticExtendSparkSqlParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.SERDEPROPERTIES, 0); }
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode SETMINUS() { return getToken(ArcticExtendSparkSqlParser.SETMINUS, 0); }
		public TerminalNode SETS() { return getToken(ArcticExtendSparkSqlParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(ArcticExtendSparkSqlParser.SKEWED, 0); }
		public TerminalNode SORT() { return getToken(ArcticExtendSparkSqlParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(ArcticExtendSparkSqlParser.SORTED, 0); }
		public TerminalNode START() { return getToken(ArcticExtendSparkSqlParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticExtendSparkSqlParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(ArcticExtendSparkSqlParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(ArcticExtendSparkSqlParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(ArcticExtendSparkSqlParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(ArcticExtendSparkSqlParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticExtendSparkSqlParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(ArcticExtendSparkSqlParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(ArcticExtendSparkSqlParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(ArcticExtendSparkSqlParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLES() { return getToken(ArcticExtendSparkSqlParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticExtendSparkSqlParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticExtendSparkSqlParser.TERMINATED, 0); }
		public TerminalNode TIMESTAMP() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TOUCH() { return getToken(ArcticExtendSparkSqlParser.TOUCH, 0); }
		public TerminalNode TRANSACTION() { return getToken(ArcticExtendSparkSqlParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(ArcticExtendSparkSqlParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(ArcticExtendSparkSqlParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(ArcticExtendSparkSqlParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(ArcticExtendSparkSqlParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(ArcticExtendSparkSqlParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(ArcticExtendSparkSqlParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(ArcticExtendSparkSqlParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(ArcticExtendSparkSqlParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(ArcticExtendSparkSqlParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(ArcticExtendSparkSqlParser.UNCACHE, 0); }
		public TerminalNode UNLOCK() { return getToken(ArcticExtendSparkSqlParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(ArcticExtendSparkSqlParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(ArcticExtendSparkSqlParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(ArcticExtendSparkSqlParser.USE, 0); }
		public TerminalNode VALUES() { return getToken(ArcticExtendSparkSqlParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(ArcticExtendSparkSqlParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticExtendSparkSqlParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(ArcticExtendSparkSqlParser.WEEK, 0); }
		public TerminalNode WINDOW() { return getToken(ArcticExtendSparkSqlParser.WINDOW, 0); }
		public TerminalNode YEAR() { return getToken(ArcticExtendSparkSqlParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(ArcticExtendSparkSqlParser.ZONE, 0); }
		public AnsiNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ansiNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterAnsiNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitAnsiNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitAnsiNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnsiNonReservedContext ansiNonReserved() throws RecognitionException {
		AnsiNonReservedContext _localctx = new AnsiNonReservedContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_ansiNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3321);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALTER) | (1L << ANALYZE) | (1L << ANTI) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << ASC) | (1L << AT) | (1L << BETWEEN) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLECTION) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << COST) | (1L << CUBE) | (1L << CURRENT) | (1L << DAY) | (1L << DAYOFYEAR) | (1L << DATA))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATABASE - 64)) | (1L << (DATABASES - 64)) | (1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FIELDS - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GROUPING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (ITEMS - 128)) | (1L << (KEYS - 128)) | (1L << (LAST - 128)) | (1L << (LAZY - 128)) | (1L << (LIKE - 128)) | (1L << (ILIKE - 128)) | (1L << (LIMIT - 128)) | (1L << (LINES - 128)) | (1L << (LIST - 128)) | (1L << (LOAD - 128)) | (1L << (LOCAL - 128)) | (1L << (LOCATION - 128)) | (1L << (LOCK - 128)) | (1L << (LOCKS - 128)) | (1L << (LOGICAL - 128)) | (1L << (MACRO - 128)) | (1L << (MAP - 128)) | (1L << (MATCHED - 128)) | (1L << (MERGE - 128)) | (1L << (MICROSECOND - 128)) | (1L << (MILLISECOND - 128)) | (1L << (MINUTE - 128)) | (1L << (MONTH - 128)) | (1L << (MSCK - 128)) | (1L << (NAMESPACE - 128)) | (1L << (NAMESPACES - 128)) | (1L << (NO - 128)) | (1L << (NULLS - 128)) | (1L << (OF - 128)) | (1L << (OPTION - 128)) | (1L << (OPTIONS - 128)) | (1L << (OUT - 128)) | (1L << (OUTPUTFORMAT - 128)) | (1L << (OVER - 128)) | (1L << (OVERLAY - 128)) | (1L << (OVERWRITE - 128)) | (1L << (PARTITION - 128)) | (1L << (PARTITIONED - 128)) | (1L << (PARTITIONS - 128)) | (1L << (PERCENTLIT - 128)) | (1L << (PIVOT - 128)) | (1L << (PLACING - 128)) | (1L << (POSITION - 128)) | (1L << (PRECEDING - 128)) | (1L << (PRINCIPALS - 128)) | (1L << (PROPERTIES - 128)) | (1L << (PURGE - 128)) | (1L << (QUARTER - 128)))) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & ((1L << (QUERY - 192)) | (1L << (RANGE - 192)) | (1L << (RECORDREADER - 192)) | (1L << (RECORDWRITER - 192)) | (1L << (RECOVER - 192)) | (1L << (REDUCE - 192)) | (1L << (REFRESH - 192)) | (1L << (RENAME - 192)) | (1L << (REPAIR - 192)) | (1L << (REPEATABLE - 192)) | (1L << (REPLACE - 192)) | (1L << (RESET - 192)) | (1L << (RESPECT - 192)) | (1L << (RESTRICT - 192)) | (1L << (REVOKE - 192)) | (1L << (RLIKE - 192)) | (1L << (ROLE - 192)) | (1L << (ROLES - 192)) | (1L << (ROLLBACK - 192)) | (1L << (ROLLUP - 192)) | (1L << (ROW - 192)) | (1L << (ROWS - 192)) | (1L << (SECOND - 192)) | (1L << (SCHEMA - 192)) | (1L << (SCHEMAS - 192)) | (1L << (SEMI - 192)) | (1L << (SEPARATED - 192)) | (1L << (SERDE - 192)) | (1L << (SERDEPROPERTIES - 192)) | (1L << (SET - 192)) | (1L << (SETMINUS - 192)) | (1L << (SETS - 192)) | (1L << (SHOW - 192)) | (1L << (SKEWED - 192)) | (1L << (SORT - 192)) | (1L << (SORTED - 192)) | (1L << (START - 192)) | (1L << (STATISTICS - 192)) | (1L << (STORED - 192)) | (1L << (STRATIFY - 192)) | (1L << (STRUCT - 192)) | (1L << (SUBSTR - 192)) | (1L << (SUBSTRING - 192)) | (1L << (SYNC - 192)) | (1L << (SYSTEM_TIME - 192)) | (1L << (SYSTEM_VERSION - 192)) | (1L << (TABLES - 192)) | (1L << (TABLESAMPLE - 192)) | (1L << (TBLPROPERTIES - 192)) | (1L << (TEMPORARY - 192)) | (1L << (TERMINATED - 192)) | (1L << (TIMESTAMP - 192)) | (1L << (TIMESTAMPADD - 192)) | (1L << (TIMESTAMPDIFF - 192)) | (1L << (TOUCH - 192)))) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (TRANSACTION - 257)) | (1L << (TRANSACTIONS - 257)) | (1L << (TRANSFORM - 257)) | (1L << (TRIM - 257)) | (1L << (TRUE - 257)) | (1L << (TRUNCATE - 257)) | (1L << (TRY_CAST - 257)) | (1L << (TYPE - 257)) | (1L << (UNARCHIVE - 257)) | (1L << (UNBOUNDED - 257)) | (1L << (UNCACHE - 257)) | (1L << (UNLOCK - 257)) | (1L << (UNSET - 257)) | (1L << (UPDATE - 257)) | (1L << (USE - 257)) | (1L << (VALUES - 257)) | (1L << (VERSION - 257)) | (1L << (VIEW - 257)) | (1L << (VIEWS - 257)) | (1L << (WEEK - 257)) | (1L << (WINDOW - 257)) | (1L << (YEAR - 257)) | (1L << (ZONE - 257)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StrictNonReservedContext extends ParserRuleContext {
		public TerminalNode ANTI() { return getToken(ArcticExtendSparkSqlParser.ANTI, 0); }
		public TerminalNode CROSS() { return getToken(ArcticExtendSparkSqlParser.CROSS, 0); }
		public TerminalNode EXCEPT() { return getToken(ArcticExtendSparkSqlParser.EXCEPT, 0); }
		public TerminalNode FULL() { return getToken(ArcticExtendSparkSqlParser.FULL, 0); }
		public TerminalNode INNER() { return getToken(ArcticExtendSparkSqlParser.INNER, 0); }
		public TerminalNode INTERSECT() { return getToken(ArcticExtendSparkSqlParser.INTERSECT, 0); }
		public TerminalNode JOIN() { return getToken(ArcticExtendSparkSqlParser.JOIN, 0); }
		public TerminalNode LATERAL() { return getToken(ArcticExtendSparkSqlParser.LATERAL, 0); }
		public TerminalNode LEFT() { return getToken(ArcticExtendSparkSqlParser.LEFT, 0); }
		public TerminalNode NATURAL() { return getToken(ArcticExtendSparkSqlParser.NATURAL, 0); }
		public TerminalNode ON() { return getToken(ArcticExtendSparkSqlParser.ON, 0); }
		public TerminalNode RIGHT() { return getToken(ArcticExtendSparkSqlParser.RIGHT, 0); }
		public TerminalNode SEMI() { return getToken(ArcticExtendSparkSqlParser.SEMI, 0); }
		public TerminalNode SETMINUS() { return getToken(ArcticExtendSparkSqlParser.SETMINUS, 0); }
		public TerminalNode UNION() { return getToken(ArcticExtendSparkSqlParser.UNION, 0); }
		public TerminalNode USING() { return getToken(ArcticExtendSparkSqlParser.USING, 0); }
		public StrictNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strictNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterStrictNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitStrictNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitStrictNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictNonReservedContext strictNonReserved() throws RecognitionException {
		StrictNonReservedContext _localctx = new StrictNonReservedContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_strictNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3323);
			_la = _input.LA(1);
			if ( !(_la==ANTI || _la==CROSS || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & ((1L << (EXCEPT - 85)) | (1L << (FULL - 85)) | (1L << (INNER - 85)) | (1L << (INTERSECT - 85)) | (1L << (JOIN - 85)) | (1L << (LATERAL - 85)) | (1L << (LEFT - 85)))) != 0) || ((((_la - 158)) & ~0x3f) == 0 && ((1L << (_la - 158)) & ((1L << (NATURAL - 158)) | (1L << (ON - 158)) | (1L << (RIGHT - 158)) | (1L << (SEMI - 158)))) != 0) || ((((_la - 226)) & ~0x3f) == 0 && ((1L << (_la - 226)) & ((1L << (SETMINUS - 226)) | (1L << (UNION - 226)) | (1L << (USING - 226)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonReservedContext extends ParserRuleContext {
		public TerminalNode ADD() { return getToken(ArcticExtendSparkSqlParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(ArcticExtendSparkSqlParser.AFTER, 0); }
		public TerminalNode ALL() { return getToken(ArcticExtendSparkSqlParser.ALL, 0); }
		public TerminalNode ALTER() { return getToken(ArcticExtendSparkSqlParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(ArcticExtendSparkSqlParser.ANALYZE, 0); }
		public TerminalNode AND() { return getToken(ArcticExtendSparkSqlParser.AND, 0); }
		public TerminalNode ANY() { return getToken(ArcticExtendSparkSqlParser.ANY, 0); }
		public TerminalNode ARCHIVE() { return getToken(ArcticExtendSparkSqlParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(ArcticExtendSparkSqlParser.ARRAY, 0); }
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public TerminalNode ASC() { return getToken(ArcticExtendSparkSqlParser.ASC, 0); }
		public TerminalNode AT() { return getToken(ArcticExtendSparkSqlParser.AT, 0); }
		public TerminalNode AUTHORIZATION() { return getToken(ArcticExtendSparkSqlParser.AUTHORIZATION, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticExtendSparkSqlParser.BETWEEN, 0); }
		public TerminalNode BOTH() { return getToken(ArcticExtendSparkSqlParser.BOTH, 0); }
		public TerminalNode BUCKET() { return getToken(ArcticExtendSparkSqlParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(ArcticExtendSparkSqlParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(ArcticExtendSparkSqlParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(ArcticExtendSparkSqlParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(ArcticExtendSparkSqlParser.CASCADE, 0); }
		public TerminalNode CASE() { return getToken(ArcticExtendSparkSqlParser.CASE, 0); }
		public TerminalNode CAST() { return getToken(ArcticExtendSparkSqlParser.CAST, 0); }
		public TerminalNode CATALOG() { return getToken(ArcticExtendSparkSqlParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(ArcticExtendSparkSqlParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(ArcticExtendSparkSqlParser.CHANGE, 0); }
		public TerminalNode CHECK() { return getToken(ArcticExtendSparkSqlParser.CHECK, 0); }
		public TerminalNode CLEAR() { return getToken(ArcticExtendSparkSqlParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(ArcticExtendSparkSqlParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(ArcticExtendSparkSqlParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(ArcticExtendSparkSqlParser.CODEGEN, 0); }
		public TerminalNode COLLATE() { return getToken(ArcticExtendSparkSqlParser.COLLATE, 0); }
		public TerminalNode COLLECTION() { return getToken(ArcticExtendSparkSqlParser.COLLECTION, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(ArcticExtendSparkSqlParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(ArcticExtendSparkSqlParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(ArcticExtendSparkSqlParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(ArcticExtendSparkSqlParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(ArcticExtendSparkSqlParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(ArcticExtendSparkSqlParser.CONCATENATE, 0); }
		public TerminalNode CONSTRAINT() { return getToken(ArcticExtendSparkSqlParser.CONSTRAINT, 0); }
		public TerminalNode COST() { return getToken(ArcticExtendSparkSqlParser.COST, 0); }
		public TerminalNode CREATE() { return getToken(ArcticExtendSparkSqlParser.CREATE, 0); }
		public TerminalNode CUBE() { return getToken(ArcticExtendSparkSqlParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticExtendSparkSqlParser.CURRENT, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(ArcticExtendSparkSqlParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(ArcticExtendSparkSqlParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(ArcticExtendSparkSqlParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(ArcticExtendSparkSqlParser.CURRENT_USER, 0); }
		public TerminalNode DATA() { return getToken(ArcticExtendSparkSqlParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticExtendSparkSqlParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticExtendSparkSqlParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(ArcticExtendSparkSqlParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(ArcticExtendSparkSqlParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(ArcticExtendSparkSqlParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(ArcticExtendSparkSqlParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(ArcticExtendSparkSqlParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(ArcticExtendSparkSqlParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(ArcticExtendSparkSqlParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(ArcticExtendSparkSqlParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticExtendSparkSqlParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(ArcticExtendSparkSqlParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticExtendSparkSqlParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticExtendSparkSqlParser.DIRECTORY, 0); }
		public TerminalNode DISTINCT() { return getToken(ArcticExtendSparkSqlParser.DISTINCT, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticExtendSparkSqlParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(ArcticExtendSparkSqlParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(ArcticExtendSparkSqlParser.DROP, 0); }
		public TerminalNode ELSE() { return getToken(ArcticExtendSparkSqlParser.ELSE, 0); }
		public TerminalNode END() { return getToken(ArcticExtendSparkSqlParser.END, 0); }
		public TerminalNode ESCAPE() { return getToken(ArcticExtendSparkSqlParser.ESCAPE, 0); }
		public TerminalNode ESCAPED() { return getToken(ArcticExtendSparkSqlParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(ArcticExtendSparkSqlParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticExtendSparkSqlParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(ArcticExtendSparkSqlParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(ArcticExtendSparkSqlParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticExtendSparkSqlParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(ArcticExtendSparkSqlParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(ArcticExtendSparkSqlParser.EXTRACT, 0); }
		public TerminalNode FALSE() { return getToken(ArcticExtendSparkSqlParser.FALSE, 0); }
		public TerminalNode FETCH() { return getToken(ArcticExtendSparkSqlParser.FETCH, 0); }
		public TerminalNode FILTER() { return getToken(ArcticExtendSparkSqlParser.FILTER, 0); }
		public TerminalNode FIELDS() { return getToken(ArcticExtendSparkSqlParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(ArcticExtendSparkSqlParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(ArcticExtendSparkSqlParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(ArcticExtendSparkSqlParser.FOLLOWING, 0); }
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public TerminalNode FOREIGN() { return getToken(ArcticExtendSparkSqlParser.FOREIGN, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticExtendSparkSqlParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticExtendSparkSqlParser.FORMATTED, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticExtendSparkSqlParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticExtendSparkSqlParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(ArcticExtendSparkSqlParser.GLOBAL, 0); }
		public TerminalNode GRANT() { return getToken(ArcticExtendSparkSqlParser.GRANT, 0); }
		public TerminalNode GROUP() { return getToken(ArcticExtendSparkSqlParser.GROUP, 0); }
		public TerminalNode GROUPING() { return getToken(ArcticExtendSparkSqlParser.GROUPING, 0); }
		public TerminalNode HAVING() { return getToken(ArcticExtendSparkSqlParser.HAVING, 0); }
		public TerminalNode HOUR() { return getToken(ArcticExtendSparkSqlParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(ArcticExtendSparkSqlParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticExtendSparkSqlParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(ArcticExtendSparkSqlParser.IMPORT, 0); }
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public TerminalNode INDEX() { return getToken(ArcticExtendSparkSqlParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(ArcticExtendSparkSqlParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(ArcticExtendSparkSqlParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(ArcticExtendSparkSqlParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(ArcticExtendSparkSqlParser.INTERVAL, 0); }
		public TerminalNode INTO() { return getToken(ArcticExtendSparkSqlParser.INTO, 0); }
		public TerminalNode IS() { return getToken(ArcticExtendSparkSqlParser.IS, 0); }
		public TerminalNode ITEMS() { return getToken(ArcticExtendSparkSqlParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(ArcticExtendSparkSqlParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(ArcticExtendSparkSqlParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(ArcticExtendSparkSqlParser.LAZY, 0); }
		public TerminalNode LEADING() { return getToken(ArcticExtendSparkSqlParser.LEADING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(ArcticExtendSparkSqlParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(ArcticExtendSparkSqlParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(ArcticExtendSparkSqlParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(ArcticExtendSparkSqlParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(ArcticExtendSparkSqlParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(ArcticExtendSparkSqlParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(ArcticExtendSparkSqlParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(ArcticExtendSparkSqlParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(ArcticExtendSparkSqlParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(ArcticExtendSparkSqlParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(ArcticExtendSparkSqlParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticExtendSparkSqlParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(ArcticExtendSparkSqlParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(ArcticExtendSparkSqlParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(ArcticExtendSparkSqlParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(ArcticExtendSparkSqlParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(ArcticExtendSparkSqlParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(ArcticExtendSparkSqlParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(ArcticExtendSparkSqlParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(ArcticExtendSparkSqlParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(ArcticExtendSparkSqlParser.NO, 0); }
		public TerminalNode NOT() { return getToken(ArcticExtendSparkSqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticExtendSparkSqlParser.NULL, 0); }
		public TerminalNode NULLS() { return getToken(ArcticExtendSparkSqlParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(ArcticExtendSparkSqlParser.OF, 0); }
		public TerminalNode ONLY() { return getToken(ArcticExtendSparkSqlParser.ONLY, 0); }
		public TerminalNode OPTION() { return getToken(ArcticExtendSparkSqlParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public TerminalNode OR() { return getToken(ArcticExtendSparkSqlParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(ArcticExtendSparkSqlParser.ORDER, 0); }
		public TerminalNode OUT() { return getToken(ArcticExtendSparkSqlParser.OUT, 0); }
		public TerminalNode OUTER() { return getToken(ArcticExtendSparkSqlParser.OUTER, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(ArcticExtendSparkSqlParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(ArcticExtendSparkSqlParser.OVER, 0); }
		public TerminalNode OVERLAPS() { return getToken(ArcticExtendSparkSqlParser.OVERLAPS, 0); }
		public TerminalNode OVERLAY() { return getToken(ArcticExtendSparkSqlParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(ArcticExtendSparkSqlParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(ArcticExtendSparkSqlParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticExtendSparkSqlParser.PARTITIONS, 0); }
		public TerminalNode PERCENTILE_CONT() { return getToken(ArcticExtendSparkSqlParser.PERCENTILE_CONT, 0); }
		public TerminalNode PERCENTILE_DISC() { return getToken(ArcticExtendSparkSqlParser.PERCENTILE_DISC, 0); }
		public TerminalNode PERCENTLIT() { return getToken(ArcticExtendSparkSqlParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(ArcticExtendSparkSqlParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(ArcticExtendSparkSqlParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(ArcticExtendSparkSqlParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticExtendSparkSqlParser.PRECEDING, 0); }
		public TerminalNode PRIMARY() { return getToken(ArcticExtendSparkSqlParser.PRIMARY, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticExtendSparkSqlParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticExtendSparkSqlParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(ArcticExtendSparkSqlParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(ArcticExtendSparkSqlParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(ArcticExtendSparkSqlParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(ArcticExtendSparkSqlParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticExtendSparkSqlParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticExtendSparkSqlParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(ArcticExtendSparkSqlParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticExtendSparkSqlParser.REDUCE, 0); }
		public TerminalNode REFERENCES() { return getToken(ArcticExtendSparkSqlParser.REFERENCES, 0); }
		public TerminalNode REFRESH() { return getToken(ArcticExtendSparkSqlParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(ArcticExtendSparkSqlParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticExtendSparkSqlParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(ArcticExtendSparkSqlParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticExtendSparkSqlParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(ArcticExtendSparkSqlParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(ArcticExtendSparkSqlParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(ArcticExtendSparkSqlParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(ArcticExtendSparkSqlParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(ArcticExtendSparkSqlParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(ArcticExtendSparkSqlParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(ArcticExtendSparkSqlParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(ArcticExtendSparkSqlParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticExtendSparkSqlParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(ArcticExtendSparkSqlParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(ArcticExtendSparkSqlParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(ArcticExtendSparkSqlParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(ArcticExtendSparkSqlParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(ArcticExtendSparkSqlParser.SECOND, 0); }
		public TerminalNode SELECT() { return getToken(ArcticExtendSparkSqlParser.SELECT, 0); }
		public TerminalNode SEPARATED() { return getToken(ArcticExtendSparkSqlParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(ArcticExtendSparkSqlParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.SERDEPROPERTIES, 0); }
		public TerminalNode SESSION_USER() { return getToken(ArcticExtendSparkSqlParser.SESSION_USER, 0); }
		public TerminalNode SET() { return getToken(ArcticExtendSparkSqlParser.SET, 0); }
		public TerminalNode SETS() { return getToken(ArcticExtendSparkSqlParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(ArcticExtendSparkSqlParser.SKEWED, 0); }
		public TerminalNode SOME() { return getToken(ArcticExtendSparkSqlParser.SOME, 0); }
		public TerminalNode SORT() { return getToken(ArcticExtendSparkSqlParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(ArcticExtendSparkSqlParser.SORTED, 0); }
		public TerminalNode START() { return getToken(ArcticExtendSparkSqlParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticExtendSparkSqlParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(ArcticExtendSparkSqlParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(ArcticExtendSparkSqlParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(ArcticExtendSparkSqlParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(ArcticExtendSparkSqlParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticExtendSparkSqlParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(ArcticExtendSparkSqlParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(ArcticExtendSparkSqlParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(ArcticExtendSparkSqlParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode TABLES() { return getToken(ArcticExtendSparkSqlParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticExtendSparkSqlParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticExtendSparkSqlParser.TERMINATED, 0); }
		public TerminalNode THEN() { return getToken(ArcticExtendSparkSqlParser.THEN, 0); }
		public TerminalNode TIME() { return getToken(ArcticExtendSparkSqlParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(ArcticExtendSparkSqlParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TO() { return getToken(ArcticExtendSparkSqlParser.TO, 0); }
		public TerminalNode TOUCH() { return getToken(ArcticExtendSparkSqlParser.TOUCH, 0); }
		public TerminalNode TRAILING() { return getToken(ArcticExtendSparkSqlParser.TRAILING, 0); }
		public TerminalNode TRANSACTION() { return getToken(ArcticExtendSparkSqlParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(ArcticExtendSparkSqlParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(ArcticExtendSparkSqlParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(ArcticExtendSparkSqlParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(ArcticExtendSparkSqlParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(ArcticExtendSparkSqlParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(ArcticExtendSparkSqlParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(ArcticExtendSparkSqlParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(ArcticExtendSparkSqlParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(ArcticExtendSparkSqlParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(ArcticExtendSparkSqlParser.UNCACHE, 0); }
		public TerminalNode UNIQUE() { return getToken(ArcticExtendSparkSqlParser.UNIQUE, 0); }
		public TerminalNode UNKNOWN() { return getToken(ArcticExtendSparkSqlParser.UNKNOWN, 0); }
		public TerminalNode UNLOCK() { return getToken(ArcticExtendSparkSqlParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(ArcticExtendSparkSqlParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(ArcticExtendSparkSqlParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(ArcticExtendSparkSqlParser.USE, 0); }
		public TerminalNode USER() { return getToken(ArcticExtendSparkSqlParser.USER, 0); }
		public TerminalNode VALUES() { return getToken(ArcticExtendSparkSqlParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(ArcticExtendSparkSqlParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticExtendSparkSqlParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(ArcticExtendSparkSqlParser.WEEK, 0); }
		public TerminalNode WHEN() { return getToken(ArcticExtendSparkSqlParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(ArcticExtendSparkSqlParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(ArcticExtendSparkSqlParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode WITHIN() { return getToken(ArcticExtendSparkSqlParser.WITHIN, 0); }
		public TerminalNode YEAR() { return getToken(ArcticExtendSparkSqlParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(ArcticExtendSparkSqlParser.ZONE, 0); }
		public NonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonReservedContext nonReserved() throws RecognitionException {
		NonReservedContext _localctx = new NonReservedContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_nonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3325);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALL) | (1L << ALTER) | (1L << ANALYZE) | (1L << AND) | (1L << ANY) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << AS) | (1L << ASC) | (1L << AT) | (1L << AUTHORIZATION) | (1L << BETWEEN) | (1L << BOTH) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CASE) | (1L << CAST) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CHECK) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLATE) | (1L << COLLECTION) | (1L << COLUMN) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << CONSTRAINT) | (1L << COST) | (1L << CREATE) | (1L << CUBE) | (1L << CURRENT) | (1L << CURRENT_DATE) | (1L << CURRENT_TIME) | (1L << CURRENT_TIMESTAMP) | (1L << CURRENT_USER) | (1L << DAY) | (1L << DAYOFYEAR) | (1L << DATA))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATABASE - 64)) | (1L << (DATABASES - 64)) | (1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTINCT - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ELSE - 64)) | (1L << (END - 64)) | (1L << (ESCAPE - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FALSE - 64)) | (1L << (FETCH - 64)) | (1L << (FIELDS - 64)) | (1L << (FILTER - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FOR - 64)) | (1L << (FOREIGN - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FROM - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GRANT - 64)) | (1L << (GROUP - 64)) | (1L << (GROUPING - 64)) | (1L << (HAVING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (IN - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)) | (1L << (INTO - 64)) | (1L << (IS - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (ITEMS - 128)) | (1L << (KEYS - 128)) | (1L << (LAST - 128)) | (1L << (LAZY - 128)) | (1L << (LEADING - 128)) | (1L << (LIKE - 128)) | (1L << (ILIKE - 128)) | (1L << (LIMIT - 128)) | (1L << (LINES - 128)) | (1L << (LIST - 128)) | (1L << (LOAD - 128)) | (1L << (LOCAL - 128)) | (1L << (LOCATION - 128)) | (1L << (LOCK - 128)) | (1L << (LOCKS - 128)) | (1L << (LOGICAL - 128)) | (1L << (MACRO - 128)) | (1L << (MAP - 128)) | (1L << (MATCHED - 128)) | (1L << (MERGE - 128)) | (1L << (MICROSECOND - 128)) | (1L << (MILLISECOND - 128)) | (1L << (MINUTE - 128)) | (1L << (MONTH - 128)) | (1L << (MSCK - 128)) | (1L << (NAMESPACE - 128)) | (1L << (NAMESPACES - 128)) | (1L << (NO - 128)) | (1L << (NOT - 128)) | (1L << (NULL - 128)) | (1L << (NULLS - 128)) | (1L << (OF - 128)) | (1L << (ONLY - 128)) | (1L << (OPTION - 128)) | (1L << (OPTIONS - 128)) | (1L << (OR - 128)) | (1L << (ORDER - 128)) | (1L << (OUT - 128)) | (1L << (OUTER - 128)) | (1L << (OUTPUTFORMAT - 128)) | (1L << (OVER - 128)) | (1L << (OVERLAPS - 128)) | (1L << (OVERLAY - 128)) | (1L << (OVERWRITE - 128)) | (1L << (PARTITION - 128)) | (1L << (PARTITIONED - 128)) | (1L << (PARTITIONS - 128)) | (1L << (PERCENTILE_CONT - 128)) | (1L << (PERCENTILE_DISC - 128)) | (1L << (PERCENTLIT - 128)) | (1L << (PIVOT - 128)) | (1L << (PLACING - 128)) | (1L << (POSITION - 128)) | (1L << (PRECEDING - 128)) | (1L << (PRIMARY - 128)) | (1L << (PRINCIPALS - 128)) | (1L << (PROPERTIES - 128)) | (1L << (PURGE - 128)) | (1L << (QUARTER - 128)))) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & ((1L << (QUERY - 192)) | (1L << (RANGE - 192)) | (1L << (RECORDREADER - 192)) | (1L << (RECORDWRITER - 192)) | (1L << (RECOVER - 192)) | (1L << (REDUCE - 192)) | (1L << (REFERENCES - 192)) | (1L << (REFRESH - 192)) | (1L << (RENAME - 192)) | (1L << (REPAIR - 192)) | (1L << (REPEATABLE - 192)) | (1L << (REPLACE - 192)) | (1L << (RESET - 192)) | (1L << (RESPECT - 192)) | (1L << (RESTRICT - 192)) | (1L << (REVOKE - 192)) | (1L << (RLIKE - 192)) | (1L << (ROLE - 192)) | (1L << (ROLES - 192)) | (1L << (ROLLBACK - 192)) | (1L << (ROLLUP - 192)) | (1L << (ROW - 192)) | (1L << (ROWS - 192)) | (1L << (SECOND - 192)) | (1L << (SCHEMA - 192)) | (1L << (SCHEMAS - 192)) | (1L << (SELECT - 192)) | (1L << (SEPARATED - 192)) | (1L << (SERDE - 192)) | (1L << (SERDEPROPERTIES - 192)) | (1L << (SESSION_USER - 192)) | (1L << (SET - 192)) | (1L << (SETS - 192)) | (1L << (SHOW - 192)) | (1L << (SKEWED - 192)) | (1L << (SOME - 192)) | (1L << (SORT - 192)) | (1L << (SORTED - 192)) | (1L << (START - 192)) | (1L << (STATISTICS - 192)) | (1L << (STORED - 192)) | (1L << (STRATIFY - 192)) | (1L << (STRUCT - 192)) | (1L << (SUBSTR - 192)) | (1L << (SUBSTRING - 192)) | (1L << (SYNC - 192)) | (1L << (SYSTEM_TIME - 192)) | (1L << (SYSTEM_VERSION - 192)) | (1L << (TABLE - 192)) | (1L << (TABLES - 192)) | (1L << (TABLESAMPLE - 192)) | (1L << (TBLPROPERTIES - 192)) | (1L << (TEMPORARY - 192)) | (1L << (TERMINATED - 192)) | (1L << (THEN - 192)) | (1L << (TIME - 192)) | (1L << (TIMESTAMP - 192)) | (1L << (TIMESTAMPADD - 192)) | (1L << (TIMESTAMPDIFF - 192)) | (1L << (TO - 192)) | (1L << (TOUCH - 192)))) != 0) || ((((_la - 256)) & ~0x3f) == 0 && ((1L << (_la - 256)) & ((1L << (TRAILING - 256)) | (1L << (TRANSACTION - 256)) | (1L << (TRANSACTIONS - 256)) | (1L << (TRANSFORM - 256)) | (1L << (TRIM - 256)) | (1L << (TRUE - 256)) | (1L << (TRUNCATE - 256)) | (1L << (TRY_CAST - 256)) | (1L << (TYPE - 256)) | (1L << (UNARCHIVE - 256)) | (1L << (UNBOUNDED - 256)) | (1L << (UNCACHE - 256)) | (1L << (UNIQUE - 256)) | (1L << (UNKNOWN - 256)) | (1L << (UNLOCK - 256)) | (1L << (UNSET - 256)) | (1L << (UPDATE - 256)) | (1L << (USE - 256)) | (1L << (USER - 256)) | (1L << (VALUES - 256)) | (1L << (VERSION - 256)) | (1L << (VIEW - 256)) | (1L << (VIEWS - 256)) | (1L << (WEEK - 256)) | (1L << (WHEN - 256)) | (1L << (WHERE - 256)) | (1L << (WINDOW - 256)) | (1L << (WITH - 256)) | (1L << (WITHIN - 256)) | (1L << (YEAR - 256)) | (1L << (ZONE - 256)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 48:
			return queryTerm_sempred((QueryTermContext)_localctx, predIndex);
		case 110:
			return booleanExpression_sempred((BooleanExpressionContext)_localctx, predIndex);
		case 112:
			return valueExpression_sempred((ValueExpressionContext)_localctx, predIndex);
		case 114:
			return primaryExpression_sempred((PrimaryExpressionContext)_localctx, predIndex);
		case 145:
			return identifier_sempred((IdentifierContext)_localctx, predIndex);
		case 146:
			return strictIdentifier_sempred((StrictIdentifierContext)_localctx, predIndex);
		case 148:
			return number_sempred((NumberContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean queryTerm_sempred(QueryTermContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 3);
		case 1:
			return legacy_setops_precedence_enabled;
		case 2:
			return precpred(_ctx, 2);
		case 3:
			return !legacy_setops_precedence_enabled;
		case 4:
			return precpred(_ctx, 1);
		case 5:
			return !legacy_setops_precedence_enabled;
		}
		return true;
	}
	private boolean booleanExpression_sempred(BooleanExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6:
			return precpred(_ctx, 2);
		case 7:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean valueExpression_sempred(ValueExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8:
			return precpred(_ctx, 6);
		case 9:
			return precpred(_ctx, 5);
		case 10:
			return precpred(_ctx, 4);
		case 11:
			return precpred(_ctx, 3);
		case 12:
			return precpred(_ctx, 2);
		case 13:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean primaryExpression_sempred(PrimaryExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 14:
			return precpred(_ctx, 9);
		case 15:
			return precpred(_ctx, 7);
		}
		return true;
	}
	private boolean identifier_sempred(IdentifierContext _localctx, int predIndex) {
		switch (predIndex) {
		case 16:
			return !SQL_standard_keyword_behavior;
		}
		return true;
	}
	private boolean strictIdentifier_sempred(StrictIdentifierContext _localctx, int predIndex) {
		switch (predIndex) {
		case 17:
			return SQL_standard_keyword_behavior;
		case 18:
			return !SQL_standard_keyword_behavior;
		}
		return true;
	}
	private boolean number_sempred(NumberContext _localctx, int predIndex) {
		switch (predIndex) {
		case 19:
			return !legacy_exponent_literal_as_decimal_enabled;
		case 20:
			return !legacy_exponent_literal_as_decimal_enabled;
		case 21:
			return legacy_exponent_literal_as_decimal_enabled;
		}
		return true;
	}

	private static final int _serializedATNSegments = 2;
	private static final String _serializedATNSegment0 =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0148\u0d02\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t"+
		"k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\4"+
		"w\tw\4x\tx\4y\ty\4z\tz\4{\t{\4|\t|\4}\t}\4~\t~\4\177\t\177\4\u0080\t\u0080"+
		"\4\u0081\t\u0081\4\u0082\t\u0082\4\u0083\t\u0083\4\u0084\t\u0084\4\u0085"+
		"\t\u0085\4\u0086\t\u0086\4\u0087\t\u0087\4\u0088\t\u0088\4\u0089\t\u0089"+
		"\4\u008a\t\u008a\4\u008b\t\u008b\4\u008c\t\u008c\4\u008d\t\u008d\4\u008e"+
		"\t\u008e\4\u008f\t\u008f\4\u0090\t\u0090\4\u0091\t\u0091\4\u0092\t\u0092"+
		"\4\u0093\t\u0093\4\u0094\t\u0094\4\u0095\t\u0095\4\u0096\t\u0096\4\u0097"+
		"\t\u0097\4\u0098\t\u0098\4\u0099\t\u0099\4\u009a\t\u009a\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\4\3\4\3\4\5\4\u013e\n\4\3\4\5\4\u0141\n\4\3\5\3\5\3\5\3\5"+
		"\3\6\3\6\3\6\3\6\5\6\u014b\n\6\3\6\3\6\3\6\5\6\u0150\n\6\3\7\3\7\7\7\u0154"+
		"\n\7\f\7\16\7\u0157\13\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\3"+
		"\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\5\16\u016f\n\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u017c\n\16\3\16"+
		"\3\16\3\16\3\16\3\16\5\16\u0183\n\16\3\16\3\16\3\16\3\16\3\16\3\16\7\16"+
		"\u018b\n\16\f\16\16\16\u018e\13\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u01a1\n\16\3\16"+
		"\3\16\5\16\u01a5\n\16\3\16\3\16\3\16\3\16\5\16\u01ab\n\16\3\16\5\16\u01ae"+
		"\n\16\3\16\5\16\u01b1\n\16\3\16\3\16\3\16\3\16\3\16\5\16\u01b8\n\16\3"+
		"\16\5\16\u01bb\n\16\3\16\3\16\5\16\u01bf\n\16\3\16\5\16\u01c2\n\16\3\16"+
		"\3\16\3\16\3\16\3\16\5\16\u01c9\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\7\16\u01d4\n\16\f\16\16\16\u01d7\13\16\3\16\3\16\3\16\3\16"+
		"\3\16\5\16\u01de\n\16\3\16\5\16\u01e1\n\16\3\16\3\16\5\16\u01e5\n\16\3"+
		"\16\5\16\u01e8\n\16\3\16\3\16\3\16\3\16\5\16\u01ee\n\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u01f9\n\16\3\16\3\16\3\16\3\16\5\16"+
		"\u01ff\n\16\3\16\3\16\3\16\5\16\u0204\n\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0226"+
		"\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0233"+
		"\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u024c\n\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0255\n\16\3\16\3\16\5\16\u0259\n"+
		"\16\3\16\3\16\3\16\3\16\5\16\u025f\n\16\3\16\3\16\5\16\u0263\n\16\3\16"+
		"\3\16\3\16\5\16\u0268\n\16\3\16\3\16\3\16\3\16\5\16\u026e\n\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u027a\n\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\5\16\u0282\n\16\3\16\3\16\3\16\3\16\5\16\u0288\n"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0295"+
		"\n\16\3\16\6\16\u0298\n\16\r\16\16\16\u0299\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u02aa\n\16\3\16\3\16"+
		"\3\16\7\16\u02af\n\16\f\16\16\16\u02b2\13\16\3\16\5\16\u02b5\n\16\3\16"+
		"\3\16\3\16\3\16\5\16\u02bb\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\5\16\u02ca\n\16\3\16\3\16\5\16\u02ce\n\16\3"+
		"\16\3\16\3\16\3\16\5\16\u02d4\n\16\3\16\3\16\3\16\3\16\5\16\u02da\n\16"+
		"\3\16\5\16\u02dd\n\16\3\16\5\16\u02e0\n\16\3\16\3\16\3\16\3\16\5\16\u02e6"+
		"\n\16\3\16\3\16\5\16\u02ea\n\16\3\16\3\16\3\16\3\16\3\16\3\16\7\16\u02f2"+
		"\n\16\f\16\16\16\u02f5\13\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u02fd"+
		"\n\16\3\16\5\16\u0300\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0309"+
		"\n\16\3\16\3\16\3\16\5\16\u030e\n\16\3\16\3\16\3\16\3\16\5\16\u0314\n"+
		"\16\3\16\3\16\3\16\3\16\3\16\5\16\u031b\n\16\3\16\5\16\u031e\n\16\3\16"+
		"\3\16\3\16\3\16\5\16\u0324\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\7\16"+
		"\u032d\n\16\f\16\16\16\u0330\13\16\5\16\u0332\n\16\3\16\3\16\5\16\u0336"+
		"\n\16\3\16\3\16\3\16\5\16\u033b\n\16\3\16\3\16\3\16\5\16\u0340\n\16\3"+
		"\16\3\16\3\16\3\16\3\16\5\16\u0347\n\16\3\16\5\16\u034a\n\16\3\16\5\16"+
		"\u034d\n\16\3\16\3\16\3\16\3\16\3\16\5\16\u0354\n\16\3\16\3\16\3\16\5"+
		"\16\u0359\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0362\n\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\5\16\u036a\n\16\3\16\3\16\3\16\3\16\5\16\u0370"+
		"\n\16\3\16\5\16\u0373\n\16\3\16\5\16\u0376\n\16\3\16\3\16\3\16\3\16\5"+
		"\16\u037c\n\16\3\16\3\16\5\16\u0380\n\16\3\16\3\16\3\16\5\16\u0385\n\16"+
		"\3\16\5\16\u0388\n\16\3\16\3\16\5\16\u038c\n\16\5\16\u038e\n\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\5\16\u0396\n\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\5\16\u039e\n\16\3\16\5\16\u03a1\n\16\3\16\3\16\3\16\5\16\u03a6\n\16\3"+
		"\16\3\16\3\16\3\16\5\16\u03ac\n\16\3\16\3\16\3\16\3\16\5\16\u03b2\n\16"+
		"\3\16\5\16\u03b5\n\16\3\16\3\16\5\16\u03b9\n\16\3\16\5\16\u03bc\n\16\3"+
		"\16\3\16\5\16\u03c0\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\7\16\u03da\n\16\f\16\16\16\u03dd\13\16\5\16\u03df\n\16\3\16\3\16"+
		"\5\16\u03e3\n\16\3\16\3\16\3\16\3\16\5\16\u03e9\n\16\3\16\5\16\u03ec\n"+
		"\16\3\16\5\16\u03ef\n\16\3\16\3\16\3\16\3\16\5\16\u03f5\n\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\5\16\u03fd\n\16\3\16\3\16\3\16\5\16\u0402\n\16\3"+
		"\16\3\16\3\16\3\16\5\16\u0408\n\16\3\16\3\16\3\16\3\16\5\16\u040e\n\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0416\n\16\3\16\3\16\3\16\7\16\u041b"+
		"\n\16\f\16\16\16\u041e\13\16\3\16\3\16\3\16\7\16\u0423\n\16\f\16\16\16"+
		"\u0426\13\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\7\16\u0434\n\16\f\16\16\16\u0437\13\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\7\16\u0442\n\16\f\16\16\16\u0445\13\16\5\16\u0447\n"+
		"\16\3\16\3\16\7\16\u044b\n\16\f\16\16\16\u044e\13\16\3\16\3\16\3\16\3"+
		"\16\7\16\u0454\n\16\f\16\16\16\u0457\13\16\3\16\3\16\3\16\3\16\7\16\u045d"+
		"\n\16\f\16\16\16\u0460\13\16\3\16\3\16\3\16\3\16\3\16\5\16\u0467\n\16"+
		"\3\16\3\16\3\16\5\16\u046c\n\16\3\16\3\16\3\16\5\16\u0471\n\16\3\16\3"+
		"\16\3\16\3\16\3\16\5\16\u0478\n\16\3\16\3\16\3\16\3\16\5\16\u047e\n\16"+
		"\3\16\3\16\3\16\5\16\u0483\n\16\3\16\3\16\3\16\3\16\7\16\u0489\n\16\f"+
		"\16\16\16\u048c\13\16\5\16\u048e\n\16\3\17\3\17\3\20\3\20\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\5\21\u049a\n\21\3\21\3\21\5\21\u049e\n\21\3\21\3\21"+
		"\3\21\3\21\3\21\5\21\u04a5\n\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u0519\n\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\5\21\u0521\n\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u0529"+
		"\n\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u0532\n\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\5\21\u053c\n\21\3\22\3\22\5\22\u0540\n\22\3"+
		"\22\5\22\u0543\n\22\3\22\3\22\3\22\3\22\5\22\u0549\n\22\3\22\3\22\3\23"+
		"\3\23\5\23\u054f\n\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\5\24\u055b\n\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\5\25"+
		"\u0567\n\25\3\25\3\25\3\25\5\25\u056c\n\25\3\26\3\26\3\26\3\27\3\27\3"+
		"\27\3\30\5\30\u0575\n\30\3\30\3\30\3\30\3\31\3\31\3\31\5\31\u057d\n\31"+
		"\3\31\3\31\3\31\3\31\3\31\5\31\u0584\n\31\5\31\u0586\n\31\3\31\5\31\u0589"+
		"\n\31\3\31\3\31\3\31\5\31\u058e\n\31\3\31\3\31\5\31\u0592\n\31\3\31\3"+
		"\31\3\31\5\31\u0597\n\31\3\31\5\31\u059a\n\31\3\31\3\31\3\31\5\31\u059f"+
		"\n\31\3\31\3\31\3\31\5\31\u05a4\n\31\3\31\5\31\u05a7\n\31\3\31\3\31\3"+
		"\31\5\31\u05ac\n\31\3\31\3\31\5\31\u05b0\n\31\3\31\3\31\3\31\5\31\u05b5"+
		"\n\31\5\31\u05b7\n\31\3\32\3\32\5\32\u05bb\n\32\3\33\3\33\3\33\3\33\3"+
		"\33\7\33\u05c2\n\33\f\33\16\33\u05c5\13\33\3\33\3\33\3\34\3\34\3\34\5"+
		"\34\u05cc\n\34\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3\37\3\37\5\37\u05d7"+
		"\n\37\3 \3 \3 \7 \u05dc\n \f \16 \u05df\13 \3!\3!\3!\3!\7!\u05e5\n!\f"+
		"!\16!\u05e8\13!\3\"\3\"\5\"\u05ec\n\"\3\"\5\"\u05ef\n\"\3\"\3\"\3\"\3"+
		"\"\3#\3#\3#\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\7$\u0605\n$\f$\16$"+
		"\u0608\13$\3%\3%\3%\3%\7%\u060e\n%\f%\16%\u0611\13%\3%\3%\3&\3&\5&\u0617"+
		"\n&\3&\5&\u061a\n&\3\'\3\'\3\'\7\'\u061f\n\'\f\'\16\'\u0622\13\'\3\'\5"+
		"\'\u0625\n\'\3(\3(\3(\3(\5(\u062b\n(\3)\3)\3)\3)\7)\u0631\n)\f)\16)\u0634"+
		"\13)\3)\3)\3*\3*\3*\3*\7*\u063c\n*\f*\16*\u063f\13*\3*\3*\3+\3+\3+\3+"+
		"\3+\3+\5+\u0649\n+\3,\3,\3,\3,\3,\5,\u0650\n,\3-\3-\3-\3-\5-\u0656\n-"+
		"\3.\3.\3.\3/\3/\3/\3/\3/\6/\u0660\n/\r/\16/\u0661\3/\3/\3/\3/\3/\5/\u0669"+
		"\n/\3/\3/\3/\3/\3/\5/\u0670\n/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\5/\u067c"+
		"\n/\3/\3/\3/\3/\7/\u0682\n/\f/\16/\u0685\13/\3/\7/\u0688\n/\f/\16/\u068b"+
		"\13/\5/\u068d\n/\3\60\3\60\3\60\3\60\3\60\7\60\u0694\n\60\f\60\16\60\u0697"+
		"\13\60\5\60\u0699\n\60\3\60\3\60\3\60\3\60\3\60\7\60\u06a0\n\60\f\60\16"+
		"\60\u06a3\13\60\5\60\u06a5\n\60\3\60\3\60\3\60\3\60\3\60\7\60\u06ac\n"+
		"\60\f\60\16\60\u06af\13\60\5\60\u06b1\n\60\3\60\3\60\3\60\3\60\3\60\7"+
		"\60\u06b8\n\60\f\60\16\60\u06bb\13\60\5\60\u06bd\n\60\3\60\5\60\u06c0"+
		"\n\60\3\60\3\60\3\60\5\60\u06c5\n\60\5\60\u06c7\n\60\3\61\3\61\3\61\3"+
		"\62\3\62\3\62\3\62\3\62\3\62\3\62\5\62\u06d3\n\62\3\62\3\62\3\62\3\62"+
		"\3\62\5\62\u06da\n\62\3\62\3\62\3\62\3\62\3\62\5\62\u06e1\n\62\3\62\7"+
		"\62\u06e4\n\62\f\62\16\62\u06e7\13\62\3\63\3\63\3\63\3\63\3\63\3\63\3"+
		"\63\3\63\3\63\5\63\u06f2\n\63\3\64\3\64\5\64\u06f6\n\64\3\64\3\64\5\64"+
		"\u06fa\n\64\3\65\3\65\6\65\u06fe\n\65\r\65\16\65\u06ff\3\66\3\66\5\66"+
		"\u0704\n\66\3\66\3\66\3\66\3\66\7\66\u070a\n\66\f\66\16\66\u070d\13\66"+
		"\3\66\5\66\u0710\n\66\3\66\5\66\u0713\n\66\3\66\5\66\u0716\n\66\3\66\5"+
		"\66\u0719\n\66\3\66\3\66\5\66\u071d\n\66\3\67\3\67\5\67\u0721\n\67\3\67"+
		"\7\67\u0724\n\67\f\67\16\67\u0727\13\67\3\67\5\67\u072a\n\67\3\67\5\67"+
		"\u072d\n\67\3\67\5\67\u0730\n\67\3\67\5\67\u0733\n\67\3\67\3\67\5\67\u0737"+
		"\n\67\3\67\7\67\u073a\n\67\f\67\16\67\u073d\13\67\3\67\5\67\u0740\n\67"+
		"\3\67\5\67\u0743\n\67\3\67\5\67\u0746\n\67\3\67\5\67\u0749\n\67\5\67\u074b"+
		"\n\67\38\38\38\38\58\u0751\n8\38\38\38\38\38\58\u0758\n8\38\38\38\58\u075d"+
		"\n8\38\58\u0760\n8\38\58\u0763\n8\38\38\58\u0767\n8\38\38\38\38\38\38"+
		"\38\38\58\u0771\n8\38\38\58\u0775\n8\58\u0777\n8\38\58\u077a\n8\38\38"+
		"\58\u077e\n8\39\39\79\u0782\n9\f9\169\u0785\139\39\59\u0788\n9\39\39\3"+
		":\3:\3:\3;\3;\3;\3;\5;\u0793\n;\3;\3;\3;\3<\3<\3<\3<\3<\5<\u079d\n<\3"+
		"<\3<\3<\3=\3=\3=\3=\3=\3=\3=\5=\u07a9\n=\3>\3>\3>\3>\3>\3>\3>\3>\3>\3"+
		">\3>\7>\u07b6\n>\f>\16>\u07b9\13>\3>\3>\5>\u07bd\n>\3?\3?\3?\7?\u07c2"+
		"\n?\f?\16?\u07c5\13?\3@\3@\3@\3@\3A\3A\3A\3B\3B\3B\3C\3C\3C\5C\u07d4\n"+
		"C\3C\7C\u07d7\nC\fC\16C\u07da\13C\3C\3C\3D\3D\3D\3D\3D\3D\7D\u07e4\nD"+
		"\fD\16D\u07e7\13D\3D\3D\5D\u07eb\nD\3E\3E\3E\3E\7E\u07f1\nE\fE\16E\u07f4"+
		"\13E\3E\7E\u07f7\nE\fE\16E\u07fa\13E\3E\5E\u07fd\nE\3F\5F\u0800\nF\3F"+
		"\3F\3F\3F\3F\5F\u0807\nF\3F\3F\3F\3F\5F\u080d\nF\3G\3G\3G\3G\3G\7G\u0814"+
		"\nG\fG\16G\u0817\13G\3G\3G\3G\3G\3G\7G\u081e\nG\fG\16G\u0821\13G\3G\3"+
		"G\3G\3G\3G\3G\3G\3G\3G\3G\7G\u082d\nG\fG\16G\u0830\13G\3G\3G\5G\u0834"+
		"\nG\5G\u0836\nG\3H\3H\5H\u083a\nH\3I\3I\3I\3I\3I\7I\u0841\nI\fI\16I\u0844"+
		"\13I\3I\3I\3I\3I\3I\3I\3I\3I\7I\u084e\nI\fI\16I\u0851\13I\3I\3I\5I\u0855"+
		"\nI\3J\3J\5J\u0859\nJ\3K\3K\3K\3K\7K\u085f\nK\fK\16K\u0862\13K\5K\u0864"+
		"\nK\3K\3K\5K\u0868\nK\3L\3L\3L\3L\3L\3L\3L\3L\3L\3L\7L\u0874\nL\fL\16"+
		"L\u0877\13L\3L\3L\3L\3M\3M\3M\3M\3M\7M\u0881\nM\fM\16M\u0884\13M\3M\3"+
		"M\5M\u0888\nM\3N\3N\5N\u088c\nN\3N\5N\u088f\nN\3O\3O\3O\5O\u0894\nO\3"+
		"O\3O\3O\3O\3O\7O\u089b\nO\fO\16O\u089e\13O\5O\u08a0\nO\3O\3O\3O\5O\u08a5"+
		"\nO\3O\3O\3O\7O\u08aa\nO\fO\16O\u08ad\13O\5O\u08af\nO\3P\3P\3Q\5Q\u08b4"+
		"\nQ\3Q\3Q\7Q\u08b8\nQ\fQ\16Q\u08bb\13Q\3R\3R\3R\5R\u08c0\nR\3R\3R\5R\u08c4"+
		"\nR\3R\3R\3R\3R\5R\u08ca\nR\3R\3R\5R\u08ce\nR\3S\5S\u08d1\nS\3S\3S\3S"+
		"\5S\u08d6\nS\3S\5S\u08d9\nS\3S\3S\3S\5S\u08de\nS\3S\3S\5S\u08e2\nS\3S"+
		"\5S\u08e5\nS\3S\5S\u08e8\nS\3T\3T\3T\3T\5T\u08ee\nT\3U\3U\3U\5U\u08f3"+
		"\nU\3U\3U\3U\3U\3U\5U\u08fa\nU\3V\5V\u08fd\nV\3V\3V\3V\3V\3V\3V\3V\3V"+
		"\3V\3V\3V\3V\3V\3V\3V\3V\5V\u090f\nV\5V\u0911\nV\3V\5V\u0914\nV\3W\3W"+
		"\3W\3W\3X\3X\3X\7X\u091d\nX\fX\16X\u0920\13X\3Y\3Y\3Y\3Y\7Y\u0926\nY\f"+
		"Y\16Y\u0929\13Y\3Y\3Y\3Z\3Z\5Z\u092f\nZ\3[\3[\3[\3[\7[\u0935\n[\f[\16"+
		"[\u0938\13[\3[\3[\3\\\3\\\5\\\u093e\n\\\3]\3]\5]\u0942\n]\3]\5]\u0945"+
		"\n]\3]\3]\3]\3]\3]\3]\5]\u094d\n]\3]\3]\3]\3]\3]\3]\5]\u0955\n]\3]\3]"+
		"\3]\3]\5]\u095b\n]\3^\3^\3^\3^\7^\u0961\n^\f^\16^\u0964\13^\3^\3^\3_\3"+
		"_\3_\3_\3_\7_\u096d\n_\f_\16_\u0970\13_\5_\u0972\n_\3_\3_\3_\3`\5`\u0978"+
		"\n`\3`\3`\5`\u097c\n`\5`\u097e\n`\3a\3a\3a\3a\3a\3a\3a\5a\u0987\na\3a"+
		"\3a\3a\3a\3a\3a\3a\3a\3a\3a\5a\u0993\na\5a\u0995\na\3a\3a\3a\3a\3a\5a"+
		"\u099c\na\3a\3a\3a\3a\3a\5a\u09a3\na\3a\3a\3a\3a\5a\u09a9\na\3a\3a\3a"+
		"\3a\5a\u09af\na\5a\u09b1\na\3b\3b\3b\7b\u09b6\nb\fb\16b\u09b9\13b\3c\3"+
		"c\3c\7c\u09be\nc\fc\16c\u09c1\13c\3d\3d\3d\7d\u09c6\nd\fd\16d\u09c9\13"+
		"d\3e\3e\3e\5e\u09ce\ne\3f\3f\3f\5f\u09d3\nf\3f\3f\3g\3g\3g\5g\u09da\n"+
		"g\3g\3g\3h\3h\5h\u09e0\nh\3h\3h\5h\u09e4\nh\5h\u09e6\nh\3i\3i\3i\7i\u09eb"+
		"\ni\fi\16i\u09ee\13i\3j\3j\3j\3j\7j\u09f4\nj\fj\16j\u09f7\13j\3j\3j\3"+
		"k\3k\5k\u09fd\nk\3l\3l\3l\3l\3l\3l\7l\u0a05\nl\fl\16l\u0a08\13l\3l\3l"+
		"\5l\u0a0c\nl\3m\3m\5m\u0a10\nm\3n\3n\3o\3o\3o\7o\u0a17\no\fo\16o\u0a1a"+
		"\13o\3p\3p\3p\3p\3p\3p\3p\3p\3p\3p\5p\u0a26\np\5p\u0a28\np\3p\3p\3p\3"+
		"p\3p\3p\7p\u0a30\np\fp\16p\u0a33\13p\3q\5q\u0a36\nq\3q\3q\3q\3q\3q\3q"+
		"\5q\u0a3e\nq\3q\3q\3q\3q\3q\7q\u0a45\nq\fq\16q\u0a48\13q\3q\3q\3q\5q\u0a4d"+
		"\nq\3q\3q\3q\3q\3q\3q\5q\u0a55\nq\3q\3q\3q\5q\u0a5a\nq\3q\3q\3q\3q\3q"+
		"\3q\3q\3q\7q\u0a64\nq\fq\16q\u0a67\13q\3q\3q\5q\u0a6b\nq\3q\5q\u0a6e\n"+
		"q\3q\3q\3q\3q\5q\u0a74\nq\3q\3q\5q\u0a78\nq\3q\3q\3q\5q\u0a7d\nq\3q\3"+
		"q\3q\5q\u0a82\nq\3q\3q\3q\5q\u0a87\nq\3r\3r\3r\3r\5r\u0a8d\nr\3r\3r\3"+
		"r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\3r\7r\u0aa2\nr\fr\16r\u0aa5"+
		"\13r\3s\3s\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3"+
		"t\3t\3t\6t\u0abf\nt\rt\16t\u0ac0\3t\3t\5t\u0ac5\nt\3t\3t\3t\3t\3t\6t\u0acc"+
		"\nt\rt\16t\u0acd\3t\3t\5t\u0ad2\nt\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3"+
		"t\3t\3t\7t\u0ae2\nt\ft\16t\u0ae5\13t\5t\u0ae7\nt\3t\3t\3t\3t\3t\3t\5t"+
		"\u0aef\nt\3t\3t\3t\3t\3t\3t\3t\5t\u0af8\nt\3t\3t\3t\3t\3t\3t\3t\3t\3t"+
		"\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\6t\u0b0d\nt\rt\16t\u0b0e\3t\3t\3t\3t\3"+
		"t\3t\3t\3t\3t\5t\u0b1a\nt\3t\3t\3t\7t\u0b1f\nt\ft\16t\u0b22\13t\5t\u0b24"+
		"\nt\3t\3t\3t\3t\3t\3t\3t\5t\u0b2d\nt\3t\3t\5t\u0b31\nt\3t\3t\5t\u0b35"+
		"\nt\3t\3t\3t\3t\3t\3t\3t\3t\6t\u0b3f\nt\rt\16t\u0b40\3t\3t\3t\3t\3t\3"+
		"t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\5t\u0b5a\nt\3t\3"+
		"t\3t\3t\3t\5t\u0b61\nt\3t\5t\u0b64\nt\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3"+
		"t\3t\3t\5t\u0b73\nt\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\3t\5t\u0b84"+
		"\nt\5t\u0b86\nt\3t\3t\3t\3t\3t\3t\3t\3t\7t\u0b90\nt\ft\16t\u0b93\13t\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\6u\u0b9d\nu\ru\16u\u0b9e\5u\u0ba1\nu\3v\3v\3w\3"+
		"w\3x\3x\3y\3y\3z\3z\3z\5z\u0bae\nz\3{\3{\5{\u0bb2\n{\3|\3|\3|\6|\u0bb7"+
		"\n|\r|\16|\u0bb8\3}\3}\3}\5}\u0bbe\n}\3~\3~\3~\3~\3~\3\177\5\177\u0bc6"+
		"\n\177\3\177\3\177\3\u0080\3\u0080\3\u0080\5\u0080\u0bcd\n\u0080\3\u0081"+
		"\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081"+
		"\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\5\u0081\u0bde\n\u0081\3\u0081"+
		"\3\u0081\5\u0081\u0be2\n\u0081\3\u0081\3\u0081\3\u0081\3\u0081\5\u0081"+
		"\u0be8\n\u0081\3\u0081\3\u0081\3\u0081\3\u0081\5\u0081\u0bee\n\u0081\3"+
		"\u0081\3\u0081\3\u0081\3\u0081\3\u0081\7\u0081\u0bf5\n\u0081\f\u0081\16"+
		"\u0081\u0bf8\13\u0081\3\u0081\5\u0081\u0bfb\n\u0081\5\u0081\u0bfd\n\u0081"+
		"\3\u0082\3\u0082\3\u0082\7\u0082\u0c02\n\u0082\f\u0082\16\u0082\u0c05"+
		"\13\u0082\3\u0083\3\u0083\3\u0083\3\u0083\5\u0083\u0c0b\n\u0083\3\u0083"+
		"\5\u0083\u0c0e\n\u0083\3\u0083\5\u0083\u0c11\n\u0083\3\u0084\3\u0084\3"+
		"\u0084\7\u0084\u0c16\n\u0084\f\u0084\16\u0084\u0c19\13\u0084\3\u0085\3"+
		"\u0085\3\u0085\3\u0085\5\u0085\u0c1f\n\u0085\3\u0085\5\u0085\u0c22\n\u0085"+
		"\3\u0086\3\u0086\3\u0086\7\u0086\u0c27\n\u0086\f\u0086\16\u0086\u0c2a"+
		"\13\u0086\3\u0087\3\u0087\5\u0087\u0c2e\n\u0087\3\u0087\3\u0087\3\u0087"+
		"\5\u0087\u0c33\n\u0087\3\u0087\5\u0087\u0c36\n\u0087\3\u0088\3\u0088\3"+
		"\u0088\3\u0088\3\u0088\3\u0089\3\u0089\3\u0089\3\u0089\7\u0089\u0c41\n"+
		"\u0089\f\u0089\16\u0089\u0c44\13\u0089\3\u008a\3\u008a\3\u008a\3\u008a"+
		"\3\u008b\3\u008b\3\u008b\3\u008b\3\u008b\3\u008b\3\u008b\3\u008b\3\u008b"+
		"\3\u008b\3\u008b\7\u008b\u0c55\n\u008b\f\u008b\16\u008b\u0c58\13\u008b"+
		"\3\u008b\3\u008b\3\u008b\3\u008b\3\u008b\7\u008b\u0c5f\n\u008b\f\u008b"+
		"\16\u008b\u0c62\13\u008b\5\u008b\u0c64\n\u008b\3\u008b\3\u008b\3\u008b"+
		"\3\u008b\3\u008b\7\u008b\u0c6b\n\u008b\f\u008b\16\u008b\u0c6e\13\u008b"+
		"\5\u008b\u0c70\n\u008b\5\u008b\u0c72\n\u008b\3\u008b\5\u008b\u0c75\n\u008b"+
		"\3\u008b\5\u008b\u0c78\n\u008b\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c"+
		"\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c"+
		"\3\u008c\3\u008c\5\u008c\u0c8a\n\u008c\3\u008d\3\u008d\3\u008d\3\u008d"+
		"\3\u008d\3\u008d\3\u008d\5\u008d\u0c93\n\u008d\3\u008e\3\u008e\3\u008e"+
		"\7\u008e\u0c98\n\u008e\f\u008e\16\u008e\u0c9b\13\u008e\3\u008f\3\u008f"+
		"\3\u008f\3\u008f\5\u008f\u0ca1\n\u008f\3\u0090\3\u0090\3\u0090\7\u0090"+
		"\u0ca6\n\u0090\f\u0090\16\u0090\u0ca9\13\u0090\3\u0091\3\u0091\3\u0091"+
		"\3\u0092\3\u0092\6\u0092\u0cb0\n\u0092\r\u0092\16\u0092\u0cb1\3\u0092"+
		"\5\u0092\u0cb5\n\u0092\3\u0093\3\u0093\3\u0093\5\u0093\u0cba\n\u0093\3"+
		"\u0094\3\u0094\3\u0094\3\u0094\3\u0094\3\u0094\5\u0094\u0cc2\n\u0094\3"+
		"\u0095\3\u0095\3\u0096\3\u0096\5\u0096\u0cc8\n\u0096\3\u0096\3\u0096\3"+
		"\u0096\5\u0096\u0ccd\n\u0096\3\u0096\3\u0096\3\u0096\5\u0096\u0cd2\n\u0096"+
		"\3\u0096\3\u0096\5\u0096\u0cd6\n\u0096\3\u0096\3\u0096\5\u0096\u0cda\n"+
		"\u0096\3\u0096\3\u0096\5\u0096\u0cde\n\u0096\3\u0096\3\u0096\5\u0096\u0ce2"+
		"\n\u0096\3\u0096\3\u0096\5\u0096\u0ce6\n\u0096\3\u0096\3\u0096\5\u0096"+
		"\u0cea\n\u0096\3\u0096\3\u0096\5\u0096\u0cee\n\u0096\3\u0096\5\u0096\u0cf1"+
		"\n\u0096\3\u0097\3\u0097\3\u0097\3\u0097\3\u0097\3\u0097\3\u0097\5\u0097"+
		"\u0cfa\n\u0097\3\u0098\3\u0098\3\u0099\3\u0099\3\u009a\3\u009a\3\u009a"+
		"\13\u03db\u041c\u0424\u0435\u0443\u044c\u0455\u045e\u048a\6b\u00de\u00e2"+
		"\u00e6\u009b\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64"+
		"\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088"+
		"\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0"+
		"\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8"+
		"\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0"+
		"\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8"+
		"\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100"+
		"\u0102\u0104\u0106\u0108\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118"+
		"\u011a\u011c\u011e\u0120\u0122\u0124\u0126\u0128\u012a\u012c\u012e\u0130"+
		"\u0132\2=\4\2FF\u00bf\u00bf\4\2  \u00d0\u00d0\4\2jjww\3\2-.\4\2\u00f5"+
		"\u00f5\u0119\u0119\4\2\17\17%%\7\2**\66\66\\\\ii\u0094\u0094\3\2JK\4\2"+
		"\\\\ii\4\2\u00a3\u00a3\u0139\u0139\5\2\f\fRR\u00f2\u00f2\4\2\f\f\u008e"+
		"\u008e\4\2\u0090\u0090\u0139\u0139\5\2BB\u009e\u009e\u00db\u00db\5\2C"+
		"C\u009f\u009f\u00dc\u00dc\6\2WW~~\u00e4\u00e4\u010e\u010e\5\2WW\u00e4"+
		"\u00e4\u010e\u010e\4\2\27\27JJ\4\2dd\u0085\u0085\4\2\u00f4\u00f4\u0118"+
		"\u0118\4\2\u0139\u0139\u013d\u013d\4\2\u00f3\u00f3\u00fd\u00fd\4\299\u00d7"+
		"\u00d7\4\2\16\16OO\4\2\u013d\u013d\u013f\u013f\3\2\u008a\u008b\5\2\16"+
		"\16\23\23\u00e8\u00e8\5\2__\u0107\u0107\u0110\u0110\4\2\u012b\u012c\u0130"+
		"\u0130\4\2QQ\u012d\u012f\4\2\u012b\u012c\u0133\u0133\t\2?@ss\u0099\u009c"+
		"\u00c1\u00c1\u00da\u00da\u011b\u011b\u0121\u0121\4\2;;=>\4\2DD\u00fe\u00fe"+
		"\4\2EE\u00ff\u00ff\4\2\"\"\u0109\u0109\4\2uu\u00cf\u00cf\3\2\u00f0\u00f1"+
		"\4\2\b\bjj\4\2\b\bff\5\2\33\33\u0088\u0088\u0102\u0102\3\2\u00b6\u00b7"+
		"\3\2\u0123\u012a\4\2QQ\u012b\u0134\6\2\21\21ww\u00a2\u00a2\u00aa\u00aa"+
		"\4\2__\u0107\u0107\3\2\u012b\u012c\5\2\u0139\u0139\u013d\u013d\u013f\u013f"+
		"\4\2\u009c\u009c\u0121\u0121\6\2??ss\u009b\u009b\u00da\u00da\5\2ss\u009b"+
		"\u009b\u00da\u00da\4\2PP\u00b3\u00b3\4\2\u00ab\u00ab\u00e9\u00e9\4\2e"+
		"e\u00bc\u00bc\3\2\u013e\u013f\4\2RR\u00e3\u00e3\65\2\f\r\17\20\22\22\24"+
		"\25\27\30\32\32\34 #%\'*,,.\64\66\669:?NPRVVX^aacehilnqqsvxy{}\177\177"+
		"\u0082\u0082\u0084\u0085\u0087\u0087\u008a\u009f\u00a1\u00a1\u00a4\u00a5"+
		"\u00a8\u00a9\u00ac\u00ac\u00ae\u00af\u00b1\u00b5\u00b8\u00bc\u00be\u00c7"+
		"\u00c9\u00d1\u00d3\u00dc\u00de\u00e1\u00e3\u00e7\u00e9\u00f4\u00f6\u00fa"+
		"\u00fd\u00ff\u0101\u0101\u0103\u010d\u0111\u0114\u0117\u011b\u011e\u011e"+
		"\u0121\u0122\22\2\22\2288WWkkzz~~\u0083\u0083\u0086\u0086\u0089\u0089"+
		"\u00a0\u00a0\u00a6\u00a6\u00d2\u00d2\u00de\u00de\u00e4\u00e4\u010e\u010e"+
		"\u0116\u0116\23\2\f\21\23\679VXjly{}\177\u0082\u0084\u0085\u0087\u0088"+
		"\u008a\u009f\u00a1\u00a5\u00a7\u00d1\u00d3\u00dd\u00df\u00e3\u00e5\u010d"+
		"\u010f\u0115\u0117\u0122\2\u0f00\2\u0134\3\2\2\2\4\u0136\3\2\2\2\6\u0138"+
		"\3\2\2\2\b\u0142\3\2\2\2\n\u014f\3\2\2\2\f\u0151\3\2\2\2\16\u015a\3\2"+
		"\2\2\20\u015d\3\2\2\2\22\u0160\3\2\2\2\24\u0163\3\2\2\2\26\u0166\3\2\2"+
		"\2\30\u0169\3\2\2\2\32\u048d\3\2\2\2\34\u048f\3\2\2\2\36\u0491\3\2\2\2"+
		" \u053b\3\2\2\2\"\u053d\3\2\2\2$\u054e\3\2\2\2&\u0554\3\2\2\2(\u0560\3"+
		"\2\2\2*\u056d\3\2\2\2,\u0570\3\2\2\2.\u0574\3\2\2\2\60\u05b6\3\2\2\2\62"+
		"\u05b8\3\2\2\2\64\u05bc\3\2\2\2\66\u05c8\3\2\2\28\u05cd\3\2\2\2:\u05cf"+
		"\3\2\2\2<\u05d6\3\2\2\2>\u05d8\3\2\2\2@\u05e0\3\2\2\2B\u05e9\3\2\2\2D"+
		"\u05f4\3\2\2\2F\u0606\3\2\2\2H\u0609\3\2\2\2J\u0614\3\2\2\2L\u0624\3\2"+
		"\2\2N\u062a\3\2\2\2P\u062c\3\2\2\2R\u0637\3\2\2\2T\u0648\3\2\2\2V\u064f"+
		"\3\2\2\2X\u0651\3\2\2\2Z\u0657\3\2\2\2\\\u068c\3\2\2\2^\u0698\3\2\2\2"+
		"`\u06c8\3\2\2\2b\u06cb\3\2\2\2d\u06f1\3\2\2\2f\u06f3\3\2\2\2h\u06fb\3"+
		"\2\2\2j\u071c\3\2\2\2l\u074a\3\2\2\2n\u075f\3\2\2\2p\u077f\3\2\2\2r\u078b"+
		"\3\2\2\2t\u078e\3\2\2\2v\u0797\3\2\2\2x\u07a8\3\2\2\2z\u07bc\3\2\2\2|"+
		"\u07be\3\2\2\2~\u07c6\3\2\2\2\u0080\u07ca\3\2\2\2\u0082\u07cd\3\2\2\2"+
		"\u0084\u07d0\3\2\2\2\u0086\u07ea\3\2\2\2\u0088\u07ec\3\2\2\2\u008a\u080c"+
		"\3\2\2\2\u008c\u0835\3\2\2\2\u008e\u0839\3\2\2\2\u0090\u0854\3\2\2\2\u0092"+
		"\u0858\3\2\2\2\u0094\u0867\3\2\2\2\u0096\u0869\3\2\2\2\u0098\u0887\3\2"+
		"\2\2\u009a\u0889\3\2\2\2\u009c\u0890\3\2\2\2\u009e\u08b0\3\2\2\2\u00a0"+
		"\u08b3\3\2\2\2\u00a2\u08cd\3\2\2\2\u00a4\u08e7\3\2\2\2\u00a6\u08ed\3\2"+
		"\2\2\u00a8\u08ef\3\2\2\2\u00aa\u0913\3\2\2\2\u00ac\u0915\3\2\2\2\u00ae"+
		"\u0919\3\2\2\2\u00b0\u0921\3\2\2\2\u00b2\u092c\3\2\2\2\u00b4\u0930\3\2"+
		"\2\2\u00b6\u093b\3\2\2\2\u00b8\u095a\3\2\2\2\u00ba\u095c\3\2\2\2\u00bc"+
		"\u0967\3\2\2\2\u00be\u097d\3\2\2\2\u00c0\u09b0\3\2\2\2\u00c2\u09b2\3\2"+
		"\2\2\u00c4\u09ba\3\2\2\2\u00c6\u09c2\3\2\2\2\u00c8\u09ca\3\2\2\2\u00ca"+
		"\u09d2\3\2\2\2\u00cc\u09d9\3\2\2\2\u00ce\u09dd\3\2\2\2\u00d0\u09e7\3\2"+
		"\2\2\u00d2\u09ef\3\2\2\2\u00d4\u09fc\3\2\2\2\u00d6\u0a0b\3\2\2\2\u00d8"+
		"\u0a0f\3\2\2\2\u00da\u0a11\3\2\2\2\u00dc\u0a13\3\2\2\2\u00de\u0a27\3\2"+
		"\2\2\u00e0\u0a86\3\2\2\2\u00e2\u0a8c\3\2\2\2\u00e4\u0aa6\3\2\2\2\u00e6"+
		"\u0b85\3\2\2\2\u00e8\u0ba0\3\2\2\2\u00ea\u0ba2\3\2\2\2\u00ec\u0ba4\3\2"+
		"\2\2\u00ee\u0ba6\3\2\2\2\u00f0\u0ba8\3\2\2\2\u00f2\u0baa\3\2\2\2\u00f4"+
		"\u0baf\3\2\2\2\u00f6\u0bb6\3\2\2\2\u00f8\u0bba\3\2\2\2\u00fa\u0bbf\3\2"+
		"\2\2\u00fc\u0bc5\3\2\2\2\u00fe\u0bcc\3\2\2\2\u0100\u0bfc\3\2\2\2\u0102"+
		"\u0bfe\3\2\2\2\u0104\u0c06\3\2\2\2\u0106\u0c12\3\2\2\2\u0108\u0c1a\3\2"+
		"\2\2\u010a\u0c23\3\2\2\2\u010c\u0c2b\3\2\2\2\u010e\u0c37\3\2\2\2\u0110"+
		"\u0c3c\3\2\2\2\u0112\u0c45\3\2\2\2\u0114\u0c77\3\2\2\2\u0116\u0c89\3\2"+
		"\2\2\u0118\u0c92\3\2\2\2\u011a\u0c94\3\2\2\2\u011c\u0ca0\3\2\2\2\u011e"+
		"\u0ca2\3\2\2\2\u0120\u0caa\3\2\2\2\u0122\u0cb4\3\2\2\2\u0124\u0cb9\3\2"+
		"\2\2\u0126\u0cc1\3\2\2\2\u0128\u0cc3\3\2\2\2\u012a\u0cf0\3\2\2\2\u012c"+
		"\u0cf9\3\2\2\2\u012e\u0cfb\3\2\2\2\u0130\u0cfd\3\2\2\2\u0132\u0cff\3\2"+
		"\2\2\u0134\u0135\5\4\3\2\u0135\3\3\2\2\2\u0136\u0137\5\6\4\2\u0137\5\3"+
		"\2\2\2\u0138\u0139\5\"\22\2\u0139\u013a\5\n\6\2\u013a\u013b\5D#\2\u013b"+
		"\u0140\5F$\2\u013c\u013e\7\26\2\2\u013d\u013c\3\2\2\2\u013d\u013e\3\2"+
		"\2\2\u013e\u013f\3\2\2\2\u013f\u0141\5.\30\2\u0140\u013d\3\2\2\2\u0140"+
		"\u0141\3\2\2\2\u0141\7\3\2\2\2\u0142\u0143\7\u00bd\2\2\u0143\u0144\7\3"+
		"\2\2\u0144\u0145\5\u00acW\2\u0145\t\3\2\2\2\u0146\u0147\7\6\2\2\u0147"+
		"\u014a\5\u0106\u0084\2\u0148\u0149\7\b\2\2\u0149\u014b\5\b\5\2\u014a\u0148"+
		"\3\2\2\2\u014a\u014b\3\2\2\2\u014b\u014c\3\2\2\2\u014c\u014d\7\7\2\2\u014d"+
		"\u0150\3\2\2\2\u014e\u0150\5\b\5\2\u014f\u0146\3\2\2\2\u014f\u014e\3\2"+
		"\2\2\u0150\13\3\2\2\2\u0151\u0155\5\32\16\2\u0152\u0154\7\5\2\2\u0153"+
		"\u0152\3\2\2\2\u0154\u0157\3\2\2\2\u0155\u0153\3\2\2\2\u0155\u0156\3\2"+
		"\2\2\u0156\u0158\3\2\2\2\u0157\u0155\3\2\2\2\u0158\u0159\7\2\2\3\u0159"+
		"\r\3\2\2\2\u015a\u015b\5\u00ceh\2\u015b\u015c\7\2\2\3\u015c\17\3\2\2\2"+
		"\u015d\u015e\5\u00caf\2\u015e\u015f\7\2\2\3\u015f\21\3\2\2\2\u0160\u0161"+
		"\5\u00c4c\2\u0161\u0162\7\2\2\3\u0162\23\3\2\2\2\u0163\u0164\5\u00ccg"+
		"\2\u0164\u0165\7\2\2\3\u0165\25\3\2\2\2\u0166\u0167\5\u0100\u0081\2\u0167"+
		"\u0168\7\2\2\3\u0168\27\3\2\2\2\u0169\u016a\5\u0106\u0084\2\u016a\u016b"+
		"\7\2\2\3\u016b\31\3\2\2\2\u016c\u048e\5.\30\2\u016d\u016f\5@!\2\u016e"+
		"\u016d\3\2\2\2\u016e\u016f\3\2\2\2\u016f\u0170\3\2\2\2\u0170\u048e\5\\"+
		"/\2\u0171\u0172\7\u0114\2\2\u0172\u048e\5\u00c4c\2\u0173\u0174\7\u0114"+
		"\2\2\u0174\u0175\58\35\2\u0175\u0176\5\u00c4c\2\u0176\u048e\3\2\2\2\u0177"+
		"\u0178\7\u00e3\2\2\u0178\u017b\7#\2\2\u0179\u017c\5\u0124\u0093\2\u017a"+
		"\u017c\7\u0139\2\2\u017b\u0179\3\2\2\2\u017b\u017a\3\2\2\2\u017c\u048e"+
		"\3\2\2\2\u017d\u017e\7\67\2\2\u017e\u0182\58\35\2\u017f\u0180\7t\2\2\u0180"+
		"\u0181\7\u00a2\2\2\u0181\u0183\7Y\2\2\u0182\u017f\3\2\2\2\u0182\u0183"+
		"\3\2\2\2\u0183\u0184\3\2\2\2\u0184\u018c\5\u00c4c\2\u0185\u018b\5,\27"+
		"\2\u0186\u018b\5*\26\2\u0187\u0188\7\u011f\2\2\u0188\u0189\t\2\2\2\u0189"+
		"\u018b\5H%\2\u018a\u0185\3\2\2\2\u018a\u0186\3\2\2\2\u018a\u0187\3\2\2"+
		"\2\u018b\u018e\3\2\2\2\u018c\u018a\3\2\2\2\u018c\u018d\3\2\2\2\u018d\u048e"+
		"\3\2\2\2\u018e\u018c\3\2\2\2\u018f\u0190\7\17\2\2\u0190\u0191\58\35\2"+
		"\u0191\u0192\5\u00c4c\2\u0192\u0193\7\u00e3\2\2\u0193\u0194\t\2\2\2\u0194"+
		"\u0195\5H%\2\u0195\u048e\3\2\2\2\u0196\u0197\7\17\2\2\u0197\u0198\58\35"+
		"\2\u0198\u0199\5\u00c4c\2\u0199\u019a\7\u00e3\2\2\u019a\u019b\5*\26\2"+
		"\u019b\u048e\3\2\2\2\u019c\u019d\7R\2\2\u019d\u01a0\58\35\2\u019e\u019f"+
		"\7t\2\2\u019f\u01a1\7Y\2\2\u01a0\u019e\3\2\2\2\u01a0\u01a1\3\2\2\2\u01a1"+
		"\u01a2\3\2\2\2\u01a2\u01a4\5\u00c4c\2\u01a3\u01a5\t\3\2\2\u01a4\u01a3"+
		"\3\2\2\2\u01a4\u01a5\3\2\2\2\u01a5\u048e\3\2\2\2\u01a6\u01a7\7\u00e6\2"+
		"\2\u01a7\u01aa\5:\36\2\u01a8\u01a9\t\4\2\2\u01a9\u01ab\5\u00c4c\2\u01aa"+
		"\u01a8\3\2\2\2\u01aa\u01ab\3\2\2\2\u01ab\u01b0\3\2\2\2\u01ac\u01ae\7\u008a"+
		"\2\2\u01ad\u01ac\3\2\2\2\u01ad\u01ae\3\2\2\2\u01ae\u01af\3\2\2\2\u01af"+
		"\u01b1\7\u0139\2\2\u01b0\u01ad\3\2\2\2\u01b0\u01b1\3\2\2\2\u01b1\u048e"+
		"\3\2\2\2\u01b2\u01b7\5\"\22\2\u01b3\u01b4\7\6\2\2\u01b4\u01b5\5\u0106"+
		"\u0084\2\u01b5\u01b6\7\7\2\2\u01b6\u01b8\3\2\2\2\u01b7\u01b3\3\2\2\2\u01b7"+
		"\u01b8\3\2\2\2\u01b8\u01ba\3\2\2\2\u01b9\u01bb\5D#\2\u01ba\u01b9\3\2\2"+
		"\2\u01ba\u01bb\3\2\2\2\u01bb\u01bc\3\2\2\2\u01bc\u01c1\5F$\2\u01bd\u01bf"+
		"\7\26\2\2\u01be\u01bd\3\2\2\2\u01be\u01bf\3\2\2\2\u01bf\u01c0\3\2\2\2"+
		"\u01c0\u01c2\5.\30\2\u01c1\u01be\3\2\2\2\u01c1\u01c2\3\2\2\2\u01c2\u048e"+
		"\3\2\2\2\u01c3\u01c4\7\67\2\2\u01c4\u01c8\7\u00f5\2\2\u01c5\u01c6\7t\2"+
		"\2\u01c6\u01c7\7\u00a2\2\2\u01c7\u01c9\7Y\2\2\u01c8\u01c5\3\2\2\2\u01c8"+
		"\u01c9\3\2\2\2\u01c9\u01ca\3\2\2\2\u01ca\u01cb\5\u00caf\2\u01cb\u01cc"+
		"\7\u008a\2\2\u01cc\u01d5\5\u00caf\2\u01cd\u01d4\5D#\2\u01ce\u01d4\5\u00c0"+
		"a\2\u01cf\u01d4\5T+\2\u01d0\u01d4\5*\26\2\u01d1\u01d2\7\u00f8\2\2\u01d2"+
		"\u01d4\5H%\2\u01d3\u01cd\3\2\2\2\u01d3\u01ce\3\2\2\2\u01d3\u01cf\3\2\2"+
		"\2\u01d3\u01d0\3\2\2\2\u01d3\u01d1\3\2\2\2\u01d4\u01d7\3\2\2\2\u01d5\u01d3"+
		"\3\2\2\2\u01d5\u01d6\3\2\2\2\u01d6\u048e\3\2\2\2\u01d7\u01d5\3\2\2\2\u01d8"+
		"\u01dd\5$\23\2\u01d9\u01da\7\6\2\2\u01da\u01db\5\u0106\u0084\2\u01db\u01dc"+
		"\7\7\2\2\u01dc\u01de\3\2\2\2\u01dd\u01d9\3\2\2\2\u01dd\u01de\3\2\2\2\u01de"+
		"\u01e0\3\2\2\2\u01df\u01e1\5D#\2\u01e0\u01df\3\2\2\2\u01e0\u01e1\3\2\2"+
		"\2\u01e1\u01e2\3\2\2\2\u01e2\u01e7\5F$\2\u01e3\u01e5\7\26\2\2\u01e4\u01e3"+
		"\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5\u01e6\3\2\2\2\u01e6\u01e8\5.\30\2\u01e7"+
		"\u01e4\3\2\2\2\u01e7\u01e8\3\2\2\2\u01e8\u048e\3\2\2\2\u01e9\u01ea\7\20"+
		"\2\2\u01ea\u01eb\7\u00f5\2\2\u01eb\u01ed\5\u00c4c\2\u01ec\u01ee\5\64\33"+
		"\2\u01ed\u01ec\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u01ef\3\2\2\2\u01ef\u01f0"+
		"\7\63\2\2\u01f0\u01f8\7\u00ec\2\2\u01f1\u01f9\5\u0124\u0093\2\u01f2\u01f3"+
		"\7f\2\2\u01f3\u01f4\7.\2\2\u01f4\u01f9\5\u00aeX\2\u01f5\u01f6\7f\2\2\u01f6"+
		"\u01f7\7\16\2\2\u01f7\u01f9\7.\2\2\u01f8\u01f1\3\2\2\2\u01f8\u01f2\3\2"+
		"\2\2\u01f8\u01f5\3\2\2\2\u01f8\u01f9\3\2\2\2\u01f9\u048e\3\2\2\2\u01fa"+
		"\u01fb\7\20\2\2\u01fb\u01fe\7\u00f6\2\2\u01fc\u01fd\t\4\2\2\u01fd\u01ff"+
		"\5\u00c4c\2\u01fe\u01fc\3\2\2\2\u01fe\u01ff\3\2\2\2\u01ff\u0200\3\2\2"+
		"\2\u0200\u0201\7\63\2\2\u0201\u0203\7\u00ec\2\2\u0202\u0204\5\u0124\u0093"+
		"\2\u0203\u0202\3\2\2\2\u0203\u0204\3\2\2\2\u0204\u048e\3\2\2\2\u0205\u0206"+
		"\7\17\2\2\u0206\u0207\7\u00f5\2\2\u0207\u0208\5\u00c4c\2\u0208\u0209\7"+
		"\f\2\2\u0209\u020a\t\5\2\2\u020a\u020b\5\u0102\u0082\2\u020b\u048e\3\2"+
		"\2\2\u020c\u020d\7\17\2\2\u020d\u020e\7\u00f5\2\2\u020e\u020f\5\u00c4"+
		"c\2\u020f\u0210\7\f\2\2\u0210\u0211\t\5\2\2\u0211\u0212\7\6\2\2\u0212"+
		"\u0213\5\u0102\u0082\2\u0213\u0214\7\7\2\2\u0214\u048e\3\2\2\2\u0215\u0216"+
		"\7\17\2\2\u0216\u0217\7\u00f5\2\2\u0217\u0218\5\u00c4c\2\u0218\u0219\7"+
		"\u00ca\2\2\u0219\u021a\7-\2\2\u021a\u021b\5\u00c4c\2\u021b\u021c\7\u0100"+
		"\2\2\u021c\u021d\5\u0120\u0091\2\u021d\u048e\3\2\2\2\u021e\u021f\7\17"+
		"\2\2\u021f\u0220\7\u00f5\2\2\u0220\u0221\5\u00c4c\2\u0221\u0222\7R\2\2"+
		"\u0222\u0225\t\5\2\2\u0223\u0224\7t\2\2\u0224\u0226\7Y\2\2\u0225\u0223"+
		"\3\2\2\2\u0225\u0226\3\2\2\2\u0226\u0227\3\2\2\2\u0227\u0228\7\6\2\2\u0228"+
		"\u0229\5\u00c2b\2\u0229\u022a\7\7\2\2\u022a\u048e\3\2\2\2\u022b\u022c"+
		"\7\17\2\2\u022c\u022d\7\u00f5\2\2\u022d\u022e\5\u00c4c\2\u022e\u022f\7"+
		"R\2\2\u022f\u0232\t\5\2\2\u0230\u0231\7t\2\2\u0231\u0233\7Y\2\2\u0232"+
		"\u0230\3\2\2\2\u0232\u0233\3\2\2\2\u0233\u0234\3\2\2\2\u0234\u0235\5\u00c2"+
		"b\2\u0235\u048e\3\2\2\2\u0236\u0237\7\17\2\2\u0237\u0238\t\6\2\2\u0238"+
		"\u0239\5\u00c4c\2\u0239\u023a\7\u00ca\2\2\u023a\u023b\7\u0100\2\2\u023b"+
		"\u023c\5\u00c4c\2\u023c\u048e\3\2\2\2\u023d\u023e\7\17\2\2\u023e\u023f"+
		"\t\6\2\2\u023f\u0240\5\u00c4c\2\u0240\u0241\7\u00e3\2\2\u0241\u0242\7"+
		"\u00f8\2\2\u0242\u0243\5H%\2\u0243\u048e\3\2\2\2\u0244\u0245\7\17\2\2"+
		"\u0245\u0246\t\6\2\2\u0246\u0247\5\u00c4c\2\u0247\u0248\7\u0112\2\2\u0248"+
		"\u024b\7\u00f8\2\2\u0249\u024a\7t\2\2\u024a\u024c\7Y\2\2\u024b\u0249\3"+
		"\2\2\2\u024b\u024c\3\2\2\2\u024c\u024d\3\2\2\2\u024d\u024e\5H%\2\u024e"+
		"\u048e\3\2\2\2\u024f\u0250\7\17\2\2\u0250\u0251\7\u00f5\2\2\u0251\u0252"+
		"\5\u00c4c\2\u0252\u0254\t\7\2\2\u0253\u0255\7-\2\2\u0254\u0253\3\2\2\2"+
		"\u0254\u0255\3\2\2\2\u0255\u0256\3\2\2\2\u0256\u0258\5\u00c4c\2\u0257"+
		"\u0259\5\u012c\u0097\2\u0258\u0257\3\2\2\2\u0258\u0259\3\2\2\2\u0259\u048e"+
		"\3\2\2\2\u025a\u025b\7\17\2\2\u025b\u025c\7\u00f5\2\2\u025c\u025e\5\u00c4"+
		"c\2\u025d\u025f\5\64\33\2\u025e\u025d\3\2\2\2\u025e\u025f\3\2\2\2\u025f"+
		"\u0260\3\2\2\2\u0260\u0262\7%\2\2\u0261\u0263\7-\2\2\u0262\u0261\3\2\2"+
		"\2\u0262\u0263\3\2\2\2\u0263\u0264\3\2\2\2\u0264\u0265\5\u00c4c\2\u0265"+
		"\u0267\5\u0108\u0085\2\u0266\u0268\5\u00fe\u0080\2\u0267\u0266\3\2\2\2"+
		"\u0267\u0268\3\2\2\2\u0268\u048e\3\2\2\2\u0269\u026a\7\17\2\2\u026a\u026b"+
		"\7\u00f5\2\2\u026b\u026d\5\u00c4c\2\u026c\u026e\5\64\33\2\u026d\u026c"+
		"\3\2\2\2\u026d\u026e\3\2\2\2\u026e\u026f\3\2\2\2\u026f\u0270\7\u00cd\2"+
		"\2\u0270\u0271\7.\2\2\u0271\u0272\7\6\2\2\u0272\u0273\5\u0102\u0082\2"+
		"\u0273\u0274\7\7\2\2\u0274\u048e\3\2\2\2\u0275\u0276\7\17\2\2\u0276\u0277"+
		"\7\u00f5\2\2\u0277\u0279\5\u00c4c\2\u0278\u027a\5\64\33\2\u0279\u0278"+
		"\3\2\2\2\u0279\u027a\3\2\2\2\u027a\u027b\3\2\2\2\u027b\u027c\7\u00e3\2"+
		"\2\u027c\u027d\7\u00e0\2\2\u027d\u0281\7\u0139\2\2\u027e\u027f\7\u011f"+
		"\2\2\u027f\u0280\7\u00e1\2\2\u0280\u0282\5H%\2\u0281\u027e\3\2\2\2\u0281"+
		"\u0282\3\2\2\2\u0282\u048e\3\2\2\2\u0283\u0284\7\17\2\2\u0284\u0285\7"+
		"\u00f5\2\2\u0285\u0287\5\u00c4c\2\u0286\u0288\5\64\33\2\u0287\u0286\3"+
		"\2\2\2\u0287\u0288\3\2\2\2\u0288\u0289\3\2\2\2\u0289\u028a\7\u00e3\2\2"+
		"\u028a\u028b\7\u00e1\2\2\u028b\u028c\5H%\2\u028c\u048e\3\2\2\2\u028d\u028e"+
		"\7\17\2\2\u028e\u028f\t\6\2\2\u028f\u0290\5\u00c4c\2\u0290\u0294\7\f\2"+
		"\2\u0291\u0292\7t\2\2\u0292\u0293\7\u00a2\2\2\u0293\u0295\7Y\2\2\u0294"+
		"\u0291\3\2\2\2\u0294\u0295\3\2\2\2\u0295\u0297\3\2\2\2\u0296\u0298\5\62"+
		"\32\2\u0297\u0296\3\2\2\2\u0298\u0299\3\2\2\2\u0299\u0297\3\2\2\2\u0299"+
		"\u029a\3\2\2\2\u029a\u048e\3\2\2\2\u029b\u029c\7\17\2\2\u029c\u029d\7"+
		"\u00f5\2\2\u029d\u029e\5\u00c4c\2\u029e\u029f\5\64\33\2\u029f\u02a0\7"+
		"\u00ca\2\2\u02a0\u02a1\7\u0100\2\2\u02a1\u02a2\5\64\33\2\u02a2\u048e\3"+
		"\2\2\2\u02a3\u02a4\7\17\2\2\u02a4\u02a5\t\6\2\2\u02a5\u02a6\5\u00c4c\2"+
		"\u02a6\u02a9\7R\2\2\u02a7\u02a8\7t\2\2\u02a8\u02aa\7Y\2\2\u02a9\u02a7"+
		"\3\2\2\2\u02a9\u02aa\3\2\2\2\u02aa\u02ab\3\2\2\2\u02ab\u02b0\5\64\33\2"+
		"\u02ac\u02ad\7\b\2\2\u02ad\u02af\5\64\33\2\u02ae\u02ac\3\2\2\2\u02af\u02b2"+
		"\3\2\2\2\u02b0\u02ae\3\2\2\2\u02b0\u02b1\3\2\2\2\u02b1\u02b4\3\2\2\2\u02b2"+
		"\u02b0\3\2\2\2\u02b3\u02b5\7\u00c0\2\2\u02b4\u02b3\3\2\2\2\u02b4\u02b5"+
		"\3\2\2\2\u02b5\u048e\3\2\2\2\u02b6\u02b7\7\17\2\2\u02b7\u02b8\7\u00f5"+
		"\2\2\u02b8\u02ba\5\u00c4c\2\u02b9\u02bb\5\64\33\2\u02ba\u02b9\3\2\2\2"+
		"\u02ba\u02bb\3\2\2\2\u02bb\u02bc\3\2\2\2\u02bc\u02bd\7\u00e3\2\2\u02bd"+
		"\u02be\5*\26\2\u02be\u048e\3\2\2\2\u02bf\u02c0\7\17\2\2\u02c0\u02c1\7"+
		"\u00f5\2\2\u02c1\u02c2\5\u00c4c\2\u02c2\u02c3\7\u00c6\2\2\u02c3\u02c4"+
		"\7\u00b5\2\2\u02c4\u048e\3\2\2\2\u02c5\u02c6\7R\2\2\u02c6\u02c9\7\u00f5"+
		"\2\2\u02c7\u02c8\7t\2\2\u02c8\u02ca\7Y\2\2\u02c9\u02c7\3\2\2\2\u02c9\u02ca"+
		"\3\2\2\2\u02ca\u02cb\3\2\2\2\u02cb\u02cd\5\u00c4c\2\u02cc\u02ce\7\u00c0"+
		"\2\2\u02cd\u02cc\3\2\2\2\u02cd\u02ce\3\2\2\2\u02ce\u048e\3\2\2\2\u02cf"+
		"\u02d0\7R\2\2\u02d0\u02d3\7\u0119\2\2\u02d1\u02d2\7t\2\2\u02d2\u02d4\7"+
		"Y\2\2\u02d3\u02d1\3\2\2\2\u02d3\u02d4\3\2\2\2\u02d4\u02d5\3\2\2\2\u02d5"+
		"\u048e\5\u00c4c\2\u02d6\u02d9\7\67\2\2\u02d7\u02d8\7\u00aa\2\2\u02d8\u02da"+
		"\7\u00cd\2\2\u02d9\u02d7\3\2\2\2\u02d9\u02da\3\2\2\2\u02da\u02df\3\2\2"+
		"\2\u02db\u02dd\7n\2\2\u02dc\u02db\3\2\2\2\u02dc\u02dd\3\2\2\2\u02dd\u02de"+
		"\3\2\2\2\u02de\u02e0\7\u00f9\2\2\u02df\u02dc\3\2\2\2\u02df\u02e0\3\2\2"+
		"\2\u02e0\u02e1\3\2\2\2\u02e1\u02e5\7\u0119\2\2\u02e2\u02e3\7t\2\2\u02e3"+
		"\u02e4\7\u00a2\2\2\u02e4\u02e6\7Y\2\2\u02e5\u02e2\3\2\2\2\u02e5\u02e6"+
		"\3\2\2\2\u02e6\u02e7\3\2\2\2\u02e7\u02e9\5\u00c4c\2\u02e8\u02ea\5\u00b4"+
		"[\2\u02e9\u02e8\3\2\2\2\u02e9\u02ea\3\2\2\2\u02ea\u02f3\3\2\2\2\u02eb"+
		"\u02f2\5,\27\2\u02ec\u02ed\7\u00b4\2\2\u02ed\u02ee\7\u00a6\2\2\u02ee\u02f2"+
		"\5\u00acW\2\u02ef\u02f0\7\u00f8\2\2\u02f0\u02f2\5H%\2\u02f1\u02eb\3\2"+
		"\2\2\u02f1\u02ec\3\2\2\2\u02f1\u02ef\3\2\2\2\u02f2\u02f5\3\2\2\2\u02f3"+
		"\u02f1\3\2\2\2\u02f3\u02f4\3\2\2\2\u02f4\u02f6\3\2\2\2\u02f5\u02f3\3\2"+
		"\2\2\u02f6\u02f7\7\26\2\2\u02f7\u02f8\5.\30\2\u02f8\u048e\3\2\2\2\u02f9"+
		"\u02fc\7\67\2\2\u02fa\u02fb\7\u00aa\2\2\u02fb\u02fd\7\u00cd\2\2\u02fc"+
		"\u02fa\3\2\2\2\u02fc\u02fd\3\2\2\2\u02fd\u02ff\3\2\2\2\u02fe\u0300\7n"+
		"\2\2\u02ff\u02fe\3\2\2\2\u02ff\u0300\3\2\2\2\u0300\u0301\3\2\2\2\u0301"+
		"\u0302\7\u00f9\2\2\u0302\u0303\7\u0119\2\2\u0303\u0308\5\u00caf\2\u0304"+
		"\u0305\7\6\2\2\u0305\u0306\5\u0106\u0084\2\u0306\u0307\7\7\2\2\u0307\u0309"+
		"\3\2\2\2\u0308\u0304\3\2\2\2\u0308\u0309\3\2\2\2\u0309\u030a\3\2\2\2\u030a"+
		"\u030d\5D#\2\u030b\u030c\7\u00a9\2\2\u030c\u030e\5H%\2\u030d\u030b\3\2"+
		"\2\2\u030d\u030e\3\2\2\2\u030e\u048e\3\2\2\2\u030f\u0310\7\17\2\2\u0310"+
		"\u0311\7\u0119\2\2\u0311\u0313\5\u00c4c\2\u0312\u0314\7\26\2\2\u0313\u0312"+
		"\3\2\2\2\u0313\u0314\3\2\2\2\u0314\u0315\3\2\2\2\u0315\u0316\5.\30\2\u0316"+
		"\u048e\3\2\2\2\u0317\u031a\7\67\2\2\u0318\u0319\7\u00aa\2\2\u0319\u031b"+
		"\7\u00cd\2\2\u031a\u0318\3\2\2\2\u031a\u031b\3\2\2\2\u031b\u031d\3\2\2"+
		"\2\u031c\u031e\7\u00f9\2\2\u031d\u031c\3\2\2\2\u031d\u031e\3\2\2\2\u031e"+
		"\u031f\3\2\2\2\u031f\u0323\7l\2\2\u0320\u0321\7t\2\2\u0321\u0322\7\u00a2"+
		"\2\2\u0322\u0324\7Y\2\2\u0323\u0320\3\2\2\2\u0323\u0324\3\2\2\2\u0324"+
		"\u0325\3\2\2\2\u0325\u0326\5\u00c4c\2\u0326\u0327\7\26\2\2\u0327\u0331"+
		"\7\u0139\2\2\u0328\u0329\7\u0116\2\2\u0329\u032e\5Z.\2\u032a\u032b\7\b"+
		"\2\2\u032b\u032d\5Z.\2\u032c\u032a\3\2\2\2\u032d\u0330\3\2\2\2\u032e\u032c"+
		"\3\2\2\2\u032e\u032f\3\2\2\2\u032f\u0332\3\2\2\2\u0330\u032e\3\2\2\2\u0331"+
		"\u0328\3\2\2\2\u0331\u0332\3\2\2\2\u0332\u048e\3\2\2\2\u0333\u0335\7R"+
		"\2\2\u0334\u0336\7\u00f9\2\2\u0335\u0334\3\2\2\2\u0335\u0336\3\2\2\2\u0336"+
		"\u0337\3\2\2\2\u0337\u033a\7l\2\2\u0338\u0339\7t\2\2\u0339\u033b\7Y\2"+
		"\2\u033a\u0338\3\2\2\2\u033a\u033b\3\2\2\2\u033b\u033c\3\2\2\2\u033c\u048e"+
		"\5\u00c4c\2\u033d\u033f\7Z\2\2\u033e\u0340\t\b\2\2\u033f\u033e\3\2\2\2"+
		"\u033f\u0340\3\2\2\2\u0340\u0341\3\2\2\2\u0341\u048e\5\32\16\2\u0342\u0343"+
		"\7\u00e6\2\2\u0343\u0346\7\u00f6\2\2\u0344\u0345\t\4\2\2\u0345\u0347\5"+
		"\u00c4c\2\u0346\u0344\3\2\2\2\u0346\u0347\3\2\2\2\u0347\u034c\3\2\2\2"+
		"\u0348\u034a\7\u008a\2\2\u0349\u0348\3\2\2\2\u0349\u034a\3\2\2\2\u034a"+
		"\u034b\3\2\2\2\u034b\u034d\7\u0139\2\2\u034c\u0349\3\2\2\2\u034c\u034d"+
		"\3\2\2\2\u034d\u048e\3\2\2\2\u034e\u034f\7\u00e6\2\2\u034f\u0350\7\u00f5"+
		"\2\2\u0350\u0353\7\\\2\2\u0351\u0352\t\4\2\2\u0352\u0354\5\u00c4c\2\u0353"+
		"\u0351\3\2\2\2\u0353\u0354\3\2\2\2\u0354\u0355\3\2\2\2\u0355\u0356\7\u008a"+
		"\2\2\u0356\u0358\7\u0139\2\2\u0357\u0359\5\64\33\2\u0358\u0357\3\2\2\2"+
		"\u0358\u0359\3\2\2\2\u0359\u048e\3\2\2\2\u035a\u035b\7\u00e6\2\2\u035b"+
		"\u035c\7\u00f8\2\2\u035c\u0361\5\u00c4c\2\u035d\u035e\7\6\2\2\u035e\u035f"+
		"\5L\'\2\u035f\u0360\7\7\2\2\u0360\u0362\3\2\2\2\u0361\u035d\3\2\2\2\u0361"+
		"\u0362\3\2\2\2\u0362\u048e\3\2\2\2\u0363\u0364\7\u00e6\2\2\u0364\u0365"+
		"\7.\2\2\u0365\u0366\t\4\2\2\u0366\u0369\5\u00c4c\2\u0367\u0368\t\4\2\2"+
		"\u0368\u036a\5\u00c4c\2\u0369\u0367\3\2\2\2\u0369\u036a\3\2\2\2\u036a"+
		"\u048e\3\2\2\2\u036b\u036c\7\u00e6\2\2\u036c\u036f\7\u011a\2\2\u036d\u036e"+
		"\t\4\2\2\u036e\u0370\5\u00c4c\2\u036f\u036d\3\2\2\2\u036f\u0370\3\2\2"+
		"\2\u0370\u0375\3\2\2\2\u0371\u0373\7\u008a\2\2\u0372\u0371\3\2\2\2\u0372"+
		"\u0373\3\2\2\2\u0373\u0374\3\2\2\2\u0374\u0376\7\u0139\2\2\u0375\u0372"+
		"\3\2\2\2\u0375\u0376\3\2\2\2\u0376\u048e\3\2\2\2\u0377\u0378\7\u00e6\2"+
		"\2\u0378\u0379\7\u00b5\2\2\u0379\u037b\5\u00c4c\2\u037a\u037c\5\64\33"+
		"\2\u037b\u037a\3\2\2\2\u037b\u037c\3\2\2\2\u037c\u048e\3\2\2\2\u037d\u037f"+
		"\7\u00e6\2\2\u037e\u0380\5\u0124\u0093\2\u037f\u037e\3\2\2\2\u037f\u0380"+
		"\3\2\2\2\u0380\u0381\3\2\2\2\u0381\u0384\7m\2\2\u0382\u0383\t\4\2\2\u0383"+
		"\u0385\5\u00c4c\2\u0384\u0382\3\2\2\2\u0384\u0385\3\2\2\2\u0385\u038d"+
		"\3\2\2\2\u0386\u0388\7\u008a\2\2\u0387\u0386\3\2\2\2\u0387\u0388\3\2\2"+
		"\2\u0388\u038b\3\2\2\2\u0389\u038c\5\u00c4c\2\u038a\u038c\7\u0139\2\2"+
		"\u038b\u0389\3\2\2\2\u038b\u038a\3\2\2\2\u038c\u038e\3\2\2\2\u038d\u0387"+
		"\3\2\2\2\u038d\u038e\3\2\2\2\u038e\u048e\3\2\2\2\u038f\u0390\7\u00e6\2"+
		"\2\u0390\u0391\7\67\2\2\u0391\u0392\7\u00f5\2\2\u0392\u0395\5\u00c4c\2"+
		"\u0393\u0394\7\26\2\2\u0394\u0396\7\u00e0\2\2\u0395\u0393\3\2\2\2\u0395"+
		"\u0396\3\2\2\2\u0396\u048e\3\2\2\2\u0397\u0398\7\u00e6\2\2\u0398\u0399"+
		"\7:\2\2\u0399\u048e\58\35\2\u039a\u039b\7\u00e6\2\2\u039b\u03a0\7$\2\2"+
		"\u039c\u039e\7\u008a\2\2\u039d\u039c\3\2\2\2\u039d\u039e\3\2\2\2\u039e"+
		"\u039f\3\2\2\2\u039f\u03a1\7\u0139\2\2\u03a0\u039d\3\2\2\2\u03a0\u03a1"+
		"\3\2\2\2\u03a1\u048e\3\2\2\2\u03a2\u03a3\t\t\2\2\u03a3\u03a5\7l\2\2\u03a4"+
		"\u03a6\7\\\2\2\u03a5\u03a4\3\2\2\2\u03a5\u03a6\3\2\2\2\u03a6\u03a7\3\2"+
		"\2\2\u03a7\u048e\5<\37\2\u03a8\u03a9\t\t\2\2\u03a9\u03ab\58\35\2\u03aa"+
		"\u03ac\7\\\2\2\u03ab\u03aa\3\2\2\2\u03ab\u03ac\3\2\2\2\u03ac\u03ad\3\2"+
		"\2\2\u03ad\u03ae\5\u00c4c\2\u03ae\u048e\3\2\2\2\u03af\u03b1\t\t\2\2\u03b0"+
		"\u03b2\7\u00f5\2\2\u03b1\u03b0\3\2\2\2\u03b1\u03b2\3\2\2\2\u03b2\u03b4"+
		"\3\2\2\2\u03b3\u03b5\t\n\2\2\u03b4\u03b3\3\2\2\2\u03b4\u03b5\3\2\2\2\u03b5"+
		"\u03b6\3\2\2\2\u03b6\u03b8\5\u00c4c\2\u03b7\u03b9\5\64\33\2\u03b8\u03b7"+
		"\3\2\2\2\u03b8\u03b9\3\2\2\2\u03b9\u03bb\3\2\2\2\u03ba\u03bc\5> \2\u03bb"+
		"\u03ba\3\2\2\2\u03bb\u03bc\3\2\2\2\u03bc\u048e\3\2\2\2\u03bd\u03bf\t\t"+
		"\2\2\u03be\u03c0\7\u00c2\2\2\u03bf\u03be\3\2\2\2\u03bf\u03c0\3\2\2\2\u03c0"+
		"\u03c1\3\2\2\2\u03c1\u048e\5.\30\2\u03c2\u03c3\7/\2\2\u03c3\u03c4\7\u00a6"+
		"\2\2\u03c4\u03c5\58\35\2\u03c5\u03c6\5\u00c4c\2\u03c6\u03c7\7\u0081\2"+
		"\2\u03c7\u03c8\t\13\2\2\u03c8\u048e\3\2\2\2\u03c9\u03ca\7/\2\2\u03ca\u03cb"+
		"\7\u00a6\2\2\u03cb\u03cc\7\u00f5\2\2\u03cc\u03cd\5\u00c4c\2\u03cd\u03ce"+
		"\7\u0081\2\2\u03ce\u03cf\t\13\2\2\u03cf\u048e\3\2\2\2\u03d0\u03d1\7\u00c9"+
		"\2\2\u03d1\u03d2\7\u00f5\2\2\u03d2\u048e\5\u00c4c\2\u03d3\u03d4\7\u00c9"+
		"\2\2\u03d4\u03d5\7l\2\2\u03d5\u048e\5\u00c4c\2\u03d6\u03de\7\u00c9\2\2"+
		"\u03d7\u03df\7\u0139\2\2\u03d8\u03da\13\2\2\2\u03d9\u03d8\3\2\2\2\u03da"+
		"\u03dd\3\2\2\2\u03db\u03dc\3\2\2\2\u03db\u03d9\3\2\2\2\u03dc\u03df\3\2"+
		"\2\2\u03dd\u03db\3\2\2\2\u03de\u03d7\3\2\2\2\u03de\u03db\3\2\2\2\u03df"+
		"\u048e\3\2\2\2\u03e0\u03e2\7\37\2\2\u03e1\u03e3\7\u0087\2\2\u03e2\u03e1"+
		"\3\2\2\2\u03e2\u03e3\3\2\2\2\u03e3\u03e4\3\2\2\2\u03e4\u03e5\7\u00f5\2"+
		"\2\u03e5\u03e8\5\u00c4c\2\u03e6\u03e7\7\u00a9\2\2\u03e7\u03e9\5H%\2\u03e8"+
		"\u03e6\3\2\2\2\u03e8\u03e9\3\2\2\2\u03e9\u03ee\3\2\2\2\u03ea\u03ec\7\26"+
		"\2\2\u03eb\u03ea\3\2\2\2\u03eb\u03ec\3\2\2\2\u03ec\u03ed\3\2\2\2\u03ed"+
		"\u03ef\5.\30\2\u03ee\u03eb\3\2\2\2\u03ee\u03ef\3\2\2\2\u03ef\u048e\3\2"+
		"\2\2\u03f0\u03f1\7\u010d\2\2\u03f1\u03f4\7\u00f5\2\2\u03f2\u03f3\7t\2"+
		"\2\u03f3\u03f5\7Y\2\2\u03f4\u03f2\3\2\2\2\u03f4\u03f5\3\2\2\2\u03f5\u03f6"+
		"\3\2\2\2\u03f6\u048e\5\u00c4c\2\u03f7\u03f8\7\'\2\2\u03f8\u048e\7\37\2"+
		"\2\u03f9\u03fa\7\u008f\2\2\u03fa\u03fc\7A\2\2\u03fb\u03fd\7\u0090\2\2"+
		"\u03fc\u03fb\3\2\2\2\u03fc\u03fd\3\2\2\2\u03fd\u03fe\3\2\2\2\u03fe\u03ff"+
		"\7{\2\2\u03ff\u0401\7\u0139\2\2\u0400\u0402\7\u00b2\2\2\u0401\u0400\3"+
		"\2\2\2\u0401\u0402\3\2\2\2\u0402\u0403\3\2\2\2\u0403\u0404\7\u0080\2\2"+
		"\u0404\u0405\7\u00f5\2\2\u0405\u0407\5\u00c4c\2\u0406\u0408\5\64\33\2"+
		"\u0407\u0406\3\2\2\2\u0407\u0408\3\2\2\2\u0408\u048e\3\2\2\2\u0409\u040a"+
		"\7\u0108\2\2\u040a\u040b\7\u00f5\2\2\u040b\u040d\5\u00c4c\2\u040c\u040e"+
		"\5\64\33\2\u040d\u040c\3\2\2\2\u040d\u040e\3\2\2\2\u040e\u048e\3\2\2\2"+
		"\u040f\u0410\7\u009d\2\2\u0410\u0411\7\u00cb\2\2\u0411\u0412\7\u00f5\2"+
		"\2\u0412\u0415\5\u00c4c\2\u0413\u0414\t\f\2\2\u0414\u0416\7\u00b5\2\2"+
		"\u0415\u0413\3\2\2\2\u0415\u0416\3\2\2\2\u0416\u048e\3\2\2\2\u0417\u0418"+
		"\t\r\2\2\u0418\u041c\5\u0124\u0093\2\u0419\u041b\13\2\2\2\u041a\u0419"+
		"\3\2\2\2\u041b\u041e\3\2\2\2\u041c\u041d\3\2\2\2\u041c\u041a\3\2\2\2\u041d"+
		"\u048e\3\2\2\2\u041e\u041c\3\2\2\2\u041f\u0420\7\u00e3\2\2\u0420\u0424"+
		"\7\u00d4\2\2\u0421\u0423\13\2\2\2\u0422\u0421\3\2\2\2\u0423\u0426\3\2"+
		"\2\2\u0424\u0425\3\2\2\2\u0424\u0422\3\2\2\2\u0425\u048e\3\2\2\2\u0426"+
		"\u0424\3\2\2\2\u0427\u0428\7\u00e3\2\2\u0428\u0429\7\u00fc\2\2\u0429\u042a"+
		"\7\u0122\2\2\u042a\u048e\5\u00f2z\2\u042b\u042c\7\u00e3\2\2\u042c\u042d"+
		"\7\u00fc\2\2\u042d\u042e\7\u0122\2\2\u042e\u048e\t\16\2\2\u042f\u0430"+
		"\7\u00e3\2\2\u0430\u0431\7\u00fc\2\2\u0431\u0435\7\u0122\2\2\u0432\u0434"+
		"\13\2\2\2\u0433\u0432\3\2\2\2\u0434\u0437\3\2\2\2\u0435\u0436\3\2\2\2"+
		"\u0435\u0433\3\2\2\2\u0436\u048e\3\2\2\2\u0437\u0435\3\2\2\2\u0438\u0439"+
		"\7\u00e3\2\2\u0439\u043a\5\34\17\2\u043a\u043b\7\u0123\2\2\u043b\u043c"+
		"\5\36\20\2\u043c\u048e\3\2\2\2\u043d\u043e\7\u00e3\2\2\u043e\u0446\5\34"+
		"\17\2\u043f\u0443\7\u0123\2\2\u0440\u0442\13\2\2\2\u0441\u0440\3\2\2\2"+
		"\u0442\u0445\3\2\2\2\u0443\u0444\3\2\2\2\u0443\u0441\3\2\2\2\u0444\u0447"+
		"\3\2\2\2\u0445\u0443\3\2\2\2\u0446\u043f\3\2\2\2\u0446\u0447\3\2\2\2\u0447"+
		"\u048e\3\2\2\2\u0448\u044c\7\u00e3\2\2\u0449\u044b\13\2\2\2\u044a\u0449"+
		"\3\2\2\2\u044b\u044e\3\2\2\2\u044c\u044d\3\2\2\2\u044c\u044a\3\2\2\2\u044d"+
		"\u044f\3\2\2\2\u044e\u044c\3\2\2\2\u044f\u0450\7\u0123\2\2\u0450\u048e"+
		"\5\36\20\2\u0451\u0455\7\u00e3\2\2\u0452\u0454\13\2\2\2\u0453\u0452\3"+
		"\2\2\2\u0454\u0457\3\2\2\2\u0455\u0456\3\2\2\2\u0455\u0453\3\2\2\2\u0456"+
		"\u048e\3\2\2\2\u0457\u0455\3\2\2\2\u0458\u0459\7\u00ce\2\2\u0459\u048e"+
		"\5\34\17\2\u045a\u045e\7\u00ce\2\2\u045b\u045d\13\2\2\2\u045c\u045b\3"+
		"\2\2\2\u045d\u0460\3\2\2\2\u045e\u045f\3\2\2\2\u045e\u045c\3\2\2\2\u045f"+
		"\u048e\3\2\2\2\u0460\u045e\3\2\2\2\u0461\u0462\7\67\2\2\u0462\u0466\7"+
		"x\2\2\u0463\u0464\7t\2\2\u0464\u0465\7\u00a2\2\2\u0465\u0467\7Y\2\2\u0466"+
		"\u0463\3\2\2\2\u0466\u0467\3\2\2\2\u0467\u0468\3\2\2\2\u0468\u0469\5\u0124"+
		"\u0093\2\u0469\u046b\7\u00a6\2\2\u046a\u046c\7\u00f5\2\2\u046b\u046a\3"+
		"\2\2\2\u046b\u046c\3\2\2\2\u046c\u046d\3\2\2\2\u046d\u0470\5\u00c4c\2"+
		"\u046e\u046f\7\u0116\2\2\u046f\u0471\5\u0124\u0093\2\u0470\u046e\3\2\2"+
		"\2\u0470\u0471\3\2\2\2\u0471\u0472\3\2\2\2\u0472\u0473\7\6\2\2\u0473\u0474"+
		"\5\u00c6d\2\u0474\u0477\7\7\2\2\u0475\u0476\7\u00a9\2\2\u0476\u0478\5"+
		"H%\2\u0477\u0475\3\2\2\2\u0477\u0478\3\2\2\2\u0478\u048e\3\2\2\2\u0479"+
		"\u047a\7R\2\2\u047a\u047d\7x\2\2\u047b\u047c\7t\2\2\u047c\u047e\7Y\2\2"+
		"\u047d\u047b\3\2\2\2\u047d\u047e\3\2\2\2\u047e\u047f\3\2\2\2\u047f\u0480"+
		"\5\u0124\u0093\2\u0480\u0482\7\u00a6\2\2\u0481\u0483\7\u00f5\2\2\u0482"+
		"\u0481\3\2\2\2\u0482\u0483\3\2\2\2\u0483\u0484\3\2\2\2\u0484\u0485\5\u00c4"+
		"c\2\u0485\u048e\3\2\2\2\u0486\u048a\5 \21\2\u0487\u0489\13\2\2\2\u0488"+
		"\u0487\3\2\2\2\u0489\u048c\3\2\2\2\u048a\u048b\3\2\2\2\u048a\u0488\3\2"+
		"\2\2\u048b\u048e\3\2\2\2\u048c\u048a\3\2\2\2\u048d\u016c\3\2\2\2\u048d"+
		"\u016e\3\2\2\2\u048d\u0171\3\2\2\2\u048d\u0173\3\2\2\2\u048d\u0177\3\2"+
		"\2\2\u048d\u017d\3\2\2\2\u048d\u018f\3\2\2\2\u048d\u0196\3\2\2\2\u048d"+
		"\u019c\3\2\2\2\u048d\u01a6\3\2\2\2\u048d\u01b2\3\2\2\2\u048d\u01c3\3\2"+
		"\2\2\u048d\u01d8\3\2\2\2\u048d\u01e9\3\2\2\2\u048d\u01fa\3\2\2\2\u048d"+
		"\u0205\3\2\2\2\u048d\u020c\3\2\2\2\u048d\u0215\3\2\2\2\u048d\u021e\3\2"+
		"\2\2\u048d\u022b\3\2\2\2\u048d\u0236\3\2\2\2\u048d\u023d\3\2\2\2\u048d"+
		"\u0244\3\2\2\2\u048d\u024f\3\2\2\2\u048d\u025a\3\2\2\2\u048d\u0269\3\2"+
		"\2\2\u048d\u0275\3\2\2\2\u048d\u0283\3\2\2\2\u048d\u028d\3\2\2\2\u048d"+
		"\u029b\3\2\2\2\u048d\u02a3\3\2\2\2\u048d\u02b6\3\2\2\2\u048d\u02bf\3\2"+
		"\2\2\u048d\u02c5\3\2\2\2\u048d\u02cf\3\2\2\2\u048d\u02d6\3\2\2\2\u048d"+
		"\u02f9\3\2\2\2\u048d\u030f\3\2\2\2\u048d\u0317\3\2\2\2\u048d\u0333\3\2"+
		"\2\2\u048d\u033d\3\2\2\2\u048d\u0342\3\2\2\2\u048d\u034e\3\2\2\2\u048d"+
		"\u035a\3\2\2\2\u048d\u0363\3\2\2\2\u048d\u036b\3\2\2\2\u048d\u0377\3\2"+
		"\2\2\u048d\u037d\3\2\2\2\u048d\u038f\3\2\2\2\u048d\u0397\3\2\2\2\u048d"+
		"\u039a\3\2\2\2\u048d\u03a2\3\2\2\2\u048d\u03a8\3\2\2\2\u048d\u03af\3\2"+
		"\2\2\u048d\u03bd\3\2\2\2\u048d\u03c2\3\2\2\2\u048d\u03c9\3\2\2\2\u048d"+
		"\u03d0\3\2\2\2\u048d\u03d3\3\2\2\2\u048d\u03d6\3\2\2\2\u048d\u03e0\3\2"+
		"\2\2\u048d\u03f0\3\2\2\2\u048d\u03f7\3\2\2\2\u048d\u03f9\3\2\2\2\u048d"+
		"\u0409\3\2\2\2\u048d\u040f\3\2\2\2\u048d\u0417\3\2\2\2\u048d\u041f\3\2"+
		"\2\2\u048d\u0427\3\2\2\2\u048d\u042b\3\2\2\2\u048d\u042f\3\2\2\2\u048d"+
		"\u0438\3\2\2\2\u048d\u043d\3\2\2\2\u048d\u0448\3\2\2\2\u048d\u0451\3\2"+
		"\2\2\u048d\u0458\3\2\2\2\u048d\u045a\3\2\2\2\u048d\u0461\3\2\2\2\u048d"+
		"\u0479\3\2\2\2\u048d\u0486\3\2\2\2\u048e\33\3\2\2\2\u048f\u0490\5\u0128"+
		"\u0095\2\u0490\35\3\2\2\2\u0491\u0492\5\u0128\u0095\2\u0492\37\3\2\2\2"+
		"\u0493\u0494\7\67\2\2\u0494\u053c\7\u00d4\2\2\u0495\u0496\7R\2\2\u0496"+
		"\u053c\7\u00d4\2\2\u0497\u0499\7o\2\2\u0498\u049a\7\u00d4\2\2\u0499\u0498"+
		"\3\2\2\2\u0499\u049a\3\2\2\2\u049a\u053c\3\2\2\2\u049b\u049d\7\u00d1\2"+
		"\2\u049c\u049e\7\u00d4\2\2\u049d\u049c\3\2\2\2\u049d\u049e\3\2\2\2\u049e"+
		"\u053c\3\2\2\2\u049f\u04a0\7\u00e6\2\2\u04a0\u053c\7o\2\2\u04a1\u04a2"+
		"\7\u00e6\2\2\u04a2\u04a4\7\u00d4\2\2\u04a3\u04a5\7o\2\2\u04a4\u04a3\3"+
		"\2\2\2\u04a4\u04a5\3\2\2\2\u04a5\u053c\3\2\2\2\u04a6\u04a7\7\u00e6\2\2"+
		"\u04a7\u053c\7\u00be\2\2\u04a8\u04a9\7\u00e6\2\2\u04a9\u053c\7\u00d5\2"+
		"\2\u04aa\u04ab\7\u00e6\2\2\u04ab\u04ac\7:\2\2\u04ac\u053c\7\u00d5\2\2"+
		"\u04ad\u04ae\7[\2\2\u04ae\u053c\7\u00f5\2\2\u04af\u04b0\7v\2\2\u04b0\u053c"+
		"\7\u00f5\2\2\u04b1\u04b2\7\u00e6\2\2\u04b2\u053c\7\62\2\2\u04b3\u04b4"+
		"\7\u00e6\2\2\u04b4\u04b5\7\67\2\2\u04b5\u053c\7\u00f5\2\2\u04b6\u04b7"+
		"\7\u00e6\2\2\u04b7\u053c\7\u0104\2\2\u04b8\u04b9\7\u00e6\2\2\u04b9\u053c"+
		"\7y\2\2\u04ba\u04bb\7\u00e6\2\2\u04bb\u053c\7\u0093\2\2\u04bc\u04bd\7"+
		"\67\2\2\u04bd\u053c\7x\2\2\u04be\u04bf\7R\2\2\u04bf\u053c\7x\2\2\u04c0"+
		"\u04c1\7\17\2\2\u04c1\u053c\7x\2\2\u04c2\u04c3\7\u0092\2\2\u04c3\u053c"+
		"\7\u00f5\2\2\u04c4\u04c5\7\u0092\2\2\u04c5\u053c\7B\2\2\u04c6\u04c7\7"+
		"\u0111\2\2\u04c7\u053c\7\u00f5\2\2\u04c8\u04c9\7\u0111\2\2\u04c9\u053c"+
		"\7B\2\2\u04ca\u04cb\7\67\2\2\u04cb\u04cc\7\u00f9\2\2\u04cc\u053c\7\u0095"+
		"\2\2\u04cd\u04ce\7R\2\2\u04ce\u04cf\7\u00f9\2\2\u04cf\u053c\7\u0095\2"+
		"\2\u04d0\u04d1\7\17\2\2\u04d1\u04d2\7\u00f5\2\2\u04d2\u04d3\5\u00caf\2"+
		"\u04d3\u04d4\7\u00a2\2\2\u04d4\u04d5\7)\2\2\u04d5\u053c\3\2\2\2\u04d6"+
		"\u04d7\7\17\2\2\u04d7\u04d8\7\u00f5\2\2\u04d8\u04d9\5\u00caf\2\u04d9\u04da"+
		"\7)\2\2\u04da\u04db\7\36\2\2\u04db\u053c\3\2\2\2\u04dc\u04dd\7\17\2\2"+
		"\u04dd\u04de\7\u00f5\2\2\u04de\u04df\5\u00caf\2\u04df\u04e0\7\u00a2\2"+
		"\2\u04e0\u04e1\7\u00ea\2\2\u04e1\u053c\3\2\2\2\u04e2\u04e3\7\17\2\2\u04e3"+
		"\u04e4\7\u00f5\2\2\u04e4\u04e5\5\u00caf\2\u04e5\u04e6\7\u00e7\2\2\u04e6"+
		"\u04e7\7\36\2\2\u04e7\u053c\3\2\2\2\u04e8\u04e9\7\17\2\2\u04e9\u04ea\7"+
		"\u00f5\2\2\u04ea\u04eb\5\u00caf\2\u04eb\u04ec\7\u00a2\2\2\u04ec\u04ed"+
		"\7\u00e7\2\2\u04ed\u053c\3\2\2\2\u04ee\u04ef\7\17\2\2\u04ef\u04f0\7\u00f5"+
		"\2\2\u04f0\u04f1\5\u00caf\2\u04f1\u04f2\7\u00a2\2\2\u04f2\u04f3\7\u00ed"+
		"\2\2\u04f3\u04f4\7\26\2\2\u04f4\u04f5\7M\2\2\u04f5\u053c\3\2\2\2\u04f6"+
		"\u04f7\7\17\2\2\u04f7\u04f8\7\u00f5\2\2\u04f8\u04f9\5\u00caf\2\u04f9\u04fa"+
		"\7\u00e3\2\2\u04fa\u04fb\7\u00e7\2\2\u04fb\u04fc\7\u0091\2\2\u04fc\u053c"+
		"\3\2\2\2\u04fd\u04fe\7\17\2\2\u04fe\u04ff\7\u00f5\2\2\u04ff\u0500\5\u00ca"+
		"f\2\u0500\u0501\7X\2\2\u0501\u0502\7\u00b3\2\2\u0502\u053c\3\2\2\2\u0503"+
		"\u0504\7\17\2\2\u0504\u0505\7\u00f5\2\2\u0505\u0506\5\u00caf\2\u0506\u0507"+
		"\7\24\2\2\u0507\u0508\7\u00b3\2\2\u0508\u053c\3\2\2\2\u0509\u050a\7\17"+
		"\2\2\u050a\u050b\7\u00f5\2\2\u050b\u050c\5\u00caf\2\u050c\u050d\7\u010b"+
		"\2\2\u050d\u050e\7\u00b3\2\2\u050e\u053c\3\2\2\2\u050f\u0510\7\17\2\2"+
		"\u0510\u0511\7\u00f5\2\2\u0511\u0512\5\u00caf\2\u0512\u0513\7\u0101\2"+
		"\2\u0513\u053c\3\2\2\2\u0514\u0515\7\17\2\2\u0515\u0516\7\u00f5\2\2\u0516"+
		"\u0518\5\u00caf\2\u0517\u0519\5\64\33\2\u0518\u0517\3\2\2\2\u0518\u0519"+
		"\3\2\2\2\u0519\u051a\3\2\2\2\u051a\u051b\7\61\2\2\u051b\u053c\3\2\2\2"+
		"\u051c\u051d\7\17\2\2\u051d\u051e\7\u00f5\2\2\u051e\u0520\5\u00caf\2\u051f"+
		"\u0521\5\64\33\2\u0520\u051f\3\2\2\2\u0520\u0521\3\2\2\2\u0521\u0522\3"+
		"\2\2\2\u0522\u0523\7\64\2\2\u0523\u053c\3\2\2\2\u0524\u0525\7\17\2\2\u0525"+
		"\u0526\7\u00f5\2\2\u0526\u0528\5\u00caf\2\u0527\u0529\5\64\33\2\u0528"+
		"\u0527\3\2\2\2\u0528\u0529\3\2\2\2\u0529\u052a\3\2\2\2\u052a\u052b\7\u00e3"+
		"\2\2\u052b\u052c\7c\2\2\u052c\u053c\3\2\2\2\u052d\u052e\7\17\2\2\u052e"+
		"\u052f\7\u00f5\2\2\u052f\u0531\5\u00caf\2\u0530\u0532\5\64\33\2\u0531"+
		"\u0530\3\2\2\2\u0531\u0532\3\2\2\2\u0532\u0533\3\2\2\2\u0533\u0534\7\u00cd"+
		"\2\2\u0534\u0535\7.\2\2\u0535\u053c\3\2\2\2\u0536\u0537\7\u00eb\2\2\u0537"+
		"\u053c\7\u0103\2\2\u0538\u053c\7\60\2\2\u0539\u053c\7\u00d6\2\2\u053a"+
		"\u053c\7L\2\2\u053b\u0493\3\2\2\2\u053b\u0495\3\2\2\2\u053b\u0497\3\2"+
		"\2\2\u053b\u049b\3\2\2\2\u053b\u049f\3\2\2\2\u053b\u04a1\3\2\2\2\u053b"+
		"\u04a6\3\2\2\2\u053b\u04a8\3\2\2\2\u053b\u04aa\3\2\2\2\u053b\u04ad\3\2"+
		"\2\2\u053b\u04af\3\2\2\2\u053b\u04b1\3\2\2\2\u053b\u04b3\3\2\2\2\u053b"+
		"\u04b6\3\2\2\2\u053b\u04b8\3\2\2\2\u053b\u04ba\3\2\2\2\u053b\u04bc\3\2"+
		"\2\2\u053b\u04be\3\2\2\2\u053b\u04c0\3\2\2\2\u053b\u04c2\3\2\2\2\u053b"+
		"\u04c4\3\2\2\2\u053b\u04c6\3\2\2\2\u053b\u04c8\3\2\2\2\u053b\u04ca\3\2"+
		"\2\2\u053b\u04cd\3\2\2\2\u053b\u04d0\3\2\2\2\u053b\u04d6\3\2\2\2\u053b"+
		"\u04dc\3\2\2\2\u053b\u04e2\3\2\2\2\u053b\u04e8\3\2\2\2\u053b\u04ee\3\2"+
		"\2\2\u053b\u04f6\3\2\2\2\u053b\u04fd\3\2\2\2\u053b\u0503\3\2\2\2\u053b"+
		"\u0509\3\2\2\2\u053b\u050f\3\2\2\2\u053b\u0514\3\2\2\2\u053b\u051c\3\2"+
		"\2\2\u053b\u0524\3\2\2\2\u053b\u052d\3\2\2\2\u053b\u0536\3\2\2\2\u053b"+
		"\u0538\3\2\2\2\u053b\u0539\3\2\2\2\u053b\u053a\3\2\2\2\u053c!\3\2\2\2"+
		"\u053d\u053f\7\67\2\2\u053e\u0540\7\u00f9\2\2\u053f\u053e\3\2\2\2\u053f"+
		"\u0540\3\2\2\2\u0540\u0542\3\2\2\2\u0541\u0543\7]\2\2\u0542\u0541\3\2"+
		"\2\2\u0542\u0543\3\2\2\2\u0543\u0544\3\2\2\2\u0544\u0548\7\u00f5\2\2\u0545"+
		"\u0546\7t\2\2\u0546\u0547\7\u00a2\2\2\u0547\u0549\7Y\2\2\u0548\u0545\3"+
		"\2\2\2\u0548\u0549\3\2\2\2\u0549\u054a\3\2\2\2\u054a\u054b\5\u00c4c\2"+
		"\u054b#\3\2\2\2\u054c\u054d\7\67\2\2\u054d\u054f\7\u00aa\2\2\u054e\u054c"+
		"\3\2\2\2\u054e\u054f\3\2\2\2\u054f\u0550\3\2\2\2\u0550\u0551\7\u00cd\2"+
		"\2\u0551\u0552\7\u00f5\2\2\u0552\u0553\5\u00c4c\2\u0553%\3\2\2\2\u0554"+
		"\u0555\7)\2\2\u0555\u0556\7\36\2\2\u0556\u055a\5\u00acW\2\u0557\u0558"+
		"\7\u00ea\2\2\u0558\u0559\7\36\2\2\u0559\u055b\5\u00b0Y\2\u055a\u0557\3"+
		"\2\2\2\u055a\u055b\3\2\2\2\u055b\u055c\3\2\2\2\u055c\u055d\7\u0080\2\2"+
		"\u055d\u055e\7\u013d\2\2\u055e\u055f\7\35\2\2\u055f\'\3\2\2\2\u0560\u0561"+
		"\7\u00e7\2\2\u0561\u0562\7\36\2\2\u0562\u0563\5\u00acW\2\u0563\u0566\7"+
		"\u00a6\2\2\u0564\u0567\5P)\2\u0565\u0567\5R*\2\u0566\u0564\3\2\2\2\u0566"+
		"\u0565\3\2\2\2\u0567\u056b\3\2\2\2\u0568\u0569\7\u00ed\2\2\u0569\u056a"+
		"\7\26\2\2\u056a\u056c\7M\2\2\u056b\u0568\3\2\2\2\u056b\u056c\3\2\2\2\u056c"+
		")\3\2\2\2\u056d\u056e\7\u0091\2\2\u056e\u056f\7\u0139\2\2\u056f+\3\2\2"+
		"\2\u0570\u0571\7/\2\2\u0571\u0572\7\u0139\2\2\u0572-\3\2\2\2\u0573\u0575"+
		"\5@!\2\u0574\u0573\3\2\2\2\u0574\u0575\3\2\2\2\u0575\u0576\3\2\2\2\u0576"+
		"\u0577\5b\62\2\u0577\u0578\5^\60\2\u0578/\3\2\2\2\u0579\u057a\7}\2\2\u057a"+
		"\u057c\7\u00b2\2\2\u057b\u057d\7\u00f5\2\2\u057c\u057b\3\2\2\2\u057c\u057d"+
		"\3\2\2\2\u057d\u057e\3\2\2\2\u057e\u0585\5\u00c4c\2\u057f\u0583\5\64\33"+
		"\2\u0580\u0581\7t\2\2\u0581\u0582\7\u00a2\2\2\u0582\u0584\7Y\2\2\u0583"+
		"\u0580\3\2\2\2\u0583\u0584\3\2\2\2\u0584\u0586\3\2\2\2\u0585\u057f\3\2"+
		"\2\2\u0585\u0586\3\2\2\2\u0586\u0588\3\2\2\2\u0587\u0589\5\u00acW\2\u0588"+
		"\u0587\3\2\2\2\u0588\u0589\3\2\2\2\u0589\u05b7\3\2\2\2\u058a\u058b\7}"+
		"\2\2\u058b\u058d\7\u0080\2\2\u058c\u058e\7\u00f5\2\2\u058d\u058c\3\2\2"+
		"\2\u058d\u058e\3\2\2\2\u058e\u058f\3\2\2\2\u058f\u0591\5\u00c4c\2\u0590"+
		"\u0592\5\64\33\2\u0591\u0590\3\2\2\2\u0591\u0592\3\2\2\2\u0592\u0596\3"+
		"\2\2\2\u0593\u0594\7t\2\2\u0594\u0595\7\u00a2\2\2\u0595\u0597\7Y\2\2\u0596"+
		"\u0593\3\2\2\2\u0596\u0597\3\2\2\2\u0597\u0599\3\2\2\2\u0598\u059a\5\u00ac"+
		"W\2\u0599\u0598\3\2\2\2\u0599\u059a\3\2\2\2\u059a\u05b7\3\2\2\2\u059b"+
		"\u059c\7}\2\2\u059c\u059e\7\u00b2\2\2\u059d\u059f\7\u0090\2\2\u059e\u059d"+
		"\3\2\2\2\u059e\u059f\3\2\2\2\u059f\u05a0\3\2\2\2\u05a0\u05a1\7N\2\2\u05a1"+
		"\u05a3\7\u0139\2\2\u05a2\u05a4\5\u00c0a\2\u05a3\u05a2\3\2\2\2\u05a3\u05a4"+
		"\3\2\2\2\u05a4\u05a6\3\2\2\2\u05a5\u05a7\5T+\2\u05a6\u05a5\3\2\2\2\u05a6"+
		"\u05a7\3\2\2\2\u05a7\u05b7\3\2\2\2\u05a8\u05a9\7}\2\2\u05a9\u05ab\7\u00b2"+
		"\2\2\u05aa\u05ac\7\u0090\2\2\u05ab\u05aa\3\2\2\2\u05ab\u05ac\3\2\2\2\u05ac"+
		"\u05ad\3\2\2\2\u05ad\u05af\7N\2\2\u05ae\u05b0\7\u0139\2\2\u05af\u05ae"+
		"\3\2\2\2\u05af\u05b0\3\2\2\2\u05b0\u05b1\3\2\2\2\u05b1\u05b4\5D#\2\u05b2"+
		"\u05b3\7\u00a9\2\2\u05b3\u05b5\5H%\2\u05b4\u05b2\3\2\2\2\u05b4\u05b5\3"+
		"\2\2\2\u05b5\u05b7\3\2\2\2\u05b6\u0579\3\2\2\2\u05b6\u058a\3\2\2\2\u05b6"+
		"\u059b\3\2\2\2\u05b6\u05a8\3\2\2\2\u05b7\61\3\2\2\2\u05b8\u05ba\5\64\33"+
		"\2\u05b9\u05bb\5*\26\2\u05ba\u05b9\3\2\2\2\u05ba\u05bb\3\2\2\2\u05bb\63"+
		"\3\2\2\2\u05bc\u05bd\7\u00b3\2\2\u05bd\u05be\7\6\2\2\u05be\u05c3\5\66"+
		"\34\2\u05bf\u05c0\7\b\2\2\u05c0\u05c2\5\66\34\2\u05c1\u05bf\3\2\2\2\u05c2"+
		"\u05c5\3\2\2\2\u05c3\u05c1\3\2\2\2\u05c3\u05c4\3\2\2\2\u05c4\u05c6\3\2"+
		"\2\2\u05c5\u05c3\3\2\2\2\u05c6\u05c7\7\7\2\2\u05c7\65\3\2\2\2\u05c8\u05cb"+
		"\5\u0124\u0093\2\u05c9\u05ca\7\u0123\2\2\u05ca\u05cc\5\u00e8u\2\u05cb"+
		"\u05c9\3\2\2\2\u05cb\u05cc\3\2\2\2\u05cc\67\3\2\2\2\u05cd\u05ce\t\17\2"+
		"\2\u05ce9\3\2\2\2\u05cf\u05d0\t\20\2\2\u05d0;\3\2\2\2\u05d1\u05d7\5\u011e"+
		"\u0090\2\u05d2\u05d7\7\u0139\2\2\u05d3\u05d7\5\u00eav\2\u05d4\u05d7\5"+
		"\u00ecw\2\u05d5\u05d7\5\u00eex\2\u05d6\u05d1\3\2\2\2\u05d6\u05d2\3\2\2"+
		"\2\u05d6\u05d3\3\2\2\2\u05d6\u05d4\3\2\2\2\u05d6\u05d5\3\2\2\2\u05d7="+
		"\3\2\2\2\u05d8\u05dd\5\u0124\u0093\2\u05d9\u05da\7\t\2\2\u05da\u05dc\5"+
		"\u0124\u0093\2\u05db\u05d9\3\2\2\2\u05dc\u05df\3\2\2\2\u05dd\u05db\3\2"+
		"\2\2\u05dd\u05de\3\2\2\2\u05de?\3\2\2\2\u05df\u05dd\3\2\2\2\u05e0\u05e1"+
		"\7\u011f\2\2\u05e1\u05e6\5B\"\2\u05e2\u05e3\7\b\2\2\u05e3\u05e5\5B\"\2"+
		"\u05e4\u05e2\3\2\2\2\u05e5\u05e8\3\2\2\2\u05e6\u05e4\3\2\2\2\u05e6\u05e7"+
		"\3\2\2\2\u05e7A\3\2\2\2\u05e8\u05e6\3\2\2\2\u05e9\u05eb\5\u0120\u0091"+
		"\2\u05ea\u05ec\5\u00acW\2\u05eb\u05ea\3\2\2\2\u05eb\u05ec\3\2\2\2\u05ec"+
		"\u05ee\3\2\2\2\u05ed\u05ef\7\26\2\2\u05ee\u05ed\3\2\2\2\u05ee\u05ef\3"+
		"\2\2\2\u05ef\u05f0\3\2\2\2\u05f0\u05f1\7\6\2\2\u05f1\u05f2\5.\30\2\u05f2"+
		"\u05f3\7\7\2\2\u05f3C\3\2\2\2\u05f4\u05f5\7\u0116\2\2\u05f5\u05f6\5\u00c4"+
		"c\2\u05f6E\3\2\2\2\u05f7\u05f8\7\u00a9\2\2\u05f8\u0605\5H%\2\u05f9\u05fa"+
		"\7\u00b4\2\2\u05fa\u05fb\7\36\2\2\u05fb\u0605\5\u00d2j\2\u05fc\u0605\5"+
		"(\25\2\u05fd\u0605\5&\24\2\u05fe\u0605\5\u00c0a\2\u05ff\u0605\5T+\2\u0600"+
		"\u0605\5*\26\2\u0601\u0605\5,\27\2\u0602\u0603\7\u00f8\2\2\u0603\u0605"+
		"\5H%\2\u0604\u05f7\3\2\2\2\u0604\u05f9\3\2\2\2\u0604\u05fc\3\2\2\2\u0604"+
		"\u05fd\3\2\2\2\u0604\u05fe\3\2\2\2\u0604\u05ff\3\2\2\2\u0604\u0600\3\2"+
		"\2\2\u0604\u0601\3\2\2\2\u0604\u0602\3\2\2\2\u0605\u0608\3\2\2\2\u0606"+
		"\u0604\3\2\2\2\u0606\u0607\3\2\2\2\u0607G\3\2\2\2\u0608\u0606\3\2\2\2"+
		"\u0609\u060a\7\6\2\2\u060a\u060f\5J&\2\u060b\u060c\7\b\2\2\u060c\u060e"+
		"\5J&\2\u060d\u060b\3\2\2\2\u060e\u0611\3\2\2\2\u060f\u060d\3\2\2\2\u060f"+
		"\u0610\3\2\2\2\u0610\u0612\3\2\2\2\u0611\u060f\3\2\2\2\u0612\u0613\7\7"+
		"\2\2\u0613I\3\2\2\2\u0614\u0619\5L\'\2\u0615\u0617\7\u0123\2\2\u0616\u0615"+
		"\3\2\2\2\u0616\u0617\3\2\2\2\u0617\u0618\3\2\2\2\u0618\u061a\5N(\2\u0619"+
		"\u0616\3\2\2\2\u0619\u061a\3\2\2\2\u061aK\3\2\2\2\u061b\u0620\5\u0124"+
		"\u0093\2\u061c\u061d\7\t\2\2\u061d\u061f\5\u0124\u0093\2\u061e\u061c\3"+
		"\2\2\2\u061f\u0622\3\2\2\2\u0620\u061e\3\2\2\2\u0620\u0621\3\2\2\2\u0621"+
		"\u0625\3\2\2\2\u0622\u0620\3\2\2\2\u0623\u0625\7\u0139\2\2\u0624\u061b"+
		"\3\2\2\2\u0624\u0623\3\2\2\2\u0625M\3\2\2\2\u0626\u062b\7\u013d\2\2\u0627"+
		"\u062b\7\u013f\2\2\u0628\u062b\5\u00f0y\2\u0629\u062b\7\u0139\2\2\u062a"+
		"\u0626\3\2\2\2\u062a\u0627\3\2\2\2\u062a\u0628\3\2\2\2\u062a\u0629\3\2"+
		"\2\2\u062bO\3\2\2\2\u062c\u062d\7\6\2\2\u062d\u0632\5\u00e8u\2\u062e\u062f"+
		"\7\b\2\2\u062f\u0631\5\u00e8u\2\u0630\u062e\3\2\2\2\u0631\u0634\3\2\2"+
		"\2\u0632\u0630\3\2\2\2\u0632\u0633\3\2\2\2\u0633\u0635\3\2\2\2\u0634\u0632"+
		"\3\2\2\2\u0635\u0636\7\7\2\2\u0636Q\3\2\2\2\u0637\u0638\7\6\2\2\u0638"+
		"\u063d\5P)\2\u0639\u063a\7\b\2\2\u063a\u063c\5P)\2\u063b\u0639\3\2\2\2"+
		"\u063c\u063f\3\2\2\2\u063d\u063b\3\2\2\2\u063d\u063e\3\2\2\2\u063e\u0640"+
		"\3\2\2\2\u063f\u063d\3\2\2\2\u0640\u0641\7\7\2\2\u0641S\3\2\2\2\u0642"+
		"\u0643\7\u00ed\2\2\u0643\u0644\7\26\2\2\u0644\u0649\5V,\2\u0645\u0646"+
		"\7\u00ed\2\2\u0646\u0647\7\36\2\2\u0647\u0649\5X-\2\u0648\u0642\3\2\2"+
		"\2\u0648\u0645\3\2\2\2\u0649U\3\2\2\2\u064a\u064b\7|\2\2\u064b\u064c\7"+
		"\u0139\2\2\u064c\u064d\7\u00ae\2\2\u064d\u0650\7\u0139\2\2\u064e\u0650"+
		"\5\u0124\u0093\2\u064f\u064a\3\2\2\2\u064f\u064e\3\2\2\2\u0650W\3\2\2"+
		"\2\u0651\u0655\7\u0139\2\2\u0652\u0653\7\u011f\2\2\u0653\u0654\7\u00e1"+
		"\2\2\u0654\u0656\5H%\2\u0655\u0652\3\2\2\2\u0655\u0656\3\2\2\2\u0656Y"+
		"\3\2\2\2\u0657\u0658\5\u0124\u0093\2\u0658\u0659\7\u0139\2\2\u0659[\3"+
		"\2\2\2\u065a\u065b\5\60\31\2\u065b\u065c\5.\30\2\u065c\u068d\3\2\2\2\u065d"+
		"\u065f\5\u0088E\2\u065e\u0660\5`\61\2\u065f\u065e\3\2\2\2\u0660\u0661"+
		"\3\2\2\2\u0661\u065f\3\2\2\2\u0661\u0662\3\2\2\2\u0662\u068d\3\2\2\2\u0663"+
		"\u0664\7H\2\2\u0664\u0665\7j\2\2\u0665\u0666\5\u00c4c\2\u0666\u0668\5"+
		"\u00be`\2\u0667\u0669\5\u0080A\2\u0668\u0667\3\2\2\2\u0668\u0669\3\2\2"+
		"\2\u0669\u068d\3\2\2\2\u066a\u066b\7\u0113\2\2\u066b\u066c\5\u00c4c\2"+
		"\u066c\u066d\5\u00be`\2\u066d\u066f\5r:\2\u066e\u0670\5\u0080A\2\u066f"+
		"\u066e\3\2\2\2\u066f\u0670\3\2\2\2\u0670\u068d\3\2\2\2\u0671\u0672\7\u0098"+
		"\2\2\u0672\u0673\7\u0080\2\2\u0673\u0674\5\u00c4c\2\u0674\u0675\5\u00be"+
		"`\2\u0675\u067b\7\u0116\2\2\u0676\u067c\5\u00c4c\2\u0677\u0678\7\6\2\2"+
		"\u0678\u0679\5.\30\2\u0679\u067a\7\7\2\2\u067a\u067c\3\2\2\2\u067b\u0676"+
		"\3\2\2\2\u067b\u0677\3\2\2\2\u067c\u067d\3\2\2\2\u067d\u067e\5\u00be`"+
		"\2\u067e\u067f\7\u00a6\2\2\u067f\u0683\5\u00dep\2\u0680\u0682\5t;\2\u0681"+
		"\u0680\3\2\2\2\u0682\u0685\3\2\2\2\u0683\u0681\3\2\2\2\u0683\u0684\3\2"+
		"\2\2\u0684\u0689\3\2\2\2\u0685\u0683\3\2\2\2\u0686\u0688\5v<\2\u0687\u0686"+
		"\3\2\2\2\u0688\u068b\3\2\2\2\u0689\u0687\3\2\2\2\u0689\u068a\3\2\2\2\u068a"+
		"\u068d\3\2\2\2\u068b\u0689\3\2\2\2\u068c\u065a\3\2\2\2\u068c\u065d\3\2"+
		"\2\2\u068c\u0663\3\2\2\2\u068c\u066a\3\2\2\2\u068c\u0671\3\2\2\2\u068d"+
		"]\3\2\2\2\u068e\u068f\7\u00ab\2\2\u068f\u0690\7\36\2\2\u0690\u0695\5f"+
		"\64\2\u0691\u0692\7\b\2\2\u0692\u0694\5f\64\2\u0693\u0691\3\2\2\2\u0694"+
		"\u0697\3\2\2\2\u0695\u0693\3\2\2\2\u0695\u0696\3\2\2\2\u0696\u0699\3\2"+
		"\2\2\u0697\u0695\3\2\2\2\u0698\u068e\3\2\2\2\u0698\u0699\3\2\2\2\u0699"+
		"\u06a4\3\2\2\2\u069a\u069b\7(\2\2\u069b\u069c\7\36\2\2\u069c\u06a1\5\u00da"+
		"n\2\u069d\u069e\7\b\2\2\u069e\u06a0\5\u00dan\2\u069f\u069d\3\2\2\2\u06a0"+
		"\u06a3\3\2\2\2\u06a1\u069f\3\2\2\2\u06a1\u06a2\3\2\2\2\u06a2\u06a5\3\2"+
		"\2\2\u06a3\u06a1\3\2\2\2\u06a4\u069a\3\2\2\2\u06a4\u06a5\3\2\2\2\u06a5"+
		"\u06b0\3\2\2\2\u06a6\u06a7\7P\2\2\u06a7\u06a8\7\36\2\2\u06a8\u06ad\5\u00da"+
		"n\2\u06a9\u06aa\7\b\2\2\u06aa\u06ac\5\u00dan\2\u06ab\u06a9\3\2\2\2\u06ac"+
		"\u06af\3\2\2\2\u06ad\u06ab\3\2\2\2\u06ad\u06ae\3\2\2\2\u06ae\u06b1\3\2"+
		"\2\2\u06af\u06ad\3\2\2\2\u06b0\u06a6\3\2\2\2\u06b0\u06b1\3\2\2\2\u06b1"+
		"\u06bc\3\2\2\2\u06b2\u06b3\7\u00e9\2\2\u06b3\u06b4\7\36\2\2\u06b4\u06b9"+
		"\5f\64\2\u06b5\u06b6\7\b\2\2\u06b6\u06b8\5f\64\2\u06b7\u06b5\3\2\2\2\u06b8"+
		"\u06bb\3\2\2\2\u06b9\u06b7\3\2\2\2\u06b9\u06ba\3\2\2\2\u06ba\u06bd\3\2"+
		"\2\2\u06bb\u06b9\3\2\2\2\u06bc\u06b2\3\2\2\2\u06bc\u06bd\3\2\2\2\u06bd"+
		"\u06bf\3\2\2\2\u06be\u06c0\5\u0110\u0089\2\u06bf\u06be\3\2\2\2\u06bf\u06c0"+
		"\3\2\2\2\u06c0\u06c6\3\2\2\2\u06c1\u06c4\7\u008c\2\2\u06c2\u06c5\7\16"+
		"\2\2\u06c3\u06c5\5\u00dan\2\u06c4\u06c2\3\2\2\2\u06c4\u06c3\3\2\2\2\u06c5"+
		"\u06c7\3\2\2\2\u06c6\u06c1\3\2\2\2\u06c6\u06c7\3\2\2\2\u06c7_\3\2\2\2"+
		"\u06c8\u06c9\5\60\31\2\u06c9\u06ca\5j\66\2\u06caa\3\2\2\2\u06cb\u06cc"+
		"\b\62\1\2\u06cc\u06cd\5d\63\2\u06cd\u06e5\3\2\2\2\u06ce\u06cf\f\5\2\2"+
		"\u06cf\u06d0\6\62\3\2\u06d0\u06d2\t\21\2\2\u06d1\u06d3\5\u009eP\2\u06d2"+
		"\u06d1\3\2\2\2\u06d2\u06d3\3\2\2\2\u06d3\u06d4\3\2\2\2\u06d4\u06e4\5b"+
		"\62\6\u06d5\u06d6\f\4\2\2\u06d6\u06d7\6\62\5\2\u06d7\u06d9\7~\2\2\u06d8"+
		"\u06da\5\u009eP\2\u06d9\u06d8\3\2\2\2\u06d9\u06da\3\2\2\2\u06da\u06db"+
		"\3\2\2\2\u06db\u06e4\5b\62\5\u06dc\u06dd\f\3\2\2\u06dd\u06de\6\62\7\2"+
		"\u06de\u06e0\t\22\2\2\u06df\u06e1\5\u009eP\2\u06e0\u06df\3\2\2\2\u06e0"+
		"\u06e1\3\2\2\2\u06e1\u06e2\3\2\2\2\u06e2\u06e4\5b\62\4\u06e3\u06ce\3\2"+
		"\2\2\u06e3\u06d5\3\2\2\2\u06e3\u06dc\3\2\2\2\u06e4\u06e7\3\2\2\2\u06e5"+
		"\u06e3\3\2\2\2\u06e5\u06e6\3\2\2\2\u06e6c\3\2\2\2\u06e7\u06e5\3\2\2\2"+
		"\u06e8\u06f2\5l\67\2\u06e9\u06f2\5h\65\2\u06ea\u06eb\7\u00f5\2\2\u06eb"+
		"\u06f2\5\u00c4c\2\u06ec\u06f2\5\u00ba^\2\u06ed\u06ee\7\6\2\2\u06ee\u06ef"+
		"\5.\30\2\u06ef\u06f0\7\7\2\2\u06f0\u06f2\3\2\2\2\u06f1\u06e8\3\2\2\2\u06f1"+
		"\u06e9\3\2\2\2\u06f1\u06ea\3\2\2\2\u06f1\u06ec\3\2\2\2\u06f1\u06ed\3\2"+
		"\2\2\u06f2e\3\2\2\2\u06f3\u06f5\5\u00dan\2\u06f4\u06f6\t\23\2\2\u06f5"+
		"\u06f4\3\2\2\2\u06f5\u06f6\3\2\2\2\u06f6\u06f9\3\2\2\2\u06f7\u06f8\7\u00a4"+
		"\2\2\u06f8\u06fa\t\24\2\2\u06f9\u06f7\3\2\2\2\u06f9\u06fa\3\2\2\2\u06fa"+
		"g\3\2\2\2\u06fb\u06fd\5\u0088E\2\u06fc\u06fe\5j\66\2\u06fd\u06fc\3\2\2"+
		"\2\u06fe\u06ff\3\2\2\2\u06ff\u06fd\3\2\2\2\u06ff\u0700\3\2\2\2\u0700i"+
		"\3\2\2\2\u0701\u0703\5n8\2\u0702\u0704\5\u0080A\2\u0703\u0702\3\2\2\2"+
		"\u0703\u0704\3\2\2\2\u0704\u0705\3\2\2\2\u0705\u0706\5^\60\2\u0706\u071d"+
		"\3\2\2\2\u0707\u070b\5p9\2\u0708\u070a\5\u009cO\2\u0709\u0708\3\2\2\2"+
		"\u070a\u070d\3\2\2\2\u070b\u0709\3\2\2\2\u070b\u070c\3\2\2\2\u070c\u070f"+
		"\3\2\2\2\u070d\u070b\3\2\2\2\u070e\u0710\5\u0080A\2\u070f\u070e\3\2\2"+
		"\2\u070f\u0710\3\2\2\2\u0710\u0712\3\2\2\2\u0711\u0713\5\u008cG\2\u0712"+
		"\u0711\3\2\2\2\u0712\u0713\3\2\2\2\u0713\u0715\3\2\2\2\u0714\u0716\5\u0082"+
		"B\2\u0715\u0714\3\2\2\2\u0715\u0716\3\2\2\2\u0716\u0718\3\2\2\2\u0717"+
		"\u0719\5\u0110\u0089\2\u0718\u0717\3\2\2\2\u0718\u0719\3\2\2\2\u0719\u071a"+
		"\3\2\2\2\u071a\u071b\5^\60\2\u071b\u071d\3\2\2\2\u071c\u0701\3\2\2\2\u071c"+
		"\u0707\3\2\2\2\u071dk\3\2\2\2\u071e\u0720\5n8\2\u071f\u0721\5\u0088E\2"+
		"\u0720\u071f\3\2\2\2\u0720\u0721\3\2\2\2\u0721\u0725\3\2\2\2\u0722\u0724"+
		"\5\u009cO\2\u0723\u0722\3\2\2\2\u0724\u0727\3\2\2\2\u0725\u0723\3\2\2"+
		"\2\u0725\u0726\3\2\2\2\u0726\u0729\3\2\2\2\u0727\u0725\3\2\2\2\u0728\u072a"+
		"\5\u0080A\2\u0729\u0728\3\2\2\2\u0729\u072a\3\2\2\2\u072a\u072c\3\2\2"+
		"\2\u072b\u072d\5\u008cG\2\u072c\u072b\3\2\2\2\u072c\u072d\3\2\2\2\u072d"+
		"\u072f\3\2\2\2\u072e\u0730\5\u0082B\2\u072f\u072e\3\2\2\2\u072f\u0730"+
		"\3\2\2\2\u0730\u0732\3\2\2\2\u0731\u0733\5\u0110\u0089\2\u0732\u0731\3"+
		"\2\2\2\u0732\u0733\3\2\2\2\u0733\u074b\3\2\2\2\u0734\u0736\5p9\2\u0735"+
		"\u0737\5\u0088E\2\u0736\u0735\3\2\2\2\u0736\u0737\3\2\2\2\u0737\u073b"+
		"\3\2\2\2\u0738\u073a\5\u009cO\2\u0739\u0738\3\2\2\2\u073a\u073d\3\2\2"+
		"\2\u073b\u0739\3\2\2\2\u073b\u073c\3\2\2\2\u073c\u073f\3\2\2\2\u073d\u073b"+
		"\3\2\2\2\u073e\u0740\5\u0080A\2\u073f\u073e\3\2\2\2\u073f\u0740\3\2\2"+
		"\2\u0740\u0742\3\2\2\2\u0741\u0743\5\u008cG\2\u0742\u0741\3\2\2\2\u0742"+
		"\u0743\3\2\2\2\u0743\u0745\3\2\2\2\u0744\u0746\5\u0082B\2\u0745\u0744"+
		"\3\2\2\2\u0745\u0746\3\2\2\2\u0746\u0748\3\2\2\2\u0747\u0749\5\u0110\u0089"+
		"\2\u0748\u0747\3\2\2\2\u0748\u0749\3\2\2\2\u0749\u074b\3\2\2\2\u074a\u071e"+
		"\3\2\2\2\u074a\u0734\3\2\2\2\u074bm\3\2\2\2\u074c\u074d\7\u00dd\2\2\u074d"+
		"\u074e\7\u0105\2\2\u074e\u0750\7\6\2\2\u074f\u0751\5\u009eP\2\u0750\u074f"+
		"\3\2\2\2\u0750\u0751\3\2\2\2\u0751\u0752\3\2\2\2\u0752\u0753\5\u00dco"+
		"\2\u0753\u0754\7\7\2\2\u0754\u0760\3\2\2\2\u0755\u0757\7\u0096\2\2\u0756"+
		"\u0758\5\u009eP\2\u0757\u0756\3\2\2\2\u0757\u0758\3\2\2\2\u0758\u0759"+
		"\3\2\2\2\u0759\u0760\5\u00dco\2\u075a\u075c\7\u00c7\2\2\u075b\u075d\5"+
		"\u009eP\2\u075c\u075b\3\2\2\2\u075c\u075d\3\2\2\2\u075d\u075e\3\2\2\2"+
		"\u075e\u0760\5\u00dco\2\u075f\u074c\3\2\2\2\u075f\u0755\3\2\2\2\u075f"+
		"\u075a\3\2\2\2\u0760\u0762\3\2\2\2\u0761\u0763\5\u00c0a\2\u0762\u0761"+
		"\3\2\2\2\u0762\u0763\3\2\2\2\u0763\u0766\3\2\2\2\u0764\u0765\7\u00c5\2"+
		"\2\u0765\u0767\7\u0139\2\2\u0766\u0764\3\2\2\2\u0766\u0767\3\2\2\2\u0767"+
		"\u0768\3\2\2\2\u0768\u0769\7\u0116\2\2\u0769\u0776\7\u0139\2\2\u076a\u0774"+
		"\7\26\2\2\u076b\u0775\5\u00aeX\2\u076c\u0775\5\u0106\u0084\2\u076d\u0770"+
		"\7\6\2\2\u076e\u0771\5\u00aeX\2\u076f\u0771\5\u0106\u0084\2\u0770\u076e"+
		"\3\2\2\2\u0770\u076f\3\2\2\2\u0771\u0772\3\2\2\2\u0772\u0773\7\7\2\2\u0773"+
		"\u0775\3\2\2\2\u0774\u076b\3\2\2\2\u0774\u076c\3\2\2\2\u0774\u076d\3\2"+
		"\2\2\u0775\u0777\3\2\2\2\u0776\u076a\3\2\2\2\u0776\u0777\3\2\2\2\u0777"+
		"\u0779\3\2\2\2\u0778\u077a\5\u00c0a\2\u0779\u0778\3\2\2\2\u0779\u077a"+
		"\3\2\2\2\u077a\u077d\3\2\2\2\u077b\u077c\7\u00c4\2\2\u077c\u077e\7\u0139"+
		"\2\2\u077d\u077b\3\2\2\2\u077d\u077e\3\2\2\2\u077eo\3\2\2\2\u077f\u0783"+
		"\7\u00dd\2\2\u0780\u0782\5\u0084C\2\u0781\u0780\3\2\2\2\u0782\u0785\3"+
		"\2\2\2\u0783\u0781\3\2\2\2\u0783\u0784\3\2\2\2\u0784\u0787\3\2\2\2\u0785"+
		"\u0783\3\2\2\2\u0786\u0788\5\u009eP\2\u0787\u0786\3\2\2\2\u0787\u0788"+
		"\3\2\2\2\u0788\u0789\3\2\2\2\u0789\u078a\5\u00d0i\2\u078aq\3\2\2\2\u078b"+
		"\u078c\7\u00e3\2\2\u078c\u078d\5|?\2\u078ds\3\2\2\2\u078e\u078f\7\u011c"+
		"\2\2\u078f\u0792\7\u0097\2\2\u0790\u0791\7\21\2\2\u0791\u0793\5\u00de"+
		"p\2\u0792\u0790\3\2\2\2\u0792\u0793\3\2\2\2\u0793\u0794\3\2\2\2\u0794"+
		"\u0795\7\u00fb\2\2\u0795\u0796\5x=\2\u0796u\3\2\2\2\u0797\u0798\7\u011c"+
		"\2\2\u0798\u0799\7\u00a2\2\2\u0799\u079c\7\u0097\2\2\u079a\u079b\7\21"+
		"\2\2\u079b\u079d\5\u00dep\2\u079c\u079a\3\2\2\2\u079c\u079d\3\2\2\2\u079d"+
		"\u079e\3\2\2\2\u079e\u079f\7\u00fb\2\2\u079f\u07a0\5z>\2\u07a0w\3\2\2"+
		"\2\u07a1\u07a9\7H\2\2\u07a2\u07a3\7\u0113\2\2\u07a3\u07a4\7\u00e3\2\2"+
		"\u07a4\u07a9\7\u012d\2\2\u07a5\u07a6\7\u0113\2\2\u07a6\u07a7\7\u00e3\2"+
		"\2\u07a7\u07a9\5|?\2\u07a8\u07a1\3\2\2\2\u07a8\u07a2\3\2\2\2\u07a8\u07a5"+
		"\3\2\2\2\u07a9y\3\2\2\2\u07aa\u07ab\7}\2\2\u07ab\u07bd\7\u012d\2\2\u07ac"+
		"\u07ad\7}\2\2\u07ad\u07ae\7\6\2\2\u07ae\u07af\5\u00c2b\2\u07af\u07b0\7"+
		"\7\2\2\u07b0\u07b1\7\u0117\2\2\u07b1\u07b2\7\6\2\2\u07b2\u07b7\5\u00da"+
		"n\2\u07b3\u07b4\7\b\2\2\u07b4\u07b6\5\u00dan\2\u07b5\u07b3\3\2\2\2\u07b6"+
		"\u07b9\3\2\2\2\u07b7\u07b5\3\2\2\2\u07b7\u07b8\3\2\2\2\u07b8\u07ba\3\2"+
		"\2\2\u07b9\u07b7\3\2\2\2\u07ba\u07bb\7\7\2\2\u07bb\u07bd\3\2\2\2\u07bc"+
		"\u07aa\3\2\2\2\u07bc\u07ac\3\2\2\2\u07bd{\3\2\2\2\u07be\u07c3\5~@\2\u07bf"+
		"\u07c0\7\b\2\2\u07c0\u07c2\5~@\2\u07c1\u07bf\3\2\2\2\u07c2\u07c5\3\2\2"+
		"\2\u07c3\u07c1\3\2\2\2\u07c3\u07c4\3\2\2\2\u07c4}\3\2\2\2\u07c5\u07c3"+
		"\3\2\2\2\u07c6\u07c7\5\u00c4c\2\u07c7\u07c8\7\u0123\2\2\u07c8\u07c9\5"+
		"\u00dan\2\u07c9\177\3\2\2\2\u07ca\u07cb\7\u011d\2\2\u07cb\u07cc\5\u00de"+
		"p\2\u07cc\u0081\3\2\2\2\u07cd\u07ce\7r\2\2\u07ce\u07cf\5\u00dep\2\u07cf"+
		"\u0083\3\2\2\2\u07d0\u07d1\7\u0137\2\2\u07d1\u07d8\5\u0086D\2\u07d2\u07d4"+
		"\7\b\2\2\u07d3\u07d2\3\2\2\2\u07d3\u07d4\3\2\2\2\u07d4\u07d5\3\2\2\2\u07d5"+
		"\u07d7\5\u0086D\2\u07d6\u07d3\3\2\2\2\u07d7\u07da\3\2\2\2\u07d8\u07d6"+
		"\3\2\2\2\u07d8\u07d9\3\2\2\2\u07d9\u07db\3\2\2\2\u07da\u07d8\3\2\2\2\u07db"+
		"\u07dc\7\u0138\2\2\u07dc\u0085\3\2\2\2\u07dd\u07eb\5\u0124\u0093\2\u07de"+
		"\u07df\5\u0124\u0093\2\u07df\u07e0\7\6\2\2\u07e0\u07e5\5\u00e6t\2\u07e1"+
		"\u07e2\7\b\2\2\u07e2\u07e4\5\u00e6t\2\u07e3\u07e1\3\2\2\2\u07e4\u07e7"+
		"\3\2\2\2\u07e5\u07e3\3\2\2\2\u07e5\u07e6\3\2\2\2\u07e6\u07e8\3\2\2\2\u07e7"+
		"\u07e5\3\2\2\2\u07e8\u07e9\7\7\2\2\u07e9\u07eb\3\2\2\2\u07ea\u07dd\3\2"+
		"\2\2\u07ea\u07de\3\2\2\2\u07eb\u0087\3\2\2\2\u07ec\u07ed\7j\2\2\u07ed"+
		"\u07f2\5\u00a0Q\2\u07ee\u07ef\7\b\2\2\u07ef\u07f1\5\u00a0Q\2\u07f0\u07ee"+
		"\3\2\2\2\u07f1\u07f4\3\2\2\2\u07f2\u07f0\3\2\2\2\u07f2\u07f3\3\2\2\2\u07f3"+
		"\u07f8\3\2\2\2\u07f4\u07f2\3\2\2\2\u07f5\u07f7\5\u009cO\2\u07f6\u07f5"+
		"\3\2\2\2\u07f7\u07fa\3\2\2\2\u07f8\u07f6\3\2\2\2\u07f8\u07f9\3\2\2\2\u07f9"+
		"\u07fc\3\2\2\2\u07fa\u07f8\3\2\2\2\u07fb\u07fd\5\u0096L\2\u07fc\u07fb"+
		"\3\2\2\2\u07fc\u07fd\3\2\2\2\u07fd\u0089\3\2\2\2\u07fe\u0800\7f\2\2\u07ff"+
		"\u07fe\3\2\2\2\u07ff\u0800\3\2\2\2\u0800\u0801\3\2\2\2\u0801\u0802\t\25"+
		"\2\2\u0802\u0803\7\26\2\2\u0803\u0804\7\u00a5\2\2\u0804\u080d\t\26\2\2"+
		"\u0805\u0807\7f\2\2\u0806\u0805\3\2\2\2\u0806\u0807\3\2\2\2\u0807\u0808"+
		"\3\2\2\2\u0808\u0809\t\27\2\2\u0809\u080a\7\26\2\2\u080a\u080b\7\u00a5"+
		"\2\2\u080b\u080d\5\u00e2r\2\u080c\u07ff\3\2\2\2\u080c\u0806\3\2\2\2\u080d"+
		"\u008b\3\2\2\2\u080e\u080f\7p\2\2\u080f\u0810\7\36\2\2\u0810\u0815\5\u008e"+
		"H\2\u0811\u0812\7\b\2\2\u0812\u0814\5\u008eH\2\u0813\u0811\3\2\2\2\u0814"+
		"\u0817\3\2\2\2\u0815\u0813\3\2\2\2\u0815\u0816\3\2\2\2\u0816\u0836\3\2"+
		"\2\2\u0817\u0815\3\2\2\2\u0818\u0819\7p\2\2\u0819\u081a\7\36\2\2";
	private static final String _serializedATNSegment1 =
		"\u081a\u081f\5\u00dan\2\u081b\u081c\7\b\2\2\u081c\u081e\5\u00dan\2\u081d"+
		"\u081b\3\2\2\2\u081e\u0821\3\2\2\2\u081f\u081d\3\2\2\2\u081f\u0820\3\2"+
		"\2\2\u0820\u0833\3\2\2\2\u0821\u081f\3\2\2\2\u0822\u0823\7\u011f\2\2\u0823"+
		"\u0834\7\u00d7\2\2\u0824\u0825\7\u011f\2\2\u0825\u0834\79\2\2\u0826\u0827"+
		"\7q\2\2\u0827\u0828\7\u00e5\2\2\u0828\u0829\7\6\2\2\u0829\u082e\5\u0094"+
		"K\2\u082a\u082b\7\b\2\2\u082b\u082d\5\u0094K\2\u082c\u082a\3\2\2\2\u082d"+
		"\u0830\3\2\2\2\u082e\u082c\3\2\2\2\u082e\u082f\3\2\2\2\u082f\u0831\3\2"+
		"\2\2\u0830\u082e\3\2\2\2\u0831\u0832\7\7\2\2\u0832\u0834\3\2\2\2\u0833"+
		"\u0822\3\2\2\2\u0833\u0824\3\2\2\2\u0833\u0826\3\2\2\2\u0833\u0834\3\2"+
		"\2\2\u0834\u0836\3\2\2\2\u0835\u080e\3\2\2\2\u0835\u0818\3\2\2\2\u0836"+
		"\u008d\3\2\2\2\u0837\u083a\5\u0090I\2\u0838\u083a\5\u00dan\2\u0839\u0837"+
		"\3\2\2\2\u0839\u0838\3\2\2\2\u083a\u008f\3\2\2\2\u083b\u083c\t\30\2\2"+
		"\u083c\u083d\7\6\2\2\u083d\u0842\5\u0094K\2\u083e\u083f\7\b\2\2\u083f"+
		"\u0841\5\u0094K\2\u0840\u083e\3\2\2\2\u0841\u0844\3\2\2\2\u0842\u0840"+
		"\3\2\2\2\u0842\u0843\3\2\2\2\u0843\u0845\3\2\2\2\u0844\u0842\3\2\2\2\u0845"+
		"\u0846\7\7\2\2\u0846\u0855\3\2\2\2\u0847\u0848\7q\2\2\u0848\u0849\7\u00e5"+
		"\2\2\u0849\u084a\7\6\2\2\u084a\u084f\5\u0092J\2\u084b\u084c\7\b\2\2\u084c"+
		"\u084e\5\u0092J\2\u084d\u084b\3\2\2\2\u084e\u0851\3\2\2\2\u084f\u084d"+
		"\3\2\2\2\u084f\u0850\3\2\2\2\u0850\u0852\3\2\2\2\u0851\u084f\3\2\2\2\u0852"+
		"\u0853\7\7\2\2\u0853\u0855\3\2\2\2\u0854\u083b\3\2\2\2\u0854\u0847\3\2"+
		"\2\2\u0855\u0091\3\2\2\2\u0856\u0859\5\u0090I\2\u0857\u0859\5\u0094K\2"+
		"\u0858\u0856\3\2\2\2\u0858\u0857\3\2\2\2\u0859\u0093\3\2\2\2\u085a\u0863"+
		"\7\6\2\2\u085b\u0860\5\u00dan\2\u085c\u085d\7\b\2\2\u085d\u085f\5\u00da"+
		"n\2\u085e\u085c\3\2\2\2\u085f\u0862\3\2\2\2\u0860\u085e\3\2\2\2\u0860"+
		"\u0861\3\2\2\2\u0861\u0864\3\2\2\2\u0862\u0860\3\2\2\2\u0863\u085b\3\2"+
		"\2\2\u0863\u0864\3\2\2\2\u0864\u0865\3\2\2\2\u0865\u0868\7\7\2\2\u0866"+
		"\u0868\5\u00dan\2\u0867\u085a\3\2\2\2\u0867\u0866\3\2\2\2\u0868\u0095"+
		"\3\2\2\2\u0869\u086a\7\u00b9\2\2\u086a\u086b\7\6\2\2\u086b\u086c\5\u00d0"+
		"i\2\u086c\u086d\7f\2\2\u086d\u086e\5\u0098M\2\u086e\u086f\7w\2\2\u086f"+
		"\u0870\7\6\2\2\u0870\u0875\5\u009aN\2\u0871\u0872\7\b\2\2\u0872\u0874"+
		"\5\u009aN\2\u0873\u0871\3\2\2\2\u0874\u0877\3\2\2\2\u0875\u0873\3\2\2"+
		"\2\u0875\u0876\3\2\2\2\u0876\u0878\3\2\2\2\u0877\u0875\3\2\2\2\u0878\u0879"+
		"\7\7\2\2\u0879\u087a\7\7\2\2\u087a\u0097\3\2\2\2\u087b\u0888\5\u0124\u0093"+
		"\2\u087c\u087d\7\6\2\2\u087d\u0882\5\u0124\u0093\2\u087e\u087f\7\b\2\2"+
		"\u087f\u0881\5\u0124\u0093\2\u0880\u087e\3\2\2\2\u0881\u0884\3\2\2\2\u0882"+
		"\u0880\3\2\2\2\u0882\u0883\3\2\2\2\u0883\u0885\3\2\2\2\u0884\u0882\3\2"+
		"\2\2\u0885\u0886\7\7\2\2\u0886\u0888\3\2\2\2\u0887\u087b\3\2\2\2\u0887"+
		"\u087c\3\2\2\2\u0888\u0099\3\2\2\2\u0889\u088e\5\u00dan\2\u088a\u088c"+
		"\7\26\2\2\u088b\u088a\3\2\2\2\u088b\u088c\3\2\2\2\u088c\u088d\3\2\2\2"+
		"\u088d\u088f\5\u0124\u0093\2\u088e\u088b\3\2\2\2\u088e\u088f\3\2\2\2\u088f"+
		"\u009b\3\2\2\2\u0890\u0891\7\u0086\2\2\u0891\u0893\7\u0119\2\2\u0892\u0894"+
		"\7\u00ad\2\2\u0893\u0892\3\2\2\2\u0893\u0894\3\2\2\2\u0894\u0895\3\2\2"+
		"\2\u0895\u0896\5\u011e\u0090\2\u0896\u089f\7\6\2\2\u0897\u089c\5\u00da"+
		"n\2\u0898\u0899\7\b\2\2\u0899\u089b\5\u00dan\2\u089a\u0898\3\2\2\2\u089b"+
		"\u089e\3\2\2\2\u089c\u089a\3\2\2\2\u089c\u089d\3\2\2\2\u089d\u08a0\3\2"+
		"\2\2\u089e\u089c\3\2\2\2\u089f\u0897\3\2\2\2\u089f\u08a0\3\2\2\2\u08a0"+
		"\u08a1\3\2\2\2\u08a1\u08a2\7\7\2\2\u08a2\u08ae\5\u0124\u0093\2\u08a3\u08a5"+
		"\7\26\2\2\u08a4\u08a3\3\2\2\2\u08a4\u08a5\3\2\2\2\u08a5\u08a6\3\2\2\2"+
		"\u08a6\u08ab\5\u0124\u0093\2\u08a7\u08a8\7\b\2\2\u08a8\u08aa\5\u0124\u0093"+
		"\2\u08a9\u08a7\3\2\2\2\u08aa\u08ad\3\2\2\2\u08ab\u08a9\3\2\2\2\u08ab\u08ac"+
		"\3\2\2\2\u08ac\u08af\3\2\2\2\u08ad\u08ab\3\2\2\2\u08ae\u08a4\3\2\2\2\u08ae"+
		"\u08af\3\2\2\2\u08af\u009d\3\2\2\2\u08b0\u08b1\t\31\2\2\u08b1\u009f\3"+
		"\2\2\2\u08b2\u08b4\7\u0086\2\2\u08b3\u08b2\3\2\2\2\u08b3\u08b4\3\2\2\2"+
		"\u08b4\u08b5\3\2\2\2\u08b5\u08b9\5\u00b8]\2\u08b6\u08b8\5\u00a2R\2\u08b7"+
		"\u08b6\3\2\2\2\u08b8\u08bb\3\2\2\2\u08b9\u08b7\3\2\2\2\u08b9\u08ba\3\2"+
		"\2\2\u08ba\u00a1\3\2\2\2\u08bb\u08b9\3\2\2\2\u08bc\u08bd\5\u00a4S\2\u08bd"+
		"\u08bf\7\u0083\2\2\u08be\u08c0\7\u0086\2\2\u08bf\u08be\3\2\2\2\u08bf\u08c0"+
		"\3\2\2\2\u08c0\u08c1\3\2\2\2\u08c1\u08c3\5\u00b8]\2\u08c2\u08c4\5\u00a6"+
		"T\2\u08c3\u08c2\3\2\2\2\u08c3\u08c4\3\2\2\2\u08c4\u08ce\3\2\2\2\u08c5"+
		"\u08c6\7\u00a0\2\2\u08c6\u08c7\5\u00a4S\2\u08c7\u08c9\7\u0083\2\2\u08c8"+
		"\u08ca\7\u0086\2\2\u08c9\u08c8\3\2\2\2\u08c9\u08ca\3\2\2\2\u08ca\u08cb"+
		"\3\2\2\2\u08cb\u08cc\5\u00b8]\2\u08cc\u08ce\3\2\2\2\u08cd\u08bc\3\2\2"+
		"\2\u08cd\u08c5\3\2\2\2\u08ce\u00a3\3\2\2\2\u08cf\u08d1\7z\2\2\u08d0\u08cf"+
		"\3\2\2\2\u08d0\u08d1\3\2\2\2\u08d1\u08e8\3\2\2\2\u08d2\u08e8\78\2\2\u08d3"+
		"\u08d5\7\u0089\2\2\u08d4\u08d6\7\u00ad\2\2\u08d5\u08d4\3\2\2\2\u08d5\u08d6"+
		"\3\2\2\2\u08d6\u08e8\3\2\2\2\u08d7\u08d9\7\u0089\2\2\u08d8\u08d7\3\2\2"+
		"\2\u08d8\u08d9\3\2\2\2\u08d9\u08da\3\2\2\2\u08da\u08e8\7\u00de\2\2\u08db"+
		"\u08dd\7\u00d2\2\2\u08dc\u08de\7\u00ad\2\2\u08dd\u08dc\3\2\2\2\u08dd\u08de"+
		"\3\2\2\2\u08de\u08e8\3\2\2\2\u08df\u08e1\7k\2\2\u08e0\u08e2\7\u00ad\2"+
		"\2\u08e1\u08e0\3\2\2\2\u08e1\u08e2\3\2\2\2\u08e2\u08e8\3\2\2\2\u08e3\u08e5"+
		"\7\u0089\2\2\u08e4\u08e3\3\2\2\2\u08e4\u08e5\3\2\2\2\u08e5\u08e6\3\2\2"+
		"\2\u08e6\u08e8\7\22\2\2\u08e7\u08d0\3\2\2\2\u08e7\u08d2\3\2\2\2\u08e7"+
		"\u08d3\3\2\2\2\u08e7\u08d8\3\2\2\2\u08e7\u08db\3\2\2\2\u08e7\u08df\3\2"+
		"\2\2\u08e7\u08e4\3\2\2\2\u08e8\u00a5\3\2\2\2\u08e9\u08ea\7\u00a6\2\2\u08ea"+
		"\u08ee\5\u00dep\2\u08eb\u08ec\7\u0116\2\2\u08ec\u08ee\5\u00acW\2\u08ed"+
		"\u08e9\3\2\2\2\u08ed\u08eb\3\2\2\2\u08ee\u00a7\3\2\2\2\u08ef\u08f0\7\u00f7"+
		"\2\2\u08f0\u08f2\7\6\2\2\u08f1\u08f3\5\u00aaV\2\u08f2\u08f1\3\2\2\2\u08f2"+
		"\u08f3\3\2\2\2\u08f3\u08f4\3\2\2\2\u08f4\u08f9\7\7\2\2\u08f5\u08f6\7\u00cc"+
		"\2\2\u08f6\u08f7\7\6\2\2\u08f7\u08f8\7\u013d\2\2\u08f8\u08fa\7\7\2\2\u08f9"+
		"\u08f5\3\2\2\2\u08f9\u08fa\3\2\2\2\u08fa\u00a9\3\2\2\2\u08fb\u08fd\7\u012c"+
		"\2\2\u08fc\u08fb\3\2\2\2\u08fc\u08fd\3\2\2\2\u08fd\u08fe\3\2\2\2\u08fe"+
		"\u08ff\t\32\2\2\u08ff\u0914\7\u00b8\2\2\u0900\u0901\5\u00dan\2\u0901\u0902"+
		"\7\u00d9\2\2\u0902\u0914\3\2\2\2\u0903\u0904\7\34\2\2\u0904\u0905\7\u013d"+
		"\2\2\u0905\u0906\7\u00ac\2\2\u0906\u0907\7\u00a5\2\2\u0907\u0910\7\u013d"+
		"\2\2\u0908\u090e\7\u00a6\2\2\u0909\u090f\5\u0124\u0093\2\u090a\u090b\5"+
		"\u011e\u0090\2\u090b\u090c\7\6\2\2\u090c\u090d\7\7\2\2\u090d\u090f\3\2"+
		"\2\2\u090e\u0909\3\2\2\2\u090e\u090a\3\2\2\2\u090f\u0911\3\2\2\2\u0910"+
		"\u0908\3\2\2\2\u0910\u0911\3\2\2\2\u0911\u0914\3\2\2\2\u0912\u0914\5\u00da"+
		"n\2\u0913\u08fc\3\2\2\2\u0913\u0900\3\2\2\2\u0913\u0903\3\2\2\2\u0913"+
		"\u0912\3\2\2\2\u0914\u00ab\3\2\2\2\u0915\u0916\7\6\2\2\u0916\u0917\5\u00ae"+
		"X\2\u0917\u0918\7\7\2\2\u0918\u00ad\3\2\2\2\u0919\u091e\5\u0120\u0091"+
		"\2\u091a\u091b\7\b\2\2\u091b\u091d\5\u0120\u0091\2\u091c\u091a\3\2\2\2"+
		"\u091d\u0920\3\2\2\2\u091e\u091c\3\2\2\2\u091e\u091f\3\2\2\2\u091f\u00af"+
		"\3\2\2\2\u0920\u091e\3\2\2\2\u0921\u0922\7\6\2\2\u0922\u0927\5\u00b2Z"+
		"\2\u0923\u0924\7\b\2\2\u0924\u0926\5\u00b2Z\2\u0925\u0923\3\2\2\2\u0926"+
		"\u0929\3\2\2\2\u0927\u0925\3\2\2\2\u0927\u0928\3\2\2\2\u0928\u092a\3\2"+
		"\2\2\u0929\u0927\3\2\2\2\u092a\u092b\7\7\2\2\u092b\u00b1\3\2\2\2\u092c"+
		"\u092e\5\u0120\u0091\2\u092d\u092f\t\23\2\2\u092e\u092d\3\2\2\2\u092e"+
		"\u092f\3\2\2\2\u092f\u00b3\3\2\2\2\u0930\u0931\7\6\2\2\u0931\u0936\5\u00b6"+
		"\\\2\u0932\u0933\7\b\2\2\u0933\u0935\5\u00b6\\\2\u0934\u0932\3\2\2\2\u0935"+
		"\u0938\3\2\2\2\u0936\u0934\3\2\2\2\u0936\u0937\3\2\2\2\u0937\u0939\3\2"+
		"\2\2\u0938\u0936\3\2\2\2\u0939\u093a\7\7\2\2\u093a\u00b5\3\2\2\2\u093b"+
		"\u093d\5\u0124\u0093\2\u093c\u093e\5,\27\2\u093d\u093c\3\2\2\2\u093d\u093e"+
		"\3\2\2\2\u093e\u00b7\3\2\2\2\u093f\u0941\5\u00c4c\2\u0940\u0942\5\u008a"+
		"F\2\u0941\u0940\3\2\2\2\u0941\u0942\3\2\2\2\u0942\u0944\3\2\2\2\u0943"+
		"\u0945\5\u00a8U\2\u0944\u0943\3\2\2\2\u0944\u0945\3\2\2\2\u0945\u0946"+
		"\3\2\2\2\u0946\u0947\5\u00be`\2\u0947\u095b\3\2\2\2\u0948\u0949\7\6\2"+
		"\2\u0949\u094a\5.\30\2\u094a\u094c\7\7\2\2\u094b\u094d\5\u00a8U\2\u094c"+
		"\u094b\3\2\2\2\u094c\u094d\3\2\2\2\u094d\u094e\3\2\2\2\u094e\u094f\5\u00be"+
		"`\2\u094f\u095b\3\2\2\2\u0950\u0951\7\6\2\2\u0951\u0952\5\u00a0Q\2\u0952"+
		"\u0954\7\7\2\2\u0953\u0955\5\u00a8U\2\u0954\u0953\3\2\2\2\u0954\u0955"+
		"\3\2\2\2\u0955\u0956\3\2\2\2\u0956\u0957\5\u00be`\2\u0957\u095b\3\2\2"+
		"\2\u0958\u095b\5\u00ba^\2\u0959\u095b\5\u00bc_\2\u095a\u093f\3\2\2\2\u095a"+
		"\u0948\3\2\2\2\u095a\u0950\3\2\2\2\u095a\u0958\3\2\2\2\u095a\u0959\3\2"+
		"\2\2\u095b\u00b9\3\2\2\2\u095c\u095d\7\u0117\2\2\u095d\u0962\5\u00dan"+
		"\2\u095e\u095f\7\b\2\2\u095f\u0961\5\u00dan\2\u0960\u095e\3\2\2\2\u0961"+
		"\u0964\3\2\2\2\u0962\u0960\3\2\2\2\u0962\u0963\3\2\2\2\u0963\u0965\3\2"+
		"\2\2\u0964\u0962\3\2\2\2\u0965\u0966\5\u00be`\2\u0966\u00bb\3\2\2\2\u0967"+
		"\u0968\5\u011c\u008f\2\u0968\u0971\7\6\2\2\u0969\u096e\5\u00dan\2\u096a"+
		"\u096b\7\b\2\2\u096b\u096d\5\u00dan\2\u096c\u096a\3\2\2\2\u096d\u0970"+
		"\3\2\2\2\u096e\u096c\3\2\2\2\u096e\u096f\3\2\2\2\u096f\u0972\3\2\2\2\u0970"+
		"\u096e\3\2\2\2\u0971\u0969\3\2\2\2\u0971\u0972\3\2\2\2\u0972\u0973\3\2"+
		"\2\2\u0973\u0974\7\7\2\2\u0974\u0975\5\u00be`\2\u0975\u00bd\3\2\2\2\u0976"+
		"\u0978\7\26\2\2\u0977\u0976\3\2\2\2\u0977\u0978\3\2\2\2\u0978\u0979\3"+
		"\2\2\2\u0979\u097b\5\u0126\u0094\2\u097a\u097c\5\u00acW\2\u097b\u097a"+
		"\3\2\2\2\u097b\u097c\3\2\2\2\u097c\u097e\3\2\2\2\u097d\u0977\3\2\2\2\u097d"+
		"\u097e\3\2\2\2\u097e\u00bf\3\2\2\2\u097f\u0980\7\u00d8\2\2\u0980\u0981"+
		"\7h\2\2\u0981\u0982\7\u00e0\2\2\u0982\u0986\7\u0139\2\2\u0983\u0984\7"+
		"\u011f\2\2\u0984\u0985\7\u00e1\2\2\u0985\u0987\5H%\2\u0986\u0983\3\2\2"+
		"\2\u0986\u0987\3\2\2\2\u0987\u09b1\3\2\2\2\u0988\u0989\7\u00d8\2\2\u0989"+
		"\u098a\7h\2\2\u098a\u0994\7I\2\2\u098b\u098c\7a\2\2\u098c\u098d\7\u00fa"+
		"\2\2\u098d\u098e\7\36\2\2\u098e\u0992\7\u0139\2\2\u098f\u0990\7V\2\2\u0990"+
		"\u0991\7\36\2\2\u0991\u0993\7\u0139\2\2\u0992\u098f\3\2\2\2\u0992\u0993"+
		"\3\2\2\2\u0993\u0995\3\2\2\2\u0994\u098b\3\2\2\2\u0994\u0995\3\2\2\2\u0995"+
		"\u099b\3\2\2\2\u0996\u0997\7,\2\2\u0997\u0998\7\u0082\2\2\u0998\u0999"+
		"\7\u00fa\2\2\u0999\u099a\7\36\2\2\u099a\u099c\7\u0139\2\2\u099b\u0996"+
		"\3\2\2\2\u099b\u099c\3\2\2\2\u099c\u09a2\3\2\2\2\u099d\u099e\7\u0096\2"+
		"\2\u099e\u099f\7\u0084\2\2\u099f\u09a0\7\u00fa\2\2\u09a0\u09a1\7\36\2"+
		"\2\u09a1\u09a3\7\u0139\2\2\u09a2\u099d\3\2\2\2\u09a2\u09a3\3\2\2\2\u09a3"+
		"\u09a8\3\2\2\2\u09a4\u09a5\7\u008d\2\2\u09a5\u09a6\7\u00fa\2\2\u09a6\u09a7"+
		"\7\36\2\2\u09a7\u09a9\7\u0139\2\2\u09a8\u09a4\3\2\2\2\u09a8\u09a9\3\2"+
		"\2\2\u09a9\u09ae\3\2\2\2\u09aa\u09ab\7\u00a3\2\2\u09ab\u09ac\7G\2\2\u09ac"+
		"\u09ad\7\26\2\2\u09ad\u09af\7\u0139\2\2\u09ae\u09aa\3\2\2\2\u09ae\u09af"+
		"\3\2\2\2\u09af\u09b1\3\2\2\2\u09b0\u097f\3\2\2\2\u09b0\u0988\3\2\2\2\u09b1"+
		"\u00c1\3\2\2\2\u09b2\u09b7\5\u00c4c\2\u09b3\u09b4\7\b\2\2\u09b4\u09b6"+
		"\5\u00c4c\2\u09b5\u09b3\3\2\2\2\u09b6\u09b9\3\2\2\2\u09b7\u09b5\3\2\2"+
		"\2\u09b7\u09b8\3\2\2\2\u09b8\u00c3\3\2\2\2\u09b9\u09b7\3\2\2\2\u09ba\u09bf"+
		"\5\u0120\u0091\2\u09bb\u09bc\7\t\2\2\u09bc\u09be\5\u0120\u0091\2\u09bd"+
		"\u09bb\3\2\2\2\u09be\u09c1\3\2\2\2\u09bf\u09bd\3\2\2\2\u09bf\u09c0\3\2"+
		"\2\2\u09c0\u00c5\3\2\2\2\u09c1\u09bf\3\2\2\2\u09c2\u09c7\5\u00c8e\2\u09c3"+
		"\u09c4\7\b\2\2\u09c4\u09c6\5\u00c8e\2\u09c5\u09c3\3\2\2\2\u09c6\u09c9"+
		"\3\2\2\2\u09c7\u09c5\3\2\2\2\u09c7\u09c8\3\2\2\2\u09c8\u00c7\3\2\2\2\u09c9"+
		"\u09c7\3\2\2\2\u09ca\u09cd\5\u00c4c\2\u09cb\u09cc\7\u00a9\2\2\u09cc\u09ce"+
		"\5H%\2\u09cd\u09cb\3\2\2\2\u09cd\u09ce\3\2\2\2\u09ce\u00c9\3\2\2\2\u09cf"+
		"\u09d0\5\u0120\u0091\2\u09d0\u09d1\7\t\2\2\u09d1\u09d3\3\2\2\2\u09d2\u09cf"+
		"\3\2\2\2\u09d2\u09d3\3\2\2\2\u09d3\u09d4\3\2\2\2\u09d4\u09d5\5\u0120\u0091"+
		"\2\u09d5\u00cb\3\2\2\2\u09d6\u09d7\5\u0120\u0091\2\u09d7\u09d8\7\t\2\2"+
		"\u09d8\u09da\3\2\2\2\u09d9\u09d6\3\2\2\2\u09d9\u09da\3\2\2\2\u09da\u09db"+
		"\3\2\2\2\u09db\u09dc\5\u0120\u0091\2\u09dc\u00cd\3\2\2\2\u09dd\u09e5\5"+
		"\u00dan\2\u09de\u09e0\7\26\2\2\u09df\u09de\3\2\2\2\u09df\u09e0\3\2\2\2"+
		"\u09e0\u09e3\3\2\2\2\u09e1\u09e4\5\u0120\u0091\2\u09e2\u09e4\5\u00acW"+
		"\2\u09e3\u09e1\3\2\2\2\u09e3\u09e2\3\2\2\2\u09e4\u09e6\3\2\2\2\u09e5\u09df"+
		"\3\2\2\2\u09e5\u09e6\3\2\2\2\u09e6\u00cf\3\2\2\2\u09e7\u09ec\5\u00ceh"+
		"\2\u09e8\u09e9\7\b\2\2\u09e9\u09eb\5\u00ceh\2\u09ea\u09e8\3\2\2\2\u09eb"+
		"\u09ee\3\2\2\2\u09ec\u09ea\3\2\2\2\u09ec\u09ed\3\2\2\2\u09ed\u00d1\3\2"+
		"\2\2\u09ee\u09ec\3\2\2\2\u09ef\u09f0\7\6\2\2\u09f0\u09f5\5\u00d4k\2\u09f1"+
		"\u09f2\7\b\2\2\u09f2\u09f4\5\u00d4k\2\u09f3\u09f1\3\2\2\2\u09f4\u09f7"+
		"\3\2\2\2\u09f5\u09f3\3\2\2\2\u09f5\u09f6\3\2\2\2\u09f6\u09f8\3\2\2\2\u09f7"+
		"\u09f5\3\2\2\2\u09f8\u09f9\7\7\2\2\u09f9\u00d3\3\2\2\2\u09fa\u09fd\5\u00d6"+
		"l\2\u09fb\u09fd\5\u0108\u0085\2\u09fc\u09fa\3\2\2\2\u09fc\u09fb\3\2\2"+
		"\2\u09fd\u00d5\3\2\2\2\u09fe\u0a0c\5\u011e\u0090\2\u09ff\u0a00\5\u0124"+
		"\u0093\2\u0a00\u0a01\7\6\2\2\u0a01\u0a06\5\u00d8m\2\u0a02\u0a03\7\b\2"+
		"\2\u0a03\u0a05\5\u00d8m\2\u0a04\u0a02\3\2\2\2\u0a05\u0a08\3\2\2\2\u0a06"+
		"\u0a04\3\2\2\2\u0a06\u0a07\3\2\2\2\u0a07\u0a09\3\2\2\2\u0a08\u0a06\3\2"+
		"\2\2\u0a09\u0a0a\7\7\2\2\u0a0a\u0a0c\3\2\2\2\u0a0b\u09fe\3\2\2\2\u0a0b"+
		"\u09ff\3\2\2\2\u0a0c\u00d7\3\2\2\2\u0a0d\u0a10\5\u011e\u0090\2\u0a0e\u0a10"+
		"\5\u00e8u\2\u0a0f\u0a0d\3\2\2\2\u0a0f\u0a0e\3\2\2\2\u0a10\u00d9\3\2\2"+
		"\2\u0a11\u0a12\5\u00dep\2\u0a12\u00db\3\2\2\2\u0a13\u0a18\5\u00dan\2\u0a14"+
		"\u0a15\7\b\2\2\u0a15\u0a17\5\u00dan\2\u0a16\u0a14\3\2\2\2\u0a17\u0a1a"+
		"\3\2\2\2\u0a18\u0a16\3\2\2\2\u0a18\u0a19\3\2\2\2\u0a19\u00dd\3\2\2\2\u0a1a"+
		"\u0a18\3\2\2\2\u0a1b\u0a1c\bp\1\2\u0a1c\u0a1d\7\u00a2\2\2\u0a1d\u0a28"+
		"\5\u00dep\7\u0a1e\u0a1f\7Y\2\2\u0a1f\u0a20\7\6\2\2\u0a20\u0a21\5.\30\2"+
		"\u0a21\u0a22\7\7\2\2\u0a22\u0a28\3\2\2\2\u0a23\u0a25\5\u00e2r\2\u0a24"+
		"\u0a26\5\u00e0q\2\u0a25\u0a24\3\2\2\2\u0a25\u0a26\3\2\2\2\u0a26\u0a28"+
		"\3\2\2\2\u0a27\u0a1b\3\2\2\2\u0a27\u0a1e\3\2\2\2\u0a27\u0a23\3\2\2\2\u0a28"+
		"\u0a31\3\2\2\2\u0a29\u0a2a\f\4\2\2\u0a2a\u0a2b\7\21\2\2\u0a2b\u0a30\5"+
		"\u00dep\5\u0a2c\u0a2d\f\3\2\2\u0a2d\u0a2e\7\u00aa\2\2\u0a2e\u0a30\5\u00de"+
		"p\4\u0a2f\u0a29\3\2\2\2\u0a2f\u0a2c\3\2\2\2\u0a30\u0a33\3\2\2\2\u0a31"+
		"\u0a2f\3\2\2\2\u0a31\u0a32\3\2\2\2\u0a32\u00df\3\2\2\2\u0a33\u0a31\3\2"+
		"\2\2\u0a34\u0a36\7\u00a2\2\2\u0a35\u0a34\3\2\2\2\u0a35\u0a36\3\2\2\2\u0a36"+
		"\u0a37\3\2\2\2\u0a37\u0a38\7\32\2\2\u0a38\u0a39\5\u00e2r\2\u0a39\u0a3a"+
		"\7\21\2\2\u0a3a\u0a3b\5\u00e2r\2\u0a3b\u0a87\3\2\2\2\u0a3c\u0a3e\7\u00a2"+
		"\2\2\u0a3d\u0a3c\3\2\2\2\u0a3d\u0a3e\3\2\2\2\u0a3e\u0a3f\3\2\2\2\u0a3f"+
		"\u0a40\7w\2\2\u0a40\u0a41\7\6\2\2\u0a41\u0a46\5\u00dan\2\u0a42\u0a43\7"+
		"\b\2\2\u0a43\u0a45\5\u00dan\2\u0a44\u0a42\3\2\2\2\u0a45\u0a48\3\2\2\2"+
		"\u0a46\u0a44\3\2\2\2\u0a46\u0a47\3\2\2\2\u0a47\u0a49\3\2\2\2\u0a48\u0a46"+
		"\3\2\2\2\u0a49\u0a4a\7\7\2\2\u0a4a\u0a87\3\2\2\2\u0a4b\u0a4d\7\u00a2\2"+
		"\2\u0a4c\u0a4b\3\2\2\2\u0a4c\u0a4d\3\2\2\2\u0a4d\u0a4e\3\2\2\2\u0a4e\u0a4f"+
		"\7w\2\2\u0a4f\u0a50\7\6\2\2\u0a50\u0a51\5.\30\2\u0a51\u0a52\7\7\2\2\u0a52"+
		"\u0a87\3\2\2\2\u0a53\u0a55\7\u00a2\2\2\u0a54\u0a53\3\2\2\2\u0a54\u0a55"+
		"\3\2\2\2\u0a55\u0a56\3\2\2\2\u0a56\u0a57\7\u00d3\2\2\u0a57\u0a87\5\u00e2"+
		"r\2\u0a58\u0a5a\7\u00a2\2\2\u0a59\u0a58\3\2\2\2\u0a59\u0a5a\3\2\2\2\u0a5a"+
		"\u0a5b\3\2\2\2\u0a5b\u0a5c\t\33\2\2\u0a5c\u0a6a\t\34\2\2\u0a5d\u0a5e\7"+
		"\6\2\2\u0a5e\u0a6b\7\7\2\2\u0a5f\u0a60\7\6\2\2\u0a60\u0a65\5\u00dan\2"+
		"\u0a61\u0a62\7\b\2\2\u0a62\u0a64\5\u00dan\2\u0a63\u0a61\3\2\2\2\u0a64"+
		"\u0a67\3\2\2\2\u0a65\u0a63\3\2\2\2\u0a65\u0a66\3\2\2\2\u0a66\u0a68\3\2"+
		"\2\2\u0a67\u0a65\3\2\2\2\u0a68\u0a69\7\7\2\2\u0a69\u0a6b\3\2\2\2\u0a6a"+
		"\u0a5d\3\2\2\2\u0a6a\u0a5f\3\2\2\2\u0a6b\u0a87\3\2\2\2\u0a6c\u0a6e\7\u00a2"+
		"\2\2\u0a6d\u0a6c\3\2\2\2\u0a6d\u0a6e\3\2\2\2\u0a6e\u0a6f\3\2\2\2\u0a6f"+
		"\u0a70\t\33\2\2\u0a70\u0a73\5\u00e2r\2\u0a71\u0a72\7U\2\2\u0a72\u0a74"+
		"\7\u0139\2\2\u0a73\u0a71\3\2\2\2\u0a73\u0a74\3\2\2\2\u0a74\u0a87\3\2\2"+
		"\2\u0a75\u0a77\7\u0081\2\2\u0a76\u0a78\7\u00a2\2\2\u0a77\u0a76\3\2\2\2"+
		"\u0a77\u0a78\3\2\2\2\u0a78\u0a79\3\2\2\2\u0a79\u0a87\7\u00a3\2\2\u0a7a"+
		"\u0a7c\7\u0081\2\2\u0a7b\u0a7d\7\u00a2\2\2\u0a7c\u0a7b\3\2\2\2\u0a7c\u0a7d"+
		"\3\2\2\2\u0a7d\u0a7e\3\2\2\2\u0a7e\u0a87\t\35\2\2\u0a7f\u0a81\7\u0081"+
		"\2\2\u0a80\u0a82\7\u00a2\2\2\u0a81\u0a80\3\2\2\2\u0a81\u0a82\3\2\2\2\u0a82"+
		"\u0a83\3\2\2\2\u0a83\u0a84\7O\2\2\u0a84\u0a85\7j\2\2\u0a85\u0a87\5\u00e2"+
		"r\2\u0a86\u0a35\3\2\2\2\u0a86\u0a3d\3\2\2\2\u0a86\u0a4c\3\2\2\2\u0a86"+
		"\u0a54\3\2\2\2\u0a86\u0a59\3\2\2\2\u0a86\u0a6d\3\2\2\2\u0a86\u0a75\3\2"+
		"\2\2\u0a86\u0a7a\3\2\2\2\u0a86\u0a7f\3\2\2\2\u0a87\u00e1\3\2\2\2\u0a88"+
		"\u0a89\br\1\2\u0a89\u0a8d\5\u00e6t\2\u0a8a\u0a8b\t\36\2\2\u0a8b\u0a8d"+
		"\5\u00e2r\t\u0a8c\u0a88\3\2\2\2\u0a8c\u0a8a\3\2\2\2\u0a8d\u0aa3\3\2\2"+
		"\2\u0a8e\u0a8f\f\b\2\2\u0a8f\u0a90\t\37\2\2\u0a90\u0aa2\5\u00e2r\t\u0a91"+
		"\u0a92\f\7\2\2\u0a92\u0a93\t \2\2\u0a93\u0aa2\5\u00e2r\b\u0a94\u0a95\f"+
		"\6\2\2\u0a95\u0a96\7\u0131\2\2\u0a96\u0aa2\5\u00e2r\7\u0a97\u0a98\f\5"+
		"\2\2\u0a98\u0a99\7\u0134\2\2\u0a99\u0aa2\5\u00e2r\6\u0a9a\u0a9b\f\4\2"+
		"\2\u0a9b\u0a9c\7\u0132\2\2\u0a9c\u0aa2\5\u00e2r\5\u0a9d\u0a9e\f\3\2\2"+
		"\u0a9e\u0a9f\5\u00eav\2\u0a9f\u0aa0\5\u00e2r\4\u0aa0\u0aa2\3\2\2\2\u0aa1"+
		"\u0a8e\3\2\2\2\u0aa1\u0a91\3\2\2\2\u0aa1\u0a94\3\2\2\2\u0aa1\u0a97\3\2"+
		"\2\2\u0aa1\u0a9a\3\2\2\2\u0aa1\u0a9d\3\2\2\2\u0aa2\u0aa5\3\2\2\2\u0aa3"+
		"\u0aa1\3\2\2\2\u0aa3\u0aa4\3\2\2\2\u0aa4\u00e3\3\2\2\2\u0aa5\u0aa3\3\2"+
		"\2\2\u0aa6\u0aa7\t!\2\2\u0aa7\u00e5\3\2\2\2\u0aa8\u0aa9\bt\1\2\u0aa9\u0b86"+
		"\t\"\2\2\u0aaa\u0aab\t#\2\2\u0aab\u0aac\7\6\2\2\u0aac\u0aad\5\u00e4s\2"+
		"\u0aad\u0aae\7\b\2\2\u0aae\u0aaf\5\u00e2r\2\u0aaf\u0ab0\7\b\2\2\u0ab0"+
		"\u0ab1\5\u00e2r\2\u0ab1\u0ab2\7\7\2\2\u0ab2\u0b86\3\2\2\2\u0ab3\u0ab4"+
		"\t$\2\2\u0ab4\u0ab5\7\6\2\2\u0ab5\u0ab6\5\u00e4s\2\u0ab6\u0ab7\7\b\2\2"+
		"\u0ab7\u0ab8\5\u00e2r\2\u0ab8\u0ab9\7\b\2\2\u0ab9\u0aba\5\u00e2r\2\u0aba"+
		"\u0abb\7\7\2\2\u0abb\u0b86\3\2\2\2\u0abc\u0abe\7!\2\2\u0abd\u0abf\5\u010e"+
		"\u0088\2\u0abe\u0abd\3\2\2\2\u0abf\u0ac0\3\2\2\2\u0ac0\u0abe\3\2\2\2\u0ac0"+
		"\u0ac1\3\2\2\2\u0ac1\u0ac4\3\2\2\2\u0ac2\u0ac3\7S\2\2\u0ac3\u0ac5\5\u00da"+
		"n\2\u0ac4\u0ac2\3\2\2\2\u0ac4\u0ac5\3\2\2\2\u0ac5\u0ac6\3\2\2\2\u0ac6"+
		"\u0ac7\7T\2\2\u0ac7\u0b86\3\2\2\2\u0ac8\u0ac9\7!\2\2\u0ac9\u0acb\5\u00da"+
		"n\2\u0aca\u0acc\5\u010e\u0088\2\u0acb\u0aca\3\2\2\2\u0acc\u0acd\3\2\2"+
		"\2\u0acd\u0acb\3\2\2\2\u0acd\u0ace\3\2\2\2\u0ace\u0ad1\3\2\2\2\u0acf\u0ad0"+
		"\7S\2\2\u0ad0\u0ad2\5\u00dan\2\u0ad1\u0acf\3\2\2\2\u0ad1\u0ad2\3\2\2\2"+
		"\u0ad2\u0ad3\3\2\2\2\u0ad3\u0ad4\7T\2\2\u0ad4\u0b86\3\2\2\2\u0ad5\u0ad6"+
		"\t%\2\2\u0ad6\u0ad7\7\6\2\2\u0ad7\u0ad8\5\u00dan\2\u0ad8\u0ad9\7\26\2"+
		"\2\u0ad9\u0ada\5\u0100\u0081\2\u0ada\u0adb\7\7\2\2\u0adb\u0b86\3\2\2\2"+
		"\u0adc\u0add\7\u00ef\2\2\u0add\u0ae6\7\6\2\2\u0ade\u0ae3\5\u00ceh\2\u0adf"+
		"\u0ae0\7\b\2\2\u0ae0\u0ae2\5\u00ceh\2\u0ae1\u0adf\3\2\2\2\u0ae2\u0ae5"+
		"\3\2\2\2\u0ae3\u0ae1\3\2\2\2\u0ae3\u0ae4\3\2\2\2\u0ae4\u0ae7\3\2\2\2\u0ae5"+
		"\u0ae3\3\2\2\2\u0ae6\u0ade\3\2\2\2\u0ae6\u0ae7\3\2\2\2\u0ae7\u0ae8\3\2"+
		"\2\2\u0ae8\u0b86\7\7\2\2\u0ae9\u0aea\7d\2\2\u0aea\u0aeb\7\6\2\2\u0aeb"+
		"\u0aee\5\u00dan\2\u0aec\u0aed\7u\2\2\u0aed\u0aef\7\u00a4\2\2\u0aee\u0aec"+
		"\3\2\2\2\u0aee\u0aef\3\2\2\2\u0aef\u0af0\3\2\2\2\u0af0\u0af1\7\7\2\2\u0af1"+
		"\u0b86\3\2\2\2\u0af2\u0af3\7\u0085\2\2\u0af3\u0af4\7\6\2\2\u0af4\u0af7"+
		"\5\u00dan\2\u0af5\u0af6\7u\2\2\u0af6\u0af8\7\u00a4\2\2\u0af7\u0af5\3\2"+
		"\2\2\u0af7\u0af8\3\2\2\2\u0af8\u0af9\3\2\2\2\u0af9\u0afa\7\7\2\2\u0afa"+
		"\u0b86\3\2\2\2\u0afb\u0afc\7\u00bb\2\2\u0afc\u0afd\7\6\2\2\u0afd\u0afe"+
		"\5\u00e2r\2\u0afe\u0aff\7w\2\2\u0aff\u0b00\5\u00e2r\2\u0b00\u0b01\7\7"+
		"\2\2\u0b01\u0b86\3\2\2\2\u0b02\u0b86\5\u00e8u\2\u0b03\u0b86\7\u012d\2"+
		"\2\u0b04\u0b05\5\u011e\u0090\2\u0b05\u0b06\7\t\2\2\u0b06\u0b07\7\u012d"+
		"\2\2\u0b07\u0b86\3\2\2\2\u0b08\u0b09\7\6\2\2\u0b09\u0b0c\5\u00ceh\2\u0b0a"+
		"\u0b0b\7\b\2\2\u0b0b\u0b0d\5\u00ceh\2\u0b0c\u0b0a\3\2\2\2\u0b0d\u0b0e"+
		"\3\2\2\2\u0b0e\u0b0c\3\2\2\2\u0b0e\u0b0f\3\2\2\2\u0b0f\u0b10\3\2\2\2\u0b10"+
		"\u0b11\7\7\2\2\u0b11\u0b86\3\2\2\2\u0b12\u0b13\7\6\2\2\u0b13\u0b14\5."+
		"\30\2\u0b14\u0b15\7\7\2\2\u0b15\u0b86\3\2\2\2\u0b16\u0b17\5\u011c\u008f"+
		"\2\u0b17\u0b23\7\6\2\2\u0b18\u0b1a\5\u009eP\2\u0b19\u0b18\3\2\2\2\u0b19"+
		"\u0b1a\3\2\2\2\u0b1a\u0b1b\3\2\2\2\u0b1b\u0b20\5\u00dan\2\u0b1c\u0b1d"+
		"\7\b\2\2\u0b1d\u0b1f\5\u00dan\2\u0b1e\u0b1c\3\2\2\2\u0b1f\u0b22\3\2\2"+
		"\2\u0b20\u0b1e\3\2\2\2\u0b20\u0b21\3\2\2\2\u0b21\u0b24\3\2\2\2\u0b22\u0b20"+
		"\3\2\2\2\u0b23\u0b19\3\2\2\2\u0b23\u0b24\3\2\2\2\u0b24\u0b25\3\2\2\2\u0b25"+
		"\u0b2c\7\7\2\2\u0b26\u0b27\7b\2\2\u0b27\u0b28\7\6\2\2\u0b28\u0b29\7\u011d"+
		"\2\2\u0b29\u0b2a\5\u00dep\2\u0b2a\u0b2b\7\7\2\2\u0b2b\u0b2d\3\2\2\2\u0b2c"+
		"\u0b26\3\2\2\2\u0b2c\u0b2d\3\2\2\2\u0b2d\u0b30\3\2\2\2\u0b2e\u0b2f\t&"+
		"\2\2\u0b2f\u0b31\7\u00a4\2\2\u0b30\u0b2e\3\2\2\2\u0b30\u0b31\3\2\2\2\u0b31"+
		"\u0b34\3\2\2\2\u0b32\u0b33\7\u00af\2\2\u0b33\u0b35\5\u0114\u008b\2\u0b34"+
		"\u0b32\3\2\2\2\u0b34\u0b35\3\2\2\2\u0b35\u0b86\3\2\2\2\u0b36\u0b37\5\u0124"+
		"\u0093\2\u0b37\u0b38\7\u0136\2\2\u0b38\u0b39\5\u00dan\2\u0b39\u0b86\3"+
		"\2\2\2\u0b3a\u0b3b\7\6\2\2\u0b3b\u0b3e\5\u0124\u0093\2\u0b3c\u0b3d\7\b"+
		"\2\2\u0b3d\u0b3f\5\u0124\u0093\2\u0b3e\u0b3c\3\2\2\2\u0b3f\u0b40\3\2\2"+
		"\2\u0b40\u0b3e\3\2\2\2\u0b40\u0b41\3\2\2\2\u0b41\u0b42\3\2\2\2\u0b42\u0b43"+
		"\7\7\2\2\u0b43\u0b44\7\u0136\2\2\u0b44\u0b45\5\u00dan\2\u0b45\u0b86\3"+
		"\2\2\2\u0b46\u0b86\5\u0124\u0093\2\u0b47\u0b48\7\6\2\2\u0b48\u0b49\5\u00da"+
		"n\2\u0b49\u0b4a\7\7\2\2\u0b4a\u0b86\3\2\2\2\u0b4b\u0b4c\7^\2\2\u0b4c\u0b4d"+
		"\7\6\2\2\u0b4d\u0b4e\5\u0124\u0093\2\u0b4e\u0b4f\7j\2\2\u0b4f\u0b50\5"+
		"\u00e2r\2\u0b50\u0b51\7\7\2\2\u0b51\u0b86\3\2\2\2\u0b52\u0b53\t\'\2\2"+
		"\u0b53\u0b54\7\6\2\2\u0b54\u0b55\5\u00e2r\2\u0b55\u0b56\t(\2\2\u0b56\u0b59"+
		"\5\u00e2r\2\u0b57\u0b58\t)\2\2\u0b58\u0b5a\5\u00e2r\2\u0b59\u0b57\3\2"+
		"\2\2\u0b59\u0b5a\3\2\2\2\u0b5a\u0b5b\3\2\2\2\u0b5b\u0b5c\7\7\2\2\u0b5c"+
		"\u0b86\3\2\2\2\u0b5d\u0b5e\7\u0106\2\2\u0b5e\u0b60\7\6\2\2\u0b5f\u0b61"+
		"\t*\2\2\u0b60\u0b5f\3\2\2\2\u0b60\u0b61\3\2\2\2\u0b61\u0b63\3\2\2\2\u0b62"+
		"\u0b64\5\u00e2r\2\u0b63\u0b62\3\2\2\2\u0b63\u0b64\3\2\2\2\u0b64\u0b65"+
		"\3\2\2\2\u0b65\u0b66\7j\2\2\u0b66\u0b67\5\u00e2r\2\u0b67\u0b68\7\7\2\2"+
		"\u0b68\u0b86\3\2\2\2\u0b69\u0b6a\7\u00b1\2\2\u0b6a\u0b6b\7\6\2\2\u0b6b"+
		"\u0b6c\5\u00e2r\2\u0b6c\u0b6d\7\u00ba\2\2\u0b6d\u0b6e\5\u00e2r\2\u0b6e"+
		"\u0b6f\7j\2\2\u0b6f\u0b72\5\u00e2r\2\u0b70\u0b71\7f\2\2\u0b71\u0b73\5"+
		"\u00e2r\2\u0b72\u0b70\3\2\2\2\u0b72\u0b73\3\2\2\2\u0b73\u0b74\3\2\2\2"+
		"\u0b74\u0b75\7\7\2\2\u0b75\u0b86\3\2\2\2\u0b76\u0b77\t+\2\2\u0b77\u0b78"+
		"\7\6\2\2\u0b78\u0b79\5\u00e2r\2\u0b79\u0b7a\7\7\2\2\u0b7a\u0b7b\7\u0120"+
		"\2\2\u0b7b\u0b7c\7p\2\2\u0b7c\u0b7d\7\6\2\2\u0b7d\u0b7e\7\u00ab\2\2\u0b7e"+
		"\u0b7f\7\36\2\2\u0b7f\u0b80\5f\64\2\u0b80\u0b83\7\7\2\2\u0b81\u0b82\7"+
		"\u00af\2\2\u0b82\u0b84\5\u0114\u008b\2\u0b83\u0b81\3\2\2\2\u0b83\u0b84"+
		"\3\2\2\2\u0b84\u0b86\3\2\2\2\u0b85\u0aa8\3\2\2\2\u0b85\u0aaa\3\2\2\2\u0b85"+
		"\u0ab3\3\2\2\2\u0b85\u0abc\3\2\2\2\u0b85\u0ac8\3\2\2\2\u0b85\u0ad5\3\2"+
		"\2\2\u0b85\u0adc\3\2\2\2\u0b85\u0ae9\3\2\2\2\u0b85\u0af2\3\2\2\2\u0b85"+
		"\u0afb\3\2\2\2\u0b85\u0b02\3\2\2\2\u0b85\u0b03\3\2\2\2\u0b85\u0b04\3\2"+
		"\2\2\u0b85\u0b08\3\2\2\2\u0b85\u0b12\3\2\2\2\u0b85\u0b16\3\2\2\2\u0b85"+
		"\u0b36\3\2\2\2\u0b85\u0b3a\3\2\2\2\u0b85\u0b46\3\2\2\2\u0b85\u0b47\3\2"+
		"\2\2\u0b85\u0b4b\3\2\2\2\u0b85\u0b52\3\2\2\2\u0b85\u0b5d\3\2\2\2\u0b85"+
		"\u0b69\3\2\2\2\u0b85\u0b76\3\2\2\2\u0b86\u0b91\3\2\2\2\u0b87\u0b88\f\13"+
		"\2\2\u0b88\u0b89\7\n\2\2\u0b89\u0b8a\5\u00e2r\2\u0b8a\u0b8b\7\13\2\2\u0b8b"+
		"\u0b90\3\2\2\2\u0b8c\u0b8d\f\t\2\2\u0b8d\u0b8e\7\t\2\2\u0b8e\u0b90\5\u0124"+
		"\u0093\2\u0b8f\u0b87\3\2\2\2\u0b8f\u0b8c\3\2\2\2\u0b90\u0b93\3\2\2\2\u0b91"+
		"\u0b8f\3\2\2\2\u0b91\u0b92\3\2\2\2\u0b92\u00e7\3\2\2\2\u0b93\u0b91\3\2"+
		"\2\2\u0b94\u0ba1\7\u00a3\2\2\u0b95\u0ba1\5\u00f2z\2\u0b96\u0b97\5\u0124"+
		"\u0093\2\u0b97\u0b98\7\u0139\2\2\u0b98\u0ba1\3\2\2\2\u0b99\u0ba1\5\u012a"+
		"\u0096\2\u0b9a\u0ba1\5\u00f0y\2\u0b9b\u0b9d\7\u0139\2\2\u0b9c\u0b9b\3"+
		"\2\2\2\u0b9d\u0b9e\3\2\2\2\u0b9e\u0b9c\3\2\2\2\u0b9e\u0b9f\3\2\2\2\u0b9f"+
		"\u0ba1\3\2\2\2\u0ba0\u0b94\3\2\2\2\u0ba0\u0b95\3\2\2\2\u0ba0\u0b96\3\2"+
		"\2\2\u0ba0\u0b99\3\2\2\2\u0ba0\u0b9a\3\2\2\2\u0ba0\u0b9c\3\2\2\2\u0ba1"+
		"\u00e9\3\2\2\2\u0ba2\u0ba3\t,\2\2\u0ba3\u00eb\3\2\2\2\u0ba4\u0ba5\t-\2"+
		"\2\u0ba5\u00ed\3\2\2\2\u0ba6\u0ba7\t.\2\2\u0ba7\u00ef\3\2\2\2\u0ba8\u0ba9"+
		"\t/\2\2\u0ba9\u00f1\3\2\2\2\u0baa\u0bad\7\177\2\2\u0bab\u0bae\5\u00f4"+
		"{\2\u0bac\u0bae\5\u00f8}\2\u0bad\u0bab\3\2\2\2\u0bad\u0bac\3\2\2\2\u0bad"+
		"\u0bae\3\2\2\2\u0bae\u00f3\3\2\2\2\u0baf\u0bb1\5\u00f6|\2\u0bb0\u0bb2"+
		"\5\u00fa~\2\u0bb1\u0bb0\3\2\2\2\u0bb1\u0bb2\3\2\2\2\u0bb2\u00f5\3\2\2"+
		"\2\u0bb3\u0bb4\5\u00fc\177\2\u0bb4\u0bb5\5\u0124\u0093\2\u0bb5\u0bb7\3"+
		"\2\2\2\u0bb6\u0bb3\3\2\2\2\u0bb7\u0bb8\3\2\2\2\u0bb8\u0bb6\3\2\2\2\u0bb8"+
		"\u0bb9\3\2\2\2\u0bb9\u00f7\3\2\2\2\u0bba\u0bbd\5\u00fa~\2\u0bbb\u0bbe"+
		"\5\u00f6|\2\u0bbc\u0bbe\5\u00fa~\2\u0bbd\u0bbb\3\2\2\2\u0bbd\u0bbc\3\2"+
		"\2\2\u0bbd\u0bbe\3\2\2\2\u0bbe\u00f9\3\2\2\2\u0bbf\u0bc0\5\u00fc\177\2"+
		"\u0bc0\u0bc1\5\u0124\u0093\2\u0bc1\u0bc2\7\u0100\2\2\u0bc2\u0bc3\5\u0124"+
		"\u0093\2\u0bc3\u00fb\3\2\2\2\u0bc4\u0bc6\t\60\2\2\u0bc5\u0bc4\3\2\2\2"+
		"\u0bc5\u0bc6\3\2\2\2\u0bc6\u0bc7\3\2\2\2\u0bc7\u0bc8\t\61\2\2\u0bc8\u00fd"+
		"\3\2\2\2\u0bc9\u0bcd\7d\2\2\u0bca\u0bcb\7\r\2\2\u0bcb\u0bcd\5\u0120\u0091"+
		"\2\u0bcc\u0bc9\3\2\2\2\u0bcc\u0bca\3\2\2\2\u0bcd\u00ff\3\2\2\2\u0bce\u0bcf"+
		"\7\25\2\2\u0bcf\u0bd0\7\u0127\2\2\u0bd0\u0bd1\5\u0100\u0081\2\u0bd1\u0bd2"+
		"\7\u0129\2\2\u0bd2\u0bfd\3\2\2\2\u0bd3\u0bd4\7\u0096\2\2\u0bd4\u0bd5\7"+
		"\u0127\2\2\u0bd5\u0bd6\5\u0100\u0081\2\u0bd6\u0bd7\7\b\2\2\u0bd7\u0bd8"+
		"\5\u0100\u0081\2\u0bd8\u0bd9\7\u0129\2\2\u0bd9\u0bfd\3\2\2\2\u0bda\u0be1"+
		"\7\u00ef\2\2\u0bdb\u0bdd\7\u0127\2\2\u0bdc\u0bde\5\u010a\u0086\2\u0bdd"+
		"\u0bdc\3\2\2\2\u0bdd\u0bde\3\2\2\2\u0bde\u0bdf\3\2\2\2\u0bdf\u0be2\7\u0129"+
		"\2\2\u0be0\u0be2\7\u0125\2\2\u0be1\u0bdb\3\2\2\2\u0be1\u0be0\3\2\2\2\u0be2"+
		"\u0bfd\3\2\2\2\u0be3\u0be4\7\177\2\2\u0be4\u0be7\t\62\2\2\u0be5\u0be6"+
		"\7\u0100\2\2\u0be6\u0be8\7\u009c\2\2\u0be7\u0be5\3\2\2\2\u0be7\u0be8\3"+
		"\2\2\2\u0be8\u0bfd\3\2\2\2\u0be9\u0bea\7\177\2\2\u0bea\u0bed\t\63\2\2"+
		"\u0beb\u0bec\7\u0100\2\2\u0bec\u0bee\t\64\2\2\u0bed\u0beb\3\2\2\2\u0bed"+
		"\u0bee\3\2\2\2\u0bee\u0bfd\3\2\2\2\u0bef\u0bfa\5\u0124\u0093\2\u0bf0\u0bf1"+
		"\7\6\2\2\u0bf1\u0bf6\7\u013d\2\2\u0bf2\u0bf3\7\b\2\2\u0bf3\u0bf5\7\u013d"+
		"\2\2\u0bf4\u0bf2\3\2\2\2\u0bf5\u0bf8\3\2\2\2\u0bf6\u0bf4\3\2\2\2\u0bf6"+
		"\u0bf7\3\2\2\2\u0bf7\u0bf9\3\2\2\2\u0bf8\u0bf6\3\2\2\2\u0bf9\u0bfb\7\7"+
		"\2\2\u0bfa\u0bf0\3\2\2\2\u0bfa\u0bfb\3\2\2\2\u0bfb\u0bfd\3\2\2\2\u0bfc"+
		"\u0bce\3\2\2\2\u0bfc\u0bd3\3\2\2\2\u0bfc\u0bda\3\2\2\2\u0bfc\u0be3\3\2"+
		"\2\2\u0bfc\u0be9\3\2\2\2\u0bfc\u0bef\3\2\2\2\u0bfd\u0101\3\2\2\2\u0bfe"+
		"\u0c03\5\u0104\u0083\2\u0bff\u0c00\7\b\2\2\u0c00\u0c02\5\u0104\u0083\2"+
		"\u0c01\u0bff\3\2\2\2\u0c02\u0c05\3\2\2\2\u0c03\u0c01\3\2\2\2\u0c03\u0c04"+
		"\3\2\2\2\u0c04\u0103\3\2\2\2\u0c05\u0c03\3\2\2\2\u0c06\u0c07\5\u00c4c"+
		"\2\u0c07\u0c0a\5\u0100\u0081\2\u0c08\u0c09\7\u00a2\2\2\u0c09\u0c0b\7\u00a3"+
		"\2\2\u0c0a\u0c08\3\2\2\2\u0c0a\u0c0b\3\2\2\2\u0c0b\u0c0d\3\2\2\2\u0c0c"+
		"\u0c0e\5,\27\2\u0c0d\u0c0c\3\2\2\2\u0c0d\u0c0e\3\2\2\2\u0c0e\u0c10\3\2"+
		"\2\2\u0c0f\u0c11\5\u00fe\u0080\2\u0c10\u0c0f\3\2\2\2\u0c10\u0c11\3\2\2"+
		"\2\u0c11\u0105\3\2\2\2\u0c12\u0c17\5\u0108\u0085\2\u0c13\u0c14\7\b\2\2"+
		"\u0c14\u0c16\5\u0108\u0085\2\u0c15\u0c13\3\2\2\2\u0c16\u0c19\3\2\2\2\u0c17"+
		"\u0c15\3\2\2\2\u0c17\u0c18\3\2\2\2\u0c18\u0107\3\2\2\2\u0c19\u0c17\3\2"+
		"\2\2\u0c1a\u0c1b\5\u0120\u0091\2\u0c1b\u0c1e\5\u0100\u0081\2\u0c1c\u0c1d"+
		"\7\u00a2\2\2\u0c1d\u0c1f\7\u00a3\2\2\u0c1e\u0c1c\3\2\2\2\u0c1e\u0c1f\3"+
		"\2\2\2\u0c1f\u0c21\3\2\2\2\u0c20\u0c22\5,\27\2\u0c21\u0c20\3\2\2\2\u0c21"+
		"\u0c22\3\2\2\2\u0c22\u0109\3\2\2\2\u0c23\u0c28\5\u010c\u0087\2\u0c24\u0c25"+
		"\7\b\2\2\u0c25\u0c27\5\u010c\u0087\2\u0c26\u0c24\3\2\2\2\u0c27\u0c2a\3"+
		"\2\2\2\u0c28\u0c26\3\2\2\2\u0c28\u0c29\3\2\2\2\u0c29\u010b\3\2\2\2\u0c2a"+
		"\u0c28\3\2\2\2\u0c2b\u0c2d\5\u0124\u0093\2\u0c2c\u0c2e\7\u0135\2\2\u0c2d"+
		"\u0c2c\3\2\2\2\u0c2d\u0c2e\3\2\2\2\u0c2e\u0c2f\3\2\2\2\u0c2f\u0c32\5\u0100"+
		"\u0081\2\u0c30\u0c31\7\u00a2\2\2\u0c31\u0c33\7\u00a3\2\2\u0c32\u0c30\3"+
		"\2\2\2\u0c32\u0c33\3\2\2\2\u0c33\u0c35\3\2\2\2\u0c34\u0c36\5,\27\2\u0c35"+
		"\u0c34\3\2\2\2\u0c35\u0c36\3\2\2\2\u0c36\u010d\3\2\2\2\u0c37\u0c38\7\u011c"+
		"\2\2\u0c38\u0c39\5\u00dan\2\u0c39\u0c3a\7\u00fb\2\2\u0c3a\u0c3b\5\u00da"+
		"n\2\u0c3b\u010f\3\2\2\2\u0c3c\u0c3d\7\u011e\2\2\u0c3d\u0c42\5\u0112\u008a"+
		"\2\u0c3e\u0c3f\7\b\2\2\u0c3f\u0c41\5\u0112\u008a\2\u0c40\u0c3e\3\2\2\2"+
		"\u0c41\u0c44\3\2\2\2\u0c42\u0c40\3\2\2\2\u0c42\u0c43\3\2\2\2\u0c43\u0111"+
		"\3\2\2\2\u0c44\u0c42\3\2\2\2\u0c45\u0c46\5\u0120\u0091\2\u0c46\u0c47\7"+
		"\26\2\2\u0c47\u0c48\5\u0114\u008b\2\u0c48\u0113\3\2\2\2\u0c49\u0c78\5"+
		"\u0120\u0091\2\u0c4a\u0c4b\7\6\2\2\u0c4b\u0c4c\5\u0120\u0091\2\u0c4c\u0c4d"+
		"\7\7\2\2\u0c4d\u0c78\3\2\2\2\u0c4e\u0c71\7\6\2\2\u0c4f\u0c50\7(\2\2\u0c50"+
		"\u0c51\7\36\2\2\u0c51\u0c56\5\u00dan\2\u0c52\u0c53\7\b\2\2\u0c53\u0c55"+
		"\5\u00dan\2\u0c54\u0c52\3\2\2\2\u0c55\u0c58\3\2\2\2\u0c56\u0c54\3\2\2"+
		"\2\u0c56\u0c57\3\2\2\2\u0c57\u0c72\3\2\2\2\u0c58\u0c56\3\2\2\2\u0c59\u0c5a"+
		"\t\65\2\2\u0c5a\u0c5b\7\36\2\2\u0c5b\u0c60\5\u00dan\2\u0c5c\u0c5d\7\b"+
		"\2\2\u0c5d\u0c5f\5\u00dan\2\u0c5e\u0c5c\3\2\2\2\u0c5f\u0c62\3\2\2\2\u0c60"+
		"\u0c5e\3\2\2\2\u0c60\u0c61\3\2\2\2\u0c61\u0c64\3\2\2\2\u0c62\u0c60\3\2"+
		"\2\2\u0c63\u0c59\3\2\2\2\u0c63\u0c64\3\2\2\2\u0c64\u0c6f\3\2\2\2\u0c65"+
		"\u0c66\t\66\2\2\u0c66\u0c67\7\36\2\2\u0c67\u0c6c\5f\64\2\u0c68\u0c69\7"+
		"\b\2\2\u0c69\u0c6b\5f\64\2\u0c6a\u0c68\3\2\2\2\u0c6b\u0c6e\3\2\2\2\u0c6c"+
		"\u0c6a\3\2\2\2\u0c6c\u0c6d\3\2\2\2\u0c6d\u0c70\3\2\2\2\u0c6e\u0c6c\3\2"+
		"\2\2\u0c6f\u0c65\3\2\2\2\u0c6f\u0c70\3\2\2\2\u0c70\u0c72\3\2\2\2\u0c71"+
		"\u0c4f\3\2\2\2\u0c71\u0c63\3\2\2\2\u0c72\u0c74\3\2\2\2\u0c73\u0c75\5\u0116"+
		"\u008c\2\u0c74\u0c73\3\2\2\2\u0c74\u0c75\3\2\2\2\u0c75\u0c76\3\2\2\2\u0c76"+
		"\u0c78\7\7\2\2\u0c77\u0c49\3\2\2\2\u0c77\u0c4a\3\2\2\2\u0c77\u0c4e\3\2"+
		"\2\2\u0c78\u0115\3\2\2\2\u0c79\u0c7a\7\u00c3\2\2\u0c7a\u0c8a\5\u0118\u008d"+
		"\2\u0c7b\u0c7c\7\u00d9\2\2\u0c7c\u0c8a\5\u0118\u008d\2\u0c7d\u0c7e\7\u00c3"+
		"\2\2\u0c7e\u0c7f\7\32\2\2\u0c7f\u0c80\5\u0118\u008d\2\u0c80\u0c81\7\21"+
		"\2\2\u0c81\u0c82\5\u0118\u008d\2\u0c82\u0c8a\3\2\2\2\u0c83\u0c84\7\u00d9"+
		"\2\2\u0c84\u0c85\7\32\2\2\u0c85\u0c86\5\u0118\u008d\2\u0c86\u0c87\7\21"+
		"\2\2\u0c87\u0c88\5\u0118\u008d\2\u0c88\u0c8a\3\2\2\2\u0c89\u0c79\3\2\2"+
		"\2\u0c89\u0c7b\3\2\2\2\u0c89\u0c7d\3\2\2\2\u0c89\u0c83\3\2\2\2\u0c8a\u0117"+
		"\3\2\2\2\u0c8b\u0c8c\7\u010c\2\2\u0c8c\u0c93\t\67\2\2\u0c8d\u0c8e\7:\2"+
		"\2\u0c8e\u0c93\7\u00d8\2\2\u0c8f\u0c90\5\u00dan\2\u0c90\u0c91\t\67\2\2"+
		"\u0c91\u0c93\3\2\2\2\u0c92\u0c8b\3\2\2\2\u0c92\u0c8d\3\2\2\2\u0c92\u0c8f"+
		"\3\2\2\2\u0c93\u0119\3\2\2\2\u0c94\u0c99\5\u011e\u0090\2\u0c95\u0c96\7"+
		"\b\2\2\u0c96\u0c98\5\u011e\u0090\2\u0c97\u0c95\3\2\2\2\u0c98\u0c9b\3\2"+
		"\2\2\u0c99\u0c97\3\2\2\2\u0c99\u0c9a\3\2\2\2\u0c9a\u011b\3\2\2\2\u0c9b"+
		"\u0c99\3\2\2\2\u0c9c\u0ca1\5\u011e\u0090\2\u0c9d\u0ca1\7b\2\2\u0c9e\u0ca1"+
		"\7\u0089\2\2\u0c9f\u0ca1\7\u00d2\2\2\u0ca0\u0c9c\3\2\2\2\u0ca0\u0c9d\3"+
		"\2\2\2\u0ca0\u0c9e\3\2\2\2\u0ca0\u0c9f\3\2\2\2\u0ca1\u011d\3\2\2\2\u0ca2"+
		"\u0ca7\5\u0124\u0093\2\u0ca3\u0ca4\7\t\2\2\u0ca4\u0ca6\5\u0124\u0093\2"+
		"\u0ca5\u0ca3\3\2\2\2\u0ca6\u0ca9\3\2\2\2\u0ca7\u0ca5\3\2\2\2\u0ca7\u0ca8"+
		"\3\2\2\2\u0ca8\u011f\3\2\2\2\u0ca9\u0ca7\3\2\2\2\u0caa\u0cab\5\u0124\u0093"+
		"\2\u0cab\u0cac\5\u0122\u0092\2\u0cac\u0121\3\2\2\2\u0cad\u0cae\7\u012c"+
		"\2\2\u0cae\u0cb0\5\u0124\u0093\2\u0caf\u0cad\3\2\2\2\u0cb0\u0cb1\3\2\2"+
		"\2\u0cb1\u0caf\3\2\2\2\u0cb1\u0cb2\3\2\2\2\u0cb2\u0cb5\3\2\2\2\u0cb3\u0cb5"+
		"\3\2\2\2\u0cb4\u0caf\3\2\2\2\u0cb4\u0cb3\3\2\2\2\u0cb5\u0123\3\2\2\2\u0cb6"+
		"\u0cba\5\u0126\u0094\2\u0cb7\u0cb8\6\u0093\22\2\u0cb8\u0cba\5\u0130\u0099"+
		"\2\u0cb9\u0cb6\3\2\2\2\u0cb9\u0cb7\3\2\2\2\u0cba\u0125\3\2\2\2\u0cbb\u0cc2"+
		"\7\u0143\2\2\u0cbc\u0cc2\5\u0128\u0095\2\u0cbd\u0cbe\6\u0094\23\2\u0cbe"+
		"\u0cc2\5\u012e\u0098\2\u0cbf\u0cc0\6\u0094\24\2\u0cc0\u0cc2\5\u0132\u009a"+
		"\2\u0cc1\u0cbb\3\2\2\2\u0cc1\u0cbc\3\2\2\2\u0cc1\u0cbd\3\2\2\2\u0cc1\u0cbf"+
		"\3\2\2\2\u0cc2\u0127\3\2\2\2\u0cc3\u0cc4\7\u0144\2\2\u0cc4\u0129\3\2\2"+
		"\2\u0cc5\u0cc7\6\u0096\25\2\u0cc6\u0cc8\7\u012c\2\2\u0cc7\u0cc6\3\2\2"+
		"\2\u0cc7\u0cc8\3\2\2\2\u0cc8\u0cc9\3\2\2\2\u0cc9\u0cf1\7\u013e\2\2\u0cca"+
		"\u0ccc\6\u0096\26\2\u0ccb\u0ccd\7\u012c\2\2\u0ccc\u0ccb\3\2\2\2\u0ccc"+
		"\u0ccd\3\2\2\2\u0ccd\u0cce\3\2\2\2\u0cce\u0cf1\7\u013f\2\2\u0ccf\u0cd1"+
		"\6\u0096\27\2\u0cd0\u0cd2\7\u012c\2\2\u0cd1\u0cd0\3\2\2\2\u0cd1\u0cd2"+
		"\3\2\2\2\u0cd2\u0cd3\3\2\2\2\u0cd3\u0cf1\t8\2\2\u0cd4\u0cd6\7\u012c\2"+
		"\2\u0cd5\u0cd4\3\2\2\2\u0cd5\u0cd6\3\2\2\2\u0cd6\u0cd7\3\2\2\2\u0cd7\u0cf1"+
		"\7\u013d\2\2\u0cd8\u0cda\7\u012c\2\2\u0cd9\u0cd8\3\2\2\2\u0cd9\u0cda\3"+
		"\2\2\2\u0cda\u0cdb\3\2\2\2\u0cdb\u0cf1\7\u013a\2\2\u0cdc\u0cde\7\u012c"+
		"\2\2\u0cdd\u0cdc\3\2\2\2\u0cdd\u0cde\3\2\2\2\u0cde\u0cdf\3\2\2\2\u0cdf"+
		"\u0cf1\7\u013b\2\2\u0ce0\u0ce2\7\u012c\2\2\u0ce1\u0ce0\3\2\2\2\u0ce1\u0ce2"+
		"\3\2\2\2\u0ce2\u0ce3\3\2\2\2\u0ce3\u0cf1\7\u013c\2\2\u0ce4\u0ce6\7\u012c"+
		"\2\2\u0ce5\u0ce4\3\2\2\2\u0ce5\u0ce6\3\2\2\2\u0ce6\u0ce7\3\2\2\2\u0ce7"+
		"\u0cf1\7\u0141\2\2\u0ce8\u0cea\7\u012c\2\2\u0ce9\u0ce8\3\2\2\2\u0ce9\u0cea"+
		"\3\2\2\2\u0cea\u0ceb\3\2\2\2\u0ceb\u0cf1\7\u0140\2\2\u0cec\u0cee\7\u012c"+
		"\2\2\u0ced\u0cec\3\2\2\2\u0ced\u0cee\3\2\2\2\u0cee\u0cef\3\2\2\2\u0cef"+
		"\u0cf1\7\u0142\2\2\u0cf0\u0cc5\3\2\2\2\u0cf0\u0cca\3\2\2\2\u0cf0\u0ccf"+
		"\3\2\2\2\u0cf0\u0cd5\3\2\2\2\u0cf0\u0cd9\3\2\2\2\u0cf0\u0cdd\3\2\2\2\u0cf0"+
		"\u0ce1\3\2\2\2\u0cf0\u0ce5\3\2\2\2\u0cf0\u0ce9\3\2\2\2\u0cf0\u0ced\3\2"+
		"\2\2\u0cf1\u012b\3\2\2\2\u0cf2\u0cf3\7\u010a\2\2\u0cf3\u0cfa\5\u0100\u0081"+
		"\2\u0cf4\u0cfa\5,\27\2\u0cf5\u0cfa\5\u00fe\u0080\2\u0cf6\u0cf7\t9\2\2"+
		"\u0cf7\u0cf8\7\u00a2\2\2\u0cf8\u0cfa\7\u00a3\2\2\u0cf9\u0cf2\3\2\2\2\u0cf9"+
		"\u0cf4\3\2\2\2\u0cf9\u0cf5\3\2\2\2\u0cf9\u0cf6\3\2\2\2\u0cfa\u012d\3\2"+
		"\2\2\u0cfb\u0cfc\t:\2\2\u0cfc\u012f\3\2\2\2\u0cfd\u0cfe\t;\2\2\u0cfe\u0131"+
		"\3\2\2\2\u0cff\u0d00\t<\2\2\u0d00\u0133\3\2\2\2\u01b8\u013d\u0140\u014a"+
		"\u014f\u0155\u016e\u017b\u0182\u018a\u018c\u01a0\u01a4\u01aa\u01ad\u01b0"+
		"\u01b7\u01ba\u01be\u01c1\u01c8\u01d3\u01d5\u01dd\u01e0\u01e4\u01e7\u01ed"+
		"\u01f8\u01fe\u0203\u0225\u0232\u024b\u0254\u0258\u025e\u0262\u0267\u026d"+
		"\u0279\u0281\u0287\u0294\u0299\u02a9\u02b0\u02b4\u02ba\u02c9\u02cd\u02d3"+
		"\u02d9\u02dc\u02df\u02e5\u02e9\u02f1\u02f3\u02fc\u02ff\u0308\u030d\u0313"+
		"\u031a\u031d\u0323\u032e\u0331\u0335\u033a\u033f\u0346\u0349\u034c\u0353"+
		"\u0358\u0361\u0369\u036f\u0372\u0375\u037b\u037f\u0384\u0387\u038b\u038d"+
		"\u0395\u039d\u03a0\u03a5\u03ab\u03b1\u03b4\u03b8\u03bb\u03bf\u03db\u03de"+
		"\u03e2\u03e8\u03eb\u03ee\u03f4\u03fc\u0401\u0407\u040d\u0415\u041c\u0424"+
		"\u0435\u0443\u0446\u044c\u0455\u045e\u0466\u046b\u0470\u0477\u047d\u0482"+
		"\u048a\u048d\u0499\u049d\u04a4\u0518\u0520\u0528\u0531\u053b\u053f\u0542"+
		"\u0548\u054e\u055a\u0566\u056b\u0574\u057c\u0583\u0585\u0588\u058d\u0591"+
		"\u0596\u0599\u059e\u05a3\u05a6\u05ab\u05af\u05b4\u05b6\u05ba\u05c3\u05cb"+
		"\u05d6\u05dd\u05e6\u05eb\u05ee\u0604\u0606\u060f\u0616\u0619\u0620\u0624"+
		"\u062a\u0632\u063d\u0648\u064f\u0655\u0661\u0668\u066f\u067b\u0683\u0689"+
		"\u068c\u0695\u0698\u06a1\u06a4\u06ad\u06b0\u06b9\u06bc\u06bf\u06c4\u06c6"+
		"\u06d2\u06d9\u06e0\u06e3\u06e5\u06f1\u06f5\u06f9\u06ff\u0703\u070b\u070f"+
		"\u0712\u0715\u0718\u071c\u0720\u0725\u0729\u072c\u072f\u0732\u0736\u073b"+
		"\u073f\u0742\u0745\u0748\u074a\u0750\u0757\u075c\u075f\u0762\u0766\u0770"+
		"\u0774\u0776\u0779\u077d\u0783\u0787\u0792\u079c\u07a8\u07b7\u07bc\u07c3"+
		"\u07d3\u07d8\u07e5\u07ea\u07f2\u07f8\u07fc\u07ff\u0806\u080c\u0815\u081f"+
		"\u082e\u0833\u0835\u0839\u0842\u084f\u0854\u0858\u0860\u0863\u0867\u0875"+
		"\u0882\u0887\u088b\u088e\u0893\u089c\u089f\u08a4\u08ab\u08ae\u08b3\u08b9"+
		"\u08bf\u08c3\u08c9\u08cd\u08d0\u08d5\u08d8\u08dd\u08e1\u08e4\u08e7\u08ed"+
		"\u08f2\u08f9\u08fc\u090e\u0910\u0913\u091e\u0927\u092e\u0936\u093d\u0941"+
		"\u0944\u094c\u0954\u095a\u0962\u096e\u0971\u0977\u097b\u097d\u0986\u0992"+
		"\u0994\u099b\u09a2\u09a8\u09ae\u09b0\u09b7\u09bf\u09c7\u09cd\u09d2\u09d9"+
		"\u09df\u09e3\u09e5\u09ec\u09f5\u09fc\u0a06\u0a0b\u0a0f\u0a18\u0a25\u0a27"+
		"\u0a2f\u0a31\u0a35\u0a3d\u0a46\u0a4c\u0a54\u0a59\u0a65\u0a6a\u0a6d\u0a73"+
		"\u0a77\u0a7c\u0a81\u0a86\u0a8c\u0aa1\u0aa3\u0ac0\u0ac4\u0acd\u0ad1\u0ae3"+
		"\u0ae6\u0aee\u0af7\u0b0e\u0b19\u0b20\u0b23\u0b2c\u0b30\u0b34\u0b40\u0b59"+
		"\u0b60\u0b63\u0b72\u0b83\u0b85\u0b8f\u0b91\u0b9e\u0ba0\u0bad\u0bb1\u0bb8"+
		"\u0bbd\u0bc5\u0bcc\u0bdd\u0be1\u0be7\u0bed\u0bf6\u0bfa\u0bfc\u0c03\u0c0a"+
		"\u0c0d\u0c10\u0c17\u0c1e\u0c21\u0c28\u0c2d\u0c32\u0c35\u0c42\u0c56\u0c60"+
		"\u0c63\u0c6c\u0c6f\u0c71\u0c74\u0c77\u0c89\u0c92\u0c99\u0ca0\u0ca7\u0cb1"+
		"\u0cb4\u0cb9\u0cc1\u0cc7\u0ccc\u0cd1\u0cd5\u0cd9\u0cdd\u0ce1\u0ce5\u0ce9"+
		"\u0ced\u0cf0\u0cf9";
	public static final String _serializedATN = Utils.join(
		new String[] {
			_serializedATNSegment0,
			_serializedATNSegment1
		},
		""
	);
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}