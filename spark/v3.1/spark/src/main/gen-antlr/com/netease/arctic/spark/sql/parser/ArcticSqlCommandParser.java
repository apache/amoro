package com.netease.arctic.spark.sql.parser;// Generated from E:/workspace/code/github-arctic-refactor/spark/v3.1/spark/src/main/antlr4/com/netease/arctic/spark/sql/parser/ArcticSqlCommand.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ArcticSqlCommandParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, KEY=12, MIGRATE=13, ARCTIC=14, OPTIMIZE=15, TABLE=16, 
		TO=17, REWRITE=18, DATA=19, USING=20, BIN_PACK=21, ADD=22, AFTER=23, ALL=24, 
		ALTER=25, ANALYZE=26, AND=27, ANTI=28, ANY=29, ARCHIVE=30, ARRAY=31, AS=32, 
		ASC=33, AT=34, AUTHORIZATION=35, BETWEEN=36, BOTH=37, BUCKET=38, BUCKETS=39, 
		BY=40, CACHE=41, CASCADE=42, CASE=43, CAST=44, CHANGE=45, CHECK=46, CLEAR=47, 
		CLUSTER=48, CLUSTERED=49, CODEGEN=50, COLLATE=51, COLLECTION=52, COLUMN=53, 
		COLUMNS=54, COMMENT=55, COMMIT=56, COMPACT=57, COMPACTIONS=58, COMPUTE=59, 
		CONCATENATE=60, CONSTRAINT=61, COST=62, CREATE=63, CROSS=64, CUBE=65, 
		CURRENT=66, CURRENT_DATE=67, CURRENT_TIME=68, CURRENT_TIMESTAMP=69, CURRENT_USER=70, 
		DAY=71, DATABASE=72, DATABASES=73, DBPROPERTIES=74, DEFINED=75, DELETE=76, 
		DELIMITED=77, DESC=78, DESCRIBE=79, DFS=80, DIRECTORIES=81, DIRECTORY=82, 
		DISTINCT=83, DISTRIBUTE=84, DIV=85, DROP=86, ELSE=87, END=88, ESCAPE=89, 
		ESCAPED=90, EXCEPT=91, EXCHANGE=92, EXISTS=93, EXPLAIN=94, EXPORT=95, 
		EXTENDED=96, EXTERNAL=97, EXTRACT=98, FALSE=99, FETCH=100, FIELDS=101, 
		FILTER=102, FILEFORMAT=103, FIRST=104, FOLLOWING=105, FOR=106, FOREIGN=107, 
		FORMAT=108, FORMATTED=109, FROM=110, FULL=111, FUNCTION=112, FUNCTIONS=113, 
		GLOBAL=114, GRANT=115, GROUP=116, GROUPING=117, HAVING=118, HOUR=119, 
		IF=120, IGNORE=121, IMPORT=122, IN=123, INDEX=124, INDEXES=125, INNER=126, 
		INPATH=127, INPUTFORMAT=128, INSERT=129, INTERSECT=130, INTERVAL=131, 
		INTO=132, IS=133, ITEMS=134, JOIN=135, KEYS=136, LAST=137, LATERAL=138, 
		LAZY=139, LEADING=140, LEFT=141, LIKE=142, LIMIT=143, LINES=144, LIST=145, 
		LOAD=146, LOCAL=147, LOCATION=148, LOCK=149, LOCKS=150, LOGICAL=151, MACRO=152, 
		MAP=153, MATCHED=154, MERGE=155, MINUTE=156, MONTH=157, MSCK=158, NAMESPACE=159, 
		NAMESPACES=160, NATURAL=161, NO=162, NOT=163, NULL=164, NULLS=165, OF=166, 
		ON=167, ONLY=168, OPTION=169, OPTIONS=170, OR=171, ORDER=172, OUT=173, 
		OUTER=174, OUTPUTFORMAT=175, OVER=176, OVERLAPS=177, OVERLAY=178, OVERWRITE=179, 
		PARTITION=180, PARTITIONED=181, PARTITIONS=182, PERCENTLIT=183, PIVOT=184, 
		PLACING=185, POSITION=186, PRECEDING=187, PRIMARY=188, PRINCIPALS=189, 
		PROPERTIES=190, PURGE=191, QUERY=192, RANGE=193, RECORDREADER=194, RECORDWRITER=195, 
		RECOVER=196, REDUCE=197, REFERENCES=198, REFRESH=199, RENAME=200, REPAIR=201, 
		REPLACE=202, RESET=203, RESPECT=204, RESTRICT=205, REVOKE=206, RIGHT=207, 
		RLIKE=208, ROLE=209, ROLES=210, ROLLBACK=211, ROLLUP=212, ROW=213, ROWS=214, 
		SECOND=215, SCHEMA=216, SELECT=217, SEMI=218, SEPARATED=219, SERDE=220, 
		SERDEPROPERTIES=221, SESSION_USER=222, SET=223, SETMINUS=224, SETS=225, 
		SHOW=226, SKEWED=227, SOME=228, SORT=229, SORTED=230, START=231, STATISTICS=232, 
		STORED=233, STRATIFY=234, STRUCT=235, SUBSTR=236, SUBSTRING=237, SYNC=238, 
		TABLES=239, TABLESAMPLE=240, TBLPROPERTIES=241, TEMPORARY=242, TERMINATED=243, 
		THEN=244, TIME=245, TOUCH=246, TRAILING=247, TRANSACTION=248, TRANSACTIONS=249, 
		TRANSFORM=250, TRIM=251, TRUE=252, TRUNCATE=253, TRY_CAST=254, TYPE=255, 
		UNARCHIVE=256, UNBOUNDED=257, UNCACHE=258, UNION=259, UNIQUE=260, UNKNOWN=261, 
		UNLOCK=262, UNSET=263, UPDATE=264, USE=265, USER=266, VALUES=267, VIEW=268, 
		VIEWS=269, WHEN=270, WHERE=271, WINDOW=272, WITH=273, YEAR=274, ZONE=275, 
		EQ=276, NSEQ=277, NEQ=278, NEQJ=279, LT=280, LTE=281, GT=282, GTE=283, 
		PLUS=284, MINUS=285, ASTERISK=286, SLASH=287, PERCENT=288, TILDE=289, 
		AMPERSAND=290, PIPE=291, CONCAT_PIPE=292, HAT=293, STRING=294, BIGINT_LITERAL=295, 
		SMALLINT_LITERAL=296, TINYINT_LITERAL=297, INTEGER_VALUE=298, EXPONENT_VALUE=299, 
		DECIMAL_VALUE=300, FLOAT_LITERAL=301, DOUBLE_LITERAL=302, BIGDECIMAL_LITERAL=303, 
		IDENTIFIER=304, BACKQUOTED_IDENTIFIER=305, SIMPLE_COMMENT=306, BRACKETED_COMMENT=307, 
		WS=308, UNRECOGNIZED=309;
	public static final int
		RULE_arcticCommand = 0, RULE_arcticStatement = 1, RULE_singleStatement = 2, 
		RULE_singleExpression = 3, RULE_singleTableIdentifier = 4, RULE_singleMultipartIdentifier = 5, 
		RULE_singleFunctionIdentifier = 6, RULE_singleDataType = 7, RULE_singleTableSchema = 8, 
		RULE_statement = 9, RULE_configKey = 10, RULE_configValue = 11, RULE_unsupportedHiveNativeCommands = 12, 
		RULE_createTableHeader = 13, RULE_replaceTableHeader = 14, RULE_bucketSpec = 15, 
		RULE_skewSpec = 16, RULE_locationSpec = 17, RULE_commentSpec = 18, RULE_query = 19, 
		RULE_insertInto = 20, RULE_partitionSpecLocation = 21, RULE_partitionSpec = 22, 
		RULE_partitionVal = 23, RULE_namespace = 24, RULE_describeFuncName = 25, 
		RULE_describeColName = 26, RULE_ctes = 27, RULE_namedQuery = 28, RULE_tableProvider = 29, 
		RULE_createTableClauses = 30, RULE_tablePropertyList = 31, RULE_tableProperty = 32, 
		RULE_tablePropertyKey = 33, RULE_tablePropertyValue = 34, RULE_constantList = 35, 
		RULE_nestedConstantList = 36, RULE_createFileFormat = 37, RULE_fileFormat = 38, 
		RULE_storageHandler = 39, RULE_resource = 40, RULE_dmlStatementNoWith = 41, 
		RULE_queryOrganization = 42, RULE_multiInsertQueryBody = 43, RULE_queryTerm = 44, 
		RULE_queryPrimary = 45, RULE_sortItem = 46, RULE_fromStatement = 47, RULE_fromStatementBody = 48, 
		RULE_querySpecification = 49, RULE_transformClause = 50, RULE_selectClause = 51, 
		RULE_setClause = 52, RULE_matchedClause = 53, RULE_notMatchedClause = 54, 
		RULE_matchedAction = 55, RULE_notMatchedAction = 56, RULE_assignmentList = 57, 
		RULE_assignment = 58, RULE_whereClause = 59, RULE_havingClause = 60, RULE_hint = 61, 
		RULE_hintStatement = 62, RULE_fromClause = 63, RULE_aggregationClause = 64, 
		RULE_groupByClause = 65, RULE_groupingAnalytics = 66, RULE_groupingElement = 67, 
		RULE_groupingSet = 68, RULE_pivotClause = 69, RULE_pivotColumn = 70, RULE_pivotValue = 71, 
		RULE_lateralView = 72, RULE_setQuantifier = 73, RULE_relation = 74, RULE_joinRelation = 75, 
		RULE_joinType = 76, RULE_joinCriteria = 77, RULE_sample = 78, RULE_sampleMethod = 79, 
		RULE_identifierList = 80, RULE_identifierSeq = 81, RULE_orderedIdentifierList = 82, 
		RULE_orderedIdentifier = 83, RULE_identifierCommentList = 84, RULE_identifierComment = 85, 
		RULE_relationPrimary = 86, RULE_inlineTable = 87, RULE_functionTable = 88, 
		RULE_tableAlias = 89, RULE_rowFormat = 90, RULE_multipartIdentifierList = 91, 
		RULE_multipartIdentifier = 92, RULE_tableIdentifier = 93, RULE_functionIdentifier = 94, 
		RULE_namedExpression = 95, RULE_namedExpressionSeq = 96, RULE_partitionFieldList = 97, 
		RULE_partitionField = 98, RULE_transform = 99, RULE_transformArgument = 100, 
		RULE_expression = 101, RULE_expressionSeq = 102, RULE_booleanExpression = 103, 
		RULE_predicate = 104, RULE_valueExpression = 105, RULE_primaryExpression = 106, 
		RULE_constant = 107, RULE_comparisonOperator = 108, RULE_arithmeticOperator = 109, 
		RULE_predicateOperator = 110, RULE_booleanValue = 111, RULE_interval = 112, 
		RULE_errorCapturingMultiUnitsInterval = 113, RULE_multiUnitsInterval = 114, 
		RULE_errorCapturingUnitToUnitInterval = 115, RULE_unitToUnitInterval = 116, 
		RULE_intervalValue = 117, RULE_colPosition = 118, RULE_dataType = 119, 
		RULE_qualifiedColTypeWithPositionList = 120, RULE_qualifiedColTypeWithPosition = 121, 
		RULE_colTypeList = 122, RULE_colType = 123, RULE_complexColTypeList = 124, 
		RULE_complexColType = 125, RULE_whenClause = 126, RULE_windowClause = 127, 
		RULE_namedWindow = 128, RULE_windowSpec = 129, RULE_windowFrame = 130, 
		RULE_frameBound = 131, RULE_qualifiedNameList = 132, RULE_functionName = 133, 
		RULE_qualifiedName = 134, RULE_errorCapturingIdentifier = 135, RULE_errorCapturingIdentifierExtra = 136, 
		RULE_identifier = 137, RULE_strictIdentifier = 138, RULE_quotedIdentifier = 139, 
		RULE_number = 140, RULE_alterColumnAction = 141, RULE_ansiNonReserved = 142, 
		RULE_strictNonReserved = 143, RULE_nonReserved = 144;
	private static String[] makeRuleNames() {
		return new String[] {
			"arcticCommand", "arcticStatement", "singleStatement", "singleExpression", 
			"singleTableIdentifier", "singleMultipartIdentifier", "singleFunctionIdentifier", 
			"singleDataType", "singleTableSchema", "statement", "configKey", "configValue", 
			"unsupportedHiveNativeCommands", "createTableHeader", "replaceTableHeader", 
			"bucketSpec", "skewSpec", "locationSpec", "commentSpec", "query", "insertInto", 
			"partitionSpecLocation", "partitionSpec", "partitionVal", "namespace", 
			"describeFuncName", "describeColName", "ctes", "namedQuery", "tableProvider", 
			"createTableClauses", "tablePropertyList", "tableProperty", "tablePropertyKey", 
			"tablePropertyValue", "constantList", "nestedConstantList", "createFileFormat", 
			"fileFormat", "storageHandler", "resource", "dmlStatementNoWith", "queryOrganization", 
			"multiInsertQueryBody", "queryTerm", "queryPrimary", "sortItem", "fromStatement", 
			"fromStatementBody", "querySpecification", "transformClause", "selectClause", 
			"setClause", "matchedClause", "notMatchedClause", "matchedAction", "notMatchedAction", 
			"assignmentList", "assignment", "whereClause", "havingClause", "hint", 
			"hintStatement", "fromClause", "aggregationClause", "groupByClause", 
			"groupingAnalytics", "groupingElement", "groupingSet", "pivotClause", 
			"pivotColumn", "pivotValue", "lateralView", "setQuantifier", "relation", 
			"joinRelation", "joinType", "joinCriteria", "sample", "sampleMethod", 
			"identifierList", "identifierSeq", "orderedIdentifierList", "orderedIdentifier", 
			"identifierCommentList", "identifierComment", "relationPrimary", "inlineTable", 
			"functionTable", "tableAlias", "rowFormat", "multipartIdentifierList", 
			"multipartIdentifier", "tableIdentifier", "functionIdentifier", "namedExpression", 
			"namedExpressionSeq", "partitionFieldList", "partitionField", "transform", 
			"transformArgument", "expression", "expressionSeq", "booleanExpression", 
			"predicate", "valueExpression", "primaryExpression", "constant", "comparisonOperator", 
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
			null, "';'", "'('", "')'", "','", "'.'", "'/*+'", "'*/'", "'->'", "'['", 
			"']'", "':'", "'KEY'", "'MIGRATE'", "'ARCTIC'", "'OPTIMIZE'", "'TABLE'", 
			"'TO'", "'REWRITE'", "'DATA'", "'USING'", "'BIN_PACK'", "'ADD'", "'AFTER'", 
			"'ALL'", "'ALTER'", "'ANALYZE'", "'AND'", "'ANTI'", "'ANY'", "'ARCHIVE'", 
			"'ARRAY'", "'AS'", "'ASC'", "'AT'", "'AUTHORIZATION'", "'BETWEEN'", "'BOTH'", 
			"'BUCKET'", "'BUCKETS'", "'BY'", "'CACHE'", "'CASCADE'", "'CASE'", "'CAST'", 
			"'CHANGE'", "'CHECK'", "'CLEAR'", "'CLUSTER'", "'CLUSTERED'", "'CODEGEN'", 
			"'COLLATE'", "'COLLECTION'", "'COLUMN'", "'COLUMNS'", "'COMMENT'", "'COMMIT'", 
			"'COMPACT'", "'COMPACTIONS'", "'COMPUTE'", "'CONCATENATE'", "'CONSTRAINT'", 
			"'COST'", "'CREATE'", "'CROSS'", "'CUBE'", "'CURRENT'", "'CURRENT_DATE'", 
			"'CURRENT_TIME'", "'CURRENT_TIMESTAMP'", "'CURRENT_USER'", "'DAY'", "'DATABASE'", 
			null, "'DBPROPERTIES'", "'DEFINED'", "'DELETE'", "'DELIMITED'", "'DESC'", 
			"'DESCRIBE'", "'DFS'", "'DIRECTORIES'", "'DIRECTORY'", "'DISTINCT'", 
			"'DISTRIBUTE'", "'DIV'", "'DROP'", "'ELSE'", "'END'", "'ESCAPE'", "'ESCAPED'", 
			"'EXCEPT'", "'EXCHANGE'", "'EXISTS'", "'EXPLAIN'", "'EXPORT'", "'EXTENDED'", 
			"'EXTERNAL'", "'EXTRACT'", "'FALSE'", "'FETCH'", "'FIELDS'", "'FILTER'", 
			"'FILEFORMAT'", "'FIRST'", "'FOLLOWING'", "'FOR'", "'FOREIGN'", "'FORMAT'", 
			"'FORMATTED'", "'FROM'", "'FULL'", "'FUNCTION'", "'FUNCTIONS'", "'GLOBAL'", 
			"'GRANT'", "'GROUP'", "'GROUPING'", "'HAVING'", "'HOUR'", "'IF'", "'IGNORE'", 
			"'IMPORT'", "'IN'", "'INDEX'", "'INDEXES'", "'INNER'", "'INPATH'", "'INPUTFORMAT'", 
			"'INSERT'", "'INTERSECT'", "'INTERVAL'", "'INTO'", "'IS'", "'ITEMS'", 
			"'JOIN'", "'KEYS'", "'LAST'", "'LATERAL'", "'LAZY'", "'LEADING'", "'LEFT'", 
			"'LIKE'", "'LIMIT'", "'LINES'", "'LIST'", "'LOAD'", "'LOCAL'", "'LOCATION'", 
			"'LOCK'", "'LOCKS'", "'LOGICAL'", "'MACRO'", "'MAP'", "'MATCHED'", "'MERGE'", 
			"'MINUTE'", "'MONTH'", "'MSCK'", "'NAMESPACE'", "'NAMESPACES'", "'NATURAL'", 
			"'NO'", null, "'NULL'", "'NULLS'", "'OF'", "'ON'", "'ONLY'", "'OPTION'", 
			"'OPTIONS'", "'OR'", "'ORDER'", "'OUT'", "'OUTER'", "'OUTPUTFORMAT'", 
			"'OVER'", "'OVERLAPS'", "'OVERLAY'", "'OVERWRITE'", "'PARTITION'", "'PARTITIONED'", 
			"'PARTITIONS'", "'PERCENT'", "'PIVOT'", "'PLACING'", "'POSITION'", "'PRECEDING'", 
			"'PRIMARY'", "'PRINCIPALS'", "'PROPERTIES'", "'PURGE'", "'QUERY'", "'RANGE'", 
			"'RECORDREADER'", "'RECORDWRITER'", "'RECOVER'", "'REDUCE'", "'REFERENCES'", 
			"'REFRESH'", "'RENAME'", "'REPAIR'", "'REPLACE'", "'RESET'", "'RESPECT'", 
			"'RESTRICT'", "'REVOKE'", "'RIGHT'", null, "'ROLE'", "'ROLES'", "'ROLLBACK'", 
			"'ROLLUP'", "'ROW'", "'ROWS'", "'SECOND'", "'SCHEMA'", "'SELECT'", "'SEMI'", 
			"'SEPARATED'", "'SERDE'", "'SERDEPROPERTIES'", "'SESSION_USER'", "'SET'", 
			"'MINUS'", "'SETS'", "'SHOW'", "'SKEWED'", "'SOME'", "'SORT'", "'SORTED'", 
			"'START'", "'STATISTICS'", "'STORED'", "'STRATIFY'", "'STRUCT'", "'SUBSTR'", 
			"'SUBSTRING'", "'SYNC'", "'TABLES'", "'TABLESAMPLE'", "'TBLPROPERTIES'", 
			null, "'TERMINATED'", "'THEN'", "'TIME'", "'TOUCH'", "'TRAILING'", "'TRANSACTION'", 
			"'TRANSACTIONS'", "'TRANSFORM'", "'TRIM'", "'TRUE'", "'TRUNCATE'", "'TRY_CAST'", 
			"'TYPE'", "'UNARCHIVE'", "'UNBOUNDED'", "'UNCACHE'", "'UNION'", "'UNIQUE'", 
			"'UNKNOWN'", "'UNLOCK'", "'UNSET'", "'UPDATE'", "'USE'", "'USER'", "'VALUES'", 
			"'VIEW'", "'VIEWS'", "'WHEN'", "'WHERE'", "'WINDOW'", "'WITH'", "'YEAR'", 
			"'ZONE'", null, "'<=>'", "'<>'", "'!='", "'<'", null, "'>'", null, "'+'", 
			"'-'", "'*'", "'/'", "'%'", "'~'", "'&'", "'|'", "'||'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"KEY", "MIGRATE", "ARCTIC", "OPTIMIZE", "TABLE", "TO", "REWRITE", "DATA", 
			"USING", "BIN_PACK", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", 
			"ANTI", "ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT", "AUTHORIZATION", 
			"BETWEEN", "BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", "CASCADE", "CASE", 
			"CAST", "CHANGE", "CHECK", "CLEAR", "CLUSTER", "CLUSTERED", "CODEGEN", 
			"COLLATE", "COLLECTION", "COLUMN", "COLUMNS", "COMMENT", "COMMIT", "COMPACT", 
			"COMPACTIONS", "COMPUTE", "CONCATENATE", "CONSTRAINT", "COST", "CREATE", 
			"CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", 
			"CURRENT_USER", "DAY", "DATABASE", "DATABASES", "DBPROPERTIES", "DEFINED", 
			"DELETE", "DELIMITED", "DESC", "DESCRIBE", "DFS", "DIRECTORIES", "DIRECTORY", 
			"DISTINCT", "DISTRIBUTE", "DIV", "DROP", "ELSE", "END", "ESCAPE", "ESCAPED", 
			"EXCEPT", "EXCHANGE", "EXISTS", "EXPLAIN", "EXPORT", "EXTENDED", "EXTERNAL", 
			"EXTRACT", "FALSE", "FETCH", "FIELDS", "FILTER", "FILEFORMAT", "FIRST", 
			"FOLLOWING", "FOR", "FOREIGN", "FORMAT", "FORMATTED", "FROM", "FULL", 
			"FUNCTION", "FUNCTIONS", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HAVING", 
			"HOUR", "IF", "IGNORE", "IMPORT", "IN", "INDEX", "INDEXES", "INNER", 
			"INPATH", "INPUTFORMAT", "INSERT", "INTERSECT", "INTERVAL", "INTO", "IS", 
			"ITEMS", "JOIN", "KEYS", "LAST", "LATERAL", "LAZY", "LEADING", "LEFT", 
			"LIKE", "LIMIT", "LINES", "LIST", "LOAD", "LOCAL", "LOCATION", "LOCK", 
			"LOCKS", "LOGICAL", "MACRO", "MAP", "MATCHED", "MERGE", "MINUTE", "MONTH", 
			"MSCK", "NAMESPACE", "NAMESPACES", "NATURAL", "NO", "NOT", "NULL", "NULLS", 
			"OF", "ON", "ONLY", "OPTION", "OPTIONS", "OR", "ORDER", "OUT", "OUTER", 
			"OUTPUTFORMAT", "OVER", "OVERLAPS", "OVERLAY", "OVERWRITE", "PARTITION", 
			"PARTITIONED", "PARTITIONS", "PERCENTLIT", "PIVOT", "PLACING", "POSITION", 
			"PRECEDING", "PRIMARY", "PRINCIPALS", "PROPERTIES", "PURGE", "QUERY", 
			"RANGE", "RECORDREADER", "RECORDWRITER", "RECOVER", "REDUCE", "REFERENCES", 
			"REFRESH", "RENAME", "REPAIR", "REPLACE", "RESET", "RESPECT", "RESTRICT", 
			"REVOKE", "RIGHT", "RLIKE", "ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", 
			"ROWS", "SECOND", "SCHEMA", "SELECT", "SEMI", "SEPARATED", "SERDE", "SERDEPROPERTIES", 
			"SESSION_USER", "SET", "SETMINUS", "SETS", "SHOW", "SKEWED", "SOME", 
			"SORT", "SORTED", "START", "STATISTICS", "STORED", "STRATIFY", "STRUCT", 
			"SUBSTR", "SUBSTRING", "SYNC", "TABLES", "TABLESAMPLE", "TBLPROPERTIES", 
			"TEMPORARY", "TERMINATED", "THEN", "TIME", "TOUCH", "TRAILING", "TRANSACTION", 
			"TRANSACTIONS", "TRANSFORM", "TRIM", "TRUE", "TRUNCATE", "TRY_CAST", 
			"TYPE", "UNARCHIVE", "UNBOUNDED", "UNCACHE", "UNION", "UNIQUE", "UNKNOWN", 
			"UNLOCK", "UNSET", "UPDATE", "USE", "USER", "VALUES", "VIEW", "VIEWS", 
			"WHEN", "WHERE", "WINDOW", "WITH", "YEAR", "ZONE", "EQ", "NSEQ", "NEQ", 
			"NEQJ", "LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "ASTERISK", "SLASH", 
			"PERCENT", "TILDE", "AMPERSAND", "PIPE", "CONCAT_PIPE", "HAT", "STRING", 
			"BIGINT_LITERAL", "SMALLINT_LITERAL", "TINYINT_LITERAL", "INTEGER_VALUE", 
			"EXPONENT_VALUE", "DECIMAL_VALUE", "FLOAT_LITERAL", "DOUBLE_LITERAL", 
			"BIGDECIMAL_LITERAL", "IDENTIFIER", "BACKQUOTED_IDENTIFIER", "SIMPLE_COMMENT", 
			"BRACKETED_COMMENT", "WS", "UNRECOGNIZED"
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(290);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class OptimizeStatementContext extends ArcticStatementContext {
		public MultipartIdentifierContext source;
		public TerminalNode OPTIMIZE() { return getToken(ArcticSqlCommandParser.OPTIMIZE, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode REWRITE() { return getToken(ArcticSqlCommandParser.REWRITE, 0); }
		public TerminalNode DATA() { return getToken(ArcticSqlCommandParser.DATA, 0); }
		public TerminalNode USING() { return getToken(ArcticSqlCommandParser.USING, 0); }
		public TerminalNode BIN_PACK() { return getToken(ArcticSqlCommandParser.BIN_PACK, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public OptimizeStatementContext(ArcticStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterOptimizeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitOptimizeStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitOptimizeStatement(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
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
			setState(306);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MIGRATE:
				_localctx = new MigrateStatementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(292);
				match(MIGRATE);
				setState(293);
				((MigrateStatementContext)_localctx).source = multipartIdentifier();
				setState(294);
				match(TO);
				setState(295);
				match(ARCTIC);
				setState(296);
				((MigrateStatementContext)_localctx).target = multipartIdentifier();
				}
				break;
			case OPTIMIZE:
				_localctx = new OptimizeStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(298);
				match(OPTIMIZE);
				setState(299);
				match(TABLE);
				setState(300);
				((OptimizeStatementContext)_localctx).source = multipartIdentifier();
				setState(301);
				match(REWRITE);
				setState(302);
				match(DATA);
				setState(303);
				match(USING);
				setState(304);
				match(BIN_PACK);
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

	@SuppressWarnings("CheckReturnValue")
	public static class SingleStatementContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ArcticSqlCommandParser.EOF, 0); }
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
			while (_la==T__0) {
				{
				{
				setState(309);
				match(T__0);
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

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class UseContext extends StatementContext {
		public TerminalNode USE() { return getToken(ArcticSqlCommandParser.USE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode NAMESPACE() { return getToken(ArcticSqlCommandParser.NAMESPACE, 0); }
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class ShowCurrentNamespaceContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticSqlCommandParser.CURRENT, 0); }
		public TerminalNode NAMESPACE() { return getToken(ArcticSqlCommandParser.NAMESPACE, 0); }
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class ShowNamespacesContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode DATABASES() { return getToken(ArcticSqlCommandParser.DATABASES, 0); }
		public TerminalNode NAMESPACES() { return getToken(ArcticSqlCommandParser.NAMESPACES, 0); }
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class ReplaceTableContext extends StatementContext {
		public ReplaceTableHeaderContext replaceTableHeader() {
			return getRuleContext(ReplaceTableHeaderContext.class,0);
		}
		public CreateTableClausesContext createTableClauses() {
			return getRuleContext(CreateTableClausesContext.class,0);
		}
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class SetNamespacePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class HiveReplaceColumnsContext extends StatementContext {
		public MultipartIdentifierContext table;
		public QualifiedColTypeWithPositionListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode REPLACE() { return getToken(ArcticSqlCommandParser.REPLACE, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableContext extends StatementContext {
		public CreateTableHeaderContext createTableHeader() {
			return getRuleContext(CreateTableHeaderContext.class,0);
		}
		public CreateTableClausesContext createTableClauses() {
			return getRuleContext(CreateTableClausesContext.class,0);
		}
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableLikeContext extends StatementContext {
		public TableIdentifierContext target;
		public TableIdentifierContext source;
		public TablePropertyListContext tableProps;
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
	@SuppressWarnings("CheckReturnValue")
	public static class ShowTblPropertiesContext extends StatementContext {
		public MultipartIdentifierContext table;
		public TablePropertyKeyContext key;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TablePropertyKeyContext tablePropertyKey() {
			return getRuleContext(TablePropertyKeyContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
	public static class UnsetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode UNSET() { return getToken(ArcticSqlCommandParser.UNSET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class DropTableColumnsContext extends StatementContext {
		public MultipartIdentifierListContext columns;
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode DROP() { return getToken(ArcticSqlCommandParser.DROP, 0); }
		public TerminalNode COLUMN() { return getToken(ArcticSqlCommandParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticSqlCommandParser.COLUMNS, 0); }
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class ShowFunctionsContext extends StatementContext {
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticSqlCommandParser.SHOW, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticSqlCommandParser.FUNCTIONS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
	public static class CacheTableContext extends StatementContext {
		public TablePropertyListContext options;
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class SetTablePropertiesContext extends StatementContext {
		public TerminalNode ALTER() { return getToken(ArcticSqlCommandParser.ALTER, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode SET() { return getToken(ArcticSqlCommandParser.SET, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
			setState(1072);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
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
				setState(342);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(341);
					match(NAMESPACE);
					}
					break;
				}
				setState(344);
				multipartIdentifier();
				}
				break;
			case 4:
				_localctx = new CreateNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(345);
				match(CREATE);
				setState(346);
				namespace();
				setState(350);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(347);
					match(IF);
					setState(348);
					match(NOT);
					setState(349);
					match(EXISTS);
					}
					break;
				}
				setState(352);
				multipartIdentifier();
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==LOCATION || _la==WITH) {
					{
					setState(358);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(353);
						commentSpec();
						}
						break;
					case LOCATION:
						{
						setState(354);
						locationSpec();
						}
						break;
					case WITH:
						{
						{
						setState(355);
						match(WITH);
						setState(356);
						_la = _input.LA(1);
						if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(357);
						tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(362);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 5:
				_localctx = new SetNamespacePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(363);
				match(ALTER);
				setState(364);
				namespace();
				setState(365);
				multipartIdentifier();
				setState(366);
				match(SET);
				setState(367);
				_la = _input.LA(1);
				if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(368);
				tablePropertyList();
				}
				break;
			case 6:
				_localctx = new SetNamespaceLocationContext(_localctx);
				enterOuterAlt(_localctx, 6);
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
				locationSpec();
				}
				break;
			case 7:
				_localctx = new DropNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(376);
				match(DROP);
				setState(377);
				namespace();
				setState(380);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(378);
					match(IF);
					setState(379);
					match(EXISTS);
					}
					break;
				}
				setState(382);
				multipartIdentifier();
				setState(384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CASCADE || _la==RESTRICT) {
					{
					setState(383);
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
			case 8:
				_localctx = new ShowNamespacesContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(386);
				match(SHOW);
				setState(387);
				_la = _input.LA(1);
				if ( !(_la==DATABASES || _la==NAMESPACES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(390);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(388);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(389);
					multipartIdentifier();
					}
				}

				setState(396);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(393);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(392);
						match(LIKE);
						}
					}

					setState(395);
					((ShowNamespacesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 9:
				_localctx = new CreateTableContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(398);
				createTableHeader();
				setState(403);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
				case 1:
					{
					setState(399);
					match(T__1);
					setState(400);
					colTypeList();
					setState(401);
					match(T__2);
					}
					break;
				}
				setState(406);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(405);
					tableProvider();
					}
				}

				setState(408);
				createTableClauses();
				setState(413);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4295032836L) != 0) || _la==FROM || _la==MAP || _la==REDUCE || _la==SELECT || _la==VALUES || _la==WITH) {
					{
					setState(410);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(409);
						match(AS);
						}
					}

					setState(412);
					query();
					}
				}

				}
				break;
			case 10:
				_localctx = new CreateTableLikeContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(415);
				match(CREATE);
				setState(416);
				match(TABLE);
				setState(420);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
				case 1:
					{
					setState(417);
					match(IF);
					setState(418);
					match(NOT);
					setState(419);
					match(EXISTS);
					}
					break;
				}
				setState(422);
				((CreateTableLikeContext)_localctx).target = tableIdentifier();
				setState(423);
				match(LIKE);
				setState(424);
				((CreateTableLikeContext)_localctx).source = tableIdentifier();
				setState(433);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==USING || _la==LOCATION || ((((_la - 213)) & ~0x3f) == 0 && ((1L << (_la - 213)) & 269484033L) != 0)) {
					{
					setState(431);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case USING:
						{
						setState(425);
						tableProvider();
						}
						break;
					case ROW:
						{
						setState(426);
						rowFormat();
						}
						break;
					case STORED:
						{
						setState(427);
						createFileFormat();
						}
						break;
					case LOCATION:
						{
						setState(428);
						locationSpec();
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(429);
						match(TBLPROPERTIES);
						setState(430);
						((CreateTableLikeContext)_localctx).tableProps = tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(435);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 11:
				_localctx = new ReplaceTableContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(436);
				replaceTableHeader();
				setState(441);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(437);
					match(T__1);
					setState(438);
					colTypeList();
					setState(439);
					match(T__2);
					}
					break;
				}
				setState(444);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(443);
					tableProvider();
					}
				}

				setState(446);
				createTableClauses();
				setState(451);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4295032836L) != 0) || _la==FROM || _la==MAP || _la==REDUCE || _la==SELECT || _la==VALUES || _la==WITH) {
					{
					setState(448);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(447);
						match(AS);
						}
					}

					setState(450);
					query();
					}
				}

				}
				break;
			case 12:
				_localctx = new AnalyzeContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(453);
				match(ANALYZE);
				setState(454);
				match(TABLE);
				setState(455);
				multipartIdentifier();
				setState(457);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(456);
					partitionSpec();
					}
				}

				setState(459);
				match(COMPUTE);
				setState(460);
				match(STATISTICS);
				setState(468);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(461);
					identifier();
					}
					break;
				case 2:
					{
					setState(462);
					match(FOR);
					setState(463);
					match(COLUMNS);
					setState(464);
					identifierSeq();
					}
					break;
				case 3:
					{
					setState(465);
					match(FOR);
					setState(466);
					match(ALL);
					setState(467);
					match(COLUMNS);
					}
					break;
				}
				}
				break;
			case 13:
				_localctx = new AnalyzeTablesContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(470);
				match(ANALYZE);
				setState(471);
				match(TABLES);
				setState(474);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(472);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(473);
					multipartIdentifier();
					}
				}

				setState(476);
				match(COMPUTE);
				setState(477);
				match(STATISTICS);
				setState(479);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(478);
					identifier();
					}
					break;
				}
				}
				break;
			case 14:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(481);
				match(ALTER);
				setState(482);
				match(TABLE);
				setState(483);
				multipartIdentifier();
				setState(484);
				match(ADD);
				setState(485);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(486);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				}
				break;
			case 15:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 15);
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
				match(T__1);
				setState(494);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(495);
				match(T__2);
				}
				break;
			case 16:
				_localctx = new RenameTableColumnContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(497);
				match(ALTER);
				setState(498);
				match(TABLE);
				setState(499);
				((RenameTableColumnContext)_localctx).table = multipartIdentifier();
				setState(500);
				match(RENAME);
				setState(501);
				match(COLUMN);
				setState(502);
				((RenameTableColumnContext)_localctx).from = multipartIdentifier();
				setState(503);
				match(TO);
				setState(504);
				((RenameTableColumnContext)_localctx).to = errorCapturingIdentifier();
				}
				break;
			case 17:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(506);
				match(ALTER);
				setState(507);
				match(TABLE);
				setState(508);
				multipartIdentifier();
				setState(509);
				match(DROP);
				setState(510);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(511);
				match(T__1);
				setState(512);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				setState(513);
				match(T__2);
				}
				break;
			case 18:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(515);
				match(ALTER);
				setState(516);
				match(TABLE);
				setState(517);
				multipartIdentifier();
				setState(518);
				match(DROP);
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
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				}
				break;
			case 19:
				_localctx = new RenameTableContext(_localctx);
				enterOuterAlt(_localctx, 19);
				{
				setState(522);
				match(ALTER);
				setState(523);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(524);
				((RenameTableContext)_localctx).from = multipartIdentifier();
				setState(525);
				match(RENAME);
				setState(526);
				match(TO);
				setState(527);
				((RenameTableContext)_localctx).to = multipartIdentifier();
				}
				break;
			case 20:
				_localctx = new SetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(529);
				match(ALTER);
				setState(530);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(531);
				multipartIdentifier();
				setState(532);
				match(SET);
				setState(533);
				match(TBLPROPERTIES);
				setState(534);
				tablePropertyList();
				}
				break;
			case 21:
				_localctx = new UnsetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(536);
				match(ALTER);
				setState(537);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(538);
				multipartIdentifier();
				setState(539);
				match(UNSET);
				setState(540);
				match(TBLPROPERTIES);
				setState(543);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(541);
					match(IF);
					setState(542);
					match(EXISTS);
					}
				}

				setState(545);
				tablePropertyList();
				}
				break;
			case 22:
				_localctx = new AlterTableAlterColumnContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(547);
				match(ALTER);
				setState(548);
				match(TABLE);
				setState(549);
				((AlterTableAlterColumnContext)_localctx).table = multipartIdentifier();
				setState(550);
				_la = _input.LA(1);
				if ( !(_la==ALTER || _la==CHANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(552);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
				case 1:
					{
					setState(551);
					match(COLUMN);
					}
					break;
				}
				setState(554);
				((AlterTableAlterColumnContext)_localctx).column = multipartIdentifier();
				setState(556);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==COMMENT || _la==DROP || _la==FIRST || _la==SET || _la==TYPE) {
					{
					setState(555);
					alterColumnAction();
					}
				}

				}
				break;
			case 23:
				_localctx = new HiveChangeColumnContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(558);
				match(ALTER);
				setState(559);
				match(TABLE);
				setState(560);
				((HiveChangeColumnContext)_localctx).table = multipartIdentifier();
				setState(562);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(561);
					partitionSpec();
					}
				}

				setState(564);
				match(CHANGE);
				setState(566);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(565);
					match(COLUMN);
					}
					break;
				}
				setState(568);
				((HiveChangeColumnContext)_localctx).colName = multipartIdentifier();
				setState(569);
				colType();
				setState(571);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==FIRST) {
					{
					setState(570);
					colPosition();
					}
				}

				}
				break;
			case 24:
				_localctx = new HiveReplaceColumnsContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(573);
				match(ALTER);
				setState(574);
				match(TABLE);
				setState(575);
				((HiveReplaceColumnsContext)_localctx).table = multipartIdentifier();
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
				match(REPLACE);
				setState(580);
				match(COLUMNS);
				setState(581);
				match(T__1);
				setState(582);
				((HiveReplaceColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(583);
				match(T__2);
				}
				break;
			case 25:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 25);
				{
				setState(585);
				match(ALTER);
				setState(586);
				match(TABLE);
				setState(587);
				multipartIdentifier();
				setState(589);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(588);
					partitionSpec();
					}
				}

				setState(591);
				match(SET);
				setState(592);
				match(SERDE);
				setState(593);
				match(STRING);
				setState(597);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(594);
					match(WITH);
					setState(595);
					match(SERDEPROPERTIES);
					setState(596);
					tablePropertyList();
					}
				}

				}
				break;
			case 26:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(599);
				match(ALTER);
				setState(600);
				match(TABLE);
				setState(601);
				multipartIdentifier();
				setState(603);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(602);
					partitionSpec();
					}
				}

				setState(605);
				match(SET);
				setState(606);
				match(SERDEPROPERTIES);
				setState(607);
				tablePropertyList();
				}
				break;
			case 27:
				_localctx = new AddTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(609);
				match(ALTER);
				setState(610);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(611);
				multipartIdentifier();
				setState(612);
				match(ADD);
				setState(616);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(613);
					match(IF);
					setState(614);
					match(NOT);
					setState(615);
					match(EXISTS);
					}
				}

				setState(619); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(618);
					partitionSpecLocation();
					}
					}
					setState(621); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==PARTITION );
				}
				break;
			case 28:
				_localctx = new RenameTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(623);
				match(ALTER);
				setState(624);
				match(TABLE);
				setState(625);
				multipartIdentifier();
				setState(626);
				((RenameTablePartitionContext)_localctx).from = partitionSpec();
				setState(627);
				match(RENAME);
				setState(628);
				match(TO);
				setState(629);
				((RenameTablePartitionContext)_localctx).to = partitionSpec();
				}
				break;
			case 29:
				_localctx = new DropTablePartitionsContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(631);
				match(ALTER);
				setState(632);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(633);
				multipartIdentifier();
				setState(634);
				match(DROP);
				setState(637);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(635);
					match(IF);
					setState(636);
					match(EXISTS);
					}
				}

				setState(639);
				partitionSpec();
				setState(644);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(640);
					match(T__3);
					setState(641);
					partitionSpec();
					}
					}
					setState(646);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(648);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(647);
					match(PURGE);
					}
				}

				}
				break;
			case 30:
				_localctx = new SetTableLocationContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(650);
				match(ALTER);
				setState(651);
				match(TABLE);
				setState(652);
				multipartIdentifier();
				setState(654);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(653);
					partitionSpec();
					}
				}

				setState(656);
				match(SET);
				setState(657);
				locationSpec();
				}
				break;
			case 31:
				_localctx = new RecoverPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(659);
				match(ALTER);
				setState(660);
				match(TABLE);
				setState(661);
				multipartIdentifier();
				setState(662);
				match(RECOVER);
				setState(663);
				match(PARTITIONS);
				}
				break;
			case 32:
				_localctx = new DropTableContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(665);
				match(DROP);
				setState(666);
				match(TABLE);
				setState(669);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
				case 1:
					{
					setState(667);
					match(IF);
					setState(668);
					match(EXISTS);
					}
					break;
				}
				setState(671);
				multipartIdentifier();
				setState(673);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(672);
					match(PURGE);
					}
				}

				}
				break;
			case 33:
				_localctx = new DropViewContext(_localctx);
				enterOuterAlt(_localctx, 33);
				{
				setState(675);
				match(DROP);
				setState(676);
				match(VIEW);
				setState(679);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
				case 1:
					{
					setState(677);
					match(IF);
					setState(678);
					match(EXISTS);
					}
					break;
				}
				setState(681);
				multipartIdentifier();
				}
				break;
			case 34:
				_localctx = new CreateViewContext(_localctx);
				enterOuterAlt(_localctx, 34);
				{
				setState(682);
				match(CREATE);
				setState(685);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(683);
					match(OR);
					setState(684);
					match(REPLACE);
					}
				}

				setState(691);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL || _la==TEMPORARY) {
					{
					setState(688);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==GLOBAL) {
						{
						setState(687);
						match(GLOBAL);
						}
					}

					setState(690);
					match(TEMPORARY);
					}
				}

				setState(693);
				match(VIEW);
				setState(697);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
				case 1:
					{
					setState(694);
					match(IF);
					setState(695);
					match(NOT);
					setState(696);
					match(EXISTS);
					}
					break;
				}
				setState(699);
				multipartIdentifier();
				setState(701);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(700);
					identifierCommentList();
					}
				}

				setState(711);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==PARTITIONED || _la==TBLPROPERTIES) {
					{
					setState(709);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(703);
						commentSpec();
						}
						break;
					case PARTITIONED:
						{
						{
						setState(704);
						match(PARTITIONED);
						setState(705);
						match(ON);
						setState(706);
						identifierList();
						}
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(707);
						match(TBLPROPERTIES);
						setState(708);
						tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(713);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(714);
				match(AS);
				setState(715);
				query();
				}
				break;
			case 35:
				_localctx = new CreateTempViewUsingContext(_localctx);
				enterOuterAlt(_localctx, 35);
				{
				setState(717);
				match(CREATE);
				setState(720);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(718);
					match(OR);
					setState(719);
					match(REPLACE);
					}
				}

				setState(723);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL) {
					{
					setState(722);
					match(GLOBAL);
					}
				}

				setState(725);
				match(TEMPORARY);
				setState(726);
				match(VIEW);
				setState(727);
				tableIdentifier();
				setState(732);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(728);
					match(T__1);
					setState(729);
					colTypeList();
					setState(730);
					match(T__2);
					}
				}

				setState(734);
				tableProvider();
				setState(737);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(735);
					match(OPTIONS);
					setState(736);
					tablePropertyList();
					}
				}

				}
				break;
			case 36:
				_localctx = new AlterViewQueryContext(_localctx);
				enterOuterAlt(_localctx, 36);
				{
				setState(739);
				match(ALTER);
				setState(740);
				match(VIEW);
				setState(741);
				multipartIdentifier();
				setState(743);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(742);
					match(AS);
					}
				}

				setState(745);
				query();
				}
				break;
			case 37:
				_localctx = new CreateFunctionContext(_localctx);
				enterOuterAlt(_localctx, 37);
				{
				setState(747);
				match(CREATE);
				setState(750);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(748);
					match(OR);
					setState(749);
					match(REPLACE);
					}
				}

				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(752);
					match(TEMPORARY);
					}
				}

				setState(755);
				match(FUNCTION);
				setState(759);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
				case 1:
					{
					setState(756);
					match(IF);
					setState(757);
					match(NOT);
					setState(758);
					match(EXISTS);
					}
					break;
				}
				setState(761);
				multipartIdentifier();
				setState(762);
				match(AS);
				setState(763);
				((CreateFunctionContext)_localctx).className = match(STRING);
				setState(773);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(764);
					match(USING);
					setState(765);
					resource();
					setState(770);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(766);
						match(T__3);
						setState(767);
						resource();
						}
						}
						setState(772);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				}
				break;
			case 38:
				_localctx = new DropFunctionContext(_localctx);
				enterOuterAlt(_localctx, 38);
				{
				setState(775);
				match(DROP);
				setState(777);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(776);
					match(TEMPORARY);
					}
				}

				setState(779);
				match(FUNCTION);
				setState(782);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
				case 1:
					{
					setState(780);
					match(IF);
					setState(781);
					match(EXISTS);
					}
					break;
				}
				setState(784);
				multipartIdentifier();
				}
				break;
			case 39:
				_localctx = new ExplainContext(_localctx);
				enterOuterAlt(_localctx, 39);
				{
				setState(785);
				match(EXPLAIN);
				setState(787);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CODEGEN || _la==COST || ((((_la - 96)) & ~0x3f) == 0 && ((1L << (_la - 96)) & 36028797018972161L) != 0)) {
					{
					setState(786);
					_la = _input.LA(1);
					if ( !(_la==CODEGEN || _la==COST || ((((_la - 96)) & ~0x3f) == 0 && ((1L << (_la - 96)) & 36028797018972161L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(789);
				statement();
				}
				break;
			case 40:
				_localctx = new ShowTablesContext(_localctx);
				enterOuterAlt(_localctx, 40);
				{
				setState(790);
				match(SHOW);
				setState(791);
				match(TABLES);
				setState(794);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(792);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(793);
					multipartIdentifier();
					}
				}

				setState(800);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(797);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(796);
						match(LIKE);
						}
					}

					setState(799);
					((ShowTablesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 41:
				_localctx = new ShowTableExtendedContext(_localctx);
				enterOuterAlt(_localctx, 41);
				{
				setState(802);
				match(SHOW);
				setState(803);
				match(TABLE);
				setState(804);
				match(EXTENDED);
				setState(807);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(805);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(806);
					((ShowTableExtendedContext)_localctx).ns = multipartIdentifier();
					}
				}

				setState(809);
				match(LIKE);
				setState(810);
				((ShowTableExtendedContext)_localctx).pattern = match(STRING);
				setState(812);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(811);
					partitionSpec();
					}
				}

				}
				break;
			case 42:
				_localctx = new ShowTblPropertiesContext(_localctx);
				enterOuterAlt(_localctx, 42);
				{
				setState(814);
				match(SHOW);
				setState(815);
				match(TBLPROPERTIES);
				setState(816);
				((ShowTblPropertiesContext)_localctx).table = multipartIdentifier();
				setState(821);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(817);
					match(T__1);
					setState(818);
					((ShowTblPropertiesContext)_localctx).key = tablePropertyKey();
					setState(819);
					match(T__2);
					}
				}

				}
				break;
			case 43:
				_localctx = new ShowColumnsContext(_localctx);
				enterOuterAlt(_localctx, 43);
				{
				setState(823);
				match(SHOW);
				setState(824);
				match(COLUMNS);
				setState(825);
				_la = _input.LA(1);
				if ( !(_la==FROM || _la==IN) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(826);
				((ShowColumnsContext)_localctx).table = multipartIdentifier();
				setState(829);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(827);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(828);
					((ShowColumnsContext)_localctx).ns = multipartIdentifier();
					}
				}

				}
				break;
			case 44:
				_localctx = new ShowViewsContext(_localctx);
				enterOuterAlt(_localctx, 44);
				{
				setState(831);
				match(SHOW);
				setState(832);
				match(VIEWS);
				setState(835);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(833);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(834);
					multipartIdentifier();
					}
				}

				setState(841);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(838);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(837);
						match(LIKE);
						}
					}

					setState(840);
					((ShowViewsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 45:
				_localctx = new ShowPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 45);
				{
				setState(843);
				match(SHOW);
				setState(844);
				match(PARTITIONS);
				setState(845);
				multipartIdentifier();
				setState(847);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(846);
					partitionSpec();
					}
				}

				}
				break;
			case 46:
				_localctx = new ShowFunctionsContext(_localctx);
				enterOuterAlt(_localctx, 46);
				{
				setState(849);
				match(SHOW);
				setState(851);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
				case 1:
					{
					setState(850);
					identifier();
					}
					break;
				}
				setState(853);
				match(FUNCTIONS);
				setState(861);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
				case 1:
					{
					setState(855);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
					case 1:
						{
						setState(854);
						match(LIKE);
						}
						break;
					}
					setState(859);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
					case 1:
						{
						setState(857);
						multipartIdentifier();
						}
						break;
					case 2:
						{
						setState(858);
						((ShowFunctionsContext)_localctx).pattern = match(STRING);
						}
						break;
					}
					}
					break;
				}
				}
				break;
			case 47:
				_localctx = new ShowCreateTableContext(_localctx);
				enterOuterAlt(_localctx, 47);
				{
				setState(863);
				match(SHOW);
				setState(864);
				match(CREATE);
				setState(865);
				match(TABLE);
				setState(866);
				multipartIdentifier();
				setState(869);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(867);
					match(AS);
					setState(868);
					match(SERDE);
					}
				}

				}
				break;
			case 48:
				_localctx = new ShowCurrentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 48);
				{
				setState(871);
				match(SHOW);
				setState(872);
				match(CURRENT);
				setState(873);
				match(NAMESPACE);
				}
				break;
			case 49:
				_localctx = new DescribeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 49);
				{
				setState(874);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(875);
				match(FUNCTION);
				setState(877);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
				case 1:
					{
					setState(876);
					match(EXTENDED);
					}
					break;
				}
				setState(879);
				describeFuncName();
				}
				break;
			case 50:
				_localctx = new DescribeNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 50);
				{
				setState(880);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(881);
				namespace();
				setState(883);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
				case 1:
					{
					setState(882);
					match(EXTENDED);
					}
					break;
				}
				setState(885);
				multipartIdentifier();
				}
				break;
			case 51:
				_localctx = new DescribeRelationContext(_localctx);
				enterOuterAlt(_localctx, 51);
				{
				setState(887);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(889);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
				case 1:
					{
					setState(888);
					match(TABLE);
					}
					break;
				}
				setState(892);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
				case 1:
					{
					setState(891);
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
				setState(894);
				multipartIdentifier();
				setState(896);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(895);
					partitionSpec();
					}
					break;
				}
				setState(899);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(898);
					describeColName();
					}
					break;
				}
				}
				break;
			case 52:
				_localctx = new DescribeQueryContext(_localctx);
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
				setState(903);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUERY) {
					{
					setState(902);
					match(QUERY);
					}
				}

				setState(905);
				query();
				}
				break;
			case 53:
				_localctx = new CommentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 53);
				{
				setState(906);
				match(COMMENT);
				setState(907);
				match(ON);
				setState(908);
				namespace();
				setState(909);
				multipartIdentifier();
				setState(910);
				match(IS);
				setState(911);
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
			case 54:
				_localctx = new CommentTableContext(_localctx);
				enterOuterAlt(_localctx, 54);
				{
				setState(913);
				match(COMMENT);
				setState(914);
				match(ON);
				setState(915);
				match(TABLE);
				setState(916);
				multipartIdentifier();
				setState(917);
				match(IS);
				setState(918);
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
			case 55:
				_localctx = new RefreshTableContext(_localctx);
				enterOuterAlt(_localctx, 55);
				{
				setState(920);
				match(REFRESH);
				setState(921);
				match(TABLE);
				setState(922);
				multipartIdentifier();
				}
				break;
			case 56:
				_localctx = new RefreshFunctionContext(_localctx);
				enterOuterAlt(_localctx, 56);
				{
				setState(923);
				match(REFRESH);
				setState(924);
				match(FUNCTION);
				setState(925);
				multipartIdentifier();
				}
				break;
			case 57:
				_localctx = new RefreshResourceContext(_localctx);
				enterOuterAlt(_localctx, 57);
				{
				setState(926);
				match(REFRESH);
				setState(934);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(927);
					match(STRING);
					}
					break;
				case 2:
					{
					setState(931);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
					while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(928);
							matchWildcard();
							}
							} 
						}
						setState(933);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
					}
					}
					break;
				}
				}
				break;
			case 58:
				_localctx = new CacheTableContext(_localctx);
				enterOuterAlt(_localctx, 58);
				{
				setState(936);
				match(CACHE);
				setState(938);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LAZY) {
					{
					setState(937);
					match(LAZY);
					}
				}

				setState(940);
				match(TABLE);
				setState(941);
				multipartIdentifier();
				setState(944);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(942);
					match(OPTIONS);
					setState(943);
					((CacheTableContext)_localctx).options = tablePropertyList();
					}
				}

				setState(950);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4295032836L) != 0) || _la==FROM || _la==MAP || _la==REDUCE || _la==SELECT || _la==VALUES || _la==WITH) {
					{
					setState(947);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(946);
						match(AS);
						}
					}

					setState(949);
					query();
					}
				}

				}
				break;
			case 59:
				_localctx = new UncacheTableContext(_localctx);
				enterOuterAlt(_localctx, 59);
				{
				setState(952);
				match(UNCACHE);
				setState(953);
				match(TABLE);
				setState(956);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
				case 1:
					{
					setState(954);
					match(IF);
					setState(955);
					match(EXISTS);
					}
					break;
				}
				setState(958);
				multipartIdentifier();
				}
				break;
			case 60:
				_localctx = new ClearCacheContext(_localctx);
				enterOuterAlt(_localctx, 60);
				{
				setState(959);
				match(CLEAR);
				setState(960);
				match(CACHE);
				}
				break;
			case 61:
				_localctx = new LoadDataContext(_localctx);
				enterOuterAlt(_localctx, 61);
				{
				setState(961);
				match(LOAD);
				setState(962);
				match(DATA);
				setState(964);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(963);
					match(LOCAL);
					}
				}

				setState(966);
				match(INPATH);
				setState(967);
				((LoadDataContext)_localctx).path = match(STRING);
				setState(969);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OVERWRITE) {
					{
					setState(968);
					match(OVERWRITE);
					}
				}

				setState(971);
				match(INTO);
				setState(972);
				match(TABLE);
				setState(973);
				multipartIdentifier();
				setState(975);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(974);
					partitionSpec();
					}
				}

				}
				break;
			case 62:
				_localctx = new TruncateTableContext(_localctx);
				enterOuterAlt(_localctx, 62);
				{
				setState(977);
				match(TRUNCATE);
				setState(978);
				match(TABLE);
				setState(979);
				multipartIdentifier();
				setState(981);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(980);
					partitionSpec();
					}
				}

				}
				break;
			case 63:
				_localctx = new RepairTableContext(_localctx);
				enterOuterAlt(_localctx, 63);
				{
				setState(983);
				match(MSCK);
				setState(984);
				match(REPAIR);
				setState(985);
				match(TABLE);
				setState(986);
				multipartIdentifier();
				setState(989);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==DROP || _la==SYNC) {
					{
					setState(987);
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
					setState(988);
					match(PARTITIONS);
					}
				}

				}
				break;
			case 64:
				_localctx = new ManageResourceContext(_localctx);
				enterOuterAlt(_localctx, 64);
				{
				setState(991);
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
				setState(992);
				identifier();
				setState(996);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,101,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(993);
						matchWildcard();
						}
						} 
					}
					setState(998);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,101,_ctx);
				}
				}
				break;
			case 65:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 65);
				{
				setState(999);
				match(SET);
				setState(1000);
				match(ROLE);
				setState(1004);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,102,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1001);
						matchWildcard();
						}
						} 
					}
					setState(1006);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,102,_ctx);
				}
				}
				break;
			case 66:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 66);
				{
				setState(1007);
				match(SET);
				setState(1008);
				match(TIME);
				setState(1009);
				match(ZONE);
				setState(1010);
				interval();
				}
				break;
			case 67:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 67);
				{
				setState(1011);
				match(SET);
				setState(1012);
				match(TIME);
				setState(1013);
				match(ZONE);
				setState(1014);
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
			case 68:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 68);
				{
				setState(1015);
				match(SET);
				setState(1016);
				match(TIME);
				setState(1017);
				match(ZONE);
				setState(1021);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,103,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1018);
						matchWildcard();
						}
						} 
					}
					setState(1023);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,103,_ctx);
				}
				}
				break;
			case 69:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 69);
				{
				setState(1024);
				match(SET);
				setState(1025);
				configKey();
				setState(1026);
				match(EQ);
				setState(1027);
				configValue();
				}
				break;
			case 70:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 70);
				{
				setState(1029);
				match(SET);
				setState(1030);
				configKey();
				setState(1038);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1031);
					match(EQ);
					setState(1035);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
					while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(1032);
							matchWildcard();
							}
							} 
						}
						setState(1037);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
					}
					}
				}

				}
				break;
			case 71:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 71);
				{
				setState(1040);
				match(SET);
				setState(1044);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1041);
						matchWildcard();
						}
						} 
					}
					setState(1046);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				}
				setState(1047);
				match(EQ);
				setState(1048);
				configValue();
				}
				break;
			case 72:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 72);
				{
				setState(1049);
				match(SET);
				setState(1053);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1050);
						matchWildcard();
						}
						} 
					}
					setState(1055);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				}
				}
				break;
			case 73:
				_localctx = new ResetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 73);
				{
				setState(1056);
				match(RESET);
				setState(1057);
				configKey();
				}
				break;
			case 74:
				_localctx = new ResetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 74);
				{
				setState(1058);
				match(RESET);
				setState(1062);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
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
				break;
			case 75:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 75);
				{
				setState(1065);
				unsupportedHiveNativeCommands();
				setState(1069);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1066);
						matchWildcard();
						}
						} 
					}
					setState(1071);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1074);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1076);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1246);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1078);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1079);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1080);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1081);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1082);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(GRANT);
				setState(1084);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
				case 1:
					{
					setState(1083);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1086);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(REVOKE);
				setState(1088);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
				case 1:
					{
					setState(1087);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1090);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1091);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(GRANT);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1092);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1093);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				setState(1095);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
				case 1:
					{
					setState(1094);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(GRANT);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1097);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1098);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(PRINCIPALS);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1099);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1100);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLES);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1101);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1102);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CURRENT);
				setState(1103);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ROLES);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1104);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(EXPORT);
				setState(1105);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1106);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(IMPORT);
				setState(1107);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1108);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1109);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(COMPACTIONS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1110);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1111);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CREATE);
				setState(1112);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TABLE);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1113);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1114);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTIONS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1115);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1116);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEXES);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1117);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1118);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(LOCKS);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1119);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1120);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1121);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1122);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1123);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1124);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1125);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1126);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1127);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1128);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1129);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1130);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1131);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1132);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1133);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1134);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1135);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1136);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1137);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1138);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1139);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1140);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1141);
				tableIdentifier();
				setState(1142);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1143);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(CLUSTERED);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(1145);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1146);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1147);
				tableIdentifier();
				setState(1148);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CLUSTERED);
				setState(1149);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(1151);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1152);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1153);
				tableIdentifier();
				setState(1154);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1155);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SORTED);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(1157);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1158);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1159);
				tableIdentifier();
				setState(1160);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SKEWED);
				setState(1161);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(1163);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1164);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1165);
				tableIdentifier();
				setState(1166);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1167);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(1169);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1170);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1171);
				tableIdentifier();
				setState(1172);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1173);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(STORED);
				setState(1174);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(AS);
				setState(1175);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw6 = match(DIRECTORIES);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(1177);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1178);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1179);
				tableIdentifier();
				setState(1180);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1181);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				setState(1182);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(LOCATION);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(1184);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1185);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1186);
				tableIdentifier();
				setState(1187);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(EXCHANGE);
				setState(1188);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(1190);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1191);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1192);
				tableIdentifier();
				setState(1193);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ARCHIVE);
				setState(1194);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(1196);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1197);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1198);
				tableIdentifier();
				setState(1199);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(UNARCHIVE);
				setState(1200);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(1202);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1203);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1204);
				tableIdentifier();
				setState(1205);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TOUCH);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(1207);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1208);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1209);
				tableIdentifier();
				setState(1211);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1210);
					partitionSpec();
					}
				}

				setState(1213);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(COMPACT);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(1215);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1216);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1217);
				tableIdentifier();
				setState(1219);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1218);
					partitionSpec();
					}
				}

				setState(1221);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CONCATENATE);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1223);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1224);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1225);
				tableIdentifier();
				setState(1227);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1226);
					partitionSpec();
					}
				}

				setState(1229);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1230);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(FILEFORMAT);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1232);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1233);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1234);
				tableIdentifier();
				setState(1236);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1235);
					partitionSpec();
					}
				}

				setState(1238);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(REPLACE);
				setState(1239);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(COLUMNS);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1241);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(START);
				setState(1242);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTION);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1243);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(COMMIT);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1244);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ROLLBACK);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1245);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1248);
			match(CREATE);
			setState(1250);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(1249);
				match(TEMPORARY);
				}
			}

			setState(1253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTERNAL) {
				{
				setState(1252);
				match(EXTERNAL);
				}
			}

			setState(1255);
			match(TABLE);
			setState(1259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				{
				setState(1256);
				match(IF);
				setState(1257);
				match(NOT);
				setState(1258);
				match(EXISTS);
				}
				break;
			}
			setState(1261);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1265);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CREATE) {
				{
				setState(1263);
				match(CREATE);
				setState(1264);
				match(OR);
				}
			}

			setState(1267);
			match(REPLACE);
			setState(1268);
			match(TABLE);
			setState(1269);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1271);
			match(CLUSTERED);
			setState(1272);
			match(BY);
			setState(1273);
			identifierList();
			setState(1277);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SORTED) {
				{
				setState(1274);
				match(SORTED);
				setState(1275);
				match(BY);
				setState(1276);
				orderedIdentifierList();
				}
			}

			setState(1279);
			match(INTO);
			setState(1280);
			match(INTEGER_VALUE);
			setState(1281);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1283);
			match(SKEWED);
			setState(1284);
			match(BY);
			setState(1285);
			identifierList();
			setState(1286);
			match(ON);
			setState(1289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,124,_ctx) ) {
			case 1:
				{
				setState(1287);
				constantList();
				}
				break;
			case 2:
				{
				setState(1288);
				nestedConstantList();
				}
				break;
			}
			setState(1294);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				{
				setState(1291);
				match(STORED);
				setState(1292);
				match(AS);
				setState(1293);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1296);
			match(LOCATION);
			setState(1297);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1299);
			match(COMMENT);
			setState(1300);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(1302);
				ctes();
				}
			}

			setState(1305);
			queryTerm(0);
			setState(1306);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class InsertOverwriteDirContext extends InsertIntoContext {
		public Token path;
		public TablePropertyListContext options;
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticSqlCommandParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticSqlCommandParser.DIRECTORY, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode LOCAL() { return getToken(ArcticSqlCommandParser.LOCAL, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticSqlCommandParser.OPTIONS, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
			setState(1369);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
			case 1:
				_localctx = new InsertOverwriteTableContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1308);
				match(INSERT);
				setState(1309);
				match(OVERWRITE);
				setState(1311);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
				case 1:
					{
					setState(1310);
					match(TABLE);
					}
					break;
				}
				setState(1313);
				multipartIdentifier();
				setState(1320);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1314);
					partitionSpec();
					setState(1318);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IF) {
						{
						setState(1315);
						match(IF);
						setState(1316);
						match(NOT);
						setState(1317);
						match(EXISTS);
						}
					}

					}
				}

				setState(1323);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
				case 1:
					{
					setState(1322);
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
				setState(1325);
				match(INSERT);
				setState(1326);
				match(INTO);
				setState(1328);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
				case 1:
					{
					setState(1327);
					match(TABLE);
					}
					break;
				}
				setState(1330);
				multipartIdentifier();
				setState(1332);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1331);
					partitionSpec();
					}
				}

				setState(1337);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(1334);
					match(IF);
					setState(1335);
					match(NOT);
					setState(1336);
					match(EXISTS);
					}
				}

				setState(1340);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
				case 1:
					{
					setState(1339);
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
				setState(1342);
				match(INSERT);
				setState(1343);
				match(OVERWRITE);
				setState(1345);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1344);
					match(LOCAL);
					}
				}

				setState(1347);
				match(DIRECTORY);
				setState(1348);
				((InsertOverwriteHiveDirContext)_localctx).path = match(STRING);
				setState(1350);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(1349);
					rowFormat();
					}
				}

				setState(1353);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORED) {
					{
					setState(1352);
					createFileFormat();
					}
				}

				}
				break;
			case 4:
				_localctx = new InsertOverwriteDirContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1355);
				match(INSERT);
				setState(1356);
				match(OVERWRITE);
				setState(1358);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1357);
					match(LOCAL);
					}
				}

				setState(1360);
				match(DIRECTORY);
				setState(1362);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING) {
					{
					setState(1361);
					((InsertOverwriteDirContext)_localctx).path = match(STRING);
					}
				}

				setState(1364);
				tableProvider();
				setState(1367);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1365);
					match(OPTIONS);
					setState(1366);
					((InsertOverwriteDirContext)_localctx).options = tablePropertyList();
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1371);
			partitionSpec();
			setState(1373);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOCATION) {
				{
				setState(1372);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PartitionSpecContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(ArcticSqlCommandParser.PARTITION, 0); }
		public List<PartitionValContext> partitionVal() {
			return getRuleContexts(PartitionValContext.class);
		}
		public PartitionValContext partitionVal(int i) {
			return getRuleContext(PartitionValContext.class,i);
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
			setState(1375);
			match(PARTITION);
			setState(1376);
			match(T__1);
			setState(1377);
			partitionVal();
			setState(1382);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(1378);
				match(T__3);
				setState(1379);
				partitionVal();
				}
				}
				setState(1384);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1385);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
			setState(1387);
			identifier();
			setState(1390);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1388);
				match(EQ);
				setState(1389);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(1392);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 50, RULE_describeFuncName);
		try {
			setState(1399);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1394);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1395);
				match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1396);
				comparisonOperator();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1397);
				arithmeticOperator();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1398);
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

	@SuppressWarnings("CheckReturnValue")
	public static class DescribeColNameContext extends ParserRuleContext {
		public IdentifierContext identifier;
		public List<IdentifierContext> nameParts = new ArrayList<IdentifierContext>();
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
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
		enterRule(_localctx, 52, RULE_describeColName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1401);
			((DescribeColNameContext)_localctx).identifier = identifier();
			((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
			setState(1406);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__4) {
				{
				{
				setState(1402);
				match(T__4);
				setState(1403);
				((DescribeColNameContext)_localctx).identifier = identifier();
				((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
				}
				}
				setState(1408);
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

	@SuppressWarnings("CheckReturnValue")
	public static class CtesContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public List<NamedQueryContext> namedQuery() {
			return getRuleContexts(NamedQueryContext.class);
		}
		public NamedQueryContext namedQuery(int i) {
			return getRuleContext(NamedQueryContext.class,i);
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
		enterRule(_localctx, 54, RULE_ctes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1409);
			match(WITH);
			setState(1410);
			namedQuery();
			setState(1415);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(1411);
				match(T__3);
				setState(1412);
				namedQuery();
				}
				}
				setState(1417);
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

	@SuppressWarnings("CheckReturnValue")
	public static class NamedQueryContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext name;
		public IdentifierListContext columnAliases;
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
		enterRule(_localctx, 56, RULE_namedQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1418);
			((NamedQueryContext)_localctx).name = errorCapturingIdentifier();
			setState(1420);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				{
				setState(1419);
				((NamedQueryContext)_localctx).columnAliases = identifierList();
				}
				break;
			}
			setState(1423);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(1422);
				match(AS);
				}
			}

			setState(1425);
			match(T__1);
			setState(1426);
			query();
			setState(1427);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 58, RULE_tableProvider);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1429);
			match(USING);
			setState(1430);
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

	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableClausesContext extends ParserRuleContext {
		public TablePropertyListContext options;
		public PartitionFieldListContext partitioning;
		public TablePropertyListContext tableProps;
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
		enterRule(_localctx, 60, RULE_createTableClauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1447);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CLUSTERED || _la==COMMENT || ((((_la - 148)) & ~0x3f) == 0 && ((1L << (_la - 148)) & 8594128897L) != 0) || ((((_la - 213)) & ~0x3f) == 0 && ((1L << (_la - 213)) & 269500417L) != 0)) {
				{
				setState(1445);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case OPTIONS:
					{
					{
					setState(1432);
					match(OPTIONS);
					setState(1433);
					((CreateTableClausesContext)_localctx).options = tablePropertyList();
					}
					}
					break;
				case PARTITIONED:
					{
					{
					setState(1434);
					match(PARTITIONED);
					setState(1435);
					match(BY);
					setState(1436);
					((CreateTableClausesContext)_localctx).partitioning = partitionFieldList();
					}
					}
					break;
				case SKEWED:
					{
					setState(1437);
					skewSpec();
					}
					break;
				case CLUSTERED:
					{
					setState(1438);
					bucketSpec();
					}
					break;
				case ROW:
					{
					setState(1439);
					rowFormat();
					}
					break;
				case STORED:
					{
					setState(1440);
					createFileFormat();
					}
					break;
				case LOCATION:
					{
					setState(1441);
					locationSpec();
					}
					break;
				case COMMENT:
					{
					setState(1442);
					commentSpec();
					}
					break;
				case TBLPROPERTIES:
					{
					{
					setState(1443);
					match(TBLPROPERTIES);
					setState(1444);
					((CreateTableClausesContext)_localctx).tableProps = tablePropertyList();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(1449);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TablePropertyListContext extends ParserRuleContext {
		public List<TablePropertyContext> tableProperty() {
			return getRuleContexts(TablePropertyContext.class);
		}
		public TablePropertyContext tableProperty(int i) {
			return getRuleContext(TablePropertyContext.class,i);
		}
		public TablePropertyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablePropertyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTablePropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTablePropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTablePropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyListContext tablePropertyList() throws RecognitionException {
		TablePropertyListContext _localctx = new TablePropertyListContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_tablePropertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1450);
			match(T__1);
			setState(1451);
			tableProperty();
			setState(1456);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(1452);
				match(T__3);
				setState(1453);
				tableProperty();
				}
				}
				setState(1458);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1459);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TablePropertyContext extends ParserRuleContext {
		public TablePropertyKeyContext key;
		public TablePropertyValueContext value;
		public TablePropertyKeyContext tablePropertyKey() {
			return getRuleContext(TablePropertyKeyContext.class,0);
		}
		public TablePropertyValueContext tablePropertyValue() {
			return getRuleContext(TablePropertyValueContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticSqlCommandParser.EQ, 0); }
		public TablePropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTableProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTableProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTableProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyContext tableProperty() throws RecognitionException {
		TablePropertyContext _localctx = new TablePropertyContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_tableProperty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1461);
			((TablePropertyContext)_localctx).key = tablePropertyKey();
			setState(1466);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FALSE || ((((_la - 252)) & ~0x3f) == 0 && ((1L << (_la - 252)) & 356241784176641L) != 0)) {
				{
				setState(1463);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1462);
					match(EQ);
					}
				}

				setState(1465);
				((TablePropertyContext)_localctx).value = tablePropertyValue();
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

	@SuppressWarnings("CheckReturnValue")
	public static class TablePropertyKeyContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TablePropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablePropertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTablePropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTablePropertyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTablePropertyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyKeyContext tablePropertyKey() throws RecognitionException {
		TablePropertyKeyContext _localctx = new TablePropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_tablePropertyKey);
		int _la;
		try {
			setState(1477);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1468);
				identifier();
				setState(1473);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__4) {
					{
					{
					setState(1469);
					match(T__4);
					setState(1470);
					identifier();
					}
					}
					setState(1475);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1476);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TablePropertyValueContext extends ParserRuleContext {
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticSqlCommandParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticSqlCommandParser.DECIMAL_VALUE, 0); }
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TablePropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablePropertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).enterTablePropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticSqlCommandListener ) ((ArcticSqlCommandListener)listener).exitTablePropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticSqlCommandVisitor ) return ((ArcticSqlCommandVisitor<? extends T>)visitor).visitTablePropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyValueContext tablePropertyValue() throws RecognitionException {
		TablePropertyValueContext _localctx = new TablePropertyValueContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_tablePropertyValue);
		try {
			setState(1483);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_VALUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1479);
				match(INTEGER_VALUE);
				}
				break;
			case DECIMAL_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1480);
				match(DECIMAL_VALUE);
				}
				break;
			case FALSE:
			case TRUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1481);
				booleanValue();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(1482);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ConstantListContext extends ParserRuleContext {
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
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
		enterRule(_localctx, 70, RULE_constantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1485);
			match(T__1);
			setState(1486);
			constant();
			setState(1491);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(1487);
				match(T__3);
				setState(1488);
				constant();
				}
				}
				setState(1493);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1494);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NestedConstantListContext extends ParserRuleContext {
		public List<ConstantListContext> constantList() {
			return getRuleContexts(ConstantListContext.class);
		}
		public ConstantListContext constantList(int i) {
			return getRuleContext(ConstantListContext.class,i);
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
		enterRule(_localctx, 72, RULE_nestedConstantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1496);
			match(T__1);
			setState(1497);
			constantList();
			setState(1502);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(1498);
				match(T__3);
				setState(1499);
				constantList();
				}
				}
				setState(1504);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1505);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 74, RULE_createFileFormat);
		try {
			setState(1513);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1507);
				match(STORED);
				setState(1508);
				match(AS);
				setState(1509);
				fileFormat();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1510);
				match(STORED);
				setState(1511);
				match(BY);
				setState(1512);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 76, RULE_fileFormat);
		try {
			setState(1520);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
			case 1:
				_localctx = new TableFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1515);
				match(INPUTFORMAT);
				setState(1516);
				((TableFileFormatContext)_localctx).inFmt = match(STRING);
				setState(1517);
				match(OUTPUTFORMAT);
				setState(1518);
				((TableFileFormatContext)_localctx).outFmt = match(STRING);
				}
				break;
			case 2:
				_localctx = new GenericFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1519);
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

	@SuppressWarnings("CheckReturnValue")
	public static class StorageHandlerContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		enterRule(_localctx, 78, RULE_storageHandler);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1522);
			match(STRING);
			setState(1526);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
			case 1:
				{
				setState(1523);
				match(WITH);
				setState(1524);
				match(SERDEPROPERTIES);
				setState(1525);
				tablePropertyList();
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 80, RULE_resource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1528);
			identifier();
			setState(1529);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class SingleInsertQueryContext extends DmlStatementNoWithContext {
		public InsertIntoContext insertInto() {
			return getRuleContext(InsertIntoContext.class,0);
		}
		public QueryTermContext queryTerm() {
			return getRuleContext(QueryTermContext.class,0);
		}
		public QueryOrganizationContext queryOrganization() {
			return getRuleContext(QueryOrganizationContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 82, RULE_dmlStatementNoWith);
		int _la;
		try {
			int _alt;
			setState(1582);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INSERT:
				_localctx = new SingleInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1531);
				insertInto();
				setState(1532);
				queryTerm(0);
				setState(1533);
				queryOrganization();
				}
				break;
			case FROM:
				_localctx = new MultiInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1535);
				fromClause();
				setState(1537); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1536);
					multiInsertQueryBody();
					}
					}
					setState(1539); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==INSERT );
				}
				break;
			case DELETE:
				_localctx = new DeleteFromTableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1541);
				match(DELETE);
				setState(1542);
				match(FROM);
				setState(1543);
				multipartIdentifier();
				setState(1544);
				tableAlias();
				setState(1546);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1545);
					whereClause();
					}
				}

				}
				break;
			case UPDATE:
				_localctx = new UpdateTableContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1548);
				match(UPDATE);
				setState(1549);
				multipartIdentifier();
				setState(1550);
				tableAlias();
				setState(1551);
				setClause();
				setState(1553);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1552);
					whereClause();
					}
				}

				}
				break;
			case MERGE:
				_localctx = new MergeIntoTableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1555);
				match(MERGE);
				setState(1556);
				match(INTO);
				setState(1557);
				((MergeIntoTableContext)_localctx).target = multipartIdentifier();
				setState(1558);
				((MergeIntoTableContext)_localctx).targetAlias = tableAlias();
				setState(1559);
				match(USING);
				setState(1565);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
				case 1:
					{
					setState(1560);
					((MergeIntoTableContext)_localctx).source = multipartIdentifier();
					}
					break;
				case 2:
					{
					setState(1561);
					match(T__1);
					setState(1562);
					((MergeIntoTableContext)_localctx).sourceQuery = query();
					setState(1563);
					match(T__2);
					}
					break;
				}
				setState(1567);
				((MergeIntoTableContext)_localctx).sourceAlias = tableAlias();
				setState(1568);
				match(ON);
				setState(1569);
				((MergeIntoTableContext)_localctx).mergeCondition = booleanExpression(0);
				setState(1573);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,167,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1570);
						matchedClause();
						}
						} 
					}
					setState(1575);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,167,_ctx);
				}
				setState(1579);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(1576);
					notMatchedClause();
					}
					}
					setState(1581);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 84, RULE_queryOrganization);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1594);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
			case 1:
				{
				setState(1584);
				match(ORDER);
				setState(1585);
				match(BY);
				setState(1586);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1591);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1587);
						match(T__3);
						setState(1588);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1593);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				}
				}
				break;
			}
			setState(1606);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
			case 1:
				{
				setState(1596);
				match(CLUSTER);
				setState(1597);
				match(BY);
				setState(1598);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1603);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1599);
						match(T__3);
						setState(1600);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1605);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
				}
				}
				break;
			}
			setState(1618);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
			case 1:
				{
				setState(1608);
				match(DISTRIBUTE);
				setState(1609);
				match(BY);
				setState(1610);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1615);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,174,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1611);
						match(T__3);
						setState(1612);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1617);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,174,_ctx);
				}
				}
				break;
			}
			setState(1630);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				{
				setState(1620);
				match(SORT);
				setState(1621);
				match(BY);
				setState(1622);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1627);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1623);
						match(T__3);
						setState(1624);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1629);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
				}
				}
				break;
			}
			setState(1633);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				setState(1632);
				windowClause();
				}
				break;
			}
			setState(1640);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				{
				setState(1635);
				match(LIMIT);
				setState(1638);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
				case 1:
					{
					setState(1636);
					match(ALL);
					}
					break;
				case 2:
					{
					setState(1637);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 86, RULE_multiInsertQueryBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1642);
			insertInto();
			setState(1643);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		int _startState = 88;
		enterRecursionRule(_localctx, 88, RULE_queryTerm, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new QueryTermDefaultContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(1646);
			queryPrimary();
			}
			_ctx.stop = _input.LT(-1);
			setState(1671);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1669);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
					case 1:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1648);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1649);
						if (!(legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "legacy_setops_precedence_enabled");
						setState(1650);
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
						setState(1652);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1651);
							setQuantifier();
							}
						}

						setState(1654);
						((SetOperationContext)_localctx).right = queryTerm(4);
						}
						break;
					case 2:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1655);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1656);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1657);
						((SetOperationContext)_localctx).operator = match(INTERSECT);
						setState(1659);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1658);
							setQuantifier();
							}
						}

						setState(1661);
						((SetOperationContext)_localctx).right = queryTerm(3);
						}
						break;
					case 3:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1662);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(1663);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1664);
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
						setState(1666);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1665);
							setQuantifier();
							}
						}

						setState(1668);
						((SetOperationContext)_localctx).right = queryTerm(2);
						}
						break;
					}
					} 
				}
				setState(1673);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class SubqueryContext extends QueryPrimaryContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 90, RULE_queryPrimary);
		try {
			setState(1683);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAP:
			case REDUCE:
			case SELECT:
				_localctx = new QueryPrimaryDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1674);
				querySpecification();
				}
				break;
			case FROM:
				_localctx = new FromStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1675);
				fromStatement();
				}
				break;
			case TABLE:
				_localctx = new TableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1676);
				match(TABLE);
				setState(1677);
				multipartIdentifier();
				}
				break;
			case VALUES:
				_localctx = new InlineTableDefault1Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1678);
				inlineTable();
				}
				break;
			case T__1:
				_localctx = new SubqueryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1679);
				match(T__1);
				setState(1680);
				query();
				setState(1681);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 92, RULE_sortItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1685);
			expression();
			setState(1687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
			case 1:
				{
				setState(1686);
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
			setState(1691);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				{
				setState(1689);
				match(NULLS);
				setState(1690);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 94, RULE_fromStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1693);
			fromClause();
			setState(1695); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1694);
					fromStatementBody();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1697); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,189,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 96, RULE_fromStatementBody);
		try {
			int _alt;
			setState(1726);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,196,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1699);
				transformClause();
				setState(1701);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
				case 1:
					{
					setState(1700);
					whereClause();
					}
					break;
				}
				setState(1703);
				queryOrganization();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1705);
				selectClause();
				setState(1709);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,191,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1706);
						lateralView();
						}
						} 
					}
					setState(1711);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,191,_ctx);
				}
				setState(1713);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
				case 1:
					{
					setState(1712);
					whereClause();
					}
					break;
				}
				setState(1716);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
				case 1:
					{
					setState(1715);
					aggregationClause();
					}
					break;
				}
				setState(1719);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
				case 1:
					{
					setState(1718);
					havingClause();
					}
					break;
				}
				setState(1722);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
				case 1:
					{
					setState(1721);
					windowClause();
					}
					break;
				}
				setState(1724);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 98, RULE_querySpecification);
		try {
			int _alt;
			setState(1772);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
			case 1:
				_localctx = new TransformQuerySpecificationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1728);
				transformClause();
				setState(1730);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
				case 1:
					{
					setState(1729);
					fromClause();
					}
					break;
				}
				setState(1735);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1732);
						lateralView();
						}
						} 
					}
					setState(1737);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
				}
				setState(1739);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,199,_ctx) ) {
				case 1:
					{
					setState(1738);
					whereClause();
					}
					break;
				}
				setState(1742);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
				case 1:
					{
					setState(1741);
					aggregationClause();
					}
					break;
				}
				setState(1745);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,201,_ctx) ) {
				case 1:
					{
					setState(1744);
					havingClause();
					}
					break;
				}
				setState(1748);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
				case 1:
					{
					setState(1747);
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
				setState(1750);
				selectClause();
				setState(1752);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
				case 1:
					{
					setState(1751);
					fromClause();
					}
					break;
				}
				setState(1757);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,204,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1754);
						lateralView();
						}
						} 
					}
					setState(1759);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,204,_ctx);
				}
				setState(1761);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
				case 1:
					{
					setState(1760);
					whereClause();
					}
					break;
				}
				setState(1764);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
				case 1:
					{
					setState(1763);
					aggregationClause();
					}
					break;
				}
				setState(1767);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(1766);
					havingClause();
					}
					break;
				}
				setState(1770);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
				case 1:
					{
					setState(1769);
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

	@SuppressWarnings("CheckReturnValue")
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
		public ExpressionSeqContext expressionSeq() {
			return getRuleContext(ExpressionSeqContext.class,0);
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
		enterRule(_localctx, 100, RULE_transformClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1793);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(1774);
				match(SELECT);
				setState(1775);
				((TransformClauseContext)_localctx).kind = match(TRANSFORM);
				setState(1776);
				match(T__1);
				setState(1778);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
				case 1:
					{
					setState(1777);
					setQuantifier();
					}
					break;
				}
				setState(1780);
				expressionSeq();
				setState(1781);
				match(T__2);
				}
				break;
			case MAP:
				{
				setState(1783);
				((TransformClauseContext)_localctx).kind = match(MAP);
				setState(1785);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
				case 1:
					{
					setState(1784);
					setQuantifier();
					}
					break;
				}
				setState(1787);
				expressionSeq();
				}
				break;
			case REDUCE:
				{
				setState(1788);
				((TransformClauseContext)_localctx).kind = match(REDUCE);
				setState(1790);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
				case 1:
					{
					setState(1789);
					setQuantifier();
					}
					break;
				}
				setState(1792);
				expressionSeq();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1796);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROW) {
				{
				setState(1795);
				((TransformClauseContext)_localctx).inRowFormat = rowFormat();
				}
			}

			setState(1800);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECORDWRITER) {
				{
				setState(1798);
				match(RECORDWRITER);
				setState(1799);
				((TransformClauseContext)_localctx).recordWriter = match(STRING);
				}
			}

			setState(1802);
			match(USING);
			setState(1803);
			((TransformClauseContext)_localctx).script = match(STRING);
			setState(1816);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
			case 1:
				{
				setState(1804);
				match(AS);
				setState(1814);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,217,_ctx) ) {
				case 1:
					{
					setState(1805);
					identifierSeq();
					}
					break;
				case 2:
					{
					setState(1806);
					colTypeList();
					}
					break;
				case 3:
					{
					{
					setState(1807);
					match(T__1);
					setState(1810);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,216,_ctx) ) {
					case 1:
						{
						setState(1808);
						identifierSeq();
						}
						break;
					case 2:
						{
						setState(1809);
						colTypeList();
						}
						break;
					}
					setState(1812);
					match(T__2);
					}
					}
					break;
				}
				}
				break;
			}
			setState(1819);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,219,_ctx) ) {
			case 1:
				{
				setState(1818);
				((TransformClauseContext)_localctx).outRowFormat = rowFormat();
				}
				break;
			}
			setState(1823);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
			case 1:
				{
				setState(1821);
				match(RECORDREADER);
				setState(1822);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 102, RULE_selectClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1825);
			match(SELECT);
			setState(1829);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,221,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1826);
					((SelectClauseContext)_localctx).hint = hint();
					((SelectClauseContext)_localctx).hints.add(((SelectClauseContext)_localctx).hint);
					}
					} 
				}
				setState(1831);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,221,_ctx);
			}
			setState(1833);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
			case 1:
				{
				setState(1832);
				setQuantifier();
				}
				break;
			}
			setState(1835);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 104, RULE_setClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1837);
			match(SET);
			setState(1838);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 106, RULE_matchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1840);
			match(WHEN);
			setState(1841);
			match(MATCHED);
			setState(1844);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1842);
				match(AND);
				setState(1843);
				((MatchedClauseContext)_localctx).matchedCond = booleanExpression(0);
				}
			}

			setState(1846);
			match(THEN);
			setState(1847);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 108, RULE_notMatchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1849);
			match(WHEN);
			setState(1850);
			match(NOT);
			setState(1851);
			match(MATCHED);
			setState(1854);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1852);
				match(AND);
				setState(1853);
				((NotMatchedClauseContext)_localctx).notMatchedCond = booleanExpression(0);
				}
			}

			setState(1856);
			match(THEN);
			setState(1857);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 110, RULE_matchedAction);
		try {
			setState(1866);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,225,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1859);
				match(DELETE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1860);
				match(UPDATE);
				setState(1861);
				match(SET);
				setState(1862);
				match(ASTERISK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1863);
				match(UPDATE);
				setState(1864);
				match(SET);
				setState(1865);
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

	@SuppressWarnings("CheckReturnValue")
	public static class NotMatchedActionContext extends ParserRuleContext {
		public MultipartIdentifierListContext columns;
		public TerminalNode INSERT() { return getToken(ArcticSqlCommandParser.INSERT, 0); }
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
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
		enterRule(_localctx, 112, RULE_notMatchedAction);
		int _la;
		try {
			setState(1886);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,227,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1868);
				match(INSERT);
				setState(1869);
				match(ASTERISK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1870);
				match(INSERT);
				setState(1871);
				match(T__1);
				setState(1872);
				((NotMatchedActionContext)_localctx).columns = multipartIdentifierList();
				setState(1873);
				match(T__2);
				setState(1874);
				match(VALUES);
				setState(1875);
				match(T__1);
				setState(1876);
				expression();
				setState(1881);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(1877);
					match(T__3);
					setState(1878);
					expression();
					}
					}
					setState(1883);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1884);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentListContext extends ParserRuleContext {
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
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
		enterRule(_localctx, 114, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1888);
			assignment();
			setState(1893);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(1889);
				match(T__3);
				setState(1890);
				assignment();
				}
				}
				setState(1895);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 116, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1896);
			((AssignmentContext)_localctx).key = multipartIdentifier();
			setState(1897);
			match(EQ);
			setState(1898);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 118, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1900);
			match(WHERE);
			setState(1901);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 120, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1903);
			match(HAVING);
			setState(1904);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HintContext extends ParserRuleContext {
		public HintStatementContext hintStatement;
		public List<HintStatementContext> hintStatements = new ArrayList<HintStatementContext>();
		public List<HintStatementContext> hintStatement() {
			return getRuleContexts(HintStatementContext.class);
		}
		public HintStatementContext hintStatement(int i) {
			return getRuleContext(HintStatementContext.class,i);
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
		enterRule(_localctx, 122, RULE_hint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1906);
			match(T__5);
			setState(1907);
			((HintContext)_localctx).hintStatement = hintStatement();
			((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
			setState(1914);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,230,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1909);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
					case 1:
						{
						setState(1908);
						match(T__3);
						}
						break;
					}
					setState(1911);
					((HintContext)_localctx).hintStatement = hintStatement();
					((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
					}
					} 
				}
				setState(1916);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,230,_ctx);
			}
			setState(1917);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HintStatementContext extends ParserRuleContext {
		public IdentifierContext hintName;
		public PrimaryExpressionContext primaryExpression;
		public List<PrimaryExpressionContext> parameters = new ArrayList<PrimaryExpressionContext>();
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
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
		enterRule(_localctx, 124, RULE_hintStatement);
		int _la;
		try {
			setState(1932);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1919);
				((HintStatementContext)_localctx).hintName = identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1920);
				((HintStatementContext)_localctx).hintName = identifier();
				setState(1921);
				match(T__1);
				setState(1922);
				((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
				((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
				setState(1927);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(1923);
					match(T__3);
					setState(1924);
					((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
					((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
					}
					}
					setState(1929);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1930);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
		public List<RelationContext> relation() {
			return getRuleContexts(RelationContext.class);
		}
		public RelationContext relation(int i) {
			return getRuleContext(RelationContext.class,i);
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
		enterRule(_localctx, 126, RULE_fromClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1934);
			match(FROM);
			setState(1935);
			relation();
			setState(1940);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1936);
					match(T__3);
					setState(1937);
					relation();
					}
					} 
				}
				setState(1942);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			}
			setState(1946);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,234,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1943);
					lateralView();
					}
					} 
				}
				setState(1948);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,234,_ctx);
			}
			setState(1950);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				{
				setState(1949);
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

	@SuppressWarnings("CheckReturnValue")
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
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SETS() { return getToken(ArcticSqlCommandParser.SETS, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
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
		enterRule(_localctx, 128, RULE_aggregationClause);
		int _la;
		try {
			int _alt;
			setState(1991);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,240,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1952);
				match(GROUP);
				setState(1953);
				match(BY);
				setState(1954);
				((AggregationClauseContext)_localctx).groupByClause = groupByClause();
				((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
				setState(1959);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,236,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1955);
						match(T__3);
						setState(1956);
						((AggregationClauseContext)_localctx).groupByClause = groupByClause();
						((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
						}
						} 
					}
					setState(1961);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,236,_ctx);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1962);
				match(GROUP);
				setState(1963);
				match(BY);
				setState(1964);
				((AggregationClauseContext)_localctx).expression = expression();
				((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
				setState(1969);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,237,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1965);
						match(T__3);
						setState(1966);
						((AggregationClauseContext)_localctx).expression = expression();
						((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
						}
						} 
					}
					setState(1971);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,237,_ctx);
				}
				setState(1989);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,239,_ctx) ) {
				case 1:
					{
					setState(1972);
					match(WITH);
					setState(1973);
					((AggregationClauseContext)_localctx).kind = match(ROLLUP);
					}
					break;
				case 2:
					{
					setState(1974);
					match(WITH);
					setState(1975);
					((AggregationClauseContext)_localctx).kind = match(CUBE);
					}
					break;
				case 3:
					{
					setState(1976);
					((AggregationClauseContext)_localctx).kind = match(GROUPING);
					setState(1977);
					match(SETS);
					setState(1978);
					match(T__1);
					setState(1979);
					groupingSet();
					setState(1984);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(1980);
						match(T__3);
						setState(1981);
						groupingSet();
						}
						}
						setState(1986);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(1987);
					match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 130, RULE_groupByClause);
		try {
			setState(1995);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,241,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1993);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1994);
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

	@SuppressWarnings("CheckReturnValue")
	public static class GroupingAnalyticsContext extends ParserRuleContext {
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode ROLLUP() { return getToken(ArcticSqlCommandParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(ArcticSqlCommandParser.CUBE, 0); }
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
		enterRule(_localctx, 132, RULE_groupingAnalytics);
		int _la;
		try {
			setState(2022);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CUBE:
			case ROLLUP:
				enterOuterAlt(_localctx, 1);
				{
				setState(1997);
				_la = _input.LA(1);
				if ( !(_la==CUBE || _la==ROLLUP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1998);
				match(T__1);
				setState(1999);
				groupingSet();
				setState(2004);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2000);
					match(T__3);
					setState(2001);
					groupingSet();
					}
					}
					setState(2006);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2007);
				match(T__2);
				}
				break;
			case GROUPING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2009);
				match(GROUPING);
				setState(2010);
				match(SETS);
				setState(2011);
				match(T__1);
				setState(2012);
				groupingElement();
				setState(2017);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2013);
					match(T__3);
					setState(2014);
					groupingElement();
					}
					}
					setState(2019);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2020);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 134, RULE_groupingElement);
		try {
			setState(2026);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,245,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2024);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2025);
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

	@SuppressWarnings("CheckReturnValue")
	public static class GroupingSetContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
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
		enterRule(_localctx, 136, RULE_groupingSet);
		int _la;
		try {
			setState(2041);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,248,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2028);
				match(T__1);
				setState(2037);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,247,_ctx) ) {
				case 1:
					{
					setState(2029);
					expression();
					setState(2034);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(2030);
						match(T__3);
						setState(2031);
						expression();
						}
						}
						setState(2036);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2039);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2040);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PivotClauseContext extends ParserRuleContext {
		public NamedExpressionSeqContext aggregates;
		public PivotValueContext pivotValue;
		public List<PivotValueContext> pivotValues = new ArrayList<PivotValueContext>();
		public TerminalNode PIVOT() { return getToken(ArcticSqlCommandParser.PIVOT, 0); }
		public TerminalNode FOR() { return getToken(ArcticSqlCommandParser.FOR, 0); }
		public PivotColumnContext pivotColumn() {
			return getRuleContext(PivotColumnContext.class,0);
		}
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public NamedExpressionSeqContext namedExpressionSeq() {
			return getRuleContext(NamedExpressionSeqContext.class,0);
		}
		public List<PivotValueContext> pivotValue() {
			return getRuleContexts(PivotValueContext.class);
		}
		public PivotValueContext pivotValue(int i) {
			return getRuleContext(PivotValueContext.class,i);
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
		enterRule(_localctx, 138, RULE_pivotClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2043);
			match(PIVOT);
			setState(2044);
			match(T__1);
			setState(2045);
			((PivotClauseContext)_localctx).aggregates = namedExpressionSeq();
			setState(2046);
			match(FOR);
			setState(2047);
			pivotColumn();
			setState(2048);
			match(IN);
			setState(2049);
			match(T__1);
			setState(2050);
			((PivotClauseContext)_localctx).pivotValue = pivotValue();
			((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
			setState(2055);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2051);
				match(T__3);
				setState(2052);
				((PivotClauseContext)_localctx).pivotValue = pivotValue();
				((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
				}
				}
				setState(2057);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2058);
			match(T__2);
			setState(2059);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PivotColumnContext extends ParserRuleContext {
		public IdentifierContext identifier;
		public List<IdentifierContext> identifiers = new ArrayList<IdentifierContext>();
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
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
		enterRule(_localctx, 140, RULE_pivotColumn);
		int _la;
		try {
			setState(2073);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,251,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2061);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2062);
				match(T__1);
				setState(2063);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				setState(2068);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2064);
					match(T__3);
					setState(2065);
					((PivotColumnContext)_localctx).identifier = identifier();
					((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
					}
					}
					setState(2070);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2071);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 142, RULE_pivotValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2075);
			expression();
			setState(2080);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,253,_ctx) ) {
			case 1:
				{
				setState(2077);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,252,_ctx) ) {
				case 1:
					{
					setState(2076);
					match(AS);
					}
					break;
				}
				setState(2079);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LateralViewContext extends ParserRuleContext {
		public IdentifierContext tblName;
		public IdentifierContext identifier;
		public List<IdentifierContext> colName = new ArrayList<IdentifierContext>();
		public TerminalNode LATERAL() { return getToken(ArcticSqlCommandParser.LATERAL, 0); }
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
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
		enterRule(_localctx, 144, RULE_lateralView);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2082);
			match(LATERAL);
			setState(2083);
			match(VIEW);
			setState(2085);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
			case 1:
				{
				setState(2084);
				match(OUTER);
				}
				break;
			}
			setState(2087);
			qualifiedName();
			setState(2088);
			match(T__1);
			setState(2097);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,256,_ctx) ) {
			case 1:
				{
				setState(2089);
				expression();
				setState(2094);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2090);
					match(T__3);
					setState(2091);
					expression();
					}
					}
					setState(2096);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2099);
			match(T__2);
			setState(2100);
			((LateralViewContext)_localctx).tblName = identifier();
			setState(2112);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,259,_ctx) ) {
			case 1:
				{
				setState(2102);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,257,_ctx) ) {
				case 1:
					{
					setState(2101);
					match(AS);
					}
					break;
				}
				setState(2104);
				((LateralViewContext)_localctx).identifier = identifier();
				((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
				setState(2109);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,258,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2105);
						match(T__3);
						setState(2106);
						((LateralViewContext)_localctx).identifier = identifier();
						((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
						}
						} 
					}
					setState(2111);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,258,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 146, RULE_setQuantifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2114);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 148, RULE_relation);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2117);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,260,_ctx) ) {
			case 1:
				{
				setState(2116);
				match(LATERAL);
				}
				break;
			}
			setState(2119);
			relationPrimary();
			setState(2123);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,261,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2120);
					joinRelation();
					}
					} 
				}
				setState(2125);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,261,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 150, RULE_joinRelation);
		try {
			setState(2143);
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
				setState(2126);
				joinType();
				}
				setState(2127);
				match(JOIN);
				setState(2129);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,262,_ctx) ) {
				case 1:
					{
					setState(2128);
					match(LATERAL);
					}
					break;
				}
				setState(2131);
				((JoinRelationContext)_localctx).right = relationPrimary();
				setState(2133);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,263,_ctx) ) {
				case 1:
					{
					setState(2132);
					joinCriteria();
					}
					break;
				}
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2135);
				match(NATURAL);
				setState(2136);
				joinType();
				setState(2137);
				match(JOIN);
				setState(2139);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
				case 1:
					{
					setState(2138);
					match(LATERAL);
					}
					break;
				}
				setState(2141);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 152, RULE_joinType);
		int _la;
		try {
			setState(2169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,272,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2146);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(2145);
					match(INNER);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2148);
				match(CROSS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2149);
				match(LEFT);
				setState(2151);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2150);
					match(OUTER);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2154);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2153);
					match(LEFT);
					}
				}

				setState(2156);
				match(SEMI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2157);
				match(RIGHT);
				setState(2159);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2158);
					match(OUTER);
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2161);
				match(FULL);
				setState(2163);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2162);
					match(OUTER);
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2166);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2165);
					match(LEFT);
					}
				}

				setState(2168);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 154, RULE_joinCriteria);
		try {
			setState(2175);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(2171);
				match(ON);
				setState(2172);
				booleanExpression(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2173);
				match(USING);
				setState(2174);
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

	@SuppressWarnings("CheckReturnValue")
	public static class SampleContext extends ParserRuleContext {
		public TerminalNode TABLESAMPLE() { return getToken(ArcticSqlCommandParser.TABLESAMPLE, 0); }
		public SampleMethodContext sampleMethod() {
			return getRuleContext(SampleMethodContext.class,0);
		}
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
		enterRule(_localctx, 156, RULE_sample);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2177);
			match(TABLESAMPLE);
			setState(2178);
			match(T__1);
			setState(2180);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,274,_ctx) ) {
			case 1:
				{
				setState(2179);
				sampleMethod();
				}
				break;
			}
			setState(2182);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 158, RULE_sampleMethod);
		int _la;
		try {
			setState(2208);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,278,_ctx) ) {
			case 1:
				_localctx = new SampleByPercentileContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2184);
					((SampleByPercentileContext)_localctx).negativeSign = match(MINUS);
					}
				}

				setState(2187);
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
				setState(2188);
				match(PERCENTLIT);
				}
				break;
			case 2:
				_localctx = new SampleByRowsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2189);
				expression();
				setState(2190);
				match(ROWS);
				}
				break;
			case 3:
				_localctx = new SampleByBucketContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2192);
				((SampleByBucketContext)_localctx).sampleType = match(BUCKET);
				setState(2193);
				((SampleByBucketContext)_localctx).numerator = match(INTEGER_VALUE);
				setState(2194);
				match(OUT);
				setState(2195);
				match(OF);
				setState(2196);
				((SampleByBucketContext)_localctx).denominator = match(INTEGER_VALUE);
				setState(2205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(2197);
					match(ON);
					setState(2203);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,276,_ctx) ) {
					case 1:
						{
						setState(2198);
						identifier();
						}
						break;
					case 2:
						{
						setState(2199);
						qualifiedName();
						setState(2200);
						match(T__1);
						setState(2201);
						match(T__2);
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
				setState(2207);
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

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierListContext extends ParserRuleContext {
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
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
		enterRule(_localctx, 160, RULE_identifierList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2210);
			match(T__1);
			setState(2211);
			identifierSeq();
			setState(2212);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierSeqContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext errorCapturingIdentifier;
		public List<ErrorCapturingIdentifierContext> ident = new ArrayList<ErrorCapturingIdentifierContext>();
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
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
		enterRule(_localctx, 162, RULE_identifierSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2214);
			((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
			setState(2219);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,279,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2215);
					match(T__3);
					setState(2216);
					((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2221);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,279,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class OrderedIdentifierListContext extends ParserRuleContext {
		public List<OrderedIdentifierContext> orderedIdentifier() {
			return getRuleContexts(OrderedIdentifierContext.class);
		}
		public OrderedIdentifierContext orderedIdentifier(int i) {
			return getRuleContext(OrderedIdentifierContext.class,i);
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
		enterRule(_localctx, 164, RULE_orderedIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2222);
			match(T__1);
			setState(2223);
			orderedIdentifier();
			setState(2228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2224);
				match(T__3);
				setState(2225);
				orderedIdentifier();
				}
				}
				setState(2230);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2231);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 166, RULE_orderedIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2233);
			((OrderedIdentifierContext)_localctx).ident = errorCapturingIdentifier();
			setState(2235);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(2234);
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

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierCommentListContext extends ParserRuleContext {
		public List<IdentifierCommentContext> identifierComment() {
			return getRuleContexts(IdentifierCommentContext.class);
		}
		public IdentifierCommentContext identifierComment(int i) {
			return getRuleContext(IdentifierCommentContext.class,i);
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
		enterRule(_localctx, 168, RULE_identifierCommentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2237);
			match(T__1);
			setState(2238);
			identifierComment();
			setState(2243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2239);
				match(T__3);
				setState(2240);
				identifierComment();
				}
				}
				setState(2245);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2246);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 170, RULE_identifierComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2248);
			identifier();
			setState(2250);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2249);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class AliasedRelationContext extends RelationPrimaryContext {
		public RelationContext relation() {
			return getRuleContext(RelationContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class AliasedQueryContext extends RelationPrimaryContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class TableNameContext extends RelationPrimaryContext {
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TableAliasContext tableAlias() {
			return getRuleContext(TableAliasContext.class,0);
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
		enterRule(_localctx, 172, RULE_relationPrimary);
		try {
			setState(2276);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,287,_ctx) ) {
			case 1:
				_localctx = new TableNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2252);
				multipartIdentifier();
				setState(2254);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,284,_ctx) ) {
				case 1:
					{
					setState(2253);
					sample();
					}
					break;
				}
				setState(2256);
				tableAlias();
				}
				break;
			case 2:
				_localctx = new AliasedQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2258);
				match(T__1);
				setState(2259);
				query();
				setState(2260);
				match(T__2);
				setState(2262);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,285,_ctx) ) {
				case 1:
					{
					setState(2261);
					sample();
					}
					break;
				}
				setState(2264);
				tableAlias();
				}
				break;
			case 3:
				_localctx = new AliasedRelationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2266);
				match(T__1);
				setState(2267);
				relation();
				setState(2268);
				match(T__2);
				setState(2270);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,286,_ctx) ) {
				case 1:
					{
					setState(2269);
					sample();
					}
					break;
				}
				setState(2272);
				tableAlias();
				}
				break;
			case 4:
				_localctx = new InlineTableDefault2Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2274);
				inlineTable();
				}
				break;
			case 5:
				_localctx = new TableValuedFunctionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2275);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 174, RULE_inlineTable);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2278);
			match(VALUES);
			setState(2279);
			expression();
			setState(2284);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,288,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2280);
					match(T__3);
					setState(2281);
					expression();
					}
					} 
				}
				setState(2286);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,288,_ctx);
			}
			setState(2287);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionTableContext extends ParserRuleContext {
		public FunctionNameContext funcName;
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
		enterRule(_localctx, 176, RULE_functionTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2289);
			((FunctionTableContext)_localctx).funcName = functionName();
			setState(2290);
			match(T__1);
			setState(2299);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
			case 1:
				{
				setState(2291);
				expression();
				setState(2296);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2292);
					match(T__3);
					setState(2293);
					expression();
					}
					}
					setState(2298);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2301);
			match(T__2);
			setState(2302);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 178, RULE_tableAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2311);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,293,_ctx) ) {
			case 1:
				{
				setState(2305);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,291,_ctx) ) {
				case 1:
					{
					setState(2304);
					match(AS);
					}
					break;
				}
				setState(2307);
				strictIdentifier();
				setState(2309);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,292,_ctx) ) {
				case 1:
					{
					setState(2308);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class RowFormatSerdeContext extends RowFormatContext {
		public Token name;
		public TablePropertyListContext props;
		public TerminalNode ROW() { return getToken(ArcticSqlCommandParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticSqlCommandParser.FORMAT, 0); }
		public TerminalNode SERDE() { return getToken(ArcticSqlCommandParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(ArcticSqlCommandParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticSqlCommandParser.SERDEPROPERTIES, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 180, RULE_rowFormat);
		try {
			setState(2362);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,301,_ctx) ) {
			case 1:
				_localctx = new RowFormatSerdeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2313);
				match(ROW);
				setState(2314);
				match(FORMAT);
				setState(2315);
				match(SERDE);
				setState(2316);
				((RowFormatSerdeContext)_localctx).name = match(STRING);
				setState(2320);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,294,_ctx) ) {
				case 1:
					{
					setState(2317);
					match(WITH);
					setState(2318);
					match(SERDEPROPERTIES);
					setState(2319);
					((RowFormatSerdeContext)_localctx).props = tablePropertyList();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new RowFormatDelimitedContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2322);
				match(ROW);
				setState(2323);
				match(FORMAT);
				setState(2324);
				match(DELIMITED);
				setState(2334);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,296,_ctx) ) {
				case 1:
					{
					setState(2325);
					match(FIELDS);
					setState(2326);
					match(TERMINATED);
					setState(2327);
					match(BY);
					setState(2328);
					((RowFormatDelimitedContext)_localctx).fieldsTerminatedBy = match(STRING);
					setState(2332);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,295,_ctx) ) {
					case 1:
						{
						setState(2329);
						match(ESCAPED);
						setState(2330);
						match(BY);
						setState(2331);
						((RowFormatDelimitedContext)_localctx).escapedBy = match(STRING);
						}
						break;
					}
					}
					break;
				}
				setState(2341);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,297,_ctx) ) {
				case 1:
					{
					setState(2336);
					match(COLLECTION);
					setState(2337);
					match(ITEMS);
					setState(2338);
					match(TERMINATED);
					setState(2339);
					match(BY);
					setState(2340);
					((RowFormatDelimitedContext)_localctx).collectionItemsTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2348);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,298,_ctx) ) {
				case 1:
					{
					setState(2343);
					match(MAP);
					setState(2344);
					match(KEYS);
					setState(2345);
					match(TERMINATED);
					setState(2346);
					match(BY);
					setState(2347);
					((RowFormatDelimitedContext)_localctx).keysTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2354);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,299,_ctx) ) {
				case 1:
					{
					setState(2350);
					match(LINES);
					setState(2351);
					match(TERMINATED);
					setState(2352);
					match(BY);
					setState(2353);
					((RowFormatDelimitedContext)_localctx).linesSeparatedBy = match(STRING);
					}
					break;
				}
				setState(2360);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,300,_ctx) ) {
				case 1:
					{
					setState(2356);
					match(NULL);
					setState(2357);
					match(DEFINED);
					setState(2358);
					match(AS);
					setState(2359);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MultipartIdentifierListContext extends ParserRuleContext {
		public List<MultipartIdentifierContext> multipartIdentifier() {
			return getRuleContexts(MultipartIdentifierContext.class);
		}
		public MultipartIdentifierContext multipartIdentifier(int i) {
			return getRuleContext(MultipartIdentifierContext.class,i);
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
		enterRule(_localctx, 182, RULE_multipartIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2364);
			multipartIdentifier();
			setState(2369);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2365);
				match(T__3);
				setState(2366);
				multipartIdentifier();
				}
				}
				setState(2371);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MultipartIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext errorCapturingIdentifier;
		public List<ErrorCapturingIdentifierContext> parts = new ArrayList<ErrorCapturingIdentifierContext>();
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
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
		enterRule(_localctx, 184, RULE_multipartIdentifier);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2372);
			((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
			setState(2377);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,303,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2373);
					match(T__4);
					setState(2374);
					((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2379);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,303,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TableIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext db;
		public ErrorCapturingIdentifierContext table;
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
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
		enterRule(_localctx, 186, RULE_tableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2383);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,304,_ctx) ) {
			case 1:
				{
				setState(2380);
				((TableIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2381);
				match(T__4);
				}
				break;
			}
			setState(2385);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext db;
		public ErrorCapturingIdentifierContext function;
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
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
		enterRule(_localctx, 188, RULE_functionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2390);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,305,_ctx) ) {
			case 1:
				{
				setState(2387);
				((FunctionIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2388);
				match(T__4);
				}
				break;
			}
			setState(2392);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 190, RULE_namedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2394);
			expression();
			setState(2402);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,308,_ctx) ) {
			case 1:
				{
				setState(2396);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,306,_ctx) ) {
				case 1:
					{
					setState(2395);
					match(AS);
					}
					break;
				}
				setState(2400);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,307,_ctx) ) {
				case 1:
					{
					setState(2398);
					((NamedExpressionContext)_localctx).name = errorCapturingIdentifier();
					}
					break;
				case 2:
					{
					setState(2399);
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

	@SuppressWarnings("CheckReturnValue")
	public static class NamedExpressionSeqContext extends ParserRuleContext {
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
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
		enterRule(_localctx, 192, RULE_namedExpressionSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2404);
			namedExpression();
			setState(2409);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,309,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2405);
					match(T__3);
					setState(2406);
					namedExpression();
					}
					} 
				}
				setState(2411);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,309,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PartitionFieldListContext extends ParserRuleContext {
		public PartitionFieldContext partitionField;
		public List<PartitionFieldContext> fields = new ArrayList<PartitionFieldContext>();
		public List<PartitionFieldContext> partitionField() {
			return getRuleContexts(PartitionFieldContext.class);
		}
		public PartitionFieldContext partitionField(int i) {
			return getRuleContext(PartitionFieldContext.class,i);
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
		enterRule(_localctx, 194, RULE_partitionFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2412);
			match(T__1);
			setState(2413);
			((PartitionFieldListContext)_localctx).partitionField = partitionField();
			((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
			setState(2418);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2414);
				match(T__3);
				setState(2415);
				((PartitionFieldListContext)_localctx).partitionField = partitionField();
				((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
				}
				}
				setState(2420);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2421);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 196, RULE_partitionField);
		try {
			setState(2425);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,311,_ctx) ) {
			case 1:
				_localctx = new PartitionTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2423);
				transform();
				}
				break;
			case 2:
				_localctx = new PartitionColumnContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2424);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class ApplyTransformContext extends TransformContext {
		public IdentifierContext transformName;
		public TransformArgumentContext transformArgument;
		public List<TransformArgumentContext> argument = new ArrayList<TransformArgumentContext>();
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TransformArgumentContext> transformArgument() {
			return getRuleContexts(TransformArgumentContext.class);
		}
		public TransformArgumentContext transformArgument(int i) {
			return getRuleContext(TransformArgumentContext.class,i);
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
		enterRule(_localctx, 198, RULE_transform);
		int _la;
		try {
			setState(2440);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,313,_ctx) ) {
			case 1:
				_localctx = new IdentityTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2427);
				qualifiedName();
				}
				break;
			case 2:
				_localctx = new ApplyTransformContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2428);
				((ApplyTransformContext)_localctx).transformName = identifier();
				setState(2429);
				match(T__1);
				setState(2430);
				((ApplyTransformContext)_localctx).transformArgument = transformArgument();
				((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
				setState(2435);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2431);
					match(T__3);
					setState(2432);
					((ApplyTransformContext)_localctx).transformArgument = transformArgument();
					((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
					}
					}
					setState(2437);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2438);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 200, RULE_transformArgument);
		try {
			setState(2444);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,314,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2442);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2443);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 202, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2446);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionSeqContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
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
		enterRule(_localctx, 204, RULE_expressionSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2448);
			expression();
			setState(2453);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2449);
				match(T__3);
				setState(2450);
				expression();
				}
				}
				setState(2455);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class ExistsContext extends BooleanExpressionContext {
		public TerminalNode EXISTS() { return getToken(ArcticSqlCommandParser.EXISTS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
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
		int _startState = 206;
		enterRecursionRule(_localctx, 206, RULE_booleanExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2468);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,317,_ctx) ) {
			case 1:
				{
				_localctx = new LogicalNotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2457);
				match(NOT);
				setState(2458);
				booleanExpression(5);
				}
				break;
			case 2:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2459);
				match(EXISTS);
				setState(2460);
				match(T__1);
				setState(2461);
				query();
				setState(2462);
				match(T__2);
				}
				break;
			case 3:
				{
				_localctx = new PredicatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2464);
				valueExpression(0);
				setState(2466);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,316,_ctx) ) {
				case 1:
					{
					setState(2465);
					predicate();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2478);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,319,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2476);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,318,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2470);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2471);
						((LogicalBinaryContext)_localctx).operator = match(AND);
						setState(2472);
						((LogicalBinaryContext)_localctx).right = booleanExpression(3);
						}
						break;
					case 2:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2473);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2474);
						((LogicalBinaryContext)_localctx).operator = match(OR);
						setState(2475);
						((LogicalBinaryContext)_localctx).right = booleanExpression(2);
						}
						break;
					}
					} 
				}
				setState(2480);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,319,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RLIKE() { return getToken(ArcticSqlCommandParser.RLIKE, 0); }
		public TerminalNode LIKE() { return getToken(ArcticSqlCommandParser.LIKE, 0); }
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
		enterRule(_localctx, 208, RULE_predicate);
		int _la;
		try {
			setState(2563);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,333,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2482);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2481);
					match(NOT);
					}
				}

				setState(2484);
				((PredicateContext)_localctx).kind = match(BETWEEN);
				setState(2485);
				((PredicateContext)_localctx).lower = valueExpression(0);
				setState(2486);
				match(AND);
				setState(2487);
				((PredicateContext)_localctx).upper = valueExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2490);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2489);
					match(NOT);
					}
				}

				setState(2492);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2493);
				match(T__1);
				setState(2494);
				expression();
				setState(2499);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(2495);
					match(T__3);
					setState(2496);
					expression();
					}
					}
					setState(2501);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2502);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2505);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2504);
					match(NOT);
					}
				}

				setState(2507);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2508);
				match(T__1);
				setState(2509);
				query();
				setState(2510);
				match(T__2);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2513);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2512);
					match(NOT);
					}
				}

				setState(2515);
				((PredicateContext)_localctx).kind = match(RLIKE);
				setState(2516);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2518);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2517);
					match(NOT);
					}
				}

				setState(2520);
				((PredicateContext)_localctx).kind = match(LIKE);
				setState(2521);
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
				setState(2535);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,327,_ctx) ) {
				case 1:
					{
					setState(2522);
					match(T__1);
					setState(2523);
					match(T__2);
					}
					break;
				case 2:
					{
					setState(2524);
					match(T__1);
					setState(2525);
					expression();
					setState(2530);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(2526);
						match(T__3);
						setState(2527);
						expression();
						}
						}
						setState(2532);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2533);
					match(T__2);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2538);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2537);
					match(NOT);
					}
				}

				setState(2540);
				((PredicateContext)_localctx).kind = match(LIKE);
				setState(2541);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				setState(2544);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,329,_ctx) ) {
				case 1:
					{
					setState(2542);
					match(ESCAPE);
					setState(2543);
					((PredicateContext)_localctx).escapeChar = match(STRING);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2546);
				match(IS);
				setState(2548);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2547);
					match(NOT);
					}
				}

				setState(2550);
				((PredicateContext)_localctx).kind = match(NULL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2551);
				match(IS);
				setState(2553);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2552);
					match(NOT);
					}
				}

				setState(2555);
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
				setState(2556);
				match(IS);
				setState(2558);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2557);
					match(NOT);
					}
				}

				setState(2560);
				((PredicateContext)_localctx).kind = match(DISTINCT);
				setState(2561);
				match(FROM);
				setState(2562);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		int _startState = 210;
		enterRecursionRule(_localctx, 210, RULE_valueExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2569);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,334,_ctx) ) {
			case 1:
				{
				_localctx = new ValueExpressionDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2566);
				primaryExpression(0);
				}
				break;
			case 2:
				{
				_localctx = new ArithmeticUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2567);
				((ArithmeticUnaryContext)_localctx).operator = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 284)) & ~0x3f) == 0 && ((1L << (_la - 284)) & 35L) != 0)) ) {
					((ArithmeticUnaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2568);
				valueExpression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2592);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,336,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2590);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,335,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2571);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2572);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==DIV || ((((_la - 286)) & ~0x3f) == 0 && ((1L << (_la - 286)) & 7L) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2573);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(7);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2574);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(2575);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 284)) & ~0x3f) == 0 && ((1L << (_la - 284)) & 259L) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2576);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(6);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2577);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(2578);
						((ArithmeticBinaryContext)_localctx).operator = match(AMPERSAND);
						setState(2579);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(5);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2580);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(2581);
						((ArithmeticBinaryContext)_localctx).operator = match(HAT);
						setState(2582);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(4);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2583);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2584);
						((ArithmeticBinaryContext)_localctx).operator = match(PIPE);
						setState(2585);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(3);
						}
						break;
					case 6:
						{
						_localctx = new ComparisonContext(new ValueExpressionContext(_parentctx, _parentState));
						((ComparisonContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2586);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2587);
						comparisonOperator();
						setState(2588);
						((ComparisonContext)_localctx).right = valueExpression(2);
						}
						break;
					}
					} 
				}
				setState(2594);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class StructContext extends PrimaryExpressionContext {
		public NamedExpressionContext namedExpression;
		public List<NamedExpressionContext> argument = new ArrayList<NamedExpressionContext>();
		public TerminalNode STRUCT() { return getToken(ArcticSqlCommandParser.STRUCT, 0); }
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
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
	@SuppressWarnings("CheckReturnValue")
	public static class DereferenceContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext base;
		public IdentifierContext fieldName;
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class RowConstructorContext extends PrimaryExpressionContext {
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
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
	@SuppressWarnings("CheckReturnValue")
	public static class LastContext extends PrimaryExpressionContext {
		public TerminalNode LAST() { return getToken(ArcticSqlCommandParser.LAST, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class StarContext extends PrimaryExpressionContext {
		public TerminalNode ASTERISK() { return getToken(ArcticSqlCommandParser.ASTERISK, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class OverlayContext extends PrimaryExpressionContext {
		public ValueExpressionContext input;
		public ValueExpressionContext replace;
		public ValueExpressionContext position;
		public ValueExpressionContext length;
		public TerminalNode OVERLAY() { return getToken(ArcticSqlCommandParser.OVERLAY, 0); }
		public TerminalNode PLACING() { return getToken(ArcticSqlCommandParser.PLACING, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
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
	@SuppressWarnings("CheckReturnValue")
	public static class SubscriptContext extends PrimaryExpressionContext {
		public PrimaryExpressionContext value;
		public ValueExpressionContext index;
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
	@SuppressWarnings("CheckReturnValue")
	public static class SubqueryExpressionContext extends PrimaryExpressionContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class SubstringContext extends PrimaryExpressionContext {
		public ValueExpressionContext str;
		public ValueExpressionContext pos;
		public ValueExpressionContext len;
		public TerminalNode SUBSTR() { return getToken(ArcticSqlCommandParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticSqlCommandParser.SUBSTRING, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
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
	@SuppressWarnings("CheckReturnValue")
	public static class CastContext extends PrimaryExpressionContext {
		public Token name;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticSqlCommandParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class LambdaContext extends PrimaryExpressionContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
	public static class ParenthesizedExpressionContext extends PrimaryExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class ExtractContext extends PrimaryExpressionContext {
		public IdentifierContext field;
		public ValueExpressionContext source;
		public TerminalNode EXTRACT() { return getToken(ArcticSqlCommandParser.EXTRACT, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
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
	@SuppressWarnings("CheckReturnValue")
	public static class TrimContext extends PrimaryExpressionContext {
		public Token trimOption;
		public ValueExpressionContext trimStr;
		public ValueExpressionContext srcStr;
		public TerminalNode TRIM() { return getToken(ArcticSqlCommandParser.TRIM, 0); }
		public TerminalNode FROM() { return getToken(ArcticSqlCommandParser.FROM, 0); }
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
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends PrimaryExpressionContext {
		public ExpressionContext expression;
		public List<ExpressionContext> argument = new ArrayList<ExpressionContext>();
		public BooleanExpressionContext where;
		public Token nullsOption;
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class PositionContext extends PrimaryExpressionContext {
		public ValueExpressionContext substr;
		public ValueExpressionContext str;
		public TerminalNode POSITION() { return getToken(ArcticSqlCommandParser.POSITION, 0); }
		public TerminalNode IN() { return getToken(ArcticSqlCommandParser.IN, 0); }
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
	@SuppressWarnings("CheckReturnValue")
	public static class FirstContext extends PrimaryExpressionContext {
		public TerminalNode FIRST() { return getToken(ArcticSqlCommandParser.FIRST, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
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
		int _startState = 212;
		enterRecursionRule(_localctx, 212, RULE_primaryExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2783);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,357,_ctx) ) {
			case 1:
				{
				_localctx = new CurrentLikeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2596);
				((CurrentLikeContext)_localctx).name = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 13L) != 0)) ) {
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
				_localctx = new SearchedCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2597);
				match(CASE);
				setState(2599); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2598);
					whenClause();
					}
					}
					setState(2601); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2605);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2603);
					match(ELSE);
					setState(2604);
					((SearchedCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2607);
				match(END);
				}
				break;
			case 3:
				{
				_localctx = new SimpleCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2609);
				match(CASE);
				setState(2610);
				((SimpleCaseContext)_localctx).value = expression();
				setState(2612); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2611);
					whenClause();
					}
					}
					setState(2614); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2618);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2616);
					match(ELSE);
					setState(2617);
					((SimpleCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2620);
				match(END);
				}
				break;
			case 4:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2622);
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
				setState(2623);
				match(T__1);
				setState(2624);
				expression();
				setState(2625);
				match(AS);
				setState(2626);
				dataType();
				setState(2627);
				match(T__2);
				}
				break;
			case 5:
				{
				_localctx = new StructContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2629);
				match(STRUCT);
				setState(2630);
				match(T__1);
				setState(2639);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,342,_ctx) ) {
				case 1:
					{
					setState(2631);
					((StructContext)_localctx).namedExpression = namedExpression();
					((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
					setState(2636);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(2632);
						match(T__3);
						setState(2633);
						((StructContext)_localctx).namedExpression = namedExpression();
						((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
						}
						}
						setState(2638);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2641);
				match(T__2);
				}
				break;
			case 6:
				{
				_localctx = new FirstContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2642);
				match(FIRST);
				setState(2643);
				match(T__1);
				setState(2644);
				expression();
				setState(2647);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2645);
					match(IGNORE);
					setState(2646);
					match(NULLS);
					}
				}

				setState(2649);
				match(T__2);
				}
				break;
			case 7:
				{
				_localctx = new LastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2651);
				match(LAST);
				setState(2652);
				match(T__1);
				setState(2653);
				expression();
				setState(2656);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2654);
					match(IGNORE);
					setState(2655);
					match(NULLS);
					}
				}

				setState(2658);
				match(T__2);
				}
				break;
			case 8:
				{
				_localctx = new PositionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2660);
				match(POSITION);
				setState(2661);
				match(T__1);
				setState(2662);
				((PositionContext)_localctx).substr = valueExpression(0);
				setState(2663);
				match(IN);
				setState(2664);
				((PositionContext)_localctx).str = valueExpression(0);
				setState(2665);
				match(T__2);
				}
				break;
			case 9:
				{
				_localctx = new ConstantDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2667);
				constant();
				}
				break;
			case 10:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2668);
				match(ASTERISK);
				}
				break;
			case 11:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2669);
				qualifiedName();
				setState(2670);
				match(T__4);
				setState(2671);
				match(ASTERISK);
				}
				break;
			case 12:
				{
				_localctx = new RowConstructorContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2673);
				match(T__1);
				setState(2674);
				namedExpression();
				setState(2677); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2675);
					match(T__3);
					setState(2676);
					namedExpression();
					}
					}
					setState(2679); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__3 );
				setState(2681);
				match(T__2);
				}
				break;
			case 13:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2683);
				match(T__1);
				setState(2684);
				query();
				setState(2685);
				match(T__2);
				}
				break;
			case 14:
				{
				_localctx = new FunctionCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2687);
				functionName();
				setState(2688);
				match(T__1);
				setState(2700);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,348,_ctx) ) {
				case 1:
					{
					setState(2690);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,346,_ctx) ) {
					case 1:
						{
						setState(2689);
						setQuantifier();
						}
						break;
					}
					setState(2692);
					((FunctionCallContext)_localctx).expression = expression();
					((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
					setState(2697);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(2693);
						match(T__3);
						setState(2694);
						((FunctionCallContext)_localctx).expression = expression();
						((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
						}
						}
						setState(2699);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2702);
				match(T__2);
				setState(2709);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,349,_ctx) ) {
				case 1:
					{
					setState(2703);
					match(FILTER);
					setState(2704);
					match(T__1);
					setState(2705);
					match(WHERE);
					setState(2706);
					((FunctionCallContext)_localctx).where = booleanExpression(0);
					setState(2707);
					match(T__2);
					}
					break;
				}
				setState(2713);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,350,_ctx) ) {
				case 1:
					{
					setState(2711);
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
					setState(2712);
					match(NULLS);
					}
					break;
				}
				setState(2717);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,351,_ctx) ) {
				case 1:
					{
					setState(2715);
					match(OVER);
					setState(2716);
					windowSpec();
					}
					break;
				}
				}
				break;
			case 15:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2719);
				identifier();
				setState(2720);
				match(T__7);
				setState(2721);
				expression();
				}
				break;
			case 16:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2723);
				match(T__1);
				setState(2724);
				identifier();
				setState(2727); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2725);
					match(T__3);
					setState(2726);
					identifier();
					}
					}
					setState(2729); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__3 );
				setState(2731);
				match(T__2);
				setState(2732);
				match(T__7);
				setState(2733);
				expression();
				}
				break;
			case 17:
				{
				_localctx = new ColumnReferenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2735);
				identifier();
				}
				break;
			case 18:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2736);
				match(T__1);
				setState(2737);
				expression();
				setState(2738);
				match(T__2);
				}
				break;
			case 19:
				{
				_localctx = new ExtractContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2740);
				match(EXTRACT);
				setState(2741);
				match(T__1);
				setState(2742);
				((ExtractContext)_localctx).field = identifier();
				setState(2743);
				match(FROM);
				setState(2744);
				((ExtractContext)_localctx).source = valueExpression(0);
				setState(2745);
				match(T__2);
				}
				break;
			case 20:
				{
				_localctx = new SubstringContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2747);
				_la = _input.LA(1);
				if ( !(_la==SUBSTR || _la==SUBSTRING) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2748);
				match(T__1);
				setState(2749);
				((SubstringContext)_localctx).str = valueExpression(0);
				setState(2750);
				_la = _input.LA(1);
				if ( !(_la==T__3 || _la==FROM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2751);
				((SubstringContext)_localctx).pos = valueExpression(0);
				setState(2754);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__3 || _la==FOR) {
					{
					setState(2752);
					_la = _input.LA(1);
					if ( !(_la==T__3 || _la==FOR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2753);
					((SubstringContext)_localctx).len = valueExpression(0);
					}
				}

				setState(2756);
				match(T__2);
				}
				break;
			case 21:
				{
				_localctx = new TrimContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2758);
				match(TRIM);
				setState(2759);
				match(T__1);
				setState(2761);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,354,_ctx) ) {
				case 1:
					{
					setState(2760);
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
				setState(2764);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,355,_ctx) ) {
				case 1:
					{
					setState(2763);
					((TrimContext)_localctx).trimStr = valueExpression(0);
					}
					break;
				}
				setState(2766);
				match(FROM);
				setState(2767);
				((TrimContext)_localctx).srcStr = valueExpression(0);
				setState(2768);
				match(T__2);
				}
				break;
			case 22:
				{
				_localctx = new OverlayContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2770);
				match(OVERLAY);
				setState(2771);
				match(T__1);
				setState(2772);
				((OverlayContext)_localctx).input = valueExpression(0);
				setState(2773);
				match(PLACING);
				setState(2774);
				((OverlayContext)_localctx).replace = valueExpression(0);
				setState(2775);
				match(FROM);
				setState(2776);
				((OverlayContext)_localctx).position = valueExpression(0);
				setState(2779);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2777);
					match(FOR);
					setState(2778);
					((OverlayContext)_localctx).length = valueExpression(0);
					}
				}

				setState(2781);
				match(T__2);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2795);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,359,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2793);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,358,_ctx) ) {
					case 1:
						{
						_localctx = new SubscriptContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((SubscriptContext)_localctx).value = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2785);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(2786);
						match(T__8);
						setState(2787);
						((SubscriptContext)_localctx).index = valueExpression(0);
						setState(2788);
						match(T__9);
						}
						break;
					case 2:
						{
						_localctx = new DereferenceContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((DereferenceContext)_localctx).base = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2790);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2791);
						match(T__4);
						setState(2792);
						((DereferenceContext)_localctx).fieldName = identifier();
						}
						break;
					}
					} 
				}
				setState(2797);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,359,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 214, RULE_constant);
		try {
			int _alt;
			setState(2810);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,361,_ctx) ) {
			case 1:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2798);
				match(NULL);
				}
				break;
			case 2:
				_localctx = new IntervalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2799);
				interval();
				}
				break;
			case 3:
				_localctx = new TypeConstructorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2800);
				identifier();
				setState(2801);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new NumericLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2803);
				number();
				}
				break;
			case 5:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2804);
				booleanValue();
				}
				break;
			case 6:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2806); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2805);
						match(STRING);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2808); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,360,_ctx);
				} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 216, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2812);
			_la = _input.LA(1);
			if ( !(((((_la - 276)) & ~0x3f) == 0 && ((1L << (_la - 276)) & 255L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 218, RULE_arithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2814);
			_la = _input.LA(1);
			if ( !(_la==DIV || ((((_la - 284)) & ~0x3f) == 0 && ((1L << (_la - 284)) & 1023L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 220, RULE_predicateOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2816);
			_la = _input.LA(1);
			if ( !(_la==AND || ((((_la - 123)) & ~0x3f) == 0 && ((1L << (_la - 123)) & 282574488338433L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 222, RULE_booleanValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2818);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 224, RULE_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2820);
			match(INTERVAL);
			setState(2823);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,362,_ctx) ) {
			case 1:
				{
				setState(2821);
				errorCapturingMultiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2822);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 226, RULE_errorCapturingMultiUnitsInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2825);
			((ErrorCapturingMultiUnitsIntervalContext)_localctx).body = multiUnitsInterval();
			setState(2827);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,363,_ctx) ) {
			case 1:
				{
				setState(2826);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 228, RULE_multiUnitsInterval);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2832); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2829);
					intervalValue();
					setState(2830);
					((MultiUnitsIntervalContext)_localctx).identifier = identifier();
					((MultiUnitsIntervalContext)_localctx).unit.add(((MultiUnitsIntervalContext)_localctx).identifier);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2834); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,364,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 230, RULE_errorCapturingUnitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2836);
			((ErrorCapturingUnitToUnitIntervalContext)_localctx).body = unitToUnitInterval();
			setState(2839);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,365,_ctx) ) {
			case 1:
				{
				setState(2837);
				((ErrorCapturingUnitToUnitIntervalContext)_localctx).error1 = multiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2838);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 232, RULE_unitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2841);
			((UnitToUnitIntervalContext)_localctx).value = intervalValue();
			setState(2842);
			((UnitToUnitIntervalContext)_localctx).from = identifier();
			setState(2843);
			match(TO);
			setState(2844);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 234, RULE_intervalValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2847);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(2846);
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

			setState(2849);
			_la = _input.LA(1);
			if ( !(((((_la - 294)) & ~0x3f) == 0 && ((1L << (_la - 294)) & 81L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 236, RULE_colPosition);
		try {
			setState(2854);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				enterOuterAlt(_localctx, 1);
				{
				setState(2851);
				((ColPositionContext)_localctx).position = match(FIRST);
				}
				break;
			case AFTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2852);
				((ColPositionContext)_localctx).position = match(AFTER);
				setState(2853);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveDataTypeContext extends DataTypeContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(ArcticSqlCommandParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(ArcticSqlCommandParser.INTEGER_VALUE, i);
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
		enterRule(_localctx, 238, RULE_dataType);
		int _la;
		try {
			setState(2902);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,374,_ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2856);
				((ComplexDataTypeContext)_localctx).complex = match(ARRAY);
				setState(2857);
				match(LT);
				setState(2858);
				dataType();
				setState(2859);
				match(GT);
				}
				break;
			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2861);
				((ComplexDataTypeContext)_localctx).complex = match(MAP);
				setState(2862);
				match(LT);
				setState(2863);
				dataType();
				setState(2864);
				match(T__3);
				setState(2865);
				dataType();
				setState(2866);
				match(GT);
				}
				break;
			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2868);
				((ComplexDataTypeContext)_localctx).complex = match(STRUCT);
				setState(2875);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LT:
					{
					setState(2869);
					match(LT);
					setState(2871);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,368,_ctx) ) {
					case 1:
						{
						setState(2870);
						complexColTypeList();
						}
						break;
					}
					setState(2873);
					match(GT);
					}
					break;
				case NEQ:
					{
					setState(2874);
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
				setState(2877);
				match(INTERVAL);
				setState(2878);
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
				setState(2881);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,370,_ctx) ) {
				case 1:
					{
					setState(2879);
					match(TO);
					setState(2880);
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
				setState(2883);
				match(INTERVAL);
				setState(2884);
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
				setState(2887);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,371,_ctx) ) {
				case 1:
					{
					setState(2885);
					match(TO);
					setState(2886);
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
				setState(2889);
				identifier();
				setState(2900);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,373,_ctx) ) {
				case 1:
					{
					setState(2890);
					match(T__1);
					setState(2891);
					match(INTEGER_VALUE);
					setState(2896);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(2892);
						match(T__3);
						setState(2893);
						match(INTEGER_VALUE);
						}
						}
						setState(2898);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2899);
					match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedColTypeWithPositionListContext extends ParserRuleContext {
		public List<QualifiedColTypeWithPositionContext> qualifiedColTypeWithPosition() {
			return getRuleContexts(QualifiedColTypeWithPositionContext.class);
		}
		public QualifiedColTypeWithPositionContext qualifiedColTypeWithPosition(int i) {
			return getRuleContext(QualifiedColTypeWithPositionContext.class,i);
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
		enterRule(_localctx, 240, RULE_qualifiedColTypeWithPositionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2904);
			qualifiedColTypeWithPosition();
			setState(2909);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2905);
				match(T__3);
				setState(2906);
				qualifiedColTypeWithPosition();
				}
				}
				setState(2911);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 242, RULE_qualifiedColTypeWithPosition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2912);
			((QualifiedColTypeWithPositionContext)_localctx).name = multipartIdentifier();
			setState(2913);
			dataType();
			setState(2916);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(2914);
				match(NOT);
				setState(2915);
				match(NULL);
				}
			}

			setState(2919);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2918);
				commentSpec();
				}
			}

			setState(2922);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AFTER || _la==FIRST) {
				{
				setState(2921);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ColTypeListContext extends ParserRuleContext {
		public List<ColTypeContext> colType() {
			return getRuleContexts(ColTypeContext.class);
		}
		public ColTypeContext colType(int i) {
			return getRuleContext(ColTypeContext.class,i);
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
		enterRule(_localctx, 244, RULE_colTypeList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2924);
			colType();
			setState(2929);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,379,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2925);
					match(T__3);
					setState(2926);
					colType();
					}
					} 
				}
				setState(2931);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,379,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 246, RULE_colType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2932);
			((ColTypeContext)_localctx).colName = errorCapturingIdentifier();
			setState(2933);
			dataType();
			setState(2936);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,380,_ctx) ) {
			case 1:
				{
				setState(2934);
				match(NOT);
				setState(2935);
				match(NULL);
				}
				break;
			}
			setState(2939);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,381,_ctx) ) {
			case 1:
				{
				setState(2938);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ComplexColTypeListContext extends ParserRuleContext {
		public List<ComplexColTypeContext> complexColType() {
			return getRuleContexts(ComplexColTypeContext.class);
		}
		public ComplexColTypeContext complexColType(int i) {
			return getRuleContext(ComplexColTypeContext.class,i);
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
		enterRule(_localctx, 248, RULE_complexColTypeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2941);
			complexColType();
			setState(2946);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(2942);
				match(T__3);
				setState(2943);
				complexColType();
				}
				}
				setState(2948);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ComplexColTypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
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
		enterRule(_localctx, 250, RULE_complexColType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2949);
			identifier();
			setState(2951);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,383,_ctx) ) {
			case 1:
				{
				setState(2950);
				match(T__10);
				}
				break;
			}
			setState(2953);
			dataType();
			setState(2956);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(2954);
				match(NOT);
				setState(2955);
				match(NULL);
				}
			}

			setState(2959);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2958);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 252, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2961);
			match(WHEN);
			setState(2962);
			((WhenClauseContext)_localctx).condition = expression();
			setState(2963);
			match(THEN);
			setState(2964);
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

	@SuppressWarnings("CheckReturnValue")
	public static class WindowClauseContext extends ParserRuleContext {
		public TerminalNode WINDOW() { return getToken(ArcticSqlCommandParser.WINDOW, 0); }
		public List<NamedWindowContext> namedWindow() {
			return getRuleContexts(NamedWindowContext.class);
		}
		public NamedWindowContext namedWindow(int i) {
			return getRuleContext(NamedWindowContext.class,i);
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
		enterRule(_localctx, 254, RULE_windowClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2966);
			match(WINDOW);
			setState(2967);
			namedWindow();
			setState(2972);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,386,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2968);
					match(T__3);
					setState(2969);
					namedWindow();
					}
					} 
				}
				setState(2974);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,386,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 256, RULE_namedWindow);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2975);
			((NamedWindowContext)_localctx).name = errorCapturingIdentifier();
			setState(2976);
			match(AS);
			setState(2977);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
	public static class WindowRefContext extends WindowSpecContext {
		public ErrorCapturingIdentifierContext name;
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
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
	@SuppressWarnings("CheckReturnValue")
	public static class WindowDefContext extends WindowSpecContext {
		public ExpressionContext expression;
		public List<ExpressionContext> partition = new ArrayList<ExpressionContext>();
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
		enterRule(_localctx, 258, RULE_windowSpec);
		int _la;
		try {
			setState(3025);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,394,_ctx) ) {
			case 1:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2979);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				}
				break;
			case 2:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2980);
				match(T__1);
				setState(2981);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				setState(2982);
				match(T__2);
				}
				break;
			case 3:
				_localctx = new WindowDefContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2984);
				match(T__1);
				setState(3019);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLUSTER:
					{
					setState(2985);
					match(CLUSTER);
					setState(2986);
					match(BY);
					setState(2987);
					((WindowDefContext)_localctx).expression = expression();
					((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
					setState(2992);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(2988);
						match(T__3);
						setState(2989);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						}
						}
						setState(2994);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case T__2:
				case DISTRIBUTE:
				case ORDER:
				case PARTITION:
				case RANGE:
				case ROWS:
				case SORT:
					{
					setState(3005);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DISTRIBUTE || _la==PARTITION) {
						{
						setState(2995);
						_la = _input.LA(1);
						if ( !(_la==DISTRIBUTE || _la==PARTITION) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2996);
						match(BY);
						setState(2997);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						setState(3002);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==T__3) {
							{
							{
							setState(2998);
							match(T__3);
							setState(2999);
							((WindowDefContext)_localctx).expression = expression();
							((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
							}
							}
							setState(3004);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
					}

					setState(3017);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ORDER || _la==SORT) {
						{
						setState(3007);
						_la = _input.LA(1);
						if ( !(_la==ORDER || _la==SORT) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3008);
						match(BY);
						setState(3009);
						sortItem();
						setState(3014);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==T__3) {
							{
							{
							setState(3010);
							match(T__3);
							setState(3011);
							sortItem();
							}
							}
							setState(3016);
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
				setState(3022);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==RANGE || _la==ROWS) {
					{
					setState(3021);
					windowFrame();
					}
				}

				setState(3024);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 260, RULE_windowFrame);
		try {
			setState(3043);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,395,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3027);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3028);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3029);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3030);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3031);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3032);
				match(BETWEEN);
				setState(3033);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3034);
				match(AND);
				setState(3035);
				((WindowFrameContext)_localctx).end = frameBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3037);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3038);
				match(BETWEEN);
				setState(3039);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3040);
				match(AND);
				setState(3041);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 262, RULE_frameBound);
		int _la;
		try {
			setState(3052);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,396,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3045);
				match(UNBOUNDED);
				setState(3046);
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
				setState(3047);
				((FrameBoundContext)_localctx).boundType = match(CURRENT);
				setState(3048);
				match(ROW);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3049);
				expression();
				setState(3050);
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

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameListContext extends ParserRuleContext {
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
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
		enterRule(_localctx, 264, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3054);
			qualifiedName();
			setState(3059);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(3055);
				match(T__3);
				setState(3056);
				qualifiedName();
				}
				}
				setState(3061);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 266, RULE_functionName);
		try {
			setState(3066);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,398,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3062);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3063);
				match(FILTER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3064);
				match(LEFT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3065);
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

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
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
		enterRule(_localctx, 268, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3068);
			identifier();
			setState(3073);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,399,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3069);
					match(T__4);
					setState(3070);
					identifier();
					}
					} 
				}
				setState(3075);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,399,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 270, RULE_errorCapturingIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3076);
			identifier();
			setState(3077);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 272, RULE_errorCapturingIdentifierExtra);
		try {
			int _alt;
			setState(3086);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,401,_ctx) ) {
			case 1:
				_localctx = new ErrorIdentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3081); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(3079);
						match(MINUS);
						setState(3080);
						identifier();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(3083); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,400,_ctx);
				} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 274, RULE_identifier);
		try {
			setState(3091);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,402,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3088);
				strictIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3089);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3090);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 276, RULE_strictIdentifier);
		try {
			setState(3099);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,403,_ctx) ) {
			case 1:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3093);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3094);
				quotedIdentifier();
				}
				break;
			case 3:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3095);
				if (!(SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "SQL_standard_keyword_behavior");
				setState(3096);
				ansiNonReserved();
				}
				break;
			case 4:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3097);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3098);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 278, RULE_quotedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3101);
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

	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 280, RULE_number);
		int _la;
		try {
			setState(3146);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,414,_ctx) ) {
			case 1:
				_localctx = new ExponentLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3103);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3105);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3104);
					match(MINUS);
					}
				}

				setState(3107);
				match(EXPONENT_VALUE);
				}
				break;
			case 2:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3108);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3110);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3109);
					match(MINUS);
					}
				}

				setState(3112);
				match(DECIMAL_VALUE);
				}
				break;
			case 3:
				_localctx = new LegacyDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3113);
				if (!(legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "legacy_exponent_literal_as_decimal_enabled");
				setState(3115);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3114);
					match(MINUS);
					}
				}

				setState(3117);
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
				setState(3119);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3118);
					match(MINUS);
					}
				}

				setState(3121);
				match(INTEGER_VALUE);
				}
				break;
			case 5:
				_localctx = new BigIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(3123);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3122);
					match(MINUS);
					}
				}

				setState(3125);
				match(BIGINT_LITERAL);
				}
				break;
			case 6:
				_localctx = new SmallIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(3127);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3126);
					match(MINUS);
					}
				}

				setState(3129);
				match(SMALLINT_LITERAL);
				}
				break;
			case 7:
				_localctx = new TinyIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(3131);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3130);
					match(MINUS);
					}
				}

				setState(3133);
				match(TINYINT_LITERAL);
				}
				break;
			case 8:
				_localctx = new DoubleLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(3135);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3134);
					match(MINUS);
					}
				}

				setState(3137);
				match(DOUBLE_LITERAL);
				}
				break;
			case 9:
				_localctx = new FloatLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(3139);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3138);
					match(MINUS);
					}
				}

				setState(3141);
				match(FLOAT_LITERAL);
				}
				break;
			case 10:
				_localctx = new BigDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(3143);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3142);
					match(MINUS);
					}
				}

				setState(3145);
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 282, RULE_alterColumnAction);
		int _la;
		try {
			setState(3155);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(3148);
				match(TYPE);
				setState(3149);
				dataType();
				}
				break;
			case COMMENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(3150);
				commentSpec();
				}
				break;
			case AFTER:
			case FIRST:
				enterOuterAlt(_localctx, 3);
				{
				setState(3151);
				colPosition();
				}
				break;
			case DROP:
			case SET:
				enterOuterAlt(_localctx, 4);
				{
				setState(3152);
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
				setState(3153);
				match(NOT);
				setState(3154);
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

	@SuppressWarnings("CheckReturnValue")
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
		public TerminalNode DAY() { return getToken(ArcticSqlCommandParser.DAY, 0); }
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
		public TerminalNode QUERY() { return getToken(ArcticSqlCommandParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(ArcticSqlCommandParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticSqlCommandParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticSqlCommandParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(ArcticSqlCommandParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticSqlCommandParser.REDUCE, 0); }
		public TerminalNode REFRESH() { return getToken(ArcticSqlCommandParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(ArcticSqlCommandParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticSqlCommandParser.REPAIR, 0); }
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
		public TerminalNode TABLES() { return getToken(ArcticSqlCommandParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticSqlCommandParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticSqlCommandParser.TERMINATED, 0); }
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
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticSqlCommandParser.VIEWS, 0); }
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
		enterRule(_localctx, 284, RULE_ansiNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3157);
			_la = _input.LA(1);
			if ( !(((((_la - 19)) & ~0x3f) == 0 && ((1L << (_la - 19)) & -4279320915027239L) != 0) || ((((_la - 84)) & ~0x3f) == 0 && ((1L << (_la - 84)) & -237358745750175929L) != 0) || ((((_la - 148)) & ~0x3f) == 0 && ((1L << (_la - 148)) & -577587752352718849L) != 0) || ((((_la - 212)) & ~0x3f) == 0 && ((1L << (_la - 212)) & -3189533745841505313L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		enterRule(_localctx, 286, RULE_strictNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3159);
			_la = _input.LA(1);
			if ( !(_la==USING || _la==ANTI || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 4611826756049960961L) != 0) || ((((_la - 130)) & ~0x3f) == 0 && ((1L << (_la - 130)) & 139586439457L) != 0) || ((((_la - 207)) & ~0x3f) == 0 && ((1L << (_la - 207)) & 4503599627503617L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		public TerminalNode DAY() { return getToken(ArcticSqlCommandParser.DAY, 0); }
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
		public TerminalNode PERCENTLIT() { return getToken(ArcticSqlCommandParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(ArcticSqlCommandParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(ArcticSqlCommandParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(ArcticSqlCommandParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticSqlCommandParser.PRECEDING, 0); }
		public TerminalNode PRIMARY() { return getToken(ArcticSqlCommandParser.PRIMARY, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticSqlCommandParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticSqlCommandParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(ArcticSqlCommandParser.PURGE, 0); }
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
		public TerminalNode TABLE() { return getToken(ArcticSqlCommandParser.TABLE, 0); }
		public TerminalNode TABLES() { return getToken(ArcticSqlCommandParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticSqlCommandParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticSqlCommandParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticSqlCommandParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticSqlCommandParser.TERMINATED, 0); }
		public TerminalNode THEN() { return getToken(ArcticSqlCommandParser.THEN, 0); }
		public TerminalNode TIME() { return getToken(ArcticSqlCommandParser.TIME, 0); }
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
		public TerminalNode VIEW() { return getToken(ArcticSqlCommandParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticSqlCommandParser.VIEWS, 0); }
		public TerminalNode WHEN() { return getToken(ArcticSqlCommandParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(ArcticSqlCommandParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(ArcticSqlCommandParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(ArcticSqlCommandParser.WITH, 0); }
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
		enterRule(_localctx, 288, RULE_nonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3161);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & -271908864L) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & -2305913378024980481L) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & -279172878915L) != 0) || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & -2181054465L) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & 524283L) != 0)) ) {
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
		case 44:
			return queryTerm_sempred((QueryTermContext)_localctx, predIndex);
		case 103:
			return booleanExpression_sempred((BooleanExpressionContext)_localctx, predIndex);
		case 105:
			return valueExpression_sempred((ValueExpressionContext)_localctx, predIndex);
		case 106:
			return primaryExpression_sempred((PrimaryExpressionContext)_localctx, predIndex);
		case 137:
			return identifier_sempred((IdentifierContext)_localctx, predIndex);
		case 138:
			return strictIdentifier_sempred((StrictIdentifierContext)_localctx, predIndex);
		case 140:
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
			return precpred(_ctx, 8);
		case 15:
			return precpred(_ctx, 6);
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
		"\u0004\u0001\u0135\u0c5c\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
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
		"\u008f\u0002\u0090\u0007\u0090\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001\u0133\b\u0001\u0001\u0002\u0001\u0002\u0005\u0002\u0137"+
		"\b\u0002\n\u0002\f\u0002\u013a\t\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0003"+
		"\t\u0152\b\t\u0001\t\u0001\t\u0001\t\u0003\t\u0157\b\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u015f\b\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0005\t\u0167\b\t\n\t\f\t\u016a\t\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u017d"+
		"\b\t\u0001\t\u0001\t\u0003\t\u0181\b\t\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0003\t\u0187\b\t\u0001\t\u0003\t\u018a\b\t\u0001\t\u0003\t\u018d\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0194\b\t\u0001\t\u0003"+
		"\t\u0197\b\t\u0001\t\u0001\t\u0003\t\u019b\b\t\u0001\t\u0003\t\u019e\b"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u01a5\b\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u01b0"+
		"\b\t\n\t\f\t\u01b3\t\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t"+
		"\u01ba\b\t\u0001\t\u0003\t\u01bd\b\t\u0001\t\u0001\t\u0003\t\u01c1\b\t"+
		"\u0001\t\u0003\t\u01c4\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u01ca"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u01d5\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u01db\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0003\t\u01e0\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0220\b\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0229\b\t\u0001\t\u0001\t\u0003"+
		"\t\u022d\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0233\b\t\u0001\t"+
		"\u0001\t\u0003\t\u0237\b\t\u0001\t\u0001\t\u0001\t\u0003\t\u023c\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\t\u0242\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u024e\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0256\b\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0003\t\u025c\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0269\b\t\u0001"+
		"\t\u0004\t\u026c\b\t\u000b\t\f\t\u026d\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u027e\b\t\u0001\t\u0001\t\u0001\t\u0005\t\u0283\b\t"+
		"\n\t\f\t\u0286\t\t\u0001\t\u0003\t\u0289\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u028f\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u029e\b\t\u0001"+
		"\t\u0001\t\u0003\t\u02a2\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u02a8"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u02ae\b\t\u0001\t\u0003\t"+
		"\u02b1\b\t\u0001\t\u0003\t\u02b4\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003"+
		"\t\u02ba\b\t\u0001\t\u0001\t\u0003\t\u02be\b\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0005\t\u02c6\b\t\n\t\f\t\u02c9\t\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u02d1\b\t\u0001\t\u0003\t\u02d4"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u02dd"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0003\t\u02e2\b\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0003\t\u02e8\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003"+
		"\t\u02ef\b\t\u0001\t\u0003\t\u02f2\b\t\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0003\t\u02f8\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0005\t\u0301\b\t\n\t\f\t\u0304\t\t\u0003\t\u0306\b\t\u0001\t\u0001"+
		"\t\u0003\t\u030a\b\t\u0001\t\u0001\t\u0001\t\u0003\t\u030f\b\t\u0001\t"+
		"\u0001\t\u0001\t\u0003\t\u0314\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u031b\b\t\u0001\t\u0003\t\u031e\b\t\u0001\t\u0003\t\u0321\b"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0328\b\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u032d\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u0336\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u033e\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0344\b\t"+
		"\u0001\t\u0003\t\u0347\b\t\u0001\t\u0003\t\u034a\b\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u0350\b\t\u0001\t\u0001\t\u0003\t\u0354\b\t\u0001\t"+
		"\u0001\t\u0003\t\u0358\b\t\u0001\t\u0001\t\u0003\t\u035c\b\t\u0003\t\u035e"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0366\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u036e\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\t\u0374\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u037a\b\t\u0001\t\u0003\t\u037d\b\t\u0001\t\u0001\t\u0003\t"+
		"\u0381\b\t\u0001\t\u0003\t\u0384\b\t\u0001\t\u0001\t\u0003\t\u0388\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u03a2\b\t\n"+
		"\t\f\t\u03a5\t\t\u0003\t\u03a7\b\t\u0001\t\u0001\t\u0003\t\u03ab\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\t\u03b1\b\t\u0001\t\u0003\t\u03b4\b\t"+
		"\u0001\t\u0003\t\u03b7\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u03bd"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u03c5\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0003\t\u03ca\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u03d0\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u03d6\b\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u03de\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0005\t\u03e3\b\t\n\t\f\t\u03e6\t\t\u0001\t\u0001\t"+
		"\u0001\t\u0005\t\u03eb\b\t\n\t\f\t\u03ee\t\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005"+
		"\t\u03fc\b\t\n\t\f\t\u03ff\t\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u040a\b\t\n\t\f\t\u040d\t\t\u0003"+
		"\t\u040f\b\t\u0001\t\u0001\t\u0005\t\u0413\b\t\n\t\f\t\u0416\t\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0005\t\u041c\b\t\n\t\f\t\u041f\t\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0005\t\u0425\b\t\n\t\f\t\u0428\t\t\u0001\t\u0001"+
		"\t\u0005\t\u042c\b\t\n\t\f\t\u042f\t\t\u0003\t\u0431\b\t\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u043d\b\f\u0001\f\u0001\f\u0003\f\u0441\b\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0003\f\u0448\b\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u04bc\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u04c4\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u04cc\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u04d5\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u04df\b\f\u0001\r\u0001\r\u0003\r\u04e3\b\r\u0001\r"+
		"\u0003\r\u04e6\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u04ec\b\r\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0003\u000e\u04f2\b\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u04fe\b\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u050a\b\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0003\u0010\u050f\b\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0003\u0013"+
		"\u0518\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0003\u0014\u0520\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0003\u0014\u0527\b\u0014\u0003\u0014\u0529\b"+
		"\u0014\u0001\u0014\u0003\u0014\u052c\b\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u0531\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0535"+
		"\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u053a\b\u0014"+
		"\u0001\u0014\u0003\u0014\u053d\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0003\u0014\u0542\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u0547\b\u0014\u0001\u0014\u0003\u0014\u054a\b\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0003\u0014\u054f\b\u0014\u0001\u0014\u0001\u0014\u0003"+
		"\u0014\u0553\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0558"+
		"\b\u0014\u0003\u0014\u055a\b\u0014\u0001\u0015\u0001\u0015\u0003\u0015"+
		"\u055e\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0005\u0016\u0565\b\u0016\n\u0016\f\u0016\u0568\t\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u056f\b\u0017\u0001"+
		"\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0003\u0019\u0578\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005"+
		"\u001a\u057d\b\u001a\n\u001a\f\u001a\u0580\t\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0005\u001b\u0586\b\u001b\n\u001b\f\u001b\u0589"+
		"\t\u001b\u0001\u001c\u0001\u001c\u0003\u001c\u058d\b\u001c\u0001\u001c"+
		"\u0003\u001c\u0590\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u05a6\b\u001e"+
		"\n\u001e\f\u001e\u05a9\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0005\u001f\u05af\b\u001f\n\u001f\f\u001f\u05b2\t\u001f\u0001\u001f"+
		"\u0001\u001f\u0001 \u0001 \u0003 \u05b8\b \u0001 \u0003 \u05bb\b \u0001"+
		"!\u0001!\u0001!\u0005!\u05c0\b!\n!\f!\u05c3\t!\u0001!\u0003!\u05c6\b!"+
		"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\"\u05cc\b\"\u0001#\u0001#\u0001"+
		"#\u0001#\u0005#\u05d2\b#\n#\f#\u05d5\t#\u0001#\u0001#\u0001$\u0001$\u0001"+
		"$\u0001$\u0005$\u05dd\b$\n$\f$\u05e0\t$\u0001$\u0001$\u0001%\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0003%\u05ea\b%\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0003&\u05f1\b&\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u05f7\b\'\u0001"+
		"(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0004)\u0602"+
		"\b)\u000b)\f)\u0603\u0001)\u0001)\u0001)\u0001)\u0001)\u0003)\u060b\b"+
		")\u0001)\u0001)\u0001)\u0001)\u0001)\u0003)\u0612\b)\u0001)\u0001)\u0001"+
		")\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0003)\u061e\b)\u0001"+
		")\u0001)\u0001)\u0001)\u0005)\u0624\b)\n)\f)\u0627\t)\u0001)\u0005)\u062a"+
		"\b)\n)\f)\u062d\t)\u0003)\u062f\b)\u0001*\u0001*\u0001*\u0001*\u0001*"+
		"\u0005*\u0636\b*\n*\f*\u0639\t*\u0003*\u063b\b*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0005*\u0642\b*\n*\f*\u0645\t*\u0003*\u0647\b*\u0001*\u0001*"+
		"\u0001*\u0001*\u0001*\u0005*\u064e\b*\n*\f*\u0651\t*\u0003*\u0653\b*\u0001"+
		"*\u0001*\u0001*\u0001*\u0001*\u0005*\u065a\b*\n*\f*\u065d\t*\u0003*\u065f"+
		"\b*\u0001*\u0003*\u0662\b*\u0001*\u0001*\u0001*\u0003*\u0667\b*\u0003"+
		"*\u0669\b*\u0001+\u0001+\u0001+\u0001,\u0001,\u0001,\u0001,\u0001,\u0001"+
		",\u0001,\u0003,\u0675\b,\u0001,\u0001,\u0001,\u0001,\u0001,\u0003,\u067c"+
		"\b,\u0001,\u0001,\u0001,\u0001,\u0001,\u0003,\u0683\b,\u0001,\u0005,\u0686"+
		"\b,\n,\f,\u0689\t,\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001"+
		"-\u0001-\u0003-\u0694\b-\u0001.\u0001.\u0003.\u0698\b.\u0001.\u0001.\u0003"+
		".\u069c\b.\u0001/\u0001/\u0004/\u06a0\b/\u000b/\f/\u06a1\u00010\u0001"+
		"0\u00030\u06a6\b0\u00010\u00010\u00010\u00010\u00050\u06ac\b0\n0\f0\u06af"+
		"\t0\u00010\u00030\u06b2\b0\u00010\u00030\u06b5\b0\u00010\u00030\u06b8"+
		"\b0\u00010\u00030\u06bb\b0\u00010\u00010\u00030\u06bf\b0\u00011\u0001"+
		"1\u00031\u06c3\b1\u00011\u00051\u06c6\b1\n1\f1\u06c9\t1\u00011\u00031"+
		"\u06cc\b1\u00011\u00031\u06cf\b1\u00011\u00031\u06d2\b1\u00011\u00031"+
		"\u06d5\b1\u00011\u00011\u00031\u06d9\b1\u00011\u00051\u06dc\b1\n1\f1\u06df"+
		"\t1\u00011\u00031\u06e2\b1\u00011\u00031\u06e5\b1\u00011\u00031\u06e8"+
		"\b1\u00011\u00031\u06eb\b1\u00031\u06ed\b1\u00012\u00012\u00012\u0001"+
		"2\u00032\u06f3\b2\u00012\u00012\u00012\u00012\u00012\u00032\u06fa\b2\u0001"+
		"2\u00012\u00012\u00032\u06ff\b2\u00012\u00032\u0702\b2\u00012\u00032\u0705"+
		"\b2\u00012\u00012\u00032\u0709\b2\u00012\u00012\u00012\u00012\u00012\u0001"+
		"2\u00012\u00012\u00032\u0713\b2\u00012\u00012\u00032\u0717\b2\u00032\u0719"+
		"\b2\u00012\u00032\u071c\b2\u00012\u00012\u00032\u0720\b2\u00013\u0001"+
		"3\u00053\u0724\b3\n3\f3\u0727\t3\u00013\u00033\u072a\b3\u00013\u00013"+
		"\u00014\u00014\u00014\u00015\u00015\u00015\u00015\u00035\u0735\b5\u0001"+
		"5\u00015\u00015\u00016\u00016\u00016\u00016\u00016\u00036\u073f\b6\u0001"+
		"6\u00016\u00016\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u0003"+
		"7\u074b\b7\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00018\u00058\u0758\b8\n8\f8\u075b\t8\u00018\u00018\u00038\u075f"+
		"\b8\u00019\u00019\u00019\u00059\u0764\b9\n9\f9\u0767\t9\u0001:\u0001:"+
		"\u0001:\u0001:\u0001;\u0001;\u0001;\u0001<\u0001<\u0001<\u0001=\u0001"+
		"=\u0001=\u0003=\u0776\b=\u0001=\u0005=\u0779\b=\n=\f=\u077c\t=\u0001="+
		"\u0001=\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0005>\u0786\b>\n>\f"+
		">\u0789\t>\u0001>\u0001>\u0003>\u078d\b>\u0001?\u0001?\u0001?\u0001?\u0005"+
		"?\u0793\b?\n?\f?\u0796\t?\u0001?\u0005?\u0799\b?\n?\f?\u079c\t?\u0001"+
		"?\u0003?\u079f\b?\u0001@\u0001@\u0001@\u0001@\u0001@\u0005@\u07a6\b@\n"+
		"@\f@\u07a9\t@\u0001@\u0001@\u0001@\u0001@\u0001@\u0005@\u07b0\b@\n@\f"+
		"@\u07b3\t@\u0001@\u0001@\u0001@\u0001@\u0001@\u0001@\u0001@\u0001@\u0001"+
		"@\u0001@\u0005@\u07bf\b@\n@\f@\u07c2\t@\u0001@\u0001@\u0003@\u07c6\b@"+
		"\u0003@\u07c8\b@\u0001A\u0001A\u0003A\u07cc\bA\u0001B\u0001B\u0001B\u0001"+
		"B\u0001B\u0005B\u07d3\bB\nB\fB\u07d6\tB\u0001B\u0001B\u0001B\u0001B\u0001"+
		"B\u0001B\u0001B\u0001B\u0005B\u07e0\bB\nB\fB\u07e3\tB\u0001B\u0001B\u0003"+
		"B\u07e7\bB\u0001C\u0001C\u0003C\u07eb\bC\u0001D\u0001D\u0001D\u0001D\u0005"+
		"D\u07f1\bD\nD\fD\u07f4\tD\u0003D\u07f6\bD\u0001D\u0001D\u0003D\u07fa\b"+
		"D\u0001E\u0001E\u0001E\u0001E\u0001E\u0001E\u0001E\u0001E\u0001E\u0001"+
		"E\u0005E\u0806\bE\nE\fE\u0809\tE\u0001E\u0001E\u0001E\u0001F\u0001F\u0001"+
		"F\u0001F\u0001F\u0005F\u0813\bF\nF\fF\u0816\tF\u0001F\u0001F\u0003F\u081a"+
		"\bF\u0001G\u0001G\u0003G\u081e\bG\u0001G\u0003G\u0821\bG\u0001H\u0001"+
		"H\u0001H\u0003H\u0826\bH\u0001H\u0001H\u0001H\u0001H\u0001H\u0005H\u082d"+
		"\bH\nH\fH\u0830\tH\u0003H\u0832\bH\u0001H\u0001H\u0001H\u0003H\u0837\b"+
		"H\u0001H\u0001H\u0001H\u0005H\u083c\bH\nH\fH\u083f\tH\u0003H\u0841\bH"+
		"\u0001I\u0001I\u0001J\u0003J\u0846\bJ\u0001J\u0001J\u0005J\u084a\bJ\n"+
		"J\fJ\u084d\tJ\u0001K\u0001K\u0001K\u0003K\u0852\bK\u0001K\u0001K\u0003"+
		"K\u0856\bK\u0001K\u0001K\u0001K\u0001K\u0003K\u085c\bK\u0001K\u0001K\u0003"+
		"K\u0860\bK\u0001L\u0003L\u0863\bL\u0001L\u0001L\u0001L\u0003L\u0868\b"+
		"L\u0001L\u0003L\u086b\bL\u0001L\u0001L\u0001L\u0003L\u0870\bL\u0001L\u0001"+
		"L\u0003L\u0874\bL\u0001L\u0003L\u0877\bL\u0001L\u0003L\u087a\bL\u0001"+
		"M\u0001M\u0001M\u0001M\u0003M\u0880\bM\u0001N\u0001N\u0001N\u0003N\u0885"+
		"\bN\u0001N\u0001N\u0001O\u0003O\u088a\bO\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0003O\u089c\bO\u0003O\u089e\bO\u0001O\u0003O\u08a1\bO\u0001"+
		"P\u0001P\u0001P\u0001P\u0001Q\u0001Q\u0001Q\u0005Q\u08aa\bQ\nQ\fQ\u08ad"+
		"\tQ\u0001R\u0001R\u0001R\u0001R\u0005R\u08b3\bR\nR\fR\u08b6\tR\u0001R"+
		"\u0001R\u0001S\u0001S\u0003S\u08bc\bS\u0001T\u0001T\u0001T\u0001T\u0005"+
		"T\u08c2\bT\nT\fT\u08c5\tT\u0001T\u0001T\u0001U\u0001U\u0003U\u08cb\bU"+
		"\u0001V\u0001V\u0003V\u08cf\bV\u0001V\u0001V\u0001V\u0001V\u0001V\u0001"+
		"V\u0003V\u08d7\bV\u0001V\u0001V\u0001V\u0001V\u0001V\u0001V\u0003V\u08df"+
		"\bV\u0001V\u0001V\u0001V\u0001V\u0003V\u08e5\bV\u0001W\u0001W\u0001W\u0001"+
		"W\u0005W\u08eb\bW\nW\fW\u08ee\tW\u0001W\u0001W\u0001X\u0001X\u0001X\u0001"+
		"X\u0001X\u0005X\u08f7\bX\nX\fX\u08fa\tX\u0003X\u08fc\bX\u0001X\u0001X"+
		"\u0001X\u0001Y\u0003Y\u0902\bY\u0001Y\u0001Y\u0003Y\u0906\bY\u0003Y\u0908"+
		"\bY\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u0911\bZ\u0001"+
		"Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003"+
		"Z\u091d\bZ\u0003Z\u091f\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u0926"+
		"\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u092d\bZ\u0001Z\u0001Z\u0001"+
		"Z\u0001Z\u0003Z\u0933\bZ\u0001Z\u0001Z\u0001Z\u0001Z\u0003Z\u0939\bZ\u0003"+
		"Z\u093b\bZ\u0001[\u0001[\u0001[\u0005[\u0940\b[\n[\f[\u0943\t[\u0001\\"+
		"\u0001\\\u0001\\\u0005\\\u0948\b\\\n\\\f\\\u094b\t\\\u0001]\u0001]\u0001"+
		"]\u0003]\u0950\b]\u0001]\u0001]\u0001^\u0001^\u0001^\u0003^\u0957\b^\u0001"+
		"^\u0001^\u0001_\u0001_\u0003_\u095d\b_\u0001_\u0001_\u0003_\u0961\b_\u0003"+
		"_\u0963\b_\u0001`\u0001`\u0001`\u0005`\u0968\b`\n`\f`\u096b\t`\u0001a"+
		"\u0001a\u0001a\u0001a\u0005a\u0971\ba\na\fa\u0974\ta\u0001a\u0001a\u0001"+
		"b\u0001b\u0003b\u097a\bb\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0005"+
		"c\u0982\bc\nc\fc\u0985\tc\u0001c\u0001c\u0003c\u0989\bc\u0001d\u0001d"+
		"\u0003d\u098d\bd\u0001e\u0001e\u0001f\u0001f\u0001f\u0005f\u0994\bf\n"+
		"f\ff\u0997\tf\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g"+
		"\u0001g\u0001g\u0003g\u09a3\bg\u0003g\u09a5\bg\u0001g\u0001g\u0001g\u0001"+
		"g\u0001g\u0001g\u0005g\u09ad\bg\ng\fg\u09b0\tg\u0001h\u0003h\u09b3\bh"+
		"\u0001h\u0001h\u0001h\u0001h\u0001h\u0001h\u0003h\u09bb\bh\u0001h\u0001"+
		"h\u0001h\u0001h\u0001h\u0005h\u09c2\bh\nh\fh\u09c5\th\u0001h\u0001h\u0001"+
		"h\u0003h\u09ca\bh\u0001h\u0001h\u0001h\u0001h\u0001h\u0001h\u0003h\u09d2"+
		"\bh\u0001h\u0001h\u0001h\u0003h\u09d7\bh\u0001h\u0001h\u0001h\u0001h\u0001"+
		"h\u0001h\u0001h\u0001h\u0005h\u09e1\bh\nh\fh\u09e4\th\u0001h\u0001h\u0003"+
		"h\u09e8\bh\u0001h\u0003h\u09eb\bh\u0001h\u0001h\u0001h\u0001h\u0003h\u09f1"+
		"\bh\u0001h\u0001h\u0003h\u09f5\bh\u0001h\u0001h\u0001h\u0003h\u09fa\b"+
		"h\u0001h\u0001h\u0001h\u0003h\u09ff\bh\u0001h\u0001h\u0001h\u0003h\u0a04"+
		"\bh\u0001i\u0001i\u0001i\u0001i\u0003i\u0a0a\bi\u0001i\u0001i\u0001i\u0001"+
		"i\u0001i\u0001i\u0001i\u0001i\u0001i\u0001i\u0001i\u0001i\u0001i\u0001"+
		"i\u0001i\u0001i\u0001i\u0001i\u0001i\u0005i\u0a1f\bi\ni\fi\u0a22\ti\u0001"+
		"j\u0001j\u0001j\u0001j\u0004j\u0a28\bj\u000bj\fj\u0a29\u0001j\u0001j\u0003"+
		"j\u0a2e\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0004j\u0a35\bj\u000bj\f"+
		"j\u0a36\u0001j\u0001j\u0003j\u0a3b\bj\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0005"+
		"j\u0a4b\bj\nj\fj\u0a4e\tj\u0003j\u0a50\bj\u0001j\u0001j\u0001j\u0001j"+
		"\u0001j\u0001j\u0003j\u0a58\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0003j\u0a61\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0004j\u0a76\bj\u000bj\fj\u0a77\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0001j\u0003j\u0a83\bj\u0001j\u0001j\u0001"+
		"j\u0005j\u0a88\bj\nj\fj\u0a8b\tj\u0003j\u0a8d\bj\u0001j\u0001j\u0001j"+
		"\u0001j\u0001j\u0001j\u0001j\u0003j\u0a96\bj\u0001j\u0001j\u0003j\u0a9a"+
		"\bj\u0001j\u0001j\u0003j\u0a9e\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0004j\u0aa8\bj\u000bj\fj\u0aa9\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0003"+
		"j\u0ac3\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0003j\u0aca\bj\u0001j\u0003"+
		"j\u0acd\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0003j\u0adc\bj\u0001j\u0001j\u0003j\u0ae0"+
		"\bj\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0005j\u0aea"+
		"\bj\nj\fj\u0aed\tj\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0001"+
		"k\u0004k\u0af7\bk\u000bk\fk\u0af8\u0003k\u0afb\bk\u0001l\u0001l\u0001"+
		"m\u0001m\u0001n\u0001n\u0001o\u0001o\u0001p\u0001p\u0001p\u0003p\u0b08"+
		"\bp\u0001q\u0001q\u0003q\u0b0c\bq\u0001r\u0001r\u0001r\u0004r\u0b11\b"+
		"r\u000br\fr\u0b12\u0001s\u0001s\u0001s\u0003s\u0b18\bs\u0001t\u0001t\u0001"+
		"t\u0001t\u0001t\u0001u\u0003u\u0b20\bu\u0001u\u0001u\u0001v\u0001v\u0001"+
		"v\u0003v\u0b27\bv\u0001w\u0001w\u0001w\u0001w\u0001w\u0001w\u0001w\u0001"+
		"w\u0001w\u0001w\u0001w\u0001w\u0001w\u0001w\u0001w\u0003w\u0b38\bw\u0001"+
		"w\u0001w\u0003w\u0b3c\bw\u0001w\u0001w\u0001w\u0001w\u0003w\u0b42\bw\u0001"+
		"w\u0001w\u0001w\u0001w\u0003w\u0b48\bw\u0001w\u0001w\u0001w\u0001w\u0001"+
		"w\u0005w\u0b4f\bw\nw\fw\u0b52\tw\u0001w\u0003w\u0b55\bw\u0003w\u0b57\b"+
		"w\u0001x\u0001x\u0001x\u0005x\u0b5c\bx\nx\fx\u0b5f\tx\u0001y\u0001y\u0001"+
		"y\u0001y\u0003y\u0b65\by\u0001y\u0003y\u0b68\by\u0001y\u0003y\u0b6b\b"+
		"y\u0001z\u0001z\u0001z\u0005z\u0b70\bz\nz\fz\u0b73\tz\u0001{\u0001{\u0001"+
		"{\u0001{\u0003{\u0b79\b{\u0001{\u0003{\u0b7c\b{\u0001|\u0001|\u0001|\u0005"+
		"|\u0b81\b|\n|\f|\u0b84\t|\u0001}\u0001}\u0003}\u0b88\b}\u0001}\u0001}"+
		"\u0001}\u0003}\u0b8d\b}\u0001}\u0003}\u0b90\b}\u0001~\u0001~\u0001~\u0001"+
		"~\u0001~\u0001\u007f\u0001\u007f\u0001\u007f\u0001\u007f\u0005\u007f\u0b9b"+
		"\b\u007f\n\u007f\f\u007f\u0b9e\t\u007f\u0001\u0080\u0001\u0080\u0001\u0080"+
		"\u0001\u0080\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081"+
		"\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081"+
		"\u0005\u0081\u0baf\b\u0081\n\u0081\f\u0081\u0bb2\t\u0081\u0001\u0081\u0001"+
		"\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0005\u0081\u0bb9\b\u0081\n"+
		"\u0081\f\u0081\u0bbc\t\u0081\u0003\u0081\u0bbe\b\u0081\u0001\u0081\u0001"+
		"\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0005\u0081\u0bc5\b\u0081\n"+
		"\u0081\f\u0081\u0bc8\t\u0081\u0003\u0081\u0bca\b\u0081\u0003\u0081\u0bcc"+
		"\b\u0081\u0001\u0081\u0003\u0081\u0bcf\b\u0081\u0001\u0081\u0003\u0081"+
		"\u0bd2\b\u0081\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082"+
		"\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082"+
		"\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082\u0001\u0082\u0003\u0082"+
		"\u0be4\b\u0082\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0003\u0083\u0bed\b\u0083\u0001\u0084\u0001\u0084"+
		"\u0001\u0084\u0005\u0084\u0bf2\b\u0084\n\u0084\f\u0084\u0bf5\t\u0084\u0001"+
		"\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0003\u0085\u0bfb\b\u0085\u0001"+
		"\u0086\u0001\u0086\u0001\u0086\u0005\u0086\u0c00\b\u0086\n\u0086\f\u0086"+
		"\u0c03\t\u0086\u0001\u0087\u0001\u0087\u0001\u0087\u0001\u0088\u0001\u0088"+
		"\u0004\u0088\u0c0a\b\u0088\u000b\u0088\f\u0088\u0c0b\u0001\u0088\u0003"+
		"\u0088\u0c0f\b\u0088\u0001\u0089\u0001\u0089\u0001\u0089\u0003\u0089\u0c14"+
		"\b\u0089\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0001"+
		"\u008a\u0003\u008a\u0c1c\b\u008a\u0001\u008b\u0001\u008b\u0001\u008c\u0001"+
		"\u008c\u0003\u008c\u0c22\b\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0003"+
		"\u008c\u0c27\b\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u0c2c"+
		"\b\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u0c30\b\u008c\u0001\u008c"+
		"\u0001\u008c\u0003\u008c\u0c34\b\u008c\u0001\u008c\u0001\u008c\u0003\u008c"+
		"\u0c38\b\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u0c3c\b\u008c\u0001"+
		"\u008c\u0001\u008c\u0003\u008c\u0c40\b\u008c\u0001\u008c\u0001\u008c\u0003"+
		"\u008c\u0c44\b\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u0c48\b\u008c"+
		"\u0001\u008c\u0003\u008c\u0c4b\b\u008c\u0001\u008d\u0001\u008d\u0001\u008d"+
		"\u0001\u008d\u0001\u008d\u0001\u008d\u0001\u008d\u0003\u008d\u0c54\b\u008d"+
		"\u0001\u008e\u0001\u008e\u0001\u008f\u0001\u008f\u0001\u0090\u0001\u0090"+
		"\u0001\u0090\t\u03a3\u03e4\u03ec\u03fd\u040b\u0414\u041d\u0426\u042d\u0004"+
		"X\u00ce\u00d2\u00d4\u0091\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0"+
		"\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8"+
		"\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0"+
		"\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108"+
		"\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118\u011a\u011c\u011e\u0120"+
		"\u00003\u0002\u0000JJ\u00be\u00be\u0002\u0000**\u00cd\u00cd\u0002\u0000"+
		"II\u00a0\u00a0\u0002\u0000nn{{\u0001\u000056\u0002\u0000\u0010\u0010\u010c"+
		"\u010c\u0002\u0000\u0019\u0019--\u0005\u000022>>``mm\u0097\u0097\u0001"+
		"\u0000NO\u0002\u0000``mm\u0002\u0000\u00a4\u00a4\u0126\u0126\u0003\u0000"+
		"\u0016\u0016VV\u00ee\u00ee\u0002\u0000\u0016\u0016\u0091\u0091\u0002\u0000"+
		"\u0093\u0093\u0126\u0126\u0003\u0000HH\u009f\u009f\u00d8\u00d8\u0004\u0000"+
		"[[\u0082\u0082\u00e0\u00e0\u0103\u0103\u0003\u0000[[\u00e0\u00e0\u0103"+
		"\u0103\u0002\u0000!!NN\u0002\u0000hh\u0089\u0089\u0002\u0000AA\u00d4\u00d4"+
		"\u0002\u0000\u0018\u0018SS\u0002\u0000\u012a\u012a\u012c\u012c\u0003\u0000"+
		"\u0018\u0018\u001d\u001d\u00e4\u00e4\u0003\u0000cc\u00fc\u00fc\u0105\u0105"+
		"\u0002\u0000\u011c\u011d\u0121\u0121\u0002\u0000UU\u011e\u0120\u0002\u0000"+
		"\u011c\u011d\u0124\u0124\u0002\u0000CCEF\u0002\u0000,,\u00fe\u00fe\u0002"+
		"\u0000yy\u00cc\u00cc\u0001\u0000\u00ec\u00ed\u0002\u0000\u0004\u0004n"+
		"n\u0002\u0000\u0004\u0004jj\u0003\u0000%%\u008c\u008c\u00f7\u00f7\u0001"+
		"\u0000\u0114\u011b\u0002\u0000UU\u011c\u0125\u0004\u0000\u001b\u001b{"+
		"{\u00a3\u00a3\u00ab\u00ab\u0002\u0000cc\u00fc\u00fc\u0001\u0000\u011c"+
		"\u011d\u0003\u0000\u0126\u0126\u012a\u012a\u012c\u012c\u0002\u0000\u009d"+
		"\u009d\u0112\u0112\u0004\u0000GGww\u009c\u009c\u00d7\u00d7\u0003\u0000"+
		"ww\u009c\u009c\u00d7\u00d7\u0002\u0000TT\u00b4\u00b4\u0002\u0000\u00ac"+
		"\u00ac\u00e5\u00e5\u0002\u0000ii\u00bb\u00bb\u0001\u0000\u012b\u012c\u0002"+
		"\u0000VV\u00df\u00df1\u0000\u0013\u0013\u0016\u0017\u0019\u001a\u001c"+
		"\u001c\u001e\u001f!\"$$&*--/2446<>>ABGRTVZZ\\beegilmpruuwz|}\u007f\u0081"+
		"\u0083\u0083\u0086\u0086\u0088\u0089\u008b\u008b\u008e\u00a0\u00a2\u00a2"+
		"\u00a5\u00a6\u00a9\u00aa\u00ad\u00ad\u00af\u00b0\u00b2\u00bb\u00bd\u00c5"+
		"\u00c7\u00ce\u00d0\u00d8\u00da\u00dd\u00df\u00e3\u00e5\u00f3\u00f6\u00f6"+
		"\u00f8\u0102\u0106\u0109\u010b\u010d\u0110\u0110\u0112\u0113\u0010\u0000"+
		"\u0014\u0014\u001c\u001c@@[[oo~~\u0082\u0082\u0087\u0087\u008a\u008a\u008d"+
		"\u008d\u00a1\u00a1\u00a7\u00a7\u00cf\u00cf\u00da\u00da\u00e0\u00e0\u0103"+
		"\u0103\u0012\u0000\u0010\u0011\u0013\u0013\u0016\u001b\u001d?AZ\\np}\u007f"+
		"\u0081\u0083\u0086\u0088\u0089\u008b\u008c\u008e\u00a0\u00a2\u00a6\u00a8"+
		"\u00ce\u00d0\u00d9\u00db\u00df\u00e1\u0102\u0104\u0113\u0e44\u0000\u0122"+
		"\u0001\u0000\u0000\u0000\u0002\u0132\u0001\u0000\u0000\u0000\u0004\u0134"+
		"\u0001\u0000\u0000\u0000\u0006\u013d\u0001\u0000\u0000\u0000\b\u0140\u0001"+
		"\u0000\u0000\u0000\n\u0143\u0001\u0000\u0000\u0000\f\u0146\u0001\u0000"+
		"\u0000\u0000\u000e\u0149\u0001\u0000\u0000\u0000\u0010\u014c\u0001\u0000"+
		"\u0000\u0000\u0012\u0430\u0001\u0000\u0000\u0000\u0014\u0432\u0001\u0000"+
		"\u0000\u0000\u0016\u0434\u0001\u0000\u0000\u0000\u0018\u04de\u0001\u0000"+
		"\u0000\u0000\u001a\u04e0\u0001\u0000\u0000\u0000\u001c\u04f1\u0001\u0000"+
		"\u0000\u0000\u001e\u04f7\u0001\u0000\u0000\u0000 \u0503\u0001\u0000\u0000"+
		"\u0000\"\u0510\u0001\u0000\u0000\u0000$\u0513\u0001\u0000\u0000\u0000"+
		"&\u0517\u0001\u0000\u0000\u0000(\u0559\u0001\u0000\u0000\u0000*\u055b"+
		"\u0001\u0000\u0000\u0000,\u055f\u0001\u0000\u0000\u0000.\u056b\u0001\u0000"+
		"\u0000\u00000\u0570\u0001\u0000\u0000\u00002\u0577\u0001\u0000\u0000\u0000"+
		"4\u0579\u0001\u0000\u0000\u00006\u0581\u0001\u0000\u0000\u00008\u058a"+
		"\u0001\u0000\u0000\u0000:\u0595\u0001\u0000\u0000\u0000<\u05a7\u0001\u0000"+
		"\u0000\u0000>\u05aa\u0001\u0000\u0000\u0000@\u05b5\u0001\u0000\u0000\u0000"+
		"B\u05c5\u0001\u0000\u0000\u0000D\u05cb\u0001\u0000\u0000\u0000F\u05cd"+
		"\u0001\u0000\u0000\u0000H\u05d8\u0001\u0000\u0000\u0000J\u05e9\u0001\u0000"+
		"\u0000\u0000L\u05f0\u0001\u0000\u0000\u0000N\u05f2\u0001\u0000\u0000\u0000"+
		"P\u05f8\u0001\u0000\u0000\u0000R\u062e\u0001\u0000\u0000\u0000T\u063a"+
		"\u0001\u0000\u0000\u0000V\u066a\u0001\u0000\u0000\u0000X\u066d\u0001\u0000"+
		"\u0000\u0000Z\u0693\u0001\u0000\u0000\u0000\\\u0695\u0001\u0000\u0000"+
		"\u0000^\u069d\u0001\u0000\u0000\u0000`\u06be\u0001\u0000\u0000\u0000b"+
		"\u06ec\u0001\u0000\u0000\u0000d\u0701\u0001\u0000\u0000\u0000f\u0721\u0001"+
		"\u0000\u0000\u0000h\u072d\u0001\u0000\u0000\u0000j\u0730\u0001\u0000\u0000"+
		"\u0000l\u0739\u0001\u0000\u0000\u0000n\u074a\u0001\u0000\u0000\u0000p"+
		"\u075e\u0001\u0000\u0000\u0000r\u0760\u0001\u0000\u0000\u0000t\u0768\u0001"+
		"\u0000\u0000\u0000v\u076c\u0001\u0000\u0000\u0000x\u076f\u0001\u0000\u0000"+
		"\u0000z\u0772\u0001\u0000\u0000\u0000|\u078c\u0001\u0000\u0000\u0000~"+
		"\u078e\u0001\u0000\u0000\u0000\u0080\u07c7\u0001\u0000\u0000\u0000\u0082"+
		"\u07cb\u0001\u0000\u0000\u0000\u0084\u07e6\u0001\u0000\u0000\u0000\u0086"+
		"\u07ea\u0001\u0000\u0000\u0000\u0088\u07f9\u0001\u0000\u0000\u0000\u008a"+
		"\u07fb\u0001\u0000\u0000\u0000\u008c\u0819\u0001\u0000\u0000\u0000\u008e"+
		"\u081b\u0001\u0000\u0000\u0000\u0090\u0822\u0001\u0000\u0000\u0000\u0092"+
		"\u0842\u0001\u0000\u0000\u0000\u0094\u0845\u0001\u0000\u0000\u0000\u0096"+
		"\u085f\u0001\u0000\u0000\u0000\u0098\u0879\u0001\u0000\u0000\u0000\u009a"+
		"\u087f\u0001\u0000\u0000\u0000\u009c\u0881\u0001\u0000\u0000\u0000\u009e"+
		"\u08a0\u0001\u0000\u0000\u0000\u00a0\u08a2\u0001\u0000\u0000\u0000\u00a2"+
		"\u08a6\u0001\u0000\u0000\u0000\u00a4\u08ae\u0001\u0000\u0000\u0000\u00a6"+
		"\u08b9\u0001\u0000\u0000\u0000\u00a8\u08bd\u0001\u0000\u0000\u0000\u00aa"+
		"\u08c8\u0001\u0000\u0000\u0000\u00ac\u08e4\u0001\u0000\u0000\u0000\u00ae"+
		"\u08e6\u0001\u0000\u0000\u0000\u00b0\u08f1\u0001\u0000\u0000\u0000\u00b2"+
		"\u0907\u0001\u0000\u0000\u0000\u00b4\u093a\u0001\u0000\u0000\u0000\u00b6"+
		"\u093c\u0001\u0000\u0000\u0000\u00b8\u0944\u0001\u0000\u0000\u0000\u00ba"+
		"\u094f\u0001\u0000\u0000\u0000\u00bc\u0956\u0001\u0000\u0000\u0000\u00be"+
		"\u095a\u0001\u0000\u0000\u0000\u00c0\u0964\u0001\u0000\u0000\u0000\u00c2"+
		"\u096c\u0001\u0000\u0000\u0000\u00c4\u0979\u0001\u0000\u0000\u0000\u00c6"+
		"\u0988\u0001\u0000\u0000\u0000\u00c8\u098c\u0001\u0000\u0000\u0000\u00ca"+
		"\u098e\u0001\u0000\u0000\u0000\u00cc\u0990\u0001\u0000\u0000\u0000\u00ce"+
		"\u09a4\u0001\u0000\u0000\u0000\u00d0\u0a03\u0001\u0000\u0000\u0000\u00d2"+
		"\u0a09\u0001\u0000\u0000\u0000\u00d4\u0adf\u0001\u0000\u0000\u0000\u00d6"+
		"\u0afa\u0001\u0000\u0000\u0000\u00d8\u0afc\u0001\u0000\u0000\u0000\u00da"+
		"\u0afe\u0001\u0000\u0000\u0000\u00dc\u0b00\u0001\u0000\u0000\u0000\u00de"+
		"\u0b02\u0001\u0000\u0000\u0000\u00e0\u0b04\u0001\u0000\u0000\u0000\u00e2"+
		"\u0b09\u0001\u0000\u0000\u0000\u00e4\u0b10\u0001\u0000\u0000\u0000\u00e6"+
		"\u0b14\u0001\u0000\u0000\u0000\u00e8\u0b19\u0001\u0000\u0000\u0000\u00ea"+
		"\u0b1f\u0001\u0000\u0000\u0000\u00ec\u0b26\u0001\u0000\u0000\u0000\u00ee"+
		"\u0b56\u0001\u0000\u0000\u0000\u00f0\u0b58\u0001\u0000\u0000\u0000\u00f2"+
		"\u0b60\u0001\u0000\u0000\u0000\u00f4\u0b6c\u0001\u0000\u0000\u0000\u00f6"+
		"\u0b74\u0001\u0000\u0000\u0000\u00f8\u0b7d\u0001\u0000\u0000\u0000\u00fa"+
		"\u0b85\u0001\u0000\u0000\u0000\u00fc\u0b91\u0001\u0000\u0000\u0000\u00fe"+
		"\u0b96\u0001\u0000\u0000\u0000\u0100\u0b9f\u0001\u0000\u0000\u0000\u0102"+
		"\u0bd1\u0001\u0000\u0000\u0000\u0104\u0be3\u0001\u0000\u0000\u0000\u0106"+
		"\u0bec\u0001\u0000\u0000\u0000\u0108\u0bee\u0001\u0000\u0000\u0000\u010a"+
		"\u0bfa\u0001\u0000\u0000\u0000\u010c\u0bfc\u0001\u0000\u0000\u0000\u010e"+
		"\u0c04\u0001\u0000\u0000\u0000\u0110\u0c0e\u0001\u0000\u0000\u0000\u0112"+
		"\u0c13\u0001\u0000\u0000\u0000\u0114\u0c1b\u0001\u0000\u0000\u0000\u0116"+
		"\u0c1d\u0001\u0000\u0000\u0000\u0118\u0c4a\u0001\u0000\u0000\u0000\u011a"+
		"\u0c53\u0001\u0000\u0000\u0000\u011c\u0c55\u0001\u0000\u0000\u0000\u011e"+
		"\u0c57\u0001\u0000\u0000\u0000\u0120\u0c59\u0001\u0000\u0000\u0000\u0122"+
		"\u0123\u0003\u0002\u0001\u0000\u0123\u0001\u0001\u0000\u0000\u0000\u0124"+
		"\u0125\u0005\r\u0000\u0000\u0125\u0126\u0003\u00b8\\\u0000\u0126\u0127"+
		"\u0005\u0011\u0000\u0000\u0127\u0128\u0005\u000e\u0000\u0000\u0128\u0129"+
		"\u0003\u00b8\\\u0000\u0129\u0133\u0001\u0000\u0000\u0000\u012a\u012b\u0005"+
		"\u000f\u0000\u0000\u012b\u012c\u0005\u0010\u0000\u0000\u012c\u012d\u0003"+
		"\u00b8\\\u0000\u012d\u012e\u0005\u0012\u0000\u0000\u012e\u012f\u0005\u0013"+
		"\u0000\u0000\u012f\u0130\u0005\u0014\u0000\u0000\u0130\u0131\u0005\u0015"+
		"\u0000\u0000\u0131\u0133\u0001\u0000\u0000\u0000\u0132\u0124\u0001\u0000"+
		"\u0000\u0000\u0132\u012a\u0001\u0000\u0000\u0000\u0133\u0003\u0001\u0000"+
		"\u0000\u0000\u0134\u0138\u0003\u0012\t\u0000\u0135\u0137\u0005\u0001\u0000"+
		"\u0000\u0136\u0135\u0001\u0000\u0000\u0000\u0137\u013a\u0001\u0000\u0000"+
		"\u0000\u0138\u0136\u0001\u0000\u0000\u0000\u0138\u0139\u0001\u0000\u0000"+
		"\u0000\u0139\u013b\u0001\u0000\u0000\u0000\u013a\u0138\u0001\u0000\u0000"+
		"\u0000\u013b\u013c\u0005\u0000\u0000\u0001\u013c\u0005\u0001\u0000\u0000"+
		"\u0000\u013d\u013e\u0003\u00be_\u0000\u013e\u013f\u0005\u0000\u0000\u0001"+
		"\u013f\u0007\u0001\u0000\u0000\u0000\u0140\u0141\u0003\u00ba]\u0000\u0141"+
		"\u0142\u0005\u0000\u0000\u0001\u0142\t\u0001\u0000\u0000\u0000\u0143\u0144"+
		"\u0003\u00b8\\\u0000\u0144\u0145\u0005\u0000\u0000\u0001\u0145\u000b\u0001"+
		"\u0000\u0000\u0000\u0146\u0147\u0003\u00bc^\u0000\u0147\u0148\u0005\u0000"+
		"\u0000\u0001\u0148\r\u0001\u0000\u0000\u0000\u0149\u014a\u0003\u00eew"+
		"\u0000\u014a\u014b\u0005\u0000\u0000\u0001\u014b\u000f\u0001\u0000\u0000"+
		"\u0000\u014c\u014d\u0003\u00f4z\u0000\u014d\u014e\u0005\u0000\u0000\u0001"+
		"\u014e\u0011\u0001\u0000\u0000\u0000\u014f\u0431\u0003&\u0013\u0000\u0150"+
		"\u0152\u00036\u001b\u0000\u0151\u0150\u0001\u0000\u0000\u0000\u0151\u0152"+
		"\u0001\u0000\u0000\u0000\u0152\u0153\u0001\u0000\u0000\u0000\u0153\u0431"+
		"\u0003R)\u0000\u0154\u0156\u0005\u0109\u0000\u0000\u0155\u0157\u0005\u009f"+
		"\u0000\u0000\u0156\u0155\u0001\u0000\u0000\u0000\u0156\u0157\u0001\u0000"+
		"\u0000\u0000\u0157\u0158\u0001\u0000\u0000\u0000\u0158\u0431\u0003\u00b8"+
		"\\\u0000\u0159\u015a\u0005?\u0000\u0000\u015a\u015e\u00030\u0018\u0000"+
		"\u015b\u015c\u0005x\u0000\u0000\u015c\u015d\u0005\u00a3\u0000\u0000\u015d"+
		"\u015f\u0005]\u0000\u0000\u015e\u015b\u0001\u0000\u0000\u0000\u015e\u015f"+
		"\u0001\u0000\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000\u0160\u0168"+
		"\u0003\u00b8\\\u0000\u0161\u0167\u0003$\u0012\u0000\u0162\u0167\u0003"+
		"\"\u0011\u0000\u0163\u0164\u0005\u0111\u0000\u0000\u0164\u0165\u0007\u0000"+
		"\u0000\u0000\u0165\u0167\u0003>\u001f\u0000\u0166\u0161\u0001\u0000\u0000"+
		"\u0000\u0166\u0162\u0001\u0000\u0000\u0000\u0166\u0163\u0001\u0000\u0000"+
		"\u0000\u0167\u016a\u0001\u0000\u0000\u0000\u0168\u0166\u0001\u0000\u0000"+
		"\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u0431\u0001\u0000\u0000"+
		"\u0000\u016a\u0168\u0001\u0000\u0000\u0000\u016b\u016c\u0005\u0019\u0000"+
		"\u0000\u016c\u016d\u00030\u0018\u0000\u016d\u016e\u0003\u00b8\\\u0000"+
		"\u016e\u016f\u0005\u00df\u0000\u0000\u016f\u0170\u0007\u0000\u0000\u0000"+
		"\u0170\u0171\u0003>\u001f\u0000\u0171\u0431\u0001\u0000\u0000\u0000\u0172"+
		"\u0173\u0005\u0019\u0000\u0000\u0173\u0174\u00030\u0018\u0000\u0174\u0175"+
		"\u0003\u00b8\\\u0000\u0175\u0176\u0005\u00df\u0000\u0000\u0176\u0177\u0003"+
		"\"\u0011\u0000\u0177\u0431\u0001\u0000\u0000\u0000\u0178\u0179\u0005V"+
		"\u0000\u0000\u0179\u017c\u00030\u0018\u0000\u017a\u017b\u0005x\u0000\u0000"+
		"\u017b\u017d\u0005]\u0000\u0000\u017c\u017a\u0001\u0000\u0000\u0000\u017c"+
		"\u017d\u0001\u0000\u0000\u0000\u017d\u017e\u0001\u0000\u0000\u0000\u017e"+
		"\u0180\u0003\u00b8\\\u0000\u017f\u0181\u0007\u0001\u0000\u0000\u0180\u017f"+
		"\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000\u0000\u0000\u0181\u0431"+
		"\u0001\u0000\u0000\u0000\u0182\u0183\u0005\u00e2\u0000\u0000\u0183\u0186"+
		"\u0007\u0002\u0000\u0000\u0184\u0185\u0007\u0003\u0000\u0000\u0185\u0187"+
		"\u0003\u00b8\\\u0000\u0186\u0184\u0001\u0000\u0000\u0000\u0186\u0187\u0001"+
		"\u0000\u0000\u0000\u0187\u018c\u0001\u0000\u0000\u0000\u0188\u018a\u0005"+
		"\u008e\u0000\u0000\u0189\u0188\u0001\u0000\u0000\u0000\u0189\u018a\u0001"+
		"\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000\u018b\u018d\u0005"+
		"\u0126\u0000\u0000\u018c\u0189\u0001\u0000\u0000\u0000\u018c\u018d\u0001"+
		"\u0000\u0000\u0000\u018d\u0431\u0001\u0000\u0000\u0000\u018e\u0193\u0003"+
		"\u001a\r\u0000\u018f\u0190\u0005\u0002\u0000\u0000\u0190\u0191\u0003\u00f4"+
		"z\u0000\u0191\u0192\u0005\u0003\u0000\u0000\u0192\u0194\u0001\u0000\u0000"+
		"\u0000\u0193\u018f\u0001\u0000\u0000\u0000\u0193\u0194\u0001\u0000\u0000"+
		"\u0000\u0194\u0196\u0001\u0000\u0000\u0000\u0195\u0197\u0003:\u001d\u0000"+
		"\u0196\u0195\u0001\u0000\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000"+
		"\u0197\u0198\u0001\u0000\u0000\u0000\u0198\u019d\u0003<\u001e\u0000\u0199"+
		"\u019b\u0005 \u0000\u0000\u019a\u0199\u0001\u0000\u0000\u0000\u019a\u019b"+
		"\u0001\u0000\u0000\u0000\u019b\u019c\u0001\u0000\u0000\u0000\u019c\u019e"+
		"\u0003&\u0013\u0000\u019d\u019a\u0001\u0000\u0000\u0000\u019d\u019e\u0001"+
		"\u0000\u0000\u0000\u019e\u0431\u0001\u0000\u0000\u0000\u019f\u01a0\u0005"+
		"?\u0000\u0000\u01a0\u01a4\u0005\u0010\u0000\u0000\u01a1\u01a2\u0005x\u0000"+
		"\u0000\u01a2\u01a3\u0005\u00a3\u0000\u0000\u01a3\u01a5\u0005]\u0000\u0000"+
		"\u01a4\u01a1\u0001\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000"+
		"\u01a5\u01a6\u0001\u0000\u0000\u0000\u01a6\u01a7\u0003\u00ba]\u0000\u01a7"+
		"\u01a8\u0005\u008e\u0000\u0000\u01a8\u01b1\u0003\u00ba]\u0000\u01a9\u01b0"+
		"\u0003:\u001d\u0000\u01aa\u01b0\u0003\u00b4Z\u0000\u01ab\u01b0\u0003J"+
		"%\u0000\u01ac\u01b0\u0003\"\u0011\u0000\u01ad\u01ae\u0005\u00f1\u0000"+
		"\u0000\u01ae\u01b0\u0003>\u001f\u0000\u01af\u01a9\u0001\u0000\u0000\u0000"+
		"\u01af\u01aa\u0001\u0000\u0000\u0000\u01af\u01ab\u0001\u0000\u0000\u0000"+
		"\u01af\u01ac\u0001\u0000\u0000\u0000\u01af\u01ad\u0001\u0000\u0000\u0000"+
		"\u01b0\u01b3\u0001\u0000\u0000\u0000\u01b1\u01af\u0001\u0000\u0000\u0000"+
		"\u01b1\u01b2\u0001\u0000\u0000\u0000\u01b2\u0431\u0001\u0000\u0000\u0000"+
		"\u01b3\u01b1\u0001\u0000\u0000\u0000\u01b4\u01b9\u0003\u001c\u000e\u0000"+
		"\u01b5\u01b6\u0005\u0002\u0000\u0000\u01b6\u01b7\u0003\u00f4z\u0000\u01b7"+
		"\u01b8\u0005\u0003\u0000\u0000\u01b8\u01ba\u0001\u0000\u0000\u0000\u01b9"+
		"\u01b5\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001\u0000\u0000\u0000\u01ba"+
		"\u01bc\u0001\u0000\u0000\u0000\u01bb\u01bd\u0003:\u001d\u0000\u01bc\u01bb"+
		"\u0001\u0000\u0000\u0000\u01bc\u01bd\u0001\u0000\u0000\u0000\u01bd\u01be"+
		"\u0001\u0000\u0000\u0000\u01be\u01c3\u0003<\u001e\u0000\u01bf\u01c1\u0005"+
		" \u0000\u0000\u01c0\u01bf\u0001\u0000\u0000\u0000\u01c0\u01c1\u0001\u0000"+
		"\u0000\u0000\u01c1\u01c2\u0001\u0000\u0000\u0000\u01c2\u01c4\u0003&\u0013"+
		"\u0000\u01c3\u01c0\u0001\u0000\u0000\u0000\u01c3\u01c4\u0001\u0000\u0000"+
		"\u0000\u01c4\u0431\u0001\u0000\u0000\u0000\u01c5\u01c6\u0005\u001a\u0000"+
		"\u0000\u01c6\u01c7\u0005\u0010\u0000\u0000\u01c7\u01c9\u0003\u00b8\\\u0000"+
		"\u01c8\u01ca\u0003,\u0016\u0000\u01c9\u01c8\u0001\u0000\u0000\u0000\u01c9"+
		"\u01ca\u0001\u0000\u0000\u0000\u01ca\u01cb\u0001\u0000\u0000\u0000\u01cb"+
		"\u01cc\u0005;\u0000\u0000\u01cc\u01d4\u0005\u00e8\u0000\u0000\u01cd\u01d5"+
		"\u0003\u0112\u0089\u0000\u01ce\u01cf\u0005j\u0000\u0000\u01cf\u01d0\u0005"+
		"6\u0000\u0000\u01d0\u01d5\u0003\u00a2Q\u0000\u01d1\u01d2\u0005j\u0000"+
		"\u0000\u01d2\u01d3\u0005\u0018\u0000\u0000\u01d3\u01d5\u00056\u0000\u0000"+
		"\u01d4\u01cd\u0001\u0000\u0000\u0000\u01d4\u01ce\u0001\u0000\u0000\u0000"+
		"\u01d4\u01d1\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001\u0000\u0000\u0000"+
		"\u01d5\u0431\u0001\u0000\u0000\u0000\u01d6\u01d7\u0005\u001a\u0000\u0000"+
		"\u01d7\u01da\u0005\u00ef\u0000\u0000\u01d8\u01d9\u0007\u0003\u0000\u0000"+
		"\u01d9\u01db\u0003\u00b8\\\u0000\u01da\u01d8\u0001\u0000\u0000\u0000\u01da"+
		"\u01db\u0001\u0000\u0000\u0000\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc"+
		"\u01dd\u0005;\u0000\u0000\u01dd\u01df\u0005\u00e8\u0000\u0000\u01de\u01e0"+
		"\u0003\u0112\u0089\u0000\u01df\u01de\u0001\u0000\u0000\u0000\u01df\u01e0"+
		"\u0001\u0000\u0000\u0000\u01e0\u0431\u0001\u0000\u0000\u0000\u01e1\u01e2"+
		"\u0005\u0019\u0000\u0000\u01e2\u01e3\u0005\u0010\u0000\u0000\u01e3\u01e4"+
		"\u0003\u00b8\\\u0000\u01e4\u01e5\u0005\u0016\u0000\u0000\u01e5\u01e6\u0007"+
		"\u0004\u0000\u0000\u01e6\u01e7\u0003\u00f0x\u0000\u01e7\u0431\u0001\u0000"+
		"\u0000\u0000\u01e8\u01e9\u0005\u0019\u0000\u0000\u01e9\u01ea\u0005\u0010"+
		"\u0000\u0000\u01ea\u01eb\u0003\u00b8\\\u0000\u01eb\u01ec\u0005\u0016\u0000"+
		"\u0000\u01ec\u01ed\u0007\u0004\u0000\u0000\u01ed\u01ee\u0005\u0002\u0000"+
		"\u0000\u01ee\u01ef\u0003\u00f0x\u0000\u01ef\u01f0\u0005\u0003\u0000\u0000"+
		"\u01f0\u0431\u0001\u0000\u0000\u0000\u01f1\u01f2\u0005\u0019\u0000\u0000"+
		"\u01f2\u01f3\u0005\u0010\u0000\u0000\u01f3\u01f4\u0003\u00b8\\\u0000\u01f4"+
		"\u01f5\u0005\u00c8\u0000\u0000\u01f5\u01f6\u00055\u0000\u0000\u01f6\u01f7"+
		"\u0003\u00b8\\\u0000\u01f7\u01f8\u0005\u0011\u0000\u0000\u01f8\u01f9\u0003"+
		"\u010e\u0087\u0000\u01f9\u0431\u0001\u0000\u0000\u0000\u01fa\u01fb\u0005"+
		"\u0019\u0000\u0000\u01fb\u01fc\u0005\u0010\u0000\u0000\u01fc\u01fd\u0003"+
		"\u00b8\\\u0000\u01fd\u01fe\u0005V\u0000\u0000\u01fe\u01ff\u0007\u0004"+
		"\u0000\u0000\u01ff\u0200\u0005\u0002\u0000\u0000\u0200\u0201\u0003\u00b6"+
		"[\u0000\u0201\u0202\u0005\u0003\u0000\u0000\u0202\u0431\u0001\u0000\u0000"+
		"\u0000\u0203\u0204\u0005\u0019\u0000\u0000\u0204\u0205\u0005\u0010\u0000"+
		"\u0000\u0205\u0206\u0003\u00b8\\\u0000\u0206\u0207\u0005V\u0000\u0000"+
		"\u0207\u0208\u0007\u0004\u0000\u0000\u0208\u0209\u0003\u00b6[\u0000\u0209"+
		"\u0431\u0001\u0000\u0000\u0000\u020a\u020b\u0005\u0019\u0000\u0000\u020b"+
		"\u020c\u0007\u0005\u0000\u0000\u020c\u020d\u0003\u00b8\\\u0000\u020d\u020e"+
		"\u0005\u00c8\u0000\u0000\u020e\u020f\u0005\u0011\u0000\u0000\u020f\u0210"+
		"\u0003\u00b8\\\u0000\u0210\u0431\u0001\u0000\u0000\u0000\u0211\u0212\u0005"+
		"\u0019\u0000\u0000\u0212\u0213\u0007\u0005\u0000\u0000\u0213\u0214\u0003"+
		"\u00b8\\\u0000\u0214\u0215\u0005\u00df\u0000\u0000\u0215\u0216\u0005\u00f1"+
		"\u0000\u0000\u0216\u0217\u0003>\u001f\u0000\u0217\u0431\u0001\u0000\u0000"+
		"\u0000\u0218\u0219\u0005\u0019\u0000\u0000\u0219\u021a\u0007\u0005\u0000"+
		"\u0000\u021a\u021b\u0003\u00b8\\\u0000\u021b\u021c\u0005\u0107\u0000\u0000"+
		"\u021c\u021f\u0005\u00f1\u0000\u0000\u021d\u021e\u0005x\u0000\u0000\u021e"+
		"\u0220\u0005]\u0000\u0000\u021f\u021d\u0001\u0000\u0000\u0000\u021f\u0220"+
		"\u0001\u0000\u0000\u0000\u0220\u0221\u0001\u0000\u0000\u0000\u0221\u0222"+
		"\u0003>\u001f\u0000\u0222\u0431\u0001\u0000\u0000\u0000\u0223\u0224\u0005"+
		"\u0019\u0000\u0000\u0224\u0225\u0005\u0010\u0000\u0000\u0225\u0226\u0003"+
		"\u00b8\\\u0000\u0226\u0228\u0007\u0006\u0000\u0000\u0227\u0229\u00055"+
		"\u0000\u0000\u0228\u0227\u0001\u0000\u0000\u0000\u0228\u0229\u0001\u0000"+
		"\u0000\u0000\u0229\u022a\u0001\u0000\u0000\u0000\u022a\u022c\u0003\u00b8"+
		"\\\u0000\u022b\u022d\u0003\u011a\u008d\u0000\u022c\u022b\u0001\u0000\u0000"+
		"\u0000\u022c\u022d\u0001\u0000\u0000\u0000\u022d\u0431\u0001\u0000\u0000"+
		"\u0000\u022e\u022f\u0005\u0019\u0000\u0000\u022f\u0230\u0005\u0010\u0000"+
		"\u0000\u0230\u0232\u0003\u00b8\\\u0000\u0231\u0233\u0003,\u0016\u0000"+
		"\u0232\u0231\u0001\u0000\u0000\u0000\u0232\u0233\u0001\u0000\u0000\u0000"+
		"\u0233\u0234\u0001\u0000\u0000\u0000\u0234\u0236\u0005-\u0000\u0000\u0235"+
		"\u0237\u00055\u0000\u0000\u0236\u0235\u0001\u0000\u0000\u0000\u0236\u0237"+
		"\u0001\u0000\u0000\u0000\u0237\u0238\u0001\u0000\u0000\u0000\u0238\u0239"+
		"\u0003\u00b8\\\u0000\u0239\u023b\u0003\u00f6{\u0000\u023a\u023c\u0003"+
		"\u00ecv\u0000\u023b\u023a\u0001\u0000\u0000\u0000\u023b\u023c\u0001\u0000"+
		"\u0000\u0000\u023c\u0431\u0001\u0000\u0000\u0000\u023d\u023e\u0005\u0019"+
		"\u0000\u0000\u023e\u023f\u0005\u0010\u0000\u0000\u023f\u0241\u0003\u00b8"+
		"\\\u0000\u0240\u0242\u0003,\u0016\u0000\u0241\u0240\u0001\u0000\u0000"+
		"\u0000\u0241\u0242\u0001\u0000\u0000\u0000\u0242\u0243\u0001\u0000\u0000"+
		"\u0000\u0243\u0244\u0005\u00ca\u0000\u0000\u0244\u0245\u00056\u0000\u0000"+
		"\u0245\u0246\u0005\u0002\u0000\u0000\u0246\u0247\u0003\u00f0x\u0000\u0247"+
		"\u0248\u0005\u0003\u0000\u0000\u0248\u0431\u0001\u0000\u0000\u0000\u0249"+
		"\u024a\u0005\u0019\u0000\u0000\u024a\u024b\u0005\u0010\u0000\u0000\u024b"+
		"\u024d\u0003\u00b8\\\u0000\u024c\u024e\u0003,\u0016\u0000\u024d\u024c"+
		"\u0001\u0000\u0000\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024e\u024f"+
		"\u0001\u0000\u0000\u0000\u024f\u0250\u0005\u00df\u0000\u0000\u0250\u0251"+
		"\u0005\u00dc\u0000\u0000\u0251\u0255\u0005\u0126\u0000\u0000\u0252\u0253"+
		"\u0005\u0111\u0000\u0000\u0253\u0254\u0005\u00dd\u0000\u0000\u0254\u0256"+
		"\u0003>\u001f\u0000\u0255\u0252\u0001\u0000\u0000\u0000\u0255\u0256\u0001"+
		"\u0000\u0000\u0000\u0256\u0431\u0001\u0000\u0000\u0000\u0257\u0258\u0005"+
		"\u0019\u0000\u0000\u0258\u0259\u0005\u0010\u0000\u0000\u0259\u025b\u0003"+
		"\u00b8\\\u0000\u025a\u025c\u0003,\u0016\u0000\u025b\u025a\u0001\u0000"+
		"\u0000\u0000\u025b\u025c\u0001\u0000\u0000\u0000\u025c\u025d\u0001\u0000"+
		"\u0000\u0000\u025d\u025e\u0005\u00df\u0000\u0000\u025e\u025f\u0005\u00dd"+
		"\u0000\u0000\u025f\u0260\u0003>\u001f\u0000\u0260\u0431\u0001\u0000\u0000"+
		"\u0000\u0261\u0262\u0005\u0019\u0000\u0000\u0262\u0263\u0007\u0005\u0000"+
		"\u0000\u0263\u0264\u0003\u00b8\\\u0000\u0264\u0268\u0005\u0016\u0000\u0000"+
		"\u0265\u0266\u0005x\u0000\u0000\u0266\u0267\u0005\u00a3\u0000\u0000\u0267"+
		"\u0269\u0005]\u0000\u0000\u0268\u0265\u0001\u0000\u0000\u0000\u0268\u0269"+
		"\u0001\u0000\u0000\u0000\u0269\u026b\u0001\u0000\u0000\u0000\u026a\u026c"+
		"\u0003*\u0015\u0000\u026b\u026a\u0001\u0000\u0000\u0000\u026c\u026d\u0001"+
		"\u0000\u0000\u0000\u026d\u026b\u0001\u0000\u0000\u0000\u026d\u026e\u0001"+
		"\u0000\u0000\u0000\u026e\u0431\u0001\u0000\u0000\u0000\u026f\u0270\u0005"+
		"\u0019\u0000\u0000\u0270\u0271\u0005\u0010\u0000\u0000\u0271\u0272\u0003"+
		"\u00b8\\\u0000\u0272\u0273\u0003,\u0016\u0000\u0273\u0274\u0005\u00c8"+
		"\u0000\u0000\u0274\u0275\u0005\u0011\u0000\u0000\u0275\u0276\u0003,\u0016"+
		"\u0000\u0276\u0431\u0001\u0000\u0000\u0000\u0277\u0278\u0005\u0019\u0000"+
		"\u0000\u0278\u0279\u0007\u0005\u0000\u0000\u0279\u027a\u0003\u00b8\\\u0000"+
		"\u027a\u027d\u0005V\u0000\u0000\u027b\u027c\u0005x\u0000\u0000\u027c\u027e"+
		"\u0005]\u0000\u0000\u027d\u027b\u0001\u0000\u0000\u0000\u027d\u027e\u0001"+
		"\u0000\u0000\u0000\u027e\u027f\u0001\u0000\u0000\u0000\u027f\u0284\u0003"+
		",\u0016\u0000\u0280\u0281\u0005\u0004\u0000\u0000\u0281\u0283\u0003,\u0016"+
		"\u0000\u0282\u0280\u0001\u0000\u0000\u0000\u0283\u0286\u0001\u0000\u0000"+
		"\u0000\u0284\u0282\u0001\u0000\u0000\u0000\u0284\u0285\u0001\u0000\u0000"+
		"\u0000\u0285\u0288\u0001\u0000\u0000\u0000\u0286\u0284\u0001\u0000\u0000"+
		"\u0000\u0287\u0289\u0005\u00bf\u0000\u0000\u0288\u0287\u0001\u0000\u0000"+
		"\u0000\u0288\u0289\u0001\u0000\u0000\u0000\u0289\u0431\u0001\u0000\u0000"+
		"\u0000\u028a\u028b\u0005\u0019\u0000\u0000\u028b\u028c\u0005\u0010\u0000"+
		"\u0000\u028c\u028e\u0003\u00b8\\\u0000\u028d\u028f\u0003,\u0016\u0000"+
		"\u028e\u028d\u0001\u0000\u0000\u0000\u028e\u028f\u0001\u0000\u0000\u0000"+
		"\u028f\u0290\u0001\u0000\u0000\u0000\u0290\u0291\u0005\u00df\u0000\u0000"+
		"\u0291\u0292\u0003\"\u0011\u0000\u0292\u0431\u0001\u0000\u0000\u0000\u0293"+
		"\u0294\u0005\u0019\u0000\u0000\u0294\u0295\u0005\u0010\u0000\u0000\u0295"+
		"\u0296\u0003\u00b8\\\u0000\u0296\u0297\u0005\u00c4\u0000\u0000\u0297\u0298"+
		"\u0005\u00b6\u0000\u0000\u0298\u0431\u0001\u0000\u0000\u0000\u0299\u029a"+
		"\u0005V\u0000\u0000\u029a\u029d\u0005\u0010\u0000\u0000\u029b\u029c\u0005"+
		"x\u0000\u0000\u029c\u029e\u0005]\u0000\u0000\u029d\u029b\u0001\u0000\u0000"+
		"\u0000\u029d\u029e\u0001\u0000\u0000\u0000\u029e\u029f\u0001\u0000\u0000"+
		"\u0000\u029f\u02a1\u0003\u00b8\\\u0000\u02a0\u02a2\u0005\u00bf\u0000\u0000"+
		"\u02a1\u02a0\u0001\u0000\u0000\u0000\u02a1\u02a2\u0001\u0000\u0000\u0000"+
		"\u02a2\u0431\u0001\u0000\u0000\u0000\u02a3\u02a4\u0005V\u0000\u0000\u02a4"+
		"\u02a7\u0005\u010c\u0000\u0000\u02a5\u02a6\u0005x\u0000\u0000\u02a6\u02a8"+
		"\u0005]\u0000\u0000\u02a7\u02a5\u0001\u0000\u0000\u0000\u02a7\u02a8\u0001"+
		"\u0000\u0000\u0000\u02a8\u02a9\u0001\u0000\u0000\u0000\u02a9\u0431\u0003"+
		"\u00b8\\\u0000\u02aa\u02ad\u0005?\u0000\u0000\u02ab\u02ac\u0005\u00ab"+
		"\u0000\u0000\u02ac\u02ae\u0005\u00ca\u0000\u0000\u02ad\u02ab\u0001\u0000"+
		"\u0000\u0000\u02ad\u02ae\u0001\u0000\u0000\u0000\u02ae\u02b3\u0001\u0000"+
		"\u0000\u0000\u02af\u02b1\u0005r\u0000\u0000\u02b0\u02af\u0001\u0000\u0000"+
		"\u0000\u02b0\u02b1\u0001\u0000\u0000\u0000\u02b1\u02b2\u0001\u0000\u0000"+
		"\u0000\u02b2\u02b4\u0005\u00f2\u0000\u0000\u02b3\u02b0\u0001\u0000\u0000"+
		"\u0000\u02b3\u02b4\u0001\u0000\u0000\u0000\u02b4\u02b5\u0001\u0000\u0000"+
		"\u0000\u02b5\u02b9\u0005\u010c\u0000\u0000\u02b6\u02b7\u0005x\u0000\u0000"+
		"\u02b7\u02b8\u0005\u00a3\u0000\u0000\u02b8\u02ba\u0005]\u0000\u0000\u02b9"+
		"\u02b6\u0001\u0000\u0000\u0000\u02b9\u02ba\u0001\u0000\u0000\u0000\u02ba"+
		"\u02bb\u0001\u0000\u0000\u0000\u02bb\u02bd\u0003\u00b8\\\u0000\u02bc\u02be"+
		"\u0003\u00a8T\u0000\u02bd\u02bc\u0001\u0000\u0000\u0000\u02bd\u02be\u0001"+
		"\u0000\u0000\u0000\u02be\u02c7\u0001\u0000\u0000\u0000\u02bf\u02c6\u0003"+
		"$\u0012\u0000\u02c0\u02c1\u0005\u00b5\u0000\u0000\u02c1\u02c2\u0005\u00a7"+
		"\u0000\u0000\u02c2\u02c6\u0003\u00a0P\u0000\u02c3\u02c4\u0005\u00f1\u0000"+
		"\u0000\u02c4\u02c6\u0003>\u001f\u0000\u02c5\u02bf\u0001\u0000\u0000\u0000"+
		"\u02c5\u02c0\u0001\u0000\u0000\u0000\u02c5\u02c3\u0001\u0000\u0000\u0000"+
		"\u02c6\u02c9\u0001\u0000\u0000\u0000\u02c7\u02c5\u0001\u0000\u0000\u0000"+
		"\u02c7\u02c8\u0001\u0000\u0000\u0000\u02c8\u02ca\u0001\u0000\u0000\u0000"+
		"\u02c9\u02c7\u0001\u0000\u0000\u0000\u02ca\u02cb\u0005 \u0000\u0000\u02cb"+
		"\u02cc\u0003&\u0013\u0000\u02cc\u0431\u0001\u0000\u0000\u0000\u02cd\u02d0"+
		"\u0005?\u0000\u0000\u02ce\u02cf\u0005\u00ab\u0000\u0000\u02cf\u02d1\u0005"+
		"\u00ca\u0000\u0000\u02d0\u02ce\u0001\u0000\u0000\u0000\u02d0\u02d1\u0001"+
		"\u0000\u0000\u0000\u02d1\u02d3\u0001\u0000\u0000\u0000\u02d2\u02d4\u0005"+
		"r\u0000\u0000\u02d3\u02d2\u0001\u0000\u0000\u0000\u02d3\u02d4\u0001\u0000"+
		"\u0000\u0000\u02d4\u02d5\u0001\u0000\u0000\u0000\u02d5\u02d6\u0005\u00f2"+
		"\u0000\u0000\u02d6\u02d7\u0005\u010c\u0000\u0000\u02d7\u02dc\u0003\u00ba"+
		"]\u0000\u02d8\u02d9\u0005\u0002\u0000\u0000\u02d9\u02da\u0003\u00f4z\u0000"+
		"\u02da\u02db\u0005\u0003\u0000\u0000\u02db\u02dd\u0001\u0000\u0000\u0000"+
		"\u02dc\u02d8\u0001\u0000\u0000\u0000\u02dc\u02dd\u0001\u0000\u0000\u0000"+
		"\u02dd\u02de\u0001\u0000\u0000\u0000\u02de\u02e1\u0003:\u001d\u0000\u02df"+
		"\u02e0\u0005\u00aa\u0000\u0000\u02e0\u02e2\u0003>\u001f\u0000\u02e1\u02df"+
		"\u0001\u0000\u0000\u0000\u02e1\u02e2\u0001\u0000\u0000\u0000\u02e2\u0431"+
		"\u0001\u0000\u0000\u0000\u02e3\u02e4\u0005\u0019\u0000\u0000\u02e4\u02e5"+
		"\u0005\u010c\u0000\u0000\u02e5\u02e7\u0003\u00b8\\\u0000\u02e6\u02e8\u0005"+
		" \u0000\u0000\u02e7\u02e6\u0001\u0000\u0000\u0000\u02e7\u02e8\u0001\u0000"+
		"\u0000\u0000\u02e8\u02e9\u0001\u0000\u0000\u0000\u02e9\u02ea\u0003&\u0013"+
		"\u0000\u02ea\u0431\u0001\u0000\u0000\u0000\u02eb\u02ee\u0005?\u0000\u0000"+
		"\u02ec\u02ed\u0005\u00ab\u0000\u0000\u02ed\u02ef\u0005\u00ca\u0000\u0000"+
		"\u02ee\u02ec\u0001\u0000\u0000\u0000\u02ee\u02ef\u0001\u0000\u0000\u0000"+
		"\u02ef\u02f1\u0001\u0000\u0000\u0000\u02f0\u02f2\u0005\u00f2\u0000\u0000"+
		"\u02f1\u02f0\u0001\u0000\u0000\u0000\u02f1\u02f2\u0001\u0000\u0000\u0000"+
		"\u02f2\u02f3\u0001\u0000\u0000\u0000\u02f3\u02f7\u0005p\u0000\u0000\u02f4"+
		"\u02f5\u0005x\u0000\u0000\u02f5\u02f6\u0005\u00a3\u0000\u0000\u02f6\u02f8"+
		"\u0005]\u0000\u0000\u02f7\u02f4\u0001\u0000\u0000\u0000\u02f7\u02f8\u0001"+
		"\u0000\u0000\u0000\u02f8\u02f9\u0001\u0000\u0000\u0000\u02f9\u02fa\u0003"+
		"\u00b8\\\u0000\u02fa\u02fb\u0005 \u0000\u0000\u02fb\u0305\u0005\u0126"+
		"\u0000\u0000\u02fc\u02fd\u0005\u0014\u0000\u0000\u02fd\u0302\u0003P(\u0000"+
		"\u02fe\u02ff\u0005\u0004\u0000\u0000\u02ff\u0301\u0003P(\u0000\u0300\u02fe"+
		"\u0001\u0000\u0000\u0000\u0301\u0304\u0001\u0000\u0000\u0000\u0302\u0300"+
		"\u0001\u0000\u0000\u0000\u0302\u0303\u0001\u0000\u0000\u0000\u0303\u0306"+
		"\u0001\u0000\u0000\u0000\u0304\u0302\u0001\u0000\u0000\u0000\u0305\u02fc"+
		"\u0001\u0000\u0000\u0000\u0305\u0306\u0001\u0000\u0000\u0000\u0306\u0431"+
		"\u0001\u0000\u0000\u0000\u0307\u0309\u0005V\u0000\u0000\u0308\u030a\u0005"+
		"\u00f2\u0000\u0000\u0309\u0308\u0001\u0000\u0000\u0000\u0309\u030a\u0001"+
		"\u0000\u0000\u0000\u030a\u030b\u0001\u0000\u0000\u0000\u030b\u030e\u0005"+
		"p\u0000\u0000\u030c\u030d\u0005x\u0000\u0000\u030d\u030f\u0005]\u0000"+
		"\u0000\u030e\u030c\u0001\u0000\u0000\u0000\u030e\u030f\u0001\u0000\u0000"+
		"\u0000\u030f\u0310\u0001\u0000\u0000\u0000\u0310\u0431\u0003\u00b8\\\u0000"+
		"\u0311\u0313\u0005^\u0000\u0000\u0312\u0314\u0007\u0007\u0000\u0000\u0313"+
		"\u0312\u0001\u0000\u0000\u0000\u0313\u0314\u0001\u0000\u0000\u0000\u0314"+
		"\u0315\u0001\u0000\u0000\u0000\u0315\u0431\u0003\u0012\t\u0000\u0316\u0317"+
		"\u0005\u00e2\u0000\u0000\u0317\u031a\u0005\u00ef\u0000\u0000\u0318\u0319"+
		"\u0007\u0003\u0000\u0000\u0319\u031b\u0003\u00b8\\\u0000\u031a\u0318\u0001"+
		"\u0000\u0000\u0000\u031a\u031b\u0001\u0000\u0000\u0000\u031b\u0320\u0001"+
		"\u0000\u0000\u0000\u031c\u031e\u0005\u008e\u0000\u0000\u031d\u031c\u0001"+
		"\u0000\u0000\u0000\u031d\u031e\u0001\u0000\u0000\u0000\u031e\u031f\u0001"+
		"\u0000\u0000\u0000\u031f\u0321\u0005\u0126\u0000\u0000\u0320\u031d\u0001"+
		"\u0000\u0000\u0000\u0320\u0321\u0001\u0000\u0000\u0000\u0321\u0431\u0001"+
		"\u0000\u0000\u0000\u0322\u0323\u0005\u00e2\u0000\u0000\u0323\u0324\u0005"+
		"\u0010\u0000\u0000\u0324\u0327\u0005`\u0000\u0000\u0325\u0326\u0007\u0003"+
		"\u0000\u0000\u0326\u0328\u0003\u00b8\\\u0000\u0327\u0325\u0001\u0000\u0000"+
		"\u0000\u0327\u0328\u0001\u0000\u0000\u0000\u0328\u0329\u0001\u0000\u0000"+
		"\u0000\u0329\u032a\u0005\u008e\u0000\u0000\u032a\u032c\u0005\u0126\u0000"+
		"\u0000\u032b\u032d\u0003,\u0016\u0000\u032c\u032b\u0001\u0000\u0000\u0000"+
		"\u032c\u032d\u0001\u0000\u0000\u0000\u032d\u0431\u0001\u0000\u0000\u0000"+
		"\u032e\u032f\u0005\u00e2\u0000\u0000\u032f\u0330\u0005\u00f1\u0000\u0000"+
		"\u0330\u0335\u0003\u00b8\\\u0000\u0331\u0332\u0005\u0002\u0000\u0000\u0332"+
		"\u0333\u0003B!\u0000\u0333\u0334\u0005\u0003\u0000\u0000\u0334\u0336\u0001"+
		"\u0000\u0000\u0000\u0335\u0331\u0001\u0000\u0000\u0000\u0335\u0336\u0001"+
		"\u0000\u0000\u0000\u0336\u0431\u0001\u0000\u0000\u0000\u0337\u0338\u0005"+
		"\u00e2\u0000\u0000\u0338\u0339\u00056\u0000\u0000\u0339\u033a\u0007\u0003"+
		"\u0000\u0000\u033a\u033d\u0003\u00b8\\\u0000\u033b\u033c\u0007\u0003\u0000"+
		"\u0000\u033c\u033e\u0003\u00b8\\\u0000\u033d\u033b\u0001\u0000\u0000\u0000"+
		"\u033d\u033e\u0001\u0000\u0000\u0000\u033e\u0431\u0001\u0000\u0000\u0000"+
		"\u033f\u0340\u0005\u00e2\u0000\u0000\u0340\u0343\u0005\u010d\u0000\u0000"+
		"\u0341\u0342\u0007\u0003\u0000\u0000\u0342\u0344\u0003\u00b8\\\u0000\u0343"+
		"\u0341\u0001\u0000\u0000\u0000\u0343\u0344\u0001\u0000\u0000\u0000\u0344"+
		"\u0349\u0001\u0000\u0000\u0000\u0345\u0347\u0005\u008e\u0000\u0000\u0346"+
		"\u0345\u0001\u0000\u0000\u0000\u0346\u0347\u0001\u0000\u0000\u0000\u0347"+
		"\u0348\u0001\u0000\u0000\u0000\u0348\u034a\u0005\u0126\u0000\u0000\u0349"+
		"\u0346\u0001\u0000\u0000\u0000\u0349\u034a\u0001\u0000\u0000\u0000\u034a"+
		"\u0431\u0001\u0000\u0000\u0000\u034b\u034c\u0005\u00e2\u0000\u0000\u034c"+
		"\u034d\u0005\u00b6\u0000\u0000\u034d\u034f\u0003\u00b8\\\u0000\u034e\u0350"+
		"\u0003,\u0016\u0000\u034f\u034e\u0001\u0000\u0000\u0000\u034f\u0350\u0001"+
		"\u0000\u0000\u0000\u0350\u0431\u0001\u0000\u0000\u0000\u0351\u0353\u0005"+
		"\u00e2\u0000\u0000\u0352\u0354\u0003\u0112\u0089\u0000\u0353\u0352\u0001"+
		"\u0000\u0000\u0000\u0353\u0354\u0001\u0000\u0000\u0000\u0354\u0355\u0001"+
		"\u0000\u0000\u0000\u0355\u035d\u0005q\u0000\u0000\u0356\u0358\u0005\u008e"+
		"\u0000\u0000\u0357\u0356\u0001\u0000\u0000\u0000\u0357\u0358\u0001\u0000"+
		"\u0000\u0000\u0358\u035b\u0001\u0000\u0000\u0000\u0359\u035c\u0003\u00b8"+
		"\\\u0000\u035a\u035c\u0005\u0126\u0000\u0000\u035b\u0359\u0001\u0000\u0000"+
		"\u0000\u035b\u035a\u0001\u0000\u0000\u0000\u035c\u035e\u0001\u0000\u0000"+
		"\u0000\u035d\u0357\u0001\u0000\u0000\u0000\u035d\u035e\u0001\u0000\u0000"+
		"\u0000\u035e\u0431\u0001\u0000\u0000\u0000\u035f\u0360\u0005\u00e2\u0000"+
		"\u0000\u0360\u0361\u0005?\u0000\u0000\u0361\u0362\u0005\u0010\u0000\u0000"+
		"\u0362\u0365\u0003\u00b8\\\u0000\u0363\u0364\u0005 \u0000\u0000\u0364"+
		"\u0366\u0005\u00dc\u0000\u0000\u0365\u0363\u0001\u0000\u0000\u0000\u0365"+
		"\u0366\u0001\u0000\u0000\u0000\u0366\u0431\u0001\u0000\u0000\u0000\u0367"+
		"\u0368\u0005\u00e2\u0000\u0000\u0368\u0369\u0005B\u0000\u0000\u0369\u0431"+
		"\u0005\u009f\u0000\u0000\u036a\u036b\u0007\b\u0000\u0000\u036b\u036d\u0005"+
		"p\u0000\u0000\u036c\u036e\u0005`\u0000\u0000\u036d\u036c\u0001\u0000\u0000"+
		"\u0000\u036d\u036e\u0001\u0000\u0000\u0000\u036e\u036f\u0001\u0000\u0000"+
		"\u0000\u036f\u0431\u00032\u0019\u0000\u0370\u0371\u0007\b\u0000\u0000"+
		"\u0371\u0373\u00030\u0018\u0000\u0372\u0374\u0005`\u0000\u0000\u0373\u0372"+
		"\u0001\u0000\u0000\u0000\u0373\u0374\u0001\u0000\u0000\u0000\u0374\u0375"+
		"\u0001\u0000\u0000\u0000\u0375\u0376\u0003\u00b8\\\u0000\u0376\u0431\u0001"+
		"\u0000\u0000\u0000\u0377\u0379\u0007\b\u0000\u0000\u0378\u037a\u0005\u0010"+
		"\u0000\u0000\u0379\u0378\u0001\u0000\u0000\u0000\u0379\u037a\u0001\u0000"+
		"\u0000\u0000\u037a\u037c\u0001\u0000\u0000\u0000\u037b\u037d\u0007\t\u0000"+
		"\u0000\u037c\u037b\u0001\u0000\u0000\u0000\u037c\u037d\u0001\u0000\u0000"+
		"\u0000\u037d\u037e\u0001\u0000\u0000\u0000\u037e\u0380\u0003\u00b8\\\u0000"+
		"\u037f\u0381\u0003,\u0016\u0000\u0380\u037f\u0001\u0000\u0000\u0000\u0380"+
		"\u0381\u0001\u0000\u0000\u0000\u0381\u0383\u0001\u0000\u0000\u0000\u0382"+
		"\u0384\u00034\u001a\u0000\u0383\u0382\u0001\u0000\u0000\u0000\u0383\u0384"+
		"\u0001\u0000\u0000\u0000\u0384\u0431\u0001\u0000\u0000\u0000\u0385\u0387"+
		"\u0007\b\u0000\u0000\u0386\u0388\u0005\u00c0\u0000\u0000\u0387\u0386\u0001"+
		"\u0000\u0000\u0000\u0387\u0388\u0001\u0000\u0000\u0000\u0388\u0389\u0001"+
		"\u0000\u0000\u0000\u0389\u0431\u0003&\u0013\u0000\u038a\u038b\u00057\u0000"+
		"\u0000\u038b\u038c\u0005\u00a7\u0000\u0000\u038c\u038d\u00030\u0018\u0000"+
		"\u038d\u038e\u0003\u00b8\\\u0000\u038e\u038f\u0005\u0085\u0000\u0000\u038f"+
		"\u0390\u0007\n\u0000\u0000\u0390\u0431\u0001\u0000\u0000\u0000\u0391\u0392"+
		"\u00057\u0000\u0000\u0392\u0393\u0005\u00a7\u0000\u0000\u0393\u0394\u0005"+
		"\u0010\u0000\u0000\u0394\u0395\u0003\u00b8\\\u0000\u0395\u0396\u0005\u0085"+
		"\u0000\u0000\u0396\u0397\u0007\n\u0000\u0000\u0397\u0431\u0001\u0000\u0000"+
		"\u0000\u0398\u0399\u0005\u00c7\u0000\u0000\u0399\u039a\u0005\u0010\u0000"+
		"\u0000\u039a\u0431\u0003\u00b8\\\u0000\u039b\u039c\u0005\u00c7\u0000\u0000"+
		"\u039c\u039d\u0005p\u0000\u0000\u039d\u0431\u0003\u00b8\\\u0000\u039e"+
		"\u03a6\u0005\u00c7\u0000\u0000\u039f\u03a7\u0005\u0126\u0000\u0000\u03a0"+
		"\u03a2\t\u0000\u0000\u0000\u03a1\u03a0\u0001\u0000\u0000\u0000\u03a2\u03a5"+
		"\u0001\u0000\u0000\u0000\u03a3\u03a4\u0001\u0000\u0000\u0000\u03a3\u03a1"+
		"\u0001\u0000\u0000\u0000\u03a4\u03a7\u0001\u0000\u0000\u0000\u03a5\u03a3"+
		"\u0001\u0000\u0000\u0000\u03a6\u039f\u0001\u0000\u0000\u0000\u03a6\u03a3"+
		"\u0001\u0000\u0000\u0000\u03a7\u0431\u0001\u0000\u0000\u0000\u03a8\u03aa"+
		"\u0005)\u0000\u0000\u03a9\u03ab\u0005\u008b\u0000\u0000\u03aa\u03a9\u0001"+
		"\u0000\u0000\u0000\u03aa\u03ab\u0001\u0000\u0000\u0000\u03ab\u03ac\u0001"+
		"\u0000\u0000\u0000\u03ac\u03ad\u0005\u0010\u0000\u0000\u03ad\u03b0\u0003"+
		"\u00b8\\\u0000\u03ae\u03af\u0005\u00aa\u0000\u0000\u03af\u03b1\u0003>"+
		"\u001f\u0000\u03b0\u03ae\u0001\u0000\u0000\u0000\u03b0\u03b1\u0001\u0000"+
		"\u0000\u0000\u03b1\u03b6\u0001\u0000\u0000\u0000\u03b2\u03b4\u0005 \u0000"+
		"\u0000\u03b3\u03b2\u0001\u0000\u0000\u0000\u03b3\u03b4\u0001\u0000\u0000"+
		"\u0000\u03b4\u03b5\u0001\u0000\u0000\u0000\u03b5\u03b7\u0003&\u0013\u0000"+
		"\u03b6\u03b3\u0001\u0000\u0000\u0000\u03b6\u03b7\u0001\u0000\u0000\u0000"+
		"\u03b7\u0431\u0001\u0000\u0000\u0000\u03b8\u03b9\u0005\u0102\u0000\u0000"+
		"\u03b9\u03bc\u0005\u0010\u0000\u0000\u03ba\u03bb\u0005x\u0000\u0000\u03bb"+
		"\u03bd\u0005]\u0000\u0000\u03bc\u03ba\u0001\u0000\u0000\u0000\u03bc\u03bd"+
		"\u0001\u0000\u0000\u0000\u03bd\u03be\u0001\u0000\u0000\u0000\u03be\u0431"+
		"\u0003\u00b8\\\u0000\u03bf\u03c0\u0005/\u0000\u0000\u03c0\u0431\u0005"+
		")\u0000\u0000\u03c1\u03c2\u0005\u0092\u0000\u0000\u03c2\u03c4\u0005\u0013"+
		"\u0000\u0000\u03c3\u03c5\u0005\u0093\u0000\u0000\u03c4\u03c3\u0001\u0000"+
		"\u0000\u0000\u03c4\u03c5\u0001\u0000\u0000\u0000\u03c5\u03c6\u0001\u0000"+
		"\u0000\u0000\u03c6\u03c7\u0005\u007f\u0000\u0000\u03c7\u03c9\u0005\u0126"+
		"\u0000\u0000\u03c8\u03ca\u0005\u00b3\u0000\u0000\u03c9\u03c8\u0001\u0000"+
		"\u0000\u0000\u03c9\u03ca\u0001\u0000\u0000\u0000\u03ca\u03cb\u0001\u0000"+
		"\u0000\u0000\u03cb\u03cc\u0005\u0084\u0000\u0000\u03cc\u03cd\u0005\u0010"+
		"\u0000\u0000\u03cd\u03cf\u0003\u00b8\\\u0000\u03ce\u03d0\u0003,\u0016"+
		"\u0000\u03cf\u03ce\u0001\u0000\u0000\u0000\u03cf\u03d0\u0001\u0000\u0000"+
		"\u0000\u03d0\u0431\u0001\u0000\u0000\u0000\u03d1\u03d2\u0005\u00fd\u0000"+
		"\u0000\u03d2\u03d3\u0005\u0010\u0000\u0000\u03d3\u03d5\u0003\u00b8\\\u0000"+
		"\u03d4\u03d6\u0003,\u0016\u0000\u03d5\u03d4\u0001\u0000\u0000\u0000\u03d5"+
		"\u03d6\u0001\u0000\u0000\u0000\u03d6\u0431\u0001\u0000\u0000\u0000\u03d7"+
		"\u03d8\u0005\u009e\u0000\u0000\u03d8\u03d9\u0005\u00c9\u0000\u0000\u03d9"+
		"\u03da\u0005\u0010\u0000\u0000\u03da\u03dd\u0003\u00b8\\\u0000\u03db\u03dc"+
		"\u0007\u000b\u0000\u0000\u03dc\u03de\u0005\u00b6\u0000\u0000\u03dd\u03db"+
		"\u0001\u0000\u0000\u0000\u03dd\u03de\u0001\u0000\u0000\u0000\u03de\u0431"+
		"\u0001\u0000\u0000\u0000\u03df\u03e0\u0007\f\u0000\u0000\u03e0\u03e4\u0003"+
		"\u0112\u0089\u0000\u03e1\u03e3\t\u0000\u0000\u0000\u03e2\u03e1\u0001\u0000"+
		"\u0000\u0000\u03e3\u03e6\u0001\u0000\u0000\u0000\u03e4\u03e5\u0001\u0000"+
		"\u0000\u0000\u03e4\u03e2\u0001\u0000\u0000\u0000\u03e5\u0431\u0001\u0000"+
		"\u0000\u0000\u03e6\u03e4\u0001\u0000\u0000\u0000\u03e7\u03e8\u0005\u00df"+
		"\u0000\u0000\u03e8\u03ec\u0005\u00d1\u0000\u0000\u03e9\u03eb\t\u0000\u0000"+
		"\u0000\u03ea\u03e9\u0001\u0000\u0000\u0000\u03eb\u03ee\u0001\u0000\u0000"+
		"\u0000\u03ec\u03ed\u0001\u0000\u0000\u0000\u03ec\u03ea\u0001\u0000\u0000"+
		"\u0000\u03ed\u0431\u0001\u0000\u0000\u0000\u03ee\u03ec\u0001\u0000\u0000"+
		"\u0000\u03ef\u03f0\u0005\u00df\u0000\u0000\u03f0\u03f1\u0005\u00f5\u0000"+
		"\u0000\u03f1\u03f2\u0005\u0113\u0000\u0000\u03f2\u0431\u0003\u00e0p\u0000"+
		"\u03f3\u03f4\u0005\u00df\u0000\u0000\u03f4\u03f5\u0005\u00f5\u0000\u0000"+
		"\u03f5\u03f6\u0005\u0113\u0000\u0000\u03f6\u0431\u0007\r\u0000\u0000\u03f7"+
		"\u03f8\u0005\u00df\u0000\u0000\u03f8\u03f9\u0005\u00f5\u0000\u0000\u03f9"+
		"\u03fd\u0005\u0113\u0000\u0000\u03fa\u03fc\t\u0000\u0000\u0000\u03fb\u03fa"+
		"\u0001\u0000\u0000\u0000\u03fc\u03ff\u0001\u0000\u0000\u0000\u03fd\u03fe"+
		"\u0001\u0000\u0000\u0000\u03fd\u03fb\u0001\u0000\u0000\u0000\u03fe\u0431"+
		"\u0001\u0000\u0000\u0000\u03ff\u03fd\u0001\u0000\u0000\u0000\u0400\u0401"+
		"\u0005\u00df\u0000\u0000\u0401\u0402\u0003\u0014\n\u0000\u0402\u0403\u0005"+
		"\u0114\u0000\u0000\u0403\u0404\u0003\u0016\u000b\u0000\u0404\u0431\u0001"+
		"\u0000\u0000\u0000\u0405\u0406\u0005\u00df\u0000\u0000\u0406\u040e\u0003"+
		"\u0014\n\u0000\u0407\u040b\u0005\u0114\u0000\u0000\u0408\u040a\t\u0000"+
		"\u0000\u0000\u0409\u0408\u0001\u0000\u0000\u0000\u040a\u040d\u0001\u0000"+
		"\u0000\u0000\u040b\u040c\u0001\u0000\u0000\u0000\u040b\u0409\u0001\u0000"+
		"\u0000\u0000\u040c\u040f\u0001\u0000\u0000\u0000\u040d\u040b\u0001\u0000"+
		"\u0000\u0000\u040e\u0407\u0001\u0000\u0000\u0000\u040e\u040f\u0001\u0000"+
		"\u0000\u0000\u040f\u0431\u0001\u0000\u0000\u0000\u0410\u0414\u0005\u00df"+
		"\u0000\u0000\u0411\u0413\t\u0000\u0000\u0000\u0412\u0411\u0001\u0000\u0000"+
		"\u0000\u0413\u0416\u0001\u0000\u0000\u0000\u0414\u0415\u0001\u0000\u0000"+
		"\u0000\u0414\u0412\u0001\u0000\u0000\u0000\u0415\u0417\u0001\u0000\u0000"+
		"\u0000\u0416\u0414\u0001\u0000\u0000\u0000\u0417\u0418\u0005\u0114\u0000"+
		"\u0000\u0418\u0431\u0003\u0016\u000b\u0000\u0419\u041d\u0005\u00df\u0000"+
		"\u0000\u041a\u041c\t\u0000\u0000\u0000\u041b\u041a\u0001\u0000\u0000\u0000"+
		"\u041c\u041f\u0001\u0000\u0000\u0000\u041d\u041e\u0001\u0000\u0000\u0000"+
		"\u041d\u041b\u0001\u0000\u0000\u0000\u041e\u0431\u0001\u0000\u0000\u0000"+
		"\u041f\u041d\u0001\u0000\u0000\u0000\u0420\u0421\u0005\u00cb\u0000\u0000"+
		"\u0421\u0431\u0003\u0014\n\u0000\u0422\u0426\u0005\u00cb\u0000\u0000\u0423"+
		"\u0425\t\u0000\u0000\u0000\u0424\u0423\u0001\u0000\u0000\u0000\u0425\u0428"+
		"\u0001\u0000\u0000\u0000\u0426\u0427\u0001\u0000\u0000\u0000\u0426\u0424"+
		"\u0001\u0000\u0000\u0000\u0427\u0431\u0001\u0000\u0000\u0000\u0428\u0426"+
		"\u0001\u0000\u0000\u0000\u0429\u042d\u0003\u0018\f\u0000\u042a\u042c\t"+
		"\u0000\u0000\u0000\u042b\u042a\u0001\u0000\u0000\u0000\u042c\u042f\u0001"+
		"\u0000\u0000\u0000\u042d\u042e\u0001\u0000\u0000\u0000\u042d\u042b\u0001"+
		"\u0000\u0000\u0000\u042e\u0431\u0001\u0000\u0000\u0000\u042f\u042d\u0001"+
		"\u0000\u0000\u0000\u0430\u014f\u0001\u0000\u0000\u0000\u0430\u0151\u0001"+
		"\u0000\u0000\u0000\u0430\u0154\u0001\u0000\u0000\u0000\u0430\u0159\u0001"+
		"\u0000\u0000\u0000\u0430\u016b\u0001\u0000\u0000\u0000\u0430\u0172\u0001"+
		"\u0000\u0000\u0000\u0430\u0178\u0001\u0000\u0000\u0000\u0430\u0182\u0001"+
		"\u0000\u0000\u0000\u0430\u018e\u0001\u0000\u0000\u0000\u0430\u019f\u0001"+
		"\u0000\u0000\u0000\u0430\u01b4\u0001\u0000\u0000\u0000\u0430\u01c5\u0001"+
		"\u0000\u0000\u0000\u0430\u01d6\u0001\u0000\u0000\u0000\u0430\u01e1\u0001"+
		"\u0000\u0000\u0000\u0430\u01e8\u0001\u0000\u0000\u0000\u0430\u01f1\u0001"+
		"\u0000\u0000\u0000\u0430\u01fa\u0001\u0000\u0000\u0000\u0430\u0203\u0001"+
		"\u0000\u0000\u0000\u0430\u020a\u0001\u0000\u0000\u0000\u0430\u0211\u0001"+
		"\u0000\u0000\u0000\u0430\u0218\u0001\u0000\u0000\u0000\u0430\u0223\u0001"+
		"\u0000\u0000\u0000\u0430\u022e\u0001\u0000\u0000\u0000\u0430\u023d\u0001"+
		"\u0000\u0000\u0000\u0430\u0249\u0001\u0000\u0000\u0000\u0430\u0257\u0001"+
		"\u0000\u0000\u0000\u0430\u0261\u0001\u0000\u0000\u0000\u0430\u026f\u0001"+
		"\u0000\u0000\u0000\u0430\u0277\u0001\u0000\u0000\u0000\u0430\u028a\u0001"+
		"\u0000\u0000\u0000\u0430\u0293\u0001\u0000\u0000\u0000\u0430\u0299\u0001"+
		"\u0000\u0000\u0000\u0430\u02a3\u0001\u0000\u0000\u0000\u0430\u02aa\u0001"+
		"\u0000\u0000\u0000\u0430\u02cd\u0001\u0000\u0000\u0000\u0430\u02e3\u0001"+
		"\u0000\u0000\u0000\u0430\u02eb\u0001\u0000\u0000\u0000\u0430\u0307\u0001"+
		"\u0000\u0000\u0000\u0430\u0311\u0001\u0000\u0000\u0000\u0430\u0316\u0001"+
		"\u0000\u0000\u0000\u0430\u0322\u0001\u0000\u0000\u0000\u0430\u032e\u0001"+
		"\u0000\u0000\u0000\u0430\u0337\u0001\u0000\u0000\u0000\u0430\u033f\u0001"+
		"\u0000\u0000\u0000\u0430\u034b\u0001\u0000\u0000\u0000\u0430\u0351\u0001"+
		"\u0000\u0000\u0000\u0430\u035f\u0001\u0000\u0000\u0000\u0430\u0367\u0001"+
		"\u0000\u0000\u0000\u0430\u036a\u0001\u0000\u0000\u0000\u0430\u0370\u0001"+
		"\u0000\u0000\u0000\u0430\u0377\u0001\u0000\u0000\u0000\u0430\u0385\u0001"+
		"\u0000\u0000\u0000\u0430\u038a\u0001\u0000\u0000\u0000\u0430\u0391\u0001"+
		"\u0000\u0000\u0000\u0430\u0398\u0001\u0000\u0000\u0000\u0430\u039b\u0001"+
		"\u0000\u0000\u0000\u0430\u039e\u0001\u0000\u0000\u0000\u0430\u03a8\u0001"+
		"\u0000\u0000\u0000\u0430\u03b8\u0001\u0000\u0000\u0000\u0430\u03bf\u0001"+
		"\u0000\u0000\u0000\u0430\u03c1\u0001\u0000\u0000\u0000\u0430\u03d1\u0001"+
		"\u0000\u0000\u0000\u0430\u03d7\u0001\u0000\u0000\u0000\u0430\u03df\u0001"+
		"\u0000\u0000\u0000\u0430\u03e7\u0001\u0000\u0000\u0000\u0430\u03ef\u0001"+
		"\u0000\u0000\u0000\u0430\u03f3\u0001\u0000\u0000\u0000\u0430\u03f7\u0001"+
		"\u0000\u0000\u0000\u0430\u0400\u0001\u0000\u0000\u0000\u0430\u0405\u0001"+
		"\u0000\u0000\u0000\u0430\u0410\u0001\u0000\u0000\u0000\u0430\u0419\u0001"+
		"\u0000\u0000\u0000\u0430\u0420\u0001\u0000\u0000\u0000\u0430\u0422\u0001"+
		"\u0000\u0000\u0000\u0430\u0429\u0001\u0000\u0000\u0000\u0431\u0013\u0001"+
		"\u0000\u0000\u0000\u0432\u0433\u0003\u0116\u008b\u0000\u0433\u0015\u0001"+
		"\u0000\u0000\u0000\u0434\u0435\u0003\u0116\u008b\u0000\u0435\u0017\u0001"+
		"\u0000\u0000\u0000\u0436\u0437\u0005?\u0000\u0000\u0437\u04df\u0005\u00d1"+
		"\u0000\u0000\u0438\u0439\u0005V\u0000\u0000\u0439\u04df\u0005\u00d1\u0000"+
		"\u0000\u043a\u043c\u0005s\u0000\u0000\u043b\u043d\u0005\u00d1\u0000\u0000"+
		"\u043c\u043b\u0001\u0000\u0000\u0000\u043c\u043d\u0001\u0000\u0000\u0000"+
		"\u043d\u04df\u0001\u0000\u0000\u0000\u043e\u0440\u0005\u00ce\u0000\u0000"+
		"\u043f\u0441\u0005\u00d1\u0000\u0000\u0440\u043f\u0001\u0000\u0000\u0000"+
		"\u0440\u0441\u0001\u0000\u0000\u0000\u0441\u04df\u0001\u0000\u0000\u0000"+
		"\u0442\u0443\u0005\u00e2\u0000\u0000\u0443\u04df\u0005s\u0000\u0000\u0444"+
		"\u0445\u0005\u00e2\u0000\u0000\u0445\u0447\u0005\u00d1\u0000\u0000\u0446"+
		"\u0448\u0005s\u0000\u0000\u0447\u0446\u0001\u0000\u0000\u0000\u0447\u0448"+
		"\u0001\u0000\u0000\u0000\u0448\u04df\u0001\u0000\u0000\u0000\u0449\u044a"+
		"\u0005\u00e2\u0000\u0000\u044a\u04df\u0005\u00bd\u0000\u0000\u044b\u044c"+
		"\u0005\u00e2\u0000\u0000\u044c\u04df\u0005\u00d2\u0000\u0000\u044d\u044e"+
		"\u0005\u00e2\u0000\u0000\u044e\u044f\u0005B\u0000\u0000\u044f\u04df\u0005"+
		"\u00d2\u0000\u0000\u0450\u0451\u0005_\u0000\u0000\u0451\u04df\u0005\u0010"+
		"\u0000\u0000\u0452\u0453\u0005z\u0000\u0000\u0453\u04df\u0005\u0010\u0000"+
		"\u0000\u0454\u0455\u0005\u00e2\u0000\u0000\u0455\u04df\u0005:\u0000\u0000"+
		"\u0456\u0457\u0005\u00e2\u0000\u0000\u0457\u0458\u0005?\u0000\u0000\u0458"+
		"\u04df\u0005\u0010\u0000\u0000\u0459\u045a\u0005\u00e2\u0000\u0000\u045a"+
		"\u04df\u0005\u00f9\u0000\u0000\u045b\u045c\u0005\u00e2\u0000\u0000\u045c"+
		"\u04df\u0005}\u0000\u0000\u045d\u045e\u0005\u00e2\u0000\u0000\u045e\u04df"+
		"\u0005\u0096\u0000\u0000\u045f\u0460\u0005?\u0000\u0000\u0460\u04df\u0005"+
		"|\u0000\u0000\u0461\u0462\u0005V\u0000\u0000\u0462\u04df\u0005|\u0000"+
		"\u0000\u0463\u0464\u0005\u0019\u0000\u0000\u0464\u04df\u0005|\u0000\u0000"+
		"\u0465\u0466\u0005\u0095\u0000\u0000\u0466\u04df\u0005\u0010\u0000\u0000"+
		"\u0467\u0468\u0005\u0095\u0000\u0000\u0468\u04df\u0005H\u0000\u0000\u0469"+
		"\u046a\u0005\u0106\u0000\u0000\u046a\u04df\u0005\u0010\u0000\u0000\u046b"+
		"\u046c\u0005\u0106\u0000\u0000\u046c\u04df\u0005H\u0000\u0000\u046d\u046e"+
		"\u0005?\u0000\u0000\u046e\u046f\u0005\u00f2\u0000\u0000\u046f\u04df\u0005"+
		"\u0098\u0000\u0000\u0470\u0471\u0005V\u0000\u0000\u0471\u0472\u0005\u00f2"+
		"\u0000\u0000\u0472\u04df\u0005\u0098\u0000\u0000\u0473\u0474\u0005\u0019"+
		"\u0000\u0000\u0474\u0475\u0005\u0010\u0000\u0000\u0475\u0476\u0003\u00ba"+
		"]\u0000\u0476\u0477\u0005\u00a3\u0000\u0000\u0477\u0478\u00051\u0000\u0000"+
		"\u0478\u04df\u0001\u0000\u0000\u0000\u0479\u047a\u0005\u0019\u0000\u0000"+
		"\u047a\u047b\u0005\u0010\u0000\u0000\u047b\u047c\u0003\u00ba]\u0000\u047c"+
		"\u047d\u00051\u0000\u0000\u047d\u047e\u0005(\u0000\u0000\u047e\u04df\u0001"+
		"\u0000\u0000\u0000\u047f\u0480\u0005\u0019\u0000\u0000\u0480\u0481\u0005"+
		"\u0010\u0000\u0000\u0481\u0482\u0003\u00ba]\u0000\u0482\u0483\u0005\u00a3"+
		"\u0000\u0000\u0483\u0484\u0005\u00e6\u0000\u0000\u0484\u04df\u0001\u0000"+
		"\u0000\u0000\u0485\u0486\u0005\u0019\u0000\u0000\u0486\u0487\u0005\u0010"+
		"\u0000\u0000\u0487\u0488\u0003\u00ba]\u0000\u0488\u0489\u0005\u00e3\u0000"+
		"\u0000\u0489\u048a\u0005(\u0000\u0000\u048a\u04df\u0001\u0000\u0000\u0000"+
		"\u048b\u048c\u0005\u0019\u0000\u0000\u048c\u048d\u0005\u0010\u0000\u0000"+
		"\u048d\u048e\u0003\u00ba]\u0000\u048e\u048f\u0005\u00a3\u0000\u0000\u048f"+
		"\u0490\u0005\u00e3\u0000\u0000\u0490\u04df\u0001\u0000\u0000\u0000\u0491"+
		"\u0492\u0005\u0019\u0000\u0000\u0492\u0493\u0005\u0010\u0000\u0000\u0493"+
		"\u0494\u0003\u00ba]\u0000\u0494\u0495\u0005\u00a3\u0000\u0000\u0495\u0496"+
		"\u0005\u00e9\u0000\u0000\u0496\u0497\u0005 \u0000\u0000\u0497\u0498\u0005"+
		"Q\u0000\u0000\u0498\u04df\u0001\u0000\u0000\u0000\u0499\u049a\u0005\u0019"+
		"\u0000\u0000\u049a\u049b\u0005\u0010\u0000\u0000\u049b\u049c\u0003\u00ba"+
		"]\u0000\u049c\u049d\u0005\u00df\u0000\u0000\u049d\u049e\u0005\u00e3\u0000"+
		"\u0000\u049e\u049f\u0005\u0094\u0000\u0000\u049f\u04df\u0001\u0000\u0000"+
		"\u0000\u04a0\u04a1\u0005\u0019\u0000\u0000\u04a1\u04a2\u0005\u0010\u0000"+
		"\u0000\u04a2\u04a3\u0003\u00ba]\u0000\u04a3\u04a4\u0005\\\u0000\u0000"+
		"\u04a4\u04a5\u0005\u00b4\u0000\u0000\u04a5\u04df\u0001\u0000\u0000\u0000"+
		"\u04a6\u04a7\u0005\u0019\u0000\u0000\u04a7\u04a8\u0005\u0010\u0000\u0000"+
		"\u04a8\u04a9\u0003\u00ba]\u0000\u04a9\u04aa\u0005\u001e\u0000\u0000\u04aa"+
		"\u04ab\u0005\u00b4\u0000\u0000\u04ab\u04df\u0001\u0000\u0000\u0000\u04ac"+
		"\u04ad\u0005\u0019\u0000\u0000\u04ad\u04ae\u0005\u0010\u0000\u0000\u04ae"+
		"\u04af\u0003\u00ba]\u0000\u04af\u04b0\u0005\u0100\u0000\u0000\u04b0\u04b1"+
		"\u0005\u00b4\u0000\u0000\u04b1\u04df\u0001\u0000\u0000\u0000\u04b2\u04b3"+
		"\u0005\u0019\u0000\u0000\u04b3\u04b4\u0005\u0010\u0000\u0000\u04b4\u04b5"+
		"\u0003\u00ba]\u0000\u04b5\u04b6\u0005\u00f6\u0000\u0000\u04b6\u04df\u0001"+
		"\u0000\u0000\u0000\u04b7\u04b8\u0005\u0019\u0000\u0000\u04b8\u04b9\u0005"+
		"\u0010\u0000\u0000\u04b9\u04bb\u0003\u00ba]\u0000\u04ba\u04bc\u0003,\u0016"+
		"\u0000\u04bb\u04ba\u0001\u0000\u0000\u0000\u04bb\u04bc\u0001\u0000\u0000"+
		"\u0000\u04bc\u04bd\u0001\u0000\u0000\u0000\u04bd\u04be\u00059\u0000\u0000"+
		"\u04be\u04df\u0001\u0000\u0000\u0000\u04bf\u04c0\u0005\u0019\u0000\u0000"+
		"\u04c0\u04c1\u0005\u0010\u0000\u0000\u04c1\u04c3\u0003\u00ba]\u0000\u04c2"+
		"\u04c4\u0003,\u0016\u0000\u04c3\u04c2\u0001\u0000\u0000\u0000\u04c3\u04c4"+
		"\u0001\u0000\u0000\u0000\u04c4\u04c5\u0001\u0000\u0000\u0000\u04c5\u04c6"+
		"\u0005<\u0000\u0000\u04c6\u04df\u0001\u0000\u0000\u0000\u04c7\u04c8\u0005"+
		"\u0019\u0000\u0000\u04c8\u04c9\u0005\u0010\u0000\u0000\u04c9\u04cb\u0003"+
		"\u00ba]\u0000\u04ca\u04cc\u0003,\u0016\u0000\u04cb\u04ca\u0001\u0000\u0000"+
		"\u0000\u04cb\u04cc\u0001\u0000\u0000\u0000\u04cc\u04cd\u0001\u0000\u0000"+
		"\u0000\u04cd\u04ce\u0005\u00df\u0000\u0000\u04ce\u04cf\u0005g\u0000\u0000"+
		"\u04cf\u04df\u0001\u0000\u0000\u0000\u04d0\u04d1\u0005\u0019\u0000\u0000"+
		"\u04d1\u04d2\u0005\u0010\u0000\u0000\u04d2\u04d4\u0003\u00ba]\u0000\u04d3"+
		"\u04d5\u0003,\u0016\u0000\u04d4\u04d3\u0001\u0000\u0000\u0000\u04d4\u04d5"+
		"\u0001\u0000\u0000\u0000\u04d5\u04d6\u0001\u0000\u0000\u0000\u04d6\u04d7"+
		"\u0005\u00ca\u0000\u0000\u04d7\u04d8\u00056\u0000\u0000\u04d8\u04df\u0001"+
		"\u0000\u0000\u0000\u04d9\u04da\u0005\u00e7\u0000\u0000\u04da\u04df\u0005"+
		"\u00f8\u0000\u0000\u04db\u04df\u00058\u0000\u0000\u04dc\u04df\u0005\u00d3"+
		"\u0000\u0000\u04dd\u04df\u0005P\u0000\u0000\u04de\u0436\u0001\u0000\u0000"+
		"\u0000\u04de\u0438\u0001\u0000\u0000\u0000\u04de\u043a\u0001\u0000\u0000"+
		"\u0000\u04de\u043e\u0001\u0000\u0000\u0000\u04de\u0442\u0001\u0000\u0000"+
		"\u0000\u04de\u0444\u0001\u0000\u0000\u0000\u04de\u0449\u0001\u0000\u0000"+
		"\u0000\u04de\u044b\u0001\u0000\u0000\u0000\u04de\u044d\u0001\u0000\u0000"+
		"\u0000\u04de\u0450\u0001\u0000\u0000\u0000\u04de\u0452\u0001\u0000\u0000"+
		"\u0000\u04de\u0454\u0001\u0000\u0000\u0000\u04de\u0456\u0001\u0000\u0000"+
		"\u0000\u04de\u0459\u0001\u0000\u0000\u0000\u04de\u045b\u0001\u0000\u0000"+
		"\u0000\u04de\u045d\u0001\u0000\u0000\u0000\u04de\u045f\u0001\u0000\u0000"+
		"\u0000\u04de\u0461\u0001\u0000\u0000\u0000\u04de\u0463\u0001\u0000\u0000"+
		"\u0000\u04de\u0465\u0001\u0000\u0000\u0000\u04de\u0467\u0001\u0000\u0000"+
		"\u0000\u04de\u0469\u0001\u0000\u0000\u0000\u04de\u046b\u0001\u0000\u0000"+
		"\u0000\u04de\u046d\u0001\u0000\u0000\u0000\u04de\u0470\u0001\u0000\u0000"+
		"\u0000\u04de\u0473\u0001\u0000\u0000\u0000\u04de\u0479\u0001\u0000\u0000"+
		"\u0000\u04de\u047f\u0001\u0000\u0000\u0000\u04de\u0485\u0001\u0000\u0000"+
		"\u0000\u04de\u048b\u0001\u0000\u0000\u0000\u04de\u0491\u0001\u0000\u0000"+
		"\u0000\u04de\u0499\u0001\u0000\u0000\u0000\u04de\u04a0\u0001\u0000\u0000"+
		"\u0000\u04de\u04a6\u0001\u0000\u0000\u0000\u04de\u04ac\u0001\u0000\u0000"+
		"\u0000\u04de\u04b2\u0001\u0000\u0000\u0000\u04de\u04b7\u0001\u0000\u0000"+
		"\u0000\u04de\u04bf\u0001\u0000\u0000\u0000\u04de\u04c7\u0001\u0000\u0000"+
		"\u0000\u04de\u04d0\u0001\u0000\u0000\u0000\u04de\u04d9\u0001\u0000\u0000"+
		"\u0000\u04de\u04db\u0001\u0000\u0000\u0000\u04de\u04dc\u0001\u0000\u0000"+
		"\u0000\u04de\u04dd\u0001\u0000\u0000\u0000\u04df\u0019\u0001\u0000\u0000"+
		"\u0000\u04e0\u04e2\u0005?\u0000\u0000\u04e1\u04e3\u0005\u00f2\u0000\u0000"+
		"\u04e2\u04e1\u0001\u0000\u0000\u0000\u04e2\u04e3\u0001\u0000\u0000\u0000"+
		"\u04e3\u04e5\u0001\u0000\u0000\u0000\u04e4\u04e6\u0005a\u0000\u0000\u04e5"+
		"\u04e4\u0001\u0000\u0000\u0000\u04e5\u04e6\u0001\u0000\u0000\u0000\u04e6"+
		"\u04e7\u0001\u0000\u0000\u0000\u04e7\u04eb\u0005\u0010\u0000\u0000\u04e8"+
		"\u04e9\u0005x\u0000\u0000\u04e9\u04ea\u0005\u00a3\u0000\u0000\u04ea\u04ec"+
		"\u0005]\u0000\u0000\u04eb\u04e8\u0001\u0000\u0000\u0000\u04eb\u04ec\u0001"+
		"\u0000\u0000\u0000\u04ec\u04ed\u0001\u0000\u0000\u0000\u04ed\u04ee\u0003"+
		"\u00b8\\\u0000\u04ee\u001b\u0001\u0000\u0000\u0000\u04ef\u04f0\u0005?"+
		"\u0000\u0000\u04f0\u04f2\u0005\u00ab\u0000\u0000\u04f1\u04ef\u0001\u0000"+
		"\u0000\u0000\u04f1\u04f2\u0001\u0000\u0000\u0000\u04f2\u04f3\u0001\u0000"+
		"\u0000\u0000\u04f3\u04f4\u0005\u00ca\u0000\u0000\u04f4\u04f5\u0005\u0010"+
		"\u0000\u0000\u04f5\u04f6\u0003\u00b8\\\u0000\u04f6\u001d\u0001\u0000\u0000"+
		"\u0000\u04f7\u04f8\u00051\u0000\u0000\u04f8\u04f9\u0005(\u0000\u0000\u04f9"+
		"\u04fd\u0003\u00a0P\u0000\u04fa\u04fb\u0005\u00e6\u0000\u0000\u04fb\u04fc"+
		"\u0005(\u0000\u0000\u04fc\u04fe\u0003\u00a4R\u0000\u04fd\u04fa\u0001\u0000"+
		"\u0000\u0000\u04fd\u04fe\u0001\u0000\u0000\u0000\u04fe\u04ff\u0001\u0000"+
		"\u0000\u0000\u04ff\u0500\u0005\u0084\u0000\u0000\u0500\u0501\u0005\u012a"+
		"\u0000\u0000\u0501\u0502\u0005\'\u0000\u0000\u0502\u001f\u0001\u0000\u0000"+
		"\u0000\u0503\u0504\u0005\u00e3\u0000\u0000\u0504\u0505\u0005(\u0000\u0000"+
		"\u0505\u0506\u0003\u00a0P\u0000\u0506\u0509\u0005\u00a7\u0000\u0000\u0507"+
		"\u050a\u0003F#\u0000\u0508\u050a\u0003H$\u0000\u0509\u0507\u0001\u0000"+
		"\u0000\u0000\u0509\u0508\u0001\u0000\u0000\u0000\u050a\u050e\u0001\u0000"+
		"\u0000\u0000\u050b\u050c\u0005\u00e9\u0000\u0000\u050c\u050d\u0005 \u0000"+
		"\u0000\u050d\u050f\u0005Q\u0000\u0000\u050e\u050b\u0001\u0000\u0000\u0000"+
		"\u050e\u050f\u0001\u0000\u0000\u0000\u050f!\u0001\u0000\u0000\u0000\u0510"+
		"\u0511\u0005\u0094\u0000\u0000\u0511\u0512\u0005\u0126\u0000\u0000\u0512"+
		"#\u0001\u0000\u0000\u0000\u0513\u0514\u00057\u0000\u0000\u0514\u0515\u0005"+
		"\u0126\u0000\u0000\u0515%\u0001\u0000\u0000\u0000\u0516\u0518\u00036\u001b"+
		"\u0000\u0517\u0516\u0001\u0000\u0000\u0000\u0517\u0518\u0001\u0000\u0000"+
		"\u0000\u0518\u0519\u0001\u0000\u0000\u0000\u0519\u051a\u0003X,\u0000\u051a"+
		"\u051b\u0003T*\u0000\u051b\'\u0001\u0000\u0000\u0000\u051c\u051d\u0005"+
		"\u0081\u0000\u0000\u051d\u051f\u0005\u00b3\u0000\u0000\u051e\u0520\u0005"+
		"\u0010\u0000\u0000\u051f\u051e\u0001\u0000\u0000\u0000\u051f\u0520\u0001"+
		"\u0000\u0000\u0000\u0520\u0521\u0001\u0000\u0000\u0000\u0521\u0528\u0003"+
		"\u00b8\\\u0000\u0522\u0526\u0003,\u0016\u0000\u0523\u0524\u0005x\u0000"+
		"\u0000\u0524\u0525\u0005\u00a3\u0000\u0000\u0525\u0527\u0005]\u0000\u0000"+
		"\u0526\u0523\u0001\u0000\u0000\u0000\u0526\u0527\u0001\u0000\u0000\u0000"+
		"\u0527\u0529\u0001\u0000\u0000\u0000\u0528\u0522\u0001\u0000\u0000\u0000"+
		"\u0528\u0529\u0001\u0000\u0000\u0000\u0529\u052b\u0001\u0000\u0000\u0000"+
		"\u052a\u052c\u0003\u00a0P\u0000\u052b\u052a\u0001\u0000\u0000\u0000\u052b"+
		"\u052c\u0001\u0000\u0000\u0000\u052c\u055a\u0001\u0000\u0000\u0000\u052d"+
		"\u052e\u0005\u0081\u0000\u0000\u052e\u0530\u0005\u0084\u0000\u0000\u052f"+
		"\u0531\u0005\u0010\u0000\u0000\u0530\u052f\u0001\u0000\u0000\u0000\u0530"+
		"\u0531\u0001\u0000\u0000\u0000\u0531\u0532\u0001\u0000\u0000\u0000\u0532"+
		"\u0534\u0003\u00b8\\\u0000\u0533\u0535\u0003,\u0016\u0000\u0534\u0533"+
		"\u0001\u0000\u0000\u0000\u0534\u0535\u0001\u0000\u0000\u0000\u0535\u0539"+
		"\u0001\u0000\u0000\u0000\u0536\u0537\u0005x\u0000\u0000\u0537\u0538\u0005"+
		"\u00a3\u0000\u0000\u0538\u053a\u0005]\u0000\u0000\u0539\u0536\u0001\u0000"+
		"\u0000\u0000\u0539\u053a\u0001\u0000\u0000\u0000\u053a\u053c\u0001\u0000"+
		"\u0000\u0000\u053b\u053d\u0003\u00a0P\u0000\u053c\u053b\u0001\u0000\u0000"+
		"\u0000\u053c\u053d\u0001\u0000\u0000\u0000\u053d\u055a\u0001\u0000\u0000"+
		"\u0000\u053e\u053f\u0005\u0081\u0000\u0000\u053f\u0541\u0005\u00b3\u0000"+
		"\u0000\u0540\u0542\u0005\u0093\u0000\u0000\u0541\u0540\u0001\u0000\u0000"+
		"\u0000\u0541\u0542\u0001\u0000\u0000\u0000\u0542\u0543\u0001\u0000\u0000"+
		"\u0000\u0543\u0544\u0005R\u0000\u0000\u0544\u0546\u0005\u0126\u0000\u0000"+
		"\u0545\u0547\u0003\u00b4Z\u0000\u0546\u0545\u0001\u0000\u0000\u0000\u0546"+
		"\u0547\u0001\u0000\u0000\u0000\u0547\u0549\u0001\u0000\u0000\u0000\u0548"+
		"\u054a\u0003J%\u0000\u0549\u0548\u0001\u0000\u0000\u0000\u0549\u054a\u0001"+
		"\u0000\u0000\u0000\u054a\u055a\u0001\u0000\u0000\u0000\u054b\u054c\u0005"+
		"\u0081\u0000\u0000\u054c\u054e\u0005\u00b3\u0000\u0000\u054d\u054f\u0005"+
		"\u0093\u0000\u0000\u054e\u054d\u0001\u0000\u0000\u0000\u054e\u054f\u0001"+
		"\u0000\u0000\u0000\u054f\u0550\u0001\u0000\u0000\u0000\u0550\u0552\u0005"+
		"R\u0000\u0000\u0551\u0553\u0005\u0126\u0000\u0000\u0552\u0551\u0001\u0000"+
		"\u0000\u0000\u0552\u0553\u0001\u0000\u0000\u0000\u0553\u0554\u0001\u0000"+
		"\u0000\u0000\u0554\u0557\u0003:\u001d\u0000\u0555\u0556\u0005\u00aa\u0000"+
		"\u0000\u0556\u0558\u0003>\u001f\u0000\u0557\u0555\u0001\u0000\u0000\u0000"+
		"\u0557\u0558\u0001\u0000\u0000\u0000\u0558\u055a\u0001\u0000\u0000\u0000"+
		"\u0559\u051c\u0001\u0000\u0000\u0000\u0559\u052d\u0001\u0000\u0000\u0000"+
		"\u0559\u053e\u0001\u0000\u0000\u0000\u0559\u054b\u0001\u0000\u0000\u0000"+
		"\u055a)\u0001\u0000\u0000\u0000\u055b\u055d\u0003,\u0016\u0000\u055c\u055e"+
		"\u0003\"\u0011\u0000\u055d\u055c\u0001\u0000\u0000\u0000\u055d\u055e\u0001"+
		"\u0000\u0000\u0000\u055e+\u0001\u0000\u0000\u0000\u055f\u0560\u0005\u00b4"+
		"\u0000\u0000\u0560\u0561\u0005\u0002\u0000\u0000\u0561\u0566\u0003.\u0017"+
		"\u0000\u0562\u0563\u0005\u0004\u0000\u0000\u0563\u0565\u0003.\u0017\u0000"+
		"\u0564\u0562\u0001\u0000\u0000\u0000\u0565\u0568\u0001\u0000\u0000\u0000"+
		"\u0566\u0564\u0001\u0000\u0000\u0000\u0566\u0567\u0001\u0000\u0000\u0000"+
		"\u0567\u0569\u0001\u0000\u0000\u0000\u0568\u0566\u0001\u0000\u0000\u0000"+
		"\u0569\u056a\u0005\u0003\u0000\u0000\u056a-\u0001\u0000\u0000\u0000\u056b"+
		"\u056e\u0003\u0112\u0089\u0000\u056c\u056d\u0005\u0114\u0000\u0000\u056d"+
		"\u056f\u0003\u00d6k\u0000\u056e\u056c\u0001\u0000\u0000\u0000\u056e\u056f"+
		"\u0001\u0000\u0000\u0000\u056f/\u0001\u0000\u0000\u0000\u0570\u0571\u0007"+
		"\u000e\u0000\u0000\u05711\u0001\u0000\u0000\u0000\u0572\u0578\u0003\u010c"+
		"\u0086\u0000\u0573\u0578\u0005\u0126\u0000\u0000\u0574\u0578\u0003\u00d8"+
		"l\u0000\u0575\u0578\u0003\u00dam\u0000\u0576\u0578\u0003\u00dcn\u0000"+
		"\u0577\u0572\u0001\u0000\u0000\u0000\u0577\u0573\u0001\u0000\u0000\u0000"+
		"\u0577\u0574\u0001\u0000\u0000\u0000\u0577\u0575\u0001\u0000\u0000\u0000"+
		"\u0577\u0576\u0001\u0000\u0000\u0000\u05783\u0001\u0000\u0000\u0000\u0579"+
		"\u057e\u0003\u0112\u0089\u0000\u057a\u057b\u0005\u0005\u0000\u0000\u057b"+
		"\u057d\u0003\u0112\u0089\u0000\u057c\u057a\u0001\u0000\u0000\u0000\u057d"+
		"\u0580\u0001\u0000\u0000\u0000\u057e\u057c\u0001\u0000\u0000\u0000\u057e"+
		"\u057f\u0001\u0000\u0000\u0000\u057f5\u0001\u0000\u0000\u0000\u0580\u057e"+
		"\u0001\u0000\u0000\u0000\u0581\u0582\u0005\u0111\u0000\u0000\u0582\u0587"+
		"\u00038\u001c\u0000\u0583\u0584\u0005\u0004\u0000\u0000\u0584\u0586\u0003"+
		"8\u001c\u0000\u0585\u0583\u0001\u0000\u0000\u0000\u0586\u0589\u0001\u0000"+
		"\u0000\u0000\u0587\u0585\u0001\u0000\u0000\u0000\u0587\u0588\u0001\u0000"+
		"\u0000\u0000\u05887\u0001\u0000\u0000\u0000\u0589\u0587\u0001\u0000\u0000"+
		"\u0000\u058a\u058c\u0003\u010e\u0087\u0000\u058b\u058d\u0003\u00a0P\u0000"+
		"\u058c\u058b\u0001\u0000\u0000\u0000\u058c\u058d\u0001\u0000\u0000\u0000"+
		"\u058d\u058f\u0001\u0000\u0000\u0000\u058e\u0590\u0005 \u0000\u0000\u058f"+
		"\u058e\u0001\u0000\u0000\u0000\u058f\u0590\u0001\u0000\u0000\u0000\u0590"+
		"\u0591\u0001\u0000\u0000\u0000\u0591\u0592\u0005\u0002\u0000\u0000\u0592"+
		"\u0593\u0003&\u0013\u0000\u0593\u0594\u0005\u0003\u0000\u0000\u05949\u0001"+
		"\u0000\u0000\u0000\u0595\u0596\u0005\u0014\u0000\u0000\u0596\u0597\u0003"+
		"\u00b8\\\u0000\u0597;\u0001\u0000\u0000\u0000\u0598\u0599\u0005\u00aa"+
		"\u0000\u0000\u0599\u05a6\u0003>\u001f\u0000\u059a\u059b\u0005\u00b5\u0000"+
		"\u0000\u059b\u059c\u0005(\u0000\u0000\u059c\u05a6\u0003\u00c2a\u0000\u059d"+
		"\u05a6\u0003 \u0010\u0000\u059e\u05a6\u0003\u001e\u000f\u0000\u059f\u05a6"+
		"\u0003\u00b4Z\u0000\u05a0\u05a6\u0003J%\u0000\u05a1\u05a6\u0003\"\u0011"+
		"\u0000\u05a2\u05a6\u0003$\u0012\u0000\u05a3\u05a4\u0005\u00f1\u0000\u0000"+
		"\u05a4\u05a6\u0003>\u001f\u0000\u05a5\u0598\u0001\u0000\u0000\u0000\u05a5"+
		"\u059a\u0001\u0000\u0000\u0000\u05a5\u059d\u0001\u0000\u0000\u0000\u05a5"+
		"\u059e\u0001\u0000\u0000\u0000\u05a5\u059f\u0001\u0000\u0000\u0000\u05a5"+
		"\u05a0\u0001\u0000\u0000\u0000\u05a5\u05a1\u0001\u0000\u0000\u0000\u05a5"+
		"\u05a2\u0001\u0000\u0000\u0000\u05a5\u05a3\u0001\u0000\u0000\u0000\u05a6"+
		"\u05a9\u0001\u0000\u0000\u0000\u05a7\u05a5\u0001\u0000\u0000\u0000\u05a7"+
		"\u05a8\u0001\u0000\u0000\u0000\u05a8=\u0001\u0000\u0000\u0000\u05a9\u05a7"+
		"\u0001\u0000\u0000\u0000\u05aa\u05ab\u0005\u0002\u0000\u0000\u05ab\u05b0"+
		"\u0003@ \u0000\u05ac\u05ad\u0005\u0004\u0000\u0000\u05ad\u05af\u0003@"+
		" \u0000\u05ae\u05ac\u0001\u0000\u0000\u0000\u05af\u05b2\u0001\u0000\u0000"+
		"\u0000\u05b0\u05ae\u0001\u0000\u0000\u0000\u05b0\u05b1\u0001\u0000\u0000"+
		"\u0000\u05b1\u05b3\u0001\u0000\u0000\u0000\u05b2\u05b0\u0001\u0000\u0000"+
		"\u0000\u05b3\u05b4\u0005\u0003\u0000\u0000\u05b4?\u0001\u0000\u0000\u0000"+
		"\u05b5\u05ba\u0003B!\u0000\u05b6\u05b8\u0005\u0114\u0000\u0000\u05b7\u05b6"+
		"\u0001\u0000\u0000\u0000\u05b7\u05b8\u0001\u0000\u0000\u0000\u05b8\u05b9"+
		"\u0001\u0000\u0000\u0000\u05b9\u05bb\u0003D\"\u0000\u05ba\u05b7\u0001"+
		"\u0000\u0000\u0000\u05ba\u05bb\u0001\u0000\u0000\u0000\u05bbA\u0001\u0000"+
		"\u0000\u0000\u05bc\u05c1\u0003\u0112\u0089\u0000\u05bd\u05be\u0005\u0005"+
		"\u0000\u0000\u05be\u05c0\u0003\u0112\u0089\u0000\u05bf\u05bd\u0001\u0000"+
		"\u0000\u0000\u05c0\u05c3\u0001\u0000\u0000\u0000\u05c1\u05bf\u0001\u0000"+
		"\u0000\u0000\u05c1\u05c2\u0001\u0000\u0000\u0000\u05c2\u05c6\u0001\u0000"+
		"\u0000\u0000\u05c3\u05c1\u0001\u0000\u0000\u0000\u05c4\u05c6\u0005\u0126"+
		"\u0000\u0000\u05c5\u05bc\u0001\u0000\u0000\u0000\u05c5\u05c4\u0001\u0000"+
		"\u0000\u0000\u05c6C\u0001\u0000\u0000\u0000\u05c7\u05cc\u0005\u012a\u0000"+
		"\u0000\u05c8\u05cc\u0005\u012c\u0000\u0000\u05c9\u05cc\u0003\u00deo\u0000"+
		"\u05ca\u05cc\u0005\u0126\u0000\u0000\u05cb\u05c7\u0001\u0000\u0000\u0000"+
		"\u05cb\u05c8\u0001\u0000\u0000\u0000\u05cb\u05c9\u0001\u0000\u0000\u0000"+
		"\u05cb\u05ca\u0001\u0000\u0000\u0000\u05ccE\u0001\u0000\u0000\u0000\u05cd"+
		"\u05ce\u0005\u0002\u0000\u0000\u05ce\u05d3\u0003\u00d6k\u0000\u05cf\u05d0"+
		"\u0005\u0004\u0000\u0000\u05d0\u05d2\u0003\u00d6k\u0000\u05d1\u05cf\u0001"+
		"\u0000\u0000\u0000\u05d2\u05d5\u0001\u0000\u0000\u0000\u05d3\u05d1\u0001"+
		"\u0000\u0000\u0000\u05d3\u05d4\u0001\u0000\u0000\u0000\u05d4\u05d6\u0001"+
		"\u0000\u0000\u0000\u05d5\u05d3\u0001\u0000\u0000\u0000\u05d6\u05d7\u0005"+
		"\u0003\u0000\u0000\u05d7G\u0001\u0000\u0000\u0000\u05d8\u05d9\u0005\u0002"+
		"\u0000\u0000\u05d9\u05de\u0003F#\u0000\u05da\u05db\u0005\u0004\u0000\u0000"+
		"\u05db\u05dd\u0003F#\u0000\u05dc\u05da\u0001\u0000\u0000\u0000\u05dd\u05e0"+
		"\u0001\u0000\u0000\u0000\u05de\u05dc\u0001\u0000\u0000\u0000\u05de\u05df"+
		"\u0001\u0000\u0000\u0000\u05df\u05e1\u0001\u0000\u0000\u0000\u05e0\u05de"+
		"\u0001\u0000\u0000\u0000\u05e1\u05e2\u0005\u0003\u0000\u0000\u05e2I\u0001"+
		"\u0000\u0000\u0000\u05e3\u05e4\u0005\u00e9\u0000\u0000\u05e4\u05e5\u0005"+
		" \u0000\u0000\u05e5\u05ea\u0003L&\u0000\u05e6\u05e7\u0005\u00e9\u0000"+
		"\u0000\u05e7\u05e8\u0005(\u0000\u0000\u05e8\u05ea\u0003N\'\u0000\u05e9"+
		"\u05e3\u0001\u0000\u0000\u0000\u05e9\u05e6\u0001\u0000\u0000\u0000\u05ea"+
		"K\u0001\u0000\u0000\u0000\u05eb\u05ec\u0005\u0080\u0000\u0000\u05ec\u05ed"+
		"\u0005\u0126\u0000\u0000\u05ed\u05ee\u0005\u00af\u0000\u0000\u05ee\u05f1"+
		"\u0005\u0126\u0000\u0000\u05ef\u05f1\u0003\u0112\u0089\u0000\u05f0\u05eb"+
		"\u0001\u0000\u0000\u0000\u05f0\u05ef\u0001\u0000\u0000\u0000\u05f1M\u0001"+
		"\u0000\u0000\u0000\u05f2\u05f6\u0005\u0126\u0000\u0000\u05f3\u05f4\u0005"+
		"\u0111\u0000\u0000\u05f4\u05f5\u0005\u00dd\u0000\u0000\u05f5\u05f7\u0003"+
		">\u001f\u0000\u05f6\u05f3\u0001\u0000\u0000\u0000\u05f6\u05f7\u0001\u0000"+
		"\u0000\u0000\u05f7O\u0001\u0000\u0000\u0000\u05f8\u05f9\u0003\u0112\u0089"+
		"\u0000\u05f9\u05fa\u0005\u0126\u0000\u0000\u05faQ\u0001\u0000\u0000\u0000"+
		"\u05fb\u05fc\u0003(\u0014\u0000\u05fc\u05fd\u0003X,\u0000\u05fd\u05fe"+
		"\u0003T*\u0000\u05fe\u062f\u0001\u0000\u0000\u0000\u05ff\u0601\u0003~"+
		"?\u0000\u0600\u0602\u0003V+\u0000\u0601\u0600\u0001\u0000\u0000\u0000"+
		"\u0602\u0603\u0001\u0000\u0000\u0000\u0603\u0601\u0001\u0000\u0000\u0000"+
		"\u0603\u0604\u0001\u0000\u0000\u0000\u0604\u062f\u0001\u0000\u0000\u0000"+
		"\u0605\u0606\u0005L\u0000\u0000\u0606\u0607\u0005n\u0000\u0000\u0607\u0608"+
		"\u0003\u00b8\\\u0000\u0608\u060a\u0003\u00b2Y\u0000\u0609\u060b\u0003"+
		"v;\u0000\u060a\u0609\u0001\u0000\u0000\u0000\u060a\u060b\u0001\u0000\u0000"+
		"\u0000\u060b\u062f\u0001\u0000\u0000\u0000\u060c\u060d\u0005\u0108\u0000"+
		"\u0000\u060d\u060e\u0003\u00b8\\\u0000\u060e\u060f\u0003\u00b2Y\u0000"+
		"\u060f\u0611\u0003h4\u0000\u0610\u0612\u0003v;\u0000\u0611\u0610\u0001"+
		"\u0000\u0000\u0000\u0611\u0612\u0001\u0000\u0000\u0000\u0612\u062f\u0001"+
		"\u0000\u0000\u0000\u0613\u0614\u0005\u009b\u0000\u0000\u0614\u0615\u0005"+
		"\u0084\u0000\u0000\u0615\u0616\u0003\u00b8\\\u0000\u0616\u0617\u0003\u00b2"+
		"Y\u0000\u0617\u061d\u0005\u0014\u0000\u0000\u0618\u061e\u0003\u00b8\\"+
		"\u0000\u0619\u061a\u0005\u0002\u0000\u0000\u061a\u061b\u0003&\u0013\u0000"+
		"\u061b\u061c\u0005\u0003\u0000\u0000\u061c\u061e\u0001\u0000\u0000\u0000"+
		"\u061d\u0618\u0001\u0000\u0000\u0000\u061d\u0619\u0001\u0000\u0000\u0000"+
		"\u061e\u061f\u0001\u0000\u0000\u0000\u061f\u0620\u0003\u00b2Y\u0000\u0620"+
		"\u0621\u0005\u00a7\u0000\u0000\u0621\u0625\u0003\u00ceg\u0000\u0622\u0624"+
		"\u0003j5\u0000\u0623\u0622\u0001\u0000\u0000\u0000\u0624\u0627\u0001\u0000"+
		"\u0000\u0000\u0625\u0623\u0001\u0000\u0000\u0000\u0625\u0626\u0001\u0000"+
		"\u0000\u0000\u0626\u062b\u0001\u0000\u0000\u0000\u0627\u0625\u0001\u0000"+
		"\u0000\u0000\u0628\u062a\u0003l6\u0000\u0629\u0628\u0001\u0000\u0000\u0000"+
		"\u062a\u062d\u0001\u0000\u0000\u0000\u062b\u0629\u0001\u0000\u0000\u0000"+
		"\u062b\u062c\u0001\u0000\u0000\u0000\u062c\u062f\u0001\u0000\u0000\u0000"+
		"\u062d\u062b\u0001\u0000\u0000\u0000\u062e\u05fb\u0001\u0000\u0000\u0000"+
		"\u062e\u05ff\u0001\u0000\u0000\u0000\u062e\u0605\u0001\u0000\u0000\u0000"+
		"\u062e\u060c\u0001\u0000\u0000\u0000\u062e\u0613\u0001\u0000\u0000\u0000"+
		"\u062fS\u0001\u0000\u0000\u0000\u0630\u0631\u0005\u00ac\u0000\u0000\u0631"+
		"\u0632\u0005(\u0000\u0000\u0632\u0637\u0003\\.\u0000\u0633\u0634\u0005"+
		"\u0004\u0000\u0000\u0634\u0636\u0003\\.\u0000\u0635\u0633\u0001\u0000"+
		"\u0000\u0000\u0636\u0639\u0001\u0000\u0000\u0000\u0637\u0635\u0001\u0000"+
		"\u0000\u0000\u0637\u0638\u0001\u0000\u0000\u0000\u0638\u063b\u0001\u0000"+
		"\u0000\u0000\u0639\u0637\u0001\u0000\u0000\u0000\u063a\u0630\u0001\u0000"+
		"\u0000\u0000\u063a\u063b\u0001\u0000\u0000\u0000\u063b\u0646\u0001\u0000"+
		"\u0000\u0000\u063c\u063d\u00050\u0000\u0000\u063d\u063e\u0005(\u0000\u0000"+
		"\u063e\u0643\u0003\u00cae\u0000\u063f\u0640\u0005\u0004\u0000\u0000\u0640"+
		"\u0642\u0003\u00cae\u0000\u0641\u063f\u0001\u0000\u0000\u0000\u0642\u0645"+
		"\u0001\u0000\u0000\u0000\u0643\u0641\u0001\u0000\u0000\u0000\u0643\u0644"+
		"\u0001\u0000\u0000\u0000\u0644\u0647\u0001\u0000\u0000\u0000\u0645\u0643"+
		"\u0001\u0000\u0000\u0000\u0646\u063c\u0001\u0000\u0000\u0000\u0646\u0647"+
		"\u0001\u0000\u0000\u0000\u0647\u0652\u0001\u0000\u0000\u0000\u0648\u0649"+
		"\u0005T\u0000\u0000\u0649\u064a\u0005(\u0000\u0000\u064a\u064f\u0003\u00ca"+
		"e\u0000\u064b\u064c\u0005\u0004\u0000\u0000\u064c\u064e\u0003\u00cae\u0000"+
		"\u064d\u064b\u0001\u0000\u0000\u0000\u064e\u0651\u0001\u0000\u0000\u0000"+
		"\u064f\u064d\u0001\u0000\u0000\u0000\u064f\u0650\u0001\u0000\u0000\u0000"+
		"\u0650\u0653\u0001\u0000\u0000\u0000\u0651\u064f\u0001\u0000\u0000\u0000"+
		"\u0652\u0648\u0001\u0000\u0000\u0000\u0652\u0653\u0001\u0000\u0000\u0000"+
		"\u0653\u065e\u0001\u0000\u0000\u0000\u0654\u0655\u0005\u00e5\u0000\u0000"+
		"\u0655\u0656\u0005(\u0000\u0000\u0656\u065b\u0003\\.\u0000\u0657\u0658"+
		"\u0005\u0004\u0000\u0000\u0658\u065a\u0003\\.\u0000\u0659\u0657\u0001"+
		"\u0000\u0000\u0000\u065a\u065d\u0001\u0000\u0000\u0000\u065b\u0659\u0001"+
		"\u0000\u0000\u0000\u065b\u065c\u0001\u0000\u0000\u0000\u065c\u065f\u0001"+
		"\u0000\u0000\u0000\u065d\u065b\u0001\u0000\u0000\u0000\u065e\u0654\u0001"+
		"\u0000\u0000\u0000\u065e\u065f\u0001\u0000\u0000\u0000\u065f\u0661\u0001"+
		"\u0000\u0000\u0000\u0660\u0662\u0003\u00fe\u007f\u0000\u0661\u0660\u0001"+
		"\u0000\u0000\u0000\u0661\u0662\u0001\u0000\u0000\u0000\u0662\u0668\u0001"+
		"\u0000\u0000\u0000\u0663\u0666\u0005\u008f\u0000\u0000\u0664\u0667\u0005"+
		"\u0018\u0000\u0000\u0665\u0667\u0003\u00cae\u0000\u0666\u0664\u0001\u0000"+
		"\u0000\u0000\u0666\u0665\u0001\u0000\u0000\u0000\u0667\u0669\u0001\u0000"+
		"\u0000\u0000\u0668\u0663\u0001\u0000\u0000\u0000\u0668\u0669\u0001\u0000"+
		"\u0000\u0000\u0669U\u0001\u0000\u0000\u0000\u066a\u066b\u0003(\u0014\u0000"+
		"\u066b\u066c\u0003`0\u0000\u066cW\u0001\u0000\u0000\u0000\u066d\u066e"+
		"\u0006,\uffff\uffff\u0000\u066e\u066f\u0003Z-\u0000\u066f\u0687\u0001"+
		"\u0000\u0000\u0000\u0670\u0671\n\u0003\u0000\u0000\u0671\u0672\u0004,"+
		"\u0001\u0000\u0672\u0674\u0007\u000f\u0000\u0000\u0673\u0675\u0003\u0092"+
		"I\u0000\u0674\u0673\u0001\u0000\u0000\u0000\u0674\u0675\u0001\u0000\u0000"+
		"\u0000\u0675\u0676\u0001\u0000\u0000\u0000\u0676\u0686\u0003X,\u0004\u0677"+
		"\u0678\n\u0002\u0000\u0000\u0678\u0679\u0004,\u0003\u0000\u0679\u067b"+
		"\u0005\u0082\u0000\u0000\u067a\u067c\u0003\u0092I\u0000\u067b\u067a\u0001"+
		"\u0000\u0000\u0000\u067b\u067c\u0001\u0000\u0000\u0000\u067c\u067d\u0001"+
		"\u0000\u0000\u0000\u067d\u0686\u0003X,\u0003\u067e\u067f\n\u0001\u0000"+
		"\u0000\u067f\u0680\u0004,\u0005\u0000\u0680\u0682\u0007\u0010\u0000\u0000"+
		"\u0681\u0683\u0003\u0092I\u0000\u0682\u0681\u0001\u0000\u0000\u0000\u0682"+
		"\u0683\u0001\u0000\u0000\u0000\u0683\u0684\u0001\u0000\u0000\u0000\u0684"+
		"\u0686\u0003X,\u0002\u0685\u0670\u0001\u0000\u0000\u0000\u0685\u0677\u0001"+
		"\u0000\u0000\u0000\u0685\u067e\u0001\u0000\u0000\u0000\u0686\u0689\u0001"+
		"\u0000\u0000\u0000\u0687\u0685\u0001\u0000\u0000\u0000\u0687\u0688\u0001"+
		"\u0000\u0000\u0000\u0688Y\u0001\u0000\u0000\u0000\u0689\u0687\u0001\u0000"+
		"\u0000\u0000\u068a\u0694\u0003b1\u0000\u068b\u0694\u0003^/\u0000\u068c"+
		"\u068d\u0005\u0010\u0000\u0000\u068d\u0694\u0003\u00b8\\\u0000\u068e\u0694"+
		"\u0003\u00aeW\u0000\u068f\u0690\u0005\u0002\u0000\u0000\u0690\u0691\u0003"+
		"&\u0013\u0000\u0691\u0692\u0005\u0003\u0000\u0000\u0692\u0694\u0001\u0000"+
		"\u0000\u0000\u0693\u068a\u0001\u0000\u0000\u0000\u0693\u068b\u0001\u0000"+
		"\u0000\u0000\u0693\u068c\u0001\u0000\u0000\u0000\u0693\u068e\u0001\u0000"+
		"\u0000\u0000\u0693\u068f\u0001\u0000\u0000\u0000\u0694[\u0001\u0000\u0000"+
		"\u0000\u0695\u0697\u0003\u00cae\u0000\u0696\u0698\u0007\u0011\u0000\u0000"+
		"\u0697\u0696\u0001\u0000\u0000\u0000\u0697\u0698\u0001\u0000\u0000\u0000"+
		"\u0698\u069b\u0001\u0000\u0000\u0000\u0699\u069a\u0005\u00a5\u0000\u0000"+
		"\u069a\u069c\u0007\u0012\u0000\u0000\u069b\u0699\u0001\u0000\u0000\u0000"+
		"\u069b\u069c\u0001\u0000\u0000\u0000\u069c]\u0001\u0000\u0000\u0000\u069d"+
		"\u069f\u0003~?\u0000\u069e\u06a0\u0003`0\u0000\u069f\u069e\u0001\u0000"+
		"\u0000\u0000\u06a0\u06a1\u0001\u0000\u0000\u0000\u06a1\u069f\u0001\u0000"+
		"\u0000\u0000\u06a1\u06a2\u0001\u0000\u0000\u0000\u06a2_\u0001\u0000\u0000"+
		"\u0000\u06a3\u06a5\u0003d2\u0000\u06a4\u06a6\u0003v;\u0000\u06a5\u06a4"+
		"\u0001\u0000\u0000\u0000\u06a5\u06a6\u0001\u0000\u0000\u0000\u06a6\u06a7"+
		"\u0001\u0000\u0000\u0000\u06a7\u06a8\u0003T*\u0000\u06a8\u06bf\u0001\u0000"+
		"\u0000\u0000\u06a9\u06ad\u0003f3\u0000\u06aa\u06ac\u0003\u0090H\u0000"+
		"\u06ab\u06aa\u0001\u0000\u0000\u0000\u06ac\u06af\u0001\u0000\u0000\u0000"+
		"\u06ad\u06ab\u0001\u0000\u0000\u0000\u06ad\u06ae\u0001\u0000\u0000\u0000"+
		"\u06ae\u06b1\u0001\u0000\u0000\u0000\u06af\u06ad\u0001\u0000\u0000\u0000"+
		"\u06b0\u06b2\u0003v;\u0000\u06b1\u06b0\u0001\u0000\u0000\u0000\u06b1\u06b2"+
		"\u0001\u0000\u0000\u0000\u06b2\u06b4\u0001\u0000\u0000\u0000\u06b3\u06b5"+
		"\u0003\u0080@\u0000\u06b4\u06b3\u0001\u0000\u0000\u0000\u06b4\u06b5\u0001"+
		"\u0000\u0000\u0000\u06b5\u06b7\u0001\u0000\u0000\u0000\u06b6\u06b8\u0003"+
		"x<\u0000\u06b7\u06b6\u0001\u0000\u0000\u0000\u06b7\u06b8\u0001\u0000\u0000"+
		"\u0000\u06b8\u06ba\u0001\u0000\u0000\u0000\u06b9\u06bb\u0003\u00fe\u007f"+
		"\u0000\u06ba\u06b9\u0001\u0000\u0000\u0000\u06ba\u06bb\u0001\u0000\u0000"+
		"\u0000\u06bb\u06bc\u0001\u0000\u0000\u0000\u06bc\u06bd\u0003T*\u0000\u06bd"+
		"\u06bf\u0001\u0000\u0000\u0000\u06be\u06a3\u0001\u0000\u0000\u0000\u06be"+
		"\u06a9\u0001\u0000\u0000\u0000\u06bfa\u0001\u0000\u0000\u0000\u06c0\u06c2"+
		"\u0003d2\u0000\u06c1\u06c3\u0003~?\u0000\u06c2\u06c1\u0001\u0000\u0000"+
		"\u0000\u06c2\u06c3\u0001\u0000\u0000\u0000\u06c3\u06c7\u0001\u0000\u0000"+
		"\u0000\u06c4\u06c6\u0003\u0090H\u0000\u06c5\u06c4\u0001\u0000\u0000\u0000"+
		"\u06c6\u06c9\u0001\u0000\u0000\u0000\u06c7\u06c5\u0001\u0000\u0000\u0000"+
		"\u06c7\u06c8\u0001\u0000\u0000\u0000\u06c8\u06cb\u0001\u0000\u0000\u0000"+
		"\u06c9\u06c7\u0001\u0000\u0000\u0000\u06ca\u06cc\u0003v;\u0000\u06cb\u06ca"+
		"\u0001\u0000\u0000\u0000\u06cb\u06cc\u0001\u0000\u0000\u0000\u06cc\u06ce"+
		"\u0001\u0000\u0000\u0000\u06cd\u06cf\u0003\u0080@\u0000\u06ce\u06cd\u0001"+
		"\u0000\u0000\u0000\u06ce\u06cf\u0001\u0000\u0000\u0000\u06cf\u06d1\u0001"+
		"\u0000\u0000\u0000\u06d0\u06d2\u0003x<\u0000\u06d1\u06d0\u0001\u0000\u0000"+
		"\u0000\u06d1\u06d2\u0001\u0000\u0000\u0000\u06d2\u06d4\u0001\u0000\u0000"+
		"\u0000\u06d3\u06d5\u0003\u00fe\u007f\u0000\u06d4\u06d3\u0001\u0000\u0000"+
		"\u0000\u06d4\u06d5\u0001\u0000\u0000\u0000\u06d5\u06ed\u0001\u0000\u0000"+
		"\u0000\u06d6\u06d8\u0003f3\u0000\u06d7\u06d9\u0003~?\u0000\u06d8\u06d7"+
		"\u0001\u0000\u0000\u0000\u06d8\u06d9\u0001\u0000\u0000\u0000\u06d9\u06dd"+
		"\u0001\u0000\u0000\u0000\u06da\u06dc\u0003\u0090H\u0000\u06db\u06da\u0001"+
		"\u0000\u0000\u0000\u06dc\u06df\u0001\u0000\u0000\u0000\u06dd\u06db\u0001"+
		"\u0000\u0000\u0000\u06dd\u06de\u0001\u0000\u0000\u0000\u06de\u06e1\u0001"+
		"\u0000\u0000\u0000\u06df\u06dd\u0001\u0000\u0000\u0000\u06e0\u06e2\u0003"+
		"v;\u0000\u06e1\u06e0\u0001\u0000\u0000\u0000\u06e1\u06e2\u0001\u0000\u0000"+
		"\u0000\u06e2\u06e4\u0001\u0000\u0000\u0000\u06e3\u06e5\u0003\u0080@\u0000"+
		"\u06e4\u06e3\u0001\u0000\u0000\u0000\u06e4\u06e5\u0001\u0000\u0000\u0000"+
		"\u06e5\u06e7\u0001\u0000\u0000\u0000\u06e6\u06e8\u0003x<\u0000\u06e7\u06e6"+
		"\u0001\u0000\u0000\u0000\u06e7\u06e8\u0001\u0000\u0000\u0000\u06e8\u06ea"+
		"\u0001\u0000\u0000\u0000\u06e9\u06eb\u0003\u00fe\u007f\u0000\u06ea\u06e9"+
		"\u0001\u0000\u0000\u0000\u06ea\u06eb\u0001\u0000\u0000\u0000\u06eb\u06ed"+
		"\u0001\u0000\u0000\u0000\u06ec\u06c0\u0001\u0000\u0000\u0000\u06ec\u06d6"+
		"\u0001\u0000\u0000\u0000\u06edc\u0001\u0000\u0000\u0000\u06ee\u06ef\u0005"+
		"\u00d9\u0000\u0000\u06ef\u06f0\u0005\u00fa\u0000\u0000\u06f0\u06f2\u0005"+
		"\u0002\u0000\u0000\u06f1\u06f3\u0003\u0092I\u0000\u06f2\u06f1\u0001\u0000"+
		"\u0000\u0000\u06f2\u06f3\u0001\u0000\u0000\u0000\u06f3\u06f4\u0001\u0000"+
		"\u0000\u0000\u06f4\u06f5\u0003\u00ccf\u0000\u06f5\u06f6\u0005\u0003\u0000"+
		"\u0000\u06f6\u0702\u0001\u0000\u0000\u0000\u06f7\u06f9\u0005\u0099\u0000"+
		"\u0000\u06f8\u06fa\u0003\u0092I\u0000\u06f9\u06f8\u0001\u0000\u0000\u0000"+
		"\u06f9\u06fa\u0001\u0000\u0000\u0000\u06fa\u06fb\u0001\u0000\u0000\u0000"+
		"\u06fb\u0702\u0003\u00ccf\u0000\u06fc\u06fe\u0005\u00c5\u0000\u0000\u06fd"+
		"\u06ff\u0003\u0092I\u0000\u06fe\u06fd\u0001\u0000\u0000\u0000\u06fe\u06ff"+
		"\u0001\u0000\u0000\u0000\u06ff\u0700\u0001\u0000\u0000\u0000\u0700\u0702"+
		"\u0003\u00ccf\u0000\u0701\u06ee\u0001\u0000\u0000\u0000\u0701\u06f7\u0001"+
		"\u0000\u0000\u0000\u0701\u06fc\u0001\u0000\u0000\u0000\u0702\u0704\u0001"+
		"\u0000\u0000\u0000\u0703\u0705\u0003\u00b4Z\u0000\u0704\u0703\u0001\u0000"+
		"\u0000\u0000\u0704\u0705\u0001\u0000\u0000\u0000\u0705\u0708\u0001\u0000"+
		"\u0000\u0000\u0706\u0707\u0005\u00c3\u0000\u0000\u0707\u0709\u0005\u0126"+
		"\u0000\u0000\u0708\u0706\u0001\u0000\u0000\u0000\u0708\u0709\u0001\u0000"+
		"\u0000\u0000\u0709\u070a\u0001\u0000\u0000\u0000\u070a\u070b\u0005\u0014"+
		"\u0000\u0000\u070b\u0718\u0005\u0126\u0000\u0000\u070c\u0716\u0005 \u0000"+
		"\u0000\u070d\u0717\u0003\u00a2Q\u0000\u070e\u0717\u0003\u00f4z\u0000\u070f"+
		"\u0712\u0005\u0002\u0000\u0000\u0710\u0713\u0003\u00a2Q\u0000\u0711\u0713"+
		"\u0003\u00f4z\u0000\u0712\u0710\u0001\u0000\u0000\u0000\u0712\u0711\u0001"+
		"\u0000\u0000\u0000\u0713\u0714\u0001\u0000\u0000\u0000\u0714\u0715\u0005"+
		"\u0003\u0000\u0000\u0715\u0717\u0001\u0000\u0000\u0000\u0716\u070d\u0001"+
		"\u0000\u0000\u0000\u0716\u070e\u0001\u0000\u0000\u0000\u0716\u070f\u0001"+
		"\u0000\u0000\u0000\u0717\u0719\u0001\u0000\u0000\u0000\u0718\u070c\u0001"+
		"\u0000\u0000\u0000\u0718\u0719\u0001\u0000\u0000\u0000\u0719\u071b\u0001"+
		"\u0000\u0000\u0000\u071a\u071c\u0003\u00b4Z\u0000\u071b\u071a\u0001\u0000"+
		"\u0000\u0000\u071b\u071c\u0001\u0000\u0000\u0000\u071c\u071f\u0001\u0000"+
		"\u0000\u0000\u071d\u071e\u0005\u00c2\u0000\u0000\u071e\u0720\u0005\u0126"+
		"\u0000\u0000\u071f\u071d\u0001\u0000\u0000\u0000\u071f\u0720\u0001\u0000"+
		"\u0000\u0000\u0720e\u0001\u0000\u0000\u0000\u0721\u0725\u0005\u00d9\u0000"+
		"\u0000\u0722\u0724\u0003z=\u0000\u0723\u0722\u0001\u0000\u0000\u0000\u0724"+
		"\u0727\u0001\u0000\u0000\u0000\u0725\u0723\u0001\u0000\u0000\u0000\u0725"+
		"\u0726\u0001\u0000\u0000\u0000\u0726\u0729\u0001\u0000\u0000\u0000\u0727"+
		"\u0725\u0001\u0000\u0000\u0000\u0728\u072a\u0003\u0092I\u0000\u0729\u0728"+
		"\u0001\u0000\u0000\u0000\u0729\u072a\u0001\u0000\u0000\u0000\u072a\u072b"+
		"\u0001\u0000\u0000\u0000\u072b\u072c\u0003\u00c0`\u0000\u072cg\u0001\u0000"+
		"\u0000\u0000\u072d\u072e\u0005\u00df\u0000\u0000\u072e\u072f\u0003r9\u0000"+
		"\u072fi\u0001\u0000\u0000\u0000\u0730\u0731\u0005\u010e\u0000\u0000\u0731"+
		"\u0734\u0005\u009a\u0000\u0000\u0732\u0733\u0005\u001b\u0000\u0000\u0733"+
		"\u0735\u0003\u00ceg\u0000\u0734\u0732\u0001\u0000\u0000\u0000\u0734\u0735"+
		"\u0001\u0000\u0000\u0000\u0735\u0736\u0001\u0000\u0000\u0000\u0736\u0737"+
		"\u0005\u00f4\u0000\u0000\u0737\u0738\u0003n7\u0000\u0738k\u0001\u0000"+
		"\u0000\u0000\u0739\u073a\u0005\u010e\u0000\u0000\u073a\u073b\u0005\u00a3"+
		"\u0000\u0000\u073b\u073e\u0005\u009a\u0000\u0000\u073c\u073d\u0005\u001b"+
		"\u0000\u0000\u073d\u073f\u0003\u00ceg\u0000\u073e\u073c\u0001\u0000\u0000"+
		"\u0000\u073e\u073f\u0001\u0000\u0000\u0000\u073f\u0740\u0001\u0000\u0000"+
		"\u0000\u0740\u0741\u0005\u00f4\u0000\u0000\u0741\u0742\u0003p8\u0000\u0742"+
		"m\u0001\u0000\u0000\u0000\u0743\u074b\u0005L\u0000\u0000\u0744\u0745\u0005"+
		"\u0108\u0000\u0000\u0745\u0746\u0005\u00df\u0000\u0000\u0746\u074b\u0005"+
		"\u011e\u0000\u0000\u0747\u0748\u0005\u0108\u0000\u0000\u0748\u0749\u0005"+
		"\u00df\u0000\u0000\u0749\u074b\u0003r9\u0000\u074a\u0743\u0001\u0000\u0000"+
		"\u0000\u074a\u0744\u0001\u0000\u0000\u0000\u074a\u0747\u0001\u0000\u0000"+
		"\u0000\u074bo\u0001\u0000\u0000\u0000\u074c\u074d\u0005\u0081\u0000\u0000"+
		"\u074d\u075f\u0005\u011e\u0000\u0000\u074e\u074f\u0005\u0081\u0000\u0000"+
		"\u074f\u0750\u0005\u0002\u0000\u0000\u0750\u0751\u0003\u00b6[\u0000\u0751"+
		"\u0752\u0005\u0003\u0000\u0000\u0752\u0753\u0005\u010b\u0000\u0000\u0753"+
		"\u0754\u0005\u0002\u0000\u0000\u0754\u0759\u0003\u00cae\u0000\u0755\u0756"+
		"\u0005\u0004\u0000\u0000\u0756\u0758\u0003\u00cae\u0000\u0757\u0755\u0001"+
		"\u0000\u0000\u0000\u0758\u075b\u0001\u0000\u0000\u0000\u0759\u0757\u0001"+
		"\u0000\u0000\u0000\u0759\u075a\u0001\u0000\u0000\u0000\u075a\u075c\u0001"+
		"\u0000\u0000\u0000\u075b\u0759\u0001\u0000\u0000\u0000\u075c\u075d\u0005"+
		"\u0003\u0000\u0000\u075d\u075f\u0001\u0000\u0000\u0000\u075e\u074c\u0001"+
		"\u0000\u0000\u0000\u075e\u074e\u0001\u0000\u0000\u0000\u075fq\u0001\u0000"+
		"\u0000\u0000\u0760\u0765\u0003t:\u0000\u0761\u0762\u0005\u0004\u0000\u0000"+
		"\u0762\u0764\u0003t:\u0000\u0763\u0761\u0001\u0000\u0000\u0000\u0764\u0767"+
		"\u0001\u0000\u0000\u0000\u0765\u0763\u0001\u0000\u0000\u0000\u0765\u0766"+
		"\u0001\u0000\u0000\u0000\u0766s\u0001\u0000\u0000\u0000\u0767\u0765\u0001"+
		"\u0000\u0000\u0000\u0768\u0769\u0003\u00b8\\\u0000\u0769\u076a\u0005\u0114"+
		"\u0000\u0000\u076a\u076b\u0003\u00cae\u0000\u076bu\u0001\u0000\u0000\u0000"+
		"\u076c\u076d\u0005\u010f\u0000\u0000\u076d\u076e\u0003\u00ceg\u0000\u076e"+
		"w\u0001\u0000\u0000\u0000\u076f\u0770\u0005v\u0000\u0000\u0770\u0771\u0003"+
		"\u00ceg\u0000\u0771y\u0001\u0000\u0000\u0000\u0772\u0773\u0005\u0006\u0000"+
		"\u0000\u0773\u077a\u0003|>\u0000\u0774\u0776\u0005\u0004\u0000\u0000\u0775"+
		"\u0774\u0001\u0000\u0000\u0000\u0775\u0776\u0001\u0000\u0000\u0000\u0776"+
		"\u0777\u0001\u0000\u0000\u0000\u0777\u0779\u0003|>\u0000\u0778\u0775\u0001"+
		"\u0000\u0000\u0000\u0779\u077c\u0001\u0000\u0000\u0000\u077a\u0778\u0001"+
		"\u0000\u0000\u0000\u077a\u077b\u0001\u0000\u0000\u0000\u077b\u077d\u0001"+
		"\u0000\u0000\u0000\u077c\u077a\u0001\u0000\u0000\u0000\u077d\u077e\u0005"+
		"\u0007\u0000\u0000\u077e{\u0001\u0000\u0000\u0000\u077f\u078d\u0003\u0112"+
		"\u0089\u0000\u0780\u0781\u0003\u0112\u0089\u0000\u0781\u0782\u0005\u0002"+
		"\u0000\u0000\u0782\u0787\u0003\u00d4j\u0000\u0783\u0784\u0005\u0004\u0000"+
		"\u0000\u0784\u0786\u0003\u00d4j\u0000\u0785\u0783\u0001\u0000\u0000\u0000"+
		"\u0786\u0789\u0001\u0000\u0000\u0000\u0787\u0785\u0001\u0000\u0000\u0000"+
		"\u0787\u0788\u0001\u0000\u0000\u0000\u0788\u078a\u0001\u0000\u0000\u0000"+
		"\u0789\u0787\u0001\u0000\u0000\u0000\u078a\u078b\u0005\u0003\u0000\u0000"+
		"\u078b\u078d\u0001\u0000\u0000\u0000\u078c\u077f\u0001\u0000\u0000\u0000"+
		"\u078c\u0780\u0001\u0000\u0000\u0000\u078d}\u0001\u0000\u0000\u0000\u078e"+
		"\u078f\u0005n\u0000\u0000\u078f\u0794\u0003\u0094J\u0000\u0790\u0791\u0005"+
		"\u0004\u0000\u0000\u0791\u0793\u0003\u0094J\u0000\u0792\u0790\u0001\u0000"+
		"\u0000\u0000\u0793\u0796\u0001\u0000\u0000\u0000\u0794\u0792\u0001\u0000"+
		"\u0000\u0000\u0794\u0795\u0001\u0000\u0000\u0000\u0795\u079a\u0001\u0000"+
		"\u0000\u0000\u0796\u0794\u0001\u0000\u0000\u0000\u0797\u0799\u0003\u0090"+
		"H\u0000\u0798\u0797\u0001\u0000\u0000\u0000\u0799\u079c\u0001\u0000\u0000"+
		"\u0000\u079a\u0798\u0001\u0000\u0000\u0000\u079a\u079b\u0001\u0000\u0000"+
		"\u0000\u079b\u079e\u0001\u0000\u0000\u0000\u079c\u079a\u0001\u0000\u0000"+
		"\u0000\u079d\u079f\u0003\u008aE\u0000\u079e\u079d\u0001\u0000\u0000\u0000"+
		"\u079e\u079f\u0001\u0000\u0000\u0000\u079f\u007f\u0001\u0000\u0000\u0000"+
		"\u07a0\u07a1\u0005t\u0000\u0000\u07a1\u07a2\u0005(\u0000\u0000\u07a2\u07a7"+
		"\u0003\u0082A\u0000\u07a3\u07a4\u0005\u0004\u0000\u0000\u07a4\u07a6\u0003"+
		"\u0082A\u0000\u07a5\u07a3\u0001\u0000\u0000\u0000\u07a6\u07a9\u0001\u0000"+
		"\u0000\u0000\u07a7\u07a5\u0001\u0000\u0000\u0000\u07a7\u07a8\u0001\u0000"+
		"\u0000\u0000\u07a8\u07c8\u0001\u0000\u0000\u0000\u07a9\u07a7\u0001\u0000"+
		"\u0000\u0000\u07aa\u07ab\u0005t\u0000\u0000\u07ab\u07ac\u0005(\u0000\u0000"+
		"\u07ac\u07b1\u0003\u00cae\u0000\u07ad\u07ae\u0005\u0004\u0000\u0000\u07ae"+
		"\u07b0\u0003\u00cae\u0000\u07af\u07ad\u0001\u0000\u0000\u0000\u07b0\u07b3"+
		"\u0001\u0000\u0000\u0000\u07b1\u07af\u0001\u0000\u0000\u0000\u07b1\u07b2"+
		"\u0001\u0000\u0000\u0000\u07b2\u07c5\u0001\u0000\u0000\u0000\u07b3\u07b1"+
		"\u0001\u0000\u0000\u0000\u07b4\u07b5\u0005\u0111\u0000\u0000\u07b5\u07c6"+
		"\u0005\u00d4\u0000\u0000\u07b6\u07b7\u0005\u0111\u0000\u0000\u07b7\u07c6"+
		"\u0005A\u0000\u0000\u07b8\u07b9\u0005u\u0000\u0000\u07b9\u07ba\u0005\u00e1"+
		"\u0000\u0000\u07ba\u07bb\u0005\u0002\u0000\u0000\u07bb\u07c0\u0003\u0088"+
		"D\u0000\u07bc\u07bd\u0005\u0004\u0000\u0000\u07bd\u07bf\u0003\u0088D\u0000"+
		"\u07be\u07bc\u0001\u0000\u0000\u0000\u07bf\u07c2\u0001\u0000\u0000\u0000"+
		"\u07c0\u07be\u0001\u0000\u0000\u0000\u07c0\u07c1\u0001\u0000\u0000\u0000"+
		"\u07c1\u07c3\u0001\u0000\u0000\u0000\u07c2\u07c0\u0001\u0000\u0000\u0000"+
		"\u07c3\u07c4\u0005\u0003\u0000\u0000\u07c4\u07c6\u0001\u0000\u0000\u0000"+
		"\u07c5\u07b4\u0001\u0000\u0000\u0000\u07c5\u07b6\u0001\u0000\u0000\u0000"+
		"\u07c5\u07b8\u0001\u0000\u0000\u0000\u07c5\u07c6\u0001\u0000\u0000\u0000"+
		"\u07c6\u07c8\u0001\u0000\u0000\u0000\u07c7\u07a0\u0001\u0000\u0000\u0000"+
		"\u07c7\u07aa\u0001\u0000\u0000\u0000\u07c8\u0081\u0001\u0000\u0000\u0000"+
		"\u07c9\u07cc\u0003\u0084B\u0000\u07ca\u07cc\u0003\u00cae\u0000\u07cb\u07c9"+
		"\u0001\u0000\u0000\u0000\u07cb\u07ca\u0001\u0000\u0000\u0000\u07cc\u0083"+
		"\u0001\u0000\u0000\u0000\u07cd\u07ce\u0007\u0013\u0000\u0000\u07ce\u07cf"+
		"\u0005\u0002\u0000\u0000\u07cf\u07d4\u0003\u0088D\u0000\u07d0\u07d1\u0005"+
		"\u0004\u0000\u0000\u07d1\u07d3\u0003\u0088D\u0000\u07d2\u07d0\u0001\u0000"+
		"\u0000\u0000\u07d3\u07d6\u0001\u0000\u0000\u0000\u07d4\u07d2\u0001\u0000"+
		"\u0000\u0000\u07d4\u07d5\u0001\u0000\u0000\u0000\u07d5\u07d7\u0001\u0000"+
		"\u0000\u0000\u07d6\u07d4\u0001\u0000\u0000\u0000\u07d7\u07d8\u0005\u0003"+
		"\u0000\u0000\u07d8\u07e7\u0001\u0000\u0000\u0000\u07d9\u07da\u0005u\u0000"+
		"\u0000\u07da\u07db\u0005\u00e1\u0000\u0000\u07db\u07dc\u0005\u0002\u0000"+
		"\u0000\u07dc\u07e1\u0003\u0086C\u0000\u07dd\u07de\u0005\u0004\u0000\u0000"+
		"\u07de\u07e0\u0003\u0086C\u0000\u07df\u07dd\u0001\u0000\u0000\u0000\u07e0"+
		"\u07e3\u0001\u0000\u0000\u0000\u07e1\u07df\u0001\u0000\u0000\u0000\u07e1"+
		"\u07e2\u0001\u0000\u0000\u0000\u07e2\u07e4\u0001\u0000\u0000\u0000\u07e3"+
		"\u07e1\u0001\u0000\u0000\u0000\u07e4\u07e5\u0005\u0003\u0000\u0000\u07e5"+
		"\u07e7\u0001\u0000\u0000\u0000\u07e6\u07cd\u0001\u0000\u0000\u0000\u07e6"+
		"\u07d9\u0001\u0000\u0000\u0000\u07e7\u0085\u0001\u0000\u0000\u0000\u07e8"+
		"\u07eb\u0003\u0084B\u0000\u07e9\u07eb\u0003\u0088D\u0000\u07ea\u07e8\u0001"+
		"\u0000\u0000\u0000\u07ea\u07e9\u0001\u0000\u0000\u0000\u07eb\u0087\u0001"+
		"\u0000\u0000\u0000\u07ec\u07f5\u0005\u0002\u0000\u0000\u07ed\u07f2\u0003"+
		"\u00cae\u0000\u07ee\u07ef\u0005\u0004\u0000\u0000\u07ef\u07f1\u0003\u00ca"+
		"e\u0000\u07f0\u07ee\u0001\u0000\u0000\u0000\u07f1\u07f4\u0001\u0000\u0000"+
		"\u0000\u07f2\u07f0\u0001\u0000\u0000\u0000\u07f2\u07f3\u0001\u0000\u0000"+
		"\u0000\u07f3\u07f6\u0001\u0000\u0000\u0000\u07f4\u07f2\u0001\u0000\u0000"+
		"\u0000\u07f5\u07ed\u0001\u0000\u0000\u0000\u07f5\u07f6\u0001\u0000\u0000"+
		"\u0000\u07f6\u07f7\u0001\u0000\u0000\u0000\u07f7\u07fa\u0005\u0003\u0000"+
		"\u0000\u07f8\u07fa\u0003\u00cae\u0000\u07f9\u07ec\u0001\u0000\u0000\u0000"+
		"\u07f9\u07f8\u0001\u0000\u0000\u0000\u07fa\u0089\u0001\u0000\u0000\u0000"+
		"\u07fb\u07fc\u0005\u00b8\u0000\u0000\u07fc\u07fd\u0005\u0002\u0000\u0000"+
		"\u07fd\u07fe\u0003\u00c0`\u0000\u07fe\u07ff\u0005j\u0000\u0000\u07ff\u0800"+
		"\u0003\u008cF\u0000\u0800\u0801\u0005{\u0000\u0000\u0801\u0802\u0005\u0002"+
		"\u0000\u0000\u0802\u0807\u0003\u008eG\u0000\u0803\u0804\u0005\u0004\u0000"+
		"\u0000\u0804\u0806\u0003\u008eG\u0000\u0805\u0803\u0001\u0000\u0000\u0000"+
		"\u0806\u0809\u0001\u0000\u0000\u0000\u0807\u0805\u0001\u0000\u0000\u0000"+
		"\u0807\u0808\u0001\u0000\u0000\u0000\u0808\u080a\u0001\u0000\u0000\u0000"+
		"\u0809\u0807\u0001\u0000\u0000\u0000\u080a\u080b\u0005\u0003\u0000\u0000"+
		"\u080b\u080c\u0005\u0003\u0000\u0000\u080c\u008b\u0001\u0000\u0000\u0000"+
		"\u080d\u081a\u0003\u0112\u0089\u0000\u080e\u080f\u0005\u0002\u0000\u0000"+
		"\u080f\u0814\u0003\u0112\u0089\u0000\u0810\u0811\u0005\u0004\u0000\u0000"+
		"\u0811\u0813\u0003\u0112\u0089\u0000\u0812\u0810\u0001\u0000\u0000\u0000"+
		"\u0813\u0816\u0001\u0000\u0000\u0000\u0814\u0812\u0001\u0000\u0000\u0000"+
		"\u0814\u0815\u0001\u0000\u0000\u0000\u0815\u0817\u0001\u0000\u0000\u0000"+
		"\u0816\u0814\u0001\u0000\u0000\u0000\u0817\u0818\u0005\u0003\u0000\u0000"+
		"\u0818\u081a\u0001\u0000\u0000\u0000\u0819\u080d\u0001\u0000\u0000\u0000"+
		"\u0819\u080e\u0001\u0000\u0000\u0000\u081a\u008d\u0001\u0000\u0000\u0000"+
		"\u081b\u0820\u0003\u00cae\u0000\u081c\u081e\u0005 \u0000\u0000\u081d\u081c"+
		"\u0001\u0000\u0000\u0000\u081d\u081e\u0001\u0000\u0000\u0000\u081e\u081f"+
		"\u0001\u0000\u0000\u0000\u081f\u0821\u0003\u0112\u0089\u0000\u0820\u081d"+
		"\u0001\u0000\u0000\u0000\u0820\u0821\u0001\u0000\u0000\u0000\u0821\u008f"+
		"\u0001\u0000\u0000\u0000\u0822\u0823\u0005\u008a\u0000\u0000\u0823\u0825"+
		"\u0005\u010c\u0000\u0000\u0824\u0826\u0005\u00ae\u0000\u0000\u0825\u0824"+
		"\u0001\u0000\u0000\u0000\u0825\u0826\u0001\u0000\u0000\u0000\u0826\u0827"+
		"\u0001\u0000\u0000\u0000\u0827\u0828\u0003\u010c\u0086\u0000\u0828\u0831"+
		"\u0005\u0002\u0000\u0000\u0829\u082e\u0003\u00cae\u0000\u082a\u082b\u0005"+
		"\u0004\u0000\u0000\u082b\u082d\u0003\u00cae\u0000\u082c\u082a\u0001\u0000"+
		"\u0000\u0000\u082d\u0830\u0001\u0000\u0000\u0000\u082e\u082c\u0001\u0000"+
		"\u0000\u0000\u082e\u082f\u0001\u0000\u0000\u0000\u082f\u0832\u0001\u0000"+
		"\u0000\u0000\u0830\u082e\u0001\u0000\u0000\u0000\u0831\u0829\u0001\u0000"+
		"\u0000\u0000\u0831\u0832\u0001\u0000\u0000\u0000\u0832\u0833\u0001\u0000"+
		"\u0000\u0000\u0833\u0834\u0005\u0003\u0000\u0000\u0834\u0840\u0003\u0112"+
		"\u0089\u0000\u0835\u0837\u0005 \u0000\u0000\u0836\u0835\u0001\u0000\u0000"+
		"\u0000\u0836\u0837\u0001\u0000\u0000\u0000\u0837\u0838\u0001\u0000\u0000"+
		"\u0000\u0838\u083d\u0003\u0112\u0089\u0000\u0839\u083a\u0005\u0004\u0000"+
		"\u0000\u083a\u083c\u0003\u0112\u0089\u0000\u083b\u0839\u0001\u0000\u0000"+
		"\u0000\u083c\u083f\u0001\u0000\u0000\u0000\u083d\u083b\u0001\u0000\u0000"+
		"\u0000\u083d\u083e\u0001\u0000\u0000\u0000\u083e\u0841\u0001\u0000\u0000"+
		"\u0000\u083f\u083d\u0001\u0000\u0000\u0000\u0840\u0836\u0001\u0000\u0000"+
		"\u0000\u0840\u0841\u0001\u0000\u0000\u0000\u0841\u0091\u0001\u0000\u0000"+
		"\u0000\u0842\u0843\u0007\u0014\u0000\u0000\u0843\u0093\u0001\u0000\u0000"+
		"\u0000\u0844\u0846\u0005\u008a\u0000\u0000\u0845\u0844\u0001\u0000\u0000"+
		"\u0000\u0845\u0846\u0001\u0000\u0000\u0000\u0846\u0847\u0001\u0000\u0000"+
		"\u0000\u0847\u084b\u0003\u00acV\u0000\u0848\u084a\u0003\u0096K\u0000\u0849"+
		"\u0848\u0001\u0000\u0000\u0000\u084a\u084d\u0001\u0000\u0000\u0000\u084b"+
		"\u0849\u0001\u0000\u0000\u0000\u084b\u084c\u0001\u0000\u0000\u0000\u084c"+
		"\u0095\u0001\u0000\u0000\u0000\u084d\u084b\u0001\u0000\u0000\u0000\u084e"+
		"\u084f\u0003\u0098L\u0000\u084f\u0851\u0005\u0087\u0000\u0000\u0850\u0852"+
		"\u0005\u008a\u0000\u0000\u0851\u0850\u0001\u0000\u0000";
	private static final String _serializedATNSegment1 =
		"\u0000\u0851\u0852\u0001\u0000\u0000\u0000\u0852\u0853\u0001\u0000\u0000"+
		"\u0000\u0853\u0855\u0003\u00acV\u0000\u0854\u0856\u0003\u009aM\u0000\u0855"+
		"\u0854\u0001\u0000\u0000\u0000\u0855\u0856\u0001\u0000\u0000\u0000\u0856"+
		"\u0860\u0001\u0000\u0000\u0000\u0857\u0858\u0005\u00a1\u0000\u0000\u0858"+
		"\u0859\u0003\u0098L\u0000\u0859\u085b\u0005\u0087\u0000\u0000\u085a\u085c"+
		"\u0005\u008a\u0000\u0000\u085b\u085a\u0001\u0000\u0000\u0000\u085b\u085c"+
		"\u0001\u0000\u0000\u0000\u085c\u085d\u0001\u0000\u0000\u0000\u085d\u085e"+
		"\u0003\u00acV\u0000\u085e\u0860\u0001\u0000\u0000\u0000\u085f\u084e\u0001"+
		"\u0000\u0000\u0000\u085f\u0857\u0001\u0000\u0000\u0000\u0860\u0097\u0001"+
		"\u0000\u0000\u0000\u0861\u0863\u0005~\u0000\u0000\u0862\u0861\u0001\u0000"+
		"\u0000\u0000\u0862\u0863\u0001\u0000\u0000\u0000\u0863\u087a\u0001\u0000"+
		"\u0000\u0000\u0864\u087a\u0005@\u0000\u0000\u0865\u0867\u0005\u008d\u0000"+
		"\u0000\u0866\u0868\u0005\u00ae\u0000\u0000\u0867\u0866\u0001\u0000\u0000"+
		"\u0000\u0867\u0868\u0001\u0000\u0000\u0000\u0868\u087a\u0001\u0000\u0000"+
		"\u0000\u0869\u086b\u0005\u008d\u0000\u0000\u086a\u0869\u0001\u0000\u0000"+
		"\u0000\u086a\u086b\u0001\u0000\u0000\u0000\u086b\u086c\u0001\u0000\u0000"+
		"\u0000\u086c\u087a\u0005\u00da\u0000\u0000\u086d\u086f\u0005\u00cf\u0000"+
		"\u0000\u086e\u0870\u0005\u00ae\u0000\u0000\u086f\u086e\u0001\u0000\u0000"+
		"\u0000\u086f\u0870\u0001\u0000\u0000\u0000\u0870\u087a\u0001\u0000\u0000"+
		"\u0000\u0871\u0873\u0005o\u0000\u0000\u0872\u0874\u0005\u00ae\u0000\u0000"+
		"\u0873\u0872\u0001\u0000\u0000\u0000\u0873\u0874\u0001\u0000\u0000\u0000"+
		"\u0874\u087a\u0001\u0000\u0000\u0000\u0875\u0877\u0005\u008d\u0000\u0000"+
		"\u0876\u0875\u0001\u0000\u0000\u0000\u0876\u0877\u0001\u0000\u0000\u0000"+
		"\u0877\u0878\u0001\u0000\u0000\u0000\u0878\u087a\u0005\u001c\u0000\u0000"+
		"\u0879\u0862\u0001\u0000\u0000\u0000\u0879\u0864\u0001\u0000\u0000\u0000"+
		"\u0879\u0865\u0001\u0000\u0000\u0000\u0879\u086a\u0001\u0000\u0000\u0000"+
		"\u0879\u086d\u0001\u0000\u0000\u0000\u0879\u0871\u0001\u0000\u0000\u0000"+
		"\u0879\u0876\u0001\u0000\u0000\u0000\u087a\u0099\u0001\u0000\u0000\u0000"+
		"\u087b\u087c\u0005\u00a7\u0000\u0000\u087c\u0880\u0003\u00ceg\u0000\u087d"+
		"\u087e\u0005\u0014\u0000\u0000\u087e\u0880\u0003\u00a0P\u0000\u087f\u087b"+
		"\u0001\u0000\u0000\u0000\u087f\u087d\u0001\u0000\u0000\u0000\u0880\u009b"+
		"\u0001\u0000\u0000\u0000\u0881\u0882\u0005\u00f0\u0000\u0000\u0882\u0884"+
		"\u0005\u0002\u0000\u0000\u0883\u0885\u0003\u009eO\u0000\u0884\u0883\u0001"+
		"\u0000\u0000\u0000\u0884\u0885\u0001\u0000\u0000\u0000\u0885\u0886\u0001"+
		"\u0000\u0000\u0000\u0886\u0887\u0005\u0003\u0000\u0000\u0887\u009d\u0001"+
		"\u0000\u0000\u0000\u0888\u088a\u0005\u011d\u0000\u0000\u0889\u0888\u0001"+
		"\u0000\u0000\u0000\u0889\u088a\u0001\u0000\u0000\u0000\u088a\u088b\u0001"+
		"\u0000\u0000\u0000\u088b\u088c\u0007\u0015\u0000\u0000\u088c\u08a1\u0005"+
		"\u00b7\u0000\u0000\u088d\u088e\u0003\u00cae\u0000\u088e\u088f\u0005\u00d6"+
		"\u0000\u0000\u088f\u08a1\u0001\u0000\u0000\u0000\u0890\u0891\u0005&\u0000"+
		"\u0000\u0891\u0892\u0005\u012a\u0000\u0000\u0892\u0893\u0005\u00ad\u0000"+
		"\u0000\u0893\u0894\u0005\u00a6\u0000\u0000\u0894\u089d\u0005\u012a\u0000"+
		"\u0000\u0895\u089b\u0005\u00a7\u0000\u0000\u0896\u089c\u0003\u0112\u0089"+
		"\u0000\u0897\u0898\u0003\u010c\u0086\u0000\u0898\u0899\u0005\u0002\u0000"+
		"\u0000\u0899\u089a\u0005\u0003\u0000\u0000\u089a\u089c\u0001\u0000\u0000"+
		"\u0000\u089b\u0896\u0001\u0000\u0000\u0000\u089b\u0897\u0001\u0000\u0000"+
		"\u0000\u089c\u089e\u0001\u0000\u0000\u0000\u089d\u0895\u0001\u0000\u0000"+
		"\u0000\u089d\u089e\u0001\u0000\u0000\u0000\u089e\u08a1\u0001\u0000\u0000"+
		"\u0000\u089f\u08a1\u0003\u00cae\u0000\u08a0\u0889\u0001\u0000\u0000\u0000"+
		"\u08a0\u088d\u0001\u0000\u0000\u0000\u08a0\u0890\u0001\u0000\u0000\u0000"+
		"\u08a0\u089f\u0001\u0000\u0000\u0000\u08a1\u009f\u0001\u0000\u0000\u0000"+
		"\u08a2\u08a3\u0005\u0002\u0000\u0000\u08a3\u08a4\u0003\u00a2Q\u0000\u08a4"+
		"\u08a5\u0005\u0003\u0000\u0000\u08a5\u00a1\u0001\u0000\u0000\u0000\u08a6"+
		"\u08ab\u0003\u010e\u0087\u0000\u08a7\u08a8\u0005\u0004\u0000\u0000\u08a8"+
		"\u08aa\u0003\u010e\u0087\u0000\u08a9\u08a7\u0001\u0000\u0000\u0000\u08aa"+
		"\u08ad\u0001\u0000\u0000\u0000\u08ab\u08a9\u0001\u0000\u0000\u0000\u08ab"+
		"\u08ac\u0001\u0000\u0000\u0000\u08ac\u00a3\u0001\u0000\u0000\u0000\u08ad"+
		"\u08ab\u0001\u0000\u0000\u0000\u08ae\u08af\u0005\u0002\u0000\u0000\u08af"+
		"\u08b4\u0003\u00a6S\u0000\u08b0\u08b1\u0005\u0004\u0000\u0000\u08b1\u08b3"+
		"\u0003\u00a6S\u0000\u08b2\u08b0\u0001\u0000\u0000\u0000\u08b3\u08b6\u0001"+
		"\u0000\u0000\u0000\u08b4\u08b2\u0001\u0000\u0000\u0000\u08b4\u08b5\u0001"+
		"\u0000\u0000\u0000\u08b5\u08b7\u0001\u0000\u0000\u0000\u08b6\u08b4\u0001"+
		"\u0000\u0000\u0000\u08b7\u08b8\u0005\u0003\u0000\u0000\u08b8\u00a5\u0001"+
		"\u0000\u0000\u0000\u08b9\u08bb\u0003\u010e\u0087\u0000\u08ba\u08bc\u0007"+
		"\u0011\u0000\u0000\u08bb\u08ba\u0001\u0000\u0000\u0000\u08bb\u08bc\u0001"+
		"\u0000\u0000\u0000\u08bc\u00a7\u0001\u0000\u0000\u0000\u08bd\u08be\u0005"+
		"\u0002\u0000\u0000\u08be\u08c3\u0003\u00aaU\u0000\u08bf\u08c0\u0005\u0004"+
		"\u0000\u0000\u08c0\u08c2\u0003\u00aaU\u0000\u08c1\u08bf\u0001\u0000\u0000"+
		"\u0000\u08c2\u08c5\u0001\u0000\u0000\u0000\u08c3\u08c1\u0001\u0000\u0000"+
		"\u0000\u08c3\u08c4\u0001\u0000\u0000\u0000\u08c4\u08c6\u0001\u0000\u0000"+
		"\u0000\u08c5\u08c3\u0001\u0000\u0000\u0000\u08c6\u08c7\u0005\u0003\u0000"+
		"\u0000\u08c7\u00a9\u0001\u0000\u0000\u0000\u08c8\u08ca\u0003\u0112\u0089"+
		"\u0000\u08c9\u08cb\u0003$\u0012\u0000\u08ca\u08c9\u0001\u0000\u0000\u0000"+
		"\u08ca\u08cb\u0001\u0000\u0000\u0000\u08cb\u00ab\u0001\u0000\u0000\u0000"+
		"\u08cc\u08ce\u0003\u00b8\\\u0000\u08cd\u08cf\u0003\u009cN\u0000\u08ce"+
		"\u08cd\u0001\u0000\u0000\u0000\u08ce\u08cf\u0001\u0000\u0000\u0000\u08cf"+
		"\u08d0\u0001\u0000\u0000\u0000\u08d0\u08d1\u0003\u00b2Y\u0000\u08d1\u08e5"+
		"\u0001\u0000\u0000\u0000\u08d2\u08d3\u0005\u0002\u0000\u0000\u08d3\u08d4"+
		"\u0003&\u0013\u0000\u08d4\u08d6\u0005\u0003\u0000\u0000\u08d5\u08d7\u0003"+
		"\u009cN\u0000\u08d6\u08d5\u0001\u0000\u0000\u0000\u08d6\u08d7\u0001\u0000"+
		"\u0000\u0000\u08d7\u08d8\u0001\u0000\u0000\u0000\u08d8\u08d9\u0003\u00b2"+
		"Y\u0000\u08d9\u08e5\u0001\u0000\u0000\u0000\u08da\u08db\u0005\u0002\u0000"+
		"\u0000\u08db\u08dc\u0003\u0094J\u0000\u08dc\u08de\u0005\u0003\u0000\u0000"+
		"\u08dd\u08df\u0003\u009cN\u0000\u08de\u08dd\u0001\u0000\u0000\u0000\u08de"+
		"\u08df\u0001\u0000\u0000\u0000\u08df\u08e0\u0001\u0000\u0000\u0000\u08e0"+
		"\u08e1\u0003\u00b2Y\u0000\u08e1\u08e5\u0001\u0000\u0000\u0000\u08e2\u08e5"+
		"\u0003\u00aeW\u0000\u08e3\u08e5\u0003\u00b0X\u0000\u08e4\u08cc\u0001\u0000"+
		"\u0000\u0000\u08e4\u08d2\u0001\u0000\u0000\u0000\u08e4\u08da\u0001\u0000"+
		"\u0000\u0000\u08e4\u08e2\u0001\u0000\u0000\u0000\u08e4\u08e3\u0001\u0000"+
		"\u0000\u0000\u08e5\u00ad\u0001\u0000\u0000\u0000\u08e6\u08e7\u0005\u010b"+
		"\u0000\u0000\u08e7\u08ec\u0003\u00cae\u0000\u08e8\u08e9\u0005\u0004\u0000"+
		"\u0000\u08e9\u08eb\u0003\u00cae\u0000\u08ea\u08e8\u0001\u0000\u0000\u0000"+
		"\u08eb\u08ee\u0001\u0000\u0000\u0000\u08ec\u08ea\u0001\u0000\u0000\u0000"+
		"\u08ec\u08ed\u0001\u0000\u0000\u0000\u08ed\u08ef\u0001\u0000\u0000\u0000"+
		"\u08ee\u08ec\u0001\u0000\u0000\u0000\u08ef\u08f0\u0003\u00b2Y\u0000\u08f0"+
		"\u00af\u0001\u0000\u0000\u0000\u08f1\u08f2\u0003\u010a\u0085\u0000\u08f2"+
		"\u08fb\u0005\u0002\u0000\u0000\u08f3\u08f8\u0003\u00cae\u0000\u08f4\u08f5"+
		"\u0005\u0004\u0000\u0000\u08f5\u08f7\u0003\u00cae\u0000\u08f6\u08f4\u0001"+
		"\u0000\u0000\u0000\u08f7\u08fa\u0001\u0000\u0000\u0000\u08f8\u08f6\u0001"+
		"\u0000\u0000\u0000\u08f8\u08f9\u0001\u0000\u0000\u0000\u08f9\u08fc\u0001"+
		"\u0000\u0000\u0000\u08fa\u08f8\u0001\u0000\u0000\u0000\u08fb\u08f3\u0001"+
		"\u0000\u0000\u0000\u08fb\u08fc\u0001\u0000\u0000\u0000\u08fc\u08fd\u0001"+
		"\u0000\u0000\u0000\u08fd\u08fe\u0005\u0003\u0000\u0000\u08fe\u08ff\u0003"+
		"\u00b2Y\u0000\u08ff\u00b1\u0001\u0000\u0000\u0000\u0900\u0902\u0005 \u0000"+
		"\u0000\u0901\u0900\u0001\u0000\u0000\u0000\u0901\u0902\u0001\u0000\u0000"+
		"\u0000\u0902\u0903\u0001\u0000\u0000\u0000\u0903\u0905\u0003\u0114\u008a"+
		"\u0000\u0904\u0906\u0003\u00a0P\u0000\u0905\u0904\u0001\u0000\u0000\u0000"+
		"\u0905\u0906\u0001\u0000\u0000\u0000\u0906\u0908\u0001\u0000\u0000\u0000"+
		"\u0907\u0901\u0001\u0000\u0000\u0000\u0907\u0908\u0001\u0000\u0000\u0000"+
		"\u0908\u00b3\u0001\u0000\u0000\u0000\u0909\u090a\u0005\u00d5\u0000\u0000"+
		"\u090a\u090b\u0005l\u0000\u0000\u090b\u090c\u0005\u00dc\u0000\u0000\u090c"+
		"\u0910\u0005\u0126\u0000\u0000\u090d\u090e\u0005\u0111\u0000\u0000\u090e"+
		"\u090f\u0005\u00dd\u0000\u0000\u090f\u0911\u0003>\u001f\u0000\u0910\u090d"+
		"\u0001\u0000\u0000\u0000\u0910\u0911\u0001\u0000\u0000\u0000\u0911\u093b"+
		"\u0001\u0000\u0000\u0000\u0912\u0913\u0005\u00d5\u0000\u0000\u0913\u0914"+
		"\u0005l\u0000\u0000\u0914\u091e\u0005M\u0000\u0000\u0915\u0916\u0005e"+
		"\u0000\u0000\u0916\u0917\u0005\u00f3\u0000\u0000\u0917\u0918\u0005(\u0000"+
		"\u0000\u0918\u091c\u0005\u0126\u0000\u0000\u0919\u091a\u0005Z\u0000\u0000"+
		"\u091a\u091b\u0005(\u0000\u0000\u091b\u091d\u0005\u0126\u0000\u0000\u091c"+
		"\u0919\u0001\u0000\u0000\u0000\u091c\u091d\u0001\u0000\u0000\u0000\u091d"+
		"\u091f\u0001\u0000\u0000\u0000\u091e\u0915\u0001\u0000\u0000\u0000\u091e"+
		"\u091f\u0001\u0000\u0000\u0000\u091f\u0925\u0001\u0000\u0000\u0000\u0920"+
		"\u0921\u00054\u0000\u0000\u0921\u0922\u0005\u0086\u0000\u0000\u0922\u0923"+
		"\u0005\u00f3\u0000\u0000\u0923\u0924\u0005(\u0000\u0000\u0924\u0926\u0005"+
		"\u0126\u0000\u0000\u0925\u0920\u0001\u0000\u0000\u0000\u0925\u0926\u0001"+
		"\u0000\u0000\u0000\u0926\u092c\u0001\u0000\u0000\u0000\u0927\u0928\u0005"+
		"\u0099\u0000\u0000\u0928\u0929\u0005\u0088\u0000\u0000\u0929\u092a\u0005"+
		"\u00f3\u0000\u0000\u092a\u092b\u0005(\u0000\u0000\u092b\u092d\u0005\u0126"+
		"\u0000\u0000\u092c\u0927\u0001\u0000\u0000\u0000\u092c\u092d\u0001\u0000"+
		"\u0000\u0000\u092d\u0932\u0001\u0000\u0000\u0000\u092e\u092f\u0005\u0090"+
		"\u0000\u0000\u092f\u0930\u0005\u00f3\u0000\u0000\u0930\u0931\u0005(\u0000"+
		"\u0000\u0931\u0933\u0005\u0126\u0000\u0000\u0932\u092e\u0001\u0000\u0000"+
		"\u0000\u0932\u0933\u0001\u0000\u0000\u0000\u0933\u0938\u0001\u0000\u0000"+
		"\u0000\u0934\u0935\u0005\u00a4\u0000\u0000\u0935\u0936\u0005K\u0000\u0000"+
		"\u0936\u0937\u0005 \u0000\u0000\u0937\u0939\u0005\u0126\u0000\u0000\u0938"+
		"\u0934\u0001\u0000\u0000\u0000\u0938\u0939\u0001\u0000\u0000\u0000\u0939"+
		"\u093b\u0001\u0000\u0000\u0000\u093a\u0909\u0001\u0000\u0000\u0000\u093a"+
		"\u0912\u0001\u0000\u0000\u0000\u093b\u00b5\u0001\u0000\u0000\u0000\u093c"+
		"\u0941\u0003\u00b8\\\u0000\u093d\u093e\u0005\u0004\u0000\u0000\u093e\u0940"+
		"\u0003\u00b8\\\u0000\u093f\u093d\u0001\u0000\u0000\u0000\u0940\u0943\u0001"+
		"\u0000\u0000\u0000\u0941\u093f\u0001\u0000\u0000\u0000\u0941\u0942\u0001"+
		"\u0000\u0000\u0000\u0942\u00b7\u0001\u0000\u0000\u0000\u0943\u0941\u0001"+
		"\u0000\u0000\u0000\u0944\u0949\u0003\u010e\u0087\u0000\u0945\u0946\u0005"+
		"\u0005\u0000\u0000\u0946\u0948\u0003\u010e\u0087\u0000\u0947\u0945\u0001"+
		"\u0000\u0000\u0000\u0948\u094b\u0001\u0000\u0000\u0000\u0949\u0947\u0001"+
		"\u0000\u0000\u0000\u0949\u094a\u0001\u0000\u0000\u0000\u094a\u00b9\u0001"+
		"\u0000\u0000\u0000\u094b\u0949\u0001\u0000\u0000\u0000\u094c\u094d\u0003"+
		"\u010e\u0087\u0000\u094d\u094e\u0005\u0005\u0000\u0000\u094e\u0950\u0001"+
		"\u0000\u0000\u0000\u094f\u094c\u0001\u0000\u0000\u0000\u094f\u0950\u0001"+
		"\u0000\u0000\u0000\u0950\u0951\u0001\u0000\u0000\u0000\u0951\u0952\u0003"+
		"\u010e\u0087\u0000\u0952\u00bb\u0001\u0000\u0000\u0000\u0953\u0954\u0003"+
		"\u010e\u0087\u0000\u0954\u0955\u0005\u0005\u0000\u0000\u0955\u0957\u0001"+
		"\u0000\u0000\u0000\u0956\u0953\u0001\u0000\u0000\u0000\u0956\u0957\u0001"+
		"\u0000\u0000\u0000\u0957\u0958\u0001\u0000\u0000\u0000\u0958\u0959\u0003"+
		"\u010e\u0087\u0000\u0959\u00bd\u0001\u0000\u0000\u0000\u095a\u0962\u0003"+
		"\u00cae\u0000\u095b\u095d\u0005 \u0000\u0000\u095c\u095b\u0001\u0000\u0000"+
		"\u0000\u095c\u095d\u0001\u0000\u0000\u0000\u095d\u0960\u0001\u0000\u0000"+
		"\u0000\u095e\u0961\u0003\u010e\u0087\u0000\u095f\u0961\u0003\u00a0P\u0000"+
		"\u0960\u095e\u0001\u0000\u0000\u0000\u0960\u095f\u0001\u0000\u0000\u0000"+
		"\u0961\u0963\u0001\u0000\u0000\u0000\u0962\u095c\u0001\u0000\u0000\u0000"+
		"\u0962\u0963\u0001\u0000\u0000\u0000\u0963\u00bf\u0001\u0000\u0000\u0000"+
		"\u0964\u0969\u0003\u00be_\u0000\u0965\u0966\u0005\u0004\u0000\u0000\u0966"+
		"\u0968\u0003\u00be_\u0000\u0967\u0965\u0001\u0000\u0000\u0000\u0968\u096b"+
		"\u0001\u0000\u0000\u0000\u0969\u0967\u0001\u0000\u0000\u0000\u0969\u096a"+
		"\u0001\u0000\u0000\u0000\u096a\u00c1\u0001\u0000\u0000\u0000\u096b\u0969"+
		"\u0001\u0000\u0000\u0000\u096c\u096d\u0005\u0002\u0000\u0000\u096d\u0972"+
		"\u0003\u00c4b\u0000\u096e\u096f\u0005\u0004\u0000\u0000\u096f\u0971\u0003"+
		"\u00c4b\u0000\u0970\u096e\u0001\u0000\u0000\u0000\u0971\u0974\u0001\u0000"+
		"\u0000\u0000\u0972\u0970\u0001\u0000\u0000\u0000\u0972\u0973\u0001\u0000"+
		"\u0000\u0000\u0973\u0975\u0001\u0000\u0000\u0000\u0974\u0972\u0001\u0000"+
		"\u0000\u0000\u0975\u0976\u0005\u0003\u0000\u0000\u0976\u00c3\u0001\u0000"+
		"\u0000\u0000\u0977\u097a\u0003\u00c6c\u0000\u0978\u097a\u0003\u00f6{\u0000"+
		"\u0979\u0977\u0001\u0000\u0000\u0000\u0979\u0978\u0001\u0000\u0000\u0000"+
		"\u097a\u00c5\u0001\u0000\u0000\u0000\u097b\u0989\u0003\u010c\u0086\u0000"+
		"\u097c\u097d\u0003\u0112\u0089\u0000\u097d\u097e\u0005\u0002\u0000\u0000"+
		"\u097e\u0983\u0003\u00c8d\u0000\u097f\u0980\u0005\u0004\u0000\u0000\u0980"+
		"\u0982\u0003\u00c8d\u0000\u0981\u097f\u0001\u0000\u0000\u0000\u0982\u0985"+
		"\u0001\u0000\u0000\u0000\u0983\u0981\u0001\u0000\u0000\u0000\u0983\u0984"+
		"\u0001\u0000\u0000\u0000\u0984\u0986\u0001\u0000\u0000\u0000\u0985\u0983"+
		"\u0001\u0000\u0000\u0000\u0986\u0987\u0005\u0003\u0000\u0000\u0987\u0989"+
		"\u0001\u0000\u0000\u0000\u0988\u097b\u0001\u0000\u0000\u0000\u0988\u097c"+
		"\u0001\u0000\u0000\u0000\u0989\u00c7\u0001\u0000\u0000\u0000\u098a\u098d"+
		"\u0003\u010c\u0086\u0000\u098b\u098d\u0003\u00d6k\u0000\u098c\u098a\u0001"+
		"\u0000\u0000\u0000\u098c\u098b\u0001\u0000\u0000\u0000\u098d\u00c9\u0001"+
		"\u0000\u0000\u0000\u098e\u098f\u0003\u00ceg\u0000\u098f\u00cb\u0001\u0000"+
		"\u0000\u0000\u0990\u0995\u0003\u00cae\u0000\u0991\u0992\u0005\u0004\u0000"+
		"\u0000\u0992\u0994\u0003\u00cae\u0000\u0993\u0991\u0001\u0000\u0000\u0000"+
		"\u0994\u0997\u0001\u0000\u0000\u0000\u0995\u0993\u0001\u0000\u0000\u0000"+
		"\u0995\u0996\u0001\u0000\u0000\u0000\u0996\u00cd\u0001\u0000\u0000\u0000"+
		"\u0997\u0995\u0001\u0000\u0000\u0000\u0998\u0999\u0006g\uffff\uffff\u0000"+
		"\u0999\u099a\u0005\u00a3\u0000\u0000\u099a\u09a5\u0003\u00ceg\u0005\u099b"+
		"\u099c\u0005]\u0000\u0000\u099c\u099d\u0005\u0002\u0000\u0000\u099d\u099e"+
		"\u0003&\u0013\u0000\u099e\u099f\u0005\u0003\u0000\u0000\u099f\u09a5\u0001"+
		"\u0000\u0000\u0000\u09a0\u09a2\u0003\u00d2i\u0000\u09a1\u09a3\u0003\u00d0"+
		"h\u0000\u09a2\u09a1\u0001\u0000\u0000\u0000\u09a2\u09a3\u0001\u0000\u0000"+
		"\u0000\u09a3\u09a5\u0001\u0000\u0000\u0000\u09a4\u0998\u0001\u0000\u0000"+
		"\u0000\u09a4\u099b\u0001\u0000\u0000\u0000\u09a4\u09a0\u0001\u0000\u0000"+
		"\u0000\u09a5\u09ae\u0001\u0000\u0000\u0000\u09a6\u09a7\n\u0002\u0000\u0000"+
		"\u09a7\u09a8\u0005\u001b\u0000\u0000\u09a8\u09ad\u0003\u00ceg\u0003\u09a9"+
		"\u09aa\n\u0001\u0000\u0000\u09aa\u09ab\u0005\u00ab\u0000\u0000\u09ab\u09ad"+
		"\u0003\u00ceg\u0002\u09ac\u09a6\u0001\u0000\u0000\u0000\u09ac\u09a9\u0001"+
		"\u0000\u0000\u0000\u09ad\u09b0\u0001\u0000\u0000\u0000\u09ae\u09ac\u0001"+
		"\u0000\u0000\u0000\u09ae\u09af\u0001\u0000\u0000\u0000\u09af\u00cf\u0001"+
		"\u0000\u0000\u0000\u09b0\u09ae\u0001\u0000\u0000\u0000\u09b1\u09b3\u0005"+
		"\u00a3\u0000\u0000\u09b2\u09b1\u0001\u0000\u0000\u0000\u09b2\u09b3\u0001"+
		"\u0000\u0000\u0000\u09b3\u09b4\u0001\u0000\u0000\u0000\u09b4\u09b5\u0005"+
		"$\u0000\u0000\u09b5\u09b6\u0003\u00d2i\u0000\u09b6\u09b7\u0005\u001b\u0000"+
		"\u0000\u09b7\u09b8\u0003\u00d2i\u0000\u09b8\u0a04\u0001\u0000\u0000\u0000"+
		"\u09b9\u09bb\u0005\u00a3\u0000\u0000\u09ba\u09b9\u0001\u0000\u0000\u0000"+
		"\u09ba\u09bb\u0001\u0000\u0000\u0000\u09bb\u09bc\u0001\u0000\u0000\u0000"+
		"\u09bc\u09bd\u0005{\u0000\u0000\u09bd\u09be\u0005\u0002\u0000\u0000\u09be"+
		"\u09c3\u0003\u00cae\u0000\u09bf\u09c0\u0005\u0004\u0000\u0000\u09c0\u09c2"+
		"\u0003\u00cae\u0000\u09c1\u09bf\u0001\u0000\u0000\u0000\u09c2\u09c5\u0001"+
		"\u0000\u0000\u0000\u09c3\u09c1\u0001\u0000\u0000\u0000\u09c3\u09c4\u0001"+
		"\u0000\u0000\u0000\u09c4\u09c6\u0001\u0000\u0000\u0000\u09c5\u09c3\u0001"+
		"\u0000\u0000\u0000\u09c6\u09c7\u0005\u0003\u0000\u0000\u09c7\u0a04\u0001"+
		"\u0000\u0000\u0000\u09c8\u09ca\u0005\u00a3\u0000\u0000\u09c9\u09c8\u0001"+
		"\u0000\u0000\u0000\u09c9\u09ca\u0001\u0000\u0000\u0000\u09ca\u09cb\u0001"+
		"\u0000\u0000\u0000\u09cb\u09cc\u0005{\u0000\u0000\u09cc\u09cd\u0005\u0002"+
		"\u0000\u0000\u09cd\u09ce\u0003&\u0013\u0000\u09ce\u09cf\u0005\u0003\u0000"+
		"\u0000\u09cf\u0a04\u0001\u0000\u0000\u0000\u09d0\u09d2\u0005\u00a3\u0000"+
		"\u0000\u09d1\u09d0\u0001\u0000\u0000\u0000\u09d1\u09d2\u0001\u0000\u0000"+
		"\u0000\u09d2\u09d3\u0001\u0000\u0000\u0000\u09d3\u09d4\u0005\u00d0\u0000"+
		"\u0000\u09d4\u0a04\u0003\u00d2i\u0000\u09d5\u09d7\u0005\u00a3\u0000\u0000"+
		"\u09d6\u09d5\u0001\u0000\u0000\u0000\u09d6\u09d7\u0001\u0000\u0000\u0000"+
		"\u09d7\u09d8\u0001\u0000\u0000\u0000\u09d8\u09d9\u0005\u008e\u0000\u0000"+
		"\u09d9\u09e7\u0007\u0016\u0000\u0000\u09da\u09db\u0005\u0002\u0000\u0000"+
		"\u09db\u09e8\u0005\u0003\u0000\u0000\u09dc\u09dd\u0005\u0002\u0000\u0000"+
		"\u09dd\u09e2\u0003\u00cae\u0000\u09de\u09df\u0005\u0004\u0000\u0000\u09df"+
		"\u09e1\u0003\u00cae\u0000\u09e0\u09de\u0001\u0000\u0000\u0000\u09e1\u09e4"+
		"\u0001\u0000\u0000\u0000\u09e2\u09e0\u0001\u0000\u0000\u0000\u09e2\u09e3"+
		"\u0001\u0000\u0000\u0000\u09e3\u09e5\u0001\u0000\u0000\u0000\u09e4\u09e2"+
		"\u0001\u0000\u0000\u0000\u09e5\u09e6\u0005\u0003\u0000\u0000\u09e6\u09e8"+
		"\u0001\u0000\u0000\u0000\u09e7\u09da\u0001\u0000\u0000\u0000\u09e7\u09dc"+
		"\u0001\u0000\u0000\u0000\u09e8\u0a04\u0001\u0000\u0000\u0000\u09e9\u09eb"+
		"\u0005\u00a3\u0000\u0000\u09ea\u09e9\u0001\u0000\u0000\u0000\u09ea\u09eb"+
		"\u0001\u0000\u0000\u0000\u09eb\u09ec\u0001\u0000\u0000\u0000\u09ec\u09ed"+
		"\u0005\u008e\u0000\u0000\u09ed\u09f0\u0003\u00d2i\u0000\u09ee\u09ef\u0005"+
		"Y\u0000\u0000\u09ef\u09f1\u0005\u0126\u0000\u0000\u09f0\u09ee\u0001\u0000"+
		"\u0000\u0000\u09f0\u09f1\u0001\u0000\u0000\u0000\u09f1\u0a04\u0001\u0000"+
		"\u0000\u0000\u09f2\u09f4\u0005\u0085\u0000\u0000\u09f3\u09f5\u0005\u00a3"+
		"\u0000\u0000\u09f4\u09f3\u0001\u0000\u0000\u0000\u09f4\u09f5\u0001\u0000"+
		"\u0000\u0000\u09f5\u09f6\u0001\u0000\u0000\u0000\u09f6\u0a04\u0005\u00a4"+
		"\u0000\u0000\u09f7\u09f9\u0005\u0085\u0000\u0000\u09f8\u09fa\u0005\u00a3"+
		"\u0000\u0000\u09f9\u09f8\u0001\u0000\u0000\u0000\u09f9\u09fa\u0001\u0000"+
		"\u0000\u0000\u09fa\u09fb\u0001\u0000\u0000\u0000\u09fb\u0a04\u0007\u0017"+
		"\u0000\u0000\u09fc\u09fe\u0005\u0085\u0000\u0000\u09fd\u09ff\u0005\u00a3"+
		"\u0000\u0000\u09fe\u09fd\u0001\u0000\u0000\u0000\u09fe\u09ff\u0001\u0000"+
		"\u0000\u0000\u09ff\u0a00\u0001\u0000\u0000\u0000\u0a00\u0a01\u0005S\u0000"+
		"\u0000\u0a01\u0a02\u0005n\u0000\u0000\u0a02\u0a04\u0003\u00d2i\u0000\u0a03"+
		"\u09b2\u0001\u0000\u0000\u0000\u0a03\u09ba\u0001\u0000\u0000\u0000\u0a03"+
		"\u09c9\u0001\u0000\u0000\u0000\u0a03\u09d1\u0001\u0000\u0000\u0000\u0a03"+
		"\u09d6\u0001\u0000\u0000\u0000\u0a03\u09ea\u0001\u0000\u0000\u0000\u0a03"+
		"\u09f2\u0001\u0000\u0000\u0000\u0a03\u09f7\u0001\u0000\u0000\u0000\u0a03"+
		"\u09fc\u0001\u0000\u0000\u0000\u0a04\u00d1\u0001\u0000\u0000\u0000\u0a05"+
		"\u0a06\u0006i\uffff\uffff\u0000\u0a06\u0a0a\u0003\u00d4j\u0000\u0a07\u0a08"+
		"\u0007\u0018\u0000\u0000\u0a08\u0a0a\u0003\u00d2i\u0007\u0a09\u0a05\u0001"+
		"\u0000\u0000\u0000\u0a09\u0a07\u0001\u0000\u0000\u0000\u0a0a\u0a20\u0001"+
		"\u0000\u0000\u0000\u0a0b\u0a0c\n\u0006\u0000\u0000\u0a0c\u0a0d\u0007\u0019"+
		"\u0000\u0000\u0a0d\u0a1f\u0003\u00d2i\u0007\u0a0e\u0a0f\n\u0005\u0000"+
		"\u0000\u0a0f\u0a10\u0007\u001a\u0000\u0000\u0a10\u0a1f\u0003\u00d2i\u0006"+
		"\u0a11\u0a12\n\u0004\u0000\u0000\u0a12\u0a13\u0005\u0122\u0000\u0000\u0a13"+
		"\u0a1f\u0003\u00d2i\u0005\u0a14\u0a15\n\u0003\u0000\u0000\u0a15\u0a16"+
		"\u0005\u0125\u0000\u0000\u0a16\u0a1f\u0003\u00d2i\u0004\u0a17\u0a18\n"+
		"\u0002\u0000\u0000\u0a18\u0a19\u0005\u0123\u0000\u0000\u0a19\u0a1f\u0003"+
		"\u00d2i\u0003\u0a1a\u0a1b\n\u0001\u0000\u0000\u0a1b\u0a1c\u0003\u00d8"+
		"l\u0000\u0a1c\u0a1d\u0003\u00d2i\u0002\u0a1d\u0a1f\u0001\u0000\u0000\u0000"+
		"\u0a1e\u0a0b\u0001\u0000\u0000\u0000\u0a1e\u0a0e\u0001\u0000\u0000\u0000"+
		"\u0a1e\u0a11\u0001\u0000\u0000\u0000\u0a1e\u0a14\u0001\u0000\u0000\u0000"+
		"\u0a1e\u0a17\u0001\u0000\u0000\u0000\u0a1e\u0a1a\u0001\u0000\u0000\u0000"+
		"\u0a1f\u0a22\u0001\u0000\u0000\u0000\u0a20\u0a1e\u0001\u0000\u0000\u0000"+
		"\u0a20\u0a21\u0001\u0000\u0000\u0000\u0a21\u00d3\u0001\u0000\u0000\u0000"+
		"\u0a22\u0a20\u0001\u0000\u0000\u0000\u0a23\u0a24\u0006j\uffff\uffff\u0000"+
		"\u0a24\u0ae0\u0007\u001b\u0000\u0000\u0a25\u0a27\u0005+\u0000\u0000\u0a26"+
		"\u0a28\u0003\u00fc~\u0000\u0a27\u0a26\u0001\u0000\u0000\u0000\u0a28\u0a29"+
		"\u0001\u0000\u0000\u0000\u0a29\u0a27\u0001\u0000\u0000\u0000\u0a29\u0a2a"+
		"\u0001\u0000\u0000\u0000\u0a2a\u0a2d\u0001\u0000\u0000\u0000\u0a2b\u0a2c"+
		"\u0005W\u0000\u0000\u0a2c\u0a2e\u0003\u00cae\u0000\u0a2d\u0a2b\u0001\u0000"+
		"\u0000\u0000\u0a2d\u0a2e\u0001\u0000\u0000\u0000\u0a2e\u0a2f\u0001\u0000"+
		"\u0000\u0000\u0a2f\u0a30\u0005X\u0000\u0000\u0a30\u0ae0\u0001\u0000\u0000"+
		"\u0000\u0a31\u0a32\u0005+\u0000\u0000\u0a32\u0a34\u0003\u00cae\u0000\u0a33"+
		"\u0a35\u0003\u00fc~\u0000\u0a34\u0a33\u0001\u0000\u0000\u0000\u0a35\u0a36"+
		"\u0001\u0000\u0000\u0000\u0a36\u0a34\u0001\u0000\u0000\u0000\u0a36\u0a37"+
		"\u0001\u0000\u0000\u0000\u0a37\u0a3a\u0001\u0000\u0000\u0000\u0a38\u0a39"+
		"\u0005W\u0000\u0000\u0a39\u0a3b\u0003\u00cae\u0000\u0a3a\u0a38\u0001\u0000"+
		"\u0000\u0000\u0a3a\u0a3b\u0001\u0000\u0000\u0000\u0a3b\u0a3c\u0001\u0000"+
		"\u0000\u0000\u0a3c\u0a3d\u0005X\u0000\u0000\u0a3d\u0ae0\u0001\u0000\u0000"+
		"\u0000\u0a3e\u0a3f\u0007\u001c\u0000\u0000\u0a3f\u0a40\u0005\u0002\u0000"+
		"\u0000\u0a40\u0a41\u0003\u00cae\u0000\u0a41\u0a42\u0005 \u0000\u0000\u0a42"+
		"\u0a43\u0003\u00eew\u0000\u0a43\u0a44\u0005\u0003\u0000\u0000\u0a44\u0ae0"+
		"\u0001\u0000\u0000\u0000\u0a45\u0a46\u0005\u00eb\u0000\u0000\u0a46\u0a4f"+
		"\u0005\u0002\u0000\u0000\u0a47\u0a4c\u0003\u00be_\u0000\u0a48\u0a49\u0005"+
		"\u0004\u0000\u0000\u0a49\u0a4b\u0003\u00be_\u0000\u0a4a\u0a48\u0001\u0000"+
		"\u0000\u0000\u0a4b\u0a4e\u0001\u0000\u0000\u0000\u0a4c\u0a4a\u0001\u0000"+
		"\u0000\u0000\u0a4c\u0a4d\u0001\u0000\u0000\u0000\u0a4d\u0a50\u0001\u0000"+
		"\u0000\u0000\u0a4e\u0a4c\u0001\u0000\u0000\u0000\u0a4f\u0a47\u0001\u0000"+
		"\u0000\u0000\u0a4f\u0a50\u0001\u0000\u0000\u0000\u0a50\u0a51\u0001\u0000"+
		"\u0000\u0000\u0a51\u0ae0\u0005\u0003\u0000\u0000\u0a52\u0a53\u0005h\u0000"+
		"\u0000\u0a53\u0a54\u0005\u0002\u0000\u0000\u0a54\u0a57\u0003\u00cae\u0000"+
		"\u0a55\u0a56\u0005y\u0000\u0000\u0a56\u0a58\u0005\u00a5\u0000\u0000\u0a57"+
		"\u0a55\u0001\u0000\u0000\u0000\u0a57\u0a58\u0001\u0000\u0000\u0000\u0a58"+
		"\u0a59\u0001\u0000\u0000\u0000\u0a59\u0a5a\u0005\u0003\u0000\u0000\u0a5a"+
		"\u0ae0\u0001\u0000\u0000\u0000\u0a5b\u0a5c\u0005\u0089\u0000\u0000\u0a5c"+
		"\u0a5d\u0005\u0002\u0000\u0000\u0a5d\u0a60\u0003\u00cae\u0000\u0a5e\u0a5f"+
		"\u0005y\u0000\u0000\u0a5f\u0a61\u0005\u00a5\u0000\u0000\u0a60\u0a5e\u0001"+
		"\u0000\u0000\u0000\u0a60\u0a61\u0001\u0000\u0000\u0000\u0a61\u0a62\u0001"+
		"\u0000\u0000\u0000\u0a62\u0a63\u0005\u0003\u0000\u0000\u0a63\u0ae0\u0001"+
		"\u0000\u0000\u0000\u0a64\u0a65\u0005\u00ba\u0000\u0000\u0a65\u0a66\u0005"+
		"\u0002\u0000\u0000\u0a66\u0a67\u0003\u00d2i\u0000\u0a67\u0a68\u0005{\u0000"+
		"\u0000\u0a68\u0a69\u0003\u00d2i\u0000\u0a69\u0a6a\u0005\u0003\u0000\u0000"+
		"\u0a6a\u0ae0\u0001\u0000\u0000\u0000\u0a6b\u0ae0\u0003\u00d6k\u0000\u0a6c"+
		"\u0ae0\u0005\u011e\u0000\u0000\u0a6d\u0a6e\u0003\u010c\u0086\u0000\u0a6e"+
		"\u0a6f\u0005\u0005\u0000\u0000\u0a6f\u0a70\u0005\u011e\u0000\u0000\u0a70"+
		"\u0ae0\u0001\u0000\u0000\u0000\u0a71\u0a72\u0005\u0002\u0000\u0000\u0a72"+
		"\u0a75\u0003\u00be_\u0000\u0a73\u0a74\u0005\u0004\u0000\u0000\u0a74\u0a76"+
		"\u0003\u00be_\u0000\u0a75\u0a73\u0001\u0000\u0000\u0000\u0a76\u0a77\u0001"+
		"\u0000\u0000\u0000\u0a77\u0a75\u0001\u0000\u0000\u0000\u0a77\u0a78\u0001"+
		"\u0000\u0000\u0000\u0a78\u0a79\u0001\u0000\u0000\u0000\u0a79\u0a7a\u0005"+
		"\u0003\u0000\u0000\u0a7a\u0ae0\u0001\u0000\u0000\u0000\u0a7b\u0a7c\u0005"+
		"\u0002\u0000\u0000\u0a7c\u0a7d\u0003&\u0013\u0000\u0a7d\u0a7e\u0005\u0003"+
		"\u0000\u0000\u0a7e\u0ae0\u0001\u0000\u0000\u0000\u0a7f\u0a80\u0003\u010a"+
		"\u0085\u0000\u0a80\u0a8c\u0005\u0002\u0000\u0000\u0a81\u0a83\u0003\u0092"+
		"I\u0000\u0a82\u0a81\u0001\u0000\u0000\u0000\u0a82\u0a83\u0001\u0000\u0000"+
		"\u0000\u0a83\u0a84\u0001\u0000\u0000\u0000\u0a84\u0a89\u0003\u00cae\u0000"+
		"\u0a85\u0a86\u0005\u0004\u0000\u0000\u0a86\u0a88\u0003\u00cae\u0000\u0a87"+
		"\u0a85\u0001\u0000\u0000\u0000\u0a88\u0a8b\u0001\u0000\u0000\u0000\u0a89"+
		"\u0a87\u0001\u0000\u0000\u0000\u0a89\u0a8a\u0001\u0000\u0000\u0000\u0a8a"+
		"\u0a8d\u0001\u0000\u0000\u0000\u0a8b\u0a89\u0001\u0000\u0000\u0000\u0a8c"+
		"\u0a82\u0001\u0000\u0000\u0000\u0a8c\u0a8d\u0001\u0000\u0000\u0000\u0a8d"+
		"\u0a8e\u0001\u0000\u0000\u0000\u0a8e\u0a95\u0005\u0003\u0000\u0000\u0a8f"+
		"\u0a90\u0005f\u0000\u0000\u0a90\u0a91\u0005\u0002\u0000\u0000\u0a91\u0a92"+
		"\u0005\u010f\u0000\u0000\u0a92\u0a93\u0003\u00ceg\u0000\u0a93\u0a94\u0005"+
		"\u0003\u0000\u0000\u0a94\u0a96\u0001\u0000\u0000\u0000\u0a95\u0a8f\u0001"+
		"\u0000\u0000\u0000\u0a95\u0a96\u0001\u0000\u0000\u0000\u0a96\u0a99\u0001"+
		"\u0000\u0000\u0000\u0a97\u0a98\u0007\u001d\u0000\u0000\u0a98\u0a9a\u0005"+
		"\u00a5\u0000\u0000\u0a99\u0a97\u0001\u0000\u0000\u0000\u0a99\u0a9a\u0001"+
		"\u0000\u0000\u0000\u0a9a\u0a9d\u0001\u0000\u0000\u0000\u0a9b\u0a9c\u0005"+
		"\u00b0\u0000\u0000\u0a9c\u0a9e\u0003\u0102\u0081\u0000\u0a9d\u0a9b\u0001"+
		"\u0000\u0000\u0000\u0a9d\u0a9e\u0001\u0000\u0000\u0000\u0a9e\u0ae0\u0001"+
		"\u0000\u0000\u0000\u0a9f\u0aa0\u0003\u0112\u0089\u0000\u0aa0\u0aa1\u0005"+
		"\b\u0000\u0000\u0aa1\u0aa2\u0003\u00cae\u0000\u0aa2\u0ae0\u0001\u0000"+
		"\u0000\u0000\u0aa3\u0aa4\u0005\u0002\u0000\u0000\u0aa4\u0aa7\u0003\u0112"+
		"\u0089\u0000\u0aa5\u0aa6\u0005\u0004\u0000\u0000\u0aa6\u0aa8\u0003\u0112"+
		"\u0089\u0000\u0aa7\u0aa5\u0001\u0000\u0000\u0000\u0aa8\u0aa9\u0001\u0000"+
		"\u0000\u0000\u0aa9\u0aa7\u0001\u0000\u0000\u0000\u0aa9\u0aaa\u0001\u0000"+
		"\u0000\u0000\u0aaa\u0aab\u0001\u0000\u0000\u0000\u0aab\u0aac\u0005\u0003"+
		"\u0000\u0000\u0aac\u0aad\u0005\b\u0000\u0000\u0aad\u0aae\u0003\u00cae"+
		"\u0000\u0aae\u0ae0\u0001\u0000\u0000\u0000\u0aaf\u0ae0\u0003\u0112\u0089"+
		"\u0000\u0ab0\u0ab1\u0005\u0002\u0000\u0000\u0ab1\u0ab2\u0003\u00cae\u0000"+
		"\u0ab2\u0ab3\u0005\u0003\u0000\u0000\u0ab3\u0ae0\u0001\u0000\u0000\u0000"+
		"\u0ab4\u0ab5\u0005b\u0000\u0000\u0ab5\u0ab6\u0005\u0002\u0000\u0000\u0ab6"+
		"\u0ab7\u0003\u0112\u0089\u0000\u0ab7\u0ab8\u0005n\u0000\u0000\u0ab8\u0ab9"+
		"\u0003\u00d2i\u0000\u0ab9\u0aba\u0005\u0003\u0000\u0000\u0aba\u0ae0\u0001"+
		"\u0000\u0000\u0000\u0abb\u0abc\u0007\u001e\u0000\u0000\u0abc\u0abd\u0005"+
		"\u0002\u0000\u0000\u0abd\u0abe\u0003\u00d2i\u0000\u0abe\u0abf\u0007\u001f"+
		"\u0000\u0000\u0abf\u0ac2\u0003\u00d2i\u0000\u0ac0\u0ac1\u0007 \u0000\u0000"+
		"\u0ac1\u0ac3\u0003\u00d2i\u0000\u0ac2\u0ac0\u0001\u0000\u0000\u0000\u0ac2"+
		"\u0ac3\u0001\u0000\u0000\u0000\u0ac3\u0ac4\u0001\u0000\u0000\u0000\u0ac4"+
		"\u0ac5\u0005\u0003\u0000\u0000\u0ac5\u0ae0\u0001\u0000\u0000\u0000\u0ac6"+
		"\u0ac7\u0005\u00fb\u0000\u0000\u0ac7\u0ac9\u0005\u0002\u0000\u0000\u0ac8"+
		"\u0aca\u0007!\u0000\u0000\u0ac9\u0ac8\u0001\u0000\u0000\u0000\u0ac9\u0aca"+
		"\u0001\u0000\u0000\u0000\u0aca\u0acc\u0001\u0000\u0000\u0000\u0acb\u0acd"+
		"\u0003\u00d2i\u0000\u0acc\u0acb\u0001\u0000\u0000\u0000\u0acc\u0acd\u0001"+
		"\u0000\u0000\u0000\u0acd\u0ace\u0001\u0000\u0000\u0000\u0ace\u0acf\u0005"+
		"n\u0000\u0000\u0acf\u0ad0\u0003\u00d2i\u0000\u0ad0\u0ad1\u0005\u0003\u0000"+
		"\u0000\u0ad1\u0ae0\u0001\u0000\u0000\u0000\u0ad2\u0ad3\u0005\u00b2\u0000"+
		"\u0000\u0ad3\u0ad4\u0005\u0002\u0000\u0000\u0ad4\u0ad5\u0003\u00d2i\u0000"+
		"\u0ad5\u0ad6\u0005\u00b9\u0000\u0000\u0ad6\u0ad7\u0003\u00d2i\u0000\u0ad7"+
		"\u0ad8\u0005n\u0000\u0000\u0ad8\u0adb\u0003\u00d2i\u0000\u0ad9\u0ada\u0005"+
		"j\u0000\u0000\u0ada\u0adc\u0003\u00d2i\u0000\u0adb\u0ad9\u0001\u0000\u0000"+
		"\u0000\u0adb\u0adc\u0001\u0000\u0000\u0000\u0adc\u0add\u0001\u0000\u0000"+
		"\u0000\u0add\u0ade\u0005\u0003\u0000\u0000\u0ade\u0ae0\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a23\u0001\u0000\u0000\u0000\u0adf\u0a25\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a31\u0001\u0000\u0000\u0000\u0adf\u0a3e\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a45\u0001\u0000\u0000\u0000\u0adf\u0a52\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a5b\u0001\u0000\u0000\u0000\u0adf\u0a64\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a6b\u0001\u0000\u0000\u0000\u0adf\u0a6c\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a6d\u0001\u0000\u0000\u0000\u0adf\u0a71\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a7b\u0001\u0000\u0000\u0000\u0adf\u0a7f\u0001\u0000\u0000"+
		"\u0000\u0adf\u0a9f\u0001\u0000\u0000\u0000\u0adf\u0aa3\u0001\u0000\u0000"+
		"\u0000\u0adf\u0aaf\u0001\u0000\u0000\u0000\u0adf\u0ab0\u0001\u0000\u0000"+
		"\u0000\u0adf\u0ab4\u0001\u0000\u0000\u0000\u0adf\u0abb\u0001\u0000\u0000"+
		"\u0000\u0adf\u0ac6\u0001\u0000\u0000\u0000\u0adf\u0ad2\u0001\u0000\u0000"+
		"\u0000\u0ae0\u0aeb\u0001\u0000\u0000\u0000\u0ae1\u0ae2\n\b\u0000\u0000"+
		"\u0ae2\u0ae3\u0005\t\u0000\u0000\u0ae3\u0ae4\u0003\u00d2i\u0000\u0ae4"+
		"\u0ae5\u0005\n\u0000\u0000\u0ae5\u0aea\u0001\u0000\u0000\u0000\u0ae6\u0ae7"+
		"\n\u0006\u0000\u0000\u0ae7\u0ae8\u0005\u0005\u0000\u0000\u0ae8\u0aea\u0003"+
		"\u0112\u0089\u0000\u0ae9\u0ae1\u0001\u0000\u0000\u0000\u0ae9\u0ae6\u0001"+
		"\u0000\u0000\u0000\u0aea\u0aed\u0001\u0000\u0000\u0000\u0aeb\u0ae9\u0001"+
		"\u0000\u0000\u0000\u0aeb\u0aec\u0001\u0000\u0000\u0000\u0aec\u00d5\u0001"+
		"\u0000\u0000\u0000\u0aed\u0aeb\u0001\u0000\u0000\u0000\u0aee\u0afb\u0005"+
		"\u00a4\u0000\u0000\u0aef\u0afb\u0003\u00e0p\u0000\u0af0\u0af1\u0003\u0112"+
		"\u0089\u0000\u0af1\u0af2\u0005\u0126\u0000\u0000\u0af2\u0afb\u0001\u0000"+
		"\u0000\u0000\u0af3\u0afb\u0003\u0118\u008c\u0000\u0af4\u0afb\u0003\u00de"+
		"o\u0000\u0af5\u0af7\u0005\u0126\u0000\u0000\u0af6\u0af5\u0001\u0000\u0000"+
		"\u0000\u0af7\u0af8\u0001\u0000\u0000\u0000\u0af8\u0af6\u0001\u0000\u0000"+
		"\u0000\u0af8\u0af9\u0001\u0000\u0000\u0000\u0af9\u0afb\u0001\u0000\u0000"+
		"\u0000\u0afa\u0aee\u0001\u0000\u0000\u0000\u0afa\u0aef\u0001\u0000\u0000"+
		"\u0000\u0afa\u0af0\u0001\u0000\u0000\u0000\u0afa\u0af3\u0001\u0000\u0000"+
		"\u0000\u0afa\u0af4\u0001\u0000\u0000\u0000\u0afa\u0af6\u0001\u0000\u0000"+
		"\u0000\u0afb\u00d7\u0001\u0000\u0000\u0000\u0afc\u0afd\u0007\"\u0000\u0000"+
		"\u0afd\u00d9\u0001\u0000\u0000\u0000\u0afe\u0aff\u0007#\u0000\u0000\u0aff"+
		"\u00db\u0001\u0000\u0000\u0000\u0b00\u0b01\u0007$\u0000\u0000\u0b01\u00dd"+
		"\u0001\u0000\u0000\u0000\u0b02\u0b03\u0007%\u0000\u0000\u0b03\u00df\u0001"+
		"\u0000\u0000\u0000\u0b04\u0b07\u0005\u0083\u0000\u0000\u0b05\u0b08\u0003"+
		"\u00e2q\u0000\u0b06\u0b08\u0003\u00e6s\u0000\u0b07\u0b05\u0001\u0000\u0000"+
		"\u0000\u0b07\u0b06\u0001\u0000\u0000\u0000\u0b07\u0b08\u0001\u0000\u0000"+
		"\u0000\u0b08\u00e1\u0001\u0000\u0000\u0000\u0b09\u0b0b\u0003\u00e4r\u0000"+
		"\u0b0a\u0b0c\u0003\u00e8t\u0000\u0b0b\u0b0a\u0001\u0000\u0000\u0000\u0b0b"+
		"\u0b0c\u0001\u0000\u0000\u0000\u0b0c\u00e3\u0001\u0000\u0000\u0000\u0b0d"+
		"\u0b0e\u0003\u00eau\u0000\u0b0e\u0b0f\u0003\u0112\u0089\u0000\u0b0f\u0b11"+
		"\u0001\u0000\u0000\u0000\u0b10\u0b0d\u0001\u0000\u0000\u0000\u0b11\u0b12"+
		"\u0001\u0000\u0000\u0000\u0b12\u0b10\u0001\u0000\u0000\u0000\u0b12\u0b13"+
		"\u0001\u0000\u0000\u0000\u0b13\u00e5\u0001\u0000\u0000\u0000\u0b14\u0b17"+
		"\u0003\u00e8t\u0000\u0b15\u0b18\u0003\u00e4r\u0000\u0b16\u0b18\u0003\u00e8"+
		"t\u0000\u0b17\u0b15\u0001\u0000\u0000\u0000\u0b17\u0b16\u0001\u0000\u0000"+
		"\u0000\u0b17\u0b18\u0001\u0000\u0000\u0000\u0b18\u00e7\u0001\u0000\u0000"+
		"\u0000\u0b19\u0b1a\u0003\u00eau\u0000\u0b1a\u0b1b\u0003\u0112\u0089\u0000"+
		"\u0b1b\u0b1c\u0005\u0011\u0000\u0000\u0b1c\u0b1d\u0003\u0112\u0089\u0000"+
		"\u0b1d\u00e9\u0001\u0000\u0000\u0000\u0b1e\u0b20\u0007&\u0000\u0000\u0b1f"+
		"\u0b1e\u0001\u0000\u0000\u0000\u0b1f\u0b20\u0001\u0000\u0000\u0000\u0b20"+
		"\u0b21\u0001\u0000\u0000\u0000\u0b21\u0b22\u0007\'\u0000\u0000\u0b22\u00eb"+
		"\u0001\u0000\u0000\u0000\u0b23\u0b27\u0005h\u0000\u0000\u0b24\u0b25\u0005"+
		"\u0017\u0000\u0000\u0b25\u0b27\u0003\u010e\u0087\u0000\u0b26\u0b23\u0001"+
		"\u0000\u0000\u0000\u0b26\u0b24\u0001\u0000\u0000\u0000\u0b27\u00ed\u0001"+
		"\u0000\u0000\u0000\u0b28\u0b29\u0005\u001f\u0000\u0000\u0b29\u0b2a\u0005"+
		"\u0118\u0000\u0000\u0b2a\u0b2b\u0003\u00eew\u0000\u0b2b\u0b2c\u0005\u011a"+
		"\u0000\u0000\u0b2c\u0b57\u0001\u0000\u0000\u0000\u0b2d\u0b2e\u0005\u0099"+
		"\u0000\u0000\u0b2e\u0b2f\u0005\u0118\u0000\u0000\u0b2f\u0b30\u0003\u00ee"+
		"w\u0000\u0b30\u0b31\u0005\u0004\u0000\u0000\u0b31\u0b32\u0003\u00eew\u0000"+
		"\u0b32\u0b33\u0005\u011a\u0000\u0000\u0b33\u0b57\u0001\u0000\u0000\u0000"+
		"\u0b34\u0b3b\u0005\u00eb\u0000\u0000\u0b35\u0b37\u0005\u0118\u0000\u0000"+
		"\u0b36\u0b38\u0003\u00f8|\u0000\u0b37\u0b36\u0001\u0000\u0000\u0000\u0b37"+
		"\u0b38\u0001\u0000\u0000\u0000\u0b38\u0b39\u0001\u0000\u0000\u0000\u0b39"+
		"\u0b3c\u0005\u011a\u0000\u0000\u0b3a\u0b3c\u0005\u0116\u0000\u0000\u0b3b"+
		"\u0b35\u0001\u0000\u0000\u0000\u0b3b\u0b3a\u0001\u0000\u0000\u0000\u0b3c"+
		"\u0b57\u0001\u0000\u0000\u0000\u0b3d\u0b3e\u0005\u0083\u0000\u0000\u0b3e"+
		"\u0b41\u0007(\u0000\u0000\u0b3f\u0b40\u0005\u0011\u0000\u0000\u0b40\u0b42"+
		"\u0005\u009d\u0000\u0000\u0b41\u0b3f\u0001\u0000\u0000\u0000\u0b41\u0b42"+
		"\u0001\u0000\u0000\u0000\u0b42\u0b57\u0001\u0000\u0000\u0000\u0b43\u0b44"+
		"\u0005\u0083\u0000\u0000\u0b44\u0b47\u0007)\u0000\u0000\u0b45\u0b46\u0005"+
		"\u0011\u0000\u0000\u0b46\u0b48\u0007*\u0000\u0000\u0b47\u0b45\u0001\u0000"+
		"\u0000\u0000\u0b47\u0b48\u0001\u0000\u0000\u0000\u0b48\u0b57\u0001\u0000"+
		"\u0000\u0000\u0b49\u0b54\u0003\u0112\u0089\u0000\u0b4a\u0b4b\u0005\u0002"+
		"\u0000\u0000\u0b4b\u0b50\u0005\u012a\u0000\u0000\u0b4c\u0b4d\u0005\u0004"+
		"\u0000\u0000\u0b4d\u0b4f\u0005\u012a\u0000\u0000\u0b4e\u0b4c\u0001\u0000"+
		"\u0000\u0000\u0b4f\u0b52\u0001\u0000\u0000\u0000\u0b50\u0b4e\u0001\u0000"+
		"\u0000\u0000\u0b50\u0b51\u0001\u0000\u0000\u0000\u0b51\u0b53\u0001\u0000"+
		"\u0000\u0000\u0b52\u0b50\u0001\u0000\u0000\u0000\u0b53\u0b55\u0005\u0003"+
		"\u0000\u0000\u0b54\u0b4a\u0001\u0000\u0000\u0000\u0b54\u0b55\u0001\u0000"+
		"\u0000\u0000\u0b55\u0b57\u0001\u0000\u0000\u0000\u0b56\u0b28\u0001\u0000"+
		"\u0000\u0000\u0b56\u0b2d\u0001\u0000\u0000\u0000\u0b56\u0b34\u0001\u0000"+
		"\u0000\u0000\u0b56\u0b3d\u0001\u0000\u0000\u0000\u0b56\u0b43\u0001\u0000"+
		"\u0000\u0000\u0b56\u0b49\u0001\u0000\u0000\u0000\u0b57\u00ef\u0001\u0000"+
		"\u0000\u0000\u0b58\u0b5d\u0003\u00f2y\u0000\u0b59\u0b5a\u0005\u0004\u0000"+
		"\u0000\u0b5a\u0b5c\u0003\u00f2y\u0000\u0b5b\u0b59\u0001\u0000\u0000\u0000"+
		"\u0b5c\u0b5f\u0001\u0000\u0000\u0000\u0b5d\u0b5b\u0001\u0000\u0000\u0000"+
		"\u0b5d\u0b5e\u0001\u0000\u0000\u0000\u0b5e\u00f1\u0001\u0000\u0000\u0000"+
		"\u0b5f\u0b5d\u0001\u0000\u0000\u0000\u0b60\u0b61\u0003\u00b8\\\u0000\u0b61"+
		"\u0b64\u0003\u00eew\u0000\u0b62\u0b63\u0005\u00a3\u0000\u0000\u0b63\u0b65"+
		"\u0005\u00a4\u0000\u0000\u0b64\u0b62\u0001\u0000\u0000\u0000\u0b64\u0b65"+
		"\u0001\u0000\u0000\u0000\u0b65\u0b67\u0001\u0000\u0000\u0000\u0b66\u0b68"+
		"\u0003$\u0012\u0000\u0b67\u0b66\u0001\u0000\u0000\u0000\u0b67\u0b68\u0001"+
		"\u0000\u0000\u0000\u0b68\u0b6a\u0001\u0000\u0000\u0000\u0b69\u0b6b\u0003"+
		"\u00ecv\u0000\u0b6a\u0b69\u0001\u0000\u0000\u0000\u0b6a\u0b6b\u0001\u0000"+
		"\u0000\u0000\u0b6b\u00f3\u0001\u0000\u0000\u0000\u0b6c\u0b71\u0003\u00f6"+
		"{\u0000\u0b6d\u0b6e\u0005\u0004\u0000\u0000\u0b6e\u0b70\u0003\u00f6{\u0000"+
		"\u0b6f\u0b6d\u0001\u0000\u0000\u0000\u0b70\u0b73\u0001\u0000\u0000\u0000"+
		"\u0b71\u0b6f\u0001\u0000\u0000\u0000\u0b71\u0b72\u0001\u0000\u0000\u0000"+
		"\u0b72\u00f5\u0001\u0000\u0000\u0000\u0b73\u0b71\u0001\u0000\u0000\u0000"+
		"\u0b74\u0b75\u0003\u010e\u0087\u0000\u0b75\u0b78\u0003\u00eew\u0000\u0b76"+
		"\u0b77\u0005\u00a3\u0000\u0000\u0b77\u0b79\u0005\u00a4\u0000\u0000\u0b78"+
		"\u0b76\u0001\u0000\u0000\u0000\u0b78\u0b79\u0001\u0000\u0000\u0000\u0b79"+
		"\u0b7b\u0001\u0000\u0000\u0000\u0b7a\u0b7c\u0003$\u0012\u0000\u0b7b\u0b7a"+
		"\u0001\u0000\u0000\u0000\u0b7b\u0b7c\u0001\u0000\u0000\u0000\u0b7c\u00f7"+
		"\u0001\u0000\u0000\u0000\u0b7d\u0b82\u0003\u00fa}\u0000\u0b7e\u0b7f\u0005"+
		"\u0004\u0000\u0000\u0b7f\u0b81\u0003\u00fa}\u0000\u0b80\u0b7e\u0001\u0000"+
		"\u0000\u0000\u0b81\u0b84\u0001\u0000\u0000\u0000\u0b82\u0b80\u0001\u0000"+
		"\u0000\u0000\u0b82\u0b83\u0001\u0000\u0000\u0000\u0b83\u00f9\u0001\u0000"+
		"\u0000\u0000\u0b84\u0b82\u0001\u0000\u0000\u0000\u0b85\u0b87\u0003\u0112"+
		"\u0089\u0000\u0b86\u0b88\u0005\u000b\u0000\u0000\u0b87\u0b86\u0001\u0000"+
		"\u0000\u0000\u0b87\u0b88\u0001\u0000\u0000\u0000\u0b88\u0b89\u0001\u0000"+
		"\u0000\u0000\u0b89\u0b8c\u0003\u00eew\u0000\u0b8a\u0b8b\u0005\u00a3\u0000"+
		"\u0000\u0b8b\u0b8d\u0005\u00a4\u0000\u0000\u0b8c\u0b8a\u0001\u0000\u0000"+
		"\u0000\u0b8c\u0b8d\u0001\u0000\u0000\u0000\u0b8d\u0b8f\u0001\u0000\u0000"+
		"\u0000\u0b8e\u0b90\u0003$\u0012\u0000\u0b8f\u0b8e\u0001\u0000\u0000\u0000"+
		"\u0b8f\u0b90\u0001\u0000\u0000\u0000\u0b90\u00fb\u0001\u0000\u0000\u0000"+
		"\u0b91\u0b92\u0005\u010e\u0000\u0000\u0b92\u0b93\u0003\u00cae\u0000\u0b93"+
		"\u0b94\u0005\u00f4\u0000\u0000\u0b94\u0b95\u0003\u00cae\u0000\u0b95\u00fd"+
		"\u0001\u0000\u0000\u0000\u0b96\u0b97\u0005\u0110\u0000\u0000\u0b97\u0b9c"+
		"\u0003\u0100\u0080\u0000\u0b98\u0b99\u0005\u0004\u0000\u0000\u0b99\u0b9b"+
		"\u0003\u0100\u0080\u0000\u0b9a\u0b98\u0001\u0000\u0000\u0000\u0b9b\u0b9e"+
		"\u0001\u0000\u0000\u0000\u0b9c\u0b9a\u0001\u0000\u0000\u0000\u0b9c\u0b9d"+
		"\u0001\u0000\u0000\u0000\u0b9d\u00ff\u0001\u0000\u0000\u0000\u0b9e\u0b9c"+
		"\u0001\u0000\u0000\u0000\u0b9f\u0ba0\u0003\u010e\u0087\u0000\u0ba0\u0ba1"+
		"\u0005 \u0000\u0000\u0ba1\u0ba2\u0003\u0102\u0081\u0000\u0ba2\u0101\u0001"+
		"\u0000\u0000\u0000\u0ba3\u0bd2\u0003\u010e\u0087\u0000\u0ba4\u0ba5\u0005"+
		"\u0002\u0000\u0000\u0ba5\u0ba6\u0003\u010e\u0087\u0000\u0ba6\u0ba7\u0005"+
		"\u0003\u0000\u0000\u0ba7\u0bd2\u0001\u0000\u0000\u0000\u0ba8\u0bcb\u0005"+
		"\u0002\u0000\u0000\u0ba9\u0baa\u00050\u0000\u0000\u0baa\u0bab\u0005(\u0000"+
		"\u0000\u0bab\u0bb0\u0003\u00cae\u0000\u0bac\u0bad\u0005\u0004\u0000\u0000"+
		"\u0bad\u0baf\u0003\u00cae\u0000\u0bae\u0bac\u0001\u0000\u0000\u0000\u0baf"+
		"\u0bb2\u0001\u0000\u0000\u0000\u0bb0\u0bae\u0001\u0000\u0000\u0000\u0bb0"+
		"\u0bb1\u0001\u0000\u0000\u0000\u0bb1\u0bcc\u0001\u0000\u0000\u0000\u0bb2"+
		"\u0bb0\u0001\u0000\u0000\u0000\u0bb3\u0bb4\u0007+\u0000\u0000\u0bb4\u0bb5"+
		"\u0005(\u0000\u0000\u0bb5\u0bba\u0003\u00cae\u0000\u0bb6\u0bb7\u0005\u0004"+
		"\u0000\u0000\u0bb7\u0bb9\u0003\u00cae\u0000\u0bb8\u0bb6\u0001\u0000\u0000"+
		"\u0000\u0bb9\u0bbc\u0001\u0000\u0000\u0000\u0bba\u0bb8\u0001\u0000\u0000"+
		"\u0000\u0bba\u0bbb\u0001\u0000\u0000\u0000\u0bbb\u0bbe\u0001\u0000\u0000"+
		"\u0000\u0bbc\u0bba\u0001\u0000\u0000\u0000\u0bbd\u0bb3\u0001\u0000\u0000"+
		"\u0000\u0bbd\u0bbe\u0001\u0000\u0000\u0000\u0bbe\u0bc9\u0001\u0000\u0000"+
		"\u0000\u0bbf\u0bc0\u0007,\u0000\u0000\u0bc0\u0bc1\u0005(\u0000\u0000\u0bc1"+
		"\u0bc6\u0003\\.\u0000\u0bc2\u0bc3\u0005\u0004\u0000\u0000\u0bc3\u0bc5"+
		"\u0003\\.\u0000\u0bc4\u0bc2\u0001\u0000\u0000\u0000\u0bc5\u0bc8\u0001"+
		"\u0000\u0000\u0000\u0bc6\u0bc4\u0001\u0000\u0000\u0000\u0bc6\u0bc7\u0001"+
		"\u0000\u0000\u0000\u0bc7\u0bca\u0001\u0000\u0000\u0000\u0bc8\u0bc6\u0001"+
		"\u0000\u0000\u0000\u0bc9\u0bbf\u0001\u0000\u0000\u0000\u0bc9\u0bca\u0001"+
		"\u0000\u0000\u0000\u0bca\u0bcc\u0001\u0000\u0000\u0000\u0bcb\u0ba9\u0001"+
		"\u0000\u0000\u0000\u0bcb\u0bbd\u0001\u0000\u0000\u0000\u0bcc\u0bce\u0001"+
		"\u0000\u0000\u0000\u0bcd\u0bcf\u0003\u0104\u0082\u0000\u0bce\u0bcd\u0001"+
		"\u0000\u0000\u0000\u0bce\u0bcf\u0001\u0000\u0000\u0000\u0bcf\u0bd0\u0001"+
		"\u0000\u0000\u0000\u0bd0\u0bd2\u0005\u0003\u0000\u0000\u0bd1\u0ba3\u0001"+
		"\u0000\u0000\u0000\u0bd1\u0ba4\u0001\u0000\u0000\u0000\u0bd1\u0ba8\u0001"+
		"\u0000\u0000\u0000\u0bd2\u0103\u0001\u0000\u0000\u0000\u0bd3\u0bd4\u0005"+
		"\u00c1\u0000\u0000\u0bd4\u0be4\u0003\u0106\u0083\u0000\u0bd5\u0bd6\u0005"+
		"\u00d6\u0000\u0000\u0bd6\u0be4\u0003\u0106\u0083\u0000\u0bd7\u0bd8\u0005"+
		"\u00c1\u0000\u0000\u0bd8\u0bd9\u0005$\u0000\u0000\u0bd9\u0bda\u0003\u0106"+
		"\u0083\u0000\u0bda\u0bdb\u0005\u001b\u0000\u0000\u0bdb\u0bdc\u0003\u0106"+
		"\u0083\u0000\u0bdc\u0be4\u0001\u0000\u0000\u0000\u0bdd\u0bde\u0005\u00d6"+
		"\u0000\u0000\u0bde\u0bdf\u0005$\u0000\u0000\u0bdf\u0be0\u0003\u0106\u0083"+
		"\u0000\u0be0\u0be1\u0005\u001b\u0000\u0000\u0be1\u0be2\u0003\u0106\u0083"+
		"\u0000\u0be2\u0be4\u0001\u0000\u0000\u0000\u0be3\u0bd3\u0001\u0000\u0000"+
		"\u0000\u0be3\u0bd5\u0001\u0000\u0000\u0000\u0be3\u0bd7\u0001\u0000\u0000"+
		"\u0000\u0be3\u0bdd\u0001\u0000\u0000\u0000\u0be4\u0105\u0001\u0000\u0000"+
		"\u0000\u0be5\u0be6\u0005\u0101\u0000\u0000\u0be6\u0bed\u0007-\u0000\u0000"+
		"\u0be7\u0be8\u0005B\u0000\u0000\u0be8\u0bed\u0005\u00d5\u0000\u0000\u0be9"+
		"\u0bea\u0003\u00cae\u0000\u0bea\u0beb\u0007-\u0000\u0000\u0beb\u0bed\u0001"+
		"\u0000\u0000\u0000\u0bec\u0be5\u0001\u0000\u0000\u0000\u0bec\u0be7\u0001"+
		"\u0000\u0000\u0000\u0bec\u0be9\u0001\u0000\u0000\u0000\u0bed\u0107\u0001"+
		"\u0000\u0000\u0000\u0bee\u0bf3\u0003\u010c\u0086\u0000\u0bef\u0bf0\u0005"+
		"\u0004\u0000\u0000\u0bf0\u0bf2\u0003\u010c\u0086\u0000\u0bf1\u0bef\u0001"+
		"\u0000\u0000\u0000\u0bf2\u0bf5\u0001\u0000\u0000\u0000\u0bf3\u0bf1\u0001"+
		"\u0000\u0000\u0000\u0bf3\u0bf4\u0001\u0000\u0000\u0000\u0bf4\u0109\u0001"+
		"\u0000\u0000\u0000\u0bf5\u0bf3\u0001\u0000\u0000\u0000\u0bf6\u0bfb\u0003"+
		"\u010c\u0086\u0000\u0bf7\u0bfb\u0005f\u0000\u0000\u0bf8\u0bfb\u0005\u008d"+
		"\u0000\u0000\u0bf9\u0bfb\u0005\u00cf\u0000\u0000\u0bfa\u0bf6\u0001\u0000"+
		"\u0000\u0000\u0bfa\u0bf7\u0001\u0000\u0000\u0000\u0bfa\u0bf8\u0001\u0000"+
		"\u0000\u0000\u0bfa\u0bf9\u0001\u0000\u0000\u0000\u0bfb\u010b\u0001\u0000"+
		"\u0000\u0000\u0bfc\u0c01\u0003\u0112\u0089\u0000\u0bfd\u0bfe\u0005\u0005"+
		"\u0000\u0000\u0bfe\u0c00\u0003\u0112\u0089\u0000\u0bff\u0bfd\u0001\u0000"+
		"\u0000\u0000\u0c00\u0c03\u0001\u0000\u0000\u0000\u0c01\u0bff\u0001\u0000"+
		"\u0000\u0000\u0c01\u0c02\u0001\u0000\u0000\u0000\u0c02\u010d\u0001\u0000"+
		"\u0000\u0000\u0c03\u0c01\u0001\u0000\u0000\u0000\u0c04\u0c05\u0003\u0112"+
		"\u0089\u0000\u0c05\u0c06\u0003\u0110\u0088\u0000\u0c06\u010f\u0001\u0000"+
		"\u0000\u0000\u0c07\u0c08\u0005\u011d\u0000\u0000\u0c08\u0c0a\u0003\u0112"+
		"\u0089\u0000\u0c09\u0c07\u0001\u0000\u0000\u0000\u0c0a\u0c0b\u0001\u0000"+
		"\u0000\u0000\u0c0b\u0c09\u0001\u0000\u0000\u0000\u0c0b\u0c0c\u0001\u0000"+
		"\u0000\u0000\u0c0c\u0c0f\u0001\u0000\u0000\u0000\u0c0d\u0c0f\u0001\u0000"+
		"\u0000\u0000\u0c0e\u0c09\u0001\u0000\u0000\u0000\u0c0e\u0c0d\u0001\u0000"+
		"\u0000\u0000\u0c0f\u0111\u0001\u0000\u0000\u0000\u0c10\u0c14\u0003\u0114"+
		"\u008a\u0000\u0c11\u0c12\u0004\u0089\u0010\u0000\u0c12\u0c14\u0003\u011e"+
		"\u008f\u0000\u0c13\u0c10\u0001\u0000\u0000\u0000\u0c13\u0c11\u0001\u0000"+
		"\u0000\u0000\u0c14\u0113\u0001\u0000\u0000\u0000\u0c15\u0c1c\u0005\u0130"+
		"\u0000\u0000\u0c16\u0c1c\u0003\u0116\u008b\u0000\u0c17\u0c18\u0004\u008a"+
		"\u0011\u0000\u0c18\u0c1c\u0003\u011c\u008e\u0000\u0c19\u0c1a\u0004\u008a"+
		"\u0012\u0000\u0c1a\u0c1c\u0003\u0120\u0090\u0000\u0c1b\u0c15\u0001\u0000"+
		"\u0000\u0000\u0c1b\u0c16\u0001\u0000\u0000\u0000\u0c1b\u0c17\u0001\u0000"+
		"\u0000\u0000\u0c1b\u0c19\u0001\u0000\u0000\u0000\u0c1c\u0115\u0001\u0000"+
		"\u0000\u0000\u0c1d\u0c1e\u0005\u0131\u0000\u0000\u0c1e\u0117\u0001\u0000"+
		"\u0000\u0000\u0c1f\u0c21\u0004\u008c\u0013\u0000\u0c20\u0c22\u0005\u011d"+
		"\u0000\u0000\u0c21\u0c20\u0001\u0000\u0000\u0000\u0c21\u0c22\u0001\u0000"+
		"\u0000\u0000\u0c22\u0c23\u0001\u0000\u0000\u0000\u0c23\u0c4b\u0005\u012b"+
		"\u0000\u0000\u0c24\u0c26\u0004\u008c\u0014\u0000\u0c25\u0c27\u0005\u011d"+
		"\u0000\u0000\u0c26\u0c25\u0001\u0000\u0000\u0000\u0c26\u0c27\u0001\u0000"+
		"\u0000\u0000\u0c27\u0c28\u0001\u0000\u0000\u0000\u0c28\u0c4b\u0005\u012c"+
		"\u0000\u0000\u0c29\u0c2b\u0004\u008c\u0015\u0000\u0c2a\u0c2c\u0005\u011d"+
		"\u0000\u0000\u0c2b\u0c2a\u0001\u0000\u0000\u0000\u0c2b\u0c2c\u0001\u0000"+
		"\u0000\u0000\u0c2c\u0c2d\u0001\u0000\u0000\u0000\u0c2d\u0c4b\u0007.\u0000"+
		"\u0000\u0c2e\u0c30\u0005\u011d\u0000\u0000\u0c2f\u0c2e\u0001\u0000\u0000"+
		"\u0000\u0c2f\u0c30\u0001\u0000\u0000\u0000\u0c30\u0c31\u0001\u0000\u0000"+
		"\u0000\u0c31\u0c4b\u0005\u012a\u0000\u0000\u0c32\u0c34\u0005\u011d\u0000"+
		"\u0000\u0c33\u0c32\u0001\u0000\u0000\u0000\u0c33\u0c34\u0001\u0000\u0000"+
		"\u0000\u0c34\u0c35\u0001\u0000\u0000\u0000\u0c35\u0c4b\u0005\u0127\u0000"+
		"\u0000\u0c36\u0c38\u0005\u011d\u0000\u0000\u0c37\u0c36\u0001\u0000\u0000"+
		"\u0000\u0c37\u0c38\u0001\u0000\u0000\u0000\u0c38\u0c39\u0001\u0000\u0000"+
		"\u0000\u0c39\u0c4b\u0005\u0128\u0000\u0000\u0c3a\u0c3c\u0005\u011d\u0000"+
		"\u0000\u0c3b\u0c3a\u0001\u0000\u0000\u0000\u0c3b\u0c3c\u0001\u0000\u0000"+
		"\u0000\u0c3c\u0c3d\u0001\u0000\u0000\u0000\u0c3d\u0c4b\u0005\u0129\u0000"+
		"\u0000\u0c3e\u0c40\u0005\u011d\u0000\u0000\u0c3f\u0c3e\u0001\u0000\u0000"+
		"\u0000\u0c3f\u0c40\u0001\u0000\u0000\u0000\u0c40\u0c41\u0001\u0000\u0000"+
		"\u0000\u0c41\u0c4b\u0005\u012e\u0000\u0000\u0c42\u0c44\u0005\u011d\u0000"+
		"\u0000\u0c43\u0c42\u0001\u0000\u0000\u0000\u0c43\u0c44\u0001\u0000\u0000"+
		"\u0000\u0c44\u0c45\u0001\u0000\u0000\u0000\u0c45\u0c4b\u0005\u012d\u0000"+
		"\u0000\u0c46\u0c48\u0005\u011d\u0000\u0000\u0c47\u0c46\u0001\u0000\u0000"+
		"\u0000\u0c47\u0c48\u0001\u0000\u0000\u0000\u0c48\u0c49\u0001\u0000\u0000"+
		"\u0000\u0c49\u0c4b\u0005\u012f\u0000\u0000\u0c4a\u0c1f\u0001\u0000\u0000"+
		"\u0000\u0c4a\u0c24\u0001\u0000\u0000\u0000\u0c4a\u0c29\u0001\u0000\u0000"+
		"\u0000\u0c4a\u0c2f\u0001\u0000\u0000\u0000\u0c4a\u0c33\u0001\u0000\u0000"+
		"\u0000\u0c4a\u0c37\u0001\u0000\u0000\u0000\u0c4a\u0c3b\u0001\u0000\u0000"+
		"\u0000\u0c4a\u0c3f\u0001\u0000\u0000\u0000\u0c4a\u0c43\u0001\u0000\u0000"+
		"\u0000\u0c4a\u0c47\u0001\u0000\u0000\u0000\u0c4b\u0119\u0001\u0000\u0000"+
		"\u0000\u0c4c\u0c4d\u0005\u00ff\u0000\u0000\u0c4d\u0c54\u0003\u00eew\u0000"+
		"\u0c4e\u0c54\u0003$\u0012\u0000\u0c4f\u0c54\u0003\u00ecv\u0000\u0c50\u0c51"+
		"\u0007/\u0000\u0000\u0c51\u0c52\u0005\u00a3\u0000\u0000\u0c52\u0c54\u0005"+
		"\u00a4\u0000\u0000\u0c53\u0c4c\u0001\u0000\u0000\u0000\u0c53\u0c4e\u0001"+
		"\u0000\u0000\u0000\u0c53\u0c4f\u0001\u0000\u0000\u0000\u0c53\u0c50\u0001"+
		"\u0000\u0000\u0000\u0c54\u011b\u0001\u0000\u0000\u0000\u0c55\u0c56\u0007"+
		"0\u0000\u0000\u0c56\u011d\u0001\u0000\u0000\u0000\u0c57\u0c58\u00071\u0000"+
		"\u0000\u0c58\u011f\u0001\u0000\u0000\u0000\u0c59\u0c5a\u00072\u0000\u0000"+
		"\u0c5a\u0121\u0001\u0000\u0000\u0000\u01a0\u0132\u0138\u0151\u0156\u015e"+
		"\u0166\u0168\u017c\u0180\u0186\u0189\u018c\u0193\u0196\u019a\u019d\u01a4"+
		"\u01af\u01b1\u01b9\u01bc\u01c0\u01c3\u01c9\u01d4\u01da\u01df\u021f\u0228"+
		"\u022c\u0232\u0236\u023b\u0241\u024d\u0255\u025b\u0268\u026d\u027d\u0284"+
		"\u0288\u028e\u029d\u02a1\u02a7\u02ad\u02b0\u02b3\u02b9\u02bd\u02c5\u02c7"+
		"\u02d0\u02d3\u02dc\u02e1\u02e7\u02ee\u02f1\u02f7\u0302\u0305\u0309\u030e"+
		"\u0313\u031a\u031d\u0320\u0327\u032c\u0335\u033d\u0343\u0346\u0349\u034f"+
		"\u0353\u0357\u035b\u035d\u0365\u036d\u0373\u0379\u037c\u0380\u0383\u0387"+
		"\u03a3\u03a6\u03aa\u03b0\u03b3\u03b6\u03bc\u03c4\u03c9\u03cf\u03d5\u03dd"+
		"\u03e4\u03ec\u03fd\u040b\u040e\u0414\u041d\u0426\u042d\u0430\u043c\u0440"+
		"\u0447\u04bb\u04c3\u04cb\u04d4\u04de\u04e2\u04e5\u04eb\u04f1\u04fd\u0509"+
		"\u050e\u0517\u051f\u0526\u0528\u052b\u0530\u0534\u0539\u053c\u0541\u0546"+
		"\u0549\u054e\u0552\u0557\u0559\u055d\u0566\u056e\u0577\u057e\u0587\u058c"+
		"\u058f\u05a5\u05a7\u05b0\u05b7\u05ba\u05c1\u05c5\u05cb\u05d3\u05de\u05e9"+
		"\u05f0\u05f6\u0603\u060a\u0611\u061d\u0625\u062b\u062e\u0637\u063a\u0643"+
		"\u0646\u064f\u0652\u065b\u065e\u0661\u0666\u0668\u0674\u067b\u0682\u0685"+
		"\u0687\u0693\u0697\u069b\u06a1\u06a5\u06ad\u06b1\u06b4\u06b7\u06ba\u06be"+
		"\u06c2\u06c7\u06cb\u06ce\u06d1\u06d4\u06d8\u06dd\u06e1\u06e4\u06e7\u06ea"+
		"\u06ec\u06f2\u06f9\u06fe\u0701\u0704\u0708\u0712\u0716\u0718\u071b\u071f"+
		"\u0725\u0729\u0734\u073e\u074a\u0759\u075e\u0765\u0775\u077a\u0787\u078c"+
		"\u0794\u079a\u079e\u07a7\u07b1\u07c0\u07c5\u07c7\u07cb\u07d4\u07e1\u07e6"+
		"\u07ea\u07f2\u07f5\u07f9\u0807\u0814\u0819\u081d\u0820\u0825\u082e\u0831"+
		"\u0836\u083d\u0840\u0845\u084b\u0851\u0855\u085b\u085f\u0862\u0867\u086a"+
		"\u086f\u0873\u0876\u0879\u087f\u0884\u0889\u089b\u089d\u08a0\u08ab\u08b4"+
		"\u08bb\u08c3\u08ca\u08ce\u08d6\u08de\u08e4\u08ec\u08f8\u08fb\u0901\u0905"+
		"\u0907\u0910\u091c\u091e\u0925\u092c\u0932\u0938\u093a\u0941\u0949\u094f"+
		"\u0956\u095c\u0960\u0962\u0969\u0972\u0979\u0983\u0988\u098c\u0995\u09a2"+
		"\u09a4\u09ac\u09ae\u09b2\u09ba\u09c3\u09c9\u09d1\u09d6\u09e2\u09e7\u09ea"+
		"\u09f0\u09f4\u09f9\u09fe\u0a03\u0a09\u0a1e\u0a20\u0a29\u0a2d\u0a36\u0a3a"+
		"\u0a4c\u0a4f\u0a57\u0a60\u0a77\u0a82\u0a89\u0a8c\u0a95\u0a99\u0a9d\u0aa9"+
		"\u0ac2\u0ac9\u0acc\u0adb\u0adf\u0ae9\u0aeb\u0af8\u0afa\u0b07\u0b0b\u0b12"+
		"\u0b17\u0b1f\u0b26\u0b37\u0b3b\u0b41\u0b47\u0b50\u0b54\u0b56\u0b5d\u0b64"+
		"\u0b67\u0b6a\u0b71\u0b78\u0b7b\u0b82\u0b87\u0b8c\u0b8f\u0b9c\u0bb0\u0bba"+
		"\u0bbd\u0bc6\u0bc9\u0bcb\u0bce\u0bd1\u0be3\u0bec\u0bf3\u0bfa\u0c01\u0c0b"+
		"\u0c0e\u0c13\u0c1b\u0c21\u0c26\u0c2b\u0c2f\u0c33\u0c37\u0c3b\u0c3f\u0c43"+
		"\u0c47\u0c4a\u0c53";
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