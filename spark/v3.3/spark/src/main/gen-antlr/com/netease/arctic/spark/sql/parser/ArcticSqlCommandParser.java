// Generated from com/netease/arctic/spark/sql/parser/ArcticSqlCommand.g4 by ANTLR 4.8
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
public class ArcticSqlCommandParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		KEY=1, MIGRATE=2, ARCTIC=3, SEMICOLON=4, LEFT_PAREN=5, RIGHT_PAREN=6, 
		COMMA=7, DOT=8, LEFT_BRACKET=9, RIGHT_BRACKET=10, ADD=11, AFTER=12, ALL=13, 
		ALTER=14, ANALYZE=15, AND=16, ANTI=17, ANY=18, ARCHIVE=19, ARRAY=20, AS=21, 
		ASC=22, AT=23, AUTHORIZATION=24, BETWEEN=25, BOTH=26, BUCKET=27, BUCKETS=28, 
		BY=29, CACHE=30, CASCADE=31, CASE=32, CAST=33, CATALOG=34, CATALOGS=35, 
		CHANGE=36, CHECK=37, CLEAR=38, CLUSTER=39, CLUSTERED=40, CODEGEN=41, COLLATE=42, 
		COLLECTION=43, COLUMN=44, COLUMNS=45, COMMENT=46, COMMIT=47, COMPACT=48, 
		COMPACTIONS=49, COMPUTE=50, CONCATENATE=51, CONSTRAINT=52, COST=53, CREATE=54, 
		CROSS=55, CUBE=56, CURRENT=57, CURRENT_DATE=58, CURRENT_TIME=59, CURRENT_TIMESTAMP=60, 
		CURRENT_USER=61, DAY=62, DAYOFYEAR=63, DATA=64, DATABASE=65, DATABASES=66, 
		DATEADD=67, DATEDIFF=68, DBPROPERTIES=69, DEFINED=70, DELETE=71, DELIMITED=72, 
		DESC=73, DESCRIBE=74, DFS=75, DIRECTORIES=76, DIRECTORY=77, DISTINCT=78, 
		DISTRIBUTE=79, DIV=80, DROP=81, ELSE=82, END=83, ESCAPE=84, ESCAPED=85, 
		EXCEPT=86, EXCHANGE=87, EXISTS=88, EXPLAIN=89, EXPORT=90, EXTENDED=91, 
		EXTERNAL=92, EXTRACT=93, FALSE=94, FETCH=95, FIELDS=96, FILTER=97, FILEFORMAT=98, 
		FIRST=99, FOLLOWING=100, FOR=101, FOREIGN=102, FORMAT=103, FORMATTED=104, 
		FROM=105, FULL=106, FUNCTION=107, FUNCTIONS=108, GLOBAL=109, GRANT=110, 
		GROUP=111, GROUPING=112, HAVING=113, HOUR=114, IF=115, IGNORE=116, IMPORT=117, 
		IN=118, INDEX=119, INDEXES=120, INNER=121, INPATH=122, INPUTFORMAT=123, 
		INSERT=124, INTERSECT=125, INTERVAL=126, INTO=127, IS=128, ITEMS=129, 
		JOIN=130, KEYS=131, LAST=132, LATERAL=133, LAZY=134, LEADING=135, LEFT=136, 
		LIKE=137, ILIKE=138, LIMIT=139, LINES=140, LIST=141, LOAD=142, LOCAL=143, 
		LOCATION=144, LOCK=145, LOCKS=146, LOGICAL=147, MACRO=148, MAP=149, MATCHED=150, 
		MERGE=151, MICROSECOND=152, MILLISECOND=153, MINUTE=154, MONTH=155, MSCK=156, 
		NAMESPACE=157, NAMESPACES=158, NATURAL=159, NO=160, NOT=161, NULL=162, 
		NULLS=163, OF=164, ON=165, ONLY=166, OPTION=167, OPTIONS=168, OR=169, 
		ORDER=170, OUT=171, OUTER=172, OUTPUTFORMAT=173, OVER=174, OVERLAPS=175, 
		OVERLAY=176, OVERWRITE=177, PARTITION=178, PARTITIONED=179, PARTITIONS=180, 
		PERCENTILE_CONT=181, PERCENTILE_DISC=182, PERCENTLIT=183, PIVOT=184, PLACING=185, 
		POSITION=186, PRECEDING=187, PRIMARY=188, PRINCIPALS=189, PROPERTIES=190, 
		PURGE=191, QUARTER=192, QUERY=193, RANGE=194, RECORDREADER=195, RECORDWRITER=196, 
		RECOVER=197, REDUCE=198, REFERENCES=199, REFRESH=200, RENAME=201, REPAIR=202, 
		REPEATABLE=203, REPLACE=204, RESET=205, RESPECT=206, RESTRICT=207, REVOKE=208, 
		RIGHT=209, RLIKE=210, ROLE=211, ROLES=212, ROLLBACK=213, ROLLUP=214, ROW=215, 
		ROWS=216, SECOND=217, SCHEMA=218, SCHEMAS=219, SELECT=220, SEMI=221, SEPARATED=222, 
		SERDE=223, SERDEPROPERTIES=224, SESSION_USER=225, SET=226, SETMINUS=227, 
		SETS=228, SHOW=229, SKEWED=230, SOME=231, SORT=232, SORTED=233, START=234, 
		STATISTICS=235, STORED=236, STRATIFY=237, STRUCT=238, SUBSTR=239, SUBSTRING=240, 
		SYNC=241, SYSTEM_TIME=242, SYSTEM_VERSION=243, TABLE=244, TABLES=245, 
		TABLESAMPLE=246, TBLPROPERTIES=247, TEMPORARY=248, TERMINATED=249, THEN=250, 
		TIME=251, TIMESTAMP=252, TIMESTAMPADD=253, TIMESTAMPDIFF=254, TO=255, 
		TOUCH=256, TRAILING=257, TRANSACTION=258, TRANSACTIONS=259, TRANSFORM=260, 
		TRIM=261, TRUE=262, TRUNCATE=263, TRY_CAST=264, TYPE=265, UNARCHIVE=266, 
		UNBOUNDED=267, UNCACHE=268, UNION=269, UNIQUE=270, UNKNOWN=271, UNLOCK=272, 
		UNSET=273, UPDATE=274, USE=275, USER=276, USING=277, VALUES=278, VERSION=279, 
		VIEW=280, VIEWS=281, WEEK=282, WHEN=283, WHERE=284, WINDOW=285, WITH=286, 
		WITHIN=287, YEAR=288, ZONE=289, EQ=290, NSEQ=291, NEQ=292, NEQJ=293, LT=294, 
		LTE=295, GT=296, GTE=297, PLUS=298, MINUS=299, ASTERISK=300, SLASH=301, 
		PERCENT=302, TILDE=303, AMPERSAND=304, PIPE=305, CONCAT_PIPE=306, HAT=307, 
		COLON=308, ARROW=309, HENT_START=310, HENT_END=311, STRING=312, BIGINT_LITERAL=313, 
		SMALLINT_LITERAL=314, TINYINT_LITERAL=315, INTEGER_VALUE=316, EXPONENT_VALUE=317, 
		DECIMAL_VALUE=318, FLOAT_LITERAL=319, DOUBLE_LITERAL=320, BIGDECIMAL_LITERAL=321, 
		IDENTIFIER=322, BACKQUOTED_IDENTIFIER=323, SIMPLE_COMMENT=324, BRACKETED_COMMENT=325, 
		WS=326, UNRECOGNIZED=327;
	public static final int
		RULE_arcticCommand = 0, RULE_arcticStatement = 1, RULE_singleStatement = 2, 
		RULE_singleExpression = 3, RULE_singleTableIdentifier = 4, RULE_singleMultipartIdentifier = 5, 
		RULE_singleFunctionIdentifier = 6, RULE_singleDataType = 7, RULE_singleTableSchema = 8, 
		RULE_statement = 9, RULE_configKey = 10, RULE_configValue = 11, RULE_unsupportedHiveNativeCommands = 12, 
		RULE_createTableHeader = 13, RULE_replaceTableHeader = 14, RULE_bucketSpec = 15, 
		RULE_skewSpec = 16, RULE_locationSpec = 17, RULE_commentSpec = 18, RULE_query = 19, 
		RULE_insertInto = 20, RULE_partitionSpecLocation = 21, RULE_partitionSpec = 22, 
		RULE_partitionVal = 23, RULE_namespace = 24, RULE_namespaces = 25, RULE_describeFuncName = 26, 
		RULE_describeColName = 27, RULE_ctes = 28, RULE_namedQuery = 29, RULE_tableProvider = 30, 
		RULE_createTableClauses = 31, RULE_propertyList = 32, RULE_property = 33, 
		RULE_propertyKey = 34, RULE_propertyValue = 35, RULE_constantList = 36, 
		RULE_nestedConstantList = 37, RULE_createFileFormat = 38, RULE_fileFormat = 39, 
		RULE_storageHandler = 40, RULE_resource = 41, RULE_dmlStatementNoWith = 42, 
		RULE_queryOrganization = 43, RULE_multiInsertQueryBody = 44, RULE_queryTerm = 45, 
		RULE_queryPrimary = 46, RULE_sortItem = 47, RULE_fromStatement = 48, RULE_fromStatementBody = 49, 
		RULE_querySpecification = 50, RULE_transformClause = 51, RULE_selectClause = 52, 
		RULE_setClause = 53, RULE_matchedClause = 54, RULE_notMatchedClause = 55, 
		RULE_matchedAction = 56, RULE_notMatchedAction = 57, RULE_assignmentList = 58, 
		RULE_assignment = 59, RULE_whereClause = 60, RULE_havingClause = 61, RULE_hint = 62, 
		RULE_hintStatement = 63, RULE_fromClause = 64, RULE_temporalClause = 65, 
		RULE_aggregationClause = 66, RULE_groupByClause = 67, RULE_groupingAnalytics = 68, 
		RULE_groupingElement = 69, RULE_groupingSet = 70, RULE_pivotClause = 71, 
		RULE_pivotColumn = 72, RULE_pivotValue = 73, RULE_lateralView = 74, RULE_setQuantifier = 75, 
		RULE_relation = 76, RULE_joinRelation = 77, RULE_joinType = 78, RULE_joinCriteria = 79, 
		RULE_sample = 80, RULE_sampleMethod = 81, RULE_identifierList = 82, RULE_identifierSeq = 83, 
		RULE_orderedIdentifierList = 84, RULE_orderedIdentifier = 85, RULE_identifierCommentList = 86, 
		RULE_identifierComment = 87, RULE_relationPrimary = 88, RULE_inlineTable = 89, 
		RULE_functionTable = 90, RULE_tableAlias = 91, RULE_rowFormat = 92, RULE_multipartIdentifierList = 93, 
		RULE_multipartIdentifier = 94, RULE_multipartIdentifierPropertyList = 95, 
		RULE_multipartIdentifierProperty = 96, RULE_tableIdentifier = 97, RULE_functionIdentifier = 98, 
		RULE_namedExpression = 99, RULE_namedExpressionSeq = 100, RULE_partitionFieldList = 101, 
		RULE_partitionField = 102, RULE_transform = 103, RULE_transformArgument = 104, 
		RULE_expression = 105, RULE_expressionSeq = 106, RULE_booleanExpression = 107, 
		RULE_predicate = 108, RULE_valueExpression = 109, RULE_datetimeUnit = 110, 
		RULE_primaryExpression = 111, RULE_constant = 112, RULE_comparisonOperator = 113, 
		RULE_arithmeticOperator = 114, RULE_predicateOperator = 115, RULE_booleanValue = 116, 
		RULE_interval = 117, RULE_errorCapturingMultiUnitsInterval = 118, RULE_multiUnitsInterval = 119, 
		RULE_errorCapturingUnitToUnitInterval = 120, RULE_unitToUnitInterval = 121, 
		RULE_intervalValue = 122, RULE_colPosition = 123, RULE_dataType = 124, 
		RULE_qualifiedColTypeWithPositionList = 125, RULE_qualifiedColTypeWithPosition = 126, 
		RULE_colTypeList = 127, RULE_colType = 128, RULE_complexColTypeList = 129, 
		RULE_complexColType = 130, RULE_whenClause = 131, RULE_windowClause = 132, 
		RULE_namedWindow = 133, RULE_windowSpec = 134, RULE_windowFrame = 135, 
		RULE_frameBound = 136, RULE_qualifiedNameList = 137, RULE_functionName = 138, 
		RULE_qualifiedName = 139, RULE_errorCapturingIdentifier = 140, RULE_errorCapturingIdentifierExtra = 141, 
		RULE_identifier = 142, RULE_strictIdentifier = 143, RULE_quotedIdentifier = 144, 
		RULE_number = 145, RULE_alterColumnAction = 146, RULE_ansiNonReserved = 147, 
		RULE_strictNonReserved = 148, RULE_nonReserved = 149;
	private static String[] makeRuleNames() {
		return new String[] {
			"arcticCommand", "arcticStatement", "singleStatement", "singleExpression", 
			"singleTableIdentifier", "singleMultipartIdentifier", "singleFunctionIdentifier", 
			"singleDataType", "singleTableSchema", "statement", "configKey", "configValue", 
			"unsupportedHiveNativeCommands", "createTableHeader", "replaceTableHeader", 
			"bucketSpec", "skewSpec", "locationSpec", "commentSpec", "query", "insertInto", 
			"partitionSpecLocation", "partitionSpec", "partitionVal", "namespace", 
			"namespaces", "describeFuncName", "describeColName", "ctes", "namedQuery", 
			"tableProvider", "createTableClauses", "propertyList", "property", "propertyKey", 
			"propertyValue", "constantList", "nestedConstantList", "createFileFormat", 
			"fileFormat", "storageHandler", "resource", "dmlStatementNoWith", "queryOrganization", 
			"multiInsertQueryBody", "queryTerm", "queryPrimary", "sortItem", "fromStatement", 
			"fromStatementBody", "querySpecification", "transformClause", "selectClause", 
			"setClause", "matchedClause", "notMatchedClause", "matchedAction", "notMatchedAction", 
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
			null, "'KEY'", "'MIGRATE'", "'ARCTIC'", "';'", "'('", "')'", "','", "'.'", 
			"'['", "']'", "'ADD'", "'AFTER'", "'ALL'", "'ALTER'", "'ANALYZE'", "'AND'", 
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
			null, "KEY", "MIGRATE", "ARCTIC", "SEMICOLON", "LEFT_PAREN", "RIGHT_PAREN", 
			"COMMA", "DOT", "LEFT_BRACKET", "RIGHT_BRACKET", "ADD", "AFTER", "ALL", 
			"ALTER", "ANALYZE", "AND", "ANTI", "ANY", "ARCHIVE", "ARRAY", "AS", "ASC", 
			"AT", "AUTHORIZATION", "BETWEEN", "BOTH", "BUCKET", "BUCKETS", "BY", 
			"CACHE", "CASCADE", "CASE", "CAST", "CATALOG", "CATALOGS", "CHANGE", 
			"CHECK", "CLEAR", "CLUSTER", "CLUSTERED", "CODEGEN", "COLLATE", "COLLECTION", 
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
	public String getGrammarFileName() { return "ArcticSqlCommand.g4"; }

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

	public ArcticSqlCommandParser(TokenStream input) {
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterArcticCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitArcticCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitArcticCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArcticCommandContext arcticCommand() throws RecognitionException {
		ArcticCommandContext _localctx = new ArcticCommandContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_arcticCommand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
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
	public static class MigrateStatementContext extends ArcticStatementContext {
		public MultipartIdentifierContext source;
		public MultipartIdentifierContext target;
		public TerminalNode MIGRATE() { return getToken(ArcticSqlCommandParser.MIGRATE, 0); }
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
		public TerminalNode ARCTIC() { return getToken(ArcticSqlCommandParser.ARCTIC, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public MigrateStatementContext(ArcticStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMigrateStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMigrateStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMigrateStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArcticStatementContext arcticStatement() throws RecognitionException {
		ArcticStatementContext _localctx = new ArcticStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_arcticStatement);
		try {
			_localctx = new MigrateStatementContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(302);
			match(MIGRATE);
			setState(303);
			((MigrateStatementContext)_localctx).source = multipartIdentifier();
			setState(304);
			match(TO);
			setState(305);
			match(ARCTIC);
			setState(306);
			((MigrateStatementContext)_localctx).target = multipartIdentifier();
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public List<TerminalNode> SEMICOLON() { return getTokens(ArcticSqlCommandParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(ArcticSqlCommandParser.SEMICOLON, i);
		}
		public SingleStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleStatementContext singleStatement() throws RecognitionException {
		SingleStatementContext _localctx = new SingleStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_singleStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			statement();
			setState(312);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(309);
				match(SEMICOLON);
				}
				}
				setState(314);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(315);
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public SingleExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleExpressionContext singleExpression() throws RecognitionException {
		SingleExpressionContext _localctx = new SingleExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_singleExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			namedExpression();
			setState(318);
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public SingleTableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleTableIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleTableIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleTableIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableIdentifierContext singleTableIdentifier() throws RecognitionException {
		SingleTableIdentifierContext _localctx = new SingleTableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_singleTableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			tableIdentifier();
			setState(321);
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public SingleMultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleMultipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleMultipartIdentifierContext singleMultipartIdentifier() throws RecognitionException {
		SingleMultipartIdentifierContext _localctx = new SingleMultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_singleMultipartIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			multipartIdentifier();
			setState(324);
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public SingleFunctionIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleFunctionIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleFunctionIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleFunctionIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleFunctionIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleFunctionIdentifierContext singleFunctionIdentifier() throws RecognitionException {
		SingleFunctionIdentifierContext _localctx = new SingleFunctionIdentifierContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_singleFunctionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			functionIdentifier();
			setState(327);
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public SingleDataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleDataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleDataTypeContext singleDataType() throws RecognitionException {
		SingleDataTypeContext _localctx = new SingleDataTypeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_singleDataType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(329);
			dataType();
			setState(330);
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
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
		public SingleTableSchemaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableSchema; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleTableSchema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleTableSchema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleTableSchema(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableSchemaContext singleTableSchema() throws RecognitionException {
		SingleTableSchemaContext _localctx = new SingleTableSchemaContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_singleTableSchema);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(332);
			colTypeList();
			setState(333);
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
		public TerminalNode EXPLAIN() { return getToken(ArcticSqlCommandParser.EXPLAIN, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode LOGICAL() { return getToken(ArcticSqlCommandParser.LOGICAL, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticSqlCommandParser.FORMATTED, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public TerminalNode CODEGEN() { return getToken(ArcticSqlCommandParser.CODEGEN, 0); }
		public TerminalNode COST() { return getToken(ArcticSqlCommandParser.COST, 0); }
		public ExplainContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterExplain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitExplain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitExplain(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ResetConfigurationContext extends StatementContext {
		public TerminalNode RESET() { return getToken(ArcticSqlCommandParser.RESET, 0); }
		public ResetConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterResetConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitResetConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitResetConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AlterViewQueryContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public AlterViewQueryContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAlterViewQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAlterViewQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAlterViewQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UseContext extends StatementContext {
		public TerminalNode USE() { return getToken(ArcticSqlCommandParser.USE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public UseContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUse(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropNamespaceContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode RESTRICT() { return getToken(ArcticSqlCommandParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(ArcticSqlCommandParser.CASCADE, 0); }
		public DropNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTempViewUsingContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode GLOBAL() { return getToken(ArcticSqlCommandParser.GLOBAL, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public CreateTempViewUsingContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateTempViewUsing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateTempViewUsing(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateTempViewUsing(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTableContext extends StatementContext {
		public MultipartIdentifierContext from;
		public MultipartIdentifierContext to;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode RENAME() { return getToken(ArcticSqlCommandParser.RENAME, 0); }
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public RenameTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRenameTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRenameTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRenameTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FailNativeCommandContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode ROLE() { return getToken(ArcticSqlCommandParser.ROLE, 0); }
		public UnsupportedHiveNativeCommandsContext unsupportedHiveNativeCommands() {
			return getRuleContext(UnsupportedHiveNativeCommandsContext.class,0);
		}
		public FailNativeCommandContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFailNativeCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFailNativeCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFailNativeCommand(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetCatalogContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode CATALOG() { return getToken(ArcticSqlCommandParser.CATALOG, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public SetCatalogContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetCatalog(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetCatalog(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetCatalog(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ClearCacheContext extends StatementContext {
		public TerminalNode CLEAR() { return getToken(ArcticSqlCommandParser.CLEAR, 0); }
		public TerminalNode CACHE() { return getToken(ArcticSqlCommandParser.CACHE, 0); }
		public ClearCacheContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterClearCache(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitClearCache(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitClearCache(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropViewContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public DropViewContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropView(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTablesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode TABLES() { return getToken(ArcticSqlCommandParser.TABLES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public ShowTablesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowTables(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RecoverPartitionsContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode RECOVER() { return getToken(ArcticSqlCommandParser.RECOVER, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticSqlCommandParser.PARTITIONS, 0); }
		public RecoverPartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRecoverPartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRecoverPartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRecoverPartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropIndexContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode INDEX() { return getToken(ArcticSqlCommandParser.INDEX, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public DropIndexContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropIndex(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCatalogsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode CATALOGS() { return getToken(ArcticSqlCommandParser.CATALOGS, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public ShowCatalogsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowCatalogs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowCatalogs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowCatalogs(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCurrentNamespaceContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticSqlCommandParser.CURRENT, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public ShowCurrentNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowCurrentNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowCurrentNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowCurrentNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTablePartitionContext extends StatementContext {
		public PartitionSpecContext from;
		public PartitionSpecContext to;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode RENAME() { return getToken(ArcticSqlCommandParser.RENAME, 0); }
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
		public List<PartitionSpecContext> partitionSpec() {
			return getRuleContexts(PartitionSpecContext.class);
		}
		public PartitionSpecContext partitionSpec(int i) {
			return getRuleContext(PartitionSpecContext.class,i);
		}
		public RenameTablePartitionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRenameTablePartition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRenameTablePartition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRenameTablePartition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RepairTableContext extends StatementContext {
		public Token option;
		public TerminalNode MSCK() { return getToken(ArcticSqlCommandParser.MSCK, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticSqlCommandParser.REPAIR, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode PARTITIONS() { return getToken(ArcticSqlCommandParser.PARTITIONS, 0); }
		public TerminalNode ADD() { return getToken(ArcticSqlCommandParser.ADD, 0); }
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode SYNC() { return getToken(ArcticSqlCommandParser.SYNC, 0); }
		public RepairTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRepairTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRepairTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRepairTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshResourceContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(ArcticSqlCommandParser.REFRESH, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public RefreshResourceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRefreshResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRefreshResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRefreshResource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCreateTableContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode SERDE() { return getToken(ArcticSqlCommandParser.SERDE, 0); }
		public ShowCreateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowCreateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowNamespacesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public NamespacesContext namespaces() {
			return getRuleContext(NamespacesContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public ShowNamespacesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowNamespaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowNamespaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowNamespaces(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext ns;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public List<TerminalNode> FROM() { return getTokens(ArcticSqlCommandParser.FROM); }
		public TerminalNode FROM(int i) {
			return getToken(ArcticSqlCommandParser.FROM, i);
		}
		public List<TerminalNode> IN() { return getTokens(ArcticSqlCommandParser.IN); }
		public TerminalNode IN(int i) {
			return getToken(ArcticSqlCommandParser.IN, i);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowColumns(this);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public ReplaceTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterReplaceTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitReplaceTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitReplaceTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AnalyzeTablesContext extends StatementContext {
		public TerminalNode ANALYZE() { return getToken(ArcticSqlCommandParser.ANALYZE, 0); }
		public TerminalNode TABLES() { return getToken(ArcticSqlCommandParser.TABLES, 0); }
		public TerminalNode COMPUTE() { return getToken(ArcticSqlCommandParser.COMPUTE, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticSqlCommandParser.STATISTICS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public AnalyzeTablesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAnalyzeTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAnalyzeTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAnalyzeTables(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AddTablePartitionContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArcticSqlCommandParser.ADD, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public List<PartitionSpecLocationContext> partitionSpecLocation() {
			return getRuleContexts(PartitionSpecLocationContext.class);
		}
		public PartitionSpecLocationContext partitionSpecLocation(int i) {
			return getRuleContext(PartitionSpecLocationContext.class,i);
		}
		public AddTablePartitionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAddTablePartition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAddTablePartition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAddTablePartition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetNamespaceLocationContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public SetNamespaceLocationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetNamespaceLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetNamespaceLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetNamespaceLocation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshTableContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(ArcticSqlCommandParser.REFRESH, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public RefreshTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRefreshTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRefreshTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRefreshTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetNamespacePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode DBPROPERTIES() { return getToken(ArcticSqlCommandParser.DBPROPERTIES, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticSqlCommandParser.PROPERTIES, 0); }
		public SetNamespacePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetNamespaceProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetNamespaceProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetNamespaceProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ManageResourceContext extends StatementContext {
		public Token op;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArcticSqlCommandParser.ADD, 0); }
		public TerminalNode LIST() { return getToken(ArcticSqlCommandParser.LIST, 0); }
		public ManageResourceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterManageResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitManageResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitManageResource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetQuotedConfigurationContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
		public ConfigValueContext configValue() {
			return getRuleContext(ConfigValueContext.class,0);
		}
		public SetQuotedConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetQuotedConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetQuotedConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetQuotedConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AnalyzeContext extends StatementContext {
		public TerminalNode ANALYZE() { return getToken(ArcticSqlCommandParser.ANALYZE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode COMPUTE() { return getToken(ArcticSqlCommandParser.COMPUTE, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticSqlCommandParser.STATISTICS, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public TerminalNode ALL() { return getToken(ArcticSqlCommandParser.ALL, 0); }
		public AnalyzeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAnalyze(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAnalyze(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAnalyze(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateFunctionContext extends StatementContext {
		public Token className;
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticSqlCommandParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public List<ResourceContext> resource() {
			return getRuleContexts(ResourceContext.class);
		}
		public ResourceContext resource(int i) {
			return getRuleContext(ResourceContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public CreateFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class HiveReplaceColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterHiveReplaceColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitHiveReplaceColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitHiveReplaceColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CommentNamespaceContext extends StatementContext {
		public Token comment;
		public TerminalNode COMMENT() { return getToken(ArcticSqlCommandParser.COMMENT, 0); }
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IS() { return getToken(ArcticSqlCommandParser.IS, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public CommentNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCommentNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCommentNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCommentNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ResetQuotedConfigurationContext extends StatementContext {
		public TerminalNode RESET() { return getToken(ArcticSqlCommandParser.RESET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public ResetQuotedConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterResetQuotedConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitResetQuotedConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitResetQuotedConfiguration(this);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public CreateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateTable(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDmlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDmlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDmlStatement(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTableLikeContext extends StatementContext {
		public TableIdentifierContext target;
		public TableIdentifierContext source;
		public PropertyListContext tableProps;
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public List<TableIdentifierContext> tableIdentifier() {
			return getRuleContexts(TableIdentifierContext.class);
		}
		public TableIdentifierContext tableIdentifier(int i) {
			return getRuleContext(TableIdentifierContext.class,i);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
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
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(ArcticSqlCommandParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(ArcticSqlCommandParser.TBLPROPERTIES, i);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateTableLike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateTableLike(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateTableLike(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UncacheTableContext extends StatementContext {
		public TerminalNode UNCACHE() { return getToken(ArcticSqlCommandParser.UNCACHE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public UncacheTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUncacheTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUncacheTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUncacheTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropFunctionContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticSqlCommandParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public DropFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeRelationContext extends StatementContext {
		public Token option;
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticSqlCommandParser.DESCRIBE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public DescribeColNameContext describeColName() {
			return getRuleContext(DescribeColNameContext.class,0);
		}
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticSqlCommandParser.FORMATTED, 0); }
		public DescribeRelationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDescribeRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDescribeRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDescribeRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LoadDataContext extends StatementContext {
		public Token path;
		public TerminalNode LOAD() { return getToken(ArcticSqlCommandParser.LOAD, 0); }
		public TerminalNode DATA() { return getToken(ArcticSqlCommandParser.DATA, 0); }
		public TerminalNode INPATH() { return getToken(ArcticSqlCommandParser.INPATH, 0); }
		public TerminalNode INTO() { return getToken(ArcticSqlCommandParser.INTO, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public LoadDataContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLoadData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLoadData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLoadData(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowPartitionsContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticSqlCommandParser.PARTITIONS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public ShowPartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowPartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowPartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowPartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeFunctionContext extends StatementContext {
		public TerminalNode FUNCTION() { return getToken(ArcticSqlCommandParser.FUNCTION, 0); }
		public DescribeFuncNameContext describeFuncName() {
			return getRuleContext(DescribeFuncNameContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticSqlCommandParser.DESCRIBE, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public DescribeFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDescribeFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDescribeFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDescribeFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTableColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext from;
		public ErrorCapturingIdentifierContext to;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode RENAME() { return getToken(ArcticSqlCommandParser.RENAME, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRenameTableColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRenameTableColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRenameTableColumn(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterStatementDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitStatementDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitStatementDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class HiveChangeColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext colName;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode CHANGE() { return getToken(ArcticSqlCommandParser.CHANGE, 0); }
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
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public HiveChangeColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterHiveChangeColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitHiveChangeColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitHiveChangeColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTimeZoneContext extends StatementContext {
		public Token timezone;
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode TIME() { return getToken(ArcticSqlCommandParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(ArcticSqlCommandParser.ZONE, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public SetTimeZoneContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetTimeZone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetTimeZone(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetTimeZone(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeQueryContext extends StatementContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticSqlCommandParser.DESCRIBE, 0); }
		public TerminalNode QUERY() { return getToken(ArcticSqlCommandParser.QUERY, 0); }
		public DescribeQueryContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDescribeQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDescribeQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDescribeQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TruncateTableContext extends StatementContext {
		public TerminalNode TRUNCATE() { return getToken(ArcticSqlCommandParser.TRUNCATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TruncateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTruncateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTruncateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTruncateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTableSerDeContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode SERDE() { return getToken(ArcticSqlCommandParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public SetTableSerDeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetTableSerDe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetTableSerDe(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetTableSerDe(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateViewContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public IdentifierCommentListContext identifierCommentList() {
			return getRuleContext(IdentifierCommentListContext.class,0);
		}
		public List<CommentSpecContext> commentSpec() {
			return getRuleContexts(CommentSpecContext.class);
		}
		public CommentSpecContext commentSpec(int i) {
			return getRuleContext(CommentSpecContext.class,i);
		}
		public List<TerminalNode> PARTITIONED() { return getTokens(ArcticSqlCommandParser.PARTITIONED); }
		public TerminalNode PARTITIONED(int i) {
			return getToken(ArcticSqlCommandParser.PARTITIONED, i);
		}
		public List<TerminalNode> ON() { return getTokens(ArcticSqlCommandParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(ArcticSqlCommandParser.ON, i);
		}
		public List<IdentifierListContext> identifierList() {
			return getRuleContexts(IdentifierListContext.class);
		}
		public IdentifierListContext identifierList(int i) {
			return getRuleContext(IdentifierListContext.class,i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(ArcticSqlCommandParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(ArcticSqlCommandParser.TBLPROPERTIES, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public TerminalNode GLOBAL() { return getToken(ArcticSqlCommandParser.GLOBAL, 0); }
		public CreateViewContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateView(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTablePartitionsContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public List<PartitionSpecContext> partitionSpec() {
			return getRuleContexts(PartitionSpecContext.class);
		}
		public PartitionSpecContext partitionSpec(int i) {
			return getRuleContext(PartitionSpecContext.class,i);
		}
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public TerminalNode PURGE() { return getToken(ArcticSqlCommandParser.PURGE, 0); }
		public DropTablePartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropTablePartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropTablePartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropTablePartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetConfigurationContext extends StatementContext {
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
		public SetConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTableContext extends StatementContext {
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode PURGE() { return getToken(ArcticSqlCommandParser.PURGE, 0); }
		public DropTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTableExtendedContext extends StatementContext {
		public MultipartIdentifierContext ns;
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public ShowTableExtendedContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowTableExtended(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowTableExtended(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowTableExtended(this);
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
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticSqlCommandParser.DESCRIBE, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public DescribeNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDescribeNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDescribeNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDescribeNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AlterTableAlterColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext column;
		public List<TerminalNode> ALTER() { return getTokens(ArcticSqlCommandParser.ALTER); }
		public TerminalNode ALTER(int i) {
			return getToken(ArcticSqlCommandParser.ALTER, i);
		}
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public TerminalNode CHANGE() { return getToken(ArcticSqlCommandParser.CHANGE, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public AlterColumnActionContext alterColumnAction() {
			return getRuleContext(AlterColumnActionContext.class,0);
		}
		public AlterTableAlterColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAlterTableAlterColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAlterTableAlterColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAlterTableAlterColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshFunctionContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(ArcticSqlCommandParser.REFRESH, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticSqlCommandParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public RefreshFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRefreshFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRefreshFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRefreshFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CommentTableContext extends StatementContext {
		public Token comment;
		public TerminalNode COMMENT() { return getToken(ArcticSqlCommandParser.COMMENT, 0); }
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IS() { return getToken(ArcticSqlCommandParser.IS, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public CommentTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCommentTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCommentTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCommentTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateIndexContext extends StatementContext {
		public IdentifierContext indexType;
		public MultipartIdentifierPropertyListContext columns;
		public PropertyListContext options;
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(ArcticSqlCommandParser.INDEX, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public MultipartIdentifierPropertyListContext multipartIdentifierPropertyList() {
			return getRuleContext(MultipartIdentifierPropertyListContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public CreateIndexContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateIndex(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UseNamespaceContext extends StatementContext {
		public TerminalNode USE() { return getToken(ArcticSqlCommandParser.USE, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public UseNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUseNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUseNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUseNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateNamespaceContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
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
		public List<TerminalNode> WITH() { return getTokens(ArcticSqlCommandParser.WITH); }
		public TerminalNode WITH(int i) {
			return getToken(ArcticSqlCommandParser.WITH, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public List<TerminalNode> DBPROPERTIES() { return getTokens(ArcticSqlCommandParser.DBPROPERTIES); }
		public TerminalNode DBPROPERTIES(int i) {
			return getToken(ArcticSqlCommandParser.DBPROPERTIES, i);
		}
		public List<TerminalNode> PROPERTIES() { return getTokens(ArcticSqlCommandParser.PROPERTIES); }
		public TerminalNode PROPERTIES(int i) {
			return getToken(ArcticSqlCommandParser.PROPERTIES, i);
		}
		public CreateNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTblPropertiesContext extends StatementContext {
		public MultipartIdentifierContext table;
		public PropertyKeyContext key;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public PropertyKeyContext propertyKey() {
			return getRuleContext(PropertyKeyContext.class,0);
		}
		public ShowTblPropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowTblProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowTblProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowTblProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnsetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode UNSET() { return getToken(ArcticSqlCommandParser.UNSET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public UnsetTablePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUnsetTableProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUnsetTableProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUnsetTableProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTableLocationContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public SetTableLocationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetTableLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetTableLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetTableLocation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTableColumnsContext extends StatementContext {
		public MultipartIdentifierListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public DropTableColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDropTableColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDropTableColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDropTableColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowViewsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticSqlCommandParser.VIEWS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public ShowViewsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowViews(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowViews(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowViews(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowFunctionsContext extends StatementContext {
		public MultipartIdentifierContext ns;
		public MultipartIdentifierContext legacy;
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticSqlCommandParser.FUNCTIONS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public ShowFunctionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterShowFunctions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitShowFunctions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitShowFunctions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CacheTableContext extends StatementContext {
		public PropertyListContext options;
		public TerminalNode CACHE() { return getToken(ArcticSqlCommandParser.CACHE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LAZY() { return getToken(ArcticSqlCommandParser.LAZY, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public CacheTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCacheTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCacheTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCacheTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AddTableColumnsContext extends StatementContext {
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ArcticSqlCommandParser.ADD, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() {
			return getRuleContext(QualifiedColTypeWithPositionListContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public AddTableColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAddTableColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAddTableColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAddTableColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public SetTablePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetTableProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetTableProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetTableProperties(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_statement);
		int _la;
		try {
			int _alt;
			setState(1136);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				_localctx = new StatementDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(335);
				query();
				}
				break;
			case 2:
				_localctx = new DmlStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(337);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(336);
					ctes();
					}
				}

				setState(339);
				dmlStatementNoWith();
				}
				break;
			case 3:
				_localctx = new UseContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(340);
				match(USE);
				setState(341);
				multipartIdentifier();
				}
				break;
			case 4:
				_localctx = new UseNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(342);
				match(USE);
				setState(343);
				namespace();
				setState(344);
				multipartIdentifier();
				}
				break;
			case 5:
				_localctx = new SetCatalogContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(346);
				match(SET);
				setState(347);
				match(CATALOG);
				setState(350);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(348);
					identifier();
					}
					break;
				case 2:
					{
					setState(349);
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
				setState(352);
				match(CREATE);
				setState(353);
				namespace();
				setState(357);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(354);
					match(IF);
					setState(355);
					match(NOT);
					setState(356);
					match(EXISTS);
					}
					break;
				}
				setState(359);
				multipartIdentifier();
				setState(367);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==LOCATION || _la==WITH) {
					{
					setState(365);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(360);
						commentSpec();
						}
						break;
					case LOCATION:
						{
						setState(361);
						locationSpec();
						}
						break;
					case WITH:
						{
						{
						setState(362);
						match(WITH);
						setState(363);
						_la = _input.LA(1);
						if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(364);
						propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(369);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 7:
				_localctx = new SetNamespacePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(370);
				match(ALTER);
				setState(371);
				namespace();
				setState(372);
				multipartIdentifier();
				setState(373);
				match(SET);
				setState(374);
				_la = _input.LA(1);
				if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(375);
				propertyList();
				}
				break;
			case 8:
				_localctx = new SetNamespaceLocationContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(377);
				match(ALTER);
				setState(378);
				namespace();
				setState(379);
				multipartIdentifier();
				setState(380);
				match(SET);
				setState(381);
				locationSpec();
				}
				break;
			case 9:
				_localctx = new DropNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(383);
				match(DROP);
				setState(384);
				namespace();
				setState(387);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(385);
					match(IF);
					setState(386);
					match(EXISTS);
					}
					break;
				}
				setState(389);
				multipartIdentifier();
				setState(391);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CASCADE || _la==RESTRICT) {
					{
					setState(390);
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
				setState(393);
				match(SHOW);
				setState(394);
				namespaces();
				setState(397);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(395);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(396);
					multipartIdentifier();
					}
				}

				setState(403);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(400);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(399);
						match(LIKE);
						}
					}

					setState(402);
					((ShowNamespacesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 11:
				_localctx = new CreateTableContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(405);
				createTableHeader();
				setState(410);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
				case 1:
					{
					setState(406);
					match(LEFT_PAREN);
					setState(407);
					colTypeList();
					setState(408);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(413);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(412);
					tableProvider();
					}
				}

				setState(415);
				createTableClauses();
				setState(420);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 198)) & ~0x3f) == 0 && ((1L << (_la - 198)) & ((1L << (REDUCE - 198)) | (1L << (SELECT - 198)) | (1L << (TABLE - 198)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(417);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(416);
						match(AS);
						}
					}

					setState(419);
					query();
					}
				}

				}
				break;
			case 12:
				_localctx = new CreateTableLikeContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(422);
				match(CREATE);
				setState(423);
				match(TABLE);
				setState(427);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(424);
					match(IF);
					setState(425);
					match(NOT);
					setState(426);
					match(EXISTS);
					}
					break;
				}
				setState(429);
				((CreateTableLikeContext)_localctx).target = tableIdentifier();
				setState(430);
				match(LIKE);
				setState(431);
				((CreateTableLikeContext)_localctx).source = tableIdentifier();
				setState(440);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LOCATION || ((((_la - 215)) & ~0x3f) == 0 && ((1L << (_la - 215)) & ((1L << (ROW - 215)) | (1L << (STORED - 215)) | (1L << (TBLPROPERTIES - 215)) | (1L << (USING - 215)))) != 0)) {
					{
					setState(438);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case USING:
						{
						setState(432);
						tableProvider();
						}
						break;
					case ROW:
						{
						setState(433);
						rowFormat();
						}
						break;
					case STORED:
						{
						setState(434);
						createFileFormat();
						}
						break;
					case LOCATION:
						{
						setState(435);
						locationSpec();
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(436);
						match(TBLPROPERTIES);
						setState(437);
						((CreateTableLikeContext)_localctx).tableProps = propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(442);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 13:
				_localctx = new ReplaceTableContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(443);
				replaceTableHeader();
				setState(448);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(444);
					match(LEFT_PAREN);
					setState(445);
					colTypeList();
					setState(446);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(451);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(450);
					tableProvider();
					}
				}

				setState(453);
				createTableClauses();
				setState(458);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 198)) & ~0x3f) == 0 && ((1L << (_la - 198)) & ((1L << (REDUCE - 198)) | (1L << (SELECT - 198)) | (1L << (TABLE - 198)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(455);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(454);
						match(AS);
						}
					}

					setState(457);
					query();
					}
				}

				}
				break;
			case 14:
				_localctx = new AnalyzeContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(460);
				match(ANALYZE);
				setState(461);
				match(TABLE);
				setState(462);
				multipartIdentifier();
				setState(464);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(463);
					partitionSpec();
					}
				}

				setState(466);
				match(COMPUTE);
				setState(467);
				match(STATISTICS);
				setState(475);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(468);
					identifier();
					}
					break;
				case 2:
					{
					setState(469);
					match(FOR);
					setState(470);
					match(COLUMNS);
					setState(471);
					identifierSeq();
					}
					break;
				case 3:
					{
					setState(472);
					match(FOR);
					setState(473);
					match(ALL);
					setState(474);
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
				setState(477);
				match(ANALYZE);
				setState(478);
				match(TABLES);
				setState(481);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(479);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(480);
					multipartIdentifier();
					}
				}

				setState(483);
				match(COMPUTE);
				setState(484);
				match(STATISTICS);
				setState(486);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(485);
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
				setState(488);
				match(ALTER);
				setState(489);
				match(TABLE);
				setState(490);
				multipartIdentifier();
				setState(491);
				match(ADD);
				setState(492);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(493);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				}
				break;
			case 17:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(495);
				match(ALTER);
				setState(496);
				match(TABLE);
				setState(497);
				multipartIdentifier();
				setState(498);
				match(ADD);
				setState(499);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(500);
				match(LEFT_PAREN);
				setState(501);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(502);
				match(RIGHT_PAREN);
				}
				break;
			case 18:
				_localctx = new RenameTableColumnContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(504);
				match(ALTER);
				setState(505);
				match(TABLE);
				setState(506);
				((RenameTableColumnContext)_localctx).table = multipartIdentifier();
				setState(507);
				match(RENAME);
				setState(508);
				match(COLUMN);
				setState(509);
				((RenameTableColumnContext)_localctx).from = multipartIdentifier();
				setState(510);
				match(TO);
				setState(511);
				((RenameTableColumnContext)_localctx).to = errorCapturingIdentifier();
				}
				break;
			case 19:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 19);
				{
				setState(513);
				match(ALTER);
				setState(514);
				match(TABLE);
				setState(515);
				multipartIdentifier();
				setState(516);
				match(DROP);
				setState(517);
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
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(518);
					match(IF);
					setState(519);
					match(EXISTS);
					}
				}

				setState(522);
				match(LEFT_PAREN);
				setState(523);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				setState(524);
				match(RIGHT_PAREN);
				}
				break;
			case 20:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(526);
				match(ALTER);
				setState(527);
				match(TABLE);
				setState(528);
				multipartIdentifier();
				setState(529);
				match(DROP);
				setState(530);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(533);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(531);
					match(IF);
					setState(532);
					match(EXISTS);
					}
					break;
				}
				setState(535);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				}
				break;
			case 21:
				_localctx = new RenameTableContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(537);
				match(ALTER);
				setState(538);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(539);
				((RenameTableContext)_localctx).from = multipartIdentifier();
				setState(540);
				match(RENAME);
				setState(541);
				match(TO);
				setState(542);
				((RenameTableContext)_localctx).to = multipartIdentifier();
				}
				break;
			case 22:
				_localctx = new SetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(544);
				match(ALTER);
				setState(545);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(546);
				multipartIdentifier();
				setState(547);
				match(SET);
				setState(548);
				match(TBLPROPERTIES);
				setState(549);
				propertyList();
				}
				break;
			case 23:
				_localctx = new UnsetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(551);
				match(ALTER);
				setState(552);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(553);
				multipartIdentifier();
				setState(554);
				match(UNSET);
				setState(555);
				match(TBLPROPERTIES);
				setState(558);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(556);
					match(IF);
					setState(557);
					match(EXISTS);
					}
				}

				setState(560);
				propertyList();
				}
				break;
			case 24:
				_localctx = new AlterTableAlterColumnContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(562);
				match(ALTER);
				setState(563);
				match(TABLE);
				setState(564);
				((AlterTableAlterColumnContext)_localctx).table = multipartIdentifier();
				setState(565);
				_la = _input.LA(1);
				if ( !(_la==ALTER || _la==CHANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(567);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(566);
					match(COLUMN);
					}
					break;
				}
				setState(569);
				((AlterTableAlterColumnContext)_localctx).column = multipartIdentifier();
				setState(571);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==COMMENT || _la==DROP || _la==FIRST || _la==SET || _la==TYPE) {
					{
					setState(570);
					alterColumnAction();
					}
				}

				}
				break;
			case 25:
				_localctx = new HiveChangeColumnContext(_localctx);
				enterOuterAlt(_localctx, 25);
				{
				setState(573);
				match(ALTER);
				setState(574);
				match(TABLE);
				setState(575);
				((HiveChangeColumnContext)_localctx).table = multipartIdentifier();
				setState(577);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(576);
					partitionSpec();
					}
				}

				setState(579);
				match(CHANGE);
				setState(581);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(580);
					match(COLUMN);
					}
					break;
				}
				setState(583);
				((HiveChangeColumnContext)_localctx).colName = multipartIdentifier();
				setState(584);
				colType();
				setState(586);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==FIRST) {
					{
					setState(585);
					colPosition();
					}
				}

				}
				break;
			case 26:
				_localctx = new HiveReplaceColumnsContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(588);
				match(ALTER);
				setState(589);
				match(TABLE);
				setState(590);
				((HiveReplaceColumnsContext)_localctx).table = multipartIdentifier();
				setState(592);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(591);
					partitionSpec();
					}
				}

				setState(594);
				match(REPLACE);
				setState(595);
				match(COLUMNS);
				setState(596);
				match(LEFT_PAREN);
				setState(597);
				((HiveReplaceColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(598);
				match(RIGHT_PAREN);
				}
				break;
			case 27:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(600);
				match(ALTER);
				setState(601);
				match(TABLE);
				setState(602);
				multipartIdentifier();
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
				match(SET);
				setState(607);
				match(SERDE);
				setState(608);
				match(STRING);
				setState(612);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(609);
					match(WITH);
					setState(610);
					match(SERDEPROPERTIES);
					setState(611);
					propertyList();
					}
				}

				}
				break;
			case 28:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(614);
				match(ALTER);
				setState(615);
				match(TABLE);
				setState(616);
				multipartIdentifier();
				setState(618);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(617);
					partitionSpec();
					}
				}

				setState(620);
				match(SET);
				setState(621);
				match(SERDEPROPERTIES);
				setState(622);
				propertyList();
				}
				break;
			case 29:
				_localctx = new AddTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(624);
				match(ALTER);
				setState(625);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(626);
				multipartIdentifier();
				setState(627);
				match(ADD);
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(628);
					match(IF);
					setState(629);
					match(NOT);
					setState(630);
					match(EXISTS);
					}
				}

				setState(634); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(633);
					partitionSpecLocation();
					}
					}
					setState(636); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==PARTITION );
				}
				break;
			case 30:
				_localctx = new RenameTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(638);
				match(ALTER);
				setState(639);
				match(TABLE);
				setState(640);
				multipartIdentifier();
				setState(641);
				((RenameTablePartitionContext)_localctx).from = partitionSpec();
				setState(642);
				match(RENAME);
				setState(643);
				match(TO);
				setState(644);
				((RenameTablePartitionContext)_localctx).to = partitionSpec();
				}
				break;
			case 31:
				_localctx = new DropTablePartitionsContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(646);
				match(ALTER);
				setState(647);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(648);
				multipartIdentifier();
				setState(649);
				match(DROP);
				setState(652);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(650);
					match(IF);
					setState(651);
					match(EXISTS);
					}
				}

				setState(654);
				partitionSpec();
				setState(659);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(655);
					match(COMMA);
					setState(656);
					partitionSpec();
					}
					}
					setState(661);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(663);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(662);
					match(PURGE);
					}
				}

				}
				break;
			case 32:
				_localctx = new SetTableLocationContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(665);
				match(ALTER);
				setState(666);
				match(TABLE);
				setState(667);
				multipartIdentifier();
				setState(669);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(668);
					partitionSpec();
					}
				}

				setState(671);
				match(SET);
				setState(672);
				locationSpec();
				}
				break;
			case 33:
				_localctx = new RecoverPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 33);
				{
				setState(674);
				match(ALTER);
				setState(675);
				match(TABLE);
				setState(676);
				multipartIdentifier();
				setState(677);
				match(RECOVER);
				setState(678);
				match(PARTITIONS);
				}
				break;
			case 34:
				_localctx = new DropTableContext(_localctx);
				enterOuterAlt(_localctx, 34);
				{
				setState(680);
				match(DROP);
				setState(681);
				match(TABLE);
				setState(684);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(682);
					match(IF);
					setState(683);
					match(EXISTS);
					}
					break;
				}
				setState(686);
				multipartIdentifier();
				setState(688);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(687);
					match(PURGE);
					}
				}

				}
				break;
			case 35:
				_localctx = new DropViewContext(_localctx);
				enterOuterAlt(_localctx, 35);
				{
				setState(690);
				match(DROP);
				setState(691);
				match(VIEW);
				setState(694);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
				case 1:
					{
					setState(692);
					match(IF);
					setState(693);
					match(EXISTS);
					}
					break;
				}
				setState(696);
				multipartIdentifier();
				}
				break;
			case 36:
				_localctx = new CreateViewContext(_localctx);
				enterOuterAlt(_localctx, 36);
				{
				setState(697);
				match(CREATE);
				setState(700);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(698);
					match(OR);
					setState(699);
					match(REPLACE);
					}
				}

				setState(706);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL || _la==TEMPORARY) {
					{
					setState(703);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==GLOBAL) {
						{
						setState(702);
						match(GLOBAL);
						}
					}

					setState(705);
					match(TEMPORARY);
					}
				}

				setState(708);
				match(VIEW);
				setState(712);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
				case 1:
					{
					setState(709);
					match(IF);
					setState(710);
					match(NOT);
					setState(711);
					match(EXISTS);
					}
					break;
				}
				setState(714);
				multipartIdentifier();
				setState(716);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(715);
					identifierCommentList();
					}
				}

				setState(726);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==PARTITIONED || _la==TBLPROPERTIES) {
					{
					setState(724);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(718);
						commentSpec();
						}
						break;
					case PARTITIONED:
						{
						{
						setState(719);
						match(PARTITIONED);
						setState(720);
						match(ON);
						setState(721);
						identifierList();
						}
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(722);
						match(TBLPROPERTIES);
						setState(723);
						propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(728);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(729);
				match(AS);
				setState(730);
				query();
				}
				break;
			case 37:
				_localctx = new CreateTempViewUsingContext(_localctx);
				enterOuterAlt(_localctx, 37);
				{
				setState(732);
				match(CREATE);
				setState(735);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(733);
					match(OR);
					setState(734);
					match(REPLACE);
					}
				}

				setState(738);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL) {
					{
					setState(737);
					match(GLOBAL);
					}
				}

				setState(740);
				match(TEMPORARY);
				setState(741);
				match(VIEW);
				setState(742);
				tableIdentifier();
				setState(747);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(743);
					match(LEFT_PAREN);
					setState(744);
					colTypeList();
					setState(745);
					match(RIGHT_PAREN);
					}
				}

				setState(749);
				tableProvider();
				setState(752);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(750);
					match(OPTIONS);
					setState(751);
					propertyList();
					}
				}

				}
				break;
			case 38:
				_localctx = new AlterViewQueryContext(_localctx);
				enterOuterAlt(_localctx, 38);
				{
				setState(754);
				match(ALTER);
				setState(755);
				match(VIEW);
				setState(756);
				multipartIdentifier();
				setState(758);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(757);
					match(AS);
					}
				}

				setState(760);
				query();
				}
				break;
			case 39:
				_localctx = new CreateFunctionContext(_localctx);
				enterOuterAlt(_localctx, 39);
				{
				setState(762);
				match(CREATE);
				setState(765);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(763);
					match(OR);
					setState(764);
					match(REPLACE);
					}
				}

				setState(768);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(767);
					match(TEMPORARY);
					}
				}

				setState(770);
				match(FUNCTION);
				setState(774);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
				case 1:
					{
					setState(771);
					match(IF);
					setState(772);
					match(NOT);
					setState(773);
					match(EXISTS);
					}
					break;
				}
				setState(776);
				multipartIdentifier();
				setState(777);
				match(AS);
				setState(778);
				((CreateFunctionContext)_localctx).className = match(STRING);
				setState(788);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(779);
					match(USING);
					setState(780);
					resource();
					setState(785);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(781);
						match(COMMA);
						setState(782);
						resource();
						}
						}
						setState(787);
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
				setState(790);
				match(DROP);
				setState(792);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(791);
					match(TEMPORARY);
					}
				}

				setState(794);
				match(FUNCTION);
				setState(797);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
				case 1:
					{
					setState(795);
					match(IF);
					setState(796);
					match(EXISTS);
					}
					break;
				}
				setState(799);
				multipartIdentifier();
				}
				break;
			case 41:
				_localctx = new ExplainContext(_localctx);
				enterOuterAlt(_localctx, 41);
				{
				setState(800);
				match(EXPLAIN);
				setState(802);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CODEGEN || _la==COST || ((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & ((1L << (EXTENDED - 91)) | (1L << (FORMATTED - 91)) | (1L << (LOGICAL - 91)))) != 0)) {
					{
					setState(801);
					_la = _input.LA(1);
					if ( !(_la==CODEGEN || _la==COST || ((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & ((1L << (EXTENDED - 91)) | (1L << (FORMATTED - 91)) | (1L << (LOGICAL - 91)))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(804);
				statement();
				}
				break;
			case 42:
				_localctx = new ShowTablesContext(_localctx);
				enterOuterAlt(_localctx, 42);
				{
				setState(805);
				match(SHOW);
				setState(806);
				match(TABLES);
				setState(809);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(807);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(808);
					multipartIdentifier();
					}
				}

				setState(815);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(812);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(811);
						match(LIKE);
						}
					}

					setState(814);
					((ShowTablesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 43:
				_localctx = new ShowTableExtendedContext(_localctx);
				enterOuterAlt(_localctx, 43);
				{
				setState(817);
				match(SHOW);
				setState(818);
				match(TABLE);
				setState(819);
				match(EXTENDED);
				setState(822);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(820);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(821);
					((ShowTableExtendedContext)_localctx).ns = multipartIdentifier();
					}
				}

				setState(824);
				match(LIKE);
				setState(825);
				((ShowTableExtendedContext)_localctx).pattern = match(STRING);
				setState(827);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(826);
					partitionSpec();
					}
				}

				}
				break;
			case 44:
				_localctx = new ShowTblPropertiesContext(_localctx);
				enterOuterAlt(_localctx, 44);
				{
				setState(829);
				match(SHOW);
				setState(830);
				match(TBLPROPERTIES);
				setState(831);
				((ShowTblPropertiesContext)_localctx).table = multipartIdentifier();
				setState(836);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(832);
					match(LEFT_PAREN);
					setState(833);
					((ShowTblPropertiesContext)_localctx).key = propertyKey();
					setState(834);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			case 45:
				_localctx = new ShowColumnsContext(_localctx);
				enterOuterAlt(_localctx, 45);
				{
				setState(838);
				match(SHOW);
				setState(839);
				match(COLUMNS);
				setState(840);
				_la = _input.LA(1);
				if ( !(_la==FROM || _la==IN) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(841);
				((ShowColumnsContext)_localctx).table = multipartIdentifier();
				setState(844);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(842);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(843);
					((ShowColumnsContext)_localctx).ns = multipartIdentifier();
					}
				}

				}
				break;
			case 46:
				_localctx = new ShowViewsContext(_localctx);
				enterOuterAlt(_localctx, 46);
				{
				setState(846);
				match(SHOW);
				setState(847);
				match(VIEWS);
				setState(850);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(848);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(849);
					multipartIdentifier();
					}
				}

				setState(856);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(853);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(852);
						match(LIKE);
						}
					}

					setState(855);
					((ShowViewsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 47:
				_localctx = new ShowPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 47);
				{
				setState(858);
				match(SHOW);
				setState(859);
				match(PARTITIONS);
				setState(860);
				multipartIdentifier();
				setState(862);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(861);
					partitionSpec();
					}
				}

				}
				break;
			case 48:
				_localctx = new ShowFunctionsContext(_localctx);
				enterOuterAlt(_localctx, 48);
				{
				setState(864);
				match(SHOW);
				setState(866);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
				case 1:
					{
					setState(865);
					identifier();
					}
					break;
				}
				setState(868);
				match(FUNCTIONS);
				setState(871);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
				case 1:
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
					((ShowFunctionsContext)_localctx).ns = multipartIdentifier();
					}
					break;
				}
				setState(880);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
				case 1:
					{
					setState(874);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
					case 1:
						{
						setState(873);
						match(LIKE);
						}
						break;
					}
					setState(878);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
					case 1:
						{
						setState(876);
						((ShowFunctionsContext)_localctx).legacy = multipartIdentifier();
						}
						break;
					case 2:
						{
						setState(877);
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
				setState(882);
				match(SHOW);
				setState(883);
				match(CREATE);
				setState(884);
				match(TABLE);
				setState(885);
				multipartIdentifier();
				setState(888);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(886);
					match(AS);
					setState(887);
					match(SERDE);
					}
				}

				}
				break;
			case 50:
				_localctx = new ShowCurrentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 50);
				{
				setState(890);
				match(SHOW);
				setState(891);
				match(CURRENT);
				setState(892);
				namespace();
				}
				break;
			case 51:
				_localctx = new ShowCatalogsContext(_localctx);
				enterOuterAlt(_localctx, 51);
				{
				setState(893);
				match(SHOW);
				setState(894);
				match(CATALOGS);
				setState(899);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(896);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(895);
						match(LIKE);
						}
					}

					setState(898);
					((ShowCatalogsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 52:
				_localctx = new DescribeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 52);
				{
				setState(901);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(902);
				match(FUNCTION);
				setState(904);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(903);
					match(EXTENDED);
					}
					break;
				}
				setState(906);
				describeFuncName();
				}
				break;
			case 53:
				_localctx = new DescribeNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 53);
				{
				setState(907);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(908);
				namespace();
				setState(910);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(909);
					match(EXTENDED);
					}
					break;
				}
				setState(912);
				multipartIdentifier();
				}
				break;
			case 54:
				_localctx = new DescribeRelationContext(_localctx);
				enterOuterAlt(_localctx, 54);
				{
				setState(914);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(916);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
				case 1:
					{
					setState(915);
					match(TABLE);
					}
					break;
				}
				setState(919);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
				case 1:
					{
					setState(918);
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
				setState(921);
				multipartIdentifier();
				setState(923);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(922);
					partitionSpec();
					}
					break;
				}
				setState(926);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(925);
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
				setState(930);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUERY) {
					{
					setState(929);
					match(QUERY);
					}
				}

				setState(932);
				query();
				}
				break;
			case 56:
				_localctx = new CommentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 56);
				{
				setState(933);
				match(COMMENT);
				setState(934);
				match(ON);
				setState(935);
				namespace();
				setState(936);
				multipartIdentifier();
				setState(937);
				match(IS);
				setState(938);
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
				setState(940);
				match(COMMENT);
				setState(941);
				match(ON);
				setState(942);
				match(TABLE);
				setState(943);
				multipartIdentifier();
				setState(944);
				match(IS);
				setState(945);
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
				setState(947);
				match(REFRESH);
				setState(948);
				match(TABLE);
				setState(949);
				multipartIdentifier();
				}
				break;
			case 59:
				_localctx = new RefreshFunctionContext(_localctx);
				enterOuterAlt(_localctx, 59);
				{
				setState(950);
				match(REFRESH);
				setState(951);
				match(FUNCTION);
				setState(952);
				multipartIdentifier();
				}
				break;
			case 60:
				_localctx = new RefreshResourceContext(_localctx);
				enterOuterAlt(_localctx, 60);
				{
				setState(953);
				match(REFRESH);
				setState(961);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
				case 1:
					{
					setState(954);
					match(STRING);
					}
					break;
				case 2:
					{
					setState(958);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(955);
							matchWildcard();
							}
							} 
						}
						setState(960);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
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
				setState(963);
				match(CACHE);
				setState(965);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LAZY) {
					{
					setState(964);
					match(LAZY);
					}
				}

				setState(967);
				match(TABLE);
				setState(968);
				multipartIdentifier();
				setState(971);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(969);
					match(OPTIONS);
					setState(970);
					((CacheTableContext)_localctx).options = propertyList();
					}
				}

				setState(977);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 198)) & ~0x3f) == 0 && ((1L << (_la - 198)) & ((1L << (REDUCE - 198)) | (1L << (SELECT - 198)) | (1L << (TABLE - 198)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(974);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(973);
						match(AS);
						}
					}

					setState(976);
					query();
					}
				}

				}
				break;
			case 62:
				_localctx = new UncacheTableContext(_localctx);
				enterOuterAlt(_localctx, 62);
				{
				setState(979);
				match(UNCACHE);
				setState(980);
				match(TABLE);
				setState(983);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
				case 1:
					{
					setState(981);
					match(IF);
					setState(982);
					match(EXISTS);
					}
					break;
				}
				setState(985);
				multipartIdentifier();
				}
				break;
			case 63:
				_localctx = new ClearCacheContext(_localctx);
				enterOuterAlt(_localctx, 63);
				{
				setState(986);
				match(CLEAR);
				setState(987);
				match(CACHE);
				}
				break;
			case 64:
				_localctx = new LoadDataContext(_localctx);
				enterOuterAlt(_localctx, 64);
				{
				setState(988);
				match(LOAD);
				setState(989);
				match(DATA);
				setState(991);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(990);
					match(LOCAL);
					}
				}

				setState(993);
				match(INPATH);
				setState(994);
				((LoadDataContext)_localctx).path = match(STRING);
				setState(996);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OVERWRITE) {
					{
					setState(995);
					match(OVERWRITE);
					}
				}

				setState(998);
				match(INTO);
				setState(999);
				match(TABLE);
				setState(1000);
				multipartIdentifier();
				setState(1002);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1001);
					partitionSpec();
					}
				}

				}
				break;
			case 65:
				_localctx = new TruncateTableContext(_localctx);
				enterOuterAlt(_localctx, 65);
				{
				setState(1004);
				match(TRUNCATE);
				setState(1005);
				match(TABLE);
				setState(1006);
				multipartIdentifier();
				setState(1008);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1007);
					partitionSpec();
					}
				}

				}
				break;
			case 66:
				_localctx = new RepairTableContext(_localctx);
				enterOuterAlt(_localctx, 66);
				{
				setState(1010);
				match(MSCK);
				setState(1011);
				match(REPAIR);
				setState(1012);
				match(TABLE);
				setState(1013);
				multipartIdentifier();
				setState(1016);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==DROP || _la==SYNC) {
					{
					setState(1014);
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
					setState(1015);
					match(PARTITIONS);
					}
				}

				}
				break;
			case 67:
				_localctx = new ManageResourceContext(_localctx);
				enterOuterAlt(_localctx, 67);
				{
				setState(1018);
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
				setState(1019);
				identifier();
				setState(1023);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1020);
						matchWildcard();
						}
						} 
					}
					setState(1025);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				}
				}
				break;
			case 68:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 68);
				{
				setState(1026);
				match(SET);
				setState(1027);
				match(ROLE);
				setState(1031);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1028);
						matchWildcard();
						}
						} 
					}
					setState(1033);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				}
				}
				break;
			case 69:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 69);
				{
				setState(1034);
				match(SET);
				setState(1035);
				match(TIME);
				setState(1036);
				match(ZONE);
				setState(1037);
				interval();
				}
				break;
			case 70:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 70);
				{
				setState(1038);
				match(SET);
				setState(1039);
				match(TIME);
				setState(1040);
				match(ZONE);
				setState(1041);
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
				setState(1042);
				match(SET);
				setState(1043);
				match(TIME);
				setState(1044);
				match(ZONE);
				setState(1048);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1045);
						matchWildcard();
						}
						} 
					}
					setState(1050);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				}
				}
				break;
			case 72:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 72);
				{
				setState(1051);
				match(SET);
				setState(1052);
				configKey();
				setState(1053);
				match(EQ);
				setState(1054);
				configValue();
				}
				break;
			case 73:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 73);
				{
				setState(1056);
				match(SET);
				setState(1057);
				configKey();
				setState(1065);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1058);
					match(EQ);
					setState(1062);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(1059);
							matchWildcard();
							}
							} 
						}
						setState(1064);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
					}
					}
				}

				}
				break;
			case 74:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 74);
				{
				setState(1067);
				match(SET);
				setState(1071);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1068);
						matchWildcard();
						}
						} 
					}
					setState(1073);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				}
				setState(1074);
				match(EQ);
				setState(1075);
				configValue();
				}
				break;
			case 75:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 75);
				{
				setState(1076);
				match(SET);
				setState(1080);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1077);
						matchWildcard();
						}
						} 
					}
					setState(1082);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				}
				}
				break;
			case 76:
				_localctx = new ResetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 76);
				{
				setState(1083);
				match(RESET);
				setState(1084);
				configKey();
				}
				break;
			case 77:
				_localctx = new ResetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 77);
				{
				setState(1085);
				match(RESET);
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
				break;
			case 78:
				_localctx = new CreateIndexContext(_localctx);
				enterOuterAlt(_localctx, 78);
				{
				setState(1092);
				match(CREATE);
				setState(1093);
				match(INDEX);
				setState(1097);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
				case 1:
					{
					setState(1094);
					match(IF);
					setState(1095);
					match(NOT);
					setState(1096);
					match(EXISTS);
					}
					break;
				}
				setState(1099);
				identifier();
				setState(1100);
				match(ON);
				setState(1102);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
				case 1:
					{
					setState(1101);
					match(TABLE);
					}
					break;
				}
				setState(1104);
				multipartIdentifier();
				setState(1107);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(1105);
					match(USING);
					setState(1106);
					((CreateIndexContext)_localctx).indexType = identifier();
					}
				}

				setState(1109);
				match(LEFT_PAREN);
				setState(1110);
				((CreateIndexContext)_localctx).columns = multipartIdentifierPropertyList();
				setState(1111);
				match(RIGHT_PAREN);
				setState(1114);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1112);
					match(OPTIONS);
					setState(1113);
					((CreateIndexContext)_localctx).options = propertyList();
					}
				}

				}
				break;
			case 79:
				_localctx = new DropIndexContext(_localctx);
				enterOuterAlt(_localctx, 79);
				{
				setState(1116);
				match(DROP);
				setState(1117);
				match(INDEX);
				setState(1120);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
				case 1:
					{
					setState(1118);
					match(IF);
					setState(1119);
					match(EXISTS);
					}
					break;
				}
				setState(1122);
				identifier();
				setState(1123);
				match(ON);
				setState(1125);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
				case 1:
					{
					setState(1124);
					match(TABLE);
					}
					break;
				}
				setState(1127);
				multipartIdentifier();
				}
				break;
			case 80:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 80);
				{
				setState(1129);
				unsupportedHiveNativeCommands();
				setState(1133);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,119,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1130);
						matchWildcard();
						}
						} 
					}
					setState(1135);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,119,_ctx);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterConfigKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitConfigKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitConfigKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigKeyContext configKey() throws RecognitionException {
		ConfigKeyContext _localctx = new ConfigKeyContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_configKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1138);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterConfigValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitConfigValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitConfigValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigValueContext configValue() throws RecognitionException {
		ConfigValueContext _localctx = new ConfigValueContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_configValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1140);
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
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode ROLE() { return getToken(ArcticSqlCommandParser.ROLE, 0); }
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode GRANT() { return getToken(ArcticSqlCommandParser.GRANT, 0); }
		public TerminalNode REVOKE() { return getToken(ArcticSqlCommandParser.REVOKE, 0); }
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticSqlCommandParser.PRINCIPALS, 0); }
		public TerminalNode ROLES() { return getToken(ArcticSqlCommandParser.ROLES, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticSqlCommandParser.CURRENT, 0); }
		public TerminalNode EXPORT() { return getToken(ArcticSqlCommandParser.EXPORT, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode IMPORT() { return getToken(ArcticSqlCommandParser.IMPORT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(ArcticSqlCommandParser.COMPACTIONS, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(ArcticSqlCommandParser.TRANSACTIONS, 0); }
		public TerminalNode INDEXES() { return getToken(ArcticSqlCommandParser.INDEXES, 0); }
		public TerminalNode LOCKS() { return getToken(ArcticSqlCommandParser.LOCKS, 0); }
		public TerminalNode INDEX() { return getToken(ArcticSqlCommandParser.INDEX, 0); }
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode LOCK() { return getToken(ArcticSqlCommandParser.LOCK, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticSqlCommandParser.DATABASE, 0); }
		public TerminalNode UNLOCK() { return getToken(ArcticSqlCommandParser.UNLOCK, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode MACRO() { return getToken(ArcticSqlCommandParser.MACRO, 0); }
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode CLUSTERED() { return getToken(ArcticSqlCommandParser.CLUSTERED, 0); }
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public TerminalNode SORTED() { return getToken(ArcticSqlCommandParser.SORTED, 0); }
		public TerminalNode SKEWED() { return getToken(ArcticSqlCommandParser.SKEWED, 0); }
		public TerminalNode STORED() { return getToken(ArcticSqlCommandParser.STORED, 0); }
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticSqlCommandParser.DIRECTORIES, 0); }
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode LOCATION() { return getToken(ArcticSqlCommandParser.LOCATION, 0); }
		public TerminalNode EXCHANGE() { return getToken(ArcticSqlCommandParser.EXCHANGE, 0); }
		public TerminalNode PARTITION() { return getToken(ArcticSqlCommandParser.PARTITION, 0); }
		public TerminalNode ARCHIVE() { return getToken(ArcticSqlCommandParser.ARCHIVE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(ArcticSqlCommandParser.UNARCHIVE, 0); }
		public TerminalNode TOUCH() { return getToken(ArcticSqlCommandParser.TOUCH, 0); }
		public TerminalNode COMPACT() { return getToken(ArcticSqlCommandParser.COMPACT, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode CONCATENATE() { return getToken(ArcticSqlCommandParser.CONCATENATE, 0); }
		public TerminalNode FILEFORMAT() { return getToken(ArcticSqlCommandParser.FILEFORMAT, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public TerminalNode START() { return getToken(ArcticSqlCommandParser.START, 0); }
		public TerminalNode TRANSACTION() { return getToken(ArcticSqlCommandParser.TRANSACTION, 0); }
		public TerminalNode COMMIT() { return getToken(ArcticSqlCommandParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(ArcticSqlCommandParser.ROLLBACK, 0); }
		public TerminalNode DFS() { return getToken(ArcticSqlCommandParser.DFS, 0); }
		public UnsupportedHiveNativeCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsupportedHiveNativeCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUnsupportedHiveNativeCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUnsupportedHiveNativeCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUnsupportedHiveNativeCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnsupportedHiveNativeCommandsContext unsupportedHiveNativeCommands() throws RecognitionException {
		UnsupportedHiveNativeCommandsContext _localctx = new UnsupportedHiveNativeCommandsContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_unsupportedHiveNativeCommands);
		int _la;
		try {
			setState(1310);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1142);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1143);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1144);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1145);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1146);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(GRANT);
				setState(1148);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
				case 1:
					{
					setState(1147);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1150);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(REVOKE);
				setState(1152);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
				case 1:
					{
					setState(1151);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1154);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1155);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(GRANT);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1156);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1157);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				setState(1159);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
				case 1:
					{
					setState(1158);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(GRANT);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1161);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1162);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(PRINCIPALS);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1163);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1164);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLES);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1165);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1166);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CURRENT);
				setState(1167);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ROLES);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1168);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(EXPORT);
				setState(1169);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1170);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(IMPORT);
				setState(1171);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1172);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1173);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(COMPACTIONS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1174);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1175);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CREATE);
				setState(1176);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TABLE);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1177);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1178);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTIONS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1179);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1180);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEXES);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1181);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1182);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(LOCKS);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1183);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1184);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1185);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1186);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1187);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1188);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1189);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1190);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1191);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1192);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1193);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1194);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1195);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1196);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1197);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1198);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1199);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1200);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1201);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1202);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1203);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1204);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1205);
				tableIdentifier();
				setState(1206);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1207);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(CLUSTERED);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(1209);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1210);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1211);
				tableIdentifier();
				setState(1212);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CLUSTERED);
				setState(1213);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(1215);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1216);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1217);
				tableIdentifier();
				setState(1218);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1219);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SORTED);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(1221);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1222);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1223);
				tableIdentifier();
				setState(1224);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SKEWED);
				setState(1225);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(1227);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1228);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1229);
				tableIdentifier();
				setState(1230);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1231);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(1233);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1234);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1235);
				tableIdentifier();
				setState(1236);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1237);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(STORED);
				setState(1238);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(AS);
				setState(1239);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw6 = match(DIRECTORIES);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(1241);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1242);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1243);
				tableIdentifier();
				setState(1244);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1245);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				setState(1246);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(LOCATION);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(1248);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1249);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1250);
				tableIdentifier();
				setState(1251);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(EXCHANGE);
				setState(1252);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(1254);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1255);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1256);
				tableIdentifier();
				setState(1257);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ARCHIVE);
				setState(1258);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(1260);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1261);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1262);
				tableIdentifier();
				setState(1263);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(UNARCHIVE);
				setState(1264);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(1266);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1267);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1268);
				tableIdentifier();
				setState(1269);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TOUCH);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(1271);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1272);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1273);
				tableIdentifier();
				setState(1275);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1274);
					partitionSpec();
					}
				}

				setState(1277);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(COMPACT);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(1279);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1280);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1281);
				tableIdentifier();
				setState(1283);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1282);
					partitionSpec();
					}
				}

				setState(1285);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CONCATENATE);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1287);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1288);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1289);
				tableIdentifier();
				setState(1291);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1290);
					partitionSpec();
					}
				}

				setState(1293);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1294);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(FILEFORMAT);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1296);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1297);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1298);
				tableIdentifier();
				setState(1300);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1299);
					partitionSpec();
					}
				}

				setState(1302);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(REPLACE);
				setState(1303);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(COLUMNS);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1305);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(START);
				setState(1306);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTION);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1307);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(COMMIT);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1308);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ROLLBACK);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1309);
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
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode EXTERNAL() { return getToken(ArcticSqlCommandParser.EXTERNAL, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public CreateTableHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateTableHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateTableHeader(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateTableHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableHeaderContext createTableHeader() throws RecognitionException {
		CreateTableHeaderContext _localctx = new CreateTableHeaderContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_createTableHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1312);
			match(CREATE);
			setState(1314);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(1313);
				match(TEMPORARY);
				}
			}

			setState(1317);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTERNAL) {
				{
				setState(1316);
				match(EXTERNAL);
				}
			}

			setState(1319);
			match(TABLE);
			setState(1323);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				{
				setState(1320);
				match(IF);
				setState(1321);
				match(NOT);
				setState(1322);
				match(EXISTS);
				}
				break;
			}
			setState(1325);
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
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public ReplaceTableHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceTableHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterReplaceTableHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitReplaceTableHeader(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitReplaceTableHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReplaceTableHeaderContext replaceTableHeader() throws RecognitionException {
		ReplaceTableHeaderContext _localctx = new ReplaceTableHeaderContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_replaceTableHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1329);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CREATE) {
				{
				setState(1327);
				match(CREATE);
				setState(1328);
				match(OR);
				}
			}

			setState(1331);
			match(REPLACE);
			setState(1332);
			match(TABLE);
			setState(1333);
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
		public TerminalNode CLUSTERED() { return getToken(ArcticSqlCommandParser.CLUSTERED, 0); }
		public List<TerminalNode> BY() { return getTokens(ArcticSqlCommandParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticSqlCommandParser.BY, i);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode INTO() { return getToken(ArcticSqlCommandParser.INTO, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode BUCKETS() { return getToken(ArcticSqlCommandParser.BUCKETS, 0); }
		public TerminalNode SORTED() { return getToken(ArcticSqlCommandParser.SORTED, 0); }
		public OrderedIdentifierListContext orderedIdentifierList() {
			return getRuleContext(OrderedIdentifierListContext.class,0);
		}
		public BucketSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bucketSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterBucketSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitBucketSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitBucketSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BucketSpecContext bucketSpec() throws RecognitionException {
		BucketSpecContext _localctx = new BucketSpecContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_bucketSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1335);
			match(CLUSTERED);
			setState(1336);
			match(BY);
			setState(1337);
			identifierList();
			setState(1341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SORTED) {
				{
				setState(1338);
				match(SORTED);
				setState(1339);
				match(BY);
				setState(1340);
				orderedIdentifierList();
				}
			}

			setState(1343);
			match(INTO);
			setState(1344);
			match(INTEGER_VALUE);
			setState(1345);
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
		public TerminalNode SKEWED() { return getToken(ArcticSqlCommandParser.SKEWED, 0); }
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public ConstantListContext constantList() {
			return getRuleContext(ConstantListContext.class,0);
		}
		public NestedConstantListContext nestedConstantList() {
			return getRuleContext(NestedConstantListContext.class,0);
		}
		public TerminalNode STORED() { return getToken(ArcticSqlCommandParser.STORED, 0); }
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticSqlCommandParser.DIRECTORIES, 0); }
		public SkewSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skewSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSkewSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSkewSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSkewSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SkewSpecContext skewSpec() throws RecognitionException {
		SkewSpecContext _localctx = new SkewSpecContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_skewSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1347);
			match(SKEWED);
			setState(1348);
			match(BY);
			setState(1349);
			identifierList();
			setState(1350);
			match(ON);
			setState(1353);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
			case 1:
				{
				setState(1351);
				constantList();
				}
				break;
			case 2:
				{
				setState(1352);
				nestedConstantList();
				}
				break;
			}
			setState(1358);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				{
				setState(1355);
				match(STORED);
				setState(1356);
				match(AS);
				setState(1357);
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
		public TerminalNode LOCATION() { return getToken(ArcticSqlCommandParser.LOCATION, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public LocationSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_locationSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLocationSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLocationSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLocationSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocationSpecContext locationSpec() throws RecognitionException {
		LocationSpecContext _localctx = new LocationSpecContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_locationSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1360);
			match(LOCATION);
			setState(1361);
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
		public TerminalNode COMMENT() { return getToken(ArcticSqlCommandParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public CommentSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCommentSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCommentSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCommentSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentSpecContext commentSpec() throws RecognitionException {
		CommentSpecContext _localctx = new CommentSpecContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_commentSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1363);
			match(COMMENT);
			setState(1364);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1367);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(1366);
				ctes();
				}
			}

			setState(1369);
			queryTerm(0);
			setState(1370);
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
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticSqlCommandParser.DIRECTORY, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public RowFormatContext rowFormat() {
			return getRuleContext(RowFormatContext.class,0);
		}
		public CreateFileFormatContext createFileFormat() {
			return getRuleContext(CreateFileFormatContext.class,0);
		}
		public InsertOverwriteHiveDirContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInsertOverwriteHiveDir(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInsertOverwriteHiveDir(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInsertOverwriteHiveDir(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertOverwriteDirContext extends InsertIntoContext {
		public Token path;
		public PropertyListContext options;
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticSqlCommandParser.DIRECTORY, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public InsertOverwriteDirContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInsertOverwriteDir(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInsertOverwriteDir(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInsertOverwriteDir(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertOverwriteTableContext extends InsertIntoContext {
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public InsertOverwriteTableContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInsertOverwriteTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInsertOverwriteTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInsertOverwriteTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertIntoTableContext extends InsertIntoContext {
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(ArcticSqlCommandParser.INTO, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public InsertIntoTableContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInsertIntoTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInsertIntoTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInsertIntoTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertIntoContext insertInto() throws RecognitionException {
		InsertIntoContext _localctx = new InsertIntoContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_insertInto);
		int _la;
		try {
			setState(1433);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				_localctx = new InsertOverwriteTableContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1372);
				match(INSERT);
				setState(1373);
				match(OVERWRITE);
				setState(1375);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
				case 1:
					{
					setState(1374);
					match(TABLE);
					}
					break;
				}
				setState(1377);
				multipartIdentifier();
				setState(1384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1378);
					partitionSpec();
					setState(1382);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IF) {
						{
						setState(1379);
						match(IF);
						setState(1380);
						match(NOT);
						setState(1381);
						match(EXISTS);
						}
					}

					}
				}

				setState(1387);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
				case 1:
					{
					setState(1386);
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
				setState(1389);
				match(INSERT);
				setState(1390);
				match(INTO);
				setState(1392);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
				case 1:
					{
					setState(1391);
					match(TABLE);
					}
					break;
				}
				setState(1394);
				multipartIdentifier();
				setState(1396);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1395);
					partitionSpec();
					}
				}

				setState(1401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(1398);
					match(IF);
					setState(1399);
					match(NOT);
					setState(1400);
					match(EXISTS);
					}
				}

				setState(1404);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
				case 1:
					{
					setState(1403);
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
				setState(1406);
				match(INSERT);
				setState(1407);
				match(OVERWRITE);
				setState(1409);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1408);
					match(LOCAL);
					}
				}

				setState(1411);
				match(DIRECTORY);
				setState(1412);
				((InsertOverwriteHiveDirContext)_localctx).path = match(STRING);
				setState(1414);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(1413);
					rowFormat();
					}
				}

				setState(1417);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORED) {
					{
					setState(1416);
					createFileFormat();
					}
				}

				}
				break;
			case 4:
				_localctx = new InsertOverwriteDirContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1419);
				match(INSERT);
				setState(1420);
				match(OVERWRITE);
				setState(1422);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1421);
					match(LOCAL);
					}
				}

				setState(1424);
				match(DIRECTORY);
				setState(1426);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING) {
					{
					setState(1425);
					((InsertOverwriteDirContext)_localctx).path = match(STRING);
					}
				}

				setState(1428);
				tableProvider();
				setState(1431);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1429);
					match(OPTIONS);
					setState(1430);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPartitionSpecLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPartitionSpecLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPartitionSpecLocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSpecLocationContext partitionSpecLocation() throws RecognitionException {
		PartitionSpecLocationContext _localctx = new PartitionSpecLocationContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_partitionSpecLocation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1435);
			partitionSpec();
			setState(1437);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOCATION) {
				{
				setState(1436);
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
		public TerminalNode PARTITION() { return getToken(ArcticSqlCommandParser.PARTITION, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<PartitionValContext> partitionVal() {
			return getRuleContexts(PartitionValContext.class);
		}
		public PartitionValContext partitionVal(int i) {
			return getRuleContext(PartitionValContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public PartitionSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPartitionSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPartitionSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPartitionSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSpecContext partitionSpec() throws RecognitionException {
		PartitionSpecContext _localctx = new PartitionSpecContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_partitionSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1439);
			match(PARTITION);
			setState(1440);
			match(LEFT_PAREN);
			setState(1441);
			partitionVal();
			setState(1446);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1442);
				match(COMMA);
				setState(1443);
				partitionVal();
				}
				}
				setState(1448);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1449);
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
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public PartitionValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPartitionVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPartitionVal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPartitionVal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionValContext partitionVal() throws RecognitionException {
		PartitionValContext _localctx = new PartitionValContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_partitionVal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1451);
			identifier();
			setState(1454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1452);
				match(EQ);
				setState(1453);
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
		public TerminalNode NAMESPACE() { return getToken(ArcticSqlCommandParser.NAMESPACE, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticSqlCommandParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(ArcticSqlCommandParser.SCHEMA, 0); }
		public NamespaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNamespace(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespaceContext namespace() throws RecognitionException {
		NamespaceContext _localctx = new NamespaceContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_namespace);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1456);
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
		public TerminalNode NAMESPACES() { return getToken(ArcticSqlCommandParser.NAMESPACES, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticSqlCommandParser.DATABASES, 0); }
		public TerminalNode SCHEMAS() { return getToken(ArcticSqlCommandParser.SCHEMAS, 0); }
		public NamespacesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespaces; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNamespaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNamespaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNamespaces(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespacesContext namespaces() throws RecognitionException {
		NamespacesContext _localctx = new NamespacesContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_namespaces);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1458);
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
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDescribeFuncName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDescribeFuncName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDescribeFuncName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeFuncNameContext describeFuncName() throws RecognitionException {
		DescribeFuncNameContext _localctx = new DescribeFuncNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_describeFuncName);
		try {
			setState(1465);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1460);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1461);
				match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1462);
				comparisonOperator();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1463);
				arithmeticOperator();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1464);
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
		public List<TerminalNode> DOT() { return getTokens(ArcticSqlCommandParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticSqlCommandParser.DOT, i);
		}
		public DescribeColNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describeColName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDescribeColName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDescribeColName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDescribeColName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeColNameContext describeColName() throws RecognitionException {
		DescribeColNameContext _localctx = new DescribeColNameContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_describeColName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1467);
			((DescribeColNameContext)_localctx).identifier = identifier();
			((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
			setState(1472);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(1468);
				match(DOT);
				setState(1469);
				((DescribeColNameContext)_localctx).identifier = identifier();
				((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
				}
				}
				setState(1474);
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
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public List<NamedQueryContext> namedQuery() {
			return getRuleContexts(NamedQueryContext.class);
		}
		public NamedQueryContext namedQuery(int i) {
			return getRuleContext(NamedQueryContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public CtesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ctes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCtes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCtes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCtes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CtesContext ctes() throws RecognitionException {
		CtesContext _localctx = new CtesContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_ctes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1475);
			match(WITH);
			setState(1476);
			namedQuery();
			setState(1481);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1477);
				match(COMMA);
				setState(1478);
				namedQuery();
				}
				}
				setState(1483);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public NamedQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNamedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNamedQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNamedQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedQueryContext namedQuery() throws RecognitionException {
		NamedQueryContext _localctx = new NamedQueryContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_namedQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1484);
			((NamedQueryContext)_localctx).name = errorCapturingIdentifier();
			setState(1486);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
			case 1:
				{
				setState(1485);
				((NamedQueryContext)_localctx).columnAliases = identifierList();
				}
				break;
			}
			setState(1489);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(1488);
				match(AS);
				}
			}

			setState(1491);
			match(LEFT_PAREN);
			setState(1492);
			query();
			setState(1493);
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
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableProviderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableProvider; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableProvider(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableProvider(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableProvider(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableProviderContext tableProvider() throws RecognitionException {
		TableProviderContext _localctx = new TableProviderContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_tableProvider);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1495);
			match(USING);
			setState(1496);
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
		public List<TerminalNode> OPTIONS() { return getTokens(ArcticSqlCommandParser.OPTIONS); }
		public TerminalNode OPTIONS(int i) {
			return getToken(ArcticSqlCommandParser.OPTIONS, i);
		}
		public List<TerminalNode> PARTITIONED() { return getTokens(ArcticSqlCommandParser.PARTITIONED); }
		public TerminalNode PARTITIONED(int i) {
			return getToken(ArcticSqlCommandParser.PARTITIONED, i);
		}
		public List<TerminalNode> BY() { return getTokens(ArcticSqlCommandParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticSqlCommandParser.BY, i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(ArcticSqlCommandParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(ArcticSqlCommandParser.TBLPROPERTIES, i);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateTableClauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateTableClauses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateTableClauses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableClausesContext createTableClauses() throws RecognitionException {
		CreateTableClausesContext _localctx = new CreateTableClausesContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_createTableClauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1513);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CLUSTERED || _la==COMMENT || ((((_la - 144)) & ~0x3f) == 0 && ((1L << (_la - 144)) & ((1L << (LOCATION - 144)) | (1L << (OPTIONS - 144)) | (1L << (PARTITIONED - 144)))) != 0) || ((((_la - 215)) & ~0x3f) == 0 && ((1L << (_la - 215)) & ((1L << (ROW - 215)) | (1L << (SKEWED - 215)) | (1L << (STORED - 215)) | (1L << (TBLPROPERTIES - 215)))) != 0)) {
				{
				setState(1511);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case OPTIONS:
					{
					{
					setState(1498);
					match(OPTIONS);
					setState(1499);
					((CreateTableClausesContext)_localctx).options = propertyList();
					}
					}
					break;
				case PARTITIONED:
					{
					{
					setState(1500);
					match(PARTITIONED);
					setState(1501);
					match(BY);
					setState(1502);
					((CreateTableClausesContext)_localctx).partitioning = partitionFieldList();
					}
					}
					break;
				case SKEWED:
					{
					setState(1503);
					skewSpec();
					}
					break;
				case CLUSTERED:
					{
					setState(1504);
					bucketSpec();
					}
					break;
				case ROW:
					{
					setState(1505);
					rowFormat();
					}
					break;
				case STORED:
					{
					setState(1506);
					createFileFormat();
					}
					break;
				case LOCATION:
					{
					setState(1507);
					locationSpec();
					}
					break;
				case COMMENT:
					{
					setState(1508);
					commentSpec();
					}
					break;
				case TBLPROPERTIES:
					{
					{
					setState(1509);
					match(TBLPROPERTIES);
					setState(1510);
					((CreateTableClausesContext)_localctx).tableProps = propertyList();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(1515);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<PropertyContext> property() {
			return getRuleContexts(PropertyContext.class);
		}
		public PropertyContext property(int i) {
			return getRuleContext(PropertyContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public PropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyListContext propertyList() throws RecognitionException {
		PropertyListContext _localctx = new PropertyListContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_propertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1516);
			match(LEFT_PAREN);
			setState(1517);
			property();
			setState(1522);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1518);
				match(COMMA);
				setState(1519);
				property();
				}
				}
				setState(1524);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1525);
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
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_property);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1527);
			((PropertyContext)_localctx).key = propertyKey();
			setState(1532);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FALSE || ((((_la - 262)) & ~0x3f) == 0 && ((1L << (_la - 262)) & ((1L << (TRUE - 262)) | (1L << (EQ - 262)) | (1L << (STRING - 262)) | (1L << (INTEGER_VALUE - 262)) | (1L << (DECIMAL_VALUE - 262)))) != 0)) {
				{
				setState(1529);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1528);
					match(EQ);
					}
				}

				setState(1531);
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
		public List<TerminalNode> DOT() { return getTokens(ArcticSqlCommandParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticSqlCommandParser.DOT, i);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public PropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPropertyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPropertyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyKeyContext propertyKey() throws RecognitionException {
		PropertyKeyContext _localctx = new PropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_propertyKey);
		int _la;
		try {
			setState(1543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1534);
				identifier();
				setState(1539);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(1535);
					match(DOT);
					setState(1536);
					identifier();
					}
					}
					setState(1541);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1542);
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
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticSqlCommandParser.DECIMAL_VALUE, 0); }
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_propertyValue);
		try {
			setState(1549);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_VALUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1545);
				match(INTEGER_VALUE);
				}
				break;
			case DECIMAL_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1546);
				match(DECIMAL_VALUE);
				}
				break;
			case FALSE:
			case TRUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1547);
				booleanValue();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(1548);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public ConstantListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterConstantList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitConstantList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitConstantList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantListContext constantList() throws RecognitionException {
		ConstantListContext _localctx = new ConstantListContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_constantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1551);
			match(LEFT_PAREN);
			setState(1552);
			constant();
			setState(1557);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1553);
				match(COMMA);
				setState(1554);
				constant();
				}
				}
				setState(1559);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1560);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<ConstantListContext> constantList() {
			return getRuleContexts(ConstantListContext.class);
		}
		public ConstantListContext constantList(int i) {
			return getRuleContext(ConstantListContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public NestedConstantListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedConstantList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNestedConstantList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNestedConstantList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNestedConstantList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedConstantListContext nestedConstantList() throws RecognitionException {
		NestedConstantListContext _localctx = new NestedConstantListContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_nestedConstantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1562);
			match(LEFT_PAREN);
			setState(1563);
			constantList();
			setState(1568);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1564);
				match(COMMA);
				setState(1565);
				constantList();
				}
				}
				setState(1570);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1571);
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
		public TerminalNode STORED() { return getToken(ArcticSqlCommandParser.STORED, 0); }
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public FileFormatContext fileFormat() {
			return getRuleContext(FileFormatContext.class,0);
		}
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public StorageHandlerContext storageHandler() {
			return getRuleContext(StorageHandlerContext.class,0);
		}
		public CreateFileFormatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createFileFormat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCreateFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCreateFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCreateFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateFileFormatContext createFileFormat() throws RecognitionException {
		CreateFileFormatContext _localctx = new CreateFileFormatContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_createFileFormat);
		try {
			setState(1579);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,170,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1573);
				match(STORED);
				setState(1574);
				match(AS);
				setState(1575);
				fileFormat();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1576);
				match(STORED);
				setState(1577);
				match(BY);
				setState(1578);
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
		public TerminalNode INPUTFORMAT() { return getToken(ArcticSqlCommandParser.INPUTFORMAT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(ArcticSqlCommandParser.OUTPUTFORMAT, 0); }
		public List<TerminalNode> STRING() { return getTokens(ArcticSqlCommandParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticSqlCommandParser.STRING, i);
		}
		public TableFileFormatContext(FileFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableFileFormat(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterGenericFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitGenericFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitGenericFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileFormatContext fileFormat() throws RecognitionException {
		FileFormatContext _localctx = new FileFormatContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_fileFormat);
		try {
			setState(1586);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
			case 1:
				_localctx = new TableFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1581);
				match(INPUTFORMAT);
				setState(1582);
				((TableFileFormatContext)_localctx).inFmt = match(STRING);
				setState(1583);
				match(OUTPUTFORMAT);
				setState(1584);
				((TableFileFormatContext)_localctx).outFmt = match(STRING);
				}
				break;
			case 2:
				_localctx = new GenericFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1585);
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
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public StorageHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_storageHandler; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterStorageHandler(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitStorageHandler(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitStorageHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StorageHandlerContext storageHandler() throws RecognitionException {
		StorageHandlerContext _localctx = new StorageHandlerContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_storageHandler);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1588);
			match(STRING);
			setState(1592);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
			case 1:
				{
				setState(1589);
				match(WITH);
				setState(1590);
				match(SERDEPROPERTIES);
				setState(1591);
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
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_resource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1594);
			identifier();
			setState(1595);
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
		public TerminalNode DELETE() { return getToken(ArcticSqlCommandParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDeleteFromTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDeleteFromTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDeleteFromTable(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSingleInsertQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSingleInsertQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSingleInsertQuery(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultiInsertQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultiInsertQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultiInsertQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UpdateTableContext extends DmlStatementNoWithContext {
		public TerminalNode UPDATE() { return getToken(ArcticSqlCommandParser.UPDATE, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUpdateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUpdateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUpdateTable(this);
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
		public TerminalNode MERGE() { return getToken(ArcticSqlCommandParser.MERGE, 0); }
		public TerminalNode INTO() { return getToken(ArcticSqlCommandParser.INTO, 0); }
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMergeIntoTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMergeIntoTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMergeIntoTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DmlStatementNoWithContext dmlStatementNoWith() throws RecognitionException {
		DmlStatementNoWithContext _localctx = new DmlStatementNoWithContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_dmlStatementNoWith);
		int _la;
		try {
			int _alt;
			setState(1647);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INSERT:
				_localctx = new SingleInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1597);
				insertInto();
				setState(1598);
				query();
				}
				break;
			case FROM:
				_localctx = new MultiInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1600);
				fromClause();
				setState(1602); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1601);
					multiInsertQueryBody();
					}
					}
					setState(1604); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==INSERT );
				}
				break;
			case DELETE:
				_localctx = new DeleteFromTableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1606);
				match(DELETE);
				setState(1607);
				match(FROM);
				setState(1608);
				multipartIdentifier();
				setState(1609);
				tableAlias();
				setState(1611);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1610);
					whereClause();
					}
				}

				}
				break;
			case UPDATE:
				_localctx = new UpdateTableContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1613);
				match(UPDATE);
				setState(1614);
				multipartIdentifier();
				setState(1615);
				tableAlias();
				setState(1616);
				setClause();
				setState(1618);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1617);
					whereClause();
					}
				}

				}
				break;
			case MERGE:
				_localctx = new MergeIntoTableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1620);
				match(MERGE);
				setState(1621);
				match(INTO);
				setState(1622);
				((MergeIntoTableContext)_localctx).target = multipartIdentifier();
				setState(1623);
				((MergeIntoTableContext)_localctx).targetAlias = tableAlias();
				setState(1624);
				match(USING);
				setState(1630);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
				case 1:
					{
					setState(1625);
					((MergeIntoTableContext)_localctx).source = multipartIdentifier();
					}
					break;
				case 2:
					{
					setState(1626);
					match(LEFT_PAREN);
					setState(1627);
					((MergeIntoTableContext)_localctx).sourceQuery = query();
					setState(1628);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(1632);
				((MergeIntoTableContext)_localctx).sourceAlias = tableAlias();
				setState(1633);
				match(ON);
				setState(1634);
				((MergeIntoTableContext)_localctx).mergeCondition = booleanExpression(0);
				setState(1638);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,177,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1635);
						matchedClause();
						}
						} 
					}
					setState(1640);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,177,_ctx);
				}
				setState(1644);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(1641);
					notMatchedClause();
					}
					}
					setState(1646);
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
		public TerminalNode ORDER() { return getToken(ArcticSqlCommandParser.ORDER, 0); }
		public List<TerminalNode> BY() { return getTokens(ArcticSqlCommandParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticSqlCommandParser.BY, i);
		}
		public TerminalNode CLUSTER() { return getToken(ArcticSqlCommandParser.CLUSTER, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticSqlCommandParser.DISTRIBUTE, 0); }
		public TerminalNode SORT() { return getToken(ArcticSqlCommandParser.SORT, 0); }
		public WindowClauseContext windowClause() {
			return getRuleContext(WindowClauseContext.class,0);
		}
		public TerminalNode LIMIT() { return getToken(ArcticSqlCommandParser.LIMIT, 0); }
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
		public TerminalNode ALL() { return getToken(ArcticSqlCommandParser.ALL, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public QueryOrganizationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryOrganization; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQueryOrganization(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQueryOrganization(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQueryOrganization(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryOrganizationContext queryOrganization() throws RecognitionException {
		QueryOrganizationContext _localctx = new QueryOrganizationContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_queryOrganization);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1659);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				{
				setState(1649);
				match(ORDER);
				setState(1650);
				match(BY);
				setState(1651);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1656);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1652);
						match(COMMA);
						setState(1653);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1658);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
				}
				}
				break;
			}
			setState(1671);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				{
				setState(1661);
				match(CLUSTER);
				setState(1662);
				match(BY);
				setState(1663);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1668);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,182,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1664);
						match(COMMA);
						setState(1665);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1670);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,182,_ctx);
				}
				}
				break;
			}
			setState(1683);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,185,_ctx) ) {
			case 1:
				{
				setState(1673);
				match(DISTRIBUTE);
				setState(1674);
				match(BY);
				setState(1675);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1680);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1676);
						match(COMMA);
						setState(1677);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1682);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
				}
				}
				break;
			}
			setState(1695);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
			case 1:
				{
				setState(1685);
				match(SORT);
				setState(1686);
				match(BY);
				setState(1687);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1692);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1688);
						match(COMMA);
						setState(1689);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1694);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				}
				}
				break;
			}
			setState(1698);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				{
				setState(1697);
				windowClause();
				}
				break;
			}
			setState(1705);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
			case 1:
				{
				setState(1700);
				match(LIMIT);
				setState(1703);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
				case 1:
					{
					setState(1701);
					match(ALL);
					}
					break;
				case 2:
					{
					setState(1702);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultiInsertQueryBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultiInsertQueryBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultiInsertQueryBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiInsertQueryBodyContext multiInsertQueryBody() throws RecognitionException {
		MultiInsertQueryBodyContext _localctx = new MultiInsertQueryBodyContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_multiInsertQueryBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1707);
			insertInto();
			setState(1708);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQueryTermDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQueryTermDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQueryTermDefault(this);
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
		public TerminalNode INTERSECT() { return getToken(ArcticSqlCommandParser.INTERSECT, 0); }
		public TerminalNode UNION() { return getToken(ArcticSqlCommandParser.UNION, 0); }
		public TerminalNode EXCEPT() { return getToken(ArcticSqlCommandParser.EXCEPT, 0); }
		public TerminalNode SETMINUS() { return getToken(ArcticSqlCommandParser.SETMINUS, 0); }
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public SetOperationContext(QueryTermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetOperation(this);
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
		int _startState = 90;
		enterRecursionRule(_localctx, 90, RULE_queryTerm, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new QueryTermDefaultContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(1711);
			queryPrimary();
			}
			_ctx.stop = _input.LT(-1);
			setState(1736);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,195,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1734);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
					case 1:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1713);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1714);
						if (!(legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "legacy_setops_precedence_enabled");
						setState(1715);
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
						setState(1717);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1716);
							setQuantifier();
							}
						}

						setState(1719);
						((SetOperationContext)_localctx).right = queryTerm(4);
						}
						break;
					case 2:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1720);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1721);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1722);
						((SetOperationContext)_localctx).operator = match(INTERSECT);
						setState(1724);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1723);
							setQuantifier();
							}
						}

						setState(1726);
						((SetOperationContext)_localctx).right = queryTerm(3);
						}
						break;
					case 3:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1727);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(1728);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1729);
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
						setState(1731);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1730);
							setQuantifier();
							}
						}

						setState(1733);
						((SetOperationContext)_localctx).right = queryTerm(2);
						}
						break;
					}
					} 
				}
				setState(1738);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,195,_ctx);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public SubqueryContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSubquery(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQueryPrimaryDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQueryPrimaryDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQueryPrimaryDefault(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInlineTableDefault1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInlineTableDefault1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInlineTableDefault1(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFromStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFromStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFromStmt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TableContext extends QueryPrimaryContext {
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryPrimaryContext queryPrimary() throws RecognitionException {
		QueryPrimaryContext _localctx = new QueryPrimaryContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_queryPrimary);
		try {
			setState(1748);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAP:
			case REDUCE:
			case SELECT:
				_localctx = new QueryPrimaryDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1739);
				querySpecification();
				}
				break;
			case FROM:
				_localctx = new FromStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1740);
				fromStatement();
				}
				break;
			case TABLE:
				_localctx = new TableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1741);
				match(TABLE);
				setState(1742);
				multipartIdentifier();
				}
				break;
			case VALUES:
				_localctx = new InlineTableDefault1Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1743);
				inlineTable();
				}
				break;
			case LEFT_PAREN:
				_localctx = new SubqueryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1744);
				match(LEFT_PAREN);
				setState(1745);
				query();
				setState(1746);
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
		public TerminalNode NULLS() { return getToken(ArcticSqlCommandParser.NULLS, 0); }
		public TerminalNode ASC() { return getToken(ArcticSqlCommandParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode LAST() { return getToken(ArcticSqlCommandParser.LAST, 0); }
		public TerminalNode FIRST() { return getToken(ArcticSqlCommandParser.FIRST, 0); }
		public SortItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSortItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSortItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSortItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortItemContext sortItem() throws RecognitionException {
		SortItemContext _localctx = new SortItemContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_sortItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1750);
			expression();
			setState(1752);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
			case 1:
				{
				setState(1751);
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
			setState(1756);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,198,_ctx) ) {
			case 1:
				{
				setState(1754);
				match(NULLS);
				setState(1755);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFromStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFromStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFromStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromStatementContext fromStatement() throws RecognitionException {
		FromStatementContext _localctx = new FromStatementContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_fromStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1758);
			fromClause();
			setState(1760); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1759);
					fromStatementBody();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1762); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,199,_ctx);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFromStatementBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFromStatementBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFromStatementBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromStatementBodyContext fromStatementBody() throws RecognitionException {
		FromStatementBodyContext _localctx = new FromStatementBodyContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_fromStatementBody);
		try {
			int _alt;
			setState(1791);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1764);
				transformClause();
				setState(1766);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
				case 1:
					{
					setState(1765);
					whereClause();
					}
					break;
				}
				setState(1768);
				queryOrganization();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1770);
				selectClause();
				setState(1774);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,201,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1771);
						lateralView();
						}
						} 
					}
					setState(1776);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,201,_ctx);
				}
				setState(1778);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
				case 1:
					{
					setState(1777);
					whereClause();
					}
					break;
				}
				setState(1781);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
				case 1:
					{
					setState(1780);
					aggregationClause();
					}
					break;
				}
				setState(1784);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
				case 1:
					{
					setState(1783);
					havingClause();
					}
					break;
				}
				setState(1787);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
				case 1:
					{
					setState(1786);
					windowClause();
					}
					break;
				}
				setState(1789);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRegularQuerySpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRegularQuerySpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRegularQuerySpecification(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTransformQuerySpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTransformQuerySpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTransformQuerySpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuerySpecificationContext querySpecification() throws RecognitionException {
		QuerySpecificationContext _localctx = new QuerySpecificationContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_querySpecification);
		try {
			int _alt;
			setState(1837);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,219,_ctx) ) {
			case 1:
				_localctx = new TransformQuerySpecificationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1793);
				transformClause();
				setState(1795);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(1794);
					fromClause();
					}
					break;
				}
				setState(1800);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,208,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1797);
						lateralView();
						}
						} 
					}
					setState(1802);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,208,_ctx);
				}
				setState(1804);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
				case 1:
					{
					setState(1803);
					whereClause();
					}
					break;
				}
				setState(1807);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
				case 1:
					{
					setState(1806);
					aggregationClause();
					}
					break;
				}
				setState(1810);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
				case 1:
					{
					setState(1809);
					havingClause();
					}
					break;
				}
				setState(1813);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
				case 1:
					{
					setState(1812);
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
				setState(1815);
				selectClause();
				setState(1817);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
				case 1:
					{
					setState(1816);
					fromClause();
					}
					break;
				}
				setState(1822);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1819);
						lateralView();
						}
						} 
					}
					setState(1824);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				}
				setState(1826);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,215,_ctx) ) {
				case 1:
					{
					setState(1825);
					whereClause();
					}
					break;
				}
				setState(1829);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,216,_ctx) ) {
				case 1:
					{
					setState(1828);
					aggregationClause();
					}
					break;
				}
				setState(1832);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,217,_ctx) ) {
				case 1:
					{
					setState(1831);
					havingClause();
					}
					break;
				}
				setState(1835);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
				case 1:
					{
					setState(1834);
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
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public List<TerminalNode> STRING() { return getTokens(ArcticSqlCommandParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticSqlCommandParser.STRING, i);
		}
		public TerminalNode SELECT() { return getToken(ArcticSqlCommandParser.SELECT, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticSqlCommandParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.LEFT_PAREN, i);
		}
		public ExpressionSeqContext expressionSeq() {
			return getRuleContext(ExpressionSeqContext.class,0);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticSqlCommandParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.RIGHT_PAREN, i);
		}
		public TerminalNode TRANSFORM() { return getToken(ArcticSqlCommandParser.TRANSFORM, 0); }
		public TerminalNode MAP() { return getToken(ArcticSqlCommandParser.MAP, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticSqlCommandParser.REDUCE, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticSqlCommandParser.RECORDWRITER, 0); }
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticSqlCommandParser.RECORDREADER, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTransformClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTransformClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTransformClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformClauseContext transformClause() throws RecognitionException {
		TransformClauseContext _localctx = new TransformClauseContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_transformClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1858);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(1839);
				match(SELECT);
				setState(1840);
				((TransformClauseContext)_localctx).kind = match(TRANSFORM);
				setState(1841);
				match(LEFT_PAREN);
				setState(1843);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
				case 1:
					{
					setState(1842);
					setQuantifier();
					}
					break;
				}
				setState(1845);
				expressionSeq();
				setState(1846);
				match(RIGHT_PAREN);
				}
				break;
			case MAP:
				{
				setState(1848);
				((TransformClauseContext)_localctx).kind = match(MAP);
				setState(1850);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,221,_ctx) ) {
				case 1:
					{
					setState(1849);
					setQuantifier();
					}
					break;
				}
				setState(1852);
				expressionSeq();
				}
				break;
			case REDUCE:
				{
				setState(1853);
				((TransformClauseContext)_localctx).kind = match(REDUCE);
				setState(1855);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
				case 1:
					{
					setState(1854);
					setQuantifier();
					}
					break;
				}
				setState(1857);
				expressionSeq();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1861);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROW) {
				{
				setState(1860);
				((TransformClauseContext)_localctx).inRowFormat = rowFormat();
				}
			}

			setState(1865);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECORDWRITER) {
				{
				setState(1863);
				match(RECORDWRITER);
				setState(1864);
				((TransformClauseContext)_localctx).recordWriter = match(STRING);
				}
			}

			setState(1867);
			match(USING);
			setState(1868);
			((TransformClauseContext)_localctx).script = match(STRING);
			setState(1881);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				{
				setState(1869);
				match(AS);
				setState(1879);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,227,_ctx) ) {
				case 1:
					{
					setState(1870);
					identifierSeq();
					}
					break;
				case 2:
					{
					setState(1871);
					colTypeList();
					}
					break;
				case 3:
					{
					{
					setState(1872);
					match(LEFT_PAREN);
					setState(1875);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,226,_ctx) ) {
					case 1:
						{
						setState(1873);
						identifierSeq();
						}
						break;
					case 2:
						{
						setState(1874);
						colTypeList();
						}
						break;
					}
					setState(1877);
					match(RIGHT_PAREN);
					}
					}
					break;
				}
				}
				break;
			}
			setState(1884);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
			case 1:
				{
				setState(1883);
				((TransformClauseContext)_localctx).outRowFormat = rowFormat();
				}
				break;
			}
			setState(1888);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				{
				setState(1886);
				match(RECORDREADER);
				setState(1887);
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
		public TerminalNode SELECT() { return getToken(ArcticSqlCommandParser.SELECT, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_selectClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1890);
			match(SELECT);
			setState(1894);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,231,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1891);
					((SelectClauseContext)_localctx).hint = hint();
					((SelectClauseContext)_localctx).hints.add(((SelectClauseContext)_localctx).hint);
					}
					} 
				}
				setState(1896);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,231,_ctx);
			}
			setState(1898);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(1897);
				setQuantifier();
				}
				break;
			}
			setState(1900);
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
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public SetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetClauseContext setClause() throws RecognitionException {
		SetClauseContext _localctx = new SetClauseContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_setClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1902);
			match(SET);
			setState(1903);
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
		public TerminalNode WHEN() { return getToken(ArcticSqlCommandParser.WHEN, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticSqlCommandParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(ArcticSqlCommandParser.THEN, 0); }
		public MatchedActionContext matchedAction() {
			return getRuleContext(MatchedActionContext.class,0);
		}
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public MatchedClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchedClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMatchedClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMatchedClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMatchedClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchedClauseContext matchedClause() throws RecognitionException {
		MatchedClauseContext _localctx = new MatchedClauseContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_matchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1905);
			match(WHEN);
			setState(1906);
			match(MATCHED);
			setState(1909);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1907);
				match(AND);
				setState(1908);
				((MatchedClauseContext)_localctx).matchedCond = booleanExpression(0);
				}
			}

			setState(1911);
			match(THEN);
			setState(1912);
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
		public TerminalNode WHEN() { return getToken(ArcticSqlCommandParser.WHEN, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticSqlCommandParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(ArcticSqlCommandParser.THEN, 0); }
		public NotMatchedActionContext notMatchedAction() {
			return getRuleContext(NotMatchedActionContext.class,0);
		}
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public NotMatchedClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notMatchedClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNotMatchedClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNotMatchedClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNotMatchedClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotMatchedClauseContext notMatchedClause() throws RecognitionException {
		NotMatchedClauseContext _localctx = new NotMatchedClauseContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_notMatchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1914);
			match(WHEN);
			setState(1915);
			match(NOT);
			setState(1916);
			match(MATCHED);
			setState(1919);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1917);
				match(AND);
				setState(1918);
				((NotMatchedClauseContext)_localctx).notMatchedCond = booleanExpression(0);
				}
			}

			setState(1921);
			match(THEN);
			setState(1922);
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
		public TerminalNode DELETE() { return getToken(ArcticSqlCommandParser.DELETE, 0); }
		public TerminalNode UPDATE() { return getToken(ArcticSqlCommandParser.UPDATE, 0); }
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public MatchedActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchedAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMatchedAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMatchedAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMatchedAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchedActionContext matchedAction() throws RecognitionException {
		MatchedActionContext _localctx = new MatchedActionContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_matchedAction);
		try {
			setState(1931);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1924);
				match(DELETE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1925);
				match(UPDATE);
				setState(1926);
				match(SET);
				setState(1927);
				match(ASTERISK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1928);
				match(UPDATE);
				setState(1929);
				match(SET);
				setState(1930);
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
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticSqlCommandParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticSqlCommandParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.RIGHT_PAREN, i);
		}
		public TerminalNode VALUES() { return getToken(ArcticSqlCommandParser.VALUES, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public NotMatchedActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notMatchedAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNotMatchedAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNotMatchedAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNotMatchedAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotMatchedActionContext notMatchedAction() throws RecognitionException {
		NotMatchedActionContext _localctx = new NotMatchedActionContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_notMatchedAction);
		int _la;
		try {
			setState(1951);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,237,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1933);
				match(INSERT);
				setState(1934);
				match(ASTERISK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1935);
				match(INSERT);
				setState(1936);
				match(LEFT_PAREN);
				setState(1937);
				((NotMatchedActionContext)_localctx).columns = multipartIdentifierList();
				setState(1938);
				match(RIGHT_PAREN);
				setState(1939);
				match(VALUES);
				setState(1940);
				match(LEFT_PAREN);
				setState(1941);
				expression();
				setState(1946);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1942);
					match(COMMA);
					setState(1943);
					expression();
					}
					}
					setState(1948);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1949);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public AssignmentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAssignmentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAssignmentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAssignmentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentListContext assignmentList() throws RecognitionException {
		AssignmentListContext _localctx = new AssignmentListContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1953);
			assignment();
			setState(1958);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1954);
				match(COMMA);
				setState(1955);
				assignment();
				}
				}
				setState(1960);
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
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1961);
			((AssignmentContext)_localctx).key = multipartIdentifier();
			setState(1962);
			match(EQ);
			setState(1963);
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
		public TerminalNode WHERE() { return getToken(ArcticSqlCommandParser.WHERE, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1965);
			match(WHERE);
			setState(1966);
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
		public TerminalNode HAVING() { return getToken(ArcticSqlCommandParser.HAVING, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitHavingClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitHavingClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1968);
			match(HAVING);
			setState(1969);
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
		public TerminalNode HENT_START() { return getToken(ArcticSqlCommandParser.HENT_START, 0); }
		public TerminalNode HENT_END() { return getToken(ArcticSqlCommandParser.HENT_END, 0); }
		public List<HintStatementContext> hintStatement() {
			return getRuleContexts(HintStatementContext.class);
		}
		public HintStatementContext hintStatement(int i) {
			return getRuleContext(HintStatementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public HintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitHint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HintContext hint() throws RecognitionException {
		HintContext _localctx = new HintContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_hint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1971);
			match(HENT_START);
			setState(1972);
			((HintContext)_localctx).hintStatement = hintStatement();
			((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
			setState(1979);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1974);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,239,_ctx) ) {
					case 1:
						{
						setState(1973);
						match(COMMA);
						}
						break;
					}
					setState(1976);
					((HintContext)_localctx).hintStatement = hintStatement();
					((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
					}
					} 
				}
				setState(1981);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
			}
			setState(1982);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public HintStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hintStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterHintStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitHintStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitHintStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HintStatementContext hintStatement() throws RecognitionException {
		HintStatementContext _localctx = new HintStatementContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_hintStatement);
		int _la;
		try {
			setState(1997);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,242,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1984);
				((HintStatementContext)_localctx).hintName = identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1985);
				((HintStatementContext)_localctx).hintName = identifier();
				setState(1986);
				match(LEFT_PAREN);
				setState(1987);
				((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
				((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
				setState(1992);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1988);
					match(COMMA);
					setState(1989);
					((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
					((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
					}
					}
					setState(1994);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1995);
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
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public List<RelationContext> relation() {
			return getRuleContexts(RelationContext.class);
		}
		public RelationContext relation(int i) {
			return getRuleContext(RelationContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_fromClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1999);
			match(FROM);
			setState(2000);
			relation();
			setState(2005);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,243,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2001);
					match(COMMA);
					setState(2002);
					relation();
					}
					} 
				}
				setState(2007);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,243,_ctx);
			}
			setState(2011);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,244,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2008);
					lateralView();
					}
					} 
				}
				setState(2013);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,244,_ctx);
			}
			setState(2015);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,245,_ctx) ) {
			case 1:
				{
				setState(2014);
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
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode OF() { return getToken(ArcticSqlCommandParser.OF, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(ArcticSqlCommandParser.SYSTEM_VERSION, 0); }
		public TerminalNode VERSION() { return getToken(ArcticSqlCommandParser.VERSION, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(ArcticSqlCommandParser.SYSTEM_TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(ArcticSqlCommandParser.TIMESTAMP, 0); }
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TemporalClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_temporalClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTemporalClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTemporalClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTemporalClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TemporalClauseContext temporalClause() throws RecognitionException {
		TemporalClauseContext _localctx = new TemporalClauseContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_temporalClause);
		int _la;
		try {
			setState(2031);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,248,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2018);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2017);
					match(FOR);
					}
				}

				setState(2020);
				_la = _input.LA(1);
				if ( !(_la==SYSTEM_VERSION || _la==VERSION) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2021);
				match(AS);
				setState(2022);
				match(OF);
				setState(2023);
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
				setState(2025);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2024);
					match(FOR);
					}
				}

				setState(2027);
				_la = _input.LA(1);
				if ( !(_la==SYSTEM_TIME || _la==TIMESTAMP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2028);
				match(AS);
				setState(2029);
				match(OF);
				setState(2030);
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
		public TerminalNode GROUP() { return getToken(ArcticSqlCommandParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public List<GroupByClauseContext> groupByClause() {
			return getRuleContexts(GroupByClauseContext.class);
		}
		public GroupByClauseContext groupByClause(int i) {
			return getRuleContext(GroupByClauseContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SETS() { return getToken(ArcticSqlCommandParser.SETS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticSqlCommandParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(ArcticSqlCommandParser.CUBE, 0); }
		public TerminalNode GROUPING() { return getToken(ArcticSqlCommandParser.GROUPING, 0); }
		public AggregationClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAggregationClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAggregationClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAggregationClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationClauseContext aggregationClause() throws RecognitionException {
		AggregationClauseContext _localctx = new AggregationClauseContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_aggregationClause);
		int _la;
		try {
			int _alt;
			setState(2072);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,253,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2033);
				match(GROUP);
				setState(2034);
				match(BY);
				setState(2035);
				((AggregationClauseContext)_localctx).groupByClause = groupByClause();
				((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
				setState(2040);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,249,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2036);
						match(COMMA);
						setState(2037);
						((AggregationClauseContext)_localctx).groupByClause = groupByClause();
						((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
						}
						} 
					}
					setState(2042);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,249,_ctx);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2043);
				match(GROUP);
				setState(2044);
				match(BY);
				setState(2045);
				((AggregationClauseContext)_localctx).expression = expression();
				((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
				setState(2050);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,250,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2046);
						match(COMMA);
						setState(2047);
						((AggregationClauseContext)_localctx).expression = expression();
						((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
						}
						} 
					}
					setState(2052);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,250,_ctx);
				}
				setState(2070);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,252,_ctx) ) {
				case 1:
					{
					setState(2053);
					match(WITH);
					setState(2054);
					((AggregationClauseContext)_localctx).kind = match(ROLLUP);
					}
					break;
				case 2:
					{
					setState(2055);
					match(WITH);
					setState(2056);
					((AggregationClauseContext)_localctx).kind = match(CUBE);
					}
					break;
				case 3:
					{
					setState(2057);
					((AggregationClauseContext)_localctx).kind = match(GROUPING);
					setState(2058);
					match(SETS);
					setState(2059);
					match(LEFT_PAREN);
					setState(2060);
					groupingSet();
					setState(2065);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2061);
						match(COMMA);
						setState(2062);
						groupingSet();
						}
						}
						setState(2067);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2068);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_groupByClause);
		try {
			setState(2076);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2074);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2075);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticSqlCommandParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(ArcticSqlCommandParser.CUBE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public TerminalNode GROUPING() { return getToken(ArcticSqlCommandParser.GROUPING, 0); }
		public TerminalNode SETS() { return getToken(ArcticSqlCommandParser.SETS, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterGroupingAnalytics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitGroupingAnalytics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitGroupingAnalytics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingAnalyticsContext groupingAnalytics() throws RecognitionException {
		GroupingAnalyticsContext _localctx = new GroupingAnalyticsContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_groupingAnalytics);
		int _la;
		try {
			setState(2103);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CUBE:
			case ROLLUP:
				enterOuterAlt(_localctx, 1);
				{
				setState(2078);
				_la = _input.LA(1);
				if ( !(_la==CUBE || _la==ROLLUP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2079);
				match(LEFT_PAREN);
				setState(2080);
				groupingSet();
				setState(2085);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2081);
					match(COMMA);
					setState(2082);
					groupingSet();
					}
					}
					setState(2087);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2088);
				match(RIGHT_PAREN);
				}
				break;
			case GROUPING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2090);
				match(GROUPING);
				setState(2091);
				match(SETS);
				setState(2092);
				match(LEFT_PAREN);
				setState(2093);
				groupingElement();
				setState(2098);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2094);
					match(COMMA);
					setState(2095);
					groupingElement();
					}
					}
					setState(2100);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2101);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterGroupingElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitGroupingElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitGroupingElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingElementContext groupingElement() throws RecognitionException {
		GroupingElementContext _localctx = new GroupingElementContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_groupingElement);
		try {
			setState(2107);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,258,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2105);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2106);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public GroupingSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupingSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterGroupingSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitGroupingSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitGroupingSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingSetContext groupingSet() throws RecognitionException {
		GroupingSetContext _localctx = new GroupingSetContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_groupingSet);
		int _la;
		try {
			setState(2122);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,261,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2109);
				match(LEFT_PAREN);
				setState(2118);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,260,_ctx) ) {
				case 1:
					{
					setState(2110);
					expression();
					setState(2115);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2111);
						match(COMMA);
						setState(2112);
						expression();
						}
						}
						setState(2117);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2120);
				match(RIGHT_PAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2121);
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
		public TerminalNode PIVOT() { return getToken(ArcticSqlCommandParser.PIVOT, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticSqlCommandParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.LEFT_PAREN, i);
		}
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public PivotColumnContext pivotColumn() {
			return getRuleContext(PivotColumnContext.class,0);
		}
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticSqlCommandParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.RIGHT_PAREN, i);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public PivotClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPivotClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPivotClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPivotClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotClauseContext pivotClause() throws RecognitionException {
		PivotClauseContext _localctx = new PivotClauseContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_pivotClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2124);
			match(PIVOT);
			setState(2125);
			match(LEFT_PAREN);
			setState(2126);
			((PivotClauseContext)_localctx).aggregates = namedExpressionSeq();
			setState(2127);
			match(FOR);
			setState(2128);
			pivotColumn();
			setState(2129);
			match(IN);
			setState(2130);
			match(LEFT_PAREN);
			setState(2131);
			((PivotClauseContext)_localctx).pivotValue = pivotValue();
			((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
			setState(2136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2132);
				match(COMMA);
				setState(2133);
				((PivotClauseContext)_localctx).pivotValue = pivotValue();
				((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
				}
				}
				setState(2138);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2139);
			match(RIGHT_PAREN);
			setState(2140);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public PivotColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotColumn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPivotColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPivotColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPivotColumn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotColumnContext pivotColumn() throws RecognitionException {
		PivotColumnContext _localctx = new PivotColumnContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_pivotColumn);
		int _la;
		try {
			setState(2154);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2142);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2143);
				match(LEFT_PAREN);
				setState(2144);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				setState(2149);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2145);
					match(COMMA);
					setState(2146);
					((PivotColumnContext)_localctx).identifier = identifier();
					((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
					}
					}
					setState(2151);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2152);
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
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public PivotValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPivotValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPivotValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPivotValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotValueContext pivotValue() throws RecognitionException {
		PivotValueContext _localctx = new PivotValueContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_pivotValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2156);
			expression();
			setState(2161);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,266,_ctx) ) {
			case 1:
				{
				setState(2158);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,265,_ctx) ) {
				case 1:
					{
					setState(2157);
					match(AS);
					}
					break;
				}
				setState(2160);
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
		public TerminalNode LATERAL() { return getToken(ArcticSqlCommandParser.LATERAL, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode OUTER() { return getToken(ArcticSqlCommandParser.OUTER, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public LateralViewContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lateralView; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLateralView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLateralView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLateralView(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LateralViewContext lateralView() throws RecognitionException {
		LateralViewContext _localctx = new LateralViewContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_lateralView);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2163);
			match(LATERAL);
			setState(2164);
			match(VIEW);
			setState(2166);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,267,_ctx) ) {
			case 1:
				{
				setState(2165);
				match(OUTER);
				}
				break;
			}
			setState(2168);
			qualifiedName();
			setState(2169);
			match(LEFT_PAREN);
			setState(2178);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,269,_ctx) ) {
			case 1:
				{
				setState(2170);
				expression();
				setState(2175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2171);
					match(COMMA);
					setState(2172);
					expression();
					}
					}
					setState(2177);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2180);
			match(RIGHT_PAREN);
			setState(2181);
			((LateralViewContext)_localctx).tblName = identifier();
			setState(2193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,272,_ctx) ) {
			case 1:
				{
				setState(2183);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,270,_ctx) ) {
				case 1:
					{
					setState(2182);
					match(AS);
					}
					break;
				}
				setState(2185);
				((LateralViewContext)_localctx).identifier = identifier();
				((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
				setState(2190);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2186);
						match(COMMA);
						setState(2187);
						((LateralViewContext)_localctx).identifier = identifier();
						((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
						}
						} 
					}
					setState(2192);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
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
		public TerminalNode DISTINCT() { return getToken(ArcticSqlCommandParser.DISTINCT, 0); }
		public TerminalNode ALL() { return getToken(ArcticSqlCommandParser.ALL, 0); }
		public SetQuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setQuantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSetQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSetQuantifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSetQuantifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetQuantifierContext setQuantifier() throws RecognitionException {
		SetQuantifierContext _localctx = new SetQuantifierContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_setQuantifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2195);
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
		public TerminalNode LATERAL() { return getToken(ArcticSqlCommandParser.LATERAL, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_relation);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2198);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,273,_ctx) ) {
			case 1:
				{
				setState(2197);
				match(LATERAL);
				}
				break;
			}
			setState(2200);
			relationPrimary();
			setState(2204);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2201);
					joinRelation();
					}
					} 
				}
				setState(2206);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
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
		public TerminalNode JOIN() { return getToken(ArcticSqlCommandParser.JOIN, 0); }
		public RelationPrimaryContext relationPrimary() {
			return getRuleContext(RelationPrimaryContext.class,0);
		}
		public JoinTypeContext joinType() {
			return getRuleContext(JoinTypeContext.class,0);
		}
		public TerminalNode LATERAL() { return getToken(ArcticSqlCommandParser.LATERAL, 0); }
		public JoinCriteriaContext joinCriteria() {
			return getRuleContext(JoinCriteriaContext.class,0);
		}
		public TerminalNode NATURAL() { return getToken(ArcticSqlCommandParser.NATURAL, 0); }
		public JoinRelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinRelation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterJoinRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitJoinRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitJoinRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinRelationContext joinRelation() throws RecognitionException {
		JoinRelationContext _localctx = new JoinRelationContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_joinRelation);
		try {
			setState(2224);
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
				setState(2207);
				joinType();
				}
				setState(2208);
				match(JOIN);
				setState(2210);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,275,_ctx) ) {
				case 1:
					{
					setState(2209);
					match(LATERAL);
					}
					break;
				}
				setState(2212);
				((JoinRelationContext)_localctx).right = relationPrimary();
				setState(2214);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,276,_ctx) ) {
				case 1:
					{
					setState(2213);
					joinCriteria();
					}
					break;
				}
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2216);
				match(NATURAL);
				setState(2217);
				joinType();
				setState(2218);
				match(JOIN);
				setState(2220);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,277,_ctx) ) {
				case 1:
					{
					setState(2219);
					match(LATERAL);
					}
					break;
				}
				setState(2222);
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
		public TerminalNode INNER() { return getToken(ArcticSqlCommandParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(ArcticSqlCommandParser.CROSS, 0); }
		public TerminalNode LEFT() { return getToken(ArcticSqlCommandParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(ArcticSqlCommandParser.OUTER, 0); }
		public TerminalNode SEMI() { return getToken(ArcticSqlCommandParser.SEMI, 0); }
		public TerminalNode RIGHT() { return getToken(ArcticSqlCommandParser.RIGHT, 0); }
		public TerminalNode FULL() { return getToken(ArcticSqlCommandParser.FULL, 0); }
		public TerminalNode ANTI() { return getToken(ArcticSqlCommandParser.ANTI, 0); }
		public JoinTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterJoinType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitJoinType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitJoinType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinTypeContext joinType() throws RecognitionException {
		JoinTypeContext _localctx = new JoinTypeContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_joinType);
		int _la;
		try {
			setState(2250);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,285,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2227);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(2226);
					match(INNER);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2229);
				match(CROSS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2230);
				match(LEFT);
				setState(2232);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2231);
					match(OUTER);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2235);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2234);
					match(LEFT);
					}
				}

				setState(2237);
				match(SEMI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2238);
				match(RIGHT);
				setState(2240);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2239);
					match(OUTER);
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2242);
				match(FULL);
				setState(2244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2243);
					match(OUTER);
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2247);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2246);
					match(LEFT);
					}
				}

				setState(2249);
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
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public JoinCriteriaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCriteria; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterJoinCriteria(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitJoinCriteria(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitJoinCriteria(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinCriteriaContext joinCriteria() throws RecognitionException {
		JoinCriteriaContext _localctx = new JoinCriteriaContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_joinCriteria);
		try {
			setState(2256);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(2252);
				match(ON);
				setState(2253);
				booleanExpression(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2254);
				match(USING);
				setState(2255);
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
		public TerminalNode TABLESAMPLE() { return getToken(ArcticSqlCommandParser.TABLESAMPLE, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticSqlCommandParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticSqlCommandParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.RIGHT_PAREN, i);
		}
		public SampleMethodContext sampleMethod() {
			return getRuleContext(SampleMethodContext.class,0);
		}
		public TerminalNode REPEATABLE() { return getToken(ArcticSqlCommandParser.REPEATABLE, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public SampleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sample; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSample(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSample(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSample(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SampleContext sample() throws RecognitionException {
		SampleContext _localctx = new SampleContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_sample);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2258);
			match(TABLESAMPLE);
			setState(2259);
			match(LEFT_PAREN);
			setState(2261);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,287,_ctx) ) {
			case 1:
				{
				setState(2260);
				sampleMethod();
				}
				break;
			}
			setState(2263);
			match(RIGHT_PAREN);
			setState(2268);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,288,_ctx) ) {
			case 1:
				{
				setState(2264);
				match(REPEATABLE);
				setState(2265);
				match(LEFT_PAREN);
				setState(2266);
				((SampleContext)_localctx).seed = match(INTEGER_VALUE);
				setState(2267);
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
		public TerminalNode ROWS() { return getToken(ArcticSqlCommandParser.ROWS, 0); }
		public SampleByRowsContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSampleByRows(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSampleByRows(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSampleByRows(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByPercentileContext extends SampleMethodContext {
		public Token negativeSign;
		public Token percentage;
		public TerminalNode PERCENTLIT() { return getToken(ArcticSqlCommandParser.PERCENTLIT, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticSqlCommandParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public SampleByPercentileContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSampleByPercentile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSampleByPercentile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSampleByPercentile(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByBucketContext extends SampleMethodContext {
		public Token sampleType;
		public Token numerator;
		public Token denominator;
		public TerminalNode OUT() { return getToken(ArcticSqlCommandParser.OUT, 0); }
		public TerminalNode OF() { return getToken(ArcticSqlCommandParser.OF, 0); }
		public TerminalNode BUCKET() { return getToken(ArcticSqlCommandParser.BUCKET, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(ArcticSqlCommandParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(ArcticSqlCommandParser.INTEGER_VALUE, i);
		}
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public SampleByBucketContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSampleByBucket(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSampleByBucket(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSampleByBucket(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSampleByBytes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSampleByBytes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSampleByBytes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SampleMethodContext sampleMethod() throws RecognitionException {
		SampleMethodContext _localctx = new SampleMethodContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_sampleMethod);
		int _la;
		try {
			setState(2294);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,292,_ctx) ) {
			case 1:
				_localctx = new SampleByPercentileContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2271);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2270);
					((SampleByPercentileContext)_localctx).negativeSign = match(MINUS);
					}
				}

				setState(2273);
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
				setState(2274);
				match(PERCENTLIT);
				}
				break;
			case 2:
				_localctx = new SampleByRowsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2275);
				expression();
				setState(2276);
				match(ROWS);
				}
				break;
			case 3:
				_localctx = new SampleByBucketContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2278);
				((SampleByBucketContext)_localctx).sampleType = match(BUCKET);
				setState(2279);
				((SampleByBucketContext)_localctx).numerator = match(INTEGER_VALUE);
				setState(2280);
				match(OUT);
				setState(2281);
				match(OF);
				setState(2282);
				((SampleByBucketContext)_localctx).denominator = match(INTEGER_VALUE);
				setState(2291);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(2283);
					match(ON);
					setState(2289);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
					case 1:
						{
						setState(2284);
						identifier();
						}
						break;
					case 2:
						{
						setState(2285);
						qualifiedName();
						setState(2286);
						match(LEFT_PAREN);
						setState(2287);
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
				setState(2293);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public IdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierListContext identifierList() throws RecognitionException {
		IdentifierListContext _localctx = new IdentifierListContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_identifierList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2296);
			match(LEFT_PAREN);
			setState(2297);
			identifierSeq();
			setState(2298);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public IdentifierSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIdentifierSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIdentifierSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIdentifierSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierSeqContext identifierSeq() throws RecognitionException {
		IdentifierSeqContext _localctx = new IdentifierSeqContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_identifierSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2300);
			((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
			setState(2305);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,293,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2301);
					match(COMMA);
					setState(2302);
					((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2307);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,293,_ctx);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<OrderedIdentifierContext> orderedIdentifier() {
			return getRuleContexts(OrderedIdentifierContext.class);
		}
		public OrderedIdentifierContext orderedIdentifier(int i) {
			return getRuleContext(OrderedIdentifierContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public OrderedIdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedIdentifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterOrderedIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitOrderedIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitOrderedIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedIdentifierListContext orderedIdentifierList() throws RecognitionException {
		OrderedIdentifierListContext _localctx = new OrderedIdentifierListContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_orderedIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2308);
			match(LEFT_PAREN);
			setState(2309);
			orderedIdentifier();
			setState(2314);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2310);
				match(COMMA);
				setState(2311);
				orderedIdentifier();
				}
				}
				setState(2316);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2317);
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
		public TerminalNode ASC() { return getToken(ArcticSqlCommandParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public OrderedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterOrderedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitOrderedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitOrderedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedIdentifierContext orderedIdentifier() throws RecognitionException {
		OrderedIdentifierContext _localctx = new OrderedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_orderedIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2319);
			((OrderedIdentifierContext)_localctx).ident = errorCapturingIdentifier();
			setState(2321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(2320);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<IdentifierCommentContext> identifierComment() {
			return getRuleContexts(IdentifierCommentContext.class);
		}
		public IdentifierCommentContext identifierComment(int i) {
			return getRuleContext(IdentifierCommentContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public IdentifierCommentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierCommentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIdentifierCommentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIdentifierCommentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIdentifierCommentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierCommentListContext identifierCommentList() throws RecognitionException {
		IdentifierCommentListContext _localctx = new IdentifierCommentListContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_identifierCommentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2323);
			match(LEFT_PAREN);
			setState(2324);
			identifierComment();
			setState(2329);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2325);
				match(COMMA);
				setState(2326);
				identifierComment();
				}
				}
				setState(2331);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2332);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIdentifierComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIdentifierComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIdentifierComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierCommentContext identifierComment() throws RecognitionException {
		IdentifierCommentContext _localctx = new IdentifierCommentContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_identifierComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2334);
			identifier();
			setState(2336);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2335);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableValuedFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableValuedFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableValuedFunction(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInlineTableDefault2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInlineTableDefault2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInlineTableDefault2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AliasedRelationContext extends RelationPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public RelationContext relation() {
			return getRuleContext(RelationContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public AliasedRelationContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAliasedRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAliasedRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAliasedRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AliasedQueryContext extends RelationPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public AliasedQueryContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAliasedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAliasedQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAliasedQuery(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationPrimaryContext relationPrimary() throws RecognitionException {
		RelationPrimaryContext _localctx = new RelationPrimaryContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_relationPrimary);
		try {
			setState(2365);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,302,_ctx) ) {
			case 1:
				_localctx = new TableNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2338);
				multipartIdentifier();
				setState(2340);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,298,_ctx) ) {
				case 1:
					{
					setState(2339);
					temporalClause();
					}
					break;
				}
				setState(2343);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,299,_ctx) ) {
				case 1:
					{
					setState(2342);
					sample();
					}
					break;
				}
				setState(2345);
				tableAlias();
				}
				break;
			case 2:
				_localctx = new AliasedQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2347);
				match(LEFT_PAREN);
				setState(2348);
				query();
				setState(2349);
				match(RIGHT_PAREN);
				setState(2351);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,300,_ctx) ) {
				case 1:
					{
					setState(2350);
					sample();
					}
					break;
				}
				setState(2353);
				tableAlias();
				}
				break;
			case 3:
				_localctx = new AliasedRelationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2355);
				match(LEFT_PAREN);
				setState(2356);
				relation();
				setState(2357);
				match(RIGHT_PAREN);
				setState(2359);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,301,_ctx) ) {
				case 1:
					{
					setState(2358);
					sample();
					}
					break;
				}
				setState(2361);
				tableAlias();
				}
				break;
			case 4:
				_localctx = new InlineTableDefault2Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2363);
				inlineTable();
				}
				break;
			case 5:
				_localctx = new TableValuedFunctionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2364);
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
		public TerminalNode VALUES() { return getToken(ArcticSqlCommandParser.VALUES, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public InlineTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInlineTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInlineTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInlineTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InlineTableContext inlineTable() throws RecognitionException {
		InlineTableContext _localctx = new InlineTableContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_inlineTable);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2367);
			match(VALUES);
			setState(2368);
			expression();
			setState(2373);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,303,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2369);
					match(COMMA);
					setState(2370);
					expression();
					}
					} 
				}
				setState(2375);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,303,_ctx);
			}
			setState(2376);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public FunctionTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFunctionTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFunctionTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFunctionTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTableContext functionTable() throws RecognitionException {
		FunctionTableContext _localctx = new FunctionTableContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_functionTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2378);
			((FunctionTableContext)_localctx).funcName = functionName();
			setState(2379);
			match(LEFT_PAREN);
			setState(2388);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,305,_ctx) ) {
			case 1:
				{
				setState(2380);
				expression();
				setState(2385);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2381);
					match(COMMA);
					setState(2382);
					expression();
					}
					}
					setState(2387);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2390);
			match(RIGHT_PAREN);
			setState(2391);
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
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TableAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableAliasContext tableAlias() throws RecognitionException {
		TableAliasContext _localctx = new TableAliasContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_tableAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2400);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,308,_ctx) ) {
			case 1:
				{
				setState(2394);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,306,_ctx) ) {
				case 1:
					{
					setState(2393);
					match(AS);
					}
					break;
				}
				setState(2396);
				strictIdentifier();
				setState(2398);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,307,_ctx) ) {
				case 1:
					{
					setState(2397);
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
		public TerminalNode ROW() { return getToken(ArcticSqlCommandParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticSqlCommandParser.FORMAT, 0); }
		public TerminalNode SERDE() { return getToken(ArcticSqlCommandParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public RowFormatSerdeContext(RowFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRowFormatSerde(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRowFormatSerde(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRowFormatSerde(this);
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
		public TerminalNode ROW() { return getToken(ArcticSqlCommandParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticSqlCommandParser.FORMAT, 0); }
		public TerminalNode DELIMITED() { return getToken(ArcticSqlCommandParser.DELIMITED, 0); }
		public TerminalNode FIELDS() { return getToken(ArcticSqlCommandParser.FIELDS, 0); }
		public List<TerminalNode> TERMINATED() { return getTokens(ArcticSqlCommandParser.TERMINATED); }
		public TerminalNode TERMINATED(int i) {
			return getToken(ArcticSqlCommandParser.TERMINATED, i);
		}
		public List<TerminalNode> BY() { return getTokens(ArcticSqlCommandParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticSqlCommandParser.BY, i);
		}
		public TerminalNode COLLECTION() { return getToken(ArcticSqlCommandParser.COLLECTION, 0); }
		public TerminalNode ITEMS() { return getToken(ArcticSqlCommandParser.ITEMS, 0); }
		public TerminalNode MAP() { return getToken(ArcticSqlCommandParser.MAP, 0); }
		public TerminalNode KEYS() { return getToken(ArcticSqlCommandParser.KEYS, 0); }
		public TerminalNode LINES() { return getToken(ArcticSqlCommandParser.LINES, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public TerminalNode DEFINED() { return getToken(ArcticSqlCommandParser.DEFINED, 0); }
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public List<TerminalNode> STRING() { return getTokens(ArcticSqlCommandParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticSqlCommandParser.STRING, i);
		}
		public TerminalNode ESCAPED() { return getToken(ArcticSqlCommandParser.ESCAPED, 0); }
		public RowFormatDelimitedContext(RowFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRowFormatDelimited(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRowFormatDelimited(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRowFormatDelimited(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowFormatContext rowFormat() throws RecognitionException {
		RowFormatContext _localctx = new RowFormatContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_rowFormat);
		try {
			setState(2451);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,316,_ctx) ) {
			case 1:
				_localctx = new RowFormatSerdeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2402);
				match(ROW);
				setState(2403);
				match(FORMAT);
				setState(2404);
				match(SERDE);
				setState(2405);
				((RowFormatSerdeContext)_localctx).name = match(STRING);
				setState(2409);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,309,_ctx) ) {
				case 1:
					{
					setState(2406);
					match(WITH);
					setState(2407);
					match(SERDEPROPERTIES);
					setState(2408);
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
				setState(2411);
				match(ROW);
				setState(2412);
				match(FORMAT);
				setState(2413);
				match(DELIMITED);
				setState(2423);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,311,_ctx) ) {
				case 1:
					{
					setState(2414);
					match(FIELDS);
					setState(2415);
					match(TERMINATED);
					setState(2416);
					match(BY);
					setState(2417);
					((RowFormatDelimitedContext)_localctx).fieldsTerminatedBy = match(STRING);
					setState(2421);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,310,_ctx) ) {
					case 1:
						{
						setState(2418);
						match(ESCAPED);
						setState(2419);
						match(BY);
						setState(2420);
						((RowFormatDelimitedContext)_localctx).escapedBy = match(STRING);
						}
						break;
					}
					}
					break;
				}
				setState(2430);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,312,_ctx) ) {
				case 1:
					{
					setState(2425);
					match(COLLECTION);
					setState(2426);
					match(ITEMS);
					setState(2427);
					match(TERMINATED);
					setState(2428);
					match(BY);
					setState(2429);
					((RowFormatDelimitedContext)_localctx).collectionItemsTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2437);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,313,_ctx) ) {
				case 1:
					{
					setState(2432);
					match(MAP);
					setState(2433);
					match(KEYS);
					setState(2434);
					match(TERMINATED);
					setState(2435);
					match(BY);
					setState(2436);
					((RowFormatDelimitedContext)_localctx).keysTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2443);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,314,_ctx) ) {
				case 1:
					{
					setState(2439);
					match(LINES);
					setState(2440);
					match(TERMINATED);
					setState(2441);
					match(BY);
					setState(2442);
					((RowFormatDelimitedContext)_localctx).linesSeparatedBy = match(STRING);
					}
					break;
				}
				setState(2449);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,315,_ctx) ) {
				case 1:
					{
					setState(2445);
					match(NULL);
					setState(2446);
					match(DEFINED);
					setState(2447);
					match(AS);
					setState(2448);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public MultipartIdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultipartIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultipartIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultipartIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierListContext multipartIdentifierList() throws RecognitionException {
		MultipartIdentifierListContext _localctx = new MultipartIdentifierListContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_multipartIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2453);
			multipartIdentifier();
			setState(2458);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2454);
				match(COMMA);
				setState(2455);
				multipartIdentifier();
				}
				}
				setState(2460);
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
		public List<TerminalNode> DOT() { return getTokens(ArcticSqlCommandParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticSqlCommandParser.DOT, i);
		}
		public MultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierContext multipartIdentifier() throws RecognitionException {
		MultipartIdentifierContext _localctx = new MultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_multipartIdentifier);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2461);
			((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
			setState(2466);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,318,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2462);
					match(DOT);
					setState(2463);
					((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2468);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,318,_ctx);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public MultipartIdentifierPropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierPropertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultipartIdentifierPropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultipartIdentifierPropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultipartIdentifierPropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierPropertyListContext multipartIdentifierPropertyList() throws RecognitionException {
		MultipartIdentifierPropertyListContext _localctx = new MultipartIdentifierPropertyListContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_multipartIdentifierPropertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2469);
			multipartIdentifierProperty();
			setState(2474);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2470);
				match(COMMA);
				setState(2471);
				multipartIdentifierProperty();
				}
				}
				setState(2476);
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
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public MultipartIdentifierPropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultipartIdentifierProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultipartIdentifierProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultipartIdentifierProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierPropertyContext multipartIdentifierProperty() throws RecognitionException {
		MultipartIdentifierPropertyContext _localctx = new MultipartIdentifierPropertyContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_multipartIdentifierProperty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2477);
			multipartIdentifier();
			setState(2480);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPTIONS) {
				{
				setState(2478);
				match(OPTIONS);
				setState(2479);
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
		public TerminalNode DOT() { return getToken(ArcticSqlCommandParser.DOT, 0); }
		public TableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableIdentifierContext tableIdentifier() throws RecognitionException {
		TableIdentifierContext _localctx = new TableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_tableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2485);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,321,_ctx) ) {
			case 1:
				{
				setState(2482);
				((TableIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2483);
				match(DOT);
				}
				break;
			}
			setState(2487);
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
		public TerminalNode DOT() { return getToken(ArcticSqlCommandParser.DOT, 0); }
		public FunctionIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFunctionIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFunctionIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFunctionIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionIdentifierContext functionIdentifier() throws RecognitionException {
		FunctionIdentifierContext _localctx = new FunctionIdentifierContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_functionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2492);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,322,_ctx) ) {
			case 1:
				{
				setState(2489);
				((FunctionIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2490);
				match(DOT);
				}
				break;
			}
			setState(2494);
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
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public NamedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNamedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNamedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNamedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedExpressionContext namedExpression() throws RecognitionException {
		NamedExpressionContext _localctx = new NamedExpressionContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_namedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2496);
			expression();
			setState(2504);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,325,_ctx) ) {
			case 1:
				{
				setState(2498);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,323,_ctx) ) {
				case 1:
					{
					setState(2497);
					match(AS);
					}
					break;
				}
				setState(2502);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,324,_ctx) ) {
				case 1:
					{
					setState(2500);
					((NamedExpressionContext)_localctx).name = errorCapturingIdentifier();
					}
					break;
				case 2:
					{
					setState(2501);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public NamedExpressionSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedExpressionSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNamedExpressionSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNamedExpressionSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNamedExpressionSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedExpressionSeqContext namedExpressionSeq() throws RecognitionException {
		NamedExpressionSeqContext _localctx = new NamedExpressionSeqContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_namedExpressionSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2506);
			namedExpression();
			setState(2511);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,326,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2507);
					match(COMMA);
					setState(2508);
					namedExpression();
					}
					} 
				}
				setState(2513);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,326,_ctx);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<PartitionFieldContext> partitionField() {
			return getRuleContexts(PartitionFieldContext.class);
		}
		public PartitionFieldContext partitionField(int i) {
			return getRuleContext(PartitionFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public PartitionFieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionFieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPartitionFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPartitionFieldList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPartitionFieldList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionFieldListContext partitionFieldList() throws RecognitionException {
		PartitionFieldListContext _localctx = new PartitionFieldListContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_partitionFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2514);
			match(LEFT_PAREN);
			setState(2515);
			((PartitionFieldListContext)_localctx).partitionField = partitionField();
			((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
			setState(2520);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2516);
				match(COMMA);
				setState(2517);
				((PartitionFieldListContext)_localctx).partitionField = partitionField();
				((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
				}
				}
				setState(2522);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2523);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPartitionColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPartitionColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPartitionColumn(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPartitionTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPartitionTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPartitionTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionFieldContext partitionField() throws RecognitionException {
		PartitionFieldContext _localctx = new PartitionFieldContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_partitionField);
		try {
			setState(2527);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,328,_ctx) ) {
			case 1:
				_localctx = new PartitionTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2525);
				transform();
				}
				break;
			case 2:
				_localctx = new PartitionColumnContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2526);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIdentityTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIdentityTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIdentityTransform(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ApplyTransformContext extends TransformContext {
		public IdentifierContext transformName;
		public TransformArgumentContext transformArgument;
		public List<TransformArgumentContext> argument = new ArrayList<TransformArgumentContext>();
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TransformArgumentContext> transformArgument() {
			return getRuleContexts(TransformArgumentContext.class);
		}
		public TransformArgumentContext transformArgument(int i) {
			return getRuleContext(TransformArgumentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public ApplyTransformContext(TransformContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterApplyTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitApplyTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitApplyTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformContext transform() throws RecognitionException {
		TransformContext _localctx = new TransformContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_transform);
		int _la;
		try {
			setState(2542);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,330,_ctx) ) {
			case 1:
				_localctx = new IdentityTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2529);
				qualifiedName();
				}
				break;
			case 2:
				_localctx = new ApplyTransformContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2530);
				((ApplyTransformContext)_localctx).transformName = identifier();
				setState(2531);
				match(LEFT_PAREN);
				setState(2532);
				((ApplyTransformContext)_localctx).transformArgument = transformArgument();
				((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
				setState(2537);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2533);
					match(COMMA);
					setState(2534);
					((ApplyTransformContext)_localctx).transformArgument = transformArgument();
					((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
					}
					}
					setState(2539);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2540);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTransformArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTransformArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTransformArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformArgumentContext transformArgument() throws RecognitionException {
		TransformArgumentContext _localctx = new TransformArgumentContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_transformArgument);
		try {
			setState(2546);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,331,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2544);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2545);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2548);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public ExpressionSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterExpressionSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitExpressionSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitExpressionSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionSeqContext expressionSeq() throws RecognitionException {
		ExpressionSeqContext _localctx = new ExpressionSeqContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_expressionSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2550);
			expression();
			setState(2555);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2551);
				match(COMMA);
				setState(2552);
				expression();
				}
				}
				setState(2557);
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
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public LogicalNotContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLogicalNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLogicalNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLogicalNot(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPredicated(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPredicated(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPredicated(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExistsContext extends BooleanExpressionContext {
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public ExistsContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitExists(this);
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
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public LogicalBinaryContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLogicalBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLogicalBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLogicalBinary(this);
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
		int _startState = 214;
		enterRecursionRule(_localctx, 214, RULE_booleanExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2570);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,334,_ctx) ) {
			case 1:
				{
				_localctx = new LogicalNotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2559);
				match(NOT);
				setState(2560);
				booleanExpression(5);
				}
				break;
			case 2:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2561);
				match(EXISTS);
				setState(2562);
				match(LEFT_PAREN);
				setState(2563);
				query();
				setState(2564);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new PredicatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2566);
				valueExpression(0);
				setState(2568);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,333,_ctx) ) {
				case 1:
					{
					setState(2567);
					predicate();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2580);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,336,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2578);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,335,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2572);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2573);
						((LogicalBinaryContext)_localctx).operator = match(AND);
						setState(2574);
						((LogicalBinaryContext)_localctx).right = booleanExpression(3);
						}
						break;
					case 2:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2575);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2576);
						((LogicalBinaryContext)_localctx).operator = match(OR);
						setState(2577);
						((LogicalBinaryContext)_localctx).right = booleanExpression(2);
						}
						break;
					}
					} 
				}
				setState(2582);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,336,_ctx);
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
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticSqlCommandParser.BETWEEN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RLIKE() { return getToken(ArcticSqlCommandParser.RLIKE, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(ArcticSqlCommandParser.ILIKE, 0); }
		public TerminalNode ANY() { return getToken(ArcticSqlCommandParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(ArcticSqlCommandParser.SOME, 0); }
		public TerminalNode ALL() { return getToken(ArcticSqlCommandParser.ALL, 0); }
		public TerminalNode ESCAPE() { return getToken(ArcticSqlCommandParser.ESCAPE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode IS() { return getToken(ArcticSqlCommandParser.IS, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public TerminalNode TRUE() { return getToken(ArcticSqlCommandParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ArcticSqlCommandParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(ArcticSqlCommandParser.UNKNOWN, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode DISTINCT() { return getToken(ArcticSqlCommandParser.DISTINCT, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_predicate);
		int _la;
		try {
			setState(2665);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,350,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2584);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2583);
					match(NOT);
					}
				}

				setState(2586);
				((PredicateContext)_localctx).kind = match(BETWEEN);
				setState(2587);
				((PredicateContext)_localctx).lower = valueExpression(0);
				setState(2588);
				match(AND);
				setState(2589);
				((PredicateContext)_localctx).upper = valueExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2592);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2591);
					match(NOT);
					}
				}

				setState(2594);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2595);
				match(LEFT_PAREN);
				setState(2596);
				expression();
				setState(2601);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2597);
					match(COMMA);
					setState(2598);
					expression();
					}
					}
					setState(2603);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2604);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2607);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2606);
					match(NOT);
					}
				}

				setState(2609);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2610);
				match(LEFT_PAREN);
				setState(2611);
				query();
				setState(2612);
				match(RIGHT_PAREN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2615);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2614);
					match(NOT);
					}
				}

				setState(2617);
				((PredicateContext)_localctx).kind = match(RLIKE);
				setState(2618);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2620);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2619);
					match(NOT);
					}
				}

				setState(2622);
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
				setState(2623);
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
				setState(2637);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,344,_ctx) ) {
				case 1:
					{
					setState(2624);
					match(LEFT_PAREN);
					setState(2625);
					match(RIGHT_PAREN);
					}
					break;
				case 2:
					{
					setState(2626);
					match(LEFT_PAREN);
					setState(2627);
					expression();
					setState(2632);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2628);
						match(COMMA);
						setState(2629);
						expression();
						}
						}
						setState(2634);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2635);
					match(RIGHT_PAREN);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2640);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2639);
					match(NOT);
					}
				}

				setState(2642);
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
				setState(2643);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				setState(2646);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,346,_ctx) ) {
				case 1:
					{
					setState(2644);
					match(ESCAPE);
					setState(2645);
					((PredicateContext)_localctx).escapeChar = match(STRING);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2648);
				match(IS);
				setState(2650);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2649);
					match(NOT);
					}
				}

				setState(2652);
				((PredicateContext)_localctx).kind = match(NULL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2653);
				match(IS);
				setState(2655);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2654);
					match(NOT);
					}
				}

				setState(2657);
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
				setState(2658);
				match(IS);
				setState(2660);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2659);
					match(NOT);
					}
				}

				setState(2662);
				((PredicateContext)_localctx).kind = match(DISTINCT);
				setState(2663);
				match(FROM);
				setState(2664);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterValueExpressionDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitValueExpressionDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitValueExpressionDefault(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitComparison(this);
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
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(ArcticSqlCommandParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(ArcticSqlCommandParser.PERCENT, 0); }
		public TerminalNode DIV() { return getToken(ArcticSqlCommandParser.DIV, 0); }
		public TerminalNode PLUS() { return getToken(ArcticSqlCommandParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public TerminalNode CONCAT_PIPE() { return getToken(ArcticSqlCommandParser.CONCAT_PIPE, 0); }
		public TerminalNode AMPERSAND() { return getToken(ArcticSqlCommandParser.AMPERSAND, 0); }
		public TerminalNode HAT() { return getToken(ArcticSqlCommandParser.HAT, 0); }
		public TerminalNode PIPE() { return getToken(ArcticSqlCommandParser.PIPE, 0); }
		public ArithmeticBinaryContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterArithmeticBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitArithmeticBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitArithmeticBinary(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ArithmeticUnaryContext extends ValueExpressionContext {
		public Token operator;
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(ArcticSqlCommandParser.PLUS, 0); }
		public TerminalNode TILDE() { return getToken(ArcticSqlCommandParser.TILDE, 0); }
		public ArithmeticUnaryContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterArithmeticUnary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitArithmeticUnary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitArithmeticUnary(this);
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
		int _startState = 218;
		enterRecursionRule(_localctx, 218, RULE_valueExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2671);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,351,_ctx) ) {
			case 1:
				{
				_localctx = new ValueExpressionDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2668);
				primaryExpression(0);
				}
				break;
			case 2:
				{
				_localctx = new ArithmeticUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2669);
				((ArithmeticUnaryContext)_localctx).operator = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 298)) & ~0x3f) == 0 && ((1L << (_la - 298)) & ((1L << (PLUS - 298)) | (1L << (MINUS - 298)) | (1L << (TILDE - 298)))) != 0)) ) {
					((ArithmeticUnaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2670);
				valueExpression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2694);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,353,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2692);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,352,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2673);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2674);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==DIV || ((((_la - 300)) & ~0x3f) == 0 && ((1L << (_la - 300)) & ((1L << (ASTERISK - 300)) | (1L << (SLASH - 300)) | (1L << (PERCENT - 300)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2675);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(7);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2676);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(2677);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 298)) & ~0x3f) == 0 && ((1L << (_la - 298)) & ((1L << (PLUS - 298)) | (1L << (MINUS - 298)) | (1L << (CONCAT_PIPE - 298)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2678);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(6);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2679);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(2680);
						((ArithmeticBinaryContext)_localctx).operator = match(AMPERSAND);
						setState(2681);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(5);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2682);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(2683);
						((ArithmeticBinaryContext)_localctx).operator = match(HAT);
						setState(2684);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(4);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2685);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2686);
						((ArithmeticBinaryContext)_localctx).operator = match(PIPE);
						setState(2687);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(3);
						}
						break;
					case 6:
						{
						_localctx = new ComparisonContext(new ValueExpressionContext(_parentctx, _parentState));
						((ComparisonContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2688);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2689);
						comparisonOperator();
						setState(2690);
						((ComparisonContext)_localctx).right = valueExpression(2);
						}
						break;
					}
					} 
				}
				setState(2696);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,353,_ctx);
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
		public TerminalNode YEAR() { return getToken(ArcticSqlCommandParser.YEAR, 0); }
		public TerminalNode QUARTER() { return getToken(ArcticSqlCommandParser.QUARTER, 0); }
		public TerminalNode MONTH() { return getToken(ArcticSqlCommandParser.MONTH, 0); }
		public TerminalNode WEEK() { return getToken(ArcticSqlCommandParser.WEEK, 0); }
		public TerminalNode DAY() { return getToken(ArcticSqlCommandParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(ArcticSqlCommandParser.DAYOFYEAR, 0); }
		public TerminalNode HOUR() { return getToken(ArcticSqlCommandParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(ArcticSqlCommandParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(ArcticSqlCommandParser.SECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(ArcticSqlCommandParser.MILLISECOND, 0); }
		public TerminalNode MICROSECOND() { return getToken(ArcticSqlCommandParser.MICROSECOND, 0); }
		public DatetimeUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datetimeUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDatetimeUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDatetimeUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDatetimeUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatetimeUnitContext datetimeUnit() throws RecognitionException {
		DatetimeUnitContext _localctx = new DatetimeUnitContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_datetimeUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2697);
			_la = _input.LA(1);
			if ( !(_la==DAY || _la==DAYOFYEAR || ((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (HOUR - 114)) | (1L << (MICROSECOND - 114)) | (1L << (MILLISECOND - 114)) | (1L << (MINUTE - 114)) | (1L << (MONTH - 114)))) != 0) || _la==QUARTER || _la==SECOND || _la==WEEK || _la==YEAR) ) {
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
		public TerminalNode STRUCT() { return getToken(ArcticSqlCommandParser.STRUCT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public StructContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitStruct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitStruct(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DereferenceContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext base;
		public IdentifierContext fieldName;
		public TerminalNode DOT() { return getToken(ArcticSqlCommandParser.DOT, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DereferenceContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDereference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDereference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDereference(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TimestampaddContext extends PrimaryExpressionContext {
		public Token name;
		public DatetimeUnitContext unit;
		public ValueExpressionContext unitsAmount;
		public ValueExpressionContext timestamp;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public DatetimeUnitContext datetimeUnit() {
			return getRuleContext(DatetimeUnitContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode TIMESTAMPADD() { return getToken(ArcticSqlCommandParser.TIMESTAMPADD, 0); }
		public TerminalNode DATEADD() { return getToken(ArcticSqlCommandParser.DATEADD, 0); }
		public TimestampaddContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTimestampadd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTimestampadd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTimestampadd(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubstringContext extends PrimaryExpressionContext {
		public ValueExpressionContext str;
		public ValueExpressionContext pos;
		public ValueExpressionContext len;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode SUBSTR() { return getToken(ArcticSqlCommandParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticSqlCommandParser.SUBSTRING, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public SubstringContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSubstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSubstring(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSubstring(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CastContext extends PrimaryExpressionContext {
		public Token name;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode CAST() { return getToken(ArcticSqlCommandParser.CAST, 0); }
		public TerminalNode TRY_CAST() { return getToken(ArcticSqlCommandParser.TRY_CAST, 0); }
		public CastContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCast(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCast(this);
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
		public TerminalNode ARROW() { return getToken(ArcticSqlCommandParser.ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public LambdaContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesizedExpressionContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public ParenthesizedExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TrimContext extends PrimaryExpressionContext {
		public Token trimOption;
		public ValueExpressionContext trimStr;
		public ValueExpressionContext srcStr;
		public TerminalNode TRIM() { return getToken(ArcticSqlCommandParser.TRIM, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode BOTH() { return getToken(ArcticSqlCommandParser.BOTH, 0); }
		public TerminalNode LEADING() { return getToken(ArcticSqlCommandParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(ArcticSqlCommandParser.TRAILING, 0); }
		public TrimContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTrim(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTrim(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTrim(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SimpleCaseContext extends PrimaryExpressionContext {
		public ExpressionContext value;
		public ExpressionContext elseExpression;
		public TerminalNode CASE() { return getToken(ArcticSqlCommandParser.CASE, 0); }
		public TerminalNode END() { return getToken(ArcticSqlCommandParser.END, 0); }
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
		public TerminalNode ELSE() { return getToken(ArcticSqlCommandParser.ELSE, 0); }
		public SimpleCaseContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSimpleCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSimpleCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSimpleCase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CurrentLikeContext extends PrimaryExpressionContext {
		public Token name;
		public TerminalNode CURRENT_DATE() { return getToken(ArcticSqlCommandParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(ArcticSqlCommandParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(ArcticSqlCommandParser.CURRENT_USER, 0); }
		public CurrentLikeContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterCurrentLike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitCurrentLike(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitCurrentLike(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterColumnReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitColumnReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitColumnReference(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RowConstructorContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public RowConstructorContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRowConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRowConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRowConstructor(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LastContext extends PrimaryExpressionContext {
		public TerminalNode LAST() { return getToken(ArcticSqlCommandParser.LAST, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticSqlCommandParser.IGNORE, 0); }
		public TerminalNode NULLS() { return getToken(ArcticSqlCommandParser.NULLS, 0); }
		public LastContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLast(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLast(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StarContext extends PrimaryExpressionContext {
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(ArcticSqlCommandParser.DOT, 0); }
		public StarContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterStar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitStar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitStar(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OverlayContext extends PrimaryExpressionContext {
		public ValueExpressionContext input;
		public ValueExpressionContext replace;
		public ValueExpressionContext position;
		public ValueExpressionContext length;
		public TerminalNode OVERLAY() { return getToken(ArcticSqlCommandParser.OVERLAY, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode PLACING() { return getToken(ArcticSqlCommandParser.PLACING, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public OverlayContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterOverlay(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitOverlay(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitOverlay(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubscriptContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext value;
		public ValueExpressionContext index;
		public TerminalNode LEFT_BRACKET() { return getToken(ArcticSqlCommandParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(ArcticSqlCommandParser.RIGHT_BRACKET, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public SubscriptContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TimestampdiffContext extends PrimaryExpressionContext {
		public Token name;
		public DatetimeUnitContext unit;
		public ValueExpressionContext startTimestamp;
		public ValueExpressionContext endTimestamp;
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public DatetimeUnitContext datetimeUnit() {
			return getRuleContext(DatetimeUnitContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode TIMESTAMPDIFF() { return getToken(ArcticSqlCommandParser.TIMESTAMPDIFF, 0); }
		public TerminalNode DATEDIFF() { return getToken(ArcticSqlCommandParser.DATEDIFF, 0); }
		public TimestampdiffContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTimestampdiff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTimestampdiff(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTimestampdiff(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubqueryExpressionContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public SubqueryExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSubqueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSubqueryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSubqueryExpression(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterConstantDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitConstantDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitConstantDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExtractContext extends PrimaryExpressionContext {
		public IdentifierContext field;
		public ValueExpressionContext source;
		public TerminalNode EXTRACT() { return getToken(ArcticSqlCommandParser.EXTRACT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public ExtractContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterExtract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitExtract(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitExtract(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PercentileContext extends PrimaryExpressionContext {
		public Token name;
		public ValueExpressionContext percentage;
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticSqlCommandParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticSqlCommandParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.RIGHT_PAREN, i);
		}
		public TerminalNode WITHIN() { return getToken(ArcticSqlCommandParser.WITHIN, 0); }
		public TerminalNode GROUP() { return getToken(ArcticSqlCommandParser.GROUP, 0); }
		public TerminalNode ORDER() { return getToken(ArcticSqlCommandParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public SortItemContext sortItem() {
			return getRuleContext(SortItemContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode PERCENTILE_CONT() { return getToken(ArcticSqlCommandParser.PERCENTILE_CONT, 0); }
		public TerminalNode PERCENTILE_DISC() { return getToken(ArcticSqlCommandParser.PERCENTILE_DISC, 0); }
		public TerminalNode OVER() { return getToken(ArcticSqlCommandParser.OVER, 0); }
		public WindowSpecContext windowSpec() {
			return getRuleContext(WindowSpecContext.class,0);
		}
		public PercentileContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPercentile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPercentile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPercentile(this);
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
		public List<TerminalNode> LEFT_PAREN() { return getTokens(ArcticSqlCommandParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(ArcticSqlCommandParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(ArcticSqlCommandParser.RIGHT_PAREN, i);
		}
		public TerminalNode FILTER() { return getToken(ArcticSqlCommandParser.FILTER, 0); }
		public TerminalNode WHERE() { return getToken(ArcticSqlCommandParser.WHERE, 0); }
		public TerminalNode NULLS() { return getToken(ArcticSqlCommandParser.NULLS, 0); }
		public TerminalNode OVER() { return getToken(ArcticSqlCommandParser.OVER, 0); }
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
		public TerminalNode IGNORE() { return getToken(ArcticSqlCommandParser.IGNORE, 0); }
		public TerminalNode RESPECT() { return getToken(ArcticSqlCommandParser.RESPECT, 0); }
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public FunctionCallContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SearchedCaseContext extends PrimaryExpressionContext {
		public ExpressionContext elseExpression;
		public TerminalNode CASE() { return getToken(ArcticSqlCommandParser.CASE, 0); }
		public TerminalNode END() { return getToken(ArcticSqlCommandParser.END, 0); }
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ArcticSqlCommandParser.ELSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SearchedCaseContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSearchedCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSearchedCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSearchedCase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PositionContext extends PrimaryExpressionContext {
		public ValueExpressionContext substr;
		public ValueExpressionContext str;
		public TerminalNode POSITION() { return getToken(ArcticSqlCommandParser.POSITION, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public PositionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPosition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FirstContext extends PrimaryExpressionContext {
		public TerminalNode FIRST() { return getToken(ArcticSqlCommandParser.FIRST, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticSqlCommandParser.IGNORE, 0); }
		public TerminalNode NULLS() { return getToken(ArcticSqlCommandParser.NULLS, 0); }
		public FirstContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFirst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFirst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFirst(this);
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
		int _startState = 222;
		enterRecursionRule(_localctx, 222, RULE_primaryExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2920);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,375,_ctx) ) {
			case 1:
				{
				_localctx = new CurrentLikeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2700);
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
				setState(2701);
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
				setState(2702);
				match(LEFT_PAREN);
				setState(2703);
				((TimestampaddContext)_localctx).unit = datetimeUnit();
				setState(2704);
				match(COMMA);
				setState(2705);
				((TimestampaddContext)_localctx).unitsAmount = valueExpression(0);
				setState(2706);
				match(COMMA);
				setState(2707);
				((TimestampaddContext)_localctx).timestamp = valueExpression(0);
				setState(2708);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new TimestampdiffContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2710);
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
				setState(2711);
				match(LEFT_PAREN);
				setState(2712);
				((TimestampdiffContext)_localctx).unit = datetimeUnit();
				setState(2713);
				match(COMMA);
				setState(2714);
				((TimestampdiffContext)_localctx).startTimestamp = valueExpression(0);
				setState(2715);
				match(COMMA);
				setState(2716);
				((TimestampdiffContext)_localctx).endTimestamp = valueExpression(0);
				setState(2717);
				match(RIGHT_PAREN);
				}
				break;
			case 4:
				{
				_localctx = new SearchedCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2719);
				match(CASE);
				setState(2721); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2720);
					whenClause();
					}
					}
					setState(2723); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2727);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2725);
					match(ELSE);
					setState(2726);
					((SearchedCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2729);
				match(END);
				}
				break;
			case 5:
				{
				_localctx = new SimpleCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2731);
				match(CASE);
				setState(2732);
				((SimpleCaseContext)_localctx).value = expression();
				setState(2734); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2733);
					whenClause();
					}
					}
					setState(2736); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2740);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2738);
					match(ELSE);
					setState(2739);
					((SimpleCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2742);
				match(END);
				}
				break;
			case 6:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2744);
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
				setState(2745);
				match(LEFT_PAREN);
				setState(2746);
				expression();
				setState(2747);
				match(AS);
				setState(2748);
				dataType();
				setState(2749);
				match(RIGHT_PAREN);
				}
				break;
			case 7:
				{
				_localctx = new StructContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2751);
				match(STRUCT);
				setState(2752);
				match(LEFT_PAREN);
				setState(2761);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,359,_ctx) ) {
				case 1:
					{
					setState(2753);
					((StructContext)_localctx).namedExpression = namedExpression();
					((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
					setState(2758);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2754);
						match(COMMA);
						setState(2755);
						((StructContext)_localctx).namedExpression = namedExpression();
						((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
						}
						}
						setState(2760);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2763);
				match(RIGHT_PAREN);
				}
				break;
			case 8:
				{
				_localctx = new FirstContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2764);
				match(FIRST);
				setState(2765);
				match(LEFT_PAREN);
				setState(2766);
				expression();
				setState(2769);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2767);
					match(IGNORE);
					setState(2768);
					match(NULLS);
					}
				}

				setState(2771);
				match(RIGHT_PAREN);
				}
				break;
			case 9:
				{
				_localctx = new LastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2773);
				match(LAST);
				setState(2774);
				match(LEFT_PAREN);
				setState(2775);
				expression();
				setState(2778);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2776);
					match(IGNORE);
					setState(2777);
					match(NULLS);
					}
				}

				setState(2780);
				match(RIGHT_PAREN);
				}
				break;
			case 10:
				{
				_localctx = new PositionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2782);
				match(POSITION);
				setState(2783);
				match(LEFT_PAREN);
				setState(2784);
				((PositionContext)_localctx).substr = valueExpression(0);
				setState(2785);
				match(IN);
				setState(2786);
				((PositionContext)_localctx).str = valueExpression(0);
				setState(2787);
				match(RIGHT_PAREN);
				}
				break;
			case 11:
				{
				_localctx = new ConstantDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2789);
				constant();
				}
				break;
			case 12:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2790);
				match(ASTERISK);
				}
				break;
			case 13:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2791);
				qualifiedName();
				setState(2792);
				match(DOT);
				setState(2793);
				match(ASTERISK);
				}
				break;
			case 14:
				{
				_localctx = new RowConstructorContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2795);
				match(LEFT_PAREN);
				setState(2796);
				namedExpression();
				setState(2799); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2797);
					match(COMMA);
					setState(2798);
					namedExpression();
					}
					}
					setState(2801); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(2803);
				match(RIGHT_PAREN);
				}
				break;
			case 15:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2805);
				match(LEFT_PAREN);
				setState(2806);
				query();
				setState(2807);
				match(RIGHT_PAREN);
				}
				break;
			case 16:
				{
				_localctx = new FunctionCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2809);
				functionName();
				setState(2810);
				match(LEFT_PAREN);
				setState(2822);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,365,_ctx) ) {
				case 1:
					{
					setState(2812);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,363,_ctx) ) {
					case 1:
						{
						setState(2811);
						setQuantifier();
						}
						break;
					}
					setState(2814);
					((FunctionCallContext)_localctx).expression = expression();
					((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
					setState(2819);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2815);
						match(COMMA);
						setState(2816);
						((FunctionCallContext)_localctx).expression = expression();
						((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
						}
						}
						setState(2821);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2824);
				match(RIGHT_PAREN);
				setState(2831);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,366,_ctx) ) {
				case 1:
					{
					setState(2825);
					match(FILTER);
					setState(2826);
					match(LEFT_PAREN);
					setState(2827);
					match(WHERE);
					setState(2828);
					((FunctionCallContext)_localctx).where = booleanExpression(0);
					setState(2829);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(2835);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,367,_ctx) ) {
				case 1:
					{
					setState(2833);
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
					setState(2834);
					match(NULLS);
					}
					break;
				}
				setState(2839);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,368,_ctx) ) {
				case 1:
					{
					setState(2837);
					match(OVER);
					setState(2838);
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
				setState(2841);
				identifier();
				setState(2842);
				match(ARROW);
				setState(2843);
				expression();
				}
				break;
			case 18:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2845);
				match(LEFT_PAREN);
				setState(2846);
				identifier();
				setState(2849); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2847);
					match(COMMA);
					setState(2848);
					identifier();
					}
					}
					setState(2851); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(2853);
				match(RIGHT_PAREN);
				setState(2854);
				match(ARROW);
				setState(2855);
				expression();
				}
				break;
			case 19:
				{
				_localctx = new ColumnReferenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2857);
				identifier();
				}
				break;
			case 20:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2858);
				match(LEFT_PAREN);
				setState(2859);
				expression();
				setState(2860);
				match(RIGHT_PAREN);
				}
				break;
			case 21:
				{
				_localctx = new ExtractContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2862);
				match(EXTRACT);
				setState(2863);
				match(LEFT_PAREN);
				setState(2864);
				((ExtractContext)_localctx).field = identifier();
				setState(2865);
				match(FROM);
				setState(2866);
				((ExtractContext)_localctx).source = valueExpression(0);
				setState(2867);
				match(RIGHT_PAREN);
				}
				break;
			case 22:
				{
				_localctx = new SubstringContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2869);
				_la = _input.LA(1);
				if ( !(_la==SUBSTR || _la==SUBSTRING) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2870);
				match(LEFT_PAREN);
				setState(2871);
				((SubstringContext)_localctx).str = valueExpression(0);
				setState(2872);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==FROM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2873);
				((SubstringContext)_localctx).pos = valueExpression(0);
				setState(2876);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA || _la==FOR) {
					{
					setState(2874);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==FOR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2875);
					((SubstringContext)_localctx).len = valueExpression(0);
					}
				}

				setState(2878);
				match(RIGHT_PAREN);
				}
				break;
			case 23:
				{
				_localctx = new TrimContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2880);
				match(TRIM);
				setState(2881);
				match(LEFT_PAREN);
				setState(2883);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,371,_ctx) ) {
				case 1:
					{
					setState(2882);
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
				setState(2886);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,372,_ctx) ) {
				case 1:
					{
					setState(2885);
					((TrimContext)_localctx).trimStr = valueExpression(0);
					}
					break;
				}
				setState(2888);
				match(FROM);
				setState(2889);
				((TrimContext)_localctx).srcStr = valueExpression(0);
				setState(2890);
				match(RIGHT_PAREN);
				}
				break;
			case 24:
				{
				_localctx = new OverlayContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2892);
				match(OVERLAY);
				setState(2893);
				match(LEFT_PAREN);
				setState(2894);
				((OverlayContext)_localctx).input = valueExpression(0);
				setState(2895);
				match(PLACING);
				setState(2896);
				((OverlayContext)_localctx).replace = valueExpression(0);
				setState(2897);
				match(FROM);
				setState(2898);
				((OverlayContext)_localctx).position = valueExpression(0);
				setState(2901);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2899);
					match(FOR);
					setState(2900);
					((OverlayContext)_localctx).length = valueExpression(0);
					}
				}

				setState(2903);
				match(RIGHT_PAREN);
				}
				break;
			case 25:
				{
				_localctx = new PercentileContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2905);
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
				setState(2906);
				match(LEFT_PAREN);
				setState(2907);
				((PercentileContext)_localctx).percentage = valueExpression(0);
				setState(2908);
				match(RIGHT_PAREN);
				setState(2909);
				match(WITHIN);
				setState(2910);
				match(GROUP);
				setState(2911);
				match(LEFT_PAREN);
				setState(2912);
				match(ORDER);
				setState(2913);
				match(BY);
				setState(2914);
				sortItem();
				setState(2915);
				match(RIGHT_PAREN);
				setState(2918);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,374,_ctx) ) {
				case 1:
					{
					setState(2916);
					match(OVER);
					setState(2917);
					windowSpec();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2932);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,377,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2930);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,376,_ctx) ) {
					case 1:
						{
						_localctx = new SubscriptContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((SubscriptContext)_localctx).value = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2922);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(2923);
						match(LEFT_BRACKET);
						setState(2924);
						((SubscriptContext)_localctx).index = valueExpression(0);
						setState(2925);
						match(RIGHT_BRACKET);
						}
						break;
					case 2:
						{
						_localctx = new DereferenceContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((DereferenceContext)_localctx).base = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2927);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(2928);
						match(DOT);
						setState(2929);
						((DereferenceContext)_localctx).fieldName = identifier();
						}
						break;
					}
					} 
				}
				setState(2934);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,377,_ctx);
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
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public NullLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNullLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNullLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNullLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringLiteralContext extends ConstantContext {
		public List<TerminalNode> STRING() { return getTokens(ArcticSqlCommandParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(ArcticSqlCommandParser.STRING, i);
		}
		public StringLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TypeConstructorContext extends ConstantContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TypeConstructorContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTypeConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTypeConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTypeConstructor(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIntervalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIntervalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIntervalLiteral(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNumericLiteral(this);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_constant);
		try {
			int _alt;
			setState(2947);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,379,_ctx) ) {
			case 1:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2935);
				match(NULL);
				}
				break;
			case 2:
				_localctx = new IntervalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2936);
				interval();
				}
				break;
			case 3:
				_localctx = new TypeConstructorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2937);
				identifier();
				setState(2938);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new NumericLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2940);
				number();
				}
				break;
			case 5:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2941);
				booleanValue();
				}
				break;
			case 6:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2943); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2942);
						match(STRING);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2945); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,378,_ctx);
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
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(ArcticSqlCommandParser.NEQ, 0); }
		public TerminalNode NEQJ() { return getToken(ArcticSqlCommandParser.NEQJ, 0); }
		public TerminalNode LT() { return getToken(ArcticSqlCommandParser.LT, 0); }
		public TerminalNode LTE() { return getToken(ArcticSqlCommandParser.LTE, 0); }
		public TerminalNode GT() { return getToken(ArcticSqlCommandParser.GT, 0); }
		public TerminalNode GTE() { return getToken(ArcticSqlCommandParser.GTE, 0); }
		public TerminalNode NSEQ() { return getToken(ArcticSqlCommandParser.NSEQ, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2949);
			_la = _input.LA(1);
			if ( !(((((_la - 290)) & ~0x3f) == 0 && ((1L << (_la - 290)) & ((1L << (EQ - 290)) | (1L << (NSEQ - 290)) | (1L << (NEQ - 290)) | (1L << (NEQJ - 290)) | (1L << (LT - 290)) | (1L << (LTE - 290)) | (1L << (GT - 290)) | (1L << (GTE - 290)))) != 0)) ) {
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
		public TerminalNode PLUS() { return getToken(ArcticSqlCommandParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(ArcticSqlCommandParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(ArcticSqlCommandParser.PERCENT, 0); }
		public TerminalNode DIV() { return getToken(ArcticSqlCommandParser.DIV, 0); }
		public TerminalNode TILDE() { return getToken(ArcticSqlCommandParser.TILDE, 0); }
		public TerminalNode AMPERSAND() { return getToken(ArcticSqlCommandParser.AMPERSAND, 0); }
		public TerminalNode PIPE() { return getToken(ArcticSqlCommandParser.PIPE, 0); }
		public TerminalNode CONCAT_PIPE() { return getToken(ArcticSqlCommandParser.CONCAT_PIPE, 0); }
		public TerminalNode HAT() { return getToken(ArcticSqlCommandParser.HAT, 0); }
		public ArithmeticOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arithmeticOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterArithmeticOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitArithmeticOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitArithmeticOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArithmeticOperatorContext arithmeticOperator() throws RecognitionException {
		ArithmeticOperatorContext _localctx = new ArithmeticOperatorContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_arithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2951);
			_la = _input.LA(1);
			if ( !(_la==DIV || ((((_la - 298)) & ~0x3f) == 0 && ((1L << (_la - 298)) & ((1L << (PLUS - 298)) | (1L << (MINUS - 298)) | (1L << (ASTERISK - 298)) | (1L << (SLASH - 298)) | (1L << (PERCENT - 298)) | (1L << (TILDE - 298)) | (1L << (AMPERSAND - 298)) | (1L << (PIPE - 298)) | (1L << (CONCAT_PIPE - 298)) | (1L << (HAT - 298)))) != 0)) ) {
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
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public PredicateOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPredicateOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPredicateOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPredicateOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateOperatorContext predicateOperator() throws RecognitionException {
		PredicateOperatorContext _localctx = new PredicateOperatorContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_predicateOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2953);
			_la = _input.LA(1);
			if ( !(_la==AND || ((((_la - 118)) & ~0x3f) == 0 && ((1L << (_la - 118)) & ((1L << (IN - 118)) | (1L << (NOT - 118)) | (1L << (OR - 118)))) != 0)) ) {
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
		public TerminalNode TRUE() { return getToken(ArcticSqlCommandParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ArcticSqlCommandParser.FALSE, 0); }
		public BooleanValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterBooleanValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitBooleanValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitBooleanValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanValueContext booleanValue() throws RecognitionException {
		BooleanValueContext _localctx = new BooleanValueContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_booleanValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2955);
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
		public TerminalNode INTERVAL() { return getToken(ArcticSqlCommandParser.INTERVAL, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2957);
			match(INTERVAL);
			setState(2960);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,380,_ctx) ) {
			case 1:
				{
				setState(2958);
				errorCapturingMultiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2959);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterErrorCapturingMultiUnitsInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitErrorCapturingMultiUnitsInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitErrorCapturingMultiUnitsInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingMultiUnitsIntervalContext errorCapturingMultiUnitsInterval() throws RecognitionException {
		ErrorCapturingMultiUnitsIntervalContext _localctx = new ErrorCapturingMultiUnitsIntervalContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_errorCapturingMultiUnitsInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2962);
			((ErrorCapturingMultiUnitsIntervalContext)_localctx).body = multiUnitsInterval();
			setState(2964);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,381,_ctx) ) {
			case 1:
				{
				setState(2963);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterMultiUnitsInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitMultiUnitsInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitMultiUnitsInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiUnitsIntervalContext multiUnitsInterval() throws RecognitionException {
		MultiUnitsIntervalContext _localctx = new MultiUnitsIntervalContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_multiUnitsInterval);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2969); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2966);
					intervalValue();
					setState(2967);
					((MultiUnitsIntervalContext)_localctx).identifier = identifier();
					((MultiUnitsIntervalContext)_localctx).unit.add(((MultiUnitsIntervalContext)_localctx).identifier);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2971); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,382,_ctx);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterErrorCapturingUnitToUnitInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitErrorCapturingUnitToUnitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitErrorCapturingUnitToUnitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingUnitToUnitIntervalContext errorCapturingUnitToUnitInterval() throws RecognitionException {
		ErrorCapturingUnitToUnitIntervalContext _localctx = new ErrorCapturingUnitToUnitIntervalContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_errorCapturingUnitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2973);
			((ErrorCapturingUnitToUnitIntervalContext)_localctx).body = unitToUnitInterval();
			setState(2976);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,383,_ctx) ) {
			case 1:
				{
				setState(2974);
				((ErrorCapturingUnitToUnitIntervalContext)_localctx).error1 = multiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2975);
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
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUnitToUnitInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUnitToUnitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUnitToUnitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnitToUnitIntervalContext unitToUnitInterval() throws RecognitionException {
		UnitToUnitIntervalContext _localctx = new UnitToUnitIntervalContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_unitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2978);
			((UnitToUnitIntervalContext)_localctx).value = intervalValue();
			setState(2979);
			((UnitToUnitIntervalContext)_localctx).from = identifier();
			setState(2980);
			match(TO);
			setState(2981);
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
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticSqlCommandParser.DECIMAL_VALUE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode PLUS() { return getToken(ArcticSqlCommandParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public IntervalValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIntervalValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIntervalValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIntervalValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalValueContext intervalValue() throws RecognitionException {
		IntervalValueContext _localctx = new IntervalValueContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_intervalValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2984);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(2983);
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

			setState(2986);
			_la = _input.LA(1);
			if ( !(((((_la - 312)) & ~0x3f) == 0 && ((1L << (_la - 312)) & ((1L << (STRING - 312)) | (1L << (INTEGER_VALUE - 312)) | (1L << (DECIMAL_VALUE - 312)))) != 0)) ) {
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
		public TerminalNode FIRST() { return getToken(ArcticSqlCommandParser.FIRST, 0); }
		public TerminalNode AFTER() { return getToken(ArcticSqlCommandParser.AFTER, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public ColPositionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colPosition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterColPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitColPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitColPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColPositionContext colPosition() throws RecognitionException {
		ColPositionContext _localctx = new ColPositionContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_colPosition);
		try {
			setState(2991);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				enterOuterAlt(_localctx, 1);
				{
				setState(2988);
				((ColPositionContext)_localctx).position = match(FIRST);
				}
				break;
			case AFTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2989);
				((ColPositionContext)_localctx).position = match(AFTER);
				setState(2990);
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
		public TerminalNode LT() { return getToken(ArcticSqlCommandParser.LT, 0); }
		public List<DataTypeContext> dataType() {
			return getRuleContexts(DataTypeContext.class);
		}
		public DataTypeContext dataType(int i) {
			return getRuleContext(DataTypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(ArcticSqlCommandParser.GT, 0); }
		public TerminalNode ARRAY() { return getToken(ArcticSqlCommandParser.ARRAY, 0); }
		public TerminalNode COMMA() { return getToken(ArcticSqlCommandParser.COMMA, 0); }
		public TerminalNode MAP() { return getToken(ArcticSqlCommandParser.MAP, 0); }
		public TerminalNode STRUCT() { return getToken(ArcticSqlCommandParser.STRUCT, 0); }
		public TerminalNode NEQ() { return getToken(ArcticSqlCommandParser.NEQ, 0); }
		public ComplexColTypeListContext complexColTypeList() {
			return getRuleContext(ComplexColTypeListContext.class,0);
		}
		public ComplexDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterComplexDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitComplexDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitComplexDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YearMonthIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(ArcticSqlCommandParser.INTERVAL, 0); }
		public TerminalNode YEAR() { return getToken(ArcticSqlCommandParser.YEAR, 0); }
		public List<TerminalNode> MONTH() { return getTokens(ArcticSqlCommandParser.MONTH); }
		public TerminalNode MONTH(int i) {
			return getToken(ArcticSqlCommandParser.MONTH, i);
		}
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
		public YearMonthIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterYearMonthIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitYearMonthIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitYearMonthIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DayTimeIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(ArcticSqlCommandParser.INTERVAL, 0); }
		public TerminalNode DAY() { return getToken(ArcticSqlCommandParser.DAY, 0); }
		public List<TerminalNode> HOUR() { return getTokens(ArcticSqlCommandParser.HOUR); }
		public TerminalNode HOUR(int i) {
			return getToken(ArcticSqlCommandParser.HOUR, i);
		}
		public List<TerminalNode> MINUTE() { return getTokens(ArcticSqlCommandParser.MINUTE); }
		public TerminalNode MINUTE(int i) {
			return getToken(ArcticSqlCommandParser.MINUTE, i);
		}
		public List<TerminalNode> SECOND() { return getTokens(ArcticSqlCommandParser.SECOND); }
		public TerminalNode SECOND(int i) {
			return getToken(ArcticSqlCommandParser.SECOND, i);
		}
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
		public DayTimeIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDayTimeIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDayTimeIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDayTimeIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimitiveDataTypeContext extends DataTypeContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(ArcticSqlCommandParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(ArcticSqlCommandParser.INTEGER_VALUE, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public PrimitiveDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterPrimitiveDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitPrimitiveDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitPrimitiveDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_dataType);
		int _la;
		try {
			setState(3039);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,392,_ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2993);
				((ComplexDataTypeContext)_localctx).complex = match(ARRAY);
				setState(2994);
				match(LT);
				setState(2995);
				dataType();
				setState(2996);
				match(GT);
				}
				break;
			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2998);
				((ComplexDataTypeContext)_localctx).complex = match(MAP);
				setState(2999);
				match(LT);
				setState(3000);
				dataType();
				setState(3001);
				match(COMMA);
				setState(3002);
				dataType();
				setState(3003);
				match(GT);
				}
				break;
			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3005);
				((ComplexDataTypeContext)_localctx).complex = match(STRUCT);
				setState(3012);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LT:
					{
					setState(3006);
					match(LT);
					setState(3008);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,386,_ctx) ) {
					case 1:
						{
						setState(3007);
						complexColTypeList();
						}
						break;
					}
					setState(3010);
					match(GT);
					}
					break;
				case NEQ:
					{
					setState(3011);
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
				setState(3014);
				match(INTERVAL);
				setState(3015);
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
				setState(3018);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,388,_ctx) ) {
				case 1:
					{
					setState(3016);
					match(TO);
					setState(3017);
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
				setState(3020);
				match(INTERVAL);
				setState(3021);
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
				setState(3024);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,389,_ctx) ) {
				case 1:
					{
					setState(3022);
					match(TO);
					setState(3023);
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
				setState(3026);
				identifier();
				setState(3037);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,391,_ctx) ) {
				case 1:
					{
					setState(3027);
					match(LEFT_PAREN);
					setState(3028);
					match(INTEGER_VALUE);
					setState(3033);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(3029);
						match(COMMA);
						setState(3030);
						match(INTEGER_VALUE);
						}
						}
						setState(3035);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(3036);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public QualifiedColTypeWithPositionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedColTypeWithPositionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQualifiedColTypeWithPositionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQualifiedColTypeWithPositionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQualifiedColTypeWithPositionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() throws RecognitionException {
		QualifiedColTypeWithPositionListContext _localctx = new QualifiedColTypeWithPositionListContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_qualifiedColTypeWithPositionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3041);
			qualifiedColTypeWithPosition();
			setState(3046);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3042);
				match(COMMA);
				setState(3043);
				qualifiedColTypeWithPosition();
				}
				}
				setState(3048);
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
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQualifiedColTypeWithPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQualifiedColTypeWithPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQualifiedColTypeWithPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedColTypeWithPositionContext qualifiedColTypeWithPosition() throws RecognitionException {
		QualifiedColTypeWithPositionContext _localctx = new QualifiedColTypeWithPositionContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_qualifiedColTypeWithPosition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3049);
			((QualifiedColTypeWithPositionContext)_localctx).name = multipartIdentifier();
			setState(3050);
			dataType();
			setState(3053);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(3051);
				match(NOT);
				setState(3052);
				match(NULL);
				}
			}

			setState(3056);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(3055);
				commentSpec();
				}
			}

			setState(3059);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AFTER || _la==FIRST) {
				{
				setState(3058);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public ColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeListContext colTypeList() throws RecognitionException {
		ColTypeListContext _localctx = new ColTypeListContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_colTypeList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3061);
			colType();
			setState(3066);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,397,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3062);
					match(COMMA);
					setState(3063);
					colType();
					}
					} 
				}
				setState(3068);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,397,_ctx);
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
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeContext colType() throws RecognitionException {
		ColTypeContext _localctx = new ColTypeContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_colType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3069);
			((ColTypeContext)_localctx).colName = errorCapturingIdentifier();
			setState(3070);
			dataType();
			setState(3073);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,398,_ctx) ) {
			case 1:
				{
				setState(3071);
				match(NOT);
				setState(3072);
				match(NULL);
				}
				break;
			}
			setState(3076);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,399,_ctx) ) {
			case 1:
				{
				setState(3075);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public ComplexColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterComplexColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitComplexColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitComplexColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeListContext complexColTypeList() throws RecognitionException {
		ComplexColTypeListContext _localctx = new ComplexColTypeListContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_complexColTypeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3078);
			complexColType();
			setState(3083);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3079);
				match(COMMA);
				setState(3080);
				complexColType();
				}
				}
				setState(3085);
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
		public TerminalNode COLON() { return getToken(ArcticSqlCommandParser.COLON, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ComplexColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterComplexColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitComplexColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitComplexColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeContext complexColType() throws RecognitionException {
		ComplexColTypeContext _localctx = new ComplexColTypeContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_complexColType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3086);
			identifier();
			setState(3088);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,401,_ctx) ) {
			case 1:
				{
				setState(3087);
				match(COLON);
				}
				break;
			}
			setState(3090);
			dataType();
			setState(3093);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(3091);
				match(NOT);
				setState(3092);
				match(NULL);
				}
			}

			setState(3096);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(3095);
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
		public TerminalNode WHEN() { return getToken(ArcticSqlCommandParser.WHEN, 0); }
		public TerminalNode THEN() { return getToken(ArcticSqlCommandParser.THEN, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterWhenClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitWhenClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitWhenClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3098);
			match(WHEN);
			setState(3099);
			((WhenClauseContext)_localctx).condition = expression();
			setState(3100);
			match(THEN);
			setState(3101);
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
		public TerminalNode WINDOW() { return getToken(ArcticSqlCommandParser.WINDOW, 0); }
		public List<NamedWindowContext> namedWindow() {
			return getRuleContexts(NamedWindowContext.class);
		}
		public NamedWindowContext namedWindow(int i) {
			return getRuleContext(NamedWindowContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public WindowClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterWindowClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitWindowClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitWindowClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowClauseContext windowClause() throws RecognitionException {
		WindowClauseContext _localctx = new WindowClauseContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_windowClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3103);
			match(WINDOW);
			setState(3104);
			namedWindow();
			setState(3109);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,404,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3105);
					match(COMMA);
					setState(3106);
					namedWindow();
					}
					} 
				}
				setState(3111);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,404,_ctx);
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
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNamedWindow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNamedWindow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNamedWindow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedWindowContext namedWindow() throws RecognitionException {
		NamedWindowContext _localctx = new NamedWindowContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_namedWindow);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3112);
			((NamedWindowContext)_localctx).name = errorCapturingIdentifier();
			setState(3113);
			match(AS);
			setState(3114);
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
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public WindowRefContext(WindowSpecContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterWindowRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitWindowRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitWindowRef(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WindowDefContext extends WindowSpecContext {
		public ExpressionContext expression;
		public List<ExpressionContext> partition = new ArrayList<ExpressionContext>();
		public TerminalNode LEFT_PAREN() { return getToken(ArcticSqlCommandParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(ArcticSqlCommandParser.RIGHT_PAREN, 0); }
		public TerminalNode CLUSTER() { return getToken(ArcticSqlCommandParser.CLUSTER, 0); }
		public List<TerminalNode> BY() { return getTokens(ArcticSqlCommandParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(ArcticSqlCommandParser.BY, i);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public List<SortItemContext> sortItem() {
			return getRuleContexts(SortItemContext.class);
		}
		public SortItemContext sortItem(int i) {
			return getRuleContext(SortItemContext.class,i);
		}
		public TerminalNode PARTITION() { return getToken(ArcticSqlCommandParser.PARTITION, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticSqlCommandParser.DISTRIBUTE, 0); }
		public TerminalNode ORDER() { return getToken(ArcticSqlCommandParser.ORDER, 0); }
		public TerminalNode SORT() { return getToken(ArcticSqlCommandParser.SORT, 0); }
		public WindowDefContext(WindowSpecContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterWindowDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitWindowDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitWindowDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowSpecContext windowSpec() throws RecognitionException {
		WindowSpecContext _localctx = new WindowSpecContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_windowSpec);
		int _la;
		try {
			setState(3162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,412,_ctx) ) {
			case 1:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3116);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				}
				break;
			case 2:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3117);
				match(LEFT_PAREN);
				setState(3118);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				setState(3119);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				_localctx = new WindowDefContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3121);
				match(LEFT_PAREN);
				setState(3156);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLUSTER:
					{
					setState(3122);
					match(CLUSTER);
					setState(3123);
					match(BY);
					setState(3124);
					((WindowDefContext)_localctx).expression = expression();
					((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
					setState(3129);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(3125);
						match(COMMA);
						setState(3126);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						}
						}
						setState(3131);
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
					setState(3142);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DISTRIBUTE || _la==PARTITION) {
						{
						setState(3132);
						_la = _input.LA(1);
						if ( !(_la==DISTRIBUTE || _la==PARTITION) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3133);
						match(BY);
						setState(3134);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						setState(3139);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==COMMA) {
							{
							{
							setState(3135);
							match(COMMA);
							setState(3136);
							((WindowDefContext)_localctx).expression = expression();
							((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
							}
							}
							setState(3141);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
					}

					setState(3154);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ORDER || _la==SORT) {
						{
						setState(3144);
						_la = _input.LA(1);
						if ( !(_la==ORDER || _la==SORT) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3145);
						match(BY);
						setState(3146);
						sortItem();
						setState(3151);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==COMMA) {
							{
							{
							setState(3147);
							match(COMMA);
							setState(3148);
							sortItem();
							}
							}
							setState(3153);
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
				setState(3159);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==RANGE || _la==ROWS) {
					{
					setState(3158);
					windowFrame();
					}
				}

				setState(3161);
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
		public TerminalNode RANGE() { return getToken(ArcticSqlCommandParser.RANGE, 0); }
		public List<FrameBoundContext> frameBound() {
			return getRuleContexts(FrameBoundContext.class);
		}
		public FrameBoundContext frameBound(int i) {
			return getRuleContext(FrameBoundContext.class,i);
		}
		public TerminalNode ROWS() { return getToken(ArcticSqlCommandParser.ROWS, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticSqlCommandParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public WindowFrameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrame; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterWindowFrame(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitWindowFrame(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitWindowFrame(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameContext windowFrame() throws RecognitionException {
		WindowFrameContext _localctx = new WindowFrameContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_windowFrame);
		try {
			setState(3180);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,413,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3164);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3165);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3166);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3167);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3168);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3169);
				match(BETWEEN);
				setState(3170);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3171);
				match(AND);
				setState(3172);
				((WindowFrameContext)_localctx).end = frameBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3174);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3175);
				match(BETWEEN);
				setState(3176);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3177);
				match(AND);
				setState(3178);
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
		public TerminalNode UNBOUNDED() { return getToken(ArcticSqlCommandParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticSqlCommandParser.PRECEDING, 0); }
		public TerminalNode FOLLOWING() { return getToken(ArcticSqlCommandParser.FOLLOWING, 0); }
		public TerminalNode ROW() { return getToken(ArcticSqlCommandParser.ROW, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticSqlCommandParser.CURRENT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FrameBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFrameBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFrameBound(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFrameBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameBoundContext frameBound() throws RecognitionException {
		FrameBoundContext _localctx = new FrameBoundContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_frameBound);
		int _la;
		try {
			setState(3189);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,414,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3182);
				match(UNBOUNDED);
				setState(3183);
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
				setState(3184);
				((FrameBoundContext)_localctx).boundType = match(CURRENT);
				setState(3185);
				match(ROW);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3186);
				expression();
				setState(3187);
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
		public List<TerminalNode> COMMA() { return getTokens(ArcticSqlCommandParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArcticSqlCommandParser.COMMA, i);
		}
		public QualifiedNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQualifiedNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQualifiedNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQualifiedNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameListContext qualifiedNameList() throws RecognitionException {
		QualifiedNameListContext _localctx = new QualifiedNameListContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3191);
			qualifiedName();
			setState(3196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3192);
				match(COMMA);
				setState(3193);
				qualifiedName();
				}
				}
				setState(3198);
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
		public TerminalNode FILTER() { return getToken(ArcticSqlCommandParser.FILTER, 0); }
		public TerminalNode LEFT() { return getToken(ArcticSqlCommandParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(ArcticSqlCommandParser.RIGHT, 0); }
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_functionName);
		try {
			setState(3203);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,416,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3199);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3200);
				match(FILTER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3201);
				match(LEFT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3202);
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
		public List<TerminalNode> DOT() { return getTokens(ArcticSqlCommandParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ArcticSqlCommandParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3205);
			identifier();
			setState(3210);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,417,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3206);
					match(DOT);
					setState(3207);
					identifier();
					}
					} 
				}
				setState(3212);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,417,_ctx);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterErrorCapturingIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitErrorCapturingIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitErrorCapturingIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierContext errorCapturingIdentifier() throws RecognitionException {
		ErrorCapturingIdentifierContext _localctx = new ErrorCapturingIdentifierContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_errorCapturingIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3213);
			identifier();
			setState(3214);
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
		public List<TerminalNode> MINUS() { return getTokens(ArcticSqlCommandParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(ArcticSqlCommandParser.MINUS, i);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterErrorIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitErrorIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitErrorIdent(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RealIdentContext extends ErrorCapturingIdentifierExtraContext {
		public RealIdentContext(ErrorCapturingIdentifierExtraContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterRealIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitRealIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitRealIdent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierExtraContext errorCapturingIdentifierExtra() throws RecognitionException {
		ErrorCapturingIdentifierExtraContext _localctx = new ErrorCapturingIdentifierExtraContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_errorCapturingIdentifierExtra);
		try {
			int _alt;
			setState(3223);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,419,_ctx) ) {
			case 1:
				_localctx = new ErrorIdentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3218); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(3216);
						match(MINUS);
						setState(3217);
						identifier();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(3220); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,418,_ctx);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_identifier);
		try {
			setState(3228);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,420,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3225);
				strictIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3226);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3227);
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
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQuotedIdentifierAlternative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQuotedIdentifierAlternative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQuotedIdentifierAlternative(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnquotedIdentifierContext extends StrictIdentifierContext {
		public TerminalNode IDENTIFIER() { return getToken(ArcticSqlCommandParser.IDENTIFIER, 0); }
		public AnsiNonReservedContext ansiNonReserved() {
			return getRuleContext(AnsiNonReservedContext.class,0);
		}
		public NonReservedContext nonReserved() {
			return getRuleContext(NonReservedContext.class,0);
		}
		public UnquotedIdentifierContext(StrictIdentifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterUnquotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitUnquotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitUnquotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictIdentifierContext strictIdentifier() throws RecognitionException {
		StrictIdentifierContext _localctx = new StrictIdentifierContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_strictIdentifier);
		try {
			setState(3236);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,421,_ctx) ) {
			case 1:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3230);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3231);
				quotedIdentifier();
				}
				break;
			case 3:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3232);
				if (!(SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "SQL_standard_keyword_behavior");
				setState(3233);
				ansiNonReserved();
				}
				break;
			case 4:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3234);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3235);
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
		public TerminalNode BACKQUOTED_IDENTIFIER() { return getToken(ArcticSqlCommandParser.BACKQUOTED_IDENTIFIER, 0); }
		public QuotedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterQuotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitQuotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitQuotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuotedIdentifierContext quotedIdentifier() throws RecognitionException {
		QuotedIdentifierContext _localctx = new QuotedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_quotedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3238);
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
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticSqlCommandParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public DecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BigIntLiteralContext extends NumberContext {
		public TerminalNode BIGINT_LITERAL() { return getToken(ArcticSqlCommandParser.BIGINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public BigIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterBigIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitBigIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitBigIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TinyIntLiteralContext extends NumberContext {
		public TerminalNode TINYINT_LITERAL() { return getToken(ArcticSqlCommandParser.TINYINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public TinyIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTinyIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTinyIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTinyIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LegacyDecimalLiteralContext extends NumberContext {
		public TerminalNode EXPONENT_VALUE() { return getToken(ArcticSqlCommandParser.EXPONENT_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticSqlCommandParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public LegacyDecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterLegacyDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitLegacyDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitLegacyDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BigDecimalLiteralContext extends NumberContext {
		public TerminalNode BIGDECIMAL_LITERAL() { return getToken(ArcticSqlCommandParser.BIGDECIMAL_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public BigDecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterBigDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitBigDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitBigDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExponentLiteralContext extends NumberContext {
		public TerminalNode EXPONENT_VALUE() { return getToken(ArcticSqlCommandParser.EXPONENT_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public ExponentLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterExponentLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitExponentLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitExponentLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DoubleLiteralContext extends NumberContext {
		public TerminalNode DOUBLE_LITERAL() { return getToken(ArcticSqlCommandParser.DOUBLE_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public DoubleLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterDoubleLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitDoubleLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitDoubleLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IntegerLiteralContext extends NumberContext {
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public IntegerLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FloatLiteralContext extends NumberContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(ArcticSqlCommandParser.FLOAT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public FloatLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitFloatLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SmallIntLiteralContext extends NumberContext {
		public TerminalNode SMALLINT_LITERAL() { return getToken(ArcticSqlCommandParser.SMALLINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(ArcticSqlCommandParser.MINUS, 0); }
		public SmallIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterSmallIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitSmallIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitSmallIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_number);
		int _la;
		try {
			setState(3283);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,432,_ctx) ) {
			case 1:
				_localctx = new ExponentLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3240);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3242);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3241);
					match(MINUS);
					}
				}

				setState(3244);
				match(EXPONENT_VALUE);
				}
				break;
			case 2:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3245);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3247);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3246);
					match(MINUS);
					}
				}

				setState(3249);
				match(DECIMAL_VALUE);
				}
				break;
			case 3:
				_localctx = new LegacyDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3250);
				if (!(legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "legacy_exponent_literal_as_decimal_enabled");
				setState(3252);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3251);
					match(MINUS);
					}
				}

				setState(3254);
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
				setState(3256);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3255);
					match(MINUS);
					}
				}

				setState(3258);
				match(INTEGER_VALUE);
				}
				break;
			case 5:
				_localctx = new BigIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(3260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3259);
					match(MINUS);
					}
				}

				setState(3262);
				match(BIGINT_LITERAL);
				}
				break;
			case 6:
				_localctx = new SmallIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(3264);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3263);
					match(MINUS);
					}
				}

				setState(3266);
				match(SMALLINT_LITERAL);
				}
				break;
			case 7:
				_localctx = new TinyIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(3268);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3267);
					match(MINUS);
					}
				}

				setState(3270);
				match(TINYINT_LITERAL);
				}
				break;
			case 8:
				_localctx = new DoubleLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(3272);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3271);
					match(MINUS);
					}
				}

				setState(3274);
				match(DOUBLE_LITERAL);
				}
				break;
			case 9:
				_localctx = new FloatLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(3276);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3275);
					match(MINUS);
					}
				}

				setState(3278);
				match(FLOAT_LITERAL);
				}
				break;
			case 10:
				_localctx = new BigDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(3280);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3279);
					match(MINUS);
					}
				}

				setState(3282);
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
		public TerminalNode TYPE() { return getToken(ArcticSqlCommandParser.TYPE, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public AlterColumnActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterColumnAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAlterColumnAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAlterColumnAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAlterColumnAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterColumnActionContext alterColumnAction() throws RecognitionException {
		AlterColumnActionContext _localctx = new AlterColumnActionContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_alterColumnAction);
		int _la;
		try {
			setState(3292);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(3285);
				match(TYPE);
				setState(3286);
				dataType();
				}
				break;
			case COMMENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(3287);
				commentSpec();
				}
				break;
			case AFTER:
			case FIRST:
				enterOuterAlt(_localctx, 3);
				{
				setState(3288);
				colPosition();
				}
				break;
			case DROP:
			case SET:
				enterOuterAlt(_localctx, 4);
				{
				setState(3289);
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
				setState(3290);
				match(NOT);
				setState(3291);
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
		public TerminalNode ADD() { return getToken(ArcticSqlCommandParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(ArcticSqlCommandParser.AFTER, 0); }
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(ArcticSqlCommandParser.ANALYZE, 0); }
		public TerminalNode ANTI() { return getToken(ArcticSqlCommandParser.ANTI, 0); }
		public TerminalNode ARCHIVE() { return getToken(ArcticSqlCommandParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(ArcticSqlCommandParser.ARRAY, 0); }
		public TerminalNode ASC() { return getToken(ArcticSqlCommandParser.ASC, 0); }
		public TerminalNode AT() { return getToken(ArcticSqlCommandParser.AT, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticSqlCommandParser.BETWEEN, 0); }
		public TerminalNode BUCKET() { return getToken(ArcticSqlCommandParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(ArcticSqlCommandParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(ArcticSqlCommandParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(ArcticSqlCommandParser.CASCADE, 0); }
		public TerminalNode CATALOG() { return getToken(ArcticSqlCommandParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(ArcticSqlCommandParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(ArcticSqlCommandParser.CHANGE, 0); }
		public TerminalNode CLEAR() { return getToken(ArcticSqlCommandParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(ArcticSqlCommandParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(ArcticSqlCommandParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(ArcticSqlCommandParser.CODEGEN, 0); }
		public TerminalNode COLLECTION() { return getToken(ArcticSqlCommandParser.COLLECTION, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(ArcticSqlCommandParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(ArcticSqlCommandParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(ArcticSqlCommandParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(ArcticSqlCommandParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(ArcticSqlCommandParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(ArcticSqlCommandParser.CONCATENATE, 0); }
		public TerminalNode COST() { return getToken(ArcticSqlCommandParser.COST, 0); }
		public TerminalNode CUBE() { return getToken(ArcticSqlCommandParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticSqlCommandParser.CURRENT, 0); }
		public TerminalNode DATA() { return getToken(ArcticSqlCommandParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticSqlCommandParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticSqlCommandParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(ArcticSqlCommandParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(ArcticSqlCommandParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(ArcticSqlCommandParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(ArcticSqlCommandParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(ArcticSqlCommandParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(ArcticSqlCommandParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(ArcticSqlCommandParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(ArcticSqlCommandParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticSqlCommandParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(ArcticSqlCommandParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticSqlCommandParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticSqlCommandParser.DIRECTORY, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticSqlCommandParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(ArcticSqlCommandParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode ESCAPED() { return getToken(ArcticSqlCommandParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(ArcticSqlCommandParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(ArcticSqlCommandParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(ArcticSqlCommandParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(ArcticSqlCommandParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(ArcticSqlCommandParser.EXTRACT, 0); }
		public TerminalNode FIELDS() { return getToken(ArcticSqlCommandParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(ArcticSqlCommandParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(ArcticSqlCommandParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(ArcticSqlCommandParser.FOLLOWING, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticSqlCommandParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticSqlCommandParser.FORMATTED, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticSqlCommandParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticSqlCommandParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(ArcticSqlCommandParser.GLOBAL, 0); }
		public TerminalNode GROUPING() { return getToken(ArcticSqlCommandParser.GROUPING, 0); }
		public TerminalNode HOUR() { return getToken(ArcticSqlCommandParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticSqlCommandParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(ArcticSqlCommandParser.IMPORT, 0); }
		public TerminalNode INDEX() { return getToken(ArcticSqlCommandParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(ArcticSqlCommandParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(ArcticSqlCommandParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(ArcticSqlCommandParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(ArcticSqlCommandParser.INTERVAL, 0); }
		public TerminalNode ITEMS() { return getToken(ArcticSqlCommandParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(ArcticSqlCommandParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(ArcticSqlCommandParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(ArcticSqlCommandParser.LAZY, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(ArcticSqlCommandParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(ArcticSqlCommandParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(ArcticSqlCommandParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(ArcticSqlCommandParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(ArcticSqlCommandParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(ArcticSqlCommandParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(ArcticSqlCommandParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(ArcticSqlCommandParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(ArcticSqlCommandParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(ArcticSqlCommandParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(ArcticSqlCommandParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticSqlCommandParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(ArcticSqlCommandParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(ArcticSqlCommandParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(ArcticSqlCommandParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(ArcticSqlCommandParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(ArcticSqlCommandParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(ArcticSqlCommandParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(ArcticSqlCommandParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(ArcticSqlCommandParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(ArcticSqlCommandParser.NO, 0); }
		public TerminalNode NULLS() { return getToken(ArcticSqlCommandParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(ArcticSqlCommandParser.OF, 0); }
		public TerminalNode OPTION() { return getToken(ArcticSqlCommandParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public TerminalNode OUT() { return getToken(ArcticSqlCommandParser.OUT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(ArcticSqlCommandParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(ArcticSqlCommandParser.OVER, 0); }
		public TerminalNode OVERLAY() { return getToken(ArcticSqlCommandParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(ArcticSqlCommandParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(ArcticSqlCommandParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticSqlCommandParser.PARTITIONS, 0); }
		public TerminalNode PERCENTLIT() { return getToken(ArcticSqlCommandParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(ArcticSqlCommandParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(ArcticSqlCommandParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(ArcticSqlCommandParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticSqlCommandParser.PRECEDING, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticSqlCommandParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticSqlCommandParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(ArcticSqlCommandParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(ArcticSqlCommandParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(ArcticSqlCommandParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(ArcticSqlCommandParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticSqlCommandParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticSqlCommandParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(ArcticSqlCommandParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticSqlCommandParser.REDUCE, 0); }
		public TerminalNode REFRESH() { return getToken(ArcticSqlCommandParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(ArcticSqlCommandParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticSqlCommandParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(ArcticSqlCommandParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(ArcticSqlCommandParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(ArcticSqlCommandParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(ArcticSqlCommandParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(ArcticSqlCommandParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(ArcticSqlCommandParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(ArcticSqlCommandParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(ArcticSqlCommandParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(ArcticSqlCommandParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticSqlCommandParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(ArcticSqlCommandParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(ArcticSqlCommandParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(ArcticSqlCommandParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(ArcticSqlCommandParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(ArcticSqlCommandParser.SECOND, 0); }
		public TerminalNode SEMI() { return getToken(ArcticSqlCommandParser.SEMI, 0); }
		public TerminalNode SEPARATED() { return getToken(ArcticSqlCommandParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(ArcticSqlCommandParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode SETMINUS() { return getToken(ArcticSqlCommandParser.SETMINUS, 0); }
		public TerminalNode SETS() { return getToken(ArcticSqlCommandParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(ArcticSqlCommandParser.SKEWED, 0); }
		public TerminalNode SORT() { return getToken(ArcticSqlCommandParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(ArcticSqlCommandParser.SORTED, 0); }
		public TerminalNode START() { return getToken(ArcticSqlCommandParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticSqlCommandParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(ArcticSqlCommandParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(ArcticSqlCommandParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(ArcticSqlCommandParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(ArcticSqlCommandParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticSqlCommandParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(ArcticSqlCommandParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(ArcticSqlCommandParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(ArcticSqlCommandParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLES() { return getToken(ArcticSqlCommandParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticSqlCommandParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticSqlCommandParser.TERMINATED, 0); }
		public TerminalNode TIMESTAMP() { return getToken(ArcticSqlCommandParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(ArcticSqlCommandParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(ArcticSqlCommandParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TOUCH() { return getToken(ArcticSqlCommandParser.TOUCH, 0); }
		public TerminalNode TRANSACTION() { return getToken(ArcticSqlCommandParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(ArcticSqlCommandParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(ArcticSqlCommandParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(ArcticSqlCommandParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(ArcticSqlCommandParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(ArcticSqlCommandParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(ArcticSqlCommandParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(ArcticSqlCommandParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(ArcticSqlCommandParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(ArcticSqlCommandParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(ArcticSqlCommandParser.UNCACHE, 0); }
		public TerminalNode UNLOCK() { return getToken(ArcticSqlCommandParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(ArcticSqlCommandParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(ArcticSqlCommandParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(ArcticSqlCommandParser.USE, 0); }
		public TerminalNode VALUES() { return getToken(ArcticSqlCommandParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(ArcticSqlCommandParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticSqlCommandParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(ArcticSqlCommandParser.WEEK, 0); }
		public TerminalNode WINDOW() { return getToken(ArcticSqlCommandParser.WINDOW, 0); }
		public TerminalNode YEAR() { return getToken(ArcticSqlCommandParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(ArcticSqlCommandParser.ZONE, 0); }
		public AnsiNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ansiNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterAnsiNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitAnsiNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitAnsiNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnsiNonReservedContext ansiNonReserved() throws RecognitionException {
		AnsiNonReservedContext _localctx = new AnsiNonReservedContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_ansiNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3294);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALTER) | (1L << ANALYZE) | (1L << ANTI) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << ASC) | (1L << AT) | (1L << BETWEEN) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLECTION) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << COST) | (1L << CUBE) | (1L << CURRENT) | (1L << DAY) | (1L << DAYOFYEAR))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATA - 64)) | (1L << (DATABASE - 64)) | (1L << (DATABASES - 64)) | (1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FIELDS - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GROUPING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)))) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & ((1L << (ITEMS - 129)) | (1L << (KEYS - 129)) | (1L << (LAST - 129)) | (1L << (LAZY - 129)) | (1L << (LIKE - 129)) | (1L << (ILIKE - 129)) | (1L << (LIMIT - 129)) | (1L << (LINES - 129)) | (1L << (LIST - 129)) | (1L << (LOAD - 129)) | (1L << (LOCAL - 129)) | (1L << (LOCATION - 129)) | (1L << (LOCK - 129)) | (1L << (LOCKS - 129)) | (1L << (LOGICAL - 129)) | (1L << (MACRO - 129)) | (1L << (MAP - 129)) | (1L << (MATCHED - 129)) | (1L << (MERGE - 129)) | (1L << (MICROSECOND - 129)) | (1L << (MILLISECOND - 129)) | (1L << (MINUTE - 129)) | (1L << (MONTH - 129)) | (1L << (MSCK - 129)) | (1L << (NAMESPACE - 129)) | (1L << (NAMESPACES - 129)) | (1L << (NO - 129)) | (1L << (NULLS - 129)) | (1L << (OF - 129)) | (1L << (OPTION - 129)) | (1L << (OPTIONS - 129)) | (1L << (OUT - 129)) | (1L << (OUTPUTFORMAT - 129)) | (1L << (OVER - 129)) | (1L << (OVERLAY - 129)) | (1L << (OVERWRITE - 129)) | (1L << (PARTITION - 129)) | (1L << (PARTITIONED - 129)) | (1L << (PARTITIONS - 129)) | (1L << (PERCENTLIT - 129)) | (1L << (PIVOT - 129)) | (1L << (PLACING - 129)) | (1L << (POSITION - 129)) | (1L << (PRECEDING - 129)) | (1L << (PRINCIPALS - 129)) | (1L << (PROPERTIES - 129)) | (1L << (PURGE - 129)) | (1L << (QUARTER - 129)))) != 0) || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & ((1L << (QUERY - 193)) | (1L << (RANGE - 193)) | (1L << (RECORDREADER - 193)) | (1L << (RECORDWRITER - 193)) | (1L << (RECOVER - 193)) | (1L << (REDUCE - 193)) | (1L << (REFRESH - 193)) | (1L << (RENAME - 193)) | (1L << (REPAIR - 193)) | (1L << (REPEATABLE - 193)) | (1L << (REPLACE - 193)) | (1L << (RESET - 193)) | (1L << (RESPECT - 193)) | (1L << (RESTRICT - 193)) | (1L << (REVOKE - 193)) | (1L << (RLIKE - 193)) | (1L << (ROLE - 193)) | (1L << (ROLES - 193)) | (1L << (ROLLBACK - 193)) | (1L << (ROLLUP - 193)) | (1L << (ROW - 193)) | (1L << (ROWS - 193)) | (1L << (SECOND - 193)) | (1L << (SCHEMA - 193)) | (1L << (SCHEMAS - 193)) | (1L << (SEMI - 193)) | (1L << (SEPARATED - 193)) | (1L << (SERDE - 193)) | (1L << (SERDEPROPERTIES - 193)) | (1L << (SET - 193)) | (1L << (SETMINUS - 193)) | (1L << (SETS - 193)) | (1L << (SHOW - 193)) | (1L << (SKEWED - 193)) | (1L << (SORT - 193)) | (1L << (SORTED - 193)) | (1L << (START - 193)) | (1L << (STATISTICS - 193)) | (1L << (STORED - 193)) | (1L << (STRATIFY - 193)) | (1L << (STRUCT - 193)) | (1L << (SUBSTR - 193)) | (1L << (SUBSTRING - 193)) | (1L << (SYNC - 193)) | (1L << (SYSTEM_TIME - 193)) | (1L << (SYSTEM_VERSION - 193)) | (1L << (TABLES - 193)) | (1L << (TABLESAMPLE - 193)) | (1L << (TBLPROPERTIES - 193)) | (1L << (TEMPORARY - 193)) | (1L << (TERMINATED - 193)) | (1L << (TIMESTAMP - 193)) | (1L << (TIMESTAMPADD - 193)) | (1L << (TIMESTAMPDIFF - 193)) | (1L << (TOUCH - 193)))) != 0) || ((((_la - 258)) & ~0x3f) == 0 && ((1L << (_la - 258)) & ((1L << (TRANSACTION - 258)) | (1L << (TRANSACTIONS - 258)) | (1L << (TRANSFORM - 258)) | (1L << (TRIM - 258)) | (1L << (TRUE - 258)) | (1L << (TRUNCATE - 258)) | (1L << (TRY_CAST - 258)) | (1L << (TYPE - 258)) | (1L << (UNARCHIVE - 258)) | (1L << (UNBOUNDED - 258)) | (1L << (UNCACHE - 258)) | (1L << (UNLOCK - 258)) | (1L << (UNSET - 258)) | (1L << (UPDATE - 258)) | (1L << (USE - 258)) | (1L << (VALUES - 258)) | (1L << (VERSION - 258)) | (1L << (VIEW - 258)) | (1L << (VIEWS - 258)) | (1L << (WEEK - 258)) | (1L << (WINDOW - 258)) | (1L << (YEAR - 258)) | (1L << (ZONE - 258)))) != 0)) ) {
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
		public TerminalNode ANTI() { return getToken(ArcticSqlCommandParser.ANTI, 0); }
		public TerminalNode CROSS() { return getToken(ArcticSqlCommandParser.CROSS, 0); }
		public TerminalNode EXCEPT() { return getToken(ArcticSqlCommandParser.EXCEPT, 0); }
		public TerminalNode FULL() { return getToken(ArcticSqlCommandParser.FULL, 0); }
		public TerminalNode INNER() { return getToken(ArcticSqlCommandParser.INNER, 0); }
		public TerminalNode INTERSECT() { return getToken(ArcticSqlCommandParser.INTERSECT, 0); }
		public TerminalNode JOIN() { return getToken(ArcticSqlCommandParser.JOIN, 0); }
		public TerminalNode LATERAL() { return getToken(ArcticSqlCommandParser.LATERAL, 0); }
		public TerminalNode LEFT() { return getToken(ArcticSqlCommandParser.LEFT, 0); }
		public TerminalNode NATURAL() { return getToken(ArcticSqlCommandParser.NATURAL, 0); }
		public TerminalNode ON() { return getToken(ArcticSqlCommandParser.ON, 0); }
		public TerminalNode RIGHT() { return getToken(ArcticSqlCommandParser.RIGHT, 0); }
		public TerminalNode SEMI() { return getToken(ArcticSqlCommandParser.SEMI, 0); }
		public TerminalNode SETMINUS() { return getToken(ArcticSqlCommandParser.SETMINUS, 0); }
		public TerminalNode UNION() { return getToken(ArcticSqlCommandParser.UNION, 0); }
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public StrictNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strictNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterStrictNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitStrictNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitStrictNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictNonReservedContext strictNonReserved() throws RecognitionException {
		StrictNonReservedContext _localctx = new StrictNonReservedContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_strictNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3296);
			_la = _input.LA(1);
			if ( !(_la==ANTI || _la==CROSS || ((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & ((1L << (EXCEPT - 86)) | (1L << (FULL - 86)) | (1L << (INNER - 86)) | (1L << (INTERSECT - 86)) | (1L << (JOIN - 86)) | (1L << (LATERAL - 86)) | (1L << (LEFT - 86)))) != 0) || ((((_la - 159)) & ~0x3f) == 0 && ((1L << (_la - 159)) & ((1L << (NATURAL - 159)) | (1L << (ON - 159)) | (1L << (RIGHT - 159)) | (1L << (SEMI - 159)))) != 0) || ((((_la - 227)) & ~0x3f) == 0 && ((1L << (_la - 227)) & ((1L << (SETMINUS - 227)) | (1L << (UNION - 227)) | (1L << (USING - 227)))) != 0)) ) {
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
		public TerminalNode ADD() { return getToken(ArcticSqlCommandParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(ArcticSqlCommandParser.AFTER, 0); }
		public TerminalNode ALL() { return getToken(ArcticSqlCommandParser.ALL, 0); }
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(ArcticSqlCommandParser.ANALYZE, 0); }
		public TerminalNode AND() { return getToken(ArcticSqlCommandParser.AND, 0); }
		public TerminalNode ANY() { return getToken(ArcticSqlCommandParser.ANY, 0); }
		public TerminalNode ARCHIVE() { return getToken(ArcticSqlCommandParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(ArcticSqlCommandParser.ARRAY, 0); }
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public TerminalNode ASC() { return getToken(ArcticSqlCommandParser.ASC, 0); }
		public TerminalNode AT() { return getToken(ArcticSqlCommandParser.AT, 0); }
		public TerminalNode AUTHORIZATION() { return getToken(ArcticSqlCommandParser.AUTHORIZATION, 0); }
		public TerminalNode BETWEEN() { return getToken(ArcticSqlCommandParser.BETWEEN, 0); }
		public TerminalNode BOTH() { return getToken(ArcticSqlCommandParser.BOTH, 0); }
		public TerminalNode BUCKET() { return getToken(ArcticSqlCommandParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(ArcticSqlCommandParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(ArcticSqlCommandParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(ArcticSqlCommandParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(ArcticSqlCommandParser.CASCADE, 0); }
		public TerminalNode CASE() { return getToken(ArcticSqlCommandParser.CASE, 0); }
		public TerminalNode CAST() { return getToken(ArcticSqlCommandParser.CAST, 0); }
		public TerminalNode CATALOG() { return getToken(ArcticSqlCommandParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(ArcticSqlCommandParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(ArcticSqlCommandParser.CHANGE, 0); }
		public TerminalNode CHECK() { return getToken(ArcticSqlCommandParser.CHECK, 0); }
		public TerminalNode CLEAR() { return getToken(ArcticSqlCommandParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(ArcticSqlCommandParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(ArcticSqlCommandParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(ArcticSqlCommandParser.CODEGEN, 0); }
		public TerminalNode COLLATE() { return getToken(ArcticSqlCommandParser.COLLATE, 0); }
		public TerminalNode COLLECTION() { return getToken(ArcticSqlCommandParser.COLLECTION, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(ArcticSqlCommandParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(ArcticSqlCommandParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(ArcticSqlCommandParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(ArcticSqlCommandParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(ArcticSqlCommandParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(ArcticSqlCommandParser.CONCATENATE, 0); }
		public TerminalNode CONSTRAINT() { return getToken(ArcticSqlCommandParser.CONSTRAINT, 0); }
		public TerminalNode COST() { return getToken(ArcticSqlCommandParser.COST, 0); }
		public TerminalNode CREATE() { return getToken(ArcticSqlCommandParser.CREATE, 0); }
		public TerminalNode CUBE() { return getToken(ArcticSqlCommandParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticSqlCommandParser.CURRENT, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(ArcticSqlCommandParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(ArcticSqlCommandParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(ArcticSqlCommandParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(ArcticSqlCommandParser.CURRENT_USER, 0); }
		public TerminalNode DATA() { return getToken(ArcticSqlCommandParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(ArcticSqlCommandParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticSqlCommandParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(ArcticSqlCommandParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(ArcticSqlCommandParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(ArcticSqlCommandParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(ArcticSqlCommandParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(ArcticSqlCommandParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(ArcticSqlCommandParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(ArcticSqlCommandParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(ArcticSqlCommandParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(ArcticSqlCommandParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(ArcticSqlCommandParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(ArcticSqlCommandParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(ArcticSqlCommandParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticSqlCommandParser.DIRECTORY, 0); }
		public TerminalNode DISTINCT() { return getToken(ArcticSqlCommandParser.DISTINCT, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(ArcticSqlCommandParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(ArcticSqlCommandParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode ELSE() { return getToken(ArcticSqlCommandParser.ELSE, 0); }
		public TerminalNode END() { return getToken(ArcticSqlCommandParser.END, 0); }
		public TerminalNode ESCAPE() { return getToken(ArcticSqlCommandParser.ESCAPE, 0); }
		public TerminalNode ESCAPED() { return getToken(ArcticSqlCommandParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(ArcticSqlCommandParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(ArcticSqlCommandParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(ArcticSqlCommandParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(ArcticSqlCommandParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(ArcticSqlCommandParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(ArcticSqlCommandParser.EXTRACT, 0); }
		public TerminalNode FALSE() { return getToken(ArcticSqlCommandParser.FALSE, 0); }
		public TerminalNode FETCH() { return getToken(ArcticSqlCommandParser.FETCH, 0); }
		public TerminalNode FILTER() { return getToken(ArcticSqlCommandParser.FILTER, 0); }
		public TerminalNode FIELDS() { return getToken(ArcticSqlCommandParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(ArcticSqlCommandParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(ArcticSqlCommandParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(ArcticSqlCommandParser.FOLLOWING, 0); }
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public TerminalNode FOREIGN() { return getToken(ArcticSqlCommandParser.FOREIGN, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticSqlCommandParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(ArcticSqlCommandParser.FORMATTED, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public TerminalNode FUNCTION() { return getToken(ArcticSqlCommandParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticSqlCommandParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(ArcticSqlCommandParser.GLOBAL, 0); }
		public TerminalNode GRANT() { return getToken(ArcticSqlCommandParser.GRANT, 0); }
		public TerminalNode GROUP() { return getToken(ArcticSqlCommandParser.GROUP, 0); }
		public TerminalNode GROUPING() { return getToken(ArcticSqlCommandParser.GROUPING, 0); }
		public TerminalNode HAVING() { return getToken(ArcticSqlCommandParser.HAVING, 0); }
		public TerminalNode HOUR() { return getToken(ArcticSqlCommandParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(ArcticSqlCommandParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(ArcticSqlCommandParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(ArcticSqlCommandParser.IMPORT, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public TerminalNode INDEX() { return getToken(ArcticSqlCommandParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(ArcticSqlCommandParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(ArcticSqlCommandParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(ArcticSqlCommandParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(ArcticSqlCommandParser.INTERVAL, 0); }
		public TerminalNode INTO() { return getToken(ArcticSqlCommandParser.INTO, 0); }
		public TerminalNode IS() { return getToken(ArcticSqlCommandParser.IS, 0); }
		public TerminalNode ITEMS() { return getToken(ArcticSqlCommandParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(ArcticSqlCommandParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(ArcticSqlCommandParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(ArcticSqlCommandParser.LAZY, 0); }
		public TerminalNode LEADING() { return getToken(ArcticSqlCommandParser.LEADING, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(ArcticSqlCommandParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(ArcticSqlCommandParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(ArcticSqlCommandParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(ArcticSqlCommandParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(ArcticSqlCommandParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(ArcticSqlCommandParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(ArcticSqlCommandParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(ArcticSqlCommandParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(ArcticSqlCommandParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(ArcticSqlCommandParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(ArcticSqlCommandParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(ArcticSqlCommandParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(ArcticSqlCommandParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(ArcticSqlCommandParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(ArcticSqlCommandParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(ArcticSqlCommandParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(ArcticSqlCommandParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(ArcticSqlCommandParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(ArcticSqlCommandParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(ArcticSqlCommandParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(ArcticSqlCommandParser.NO, 0); }
		public TerminalNode NOT() { return getToken(ArcticSqlCommandParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(ArcticSqlCommandParser.NULL, 0); }
		public TerminalNode NULLS() { return getToken(ArcticSqlCommandParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(ArcticSqlCommandParser.OF, 0); }
		public TerminalNode ONLY() { return getToken(ArcticSqlCommandParser.ONLY, 0); }
		public TerminalNode OPTION() { return getToken(ArcticSqlCommandParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public TerminalNode OR() { return getToken(ArcticSqlCommandParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(ArcticSqlCommandParser.ORDER, 0); }
		public TerminalNode OUT() { return getToken(ArcticSqlCommandParser.OUT, 0); }
		public TerminalNode OUTER() { return getToken(ArcticSqlCommandParser.OUTER, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(ArcticSqlCommandParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(ArcticSqlCommandParser.OVER, 0); }
		public TerminalNode OVERLAPS() { return getToken(ArcticSqlCommandParser.OVERLAPS, 0); }
		public TerminalNode OVERLAY() { return getToken(ArcticSqlCommandParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(ArcticSqlCommandParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(ArcticSqlCommandParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(ArcticSqlCommandParser.PARTITIONS, 0); }
		public TerminalNode PERCENTILE_CONT() { return getToken(ArcticSqlCommandParser.PERCENTILE_CONT, 0); }
		public TerminalNode PERCENTILE_DISC() { return getToken(ArcticSqlCommandParser.PERCENTILE_DISC, 0); }
		public TerminalNode PERCENTLIT() { return getToken(ArcticSqlCommandParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(ArcticSqlCommandParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(ArcticSqlCommandParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(ArcticSqlCommandParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticSqlCommandParser.PRECEDING, 0); }
		public TerminalNode PRIMARY() { return getToken(ArcticSqlCommandParser.PRIMARY, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticSqlCommandParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticSqlCommandParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(ArcticSqlCommandParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(ArcticSqlCommandParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(ArcticSqlCommandParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(ArcticSqlCommandParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticSqlCommandParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticSqlCommandParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(ArcticSqlCommandParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticSqlCommandParser.REDUCE, 0); }
		public TerminalNode REFERENCES() { return getToken(ArcticSqlCommandParser.REFERENCES, 0); }
		public TerminalNode REFRESH() { return getToken(ArcticSqlCommandParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(ArcticSqlCommandParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticSqlCommandParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(ArcticSqlCommandParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(ArcticSqlCommandParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(ArcticSqlCommandParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(ArcticSqlCommandParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(ArcticSqlCommandParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(ArcticSqlCommandParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(ArcticSqlCommandParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(ArcticSqlCommandParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(ArcticSqlCommandParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(ArcticSqlCommandParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(ArcticSqlCommandParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(ArcticSqlCommandParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(ArcticSqlCommandParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(ArcticSqlCommandParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(ArcticSqlCommandParser.SECOND, 0); }
		public TerminalNode SELECT() { return getToken(ArcticSqlCommandParser.SELECT, 0); }
		public TerminalNode SEPARATED() { return getToken(ArcticSqlCommandParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(ArcticSqlCommandParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public TerminalNode SESSION_USER() { return getToken(ArcticSqlCommandParser.SESSION_USER, 0); }
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode SETS() { return getToken(ArcticSqlCommandParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(ArcticSqlCommandParser.SKEWED, 0); }
		public TerminalNode SOME() { return getToken(ArcticSqlCommandParser.SOME, 0); }
		public TerminalNode SORT() { return getToken(ArcticSqlCommandParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(ArcticSqlCommandParser.SORTED, 0); }
		public TerminalNode START() { return getToken(ArcticSqlCommandParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(ArcticSqlCommandParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(ArcticSqlCommandParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(ArcticSqlCommandParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(ArcticSqlCommandParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(ArcticSqlCommandParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticSqlCommandParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(ArcticSqlCommandParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(ArcticSqlCommandParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(ArcticSqlCommandParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode TABLES() { return getToken(ArcticSqlCommandParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticSqlCommandParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticSqlCommandParser.TERMINATED, 0); }
		public TerminalNode THEN() { return getToken(ArcticSqlCommandParser.THEN, 0); }
		public TerminalNode TIME() { return getToken(ArcticSqlCommandParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(ArcticSqlCommandParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(ArcticSqlCommandParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(ArcticSqlCommandParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TO() { return getToken(ArcticSqlCommandParser.TO, 0); }
		public TerminalNode TOUCH() { return getToken(ArcticSqlCommandParser.TOUCH, 0); }
		public TerminalNode TRAILING() { return getToken(ArcticSqlCommandParser.TRAILING, 0); }
		public TerminalNode TRANSACTION() { return getToken(ArcticSqlCommandParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(ArcticSqlCommandParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(ArcticSqlCommandParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(ArcticSqlCommandParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(ArcticSqlCommandParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(ArcticSqlCommandParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(ArcticSqlCommandParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(ArcticSqlCommandParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(ArcticSqlCommandParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(ArcticSqlCommandParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(ArcticSqlCommandParser.UNCACHE, 0); }
		public TerminalNode UNIQUE() { return getToken(ArcticSqlCommandParser.UNIQUE, 0); }
		public TerminalNode UNKNOWN() { return getToken(ArcticSqlCommandParser.UNKNOWN, 0); }
		public TerminalNode UNLOCK() { return getToken(ArcticSqlCommandParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(ArcticSqlCommandParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(ArcticSqlCommandParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(ArcticSqlCommandParser.USE, 0); }
		public TerminalNode USER() { return getToken(ArcticSqlCommandParser.USER, 0); }
		public TerminalNode VALUES() { return getToken(ArcticSqlCommandParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(ArcticSqlCommandParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticSqlCommandParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(ArcticSqlCommandParser.WEEK, 0); }
		public TerminalNode WHEN() { return getToken(ArcticSqlCommandParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(ArcticSqlCommandParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(ArcticSqlCommandParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode WITHIN() { return getToken(ArcticSqlCommandParser.WITHIN, 0); }
		public TerminalNode YEAR() { return getToken(ArcticSqlCommandParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(ArcticSqlCommandParser.ZONE, 0); }
		public NonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonReservedContext nonReserved() throws RecognitionException {
		NonReservedContext _localctx = new NonReservedContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_nonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3298);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALL) | (1L << ALTER) | (1L << ANALYZE) | (1L << AND) | (1L << ANY) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << AS) | (1L << ASC) | (1L << AT) | (1L << AUTHORIZATION) | (1L << BETWEEN) | (1L << BOTH) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CASE) | (1L << CAST) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CHECK) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLATE) | (1L << COLLECTION) | (1L << COLUMN) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << CONSTRAINT) | (1L << COST) | (1L << CREATE) | (1L << CUBE) | (1L << CURRENT) | (1L << CURRENT_DATE) | (1L << CURRENT_TIME) | (1L << CURRENT_TIMESTAMP) | (1L << CURRENT_USER) | (1L << DAY) | (1L << DAYOFYEAR))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATA - 64)) | (1L << (DATABASE - 64)) | (1L << (DATABASES - 64)) | (1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTINCT - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ELSE - 64)) | (1L << (END - 64)) | (1L << (ESCAPE - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FALSE - 64)) | (1L << (FETCH - 64)) | (1L << (FIELDS - 64)) | (1L << (FILTER - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FOR - 64)) | (1L << (FOREIGN - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FROM - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GRANT - 64)) | (1L << (GROUP - 64)) | (1L << (GROUPING - 64)) | (1L << (HAVING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (IN - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)) | (1L << (INTO - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (IS - 128)) | (1L << (ITEMS - 128)) | (1L << (KEYS - 128)) | (1L << (LAST - 128)) | (1L << (LAZY - 128)) | (1L << (LEADING - 128)) | (1L << (LIKE - 128)) | (1L << (ILIKE - 128)) | (1L << (LIMIT - 128)) | (1L << (LINES - 128)) | (1L << (LIST - 128)) | (1L << (LOAD - 128)) | (1L << (LOCAL - 128)) | (1L << (LOCATION - 128)) | (1L << (LOCK - 128)) | (1L << (LOCKS - 128)) | (1L << (LOGICAL - 128)) | (1L << (MACRO - 128)) | (1L << (MAP - 128)) | (1L << (MATCHED - 128)) | (1L << (MERGE - 128)) | (1L << (MICROSECOND - 128)) | (1L << (MILLISECOND - 128)) | (1L << (MINUTE - 128)) | (1L << (MONTH - 128)) | (1L << (MSCK - 128)) | (1L << (NAMESPACE - 128)) | (1L << (NAMESPACES - 128)) | (1L << (NO - 128)) | (1L << (NOT - 128)) | (1L << (NULL - 128)) | (1L << (NULLS - 128)) | (1L << (OF - 128)) | (1L << (ONLY - 128)) | (1L << (OPTION - 128)) | (1L << (OPTIONS - 128)) | (1L << (OR - 128)) | (1L << (ORDER - 128)) | (1L << (OUT - 128)) | (1L << (OUTER - 128)) | (1L << (OUTPUTFORMAT - 128)) | (1L << (OVER - 128)) | (1L << (OVERLAPS - 128)) | (1L << (OVERLAY - 128)) | (1L << (OVERWRITE - 128)) | (1L << (PARTITION - 128)) | (1L << (PARTITIONED - 128)) | (1L << (PARTITIONS - 128)) | (1L << (PERCENTILE_CONT - 128)) | (1L << (PERCENTILE_DISC - 128)) | (1L << (PERCENTLIT - 128)) | (1L << (PIVOT - 128)) | (1L << (PLACING - 128)) | (1L << (POSITION - 128)) | (1L << (PRECEDING - 128)) | (1L << (PRIMARY - 128)) | (1L << (PRINCIPALS - 128)) | (1L << (PROPERTIES - 128)) | (1L << (PURGE - 128)))) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & ((1L << (QUARTER - 192)) | (1L << (QUERY - 192)) | (1L << (RANGE - 192)) | (1L << (RECORDREADER - 192)) | (1L << (RECORDWRITER - 192)) | (1L << (RECOVER - 192)) | (1L << (REDUCE - 192)) | (1L << (REFERENCES - 192)) | (1L << (REFRESH - 192)) | (1L << (RENAME - 192)) | (1L << (REPAIR - 192)) | (1L << (REPEATABLE - 192)) | (1L << (REPLACE - 192)) | (1L << (RESET - 192)) | (1L << (RESPECT - 192)) | (1L << (RESTRICT - 192)) | (1L << (REVOKE - 192)) | (1L << (RLIKE - 192)) | (1L << (ROLE - 192)) | (1L << (ROLES - 192)) | (1L << (ROLLBACK - 192)) | (1L << (ROLLUP - 192)) | (1L << (ROW - 192)) | (1L << (ROWS - 192)) | (1L << (SECOND - 192)) | (1L << (SCHEMA - 192)) | (1L << (SCHEMAS - 192)) | (1L << (SELECT - 192)) | (1L << (SEPARATED - 192)) | (1L << (SERDE - 192)) | (1L << (SERDEPROPERTIES - 192)) | (1L << (SESSION_USER - 192)) | (1L << (SET - 192)) | (1L << (SETS - 192)) | (1L << (SHOW - 192)) | (1L << (SKEWED - 192)) | (1L << (SOME - 192)) | (1L << (SORT - 192)) | (1L << (SORTED - 192)) | (1L << (START - 192)) | (1L << (STATISTICS - 192)) | (1L << (STORED - 192)) | (1L << (STRATIFY - 192)) | (1L << (STRUCT - 192)) | (1L << (SUBSTR - 192)) | (1L << (SUBSTRING - 192)) | (1L << (SYNC - 192)) | (1L << (SYSTEM_TIME - 192)) | (1L << (SYSTEM_VERSION - 192)) | (1L << (TABLE - 192)) | (1L << (TABLES - 192)) | (1L << (TABLESAMPLE - 192)) | (1L << (TBLPROPERTIES - 192)) | (1L << (TEMPORARY - 192)) | (1L << (TERMINATED - 192)) | (1L << (THEN - 192)) | (1L << (TIME - 192)) | (1L << (TIMESTAMP - 192)) | (1L << (TIMESTAMPADD - 192)) | (1L << (TIMESTAMPDIFF - 192)) | (1L << (TO - 192)))) != 0) || ((((_la - 256)) & ~0x3f) == 0 && ((1L << (_la - 256)) & ((1L << (TOUCH - 256)) | (1L << (TRAILING - 256)) | (1L << (TRANSACTION - 256)) | (1L << (TRANSACTIONS - 256)) | (1L << (TRANSFORM - 256)) | (1L << (TRIM - 256)) | (1L << (TRUE - 256)) | (1L << (TRUNCATE - 256)) | (1L << (TRY_CAST - 256)) | (1L << (TYPE - 256)) | (1L << (UNARCHIVE - 256)) | (1L << (UNBOUNDED - 256)) | (1L << (UNCACHE - 256)) | (1L << (UNIQUE - 256)) | (1L << (UNKNOWN - 256)) | (1L << (UNLOCK - 256)) | (1L << (UNSET - 256)) | (1L << (UPDATE - 256)) | (1L << (USE - 256)) | (1L << (USER - 256)) | (1L << (VALUES - 256)) | (1L << (VERSION - 256)) | (1L << (VIEW - 256)) | (1L << (VIEWS - 256)) | (1L << (WEEK - 256)) | (1L << (WHEN - 256)) | (1L << (WHERE - 256)) | (1L << (WINDOW - 256)) | (1L << (WITH - 256)) | (1L << (WITHIN - 256)) | (1L << (YEAR - 256)) | (1L << (ZONE - 256)))) != 0)) ) {
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
		case 45:
			return queryTerm_sempred((QueryTermContext)_localctx, predIndex);
		case 107:
			return booleanExpression_sempred((BooleanExpressionContext)_localctx, predIndex);
		case 109:
			return valueExpression_sempred((ValueExpressionContext)_localctx, predIndex);
		case 111:
			return primaryExpression_sempred((PrimaryExpressionContext)_localctx, predIndex);
		case 142:
			return identifier_sempred((IdentifierContext)_localctx, predIndex);
		case 143:
			return strictIdentifier_sempred((StrictIdentifierContext)_localctx, predIndex);
		case 145:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0149\u0ce7\4\2\t"+
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
		"\t\u0097\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\7\4\u0139\n\4\f\4\16"+
		"\4\u013c\13\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b"+
		"\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\5\13\u0154\n\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0161\n\13\3\13\3\13\3\13\3\13"+
		"\3\13\5\13\u0168\n\13\3\13\3\13\3\13\3\13\3\13\3\13\7\13\u0170\n\13\f"+
		"\13\16\13\u0173\13\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0186\n\13\3\13\3\13\5\13\u018a"+
		"\n\13\3\13\3\13\3\13\3\13\5\13\u0190\n\13\3\13\5\13\u0193\n\13\3\13\5"+
		"\13\u0196\n\13\3\13\3\13\3\13\3\13\3\13\5\13\u019d\n\13\3\13\5\13\u01a0"+
		"\n\13\3\13\3\13\5\13\u01a4\n\13\3\13\5\13\u01a7\n\13\3\13\3\13\3\13\3"+
		"\13\3\13\5\13\u01ae\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\7\13\u01b9\n\13\f\13\16\13\u01bc\13\13\3\13\3\13\3\13\3\13\3\13\5\13"+
		"\u01c3\n\13\3\13\5\13\u01c6\n\13\3\13\3\13\5\13\u01ca\n\13\3\13\5\13\u01cd"+
		"\n\13\3\13\3\13\3\13\3\13\5\13\u01d3\n\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\5\13\u01de\n\13\3\13\3\13\3\13\3\13\5\13\u01e4\n\13\3"+
		"\13\3\13\3\13\5\13\u01e9\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u020b\n\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0218\n\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0231\n\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\5\13\u023a\n\13\3\13\3\13\5\13\u023e\n\13\3\13\3"+
		"\13\3\13\3\13\5\13\u0244\n\13\3\13\3\13\5\13\u0248\n\13\3\13\3\13\3\13"+
		"\5\13\u024d\n\13\3\13\3\13\3\13\3\13\5\13\u0253\n\13\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u025f\n\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\5\13\u0267\n\13\3\13\3\13\3\13\3\13\5\13\u026d\n\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u027a\n\13\3\13"+
		"\6\13\u027d\n\13\r\13\16\13\u027e\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u028f\n\13\3\13\3\13\3\13\7\13"+
		"\u0294\n\13\f\13\16\13\u0297\13\13\3\13\5\13\u029a\n\13\3\13\3\13\3\13"+
		"\3\13\5\13\u02a0\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\5\13\u02af\n\13\3\13\3\13\5\13\u02b3\n\13\3\13\3\13\3"+
		"\13\3\13\5\13\u02b9\n\13\3\13\3\13\3\13\3\13\5\13\u02bf\n\13\3\13\5\13"+
		"\u02c2\n\13\3\13\5\13\u02c5\n\13\3\13\3\13\3\13\3\13\5\13\u02cb\n\13\3"+
		"\13\3\13\5\13\u02cf\n\13\3\13\3\13\3\13\3\13\3\13\3\13\7\13\u02d7\n\13"+
		"\f\13\16\13\u02da\13\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u02e2\n\13"+
		"\3\13\5\13\u02e5\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u02ee\n"+
		"\13\3\13\3\13\3\13\5\13\u02f3\n\13\3\13\3\13\3\13\3\13\5\13\u02f9\n\13"+
		"\3\13\3\13\3\13\3\13\3\13\5\13\u0300\n\13\3\13\5\13\u0303\n\13\3\13\3"+
		"\13\3\13\3\13\5\13\u0309\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\7\13"+
		"\u0312\n\13\f\13\16\13\u0315\13\13\5\13\u0317\n\13\3\13\3\13\5\13\u031b"+
		"\n\13\3\13\3\13\3\13\5\13\u0320\n\13\3\13\3\13\3\13\5\13\u0325\n\13\3"+
		"\13\3\13\3\13\3\13\3\13\5\13\u032c\n\13\3\13\5\13\u032f\n\13\3\13\5\13"+
		"\u0332\n\13\3\13\3\13\3\13\3\13\3\13\5\13\u0339\n\13\3\13\3\13\3\13\5"+
		"\13\u033e\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0347\n\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\5\13\u034f\n\13\3\13\3\13\3\13\3\13\5\13\u0355"+
		"\n\13\3\13\5\13\u0358\n\13\3\13\5\13\u035b\n\13\3\13\3\13\3\13\3\13\5"+
		"\13\u0361\n\13\3\13\3\13\5\13\u0365\n\13\3\13\3\13\3\13\5\13\u036a\n\13"+
		"\3\13\5\13\u036d\n\13\3\13\3\13\5\13\u0371\n\13\5\13\u0373\n\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\5\13\u037b\n\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\5\13\u0383\n\13\3\13\5\13\u0386\n\13\3\13\3\13\3\13\5\13\u038b\n\13\3"+
		"\13\3\13\3\13\3\13\5\13\u0391\n\13\3\13\3\13\3\13\3\13\5\13\u0397\n\13"+
		"\3\13\5\13\u039a\n\13\3\13\3\13\5\13\u039e\n\13\3\13\5\13\u03a1\n\13\3"+
		"\13\3\13\5\13\u03a5\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\7\13\u03bf\n\13\f\13\16\13\u03c2\13\13\5\13\u03c4\n\13\3\13\3\13"+
		"\5\13\u03c8\n\13\3\13\3\13\3\13\3\13\5\13\u03ce\n\13\3\13\5\13\u03d1\n"+
		"\13\3\13\5\13\u03d4\n\13\3\13\3\13\3\13\3\13\5\13\u03da\n\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\5\13\u03e2\n\13\3\13\3\13\3\13\5\13\u03e7\n\13\3"+
		"\13\3\13\3\13\3\13\5\13\u03ed\n\13\3\13\3\13\3\13\3\13\5\13\u03f3\n\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u03fb\n\13\3\13\3\13\3\13\7\13\u0400"+
		"\n\13\f\13\16\13\u0403\13\13\3\13\3\13\3\13\7\13\u0408\n\13\f\13\16\13"+
		"\u040b\13\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\7\13\u0419\n\13\f\13\16\13\u041c\13\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\7\13\u0427\n\13\f\13\16\13\u042a\13\13\5\13\u042c\n"+
		"\13\3\13\3\13\7\13\u0430\n\13\f\13\16\13\u0433\13\13\3\13\3\13\3\13\3"+
		"\13\7\13\u0439\n\13\f\13\16\13\u043c\13\13\3\13\3\13\3\13\3\13\7\13\u0442"+
		"\n\13\f\13\16\13\u0445\13\13\3\13\3\13\3\13\3\13\3\13\5\13\u044c\n\13"+
		"\3\13\3\13\3\13\5\13\u0451\n\13\3\13\3\13\3\13\5\13\u0456\n\13\3\13\3"+
		"\13\3\13\3\13\3\13\5\13\u045d\n\13\3\13\3\13\3\13\3\13\5\13\u0463\n\13"+
		"\3\13\3\13\3\13\5\13\u0468\n\13\3\13\3\13\3\13\3\13\7\13\u046e\n\13\f"+
		"\13\16\13\u0471\13\13\5\13\u0473\n\13\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\5\16\u047f\n\16\3\16\3\16\5\16\u0483\n\16\3\16\3\16\3\16"+
		"\3\16\3\16\5\16\u048a\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u04fe\n\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\5\16\u0506\n\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u050e\n"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0517\n\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\5\16\u0521\n\16\3\17\3\17\5\17\u0525\n\17\3"+
		"\17\5\17\u0528\n\17\3\17\3\17\3\17\3\17\5\17\u052e\n\17\3\17\3\17\3\20"+
		"\3\20\5\20\u0534\n\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\5\21\u0540\n\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22"+
		"\u054c\n\22\3\22\3\22\3\22\5\22\u0551\n\22\3\23\3\23\3\23\3\24\3\24\3"+
		"\24\3\25\5\25\u055a\n\25\3\25\3\25\3\25\3\26\3\26\3\26\5\26\u0562\n\26"+
		"\3\26\3\26\3\26\3\26\3\26\5\26\u0569\n\26\5\26\u056b\n\26\3\26\5\26\u056e"+
		"\n\26\3\26\3\26\3\26\5\26\u0573\n\26\3\26\3\26\5\26\u0577\n\26\3\26\3"+
		"\26\3\26\5\26\u057c\n\26\3\26\5\26\u057f\n\26\3\26\3\26\3\26\5\26\u0584"+
		"\n\26\3\26\3\26\3\26\5\26\u0589\n\26\3\26\5\26\u058c\n\26\3\26\3\26\3"+
		"\26\5\26\u0591\n\26\3\26\3\26\5\26\u0595\n\26\3\26\3\26\3\26\5\26\u059a"+
		"\n\26\5\26\u059c\n\26\3\27\3\27\5\27\u05a0\n\27\3\30\3\30\3\30\3\30\3"+
		"\30\7\30\u05a7\n\30\f\30\16\30\u05aa\13\30\3\30\3\30\3\31\3\31\3\31\5"+
		"\31\u05b1\n\31\3\32\3\32\3\33\3\33\3\34\3\34\3\34\3\34\3\34\5\34\u05bc"+
		"\n\34\3\35\3\35\3\35\7\35\u05c1\n\35\f\35\16\35\u05c4\13\35\3\36\3\36"+
		"\3\36\3\36\7\36\u05ca\n\36\f\36\16\36\u05cd\13\36\3\37\3\37\5\37\u05d1"+
		"\n\37\3\37\5\37\u05d4\n\37\3\37\3\37\3\37\3\37\3 \3 \3 \3!\3!\3!\3!\3"+
		"!\3!\3!\3!\3!\3!\3!\3!\3!\7!\u05ea\n!\f!\16!\u05ed\13!\3\"\3\"\3\"\3\""+
		"\7\"\u05f3\n\"\f\"\16\"\u05f6\13\"\3\"\3\"\3#\3#\5#\u05fc\n#\3#\5#\u05ff"+
		"\n#\3$\3$\3$\7$\u0604\n$\f$\16$\u0607\13$\3$\5$\u060a\n$\3%\3%\3%\3%\5"+
		"%\u0610\n%\3&\3&\3&\3&\7&\u0616\n&\f&\16&\u0619\13&\3&\3&\3\'\3\'\3\'"+
		"\3\'\7\'\u0621\n\'\f\'\16\'\u0624\13\'\3\'\3\'\3(\3(\3(\3(\3(\3(\5(\u062e"+
		"\n(\3)\3)\3)\3)\3)\5)\u0635\n)\3*\3*\3*\3*\5*\u063b\n*\3+\3+\3+\3,\3,"+
		"\3,\3,\3,\6,\u0645\n,\r,\16,\u0646\3,\3,\3,\3,\3,\5,\u064e\n,\3,\3,\3"+
		",\3,\3,\5,\u0655\n,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\5,\u0661\n,\3,\3,\3"+
		",\3,\7,\u0667\n,\f,\16,\u066a\13,\3,\7,\u066d\n,\f,\16,\u0670\13,\5,\u0672"+
		"\n,\3-\3-\3-\3-\3-\7-\u0679\n-\f-\16-\u067c\13-\5-\u067e\n-\3-\3-\3-\3"+
		"-\3-\7-\u0685\n-\f-\16-\u0688\13-\5-\u068a\n-\3-\3-\3-\3-\3-\7-\u0691"+
		"\n-\f-\16-\u0694\13-\5-\u0696\n-\3-\3-\3-\3-\3-\7-\u069d\n-\f-\16-\u06a0"+
		"\13-\5-\u06a2\n-\3-\5-\u06a5\n-\3-\3-\3-\5-\u06aa\n-\5-\u06ac\n-\3.\3"+
		".\3.\3/\3/\3/\3/\3/\3/\3/\5/\u06b8\n/\3/\3/\3/\3/\3/\5/\u06bf\n/\3/\3"+
		"/\3/\3/\3/\5/\u06c6\n/\3/\7/\u06c9\n/\f/\16/\u06cc\13/\3\60\3\60\3\60"+
		"\3\60\3\60\3\60\3\60\3\60\3\60\5\60\u06d7\n\60\3\61\3\61\5\61\u06db\n"+
		"\61\3\61\3\61\5\61\u06df\n\61\3\62\3\62\6\62\u06e3\n\62\r\62\16\62\u06e4"+
		"\3\63\3\63\5\63\u06e9\n\63\3\63\3\63\3\63\3\63\7\63\u06ef\n\63\f\63\16"+
		"\63\u06f2\13\63\3\63\5\63\u06f5\n\63\3\63\5\63\u06f8\n\63\3\63\5\63\u06fb"+
		"\n\63\3\63\5\63\u06fe\n\63\3\63\3\63\5\63\u0702\n\63\3\64\3\64\5\64\u0706"+
		"\n\64\3\64\7\64\u0709\n\64\f\64\16\64\u070c\13\64\3\64\5\64\u070f\n\64"+
		"\3\64\5\64\u0712\n\64\3\64\5\64\u0715\n\64\3\64\5\64\u0718\n\64\3\64\3"+
		"\64\5\64\u071c\n\64\3\64\7\64\u071f\n\64\f\64\16\64\u0722\13\64\3\64\5"+
		"\64\u0725\n\64\3\64\5\64\u0728\n\64\3\64\5\64\u072b\n\64\3\64\5\64\u072e"+
		"\n\64\5\64\u0730\n\64\3\65\3\65\3\65\3\65\5\65\u0736\n\65\3\65\3\65\3"+
		"\65\3\65\3\65\5\65\u073d\n\65\3\65\3\65\3\65\5\65\u0742\n\65\3\65\5\65"+
		"\u0745\n\65\3\65\5\65\u0748\n\65\3\65\3\65\5\65\u074c\n\65\3\65\3\65\3"+
		"\65\3\65\3\65\3\65\3\65\3\65\5\65\u0756\n\65\3\65\3\65\5\65\u075a\n\65"+
		"\5\65\u075c\n\65\3\65\5\65\u075f\n\65\3\65\3\65\5\65\u0763\n\65\3\66\3"+
		"\66\7\66\u0767\n\66\f\66\16\66\u076a\13\66\3\66\5\66\u076d\n\66\3\66\3"+
		"\66\3\67\3\67\3\67\38\38\38\38\58\u0778\n8\38\38\38\39\39\39\39\39\59"+
		"\u0782\n9\39\39\39\3:\3:\3:\3:\3:\3:\3:\5:\u078e\n:\3;\3;\3;\3;\3;\3;"+
		"\3;\3;\3;\3;\3;\7;\u079b\n;\f;\16;\u079e\13;\3;\3;\5;\u07a2\n;\3<\3<\3"+
		"<\7<\u07a7\n<\f<\16<\u07aa\13<\3=\3=\3=\3=\3>\3>\3>\3?\3?\3?\3@\3@\3@"+
		"\5@\u07b9\n@\3@\7@\u07bc\n@\f@\16@\u07bf\13@\3@\3@\3A\3A\3A\3A\3A\3A\7"+
		"A\u07c9\nA\fA\16A\u07cc\13A\3A\3A\5A\u07d0\nA\3B\3B\3B\3B\7B\u07d6\nB"+
		"\fB\16B\u07d9\13B\3B\7B\u07dc\nB\fB\16B\u07df\13B\3B\5B\u07e2\nB\3C\5"+
		"C\u07e5\nC\3C\3C\3C\3C\3C\5C\u07ec\nC\3C\3C\3C\3C\5C\u07f2\nC\3D\3D\3"+
		"D\3D\3D\7D\u07f9\nD\fD\16D\u07fc\13D\3D\3D\3D\3D\3D\7D\u0803\nD\fD\16"+
		"D\u0806\13D\3D\3D\3D\3D\3D\3D\3D\3D\3D\3D\7D\u0812\nD\fD\16D\u0815\13"+
		"D\3D\3D\5D\u0819\nD\5D\u081b\nD\3E\3E\5E\u081f\nE\3F\3F\3F\3F\3F\7F\u0826"+
		"\nF\fF\16F\u0829\13F\3F\3F\3F\3F\3F\3F\3F\3F\7F\u0833\nF\fF\16F\u0836"+
		"\13F\3F\3F\5F\u083a\nF\3G\3G\5G\u083e\nG\3H\3H\3H\3H\7H\u0844\nH\fH\16"+
		"H\u0847\13H\5H\u0849\nH\3H\3H\5H\u084d\nH\3I\3I\3I\3I\3I\3I\3I\3I\3I\3"+
		"I\7I\u0859\nI\fI\16I\u085c\13I\3I\3I\3I\3J\3J\3J\3J\3J\7J\u0866\nJ\fJ"+
		"\16J\u0869\13J\3J\3J\5J\u086d\nJ\3K\3K\5K\u0871\nK\3K\5K\u0874\nK\3L\3"+
		"L\3L\5L\u0879\nL\3L\3L\3L\3L\3L\7L\u0880\nL\fL\16L\u0883\13L\5L\u0885"+
		"\nL\3L\3L\3L\5L\u088a\nL\3L\3L\3L\7L\u088f\nL\fL\16L\u0892\13L\5L\u0894"+
		"\nL\3M\3M\3N\5N\u0899\nN\3N\3N\7N\u089d\nN\fN\16N\u08a0\13N\3O\3O\3O\5"+
		"O\u08a5\nO\3O\3O\5O\u08a9\nO\3O\3O\3O\3O\5O\u08af\nO\3O\3O\5O\u08b3\n"+
		"O\3P\5P\u08b6\nP\3P\3P\3P\5P\u08bb\nP\3P\5P\u08be\nP\3P\3P\3P\5P\u08c3"+
		"\nP\3P\3P\5P\u08c7\nP\3P\5P\u08ca\nP\3P\5P\u08cd\nP\3Q\3Q\3Q\3Q\5Q\u08d3"+
		"\nQ\3R\3R\3R\5R\u08d8\nR\3R\3R\3R\3R\3R\5R\u08df\nR\3S\5S\u08e2\nS\3S"+
		"\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\5S\u08f4\nS\5S\u08f6\nS"+
		"\3S\5S\u08f9\nS\3T\3T\3T\3T\3U\3U\3U\7U\u0902\nU\fU\16U\u0905\13U\3V\3"+
		"V\3V\3V\7V\u090b\nV\fV\16V\u090e\13V\3V\3V\3W\3W\5W\u0914\nW\3X\3X\3X"+
		"\3X\7X\u091a\nX\fX\16X\u091d\13X\3X\3X\3Y\3Y\5Y\u0923\nY\3Z\3Z\5Z\u0927"+
		"\nZ\3Z\5Z\u092a\nZ\3Z\3Z\3Z\3Z\3Z\3Z\5Z\u0932\nZ\3Z\3Z\3Z\3Z\3Z\3Z\5Z"+
		"\u093a\nZ\3Z\3Z\3Z\3Z\5Z\u0940\nZ\3[\3[\3[\3[\7[\u0946\n[\f[\16[\u0949"+
		"\13[\3[\3[\3\\\3\\\3\\\3\\\3\\\7\\\u0952\n\\\f\\\16\\\u0955\13\\\5\\\u0957"+
		"\n\\\3\\\3\\\3\\\3]\5]\u095d\n]\3]\3]\5]\u0961\n]\5]\u0963\n]\3^\3^\3"+
		"^\3^\3^\3^\3^\5^\u096c\n^\3^\3^\3^\3^\3^\3^\3^\3^\3^\3^\5^\u0978\n^\5"+
		"^\u097a\n^\3^\3^\3^\3^\3^\5^\u0981\n^\3^\3^\3^\3^\3^\5^\u0988\n^\3^\3"+
		"^\3^\3^\5^\u098e\n^\3^\3^\3^\3^\5^\u0994\n^\5^\u0996\n^\3_\3_\3_\7_\u099b"+
		"\n_\f_\16_\u099e\13_\3`\3`\3`\7`\u09a3\n`\f`\16`\u09a6\13`\3a\3a\3a\7"+
		"a\u09ab\na\fa\16a\u09ae\13a\3b\3b\3b\5b\u09b3\nb\3c\3c\3c\5c\u09b8\nc"+
		"\3c\3c\3d\3d\3d\5d\u09bf\nd\3d\3d\3e\3e\5e\u09c5\ne\3e\3e\5e\u09c9\ne"+
		"\5e\u09cb\ne\3f\3f\3f\7f\u09d0\nf\ff\16f\u09d3\13f\3g\3g\3g\3g\7g\u09d9"+
		"\ng\fg\16g\u09dc\13g\3g\3g\3h\3h\5h\u09e2\nh\3i\3i\3i\3i\3i\3i\7i\u09ea"+
		"\ni\fi\16i\u09ed\13i\3i\3i\5i\u09f1\ni\3j\3j\5j\u09f5\nj\3k\3k\3l\3l\3"+
		"l\7l\u09fc\nl\fl\16l\u09ff\13l\3m\3m\3m\3m\3m\3m\3m\3m\3m\3m\5m\u0a0b"+
		"\nm\5m\u0a0d\nm\3m\3m\3m\3m\3m\3m\7m\u0a15\nm\fm\16m\u0a18\13m\3n\5n\u0a1b"+
		"\nn\3n\3n\3n\3n\3n\3n\5n\u0a23\nn\3n\3n\3n\3n\3n\7n\u0a2a\nn\fn\16n\u0a2d"+
		"\13n\3n\3n\3n\5n\u0a32\nn\3n\3n\3n\3n\3n\3n\5n\u0a3a\nn\3n\3n\3n\5n\u0a3f"+
		"\nn\3n\3n\3n\3n\3n\3n\3n\3n\7n\u0a49\nn\fn\16n\u0a4c\13n\3n\3n\5n\u0a50"+
		"\nn\3n\5n\u0a53\nn\3n\3n\3n\3n\5n\u0a59\nn\3n\3n\5n\u0a5d\nn\3n\3n\3n"+
		"\5n\u0a62\nn\3n\3n\3n\5n\u0a67\nn\3n\3n\3n\5n\u0a6c\nn\3o\3o\3o\3o\5o"+
		"\u0a72\no\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\3o\7o"+
		"\u0a87\no\fo\16o\u0a8a\13o\3p\3p\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3"+
		"q\3q\3q\3q\3q\3q\3q\3q\3q\3q\6q\u0aa4\nq\rq\16q\u0aa5\3q\3q\5q\u0aaa\n"+
		"q\3q\3q\3q\3q\3q\6q\u0ab1\nq\rq\16q\u0ab2\3q\3q\5q\u0ab7\nq\3q\3q\3q\3"+
		"q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\7q\u0ac7\nq\fq\16q\u0aca\13q\5q\u0acc"+
		"\nq\3q\3q\3q\3q\3q\3q\5q\u0ad4\nq\3q\3q\3q\3q\3q\3q\3q\5q\u0add\nq\3q"+
		"\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\6q\u0af2\nq\rq"+
		"\16q\u0af3\3q\3q\3q\3q\3q\3q\3q\3q\3q\5q\u0aff\nq\3q\3q\3q\7q\u0b04\n"+
		"q\fq\16q\u0b07\13q\5q\u0b09\nq\3q\3q\3q\3q\3q\3q\3q\5q\u0b12\nq\3q\3q"+
		"\5q\u0b16\nq\3q\3q\5q\u0b1a\nq\3q\3q\3q\3q\3q\3q\3q\3q\6q\u0b24\nq\rq"+
		"\16q\u0b25\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3"+
		"q\3q\3q\3q\5q\u0b3f\nq\3q\3q\3q\3q\3q\5q\u0b46\nq\3q\5q\u0b49\nq\3q\3"+
		"q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\3q\5q\u0b58\nq\3q\3q\3q\3q\3q\3q\3q\3"+
		"q\3q\3q\3q\3q\3q\3q\3q\5q\u0b69\nq\5q\u0b6b\nq\3q\3q\3q\3q\3q\3q\3q\3"+
		"q\7q\u0b75\nq\fq\16q\u0b78\13q\3r\3r\3r\3r\3r\3r\3r\3r\6r\u0b82\nr\rr"+
		"\16r\u0b83\5r\u0b86\nr\3s\3s\3t\3t\3u\3u\3v\3v\3w\3w\3w\5w\u0b93\nw\3"+
		"x\3x\5x\u0b97\nx\3y\3y\3y\6y\u0b9c\ny\ry\16y\u0b9d\3z\3z\3z\5z\u0ba3\n"+
		"z\3{\3{\3{\3{\3{\3|\5|\u0bab\n|\3|\3|\3}\3}\3}\5}\u0bb2\n}\3~\3~\3~\3"+
		"~\3~\3~\3~\3~\3~\3~\3~\3~\3~\3~\3~\5~\u0bc3\n~\3~\3~\5~\u0bc7\n~\3~\3"+
		"~\3~\3~\5~\u0bcd\n~\3~\3~\3~\3~\5~\u0bd3\n~\3~\3~\3~\3~\3~\7~\u0bda\n"+
		"~\f~\16~\u0bdd\13~\3~\5~\u0be0\n~\5~\u0be2\n~\3\177\3\177\3\177\7\177"+
		"\u0be7\n\177\f\177\16\177\u0bea\13\177\3\u0080\3\u0080\3\u0080\3\u0080"+
		"\5\u0080\u0bf0\n\u0080\3\u0080\5\u0080\u0bf3\n\u0080\3\u0080\5\u0080\u0bf6"+
		"\n\u0080\3\u0081\3\u0081\3\u0081\7\u0081\u0bfb\n\u0081\f\u0081\16\u0081"+
		"\u0bfe\13\u0081\3\u0082\3\u0082\3\u0082\3\u0082\5\u0082\u0c04\n\u0082"+
		"\3\u0082\5\u0082\u0c07\n\u0082\3\u0083\3\u0083\3\u0083\7\u0083\u0c0c\n"+
		"\u0083\f\u0083\16\u0083\u0c0f\13\u0083\3\u0084\3\u0084\5\u0084\u0c13\n"+
		"\u0084\3\u0084\3\u0084\3\u0084\5\u0084\u0c18\n\u0084\3\u0084\5\u0084\u0c1b"+
		"\n\u0084\3\u0085\3\u0085\3\u0085\3\u0085\3\u0085\3\u0086\3\u0086\3\u0086"+
		"\3\u0086\7\u0086\u0c26\n\u0086\f\u0086\16\u0086\u0c29\13\u0086\3\u0087"+
		"\3\u0087\3\u0087\3\u0087\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088"+
		"\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\7\u0088\u0c3a\n\u0088\f\u0088"+
		"\16\u0088\u0c3d\13\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\7\u0088"+
		"\u0c44\n\u0088\f\u0088\16\u0088\u0c47\13\u0088\5\u0088\u0c49\n\u0088\3"+
		"\u0088\3\u0088\3\u0088\3\u0088\3\u0088\7\u0088\u0c50\n\u0088\f\u0088\16"+
		"\u0088\u0c53\13\u0088\5\u0088\u0c55\n\u0088\5\u0088\u0c57\n\u0088\3\u0088"+
		"\5\u0088\u0c5a\n\u0088\3\u0088\5\u0088\u0c5d\n\u0088\3\u0089\3\u0089\3"+
		"\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089"+
		"\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\5\u0089\u0c6f\n\u0089\3\u008a"+
		"\3\u008a\3\u008a\3\u008a\3\u008a\3\u008a\3\u008a\5\u008a\u0c78\n\u008a"+
		"\3\u008b\3\u008b\3\u008b\7\u008b\u0c7d\n\u008b\f\u008b\16\u008b\u0c80"+
		"\13\u008b\3\u008c\3\u008c\3\u008c\3\u008c\5\u008c\u0c86\n\u008c\3\u008d"+
		"\3\u008d\3\u008d\7\u008d\u0c8b\n\u008d\f\u008d\16\u008d\u0c8e\13\u008d"+
		"\3\u008e\3\u008e\3\u008e\3\u008f\3\u008f\6\u008f\u0c95\n\u008f\r\u008f"+
		"\16\u008f\u0c96\3\u008f\5\u008f\u0c9a\n\u008f\3\u0090\3\u0090\3\u0090"+
		"\5\u0090\u0c9f\n\u0090\3\u0091\3\u0091\3\u0091\3\u0091\3\u0091\3\u0091"+
		"\5\u0091\u0ca7\n\u0091\3\u0092\3\u0092\3\u0093\3\u0093\5\u0093\u0cad\n"+
		"\u0093\3\u0093\3\u0093\3\u0093\5\u0093\u0cb2\n\u0093\3\u0093\3\u0093\3"+
		"\u0093\5\u0093\u0cb7\n\u0093\3\u0093\3\u0093\5\u0093\u0cbb\n\u0093\3\u0093"+
		"\3\u0093\5\u0093\u0cbf\n\u0093\3\u0093\3\u0093\5\u0093\u0cc3\n\u0093\3"+
		"\u0093\3\u0093\5\u0093\u0cc7\n\u0093\3\u0093\3\u0093\5\u0093\u0ccb\n\u0093"+
		"\3\u0093\3\u0093\5\u0093\u0ccf\n\u0093\3\u0093\3\u0093\5\u0093\u0cd3\n"+
		"\u0093\3\u0093\5\u0093\u0cd6\n\u0093\3\u0094\3\u0094\3\u0094\3\u0094\3"+
		"\u0094\3\u0094\3\u0094\5\u0094\u0cdf\n\u0094\3\u0095\3\u0095\3\u0096\3"+
		"\u0096\3\u0097\3\u0097\3\u0097\13\u03c0\u0401\u0409\u041a\u0428\u0431"+
		"\u043a\u0443\u046f\6\\\u00d8\u00dc\u00e0\u0098\2\4\6\b\n\f\16\20\22\24"+
		"\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtv"+
		"xz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094"+
		"\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac"+
		"\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4"+
		"\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc"+
		"\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4"+
		"\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108\u010a\u010c"+
		"\u010e\u0110\u0112\u0114\u0116\u0118\u011a\u011c\u011e\u0120\u0122\u0124"+
		"\u0126\u0128\u012a\u012c\2=\4\2GG\u00c0\u00c0\4\2!!\u00d1\u00d1\4\2kk"+
		"xx\3\2./\4\2\u00f6\u00f6\u011a\u011a\4\2\20\20&&\7\2++\67\67]]jj\u0095"+
		"\u0095\3\2KL\4\2]]jj\4\2\u00a4\u00a4\u013a\u013a\5\2\r\rSS\u00f3\u00f3"+
		"\4\2\r\r\u008f\u008f\4\2\u0091\u0091\u013a\u013a\5\2CC\u009f\u009f\u00dc"+
		"\u00dc\5\2DD\u00a0\u00a0\u00dd\u00dd\6\2XX\177\177\u00e5\u00e5\u010f\u010f"+
		"\5\2XX\u00e5\u00e5\u010f\u010f\4\2\30\30KK\4\2ee\u0086\u0086\4\2\u00f5"+
		"\u00f5\u0119\u0119\4\2\u013a\u013a\u013e\u013e\4\2\u00f4\u00f4\u00fe\u00fe"+
		"\4\2::\u00d8\u00d8\4\2\17\17PP\4\2\u013e\u013e\u0140\u0140\3\2\u008b\u008c"+
		"\5\2\17\17\24\24\u00e9\u00e9\5\2``\u0108\u0108\u0111\u0111\4\2\u012c\u012d"+
		"\u0131\u0131\4\2RR\u012e\u0130\4\2\u012c\u012d\u0134\u0134\t\2@Att\u009a"+
		"\u009d\u00c2\u00c2\u00db\u00db\u011c\u011c\u0122\u0122\4\2<<>?\4\2EE\u00ff"+
		"\u00ff\4\2FF\u0100\u0100\4\2##\u010a\u010a\4\2vv\u00d0\u00d0\3\2\u00f1"+
		"\u00f2\4\2\t\tkk\4\2\t\tgg\5\2\34\34\u0089\u0089\u0103\u0103\3\2\u00b7"+
		"\u00b8\3\2\u0124\u012b\4\2RR\u012c\u0135\6\2\22\22xx\u00a3\u00a3\u00ab"+
		"\u00ab\4\2``\u0108\u0108\3\2\u012c\u012d\5\2\u013a\u013a\u013e\u013e\u0140"+
		"\u0140\4\2\u009d\u009d\u0122\u0122\6\2@@tt\u009c\u009c\u00db\u00db\5\2"+
		"tt\u009c\u009c\u00db\u00db\4\2QQ\u00b4\u00b4\4\2\u00ac\u00ac\u00ea\u00ea"+
		"\4\2ff\u00bd\u00bd\3\2\u013f\u0140\4\2SS\u00e4\u00e4\65\2\r\16\20\21\23"+
		"\23\25\26\30\31\33\33\35!$&(+--/\65\67\67:;@OQSWWY_bbdfijmorrtwyz|~\u0080"+
		"\u0080\u0083\u0083\u0085\u0086\u0088\u0088\u008b\u00a0\u00a2\u00a2\u00a5"+
		"\u00a6\u00a9\u00aa\u00ad\u00ad\u00af\u00b0\u00b2\u00b6\u00b9\u00bd\u00bf"+
		"\u00c8\u00ca\u00d2\u00d4\u00dd\u00df\u00e2\u00e4\u00e8\u00ea\u00f5\u00f7"+
		"\u00fb\u00fe\u0100\u0102\u0102\u0104\u010e\u0112\u0115\u0118\u011c\u011f"+
		"\u011f\u0122\u0123\22\2\23\2399XXll{{\177\177\u0084\u0084\u0087\u0087"+
		"\u008a\u008a\u00a1\u00a1\u00a7\u00a7\u00d3\u00d3\u00df\u00df\u00e5\u00e5"+
		"\u010f\u010f\u0117\u0117\23\2\r\22\248:WYkmz|~\u0080\u0083\u0085\u0086"+
		"\u0088\u0089\u008b\u00a0\u00a2\u00a6\u00a8\u00d2\u00d4\u00de\u00e0\u00e4"+
		"\u00e6\u010e\u0110\u0116\u0118\u0123\2\u0ee4\2\u012e\3\2\2\2\4\u0130\3"+
		"\2\2\2\6\u0136\3\2\2\2\b\u013f\3\2\2\2\n\u0142\3\2\2\2\f\u0145\3\2\2\2"+
		"\16\u0148\3\2\2\2\20\u014b\3\2\2\2\22\u014e\3\2\2\2\24\u0472\3\2\2\2\26"+
		"\u0474\3\2\2\2\30\u0476\3\2\2\2\32\u0520\3\2\2\2\34\u0522\3\2\2\2\36\u0533"+
		"\3\2\2\2 \u0539\3\2\2\2\"\u0545\3\2\2\2$\u0552\3\2\2\2&\u0555\3\2\2\2"+
		"(\u0559\3\2\2\2*\u059b\3\2\2\2,\u059d\3\2\2\2.\u05a1\3\2\2\2\60\u05ad"+
		"\3\2\2\2\62\u05b2\3\2\2\2\64\u05b4\3\2\2\2\66\u05bb\3\2\2\28\u05bd\3\2"+
		"\2\2:\u05c5\3\2\2\2<\u05ce\3\2\2\2>\u05d9\3\2\2\2@\u05eb\3\2\2\2B\u05ee"+
		"\3\2\2\2D\u05f9\3\2\2\2F\u0609\3\2\2\2H\u060f\3\2\2\2J\u0611\3\2\2\2L"+
		"\u061c\3\2\2\2N\u062d\3\2\2\2P\u0634\3\2\2\2R\u0636\3\2\2\2T\u063c\3\2"+
		"\2\2V\u0671\3\2\2\2X\u067d\3\2\2\2Z\u06ad\3\2\2\2\\\u06b0\3\2\2\2^\u06d6"+
		"\3\2\2\2`\u06d8\3\2\2\2b\u06e0\3\2\2\2d\u0701\3\2\2\2f\u072f\3\2\2\2h"+
		"\u0744\3\2\2\2j\u0764\3\2\2\2l\u0770\3\2\2\2n\u0773\3\2\2\2p\u077c\3\2"+
		"\2\2r\u078d\3\2\2\2t\u07a1\3\2\2\2v\u07a3\3\2\2\2x\u07ab\3\2\2\2z\u07af"+
		"\3\2\2\2|\u07b2\3\2\2\2~\u07b5\3\2\2\2\u0080\u07cf\3\2\2\2\u0082\u07d1"+
		"\3\2\2\2\u0084\u07f1\3\2\2\2\u0086\u081a\3\2\2\2\u0088\u081e\3\2\2\2\u008a"+
		"\u0839\3\2\2\2\u008c\u083d\3\2\2\2\u008e\u084c\3\2\2\2\u0090\u084e\3\2"+
		"\2\2\u0092\u086c\3\2\2\2\u0094\u086e\3\2\2\2\u0096\u0875\3\2\2\2\u0098"+
		"\u0895\3\2\2\2\u009a\u0898\3\2\2\2\u009c\u08b2\3\2\2\2\u009e\u08cc\3\2"+
		"\2\2\u00a0\u08d2\3\2\2\2\u00a2\u08d4\3\2\2\2\u00a4\u08f8\3\2\2\2\u00a6"+
		"\u08fa\3\2\2\2\u00a8\u08fe\3\2\2\2\u00aa\u0906\3\2\2\2\u00ac\u0911\3\2"+
		"\2\2\u00ae\u0915\3\2\2\2\u00b0\u0920\3\2\2\2\u00b2\u093f\3\2\2\2\u00b4"+
		"\u0941\3\2\2\2\u00b6\u094c\3\2\2\2\u00b8\u0962\3\2\2\2\u00ba\u0995\3\2"+
		"\2\2\u00bc\u0997\3\2\2\2\u00be\u099f\3\2\2\2\u00c0\u09a7\3\2\2\2\u00c2"+
		"\u09af\3\2\2\2\u00c4\u09b7\3\2\2\2\u00c6\u09be\3\2\2\2\u00c8\u09c2\3\2"+
		"\2\2\u00ca\u09cc\3\2\2\2\u00cc\u09d4\3\2\2\2\u00ce\u09e1\3\2\2\2\u00d0"+
		"\u09f0\3\2\2\2\u00d2\u09f4\3\2\2\2\u00d4\u09f6\3\2\2\2\u00d6\u09f8\3\2"+
		"\2\2\u00d8\u0a0c\3\2\2\2\u00da\u0a6b\3\2\2\2\u00dc\u0a71\3\2\2\2\u00de"+
		"\u0a8b\3\2\2\2\u00e0\u0b6a\3\2\2\2\u00e2\u0b85\3\2\2\2\u00e4\u0b87\3\2"+
		"\2\2\u00e6\u0b89\3\2\2\2\u00e8\u0b8b\3\2\2\2\u00ea\u0b8d\3\2\2\2\u00ec"+
		"\u0b8f\3\2\2\2\u00ee\u0b94\3\2\2\2\u00f0\u0b9b\3\2\2\2\u00f2\u0b9f\3\2"+
		"\2\2\u00f4\u0ba4\3\2\2\2\u00f6\u0baa\3\2\2\2\u00f8\u0bb1\3\2\2\2\u00fa"+
		"\u0be1\3\2\2\2\u00fc\u0be3\3\2\2\2\u00fe\u0beb\3\2\2\2\u0100\u0bf7\3\2"+
		"\2\2\u0102\u0bff\3\2\2\2\u0104\u0c08\3\2\2\2\u0106\u0c10\3\2\2\2\u0108"+
		"\u0c1c\3\2\2\2\u010a\u0c21\3\2\2\2\u010c\u0c2a\3\2\2\2\u010e\u0c5c\3\2"+
		"\2\2\u0110\u0c6e\3\2\2\2\u0112\u0c77\3\2\2\2\u0114\u0c79\3\2\2\2\u0116"+
		"\u0c85\3\2\2\2\u0118\u0c87\3\2\2\2\u011a\u0c8f\3\2\2\2\u011c\u0c99\3\2"+
		"\2\2\u011e\u0c9e\3\2\2\2\u0120\u0ca6\3\2\2\2\u0122\u0ca8\3\2\2\2\u0124"+
		"\u0cd5\3\2\2\2\u0126\u0cde\3\2\2\2\u0128\u0ce0\3\2\2\2\u012a\u0ce2\3\2"+
		"\2\2\u012c\u0ce4\3\2\2\2\u012e\u012f\5\4\3\2\u012f\3\3\2\2\2\u0130\u0131"+
		"\7\4\2\2\u0131\u0132\5\u00be`\2\u0132\u0133\7\u0101\2\2\u0133\u0134\7"+
		"\5\2\2\u0134\u0135\5\u00be`\2\u0135\5\3\2\2\2\u0136\u013a\5\24\13\2\u0137"+
		"\u0139\7\6\2\2\u0138\u0137\3\2\2\2\u0139\u013c\3\2\2\2\u013a\u0138\3\2"+
		"\2\2\u013a\u013b\3\2\2\2\u013b\u013d\3\2\2\2\u013c\u013a\3\2\2\2\u013d"+
		"\u013e\7\2\2\3\u013e\7\3\2\2\2\u013f\u0140\5\u00c8e\2\u0140\u0141\7\2"+
		"\2\3\u0141\t\3\2\2\2\u0142\u0143\5\u00c4c\2\u0143\u0144\7\2\2\3\u0144"+
		"\13\3\2\2\2\u0145\u0146\5\u00be`\2\u0146\u0147\7\2\2\3\u0147\r\3\2\2\2"+
		"\u0148\u0149\5\u00c6d\2\u0149\u014a\7\2\2\3\u014a\17\3\2\2\2\u014b\u014c"+
		"\5\u00fa~\2\u014c\u014d\7\2\2\3\u014d\21\3\2\2\2\u014e\u014f\5\u0100\u0081"+
		"\2\u014f\u0150\7\2\2\3\u0150\23\3\2\2\2\u0151\u0473\5(\25\2\u0152\u0154"+
		"\5:\36\2\u0153\u0152\3\2\2\2\u0153\u0154\3\2\2\2\u0154\u0155\3\2\2\2\u0155"+
		"\u0473\5V,\2\u0156\u0157\7\u0115\2\2\u0157\u0473\5\u00be`\2\u0158\u0159"+
		"\7\u0115\2\2\u0159\u015a\5\62\32\2\u015a\u015b\5\u00be`\2\u015b\u0473"+
		"\3\2\2\2\u015c\u015d\7\u00e4\2\2\u015d\u0160\7$\2\2\u015e\u0161\5\u011e"+
		"\u0090\2\u015f\u0161\7\u013a\2\2\u0160\u015e\3\2\2\2\u0160\u015f\3\2\2"+
		"\2\u0161\u0473\3\2\2\2\u0162\u0163\78\2\2\u0163\u0167\5\62\32\2\u0164"+
		"\u0165\7u\2\2\u0165\u0166\7\u00a3\2\2\u0166\u0168\7Z\2\2\u0167\u0164\3"+
		"\2\2\2\u0167\u0168\3\2\2\2\u0168\u0169\3\2\2\2\u0169\u0171\5\u00be`\2"+
		"\u016a\u0170\5&\24\2\u016b\u0170\5$\23\2\u016c\u016d\7\u0120\2\2\u016d"+
		"\u016e\t\2\2\2\u016e\u0170\5B\"\2\u016f\u016a\3\2\2\2\u016f\u016b\3\2"+
		"\2\2\u016f\u016c\3\2\2\2\u0170\u0173\3\2\2\2\u0171\u016f\3\2\2\2\u0171"+
		"\u0172\3\2\2\2\u0172\u0473\3\2\2\2\u0173\u0171\3\2\2\2\u0174\u0175\7\20"+
		"\2\2\u0175\u0176\5\62\32\2\u0176\u0177\5\u00be`\2\u0177\u0178\7\u00e4"+
		"\2\2\u0178\u0179\t\2\2\2\u0179\u017a\5B\"\2\u017a\u0473\3\2\2\2\u017b"+
		"\u017c\7\20\2\2\u017c\u017d\5\62\32\2\u017d\u017e\5\u00be`\2\u017e\u017f"+
		"\7\u00e4\2\2\u017f\u0180\5$\23\2\u0180\u0473\3\2\2\2\u0181\u0182\7S\2"+
		"\2\u0182\u0185\5\62\32\2\u0183\u0184\7u\2\2\u0184\u0186\7Z\2\2\u0185\u0183"+
		"\3\2\2\2\u0185\u0186\3\2\2\2\u0186\u0187\3\2\2\2\u0187\u0189\5\u00be`"+
		"\2\u0188\u018a\t\3\2\2\u0189\u0188\3\2\2\2\u0189\u018a\3\2\2\2\u018a\u0473"+
		"\3\2\2\2\u018b\u018c\7\u00e7\2\2\u018c\u018f\5\64\33\2\u018d\u018e\t\4"+
		"\2\2\u018e\u0190\5\u00be`\2\u018f\u018d\3\2\2\2\u018f\u0190\3\2\2\2\u0190"+
		"\u0195\3\2\2\2\u0191\u0193\7\u008b\2\2\u0192\u0191\3\2\2\2\u0192\u0193"+
		"\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0196\7\u013a\2\2\u0195\u0192\3\2\2"+
		"\2\u0195\u0196\3\2\2\2\u0196\u0473\3\2\2\2\u0197\u019c\5\34\17\2\u0198"+
		"\u0199\7\7\2\2\u0199\u019a\5\u0100\u0081\2\u019a\u019b\7\b\2\2\u019b\u019d"+
		"\3\2\2\2\u019c\u0198\3\2\2\2\u019c\u019d\3\2\2\2\u019d\u019f\3\2\2\2\u019e"+
		"\u01a0\5> \2\u019f\u019e\3\2\2\2\u019f\u01a0\3\2\2\2\u01a0\u01a1\3\2\2"+
		"\2\u01a1\u01a6\5@!\2\u01a2\u01a4\7\27\2\2\u01a3\u01a2\3\2\2\2\u01a3\u01a4"+
		"\3\2\2\2\u01a4\u01a5\3\2\2\2\u01a5\u01a7\5(\25\2\u01a6\u01a3\3\2\2\2\u01a6"+
		"\u01a7\3\2\2\2\u01a7\u0473\3\2\2\2\u01a8\u01a9\78\2\2\u01a9\u01ad\7\u00f6"+
		"\2\2\u01aa\u01ab\7u\2\2\u01ab\u01ac\7\u00a3\2\2\u01ac\u01ae\7Z\2\2\u01ad"+
		"\u01aa\3\2\2\2\u01ad\u01ae\3\2\2\2\u01ae\u01af\3\2\2\2\u01af\u01b0\5\u00c4"+
		"c\2\u01b0\u01b1\7\u008b\2\2\u01b1\u01ba\5\u00c4c\2\u01b2\u01b9\5> \2\u01b3"+
		"\u01b9\5\u00ba^\2\u01b4\u01b9\5N(\2\u01b5\u01b9\5$\23\2\u01b6\u01b7\7"+
		"\u00f9\2\2\u01b7\u01b9\5B\"\2\u01b8\u01b2\3\2\2\2\u01b8\u01b3\3\2\2\2"+
		"\u01b8\u01b4\3\2\2\2\u01b8\u01b5\3\2\2\2\u01b8\u01b6\3\2\2\2\u01b9\u01bc"+
		"\3\2\2\2\u01ba\u01b8\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb\u0473\3\2\2\2\u01bc"+
		"\u01ba\3\2\2\2\u01bd\u01c2\5\36\20\2\u01be\u01bf\7\7\2\2\u01bf\u01c0\5"+
		"\u0100\u0081\2\u01c0\u01c1\7\b\2\2\u01c1\u01c3\3\2\2\2\u01c2\u01be\3\2"+
		"\2\2\u01c2\u01c3\3\2\2\2\u01c3\u01c5\3\2\2\2\u01c4\u01c6\5> \2\u01c5\u01c4"+
		"\3\2\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c7\3\2\2\2\u01c7\u01cc\5@!\2\u01c8"+
		"\u01ca\7\27\2\2\u01c9\u01c8\3\2\2\2\u01c9\u01ca\3\2\2\2\u01ca\u01cb\3"+
		"\2\2\2\u01cb\u01cd\5(\25\2\u01cc\u01c9\3\2\2\2\u01cc\u01cd\3\2\2\2\u01cd"+
		"\u0473\3\2\2\2\u01ce\u01cf\7\21\2\2\u01cf\u01d0\7\u00f6\2\2\u01d0\u01d2"+
		"\5\u00be`\2\u01d1\u01d3\5.\30\2\u01d2\u01d1\3\2\2\2\u01d2\u01d3\3\2\2"+
		"\2\u01d3\u01d4\3\2\2\2\u01d4\u01d5\7\64\2\2\u01d5\u01dd\7\u00ed\2\2\u01d6"+
		"\u01de\5\u011e\u0090\2\u01d7\u01d8\7g\2\2\u01d8\u01d9\7/\2\2\u01d9\u01de"+
		"\5\u00a8U\2\u01da\u01db\7g\2\2\u01db\u01dc\7\17\2\2\u01dc\u01de\7/\2\2"+
		"\u01dd\u01d6\3\2\2\2\u01dd\u01d7\3\2\2\2\u01dd\u01da\3\2\2\2\u01dd\u01de"+
		"\3\2\2\2\u01de\u0473\3\2\2\2\u01df\u01e0\7\21\2\2\u01e0\u01e3\7\u00f7"+
		"\2\2\u01e1\u01e2\t\4\2\2\u01e2\u01e4\5\u00be`\2\u01e3\u01e1\3\2\2\2\u01e3"+
		"\u01e4\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5\u01e6\7\64\2\2\u01e6\u01e8\7"+
		"\u00ed\2\2\u01e7\u01e9\5\u011e\u0090\2\u01e8\u01e7\3\2\2\2\u01e8\u01e9"+
		"\3\2\2\2\u01e9\u0473\3\2\2\2\u01ea\u01eb\7\20\2\2\u01eb\u01ec\7\u00f6"+
		"\2\2\u01ec\u01ed\5\u00be`\2\u01ed\u01ee\7\r\2\2\u01ee\u01ef\t\5\2\2\u01ef"+
		"\u01f0\5\u00fc\177\2\u01f0\u0473\3\2\2\2\u01f1\u01f2\7\20\2\2\u01f2\u01f3"+
		"\7\u00f6\2\2\u01f3\u01f4\5\u00be`\2\u01f4\u01f5\7\r\2\2\u01f5\u01f6\t"+
		"\5\2\2\u01f6\u01f7\7\7\2\2\u01f7\u01f8\5\u00fc\177\2\u01f8\u01f9\7\b\2"+
		"\2\u01f9\u0473\3\2\2\2\u01fa\u01fb\7\20\2\2\u01fb\u01fc\7\u00f6\2\2\u01fc"+
		"\u01fd\5\u00be`\2\u01fd\u01fe\7\u00cb\2\2\u01fe\u01ff\7.\2\2\u01ff\u0200"+
		"\5\u00be`\2\u0200\u0201\7\u0101\2\2\u0201\u0202\5\u011a\u008e\2\u0202"+
		"\u0473\3\2\2\2\u0203\u0204\7\20\2\2\u0204\u0205\7\u00f6\2\2\u0205\u0206"+
		"\5\u00be`\2\u0206\u0207\7S\2\2\u0207\u020a\t\5\2\2\u0208\u0209\7u\2\2"+
		"\u0209\u020b\7Z\2\2\u020a\u0208\3\2\2\2\u020a\u020b\3\2\2\2\u020b\u020c"+
		"\3\2\2\2\u020c\u020d\7\7\2\2\u020d\u020e\5\u00bc_\2\u020e\u020f\7\b\2"+
		"\2\u020f\u0473\3\2\2\2\u0210\u0211\7\20\2\2\u0211\u0212\7\u00f6\2\2\u0212"+
		"\u0213\5\u00be`\2\u0213\u0214\7S\2\2\u0214\u0217\t\5\2\2\u0215\u0216\7"+
		"u\2\2\u0216\u0218\7Z\2\2\u0217\u0215\3\2\2\2\u0217\u0218\3\2\2\2\u0218"+
		"\u0219\3\2\2\2\u0219\u021a\5\u00bc_\2\u021a\u0473\3\2\2\2\u021b\u021c"+
		"\7\20\2\2\u021c\u021d\t\6\2\2\u021d\u021e\5\u00be`\2\u021e\u021f\7\u00cb"+
		"\2\2\u021f\u0220\7\u0101\2\2\u0220\u0221\5\u00be`\2\u0221\u0473\3\2\2"+
		"\2\u0222\u0223\7\20\2\2\u0223\u0224\t\6\2\2\u0224\u0225\5\u00be`\2\u0225"+
		"\u0226\7\u00e4\2\2\u0226\u0227\7\u00f9\2\2\u0227\u0228\5B\"\2\u0228\u0473"+
		"\3\2\2\2\u0229\u022a\7\20\2\2\u022a\u022b\t\6\2\2\u022b\u022c\5\u00be"+
		"`\2\u022c\u022d\7\u0113\2\2\u022d\u0230\7\u00f9\2\2\u022e\u022f\7u\2\2"+
		"\u022f\u0231\7Z\2\2\u0230\u022e\3\2\2\2\u0230\u0231\3\2\2\2\u0231\u0232"+
		"\3\2\2\2\u0232\u0233\5B\"\2\u0233\u0473\3\2\2\2\u0234\u0235\7\20\2\2\u0235"+
		"\u0236\7\u00f6\2\2\u0236\u0237\5\u00be`\2\u0237\u0239\t\7\2\2\u0238\u023a"+
		"\7.\2\2\u0239\u0238\3\2\2\2\u0239\u023a\3\2\2\2\u023a\u023b\3\2\2\2\u023b"+
		"\u023d\5\u00be`\2\u023c\u023e\5\u0126\u0094\2\u023d\u023c\3\2\2\2\u023d"+
		"\u023e\3\2\2\2\u023e\u0473\3\2\2\2\u023f\u0240\7\20\2\2\u0240\u0241\7"+
		"\u00f6\2\2\u0241\u0243\5\u00be`\2\u0242\u0244\5.\30\2\u0243\u0242\3\2"+
		"\2\2\u0243\u0244\3\2\2\2\u0244\u0245\3\2\2\2\u0245\u0247\7&\2\2\u0246"+
		"\u0248\7.\2\2\u0247\u0246\3\2\2\2\u0247\u0248\3\2\2\2\u0248\u0249\3\2"+
		"\2\2\u0249\u024a\5\u00be`\2\u024a\u024c\5\u0102\u0082\2\u024b\u024d\5"+
		"\u00f8}\2\u024c\u024b\3\2\2\2\u024c\u024d\3\2\2\2\u024d\u0473\3\2\2\2"+
		"\u024e\u024f\7\20\2\2\u024f\u0250\7\u00f6\2\2\u0250\u0252\5\u00be`\2\u0251"+
		"\u0253\5.\30\2\u0252\u0251\3\2\2\2\u0252\u0253\3\2\2\2\u0253\u0254\3\2"+
		"\2\2\u0254\u0255\7\u00ce\2\2\u0255\u0256\7/\2\2\u0256\u0257\7\7\2\2\u0257"+
		"\u0258\5\u00fc\177\2\u0258\u0259\7\b\2\2\u0259\u0473\3\2\2\2\u025a\u025b"+
		"\7\20\2\2\u025b\u025c\7\u00f6\2\2\u025c\u025e\5\u00be`\2\u025d\u025f\5"+
		".\30\2\u025e\u025d\3\2\2\2\u025e\u025f\3\2\2\2\u025f\u0260\3\2\2\2\u0260"+
		"\u0261\7\u00e4\2\2\u0261\u0262\7\u00e1\2\2\u0262\u0266\7\u013a\2\2\u0263"+
		"\u0264\7\u0120\2\2\u0264\u0265\7\u00e2\2\2\u0265\u0267\5B\"\2\u0266\u0263"+
		"\3\2\2\2\u0266\u0267\3\2\2\2\u0267\u0473\3\2\2\2\u0268\u0269\7\20\2\2"+
		"\u0269\u026a\7\u00f6\2\2\u026a\u026c\5\u00be`\2\u026b\u026d\5.\30\2\u026c"+
		"\u026b\3\2\2\2\u026c\u026d\3\2\2\2\u026d\u026e\3\2\2\2\u026e\u026f\7\u00e4"+
		"\2\2\u026f\u0270\7\u00e2\2\2\u0270\u0271\5B\"\2\u0271\u0473\3\2\2\2\u0272"+
		"\u0273\7\20\2\2\u0273\u0274\t\6\2\2\u0274\u0275\5\u00be`\2\u0275\u0279"+
		"\7\r\2\2\u0276\u0277\7u\2\2\u0277\u0278\7\u00a3\2\2\u0278\u027a\7Z\2\2"+
		"\u0279\u0276\3\2\2\2\u0279\u027a\3\2\2\2\u027a\u027c\3\2\2\2\u027b\u027d"+
		"\5,\27\2\u027c\u027b\3\2\2\2\u027d\u027e\3\2\2\2\u027e\u027c\3\2\2\2\u027e"+
		"\u027f\3\2\2\2\u027f\u0473\3\2\2\2\u0280\u0281\7\20\2\2\u0281\u0282\7"+
		"\u00f6\2\2\u0282\u0283\5\u00be`\2\u0283\u0284\5.\30\2\u0284\u0285\7\u00cb"+
		"\2\2\u0285\u0286\7\u0101\2\2\u0286\u0287\5.\30\2\u0287\u0473\3\2\2\2\u0288"+
		"\u0289\7\20\2\2\u0289\u028a\t\6\2\2\u028a\u028b\5\u00be`\2\u028b\u028e"+
		"\7S\2\2\u028c\u028d\7u\2\2\u028d\u028f\7Z\2\2\u028e\u028c\3\2\2\2\u028e"+
		"\u028f\3\2\2\2\u028f\u0290\3\2\2\2\u0290\u0295\5.\30\2\u0291\u0292\7\t"+
		"\2\2\u0292\u0294\5.\30\2\u0293\u0291\3\2\2\2\u0294\u0297\3\2\2\2\u0295"+
		"\u0293\3\2\2\2\u0295\u0296\3\2\2\2\u0296\u0299\3\2\2\2\u0297\u0295\3\2"+
		"\2\2\u0298\u029a\7\u00c1\2\2\u0299\u0298\3\2\2\2\u0299\u029a\3\2\2\2\u029a"+
		"\u0473\3\2\2\2\u029b\u029c\7\20\2\2\u029c\u029d\7\u00f6\2\2\u029d\u029f"+
		"\5\u00be`\2\u029e\u02a0\5.\30\2\u029f\u029e\3\2\2\2\u029f\u02a0\3\2\2"+
		"\2\u02a0\u02a1\3\2\2\2\u02a1\u02a2\7\u00e4\2\2\u02a2\u02a3\5$\23\2\u02a3"+
		"\u0473\3\2\2\2\u02a4\u02a5\7\20\2\2\u02a5\u02a6\7\u00f6\2\2\u02a6\u02a7"+
		"\5\u00be`\2\u02a7\u02a8\7\u00c7\2\2\u02a8\u02a9\7\u00b6\2\2\u02a9\u0473"+
		"\3\2\2\2\u02aa\u02ab\7S\2\2\u02ab\u02ae\7\u00f6\2\2\u02ac\u02ad\7u\2\2"+
		"\u02ad\u02af\7Z\2\2\u02ae\u02ac\3\2\2\2\u02ae\u02af\3\2\2\2\u02af\u02b0"+
		"\3\2\2\2\u02b0\u02b2\5\u00be`\2\u02b1\u02b3\7\u00c1\2\2\u02b2\u02b1\3"+
		"\2\2\2\u02b2\u02b3\3\2\2\2\u02b3\u0473\3\2\2\2\u02b4\u02b5\7S\2\2\u02b5"+
		"\u02b8\7\u011a\2\2\u02b6\u02b7\7u\2\2\u02b7\u02b9\7Z\2\2\u02b8\u02b6\3"+
		"\2\2\2\u02b8\u02b9\3\2\2\2\u02b9\u02ba\3\2\2\2\u02ba\u0473\5\u00be`\2"+
		"\u02bb\u02be\78\2\2\u02bc\u02bd\7\u00ab\2\2\u02bd\u02bf\7\u00ce\2\2\u02be"+
		"\u02bc\3\2\2\2\u02be\u02bf\3\2\2\2\u02bf\u02c4\3\2\2\2\u02c0\u02c2\7o"+
		"\2\2\u02c1\u02c0\3\2\2\2\u02c1\u02c2\3\2\2\2\u02c2\u02c3\3\2\2\2\u02c3"+
		"\u02c5\7\u00fa\2\2\u02c4\u02c1\3\2\2\2\u02c4\u02c5\3\2\2\2\u02c5\u02c6"+
		"\3\2\2\2\u02c6\u02ca\7\u011a\2\2\u02c7\u02c8\7u\2\2\u02c8\u02c9\7\u00a3"+
		"\2\2\u02c9\u02cb\7Z\2\2\u02ca\u02c7\3\2\2\2\u02ca\u02cb\3\2\2\2\u02cb"+
		"\u02cc\3\2\2\2\u02cc\u02ce\5\u00be`\2\u02cd\u02cf\5\u00aeX\2\u02ce\u02cd"+
		"\3\2\2\2\u02ce\u02cf\3\2\2\2\u02cf\u02d8\3\2\2\2\u02d0\u02d7\5&\24\2\u02d1"+
		"\u02d2\7\u00b5\2\2\u02d2\u02d3\7\u00a7\2\2\u02d3\u02d7\5\u00a6T\2\u02d4"+
		"\u02d5\7\u00f9\2\2\u02d5\u02d7\5B\"\2\u02d6\u02d0\3\2\2\2\u02d6\u02d1"+
		"\3\2\2\2\u02d6\u02d4\3\2\2\2\u02d7\u02da\3\2\2\2\u02d8\u02d6\3\2\2\2\u02d8"+
		"\u02d9\3\2\2\2\u02d9\u02db\3\2\2\2\u02da\u02d8\3\2\2\2\u02db\u02dc\7\27"+
		"\2\2\u02dc\u02dd\5(\25\2\u02dd\u0473\3\2\2\2\u02de\u02e1\78\2\2\u02df"+
		"\u02e0\7\u00ab\2\2\u02e0\u02e2\7\u00ce\2\2\u02e1\u02df\3\2\2\2\u02e1\u02e2"+
		"\3\2\2\2\u02e2\u02e4\3\2\2\2\u02e3\u02e5\7o\2\2\u02e4\u02e3\3\2\2\2\u02e4"+
		"\u02e5\3\2\2\2\u02e5\u02e6\3\2\2\2\u02e6\u02e7\7\u00fa\2\2\u02e7\u02e8"+
		"\7\u011a\2\2\u02e8\u02ed\5\u00c4c\2\u02e9\u02ea\7\7\2\2\u02ea\u02eb\5"+
		"\u0100\u0081\2\u02eb\u02ec\7\b\2\2\u02ec\u02ee\3\2\2\2\u02ed\u02e9\3\2"+
		"\2\2\u02ed\u02ee\3\2\2\2\u02ee\u02ef\3\2\2\2\u02ef\u02f2\5> \2\u02f0\u02f1"+
		"\7\u00aa\2\2\u02f1\u02f3\5B\"\2\u02f2\u02f0\3\2\2\2\u02f2\u02f3\3\2\2"+
		"\2\u02f3\u0473\3\2\2\2\u02f4\u02f5\7\20\2\2\u02f5\u02f6\7\u011a\2\2\u02f6"+
		"\u02f8\5\u00be`\2\u02f7\u02f9\7\27\2\2\u02f8\u02f7\3\2\2\2\u02f8\u02f9"+
		"\3\2\2\2\u02f9\u02fa\3\2\2\2\u02fa\u02fb\5(\25\2\u02fb\u0473\3\2\2\2\u02fc"+
		"\u02ff\78\2\2\u02fd\u02fe\7\u00ab\2\2\u02fe\u0300\7\u00ce\2\2\u02ff\u02fd"+
		"\3\2\2\2\u02ff\u0300\3\2\2\2\u0300\u0302\3\2\2\2\u0301\u0303\7\u00fa\2"+
		"\2\u0302\u0301\3\2\2\2\u0302\u0303\3\2\2\2\u0303\u0304\3\2\2\2\u0304\u0308"+
		"\7m\2\2\u0305\u0306\7u\2\2\u0306\u0307\7\u00a3\2\2\u0307\u0309\7Z\2\2"+
		"\u0308\u0305\3\2\2\2\u0308\u0309\3\2\2\2\u0309\u030a\3\2\2\2\u030a\u030b"+
		"\5\u00be`\2\u030b\u030c\7\27\2\2\u030c\u0316\7\u013a\2\2\u030d\u030e\7"+
		"\u0117\2\2\u030e\u0313\5T+\2\u030f\u0310\7\t\2\2\u0310\u0312\5T+\2\u0311"+
		"\u030f\3\2\2\2\u0312\u0315\3\2\2\2\u0313\u0311\3\2\2\2\u0313\u0314\3\2"+
		"\2\2\u0314\u0317\3\2\2\2\u0315\u0313\3\2\2\2\u0316\u030d\3\2\2\2\u0316"+
		"\u0317\3\2\2\2\u0317\u0473\3\2\2\2\u0318\u031a\7S\2\2\u0319\u031b\7\u00fa"+
		"\2\2\u031a\u0319\3\2\2\2\u031a\u031b\3\2\2\2\u031b\u031c\3\2\2\2\u031c"+
		"\u031f\7m\2\2\u031d\u031e\7u\2\2\u031e\u0320\7Z\2\2\u031f\u031d\3\2\2"+
		"\2\u031f\u0320\3\2\2\2\u0320\u0321\3\2\2\2\u0321\u0473\5\u00be`\2\u0322"+
		"\u0324\7[\2\2\u0323\u0325\t\b\2\2\u0324\u0323\3\2\2\2\u0324\u0325\3\2"+
		"\2\2\u0325\u0326\3\2\2\2\u0326\u0473\5\24\13\2\u0327\u0328\7\u00e7\2\2"+
		"\u0328\u032b\7\u00f7\2\2\u0329\u032a\t\4\2\2\u032a\u032c\5\u00be`\2\u032b"+
		"\u0329\3\2\2\2\u032b\u032c\3\2\2\2\u032c\u0331\3\2\2\2\u032d\u032f\7\u008b"+
		"\2\2\u032e\u032d\3\2\2\2\u032e\u032f\3\2\2\2\u032f\u0330\3\2\2\2\u0330"+
		"\u0332\7\u013a\2\2\u0331\u032e\3\2\2\2\u0331\u0332\3\2\2\2\u0332\u0473"+
		"\3\2\2\2\u0333\u0334\7\u00e7\2\2\u0334\u0335\7\u00f6\2\2\u0335\u0338\7"+
		"]\2\2\u0336\u0337\t\4\2\2\u0337\u0339\5\u00be`\2\u0338\u0336\3\2\2\2\u0338"+
		"\u0339\3\2\2\2\u0339\u033a\3\2\2\2\u033a\u033b\7\u008b\2\2\u033b\u033d"+
		"\7\u013a\2\2\u033c\u033e\5.\30\2\u033d\u033c\3\2\2\2\u033d\u033e\3\2\2"+
		"\2\u033e\u0473\3\2\2\2\u033f\u0340\7\u00e7\2\2\u0340\u0341\7\u00f9\2\2"+
		"\u0341\u0346\5\u00be`\2\u0342\u0343\7\7\2\2\u0343\u0344\5F$\2\u0344\u0345"+
		"\7\b\2\2\u0345\u0347\3\2\2\2\u0346\u0342\3\2\2\2\u0346\u0347\3\2\2\2\u0347"+
		"\u0473\3\2\2\2\u0348\u0349\7\u00e7\2\2\u0349\u034a\7/\2\2\u034a\u034b"+
		"\t\4\2\2\u034b\u034e\5\u00be`\2\u034c\u034d\t\4\2\2\u034d\u034f\5\u00be"+
		"`\2\u034e\u034c\3\2\2\2\u034e\u034f\3\2\2\2\u034f\u0473\3\2\2\2\u0350"+
		"\u0351\7\u00e7\2\2\u0351\u0354\7\u011b\2\2\u0352\u0353\t\4\2\2\u0353\u0355"+
		"\5\u00be`\2\u0354\u0352\3\2\2\2\u0354\u0355\3\2\2\2\u0355\u035a\3\2\2"+
		"\2\u0356\u0358\7\u008b\2\2\u0357\u0356\3\2\2\2\u0357\u0358\3\2\2\2\u0358"+
		"\u0359\3\2\2\2\u0359\u035b\7\u013a\2\2\u035a\u0357\3\2\2\2\u035a\u035b"+
		"\3\2\2\2\u035b\u0473\3\2\2\2\u035c\u035d\7\u00e7\2\2\u035d\u035e\7\u00b6"+
		"\2\2\u035e\u0360\5\u00be`\2\u035f\u0361\5.\30\2\u0360\u035f\3\2\2\2\u0360"+
		"\u0361\3\2\2\2\u0361\u0473\3\2\2\2\u0362\u0364\7\u00e7\2\2\u0363\u0365"+
		"\5\u011e\u0090\2\u0364\u0363\3\2\2\2\u0364\u0365\3\2\2\2\u0365\u0366\3"+
		"\2\2\2\u0366\u0369\7n\2\2\u0367\u0368\t\4\2\2\u0368\u036a\5\u00be`\2\u0369"+
		"\u0367\3\2\2\2\u0369\u036a\3\2\2\2\u036a\u0372\3\2\2\2\u036b\u036d\7\u008b"+
		"\2\2\u036c\u036b\3\2\2\2\u036c\u036d\3\2\2\2\u036d\u0370\3\2\2\2\u036e"+
		"\u0371\5\u00be`\2\u036f\u0371\7\u013a\2\2\u0370\u036e\3\2\2\2\u0370\u036f"+
		"\3\2\2\2\u0371\u0373\3\2\2\2\u0372\u036c\3\2\2\2\u0372\u0373\3\2\2\2\u0373"+
		"\u0473\3\2\2\2\u0374\u0375\7\u00e7\2\2\u0375\u0376\78\2\2\u0376\u0377"+
		"\7\u00f6\2\2\u0377\u037a\5\u00be`\2\u0378\u0379\7\27\2\2\u0379\u037b\7"+
		"\u00e1\2\2\u037a\u0378\3\2\2\2\u037a\u037b\3\2\2\2\u037b\u0473\3\2\2\2"+
		"\u037c\u037d\7\u00e7\2\2\u037d\u037e\7;\2\2\u037e\u0473\5\62\32\2\u037f"+
		"\u0380\7\u00e7\2\2\u0380\u0385\7%\2\2\u0381\u0383\7\u008b\2\2\u0382\u0381"+
		"\3\2\2\2\u0382\u0383\3\2\2\2\u0383\u0384\3\2\2\2\u0384\u0386\7\u013a\2"+
		"\2\u0385\u0382\3\2\2\2\u0385\u0386\3\2\2\2\u0386\u0473\3\2\2\2\u0387\u0388"+
		"\t\t\2\2\u0388\u038a\7m\2\2\u0389\u038b\7]\2\2\u038a\u0389\3\2\2\2\u038a"+
		"\u038b\3\2\2\2\u038b\u038c\3\2\2\2\u038c\u0473\5\66\34\2\u038d\u038e\t"+
		"\t\2\2\u038e\u0390\5\62\32\2\u038f\u0391\7]\2\2\u0390\u038f\3\2\2\2\u0390"+
		"\u0391\3\2\2\2\u0391\u0392\3\2\2\2\u0392\u0393\5\u00be`\2\u0393\u0473"+
		"\3\2\2\2\u0394\u0396\t\t\2\2\u0395\u0397\7\u00f6\2\2\u0396\u0395\3\2\2"+
		"\2\u0396\u0397\3\2\2\2\u0397\u0399\3\2\2\2\u0398\u039a\t\n\2\2\u0399\u0398"+
		"\3\2\2\2\u0399\u039a\3\2\2\2\u039a\u039b\3\2\2\2\u039b\u039d\5\u00be`"+
		"\2\u039c\u039e\5.\30\2\u039d\u039c\3\2\2\2\u039d\u039e\3\2\2\2\u039e\u03a0"+
		"\3\2\2\2\u039f\u03a1\58\35\2\u03a0\u039f\3\2\2\2\u03a0\u03a1\3\2\2\2\u03a1"+
		"\u0473\3\2\2\2\u03a2\u03a4\t\t\2\2\u03a3\u03a5\7\u00c3\2\2\u03a4\u03a3"+
		"\3\2\2\2\u03a4\u03a5\3\2\2\2\u03a5\u03a6\3\2\2\2\u03a6\u0473\5(\25\2\u03a7"+
		"\u03a8\7\60\2\2\u03a8\u03a9\7\u00a7\2\2\u03a9\u03aa\5\62\32\2\u03aa\u03ab"+
		"\5\u00be`\2\u03ab\u03ac\7\u0082\2\2\u03ac\u03ad\t\13\2\2\u03ad\u0473\3"+
		"\2\2\2\u03ae\u03af\7\60\2\2\u03af\u03b0\7\u00a7\2\2\u03b0\u03b1\7\u00f6"+
		"\2\2\u03b1\u03b2\5\u00be`\2\u03b2\u03b3\7\u0082\2\2\u03b3\u03b4\t\13\2"+
		"\2\u03b4\u0473\3\2\2\2\u03b5\u03b6\7\u00ca\2\2\u03b6\u03b7\7\u00f6\2\2"+
		"\u03b7\u0473\5\u00be`\2\u03b8\u03b9\7\u00ca\2\2\u03b9\u03ba\7m\2\2\u03ba"+
		"\u0473\5\u00be`\2\u03bb\u03c3\7\u00ca\2\2\u03bc\u03c4\7\u013a\2\2\u03bd"+
		"\u03bf\13\2\2\2\u03be\u03bd\3\2\2\2\u03bf\u03c2\3\2\2\2\u03c0\u03c1\3"+
		"\2\2\2\u03c0\u03be\3\2\2\2\u03c1\u03c4\3\2\2\2\u03c2\u03c0\3\2\2\2\u03c3"+
		"\u03bc\3\2\2\2\u03c3\u03c0\3\2\2\2\u03c4\u0473\3\2\2\2\u03c5\u03c7\7 "+
		"\2\2\u03c6\u03c8\7\u0088\2\2\u03c7\u03c6\3\2\2\2\u03c7\u03c8\3\2\2\2\u03c8"+
		"\u03c9\3\2\2\2\u03c9\u03ca\7\u00f6\2\2\u03ca\u03cd\5\u00be`\2\u03cb\u03cc"+
		"\7\u00aa\2\2\u03cc\u03ce\5B\"\2\u03cd\u03cb\3\2\2\2\u03cd\u03ce\3\2\2"+
		"\2\u03ce\u03d3\3\2\2\2\u03cf\u03d1\7\27\2\2\u03d0\u03cf\3\2\2\2\u03d0"+
		"\u03d1\3\2\2\2\u03d1\u03d2\3\2\2\2\u03d2\u03d4\5(\25\2\u03d3\u03d0\3\2"+
		"\2\2\u03d3\u03d4\3\2\2\2\u03d4\u0473\3\2\2\2\u03d5\u03d6\7\u010e\2\2\u03d6"+
		"\u03d9\7\u00f6\2\2\u03d7\u03d8\7u\2\2\u03d8\u03da\7Z\2\2\u03d9\u03d7\3"+
		"\2\2\2\u03d9\u03da\3\2\2\2\u03da\u03db\3\2\2\2\u03db\u0473\5\u00be`\2"+
		"\u03dc\u03dd\7(\2\2\u03dd\u0473\7 \2\2\u03de\u03df\7\u0090\2\2\u03df\u03e1"+
		"\7B\2\2\u03e0\u03e2\7\u0091\2\2\u03e1\u03e0\3\2\2\2\u03e1\u03e2\3\2\2"+
		"\2\u03e2\u03e3\3\2\2\2\u03e3\u03e4\7|\2\2\u03e4\u03e6\7\u013a\2\2\u03e5"+
		"\u03e7\7\u00b3\2\2\u03e6\u03e5\3\2\2\2\u03e6\u03e7\3\2\2\2\u03e7\u03e8"+
		"\3\2\2\2\u03e8\u03e9\7\u0081\2\2\u03e9\u03ea\7\u00f6\2\2\u03ea\u03ec\5"+
		"\u00be`\2\u03eb\u03ed\5.\30\2\u03ec\u03eb\3\2\2\2\u03ec\u03ed\3\2\2\2"+
		"\u03ed\u0473\3\2\2\2\u03ee\u03ef\7\u0109\2\2\u03ef\u03f0\7\u00f6\2\2\u03f0"+
		"\u03f2\5\u00be`\2\u03f1\u03f3\5.\30\2\u03f2\u03f1\3\2\2\2\u03f2\u03f3"+
		"\3\2\2\2\u03f3\u0473\3\2\2\2\u03f4\u03f5\7\u009e\2\2\u03f5\u03f6\7\u00cc"+
		"\2\2\u03f6\u03f7\7\u00f6\2\2\u03f7\u03fa\5\u00be`\2\u03f8\u03f9\t\f\2"+
		"\2\u03f9\u03fb\7\u00b6\2\2\u03fa\u03f8\3\2\2\2\u03fa\u03fb\3\2\2\2\u03fb"+
		"\u0473\3\2\2\2\u03fc\u03fd\t\r\2\2\u03fd\u0401\5\u011e\u0090\2\u03fe\u0400"+
		"\13\2\2\2\u03ff\u03fe\3\2\2\2\u0400\u0403\3\2\2\2\u0401\u0402\3\2\2\2"+
		"\u0401\u03ff\3\2\2\2\u0402\u0473\3\2\2\2\u0403\u0401\3\2\2\2\u0404\u0405"+
		"\7\u00e4\2\2\u0405\u0409\7\u00d5\2\2\u0406\u0408\13\2\2\2\u0407\u0406"+
		"\3\2\2\2\u0408\u040b\3\2\2\2\u0409\u040a\3\2\2\2\u0409\u0407\3\2\2\2\u040a"+
		"\u0473\3\2\2\2\u040b\u0409\3\2\2\2\u040c\u040d\7\u00e4\2\2\u040d\u040e"+
		"\7\u00fd\2\2\u040e\u040f\7\u0123\2\2\u040f\u0473\5\u00ecw\2\u0410\u0411"+
		"\7\u00e4\2\2\u0411\u0412\7\u00fd\2\2\u0412\u0413\7\u0123\2\2\u0413\u0473"+
		"\t\16\2\2\u0414\u0415\7\u00e4\2\2\u0415\u0416\7\u00fd\2\2\u0416\u041a"+
		"\7\u0123\2\2\u0417\u0419\13\2\2\2\u0418\u0417\3\2\2\2\u0419\u041c\3\2"+
		"\2\2\u041a\u041b\3\2\2\2\u041a\u0418\3\2\2\2\u041b\u0473\3\2\2\2\u041c"+
		"\u041a\3\2\2\2\u041d\u041e\7\u00e4\2\2\u041e\u041f\5\26\f\2\u041f\u0420"+
		"\7\u0124\2\2\u0420\u0421\5\30\r\2\u0421\u0473\3\2\2\2\u0422\u0423\7\u00e4"+
		"\2\2\u0423\u042b\5\26\f\2\u0424\u0428\7\u0124\2\2\u0425\u0427\13\2\2\2"+
		"\u0426\u0425\3\2\2\2\u0427\u042a\3\2\2\2\u0428\u0429\3\2\2\2\u0428\u0426"+
		"\3\2\2\2\u0429\u042c\3\2\2\2\u042a\u0428\3\2\2\2\u042b\u0424\3\2\2\2\u042b"+
		"\u042c\3\2\2\2\u042c\u0473\3\2\2\2\u042d\u0431\7\u00e4\2\2\u042e\u0430"+
		"\13\2\2\2\u042f\u042e\3\2\2\2\u0430\u0433\3\2\2\2\u0431\u0432\3\2\2\2"+
		"\u0431\u042f\3\2\2\2\u0432\u0434\3\2\2\2\u0433\u0431\3\2\2\2\u0434\u0435"+
		"\7\u0124\2\2\u0435\u0473\5\30\r\2\u0436\u043a\7\u00e4\2\2\u0437\u0439"+
		"\13\2\2\2\u0438\u0437\3\2\2\2\u0439\u043c\3\2\2\2\u043a\u043b\3\2\2\2"+
		"\u043a\u0438\3\2\2\2\u043b\u0473\3\2\2\2\u043c\u043a\3\2\2\2\u043d\u043e"+
		"\7\u00cf\2\2\u043e\u0473\5\26\f\2\u043f\u0443\7\u00cf\2\2\u0440\u0442"+
		"\13\2\2\2\u0441\u0440\3\2\2\2\u0442\u0445\3\2\2\2\u0443\u0444\3\2\2\2"+
		"\u0443\u0441\3\2\2\2\u0444\u0473\3\2\2\2\u0445\u0443\3\2\2\2\u0446\u0447"+
		"\78\2\2\u0447\u044b\7y\2\2\u0448\u0449\7u\2\2\u0449\u044a\7\u00a3\2\2"+
		"\u044a\u044c\7Z\2\2\u044b\u0448\3\2\2\2\u044b\u044c\3\2\2\2\u044c\u044d"+
		"\3\2\2\2\u044d\u044e\5\u011e\u0090\2\u044e\u0450\7\u00a7\2\2\u044f\u0451"+
		"\7\u00f6\2\2\u0450\u044f\3\2\2\2\u0450\u0451\3\2\2\2\u0451\u0452\3\2\2"+
		"\2\u0452\u0455\5\u00be`\2\u0453\u0454\7\u0117\2\2\u0454\u0456\5\u011e"+
		"\u0090\2\u0455\u0453\3\2\2\2\u0455\u0456\3\2\2\2\u0456\u0457\3\2\2\2\u0457"+
		"\u0458\7\7\2\2\u0458\u0459\5\u00c0a\2\u0459\u045c\7\b\2\2\u045a\u045b"+
		"\7\u00aa\2\2\u045b\u045d\5B\"\2\u045c\u045a\3\2\2\2\u045c\u045d\3\2\2"+
		"\2\u045d\u0473\3\2\2\2\u045e\u045f\7S\2\2\u045f\u0462\7y\2\2\u0460\u0461"+
		"\7u\2\2\u0461\u0463\7Z\2\2\u0462\u0460\3\2\2\2\u0462\u0463\3\2\2\2\u0463"+
		"\u0464\3\2\2\2\u0464\u0465\5\u011e\u0090\2\u0465\u0467\7\u00a7\2\2\u0466"+
		"\u0468\7\u00f6\2\2\u0467\u0466\3\2\2\2\u0467\u0468\3\2\2\2\u0468\u0469"+
		"\3\2\2\2\u0469\u046a\5\u00be`\2\u046a\u0473\3\2\2\2\u046b\u046f\5\32\16"+
		"\2\u046c\u046e\13\2\2\2\u046d\u046c\3\2\2\2\u046e\u0471\3\2\2\2\u046f"+
		"\u0470\3\2\2\2\u046f\u046d\3\2\2\2\u0470\u0473\3\2\2\2\u0471\u046f\3\2"+
		"\2\2\u0472\u0151\3\2\2\2\u0472\u0153\3\2\2\2\u0472\u0156\3\2\2\2\u0472"+
		"\u0158\3\2\2\2\u0472\u015c\3\2\2\2\u0472\u0162\3\2\2\2\u0472\u0174\3\2"+
		"\2\2\u0472\u017b\3\2\2\2\u0472\u0181\3\2\2\2\u0472\u018b\3\2\2\2\u0472"+
		"\u0197\3\2\2\2\u0472\u01a8\3\2\2\2\u0472\u01bd\3\2\2\2\u0472\u01ce\3\2"+
		"\2\2\u0472\u01df\3\2\2\2\u0472\u01ea\3\2\2\2\u0472\u01f1\3\2\2\2\u0472"+
		"\u01fa\3\2\2\2\u0472\u0203\3\2\2\2\u0472\u0210\3\2\2\2\u0472\u021b\3\2"+
		"\2\2\u0472\u0222\3\2\2\2\u0472\u0229\3\2\2\2\u0472\u0234\3\2\2\2\u0472"+
		"\u023f\3\2\2\2\u0472\u024e\3\2\2\2\u0472\u025a\3\2\2\2\u0472\u0268\3\2"+
		"\2\2\u0472\u0272\3\2\2\2\u0472\u0280\3\2\2\2\u0472\u0288\3\2\2\2\u0472"+
		"\u029b\3\2\2\2\u0472\u02a4\3\2\2\2\u0472\u02aa\3\2\2\2\u0472\u02b4\3\2"+
		"\2\2\u0472\u02bb\3\2\2\2\u0472\u02de\3\2\2\2\u0472\u02f4\3\2\2\2\u0472"+
		"\u02fc\3\2\2\2\u0472\u0318\3\2\2\2\u0472\u0322\3\2\2\2\u0472\u0327\3\2"+
		"\2\2\u0472\u0333\3\2\2\2\u0472\u033f\3\2\2\2\u0472\u0348\3\2\2\2\u0472"+
		"\u0350\3\2\2\2\u0472\u035c\3\2\2\2\u0472\u0362\3\2\2\2\u0472\u0374\3\2"+
		"\2\2\u0472\u037c\3\2\2\2\u0472\u037f\3\2\2\2\u0472\u0387\3\2\2\2\u0472"+
		"\u038d\3\2\2\2\u0472\u0394\3\2\2\2\u0472\u03a2\3\2\2\2\u0472\u03a7\3\2"+
		"\2\2\u0472\u03ae\3\2\2\2\u0472\u03b5\3\2\2\2\u0472\u03b8\3\2\2\2\u0472"+
		"\u03bb\3\2\2\2\u0472\u03c5\3\2\2\2\u0472\u03d5\3\2\2\2\u0472\u03dc\3\2"+
		"\2\2\u0472\u03de\3\2\2\2\u0472\u03ee\3\2\2\2\u0472\u03f4\3\2\2\2\u0472"+
		"\u03fc\3\2\2\2\u0472\u0404\3\2\2\2\u0472\u040c\3\2\2\2\u0472\u0410\3\2"+
		"\2\2\u0472\u0414\3\2\2\2\u0472\u041d\3\2\2\2\u0472\u0422\3\2\2\2\u0472"+
		"\u042d\3\2\2\2\u0472\u0436\3\2\2\2\u0472\u043d\3\2\2\2\u0472\u043f\3\2"+
		"\2\2\u0472\u0446\3\2\2\2\u0472\u045e\3\2\2\2\u0472\u046b\3\2\2\2\u0473"+
		"\25\3\2\2\2\u0474\u0475\5\u0122\u0092\2\u0475\27\3\2\2\2\u0476\u0477\5"+
		"\u0122\u0092\2\u0477\31\3\2\2\2\u0478\u0479\78\2\2\u0479\u0521\7\u00d5"+
		"\2\2\u047a\u047b\7S\2\2\u047b\u0521\7\u00d5\2\2\u047c\u047e\7p\2\2\u047d"+
		"\u047f\7\u00d5\2\2\u047e\u047d\3\2\2\2\u047e\u047f\3\2\2\2\u047f\u0521"+
		"\3\2\2\2\u0480\u0482\7\u00d2\2\2\u0481\u0483\7\u00d5\2\2\u0482\u0481\3"+
		"\2\2\2\u0482\u0483\3\2\2\2\u0483\u0521\3\2\2\2\u0484\u0485\7\u00e7\2\2"+
		"\u0485\u0521\7p\2\2\u0486\u0487\7\u00e7\2\2\u0487\u0489\7\u00d5\2\2\u0488"+
		"\u048a\7p\2\2\u0489\u0488\3\2\2\2\u0489\u048a\3\2\2\2\u048a\u0521\3\2"+
		"\2\2\u048b\u048c\7\u00e7\2\2\u048c\u0521\7\u00bf\2\2\u048d\u048e\7\u00e7"+
		"\2\2\u048e\u0521\7\u00d6\2\2\u048f\u0490\7\u00e7\2\2\u0490\u0491\7;\2"+
		"\2\u0491\u0521\7\u00d6\2\2\u0492\u0493\7\\\2\2\u0493\u0521\7\u00f6\2\2"+
		"\u0494\u0495\7w\2\2\u0495\u0521\7\u00f6\2\2\u0496\u0497\7\u00e7\2\2\u0497"+
		"\u0521\7\63\2\2\u0498\u0499\7\u00e7\2\2\u0499\u049a\78\2\2\u049a\u0521"+
		"\7\u00f6\2\2\u049b\u049c\7\u00e7\2\2\u049c\u0521\7\u0105\2\2\u049d\u049e"+
		"\7\u00e7\2\2\u049e\u0521\7z\2\2\u049f\u04a0\7\u00e7\2\2\u04a0\u0521\7"+
		"\u0094\2\2\u04a1\u04a2\78\2\2\u04a2\u0521\7y\2\2\u04a3\u04a4\7S\2\2\u04a4"+
		"\u0521\7y\2\2\u04a5\u04a6\7\20\2\2\u04a6\u0521\7y\2\2\u04a7\u04a8\7\u0093"+
		"\2\2\u04a8\u0521\7\u00f6\2\2\u04a9\u04aa\7\u0093\2\2\u04aa\u0521\7C\2"+
		"\2\u04ab\u04ac\7\u0112\2\2\u04ac\u0521\7\u00f6\2\2\u04ad\u04ae\7\u0112"+
		"\2\2\u04ae\u0521\7C\2\2\u04af\u04b0\78\2\2\u04b0\u04b1\7\u00fa\2\2\u04b1"+
		"\u0521\7\u0096\2\2\u04b2\u04b3\7S\2\2\u04b3\u04b4\7\u00fa\2\2\u04b4\u0521"+
		"\7\u0096\2\2\u04b5\u04b6\7\20\2\2\u04b6\u04b7\7\u00f6\2\2\u04b7\u04b8"+
		"\5\u00c4c\2\u04b8\u04b9\7\u00a3\2\2\u04b9\u04ba\7*\2\2\u04ba\u0521\3\2"+
		"\2\2\u04bb\u04bc\7\20\2\2\u04bc\u04bd\7\u00f6\2\2\u04bd\u04be\5\u00c4"+
		"c\2\u04be\u04bf\7*\2\2\u04bf\u04c0\7\37\2\2\u04c0\u0521\3\2\2\2\u04c1"+
		"\u04c2\7\20\2\2\u04c2\u04c3\7\u00f6\2\2\u04c3\u04c4\5\u00c4c\2\u04c4\u04c5"+
		"\7\u00a3\2\2\u04c5\u04c6\7\u00eb\2\2\u04c6\u0521\3\2\2\2\u04c7\u04c8\7"+
		"\20\2\2\u04c8\u04c9\7\u00f6\2\2\u04c9\u04ca\5\u00c4c\2\u04ca\u04cb\7\u00e8"+
		"\2\2\u04cb\u04cc\7\37\2\2\u04cc\u0521\3\2\2\2\u04cd\u04ce\7\20\2\2\u04ce"+
		"\u04cf\7\u00f6\2\2\u04cf\u04d0\5\u00c4c\2\u04d0\u04d1\7\u00a3\2\2\u04d1"+
		"\u04d2\7\u00e8\2\2\u04d2\u0521\3\2\2\2\u04d3\u04d4\7\20\2\2\u04d4\u04d5"+
		"\7\u00f6\2\2\u04d5\u04d6\5\u00c4c\2\u04d6\u04d7\7\u00a3\2\2\u04d7\u04d8"+
		"\7\u00ee\2\2\u04d8\u04d9\7\27\2\2\u04d9\u04da\7N\2\2\u04da\u0521\3\2\2"+
		"\2\u04db\u04dc\7\20\2\2\u04dc\u04dd\7\u00f6\2\2\u04dd\u04de\5\u00c4c\2"+
		"\u04de\u04df\7\u00e4\2\2\u04df\u04e0\7\u00e8\2\2\u04e0\u04e1\7\u0092\2"+
		"\2\u04e1\u0521\3\2\2\2\u04e2\u04e3\7\20\2\2\u04e3\u04e4\7\u00f6\2\2\u04e4"+
		"\u04e5\5\u00c4c\2\u04e5\u04e6\7Y\2\2\u04e6\u04e7\7\u00b4\2\2\u04e7\u0521"+
		"\3\2\2\2\u04e8\u04e9\7\20\2\2\u04e9\u04ea\7\u00f6\2\2\u04ea\u04eb\5\u00c4"+
		"c\2\u04eb\u04ec\7\25\2\2\u04ec\u04ed\7\u00b4\2\2\u04ed\u0521\3\2\2\2\u04ee"+
		"\u04ef\7\20\2\2\u04ef\u04f0\7\u00f6\2\2\u04f0\u04f1\5\u00c4c\2\u04f1\u04f2"+
		"\7\u010c\2\2\u04f2\u04f3\7\u00b4\2\2\u04f3\u0521\3\2\2\2\u04f4\u04f5\7"+
		"\20\2\2\u04f5\u04f6\7\u00f6\2\2\u04f6\u04f7\5\u00c4c\2\u04f7\u04f8\7\u0102"+
		"\2\2\u04f8\u0521\3\2\2\2\u04f9\u04fa\7\20\2\2\u04fa\u04fb\7\u00f6\2\2"+
		"\u04fb\u04fd\5\u00c4c\2\u04fc\u04fe\5.\30\2\u04fd\u04fc\3\2\2\2\u04fd"+
		"\u04fe\3\2\2\2\u04fe\u04ff\3\2\2\2\u04ff\u0500\7\62\2\2\u0500\u0521\3"+
		"\2\2\2\u0501\u0502\7\20\2\2\u0502\u0503\7\u00f6\2\2\u0503\u0505\5\u00c4"+
		"c\2\u0504\u0506\5.\30\2\u0505\u0504\3\2\2\2\u0505\u0506\3\2\2\2\u0506"+
		"\u0507\3\2\2\2\u0507\u0508\7\65\2\2\u0508\u0521\3\2\2\2\u0509\u050a\7"+
		"\20\2\2\u050a\u050b\7\u00f6\2\2\u050b\u050d\5\u00c4c\2\u050c\u050e\5."+
		"\30\2\u050d\u050c\3\2\2\2\u050d\u050e\3\2\2\2\u050e\u050f\3\2\2\2\u050f"+
		"\u0510\7\u00e4\2\2\u0510\u0511\7d\2\2\u0511\u0521\3\2\2\2\u0512\u0513"+
		"\7\20\2\2\u0513\u0514\7\u00f6\2\2\u0514\u0516\5\u00c4c\2\u0515\u0517\5"+
		".\30\2\u0516\u0515\3\2\2\2\u0516\u0517\3\2\2\2\u0517\u0518\3\2\2\2\u0518"+
		"\u0519\7\u00ce\2\2\u0519\u051a\7/\2\2\u051a\u0521\3\2\2\2\u051b\u051c"+
		"\7\u00ec\2\2\u051c\u0521\7\u0104\2\2\u051d\u0521\7\61\2\2\u051e\u0521"+
		"\7\u00d7\2\2\u051f\u0521\7M\2\2\u0520\u0478\3\2\2\2\u0520\u047a\3\2\2"+
		"\2\u0520\u047c\3\2\2\2\u0520\u0480\3\2\2\2\u0520\u0484\3\2\2\2\u0520\u0486"+
		"\3\2\2\2\u0520\u048b\3\2\2\2\u0520\u048d\3\2\2\2\u0520\u048f\3\2\2\2\u0520"+
		"\u0492\3\2\2\2\u0520\u0494\3\2\2\2\u0520\u0496\3\2\2\2\u0520\u0498\3\2"+
		"\2\2\u0520\u049b\3\2\2\2\u0520\u049d\3\2\2\2\u0520\u049f\3\2\2\2\u0520"+
		"\u04a1\3\2\2\2\u0520\u04a3\3\2\2\2\u0520\u04a5\3\2\2\2\u0520\u04a7\3\2"+
		"\2\2\u0520\u04a9\3\2\2\2\u0520\u04ab\3\2\2\2\u0520\u04ad\3\2\2\2\u0520"+
		"\u04af\3\2\2\2\u0520\u04b2\3\2\2\2\u0520\u04b5\3\2\2\2\u0520\u04bb\3\2"+
		"\2\2\u0520\u04c1\3\2\2\2\u0520\u04c7\3\2\2\2\u0520\u04cd\3\2\2\2\u0520"+
		"\u04d3\3\2\2\2\u0520\u04db\3\2\2\2\u0520\u04e2\3\2\2\2\u0520\u04e8\3\2"+
		"\2\2\u0520\u04ee\3\2\2\2\u0520\u04f4\3\2\2\2\u0520\u04f9\3\2\2\2\u0520"+
		"\u0501\3\2\2\2\u0520\u0509\3\2\2\2\u0520\u0512\3\2\2\2\u0520\u051b\3\2"+
		"\2\2\u0520\u051d\3\2\2\2\u0520\u051e\3\2\2\2\u0520\u051f\3\2\2\2\u0521"+
		"\33\3\2\2\2\u0522\u0524\78\2\2\u0523\u0525\7\u00fa\2\2\u0524\u0523\3\2"+
		"\2\2\u0524\u0525\3\2\2\2\u0525\u0527\3\2\2\2\u0526\u0528\7^\2\2\u0527"+
		"\u0526\3\2\2\2\u0527\u0528\3\2\2\2\u0528\u0529\3\2\2\2\u0529\u052d\7\u00f6"+
		"\2\2\u052a\u052b\7u\2\2\u052b\u052c\7\u00a3\2\2\u052c\u052e\7Z\2\2\u052d"+
		"\u052a\3\2\2\2\u052d\u052e\3\2\2\2\u052e\u052f\3\2\2\2\u052f\u0530\5\u00be"+
		"`\2\u0530\35\3\2\2\2\u0531\u0532\78\2\2\u0532\u0534\7\u00ab\2\2\u0533"+
		"\u0531\3\2\2\2\u0533\u0534\3\2\2\2\u0534\u0535\3\2\2\2\u0535\u0536\7\u00ce"+
		"\2\2\u0536\u0537\7\u00f6\2\2\u0537\u0538\5\u00be`\2\u0538\37\3\2\2\2\u0539"+
		"\u053a\7*\2\2\u053a\u053b\7\37\2\2\u053b\u053f\5\u00a6T\2\u053c\u053d"+
		"\7\u00eb\2\2\u053d\u053e\7\37\2\2\u053e\u0540\5\u00aaV\2\u053f\u053c\3"+
		"\2\2\2\u053f\u0540\3\2\2\2\u0540\u0541\3\2\2\2\u0541\u0542\7\u0081\2\2"+
		"\u0542\u0543\7\u013e\2\2\u0543\u0544\7\36\2\2\u0544!\3\2\2\2\u0545\u0546"+
		"\7\u00e8\2\2\u0546\u0547\7\37\2\2\u0547\u0548\5\u00a6T\2\u0548\u054b\7"+
		"\u00a7\2\2\u0549\u054c\5J&\2\u054a\u054c\5L\'\2\u054b\u0549\3\2\2\2\u054b"+
		"\u054a\3\2\2\2\u054c\u0550\3\2\2\2\u054d\u054e\7\u00ee\2\2\u054e\u054f"+
		"\7\27\2\2\u054f\u0551\7N\2\2\u0550\u054d\3\2\2\2\u0550\u0551\3\2\2\2\u0551"+
		"#\3\2\2\2\u0552\u0553\7\u0092\2\2\u0553\u0554\7\u013a\2\2\u0554%\3\2\2"+
		"\2\u0555\u0556\7\60\2\2\u0556\u0557\7\u013a\2\2\u0557\'\3\2\2\2\u0558"+
		"\u055a\5:\36\2\u0559\u0558\3\2\2\2\u0559\u055a\3\2\2\2\u055a\u055b\3\2"+
		"\2\2\u055b\u055c\5\\/\2\u055c\u055d\5X-\2\u055d)\3\2\2\2\u055e\u055f\7"+
		"~\2\2\u055f\u0561\7\u00b3\2\2\u0560\u0562\7\u00f6\2\2\u0561\u0560\3\2"+
		"\2\2\u0561\u0562\3\2\2\2\u0562\u0563\3\2\2\2\u0563\u056a\5\u00be`\2\u0564"+
		"\u0568\5.\30\2\u0565\u0566\7u\2\2\u0566\u0567\7\u00a3\2\2\u0567\u0569"+
		"\7Z\2\2\u0568\u0565\3\2\2\2\u0568\u0569\3\2\2\2\u0569\u056b\3\2\2\2\u056a"+
		"\u0564\3\2\2\2\u056a\u056b\3\2\2\2\u056b\u056d\3\2\2\2\u056c\u056e\5\u00a6"+
		"T\2\u056d\u056c\3\2\2\2\u056d\u056e\3\2\2\2\u056e\u059c\3\2\2\2\u056f"+
		"\u0570\7~\2\2\u0570\u0572\7\u0081\2\2\u0571\u0573\7\u00f6\2\2\u0572\u0571"+
		"\3\2\2\2\u0572\u0573\3\2\2\2\u0573\u0574\3\2\2\2\u0574\u0576\5\u00be`"+
		"\2\u0575\u0577\5.\30\2\u0576\u0575\3\2\2\2\u0576\u0577\3\2\2\2\u0577\u057b"+
		"\3\2\2\2\u0578\u0579\7u\2\2\u0579\u057a\7\u00a3\2\2\u057a\u057c\7Z\2\2"+
		"\u057b\u0578\3\2\2\2\u057b\u057c\3\2\2\2\u057c\u057e\3\2\2\2\u057d\u057f"+
		"\5\u00a6T\2\u057e\u057d\3\2\2\2\u057e\u057f\3\2\2\2\u057f\u059c\3\2\2"+
		"\2\u0580\u0581\7~\2\2\u0581\u0583\7\u00b3\2\2\u0582\u0584\7\u0091\2\2"+
		"\u0583\u0582\3\2\2\2\u0583\u0584\3\2\2\2\u0584\u0585\3\2\2\2\u0585\u0586"+
		"\7O\2\2\u0586\u0588\7\u013a\2\2\u0587\u0589\5\u00ba^\2\u0588\u0587\3\2"+
		"\2\2\u0588\u0589\3\2\2\2\u0589\u058b\3\2\2\2\u058a\u058c\5N(\2\u058b\u058a"+
		"\3\2\2\2\u058b\u058c\3\2\2\2\u058c\u059c\3\2\2\2\u058d\u058e\7~\2\2\u058e"+
		"\u0590\7\u00b3\2\2\u058f\u0591\7\u0091\2\2\u0590\u058f\3\2\2\2\u0590\u0591"+
		"\3\2\2\2\u0591\u0592\3\2\2\2\u0592\u0594\7O\2\2\u0593\u0595\7\u013a\2"+
		"\2\u0594\u0593\3\2\2\2\u0594\u0595\3\2\2\2\u0595\u0596\3\2\2\2\u0596\u0599"+
		"\5> \2\u0597\u0598\7\u00aa\2\2\u0598\u059a\5B\"\2\u0599\u0597\3\2\2\2"+
		"\u0599\u059a\3\2\2\2\u059a\u059c\3\2\2\2\u059b\u055e\3\2\2\2\u059b\u056f"+
		"\3\2\2\2\u059b\u0580\3\2\2\2\u059b\u058d\3\2\2\2\u059c+\3\2\2\2\u059d"+
		"\u059f\5.\30\2\u059e\u05a0\5$\23\2\u059f\u059e\3\2\2\2\u059f\u05a0\3\2"+
		"\2\2\u05a0-\3\2\2\2\u05a1\u05a2\7\u00b4\2\2\u05a2\u05a3\7\7\2\2\u05a3"+
		"\u05a8\5\60\31\2\u05a4\u05a5\7\t\2\2\u05a5\u05a7\5\60\31\2\u05a6\u05a4"+
		"\3\2\2\2\u05a7\u05aa\3\2\2\2\u05a8\u05a6\3\2\2\2\u05a8\u05a9\3\2\2\2\u05a9"+
		"\u05ab\3\2\2\2\u05aa\u05a8\3\2\2\2\u05ab\u05ac\7\b\2\2\u05ac/\3\2\2\2"+
		"\u05ad\u05b0\5\u011e\u0090\2\u05ae\u05af\7\u0124\2\2\u05af\u05b1\5\u00e2"+
		"r\2\u05b0\u05ae\3\2\2\2\u05b0\u05b1\3\2\2\2\u05b1\61\3\2\2\2\u05b2\u05b3"+
		"\t\17\2\2\u05b3\63\3\2\2\2\u05b4\u05b5\t\20\2\2\u05b5\65\3\2\2\2\u05b6"+
		"\u05bc\5\u0118\u008d\2\u05b7\u05bc\7\u013a\2\2\u05b8\u05bc\5\u00e4s\2"+
		"\u05b9\u05bc\5\u00e6t\2\u05ba\u05bc\5\u00e8u\2\u05bb\u05b6\3\2\2\2\u05bb"+
		"\u05b7\3\2\2\2\u05bb\u05b8\3\2\2\2\u05bb\u05b9\3\2\2\2\u05bb\u05ba\3\2"+
		"\2\2\u05bc\67\3\2\2\2\u05bd\u05c2\5\u011e\u0090\2\u05be\u05bf\7\n\2\2"+
		"\u05bf\u05c1\5\u011e\u0090\2\u05c0\u05be\3\2\2\2\u05c1\u05c4\3\2\2\2\u05c2"+
		"\u05c0\3\2\2\2\u05c2\u05c3\3\2\2\2\u05c39\3\2\2\2\u05c4\u05c2\3\2\2\2"+
		"\u05c5\u05c6\7\u0120\2\2\u05c6\u05cb\5<\37\2\u05c7\u05c8\7\t\2\2\u05c8"+
		"\u05ca\5<\37\2\u05c9\u05c7\3\2\2\2\u05ca\u05cd\3\2\2\2\u05cb\u05c9\3\2"+
		"\2\2\u05cb\u05cc\3\2\2\2\u05cc;\3\2\2\2\u05cd\u05cb\3\2\2\2\u05ce\u05d0"+
		"\5\u011a\u008e\2\u05cf\u05d1\5\u00a6T\2\u05d0\u05cf\3\2\2\2\u05d0\u05d1"+
		"\3\2\2\2\u05d1\u05d3\3\2\2\2\u05d2\u05d4\7\27\2\2\u05d3\u05d2\3\2\2\2"+
		"\u05d3\u05d4\3\2\2\2\u05d4\u05d5\3\2\2\2\u05d5\u05d6\7\7\2\2\u05d6\u05d7"+
		"\5(\25\2\u05d7\u05d8\7\b\2\2\u05d8=\3\2\2\2\u05d9\u05da\7\u0117\2\2\u05da"+
		"\u05db\5\u00be`\2\u05db?\3\2\2\2\u05dc\u05dd\7\u00aa\2\2\u05dd\u05ea\5"+
		"B\"\2\u05de\u05df\7\u00b5\2\2\u05df\u05e0\7\37\2\2\u05e0\u05ea\5\u00cc"+
		"g\2\u05e1\u05ea\5\"\22\2\u05e2\u05ea\5 \21\2\u05e3\u05ea\5\u00ba^\2\u05e4"+
		"\u05ea\5N(\2\u05e5\u05ea\5$\23\2\u05e6\u05ea\5&\24\2\u05e7\u05e8\7\u00f9"+
		"\2\2\u05e8\u05ea\5B\"\2\u05e9\u05dc\3\2\2\2\u05e9\u05de\3\2\2\2\u05e9"+
		"\u05e1\3\2\2\2\u05e9\u05e2\3\2\2\2\u05e9\u05e3\3\2\2\2\u05e9\u05e4\3\2"+
		"\2\2\u05e9\u05e5\3\2\2\2\u05e9\u05e6\3\2\2\2\u05e9\u05e7\3\2\2\2\u05ea"+
		"\u05ed\3\2\2\2\u05eb\u05e9\3\2\2\2\u05eb\u05ec\3\2\2\2\u05ecA\3\2\2\2"+
		"\u05ed\u05eb\3\2\2\2\u05ee\u05ef\7\7\2\2\u05ef\u05f4\5D#\2\u05f0\u05f1"+
		"\7\t\2\2\u05f1\u05f3\5D#\2\u05f2\u05f0\3\2\2\2\u05f3\u05f6\3\2\2\2\u05f4"+
		"\u05f2\3\2\2\2\u05f4\u05f5\3\2\2\2\u05f5\u05f7\3\2\2\2\u05f6\u05f4\3\2"+
		"\2\2\u05f7\u05f8\7\b\2\2\u05f8C\3\2\2\2\u05f9\u05fe\5F$\2\u05fa\u05fc"+
		"\7\u0124\2\2\u05fb\u05fa\3\2\2\2\u05fb\u05fc\3\2\2\2\u05fc\u05fd\3\2\2"+
		"\2\u05fd\u05ff\5H%\2\u05fe\u05fb\3\2\2\2\u05fe\u05ff\3\2\2\2\u05ffE\3"+
		"\2\2\2\u0600\u0605\5\u011e\u0090\2\u0601\u0602\7\n\2\2\u0602\u0604\5\u011e"+
		"\u0090\2\u0603\u0601\3\2\2\2\u0604\u0607\3\2\2\2\u0605\u0603\3\2\2\2\u0605"+
		"\u0606\3\2\2\2\u0606\u060a\3\2\2\2\u0607\u0605\3\2\2\2\u0608\u060a\7\u013a"+
		"\2\2\u0609\u0600\3\2\2\2\u0609\u0608\3\2\2\2\u060aG\3\2\2\2\u060b\u0610"+
		"\7\u013e\2\2\u060c\u0610\7\u0140\2\2\u060d\u0610\5\u00eav\2\u060e\u0610"+
		"\7\u013a\2\2\u060f\u060b\3\2\2\2\u060f\u060c\3\2\2\2\u060f\u060d\3\2\2"+
		"\2\u060f\u060e\3\2\2\2\u0610I\3\2\2\2\u0611\u0612\7\7\2\2\u0612\u0617"+
		"\5\u00e2r\2\u0613\u0614\7\t\2\2\u0614\u0616\5\u00e2r\2\u0615\u0613\3\2"+
		"\2\2\u0616\u0619\3\2\2\2\u0617\u0615\3\2\2\2\u0617\u0618\3\2\2\2\u0618"+
		"\u061a\3\2\2\2\u0619\u0617\3\2\2\2\u061a\u061b\7\b\2\2\u061bK\3\2\2\2"+
		"\u061c\u061d\7\7\2\2\u061d\u0622\5J&\2\u061e\u061f\7\t\2\2\u061f\u0621"+
		"\5J&\2\u0620\u061e\3\2\2\2\u0621\u0624\3\2\2\2\u0622\u0620\3\2\2\2\u0622"+
		"\u0623\3\2\2\2\u0623\u0625\3\2\2\2\u0624\u0622\3\2\2\2\u0625\u0626\7\b"+
		"\2\2\u0626M\3\2\2\2\u0627\u0628\7\u00ee\2\2\u0628\u0629\7\27\2\2\u0629"+
		"\u062e\5P)\2\u062a\u062b\7\u00ee\2\2\u062b\u062c\7\37\2\2\u062c\u062e"+
		"\5R*\2\u062d\u0627\3\2\2\2\u062d\u062a\3\2\2\2\u062eO\3\2\2\2\u062f\u0630"+
		"\7}\2\2\u0630\u0631\7\u013a\2\2\u0631\u0632\7\u00af\2\2\u0632\u0635\7"+
		"\u013a\2\2\u0633\u0635\5\u011e\u0090\2\u0634\u062f\3\2\2\2\u0634\u0633"+
		"\3\2\2\2\u0635Q\3\2\2\2\u0636\u063a\7\u013a\2\2\u0637\u0638\7\u0120\2"+
		"\2\u0638\u0639\7\u00e2\2\2\u0639\u063b\5B\"\2\u063a\u0637\3\2\2\2\u063a"+
		"\u063b\3\2\2\2\u063bS\3\2\2\2\u063c\u063d\5\u011e\u0090\2\u063d\u063e"+
		"\7\u013a\2\2\u063eU\3\2\2\2\u063f\u0640\5*\26\2\u0640\u0641\5(\25\2\u0641"+
		"\u0672\3\2\2\2\u0642\u0644\5\u0082B\2\u0643\u0645\5Z.\2\u0644\u0643\3"+
		"\2\2\2\u0645\u0646\3\2\2\2\u0646\u0644\3\2\2\2\u0646\u0647\3\2\2\2\u0647"+
		"\u0672\3\2\2\2\u0648\u0649\7I\2\2\u0649\u064a\7k\2\2\u064a\u064b\5\u00be"+
		"`\2\u064b\u064d\5\u00b8]\2\u064c\u064e\5z>\2\u064d\u064c\3\2\2\2\u064d"+
		"\u064e\3\2\2\2\u064e\u0672\3\2\2\2\u064f\u0650\7\u0114\2\2\u0650\u0651"+
		"\5\u00be`\2\u0651\u0652\5\u00b8]\2\u0652\u0654\5l\67\2\u0653\u0655\5z"+
		">\2\u0654\u0653\3\2\2\2\u0654\u0655\3\2\2\2\u0655\u0672\3\2\2\2\u0656"+
		"\u0657\7\u0099\2\2\u0657\u0658\7\u0081\2\2\u0658\u0659\5\u00be`\2\u0659"+
		"\u065a\5\u00b8]\2\u065a\u0660\7\u0117\2\2\u065b\u0661\5\u00be`\2\u065c"+
		"\u065d\7\7\2\2\u065d\u065e\5(\25\2\u065e\u065f\7\b\2\2\u065f\u0661\3\2"+
		"\2\2\u0660\u065b\3\2\2\2\u0660\u065c\3\2\2\2\u0661\u0662\3\2\2\2\u0662"+
		"\u0663\5\u00b8]\2\u0663\u0664\7\u00a7\2\2\u0664\u0668\5\u00d8m\2\u0665"+
		"\u0667\5n8\2\u0666\u0665\3\2\2\2\u0667\u066a\3\2\2\2\u0668\u0666\3\2\2"+
		"\2\u0668\u0669\3\2\2\2\u0669\u066e\3\2\2\2\u066a\u0668\3\2\2\2\u066b\u066d"+
		"\5p9\2\u066c\u066b\3\2\2\2\u066d\u0670\3\2\2\2\u066e\u066c\3\2\2\2\u066e"+
		"\u066f\3\2\2\2\u066f\u0672\3\2\2\2\u0670\u066e\3\2\2\2\u0671\u063f\3\2"+
		"\2\2\u0671\u0642\3\2\2\2\u0671\u0648\3\2\2\2\u0671\u064f\3\2\2\2\u0671"+
		"\u0656\3\2\2\2\u0672W\3\2\2\2\u0673\u0674\7\u00ac\2\2\u0674\u0675\7\37"+
		"\2\2\u0675\u067a\5`\61\2\u0676\u0677\7\t\2\2\u0677\u0679\5`\61\2\u0678"+
		"\u0676\3\2\2\2\u0679\u067c\3\2\2\2\u067a\u0678\3\2\2\2\u067a\u067b\3\2"+
		"\2\2\u067b\u067e\3\2\2\2\u067c\u067a\3\2\2\2\u067d\u0673\3\2\2\2\u067d"+
		"\u067e\3\2\2\2\u067e\u0689\3\2\2\2\u067f\u0680\7)\2\2\u0680\u0681\7\37"+
		"\2\2\u0681\u0686\5\u00d4k\2\u0682\u0683\7\t\2\2\u0683\u0685\5\u00d4k\2"+
		"\u0684\u0682\3\2\2\2\u0685\u0688\3\2\2\2\u0686\u0684\3\2\2\2\u0686\u0687"+
		"\3\2\2\2\u0687\u068a\3\2\2\2\u0688\u0686\3\2\2\2\u0689\u067f\3\2\2\2\u0689"+
		"\u068a\3\2\2\2\u068a\u0695\3\2\2\2\u068b\u068c\7Q\2\2\u068c\u068d\7\37"+
		"\2\2\u068d\u0692\5\u00d4k\2\u068e\u068f\7\t\2\2\u068f\u0691\5\u00d4k\2"+
		"\u0690\u068e\3\2\2\2\u0691\u0694\3\2\2\2\u0692\u0690\3\2\2\2\u0692\u0693"+
		"\3\2\2\2\u0693\u0696\3\2\2\2\u0694\u0692\3\2\2\2\u0695\u068b\3\2\2\2\u0695"+
		"\u0696\3\2\2\2\u0696\u06a1\3\2\2\2\u0697\u0698\7\u00ea\2\2\u0698\u0699"+
		"\7\37\2\2\u0699\u069e\5`\61\2\u069a\u069b\7\t\2\2\u069b\u069d\5`\61\2"+
		"\u069c\u069a\3\2\2\2\u069d\u06a0\3\2\2\2\u069e\u069c\3\2\2\2\u069e\u069f"+
		"\3\2\2\2\u069f\u06a2\3\2\2\2\u06a0\u069e\3\2\2\2\u06a1\u0697\3\2\2\2\u06a1"+
		"\u06a2\3\2\2\2\u06a2\u06a4\3\2\2\2\u06a3\u06a5\5\u010a\u0086\2\u06a4\u06a3"+
		"\3\2\2\2\u06a4\u06a5\3\2\2\2\u06a5\u06ab\3\2\2\2\u06a6\u06a9\7\u008d\2"+
		"\2\u06a7\u06aa\7\17\2\2\u06a8\u06aa\5\u00d4k\2\u06a9\u06a7\3\2\2\2\u06a9"+
		"\u06a8\3\2\2\2\u06aa\u06ac\3\2\2\2\u06ab\u06a6\3\2\2\2\u06ab\u06ac\3\2"+
		"\2\2\u06acY\3\2\2\2\u06ad\u06ae\5*\26\2\u06ae\u06af\5d\63\2\u06af[\3\2"+
		"\2\2\u06b0\u06b1\b/\1\2\u06b1\u06b2\5^\60\2\u06b2\u06ca\3\2\2\2\u06b3"+
		"\u06b4\f\5\2\2\u06b4\u06b5\6/\3\2\u06b5\u06b7\t\21\2\2\u06b6\u06b8\5\u0098"+
		"M\2\u06b7\u06b6\3\2\2\2\u06b7\u06b8\3\2\2\2\u06b8\u06b9\3\2\2\2\u06b9"+
		"\u06c9\5\\/\6\u06ba\u06bb\f\4\2\2\u06bb\u06bc\6/\5\2\u06bc\u06be\7\177"+
		"\2\2\u06bd\u06bf\5\u0098M\2\u06be\u06bd\3\2\2\2\u06be\u06bf\3\2\2\2\u06bf"+
		"\u06c0\3\2\2\2\u06c0\u06c9\5\\/\5\u06c1\u06c2\f\3\2\2\u06c2\u06c3\6/\7"+
		"\2\u06c3\u06c5\t\22\2\2\u06c4\u06c6\5\u0098M\2\u06c5\u06c4\3\2\2\2\u06c5"+
		"\u06c6\3\2\2\2\u06c6\u06c7\3\2\2\2\u06c7\u06c9\5\\/\4\u06c8\u06b3\3\2"+
		"\2\2\u06c8\u06ba\3\2\2\2\u06c8\u06c1\3\2\2\2\u06c9\u06cc\3\2\2\2\u06ca"+
		"\u06c8\3\2\2\2\u06ca\u06cb\3\2\2\2\u06cb]\3\2\2\2\u06cc\u06ca\3\2\2\2"+
		"\u06cd\u06d7\5f\64\2\u06ce\u06d7\5b\62\2\u06cf\u06d0\7\u00f6\2\2\u06d0"+
		"\u06d7\5\u00be`\2\u06d1\u06d7\5\u00b4[\2\u06d2\u06d3\7\7\2\2\u06d3\u06d4"+
		"\5(\25\2\u06d4\u06d5\7\b\2\2\u06d5\u06d7\3\2\2\2\u06d6\u06cd\3\2\2\2\u06d6"+
		"\u06ce\3\2\2\2\u06d6\u06cf\3\2\2\2\u06d6\u06d1\3\2\2\2\u06d6\u06d2\3\2"+
		"\2\2\u06d7_\3\2\2\2\u06d8\u06da\5\u00d4k\2\u06d9\u06db\t\23\2\2\u06da"+
		"\u06d9\3\2\2\2\u06da\u06db\3\2\2\2\u06db\u06de\3\2\2\2\u06dc\u06dd\7\u00a5"+
		"\2\2\u06dd\u06df\t\24\2\2\u06de\u06dc\3\2\2\2\u06de\u06df\3\2\2\2\u06df"+
		"a\3\2\2\2\u06e0\u06e2\5\u0082B\2\u06e1\u06e3\5d\63\2\u06e2\u06e1\3\2\2"+
		"\2\u06e3\u06e4\3\2\2\2\u06e4\u06e2\3\2\2\2\u06e4\u06e5\3\2\2\2\u06e5c"+
		"\3\2\2\2\u06e6\u06e8\5h\65\2\u06e7\u06e9\5z>\2\u06e8\u06e7\3\2\2\2\u06e8"+
		"\u06e9\3\2\2\2\u06e9\u06ea\3\2\2\2\u06ea\u06eb\5X-\2\u06eb\u0702\3\2\2"+
		"\2\u06ec\u06f0\5j\66\2\u06ed\u06ef\5\u0096L\2\u06ee\u06ed\3\2\2\2\u06ef"+
		"\u06f2\3\2\2\2\u06f0\u06ee\3\2\2\2\u06f0\u06f1\3\2\2\2\u06f1\u06f4\3\2"+
		"\2\2\u06f2\u06f0\3\2\2\2\u06f3\u06f5\5z>\2\u06f4\u06f3\3\2\2\2\u06f4\u06f5"+
		"\3\2\2\2\u06f5\u06f7\3\2\2\2\u06f6\u06f8\5\u0086D\2\u06f7\u06f6\3\2\2"+
		"\2\u06f7\u06f8\3\2\2\2\u06f8\u06fa\3\2\2\2\u06f9\u06fb\5|?\2\u06fa\u06f9"+
		"\3\2\2\2\u06fa\u06fb\3\2\2\2\u06fb\u06fd\3\2\2\2\u06fc\u06fe\5\u010a\u0086"+
		"\2\u06fd\u06fc\3\2\2\2\u06fd\u06fe\3\2\2\2\u06fe\u06ff\3\2\2\2\u06ff\u0700"+
		"\5X-\2\u0700\u0702\3\2\2\2\u0701\u06e6\3\2\2\2\u0701\u06ec\3\2\2\2\u0702"+
		"e\3\2\2\2\u0703\u0705\5h\65\2\u0704\u0706\5\u0082B\2\u0705\u0704\3\2\2"+
		"\2\u0705\u0706\3\2\2\2\u0706\u070a\3\2\2\2\u0707\u0709\5\u0096L\2\u0708"+
		"\u0707\3\2\2\2\u0709\u070c\3\2\2\2\u070a\u0708\3\2\2\2\u070a\u070b\3\2"+
		"\2\2\u070b\u070e\3\2\2\2\u070c\u070a\3\2\2\2\u070d\u070f\5z>\2\u070e\u070d"+
		"\3\2\2\2\u070e\u070f\3\2\2\2\u070f\u0711\3\2\2\2\u0710\u0712\5\u0086D"+
		"\2\u0711\u0710\3\2\2\2\u0711\u0712\3\2\2\2\u0712\u0714\3\2\2\2\u0713\u0715"+
		"\5|?\2\u0714\u0713\3\2\2\2\u0714\u0715\3\2\2\2\u0715\u0717\3\2\2\2\u0716"+
		"\u0718\5\u010a\u0086\2\u0717\u0716\3\2\2\2\u0717\u0718\3\2\2\2\u0718\u0730"+
		"\3\2\2\2\u0719\u071b\5j\66\2\u071a\u071c\5\u0082B\2\u071b\u071a\3\2\2"+
		"\2\u071b\u071c\3\2\2\2\u071c\u0720\3\2\2\2\u071d\u071f\5\u0096L\2\u071e"+
		"\u071d\3\2\2\2\u071f\u0722\3\2\2\2\u0720\u071e\3\2\2\2\u0720\u0721\3\2"+
		"\2\2\u0721\u0724\3\2\2\2\u0722\u0720\3\2\2\2\u0723\u0725\5z>\2\u0724\u0723"+
		"\3\2\2\2\u0724\u0725\3\2\2\2\u0725\u0727\3\2\2\2\u0726\u0728\5\u0086D"+
		"\2\u0727\u0726\3\2\2\2\u0727\u0728\3\2\2\2\u0728\u072a\3\2\2\2\u0729\u072b"+
		"\5|?\2\u072a\u0729\3\2\2\2\u072a\u072b\3\2\2\2\u072b\u072d\3\2\2\2\u072c"+
		"\u072e\5\u010a\u0086\2\u072d\u072c\3\2\2\2\u072d\u072e\3\2\2\2\u072e\u0730"+
		"\3\2\2\2\u072f\u0703\3\2\2\2\u072f\u0719\3\2\2\2\u0730g\3\2\2\2\u0731"+
		"\u0732\7\u00de\2\2\u0732\u0733\7\u0106\2\2\u0733\u0735\7\7\2\2\u0734\u0736"+
		"\5\u0098M\2\u0735\u0734\3\2\2\2\u0735\u0736\3\2\2\2\u0736\u0737\3\2\2"+
		"\2\u0737\u0738\5\u00d6l\2\u0738\u0739\7\b\2\2\u0739\u0745\3\2\2\2\u073a"+
		"\u073c\7\u0097\2\2\u073b\u073d\5\u0098M\2\u073c\u073b\3\2\2\2\u073c\u073d"+
		"\3\2\2\2\u073d\u073e\3\2\2\2\u073e\u0745\5\u00d6l\2\u073f\u0741\7\u00c8"+
		"\2\2\u0740\u0742\5\u0098M\2\u0741\u0740\3\2\2\2\u0741\u0742\3\2\2\2\u0742"+
		"\u0743\3\2\2\2\u0743\u0745\5\u00d6l\2\u0744\u0731\3\2\2\2\u0744\u073a"+
		"\3\2\2\2\u0744\u073f\3\2\2\2\u0745\u0747\3\2\2\2\u0746\u0748\5\u00ba^"+
		"\2\u0747\u0746\3\2\2\2\u0747\u0748\3\2\2\2\u0748\u074b\3\2\2\2\u0749\u074a"+
		"\7\u00c6\2\2\u074a\u074c\7\u013a\2\2\u074b\u0749\3\2\2\2\u074b\u074c\3"+
		"\2\2\2\u074c\u074d\3\2\2\2\u074d\u074e\7\u0117\2\2\u074e\u075b\7\u013a"+
		"\2\2\u074f\u0759\7\27\2\2\u0750\u075a\5\u00a8U\2\u0751\u075a\5\u0100\u0081"+
		"\2\u0752\u0755\7\7\2\2\u0753\u0756\5\u00a8U\2\u0754\u0756\5\u0100\u0081"+
		"\2\u0755\u0753\3\2\2\2\u0755\u0754\3\2\2\2\u0756\u0757\3\2\2\2\u0757\u0758"+
		"\7\b\2\2\u0758\u075a\3\2\2\2\u0759\u0750\3\2\2\2\u0759\u0751\3\2\2\2\u0759"+
		"\u0752\3\2\2\2\u075a\u075c\3\2\2\2\u075b\u074f\3\2\2\2\u075b\u075c\3\2"+
		"\2\2\u075c\u075e\3\2\2\2\u075d\u075f\5\u00ba^\2\u075e\u075d\3\2\2\2\u075e"+
		"\u075f\3\2\2\2\u075f\u0762\3\2\2\2\u0760\u0761\7\u00c5\2\2\u0761\u0763"+
		"\7\u013a\2\2\u0762\u0760\3\2\2\2\u0762\u0763\3\2\2\2\u0763i\3\2\2\2\u0764"+
		"\u0768\7\u00de\2\2\u0765\u0767\5~@\2\u0766\u0765\3\2\2\2\u0767\u076a\3"+
		"\2\2\2\u0768\u0766\3\2\2\2\u0768\u0769\3\2\2\2\u0769\u076c\3\2\2\2\u076a"+
		"\u0768\3\2\2\2\u076b\u076d\5\u0098M\2\u076c\u076b\3\2\2\2\u076c\u076d"+
		"\3\2\2\2\u076d\u076e\3\2\2\2\u076e\u076f\5\u00caf\2\u076fk\3\2\2\2\u0770"+
		"\u0771\7\u00e4\2\2\u0771\u0772\5v<\2\u0772m\3\2\2\2\u0773\u0774\7\u011d"+
		"\2\2\u0774\u0777\7\u0098\2\2\u0775\u0776\7\22\2\2\u0776\u0778\5\u00d8"+
		"m\2\u0777\u0775\3\2\2\2\u0777\u0778\3\2\2\2\u0778\u0779\3\2\2\2\u0779"+
		"\u077a\7\u00fc\2\2\u077a\u077b\5r:\2\u077bo\3\2\2\2\u077c\u077d\7\u011d"+
		"\2\2\u077d\u077e\7\u00a3\2\2\u077e\u0781\7\u0098\2\2\u077f\u0780\7\22"+
		"\2\2\u0780\u0782\5\u00d8m\2\u0781\u077f\3\2\2\2\u0781\u0782\3\2\2\2\u0782"+
		"\u0783\3\2\2\2\u0783\u0784\7\u00fc\2\2\u0784\u0785\5t;\2\u0785q\3\2\2"+
		"\2\u0786\u078e\7I\2\2\u0787\u0788\7\u0114\2\2\u0788\u0789\7\u00e4\2\2"+
		"\u0789\u078e\7\u012e\2\2\u078a\u078b\7\u0114\2\2\u078b\u078c\7\u00e4\2"+
		"\2\u078c\u078e\5v<\2\u078d\u0786\3\2\2\2\u078d\u0787\3\2\2\2\u078d\u078a"+
		"\3\2\2\2\u078es\3\2\2\2\u078f\u0790\7~\2\2\u0790\u07a2\7\u012e\2\2\u0791"+
		"\u0792\7~\2\2\u0792\u0793\7\7\2\2\u0793\u0794\5\u00bc_\2\u0794\u0795\7"+
		"\b\2\2\u0795\u0796\7\u0118\2\2\u0796\u0797\7\7\2\2\u0797\u079c\5\u00d4"+
		"k\2\u0798\u0799\7\t\2\2\u0799\u079b\5\u00d4k\2\u079a\u0798\3\2\2\2\u079b"+
		"\u079e\3\2\2\2\u079c\u079a\3\2\2\2\u079c\u079d\3\2\2\2\u079d\u079f\3\2"+
		"\2\2\u079e\u079c\3\2\2\2\u079f\u07a0\7\b\2\2\u07a0\u07a2\3\2\2\2\u07a1"+
		"\u078f\3\2\2\2\u07a1\u0791\3\2\2\2\u07a2u\3\2\2\2\u07a3\u07a8\5x=\2\u07a4"+
		"\u07a5\7\t\2\2\u07a5\u07a7\5x=\2\u07a6\u07a4\3\2\2\2\u07a7\u07aa\3\2\2"+
		"\2\u07a8\u07a6\3\2\2\2\u07a8\u07a9\3\2\2\2\u07a9w\3\2\2\2\u07aa\u07a8"+
		"\3\2\2\2\u07ab\u07ac\5\u00be`\2\u07ac\u07ad\7\u0124\2\2\u07ad\u07ae\5"+
		"\u00d4k\2\u07aey\3\2\2\2\u07af\u07b0\7\u011e\2\2\u07b0\u07b1\5\u00d8m"+
		"\2\u07b1{\3\2\2\2\u07b2\u07b3\7s\2\2\u07b3\u07b4\5\u00d8m\2\u07b4}\3\2"+
		"\2\2\u07b5\u07b6\7\u0138\2\2\u07b6\u07bd\5\u0080A\2\u07b7\u07b9\7\t\2"+
		"\2\u07b8\u07b7\3\2\2\2\u07b8\u07b9\3\2\2\2\u07b9\u07ba\3\2\2\2\u07ba\u07bc"+
		"\5\u0080A\2\u07bb\u07b8\3\2\2\2\u07bc\u07bf\3\2\2\2\u07bd\u07bb\3\2\2"+
		"\2\u07bd\u07be\3\2\2\2\u07be\u07c0\3\2\2\2\u07bf\u07bd\3\2\2\2\u07c0\u07c1"+
		"\7\u0139\2\2\u07c1\177\3\2\2\2\u07c2\u07d0\5\u011e\u0090\2\u07c3\u07c4"+
		"\5\u011e\u0090\2\u07c4\u07c5\7\7\2\2\u07c5\u07ca\5\u00e0q\2\u07c6\u07c7"+
		"\7\t\2\2\u07c7\u07c9\5\u00e0q\2\u07c8\u07c6\3\2\2\2\u07c9\u07cc\3\2\2"+
		"\2\u07ca\u07c8\3\2\2\2\u07ca\u07cb\3\2\2\2\u07cb\u07cd\3\2\2\2\u07cc\u07ca"+
		"\3\2\2\2\u07cd\u07ce\7\b\2\2\u07ce\u07d0\3\2\2\2\u07cf\u07c2\3\2\2\2\u07cf"+
		"\u07c3\3\2\2\2\u07d0\u0081\3\2\2\2\u07d1\u07d2\7k\2\2\u07d2\u07d7\5\u009a"+
		"N\2\u07d3\u07d4\7\t\2\2\u07d4\u07d6\5\u009aN\2\u07d5\u07d3\3\2\2\2\u07d6"+
		"\u07d9\3\2\2\2\u07d7\u07d5\3\2\2\2\u07d7\u07d8\3\2\2\2\u07d8\u07dd\3\2"+
		"\2\2\u07d9\u07d7\3\2\2\2\u07da\u07dc\5\u0096L\2\u07db\u07da\3\2\2\2\u07dc"+
		"\u07df\3\2\2\2\u07dd\u07db\3\2\2\2\u07dd\u07de\3\2\2\2\u07de\u07e1\3\2"+
		"\2\2\u07df\u07dd\3\2\2\2\u07e0\u07e2\5\u0090I\2\u07e1\u07e0\3\2\2\2\u07e1"+
		"\u07e2\3\2\2\2\u07e2\u0083\3\2\2\2\u07e3\u07e5\7g\2\2\u07e4\u07e3\3\2"+
		"\2\2\u07e4\u07e5\3\2\2\2\u07e5\u07e6\3\2\2\2\u07e6\u07e7\t\25\2\2\u07e7"+
		"\u07e8\7\27\2\2\u07e8\u07e9\7\u00a6\2\2\u07e9\u07f2\t\26\2\2\u07ea\u07ec"+
		"\7g\2\2\u07eb\u07ea\3\2\2\2\u07eb\u07ec\3\2\2\2\u07ec\u07ed\3\2\2\2\u07ed"+
		"\u07ee\t\27\2\2\u07ee\u07ef\7\27\2\2\u07ef\u07f0\7\u00a6\2\2\u07f0\u07f2"+
		"\5\u00dco\2\u07f1\u07e4\3\2\2\2\u07f1\u07eb\3\2\2\2\u07f2\u0085\3\2\2"+
		"\2\u07f3\u07f4\7q\2\2\u07f4\u07f5\7\37\2\2\u07f5\u07fa\5\u0088E\2\u07f6"+
		"\u07f7\7\t\2\2\u07f7\u07f9\5\u0088E\2\u07f8\u07f6\3\2\2\2\u07f9\u07fc"+
		"\3\2\2\2\u07fa\u07f8\3\2\2\2\u07fa\u07fb\3\2\2\2\u07fb\u081b\3\2\2\2\u07fc"+
		"\u07fa\3\2\2\2\u07fd\u07fe\7q\2\2\u07fe\u07ff\7\37\2\2\u07ff\u0804\5\u00d4"+
		"k\2\u0800\u0801\7\t\2\2\u0801\u0803\5\u00d4k\2\u0802\u0800\3\2\2\2\u0803"+
		"\u0806\3\2\2\2\u0804\u0802\3\2\2\2\u0804\u0805\3\2\2\2\u0805\u0818\3\2"+
		"\2\2\u0806\u0804\3\2\2\2\u0807\u0808\7\u0120\2\2\u0808\u0819\7\u00d8\2"+
		"\2\u0809\u080a\7\u0120\2\2\u080a\u0819\7:\2\2\u080b\u080c\7r\2\2\u080c"+
		"\u080d\7\u00e6\2\2\u080d\u080e\7\7\2\2\u080e\u0813\5\u008eH\2\u080f\u0810"+
		"\7\t\2\2\u0810\u0812\5\u008eH\2\u0811\u080f\3\2\2\2\u0812\u0815\3\2\2"+
		"\2\u0813\u0811\3\2\2\2\u0813\u0814\3\2\2\2\u0814\u0816\3\2\2\2\u0815\u0813"+
		"\3\2\2\2\u0816\u0817\7\b\2\2\u0817\u0819\3\2\2\2\u0818\u0807\3\2\2\2\u0818"+
		"\u0809\3\2\2\2\u0818\u080b\3\2\2\2\u0818\u0819\3\2\2\2\u0819\u081b\3\2"+
		"\2\2\u081a\u07f3\3\2\2\2\u081a\u07fd\3\2\2\2\u081b\u0087\3\2\2\2\u081c"+
		"\u081f\5\u008aF\2\u081d\u081f\5\u00d4k\2\u081e\u081c\3\2\2\2\u081e";
	private static final String _serializedATNSegment1 =
		"\u081d\3\2\2\2\u081f\u0089\3\2\2\2\u0820\u0821\t\30\2\2\u0821\u0822\7"+
		"\7\2\2\u0822\u0827\5\u008eH\2\u0823\u0824\7\t\2\2\u0824\u0826\5\u008e"+
		"H\2\u0825\u0823\3\2\2\2\u0826\u0829\3\2\2\2\u0827\u0825\3\2\2\2\u0827"+
		"\u0828\3\2\2\2\u0828\u082a\3\2\2\2\u0829\u0827\3\2\2\2\u082a\u082b\7\b"+
		"\2\2\u082b\u083a\3\2\2\2\u082c\u082d\7r\2\2\u082d\u082e\7\u00e6\2\2\u082e"+
		"\u082f\7\7\2\2\u082f\u0834\5\u008cG\2\u0830\u0831\7\t\2\2\u0831\u0833"+
		"\5\u008cG\2\u0832\u0830\3\2\2\2\u0833\u0836\3\2\2\2\u0834\u0832\3\2\2"+
		"\2\u0834\u0835\3\2\2\2\u0835\u0837\3\2\2\2\u0836\u0834\3\2\2\2\u0837\u0838"+
		"\7\b\2\2\u0838\u083a\3\2\2\2\u0839\u0820\3\2\2\2\u0839\u082c\3\2\2\2\u083a"+
		"\u008b\3\2\2\2\u083b\u083e\5\u008aF\2\u083c\u083e\5\u008eH\2\u083d\u083b"+
		"\3\2\2\2\u083d\u083c\3\2\2\2\u083e\u008d\3\2\2\2\u083f\u0848\7\7\2\2\u0840"+
		"\u0845\5\u00d4k\2\u0841\u0842\7\t\2\2\u0842\u0844\5\u00d4k\2\u0843\u0841"+
		"\3\2\2\2\u0844\u0847\3\2\2\2\u0845\u0843\3\2\2\2\u0845\u0846\3\2\2\2\u0846"+
		"\u0849\3\2\2\2\u0847\u0845\3\2\2\2\u0848\u0840\3\2\2\2\u0848\u0849\3\2"+
		"\2\2\u0849\u084a\3\2\2\2\u084a\u084d\7\b\2\2\u084b\u084d\5\u00d4k\2\u084c"+
		"\u083f\3\2\2\2\u084c\u084b\3\2\2\2\u084d\u008f\3\2\2\2\u084e\u084f\7\u00ba"+
		"\2\2\u084f\u0850\7\7\2\2\u0850\u0851\5\u00caf\2\u0851\u0852\7g\2\2\u0852"+
		"\u0853\5\u0092J\2\u0853\u0854\7x\2\2\u0854\u0855\7\7\2\2\u0855\u085a\5"+
		"\u0094K\2\u0856\u0857\7\t\2\2\u0857\u0859\5\u0094K\2\u0858\u0856\3\2\2"+
		"\2\u0859\u085c\3\2\2\2\u085a\u0858\3\2\2\2\u085a\u085b\3\2\2\2\u085b\u085d"+
		"\3\2\2\2\u085c\u085a\3\2\2\2\u085d\u085e\7\b\2\2\u085e\u085f\7\b\2\2\u085f"+
		"\u0091\3\2\2\2\u0860\u086d\5\u011e\u0090\2\u0861\u0862\7\7\2\2\u0862\u0867"+
		"\5\u011e\u0090\2\u0863\u0864\7\t\2\2\u0864\u0866\5\u011e\u0090\2\u0865"+
		"\u0863\3\2\2\2\u0866\u0869\3\2\2\2\u0867\u0865\3\2\2\2\u0867\u0868\3\2"+
		"\2\2\u0868\u086a\3\2\2\2\u0869\u0867\3\2\2\2\u086a\u086b\7\b\2\2\u086b"+
		"\u086d\3\2\2\2\u086c\u0860\3\2\2\2\u086c\u0861\3\2\2\2\u086d\u0093\3\2"+
		"\2\2\u086e\u0873\5\u00d4k\2\u086f\u0871\7\27\2\2\u0870\u086f\3\2\2\2\u0870"+
		"\u0871\3\2\2\2\u0871\u0872\3\2\2\2\u0872\u0874\5\u011e\u0090\2\u0873\u0870"+
		"\3\2\2\2\u0873\u0874\3\2\2\2\u0874\u0095\3\2\2\2\u0875\u0876\7\u0087\2"+
		"\2\u0876\u0878\7\u011a\2\2\u0877\u0879\7\u00ae\2\2\u0878\u0877\3\2\2\2"+
		"\u0878\u0879\3\2\2\2\u0879\u087a\3\2\2\2\u087a\u087b\5\u0118\u008d\2\u087b"+
		"\u0884\7\7\2\2\u087c\u0881\5\u00d4k\2\u087d\u087e\7\t\2\2\u087e\u0880"+
		"\5\u00d4k\2\u087f\u087d\3\2\2\2\u0880\u0883\3\2\2\2\u0881\u087f\3\2\2"+
		"\2\u0881\u0882\3\2\2\2\u0882\u0885\3\2\2\2\u0883\u0881\3\2\2\2\u0884\u087c"+
		"\3\2\2\2\u0884\u0885\3\2\2\2\u0885\u0886\3\2\2\2\u0886\u0887\7\b\2\2\u0887"+
		"\u0893\5\u011e\u0090\2\u0888\u088a\7\27\2\2\u0889\u0888\3\2\2\2\u0889"+
		"\u088a\3\2\2\2\u088a\u088b\3\2\2\2\u088b\u0890\5\u011e\u0090\2\u088c\u088d"+
		"\7\t\2\2\u088d\u088f\5\u011e\u0090\2\u088e\u088c\3\2\2\2\u088f\u0892\3"+
		"\2\2\2\u0890\u088e\3\2\2\2\u0890\u0891\3\2\2\2\u0891\u0894\3\2\2\2\u0892"+
		"\u0890\3\2\2\2\u0893\u0889\3\2\2\2\u0893\u0894\3\2\2\2\u0894\u0097\3\2"+
		"\2\2\u0895\u0896\t\31\2\2\u0896\u0099\3\2\2\2\u0897\u0899\7\u0087\2\2"+
		"\u0898\u0897\3\2\2\2\u0898\u0899\3\2\2\2\u0899\u089a\3\2\2\2\u089a\u089e"+
		"\5\u00b2Z\2\u089b\u089d\5\u009cO\2\u089c\u089b\3\2\2\2\u089d\u08a0\3\2"+
		"\2\2\u089e\u089c\3\2\2\2\u089e\u089f\3\2\2\2\u089f\u009b\3\2\2\2\u08a0"+
		"\u089e\3\2\2\2\u08a1\u08a2\5\u009eP\2\u08a2\u08a4\7\u0084\2\2\u08a3\u08a5"+
		"\7\u0087\2\2\u08a4\u08a3\3\2\2\2\u08a4\u08a5\3\2\2\2\u08a5\u08a6\3\2\2"+
		"\2\u08a6\u08a8\5\u00b2Z\2\u08a7\u08a9\5\u00a0Q\2\u08a8\u08a7\3\2\2\2\u08a8"+
		"\u08a9\3\2\2\2\u08a9\u08b3\3\2\2\2\u08aa\u08ab\7\u00a1\2\2\u08ab\u08ac"+
		"\5\u009eP\2\u08ac\u08ae\7\u0084\2\2\u08ad\u08af\7\u0087\2\2\u08ae\u08ad"+
		"\3\2\2\2\u08ae\u08af\3\2\2\2\u08af\u08b0\3\2\2\2\u08b0\u08b1\5\u00b2Z"+
		"\2\u08b1\u08b3\3\2\2\2\u08b2\u08a1\3\2\2\2\u08b2\u08aa\3\2\2\2\u08b3\u009d"+
		"\3\2\2\2\u08b4\u08b6\7{\2\2\u08b5\u08b4\3\2\2\2\u08b5\u08b6\3\2\2\2\u08b6"+
		"\u08cd\3\2\2\2\u08b7\u08cd\79\2\2\u08b8\u08ba\7\u008a\2\2\u08b9\u08bb"+
		"\7\u00ae\2\2\u08ba\u08b9\3\2\2\2\u08ba\u08bb\3\2\2\2\u08bb\u08cd\3\2\2"+
		"\2\u08bc\u08be\7\u008a\2\2\u08bd\u08bc\3\2\2\2\u08bd\u08be\3\2\2\2\u08be"+
		"\u08bf\3\2\2\2\u08bf\u08cd\7\u00df\2\2\u08c0\u08c2\7\u00d3\2\2\u08c1\u08c3"+
		"\7\u00ae\2\2\u08c2\u08c1\3\2\2\2\u08c2\u08c3\3\2\2\2\u08c3\u08cd\3\2\2"+
		"\2\u08c4\u08c6\7l\2\2\u08c5\u08c7\7\u00ae\2\2\u08c6\u08c5\3\2\2\2\u08c6"+
		"\u08c7\3\2\2\2\u08c7\u08cd\3\2\2\2\u08c8\u08ca\7\u008a\2\2\u08c9\u08c8"+
		"\3\2\2\2\u08c9\u08ca\3\2\2\2\u08ca\u08cb\3\2\2\2\u08cb\u08cd\7\23\2\2"+
		"\u08cc\u08b5\3\2\2\2\u08cc\u08b7\3\2\2\2\u08cc\u08b8\3\2\2\2\u08cc\u08bd"+
		"\3\2\2\2\u08cc\u08c0\3\2\2\2\u08cc\u08c4\3\2\2\2\u08cc\u08c9\3\2\2\2\u08cd"+
		"\u009f\3\2\2\2\u08ce\u08cf\7\u00a7\2\2\u08cf\u08d3\5\u00d8m\2\u08d0\u08d1"+
		"\7\u0117\2\2\u08d1\u08d3\5\u00a6T\2\u08d2\u08ce\3\2\2\2\u08d2\u08d0\3"+
		"\2\2\2\u08d3\u00a1\3\2\2\2\u08d4\u08d5\7\u00f8\2\2\u08d5\u08d7\7\7\2\2"+
		"\u08d6\u08d8\5\u00a4S\2\u08d7\u08d6\3\2\2\2\u08d7\u08d8\3\2\2\2\u08d8"+
		"\u08d9\3\2\2\2\u08d9\u08de\7\b\2\2\u08da\u08db\7\u00cd\2\2\u08db\u08dc"+
		"\7\7\2\2\u08dc\u08dd\7\u013e\2\2\u08dd\u08df\7\b\2\2\u08de\u08da\3\2\2"+
		"\2\u08de\u08df\3\2\2\2\u08df\u00a3\3\2\2\2\u08e0\u08e2\7\u012d\2\2\u08e1"+
		"\u08e0\3\2\2\2\u08e1\u08e2\3\2\2\2\u08e2\u08e3\3\2\2\2\u08e3\u08e4\t\32"+
		"\2\2\u08e4\u08f9\7\u00b9\2\2\u08e5\u08e6\5\u00d4k\2\u08e6\u08e7\7\u00da"+
		"\2\2\u08e7\u08f9\3\2\2\2\u08e8\u08e9\7\35\2\2\u08e9\u08ea\7\u013e\2\2"+
		"\u08ea\u08eb\7\u00ad\2\2\u08eb\u08ec\7\u00a6\2\2\u08ec\u08f5\7\u013e\2"+
		"\2\u08ed\u08f3\7\u00a7\2\2\u08ee\u08f4\5\u011e\u0090\2\u08ef\u08f0\5\u0118"+
		"\u008d\2\u08f0\u08f1\7\7\2\2\u08f1\u08f2\7\b\2\2\u08f2\u08f4\3\2\2\2\u08f3"+
		"\u08ee\3\2\2\2\u08f3\u08ef\3\2\2\2\u08f4\u08f6\3\2\2\2\u08f5\u08ed\3\2"+
		"\2\2\u08f5\u08f6\3\2\2\2\u08f6\u08f9\3\2\2\2\u08f7\u08f9\5\u00d4k\2\u08f8"+
		"\u08e1\3\2\2\2\u08f8\u08e5\3\2\2\2\u08f8\u08e8\3\2\2\2\u08f8\u08f7\3\2"+
		"\2\2\u08f9\u00a5\3\2\2\2\u08fa\u08fb\7\7\2\2\u08fb\u08fc\5\u00a8U\2\u08fc"+
		"\u08fd\7\b\2\2\u08fd\u00a7\3\2\2\2\u08fe\u0903\5\u011a\u008e\2\u08ff\u0900"+
		"\7\t\2\2\u0900\u0902\5\u011a\u008e\2\u0901\u08ff\3\2\2\2\u0902\u0905\3"+
		"\2\2\2\u0903\u0901\3\2\2\2\u0903\u0904\3\2\2\2\u0904\u00a9\3\2\2\2\u0905"+
		"\u0903\3\2\2\2\u0906\u0907\7\7\2\2\u0907\u090c\5\u00acW\2\u0908\u0909"+
		"\7\t\2\2\u0909\u090b\5\u00acW\2\u090a\u0908\3\2\2\2\u090b\u090e\3\2\2"+
		"\2\u090c\u090a\3\2\2\2\u090c\u090d\3\2\2\2\u090d\u090f\3\2\2\2\u090e\u090c"+
		"\3\2\2\2\u090f\u0910\7\b\2\2\u0910\u00ab\3\2\2\2\u0911\u0913\5\u011a\u008e"+
		"\2\u0912\u0914\t\23\2\2\u0913\u0912\3\2\2\2\u0913\u0914\3\2\2\2\u0914"+
		"\u00ad\3\2\2\2\u0915\u0916\7\7\2\2\u0916\u091b\5\u00b0Y\2\u0917\u0918"+
		"\7\t\2\2\u0918\u091a\5\u00b0Y\2\u0919\u0917\3\2\2\2\u091a\u091d\3\2\2"+
		"\2\u091b\u0919\3\2\2\2\u091b\u091c\3\2\2\2\u091c\u091e\3\2\2\2\u091d\u091b"+
		"\3\2\2\2\u091e\u091f\7\b\2\2\u091f\u00af\3\2\2\2\u0920\u0922\5\u011e\u0090"+
		"\2\u0921\u0923\5&\24\2\u0922\u0921\3\2\2\2\u0922\u0923\3\2\2\2\u0923\u00b1"+
		"\3\2\2\2\u0924\u0926\5\u00be`\2\u0925\u0927\5\u0084C\2\u0926\u0925\3\2"+
		"\2\2\u0926\u0927\3\2\2\2\u0927\u0929\3\2\2\2\u0928\u092a\5\u00a2R\2\u0929"+
		"\u0928\3\2\2\2\u0929\u092a\3\2\2\2\u092a\u092b\3\2\2\2\u092b\u092c\5\u00b8"+
		"]\2\u092c\u0940\3\2\2\2\u092d\u092e\7\7\2\2\u092e\u092f\5(\25\2\u092f"+
		"\u0931\7\b\2\2\u0930\u0932\5\u00a2R\2\u0931\u0930\3\2\2\2\u0931\u0932"+
		"\3\2\2\2\u0932\u0933\3\2\2\2\u0933\u0934\5\u00b8]\2\u0934\u0940\3\2\2"+
		"\2\u0935\u0936\7\7\2\2\u0936\u0937\5\u009aN\2\u0937\u0939\7\b\2\2\u0938"+
		"\u093a\5\u00a2R\2\u0939\u0938\3\2\2\2\u0939\u093a\3\2\2\2\u093a\u093b"+
		"\3\2\2\2\u093b\u093c\5\u00b8]\2\u093c\u0940\3\2\2\2\u093d\u0940\5\u00b4"+
		"[\2\u093e\u0940\5\u00b6\\\2\u093f\u0924\3\2\2\2\u093f\u092d\3\2\2\2\u093f"+
		"\u0935\3\2\2\2\u093f\u093d\3\2\2\2\u093f\u093e\3\2\2\2\u0940\u00b3\3\2"+
		"\2\2\u0941\u0942\7\u0118\2\2\u0942\u0947\5\u00d4k\2\u0943\u0944\7\t\2"+
		"\2\u0944\u0946\5\u00d4k\2\u0945\u0943\3\2\2\2\u0946\u0949\3\2\2\2\u0947"+
		"\u0945\3\2\2\2\u0947\u0948\3\2\2\2\u0948\u094a\3\2\2\2\u0949\u0947\3\2"+
		"\2\2\u094a\u094b\5\u00b8]\2\u094b\u00b5\3\2\2\2\u094c\u094d\5\u0116\u008c"+
		"\2\u094d\u0956\7\7\2\2\u094e\u0953\5\u00d4k\2\u094f\u0950\7\t\2\2\u0950"+
		"\u0952\5\u00d4k\2\u0951\u094f\3\2\2\2\u0952\u0955\3\2\2\2\u0953\u0951"+
		"\3\2\2\2\u0953\u0954\3\2\2\2\u0954\u0957\3\2\2\2\u0955\u0953\3\2\2\2\u0956"+
		"\u094e\3\2\2\2\u0956\u0957\3\2\2\2\u0957\u0958\3\2\2\2\u0958\u0959\7\b"+
		"\2\2\u0959\u095a\5\u00b8]\2\u095a\u00b7\3\2\2\2\u095b\u095d\7\27\2\2\u095c"+
		"\u095b\3\2\2\2\u095c\u095d\3\2\2\2\u095d\u095e\3\2\2\2\u095e\u0960\5\u0120"+
		"\u0091\2\u095f\u0961\5\u00a6T\2\u0960\u095f\3\2\2\2\u0960\u0961\3\2\2"+
		"\2\u0961\u0963\3\2\2\2\u0962\u095c\3\2\2\2\u0962\u0963\3\2\2\2\u0963\u00b9"+
		"\3\2\2\2\u0964\u0965\7\u00d9\2\2\u0965\u0966\7i\2\2\u0966\u0967\7\u00e1"+
		"\2\2\u0967\u096b\7\u013a\2\2\u0968\u0969\7\u0120\2\2\u0969\u096a\7\u00e2"+
		"\2\2\u096a\u096c\5B\"\2\u096b\u0968\3\2\2\2\u096b\u096c\3\2\2\2\u096c"+
		"\u0996\3\2\2\2\u096d\u096e\7\u00d9\2\2\u096e\u096f\7i\2\2\u096f\u0979"+
		"\7J\2\2\u0970\u0971\7b\2\2\u0971\u0972\7\u00fb\2\2\u0972\u0973\7\37\2"+
		"\2\u0973\u0977\7\u013a\2\2\u0974\u0975\7W\2\2\u0975\u0976\7\37\2\2\u0976"+
		"\u0978\7\u013a\2\2\u0977\u0974\3\2\2\2\u0977\u0978\3\2\2\2\u0978\u097a"+
		"\3\2\2\2\u0979\u0970\3\2\2\2\u0979\u097a\3\2\2\2\u097a\u0980\3\2\2\2\u097b"+
		"\u097c\7-\2\2\u097c\u097d\7\u0083\2\2\u097d\u097e\7\u00fb\2\2\u097e\u097f"+
		"\7\37\2\2\u097f\u0981\7\u013a\2\2\u0980\u097b\3\2\2\2\u0980\u0981\3\2"+
		"\2\2\u0981\u0987\3\2\2\2\u0982\u0983\7\u0097\2\2\u0983\u0984\7\u0085\2"+
		"\2\u0984\u0985\7\u00fb\2\2\u0985\u0986\7\37\2\2\u0986\u0988\7\u013a\2"+
		"\2\u0987\u0982\3\2\2\2\u0987\u0988\3\2\2\2\u0988\u098d\3\2\2\2\u0989\u098a"+
		"\7\u008e\2\2\u098a\u098b\7\u00fb\2\2\u098b\u098c\7\37\2\2\u098c\u098e"+
		"\7\u013a\2\2\u098d\u0989\3\2\2\2\u098d\u098e\3\2\2\2\u098e\u0993\3\2\2"+
		"\2\u098f\u0990\7\u00a4\2\2\u0990\u0991\7H\2\2\u0991\u0992\7\27\2\2\u0992"+
		"\u0994\7\u013a\2\2\u0993\u098f\3\2\2\2\u0993\u0994\3\2\2\2\u0994\u0996"+
		"\3\2\2\2\u0995\u0964\3\2\2\2\u0995\u096d\3\2\2\2\u0996\u00bb\3\2\2\2\u0997"+
		"\u099c\5\u00be`\2\u0998\u0999\7\t\2\2\u0999\u099b\5\u00be`\2\u099a\u0998"+
		"\3\2\2\2\u099b\u099e\3\2\2\2\u099c\u099a\3\2\2\2\u099c\u099d\3\2\2\2\u099d"+
		"\u00bd\3\2\2\2\u099e\u099c\3\2\2\2\u099f\u09a4\5\u011a\u008e\2\u09a0\u09a1"+
		"\7\n\2\2\u09a1\u09a3\5\u011a\u008e\2\u09a2\u09a0\3\2\2\2\u09a3\u09a6\3"+
		"\2\2\2\u09a4\u09a2\3\2\2\2\u09a4\u09a5\3\2\2\2\u09a5\u00bf\3\2\2\2\u09a6"+
		"\u09a4\3\2\2\2\u09a7\u09ac\5\u00c2b\2\u09a8\u09a9\7\t\2\2\u09a9\u09ab"+
		"\5\u00c2b\2\u09aa\u09a8\3\2\2\2\u09ab\u09ae\3\2\2\2\u09ac\u09aa\3\2\2"+
		"\2\u09ac\u09ad\3\2\2\2\u09ad\u00c1\3\2\2\2\u09ae\u09ac\3\2\2\2\u09af\u09b2"+
		"\5\u00be`\2\u09b0\u09b1\7\u00aa\2\2\u09b1\u09b3\5B\"\2\u09b2\u09b0\3\2"+
		"\2\2\u09b2\u09b3\3\2\2\2\u09b3\u00c3\3\2\2\2\u09b4\u09b5\5\u011a\u008e"+
		"\2\u09b5\u09b6\7\n\2\2\u09b6\u09b8\3\2\2\2\u09b7\u09b4\3\2\2\2\u09b7\u09b8"+
		"\3\2\2\2\u09b8\u09b9\3\2\2\2\u09b9\u09ba\5\u011a\u008e\2\u09ba\u00c5\3"+
		"\2\2\2\u09bb\u09bc\5\u011a\u008e\2\u09bc\u09bd\7\n\2\2\u09bd\u09bf\3\2"+
		"\2\2\u09be\u09bb\3\2\2\2\u09be\u09bf\3\2\2\2\u09bf\u09c0\3\2\2\2\u09c0"+
		"\u09c1\5\u011a\u008e\2\u09c1\u00c7\3\2\2\2\u09c2\u09ca\5\u00d4k\2\u09c3"+
		"\u09c5\7\27\2\2\u09c4\u09c3\3\2\2\2\u09c4\u09c5\3\2\2\2\u09c5\u09c8\3"+
		"\2\2\2\u09c6\u09c9\5\u011a\u008e\2\u09c7\u09c9\5\u00a6T\2\u09c8\u09c6"+
		"\3\2\2\2\u09c8\u09c7\3\2\2\2\u09c9\u09cb\3\2\2\2\u09ca\u09c4\3\2\2\2\u09ca"+
		"\u09cb\3\2\2\2\u09cb\u00c9\3\2\2\2\u09cc\u09d1\5\u00c8e\2\u09cd\u09ce"+
		"\7\t\2\2\u09ce\u09d0\5\u00c8e\2\u09cf\u09cd\3\2\2\2\u09d0\u09d3\3\2\2"+
		"\2\u09d1\u09cf\3\2\2\2\u09d1\u09d2\3\2\2\2\u09d2\u00cb\3\2\2\2\u09d3\u09d1"+
		"\3\2\2\2\u09d4\u09d5\7\7\2\2\u09d5\u09da\5\u00ceh\2\u09d6\u09d7\7\t\2"+
		"\2\u09d7\u09d9\5\u00ceh\2\u09d8\u09d6\3\2\2\2\u09d9\u09dc\3\2\2\2\u09da"+
		"\u09d8\3\2\2\2\u09da\u09db\3\2\2\2\u09db\u09dd\3\2\2\2\u09dc\u09da\3\2"+
		"\2\2\u09dd\u09de\7\b\2\2\u09de\u00cd\3\2\2\2\u09df\u09e2\5\u00d0i\2\u09e0"+
		"\u09e2\5\u0102\u0082\2\u09e1\u09df\3\2\2\2\u09e1\u09e0\3\2\2\2\u09e2\u00cf"+
		"\3\2\2\2\u09e3\u09f1\5\u0118\u008d\2\u09e4\u09e5\5\u011e\u0090\2\u09e5"+
		"\u09e6\7\7\2\2\u09e6\u09eb\5\u00d2j\2\u09e7\u09e8\7\t\2\2\u09e8\u09ea"+
		"\5\u00d2j\2\u09e9\u09e7\3\2\2\2\u09ea\u09ed\3\2\2\2\u09eb\u09e9\3\2\2"+
		"\2\u09eb\u09ec\3\2\2\2\u09ec\u09ee\3\2\2\2\u09ed\u09eb\3\2\2\2\u09ee\u09ef"+
		"\7\b\2\2\u09ef\u09f1\3\2\2\2\u09f0\u09e3\3\2\2\2\u09f0\u09e4\3\2\2\2\u09f1"+
		"\u00d1\3\2\2\2\u09f2\u09f5\5\u0118\u008d\2\u09f3\u09f5\5\u00e2r\2\u09f4"+
		"\u09f2\3\2\2\2\u09f4\u09f3\3\2\2\2\u09f5\u00d3\3\2\2\2\u09f6\u09f7\5\u00d8"+
		"m\2\u09f7\u00d5\3\2\2\2\u09f8\u09fd\5\u00d4k\2\u09f9\u09fa\7\t\2\2\u09fa"+
		"\u09fc\5\u00d4k\2\u09fb\u09f9\3\2\2\2\u09fc\u09ff\3\2\2\2\u09fd\u09fb"+
		"\3\2\2\2\u09fd\u09fe\3\2\2\2\u09fe\u00d7\3\2\2\2\u09ff\u09fd\3\2\2\2\u0a00"+
		"\u0a01\bm\1\2\u0a01\u0a02\7\u00a3\2\2\u0a02\u0a0d\5\u00d8m\7\u0a03\u0a04"+
		"\7Z\2\2\u0a04\u0a05\7\7\2\2\u0a05\u0a06\5(\25\2\u0a06\u0a07\7\b\2\2\u0a07"+
		"\u0a0d\3\2\2\2\u0a08\u0a0a\5\u00dco\2\u0a09\u0a0b\5\u00dan\2\u0a0a\u0a09"+
		"\3\2\2\2\u0a0a\u0a0b\3\2\2\2\u0a0b\u0a0d\3\2\2\2\u0a0c\u0a00\3\2\2\2\u0a0c"+
		"\u0a03\3\2\2\2\u0a0c\u0a08\3\2\2\2\u0a0d\u0a16\3\2\2\2\u0a0e\u0a0f\f\4"+
		"\2\2\u0a0f\u0a10\7\22\2\2\u0a10\u0a15\5\u00d8m\5\u0a11\u0a12\f\3\2\2\u0a12"+
		"\u0a13\7\u00ab\2\2\u0a13\u0a15\5\u00d8m\4\u0a14\u0a0e\3\2\2\2\u0a14\u0a11"+
		"\3\2\2\2\u0a15\u0a18\3\2\2\2\u0a16\u0a14\3\2\2\2\u0a16\u0a17\3\2\2\2\u0a17"+
		"\u00d9\3\2\2\2\u0a18\u0a16\3\2\2\2\u0a19\u0a1b\7\u00a3\2\2\u0a1a\u0a19"+
		"\3\2\2\2\u0a1a\u0a1b\3\2\2\2\u0a1b\u0a1c\3\2\2\2\u0a1c\u0a1d\7\33\2\2"+
		"\u0a1d\u0a1e\5\u00dco\2\u0a1e\u0a1f\7\22\2\2\u0a1f\u0a20\5\u00dco\2\u0a20"+
		"\u0a6c\3\2\2\2\u0a21\u0a23\7\u00a3\2\2\u0a22\u0a21\3\2\2\2\u0a22\u0a23"+
		"\3\2\2\2\u0a23\u0a24\3\2\2\2\u0a24\u0a25\7x\2\2\u0a25\u0a26\7\7\2\2\u0a26"+
		"\u0a2b\5\u00d4k\2\u0a27\u0a28\7\t\2\2\u0a28\u0a2a\5\u00d4k\2\u0a29\u0a27"+
		"\3\2\2\2\u0a2a\u0a2d\3\2\2\2\u0a2b\u0a29\3\2\2\2\u0a2b\u0a2c\3\2\2\2\u0a2c"+
		"\u0a2e\3\2\2\2\u0a2d\u0a2b\3\2\2\2\u0a2e\u0a2f\7\b\2\2\u0a2f\u0a6c\3\2"+
		"\2\2\u0a30\u0a32\7\u00a3\2\2\u0a31\u0a30\3\2\2\2\u0a31\u0a32\3\2\2\2\u0a32"+
		"\u0a33\3\2\2\2\u0a33\u0a34\7x\2\2\u0a34\u0a35\7\7\2\2\u0a35\u0a36\5(\25"+
		"\2\u0a36\u0a37\7\b\2\2\u0a37\u0a6c\3\2\2\2\u0a38\u0a3a\7\u00a3\2\2\u0a39"+
		"\u0a38\3\2\2\2\u0a39\u0a3a\3\2\2\2\u0a3a\u0a3b\3\2\2\2\u0a3b\u0a3c\7\u00d4"+
		"\2\2\u0a3c\u0a6c\5\u00dco\2\u0a3d\u0a3f\7\u00a3\2\2\u0a3e\u0a3d\3\2\2"+
		"\2\u0a3e\u0a3f\3\2\2\2\u0a3f\u0a40\3\2\2\2\u0a40\u0a41\t\33\2\2\u0a41"+
		"\u0a4f\t\34\2\2\u0a42\u0a43\7\7\2\2\u0a43\u0a50\7\b\2\2\u0a44\u0a45\7"+
		"\7\2\2\u0a45\u0a4a\5\u00d4k\2\u0a46\u0a47\7\t\2\2\u0a47\u0a49\5\u00d4"+
		"k\2\u0a48\u0a46\3\2\2\2\u0a49\u0a4c\3\2\2\2\u0a4a\u0a48\3\2\2\2\u0a4a"+
		"\u0a4b\3\2\2\2\u0a4b\u0a4d\3\2\2\2\u0a4c\u0a4a\3\2\2\2\u0a4d\u0a4e\7\b"+
		"\2\2\u0a4e\u0a50\3\2\2\2\u0a4f\u0a42\3\2\2\2\u0a4f\u0a44\3\2\2\2\u0a50"+
		"\u0a6c\3\2\2\2\u0a51\u0a53\7\u00a3\2\2\u0a52\u0a51\3\2\2\2\u0a52\u0a53"+
		"\3\2\2\2\u0a53\u0a54\3\2\2\2\u0a54\u0a55\t\33\2\2\u0a55\u0a58\5\u00dc"+
		"o\2\u0a56\u0a57\7V\2\2\u0a57\u0a59\7\u013a\2\2\u0a58\u0a56\3\2\2\2\u0a58"+
		"\u0a59\3\2\2\2\u0a59\u0a6c\3\2\2\2\u0a5a\u0a5c\7\u0082\2\2\u0a5b\u0a5d"+
		"\7\u00a3\2\2\u0a5c\u0a5b\3\2\2\2\u0a5c\u0a5d\3\2\2\2\u0a5d\u0a5e\3\2\2"+
		"\2\u0a5e\u0a6c\7\u00a4\2\2\u0a5f\u0a61\7\u0082\2\2\u0a60\u0a62\7\u00a3"+
		"\2\2\u0a61\u0a60\3\2\2\2\u0a61\u0a62\3\2\2\2\u0a62\u0a63\3\2\2\2\u0a63"+
		"\u0a6c\t\35\2\2\u0a64\u0a66\7\u0082\2\2\u0a65\u0a67\7\u00a3\2\2\u0a66"+
		"\u0a65\3\2\2\2\u0a66\u0a67\3\2\2\2\u0a67\u0a68\3\2\2\2\u0a68\u0a69\7P"+
		"\2\2\u0a69\u0a6a\7k\2\2\u0a6a\u0a6c\5\u00dco\2\u0a6b\u0a1a\3\2\2\2\u0a6b"+
		"\u0a22\3\2\2\2\u0a6b\u0a31\3\2\2\2\u0a6b\u0a39\3\2\2\2\u0a6b\u0a3e\3\2"+
		"\2\2\u0a6b\u0a52\3\2\2\2\u0a6b\u0a5a\3\2\2\2\u0a6b\u0a5f\3\2\2\2\u0a6b"+
		"\u0a64\3\2\2\2\u0a6c\u00db\3\2\2\2\u0a6d\u0a6e\bo\1\2\u0a6e\u0a72\5\u00e0"+
		"q\2\u0a6f\u0a70\t\36\2\2\u0a70\u0a72\5\u00dco\t\u0a71\u0a6d\3\2\2\2\u0a71"+
		"\u0a6f\3\2\2\2\u0a72\u0a88\3\2\2\2\u0a73\u0a74\f\b\2\2\u0a74\u0a75\t\37"+
		"\2\2\u0a75\u0a87\5\u00dco\t\u0a76\u0a77\f\7\2\2\u0a77\u0a78\t \2\2\u0a78"+
		"\u0a87\5\u00dco\b\u0a79\u0a7a\f\6\2\2\u0a7a\u0a7b\7\u0132\2\2\u0a7b\u0a87"+
		"\5\u00dco\7\u0a7c\u0a7d\f\5\2\2\u0a7d\u0a7e\7\u0135\2\2\u0a7e\u0a87\5"+
		"\u00dco\6\u0a7f\u0a80\f\4\2\2\u0a80\u0a81\7\u0133\2\2\u0a81\u0a87\5\u00dc"+
		"o\5\u0a82\u0a83\f\3\2\2\u0a83\u0a84\5\u00e4s\2\u0a84\u0a85\5\u00dco\4"+
		"\u0a85\u0a87\3\2\2\2\u0a86\u0a73\3\2\2\2\u0a86\u0a76\3\2\2\2\u0a86\u0a79"+
		"\3\2\2\2\u0a86\u0a7c\3\2\2\2\u0a86\u0a7f\3\2\2\2\u0a86\u0a82\3\2\2\2\u0a87"+
		"\u0a8a\3\2\2\2\u0a88\u0a86\3\2\2\2\u0a88\u0a89\3\2\2\2\u0a89\u00dd\3\2"+
		"\2\2\u0a8a\u0a88\3\2\2\2\u0a8b\u0a8c\t!\2\2\u0a8c\u00df\3\2\2\2\u0a8d"+
		"\u0a8e\bq\1\2\u0a8e\u0b6b\t\"\2\2\u0a8f\u0a90\t#\2\2\u0a90\u0a91\7\7\2"+
		"\2\u0a91\u0a92\5\u00dep\2\u0a92\u0a93\7\t\2\2\u0a93\u0a94\5\u00dco\2\u0a94"+
		"\u0a95\7\t\2\2\u0a95\u0a96\5\u00dco\2\u0a96\u0a97\7\b\2\2\u0a97\u0b6b"+
		"\3\2\2\2\u0a98\u0a99\t$\2\2\u0a99\u0a9a\7\7\2\2\u0a9a\u0a9b\5\u00dep\2"+
		"\u0a9b\u0a9c\7\t\2\2\u0a9c\u0a9d\5\u00dco\2\u0a9d\u0a9e\7\t\2\2\u0a9e"+
		"\u0a9f\5\u00dco\2\u0a9f\u0aa0\7\b\2\2\u0aa0\u0b6b\3\2\2\2\u0aa1\u0aa3"+
		"\7\"\2\2\u0aa2\u0aa4\5\u0108\u0085\2\u0aa3\u0aa2\3\2\2\2\u0aa4\u0aa5\3"+
		"\2\2\2\u0aa5\u0aa3\3\2\2\2\u0aa5\u0aa6\3\2\2\2\u0aa6\u0aa9\3\2\2\2\u0aa7"+
		"\u0aa8\7T\2\2\u0aa8\u0aaa\5\u00d4k\2\u0aa9\u0aa7\3\2\2\2\u0aa9\u0aaa\3"+
		"\2\2\2\u0aaa\u0aab\3\2\2\2\u0aab\u0aac\7U\2\2\u0aac\u0b6b\3\2\2\2\u0aad"+
		"\u0aae\7\"\2\2\u0aae\u0ab0\5\u00d4k\2\u0aaf\u0ab1\5\u0108\u0085\2\u0ab0"+
		"\u0aaf\3\2\2\2\u0ab1\u0ab2\3\2\2\2\u0ab2\u0ab0\3\2\2\2\u0ab2\u0ab3\3\2"+
		"\2\2\u0ab3\u0ab6\3\2\2\2\u0ab4\u0ab5\7T\2\2\u0ab5\u0ab7\5\u00d4k\2\u0ab6"+
		"\u0ab4\3\2\2\2\u0ab6\u0ab7\3\2\2\2\u0ab7\u0ab8\3\2\2\2\u0ab8\u0ab9\7U"+
		"\2\2\u0ab9\u0b6b\3\2\2\2\u0aba\u0abb\t%\2\2\u0abb\u0abc\7\7\2\2\u0abc"+
		"\u0abd\5\u00d4k\2\u0abd\u0abe\7\27\2\2\u0abe\u0abf\5\u00fa~\2\u0abf\u0ac0"+
		"\7\b\2\2\u0ac0\u0b6b\3\2\2\2\u0ac1\u0ac2\7\u00f0\2\2\u0ac2\u0acb\7\7\2"+
		"\2\u0ac3\u0ac8\5\u00c8e\2\u0ac4\u0ac5\7\t\2\2\u0ac5\u0ac7\5\u00c8e\2\u0ac6"+
		"\u0ac4\3\2\2\2\u0ac7\u0aca\3\2\2\2\u0ac8\u0ac6\3\2\2\2\u0ac8\u0ac9\3\2"+
		"\2\2\u0ac9\u0acc\3\2\2\2\u0aca\u0ac8\3\2\2\2\u0acb\u0ac3\3\2\2\2\u0acb"+
		"\u0acc\3\2\2\2\u0acc\u0acd\3\2\2\2\u0acd\u0b6b\7\b\2\2\u0ace\u0acf\7e"+
		"\2\2\u0acf\u0ad0\7\7\2\2\u0ad0\u0ad3\5\u00d4k\2\u0ad1\u0ad2\7v\2\2\u0ad2"+
		"\u0ad4\7\u00a5\2\2\u0ad3\u0ad1\3\2\2\2\u0ad3\u0ad4\3\2\2\2\u0ad4\u0ad5"+
		"\3\2\2\2\u0ad5\u0ad6\7\b\2\2\u0ad6\u0b6b\3\2\2\2\u0ad7\u0ad8\7\u0086\2"+
		"\2\u0ad8\u0ad9\7\7\2\2\u0ad9\u0adc\5\u00d4k\2\u0ada\u0adb\7v\2\2\u0adb"+
		"\u0add\7\u00a5\2\2\u0adc\u0ada\3\2\2\2\u0adc\u0add\3\2\2\2\u0add\u0ade"+
		"\3\2\2\2\u0ade\u0adf\7\b\2\2\u0adf\u0b6b\3\2\2\2\u0ae0\u0ae1\7\u00bc\2"+
		"\2\u0ae1\u0ae2\7\7\2\2\u0ae2\u0ae3\5\u00dco\2\u0ae3\u0ae4\7x\2\2\u0ae4"+
		"\u0ae5\5\u00dco\2\u0ae5\u0ae6\7\b\2\2\u0ae6\u0b6b\3\2\2\2\u0ae7\u0b6b"+
		"\5\u00e2r\2\u0ae8\u0b6b\7\u012e\2\2\u0ae9\u0aea\5\u0118\u008d\2\u0aea"+
		"\u0aeb\7\n\2\2\u0aeb\u0aec\7\u012e\2\2\u0aec\u0b6b\3\2\2\2\u0aed\u0aee"+
		"\7\7\2\2\u0aee\u0af1\5\u00c8e\2\u0aef\u0af0\7\t\2\2\u0af0\u0af2\5\u00c8"+
		"e\2\u0af1\u0aef\3\2\2\2\u0af2\u0af3\3\2\2\2\u0af3\u0af1\3\2\2\2\u0af3"+
		"\u0af4\3\2\2\2\u0af4\u0af5\3\2\2\2\u0af5\u0af6\7\b\2\2\u0af6\u0b6b\3\2"+
		"\2\2\u0af7\u0af8\7\7\2\2\u0af8\u0af9\5(\25\2\u0af9\u0afa\7\b\2\2\u0afa"+
		"\u0b6b\3\2\2\2\u0afb\u0afc\5\u0116\u008c\2\u0afc\u0b08\7\7\2\2\u0afd\u0aff"+
		"\5\u0098M\2\u0afe\u0afd\3\2\2\2\u0afe\u0aff\3\2\2\2\u0aff\u0b00\3\2\2"+
		"\2\u0b00\u0b05\5\u00d4k\2\u0b01\u0b02\7\t\2\2\u0b02\u0b04\5\u00d4k\2\u0b03"+
		"\u0b01\3\2\2\2\u0b04\u0b07\3\2\2\2\u0b05\u0b03\3\2\2\2\u0b05\u0b06\3\2"+
		"\2\2\u0b06\u0b09\3\2\2\2\u0b07\u0b05\3\2\2\2\u0b08\u0afe\3\2\2\2\u0b08"+
		"\u0b09\3\2\2\2\u0b09\u0b0a\3\2\2\2\u0b0a\u0b11\7\b\2\2\u0b0b\u0b0c\7c"+
		"\2\2\u0b0c\u0b0d\7\7\2\2\u0b0d\u0b0e\7\u011e\2\2\u0b0e\u0b0f\5\u00d8m"+
		"\2\u0b0f\u0b10\7\b\2\2\u0b10\u0b12\3\2\2\2\u0b11\u0b0b\3\2\2\2\u0b11\u0b12"+
		"\3\2\2\2\u0b12\u0b15\3\2\2\2\u0b13\u0b14\t&\2\2\u0b14\u0b16\7\u00a5\2"+
		"\2\u0b15\u0b13\3\2\2\2\u0b15\u0b16\3\2\2\2\u0b16\u0b19\3\2\2\2\u0b17\u0b18"+
		"\7\u00b0\2\2\u0b18\u0b1a\5\u010e\u0088\2\u0b19\u0b17\3\2\2\2\u0b19\u0b1a"+
		"\3\2\2\2\u0b1a\u0b6b\3\2\2\2\u0b1b\u0b1c\5\u011e\u0090\2\u0b1c\u0b1d\7"+
		"\u0137\2\2\u0b1d\u0b1e\5\u00d4k\2\u0b1e\u0b6b\3\2\2\2\u0b1f\u0b20\7\7"+
		"\2\2\u0b20\u0b23\5\u011e\u0090\2\u0b21\u0b22\7\t\2\2\u0b22\u0b24\5\u011e"+
		"\u0090\2\u0b23\u0b21\3\2\2\2\u0b24\u0b25\3\2\2\2\u0b25\u0b23\3\2\2\2\u0b25"+
		"\u0b26\3\2\2\2\u0b26\u0b27\3\2\2\2\u0b27\u0b28\7\b\2\2\u0b28\u0b29\7\u0137"+
		"\2\2\u0b29\u0b2a\5\u00d4k\2\u0b2a\u0b6b\3\2\2\2\u0b2b\u0b6b\5\u011e\u0090"+
		"\2\u0b2c\u0b2d\7\7\2\2\u0b2d\u0b2e\5\u00d4k\2\u0b2e\u0b2f\7\b\2\2\u0b2f"+
		"\u0b6b\3\2\2\2\u0b30\u0b31\7_\2\2\u0b31\u0b32\7\7\2\2\u0b32\u0b33\5\u011e"+
		"\u0090\2\u0b33\u0b34\7k\2\2\u0b34\u0b35\5\u00dco\2\u0b35\u0b36\7\b\2\2"+
		"\u0b36\u0b6b\3\2\2\2\u0b37\u0b38\t\'\2\2\u0b38\u0b39\7\7\2\2\u0b39\u0b3a"+
		"\5\u00dco\2\u0b3a\u0b3b\t(\2\2\u0b3b\u0b3e\5\u00dco\2\u0b3c\u0b3d\t)\2"+
		"\2\u0b3d\u0b3f\5\u00dco\2\u0b3e\u0b3c\3\2\2\2\u0b3e\u0b3f\3\2\2\2\u0b3f"+
		"\u0b40\3\2\2\2\u0b40\u0b41\7\b\2\2\u0b41\u0b6b\3\2\2\2\u0b42\u0b43\7\u0107"+
		"\2\2\u0b43\u0b45\7\7\2\2\u0b44\u0b46\t*\2\2\u0b45\u0b44\3\2\2\2\u0b45"+
		"\u0b46\3\2\2\2\u0b46\u0b48\3\2\2\2\u0b47\u0b49\5\u00dco\2\u0b48\u0b47"+
		"\3\2\2\2\u0b48\u0b49\3\2\2\2\u0b49\u0b4a\3\2\2\2\u0b4a\u0b4b\7k\2\2\u0b4b"+
		"\u0b4c\5\u00dco\2\u0b4c\u0b4d\7\b\2\2\u0b4d\u0b6b\3\2\2\2\u0b4e\u0b4f"+
		"\7\u00b2\2\2\u0b4f\u0b50\7\7\2\2\u0b50\u0b51\5\u00dco\2\u0b51\u0b52\7"+
		"\u00bb\2\2\u0b52\u0b53\5\u00dco\2\u0b53\u0b54\7k\2\2\u0b54\u0b57\5\u00dc"+
		"o\2\u0b55\u0b56\7g\2\2\u0b56\u0b58\5\u00dco\2\u0b57\u0b55\3\2\2\2\u0b57"+
		"\u0b58\3\2\2\2\u0b58\u0b59\3\2\2\2\u0b59\u0b5a\7\b\2\2\u0b5a\u0b6b\3\2"+
		"\2\2\u0b5b\u0b5c\t+\2\2\u0b5c\u0b5d\7\7\2\2\u0b5d\u0b5e\5\u00dco\2\u0b5e"+
		"\u0b5f\7\b\2\2\u0b5f\u0b60\7\u0121\2\2\u0b60\u0b61\7q\2\2\u0b61\u0b62"+
		"\7\7\2\2\u0b62\u0b63\7\u00ac\2\2\u0b63\u0b64\7\37\2\2\u0b64\u0b65\5`\61"+
		"\2\u0b65\u0b68\7\b\2\2\u0b66\u0b67\7\u00b0\2\2\u0b67\u0b69\5\u010e\u0088"+
		"\2\u0b68\u0b66\3\2\2\2\u0b68\u0b69\3\2\2\2\u0b69\u0b6b\3\2\2\2\u0b6a\u0a8d"+
		"\3\2\2\2\u0b6a\u0a8f\3\2\2\2\u0b6a\u0a98\3\2\2\2\u0b6a\u0aa1\3\2\2\2\u0b6a"+
		"\u0aad\3\2\2\2\u0b6a\u0aba\3\2\2\2\u0b6a\u0ac1\3\2\2\2\u0b6a\u0ace\3\2"+
		"\2\2\u0b6a\u0ad7\3\2\2\2\u0b6a\u0ae0\3\2\2\2\u0b6a\u0ae7\3\2\2\2\u0b6a"+
		"\u0ae8\3\2\2\2\u0b6a\u0ae9\3\2\2\2\u0b6a\u0aed\3\2\2\2\u0b6a\u0af7\3\2"+
		"\2\2\u0b6a\u0afb\3\2\2\2\u0b6a\u0b1b\3\2\2\2\u0b6a\u0b1f\3\2\2\2\u0b6a"+
		"\u0b2b\3\2\2\2\u0b6a\u0b2c\3\2\2\2\u0b6a\u0b30\3\2\2\2\u0b6a\u0b37\3\2"+
		"\2\2\u0b6a\u0b42\3\2\2\2\u0b6a\u0b4e\3\2\2\2\u0b6a\u0b5b\3\2\2\2\u0b6b"+
		"\u0b76\3\2\2\2\u0b6c\u0b6d\f\13\2\2\u0b6d\u0b6e\7\13\2\2\u0b6e\u0b6f\5"+
		"\u00dco\2\u0b6f\u0b70\7\f\2\2\u0b70\u0b75\3\2\2\2\u0b71\u0b72\f\t\2\2"+
		"\u0b72\u0b73\7\n\2\2\u0b73\u0b75\5\u011e\u0090\2\u0b74\u0b6c\3\2\2\2\u0b74"+
		"\u0b71\3\2\2\2\u0b75\u0b78\3\2\2\2\u0b76\u0b74\3\2\2\2\u0b76\u0b77\3\2"+
		"\2\2\u0b77\u00e1\3\2\2\2\u0b78\u0b76\3\2\2\2\u0b79\u0b86\7\u00a4\2\2\u0b7a"+
		"\u0b86\5\u00ecw\2\u0b7b\u0b7c\5\u011e\u0090\2\u0b7c\u0b7d\7\u013a\2\2"+
		"\u0b7d\u0b86\3\2\2\2\u0b7e\u0b86\5\u0124\u0093\2\u0b7f\u0b86\5\u00eav"+
		"\2\u0b80\u0b82\7\u013a\2\2\u0b81\u0b80\3\2\2\2\u0b82\u0b83\3\2\2\2\u0b83"+
		"\u0b81\3\2\2\2\u0b83\u0b84\3\2\2\2\u0b84\u0b86\3\2\2\2\u0b85\u0b79\3\2"+
		"\2\2\u0b85\u0b7a\3\2\2\2\u0b85\u0b7b\3\2\2\2\u0b85\u0b7e\3\2\2\2\u0b85"+
		"\u0b7f\3\2\2\2\u0b85\u0b81\3\2\2\2\u0b86\u00e3\3\2\2\2\u0b87\u0b88\t,"+
		"\2\2\u0b88\u00e5\3\2\2\2\u0b89\u0b8a\t-\2\2\u0b8a\u00e7\3\2\2\2\u0b8b"+
		"\u0b8c\t.\2\2\u0b8c\u00e9\3\2\2\2\u0b8d\u0b8e\t/\2\2\u0b8e\u00eb\3\2\2"+
		"\2\u0b8f\u0b92\7\u0080\2\2\u0b90\u0b93\5\u00eex\2\u0b91\u0b93\5\u00f2"+
		"z\2\u0b92\u0b90\3\2\2\2\u0b92\u0b91\3\2\2\2\u0b92\u0b93\3\2\2\2\u0b93"+
		"\u00ed\3\2\2\2\u0b94\u0b96\5\u00f0y\2\u0b95\u0b97\5\u00f4{\2\u0b96\u0b95"+
		"\3\2\2\2\u0b96\u0b97\3\2\2\2\u0b97\u00ef\3\2\2\2\u0b98\u0b99\5\u00f6|"+
		"\2\u0b99\u0b9a\5\u011e\u0090\2\u0b9a\u0b9c\3\2\2\2\u0b9b\u0b98\3\2\2\2"+
		"\u0b9c\u0b9d\3\2\2\2\u0b9d\u0b9b\3\2\2\2\u0b9d\u0b9e\3\2\2\2\u0b9e\u00f1"+
		"\3\2\2\2\u0b9f\u0ba2\5\u00f4{\2\u0ba0\u0ba3\5\u00f0y\2\u0ba1\u0ba3\5\u00f4"+
		"{\2\u0ba2\u0ba0\3\2\2\2\u0ba2\u0ba1\3\2\2\2\u0ba2\u0ba3\3\2\2\2\u0ba3"+
		"\u00f3\3\2\2\2\u0ba4\u0ba5\5\u00f6|\2\u0ba5\u0ba6\5\u011e\u0090\2\u0ba6"+
		"\u0ba7\7\u0101\2\2\u0ba7\u0ba8\5\u011e\u0090\2\u0ba8\u00f5\3\2\2\2\u0ba9"+
		"\u0bab\t\60\2\2\u0baa\u0ba9\3\2\2\2\u0baa\u0bab\3\2\2\2\u0bab\u0bac\3"+
		"\2\2\2\u0bac\u0bad\t\61\2\2\u0bad\u00f7\3\2\2\2\u0bae\u0bb2\7e\2\2\u0baf"+
		"\u0bb0\7\16\2\2\u0bb0\u0bb2\5\u011a\u008e\2\u0bb1\u0bae\3\2\2\2\u0bb1"+
		"\u0baf\3\2\2\2\u0bb2\u00f9\3\2\2\2\u0bb3\u0bb4\7\26\2\2\u0bb4\u0bb5\7"+
		"\u0128\2\2\u0bb5\u0bb6\5\u00fa~\2\u0bb6\u0bb7\7\u012a\2\2\u0bb7\u0be2"+
		"\3\2\2\2\u0bb8\u0bb9\7\u0097\2\2\u0bb9\u0bba\7\u0128\2\2\u0bba\u0bbb\5"+
		"\u00fa~\2\u0bbb\u0bbc\7\t\2\2\u0bbc\u0bbd\5\u00fa~\2\u0bbd\u0bbe\7\u012a"+
		"\2\2\u0bbe\u0be2\3\2\2\2\u0bbf\u0bc6\7\u00f0\2\2\u0bc0\u0bc2\7\u0128\2"+
		"\2\u0bc1\u0bc3\5\u0104\u0083\2\u0bc2\u0bc1\3\2\2\2\u0bc2\u0bc3\3\2\2\2"+
		"\u0bc3\u0bc4\3\2\2\2\u0bc4\u0bc7\7\u012a\2\2\u0bc5\u0bc7\7\u0126\2\2\u0bc6"+
		"\u0bc0\3\2\2\2\u0bc6\u0bc5\3\2\2\2\u0bc7\u0be2\3\2\2\2\u0bc8\u0bc9\7\u0080"+
		"\2\2\u0bc9\u0bcc\t\62\2\2\u0bca\u0bcb\7\u0101\2\2\u0bcb\u0bcd\7\u009d"+
		"\2\2\u0bcc\u0bca\3\2\2\2\u0bcc\u0bcd\3\2\2\2\u0bcd\u0be2\3\2\2\2\u0bce"+
		"\u0bcf\7\u0080\2\2\u0bcf\u0bd2\t\63\2\2\u0bd0\u0bd1\7\u0101\2\2\u0bd1"+
		"\u0bd3\t\64\2\2\u0bd2\u0bd0\3\2\2\2\u0bd2\u0bd3\3\2\2\2\u0bd3\u0be2\3"+
		"\2\2\2\u0bd4\u0bdf\5\u011e\u0090\2\u0bd5\u0bd6\7\7\2\2\u0bd6\u0bdb\7\u013e"+
		"\2\2\u0bd7\u0bd8\7\t\2\2\u0bd8\u0bda\7\u013e\2\2\u0bd9\u0bd7\3\2\2\2\u0bda"+
		"\u0bdd\3\2\2\2\u0bdb\u0bd9\3\2\2\2\u0bdb\u0bdc\3\2\2\2\u0bdc\u0bde\3\2"+
		"\2\2\u0bdd\u0bdb\3\2\2\2\u0bde\u0be0\7\b\2\2\u0bdf\u0bd5\3\2\2\2\u0bdf"+
		"\u0be0\3\2\2\2\u0be0\u0be2\3\2\2\2\u0be1\u0bb3\3\2\2\2\u0be1\u0bb8\3\2"+
		"\2\2\u0be1\u0bbf\3\2\2\2\u0be1\u0bc8\3\2\2\2\u0be1\u0bce\3\2\2\2\u0be1"+
		"\u0bd4\3\2\2\2\u0be2\u00fb\3\2\2\2\u0be3\u0be8\5\u00fe\u0080\2\u0be4\u0be5"+
		"\7\t\2\2\u0be5\u0be7\5\u00fe\u0080\2\u0be6\u0be4\3\2\2\2\u0be7\u0bea\3"+
		"\2\2\2\u0be8\u0be6\3\2\2\2\u0be8\u0be9\3\2\2\2\u0be9\u00fd\3\2\2\2\u0bea"+
		"\u0be8\3\2\2\2\u0beb\u0bec\5\u00be`\2\u0bec\u0bef\5\u00fa~\2\u0bed\u0bee"+
		"\7\u00a3\2\2\u0bee\u0bf0\7\u00a4\2\2\u0bef\u0bed\3\2\2\2\u0bef\u0bf0\3"+
		"\2\2\2\u0bf0\u0bf2\3\2\2\2\u0bf1\u0bf3\5&\24\2\u0bf2\u0bf1\3\2\2\2\u0bf2"+
		"\u0bf3\3\2\2\2\u0bf3\u0bf5\3\2\2\2\u0bf4\u0bf6\5\u00f8}\2\u0bf5\u0bf4"+
		"\3\2\2\2\u0bf5\u0bf6\3\2\2\2\u0bf6\u00ff\3\2\2\2\u0bf7\u0bfc\5\u0102\u0082"+
		"\2\u0bf8\u0bf9\7\t\2\2\u0bf9\u0bfb\5\u0102\u0082\2\u0bfa\u0bf8\3\2\2\2"+
		"\u0bfb\u0bfe\3\2\2\2\u0bfc\u0bfa\3\2\2\2\u0bfc\u0bfd\3\2\2\2\u0bfd\u0101"+
		"\3\2\2\2\u0bfe\u0bfc\3\2\2\2\u0bff\u0c00\5\u011a\u008e\2\u0c00\u0c03\5"+
		"\u00fa~\2\u0c01\u0c02\7\u00a3\2\2\u0c02\u0c04\7\u00a4\2\2\u0c03\u0c01"+
		"\3\2\2\2\u0c03\u0c04\3\2\2\2\u0c04\u0c06\3\2\2\2\u0c05\u0c07\5&\24\2\u0c06"+
		"\u0c05\3\2\2\2\u0c06\u0c07\3\2\2\2\u0c07\u0103\3\2\2\2\u0c08\u0c0d\5\u0106"+
		"\u0084\2\u0c09\u0c0a\7\t\2\2\u0c0a\u0c0c\5\u0106\u0084\2\u0c0b\u0c09\3"+
		"\2\2\2\u0c0c\u0c0f\3\2\2\2\u0c0d\u0c0b\3\2\2\2\u0c0d\u0c0e\3\2\2\2\u0c0e"+
		"\u0105\3\2\2\2\u0c0f\u0c0d\3\2\2\2\u0c10\u0c12\5\u011e\u0090\2\u0c11\u0c13"+
		"\7\u0136\2\2\u0c12\u0c11\3\2\2\2\u0c12\u0c13\3\2\2\2\u0c13\u0c14\3\2\2"+
		"\2\u0c14\u0c17\5\u00fa~\2\u0c15\u0c16\7\u00a3\2\2\u0c16\u0c18\7\u00a4"+
		"\2\2\u0c17\u0c15\3\2\2\2\u0c17\u0c18\3\2\2\2\u0c18\u0c1a\3\2\2\2\u0c19"+
		"\u0c1b\5&\24\2\u0c1a\u0c19\3\2\2\2\u0c1a\u0c1b\3\2\2\2\u0c1b\u0107\3\2"+
		"\2\2\u0c1c\u0c1d\7\u011d\2\2\u0c1d\u0c1e\5\u00d4k\2\u0c1e\u0c1f\7\u00fc"+
		"\2\2\u0c1f\u0c20\5\u00d4k\2\u0c20\u0109\3\2\2\2\u0c21\u0c22\7\u011f\2"+
		"\2\u0c22\u0c27\5\u010c\u0087\2\u0c23\u0c24\7\t\2\2\u0c24\u0c26\5\u010c"+
		"\u0087\2\u0c25\u0c23\3\2\2\2\u0c26\u0c29\3\2\2\2\u0c27\u0c25\3\2\2\2\u0c27"+
		"\u0c28\3\2\2\2\u0c28\u010b\3\2\2\2\u0c29\u0c27\3\2\2\2\u0c2a\u0c2b\5\u011a"+
		"\u008e\2\u0c2b\u0c2c\7\27\2\2\u0c2c\u0c2d\5\u010e\u0088\2\u0c2d\u010d"+
		"\3\2\2\2\u0c2e\u0c5d\5\u011a\u008e\2\u0c2f\u0c30\7\7\2\2\u0c30\u0c31\5"+
		"\u011a\u008e\2\u0c31\u0c32\7\b\2\2\u0c32\u0c5d\3\2\2\2\u0c33\u0c56\7\7"+
		"\2\2\u0c34\u0c35\7)\2\2\u0c35\u0c36\7\37\2\2\u0c36\u0c3b\5\u00d4k\2\u0c37"+
		"\u0c38\7\t\2\2\u0c38\u0c3a\5\u00d4k\2\u0c39\u0c37\3\2\2\2\u0c3a\u0c3d"+
		"\3\2\2\2\u0c3b\u0c39\3\2\2\2\u0c3b\u0c3c\3\2\2\2\u0c3c\u0c57\3\2\2\2\u0c3d"+
		"\u0c3b\3\2\2\2\u0c3e\u0c3f\t\65\2\2\u0c3f\u0c40\7\37\2\2\u0c40\u0c45\5"+
		"\u00d4k\2\u0c41\u0c42\7\t\2\2\u0c42\u0c44\5\u00d4k\2\u0c43\u0c41\3\2\2"+
		"\2\u0c44\u0c47\3\2\2\2\u0c45\u0c43\3\2\2\2\u0c45\u0c46\3\2\2\2\u0c46\u0c49"+
		"\3\2\2\2\u0c47\u0c45\3\2\2\2\u0c48\u0c3e\3\2\2\2\u0c48\u0c49\3\2\2\2\u0c49"+
		"\u0c54\3\2\2\2\u0c4a\u0c4b\t\66\2\2\u0c4b\u0c4c\7\37\2\2\u0c4c\u0c51\5"+
		"`\61\2\u0c4d\u0c4e\7\t\2\2\u0c4e\u0c50\5`\61\2\u0c4f\u0c4d\3\2\2\2\u0c50"+
		"\u0c53\3\2\2\2\u0c51\u0c4f\3\2\2\2\u0c51\u0c52\3\2\2\2\u0c52\u0c55\3\2"+
		"\2\2\u0c53\u0c51\3\2\2\2\u0c54\u0c4a\3\2\2\2\u0c54\u0c55\3\2\2\2\u0c55"+
		"\u0c57\3\2\2\2\u0c56\u0c34\3\2\2\2\u0c56\u0c48\3\2\2\2\u0c57\u0c59\3\2"+
		"\2\2\u0c58\u0c5a\5\u0110\u0089\2\u0c59\u0c58\3\2\2\2\u0c59\u0c5a\3\2\2"+
		"\2\u0c5a\u0c5b\3\2\2\2\u0c5b\u0c5d\7\b\2\2\u0c5c\u0c2e\3\2\2\2\u0c5c\u0c2f"+
		"\3\2\2\2\u0c5c\u0c33\3\2\2\2\u0c5d\u010f\3\2\2\2\u0c5e\u0c5f\7\u00c4\2"+
		"\2\u0c5f\u0c6f\5\u0112\u008a\2\u0c60\u0c61\7\u00da\2\2\u0c61\u0c6f\5\u0112"+
		"\u008a\2\u0c62\u0c63\7\u00c4\2\2\u0c63\u0c64\7\33\2\2\u0c64\u0c65\5\u0112"+
		"\u008a\2\u0c65\u0c66\7\22\2\2\u0c66\u0c67\5\u0112\u008a\2\u0c67\u0c6f"+
		"\3\2\2\2\u0c68\u0c69\7\u00da\2\2\u0c69\u0c6a\7\33\2\2\u0c6a\u0c6b\5\u0112"+
		"\u008a\2\u0c6b\u0c6c\7\22\2\2\u0c6c\u0c6d\5\u0112\u008a\2\u0c6d\u0c6f"+
		"\3\2\2\2\u0c6e\u0c5e\3\2\2\2\u0c6e\u0c60\3\2\2\2\u0c6e\u0c62\3\2\2\2\u0c6e"+
		"\u0c68\3\2\2\2\u0c6f\u0111\3\2\2\2\u0c70\u0c71\7\u010d\2\2\u0c71\u0c78"+
		"\t\67\2\2\u0c72\u0c73\7;\2\2\u0c73\u0c78\7\u00d9\2\2\u0c74\u0c75\5\u00d4"+
		"k\2\u0c75\u0c76\t\67\2\2\u0c76\u0c78\3\2\2\2\u0c77\u0c70\3\2\2\2\u0c77"+
		"\u0c72\3\2\2\2\u0c77\u0c74\3\2\2\2\u0c78\u0113\3\2\2\2\u0c79\u0c7e\5\u0118"+
		"\u008d\2\u0c7a\u0c7b\7\t\2\2\u0c7b\u0c7d\5\u0118\u008d\2\u0c7c\u0c7a\3"+
		"\2\2\2\u0c7d\u0c80\3\2\2\2\u0c7e\u0c7c\3\2\2\2\u0c7e\u0c7f\3\2\2\2\u0c7f"+
		"\u0115\3\2\2\2\u0c80\u0c7e\3\2\2\2\u0c81\u0c86\5\u0118\u008d\2\u0c82\u0c86"+
		"\7c\2\2\u0c83\u0c86\7\u008a\2\2\u0c84\u0c86\7\u00d3\2\2\u0c85\u0c81\3"+
		"\2\2\2\u0c85\u0c82\3\2\2\2\u0c85\u0c83\3\2\2\2\u0c85\u0c84\3\2\2\2\u0c86"+
		"\u0117\3\2\2\2\u0c87\u0c8c\5\u011e\u0090\2\u0c88\u0c89\7\n\2\2\u0c89\u0c8b"+
		"\5\u011e\u0090\2\u0c8a\u0c88\3\2\2\2\u0c8b\u0c8e\3\2\2\2\u0c8c\u0c8a\3"+
		"\2\2\2\u0c8c\u0c8d\3\2\2\2\u0c8d\u0119\3\2\2\2\u0c8e\u0c8c\3\2\2\2\u0c8f"+
		"\u0c90\5\u011e\u0090\2\u0c90\u0c91\5\u011c\u008f\2\u0c91\u011b\3\2\2\2"+
		"\u0c92\u0c93\7\u012d\2\2\u0c93\u0c95\5\u011e\u0090\2\u0c94\u0c92\3\2\2"+
		"\2\u0c95\u0c96\3\2\2\2\u0c96\u0c94\3\2\2\2\u0c96\u0c97\3\2\2\2\u0c97\u0c9a"+
		"\3\2\2\2\u0c98\u0c9a\3\2\2\2\u0c99\u0c94\3\2\2\2\u0c99\u0c98\3\2\2\2\u0c9a"+
		"\u011d\3\2\2\2\u0c9b\u0c9f\5\u0120\u0091\2\u0c9c\u0c9d\6\u0090\22\2\u0c9d"+
		"\u0c9f\5\u012a\u0096\2\u0c9e\u0c9b\3\2\2\2\u0c9e\u0c9c\3\2\2\2\u0c9f\u011f"+
		"\3\2\2\2\u0ca0\u0ca7\7\u0144\2\2\u0ca1\u0ca7\5\u0122\u0092\2\u0ca2\u0ca3"+
		"\6\u0091\23\2\u0ca3\u0ca7\5\u0128\u0095\2\u0ca4\u0ca5\6\u0091\24\2\u0ca5"+
		"\u0ca7\5\u012c\u0097\2\u0ca6\u0ca0\3\2\2\2\u0ca6\u0ca1\3\2\2\2\u0ca6\u0ca2"+
		"\3\2\2\2\u0ca6\u0ca4\3\2\2\2\u0ca7\u0121\3\2\2\2\u0ca8\u0ca9\7\u0145\2"+
		"\2\u0ca9\u0123\3\2\2\2\u0caa\u0cac\6\u0093\25\2\u0cab\u0cad\7\u012d\2"+
		"\2\u0cac\u0cab\3\2\2\2\u0cac\u0cad\3\2\2\2\u0cad\u0cae\3\2\2\2\u0cae\u0cd6"+
		"\7\u013f\2\2\u0caf\u0cb1\6\u0093\26\2\u0cb0\u0cb2\7\u012d\2\2\u0cb1\u0cb0"+
		"\3\2\2\2\u0cb1\u0cb2\3\2\2\2\u0cb2\u0cb3\3\2\2\2\u0cb3\u0cd6\7\u0140\2"+
		"\2\u0cb4\u0cb6\6\u0093\27\2\u0cb5\u0cb7\7\u012d\2\2\u0cb6\u0cb5\3\2\2"+
		"\2\u0cb6\u0cb7\3\2\2\2\u0cb7\u0cb8\3\2\2\2\u0cb8\u0cd6\t8\2\2\u0cb9\u0cbb"+
		"\7\u012d\2\2\u0cba\u0cb9\3\2\2\2\u0cba\u0cbb\3\2\2\2\u0cbb\u0cbc\3\2\2"+
		"\2\u0cbc\u0cd6\7\u013e\2\2\u0cbd\u0cbf\7\u012d\2\2\u0cbe\u0cbd\3\2\2\2"+
		"\u0cbe\u0cbf\3\2\2\2\u0cbf\u0cc0\3\2\2\2\u0cc0\u0cd6\7\u013b\2\2\u0cc1"+
		"\u0cc3\7\u012d\2\2\u0cc2\u0cc1\3\2\2\2\u0cc2\u0cc3\3\2\2\2\u0cc3\u0cc4"+
		"\3\2\2\2\u0cc4\u0cd6\7\u013c\2\2\u0cc5\u0cc7\7\u012d\2\2\u0cc6\u0cc5\3"+
		"\2\2\2\u0cc6\u0cc7\3\2\2\2\u0cc7\u0cc8\3\2\2\2\u0cc8\u0cd6\7\u013d\2\2"+
		"\u0cc9\u0ccb\7\u012d\2\2\u0cca\u0cc9\3\2\2\2\u0cca\u0ccb\3\2\2\2\u0ccb"+
		"\u0ccc\3\2\2\2\u0ccc\u0cd6\7\u0142\2\2\u0ccd\u0ccf\7\u012d\2\2\u0cce\u0ccd"+
		"\3\2\2\2\u0cce\u0ccf\3\2\2\2\u0ccf\u0cd0\3\2\2\2\u0cd0\u0cd6\7\u0141\2"+
		"\2\u0cd1\u0cd3\7\u012d\2\2\u0cd2\u0cd1\3\2\2\2\u0cd2\u0cd3\3\2\2\2\u0cd3"+
		"\u0cd4\3\2\2\2\u0cd4\u0cd6\7\u0143\2\2\u0cd5\u0caa\3\2\2\2\u0cd5\u0caf"+
		"\3\2\2\2\u0cd5\u0cb4\3\2\2\2\u0cd5\u0cba\3\2\2\2\u0cd5\u0cbe\3\2\2\2\u0cd5"+
		"\u0cc2\3\2\2\2\u0cd5\u0cc6\3\2\2\2\u0cd5\u0cca\3\2\2\2\u0cd5\u0cce\3\2"+
		"\2\2\u0cd5\u0cd2\3\2\2\2\u0cd6\u0125\3\2\2\2\u0cd7\u0cd8\7\u010b\2\2\u0cd8"+
		"\u0cdf\5\u00fa~\2\u0cd9\u0cdf\5&\24\2\u0cda\u0cdf\5\u00f8}\2\u0cdb\u0cdc"+
		"\t9\2\2\u0cdc\u0cdd\7\u00a3\2\2\u0cdd\u0cdf\7\u00a4\2\2\u0cde\u0cd7\3"+
		"\2\2\2\u0cde\u0cd9\3\2\2\2\u0cde\u0cda\3\2\2\2\u0cde\u0cdb\3\2\2\2\u0cdf"+
		"\u0127\3\2\2\2\u0ce0\u0ce1\t:\2\2\u0ce1\u0129\3\2\2\2\u0ce2\u0ce3\t;\2"+
		"\2\u0ce3\u012b\3\2\2\2\u0ce4\u0ce5\t<\2\2\u0ce5\u012d\3\2\2\2\u01b4\u013a"+
		"\u0153\u0160\u0167\u016f\u0171\u0185\u0189\u018f\u0192\u0195\u019c\u019f"+
		"\u01a3\u01a6\u01ad\u01b8\u01ba\u01c2\u01c5\u01c9\u01cc\u01d2\u01dd\u01e3"+
		"\u01e8\u020a\u0217\u0230\u0239\u023d\u0243\u0247\u024c\u0252\u025e\u0266"+
		"\u026c\u0279\u027e\u028e\u0295\u0299\u029f\u02ae\u02b2\u02b8\u02be\u02c1"+
		"\u02c4\u02ca\u02ce\u02d6\u02d8\u02e1\u02e4\u02ed\u02f2\u02f8\u02ff\u0302"+
		"\u0308\u0313\u0316\u031a\u031f\u0324\u032b\u032e\u0331\u0338\u033d\u0346"+
		"\u034e\u0354\u0357\u035a\u0360\u0364\u0369\u036c\u0370\u0372\u037a\u0382"+
		"\u0385\u038a\u0390\u0396\u0399\u039d\u03a0\u03a4\u03c0\u03c3\u03c7\u03cd"+
		"\u03d0\u03d3\u03d9\u03e1\u03e6\u03ec\u03f2\u03fa\u0401\u0409\u041a\u0428"+
		"\u042b\u0431\u043a\u0443\u044b\u0450\u0455\u045c\u0462\u0467\u046f\u0472"+
		"\u047e\u0482\u0489\u04fd\u0505\u050d\u0516\u0520\u0524\u0527\u052d\u0533"+
		"\u053f\u054b\u0550\u0559\u0561\u0568\u056a\u056d\u0572\u0576\u057b\u057e"+
		"\u0583\u0588\u058b\u0590\u0594\u0599\u059b\u059f\u05a8\u05b0\u05bb\u05c2"+
		"\u05cb\u05d0\u05d3\u05e9\u05eb\u05f4\u05fb\u05fe\u0605\u0609\u060f\u0617"+
		"\u0622\u062d\u0634\u063a\u0646\u064d\u0654\u0660\u0668\u066e\u0671\u067a"+
		"\u067d\u0686\u0689\u0692\u0695\u069e\u06a1\u06a4\u06a9\u06ab\u06b7\u06be"+
		"\u06c5\u06c8\u06ca\u06d6\u06da\u06de\u06e4\u06e8\u06f0\u06f4\u06f7\u06fa"+
		"\u06fd\u0701\u0705\u070a\u070e\u0711\u0714\u0717\u071b\u0720\u0724\u0727"+
		"\u072a\u072d\u072f\u0735\u073c\u0741\u0744\u0747\u074b\u0755\u0759\u075b"+
		"\u075e\u0762\u0768\u076c\u0777\u0781\u078d\u079c\u07a1\u07a8\u07b8\u07bd"+
		"\u07ca\u07cf\u07d7\u07dd\u07e1\u07e4\u07eb\u07f1\u07fa\u0804\u0813\u0818"+
		"\u081a\u081e\u0827\u0834\u0839\u083d\u0845\u0848\u084c\u085a\u0867\u086c"+
		"\u0870\u0873\u0878\u0881\u0884\u0889\u0890\u0893\u0898\u089e\u08a4\u08a8"+
		"\u08ae\u08b2\u08b5\u08ba\u08bd\u08c2\u08c6\u08c9\u08cc\u08d2\u08d7\u08de"+
		"\u08e1\u08f3\u08f5\u08f8\u0903\u090c\u0913\u091b\u0922\u0926\u0929\u0931"+
		"\u0939\u093f\u0947\u0953\u0956\u095c\u0960\u0962\u096b\u0977\u0979\u0980"+
		"\u0987\u098d\u0993\u0995\u099c\u09a4\u09ac\u09b2\u09b7\u09be\u09c4\u09c8"+
		"\u09ca\u09d1\u09da\u09e1\u09eb\u09f0\u09f4\u09fd\u0a0a\u0a0c\u0a14\u0a16"+
		"\u0a1a\u0a22\u0a2b\u0a31\u0a39\u0a3e\u0a4a\u0a4f\u0a52\u0a58\u0a5c\u0a61"+
		"\u0a66\u0a6b\u0a71\u0a86\u0a88\u0aa5\u0aa9\u0ab2\u0ab6\u0ac8\u0acb\u0ad3"+
		"\u0adc\u0af3\u0afe\u0b05\u0b08\u0b11\u0b15\u0b19\u0b25\u0b3e\u0b45\u0b48"+
		"\u0b57\u0b68\u0b6a\u0b74\u0b76\u0b83\u0b85\u0b92\u0b96\u0b9d\u0ba2\u0baa"+
		"\u0bb1\u0bc2\u0bc6\u0bcc\u0bd2\u0bdb\u0bdf\u0be1\u0be8\u0bef\u0bf2\u0bf5"+
		"\u0bfc\u0c03\u0c06\u0c0d\u0c12\u0c17\u0c1a\u0c27\u0c3b\u0c45\u0c48\u0c51"+
		"\u0c54\u0c56\u0c59\u0c5c\u0c6e\u0c77\u0c7e\u0c85\u0c8c\u0c96\u0c99\u0c9e"+
		"\u0ca6\u0cac\u0cb1\u0cb6\u0cba\u0cbe\u0cc2\u0cc6\u0cca\u0cce\u0cd2\u0cd5"+
		"\u0cde";
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