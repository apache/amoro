// Generated from /Users/jinsilei/arctic/arctic/spark/v3.3/spark/src/main/antlr4/com/netease/arctic/spark/sql/parser/SqlBaseParser.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SqlBaseParserParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEMICOLON=1, LEFT_PAREN=2, RIGHT_PAREN=3, COMMA=4, DOT=5, LEFT_BRACKET=6, 
		RIGHT_BRACKET=7, ADD=8, AFTER=9, ALL=10, ALTER=11, ANALYZE=12, AND=13, 
		ANTI=14, ANY=15, ARCHIVE=16, ARRAY=17, AS=18, ASC=19, AT=20, AUTHORIZATION=21, 
		BETWEEN=22, BOTH=23, BUCKET=24, BUCKETS=25, BY=26, CACHE=27, CASCADE=28, 
		CASE=29, CAST=30, CATALOG=31, CATALOGS=32, CHANGE=33, CHECK=34, CLEAR=35, 
		CLUSTER=36, CLUSTERED=37, CODEGEN=38, COLLATE=39, COLLECTION=40, COLUMN=41, 
		COLUMNS=42, COMMENT=43, COMMIT=44, COMPACT=45, COMPACTIONS=46, COMPUTE=47, 
		CONCATENATE=48, CONSTRAINT=49, COST=50, CREATE=51, CROSS=52, CUBE=53, 
		CURRENT=54, CURRENT_DATE=55, CURRENT_TIME=56, CURRENT_TIMESTAMP=57, CURRENT_USER=58, 
		DAY=59, DAYOFYEAR=60, DATA=61, DATABASE=62, DATABASES=63, DATEADD=64, 
		DATEDIFF=65, DBPROPERTIES=66, DEFINED=67, DELETE=68, DELIMITED=69, DESC=70, 
		DESCRIBE=71, DFS=72, DIRECTORIES=73, DIRECTORY=74, DISTINCT=75, DISTRIBUTE=76, 
		DIV=77, DROP=78, ELSE=79, END=80, ESCAPE=81, ESCAPED=82, EXCEPT=83, EXCHANGE=84, 
		EXISTS=85, EXPLAIN=86, EXPORT=87, EXTENDED=88, EXTERNAL=89, EXTRACT=90, 
		FALSE=91, FETCH=92, FIELDS=93, FILTER=94, FILEFORMAT=95, FIRST=96, FOLLOWING=97, 
		FOR=98, FOREIGN=99, FORMAT=100, FORMATTED=101, FROM=102, FULL=103, FUNCTION=104, 
		FUNCTIONS=105, GLOBAL=106, GRANT=107, GROUP=108, GROUPING=109, HAVING=110, 
		HOUR=111, IF=112, IGNORE=113, IMPORT=114, IN=115, INDEX=116, INDEXES=117, 
		INNER=118, INPATH=119, INPUTFORMAT=120, INSERT=121, INTERSECT=122, INTERVAL=123, 
		INTO=124, IS=125, ITEMS=126, JOIN=127, KEYS=128, LAST=129, LATERAL=130, 
		LAZY=131, LEADING=132, LEFT=133, LIKE=134, ILIKE=135, LIMIT=136, LINES=137, 
		LIST=138, LOAD=139, LOCAL=140, LOCATION=141, LOCK=142, LOCKS=143, LOGICAL=144, 
		MACRO=145, MAP=146, MATCHED=147, MERGE=148, MICROSECOND=149, MILLISECOND=150, 
		MINUTE=151, MONTH=152, MSCK=153, NAMESPACE=154, NAMESPACES=155, NATURAL=156, 
		NO=157, NOT=158, NULL=159, NULLS=160, OF=161, ON=162, ONLY=163, OPTION=164, 
		OPTIONS=165, OR=166, ORDER=167, OUT=168, OUTER=169, OUTPUTFORMAT=170, 
		OVER=171, OVERLAPS=172, OVERLAY=173, OVERWRITE=174, PARTITION=175, PARTITIONED=176, 
		PARTITIONS=177, PERCENTILE_CONT=178, PERCENTILE_DISC=179, PERCENTLIT=180, 
		PIVOT=181, PLACING=182, POSITION=183, PRECEDING=184, PRIMARY=185, PRINCIPALS=186, 
		PROPERTIES=187, PURGE=188, QUARTER=189, QUERY=190, RANGE=191, RECORDREADER=192, 
		RECORDWRITER=193, RECOVER=194, REDUCE=195, REFERENCES=196, REFRESH=197, 
		RENAME=198, REPAIR=199, REPEATABLE=200, REPLACE=201, RESET=202, RESPECT=203, 
		RESTRICT=204, REVOKE=205, RIGHT=206, RLIKE=207, ROLE=208, ROLES=209, ROLLBACK=210, 
		ROLLUP=211, ROW=212, ROWS=213, SECOND=214, SCHEMA=215, SCHEMAS=216, SELECT=217, 
		SEMI=218, SEPARATED=219, SERDE=220, SERDEPROPERTIES=221, SESSION_USER=222, 
		SET=223, SETMINUS=224, SETS=225, SHOW=226, SKEWED=227, SOME=228, SORT=229, 
		SORTED=230, START=231, STATISTICS=232, STORED=233, STRATIFY=234, STRUCT=235, 
		SUBSTR=236, SUBSTRING=237, SYNC=238, SYSTEM_TIME=239, SYSTEM_VERSION=240, 
		TABLE=241, TABLES=242, TABLESAMPLE=243, TBLPROPERTIES=244, TEMPORARY=245, 
		TERMINATED=246, THEN=247, TIME=248, TIMESTAMP=249, TIMESTAMPADD=250, TIMESTAMPDIFF=251, 
		TO=252, TOUCH=253, TRAILING=254, TRANSACTION=255, TRANSACTIONS=256, TRANSFORM=257, 
		TRIM=258, TRUE=259, TRUNCATE=260, TRY_CAST=261, TYPE=262, UNARCHIVE=263, 
		UNBOUNDED=264, UNCACHE=265, UNION=266, UNIQUE=267, UNKNOWN=268, UNLOCK=269, 
		UNSET=270, UPDATE=271, USE=272, USER=273, USING=274, VALUES=275, VERSION=276, 
		VIEW=277, VIEWS=278, WEEK=279, WHEN=280, WHERE=281, WINDOW=282, WITH=283, 
		WITHIN=284, YEAR=285, ZONE=286, EQ=287, NSEQ=288, NEQ=289, NEQJ=290, LT=291, 
		LTE=292, GT=293, GTE=294, PLUS=295, MINUS=296, ASTERISK=297, SLASH=298, 
		PERCENT=299, TILDE=300, AMPERSAND=301, PIPE=302, CONCAT_PIPE=303, HAT=304, 
		COLON=305, ARROW=306, HENT_START=307, HENT_END=308, STRING=309, BIGINT_LITERAL=310, 
		SMALLINT_LITERAL=311, TINYINT_LITERAL=312, INTEGER_VALUE=313, EXPONENT_VALUE=314, 
		DECIMAL_VALUE=315, FLOAT_LITERAL=316, DOUBLE_LITERAL=317, BIGDECIMAL_LITERAL=318, 
		IDENTIFIER=319, BACKQUOTED_IDENTIFIER=320, SIMPLE_COMMENT=321, BRACKETED_COMMENT=322, 
		WS=323, UNRECOGNIZED=324;
	public static final int
		RULE_singleStatement = 0, RULE_singleExpression = 1, RULE_singleTableIdentifier = 2, 
		RULE_singleMultipartIdentifier = 3, RULE_singleFunctionIdentifier = 4, 
		RULE_singleDataType = 5, RULE_singleTableSchema = 6, RULE_statement = 7, 
		RULE_configKey = 8, RULE_configValue = 9, RULE_unsupportedHiveNativeCommands = 10, 
		RULE_createTableHeader = 11, RULE_replaceTableHeader = 12, RULE_bucketSpec = 13, 
		RULE_skewSpec = 14, RULE_locationSpec = 15, RULE_commentSpec = 16, RULE_query = 17, 
		RULE_insertInto = 18, RULE_partitionSpecLocation = 19, RULE_partitionSpec = 20, 
		RULE_partitionVal = 21, RULE_namespace = 22, RULE_namespaces = 23, RULE_describeFuncName = 24, 
		RULE_describeColName = 25, RULE_ctes = 26, RULE_namedQuery = 27, RULE_tableProvider = 28, 
		RULE_createTableClauses = 29, RULE_propertyList = 30, RULE_property = 31, 
		RULE_propertyKey = 32, RULE_propertyValue = 33, RULE_constantList = 34, 
		RULE_nestedConstantList = 35, RULE_createFileFormat = 36, RULE_fileFormat = 37, 
		RULE_storageHandler = 38, RULE_resource = 39, RULE_dmlStatementNoWith = 40, 
		RULE_queryOrganization = 41, RULE_multiInsertQueryBody = 42, RULE_queryTerm = 43, 
		RULE_queryPrimary = 44, RULE_sortItem = 45, RULE_fromStatement = 46, RULE_fromStatementBody = 47, 
		RULE_querySpecification = 48, RULE_transformClause = 49, RULE_selectClause = 50, 
		RULE_setClause = 51, RULE_matchedClause = 52, RULE_notMatchedClause = 53, 
		RULE_matchedAction = 54, RULE_notMatchedAction = 55, RULE_assignmentList = 56, 
		RULE_assignment = 57, RULE_whereClause = 58, RULE_havingClause = 59, RULE_hint = 60, 
		RULE_hintStatement = 61, RULE_fromClause = 62, RULE_temporalClause = 63, 
		RULE_aggregationClause = 64, RULE_groupByClause = 65, RULE_groupingAnalytics = 66, 
		RULE_groupingElement = 67, RULE_groupingSet = 68, RULE_pivotClause = 69, 
		RULE_pivotColumn = 70, RULE_pivotValue = 71, RULE_lateralView = 72, RULE_setQuantifier = 73, 
		RULE_relation = 74, RULE_joinRelation = 75, RULE_joinType = 76, RULE_joinCriteria = 77, 
		RULE_sample = 78, RULE_sampleMethod = 79, RULE_identifierList = 80, RULE_identifierSeq = 81, 
		RULE_orderedIdentifierList = 82, RULE_orderedIdentifier = 83, RULE_identifierCommentList = 84, 
		RULE_identifierComment = 85, RULE_relationPrimary = 86, RULE_inlineTable = 87, 
		RULE_functionTable = 88, RULE_tableAlias = 89, RULE_rowFormat = 90, RULE_multipartIdentifierList = 91, 
		RULE_multipartIdentifier = 92, RULE_multipartIdentifierPropertyList = 93, 
		RULE_multipartIdentifierProperty = 94, RULE_tableIdentifier = 95, RULE_functionIdentifier = 96, 
		RULE_namedExpression = 97, RULE_namedExpressionSeq = 98, RULE_partitionFieldList = 99, 
		RULE_partitionField = 100, RULE_transform = 101, RULE_transformArgument = 102, 
		RULE_expression = 103, RULE_expressionSeq = 104, RULE_booleanExpression = 105, 
		RULE_predicate = 106, RULE_valueExpression = 107, RULE_datetimeUnit = 108, 
		RULE_primaryExpression = 109, RULE_constant = 110, RULE_comparisonOperator = 111, 
		RULE_arithmeticOperator = 112, RULE_predicateOperator = 113, RULE_booleanValue = 114, 
		RULE_interval = 115, RULE_errorCapturingMultiUnitsInterval = 116, RULE_multiUnitsInterval = 117, 
		RULE_errorCapturingUnitToUnitInterval = 118, RULE_unitToUnitInterval = 119, 
		RULE_intervalValue = 120, RULE_colPosition = 121, RULE_dataType = 122, 
		RULE_qualifiedColTypeWithPositionList = 123, RULE_qualifiedColTypeWithPosition = 124, 
		RULE_colTypeList = 125, RULE_colType = 126, RULE_complexColTypeList = 127, 
		RULE_complexColType = 128, RULE_whenClause = 129, RULE_windowClause = 130, 
		RULE_namedWindow = 131, RULE_windowSpec = 132, RULE_windowFrame = 133, 
		RULE_frameBound = 134, RULE_qualifiedNameList = 135, RULE_functionName = 136, 
		RULE_qualifiedName = 137, RULE_errorCapturingIdentifier = 138, RULE_errorCapturingIdentifierExtra = 139, 
		RULE_identifier = 140, RULE_strictIdentifier = 141, RULE_quotedIdentifier = 142, 
		RULE_number = 143, RULE_alterColumnAction = 144, RULE_ansiNonReserved = 145, 
		RULE_strictNonReserved = 146, RULE_nonReserved = 147;
	private static String[] makeRuleNames() {
		return new String[] {
			"singleStatement", "singleExpression", "singleTableIdentifier", "singleMultipartIdentifier", 
			"singleFunctionIdentifier", "singleDataType", "singleTableSchema", "statement", 
			"configKey", "configValue", "unsupportedHiveNativeCommands", "createTableHeader", 
			"replaceTableHeader", "bucketSpec", "skewSpec", "locationSpec", "commentSpec", 
			"query", "insertInto", "partitionSpecLocation", "partitionSpec", "partitionVal", 
			"namespace", "namespaces", "describeFuncName", "describeColName", "ctes", 
			"namedQuery", "tableProvider", "createTableClauses", "propertyList", 
			"property", "propertyKey", "propertyValue", "constantList", "nestedConstantList", 
			"createFileFormat", "fileFormat", "storageHandler", "resource", "dmlStatementNoWith", 
			"queryOrganization", "multiInsertQueryBody", "queryTerm", "queryPrimary", 
			"sortItem", "fromStatement", "fromStatementBody", "querySpecification", 
			"transformClause", "selectClause", "setClause", "matchedClause", "notMatchedClause", 
			"matchedAction", "notMatchedAction", "assignmentList", "assignment", 
			"whereClause", "havingClause", "hint", "hintStatement", "fromClause", 
			"temporalClause", "aggregationClause", "groupByClause", "groupingAnalytics", 
			"groupingElement", "groupingSet", "pivotClause", "pivotColumn", "pivotValue", 
			"lateralView", "setQuantifier", "relation", "joinRelation", "joinType", 
			"joinCriteria", "sample", "sampleMethod", "identifierList", "identifierSeq", 
			"orderedIdentifierList", "orderedIdentifier", "identifierCommentList", 
			"identifierComment", "relationPrimary", "inlineTable", "functionTable", 
			"tableAlias", "rowFormat", "multipartIdentifierList", "multipartIdentifier", 
			"multipartIdentifierPropertyList", "multipartIdentifierProperty", "tableIdentifier", 
			"functionIdentifier", "namedExpression", "namedExpressionSeq", "partitionFieldList", 
			"partitionField", "transform", "transformArgument", "expression", "expressionSeq", 
			"booleanExpression", "predicate", "valueExpression", "datetimeUnit", 
			"primaryExpression", "constant", "comparisonOperator", "arithmeticOperator", 
			"predicateOperator", "booleanValue", "interval", "errorCapturingMultiUnitsInterval", 
			"multiUnitsInterval", "errorCapturingUnitToUnitInterval", "unitToUnitInterval", 
			"intervalValue", "colPosition", "dataType", "qualifiedColTypeWithPositionList", 
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
			null, "';'", "'('", "')'", "','", "'.'", "'['", "']'", "'ADD'", "'AFTER'", 
			"'ALL'", "'ALTER'", "'ANALYZE'", "'AND'", "'ANTI'", "'ANY'", "'ARCHIVE'", 
			"'ARRAY'", "'AS'", "'ASC'", "'AT'", "'AUTHORIZATION'", "'BETWEEN'", "'BOTH'", 
			"'BUCKET'", "'BUCKETS'", "'BY'", "'CACHE'", "'CASCADE'", "'CASE'", "'CAST'", 
			"'CATALOG'", "'CATALOGS'", "'CHANGE'", "'CHECK'", "'CLEAR'", "'CLUSTER'", 
			"'CLUSTERED'", "'CODEGEN'", "'COLLATE'", "'COLLECTION'", "'COLUMN'", 
			"'COLUMNS'", "'COMMENT'", "'COMMIT'", "'COMPACT'", "'COMPACTIONS'", "'COMPUTE'", 
			"'CONCATENATE'", "'CONSTRAINT'", "'COST'", "'CREATE'", "'CROSS'", "'CUBE'", 
			"'CURRENT'", "'CURRENT_DATE'", "'CURRENT_TIME'", "'CURRENT_TIMESTAMP'", 
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
			null, "SEMICOLON", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", "DOT", "LEFT_BRACKET", 
			"RIGHT_BRACKET", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "ANTI", 
			"ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT", "AUTHORIZATION", "BETWEEN", 
			"BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", "CASCADE", "CASE", "CAST", 
			"CATALOG", "CATALOGS", "CHANGE", "CHECK", "CLEAR", "CLUSTER", "CLUSTERED", 
			"CODEGEN", "COLLATE", "COLLECTION", "COLUMN", "COLUMNS", "COMMENT", "COMMIT", 
			"COMPACT", "COMPACTIONS", "COMPUTE", "CONCATENATE", "CONSTRAINT", "COST", 
			"CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", 
			"CURRENT_TIMESTAMP", "CURRENT_USER", "DAY", "DAYOFYEAR", "DATA", "DATABASE", 
			"DATABASES", "DATEADD", "DATEDIFF", "DBPROPERTIES", "DEFINED", "DELETE", 
			"DELIMITED", "DESC", "DESCRIBE", "DFS", "DIRECTORIES", "DIRECTORY", "DISTINCT", 
			"DISTRIBUTE", "DIV", "DROP", "ELSE", "END", "ESCAPE", "ESCAPED", "EXCEPT", 
			"EXCHANGE", "EXISTS", "EXPLAIN", "EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", 
			"FALSE", "FETCH", "FIELDS", "FILTER", "FILEFORMAT", "FIRST", "FOLLOWING", 
			"FOR", "FOREIGN", "FORMAT", "FORMATTED", "FROM", "FULL", "FUNCTION", 
			"FUNCTIONS", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HAVING", "HOUR", 
			"IF", "IGNORE", "IMPORT", "IN", "INDEX", "INDEXES", "INNER", "INPATH", 
			"INPUTFORMAT", "INSERT", "INTERSECT", "INTERVAL", "INTO", "IS", "ITEMS", 
			"JOIN", "KEYS", "LAST", "LATERAL", "LAZY", "LEADING", "LEFT", "LIKE", 
			"ILIKE", "LIMIT", "LINES", "LIST", "LOAD", "LOCAL", "LOCATION", "LOCK", 
			"LOCKS", "LOGICAL", "MACRO", "MAP", "MATCHED", "MERGE", "MICROSECOND", 
			"MILLISECOND", "MINUTE", "MONTH", "MSCK", "NAMESPACE", "NAMESPACES", 
			"NATURAL", "NO", "NOT", "NULL", "NULLS", "OF", "ON", "ONLY", "OPTION", 
			"OPTIONS", "OR", "ORDER", "OUT", "OUTER", "OUTPUTFORMAT", "OVER", "OVERLAPS", 
			"OVERLAY", "OVERWRITE", "PARTITION", "PARTITIONED", "PARTITIONS", "PERCENTILE_CONT", 
			"PERCENTILE_DISC", "PERCENTLIT", "PIVOT", "PLACING", "POSITION", "PRECEDING", 
			"PRIMARY", "PRINCIPALS", "PROPERTIES", "PURGE", "QUARTER", "QUERY", "RANGE", 
			"RECORDREADER", "RECORDWRITER", "RECOVER", "REDUCE", "REFERENCES", "REFRESH", 
			"RENAME", "REPAIR", "REPEATABLE", "REPLACE", "RESET", "RESPECT", "RESTRICT", 
			"REVOKE", "RIGHT", "RLIKE", "ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", 
			"ROWS", "SECOND", "SCHEMA", "SCHEMAS", "SELECT", "SEMI", "SEPARATED", 
			"SERDE", "SERDEPROPERTIES", "SESSION_USER", "SET", "SETMINUS", "SETS", 
			"SHOW", "SKEWED", "SOME", "SORT", "SORTED", "START", "STATISTICS", "STORED", 
			"STRATIFY", "STRUCT", "SUBSTR", "SUBSTRING", "SYNC", "SYSTEM_TIME", "SYSTEM_VERSION", 
			"TABLE", "TABLES", "TABLESAMPLE", "TBLPROPERTIES", "TEMPORARY", "TERMINATED", 
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
	public String getGrammarFileName() { return "SqlBaseParser.g4"; }

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

	public SqlBaseParserParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SingleStatementContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public List<TerminalNode> SEMICOLON() { return getTokens(SqlBaseParserParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(SqlBaseParserParser.SEMICOLON, i);
		}
		public SingleStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleStatementContext singleStatement() throws RecognitionException {
		SingleStatementContext _localctx = new SingleStatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_singleStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(296);
			statement();
			setState(300);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(297);
				match(SEMICOLON);
				}
				}
				setState(302);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(303);
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
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public SingleExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleExpressionContext singleExpression() throws RecognitionException {
		SingleExpressionContext _localctx = new SingleExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_singleExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(305);
			namedExpression();
			setState(306);
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
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public SingleTableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleTableIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleTableIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleTableIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableIdentifierContext singleTableIdentifier() throws RecognitionException {
		SingleTableIdentifierContext _localctx = new SingleTableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_singleTableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			tableIdentifier();
			setState(309);
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
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public SingleMultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleMultipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleMultipartIdentifierContext singleMultipartIdentifier() throws RecognitionException {
		SingleMultipartIdentifierContext _localctx = new SingleMultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_singleMultipartIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			multipartIdentifier();
			setState(312);
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
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public SingleFunctionIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleFunctionIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleFunctionIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleFunctionIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleFunctionIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleFunctionIdentifierContext singleFunctionIdentifier() throws RecognitionException {
		SingleFunctionIdentifierContext _localctx = new SingleFunctionIdentifierContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_singleFunctionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(314);
			functionIdentifier();
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

	public static class SingleDataTypeContext extends ParserRuleContext {
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public SingleDataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleDataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleDataTypeContext singleDataType() throws RecognitionException {
		SingleDataTypeContext _localctx = new SingleDataTypeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_singleDataType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			dataType();
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

	public static class SingleTableSchemaContext extends ParserRuleContext {
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SqlBaseParserParser.EOF, 0); }
		public SingleTableSchemaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableSchema; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleTableSchema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleTableSchema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleTableSchema(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableSchemaContext singleTableSchema() throws RecognitionException {
		SingleTableSchemaContext _localctx = new SingleTableSchemaContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_singleTableSchema);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			colTypeList();
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
		public TerminalNode EXPLAIN() { return getToken(SqlBaseParserParser.EXPLAIN, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode LOGICAL() { return getToken(SqlBaseParserParser.LOGICAL, 0); }
		public TerminalNode FORMATTED() { return getToken(SqlBaseParserParser.FORMATTED, 0); }
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public TerminalNode CODEGEN() { return getToken(SqlBaseParserParser.CODEGEN, 0); }
		public TerminalNode COST() { return getToken(SqlBaseParserParser.COST, 0); }
		public ExplainContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterExplain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitExplain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitExplain(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ResetConfigurationContext extends StatementContext {
		public TerminalNode RESET() { return getToken(SqlBaseParserParser.RESET, 0); }
		public ResetConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterResetConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitResetConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitResetConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AlterViewQueryContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public AlterViewQueryContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAlterViewQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAlterViewQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAlterViewQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UseContext extends StatementContext {
		public TerminalNode USE() { return getToken(SqlBaseParserParser.USE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public UseContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUse(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropNamespaceContext extends StatementContext {
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode RESTRICT() { return getToken(SqlBaseParserParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(SqlBaseParserParser.CASCADE, 0); }
		public DropNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTempViewUsingContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode GLOBAL() { return getToken(SqlBaseParserParser.GLOBAL, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public CreateTempViewUsingContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateTempViewUsing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateTempViewUsing(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateTempViewUsing(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTableContext extends StatementContext {
		public MultipartIdentifierContext from;
		public MultipartIdentifierContext to;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode RENAME() { return getToken(SqlBaseParserParser.RENAME, 0); }
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public RenameTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRenameTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRenameTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRenameTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FailNativeCommandContext extends StatementContext {
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode ROLE() { return getToken(SqlBaseParserParser.ROLE, 0); }
		public UnsupportedHiveNativeCommandsContext unsupportedHiveNativeCommands() {
			return getRuleContext(UnsupportedHiveNativeCommandsContext.class,0);
		}
		public FailNativeCommandContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFailNativeCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFailNativeCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFailNativeCommand(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetCatalogContext extends StatementContext {
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode CATALOG() { return getToken(SqlBaseParserParser.CATALOG, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public SetCatalogContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetCatalog(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetCatalog(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetCatalog(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ClearCacheContext extends StatementContext {
		public TerminalNode CLEAR() { return getToken(SqlBaseParserParser.CLEAR, 0); }
		public TerminalNode CACHE() { return getToken(SqlBaseParserParser.CACHE, 0); }
		public ClearCacheContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterClearCache(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitClearCache(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitClearCache(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropViewContext extends StatementContext {
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public DropViewContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropView(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTablesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode TABLES() { return getToken(SqlBaseParserParser.TABLES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public ShowTablesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowTables(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RecoverPartitionsContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode RECOVER() { return getToken(SqlBaseParserParser.RECOVER, 0); }
		public TerminalNode PARTITIONS() { return getToken(SqlBaseParserParser.PARTITIONS, 0); }
		public RecoverPartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRecoverPartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRecoverPartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRecoverPartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropIndexContext extends StatementContext {
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode INDEX() { return getToken(SqlBaseParserParser.INDEX, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public DropIndexContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropIndex(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCatalogsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode CATALOGS() { return getToken(SqlBaseParserParser.CATALOGS, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public ShowCatalogsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowCatalogs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowCatalogs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowCatalogs(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCurrentNamespaceContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode CURRENT() { return getToken(SqlBaseParserParser.CURRENT, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public ShowCurrentNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowCurrentNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowCurrentNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowCurrentNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTablePartitionContext extends StatementContext {
		public PartitionSpecContext from;
		public PartitionSpecContext to;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode RENAME() { return getToken(SqlBaseParserParser.RENAME, 0); }
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
		public List<PartitionSpecContext> partitionSpec() {
			return getRuleContexts(PartitionSpecContext.class);
		}
		public PartitionSpecContext partitionSpec(int i) {
			return getRuleContext(PartitionSpecContext.class,i);
		}
		public RenameTablePartitionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRenameTablePartition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRenameTablePartition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRenameTablePartition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RepairTableContext extends StatementContext {
		public Token option;
		public TerminalNode MSCK() { return getToken(SqlBaseParserParser.MSCK, 0); }
		public TerminalNode REPAIR() { return getToken(SqlBaseParserParser.REPAIR, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode PARTITIONS() { return getToken(SqlBaseParserParser.PARTITIONS, 0); }
		public TerminalNode ADD() { return getToken(SqlBaseParserParser.ADD, 0); }
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode SYNC() { return getToken(SqlBaseParserParser.SYNC, 0); }
		public RepairTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRepairTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRepairTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRepairTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshResourceContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(SqlBaseParserParser.REFRESH, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public RefreshResourceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRefreshResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRefreshResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRefreshResource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowCreateTableContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode SERDE() { return getToken(SqlBaseParserParser.SERDE, 0); }
		public ShowCreateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowCreateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowNamespacesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public NamespacesContext namespaces() {
			return getRuleContext(NamespacesContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public ShowNamespacesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowNamespaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowNamespaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowNamespaces(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext ns;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public List<TerminalNode> FROM() { return getTokens(SqlBaseParserParser.FROM); }
		public TerminalNode FROM(int i) {
			return getToken(SqlBaseParserParser.FROM, i);
		}
		public List<TerminalNode> IN() { return getTokens(SqlBaseParserParser.IN); }
		public TerminalNode IN(int i) {
			return getToken(SqlBaseParserParser.IN, i);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowColumns(this);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public ReplaceTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterReplaceTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitReplaceTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitReplaceTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AnalyzeTablesContext extends StatementContext {
		public TerminalNode ANALYZE() { return getToken(SqlBaseParserParser.ANALYZE, 0); }
		public TerminalNode TABLES() { return getToken(SqlBaseParserParser.TABLES, 0); }
		public TerminalNode COMPUTE() { return getToken(SqlBaseParserParser.COMPUTE, 0); }
		public TerminalNode STATISTICS() { return getToken(SqlBaseParserParser.STATISTICS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public AnalyzeTablesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAnalyzeTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAnalyzeTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAnalyzeTables(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AddTablePartitionContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(SqlBaseParserParser.ADD, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public List<PartitionSpecLocationContext> partitionSpecLocation() {
			return getRuleContexts(PartitionSpecLocationContext.class);
		}
		public PartitionSpecLocationContext partitionSpecLocation(int i) {
			return getRuleContext(PartitionSpecLocationContext.class,i);
		}
		public AddTablePartitionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAddTablePartition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAddTablePartition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAddTablePartition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetNamespaceLocationContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public SetNamespaceLocationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetNamespaceLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetNamespaceLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetNamespaceLocation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshTableContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(SqlBaseParserParser.REFRESH, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public RefreshTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRefreshTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRefreshTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRefreshTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetNamespacePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode DBPROPERTIES() { return getToken(SqlBaseParserParser.DBPROPERTIES, 0); }
		public TerminalNode PROPERTIES() { return getToken(SqlBaseParserParser.PROPERTIES, 0); }
		public SetNamespacePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetNamespaceProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetNamespaceProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetNamespaceProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ManageResourceContext extends StatementContext {
		public Token op;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(SqlBaseParserParser.ADD, 0); }
		public TerminalNode LIST() { return getToken(SqlBaseParserParser.LIST, 0); }
		public ManageResourceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterManageResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitManageResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitManageResource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetQuotedConfigurationContext extends StatementContext {
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public TerminalNode EQ() { return getToken(SqlBaseParserParser.EQ, 0); }
		public ConfigValueContext configValue() {
			return getRuleContext(ConfigValueContext.class,0);
		}
		public SetQuotedConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetQuotedConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetQuotedConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetQuotedConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AnalyzeContext extends StatementContext {
		public TerminalNode ANALYZE() { return getToken(SqlBaseParserParser.ANALYZE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode COMPUTE() { return getToken(SqlBaseParserParser.COMPUTE, 0); }
		public TerminalNode STATISTICS() { return getToken(SqlBaseParserParser.STATISTICS, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FOR() { return getToken(SqlBaseParserParser.FOR, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public TerminalNode ALL() { return getToken(SqlBaseParserParser.ALL, 0); }
		public AnalyzeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAnalyze(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAnalyze(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAnalyze(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateFunctionContext extends StatementContext {
		public Token className;
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode FUNCTION() { return getToken(SqlBaseParserParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public List<ResourceContext> resource() {
			return getRuleContexts(ResourceContext.class);
		}
		public ResourceContext resource(int i) {
			return getRuleContext(ResourceContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public CreateFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class HiveReplaceColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterHiveReplaceColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitHiveReplaceColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitHiveReplaceColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CommentNamespaceContext extends StatementContext {
		public Token comment;
		public TerminalNode COMMENT() { return getToken(SqlBaseParserParser.COMMENT, 0); }
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IS() { return getToken(SqlBaseParserParser.IS, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public CommentNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCommentNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCommentNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCommentNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ResetQuotedConfigurationContext extends StatementContext {
		public TerminalNode RESET() { return getToken(SqlBaseParserParser.RESET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public ResetQuotedConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterResetQuotedConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitResetQuotedConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitResetQuotedConfiguration(this);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public CreateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateTable(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDmlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDmlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDmlStatement(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateTableLikeContext extends StatementContext {
		public TableIdentifierContext target;
		public TableIdentifierContext source;
		public PropertyListContext tableProps;
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public List<TableIdentifierContext> tableIdentifier() {
			return getRuleContexts(TableIdentifierContext.class);
		}
		public TableIdentifierContext tableIdentifier(int i) {
			return getRuleContext(TableIdentifierContext.class,i);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
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
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(SqlBaseParserParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(SqlBaseParserParser.TBLPROPERTIES, i);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateTableLike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateTableLike(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateTableLike(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UncacheTableContext extends StatementContext {
		public TerminalNode UNCACHE() { return getToken(SqlBaseParserParser.UNCACHE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public UncacheTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUncacheTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUncacheTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUncacheTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropFunctionContext extends StatementContext {
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode FUNCTION() { return getToken(SqlBaseParserParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public DropFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeRelationContext extends StatementContext {
		public Token option;
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SqlBaseParserParser.DESCRIBE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public DescribeColNameContext describeColName() {
			return getRuleContext(DescribeColNameContext.class,0);
		}
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public TerminalNode FORMATTED() { return getToken(SqlBaseParserParser.FORMATTED, 0); }
		public DescribeRelationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDescribeRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDescribeRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDescribeRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LoadDataContext extends StatementContext {
		public Token path;
		public TerminalNode LOAD() { return getToken(SqlBaseParserParser.LOAD, 0); }
		public TerminalNode DATA() { return getToken(SqlBaseParserParser.DATA, 0); }
		public TerminalNode INPATH() { return getToken(SqlBaseParserParser.INPATH, 0); }
		public TerminalNode INTO() { return getToken(SqlBaseParserParser.INTO, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(SqlBaseParserParser.LOCAL, 0); }
		public TerminalNode OVERWRITE() { return getToken(SqlBaseParserParser.OVERWRITE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public LoadDataContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLoadData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLoadData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLoadData(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowPartitionsContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode PARTITIONS() { return getToken(SqlBaseParserParser.PARTITIONS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public ShowPartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowPartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowPartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowPartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeFunctionContext extends StatementContext {
		public TerminalNode FUNCTION() { return getToken(SqlBaseParserParser.FUNCTION, 0); }
		public DescribeFuncNameContext describeFuncName() {
			return getRuleContext(DescribeFuncNameContext.class,0);
		}
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SqlBaseParserParser.DESCRIBE, 0); }
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public DescribeFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDescribeFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDescribeFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDescribeFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RenameTableColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext from;
		public ErrorCapturingIdentifierContext to;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode RENAME() { return getToken(SqlBaseParserParser.RENAME, 0); }
		public TerminalNode COLUMN() { return getToken(SqlBaseParserParser.COLUMN, 0); }
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRenameTableColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRenameTableColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRenameTableColumn(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterStatementDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitStatementDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitStatementDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class HiveChangeColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext colName;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode CHANGE() { return getToken(SqlBaseParserParser.CHANGE, 0); }
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
		public TerminalNode COLUMN() { return getToken(SqlBaseParserParser.COLUMN, 0); }
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public HiveChangeColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterHiveChangeColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitHiveChangeColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitHiveChangeColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTimeZoneContext extends StatementContext {
		public Token timezone;
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode TIME() { return getToken(SqlBaseParserParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(SqlBaseParserParser.ZONE, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(SqlBaseParserParser.LOCAL, 0); }
		public SetTimeZoneContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetTimeZone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetTimeZone(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetTimeZone(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DescribeQueryContext extends StatementContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SqlBaseParserParser.DESCRIBE, 0); }
		public TerminalNode QUERY() { return getToken(SqlBaseParserParser.QUERY, 0); }
		public DescribeQueryContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDescribeQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDescribeQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDescribeQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TruncateTableContext extends StatementContext {
		public TerminalNode TRUNCATE() { return getToken(SqlBaseParserParser.TRUNCATE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TruncateTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTruncateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTruncateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTruncateTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTableSerDeContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode SERDE() { return getToken(SqlBaseParserParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode WITH() { return getToken(SqlBaseParserParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(SqlBaseParserParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public SetTableSerDeContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetTableSerDe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetTableSerDe(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetTableSerDe(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateViewContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public IdentifierCommentListContext identifierCommentList() {
			return getRuleContext(IdentifierCommentListContext.class,0);
		}
		public List<CommentSpecContext> commentSpec() {
			return getRuleContexts(CommentSpecContext.class);
		}
		public CommentSpecContext commentSpec(int i) {
			return getRuleContext(CommentSpecContext.class,i);
		}
		public List<TerminalNode> PARTITIONED() { return getTokens(SqlBaseParserParser.PARTITIONED); }
		public TerminalNode PARTITIONED(int i) {
			return getToken(SqlBaseParserParser.PARTITIONED, i);
		}
		public List<TerminalNode> ON() { return getTokens(SqlBaseParserParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(SqlBaseParserParser.ON, i);
		}
		public List<IdentifierListContext> identifierList() {
			return getRuleContexts(IdentifierListContext.class);
		}
		public IdentifierListContext identifierList(int i) {
			return getRuleContext(IdentifierListContext.class,i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(SqlBaseParserParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(SqlBaseParserParser.TBLPROPERTIES, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public TerminalNode GLOBAL() { return getToken(SqlBaseParserParser.GLOBAL, 0); }
		public CreateViewContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateView(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTablePartitionsContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public List<PartitionSpecContext> partitionSpec() {
			return getRuleContexts(PartitionSpecContext.class);
		}
		public PartitionSpecContext partitionSpec(int i) {
			return getRuleContext(PartitionSpecContext.class,i);
		}
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public TerminalNode PURGE() { return getToken(SqlBaseParserParser.PURGE, 0); }
		public DropTablePartitionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropTablePartitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropTablePartitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropTablePartitions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetConfigurationContext extends StatementContext {
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public ConfigKeyContext configKey() {
			return getRuleContext(ConfigKeyContext.class,0);
		}
		public TerminalNode EQ() { return getToken(SqlBaseParserParser.EQ, 0); }
		public SetConfigurationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetConfiguration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetConfiguration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetConfiguration(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTableContext extends StatementContext {
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode PURGE() { return getToken(SqlBaseParserParser.PURGE, 0); }
		public DropTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTableExtendedContext extends StatementContext {
		public MultipartIdentifierContext ns;
		public Token pattern;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public ShowTableExtendedContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowTableExtended(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowTableExtended(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowTableExtended(this);
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
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SqlBaseParserParser.DESCRIBE, 0); }
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public DescribeNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDescribeNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDescribeNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDescribeNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AlterTableAlterColumnContext extends StatementContext {
		public MultipartIdentifierContext table;
		public MultipartIdentifierContext column;
		public List<TerminalNode> ALTER() { return getTokens(SqlBaseParserParser.ALTER); }
		public TerminalNode ALTER(int i) {
			return getToken(SqlBaseParserParser.ALTER, i);
		}
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public TerminalNode CHANGE() { return getToken(SqlBaseParserParser.CHANGE, 0); }
		public TerminalNode COLUMN() { return getToken(SqlBaseParserParser.COLUMN, 0); }
		public AlterColumnActionContext alterColumnAction() {
			return getRuleContext(AlterColumnActionContext.class,0);
		}
		public AlterTableAlterColumnContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAlterTableAlterColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAlterTableAlterColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAlterTableAlterColumn(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RefreshFunctionContext extends StatementContext {
		public TerminalNode REFRESH() { return getToken(SqlBaseParserParser.REFRESH, 0); }
		public TerminalNode FUNCTION() { return getToken(SqlBaseParserParser.FUNCTION, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public RefreshFunctionContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRefreshFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRefreshFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRefreshFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CommentTableContext extends StatementContext {
		public Token comment;
		public TerminalNode COMMENT() { return getToken(SqlBaseParserParser.COMMENT, 0); }
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IS() { return getToken(SqlBaseParserParser.IS, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public CommentTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCommentTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCommentTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCommentTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateIndexContext extends StatementContext {
		public IdentifierContext indexType;
		public MultipartIdentifierPropertyListContext columns;
		public PropertyListContext options;
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(SqlBaseParserParser.INDEX, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public MultipartIdentifierPropertyListContext multipartIdentifierPropertyList() {
			return getRuleContext(MultipartIdentifierPropertyListContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public CreateIndexContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateIndex(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UseNamespaceContext extends StatementContext {
		public TerminalNode USE() { return getToken(SqlBaseParserParser.USE, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public UseNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUseNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUseNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUseNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CreateNamespaceContext extends StatementContext {
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
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
		public List<TerminalNode> WITH() { return getTokens(SqlBaseParserParser.WITH); }
		public TerminalNode WITH(int i) {
			return getToken(SqlBaseParserParser.WITH, i);
		}
		public List<PropertyListContext> propertyList() {
			return getRuleContexts(PropertyListContext.class);
		}
		public PropertyListContext propertyList(int i) {
			return getRuleContext(PropertyListContext.class,i);
		}
		public List<TerminalNode> DBPROPERTIES() { return getTokens(SqlBaseParserParser.DBPROPERTIES); }
		public TerminalNode DBPROPERTIES(int i) {
			return getToken(SqlBaseParserParser.DBPROPERTIES, i);
		}
		public List<TerminalNode> PROPERTIES() { return getTokens(SqlBaseParserParser.PROPERTIES); }
		public TerminalNode PROPERTIES(int i) {
			return getToken(SqlBaseParserParser.PROPERTIES, i);
		}
		public CreateNamespaceContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateNamespace(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowTblPropertiesContext extends StatementContext {
		public MultipartIdentifierContext table;
		public PropertyKeyContext key;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(SqlBaseParserParser.TBLPROPERTIES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public PropertyKeyContext propertyKey() {
			return getRuleContext(PropertyKeyContext.class,0);
		}
		public ShowTblPropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowTblProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowTblProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowTblProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnsetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode UNSET() { return getToken(SqlBaseParserParser.UNSET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(SqlBaseParserParser.TBLPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public UnsetTablePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUnsetTableProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUnsetTableProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUnsetTableProperties(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTableLocationContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public LocationSpecContext locationSpec() {
			return getRuleContext(LocationSpecContext.class,0);
		}
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public SetTableLocationContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetTableLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetTableLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetTableLocation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DropTableColumnsContext extends StatementContext {
		public MultipartIdentifierListContext columns;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode COLUMN() { return getToken(SqlBaseParserParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public DropTableColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDropTableColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDropTableColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDropTableColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowViewsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode VIEWS() { return getToken(SqlBaseParserParser.VIEWS, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public ShowViewsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowViews(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowViews(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowViews(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShowFunctionsContext extends StatementContext {
		public MultipartIdentifierContext ns;
		public MultipartIdentifierContext legacy;
		public Token pattern;
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode FUNCTIONS() { return getToken(SqlBaseParserParser.FUNCTIONS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
		}
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public ShowFunctionsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterShowFunctions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitShowFunctions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitShowFunctions(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CacheTableContext extends StatementContext {
		public PropertyListContext options;
		public TerminalNode CACHE() { return getToken(SqlBaseParserParser.CACHE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LAZY() { return getToken(SqlBaseParserParser.LAZY, 0); }
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public CacheTableContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCacheTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCacheTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCacheTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AddTableColumnsContext extends StatementContext {
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode ADD() { return getToken(SqlBaseParserParser.ADD, 0); }
		public TerminalNode COLUMN() { return getToken(SqlBaseParserParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() {
			return getRuleContext(QualifiedColTypeWithPositionListContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public AddTableColumnsContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAddTableColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAddTableColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAddTableColumns(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(SqlBaseParserParser.TBLPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public SetTablePropertiesContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetTableProperties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetTableProperties(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetTableProperties(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_statement);
		int _la;
		try {
			int _alt;
			setState(1124);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				_localctx = new StatementDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(323);
				query();
				}
				break;
			case 2:
				_localctx = new DmlStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(325);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(324);
					ctes();
					}
				}

				setState(327);
				dmlStatementNoWith();
				}
				break;
			case 3:
				_localctx = new UseContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(328);
				match(USE);
				setState(329);
				multipartIdentifier();
				}
				break;
			case 4:
				_localctx = new UseNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(330);
				match(USE);
				setState(331);
				namespace();
				setState(332);
				multipartIdentifier();
				}
				break;
			case 5:
				_localctx = new SetCatalogContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(334);
				match(SET);
				setState(335);
				match(CATALOG);
				setState(338);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(336);
					identifier();
					}
					break;
				case 2:
					{
					setState(337);
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
				setState(340);
				match(CREATE);
				setState(341);
				namespace();
				setState(345);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(342);
					match(IF);
					setState(343);
					match(NOT);
					setState(344);
					match(EXISTS);
					}
					break;
				}
				setState(347);
				multipartIdentifier();
				setState(355);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==LOCATION || _la==WITH) {
					{
					setState(353);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(348);
						commentSpec();
						}
						break;
					case LOCATION:
						{
						setState(349);
						locationSpec();
						}
						break;
					case WITH:
						{
						{
						setState(350);
						match(WITH);
						setState(351);
						_la = _input.LA(1);
						if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(352);
						propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(357);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 7:
				_localctx = new SetNamespacePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(358);
				match(ALTER);
				setState(359);
				namespace();
				setState(360);
				multipartIdentifier();
				setState(361);
				match(SET);
				setState(362);
				_la = _input.LA(1);
				if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(363);
				propertyList();
				}
				break;
			case 8:
				_localctx = new SetNamespaceLocationContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(365);
				match(ALTER);
				setState(366);
				namespace();
				setState(367);
				multipartIdentifier();
				setState(368);
				match(SET);
				setState(369);
				locationSpec();
				}
				break;
			case 9:
				_localctx = new DropNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(371);
				match(DROP);
				setState(372);
				namespace();
				setState(375);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(373);
					match(IF);
					setState(374);
					match(EXISTS);
					}
					break;
				}
				setState(377);
				multipartIdentifier();
				setState(379);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CASCADE || _la==RESTRICT) {
					{
					setState(378);
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
				setState(381);
				match(SHOW);
				setState(382);
				namespaces();
				setState(385);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(383);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(384);
					multipartIdentifier();
					}
				}

				setState(391);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(388);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(387);
						match(LIKE);
						}
					}

					setState(390);
					((ShowNamespacesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 11:
				_localctx = new CreateTableContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(393);
				createTableHeader();
				setState(398);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
				case 1:
					{
					setState(394);
					match(LEFT_PAREN);
					setState(395);
					colTypeList();
					setState(396);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(400);
					tableProvider();
					}
				}

				setState(403);
				createTableClauses();
				setState(408);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 195)) & ~0x3f) == 0 && ((1L << (_la - 195)) & ((1L << (REDUCE - 195)) | (1L << (SELECT - 195)) | (1L << (TABLE - 195)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(405);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(404);
						match(AS);
						}
					}

					setState(407);
					query();
					}
				}

				}
				break;
			case 12:
				_localctx = new CreateTableLikeContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(410);
				match(CREATE);
				setState(411);
				match(TABLE);
				setState(415);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(412);
					match(IF);
					setState(413);
					match(NOT);
					setState(414);
					match(EXISTS);
					}
					break;
				}
				setState(417);
				((CreateTableLikeContext)_localctx).target = tableIdentifier();
				setState(418);
				match(LIKE);
				setState(419);
				((CreateTableLikeContext)_localctx).source = tableIdentifier();
				setState(428);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LOCATION || ((((_la - 212)) & ~0x3f) == 0 && ((1L << (_la - 212)) & ((1L << (ROW - 212)) | (1L << (STORED - 212)) | (1L << (TBLPROPERTIES - 212)) | (1L << (USING - 212)))) != 0)) {
					{
					setState(426);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case USING:
						{
						setState(420);
						tableProvider();
						}
						break;
					case ROW:
						{
						setState(421);
						rowFormat();
						}
						break;
					case STORED:
						{
						setState(422);
						createFileFormat();
						}
						break;
					case LOCATION:
						{
						setState(423);
						locationSpec();
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(424);
						match(TBLPROPERTIES);
						setState(425);
						((CreateTableLikeContext)_localctx).tableProps = propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(430);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 13:
				_localctx = new ReplaceTableContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(431);
				replaceTableHeader();
				setState(436);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(432);
					match(LEFT_PAREN);
					setState(433);
					colTypeList();
					setState(434);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(439);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(438);
					tableProvider();
					}
				}

				setState(441);
				createTableClauses();
				setState(446);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 195)) & ~0x3f) == 0 && ((1L << (_la - 195)) & ((1L << (REDUCE - 195)) | (1L << (SELECT - 195)) | (1L << (TABLE - 195)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(443);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(442);
						match(AS);
						}
					}

					setState(445);
					query();
					}
				}

				}
				break;
			case 14:
				_localctx = new AnalyzeContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(448);
				match(ANALYZE);
				setState(449);
				match(TABLE);
				setState(450);
				multipartIdentifier();
				setState(452);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(451);
					partitionSpec();
					}
				}

				setState(454);
				match(COMPUTE);
				setState(455);
				match(STATISTICS);
				setState(463);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(456);
					identifier();
					}
					break;
				case 2:
					{
					setState(457);
					match(FOR);
					setState(458);
					match(COLUMNS);
					setState(459);
					identifierSeq();
					}
					break;
				case 3:
					{
					setState(460);
					match(FOR);
					setState(461);
					match(ALL);
					setState(462);
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
				setState(465);
				match(ANALYZE);
				setState(466);
				match(TABLES);
				setState(469);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(467);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(468);
					multipartIdentifier();
					}
				}

				setState(471);
				match(COMPUTE);
				setState(472);
				match(STATISTICS);
				setState(474);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(473);
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
				setState(476);
				match(ALTER);
				setState(477);
				match(TABLE);
				setState(478);
				multipartIdentifier();
				setState(479);
				match(ADD);
				setState(480);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(481);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				}
				break;
			case 17:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(483);
				match(ALTER);
				setState(484);
				match(TABLE);
				setState(485);
				multipartIdentifier();
				setState(486);
				match(ADD);
				setState(487);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(488);
				match(LEFT_PAREN);
				setState(489);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(490);
				match(RIGHT_PAREN);
				}
				break;
			case 18:
				_localctx = new RenameTableColumnContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(492);
				match(ALTER);
				setState(493);
				match(TABLE);
				setState(494);
				((RenameTableColumnContext)_localctx).table = multipartIdentifier();
				setState(495);
				match(RENAME);
				setState(496);
				match(COLUMN);
				setState(497);
				((RenameTableColumnContext)_localctx).from = multipartIdentifier();
				setState(498);
				match(TO);
				setState(499);
				((RenameTableColumnContext)_localctx).to = errorCapturingIdentifier();
				}
				break;
			case 19:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 19);
				{
				setState(501);
				match(ALTER);
				setState(502);
				match(TABLE);
				setState(503);
				multipartIdentifier();
				setState(504);
				match(DROP);
				setState(505);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(508);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(506);
					match(IF);
					setState(507);
					match(EXISTS);
					}
				}

				setState(510);
				match(LEFT_PAREN);
				setState(511);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				setState(512);
				match(RIGHT_PAREN);
				}
				break;
			case 20:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(514);
				match(ALTER);
				setState(515);
				match(TABLE);
				setState(516);
				multipartIdentifier();
				setState(517);
				match(DROP);
				setState(518);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(521);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(519);
					match(IF);
					setState(520);
					match(EXISTS);
					}
					break;
				}
				setState(523);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				}
				break;
			case 21:
				_localctx = new RenameTableContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(525);
				match(ALTER);
				setState(526);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(527);
				((RenameTableContext)_localctx).from = multipartIdentifier();
				setState(528);
				match(RENAME);
				setState(529);
				match(TO);
				setState(530);
				((RenameTableContext)_localctx).to = multipartIdentifier();
				}
				break;
			case 22:
				_localctx = new SetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(532);
				match(ALTER);
				setState(533);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(534);
				multipartIdentifier();
				setState(535);
				match(SET);
				setState(536);
				match(TBLPROPERTIES);
				setState(537);
				propertyList();
				}
				break;
			case 23:
				_localctx = new UnsetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(539);
				match(ALTER);
				setState(540);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(541);
				multipartIdentifier();
				setState(542);
				match(UNSET);
				setState(543);
				match(TBLPROPERTIES);
				setState(546);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(544);
					match(IF);
					setState(545);
					match(EXISTS);
					}
				}

				setState(548);
				propertyList();
				}
				break;
			case 24:
				_localctx = new AlterTableAlterColumnContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(550);
				match(ALTER);
				setState(551);
				match(TABLE);
				setState(552);
				((AlterTableAlterColumnContext)_localctx).table = multipartIdentifier();
				setState(553);
				_la = _input.LA(1);
				if ( !(_la==ALTER || _la==CHANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(555);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(554);
					match(COLUMN);
					}
					break;
				}
				setState(557);
				((AlterTableAlterColumnContext)_localctx).column = multipartIdentifier();
				setState(559);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==COMMENT || _la==DROP || _la==FIRST || _la==SET || _la==TYPE) {
					{
					setState(558);
					alterColumnAction();
					}
				}

				}
				break;
			case 25:
				_localctx = new HiveChangeColumnContext(_localctx);
				enterOuterAlt(_localctx, 25);
				{
				setState(561);
				match(ALTER);
				setState(562);
				match(TABLE);
				setState(563);
				((HiveChangeColumnContext)_localctx).table = multipartIdentifier();
				setState(565);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(564);
					partitionSpec();
					}
				}

				setState(567);
				match(CHANGE);
				setState(569);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(568);
					match(COLUMN);
					}
					break;
				}
				setState(571);
				((HiveChangeColumnContext)_localctx).colName = multipartIdentifier();
				setState(572);
				colType();
				setState(574);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==FIRST) {
					{
					setState(573);
					colPosition();
					}
				}

				}
				break;
			case 26:
				_localctx = new HiveReplaceColumnsContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(576);
				match(ALTER);
				setState(577);
				match(TABLE);
				setState(578);
				((HiveReplaceColumnsContext)_localctx).table = multipartIdentifier();
				setState(580);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(579);
					partitionSpec();
					}
				}

				setState(582);
				match(REPLACE);
				setState(583);
				match(COLUMNS);
				setState(584);
				match(LEFT_PAREN);
				setState(585);
				((HiveReplaceColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(586);
				match(RIGHT_PAREN);
				}
				break;
			case 27:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(588);
				match(ALTER);
				setState(589);
				match(TABLE);
				setState(590);
				multipartIdentifier();
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
				match(SET);
				setState(595);
				match(SERDE);
				setState(596);
				match(STRING);
				setState(600);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(597);
					match(WITH);
					setState(598);
					match(SERDEPROPERTIES);
					setState(599);
					propertyList();
					}
				}

				}
				break;
			case 28:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(602);
				match(ALTER);
				setState(603);
				match(TABLE);
				setState(604);
				multipartIdentifier();
				setState(606);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(605);
					partitionSpec();
					}
				}

				setState(608);
				match(SET);
				setState(609);
				match(SERDEPROPERTIES);
				setState(610);
				propertyList();
				}
				break;
			case 29:
				_localctx = new AddTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(612);
				match(ALTER);
				setState(613);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(614);
				multipartIdentifier();
				setState(615);
				match(ADD);
				setState(619);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(616);
					match(IF);
					setState(617);
					match(NOT);
					setState(618);
					match(EXISTS);
					}
				}

				setState(622); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(621);
					partitionSpecLocation();
					}
					}
					setState(624); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==PARTITION );
				}
				break;
			case 30:
				_localctx = new RenameTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(626);
				match(ALTER);
				setState(627);
				match(TABLE);
				setState(628);
				multipartIdentifier();
				setState(629);
				((RenameTablePartitionContext)_localctx).from = partitionSpec();
				setState(630);
				match(RENAME);
				setState(631);
				match(TO);
				setState(632);
				((RenameTablePartitionContext)_localctx).to = partitionSpec();
				}
				break;
			case 31:
				_localctx = new DropTablePartitionsContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(634);
				match(ALTER);
				setState(635);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(636);
				multipartIdentifier();
				setState(637);
				match(DROP);
				setState(640);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(638);
					match(IF);
					setState(639);
					match(EXISTS);
					}
				}

				setState(642);
				partitionSpec();
				setState(647);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(643);
					match(COMMA);
					setState(644);
					partitionSpec();
					}
					}
					setState(649);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(651);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(650);
					match(PURGE);
					}
				}

				}
				break;
			case 32:
				_localctx = new SetTableLocationContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(653);
				match(ALTER);
				setState(654);
				match(TABLE);
				setState(655);
				multipartIdentifier();
				setState(657);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(656);
					partitionSpec();
					}
				}

				setState(659);
				match(SET);
				setState(660);
				locationSpec();
				}
				break;
			case 33:
				_localctx = new RecoverPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 33);
				{
				setState(662);
				match(ALTER);
				setState(663);
				match(TABLE);
				setState(664);
				multipartIdentifier();
				setState(665);
				match(RECOVER);
				setState(666);
				match(PARTITIONS);
				}
				break;
			case 34:
				_localctx = new DropTableContext(_localctx);
				enterOuterAlt(_localctx, 34);
				{
				setState(668);
				match(DROP);
				setState(669);
				match(TABLE);
				setState(672);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(670);
					match(IF);
					setState(671);
					match(EXISTS);
					}
					break;
				}
				setState(674);
				multipartIdentifier();
				setState(676);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(675);
					match(PURGE);
					}
				}

				}
				break;
			case 35:
				_localctx = new DropViewContext(_localctx);
				enterOuterAlt(_localctx, 35);
				{
				setState(678);
				match(DROP);
				setState(679);
				match(VIEW);
				setState(682);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
				case 1:
					{
					setState(680);
					match(IF);
					setState(681);
					match(EXISTS);
					}
					break;
				}
				setState(684);
				multipartIdentifier();
				}
				break;
			case 36:
				_localctx = new CreateViewContext(_localctx);
				enterOuterAlt(_localctx, 36);
				{
				setState(685);
				match(CREATE);
				setState(688);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(686);
					match(OR);
					setState(687);
					match(REPLACE);
					}
				}

				setState(694);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL || _la==TEMPORARY) {
					{
					setState(691);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==GLOBAL) {
						{
						setState(690);
						match(GLOBAL);
						}
					}

					setState(693);
					match(TEMPORARY);
					}
				}

				setState(696);
				match(VIEW);
				setState(700);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
				case 1:
					{
					setState(697);
					match(IF);
					setState(698);
					match(NOT);
					setState(699);
					match(EXISTS);
					}
					break;
				}
				setState(702);
				multipartIdentifier();
				setState(704);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(703);
					identifierCommentList();
					}
				}

				setState(714);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==PARTITIONED || _la==TBLPROPERTIES) {
					{
					setState(712);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(706);
						commentSpec();
						}
						break;
					case PARTITIONED:
						{
						{
						setState(707);
						match(PARTITIONED);
						setState(708);
						match(ON);
						setState(709);
						identifierList();
						}
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(710);
						match(TBLPROPERTIES);
						setState(711);
						propertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(716);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(717);
				match(AS);
				setState(718);
				query();
				}
				break;
			case 37:
				_localctx = new CreateTempViewUsingContext(_localctx);
				enterOuterAlt(_localctx, 37);
				{
				setState(720);
				match(CREATE);
				setState(723);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(721);
					match(OR);
					setState(722);
					match(REPLACE);
					}
				}

				setState(726);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL) {
					{
					setState(725);
					match(GLOBAL);
					}
				}

				setState(728);
				match(TEMPORARY);
				setState(729);
				match(VIEW);
				setState(730);
				tableIdentifier();
				setState(735);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(731);
					match(LEFT_PAREN);
					setState(732);
					colTypeList();
					setState(733);
					match(RIGHT_PAREN);
					}
				}

				setState(737);
				tableProvider();
				setState(740);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(738);
					match(OPTIONS);
					setState(739);
					propertyList();
					}
				}

				}
				break;
			case 38:
				_localctx = new AlterViewQueryContext(_localctx);
				enterOuterAlt(_localctx, 38);
				{
				setState(742);
				match(ALTER);
				setState(743);
				match(VIEW);
				setState(744);
				multipartIdentifier();
				setState(746);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(745);
					match(AS);
					}
				}

				setState(748);
				query();
				}
				break;
			case 39:
				_localctx = new CreateFunctionContext(_localctx);
				enterOuterAlt(_localctx, 39);
				{
				setState(750);
				match(CREATE);
				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(751);
					match(OR);
					setState(752);
					match(REPLACE);
					}
				}

				setState(756);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(755);
					match(TEMPORARY);
					}
				}

				setState(758);
				match(FUNCTION);
				setState(762);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
				case 1:
					{
					setState(759);
					match(IF);
					setState(760);
					match(NOT);
					setState(761);
					match(EXISTS);
					}
					break;
				}
				setState(764);
				multipartIdentifier();
				setState(765);
				match(AS);
				setState(766);
				((CreateFunctionContext)_localctx).className = match(STRING);
				setState(776);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(767);
					match(USING);
					setState(768);
					resource();
					setState(773);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(769);
						match(COMMA);
						setState(770);
						resource();
						}
						}
						setState(775);
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
				setState(778);
				match(DROP);
				setState(780);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(779);
					match(TEMPORARY);
					}
				}

				setState(782);
				match(FUNCTION);
				setState(785);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
				case 1:
					{
					setState(783);
					match(IF);
					setState(784);
					match(EXISTS);
					}
					break;
				}
				setState(787);
				multipartIdentifier();
				}
				break;
			case 41:
				_localctx = new ExplainContext(_localctx);
				enterOuterAlt(_localctx, 41);
				{
				setState(788);
				match(EXPLAIN);
				setState(790);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CODEGEN || _la==COST || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & ((1L << (EXTENDED - 88)) | (1L << (FORMATTED - 88)) | (1L << (LOGICAL - 88)))) != 0)) {
					{
					setState(789);
					_la = _input.LA(1);
					if ( !(_la==CODEGEN || _la==COST || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & ((1L << (EXTENDED - 88)) | (1L << (FORMATTED - 88)) | (1L << (LOGICAL - 88)))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(792);
				statement();
				}
				break;
			case 42:
				_localctx = new ShowTablesContext(_localctx);
				enterOuterAlt(_localctx, 42);
				{
				setState(793);
				match(SHOW);
				setState(794);
				match(TABLES);
				setState(797);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(795);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(796);
					multipartIdentifier();
					}
				}

				setState(803);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(800);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(799);
						match(LIKE);
						}
					}

					setState(802);
					((ShowTablesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 43:
				_localctx = new ShowTableExtendedContext(_localctx);
				enterOuterAlt(_localctx, 43);
				{
				setState(805);
				match(SHOW);
				setState(806);
				match(TABLE);
				setState(807);
				match(EXTENDED);
				setState(810);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(808);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(809);
					((ShowTableExtendedContext)_localctx).ns = multipartIdentifier();
					}
				}

				setState(812);
				match(LIKE);
				setState(813);
				((ShowTableExtendedContext)_localctx).pattern = match(STRING);
				setState(815);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(814);
					partitionSpec();
					}
				}

				}
				break;
			case 44:
				_localctx = new ShowTblPropertiesContext(_localctx);
				enterOuterAlt(_localctx, 44);
				{
				setState(817);
				match(SHOW);
				setState(818);
				match(TBLPROPERTIES);
				setState(819);
				((ShowTblPropertiesContext)_localctx).table = multipartIdentifier();
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(820);
					match(LEFT_PAREN);
					setState(821);
					((ShowTblPropertiesContext)_localctx).key = propertyKey();
					setState(822);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			case 45:
				_localctx = new ShowColumnsContext(_localctx);
				enterOuterAlt(_localctx, 45);
				{
				setState(826);
				match(SHOW);
				setState(827);
				match(COLUMNS);
				setState(828);
				_la = _input.LA(1);
				if ( !(_la==FROM || _la==IN) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(829);
				((ShowColumnsContext)_localctx).table = multipartIdentifier();
				setState(832);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(830);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(831);
					((ShowColumnsContext)_localctx).ns = multipartIdentifier();
					}
				}

				}
				break;
			case 46:
				_localctx = new ShowViewsContext(_localctx);
				enterOuterAlt(_localctx, 46);
				{
				setState(834);
				match(SHOW);
				setState(835);
				match(VIEWS);
				setState(838);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(836);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(837);
					multipartIdentifier();
					}
				}

				setState(844);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(841);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(840);
						match(LIKE);
						}
					}

					setState(843);
					((ShowViewsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 47:
				_localctx = new ShowPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 47);
				{
				setState(846);
				match(SHOW);
				setState(847);
				match(PARTITIONS);
				setState(848);
				multipartIdentifier();
				setState(850);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(849);
					partitionSpec();
					}
				}

				}
				break;
			case 48:
				_localctx = new ShowFunctionsContext(_localctx);
				enterOuterAlt(_localctx, 48);
				{
				setState(852);
				match(SHOW);
				setState(854);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
				case 1:
					{
					setState(853);
					identifier();
					}
					break;
				}
				setState(856);
				match(FUNCTIONS);
				setState(859);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
				case 1:
					{
					setState(857);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(858);
					((ShowFunctionsContext)_localctx).ns = multipartIdentifier();
					}
					break;
				}
				setState(868);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
				case 1:
					{
					setState(862);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
					case 1:
						{
						setState(861);
						match(LIKE);
						}
						break;
					}
					setState(866);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
					case 1:
						{
						setState(864);
						((ShowFunctionsContext)_localctx).legacy = multipartIdentifier();
						}
						break;
					case 2:
						{
						setState(865);
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
				setState(870);
				match(SHOW);
				setState(871);
				match(CREATE);
				setState(872);
				match(TABLE);
				setState(873);
				multipartIdentifier();
				setState(876);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(874);
					match(AS);
					setState(875);
					match(SERDE);
					}
				}

				}
				break;
			case 50:
				_localctx = new ShowCurrentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 50);
				{
				setState(878);
				match(SHOW);
				setState(879);
				match(CURRENT);
				setState(880);
				namespace();
				}
				break;
			case 51:
				_localctx = new ShowCatalogsContext(_localctx);
				enterOuterAlt(_localctx, 51);
				{
				setState(881);
				match(SHOW);
				setState(882);
				match(CATALOGS);
				setState(887);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(884);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(883);
						match(LIKE);
						}
					}

					setState(886);
					((ShowCatalogsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 52:
				_localctx = new DescribeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 52);
				{
				setState(889);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(890);
				match(FUNCTION);
				setState(892);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(891);
					match(EXTENDED);
					}
					break;
				}
				setState(894);
				describeFuncName();
				}
				break;
			case 53:
				_localctx = new DescribeNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 53);
				{
				setState(895);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(896);
				namespace();
				setState(898);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(897);
					match(EXTENDED);
					}
					break;
				}
				setState(900);
				multipartIdentifier();
				}
				break;
			case 54:
				_localctx = new DescribeRelationContext(_localctx);
				enterOuterAlt(_localctx, 54);
				{
				setState(902);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(904);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
				case 1:
					{
					setState(903);
					match(TABLE);
					}
					break;
				}
				setState(907);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
				case 1:
					{
					setState(906);
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
				setState(909);
				multipartIdentifier();
				setState(911);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(910);
					partitionSpec();
					}
					break;
				}
				setState(914);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(913);
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
				setState(916);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(918);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUERY) {
					{
					setState(917);
					match(QUERY);
					}
				}

				setState(920);
				query();
				}
				break;
			case 56:
				_localctx = new CommentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 56);
				{
				setState(921);
				match(COMMENT);
				setState(922);
				match(ON);
				setState(923);
				namespace();
				setState(924);
				multipartIdentifier();
				setState(925);
				match(IS);
				setState(926);
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
				setState(928);
				match(COMMENT);
				setState(929);
				match(ON);
				setState(930);
				match(TABLE);
				setState(931);
				multipartIdentifier();
				setState(932);
				match(IS);
				setState(933);
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
				setState(935);
				match(REFRESH);
				setState(936);
				match(TABLE);
				setState(937);
				multipartIdentifier();
				}
				break;
			case 59:
				_localctx = new RefreshFunctionContext(_localctx);
				enterOuterAlt(_localctx, 59);
				{
				setState(938);
				match(REFRESH);
				setState(939);
				match(FUNCTION);
				setState(940);
				multipartIdentifier();
				}
				break;
			case 60:
				_localctx = new RefreshResourceContext(_localctx);
				enterOuterAlt(_localctx, 60);
				{
				setState(941);
				match(REFRESH);
				setState(949);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
				case 1:
					{
					setState(942);
					match(STRING);
					}
					break;
				case 2:
					{
					setState(946);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(943);
							matchWildcard();
							}
							} 
						}
						setState(948);
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
				setState(951);
				match(CACHE);
				setState(953);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LAZY) {
					{
					setState(952);
					match(LAZY);
					}
				}

				setState(955);
				match(TABLE);
				setState(956);
				multipartIdentifier();
				setState(959);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(957);
					match(OPTIONS);
					setState(958);
					((CacheTableContext)_localctx).options = propertyList();
					}
				}

				setState(965);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN || _la==AS || _la==FROM || _la==MAP || ((((_la - 195)) & ~0x3f) == 0 && ((1L << (_la - 195)) & ((1L << (REDUCE - 195)) | (1L << (SELECT - 195)) | (1L << (TABLE - 195)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(962);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(961);
						match(AS);
						}
					}

					setState(964);
					query();
					}
				}

				}
				break;
			case 62:
				_localctx = new UncacheTableContext(_localctx);
				enterOuterAlt(_localctx, 62);
				{
				setState(967);
				match(UNCACHE);
				setState(968);
				match(TABLE);
				setState(971);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
				case 1:
					{
					setState(969);
					match(IF);
					setState(970);
					match(EXISTS);
					}
					break;
				}
				setState(973);
				multipartIdentifier();
				}
				break;
			case 63:
				_localctx = new ClearCacheContext(_localctx);
				enterOuterAlt(_localctx, 63);
				{
				setState(974);
				match(CLEAR);
				setState(975);
				match(CACHE);
				}
				break;
			case 64:
				_localctx = new LoadDataContext(_localctx);
				enterOuterAlt(_localctx, 64);
				{
				setState(976);
				match(LOAD);
				setState(977);
				match(DATA);
				setState(979);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(978);
					match(LOCAL);
					}
				}

				setState(981);
				match(INPATH);
				setState(982);
				((LoadDataContext)_localctx).path = match(STRING);
				setState(984);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OVERWRITE) {
					{
					setState(983);
					match(OVERWRITE);
					}
				}

				setState(986);
				match(INTO);
				setState(987);
				match(TABLE);
				setState(988);
				multipartIdentifier();
				setState(990);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(989);
					partitionSpec();
					}
				}

				}
				break;
			case 65:
				_localctx = new TruncateTableContext(_localctx);
				enterOuterAlt(_localctx, 65);
				{
				setState(992);
				match(TRUNCATE);
				setState(993);
				match(TABLE);
				setState(994);
				multipartIdentifier();
				setState(996);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(995);
					partitionSpec();
					}
				}

				}
				break;
			case 66:
				_localctx = new RepairTableContext(_localctx);
				enterOuterAlt(_localctx, 66);
				{
				setState(998);
				match(MSCK);
				setState(999);
				match(REPAIR);
				setState(1000);
				match(TABLE);
				setState(1001);
				multipartIdentifier();
				setState(1004);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==DROP || _la==SYNC) {
					{
					setState(1002);
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
					setState(1003);
					match(PARTITIONS);
					}
				}

				}
				break;
			case 67:
				_localctx = new ManageResourceContext(_localctx);
				enterOuterAlt(_localctx, 67);
				{
				setState(1006);
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
				setState(1007);
				identifier();
				setState(1011);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1008);
						matchWildcard();
						}
						} 
					}
					setState(1013);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				}
				}
				break;
			case 68:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 68);
				{
				setState(1014);
				match(SET);
				setState(1015);
				match(ROLE);
				setState(1019);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1016);
						matchWildcard();
						}
						} 
					}
					setState(1021);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				}
				}
				break;
			case 69:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 69);
				{
				setState(1022);
				match(SET);
				setState(1023);
				match(TIME);
				setState(1024);
				match(ZONE);
				setState(1025);
				interval();
				}
				break;
			case 70:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 70);
				{
				setState(1026);
				match(SET);
				setState(1027);
				match(TIME);
				setState(1028);
				match(ZONE);
				setState(1029);
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
				setState(1030);
				match(SET);
				setState(1031);
				match(TIME);
				setState(1032);
				match(ZONE);
				setState(1036);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1033);
						matchWildcard();
						}
						} 
					}
					setState(1038);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				}
				}
				break;
			case 72:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 72);
				{
				setState(1039);
				match(SET);
				setState(1040);
				configKey();
				setState(1041);
				match(EQ);
				setState(1042);
				configValue();
				}
				break;
			case 73:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 73);
				{
				setState(1044);
				match(SET);
				setState(1045);
				configKey();
				setState(1053);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1046);
					match(EQ);
					setState(1050);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
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
				setState(1055);
				match(SET);
				setState(1059);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1056);
						matchWildcard();
						}
						} 
					}
					setState(1061);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				}
				setState(1062);
				match(EQ);
				setState(1063);
				configValue();
				}
				break;
			case 75:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 75);
				{
				setState(1064);
				match(SET);
				setState(1068);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1065);
						matchWildcard();
						}
						} 
					}
					setState(1070);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				}
				}
				break;
			case 76:
				_localctx = new ResetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 76);
				{
				setState(1071);
				match(RESET);
				setState(1072);
				configKey();
				}
				break;
			case 77:
				_localctx = new ResetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 77);
				{
				setState(1073);
				match(RESET);
				setState(1077);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1074);
						matchWildcard();
						}
						} 
					}
					setState(1079);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
				}
				}
				break;
			case 78:
				_localctx = new CreateIndexContext(_localctx);
				enterOuterAlt(_localctx, 78);
				{
				setState(1080);
				match(CREATE);
				setState(1081);
				match(INDEX);
				setState(1085);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
				case 1:
					{
					setState(1082);
					match(IF);
					setState(1083);
					match(NOT);
					setState(1084);
					match(EXISTS);
					}
					break;
				}
				setState(1087);
				identifier();
				setState(1088);
				match(ON);
				setState(1090);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
				case 1:
					{
					setState(1089);
					match(TABLE);
					}
					break;
				}
				setState(1092);
				multipartIdentifier();
				setState(1095);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(1093);
					match(USING);
					setState(1094);
					((CreateIndexContext)_localctx).indexType = identifier();
					}
				}

				setState(1097);
				match(LEFT_PAREN);
				setState(1098);
				((CreateIndexContext)_localctx).columns = multipartIdentifierPropertyList();
				setState(1099);
				match(RIGHT_PAREN);
				setState(1102);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1100);
					match(OPTIONS);
					setState(1101);
					((CreateIndexContext)_localctx).options = propertyList();
					}
				}

				}
				break;
			case 79:
				_localctx = new DropIndexContext(_localctx);
				enterOuterAlt(_localctx, 79);
				{
				setState(1104);
				match(DROP);
				setState(1105);
				match(INDEX);
				setState(1108);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
				case 1:
					{
					setState(1106);
					match(IF);
					setState(1107);
					match(EXISTS);
					}
					break;
				}
				setState(1110);
				identifier();
				setState(1111);
				match(ON);
				setState(1113);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
				case 1:
					{
					setState(1112);
					match(TABLE);
					}
					break;
				}
				setState(1115);
				multipartIdentifier();
				}
				break;
			case 80:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 80);
				{
				setState(1117);
				unsupportedHiveNativeCommands();
				setState(1121);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,119,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1118);
						matchWildcard();
						}
						} 
					}
					setState(1123);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterConfigKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitConfigKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitConfigKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigKeyContext configKey() throws RecognitionException {
		ConfigKeyContext _localctx = new ConfigKeyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_configKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1126);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterConfigValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitConfigValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitConfigValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigValueContext configValue() throws RecognitionException {
		ConfigValueContext _localctx = new ConfigValueContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_configValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1128);
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
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode ROLE() { return getToken(SqlBaseParserParser.ROLE, 0); }
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode GRANT() { return getToken(SqlBaseParserParser.GRANT, 0); }
		public TerminalNode REVOKE() { return getToken(SqlBaseParserParser.REVOKE, 0); }
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode PRINCIPALS() { return getToken(SqlBaseParserParser.PRINCIPALS, 0); }
		public TerminalNode ROLES() { return getToken(SqlBaseParserParser.ROLES, 0); }
		public TerminalNode CURRENT() { return getToken(SqlBaseParserParser.CURRENT, 0); }
		public TerminalNode EXPORT() { return getToken(SqlBaseParserParser.EXPORT, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode IMPORT() { return getToken(SqlBaseParserParser.IMPORT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(SqlBaseParserParser.COMPACTIONS, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(SqlBaseParserParser.TRANSACTIONS, 0); }
		public TerminalNode INDEXES() { return getToken(SqlBaseParserParser.INDEXES, 0); }
		public TerminalNode LOCKS() { return getToken(SqlBaseParserParser.LOCKS, 0); }
		public TerminalNode INDEX() { return getToken(SqlBaseParserParser.INDEX, 0); }
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode LOCK() { return getToken(SqlBaseParserParser.LOCK, 0); }
		public TerminalNode DATABASE() { return getToken(SqlBaseParserParser.DATABASE, 0); }
		public TerminalNode UNLOCK() { return getToken(SqlBaseParserParser.UNLOCK, 0); }
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode MACRO() { return getToken(SqlBaseParserParser.MACRO, 0); }
		public TableIdentifierContext tableIdentifier() {
			return getRuleContext(TableIdentifierContext.class,0);
		}
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode CLUSTERED() { return getToken(SqlBaseParserParser.CLUSTERED, 0); }
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public TerminalNode SORTED() { return getToken(SqlBaseParserParser.SORTED, 0); }
		public TerminalNode SKEWED() { return getToken(SqlBaseParserParser.SKEWED, 0); }
		public TerminalNode STORED() { return getToken(SqlBaseParserParser.STORED, 0); }
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(SqlBaseParserParser.DIRECTORIES, 0); }
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode LOCATION() { return getToken(SqlBaseParserParser.LOCATION, 0); }
		public TerminalNode EXCHANGE() { return getToken(SqlBaseParserParser.EXCHANGE, 0); }
		public TerminalNode PARTITION() { return getToken(SqlBaseParserParser.PARTITION, 0); }
		public TerminalNode ARCHIVE() { return getToken(SqlBaseParserParser.ARCHIVE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(SqlBaseParserParser.UNARCHIVE, 0); }
		public TerminalNode TOUCH() { return getToken(SqlBaseParserParser.TOUCH, 0); }
		public TerminalNode COMPACT() { return getToken(SqlBaseParserParser.COMPACT, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode CONCATENATE() { return getToken(SqlBaseParserParser.CONCATENATE, 0); }
		public TerminalNode FILEFORMAT() { return getToken(SqlBaseParserParser.FILEFORMAT, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public TerminalNode START() { return getToken(SqlBaseParserParser.START, 0); }
		public TerminalNode TRANSACTION() { return getToken(SqlBaseParserParser.TRANSACTION, 0); }
		public TerminalNode COMMIT() { return getToken(SqlBaseParserParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(SqlBaseParserParser.ROLLBACK, 0); }
		public TerminalNode DFS() { return getToken(SqlBaseParserParser.DFS, 0); }
		public UnsupportedHiveNativeCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsupportedHiveNativeCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUnsupportedHiveNativeCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUnsupportedHiveNativeCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUnsupportedHiveNativeCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnsupportedHiveNativeCommandsContext unsupportedHiveNativeCommands() throws RecognitionException {
		UnsupportedHiveNativeCommandsContext _localctx = new UnsupportedHiveNativeCommandsContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_unsupportedHiveNativeCommands);
		int _la;
		try {
			setState(1298);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1130);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1131);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1132);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1133);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1134);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(GRANT);
				setState(1136);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
				case 1:
					{
					setState(1135);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1138);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(REVOKE);
				setState(1140);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
				case 1:
					{
					setState(1139);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1142);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1143);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(GRANT);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1144);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1145);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				setState(1147);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
				case 1:
					{
					setState(1146);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(GRANT);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1149);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1150);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(PRINCIPALS);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1151);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1152);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLES);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1153);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1154);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CURRENT);
				setState(1155);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ROLES);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1156);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(EXPORT);
				setState(1157);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1158);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(IMPORT);
				setState(1159);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1160);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1161);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(COMPACTIONS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1162);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1163);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CREATE);
				setState(1164);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TABLE);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1165);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1166);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTIONS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1167);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1168);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEXES);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1169);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1170);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(LOCKS);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1171);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1172);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1173);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1174);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1175);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1176);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1177);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1178);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1179);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1180);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1181);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1182);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1183);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1184);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1185);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1186);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1187);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1188);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1189);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1190);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1191);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1192);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1193);
				tableIdentifier();
				setState(1194);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1195);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(CLUSTERED);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(1197);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1198);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1199);
				tableIdentifier();
				setState(1200);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CLUSTERED);
				setState(1201);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
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
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SORTED);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(1209);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1210);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1211);
				tableIdentifier();
				setState(1212);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SKEWED);
				setState(1213);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
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
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(1221);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1222);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1223);
				tableIdentifier();
				setState(1224);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1225);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(STORED);
				setState(1226);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(AS);
				setState(1227);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw6 = match(DIRECTORIES);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(1229);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1230);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1231);
				tableIdentifier();
				setState(1232);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1233);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				setState(1234);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(LOCATION);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(1236);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1237);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1238);
				tableIdentifier();
				setState(1239);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(EXCHANGE);
				setState(1240);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(1242);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1243);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1244);
				tableIdentifier();
				setState(1245);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ARCHIVE);
				setState(1246);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(1248);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1249);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1250);
				tableIdentifier();
				setState(1251);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(UNARCHIVE);
				setState(1252);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(1254);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1255);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1256);
				tableIdentifier();
				setState(1257);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TOUCH);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(1259);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1260);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1261);
				tableIdentifier();
				setState(1263);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1262);
					partitionSpec();
					}
				}

				setState(1265);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(COMPACT);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(1267);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1268);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1269);
				tableIdentifier();
				setState(1271);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1270);
					partitionSpec();
					}
				}

				setState(1273);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CONCATENATE);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1275);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1276);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1277);
				tableIdentifier();
				setState(1279);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1278);
					partitionSpec();
					}
				}

				setState(1281);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1282);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(FILEFORMAT);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1284);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1285);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1286);
				tableIdentifier();
				setState(1288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1287);
					partitionSpec();
					}
				}

				setState(1290);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(REPLACE);
				setState(1291);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(COLUMNS);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1293);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(START);
				setState(1294);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTION);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1295);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(COMMIT);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1296);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ROLLBACK);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1297);
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
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode EXTERNAL() { return getToken(SqlBaseParserParser.EXTERNAL, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public CreateTableHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateTableHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateTableHeader(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateTableHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableHeaderContext createTableHeader() throws RecognitionException {
		CreateTableHeaderContext _localctx = new CreateTableHeaderContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_createTableHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1300);
			match(CREATE);
			setState(1302);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(1301);
				match(TEMPORARY);
				}
			}

			setState(1305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTERNAL) {
				{
				setState(1304);
				match(EXTERNAL);
				}
			}

			setState(1307);
			match(TABLE);
			setState(1311);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				{
				setState(1308);
				match(IF);
				setState(1309);
				match(NOT);
				setState(1310);
				match(EXISTS);
				}
				break;
			}
			setState(1313);
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
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public ReplaceTableHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceTableHeader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterReplaceTableHeader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitReplaceTableHeader(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitReplaceTableHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReplaceTableHeaderContext replaceTableHeader() throws RecognitionException {
		ReplaceTableHeaderContext _localctx = new ReplaceTableHeaderContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_replaceTableHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1317);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CREATE) {
				{
				setState(1315);
				match(CREATE);
				setState(1316);
				match(OR);
				}
			}

			setState(1319);
			match(REPLACE);
			setState(1320);
			match(TABLE);
			setState(1321);
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
		public TerminalNode CLUSTERED() { return getToken(SqlBaseParserParser.CLUSTERED, 0); }
		public List<TerminalNode> BY() { return getTokens(SqlBaseParserParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(SqlBaseParserParser.BY, i);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode INTO() { return getToken(SqlBaseParserParser.INTO, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public TerminalNode BUCKETS() { return getToken(SqlBaseParserParser.BUCKETS, 0); }
		public TerminalNode SORTED() { return getToken(SqlBaseParserParser.SORTED, 0); }
		public OrderedIdentifierListContext orderedIdentifierList() {
			return getRuleContext(OrderedIdentifierListContext.class,0);
		}
		public BucketSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bucketSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterBucketSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitBucketSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitBucketSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BucketSpecContext bucketSpec() throws RecognitionException {
		BucketSpecContext _localctx = new BucketSpecContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_bucketSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1323);
			match(CLUSTERED);
			setState(1324);
			match(BY);
			setState(1325);
			identifierList();
			setState(1329);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SORTED) {
				{
				setState(1326);
				match(SORTED);
				setState(1327);
				match(BY);
				setState(1328);
				orderedIdentifierList();
				}
			}

			setState(1331);
			match(INTO);
			setState(1332);
			match(INTEGER_VALUE);
			setState(1333);
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
		public TerminalNode SKEWED() { return getToken(SqlBaseParserParser.SKEWED, 0); }
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public ConstantListContext constantList() {
			return getRuleContext(ConstantListContext.class,0);
		}
		public NestedConstantListContext nestedConstantList() {
			return getRuleContext(NestedConstantListContext.class,0);
		}
		public TerminalNode STORED() { return getToken(SqlBaseParserParser.STORED, 0); }
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(SqlBaseParserParser.DIRECTORIES, 0); }
		public SkewSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skewSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSkewSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSkewSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSkewSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SkewSpecContext skewSpec() throws RecognitionException {
		SkewSpecContext _localctx = new SkewSpecContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_skewSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1335);
			match(SKEWED);
			setState(1336);
			match(BY);
			setState(1337);
			identifierList();
			setState(1338);
			match(ON);
			setState(1341);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
			case 1:
				{
				setState(1339);
				constantList();
				}
				break;
			case 2:
				{
				setState(1340);
				nestedConstantList();
				}
				break;
			}
			setState(1346);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				{
				setState(1343);
				match(STORED);
				setState(1344);
				match(AS);
				setState(1345);
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
		public TerminalNode LOCATION() { return getToken(SqlBaseParserParser.LOCATION, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public LocationSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_locationSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLocationSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLocationSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLocationSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocationSpecContext locationSpec() throws RecognitionException {
		LocationSpecContext _localctx = new LocationSpecContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_locationSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1348);
			match(LOCATION);
			setState(1349);
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
		public TerminalNode COMMENT() { return getToken(SqlBaseParserParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public CommentSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCommentSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCommentSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCommentSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentSpecContext commentSpec() throws RecognitionException {
		CommentSpecContext _localctx = new CommentSpecContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_commentSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1351);
			match(COMMENT);
			setState(1352);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1355);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(1354);
				ctes();
				}
			}

			setState(1357);
			queryTerm(0);
			setState(1358);
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
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(SqlBaseParserParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(SqlBaseParserParser.DIRECTORY, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode LOCAL() { return getToken(SqlBaseParserParser.LOCAL, 0); }
		public RowFormatContext rowFormat() {
			return getRuleContext(RowFormatContext.class,0);
		}
		public CreateFileFormatContext createFileFormat() {
			return getRuleContext(CreateFileFormatContext.class,0);
		}
		public InsertOverwriteHiveDirContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInsertOverwriteHiveDir(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInsertOverwriteHiveDir(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInsertOverwriteHiveDir(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertOverwriteDirContext extends InsertIntoContext {
		public Token path;
		public PropertyListContext options;
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(SqlBaseParserParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(SqlBaseParserParser.DIRECTORY, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode LOCAL() { return getToken(SqlBaseParserParser.LOCAL, 0); }
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public InsertOverwriteDirContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInsertOverwriteDir(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInsertOverwriteDir(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInsertOverwriteDir(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertOverwriteTableContext extends InsertIntoContext {
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(SqlBaseParserParser.OVERWRITE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public InsertOverwriteTableContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInsertOverwriteTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInsertOverwriteTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInsertOverwriteTable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InsertIntoTableContext extends InsertIntoContext {
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(SqlBaseParserParser.INTO, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public PartitionSpecContext partitionSpec() {
			return getRuleContext(PartitionSpecContext.class,0);
		}
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public InsertIntoTableContext(InsertIntoContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInsertIntoTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInsertIntoTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInsertIntoTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertIntoContext insertInto() throws RecognitionException {
		InsertIntoContext _localctx = new InsertIntoContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_insertInto);
		int _la;
		try {
			setState(1421);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				_localctx = new InsertOverwriteTableContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1360);
				match(INSERT);
				setState(1361);
				match(OVERWRITE);
				setState(1363);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
				case 1:
					{
					setState(1362);
					match(TABLE);
					}
					break;
				}
				setState(1365);
				multipartIdentifier();
				setState(1372);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1366);
					partitionSpec();
					setState(1370);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IF) {
						{
						setState(1367);
						match(IF);
						setState(1368);
						match(NOT);
						setState(1369);
						match(EXISTS);
						}
					}

					}
				}

				setState(1375);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
				case 1:
					{
					setState(1374);
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
				setState(1377);
				match(INSERT);
				setState(1378);
				match(INTO);
				setState(1380);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
				case 1:
					{
					setState(1379);
					match(TABLE);
					}
					break;
				}
				setState(1382);
				multipartIdentifier();
				setState(1384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1383);
					partitionSpec();
					}
				}

				setState(1389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(1386);
					match(IF);
					setState(1387);
					match(NOT);
					setState(1388);
					match(EXISTS);
					}
				}

				setState(1392);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
				case 1:
					{
					setState(1391);
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
				setState(1394);
				match(INSERT);
				setState(1395);
				match(OVERWRITE);
				setState(1397);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1396);
					match(LOCAL);
					}
				}

				setState(1399);
				match(DIRECTORY);
				setState(1400);
				((InsertOverwriteHiveDirContext)_localctx).path = match(STRING);
				setState(1402);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(1401);
					rowFormat();
					}
				}

				setState(1405);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORED) {
					{
					setState(1404);
					createFileFormat();
					}
				}

				}
				break;
			case 4:
				_localctx = new InsertOverwriteDirContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1407);
				match(INSERT);
				setState(1408);
				match(OVERWRITE);
				setState(1410);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1409);
					match(LOCAL);
					}
				}

				setState(1412);
				match(DIRECTORY);
				setState(1414);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING) {
					{
					setState(1413);
					((InsertOverwriteDirContext)_localctx).path = match(STRING);
					}
				}

				setState(1416);
				tableProvider();
				setState(1419);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1417);
					match(OPTIONS);
					setState(1418);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPartitionSpecLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPartitionSpecLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPartitionSpecLocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSpecLocationContext partitionSpecLocation() throws RecognitionException {
		PartitionSpecLocationContext _localctx = new PartitionSpecLocationContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_partitionSpecLocation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1423);
			partitionSpec();
			setState(1425);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOCATION) {
				{
				setState(1424);
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
		public TerminalNode PARTITION() { return getToken(SqlBaseParserParser.PARTITION, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<PartitionValContext> partitionVal() {
			return getRuleContexts(PartitionValContext.class);
		}
		public PartitionValContext partitionVal(int i) {
			return getRuleContext(PartitionValContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public PartitionSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPartitionSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPartitionSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPartitionSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSpecContext partitionSpec() throws RecognitionException {
		PartitionSpecContext _localctx = new PartitionSpecContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_partitionSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1427);
			match(PARTITION);
			setState(1428);
			match(LEFT_PAREN);
			setState(1429);
			partitionVal();
			setState(1434);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1430);
				match(COMMA);
				setState(1431);
				partitionVal();
				}
				}
				setState(1436);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1437);
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
		public TerminalNode EQ() { return getToken(SqlBaseParserParser.EQ, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public PartitionValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPartitionVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPartitionVal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPartitionVal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionValContext partitionVal() throws RecognitionException {
		PartitionValContext _localctx = new PartitionValContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_partitionVal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1439);
			identifier();
			setState(1442);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1440);
				match(EQ);
				setState(1441);
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
		public TerminalNode NAMESPACE() { return getToken(SqlBaseParserParser.NAMESPACE, 0); }
		public TerminalNode DATABASE() { return getToken(SqlBaseParserParser.DATABASE, 0); }
		public TerminalNode SCHEMA() { return getToken(SqlBaseParserParser.SCHEMA, 0); }
		public NamespaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNamespace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNamespace(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespaceContext namespace() throws RecognitionException {
		NamespaceContext _localctx = new NamespaceContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_namespace);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1444);
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
		public TerminalNode NAMESPACES() { return getToken(SqlBaseParserParser.NAMESPACES, 0); }
		public TerminalNode DATABASES() { return getToken(SqlBaseParserParser.DATABASES, 0); }
		public TerminalNode SCHEMAS() { return getToken(SqlBaseParserParser.SCHEMAS, 0); }
		public NamespacesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespaces; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNamespaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNamespaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNamespaces(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespacesContext namespaces() throws RecognitionException {
		NamespacesContext _localctx = new NamespacesContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_namespaces);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1446);
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
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDescribeFuncName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDescribeFuncName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDescribeFuncName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeFuncNameContext describeFuncName() throws RecognitionException {
		DescribeFuncNameContext _localctx = new DescribeFuncNameContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_describeFuncName);
		try {
			setState(1453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1448);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1449);
				match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1450);
				comparisonOperator();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1451);
				arithmeticOperator();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1452);
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
		public List<TerminalNode> DOT() { return getTokens(SqlBaseParserParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SqlBaseParserParser.DOT, i);
		}
		public DescribeColNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describeColName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDescribeColName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDescribeColName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDescribeColName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeColNameContext describeColName() throws RecognitionException {
		DescribeColNameContext _localctx = new DescribeColNameContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_describeColName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1455);
			((DescribeColNameContext)_localctx).identifier = identifier();
			((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
			setState(1460);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(1456);
				match(DOT);
				setState(1457);
				((DescribeColNameContext)_localctx).identifier = identifier();
				((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
				}
				}
				setState(1462);
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
		public TerminalNode WITH() { return getToken(SqlBaseParserParser.WITH, 0); }
		public List<NamedQueryContext> namedQuery() {
			return getRuleContexts(NamedQueryContext.class);
		}
		public NamedQueryContext namedQuery(int i) {
			return getRuleContext(NamedQueryContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public CtesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ctes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCtes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCtes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCtes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CtesContext ctes() throws RecognitionException {
		CtesContext _localctx = new CtesContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_ctes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1463);
			match(WITH);
			setState(1464);
			namedQuery();
			setState(1469);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1465);
				match(COMMA);
				setState(1466);
				namedQuery();
				}
				}
				setState(1471);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public NamedQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNamedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNamedQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNamedQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedQueryContext namedQuery() throws RecognitionException {
		NamedQueryContext _localctx = new NamedQueryContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_namedQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1472);
			((NamedQueryContext)_localctx).name = errorCapturingIdentifier();
			setState(1474);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
			case 1:
				{
				setState(1473);
				((NamedQueryContext)_localctx).columnAliases = identifierList();
				}
				break;
			}
			setState(1477);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(1476);
				match(AS);
				}
			}

			setState(1479);
			match(LEFT_PAREN);
			setState(1480);
			query();
			setState(1481);
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
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableProviderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableProvider; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTableProvider(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTableProvider(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTableProvider(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableProviderContext tableProvider() throws RecognitionException {
		TableProviderContext _localctx = new TableProviderContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_tableProvider);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1483);
			match(USING);
			setState(1484);
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
		public List<TerminalNode> OPTIONS() { return getTokens(SqlBaseParserParser.OPTIONS); }
		public TerminalNode OPTIONS(int i) {
			return getToken(SqlBaseParserParser.OPTIONS, i);
		}
		public List<TerminalNode> PARTITIONED() { return getTokens(SqlBaseParserParser.PARTITIONED); }
		public TerminalNode PARTITIONED(int i) {
			return getToken(SqlBaseParserParser.PARTITIONED, i);
		}
		public List<TerminalNode> BY() { return getTokens(SqlBaseParserParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(SqlBaseParserParser.BY, i);
		}
		public List<TerminalNode> TBLPROPERTIES() { return getTokens(SqlBaseParserParser.TBLPROPERTIES); }
		public TerminalNode TBLPROPERTIES(int i) {
			return getToken(SqlBaseParserParser.TBLPROPERTIES, i);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateTableClauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateTableClauses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateTableClauses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableClausesContext createTableClauses() throws RecognitionException {
		CreateTableClausesContext _localctx = new CreateTableClausesContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_createTableClauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1501);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CLUSTERED || _la==COMMENT || ((((_la - 141)) & ~0x3f) == 0 && ((1L << (_la - 141)) & ((1L << (LOCATION - 141)) | (1L << (OPTIONS - 141)) | (1L << (PARTITIONED - 141)))) != 0) || ((((_la - 212)) & ~0x3f) == 0 && ((1L << (_la - 212)) & ((1L << (ROW - 212)) | (1L << (SKEWED - 212)) | (1L << (STORED - 212)) | (1L << (TBLPROPERTIES - 212)))) != 0)) {
				{
				setState(1499);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case OPTIONS:
					{
					{
					setState(1486);
					match(OPTIONS);
					setState(1487);
					((CreateTableClausesContext)_localctx).options = propertyList();
					}
					}
					break;
				case PARTITIONED:
					{
					{
					setState(1488);
					match(PARTITIONED);
					setState(1489);
					match(BY);
					setState(1490);
					((CreateTableClausesContext)_localctx).partitioning = partitionFieldList();
					}
					}
					break;
				case SKEWED:
					{
					setState(1491);
					skewSpec();
					}
					break;
				case CLUSTERED:
					{
					setState(1492);
					bucketSpec();
					}
					break;
				case ROW:
					{
					setState(1493);
					rowFormat();
					}
					break;
				case STORED:
					{
					setState(1494);
					createFileFormat();
					}
					break;
				case LOCATION:
					{
					setState(1495);
					locationSpec();
					}
					break;
				case COMMENT:
					{
					setState(1496);
					commentSpec();
					}
					break;
				case TBLPROPERTIES:
					{
					{
					setState(1497);
					match(TBLPROPERTIES);
					setState(1498);
					((CreateTableClausesContext)_localctx).tableProps = propertyList();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(1503);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<PropertyContext> property() {
			return getRuleContexts(PropertyContext.class);
		}
		public PropertyContext property(int i) {
			return getRuleContext(PropertyContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public PropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyListContext propertyList() throws RecognitionException {
		PropertyListContext _localctx = new PropertyListContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_propertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1504);
			match(LEFT_PAREN);
			setState(1505);
			property();
			setState(1510);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1506);
				match(COMMA);
				setState(1507);
				property();
				}
				}
				setState(1512);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1513);
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
		public TerminalNode EQ() { return getToken(SqlBaseParserParser.EQ, 0); }
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_property);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1515);
			((PropertyContext)_localctx).key = propertyKey();
			setState(1520);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FALSE || ((((_la - 259)) & ~0x3f) == 0 && ((1L << (_la - 259)) & ((1L << (TRUE - 259)) | (1L << (EQ - 259)) | (1L << (STRING - 259)) | (1L << (INTEGER_VALUE - 259)) | (1L << (DECIMAL_VALUE - 259)))) != 0)) {
				{
				setState(1517);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1516);
					match(EQ);
					}
				}

				setState(1519);
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
		public List<TerminalNode> DOT() { return getTokens(SqlBaseParserParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SqlBaseParserParser.DOT, i);
		}
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public PropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPropertyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPropertyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyKeyContext propertyKey() throws RecognitionException {
		PropertyKeyContext _localctx = new PropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_propertyKey);
		int _la;
		try {
			setState(1531);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1522);
				identifier();
				setState(1527);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(1523);
					match(DOT);
					setState(1524);
					identifier();
					}
					}
					setState(1529);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1530);
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
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(SqlBaseParserParser.DECIMAL_VALUE, 0); }
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_propertyValue);
		try {
			setState(1537);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_VALUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1533);
				match(INTEGER_VALUE);
				}
				break;
			case DECIMAL_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1534);
				match(DECIMAL_VALUE);
				}
				break;
			case FALSE:
			case TRUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1535);
				booleanValue();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(1536);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public ConstantListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterConstantList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitConstantList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitConstantList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantListContext constantList() throws RecognitionException {
		ConstantListContext _localctx = new ConstantListContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_constantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1539);
			match(LEFT_PAREN);
			setState(1540);
			constant();
			setState(1545);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1541);
				match(COMMA);
				setState(1542);
				constant();
				}
				}
				setState(1547);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1548);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<ConstantListContext> constantList() {
			return getRuleContexts(ConstantListContext.class);
		}
		public ConstantListContext constantList(int i) {
			return getRuleContext(ConstantListContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public NestedConstantListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedConstantList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNestedConstantList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNestedConstantList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNestedConstantList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedConstantListContext nestedConstantList() throws RecognitionException {
		NestedConstantListContext _localctx = new NestedConstantListContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_nestedConstantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1550);
			match(LEFT_PAREN);
			setState(1551);
			constantList();
			setState(1556);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1552);
				match(COMMA);
				setState(1553);
				constantList();
				}
				}
				setState(1558);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1559);
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
		public TerminalNode STORED() { return getToken(SqlBaseParserParser.STORED, 0); }
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public FileFormatContext fileFormat() {
			return getRuleContext(FileFormatContext.class,0);
		}
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public StorageHandlerContext storageHandler() {
			return getRuleContext(StorageHandlerContext.class,0);
		}
		public CreateFileFormatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createFileFormat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCreateFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCreateFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCreateFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateFileFormatContext createFileFormat() throws RecognitionException {
		CreateFileFormatContext _localctx = new CreateFileFormatContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_createFileFormat);
		try {
			setState(1567);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,170,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1561);
				match(STORED);
				setState(1562);
				match(AS);
				setState(1563);
				fileFormat();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1564);
				match(STORED);
				setState(1565);
				match(BY);
				setState(1566);
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
		public TerminalNode INPUTFORMAT() { return getToken(SqlBaseParserParser.INPUTFORMAT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(SqlBaseParserParser.OUTPUTFORMAT, 0); }
		public List<TerminalNode> STRING() { return getTokens(SqlBaseParserParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(SqlBaseParserParser.STRING, i);
		}
		public TableFileFormatContext(FileFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTableFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTableFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTableFileFormat(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterGenericFileFormat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitGenericFileFormat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitGenericFileFormat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileFormatContext fileFormat() throws RecognitionException {
		FileFormatContext _localctx = new FileFormatContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_fileFormat);
		try {
			setState(1574);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
			case 1:
				_localctx = new TableFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1569);
				match(INPUTFORMAT);
				setState(1570);
				((TableFileFormatContext)_localctx).inFmt = match(STRING);
				setState(1571);
				match(OUTPUTFORMAT);
				setState(1572);
				((TableFileFormatContext)_localctx).outFmt = match(STRING);
				}
				break;
			case 2:
				_localctx = new GenericFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1573);
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
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(SqlBaseParserParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(SqlBaseParserParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public StorageHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_storageHandler; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterStorageHandler(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitStorageHandler(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitStorageHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StorageHandlerContext storageHandler() throws RecognitionException {
		StorageHandlerContext _localctx = new StorageHandlerContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_storageHandler);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1576);
			match(STRING);
			setState(1580);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
			case 1:
				{
				setState(1577);
				match(WITH);
				setState(1578);
				match(SERDEPROPERTIES);
				setState(1579);
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
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterResource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitResource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_resource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1582);
			identifier();
			setState(1583);
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
		public TerminalNode DELETE() { return getToken(SqlBaseParserParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDeleteFromTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDeleteFromTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDeleteFromTable(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSingleInsertQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSingleInsertQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSingleInsertQuery(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultiInsertQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultiInsertQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultiInsertQuery(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UpdateTableContext extends DmlStatementNoWithContext {
		public TerminalNode UPDATE() { return getToken(SqlBaseParserParser.UPDATE, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUpdateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUpdateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUpdateTable(this);
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
		public TerminalNode MERGE() { return getToken(SqlBaseParserParser.MERGE, 0); }
		public TerminalNode INTO() { return getToken(SqlBaseParserParser.INTO, 0); }
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMergeIntoTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMergeIntoTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMergeIntoTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DmlStatementNoWithContext dmlStatementNoWith() throws RecognitionException {
		DmlStatementNoWithContext _localctx = new DmlStatementNoWithContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_dmlStatementNoWith);
		int _la;
		try {
			int _alt;
			setState(1635);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INSERT:
				_localctx = new SingleInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1585);
				insertInto();
				setState(1586);
				query();
				}
				break;
			case FROM:
				_localctx = new MultiInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1588);
				fromClause();
				setState(1590); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1589);
					multiInsertQueryBody();
					}
					}
					setState(1592); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==INSERT );
				}
				break;
			case DELETE:
				_localctx = new DeleteFromTableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1594);
				match(DELETE);
				setState(1595);
				match(FROM);
				setState(1596);
				multipartIdentifier();
				setState(1597);
				tableAlias();
				setState(1599);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1598);
					whereClause();
					}
				}

				}
				break;
			case UPDATE:
				_localctx = new UpdateTableContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1601);
				match(UPDATE);
				setState(1602);
				multipartIdentifier();
				setState(1603);
				tableAlias();
				setState(1604);
				setClause();
				setState(1606);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1605);
					whereClause();
					}
				}

				}
				break;
			case MERGE:
				_localctx = new MergeIntoTableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1608);
				match(MERGE);
				setState(1609);
				match(INTO);
				setState(1610);
				((MergeIntoTableContext)_localctx).target = multipartIdentifier();
				setState(1611);
				((MergeIntoTableContext)_localctx).targetAlias = tableAlias();
				setState(1612);
				match(USING);
				setState(1618);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
				case 1:
					{
					setState(1613);
					((MergeIntoTableContext)_localctx).source = multipartIdentifier();
					}
					break;
				case 2:
					{
					setState(1614);
					match(LEFT_PAREN);
					setState(1615);
					((MergeIntoTableContext)_localctx).sourceQuery = query();
					setState(1616);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(1620);
				((MergeIntoTableContext)_localctx).sourceAlias = tableAlias();
				setState(1621);
				match(ON);
				setState(1622);
				((MergeIntoTableContext)_localctx).mergeCondition = booleanExpression(0);
				setState(1626);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,177,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1623);
						matchedClause();
						}
						} 
					}
					setState(1628);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,177,_ctx);
				}
				setState(1632);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(1629);
					notMatchedClause();
					}
					}
					setState(1634);
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
		public TerminalNode ORDER() { return getToken(SqlBaseParserParser.ORDER, 0); }
		public List<TerminalNode> BY() { return getTokens(SqlBaseParserParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(SqlBaseParserParser.BY, i);
		}
		public TerminalNode CLUSTER() { return getToken(SqlBaseParserParser.CLUSTER, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(SqlBaseParserParser.DISTRIBUTE, 0); }
		public TerminalNode SORT() { return getToken(SqlBaseParserParser.SORT, 0); }
		public WindowClauseContext windowClause() {
			return getRuleContext(WindowClauseContext.class,0);
		}
		public TerminalNode LIMIT() { return getToken(SqlBaseParserParser.LIMIT, 0); }
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
		public TerminalNode ALL() { return getToken(SqlBaseParserParser.ALL, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public QueryOrganizationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryOrganization; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQueryOrganization(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQueryOrganization(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQueryOrganization(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryOrganizationContext queryOrganization() throws RecognitionException {
		QueryOrganizationContext _localctx = new QueryOrganizationContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_queryOrganization);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1647);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				{
				setState(1637);
				match(ORDER);
				setState(1638);
				match(BY);
				setState(1639);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1644);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1640);
						match(COMMA);
						setState(1641);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1646);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
				}
				}
				break;
			}
			setState(1659);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				{
				setState(1649);
				match(CLUSTER);
				setState(1650);
				match(BY);
				setState(1651);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1656);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,182,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1652);
						match(COMMA);
						setState(1653);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1658);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,182,_ctx);
				}
				}
				break;
			}
			setState(1671);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,185,_ctx) ) {
			case 1:
				{
				setState(1661);
				match(DISTRIBUTE);
				setState(1662);
				match(BY);
				setState(1663);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1668);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1664);
						match(COMMA);
						setState(1665);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1670);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
				}
				}
				break;
			}
			setState(1683);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
			case 1:
				{
				setState(1673);
				match(SORT);
				setState(1674);
				match(BY);
				setState(1675);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1680);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1676);
						match(COMMA);
						setState(1677);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1682);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				}
				}
				break;
			}
			setState(1686);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				{
				setState(1685);
				windowClause();
				}
				break;
			}
			setState(1693);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
			case 1:
				{
				setState(1688);
				match(LIMIT);
				setState(1691);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
				case 1:
					{
					setState(1689);
					match(ALL);
					}
					break;
				case 2:
					{
					setState(1690);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultiInsertQueryBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultiInsertQueryBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultiInsertQueryBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiInsertQueryBodyContext multiInsertQueryBody() throws RecognitionException {
		MultiInsertQueryBodyContext _localctx = new MultiInsertQueryBodyContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_multiInsertQueryBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1695);
			insertInto();
			setState(1696);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQueryTermDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQueryTermDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQueryTermDefault(this);
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
		public TerminalNode INTERSECT() { return getToken(SqlBaseParserParser.INTERSECT, 0); }
		public TerminalNode UNION() { return getToken(SqlBaseParserParser.UNION, 0); }
		public TerminalNode EXCEPT() { return getToken(SqlBaseParserParser.EXCEPT, 0); }
		public TerminalNode SETMINUS() { return getToken(SqlBaseParserParser.SETMINUS, 0); }
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public SetOperationContext(QueryTermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetOperation(this);
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
		int _startState = 86;
		enterRecursionRule(_localctx, 86, RULE_queryTerm, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new QueryTermDefaultContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(1699);
			queryPrimary();
			}
			_ctx.stop = _input.LT(-1);
			setState(1724);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,195,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1722);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
					case 1:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1701);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1702);
						if (!(legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "legacy_setops_precedence_enabled");
						setState(1703);
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
						setState(1705);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1704);
							setQuantifier();
							}
						}

						setState(1707);
						((SetOperationContext)_localctx).right = queryTerm(4);
						}
						break;
					case 2:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1708);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1709);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1710);
						((SetOperationContext)_localctx).operator = match(INTERSECT);
						setState(1712);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1711);
							setQuantifier();
							}
						}

						setState(1714);
						((SetOperationContext)_localctx).right = queryTerm(3);
						}
						break;
					case 3:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1715);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(1716);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1717);
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
						setState(1719);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1718);
							setQuantifier();
							}
						}

						setState(1721);
						((SetOperationContext)_localctx).right = queryTerm(2);
						}
						break;
					}
					} 
				}
				setState(1726);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public SubqueryContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSubquery(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQueryPrimaryDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQueryPrimaryDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQueryPrimaryDefault(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInlineTableDefault1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInlineTableDefault1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInlineTableDefault1(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFromStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFromStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFromStmt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TableContext extends QueryPrimaryContext {
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableContext(QueryPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryPrimaryContext queryPrimary() throws RecognitionException {
		QueryPrimaryContext _localctx = new QueryPrimaryContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_queryPrimary);
		try {
			setState(1736);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAP:
			case REDUCE:
			case SELECT:
				_localctx = new QueryPrimaryDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1727);
				querySpecification();
				}
				break;
			case FROM:
				_localctx = new FromStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1728);
				fromStatement();
				}
				break;
			case TABLE:
				_localctx = new TableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1729);
				match(TABLE);
				setState(1730);
				multipartIdentifier();
				}
				break;
			case VALUES:
				_localctx = new InlineTableDefault1Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1731);
				inlineTable();
				}
				break;
			case LEFT_PAREN:
				_localctx = new SubqueryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1732);
				match(LEFT_PAREN);
				setState(1733);
				query();
				setState(1734);
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
		public TerminalNode NULLS() { return getToken(SqlBaseParserParser.NULLS, 0); }
		public TerminalNode ASC() { return getToken(SqlBaseParserParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode LAST() { return getToken(SqlBaseParserParser.LAST, 0); }
		public TerminalNode FIRST() { return getToken(SqlBaseParserParser.FIRST, 0); }
		public SortItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSortItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSortItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSortItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortItemContext sortItem() throws RecognitionException {
		SortItemContext _localctx = new SortItemContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_sortItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1738);
			expression();
			setState(1740);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
			case 1:
				{
				setState(1739);
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
			setState(1744);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,198,_ctx) ) {
			case 1:
				{
				setState(1742);
				match(NULLS);
				setState(1743);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFromStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFromStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFromStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromStatementContext fromStatement() throws RecognitionException {
		FromStatementContext _localctx = new FromStatementContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_fromStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1746);
			fromClause();
			setState(1748); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1747);
					fromStatementBody();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1750); 
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFromStatementBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFromStatementBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFromStatementBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromStatementBodyContext fromStatementBody() throws RecognitionException {
		FromStatementBodyContext _localctx = new FromStatementBodyContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_fromStatementBody);
		try {
			int _alt;
			setState(1779);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1752);
				transformClause();
				setState(1754);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
				case 1:
					{
					setState(1753);
					whereClause();
					}
					break;
				}
				setState(1756);
				queryOrganization();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1758);
				selectClause();
				setState(1762);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,201,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1759);
						lateralView();
						}
						} 
					}
					setState(1764);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,201,_ctx);
				}
				setState(1766);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
				case 1:
					{
					setState(1765);
					whereClause();
					}
					break;
				}
				setState(1769);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
				case 1:
					{
					setState(1768);
					aggregationClause();
					}
					break;
				}
				setState(1772);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
				case 1:
					{
					setState(1771);
					havingClause();
					}
					break;
				}
				setState(1775);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
				case 1:
					{
					setState(1774);
					windowClause();
					}
					break;
				}
				setState(1777);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRegularQuerySpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRegularQuerySpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRegularQuerySpecification(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTransformQuerySpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTransformQuerySpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTransformQuerySpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuerySpecificationContext querySpecification() throws RecognitionException {
		QuerySpecificationContext _localctx = new QuerySpecificationContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_querySpecification);
		try {
			int _alt;
			setState(1825);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,219,_ctx) ) {
			case 1:
				_localctx = new TransformQuerySpecificationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1781);
				transformClause();
				setState(1783);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(1782);
					fromClause();
					}
					break;
				}
				setState(1788);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,208,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1785);
						lateralView();
						}
						} 
					}
					setState(1790);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,208,_ctx);
				}
				setState(1792);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
				case 1:
					{
					setState(1791);
					whereClause();
					}
					break;
				}
				setState(1795);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
				case 1:
					{
					setState(1794);
					aggregationClause();
					}
					break;
				}
				setState(1798);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
				case 1:
					{
					setState(1797);
					havingClause();
					}
					break;
				}
				setState(1801);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
				case 1:
					{
					setState(1800);
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
				setState(1803);
				selectClause();
				setState(1805);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
				case 1:
					{
					setState(1804);
					fromClause();
					}
					break;
				}
				setState(1810);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1807);
						lateralView();
						}
						} 
					}
					setState(1812);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				}
				setState(1814);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,215,_ctx) ) {
				case 1:
					{
					setState(1813);
					whereClause();
					}
					break;
				}
				setState(1817);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,216,_ctx) ) {
				case 1:
					{
					setState(1816);
					aggregationClause();
					}
					break;
				}
				setState(1820);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,217,_ctx) ) {
				case 1:
					{
					setState(1819);
					havingClause();
					}
					break;
				}
				setState(1823);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
				case 1:
					{
					setState(1822);
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
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public List<TerminalNode> STRING() { return getTokens(SqlBaseParserParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(SqlBaseParserParser.STRING, i);
		}
		public TerminalNode SELECT() { return getToken(SqlBaseParserParser.SELECT, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(SqlBaseParserParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(SqlBaseParserParser.LEFT_PAREN, i);
		}
		public ExpressionSeqContext expressionSeq() {
			return getRuleContext(ExpressionSeqContext.class,0);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(SqlBaseParserParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(SqlBaseParserParser.RIGHT_PAREN, i);
		}
		public TerminalNode TRANSFORM() { return getToken(SqlBaseParserParser.TRANSFORM, 0); }
		public TerminalNode MAP() { return getToken(SqlBaseParserParser.MAP, 0); }
		public TerminalNode REDUCE() { return getToken(SqlBaseParserParser.REDUCE, 0); }
		public TerminalNode RECORDWRITER() { return getToken(SqlBaseParserParser.RECORDWRITER, 0); }
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode RECORDREADER() { return getToken(SqlBaseParserParser.RECORDREADER, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTransformClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTransformClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTransformClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformClauseContext transformClause() throws RecognitionException {
		TransformClauseContext _localctx = new TransformClauseContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_transformClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1846);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(1827);
				match(SELECT);
				setState(1828);
				((TransformClauseContext)_localctx).kind = match(TRANSFORM);
				setState(1829);
				match(LEFT_PAREN);
				setState(1831);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
				case 1:
					{
					setState(1830);
					setQuantifier();
					}
					break;
				}
				setState(1833);
				expressionSeq();
				setState(1834);
				match(RIGHT_PAREN);
				}
				break;
			case MAP:
				{
				setState(1836);
				((TransformClauseContext)_localctx).kind = match(MAP);
				setState(1838);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,221,_ctx) ) {
				case 1:
					{
					setState(1837);
					setQuantifier();
					}
					break;
				}
				setState(1840);
				expressionSeq();
				}
				break;
			case REDUCE:
				{
				setState(1841);
				((TransformClauseContext)_localctx).kind = match(REDUCE);
				setState(1843);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
				case 1:
					{
					setState(1842);
					setQuantifier();
					}
					break;
				}
				setState(1845);
				expressionSeq();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1849);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROW) {
				{
				setState(1848);
				((TransformClauseContext)_localctx).inRowFormat = rowFormat();
				}
			}

			setState(1853);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECORDWRITER) {
				{
				setState(1851);
				match(RECORDWRITER);
				setState(1852);
				((TransformClauseContext)_localctx).recordWriter = match(STRING);
				}
			}

			setState(1855);
			match(USING);
			setState(1856);
			((TransformClauseContext)_localctx).script = match(STRING);
			setState(1869);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				{
				setState(1857);
				match(AS);
				setState(1867);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,227,_ctx) ) {
				case 1:
					{
					setState(1858);
					identifierSeq();
					}
					break;
				case 2:
					{
					setState(1859);
					colTypeList();
					}
					break;
				case 3:
					{
					{
					setState(1860);
					match(LEFT_PAREN);
					setState(1863);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,226,_ctx) ) {
					case 1:
						{
						setState(1861);
						identifierSeq();
						}
						break;
					case 2:
						{
						setState(1862);
						colTypeList();
						}
						break;
					}
					setState(1865);
					match(RIGHT_PAREN);
					}
					}
					break;
				}
				}
				break;
			}
			setState(1872);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
			case 1:
				{
				setState(1871);
				((TransformClauseContext)_localctx).outRowFormat = rowFormat();
				}
				break;
			}
			setState(1876);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				{
				setState(1874);
				match(RECORDREADER);
				setState(1875);
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
		public TerminalNode SELECT() { return getToken(SqlBaseParserParser.SELECT, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_selectClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1878);
			match(SELECT);
			setState(1882);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,231,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1879);
					((SelectClauseContext)_localctx).hint = hint();
					((SelectClauseContext)_localctx).hints.add(((SelectClauseContext)_localctx).hint);
					}
					} 
				}
				setState(1884);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,231,_ctx);
			}
			setState(1886);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(1885);
				setQuantifier();
				}
				break;
			}
			setState(1888);
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
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public SetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetClauseContext setClause() throws RecognitionException {
		SetClauseContext _localctx = new SetClauseContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_setClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1890);
			match(SET);
			setState(1891);
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
		public TerminalNode WHEN() { return getToken(SqlBaseParserParser.WHEN, 0); }
		public TerminalNode MATCHED() { return getToken(SqlBaseParserParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(SqlBaseParserParser.THEN, 0); }
		public MatchedActionContext matchedAction() {
			return getRuleContext(MatchedActionContext.class,0);
		}
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public MatchedClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchedClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMatchedClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMatchedClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMatchedClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchedClauseContext matchedClause() throws RecognitionException {
		MatchedClauseContext _localctx = new MatchedClauseContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_matchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1893);
			match(WHEN);
			setState(1894);
			match(MATCHED);
			setState(1897);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1895);
				match(AND);
				setState(1896);
				((MatchedClauseContext)_localctx).matchedCond = booleanExpression(0);
				}
			}

			setState(1899);
			match(THEN);
			setState(1900);
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
		public TerminalNode WHEN() { return getToken(SqlBaseParserParser.WHEN, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode MATCHED() { return getToken(SqlBaseParserParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(SqlBaseParserParser.THEN, 0); }
		public NotMatchedActionContext notMatchedAction() {
			return getRuleContext(NotMatchedActionContext.class,0);
		}
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public NotMatchedClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notMatchedClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNotMatchedClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNotMatchedClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNotMatchedClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotMatchedClauseContext notMatchedClause() throws RecognitionException {
		NotMatchedClauseContext _localctx = new NotMatchedClauseContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_notMatchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1902);
			match(WHEN);
			setState(1903);
			match(NOT);
			setState(1904);
			match(MATCHED);
			setState(1907);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1905);
				match(AND);
				setState(1906);
				((NotMatchedClauseContext)_localctx).notMatchedCond = booleanExpression(0);
				}
			}

			setState(1909);
			match(THEN);
			setState(1910);
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
		public TerminalNode DELETE() { return getToken(SqlBaseParserParser.DELETE, 0); }
		public TerminalNode UPDATE() { return getToken(SqlBaseParserParser.UPDATE, 0); }
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode ASTERISK() { return getToken(SqlBaseParserParser.ASTERISK, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public MatchedActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchedAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMatchedAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMatchedAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMatchedAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchedActionContext matchedAction() throws RecognitionException {
		MatchedActionContext _localctx = new MatchedActionContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_matchedAction);
		try {
			setState(1919);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1912);
				match(DELETE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1913);
				match(UPDATE);
				setState(1914);
				match(SET);
				setState(1915);
				match(ASTERISK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1916);
				match(UPDATE);
				setState(1917);
				match(SET);
				setState(1918);
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
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode ASTERISK() { return getToken(SqlBaseParserParser.ASTERISK, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(SqlBaseParserParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(SqlBaseParserParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(SqlBaseParserParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(SqlBaseParserParser.RIGHT_PAREN, i);
		}
		public TerminalNode VALUES() { return getToken(SqlBaseParserParser.VALUES, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public NotMatchedActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notMatchedAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNotMatchedAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNotMatchedAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNotMatchedAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotMatchedActionContext notMatchedAction() throws RecognitionException {
		NotMatchedActionContext _localctx = new NotMatchedActionContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_notMatchedAction);
		int _la;
		try {
			setState(1939);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,237,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1921);
				match(INSERT);
				setState(1922);
				match(ASTERISK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1923);
				match(INSERT);
				setState(1924);
				match(LEFT_PAREN);
				setState(1925);
				((NotMatchedActionContext)_localctx).columns = multipartIdentifierList();
				setState(1926);
				match(RIGHT_PAREN);
				setState(1927);
				match(VALUES);
				setState(1928);
				match(LEFT_PAREN);
				setState(1929);
				expression();
				setState(1934);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1930);
					match(COMMA);
					setState(1931);
					expression();
					}
					}
					setState(1936);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1937);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public AssignmentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAssignmentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAssignmentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAssignmentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentListContext assignmentList() throws RecognitionException {
		AssignmentListContext _localctx = new AssignmentListContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1941);
			assignment();
			setState(1946);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1942);
				match(COMMA);
				setState(1943);
				assignment();
				}
				}
				setState(1948);
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
		public TerminalNode EQ() { return getToken(SqlBaseParserParser.EQ, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1949);
			((AssignmentContext)_localctx).key = multipartIdentifier();
			setState(1950);
			match(EQ);
			setState(1951);
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
		public TerminalNode WHERE() { return getToken(SqlBaseParserParser.WHERE, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1953);
			match(WHERE);
			setState(1954);
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
		public TerminalNode HAVING() { return getToken(SqlBaseParserParser.HAVING, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitHavingClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitHavingClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1956);
			match(HAVING);
			setState(1957);
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
		public TerminalNode HENT_START() { return getToken(SqlBaseParserParser.HENT_START, 0); }
		public TerminalNode HENT_END() { return getToken(SqlBaseParserParser.HENT_END, 0); }
		public List<HintStatementContext> hintStatement() {
			return getRuleContexts(HintStatementContext.class);
		}
		public HintStatementContext hintStatement(int i) {
			return getRuleContext(HintStatementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public HintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitHint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HintContext hint() throws RecognitionException {
		HintContext _localctx = new HintContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_hint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1959);
			match(HENT_START);
			setState(1960);
			((HintContext)_localctx).hintStatement = hintStatement();
			((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
			setState(1967);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1962);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,239,_ctx) ) {
					case 1:
						{
						setState(1961);
						match(COMMA);
						}
						break;
					}
					setState(1964);
					((HintContext)_localctx).hintStatement = hintStatement();
					((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
					}
					} 
				}
				setState(1969);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
			}
			setState(1970);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public HintStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hintStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterHintStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitHintStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitHintStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HintStatementContext hintStatement() throws RecognitionException {
		HintStatementContext _localctx = new HintStatementContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_hintStatement);
		int _la;
		try {
			setState(1985);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,242,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1972);
				((HintStatementContext)_localctx).hintName = identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1973);
				((HintStatementContext)_localctx).hintName = identifier();
				setState(1974);
				match(LEFT_PAREN);
				setState(1975);
				((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
				((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
				setState(1980);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1976);
					match(COMMA);
					setState(1977);
					((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
					((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
					}
					}
					setState(1982);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1983);
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
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public List<RelationContext> relation() {
			return getRuleContexts(RelationContext.class);
		}
		public RelationContext relation(int i) {
			return getRuleContext(RelationContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_fromClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1987);
			match(FROM);
			setState(1988);
			relation();
			setState(1993);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,243,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1989);
					match(COMMA);
					setState(1990);
					relation();
					}
					} 
				}
				setState(1995);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,243,_ctx);
			}
			setState(1999);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,244,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1996);
					lateralView();
					}
					} 
				}
				setState(2001);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,244,_ctx);
			}
			setState(2003);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,245,_ctx) ) {
			case 1:
				{
				setState(2002);
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
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode OF() { return getToken(SqlBaseParserParser.OF, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(SqlBaseParserParser.SYSTEM_VERSION, 0); }
		public TerminalNode VERSION() { return getToken(SqlBaseParserParser.VERSION, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode FOR() { return getToken(SqlBaseParserParser.FOR, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(SqlBaseParserParser.SYSTEM_TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(SqlBaseParserParser.TIMESTAMP, 0); }
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TemporalClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_temporalClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTemporalClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTemporalClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTemporalClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TemporalClauseContext temporalClause() throws RecognitionException {
		TemporalClauseContext _localctx = new TemporalClauseContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_temporalClause);
		int _la;
		try {
			setState(2019);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,248,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2006);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2005);
					match(FOR);
					}
				}

				setState(2008);
				_la = _input.LA(1);
				if ( !(_la==SYSTEM_VERSION || _la==VERSION) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2009);
				match(AS);
				setState(2010);
				match(OF);
				setState(2011);
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
				setState(2013);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2012);
					match(FOR);
					}
				}

				setState(2015);
				_la = _input.LA(1);
				if ( !(_la==SYSTEM_TIME || _la==TIMESTAMP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2016);
				match(AS);
				setState(2017);
				match(OF);
				setState(2018);
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
		public TerminalNode GROUP() { return getToken(SqlBaseParserParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public List<GroupByClauseContext> groupByClause() {
			return getRuleContexts(GroupByClauseContext.class);
		}
		public GroupByClauseContext groupByClause(int i) {
			return getRuleContext(GroupByClauseContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode WITH() { return getToken(SqlBaseParserParser.WITH, 0); }
		public TerminalNode SETS() { return getToken(SqlBaseParserParser.SETS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode ROLLUP() { return getToken(SqlBaseParserParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(SqlBaseParserParser.CUBE, 0); }
		public TerminalNode GROUPING() { return getToken(SqlBaseParserParser.GROUPING, 0); }
		public AggregationClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAggregationClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAggregationClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAggregationClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationClauseContext aggregationClause() throws RecognitionException {
		AggregationClauseContext _localctx = new AggregationClauseContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_aggregationClause);
		int _la;
		try {
			int _alt;
			setState(2060);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,253,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2021);
				match(GROUP);
				setState(2022);
				match(BY);
				setState(2023);
				((AggregationClauseContext)_localctx).groupByClause = groupByClause();
				((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
				setState(2028);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,249,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2024);
						match(COMMA);
						setState(2025);
						((AggregationClauseContext)_localctx).groupByClause = groupByClause();
						((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
						}
						} 
					}
					setState(2030);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,249,_ctx);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2031);
				match(GROUP);
				setState(2032);
				match(BY);
				setState(2033);
				((AggregationClauseContext)_localctx).expression = expression();
				((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
				setState(2038);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,250,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2034);
						match(COMMA);
						setState(2035);
						((AggregationClauseContext)_localctx).expression = expression();
						((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
						}
						} 
					}
					setState(2040);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,250,_ctx);
				}
				setState(2058);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,252,_ctx) ) {
				case 1:
					{
					setState(2041);
					match(WITH);
					setState(2042);
					((AggregationClauseContext)_localctx).kind = match(ROLLUP);
					}
					break;
				case 2:
					{
					setState(2043);
					match(WITH);
					setState(2044);
					((AggregationClauseContext)_localctx).kind = match(CUBE);
					}
					break;
				case 3:
					{
					setState(2045);
					((AggregationClauseContext)_localctx).kind = match(GROUPING);
					setState(2046);
					match(SETS);
					setState(2047);
					match(LEFT_PAREN);
					setState(2048);
					groupingSet();
					setState(2053);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2049);
						match(COMMA);
						setState(2050);
						groupingSet();
						}
						}
						setState(2055);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2056);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_groupByClause);
		try {
			setState(2064);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2062);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2063);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode ROLLUP() { return getToken(SqlBaseParserParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(SqlBaseParserParser.CUBE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public TerminalNode GROUPING() { return getToken(SqlBaseParserParser.GROUPING, 0); }
		public TerminalNode SETS() { return getToken(SqlBaseParserParser.SETS, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterGroupingAnalytics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitGroupingAnalytics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitGroupingAnalytics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingAnalyticsContext groupingAnalytics() throws RecognitionException {
		GroupingAnalyticsContext _localctx = new GroupingAnalyticsContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_groupingAnalytics);
		int _la;
		try {
			setState(2091);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CUBE:
			case ROLLUP:
				enterOuterAlt(_localctx, 1);
				{
				setState(2066);
				_la = _input.LA(1);
				if ( !(_la==CUBE || _la==ROLLUP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2067);
				match(LEFT_PAREN);
				setState(2068);
				groupingSet();
				setState(2073);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2069);
					match(COMMA);
					setState(2070);
					groupingSet();
					}
					}
					setState(2075);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2076);
				match(RIGHT_PAREN);
				}
				break;
			case GROUPING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2078);
				match(GROUPING);
				setState(2079);
				match(SETS);
				setState(2080);
				match(LEFT_PAREN);
				setState(2081);
				groupingElement();
				setState(2086);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2082);
					match(COMMA);
					setState(2083);
					groupingElement();
					}
					}
					setState(2088);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2089);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterGroupingElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitGroupingElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitGroupingElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingElementContext groupingElement() throws RecognitionException {
		GroupingElementContext _localctx = new GroupingElementContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_groupingElement);
		try {
			setState(2095);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,258,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2093);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2094);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public GroupingSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupingSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterGroupingSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitGroupingSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitGroupingSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupingSetContext groupingSet() throws RecognitionException {
		GroupingSetContext _localctx = new GroupingSetContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_groupingSet);
		int _la;
		try {
			setState(2110);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,261,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2097);
				match(LEFT_PAREN);
				setState(2106);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,260,_ctx) ) {
				case 1:
					{
					setState(2098);
					expression();
					setState(2103);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2099);
						match(COMMA);
						setState(2100);
						expression();
						}
						}
						setState(2105);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2108);
				match(RIGHT_PAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2109);
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
		public TerminalNode PIVOT() { return getToken(SqlBaseParserParser.PIVOT, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(SqlBaseParserParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(SqlBaseParserParser.LEFT_PAREN, i);
		}
		public TerminalNode FOR() { return getToken(SqlBaseParserParser.FOR, 0); }
		public PivotColumnContext pivotColumn() {
			return getRuleContext(PivotColumnContext.class,0);
		}
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(SqlBaseParserParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(SqlBaseParserParser.RIGHT_PAREN, i);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public PivotClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPivotClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPivotClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPivotClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotClauseContext pivotClause() throws RecognitionException {
		PivotClauseContext _localctx = new PivotClauseContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_pivotClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2112);
			match(PIVOT);
			setState(2113);
			match(LEFT_PAREN);
			setState(2114);
			((PivotClauseContext)_localctx).aggregates = namedExpressionSeq();
			setState(2115);
			match(FOR);
			setState(2116);
			pivotColumn();
			setState(2117);
			match(IN);
			setState(2118);
			match(LEFT_PAREN);
			setState(2119);
			((PivotClauseContext)_localctx).pivotValue = pivotValue();
			((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
			setState(2124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2120);
				match(COMMA);
				setState(2121);
				((PivotClauseContext)_localctx).pivotValue = pivotValue();
				((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
				}
				}
				setState(2126);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2127);
			match(RIGHT_PAREN);
			setState(2128);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public PivotColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotColumn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPivotColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPivotColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPivotColumn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotColumnContext pivotColumn() throws RecognitionException {
		PivotColumnContext _localctx = new PivotColumnContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_pivotColumn);
		int _la;
		try {
			setState(2142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2130);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2131);
				match(LEFT_PAREN);
				setState(2132);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				setState(2137);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2133);
					match(COMMA);
					setState(2134);
					((PivotColumnContext)_localctx).identifier = identifier();
					((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
					}
					}
					setState(2139);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2140);
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
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public PivotValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pivotValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPivotValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPivotValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPivotValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PivotValueContext pivotValue() throws RecognitionException {
		PivotValueContext _localctx = new PivotValueContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_pivotValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2144);
			expression();
			setState(2149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,266,_ctx) ) {
			case 1:
				{
				setState(2146);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,265,_ctx) ) {
				case 1:
					{
					setState(2145);
					match(AS);
					}
					break;
				}
				setState(2148);
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
		public TerminalNode LATERAL() { return getToken(SqlBaseParserParser.LATERAL, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode OUTER() { return getToken(SqlBaseParserParser.OUTER, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public LateralViewContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lateralView; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLateralView(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLateralView(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLateralView(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LateralViewContext lateralView() throws RecognitionException {
		LateralViewContext _localctx = new LateralViewContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_lateralView);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2151);
			match(LATERAL);
			setState(2152);
			match(VIEW);
			setState(2154);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,267,_ctx) ) {
			case 1:
				{
				setState(2153);
				match(OUTER);
				}
				break;
			}
			setState(2156);
			qualifiedName();
			setState(2157);
			match(LEFT_PAREN);
			setState(2166);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,269,_ctx) ) {
			case 1:
				{
				setState(2158);
				expression();
				setState(2163);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2159);
					match(COMMA);
					setState(2160);
					expression();
					}
					}
					setState(2165);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2168);
			match(RIGHT_PAREN);
			setState(2169);
			((LateralViewContext)_localctx).tblName = identifier();
			setState(2181);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,272,_ctx) ) {
			case 1:
				{
				setState(2171);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,270,_ctx) ) {
				case 1:
					{
					setState(2170);
					match(AS);
					}
					break;
				}
				setState(2173);
				((LateralViewContext)_localctx).identifier = identifier();
				((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
				setState(2178);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2174);
						match(COMMA);
						setState(2175);
						((LateralViewContext)_localctx).identifier = identifier();
						((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
						}
						} 
					}
					setState(2180);
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
		public TerminalNode DISTINCT() { return getToken(SqlBaseParserParser.DISTINCT, 0); }
		public TerminalNode ALL() { return getToken(SqlBaseParserParser.ALL, 0); }
		public SetQuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setQuantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSetQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSetQuantifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSetQuantifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetQuantifierContext setQuantifier() throws RecognitionException {
		SetQuantifierContext _localctx = new SetQuantifierContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_setQuantifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2183);
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
		public TerminalNode LATERAL() { return getToken(SqlBaseParserParser.LATERAL, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_relation);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,273,_ctx) ) {
			case 1:
				{
				setState(2185);
				match(LATERAL);
				}
				break;
			}
			setState(2188);
			relationPrimary();
			setState(2192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2189);
					joinRelation();
					}
					} 
				}
				setState(2194);
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
		public TerminalNode JOIN() { return getToken(SqlBaseParserParser.JOIN, 0); }
		public RelationPrimaryContext relationPrimary() {
			return getRuleContext(RelationPrimaryContext.class,0);
		}
		public JoinTypeContext joinType() {
			return getRuleContext(JoinTypeContext.class,0);
		}
		public TerminalNode LATERAL() { return getToken(SqlBaseParserParser.LATERAL, 0); }
		public JoinCriteriaContext joinCriteria() {
			return getRuleContext(JoinCriteriaContext.class,0);
		}
		public TerminalNode NATURAL() { return getToken(SqlBaseParserParser.NATURAL, 0); }
		public JoinRelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinRelation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterJoinRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitJoinRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitJoinRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinRelationContext joinRelation() throws RecognitionException {
		JoinRelationContext _localctx = new JoinRelationContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_joinRelation);
		try {
			setState(2212);
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
				setState(2195);
				joinType();
				}
				setState(2196);
				match(JOIN);
				setState(2198);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,275,_ctx) ) {
				case 1:
					{
					setState(2197);
					match(LATERAL);
					}
					break;
				}
				setState(2200);
				((JoinRelationContext)_localctx).right = relationPrimary();
				setState(2202);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,276,_ctx) ) {
				case 1:
					{
					setState(2201);
					joinCriteria();
					}
					break;
				}
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2204);
				match(NATURAL);
				setState(2205);
				joinType();
				setState(2206);
				match(JOIN);
				setState(2208);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,277,_ctx) ) {
				case 1:
					{
					setState(2207);
					match(LATERAL);
					}
					break;
				}
				setState(2210);
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
		public TerminalNode INNER() { return getToken(SqlBaseParserParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(SqlBaseParserParser.CROSS, 0); }
		public TerminalNode LEFT() { return getToken(SqlBaseParserParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(SqlBaseParserParser.OUTER, 0); }
		public TerminalNode SEMI() { return getToken(SqlBaseParserParser.SEMI, 0); }
		public TerminalNode RIGHT() { return getToken(SqlBaseParserParser.RIGHT, 0); }
		public TerminalNode FULL() { return getToken(SqlBaseParserParser.FULL, 0); }
		public TerminalNode ANTI() { return getToken(SqlBaseParserParser.ANTI, 0); }
		public JoinTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterJoinType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitJoinType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitJoinType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinTypeContext joinType() throws RecognitionException {
		JoinTypeContext _localctx = new JoinTypeContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_joinType);
		int _la;
		try {
			setState(2238);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,285,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2215);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(2214);
					match(INNER);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2217);
				match(CROSS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2218);
				match(LEFT);
				setState(2220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2219);
					match(OUTER);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2223);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2222);
					match(LEFT);
					}
				}

				setState(2225);
				match(SEMI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2226);
				match(RIGHT);
				setState(2228);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2227);
					match(OUTER);
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2230);
				match(FULL);
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
			case 7:
				enterOuterAlt(_localctx, 7);
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
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public JoinCriteriaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCriteria; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterJoinCriteria(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitJoinCriteria(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitJoinCriteria(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinCriteriaContext joinCriteria() throws RecognitionException {
		JoinCriteriaContext _localctx = new JoinCriteriaContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_joinCriteria);
		try {
			setState(2244);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(2240);
				match(ON);
				setState(2241);
				booleanExpression(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2242);
				match(USING);
				setState(2243);
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
		public TerminalNode TABLESAMPLE() { return getToken(SqlBaseParserParser.TABLESAMPLE, 0); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(SqlBaseParserParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(SqlBaseParserParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(SqlBaseParserParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(SqlBaseParserParser.RIGHT_PAREN, i);
		}
		public SampleMethodContext sampleMethod() {
			return getRuleContext(SampleMethodContext.class,0);
		}
		public TerminalNode REPEATABLE() { return getToken(SqlBaseParserParser.REPEATABLE, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public SampleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sample; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSample(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSample(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSample(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SampleContext sample() throws RecognitionException {
		SampleContext _localctx = new SampleContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_sample);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2246);
			match(TABLESAMPLE);
			setState(2247);
			match(LEFT_PAREN);
			setState(2249);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,287,_ctx) ) {
			case 1:
				{
				setState(2248);
				sampleMethod();
				}
				break;
			}
			setState(2251);
			match(RIGHT_PAREN);
			setState(2256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,288,_ctx) ) {
			case 1:
				{
				setState(2252);
				match(REPEATABLE);
				setState(2253);
				match(LEFT_PAREN);
				setState(2254);
				((SampleContext)_localctx).seed = match(INTEGER_VALUE);
				setState(2255);
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
		public TerminalNode ROWS() { return getToken(SqlBaseParserParser.ROWS, 0); }
		public SampleByRowsContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSampleByRows(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSampleByRows(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSampleByRows(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByPercentileContext extends SampleMethodContext {
		public Token negativeSign;
		public Token percentage;
		public TerminalNode PERCENTLIT() { return getToken(SqlBaseParserParser.PERCENTLIT, 0); }
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(SqlBaseParserParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public SampleByPercentileContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSampleByPercentile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSampleByPercentile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSampleByPercentile(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SampleByBucketContext extends SampleMethodContext {
		public Token sampleType;
		public Token numerator;
		public Token denominator;
		public TerminalNode OUT() { return getToken(SqlBaseParserParser.OUT, 0); }
		public TerminalNode OF() { return getToken(SqlBaseParserParser.OF, 0); }
		public TerminalNode BUCKET() { return getToken(SqlBaseParserParser.BUCKET, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(SqlBaseParserParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(SqlBaseParserParser.INTEGER_VALUE, i);
		}
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public SampleByBucketContext(SampleMethodContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSampleByBucket(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSampleByBucket(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSampleByBucket(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSampleByBytes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSampleByBytes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSampleByBytes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SampleMethodContext sampleMethod() throws RecognitionException {
		SampleMethodContext _localctx = new SampleMethodContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_sampleMethod);
		int _la;
		try {
			setState(2282);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,292,_ctx) ) {
			case 1:
				_localctx = new SampleByPercentileContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2259);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2258);
					((SampleByPercentileContext)_localctx).negativeSign = match(MINUS);
					}
				}

				setState(2261);
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
				setState(2262);
				match(PERCENTLIT);
				}
				break;
			case 2:
				_localctx = new SampleByRowsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2263);
				expression();
				setState(2264);
				match(ROWS);
				}
				break;
			case 3:
				_localctx = new SampleByBucketContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2266);
				((SampleByBucketContext)_localctx).sampleType = match(BUCKET);
				setState(2267);
				((SampleByBucketContext)_localctx).numerator = match(INTEGER_VALUE);
				setState(2268);
				match(OUT);
				setState(2269);
				match(OF);
				setState(2270);
				((SampleByBucketContext)_localctx).denominator = match(INTEGER_VALUE);
				setState(2279);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(2271);
					match(ON);
					setState(2277);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
					case 1:
						{
						setState(2272);
						identifier();
						}
						break;
					case 2:
						{
						setState(2273);
						qualifiedName();
						setState(2274);
						match(LEFT_PAREN);
						setState(2275);
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
				setState(2281);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public IdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierListContext identifierList() throws RecognitionException {
		IdentifierListContext _localctx = new IdentifierListContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_identifierList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2284);
			match(LEFT_PAREN);
			setState(2285);
			identifierSeq();
			setState(2286);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public IdentifierSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIdentifierSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIdentifierSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIdentifierSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierSeqContext identifierSeq() throws RecognitionException {
		IdentifierSeqContext _localctx = new IdentifierSeqContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_identifierSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2288);
			((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
			setState(2293);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,293,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2289);
					match(COMMA);
					setState(2290);
					((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2295);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<OrderedIdentifierContext> orderedIdentifier() {
			return getRuleContexts(OrderedIdentifierContext.class);
		}
		public OrderedIdentifierContext orderedIdentifier(int i) {
			return getRuleContext(OrderedIdentifierContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public OrderedIdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedIdentifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterOrderedIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitOrderedIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitOrderedIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedIdentifierListContext orderedIdentifierList() throws RecognitionException {
		OrderedIdentifierListContext _localctx = new OrderedIdentifierListContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_orderedIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2296);
			match(LEFT_PAREN);
			setState(2297);
			orderedIdentifier();
			setState(2302);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2298);
				match(COMMA);
				setState(2299);
				orderedIdentifier();
				}
				}
				setState(2304);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2305);
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
		public TerminalNode ASC() { return getToken(SqlBaseParserParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public OrderedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterOrderedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitOrderedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitOrderedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedIdentifierContext orderedIdentifier() throws RecognitionException {
		OrderedIdentifierContext _localctx = new OrderedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_orderedIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2307);
			((OrderedIdentifierContext)_localctx).ident = errorCapturingIdentifier();
			setState(2309);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(2308);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<IdentifierCommentContext> identifierComment() {
			return getRuleContexts(IdentifierCommentContext.class);
		}
		public IdentifierCommentContext identifierComment(int i) {
			return getRuleContext(IdentifierCommentContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public IdentifierCommentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierCommentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIdentifierCommentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIdentifierCommentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIdentifierCommentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierCommentListContext identifierCommentList() throws RecognitionException {
		IdentifierCommentListContext _localctx = new IdentifierCommentListContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_identifierCommentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2311);
			match(LEFT_PAREN);
			setState(2312);
			identifierComment();
			setState(2317);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2313);
				match(COMMA);
				setState(2314);
				identifierComment();
				}
				}
				setState(2319);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2320);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIdentifierComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIdentifierComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIdentifierComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierCommentContext identifierComment() throws RecognitionException {
		IdentifierCommentContext _localctx = new IdentifierCommentContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_identifierComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2322);
			identifier();
			setState(2324);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2323);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTableValuedFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTableValuedFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTableValuedFunction(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInlineTableDefault2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInlineTableDefault2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInlineTableDefault2(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AliasedRelationContext extends RelationPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public RelationContext relation() {
			return getRuleContext(RelationContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public AliasedRelationContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAliasedRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAliasedRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAliasedRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AliasedQueryContext extends RelationPrimaryContext {
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public SampleContext sample() {
			return getRuleContext(SampleContext.class,0);
		}
		public AliasedQueryContext(RelationPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAliasedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAliasedQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAliasedQuery(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationPrimaryContext relationPrimary() throws RecognitionException {
		RelationPrimaryContext _localctx = new RelationPrimaryContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_relationPrimary);
		try {
			setState(2353);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,302,_ctx) ) {
			case 1:
				_localctx = new TableNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2326);
				multipartIdentifier();
				setState(2328);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,298,_ctx) ) {
				case 1:
					{
					setState(2327);
					temporalClause();
					}
					break;
				}
				setState(2331);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,299,_ctx) ) {
				case 1:
					{
					setState(2330);
					sample();
					}
					break;
				}
				setState(2333);
				tableAlias();
				}
				break;
			case 2:
				_localctx = new AliasedQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2335);
				match(LEFT_PAREN);
				setState(2336);
				query();
				setState(2337);
				match(RIGHT_PAREN);
				setState(2339);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,300,_ctx) ) {
				case 1:
					{
					setState(2338);
					sample();
					}
					break;
				}
				setState(2341);
				tableAlias();
				}
				break;
			case 3:
				_localctx = new AliasedRelationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2343);
				match(LEFT_PAREN);
				setState(2344);
				relation();
				setState(2345);
				match(RIGHT_PAREN);
				setState(2347);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,301,_ctx) ) {
				case 1:
					{
					setState(2346);
					sample();
					}
					break;
				}
				setState(2349);
				tableAlias();
				}
				break;
			case 4:
				_localctx = new InlineTableDefault2Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2351);
				inlineTable();
				}
				break;
			case 5:
				_localctx = new TableValuedFunctionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2352);
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
		public TerminalNode VALUES() { return getToken(SqlBaseParserParser.VALUES, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public InlineTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInlineTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInlineTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInlineTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InlineTableContext inlineTable() throws RecognitionException {
		InlineTableContext _localctx = new InlineTableContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_inlineTable);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2355);
			match(VALUES);
			setState(2356);
			expression();
			setState(2361);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,303,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2357);
					match(COMMA);
					setState(2358);
					expression();
					}
					} 
				}
				setState(2363);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,303,_ctx);
			}
			setState(2364);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public FunctionTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFunctionTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFunctionTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFunctionTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTableContext functionTable() throws RecognitionException {
		FunctionTableContext _localctx = new FunctionTableContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_functionTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2366);
			((FunctionTableContext)_localctx).funcName = functionName();
			setState(2367);
			match(LEFT_PAREN);
			setState(2376);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,305,_ctx) ) {
			case 1:
				{
				setState(2368);
				expression();
				setState(2373);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2369);
					match(COMMA);
					setState(2370);
					expression();
					}
					}
					setState(2375);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2378);
			match(RIGHT_PAREN);
			setState(2379);
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
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public TableAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTableAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTableAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTableAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableAliasContext tableAlias() throws RecognitionException {
		TableAliasContext _localctx = new TableAliasContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_tableAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2388);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,308,_ctx) ) {
			case 1:
				{
				setState(2382);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,306,_ctx) ) {
				case 1:
					{
					setState(2381);
					match(AS);
					}
					break;
				}
				setState(2384);
				strictIdentifier();
				setState(2386);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,307,_ctx) ) {
				case 1:
					{
					setState(2385);
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
		public TerminalNode ROW() { return getToken(SqlBaseParserParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(SqlBaseParserParser.FORMAT, 0); }
		public TerminalNode SERDE() { return getToken(SqlBaseParserParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(SqlBaseParserParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(SqlBaseParserParser.SERDEPROPERTIES, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public RowFormatSerdeContext(RowFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRowFormatSerde(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRowFormatSerde(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRowFormatSerde(this);
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
		public TerminalNode ROW() { return getToken(SqlBaseParserParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(SqlBaseParserParser.FORMAT, 0); }
		public TerminalNode DELIMITED() { return getToken(SqlBaseParserParser.DELIMITED, 0); }
		public TerminalNode FIELDS() { return getToken(SqlBaseParserParser.FIELDS, 0); }
		public List<TerminalNode> TERMINATED() { return getTokens(SqlBaseParserParser.TERMINATED); }
		public TerminalNode TERMINATED(int i) {
			return getToken(SqlBaseParserParser.TERMINATED, i);
		}
		public List<TerminalNode> BY() { return getTokens(SqlBaseParserParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(SqlBaseParserParser.BY, i);
		}
		public TerminalNode COLLECTION() { return getToken(SqlBaseParserParser.COLLECTION, 0); }
		public TerminalNode ITEMS() { return getToken(SqlBaseParserParser.ITEMS, 0); }
		public TerminalNode MAP() { return getToken(SqlBaseParserParser.MAP, 0); }
		public TerminalNode KEYS() { return getToken(SqlBaseParserParser.KEYS, 0); }
		public TerminalNode LINES() { return getToken(SqlBaseParserParser.LINES, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public TerminalNode DEFINED() { return getToken(SqlBaseParserParser.DEFINED, 0); }
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public List<TerminalNode> STRING() { return getTokens(SqlBaseParserParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(SqlBaseParserParser.STRING, i);
		}
		public TerminalNode ESCAPED() { return getToken(SqlBaseParserParser.ESCAPED, 0); }
		public RowFormatDelimitedContext(RowFormatContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRowFormatDelimited(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRowFormatDelimited(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRowFormatDelimited(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowFormatContext rowFormat() throws RecognitionException {
		RowFormatContext _localctx = new RowFormatContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_rowFormat);
		try {
			setState(2439);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,316,_ctx) ) {
			case 1:
				_localctx = new RowFormatSerdeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2390);
				match(ROW);
				setState(2391);
				match(FORMAT);
				setState(2392);
				match(SERDE);
				setState(2393);
				((RowFormatSerdeContext)_localctx).name = match(STRING);
				setState(2397);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,309,_ctx) ) {
				case 1:
					{
					setState(2394);
					match(WITH);
					setState(2395);
					match(SERDEPROPERTIES);
					setState(2396);
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
				setState(2399);
				match(ROW);
				setState(2400);
				match(FORMAT);
				setState(2401);
				match(DELIMITED);
				setState(2411);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,311,_ctx) ) {
				case 1:
					{
					setState(2402);
					match(FIELDS);
					setState(2403);
					match(TERMINATED);
					setState(2404);
					match(BY);
					setState(2405);
					((RowFormatDelimitedContext)_localctx).fieldsTerminatedBy = match(STRING);
					setState(2409);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,310,_ctx) ) {
					case 1:
						{
						setState(2406);
						match(ESCAPED);
						setState(2407);
						match(BY);
						setState(2408);
						((RowFormatDelimitedContext)_localctx).escapedBy = match(STRING);
						}
						break;
					}
					}
					break;
				}
				setState(2418);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,312,_ctx) ) {
				case 1:
					{
					setState(2413);
					match(COLLECTION);
					setState(2414);
					match(ITEMS);
					setState(2415);
					match(TERMINATED);
					setState(2416);
					match(BY);
					setState(2417);
					((RowFormatDelimitedContext)_localctx).collectionItemsTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2425);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,313,_ctx) ) {
				case 1:
					{
					setState(2420);
					match(MAP);
					setState(2421);
					match(KEYS);
					setState(2422);
					match(TERMINATED);
					setState(2423);
					match(BY);
					setState(2424);
					((RowFormatDelimitedContext)_localctx).keysTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2431);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,314,_ctx) ) {
				case 1:
					{
					setState(2427);
					match(LINES);
					setState(2428);
					match(TERMINATED);
					setState(2429);
					match(BY);
					setState(2430);
					((RowFormatDelimitedContext)_localctx).linesSeparatedBy = match(STRING);
					}
					break;
				}
				setState(2437);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,315,_ctx) ) {
				case 1:
					{
					setState(2433);
					match(NULL);
					setState(2434);
					match(DEFINED);
					setState(2435);
					match(AS);
					setState(2436);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public MultipartIdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultipartIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultipartIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultipartIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierListContext multipartIdentifierList() throws RecognitionException {
		MultipartIdentifierListContext _localctx = new MultipartIdentifierListContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_multipartIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2441);
			multipartIdentifier();
			setState(2446);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2442);
				match(COMMA);
				setState(2443);
				multipartIdentifier();
				}
				}
				setState(2448);
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
		public List<TerminalNode> DOT() { return getTokens(SqlBaseParserParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SqlBaseParserParser.DOT, i);
		}
		public MultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierContext multipartIdentifier() throws RecognitionException {
		MultipartIdentifierContext _localctx = new MultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_multipartIdentifier);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2449);
			((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
			setState(2454);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,318,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2450);
					match(DOT);
					setState(2451);
					((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2456);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public MultipartIdentifierPropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierPropertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultipartIdentifierPropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultipartIdentifierPropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultipartIdentifierPropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierPropertyListContext multipartIdentifierPropertyList() throws RecognitionException {
		MultipartIdentifierPropertyListContext _localctx = new MultipartIdentifierPropertyListContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_multipartIdentifierPropertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2457);
			multipartIdentifierProperty();
			setState(2462);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2458);
				match(COMMA);
				setState(2459);
				multipartIdentifierProperty();
				}
				}
				setState(2464);
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
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public PropertyListContext propertyList() {
			return getRuleContext(PropertyListContext.class,0);
		}
		public MultipartIdentifierPropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifierProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultipartIdentifierProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultipartIdentifierProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultipartIdentifierProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierPropertyContext multipartIdentifierProperty() throws RecognitionException {
		MultipartIdentifierPropertyContext _localctx = new MultipartIdentifierPropertyContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_multipartIdentifierProperty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2465);
			multipartIdentifier();
			setState(2468);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPTIONS) {
				{
				setState(2466);
				match(OPTIONS);
				setState(2467);
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
		public TerminalNode DOT() { return getToken(SqlBaseParserParser.DOT, 0); }
		public TableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTableIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTableIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTableIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableIdentifierContext tableIdentifier() throws RecognitionException {
		TableIdentifierContext _localctx = new TableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_tableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2473);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,321,_ctx) ) {
			case 1:
				{
				setState(2470);
				((TableIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2471);
				match(DOT);
				}
				break;
			}
			setState(2475);
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
		public TerminalNode DOT() { return getToken(SqlBaseParserParser.DOT, 0); }
		public FunctionIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFunctionIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFunctionIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFunctionIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionIdentifierContext functionIdentifier() throws RecognitionException {
		FunctionIdentifierContext _localctx = new FunctionIdentifierContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_functionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2480);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,322,_ctx) ) {
			case 1:
				{
				setState(2477);
				((FunctionIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2478);
				match(DOT);
				}
				break;
			}
			setState(2482);
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
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public NamedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNamedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNamedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNamedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedExpressionContext namedExpression() throws RecognitionException {
		NamedExpressionContext _localctx = new NamedExpressionContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_namedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2484);
			expression();
			setState(2492);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,325,_ctx) ) {
			case 1:
				{
				setState(2486);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,323,_ctx) ) {
				case 1:
					{
					setState(2485);
					match(AS);
					}
					break;
				}
				setState(2490);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,324,_ctx) ) {
				case 1:
					{
					setState(2488);
					((NamedExpressionContext)_localctx).name = errorCapturingIdentifier();
					}
					break;
				case 2:
					{
					setState(2489);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public NamedExpressionSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedExpressionSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNamedExpressionSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNamedExpressionSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNamedExpressionSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedExpressionSeqContext namedExpressionSeq() throws RecognitionException {
		NamedExpressionSeqContext _localctx = new NamedExpressionSeqContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_namedExpressionSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2494);
			namedExpression();
			setState(2499);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,326,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2495);
					match(COMMA);
					setState(2496);
					namedExpression();
					}
					} 
				}
				setState(2501);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<PartitionFieldContext> partitionField() {
			return getRuleContexts(PartitionFieldContext.class);
		}
		public PartitionFieldContext partitionField(int i) {
			return getRuleContext(PartitionFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public PartitionFieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionFieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPartitionFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPartitionFieldList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPartitionFieldList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionFieldListContext partitionFieldList() throws RecognitionException {
		PartitionFieldListContext _localctx = new PartitionFieldListContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_partitionFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2502);
			match(LEFT_PAREN);
			setState(2503);
			((PartitionFieldListContext)_localctx).partitionField = partitionField();
			((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
			setState(2508);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2504);
				match(COMMA);
				setState(2505);
				((PartitionFieldListContext)_localctx).partitionField = partitionField();
				((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
				}
				}
				setState(2510);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2511);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPartitionColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPartitionColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPartitionColumn(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPartitionTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPartitionTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPartitionTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionFieldContext partitionField() throws RecognitionException {
		PartitionFieldContext _localctx = new PartitionFieldContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_partitionField);
		try {
			setState(2515);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,328,_ctx) ) {
			case 1:
				_localctx = new PartitionTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2513);
				transform();
				}
				break;
			case 2:
				_localctx = new PartitionColumnContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2514);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIdentityTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIdentityTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIdentityTransform(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ApplyTransformContext extends TransformContext {
		public IdentifierContext transformName;
		public TransformArgumentContext transformArgument;
		public List<TransformArgumentContext> argument = new ArrayList<TransformArgumentContext>();
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TransformArgumentContext> transformArgument() {
			return getRuleContexts(TransformArgumentContext.class);
		}
		public TransformArgumentContext transformArgument(int i) {
			return getRuleContext(TransformArgumentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public ApplyTransformContext(TransformContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterApplyTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitApplyTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitApplyTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformContext transform() throws RecognitionException {
		TransformContext _localctx = new TransformContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_transform);
		int _la;
		try {
			setState(2530);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,330,_ctx) ) {
			case 1:
				_localctx = new IdentityTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2517);
				qualifiedName();
				}
				break;
			case 2:
				_localctx = new ApplyTransformContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2518);
				((ApplyTransformContext)_localctx).transformName = identifier();
				setState(2519);
				match(LEFT_PAREN);
				setState(2520);
				((ApplyTransformContext)_localctx).transformArgument = transformArgument();
				((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
				setState(2525);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2521);
					match(COMMA);
					setState(2522);
					((ApplyTransformContext)_localctx).transformArgument = transformArgument();
					((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
					}
					}
					setState(2527);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2528);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTransformArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTransformArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTransformArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformArgumentContext transformArgument() throws RecognitionException {
		TransformArgumentContext _localctx = new TransformArgumentContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_transformArgument);
		try {
			setState(2534);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,331,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2532);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2533);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2536);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public ExpressionSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionSeq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterExpressionSeq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitExpressionSeq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitExpressionSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionSeqContext expressionSeq() throws RecognitionException {
		ExpressionSeqContext _localctx = new ExpressionSeqContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_expressionSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2538);
			expression();
			setState(2543);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2539);
				match(COMMA);
				setState(2540);
				expression();
				}
				}
				setState(2545);
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
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public LogicalNotContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLogicalNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLogicalNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLogicalNot(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPredicated(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPredicated(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPredicated(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExistsContext extends BooleanExpressionContext {
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public ExistsContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitExists(this);
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
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public LogicalBinaryContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLogicalBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLogicalBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLogicalBinary(this);
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
		int _startState = 210;
		enterRecursionRule(_localctx, 210, RULE_booleanExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2558);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,334,_ctx) ) {
			case 1:
				{
				_localctx = new LogicalNotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2547);
				match(NOT);
				setState(2548);
				booleanExpression(5);
				}
				break;
			case 2:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2549);
				match(EXISTS);
				setState(2550);
				match(LEFT_PAREN);
				setState(2551);
				query();
				setState(2552);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new PredicatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2554);
				valueExpression(0);
				setState(2556);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,333,_ctx) ) {
				case 1:
					{
					setState(2555);
					predicate();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2568);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,336,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2566);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,335,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2560);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2561);
						((LogicalBinaryContext)_localctx).operator = match(AND);
						setState(2562);
						((LogicalBinaryContext)_localctx).right = booleanExpression(3);
						}
						break;
					case 2:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2563);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2564);
						((LogicalBinaryContext)_localctx).operator = match(OR);
						setState(2565);
						((LogicalBinaryContext)_localctx).right = booleanExpression(2);
						}
						break;
					}
					} 
				}
				setState(2570);
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
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public TerminalNode BETWEEN() { return getToken(SqlBaseParserParser.BETWEEN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RLIKE() { return getToken(SqlBaseParserParser.RLIKE, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(SqlBaseParserParser.ILIKE, 0); }
		public TerminalNode ANY() { return getToken(SqlBaseParserParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(SqlBaseParserParser.SOME, 0); }
		public TerminalNode ALL() { return getToken(SqlBaseParserParser.ALL, 0); }
		public TerminalNode ESCAPE() { return getToken(SqlBaseParserParser.ESCAPE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode IS() { return getToken(SqlBaseParserParser.IS, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public TerminalNode TRUE() { return getToken(SqlBaseParserParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(SqlBaseParserParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(SqlBaseParserParser.UNKNOWN, 0); }
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode DISTINCT() { return getToken(SqlBaseParserParser.DISTINCT, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_predicate);
		int _la;
		try {
			setState(2653);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,350,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2572);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2571);
					match(NOT);
					}
				}

				setState(2574);
				((PredicateContext)_localctx).kind = match(BETWEEN);
				setState(2575);
				((PredicateContext)_localctx).lower = valueExpression(0);
				setState(2576);
				match(AND);
				setState(2577);
				((PredicateContext)_localctx).upper = valueExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2580);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2579);
					match(NOT);
					}
				}

				setState(2582);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2583);
				match(LEFT_PAREN);
				setState(2584);
				expression();
				setState(2589);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2585);
					match(COMMA);
					setState(2586);
					expression();
					}
					}
					setState(2591);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2592);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2595);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2594);
					match(NOT);
					}
				}

				setState(2597);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2598);
				match(LEFT_PAREN);
				setState(2599);
				query();
				setState(2600);
				match(RIGHT_PAREN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2603);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2602);
					match(NOT);
					}
				}

				setState(2605);
				((PredicateContext)_localctx).kind = match(RLIKE);
				setState(2606);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2608);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2607);
					match(NOT);
					}
				}

				setState(2610);
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
				setState(2611);
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
				setState(2625);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,344,_ctx) ) {
				case 1:
					{
					setState(2612);
					match(LEFT_PAREN);
					setState(2613);
					match(RIGHT_PAREN);
					}
					break;
				case 2:
					{
					setState(2614);
					match(LEFT_PAREN);
					setState(2615);
					expression();
					setState(2620);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2616);
						match(COMMA);
						setState(2617);
						expression();
						}
						}
						setState(2622);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2623);
					match(RIGHT_PAREN);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2628);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2627);
					match(NOT);
					}
				}

				setState(2630);
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
				setState(2631);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				setState(2634);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,346,_ctx) ) {
				case 1:
					{
					setState(2632);
					match(ESCAPE);
					setState(2633);
					((PredicateContext)_localctx).escapeChar = match(STRING);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2636);
				match(IS);
				setState(2638);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2637);
					match(NOT);
					}
				}

				setState(2640);
				((PredicateContext)_localctx).kind = match(NULL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2641);
				match(IS);
				setState(2643);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2642);
					match(NOT);
					}
				}

				setState(2645);
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
				setState(2646);
				match(IS);
				setState(2648);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2647);
					match(NOT);
					}
				}

				setState(2650);
				((PredicateContext)_localctx).kind = match(DISTINCT);
				setState(2651);
				match(FROM);
				setState(2652);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterValueExpressionDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitValueExpressionDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitValueExpressionDefault(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitComparison(this);
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
		public TerminalNode ASTERISK() { return getToken(SqlBaseParserParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(SqlBaseParserParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(SqlBaseParserParser.PERCENT, 0); }
		public TerminalNode DIV() { return getToken(SqlBaseParserParser.DIV, 0); }
		public TerminalNode PLUS() { return getToken(SqlBaseParserParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public TerminalNode CONCAT_PIPE() { return getToken(SqlBaseParserParser.CONCAT_PIPE, 0); }
		public TerminalNode AMPERSAND() { return getToken(SqlBaseParserParser.AMPERSAND, 0); }
		public TerminalNode HAT() { return getToken(SqlBaseParserParser.HAT, 0); }
		public TerminalNode PIPE() { return getToken(SqlBaseParserParser.PIPE, 0); }
		public ArithmeticBinaryContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterArithmeticBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitArithmeticBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitArithmeticBinary(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ArithmeticUnaryContext extends ValueExpressionContext {
		public Token operator;
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(SqlBaseParserParser.PLUS, 0); }
		public TerminalNode TILDE() { return getToken(SqlBaseParserParser.TILDE, 0); }
		public ArithmeticUnaryContext(ValueExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterArithmeticUnary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitArithmeticUnary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitArithmeticUnary(this);
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
		int _startState = 214;
		enterRecursionRule(_localctx, 214, RULE_valueExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2659);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,351,_ctx) ) {
			case 1:
				{
				_localctx = new ValueExpressionDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2656);
				primaryExpression(0);
				}
				break;
			case 2:
				{
				_localctx = new ArithmeticUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2657);
				((ArithmeticUnaryContext)_localctx).operator = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 295)) & ~0x3f) == 0 && ((1L << (_la - 295)) & ((1L << (PLUS - 295)) | (1L << (MINUS - 295)) | (1L << (TILDE - 295)))) != 0)) ) {
					((ArithmeticUnaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2658);
				valueExpression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2682);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,353,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2680);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,352,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2661);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2662);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==DIV || ((((_la - 297)) & ~0x3f) == 0 && ((1L << (_la - 297)) & ((1L << (ASTERISK - 297)) | (1L << (SLASH - 297)) | (1L << (PERCENT - 297)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2663);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(7);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2664);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(2665);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 295)) & ~0x3f) == 0 && ((1L << (_la - 295)) & ((1L << (PLUS - 295)) | (1L << (MINUS - 295)) | (1L << (CONCAT_PIPE - 295)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2666);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(6);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2667);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(2668);
						((ArithmeticBinaryContext)_localctx).operator = match(AMPERSAND);
						setState(2669);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(5);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2670);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(2671);
						((ArithmeticBinaryContext)_localctx).operator = match(HAT);
						setState(2672);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(4);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2673);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2674);
						((ArithmeticBinaryContext)_localctx).operator = match(PIPE);
						setState(2675);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(3);
						}
						break;
					case 6:
						{
						_localctx = new ComparisonContext(new ValueExpressionContext(_parentctx, _parentState));
						((ComparisonContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2676);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2677);
						comparisonOperator();
						setState(2678);
						((ComparisonContext)_localctx).right = valueExpression(2);
						}
						break;
					}
					} 
				}
				setState(2684);
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
		public TerminalNode YEAR() { return getToken(SqlBaseParserParser.YEAR, 0); }
		public TerminalNode QUARTER() { return getToken(SqlBaseParserParser.QUARTER, 0); }
		public TerminalNode MONTH() { return getToken(SqlBaseParserParser.MONTH, 0); }
		public TerminalNode WEEK() { return getToken(SqlBaseParserParser.WEEK, 0); }
		public TerminalNode DAY() { return getToken(SqlBaseParserParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(SqlBaseParserParser.DAYOFYEAR, 0); }
		public TerminalNode HOUR() { return getToken(SqlBaseParserParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(SqlBaseParserParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(SqlBaseParserParser.SECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(SqlBaseParserParser.MILLISECOND, 0); }
		public TerminalNode MICROSECOND() { return getToken(SqlBaseParserParser.MICROSECOND, 0); }
		public DatetimeUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datetimeUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDatetimeUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDatetimeUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDatetimeUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatetimeUnitContext datetimeUnit() throws RecognitionException {
		DatetimeUnitContext _localctx = new DatetimeUnitContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_datetimeUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2685);
			_la = _input.LA(1);
			if ( !(_la==DAY || _la==DAYOFYEAR || ((((_la - 111)) & ~0x3f) == 0 && ((1L << (_la - 111)) & ((1L << (HOUR - 111)) | (1L << (MICROSECOND - 111)) | (1L << (MILLISECOND - 111)) | (1L << (MINUTE - 111)) | (1L << (MONTH - 111)))) != 0) || _la==QUARTER || _la==SECOND || _la==WEEK || _la==YEAR) ) {
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
		public TerminalNode STRUCT() { return getToken(SqlBaseParserParser.STRUCT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public StructContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitStruct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitStruct(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DereferenceContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext base;
		public IdentifierContext fieldName;
		public TerminalNode DOT() { return getToken(SqlBaseParserParser.DOT, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DereferenceContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDereference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDereference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDereference(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TimestampaddContext extends PrimaryExpressionContext {
		public Token name;
		public DatetimeUnitContext unit;
		public ValueExpressionContext unitsAmount;
		public ValueExpressionContext timestamp;
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public DatetimeUnitContext datetimeUnit() {
			return getRuleContext(DatetimeUnitContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode TIMESTAMPADD() { return getToken(SqlBaseParserParser.TIMESTAMPADD, 0); }
		public TerminalNode DATEADD() { return getToken(SqlBaseParserParser.DATEADD, 0); }
		public TimestampaddContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTimestampadd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTimestampadd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTimestampadd(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubstringContext extends PrimaryExpressionContext {
		public ValueExpressionContext str;
		public ValueExpressionContext pos;
		public ValueExpressionContext len;
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode SUBSTR() { return getToken(SqlBaseParserParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(SqlBaseParserParser.SUBSTRING, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public TerminalNode FOR() { return getToken(SqlBaseParserParser.FOR, 0); }
		public SubstringContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSubstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSubstring(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSubstring(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CastContext extends PrimaryExpressionContext {
		public Token name;
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode CAST() { return getToken(SqlBaseParserParser.CAST, 0); }
		public TerminalNode TRY_CAST() { return getToken(SqlBaseParserParser.TRY_CAST, 0); }
		public CastContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCast(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCast(this);
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
		public TerminalNode ARROW() { return getToken(SqlBaseParserParser.ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public LambdaContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesizedExpressionContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public ParenthesizedExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TrimContext extends PrimaryExpressionContext {
		public Token trimOption;
		public ValueExpressionContext trimStr;
		public ValueExpressionContext srcStr;
		public TerminalNode TRIM() { return getToken(SqlBaseParserParser.TRIM, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode BOTH() { return getToken(SqlBaseParserParser.BOTH, 0); }
		public TerminalNode LEADING() { return getToken(SqlBaseParserParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(SqlBaseParserParser.TRAILING, 0); }
		public TrimContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTrim(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTrim(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTrim(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SimpleCaseContext extends PrimaryExpressionContext {
		public ExpressionContext value;
		public ExpressionContext elseExpression;
		public TerminalNode CASE() { return getToken(SqlBaseParserParser.CASE, 0); }
		public TerminalNode END() { return getToken(SqlBaseParserParser.END, 0); }
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
		public TerminalNode ELSE() { return getToken(SqlBaseParserParser.ELSE, 0); }
		public SimpleCaseContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSimpleCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSimpleCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSimpleCase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CurrentLikeContext extends PrimaryExpressionContext {
		public Token name;
		public TerminalNode CURRENT_DATE() { return getToken(SqlBaseParserParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(SqlBaseParserParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(SqlBaseParserParser.CURRENT_USER, 0); }
		public CurrentLikeContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterCurrentLike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitCurrentLike(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitCurrentLike(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterColumnReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitColumnReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitColumnReference(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RowConstructorContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public RowConstructorContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRowConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRowConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRowConstructor(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LastContext extends PrimaryExpressionContext {
		public TerminalNode LAST() { return getToken(SqlBaseParserParser.LAST, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode IGNORE() { return getToken(SqlBaseParserParser.IGNORE, 0); }
		public TerminalNode NULLS() { return getToken(SqlBaseParserParser.NULLS, 0); }
		public LastContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLast(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLast(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StarContext extends PrimaryExpressionContext {
		public TerminalNode ASTERISK() { return getToken(SqlBaseParserParser.ASTERISK, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SqlBaseParserParser.DOT, 0); }
		public StarContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterStar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitStar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitStar(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OverlayContext extends PrimaryExpressionContext {
		public ValueExpressionContext input;
		public ValueExpressionContext replace;
		public ValueExpressionContext position;
		public ValueExpressionContext length;
		public TerminalNode OVERLAY() { return getToken(SqlBaseParserParser.OVERLAY, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode PLACING() { return getToken(SqlBaseParserParser.PLACING, 0); }
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FOR() { return getToken(SqlBaseParserParser.FOR, 0); }
		public OverlayContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterOverlay(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitOverlay(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitOverlay(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubscriptContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext value;
		public ValueExpressionContext index;
		public TerminalNode LEFT_BRACKET() { return getToken(SqlBaseParserParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(SqlBaseParserParser.RIGHT_BRACKET, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public SubscriptContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TimestampdiffContext extends PrimaryExpressionContext {
		public Token name;
		public DatetimeUnitContext unit;
		public ValueExpressionContext startTimestamp;
		public ValueExpressionContext endTimestamp;
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public DatetimeUnitContext datetimeUnit() {
			return getRuleContext(DatetimeUnitContext.class,0);
		}
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode TIMESTAMPDIFF() { return getToken(SqlBaseParserParser.TIMESTAMPDIFF, 0); }
		public TerminalNode DATEDIFF() { return getToken(SqlBaseParserParser.DATEDIFF, 0); }
		public TimestampdiffContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTimestampdiff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTimestampdiff(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTimestampdiff(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubqueryExpressionContext extends PrimaryExpressionContext {
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public SubqueryExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSubqueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSubqueryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSubqueryExpression(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterConstantDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitConstantDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitConstantDefault(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExtractContext extends PrimaryExpressionContext {
		public IdentifierContext field;
		public ValueExpressionContext source;
		public TerminalNode EXTRACT() { return getToken(SqlBaseParserParser.EXTRACT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public ExtractContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterExtract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitExtract(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitExtract(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PercentileContext extends PrimaryExpressionContext {
		public Token name;
		public ValueExpressionContext percentage;
		public List<TerminalNode> LEFT_PAREN() { return getTokens(SqlBaseParserParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(SqlBaseParserParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(SqlBaseParserParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(SqlBaseParserParser.RIGHT_PAREN, i);
		}
		public TerminalNode WITHIN() { return getToken(SqlBaseParserParser.WITHIN, 0); }
		public TerminalNode GROUP() { return getToken(SqlBaseParserParser.GROUP, 0); }
		public TerminalNode ORDER() { return getToken(SqlBaseParserParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public SortItemContext sortItem() {
			return getRuleContext(SortItemContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode PERCENTILE_CONT() { return getToken(SqlBaseParserParser.PERCENTILE_CONT, 0); }
		public TerminalNode PERCENTILE_DISC() { return getToken(SqlBaseParserParser.PERCENTILE_DISC, 0); }
		public TerminalNode OVER() { return getToken(SqlBaseParserParser.OVER, 0); }
		public WindowSpecContext windowSpec() {
			return getRuleContext(WindowSpecContext.class,0);
		}
		public PercentileContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPercentile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPercentile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPercentile(this);
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
		public List<TerminalNode> LEFT_PAREN() { return getTokens(SqlBaseParserParser.LEFT_PAREN); }
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(SqlBaseParserParser.LEFT_PAREN, i);
		}
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(SqlBaseParserParser.RIGHT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(SqlBaseParserParser.RIGHT_PAREN, i);
		}
		public TerminalNode FILTER() { return getToken(SqlBaseParserParser.FILTER, 0); }
		public TerminalNode WHERE() { return getToken(SqlBaseParserParser.WHERE, 0); }
		public TerminalNode NULLS() { return getToken(SqlBaseParserParser.NULLS, 0); }
		public TerminalNode OVER() { return getToken(SqlBaseParserParser.OVER, 0); }
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
		public TerminalNode IGNORE() { return getToken(SqlBaseParserParser.IGNORE, 0); }
		public TerminalNode RESPECT() { return getToken(SqlBaseParserParser.RESPECT, 0); }
		public SetQuantifierContext setQuantifier() {
			return getRuleContext(SetQuantifierContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public FunctionCallContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SearchedCaseContext extends PrimaryExpressionContext {
		public ExpressionContext elseExpression;
		public TerminalNode CASE() { return getToken(SqlBaseParserParser.CASE, 0); }
		public TerminalNode END() { return getToken(SqlBaseParserParser.END, 0); }
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(SqlBaseParserParser.ELSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SearchedCaseContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSearchedCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSearchedCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSearchedCase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PositionContext extends PrimaryExpressionContext {
		public ValueExpressionContext substr;
		public ValueExpressionContext str;
		public TerminalNode POSITION() { return getToken(SqlBaseParserParser.POSITION, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public PositionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPosition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FirstContext extends PrimaryExpressionContext {
		public TerminalNode FIRST() { return getToken(SqlBaseParserParser.FIRST, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode IGNORE() { return getToken(SqlBaseParserParser.IGNORE, 0); }
		public TerminalNode NULLS() { return getToken(SqlBaseParserParser.NULLS, 0); }
		public FirstContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFirst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFirst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFirst(this);
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
		int _startState = 218;
		enterRecursionRule(_localctx, 218, RULE_primaryExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2908);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,375,_ctx) ) {
			case 1:
				{
				_localctx = new CurrentLikeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2688);
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
				setState(2689);
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
				setState(2690);
				match(LEFT_PAREN);
				setState(2691);
				((TimestampaddContext)_localctx).unit = datetimeUnit();
				setState(2692);
				match(COMMA);
				setState(2693);
				((TimestampaddContext)_localctx).unitsAmount = valueExpression(0);
				setState(2694);
				match(COMMA);
				setState(2695);
				((TimestampaddContext)_localctx).timestamp = valueExpression(0);
				setState(2696);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				{
				_localctx = new TimestampdiffContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2698);
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
				setState(2699);
				match(LEFT_PAREN);
				setState(2700);
				((TimestampdiffContext)_localctx).unit = datetimeUnit();
				setState(2701);
				match(COMMA);
				setState(2702);
				((TimestampdiffContext)_localctx).startTimestamp = valueExpression(0);
				setState(2703);
				match(COMMA);
				setState(2704);
				((TimestampdiffContext)_localctx).endTimestamp = valueExpression(0);
				setState(2705);
				match(RIGHT_PAREN);
				}
				break;
			case 4:
				{
				_localctx = new SearchedCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2707);
				match(CASE);
				setState(2709); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2708);
					whenClause();
					}
					}
					setState(2711); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2715);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2713);
					match(ELSE);
					setState(2714);
					((SearchedCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2717);
				match(END);
				}
				break;
			case 5:
				{
				_localctx = new SimpleCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2719);
				match(CASE);
				setState(2720);
				((SimpleCaseContext)_localctx).value = expression();
				setState(2722); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2721);
					whenClause();
					}
					}
					setState(2724); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2728);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2726);
					match(ELSE);
					setState(2727);
					((SimpleCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2730);
				match(END);
				}
				break;
			case 6:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2732);
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
				setState(2733);
				match(LEFT_PAREN);
				setState(2734);
				expression();
				setState(2735);
				match(AS);
				setState(2736);
				dataType();
				setState(2737);
				match(RIGHT_PAREN);
				}
				break;
			case 7:
				{
				_localctx = new StructContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2739);
				match(STRUCT);
				setState(2740);
				match(LEFT_PAREN);
				setState(2749);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,359,_ctx) ) {
				case 1:
					{
					setState(2741);
					((StructContext)_localctx).namedExpression = namedExpression();
					((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
					setState(2746);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2742);
						match(COMMA);
						setState(2743);
						((StructContext)_localctx).namedExpression = namedExpression();
						((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
						}
						}
						setState(2748);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2751);
				match(RIGHT_PAREN);
				}
				break;
			case 8:
				{
				_localctx = new FirstContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2752);
				match(FIRST);
				setState(2753);
				match(LEFT_PAREN);
				setState(2754);
				expression();
				setState(2757);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2755);
					match(IGNORE);
					setState(2756);
					match(NULLS);
					}
				}

				setState(2759);
				match(RIGHT_PAREN);
				}
				break;
			case 9:
				{
				_localctx = new LastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2761);
				match(LAST);
				setState(2762);
				match(LEFT_PAREN);
				setState(2763);
				expression();
				setState(2766);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2764);
					match(IGNORE);
					setState(2765);
					match(NULLS);
					}
				}

				setState(2768);
				match(RIGHT_PAREN);
				}
				break;
			case 10:
				{
				_localctx = new PositionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2770);
				match(POSITION);
				setState(2771);
				match(LEFT_PAREN);
				setState(2772);
				((PositionContext)_localctx).substr = valueExpression(0);
				setState(2773);
				match(IN);
				setState(2774);
				((PositionContext)_localctx).str = valueExpression(0);
				setState(2775);
				match(RIGHT_PAREN);
				}
				break;
			case 11:
				{
				_localctx = new ConstantDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2777);
				constant();
				}
				break;
			case 12:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2778);
				match(ASTERISK);
				}
				break;
			case 13:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2779);
				qualifiedName();
				setState(2780);
				match(DOT);
				setState(2781);
				match(ASTERISK);
				}
				break;
			case 14:
				{
				_localctx = new RowConstructorContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2783);
				match(LEFT_PAREN);
				setState(2784);
				namedExpression();
				setState(2787); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2785);
					match(COMMA);
					setState(2786);
					namedExpression();
					}
					}
					setState(2789); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(2791);
				match(RIGHT_PAREN);
				}
				break;
			case 15:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2793);
				match(LEFT_PAREN);
				setState(2794);
				query();
				setState(2795);
				match(RIGHT_PAREN);
				}
				break;
			case 16:
				{
				_localctx = new FunctionCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2797);
				functionName();
				setState(2798);
				match(LEFT_PAREN);
				setState(2810);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,365,_ctx) ) {
				case 1:
					{
					setState(2800);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,363,_ctx) ) {
					case 1:
						{
						setState(2799);
						setQuantifier();
						}
						break;
					}
					setState(2802);
					((FunctionCallContext)_localctx).expression = expression();
					((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
					setState(2807);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(2803);
						match(COMMA);
						setState(2804);
						((FunctionCallContext)_localctx).expression = expression();
						((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
						}
						}
						setState(2809);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2812);
				match(RIGHT_PAREN);
				setState(2819);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,366,_ctx) ) {
				case 1:
					{
					setState(2813);
					match(FILTER);
					setState(2814);
					match(LEFT_PAREN);
					setState(2815);
					match(WHERE);
					setState(2816);
					((FunctionCallContext)_localctx).where = booleanExpression(0);
					setState(2817);
					match(RIGHT_PAREN);
					}
					break;
				}
				setState(2823);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,367,_ctx) ) {
				case 1:
					{
					setState(2821);
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
					setState(2822);
					match(NULLS);
					}
					break;
				}
				setState(2827);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,368,_ctx) ) {
				case 1:
					{
					setState(2825);
					match(OVER);
					setState(2826);
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
				setState(2829);
				identifier();
				setState(2830);
				match(ARROW);
				setState(2831);
				expression();
				}
				break;
			case 18:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2833);
				match(LEFT_PAREN);
				setState(2834);
				identifier();
				setState(2837); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2835);
					match(COMMA);
					setState(2836);
					identifier();
					}
					}
					setState(2839); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(2841);
				match(RIGHT_PAREN);
				setState(2842);
				match(ARROW);
				setState(2843);
				expression();
				}
				break;
			case 19:
				{
				_localctx = new ColumnReferenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2845);
				identifier();
				}
				break;
			case 20:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2846);
				match(LEFT_PAREN);
				setState(2847);
				expression();
				setState(2848);
				match(RIGHT_PAREN);
				}
				break;
			case 21:
				{
				_localctx = new ExtractContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2850);
				match(EXTRACT);
				setState(2851);
				match(LEFT_PAREN);
				setState(2852);
				((ExtractContext)_localctx).field = identifier();
				setState(2853);
				match(FROM);
				setState(2854);
				((ExtractContext)_localctx).source = valueExpression(0);
				setState(2855);
				match(RIGHT_PAREN);
				}
				break;
			case 22:
				{
				_localctx = new SubstringContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2857);
				_la = _input.LA(1);
				if ( !(_la==SUBSTR || _la==SUBSTRING) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2858);
				match(LEFT_PAREN);
				setState(2859);
				((SubstringContext)_localctx).str = valueExpression(0);
				setState(2860);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==FROM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2861);
				((SubstringContext)_localctx).pos = valueExpression(0);
				setState(2864);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA || _la==FOR) {
					{
					setState(2862);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==FOR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2863);
					((SubstringContext)_localctx).len = valueExpression(0);
					}
				}

				setState(2866);
				match(RIGHT_PAREN);
				}
				break;
			case 23:
				{
				_localctx = new TrimContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2868);
				match(TRIM);
				setState(2869);
				match(LEFT_PAREN);
				setState(2871);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,371,_ctx) ) {
				case 1:
					{
					setState(2870);
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
				setState(2874);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,372,_ctx) ) {
				case 1:
					{
					setState(2873);
					((TrimContext)_localctx).trimStr = valueExpression(0);
					}
					break;
				}
				setState(2876);
				match(FROM);
				setState(2877);
				((TrimContext)_localctx).srcStr = valueExpression(0);
				setState(2878);
				match(RIGHT_PAREN);
				}
				break;
			case 24:
				{
				_localctx = new OverlayContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2880);
				match(OVERLAY);
				setState(2881);
				match(LEFT_PAREN);
				setState(2882);
				((OverlayContext)_localctx).input = valueExpression(0);
				setState(2883);
				match(PLACING);
				setState(2884);
				((OverlayContext)_localctx).replace = valueExpression(0);
				setState(2885);
				match(FROM);
				setState(2886);
				((OverlayContext)_localctx).position = valueExpression(0);
				setState(2889);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2887);
					match(FOR);
					setState(2888);
					((OverlayContext)_localctx).length = valueExpression(0);
					}
				}

				setState(2891);
				match(RIGHT_PAREN);
				}
				break;
			case 25:
				{
				_localctx = new PercentileContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2893);
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
				setState(2894);
				match(LEFT_PAREN);
				setState(2895);
				((PercentileContext)_localctx).percentage = valueExpression(0);
				setState(2896);
				match(RIGHT_PAREN);
				setState(2897);
				match(WITHIN);
				setState(2898);
				match(GROUP);
				setState(2899);
				match(LEFT_PAREN);
				setState(2900);
				match(ORDER);
				setState(2901);
				match(BY);
				setState(2902);
				sortItem();
				setState(2903);
				match(RIGHT_PAREN);
				setState(2906);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,374,_ctx) ) {
				case 1:
					{
					setState(2904);
					match(OVER);
					setState(2905);
					windowSpec();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2920);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,377,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2918);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,376,_ctx) ) {
					case 1:
						{
						_localctx = new SubscriptContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((SubscriptContext)_localctx).value = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2910);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(2911);
						match(LEFT_BRACKET);
						setState(2912);
						((SubscriptContext)_localctx).index = valueExpression(0);
						setState(2913);
						match(RIGHT_BRACKET);
						}
						break;
					case 2:
						{
						_localctx = new DereferenceContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((DereferenceContext)_localctx).base = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2915);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(2916);
						match(DOT);
						setState(2917);
						((DereferenceContext)_localctx).fieldName = identifier();
						}
						break;
					}
					} 
				}
				setState(2922);
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
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public NullLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNullLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNullLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNullLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringLiteralContext extends ConstantContext {
		public List<TerminalNode> STRING() { return getTokens(SqlBaseParserParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(SqlBaseParserParser.STRING, i);
		}
		public StringLiteralContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TypeConstructorContext extends ConstantContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TypeConstructorContext(ConstantContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTypeConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTypeConstructor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTypeConstructor(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIntervalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIntervalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIntervalLiteral(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNumericLiteral(this);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_constant);
		try {
			int _alt;
			setState(2935);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,379,_ctx) ) {
			case 1:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2923);
				match(NULL);
				}
				break;
			case 2:
				_localctx = new IntervalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2924);
				interval();
				}
				break;
			case 3:
				_localctx = new TypeConstructorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2925);
				identifier();
				setState(2926);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new NumericLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2928);
				number();
				}
				break;
			case 5:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2929);
				booleanValue();
				}
				break;
			case 6:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2931); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2930);
						match(STRING);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2933); 
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
		public TerminalNode EQ() { return getToken(SqlBaseParserParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(SqlBaseParserParser.NEQ, 0); }
		public TerminalNode NEQJ() { return getToken(SqlBaseParserParser.NEQJ, 0); }
		public TerminalNode LT() { return getToken(SqlBaseParserParser.LT, 0); }
		public TerminalNode LTE() { return getToken(SqlBaseParserParser.LTE, 0); }
		public TerminalNode GT() { return getToken(SqlBaseParserParser.GT, 0); }
		public TerminalNode GTE() { return getToken(SqlBaseParserParser.GTE, 0); }
		public TerminalNode NSEQ() { return getToken(SqlBaseParserParser.NSEQ, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2937);
			_la = _input.LA(1);
			if ( !(((((_la - 287)) & ~0x3f) == 0 && ((1L << (_la - 287)) & ((1L << (EQ - 287)) | (1L << (NSEQ - 287)) | (1L << (NEQ - 287)) | (1L << (NEQJ - 287)) | (1L << (LT - 287)) | (1L << (LTE - 287)) | (1L << (GT - 287)) | (1L << (GTE - 287)))) != 0)) ) {
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
		public TerminalNode PLUS() { return getToken(SqlBaseParserParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public TerminalNode ASTERISK() { return getToken(SqlBaseParserParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(SqlBaseParserParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(SqlBaseParserParser.PERCENT, 0); }
		public TerminalNode DIV() { return getToken(SqlBaseParserParser.DIV, 0); }
		public TerminalNode TILDE() { return getToken(SqlBaseParserParser.TILDE, 0); }
		public TerminalNode AMPERSAND() { return getToken(SqlBaseParserParser.AMPERSAND, 0); }
		public TerminalNode PIPE() { return getToken(SqlBaseParserParser.PIPE, 0); }
		public TerminalNode CONCAT_PIPE() { return getToken(SqlBaseParserParser.CONCAT_PIPE, 0); }
		public TerminalNode HAT() { return getToken(SqlBaseParserParser.HAT, 0); }
		public ArithmeticOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arithmeticOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterArithmeticOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitArithmeticOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitArithmeticOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArithmeticOperatorContext arithmeticOperator() throws RecognitionException {
		ArithmeticOperatorContext _localctx = new ArithmeticOperatorContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_arithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2939);
			_la = _input.LA(1);
			if ( !(_la==DIV || ((((_la - 295)) & ~0x3f) == 0 && ((1L << (_la - 295)) & ((1L << (PLUS - 295)) | (1L << (MINUS - 295)) | (1L << (ASTERISK - 295)) | (1L << (SLASH - 295)) | (1L << (PERCENT - 295)) | (1L << (TILDE - 295)) | (1L << (AMPERSAND - 295)) | (1L << (PIPE - 295)) | (1L << (CONCAT_PIPE - 295)) | (1L << (HAT - 295)))) != 0)) ) {
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
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public PredicateOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPredicateOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPredicateOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPredicateOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateOperatorContext predicateOperator() throws RecognitionException {
		PredicateOperatorContext _localctx = new PredicateOperatorContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_predicateOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2941);
			_la = _input.LA(1);
			if ( !(_la==AND || ((((_la - 115)) & ~0x3f) == 0 && ((1L << (_la - 115)) & ((1L << (IN - 115)) | (1L << (NOT - 115)) | (1L << (OR - 115)))) != 0)) ) {
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
		public TerminalNode TRUE() { return getToken(SqlBaseParserParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(SqlBaseParserParser.FALSE, 0); }
		public BooleanValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterBooleanValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitBooleanValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitBooleanValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanValueContext booleanValue() throws RecognitionException {
		BooleanValueContext _localctx = new BooleanValueContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_booleanValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2943);
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
		public TerminalNode INTERVAL() { return getToken(SqlBaseParserParser.INTERVAL, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2945);
			match(INTERVAL);
			setState(2948);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,380,_ctx) ) {
			case 1:
				{
				setState(2946);
				errorCapturingMultiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2947);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterErrorCapturingMultiUnitsInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitErrorCapturingMultiUnitsInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitErrorCapturingMultiUnitsInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingMultiUnitsIntervalContext errorCapturingMultiUnitsInterval() throws RecognitionException {
		ErrorCapturingMultiUnitsIntervalContext _localctx = new ErrorCapturingMultiUnitsIntervalContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_errorCapturingMultiUnitsInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2950);
			((ErrorCapturingMultiUnitsIntervalContext)_localctx).body = multiUnitsInterval();
			setState(2952);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,381,_ctx) ) {
			case 1:
				{
				setState(2951);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterMultiUnitsInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitMultiUnitsInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitMultiUnitsInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiUnitsIntervalContext multiUnitsInterval() throws RecognitionException {
		MultiUnitsIntervalContext _localctx = new MultiUnitsIntervalContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_multiUnitsInterval);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2957); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2954);
					intervalValue();
					setState(2955);
					((MultiUnitsIntervalContext)_localctx).identifier = identifier();
					((MultiUnitsIntervalContext)_localctx).unit.add(((MultiUnitsIntervalContext)_localctx).identifier);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2959); 
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterErrorCapturingUnitToUnitInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitErrorCapturingUnitToUnitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitErrorCapturingUnitToUnitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingUnitToUnitIntervalContext errorCapturingUnitToUnitInterval() throws RecognitionException {
		ErrorCapturingUnitToUnitIntervalContext _localctx = new ErrorCapturingUnitToUnitIntervalContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_errorCapturingUnitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2961);
			((ErrorCapturingUnitToUnitIntervalContext)_localctx).body = unitToUnitInterval();
			setState(2964);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,383,_ctx) ) {
			case 1:
				{
				setState(2962);
				((ErrorCapturingUnitToUnitIntervalContext)_localctx).error1 = multiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2963);
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
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUnitToUnitInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUnitToUnitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUnitToUnitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnitToUnitIntervalContext unitToUnitInterval() throws RecognitionException {
		UnitToUnitIntervalContext _localctx = new UnitToUnitIntervalContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_unitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2966);
			((UnitToUnitIntervalContext)_localctx).value = intervalValue();
			setState(2967);
			((UnitToUnitIntervalContext)_localctx).from = identifier();
			setState(2968);
			match(TO);
			setState(2969);
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
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(SqlBaseParserParser.DECIMAL_VALUE, 0); }
		public TerminalNode STRING() { return getToken(SqlBaseParserParser.STRING, 0); }
		public TerminalNode PLUS() { return getToken(SqlBaseParserParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public IntervalValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIntervalValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIntervalValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIntervalValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalValueContext intervalValue() throws RecognitionException {
		IntervalValueContext _localctx = new IntervalValueContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_intervalValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2972);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(2971);
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

			setState(2974);
			_la = _input.LA(1);
			if ( !(((((_la - 309)) & ~0x3f) == 0 && ((1L << (_la - 309)) & ((1L << (STRING - 309)) | (1L << (INTEGER_VALUE - 309)) | (1L << (DECIMAL_VALUE - 309)))) != 0)) ) {
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
		public TerminalNode FIRST() { return getToken(SqlBaseParserParser.FIRST, 0); }
		public TerminalNode AFTER() { return getToken(SqlBaseParserParser.AFTER, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public ColPositionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colPosition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterColPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitColPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitColPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColPositionContext colPosition() throws RecognitionException {
		ColPositionContext _localctx = new ColPositionContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_colPosition);
		try {
			setState(2979);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				enterOuterAlt(_localctx, 1);
				{
				setState(2976);
				((ColPositionContext)_localctx).position = match(FIRST);
				}
				break;
			case AFTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2977);
				((ColPositionContext)_localctx).position = match(AFTER);
				setState(2978);
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
		public TerminalNode LT() { return getToken(SqlBaseParserParser.LT, 0); }
		public List<DataTypeContext> dataType() {
			return getRuleContexts(DataTypeContext.class);
		}
		public DataTypeContext dataType(int i) {
			return getRuleContext(DataTypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(SqlBaseParserParser.GT, 0); }
		public TerminalNode ARRAY() { return getToken(SqlBaseParserParser.ARRAY, 0); }
		public TerminalNode COMMA() { return getToken(SqlBaseParserParser.COMMA, 0); }
		public TerminalNode MAP() { return getToken(SqlBaseParserParser.MAP, 0); }
		public TerminalNode STRUCT() { return getToken(SqlBaseParserParser.STRUCT, 0); }
		public TerminalNode NEQ() { return getToken(SqlBaseParserParser.NEQ, 0); }
		public ComplexColTypeListContext complexColTypeList() {
			return getRuleContext(ComplexColTypeListContext.class,0);
		}
		public ComplexDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterComplexDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitComplexDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitComplexDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YearMonthIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(SqlBaseParserParser.INTERVAL, 0); }
		public TerminalNode YEAR() { return getToken(SqlBaseParserParser.YEAR, 0); }
		public List<TerminalNode> MONTH() { return getTokens(SqlBaseParserParser.MONTH); }
		public TerminalNode MONTH(int i) {
			return getToken(SqlBaseParserParser.MONTH, i);
		}
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
		public YearMonthIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterYearMonthIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitYearMonthIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitYearMonthIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DayTimeIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(SqlBaseParserParser.INTERVAL, 0); }
		public TerminalNode DAY() { return getToken(SqlBaseParserParser.DAY, 0); }
		public List<TerminalNode> HOUR() { return getTokens(SqlBaseParserParser.HOUR); }
		public TerminalNode HOUR(int i) {
			return getToken(SqlBaseParserParser.HOUR, i);
		}
		public List<TerminalNode> MINUTE() { return getTokens(SqlBaseParserParser.MINUTE); }
		public TerminalNode MINUTE(int i) {
			return getToken(SqlBaseParserParser.MINUTE, i);
		}
		public List<TerminalNode> SECOND() { return getTokens(SqlBaseParserParser.SECOND); }
		public TerminalNode SECOND(int i) {
			return getToken(SqlBaseParserParser.SECOND, i);
		}
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
		public DayTimeIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDayTimeIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDayTimeIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDayTimeIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimitiveDataTypeContext extends DataTypeContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(SqlBaseParserParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(SqlBaseParserParser.INTEGER_VALUE, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public PrimitiveDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterPrimitiveDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitPrimitiveDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitPrimitiveDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_dataType);
		int _la;
		try {
			setState(3027);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,392,_ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2981);
				((ComplexDataTypeContext)_localctx).complex = match(ARRAY);
				setState(2982);
				match(LT);
				setState(2983);
				dataType();
				setState(2984);
				match(GT);
				}
				break;
			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2986);
				((ComplexDataTypeContext)_localctx).complex = match(MAP);
				setState(2987);
				match(LT);
				setState(2988);
				dataType();
				setState(2989);
				match(COMMA);
				setState(2990);
				dataType();
				setState(2991);
				match(GT);
				}
				break;
			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2993);
				((ComplexDataTypeContext)_localctx).complex = match(STRUCT);
				setState(3000);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LT:
					{
					setState(2994);
					match(LT);
					setState(2996);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,386,_ctx) ) {
					case 1:
						{
						setState(2995);
						complexColTypeList();
						}
						break;
					}
					setState(2998);
					match(GT);
					}
					break;
				case NEQ:
					{
					setState(2999);
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
				setState(3002);
				match(INTERVAL);
				setState(3003);
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
				setState(3006);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,388,_ctx) ) {
				case 1:
					{
					setState(3004);
					match(TO);
					setState(3005);
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
				setState(3008);
				match(INTERVAL);
				setState(3009);
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
				setState(3012);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,389,_ctx) ) {
				case 1:
					{
					setState(3010);
					match(TO);
					setState(3011);
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
				setState(3014);
				identifier();
				setState(3025);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,391,_ctx) ) {
				case 1:
					{
					setState(3015);
					match(LEFT_PAREN);
					setState(3016);
					match(INTEGER_VALUE);
					setState(3021);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(3017);
						match(COMMA);
						setState(3018);
						match(INTEGER_VALUE);
						}
						}
						setState(3023);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(3024);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public QualifiedColTypeWithPositionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedColTypeWithPositionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQualifiedColTypeWithPositionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQualifiedColTypeWithPositionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQualifiedColTypeWithPositionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedColTypeWithPositionListContext qualifiedColTypeWithPositionList() throws RecognitionException {
		QualifiedColTypeWithPositionListContext _localctx = new QualifiedColTypeWithPositionListContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_qualifiedColTypeWithPositionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3029);
			qualifiedColTypeWithPosition();
			setState(3034);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3030);
				match(COMMA);
				setState(3031);
				qualifiedColTypeWithPosition();
				}
				}
				setState(3036);
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
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQualifiedColTypeWithPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQualifiedColTypeWithPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQualifiedColTypeWithPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedColTypeWithPositionContext qualifiedColTypeWithPosition() throws RecognitionException {
		QualifiedColTypeWithPositionContext _localctx = new QualifiedColTypeWithPositionContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_qualifiedColTypeWithPosition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3037);
			((QualifiedColTypeWithPositionContext)_localctx).name = multipartIdentifier();
			setState(3038);
			dataType();
			setState(3041);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(3039);
				match(NOT);
				setState(3040);
				match(NULL);
				}
			}

			setState(3044);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(3043);
				commentSpec();
				}
			}

			setState(3047);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AFTER || _la==FIRST) {
				{
				setState(3046);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public ColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeListContext colTypeList() throws RecognitionException {
		ColTypeListContext _localctx = new ColTypeListContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_colTypeList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3049);
			colType();
			setState(3054);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,397,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3050);
					match(COMMA);
					setState(3051);
					colType();
					}
					} 
				}
				setState(3056);
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
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeContext colType() throws RecognitionException {
		ColTypeContext _localctx = new ColTypeContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_colType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3057);
			((ColTypeContext)_localctx).colName = errorCapturingIdentifier();
			setState(3058);
			dataType();
			setState(3061);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,398,_ctx) ) {
			case 1:
				{
				setState(3059);
				match(NOT);
				setState(3060);
				match(NULL);
				}
				break;
			}
			setState(3064);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,399,_ctx) ) {
			case 1:
				{
				setState(3063);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public ComplexColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterComplexColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitComplexColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitComplexColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeListContext complexColTypeList() throws RecognitionException {
		ComplexColTypeListContext _localctx = new ComplexColTypeListContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_complexColTypeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3066);
			complexColType();
			setState(3071);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3067);
				match(COMMA);
				setState(3068);
				complexColType();
				}
				}
				setState(3073);
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
		public TerminalNode COLON() { return getToken(SqlBaseParserParser.COLON, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ComplexColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterComplexColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitComplexColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitComplexColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeContext complexColType() throws RecognitionException {
		ComplexColTypeContext _localctx = new ComplexColTypeContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_complexColType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3074);
			identifier();
			setState(3076);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,401,_ctx) ) {
			case 1:
				{
				setState(3075);
				match(COLON);
				}
				break;
			}
			setState(3078);
			dataType();
			setState(3081);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(3079);
				match(NOT);
				setState(3080);
				match(NULL);
				}
			}

			setState(3084);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(3083);
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
		public TerminalNode WHEN() { return getToken(SqlBaseParserParser.WHEN, 0); }
		public TerminalNode THEN() { return getToken(SqlBaseParserParser.THEN, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterWhenClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitWhenClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitWhenClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3086);
			match(WHEN);
			setState(3087);
			((WhenClauseContext)_localctx).condition = expression();
			setState(3088);
			match(THEN);
			setState(3089);
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
		public TerminalNode WINDOW() { return getToken(SqlBaseParserParser.WINDOW, 0); }
		public List<NamedWindowContext> namedWindow() {
			return getRuleContexts(NamedWindowContext.class);
		}
		public NamedWindowContext namedWindow(int i) {
			return getRuleContext(NamedWindowContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public WindowClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterWindowClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitWindowClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitWindowClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowClauseContext windowClause() throws RecognitionException {
		WindowClauseContext _localctx = new WindowClauseContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_windowClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3091);
			match(WINDOW);
			setState(3092);
			namedWindow();
			setState(3097);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,404,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3093);
					match(COMMA);
					setState(3094);
					namedWindow();
					}
					} 
				}
				setState(3099);
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
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNamedWindow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNamedWindow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNamedWindow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedWindowContext namedWindow() throws RecognitionException {
		NamedWindowContext _localctx = new NamedWindowContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_namedWindow);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3100);
			((NamedWindowContext)_localctx).name = errorCapturingIdentifier();
			setState(3101);
			match(AS);
			setState(3102);
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
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public WindowRefContext(WindowSpecContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterWindowRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitWindowRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitWindowRef(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WindowDefContext extends WindowSpecContext {
		public ExpressionContext expression;
		public List<ExpressionContext> partition = new ArrayList<ExpressionContext>();
		public TerminalNode LEFT_PAREN() { return getToken(SqlBaseParserParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SqlBaseParserParser.RIGHT_PAREN, 0); }
		public TerminalNode CLUSTER() { return getToken(SqlBaseParserParser.CLUSTER, 0); }
		public List<TerminalNode> BY() { return getTokens(SqlBaseParserParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(SqlBaseParserParser.BY, i);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public List<SortItemContext> sortItem() {
			return getRuleContexts(SortItemContext.class);
		}
		public SortItemContext sortItem(int i) {
			return getRuleContext(SortItemContext.class,i);
		}
		public TerminalNode PARTITION() { return getToken(SqlBaseParserParser.PARTITION, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(SqlBaseParserParser.DISTRIBUTE, 0); }
		public TerminalNode ORDER() { return getToken(SqlBaseParserParser.ORDER, 0); }
		public TerminalNode SORT() { return getToken(SqlBaseParserParser.SORT, 0); }
		public WindowDefContext(WindowSpecContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterWindowDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitWindowDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitWindowDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowSpecContext windowSpec() throws RecognitionException {
		WindowSpecContext _localctx = new WindowSpecContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_windowSpec);
		int _la;
		try {
			setState(3150);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,412,_ctx) ) {
			case 1:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3104);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				}
				break;
			case 2:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3105);
				match(LEFT_PAREN);
				setState(3106);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				setState(3107);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				_localctx = new WindowDefContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3109);
				match(LEFT_PAREN);
				setState(3144);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLUSTER:
					{
					setState(3110);
					match(CLUSTER);
					setState(3111);
					match(BY);
					setState(3112);
					((WindowDefContext)_localctx).expression = expression();
					((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
					setState(3117);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(3113);
						match(COMMA);
						setState(3114);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						}
						}
						setState(3119);
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
					setState(3130);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DISTRIBUTE || _la==PARTITION) {
						{
						setState(3120);
						_la = _input.LA(1);
						if ( !(_la==DISTRIBUTE || _la==PARTITION) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3121);
						match(BY);
						setState(3122);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						setState(3127);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==COMMA) {
							{
							{
							setState(3123);
							match(COMMA);
							setState(3124);
							((WindowDefContext)_localctx).expression = expression();
							((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
							}
							}
							setState(3129);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
					}

					setState(3142);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ORDER || _la==SORT) {
						{
						setState(3132);
						_la = _input.LA(1);
						if ( !(_la==ORDER || _la==SORT) ) {
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
						sortItem();
						setState(3139);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==COMMA) {
							{
							{
							setState(3135);
							match(COMMA);
							setState(3136);
							sortItem();
							}
							}
							setState(3141);
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
				setState(3147);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==RANGE || _la==ROWS) {
					{
					setState(3146);
					windowFrame();
					}
				}

				setState(3149);
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
		public TerminalNode RANGE() { return getToken(SqlBaseParserParser.RANGE, 0); }
		public List<FrameBoundContext> frameBound() {
			return getRuleContexts(FrameBoundContext.class);
		}
		public FrameBoundContext frameBound(int i) {
			return getRuleContext(FrameBoundContext.class,i);
		}
		public TerminalNode ROWS() { return getToken(SqlBaseParserParser.ROWS, 0); }
		public TerminalNode BETWEEN() { return getToken(SqlBaseParserParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public WindowFrameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrame; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterWindowFrame(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitWindowFrame(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitWindowFrame(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameContext windowFrame() throws RecognitionException {
		WindowFrameContext _localctx = new WindowFrameContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_windowFrame);
		try {
			setState(3168);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,413,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3152);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3153);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3154);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3155);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3156);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3157);
				match(BETWEEN);
				setState(3158);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3159);
				match(AND);
				setState(3160);
				((WindowFrameContext)_localctx).end = frameBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3162);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3163);
				match(BETWEEN);
				setState(3164);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3165);
				match(AND);
				setState(3166);
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
		public TerminalNode UNBOUNDED() { return getToken(SqlBaseParserParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(SqlBaseParserParser.PRECEDING, 0); }
		public TerminalNode FOLLOWING() { return getToken(SqlBaseParserParser.FOLLOWING, 0); }
		public TerminalNode ROW() { return getToken(SqlBaseParserParser.ROW, 0); }
		public TerminalNode CURRENT() { return getToken(SqlBaseParserParser.CURRENT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FrameBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFrameBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFrameBound(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFrameBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameBoundContext frameBound() throws RecognitionException {
		FrameBoundContext _localctx = new FrameBoundContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_frameBound);
		int _la;
		try {
			setState(3177);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,414,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3170);
				match(UNBOUNDED);
				setState(3171);
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
				setState(3172);
				((FrameBoundContext)_localctx).boundType = match(CURRENT);
				setState(3173);
				match(ROW);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3174);
				expression();
				setState(3175);
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
		public List<TerminalNode> COMMA() { return getTokens(SqlBaseParserParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlBaseParserParser.COMMA, i);
		}
		public QualifiedNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQualifiedNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQualifiedNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQualifiedNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameListContext qualifiedNameList() throws RecognitionException {
		QualifiedNameListContext _localctx = new QualifiedNameListContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3179);
			qualifiedName();
			setState(3184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(3180);
				match(COMMA);
				setState(3181);
				qualifiedName();
				}
				}
				setState(3186);
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
		public TerminalNode FILTER() { return getToken(SqlBaseParserParser.FILTER, 0); }
		public TerminalNode LEFT() { return getToken(SqlBaseParserParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(SqlBaseParserParser.RIGHT, 0); }
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_functionName);
		try {
			setState(3191);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,416,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3187);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3188);
				match(FILTER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3189);
				match(LEFT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3190);
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
		public List<TerminalNode> DOT() { return getTokens(SqlBaseParserParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SqlBaseParserParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3193);
			identifier();
			setState(3198);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,417,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3194);
					match(DOT);
					setState(3195);
					identifier();
					}
					} 
				}
				setState(3200);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterErrorCapturingIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitErrorCapturingIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitErrorCapturingIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierContext errorCapturingIdentifier() throws RecognitionException {
		ErrorCapturingIdentifierContext _localctx = new ErrorCapturingIdentifierContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_errorCapturingIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3201);
			identifier();
			setState(3202);
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
		public List<TerminalNode> MINUS() { return getTokens(SqlBaseParserParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(SqlBaseParserParser.MINUS, i);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterErrorIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitErrorIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitErrorIdent(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RealIdentContext extends ErrorCapturingIdentifierExtraContext {
		public RealIdentContext(ErrorCapturingIdentifierExtraContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterRealIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitRealIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitRealIdent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierExtraContext errorCapturingIdentifierExtra() throws RecognitionException {
		ErrorCapturingIdentifierExtraContext _localctx = new ErrorCapturingIdentifierExtraContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_errorCapturingIdentifierExtra);
		try {
			int _alt;
			setState(3211);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,419,_ctx) ) {
			case 1:
				_localctx = new ErrorIdentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3206); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(3204);
						match(MINUS);
						setState(3205);
						identifier();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(3208); 
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_identifier);
		try {
			setState(3216);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,420,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3213);
				strictIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3214);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3215);
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
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQuotedIdentifierAlternative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQuotedIdentifierAlternative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQuotedIdentifierAlternative(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnquotedIdentifierContext extends StrictIdentifierContext {
		public TerminalNode IDENTIFIER() { return getToken(SqlBaseParserParser.IDENTIFIER, 0); }
		public AnsiNonReservedContext ansiNonReserved() {
			return getRuleContext(AnsiNonReservedContext.class,0);
		}
		public NonReservedContext nonReserved() {
			return getRuleContext(NonReservedContext.class,0);
		}
		public UnquotedIdentifierContext(StrictIdentifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterUnquotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitUnquotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitUnquotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictIdentifierContext strictIdentifier() throws RecognitionException {
		StrictIdentifierContext _localctx = new StrictIdentifierContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_strictIdentifier);
		try {
			setState(3224);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,421,_ctx) ) {
			case 1:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3218);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3219);
				quotedIdentifier();
				}
				break;
			case 3:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3220);
				if (!(SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "SQL_standard_keyword_behavior");
				setState(3221);
				ansiNonReserved();
				}
				break;
			case 4:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3222);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3223);
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
		public TerminalNode BACKQUOTED_IDENTIFIER() { return getToken(SqlBaseParserParser.BACKQUOTED_IDENTIFIER, 0); }
		public QuotedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterQuotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitQuotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitQuotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuotedIdentifierContext quotedIdentifier() throws RecognitionException {
		QuotedIdentifierContext _localctx = new QuotedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_quotedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3226);
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
		public TerminalNode DECIMAL_VALUE() { return getToken(SqlBaseParserParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public DecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BigIntLiteralContext extends NumberContext {
		public TerminalNode BIGINT_LITERAL() { return getToken(SqlBaseParserParser.BIGINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public BigIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterBigIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitBigIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitBigIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TinyIntLiteralContext extends NumberContext {
		public TerminalNode TINYINT_LITERAL() { return getToken(SqlBaseParserParser.TINYINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public TinyIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterTinyIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitTinyIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitTinyIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LegacyDecimalLiteralContext extends NumberContext {
		public TerminalNode EXPONENT_VALUE() { return getToken(SqlBaseParserParser.EXPONENT_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(SqlBaseParserParser.DECIMAL_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public LegacyDecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterLegacyDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitLegacyDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitLegacyDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BigDecimalLiteralContext extends NumberContext {
		public TerminalNode BIGDECIMAL_LITERAL() { return getToken(SqlBaseParserParser.BIGDECIMAL_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public BigDecimalLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterBigDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitBigDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitBigDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExponentLiteralContext extends NumberContext {
		public TerminalNode EXPONENT_VALUE() { return getToken(SqlBaseParserParser.EXPONENT_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public ExponentLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterExponentLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitExponentLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitExponentLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DoubleLiteralContext extends NumberContext {
		public TerminalNode DOUBLE_LITERAL() { return getToken(SqlBaseParserParser.DOUBLE_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public DoubleLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterDoubleLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitDoubleLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitDoubleLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IntegerLiteralContext extends NumberContext {
		public TerminalNode INTEGER_VALUE() { return getToken(SqlBaseParserParser.INTEGER_VALUE, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public IntegerLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FloatLiteralContext extends NumberContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(SqlBaseParserParser.FLOAT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public FloatLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitFloatLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SmallIntLiteralContext extends NumberContext {
		public TerminalNode SMALLINT_LITERAL() { return getToken(SqlBaseParserParser.SMALLINT_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(SqlBaseParserParser.MINUS, 0); }
		public SmallIntLiteralContext(NumberContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterSmallIntLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitSmallIntLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitSmallIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_number);
		int _la;
		try {
			setState(3271);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,432,_ctx) ) {
			case 1:
				_localctx = new ExponentLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3228);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3230);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3229);
					match(MINUS);
					}
				}

				setState(3232);
				match(EXPONENT_VALUE);
				}
				break;
			case 2:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3233);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3235);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3234);
					match(MINUS);
					}
				}

				setState(3237);
				match(DECIMAL_VALUE);
				}
				break;
			case 3:
				_localctx = new LegacyDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3238);
				if (!(legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "legacy_exponent_literal_as_decimal_enabled");
				setState(3240);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3239);
					match(MINUS);
					}
				}

				setState(3242);
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
				setState(3244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3243);
					match(MINUS);
					}
				}

				setState(3246);
				match(INTEGER_VALUE);
				}
				break;
			case 5:
				_localctx = new BigIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(3248);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3247);
					match(MINUS);
					}
				}

				setState(3250);
				match(BIGINT_LITERAL);
				}
				break;
			case 6:
				_localctx = new SmallIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
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
				match(SMALLINT_LITERAL);
				}
				break;
			case 7:
				_localctx = new TinyIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
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
				match(TINYINT_LITERAL);
				}
				break;
			case 8:
				_localctx = new DoubleLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
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
				match(DOUBLE_LITERAL);
				}
				break;
			case 9:
				_localctx = new FloatLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
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
				match(FLOAT_LITERAL);
				}
				break;
			case 10:
				_localctx = new BigDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
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
		public TerminalNode TYPE() { return getToken(SqlBaseParserParser.TYPE, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public CommentSpecContext commentSpec() {
			return getRuleContext(CommentSpecContext.class,0);
		}
		public ColPositionContext colPosition() {
			return getRuleContext(ColPositionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public AlterColumnActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterColumnAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAlterColumnAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAlterColumnAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAlterColumnAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterColumnActionContext alterColumnAction() throws RecognitionException {
		AlterColumnActionContext _localctx = new AlterColumnActionContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_alterColumnAction);
		int _la;
		try {
			setState(3280);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(3273);
				match(TYPE);
				setState(3274);
				dataType();
				}
				break;
			case COMMENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(3275);
				commentSpec();
				}
				break;
			case AFTER:
			case FIRST:
				enterOuterAlt(_localctx, 3);
				{
				setState(3276);
				colPosition();
				}
				break;
			case DROP:
			case SET:
				enterOuterAlt(_localctx, 4);
				{
				setState(3277);
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
				setState(3278);
				match(NOT);
				setState(3279);
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
		public TerminalNode ADD() { return getToken(SqlBaseParserParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(SqlBaseParserParser.AFTER, 0); }
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(SqlBaseParserParser.ANALYZE, 0); }
		public TerminalNode ANTI() { return getToken(SqlBaseParserParser.ANTI, 0); }
		public TerminalNode ARCHIVE() { return getToken(SqlBaseParserParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(SqlBaseParserParser.ARRAY, 0); }
		public TerminalNode ASC() { return getToken(SqlBaseParserParser.ASC, 0); }
		public TerminalNode AT() { return getToken(SqlBaseParserParser.AT, 0); }
		public TerminalNode BETWEEN() { return getToken(SqlBaseParserParser.BETWEEN, 0); }
		public TerminalNode BUCKET() { return getToken(SqlBaseParserParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(SqlBaseParserParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(SqlBaseParserParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(SqlBaseParserParser.CASCADE, 0); }
		public TerminalNode CATALOG() { return getToken(SqlBaseParserParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(SqlBaseParserParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(SqlBaseParserParser.CHANGE, 0); }
		public TerminalNode CLEAR() { return getToken(SqlBaseParserParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(SqlBaseParserParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(SqlBaseParserParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(SqlBaseParserParser.CODEGEN, 0); }
		public TerminalNode COLLECTION() { return getToken(SqlBaseParserParser.COLLECTION, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(SqlBaseParserParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(SqlBaseParserParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(SqlBaseParserParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(SqlBaseParserParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(SqlBaseParserParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(SqlBaseParserParser.CONCATENATE, 0); }
		public TerminalNode COST() { return getToken(SqlBaseParserParser.COST, 0); }
		public TerminalNode CUBE() { return getToken(SqlBaseParserParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(SqlBaseParserParser.CURRENT, 0); }
		public TerminalNode DATA() { return getToken(SqlBaseParserParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(SqlBaseParserParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(SqlBaseParserParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(SqlBaseParserParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(SqlBaseParserParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(SqlBaseParserParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(SqlBaseParserParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(SqlBaseParserParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(SqlBaseParserParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(SqlBaseParserParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(SqlBaseParserParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SqlBaseParserParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(SqlBaseParserParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(SqlBaseParserParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(SqlBaseParserParser.DIRECTORY, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(SqlBaseParserParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(SqlBaseParserParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode ESCAPED() { return getToken(SqlBaseParserParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(SqlBaseParserParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(SqlBaseParserParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(SqlBaseParserParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(SqlBaseParserParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(SqlBaseParserParser.EXTRACT, 0); }
		public TerminalNode FIELDS() { return getToken(SqlBaseParserParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(SqlBaseParserParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(SqlBaseParserParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(SqlBaseParserParser.FOLLOWING, 0); }
		public TerminalNode FORMAT() { return getToken(SqlBaseParserParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(SqlBaseParserParser.FORMATTED, 0); }
		public TerminalNode FUNCTION() { return getToken(SqlBaseParserParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(SqlBaseParserParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(SqlBaseParserParser.GLOBAL, 0); }
		public TerminalNode GROUPING() { return getToken(SqlBaseParserParser.GROUPING, 0); }
		public TerminalNode HOUR() { return getToken(SqlBaseParserParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(SqlBaseParserParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(SqlBaseParserParser.IMPORT, 0); }
		public TerminalNode INDEX() { return getToken(SqlBaseParserParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(SqlBaseParserParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(SqlBaseParserParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(SqlBaseParserParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(SqlBaseParserParser.INTERVAL, 0); }
		public TerminalNode ITEMS() { return getToken(SqlBaseParserParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(SqlBaseParserParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(SqlBaseParserParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(SqlBaseParserParser.LAZY, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(SqlBaseParserParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(SqlBaseParserParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(SqlBaseParserParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(SqlBaseParserParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(SqlBaseParserParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(SqlBaseParserParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(SqlBaseParserParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(SqlBaseParserParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(SqlBaseParserParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(SqlBaseParserParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(SqlBaseParserParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(SqlBaseParserParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(SqlBaseParserParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(SqlBaseParserParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(SqlBaseParserParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(SqlBaseParserParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(SqlBaseParserParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(SqlBaseParserParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(SqlBaseParserParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(SqlBaseParserParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(SqlBaseParserParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(SqlBaseParserParser.NO, 0); }
		public TerminalNode NULLS() { return getToken(SqlBaseParserParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(SqlBaseParserParser.OF, 0); }
		public TerminalNode OPTION() { return getToken(SqlBaseParserParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public TerminalNode OUT() { return getToken(SqlBaseParserParser.OUT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(SqlBaseParserParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(SqlBaseParserParser.OVER, 0); }
		public TerminalNode OVERLAY() { return getToken(SqlBaseParserParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(SqlBaseParserParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(SqlBaseParserParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(SqlBaseParserParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(SqlBaseParserParser.PARTITIONS, 0); }
		public TerminalNode PERCENTLIT() { return getToken(SqlBaseParserParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(SqlBaseParserParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(SqlBaseParserParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(SqlBaseParserParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(SqlBaseParserParser.PRECEDING, 0); }
		public TerminalNode PRINCIPALS() { return getToken(SqlBaseParserParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(SqlBaseParserParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(SqlBaseParserParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(SqlBaseParserParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(SqlBaseParserParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(SqlBaseParserParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(SqlBaseParserParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(SqlBaseParserParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(SqlBaseParserParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(SqlBaseParserParser.REDUCE, 0); }
		public TerminalNode REFRESH() { return getToken(SqlBaseParserParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(SqlBaseParserParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(SqlBaseParserParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(SqlBaseParserParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(SqlBaseParserParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(SqlBaseParserParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(SqlBaseParserParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(SqlBaseParserParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(SqlBaseParserParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(SqlBaseParserParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(SqlBaseParserParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(SqlBaseParserParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(SqlBaseParserParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(SqlBaseParserParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(SqlBaseParserParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(SqlBaseParserParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(SqlBaseParserParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(SqlBaseParserParser.SECOND, 0); }
		public TerminalNode SEMI() { return getToken(SqlBaseParserParser.SEMI, 0); }
		public TerminalNode SEPARATED() { return getToken(SqlBaseParserParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(SqlBaseParserParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(SqlBaseParserParser.SERDEPROPERTIES, 0); }
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode SETMINUS() { return getToken(SqlBaseParserParser.SETMINUS, 0); }
		public TerminalNode SETS() { return getToken(SqlBaseParserParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(SqlBaseParserParser.SKEWED, 0); }
		public TerminalNode SORT() { return getToken(SqlBaseParserParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(SqlBaseParserParser.SORTED, 0); }
		public TerminalNode START() { return getToken(SqlBaseParserParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(SqlBaseParserParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(SqlBaseParserParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(SqlBaseParserParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(SqlBaseParserParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(SqlBaseParserParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(SqlBaseParserParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(SqlBaseParserParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(SqlBaseParserParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(SqlBaseParserParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLES() { return getToken(SqlBaseParserParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(SqlBaseParserParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(SqlBaseParserParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(SqlBaseParserParser.TERMINATED, 0); }
		public TerminalNode TIMESTAMP() { return getToken(SqlBaseParserParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(SqlBaseParserParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(SqlBaseParserParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TOUCH() { return getToken(SqlBaseParserParser.TOUCH, 0); }
		public TerminalNode TRANSACTION() { return getToken(SqlBaseParserParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(SqlBaseParserParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(SqlBaseParserParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(SqlBaseParserParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(SqlBaseParserParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(SqlBaseParserParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(SqlBaseParserParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(SqlBaseParserParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(SqlBaseParserParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(SqlBaseParserParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(SqlBaseParserParser.UNCACHE, 0); }
		public TerminalNode UNLOCK() { return getToken(SqlBaseParserParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(SqlBaseParserParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(SqlBaseParserParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(SqlBaseParserParser.USE, 0); }
		public TerminalNode VALUES() { return getToken(SqlBaseParserParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(SqlBaseParserParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(SqlBaseParserParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(SqlBaseParserParser.WEEK, 0); }
		public TerminalNode WINDOW() { return getToken(SqlBaseParserParser.WINDOW, 0); }
		public TerminalNode YEAR() { return getToken(SqlBaseParserParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(SqlBaseParserParser.ZONE, 0); }
		public AnsiNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ansiNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterAnsiNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitAnsiNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitAnsiNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnsiNonReservedContext ansiNonReserved() throws RecognitionException {
		AnsiNonReservedContext _localctx = new AnsiNonReservedContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_ansiNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3282);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALTER) | (1L << ANALYZE) | (1L << ANTI) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << ASC) | (1L << AT) | (1L << BETWEEN) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLECTION) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << COST) | (1L << CUBE) | (1L << CURRENT) | (1L << DAY) | (1L << DAYOFYEAR) | (1L << DATA) | (1L << DATABASE) | (1L << DATABASES))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FIELDS - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GROUPING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)) | (1L << (ITEMS - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (KEYS - 128)) | (1L << (LAST - 128)) | (1L << (LAZY - 128)) | (1L << (LIKE - 128)) | (1L << (ILIKE - 128)) | (1L << (LIMIT - 128)) | (1L << (LINES - 128)) | (1L << (LIST - 128)) | (1L << (LOAD - 128)) | (1L << (LOCAL - 128)) | (1L << (LOCATION - 128)) | (1L << (LOCK - 128)) | (1L << (LOCKS - 128)) | (1L << (LOGICAL - 128)) | (1L << (MACRO - 128)) | (1L << (MAP - 128)) | (1L << (MATCHED - 128)) | (1L << (MERGE - 128)) | (1L << (MICROSECOND - 128)) | (1L << (MILLISECOND - 128)) | (1L << (MINUTE - 128)) | (1L << (MONTH - 128)) | (1L << (MSCK - 128)) | (1L << (NAMESPACE - 128)) | (1L << (NAMESPACES - 128)) | (1L << (NO - 128)) | (1L << (NULLS - 128)) | (1L << (OF - 128)) | (1L << (OPTION - 128)) | (1L << (OPTIONS - 128)) | (1L << (OUT - 128)) | (1L << (OUTPUTFORMAT - 128)) | (1L << (OVER - 128)) | (1L << (OVERLAY - 128)) | (1L << (OVERWRITE - 128)) | (1L << (PARTITION - 128)) | (1L << (PARTITIONED - 128)) | (1L << (PARTITIONS - 128)) | (1L << (PERCENTLIT - 128)) | (1L << (PIVOT - 128)) | (1L << (PLACING - 128)) | (1L << (POSITION - 128)) | (1L << (PRECEDING - 128)) | (1L << (PRINCIPALS - 128)) | (1L << (PROPERTIES - 128)) | (1L << (PURGE - 128)) | (1L << (QUARTER - 128)) | (1L << (QUERY - 128)) | (1L << (RANGE - 128)))) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & ((1L << (RECORDREADER - 192)) | (1L << (RECORDWRITER - 192)) | (1L << (RECOVER - 192)) | (1L << (REDUCE - 192)) | (1L << (REFRESH - 192)) | (1L << (RENAME - 192)) | (1L << (REPAIR - 192)) | (1L << (REPEATABLE - 192)) | (1L << (REPLACE - 192)) | (1L << (RESET - 192)) | (1L << (RESPECT - 192)) | (1L << (RESTRICT - 192)) | (1L << (REVOKE - 192)) | (1L << (RLIKE - 192)) | (1L << (ROLE - 192)) | (1L << (ROLES - 192)) | (1L << (ROLLBACK - 192)) | (1L << (ROLLUP - 192)) | (1L << (ROW - 192)) | (1L << (ROWS - 192)) | (1L << (SECOND - 192)) | (1L << (SCHEMA - 192)) | (1L << (SCHEMAS - 192)) | (1L << (SEMI - 192)) | (1L << (SEPARATED - 192)) | (1L << (SERDE - 192)) | (1L << (SERDEPROPERTIES - 192)) | (1L << (SET - 192)) | (1L << (SETMINUS - 192)) | (1L << (SETS - 192)) | (1L << (SHOW - 192)) | (1L << (SKEWED - 192)) | (1L << (SORT - 192)) | (1L << (SORTED - 192)) | (1L << (START - 192)) | (1L << (STATISTICS - 192)) | (1L << (STORED - 192)) | (1L << (STRATIFY - 192)) | (1L << (STRUCT - 192)) | (1L << (SUBSTR - 192)) | (1L << (SUBSTRING - 192)) | (1L << (SYNC - 192)) | (1L << (SYSTEM_TIME - 192)) | (1L << (SYSTEM_VERSION - 192)) | (1L << (TABLES - 192)) | (1L << (TABLESAMPLE - 192)) | (1L << (TBLPROPERTIES - 192)) | (1L << (TEMPORARY - 192)) | (1L << (TERMINATED - 192)) | (1L << (TIMESTAMP - 192)) | (1L << (TIMESTAMPADD - 192)) | (1L << (TIMESTAMPDIFF - 192)) | (1L << (TOUCH - 192)) | (1L << (TRANSACTION - 192)))) != 0) || ((((_la - 256)) & ~0x3f) == 0 && ((1L << (_la - 256)) & ((1L << (TRANSACTIONS - 256)) | (1L << (TRANSFORM - 256)) | (1L << (TRIM - 256)) | (1L << (TRUE - 256)) | (1L << (TRUNCATE - 256)) | (1L << (TRY_CAST - 256)) | (1L << (TYPE - 256)) | (1L << (UNARCHIVE - 256)) | (1L << (UNBOUNDED - 256)) | (1L << (UNCACHE - 256)) | (1L << (UNLOCK - 256)) | (1L << (UNSET - 256)) | (1L << (UPDATE - 256)) | (1L << (USE - 256)) | (1L << (VALUES - 256)) | (1L << (VERSION - 256)) | (1L << (VIEW - 256)) | (1L << (VIEWS - 256)) | (1L << (WEEK - 256)) | (1L << (WINDOW - 256)) | (1L << (YEAR - 256)) | (1L << (ZONE - 256)))) != 0)) ) {
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
		public TerminalNode ANTI() { return getToken(SqlBaseParserParser.ANTI, 0); }
		public TerminalNode CROSS() { return getToken(SqlBaseParserParser.CROSS, 0); }
		public TerminalNode EXCEPT() { return getToken(SqlBaseParserParser.EXCEPT, 0); }
		public TerminalNode FULL() { return getToken(SqlBaseParserParser.FULL, 0); }
		public TerminalNode INNER() { return getToken(SqlBaseParserParser.INNER, 0); }
		public TerminalNode INTERSECT() { return getToken(SqlBaseParserParser.INTERSECT, 0); }
		public TerminalNode JOIN() { return getToken(SqlBaseParserParser.JOIN, 0); }
		public TerminalNode LATERAL() { return getToken(SqlBaseParserParser.LATERAL, 0); }
		public TerminalNode LEFT() { return getToken(SqlBaseParserParser.LEFT, 0); }
		public TerminalNode NATURAL() { return getToken(SqlBaseParserParser.NATURAL, 0); }
		public TerminalNode ON() { return getToken(SqlBaseParserParser.ON, 0); }
		public TerminalNode RIGHT() { return getToken(SqlBaseParserParser.RIGHT, 0); }
		public TerminalNode SEMI() { return getToken(SqlBaseParserParser.SEMI, 0); }
		public TerminalNode SETMINUS() { return getToken(SqlBaseParserParser.SETMINUS, 0); }
		public TerminalNode UNION() { return getToken(SqlBaseParserParser.UNION, 0); }
		public TerminalNode USING() { return getToken(SqlBaseParserParser.USING, 0); }
		public StrictNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strictNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterStrictNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitStrictNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitStrictNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictNonReservedContext strictNonReserved() throws RecognitionException {
		StrictNonReservedContext _localctx = new StrictNonReservedContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_strictNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3284);
			_la = _input.LA(1);
			if ( !(_la==ANTI || _la==CROSS || ((((_la - 83)) & ~0x3f) == 0 && ((1L << (_la - 83)) & ((1L << (EXCEPT - 83)) | (1L << (FULL - 83)) | (1L << (INNER - 83)) | (1L << (INTERSECT - 83)) | (1L << (JOIN - 83)) | (1L << (LATERAL - 83)) | (1L << (LEFT - 83)))) != 0) || ((((_la - 156)) & ~0x3f) == 0 && ((1L << (_la - 156)) & ((1L << (NATURAL - 156)) | (1L << (ON - 156)) | (1L << (RIGHT - 156)) | (1L << (SEMI - 156)))) != 0) || ((((_la - 224)) & ~0x3f) == 0 && ((1L << (_la - 224)) & ((1L << (SETMINUS - 224)) | (1L << (UNION - 224)) | (1L << (USING - 224)))) != 0)) ) {
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
		public TerminalNode ADD() { return getToken(SqlBaseParserParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(SqlBaseParserParser.AFTER, 0); }
		public TerminalNode ALL() { return getToken(SqlBaseParserParser.ALL, 0); }
		public TerminalNode ALTER() { return getToken(SqlBaseParserParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(SqlBaseParserParser.ANALYZE, 0); }
		public TerminalNode AND() { return getToken(SqlBaseParserParser.AND, 0); }
		public TerminalNode ANY() { return getToken(SqlBaseParserParser.ANY, 0); }
		public TerminalNode ARCHIVE() { return getToken(SqlBaseParserParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(SqlBaseParserParser.ARRAY, 0); }
		public TerminalNode AS() { return getToken(SqlBaseParserParser.AS, 0); }
		public TerminalNode ASC() { return getToken(SqlBaseParserParser.ASC, 0); }
		public TerminalNode AT() { return getToken(SqlBaseParserParser.AT, 0); }
		public TerminalNode AUTHORIZATION() { return getToken(SqlBaseParserParser.AUTHORIZATION, 0); }
		public TerminalNode BETWEEN() { return getToken(SqlBaseParserParser.BETWEEN, 0); }
		public TerminalNode BOTH() { return getToken(SqlBaseParserParser.BOTH, 0); }
		public TerminalNode BUCKET() { return getToken(SqlBaseParserParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(SqlBaseParserParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(SqlBaseParserParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(SqlBaseParserParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(SqlBaseParserParser.CASCADE, 0); }
		public TerminalNode CASE() { return getToken(SqlBaseParserParser.CASE, 0); }
		public TerminalNode CAST() { return getToken(SqlBaseParserParser.CAST, 0); }
		public TerminalNode CATALOG() { return getToken(SqlBaseParserParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(SqlBaseParserParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(SqlBaseParserParser.CHANGE, 0); }
		public TerminalNode CHECK() { return getToken(SqlBaseParserParser.CHECK, 0); }
		public TerminalNode CLEAR() { return getToken(SqlBaseParserParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(SqlBaseParserParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(SqlBaseParserParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(SqlBaseParserParser.CODEGEN, 0); }
		public TerminalNode COLLATE() { return getToken(SqlBaseParserParser.COLLATE, 0); }
		public TerminalNode COLLECTION() { return getToken(SqlBaseParserParser.COLLECTION, 0); }
		public TerminalNode COLUMN() { return getToken(SqlBaseParserParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(SqlBaseParserParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(SqlBaseParserParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(SqlBaseParserParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(SqlBaseParserParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(SqlBaseParserParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(SqlBaseParserParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(SqlBaseParserParser.CONCATENATE, 0); }
		public TerminalNode CONSTRAINT() { return getToken(SqlBaseParserParser.CONSTRAINT, 0); }
		public TerminalNode COST() { return getToken(SqlBaseParserParser.COST, 0); }
		public TerminalNode CREATE() { return getToken(SqlBaseParserParser.CREATE, 0); }
		public TerminalNode CUBE() { return getToken(SqlBaseParserParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(SqlBaseParserParser.CURRENT, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(SqlBaseParserParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(SqlBaseParserParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(SqlBaseParserParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(SqlBaseParserParser.CURRENT_USER, 0); }
		public TerminalNode DATA() { return getToken(SqlBaseParserParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(SqlBaseParserParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(SqlBaseParserParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(SqlBaseParserParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(SqlBaseParserParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(SqlBaseParserParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(SqlBaseParserParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(SqlBaseParserParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(SqlBaseParserParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(SqlBaseParserParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(SqlBaseParserParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(SqlBaseParserParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SqlBaseParserParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(SqlBaseParserParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(SqlBaseParserParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(SqlBaseParserParser.DIRECTORY, 0); }
		public TerminalNode DISTINCT() { return getToken(SqlBaseParserParser.DISTINCT, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(SqlBaseParserParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(SqlBaseParserParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(SqlBaseParserParser.DROP, 0); }
		public TerminalNode ELSE() { return getToken(SqlBaseParserParser.ELSE, 0); }
		public TerminalNode END() { return getToken(SqlBaseParserParser.END, 0); }
		public TerminalNode ESCAPE() { return getToken(SqlBaseParserParser.ESCAPE, 0); }
		public TerminalNode ESCAPED() { return getToken(SqlBaseParserParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(SqlBaseParserParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(SqlBaseParserParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(SqlBaseParserParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(SqlBaseParserParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(SqlBaseParserParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(SqlBaseParserParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(SqlBaseParserParser.EXTRACT, 0); }
		public TerminalNode FALSE() { return getToken(SqlBaseParserParser.FALSE, 0); }
		public TerminalNode FETCH() { return getToken(SqlBaseParserParser.FETCH, 0); }
		public TerminalNode FILTER() { return getToken(SqlBaseParserParser.FILTER, 0); }
		public TerminalNode FIELDS() { return getToken(SqlBaseParserParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(SqlBaseParserParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(SqlBaseParserParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(SqlBaseParserParser.FOLLOWING, 0); }
		public TerminalNode FOR() { return getToken(SqlBaseParserParser.FOR, 0); }
		public TerminalNode FOREIGN() { return getToken(SqlBaseParserParser.FOREIGN, 0); }
		public TerminalNode FORMAT() { return getToken(SqlBaseParserParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(SqlBaseParserParser.FORMATTED, 0); }
		public TerminalNode FROM() { return getToken(SqlBaseParserParser.FROM, 0); }
		public TerminalNode FUNCTION() { return getToken(SqlBaseParserParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(SqlBaseParserParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(SqlBaseParserParser.GLOBAL, 0); }
		public TerminalNode GRANT() { return getToken(SqlBaseParserParser.GRANT, 0); }
		public TerminalNode GROUP() { return getToken(SqlBaseParserParser.GROUP, 0); }
		public TerminalNode GROUPING() { return getToken(SqlBaseParserParser.GROUPING, 0); }
		public TerminalNode HAVING() { return getToken(SqlBaseParserParser.HAVING, 0); }
		public TerminalNode HOUR() { return getToken(SqlBaseParserParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(SqlBaseParserParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(SqlBaseParserParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(SqlBaseParserParser.IMPORT, 0); }
		public TerminalNode IN() { return getToken(SqlBaseParserParser.IN, 0); }
		public TerminalNode INDEX() { return getToken(SqlBaseParserParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(SqlBaseParserParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(SqlBaseParserParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(SqlBaseParserParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(SqlBaseParserParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(SqlBaseParserParser.INTERVAL, 0); }
		public TerminalNode INTO() { return getToken(SqlBaseParserParser.INTO, 0); }
		public TerminalNode IS() { return getToken(SqlBaseParserParser.IS, 0); }
		public TerminalNode ITEMS() { return getToken(SqlBaseParserParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(SqlBaseParserParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(SqlBaseParserParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(SqlBaseParserParser.LAZY, 0); }
		public TerminalNode LEADING() { return getToken(SqlBaseParserParser.LEADING, 0); }
		public TerminalNode LIKE() { return getToken(SqlBaseParserParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(SqlBaseParserParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(SqlBaseParserParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(SqlBaseParserParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(SqlBaseParserParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(SqlBaseParserParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(SqlBaseParserParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(SqlBaseParserParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(SqlBaseParserParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(SqlBaseParserParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(SqlBaseParserParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(SqlBaseParserParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(SqlBaseParserParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(SqlBaseParserParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(SqlBaseParserParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(SqlBaseParserParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(SqlBaseParserParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(SqlBaseParserParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(SqlBaseParserParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(SqlBaseParserParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(SqlBaseParserParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(SqlBaseParserParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(SqlBaseParserParser.NO, 0); }
		public TerminalNode NOT() { return getToken(SqlBaseParserParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SqlBaseParserParser.NULL, 0); }
		public TerminalNode NULLS() { return getToken(SqlBaseParserParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(SqlBaseParserParser.OF, 0); }
		public TerminalNode ONLY() { return getToken(SqlBaseParserParser.ONLY, 0); }
		public TerminalNode OPTION() { return getToken(SqlBaseParserParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(SqlBaseParserParser.OPTIONS, 0); }
		public TerminalNode OR() { return getToken(SqlBaseParserParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(SqlBaseParserParser.ORDER, 0); }
		public TerminalNode OUT() { return getToken(SqlBaseParserParser.OUT, 0); }
		public TerminalNode OUTER() { return getToken(SqlBaseParserParser.OUTER, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(SqlBaseParserParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(SqlBaseParserParser.OVER, 0); }
		public TerminalNode OVERLAPS() { return getToken(SqlBaseParserParser.OVERLAPS, 0); }
		public TerminalNode OVERLAY() { return getToken(SqlBaseParserParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(SqlBaseParserParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(SqlBaseParserParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(SqlBaseParserParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(SqlBaseParserParser.PARTITIONS, 0); }
		public TerminalNode PERCENTILE_CONT() { return getToken(SqlBaseParserParser.PERCENTILE_CONT, 0); }
		public TerminalNode PERCENTILE_DISC() { return getToken(SqlBaseParserParser.PERCENTILE_DISC, 0); }
		public TerminalNode PERCENTLIT() { return getToken(SqlBaseParserParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(SqlBaseParserParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(SqlBaseParserParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(SqlBaseParserParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(SqlBaseParserParser.PRECEDING, 0); }
		public TerminalNode PRIMARY() { return getToken(SqlBaseParserParser.PRIMARY, 0); }
		public TerminalNode PRINCIPALS() { return getToken(SqlBaseParserParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(SqlBaseParserParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(SqlBaseParserParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(SqlBaseParserParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(SqlBaseParserParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(SqlBaseParserParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(SqlBaseParserParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(SqlBaseParserParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(SqlBaseParserParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(SqlBaseParserParser.REDUCE, 0); }
		public TerminalNode REFERENCES() { return getToken(SqlBaseParserParser.REFERENCES, 0); }
		public TerminalNode REFRESH() { return getToken(SqlBaseParserParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(SqlBaseParserParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(SqlBaseParserParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(SqlBaseParserParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(SqlBaseParserParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(SqlBaseParserParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(SqlBaseParserParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(SqlBaseParserParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(SqlBaseParserParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(SqlBaseParserParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(SqlBaseParserParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(SqlBaseParserParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(SqlBaseParserParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(SqlBaseParserParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(SqlBaseParserParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(SqlBaseParserParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(SqlBaseParserParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(SqlBaseParserParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(SqlBaseParserParser.SECOND, 0); }
		public TerminalNode SELECT() { return getToken(SqlBaseParserParser.SELECT, 0); }
		public TerminalNode SEPARATED() { return getToken(SqlBaseParserParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(SqlBaseParserParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(SqlBaseParserParser.SERDEPROPERTIES, 0); }
		public TerminalNode SESSION_USER() { return getToken(SqlBaseParserParser.SESSION_USER, 0); }
		public TerminalNode SET() { return getToken(SqlBaseParserParser.SET, 0); }
		public TerminalNode SETS() { return getToken(SqlBaseParserParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(SqlBaseParserParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(SqlBaseParserParser.SKEWED, 0); }
		public TerminalNode SOME() { return getToken(SqlBaseParserParser.SOME, 0); }
		public TerminalNode SORT() { return getToken(SqlBaseParserParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(SqlBaseParserParser.SORTED, 0); }
		public TerminalNode START() { return getToken(SqlBaseParserParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(SqlBaseParserParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(SqlBaseParserParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(SqlBaseParserParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(SqlBaseParserParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(SqlBaseParserParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(SqlBaseParserParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(SqlBaseParserParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(SqlBaseParserParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(SqlBaseParserParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLE() { return getToken(SqlBaseParserParser.TABLE, 0); }
		public TerminalNode TABLES() { return getToken(SqlBaseParserParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(SqlBaseParserParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(SqlBaseParserParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(SqlBaseParserParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(SqlBaseParserParser.TERMINATED, 0); }
		public TerminalNode THEN() { return getToken(SqlBaseParserParser.THEN, 0); }
		public TerminalNode TIME() { return getToken(SqlBaseParserParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(SqlBaseParserParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(SqlBaseParserParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(SqlBaseParserParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TO() { return getToken(SqlBaseParserParser.TO, 0); }
		public TerminalNode TOUCH() { return getToken(SqlBaseParserParser.TOUCH, 0); }
		public TerminalNode TRAILING() { return getToken(SqlBaseParserParser.TRAILING, 0); }
		public TerminalNode TRANSACTION() { return getToken(SqlBaseParserParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(SqlBaseParserParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(SqlBaseParserParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(SqlBaseParserParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(SqlBaseParserParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(SqlBaseParserParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(SqlBaseParserParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(SqlBaseParserParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(SqlBaseParserParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(SqlBaseParserParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(SqlBaseParserParser.UNCACHE, 0); }
		public TerminalNode UNIQUE() { return getToken(SqlBaseParserParser.UNIQUE, 0); }
		public TerminalNode UNKNOWN() { return getToken(SqlBaseParserParser.UNKNOWN, 0); }
		public TerminalNode UNLOCK() { return getToken(SqlBaseParserParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(SqlBaseParserParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(SqlBaseParserParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(SqlBaseParserParser.USE, 0); }
		public TerminalNode USER() { return getToken(SqlBaseParserParser.USER, 0); }
		public TerminalNode VALUES() { return getToken(SqlBaseParserParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(SqlBaseParserParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(SqlBaseParserParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(SqlBaseParserParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(SqlBaseParserParser.WEEK, 0); }
		public TerminalNode WHEN() { return getToken(SqlBaseParserParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(SqlBaseParserParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(SqlBaseParserParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(SqlBaseParserParser.WITH, 0); }
		public TerminalNode WITHIN() { return getToken(SqlBaseParserParser.WITHIN, 0); }
		public TerminalNode YEAR() { return getToken(SqlBaseParserParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(SqlBaseParserParser.ZONE, 0); }
		public NonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).enterNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlBaseParserListener ) ((SqlBaseParserListener)listener).exitNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlBaseParserVisitor ) return ((SqlBaseParserVisitor<? extends T>)visitor).visitNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonReservedContext nonReserved() throws RecognitionException {
		NonReservedContext _localctx = new NonReservedContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_nonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3286);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALL) | (1L << ALTER) | (1L << ANALYZE) | (1L << AND) | (1L << ANY) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << AS) | (1L << ASC) | (1L << AT) | (1L << AUTHORIZATION) | (1L << BETWEEN) | (1L << BOTH) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CASE) | (1L << CAST) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CHECK) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLATE) | (1L << COLLECTION) | (1L << COLUMN) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << CONSTRAINT) | (1L << COST) | (1L << CREATE) | (1L << CUBE) | (1L << CURRENT) | (1L << CURRENT_DATE) | (1L << CURRENT_TIME) | (1L << CURRENT_TIMESTAMP) | (1L << CURRENT_USER) | (1L << DAY) | (1L << DAYOFYEAR) | (1L << DATA) | (1L << DATABASE) | (1L << DATABASES))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTINCT - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ELSE - 64)) | (1L << (END - 64)) | (1L << (ESCAPE - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FALSE - 64)) | (1L << (FETCH - 64)) | (1L << (FIELDS - 64)) | (1L << (FILTER - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FOR - 64)) | (1L << (FOREIGN - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FROM - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GRANT - 64)) | (1L << (GROUP - 64)) | (1L << (GROUPING - 64)) | (1L << (HAVING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (IN - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)) | (1L << (INTO - 64)) | (1L << (IS - 64)) | (1L << (ITEMS - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (KEYS - 128)) | (1L << (LAST - 128)) | (1L << (LAZY - 128)) | (1L << (LEADING - 128)) | (1L << (LIKE - 128)) | (1L << (ILIKE - 128)) | (1L << (LIMIT - 128)) | (1L << (LINES - 128)) | (1L << (LIST - 128)) | (1L << (LOAD - 128)) | (1L << (LOCAL - 128)) | (1L << (LOCATION - 128)) | (1L << (LOCK - 128)) | (1L << (LOCKS - 128)) | (1L << (LOGICAL - 128)) | (1L << (MACRO - 128)) | (1L << (MAP - 128)) | (1L << (MATCHED - 128)) | (1L << (MERGE - 128)) | (1L << (MICROSECOND - 128)) | (1L << (MILLISECOND - 128)) | (1L << (MINUTE - 128)) | (1L << (MONTH - 128)) | (1L << (MSCK - 128)) | (1L << (NAMESPACE - 128)) | (1L << (NAMESPACES - 128)) | (1L << (NO - 128)) | (1L << (NOT - 128)) | (1L << (NULL - 128)) | (1L << (NULLS - 128)) | (1L << (OF - 128)) | (1L << (ONLY - 128)) | (1L << (OPTION - 128)) | (1L << (OPTIONS - 128)) | (1L << (OR - 128)) | (1L << (ORDER - 128)) | (1L << (OUT - 128)) | (1L << (OUTER - 128)) | (1L << (OUTPUTFORMAT - 128)) | (1L << (OVER - 128)) | (1L << (OVERLAPS - 128)) | (1L << (OVERLAY - 128)) | (1L << (OVERWRITE - 128)) | (1L << (PARTITION - 128)) | (1L << (PARTITIONED - 128)) | (1L << (PARTITIONS - 128)) | (1L << (PERCENTILE_CONT - 128)) | (1L << (PERCENTILE_DISC - 128)) | (1L << (PERCENTLIT - 128)) | (1L << (PIVOT - 128)) | (1L << (PLACING - 128)) | (1L << (POSITION - 128)) | (1L << (PRECEDING - 128)) | (1L << (PRIMARY - 128)) | (1L << (PRINCIPALS - 128)) | (1L << (PROPERTIES - 128)) | (1L << (PURGE - 128)) | (1L << (QUARTER - 128)) | (1L << (QUERY - 128)) | (1L << (RANGE - 128)))) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & ((1L << (RECORDREADER - 192)) | (1L << (RECORDWRITER - 192)) | (1L << (RECOVER - 192)) | (1L << (REDUCE - 192)) | (1L << (REFERENCES - 192)) | (1L << (REFRESH - 192)) | (1L << (RENAME - 192)) | (1L << (REPAIR - 192)) | (1L << (REPEATABLE - 192)) | (1L << (REPLACE - 192)) | (1L << (RESET - 192)) | (1L << (RESPECT - 192)) | (1L << (RESTRICT - 192)) | (1L << (REVOKE - 192)) | (1L << (RLIKE - 192)) | (1L << (ROLE - 192)) | (1L << (ROLES - 192)) | (1L << (ROLLBACK - 192)) | (1L << (ROLLUP - 192)) | (1L << (ROW - 192)) | (1L << (ROWS - 192)) | (1L << (SECOND - 192)) | (1L << (SCHEMA - 192)) | (1L << (SCHEMAS - 192)) | (1L << (SELECT - 192)) | (1L << (SEPARATED - 192)) | (1L << (SERDE - 192)) | (1L << (SERDEPROPERTIES - 192)) | (1L << (SESSION_USER - 192)) | (1L << (SET - 192)) | (1L << (SETS - 192)) | (1L << (SHOW - 192)) | (1L << (SKEWED - 192)) | (1L << (SOME - 192)) | (1L << (SORT - 192)) | (1L << (SORTED - 192)) | (1L << (START - 192)) | (1L << (STATISTICS - 192)) | (1L << (STORED - 192)) | (1L << (STRATIFY - 192)) | (1L << (STRUCT - 192)) | (1L << (SUBSTR - 192)) | (1L << (SUBSTRING - 192)) | (1L << (SYNC - 192)) | (1L << (SYSTEM_TIME - 192)) | (1L << (SYSTEM_VERSION - 192)) | (1L << (TABLE - 192)) | (1L << (TABLES - 192)) | (1L << (TABLESAMPLE - 192)) | (1L << (TBLPROPERTIES - 192)) | (1L << (TEMPORARY - 192)) | (1L << (TERMINATED - 192)) | (1L << (THEN - 192)) | (1L << (TIME - 192)) | (1L << (TIMESTAMP - 192)) | (1L << (TIMESTAMPADD - 192)) | (1L << (TIMESTAMPDIFF - 192)) | (1L << (TO - 192)) | (1L << (TOUCH - 192)) | (1L << (TRAILING - 192)) | (1L << (TRANSACTION - 192)))) != 0) || ((((_la - 256)) & ~0x3f) == 0 && ((1L << (_la - 256)) & ((1L << (TRANSACTIONS - 256)) | (1L << (TRANSFORM - 256)) | (1L << (TRIM - 256)) | (1L << (TRUE - 256)) | (1L << (TRUNCATE - 256)) | (1L << (TRY_CAST - 256)) | (1L << (TYPE - 256)) | (1L << (UNARCHIVE - 256)) | (1L << (UNBOUNDED - 256)) | (1L << (UNCACHE - 256)) | (1L << (UNIQUE - 256)) | (1L << (UNKNOWN - 256)) | (1L << (UNLOCK - 256)) | (1L << (UNSET - 256)) | (1L << (UPDATE - 256)) | (1L << (USE - 256)) | (1L << (USER - 256)) | (1L << (VALUES - 256)) | (1L << (VERSION - 256)) | (1L << (VIEW - 256)) | (1L << (VIEWS - 256)) | (1L << (WEEK - 256)) | (1L << (WHEN - 256)) | (1L << (WHERE - 256)) | (1L << (WINDOW - 256)) | (1L << (WITH - 256)) | (1L << (WITHIN - 256)) | (1L << (YEAR - 256)) | (1L << (ZONE - 256)))) != 0)) ) {
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
		case 43:
			return queryTerm_sempred((QueryTermContext)_localctx, predIndex);
		case 105:
			return booleanExpression_sempred((BooleanExpressionContext)_localctx, predIndex);
		case 107:
			return valueExpression_sempred((ValueExpressionContext)_localctx, predIndex);
		case 109:
			return primaryExpression_sempred((PrimaryExpressionContext)_localctx, predIndex);
		case 140:
			return identifier_sempred((IdentifierContext)_localctx, predIndex);
		case 141:
			return strictIdentifier_sempred((StrictIdentifierContext)_localctx, predIndex);
		case 143:
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

	private static final String _serializedATNSegment0 =
		"\u0004\u0001\u0144\u0cd9\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007"+
		"\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007"+
		"\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007"+
		",\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u0007"+
		"1\u00022\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u0007"+
		"6\u00027\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007"+
		";\u0002<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007"+
		"@\u0002A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007"+
		"E\u0002F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007"+
		"J\u0002K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007"+
		"O\u0002P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007"+
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007"+
		"Y\u0002Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007"+
		"^\u0002_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007"+
		"c\u0002d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007"+
		"h\u0002i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007"+
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0002r\u0007"+
		"r\u0002s\u0007s\u0002t\u0007t\u0002u\u0007u\u0002v\u0007v\u0002w\u0007"+
		"w\u0002x\u0007x\u0002y\u0007y\u0002z\u0007z\u0002{\u0007{\u0002|\u0007"+
		"|\u0002}\u0007}\u0002~\u0007~\u0002\u007f\u0007\u007f\u0002\u0080\u0007"+
		"\u0080\u0002\u0081\u0007\u0081\u0002\u0082\u0007\u0082\u0002\u0083\u0007"+
		"\u0083\u0002\u0084\u0007\u0084\u0002\u0085\u0007\u0085\u0002\u0086\u0007"+
		"\u0086\u0002\u0087\u0007\u0087\u0002\u0088\u0007\u0088\u0002\u0089\u0007"+
		"\u0089\u0002\u008a\u0007\u008a\u0002\u008b\u0007\u008b\u0002\u008c\u0007"+
		"\u008c\u0002\u008d\u0007\u008d\u0002\u008e\u0007\u008e\u0002\u008f\u0007"+
		"\u008f\u0002\u0090\u0007\u0090\u0002\u0091\u0007\u0091\u0002\u0092\u0007"+
		"\u0092\u0002\u0093\u0007\u0093\u0001\u0000\u0001\u0000\u0005\u0000\u012b"+
		"\b\u0000\n\u0000\f\u0000\u012e\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u0146\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007\u0153\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u015a\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007"+
		"\u0162\b\u0007\n\u0007\f\u0007\u0165\t\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0178\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u017c\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u0182\b\u0007\u0001\u0007\u0003\u0007\u0185\b\u0007"+
		"\u0001\u0007\u0003\u0007\u0188\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007\u018f\b\u0007\u0001\u0007\u0003\u0007"+
		"\u0192\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0196\b\u0007\u0001"+
		"\u0007\u0003\u0007\u0199\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u01a0\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0005\u0007\u01ab\b\u0007\n\u0007\f\u0007\u01ae\t\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u01b5\b\u0007"+
		"\u0001\u0007\u0003\u0007\u01b8\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u01bc\b\u0007\u0001\u0007\u0003\u0007\u01bf\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u01c5\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u01d0\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u01d6\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u01db\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u01fd"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u020a\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u0223\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u022c\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0230\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0236\b\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u023a\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u023f"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0245"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0251"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u0259\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u025f\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u026c\b\u0007\u0001\u0007\u0004\u0007\u026f"+
		"\b\u0007\u000b\u0007\f\u0007\u0270\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u0281\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0286\b"+
		"\u0007\n\u0007\f\u0007\u0289\t\u0007\u0001\u0007\u0003\u0007\u028c\b\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0292\b\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u02a1\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u02a5\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u02ab\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u02b1\b\u0007\u0001\u0007\u0003\u0007\u02b4\b\u0007\u0001\u0007\u0003"+
		"\u0007\u02b7\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u02bd\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u02c1\b\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0005\u0007\u02c9\b\u0007\n\u0007\f\u0007\u02cc\t\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u02d4"+
		"\b\u0007\u0001\u0007\u0003\u0007\u02d7\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u02e0\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u02e5\b"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u02eb"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u02f2\b\u0007\u0001\u0007\u0003\u0007\u02f5\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u02fb\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0005\u0007\u0304\b\u0007\n\u0007\f\u0007\u0307\t\u0007\u0003\u0007\u0309"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u030d\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007\u0312\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u0317\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007\u031e\b\u0007\u0001\u0007\u0003\u0007"+
		"\u0321\b\u0007\u0001\u0007\u0003\u0007\u0324\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u032b\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0330\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u0339\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0341\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0347\b\u0007\u0001\u0007\u0003\u0007\u034a"+
		"\b\u0007\u0001\u0007\u0003\u0007\u034d\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007\u0353\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0003\u0007\u0357\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u035c\b\u0007\u0001\u0007\u0003\u0007\u035f\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u0363\b\u0007\u0003\u0007\u0365\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u036d\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u0375\b\u0007\u0001\u0007\u0003\u0007\u0378\b"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u037d\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0383\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0389\b\u0007\u0001"+
		"\u0007\u0003\u0007\u038c\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0390"+
		"\b\u0007\u0001\u0007\u0003\u0007\u0393\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0003\u0007\u0397\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007\u03b1\b\u0007\n\u0007\f\u0007\u03b4"+
		"\t\u0007\u0003\u0007\u03b6\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u03ba\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u03c0\b\u0007\u0001\u0007\u0003\u0007\u03c3\b\u0007\u0001\u0007\u0003"+
		"\u0007\u03c6\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u03cc\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u03d4\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u03d9\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u03df\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u03e5\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u03ed\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0005\u0007\u03f2\b\u0007\n\u0007\f\u0007\u03f5\t\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u03fa\b\u0007\n\u0007"+
		"\f\u0007\u03fd\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007\u040b\b\u0007\n\u0007\f\u0007\u040e"+
		"\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0419\b\u0007\n"+
		"\u0007\f\u0007\u041c\t\u0007\u0003\u0007\u041e\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0005\u0007\u0422\b\u0007\n\u0007\f\u0007\u0425\t\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u042b\b\u0007\n\u0007"+
		"\f\u0007\u042e\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0005\u0007\u0434\b\u0007\n\u0007\f\u0007\u0437\t\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u043e\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0443\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0448\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u044f\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0455\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u045a\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0005\u0007\u0460\b\u0007\n\u0007\f\u0007\u0463\t\u0007"+
		"\u0003\u0007\u0465\b\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0471\b\n\u0001\n\u0001\n\u0003"+
		"\n\u0475\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u047c\b\n"+
		"\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u04f0\b\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u04f8\b\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0500\b\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0509\b\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0513\b\n\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u0517\b\u000b\u0001\u000b\u0003\u000b\u051a\b"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0520"+
		"\b\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0003\f\u0526\b\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0003\r\u0532\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u053e"+
		"\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0543\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0003\u0011\u054c\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0554\b\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u055b\b\u0012"+
		"\u0003\u0012\u055d\b\u0012\u0001\u0012\u0003\u0012\u0560\b\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0565\b\u0012\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u0569\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003"+
		"\u0012\u056e\b\u0012\u0001\u0012\u0003\u0012\u0571\b\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u0576\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0003\u0012\u057b\b\u0012\u0001\u0012\u0003\u0012\u057e\b"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0583\b\u0012\u0001"+
		"\u0012\u0001\u0012\u0003\u0012\u0587\b\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u058c\b\u0012\u0003\u0012\u058e\b\u0012\u0001\u0013"+
		"\u0001\u0013\u0003\u0013\u0592\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0005\u0014\u0599\b\u0014\n\u0014\f\u0014\u059c"+
		"\t\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u05a3\b\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u05ae"+
		"\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u05b3\b\u0019"+
		"\n\u0019\f\u0019\u05b6\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0005\u001a\u05bc\b\u001a\n\u001a\f\u001a\u05bf\t\u001a\u0001\u001b"+
		"\u0001\u001b\u0003\u001b\u05c3\b\u001b\u0001\u001b\u0003\u001b\u05c6\b"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u05dc\b\u001d\n\u001d\f\u001d"+
		"\u05df\t\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e"+
		"\u05e5\b\u001e\n\u001e\f\u001e\u05e8\t\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001f\u0001\u001f\u0003\u001f\u05ee\b\u001f\u0001\u001f\u0003\u001f\u05f1"+
		"\b\u001f\u0001 \u0001 \u0001 \u0005 \u05f6\b \n \f \u05f9\t \u0001 \u0003"+
		" \u05fc\b \u0001!\u0001!\u0001!\u0001!\u0003!\u0602\b!\u0001\"\u0001\""+
		"\u0001\"\u0001\"\u0005\"\u0608\b\"\n\"\f\"\u060b\t\"\u0001\"\u0001\"\u0001"+
		"#\u0001#\u0001#\u0001#\u0005#\u0613\b#\n#\f#\u0616\t#\u0001#\u0001#\u0001"+
		"$\u0001$\u0001$\u0001$\u0001$\u0001$\u0003$\u0620\b$\u0001%\u0001%\u0001"+
		"%\u0001%\u0001%\u0003%\u0627\b%\u0001&\u0001&\u0001&\u0001&\u0003&\u062d"+
		"\b&\u0001\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0001(\u0001(\u0004("+
		"\u0637\b(\u000b(\f(\u0638\u0001(\u0001(\u0001(\u0001(\u0001(\u0003(\u0640"+
		"\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003(\u0647\b(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003(\u0653\b(\u0001"+
		"(\u0001(\u0001(\u0001(\u0005(\u0659\b(\n(\f(\u065c\t(\u0001(\u0005(\u065f"+
		"\b(\n(\f(\u0662\t(\u0003(\u0664\b(\u0001)\u0001)\u0001)\u0001)\u0001)"+
		"\u0005)\u066b\b)\n)\f)\u066e\t)\u0003)\u0670\b)\u0001)\u0001)\u0001)\u0001"+
		")\u0001)\u0005)\u0677\b)\n)\f)\u067a\t)\u0003)\u067c\b)\u0001)\u0001)"+
		"\u0001)\u0001)\u0001)\u0005)\u0683\b)\n)\f)\u0686\t)\u0003)\u0688\b)\u0001"+
		")\u0001)\u0001)\u0001)\u0001)\u0005)\u068f\b)\n)\f)\u0692\t)\u0003)\u0694"+
		"\b)\u0001)\u0003)\u0697\b)\u0001)\u0001)\u0001)\u0003)\u069c\b)\u0003"+
		")\u069e\b)\u0001*\u0001*\u0001*\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"+
		"+\u0001+\u0003+\u06aa\b+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u06b1"+
		"\b+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u06b8\b+\u0001+\u0005+\u06bb"+
		"\b+\n+\f+\u06be\t+\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001"+
		",\u0001,\u0003,\u06c9\b,\u0001-\u0001-\u0003-\u06cd\b-\u0001-\u0001-\u0003"+
		"-\u06d1\b-\u0001.\u0001.\u0004.\u06d5\b.\u000b.\f.\u06d6\u0001/\u0001"+
		"/\u0003/\u06db\b/\u0001/\u0001/\u0001/\u0001/\u0005/\u06e1\b/\n/\f/\u06e4"+
		"\t/\u0001/\u0003/\u06e7\b/\u0001/\u0003/\u06ea\b/\u0001/\u0003/\u06ed"+
		"\b/\u0001/\u0003/\u06f0\b/\u0001/\u0001/\u0003/\u06f4\b/\u00010\u0001"+
		"0\u00030\u06f8\b0\u00010\u00050\u06fb\b0\n0\f0\u06fe\t0\u00010\u00030"+
		"\u0701\b0\u00010\u00030\u0704\b0\u00010\u00030\u0707\b0\u00010\u00030"+
		"\u070a\b0\u00010\u00010\u00030\u070e\b0\u00010\u00050\u0711\b0\n0\f0\u0714"+
		"\t0\u00010\u00030\u0717\b0\u00010\u00030\u071a\b0\u00010\u00030\u071d"+
		"\b0\u00010\u00030\u0720\b0\u00030\u0722\b0\u00011\u00011\u00011\u0001"+
		"1\u00031\u0728\b1\u00011\u00011\u00011\u00011\u00011\u00031\u072f\b1\u0001"+
		"1\u00011\u00011\u00031\u0734\b1\u00011\u00031\u0737\b1\u00011\u00031\u073a"+
		"\b1\u00011\u00011\u00031\u073e\b1\u00011\u00011\u00011\u00011\u00011\u0001"+
		"1\u00011\u00011\u00031\u0748\b1\u00011\u00011\u00031\u074c\b1\u00031\u074e"+
		"\b1\u00011\u00031\u0751\b1\u00011\u00011\u00031\u0755\b1\u00012\u0001"+
		"2\u00052\u0759\b2\n2\f2\u075c\t2\u00012\u00032\u075f\b2\u00012\u00012"+
		"\u00013\u00013\u00013\u00014\u00014\u00014\u00014\u00034\u076a\b4\u0001"+
		"4\u00014\u00014\u00015\u00015\u00015\u00015\u00015\u00035\u0774\b5\u0001"+
		"5\u00015\u00015\u00016\u00016\u00016\u00016\u00016\u00016\u00016\u0003"+
		"6\u0780\b6\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u0001"+
		"7\u00017\u00017\u00057\u078d\b7\n7\f7\u0790\t7\u00017\u00017\u00037\u0794"+
		"\b7\u00018\u00018\u00018\u00058\u0799\b8\n8\f8\u079c\t8\u00019\u00019"+
		"\u00019\u00019\u0001:\u0001:\u0001:\u0001;\u0001;\u0001;\u0001<\u0001"+
		"<\u0001<\u0003<\u07ab\b<\u0001<\u0005<\u07ae\b<\n<\f<\u07b1\t<\u0001<"+
		"\u0001<\u0001=\u0001=\u0001=\u0001=\u0001=\u0001=\u0005=\u07bb\b=\n=\f"+
		"=\u07be\t=\u0001=\u0001=\u0003=\u07c2\b=\u0001>\u0001>\u0001>\u0001>\u0005"+
		">\u07c8\b>\n>\f>\u07cb\t>\u0001>\u0005>\u07ce\b>\n>\f>\u07d1\t>\u0001"+
		">\u0003>\u07d4\b>\u0001?\u0003?\u07d7\b?\u0001?\u0001?\u0001?\u0001?\u0001"+
		"?\u0003?\u07de\b?\u0001?\u0001?\u0001?\u0001?\u0003?\u07e4\b?\u0001@\u0001"+
		"@\u0001@\u0001@\u0001@\u0005@\u07eb\b@\n@\f@\u07ee\t@\u0001@\u0001@\u0001"+
		"@\u0001@\u0001@\u0005@\u07f5\b@\n@\f@\u07f8\t@\u0001@\u0001@\u0001@\u0001"+
		"@\u0001@\u0001@\u0001@\u0001@\u0001@\u0001@\u0005@\u0804\b@\n@\f@\u0807"+
		"\t@\u0001@\u0001@\u0003@\u080b\b@\u0003@\u080d\b@\u0001A\u0001A\u0003"+
		"A\u0811\bA\u0001B\u0001B\u0001B\u0001B\u0001B\u0005B\u0818\bB\nB\fB\u081b"+
		"\tB\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0005B\u0825"+
		"\bB\nB\fB\u0828\tB\u0001B\u0001B\u0003B\u082c\bB\u0001C\u0001C\u0003C"+
		"\u0830\bC\u0001D\u0001D\u0001D\u0001D\u0005D\u0836\bD\nD\fD\u0839\tD\u0003"+
		"D\u083b\bD\u0001D\u0001D\u0003D\u083f\bD\u0001E\u0001E\u0001E\u0001E\u0001"+
		"E\u0001E\u0001E\u0001E\u0001E\u0001E\u0005E\u084b\bE\nE\fE\u084e\tE\u0001"+
		"E\u0001E\u0001E\u0001F\u0001F\u0001F\u0001F\u0001F\u0005F\u0858\bF\nF"+
		"\fF\u085b\tF\u0001F\u0001F\u0003F\u085f\bF\u0001G\u0001G\u0003G\u0863"+
		"\bG\u0001G\u0003G\u0866\bG\u0001H\u0001H\u0001H\u0003H\u086b\bH\u0001"+
		"H\u0001H\u0001H\u0001H\u0001H\u0005H\u0872\bH\nH\fH\u0875\tH\u0003H\u0877"+
		"\bH\u0001H\u0001H\u0001H\u0003H\u087c\bH\u0001H\u0001H\u0001H\u0005H\u0881"+
		"\bH\nH\fH\u0884\tH\u0003H\u0886\bH\u0001I\u0001I\u0001J\u0003J\u088b\b"+
		"J\u0001J\u0001J\u0005J\u088f\bJ\nJ\fJ\u0892\tJ\u0001K\u0001K\u0001K\u0003"+
		"K\u0897\bK\u0001K\u0001K\u0003K\u089b\bK\u0001K\u0001K\u0001K\u0001K\u0003"+
		"K\u08a1\bK\u0001K\u0001K\u0003K\u08a5\bK\u0001L\u0003L\u08a8\bL\u0001"+
		"L\u0001L\u0001L\u0003L\u08ad\bL\u0001L\u0003L\u08b0\bL\u0001L\u0001L\u0001"+
		"L\u0003L\u08b5\bL\u0001L\u0001L\u0003L\u08b9\bL\u0001L\u0003L\u08bc\b"+
		"L\u0001L\u0003L\u08bf\bL\u0001M\u0001M\u0001M\u0001M\u0003M\u08c5\bM\u0001"+
		"N\u0001N\u0001N\u0003N\u08ca\bN\u0001N\u0001N\u0001N\u0001N\u0001N\u0003"+
		"N\u08d1\bN\u0001O\u0003O\u08d4\bO\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0003O\u08e6\bO\u0003O\u08e8\bO\u0001O\u0003O\u08eb\bO\u0001P\u0001"+
		"P\u0001P\u0001P\u0001Q\u0001Q\u0001Q\u0005Q\u08f4\bQ\nQ\fQ\u08f7\tQ\u0001"+
		"R\u0001R\u0001R\u0001R\u0005R\u08fd\bR\nR\fR\u0900\tR\u0001R\u0001R\u0001"+
		"S\u0001S\u0003S\u0906\bS\u0001T\u0001T\u0001T\u0001T\u0005T\u090c\bT\n"+
		"T\fT\u090f\tT\u0001T\u0001T\u0001U\u0001U\u0003U\u0915\bU\u0001V\u0001"+
		"V\u0003V\u0919\bV\u0001V\u0003V\u091c\bV\u0001V\u0001V\u0001V\u0001V\u0001"+
		"V\u0001V\u0003V\u0924\bV\u0001V\u0001V\u0001V\u0001V\u0001V\u0001V\u0003"+
		"V\u092c\bV\u0001V\u0001V\u0001V\u0001V\u0003V\u0932\bV\u0001W\u0001W\u0001"+
		"W\u0001W\u0005W\u0938\bW\nW\fW\u093b\tW\u0001W\u0001W\u0001X\u0001X\u0001"+
		"X\u0001X\u0001X\u0005X\u0944\bX\nX\fX\u0947\tX\u0003X\u0949\bX\u0001X"+
		"\u0001X\u0001X\u0001Y\u0003Y\u094f\bY\u0001Y\u0001Y\u0003Y\u0953\bY\u0003"+
		"Y\u0955\bY\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u095e"+
		"\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001"+
		"Z\u0003Z\u096a\bZ\u0003Z\u096c\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003"+
		"Z\u0973\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u097a\bZ\u0001Z\u0001"+
		"Z\u0001Z\u0001Z\u0003Z\u0980\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u0986"+
		"\bZ\u0003Z\u0988\bZ\u0001[\u0001[\u0001[\u0005[\u098d\b[\n[\f[\u0990\t"+
		"[\u0001\\\u0001\\\u0001\\\u0005\\\u0995\b\\\n\\\f\\\u0998\t\\\u0001]\u0001"+
		"]\u0001]\u0005]\u099d\b]\n]\f]\u09a0\t]\u0001^\u0001^\u0001^\u0003^\u09a5"+
		"\b^\u0001_\u0001_\u0001_\u0003_\u09aa\b_\u0001_\u0001_\u0001`\u0001`\u0001"+
		"`\u0003`\u09b1\b`\u0001`\u0001`\u0001a\u0001a\u0003a\u09b7\ba\u0001a\u0001"+
		"a\u0003a\u09bb\ba\u0003a\u09bd\ba\u0001b\u0001b\u0001b\u0005b\u09c2\b"+
		"b\nb\fb\u09c5\tb\u0001c\u0001c\u0001c\u0001c\u0005c\u09cb\bc\nc\fc\u09ce"+
		"\tc\u0001c\u0001c\u0001d\u0001d\u0003d\u09d4\bd\u0001e\u0001e\u0001e\u0001"+
		"e\u0001e\u0001e\u0005e\u09dc\be\ne\fe\u09df\te\u0001e\u0001e\u0003e\u09e3"+
		"\be\u0001f\u0001f\u0003f\u09e7\bf\u0001g\u0001g\u0001h\u0001h\u0001h\u0005"+
		"h\u09ee\bh\nh\fh\u09f1\th\u0001i\u0001i\u0001i\u0001i\u0001i\u0001i\u0001"+
		"i\u0001i\u0001i\u0001i\u0003i\u09fd\bi\u0003i\u09ff\bi\u0001i\u0001i\u0001"+
		"i\u0001i\u0001i\u0001i\u0005i\u0a07\bi\ni\fi\u0a0a\ti\u0001j\u0003j\u0a0d"+
		"\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0003j\u0a15\bj\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0005j\u0a1c\bj\nj\fj\u0a1f\tj\u0001j\u0001j\u0001"+
		"j\u0003j\u0a24\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0003j\u0a2c"+
		"\bj\u0001j\u0001j\u0001j\u0003j\u0a31\bj\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0005j\u0a3b\bj\nj\fj\u0a3e\tj\u0001j\u0001j\u0003"+
		"j\u0a42\bj\u0001j\u0003j\u0a45\bj\u0001j\u0001j\u0001j\u0001j\u0003j\u0a4b"+
		"\bj\u0001j\u0001j\u0003j\u0a4f\bj\u0001j\u0001j\u0001j\u0003j\u0a54\b"+
		"j\u0001j\u0001j\u0001j\u0003j\u0a59\bj\u0001j\u0001j\u0001j\u0003j\u0a5e"+
		"\bj\u0001k\u0001k\u0001k\u0001k\u0003k\u0a64\bk\u0001k\u0001k\u0001k\u0001"+
		"k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001"+
		"k\u0001k\u0001k\u0001k\u0001k\u0001k\u0005k\u0a79\bk\nk\fk\u0a7c\tk\u0001"+
		"l\u0001l\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0004m\u0a96\bm\u000bm\fm\u0a97\u0001m\u0001m\u0003"+
		"m\u0a9c\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0004m\u0aa3\bm\u000bm\f"+
		"m\u0aa4\u0001m\u0001m\u0003m\u0aa9\bm\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0005"+
		"m\u0ab9\bm\nm\fm\u0abc\tm\u0003m\u0abe\bm\u0001m\u0001m\u0001m\u0001m"+
		"\u0001m\u0001m\u0003m\u0ac6\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0003m\u0acf\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0004m\u0ae4\bm\u000bm\fm\u0ae5\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0003m\u0af1\bm\u0001m\u0001m\u0001"+
		"m\u0005m\u0af6\bm\nm\fm\u0af9\tm\u0003m\u0afb\bm\u0001m\u0001m\u0001m"+
		"\u0001m\u0001m\u0001m\u0001m\u0003m\u0b04\bm\u0001m\u0001m\u0003m\u0b08"+
		"\bm\u0001m\u0001m\u0003m\u0b0c\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0004m\u0b16\bm\u000bm\fm\u0b17\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0003"+
		"m\u0b31\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0003m\u0b38\bm\u0001m\u0003"+
		"m\u0b3b\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0003m\u0b4a\bm\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0003m\u0b5b\bm\u0003m\u0b5d\bm\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0005m\u0b67\bm\nm\fm\u0b6a\tm\u0001n\u0001n\u0001"+
		"n\u0001n\u0001n\u0001n\u0001n\u0001n\u0004n\u0b74\bn\u000bn\fn\u0b75\u0003"+
		"n\u0b78\bn\u0001o\u0001o\u0001p\u0001p\u0001q\u0001q\u0001r\u0001r\u0001"+
		"s\u0001s\u0001s\u0003s\u0b85\bs\u0001t\u0001t\u0003t\u0b89\bt\u0001u\u0001"+
		"u\u0001u\u0004u\u0b8e\bu\u000bu\fu\u0b8f\u0001v\u0001v\u0001v\u0003v\u0b95"+
		"\bv\u0001w\u0001w\u0001w\u0001w\u0001w\u0001x\u0003x\u0b9d\bx\u0001x\u0001"+
		"x\u0001y\u0001y\u0001y\u0003y\u0ba4\by\u0001z\u0001z\u0001z\u0001z\u0001"+
		"z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001"+
		"z\u0003z\u0bb5\bz\u0001z\u0001z\u0003z\u0bb9\bz\u0001z\u0001z\u0001z\u0001"+
		"z\u0003z\u0bbf\bz\u0001z\u0001z\u0001z\u0001z\u0003z\u0bc5\bz\u0001z\u0001"+
		"z\u0001z\u0001z\u0001z\u0005z\u0bcc\bz\nz\fz\u0bcf\tz\u0001z\u0003z\u0bd2"+
		"\bz\u0003z\u0bd4\bz\u0001{\u0001{\u0001{\u0005{\u0bd9\b{\n{\f{\u0bdc\t"+
		"{\u0001|\u0001|\u0001|\u0001|\u0003|\u0be2\b|\u0001|\u0003|\u0be5\b|\u0001"+
		"|\u0003|\u0be8\b|\u0001}\u0001}\u0001}\u0005}\u0bed\b}\n}\f}\u0bf0\t}"+
		"\u0001~\u0001~\u0001~\u0001~\u0003~\u0bf6\b~\u0001~\u0003~\u0bf9\b~\u0001"+
		"\u007f\u0001\u007f\u0001\u007f\u0005\u007f\u0bfe\b\u007f\n\u007f\f\u007f"+
		"\u0c01\t\u007f\u0001\u0080\u0001\u0080\u0003\u0080\u0c05\b\u0080\u0001"+
		"\u0080\u0001\u0080\u0001\u0080\u0003\u0080\u0c0a\b\u0080\u0001\u0080\u0003"+
		"\u0080\u0c0d\b\u0080\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0001"+
		"\u0081\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082\u0005\u0082\u0c18"+
		"\b\u0082\n\u0082\f\u0082\u0c1b\t\u0082\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084"+
		"\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084"+
		"\u0005\u0084\u0c2c\b\u0084\n\u0084\f\u0084\u0c2f\t\u0084\u0001\u0084\u0001"+
		"\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0005\u0084\u0c36\b\u0084\n"+
		"\u0084\f\u0084\u0c39\t\u0084\u0003\u0084\u0c3b\b\u0084\u0001\u0084\u0001"+
		"\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0005\u0084\u0c42\b\u0084\n"+
		"\u0084\f\u0084\u0c45\t\u0084\u0003\u0084\u0c47\b\u0084\u0003\u0084\u0c49"+
		"\b\u0084\u0001\u0084\u0003\u0084\u0c4c\b\u0084\u0001\u0084\u0003\u0084"+
		"\u0c4f\b\u0084\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085"+
		"\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085"+
		"\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0003\u0085"+
		"\u0c61\b\u0085\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086"+
		"\u0001\u0086\u0001\u0086\u0003\u0086\u0c6a\b\u0086\u0001\u0087\u0001\u0087"+
		"\u0001\u0087\u0005\u0087\u0c6f\b\u0087\n\u0087\f\u0087\u0c72\t\u0087\u0001"+
		"\u0088\u0001\u0088\u0001\u0088\u0001\u0088\u0003\u0088\u0c78\b\u0088\u0001"+
		"\u0089\u0001\u0089\u0001\u0089\u0005\u0089\u0c7d\b\u0089\n\u0089\f\u0089"+
		"\u0c80\t\u0089\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008b\u0001\u008b"+
		"\u0004\u008b\u0c87\b\u008b\u000b\u008b\f\u008b\u0c88\u0001\u008b\u0003"+
		"\u008b\u0c8c\b\u008b\u0001\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u0c91"+
		"\b\u008c\u0001\u008d\u0001\u008d\u0001\u008d\u0001\u008d\u0001\u008d\u0001"+
		"\u008d\u0003\u008d\u0c99\b\u008d\u0001\u008e\u0001\u008e\u0001\u008f\u0001"+
		"\u008f\u0003\u008f\u0c9f\b\u008f\u0001\u008f\u0001\u008f\u0001\u008f\u0003"+
		"\u008f\u0ca4\b\u008f\u0001\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0ca9"+
		"\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0cad\b\u008f\u0001\u008f"+
		"\u0001\u008f\u0003\u008f\u0cb1\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f"+
		"\u0cb5\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0cb9\b\u008f\u0001"+
		"\u008f\u0001\u008f\u0003\u008f\u0cbd\b\u008f\u0001\u008f\u0001\u008f\u0003"+
		"\u008f\u0cc1\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0cc5\b\u008f"+
		"\u0001\u008f\u0003\u008f\u0cc8\b\u008f\u0001\u0090\u0001\u0090\u0001\u0090"+
		"\u0001\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0003\u0090\u0cd1\b\u0090"+
		"\u0001\u0091\u0001\u0091\u0001\u0092\u0001\u0092\u0001\u0093\u0001\u0093"+
		"\u0001\u0093\t\u03b2\u03f3\u03fb\u040c\u041a\u0423\u042c\u0435\u0461\u0004"+
		"V\u00d2\u00d6\u00da\u0094\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0"+
		"\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8"+
		"\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0"+
		"\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108"+
		"\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118\u011a\u011c\u011e\u0120"+
		"\u0122\u0124\u0126\u0000;\u0002\u0000BB\u00bb\u00bb\u0002\u0000\u001c"+
		"\u001c\u00cc\u00cc\u0002\u0000ffss\u0001\u0000)*\u0002\u0000\u00f1\u00f1"+
		"\u0115\u0115\u0002\u0000\u000b\u000b!!\u0005\u0000&&22XXee\u0090\u0090"+
		"\u0001\u0000FG\u0002\u0000XXee\u0002\u0000\u009f\u009f\u0135\u0135\u0003"+
		"\u0000\b\bNN\u00ee\u00ee\u0002\u0000\b\b\u008a\u008a\u0002\u0000\u008c"+
		"\u008c\u0135\u0135\u0003\u0000>>\u009a\u009a\u00d7\u00d7\u0003\u0000?"+
		"?\u009b\u009b\u00d8\u00d8\u0004\u0000SSzz\u00e0\u00e0\u010a\u010a\u0003"+
		"\u0000SS\u00e0\u00e0\u010a\u010a\u0002\u0000\u0013\u0013FF\u0002\u0000"+
		"``\u0081\u0081\u0002\u0000\u00f0\u00f0\u0114\u0114\u0002\u0000\u0135\u0135"+
		"\u0139\u0139\u0002\u0000\u00ef\u00ef\u00f9\u00f9\u0002\u000055\u00d3\u00d3"+
		"\u0002\u0000\n\nKK\u0002\u0000\u0139\u0139\u013b\u013b\u0001\u0000\u0086"+
		"\u0087\u0003\u0000\n\n\u000f\u000f\u00e4\u00e4\u0003\u0000[[\u0103\u0103"+
		"\u010c\u010c\u0002\u0000\u0127\u0128\u012c\u012c\u0002\u0000MM\u0129\u012b"+
		"\u0002\u0000\u0127\u0128\u012f\u012f\u0007\u0000;<oo\u0095\u0098\u00bd"+
		"\u00bd\u00d6\u00d6\u0117\u0117\u011d\u011d\u0002\u0000779:\u0002\u0000"+
		"@@\u00fa\u00fa\u0002\u0000AA\u00fb\u00fb\u0002\u0000\u001e\u001e\u0105"+
		"\u0105\u0002\u0000qq\u00cb\u00cb\u0001\u0000\u00ec\u00ed\u0002\u0000\u0004"+
		"\u0004ff\u0002\u0000\u0004\u0004bb\u0003\u0000\u0017\u0017\u0084\u0084"+
		"\u00fe\u00fe\u0001\u0000\u00b2\u00b3\u0001\u0000\u011f\u0126\u0002\u0000"+
		"MM\u0127\u0130\u0004\u0000\r\rss\u009e\u009e\u00a6\u00a6\u0002\u0000["+
		"[\u0103\u0103\u0001\u0000\u0127\u0128\u0003\u0000\u0135\u0135\u0139\u0139"+
		"\u013b\u013b\u0002\u0000\u0098\u0098\u011d\u011d\u0004\u0000;;oo\u0097"+
		"\u0097\u00d6\u00d6\u0003\u0000oo\u0097\u0097\u00d6\u00d6\u0002\u0000L"+
		"L\u00af\u00af\u0002\u0000\u00a7\u00a7\u00e5\u00e5\u0002\u0000aa\u00b8"+
		"\u00b8\u0001\u0000\u013a\u013b\u0002\u0000NN\u00df\u00df3\u0000\b\t\u000b"+
		"\f\u000e\u000e\u0010\u0011\u0013\u0014\u0016\u0016\u0018\u001c\u001f!"+
		"#&((*02256;JLNRRTZ]]_adehjmmortuwy{{~~\u0080\u0081\u0083\u0083\u0086\u009b"+
		"\u009d\u009d\u00a0\u00a1\u00a4\u00a5\u00a8\u00a8\u00aa\u00ab\u00ad\u00b1"+
		"\u00b4\u00b8\u00ba\u00c3\u00c5\u00cd\u00cf\u00d8\u00da\u00dd\u00df\u00e3"+
		"\u00e5\u00f0\u00f2\u00f6\u00f9\u00fb\u00fd\u00fd\u00ff\u0109\u010d\u0110"+
		"\u0113\u0117\u011a\u011a\u011d\u011e\u0010\u0000\u000e\u000e44SSggvvz"+
		"z\u007f\u007f\u0082\u0082\u0085\u0085\u009c\u009c\u00a2\u00a2\u00ce\u00ce"+
		"\u00da\u00da\u00e0\u00e0\u010a\u010a\u0112\u0112\u0011\u0000\b\r\u000f"+
		"35RTfhuwy{~\u0080\u0081\u0083\u0084\u0086\u009b\u009d\u00a1\u00a3\u00cd"+
		"\u00cf\u00d9\u00db\u00df\u00e1\u0109\u010b\u0111\u0113\u011e\u0ed8\u0000"+
		"\u0128\u0001\u0000\u0000\u0000\u0002\u0131\u0001\u0000\u0000\u0000\u0004"+
		"\u0134\u0001\u0000\u0000\u0000\u0006\u0137\u0001\u0000\u0000\u0000\b\u013a"+
		"\u0001\u0000\u0000\u0000\n\u013d\u0001\u0000\u0000\u0000\f\u0140\u0001"+
		"\u0000\u0000\u0000\u000e\u0464\u0001\u0000\u0000\u0000\u0010\u0466\u0001"+
		"\u0000\u0000\u0000\u0012\u0468\u0001\u0000\u0000\u0000\u0014\u0512\u0001"+
		"\u0000\u0000\u0000\u0016\u0514\u0001\u0000\u0000\u0000\u0018\u0525\u0001"+
		"\u0000\u0000\u0000\u001a\u052b\u0001\u0000\u0000\u0000\u001c\u0537\u0001"+
		"\u0000\u0000\u0000\u001e\u0544\u0001\u0000\u0000\u0000 \u0547\u0001\u0000"+
		"\u0000\u0000\"\u054b\u0001\u0000\u0000\u0000$\u058d\u0001\u0000\u0000"+
		"\u0000&\u058f\u0001\u0000\u0000\u0000(\u0593\u0001\u0000\u0000\u0000*"+
		"\u059f\u0001\u0000\u0000\u0000,\u05a4\u0001\u0000\u0000\u0000.\u05a6\u0001"+
		"\u0000\u0000\u00000\u05ad\u0001\u0000\u0000\u00002\u05af\u0001\u0000\u0000"+
		"\u00004\u05b7\u0001\u0000\u0000\u00006\u05c0\u0001\u0000\u0000\u00008"+
		"\u05cb\u0001\u0000\u0000\u0000:\u05dd\u0001\u0000\u0000\u0000<\u05e0\u0001"+
		"\u0000\u0000\u0000>\u05eb\u0001\u0000\u0000\u0000@\u05fb\u0001\u0000\u0000"+
		"\u0000B\u0601\u0001\u0000\u0000\u0000D\u0603\u0001\u0000\u0000\u0000F"+
		"\u060e\u0001\u0000\u0000\u0000H\u061f\u0001\u0000\u0000\u0000J\u0626\u0001"+
		"\u0000\u0000\u0000L\u0628\u0001\u0000\u0000\u0000N\u062e\u0001\u0000\u0000"+
		"\u0000P\u0663\u0001\u0000\u0000\u0000R\u066f\u0001\u0000\u0000\u0000T"+
		"\u069f\u0001\u0000\u0000\u0000V\u06a2\u0001\u0000\u0000\u0000X\u06c8\u0001"+
		"\u0000\u0000\u0000Z\u06ca\u0001\u0000\u0000\u0000\\\u06d2\u0001\u0000"+
		"\u0000\u0000^\u06f3\u0001\u0000\u0000\u0000`\u0721\u0001\u0000\u0000\u0000"+
		"b\u0736\u0001\u0000\u0000\u0000d\u0756\u0001\u0000\u0000\u0000f\u0762"+
		"\u0001\u0000\u0000\u0000h\u0765\u0001\u0000\u0000\u0000j\u076e\u0001\u0000"+
		"\u0000\u0000l\u077f\u0001\u0000\u0000\u0000n\u0793\u0001\u0000\u0000\u0000"+
		"p\u0795\u0001\u0000\u0000\u0000r\u079d\u0001\u0000\u0000\u0000t\u07a1"+
		"\u0001\u0000\u0000\u0000v\u07a4\u0001\u0000\u0000\u0000x\u07a7\u0001\u0000"+
		"\u0000\u0000z\u07c1\u0001\u0000\u0000\u0000|\u07c3\u0001\u0000\u0000\u0000"+
		"~\u07e3\u0001\u0000\u0000\u0000\u0080\u080c\u0001\u0000\u0000\u0000\u0082"+
		"\u0810\u0001\u0000\u0000\u0000\u0084\u082b\u0001\u0000\u0000\u0000\u0086"+
		"\u082f\u0001\u0000\u0000\u0000\u0088\u083e\u0001\u0000\u0000\u0000\u008a"+
		"\u0840\u0001\u0000\u0000\u0000\u008c\u085e\u0001\u0000\u0000\u0000\u008e"+
		"\u0860\u0001\u0000\u0000\u0000\u0090\u0867\u0001\u0000\u0000\u0000\u0092"+
		"\u0887\u0001\u0000\u0000\u0000\u0094\u088a\u0001\u0000\u0000\u0000\u0096"+
		"\u08a4\u0001\u0000\u0000\u0000\u0098\u08be\u0001\u0000\u0000\u0000\u009a"+
		"\u08c4\u0001\u0000\u0000\u0000\u009c\u08c6\u0001\u0000\u0000\u0000\u009e"+
		"\u08ea\u0001\u0000\u0000\u0000\u00a0\u08ec\u0001\u0000\u0000\u0000\u00a2"+
		"\u08f0\u0001\u0000\u0000\u0000\u00a4\u08f8\u0001\u0000\u0000\u0000\u00a6"+
		"\u0903\u0001\u0000\u0000\u0000\u00a8\u0907\u0001\u0000\u0000\u0000\u00aa"+
		"\u0912\u0001\u0000\u0000\u0000\u00ac\u0931\u0001\u0000\u0000\u0000\u00ae"+
		"\u0933\u0001\u0000\u0000\u0000\u00b0\u093e\u0001\u0000\u0000\u0000\u00b2"+
		"\u0954\u0001\u0000\u0000\u0000\u00b4\u0987\u0001\u0000\u0000\u0000\u00b6"+
		"\u0989\u0001\u0000\u0000\u0000\u00b8\u0991\u0001\u0000\u0000\u0000\u00ba"+
		"\u0999\u0001\u0000\u0000\u0000\u00bc\u09a1\u0001\u0000\u0000\u0000\u00be"+
		"\u09a9\u0001\u0000\u0000\u0000\u00c0\u09b0\u0001\u0000\u0000\u0000\u00c2"+
		"\u09b4\u0001\u0000\u0000\u0000\u00c4\u09be\u0001\u0000\u0000\u0000\u00c6"+
		"\u09c6\u0001\u0000\u0000\u0000\u00c8\u09d3\u0001\u0000\u0000\u0000\u00ca"+
		"\u09e2\u0001\u0000\u0000\u0000\u00cc\u09e6\u0001\u0000\u0000\u0000\u00ce"+
		"\u09e8\u0001\u0000\u0000\u0000\u00d0\u09ea\u0001\u0000\u0000\u0000\u00d2"+
		"\u09fe\u0001\u0000\u0000\u0000\u00d4\u0a5d\u0001\u0000\u0000\u0000\u00d6"+
		"\u0a63\u0001\u0000\u0000\u0000\u00d8\u0a7d\u0001\u0000\u0000\u0000\u00da"+
		"\u0b5c\u0001\u0000\u0000\u0000\u00dc\u0b77\u0001\u0000\u0000\u0000\u00de"+
		"\u0b79\u0001\u0000\u0000\u0000\u00e0\u0b7b\u0001\u0000\u0000\u0000\u00e2"+
		"\u0b7d\u0001\u0000\u0000\u0000\u00e4\u0b7f\u0001\u0000\u0000\u0000\u00e6"+
		"\u0b81\u0001\u0000\u0000\u0000\u00e8\u0b86\u0001\u0000\u0000\u0000\u00ea"+
		"\u0b8d\u0001\u0000\u0000\u0000\u00ec\u0b91\u0001\u0000\u0000\u0000\u00ee"+
		"\u0b96\u0001\u0000\u0000\u0000\u00f0\u0b9c\u0001\u0000\u0000\u0000\u00f2"+
		"\u0ba3\u0001\u0000\u0000\u0000\u00f4\u0bd3\u0001\u0000\u0000\u0000\u00f6"+
		"\u0bd5\u0001\u0000\u0000\u0000\u00f8\u0bdd\u0001\u0000\u0000\u0000\u00fa"+
		"\u0be9\u0001\u0000\u0000\u0000\u00fc\u0bf1\u0001\u0000\u0000\u0000\u00fe"+
		"\u0bfa\u0001\u0000\u0000\u0000\u0100\u0c02\u0001\u0000\u0000\u0000\u0102"+
		"\u0c0e\u0001\u0000\u0000\u0000\u0104\u0c13\u0001\u0000\u0000\u0000\u0106"+
		"\u0c1c\u0001\u0000\u0000\u0000\u0108\u0c4e\u0001\u0000\u0000\u0000\u010a"+
		"\u0c60\u0001\u0000\u0000\u0000\u010c\u0c69\u0001\u0000\u0000\u0000\u010e"+
		"\u0c6b\u0001\u0000\u0000\u0000\u0110\u0c77\u0001\u0000\u0000\u0000\u0112"+
		"\u0c79\u0001\u0000\u0000\u0000\u0114\u0c81\u0001\u0000\u0000\u0000\u0116"+
		"\u0c8b\u0001\u0000\u0000\u0000\u0118\u0c90\u0001\u0000\u0000\u0000\u011a"+
		"\u0c98\u0001\u0000\u0000\u0000\u011c\u0c9a\u0001\u0000\u0000\u0000\u011e"+
		"\u0cc7\u0001\u0000\u0000\u0000\u0120\u0cd0\u0001\u0000\u0000\u0000\u0122"+
		"\u0cd2\u0001\u0000\u0000\u0000\u0124\u0cd4\u0001\u0000\u0000\u0000\u0126"+
		"\u0cd6\u0001\u0000\u0000\u0000\u0128\u012c\u0003\u000e\u0007\u0000\u0129"+
		"\u012b\u0005\u0001\u0000\u0000\u012a\u0129\u0001\u0000\u0000\u0000\u012b"+
		"\u012e\u0001\u0000\u0000\u0000\u012c\u012a\u0001\u0000\u0000\u0000\u012c"+
		"\u012d\u0001\u0000\u0000\u0000\u012d\u012f\u0001\u0000\u0000\u0000\u012e"+
		"\u012c\u0001\u0000\u0000\u0000\u012f\u0130\u0005\u0000\u0000\u0001\u0130"+
		"\u0001\u0001\u0000\u0000\u0000\u0131\u0132\u0003\u00c2a\u0000\u0132\u0133"+
		"\u0005\u0000\u0000\u0001\u0133\u0003\u0001\u0000\u0000\u0000\u0134\u0135"+
		"\u0003\u00be_\u0000\u0135\u0136\u0005\u0000\u0000\u0001\u0136\u0005\u0001"+
		"\u0000\u0000\u0000\u0137\u0138\u0003\u00b8\\\u0000\u0138\u0139\u0005\u0000"+
		"\u0000\u0001\u0139\u0007\u0001\u0000\u0000\u0000\u013a\u013b\u0003\u00c0"+
		"`\u0000\u013b\u013c\u0005\u0000\u0000\u0001\u013c\t\u0001\u0000\u0000"+
		"\u0000\u013d\u013e\u0003\u00f4z\u0000\u013e\u013f\u0005\u0000\u0000\u0001"+
		"\u013f\u000b\u0001\u0000\u0000\u0000\u0140\u0141\u0003\u00fa}\u0000\u0141"+
		"\u0142\u0005\u0000\u0000\u0001\u0142\r\u0001\u0000\u0000\u0000\u0143\u0465"+
		"\u0003\"\u0011\u0000\u0144\u0146\u00034\u001a\u0000\u0145\u0144\u0001"+
		"\u0000\u0000\u0000\u0145\u0146\u0001\u0000\u0000\u0000\u0146\u0147\u0001"+
		"\u0000\u0000\u0000\u0147\u0465\u0003P(\u0000\u0148\u0149\u0005\u0110\u0000"+
		"\u0000\u0149\u0465\u0003\u00b8\\\u0000\u014a\u014b\u0005\u0110\u0000\u0000"+
		"\u014b\u014c\u0003,\u0016\u0000\u014c\u014d\u0003\u00b8\\\u0000\u014d"+
		"\u0465\u0001\u0000\u0000\u0000\u014e\u014f\u0005\u00df\u0000\u0000\u014f"+
		"\u0152\u0005\u001f\u0000\u0000\u0150\u0153\u0003\u0118\u008c\u0000\u0151"+
		"\u0153\u0005\u0135\u0000\u0000\u0152\u0150\u0001\u0000\u0000\u0000\u0152"+
		"\u0151\u0001\u0000\u0000\u0000\u0153\u0465\u0001\u0000\u0000\u0000\u0154"+
		"\u0155\u00053\u0000\u0000\u0155\u0159\u0003,\u0016\u0000\u0156\u0157\u0005"+
		"p\u0000\u0000\u0157\u0158\u0005\u009e\u0000\u0000\u0158\u015a\u0005U\u0000"+
		"\u0000\u0159\u0156\u0001\u0000\u0000\u0000\u0159\u015a\u0001\u0000\u0000"+
		"\u0000\u015a\u015b\u0001\u0000\u0000\u0000\u015b\u0163\u0003\u00b8\\\u0000"+
		"\u015c\u0162\u0003 \u0010\u0000\u015d\u0162\u0003\u001e\u000f\u0000\u015e"+
		"\u015f\u0005\u011b\u0000\u0000\u015f\u0160\u0007\u0000\u0000\u0000\u0160"+
		"\u0162\u0003<\u001e\u0000\u0161\u015c\u0001\u0000\u0000\u0000\u0161\u015d"+
		"\u0001\u0000\u0000\u0000\u0161\u015e\u0001\u0000\u0000\u0000\u0162\u0165"+
		"\u0001\u0000\u0000\u0000\u0163\u0161\u0001\u0000\u0000\u0000\u0163\u0164"+
		"\u0001\u0000\u0000\u0000\u0164\u0465\u0001\u0000\u0000\u0000\u0165\u0163"+
		"\u0001\u0000\u0000\u0000\u0166\u0167\u0005\u000b\u0000\u0000\u0167\u0168"+
		"\u0003,\u0016\u0000\u0168\u0169\u0003\u00b8\\\u0000\u0169\u016a\u0005"+
		"\u00df\u0000\u0000\u016a\u016b\u0007\u0000\u0000\u0000\u016b\u016c\u0003"+
		"<\u001e\u0000\u016c\u0465\u0001\u0000\u0000\u0000\u016d\u016e\u0005\u000b"+
		"\u0000\u0000\u016e\u016f\u0003,\u0016\u0000\u016f\u0170\u0003\u00b8\\"+
		"\u0000\u0170\u0171\u0005\u00df\u0000\u0000\u0171\u0172\u0003\u001e\u000f"+
		"\u0000\u0172\u0465\u0001\u0000\u0000\u0000\u0173\u0174\u0005N\u0000\u0000"+
		"\u0174\u0177\u0003,\u0016\u0000\u0175\u0176\u0005p\u0000\u0000\u0176\u0178"+
		"\u0005U\u0000\u0000\u0177\u0175\u0001\u0000\u0000\u0000\u0177\u0178\u0001"+
		"\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000\u0000\u0179\u017b\u0003"+
		"\u00b8\\\u0000\u017a\u017c\u0007\u0001\u0000\u0000\u017b\u017a\u0001\u0000"+
		"\u0000\u0000\u017b\u017c\u0001\u0000\u0000\u0000\u017c\u0465\u0001\u0000"+
		"\u0000\u0000\u017d\u017e\u0005\u00e2\u0000\u0000\u017e\u0181\u0003.\u0017"+
		"\u0000\u017f\u0180\u0007\u0002\u0000\u0000\u0180\u0182\u0003\u00b8\\\u0000"+
		"\u0181\u017f\u0001\u0000\u0000\u0000\u0181\u0182\u0001\u0000\u0000\u0000"+
		"\u0182\u0187\u0001\u0000\u0000\u0000\u0183\u0185\u0005\u0086\u0000\u0000"+
		"\u0184\u0183\u0001\u0000\u0000\u0000\u0184\u0185\u0001\u0000\u0000\u0000"+
		"\u0185\u0186\u0001\u0000\u0000\u0000\u0186\u0188\u0005\u0135\u0000\u0000"+
		"\u0187\u0184\u0001\u0000\u0000\u0000\u0187\u0188\u0001\u0000\u0000\u0000"+
		"\u0188\u0465\u0001\u0000\u0000\u0000\u0189\u018e\u0003\u0016\u000b\u0000"+
		"\u018a\u018b\u0005\u0002\u0000\u0000\u018b\u018c\u0003\u00fa}\u0000\u018c"+
		"\u018d\u0005\u0003\u0000\u0000\u018d\u018f\u0001\u0000\u0000\u0000\u018e"+
		"\u018a\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000\u0000\u018f"+
		"\u0191\u0001\u0000\u0000\u0000\u0190\u0192\u00038\u001c\u0000\u0191\u0190"+
		"\u0001\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192\u0193"+
		"\u0001\u0000\u0000\u0000\u0193\u0198\u0003:\u001d\u0000\u0194\u0196\u0005"+
		"\u0012\u0000\u0000\u0195\u0194\u0001\u0000\u0000\u0000\u0195\u0196\u0001"+
		"\u0000\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u0199\u0003"+
		"\"\u0011\u0000\u0198\u0195\u0001\u0000\u0000\u0000\u0198\u0199\u0001\u0000"+
		"\u0000\u0000\u0199\u0465\u0001\u0000\u0000\u0000\u019a\u019b\u00053\u0000"+
		"\u0000\u019b\u019f\u0005\u00f1\u0000\u0000\u019c\u019d\u0005p\u0000\u0000"+
		"\u019d\u019e\u0005\u009e\u0000\u0000\u019e\u01a0\u0005U\u0000\u0000\u019f"+
		"\u019c\u0001\u0000\u0000\u0000\u019f\u01a0\u0001\u0000\u0000\u0000\u01a0"+
		"\u01a1\u0001\u0000\u0000\u0000\u01a1\u01a2\u0003\u00be_\u0000\u01a2\u01a3"+
		"\u0005\u0086\u0000\u0000\u01a3\u01ac\u0003\u00be_\u0000\u01a4\u01ab\u0003"+
		"8\u001c\u0000\u01a5\u01ab\u0003\u00b4Z\u0000\u01a6\u01ab\u0003H$\u0000"+
		"\u01a7\u01ab\u0003\u001e\u000f\u0000\u01a8\u01a9\u0005\u00f4\u0000\u0000"+
		"\u01a9\u01ab\u0003<\u001e\u0000\u01aa\u01a4\u0001\u0000\u0000\u0000\u01aa"+
		"\u01a5\u0001\u0000\u0000\u0000\u01aa\u01a6\u0001\u0000\u0000\u0000\u01aa"+
		"\u01a7\u0001\u0000\u0000\u0000\u01aa\u01a8\u0001\u0000\u0000\u0000\u01ab"+
		"\u01ae\u0001\u0000\u0000\u0000\u01ac\u01aa\u0001\u0000\u0000\u0000\u01ac"+
		"\u01ad\u0001\u0000\u0000\u0000\u01ad\u0465\u0001\u0000\u0000\u0000\u01ae"+
		"\u01ac\u0001\u0000\u0000\u0000\u01af\u01b4\u0003\u0018\f\u0000\u01b0\u01b1"+
		"\u0005\u0002\u0000\u0000\u01b1\u01b2\u0003\u00fa}\u0000\u01b2\u01b3\u0005"+
		"\u0003\u0000\u0000\u01b3\u01b5\u0001\u0000\u0000\u0000\u01b4\u01b0\u0001"+
		"\u0000\u0000\u0000\u01b4\u01b5\u0001\u0000\u0000\u0000\u01b5\u01b7\u0001"+
		"\u0000\u0000\u0000\u01b6\u01b8\u00038\u001c\u0000\u01b7\u01b6\u0001\u0000"+
		"\u0000\u0000\u01b7\u01b8\u0001\u0000\u0000\u0000\u01b8\u01b9\u0001\u0000"+
		"\u0000\u0000\u01b9\u01be\u0003:\u001d\u0000\u01ba\u01bc\u0005\u0012\u0000"+
		"\u0000\u01bb\u01ba\u0001\u0000\u0000\u0000\u01bb\u01bc\u0001\u0000\u0000"+
		"\u0000\u01bc\u01bd\u0001\u0000\u0000\u0000\u01bd\u01bf\u0003\"\u0011\u0000"+
		"\u01be\u01bb\u0001\u0000\u0000\u0000\u01be\u01bf\u0001\u0000\u0000\u0000"+
		"\u01bf\u0465\u0001\u0000\u0000\u0000\u01c0\u01c1\u0005\f\u0000\u0000\u01c1"+
		"\u01c2\u0005\u00f1\u0000\u0000\u01c2\u01c4\u0003\u00b8\\\u0000\u01c3\u01c5"+
		"\u0003(\u0014\u0000\u01c4\u01c3\u0001\u0000\u0000\u0000\u01c4\u01c5\u0001"+
		"\u0000\u0000\u0000\u01c5\u01c6\u0001\u0000\u0000\u0000\u01c6\u01c7\u0005"+
		"/\u0000\u0000\u01c7\u01cf\u0005\u00e8\u0000\u0000\u01c8\u01d0\u0003\u0118"+
		"\u008c\u0000\u01c9\u01ca\u0005b\u0000\u0000\u01ca\u01cb\u0005*\u0000\u0000"+
		"\u01cb\u01d0\u0003\u00a2Q\u0000\u01cc\u01cd\u0005b\u0000\u0000\u01cd\u01ce"+
		"\u0005\n\u0000\u0000\u01ce\u01d0\u0005*\u0000\u0000\u01cf\u01c8\u0001"+
		"\u0000\u0000\u0000\u01cf\u01c9\u0001\u0000\u0000\u0000\u01cf\u01cc\u0001"+
		"\u0000\u0000\u0000\u01cf\u01d0\u0001\u0000\u0000\u0000\u01d0\u0465\u0001"+
		"\u0000\u0000\u0000\u01d1\u01d2\u0005\f\u0000\u0000\u01d2\u01d5\u0005\u00f2"+
		"\u0000\u0000\u01d3\u01d4\u0007\u0002\u0000\u0000\u01d4\u01d6\u0003\u00b8"+
		"\\\u0000\u01d5\u01d3\u0001\u0000\u0000\u0000\u01d5\u01d6\u0001\u0000\u0000"+
		"\u0000\u01d6\u01d7\u0001\u0000\u0000\u0000\u01d7\u01d8\u0005/\u0000\u0000"+
		"\u01d8\u01da\u0005\u00e8\u0000\u0000\u01d9\u01db\u0003\u0118\u008c\u0000"+
		"\u01da\u01d9\u0001\u0000\u0000\u0000\u01da\u01db\u0001\u0000\u0000\u0000"+
		"\u01db\u0465\u0001\u0000\u0000\u0000\u01dc\u01dd\u0005\u000b\u0000\u0000"+
		"\u01dd\u01de\u0005\u00f1\u0000\u0000\u01de\u01df\u0003\u00b8\\\u0000\u01df"+
		"\u01e0\u0005\b\u0000\u0000\u01e0\u01e1\u0007\u0003\u0000\u0000\u01e1\u01e2"+
		"\u0003\u00f6{\u0000\u01e2\u0465\u0001\u0000\u0000\u0000\u01e3\u01e4\u0005"+
		"\u000b\u0000\u0000\u01e4\u01e5\u0005\u00f1\u0000\u0000\u01e5\u01e6\u0003"+
		"\u00b8\\\u0000\u01e6\u01e7\u0005\b\u0000\u0000\u01e7\u01e8\u0007\u0003"+
		"\u0000\u0000\u01e8\u01e9\u0005\u0002\u0000\u0000\u01e9\u01ea\u0003\u00f6"+
		"{\u0000\u01ea\u01eb\u0005\u0003\u0000\u0000\u01eb\u0465\u0001\u0000\u0000"+
		"\u0000\u01ec\u01ed\u0005\u000b\u0000\u0000\u01ed\u01ee\u0005\u00f1\u0000"+
		"\u0000\u01ee\u01ef\u0003\u00b8\\\u0000\u01ef\u01f0\u0005\u00c6\u0000\u0000"+
		"\u01f0\u01f1\u0005)\u0000\u0000\u01f1\u01f2\u0003\u00b8\\\u0000\u01f2"+
		"\u01f3\u0005\u00fc\u0000\u0000\u01f3\u01f4\u0003\u0114\u008a\u0000\u01f4"+
		"\u0465\u0001\u0000\u0000\u0000\u01f5\u01f6\u0005\u000b\u0000\u0000\u01f6"+
		"\u01f7\u0005\u00f1\u0000\u0000\u01f7\u01f8\u0003\u00b8\\\u0000\u01f8\u01f9"+
		"\u0005N\u0000\u0000\u01f9\u01fc\u0007\u0003\u0000\u0000\u01fa\u01fb\u0005"+
		"p\u0000\u0000\u01fb\u01fd\u0005U\u0000\u0000\u01fc\u01fa\u0001\u0000\u0000"+
		"\u0000\u01fc\u01fd\u0001\u0000\u0000\u0000\u01fd\u01fe\u0001\u0000\u0000"+
		"\u0000\u01fe\u01ff\u0005\u0002\u0000\u0000\u01ff\u0200\u0003\u00b6[\u0000"+
		"\u0200\u0201\u0005\u0003\u0000\u0000\u0201\u0465\u0001\u0000\u0000\u0000"+
		"\u0202\u0203\u0005\u000b\u0000\u0000\u0203\u0204\u0005\u00f1\u0000\u0000"+
		"\u0204\u0205\u0003\u00b8\\\u0000\u0205\u0206\u0005N\u0000\u0000\u0206"+
		"\u0209\u0007\u0003\u0000\u0000\u0207\u0208\u0005p\u0000\u0000\u0208\u020a"+
		"\u0005U\u0000\u0000\u0209\u0207\u0001\u0000\u0000\u0000\u0209\u020a\u0001"+
		"\u0000\u0000\u0000\u020a\u020b\u0001\u0000\u0000\u0000\u020b\u020c\u0003"+
		"\u00b6[\u0000\u020c\u0465\u0001\u0000\u0000\u0000\u020d\u020e\u0005\u000b"+
		"\u0000\u0000\u020e\u020f\u0007\u0004\u0000\u0000\u020f\u0210\u0003\u00b8"+
		"\\\u0000\u0210\u0211\u0005\u00c6\u0000\u0000\u0211\u0212\u0005\u00fc\u0000"+
		"\u0000\u0212\u0213\u0003\u00b8\\\u0000\u0213\u0465\u0001\u0000\u0000\u0000"+
		"\u0214\u0215\u0005\u000b\u0000\u0000\u0215\u0216\u0007\u0004\u0000\u0000"+
		"\u0216\u0217\u0003\u00b8\\\u0000\u0217\u0218\u0005\u00df\u0000\u0000\u0218"+
		"\u0219\u0005\u00f4\u0000\u0000\u0219\u021a\u0003<\u001e\u0000\u021a\u0465"+
		"\u0001\u0000\u0000\u0000\u021b\u021c\u0005\u000b\u0000\u0000\u021c\u021d"+
		"\u0007\u0004\u0000\u0000\u021d\u021e\u0003\u00b8\\\u0000\u021e\u021f\u0005"+
		"\u010e\u0000\u0000\u021f\u0222\u0005\u00f4\u0000\u0000\u0220\u0221\u0005"+
		"p\u0000\u0000\u0221\u0223\u0005U\u0000\u0000\u0222\u0220\u0001\u0000\u0000"+
		"\u0000\u0222\u0223\u0001\u0000\u0000\u0000\u0223\u0224\u0001\u0000\u0000"+
		"\u0000\u0224\u0225\u0003<\u001e\u0000\u0225\u0465\u0001\u0000\u0000\u0000"+
		"\u0226\u0227\u0005\u000b\u0000\u0000\u0227\u0228\u0005\u00f1\u0000\u0000"+
		"\u0228\u0229\u0003\u00b8\\\u0000\u0229\u022b\u0007\u0005\u0000\u0000\u022a"+
		"\u022c\u0005)\u0000\u0000\u022b\u022a\u0001\u0000\u0000\u0000\u022b\u022c"+
		"\u0001\u0000\u0000\u0000\u022c\u022d\u0001\u0000\u0000\u0000\u022d\u022f"+
		"\u0003\u00b8\\\u0000\u022e\u0230\u0003\u0120\u0090\u0000\u022f\u022e\u0001"+
		"\u0000\u0000\u0000\u022f\u0230\u0001\u0000\u0000\u0000\u0230\u0465\u0001"+
		"\u0000\u0000\u0000\u0231\u0232\u0005\u000b\u0000\u0000\u0232\u0233\u0005"+
		"\u00f1\u0000\u0000\u0233\u0235\u0003\u00b8\\\u0000\u0234\u0236\u0003("+
		"\u0014\u0000\u0235\u0234\u0001\u0000\u0000\u0000\u0235\u0236\u0001\u0000"+
		"\u0000\u0000\u0236\u0237\u0001\u0000\u0000\u0000\u0237\u0239\u0005!\u0000"+
		"\u0000\u0238\u023a\u0005)\u0000\u0000\u0239\u0238\u0001\u0000\u0000\u0000"+
		"\u0239\u023a\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000"+
		"\u023b\u023c\u0003\u00b8\\\u0000\u023c\u023e\u0003\u00fc~\u0000\u023d"+
		"\u023f\u0003\u00f2y\u0000\u023e\u023d\u0001\u0000\u0000\u0000\u023e\u023f"+
		"\u0001\u0000\u0000\u0000\u023f\u0465\u0001\u0000\u0000\u0000\u0240\u0241"+
		"\u0005\u000b\u0000\u0000\u0241\u0242\u0005\u00f1\u0000\u0000\u0242\u0244"+
		"\u0003\u00b8\\\u0000\u0243\u0245\u0003(\u0014\u0000\u0244\u0243\u0001"+
		"\u0000\u0000\u0000\u0244\u0245\u0001\u0000\u0000\u0000\u0245\u0246\u0001"+
		"\u0000\u0000\u0000\u0246\u0247\u0005\u00c9\u0000\u0000\u0247\u0248\u0005"+
		"*\u0000\u0000\u0248\u0249\u0005\u0002\u0000\u0000\u0249\u024a\u0003\u00f6"+
		"{\u0000\u024a\u024b\u0005\u0003\u0000\u0000\u024b\u0465\u0001\u0000\u0000"+
		"\u0000\u024c\u024d\u0005\u000b\u0000\u0000\u024d\u024e\u0005\u00f1\u0000"+
		"\u0000\u024e\u0250\u0003\u00b8\\\u0000\u024f\u0251\u0003(\u0014\u0000"+
		"\u0250\u024f\u0001\u0000\u0000\u0000\u0250\u0251\u0001\u0000\u0000\u0000"+
		"\u0251\u0252\u0001\u0000\u0000\u0000\u0252\u0253\u0005\u00df\u0000\u0000"+
		"\u0253\u0254\u0005\u00dc\u0000\u0000\u0254\u0258\u0005\u0135\u0000\u0000"+
		"\u0255\u0256\u0005\u011b\u0000\u0000\u0256\u0257\u0005\u00dd\u0000\u0000"+
		"\u0257\u0259\u0003<\u001e\u0000\u0258\u0255\u0001\u0000\u0000\u0000\u0258"+
		"\u0259\u0001\u0000\u0000\u0000\u0259\u0465\u0001\u0000\u0000\u0000\u025a"+
		"\u025b\u0005\u000b\u0000\u0000\u025b\u025c\u0005\u00f1\u0000\u0000\u025c"+
		"\u025e\u0003\u00b8\\\u0000\u025d\u025f\u0003(\u0014\u0000\u025e\u025d"+
		"\u0001\u0000\u0000\u0000\u025e\u025f\u0001\u0000\u0000\u0000\u025f\u0260"+
		"\u0001\u0000\u0000\u0000\u0260\u0261\u0005\u00df\u0000\u0000\u0261\u0262"+
		"\u0005\u00dd\u0000\u0000\u0262\u0263\u0003<\u001e\u0000\u0263\u0465\u0001"+
		"\u0000\u0000\u0000\u0264\u0265\u0005\u000b\u0000\u0000\u0265\u0266\u0007"+
		"\u0004\u0000\u0000\u0266\u0267\u0003\u00b8\\\u0000\u0267\u026b\u0005\b"+
		"\u0000\u0000\u0268\u0269\u0005p\u0000\u0000\u0269\u026a\u0005\u009e\u0000"+
		"\u0000\u026a\u026c\u0005U\u0000\u0000\u026b\u0268\u0001\u0000\u0000\u0000"+
		"\u026b\u026c\u0001\u0000\u0000\u0000\u026c\u026e\u0001\u0000\u0000\u0000"+
		"\u026d\u026f\u0003&\u0013\u0000\u026e\u026d\u0001\u0000\u0000\u0000\u026f"+
		"\u0270\u0001\u0000\u0000\u0000\u0270\u026e\u0001\u0000\u0000\u0000\u0270"+
		"\u0271\u0001\u0000\u0000\u0000\u0271\u0465\u0001\u0000\u0000\u0000\u0272"+
		"\u0273\u0005\u000b\u0000\u0000\u0273\u0274\u0005\u00f1\u0000\u0000\u0274"+
		"\u0275\u0003\u00b8\\\u0000\u0275\u0276\u0003(\u0014\u0000\u0276\u0277"+
		"\u0005\u00c6\u0000\u0000\u0277\u0278\u0005\u00fc\u0000\u0000\u0278\u0279"+
		"\u0003(\u0014\u0000\u0279\u0465\u0001\u0000\u0000\u0000\u027a\u027b\u0005"+
		"\u000b\u0000\u0000\u027b\u027c\u0007\u0004\u0000\u0000\u027c\u027d\u0003"+
		"\u00b8\\\u0000\u027d\u0280\u0005N\u0000\u0000\u027e\u027f\u0005p\u0000"+
		"\u0000\u027f\u0281\u0005U\u0000\u0000\u0280\u027e\u0001\u0000\u0000\u0000"+
		"\u0280\u0281\u0001\u0000\u0000\u0000\u0281\u0282\u0001\u0000\u0000\u0000"+
		"\u0282\u0287\u0003(\u0014\u0000\u0283\u0284\u0005\u0004\u0000\u0000\u0284"+
		"\u0286\u0003(\u0014\u0000\u0285\u0283\u0001\u0000\u0000\u0000\u0286\u0289"+
		"\u0001\u0000\u0000\u0000\u0287\u0285\u0001\u0000\u0000\u0000\u0287\u0288"+
		"\u0001\u0000\u0000\u0000\u0288\u028b\u0001\u0000\u0000\u0000\u0289\u0287"+
		"\u0001\u0000\u0000\u0000\u028a\u028c\u0005\u00bc\u0000\u0000\u028b\u028a"+
		"\u0001\u0000\u0000\u0000\u028b\u028c\u0001\u0000\u0000\u0000\u028c\u0465"+
		"\u0001\u0000\u0000\u0000\u028d\u028e\u0005\u000b\u0000\u0000\u028e\u028f"+
		"\u0005\u00f1\u0000\u0000\u028f\u0291\u0003\u00b8\\\u0000\u0290\u0292\u0003"+
		"(\u0014\u0000\u0291\u0290\u0001\u0000\u0000\u0000\u0291\u0292\u0001\u0000"+
		"\u0000\u0000\u0292\u0293\u0001\u0000\u0000\u0000\u0293\u0294\u0005\u00df"+
		"\u0000\u0000\u0294\u0295\u0003\u001e\u000f\u0000\u0295\u0465\u0001\u0000"+
		"\u0000\u0000\u0296\u0297\u0005\u000b\u0000\u0000\u0297\u0298\u0005\u00f1"+
		"\u0000\u0000\u0298\u0299\u0003\u00b8\\\u0000\u0299\u029a\u0005\u00c2\u0000"+
		"\u0000\u029a\u029b\u0005\u00b1\u0000\u0000\u029b\u0465\u0001\u0000\u0000"+
		"\u0000\u029c\u029d\u0005N\u0000\u0000\u029d\u02a0\u0005\u00f1\u0000\u0000"+
		"\u029e\u029f\u0005p\u0000\u0000\u029f\u02a1\u0005U\u0000\u0000\u02a0\u029e"+
		"\u0001\u0000\u0000\u0000\u02a0\u02a1\u0001\u0000\u0000\u0000\u02a1\u02a2"+
		"\u0001\u0000\u0000\u0000\u02a2\u02a4\u0003\u00b8\\\u0000\u02a3\u02a5\u0005"+
		"\u00bc\u0000\u0000\u02a4\u02a3\u0001\u0000\u0000\u0000\u02a4\u02a5\u0001"+
		"\u0000\u0000\u0000\u02a5\u0465\u0001\u0000\u0000\u0000\u02a6\u02a7\u0005"+
		"N\u0000\u0000\u02a7\u02aa\u0005\u0115\u0000\u0000\u02a8\u02a9\u0005p\u0000"+
		"\u0000\u02a9\u02ab\u0005U\u0000\u0000\u02aa\u02a8\u0001\u0000\u0000\u0000"+
		"\u02aa\u02ab\u0001\u0000\u0000\u0000\u02ab\u02ac\u0001\u0000\u0000\u0000"+
		"\u02ac\u0465\u0003\u00b8\\\u0000\u02ad\u02b0\u00053\u0000\u0000\u02ae"+
		"\u02af\u0005\u00a6\u0000\u0000\u02af\u02b1\u0005\u00c9\u0000\u0000\u02b0"+
		"\u02ae\u0001\u0000\u0000\u0000\u02b0\u02b1\u0001\u0000\u0000\u0000\u02b1"+
		"\u02b6\u0001\u0000\u0000\u0000\u02b2\u02b4\u0005j\u0000\u0000\u02b3\u02b2"+
		"\u0001\u0000\u0000\u0000\u02b3\u02b4\u0001\u0000\u0000\u0000\u02b4\u02b5"+
		"\u0001\u0000\u0000\u0000\u02b5\u02b7\u0005\u00f5\u0000\u0000\u02b6\u02b3"+
		"\u0001\u0000\u0000\u0000\u02b6\u02b7\u0001\u0000\u0000\u0000\u02b7\u02b8"+
		"\u0001\u0000\u0000\u0000\u02b8\u02bc\u0005\u0115\u0000\u0000\u02b9\u02ba"+
		"\u0005p\u0000\u0000\u02ba\u02bb\u0005\u009e\u0000\u0000\u02bb\u02bd\u0005"+
		"U\u0000\u0000\u02bc\u02b9\u0001\u0000\u0000\u0000\u02bc\u02bd\u0001\u0000"+
		"\u0000\u0000\u02bd\u02be\u0001\u0000\u0000\u0000\u02be\u02c0\u0003\u00b8"+
		"\\\u0000\u02bf\u02c1\u0003\u00a8T\u0000\u02c0\u02bf\u0001\u0000\u0000"+
		"\u0000\u02c0\u02c1\u0001\u0000\u0000\u0000\u02c1\u02ca\u0001\u0000\u0000"+
		"\u0000\u02c2\u02c9\u0003 \u0010\u0000\u02c3\u02c4\u0005\u00b0\u0000\u0000"+
		"\u02c4\u02c5\u0005\u00a2\u0000\u0000\u02c5\u02c9\u0003\u00a0P\u0000\u02c6"+
		"\u02c7\u0005\u00f4\u0000\u0000\u02c7\u02c9\u0003<\u001e\u0000\u02c8\u02c2"+
		"\u0001\u0000\u0000\u0000\u02c8\u02c3\u0001\u0000\u0000\u0000\u02c8\u02c6"+
		"\u0001\u0000\u0000\u0000\u02c9\u02cc\u0001\u0000\u0000\u0000\u02ca\u02c8"+
		"\u0001\u0000\u0000\u0000\u02ca\u02cb\u0001\u0000\u0000\u0000\u02cb\u02cd"+
		"\u0001\u0000\u0000\u0000\u02cc\u02ca\u0001\u0000\u0000\u0000\u02cd\u02ce"+
		"\u0005\u0012\u0000\u0000\u02ce\u02cf\u0003\"\u0011\u0000\u02cf\u0465\u0001"+
		"\u0000\u0000\u0000\u02d0\u02d3\u00053\u0000\u0000\u02d1\u02d2\u0005\u00a6"+
		"\u0000\u0000\u02d2\u02d4\u0005\u00c9\u0000\u0000\u02d3\u02d1\u0001\u0000"+
		"\u0000\u0000\u02d3\u02d4\u0001\u0000\u0000\u0000\u02d4\u02d6\u0001\u0000"+
		"\u0000\u0000\u02d5\u02d7\u0005j\u0000\u0000\u02d6\u02d5\u0001\u0000\u0000"+
		"\u0000\u02d6\u02d7\u0001\u0000\u0000\u0000\u02d7\u02d8\u0001\u0000\u0000"+
		"\u0000\u02d8\u02d9\u0005\u00f5\u0000\u0000\u02d9\u02da\u0005\u0115\u0000"+
		"\u0000\u02da\u02df\u0003\u00be_\u0000\u02db\u02dc\u0005\u0002\u0000\u0000"+
		"\u02dc\u02dd\u0003\u00fa}\u0000\u02dd\u02de\u0005\u0003\u0000\u0000\u02de"+
		"\u02e0\u0001\u0000\u0000\u0000\u02df\u02db\u0001\u0000\u0000\u0000\u02df"+
		"\u02e0\u0001\u0000\u0000\u0000\u02e0\u02e1\u0001\u0000\u0000\u0000\u02e1"+
		"\u02e4\u00038\u001c\u0000\u02e2\u02e3\u0005\u00a5\u0000\u0000\u02e3\u02e5"+
		"\u0003<\u001e\u0000\u02e4\u02e2\u0001\u0000\u0000\u0000\u02e4\u02e5\u0001"+
		"\u0000\u0000\u0000\u02e5\u0465\u0001\u0000\u0000\u0000\u02e6\u02e7\u0005"+
		"\u000b\u0000\u0000\u02e7\u02e8\u0005\u0115\u0000\u0000\u02e8\u02ea\u0003"+
		"\u00b8\\\u0000\u02e9\u02eb\u0005\u0012\u0000\u0000\u02ea\u02e9\u0001\u0000"+
		"\u0000\u0000\u02ea\u02eb\u0001\u0000\u0000\u0000\u02eb\u02ec\u0001\u0000"+
		"\u0000\u0000\u02ec\u02ed\u0003\"\u0011\u0000\u02ed\u0465\u0001\u0000\u0000"+
		"\u0000\u02ee\u02f1\u00053\u0000\u0000\u02ef\u02f0\u0005\u00a6\u0000\u0000"+
		"\u02f0\u02f2\u0005\u00c9\u0000\u0000\u02f1\u02ef\u0001\u0000\u0000\u0000"+
		"\u02f1\u02f2\u0001\u0000\u0000\u0000\u02f2\u02f4\u0001\u0000\u0000\u0000"+
		"\u02f3\u02f5\u0005\u00f5\u0000\u0000\u02f4\u02f3\u0001\u0000\u0000\u0000"+
		"\u02f4\u02f5\u0001\u0000\u0000\u0000\u02f5\u02f6\u0001\u0000\u0000\u0000"+
		"\u02f6\u02fa\u0005h\u0000\u0000\u02f7\u02f8\u0005p\u0000\u0000\u02f8\u02f9"+
		"\u0005\u009e\u0000\u0000\u02f9\u02fb\u0005U\u0000\u0000\u02fa\u02f7\u0001"+
		"\u0000\u0000\u0000\u02fa\u02fb\u0001\u0000\u0000\u0000\u02fb\u02fc\u0001"+
		"\u0000\u0000\u0000\u02fc\u02fd\u0003\u00b8\\\u0000\u02fd\u02fe\u0005\u0012"+
		"\u0000\u0000\u02fe\u0308\u0005\u0135\u0000\u0000\u02ff\u0300\u0005\u0112"+
		"\u0000\u0000\u0300\u0305\u0003N\'\u0000\u0301\u0302\u0005\u0004\u0000"+
		"\u0000\u0302\u0304\u0003N\'\u0000\u0303\u0301\u0001\u0000\u0000\u0000"+
		"\u0304\u0307\u0001\u0000\u0000\u0000\u0305\u0303\u0001\u0000\u0000\u0000"+
		"\u0305\u0306\u0001\u0000\u0000\u0000\u0306\u0309\u0001\u0000\u0000\u0000"+
		"\u0307\u0305\u0001\u0000\u0000\u0000\u0308\u02ff\u0001\u0000\u0000\u0000"+
		"\u0308\u0309\u0001\u0000\u0000\u0000\u0309\u0465\u0001\u0000\u0000\u0000"+
		"\u030a\u030c\u0005N\u0000\u0000\u030b\u030d\u0005\u00f5\u0000\u0000\u030c"+
		"\u030b\u0001\u0000\u0000\u0000\u030c\u030d\u0001\u0000\u0000\u0000\u030d"+
		"\u030e\u0001\u0000\u0000\u0000\u030e\u0311\u0005h\u0000\u0000\u030f\u0310"+
		"\u0005p\u0000\u0000\u0310\u0312\u0005U\u0000\u0000\u0311\u030f\u0001\u0000"+
		"\u0000\u0000\u0311\u0312\u0001\u0000\u0000\u0000\u0312\u0313\u0001\u0000"+
		"\u0000\u0000\u0313\u0465\u0003\u00b8\\\u0000\u0314\u0316\u0005V\u0000"+
		"\u0000\u0315\u0317\u0007\u0006\u0000\u0000\u0316\u0315\u0001\u0000\u0000"+
		"\u0000\u0316\u0317\u0001\u0000\u0000\u0000\u0317\u0318\u0001\u0000\u0000"+
		"\u0000\u0318\u0465\u0003\u000e\u0007\u0000\u0319\u031a\u0005\u00e2\u0000"+
		"\u0000\u031a\u031d\u0005\u00f2\u0000\u0000\u031b\u031c\u0007\u0002\u0000"+
		"\u0000\u031c\u031e\u0003\u00b8\\\u0000\u031d\u031b\u0001\u0000\u0000\u0000"+
		"\u031d\u031e\u0001\u0000\u0000\u0000\u031e\u0323\u0001\u0000\u0000\u0000"+
		"\u031f\u0321\u0005\u0086\u0000\u0000\u0320\u031f\u0001\u0000\u0000\u0000"+
		"\u0320\u0321\u0001\u0000\u0000\u0000\u0321\u0322\u0001\u0000\u0000\u0000"+
		"\u0322\u0324\u0005\u0135\u0000\u0000\u0323\u0320\u0001\u0000\u0000\u0000"+
		"\u0323\u0324\u0001\u0000\u0000\u0000\u0324\u0465\u0001\u0000\u0000\u0000"+
		"\u0325\u0326\u0005\u00e2\u0000\u0000\u0326\u0327\u0005\u00f1\u0000\u0000"+
		"\u0327\u032a\u0005X\u0000\u0000\u0328\u0329\u0007\u0002\u0000\u0000\u0329"+
		"\u032b\u0003\u00b8\\\u0000\u032a\u0328\u0001\u0000\u0000\u0000\u032a\u032b"+
		"\u0001\u0000\u0000\u0000\u032b\u032c\u0001\u0000\u0000\u0000\u032c\u032d"+
		"\u0005\u0086\u0000\u0000\u032d\u032f\u0005\u0135\u0000\u0000\u032e\u0330"+
		"\u0003(\u0014\u0000\u032f\u032e\u0001\u0000\u0000\u0000\u032f\u0330\u0001"+
		"\u0000\u0000\u0000\u0330\u0465\u0001\u0000\u0000\u0000\u0331\u0332\u0005"+
		"\u00e2\u0000\u0000\u0332\u0333\u0005\u00f4\u0000\u0000\u0333\u0338\u0003"+
		"\u00b8\\\u0000\u0334\u0335\u0005\u0002\u0000\u0000\u0335\u0336\u0003@"+
		" \u0000\u0336\u0337\u0005\u0003\u0000\u0000\u0337\u0339\u0001\u0000\u0000"+
		"\u0000\u0338\u0334\u0001\u0000\u0000\u0000\u0338\u0339\u0001\u0000\u0000"+
		"\u0000\u0339\u0465\u0001\u0000\u0000\u0000\u033a\u033b\u0005\u00e2\u0000"+
		"\u0000\u033b\u033c\u0005*\u0000\u0000\u033c\u033d\u0007\u0002\u0000\u0000"+
		"\u033d\u0340\u0003\u00b8\\\u0000\u033e\u033f\u0007\u0002\u0000\u0000\u033f"+
		"\u0341\u0003\u00b8\\\u0000\u0340\u033e\u0001\u0000\u0000\u0000\u0340\u0341"+
		"\u0001\u0000\u0000\u0000\u0341\u0465\u0001\u0000\u0000\u0000\u0342\u0343"+
		"\u0005\u00e2\u0000\u0000\u0343\u0346\u0005\u0116\u0000\u0000\u0344\u0345"+
		"\u0007\u0002\u0000\u0000\u0345\u0347\u0003\u00b8\\\u0000\u0346\u0344\u0001"+
		"\u0000\u0000\u0000\u0346\u0347\u0001\u0000\u0000\u0000\u0347\u034c\u0001"+
		"\u0000\u0000\u0000\u0348\u034a\u0005\u0086\u0000\u0000\u0349\u0348\u0001"+
		"\u0000\u0000\u0000\u0349\u034a\u0001\u0000\u0000\u0000\u034a\u034b\u0001"+
		"\u0000\u0000\u0000\u034b\u034d\u0005\u0135\u0000\u0000\u034c\u0349\u0001"+
		"\u0000\u0000\u0000\u034c\u034d\u0001\u0000\u0000\u0000\u034d\u0465\u0001"+
		"\u0000\u0000\u0000\u034e\u034f\u0005\u00e2\u0000\u0000\u034f\u0350\u0005"+
		"\u00b1\u0000\u0000\u0350\u0352\u0003\u00b8\\\u0000\u0351\u0353\u0003("+
		"\u0014\u0000\u0352\u0351\u0001\u0000\u0000\u0000\u0352\u0353\u0001\u0000"+
		"\u0000\u0000\u0353\u0465\u0001\u0000\u0000\u0000\u0354\u0356\u0005\u00e2"+
		"\u0000\u0000\u0355\u0357\u0003\u0118\u008c\u0000\u0356\u0355\u0001\u0000"+
		"\u0000\u0000\u0356\u0357\u0001\u0000\u0000\u0000\u0357\u0358\u0001\u0000"+
		"\u0000\u0000\u0358\u035b\u0005i\u0000\u0000\u0359\u035a\u0007\u0002\u0000"+
		"\u0000\u035a\u035c\u0003\u00b8\\\u0000\u035b\u0359\u0001\u0000\u0000\u0000"+
		"\u035b\u035c\u0001\u0000\u0000\u0000\u035c\u0364\u0001\u0000\u0000\u0000"+
		"\u035d\u035f\u0005\u0086\u0000\u0000\u035e\u035d\u0001\u0000\u0000\u0000"+
		"\u035e\u035f\u0001\u0000\u0000\u0000\u035f\u0362\u0001\u0000\u0000\u0000"+
		"\u0360\u0363\u0003\u00b8\\\u0000\u0361\u0363\u0005\u0135\u0000\u0000\u0362"+
		"\u0360\u0001\u0000\u0000\u0000\u0362\u0361\u0001\u0000\u0000\u0000\u0363"+
		"\u0365\u0001\u0000\u0000\u0000\u0364\u035e\u0001\u0000\u0000\u0000\u0364"+
		"\u0365\u0001\u0000\u0000\u0000\u0365\u0465\u0001\u0000\u0000\u0000\u0366"+
		"\u0367\u0005\u00e2\u0000\u0000\u0367\u0368\u00053\u0000\u0000\u0368\u0369"+
		"\u0005\u00f1\u0000\u0000\u0369\u036c\u0003\u00b8\\\u0000\u036a\u036b\u0005"+
		"\u0012\u0000\u0000\u036b\u036d\u0005\u00dc\u0000\u0000\u036c\u036a\u0001"+
		"\u0000\u0000\u0000\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u0465\u0001"+
		"\u0000\u0000\u0000\u036e\u036f\u0005\u00e2\u0000\u0000\u036f\u0370\u0005"+
		"6\u0000\u0000\u0370\u0465\u0003,\u0016\u0000\u0371\u0372\u0005\u00e2\u0000"+
		"\u0000\u0372\u0377\u0005 \u0000\u0000\u0373\u0375\u0005\u0086\u0000\u0000"+
		"\u0374\u0373\u0001\u0000\u0000\u0000\u0374\u0375\u0001\u0000\u0000\u0000"+
		"\u0375\u0376\u0001\u0000\u0000\u0000\u0376\u0378\u0005\u0135\u0000\u0000"+
		"\u0377\u0374\u0001\u0000\u0000\u0000\u0377\u0378\u0001\u0000\u0000\u0000"+
		"\u0378\u0465\u0001\u0000\u0000\u0000\u0379\u037a\u0007\u0007\u0000\u0000"+
		"\u037a\u037c\u0005h\u0000\u0000\u037b\u037d\u0005X\u0000\u0000\u037c\u037b"+
		"\u0001\u0000\u0000\u0000\u037c\u037d\u0001\u0000\u0000\u0000\u037d\u037e"+
		"\u0001\u0000\u0000\u0000\u037e\u0465\u00030\u0018\u0000\u037f\u0380\u0007"+
		"\u0007\u0000\u0000\u0380\u0382\u0003,\u0016\u0000\u0381\u0383\u0005X\u0000"+
		"\u0000\u0382\u0381\u0001\u0000\u0000\u0000\u0382\u0383\u0001\u0000\u0000"+
		"\u0000\u0383\u0384\u0001\u0000\u0000\u0000\u0384\u0385\u0003\u00b8\\\u0000"+
		"\u0385\u0465\u0001\u0000\u0000\u0000\u0386\u0388\u0007\u0007\u0000\u0000"+
		"\u0387\u0389\u0005\u00f1\u0000\u0000\u0388\u0387\u0001\u0000\u0000\u0000"+
		"\u0388\u0389\u0001\u0000\u0000\u0000\u0389\u038b\u0001\u0000\u0000\u0000"+
		"\u038a\u038c\u0007\b\u0000\u0000\u038b\u038a\u0001\u0000\u0000\u0000\u038b"+
		"\u038c\u0001\u0000\u0000\u0000\u038c\u038d\u0001\u0000\u0000\u0000\u038d"+
		"\u038f\u0003\u00b8\\\u0000\u038e\u0390\u0003(\u0014\u0000\u038f\u038e"+
		"\u0001\u0000\u0000\u0000\u038f\u0390\u0001\u0000\u0000\u0000\u0390\u0392"+
		"\u0001\u0000\u0000\u0000\u0391\u0393\u00032\u0019\u0000\u0392\u0391\u0001"+
		"\u0000\u0000\u0000\u0392\u0393\u0001\u0000\u0000\u0000\u0393\u0465\u0001"+
		"\u0000\u0000\u0000\u0394\u0396\u0007\u0007\u0000\u0000\u0395\u0397\u0005"+
		"\u00be\u0000\u0000\u0396\u0395\u0001\u0000\u0000\u0000\u0396\u0397\u0001"+
		"\u0000\u0000\u0000\u0397\u0398\u0001\u0000\u0000\u0000\u0398\u0465\u0003"+
		"\"\u0011\u0000\u0399\u039a\u0005+\u0000\u0000\u039a\u039b\u0005\u00a2"+
		"\u0000\u0000\u039b\u039c\u0003,\u0016\u0000\u039c\u039d\u0003\u00b8\\"+
		"\u0000\u039d\u039e\u0005}\u0000\u0000\u039e\u039f\u0007\t\u0000\u0000"+
		"\u039f\u0465\u0001\u0000\u0000\u0000\u03a0\u03a1\u0005+\u0000\u0000\u03a1"+
		"\u03a2\u0005\u00a2\u0000\u0000\u03a2\u03a3\u0005\u00f1\u0000\u0000\u03a3"+
		"\u03a4\u0003\u00b8\\\u0000\u03a4\u03a5\u0005}\u0000\u0000\u03a5\u03a6"+
		"\u0007\t\u0000\u0000\u03a6\u0465\u0001\u0000\u0000\u0000\u03a7\u03a8\u0005"+
		"\u00c5\u0000\u0000\u03a8\u03a9\u0005\u00f1\u0000\u0000\u03a9\u0465\u0003"+
		"\u00b8\\\u0000\u03aa\u03ab\u0005\u00c5\u0000\u0000\u03ab\u03ac\u0005h"+
		"\u0000\u0000\u03ac\u0465\u0003\u00b8\\\u0000\u03ad\u03b5\u0005\u00c5\u0000"+
		"\u0000\u03ae\u03b6\u0005\u0135\u0000\u0000\u03af\u03b1\t\u0000\u0000\u0000"+
		"\u03b0\u03af\u0001\u0000\u0000\u0000\u03b1\u03b4\u0001\u0000\u0000\u0000"+
		"\u03b2\u03b3\u0001\u0000\u0000\u0000\u03b2\u03b0\u0001\u0000\u0000\u0000"+
		"\u03b3\u03b6\u0001\u0000\u0000\u0000\u03b4\u03b2\u0001\u0000\u0000\u0000"+
		"\u03b5\u03ae\u0001\u0000\u0000\u0000\u03b5\u03b2\u0001\u0000\u0000\u0000"+
		"\u03b6\u0465\u0001\u0000\u0000\u0000\u03b7\u03b9\u0005\u001b\u0000\u0000"+
		"\u03b8\u03ba\u0005\u0083\u0000\u0000\u03b9\u03b8\u0001\u0000\u0000\u0000"+
		"\u03b9\u03ba\u0001\u0000\u0000\u0000\u03ba\u03bb\u0001\u0000\u0000\u0000"+
		"\u03bb\u03bc\u0005\u00f1\u0000\u0000\u03bc\u03bf\u0003\u00b8\\\u0000\u03bd"+
		"\u03be\u0005\u00a5\u0000\u0000\u03be\u03c0\u0003<\u001e\u0000\u03bf\u03bd"+
		"\u0001\u0000\u0000\u0000\u03bf\u03c0\u0001\u0000\u0000\u0000\u03c0\u03c5"+
		"\u0001\u0000\u0000\u0000\u03c1\u03c3\u0005\u0012\u0000\u0000\u03c2\u03c1"+
		"\u0001\u0000\u0000\u0000\u03c2\u03c3\u0001\u0000\u0000\u0000\u03c3\u03c4"+
		"\u0001\u0000\u0000\u0000\u03c4\u03c6\u0003\"\u0011\u0000\u03c5\u03c2\u0001"+
		"\u0000\u0000\u0000\u03c5\u03c6\u0001\u0000\u0000\u0000\u03c6\u0465\u0001"+
		"\u0000\u0000\u0000\u03c7\u03c8\u0005\u0109\u0000\u0000\u03c8\u03cb\u0005"+
		"\u00f1\u0000\u0000\u03c9\u03ca\u0005p\u0000\u0000\u03ca\u03cc\u0005U\u0000"+
		"\u0000\u03cb\u03c9\u0001\u0000\u0000\u0000\u03cb\u03cc\u0001\u0000\u0000"+
		"\u0000\u03cc\u03cd\u0001\u0000\u0000\u0000\u03cd\u0465\u0003\u00b8\\\u0000"+
		"\u03ce\u03cf\u0005#\u0000\u0000\u03cf\u0465\u0005\u001b\u0000\u0000\u03d0"+
		"\u03d1\u0005\u008b\u0000\u0000\u03d1\u03d3\u0005=\u0000\u0000\u03d2\u03d4"+
		"\u0005\u008c\u0000\u0000\u03d3\u03d2\u0001\u0000\u0000\u0000\u03d3\u03d4"+
		"\u0001\u0000\u0000\u0000\u03d4\u03d5\u0001\u0000\u0000\u0000\u03d5\u03d6"+
		"\u0005w\u0000\u0000\u03d6\u03d8\u0005\u0135\u0000\u0000\u03d7\u03d9\u0005"+
		"\u00ae\u0000\u0000\u03d8\u03d7\u0001\u0000\u0000\u0000\u03d8\u03d9\u0001"+
		"\u0000\u0000\u0000\u03d9\u03da\u0001\u0000\u0000\u0000\u03da\u03db\u0005"+
		"|\u0000\u0000\u03db\u03dc\u0005\u00f1\u0000\u0000\u03dc\u03de\u0003\u00b8"+
		"\\\u0000\u03dd\u03df\u0003(\u0014\u0000\u03de\u03dd\u0001\u0000\u0000"+
		"\u0000\u03de\u03df\u0001\u0000\u0000\u0000\u03df\u0465\u0001\u0000\u0000"+
		"\u0000\u03e0\u03e1\u0005\u0104\u0000\u0000\u03e1\u03e2\u0005\u00f1\u0000"+
		"\u0000\u03e2\u03e4\u0003\u00b8\\\u0000\u03e3\u03e5\u0003(\u0014\u0000"+
		"\u03e4\u03e3\u0001\u0000\u0000\u0000\u03e4\u03e5\u0001\u0000\u0000\u0000"+
		"\u03e5\u0465\u0001\u0000\u0000\u0000\u03e6\u03e7\u0005\u0099\u0000\u0000"+
		"\u03e7\u03e8\u0005\u00c7\u0000\u0000\u03e8\u03e9\u0005\u00f1\u0000\u0000"+
		"\u03e9\u03ec\u0003\u00b8\\\u0000\u03ea\u03eb\u0007\n\u0000\u0000\u03eb"+
		"\u03ed\u0005\u00b1\u0000\u0000\u03ec\u03ea\u0001\u0000\u0000\u0000\u03ec"+
		"\u03ed\u0001\u0000\u0000\u0000\u03ed\u0465\u0001\u0000\u0000\u0000\u03ee"+
		"\u03ef\u0007\u000b\u0000\u0000\u03ef\u03f3\u0003\u0118\u008c\u0000\u03f0"+
		"\u03f2\t\u0000\u0000\u0000\u03f1\u03f0\u0001\u0000\u0000\u0000\u03f2\u03f5"+
		"\u0001\u0000\u0000\u0000\u03f3\u03f4\u0001\u0000\u0000\u0000\u03f3\u03f1"+
		"\u0001\u0000\u0000\u0000\u03f4\u0465\u0001\u0000\u0000\u0000\u03f5\u03f3"+
		"\u0001\u0000\u0000\u0000\u03f6\u03f7\u0005\u00df\u0000\u0000\u03f7\u03fb"+
		"\u0005\u00d0\u0000\u0000\u03f8\u03fa\t\u0000\u0000\u0000\u03f9\u03f8\u0001"+
		"\u0000\u0000\u0000\u03fa\u03fd\u0001\u0000\u0000\u0000\u03fb\u03fc\u0001"+
		"\u0000\u0000\u0000\u03fb\u03f9\u0001\u0000\u0000\u0000\u03fc\u0465\u0001"+
		"\u0000\u0000\u0000\u03fd\u03fb\u0001\u0000\u0000\u0000\u03fe\u03ff\u0005"+
		"\u00df\u0000\u0000\u03ff\u0400\u0005\u00f8\u0000\u0000\u0400\u0401\u0005"+
		"\u011e\u0000\u0000\u0401\u0465\u0003\u00e6s\u0000\u0402\u0403\u0005\u00df"+
		"\u0000\u0000\u0403\u0404\u0005\u00f8\u0000\u0000\u0404\u0405\u0005\u011e"+
		"\u0000\u0000\u0405\u0465\u0007\f\u0000\u0000\u0406\u0407\u0005\u00df\u0000"+
		"\u0000\u0407\u0408\u0005\u00f8\u0000\u0000\u0408\u040c\u0005\u011e\u0000"+
		"\u0000\u0409\u040b\t\u0000\u0000\u0000\u040a\u0409\u0001\u0000\u0000\u0000"+
		"\u040b\u040e\u0001\u0000\u0000\u0000\u040c\u040d\u0001\u0000\u0000\u0000"+
		"\u040c\u040a\u0001\u0000\u0000\u0000\u040d\u0465\u0001\u0000\u0000\u0000"+
		"\u040e\u040c\u0001\u0000\u0000\u0000\u040f\u0410\u0005\u00df\u0000\u0000"+
		"\u0410\u0411\u0003\u0010\b\u0000\u0411\u0412\u0005\u011f\u0000\u0000\u0412"+
		"\u0413\u0003\u0012\t\u0000\u0413\u0465\u0001\u0000\u0000\u0000\u0414\u0415"+
		"\u0005\u00df\u0000\u0000\u0415\u041d\u0003\u0010\b\u0000\u0416\u041a\u0005"+
		"\u011f\u0000\u0000\u0417\u0419\t\u0000\u0000\u0000\u0418\u0417\u0001\u0000"+
		"\u0000\u0000\u0419\u041c\u0001\u0000\u0000\u0000\u041a\u041b\u0001\u0000"+
		"\u0000\u0000\u041a\u0418\u0001\u0000\u0000\u0000\u041b\u041e\u0001\u0000"+
		"\u0000\u0000\u041c\u041a\u0001\u0000\u0000\u0000\u041d\u0416\u0001\u0000"+
		"\u0000\u0000\u041d\u041e\u0001\u0000\u0000\u0000\u041e\u0465\u0001\u0000"+
		"\u0000\u0000\u041f\u0423\u0005\u00df\u0000\u0000\u0420\u0422\t\u0000\u0000"+
		"\u0000\u0421\u0420\u0001\u0000\u0000\u0000\u0422\u0425\u0001\u0000\u0000"+
		"\u0000\u0423\u0424\u0001\u0000\u0000\u0000\u0423\u0421\u0001\u0000\u0000"+
		"\u0000\u0424\u0426\u0001\u0000\u0000\u0000\u0425\u0423\u0001\u0000\u0000"+
		"\u0000\u0426\u0427\u0005\u011f\u0000\u0000\u0427\u0465\u0003\u0012\t\u0000"+
		"\u0428\u042c\u0005\u00df\u0000\u0000\u0429\u042b\t\u0000\u0000\u0000\u042a"+
		"\u0429\u0001\u0000\u0000\u0000\u042b\u042e\u0001\u0000\u0000\u0000\u042c"+
		"\u042d\u0001\u0000\u0000\u0000\u042c\u042a\u0001\u0000\u0000\u0000\u042d"+
		"\u0465\u0001\u0000\u0000\u0000\u042e\u042c\u0001\u0000\u0000\u0000\u042f"+
		"\u0430\u0005\u00ca\u0000\u0000\u0430\u0465\u0003\u0010\b\u0000\u0431\u0435"+
		"\u0005\u00ca\u0000\u0000\u0432\u0434\t\u0000\u0000\u0000\u0433\u0432\u0001"+
		"\u0000\u0000\u0000\u0434\u0437\u0001\u0000\u0000\u0000\u0435\u0436\u0001"+
		"\u0000\u0000\u0000\u0435\u0433\u0001\u0000\u0000\u0000\u0436\u0465\u0001"+
		"\u0000\u0000\u0000\u0437\u0435\u0001\u0000\u0000\u0000\u0438\u0439\u0005"+
		"3\u0000\u0000\u0439\u043d\u0005t\u0000\u0000\u043a\u043b\u0005p\u0000"+
		"\u0000\u043b\u043c\u0005\u009e\u0000\u0000\u043c\u043e\u0005U\u0000\u0000"+
		"\u043d\u043a\u0001\u0000\u0000\u0000\u043d\u043e\u0001\u0000\u0000\u0000"+
		"\u043e\u043f\u0001\u0000\u0000\u0000\u043f\u0440\u0003\u0118\u008c\u0000"+
		"\u0440\u0442\u0005\u00a2\u0000\u0000\u0441\u0443\u0005\u00f1\u0000\u0000"+
		"\u0442\u0441\u0001\u0000\u0000\u0000\u0442\u0443\u0001\u0000\u0000\u0000"+
		"\u0443\u0444\u0001\u0000\u0000\u0000\u0444\u0447\u0003\u00b8\\\u0000\u0445"+
		"\u0446\u0005\u0112\u0000\u0000\u0446\u0448\u0003\u0118\u008c\u0000\u0447"+
		"\u0445\u0001\u0000\u0000\u0000\u0447\u0448\u0001\u0000\u0000\u0000\u0448"+
		"\u0449\u0001\u0000\u0000\u0000\u0449\u044a\u0005\u0002\u0000\u0000\u044a"+
		"\u044b\u0003\u00ba]\u0000\u044b\u044e\u0005\u0003\u0000\u0000\u044c\u044d"+
		"\u0005\u00a5\u0000\u0000\u044d\u044f\u0003<\u001e\u0000\u044e\u044c\u0001"+
		"\u0000\u0000\u0000\u044e\u044f\u0001\u0000\u0000\u0000\u044f\u0465\u0001"+
		"\u0000\u0000\u0000\u0450\u0451\u0005N\u0000\u0000\u0451\u0454\u0005t\u0000"+
		"\u0000\u0452\u0453\u0005p\u0000\u0000\u0453\u0455\u0005U\u0000\u0000\u0454"+
		"\u0452\u0001\u0000\u0000\u0000\u0454\u0455\u0001\u0000\u0000\u0000\u0455"+
		"\u0456\u0001\u0000\u0000\u0000\u0456\u0457\u0003\u0118\u008c\u0000\u0457"+
		"\u0459\u0005\u00a2\u0000\u0000\u0458\u045a\u0005\u00f1\u0000\u0000\u0459"+
		"\u0458\u0001\u0000\u0000\u0000\u0459\u045a\u0001\u0000\u0000\u0000\u045a"+
		"\u045b\u0001\u0000\u0000\u0000\u045b\u045c\u0003\u00b8\\\u0000\u045c\u0465"+
		"\u0001\u0000\u0000\u0000\u045d\u0461\u0003\u0014\n\u0000\u045e\u0460\t"+
		"\u0000\u0000\u0000\u045f\u045e\u0001\u0000\u0000\u0000\u0460\u0463\u0001"+
		"\u0000\u0000\u0000\u0461\u0462\u0001\u0000\u0000\u0000\u0461\u045f\u0001"+
		"\u0000\u0000\u0000\u0462\u0465\u0001\u0000\u0000\u0000\u0463\u0461\u0001"+
		"\u0000\u0000\u0000\u0464\u0143\u0001\u0000\u0000\u0000\u0464\u0145\u0001"+
		"\u0000\u0000\u0000\u0464\u0148\u0001\u0000\u0000\u0000\u0464\u014a\u0001"+
		"\u0000\u0000\u0000\u0464\u014e\u0001\u0000\u0000\u0000\u0464\u0154\u0001"+
		"\u0000\u0000\u0000\u0464\u0166\u0001\u0000\u0000\u0000\u0464\u016d\u0001"+
		"\u0000\u0000\u0000\u0464\u0173\u0001\u0000\u0000\u0000\u0464\u017d\u0001"+
		"\u0000\u0000\u0000\u0464\u0189\u0001\u0000\u0000\u0000\u0464\u019a\u0001"+
		"\u0000\u0000\u0000\u0464\u01af\u0001\u0000\u0000\u0000\u0464\u01c0\u0001"+
		"\u0000\u0000\u0000\u0464\u01d1\u0001\u0000\u0000\u0000\u0464\u01dc\u0001"+
		"\u0000\u0000\u0000\u0464\u01e3\u0001\u0000\u0000\u0000\u0464\u01ec\u0001"+
		"\u0000\u0000\u0000\u0464\u01f5\u0001\u0000\u0000\u0000\u0464\u0202\u0001"+
		"\u0000\u0000\u0000\u0464\u020d\u0001\u0000\u0000\u0000\u0464\u0214\u0001"+
		"\u0000\u0000\u0000\u0464\u021b\u0001\u0000\u0000\u0000\u0464\u0226\u0001"+
		"\u0000\u0000\u0000\u0464\u0231\u0001\u0000\u0000\u0000\u0464\u0240\u0001"+
		"\u0000\u0000\u0000\u0464\u024c\u0001\u0000\u0000\u0000\u0464\u025a\u0001"+
		"\u0000\u0000\u0000\u0464\u0264\u0001\u0000\u0000\u0000\u0464\u0272\u0001"+
		"\u0000\u0000\u0000\u0464\u027a\u0001\u0000\u0000\u0000\u0464\u028d\u0001"+
		"\u0000\u0000\u0000\u0464\u0296\u0001\u0000\u0000\u0000\u0464\u029c\u0001"+
		"\u0000\u0000\u0000\u0464\u02a6\u0001\u0000\u0000\u0000\u0464\u02ad\u0001"+
		"\u0000\u0000\u0000\u0464\u02d0\u0001\u0000\u0000\u0000\u0464\u02e6\u0001"+
		"\u0000\u0000\u0000\u0464\u02ee\u0001\u0000\u0000\u0000\u0464\u030a\u0001"+
		"\u0000\u0000\u0000\u0464\u0314\u0001\u0000\u0000\u0000\u0464\u0319\u0001"+
		"\u0000\u0000\u0000\u0464\u0325\u0001\u0000\u0000\u0000\u0464\u0331\u0001"+
		"\u0000\u0000\u0000\u0464\u033a\u0001\u0000\u0000\u0000\u0464\u0342\u0001"+
		"\u0000\u0000\u0000\u0464\u034e\u0001\u0000\u0000\u0000\u0464\u0354\u0001"+
		"\u0000\u0000\u0000\u0464\u0366\u0001\u0000\u0000\u0000\u0464\u036e\u0001"+
		"\u0000\u0000\u0000\u0464\u0371\u0001\u0000\u0000\u0000\u0464\u0379\u0001"+
		"\u0000\u0000\u0000\u0464\u037f\u0001\u0000\u0000\u0000\u0464\u0386\u0001"+
		"\u0000\u0000\u0000\u0464\u0394\u0001\u0000\u0000\u0000\u0464\u0399\u0001"+
		"\u0000\u0000\u0000\u0464\u03a0\u0001\u0000\u0000\u0000\u0464\u03a7\u0001"+
		"\u0000\u0000\u0000\u0464\u03aa\u0001\u0000\u0000\u0000\u0464\u03ad\u0001"+
		"\u0000\u0000\u0000\u0464\u03b7\u0001\u0000\u0000\u0000\u0464\u03c7\u0001"+
		"\u0000\u0000\u0000\u0464\u03ce\u0001\u0000\u0000\u0000\u0464\u03d0\u0001"+
		"\u0000\u0000\u0000\u0464\u03e0\u0001\u0000\u0000\u0000\u0464\u03e6\u0001"+
		"\u0000\u0000\u0000\u0464\u03ee\u0001\u0000\u0000\u0000\u0464\u03f6\u0001"+
		"\u0000\u0000\u0000\u0464\u03fe\u0001\u0000\u0000\u0000\u0464\u0402\u0001"+
		"\u0000\u0000\u0000\u0464\u0406\u0001\u0000\u0000\u0000\u0464\u040f\u0001"+
		"\u0000\u0000\u0000\u0464\u0414\u0001\u0000\u0000\u0000\u0464\u041f\u0001"+
		"\u0000\u0000\u0000\u0464\u0428\u0001\u0000\u0000\u0000\u0464\u042f\u0001"+
		"\u0000\u0000\u0000\u0464\u0431\u0001\u0000\u0000\u0000\u0464\u0438\u0001"+
		"\u0000\u0000\u0000\u0464\u0450\u0001\u0000\u0000\u0000\u0464\u045d\u0001"+
		"\u0000\u0000\u0000\u0465\u000f\u0001\u0000\u0000\u0000\u0466\u0467\u0003"+
		"\u011c\u008e\u0000\u0467\u0011\u0001\u0000\u0000\u0000\u0468\u0469\u0003"+
		"\u011c\u008e\u0000\u0469\u0013\u0001\u0000\u0000\u0000\u046a\u046b\u0005"+
		"3\u0000\u0000\u046b\u0513\u0005\u00d0\u0000\u0000\u046c\u046d\u0005N\u0000"+
		"\u0000\u046d\u0513\u0005\u00d0\u0000\u0000\u046e\u0470\u0005k\u0000\u0000"+
		"\u046f\u0471\u0005\u00d0\u0000\u0000\u0470\u046f\u0001\u0000\u0000\u0000"+
		"\u0470\u0471\u0001\u0000\u0000\u0000\u0471\u0513\u0001\u0000\u0000\u0000"+
		"\u0472\u0474\u0005\u00cd\u0000\u0000\u0473\u0475\u0005\u00d0\u0000\u0000"+
		"\u0474\u0473\u0001\u0000\u0000\u0000\u0474\u0475\u0001\u0000\u0000\u0000"+
		"\u0475\u0513\u0001\u0000\u0000\u0000\u0476\u0477\u0005\u00e2\u0000\u0000"+
		"\u0477\u0513\u0005k\u0000\u0000\u0478\u0479\u0005\u00e2\u0000\u0000\u0479"+
		"\u047b\u0005\u00d0\u0000\u0000\u047a\u047c\u0005k\u0000\u0000\u047b\u047a"+
		"\u0001\u0000\u0000\u0000\u047b\u047c\u0001\u0000\u0000\u0000\u047c\u0513"+
		"\u0001\u0000\u0000\u0000\u047d\u047e\u0005\u00e2\u0000\u0000\u047e\u0513"+
		"\u0005\u00ba\u0000\u0000\u047f\u0480\u0005\u00e2\u0000\u0000\u0480\u0513"+
		"\u0005\u00d1\u0000\u0000\u0481\u0482\u0005\u00e2\u0000\u0000\u0482\u0483"+
		"\u00056\u0000\u0000\u0483\u0513\u0005\u00d1\u0000\u0000\u0484\u0485\u0005"+
		"W\u0000\u0000\u0485\u0513\u0005\u00f1\u0000\u0000\u0486\u0487\u0005r\u0000"+
		"\u0000\u0487\u0513\u0005\u00f1\u0000\u0000\u0488\u0489\u0005\u00e2\u0000"+
		"\u0000\u0489\u0513\u0005.\u0000\u0000\u048a\u048b\u0005\u00e2\u0000\u0000"+
		"\u048b\u048c\u00053\u0000\u0000\u048c\u0513\u0005\u00f1\u0000\u0000\u048d"+
		"\u048e\u0005\u00e2\u0000\u0000\u048e\u0513\u0005\u0100\u0000\u0000\u048f"+
		"\u0490\u0005\u00e2\u0000\u0000\u0490\u0513\u0005u\u0000\u0000\u0491\u0492"+
		"\u0005\u00e2\u0000\u0000\u0492\u0513\u0005\u008f\u0000\u0000\u0493\u0494"+
		"\u00053\u0000\u0000\u0494\u0513\u0005t\u0000\u0000\u0495\u0496\u0005N"+
		"\u0000\u0000\u0496\u0513\u0005t\u0000\u0000\u0497\u0498\u0005\u000b\u0000"+
		"\u0000\u0498\u0513\u0005t\u0000\u0000\u0499\u049a\u0005\u008e\u0000\u0000"+
		"\u049a\u0513\u0005\u00f1\u0000\u0000\u049b\u049c\u0005\u008e\u0000\u0000"+
		"\u049c\u0513\u0005>\u0000\u0000\u049d\u049e\u0005\u010d\u0000\u0000\u049e"+
		"\u0513\u0005\u00f1\u0000\u0000\u049f\u04a0\u0005\u010d\u0000\u0000\u04a0"+
		"\u0513\u0005>\u0000\u0000\u04a1\u04a2\u00053\u0000\u0000\u04a2\u04a3\u0005"+
		"\u00f5\u0000\u0000\u04a3\u0513\u0005\u0091\u0000\u0000\u04a4\u04a5\u0005"+
		"N\u0000\u0000\u04a5\u04a6\u0005\u00f5\u0000\u0000\u04a6\u0513\u0005\u0091"+
		"\u0000\u0000\u04a7\u04a8\u0005\u000b\u0000\u0000\u04a8\u04a9\u0005\u00f1"+
		"\u0000\u0000\u04a9\u04aa\u0003\u00be_\u0000\u04aa\u04ab\u0005\u009e\u0000"+
		"\u0000\u04ab\u04ac\u0005%\u0000\u0000\u04ac\u0513\u0001\u0000\u0000\u0000"+
		"\u04ad\u04ae\u0005\u000b\u0000\u0000\u04ae\u04af\u0005\u00f1\u0000\u0000"+
		"\u04af\u04b0\u0003\u00be_\u0000\u04b0\u04b1\u0005%\u0000\u0000\u04b1\u04b2"+
		"\u0005\u001a\u0000\u0000\u04b2\u0513\u0001\u0000\u0000\u0000\u04b3\u04b4"+
		"\u0005\u000b\u0000\u0000\u04b4\u04b5\u0005\u00f1\u0000\u0000\u04b5\u04b6"+
		"\u0003\u00be_\u0000\u04b6\u04b7\u0005\u009e\u0000\u0000\u04b7\u04b8\u0005"+
		"\u00e6\u0000\u0000\u04b8\u0513\u0001\u0000\u0000\u0000\u04b9\u04ba\u0005"+
		"\u000b\u0000\u0000\u04ba\u04bb\u0005\u00f1\u0000\u0000\u04bb\u04bc\u0003"+
		"\u00be_\u0000\u04bc\u04bd\u0005\u00e3\u0000\u0000\u04bd\u04be\u0005\u001a"+
		"\u0000\u0000\u04be\u0513\u0001\u0000\u0000\u0000\u04bf\u04c0\u0005\u000b"+
		"\u0000\u0000\u04c0\u04c1\u0005\u00f1\u0000\u0000\u04c1\u04c2\u0003\u00be"+
		"_\u0000\u04c2\u04c3\u0005\u009e\u0000\u0000\u04c3\u04c4\u0005\u00e3\u0000"+
		"\u0000\u04c4\u0513\u0001\u0000\u0000\u0000\u04c5\u04c6\u0005\u000b\u0000"+
		"\u0000\u04c6\u04c7\u0005\u00f1\u0000\u0000\u04c7\u04c8\u0003\u00be_\u0000"+
		"\u04c8\u04c9\u0005\u009e\u0000\u0000\u04c9\u04ca\u0005\u00e9\u0000\u0000"+
		"\u04ca\u04cb\u0005\u0012\u0000\u0000\u04cb\u04cc\u0005I\u0000\u0000\u04cc"+
		"\u0513\u0001\u0000\u0000\u0000\u04cd\u04ce\u0005\u000b\u0000\u0000\u04ce"+
		"\u04cf\u0005\u00f1\u0000\u0000\u04cf\u04d0\u0003\u00be_\u0000\u04d0\u04d1"+
		"\u0005\u00df\u0000\u0000\u04d1\u04d2\u0005\u00e3\u0000\u0000\u04d2\u04d3"+
		"\u0005\u008d\u0000\u0000\u04d3\u0513\u0001\u0000\u0000\u0000\u04d4\u04d5"+
		"\u0005\u000b\u0000\u0000\u04d5\u04d6\u0005\u00f1\u0000\u0000\u04d6\u04d7"+
		"\u0003\u00be_\u0000\u04d7\u04d8\u0005T\u0000\u0000\u04d8\u04d9\u0005\u00af"+
		"\u0000\u0000\u04d9\u0513\u0001\u0000\u0000\u0000\u04da\u04db\u0005\u000b"+
		"\u0000\u0000\u04db\u04dc\u0005\u00f1\u0000\u0000\u04dc\u04dd\u0003\u00be"+
		"_\u0000\u04dd\u04de\u0005\u0010\u0000\u0000\u04de\u04df\u0005\u00af\u0000"+
		"\u0000\u04df\u0513\u0001\u0000\u0000\u0000\u04e0\u04e1\u0005\u000b\u0000"+
		"\u0000\u04e1\u04e2\u0005\u00f1\u0000\u0000\u04e2\u04e3\u0003\u00be_\u0000"+
		"\u04e3\u04e4\u0005\u0107\u0000\u0000\u04e4\u04e5\u0005\u00af\u0000\u0000"+
		"\u04e5\u0513\u0001\u0000\u0000\u0000\u04e6\u04e7\u0005\u000b\u0000\u0000"+
		"\u04e7\u04e8\u0005\u00f1\u0000\u0000\u04e8\u04e9\u0003\u00be_\u0000\u04e9"+
		"\u04ea\u0005\u00fd\u0000\u0000\u04ea\u0513\u0001\u0000\u0000\u0000\u04eb"+
		"\u04ec\u0005\u000b\u0000\u0000\u04ec\u04ed\u0005\u00f1\u0000\u0000\u04ed"+
		"\u04ef\u0003\u00be_\u0000\u04ee\u04f0\u0003(\u0014\u0000\u04ef\u04ee\u0001"+
		"\u0000\u0000\u0000\u04ef\u04f0\u0001\u0000\u0000\u0000\u04f0\u04f1\u0001"+
		"\u0000\u0000\u0000\u04f1\u04f2\u0005-\u0000\u0000\u04f2\u0513\u0001\u0000"+
		"\u0000\u0000\u04f3\u04f4\u0005\u000b\u0000\u0000\u04f4\u04f5\u0005\u00f1"+
		"\u0000\u0000\u04f5\u04f7\u0003\u00be_\u0000\u04f6\u04f8\u0003(\u0014\u0000"+
		"\u04f7\u04f6\u0001\u0000\u0000\u0000\u04f7\u04f8\u0001\u0000\u0000\u0000"+
		"\u04f8\u04f9\u0001\u0000\u0000\u0000\u04f9\u04fa\u00050\u0000\u0000\u04fa"+
		"\u0513\u0001\u0000\u0000\u0000\u04fb\u04fc\u0005\u000b\u0000\u0000\u04fc"+
		"\u04fd\u0005\u00f1\u0000\u0000\u04fd\u04ff\u0003\u00be_\u0000\u04fe\u0500"+
		"\u0003(\u0014\u0000\u04ff\u04fe\u0001\u0000\u0000\u0000\u04ff\u0500\u0001"+
		"\u0000\u0000\u0000\u0500\u0501\u0001\u0000\u0000\u0000\u0501\u0502\u0005"+
		"\u00df\u0000\u0000\u0502\u0503\u0005_\u0000\u0000\u0503\u0513\u0001\u0000"+
		"\u0000\u0000\u0504\u0505\u0005\u000b\u0000\u0000\u0505\u0506\u0005\u00f1"+
		"\u0000\u0000\u0506\u0508\u0003\u00be_\u0000\u0507\u0509\u0003(\u0014\u0000"+
		"\u0508\u0507\u0001\u0000\u0000\u0000\u0508\u0509\u0001\u0000\u0000\u0000"+
		"\u0509\u050a\u0001\u0000\u0000\u0000\u050a\u050b\u0005\u00c9\u0000\u0000"+
		"\u050b\u050c\u0005*\u0000\u0000\u050c\u0513\u0001\u0000\u0000\u0000\u050d"+
		"\u050e\u0005\u00e7\u0000\u0000\u050e\u0513\u0005\u00ff\u0000\u0000\u050f"+
		"\u0513\u0005,\u0000\u0000\u0510\u0513\u0005\u00d2\u0000\u0000\u0511\u0513"+
		"\u0005H\u0000\u0000\u0512\u046a\u0001\u0000\u0000\u0000\u0512\u046c\u0001"+
		"\u0000\u0000\u0000\u0512\u046e\u0001\u0000\u0000\u0000\u0512\u0472\u0001"+
		"\u0000\u0000\u0000\u0512\u0476\u0001\u0000\u0000\u0000\u0512\u0478\u0001"+
		"\u0000\u0000\u0000\u0512\u047d\u0001\u0000\u0000\u0000\u0512\u047f\u0001"+
		"\u0000\u0000\u0000\u0512\u0481\u0001\u0000\u0000\u0000\u0512\u0484\u0001"+
		"\u0000\u0000\u0000\u0512\u0486\u0001\u0000\u0000\u0000\u0512\u0488\u0001"+
		"\u0000\u0000\u0000\u0512\u048a\u0001\u0000\u0000\u0000\u0512\u048d\u0001"+
		"\u0000\u0000\u0000\u0512\u048f\u0001\u0000\u0000\u0000\u0512\u0491\u0001"+
		"\u0000\u0000\u0000\u0512\u0493\u0001\u0000\u0000\u0000\u0512\u0495\u0001"+
		"\u0000\u0000\u0000\u0512\u0497\u0001\u0000\u0000\u0000\u0512\u0499\u0001"+
		"\u0000\u0000\u0000\u0512\u049b\u0001\u0000\u0000\u0000\u0512\u049d\u0001"+
		"\u0000\u0000\u0000\u0512\u049f\u0001\u0000\u0000\u0000\u0512\u04a1\u0001"+
		"\u0000\u0000\u0000\u0512\u04a4\u0001\u0000\u0000\u0000\u0512\u04a7\u0001"+
		"\u0000\u0000\u0000\u0512\u04ad\u0001\u0000\u0000\u0000\u0512\u04b3\u0001"+
		"\u0000\u0000\u0000\u0512\u04b9\u0001\u0000\u0000\u0000\u0512\u04bf\u0001"+
		"\u0000\u0000\u0000\u0512\u04c5\u0001\u0000\u0000\u0000\u0512\u04cd\u0001"+
		"\u0000\u0000\u0000\u0512\u04d4\u0001\u0000\u0000\u0000\u0512\u04da\u0001"+
		"\u0000\u0000\u0000\u0512\u04e0\u0001\u0000\u0000\u0000\u0512\u04e6\u0001"+
		"\u0000\u0000\u0000\u0512\u04eb\u0001\u0000\u0000\u0000\u0512\u04f3\u0001"+
		"\u0000\u0000\u0000\u0512\u04fb\u0001\u0000\u0000\u0000\u0512\u0504\u0001"+
		"\u0000\u0000\u0000\u0512\u050d\u0001\u0000\u0000\u0000\u0512\u050f\u0001"+
		"\u0000\u0000\u0000\u0512\u0510\u0001\u0000\u0000\u0000\u0512\u0511\u0001"+
		"\u0000\u0000\u0000\u0513\u0015\u0001\u0000\u0000\u0000\u0514\u0516\u0005"+
		"3\u0000\u0000\u0515\u0517\u0005\u00f5\u0000\u0000\u0516\u0515\u0001\u0000"+
		"\u0000\u0000\u0516\u0517\u0001\u0000\u0000\u0000\u0517\u0519\u0001\u0000"+
		"\u0000\u0000\u0518\u051a\u0005Y\u0000\u0000\u0519\u0518\u0001\u0000\u0000"+
		"\u0000\u0519\u051a\u0001\u0000\u0000\u0000\u051a\u051b\u0001\u0000\u0000"+
		"\u0000\u051b\u051f\u0005\u00f1\u0000\u0000\u051c\u051d\u0005p\u0000\u0000"+
		"\u051d\u051e\u0005\u009e\u0000\u0000\u051e\u0520\u0005U\u0000\u0000\u051f"+
		"\u051c\u0001\u0000\u0000\u0000\u051f\u0520\u0001\u0000\u0000\u0000\u0520"+
		"\u0521\u0001\u0000\u0000\u0000\u0521\u0522\u0003\u00b8\\\u0000\u0522\u0017"+
		"\u0001\u0000\u0000\u0000\u0523\u0524\u00053\u0000\u0000\u0524\u0526\u0005"+
		"\u00a6\u0000\u0000\u0525\u0523\u0001\u0000\u0000\u0000\u0525\u0526\u0001"+
		"\u0000\u0000\u0000\u0526\u0527\u0001\u0000\u0000\u0000\u0527\u0528\u0005"+
		"\u00c9\u0000\u0000\u0528\u0529\u0005\u00f1\u0000\u0000\u0529\u052a\u0003"+
		"\u00b8\\\u0000\u052a\u0019\u0001\u0000\u0000\u0000\u052b\u052c\u0005%"+
		"\u0000\u0000\u052c\u052d\u0005\u001a\u0000\u0000\u052d\u0531\u0003\u00a0"+
		"P\u0000\u052e\u052f\u0005\u00e6\u0000\u0000\u052f\u0530\u0005\u001a\u0000"+
		"\u0000\u0530\u0532\u0003\u00a4R\u0000\u0531\u052e\u0001\u0000\u0000\u0000"+
		"\u0531\u0532\u0001\u0000\u0000\u0000\u0532\u0533\u0001\u0000\u0000\u0000"+
		"\u0533\u0534\u0005|\u0000\u0000\u0534\u0535\u0005\u0139\u0000\u0000\u0535"+
		"\u0536\u0005\u0019\u0000\u0000\u0536\u001b\u0001\u0000\u0000\u0000\u0537"+
		"\u0538\u0005\u00e3\u0000\u0000\u0538\u0539\u0005\u001a\u0000\u0000\u0539"+
		"\u053a\u0003\u00a0P\u0000\u053a\u053d\u0005\u00a2\u0000\u0000\u053b\u053e"+
		"\u0003D\"\u0000\u053c\u053e\u0003F#\u0000\u053d\u053b\u0001\u0000\u0000"+
		"\u0000\u053d\u053c\u0001\u0000\u0000\u0000\u053e\u0542\u0001\u0000\u0000"+
		"\u0000\u053f\u0540\u0005\u00e9\u0000\u0000\u0540\u0541\u0005\u0012\u0000"+
		"\u0000\u0541\u0543\u0005I\u0000\u0000\u0542\u053f\u0001\u0000\u0000\u0000"+
		"\u0542\u0543\u0001\u0000\u0000\u0000\u0543\u001d\u0001\u0000\u0000\u0000"+
		"\u0544\u0545\u0005\u008d\u0000\u0000\u0545\u0546\u0005\u0135\u0000\u0000"+
		"\u0546\u001f\u0001\u0000\u0000\u0000\u0547\u0548\u0005+\u0000\u0000\u0548"+
		"\u0549\u0005\u0135\u0000\u0000\u0549!\u0001\u0000\u0000\u0000\u054a\u054c"+
		"\u00034\u001a\u0000\u054b\u054a\u0001\u0000\u0000\u0000\u054b\u054c\u0001"+
		"\u0000\u0000\u0000\u054c\u054d\u0001\u0000\u0000\u0000\u054d\u054e\u0003"+
		"V+\u0000\u054e\u054f\u0003R)\u0000\u054f#\u0001\u0000\u0000\u0000\u0550"+
		"\u0551\u0005y\u0000\u0000\u0551\u0553\u0005\u00ae\u0000\u0000\u0552\u0554"+
		"\u0005\u00f1\u0000\u0000\u0553\u0552\u0001\u0000\u0000\u0000\u0553\u0554"+
		"\u0001\u0000\u0000\u0000\u0554\u0555\u0001\u0000\u0000\u0000\u0555\u055c"+
		"\u0003\u00b8\\\u0000\u0556\u055a\u0003(\u0014\u0000\u0557\u0558\u0005"+
		"p\u0000\u0000\u0558\u0559\u0005\u009e\u0000\u0000\u0559\u055b\u0005U\u0000"+
		"\u0000\u055a\u0557\u0001\u0000\u0000\u0000\u055a\u055b\u0001\u0000\u0000"+
		"\u0000\u055b\u055d\u0001\u0000\u0000\u0000\u055c\u0556\u0001\u0000\u0000"+
		"\u0000\u055c\u055d\u0001\u0000\u0000\u0000\u055d\u055f\u0001\u0000\u0000"+
		"\u0000\u055e\u0560\u0003\u00a0P\u0000\u055f\u055e\u0001\u0000\u0000\u0000"+
		"\u055f\u0560\u0001\u0000\u0000\u0000\u0560\u058e\u0001\u0000\u0000\u0000"+
		"\u0561\u0562\u0005y\u0000\u0000\u0562\u0564\u0005|\u0000\u0000\u0563\u0565"+
		"\u0005\u00f1\u0000\u0000\u0564\u0563\u0001\u0000\u0000\u0000\u0564\u0565"+
		"\u0001\u0000\u0000\u0000\u0565\u0566\u0001\u0000\u0000\u0000\u0566\u0568"+
		"\u0003\u00b8\\\u0000\u0567\u0569\u0003(\u0014\u0000\u0568\u0567\u0001"+
		"\u0000\u0000\u0000\u0568\u0569\u0001\u0000\u0000\u0000\u0569\u056d\u0001"+
		"\u0000\u0000\u0000\u056a\u056b\u0005p\u0000\u0000\u056b\u056c\u0005\u009e"+
		"\u0000\u0000\u056c\u056e\u0005U\u0000\u0000\u056d\u056a\u0001\u0000\u0000"+
		"\u0000\u056d\u056e\u0001\u0000\u0000\u0000\u056e\u0570\u0001\u0000\u0000"+
		"\u0000\u056f\u0571\u0003\u00a0P\u0000\u0570\u056f\u0001\u0000\u0000\u0000"+
		"\u0570\u0571\u0001\u0000\u0000\u0000\u0571\u058e\u0001\u0000\u0000\u0000"+
		"\u0572\u0573\u0005y\u0000\u0000\u0573\u0575\u0005\u00ae\u0000\u0000\u0574"+
		"\u0576\u0005\u008c\u0000\u0000\u0575\u0574\u0001\u0000\u0000\u0000\u0575"+
		"\u0576\u0001\u0000\u0000\u0000\u0576\u0577\u0001\u0000\u0000\u0000\u0577"+
		"\u0578\u0005J\u0000\u0000\u0578\u057a\u0005\u0135\u0000\u0000\u0579\u057b"+
		"\u0003\u00b4Z\u0000\u057a\u0579\u0001\u0000\u0000\u0000\u057a\u057b\u0001"+
		"\u0000\u0000\u0000\u057b\u057d\u0001\u0000\u0000\u0000\u057c\u057e\u0003"+
		"H$\u0000\u057d\u057c\u0001\u0000\u0000\u0000\u057d\u057e\u0001\u0000\u0000"+
		"\u0000\u057e\u058e\u0001\u0000\u0000\u0000\u057f\u0580\u0005y\u0000\u0000"+
		"\u0580\u0582\u0005\u00ae\u0000\u0000\u0581\u0583\u0005\u008c\u0000\u0000"+
		"\u0582\u0581\u0001\u0000\u0000\u0000\u0582\u0583\u0001\u0000\u0000\u0000"+
		"\u0583\u0584\u0001\u0000\u0000\u0000\u0584\u0586\u0005J\u0000\u0000\u0585"+
		"\u0587\u0005\u0135\u0000\u0000\u0586\u0585\u0001\u0000\u0000\u0000\u0586"+
		"\u0587\u0001\u0000\u0000\u0000\u0587\u0588\u0001\u0000\u0000\u0000\u0588"+
		"\u058b\u00038\u001c\u0000\u0589\u058a\u0005\u00a5\u0000\u0000\u058a\u058c"+
		"\u0003<\u001e\u0000\u058b\u0589\u0001\u0000\u0000\u0000\u058b\u058c\u0001"+
		"\u0000\u0000\u0000\u058c\u058e\u0001\u0000\u0000\u0000\u058d\u0550\u0001"+
		"\u0000\u0000\u0000\u058d\u0561\u0001\u0000\u0000\u0000\u058d\u0572\u0001"+
		"\u0000\u0000\u0000\u058d\u057f\u0001\u0000\u0000\u0000\u058e%\u0001\u0000"+
		"\u0000\u0000\u058f\u0591\u0003(\u0014\u0000\u0590\u0592\u0003\u001e\u000f"+
		"\u0000\u0591\u0590\u0001\u0000\u0000\u0000\u0591\u0592\u0001\u0000\u0000"+
		"\u0000\u0592\'\u0001\u0000\u0000\u0000\u0593\u0594\u0005\u00af\u0000\u0000"+
		"\u0594\u0595\u0005\u0002\u0000\u0000\u0595\u059a\u0003*\u0015\u0000\u0596"+
		"\u0597\u0005\u0004\u0000\u0000\u0597\u0599\u0003*\u0015\u0000\u0598\u0596"+
		"\u0001\u0000\u0000\u0000\u0599\u059c\u0001\u0000\u0000\u0000\u059a\u0598"+
		"\u0001\u0000\u0000\u0000\u059a\u059b\u0001\u0000\u0000\u0000\u059b\u059d"+
		"\u0001\u0000\u0000\u0000\u059c\u059a\u0001\u0000\u0000\u0000\u059d\u059e"+
		"\u0005\u0003\u0000\u0000\u059e)\u0001\u0000\u0000\u0000\u059f\u05a2\u0003"+
		"\u0118\u008c\u0000\u05a0\u05a1\u0005\u011f\u0000\u0000\u05a1\u05a3\u0003"+
		"\u00dcn\u0000\u05a2\u05a0\u0001\u0000\u0000\u0000\u05a2\u05a3\u0001\u0000"+
		"\u0000\u0000\u05a3+\u0001\u0000\u0000\u0000\u05a4\u05a5\u0007\r\u0000"+
		"\u0000\u05a5-\u0001\u0000\u0000\u0000\u05a6\u05a7\u0007\u000e\u0000\u0000"+
		"\u05a7/\u0001\u0000\u0000\u0000\u05a8\u05ae\u0003\u0112\u0089\u0000\u05a9"+
		"\u05ae\u0005\u0135\u0000\u0000\u05aa\u05ae\u0003\u00deo\u0000\u05ab\u05ae"+
		"\u0003\u00e0p\u0000\u05ac\u05ae\u0003\u00e2q\u0000\u05ad\u05a8\u0001\u0000"+
		"\u0000\u0000\u05ad\u05a9\u0001\u0000\u0000\u0000\u05ad\u05aa\u0001\u0000"+
		"\u0000\u0000\u05ad\u05ab\u0001\u0000\u0000\u0000\u05ad\u05ac\u0001\u0000"+
		"\u0000\u0000\u05ae1\u0001\u0000\u0000\u0000\u05af\u05b4\u0003\u0118\u008c"+
		"\u0000\u05b0\u05b1\u0005\u0005\u0000\u0000\u05b1\u05b3\u0003\u0118\u008c"+
		"\u0000\u05b2\u05b0\u0001\u0000\u0000\u0000\u05b3\u05b6\u0001\u0000\u0000"+
		"\u0000\u05b4\u05b2\u0001\u0000\u0000\u0000\u05b4\u05b5\u0001\u0000\u0000"+
		"\u0000\u05b53\u0001\u0000\u0000\u0000\u05b6\u05b4\u0001\u0000\u0000\u0000"+
		"\u05b7\u05b8\u0005\u011b\u0000\u0000\u05b8\u05bd\u00036\u001b\u0000\u05b9"+
		"\u05ba\u0005\u0004\u0000\u0000\u05ba\u05bc\u00036\u001b\u0000\u05bb\u05b9"+
		"\u0001\u0000\u0000\u0000\u05bc\u05bf\u0001\u0000\u0000\u0000\u05bd\u05bb"+
		"\u0001\u0000\u0000\u0000\u05bd\u05be\u0001\u0000\u0000\u0000\u05be5\u0001"+
		"\u0000\u0000\u0000\u05bf\u05bd\u0001\u0000\u0000\u0000\u05c0\u05c2\u0003"+
		"\u0114\u008a\u0000\u05c1\u05c3\u0003\u00a0P\u0000\u05c2\u05c1\u0001\u0000"+
		"\u0000\u0000\u05c2\u05c3\u0001\u0000\u0000\u0000\u05c3\u05c5\u0001\u0000"+
		"\u0000\u0000\u05c4\u05c6\u0005\u0012\u0000\u0000\u05c5\u05c4\u0001\u0000"+
		"\u0000\u0000\u05c5\u05c6\u0001\u0000\u0000\u0000\u05c6\u05c7\u0001\u0000"+
		"\u0000\u0000\u05c7\u05c8\u0005\u0002\u0000\u0000\u05c8\u05c9\u0003\"\u0011"+
		"\u0000\u05c9\u05ca\u0005\u0003\u0000\u0000\u05ca7\u0001\u0000\u0000\u0000"+
		"\u05cb\u05cc\u0005\u0112\u0000\u0000\u05cc\u05cd\u0003\u00b8\\\u0000\u05cd"+
		"9\u0001\u0000\u0000\u0000\u05ce\u05cf\u0005\u00a5\u0000\u0000\u05cf\u05dc"+
		"\u0003<\u001e\u0000\u05d0\u05d1\u0005\u00b0\u0000\u0000\u05d1\u05d2\u0005"+
		"\u001a\u0000\u0000\u05d2\u05dc\u0003\u00c6c\u0000\u05d3\u05dc\u0003\u001c"+
		"\u000e\u0000\u05d4\u05dc\u0003\u001a\r\u0000\u05d5\u05dc\u0003\u00b4Z"+
		"\u0000\u05d6\u05dc\u0003H$\u0000\u05d7\u05dc\u0003\u001e\u000f\u0000\u05d8"+
		"\u05dc\u0003 \u0010\u0000\u05d9\u05da\u0005\u00f4\u0000\u0000\u05da\u05dc"+
		"\u0003<\u001e\u0000\u05db\u05ce\u0001\u0000\u0000\u0000\u05db\u05d0\u0001"+
		"\u0000\u0000\u0000\u05db\u05d3\u0001\u0000\u0000\u0000\u05db\u05d4\u0001"+
		"\u0000\u0000\u0000\u05db\u05d5\u0001\u0000\u0000\u0000\u05db\u05d6\u0001"+
		"\u0000\u0000\u0000\u05db\u05d7\u0001\u0000\u0000\u0000\u05db\u05d8\u0001"+
		"\u0000\u0000\u0000\u05db\u05d9\u0001\u0000\u0000\u0000\u05dc\u05df\u0001"+
		"\u0000\u0000\u0000\u05dd\u05db\u0001\u0000\u0000\u0000\u05dd\u05de\u0001"+
		"\u0000\u0000\u0000\u05de;\u0001\u0000\u0000\u0000\u05df\u05dd\u0001\u0000"+
		"\u0000\u0000\u05e0\u05e1\u0005\u0002\u0000\u0000\u05e1\u05e6\u0003>\u001f"+
		"\u0000\u05e2\u05e3\u0005\u0004\u0000\u0000\u05e3\u05e5\u0003>\u001f\u0000"+
		"\u05e4\u05e2\u0001\u0000\u0000\u0000\u05e5\u05e8\u0001\u0000\u0000\u0000"+
		"\u05e6\u05e4\u0001\u0000\u0000\u0000\u05e6\u05e7\u0001\u0000\u0000\u0000"+
		"\u05e7\u05e9\u0001\u0000\u0000\u0000\u05e8\u05e6\u0001\u0000\u0000\u0000"+
		"\u05e9\u05ea\u0005\u0003\u0000\u0000\u05ea=\u0001\u0000\u0000\u0000\u05eb"+
		"\u05f0\u0003@ \u0000\u05ec\u05ee\u0005\u011f\u0000\u0000\u05ed\u05ec\u0001"+
		"\u0000\u0000\u0000\u05ed\u05ee\u0001\u0000\u0000\u0000\u05ee\u05ef\u0001"+
		"\u0000\u0000\u0000\u05ef\u05f1\u0003B!\u0000\u05f0\u05ed\u0001\u0000\u0000"+
		"\u0000\u05f0\u05f1\u0001\u0000\u0000\u0000\u05f1?\u0001\u0000\u0000\u0000"+
		"\u05f2\u05f7\u0003\u0118\u008c\u0000\u05f3\u05f4\u0005\u0005\u0000\u0000"+
		"\u05f4\u05f6\u0003\u0118\u008c\u0000\u05f5\u05f3\u0001\u0000\u0000\u0000"+
		"\u05f6\u05f9\u0001\u0000\u0000\u0000\u05f7\u05f5\u0001\u0000\u0000\u0000"+
		"\u05f7\u05f8\u0001\u0000\u0000\u0000\u05f8\u05fc\u0001\u0000\u0000\u0000"+
		"\u05f9\u05f7\u0001\u0000\u0000\u0000\u05fa\u05fc\u0005\u0135\u0000\u0000"+
		"\u05fb\u05f2\u0001\u0000\u0000\u0000\u05fb\u05fa\u0001\u0000\u0000\u0000"+
		"\u05fcA\u0001\u0000\u0000\u0000\u05fd\u0602\u0005\u0139\u0000\u0000\u05fe"+
		"\u0602\u0005\u013b\u0000\u0000\u05ff\u0602\u0003\u00e4r\u0000\u0600\u0602"+
		"\u0005\u0135\u0000\u0000\u0601\u05fd\u0001\u0000\u0000\u0000\u0601\u05fe"+
		"\u0001\u0000\u0000\u0000\u0601\u05ff\u0001\u0000\u0000\u0000\u0601\u0600"+
		"\u0001\u0000\u0000\u0000\u0602C\u0001\u0000\u0000\u0000\u0603\u0604\u0005"+
		"\u0002\u0000\u0000\u0604\u0609\u0003\u00dcn\u0000\u0605\u0606\u0005\u0004"+
		"\u0000\u0000\u0606\u0608\u0003\u00dcn\u0000\u0607\u0605\u0001\u0000\u0000"+
		"\u0000\u0608\u060b\u0001\u0000\u0000\u0000\u0609\u0607\u0001\u0000\u0000"+
		"\u0000\u0609\u060a\u0001\u0000\u0000\u0000\u060a\u060c\u0001\u0000\u0000"+
		"\u0000\u060b\u0609\u0001\u0000\u0000\u0000\u060c\u060d\u0005\u0003\u0000"+
		"\u0000\u060dE\u0001\u0000\u0000\u0000\u060e\u060f\u0005\u0002\u0000\u0000"+
		"\u060f\u0614\u0003D\"\u0000\u0610\u0611\u0005\u0004\u0000\u0000\u0611"+
		"\u0613\u0003D\"\u0000\u0612\u0610\u0001\u0000\u0000\u0000\u0613\u0616"+
		"\u0001\u0000\u0000\u0000\u0614\u0612\u0001\u0000\u0000\u0000\u0614\u0615"+
		"\u0001\u0000\u0000\u0000\u0615\u0617\u0001\u0000\u0000\u0000\u0616\u0614"+
		"\u0001\u0000\u0000\u0000\u0617\u0618\u0005\u0003\u0000\u0000\u0618G\u0001"+
		"\u0000\u0000\u0000\u0619\u061a\u0005\u00e9\u0000\u0000\u061a\u061b\u0005"+
		"\u0012\u0000\u0000\u061b\u0620\u0003J%\u0000\u061c\u061d\u0005\u00e9\u0000"+
		"\u0000\u061d\u061e\u0005\u001a\u0000\u0000\u061e\u0620\u0003L&\u0000\u061f"+
		"\u0619\u0001\u0000\u0000\u0000\u061f\u061c\u0001\u0000\u0000\u0000\u0620"+
		"I\u0001\u0000\u0000\u0000\u0621\u0622\u0005x\u0000\u0000\u0622\u0623\u0005"+
		"\u0135\u0000\u0000\u0623\u0624\u0005\u00aa\u0000\u0000\u0624\u0627\u0005"+
		"\u0135\u0000\u0000\u0625\u0627\u0003\u0118\u008c\u0000\u0626\u0621\u0001"+
		"\u0000\u0000\u0000\u0626\u0625\u0001\u0000\u0000\u0000\u0627K\u0001\u0000"+
		"\u0000\u0000\u0628\u062c\u0005\u0135\u0000\u0000\u0629\u062a\u0005\u011b"+
		"\u0000\u0000\u062a\u062b\u0005\u00dd\u0000\u0000\u062b\u062d\u0003<\u001e"+
		"\u0000\u062c\u0629\u0001\u0000\u0000\u0000\u062c\u062d\u0001\u0000\u0000"+
		"\u0000\u062dM\u0001\u0000\u0000\u0000\u062e\u062f\u0003\u0118\u008c\u0000"+
		"\u062f\u0630\u0005\u0135\u0000\u0000\u0630O\u0001\u0000\u0000\u0000\u0631"+
		"\u0632\u0003$\u0012\u0000\u0632\u0633\u0003\"\u0011\u0000\u0633\u0664"+
		"\u0001\u0000\u0000\u0000\u0634\u0636\u0003|>\u0000\u0635\u0637\u0003T"+
		"*\u0000\u0636\u0635\u0001\u0000\u0000\u0000\u0637\u0638\u0001\u0000\u0000"+
		"\u0000\u0638\u0636\u0001\u0000\u0000\u0000\u0638\u0639\u0001\u0000\u0000"+
		"\u0000\u0639\u0664\u0001\u0000\u0000\u0000\u063a\u063b\u0005D\u0000\u0000"+
		"\u063b\u063c\u0005f\u0000\u0000\u063c\u063d\u0003\u00b8\\\u0000\u063d"+
		"\u063f\u0003\u00b2Y\u0000\u063e\u0640\u0003t:\u0000\u063f\u063e\u0001"+
		"\u0000\u0000\u0000\u063f\u0640\u0001\u0000\u0000\u0000\u0640\u0664\u0001"+
		"\u0000\u0000\u0000\u0641\u0642\u0005\u010f\u0000\u0000\u0642\u0643\u0003"+
		"\u00b8\\\u0000\u0643\u0644\u0003\u00b2Y\u0000\u0644\u0646\u0003f3\u0000"+
		"\u0645\u0647\u0003t:\u0000\u0646\u0645\u0001\u0000\u0000\u0000\u0646\u0647"+
		"\u0001\u0000\u0000\u0000\u0647\u0664\u0001\u0000\u0000\u0000\u0648\u0649"+
		"\u0005\u0094\u0000\u0000\u0649\u064a\u0005|\u0000\u0000\u064a\u064b\u0003"+
		"\u00b8\\\u0000\u064b\u064c\u0003\u00b2Y\u0000\u064c\u0652\u0005\u0112"+
		"\u0000\u0000\u064d\u0653\u0003\u00b8\\\u0000\u064e\u064f\u0005\u0002\u0000"+
		"\u0000\u064f\u0650\u0003\"\u0011\u0000\u0650\u0651\u0005\u0003\u0000\u0000"+
		"\u0651\u0653\u0001\u0000\u0000\u0000\u0652\u064d\u0001\u0000\u0000\u0000"+
		"\u0652\u064e\u0001\u0000\u0000\u0000\u0653\u0654\u0001\u0000\u0000\u0000"+
		"\u0654\u0655\u0003\u00b2Y\u0000\u0655\u0656\u0005\u00a2\u0000\u0000\u0656"+
		"\u065a\u0003\u00d2i\u0000\u0657\u0659\u0003h4\u0000\u0658\u0657\u0001"+
		"\u0000\u0000\u0000\u0659\u065c\u0001\u0000\u0000\u0000\u065a\u0658\u0001"+
		"\u0000\u0000\u0000\u065a\u065b\u0001\u0000\u0000\u0000\u065b\u0660\u0001"+
		"\u0000\u0000\u0000\u065c\u065a\u0001\u0000\u0000\u0000\u065d\u065f\u0003"+
		"j5\u0000\u065e\u065d\u0001\u0000\u0000\u0000\u065f\u0662\u0001\u0000\u0000"+
		"\u0000\u0660\u065e\u0001\u0000\u0000\u0000\u0660\u0661\u0001\u0000\u0000"+
		"\u0000\u0661\u0664\u0001\u0000\u0000\u0000\u0662\u0660\u0001\u0000\u0000"+
		"\u0000\u0663\u0631\u0001\u0000\u0000\u0000\u0663\u0634\u0001\u0000\u0000"+
		"\u0000\u0663\u063a\u0001\u0000\u0000\u0000\u0663\u0641\u0001\u0000\u0000"+
		"\u0000\u0663\u0648\u0001\u0000\u0000\u0000\u0664Q\u0001\u0000\u0000\u0000"+
		"\u0665\u0666\u0005\u00a7\u0000\u0000\u0666\u0667\u0005\u001a\u0000\u0000"+
		"\u0667\u066c\u0003Z-\u0000\u0668\u0669\u0005\u0004\u0000\u0000\u0669\u066b"+
		"\u0003Z-\u0000\u066a\u0668\u0001\u0000\u0000\u0000\u066b\u066e\u0001\u0000"+
		"\u0000\u0000\u066c\u066a\u0001\u0000\u0000\u0000\u066c\u066d\u0001\u0000"+
		"\u0000\u0000\u066d\u0670\u0001\u0000\u0000\u0000\u066e\u066c\u0001\u0000"+
		"\u0000\u0000\u066f\u0665\u0001\u0000\u0000\u0000\u066f\u0670\u0001\u0000"+
		"\u0000\u0000\u0670\u067b\u0001\u0000\u0000\u0000\u0671\u0672\u0005$\u0000"+
		"\u0000\u0672\u0673\u0005\u001a\u0000\u0000\u0673\u0678\u0003\u00ceg\u0000"+
		"\u0674\u0675\u0005\u0004\u0000\u0000\u0675\u0677\u0003\u00ceg\u0000\u0676"+
		"\u0674\u0001\u0000\u0000\u0000\u0677\u067a\u0001\u0000\u0000\u0000\u0678"+
		"\u0676\u0001\u0000\u0000\u0000\u0678\u0679\u0001\u0000\u0000\u0000\u0679"+
		"\u067c\u0001\u0000\u0000\u0000\u067a\u0678\u0001\u0000\u0000\u0000\u067b"+
		"\u0671\u0001\u0000\u0000\u0000\u067b\u067c\u0001\u0000\u0000\u0000\u067c"+
		"\u0687\u0001\u0000\u0000\u0000\u067d\u067e\u0005L\u0000\u0000\u067e\u067f"+
		"\u0005\u001a\u0000\u0000\u067f\u0684\u0003\u00ceg\u0000\u0680\u0681\u0005"+
		"\u0004\u0000\u0000\u0681\u0683\u0003\u00ceg\u0000\u0682\u0680\u0001\u0000"+
		"\u0000\u0000\u0683\u0686\u0001\u0000\u0000\u0000\u0684\u0682\u0001\u0000"+
		"\u0000\u0000\u0684\u0685\u0001\u0000\u0000\u0000\u0685\u0688\u0001\u0000"+
		"\u0000\u0000\u0686\u0684\u0001\u0000\u0000\u0000\u0687\u067d\u0001\u0000"+
		"\u0000\u0000\u0687\u0688\u0001\u0000\u0000\u0000\u0688\u0693\u0001\u0000"+
		"\u0000\u0000\u0689\u068a\u0005\u00e5\u0000\u0000\u068a\u068b\u0005\u001a"+
		"\u0000\u0000\u068b\u0690\u0003Z-\u0000\u068c\u068d\u0005\u0004\u0000\u0000"+
		"\u068d\u068f\u0003Z-\u0000\u068e\u068c\u0001\u0000\u0000\u0000\u068f\u0692"+
		"\u0001\u0000\u0000\u0000\u0690\u068e\u0001\u0000\u0000\u0000\u0690\u0691"+
		"\u0001\u0000\u0000\u0000\u0691\u0694\u0001\u0000\u0000\u0000\u0692\u0690"+
		"\u0001\u0000\u0000\u0000\u0693\u0689\u0001\u0000\u0000\u0000\u0693\u0694"+
		"\u0001\u0000\u0000\u0000\u0694\u0696\u0001\u0000\u0000\u0000\u0695\u0697"+
		"\u0003\u0104\u0082\u0000\u0696\u0695\u0001\u0000\u0000\u0000\u0696\u0697"+
		"\u0001\u0000\u0000\u0000\u0697\u069d\u0001\u0000\u0000\u0000\u0698\u069b"+
		"\u0005\u0088\u0000\u0000\u0699\u069c\u0005\n\u0000\u0000\u069a\u069c\u0003"+
		"\u00ceg\u0000\u069b\u0699\u0001\u0000\u0000\u0000\u069b\u069a\u0001\u0000"+
		"\u0000\u0000\u069c\u069e\u0001\u0000\u0000\u0000\u069d\u0698\u0001\u0000"+
		"\u0000\u0000\u069d\u069e\u0001\u0000\u0000\u0000\u069eS\u0001\u0000\u0000"+
		"\u0000\u069f\u06a0\u0003$\u0012\u0000\u06a0\u06a1\u0003^/\u0000\u06a1"+
		"U\u0001\u0000\u0000\u0000\u06a2\u06a3\u0006+\uffff\uffff\u0000\u06a3\u06a4"+
		"\u0003X,\u0000\u06a4\u06bc\u0001\u0000\u0000\u0000\u06a5\u06a6\n\u0003"+
		"\u0000\u0000\u06a6\u06a7\u0004+\u0001\u0000\u06a7\u06a9\u0007\u000f\u0000"+
		"\u0000\u06a8\u06aa\u0003\u0092I\u0000\u06a9\u06a8\u0001\u0000\u0000\u0000"+
		"\u06a9\u06aa\u0001\u0000\u0000\u0000\u06aa\u06ab\u0001\u0000\u0000\u0000"+
		"\u06ab\u06bb\u0003V+\u0004\u06ac\u06ad\n\u0002\u0000\u0000\u06ad\u06ae"+
		"\u0004+\u0003\u0000\u06ae\u06b0\u0005z\u0000\u0000\u06af\u06b1\u0003\u0092"+
		"I\u0000\u06b0\u06af\u0001\u0000\u0000\u0000\u06b0\u06b1\u0001\u0000\u0000"+
		"\u0000\u06b1\u06b2\u0001\u0000\u0000\u0000\u06b2\u06bb\u0003V+\u0003\u06b3"+
		"\u06b4\n\u0001\u0000\u0000\u06b4\u06b5\u0004+\u0005\u0000\u06b5\u06b7"+
		"\u0007\u0010\u0000\u0000\u06b6\u06b8\u0003\u0092I\u0000\u06b7\u06b6\u0001"+
		"\u0000\u0000\u0000\u06b7\u06b8\u0001\u0000\u0000\u0000\u06b8\u06b9\u0001"+
		"\u0000\u0000\u0000\u06b9\u06bb\u0003V+\u0002\u06ba\u06a5\u0001\u0000\u0000"+
		"\u0000\u06ba\u06ac\u0001\u0000\u0000\u0000\u06ba\u06b3\u0001\u0000\u0000"+
		"\u0000\u06bb\u06be\u0001\u0000\u0000\u0000\u06bc\u06ba\u0001\u0000\u0000"+
		"\u0000\u06bc\u06bd\u0001\u0000\u0000\u0000\u06bdW\u0001\u0000\u0000\u0000"+
		"\u06be\u06bc\u0001\u0000\u0000\u0000\u06bf\u06c9\u0003`0\u0000\u06c0\u06c9"+
		"\u0003\\.\u0000\u06c1\u06c2\u0005\u00f1\u0000\u0000\u06c2\u06c9\u0003"+
		"\u00b8\\\u0000\u06c3\u06c9\u0003\u00aeW\u0000\u06c4\u06c5\u0005\u0002"+
		"\u0000\u0000\u06c5\u06c6\u0003\"\u0011\u0000\u06c6\u06c7\u0005\u0003\u0000"+
		"\u0000\u06c7\u06c9\u0001\u0000\u0000\u0000\u06c8\u06bf\u0001\u0000\u0000"+
		"\u0000\u06c8\u06c0\u0001\u0000\u0000\u0000\u06c8\u06c1\u0001\u0000\u0000"+
		"\u0000\u06c8\u06c3\u0001\u0000\u0000\u0000\u06c8\u06c4\u0001\u0000\u0000"+
		"\u0000\u06c9Y\u0001\u0000\u0000\u0000\u06ca\u06cc\u0003\u00ceg\u0000\u06cb"+
		"\u06cd\u0007\u0011\u0000\u0000\u06cc\u06cb\u0001\u0000\u0000\u0000\u06cc"+
		"\u06cd\u0001\u0000\u0000\u0000\u06cd\u06d0\u0001\u0000\u0000\u0000\u06ce"+
		"\u06cf\u0005\u00a0\u0000\u0000\u06cf\u06d1\u0007\u0012\u0000\u0000\u06d0"+
		"\u06ce\u0001\u0000\u0000\u0000\u06d0\u06d1\u0001\u0000\u0000\u0000\u06d1"+
		"[\u0001\u0000\u0000\u0000\u06d2\u06d4\u0003|>\u0000\u06d3\u06d5\u0003"+
		"^/\u0000\u06d4\u06d3\u0001\u0000\u0000\u0000\u06d5\u06d6\u0001\u0000\u0000"+
		"\u0000\u06d6\u06d4\u0001\u0000\u0000\u0000\u06d6\u06d7\u0001\u0000\u0000"+
		"\u0000\u06d7]\u0001\u0000\u0000\u0000\u06d8\u06da\u0003b1\u0000\u06d9"+
		"\u06db\u0003t:\u0000\u06da\u06d9\u0001\u0000\u0000\u0000\u06da\u06db\u0001"+
		"\u0000\u0000\u0000\u06db\u06dc\u0001\u0000\u0000\u0000\u06dc\u06dd\u0003"+
		"R)\u0000\u06dd\u06f4\u0001\u0000\u0000\u0000\u06de\u06e2\u0003d2\u0000"+
		"\u06df\u06e1\u0003\u0090H\u0000\u06e0\u06df\u0001\u0000\u0000\u0000\u06e1"+
		"\u06e4\u0001\u0000\u0000\u0000\u06e2\u06e0\u0001\u0000\u0000\u0000\u06e2"+
		"\u06e3\u0001\u0000\u0000\u0000\u06e3\u06e6\u0001\u0000\u0000\u0000\u06e4"+
		"\u06e2\u0001\u0000\u0000\u0000\u06e5\u06e7\u0003t:\u0000\u06e6\u06e5\u0001"+
		"\u0000\u0000\u0000\u06e6\u06e7\u0001\u0000\u0000\u0000\u06e7\u06e9\u0001"+
		"\u0000\u0000\u0000\u06e8\u06ea\u0003\u0080@\u0000\u06e9\u06e8\u0001\u0000"+
		"\u0000\u0000\u06e9\u06ea\u0001\u0000\u0000\u0000\u06ea\u06ec\u0001\u0000"+
		"\u0000\u0000\u06eb\u06ed\u0003v;\u0000\u06ec\u06eb\u0001\u0000\u0000\u0000"+
		"\u06ec\u06ed\u0001\u0000\u0000\u0000\u06ed\u06ef\u0001\u0000\u0000\u0000"+
		"\u06ee\u06f0\u0003\u0104\u0082\u0000\u06ef\u06ee\u0001\u0000\u0000\u0000"+
		"\u06ef\u06f0\u0001\u0000\u0000\u0000\u06f0\u06f1\u0001\u0000\u0000\u0000"+
		"\u06f1\u06f2\u0003R)\u0000\u06f2\u06f4\u0001\u0000\u0000\u0000\u06f3\u06d8"+
		"\u0001\u0000\u0000\u0000\u06f3\u06de\u0001\u0000\u0000\u0000\u06f4_\u0001"+
		"\u0000\u0000\u0000\u06f5\u06f7\u0003b1\u0000\u06f6\u06f8\u0003|>\u0000"+
		"\u06f7\u06f6\u0001\u0000\u0000\u0000\u06f7\u06f8\u0001\u0000\u0000\u0000"+
		"\u06f8\u06fc\u0001\u0000\u0000\u0000\u06f9\u06fb\u0003\u0090H\u0000\u06fa"+
		"\u06f9\u0001\u0000\u0000\u0000\u06fb\u06fe\u0001\u0000\u0000\u0000\u06fc"+
		"\u06fa\u0001\u0000\u0000\u0000\u06fc\u06fd\u0001\u0000\u0000\u0000\u06fd"+
		"\u0700\u0001\u0000\u0000\u0000\u06fe\u06fc\u0001\u0000\u0000\u0000\u06ff"+
		"\u0701\u0003t:\u0000\u0700\u06ff\u0001\u0000\u0000\u0000\u0700\u0701\u0001"+
		"\u0000\u0000\u0000\u0701\u0703\u0001\u0000\u0000\u0000\u0702\u0704\u0003"+
		"\u0080@\u0000\u0703\u0702\u0001\u0000\u0000\u0000\u0703\u0704\u0001\u0000"+
		"\u0000\u0000\u0704\u0706\u0001\u0000\u0000\u0000\u0705\u0707\u0003v;\u0000"+
		"\u0706\u0705\u0001\u0000\u0000\u0000\u0706\u0707\u0001\u0000\u0000\u0000"+
		"\u0707\u0709\u0001\u0000\u0000\u0000\u0708\u070a\u0003\u0104\u0082\u0000"+
		"\u0709\u0708\u0001\u0000\u0000\u0000\u0709\u070a\u0001\u0000\u0000\u0000"+
		"\u070a\u0722\u0001\u0000\u0000\u0000\u070b\u070d\u0003d2\u0000\u070c\u070e"+
		"\u0003|>\u0000\u070d\u070c\u0001\u0000\u0000\u0000\u070d\u070e\u0001\u0000"+
		"\u0000\u0000\u070e\u0712\u0001\u0000\u0000\u0000\u070f\u0711\u0003\u0090"+
		"H\u0000\u0710\u070f\u0001\u0000\u0000\u0000\u0711\u0714\u0001\u0000\u0000"+
		"\u0000\u0712\u0710\u0001\u0000\u0000\u0000\u0712\u0713\u0001\u0000\u0000"+
		"\u0000\u0713\u0716\u0001\u0000\u0000\u0000\u0714\u0712\u0001\u0000\u0000"+
		"\u0000\u0715\u0717\u0003t:\u0000\u0716\u0715\u0001\u0000\u0000\u0000\u0716"+
		"\u0717\u0001\u0000\u0000\u0000\u0717\u0719\u0001\u0000\u0000\u0000\u0718"+
		"\u071a\u0003\u0080@\u0000\u0719\u0718\u0001\u0000\u0000\u0000\u0719\u071a"+
		"\u0001\u0000\u0000\u0000\u071a\u071c\u0001\u0000\u0000\u0000\u071b\u071d"+
		"\u0003v;\u0000\u071c\u071b\u0001\u0000\u0000\u0000\u071c\u071d\u0001\u0000"+
		"\u0000\u0000\u071d\u071f\u0001\u0000\u0000\u0000\u071e\u0720\u0003\u0104"+
		"\u0082\u0000\u071f\u071e\u0001\u0000\u0000\u0000\u071f\u0720\u0001\u0000"+
		"\u0000\u0000\u0720\u0722\u0001\u0000\u0000\u0000\u0721\u06f5\u0001\u0000"+
		"\u0000\u0000\u0721\u070b\u0001\u0000\u0000\u0000\u0722a\u0001\u0000\u0000"+
		"\u0000\u0723\u0724\u0005\u00d9\u0000\u0000\u0724\u0725\u0005\u0101\u0000"+
		"\u0000\u0725\u0727\u0005\u0002\u0000\u0000\u0726\u0728\u0003\u0092I\u0000"+
		"\u0727\u0726\u0001\u0000\u0000\u0000\u0727\u0728\u0001\u0000\u0000\u0000"+
		"\u0728\u0729\u0001\u0000\u0000\u0000\u0729\u072a\u0003\u00d0h\u0000\u072a"+
		"\u072b\u0005\u0003\u0000\u0000\u072b\u0737\u0001\u0000\u0000\u0000\u072c"+
		"\u072e\u0005\u0092\u0000\u0000\u072d\u072f\u0003\u0092I\u0000\u072e\u072d"+
		"\u0001\u0000\u0000\u0000\u072e\u072f\u0001\u0000\u0000\u0000\u072f\u0730"+
		"\u0001\u0000\u0000\u0000\u0730\u0737\u0003\u00d0h\u0000\u0731\u0733\u0005"+
		"\u00c3\u0000\u0000\u0732\u0734\u0003\u0092I\u0000\u0733\u0732\u0001\u0000"+
		"\u0000\u0000\u0733\u0734\u0001\u0000\u0000\u0000\u0734\u0735\u0001\u0000"+
		"\u0000\u0000\u0735\u0737\u0003\u00d0h\u0000\u0736\u0723\u0001\u0000\u0000"+
		"\u0000\u0736\u072c\u0001\u0000\u0000\u0000\u0736\u0731\u0001\u0000\u0000"+
		"\u0000\u0737\u0739\u0001\u0000\u0000\u0000\u0738\u073a\u0003\u00b4Z\u0000"+
		"\u0739\u0738\u0001\u0000\u0000\u0000\u0739\u073a\u0001\u0000\u0000\u0000"+
		"\u073a\u073d\u0001\u0000\u0000\u0000\u073b\u073c\u0005\u00c1\u0000\u0000"+
		"\u073c\u073e\u0005\u0135\u0000\u0000\u073d\u073b\u0001\u0000\u0000\u0000"+
		"\u073d\u073e\u0001\u0000\u0000\u0000\u073e\u073f\u0001\u0000\u0000\u0000"+
		"\u073f\u0740\u0005\u0112\u0000\u0000\u0740\u074d\u0005\u0135\u0000\u0000"+
		"\u0741\u074b\u0005\u0012\u0000\u0000\u0742\u074c\u0003\u00a2Q\u0000\u0743"+
		"\u074c\u0003\u00fa}\u0000\u0744\u0747\u0005\u0002\u0000\u0000\u0745\u0748"+
		"\u0003\u00a2Q\u0000\u0746\u0748\u0003\u00fa}\u0000\u0747\u0745\u0001\u0000"+
		"\u0000\u0000\u0747\u0746\u0001\u0000\u0000\u0000\u0748\u0749\u0001\u0000"+
		"\u0000\u0000\u0749\u074a\u0005\u0003\u0000\u0000\u074a\u074c\u0001\u0000"+
		"\u0000\u0000\u074b\u0742\u0001\u0000\u0000\u0000\u074b\u0743\u0001\u0000"+
		"\u0000\u0000\u074b\u0744\u0001\u0000\u0000\u0000\u074c\u074e\u0001\u0000"+
		"\u0000\u0000\u074d\u0741\u0001\u0000\u0000\u0000\u074d\u074e\u0001\u0000"+
		"\u0000\u0000\u074e\u0750\u0001\u0000\u0000\u0000\u074f\u0751\u0003\u00b4"+
		"Z\u0000\u0750\u074f\u0001\u0000\u0000\u0000\u0750\u0751\u0001\u0000\u0000"+
		"\u0000\u0751\u0754\u0001\u0000\u0000\u0000\u0752\u0753\u0005\u00c0\u0000"+
		"\u0000\u0753\u0755\u0005\u0135\u0000\u0000\u0754\u0752\u0001\u0000\u0000"+
		"\u0000\u0754\u0755\u0001\u0000\u0000\u0000\u0755c\u0001\u0000\u0000\u0000"+
		"\u0756\u075a\u0005\u00d9\u0000\u0000\u0757\u0759\u0003x<\u0000\u0758\u0757"+
		"\u0001\u0000\u0000\u0000\u0759\u075c\u0001\u0000\u0000\u0000\u075a\u0758"+
		"\u0001\u0000\u0000\u0000\u075a\u075b\u0001\u0000\u0000\u0000\u075b\u075e"+
		"\u0001\u0000\u0000\u0000\u075c\u075a\u0001\u0000\u0000\u0000\u075d\u075f"+
		"\u0003\u0092I\u0000\u075e\u075d\u0001\u0000\u0000\u0000\u075e\u075f\u0001"+
		"\u0000\u0000\u0000\u075f\u0760\u0001\u0000\u0000\u0000\u0760\u0761\u0003"+
		"\u00c4b\u0000\u0761e\u0001\u0000\u0000\u0000\u0762\u0763\u0005\u00df\u0000"+
		"\u0000\u0763\u0764\u0003p8\u0000\u0764g\u0001\u0000\u0000\u0000\u0765"+
		"\u0766\u0005\u0118\u0000\u0000\u0766\u0769\u0005\u0093\u0000\u0000\u0767"+
		"\u0768\u0005\r\u0000\u0000\u0768\u076a\u0003\u00d2i\u0000\u0769\u0767"+
		"\u0001\u0000\u0000\u0000\u0769\u076a\u0001\u0000\u0000\u0000\u076a\u076b"+
		"\u0001\u0000\u0000\u0000\u076b\u076c\u0005\u00f7\u0000\u0000\u076c\u076d"+
		"\u0003l6\u0000\u076di\u0001\u0000\u0000\u0000\u076e\u076f\u0005\u0118"+
		"\u0000\u0000\u076f\u0770\u0005\u009e\u0000\u0000\u0770\u0773\u0005\u0093"+
		"\u0000\u0000\u0771\u0772\u0005\r\u0000\u0000\u0772\u0774\u0003\u00d2i"+
		"\u0000\u0773\u0771\u0001\u0000\u0000\u0000\u0773\u0774\u0001\u0000\u0000"+
		"\u0000\u0774\u0775\u0001\u0000\u0000\u0000\u0775\u0776\u0005\u00f7\u0000"+
		"\u0000\u0776\u0777\u0003n7\u0000\u0777k\u0001\u0000\u0000\u0000\u0778"+
		"\u0780\u0005D\u0000\u0000\u0779\u077a\u0005\u010f\u0000\u0000\u077a\u077b"+
		"\u0005\u00df\u0000\u0000\u077b\u0780\u0005\u0129\u0000\u0000\u077c\u077d"+
		"\u0005\u010f\u0000\u0000\u077d\u077e\u0005\u00df\u0000\u0000\u077e\u0780"+
		"\u0003p8\u0000\u077f\u0778\u0001\u0000\u0000\u0000\u077f\u0779\u0001\u0000"+
		"\u0000\u0000\u077f\u077c\u0001\u0000\u0000\u0000\u0780m\u0001\u0000\u0000"+
		"\u0000\u0781\u0782\u0005y\u0000\u0000\u0782\u0794\u0005\u0129\u0000\u0000"+
		"\u0783\u0784\u0005y\u0000\u0000\u0784\u0785\u0005\u0002\u0000\u0000\u0785"+
		"\u0786\u0003\u00b6[\u0000\u0786\u0787\u0005\u0003\u0000\u0000\u0787\u0788"+
		"\u0005\u0113\u0000\u0000\u0788\u0789\u0005\u0002\u0000\u0000\u0789\u078e"+
		"\u0003\u00ceg\u0000\u078a\u078b\u0005\u0004\u0000\u0000\u078b\u078d\u0003"+
		"\u00ceg\u0000\u078c\u078a\u0001\u0000\u0000\u0000\u078d\u0790\u0001\u0000"+
		"\u0000\u0000\u078e\u078c\u0001\u0000\u0000\u0000\u078e\u078f\u0001\u0000"+
		"\u0000\u0000\u078f\u0791\u0001\u0000\u0000\u0000\u0790\u078e\u0001\u0000"+
		"\u0000\u0000\u0791\u0792\u0005\u0003\u0000\u0000\u0792\u0794\u0001\u0000"+
		"\u0000\u0000\u0793\u0781\u0001\u0000\u0000\u0000\u0793\u0783\u0001\u0000"+
		"\u0000\u0000\u0794o\u0001\u0000\u0000\u0000\u0795\u079a\u0003r9\u0000"+
		"\u0796\u0797\u0005\u0004\u0000\u0000\u0797\u0799\u0003r9\u0000\u0798\u0796"+
		"\u0001\u0000\u0000\u0000\u0799\u079c\u0001\u0000\u0000\u0000\u079a\u0798"+
		"\u0001\u0000\u0000\u0000\u079a\u079b\u0001\u0000\u0000\u0000\u079bq\u0001"+
		"\u0000\u0000\u0000\u079c\u079a\u0001\u0000\u0000\u0000\u079d\u079e\u0003"+
		"\u00b8\\\u0000\u079e\u079f\u0005\u011f\u0000\u0000\u079f\u07a0\u0003\u00ce"+
		"g\u0000\u07a0s\u0001\u0000\u0000\u0000\u07a1\u07a2\u0005\u0119\u0000\u0000"+
		"\u07a2\u07a3\u0003\u00d2i\u0000\u07a3u\u0001\u0000\u0000\u0000\u07a4\u07a5"+
		"\u0005n\u0000\u0000\u07a5\u07a6\u0003\u00d2i\u0000\u07a6w\u0001\u0000"+
		"\u0000\u0000\u07a7\u07a8\u0005\u0133\u0000\u0000\u07a8\u07af\u0003z=\u0000"+
		"\u07a9\u07ab\u0005\u0004\u0000\u0000\u07aa\u07a9\u0001\u0000\u0000\u0000"+
		"\u07aa\u07ab\u0001\u0000\u0000\u0000\u07ab\u07ac\u0001\u0000\u0000\u0000"+
		"\u07ac\u07ae\u0003z=\u0000\u07ad\u07aa\u0001\u0000\u0000\u0000\u07ae\u07b1"+
		"\u0001\u0000\u0000\u0000\u07af\u07ad\u0001\u0000\u0000\u0000\u07af\u07b0"+
		"\u0001\u0000\u0000\u0000\u07b0\u07b2\u0001\u0000\u0000\u0000\u07b1\u07af"+
		"\u0001\u0000\u0000\u0000\u07b2\u07b3\u0005\u0134\u0000\u0000\u07b3y\u0001"+
		"\u0000\u0000\u0000\u07b4\u07c2\u0003\u0118\u008c\u0000\u07b5\u07b6\u0003"+
		"\u0118\u008c\u0000\u07b6\u07b7\u0005\u0002\u0000\u0000\u07b7\u07bc\u0003"+
		"\u00dam\u0000\u07b8\u07b9\u0005\u0004\u0000\u0000\u07b9\u07bb\u0003\u00da"+
		"m\u0000\u07ba\u07b8\u0001\u0000\u0000\u0000\u07bb\u07be\u0001\u0000\u0000"+
		"\u0000\u07bc\u07ba\u0001\u0000\u0000\u0000\u07bc\u07bd\u0001\u0000\u0000"+
		"\u0000\u07bd\u07bf\u0001\u0000\u0000\u0000\u07be\u07bc\u0001\u0000\u0000"+
		"\u0000\u07bf\u07c0\u0005\u0003\u0000\u0000\u07c0\u07c2\u0001\u0000\u0000"+
		"\u0000\u07c1\u07b4\u0001\u0000\u0000\u0000\u07c1\u07b5\u0001\u0000\u0000"+
		"\u0000\u07c2{\u0001\u0000\u0000\u0000\u07c3\u07c4\u0005f\u0000\u0000\u07c4"+
		"\u07c9\u0003\u0094J\u0000\u07c5\u07c6\u0005\u0004\u0000\u0000\u07c6\u07c8"+
		"\u0003\u0094J\u0000\u07c7\u07c5\u0001\u0000\u0000\u0000\u07c8\u07cb\u0001"+
		"\u0000\u0000\u0000\u07c9\u07c7\u0001\u0000\u0000\u0000\u07c9\u07ca\u0001"+
		"\u0000\u0000\u0000\u07ca\u07cf\u0001\u0000\u0000\u0000\u07cb\u07c9\u0001"+
		"\u0000\u0000\u0000\u07cc\u07ce\u0003\u0090H\u0000\u07cd\u07cc\u0001\u0000"+
		"\u0000\u0000\u07ce\u07d1\u0001\u0000\u0000\u0000\u07cf\u07cd\u0001\u0000"+
		"\u0000\u0000\u07cf\u07d0\u0001\u0000\u0000\u0000\u07d0\u07d3\u0001\u0000"+
		"\u0000\u0000\u07d1\u07cf\u0001\u0000\u0000\u0000\u07d2\u07d4\u0003\u008a"+
		"E\u0000\u07d3\u07d2\u0001\u0000\u0000\u0000\u07d3\u07d4\u0001\u0000\u0000"+
		"\u0000\u07d4}\u0001\u0000\u0000\u0000\u07d5\u07d7\u0005b\u0000\u0000\u07d6"+
		"\u07d5\u0001\u0000\u0000\u0000\u07d6\u07d7\u0001\u0000\u0000\u0000\u07d7"+
		"\u07d8\u0001\u0000\u0000\u0000\u07d8\u07d9\u0007\u0013\u0000\u0000\u07d9"+
		"\u07da\u0005\u0012\u0000\u0000\u07da\u07db\u0005\u00a1\u0000\u0000\u07db"+
		"\u07e4\u0007\u0014\u0000\u0000\u07dc\u07de\u0005b\u0000\u0000\u07dd\u07dc"+
		"\u0001\u0000\u0000\u0000\u07dd\u07de\u0001\u0000\u0000\u0000\u07de\u07df"+
		"\u0001\u0000\u0000\u0000\u07df\u07e0\u0007\u0015\u0000\u0000\u07e0\u07e1"+
		"\u0005\u0012\u0000\u0000\u07e1\u07e2\u0005\u00a1\u0000\u0000\u07e2\u07e4"+
		"\u0003\u00d6k\u0000\u07e3\u07d6\u0001\u0000\u0000\u0000\u07e3\u07dd\u0001"+
		"\u0000\u0000\u0000\u07e4\u007f\u0001\u0000\u0000\u0000\u07e5\u07e6\u0005"+
		"l\u0000\u0000\u07e6\u07e7\u0005\u001a\u0000\u0000\u07e7\u07ec\u0003\u0082"+
		"A\u0000\u07e8\u07e9\u0005\u0004\u0000\u0000\u07e9\u07eb\u0003\u0082A\u0000"+
		"\u07ea\u07e8\u0001\u0000\u0000\u0000\u07eb\u07ee\u0001\u0000\u0000\u0000"+
		"\u07ec\u07ea\u0001\u0000\u0000\u0000\u07ec\u07ed\u0001\u0000\u0000\u0000"+
		"\u07ed\u080d\u0001\u0000\u0000\u0000\u07ee\u07ec\u0001\u0000\u0000\u0000"+
		"\u07ef\u07f0\u0005l\u0000\u0000\u07f0\u07f1\u0005\u001a\u0000\u0000\u07f1"+
		"\u07f6\u0003\u00ceg\u0000\u07f2\u07f3\u0005\u0004\u0000\u0000\u07f3\u07f5"+
		"\u0003\u00ceg\u0000\u07f4\u07f2\u0001\u0000\u0000\u0000\u07f5\u07f8\u0001"+
		"\u0000\u0000\u0000\u07f6\u07f4\u0001\u0000\u0000\u0000\u07f6\u07f7\u0001"+
		"\u0000\u0000\u0000\u07f7\u080a\u0001\u0000\u0000\u0000\u07f8\u07f6\u0001"+
		"\u0000\u0000\u0000\u07f9\u07fa\u0005\u011b\u0000\u0000\u07fa\u080b\u0005"+
		"\u00d3\u0000\u0000\u07fb\u07fc\u0005\u011b\u0000\u0000\u07fc\u080b\u0005"+
		"5\u0000\u0000\u07fd\u07fe\u0005m\u0000\u0000\u07fe\u07ff\u0005\u00e1\u0000"+
		"\u0000\u07ff\u0800\u0005\u0002\u0000\u0000\u0800\u0805\u0003\u0088D\u0000"+
		"\u0801\u0802\u0005\u0004\u0000\u0000\u0802\u0804\u0003\u0088D\u0000\u0803"+
		"\u0801\u0001\u0000\u0000\u0000\u0804\u0807\u0001\u0000\u0000\u0000\u0805"+
		"\u0803\u0001\u0000\u0000\u0000\u0805\u0806\u0001\u0000\u0000\u0000\u0806"+
		"\u0808\u0001\u0000\u0000\u0000\u0807\u0805\u0001\u0000\u0000\u0000\u0808"+
		"\u0809\u0005\u0003\u0000\u0000\u0809\u080b\u0001\u0000\u0000\u0000\u080a"+
		"\u07f9\u0001\u0000\u0000\u0000\u080a\u07fb\u0001\u0000\u0000\u0000\u080a"+
		"\u07fd\u0001\u0000\u0000\u0000\u080a\u080b\u0001\u0000\u0000\u0000\u080b"+
		"\u080d\u0001\u0000\u0000\u0000\u080c\u07e5\u0001\u0000\u0000\u0000\u080c"+
		"\u07ef\u0001\u0000\u0000\u0000\u080d\u0081\u0001\u0000\u0000\u0000\u080e"+
		"\u0811\u0003\u0084B\u0000\u080f\u0811\u0003\u00ceg\u0000\u0810\u080e\u0001"+
		"\u0000\u0000\u0000\u0810\u080f\u0001\u0000\u0000\u0000\u0811\u0083\u0001"+
		"\u0000\u0000\u0000\u0812\u0813\u0007\u0016\u0000\u0000\u0813\u0814\u0005"+
		"\u0002\u0000\u0000\u0814\u0819\u0003\u0088D\u0000\u0815\u0816\u0005\u0004"+
		"\u0000\u0000\u0816\u0818\u0003\u0088D\u0000\u0817\u0815\u0001\u0000\u0000"+
		"\u0000\u0818\u081b\u0001\u0000\u0000\u0000\u0819\u0817\u0001\u0000\u0000"+
		"\u0000\u0819\u081a\u0001\u0000\u0000\u0000\u081a\u081c\u0001\u0000\u0000"+
		"\u0000\u081b\u0819\u0001\u0000\u0000\u0000\u081c\u081d\u0005\u0003\u0000"+
		"\u0000\u081d\u082c\u0001\u0000\u0000\u0000\u081e\u081f\u0005m\u0000";
	private static final String _serializedATNSegment1 =
		"\u0000\u081f\u0820\u0005\u00e1\u0000\u0000\u0820\u0821\u0005\u0002\u0000"+
		"\u0000\u0821\u0826\u0003\u0086C\u0000\u0822\u0823\u0005\u0004\u0000\u0000"+
		"\u0823\u0825\u0003\u0086C\u0000\u0824\u0822\u0001\u0000\u0000\u0000\u0825"+
		"\u0828\u0001\u0000\u0000\u0000\u0826\u0824\u0001\u0000\u0000\u0000\u0826"+
		"\u0827\u0001\u0000\u0000\u0000\u0827\u0829\u0001\u0000\u0000\u0000\u0828"+
		"\u0826\u0001\u0000\u0000\u0000\u0829\u082a\u0005\u0003\u0000\u0000\u082a"+
		"\u082c\u0001\u0000\u0000\u0000\u082b\u0812\u0001\u0000\u0000\u0000\u082b"+
		"\u081e\u0001\u0000\u0000\u0000\u082c\u0085\u0001\u0000\u0000\u0000\u082d"+
		"\u0830\u0003\u0084B\u0000\u082e\u0830\u0003\u0088D\u0000\u082f\u082d\u0001"+
		"\u0000\u0000\u0000\u082f\u082e\u0001\u0000\u0000\u0000\u0830\u0087\u0001"+
		"\u0000\u0000\u0000\u0831\u083a\u0005\u0002\u0000\u0000\u0832\u0837\u0003"+
		"\u00ceg\u0000\u0833\u0834\u0005\u0004\u0000\u0000\u0834\u0836\u0003\u00ce"+
		"g\u0000\u0835\u0833\u0001\u0000\u0000\u0000\u0836\u0839\u0001\u0000\u0000"+
		"\u0000\u0837\u0835\u0001\u0000\u0000\u0000\u0837\u0838\u0001\u0000\u0000"+
		"\u0000\u0838\u083b\u0001\u0000\u0000\u0000\u0839\u0837\u0001\u0000\u0000"+
		"\u0000\u083a\u0832\u0001\u0000\u0000\u0000\u083a\u083b\u0001\u0000\u0000"+
		"\u0000\u083b\u083c\u0001\u0000\u0000\u0000\u083c\u083f\u0005\u0003\u0000"+
		"\u0000\u083d\u083f\u0003\u00ceg\u0000\u083e\u0831\u0001\u0000\u0000\u0000"+
		"\u083e\u083d\u0001\u0000\u0000\u0000\u083f\u0089\u0001\u0000\u0000\u0000"+
		"\u0840\u0841\u0005\u00b5\u0000\u0000\u0841\u0842\u0005\u0002\u0000\u0000"+
		"\u0842\u0843\u0003\u00c4b\u0000\u0843\u0844\u0005b\u0000\u0000\u0844\u0845"+
		"\u0003\u008cF\u0000\u0845\u0846\u0005s\u0000\u0000\u0846\u0847\u0005\u0002"+
		"\u0000\u0000\u0847\u084c\u0003\u008eG\u0000\u0848\u0849\u0005\u0004\u0000"+
		"\u0000\u0849\u084b\u0003\u008eG\u0000\u084a\u0848\u0001\u0000\u0000\u0000"+
		"\u084b\u084e\u0001\u0000\u0000\u0000\u084c\u084a\u0001\u0000\u0000\u0000"+
		"\u084c\u084d\u0001\u0000\u0000\u0000\u084d\u084f\u0001\u0000\u0000\u0000"+
		"\u084e\u084c\u0001\u0000\u0000\u0000\u084f\u0850\u0005\u0003\u0000\u0000"+
		"\u0850\u0851\u0005\u0003\u0000\u0000\u0851\u008b\u0001\u0000\u0000\u0000"+
		"\u0852\u085f\u0003\u0118\u008c\u0000\u0853\u0854\u0005\u0002\u0000\u0000"+
		"\u0854\u0859\u0003\u0118\u008c\u0000\u0855\u0856\u0005\u0004\u0000\u0000"+
		"\u0856\u0858\u0003\u0118\u008c\u0000\u0857\u0855\u0001\u0000\u0000\u0000"+
		"\u0858\u085b\u0001\u0000\u0000\u0000\u0859\u0857\u0001\u0000\u0000\u0000"+
		"\u0859\u085a\u0001\u0000\u0000\u0000\u085a\u085c\u0001\u0000\u0000\u0000"+
		"\u085b\u0859\u0001\u0000\u0000\u0000\u085c\u085d\u0005\u0003\u0000\u0000"+
		"\u085d\u085f\u0001\u0000\u0000\u0000\u085e\u0852\u0001\u0000\u0000\u0000"+
		"\u085e\u0853\u0001\u0000\u0000\u0000\u085f\u008d\u0001\u0000\u0000\u0000"+
		"\u0860\u0865\u0003\u00ceg\u0000\u0861\u0863\u0005\u0012\u0000\u0000\u0862"+
		"\u0861\u0001\u0000\u0000\u0000\u0862\u0863\u0001\u0000\u0000\u0000\u0863"+
		"\u0864\u0001\u0000\u0000\u0000\u0864\u0866\u0003\u0118\u008c\u0000\u0865"+
		"\u0862\u0001\u0000\u0000\u0000\u0865\u0866\u0001\u0000\u0000\u0000\u0866"+
		"\u008f\u0001\u0000\u0000\u0000\u0867\u0868\u0005\u0082\u0000\u0000\u0868"+
		"\u086a\u0005\u0115\u0000\u0000\u0869\u086b\u0005\u00a9\u0000\u0000\u086a"+
		"\u0869\u0001\u0000\u0000\u0000\u086a\u086b\u0001\u0000\u0000\u0000\u086b"+
		"\u086c\u0001\u0000\u0000\u0000\u086c\u086d\u0003\u0112\u0089\u0000\u086d"+
		"\u0876\u0005\u0002\u0000\u0000\u086e\u0873\u0003\u00ceg\u0000\u086f\u0870"+
		"\u0005\u0004\u0000\u0000\u0870\u0872\u0003\u00ceg\u0000\u0871\u086f\u0001"+
		"\u0000\u0000\u0000\u0872\u0875\u0001\u0000\u0000\u0000\u0873\u0871\u0001"+
		"\u0000\u0000\u0000\u0873\u0874\u0001\u0000\u0000\u0000\u0874\u0877\u0001"+
		"\u0000\u0000\u0000\u0875\u0873\u0001\u0000\u0000\u0000\u0876\u086e\u0001"+
		"\u0000\u0000\u0000\u0876\u0877\u0001\u0000\u0000\u0000\u0877\u0878\u0001"+
		"\u0000\u0000\u0000\u0878\u0879\u0005\u0003\u0000\u0000\u0879\u0885\u0003"+
		"\u0118\u008c\u0000\u087a\u087c\u0005\u0012\u0000\u0000\u087b\u087a\u0001"+
		"\u0000\u0000\u0000\u087b\u087c\u0001\u0000\u0000\u0000\u087c\u087d\u0001"+
		"\u0000\u0000\u0000\u087d\u0882\u0003\u0118\u008c\u0000\u087e\u087f\u0005"+
		"\u0004\u0000\u0000\u087f\u0881\u0003\u0118\u008c\u0000\u0880\u087e\u0001"+
		"\u0000\u0000\u0000\u0881\u0884\u0001\u0000\u0000\u0000\u0882\u0880\u0001"+
		"\u0000\u0000\u0000\u0882\u0883\u0001\u0000\u0000\u0000\u0883\u0886\u0001"+
		"\u0000\u0000\u0000\u0884\u0882\u0001\u0000\u0000\u0000\u0885\u087b\u0001"+
		"\u0000\u0000\u0000\u0885\u0886\u0001\u0000\u0000\u0000\u0886\u0091\u0001"+
		"\u0000\u0000\u0000\u0887\u0888\u0007\u0017\u0000\u0000\u0888\u0093\u0001"+
		"\u0000\u0000\u0000\u0889\u088b\u0005\u0082\u0000\u0000\u088a\u0889\u0001"+
		"\u0000\u0000\u0000\u088a\u088b\u0001\u0000\u0000\u0000\u088b\u088c\u0001"+
		"\u0000\u0000\u0000\u088c\u0890\u0003\u00acV\u0000\u088d\u088f\u0003\u0096"+
		"K\u0000\u088e\u088d\u0001\u0000\u0000\u0000\u088f\u0892\u0001\u0000\u0000"+
		"\u0000\u0890\u088e\u0001\u0000\u0000\u0000\u0890\u0891\u0001\u0000\u0000"+
		"\u0000\u0891\u0095\u0001\u0000\u0000\u0000\u0892\u0890\u0001\u0000\u0000"+
		"\u0000\u0893\u0894\u0003\u0098L\u0000\u0894\u0896\u0005\u007f\u0000\u0000"+
		"\u0895\u0897\u0005\u0082\u0000\u0000\u0896\u0895\u0001\u0000\u0000\u0000"+
		"\u0896\u0897\u0001\u0000\u0000\u0000\u0897\u0898\u0001\u0000\u0000\u0000"+
		"\u0898\u089a\u0003\u00acV\u0000\u0899\u089b\u0003\u009aM\u0000\u089a\u0899"+
		"\u0001\u0000\u0000\u0000\u089a\u089b\u0001\u0000\u0000\u0000\u089b\u08a5"+
		"\u0001\u0000\u0000\u0000\u089c\u089d\u0005\u009c\u0000\u0000\u089d\u089e"+
		"\u0003\u0098L\u0000\u089e\u08a0\u0005\u007f\u0000\u0000\u089f\u08a1\u0005"+
		"\u0082\u0000\u0000\u08a0\u089f\u0001\u0000\u0000\u0000\u08a0\u08a1\u0001"+
		"\u0000\u0000\u0000\u08a1\u08a2\u0001\u0000\u0000\u0000\u08a2\u08a3\u0003"+
		"\u00acV\u0000\u08a3\u08a5\u0001\u0000\u0000\u0000\u08a4\u0893\u0001\u0000"+
		"\u0000\u0000\u08a4\u089c\u0001\u0000\u0000\u0000\u08a5\u0097\u0001\u0000"+
		"\u0000\u0000\u08a6\u08a8\u0005v\u0000\u0000\u08a7\u08a6\u0001\u0000\u0000"+
		"\u0000\u08a7\u08a8\u0001\u0000\u0000\u0000\u08a8\u08bf\u0001\u0000\u0000"+
		"\u0000\u08a9\u08bf\u00054\u0000\u0000\u08aa\u08ac\u0005\u0085\u0000\u0000"+
		"\u08ab\u08ad\u0005\u00a9\u0000\u0000\u08ac\u08ab\u0001\u0000\u0000\u0000"+
		"\u08ac\u08ad\u0001\u0000\u0000\u0000\u08ad\u08bf\u0001\u0000\u0000\u0000"+
		"\u08ae\u08b0\u0005\u0085\u0000\u0000\u08af\u08ae\u0001\u0000\u0000\u0000"+
		"\u08af\u08b0\u0001\u0000\u0000\u0000\u08b0\u08b1\u0001\u0000\u0000\u0000"+
		"\u08b1\u08bf\u0005\u00da\u0000\u0000\u08b2\u08b4\u0005\u00ce\u0000\u0000"+
		"\u08b3\u08b5\u0005\u00a9\u0000\u0000\u08b4\u08b3\u0001\u0000\u0000\u0000"+
		"\u08b4\u08b5\u0001\u0000\u0000\u0000\u08b5\u08bf\u0001\u0000\u0000\u0000"+
		"\u08b6\u08b8\u0005g\u0000\u0000\u08b7\u08b9\u0005\u00a9\u0000\u0000\u08b8"+
		"\u08b7\u0001\u0000\u0000\u0000\u08b8\u08b9\u0001\u0000\u0000\u0000\u08b9"+
		"\u08bf\u0001\u0000\u0000\u0000\u08ba\u08bc\u0005\u0085\u0000\u0000\u08bb"+
		"\u08ba\u0001\u0000\u0000\u0000\u08bb\u08bc\u0001\u0000\u0000\u0000\u08bc"+
		"\u08bd\u0001\u0000\u0000\u0000\u08bd\u08bf\u0005\u000e\u0000\u0000\u08be"+
		"\u08a7\u0001\u0000\u0000\u0000\u08be\u08a9\u0001\u0000\u0000\u0000\u08be"+
		"\u08aa\u0001\u0000\u0000\u0000\u08be\u08af\u0001\u0000\u0000\u0000\u08be"+
		"\u08b2\u0001\u0000\u0000\u0000\u08be\u08b6\u0001\u0000\u0000\u0000\u08be"+
		"\u08bb\u0001\u0000\u0000\u0000\u08bf\u0099\u0001\u0000\u0000\u0000\u08c0"+
		"\u08c1\u0005\u00a2\u0000\u0000\u08c1\u08c5\u0003\u00d2i\u0000\u08c2\u08c3"+
		"\u0005\u0112\u0000\u0000\u08c3\u08c5\u0003\u00a0P\u0000\u08c4\u08c0\u0001"+
		"\u0000\u0000\u0000\u08c4\u08c2\u0001\u0000\u0000\u0000\u08c5\u009b\u0001"+
		"\u0000\u0000\u0000\u08c6\u08c7\u0005\u00f3\u0000\u0000\u08c7\u08c9\u0005"+
		"\u0002\u0000\u0000\u08c8\u08ca\u0003\u009eO\u0000\u08c9\u08c8\u0001\u0000"+
		"\u0000\u0000\u08c9\u08ca\u0001\u0000\u0000\u0000\u08ca\u08cb\u0001\u0000"+
		"\u0000\u0000\u08cb\u08d0\u0005\u0003\u0000\u0000\u08cc\u08cd\u0005\u00c8"+
		"\u0000\u0000\u08cd\u08ce\u0005\u0002\u0000\u0000\u08ce\u08cf\u0005\u0139"+
		"\u0000\u0000\u08cf\u08d1\u0005\u0003\u0000\u0000\u08d0\u08cc\u0001\u0000"+
		"\u0000\u0000\u08d0\u08d1\u0001\u0000\u0000\u0000\u08d1\u009d\u0001\u0000"+
		"\u0000\u0000\u08d2\u08d4\u0005\u0128\u0000\u0000\u08d3\u08d2\u0001\u0000"+
		"\u0000\u0000\u08d3\u08d4\u0001\u0000\u0000\u0000\u08d4\u08d5\u0001\u0000"+
		"\u0000\u0000\u08d5\u08d6\u0007\u0018\u0000\u0000\u08d6\u08eb\u0005\u00b4"+
		"\u0000\u0000\u08d7\u08d8\u0003\u00ceg\u0000\u08d8\u08d9\u0005\u00d5\u0000"+
		"\u0000\u08d9\u08eb\u0001\u0000\u0000\u0000\u08da\u08db\u0005\u0018\u0000"+
		"\u0000\u08db\u08dc\u0005\u0139\u0000\u0000\u08dc\u08dd\u0005\u00a8\u0000"+
		"\u0000\u08dd\u08de\u0005\u00a1\u0000\u0000\u08de\u08e7\u0005\u0139\u0000"+
		"\u0000\u08df\u08e5\u0005\u00a2\u0000\u0000\u08e0\u08e6\u0003\u0118\u008c"+
		"\u0000\u08e1\u08e2\u0003\u0112\u0089\u0000\u08e2\u08e3\u0005\u0002\u0000"+
		"\u0000\u08e3\u08e4\u0005\u0003\u0000\u0000\u08e4\u08e6\u0001\u0000\u0000"+
		"\u0000\u08e5\u08e0\u0001\u0000\u0000\u0000\u08e5\u08e1\u0001\u0000\u0000"+
		"\u0000\u08e6\u08e8\u0001\u0000\u0000\u0000\u08e7\u08df\u0001\u0000\u0000"+
		"\u0000\u08e7\u08e8\u0001\u0000\u0000\u0000\u08e8\u08eb\u0001\u0000\u0000"+
		"\u0000\u08e9\u08eb\u0003\u00ceg\u0000\u08ea\u08d3\u0001\u0000\u0000\u0000"+
		"\u08ea\u08d7\u0001\u0000\u0000\u0000\u08ea\u08da\u0001\u0000\u0000\u0000"+
		"\u08ea\u08e9\u0001\u0000\u0000\u0000\u08eb\u009f\u0001\u0000\u0000\u0000"+
		"\u08ec\u08ed\u0005\u0002\u0000\u0000\u08ed\u08ee\u0003\u00a2Q\u0000\u08ee"+
		"\u08ef\u0005\u0003\u0000\u0000\u08ef\u00a1\u0001\u0000\u0000\u0000\u08f0"+
		"\u08f5\u0003\u0114\u008a\u0000\u08f1\u08f2\u0005\u0004\u0000\u0000\u08f2"+
		"\u08f4\u0003\u0114\u008a\u0000\u08f3\u08f1\u0001\u0000\u0000\u0000\u08f4"+
		"\u08f7\u0001\u0000\u0000\u0000\u08f5\u08f3\u0001\u0000\u0000\u0000\u08f5"+
		"\u08f6\u0001\u0000\u0000\u0000\u08f6\u00a3\u0001\u0000\u0000\u0000\u08f7"+
		"\u08f5\u0001\u0000\u0000\u0000\u08f8\u08f9\u0005\u0002\u0000\u0000\u08f9"+
		"\u08fe\u0003\u00a6S\u0000\u08fa\u08fb\u0005\u0004\u0000\u0000\u08fb\u08fd"+
		"\u0003\u00a6S\u0000\u08fc\u08fa\u0001\u0000\u0000\u0000\u08fd\u0900\u0001"+
		"\u0000\u0000\u0000\u08fe\u08fc\u0001\u0000\u0000\u0000\u08fe\u08ff\u0001"+
		"\u0000\u0000\u0000\u08ff\u0901\u0001\u0000\u0000\u0000\u0900\u08fe\u0001"+
		"\u0000\u0000\u0000\u0901\u0902\u0005\u0003\u0000\u0000\u0902\u00a5\u0001"+
		"\u0000\u0000\u0000\u0903\u0905\u0003\u0114\u008a\u0000\u0904\u0906\u0007"+
		"\u0011\u0000\u0000\u0905\u0904\u0001\u0000\u0000\u0000\u0905\u0906\u0001"+
		"\u0000\u0000\u0000\u0906\u00a7\u0001\u0000\u0000\u0000\u0907\u0908\u0005"+
		"\u0002\u0000\u0000\u0908\u090d\u0003\u00aaU\u0000\u0909\u090a\u0005\u0004"+
		"\u0000\u0000\u090a\u090c\u0003\u00aaU\u0000\u090b\u0909\u0001\u0000\u0000"+
		"\u0000\u090c\u090f\u0001\u0000\u0000\u0000\u090d\u090b\u0001\u0000\u0000"+
		"\u0000\u090d\u090e\u0001\u0000\u0000\u0000\u090e\u0910\u0001\u0000\u0000"+
		"\u0000\u090f\u090d\u0001\u0000\u0000\u0000\u0910\u0911\u0005\u0003\u0000"+
		"\u0000\u0911\u00a9\u0001\u0000\u0000\u0000\u0912\u0914\u0003\u0118\u008c"+
		"\u0000\u0913\u0915\u0003 \u0010\u0000\u0914\u0913\u0001\u0000\u0000\u0000"+
		"\u0914\u0915\u0001\u0000\u0000\u0000\u0915\u00ab\u0001\u0000\u0000\u0000"+
		"\u0916\u0918\u0003\u00b8\\\u0000\u0917\u0919\u0003~?\u0000\u0918\u0917"+
		"\u0001\u0000\u0000\u0000\u0918\u0919\u0001\u0000\u0000\u0000\u0919\u091b"+
		"\u0001\u0000\u0000\u0000\u091a\u091c\u0003\u009cN\u0000\u091b\u091a\u0001"+
		"\u0000\u0000\u0000\u091b\u091c\u0001\u0000\u0000\u0000\u091c\u091d\u0001"+
		"\u0000\u0000\u0000\u091d\u091e\u0003\u00b2Y\u0000\u091e\u0932\u0001\u0000"+
		"\u0000\u0000\u091f\u0920\u0005\u0002\u0000\u0000\u0920\u0921\u0003\"\u0011"+
		"\u0000\u0921\u0923\u0005\u0003\u0000\u0000\u0922\u0924\u0003\u009cN\u0000"+
		"\u0923\u0922\u0001\u0000\u0000\u0000\u0923\u0924\u0001\u0000\u0000\u0000"+
		"\u0924\u0925\u0001\u0000\u0000\u0000\u0925\u0926\u0003\u00b2Y\u0000\u0926"+
		"\u0932\u0001\u0000\u0000\u0000\u0927\u0928\u0005\u0002\u0000\u0000\u0928"+
		"\u0929\u0003\u0094J\u0000\u0929\u092b\u0005\u0003\u0000\u0000\u092a\u092c"+
		"\u0003\u009cN\u0000\u092b\u092a\u0001\u0000\u0000\u0000\u092b\u092c\u0001"+
		"\u0000\u0000\u0000\u092c\u092d\u0001\u0000\u0000\u0000\u092d\u092e\u0003"+
		"\u00b2Y\u0000\u092e\u0932\u0001\u0000\u0000\u0000\u092f\u0932\u0003\u00ae"+
		"W\u0000\u0930\u0932\u0003\u00b0X\u0000\u0931\u0916\u0001\u0000\u0000\u0000"+
		"\u0931\u091f\u0001\u0000\u0000\u0000\u0931\u0927\u0001\u0000\u0000\u0000"+
		"\u0931\u092f\u0001\u0000\u0000\u0000\u0931\u0930\u0001\u0000\u0000\u0000"+
		"\u0932\u00ad\u0001\u0000\u0000\u0000\u0933\u0934\u0005\u0113\u0000\u0000"+
		"\u0934\u0939\u0003\u00ceg\u0000\u0935\u0936\u0005\u0004\u0000\u0000\u0936"+
		"\u0938\u0003\u00ceg\u0000\u0937\u0935\u0001\u0000\u0000\u0000\u0938\u093b"+
		"\u0001\u0000\u0000\u0000\u0939\u0937\u0001\u0000\u0000\u0000\u0939\u093a"+
		"\u0001\u0000\u0000\u0000\u093a\u093c\u0001\u0000\u0000\u0000\u093b\u0939"+
		"\u0001\u0000\u0000\u0000\u093c\u093d\u0003\u00b2Y\u0000\u093d\u00af\u0001"+
		"\u0000\u0000\u0000\u093e\u093f\u0003\u0110\u0088\u0000\u093f\u0948\u0005"+
		"\u0002\u0000\u0000\u0940\u0945\u0003\u00ceg\u0000\u0941\u0942\u0005\u0004"+
		"\u0000\u0000\u0942\u0944\u0003\u00ceg\u0000\u0943\u0941\u0001\u0000\u0000"+
		"\u0000\u0944\u0947\u0001\u0000\u0000\u0000\u0945\u0943\u0001\u0000\u0000"+
		"\u0000\u0945\u0946\u0001\u0000\u0000\u0000\u0946\u0949\u0001\u0000\u0000"+
		"\u0000\u0947\u0945\u0001\u0000\u0000\u0000\u0948\u0940\u0001\u0000\u0000"+
		"\u0000\u0948\u0949\u0001\u0000\u0000\u0000\u0949\u094a\u0001\u0000\u0000"+
		"\u0000\u094a\u094b\u0005\u0003\u0000\u0000\u094b\u094c\u0003\u00b2Y\u0000"+
		"\u094c\u00b1\u0001\u0000\u0000\u0000\u094d\u094f\u0005\u0012\u0000\u0000"+
		"\u094e\u094d\u0001\u0000\u0000\u0000\u094e\u094f\u0001\u0000\u0000\u0000"+
		"\u094f\u0950\u0001\u0000\u0000\u0000\u0950\u0952\u0003\u011a\u008d\u0000"+
		"\u0951\u0953\u0003\u00a0P\u0000\u0952\u0951\u0001\u0000\u0000\u0000\u0952"+
		"\u0953\u0001\u0000\u0000\u0000\u0953\u0955\u0001\u0000\u0000\u0000\u0954"+
		"\u094e\u0001\u0000\u0000\u0000\u0954\u0955\u0001\u0000\u0000\u0000\u0955"+
		"\u00b3\u0001\u0000\u0000\u0000\u0956\u0957\u0005\u00d4\u0000\u0000\u0957"+
		"\u0958\u0005d\u0000\u0000\u0958\u0959\u0005\u00dc\u0000\u0000\u0959\u095d"+
		"\u0005\u0135\u0000\u0000\u095a\u095b\u0005\u011b\u0000\u0000\u095b\u095c"+
		"\u0005\u00dd\u0000\u0000\u095c\u095e\u0003<\u001e\u0000\u095d\u095a\u0001"+
		"\u0000\u0000\u0000\u095d\u095e\u0001\u0000\u0000\u0000\u095e\u0988\u0001"+
		"\u0000\u0000\u0000\u095f\u0960\u0005\u00d4\u0000\u0000\u0960\u0961\u0005"+
		"d\u0000\u0000\u0961\u096b\u0005E\u0000\u0000\u0962\u0963\u0005]\u0000"+
		"\u0000\u0963\u0964\u0005\u00f6\u0000\u0000\u0964\u0965\u0005\u001a\u0000"+
		"\u0000\u0965\u0969\u0005\u0135\u0000\u0000\u0966\u0967\u0005R\u0000\u0000"+
		"\u0967\u0968\u0005\u001a\u0000\u0000\u0968\u096a\u0005\u0135\u0000\u0000"+
		"\u0969\u0966\u0001\u0000\u0000\u0000\u0969\u096a\u0001\u0000\u0000\u0000"+
		"\u096a\u096c\u0001\u0000\u0000\u0000\u096b\u0962\u0001\u0000\u0000\u0000"+
		"\u096b\u096c\u0001\u0000\u0000\u0000\u096c\u0972\u0001\u0000\u0000\u0000"+
		"\u096d\u096e\u0005(\u0000\u0000\u096e\u096f\u0005~\u0000\u0000\u096f\u0970"+
		"\u0005\u00f6\u0000\u0000\u0970\u0971\u0005\u001a\u0000\u0000\u0971\u0973"+
		"\u0005\u0135\u0000\u0000\u0972\u096d\u0001\u0000\u0000\u0000\u0972\u0973"+
		"\u0001\u0000\u0000\u0000\u0973\u0979\u0001\u0000\u0000\u0000\u0974\u0975"+
		"\u0005\u0092\u0000\u0000\u0975\u0976\u0005\u0080\u0000\u0000\u0976\u0977"+
		"\u0005\u00f6\u0000\u0000\u0977\u0978\u0005\u001a\u0000\u0000\u0978\u097a"+
		"\u0005\u0135\u0000\u0000\u0979\u0974\u0001\u0000\u0000\u0000\u0979\u097a"+
		"\u0001\u0000\u0000\u0000\u097a\u097f\u0001\u0000\u0000\u0000\u097b\u097c"+
		"\u0005\u0089\u0000\u0000\u097c\u097d\u0005\u00f6\u0000\u0000\u097d\u097e"+
		"\u0005\u001a\u0000\u0000\u097e\u0980\u0005\u0135\u0000\u0000\u097f\u097b"+
		"\u0001\u0000\u0000\u0000\u097f\u0980\u0001\u0000\u0000\u0000\u0980\u0985"+
		"\u0001\u0000\u0000\u0000\u0981\u0982\u0005\u009f\u0000\u0000\u0982\u0983"+
		"\u0005C\u0000\u0000\u0983\u0984\u0005\u0012\u0000\u0000\u0984\u0986\u0005"+
		"\u0135\u0000\u0000\u0985\u0981\u0001\u0000\u0000\u0000\u0985\u0986\u0001"+
		"\u0000\u0000\u0000\u0986\u0988\u0001\u0000\u0000\u0000\u0987\u0956\u0001"+
		"\u0000\u0000\u0000\u0987\u095f\u0001\u0000\u0000\u0000\u0988\u00b5\u0001"+
		"\u0000\u0000\u0000\u0989\u098e\u0003\u00b8\\\u0000\u098a\u098b\u0005\u0004"+
		"\u0000\u0000\u098b\u098d\u0003\u00b8\\\u0000\u098c\u098a\u0001\u0000\u0000"+
		"\u0000\u098d\u0990\u0001\u0000\u0000\u0000\u098e\u098c\u0001\u0000\u0000"+
		"\u0000\u098e\u098f\u0001\u0000\u0000\u0000\u098f\u00b7\u0001\u0000\u0000"+
		"\u0000\u0990\u098e\u0001\u0000\u0000\u0000\u0991\u0996\u0003\u0114\u008a"+
		"\u0000\u0992\u0993\u0005\u0005\u0000\u0000\u0993\u0995\u0003\u0114\u008a"+
		"\u0000\u0994\u0992\u0001\u0000\u0000\u0000\u0995\u0998\u0001\u0000\u0000"+
		"\u0000\u0996\u0994\u0001\u0000\u0000\u0000\u0996\u0997\u0001\u0000\u0000"+
		"\u0000\u0997\u00b9\u0001\u0000\u0000\u0000\u0998\u0996\u0001\u0000\u0000"+
		"\u0000\u0999\u099e\u0003\u00bc^\u0000\u099a\u099b\u0005\u0004\u0000\u0000"+
		"\u099b\u099d\u0003\u00bc^\u0000\u099c\u099a\u0001\u0000\u0000\u0000\u099d"+
		"\u09a0\u0001\u0000\u0000\u0000\u099e\u099c\u0001\u0000\u0000\u0000\u099e"+
		"\u099f\u0001\u0000\u0000\u0000\u099f\u00bb\u0001\u0000\u0000\u0000\u09a0"+
		"\u099e\u0001\u0000\u0000\u0000\u09a1\u09a4\u0003\u00b8\\\u0000\u09a2\u09a3"+
		"\u0005\u00a5\u0000\u0000\u09a3\u09a5\u0003<\u001e\u0000\u09a4\u09a2\u0001"+
		"\u0000\u0000\u0000\u09a4\u09a5\u0001\u0000\u0000\u0000\u09a5\u00bd\u0001"+
		"\u0000\u0000\u0000\u09a6\u09a7\u0003\u0114\u008a\u0000\u09a7\u09a8\u0005"+
		"\u0005\u0000\u0000\u09a8\u09aa\u0001\u0000\u0000\u0000\u09a9\u09a6\u0001"+
		"\u0000\u0000\u0000\u09a9\u09aa\u0001\u0000\u0000\u0000\u09aa\u09ab\u0001"+
		"\u0000\u0000\u0000\u09ab\u09ac\u0003\u0114\u008a\u0000\u09ac\u00bf\u0001"+
		"\u0000\u0000\u0000\u09ad\u09ae\u0003\u0114\u008a\u0000\u09ae\u09af\u0005"+
		"\u0005\u0000\u0000\u09af\u09b1\u0001\u0000\u0000\u0000\u09b0\u09ad\u0001"+
		"\u0000\u0000\u0000\u09b0\u09b1\u0001\u0000\u0000\u0000\u09b1\u09b2\u0001"+
		"\u0000\u0000\u0000\u09b2\u09b3\u0003\u0114\u008a\u0000\u09b3\u00c1\u0001"+
		"\u0000\u0000\u0000\u09b4\u09bc\u0003\u00ceg\u0000\u09b5\u09b7\u0005\u0012"+
		"\u0000\u0000\u09b6\u09b5\u0001\u0000\u0000\u0000\u09b6\u09b7\u0001\u0000"+
		"\u0000\u0000\u09b7\u09ba\u0001\u0000\u0000\u0000\u09b8\u09bb\u0003\u0114"+
		"\u008a\u0000\u09b9\u09bb\u0003\u00a0P\u0000\u09ba\u09b8\u0001\u0000\u0000"+
		"\u0000\u09ba\u09b9\u0001\u0000\u0000\u0000\u09bb\u09bd\u0001\u0000\u0000"+
		"\u0000\u09bc\u09b6\u0001\u0000\u0000\u0000\u09bc\u09bd\u0001\u0000\u0000"+
		"\u0000\u09bd\u00c3\u0001\u0000\u0000\u0000\u09be\u09c3\u0003\u00c2a\u0000"+
		"\u09bf\u09c0\u0005\u0004\u0000\u0000\u09c0\u09c2\u0003\u00c2a\u0000\u09c1"+
		"\u09bf\u0001\u0000\u0000\u0000\u09c2\u09c5\u0001\u0000\u0000\u0000\u09c3"+
		"\u09c1\u0001\u0000\u0000\u0000\u09c3\u09c4\u0001\u0000\u0000\u0000\u09c4"+
		"\u00c5\u0001\u0000\u0000\u0000\u09c5\u09c3\u0001\u0000\u0000\u0000\u09c6"+
		"\u09c7\u0005\u0002\u0000\u0000\u09c7\u09cc\u0003\u00c8d\u0000\u09c8\u09c9"+
		"\u0005\u0004\u0000\u0000\u09c9\u09cb\u0003\u00c8d\u0000\u09ca\u09c8\u0001"+
		"\u0000\u0000\u0000\u09cb\u09ce\u0001\u0000\u0000\u0000\u09cc\u09ca\u0001"+
		"\u0000\u0000\u0000\u09cc\u09cd\u0001\u0000\u0000\u0000\u09cd\u09cf\u0001"+
		"\u0000\u0000\u0000\u09ce\u09cc\u0001\u0000\u0000\u0000\u09cf\u09d0\u0005"+
		"\u0003\u0000\u0000\u09d0\u00c7\u0001\u0000\u0000\u0000\u09d1\u09d4\u0003"+
		"\u00cae\u0000\u09d2\u09d4\u0003\u00fc~\u0000\u09d3\u09d1\u0001\u0000\u0000"+
		"\u0000\u09d3\u09d2\u0001\u0000\u0000\u0000\u09d4\u00c9\u0001\u0000\u0000"+
		"\u0000\u09d5\u09e3\u0003\u0112\u0089\u0000\u09d6\u09d7\u0003\u0118\u008c"+
		"\u0000\u09d7\u09d8\u0005\u0002\u0000\u0000\u09d8\u09dd\u0003\u00ccf\u0000"+
		"\u09d9\u09da\u0005\u0004\u0000\u0000\u09da\u09dc\u0003\u00ccf\u0000\u09db"+
		"\u09d9\u0001\u0000\u0000\u0000\u09dc\u09df\u0001\u0000\u0000\u0000\u09dd"+
		"\u09db\u0001\u0000\u0000\u0000\u09dd\u09de\u0001\u0000\u0000\u0000\u09de"+
		"\u09e0\u0001\u0000\u0000\u0000\u09df\u09dd\u0001\u0000\u0000\u0000\u09e0"+
		"\u09e1\u0005\u0003\u0000\u0000\u09e1\u09e3\u0001\u0000\u0000\u0000\u09e2"+
		"\u09d5\u0001\u0000\u0000\u0000\u09e2\u09d6\u0001\u0000\u0000\u0000\u09e3"+
		"\u00cb\u0001\u0000\u0000\u0000\u09e4\u09e7\u0003\u0112\u0089\u0000\u09e5"+
		"\u09e7\u0003\u00dcn\u0000\u09e6\u09e4\u0001\u0000\u0000\u0000\u09e6\u09e5"+
		"\u0001\u0000\u0000\u0000\u09e7\u00cd\u0001\u0000\u0000\u0000\u09e8\u09e9"+
		"\u0003\u00d2i\u0000\u09e9\u00cf\u0001\u0000\u0000\u0000\u09ea\u09ef\u0003"+
		"\u00ceg\u0000\u09eb\u09ec\u0005\u0004\u0000\u0000\u09ec\u09ee\u0003\u00ce"+
		"g\u0000\u09ed\u09eb\u0001\u0000\u0000\u0000\u09ee\u09f1\u0001\u0000\u0000"+
		"\u0000\u09ef\u09ed\u0001\u0000\u0000\u0000\u09ef\u09f0\u0001\u0000\u0000"+
		"\u0000\u09f0\u00d1\u0001\u0000\u0000\u0000\u09f1\u09ef\u0001\u0000\u0000"+
		"\u0000\u09f2\u09f3\u0006i\uffff\uffff\u0000\u09f3\u09f4\u0005\u009e\u0000"+
		"\u0000\u09f4\u09ff\u0003\u00d2i\u0005\u09f5\u09f6\u0005U\u0000\u0000\u09f6"+
		"\u09f7\u0005\u0002\u0000\u0000\u09f7\u09f8\u0003\"\u0011\u0000\u09f8\u09f9"+
		"\u0005\u0003\u0000\u0000\u09f9\u09ff\u0001\u0000\u0000\u0000\u09fa\u09fc"+
		"\u0003\u00d6k\u0000\u09fb\u09fd\u0003\u00d4j\u0000\u09fc\u09fb\u0001\u0000"+
		"\u0000\u0000\u09fc\u09fd\u0001\u0000\u0000\u0000\u09fd\u09ff\u0001\u0000"+
		"\u0000\u0000\u09fe\u09f2\u0001\u0000\u0000\u0000\u09fe\u09f5\u0001\u0000"+
		"\u0000\u0000\u09fe\u09fa\u0001\u0000\u0000\u0000\u09ff\u0a08\u0001\u0000"+
		"\u0000\u0000\u0a00\u0a01\n\u0002\u0000\u0000\u0a01\u0a02\u0005\r\u0000"+
		"\u0000\u0a02\u0a07\u0003\u00d2i\u0003\u0a03\u0a04\n\u0001\u0000\u0000"+
		"\u0a04\u0a05\u0005\u00a6\u0000\u0000\u0a05\u0a07\u0003\u00d2i\u0002\u0a06"+
		"\u0a00\u0001\u0000\u0000\u0000\u0a06\u0a03\u0001\u0000\u0000\u0000\u0a07"+
		"\u0a0a\u0001\u0000\u0000\u0000\u0a08\u0a06\u0001\u0000\u0000\u0000\u0a08"+
		"\u0a09\u0001\u0000\u0000\u0000\u0a09\u00d3\u0001\u0000\u0000\u0000\u0a0a"+
		"\u0a08\u0001\u0000\u0000\u0000\u0a0b\u0a0d\u0005\u009e\u0000\u0000\u0a0c"+
		"\u0a0b\u0001\u0000\u0000\u0000\u0a0c\u0a0d\u0001\u0000\u0000\u0000\u0a0d"+
		"\u0a0e\u0001\u0000\u0000\u0000\u0a0e\u0a0f\u0005\u0016\u0000\u0000\u0a0f"+
		"\u0a10\u0003\u00d6k\u0000\u0a10\u0a11\u0005\r\u0000\u0000\u0a11\u0a12"+
		"\u0003\u00d6k\u0000\u0a12\u0a5e\u0001\u0000\u0000\u0000\u0a13\u0a15\u0005"+
		"\u009e\u0000\u0000\u0a14\u0a13\u0001\u0000\u0000\u0000\u0a14\u0a15\u0001"+
		"\u0000\u0000\u0000\u0a15\u0a16\u0001\u0000\u0000\u0000\u0a16\u0a17\u0005"+
		"s\u0000\u0000\u0a17\u0a18\u0005\u0002\u0000\u0000\u0a18\u0a1d\u0003\u00ce"+
		"g\u0000\u0a19\u0a1a\u0005\u0004\u0000\u0000\u0a1a\u0a1c\u0003\u00ceg\u0000"+
		"\u0a1b\u0a19\u0001\u0000\u0000\u0000\u0a1c\u0a1f\u0001\u0000\u0000\u0000"+
		"\u0a1d\u0a1b\u0001\u0000\u0000\u0000\u0a1d\u0a1e\u0001\u0000\u0000\u0000"+
		"\u0a1e\u0a20\u0001\u0000\u0000\u0000\u0a1f\u0a1d\u0001\u0000\u0000\u0000"+
		"\u0a20\u0a21\u0005\u0003\u0000\u0000\u0a21\u0a5e\u0001\u0000\u0000\u0000"+
		"\u0a22\u0a24\u0005\u009e\u0000\u0000\u0a23\u0a22\u0001\u0000\u0000\u0000"+
		"\u0a23\u0a24\u0001\u0000\u0000\u0000\u0a24\u0a25\u0001\u0000\u0000\u0000"+
		"\u0a25\u0a26\u0005s\u0000\u0000\u0a26\u0a27\u0005\u0002\u0000\u0000\u0a27"+
		"\u0a28\u0003\"\u0011\u0000\u0a28\u0a29\u0005\u0003\u0000\u0000\u0a29\u0a5e"+
		"\u0001\u0000\u0000\u0000\u0a2a\u0a2c\u0005\u009e\u0000\u0000\u0a2b\u0a2a"+
		"\u0001\u0000\u0000\u0000\u0a2b\u0a2c\u0001\u0000\u0000\u0000\u0a2c\u0a2d"+
		"\u0001\u0000\u0000\u0000\u0a2d\u0a2e\u0005\u00cf\u0000\u0000\u0a2e\u0a5e"+
		"\u0003\u00d6k\u0000\u0a2f\u0a31\u0005\u009e\u0000\u0000\u0a30\u0a2f\u0001"+
		"\u0000\u0000\u0000\u0a30\u0a31\u0001\u0000\u0000\u0000\u0a31\u0a32\u0001"+
		"\u0000\u0000\u0000\u0a32\u0a33\u0007\u0019\u0000\u0000\u0a33\u0a41\u0007"+
		"\u001a\u0000\u0000\u0a34\u0a35\u0005\u0002\u0000\u0000\u0a35\u0a42\u0005"+
		"\u0003\u0000\u0000\u0a36\u0a37\u0005\u0002\u0000\u0000\u0a37\u0a3c\u0003"+
		"\u00ceg\u0000\u0a38\u0a39\u0005\u0004\u0000\u0000\u0a39\u0a3b\u0003\u00ce"+
		"g\u0000\u0a3a\u0a38\u0001\u0000\u0000\u0000\u0a3b\u0a3e\u0001\u0000\u0000"+
		"\u0000\u0a3c\u0a3a\u0001\u0000\u0000\u0000\u0a3c\u0a3d\u0001\u0000\u0000"+
		"\u0000\u0a3d\u0a3f\u0001\u0000\u0000\u0000\u0a3e\u0a3c\u0001\u0000\u0000"+
		"\u0000\u0a3f\u0a40\u0005\u0003\u0000\u0000\u0a40\u0a42\u0001\u0000\u0000"+
		"\u0000\u0a41\u0a34\u0001\u0000\u0000\u0000\u0a41\u0a36\u0001\u0000\u0000"+
		"\u0000\u0a42\u0a5e\u0001\u0000\u0000\u0000\u0a43\u0a45\u0005\u009e\u0000"+
		"\u0000\u0a44\u0a43\u0001\u0000\u0000\u0000\u0a44\u0a45\u0001\u0000\u0000"+
		"\u0000\u0a45\u0a46\u0001\u0000\u0000\u0000\u0a46\u0a47\u0007\u0019\u0000"+
		"\u0000\u0a47\u0a4a\u0003\u00d6k\u0000\u0a48\u0a49\u0005Q\u0000\u0000\u0a49"+
		"\u0a4b\u0005\u0135\u0000\u0000\u0a4a\u0a48\u0001\u0000\u0000\u0000\u0a4a"+
		"\u0a4b\u0001\u0000\u0000\u0000\u0a4b\u0a5e\u0001\u0000\u0000\u0000\u0a4c"+
		"\u0a4e\u0005}\u0000\u0000\u0a4d\u0a4f\u0005\u009e\u0000\u0000\u0a4e\u0a4d"+
		"\u0001\u0000\u0000\u0000\u0a4e\u0a4f\u0001\u0000\u0000\u0000\u0a4f\u0a50"+
		"\u0001\u0000\u0000\u0000\u0a50\u0a5e\u0005\u009f\u0000\u0000\u0a51\u0a53"+
		"\u0005}\u0000\u0000\u0a52\u0a54\u0005\u009e\u0000\u0000\u0a53\u0a52\u0001"+
		"\u0000\u0000\u0000\u0a53\u0a54\u0001\u0000\u0000\u0000\u0a54\u0a55\u0001"+
		"\u0000\u0000\u0000\u0a55\u0a5e\u0007\u001b\u0000\u0000\u0a56\u0a58\u0005"+
		"}\u0000\u0000\u0a57\u0a59\u0005\u009e\u0000\u0000\u0a58\u0a57\u0001\u0000"+
		"\u0000\u0000\u0a58\u0a59\u0001\u0000\u0000\u0000\u0a59\u0a5a\u0001\u0000"+
		"\u0000\u0000\u0a5a\u0a5b\u0005K\u0000\u0000\u0a5b\u0a5c\u0005f\u0000\u0000"+
		"\u0a5c\u0a5e\u0003\u00d6k\u0000\u0a5d\u0a0c\u0001\u0000\u0000\u0000\u0a5d"+
		"\u0a14\u0001\u0000\u0000\u0000\u0a5d\u0a23\u0001\u0000\u0000\u0000\u0a5d"+
		"\u0a2b\u0001\u0000\u0000\u0000\u0a5d\u0a30\u0001\u0000\u0000\u0000\u0a5d"+
		"\u0a44\u0001\u0000\u0000\u0000\u0a5d\u0a4c\u0001\u0000\u0000\u0000\u0a5d"+
		"\u0a51\u0001\u0000\u0000\u0000\u0a5d\u0a56\u0001\u0000\u0000\u0000\u0a5e"+
		"\u00d5\u0001\u0000\u0000\u0000\u0a5f\u0a60\u0006k\uffff\uffff\u0000\u0a60"+
		"\u0a64\u0003\u00dam\u0000\u0a61\u0a62\u0007\u001c\u0000\u0000\u0a62\u0a64"+
		"\u0003\u00d6k\u0007\u0a63\u0a5f\u0001\u0000\u0000\u0000\u0a63\u0a61\u0001"+
		"\u0000\u0000\u0000\u0a64\u0a7a\u0001\u0000\u0000\u0000\u0a65\u0a66\n\u0006"+
		"\u0000\u0000\u0a66\u0a67\u0007\u001d\u0000\u0000\u0a67\u0a79\u0003\u00d6"+
		"k\u0007\u0a68\u0a69\n\u0005\u0000\u0000\u0a69\u0a6a\u0007\u001e\u0000"+
		"\u0000\u0a6a\u0a79\u0003\u00d6k\u0006\u0a6b\u0a6c\n\u0004\u0000\u0000"+
		"\u0a6c\u0a6d\u0005\u012d\u0000\u0000\u0a6d\u0a79\u0003\u00d6k\u0005\u0a6e"+
		"\u0a6f\n\u0003\u0000\u0000\u0a6f\u0a70\u0005\u0130\u0000\u0000\u0a70\u0a79"+
		"\u0003\u00d6k\u0004\u0a71\u0a72\n\u0002\u0000\u0000\u0a72\u0a73\u0005"+
		"\u012e\u0000\u0000\u0a73\u0a79\u0003\u00d6k\u0003\u0a74\u0a75\n\u0001"+
		"\u0000\u0000\u0a75\u0a76\u0003\u00deo\u0000\u0a76\u0a77\u0003\u00d6k\u0002"+
		"\u0a77\u0a79\u0001\u0000\u0000\u0000\u0a78\u0a65\u0001\u0000\u0000\u0000"+
		"\u0a78\u0a68\u0001\u0000\u0000\u0000\u0a78\u0a6b\u0001\u0000\u0000\u0000"+
		"\u0a78\u0a6e\u0001\u0000\u0000\u0000\u0a78\u0a71\u0001\u0000\u0000\u0000"+
		"\u0a78\u0a74\u0001\u0000\u0000\u0000\u0a79\u0a7c\u0001\u0000\u0000\u0000"+
		"\u0a7a\u0a78\u0001\u0000\u0000\u0000\u0a7a\u0a7b\u0001\u0000\u0000\u0000"+
		"\u0a7b\u00d7\u0001\u0000\u0000\u0000\u0a7c\u0a7a\u0001\u0000\u0000\u0000"+
		"\u0a7d\u0a7e\u0007\u001f\u0000\u0000\u0a7e\u00d9\u0001\u0000\u0000\u0000"+
		"\u0a7f\u0a80\u0006m\uffff\uffff\u0000\u0a80\u0b5d\u0007 \u0000\u0000\u0a81"+
		"\u0a82\u0007!\u0000\u0000\u0a82\u0a83\u0005\u0002\u0000\u0000\u0a83\u0a84"+
		"\u0003\u00d8l\u0000\u0a84\u0a85\u0005\u0004\u0000\u0000\u0a85\u0a86\u0003"+
		"\u00d6k\u0000\u0a86\u0a87\u0005\u0004\u0000\u0000\u0a87\u0a88\u0003\u00d6"+
		"k\u0000\u0a88\u0a89\u0005\u0003\u0000\u0000\u0a89\u0b5d\u0001\u0000\u0000"+
		"\u0000\u0a8a\u0a8b\u0007\"\u0000\u0000\u0a8b\u0a8c\u0005\u0002\u0000\u0000"+
		"\u0a8c\u0a8d\u0003\u00d8l\u0000\u0a8d\u0a8e\u0005\u0004\u0000\u0000\u0a8e"+
		"\u0a8f\u0003\u00d6k\u0000\u0a8f\u0a90\u0005\u0004\u0000\u0000\u0a90\u0a91"+
		"\u0003\u00d6k\u0000\u0a91\u0a92\u0005\u0003\u0000\u0000\u0a92\u0b5d\u0001"+
		"\u0000\u0000\u0000\u0a93\u0a95\u0005\u001d\u0000\u0000\u0a94\u0a96\u0003"+
		"\u0102\u0081\u0000\u0a95\u0a94\u0001\u0000\u0000\u0000\u0a96\u0a97\u0001"+
		"\u0000\u0000\u0000\u0a97\u0a95\u0001\u0000\u0000\u0000\u0a97\u0a98\u0001"+
		"\u0000\u0000\u0000\u0a98\u0a9b\u0001\u0000\u0000\u0000\u0a99\u0a9a\u0005"+
		"O\u0000\u0000\u0a9a\u0a9c\u0003\u00ceg\u0000\u0a9b\u0a99\u0001\u0000\u0000"+
		"\u0000\u0a9b\u0a9c\u0001\u0000\u0000\u0000\u0a9c\u0a9d\u0001\u0000\u0000"+
		"\u0000\u0a9d\u0a9e\u0005P\u0000\u0000\u0a9e\u0b5d\u0001\u0000\u0000\u0000"+
		"\u0a9f\u0aa0\u0005\u001d\u0000\u0000\u0aa0\u0aa2\u0003\u00ceg\u0000\u0aa1"+
		"\u0aa3\u0003\u0102\u0081\u0000\u0aa2\u0aa1\u0001\u0000\u0000\u0000\u0aa3"+
		"\u0aa4\u0001\u0000\u0000\u0000\u0aa4\u0aa2\u0001\u0000\u0000\u0000\u0aa4"+
		"\u0aa5\u0001\u0000\u0000\u0000\u0aa5\u0aa8\u0001\u0000\u0000\u0000\u0aa6"+
		"\u0aa7\u0005O\u0000\u0000\u0aa7\u0aa9\u0003\u00ceg\u0000\u0aa8\u0aa6\u0001"+
		"\u0000\u0000\u0000\u0aa8\u0aa9\u0001\u0000\u0000\u0000\u0aa9\u0aaa\u0001"+
		"\u0000\u0000\u0000\u0aaa\u0aab\u0005P\u0000\u0000\u0aab\u0b5d\u0001\u0000"+
		"\u0000\u0000\u0aac\u0aad\u0007#\u0000\u0000\u0aad\u0aae\u0005\u0002\u0000"+
		"\u0000\u0aae\u0aaf\u0003\u00ceg\u0000\u0aaf\u0ab0\u0005\u0012\u0000\u0000"+
		"\u0ab0\u0ab1\u0003\u00f4z\u0000\u0ab1\u0ab2\u0005\u0003\u0000\u0000\u0ab2"+
		"\u0b5d\u0001\u0000\u0000\u0000\u0ab3\u0ab4\u0005\u00eb\u0000\u0000\u0ab4"+
		"\u0abd\u0005\u0002\u0000\u0000\u0ab5\u0aba\u0003\u00c2a\u0000\u0ab6\u0ab7"+
		"\u0005\u0004\u0000\u0000\u0ab7\u0ab9\u0003\u00c2a\u0000\u0ab8\u0ab6\u0001"+
		"\u0000\u0000\u0000\u0ab9\u0abc\u0001\u0000\u0000\u0000\u0aba\u0ab8\u0001"+
		"\u0000\u0000\u0000\u0aba\u0abb\u0001\u0000\u0000\u0000\u0abb\u0abe\u0001"+
		"\u0000\u0000\u0000\u0abc\u0aba\u0001\u0000\u0000\u0000\u0abd\u0ab5\u0001"+
		"\u0000\u0000\u0000\u0abd\u0abe\u0001\u0000\u0000\u0000\u0abe\u0abf\u0001"+
		"\u0000\u0000\u0000\u0abf\u0b5d\u0005\u0003\u0000\u0000\u0ac0\u0ac1\u0005"+
		"`\u0000\u0000\u0ac1\u0ac2\u0005\u0002\u0000\u0000\u0ac2\u0ac5\u0003\u00ce"+
		"g\u0000\u0ac3\u0ac4\u0005q\u0000\u0000\u0ac4\u0ac6\u0005\u00a0\u0000\u0000"+
		"\u0ac5\u0ac3\u0001\u0000\u0000\u0000\u0ac5\u0ac6\u0001\u0000\u0000\u0000"+
		"\u0ac6\u0ac7\u0001\u0000\u0000\u0000\u0ac7\u0ac8\u0005\u0003\u0000\u0000"+
		"\u0ac8\u0b5d\u0001\u0000\u0000\u0000\u0ac9\u0aca\u0005\u0081\u0000\u0000"+
		"\u0aca\u0acb\u0005\u0002\u0000\u0000\u0acb\u0ace\u0003\u00ceg\u0000\u0acc"+
		"\u0acd\u0005q\u0000\u0000\u0acd\u0acf\u0005\u00a0\u0000\u0000\u0ace\u0acc"+
		"\u0001\u0000\u0000\u0000\u0ace\u0acf\u0001\u0000\u0000\u0000\u0acf\u0ad0"+
		"\u0001\u0000\u0000\u0000\u0ad0\u0ad1\u0005\u0003\u0000\u0000\u0ad1\u0b5d"+
		"\u0001\u0000\u0000\u0000\u0ad2\u0ad3\u0005\u00b7\u0000\u0000\u0ad3\u0ad4"+
		"\u0005\u0002\u0000\u0000\u0ad4\u0ad5\u0003\u00d6k\u0000\u0ad5\u0ad6\u0005"+
		"s\u0000\u0000\u0ad6\u0ad7\u0003\u00d6k\u0000\u0ad7\u0ad8\u0005\u0003\u0000"+
		"\u0000\u0ad8\u0b5d\u0001\u0000\u0000\u0000\u0ad9\u0b5d\u0003\u00dcn\u0000"+
		"\u0ada\u0b5d\u0005\u0129\u0000\u0000\u0adb\u0adc\u0003\u0112\u0089\u0000"+
		"\u0adc\u0add\u0005\u0005\u0000\u0000\u0add\u0ade\u0005\u0129\u0000\u0000"+
		"\u0ade\u0b5d\u0001\u0000\u0000\u0000\u0adf\u0ae0\u0005\u0002\u0000\u0000"+
		"\u0ae0\u0ae3\u0003\u00c2a\u0000\u0ae1\u0ae2\u0005\u0004\u0000\u0000\u0ae2"+
		"\u0ae4\u0003\u00c2a\u0000\u0ae3\u0ae1\u0001\u0000\u0000\u0000\u0ae4\u0ae5"+
		"\u0001\u0000\u0000\u0000\u0ae5\u0ae3\u0001\u0000\u0000\u0000\u0ae5\u0ae6"+
		"\u0001\u0000\u0000\u0000\u0ae6\u0ae7\u0001\u0000\u0000\u0000\u0ae7\u0ae8"+
		"\u0005\u0003\u0000\u0000\u0ae8\u0b5d\u0001\u0000\u0000\u0000\u0ae9\u0aea"+
		"\u0005\u0002\u0000\u0000\u0aea\u0aeb\u0003\"\u0011\u0000\u0aeb\u0aec\u0005"+
		"\u0003\u0000\u0000\u0aec\u0b5d\u0001\u0000\u0000\u0000\u0aed\u0aee\u0003"+
		"\u0110\u0088\u0000\u0aee\u0afa\u0005\u0002\u0000\u0000\u0aef\u0af1\u0003"+
		"\u0092I\u0000\u0af0\u0aef\u0001\u0000\u0000\u0000\u0af0\u0af1\u0001\u0000"+
		"\u0000\u0000\u0af1\u0af2\u0001\u0000\u0000\u0000\u0af2\u0af7\u0003\u00ce"+
		"g\u0000\u0af3\u0af4\u0005\u0004\u0000\u0000\u0af4\u0af6\u0003\u00ceg\u0000"+
		"\u0af5\u0af3\u0001\u0000\u0000\u0000\u0af6\u0af9\u0001\u0000\u0000\u0000"+
		"\u0af7\u0af5\u0001\u0000\u0000\u0000\u0af7\u0af8\u0001\u0000\u0000\u0000"+
		"\u0af8\u0afb\u0001\u0000\u0000\u0000\u0af9\u0af7\u0001\u0000\u0000\u0000"+
		"\u0afa\u0af0\u0001\u0000\u0000\u0000\u0afa\u0afb\u0001\u0000\u0000\u0000"+
		"\u0afb\u0afc\u0001\u0000\u0000\u0000\u0afc\u0b03\u0005\u0003\u0000\u0000"+
		"\u0afd\u0afe\u0005^\u0000\u0000\u0afe\u0aff\u0005\u0002\u0000\u0000\u0aff"+
		"\u0b00\u0005\u0119\u0000\u0000\u0b00\u0b01\u0003\u00d2i\u0000\u0b01\u0b02"+
		"\u0005\u0003\u0000\u0000\u0b02\u0b04\u0001\u0000\u0000\u0000\u0b03\u0afd"+
		"\u0001\u0000\u0000\u0000\u0b03\u0b04\u0001\u0000\u0000\u0000\u0b04\u0b07"+
		"\u0001\u0000\u0000\u0000\u0b05\u0b06\u0007$\u0000\u0000\u0b06\u0b08\u0005"+
		"\u00a0\u0000\u0000\u0b07\u0b05\u0001\u0000\u0000\u0000\u0b07\u0b08\u0001"+
		"\u0000\u0000\u0000\u0b08\u0b0b\u0001\u0000\u0000\u0000\u0b09\u0b0a\u0005"+
		"\u00ab\u0000\u0000\u0b0a\u0b0c\u0003\u0108\u0084\u0000\u0b0b\u0b09\u0001"+
		"\u0000\u0000\u0000\u0b0b\u0b0c\u0001\u0000\u0000\u0000\u0b0c\u0b5d\u0001"+
		"\u0000\u0000\u0000\u0b0d\u0b0e\u0003\u0118\u008c\u0000\u0b0e\u0b0f\u0005"+
		"\u0132\u0000\u0000\u0b0f\u0b10\u0003\u00ceg\u0000\u0b10\u0b5d\u0001\u0000"+
		"\u0000\u0000\u0b11\u0b12\u0005\u0002\u0000\u0000\u0b12\u0b15\u0003\u0118"+
		"\u008c\u0000\u0b13\u0b14\u0005\u0004\u0000\u0000\u0b14\u0b16\u0003\u0118"+
		"\u008c\u0000\u0b15\u0b13\u0001\u0000\u0000\u0000\u0b16\u0b17\u0001\u0000"+
		"\u0000\u0000\u0b17\u0b15\u0001\u0000\u0000\u0000\u0b17\u0b18\u0001\u0000"+
		"\u0000\u0000\u0b18\u0b19\u0001\u0000\u0000\u0000\u0b19\u0b1a\u0005\u0003"+
		"\u0000\u0000\u0b1a\u0b1b\u0005\u0132\u0000\u0000\u0b1b\u0b1c\u0003\u00ce"+
		"g\u0000\u0b1c\u0b5d\u0001\u0000\u0000\u0000\u0b1d\u0b5d\u0003\u0118\u008c"+
		"\u0000\u0b1e\u0b1f\u0005\u0002\u0000\u0000\u0b1f\u0b20\u0003\u00ceg\u0000"+
		"\u0b20\u0b21\u0005\u0003\u0000\u0000\u0b21\u0b5d\u0001\u0000\u0000\u0000"+
		"\u0b22\u0b23\u0005Z\u0000\u0000\u0b23\u0b24\u0005\u0002\u0000\u0000\u0b24"+
		"\u0b25\u0003\u0118\u008c\u0000\u0b25\u0b26\u0005f\u0000\u0000\u0b26\u0b27"+
		"\u0003\u00d6k\u0000\u0b27\u0b28\u0005\u0003\u0000\u0000\u0b28\u0b5d\u0001"+
		"\u0000\u0000\u0000\u0b29\u0b2a\u0007%\u0000\u0000\u0b2a\u0b2b\u0005\u0002"+
		"\u0000\u0000\u0b2b\u0b2c\u0003\u00d6k\u0000\u0b2c\u0b2d\u0007&\u0000\u0000"+
		"\u0b2d\u0b30\u0003\u00d6k\u0000\u0b2e\u0b2f\u0007\'\u0000\u0000\u0b2f"+
		"\u0b31\u0003\u00d6k\u0000\u0b30\u0b2e\u0001\u0000\u0000\u0000\u0b30\u0b31"+
		"\u0001\u0000\u0000\u0000\u0b31\u0b32\u0001\u0000\u0000\u0000\u0b32\u0b33"+
		"\u0005\u0003\u0000\u0000\u0b33\u0b5d\u0001\u0000\u0000\u0000\u0b34\u0b35"+
		"\u0005\u0102\u0000\u0000\u0b35\u0b37\u0005\u0002\u0000\u0000\u0b36\u0b38"+
		"\u0007(\u0000\u0000\u0b37\u0b36\u0001\u0000\u0000\u0000\u0b37\u0b38\u0001"+
		"\u0000\u0000\u0000\u0b38\u0b3a\u0001\u0000\u0000\u0000\u0b39\u0b3b\u0003"+
		"\u00d6k\u0000\u0b3a\u0b39\u0001\u0000\u0000\u0000\u0b3a\u0b3b\u0001\u0000"+
		"\u0000\u0000\u0b3b\u0b3c\u0001\u0000\u0000\u0000\u0b3c\u0b3d\u0005f\u0000"+
		"\u0000\u0b3d\u0b3e\u0003\u00d6k\u0000\u0b3e\u0b3f\u0005\u0003\u0000\u0000"+
		"\u0b3f\u0b5d\u0001\u0000\u0000\u0000\u0b40\u0b41\u0005\u00ad\u0000\u0000"+
		"\u0b41\u0b42\u0005\u0002\u0000\u0000\u0b42\u0b43\u0003\u00d6k\u0000\u0b43"+
		"\u0b44\u0005\u00b6\u0000\u0000\u0b44\u0b45\u0003\u00d6k\u0000\u0b45\u0b46"+
		"\u0005f\u0000\u0000\u0b46\u0b49\u0003\u00d6k\u0000\u0b47\u0b48\u0005b"+
		"\u0000\u0000\u0b48\u0b4a\u0003\u00d6k\u0000\u0b49\u0b47\u0001\u0000\u0000"+
		"\u0000\u0b49\u0b4a\u0001\u0000\u0000\u0000\u0b4a\u0b4b\u0001\u0000\u0000"+
		"\u0000\u0b4b\u0b4c\u0005\u0003\u0000\u0000\u0b4c\u0b5d\u0001\u0000\u0000"+
		"\u0000\u0b4d\u0b4e\u0007)\u0000\u0000\u0b4e\u0b4f\u0005\u0002\u0000\u0000"+
		"\u0b4f\u0b50\u0003\u00d6k\u0000\u0b50\u0b51\u0005\u0003\u0000\u0000\u0b51"+
		"\u0b52\u0005\u011c\u0000\u0000\u0b52\u0b53\u0005l\u0000\u0000\u0b53\u0b54"+
		"\u0005\u0002\u0000\u0000\u0b54\u0b55\u0005\u00a7\u0000\u0000\u0b55\u0b56"+
		"\u0005\u001a\u0000\u0000\u0b56\u0b57\u0003Z-\u0000\u0b57\u0b5a\u0005\u0003"+
		"\u0000\u0000\u0b58\u0b59\u0005\u00ab\u0000\u0000\u0b59\u0b5b\u0003\u0108"+
		"\u0084\u0000\u0b5a\u0b58\u0001\u0000\u0000\u0000\u0b5a\u0b5b\u0001\u0000"+
		"\u0000\u0000\u0b5b\u0b5d\u0001\u0000\u0000\u0000\u0b5c\u0a7f\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0a81\u0001\u0000\u0000\u0000\u0b5c\u0a8a\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0a93\u0001\u0000\u0000\u0000\u0b5c\u0a9f\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0aac\u0001\u0000\u0000\u0000\u0b5c\u0ab3\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0ac0\u0001\u0000\u0000\u0000\u0b5c\u0ac9\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0ad2\u0001\u0000\u0000\u0000\u0b5c\u0ad9\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0ada\u0001\u0000\u0000\u0000\u0b5c\u0adb\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0adf\u0001\u0000\u0000\u0000\u0b5c\u0ae9\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0aed\u0001\u0000\u0000\u0000\u0b5c\u0b0d\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0b11\u0001\u0000\u0000\u0000\u0b5c\u0b1d\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0b1e\u0001\u0000\u0000\u0000\u0b5c\u0b22\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0b29\u0001\u0000\u0000\u0000\u0b5c\u0b34\u0001\u0000"+
		"\u0000\u0000\u0b5c\u0b40\u0001\u0000\u0000\u0000\u0b5c\u0b4d\u0001\u0000"+
		"\u0000\u0000\u0b5d\u0b68\u0001\u0000\u0000\u0000\u0b5e\u0b5f\n\t\u0000"+
		"\u0000\u0b5f\u0b60\u0005\u0006\u0000\u0000\u0b60\u0b61\u0003\u00d6k\u0000"+
		"\u0b61\u0b62\u0005\u0007\u0000\u0000\u0b62\u0b67\u0001\u0000\u0000\u0000"+
		"\u0b63\u0b64\n\u0007\u0000\u0000\u0b64\u0b65\u0005\u0005\u0000\u0000\u0b65"+
		"\u0b67\u0003\u0118\u008c\u0000\u0b66\u0b5e\u0001\u0000\u0000\u0000\u0b66"+
		"\u0b63\u0001\u0000\u0000\u0000\u0b67\u0b6a\u0001\u0000\u0000\u0000\u0b68"+
		"\u0b66\u0001\u0000\u0000\u0000\u0b68\u0b69\u0001\u0000\u0000\u0000\u0b69"+
		"\u00db\u0001\u0000\u0000\u0000\u0b6a\u0b68\u0001\u0000\u0000\u0000\u0b6b"+
		"\u0b78\u0005\u009f\u0000\u0000\u0b6c\u0b78\u0003\u00e6s\u0000\u0b6d\u0b6e"+
		"\u0003\u0118\u008c\u0000\u0b6e\u0b6f\u0005\u0135\u0000\u0000\u0b6f\u0b78"+
		"\u0001\u0000\u0000\u0000\u0b70\u0b78\u0003\u011e\u008f\u0000\u0b71\u0b78"+
		"\u0003\u00e4r\u0000\u0b72\u0b74\u0005\u0135\u0000\u0000\u0b73\u0b72\u0001"+
		"\u0000\u0000\u0000\u0b74\u0b75\u0001\u0000\u0000\u0000\u0b75\u0b73\u0001"+
		"\u0000\u0000\u0000\u0b75\u0b76\u0001\u0000\u0000\u0000\u0b76\u0b78\u0001"+
		"\u0000\u0000\u0000\u0b77\u0b6b\u0001\u0000\u0000\u0000\u0b77\u0b6c\u0001"+
		"\u0000\u0000\u0000\u0b77\u0b6d\u0001\u0000\u0000\u0000\u0b77\u0b70\u0001"+
		"\u0000\u0000\u0000\u0b77\u0b71\u0001\u0000\u0000\u0000\u0b77\u0b73\u0001"+
		"\u0000\u0000\u0000\u0b78\u00dd\u0001\u0000\u0000\u0000\u0b79\u0b7a\u0007"+
		"*\u0000\u0000\u0b7a\u00df\u0001\u0000\u0000\u0000\u0b7b\u0b7c\u0007+\u0000"+
		"\u0000\u0b7c\u00e1\u0001\u0000\u0000\u0000\u0b7d\u0b7e\u0007,\u0000\u0000"+
		"\u0b7e\u00e3\u0001\u0000\u0000\u0000\u0b7f\u0b80\u0007-\u0000\u0000\u0b80"+
		"\u00e5\u0001\u0000\u0000\u0000\u0b81\u0b84\u0005{\u0000\u0000\u0b82\u0b85"+
		"\u0003\u00e8t\u0000\u0b83\u0b85\u0003\u00ecv\u0000\u0b84\u0b82\u0001\u0000"+
		"\u0000\u0000\u0b84\u0b83\u0001\u0000\u0000\u0000\u0b84\u0b85\u0001\u0000"+
		"\u0000\u0000\u0b85\u00e7\u0001\u0000\u0000\u0000\u0b86\u0b88\u0003\u00ea"+
		"u\u0000\u0b87\u0b89\u0003\u00eew\u0000\u0b88\u0b87\u0001\u0000\u0000\u0000"+
		"\u0b88\u0b89\u0001\u0000\u0000\u0000\u0b89\u00e9\u0001\u0000\u0000\u0000"+
		"\u0b8a\u0b8b\u0003\u00f0x\u0000\u0b8b\u0b8c\u0003\u0118\u008c\u0000\u0b8c"+
		"\u0b8e\u0001\u0000\u0000\u0000\u0b8d\u0b8a\u0001\u0000\u0000\u0000\u0b8e"+
		"\u0b8f\u0001\u0000\u0000\u0000\u0b8f\u0b8d\u0001\u0000\u0000\u0000\u0b8f"+
		"\u0b90\u0001\u0000\u0000\u0000\u0b90\u00eb\u0001\u0000\u0000\u0000\u0b91"+
		"\u0b94\u0003\u00eew\u0000\u0b92\u0b95\u0003\u00eau\u0000\u0b93\u0b95\u0003"+
		"\u00eew\u0000\u0b94\u0b92\u0001\u0000\u0000\u0000\u0b94\u0b93\u0001\u0000"+
		"\u0000\u0000\u0b94\u0b95\u0001\u0000\u0000\u0000\u0b95\u00ed\u0001\u0000"+
		"\u0000\u0000\u0b96\u0b97\u0003\u00f0x\u0000\u0b97\u0b98\u0003\u0118\u008c"+
		"\u0000\u0b98\u0b99\u0005\u00fc\u0000\u0000\u0b99\u0b9a\u0003\u0118\u008c"+
		"\u0000\u0b9a\u00ef\u0001\u0000\u0000\u0000\u0b9b\u0b9d\u0007.\u0000\u0000"+
		"\u0b9c\u0b9b\u0001\u0000\u0000\u0000\u0b9c\u0b9d\u0001\u0000\u0000\u0000"+
		"\u0b9d\u0b9e\u0001\u0000\u0000\u0000\u0b9e\u0b9f\u0007/\u0000\u0000\u0b9f"+
		"\u00f1\u0001\u0000\u0000\u0000\u0ba0\u0ba4\u0005`\u0000\u0000\u0ba1\u0ba2"+
		"\u0005\t\u0000\u0000\u0ba2\u0ba4\u0003\u0114\u008a\u0000\u0ba3\u0ba0\u0001"+
		"\u0000\u0000\u0000\u0ba3\u0ba1\u0001\u0000\u0000\u0000\u0ba4\u00f3\u0001"+
		"\u0000\u0000\u0000\u0ba5\u0ba6\u0005\u0011\u0000\u0000\u0ba6\u0ba7\u0005"+
		"\u0123\u0000\u0000\u0ba7\u0ba8\u0003\u00f4z\u0000\u0ba8\u0ba9\u0005\u0125"+
		"\u0000\u0000\u0ba9\u0bd4\u0001\u0000\u0000\u0000\u0baa\u0bab\u0005\u0092"+
		"\u0000\u0000\u0bab\u0bac\u0005\u0123\u0000\u0000\u0bac\u0bad\u0003\u00f4"+
		"z\u0000\u0bad\u0bae\u0005\u0004\u0000\u0000\u0bae\u0baf\u0003\u00f4z\u0000"+
		"\u0baf\u0bb0\u0005\u0125\u0000\u0000\u0bb0\u0bd4\u0001\u0000\u0000\u0000"+
		"\u0bb1\u0bb8\u0005\u00eb\u0000\u0000\u0bb2\u0bb4\u0005\u0123\u0000\u0000"+
		"\u0bb3\u0bb5\u0003\u00fe\u007f\u0000\u0bb4\u0bb3\u0001\u0000\u0000\u0000"+
		"\u0bb4\u0bb5\u0001\u0000\u0000\u0000\u0bb5\u0bb6\u0001\u0000\u0000\u0000"+
		"\u0bb6\u0bb9\u0005\u0125\u0000\u0000\u0bb7\u0bb9\u0005\u0121\u0000\u0000"+
		"\u0bb8\u0bb2\u0001\u0000\u0000\u0000\u0bb8\u0bb7\u0001\u0000\u0000\u0000"+
		"\u0bb9\u0bd4\u0001\u0000\u0000\u0000\u0bba\u0bbb\u0005{\u0000\u0000\u0bbb"+
		"\u0bbe\u00070\u0000\u0000\u0bbc\u0bbd\u0005\u00fc\u0000\u0000\u0bbd\u0bbf"+
		"\u0005\u0098\u0000\u0000\u0bbe\u0bbc\u0001\u0000\u0000\u0000\u0bbe\u0bbf"+
		"\u0001\u0000\u0000\u0000\u0bbf\u0bd4\u0001\u0000\u0000\u0000\u0bc0\u0bc1"+
		"\u0005{\u0000\u0000\u0bc1\u0bc4\u00071\u0000\u0000\u0bc2\u0bc3\u0005\u00fc"+
		"\u0000\u0000\u0bc3\u0bc5\u00072\u0000\u0000\u0bc4\u0bc2\u0001\u0000\u0000"+
		"\u0000\u0bc4\u0bc5\u0001\u0000\u0000\u0000\u0bc5\u0bd4\u0001\u0000\u0000"+
		"\u0000\u0bc6\u0bd1\u0003\u0118\u008c\u0000\u0bc7\u0bc8\u0005\u0002\u0000"+
		"\u0000\u0bc8\u0bcd\u0005\u0139\u0000\u0000\u0bc9\u0bca\u0005\u0004\u0000"+
		"\u0000\u0bca\u0bcc\u0005\u0139\u0000\u0000\u0bcb\u0bc9\u0001\u0000\u0000"+
		"\u0000\u0bcc\u0bcf\u0001\u0000\u0000\u0000\u0bcd\u0bcb\u0001\u0000\u0000"+
		"\u0000\u0bcd\u0bce\u0001\u0000\u0000\u0000\u0bce\u0bd0\u0001\u0000\u0000"+
		"\u0000\u0bcf\u0bcd\u0001\u0000\u0000\u0000\u0bd0\u0bd2\u0005\u0003\u0000"+
		"\u0000\u0bd1\u0bc7\u0001\u0000\u0000\u0000\u0bd1\u0bd2\u0001\u0000\u0000"+
		"\u0000\u0bd2\u0bd4\u0001\u0000\u0000\u0000\u0bd3\u0ba5\u0001\u0000\u0000"+
		"\u0000\u0bd3\u0baa\u0001\u0000\u0000\u0000\u0bd3\u0bb1\u0001\u0000\u0000"+
		"\u0000\u0bd3\u0bba\u0001\u0000\u0000\u0000\u0bd3\u0bc0\u0001\u0000\u0000"+
		"\u0000\u0bd3\u0bc6\u0001\u0000\u0000\u0000\u0bd4\u00f5\u0001\u0000\u0000"+
		"\u0000\u0bd5\u0bda\u0003\u00f8|\u0000\u0bd6\u0bd7\u0005\u0004\u0000\u0000"+
		"\u0bd7\u0bd9\u0003\u00f8|\u0000\u0bd8\u0bd6\u0001\u0000\u0000\u0000\u0bd9"+
		"\u0bdc\u0001\u0000\u0000\u0000\u0bda\u0bd8\u0001\u0000\u0000\u0000\u0bda"+
		"\u0bdb\u0001\u0000\u0000\u0000\u0bdb\u00f7\u0001\u0000\u0000\u0000\u0bdc"+
		"\u0bda\u0001\u0000\u0000\u0000\u0bdd\u0bde\u0003\u00b8\\\u0000\u0bde\u0be1"+
		"\u0003\u00f4z\u0000\u0bdf\u0be0\u0005\u009e\u0000\u0000\u0be0\u0be2\u0005"+
		"\u009f\u0000\u0000\u0be1\u0bdf\u0001\u0000\u0000\u0000\u0be1\u0be2\u0001"+
		"\u0000\u0000\u0000\u0be2\u0be4\u0001\u0000\u0000\u0000\u0be3\u0be5\u0003"+
		" \u0010\u0000\u0be4\u0be3\u0001\u0000\u0000\u0000\u0be4\u0be5\u0001\u0000"+
		"\u0000\u0000\u0be5\u0be7\u0001\u0000\u0000\u0000\u0be6\u0be8\u0003\u00f2"+
		"y\u0000\u0be7\u0be6\u0001\u0000\u0000\u0000\u0be7\u0be8\u0001\u0000\u0000"+
		"\u0000\u0be8\u00f9\u0001\u0000\u0000\u0000\u0be9\u0bee\u0003\u00fc~\u0000"+
		"\u0bea\u0beb\u0005\u0004\u0000\u0000\u0beb\u0bed\u0003\u00fc~\u0000\u0bec"+
		"\u0bea\u0001\u0000\u0000\u0000\u0bed\u0bf0\u0001\u0000\u0000\u0000\u0bee"+
		"\u0bec\u0001\u0000\u0000\u0000\u0bee\u0bef\u0001\u0000\u0000\u0000\u0bef"+
		"\u00fb\u0001\u0000\u0000\u0000\u0bf0\u0bee\u0001\u0000\u0000\u0000\u0bf1"+
		"\u0bf2\u0003\u0114\u008a\u0000\u0bf2\u0bf5\u0003\u00f4z\u0000\u0bf3\u0bf4"+
		"\u0005\u009e\u0000\u0000\u0bf4\u0bf6\u0005\u009f\u0000\u0000\u0bf5\u0bf3"+
		"\u0001\u0000\u0000\u0000\u0bf5\u0bf6\u0001\u0000\u0000\u0000\u0bf6\u0bf8"+
		"\u0001\u0000\u0000\u0000\u0bf7\u0bf9\u0003 \u0010\u0000\u0bf8\u0bf7\u0001"+
		"\u0000\u0000\u0000\u0bf8\u0bf9\u0001\u0000\u0000\u0000\u0bf9\u00fd\u0001"+
		"\u0000\u0000\u0000\u0bfa\u0bff\u0003\u0100\u0080\u0000\u0bfb\u0bfc\u0005"+
		"\u0004\u0000\u0000\u0bfc\u0bfe\u0003\u0100\u0080\u0000\u0bfd\u0bfb\u0001"+
		"\u0000\u0000\u0000\u0bfe\u0c01\u0001\u0000\u0000\u0000\u0bff\u0bfd\u0001"+
		"\u0000\u0000\u0000\u0bff\u0c00\u0001\u0000\u0000\u0000\u0c00\u00ff\u0001"+
		"\u0000\u0000\u0000\u0c01\u0bff\u0001\u0000\u0000\u0000\u0c02\u0c04\u0003"+
		"\u0118\u008c\u0000\u0c03\u0c05\u0005\u0131\u0000\u0000\u0c04\u0c03\u0001"+
		"\u0000\u0000\u0000\u0c04\u0c05\u0001\u0000\u0000\u0000\u0c05\u0c06\u0001"+
		"\u0000\u0000\u0000\u0c06\u0c09\u0003\u00f4z\u0000\u0c07\u0c08\u0005\u009e"+
		"\u0000\u0000\u0c08\u0c0a\u0005\u009f\u0000\u0000\u0c09\u0c07\u0001\u0000"+
		"\u0000\u0000\u0c09\u0c0a\u0001\u0000\u0000\u0000\u0c0a\u0c0c\u0001\u0000"+
		"\u0000\u0000\u0c0b\u0c0d\u0003 \u0010\u0000\u0c0c\u0c0b\u0001\u0000\u0000"+
		"\u0000\u0c0c\u0c0d\u0001\u0000\u0000\u0000\u0c0d\u0101\u0001\u0000\u0000"+
		"\u0000\u0c0e\u0c0f\u0005\u0118\u0000\u0000\u0c0f\u0c10\u0003\u00ceg\u0000"+
		"\u0c10\u0c11\u0005\u00f7\u0000\u0000\u0c11\u0c12\u0003\u00ceg\u0000\u0c12"+
		"\u0103\u0001\u0000\u0000\u0000\u0c13\u0c14\u0005\u011a\u0000\u0000\u0c14"+
		"\u0c19\u0003\u0106\u0083\u0000\u0c15\u0c16\u0005\u0004\u0000\u0000\u0c16"+
		"\u0c18\u0003\u0106\u0083\u0000\u0c17\u0c15\u0001\u0000\u0000\u0000\u0c18"+
		"\u0c1b\u0001\u0000\u0000\u0000\u0c19\u0c17\u0001\u0000\u0000\u0000\u0c19"+
		"\u0c1a\u0001\u0000\u0000\u0000\u0c1a\u0105\u0001\u0000\u0000\u0000\u0c1b"+
		"\u0c19\u0001\u0000\u0000\u0000\u0c1c\u0c1d\u0003\u0114\u008a\u0000\u0c1d"+
		"\u0c1e\u0005\u0012\u0000\u0000\u0c1e\u0c1f\u0003\u0108\u0084\u0000\u0c1f"+
		"\u0107\u0001\u0000\u0000\u0000\u0c20\u0c4f\u0003\u0114\u008a\u0000\u0c21"+
		"\u0c22\u0005\u0002\u0000\u0000\u0c22\u0c23\u0003\u0114\u008a\u0000\u0c23"+
		"\u0c24\u0005\u0003\u0000\u0000\u0c24\u0c4f\u0001\u0000\u0000\u0000\u0c25"+
		"\u0c48\u0005\u0002\u0000\u0000\u0c26\u0c27\u0005$\u0000\u0000\u0c27\u0c28"+
		"\u0005\u001a\u0000\u0000\u0c28\u0c2d\u0003\u00ceg\u0000\u0c29\u0c2a\u0005"+
		"\u0004\u0000\u0000\u0c2a\u0c2c\u0003\u00ceg\u0000\u0c2b\u0c29\u0001\u0000"+
		"\u0000\u0000\u0c2c\u0c2f\u0001\u0000\u0000\u0000\u0c2d\u0c2b\u0001\u0000"+
		"\u0000\u0000\u0c2d\u0c2e\u0001\u0000\u0000\u0000\u0c2e\u0c49\u0001\u0000"+
		"\u0000\u0000\u0c2f\u0c2d\u0001\u0000\u0000\u0000\u0c30\u0c31\u00073\u0000"+
		"\u0000\u0c31\u0c32\u0005\u001a\u0000\u0000\u0c32\u0c37\u0003\u00ceg\u0000"+
		"\u0c33\u0c34\u0005\u0004\u0000\u0000\u0c34\u0c36\u0003\u00ceg\u0000\u0c35"+
		"\u0c33\u0001\u0000\u0000\u0000\u0c36\u0c39\u0001\u0000\u0000\u0000\u0c37"+
		"\u0c35\u0001\u0000\u0000\u0000\u0c37\u0c38\u0001\u0000\u0000\u0000\u0c38"+
		"\u0c3b\u0001\u0000\u0000\u0000\u0c39\u0c37\u0001\u0000\u0000\u0000\u0c3a"+
		"\u0c30\u0001\u0000\u0000\u0000\u0c3a\u0c3b\u0001\u0000\u0000\u0000\u0c3b"+
		"\u0c46\u0001\u0000\u0000\u0000\u0c3c\u0c3d\u00074\u0000\u0000\u0c3d\u0c3e"+
		"\u0005\u001a\u0000\u0000\u0c3e\u0c43\u0003Z-\u0000\u0c3f\u0c40\u0005\u0004"+
		"\u0000\u0000\u0c40\u0c42\u0003Z-\u0000\u0c41\u0c3f\u0001\u0000\u0000\u0000"+
		"\u0c42\u0c45\u0001\u0000\u0000\u0000\u0c43\u0c41\u0001\u0000\u0000\u0000"+
		"\u0c43\u0c44\u0001\u0000\u0000\u0000\u0c44\u0c47\u0001\u0000\u0000\u0000"+
		"\u0c45\u0c43\u0001\u0000\u0000\u0000\u0c46\u0c3c\u0001\u0000\u0000\u0000"+
		"\u0c46\u0c47\u0001\u0000\u0000\u0000\u0c47\u0c49\u0001\u0000\u0000\u0000"+
		"\u0c48\u0c26\u0001\u0000\u0000\u0000\u0c48\u0c3a\u0001\u0000\u0000\u0000"+
		"\u0c49\u0c4b\u0001\u0000\u0000\u0000\u0c4a\u0c4c\u0003\u010a\u0085\u0000"+
		"\u0c4b\u0c4a\u0001\u0000\u0000\u0000\u0c4b\u0c4c\u0001\u0000\u0000\u0000"+
		"\u0c4c\u0c4d\u0001\u0000\u0000\u0000\u0c4d\u0c4f\u0005\u0003\u0000\u0000"+
		"\u0c4e\u0c20\u0001\u0000\u0000\u0000\u0c4e\u0c21\u0001\u0000\u0000\u0000"+
		"\u0c4e\u0c25\u0001\u0000\u0000\u0000\u0c4f\u0109\u0001\u0000\u0000\u0000"+
		"\u0c50\u0c51\u0005\u00bf\u0000\u0000\u0c51\u0c61\u0003\u010c\u0086\u0000"+
		"\u0c52\u0c53\u0005\u00d5\u0000\u0000\u0c53\u0c61\u0003\u010c\u0086\u0000"+
		"\u0c54\u0c55\u0005\u00bf\u0000\u0000\u0c55\u0c56\u0005\u0016\u0000\u0000"+
		"\u0c56\u0c57\u0003\u010c\u0086\u0000\u0c57\u0c58\u0005\r\u0000\u0000\u0c58"+
		"\u0c59\u0003\u010c\u0086\u0000\u0c59\u0c61\u0001\u0000\u0000\u0000\u0c5a"+
		"\u0c5b\u0005\u00d5\u0000\u0000\u0c5b\u0c5c\u0005\u0016\u0000\u0000\u0c5c"+
		"\u0c5d\u0003\u010c\u0086\u0000\u0c5d\u0c5e\u0005\r\u0000\u0000\u0c5e\u0c5f"+
		"\u0003\u010c\u0086\u0000\u0c5f\u0c61\u0001\u0000\u0000\u0000\u0c60\u0c50"+
		"\u0001\u0000\u0000\u0000\u0c60\u0c52\u0001\u0000\u0000\u0000\u0c60\u0c54"+
		"\u0001\u0000\u0000\u0000\u0c60\u0c5a\u0001\u0000\u0000\u0000\u0c61\u010b"+
		"\u0001\u0000\u0000\u0000\u0c62\u0c63\u0005\u0108\u0000\u0000\u0c63\u0c6a"+
		"\u00075\u0000\u0000\u0c64\u0c65\u00056\u0000\u0000\u0c65\u0c6a\u0005\u00d4"+
		"\u0000\u0000\u0c66\u0c67\u0003\u00ceg\u0000\u0c67\u0c68\u00075\u0000\u0000"+
		"\u0c68\u0c6a\u0001\u0000\u0000\u0000\u0c69\u0c62\u0001\u0000\u0000\u0000"+
		"\u0c69\u0c64\u0001\u0000\u0000\u0000\u0c69\u0c66\u0001\u0000\u0000\u0000"+
		"\u0c6a\u010d\u0001\u0000\u0000\u0000\u0c6b\u0c70\u0003\u0112\u0089\u0000"+
		"\u0c6c\u0c6d\u0005\u0004\u0000\u0000\u0c6d\u0c6f\u0003\u0112\u0089\u0000"+
		"\u0c6e\u0c6c\u0001\u0000\u0000\u0000\u0c6f\u0c72\u0001\u0000\u0000\u0000"+
		"\u0c70\u0c6e\u0001\u0000\u0000\u0000\u0c70\u0c71\u0001\u0000\u0000\u0000"+
		"\u0c71\u010f\u0001\u0000\u0000\u0000\u0c72\u0c70\u0001\u0000\u0000\u0000"+
		"\u0c73\u0c78\u0003\u0112\u0089\u0000\u0c74\u0c78\u0005^\u0000\u0000\u0c75"+
		"\u0c78\u0005\u0085\u0000\u0000\u0c76\u0c78\u0005\u00ce\u0000\u0000\u0c77"+
		"\u0c73\u0001\u0000\u0000\u0000\u0c77\u0c74\u0001\u0000\u0000\u0000\u0c77"+
		"\u0c75\u0001\u0000\u0000\u0000\u0c77\u0c76\u0001\u0000\u0000\u0000\u0c78"+
		"\u0111\u0001\u0000\u0000\u0000\u0c79\u0c7e\u0003\u0118\u008c\u0000\u0c7a"+
		"\u0c7b\u0005\u0005\u0000\u0000\u0c7b\u0c7d\u0003\u0118\u008c\u0000\u0c7c"+
		"\u0c7a\u0001\u0000\u0000\u0000\u0c7d\u0c80\u0001\u0000\u0000\u0000\u0c7e"+
		"\u0c7c\u0001\u0000\u0000\u0000\u0c7e\u0c7f\u0001\u0000\u0000\u0000\u0c7f"+
		"\u0113\u0001\u0000\u0000\u0000\u0c80\u0c7e\u0001\u0000\u0000\u0000\u0c81"+
		"\u0c82\u0003\u0118\u008c\u0000\u0c82\u0c83\u0003\u0116\u008b\u0000\u0c83"+
		"\u0115\u0001\u0000\u0000\u0000\u0c84\u0c85\u0005\u0128\u0000\u0000\u0c85"+
		"\u0c87\u0003\u0118\u008c\u0000\u0c86\u0c84\u0001\u0000\u0000\u0000\u0c87"+
		"\u0c88\u0001\u0000\u0000\u0000\u0c88\u0c86\u0001\u0000\u0000\u0000\u0c88"+
		"\u0c89\u0001\u0000\u0000\u0000\u0c89\u0c8c\u0001\u0000\u0000\u0000\u0c8a"+
		"\u0c8c\u0001\u0000\u0000\u0000\u0c8b\u0c86\u0001\u0000\u0000\u0000\u0c8b"+
		"\u0c8a\u0001\u0000\u0000\u0000\u0c8c\u0117\u0001\u0000\u0000\u0000\u0c8d"+
		"\u0c91\u0003\u011a\u008d\u0000\u0c8e\u0c8f\u0004\u008c\u0010\u0000\u0c8f"+
		"\u0c91\u0003\u0124\u0092\u0000\u0c90\u0c8d\u0001\u0000\u0000\u0000\u0c90"+
		"\u0c8e\u0001\u0000\u0000\u0000\u0c91\u0119\u0001\u0000\u0000\u0000\u0c92"+
		"\u0c99\u0005\u013f\u0000\u0000\u0c93\u0c99\u0003\u011c\u008e\u0000\u0c94"+
		"\u0c95\u0004\u008d\u0011\u0000\u0c95\u0c99\u0003\u0122\u0091\u0000\u0c96"+
		"\u0c97\u0004\u008d\u0012\u0000\u0c97\u0c99\u0003\u0126\u0093\u0000\u0c98"+
		"\u0c92\u0001\u0000\u0000\u0000\u0c98\u0c93\u0001\u0000\u0000\u0000\u0c98"+
		"\u0c94\u0001\u0000\u0000\u0000\u0c98\u0c96\u0001\u0000\u0000\u0000\u0c99"+
		"\u011b\u0001\u0000\u0000\u0000\u0c9a\u0c9b\u0005\u0140\u0000\u0000\u0c9b"+
		"\u011d\u0001\u0000\u0000\u0000\u0c9c\u0c9e\u0004\u008f\u0013\u0000\u0c9d"+
		"\u0c9f\u0005\u0128\u0000\u0000\u0c9e\u0c9d\u0001\u0000\u0000\u0000\u0c9e"+
		"\u0c9f\u0001\u0000\u0000\u0000\u0c9f\u0ca0\u0001\u0000\u0000\u0000\u0ca0"+
		"\u0cc8\u0005\u013a\u0000\u0000\u0ca1\u0ca3\u0004\u008f\u0014\u0000\u0ca2"+
		"\u0ca4\u0005\u0128\u0000\u0000\u0ca3\u0ca2\u0001\u0000\u0000\u0000\u0ca3"+
		"\u0ca4\u0001\u0000\u0000\u0000\u0ca4\u0ca5\u0001\u0000\u0000\u0000\u0ca5"+
		"\u0cc8\u0005\u013b\u0000\u0000\u0ca6\u0ca8\u0004\u008f\u0015\u0000\u0ca7"+
		"\u0ca9\u0005\u0128\u0000\u0000\u0ca8\u0ca7\u0001\u0000\u0000\u0000\u0ca8"+
		"\u0ca9\u0001\u0000\u0000\u0000\u0ca9\u0caa\u0001\u0000\u0000\u0000\u0caa"+
		"\u0cc8\u00076\u0000\u0000\u0cab\u0cad\u0005\u0128\u0000\u0000\u0cac\u0cab"+
		"\u0001\u0000\u0000\u0000\u0cac\u0cad\u0001\u0000\u0000\u0000\u0cad\u0cae"+
		"\u0001\u0000\u0000\u0000\u0cae\u0cc8\u0005\u0139\u0000\u0000\u0caf\u0cb1"+
		"\u0005\u0128\u0000\u0000\u0cb0\u0caf\u0001\u0000\u0000\u0000\u0cb0\u0cb1"+
		"\u0001\u0000\u0000\u0000\u0cb1\u0cb2\u0001\u0000\u0000\u0000\u0cb2\u0cc8"+
		"\u0005\u0136\u0000\u0000\u0cb3\u0cb5\u0005\u0128\u0000\u0000\u0cb4\u0cb3"+
		"\u0001\u0000\u0000\u0000\u0cb4\u0cb5\u0001\u0000\u0000\u0000\u0cb5\u0cb6"+
		"\u0001\u0000\u0000\u0000\u0cb6\u0cc8\u0005\u0137\u0000\u0000\u0cb7\u0cb9"+
		"\u0005\u0128\u0000\u0000\u0cb8\u0cb7\u0001\u0000\u0000\u0000\u0cb8\u0cb9"+
		"\u0001\u0000\u0000\u0000\u0cb9\u0cba\u0001\u0000\u0000\u0000\u0cba\u0cc8"+
		"\u0005\u0138\u0000\u0000\u0cbb\u0cbd\u0005\u0128\u0000\u0000\u0cbc\u0cbb"+
		"\u0001\u0000\u0000\u0000\u0cbc\u0cbd\u0001\u0000\u0000\u0000\u0cbd\u0cbe"+
		"\u0001\u0000\u0000\u0000\u0cbe\u0cc8\u0005\u013d\u0000\u0000\u0cbf\u0cc1"+
		"\u0005\u0128\u0000\u0000\u0cc0\u0cbf\u0001\u0000\u0000\u0000\u0cc0\u0cc1"+
		"\u0001\u0000\u0000\u0000\u0cc1\u0cc2\u0001\u0000\u0000\u0000\u0cc2\u0cc8"+
		"\u0005\u013c\u0000\u0000\u0cc3\u0cc5\u0005\u0128\u0000\u0000\u0cc4\u0cc3"+
		"\u0001\u0000\u0000\u0000\u0cc4\u0cc5\u0001\u0000\u0000\u0000\u0cc5\u0cc6"+
		"\u0001\u0000\u0000\u0000\u0cc6\u0cc8\u0005\u013e\u0000\u0000\u0cc7\u0c9c"+
		"\u0001\u0000\u0000\u0000\u0cc7\u0ca1\u0001\u0000\u0000\u0000\u0cc7\u0ca6"+
		"\u0001\u0000\u0000\u0000\u0cc7\u0cac\u0001\u0000\u0000\u0000\u0cc7\u0cb0"+
		"\u0001\u0000\u0000\u0000\u0cc7\u0cb4\u0001\u0000\u0000\u0000\u0cc7\u0cb8"+
		"\u0001\u0000\u0000\u0000\u0cc7\u0cbc\u0001\u0000\u0000\u0000\u0cc7\u0cc0"+
		"\u0001\u0000\u0000\u0000\u0cc7\u0cc4\u0001\u0000\u0000\u0000\u0cc8\u011f"+
		"\u0001\u0000\u0000\u0000\u0cc9\u0cca\u0005\u0106\u0000\u0000\u0cca\u0cd1"+
		"\u0003\u00f4z\u0000\u0ccb\u0cd1\u0003 \u0010\u0000\u0ccc\u0cd1\u0003\u00f2"+
		"y\u0000\u0ccd\u0cce\u00077\u0000\u0000\u0cce\u0ccf\u0005\u009e\u0000\u0000"+
		"\u0ccf\u0cd1\u0005\u009f\u0000\u0000\u0cd0\u0cc9\u0001\u0000\u0000\u0000"+
		"\u0cd0\u0ccb\u0001\u0000\u0000\u0000\u0cd0\u0ccc\u0001\u0000\u0000\u0000"+
		"\u0cd0\u0ccd\u0001\u0000\u0000\u0000\u0cd1\u0121\u0001\u0000\u0000\u0000"+
		"\u0cd2\u0cd3\u00078\u0000\u0000\u0cd3\u0123\u0001\u0000\u0000\u0000\u0cd4"+
		"\u0cd5\u00079\u0000\u0000\u0cd5\u0125\u0001\u0000\u0000\u0000\u0cd6\u0cd7"+
		"\u0007:\u0000\u0000\u0cd7\u0127\u0001\u0000\u0000\u0000\u01b2\u012c\u0145"+
		"\u0152\u0159\u0161\u0163\u0177\u017b\u0181\u0184\u0187\u018e\u0191\u0195"+
		"\u0198\u019f\u01aa\u01ac\u01b4\u01b7\u01bb\u01be\u01c4\u01cf\u01d5\u01da"+
		"\u01fc\u0209\u0222\u022b\u022f\u0235\u0239\u023e\u0244\u0250\u0258\u025e"+
		"\u026b\u0270\u0280\u0287\u028b\u0291\u02a0\u02a4\u02aa\u02b0\u02b3\u02b6"+
		"\u02bc\u02c0\u02c8\u02ca\u02d3\u02d6\u02df\u02e4\u02ea\u02f1\u02f4\u02fa"+
		"\u0305\u0308\u030c\u0311\u0316\u031d\u0320\u0323\u032a\u032f\u0338\u0340"+
		"\u0346\u0349\u034c\u0352\u0356\u035b\u035e\u0362\u0364\u036c\u0374\u0377"+
		"\u037c\u0382\u0388\u038b\u038f\u0392\u0396\u03b2\u03b5\u03b9\u03bf\u03c2"+
		"\u03c5\u03cb\u03d3\u03d8\u03de\u03e4\u03ec\u03f3\u03fb\u040c\u041a\u041d"+
		"\u0423\u042c\u0435\u043d\u0442\u0447\u044e\u0454\u0459\u0461\u0464\u0470"+
		"\u0474\u047b\u04ef\u04f7\u04ff\u0508\u0512\u0516\u0519\u051f\u0525\u0531"+
		"\u053d\u0542\u054b\u0553\u055a\u055c\u055f\u0564\u0568\u056d\u0570\u0575"+
		"\u057a\u057d\u0582\u0586\u058b\u058d\u0591\u059a\u05a2\u05ad\u05b4\u05bd"+
		"\u05c2\u05c5\u05db\u05dd\u05e6\u05ed\u05f0\u05f7\u05fb\u0601\u0609\u0614"+
		"\u061f\u0626\u062c\u0638\u063f\u0646\u0652\u065a\u0660\u0663\u066c\u066f"+
		"\u0678\u067b\u0684\u0687\u0690\u0693\u0696\u069b\u069d\u06a9\u06b0\u06b7"+
		"\u06ba\u06bc\u06c8\u06cc\u06d0\u06d6\u06da\u06e2\u06e6\u06e9\u06ec\u06ef"+
		"\u06f3\u06f7\u06fc\u0700\u0703\u0706\u0709\u070d\u0712\u0716\u0719\u071c"+
		"\u071f\u0721\u0727\u072e\u0733\u0736\u0739\u073d\u0747\u074b\u074d\u0750"+
		"\u0754\u075a\u075e\u0769\u0773\u077f\u078e\u0793\u079a\u07aa\u07af\u07bc"+
		"\u07c1\u07c9\u07cf\u07d3\u07d6\u07dd\u07e3\u07ec\u07f6\u0805\u080a\u080c"+
		"\u0810\u0819\u0826\u082b\u082f\u0837\u083a\u083e\u084c\u0859\u085e\u0862"+
		"\u0865\u086a\u0873\u0876\u087b\u0882\u0885\u088a\u0890\u0896\u089a\u08a0"+
		"\u08a4\u08a7\u08ac\u08af\u08b4\u08b8\u08bb\u08be\u08c4\u08c9\u08d0\u08d3"+
		"\u08e5\u08e7\u08ea\u08f5\u08fe\u0905\u090d\u0914\u0918\u091b\u0923\u092b"+
		"\u0931\u0939\u0945\u0948\u094e\u0952\u0954\u095d\u0969\u096b\u0972\u0979"+
		"\u097f\u0985\u0987\u098e\u0996\u099e\u09a4\u09a9\u09b0\u09b6\u09ba\u09bc"+
		"\u09c3\u09cc\u09d3\u09dd\u09e2\u09e6\u09ef\u09fc\u09fe\u0a06\u0a08\u0a0c"+
		"\u0a14\u0a1d\u0a23\u0a2b\u0a30\u0a3c\u0a41\u0a44\u0a4a\u0a4e\u0a53\u0a58"+
		"\u0a5d\u0a63\u0a78\u0a7a\u0a97\u0a9b\u0aa4\u0aa8\u0aba\u0abd\u0ac5\u0ace"+
		"\u0ae5\u0af0\u0af7\u0afa\u0b03\u0b07\u0b0b\u0b17\u0b30\u0b37\u0b3a\u0b49"+
		"\u0b5a\u0b5c\u0b66\u0b68\u0b75\u0b77\u0b84\u0b88\u0b8f\u0b94\u0b9c\u0ba3"+
		"\u0bb4\u0bb8\u0bbe\u0bc4\u0bcd\u0bd1\u0bd3\u0bda\u0be1\u0be4\u0be7\u0bee"+
		"\u0bf5\u0bf8\u0bff\u0c04\u0c09\u0c0c\u0c19\u0c2d\u0c37\u0c3a\u0c43\u0c46"+
		"\u0c48\u0c4b\u0c4e\u0c60\u0c69\u0c70\u0c77\u0c7e\u0c88\u0c8b\u0c90\u0c98"+
		"\u0c9e\u0ca3\u0ca8\u0cac\u0cb0\u0cb4\u0cb8\u0cbc\u0cc0\u0cc4\u0cc7\u0cd0";
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