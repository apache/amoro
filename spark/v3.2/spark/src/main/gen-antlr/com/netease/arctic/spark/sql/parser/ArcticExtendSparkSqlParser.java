// Generated from /Users/jinsilei/arctic/arctic/spark/v3.2/spark/src/main/antlr4/com/netease/arctic/spark/sql/parser/ArcticExtendSparkSql.g4 by ANTLR 4.10.1
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
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, KEY=12, MIGRATE=13, ADD=14, AFTER=15, ALL=16, ALTER=17, 
		ANALYZE=18, AND=19, ANTI=20, ANY=21, ARCHIVE=22, ARRAY=23, AS=24, ASC=25, 
		AT=26, AUTHORIZATION=27, BETWEEN=28, BOTH=29, BUCKET=30, BUCKETS=31, BY=32, 
		CACHE=33, CASCADE=34, CASE=35, CAST=36, CHANGE=37, CHECK=38, CLEAR=39, 
		CLUSTER=40, CLUSTERED=41, CODEGEN=42, COLLATE=43, COLLECTION=44, COLUMN=45, 
		COLUMNS=46, COMMENT=47, COMMIT=48, COMPACT=49, COMPACTIONS=50, COMPUTE=51, 
		CONCATENATE=52, CONSTRAINT=53, COST=54, CREATE=55, CROSS=56, CUBE=57, 
		CURRENT=58, CURRENT_DATE=59, CURRENT_TIME=60, CURRENT_TIMESTAMP=61, CURRENT_USER=62, 
		DAY=63, DATA=64, DATABASE=65, DATABASES=66, DBPROPERTIES=67, DEFINED=68, 
		DELETE=69, DELIMITED=70, DESC=71, DESCRIBE=72, DFS=73, DIRECTORIES=74, 
		DIRECTORY=75, DISTINCT=76, DISTRIBUTE=77, DIV=78, DROP=79, ELSE=80, END=81, 
		ESCAPE=82, ESCAPED=83, EXCEPT=84, EXCHANGE=85, EXISTS=86, EXPLAIN=87, 
		EXPORT=88, EXTENDED=89, EXTERNAL=90, EXTRACT=91, FALSE=92, FETCH=93, FIELDS=94, 
		FILTER=95, FILEFORMAT=96, FIRST=97, FOLLOWING=98, FOR=99, FOREIGN=100, 
		FORMAT=101, FORMATTED=102, FROM=103, FULL=104, FUNCTION=105, FUNCTIONS=106, 
		GLOBAL=107, GRANT=108, GROUP=109, GROUPING=110, HAVING=111, HOUR=112, 
		IF=113, IGNORE=114, IMPORT=115, IN=116, INDEX=117, INDEXES=118, INNER=119, 
		INPATH=120, INPUTFORMAT=121, INSERT=122, INTERSECT=123, INTERVAL=124, 
		INTO=125, IS=126, ITEMS=127, JOIN=128, KEYS=129, LAST=130, LATERAL=131, 
		LAZY=132, LEADING=133, LEFT=134, LIKE=135, LIMIT=136, LINES=137, LIST=138, 
		LOAD=139, LOCAL=140, LOCATION=141, LOCK=142, LOCKS=143, LOGICAL=144, MACRO=145, 
		MAP=146, MATCHED=147, MERGE=148, MINUTE=149, MONTH=150, MSCK=151, NAMESPACE=152, 
		NAMESPACES=153, NATURAL=154, NO=155, NOT=156, NULL=157, NULLS=158, OF=159, 
		ON=160, ONLY=161, OPTION=162, OPTIONS=163, OR=164, ORDER=165, OUT=166, 
		OUTER=167, OUTPUTFORMAT=168, OVER=169, OVERLAPS=170, OVERLAY=171, OVERWRITE=172, 
		PARTITION=173, PARTITIONED=174, PARTITIONS=175, PERCENTLIT=176, PIVOT=177, 
		PLACING=178, POSITION=179, PRECEDING=180, PRIMARY=181, PRINCIPALS=182, 
		PROPERTIES=183, PURGE=184, QUERY=185, RANGE=186, RECORDREADER=187, RECORDWRITER=188, 
		RECOVER=189, REDUCE=190, REFERENCES=191, REFRESH=192, RENAME=193, REPAIR=194, 
		REPLACE=195, RESET=196, RESPECT=197, RESTRICT=198, REVOKE=199, RIGHT=200, 
		RLIKE=201, ROLE=202, ROLES=203, ROLLBACK=204, ROLLUP=205, ROW=206, ROWS=207, 
		SECOND=208, SCHEMA=209, SELECT=210, SEMI=211, SEPARATED=212, SERDE=213, 
		SERDEPROPERTIES=214, SESSION_USER=215, SET=216, SETMINUS=217, SETS=218, 
		SHOW=219, SKEWED=220, SOME=221, SORT=222, SORTED=223, START=224, STATISTICS=225, 
		STORED=226, STRATIFY=227, STRUCT=228, SUBSTR=229, SUBSTRING=230, SYNC=231, 
		TABLE=232, TABLES=233, TABLESAMPLE=234, TBLPROPERTIES=235, TEMPORARY=236, 
		TERMINATED=237, THEN=238, TIME=239, TO=240, TOUCH=241, TRAILING=242, TRANSACTION=243, 
		TRANSACTIONS=244, TRANSFORM=245, TRIM=246, TRUE=247, TRUNCATE=248, TRY_CAST=249, 
		TYPE=250, UNARCHIVE=251, UNBOUNDED=252, UNCACHE=253, UNION=254, UNIQUE=255, 
		UNKNOWN=256, UNLOCK=257, UNSET=258, UPDATE=259, USE=260, USER=261, USING=262, 
		VALUES=263, VIEW=264, VIEWS=265, WHEN=266, WHERE=267, WINDOW=268, WITH=269, 
		YEAR=270, ZONE=271, EQ=272, NSEQ=273, NEQ=274, NEQJ=275, LT=276, LTE=277, 
		GT=278, GTE=279, PLUS=280, MINUS=281, ASTERISK=282, SLASH=283, PERCENT=284, 
		TILDE=285, AMPERSAND=286, PIPE=287, CONCAT_PIPE=288, HAT=289, STRING=290, 
		BIGINT_LITERAL=291, SMALLINT_LITERAL=292, TINYINT_LITERAL=293, INTEGER_VALUE=294, 
		EXPONENT_VALUE=295, DECIMAL_VALUE=296, FLOAT_LITERAL=297, DOUBLE_LITERAL=298, 
		BIGDECIMAL_LITERAL=299, IDENTIFIER=300, BACKQUOTED_IDENTIFIER=301, SIMPLE_COMMENT=302, 
		BRACKETED_COMMENT=303, WS=304, UNRECOGNIZED=305;
	public static final int
		RULE_arcticCommand = 0, RULE_arcticStatement = 1, RULE_createTableWithPrimaryKey = 2, 
		RULE_primarySpec = 3, RULE_colListAndPk = 4, RULE_singleStatement = 5, 
		RULE_singleExpression = 6, RULE_singleTableIdentifier = 7, RULE_singleMultipartIdentifier = 8, 
		RULE_singleFunctionIdentifier = 9, RULE_singleDataType = 10, RULE_singleTableSchema = 11, 
		RULE_statement = 12, RULE_configKey = 13, RULE_configValue = 14, RULE_unsupportedHiveNativeCommands = 15, 
		RULE_createTableHeader = 16, RULE_replaceTableHeader = 17, RULE_bucketSpec = 18, 
		RULE_skewSpec = 19, RULE_locationSpec = 20, RULE_commentSpec = 21, RULE_query = 22, 
		RULE_insertInto = 23, RULE_partitionSpecLocation = 24, RULE_partitionSpec = 25, 
		RULE_partitionVal = 26, RULE_namespace = 27, RULE_describeFuncName = 28, 
		RULE_describeColName = 29, RULE_ctes = 30, RULE_namedQuery = 31, RULE_tableProvider = 32, 
		RULE_createTableClauses = 33, RULE_tablePropertyList = 34, RULE_tableProperty = 35, 
		RULE_tablePropertyKey = 36, RULE_tablePropertyValue = 37, RULE_constantList = 38, 
		RULE_nestedConstantList = 39, RULE_createFileFormat = 40, RULE_fileFormat = 41, 
		RULE_storageHandler = 42, RULE_resource = 43, RULE_dmlStatementNoWith = 44, 
		RULE_queryOrganization = 45, RULE_multiInsertQueryBody = 46, RULE_queryTerm = 47, 
		RULE_queryPrimary = 48, RULE_sortItem = 49, RULE_fromStatement = 50, RULE_fromStatementBody = 51, 
		RULE_querySpecification = 52, RULE_transformClause = 53, RULE_selectClause = 54, 
		RULE_setClause = 55, RULE_matchedClause = 56, RULE_notMatchedClause = 57, 
		RULE_matchedAction = 58, RULE_notMatchedAction = 59, RULE_assignmentList = 60, 
		RULE_assignment = 61, RULE_whereClause = 62, RULE_havingClause = 63, RULE_hint = 64, 
		RULE_hintStatement = 65, RULE_fromClause = 66, RULE_aggregationClause = 67, 
		RULE_groupByClause = 68, RULE_groupingAnalytics = 69, RULE_groupingElement = 70, 
		RULE_groupingSet = 71, RULE_pivotClause = 72, RULE_pivotColumn = 73, RULE_pivotValue = 74, 
		RULE_lateralView = 75, RULE_setQuantifier = 76, RULE_relation = 77, RULE_joinRelation = 78, 
		RULE_joinType = 79, RULE_joinCriteria = 80, RULE_sample = 81, RULE_sampleMethod = 82, 
		RULE_identifierList = 83, RULE_identifierSeq = 84, RULE_orderedIdentifierList = 85, 
		RULE_orderedIdentifier = 86, RULE_identifierCommentList = 87, RULE_identifierComment = 88, 
		RULE_relationPrimary = 89, RULE_inlineTable = 90, RULE_functionTable = 91, 
		RULE_tableAlias = 92, RULE_rowFormat = 93, RULE_multipartIdentifierList = 94, 
		RULE_multipartIdentifier = 95, RULE_tableIdentifier = 96, RULE_functionIdentifier = 97, 
		RULE_namedExpression = 98, RULE_namedExpressionSeq = 99, RULE_partitionFieldList = 100, 
		RULE_partitionField = 101, RULE_transform = 102, RULE_transformArgument = 103, 
		RULE_expression = 104, RULE_expressionSeq = 105, RULE_booleanExpression = 106, 
		RULE_predicate = 107, RULE_valueExpression = 108, RULE_primaryExpression = 109, 
		RULE_constant = 110, RULE_comparisonOperator = 111, RULE_arithmeticOperator = 112, 
		RULE_predicateOperator = 113, RULE_booleanValue = 114, RULE_interval = 115, 
		RULE_errorCapturingMultiUnitsInterval = 116, RULE_multiUnitsInterval = 117, 
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
			"arcticCommand", "arcticStatement", "createTableWithPrimaryKey", "primarySpec", 
			"colListAndPk", "singleStatement", "singleExpression", "singleTableIdentifier", 
			"singleMultipartIdentifier", "singleFunctionIdentifier", "singleDataType", 
			"singleTableSchema", "statement", "configKey", "configValue", "unsupportedHiveNativeCommands", 
			"createTableHeader", "replaceTableHeader", "bucketSpec", "skewSpec", 
			"locationSpec", "commentSpec", "query", "insertInto", "partitionSpecLocation", 
			"partitionSpec", "partitionVal", "namespace", "describeFuncName", "describeColName", 
			"ctes", "namedQuery", "tableProvider", "createTableClauses", "tablePropertyList", 
			"tableProperty", "tablePropertyKey", "tablePropertyValue", "constantList", 
			"nestedConstantList", "createFileFormat", "fileFormat", "storageHandler", 
			"resource", "dmlStatementNoWith", "queryOrganization", "multiInsertQueryBody", 
			"queryTerm", "queryPrimary", "sortItem", "fromStatement", "fromStatementBody", 
			"querySpecification", "transformClause", "selectClause", "setClause", 
			"matchedClause", "notMatchedClause", "matchedAction", "notMatchedAction", 
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
			null, "'('", "','", "')'", "';'", "'.'", "'/*+'", "'*/'", "'->'", "'['", 
			"']'", "':'", "'KEY'", "'MIGRATE'", "'ADD'", "'AFTER'", "'ALL'", "'ALTER'", 
			"'ANALYZE'", "'AND'", "'ANTI'", "'ANY'", "'ARCHIVE'", "'ARRAY'", "'AS'", 
			"'ASC'", "'AT'", "'AUTHORIZATION'", "'BETWEEN'", "'BOTH'", "'BUCKET'", 
			"'BUCKETS'", "'BY'", "'CACHE'", "'CASCADE'", "'CASE'", "'CAST'", "'CHANGE'", 
			"'CHECK'", "'CLEAR'", "'CLUSTER'", "'CLUSTERED'", "'CODEGEN'", "'COLLATE'", 
			"'COLLECTION'", "'COLUMN'", "'COLUMNS'", "'COMMENT'", "'COMMIT'", "'COMPACT'", 
			"'COMPACTIONS'", "'COMPUTE'", "'CONCATENATE'", "'CONSTRAINT'", "'COST'", 
			"'CREATE'", "'CROSS'", "'CUBE'", "'CURRENT'", "'CURRENT_DATE'", "'CURRENT_TIME'", 
			"'CURRENT_TIMESTAMP'", "'CURRENT_USER'", "'DAY'", "'DATA'", "'DATABASE'", 
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
			"'SUBSTRING'", "'SYNC'", "'TABLE'", "'TABLES'", "'TABLESAMPLE'", "'TBLPROPERTIES'", 
			null, "'TERMINATED'", "'THEN'", "'TIME'", "'TO'", "'TOUCH'", "'TRAILING'", 
			"'TRANSACTION'", "'TRANSACTIONS'", "'TRANSFORM'", "'TRIM'", "'TRUE'", 
			"'TRUNCATE'", "'TRY_CAST'", "'TYPE'", "'UNARCHIVE'", "'UNBOUNDED'", "'UNCACHE'", 
			"'UNION'", "'UNIQUE'", "'UNKNOWN'", "'UNLOCK'", "'UNSET'", "'UPDATE'", 
			"'USE'", "'USER'", "'USING'", "'VALUES'", "'VIEW'", "'VIEWS'", "'WHEN'", 
			"'WHERE'", "'WINDOW'", "'WITH'", "'YEAR'", "'ZONE'", null, "'<=>'", "'<>'", 
			"'!='", "'<'", null, "'>'", null, "'+'", "'-'", "'*'", "'/'", "'%'", 
			"'~'", "'&'", "'|'", "'||'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"KEY", "MIGRATE", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "ANTI", 
			"ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT", "AUTHORIZATION", "BETWEEN", 
			"BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", "CASCADE", "CASE", "CAST", 
			"CHANGE", "CHECK", "CLEAR", "CLUSTER", "CLUSTERED", "CODEGEN", "COLLATE", 
			"COLLECTION", "COLUMN", "COLUMNS", "COMMENT", "COMMIT", "COMPACT", "COMPACTIONS", 
			"COMPUTE", "CONCATENATE", "CONSTRAINT", "COST", "CREATE", "CROSS", "CUBE", 
			"CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", 
			"DAY", "DATA", "DATABASE", "DATABASES", "DBPROPERTIES", "DEFINED", "DELETE", 
			"DELIMITED", "DESC", "DESCRIBE", "DFS", "DIRECTORIES", "DIRECTORY", "DISTINCT", 
			"DISTRIBUTE", "DIV", "DROP", "ELSE", "END", "ESCAPE", "ESCAPED", "EXCEPT", 
			"EXCHANGE", "EXISTS", "EXPLAIN", "EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", 
			"FALSE", "FETCH", "FIELDS", "FILTER", "FILEFORMAT", "FIRST", "FOLLOWING", 
			"FOR", "FOREIGN", "FORMAT", "FORMATTED", "FROM", "FULL", "FUNCTION", 
			"FUNCTIONS", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HAVING", "HOUR", 
			"IF", "IGNORE", "IMPORT", "IN", "INDEX", "INDEXES", "INNER", "INPATH", 
			"INPUTFORMAT", "INSERT", "INTERSECT", "INTERVAL", "INTO", "IS", "ITEMS", 
			"JOIN", "KEYS", "LAST", "LATERAL", "LAZY", "LEADING", "LEFT", "LIKE", 
			"LIMIT", "LINES", "LIST", "LOAD", "LOCAL", "LOCATION", "LOCK", "LOCKS", 
			"LOGICAL", "MACRO", "MAP", "MATCHED", "MERGE", "MINUTE", "MONTH", "MSCK", 
			"NAMESPACE", "NAMESPACES", "NATURAL", "NO", "NOT", "NULL", "NULLS", "OF", 
			"ON", "ONLY", "OPTION", "OPTIONS", "OR", "ORDER", "OUT", "OUTER", "OUTPUTFORMAT", 
			"OVER", "OVERLAPS", "OVERLAY", "OVERWRITE", "PARTITION", "PARTITIONED", 
			"PARTITIONS", "PERCENTLIT", "PIVOT", "PLACING", "POSITION", "PRECEDING", 
			"PRIMARY", "PRINCIPALS", "PROPERTIES", "PURGE", "QUERY", "RANGE", "RECORDREADER", 
			"RECORDWRITER", "RECOVER", "REDUCE", "REFERENCES", "REFRESH", "RENAME", 
			"REPAIR", "REPLACE", "RESET", "RESPECT", "RESTRICT", "REVOKE", "RIGHT", 
			"RLIKE", "ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SECOND", 
			"SCHEMA", "SELECT", "SEMI", "SEPARATED", "SERDE", "SERDEPROPERTIES", 
			"SESSION_USER", "SET", "SETMINUS", "SETS", "SHOW", "SKEWED", "SOME", 
			"SORT", "SORTED", "START", "STATISTICS", "STORED", "STRATIFY", "STRUCT", 
			"SUBSTR", "SUBSTRING", "SYNC", "TABLE", "TABLES", "TABLESAMPLE", "TBLPROPERTIES", 
			"TEMPORARY", "TERMINATED", "THEN", "TIME", "TO", "TOUCH", "TRAILING", 
			"TRANSACTION", "TRANSACTIONS", "TRANSFORM", "TRIM", "TRUE", "TRUNCATE", 
			"TRY_CAST", "TYPE", "UNARCHIVE", "UNBOUNDED", "UNCACHE", "UNION", "UNIQUE", 
			"UNKNOWN", "UNLOCK", "UNSET", "UPDATE", "USE", "USER", "USING", "VALUES", 
			"VIEW", "VIEWS", "WHEN", "WHERE", "WINDOW", "WITH", "YEAR", "ZONE", "EQ", 
			"NSEQ", "NEQ", "NEQJ", "LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "ASTERISK", 
			"SLASH", "PERCENT", "TILDE", "AMPERSAND", "PIPE", "CONCAT_PIPE", "HAT", 
			"STRING", "BIGINT_LITERAL", "SMALLINT_LITERAL", "TINYINT_LITERAL", "INTEGER_VALUE", 
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
			setState(296);
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
			setState(298);
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
			setState(300);
			createTableHeader();
			setState(301);
			colListAndPk();
			setState(302);
			tableProvider();
			setState(303);
			createTableClauses();
			setState(308);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0 || _la==AS || _la==FROM || _la==MAP || ((((_la - 190)) & ~0x3f) == 0 && ((1L << (_la - 190)) & ((1L << (REDUCE - 190)) | (1L << (SELECT - 190)) | (1L << (TABLE - 190)))) != 0) || _la==VALUES || _la==WITH) {
				{
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(304);
					match(AS);
					}
				}

				setState(307);
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
			setState(310);
			match(PRIMARY);
			setState(311);
			match(KEY);
			setState(312);
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
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
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
			setState(323);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				_localctx = new ColListWithPkContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(314);
				match(T__0);
				setState(315);
				colTypeList();
				setState(318);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(316);
					match(T__1);
					setState(317);
					primarySpec();
					}
				}

				setState(320);
				match(T__2);
				}
				break;
			case PRIMARY:
				_localctx = new ColListOnlyPkContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(322);
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
			setState(325);
			statement();
			setState(329);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(326);
				match(T__3);
				}
				}
				setState(331);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(332);
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
			setState(334);
			namedExpression();
			setState(335);
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
			setState(337);
			tableIdentifier();
			setState(338);
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
			setState(340);
			multipartIdentifier();
			setState(341);
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
			setState(343);
			functionIdentifier();
			setState(344);
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
			setState(346);
			dataType();
			setState(347);
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
			setState(349);
			colTypeList();
			setState(350);
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
		public TerminalNode NAMESPACE() { return getToken(ArcticExtendSparkSqlParser.NAMESPACE, 0); }
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
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
	public static class ShowCurrentNamespaceContext extends StatementContext {
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode CURRENT() { return getToken(ArcticExtendSparkSqlParser.CURRENT, 0); }
		public TerminalNode NAMESPACE() { return getToken(ArcticExtendSparkSqlParser.NAMESPACE, 0); }
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
		public TerminalNode DATABASES() { return getToken(ArcticExtendSparkSqlParser.DATABASES, 0); }
		public TerminalNode NAMESPACES() { return getToken(ArcticExtendSparkSqlParser.NAMESPACES, 0); }
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
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		public ColTypeListContext colTypeList() {
			return getRuleContext(ColTypeListContext.class,0);
		}
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
		public TablePropertyListContext tableProps;
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
		public List<TablePropertyListContext> tablePropertyList() {
			return getRuleContexts(TablePropertyListContext.class);
		}
		public TablePropertyListContext tablePropertyList(int i) {
			return getRuleContext(TablePropertyListContext.class,i);
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
		public TablePropertyKeyContext key;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TablePropertyKeyContext tablePropertyKey() {
			return getRuleContext(TablePropertyKeyContext.class,0);
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		public TerminalNode COLUMN() { return getToken(ArcticExtendSparkSqlParser.COLUMN, 0); }
		public TerminalNode COLUMNS() { return getToken(ArcticExtendSparkSqlParser.COLUMNS, 0); }
		public MultipartIdentifierListContext multipartIdentifierList() {
			return getRuleContext(MultipartIdentifierListContext.class,0);
		}
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
		public Token pattern;
		public TerminalNode SHOW() { return getToken(ArcticExtendSparkSqlParser.SHOW, 0); }
		public TerminalNode FUNCTIONS() { return getToken(ArcticExtendSparkSqlParser.FUNCTIONS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
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
		public TablePropertyListContext options;
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
			setState(1089);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
			case 1:
				_localctx = new StatementDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(352);
				query();
				}
				break;
			case 2:
				_localctx = new DmlStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(354);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(353);
					ctes();
					}
				}

				setState(356);
				dmlStatementNoWith();
				}
				break;
			case 3:
				_localctx = new UseContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(357);
				match(USE);
				setState(359);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(358);
					match(NAMESPACE);
					}
					break;
				}
				setState(361);
				multipartIdentifier();
				}
				break;
			case 4:
				_localctx = new CreateNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(362);
				match(CREATE);
				setState(363);
				namespace();
				setState(367);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(364);
					match(IF);
					setState(365);
					match(NOT);
					setState(366);
					match(EXISTS);
					}
					break;
				}
				setState(369);
				multipartIdentifier();
				setState(377);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==LOCATION || _la==WITH) {
					{
					setState(375);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(370);
						commentSpec();
						}
						break;
					case LOCATION:
						{
						setState(371);
						locationSpec();
						}
						break;
					case WITH:
						{
						{
						setState(372);
						match(WITH);
						setState(373);
						_la = _input.LA(1);
						if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(374);
						tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(379);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 5:
				_localctx = new SetNamespacePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(380);
				match(ALTER);
				setState(381);
				namespace();
				setState(382);
				multipartIdentifier();
				setState(383);
				match(SET);
				setState(384);
				_la = _input.LA(1);
				if ( !(_la==DBPROPERTIES || _la==PROPERTIES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(385);
				tablePropertyList();
				}
				break;
			case 6:
				_localctx = new SetNamespaceLocationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(387);
				match(ALTER);
				setState(388);
				namespace();
				setState(389);
				multipartIdentifier();
				setState(390);
				match(SET);
				setState(391);
				locationSpec();
				}
				break;
			case 7:
				_localctx = new DropNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(393);
				match(DROP);
				setState(394);
				namespace();
				setState(397);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
				case 1:
					{
					setState(395);
					match(IF);
					setState(396);
					match(EXISTS);
					}
					break;
				}
				setState(399);
				multipartIdentifier();
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CASCADE || _la==RESTRICT) {
					{
					setState(400);
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
				setState(403);
				match(SHOW);
				setState(404);
				_la = _input.LA(1);
				if ( !(_la==DATABASES || _la==NAMESPACES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(407);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(405);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(406);
					multipartIdentifier();
					}
				}

				setState(413);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(410);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(409);
						match(LIKE);
						}
					}

					setState(412);
					((ShowNamespacesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 9:
				_localctx = new CreateTableContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(415);
				createTableHeader();
				setState(420);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(416);
					match(T__0);
					setState(417);
					colTypeList();
					setState(418);
					match(T__2);
					}
					break;
				}
				setState(423);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(422);
					tableProvider();
					}
				}

				setState(425);
				createTableClauses();
				setState(430);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0 || _la==AS || _la==FROM || _la==MAP || ((((_la - 190)) & ~0x3f) == 0 && ((1L << (_la - 190)) & ((1L << (REDUCE - 190)) | (1L << (SELECT - 190)) | (1L << (TABLE - 190)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(427);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(426);
						match(AS);
						}
					}

					setState(429);
					query();
					}
				}

				}
				break;
			case 10:
				_localctx = new CreateTableLikeContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(432);
				match(CREATE);
				setState(433);
				match(TABLE);
				setState(437);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(434);
					match(IF);
					setState(435);
					match(NOT);
					setState(436);
					match(EXISTS);
					}
					break;
				}
				setState(439);
				((CreateTableLikeContext)_localctx).target = tableIdentifier();
				setState(440);
				match(LIKE);
				setState(441);
				((CreateTableLikeContext)_localctx).source = tableIdentifier();
				setState(450);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LOCATION || ((((_la - 206)) & ~0x3f) == 0 && ((1L << (_la - 206)) & ((1L << (ROW - 206)) | (1L << (STORED - 206)) | (1L << (TBLPROPERTIES - 206)) | (1L << (USING - 206)))) != 0)) {
					{
					setState(448);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case USING:
						{
						setState(442);
						tableProvider();
						}
						break;
					case ROW:
						{
						setState(443);
						rowFormat();
						}
						break;
					case STORED:
						{
						setState(444);
						createFileFormat();
						}
						break;
					case LOCATION:
						{
						setState(445);
						locationSpec();
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(446);
						match(TBLPROPERTIES);
						setState(447);
						((CreateTableLikeContext)_localctx).tableProps = tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(452);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 11:
				_localctx = new ReplaceTableContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(453);
				replaceTableHeader();
				setState(458);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
				case 1:
					{
					setState(454);
					match(T__0);
					setState(455);
					colTypeList();
					setState(456);
					match(T__2);
					}
					break;
				}
				setState(461);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(460);
					tableProvider();
					}
				}

				setState(463);
				createTableClauses();
				setState(468);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0 || _la==AS || _la==FROM || _la==MAP || ((((_la - 190)) & ~0x3f) == 0 && ((1L << (_la - 190)) & ((1L << (REDUCE - 190)) | (1L << (SELECT - 190)) | (1L << (TABLE - 190)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(465);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(464);
						match(AS);
						}
					}

					setState(467);
					query();
					}
				}

				}
				break;
			case 12:
				_localctx = new AnalyzeContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(470);
				match(ANALYZE);
				setState(471);
				match(TABLE);
				setState(472);
				multipartIdentifier();
				setState(474);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(473);
					partitionSpec();
					}
				}

				setState(476);
				match(COMPUTE);
				setState(477);
				match(STATISTICS);
				setState(485);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(478);
					identifier();
					}
					break;
				case 2:
					{
					setState(479);
					match(FOR);
					setState(480);
					match(COLUMNS);
					setState(481);
					identifierSeq();
					}
					break;
				case 3:
					{
					setState(482);
					match(FOR);
					setState(483);
					match(ALL);
					setState(484);
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
				setState(487);
				match(ANALYZE);
				setState(488);
				match(TABLES);
				setState(491);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(489);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(490);
					multipartIdentifier();
					}
				}

				setState(493);
				match(COMPUTE);
				setState(494);
				match(STATISTICS);
				setState(496);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(495);
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
				setState(498);
				match(ALTER);
				setState(499);
				match(TABLE);
				setState(500);
				multipartIdentifier();
				setState(501);
				match(ADD);
				setState(502);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(503);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				}
				break;
			case 15:
				_localctx = new AddTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(505);
				match(ALTER);
				setState(506);
				match(TABLE);
				setState(507);
				multipartIdentifier();
				setState(508);
				match(ADD);
				setState(509);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(510);
				match(T__0);
				setState(511);
				((AddTableColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(512);
				match(T__2);
				}
				break;
			case 16:
				_localctx = new RenameTableColumnContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(514);
				match(ALTER);
				setState(515);
				match(TABLE);
				setState(516);
				((RenameTableColumnContext)_localctx).table = multipartIdentifier();
				setState(517);
				match(RENAME);
				setState(518);
				match(COLUMN);
				setState(519);
				((RenameTableColumnContext)_localctx).from = multipartIdentifier();
				setState(520);
				match(TO);
				setState(521);
				((RenameTableColumnContext)_localctx).to = errorCapturingIdentifier();
				}
				break;
			case 17:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(523);
				match(ALTER);
				setState(524);
				match(TABLE);
				setState(525);
				multipartIdentifier();
				setState(526);
				match(DROP);
				setState(527);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(528);
				match(T__0);
				setState(529);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				setState(530);
				match(T__2);
				}
				break;
			case 18:
				_localctx = new DropTableColumnsContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(532);
				match(ALTER);
				setState(533);
				match(TABLE);
				setState(534);
				multipartIdentifier();
				setState(535);
				match(DROP);
				setState(536);
				_la = _input.LA(1);
				if ( !(_la==COLUMN || _la==COLUMNS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(537);
				((DropTableColumnsContext)_localctx).columns = multipartIdentifierList();
				}
				break;
			case 19:
				_localctx = new RenameTableContext(_localctx);
				enterOuterAlt(_localctx, 19);
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
				((RenameTableContext)_localctx).from = multipartIdentifier();
				setState(542);
				match(RENAME);
				setState(543);
				match(TO);
				setState(544);
				((RenameTableContext)_localctx).to = multipartIdentifier();
				}
				break;
			case 20:
				_localctx = new SetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(546);
				match(ALTER);
				setState(547);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(548);
				multipartIdentifier();
				setState(549);
				match(SET);
				setState(550);
				match(TBLPROPERTIES);
				setState(551);
				tablePropertyList();
				}
				break;
			case 21:
				_localctx = new UnsetTablePropertiesContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(553);
				match(ALTER);
				setState(554);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(555);
				multipartIdentifier();
				setState(556);
				match(UNSET);
				setState(557);
				match(TBLPROPERTIES);
				setState(560);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(558);
					match(IF);
					setState(559);
					match(EXISTS);
					}
				}

				setState(562);
				tablePropertyList();
				}
				break;
			case 22:
				_localctx = new AlterTableAlterColumnContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(564);
				match(ALTER);
				setState(565);
				match(TABLE);
				setState(566);
				((AlterTableAlterColumnContext)_localctx).table = multipartIdentifier();
				setState(567);
				_la = _input.LA(1);
				if ( !(_la==ALTER || _la==CHANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(569);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(568);
					match(COLUMN);
					}
					break;
				}
				setState(571);
				((AlterTableAlterColumnContext)_localctx).column = multipartIdentifier();
				setState(573);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==COMMENT || _la==DROP || _la==FIRST || _la==SET || _la==TYPE) {
					{
					setState(572);
					alterColumnAction();
					}
				}

				}
				break;
			case 23:
				_localctx = new HiveChangeColumnContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(575);
				match(ALTER);
				setState(576);
				match(TABLE);
				setState(577);
				((HiveChangeColumnContext)_localctx).table = multipartIdentifier();
				setState(579);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(578);
					partitionSpec();
					}
				}

				setState(581);
				match(CHANGE);
				setState(583);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
				case 1:
					{
					setState(582);
					match(COLUMN);
					}
					break;
				}
				setState(585);
				((HiveChangeColumnContext)_localctx).colName = multipartIdentifier();
				setState(586);
				colType();
				setState(588);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AFTER || _la==FIRST) {
					{
					setState(587);
					colPosition();
					}
				}

				}
				break;
			case 24:
				_localctx = new HiveReplaceColumnsContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(590);
				match(ALTER);
				setState(591);
				match(TABLE);
				setState(592);
				((HiveReplaceColumnsContext)_localctx).table = multipartIdentifier();
				setState(594);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(593);
					partitionSpec();
					}
				}

				setState(596);
				match(REPLACE);
				setState(597);
				match(COLUMNS);
				setState(598);
				match(T__0);
				setState(599);
				((HiveReplaceColumnsContext)_localctx).columns = qualifiedColTypeWithPositionList();
				setState(600);
				match(T__2);
				}
				break;
			case 25:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 25);
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
				match(SERDE);
				setState(610);
				match(STRING);
				setState(614);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(611);
					match(WITH);
					setState(612);
					match(SERDEPROPERTIES);
					setState(613);
					tablePropertyList();
					}
				}

				}
				break;
			case 26:
				_localctx = new SetTableSerDeContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(616);
				match(ALTER);
				setState(617);
				match(TABLE);
				setState(618);
				multipartIdentifier();
				setState(620);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(619);
					partitionSpec();
					}
				}

				setState(622);
				match(SET);
				setState(623);
				match(SERDEPROPERTIES);
				setState(624);
				tablePropertyList();
				}
				break;
			case 27:
				_localctx = new AddTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(626);
				match(ALTER);
				setState(627);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(628);
				multipartIdentifier();
				setState(629);
				match(ADD);
				setState(633);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(630);
					match(IF);
					setState(631);
					match(NOT);
					setState(632);
					match(EXISTS);
					}
				}

				setState(636); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(635);
					partitionSpecLocation();
					}
					}
					setState(638); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==PARTITION );
				}
				break;
			case 28:
				_localctx = new RenameTablePartitionContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(640);
				match(ALTER);
				setState(641);
				match(TABLE);
				setState(642);
				multipartIdentifier();
				setState(643);
				((RenameTablePartitionContext)_localctx).from = partitionSpec();
				setState(644);
				match(RENAME);
				setState(645);
				match(TO);
				setState(646);
				((RenameTablePartitionContext)_localctx).to = partitionSpec();
				}
				break;
			case 29:
				_localctx = new DropTablePartitionsContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(648);
				match(ALTER);
				setState(649);
				_la = _input.LA(1);
				if ( !(_la==TABLE || _la==VIEW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(650);
				multipartIdentifier();
				setState(651);
				match(DROP);
				setState(654);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(652);
					match(IF);
					setState(653);
					match(EXISTS);
					}
				}

				setState(656);
				partitionSpec();
				setState(661);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(657);
					match(T__1);
					setState(658);
					partitionSpec();
					}
					}
					setState(663);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(665);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PURGE) {
					{
					setState(664);
					match(PURGE);
					}
				}

				}
				break;
			case 30:
				_localctx = new SetTableLocationContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(667);
				match(ALTER);
				setState(668);
				match(TABLE);
				setState(669);
				multipartIdentifier();
				setState(671);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(670);
					partitionSpec();
					}
				}

				setState(673);
				match(SET);
				setState(674);
				locationSpec();
				}
				break;
			case 31:
				_localctx = new RecoverPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(676);
				match(ALTER);
				setState(677);
				match(TABLE);
				setState(678);
				multipartIdentifier();
				setState(679);
				match(RECOVER);
				setState(680);
				match(PARTITIONS);
				}
				break;
			case 32:
				_localctx = new DropTableContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(682);
				match(DROP);
				setState(683);
				match(TABLE);
				setState(686);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
				case 1:
					{
					setState(684);
					match(IF);
					setState(685);
					match(EXISTS);
					}
					break;
				}
				setState(688);
				multipartIdentifier();
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
			case 33:
				_localctx = new DropViewContext(_localctx);
				enterOuterAlt(_localctx, 33);
				{
				setState(692);
				match(DROP);
				setState(693);
				match(VIEW);
				setState(696);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
				case 1:
					{
					setState(694);
					match(IF);
					setState(695);
					match(EXISTS);
					}
					break;
				}
				setState(698);
				multipartIdentifier();
				}
				break;
			case 34:
				_localctx = new CreateViewContext(_localctx);
				enterOuterAlt(_localctx, 34);
				{
				setState(699);
				match(CREATE);
				setState(702);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(700);
					match(OR);
					setState(701);
					match(REPLACE);
					}
				}

				setState(708);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL || _la==TEMPORARY) {
					{
					setState(705);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==GLOBAL) {
						{
						setState(704);
						match(GLOBAL);
						}
					}

					setState(707);
					match(TEMPORARY);
					}
				}

				setState(710);
				match(VIEW);
				setState(714);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
				case 1:
					{
					setState(711);
					match(IF);
					setState(712);
					match(NOT);
					setState(713);
					match(EXISTS);
					}
					break;
				}
				setState(716);
				multipartIdentifier();
				setState(718);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(717);
					identifierCommentList();
					}
				}

				setState(728);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMENT || _la==PARTITIONED || _la==TBLPROPERTIES) {
					{
					setState(726);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COMMENT:
						{
						setState(720);
						commentSpec();
						}
						break;
					case PARTITIONED:
						{
						{
						setState(721);
						match(PARTITIONED);
						setState(722);
						match(ON);
						setState(723);
						identifierList();
						}
						}
						break;
					case TBLPROPERTIES:
						{
						{
						setState(724);
						match(TBLPROPERTIES);
						setState(725);
						tablePropertyList();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(730);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(731);
				match(AS);
				setState(732);
				query();
				}
				break;
			case 35:
				_localctx = new CreateTempViewUsingContext(_localctx);
				enterOuterAlt(_localctx, 35);
				{
				setState(734);
				match(CREATE);
				setState(737);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(735);
					match(OR);
					setState(736);
					match(REPLACE);
					}
				}

				setState(740);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GLOBAL) {
					{
					setState(739);
					match(GLOBAL);
					}
				}

				setState(742);
				match(TEMPORARY);
				setState(743);
				match(VIEW);
				setState(744);
				tableIdentifier();
				setState(749);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(745);
					match(T__0);
					setState(746);
					colTypeList();
					setState(747);
					match(T__2);
					}
				}

				setState(751);
				tableProvider();
				setState(754);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(752);
					match(OPTIONS);
					setState(753);
					tablePropertyList();
					}
				}

				}
				break;
			case 36:
				_localctx = new AlterViewQueryContext(_localctx);
				enterOuterAlt(_localctx, 36);
				{
				setState(756);
				match(ALTER);
				setState(757);
				match(VIEW);
				setState(758);
				multipartIdentifier();
				setState(760);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(759);
					match(AS);
					}
				}

				setState(762);
				query();
				}
				break;
			case 37:
				_localctx = new CreateFunctionContext(_localctx);
				enterOuterAlt(_localctx, 37);
				{
				setState(764);
				match(CREATE);
				setState(767);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(765);
					match(OR);
					setState(766);
					match(REPLACE);
					}
				}

				setState(770);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(769);
					match(TEMPORARY);
					}
				}

				setState(772);
				match(FUNCTION);
				setState(776);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
				case 1:
					{
					setState(773);
					match(IF);
					setState(774);
					match(NOT);
					setState(775);
					match(EXISTS);
					}
					break;
				}
				setState(778);
				multipartIdentifier();
				setState(779);
				match(AS);
				setState(780);
				((CreateFunctionContext)_localctx).className = match(STRING);
				setState(790);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(781);
					match(USING);
					setState(782);
					resource();
					setState(787);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(783);
						match(T__1);
						setState(784);
						resource();
						}
						}
						setState(789);
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
				setState(792);
				match(DROP);
				setState(794);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TEMPORARY) {
					{
					setState(793);
					match(TEMPORARY);
					}
				}

				setState(796);
				match(FUNCTION);
				setState(799);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
				case 1:
					{
					setState(797);
					match(IF);
					setState(798);
					match(EXISTS);
					}
					break;
				}
				setState(801);
				multipartIdentifier();
				}
				break;
			case 39:
				_localctx = new ExplainContext(_localctx);
				enterOuterAlt(_localctx, 39);
				{
				setState(802);
				match(EXPLAIN);
				setState(804);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CODEGEN || _la==COST || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & ((1L << (EXTENDED - 89)) | (1L << (FORMATTED - 89)) | (1L << (LOGICAL - 89)))) != 0)) {
					{
					setState(803);
					_la = _input.LA(1);
					if ( !(_la==CODEGEN || _la==COST || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & ((1L << (EXTENDED - 89)) | (1L << (FORMATTED - 89)) | (1L << (LOGICAL - 89)))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(806);
				statement();
				}
				break;
			case 40:
				_localctx = new ShowTablesContext(_localctx);
				enterOuterAlt(_localctx, 40);
				{
				setState(807);
				match(SHOW);
				setState(808);
				match(TABLES);
				setState(811);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(809);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(810);
					multipartIdentifier();
					}
				}

				setState(817);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(814);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(813);
						match(LIKE);
						}
					}

					setState(816);
					((ShowTablesContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 41:
				_localctx = new ShowTableExtendedContext(_localctx);
				enterOuterAlt(_localctx, 41);
				{
				setState(819);
				match(SHOW);
				setState(820);
				match(TABLE);
				setState(821);
				match(EXTENDED);
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(822);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(823);
					((ShowTableExtendedContext)_localctx).ns = multipartIdentifier();
					}
				}

				setState(826);
				match(LIKE);
				setState(827);
				((ShowTableExtendedContext)_localctx).pattern = match(STRING);
				setState(829);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(828);
					partitionSpec();
					}
				}

				}
				break;
			case 42:
				_localctx = new ShowTblPropertiesContext(_localctx);
				enterOuterAlt(_localctx, 42);
				{
				setState(831);
				match(SHOW);
				setState(832);
				match(TBLPROPERTIES);
				setState(833);
				((ShowTblPropertiesContext)_localctx).table = multipartIdentifier();
				setState(838);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(834);
					match(T__0);
					setState(835);
					((ShowTblPropertiesContext)_localctx).key = tablePropertyKey();
					setState(836);
					match(T__2);
					}
				}

				}
				break;
			case 43:
				_localctx = new ShowColumnsContext(_localctx);
				enterOuterAlt(_localctx, 43);
				{
				setState(840);
				match(SHOW);
				setState(841);
				match(COLUMNS);
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
				((ShowColumnsContext)_localctx).table = multipartIdentifier();
				setState(846);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(844);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(845);
					((ShowColumnsContext)_localctx).ns = multipartIdentifier();
					}
				}

				}
				break;
			case 44:
				_localctx = new ShowViewsContext(_localctx);
				enterOuterAlt(_localctx, 44);
				{
				setState(848);
				match(SHOW);
				setState(849);
				match(VIEWS);
				setState(852);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM || _la==IN) {
					{
					setState(850);
					_la = _input.LA(1);
					if ( !(_la==FROM || _la==IN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(851);
					multipartIdentifier();
					}
				}

				setState(858);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIKE || _la==STRING) {
					{
					setState(855);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LIKE) {
						{
						setState(854);
						match(LIKE);
						}
					}

					setState(857);
					((ShowViewsContext)_localctx).pattern = match(STRING);
					}
				}

				}
				break;
			case 45:
				_localctx = new ShowPartitionsContext(_localctx);
				enterOuterAlt(_localctx, 45);
				{
				setState(860);
				match(SHOW);
				setState(861);
				match(PARTITIONS);
				setState(862);
				multipartIdentifier();
				setState(864);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(863);
					partitionSpec();
					}
				}

				}
				break;
			case 46:
				_localctx = new ShowFunctionsContext(_localctx);
				enterOuterAlt(_localctx, 46);
				{
				setState(866);
				match(SHOW);
				setState(868);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
				case 1:
					{
					setState(867);
					identifier();
					}
					break;
				}
				setState(870);
				match(FUNCTIONS);
				setState(878);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
				case 1:
					{
					setState(872);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
					case 1:
						{
						setState(871);
						match(LIKE);
						}
						break;
					}
					setState(876);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
					case 1:
						{
						setState(874);
						multipartIdentifier();
						}
						break;
					case 2:
						{
						setState(875);
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
				setState(880);
				match(SHOW);
				setState(881);
				match(CREATE);
				setState(882);
				match(TABLE);
				setState(883);
				multipartIdentifier();
				setState(886);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(884);
					match(AS);
					setState(885);
					match(SERDE);
					}
				}

				}
				break;
			case 48:
				_localctx = new ShowCurrentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 48);
				{
				setState(888);
				match(SHOW);
				setState(889);
				match(CURRENT);
				setState(890);
				match(NAMESPACE);
				}
				break;
			case 49:
				_localctx = new DescribeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 49);
				{
				setState(891);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(892);
				match(FUNCTION);
				setState(894);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
				case 1:
					{
					setState(893);
					match(EXTENDED);
					}
					break;
				}
				setState(896);
				describeFuncName();
				}
				break;
			case 50:
				_localctx = new DescribeNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 50);
				{
				setState(897);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(898);
				namespace();
				setState(900);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(899);
					match(EXTENDED);
					}
					break;
				}
				setState(902);
				multipartIdentifier();
				}
				break;
			case 51:
				_localctx = new DescribeRelationContext(_localctx);
				enterOuterAlt(_localctx, 51);
				{
				setState(904);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(906);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(905);
					match(TABLE);
					}
					break;
				}
				setState(909);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
				case 1:
					{
					setState(908);
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
				setState(911);
				multipartIdentifier();
				setState(913);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
				case 1:
					{
					setState(912);
					partitionSpec();
					}
					break;
				}
				setState(916);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(915);
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
				setState(918);
				_la = _input.LA(1);
				if ( !(_la==DESC || _la==DESCRIBE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(920);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUERY) {
					{
					setState(919);
					match(QUERY);
					}
				}

				setState(922);
				query();
				}
				break;
			case 53:
				_localctx = new CommentNamespaceContext(_localctx);
				enterOuterAlt(_localctx, 53);
				{
				setState(923);
				match(COMMENT);
				setState(924);
				match(ON);
				setState(925);
				namespace();
				setState(926);
				multipartIdentifier();
				setState(927);
				match(IS);
				setState(928);
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
				setState(930);
				match(COMMENT);
				setState(931);
				match(ON);
				setState(932);
				match(TABLE);
				setState(933);
				multipartIdentifier();
				setState(934);
				match(IS);
				setState(935);
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
				setState(937);
				match(REFRESH);
				setState(938);
				match(TABLE);
				setState(939);
				multipartIdentifier();
				}
				break;
			case 56:
				_localctx = new RefreshFunctionContext(_localctx);
				enterOuterAlt(_localctx, 56);
				{
				setState(940);
				match(REFRESH);
				setState(941);
				match(FUNCTION);
				setState(942);
				multipartIdentifier();
				}
				break;
			case 57:
				_localctx = new RefreshResourceContext(_localctx);
				enterOuterAlt(_localctx, 57);
				{
				setState(943);
				match(REFRESH);
				setState(951);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
				case 1:
					{
					setState(944);
					match(STRING);
					}
					break;
				case 2:
					{
					setState(948);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,92,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(945);
							matchWildcard();
							}
							} 
						}
						setState(950);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,92,_ctx);
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
				setState(953);
				match(CACHE);
				setState(955);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LAZY) {
					{
					setState(954);
					match(LAZY);
					}
				}

				setState(957);
				match(TABLE);
				setState(958);
				multipartIdentifier();
				setState(961);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(959);
					match(OPTIONS);
					setState(960);
					((CacheTableContext)_localctx).options = tablePropertyList();
					}
				}

				setState(967);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0 || _la==AS || _la==FROM || _la==MAP || ((((_la - 190)) & ~0x3f) == 0 && ((1L << (_la - 190)) & ((1L << (REDUCE - 190)) | (1L << (SELECT - 190)) | (1L << (TABLE - 190)))) != 0) || _la==VALUES || _la==WITH) {
					{
					setState(964);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(963);
						match(AS);
						}
					}

					setState(966);
					query();
					}
				}

				}
				break;
			case 59:
				_localctx = new UncacheTableContext(_localctx);
				enterOuterAlt(_localctx, 59);
				{
				setState(969);
				match(UNCACHE);
				setState(970);
				match(TABLE);
				setState(973);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
				case 1:
					{
					setState(971);
					match(IF);
					setState(972);
					match(EXISTS);
					}
					break;
				}
				setState(975);
				multipartIdentifier();
				}
				break;
			case 60:
				_localctx = new ClearCacheContext(_localctx);
				enterOuterAlt(_localctx, 60);
				{
				setState(976);
				match(CLEAR);
				setState(977);
				match(CACHE);
				}
				break;
			case 61:
				_localctx = new LoadDataContext(_localctx);
				enterOuterAlt(_localctx, 61);
				{
				setState(978);
				match(LOAD);
				setState(979);
				match(DATA);
				setState(981);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(980);
					match(LOCAL);
					}
				}

				setState(983);
				match(INPATH);
				setState(984);
				((LoadDataContext)_localctx).path = match(STRING);
				setState(986);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OVERWRITE) {
					{
					setState(985);
					match(OVERWRITE);
					}
				}

				setState(988);
				match(INTO);
				setState(989);
				match(TABLE);
				setState(990);
				multipartIdentifier();
				setState(992);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(991);
					partitionSpec();
					}
				}

				}
				break;
			case 62:
				_localctx = new TruncateTableContext(_localctx);
				enterOuterAlt(_localctx, 62);
				{
				setState(994);
				match(TRUNCATE);
				setState(995);
				match(TABLE);
				setState(996);
				multipartIdentifier();
				setState(998);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(997);
					partitionSpec();
					}
				}

				}
				break;
			case 63:
				_localctx = new RepairTableContext(_localctx);
				enterOuterAlt(_localctx, 63);
				{
				setState(1000);
				match(MSCK);
				setState(1001);
				match(REPAIR);
				setState(1002);
				match(TABLE);
				setState(1003);
				multipartIdentifier();
				setState(1006);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD || _la==DROP || _la==SYNC) {
					{
					setState(1004);
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
					setState(1005);
					match(PARTITIONS);
					}
				}

				}
				break;
			case 64:
				_localctx = new ManageResourceContext(_localctx);
				enterOuterAlt(_localctx, 64);
				{
				setState(1008);
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
				setState(1009);
				identifier();
				setState(1013);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1010);
						matchWildcard();
						}
						} 
					}
					setState(1015);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
				}
				}
				break;
			case 65:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 65);
				{
				setState(1016);
				match(SET);
				setState(1017);
				match(ROLE);
				setState(1021);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
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
					_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
				}
				}
				break;
			case 66:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 66);
				{
				setState(1024);
				match(SET);
				setState(1025);
				match(TIME);
				setState(1026);
				match(ZONE);
				setState(1027);
				interval();
				}
				break;
			case 67:
				_localctx = new SetTimeZoneContext(_localctx);
				enterOuterAlt(_localctx, 67);
				{
				setState(1028);
				match(SET);
				setState(1029);
				match(TIME);
				setState(1030);
				match(ZONE);
				setState(1031);
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
				setState(1032);
				match(SET);
				setState(1033);
				match(TIME);
				setState(1034);
				match(ZONE);
				setState(1038);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1035);
						matchWildcard();
						}
						} 
					}
					setState(1040);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				}
				}
				break;
			case 69:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 69);
				{
				setState(1041);
				match(SET);
				setState(1042);
				configKey();
				setState(1043);
				match(EQ);
				setState(1044);
				configValue();
				}
				break;
			case 70:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 70);
				{
				setState(1046);
				match(SET);
				setState(1047);
				configKey();
				setState(1055);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1048);
					match(EQ);
					setState(1052);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
					while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1+1 ) {
							{
							{
							setState(1049);
							matchWildcard();
							}
							} 
						}
						setState(1054);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
					}
					}
				}

				}
				break;
			case 71:
				_localctx = new SetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 71);
				{
				setState(1057);
				match(SET);
				setState(1061);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1058);
						matchWildcard();
						}
						} 
					}
					setState(1063);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,109,_ctx);
				}
				setState(1064);
				match(EQ);
				setState(1065);
				configValue();
				}
				break;
			case 72:
				_localctx = new SetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 72);
				{
				setState(1066);
				match(SET);
				setState(1070);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1067);
						matchWildcard();
						}
						} 
					}
					setState(1072);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				}
				}
				break;
			case 73:
				_localctx = new ResetQuotedConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 73);
				{
				setState(1073);
				match(RESET);
				setState(1074);
				configKey();
				}
				break;
			case 74:
				_localctx = new ResetConfigurationContext(_localctx);
				enterOuterAlt(_localctx, 74);
				{
				setState(1075);
				match(RESET);
				setState(1079);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1076);
						matchWildcard();
						}
						} 
					}
					setState(1081);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,111,_ctx);
				}
				}
				break;
			case 75:
				_localctx = new FailNativeCommandContext(_localctx);
				enterOuterAlt(_localctx, 75);
				{
				setState(1082);
				unsupportedHiveNativeCommands();
				setState(1086);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(1083);
						matchWildcard();
						}
						} 
					}
					setState(1088);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
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
			setState(1091);
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
			setState(1093);
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
			setState(1263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1095);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1096);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1097);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1098);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1099);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(GRANT);
				setState(1101);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
				case 1:
					{
					setState(1100);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1103);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(REVOKE);
				setState(1105);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
				case 1:
					{
					setState(1104);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1107);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1108);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(GRANT);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1109);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1110);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLE);
				setState(1112);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
				case 1:
					{
					setState(1111);
					((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(GRANT);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1114);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1115);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(PRINCIPALS);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1116);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1117);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(ROLES);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1118);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1119);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CURRENT);
				setState(1120);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ROLES);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1121);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(EXPORT);
				setState(1122);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1123);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(IMPORT);
				setState(1124);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1125);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1126);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(COMPACTIONS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1127);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1128);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(CREATE);
				setState(1129);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TABLE);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1130);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1131);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTIONS);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1132);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1133);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEXES);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1134);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(SHOW);
				setState(1135);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(LOCKS);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1136);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1137);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1138);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1139);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1140);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1141);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(INDEX);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1142);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1143);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1144);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(LOCK);
				setState(1145);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1146);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1147);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1148);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(UNLOCK);
				setState(1149);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(DATABASE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1150);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(CREATE);
				setState(1151);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1152);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1153);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(DROP);
				setState(1154);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TEMPORARY);
				setState(1155);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(MACRO);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1156);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1157);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1158);
				tableIdentifier();
				setState(1159);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1160);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(CLUSTERED);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(1162);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1163);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1164);
				tableIdentifier();
				setState(1165);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CLUSTERED);
				setState(1166);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(1168);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1169);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1170);
				tableIdentifier();
				setState(1171);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1172);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SORTED);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(1174);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1175);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1176);
				tableIdentifier();
				setState(1177);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SKEWED);
				setState(1178);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(BY);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(1180);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1181);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1182);
				tableIdentifier();
				setState(1183);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1184);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(1186);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1187);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1188);
				tableIdentifier();
				setState(1189);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(NOT);
				setState(1190);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(STORED);
				setState(1191);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(AS);
				setState(1192);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw6 = match(DIRECTORIES);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(1194);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1195);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1196);
				tableIdentifier();
				setState(1197);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1198);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(SKEWED);
				setState(1199);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw5 = match(LOCATION);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(1201);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1202);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1203);
				tableIdentifier();
				setState(1204);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(EXCHANGE);
				setState(1205);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(1207);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1208);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1209);
				tableIdentifier();
				setState(1210);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(ARCHIVE);
				setState(1211);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(1213);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1214);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1215);
				tableIdentifier();
				setState(1216);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(UNARCHIVE);
				setState(1217);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(PARTITION);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(1219);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1220);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1221);
				tableIdentifier();
				setState(1222);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(TOUCH);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(1224);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1225);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1226);
				tableIdentifier();
				setState(1228);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1227);
					partitionSpec();
					}
				}

				setState(1230);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(COMPACT);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
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
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(CONCATENATE);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1240);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1241);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1242);
				tableIdentifier();
				setState(1244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1243);
					partitionSpec();
					}
				}

				setState(1246);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(SET);
				setState(1247);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(FILEFORMAT);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1249);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ALTER);
				setState(1250);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TABLE);
				setState(1251);
				tableIdentifier();
				setState(1253);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1252);
					partitionSpec();
					}
				}

				setState(1255);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw3 = match(REPLACE);
				setState(1256);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw4 = match(COLUMNS);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1258);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(START);
				setState(1259);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw2 = match(TRANSACTION);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1260);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(COMMIT);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1261);
				((UnsupportedHiveNativeCommandsContext)_localctx).kw1 = match(ROLLBACK);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1262);
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
			setState(1265);
			match(CREATE);
			setState(1267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(1266);
				match(TEMPORARY);
				}
			}

			setState(1270);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTERNAL) {
				{
				setState(1269);
				match(EXTERNAL);
				}
			}

			setState(1272);
			match(TABLE);
			setState(1276);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,124,_ctx) ) {
			case 1:
				{
				setState(1273);
				match(IF);
				setState(1274);
				match(NOT);
				setState(1275);
				match(EXISTS);
				}
				break;
			}
			setState(1278);
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
			setState(1282);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CREATE) {
				{
				setState(1280);
				match(CREATE);
				setState(1281);
				match(OR);
				}
			}

			setState(1284);
			match(REPLACE);
			setState(1285);
			match(TABLE);
			setState(1286);
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
			setState(1288);
			match(CLUSTERED);
			setState(1289);
			match(BY);
			setState(1290);
			identifierList();
			setState(1294);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SORTED) {
				{
				setState(1291);
				match(SORTED);
				setState(1292);
				match(BY);
				setState(1293);
				orderedIdentifierList();
				}
			}

			setState(1296);
			match(INTO);
			setState(1297);
			match(INTEGER_VALUE);
			setState(1298);
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
			setState(1300);
			match(SKEWED);
			setState(1301);
			match(BY);
			setState(1302);
			identifierList();
			setState(1303);
			match(ON);
			setState(1306);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
			case 1:
				{
				setState(1304);
				constantList();
				}
				break;
			case 2:
				{
				setState(1305);
				nestedConstantList();
				}
				break;
			}
			setState(1311);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				{
				setState(1308);
				match(STORED);
				setState(1309);
				match(AS);
				setState(1310);
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
			setState(1313);
			match(LOCATION);
			setState(1314);
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
			setState(1316);
			match(COMMENT);
			setState(1317);
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
			setState(1320);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(1319);
				ctes();
				}
			}

			setState(1322);
			queryTerm(0);
			setState(1323);
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
		public TablePropertyListContext options;
		public TerminalNode INSERT() { return getToken(ArcticExtendSparkSqlParser.INSERT, 0); }
		public TerminalNode OVERWRITE() { return getToken(ArcticExtendSparkSqlParser.OVERWRITE, 0); }
		public TerminalNode DIRECTORY() { return getToken(ArcticExtendSparkSqlParser.DIRECTORY, 0); }
		public TableProviderContext tableProvider() {
			return getRuleContext(TableProviderContext.class,0);
		}
		public TerminalNode LOCAL() { return getToken(ArcticExtendSparkSqlParser.LOCAL, 0); }
		public TerminalNode OPTIONS() { return getToken(ArcticExtendSparkSqlParser.OPTIONS, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
			setState(1386);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
			case 1:
				_localctx = new InsertOverwriteTableContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1325);
				match(INSERT);
				setState(1326);
				match(OVERWRITE);
				setState(1328);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
				case 1:
					{
					setState(1327);
					match(TABLE);
					}
					break;
				}
				setState(1330);
				multipartIdentifier();
				setState(1337);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1331);
					partitionSpec();
					setState(1335);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IF) {
						{
						setState(1332);
						match(IF);
						setState(1333);
						match(NOT);
						setState(1334);
						match(EXISTS);
						}
					}

					}
				}

				setState(1340);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
				case 1:
					{
					setState(1339);
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
				setState(1342);
				match(INSERT);
				setState(1343);
				match(INTO);
				setState(1345);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
				case 1:
					{
					setState(1344);
					match(TABLE);
					}
					break;
				}
				setState(1347);
				multipartIdentifier();
				setState(1349);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1348);
					partitionSpec();
					}
				}

				setState(1354);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(1351);
					match(IF);
					setState(1352);
					match(NOT);
					setState(1353);
					match(EXISTS);
					}
				}

				setState(1357);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
				case 1:
					{
					setState(1356);
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
				setState(1359);
				match(INSERT);
				setState(1360);
				match(OVERWRITE);
				setState(1362);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1361);
					match(LOCAL);
					}
				}

				setState(1364);
				match(DIRECTORY);
				setState(1365);
				((InsertOverwriteHiveDirContext)_localctx).path = match(STRING);
				setState(1367);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(1366);
					rowFormat();
					}
				}

				setState(1370);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORED) {
					{
					setState(1369);
					createFileFormat();
					}
				}

				}
				break;
			case 4:
				_localctx = new InsertOverwriteDirContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1372);
				match(INSERT);
				setState(1373);
				match(OVERWRITE);
				setState(1375);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOCAL) {
					{
					setState(1374);
					match(LOCAL);
					}
				}

				setState(1377);
				match(DIRECTORY);
				setState(1379);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING) {
					{
					setState(1378);
					((InsertOverwriteDirContext)_localctx).path = match(STRING);
					}
				}

				setState(1381);
				tableProvider();
				setState(1384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OPTIONS) {
					{
					setState(1382);
					match(OPTIONS);
					setState(1383);
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
			setState(1388);
			partitionSpec();
			setState(1390);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOCATION) {
				{
				setState(1389);
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
			setState(1392);
			match(PARTITION);
			setState(1393);
			match(T__0);
			setState(1394);
			partitionVal();
			setState(1399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(1395);
				match(T__1);
				setState(1396);
				partitionVal();
				}
				}
				setState(1401);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1402);
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
			setState(1404);
			identifier();
			setState(1407);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1405);
				match(EQ);
				setState(1406);
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
			setState(1409);
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
		enterRule(_localctx, 56, RULE_describeFuncName);
		try {
			setState(1416);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1411);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1412);
				match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1413);
				comparisonOperator();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1414);
				arithmeticOperator();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1415);
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
		enterRule(_localctx, 58, RULE_describeColName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1418);
			((DescribeColNameContext)_localctx).identifier = identifier();
			((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
			setState(1423);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__4) {
				{
				{
				setState(1419);
				match(T__4);
				setState(1420);
				((DescribeColNameContext)_localctx).identifier = identifier();
				((DescribeColNameContext)_localctx).nameParts.add(((DescribeColNameContext)_localctx).identifier);
				}
				}
				setState(1425);
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
		enterRule(_localctx, 60, RULE_ctes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1426);
			match(WITH);
			setState(1427);
			namedQuery();
			setState(1432);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(1428);
				match(T__1);
				setState(1429);
				namedQuery();
				}
				}
				setState(1434);
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
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
		enterRule(_localctx, 62, RULE_namedQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1435);
			((NamedQueryContext)_localctx).name = errorCapturingIdentifier();
			setState(1437);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				{
				setState(1436);
				((NamedQueryContext)_localctx).columnAliases = identifierList();
				}
				break;
			}
			setState(1440);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(1439);
				match(AS);
				}
			}

			setState(1442);
			match(T__0);
			setState(1443);
			query();
			setState(1444);
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
		enterRule(_localctx, 64, RULE_tableProvider);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1446);
			match(USING);
			setState(1447);
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
		enterRule(_localctx, 66, RULE_createTableClauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1464);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CLUSTERED || _la==COMMENT || ((((_la - 141)) & ~0x3f) == 0 && ((1L << (_la - 141)) & ((1L << (LOCATION - 141)) | (1L << (OPTIONS - 141)) | (1L << (PARTITIONED - 141)))) != 0) || ((((_la - 206)) & ~0x3f) == 0 && ((1L << (_la - 206)) & ((1L << (ROW - 206)) | (1L << (SKEWED - 206)) | (1L << (STORED - 206)) | (1L << (TBLPROPERTIES - 206)))) != 0)) {
				{
				setState(1462);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case OPTIONS:
					{
					{
					setState(1449);
					match(OPTIONS);
					setState(1450);
					((CreateTableClausesContext)_localctx).options = tablePropertyList();
					}
					}
					break;
				case PARTITIONED:
					{
					{
					setState(1451);
					match(PARTITIONED);
					setState(1452);
					match(BY);
					setState(1453);
					((CreateTableClausesContext)_localctx).partitioning = partitionFieldList();
					}
					}
					break;
				case SKEWED:
					{
					setState(1454);
					skewSpec();
					}
					break;
				case CLUSTERED:
					{
					setState(1455);
					bucketSpec();
					}
					break;
				case ROW:
					{
					setState(1456);
					rowFormat();
					}
					break;
				case STORED:
					{
					setState(1457);
					createFileFormat();
					}
					break;
				case LOCATION:
					{
					setState(1458);
					locationSpec();
					}
					break;
				case COMMENT:
					{
					setState(1459);
					commentSpec();
					}
					break;
				case TBLPROPERTIES:
					{
					{
					setState(1460);
					match(TBLPROPERTIES);
					setState(1461);
					((CreateTableClausesContext)_localctx).tableProps = tablePropertyList();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(1466);
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
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTablePropertyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTablePropertyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTablePropertyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyListContext tablePropertyList() throws RecognitionException {
		TablePropertyListContext _localctx = new TablePropertyListContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_tablePropertyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1467);
			match(T__0);
			setState(1468);
			tableProperty();
			setState(1473);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(1469);
				match(T__1);
				setState(1470);
				tableProperty();
				}
				}
				setState(1475);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1476);
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

	public static class TablePropertyContext extends ParserRuleContext {
		public TablePropertyKeyContext key;
		public TablePropertyValueContext value;
		public TablePropertyKeyContext tablePropertyKey() {
			return getRuleContext(TablePropertyKeyContext.class,0);
		}
		public TablePropertyValueContext tablePropertyValue() {
			return getRuleContext(TablePropertyValueContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ArcticExtendSparkSqlParser.EQ, 0); }
		public TablePropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTableProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTableProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTableProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyContext tableProperty() throws RecognitionException {
		TablePropertyContext _localctx = new TablePropertyContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_tableProperty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1478);
			((TablePropertyContext)_localctx).key = tablePropertyKey();
			setState(1483);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FALSE || ((((_la - 247)) & ~0x3f) == 0 && ((1L << (_la - 247)) & ((1L << (TRUE - 247)) | (1L << (EQ - 247)) | (1L << (STRING - 247)) | (1L << (INTEGER_VALUE - 247)) | (1L << (DECIMAL_VALUE - 247)))) != 0)) {
				{
				setState(1480);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ) {
					{
					setState(1479);
					match(EQ);
					}
				}

				setState(1482);
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

	public static class TablePropertyKeyContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TablePropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablePropertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTablePropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTablePropertyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTablePropertyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyKeyContext tablePropertyKey() throws RecognitionException {
		TablePropertyKeyContext _localctx = new TablePropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_tablePropertyKey);
		int _la;
		try {
			setState(1494);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1485);
				identifier();
				setState(1490);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__4) {
					{
					{
					setState(1486);
					match(T__4);
					setState(1487);
					identifier();
					}
					}
					setState(1492);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1493);
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

	public static class TablePropertyValueContext extends ParserRuleContext {
		public TerminalNode INTEGER_VALUE() { return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, 0); }
		public TerminalNode DECIMAL_VALUE() { return getToken(ArcticExtendSparkSqlParser.DECIMAL_VALUE, 0); }
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TablePropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablePropertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).enterTablePropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArcticExtendSparkSqlListener ) ((ArcticExtendSparkSqlListener)listener).exitTablePropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArcticExtendSparkSqlVisitor ) return ((ArcticExtendSparkSqlVisitor<? extends T>)visitor).visitTablePropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePropertyValueContext tablePropertyValue() throws RecognitionException {
		TablePropertyValueContext _localctx = new TablePropertyValueContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_tablePropertyValue);
		try {
			setState(1500);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_VALUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1496);
				match(INTEGER_VALUE);
				}
				break;
			case DECIMAL_VALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1497);
				match(DECIMAL_VALUE);
				}
				break;
			case FALSE:
			case TRUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1498);
				booleanValue();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(1499);
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
		enterRule(_localctx, 76, RULE_constantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1502);
			match(T__0);
			setState(1503);
			constant();
			setState(1508);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(1504);
				match(T__1);
				setState(1505);
				constant();
				}
				}
				setState(1510);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1511);
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
		enterRule(_localctx, 78, RULE_nestedConstantList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1513);
			match(T__0);
			setState(1514);
			constantList();
			setState(1519);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(1515);
				match(T__1);
				setState(1516);
				constantList();
				}
				}
				setState(1521);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1522);
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
		enterRule(_localctx, 80, RULE_createFileFormat);
		try {
			setState(1530);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1524);
				match(STORED);
				setState(1525);
				match(AS);
				setState(1526);
				fileFormat();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1527);
				match(STORED);
				setState(1528);
				match(BY);
				setState(1529);
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
		enterRule(_localctx, 82, RULE_fileFormat);
		try {
			setState(1537);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,164,_ctx) ) {
			case 1:
				_localctx = new TableFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1532);
				match(INPUTFORMAT);
				setState(1533);
				((TableFileFormatContext)_localctx).inFmt = match(STRING);
				setState(1534);
				match(OUTPUTFORMAT);
				setState(1535);
				((TableFileFormatContext)_localctx).outFmt = match(STRING);
				}
				break;
			case 2:
				_localctx = new GenericFileFormatContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1536);
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
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		enterRule(_localctx, 84, RULE_storageHandler);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1539);
			match(STRING);
			setState(1543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
			case 1:
				{
				setState(1540);
				match(WITH);
				setState(1541);
				match(SERDEPROPERTIES);
				setState(1542);
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
		enterRule(_localctx, 86, RULE_resource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1545);
			identifier();
			setState(1546);
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
		public QueryTermContext queryTerm() {
			return getRuleContext(QueryTermContext.class,0);
		}
		public QueryOrganizationContext queryOrganization() {
			return getRuleContext(QueryOrganizationContext.class,0);
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
		enterRule(_localctx, 88, RULE_dmlStatementNoWith);
		int _la;
		try {
			int _alt;
			setState(1599);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INSERT:
				_localctx = new SingleInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1548);
				insertInto();
				setState(1549);
				queryTerm(0);
				setState(1550);
				queryOrganization();
				}
				break;
			case FROM:
				_localctx = new MultiInsertQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1552);
				fromClause();
				setState(1554); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1553);
					multiInsertQueryBody();
					}
					}
					setState(1556); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==INSERT );
				}
				break;
			case DELETE:
				_localctx = new DeleteFromTableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1558);
				match(DELETE);
				setState(1559);
				match(FROM);
				setState(1560);
				multipartIdentifier();
				setState(1561);
				tableAlias();
				setState(1563);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1562);
					whereClause();
					}
				}

				}
				break;
			case UPDATE:
				_localctx = new UpdateTableContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1565);
				match(UPDATE);
				setState(1566);
				multipartIdentifier();
				setState(1567);
				tableAlias();
				setState(1568);
				setClause();
				setState(1570);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(1569);
					whereClause();
					}
				}

				}
				break;
			case MERGE:
				_localctx = new MergeIntoTableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1572);
				match(MERGE);
				setState(1573);
				match(INTO);
				setState(1574);
				((MergeIntoTableContext)_localctx).target = multipartIdentifier();
				setState(1575);
				((MergeIntoTableContext)_localctx).targetAlias = tableAlias();
				setState(1576);
				match(USING);
				setState(1582);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
				case 1:
					{
					setState(1577);
					((MergeIntoTableContext)_localctx).source = multipartIdentifier();
					}
					break;
				case 2:
					{
					setState(1578);
					match(T__0);
					setState(1579);
					((MergeIntoTableContext)_localctx).sourceQuery = query();
					setState(1580);
					match(T__2);
					}
					break;
				}
				setState(1584);
				((MergeIntoTableContext)_localctx).sourceAlias = tableAlias();
				setState(1585);
				match(ON);
				setState(1586);
				((MergeIntoTableContext)_localctx).mergeCondition = booleanExpression(0);
				setState(1590);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1587);
						matchedClause();
						}
						} 
					}
					setState(1592);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				}
				setState(1596);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(1593);
					notMatchedClause();
					}
					}
					setState(1598);
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
		enterRule(_localctx, 90, RULE_queryOrganization);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1611);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				setState(1601);
				match(ORDER);
				setState(1602);
				match(BY);
				setState(1603);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1608);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,173,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1604);
						match(T__1);
						setState(1605);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).order.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1610);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,173,_ctx);
				}
				}
				break;
			}
			setState(1623);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				{
				setState(1613);
				match(CLUSTER);
				setState(1614);
				match(BY);
				setState(1615);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1620);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,175,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1616);
						match(T__1);
						setState(1617);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).clusterBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1622);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,175,_ctx);
				}
				}
				break;
			}
			setState(1635);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				setState(1625);
				match(DISTRIBUTE);
				setState(1626);
				match(BY);
				setState(1627);
				((QueryOrganizationContext)_localctx).expression = expression();
				((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
				setState(1632);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,177,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1628);
						match(T__1);
						setState(1629);
						((QueryOrganizationContext)_localctx).expression = expression();
						((QueryOrganizationContext)_localctx).distributeBy.add(((QueryOrganizationContext)_localctx).expression);
						}
						} 
					}
					setState(1634);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,177,_ctx);
				}
				}
				break;
			}
			setState(1647);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				{
				setState(1637);
				match(SORT);
				setState(1638);
				match(BY);
				setState(1639);
				((QueryOrganizationContext)_localctx).sortItem = sortItem();
				((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
				setState(1644);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1640);
						match(T__1);
						setState(1641);
						((QueryOrganizationContext)_localctx).sortItem = sortItem();
						((QueryOrganizationContext)_localctx).sort.add(((QueryOrganizationContext)_localctx).sortItem);
						}
						} 
					}
					setState(1646);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
				}
				}
				break;
			}
			setState(1650);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				{
				setState(1649);
				windowClause();
				}
				break;
			}
			setState(1657);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				{
				setState(1652);
				match(LIMIT);
				setState(1655);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
				case 1:
					{
					setState(1653);
					match(ALL);
					}
					break;
				case 2:
					{
					setState(1654);
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
		enterRule(_localctx, 92, RULE_multiInsertQueryBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1659);
			insertInto();
			setState(1660);
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
		int _startState = 94;
		enterRecursionRule(_localctx, 94, RULE_queryTerm, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new QueryTermDefaultContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(1663);
			queryPrimary();
			}
			_ctx.stop = _input.LT(-1);
			setState(1688);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1686);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
					case 1:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1665);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1666);
						if (!(legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "legacy_setops_precedence_enabled");
						setState(1667);
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
						setState(1669);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1668);
							setQuantifier();
							}
						}

						setState(1671);
						((SetOperationContext)_localctx).right = queryTerm(4);
						}
						break;
					case 2:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1672);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1673);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1674);
						((SetOperationContext)_localctx).operator = match(INTERSECT);
						setState(1676);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1675);
							setQuantifier();
							}
						}

						setState(1678);
						((SetOperationContext)_localctx).right = queryTerm(3);
						}
						break;
					case 3:
						{
						_localctx = new SetOperationContext(new QueryTermContext(_parentctx, _parentState));
						((SetOperationContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_queryTerm);
						setState(1679);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(1680);
						if (!(!legacy_setops_precedence_enabled)) throw new FailedPredicateException(this, "!legacy_setops_precedence_enabled");
						setState(1681);
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
						setState(1683);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==DISTINCT) {
							{
							setState(1682);
							setQuantifier();
							}
						}

						setState(1685);
						((SetOperationContext)_localctx).right = queryTerm(2);
						}
						break;
					}
					} 
				}
				setState(1690);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
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
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
		enterRule(_localctx, 96, RULE_queryPrimary);
		try {
			setState(1700);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAP:
			case REDUCE:
			case SELECT:
				_localctx = new QueryPrimaryDefaultContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1691);
				querySpecification();
				}
				break;
			case FROM:
				_localctx = new FromStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1692);
				fromStatement();
				}
				break;
			case TABLE:
				_localctx = new TableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1693);
				match(TABLE);
				setState(1694);
				multipartIdentifier();
				}
				break;
			case VALUES:
				_localctx = new InlineTableDefault1Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1695);
				inlineTable();
				}
				break;
			case T__0:
				_localctx = new SubqueryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1696);
				match(T__0);
				setState(1697);
				query();
				setState(1698);
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
		enterRule(_localctx, 98, RULE_sortItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1702);
			expression();
			setState(1704);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
			case 1:
				{
				setState(1703);
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
			setState(1708);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,191,_ctx) ) {
			case 1:
				{
				setState(1706);
				match(NULLS);
				setState(1707);
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
		enterRule(_localctx, 100, RULE_fromStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1710);
			fromClause();
			setState(1712); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1711);
					fromStatementBody();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1714); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,192,_ctx);
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
		enterRule(_localctx, 102, RULE_fromStatementBody);
		try {
			int _alt;
			setState(1743);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,199,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1716);
				transformClause();
				setState(1718);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
				case 1:
					{
					setState(1717);
					whereClause();
					}
					break;
				}
				setState(1720);
				queryOrganization();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1722);
				selectClause();
				setState(1726);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,194,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1723);
						lateralView();
						}
						} 
					}
					setState(1728);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,194,_ctx);
				}
				setState(1730);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
				case 1:
					{
					setState(1729);
					whereClause();
					}
					break;
				}
				setState(1733);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,196,_ctx) ) {
				case 1:
					{
					setState(1732);
					aggregationClause();
					}
					break;
				}
				setState(1736);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
				case 1:
					{
					setState(1735);
					havingClause();
					}
					break;
				}
				setState(1739);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,198,_ctx) ) {
				case 1:
					{
					setState(1738);
					windowClause();
					}
					break;
				}
				setState(1741);
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
		enterRule(_localctx, 104, RULE_querySpecification);
		try {
			int _alt;
			setState(1789);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
			case 1:
				_localctx = new TransformQuerySpecificationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1745);
				transformClause();
				setState(1747);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
				case 1:
					{
					setState(1746);
					fromClause();
					}
					break;
				}
				setState(1752);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,201,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1749);
						lateralView();
						}
						} 
					}
					setState(1754);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,201,_ctx);
				}
				setState(1756);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
				case 1:
					{
					setState(1755);
					whereClause();
					}
					break;
				}
				setState(1759);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
				case 1:
					{
					setState(1758);
					aggregationClause();
					}
					break;
				}
				setState(1762);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
				case 1:
					{
					setState(1761);
					havingClause();
					}
					break;
				}
				setState(1765);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
				case 1:
					{
					setState(1764);
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
				setState(1767);
				selectClause();
				setState(1769);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
				case 1:
					{
					setState(1768);
					fromClause();
					}
					break;
				}
				setState(1774);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,207,_ctx);
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
					_alt = getInterpreter().adaptivePredict(_input,207,_ctx);
				}
				setState(1778);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
				case 1:
					{
					setState(1777);
					whereClause();
					}
					break;
				}
				setState(1781);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
				case 1:
					{
					setState(1780);
					aggregationClause();
					}
					break;
				}
				setState(1784);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
				case 1:
					{
					setState(1783);
					havingClause();
					}
					break;
				}
				setState(1787);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
				case 1:
					{
					setState(1786);
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
		public ExpressionSeqContext expressionSeq() {
			return getRuleContext(ExpressionSeqContext.class,0);
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
		enterRule(_localctx, 106, RULE_transformClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1810);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(1791);
				match(SELECT);
				setState(1792);
				((TransformClauseContext)_localctx).kind = match(TRANSFORM);
				setState(1793);
				match(T__0);
				setState(1795);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
				case 1:
					{
					setState(1794);
					setQuantifier();
					}
					break;
				}
				setState(1797);
				expressionSeq();
				setState(1798);
				match(T__2);
				}
				break;
			case MAP:
				{
				setState(1800);
				((TransformClauseContext)_localctx).kind = match(MAP);
				setState(1802);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,214,_ctx) ) {
				case 1:
					{
					setState(1801);
					setQuantifier();
					}
					break;
				}
				setState(1804);
				expressionSeq();
				}
				break;
			case REDUCE:
				{
				setState(1805);
				((TransformClauseContext)_localctx).kind = match(REDUCE);
				setState(1807);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,215,_ctx) ) {
				case 1:
					{
					setState(1806);
					setQuantifier();
					}
					break;
				}
				setState(1809);
				expressionSeq();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1813);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROW) {
				{
				setState(1812);
				((TransformClauseContext)_localctx).inRowFormat = rowFormat();
				}
			}

			setState(1817);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECORDWRITER) {
				{
				setState(1815);
				match(RECORDWRITER);
				setState(1816);
				((TransformClauseContext)_localctx).recordWriter = match(STRING);
				}
			}

			setState(1819);
			match(USING);
			setState(1820);
			((TransformClauseContext)_localctx).script = match(STRING);
			setState(1833);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,221,_ctx) ) {
			case 1:
				{
				setState(1821);
				match(AS);
				setState(1831);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
				case 1:
					{
					setState(1822);
					identifierSeq();
					}
					break;
				case 2:
					{
					setState(1823);
					colTypeList();
					}
					break;
				case 3:
					{
					{
					setState(1824);
					match(T__0);
					setState(1827);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,219,_ctx) ) {
					case 1:
						{
						setState(1825);
						identifierSeq();
						}
						break;
					case 2:
						{
						setState(1826);
						colTypeList();
						}
						break;
					}
					setState(1829);
					match(T__2);
					}
					}
					break;
				}
				}
				break;
			}
			setState(1836);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
			case 1:
				{
				setState(1835);
				((TransformClauseContext)_localctx).outRowFormat = rowFormat();
				}
				break;
			}
			setState(1840);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,223,_ctx) ) {
			case 1:
				{
				setState(1838);
				match(RECORDREADER);
				setState(1839);
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
		enterRule(_localctx, 108, RULE_selectClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1842);
			match(SELECT);
			setState(1846);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,224,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1843);
					((SelectClauseContext)_localctx).hint = hint();
					((SelectClauseContext)_localctx).hints.add(((SelectClauseContext)_localctx).hint);
					}
					} 
				}
				setState(1848);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,224,_ctx);
			}
			setState(1850);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,225,_ctx) ) {
			case 1:
				{
				setState(1849);
				setQuantifier();
				}
				break;
			}
			setState(1852);
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
		enterRule(_localctx, 110, RULE_setClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1854);
			match(SET);
			setState(1855);
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
		enterRule(_localctx, 112, RULE_matchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1857);
			match(WHEN);
			setState(1858);
			match(MATCHED);
			setState(1861);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1859);
				match(AND);
				setState(1860);
				((MatchedClauseContext)_localctx).matchedCond = booleanExpression(0);
				}
			}

			setState(1863);
			match(THEN);
			setState(1864);
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
		enterRule(_localctx, 114, RULE_notMatchedClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1866);
			match(WHEN);
			setState(1867);
			match(NOT);
			setState(1868);
			match(MATCHED);
			setState(1871);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(1869);
				match(AND);
				setState(1870);
				((NotMatchedClauseContext)_localctx).notMatchedCond = booleanExpression(0);
				}
			}

			setState(1873);
			match(THEN);
			setState(1874);
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
		enterRule(_localctx, 116, RULE_matchedAction);
		try {
			setState(1883);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1876);
				match(DELETE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1877);
				match(UPDATE);
				setState(1878);
				match(SET);
				setState(1879);
				match(ASTERISK);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1880);
				match(UPDATE);
				setState(1881);
				match(SET);
				setState(1882);
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
		enterRule(_localctx, 118, RULE_notMatchedAction);
		int _la;
		try {
			setState(1903);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1885);
				match(INSERT);
				setState(1886);
				match(ASTERISK);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1887);
				match(INSERT);
				setState(1888);
				match(T__0);
				setState(1889);
				((NotMatchedActionContext)_localctx).columns = multipartIdentifierList();
				setState(1890);
				match(T__2);
				setState(1891);
				match(VALUES);
				setState(1892);
				match(T__0);
				setState(1893);
				expression();
				setState(1898);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(1894);
					match(T__1);
					setState(1895);
					expression();
					}
					}
					setState(1900);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1901);
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
		enterRule(_localctx, 120, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1905);
			assignment();
			setState(1910);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(1906);
				match(T__1);
				setState(1907);
				assignment();
				}
				}
				setState(1912);
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
		enterRule(_localctx, 122, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1913);
			((AssignmentContext)_localctx).key = multipartIdentifier();
			setState(1914);
			match(EQ);
			setState(1915);
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
		enterRule(_localctx, 124, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1917);
			match(WHERE);
			setState(1918);
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
		enterRule(_localctx, 126, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1920);
			match(HAVING);
			setState(1921);
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
		enterRule(_localctx, 128, RULE_hint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1923);
			match(T__5);
			setState(1924);
			((HintContext)_localctx).hintStatement = hintStatement();
			((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
			setState(1931);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1926);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
					case 1:
						{
						setState(1925);
						match(T__1);
						}
						break;
					}
					setState(1928);
					((HintContext)_localctx).hintStatement = hintStatement();
					((HintContext)_localctx).hintStatements.add(((HintContext)_localctx).hintStatement);
					}
					} 
				}
				setState(1933);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			}
			setState(1934);
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
		enterRule(_localctx, 130, RULE_hintStatement);
		int _la;
		try {
			setState(1949);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1936);
				((HintStatementContext)_localctx).hintName = identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1937);
				((HintStatementContext)_localctx).hintName = identifier();
				setState(1938);
				match(T__0);
				setState(1939);
				((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
				((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
				setState(1944);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(1940);
					match(T__1);
					setState(1941);
					((HintStatementContext)_localctx).primaryExpression = primaryExpression(0);
					((HintStatementContext)_localctx).parameters.add(((HintStatementContext)_localctx).primaryExpression);
					}
					}
					setState(1946);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1947);
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

	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
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
		enterRule(_localctx, 132, RULE_fromClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1951);
			match(FROM);
			setState(1952);
			relation();
			setState(1957);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,236,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1953);
					match(T__1);
					setState(1954);
					relation();
					}
					} 
				}
				setState(1959);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,236,_ctx);
			}
			setState(1963);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,237,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1960);
					lateralView();
					}
					} 
				}
				setState(1965);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,237,_ctx);
			}
			setState(1967);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,238,_ctx) ) {
			case 1:
				{
				setState(1966);
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
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode SETS() { return getToken(ArcticExtendSparkSqlParser.SETS, 0); }
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
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
		enterRule(_localctx, 134, RULE_aggregationClause);
		int _la;
		try {
			int _alt;
			setState(2008);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1969);
				match(GROUP);
				setState(1970);
				match(BY);
				setState(1971);
				((AggregationClauseContext)_localctx).groupByClause = groupByClause();
				((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
				setState(1976);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,239,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1972);
						match(T__1);
						setState(1973);
						((AggregationClauseContext)_localctx).groupByClause = groupByClause();
						((AggregationClauseContext)_localctx).groupingExpressionsWithGroupingAnalytics.add(((AggregationClauseContext)_localctx).groupByClause);
						}
						} 
					}
					setState(1978);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,239,_ctx);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1979);
				match(GROUP);
				setState(1980);
				match(BY);
				setState(1981);
				((AggregationClauseContext)_localctx).expression = expression();
				((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
				setState(1986);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1982);
						match(T__1);
						setState(1983);
						((AggregationClauseContext)_localctx).expression = expression();
						((AggregationClauseContext)_localctx).groupingExpressions.add(((AggregationClauseContext)_localctx).expression);
						}
						} 
					}
					setState(1988);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
				}
				setState(2006);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,242,_ctx) ) {
				case 1:
					{
					setState(1989);
					match(WITH);
					setState(1990);
					((AggregationClauseContext)_localctx).kind = match(ROLLUP);
					}
					break;
				case 2:
					{
					setState(1991);
					match(WITH);
					setState(1992);
					((AggregationClauseContext)_localctx).kind = match(CUBE);
					}
					break;
				case 3:
					{
					setState(1993);
					((AggregationClauseContext)_localctx).kind = match(GROUPING);
					setState(1994);
					match(SETS);
					setState(1995);
					match(T__0);
					setState(1996);
					groupingSet();
					setState(2001);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(1997);
						match(T__1);
						setState(1998);
						groupingSet();
						}
						}
						setState(2003);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2004);
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
		enterRule(_localctx, 136, RULE_groupByClause);
		try {
			setState(2012);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,244,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2010);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2011);
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
		public List<GroupingSetContext> groupingSet() {
			return getRuleContexts(GroupingSetContext.class);
		}
		public GroupingSetContext groupingSet(int i) {
			return getRuleContext(GroupingSetContext.class,i);
		}
		public TerminalNode ROLLUP() { return getToken(ArcticExtendSparkSqlParser.ROLLUP, 0); }
		public TerminalNode CUBE() { return getToken(ArcticExtendSparkSqlParser.CUBE, 0); }
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
		enterRule(_localctx, 138, RULE_groupingAnalytics);
		int _la;
		try {
			setState(2039);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CUBE:
			case ROLLUP:
				enterOuterAlt(_localctx, 1);
				{
				setState(2014);
				_la = _input.LA(1);
				if ( !(_la==CUBE || _la==ROLLUP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2015);
				match(T__0);
				setState(2016);
				groupingSet();
				setState(2021);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2017);
					match(T__1);
					setState(2018);
					groupingSet();
					}
					}
					setState(2023);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2024);
				match(T__2);
				}
				break;
			case GROUPING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2026);
				match(GROUPING);
				setState(2027);
				match(SETS);
				setState(2028);
				match(T__0);
				setState(2029);
				groupingElement();
				setState(2034);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2030);
					match(T__1);
					setState(2031);
					groupingElement();
					}
					}
					setState(2036);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2037);
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
		enterRule(_localctx, 140, RULE_groupingElement);
		try {
			setState(2043);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,248,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2041);
				groupingAnalytics();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2042);
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
		enterRule(_localctx, 142, RULE_groupingSet);
		int _la;
		try {
			setState(2058);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,251,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2045);
				match(T__0);
				setState(2054);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,250,_ctx) ) {
				case 1:
					{
					setState(2046);
					expression();
					setState(2051);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(2047);
						match(T__1);
						setState(2048);
						expression();
						}
						}
						setState(2053);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2056);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2057);
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
		public TerminalNode FOR() { return getToken(ArcticExtendSparkSqlParser.FOR, 0); }
		public PivotColumnContext pivotColumn() {
			return getRuleContext(PivotColumnContext.class,0);
		}
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
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
		enterRule(_localctx, 144, RULE_pivotClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2060);
			match(PIVOT);
			setState(2061);
			match(T__0);
			setState(2062);
			((PivotClauseContext)_localctx).aggregates = namedExpressionSeq();
			setState(2063);
			match(FOR);
			setState(2064);
			pivotColumn();
			setState(2065);
			match(IN);
			setState(2066);
			match(T__0);
			setState(2067);
			((PivotClauseContext)_localctx).pivotValue = pivotValue();
			((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
			setState(2072);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2068);
				match(T__1);
				setState(2069);
				((PivotClauseContext)_localctx).pivotValue = pivotValue();
				((PivotClauseContext)_localctx).pivotValues.add(((PivotClauseContext)_localctx).pivotValue);
				}
				}
				setState(2074);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2075);
			match(T__2);
			setState(2076);
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
		enterRule(_localctx, 146, RULE_pivotColumn);
		int _la;
		try {
			setState(2090);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2078);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2079);
				match(T__0);
				setState(2080);
				((PivotColumnContext)_localctx).identifier = identifier();
				((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
				setState(2085);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2081);
					match(T__1);
					setState(2082);
					((PivotColumnContext)_localctx).identifier = identifier();
					((PivotColumnContext)_localctx).identifiers.add(((PivotColumnContext)_localctx).identifier);
					}
					}
					setState(2087);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2088);
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
		enterRule(_localctx, 148, RULE_pivotValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2092);
			expression();
			setState(2097);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,256,_ctx) ) {
			case 1:
				{
				setState(2094);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,255,_ctx) ) {
				case 1:
					{
					setState(2093);
					match(AS);
					}
					break;
				}
				setState(2096);
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
		enterRule(_localctx, 150, RULE_lateralView);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2099);
			match(LATERAL);
			setState(2100);
			match(VIEW);
			setState(2102);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,257,_ctx) ) {
			case 1:
				{
				setState(2101);
				match(OUTER);
				}
				break;
			}
			setState(2104);
			qualifiedName();
			setState(2105);
			match(T__0);
			setState(2114);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,259,_ctx) ) {
			case 1:
				{
				setState(2106);
				expression();
				setState(2111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2107);
					match(T__1);
					setState(2108);
					expression();
					}
					}
					setState(2113);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2116);
			match(T__2);
			setState(2117);
			((LateralViewContext)_localctx).tblName = identifier();
			setState(2129);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,262,_ctx) ) {
			case 1:
				{
				setState(2119);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,260,_ctx) ) {
				case 1:
					{
					setState(2118);
					match(AS);
					}
					break;
				}
				setState(2121);
				((LateralViewContext)_localctx).identifier = identifier();
				((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
				setState(2126);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,261,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2122);
						match(T__1);
						setState(2123);
						((LateralViewContext)_localctx).identifier = identifier();
						((LateralViewContext)_localctx).colName.add(((LateralViewContext)_localctx).identifier);
						}
						} 
					}
					setState(2128);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,261,_ctx);
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
		enterRule(_localctx, 152, RULE_setQuantifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2131);
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
		enterRule(_localctx, 154, RULE_relation);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,263,_ctx) ) {
			case 1:
				{
				setState(2133);
				match(LATERAL);
				}
				break;
			}
			setState(2136);
			relationPrimary();
			setState(2140);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,264,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2137);
					joinRelation();
					}
					} 
				}
				setState(2142);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,264,_ctx);
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
		enterRule(_localctx, 156, RULE_joinRelation);
		try {
			setState(2160);
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
				setState(2143);
				joinType();
				}
				setState(2144);
				match(JOIN);
				setState(2146);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,265,_ctx) ) {
				case 1:
					{
					setState(2145);
					match(LATERAL);
					}
					break;
				}
				setState(2148);
				((JoinRelationContext)_localctx).right = relationPrimary();
				setState(2150);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,266,_ctx) ) {
				case 1:
					{
					setState(2149);
					joinCriteria();
					}
					break;
				}
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2152);
				match(NATURAL);
				setState(2153);
				joinType();
				setState(2154);
				match(JOIN);
				setState(2156);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,267,_ctx) ) {
				case 1:
					{
					setState(2155);
					match(LATERAL);
					}
					break;
				}
				setState(2158);
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
		enterRule(_localctx, 158, RULE_joinType);
		int _la;
		try {
			setState(2186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,275,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2163);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(2162);
					match(INNER);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2165);
				match(CROSS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2166);
				match(LEFT);
				setState(2168);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2167);
					match(OUTER);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2171);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2170);
					match(LEFT);
					}
				}

				setState(2173);
				match(SEMI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2174);
				match(RIGHT);
				setState(2176);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2175);
					match(OUTER);
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2178);
				match(FULL);
				setState(2180);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(2179);
					match(OUTER);
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2183);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT) {
					{
					setState(2182);
					match(LEFT);
					}
				}

				setState(2185);
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
		enterRule(_localctx, 160, RULE_joinCriteria);
		try {
			setState(2192);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(2188);
				match(ON);
				setState(2189);
				booleanExpression(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(2190);
				match(USING);
				setState(2191);
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
		public TerminalNode TABLESAMPLE() { return getToken(ArcticExtendSparkSqlParser.TABLESAMPLE, 0); }
		public SampleMethodContext sampleMethod() {
			return getRuleContext(SampleMethodContext.class,0);
		}
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
		enterRule(_localctx, 162, RULE_sample);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2194);
			match(TABLESAMPLE);
			setState(2195);
			match(T__0);
			setState(2197);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,277,_ctx) ) {
			case 1:
				{
				setState(2196);
				sampleMethod();
				}
				break;
			}
			setState(2199);
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
		enterRule(_localctx, 164, RULE_sampleMethod);
		int _la;
		try {
			setState(2225);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,281,_ctx) ) {
			case 1:
				_localctx = new SampleByPercentileContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2202);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2201);
					((SampleByPercentileContext)_localctx).negativeSign = match(MINUS);
					}
				}

				setState(2204);
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
				setState(2205);
				match(PERCENTLIT);
				}
				break;
			case 2:
				_localctx = new SampleByRowsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2206);
				expression();
				setState(2207);
				match(ROWS);
				}
				break;
			case 3:
				_localctx = new SampleByBucketContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2209);
				((SampleByBucketContext)_localctx).sampleType = match(BUCKET);
				setState(2210);
				((SampleByBucketContext)_localctx).numerator = match(INTEGER_VALUE);
				setState(2211);
				match(OUT);
				setState(2212);
				match(OF);
				setState(2213);
				((SampleByBucketContext)_localctx).denominator = match(INTEGER_VALUE);
				setState(2222);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(2214);
					match(ON);
					setState(2220);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,279,_ctx) ) {
					case 1:
						{
						setState(2215);
						identifier();
						}
						break;
					case 2:
						{
						setState(2216);
						qualifiedName();
						setState(2217);
						match(T__0);
						setState(2218);
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
				setState(2224);
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
		public IdentifierSeqContext identifierSeq() {
			return getRuleContext(IdentifierSeqContext.class,0);
		}
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
		enterRule(_localctx, 166, RULE_identifierList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2227);
			match(T__0);
			setState(2228);
			identifierSeq();
			setState(2229);
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
		enterRule(_localctx, 168, RULE_identifierSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2231);
			((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
			setState(2236);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,282,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2232);
					match(T__1);
					setState(2233);
					((IdentifierSeqContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((IdentifierSeqContext)_localctx).ident.add(((IdentifierSeqContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2238);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,282,_ctx);
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
		enterRule(_localctx, 170, RULE_orderedIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2239);
			match(T__0);
			setState(2240);
			orderedIdentifier();
			setState(2245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2241);
				match(T__1);
				setState(2242);
				orderedIdentifier();
				}
				}
				setState(2247);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2248);
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
		enterRule(_localctx, 172, RULE_orderedIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2250);
			((OrderedIdentifierContext)_localctx).ident = errorCapturingIdentifier();
			setState(2252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(2251);
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
		enterRule(_localctx, 174, RULE_identifierCommentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2254);
			match(T__0);
			setState(2255);
			identifierComment();
			setState(2260);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2256);
				match(T__1);
				setState(2257);
				identifierComment();
				}
				}
				setState(2262);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2263);
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
		enterRule(_localctx, 176, RULE_identifierComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2265);
			identifier();
			setState(2267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2266);
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
		enterRule(_localctx, 178, RULE_relationPrimary);
		try {
			setState(2293);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
			case 1:
				_localctx = new TableNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2269);
				multipartIdentifier();
				setState(2271);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,287,_ctx) ) {
				case 1:
					{
					setState(2270);
					sample();
					}
					break;
				}
				setState(2273);
				tableAlias();
				}
				break;
			case 2:
				_localctx = new AliasedQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2275);
				match(T__0);
				setState(2276);
				query();
				setState(2277);
				match(T__2);
				setState(2279);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,288,_ctx) ) {
				case 1:
					{
					setState(2278);
					sample();
					}
					break;
				}
				setState(2281);
				tableAlias();
				}
				break;
			case 3:
				_localctx = new AliasedRelationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2283);
				match(T__0);
				setState(2284);
				relation();
				setState(2285);
				match(T__2);
				setState(2287);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,289,_ctx) ) {
				case 1:
					{
					setState(2286);
					sample();
					}
					break;
				}
				setState(2289);
				tableAlias();
				}
				break;
			case 4:
				_localctx = new InlineTableDefault2Context(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2291);
				inlineTable();
				}
				break;
			case 5:
				_localctx = new TableValuedFunctionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2292);
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
		enterRule(_localctx, 180, RULE_inlineTable);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2295);
			match(VALUES);
			setState(2296);
			expression();
			setState(2301);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,291,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2297);
					match(T__1);
					setState(2298);
					expression();
					}
					} 
				}
				setState(2303);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,291,_ctx);
			}
			setState(2304);
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
		enterRule(_localctx, 182, RULE_functionTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2306);
			((FunctionTableContext)_localctx).funcName = functionName();
			setState(2307);
			match(T__0);
			setState(2316);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,293,_ctx) ) {
			case 1:
				{
				setState(2308);
				expression();
				setState(2313);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2309);
					match(T__1);
					setState(2310);
					expression();
					}
					}
					setState(2315);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2318);
			match(T__2);
			setState(2319);
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
		enterRule(_localctx, 184, RULE_tableAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2328);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,296,_ctx) ) {
			case 1:
				{
				setState(2322);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,294,_ctx) ) {
				case 1:
					{
					setState(2321);
					match(AS);
					}
					break;
				}
				setState(2324);
				strictIdentifier();
				setState(2326);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,295,_ctx) ) {
				case 1:
					{
					setState(2325);
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
		public TablePropertyListContext props;
		public TerminalNode ROW() { return getToken(ArcticExtendSparkSqlParser.ROW, 0); }
		public TerminalNode FORMAT() { return getToken(ArcticExtendSparkSqlParser.FORMAT, 0); }
		public TerminalNode SERDE() { return getToken(ArcticExtendSparkSqlParser.SERDE, 0); }
		public TerminalNode STRING() { return getToken(ArcticExtendSparkSqlParser.STRING, 0); }
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.SERDEPROPERTIES, 0); }
		public TablePropertyListContext tablePropertyList() {
			return getRuleContext(TablePropertyListContext.class,0);
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
		enterRule(_localctx, 186, RULE_rowFormat);
		try {
			setState(2379);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,304,_ctx) ) {
			case 1:
				_localctx = new RowFormatSerdeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2330);
				match(ROW);
				setState(2331);
				match(FORMAT);
				setState(2332);
				match(SERDE);
				setState(2333);
				((RowFormatSerdeContext)_localctx).name = match(STRING);
				setState(2337);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,297,_ctx) ) {
				case 1:
					{
					setState(2334);
					match(WITH);
					setState(2335);
					match(SERDEPROPERTIES);
					setState(2336);
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
				setState(2339);
				match(ROW);
				setState(2340);
				match(FORMAT);
				setState(2341);
				match(DELIMITED);
				setState(2351);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,299,_ctx) ) {
				case 1:
					{
					setState(2342);
					match(FIELDS);
					setState(2343);
					match(TERMINATED);
					setState(2344);
					match(BY);
					setState(2345);
					((RowFormatDelimitedContext)_localctx).fieldsTerminatedBy = match(STRING);
					setState(2349);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,298,_ctx) ) {
					case 1:
						{
						setState(2346);
						match(ESCAPED);
						setState(2347);
						match(BY);
						setState(2348);
						((RowFormatDelimitedContext)_localctx).escapedBy = match(STRING);
						}
						break;
					}
					}
					break;
				}
				setState(2358);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,300,_ctx) ) {
				case 1:
					{
					setState(2353);
					match(COLLECTION);
					setState(2354);
					match(ITEMS);
					setState(2355);
					match(TERMINATED);
					setState(2356);
					match(BY);
					setState(2357);
					((RowFormatDelimitedContext)_localctx).collectionItemsTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2365);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,301,_ctx) ) {
				case 1:
					{
					setState(2360);
					match(MAP);
					setState(2361);
					match(KEYS);
					setState(2362);
					match(TERMINATED);
					setState(2363);
					match(BY);
					setState(2364);
					((RowFormatDelimitedContext)_localctx).keysTerminatedBy = match(STRING);
					}
					break;
				}
				setState(2371);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,302,_ctx) ) {
				case 1:
					{
					setState(2367);
					match(LINES);
					setState(2368);
					match(TERMINATED);
					setState(2369);
					match(BY);
					setState(2370);
					((RowFormatDelimitedContext)_localctx).linesSeparatedBy = match(STRING);
					}
					break;
				}
				setState(2377);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,303,_ctx) ) {
				case 1:
					{
					setState(2373);
					match(NULL);
					setState(2374);
					match(DEFINED);
					setState(2375);
					match(AS);
					setState(2376);
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
		enterRule(_localctx, 188, RULE_multipartIdentifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2381);
			multipartIdentifier();
			setState(2386);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2382);
				match(T__1);
				setState(2383);
				multipartIdentifier();
				}
				}
				setState(2388);
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
		enterRule(_localctx, 190, RULE_multipartIdentifier);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2389);
			((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
			setState(2394);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,306,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2390);
					match(T__4);
					setState(2391);
					((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
					((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
					}
					} 
				}
				setState(2396);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,306,_ctx);
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
		enterRule(_localctx, 192, RULE_tableIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2400);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,307,_ctx) ) {
			case 1:
				{
				setState(2397);
				((TableIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2398);
				match(T__4);
				}
				break;
			}
			setState(2402);
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
		enterRule(_localctx, 194, RULE_functionIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2407);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,308,_ctx) ) {
			case 1:
				{
				setState(2404);
				((FunctionIdentifierContext)_localctx).db = errorCapturingIdentifier();
				setState(2405);
				match(T__4);
				}
				break;
			}
			setState(2409);
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
		enterRule(_localctx, 196, RULE_namedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2411);
			expression();
			setState(2419);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,311,_ctx) ) {
			case 1:
				{
				setState(2413);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,309,_ctx) ) {
				case 1:
					{
					setState(2412);
					match(AS);
					}
					break;
				}
				setState(2417);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,310,_ctx) ) {
				case 1:
					{
					setState(2415);
					((NamedExpressionContext)_localctx).name = errorCapturingIdentifier();
					}
					break;
				case 2:
					{
					setState(2416);
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
		enterRule(_localctx, 198, RULE_namedExpressionSeq);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2421);
			namedExpression();
			setState(2426);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,312,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2422);
					match(T__1);
					setState(2423);
					namedExpression();
					}
					} 
				}
				setState(2428);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,312,_ctx);
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
		enterRule(_localctx, 200, RULE_partitionFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2429);
			match(T__0);
			setState(2430);
			((PartitionFieldListContext)_localctx).partitionField = partitionField();
			((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
			setState(2435);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2431);
				match(T__1);
				setState(2432);
				((PartitionFieldListContext)_localctx).partitionField = partitionField();
				((PartitionFieldListContext)_localctx).fields.add(((PartitionFieldListContext)_localctx).partitionField);
				}
				}
				setState(2437);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2438);
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
		enterRule(_localctx, 202, RULE_partitionField);
		try {
			setState(2442);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,314,_ctx) ) {
			case 1:
				_localctx = new PartitionTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2440);
				transform();
				}
				break;
			case 2:
				_localctx = new PartitionColumnContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2441);
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
		enterRule(_localctx, 204, RULE_transform);
		int _la;
		try {
			setState(2457);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,316,_ctx) ) {
			case 1:
				_localctx = new IdentityTransformContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2444);
				qualifiedName();
				}
				break;
			case 2:
				_localctx = new ApplyTransformContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2445);
				((ApplyTransformContext)_localctx).transformName = identifier();
				setState(2446);
				match(T__0);
				setState(2447);
				((ApplyTransformContext)_localctx).transformArgument = transformArgument();
				((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
				setState(2452);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2448);
					match(T__1);
					setState(2449);
					((ApplyTransformContext)_localctx).transformArgument = transformArgument();
					((ApplyTransformContext)_localctx).argument.add(((ApplyTransformContext)_localctx).transformArgument);
					}
					}
					setState(2454);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2455);
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
		enterRule(_localctx, 206, RULE_transformArgument);
		try {
			setState(2461);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,317,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2459);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2460);
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
		enterRule(_localctx, 208, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2463);
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
		enterRule(_localctx, 210, RULE_expressionSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2465);
			expression();
			setState(2470);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2466);
				match(T__1);
				setState(2467);
				expression();
				}
				}
				setState(2472);
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
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
		int _startState = 212;
		enterRecursionRule(_localctx, 212, RULE_booleanExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2485);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,320,_ctx) ) {
			case 1:
				{
				_localctx = new LogicalNotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2474);
				match(NOT);
				setState(2475);
				booleanExpression(5);
				}
				break;
			case 2:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2476);
				match(EXISTS);
				setState(2477);
				match(T__0);
				setState(2478);
				query();
				setState(2479);
				match(T__2);
				}
				break;
			case 3:
				{
				_localctx = new PredicatedContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2481);
				valueExpression(0);
				setState(2483);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,319,_ctx) ) {
				case 1:
					{
					setState(2482);
					predicate();
					}
					break;
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2495);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2493);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,321,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2487);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2488);
						((LogicalBinaryContext)_localctx).operator = match(AND);
						setState(2489);
						((LogicalBinaryContext)_localctx).right = booleanExpression(3);
						}
						break;
					case 2:
						{
						_localctx = new LogicalBinaryContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(2490);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2491);
						((LogicalBinaryContext)_localctx).operator = match(OR);
						setState(2492);
						((LogicalBinaryContext)_localctx).right = booleanExpression(2);
						}
						break;
					}
					} 
				}
				setState(2497);
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
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RLIKE() { return getToken(ArcticExtendSparkSqlParser.RLIKE, 0); }
		public TerminalNode LIKE() { return getToken(ArcticExtendSparkSqlParser.LIKE, 0); }
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
		enterRule(_localctx, 214, RULE_predicate);
		int _la;
		try {
			setState(2580);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,336,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2499);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2498);
					match(NOT);
					}
				}

				setState(2501);
				((PredicateContext)_localctx).kind = match(BETWEEN);
				setState(2502);
				((PredicateContext)_localctx).lower = valueExpression(0);
				setState(2503);
				match(AND);
				setState(2504);
				((PredicateContext)_localctx).upper = valueExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2507);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2506);
					match(NOT);
					}
				}

				setState(2509);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2510);
				match(T__0);
				setState(2511);
				expression();
				setState(2516);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(2512);
					match(T__1);
					setState(2513);
					expression();
					}
					}
					setState(2518);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2519);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2522);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2521);
					match(NOT);
					}
				}

				setState(2524);
				((PredicateContext)_localctx).kind = match(IN);
				setState(2525);
				match(T__0);
				setState(2526);
				query();
				setState(2527);
				match(T__2);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2530);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2529);
					match(NOT);
					}
				}

				setState(2532);
				((PredicateContext)_localctx).kind = match(RLIKE);
				setState(2533);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2535);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2534);
					match(NOT);
					}
				}

				setState(2537);
				((PredicateContext)_localctx).kind = match(LIKE);
				setState(2538);
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
				setState(2552);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,330,_ctx) ) {
				case 1:
					{
					setState(2539);
					match(T__0);
					setState(2540);
					match(T__2);
					}
					break;
				case 2:
					{
					setState(2541);
					match(T__0);
					setState(2542);
					expression();
					setState(2547);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(2543);
						match(T__1);
						setState(2544);
						expression();
						}
						}
						setState(2549);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2550);
					match(T__2);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2555);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2554);
					match(NOT);
					}
				}

				setState(2557);
				((PredicateContext)_localctx).kind = match(LIKE);
				setState(2558);
				((PredicateContext)_localctx).pattern = valueExpression(0);
				setState(2561);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,332,_ctx) ) {
				case 1:
					{
					setState(2559);
					match(ESCAPE);
					setState(2560);
					((PredicateContext)_localctx).escapeChar = match(STRING);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2563);
				match(IS);
				setState(2565);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2564);
					match(NOT);
					}
				}

				setState(2567);
				((PredicateContext)_localctx).kind = match(NULL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2568);
				match(IS);
				setState(2570);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2569);
					match(NOT);
					}
				}

				setState(2572);
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
				setState(2573);
				match(IS);
				setState(2575);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(2574);
					match(NOT);
					}
				}

				setState(2577);
				((PredicateContext)_localctx).kind = match(DISTINCT);
				setState(2578);
				match(FROM);
				setState(2579);
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
		int _startState = 216;
		enterRecursionRule(_localctx, 216, RULE_valueExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2586);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,337,_ctx) ) {
			case 1:
				{
				_localctx = new ValueExpressionDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2583);
				primaryExpression(0);
				}
				break;
			case 2:
				{
				_localctx = new ArithmeticUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2584);
				((ArithmeticUnaryContext)_localctx).operator = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 280)) & ~0x3f) == 0 && ((1L << (_la - 280)) & ((1L << (PLUS - 280)) | (1L << (MINUS - 280)) | (1L << (TILDE - 280)))) != 0)) ) {
					((ArithmeticUnaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2585);
				valueExpression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2609);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,339,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2607);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,338,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2588);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2589);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==DIV || ((((_la - 282)) & ~0x3f) == 0 && ((1L << (_la - 282)) & ((1L << (ASTERISK - 282)) | (1L << (SLASH - 282)) | (1L << (PERCENT - 282)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2590);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(7);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2591);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(2592);
						((ArithmeticBinaryContext)_localctx).operator = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 280)) & ~0x3f) == 0 && ((1L << (_la - 280)) & ((1L << (PLUS - 280)) | (1L << (MINUS - 280)) | (1L << (CONCAT_PIPE - 280)))) != 0)) ) {
							((ArithmeticBinaryContext)_localctx).operator = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2593);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(6);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2594);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(2595);
						((ArithmeticBinaryContext)_localctx).operator = match(AMPERSAND);
						setState(2596);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(5);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2597);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(2598);
						((ArithmeticBinaryContext)_localctx).operator = match(HAT);
						setState(2599);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(4);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticBinaryContext(new ValueExpressionContext(_parentctx, _parentState));
						((ArithmeticBinaryContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2600);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2601);
						((ArithmeticBinaryContext)_localctx).operator = match(PIPE);
						setState(2602);
						((ArithmeticBinaryContext)_localctx).right = valueExpression(3);
						}
						break;
					case 6:
						{
						_localctx = new ComparisonContext(new ValueExpressionContext(_parentctx, _parentState));
						((ComparisonContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_valueExpression);
						setState(2603);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(2604);
						comparisonOperator();
						setState(2605);
						((ComparisonContext)_localctx).right = valueExpression(2);
						}
						break;
					}
					} 
				}
				setState(2611);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,339,_ctx);
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
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
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
		public List<NamedExpressionContext> namedExpression() {
			return getRuleContexts(NamedExpressionContext.class);
		}
		public NamedExpressionContext namedExpression(int i) {
			return getRuleContext(NamedExpressionContext.class,i);
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
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
		public TerminalNode PLACING() { return getToken(ArcticExtendSparkSqlParser.PLACING, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
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
	public static class SubqueryExpressionContext extends PrimaryExpressionContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
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
	public static class SubstringContext extends PrimaryExpressionContext {
		public ValueExpressionContext str;
		public ValueExpressionContext pos;
		public ValueExpressionContext len;
		public TerminalNode SUBSTR() { return getToken(ArcticExtendSparkSqlParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(ArcticExtendSparkSqlParser.SUBSTRING, 0); }
		public List<ValueExpressionContext> valueExpression() {
			return getRuleContexts(ValueExpressionContext.class);
		}
		public ValueExpressionContext valueExpression(int i) {
			return getRuleContext(ValueExpressionContext.class,i);
		}
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(ArcticExtendSparkSqlParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
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
	public static class ExtractContext extends PrimaryExpressionContext {
		public IdentifierContext field;
		public ValueExpressionContext source;
		public TerminalNode EXTRACT() { return getToken(ArcticExtendSparkSqlParser.EXTRACT, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
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
	public static class TrimContext extends PrimaryExpressionContext {
		public Token trimOption;
		public ValueExpressionContext trimStr;
		public ValueExpressionContext srcStr;
		public TerminalNode TRIM() { return getToken(ArcticExtendSparkSqlParser.TRIM, 0); }
		public TerminalNode FROM() { return getToken(ArcticExtendSparkSqlParser.FROM, 0); }
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
	public static class FunctionCallContext extends PrimaryExpressionContext {
		public ExpressionContext expression;
		public List<ExpressionContext> argument = new ArrayList<ExpressionContext>();
		public BooleanExpressionContext where;
		public Token nullsOption;
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
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
		public TerminalNode IN() { return getToken(ArcticExtendSparkSqlParser.IN, 0); }
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
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
		int _startState = 218;
		enterRecursionRule(_localctx, 218, RULE_primaryExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2800);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,360,_ctx) ) {
			case 1:
				{
				_localctx = new CurrentLikeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(2613);
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
				_localctx = new SearchedCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2614);
				match(CASE);
				setState(2616); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2615);
					whenClause();
					}
					}
					setState(2618); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2622);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2620);
					match(ELSE);
					setState(2621);
					((SearchedCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2624);
				match(END);
				}
				break;
			case 3:
				{
				_localctx = new SimpleCaseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2626);
				match(CASE);
				setState(2627);
				((SimpleCaseContext)_localctx).value = expression();
				setState(2629); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2628);
					whenClause();
					}
					}
					setState(2631); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(2635);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(2633);
					match(ELSE);
					setState(2634);
					((SimpleCaseContext)_localctx).elseExpression = expression();
					}
				}

				setState(2637);
				match(END);
				}
				break;
			case 4:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2639);
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
				setState(2640);
				match(T__0);
				setState(2641);
				expression();
				setState(2642);
				match(AS);
				setState(2643);
				dataType();
				setState(2644);
				match(T__2);
				}
				break;
			case 5:
				{
				_localctx = new StructContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2646);
				match(STRUCT);
				setState(2647);
				match(T__0);
				setState(2656);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,345,_ctx) ) {
				case 1:
					{
					setState(2648);
					((StructContext)_localctx).namedExpression = namedExpression();
					((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
					setState(2653);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(2649);
						match(T__1);
						setState(2650);
						((StructContext)_localctx).namedExpression = namedExpression();
						((StructContext)_localctx).argument.add(((StructContext)_localctx).namedExpression);
						}
						}
						setState(2655);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2658);
				match(T__2);
				}
				break;
			case 6:
				{
				_localctx = new FirstContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2659);
				match(FIRST);
				setState(2660);
				match(T__0);
				setState(2661);
				expression();
				setState(2664);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2662);
					match(IGNORE);
					setState(2663);
					match(NULLS);
					}
				}

				setState(2666);
				match(T__2);
				}
				break;
			case 7:
				{
				_localctx = new LastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2668);
				match(LAST);
				setState(2669);
				match(T__0);
				setState(2670);
				expression();
				setState(2673);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(2671);
					match(IGNORE);
					setState(2672);
					match(NULLS);
					}
				}

				setState(2675);
				match(T__2);
				}
				break;
			case 8:
				{
				_localctx = new PositionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2677);
				match(POSITION);
				setState(2678);
				match(T__0);
				setState(2679);
				((PositionContext)_localctx).substr = valueExpression(0);
				setState(2680);
				match(IN);
				setState(2681);
				((PositionContext)_localctx).str = valueExpression(0);
				setState(2682);
				match(T__2);
				}
				break;
			case 9:
				{
				_localctx = new ConstantDefaultContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2684);
				constant();
				}
				break;
			case 10:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2685);
				match(ASTERISK);
				}
				break;
			case 11:
				{
				_localctx = new StarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2686);
				qualifiedName();
				setState(2687);
				match(T__4);
				setState(2688);
				match(ASTERISK);
				}
				break;
			case 12:
				{
				_localctx = new RowConstructorContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2690);
				match(T__0);
				setState(2691);
				namedExpression();
				setState(2694); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2692);
					match(T__1);
					setState(2693);
					namedExpression();
					}
					}
					setState(2696); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__1 );
				setState(2698);
				match(T__2);
				}
				break;
			case 13:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2700);
				match(T__0);
				setState(2701);
				query();
				setState(2702);
				match(T__2);
				}
				break;
			case 14:
				{
				_localctx = new FunctionCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2704);
				functionName();
				setState(2705);
				match(T__0);
				setState(2717);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,351,_ctx) ) {
				case 1:
					{
					setState(2707);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,349,_ctx) ) {
					case 1:
						{
						setState(2706);
						setQuantifier();
						}
						break;
					}
					setState(2709);
					((FunctionCallContext)_localctx).expression = expression();
					((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
					setState(2714);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(2710);
						match(T__1);
						setState(2711);
						((FunctionCallContext)_localctx).expression = expression();
						((FunctionCallContext)_localctx).argument.add(((FunctionCallContext)_localctx).expression);
						}
						}
						setState(2716);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				}
				setState(2719);
				match(T__2);
				setState(2726);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,352,_ctx) ) {
				case 1:
					{
					setState(2720);
					match(FILTER);
					setState(2721);
					match(T__0);
					setState(2722);
					match(WHERE);
					setState(2723);
					((FunctionCallContext)_localctx).where = booleanExpression(0);
					setState(2724);
					match(T__2);
					}
					break;
				}
				setState(2730);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,353,_ctx) ) {
				case 1:
					{
					setState(2728);
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
					setState(2729);
					match(NULLS);
					}
					break;
				}
				setState(2734);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,354,_ctx) ) {
				case 1:
					{
					setState(2732);
					match(OVER);
					setState(2733);
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
				setState(2736);
				identifier();
				setState(2737);
				match(T__7);
				setState(2738);
				expression();
				}
				break;
			case 16:
				{
				_localctx = new LambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2740);
				match(T__0);
				setState(2741);
				identifier();
				setState(2744); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(2742);
					match(T__1);
					setState(2743);
					identifier();
					}
					}
					setState(2746); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__1 );
				setState(2748);
				match(T__2);
				setState(2749);
				match(T__7);
				setState(2750);
				expression();
				}
				break;
			case 17:
				{
				_localctx = new ColumnReferenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2752);
				identifier();
				}
				break;
			case 18:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2753);
				match(T__0);
				setState(2754);
				expression();
				setState(2755);
				match(T__2);
				}
				break;
			case 19:
				{
				_localctx = new ExtractContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2757);
				match(EXTRACT);
				setState(2758);
				match(T__0);
				setState(2759);
				((ExtractContext)_localctx).field = identifier();
				setState(2760);
				match(FROM);
				setState(2761);
				((ExtractContext)_localctx).source = valueExpression(0);
				setState(2762);
				match(T__2);
				}
				break;
			case 20:
				{
				_localctx = new SubstringContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2764);
				_la = _input.LA(1);
				if ( !(_la==SUBSTR || _la==SUBSTRING) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2765);
				match(T__0);
				setState(2766);
				((SubstringContext)_localctx).str = valueExpression(0);
				setState(2767);
				_la = _input.LA(1);
				if ( !(_la==T__1 || _la==FROM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2768);
				((SubstringContext)_localctx).pos = valueExpression(0);
				setState(2771);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1 || _la==FOR) {
					{
					setState(2769);
					_la = _input.LA(1);
					if ( !(_la==T__1 || _la==FOR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2770);
					((SubstringContext)_localctx).len = valueExpression(0);
					}
				}

				setState(2773);
				match(T__2);
				}
				break;
			case 21:
				{
				_localctx = new TrimContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2775);
				match(TRIM);
				setState(2776);
				match(T__0);
				setState(2778);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,357,_ctx) ) {
				case 1:
					{
					setState(2777);
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
				setState(2781);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,358,_ctx) ) {
				case 1:
					{
					setState(2780);
					((TrimContext)_localctx).trimStr = valueExpression(0);
					}
					break;
				}
				setState(2783);
				match(FROM);
				setState(2784);
				((TrimContext)_localctx).srcStr = valueExpression(0);
				setState(2785);
				match(T__2);
				}
				break;
			case 22:
				{
				_localctx = new OverlayContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(2787);
				match(OVERLAY);
				setState(2788);
				match(T__0);
				setState(2789);
				((OverlayContext)_localctx).input = valueExpression(0);
				setState(2790);
				match(PLACING);
				setState(2791);
				((OverlayContext)_localctx).replace = valueExpression(0);
				setState(2792);
				match(FROM);
				setState(2793);
				((OverlayContext)_localctx).position = valueExpression(0);
				setState(2796);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(2794);
					match(FOR);
					setState(2795);
					((OverlayContext)_localctx).length = valueExpression(0);
					}
				}

				setState(2798);
				match(T__2);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2812);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,362,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2810);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,361,_ctx) ) {
					case 1:
						{
						_localctx = new SubscriptContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((SubscriptContext)_localctx).value = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2802);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(2803);
						match(T__8);
						setState(2804);
						((SubscriptContext)_localctx).index = valueExpression(0);
						setState(2805);
						match(T__9);
						}
						break;
					case 2:
						{
						_localctx = new DereferenceContext(new PrimaryExpressionContext(_parentctx, _parentState));
						((DereferenceContext)_localctx).base = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_primaryExpression);
						setState(2807);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2808);
						match(T__4);
						setState(2809);
						((DereferenceContext)_localctx).fieldName = identifier();
						}
						break;
					}
					} 
				}
				setState(2814);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,362,_ctx);
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
		enterRule(_localctx, 220, RULE_constant);
		try {
			int _alt;
			setState(2827);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,364,_ctx) ) {
			case 1:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2815);
				match(NULL);
				}
				break;
			case 2:
				_localctx = new IntervalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2816);
				interval();
				}
				break;
			case 3:
				_localctx = new TypeConstructorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2817);
				identifier();
				setState(2818);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new NumericLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2820);
				number();
				}
				break;
			case 5:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2821);
				booleanValue();
				}
				break;
			case 6:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2823); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2822);
						match(STRING);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2825); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,363,_ctx);
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
		enterRule(_localctx, 222, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2829);
			_la = _input.LA(1);
			if ( !(((((_la - 272)) & ~0x3f) == 0 && ((1L << (_la - 272)) & ((1L << (EQ - 272)) | (1L << (NSEQ - 272)) | (1L << (NEQ - 272)) | (1L << (NEQJ - 272)) | (1L << (LT - 272)) | (1L << (LTE - 272)) | (1L << (GT - 272)) | (1L << (GTE - 272)))) != 0)) ) {
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
		enterRule(_localctx, 224, RULE_arithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2831);
			_la = _input.LA(1);
			if ( !(_la==DIV || ((((_la - 280)) & ~0x3f) == 0 && ((1L << (_la - 280)) & ((1L << (PLUS - 280)) | (1L << (MINUS - 280)) | (1L << (ASTERISK - 280)) | (1L << (SLASH - 280)) | (1L << (PERCENT - 280)) | (1L << (TILDE - 280)) | (1L << (AMPERSAND - 280)) | (1L << (PIPE - 280)) | (1L << (CONCAT_PIPE - 280)) | (1L << (HAT - 280)))) != 0)) ) {
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
		enterRule(_localctx, 226, RULE_predicateOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2833);
			_la = _input.LA(1);
			if ( !(_la==AND || ((((_la - 116)) & ~0x3f) == 0 && ((1L << (_la - 116)) & ((1L << (IN - 116)) | (1L << (NOT - 116)) | (1L << (OR - 116)))) != 0)) ) {
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
		enterRule(_localctx, 228, RULE_booleanValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2835);
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
		enterRule(_localctx, 230, RULE_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2837);
			match(INTERVAL);
			setState(2840);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,365,_ctx) ) {
			case 1:
				{
				setState(2838);
				errorCapturingMultiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2839);
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
		enterRule(_localctx, 232, RULE_errorCapturingMultiUnitsInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2842);
			((ErrorCapturingMultiUnitsIntervalContext)_localctx).body = multiUnitsInterval();
			setState(2844);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,366,_ctx) ) {
			case 1:
				{
				setState(2843);
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
		enterRule(_localctx, 234, RULE_multiUnitsInterval);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2849); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2846);
					intervalValue();
					setState(2847);
					((MultiUnitsIntervalContext)_localctx).identifier = identifier();
					((MultiUnitsIntervalContext)_localctx).unit.add(((MultiUnitsIntervalContext)_localctx).identifier);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2851); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,367,_ctx);
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
		enterRule(_localctx, 236, RULE_errorCapturingUnitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2853);
			((ErrorCapturingUnitToUnitIntervalContext)_localctx).body = unitToUnitInterval();
			setState(2856);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,368,_ctx) ) {
			case 1:
				{
				setState(2854);
				((ErrorCapturingUnitToUnitIntervalContext)_localctx).error1 = multiUnitsInterval();
				}
				break;
			case 2:
				{
				setState(2855);
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
		enterRule(_localctx, 238, RULE_unitToUnitInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2858);
			((UnitToUnitIntervalContext)_localctx).value = intervalValue();
			setState(2859);
			((UnitToUnitIntervalContext)_localctx).from = identifier();
			setState(2860);
			match(TO);
			setState(2861);
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
		enterRule(_localctx, 240, RULE_intervalValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2864);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(2863);
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

			setState(2866);
			_la = _input.LA(1);
			if ( !(((((_la - 290)) & ~0x3f) == 0 && ((1L << (_la - 290)) & ((1L << (STRING - 290)) | (1L << (INTEGER_VALUE - 290)) | (1L << (DECIMAL_VALUE - 290)))) != 0)) ) {
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
		enterRule(_localctx, 242, RULE_colPosition);
		try {
			setState(2871);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				enterOuterAlt(_localctx, 1);
				{
				setState(2868);
				((ColPositionContext)_localctx).position = match(FIRST);
				}
				break;
			case AFTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2869);
				((ColPositionContext)_localctx).position = match(AFTER);
				setState(2870);
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
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(ArcticExtendSparkSqlParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(ArcticExtendSparkSqlParser.INTEGER_VALUE, i);
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
		enterRule(_localctx, 244, RULE_dataType);
		int _la;
		try {
			setState(2919);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,377,_ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2873);
				((ComplexDataTypeContext)_localctx).complex = match(ARRAY);
				setState(2874);
				match(LT);
				setState(2875);
				dataType();
				setState(2876);
				match(GT);
				}
				break;
			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2878);
				((ComplexDataTypeContext)_localctx).complex = match(MAP);
				setState(2879);
				match(LT);
				setState(2880);
				dataType();
				setState(2881);
				match(T__1);
				setState(2882);
				dataType();
				setState(2883);
				match(GT);
				}
				break;
			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2885);
				((ComplexDataTypeContext)_localctx).complex = match(STRUCT);
				setState(2892);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LT:
					{
					setState(2886);
					match(LT);
					setState(2888);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,371,_ctx) ) {
					case 1:
						{
						setState(2887);
						complexColTypeList();
						}
						break;
					}
					setState(2890);
					match(GT);
					}
					break;
				case NEQ:
					{
					setState(2891);
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
				setState(2894);
				match(INTERVAL);
				setState(2895);
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
				setState(2898);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,373,_ctx) ) {
				case 1:
					{
					setState(2896);
					match(TO);
					setState(2897);
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
				setState(2900);
				match(INTERVAL);
				setState(2901);
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
				setState(2904);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,374,_ctx) ) {
				case 1:
					{
					setState(2902);
					match(TO);
					setState(2903);
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
				setState(2906);
				identifier();
				setState(2917);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,376,_ctx) ) {
				case 1:
					{
					setState(2907);
					match(T__0);
					setState(2908);
					match(INTEGER_VALUE);
					setState(2913);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(2909);
						match(T__1);
						setState(2910);
						match(INTEGER_VALUE);
						}
						}
						setState(2915);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(2916);
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
		enterRule(_localctx, 246, RULE_qualifiedColTypeWithPositionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2921);
			qualifiedColTypeWithPosition();
			setState(2926);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2922);
				match(T__1);
				setState(2923);
				qualifiedColTypeWithPosition();
				}
				}
				setState(2928);
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
		enterRule(_localctx, 248, RULE_qualifiedColTypeWithPosition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2929);
			((QualifiedColTypeWithPositionContext)_localctx).name = multipartIdentifier();
			setState(2930);
			dataType();
			setState(2933);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(2931);
				match(NOT);
				setState(2932);
				match(NULL);
				}
			}

			setState(2936);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2935);
				commentSpec();
				}
			}

			setState(2939);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AFTER || _la==FIRST) {
				{
				setState(2938);
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
		enterRule(_localctx, 250, RULE_colTypeList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2941);
			colType();
			setState(2946);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,382,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2942);
					match(T__1);
					setState(2943);
					colType();
					}
					} 
				}
				setState(2948);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,382,_ctx);
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
		enterRule(_localctx, 252, RULE_colType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2949);
			((ColTypeContext)_localctx).colName = errorCapturingIdentifier();
			setState(2950);
			dataType();
			setState(2953);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,383,_ctx) ) {
			case 1:
				{
				setState(2951);
				match(NOT);
				setState(2952);
				match(NULL);
				}
				break;
			}
			setState(2956);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,384,_ctx) ) {
			case 1:
				{
				setState(2955);
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
		enterRule(_localctx, 254, RULE_complexColTypeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2958);
			complexColType();
			setState(2963);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(2959);
				match(T__1);
				setState(2960);
				complexColType();
				}
				}
				setState(2965);
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
		enterRule(_localctx, 256, RULE_complexColType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2966);
			identifier();
			setState(2968);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,386,_ctx) ) {
			case 1:
				{
				setState(2967);
				match(T__10);
				}
				break;
			}
			setState(2970);
			dataType();
			setState(2973);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(2971);
				match(NOT);
				setState(2972);
				match(NULL);
				}
			}

			setState(2976);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(2975);
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
		enterRule(_localctx, 258, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2978);
			match(WHEN);
			setState(2979);
			((WhenClauseContext)_localctx).condition = expression();
			setState(2980);
			match(THEN);
			setState(2981);
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
		enterRule(_localctx, 260, RULE_windowClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2983);
			match(WINDOW);
			setState(2984);
			namedWindow();
			setState(2989);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,389,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2985);
					match(T__1);
					setState(2986);
					namedWindow();
					}
					} 
				}
				setState(2991);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,389,_ctx);
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
		enterRule(_localctx, 262, RULE_namedWindow);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2992);
			((NamedWindowContext)_localctx).name = errorCapturingIdentifier();
			setState(2993);
			match(AS);
			setState(2994);
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
		enterRule(_localctx, 264, RULE_windowSpec);
		int _la;
		try {
			setState(3042);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,397,_ctx) ) {
			case 1:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2996);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				}
				break;
			case 2:
				_localctx = new WindowRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2997);
				match(T__0);
				setState(2998);
				((WindowRefContext)_localctx).name = errorCapturingIdentifier();
				setState(2999);
				match(T__2);
				}
				break;
			case 3:
				_localctx = new WindowDefContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3001);
				match(T__0);
				setState(3036);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLUSTER:
					{
					setState(3002);
					match(CLUSTER);
					setState(3003);
					match(BY);
					setState(3004);
					((WindowDefContext)_localctx).expression = expression();
					((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
					setState(3009);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__1) {
						{
						{
						setState(3005);
						match(T__1);
						setState(3006);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						}
						}
						setState(3011);
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
					setState(3022);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DISTRIBUTE || _la==PARTITION) {
						{
						setState(3012);
						_la = _input.LA(1);
						if ( !(_la==DISTRIBUTE || _la==PARTITION) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3013);
						match(BY);
						setState(3014);
						((WindowDefContext)_localctx).expression = expression();
						((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
						setState(3019);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==T__1) {
							{
							{
							setState(3015);
							match(T__1);
							setState(3016);
							((WindowDefContext)_localctx).expression = expression();
							((WindowDefContext)_localctx).partition.add(((WindowDefContext)_localctx).expression);
							}
							}
							setState(3021);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
					}

					setState(3034);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ORDER || _la==SORT) {
						{
						setState(3024);
						_la = _input.LA(1);
						if ( !(_la==ORDER || _la==SORT) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(3025);
						match(BY);
						setState(3026);
						sortItem();
						setState(3031);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==T__1) {
							{
							{
							setState(3027);
							match(T__1);
							setState(3028);
							sortItem();
							}
							}
							setState(3033);
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
				setState(3039);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==RANGE || _la==ROWS) {
					{
					setState(3038);
					windowFrame();
					}
				}

				setState(3041);
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
		enterRule(_localctx, 266, RULE_windowFrame);
		try {
			setState(3060);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,398,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3044);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3045);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3046);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3047);
				((WindowFrameContext)_localctx).start = frameBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3048);
				((WindowFrameContext)_localctx).frameType = match(RANGE);
				setState(3049);
				match(BETWEEN);
				setState(3050);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3051);
				match(AND);
				setState(3052);
				((WindowFrameContext)_localctx).end = frameBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3054);
				((WindowFrameContext)_localctx).frameType = match(ROWS);
				setState(3055);
				match(BETWEEN);
				setState(3056);
				((WindowFrameContext)_localctx).start = frameBound();
				setState(3057);
				match(AND);
				setState(3058);
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
		enterRule(_localctx, 268, RULE_frameBound);
		int _la;
		try {
			setState(3069);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,399,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3062);
				match(UNBOUNDED);
				setState(3063);
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
				setState(3064);
				((FrameBoundContext)_localctx).boundType = match(CURRENT);
				setState(3065);
				match(ROW);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3066);
				expression();
				setState(3067);
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
		enterRule(_localctx, 270, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3071);
			qualifiedName();
			setState(3076);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__1) {
				{
				{
				setState(3072);
				match(T__1);
				setState(3073);
				qualifiedName();
				}
				}
				setState(3078);
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
		enterRule(_localctx, 272, RULE_functionName);
		try {
			setState(3083);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,401,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3079);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3080);
				match(FILTER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(3081);
				match(LEFT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(3082);
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
		enterRule(_localctx, 274, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(3085);
			identifier();
			setState(3090);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,402,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(3086);
					match(T__4);
					setState(3087);
					identifier();
					}
					} 
				}
				setState(3092);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,402,_ctx);
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
		enterRule(_localctx, 276, RULE_errorCapturingIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3093);
			identifier();
			setState(3094);
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
		enterRule(_localctx, 278, RULE_errorCapturingIdentifierExtra);
		try {
			int _alt;
			setState(3103);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,404,_ctx) ) {
			case 1:
				_localctx = new ErrorIdentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3098); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(3096);
						match(MINUS);
						setState(3097);
						identifier();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(3100); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,403,_ctx);
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
		enterRule(_localctx, 280, RULE_identifier);
		try {
			setState(3108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,405,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(3105);
				strictIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(3106);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3107);
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
		enterRule(_localctx, 282, RULE_strictIdentifier);
		try {
			setState(3116);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,406,_ctx) ) {
			case 1:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3110);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3111);
				quotedIdentifier();
				}
				break;
			case 3:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3112);
				if (!(SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "SQL_standard_keyword_behavior");
				setState(3113);
				ansiNonReserved();
				}
				break;
			case 4:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(3114);
				if (!(!SQL_standard_keyword_behavior)) throw new FailedPredicateException(this, "!SQL_standard_keyword_behavior");
				setState(3115);
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
		enterRule(_localctx, 284, RULE_quotedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3118);
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
		enterRule(_localctx, 286, RULE_number);
		int _la;
		try {
			setState(3163);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,417,_ctx) ) {
			case 1:
				_localctx = new ExponentLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(3120);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
				setState(3122);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3121);
					match(MINUS);
					}
				}

				setState(3124);
				match(EXPONENT_VALUE);
				}
				break;
			case 2:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(3125);
				if (!(!legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "!legacy_exponent_literal_as_decimal_enabled");
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
				match(DECIMAL_VALUE);
				}
				break;
			case 3:
				_localctx = new LegacyDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(3130);
				if (!(legacy_exponent_literal_as_decimal_enabled)) throw new FailedPredicateException(this, "legacy_exponent_literal_as_decimal_enabled");
				setState(3132);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3131);
					match(MINUS);
					}
				}

				setState(3134);
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
				setState(3136);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3135);
					match(MINUS);
					}
				}

				setState(3138);
				match(INTEGER_VALUE);
				}
				break;
			case 5:
				_localctx = new BigIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(3140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3139);
					match(MINUS);
					}
				}

				setState(3142);
				match(BIGINT_LITERAL);
				}
				break;
			case 6:
				_localctx = new SmallIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(3144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3143);
					match(MINUS);
					}
				}

				setState(3146);
				match(SMALLINT_LITERAL);
				}
				break;
			case 7:
				_localctx = new TinyIntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(3148);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3147);
					match(MINUS);
					}
				}

				setState(3150);
				match(TINYINT_LITERAL);
				}
				break;
			case 8:
				_localctx = new DoubleLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(3152);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3151);
					match(MINUS);
					}
				}

				setState(3154);
				match(DOUBLE_LITERAL);
				}
				break;
			case 9:
				_localctx = new FloatLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(3156);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3155);
					match(MINUS);
					}
				}

				setState(3158);
				match(FLOAT_LITERAL);
				}
				break;
			case 10:
				_localctx = new BigDecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(3160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(3159);
					match(MINUS);
					}
				}

				setState(3162);
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
		enterRule(_localctx, 288, RULE_alterColumnAction);
		int _la;
		try {
			setState(3172);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(3165);
				match(TYPE);
				setState(3166);
				dataType();
				}
				break;
			case COMMENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(3167);
				commentSpec();
				}
				break;
			case AFTER:
			case FIRST:
				enterOuterAlt(_localctx, 3);
				{
				setState(3168);
				colPosition();
				}
				break;
			case DROP:
			case SET:
				enterOuterAlt(_localctx, 4);
				{
				setState(3169);
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
				setState(3170);
				match(NOT);
				setState(3171);
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
		public TerminalNode DAY() { return getToken(ArcticExtendSparkSqlParser.DAY, 0); }
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
		public TerminalNode QUERY() { return getToken(ArcticExtendSparkSqlParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(ArcticExtendSparkSqlParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(ArcticExtendSparkSqlParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(ArcticExtendSparkSqlParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(ArcticExtendSparkSqlParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(ArcticExtendSparkSqlParser.REDUCE, 0); }
		public TerminalNode REFRESH() { return getToken(ArcticExtendSparkSqlParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(ArcticExtendSparkSqlParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(ArcticExtendSparkSqlParser.REPAIR, 0); }
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
		public TerminalNode TABLES() { return getToken(ArcticExtendSparkSqlParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticExtendSparkSqlParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticExtendSparkSqlParser.TERMINATED, 0); }
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
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticExtendSparkSqlParser.VIEWS, 0); }
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
		enterRule(_localctx, 290, RULE_ansiNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3174);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALTER) | (1L << ANALYZE) | (1L << ANTI) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << ASC) | (1L << AT) | (1L << BETWEEN) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CHANGE) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLECTION) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << COST) | (1L << CUBE) | (1L << CURRENT) | (1L << DAY))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATA - 64)) | (1L << (DATABASE - 64)) | (1L << (DATABASES - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FIELDS - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GROUPING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)) | (1L << (ITEMS - 64)))) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & ((1L << (KEYS - 129)) | (1L << (LAST - 129)) | (1L << (LAZY - 129)) | (1L << (LIKE - 129)) | (1L << (LIMIT - 129)) | (1L << (LINES - 129)) | (1L << (LIST - 129)) | (1L << (LOAD - 129)) | (1L << (LOCAL - 129)) | (1L << (LOCATION - 129)) | (1L << (LOCK - 129)) | (1L << (LOCKS - 129)) | (1L << (LOGICAL - 129)) | (1L << (MACRO - 129)) | (1L << (MAP - 129)) | (1L << (MATCHED - 129)) | (1L << (MERGE - 129)) | (1L << (MINUTE - 129)) | (1L << (MONTH - 129)) | (1L << (MSCK - 129)) | (1L << (NAMESPACE - 129)) | (1L << (NAMESPACES - 129)) | (1L << (NO - 129)) | (1L << (NULLS - 129)) | (1L << (OF - 129)) | (1L << (OPTION - 129)) | (1L << (OPTIONS - 129)) | (1L << (OUT - 129)) | (1L << (OUTPUTFORMAT - 129)) | (1L << (OVER - 129)) | (1L << (OVERLAY - 129)) | (1L << (OVERWRITE - 129)) | (1L << (PARTITION - 129)) | (1L << (PARTITIONED - 129)) | (1L << (PARTITIONS - 129)) | (1L << (PERCENTLIT - 129)) | (1L << (PIVOT - 129)) | (1L << (PLACING - 129)) | (1L << (POSITION - 129)) | (1L << (PRECEDING - 129)) | (1L << (PRINCIPALS - 129)) | (1L << (PROPERTIES - 129)) | (1L << (PURGE - 129)) | (1L << (QUERY - 129)) | (1L << (RANGE - 129)) | (1L << (RECORDREADER - 129)) | (1L << (RECORDWRITER - 129)) | (1L << (RECOVER - 129)) | (1L << (REDUCE - 129)) | (1L << (REFRESH - 129)))) != 0) || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & ((1L << (RENAME - 193)) | (1L << (REPAIR - 193)) | (1L << (REPLACE - 193)) | (1L << (RESET - 193)) | (1L << (RESPECT - 193)) | (1L << (RESTRICT - 193)) | (1L << (REVOKE - 193)) | (1L << (RLIKE - 193)) | (1L << (ROLE - 193)) | (1L << (ROLES - 193)) | (1L << (ROLLBACK - 193)) | (1L << (ROLLUP - 193)) | (1L << (ROW - 193)) | (1L << (ROWS - 193)) | (1L << (SECOND - 193)) | (1L << (SCHEMA - 193)) | (1L << (SEMI - 193)) | (1L << (SEPARATED - 193)) | (1L << (SERDE - 193)) | (1L << (SERDEPROPERTIES - 193)) | (1L << (SET - 193)) | (1L << (SETMINUS - 193)) | (1L << (SETS - 193)) | (1L << (SHOW - 193)) | (1L << (SKEWED - 193)) | (1L << (SORT - 193)) | (1L << (SORTED - 193)) | (1L << (START - 193)) | (1L << (STATISTICS - 193)) | (1L << (STORED - 193)) | (1L << (STRATIFY - 193)) | (1L << (STRUCT - 193)) | (1L << (SUBSTR - 193)) | (1L << (SUBSTRING - 193)) | (1L << (SYNC - 193)) | (1L << (TABLES - 193)) | (1L << (TABLESAMPLE - 193)) | (1L << (TBLPROPERTIES - 193)) | (1L << (TEMPORARY - 193)) | (1L << (TERMINATED - 193)) | (1L << (TOUCH - 193)) | (1L << (TRANSACTION - 193)) | (1L << (TRANSACTIONS - 193)) | (1L << (TRANSFORM - 193)) | (1L << (TRIM - 193)) | (1L << (TRUE - 193)) | (1L << (TRUNCATE - 193)) | (1L << (TRY_CAST - 193)) | (1L << (TYPE - 193)) | (1L << (UNARCHIVE - 193)) | (1L << (UNBOUNDED - 193)) | (1L << (UNCACHE - 193)))) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (UNLOCK - 257)) | (1L << (UNSET - 257)) | (1L << (UPDATE - 257)) | (1L << (USE - 257)) | (1L << (VALUES - 257)) | (1L << (VIEW - 257)) | (1L << (VIEWS - 257)) | (1L << (WINDOW - 257)) | (1L << (YEAR - 257)) | (1L << (ZONE - 257)))) != 0)) ) {
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
		enterRule(_localctx, 292, RULE_strictNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3176);
			_la = _input.LA(1);
			if ( !(_la==ANTI || _la==CROSS || ((((_la - 84)) & ~0x3f) == 0 && ((1L << (_la - 84)) & ((1L << (EXCEPT - 84)) | (1L << (FULL - 84)) | (1L << (INNER - 84)) | (1L << (INTERSECT - 84)) | (1L << (JOIN - 84)) | (1L << (LATERAL - 84)) | (1L << (LEFT - 84)))) != 0) || ((((_la - 154)) & ~0x3f) == 0 && ((1L << (_la - 154)) & ((1L << (NATURAL - 154)) | (1L << (ON - 154)) | (1L << (RIGHT - 154)) | (1L << (SEMI - 154)) | (1L << (SETMINUS - 154)))) != 0) || _la==UNION || _la==USING) ) {
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
		public TerminalNode DAY() { return getToken(ArcticExtendSparkSqlParser.DAY, 0); }
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
		public TerminalNode PERCENTLIT() { return getToken(ArcticExtendSparkSqlParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(ArcticExtendSparkSqlParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(ArcticExtendSparkSqlParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(ArcticExtendSparkSqlParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(ArcticExtendSparkSqlParser.PRECEDING, 0); }
		public TerminalNode PRIMARY() { return getToken(ArcticExtendSparkSqlParser.PRIMARY, 0); }
		public TerminalNode PRINCIPALS() { return getToken(ArcticExtendSparkSqlParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(ArcticExtendSparkSqlParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(ArcticExtendSparkSqlParser.PURGE, 0); }
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
		public TerminalNode TABLE() { return getToken(ArcticExtendSparkSqlParser.TABLE, 0); }
		public TerminalNode TABLES() { return getToken(ArcticExtendSparkSqlParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(ArcticExtendSparkSqlParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(ArcticExtendSparkSqlParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(ArcticExtendSparkSqlParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(ArcticExtendSparkSqlParser.TERMINATED, 0); }
		public TerminalNode THEN() { return getToken(ArcticExtendSparkSqlParser.THEN, 0); }
		public TerminalNode TIME() { return getToken(ArcticExtendSparkSqlParser.TIME, 0); }
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
		public TerminalNode VIEW() { return getToken(ArcticExtendSparkSqlParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(ArcticExtendSparkSqlParser.VIEWS, 0); }
		public TerminalNode WHEN() { return getToken(ArcticExtendSparkSqlParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(ArcticExtendSparkSqlParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(ArcticExtendSparkSqlParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(ArcticExtendSparkSqlParser.WITH, 0); }
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
		enterRule(_localctx, 294, RULE_nonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(3178);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALL) | (1L << ALTER) | (1L << ANALYZE) | (1L << AND) | (1L << ANY) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << AS) | (1L << ASC) | (1L << AT) | (1L << AUTHORIZATION) | (1L << BETWEEN) | (1L << BOTH) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CASE) | (1L << CAST) | (1L << CHANGE) | (1L << CHECK) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLATE) | (1L << COLLECTION) | (1L << COLUMN) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << CONSTRAINT) | (1L << COST) | (1L << CREATE) | (1L << CUBE) | (1L << CURRENT) | (1L << CURRENT_DATE) | (1L << CURRENT_TIME) | (1L << CURRENT_TIMESTAMP) | (1L << CURRENT_USER) | (1L << DAY))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATA - 64)) | (1L << (DATABASE - 64)) | (1L << (DATABASES - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTINCT - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ELSE - 64)) | (1L << (END - 64)) | (1L << (ESCAPE - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FALSE - 64)) | (1L << (FETCH - 64)) | (1L << (FIELDS - 64)) | (1L << (FILTER - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FOR - 64)) | (1L << (FOREIGN - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FROM - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GRANT - 64)) | (1L << (GROUP - 64)) | (1L << (GROUPING - 64)) | (1L << (HAVING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (IN - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)) | (1L << (INTO - 64)) | (1L << (IS - 64)) | (1L << (ITEMS - 64)))) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & ((1L << (KEYS - 129)) | (1L << (LAST - 129)) | (1L << (LAZY - 129)) | (1L << (LEADING - 129)) | (1L << (LIKE - 129)) | (1L << (LIMIT - 129)) | (1L << (LINES - 129)) | (1L << (LIST - 129)) | (1L << (LOAD - 129)) | (1L << (LOCAL - 129)) | (1L << (LOCATION - 129)) | (1L << (LOCK - 129)) | (1L << (LOCKS - 129)) | (1L << (LOGICAL - 129)) | (1L << (MACRO - 129)) | (1L << (MAP - 129)) | (1L << (MATCHED - 129)) | (1L << (MERGE - 129)) | (1L << (MINUTE - 129)) | (1L << (MONTH - 129)) | (1L << (MSCK - 129)) | (1L << (NAMESPACE - 129)) | (1L << (NAMESPACES - 129)) | (1L << (NO - 129)) | (1L << (NOT - 129)) | (1L << (NULL - 129)) | (1L << (NULLS - 129)) | (1L << (OF - 129)) | (1L << (ONLY - 129)) | (1L << (OPTION - 129)) | (1L << (OPTIONS - 129)) | (1L << (OR - 129)) | (1L << (ORDER - 129)) | (1L << (OUT - 129)) | (1L << (OUTER - 129)) | (1L << (OUTPUTFORMAT - 129)) | (1L << (OVER - 129)) | (1L << (OVERLAPS - 129)) | (1L << (OVERLAY - 129)) | (1L << (OVERWRITE - 129)) | (1L << (PARTITION - 129)) | (1L << (PARTITIONED - 129)) | (1L << (PARTITIONS - 129)) | (1L << (PERCENTLIT - 129)) | (1L << (PIVOT - 129)) | (1L << (PLACING - 129)) | (1L << (POSITION - 129)) | (1L << (PRECEDING - 129)) | (1L << (PRIMARY - 129)) | (1L << (PRINCIPALS - 129)) | (1L << (PROPERTIES - 129)) | (1L << (PURGE - 129)) | (1L << (QUERY - 129)) | (1L << (RANGE - 129)) | (1L << (RECORDREADER - 129)) | (1L << (RECORDWRITER - 129)) | (1L << (RECOVER - 129)) | (1L << (REDUCE - 129)) | (1L << (REFERENCES - 129)) | (1L << (REFRESH - 129)))) != 0) || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & ((1L << (RENAME - 193)) | (1L << (REPAIR - 193)) | (1L << (REPLACE - 193)) | (1L << (RESET - 193)) | (1L << (RESPECT - 193)) | (1L << (RESTRICT - 193)) | (1L << (REVOKE - 193)) | (1L << (RLIKE - 193)) | (1L << (ROLE - 193)) | (1L << (ROLES - 193)) | (1L << (ROLLBACK - 193)) | (1L << (ROLLUP - 193)) | (1L << (ROW - 193)) | (1L << (ROWS - 193)) | (1L << (SECOND - 193)) | (1L << (SCHEMA - 193)) | (1L << (SELECT - 193)) | (1L << (SEPARATED - 193)) | (1L << (SERDE - 193)) | (1L << (SERDEPROPERTIES - 193)) | (1L << (SESSION_USER - 193)) | (1L << (SET - 193)) | (1L << (SETS - 193)) | (1L << (SHOW - 193)) | (1L << (SKEWED - 193)) | (1L << (SOME - 193)) | (1L << (SORT - 193)) | (1L << (SORTED - 193)) | (1L << (START - 193)) | (1L << (STATISTICS - 193)) | (1L << (STORED - 193)) | (1L << (STRATIFY - 193)) | (1L << (STRUCT - 193)) | (1L << (SUBSTR - 193)) | (1L << (SUBSTRING - 193)) | (1L << (SYNC - 193)) | (1L << (TABLE - 193)) | (1L << (TABLES - 193)) | (1L << (TABLESAMPLE - 193)) | (1L << (TBLPROPERTIES - 193)) | (1L << (TEMPORARY - 193)) | (1L << (TERMINATED - 193)) | (1L << (THEN - 193)) | (1L << (TIME - 193)) | (1L << (TO - 193)) | (1L << (TOUCH - 193)) | (1L << (TRAILING - 193)) | (1L << (TRANSACTION - 193)) | (1L << (TRANSACTIONS - 193)) | (1L << (TRANSFORM - 193)) | (1L << (TRIM - 193)) | (1L << (TRUE - 193)) | (1L << (TRUNCATE - 193)) | (1L << (TRY_CAST - 193)) | (1L << (TYPE - 193)) | (1L << (UNARCHIVE - 193)) | (1L << (UNBOUNDED - 193)) | (1L << (UNCACHE - 193)) | (1L << (UNIQUE - 193)) | (1L << (UNKNOWN - 193)))) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (UNLOCK - 257)) | (1L << (UNSET - 257)) | (1L << (UPDATE - 257)) | (1L << (USE - 257)) | (1L << (USER - 257)) | (1L << (VALUES - 257)) | (1L << (VIEW - 257)) | (1L << (VIEWS - 257)) | (1L << (WHEN - 257)) | (1L << (WHERE - 257)) | (1L << (WINDOW - 257)) | (1L << (WITH - 257)) | (1L << (YEAR - 257)) | (1L << (ZONE - 257)))) != 0)) ) {
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
		case 47:
			return queryTerm_sempred((QueryTermContext)_localctx, predIndex);
		case 106:
			return booleanExpression_sempred((BooleanExpressionContext)_localctx, predIndex);
		case 108:
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
		"\u0004\u0001\u0131\u0c6d\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
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
		"\u0092\u0002\u0093\u0007\u0093\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003"+
		"\u0002\u0132\b\u0002\u0001\u0002\u0003\u0002\u0135\b\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0003\u0004\u013f\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u0004\u0144\b\u0004\u0001\u0005\u0001\u0005\u0005\u0005\u0148\b"+
		"\u0005\n\u0005\f\u0005\u014b\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0003\f\u0163\b\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u0168\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u0170\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005"+
		"\f\u0178\b\f\n\f\f\f\u017b\t\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0003\f\u018e\b\f\u0001\f\u0001\f\u0003\f\u0192"+
		"\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0198\b\f\u0001\f\u0003\f"+
		"\u019b\b\f\u0001\f\u0003\f\u019e\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u01a5\b\f\u0001\f\u0003\f\u01a8\b\f\u0001\f\u0001\f\u0003\f"+
		"\u01ac\b\f\u0001\f\u0003\f\u01af\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u01b6\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0005\f\u01c1\b\f\n\f\f\f\u01c4\t\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0003\f\u01cb\b\f\u0001\f\u0003\f\u01ce\b\f\u0001"+
		"\f\u0001\f\u0003\f\u01d2\b\f\u0001\f\u0003\f\u01d5\b\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0003\f\u01db\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u01e6\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u01ec\b\f\u0001\f\u0001\f\u0001\f\u0003\f\u01f1\b\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u0231\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u023a\b\f\u0001\f\u0001\f\u0003\f\u023e\b\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0003\f\u0244\b\f\u0001\f\u0001\f\u0003\f\u0248\b\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u024d\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0253"+
		"\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u025f\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u0267\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u026d\b\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u027a\b\f\u0001\f\u0004\f\u027d\b\f\u000b\f"+
		"\f\f\u027e\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u028f\b\f\u0001"+
		"\f\u0001\f\u0001\f\u0005\f\u0294\b\f\n\f\f\f\u0297\t\f\u0001\f\u0003\f"+
		"\u029a\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u02a0\b\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u02af\b\f\u0001\f\u0001\f\u0003\f\u02b3\b\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u02b9\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u02bf\b\f\u0001\f\u0003\f\u02c2\b\f\u0001\f\u0003\f"+
		"\u02c5\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u02cb\b\f\u0001\f\u0001"+
		"\f\u0003\f\u02cf\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005"+
		"\f\u02d7\b\f\n\f\f\f\u02da\t\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0003\f\u02e2\b\f\u0001\f\u0003\f\u02e5\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u02ee\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u02f3\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u02f9\b\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0300\b\f\u0001\f\u0003"+
		"\f\u0303\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0309\b\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u0312\b\f\n\f"+
		"\f\f\u0315\t\f\u0003\f\u0317\b\f\u0001\f\u0001\f\u0003\f\u031b\b\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u0320\b\f\u0001\f\u0001\f\u0001\f\u0003\f\u0325"+
		"\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u032c\b\f\u0001\f"+
		"\u0003\f\u032f\b\f\u0001\f\u0003\f\u0332\b\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u0339\b\f\u0001\f\u0001\f\u0001\f\u0003\f\u033e\b\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0347"+
		"\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u034f\b\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0355\b\f\u0001\f\u0003\f\u0358"+
		"\b\f\u0001\f\u0003\f\u035b\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f"+
		"\u0361\b\f\u0001\f\u0001\f\u0003\f\u0365\b\f\u0001\f\u0001\f\u0003\f\u0369"+
		"\b\f\u0001\f\u0001\f\u0003\f\u036d\b\f\u0003\f\u036f\b\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0377\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0003\f\u037f\b\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u0385\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u038b\b\f"+
		"\u0001\f\u0003\f\u038e\b\f\u0001\f\u0001\f\u0003\f\u0392\b\f\u0001\f\u0003"+
		"\f\u0395\b\f\u0001\f\u0001\f\u0003\f\u0399\b\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0005\f\u03b3\b\f\n\f\f\f\u03b6\t\f\u0003\f"+
		"\u03b8\b\f\u0001\f\u0001\f\u0003\f\u03bc\b\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u03c2\b\f\u0001\f\u0003\f\u03c5\b\f\u0001\f\u0003\f\u03c8\b"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u03ce\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0003\f\u03d6\b\f\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u03db\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u03e1\b\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0003\f\u03e7\b\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u03ef\b\f\u0001\f\u0001\f\u0001\f\u0005\f\u03f4"+
		"\b\f\n\f\f\f\u03f7\t\f\u0001\f\u0001\f\u0001\f\u0005\f\u03fc\b\f\n\f\f"+
		"\f\u03ff\t\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u040d\b\f\n\f\f\f\u0410\t\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0005\f\u041b\b\f\n\f\f\f\u041e\t\f\u0003\f\u0420\b\f\u0001\f\u0001"+
		"\f\u0005\f\u0424\b\f\n\f\f\f\u0427\t\f\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0005\f\u042d\b\f\n\f\f\f\u0430\t\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005"+
		"\f\u0436\b\f\n\f\f\f\u0439\t\f\u0001\f\u0001\f\u0005\f\u043d\b\f\n\f\f"+
		"\f\u0440\t\f\u0003\f\u0442\b\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0003\u000f\u044e\b\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0452\b"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u0459\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0003\u000f\u04cd\b\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u04d5\b\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u04dd\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u04e6\b\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0003\u000f\u04f0\b\u000f\u0001\u0010\u0001\u0010\u0003\u0010\u04f4"+
		"\b\u0010\u0001\u0010\u0003\u0010\u04f7\b\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0003\u0010\u04fd\b\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0001\u0011\u0003\u0011\u0503\b\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u050f\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u051b\b\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0003\u0013\u0520\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0003\u0016\u0529\b\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u0531\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0003\u0017\u0538\b\u0017\u0003\u0017\u053a\b\u0017\u0001"+
		"\u0017\u0003\u0017\u053d\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003"+
		"\u0017\u0542\b\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0546\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u054b\b\u0017\u0001\u0017"+
		"\u0003\u0017\u054e\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017"+
		"\u0553\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0558\b"+
		"\u0017\u0001\u0017\u0003\u0017\u055b\b\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0003\u0017\u0560\b\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0564"+
		"\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0569\b\u0017"+
		"\u0003\u0017\u056b\b\u0017\u0001\u0018\u0001\u0018\u0003\u0018\u056f\b"+
		"\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005"+
		"\u0019\u0576\b\u0019\n\u0019\f\u0019\u0579\t\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u0580\b\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0003\u001c\u0589\b\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d"+
		"\u058e\b\u001d\n\u001d\f\u001d\u0591\t\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0005\u001e\u0597\b\u001e\n\u001e\f\u001e\u059a\t\u001e"+
		"\u0001\u001f\u0001\u001f\u0003\u001f\u059e\b\u001f\u0001\u001f\u0003\u001f"+
		"\u05a1\b\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001 "+
		"\u0001 \u0001 \u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u05b7\b!\n!\f!\u05ba\t!\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0005\"\u05c0\b\"\n\"\f\"\u05c3\t\"\u0001\""+
		"\u0001\"\u0001#\u0001#\u0003#\u05c9\b#\u0001#\u0003#\u05cc\b#\u0001$\u0001"+
		"$\u0001$\u0005$\u05d1\b$\n$\f$\u05d4\t$\u0001$\u0003$\u05d7\b$\u0001%"+
		"\u0001%\u0001%\u0001%\u0003%\u05dd\b%\u0001&\u0001&\u0001&\u0001&\u0005"+
		"&\u05e3\b&\n&\f&\u05e6\t&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0005\'\u05ee\b\'\n\'\f\'\u05f1\t\'\u0001\'\u0001\'\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0003(\u05fb\b(\u0001)\u0001)\u0001)\u0001)\u0001"+
		")\u0003)\u0602\b)\u0001*\u0001*\u0001*\u0001*\u0003*\u0608\b*\u0001+\u0001"+
		"+\u0001+\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0004,\u0613\b,\u000b"+
		",\f,\u0614\u0001,\u0001,\u0001,\u0001,\u0001,\u0003,\u061c\b,\u0001,\u0001"+
		",\u0001,\u0001,\u0001,\u0003,\u0623\b,\u0001,\u0001,\u0001,\u0001,\u0001"+
		",\u0001,\u0001,\u0001,\u0001,\u0001,\u0003,\u062f\b,\u0001,\u0001,\u0001"+
		",\u0001,\u0005,\u0635\b,\n,\f,\u0638\t,\u0001,\u0005,\u063b\b,\n,\f,\u063e"+
		"\t,\u0003,\u0640\b,\u0001-\u0001-\u0001-\u0001-\u0001-\u0005-\u0647\b"+
		"-\n-\f-\u064a\t-\u0003-\u064c\b-\u0001-\u0001-\u0001-\u0001-\u0001-\u0005"+
		"-\u0653\b-\n-\f-\u0656\t-\u0003-\u0658\b-\u0001-\u0001-\u0001-\u0001-"+
		"\u0001-\u0005-\u065f\b-\n-\f-\u0662\t-\u0003-\u0664\b-\u0001-\u0001-\u0001"+
		"-\u0001-\u0001-\u0005-\u066b\b-\n-\f-\u066e\t-\u0003-\u0670\b-\u0001-"+
		"\u0003-\u0673\b-\u0001-\u0001-\u0001-\u0003-\u0678\b-\u0003-\u067a\b-"+
		"\u0001.\u0001.\u0001.\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001"+
		"/\u0003/\u0686\b/\u0001/\u0001/\u0001/\u0001/\u0001/\u0003/\u068d\b/\u0001"+
		"/\u0001/\u0001/\u0001/\u0001/\u0003/\u0694\b/\u0001/\u0005/\u0697\b/\n"+
		"/\f/\u069a\t/\u00010\u00010\u00010\u00010\u00010\u00010\u00010\u00010"+
		"\u00010\u00030\u06a5\b0\u00011\u00011\u00031\u06a9\b1\u00011\u00011\u0003"+
		"1\u06ad\b1\u00012\u00012\u00042\u06b1\b2\u000b2\f2\u06b2\u00013\u0001"+
		"3\u00033\u06b7\b3\u00013\u00013\u00013\u00013\u00053\u06bd\b3\n3\f3\u06c0"+
		"\t3\u00013\u00033\u06c3\b3\u00013\u00033\u06c6\b3\u00013\u00033\u06c9"+
		"\b3\u00013\u00033\u06cc\b3\u00013\u00013\u00033\u06d0\b3\u00014\u0001"+
		"4\u00034\u06d4\b4\u00014\u00054\u06d7\b4\n4\f4\u06da\t4\u00014\u00034"+
		"\u06dd\b4\u00014\u00034\u06e0\b4\u00014\u00034\u06e3\b4\u00014\u00034"+
		"\u06e6\b4\u00014\u00014\u00034\u06ea\b4\u00014\u00054\u06ed\b4\n4\f4\u06f0"+
		"\t4\u00014\u00034\u06f3\b4\u00014\u00034\u06f6\b4\u00014\u00034\u06f9"+
		"\b4\u00014\u00034\u06fc\b4\u00034\u06fe\b4\u00015\u00015\u00015\u0001"+
		"5\u00035\u0704\b5\u00015\u00015\u00015\u00015\u00015\u00035\u070b\b5\u0001"+
		"5\u00015\u00015\u00035\u0710\b5\u00015\u00035\u0713\b5\u00015\u00035\u0716"+
		"\b5\u00015\u00015\u00035\u071a\b5\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00015\u00035\u0724\b5\u00015\u00015\u00035\u0728\b5\u00035\u072a"+
		"\b5\u00015\u00035\u072d\b5\u00015\u00015\u00035\u0731\b5\u00016\u0001"+
		"6\u00056\u0735\b6\n6\f6\u0738\t6\u00016\u00036\u073b\b6\u00016\u00016"+
		"\u00017\u00017\u00017\u00018\u00018\u00018\u00018\u00038\u0746\b8\u0001"+
		"8\u00018\u00018\u00019\u00019\u00019\u00019\u00019\u00039\u0750\b9\u0001"+
		"9\u00019\u00019\u0001:\u0001:\u0001:\u0001:\u0001:\u0001:\u0001:\u0003"+
		":\u075c\b:\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0005;\u0769\b;\n;\f;\u076c\t;\u0001;\u0001;\u0003;\u0770"+
		"\b;\u0001<\u0001<\u0001<\u0005<\u0775\b<\n<\f<\u0778\t<\u0001=\u0001="+
		"\u0001=\u0001=\u0001>\u0001>\u0001>\u0001?\u0001?\u0001?\u0001@\u0001"+
		"@\u0001@\u0003@\u0787\b@\u0001@\u0005@\u078a\b@\n@\f@\u078d\t@\u0001@"+
		"\u0001@\u0001A\u0001A\u0001A\u0001A\u0001A\u0001A\u0005A\u0797\bA\nA\f"+
		"A\u079a\tA\u0001A\u0001A\u0003A\u079e\bA\u0001B\u0001B\u0001B\u0001B\u0005"+
		"B\u07a4\bB\nB\fB\u07a7\tB\u0001B\u0005B\u07aa\bB\nB\fB\u07ad\tB\u0001"+
		"B\u0003B\u07b0\bB\u0001C\u0001C\u0001C\u0001C\u0001C\u0005C\u07b7\bC\n"+
		"C\fC\u07ba\tC\u0001C\u0001C\u0001C\u0001C\u0001C\u0005C\u07c1\bC\nC\f"+
		"C\u07c4\tC\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001"+
		"C\u0001C\u0005C\u07d0\bC\nC\fC\u07d3\tC\u0001C\u0001C\u0003C\u07d7\bC"+
		"\u0003C\u07d9\bC\u0001D\u0001D\u0003D\u07dd\bD\u0001E\u0001E\u0001E\u0001"+
		"E\u0001E\u0005E\u07e4\bE\nE\fE\u07e7\tE\u0001E\u0001E\u0001E\u0001E\u0001"+
		"E\u0001E\u0001E\u0001E\u0005E\u07f1\bE\nE\fE\u07f4\tE\u0001E\u0001E\u0003"+
		"E\u07f8\bE\u0001F\u0001F\u0003F\u07fc\bF\u0001G\u0001G\u0001G\u0001G\u0005"+
		"G\u0802\bG\nG\fG\u0805\tG\u0003G\u0807\bG\u0001G\u0001G\u0003G\u080b\b"+
		"G\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001"+
		"H\u0005H\u0817\bH\nH\fH\u081a\tH\u0001H\u0001H\u0001H\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0005I\u0824\bI\nI\fI\u0827\tI\u0001I\u0001I\u0003I\u082b"+
		"\bI\u0001J\u0001J\u0003J\u082f\bJ\u0001J\u0003J\u0832\bJ\u0001K\u0001"+
		"K\u0001K\u0003K\u0837\bK\u0001K\u0001K\u0001K\u0001K\u0001K\u0005K\u083e"+
		"\bK\nK\fK\u0841\tK\u0003K\u0843\bK\u0001K\u0001K\u0001K\u0003K\u0848\b"+
		"K\u0001K\u0001K\u0001K\u0005K\u084d\bK\nK\fK\u0850\tK\u0003K\u0852\bK"+
		"\u0001L\u0001L\u0001M\u0003M\u0857\bM\u0001M\u0001M\u0005M\u085b\bM\n"+
		"M\fM\u085e\tM\u0001N\u0001N\u0001N\u0003N\u0863\bN\u0001N\u0001N\u0003"+
		"N\u0867\bN\u0001N\u0001N\u0001N\u0001N\u0003N\u086d\bN\u0001N\u0001N\u0003"+
		"N\u0871\bN\u0001O\u0003O\u0874\bO\u0001O\u0001O\u0001O\u0003O\u0879\b"+
		"O\u0001O\u0003O\u087c\bO\u0001O\u0001O\u0001O\u0003O\u0881\bO\u0001O\u0001"+
		"O\u0003O\u0885\bO\u0001O\u0003O\u0888\bO\u0001O\u0003O\u088b\bO\u0001"+
		"P\u0001P\u0001P\u0001P\u0003P\u0891\bP\u0001Q\u0001Q\u0001Q\u0003Q\u0896"+
		"\bQ\u0001Q\u0001Q\u0001R\u0003R\u089b\bR\u0001R\u0001R\u0001R\u0001R\u0001"+
		"R\u0001R\u0001R\u0001R\u0001R\u0001R\u0001R\u0001R\u0001R\u0001R\u0001"+
		"R\u0001R\u0003R\u08ad\bR\u0003R\u08af\bR\u0001R\u0003R\u08b2\bR\u0001"+
		"S\u0001S\u0001S\u0001S\u0001T\u0001T\u0001T\u0005T\u08bb\bT\nT\fT\u08be"+
		"\tT\u0001U\u0001U\u0001U\u0001U\u0005U\u08c4\bU\nU\fU\u08c7\tU\u0001U"+
		"\u0001U\u0001V\u0001V\u0003V\u08cd\bV\u0001W\u0001W\u0001W\u0001W\u0005"+
		"W\u08d3\bW\nW\fW\u08d6\tW\u0001W\u0001W\u0001X\u0001X\u0003X\u08dc\bX"+
		"\u0001Y\u0001Y\u0003Y\u08e0\bY\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001"+
		"Y\u0003Y\u08e8\bY\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0003Y\u08f0"+
		"\bY\u0001Y\u0001Y\u0001Y\u0001Y\u0003Y\u08f6\bY\u0001Z\u0001Z\u0001Z\u0001"+
		"Z\u0005Z\u08fc\bZ\nZ\fZ\u08ff\tZ\u0001Z\u0001Z\u0001[\u0001[\u0001[\u0001"+
		"[\u0001[\u0005[\u0908\b[\n[\f[\u090b\t[\u0003[\u090d\b[\u0001[\u0001["+
		"\u0001[\u0001\\\u0003\\\u0913\b\\\u0001\\\u0001\\\u0003\\\u0917\b\\\u0003"+
		"\\\u0919\b\\\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0003]\u0922"+
		"\b]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001"+
		"]\u0003]\u092e\b]\u0003]\u0930\b]\u0001]\u0001]\u0001]\u0001]\u0001]\u0003"+
		"]\u0937\b]\u0001]\u0001]\u0001]\u0001]\u0001]\u0003]\u093e\b]\u0001]\u0001"+
		"]\u0001]\u0001]\u0003]\u0944\b]\u0001]\u0001]\u0001]\u0001]\u0003]\u094a"+
		"\b]\u0003]\u094c\b]\u0001^\u0001^\u0001^\u0005^\u0951\b^\n^\f^\u0954\t"+
		"^\u0001_\u0001_\u0001_\u0005_\u0959\b_\n_\f_\u095c\t_\u0001`\u0001`\u0001"+
		"`\u0003`\u0961\b`\u0001`\u0001`\u0001a\u0001a\u0001a\u0003a\u0968\ba\u0001"+
		"a\u0001a\u0001b\u0001b\u0003b\u096e\bb\u0001b\u0001b\u0003b\u0972\bb\u0003"+
		"b\u0974\bb\u0001c\u0001c\u0001c\u0005c\u0979\bc\nc\fc\u097c\tc\u0001d"+
		"\u0001d\u0001d\u0001d\u0005d\u0982\bd\nd\fd\u0985\td\u0001d\u0001d\u0001"+
		"e\u0001e\u0003e\u098b\be\u0001f\u0001f\u0001f\u0001f\u0001f\u0001f\u0005"+
		"f\u0993\bf\nf\ff\u0996\tf\u0001f\u0001f\u0003f\u099a\bf\u0001g\u0001g"+
		"\u0003g\u099e\bg\u0001h\u0001h\u0001i\u0001i\u0001i\u0005i\u09a5\bi\n"+
		"i\fi\u09a8\ti\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j"+
		"\u0001j\u0001j\u0003j\u09b4\bj\u0003j\u09b6\bj\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0005j\u09be\bj\nj\fj\u09c1\tj\u0001k\u0003k\u09c4\bk"+
		"\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0003k\u09cc\bk\u0001k\u0001"+
		"k\u0001k\u0001k\u0001k\u0005k\u09d3\bk\nk\fk\u09d6\tk\u0001k\u0001k\u0001"+
		"k\u0003k\u09db\bk\u0001k\u0001k\u0001k\u0001k\u0001k\u0001k\u0003k\u09e3"+
		"\bk\u0001k\u0001k\u0001k\u0003k\u09e8\bk\u0001k\u0001k\u0001k\u0001k\u0001"+
		"k\u0001k\u0001k\u0001k\u0005k\u09f2\bk\nk\fk\u09f5\tk\u0001k\u0001k\u0003"+
		"k\u09f9\bk\u0001k\u0003k\u09fc\bk\u0001k\u0001k\u0001k\u0001k\u0003k\u0a02"+
		"\bk\u0001k\u0001k\u0003k\u0a06\bk\u0001k\u0001k\u0001k\u0003k\u0a0b\b"+
		"k\u0001k\u0001k\u0001k\u0003k\u0a10\bk\u0001k\u0001k\u0001k\u0003k\u0a15"+
		"\bk\u0001l\u0001l\u0001l\u0001l\u0003l\u0a1b\bl\u0001l\u0001l\u0001l\u0001"+
		"l\u0001l\u0001l\u0001l\u0001l\u0001l\u0001l\u0001l\u0001l\u0001l\u0001"+
		"l\u0001l\u0001l\u0001l\u0001l\u0001l\u0005l\u0a30\bl\nl\fl\u0a33\tl\u0001"+
		"m\u0001m\u0001m\u0001m\u0004m\u0a39\bm\u000bm\fm\u0a3a\u0001m\u0001m\u0003"+
		"m\u0a3f\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0004m\u0a46\bm\u000bm\f"+
		"m\u0a47\u0001m\u0001m\u0003m\u0a4c\bm\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0005"+
		"m\u0a5c\bm\nm\fm\u0a5f\tm\u0003m\u0a61\bm\u0001m\u0001m\u0001m\u0001m"+
		"\u0001m\u0001m\u0003m\u0a69\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0003m\u0a72\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0004m\u0a87\bm\u000bm\fm\u0a88\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0003m\u0a94\bm\u0001m\u0001m\u0001"+
		"m\u0005m\u0a99\bm\nm\fm\u0a9c\tm\u0003m\u0a9e\bm\u0001m\u0001m\u0001m"+
		"\u0001m\u0001m\u0001m\u0001m\u0003m\u0aa7\bm\u0001m\u0001m\u0003m\u0aab"+
		"\bm\u0001m\u0001m\u0003m\u0aaf\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0004m\u0ab9\bm\u000bm\fm\u0aba\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0003"+
		"m\u0ad4\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0003m\u0adb\bm\u0001m\u0003"+
		"m\u0ade\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0003m\u0aed\bm\u0001m\u0001m\u0003m\u0af1"+
		"\bm\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001m\u0005m\u0afb"+
		"\bm\nm\fm\u0afe\tm\u0001n\u0001n\u0001n\u0001n\u0001n\u0001n\u0001n\u0001"+
		"n\u0004n\u0b08\bn\u000bn\fn\u0b09\u0003n\u0b0c\bn\u0001o\u0001o\u0001"+
		"p\u0001p\u0001q\u0001q\u0001r\u0001r\u0001s\u0001s\u0001s\u0003s\u0b19"+
		"\bs\u0001t\u0001t\u0003t\u0b1d\bt\u0001u\u0001u\u0001u\u0004u\u0b22\b"+
		"u\u000bu\fu\u0b23\u0001v\u0001v\u0001v\u0003v\u0b29\bv\u0001w\u0001w\u0001"+
		"w\u0001w\u0001w\u0001x\u0003x\u0b31\bx\u0001x\u0001x\u0001y\u0001y\u0001"+
		"y\u0003y\u0b38\by\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001"+
		"z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0003z\u0b49\bz\u0001"+
		"z\u0001z\u0003z\u0b4d\bz\u0001z\u0001z\u0001z\u0001z\u0003z\u0b53\bz\u0001"+
		"z\u0001z\u0001z\u0001z\u0003z\u0b59\bz\u0001z\u0001z\u0001z\u0001z\u0001"+
		"z\u0005z\u0b60\bz\nz\fz\u0b63\tz\u0001z\u0003z\u0b66\bz\u0003z\u0b68\b"+
		"z\u0001{\u0001{\u0001{\u0005{\u0b6d\b{\n{\f{\u0b70\t{\u0001|\u0001|\u0001"+
		"|\u0001|\u0003|\u0b76\b|\u0001|\u0003|\u0b79\b|\u0001|\u0003|\u0b7c\b"+
		"|\u0001}\u0001}\u0001}\u0005}\u0b81\b}\n}\f}\u0b84\t}\u0001~\u0001~\u0001"+
		"~\u0001~\u0003~\u0b8a\b~\u0001~\u0003~\u0b8d\b~\u0001\u007f\u0001\u007f"+
		"\u0001\u007f\u0005\u007f\u0b92\b\u007f\n\u007f\f\u007f\u0b95\t\u007f\u0001"+
		"\u0080\u0001\u0080\u0003\u0080\u0b99\b\u0080\u0001\u0080\u0001\u0080\u0001"+
		"\u0080\u0003\u0080\u0b9e\b\u0080\u0001\u0080\u0003\u0080\u0ba1\b\u0080"+
		"\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0081\u0001\u0082"+
		"\u0001\u0082\u0001\u0082\u0001\u0082\u0005\u0082\u0bac\b\u0082\n\u0082"+
		"\f\u0082\u0baf\t\u0082\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084"+
		"\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0001\u0084\u0005\u0084"+
		"\u0bc0\b\u0084\n\u0084\f\u0084\u0bc3\t\u0084\u0001\u0084\u0001\u0084\u0001"+
		"\u0084\u0001\u0084\u0001\u0084\u0005\u0084\u0bca\b\u0084\n\u0084\f\u0084"+
		"\u0bcd\t\u0084\u0003\u0084\u0bcf\b\u0084\u0001\u0084\u0001\u0084\u0001"+
		"\u0084\u0001\u0084\u0001\u0084\u0005\u0084\u0bd6\b\u0084\n\u0084\f\u0084"+
		"\u0bd9\t\u0084\u0003\u0084\u0bdb\b\u0084\u0003\u0084\u0bdd\b\u0084\u0001"+
		"\u0084\u0003\u0084\u0be0\b\u0084\u0001\u0084\u0003\u0084\u0be3\b\u0084"+
		"\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085"+
		"\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085"+
		"\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0003\u0085\u0bf5\b\u0085"+
		"\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086"+
		"\u0001\u0086\u0003\u0086\u0bfe\b\u0086\u0001\u0087\u0001\u0087\u0001\u0087"+
		"\u0005\u0087\u0c03\b\u0087\n\u0087\f\u0087\u0c06\t\u0087\u0001\u0088\u0001"+
		"\u0088\u0001\u0088\u0001\u0088\u0003\u0088\u0c0c\b\u0088\u0001\u0089\u0001"+
		"\u0089\u0001\u0089\u0005\u0089\u0c11\b\u0089\n\u0089\f\u0089\u0c14\t\u0089"+
		"\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008b\u0001\u008b\u0004\u008b"+
		"\u0c1b\b\u008b\u000b\u008b\f\u008b\u0c1c\u0001\u008b\u0003\u008b\u0c20"+
		"\b\u008b\u0001\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u0c25\b\u008c"+
		"\u0001\u008d\u0001\u008d\u0001\u008d\u0001\u008d\u0001\u008d\u0001\u008d"+
		"\u0003\u008d\u0c2d\b\u008d\u0001\u008e\u0001\u008e\u0001\u008f\u0001\u008f"+
		"\u0003\u008f\u0c33\b\u008f\u0001\u008f\u0001\u008f\u0001\u008f\u0003\u008f"+
		"\u0c38\b\u008f\u0001\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0c3d\b"+
		"\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0c41\b\u008f\u0001\u008f\u0001"+
		"\u008f\u0003\u008f\u0c45\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0c49"+
		"\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0c4d\b\u008f\u0001\u008f"+
		"\u0001\u008f\u0003\u008f\u0c51\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f"+
		"\u0c55\b\u008f\u0001\u008f\u0001\u008f\u0003\u008f\u0c59\b\u008f\u0001"+
		"\u008f\u0003\u008f\u0c5c\b\u008f\u0001\u0090\u0001\u0090\u0001\u0090\u0001"+
		"\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0003\u0090\u0c65\b\u0090\u0001"+
		"\u0091\u0001\u0091\u0001\u0092\u0001\u0092\u0001\u0093\u0001\u0093\u0001"+
		"\u0093\t\u03b4\u03f5\u03fd\u040e\u041c\u0425\u042e\u0437\u043e\u0004^"+
		"\u00d4\u00d8\u00da\u0094\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0"+
		"\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8"+
		"\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0"+
		"\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108"+
		"\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118\u011a\u011c\u011e\u0120"+
		"\u0122\u0124\u0126\u00003\u0002\u0000CC\u00b7\u00b7\u0002\u0000\"\"\u00c6"+
		"\u00c6\u0002\u0000BB\u0099\u0099\u0002\u0000ggtt\u0001\u0000-.\u0002\u0000"+
		"\u00e8\u00e8\u0108\u0108\u0002\u0000\u0011\u0011%%\u0005\u0000**66YYf"+
		"f\u0090\u0090\u0001\u0000GH\u0002\u0000YYff\u0002\u0000\u009d\u009d\u0122"+
		"\u0122\u0003\u0000\u000e\u000eOO\u00e7\u00e7\u0002\u0000\u000e\u000e\u008a"+
		"\u008a\u0002\u0000\u008c\u008c\u0122\u0122\u0003\u0000AA\u0098\u0098\u00d1"+
		"\u00d1\u0004\u0000TT{{\u00d9\u00d9\u00fe\u00fe\u0003\u0000TT\u00d9\u00d9"+
		"\u00fe\u00fe\u0002\u0000\u0019\u0019GG\u0002\u0000aa\u0082\u0082\u0002"+
		"\u000099\u00cd\u00cd\u0002\u0000\u0010\u0010LL\u0002\u0000\u0126\u0126"+
		"\u0128\u0128\u0003\u0000\u0010\u0010\u0015\u0015\u00dd\u00dd\u0003\u0000"+
		"\\\\\u00f7\u00f7\u0100\u0100\u0002\u0000\u0118\u0119\u011d\u011d\u0002"+
		"\u0000NN\u011a\u011c\u0002\u0000\u0118\u0119\u0120\u0120\u0002\u0000;"+
		";=>\u0002\u0000$$\u00f9\u00f9\u0002\u0000rr\u00c5\u00c5\u0001\u0000\u00e5"+
		"\u00e6\u0002\u0000\u0002\u0002gg\u0002\u0000\u0002\u0002cc\u0003\u0000"+
		"\u001d\u001d\u0085\u0085\u00f2\u00f2\u0001\u0000\u0110\u0117\u0002\u0000"+
		"NN\u0118\u0121\u0004\u0000\u0013\u0013tt\u009c\u009c\u00a4\u00a4\u0002"+
		"\u0000\\\\\u00f7\u00f7\u0001\u0000\u0118\u0119\u0003\u0000\u0122\u0122"+
		"\u0126\u0126\u0128\u0128\u0002\u0000\u0096\u0096\u010e\u010e\u0004\u0000"+
		"??pp\u0095\u0095\u00d0\u00d0\u0003\u0000pp\u0095\u0095\u00d0\u00d0\u0002"+
		"\u0000MM\u00ad\u00ad\u0002\u0000\u00a5\u00a5\u00de\u00de\u0002\u0000b"+
		"b\u00b4\u00b4\u0001\u0000\u0127\u0128\u0002\u0000OO\u00d8\u00d81\u0000"+
		"\u000e\u000f\u0011\u0012\u0014\u0014\u0016\u0017\u0019\u001a\u001c\u001c"+
		"\u001e\"%%\'*,,.4669:?KMOSSU[^^`befiknnpsuvxz||\u007f\u007f\u0081\u0082"+
		"\u0084\u0084\u0087\u0099\u009b\u009b\u009e\u009f\u00a2\u00a3\u00a6\u00a6"+
		"\u00a8\u00a9\u00ab\u00b4\u00b6\u00be\u00c0\u00c7\u00c9\u00d1\u00d3\u00d6"+
		"\u00d8\u00dc\u00de\u00e7\u00e9\u00ed\u00f1\u00f1\u00f3\u00fd\u0101\u0104"+
		"\u0107\u0109\u010c\u010c\u010e\u010f\u0010\u0000\u0014\u001488TThhww{"+
		"{\u0080\u0080\u0083\u0083\u0086\u0086\u009a\u009a\u00a0\u00a0\u00c8\u00c8"+
		"\u00d3\u00d3\u00d9\u00d9\u00fe\u00fe\u0106\u0106\u0011\u0000\u000e\u0013"+
		"\u001579SUgivxz|\u007f\u0081\u0082\u0084\u0085\u0087\u0099\u009b\u009f"+
		"\u00a1\u00c7\u00c9\u00d2\u00d4\u00d8\u00da\u00fd\u00ff\u0105\u0107\u010f"+
		"\u0e55\u0000\u0128\u0001\u0000\u0000\u0000\u0002\u012a\u0001\u0000\u0000"+
		"\u0000\u0004\u012c\u0001\u0000\u0000\u0000\u0006\u0136\u0001\u0000\u0000"+
		"\u0000\b\u0143\u0001\u0000\u0000\u0000\n\u0145\u0001\u0000\u0000\u0000"+
		"\f\u014e\u0001\u0000\u0000\u0000\u000e\u0151\u0001\u0000\u0000\u0000\u0010"+
		"\u0154\u0001\u0000\u0000\u0000\u0012\u0157\u0001\u0000\u0000\u0000\u0014"+
		"\u015a\u0001\u0000\u0000\u0000\u0016\u015d\u0001\u0000\u0000\u0000\u0018"+
		"\u0441\u0001\u0000\u0000\u0000\u001a\u0443\u0001\u0000\u0000\u0000\u001c"+
		"\u0445\u0001\u0000\u0000\u0000\u001e\u04ef\u0001\u0000\u0000\u0000 \u04f1"+
		"\u0001\u0000\u0000\u0000\"\u0502\u0001\u0000\u0000\u0000$\u0508\u0001"+
		"\u0000\u0000\u0000&\u0514\u0001\u0000\u0000\u0000(\u0521\u0001\u0000\u0000"+
		"\u0000*\u0524\u0001\u0000\u0000\u0000,\u0528\u0001\u0000\u0000\u0000."+
		"\u056a\u0001\u0000\u0000\u00000\u056c\u0001\u0000\u0000\u00002\u0570\u0001"+
		"\u0000\u0000\u00004\u057c\u0001\u0000\u0000\u00006\u0581\u0001\u0000\u0000"+
		"\u00008\u0588\u0001\u0000\u0000\u0000:\u058a\u0001\u0000\u0000\u0000<"+
		"\u0592\u0001\u0000\u0000\u0000>\u059b\u0001\u0000\u0000\u0000@\u05a6\u0001"+
		"\u0000\u0000\u0000B\u05b8\u0001\u0000\u0000\u0000D\u05bb\u0001\u0000\u0000"+
		"\u0000F\u05c6\u0001\u0000\u0000\u0000H\u05d6\u0001\u0000\u0000\u0000J"+
		"\u05dc\u0001\u0000\u0000\u0000L\u05de\u0001\u0000\u0000\u0000N\u05e9\u0001"+
		"\u0000\u0000\u0000P\u05fa\u0001\u0000\u0000\u0000R\u0601\u0001\u0000\u0000"+
		"\u0000T\u0603\u0001\u0000\u0000\u0000V\u0609\u0001\u0000\u0000\u0000X"+
		"\u063f\u0001\u0000\u0000\u0000Z\u064b\u0001\u0000\u0000\u0000\\\u067b"+
		"\u0001\u0000\u0000\u0000^\u067e\u0001\u0000\u0000\u0000`\u06a4\u0001\u0000"+
		"\u0000\u0000b\u06a6\u0001\u0000\u0000\u0000d\u06ae\u0001\u0000\u0000\u0000"+
		"f\u06cf\u0001\u0000\u0000\u0000h\u06fd\u0001\u0000\u0000\u0000j\u0712"+
		"\u0001\u0000\u0000\u0000l\u0732\u0001\u0000\u0000\u0000n\u073e\u0001\u0000"+
		"\u0000\u0000p\u0741\u0001\u0000\u0000\u0000r\u074a\u0001\u0000\u0000\u0000"+
		"t\u075b\u0001\u0000\u0000\u0000v\u076f\u0001\u0000\u0000\u0000x\u0771"+
		"\u0001\u0000\u0000\u0000z\u0779\u0001\u0000\u0000\u0000|\u077d\u0001\u0000"+
		"\u0000\u0000~\u0780\u0001\u0000\u0000\u0000\u0080\u0783\u0001\u0000\u0000"+
		"\u0000\u0082\u079d\u0001\u0000\u0000\u0000\u0084\u079f\u0001\u0000\u0000"+
		"\u0000\u0086\u07d8\u0001\u0000\u0000\u0000\u0088\u07dc\u0001\u0000\u0000"+
		"\u0000\u008a\u07f7\u0001\u0000\u0000\u0000\u008c\u07fb\u0001\u0000\u0000"+
		"\u0000\u008e\u080a\u0001\u0000\u0000\u0000\u0090\u080c\u0001\u0000\u0000"+
		"\u0000\u0092\u082a\u0001\u0000\u0000\u0000\u0094\u082c\u0001\u0000\u0000"+
		"\u0000\u0096\u0833\u0001\u0000\u0000\u0000\u0098\u0853\u0001\u0000\u0000"+
		"\u0000\u009a\u0856\u0001\u0000\u0000\u0000\u009c\u0870\u0001\u0000\u0000"+
		"\u0000\u009e\u088a\u0001\u0000\u0000\u0000\u00a0\u0890\u0001\u0000\u0000"+
		"\u0000\u00a2\u0892\u0001\u0000\u0000\u0000\u00a4\u08b1\u0001\u0000\u0000"+
		"\u0000\u00a6\u08b3\u0001\u0000\u0000\u0000\u00a8\u08b7\u0001\u0000\u0000"+
		"\u0000\u00aa\u08bf\u0001\u0000\u0000\u0000\u00ac\u08ca\u0001\u0000\u0000"+
		"\u0000\u00ae\u08ce\u0001\u0000\u0000\u0000\u00b0\u08d9\u0001\u0000\u0000"+
		"\u0000\u00b2\u08f5\u0001\u0000\u0000\u0000\u00b4\u08f7\u0001\u0000\u0000"+
		"\u0000\u00b6\u0902\u0001\u0000\u0000\u0000\u00b8\u0918\u0001\u0000\u0000"+
		"\u0000\u00ba\u094b\u0001\u0000\u0000\u0000\u00bc\u094d\u0001\u0000\u0000"+
		"\u0000\u00be\u0955\u0001\u0000\u0000\u0000\u00c0\u0960\u0001\u0000\u0000"+
		"\u0000\u00c2\u0967\u0001\u0000\u0000\u0000\u00c4\u096b\u0001\u0000\u0000"+
		"\u0000\u00c6\u0975\u0001\u0000\u0000\u0000\u00c8\u097d\u0001\u0000\u0000"+
		"\u0000\u00ca\u098a\u0001\u0000\u0000\u0000\u00cc\u0999\u0001\u0000\u0000"+
		"\u0000\u00ce\u099d\u0001\u0000\u0000\u0000\u00d0\u099f\u0001\u0000\u0000"+
		"\u0000\u00d2\u09a1\u0001\u0000\u0000\u0000\u00d4\u09b5\u0001\u0000\u0000"+
		"\u0000\u00d6\u0a14\u0001\u0000\u0000\u0000\u00d8\u0a1a\u0001\u0000\u0000"+
		"\u0000\u00da\u0af0\u0001\u0000\u0000\u0000\u00dc\u0b0b\u0001\u0000\u0000"+
		"\u0000\u00de\u0b0d\u0001\u0000\u0000\u0000\u00e0\u0b0f\u0001\u0000\u0000"+
		"\u0000\u00e2\u0b11\u0001\u0000\u0000\u0000\u00e4\u0b13\u0001\u0000\u0000"+
		"\u0000\u00e6\u0b15\u0001\u0000\u0000\u0000\u00e8\u0b1a\u0001\u0000\u0000"+
		"\u0000\u00ea\u0b21\u0001\u0000\u0000\u0000\u00ec\u0b25\u0001\u0000\u0000"+
		"\u0000\u00ee\u0b2a\u0001\u0000\u0000\u0000\u00f0\u0b30\u0001\u0000\u0000"+
		"\u0000\u00f2\u0b37\u0001\u0000\u0000\u0000\u00f4\u0b67\u0001\u0000\u0000"+
		"\u0000\u00f6\u0b69\u0001\u0000\u0000\u0000\u00f8\u0b71\u0001\u0000\u0000"+
		"\u0000\u00fa\u0b7d\u0001\u0000\u0000\u0000\u00fc\u0b85\u0001\u0000\u0000"+
		"\u0000\u00fe\u0b8e\u0001\u0000\u0000\u0000\u0100\u0b96\u0001\u0000\u0000"+
		"\u0000\u0102\u0ba2\u0001\u0000\u0000\u0000\u0104\u0ba7\u0001\u0000\u0000"+
		"\u0000\u0106\u0bb0\u0001\u0000\u0000\u0000\u0108\u0be2\u0001\u0000\u0000"+
		"\u0000\u010a\u0bf4\u0001\u0000\u0000\u0000\u010c\u0bfd\u0001\u0000\u0000"+
		"\u0000\u010e\u0bff\u0001\u0000\u0000\u0000\u0110\u0c0b\u0001\u0000\u0000"+
		"\u0000\u0112\u0c0d\u0001\u0000\u0000\u0000\u0114\u0c15\u0001\u0000\u0000"+
		"\u0000\u0116\u0c1f\u0001\u0000\u0000\u0000\u0118\u0c24\u0001\u0000\u0000"+
		"\u0000\u011a\u0c2c\u0001\u0000\u0000\u0000\u011c\u0c2e\u0001\u0000\u0000"+
		"\u0000\u011e\u0c5b\u0001\u0000\u0000\u0000\u0120\u0c64\u0001\u0000\u0000"+
		"\u0000\u0122\u0c66\u0001\u0000\u0000\u0000\u0124\u0c68\u0001\u0000\u0000"+
		"\u0000\u0126\u0c6a\u0001\u0000\u0000\u0000\u0128\u0129\u0003\u0002\u0001"+
		"\u0000\u0129\u0001\u0001\u0000\u0000\u0000\u012a\u012b\u0003\u0004\u0002"+
		"\u0000\u012b\u0003\u0001\u0000\u0000\u0000\u012c\u012d\u0003 \u0010\u0000"+
		"\u012d\u012e\u0003\b\u0004\u0000\u012e\u012f\u0003@ \u0000\u012f\u0134"+
		"\u0003B!\u0000\u0130\u0132\u0005\u0018\u0000\u0000\u0131\u0130\u0001\u0000"+
		"\u0000\u0000\u0131\u0132\u0001\u0000\u0000\u0000\u0132\u0133\u0001\u0000"+
		"\u0000\u0000\u0133\u0135\u0003,\u0016\u0000\u0134\u0131\u0001\u0000\u0000"+
		"\u0000\u0134\u0135\u0001\u0000\u0000\u0000\u0135\u0005\u0001\u0000\u0000"+
		"\u0000\u0136\u0137\u0005\u00b5\u0000\u0000\u0137\u0138\u0005\f\u0000\u0000"+
		"\u0138\u0139\u0003\u00a6S\u0000\u0139\u0007\u0001\u0000\u0000\u0000\u013a"+
		"\u013b\u0005\u0001\u0000\u0000\u013b\u013e\u0003\u00fa}\u0000\u013c\u013d"+
		"\u0005\u0002\u0000\u0000\u013d\u013f\u0003\u0006\u0003\u0000\u013e\u013c"+
		"\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0140"+
		"\u0001\u0000\u0000\u0000\u0140\u0141\u0005\u0003\u0000\u0000\u0141\u0144"+
		"\u0001\u0000\u0000\u0000\u0142\u0144\u0003\u0006\u0003\u0000\u0143\u013a"+
		"\u0001\u0000\u0000\u0000\u0143\u0142\u0001\u0000\u0000\u0000\u0144\t\u0001"+
		"\u0000\u0000\u0000\u0145\u0149\u0003\u0018\f\u0000\u0146\u0148\u0005\u0004"+
		"\u0000\u0000\u0147\u0146\u0001\u0000\u0000\u0000\u0148\u014b\u0001\u0000"+
		"\u0000\u0000\u0149\u0147\u0001\u0000\u0000\u0000\u0149\u014a\u0001\u0000"+
		"\u0000\u0000\u014a\u014c\u0001\u0000\u0000\u0000\u014b\u0149\u0001\u0000"+
		"\u0000\u0000\u014c\u014d\u0005\u0000\u0000\u0001\u014d\u000b\u0001\u0000"+
		"\u0000\u0000\u014e\u014f\u0003\u00c4b\u0000\u014f\u0150\u0005\u0000\u0000"+
		"\u0001\u0150\r\u0001\u0000\u0000\u0000\u0151\u0152\u0003\u00c0`\u0000"+
		"\u0152\u0153\u0005\u0000\u0000\u0001\u0153\u000f\u0001\u0000\u0000\u0000"+
		"\u0154\u0155\u0003\u00be_\u0000\u0155\u0156\u0005\u0000\u0000\u0001\u0156"+
		"\u0011\u0001\u0000\u0000\u0000\u0157\u0158\u0003\u00c2a\u0000\u0158\u0159"+
		"\u0005\u0000\u0000\u0001\u0159\u0013\u0001\u0000\u0000\u0000\u015a\u015b"+
		"\u0003\u00f4z\u0000\u015b\u015c\u0005\u0000\u0000\u0001\u015c\u0015\u0001"+
		"\u0000\u0000\u0000\u015d\u015e\u0003\u00fa}\u0000\u015e\u015f\u0005\u0000"+
		"\u0000\u0001\u015f\u0017\u0001\u0000\u0000\u0000\u0160\u0442\u0003,\u0016"+
		"\u0000\u0161\u0163\u0003<\u001e\u0000\u0162\u0161\u0001\u0000\u0000\u0000"+
		"\u0162\u0163\u0001\u0000\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000"+
		"\u0164\u0442\u0003X,\u0000\u0165\u0167\u0005\u0104\u0000\u0000\u0166\u0168"+
		"\u0005\u0098\u0000\u0000\u0167\u0166\u0001\u0000\u0000\u0000\u0167\u0168"+
		"\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u0442"+
		"\u0003\u00be_\u0000\u016a\u016b\u00057\u0000\u0000\u016b\u016f\u00036"+
		"\u001b\u0000\u016c\u016d\u0005q\u0000\u0000\u016d\u016e\u0005\u009c\u0000"+
		"\u0000\u016e\u0170\u0005V\u0000\u0000\u016f\u016c\u0001\u0000\u0000\u0000"+
		"\u016f\u0170\u0001\u0000\u0000\u0000\u0170\u0171\u0001\u0000\u0000\u0000"+
		"\u0171\u0179\u0003\u00be_\u0000\u0172\u0178\u0003*\u0015\u0000\u0173\u0178"+
		"\u0003(\u0014\u0000\u0174\u0175\u0005\u010d\u0000\u0000\u0175\u0176\u0007"+
		"\u0000\u0000\u0000\u0176\u0178\u0003D\"\u0000\u0177\u0172\u0001\u0000"+
		"\u0000\u0000\u0177\u0173\u0001\u0000\u0000\u0000\u0177\u0174\u0001\u0000"+
		"\u0000\u0000\u0178\u017b\u0001\u0000\u0000\u0000\u0179\u0177\u0001\u0000"+
		"\u0000\u0000\u0179\u017a\u0001\u0000\u0000\u0000\u017a\u0442\u0001\u0000"+
		"\u0000\u0000\u017b\u0179\u0001\u0000\u0000\u0000\u017c\u017d\u0005\u0011"+
		"\u0000\u0000\u017d\u017e\u00036\u001b\u0000\u017e\u017f\u0003\u00be_\u0000"+
		"\u017f\u0180\u0005\u00d8\u0000\u0000\u0180\u0181\u0007\u0000\u0000\u0000"+
		"\u0181\u0182\u0003D\"\u0000\u0182\u0442\u0001\u0000\u0000\u0000\u0183"+
		"\u0184\u0005\u0011\u0000\u0000\u0184\u0185\u00036\u001b\u0000\u0185\u0186"+
		"\u0003\u00be_\u0000\u0186\u0187\u0005\u00d8\u0000\u0000\u0187\u0188\u0003"+
		"(\u0014\u0000\u0188\u0442\u0001\u0000\u0000\u0000\u0189\u018a\u0005O\u0000"+
		"\u0000\u018a\u018d\u00036\u001b\u0000\u018b\u018c\u0005q\u0000\u0000\u018c"+
		"\u018e\u0005V\u0000\u0000\u018d\u018b\u0001\u0000\u0000\u0000\u018d\u018e"+
		"\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000\u0000\u018f\u0191"+
		"\u0003\u00be_\u0000\u0190\u0192\u0007\u0001\u0000\u0000\u0191\u0190\u0001"+
		"\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192\u0442\u0001"+
		"\u0000\u0000\u0000\u0193\u0194\u0005\u00db\u0000\u0000\u0194\u0197\u0007"+
		"\u0002\u0000\u0000\u0195\u0196\u0007\u0003\u0000\u0000\u0196\u0198\u0003"+
		"\u00be_\u0000\u0197\u0195\u0001\u0000\u0000\u0000\u0197\u0198\u0001\u0000"+
		"\u0000\u0000\u0198\u019d\u0001\u0000\u0000\u0000\u0199\u019b\u0005\u0087"+
		"\u0000\u0000\u019a\u0199\u0001\u0000\u0000\u0000\u019a\u019b\u0001\u0000"+
		"\u0000\u0000\u019b\u019c\u0001\u0000\u0000\u0000\u019c\u019e\u0005\u0122"+
		"\u0000\u0000\u019d\u019a\u0001\u0000\u0000\u0000\u019d\u019e\u0001\u0000"+
		"\u0000\u0000\u019e\u0442\u0001\u0000\u0000\u0000\u019f\u01a4\u0003 \u0010"+
		"\u0000\u01a0\u01a1\u0005\u0001\u0000\u0000\u01a1\u01a2\u0003\u00fa}\u0000"+
		"\u01a2\u01a3\u0005\u0003\u0000\u0000\u01a3\u01a5\u0001\u0000\u0000\u0000"+
		"\u01a4\u01a0\u0001\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000"+
		"\u01a5\u01a7\u0001\u0000\u0000\u0000\u01a6\u01a8\u0003@ \u0000\u01a7\u01a6"+
		"\u0001\u0000\u0000\u0000\u01a7\u01a8\u0001\u0000\u0000\u0000\u01a8\u01a9"+
		"\u0001\u0000\u0000\u0000\u01a9\u01ae\u0003B!\u0000\u01aa\u01ac\u0005\u0018"+
		"\u0000\u0000\u01ab\u01aa\u0001\u0000\u0000\u0000\u01ab\u01ac\u0001\u0000"+
		"\u0000\u0000\u01ac\u01ad\u0001\u0000\u0000\u0000\u01ad\u01af\u0003,\u0016"+
		"\u0000\u01ae\u01ab\u0001\u0000\u0000\u0000\u01ae\u01af\u0001\u0000\u0000"+
		"\u0000\u01af\u0442\u0001\u0000\u0000\u0000\u01b0\u01b1\u00057\u0000\u0000"+
		"\u01b1\u01b5\u0005\u00e8\u0000\u0000\u01b2\u01b3\u0005q\u0000\u0000\u01b3"+
		"\u01b4\u0005\u009c\u0000\u0000\u01b4\u01b6\u0005V\u0000\u0000\u01b5\u01b2"+
		"\u0001\u0000\u0000\u0000\u01b5\u01b6\u0001\u0000\u0000\u0000\u01b6\u01b7"+
		"\u0001\u0000\u0000\u0000\u01b7\u01b8\u0003\u00c0`\u0000\u01b8\u01b9\u0005"+
		"\u0087\u0000\u0000\u01b9\u01c2\u0003\u00c0`\u0000\u01ba\u01c1\u0003@ "+
		"\u0000\u01bb\u01c1\u0003\u00ba]\u0000\u01bc\u01c1\u0003P(\u0000\u01bd"+
		"\u01c1\u0003(\u0014\u0000\u01be\u01bf\u0005\u00eb\u0000\u0000\u01bf\u01c1"+
		"\u0003D\"\u0000\u01c0\u01ba\u0001\u0000\u0000\u0000\u01c0\u01bb\u0001"+
		"\u0000\u0000\u0000\u01c0\u01bc\u0001\u0000\u0000\u0000\u01c0\u01bd\u0001"+
		"\u0000\u0000\u0000\u01c0\u01be\u0001\u0000\u0000\u0000\u01c1\u01c4\u0001"+
		"\u0000\u0000\u0000\u01c2\u01c0\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001"+
		"\u0000\u0000\u0000\u01c3\u0442\u0001\u0000\u0000\u0000\u01c4\u01c2\u0001"+
		"\u0000\u0000\u0000\u01c5\u01ca\u0003\"\u0011\u0000\u01c6\u01c7\u0005\u0001"+
		"\u0000\u0000\u01c7\u01c8\u0003\u00fa}\u0000\u01c8\u01c9\u0005\u0003\u0000"+
		"\u0000\u01c9\u01cb\u0001\u0000\u0000\u0000\u01ca\u01c6\u0001\u0000\u0000"+
		"\u0000\u01ca\u01cb\u0001\u0000\u0000\u0000\u01cb\u01cd\u0001\u0000\u0000"+
		"\u0000\u01cc\u01ce\u0003@ \u0000\u01cd\u01cc\u0001\u0000\u0000\u0000\u01cd"+
		"\u01ce\u0001\u0000\u0000\u0000\u01ce\u01cf\u0001\u0000\u0000\u0000\u01cf"+
		"\u01d4\u0003B!\u0000\u01d0\u01d2\u0005\u0018\u0000\u0000\u01d1\u01d0\u0001"+
		"\u0000\u0000\u0000\u01d1\u01d2\u0001\u0000\u0000\u0000\u01d2\u01d3\u0001"+
		"\u0000\u0000\u0000\u01d3\u01d5\u0003,\u0016\u0000\u01d4\u01d1\u0001\u0000"+
		"\u0000\u0000\u01d4\u01d5\u0001\u0000\u0000\u0000\u01d5\u0442\u0001\u0000"+
		"\u0000\u0000\u01d6\u01d7\u0005\u0012\u0000\u0000\u01d7\u01d8\u0005\u00e8"+
		"\u0000\u0000\u01d8\u01da\u0003\u00be_\u0000\u01d9\u01db\u00032\u0019\u0000"+
		"\u01da\u01d9\u0001\u0000\u0000\u0000\u01da\u01db\u0001\u0000\u0000\u0000"+
		"\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01dd\u00053\u0000\u0000\u01dd"+
		"\u01e5\u0005\u00e1\u0000\u0000\u01de\u01e6\u0003\u0118\u008c\u0000\u01df"+
		"\u01e0\u0005c\u0000\u0000\u01e0\u01e1\u0005.\u0000\u0000\u01e1\u01e6\u0003"+
		"\u00a8T\u0000\u01e2\u01e3\u0005c\u0000\u0000\u01e3\u01e4\u0005\u0010\u0000"+
		"\u0000\u01e4\u01e6\u0005.\u0000\u0000\u01e5\u01de\u0001\u0000\u0000\u0000"+
		"\u01e5\u01df\u0001\u0000\u0000\u0000\u01e5\u01e2\u0001\u0000\u0000\u0000"+
		"\u01e5\u01e6\u0001\u0000\u0000\u0000\u01e6\u0442\u0001\u0000\u0000\u0000"+
		"\u01e7\u01e8\u0005\u0012\u0000\u0000\u01e8\u01eb\u0005\u00e9\u0000\u0000"+
		"\u01e9\u01ea\u0007\u0003\u0000\u0000\u01ea\u01ec\u0003\u00be_\u0000\u01eb"+
		"\u01e9\u0001\u0000\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec"+
		"\u01ed\u0001\u0000\u0000\u0000\u01ed\u01ee\u00053\u0000\u0000\u01ee\u01f0"+
		"\u0005\u00e1\u0000\u0000\u01ef\u01f1\u0003\u0118\u008c\u0000\u01f0\u01ef"+
		"\u0001\u0000\u0000\u0000\u01f0\u01f1\u0001\u0000\u0000\u0000\u01f1\u0442"+
		"\u0001\u0000\u0000\u0000\u01f2\u01f3\u0005\u0011\u0000\u0000\u01f3\u01f4"+
		"\u0005\u00e8\u0000\u0000\u01f4\u01f5\u0003\u00be_\u0000\u01f5\u01f6\u0005"+
		"\u000e\u0000\u0000\u01f6\u01f7\u0007\u0004\u0000\u0000\u01f7\u01f8\u0003"+
		"\u00f6{\u0000\u01f8\u0442\u0001\u0000\u0000\u0000\u01f9\u01fa\u0005\u0011"+
		"\u0000\u0000\u01fa\u01fb\u0005\u00e8\u0000\u0000\u01fb\u01fc\u0003\u00be"+
		"_\u0000\u01fc\u01fd\u0005\u000e\u0000\u0000\u01fd\u01fe\u0007\u0004\u0000"+
		"\u0000\u01fe\u01ff\u0005\u0001\u0000\u0000\u01ff\u0200\u0003\u00f6{\u0000"+
		"\u0200\u0201\u0005\u0003\u0000\u0000\u0201\u0442\u0001\u0000\u0000\u0000"+
		"\u0202\u0203\u0005\u0011\u0000\u0000\u0203\u0204\u0005\u00e8\u0000\u0000"+
		"\u0204\u0205\u0003\u00be_\u0000\u0205\u0206\u0005\u00c1\u0000\u0000\u0206"+
		"\u0207\u0005-\u0000\u0000\u0207\u0208\u0003\u00be_\u0000\u0208\u0209\u0005"+
		"\u00f0\u0000\u0000\u0209\u020a\u0003\u0114\u008a\u0000\u020a\u0442\u0001"+
		"\u0000\u0000\u0000\u020b\u020c\u0005\u0011\u0000\u0000\u020c\u020d\u0005"+
		"\u00e8\u0000\u0000\u020d\u020e\u0003\u00be_\u0000\u020e\u020f\u0005O\u0000"+
		"\u0000\u020f\u0210\u0007\u0004\u0000\u0000\u0210\u0211\u0005\u0001\u0000"+
		"\u0000\u0211\u0212\u0003\u00bc^\u0000\u0212\u0213\u0005\u0003\u0000\u0000"+
		"\u0213\u0442\u0001\u0000\u0000\u0000\u0214\u0215\u0005\u0011\u0000\u0000"+
		"\u0215\u0216\u0005\u00e8\u0000\u0000\u0216\u0217\u0003\u00be_\u0000\u0217"+
		"\u0218\u0005O\u0000\u0000\u0218\u0219\u0007\u0004\u0000\u0000\u0219\u021a"+
		"\u0003\u00bc^\u0000\u021a\u0442\u0001\u0000\u0000\u0000\u021b\u021c\u0005"+
		"\u0011\u0000\u0000\u021c\u021d\u0007\u0005\u0000\u0000\u021d\u021e\u0003"+
		"\u00be_\u0000\u021e\u021f\u0005\u00c1\u0000\u0000\u021f\u0220\u0005\u00f0"+
		"\u0000\u0000\u0220\u0221\u0003\u00be_\u0000\u0221\u0442\u0001\u0000\u0000"+
		"\u0000\u0222\u0223\u0005\u0011\u0000\u0000\u0223\u0224\u0007\u0005\u0000"+
		"\u0000\u0224\u0225\u0003\u00be_\u0000\u0225\u0226\u0005\u00d8\u0000\u0000"+
		"\u0226\u0227\u0005\u00eb\u0000\u0000\u0227\u0228\u0003D\"\u0000\u0228"+
		"\u0442\u0001\u0000\u0000\u0000\u0229\u022a\u0005\u0011\u0000\u0000\u022a"+
		"\u022b\u0007\u0005\u0000\u0000\u022b\u022c\u0003\u00be_\u0000\u022c\u022d"+
		"\u0005\u0102\u0000\u0000\u022d\u0230\u0005\u00eb\u0000\u0000\u022e\u022f"+
		"\u0005q\u0000\u0000\u022f\u0231\u0005V\u0000\u0000\u0230\u022e\u0001\u0000"+
		"\u0000\u0000\u0230\u0231\u0001\u0000\u0000\u0000\u0231\u0232\u0001\u0000"+
		"\u0000\u0000\u0232\u0233\u0003D\"\u0000\u0233\u0442\u0001\u0000\u0000"+
		"\u0000\u0234\u0235\u0005\u0011\u0000\u0000\u0235\u0236\u0005\u00e8\u0000"+
		"\u0000\u0236\u0237\u0003\u00be_\u0000\u0237\u0239\u0007\u0006\u0000\u0000"+
		"\u0238\u023a\u0005-\u0000\u0000\u0239\u0238\u0001\u0000\u0000\u0000\u0239"+
		"\u023a\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000\u023b"+
		"\u023d\u0003\u00be_\u0000\u023c\u023e\u0003\u0120\u0090\u0000\u023d\u023c"+
		"\u0001\u0000\u0000\u0000\u023d\u023e\u0001\u0000\u0000\u0000\u023e\u0442"+
		"\u0001\u0000\u0000\u0000\u023f\u0240\u0005\u0011\u0000\u0000\u0240\u0241"+
		"\u0005\u00e8\u0000\u0000\u0241\u0243\u0003\u00be_\u0000\u0242\u0244\u0003"+
		"2\u0019\u0000\u0243\u0242\u0001\u0000\u0000\u0000\u0243\u0244\u0001\u0000"+
		"\u0000\u0000\u0244\u0245\u0001\u0000\u0000\u0000\u0245\u0247\u0005%\u0000"+
		"\u0000\u0246\u0248\u0005-\u0000\u0000\u0247\u0246\u0001\u0000\u0000\u0000"+
		"\u0247\u0248\u0001\u0000\u0000\u0000\u0248\u0249\u0001\u0000\u0000\u0000"+
		"\u0249\u024a\u0003\u00be_\u0000\u024a\u024c\u0003\u00fc~\u0000\u024b\u024d"+
		"\u0003\u00f2y\u0000\u024c\u024b\u0001\u0000\u0000\u0000\u024c\u024d\u0001"+
		"\u0000\u0000\u0000\u024d\u0442\u0001\u0000\u0000\u0000\u024e\u024f\u0005"+
		"\u0011\u0000\u0000\u024f\u0250\u0005\u00e8\u0000\u0000\u0250\u0252\u0003"+
		"\u00be_\u0000\u0251\u0253\u00032\u0019\u0000\u0252\u0251\u0001\u0000\u0000"+
		"\u0000\u0252\u0253\u0001\u0000\u0000\u0000\u0253\u0254\u0001\u0000\u0000"+
		"\u0000\u0254\u0255\u0005\u00c3\u0000\u0000\u0255\u0256\u0005.\u0000\u0000"+
		"\u0256\u0257\u0005\u0001\u0000\u0000\u0257\u0258\u0003\u00f6{\u0000\u0258"+
		"\u0259\u0005\u0003\u0000\u0000\u0259\u0442\u0001\u0000\u0000\u0000\u025a"+
		"\u025b\u0005\u0011\u0000\u0000\u025b\u025c\u0005\u00e8\u0000\u0000\u025c"+
		"\u025e\u0003\u00be_\u0000\u025d\u025f\u00032\u0019\u0000\u025e\u025d\u0001"+
		"\u0000\u0000\u0000\u025e\u025f\u0001\u0000\u0000\u0000\u025f\u0260\u0001"+
		"\u0000\u0000\u0000\u0260\u0261\u0005\u00d8\u0000\u0000\u0261\u0262\u0005"+
		"\u00d5\u0000\u0000\u0262\u0266\u0005\u0122\u0000\u0000\u0263\u0264\u0005"+
		"\u010d\u0000\u0000\u0264\u0265\u0005\u00d6\u0000\u0000\u0265\u0267\u0003"+
		"D\"\u0000\u0266\u0263\u0001\u0000\u0000\u0000\u0266\u0267\u0001\u0000"+
		"\u0000\u0000\u0267\u0442\u0001\u0000\u0000\u0000\u0268\u0269\u0005\u0011"+
		"\u0000\u0000\u0269\u026a\u0005\u00e8\u0000\u0000\u026a\u026c\u0003\u00be"+
		"_\u0000\u026b\u026d\u00032\u0019\u0000\u026c\u026b\u0001\u0000\u0000\u0000"+
		"\u026c\u026d\u0001\u0000\u0000\u0000\u026d\u026e\u0001\u0000\u0000\u0000"+
		"\u026e\u026f\u0005\u00d8\u0000\u0000\u026f\u0270\u0005\u00d6\u0000\u0000"+
		"\u0270\u0271\u0003D\"\u0000\u0271\u0442\u0001\u0000\u0000\u0000\u0272"+
		"\u0273\u0005\u0011\u0000\u0000\u0273\u0274\u0007\u0005\u0000\u0000\u0274"+
		"\u0275\u0003\u00be_\u0000\u0275\u0279\u0005\u000e\u0000\u0000\u0276\u0277"+
		"\u0005q\u0000\u0000\u0277\u0278\u0005\u009c\u0000\u0000\u0278\u027a\u0005"+
		"V\u0000\u0000\u0279\u0276\u0001\u0000\u0000\u0000\u0279\u027a\u0001\u0000"+
		"\u0000\u0000\u027a\u027c\u0001\u0000\u0000\u0000\u027b\u027d\u00030\u0018"+
		"\u0000\u027c\u027b\u0001\u0000\u0000\u0000\u027d\u027e\u0001\u0000\u0000"+
		"\u0000\u027e\u027c\u0001\u0000\u0000\u0000\u027e\u027f\u0001\u0000\u0000"+
		"\u0000\u027f\u0442\u0001\u0000\u0000\u0000\u0280\u0281\u0005\u0011\u0000"+
		"\u0000\u0281\u0282\u0005\u00e8\u0000\u0000\u0282\u0283\u0003\u00be_\u0000"+
		"\u0283\u0284\u00032\u0019\u0000\u0284\u0285\u0005\u00c1\u0000\u0000\u0285"+
		"\u0286\u0005\u00f0\u0000\u0000\u0286\u0287\u00032\u0019\u0000\u0287\u0442"+
		"\u0001\u0000\u0000\u0000\u0288\u0289\u0005\u0011\u0000\u0000\u0289\u028a"+
		"\u0007\u0005\u0000\u0000\u028a\u028b\u0003\u00be_\u0000\u028b\u028e\u0005"+
		"O\u0000\u0000\u028c\u028d\u0005q\u0000\u0000\u028d\u028f\u0005V\u0000"+
		"\u0000\u028e\u028c\u0001\u0000\u0000\u0000\u028e\u028f\u0001\u0000\u0000"+
		"\u0000\u028f\u0290\u0001\u0000\u0000\u0000\u0290\u0295\u00032\u0019\u0000"+
		"\u0291\u0292\u0005\u0002\u0000\u0000\u0292\u0294\u00032\u0019\u0000\u0293"+
		"\u0291\u0001\u0000\u0000\u0000\u0294\u0297\u0001\u0000\u0000\u0000\u0295"+
		"\u0293\u0001\u0000\u0000\u0000\u0295\u0296\u0001\u0000\u0000\u0000\u0296"+
		"\u0299\u0001\u0000\u0000\u0000\u0297\u0295\u0001\u0000\u0000\u0000\u0298"+
		"\u029a\u0005\u00b8\u0000\u0000\u0299\u0298\u0001\u0000\u0000\u0000\u0299"+
		"\u029a\u0001\u0000\u0000\u0000\u029a\u0442\u0001\u0000\u0000\u0000\u029b"+
		"\u029c\u0005\u0011\u0000\u0000\u029c\u029d\u0005\u00e8\u0000\u0000\u029d"+
		"\u029f\u0003\u00be_\u0000\u029e\u02a0\u00032\u0019\u0000\u029f\u029e\u0001"+
		"\u0000\u0000\u0000\u029f\u02a0\u0001\u0000\u0000\u0000\u02a0\u02a1\u0001"+
		"\u0000\u0000\u0000\u02a1\u02a2\u0005\u00d8\u0000\u0000\u02a2\u02a3\u0003"+
		"(\u0014\u0000\u02a3\u0442\u0001\u0000\u0000\u0000\u02a4\u02a5\u0005\u0011"+
		"\u0000\u0000\u02a5\u02a6\u0005\u00e8\u0000\u0000\u02a6\u02a7\u0003\u00be"+
		"_\u0000\u02a7\u02a8\u0005\u00bd\u0000\u0000\u02a8\u02a9\u0005\u00af\u0000"+
		"\u0000\u02a9\u0442\u0001\u0000\u0000\u0000\u02aa\u02ab\u0005O\u0000\u0000"+
		"\u02ab\u02ae\u0005\u00e8\u0000\u0000\u02ac\u02ad\u0005q\u0000\u0000\u02ad"+
		"\u02af\u0005V\u0000\u0000\u02ae\u02ac\u0001\u0000\u0000\u0000\u02ae\u02af"+
		"\u0001\u0000\u0000\u0000\u02af\u02b0\u0001\u0000\u0000\u0000\u02b0\u02b2"+
		"\u0003\u00be_\u0000\u02b1\u02b3\u0005\u00b8\u0000\u0000\u02b2\u02b1\u0001"+
		"\u0000\u0000\u0000\u02b2\u02b3\u0001\u0000\u0000\u0000\u02b3\u0442\u0001"+
		"\u0000\u0000\u0000\u02b4\u02b5\u0005O\u0000\u0000\u02b5\u02b8\u0005\u0108"+
		"\u0000\u0000\u02b6\u02b7\u0005q\u0000\u0000\u02b7\u02b9\u0005V\u0000\u0000"+
		"\u02b8\u02b6\u0001\u0000\u0000\u0000\u02b8\u02b9\u0001\u0000\u0000\u0000"+
		"\u02b9\u02ba\u0001\u0000\u0000\u0000\u02ba\u0442\u0003\u00be_\u0000\u02bb"+
		"\u02be\u00057\u0000\u0000\u02bc\u02bd\u0005\u00a4\u0000\u0000\u02bd\u02bf"+
		"\u0005\u00c3\u0000\u0000\u02be\u02bc\u0001\u0000\u0000\u0000\u02be\u02bf"+
		"\u0001\u0000\u0000\u0000\u02bf\u02c4\u0001\u0000\u0000\u0000\u02c0\u02c2"+
		"\u0005k\u0000\u0000\u02c1\u02c0\u0001\u0000\u0000\u0000\u02c1\u02c2\u0001"+
		"\u0000\u0000\u0000\u02c2\u02c3\u0001\u0000\u0000\u0000\u02c3\u02c5\u0005"+
		"\u00ec\u0000\u0000\u02c4\u02c1\u0001\u0000\u0000\u0000\u02c4\u02c5\u0001"+
		"\u0000\u0000\u0000\u02c5\u02c6\u0001\u0000\u0000\u0000\u02c6\u02ca\u0005"+
		"\u0108\u0000\u0000\u02c7\u02c8\u0005q\u0000\u0000\u02c8\u02c9\u0005\u009c"+
		"\u0000\u0000\u02c9\u02cb\u0005V\u0000\u0000\u02ca\u02c7\u0001\u0000\u0000"+
		"\u0000\u02ca\u02cb\u0001\u0000\u0000\u0000\u02cb\u02cc\u0001\u0000\u0000"+
		"\u0000\u02cc\u02ce\u0003\u00be_\u0000\u02cd\u02cf\u0003\u00aeW\u0000\u02ce"+
		"\u02cd\u0001\u0000\u0000\u0000\u02ce\u02cf\u0001\u0000\u0000\u0000\u02cf"+
		"\u02d8\u0001\u0000\u0000\u0000\u02d0\u02d7\u0003*\u0015\u0000\u02d1\u02d2"+
		"\u0005\u00ae\u0000\u0000\u02d2\u02d3\u0005\u00a0\u0000\u0000\u02d3\u02d7"+
		"\u0003\u00a6S\u0000\u02d4\u02d5\u0005\u00eb\u0000\u0000\u02d5\u02d7\u0003"+
		"D\"\u0000\u02d6\u02d0\u0001\u0000\u0000\u0000\u02d6\u02d1\u0001\u0000"+
		"\u0000\u0000\u02d6\u02d4\u0001\u0000\u0000\u0000\u02d7\u02da\u0001\u0000"+
		"\u0000\u0000\u02d8\u02d6\u0001\u0000\u0000\u0000\u02d8\u02d9\u0001\u0000"+
		"\u0000\u0000\u02d9\u02db\u0001\u0000\u0000\u0000\u02da\u02d8\u0001\u0000"+
		"\u0000\u0000\u02db\u02dc\u0005\u0018\u0000\u0000\u02dc\u02dd\u0003,\u0016"+
		"\u0000\u02dd\u0442\u0001\u0000\u0000\u0000\u02de\u02e1\u00057\u0000\u0000"+
		"\u02df\u02e0\u0005\u00a4\u0000\u0000\u02e0\u02e2\u0005\u00c3\u0000\u0000"+
		"\u02e1\u02df\u0001\u0000\u0000\u0000\u02e1\u02e2\u0001\u0000\u0000\u0000"+
		"\u02e2\u02e4\u0001\u0000\u0000\u0000\u02e3\u02e5\u0005k\u0000\u0000\u02e4"+
		"\u02e3\u0001\u0000\u0000\u0000\u02e4\u02e5\u0001\u0000\u0000\u0000\u02e5"+
		"\u02e6\u0001\u0000\u0000\u0000\u02e6\u02e7\u0005\u00ec\u0000\u0000\u02e7"+
		"\u02e8\u0005\u0108\u0000\u0000\u02e8\u02ed\u0003\u00c0`\u0000\u02e9\u02ea"+
		"\u0005\u0001\u0000\u0000\u02ea\u02eb\u0003\u00fa}\u0000\u02eb\u02ec\u0005"+
		"\u0003\u0000\u0000\u02ec\u02ee\u0001\u0000\u0000\u0000\u02ed\u02e9\u0001"+
		"\u0000\u0000\u0000\u02ed\u02ee\u0001\u0000\u0000\u0000\u02ee\u02ef\u0001"+
		"\u0000\u0000\u0000\u02ef\u02f2\u0003@ \u0000\u02f0\u02f1\u0005\u00a3\u0000"+
		"\u0000\u02f1\u02f3\u0003D\"\u0000\u02f2\u02f0\u0001\u0000\u0000\u0000"+
		"\u02f2\u02f3\u0001\u0000\u0000\u0000\u02f3\u0442\u0001\u0000\u0000\u0000"+
		"\u02f4\u02f5\u0005\u0011\u0000\u0000\u02f5\u02f6\u0005\u0108\u0000\u0000"+
		"\u02f6\u02f8\u0003\u00be_\u0000\u02f7\u02f9\u0005\u0018\u0000\u0000\u02f8"+
		"\u02f7\u0001\u0000\u0000\u0000\u02f8\u02f9\u0001\u0000\u0000\u0000\u02f9"+
		"\u02fa\u0001\u0000\u0000\u0000\u02fa\u02fb\u0003,\u0016\u0000\u02fb\u0442"+
		"\u0001\u0000\u0000\u0000\u02fc\u02ff\u00057\u0000\u0000\u02fd\u02fe\u0005"+
		"\u00a4\u0000\u0000\u02fe\u0300\u0005\u00c3\u0000\u0000\u02ff\u02fd\u0001"+
		"\u0000\u0000\u0000\u02ff\u0300\u0001\u0000\u0000\u0000\u0300\u0302\u0001"+
		"\u0000\u0000\u0000\u0301\u0303\u0005\u00ec\u0000\u0000\u0302\u0301\u0001"+
		"\u0000\u0000\u0000\u0302\u0303\u0001\u0000\u0000\u0000\u0303\u0304\u0001"+
		"\u0000\u0000\u0000\u0304\u0308\u0005i\u0000\u0000\u0305\u0306\u0005q\u0000"+
		"\u0000\u0306\u0307\u0005\u009c\u0000\u0000\u0307\u0309\u0005V\u0000\u0000"+
		"\u0308\u0305\u0001\u0000\u0000\u0000\u0308\u0309\u0001\u0000\u0000\u0000"+
		"\u0309\u030a\u0001\u0000\u0000\u0000\u030a\u030b\u0003\u00be_\u0000\u030b"+
		"\u030c\u0005\u0018\u0000\u0000\u030c\u0316\u0005\u0122\u0000\u0000\u030d"+
		"\u030e\u0005\u0106\u0000\u0000\u030e\u0313\u0003V+\u0000\u030f\u0310\u0005"+
		"\u0002\u0000\u0000\u0310\u0312\u0003V+\u0000\u0311\u030f\u0001\u0000\u0000"+
		"\u0000\u0312\u0315\u0001\u0000\u0000\u0000\u0313\u0311\u0001\u0000\u0000"+
		"\u0000\u0313\u0314\u0001\u0000\u0000\u0000\u0314\u0317\u0001\u0000\u0000"+
		"\u0000\u0315\u0313\u0001\u0000\u0000\u0000\u0316\u030d\u0001\u0000\u0000"+
		"\u0000\u0316\u0317\u0001\u0000\u0000\u0000\u0317\u0442\u0001\u0000\u0000"+
		"\u0000\u0318\u031a\u0005O\u0000\u0000\u0319\u031b\u0005\u00ec\u0000\u0000"+
		"\u031a\u0319\u0001\u0000\u0000\u0000\u031a\u031b\u0001\u0000\u0000\u0000"+
		"\u031b\u031c\u0001\u0000\u0000\u0000\u031c\u031f\u0005i\u0000\u0000\u031d"+
		"\u031e\u0005q\u0000\u0000\u031e\u0320\u0005V\u0000\u0000\u031f\u031d\u0001"+
		"\u0000\u0000\u0000\u031f\u0320\u0001\u0000\u0000\u0000\u0320\u0321\u0001"+
		"\u0000\u0000\u0000\u0321\u0442\u0003\u00be_\u0000\u0322\u0324\u0005W\u0000"+
		"\u0000\u0323\u0325\u0007\u0007\u0000\u0000\u0324\u0323\u0001\u0000\u0000"+
		"\u0000\u0324\u0325\u0001\u0000\u0000\u0000\u0325\u0326\u0001\u0000\u0000"+
		"\u0000\u0326\u0442\u0003\u0018\f\u0000\u0327\u0328\u0005\u00db\u0000\u0000"+
		"\u0328\u032b\u0005\u00e9\u0000\u0000\u0329\u032a\u0007\u0003\u0000\u0000"+
		"\u032a\u032c\u0003\u00be_\u0000\u032b\u0329\u0001\u0000\u0000\u0000\u032b"+
		"\u032c\u0001\u0000\u0000\u0000\u032c\u0331\u0001\u0000\u0000\u0000\u032d"+
		"\u032f\u0005\u0087\u0000\u0000\u032e\u032d\u0001\u0000\u0000\u0000\u032e"+
		"\u032f\u0001\u0000\u0000\u0000\u032f\u0330\u0001\u0000\u0000\u0000\u0330"+
		"\u0332\u0005\u0122\u0000\u0000\u0331\u032e\u0001\u0000\u0000\u0000\u0331"+
		"\u0332\u0001\u0000\u0000\u0000\u0332\u0442\u0001\u0000\u0000\u0000\u0333"+
		"\u0334\u0005\u00db\u0000\u0000\u0334\u0335\u0005\u00e8\u0000\u0000\u0335"+
		"\u0338\u0005Y\u0000\u0000\u0336\u0337\u0007\u0003\u0000\u0000\u0337\u0339"+
		"\u0003\u00be_\u0000\u0338\u0336\u0001\u0000\u0000\u0000\u0338\u0339\u0001"+
		"\u0000\u0000\u0000\u0339\u033a\u0001\u0000\u0000\u0000\u033a\u033b\u0005"+
		"\u0087\u0000\u0000\u033b\u033d\u0005\u0122\u0000\u0000\u033c\u033e\u0003"+
		"2\u0019\u0000\u033d\u033c\u0001\u0000\u0000\u0000\u033d\u033e\u0001\u0000"+
		"\u0000\u0000\u033e\u0442\u0001\u0000\u0000\u0000\u033f\u0340\u0005\u00db"+
		"\u0000\u0000\u0340\u0341\u0005\u00eb\u0000\u0000\u0341\u0346\u0003\u00be"+
		"_\u0000\u0342\u0343\u0005\u0001\u0000\u0000\u0343\u0344\u0003H$\u0000"+
		"\u0344\u0345\u0005\u0003\u0000\u0000\u0345\u0347\u0001\u0000\u0000\u0000"+
		"\u0346\u0342\u0001\u0000\u0000\u0000\u0346\u0347\u0001\u0000\u0000\u0000"+
		"\u0347\u0442\u0001\u0000\u0000\u0000\u0348\u0349\u0005\u00db\u0000\u0000"+
		"\u0349\u034a\u0005.\u0000\u0000\u034a\u034b\u0007\u0003\u0000\u0000\u034b"+
		"\u034e\u0003\u00be_\u0000\u034c\u034d\u0007\u0003\u0000\u0000\u034d\u034f"+
		"\u0003\u00be_\u0000\u034e\u034c\u0001\u0000\u0000\u0000\u034e\u034f\u0001"+
		"\u0000\u0000\u0000\u034f\u0442\u0001\u0000\u0000\u0000\u0350\u0351\u0005"+
		"\u00db\u0000\u0000\u0351\u0354\u0005\u0109\u0000\u0000\u0352\u0353\u0007"+
		"\u0003\u0000\u0000\u0353\u0355\u0003\u00be_\u0000\u0354\u0352\u0001\u0000"+
		"\u0000\u0000\u0354\u0355\u0001\u0000\u0000\u0000\u0355\u035a\u0001\u0000"+
		"\u0000\u0000\u0356\u0358\u0005\u0087\u0000\u0000\u0357\u0356\u0001\u0000"+
		"\u0000\u0000\u0357\u0358\u0001\u0000\u0000\u0000\u0358\u0359\u0001\u0000"+
		"\u0000\u0000\u0359\u035b\u0005\u0122\u0000\u0000\u035a\u0357\u0001\u0000"+
		"\u0000\u0000\u035a\u035b\u0001\u0000\u0000\u0000\u035b\u0442\u0001\u0000"+
		"\u0000\u0000\u035c\u035d\u0005\u00db\u0000\u0000\u035d\u035e\u0005\u00af"+
		"\u0000\u0000\u035e\u0360\u0003\u00be_\u0000\u035f\u0361\u00032\u0019\u0000"+
		"\u0360\u035f\u0001\u0000\u0000\u0000\u0360\u0361\u0001\u0000\u0000\u0000"+
		"\u0361\u0442\u0001\u0000\u0000\u0000\u0362\u0364\u0005\u00db\u0000\u0000"+
		"\u0363\u0365\u0003\u0118\u008c\u0000\u0364\u0363\u0001\u0000\u0000\u0000"+
		"\u0364\u0365\u0001\u0000\u0000\u0000\u0365\u0366\u0001\u0000\u0000\u0000"+
		"\u0366\u036e\u0005j\u0000\u0000\u0367\u0369\u0005\u0087\u0000\u0000\u0368"+
		"\u0367\u0001\u0000\u0000\u0000\u0368\u0369\u0001\u0000\u0000\u0000\u0369"+
		"\u036c\u0001\u0000\u0000\u0000\u036a\u036d\u0003\u00be_\u0000\u036b\u036d"+
		"\u0005\u0122\u0000\u0000\u036c\u036a\u0001\u0000\u0000\u0000\u036c\u036b"+
		"\u0001\u0000\u0000\u0000\u036d\u036f\u0001\u0000\u0000\u0000\u036e\u0368"+
		"\u0001\u0000\u0000\u0000\u036e\u036f\u0001\u0000\u0000\u0000\u036f\u0442"+
		"\u0001\u0000\u0000\u0000\u0370\u0371\u0005\u00db\u0000\u0000\u0371\u0372"+
		"\u00057\u0000\u0000\u0372\u0373\u0005\u00e8\u0000\u0000\u0373\u0376\u0003"+
		"\u00be_\u0000\u0374\u0375\u0005\u0018\u0000\u0000\u0375\u0377\u0005\u00d5"+
		"\u0000\u0000\u0376\u0374\u0001\u0000\u0000\u0000\u0376\u0377\u0001\u0000"+
		"\u0000\u0000\u0377\u0442\u0001\u0000\u0000\u0000\u0378\u0379\u0005\u00db"+
		"\u0000\u0000\u0379\u037a\u0005:\u0000\u0000\u037a\u0442\u0005\u0098\u0000"+
		"\u0000\u037b\u037c\u0007\b\u0000\u0000\u037c\u037e\u0005i\u0000\u0000"+
		"\u037d\u037f\u0005Y\u0000\u0000\u037e\u037d\u0001\u0000\u0000\u0000\u037e"+
		"\u037f\u0001\u0000\u0000\u0000\u037f\u0380\u0001\u0000\u0000\u0000\u0380"+
		"\u0442\u00038\u001c\u0000\u0381\u0382\u0007\b\u0000\u0000\u0382\u0384"+
		"\u00036\u001b\u0000\u0383\u0385\u0005Y\u0000\u0000\u0384\u0383\u0001\u0000"+
		"\u0000\u0000\u0384\u0385\u0001\u0000\u0000\u0000\u0385\u0386\u0001\u0000"+
		"\u0000\u0000\u0386\u0387\u0003\u00be_\u0000\u0387\u0442\u0001\u0000\u0000"+
		"\u0000\u0388\u038a\u0007\b\u0000\u0000\u0389\u038b\u0005\u00e8\u0000\u0000"+
		"\u038a\u0389\u0001\u0000\u0000\u0000\u038a\u038b\u0001\u0000\u0000\u0000"+
		"\u038b\u038d\u0001\u0000\u0000\u0000\u038c\u038e\u0007\t\u0000\u0000\u038d"+
		"\u038c\u0001\u0000\u0000\u0000\u038d\u038e\u0001\u0000\u0000\u0000\u038e"+
		"\u038f\u0001\u0000\u0000\u0000\u038f\u0391\u0003\u00be_\u0000\u0390\u0392"+
		"\u00032\u0019\u0000\u0391\u0390\u0001\u0000\u0000\u0000\u0391\u0392\u0001"+
		"\u0000\u0000\u0000\u0392\u0394\u0001\u0000\u0000\u0000\u0393\u0395\u0003"+
		":\u001d\u0000\u0394\u0393\u0001\u0000\u0000\u0000\u0394\u0395\u0001\u0000"+
		"\u0000\u0000\u0395\u0442\u0001\u0000\u0000\u0000\u0396\u0398\u0007\b\u0000"+
		"\u0000\u0397\u0399\u0005\u00b9\u0000\u0000\u0398\u0397\u0001\u0000\u0000"+
		"\u0000\u0398\u0399\u0001\u0000\u0000\u0000\u0399\u039a\u0001\u0000\u0000"+
		"\u0000\u039a\u0442\u0003,\u0016\u0000\u039b\u039c\u0005/\u0000\u0000\u039c"+
		"\u039d\u0005\u00a0\u0000\u0000\u039d\u039e\u00036\u001b\u0000\u039e\u039f"+
		"\u0003\u00be_\u0000\u039f\u03a0\u0005~\u0000\u0000\u03a0\u03a1\u0007\n"+
		"\u0000\u0000\u03a1\u0442\u0001\u0000\u0000\u0000\u03a2\u03a3\u0005/\u0000"+
		"\u0000\u03a3\u03a4\u0005\u00a0\u0000\u0000\u03a4\u03a5\u0005\u00e8\u0000"+
		"\u0000\u03a5\u03a6\u0003\u00be_\u0000\u03a6\u03a7\u0005~\u0000\u0000\u03a7"+
		"\u03a8\u0007\n\u0000\u0000\u03a8\u0442\u0001\u0000\u0000\u0000\u03a9\u03aa"+
		"\u0005\u00c0\u0000\u0000\u03aa\u03ab\u0005\u00e8\u0000\u0000\u03ab\u0442"+
		"\u0003\u00be_\u0000\u03ac\u03ad\u0005\u00c0\u0000\u0000\u03ad\u03ae\u0005"+
		"i\u0000\u0000\u03ae\u0442\u0003\u00be_\u0000\u03af\u03b7\u0005\u00c0\u0000"+
		"\u0000\u03b0\u03b8\u0005\u0122\u0000\u0000\u03b1\u03b3\t\u0000\u0000\u0000"+
		"\u03b2\u03b1\u0001\u0000\u0000\u0000\u03b3\u03b6\u0001\u0000\u0000\u0000"+
		"\u03b4\u03b5\u0001\u0000\u0000\u0000\u03b4\u03b2\u0001\u0000\u0000\u0000"+
		"\u03b5\u03b8\u0001\u0000\u0000\u0000\u03b6\u03b4\u0001\u0000\u0000\u0000"+
		"\u03b7\u03b0\u0001\u0000\u0000\u0000\u03b7\u03b4\u0001\u0000\u0000\u0000"+
		"\u03b8\u0442\u0001\u0000\u0000\u0000\u03b9\u03bb\u0005!\u0000\u0000\u03ba"+
		"\u03bc\u0005\u0084\u0000\u0000\u03bb\u03ba\u0001\u0000\u0000\u0000\u03bb"+
		"\u03bc\u0001\u0000\u0000\u0000\u03bc\u03bd\u0001\u0000\u0000\u0000\u03bd"+
		"\u03be\u0005\u00e8\u0000\u0000\u03be\u03c1\u0003\u00be_\u0000\u03bf\u03c0"+
		"\u0005\u00a3\u0000\u0000\u03c0\u03c2\u0003D\"\u0000\u03c1\u03bf\u0001"+
		"\u0000\u0000\u0000\u03c1\u03c2\u0001\u0000\u0000\u0000\u03c2\u03c7\u0001"+
		"\u0000\u0000\u0000\u03c3\u03c5\u0005\u0018\u0000\u0000\u03c4\u03c3\u0001"+
		"\u0000\u0000\u0000\u03c4\u03c5\u0001\u0000\u0000\u0000\u03c5\u03c6\u0001"+
		"\u0000\u0000\u0000\u03c6\u03c8\u0003,\u0016\u0000\u03c7\u03c4\u0001\u0000"+
		"\u0000\u0000\u03c7\u03c8\u0001\u0000\u0000\u0000\u03c8\u0442\u0001\u0000"+
		"\u0000\u0000\u03c9\u03ca\u0005\u00fd\u0000\u0000\u03ca\u03cd\u0005\u00e8"+
		"\u0000\u0000\u03cb\u03cc\u0005q\u0000\u0000\u03cc\u03ce\u0005V\u0000\u0000"+
		"\u03cd\u03cb\u0001\u0000\u0000\u0000\u03cd\u03ce\u0001\u0000\u0000\u0000"+
		"\u03ce\u03cf\u0001\u0000\u0000\u0000\u03cf\u0442\u0003\u00be_\u0000\u03d0"+
		"\u03d1\u0005\'\u0000\u0000\u03d1\u0442\u0005!\u0000\u0000\u03d2\u03d3"+
		"\u0005\u008b\u0000\u0000\u03d3\u03d5\u0005@\u0000\u0000\u03d4\u03d6\u0005"+
		"\u008c\u0000\u0000\u03d5\u03d4\u0001\u0000\u0000\u0000\u03d5\u03d6\u0001"+
		"\u0000\u0000\u0000\u03d6\u03d7\u0001\u0000\u0000\u0000\u03d7\u03d8\u0005"+
		"x\u0000\u0000\u03d8\u03da\u0005\u0122\u0000\u0000\u03d9\u03db\u0005\u00ac"+
		"\u0000\u0000\u03da\u03d9\u0001\u0000\u0000\u0000\u03da\u03db\u0001\u0000"+
		"\u0000\u0000\u03db\u03dc\u0001\u0000\u0000\u0000\u03dc\u03dd\u0005}\u0000"+
		"\u0000\u03dd\u03de\u0005\u00e8\u0000\u0000\u03de\u03e0\u0003\u00be_\u0000"+
		"\u03df\u03e1\u00032\u0019\u0000\u03e0\u03df\u0001\u0000\u0000\u0000\u03e0"+
		"\u03e1\u0001\u0000\u0000\u0000\u03e1\u0442\u0001\u0000\u0000\u0000\u03e2"+
		"\u03e3\u0005\u00f8\u0000\u0000\u03e3\u03e4\u0005\u00e8\u0000\u0000\u03e4"+
		"\u03e6\u0003\u00be_\u0000\u03e5\u03e7\u00032\u0019\u0000\u03e6\u03e5\u0001"+
		"\u0000\u0000\u0000\u03e6\u03e7\u0001\u0000\u0000\u0000\u03e7\u0442\u0001"+
		"\u0000\u0000\u0000\u03e8\u03e9\u0005\u0097\u0000\u0000\u03e9\u03ea\u0005"+
		"\u00c2\u0000\u0000\u03ea\u03eb\u0005\u00e8\u0000\u0000\u03eb\u03ee\u0003"+
		"\u00be_\u0000\u03ec\u03ed\u0007\u000b\u0000\u0000\u03ed\u03ef\u0005\u00af"+
		"\u0000\u0000\u03ee\u03ec\u0001\u0000\u0000\u0000\u03ee\u03ef\u0001\u0000"+
		"\u0000\u0000\u03ef\u0442\u0001\u0000\u0000\u0000\u03f0\u03f1\u0007\f\u0000"+
		"\u0000\u03f1\u03f5\u0003\u0118\u008c\u0000\u03f2\u03f4\t\u0000\u0000\u0000"+
		"\u03f3\u03f2\u0001\u0000\u0000\u0000\u03f4\u03f7\u0001\u0000\u0000\u0000"+
		"\u03f5\u03f6\u0001\u0000\u0000\u0000\u03f5\u03f3\u0001\u0000\u0000\u0000"+
		"\u03f6\u0442\u0001\u0000\u0000\u0000\u03f7\u03f5\u0001\u0000\u0000\u0000"+
		"\u03f8\u03f9\u0005\u00d8\u0000\u0000\u03f9\u03fd\u0005\u00ca\u0000\u0000"+
		"\u03fa\u03fc\t\u0000\u0000\u0000\u03fb\u03fa\u0001\u0000\u0000\u0000\u03fc"+
		"\u03ff\u0001\u0000\u0000\u0000\u03fd\u03fe\u0001\u0000\u0000\u0000\u03fd"+
		"\u03fb\u0001\u0000\u0000\u0000\u03fe\u0442\u0001\u0000\u0000\u0000\u03ff"+
		"\u03fd\u0001\u0000\u0000\u0000\u0400\u0401\u0005\u00d8\u0000\u0000\u0401"+
		"\u0402\u0005\u00ef\u0000\u0000\u0402\u0403\u0005\u010f\u0000\u0000\u0403"+
		"\u0442\u0003\u00e6s\u0000\u0404\u0405\u0005\u00d8\u0000\u0000\u0405\u0406"+
		"\u0005\u00ef\u0000\u0000\u0406\u0407\u0005\u010f\u0000\u0000\u0407\u0442"+
		"\u0007\r\u0000\u0000\u0408\u0409\u0005\u00d8\u0000\u0000\u0409\u040a\u0005"+
		"\u00ef\u0000\u0000\u040a\u040e\u0005\u010f\u0000\u0000\u040b\u040d\t\u0000"+
		"\u0000\u0000\u040c\u040b\u0001\u0000\u0000\u0000\u040d\u0410\u0001\u0000"+
		"\u0000\u0000\u040e\u040f\u0001\u0000\u0000\u0000\u040e\u040c\u0001\u0000"+
		"\u0000\u0000\u040f\u0442\u0001\u0000\u0000\u0000\u0410\u040e\u0001\u0000"+
		"\u0000\u0000\u0411\u0412\u0005\u00d8\u0000\u0000\u0412\u0413\u0003\u001a"+
		"\r\u0000\u0413\u0414\u0005\u0110\u0000\u0000\u0414\u0415\u0003\u001c\u000e"+
		"\u0000\u0415\u0442\u0001\u0000\u0000\u0000\u0416\u0417\u0005\u00d8\u0000"+
		"\u0000\u0417\u041f\u0003\u001a\r\u0000\u0418\u041c\u0005\u0110\u0000\u0000"+
		"\u0419\u041b\t\u0000\u0000\u0000\u041a\u0419\u0001\u0000\u0000\u0000\u041b"+
		"\u041e\u0001\u0000\u0000\u0000\u041c\u041d\u0001\u0000\u0000\u0000\u041c"+
		"\u041a\u0001\u0000\u0000\u0000\u041d\u0420\u0001\u0000\u0000\u0000\u041e"+
		"\u041c\u0001\u0000\u0000\u0000\u041f\u0418\u0001\u0000\u0000\u0000\u041f"+
		"\u0420\u0001\u0000\u0000\u0000\u0420\u0442\u0001\u0000\u0000\u0000\u0421"+
		"\u0425\u0005\u00d8\u0000\u0000\u0422\u0424\t\u0000\u0000\u0000\u0423\u0422"+
		"\u0001\u0000\u0000\u0000\u0424\u0427\u0001\u0000\u0000\u0000\u0425\u0426"+
		"\u0001\u0000\u0000\u0000\u0425\u0423\u0001\u0000\u0000\u0000\u0426\u0428"+
		"\u0001\u0000\u0000\u0000\u0427\u0425\u0001\u0000\u0000\u0000\u0428\u0429"+
		"\u0005\u0110\u0000\u0000\u0429\u0442\u0003\u001c\u000e\u0000\u042a\u042e"+
		"\u0005\u00d8\u0000\u0000\u042b\u042d\t\u0000\u0000\u0000\u042c\u042b\u0001"+
		"\u0000\u0000\u0000\u042d\u0430\u0001\u0000\u0000\u0000\u042e\u042f\u0001"+
		"\u0000\u0000\u0000\u042e\u042c\u0001\u0000\u0000\u0000\u042f\u0442\u0001"+
		"\u0000\u0000\u0000\u0430\u042e\u0001\u0000\u0000\u0000\u0431\u0432\u0005"+
		"\u00c4\u0000\u0000\u0432\u0442\u0003\u001a\r\u0000\u0433\u0437\u0005\u00c4"+
		"\u0000\u0000\u0434\u0436\t\u0000\u0000\u0000\u0435\u0434\u0001\u0000\u0000"+
		"\u0000\u0436\u0439\u0001\u0000\u0000\u0000\u0437\u0438\u0001\u0000\u0000"+
		"\u0000\u0437\u0435\u0001\u0000\u0000\u0000\u0438\u0442\u0001\u0000\u0000"+
		"\u0000\u0439\u0437\u0001\u0000\u0000\u0000\u043a\u043e\u0003\u001e\u000f"+
		"\u0000\u043b\u043d\t\u0000\u0000\u0000\u043c\u043b\u0001\u0000\u0000\u0000"+
		"\u043d\u0440\u0001\u0000\u0000\u0000\u043e\u043f\u0001\u0000\u0000\u0000"+
		"\u043e\u043c\u0001\u0000\u0000\u0000\u043f\u0442\u0001\u0000\u0000\u0000"+
		"\u0440\u043e\u0001\u0000\u0000\u0000\u0441\u0160\u0001\u0000\u0000\u0000"+
		"\u0441\u0162\u0001\u0000\u0000\u0000\u0441\u0165\u0001\u0000\u0000\u0000"+
		"\u0441\u016a\u0001\u0000\u0000\u0000\u0441\u017c\u0001\u0000\u0000\u0000"+
		"\u0441\u0183\u0001\u0000\u0000\u0000\u0441\u0189\u0001\u0000\u0000\u0000"+
		"\u0441\u0193\u0001\u0000\u0000\u0000\u0441\u019f\u0001\u0000\u0000\u0000"+
		"\u0441\u01b0\u0001\u0000\u0000\u0000\u0441\u01c5\u0001\u0000\u0000\u0000"+
		"\u0441\u01d6\u0001\u0000\u0000\u0000\u0441\u01e7\u0001\u0000\u0000\u0000"+
		"\u0441\u01f2\u0001\u0000\u0000\u0000\u0441\u01f9\u0001\u0000\u0000\u0000"+
		"\u0441\u0202\u0001\u0000\u0000\u0000\u0441\u020b\u0001\u0000\u0000\u0000"+
		"\u0441\u0214\u0001\u0000\u0000\u0000\u0441\u021b\u0001\u0000\u0000\u0000"+
		"\u0441\u0222\u0001\u0000\u0000\u0000\u0441\u0229\u0001\u0000\u0000\u0000"+
		"\u0441\u0234\u0001\u0000\u0000\u0000\u0441\u023f\u0001\u0000\u0000\u0000"+
		"\u0441\u024e\u0001\u0000\u0000\u0000\u0441\u025a\u0001\u0000\u0000\u0000"+
		"\u0441\u0268\u0001\u0000\u0000\u0000\u0441\u0272\u0001\u0000\u0000\u0000"+
		"\u0441\u0280\u0001\u0000\u0000\u0000\u0441\u0288\u0001\u0000\u0000\u0000"+
		"\u0441\u029b\u0001\u0000\u0000\u0000\u0441\u02a4\u0001\u0000\u0000\u0000"+
		"\u0441\u02aa\u0001\u0000\u0000\u0000\u0441\u02b4\u0001\u0000\u0000\u0000"+
		"\u0441\u02bb\u0001\u0000\u0000\u0000\u0441\u02de\u0001\u0000\u0000\u0000"+
		"\u0441\u02f4\u0001\u0000\u0000\u0000\u0441\u02fc\u0001\u0000\u0000\u0000"+
		"\u0441\u0318\u0001\u0000\u0000\u0000\u0441\u0322\u0001\u0000\u0000\u0000"+
		"\u0441\u0327\u0001\u0000\u0000\u0000\u0441\u0333\u0001\u0000\u0000\u0000"+
		"\u0441\u033f\u0001\u0000\u0000\u0000\u0441\u0348\u0001\u0000\u0000\u0000"+
		"\u0441\u0350\u0001\u0000\u0000\u0000\u0441\u035c\u0001\u0000\u0000\u0000"+
		"\u0441\u0362\u0001\u0000\u0000\u0000\u0441\u0370\u0001\u0000\u0000\u0000"+
		"\u0441\u0378\u0001\u0000\u0000\u0000\u0441\u037b\u0001\u0000\u0000\u0000"+
		"\u0441\u0381\u0001\u0000\u0000\u0000\u0441\u0388\u0001\u0000\u0000\u0000"+
		"\u0441\u0396\u0001\u0000\u0000\u0000\u0441\u039b\u0001\u0000\u0000\u0000"+
		"\u0441\u03a2\u0001\u0000\u0000\u0000\u0441\u03a9\u0001\u0000\u0000\u0000"+
		"\u0441\u03ac\u0001\u0000\u0000\u0000\u0441\u03af\u0001\u0000\u0000\u0000"+
		"\u0441\u03b9\u0001\u0000\u0000\u0000\u0441\u03c9\u0001\u0000\u0000\u0000"+
		"\u0441\u03d0\u0001\u0000\u0000\u0000\u0441\u03d2\u0001\u0000\u0000\u0000"+
		"\u0441\u03e2\u0001\u0000\u0000\u0000\u0441\u03e8\u0001\u0000\u0000\u0000"+
		"\u0441\u03f0\u0001\u0000\u0000\u0000\u0441\u03f8\u0001\u0000\u0000\u0000"+
		"\u0441\u0400\u0001\u0000\u0000\u0000\u0441\u0404\u0001\u0000\u0000\u0000"+
		"\u0441\u0408\u0001\u0000\u0000\u0000\u0441\u0411\u0001\u0000\u0000\u0000"+
		"\u0441\u0416\u0001\u0000\u0000\u0000\u0441\u0421\u0001\u0000\u0000\u0000"+
		"\u0441\u042a\u0001\u0000\u0000\u0000\u0441\u0431\u0001\u0000\u0000\u0000"+
		"\u0441\u0433\u0001\u0000\u0000\u0000\u0441\u043a\u0001\u0000\u0000\u0000"+
		"\u0442\u0019\u0001\u0000\u0000\u0000\u0443\u0444\u0003\u011c\u008e\u0000"+
		"\u0444\u001b\u0001\u0000\u0000\u0000\u0445\u0446\u0003\u011c\u008e\u0000"+
		"\u0446\u001d\u0001\u0000\u0000\u0000\u0447\u0448\u00057\u0000\u0000\u0448"+
		"\u04f0\u0005\u00ca\u0000\u0000\u0449\u044a\u0005O\u0000\u0000\u044a\u04f0"+
		"\u0005\u00ca\u0000\u0000\u044b\u044d\u0005l\u0000\u0000\u044c\u044e\u0005"+
		"\u00ca\u0000\u0000\u044d\u044c\u0001\u0000\u0000\u0000\u044d\u044e\u0001"+
		"\u0000\u0000\u0000\u044e\u04f0\u0001\u0000\u0000\u0000\u044f\u0451\u0005"+
		"\u00c7\u0000\u0000\u0450\u0452\u0005\u00ca\u0000\u0000\u0451\u0450\u0001"+
		"\u0000\u0000\u0000\u0451\u0452\u0001\u0000\u0000\u0000\u0452\u04f0\u0001"+
		"\u0000\u0000\u0000\u0453\u0454\u0005\u00db\u0000\u0000\u0454\u04f0\u0005"+
		"l\u0000\u0000\u0455\u0456\u0005\u00db\u0000\u0000\u0456\u0458\u0005\u00ca"+
		"\u0000\u0000\u0457\u0459\u0005l\u0000\u0000\u0458\u0457\u0001\u0000\u0000"+
		"\u0000\u0458\u0459\u0001\u0000\u0000\u0000\u0459\u04f0\u0001\u0000\u0000"+
		"\u0000\u045a\u045b\u0005\u00db\u0000\u0000\u045b\u04f0\u0005\u00b6\u0000"+
		"\u0000\u045c\u045d\u0005\u00db\u0000\u0000\u045d\u04f0\u0005\u00cb\u0000"+
		"\u0000\u045e\u045f\u0005\u00db\u0000\u0000\u045f\u0460\u0005:\u0000\u0000"+
		"\u0460\u04f0\u0005\u00cb\u0000\u0000\u0461\u0462\u0005X\u0000\u0000\u0462"+
		"\u04f0\u0005\u00e8\u0000\u0000\u0463\u0464\u0005s\u0000\u0000\u0464\u04f0"+
		"\u0005\u00e8\u0000\u0000\u0465\u0466\u0005\u00db\u0000\u0000\u0466\u04f0"+
		"\u00052\u0000\u0000\u0467\u0468\u0005\u00db\u0000\u0000\u0468\u0469\u0005"+
		"7\u0000\u0000\u0469\u04f0\u0005\u00e8\u0000\u0000\u046a\u046b\u0005\u00db"+
		"\u0000\u0000\u046b\u04f0\u0005\u00f4\u0000\u0000\u046c\u046d\u0005\u00db"+
		"\u0000\u0000\u046d\u04f0\u0005v\u0000\u0000\u046e\u046f\u0005\u00db\u0000"+
		"\u0000\u046f\u04f0\u0005\u008f\u0000\u0000\u0470\u0471\u00057\u0000\u0000"+
		"\u0471\u04f0\u0005u\u0000\u0000\u0472\u0473\u0005O\u0000\u0000\u0473\u04f0"+
		"\u0005u\u0000\u0000\u0474\u0475\u0005\u0011\u0000\u0000\u0475\u04f0\u0005"+
		"u\u0000\u0000\u0476\u0477\u0005\u008e\u0000\u0000\u0477\u04f0\u0005\u00e8"+
		"\u0000\u0000\u0478\u0479\u0005\u008e\u0000\u0000\u0479\u04f0\u0005A\u0000"+
		"\u0000\u047a\u047b\u0005\u0101\u0000\u0000\u047b\u04f0\u0005\u00e8\u0000"+
		"\u0000\u047c\u047d\u0005\u0101\u0000\u0000\u047d\u04f0\u0005A\u0000\u0000"+
		"\u047e\u047f\u00057\u0000\u0000\u047f\u0480\u0005\u00ec\u0000\u0000\u0480"+
		"\u04f0\u0005\u0091\u0000\u0000\u0481\u0482\u0005O\u0000\u0000\u0482\u0483"+
		"\u0005\u00ec\u0000\u0000\u0483\u04f0\u0005\u0091\u0000\u0000\u0484\u0485"+
		"\u0005\u0011\u0000\u0000\u0485\u0486\u0005\u00e8\u0000\u0000\u0486\u0487"+
		"\u0003\u00c0`\u0000\u0487\u0488\u0005\u009c\u0000\u0000\u0488\u0489\u0005"+
		")\u0000\u0000\u0489\u04f0\u0001\u0000\u0000\u0000\u048a\u048b\u0005\u0011"+
		"\u0000\u0000\u048b\u048c\u0005\u00e8\u0000\u0000\u048c\u048d\u0003\u00c0"+
		"`\u0000\u048d\u048e\u0005)\u0000\u0000\u048e\u048f\u0005 \u0000\u0000"+
		"\u048f\u04f0\u0001\u0000\u0000\u0000\u0490\u0491\u0005\u0011\u0000\u0000"+
		"\u0491\u0492\u0005\u00e8\u0000\u0000\u0492\u0493\u0003\u00c0`\u0000\u0493"+
		"\u0494\u0005\u009c\u0000\u0000\u0494\u0495\u0005\u00df\u0000\u0000\u0495"+
		"\u04f0\u0001\u0000\u0000\u0000\u0496\u0497\u0005\u0011\u0000\u0000\u0497"+
		"\u0498\u0005\u00e8\u0000\u0000\u0498\u0499\u0003\u00c0`\u0000\u0499\u049a"+
		"\u0005\u00dc\u0000\u0000\u049a\u049b\u0005 \u0000\u0000\u049b\u04f0\u0001"+
		"\u0000\u0000\u0000\u049c\u049d\u0005\u0011\u0000\u0000\u049d\u049e\u0005"+
		"\u00e8\u0000\u0000\u049e\u049f\u0003\u00c0`\u0000\u049f\u04a0\u0005\u009c"+
		"\u0000\u0000\u04a0\u04a1\u0005\u00dc\u0000\u0000\u04a1\u04f0\u0001\u0000"+
		"\u0000\u0000\u04a2\u04a3\u0005\u0011\u0000\u0000\u04a3\u04a4\u0005\u00e8"+
		"\u0000\u0000\u04a4\u04a5\u0003\u00c0`\u0000\u04a5\u04a6\u0005\u009c\u0000"+
		"\u0000\u04a6\u04a7\u0005\u00e2\u0000\u0000\u04a7\u04a8\u0005\u0018\u0000"+
		"\u0000\u04a8\u04a9\u0005J\u0000\u0000\u04a9\u04f0\u0001\u0000\u0000\u0000"+
		"\u04aa\u04ab\u0005\u0011\u0000\u0000\u04ab\u04ac\u0005\u00e8\u0000\u0000"+
		"\u04ac\u04ad\u0003\u00c0`\u0000\u04ad\u04ae\u0005\u00d8\u0000\u0000\u04ae"+
		"\u04af\u0005\u00dc\u0000\u0000\u04af\u04b0\u0005\u008d\u0000\u0000\u04b0"+
		"\u04f0\u0001\u0000\u0000\u0000\u04b1\u04b2\u0005\u0011\u0000\u0000\u04b2"+
		"\u04b3\u0005\u00e8\u0000\u0000\u04b3\u04b4\u0003\u00c0`\u0000\u04b4\u04b5"+
		"\u0005U\u0000\u0000\u04b5\u04b6\u0005\u00ad\u0000\u0000\u04b6\u04f0\u0001"+
		"\u0000\u0000\u0000\u04b7\u04b8\u0005\u0011\u0000\u0000\u04b8\u04b9\u0005"+
		"\u00e8\u0000\u0000\u04b9\u04ba\u0003\u00c0`\u0000\u04ba\u04bb\u0005\u0016"+
		"\u0000\u0000\u04bb\u04bc\u0005\u00ad\u0000\u0000\u04bc\u04f0\u0001\u0000"+
		"\u0000\u0000\u04bd\u04be\u0005\u0011\u0000\u0000\u04be\u04bf\u0005\u00e8"+
		"\u0000\u0000\u04bf\u04c0\u0003\u00c0`\u0000\u04c0\u04c1\u0005\u00fb\u0000"+
		"\u0000\u04c1\u04c2\u0005\u00ad\u0000\u0000\u04c2\u04f0\u0001\u0000\u0000"+
		"\u0000\u04c3\u04c4\u0005\u0011\u0000\u0000\u04c4\u04c5\u0005\u00e8\u0000"+
		"\u0000\u04c5\u04c6\u0003\u00c0`\u0000\u04c6\u04c7\u0005\u00f1\u0000\u0000"+
		"\u04c7\u04f0\u0001\u0000\u0000\u0000\u04c8\u04c9\u0005\u0011\u0000\u0000"+
		"\u04c9\u04ca\u0005\u00e8\u0000\u0000\u04ca\u04cc\u0003\u00c0`\u0000\u04cb"+
		"\u04cd\u00032\u0019\u0000\u04cc\u04cb\u0001\u0000\u0000\u0000\u04cc\u04cd"+
		"\u0001\u0000\u0000\u0000\u04cd\u04ce\u0001\u0000\u0000\u0000\u04ce\u04cf"+
		"\u00051\u0000\u0000\u04cf\u04f0\u0001\u0000\u0000\u0000\u04d0\u04d1\u0005"+
		"\u0011\u0000\u0000\u04d1\u04d2\u0005\u00e8\u0000\u0000\u04d2\u04d4\u0003"+
		"\u00c0`\u0000\u04d3\u04d5\u00032\u0019\u0000\u04d4\u04d3\u0001\u0000\u0000"+
		"\u0000\u04d4\u04d5\u0001\u0000\u0000\u0000\u04d5\u04d6\u0001\u0000\u0000"+
		"\u0000\u04d6\u04d7\u00054\u0000\u0000\u04d7\u04f0\u0001\u0000\u0000\u0000"+
		"\u04d8\u04d9\u0005\u0011\u0000\u0000\u04d9\u04da\u0005\u00e8\u0000\u0000"+
		"\u04da\u04dc\u0003\u00c0`\u0000\u04db\u04dd\u00032\u0019\u0000\u04dc\u04db"+
		"\u0001\u0000\u0000\u0000\u04dc\u04dd\u0001\u0000\u0000\u0000\u04dd\u04de"+
		"\u0001\u0000\u0000\u0000\u04de\u04df\u0005\u00d8\u0000\u0000\u04df\u04e0"+
		"\u0005`\u0000\u0000\u04e0\u04f0\u0001\u0000\u0000\u0000\u04e1\u04e2\u0005"+
		"\u0011\u0000\u0000\u04e2\u04e3\u0005\u00e8\u0000\u0000\u04e3\u04e5\u0003"+
		"\u00c0`\u0000\u04e4\u04e6\u00032\u0019\u0000\u04e5\u04e4\u0001\u0000\u0000"+
		"\u0000\u04e5\u04e6\u0001\u0000\u0000\u0000\u04e6\u04e7\u0001\u0000\u0000"+
		"\u0000\u04e7\u04e8\u0005\u00c3\u0000\u0000\u04e8\u04e9\u0005.\u0000\u0000"+
		"\u04e9\u04f0\u0001\u0000\u0000\u0000\u04ea\u04eb\u0005\u00e0\u0000\u0000"+
		"\u04eb\u04f0\u0005\u00f3\u0000\u0000\u04ec\u04f0\u00050\u0000\u0000\u04ed"+
		"\u04f0\u0005\u00cc\u0000\u0000\u04ee\u04f0\u0005I\u0000\u0000\u04ef\u0447"+
		"\u0001\u0000\u0000\u0000\u04ef\u0449\u0001\u0000\u0000\u0000\u04ef\u044b"+
		"\u0001\u0000\u0000\u0000\u04ef\u044f\u0001\u0000\u0000\u0000\u04ef\u0453"+
		"\u0001\u0000\u0000\u0000\u04ef\u0455\u0001\u0000\u0000\u0000\u04ef\u045a"+
		"\u0001\u0000\u0000\u0000\u04ef\u045c\u0001\u0000\u0000\u0000\u04ef\u045e"+
		"\u0001\u0000\u0000\u0000\u04ef\u0461\u0001\u0000\u0000\u0000\u04ef\u0463"+
		"\u0001\u0000\u0000\u0000\u04ef\u0465\u0001\u0000\u0000\u0000\u04ef\u0467"+
		"\u0001\u0000\u0000\u0000\u04ef\u046a\u0001\u0000\u0000\u0000\u04ef\u046c"+
		"\u0001\u0000\u0000\u0000\u04ef\u046e\u0001\u0000\u0000\u0000\u04ef\u0470"+
		"\u0001\u0000\u0000\u0000\u04ef\u0472\u0001\u0000\u0000\u0000\u04ef\u0474"+
		"\u0001\u0000\u0000\u0000\u04ef\u0476\u0001\u0000\u0000\u0000\u04ef\u0478"+
		"\u0001\u0000\u0000\u0000\u04ef\u047a\u0001\u0000\u0000\u0000\u04ef\u047c"+
		"\u0001\u0000\u0000\u0000\u04ef\u047e\u0001\u0000\u0000\u0000\u04ef\u0481"+
		"\u0001\u0000\u0000\u0000\u04ef\u0484\u0001\u0000\u0000\u0000\u04ef\u048a"+
		"\u0001\u0000\u0000\u0000\u04ef\u0490\u0001\u0000\u0000\u0000\u04ef\u0496"+
		"\u0001\u0000\u0000\u0000\u04ef\u049c\u0001\u0000\u0000\u0000\u04ef\u04a2"+
		"\u0001\u0000\u0000\u0000\u04ef\u04aa\u0001\u0000\u0000\u0000\u04ef\u04b1"+
		"\u0001\u0000\u0000\u0000\u04ef\u04b7\u0001\u0000\u0000\u0000\u04ef\u04bd"+
		"\u0001\u0000\u0000\u0000\u04ef\u04c3\u0001\u0000\u0000\u0000\u04ef\u04c8"+
		"\u0001\u0000\u0000\u0000\u04ef\u04d0\u0001\u0000\u0000\u0000\u04ef\u04d8"+
		"\u0001\u0000\u0000\u0000\u04ef\u04e1\u0001\u0000\u0000\u0000\u04ef\u04ea"+
		"\u0001\u0000\u0000\u0000\u04ef\u04ec\u0001\u0000\u0000\u0000\u04ef\u04ed"+
		"\u0001\u0000\u0000\u0000\u04ef\u04ee\u0001\u0000\u0000\u0000\u04f0\u001f"+
		"\u0001\u0000\u0000\u0000\u04f1\u04f3\u00057\u0000\u0000\u04f2\u04f4\u0005"+
		"\u00ec\u0000\u0000\u04f3\u04f2\u0001\u0000\u0000\u0000\u04f3\u04f4\u0001"+
		"\u0000\u0000\u0000\u04f4\u04f6\u0001\u0000\u0000\u0000\u04f5\u04f7\u0005"+
		"Z\u0000\u0000\u04f6\u04f5\u0001\u0000\u0000\u0000\u04f6\u04f7\u0001\u0000"+
		"\u0000\u0000\u04f7\u04f8\u0001\u0000\u0000\u0000\u04f8\u04fc\u0005\u00e8"+
		"\u0000\u0000\u04f9\u04fa\u0005q\u0000\u0000\u04fa\u04fb\u0005\u009c\u0000"+
		"\u0000\u04fb\u04fd\u0005V\u0000\u0000\u04fc\u04f9\u0001\u0000\u0000\u0000"+
		"\u04fc\u04fd\u0001\u0000\u0000\u0000\u04fd\u04fe\u0001\u0000\u0000\u0000"+
		"\u04fe\u04ff\u0003\u00be_\u0000\u04ff!\u0001\u0000\u0000\u0000\u0500\u0501"+
		"\u00057\u0000\u0000\u0501\u0503\u0005\u00a4\u0000\u0000\u0502\u0500\u0001"+
		"\u0000\u0000\u0000\u0502\u0503\u0001\u0000\u0000\u0000\u0503\u0504\u0001"+
		"\u0000\u0000\u0000\u0504\u0505\u0005\u00c3\u0000\u0000\u0505\u0506\u0005"+
		"\u00e8\u0000\u0000\u0506\u0507\u0003\u00be_\u0000\u0507#\u0001\u0000\u0000"+
		"\u0000\u0508\u0509\u0005)\u0000\u0000\u0509\u050a\u0005 \u0000\u0000\u050a"+
		"\u050e\u0003\u00a6S\u0000\u050b\u050c\u0005\u00df\u0000\u0000\u050c\u050d"+
		"\u0005 \u0000\u0000\u050d\u050f\u0003\u00aaU\u0000\u050e\u050b\u0001\u0000"+
		"\u0000\u0000\u050e\u050f\u0001\u0000\u0000\u0000\u050f\u0510\u0001\u0000"+
		"\u0000\u0000\u0510\u0511\u0005}\u0000\u0000\u0511\u0512\u0005\u0126\u0000"+
		"\u0000\u0512\u0513\u0005\u001f\u0000\u0000\u0513%\u0001\u0000\u0000\u0000"+
		"\u0514\u0515\u0005\u00dc\u0000\u0000\u0515\u0516\u0005 \u0000\u0000\u0516"+
		"\u0517\u0003\u00a6S\u0000\u0517\u051a\u0005\u00a0\u0000\u0000\u0518\u051b"+
		"\u0003L&\u0000\u0519\u051b\u0003N\'\u0000\u051a\u0518\u0001\u0000\u0000"+
		"\u0000\u051a\u0519\u0001\u0000\u0000\u0000\u051b\u051f\u0001\u0000\u0000"+
		"\u0000\u051c\u051d\u0005\u00e2\u0000\u0000\u051d\u051e\u0005\u0018\u0000"+
		"\u0000\u051e\u0520\u0005J\u0000\u0000\u051f\u051c\u0001\u0000\u0000\u0000"+
		"\u051f\u0520\u0001\u0000\u0000\u0000\u0520\'\u0001\u0000\u0000\u0000\u0521"+
		"\u0522\u0005\u008d\u0000\u0000\u0522\u0523\u0005\u0122\u0000\u0000\u0523"+
		")\u0001\u0000\u0000\u0000\u0524\u0525\u0005/\u0000\u0000\u0525\u0526\u0005"+
		"\u0122\u0000\u0000\u0526+\u0001\u0000\u0000\u0000\u0527\u0529\u0003<\u001e"+
		"\u0000\u0528\u0527\u0001\u0000\u0000\u0000\u0528\u0529\u0001\u0000\u0000"+
		"\u0000\u0529\u052a\u0001\u0000\u0000\u0000\u052a\u052b\u0003^/\u0000\u052b"+
		"\u052c\u0003Z-\u0000\u052c-\u0001\u0000\u0000\u0000\u052d\u052e\u0005"+
		"z\u0000\u0000\u052e\u0530\u0005\u00ac\u0000\u0000\u052f\u0531\u0005\u00e8"+
		"\u0000\u0000\u0530\u052f\u0001\u0000\u0000\u0000\u0530\u0531\u0001\u0000"+
		"\u0000\u0000\u0531\u0532\u0001\u0000\u0000\u0000\u0532\u0539\u0003\u00be"+
		"_\u0000\u0533\u0537\u00032\u0019\u0000\u0534\u0535\u0005q\u0000\u0000"+
		"\u0535\u0536\u0005\u009c\u0000\u0000\u0536\u0538\u0005V\u0000\u0000\u0537"+
		"\u0534\u0001\u0000\u0000\u0000\u0537\u0538\u0001\u0000\u0000\u0000\u0538"+
		"\u053a\u0001\u0000\u0000\u0000\u0539\u0533\u0001\u0000\u0000\u0000\u0539"+
		"\u053a\u0001\u0000\u0000\u0000\u053a\u053c\u0001\u0000\u0000\u0000\u053b"+
		"\u053d\u0003\u00a6S\u0000\u053c\u053b\u0001\u0000\u0000\u0000\u053c\u053d"+
		"\u0001\u0000\u0000\u0000\u053d\u056b\u0001\u0000\u0000\u0000\u053e\u053f"+
		"\u0005z\u0000\u0000\u053f\u0541\u0005}\u0000\u0000\u0540\u0542\u0005\u00e8"+
		"\u0000\u0000\u0541\u0540\u0001\u0000\u0000\u0000\u0541\u0542\u0001\u0000"+
		"\u0000\u0000\u0542\u0543\u0001\u0000\u0000\u0000\u0543\u0545\u0003\u00be"+
		"_\u0000\u0544\u0546\u00032\u0019\u0000\u0545\u0544\u0001\u0000\u0000\u0000"+
		"\u0545\u0546\u0001\u0000\u0000\u0000\u0546\u054a\u0001\u0000\u0000\u0000"+
		"\u0547\u0548\u0005q\u0000\u0000\u0548\u0549\u0005\u009c\u0000\u0000\u0549"+
		"\u054b\u0005V\u0000\u0000\u054a\u0547\u0001\u0000\u0000\u0000\u054a\u054b"+
		"\u0001\u0000\u0000\u0000\u054b\u054d\u0001\u0000\u0000\u0000\u054c\u054e"+
		"\u0003\u00a6S\u0000\u054d\u054c\u0001\u0000\u0000\u0000\u054d\u054e\u0001"+
		"\u0000\u0000\u0000\u054e\u056b\u0001\u0000\u0000\u0000\u054f\u0550\u0005"+
		"z\u0000\u0000\u0550\u0552\u0005\u00ac\u0000\u0000\u0551\u0553\u0005\u008c"+
		"\u0000\u0000\u0552\u0551\u0001\u0000\u0000\u0000\u0552\u0553\u0001\u0000"+
		"\u0000\u0000\u0553\u0554\u0001\u0000\u0000\u0000\u0554\u0555\u0005K\u0000"+
		"\u0000\u0555\u0557\u0005\u0122\u0000\u0000\u0556\u0558\u0003\u00ba]\u0000"+
		"\u0557\u0556\u0001\u0000\u0000\u0000\u0557\u0558\u0001\u0000\u0000\u0000"+
		"\u0558\u055a\u0001\u0000\u0000\u0000\u0559\u055b\u0003P(\u0000\u055a\u0559"+
		"\u0001\u0000\u0000\u0000\u055a\u055b\u0001\u0000\u0000\u0000\u055b\u056b"+
		"\u0001\u0000\u0000\u0000\u055c\u055d\u0005z\u0000\u0000\u055d\u055f\u0005"+
		"\u00ac\u0000\u0000\u055e\u0560\u0005\u008c\u0000\u0000\u055f\u055e\u0001"+
		"\u0000\u0000\u0000\u055f\u0560\u0001\u0000\u0000\u0000\u0560\u0561\u0001"+
		"\u0000\u0000\u0000\u0561\u0563\u0005K\u0000\u0000\u0562\u0564\u0005\u0122"+
		"\u0000\u0000\u0563\u0562\u0001\u0000\u0000\u0000\u0563\u0564\u0001\u0000"+
		"\u0000\u0000\u0564\u0565\u0001\u0000\u0000\u0000\u0565\u0568\u0003@ \u0000"+
		"\u0566\u0567\u0005\u00a3\u0000\u0000\u0567\u0569\u0003D\"\u0000\u0568"+
		"\u0566\u0001\u0000\u0000\u0000\u0568\u0569\u0001\u0000\u0000\u0000\u0569"+
		"\u056b\u0001\u0000\u0000\u0000\u056a\u052d\u0001\u0000\u0000\u0000\u056a"+
		"\u053e\u0001\u0000\u0000\u0000\u056a\u054f\u0001\u0000\u0000\u0000\u056a"+
		"\u055c\u0001\u0000\u0000\u0000\u056b/\u0001\u0000\u0000\u0000\u056c\u056e"+
		"\u00032\u0019\u0000\u056d\u056f\u0003(\u0014\u0000\u056e\u056d\u0001\u0000"+
		"\u0000\u0000\u056e\u056f\u0001\u0000\u0000\u0000\u056f1\u0001\u0000\u0000"+
		"\u0000\u0570\u0571\u0005\u00ad\u0000\u0000\u0571\u0572\u0005\u0001\u0000"+
		"\u0000\u0572\u0577\u00034\u001a\u0000\u0573\u0574\u0005\u0002\u0000\u0000"+
		"\u0574\u0576\u00034\u001a\u0000\u0575\u0573\u0001\u0000\u0000\u0000\u0576"+
		"\u0579\u0001\u0000\u0000\u0000\u0577\u0575\u0001\u0000\u0000\u0000\u0577"+
		"\u0578\u0001\u0000\u0000\u0000\u0578\u057a\u0001\u0000\u0000\u0000\u0579"+
		"\u0577\u0001\u0000\u0000\u0000\u057a\u057b\u0005\u0003\u0000\u0000\u057b"+
		"3\u0001\u0000\u0000\u0000\u057c\u057f\u0003\u0118\u008c\u0000\u057d\u057e"+
		"\u0005\u0110\u0000\u0000\u057e\u0580\u0003\u00dcn\u0000\u057f\u057d\u0001"+
		"\u0000\u0000\u0000\u057f\u0580\u0001\u0000\u0000\u0000\u05805\u0001\u0000"+
		"\u0000\u0000\u0581\u0582\u0007\u000e\u0000\u0000\u05827\u0001\u0000\u0000"+
		"\u0000\u0583\u0589\u0003\u0112\u0089\u0000\u0584\u0589\u0005\u0122\u0000"+
		"\u0000\u0585\u0589\u0003\u00deo\u0000\u0586\u0589\u0003\u00e0p\u0000\u0587"+
		"\u0589\u0003\u00e2q\u0000\u0588\u0583\u0001\u0000\u0000\u0000\u0588\u0584"+
		"\u0001\u0000\u0000\u0000\u0588\u0585\u0001\u0000\u0000\u0000\u0588\u0586"+
		"\u0001\u0000\u0000\u0000\u0588\u0587\u0001\u0000\u0000\u0000\u05899\u0001"+
		"\u0000\u0000\u0000\u058a\u058f\u0003\u0118\u008c\u0000\u058b\u058c\u0005"+
		"\u0005\u0000\u0000\u058c\u058e\u0003\u0118\u008c\u0000\u058d\u058b\u0001"+
		"\u0000\u0000\u0000\u058e\u0591\u0001\u0000\u0000\u0000\u058f\u058d\u0001"+
		"\u0000\u0000\u0000\u058f\u0590\u0001\u0000\u0000\u0000\u0590;\u0001\u0000"+
		"\u0000\u0000\u0591\u058f\u0001\u0000\u0000\u0000\u0592\u0593\u0005\u010d"+
		"\u0000\u0000\u0593\u0598\u0003>\u001f\u0000\u0594\u0595\u0005\u0002\u0000"+
		"\u0000\u0595\u0597\u0003>\u001f\u0000\u0596\u0594\u0001\u0000\u0000\u0000"+
		"\u0597\u059a\u0001\u0000\u0000\u0000\u0598\u0596\u0001\u0000\u0000\u0000"+
		"\u0598\u0599\u0001\u0000\u0000\u0000\u0599=\u0001\u0000\u0000\u0000\u059a"+
		"\u0598\u0001\u0000\u0000\u0000\u059b\u059d\u0003\u0114\u008a\u0000\u059c"+
		"\u059e\u0003\u00a6S\u0000\u059d\u059c\u0001\u0000\u0000\u0000\u059d\u059e"+
		"\u0001\u0000\u0000\u0000\u059e\u05a0\u0001\u0000\u0000\u0000\u059f\u05a1"+
		"\u0005\u0018\u0000\u0000\u05a0\u059f\u0001\u0000\u0000\u0000\u05a0\u05a1"+
		"\u0001\u0000\u0000\u0000\u05a1\u05a2\u0001\u0000\u0000\u0000\u05a2\u05a3"+
		"\u0005\u0001\u0000\u0000\u05a3\u05a4\u0003,\u0016\u0000\u05a4\u05a5\u0005"+
		"\u0003\u0000\u0000\u05a5?\u0001\u0000\u0000\u0000\u05a6\u05a7\u0005\u0106"+
		"\u0000\u0000\u05a7\u05a8\u0003\u00be_\u0000\u05a8A\u0001\u0000\u0000\u0000"+
		"\u05a9\u05aa\u0005\u00a3\u0000\u0000\u05aa\u05b7\u0003D\"\u0000\u05ab"+
		"\u05ac\u0005\u00ae\u0000\u0000\u05ac\u05ad\u0005 \u0000\u0000\u05ad\u05b7"+
		"\u0003\u00c8d\u0000\u05ae\u05b7\u0003&\u0013\u0000\u05af\u05b7\u0003$"+
		"\u0012\u0000\u05b0\u05b7\u0003\u00ba]\u0000\u05b1\u05b7\u0003P(\u0000"+
		"\u05b2\u05b7\u0003(\u0014\u0000\u05b3\u05b7\u0003*\u0015\u0000\u05b4\u05b5"+
		"\u0005\u00eb\u0000\u0000\u05b5\u05b7\u0003D\"\u0000\u05b6\u05a9\u0001"+
		"\u0000\u0000\u0000\u05b6\u05ab\u0001\u0000\u0000\u0000\u05b6\u05ae\u0001"+
		"\u0000\u0000\u0000\u05b6\u05af\u0001\u0000\u0000\u0000\u05b6\u05b0\u0001"+
		"\u0000\u0000\u0000\u05b6\u05b1\u0001\u0000\u0000\u0000\u05b6\u05b2\u0001"+
		"\u0000\u0000\u0000\u05b6\u05b3\u0001\u0000\u0000\u0000\u05b6\u05b4\u0001"+
		"\u0000\u0000\u0000\u05b7\u05ba\u0001\u0000\u0000\u0000\u05b8\u05b6\u0001"+
		"\u0000\u0000\u0000\u05b8\u05b9\u0001\u0000\u0000\u0000\u05b9C\u0001\u0000"+
		"\u0000\u0000\u05ba\u05b8\u0001\u0000\u0000\u0000\u05bb\u05bc\u0005\u0001"+
		"\u0000\u0000\u05bc\u05c1\u0003F#\u0000\u05bd\u05be\u0005\u0002\u0000\u0000"+
		"\u05be\u05c0\u0003F#\u0000\u05bf\u05bd\u0001\u0000\u0000\u0000\u05c0\u05c3"+
		"\u0001\u0000\u0000\u0000\u05c1\u05bf\u0001\u0000\u0000\u0000\u05c1\u05c2"+
		"\u0001\u0000\u0000\u0000\u05c2\u05c4\u0001\u0000\u0000\u0000\u05c3\u05c1"+
		"\u0001\u0000\u0000\u0000\u05c4\u05c5\u0005\u0003\u0000\u0000\u05c5E\u0001"+
		"\u0000\u0000\u0000\u05c6\u05cb\u0003H$\u0000\u05c7\u05c9\u0005\u0110\u0000"+
		"\u0000\u05c8\u05c7\u0001\u0000\u0000\u0000\u05c8\u05c9\u0001\u0000\u0000"+
		"\u0000\u05c9\u05ca\u0001\u0000\u0000\u0000\u05ca\u05cc\u0003J%\u0000\u05cb"+
		"\u05c8\u0001\u0000\u0000\u0000\u05cb\u05cc\u0001\u0000\u0000\u0000\u05cc"+
		"G\u0001\u0000\u0000\u0000\u05cd\u05d2\u0003\u0118\u008c\u0000\u05ce\u05cf"+
		"\u0005\u0005\u0000\u0000\u05cf\u05d1\u0003\u0118\u008c\u0000\u05d0\u05ce"+
		"\u0001\u0000\u0000\u0000\u05d1\u05d4\u0001\u0000\u0000\u0000\u05d2\u05d0"+
		"\u0001\u0000\u0000\u0000\u05d2\u05d3\u0001\u0000\u0000\u0000\u05d3\u05d7"+
		"\u0001\u0000\u0000\u0000\u05d4\u05d2\u0001\u0000\u0000\u0000\u05d5\u05d7"+
		"\u0005\u0122\u0000\u0000\u05d6\u05cd\u0001\u0000\u0000\u0000\u05d6\u05d5"+
		"\u0001\u0000\u0000\u0000\u05d7I\u0001\u0000\u0000\u0000\u05d8\u05dd\u0005"+
		"\u0126\u0000\u0000\u05d9\u05dd\u0005\u0128\u0000\u0000\u05da\u05dd\u0003"+
		"\u00e4r\u0000\u05db\u05dd\u0005\u0122\u0000\u0000\u05dc\u05d8\u0001\u0000"+
		"\u0000\u0000\u05dc\u05d9\u0001\u0000\u0000\u0000\u05dc\u05da\u0001\u0000"+
		"\u0000\u0000\u05dc\u05db\u0001\u0000\u0000\u0000\u05ddK\u0001\u0000\u0000"+
		"\u0000\u05de\u05df\u0005\u0001\u0000\u0000\u05df\u05e4\u0003\u00dcn\u0000"+
		"\u05e0\u05e1\u0005\u0002\u0000\u0000\u05e1\u05e3\u0003\u00dcn\u0000\u05e2"+
		"\u05e0\u0001\u0000\u0000\u0000\u05e3\u05e6\u0001\u0000\u0000\u0000\u05e4"+
		"\u05e2\u0001\u0000\u0000\u0000\u05e4\u05e5\u0001\u0000\u0000\u0000\u05e5"+
		"\u05e7\u0001\u0000\u0000\u0000\u05e6\u05e4\u0001\u0000\u0000\u0000\u05e7"+
		"\u05e8\u0005\u0003\u0000\u0000\u05e8M\u0001\u0000\u0000\u0000\u05e9\u05ea"+
		"\u0005\u0001\u0000\u0000\u05ea\u05ef\u0003L&\u0000\u05eb\u05ec\u0005\u0002"+
		"\u0000\u0000\u05ec\u05ee\u0003L&\u0000\u05ed\u05eb\u0001\u0000\u0000\u0000"+
		"\u05ee\u05f1\u0001\u0000\u0000\u0000\u05ef\u05ed\u0001\u0000\u0000\u0000"+
		"\u05ef\u05f0\u0001\u0000\u0000\u0000\u05f0\u05f2\u0001\u0000\u0000\u0000"+
		"\u05f1\u05ef\u0001\u0000\u0000\u0000\u05f2\u05f3\u0005\u0003\u0000\u0000"+
		"\u05f3O\u0001\u0000\u0000\u0000\u05f4\u05f5\u0005\u00e2\u0000\u0000\u05f5"+
		"\u05f6\u0005\u0018\u0000\u0000\u05f6\u05fb\u0003R)\u0000\u05f7\u05f8\u0005"+
		"\u00e2\u0000\u0000\u05f8\u05f9\u0005 \u0000\u0000\u05f9\u05fb\u0003T*"+
		"\u0000\u05fa\u05f4\u0001\u0000\u0000\u0000\u05fa\u05f7\u0001\u0000\u0000"+
		"\u0000\u05fbQ\u0001\u0000\u0000\u0000\u05fc\u05fd\u0005y\u0000\u0000\u05fd"+
		"\u05fe\u0005\u0122\u0000\u0000\u05fe\u05ff\u0005\u00a8\u0000\u0000\u05ff"+
		"\u0602\u0005\u0122\u0000\u0000\u0600\u0602\u0003\u0118\u008c\u0000\u0601"+
		"\u05fc\u0001\u0000\u0000\u0000\u0601\u0600\u0001\u0000\u0000\u0000\u0602"+
		"S\u0001\u0000\u0000\u0000\u0603\u0607\u0005\u0122\u0000\u0000\u0604\u0605"+
		"\u0005\u010d\u0000\u0000\u0605\u0606\u0005\u00d6\u0000\u0000\u0606\u0608"+
		"\u0003D\"\u0000\u0607\u0604\u0001\u0000\u0000\u0000\u0607\u0608\u0001"+
		"\u0000\u0000\u0000\u0608U\u0001\u0000\u0000\u0000\u0609\u060a\u0003\u0118"+
		"\u008c\u0000\u060a\u060b\u0005\u0122\u0000\u0000\u060bW\u0001\u0000\u0000"+
		"\u0000\u060c\u060d\u0003.\u0017\u0000\u060d\u060e\u0003^/\u0000\u060e"+
		"\u060f\u0003Z-\u0000\u060f\u0640\u0001\u0000\u0000\u0000\u0610\u0612\u0003"+
		"\u0084B\u0000\u0611\u0613\u0003\\.\u0000\u0612\u0611\u0001\u0000\u0000"+
		"\u0000\u0613\u0614\u0001\u0000\u0000\u0000\u0614\u0612\u0001\u0000\u0000"+
		"\u0000\u0614\u0615\u0001\u0000\u0000\u0000\u0615\u0640\u0001\u0000\u0000"+
		"\u0000\u0616\u0617\u0005E\u0000\u0000\u0617\u0618\u0005g\u0000\u0000\u0618"+
		"\u0619\u0003\u00be_\u0000\u0619\u061b\u0003\u00b8\\\u0000\u061a\u061c"+
		"\u0003|>\u0000\u061b\u061a\u0001\u0000\u0000\u0000\u061b\u061c\u0001\u0000"+
		"\u0000\u0000\u061c\u0640\u0001\u0000\u0000\u0000\u061d\u061e\u0005\u0103"+
		"\u0000\u0000\u061e\u061f\u0003\u00be_\u0000\u061f\u0620\u0003\u00b8\\"+
		"\u0000\u0620\u0622\u0003n7\u0000\u0621\u0623\u0003|>\u0000\u0622\u0621"+
		"\u0001\u0000\u0000\u0000\u0622\u0623\u0001\u0000\u0000\u0000\u0623\u0640"+
		"\u0001\u0000\u0000\u0000\u0624\u0625\u0005\u0094\u0000\u0000\u0625\u0626"+
		"\u0005}\u0000\u0000\u0626\u0627\u0003\u00be_\u0000\u0627\u0628\u0003\u00b8"+
		"\\\u0000\u0628\u062e\u0005\u0106\u0000\u0000\u0629\u062f\u0003\u00be_"+
		"\u0000\u062a\u062b\u0005\u0001\u0000\u0000\u062b\u062c\u0003,\u0016\u0000"+
		"\u062c\u062d\u0005\u0003\u0000\u0000\u062d\u062f\u0001\u0000\u0000\u0000"+
		"\u062e\u0629\u0001\u0000\u0000\u0000\u062e\u062a\u0001\u0000\u0000\u0000"+
		"\u062f\u0630\u0001\u0000\u0000\u0000\u0630\u0631\u0003\u00b8\\\u0000\u0631"+
		"\u0632\u0005\u00a0\u0000\u0000\u0632\u0636\u0003\u00d4j\u0000\u0633\u0635"+
		"\u0003p8\u0000\u0634\u0633\u0001\u0000\u0000\u0000\u0635\u0638\u0001\u0000"+
		"\u0000\u0000\u0636\u0634\u0001\u0000\u0000\u0000\u0636\u0637\u0001\u0000"+
		"\u0000\u0000\u0637\u063c\u0001\u0000\u0000\u0000\u0638\u0636\u0001\u0000"+
		"\u0000\u0000\u0639\u063b\u0003r9\u0000\u063a\u0639\u0001\u0000\u0000\u0000"+
		"\u063b\u063e\u0001\u0000\u0000\u0000\u063c\u063a\u0001\u0000\u0000\u0000"+
		"\u063c\u063d\u0001\u0000\u0000\u0000\u063d\u0640\u0001\u0000\u0000\u0000"+
		"\u063e\u063c\u0001\u0000\u0000\u0000\u063f\u060c\u0001\u0000\u0000\u0000"+
		"\u063f\u0610\u0001\u0000\u0000\u0000\u063f\u0616\u0001\u0000\u0000\u0000"+
		"\u063f\u061d\u0001\u0000\u0000\u0000\u063f\u0624\u0001\u0000\u0000\u0000"+
		"\u0640Y\u0001\u0000\u0000\u0000\u0641\u0642\u0005\u00a5\u0000\u0000\u0642"+
		"\u0643\u0005 \u0000\u0000\u0643\u0648\u0003b1\u0000\u0644\u0645\u0005"+
		"\u0002\u0000\u0000\u0645\u0647\u0003b1\u0000\u0646\u0644\u0001\u0000\u0000"+
		"\u0000\u0647\u064a\u0001\u0000\u0000\u0000\u0648\u0646\u0001\u0000\u0000"+
		"\u0000\u0648\u0649\u0001\u0000\u0000\u0000\u0649\u064c\u0001\u0000\u0000"+
		"\u0000\u064a\u0648\u0001\u0000\u0000\u0000\u064b\u0641\u0001\u0000\u0000"+
		"\u0000\u064b\u064c\u0001\u0000\u0000\u0000\u064c\u0657\u0001\u0000\u0000"+
		"\u0000\u064d\u064e\u0005(\u0000\u0000\u064e\u064f\u0005 \u0000\u0000\u064f"+
		"\u0654\u0003\u00d0h\u0000\u0650\u0651\u0005\u0002\u0000\u0000\u0651\u0653"+
		"\u0003\u00d0h\u0000\u0652\u0650\u0001\u0000\u0000\u0000\u0653\u0656\u0001"+
		"\u0000\u0000\u0000\u0654\u0652\u0001\u0000\u0000\u0000\u0654\u0655\u0001"+
		"\u0000\u0000\u0000\u0655\u0658\u0001\u0000\u0000\u0000\u0656\u0654\u0001"+
		"\u0000\u0000\u0000\u0657\u064d\u0001\u0000\u0000\u0000\u0657\u0658\u0001"+
		"\u0000\u0000\u0000\u0658\u0663\u0001\u0000\u0000\u0000\u0659\u065a\u0005"+
		"M\u0000\u0000\u065a\u065b\u0005 \u0000\u0000\u065b\u0660\u0003\u00d0h"+
		"\u0000\u065c\u065d\u0005\u0002\u0000\u0000\u065d\u065f\u0003\u00d0h\u0000"+
		"\u065e\u065c\u0001\u0000\u0000\u0000\u065f\u0662\u0001\u0000\u0000\u0000"+
		"\u0660\u065e\u0001\u0000\u0000\u0000\u0660\u0661\u0001\u0000\u0000\u0000"+
		"\u0661\u0664\u0001\u0000\u0000\u0000\u0662\u0660\u0001\u0000\u0000\u0000"+
		"\u0663\u0659\u0001\u0000\u0000\u0000\u0663\u0664\u0001\u0000\u0000\u0000"+
		"\u0664\u066f\u0001\u0000\u0000\u0000\u0665\u0666\u0005\u00de\u0000\u0000"+
		"\u0666\u0667\u0005 \u0000\u0000\u0667\u066c\u0003b1\u0000\u0668\u0669"+
		"\u0005\u0002\u0000\u0000\u0669\u066b\u0003b1\u0000\u066a\u0668\u0001\u0000"+
		"\u0000\u0000\u066b\u066e\u0001\u0000\u0000\u0000\u066c\u066a\u0001\u0000"+
		"\u0000\u0000\u066c\u066d\u0001\u0000\u0000\u0000\u066d\u0670\u0001\u0000"+
		"\u0000\u0000\u066e\u066c\u0001\u0000\u0000\u0000\u066f\u0665\u0001\u0000"+
		"\u0000\u0000\u066f\u0670\u0001\u0000\u0000\u0000\u0670\u0672\u0001\u0000"+
		"\u0000\u0000\u0671\u0673\u0003\u0104\u0082\u0000\u0672\u0671\u0001\u0000"+
		"\u0000\u0000\u0672\u0673\u0001\u0000\u0000\u0000\u0673\u0679\u0001\u0000"+
		"\u0000\u0000\u0674\u0677\u0005\u0088\u0000\u0000\u0675\u0678\u0005\u0010"+
		"\u0000\u0000\u0676\u0678\u0003\u00d0h\u0000\u0677\u0675\u0001\u0000\u0000"+
		"\u0000\u0677\u0676\u0001\u0000\u0000\u0000\u0678\u067a\u0001\u0000\u0000"+
		"\u0000\u0679\u0674\u0001\u0000\u0000\u0000\u0679\u067a\u0001\u0000\u0000"+
		"\u0000\u067a[\u0001\u0000\u0000\u0000\u067b\u067c\u0003.\u0017\u0000\u067c"+
		"\u067d\u0003f3\u0000\u067d]\u0001\u0000\u0000\u0000\u067e\u067f\u0006"+
		"/\uffff\uffff\u0000\u067f\u0680\u0003`0\u0000\u0680\u0698\u0001\u0000"+
		"\u0000\u0000\u0681\u0682\n\u0003\u0000\u0000\u0682\u0683\u0004/\u0001"+
		"\u0000\u0683\u0685\u0007\u000f\u0000\u0000\u0684\u0686\u0003\u0098L\u0000"+
		"\u0685\u0684\u0001\u0000\u0000\u0000\u0685\u0686\u0001\u0000\u0000\u0000"+
		"\u0686\u0687\u0001\u0000\u0000\u0000\u0687\u0697\u0003^/\u0004\u0688\u0689"+
		"\n\u0002\u0000\u0000\u0689\u068a\u0004/\u0003\u0000\u068a\u068c\u0005"+
		"{\u0000\u0000\u068b\u068d\u0003\u0098L\u0000\u068c\u068b\u0001\u0000\u0000"+
		"\u0000\u068c\u068d\u0001\u0000\u0000\u0000\u068d\u068e\u0001\u0000\u0000"+
		"\u0000\u068e\u0697\u0003^/\u0003\u068f\u0690\n\u0001\u0000\u0000\u0690"+
		"\u0691\u0004/\u0005\u0000\u0691\u0693\u0007\u0010\u0000\u0000\u0692\u0694"+
		"\u0003\u0098L\u0000\u0693\u0692\u0001\u0000\u0000\u0000\u0693\u0694\u0001"+
		"\u0000\u0000\u0000\u0694\u0695\u0001\u0000\u0000\u0000\u0695\u0697\u0003"+
		"^/\u0002\u0696\u0681\u0001\u0000\u0000\u0000\u0696\u0688\u0001\u0000\u0000"+
		"\u0000\u0696\u068f\u0001\u0000\u0000\u0000\u0697\u069a\u0001\u0000\u0000"+
		"\u0000\u0698\u0696\u0001\u0000\u0000\u0000\u0698\u0699\u0001\u0000\u0000"+
		"\u0000\u0699_\u0001\u0000\u0000\u0000\u069a\u0698\u0001\u0000\u0000\u0000"+
		"\u069b\u06a5\u0003h4\u0000\u069c\u06a5\u0003d2\u0000\u069d\u069e\u0005"+
		"\u00e8\u0000\u0000\u069e\u06a5\u0003\u00be_\u0000\u069f\u06a5\u0003\u00b4"+
		"Z\u0000\u06a0\u06a1\u0005\u0001\u0000\u0000\u06a1\u06a2\u0003,\u0016\u0000"+
		"\u06a2\u06a3\u0005\u0003\u0000\u0000\u06a3\u06a5\u0001\u0000\u0000\u0000"+
		"\u06a4\u069b\u0001\u0000\u0000\u0000\u06a4\u069c\u0001\u0000\u0000\u0000"+
		"\u06a4\u069d\u0001\u0000\u0000\u0000\u06a4\u069f\u0001\u0000\u0000\u0000"+
		"\u06a4\u06a0\u0001\u0000\u0000\u0000\u06a5a\u0001\u0000\u0000\u0000\u06a6"+
		"\u06a8\u0003\u00d0h\u0000\u06a7\u06a9\u0007\u0011\u0000\u0000\u06a8\u06a7"+
		"\u0001\u0000\u0000\u0000\u06a8\u06a9\u0001\u0000\u0000\u0000\u06a9\u06ac"+
		"\u0001\u0000\u0000\u0000\u06aa\u06ab\u0005\u009e\u0000\u0000\u06ab\u06ad"+
		"\u0007\u0012\u0000\u0000\u06ac\u06aa\u0001\u0000\u0000\u0000\u06ac\u06ad"+
		"\u0001\u0000\u0000\u0000\u06adc\u0001\u0000\u0000\u0000\u06ae\u06b0\u0003"+
		"\u0084B\u0000\u06af\u06b1\u0003f3\u0000\u06b0\u06af\u0001\u0000\u0000"+
		"\u0000\u06b1\u06b2\u0001\u0000\u0000\u0000\u06b2\u06b0\u0001\u0000\u0000"+
		"\u0000\u06b2\u06b3\u0001\u0000\u0000\u0000\u06b3e\u0001\u0000\u0000\u0000"+
		"\u06b4\u06b6\u0003j5\u0000\u06b5\u06b7\u0003|>\u0000\u06b6\u06b5\u0001"+
		"\u0000\u0000\u0000\u06b6\u06b7\u0001\u0000\u0000\u0000\u06b7\u06b8\u0001"+
		"\u0000\u0000\u0000\u06b8\u06b9\u0003Z-\u0000\u06b9\u06d0\u0001\u0000\u0000"+
		"\u0000\u06ba\u06be\u0003l6\u0000\u06bb\u06bd\u0003\u0096K\u0000\u06bc"+
		"\u06bb\u0001\u0000\u0000\u0000\u06bd\u06c0\u0001\u0000\u0000\u0000\u06be"+
		"\u06bc\u0001\u0000\u0000\u0000\u06be\u06bf\u0001\u0000\u0000\u0000\u06bf"+
		"\u06c2\u0001\u0000\u0000\u0000\u06c0\u06be\u0001\u0000\u0000\u0000\u06c1"+
		"\u06c3\u0003|>\u0000\u06c2\u06c1\u0001\u0000\u0000\u0000\u06c2\u06c3\u0001"+
		"\u0000\u0000\u0000\u06c3\u06c5\u0001\u0000\u0000\u0000\u06c4\u06c6\u0003"+
		"\u0086C\u0000\u06c5\u06c4\u0001\u0000\u0000\u0000\u06c5\u06c6\u0001\u0000"+
		"\u0000\u0000\u06c6\u06c8\u0001\u0000\u0000\u0000\u06c7\u06c9\u0003~?\u0000"+
		"\u06c8\u06c7\u0001\u0000\u0000\u0000\u06c8\u06c9\u0001\u0000\u0000\u0000"+
		"\u06c9\u06cb\u0001\u0000\u0000\u0000\u06ca\u06cc\u0003\u0104\u0082\u0000"+
		"\u06cb\u06ca\u0001\u0000\u0000\u0000\u06cb\u06cc\u0001\u0000\u0000\u0000"+
		"\u06cc\u06cd\u0001\u0000\u0000\u0000\u06cd\u06ce\u0003Z-\u0000\u06ce\u06d0"+
		"\u0001\u0000\u0000\u0000\u06cf\u06b4\u0001\u0000\u0000\u0000\u06cf\u06ba"+
		"\u0001\u0000\u0000\u0000\u06d0g\u0001\u0000\u0000\u0000\u06d1\u06d3\u0003"+
		"j5\u0000\u06d2\u06d4\u0003\u0084B\u0000\u06d3\u06d2\u0001\u0000\u0000"+
		"\u0000\u06d3\u06d4\u0001\u0000\u0000\u0000\u06d4\u06d8\u0001\u0000\u0000"+
		"\u0000\u06d5\u06d7\u0003\u0096K\u0000\u06d6\u06d5\u0001\u0000\u0000\u0000"+
		"\u06d7\u06da\u0001\u0000\u0000\u0000\u06d8\u06d6\u0001\u0000\u0000\u0000"+
		"\u06d8\u06d9\u0001\u0000\u0000\u0000\u06d9\u06dc\u0001\u0000\u0000\u0000"+
		"\u06da\u06d8\u0001\u0000\u0000\u0000\u06db\u06dd\u0003|>\u0000\u06dc\u06db"+
		"\u0001\u0000\u0000\u0000\u06dc\u06dd\u0001\u0000\u0000\u0000\u06dd\u06df"+
		"\u0001\u0000\u0000\u0000\u06de\u06e0\u0003\u0086C\u0000\u06df\u06de\u0001"+
		"\u0000\u0000\u0000\u06df\u06e0\u0001\u0000\u0000\u0000\u06e0\u06e2\u0001"+
		"\u0000\u0000\u0000\u06e1\u06e3\u0003~?\u0000\u06e2\u06e1\u0001\u0000\u0000"+
		"\u0000\u06e2\u06e3\u0001\u0000\u0000\u0000\u06e3\u06e5\u0001\u0000\u0000"+
		"\u0000\u06e4\u06e6\u0003\u0104\u0082\u0000\u06e5\u06e4\u0001\u0000\u0000"+
		"\u0000\u06e5\u06e6\u0001\u0000\u0000\u0000\u06e6\u06fe\u0001\u0000\u0000"+
		"\u0000\u06e7\u06e9\u0003l6\u0000\u06e8\u06ea\u0003\u0084B\u0000\u06e9"+
		"\u06e8\u0001\u0000\u0000\u0000\u06e9\u06ea\u0001\u0000\u0000\u0000\u06ea"+
		"\u06ee\u0001\u0000\u0000\u0000\u06eb\u06ed\u0003\u0096K\u0000\u06ec\u06eb"+
		"\u0001\u0000\u0000\u0000\u06ed\u06f0\u0001\u0000\u0000\u0000\u06ee\u06ec"+
		"\u0001\u0000\u0000\u0000\u06ee\u06ef\u0001\u0000\u0000\u0000\u06ef\u06f2"+
		"\u0001\u0000\u0000\u0000\u06f0\u06ee\u0001\u0000\u0000\u0000\u06f1\u06f3"+
		"\u0003|>\u0000\u06f2\u06f1\u0001\u0000\u0000\u0000\u06f2\u06f3\u0001\u0000"+
		"\u0000\u0000\u06f3\u06f5\u0001\u0000\u0000\u0000\u06f4\u06f6\u0003\u0086"+
		"C\u0000\u06f5\u06f4\u0001\u0000\u0000\u0000\u06f5\u06f6\u0001\u0000\u0000"+
		"\u0000\u06f6\u06f8\u0001\u0000\u0000\u0000\u06f7\u06f9\u0003~?\u0000\u06f8"+
		"\u06f7\u0001\u0000\u0000\u0000\u06f8\u06f9\u0001\u0000\u0000\u0000\u06f9"+
		"\u06fb\u0001\u0000\u0000\u0000\u06fa\u06fc\u0003\u0104\u0082\u0000\u06fb"+
		"\u06fa\u0001\u0000\u0000\u0000\u06fb\u06fc\u0001\u0000\u0000\u0000\u06fc"+
		"\u06fe\u0001\u0000\u0000\u0000\u06fd\u06d1\u0001\u0000\u0000\u0000\u06fd"+
		"\u06e7\u0001\u0000\u0000\u0000\u06fei\u0001\u0000\u0000\u0000\u06ff\u0700"+
		"\u0005\u00d2\u0000\u0000\u0700\u0701\u0005\u00f5\u0000\u0000\u0701\u0703"+
		"\u0005\u0001\u0000\u0000\u0702\u0704\u0003\u0098L\u0000\u0703\u0702\u0001"+
		"\u0000\u0000\u0000\u0703\u0704\u0001\u0000\u0000\u0000\u0704\u0705\u0001"+
		"\u0000\u0000\u0000\u0705\u0706\u0003\u00d2i\u0000\u0706\u0707\u0005\u0003"+
		"\u0000\u0000\u0707\u0713\u0001\u0000\u0000\u0000\u0708\u070a\u0005\u0092"+
		"\u0000\u0000\u0709\u070b\u0003\u0098L\u0000\u070a\u0709\u0001\u0000\u0000"+
		"\u0000\u070a\u070b\u0001\u0000\u0000\u0000\u070b\u070c\u0001\u0000\u0000"+
		"\u0000\u070c\u0713\u0003\u00d2i\u0000\u070d\u070f\u0005\u00be\u0000\u0000"+
		"\u070e\u0710\u0003\u0098L\u0000\u070f\u070e\u0001\u0000\u0000\u0000\u070f"+
		"\u0710\u0001\u0000\u0000\u0000\u0710\u0711\u0001\u0000\u0000\u0000\u0711"+
		"\u0713\u0003\u00d2i\u0000\u0712\u06ff\u0001\u0000\u0000\u0000\u0712\u0708"+
		"\u0001\u0000\u0000\u0000\u0712\u070d\u0001\u0000\u0000\u0000\u0713\u0715"+
		"\u0001\u0000\u0000\u0000\u0714\u0716\u0003\u00ba]\u0000\u0715\u0714\u0001"+
		"\u0000\u0000\u0000\u0715\u0716\u0001\u0000\u0000\u0000\u0716\u0719\u0001"+
		"\u0000\u0000\u0000\u0717\u0718\u0005\u00bc\u0000\u0000\u0718\u071a\u0005"+
		"\u0122\u0000\u0000\u0719\u0717\u0001\u0000\u0000\u0000\u0719\u071a\u0001"+
		"\u0000\u0000\u0000\u071a\u071b\u0001\u0000\u0000\u0000\u071b\u071c\u0005"+
		"\u0106\u0000\u0000\u071c\u0729\u0005\u0122\u0000\u0000\u071d\u0727\u0005"+
		"\u0018\u0000\u0000\u071e\u0728\u0003\u00a8T\u0000\u071f\u0728\u0003\u00fa"+
		"}\u0000\u0720\u0723\u0005\u0001\u0000\u0000\u0721\u0724\u0003\u00a8T\u0000"+
		"\u0722\u0724\u0003\u00fa}\u0000\u0723\u0721\u0001\u0000\u0000\u0000\u0723"+
		"\u0722\u0001\u0000\u0000\u0000\u0724\u0725\u0001\u0000\u0000\u0000\u0725"+
		"\u0726\u0005\u0003\u0000\u0000\u0726\u0728\u0001\u0000\u0000\u0000\u0727"+
		"\u071e\u0001\u0000\u0000\u0000\u0727\u071f\u0001\u0000\u0000\u0000\u0727"+
		"\u0720\u0001\u0000\u0000\u0000\u0728\u072a\u0001\u0000\u0000\u0000\u0729"+
		"\u071d\u0001\u0000\u0000\u0000\u0729\u072a\u0001\u0000\u0000\u0000\u072a"+
		"\u072c\u0001\u0000\u0000\u0000\u072b\u072d\u0003\u00ba]\u0000\u072c\u072b"+
		"\u0001\u0000\u0000\u0000\u072c\u072d\u0001\u0000\u0000\u0000\u072d\u0730"+
		"\u0001\u0000\u0000\u0000\u072e\u072f\u0005\u00bb\u0000\u0000\u072f\u0731"+
		"\u0005\u0122\u0000\u0000\u0730\u072e\u0001\u0000\u0000\u0000\u0730\u0731"+
		"\u0001\u0000\u0000\u0000\u0731k\u0001\u0000\u0000\u0000\u0732\u0736\u0005"+
		"\u00d2\u0000\u0000\u0733\u0735\u0003\u0080@\u0000\u0734\u0733\u0001\u0000"+
		"\u0000\u0000\u0735\u0738\u0001\u0000\u0000\u0000\u0736\u0734\u0001\u0000"+
		"\u0000\u0000\u0736\u0737\u0001\u0000\u0000\u0000\u0737\u073a\u0001\u0000"+
		"\u0000\u0000\u0738\u0736\u0001\u0000\u0000\u0000\u0739\u073b\u0003\u0098"+
		"L\u0000\u073a\u0739\u0001\u0000\u0000\u0000\u073a\u073b\u0001\u0000\u0000"+
		"\u0000\u073b\u073c\u0001\u0000\u0000\u0000\u073c\u073d\u0003\u00c6c\u0000"+
		"\u073dm\u0001\u0000\u0000\u0000\u073e\u073f\u0005\u00d8\u0000\u0000\u073f"+
		"\u0740\u0003x<\u0000\u0740o\u0001\u0000\u0000\u0000\u0741\u0742\u0005"+
		"\u010a\u0000\u0000\u0742\u0745\u0005\u0093\u0000\u0000\u0743\u0744\u0005"+
		"\u0013\u0000\u0000\u0744\u0746\u0003\u00d4j\u0000\u0745\u0743\u0001\u0000"+
		"\u0000\u0000\u0745\u0746\u0001\u0000\u0000\u0000\u0746\u0747\u0001\u0000"+
		"\u0000\u0000\u0747\u0748\u0005\u00ee\u0000\u0000\u0748\u0749\u0003t:\u0000"+
		"\u0749q\u0001\u0000\u0000\u0000\u074a\u074b\u0005\u010a\u0000\u0000\u074b"+
		"\u074c\u0005\u009c\u0000\u0000\u074c\u074f\u0005\u0093\u0000\u0000\u074d"+
		"\u074e\u0005\u0013\u0000\u0000\u074e\u0750\u0003\u00d4j\u0000\u074f\u074d"+
		"\u0001\u0000\u0000\u0000\u074f\u0750\u0001\u0000\u0000\u0000\u0750\u0751"+
		"\u0001\u0000\u0000\u0000\u0751\u0752\u0005\u00ee\u0000\u0000\u0752\u0753"+
		"\u0003v;\u0000\u0753s\u0001\u0000\u0000\u0000\u0754\u075c\u0005E\u0000"+
		"\u0000\u0755\u0756\u0005\u0103\u0000\u0000\u0756\u0757\u0005\u00d8\u0000"+
		"\u0000\u0757\u075c\u0005\u011a\u0000\u0000\u0758\u0759\u0005\u0103\u0000"+
		"\u0000\u0759\u075a\u0005\u00d8\u0000\u0000\u075a\u075c\u0003x<\u0000\u075b"+
		"\u0754\u0001\u0000\u0000\u0000\u075b\u0755\u0001\u0000\u0000\u0000\u075b"+
		"\u0758\u0001\u0000\u0000\u0000\u075cu\u0001\u0000\u0000\u0000\u075d\u075e"+
		"\u0005z\u0000\u0000\u075e\u0770\u0005\u011a\u0000\u0000\u075f\u0760\u0005"+
		"z\u0000\u0000\u0760\u0761\u0005\u0001\u0000\u0000\u0761\u0762\u0003\u00bc"+
		"^\u0000\u0762\u0763\u0005\u0003\u0000\u0000\u0763\u0764\u0005\u0107\u0000"+
		"\u0000\u0764\u0765\u0005\u0001\u0000\u0000\u0765\u076a\u0003\u00d0h\u0000"+
		"\u0766\u0767\u0005\u0002\u0000\u0000\u0767\u0769\u0003\u00d0h\u0000\u0768"+
		"\u0766\u0001\u0000\u0000\u0000\u0769\u076c\u0001\u0000\u0000\u0000\u076a"+
		"\u0768\u0001\u0000\u0000\u0000\u076a\u076b\u0001\u0000\u0000\u0000\u076b"+
		"\u076d\u0001\u0000\u0000\u0000\u076c\u076a\u0001\u0000\u0000\u0000\u076d"+
		"\u076e\u0005\u0003\u0000\u0000\u076e\u0770\u0001\u0000\u0000\u0000\u076f"+
		"\u075d\u0001\u0000\u0000\u0000\u076f\u075f\u0001\u0000\u0000\u0000\u0770"+
		"w\u0001\u0000\u0000\u0000\u0771\u0776\u0003z=\u0000\u0772\u0773\u0005"+
		"\u0002\u0000\u0000\u0773\u0775\u0003z=\u0000\u0774\u0772\u0001\u0000\u0000"+
		"\u0000\u0775\u0778\u0001\u0000\u0000\u0000\u0776\u0774\u0001\u0000\u0000"+
		"\u0000\u0776\u0777\u0001\u0000\u0000\u0000\u0777y\u0001\u0000\u0000\u0000"+
		"\u0778\u0776\u0001\u0000\u0000\u0000\u0779\u077a\u0003\u00be_\u0000\u077a"+
		"\u077b\u0005\u0110\u0000\u0000\u077b\u077c\u0003\u00d0h\u0000\u077c{\u0001"+
		"\u0000\u0000\u0000\u077d\u077e\u0005\u010b\u0000\u0000\u077e\u077f\u0003"+
		"\u00d4j\u0000\u077f}\u0001\u0000\u0000\u0000\u0780\u0781\u0005o\u0000"+
		"\u0000\u0781\u0782\u0003\u00d4j\u0000\u0782\u007f\u0001\u0000\u0000\u0000"+
		"\u0783\u0784\u0005\u0006\u0000\u0000\u0784\u078b\u0003\u0082A\u0000\u0785"+
		"\u0787\u0005\u0002\u0000\u0000\u0786\u0785\u0001\u0000\u0000\u0000\u0786"+
		"\u0787\u0001\u0000\u0000\u0000\u0787\u0788\u0001\u0000\u0000\u0000\u0788"+
		"\u078a\u0003\u0082A\u0000\u0789\u0786\u0001\u0000\u0000\u0000\u078a\u078d"+
		"\u0001\u0000\u0000\u0000\u078b\u0789\u0001\u0000\u0000\u0000\u078b\u078c"+
		"\u0001\u0000\u0000\u0000\u078c\u078e\u0001\u0000\u0000\u0000\u078d\u078b"+
		"\u0001\u0000\u0000\u0000\u078e\u078f\u0005\u0007\u0000\u0000\u078f\u0081"+
		"\u0001\u0000\u0000\u0000\u0790\u079e\u0003\u0118\u008c\u0000\u0791\u0792"+
		"\u0003\u0118\u008c\u0000\u0792\u0793\u0005\u0001\u0000\u0000\u0793\u0798"+
		"\u0003\u00dam\u0000\u0794\u0795\u0005\u0002\u0000\u0000\u0795\u0797\u0003"+
		"\u00dam\u0000\u0796\u0794\u0001\u0000\u0000\u0000\u0797\u079a\u0001\u0000"+
		"\u0000\u0000\u0798\u0796\u0001\u0000\u0000\u0000\u0798\u0799\u0001\u0000"+
		"\u0000\u0000\u0799\u079b\u0001\u0000\u0000\u0000\u079a\u0798\u0001\u0000"+
		"\u0000\u0000\u079b\u079c\u0005\u0003\u0000\u0000\u079c\u079e\u0001\u0000"+
		"\u0000\u0000\u079d\u0790\u0001\u0000\u0000\u0000\u079d\u0791\u0001\u0000"+
		"\u0000\u0000\u079e\u0083\u0001\u0000\u0000\u0000\u079f\u07a0\u0005g\u0000"+
		"\u0000\u07a0\u07a5\u0003\u009aM\u0000\u07a1\u07a2\u0005\u0002\u0000\u0000"+
		"\u07a2\u07a4\u0003\u009aM\u0000\u07a3\u07a1\u0001\u0000\u0000\u0000\u07a4"+
		"\u07a7\u0001\u0000\u0000\u0000\u07a5\u07a3\u0001\u0000\u0000\u0000\u07a5"+
		"\u07a6\u0001\u0000\u0000\u0000\u07a6\u07ab\u0001\u0000\u0000\u0000\u07a7"+
		"\u07a5\u0001\u0000\u0000\u0000\u07a8\u07aa\u0003\u0096K\u0000\u07a9\u07a8"+
		"\u0001\u0000\u0000\u0000\u07aa\u07ad\u0001\u0000\u0000\u0000\u07ab\u07a9"+
		"\u0001\u0000\u0000\u0000\u07ab\u07ac\u0001\u0000\u0000\u0000\u07ac\u07af"+
		"\u0001\u0000\u0000\u0000\u07ad\u07ab\u0001\u0000\u0000\u0000\u07ae\u07b0"+
		"\u0003\u0090H\u0000\u07af\u07ae\u0001\u0000\u0000\u0000\u07af\u07b0\u0001"+
		"\u0000\u0000\u0000\u07b0\u0085\u0001\u0000\u0000\u0000\u07b1\u07b2\u0005"+
		"m\u0000\u0000\u07b2\u07b3\u0005 \u0000\u0000\u07b3\u07b8\u0003\u0088D"+
		"\u0000\u07b4\u07b5\u0005\u0002\u0000\u0000\u07b5\u07b7\u0003\u0088D\u0000"+
		"\u07b6\u07b4\u0001\u0000\u0000\u0000\u07b7\u07ba\u0001\u0000\u0000\u0000"+
		"\u07b8\u07b6\u0001\u0000\u0000\u0000\u07b8\u07b9\u0001\u0000\u0000\u0000"+
		"\u07b9\u07d9\u0001\u0000\u0000\u0000\u07ba\u07b8\u0001\u0000\u0000\u0000"+
		"\u07bb\u07bc\u0005m\u0000\u0000\u07bc\u07bd\u0005 \u0000\u0000\u07bd\u07c2"+
		"\u0003\u00d0h\u0000\u07be\u07bf\u0005\u0002\u0000\u0000\u07bf\u07c1\u0003"+
		"\u00d0h\u0000\u07c0\u07be\u0001\u0000\u0000\u0000\u07c1\u07c4\u0001\u0000"+
		"\u0000\u0000\u07c2\u07c0\u0001\u0000\u0000\u0000\u07c2\u07c3\u0001\u0000"+
		"\u0000\u0000\u07c3\u07d6\u0001\u0000\u0000\u0000\u07c4\u07c2\u0001\u0000"+
		"\u0000\u0000\u07c5\u07c6\u0005\u010d\u0000\u0000\u07c6\u07d7\u0005\u00cd"+
		"\u0000\u0000\u07c7\u07c8\u0005\u010d\u0000\u0000\u07c8\u07d7\u00059\u0000"+
		"\u0000\u07c9\u07ca\u0005n\u0000\u0000\u07ca\u07cb\u0005\u00da\u0000\u0000"+
		"\u07cb\u07cc\u0005\u0001\u0000\u0000\u07cc\u07d1\u0003\u008eG\u0000\u07cd"+
		"\u07ce\u0005\u0002\u0000\u0000\u07ce\u07d0\u0003\u008eG\u0000\u07cf\u07cd"+
		"\u0001\u0000\u0000\u0000\u07d0\u07d3\u0001\u0000\u0000\u0000\u07d1\u07cf"+
		"\u0001\u0000\u0000\u0000\u07d1\u07d2\u0001\u0000\u0000\u0000\u07d2\u07d4"+
		"\u0001\u0000\u0000\u0000\u07d3\u07d1\u0001\u0000\u0000\u0000\u07d4\u07d5"+
		"\u0005\u0003\u0000\u0000\u07d5\u07d7\u0001\u0000\u0000\u0000\u07d6\u07c5"+
		"\u0001\u0000\u0000\u0000\u07d6\u07c7\u0001\u0000\u0000\u0000\u07d6\u07c9"+
		"\u0001\u0000\u0000\u0000\u07d6\u07d7\u0001\u0000\u0000\u0000\u07d7\u07d9"+
		"\u0001\u0000\u0000\u0000\u07d8\u07b1\u0001\u0000\u0000\u0000\u07d8\u07bb"+
		"\u0001\u0000\u0000\u0000\u07d9\u0087\u0001\u0000\u0000\u0000\u07da\u07dd"+
		"\u0003\u008aE\u0000\u07db\u07dd\u0003\u00d0h\u0000\u07dc\u07da\u0001\u0000"+
		"\u0000\u0000\u07dc\u07db\u0001\u0000\u0000\u0000\u07dd\u0089\u0001\u0000"+
		"\u0000\u0000\u07de\u07df\u0007\u0013\u0000\u0000\u07df\u07e0\u0005\u0001"+
		"\u0000\u0000\u07e0\u07e5\u0003\u008eG\u0000\u07e1\u07e2\u0005\u0002\u0000"+
		"\u0000\u07e2\u07e4\u0003\u008eG\u0000\u07e3\u07e1\u0001\u0000\u0000\u0000"+
		"\u07e4\u07e7\u0001\u0000\u0000\u0000\u07e5\u07e3\u0001\u0000\u0000\u0000"+
		"\u07e5\u07e6\u0001\u0000\u0000\u0000\u07e6\u07e8\u0001\u0000\u0000\u0000"+
		"\u07e7\u07e5\u0001\u0000\u0000\u0000\u07e8\u07e9\u0005\u0003\u0000\u0000"+
		"\u07e9\u07f8\u0001\u0000\u0000\u0000\u07ea\u07eb\u0005n\u0000\u0000\u07eb"+
		"\u07ec\u0005\u00da\u0000\u0000\u07ec\u07ed\u0005\u0001\u0000\u0000\u07ed"+
		"\u07f2\u0003\u008cF\u0000\u07ee\u07ef\u0005\u0002\u0000\u0000\u07ef\u07f1"+
		"\u0003\u008cF\u0000\u07f0\u07ee\u0001\u0000\u0000\u0000\u07f1\u07f4\u0001"+
		"\u0000\u0000\u0000\u07f2\u07f0\u0001\u0000\u0000\u0000\u07f2\u07f3\u0001"+
		"\u0000\u0000\u0000\u07f3\u07f5\u0001\u0000\u0000\u0000\u07f4\u07f2\u0001"+
		"\u0000\u0000\u0000\u07f5\u07f6\u0005\u0003\u0000\u0000\u07f6\u07f8\u0001"+
		"\u0000\u0000\u0000\u07f7\u07de\u0001\u0000\u0000\u0000\u07f7\u07ea\u0001"+
		"\u0000\u0000\u0000\u07f8\u008b\u0001\u0000\u0000\u0000\u07f9\u07fc\u0003"+
		"\u008aE\u0000\u07fa\u07fc\u0003\u008eG\u0000\u07fb\u07f9\u0001\u0000\u0000"+
		"\u0000\u07fb\u07fa\u0001\u0000\u0000\u0000\u07fc\u008d\u0001\u0000\u0000"+
		"\u0000\u07fd\u0806\u0005\u0001\u0000\u0000\u07fe\u0803\u0003\u00d0h\u0000"+
		"\u07ff\u0800\u0005\u0002\u0000\u0000\u0800\u0802\u0003\u00d0h\u0000\u0801"+
		"\u07ff\u0001\u0000\u0000\u0000\u0802\u0805\u0001\u0000\u0000\u0000\u0803"+
		"\u0801\u0001\u0000\u0000\u0000\u0803\u0804\u0001\u0000\u0000\u0000\u0804"+
		"\u0807\u0001\u0000\u0000\u0000\u0805\u0803\u0001\u0000\u0000\u0000\u0806"+
		"\u07fe\u0001\u0000\u0000\u0000\u0806\u0807\u0001\u0000\u0000\u0000\u0807"+
		"\u0808\u0001\u0000\u0000\u0000\u0808\u080b\u0005\u0003\u0000\u0000\u0809"+
		"\u080b\u0003\u00d0h\u0000\u080a\u07fd\u0001\u0000\u0000\u0000\u080a\u0809"+
		"\u0001\u0000\u0000\u0000\u080b\u008f\u0001\u0000\u0000\u0000\u080c\u080d"+
		"\u0005\u00b1\u0000\u0000\u080d\u080e\u0005\u0001\u0000\u0000\u080e\u080f"+
		"\u0003\u00c6c\u0000\u080f\u0810\u0005c\u0000\u0000\u0810\u0811\u0003\u0092"+
		"I\u0000\u0811\u0812\u0005t\u0000\u0000\u0812\u0813\u0005\u0001\u0000\u0000"+
		"\u0813\u0818\u0003\u0094J\u0000\u0814\u0815\u0005\u0002\u0000\u0000\u0815"+
		"\u0817\u0003\u0094J\u0000\u0816\u0814\u0001\u0000\u0000\u0000\u0817\u081a"+
		"\u0001\u0000\u0000\u0000\u0818\u0816\u0001\u0000\u0000\u0000\u0818\u0819"+
		"\u0001\u0000\u0000\u0000\u0819\u081b\u0001\u0000\u0000\u0000\u081a\u0818"+
		"\u0001\u0000\u0000\u0000\u081b\u081c\u0005\u0003\u0000\u0000\u081c\u081d"+
		"\u0005\u0003\u0000\u0000\u081d\u0091\u0001\u0000\u0000\u0000\u081e\u082b"+
		"\u0003\u0118\u008c\u0000\u081f\u0820\u0005\u0001\u0000\u0000\u0820\u0825"+
		"\u0003\u0118\u008c\u0000\u0821\u0822\u0005\u0002\u0000\u0000\u0822\u0824"+
		"\u0003\u0118\u008c\u0000\u0823\u0821\u0001\u0000\u0000\u0000\u0824\u0827"+
		"\u0001\u0000\u0000\u0000\u0825\u0823\u0001\u0000\u0000\u0000\u0825\u0826"+
		"\u0001\u0000\u0000\u0000\u0826\u0828\u0001\u0000\u0000\u0000\u0827\u0825"+
		"\u0001\u0000\u0000\u0000\u0828\u0829\u0005\u0003\u0000\u0000\u0829\u082b"+
		"\u0001\u0000\u0000\u0000\u082a\u081e\u0001\u0000\u0000\u0000\u082a\u081f"+
		"\u0001\u0000\u0000\u0000\u082b\u0093\u0001\u0000\u0000\u0000\u082c\u0831"+
		"\u0003\u00d0h\u0000\u082d\u082f\u0005\u0018\u0000\u0000\u082e\u082d\u0001"+
		"\u0000\u0000\u0000\u082e\u082f\u0001\u0000\u0000\u0000\u082f\u0830\u0001"+
		"\u0000\u0000\u0000\u0830\u0832\u0003\u0118\u008c\u0000\u0831\u082e\u0001"+
		"\u0000\u0000\u0000\u0831\u0832\u0001\u0000\u0000\u0000\u0832\u0095\u0001"+
		"\u0000\u0000\u0000\u0833\u0834\u0005\u0083\u0000\u0000\u0834\u0836\u0005"+
		"\u0108\u0000\u0000\u0835\u0837\u0005\u00a7\u0000\u0000\u0836\u0835\u0001"+
		"\u0000\u0000\u0000\u0836\u0837\u0001\u0000\u0000\u0000\u0837\u0838\u0001"+
		"\u0000\u0000\u0000\u0838\u0839\u0003\u0112\u0089\u0000\u0839\u0842\u0005"+
		"\u0001\u0000\u0000\u083a\u083f\u0003\u00d0h\u0000\u083b\u083c\u0005\u0002"+
		"\u0000\u0000\u083c\u083e\u0003\u00d0h\u0000\u083d\u083b\u0001\u0000\u0000"+
		"\u0000\u083e\u0841\u0001\u0000\u0000\u0000\u083f\u083d\u0001\u0000\u0000"+
		"\u0000\u083f\u0840\u0001\u0000\u0000\u0000\u0840\u0843\u0001\u0000\u0000"+
		"\u0000\u0841\u083f\u0001\u0000\u0000\u0000\u0842\u083a\u0001\u0000\u0000"+
		"\u0000\u0842\u0843\u0001\u0000\u0000\u0000\u0843\u0844\u0001\u0000\u0000"+
		"\u0000\u0844\u0845\u0005\u0003\u0000\u0000\u0845\u0851\u0003\u0118\u008c"+
		"\u0000\u0846\u0848\u0005\u0018\u0000\u0000\u0847\u0846\u0001\u0000\u0000"+
		"\u0000\u0847\u0848\u0001\u0000\u0000\u0000\u0848\u0849\u0001\u0000\u0000"+
		"\u0000\u0849\u084e\u0003\u0118\u008c\u0000\u084a\u084b\u0005\u0002\u0000"+
		"\u0000\u084b\u084d\u0003\u0118\u008c\u0000\u084c\u084a\u0001\u0000\u0000"+
		"\u0000\u084d\u0850\u0001\u0000\u0000\u0000\u084e\u084c\u0001\u0000\u0000"+
		"\u0000\u084e\u084f\u0001";
	private static final String _serializedATNSegment1 =
		"\u0000\u0000\u0000\u084f\u0852\u0001\u0000\u0000\u0000\u0850\u084e\u0001"+
		"\u0000\u0000\u0000\u0851\u0847\u0001\u0000\u0000\u0000\u0851\u0852\u0001"+
		"\u0000\u0000\u0000\u0852\u0097\u0001\u0000\u0000\u0000\u0853\u0854\u0007"+
		"\u0014\u0000\u0000\u0854\u0099\u0001\u0000\u0000\u0000\u0855\u0857\u0005"+
		"\u0083\u0000\u0000\u0856\u0855\u0001\u0000\u0000\u0000\u0856\u0857\u0001"+
		"\u0000\u0000\u0000\u0857\u0858\u0001\u0000\u0000\u0000\u0858\u085c\u0003"+
		"\u00b2Y\u0000\u0859\u085b\u0003\u009cN\u0000\u085a\u0859\u0001\u0000\u0000"+
		"\u0000\u085b\u085e\u0001\u0000\u0000\u0000\u085c\u085a\u0001\u0000\u0000"+
		"\u0000\u085c\u085d\u0001\u0000\u0000\u0000\u085d\u009b\u0001\u0000\u0000"+
		"\u0000\u085e\u085c\u0001\u0000\u0000\u0000\u085f\u0860\u0003\u009eO\u0000"+
		"\u0860\u0862\u0005\u0080\u0000\u0000\u0861\u0863\u0005\u0083\u0000\u0000"+
		"\u0862\u0861\u0001\u0000\u0000\u0000\u0862\u0863\u0001\u0000\u0000\u0000"+
		"\u0863\u0864\u0001\u0000\u0000\u0000\u0864\u0866\u0003\u00b2Y\u0000\u0865"+
		"\u0867\u0003\u00a0P\u0000\u0866\u0865\u0001\u0000\u0000\u0000\u0866\u0867"+
		"\u0001\u0000\u0000\u0000\u0867\u0871\u0001\u0000\u0000\u0000\u0868\u0869"+
		"\u0005\u009a\u0000\u0000\u0869\u086a\u0003\u009eO\u0000\u086a\u086c\u0005"+
		"\u0080\u0000\u0000\u086b\u086d\u0005\u0083\u0000\u0000\u086c\u086b\u0001"+
		"\u0000\u0000\u0000\u086c\u086d\u0001\u0000\u0000\u0000\u086d\u086e\u0001"+
		"\u0000\u0000\u0000\u086e\u086f\u0003\u00b2Y\u0000\u086f\u0871\u0001\u0000"+
		"\u0000\u0000\u0870\u085f\u0001\u0000\u0000\u0000\u0870\u0868\u0001\u0000"+
		"\u0000\u0000\u0871\u009d\u0001\u0000\u0000\u0000\u0872\u0874\u0005w\u0000"+
		"\u0000\u0873\u0872\u0001\u0000\u0000\u0000\u0873\u0874\u0001\u0000\u0000"+
		"\u0000\u0874\u088b\u0001\u0000\u0000\u0000\u0875\u088b\u00058\u0000\u0000"+
		"\u0876\u0878\u0005\u0086\u0000\u0000\u0877\u0879\u0005\u00a7\u0000\u0000"+
		"\u0878\u0877\u0001\u0000\u0000\u0000\u0878\u0879\u0001\u0000\u0000\u0000"+
		"\u0879\u088b\u0001\u0000\u0000\u0000\u087a\u087c\u0005\u0086\u0000\u0000"+
		"\u087b\u087a\u0001\u0000\u0000\u0000\u087b\u087c\u0001\u0000\u0000\u0000"+
		"\u087c\u087d\u0001\u0000\u0000\u0000\u087d\u088b\u0005\u00d3\u0000\u0000"+
		"\u087e\u0880\u0005\u00c8\u0000\u0000\u087f\u0881\u0005\u00a7\u0000\u0000"+
		"\u0880\u087f\u0001\u0000\u0000\u0000\u0880\u0881\u0001\u0000\u0000\u0000"+
		"\u0881\u088b\u0001\u0000\u0000\u0000\u0882\u0884\u0005h\u0000\u0000\u0883"+
		"\u0885\u0005\u00a7\u0000\u0000\u0884\u0883\u0001\u0000\u0000\u0000\u0884"+
		"\u0885\u0001\u0000\u0000\u0000\u0885\u088b\u0001\u0000\u0000\u0000\u0886"+
		"\u0888\u0005\u0086\u0000\u0000\u0887\u0886\u0001\u0000\u0000\u0000\u0887"+
		"\u0888\u0001\u0000\u0000\u0000\u0888\u0889\u0001\u0000\u0000\u0000\u0889"+
		"\u088b\u0005\u0014\u0000\u0000\u088a\u0873\u0001\u0000\u0000\u0000\u088a"+
		"\u0875\u0001\u0000\u0000\u0000\u088a\u0876\u0001\u0000\u0000\u0000\u088a"+
		"\u087b\u0001\u0000\u0000\u0000\u088a\u087e\u0001\u0000\u0000\u0000\u088a"+
		"\u0882\u0001\u0000\u0000\u0000\u088a\u0887\u0001\u0000\u0000\u0000\u088b"+
		"\u009f\u0001\u0000\u0000\u0000\u088c\u088d\u0005\u00a0\u0000\u0000\u088d"+
		"\u0891\u0003\u00d4j\u0000\u088e\u088f\u0005\u0106\u0000\u0000\u088f\u0891"+
		"\u0003\u00a6S\u0000\u0890\u088c\u0001\u0000\u0000\u0000\u0890\u088e\u0001"+
		"\u0000\u0000\u0000\u0891\u00a1\u0001\u0000\u0000\u0000\u0892\u0893\u0005"+
		"\u00ea\u0000\u0000\u0893\u0895\u0005\u0001\u0000\u0000\u0894\u0896\u0003"+
		"\u00a4R\u0000\u0895\u0894\u0001\u0000\u0000\u0000\u0895\u0896\u0001\u0000"+
		"\u0000\u0000\u0896\u0897\u0001\u0000\u0000\u0000\u0897\u0898\u0005\u0003"+
		"\u0000\u0000\u0898\u00a3\u0001\u0000\u0000\u0000\u0899\u089b\u0005\u0119"+
		"\u0000\u0000\u089a\u0899\u0001\u0000\u0000\u0000\u089a\u089b\u0001\u0000"+
		"\u0000\u0000\u089b\u089c\u0001\u0000\u0000\u0000\u089c\u089d\u0007\u0015"+
		"\u0000\u0000\u089d\u08b2\u0005\u00b0\u0000\u0000\u089e\u089f\u0003\u00d0"+
		"h\u0000\u089f\u08a0\u0005\u00cf\u0000\u0000\u08a0\u08b2\u0001\u0000\u0000"+
		"\u0000\u08a1\u08a2\u0005\u001e\u0000\u0000\u08a2\u08a3\u0005\u0126\u0000"+
		"\u0000\u08a3\u08a4\u0005\u00a6\u0000\u0000\u08a4\u08a5\u0005\u009f\u0000"+
		"\u0000\u08a5\u08ae\u0005\u0126\u0000\u0000\u08a6\u08ac\u0005\u00a0\u0000"+
		"\u0000\u08a7\u08ad\u0003\u0118\u008c\u0000\u08a8\u08a9\u0003\u0112\u0089"+
		"\u0000\u08a9\u08aa\u0005\u0001\u0000\u0000\u08aa\u08ab\u0005\u0003\u0000"+
		"\u0000\u08ab\u08ad\u0001\u0000\u0000\u0000\u08ac\u08a7\u0001\u0000\u0000"+
		"\u0000\u08ac\u08a8\u0001\u0000\u0000\u0000\u08ad\u08af\u0001\u0000\u0000"+
		"\u0000\u08ae\u08a6\u0001\u0000\u0000\u0000\u08ae\u08af\u0001\u0000\u0000"+
		"\u0000\u08af\u08b2\u0001\u0000\u0000\u0000\u08b0\u08b2\u0003\u00d0h\u0000"+
		"\u08b1\u089a\u0001\u0000\u0000\u0000\u08b1\u089e\u0001\u0000\u0000\u0000"+
		"\u08b1\u08a1\u0001\u0000\u0000\u0000\u08b1\u08b0\u0001\u0000\u0000\u0000"+
		"\u08b2\u00a5\u0001\u0000\u0000\u0000\u08b3\u08b4\u0005\u0001\u0000\u0000"+
		"\u08b4\u08b5\u0003\u00a8T\u0000\u08b5\u08b6\u0005\u0003\u0000\u0000\u08b6"+
		"\u00a7\u0001\u0000\u0000\u0000\u08b7\u08bc\u0003\u0114\u008a\u0000\u08b8"+
		"\u08b9\u0005\u0002\u0000\u0000\u08b9\u08bb\u0003\u0114\u008a\u0000\u08ba"+
		"\u08b8\u0001\u0000\u0000\u0000\u08bb\u08be\u0001\u0000\u0000\u0000\u08bc"+
		"\u08ba\u0001\u0000\u0000\u0000\u08bc\u08bd\u0001\u0000\u0000\u0000\u08bd"+
		"\u00a9\u0001\u0000\u0000\u0000\u08be\u08bc\u0001\u0000\u0000\u0000\u08bf"+
		"\u08c0\u0005\u0001\u0000\u0000\u08c0\u08c5\u0003\u00acV\u0000\u08c1\u08c2"+
		"\u0005\u0002\u0000\u0000\u08c2\u08c4\u0003\u00acV\u0000\u08c3\u08c1\u0001"+
		"\u0000\u0000\u0000\u08c4\u08c7\u0001\u0000\u0000\u0000\u08c5\u08c3\u0001"+
		"\u0000\u0000\u0000\u08c5\u08c6\u0001\u0000\u0000\u0000\u08c6\u08c8\u0001"+
		"\u0000\u0000\u0000\u08c7\u08c5\u0001\u0000\u0000\u0000\u08c8\u08c9\u0005"+
		"\u0003\u0000\u0000\u08c9\u00ab\u0001\u0000\u0000\u0000\u08ca\u08cc\u0003"+
		"\u0114\u008a\u0000\u08cb\u08cd\u0007\u0011\u0000\u0000\u08cc\u08cb\u0001"+
		"\u0000\u0000\u0000\u08cc\u08cd\u0001\u0000\u0000\u0000\u08cd\u00ad\u0001"+
		"\u0000\u0000\u0000\u08ce\u08cf\u0005\u0001\u0000\u0000\u08cf\u08d4\u0003"+
		"\u00b0X\u0000\u08d0\u08d1\u0005\u0002\u0000\u0000\u08d1\u08d3\u0003\u00b0"+
		"X\u0000\u08d2\u08d0\u0001\u0000\u0000\u0000\u08d3\u08d6\u0001\u0000\u0000"+
		"\u0000\u08d4\u08d2\u0001\u0000\u0000\u0000\u08d4\u08d5\u0001\u0000\u0000"+
		"\u0000\u08d5\u08d7\u0001\u0000\u0000\u0000\u08d6\u08d4\u0001\u0000\u0000"+
		"\u0000\u08d7\u08d8\u0005\u0003\u0000\u0000\u08d8\u00af\u0001\u0000\u0000"+
		"\u0000\u08d9\u08db\u0003\u0118\u008c\u0000\u08da\u08dc\u0003*\u0015\u0000"+
		"\u08db\u08da\u0001\u0000\u0000\u0000\u08db\u08dc\u0001\u0000\u0000\u0000"+
		"\u08dc\u00b1\u0001\u0000\u0000\u0000\u08dd\u08df\u0003\u00be_\u0000\u08de"+
		"\u08e0\u0003\u00a2Q\u0000\u08df\u08de\u0001\u0000\u0000\u0000\u08df\u08e0"+
		"\u0001\u0000\u0000\u0000\u08e0\u08e1\u0001\u0000\u0000\u0000\u08e1\u08e2"+
		"\u0003\u00b8\\\u0000\u08e2\u08f6\u0001\u0000\u0000\u0000\u08e3\u08e4\u0005"+
		"\u0001\u0000\u0000\u08e4\u08e5\u0003,\u0016\u0000\u08e5\u08e7\u0005\u0003"+
		"\u0000\u0000\u08e6\u08e8\u0003\u00a2Q\u0000\u08e7\u08e6\u0001\u0000\u0000"+
		"\u0000\u08e7\u08e8\u0001\u0000\u0000\u0000\u08e8\u08e9\u0001\u0000\u0000"+
		"\u0000\u08e9\u08ea\u0003\u00b8\\\u0000\u08ea\u08f6\u0001\u0000\u0000\u0000"+
		"\u08eb\u08ec\u0005\u0001\u0000\u0000\u08ec\u08ed\u0003\u009aM\u0000\u08ed"+
		"\u08ef\u0005\u0003\u0000\u0000\u08ee\u08f0\u0003\u00a2Q\u0000\u08ef\u08ee"+
		"\u0001\u0000\u0000\u0000\u08ef\u08f0\u0001\u0000\u0000\u0000\u08f0\u08f1"+
		"\u0001\u0000\u0000\u0000\u08f1\u08f2\u0003\u00b8\\\u0000\u08f2\u08f6\u0001"+
		"\u0000\u0000\u0000\u08f3\u08f6\u0003\u00b4Z\u0000\u08f4\u08f6\u0003\u00b6"+
		"[\u0000\u08f5\u08dd\u0001\u0000\u0000\u0000\u08f5\u08e3\u0001\u0000\u0000"+
		"\u0000\u08f5\u08eb\u0001\u0000\u0000\u0000\u08f5\u08f3\u0001\u0000\u0000"+
		"\u0000\u08f5\u08f4\u0001\u0000\u0000\u0000\u08f6\u00b3\u0001\u0000\u0000"+
		"\u0000\u08f7\u08f8\u0005\u0107\u0000\u0000\u08f8\u08fd\u0003\u00d0h\u0000"+
		"\u08f9\u08fa\u0005\u0002\u0000\u0000\u08fa\u08fc\u0003\u00d0h\u0000\u08fb"+
		"\u08f9\u0001\u0000\u0000\u0000\u08fc\u08ff\u0001\u0000\u0000\u0000\u08fd"+
		"\u08fb\u0001\u0000\u0000\u0000\u08fd\u08fe\u0001\u0000\u0000\u0000\u08fe"+
		"\u0900\u0001\u0000\u0000\u0000\u08ff\u08fd\u0001\u0000\u0000\u0000\u0900"+
		"\u0901\u0003\u00b8\\\u0000\u0901\u00b5\u0001\u0000\u0000\u0000\u0902\u0903"+
		"\u0003\u0110\u0088\u0000\u0903\u090c\u0005\u0001\u0000\u0000\u0904\u0909"+
		"\u0003\u00d0h\u0000\u0905\u0906\u0005\u0002\u0000\u0000\u0906\u0908\u0003"+
		"\u00d0h\u0000\u0907\u0905\u0001\u0000\u0000\u0000\u0908\u090b\u0001\u0000"+
		"\u0000\u0000\u0909\u0907\u0001\u0000\u0000\u0000\u0909\u090a\u0001\u0000"+
		"\u0000\u0000\u090a\u090d\u0001\u0000\u0000\u0000\u090b\u0909\u0001\u0000"+
		"\u0000\u0000\u090c\u0904\u0001\u0000\u0000\u0000\u090c\u090d\u0001\u0000"+
		"\u0000\u0000\u090d\u090e\u0001\u0000\u0000\u0000\u090e\u090f\u0005\u0003"+
		"\u0000\u0000\u090f\u0910\u0003\u00b8\\\u0000\u0910\u00b7\u0001\u0000\u0000"+
		"\u0000\u0911\u0913\u0005\u0018\u0000\u0000\u0912\u0911\u0001\u0000\u0000"+
		"\u0000\u0912\u0913\u0001\u0000\u0000\u0000\u0913\u0914\u0001\u0000\u0000"+
		"\u0000\u0914\u0916\u0003\u011a\u008d\u0000\u0915\u0917\u0003\u00a6S\u0000"+
		"\u0916\u0915\u0001\u0000\u0000\u0000\u0916\u0917\u0001\u0000\u0000\u0000"+
		"\u0917\u0919\u0001\u0000\u0000\u0000\u0918\u0912\u0001\u0000\u0000\u0000"+
		"\u0918\u0919\u0001\u0000\u0000\u0000\u0919\u00b9\u0001\u0000\u0000\u0000"+
		"\u091a\u091b\u0005\u00ce\u0000\u0000\u091b\u091c\u0005e\u0000\u0000\u091c"+
		"\u091d\u0005\u00d5\u0000\u0000\u091d\u0921\u0005\u0122\u0000\u0000\u091e"+
		"\u091f\u0005\u010d\u0000\u0000\u091f\u0920\u0005\u00d6\u0000\u0000\u0920"+
		"\u0922\u0003D\"\u0000\u0921\u091e\u0001\u0000\u0000\u0000\u0921\u0922"+
		"\u0001\u0000\u0000\u0000\u0922\u094c\u0001\u0000\u0000\u0000\u0923\u0924"+
		"\u0005\u00ce\u0000\u0000\u0924\u0925\u0005e\u0000\u0000\u0925\u092f\u0005"+
		"F\u0000\u0000\u0926\u0927\u0005^\u0000\u0000\u0927\u0928\u0005\u00ed\u0000"+
		"\u0000\u0928\u0929\u0005 \u0000\u0000\u0929\u092d\u0005\u0122\u0000\u0000"+
		"\u092a\u092b\u0005S\u0000\u0000\u092b\u092c\u0005 \u0000\u0000\u092c\u092e"+
		"\u0005\u0122\u0000\u0000\u092d\u092a\u0001\u0000\u0000\u0000\u092d\u092e"+
		"\u0001\u0000\u0000\u0000\u092e\u0930\u0001\u0000\u0000\u0000\u092f\u0926"+
		"\u0001\u0000\u0000\u0000\u092f\u0930\u0001\u0000\u0000\u0000\u0930\u0936"+
		"\u0001\u0000\u0000\u0000\u0931\u0932\u0005,\u0000\u0000\u0932\u0933\u0005"+
		"\u007f\u0000\u0000\u0933\u0934\u0005\u00ed\u0000\u0000\u0934\u0935\u0005"+
		" \u0000\u0000\u0935\u0937\u0005\u0122\u0000\u0000\u0936\u0931\u0001\u0000"+
		"\u0000\u0000\u0936\u0937\u0001\u0000\u0000\u0000\u0937\u093d\u0001\u0000"+
		"\u0000\u0000\u0938\u0939\u0005\u0092\u0000\u0000\u0939\u093a\u0005\u0081"+
		"\u0000\u0000\u093a\u093b\u0005\u00ed\u0000\u0000\u093b\u093c\u0005 \u0000"+
		"\u0000\u093c\u093e\u0005\u0122\u0000\u0000\u093d\u0938\u0001\u0000\u0000"+
		"\u0000\u093d\u093e\u0001\u0000\u0000\u0000\u093e\u0943\u0001\u0000\u0000"+
		"\u0000\u093f\u0940\u0005\u0089\u0000\u0000\u0940\u0941\u0005\u00ed\u0000"+
		"\u0000\u0941\u0942\u0005 \u0000\u0000\u0942\u0944\u0005\u0122\u0000\u0000"+
		"\u0943\u093f\u0001\u0000\u0000\u0000\u0943\u0944\u0001\u0000\u0000\u0000"+
		"\u0944\u0949\u0001\u0000\u0000\u0000\u0945\u0946\u0005\u009d\u0000\u0000"+
		"\u0946\u0947\u0005D\u0000\u0000\u0947\u0948\u0005\u0018\u0000\u0000\u0948"+
		"\u094a\u0005\u0122\u0000\u0000\u0949\u0945\u0001\u0000\u0000\u0000\u0949"+
		"\u094a\u0001\u0000\u0000\u0000\u094a\u094c\u0001\u0000\u0000\u0000\u094b"+
		"\u091a\u0001\u0000\u0000\u0000\u094b\u0923\u0001\u0000\u0000\u0000\u094c"+
		"\u00bb\u0001\u0000\u0000\u0000\u094d\u0952\u0003\u00be_\u0000\u094e\u094f"+
		"\u0005\u0002\u0000\u0000\u094f\u0951\u0003\u00be_\u0000\u0950\u094e\u0001"+
		"\u0000\u0000\u0000\u0951\u0954\u0001\u0000\u0000\u0000\u0952\u0950\u0001"+
		"\u0000\u0000\u0000\u0952\u0953\u0001\u0000\u0000\u0000\u0953\u00bd\u0001"+
		"\u0000\u0000\u0000\u0954\u0952\u0001\u0000\u0000\u0000\u0955\u095a\u0003"+
		"\u0114\u008a\u0000\u0956\u0957\u0005\u0005\u0000\u0000\u0957\u0959\u0003"+
		"\u0114\u008a\u0000\u0958\u0956\u0001\u0000\u0000\u0000\u0959\u095c\u0001"+
		"\u0000\u0000\u0000\u095a\u0958\u0001\u0000\u0000\u0000\u095a\u095b\u0001"+
		"\u0000\u0000\u0000\u095b\u00bf\u0001\u0000\u0000\u0000\u095c\u095a\u0001"+
		"\u0000\u0000\u0000\u095d\u095e\u0003\u0114\u008a\u0000\u095e\u095f\u0005"+
		"\u0005\u0000\u0000\u095f\u0961\u0001\u0000\u0000\u0000\u0960\u095d\u0001"+
		"\u0000\u0000\u0000\u0960\u0961\u0001\u0000\u0000\u0000\u0961\u0962\u0001"+
		"\u0000\u0000\u0000\u0962\u0963\u0003\u0114\u008a\u0000\u0963\u00c1\u0001"+
		"\u0000\u0000\u0000\u0964\u0965\u0003\u0114\u008a\u0000\u0965\u0966\u0005"+
		"\u0005\u0000\u0000\u0966\u0968\u0001\u0000\u0000\u0000\u0967\u0964\u0001"+
		"\u0000\u0000\u0000\u0967\u0968\u0001\u0000\u0000\u0000\u0968\u0969\u0001"+
		"\u0000\u0000\u0000\u0969\u096a\u0003\u0114\u008a\u0000\u096a\u00c3\u0001"+
		"\u0000\u0000\u0000\u096b\u0973\u0003\u00d0h\u0000\u096c\u096e\u0005\u0018"+
		"\u0000\u0000\u096d\u096c\u0001\u0000\u0000\u0000\u096d\u096e\u0001\u0000"+
		"\u0000\u0000\u096e\u0971\u0001\u0000\u0000\u0000\u096f\u0972\u0003\u0114"+
		"\u008a\u0000\u0970\u0972\u0003\u00a6S\u0000\u0971\u096f\u0001\u0000\u0000"+
		"\u0000\u0971\u0970\u0001\u0000\u0000\u0000\u0972\u0974\u0001\u0000\u0000"+
		"\u0000\u0973\u096d\u0001\u0000\u0000\u0000\u0973\u0974\u0001\u0000\u0000"+
		"\u0000\u0974\u00c5\u0001\u0000\u0000\u0000\u0975\u097a\u0003\u00c4b\u0000"+
		"\u0976\u0977\u0005\u0002\u0000\u0000\u0977\u0979\u0003\u00c4b\u0000\u0978"+
		"\u0976\u0001\u0000\u0000\u0000\u0979\u097c\u0001\u0000\u0000\u0000\u097a"+
		"\u0978\u0001\u0000\u0000\u0000\u097a\u097b\u0001\u0000\u0000\u0000\u097b"+
		"\u00c7\u0001\u0000\u0000\u0000\u097c\u097a\u0001\u0000\u0000\u0000\u097d"+
		"\u097e\u0005\u0001\u0000\u0000\u097e\u0983\u0003\u00cae\u0000\u097f\u0980"+
		"\u0005\u0002\u0000\u0000\u0980\u0982\u0003\u00cae\u0000\u0981\u097f\u0001"+
		"\u0000\u0000\u0000\u0982\u0985\u0001\u0000\u0000\u0000\u0983\u0981\u0001"+
		"\u0000\u0000\u0000\u0983\u0984\u0001\u0000\u0000\u0000\u0984\u0986\u0001"+
		"\u0000\u0000\u0000\u0985\u0983\u0001\u0000\u0000\u0000\u0986\u0987\u0005"+
		"\u0003\u0000\u0000\u0987\u00c9\u0001\u0000\u0000\u0000\u0988\u098b\u0003"+
		"\u00ccf\u0000\u0989\u098b\u0003\u00fc~\u0000\u098a\u0988\u0001\u0000\u0000"+
		"\u0000\u098a\u0989\u0001\u0000\u0000\u0000\u098b\u00cb\u0001\u0000\u0000"+
		"\u0000\u098c\u099a\u0003\u0112\u0089\u0000\u098d\u098e\u0003\u0118\u008c"+
		"\u0000\u098e\u098f\u0005\u0001\u0000\u0000\u098f\u0994\u0003\u00ceg\u0000"+
		"\u0990\u0991\u0005\u0002\u0000\u0000\u0991\u0993\u0003\u00ceg\u0000\u0992"+
		"\u0990\u0001\u0000\u0000\u0000\u0993\u0996\u0001\u0000\u0000\u0000\u0994"+
		"\u0992\u0001\u0000\u0000\u0000\u0994\u0995\u0001\u0000\u0000\u0000\u0995"+
		"\u0997\u0001\u0000\u0000\u0000\u0996\u0994\u0001\u0000\u0000\u0000\u0997"+
		"\u0998\u0005\u0003\u0000\u0000\u0998\u099a\u0001\u0000\u0000\u0000\u0999"+
		"\u098c\u0001\u0000\u0000\u0000\u0999\u098d\u0001\u0000\u0000\u0000\u099a"+
		"\u00cd\u0001\u0000\u0000\u0000\u099b\u099e\u0003\u0112\u0089\u0000\u099c"+
		"\u099e\u0003\u00dcn\u0000\u099d\u099b\u0001\u0000\u0000\u0000\u099d\u099c"+
		"\u0001\u0000\u0000\u0000\u099e\u00cf\u0001\u0000\u0000\u0000\u099f\u09a0"+
		"\u0003\u00d4j\u0000\u09a0\u00d1\u0001\u0000\u0000\u0000\u09a1\u09a6\u0003"+
		"\u00d0h\u0000\u09a2\u09a3\u0005\u0002\u0000\u0000\u09a3\u09a5\u0003\u00d0"+
		"h\u0000\u09a4\u09a2\u0001\u0000\u0000\u0000\u09a5\u09a8\u0001\u0000\u0000"+
		"\u0000\u09a6\u09a4\u0001\u0000\u0000\u0000\u09a6\u09a7\u0001\u0000\u0000"+
		"\u0000\u09a7\u00d3\u0001\u0000\u0000\u0000\u09a8\u09a6\u0001\u0000\u0000"+
		"\u0000\u09a9\u09aa\u0006j\uffff\uffff\u0000\u09aa\u09ab\u0005\u009c\u0000"+
		"\u0000\u09ab\u09b6\u0003\u00d4j\u0005\u09ac\u09ad\u0005V\u0000\u0000\u09ad"+
		"\u09ae\u0005\u0001\u0000\u0000\u09ae\u09af\u0003,\u0016\u0000\u09af\u09b0"+
		"\u0005\u0003\u0000\u0000\u09b0\u09b6\u0001\u0000\u0000\u0000\u09b1\u09b3"+
		"\u0003\u00d8l\u0000\u09b2\u09b4\u0003\u00d6k\u0000\u09b3\u09b2\u0001\u0000"+
		"\u0000\u0000\u09b3\u09b4\u0001\u0000\u0000\u0000\u09b4\u09b6\u0001\u0000"+
		"\u0000\u0000\u09b5\u09a9\u0001\u0000\u0000\u0000\u09b5\u09ac\u0001\u0000"+
		"\u0000\u0000\u09b5\u09b1\u0001\u0000\u0000\u0000\u09b6\u09bf\u0001\u0000"+
		"\u0000\u0000\u09b7\u09b8\n\u0002\u0000\u0000\u09b8\u09b9\u0005\u0013\u0000"+
		"\u0000\u09b9\u09be\u0003\u00d4j\u0003\u09ba\u09bb\n\u0001\u0000\u0000"+
		"\u09bb\u09bc\u0005\u00a4\u0000\u0000\u09bc\u09be\u0003\u00d4j\u0002\u09bd"+
		"\u09b7\u0001\u0000\u0000\u0000\u09bd\u09ba\u0001\u0000\u0000\u0000\u09be"+
		"\u09c1\u0001\u0000\u0000\u0000\u09bf\u09bd\u0001\u0000\u0000\u0000\u09bf"+
		"\u09c0\u0001\u0000\u0000\u0000\u09c0\u00d5\u0001\u0000\u0000\u0000\u09c1"+
		"\u09bf\u0001\u0000\u0000\u0000\u09c2\u09c4\u0005\u009c\u0000\u0000\u09c3"+
		"\u09c2\u0001\u0000\u0000\u0000\u09c3\u09c4\u0001\u0000\u0000\u0000\u09c4"+
		"\u09c5\u0001\u0000\u0000\u0000\u09c5\u09c6\u0005\u001c\u0000\u0000\u09c6"+
		"\u09c7\u0003\u00d8l\u0000\u09c7\u09c8\u0005\u0013\u0000\u0000\u09c8\u09c9"+
		"\u0003\u00d8l\u0000\u09c9\u0a15\u0001\u0000\u0000\u0000\u09ca\u09cc\u0005"+
		"\u009c\u0000\u0000\u09cb\u09ca\u0001\u0000\u0000\u0000\u09cb\u09cc\u0001"+
		"\u0000\u0000\u0000\u09cc\u09cd\u0001\u0000\u0000\u0000\u09cd\u09ce\u0005"+
		"t\u0000\u0000\u09ce\u09cf\u0005\u0001\u0000\u0000\u09cf\u09d4\u0003\u00d0"+
		"h\u0000\u09d0\u09d1\u0005\u0002\u0000\u0000\u09d1\u09d3\u0003\u00d0h\u0000"+
		"\u09d2\u09d0\u0001\u0000\u0000\u0000\u09d3\u09d6\u0001\u0000\u0000\u0000"+
		"\u09d4\u09d2\u0001\u0000\u0000\u0000\u09d4\u09d5\u0001\u0000\u0000\u0000"+
		"\u09d5\u09d7\u0001\u0000\u0000\u0000\u09d6\u09d4\u0001\u0000\u0000\u0000"+
		"\u09d7\u09d8\u0005\u0003\u0000\u0000\u09d8\u0a15\u0001\u0000\u0000\u0000"+
		"\u09d9\u09db\u0005\u009c\u0000\u0000\u09da\u09d9\u0001\u0000\u0000\u0000"+
		"\u09da\u09db\u0001\u0000\u0000\u0000\u09db\u09dc\u0001\u0000\u0000\u0000"+
		"\u09dc\u09dd\u0005t\u0000\u0000\u09dd\u09de\u0005\u0001\u0000\u0000\u09de"+
		"\u09df\u0003,\u0016\u0000\u09df\u09e0\u0005\u0003\u0000\u0000\u09e0\u0a15"+
		"\u0001\u0000\u0000\u0000\u09e1\u09e3\u0005\u009c\u0000\u0000\u09e2\u09e1"+
		"\u0001\u0000\u0000\u0000\u09e2\u09e3\u0001\u0000\u0000\u0000\u09e3\u09e4"+
		"\u0001\u0000\u0000\u0000\u09e4\u09e5\u0005\u00c9\u0000\u0000\u09e5\u0a15"+
		"\u0003\u00d8l\u0000\u09e6\u09e8\u0005\u009c\u0000\u0000\u09e7\u09e6\u0001"+
		"\u0000\u0000\u0000\u09e7\u09e8\u0001\u0000\u0000\u0000\u09e8\u09e9\u0001"+
		"\u0000\u0000\u0000\u09e9\u09ea\u0005\u0087\u0000\u0000\u09ea\u09f8\u0007"+
		"\u0016\u0000\u0000\u09eb\u09ec\u0005\u0001\u0000\u0000\u09ec\u09f9\u0005"+
		"\u0003\u0000\u0000\u09ed\u09ee\u0005\u0001\u0000\u0000\u09ee\u09f3\u0003"+
		"\u00d0h\u0000\u09ef\u09f0\u0005\u0002\u0000\u0000\u09f0\u09f2\u0003\u00d0"+
		"h\u0000\u09f1\u09ef\u0001\u0000\u0000\u0000\u09f2\u09f5\u0001\u0000\u0000"+
		"\u0000\u09f3\u09f1\u0001\u0000\u0000\u0000\u09f3\u09f4\u0001\u0000\u0000"+
		"\u0000\u09f4\u09f6\u0001\u0000\u0000\u0000\u09f5\u09f3\u0001\u0000\u0000"+
		"\u0000\u09f6\u09f7\u0005\u0003\u0000\u0000\u09f7\u09f9\u0001\u0000\u0000"+
		"\u0000\u09f8\u09eb\u0001\u0000\u0000\u0000\u09f8\u09ed\u0001\u0000\u0000"+
		"\u0000\u09f9\u0a15\u0001\u0000\u0000\u0000\u09fa\u09fc\u0005\u009c\u0000"+
		"\u0000\u09fb\u09fa\u0001\u0000\u0000\u0000\u09fb\u09fc\u0001\u0000\u0000"+
		"\u0000\u09fc\u09fd\u0001\u0000\u0000\u0000\u09fd\u09fe\u0005\u0087\u0000"+
		"\u0000\u09fe\u0a01\u0003\u00d8l\u0000\u09ff\u0a00\u0005R\u0000\u0000\u0a00"+
		"\u0a02\u0005\u0122\u0000\u0000\u0a01\u09ff\u0001\u0000\u0000\u0000\u0a01"+
		"\u0a02\u0001\u0000\u0000\u0000\u0a02\u0a15\u0001\u0000\u0000\u0000\u0a03"+
		"\u0a05\u0005~\u0000\u0000\u0a04\u0a06\u0005\u009c\u0000\u0000\u0a05\u0a04"+
		"\u0001\u0000\u0000\u0000\u0a05\u0a06\u0001\u0000\u0000\u0000\u0a06\u0a07"+
		"\u0001\u0000\u0000\u0000\u0a07\u0a15\u0005\u009d\u0000\u0000\u0a08\u0a0a"+
		"\u0005~\u0000\u0000\u0a09\u0a0b\u0005\u009c\u0000\u0000\u0a0a\u0a09\u0001"+
		"\u0000\u0000\u0000\u0a0a\u0a0b\u0001\u0000\u0000\u0000\u0a0b\u0a0c\u0001"+
		"\u0000\u0000\u0000\u0a0c\u0a15\u0007\u0017\u0000\u0000\u0a0d\u0a0f\u0005"+
		"~\u0000\u0000\u0a0e\u0a10\u0005\u009c\u0000\u0000\u0a0f\u0a0e\u0001\u0000"+
		"\u0000\u0000\u0a0f\u0a10\u0001\u0000\u0000\u0000\u0a10\u0a11\u0001\u0000"+
		"\u0000\u0000\u0a11\u0a12\u0005L\u0000\u0000\u0a12\u0a13\u0005g\u0000\u0000"+
		"\u0a13\u0a15\u0003\u00d8l\u0000\u0a14\u09c3\u0001\u0000\u0000\u0000\u0a14"+
		"\u09cb\u0001\u0000\u0000\u0000\u0a14\u09da\u0001\u0000\u0000\u0000\u0a14"+
		"\u09e2\u0001\u0000\u0000\u0000\u0a14\u09e7\u0001\u0000\u0000\u0000\u0a14"+
		"\u09fb\u0001\u0000\u0000\u0000\u0a14\u0a03\u0001\u0000\u0000\u0000\u0a14"+
		"\u0a08\u0001\u0000\u0000\u0000\u0a14\u0a0d\u0001\u0000\u0000\u0000\u0a15"+
		"\u00d7\u0001\u0000\u0000\u0000\u0a16\u0a17\u0006l\uffff\uffff\u0000\u0a17"+
		"\u0a1b\u0003\u00dam\u0000\u0a18\u0a19\u0007\u0018\u0000\u0000\u0a19\u0a1b"+
		"\u0003\u00d8l\u0007\u0a1a\u0a16\u0001\u0000\u0000\u0000\u0a1a\u0a18\u0001"+
		"\u0000\u0000\u0000\u0a1b\u0a31\u0001\u0000\u0000\u0000\u0a1c\u0a1d\n\u0006"+
		"\u0000\u0000\u0a1d\u0a1e\u0007\u0019\u0000\u0000\u0a1e\u0a30\u0003\u00d8"+
		"l\u0007\u0a1f\u0a20\n\u0005\u0000\u0000\u0a20\u0a21\u0007\u001a\u0000"+
		"\u0000\u0a21\u0a30\u0003\u00d8l\u0006\u0a22\u0a23\n\u0004\u0000\u0000"+
		"\u0a23\u0a24\u0005\u011e\u0000\u0000\u0a24\u0a30\u0003\u00d8l\u0005\u0a25"+
		"\u0a26\n\u0003\u0000\u0000\u0a26\u0a27\u0005\u0121\u0000\u0000\u0a27\u0a30"+
		"\u0003\u00d8l\u0004\u0a28\u0a29\n\u0002\u0000\u0000\u0a29\u0a2a\u0005"+
		"\u011f\u0000\u0000\u0a2a\u0a30\u0003\u00d8l\u0003\u0a2b\u0a2c\n\u0001"+
		"\u0000\u0000\u0a2c\u0a2d\u0003\u00deo\u0000\u0a2d\u0a2e\u0003\u00d8l\u0002"+
		"\u0a2e\u0a30\u0001\u0000\u0000\u0000\u0a2f\u0a1c\u0001\u0000\u0000\u0000"+
		"\u0a2f\u0a1f\u0001\u0000\u0000\u0000\u0a2f\u0a22\u0001\u0000\u0000\u0000"+
		"\u0a2f\u0a25\u0001\u0000\u0000\u0000\u0a2f\u0a28\u0001\u0000\u0000\u0000"+
		"\u0a2f\u0a2b\u0001\u0000\u0000\u0000\u0a30\u0a33\u0001\u0000\u0000\u0000"+
		"\u0a31\u0a2f\u0001\u0000\u0000\u0000\u0a31\u0a32\u0001\u0000\u0000\u0000"+
		"\u0a32\u00d9\u0001\u0000\u0000\u0000\u0a33\u0a31\u0001\u0000\u0000\u0000"+
		"\u0a34\u0a35\u0006m\uffff\uffff\u0000\u0a35\u0af1\u0007\u001b\u0000\u0000"+
		"\u0a36\u0a38\u0005#\u0000\u0000\u0a37\u0a39\u0003\u0102\u0081\u0000\u0a38"+
		"\u0a37\u0001\u0000\u0000\u0000\u0a39\u0a3a\u0001\u0000\u0000\u0000\u0a3a"+
		"\u0a38\u0001\u0000\u0000\u0000\u0a3a\u0a3b\u0001\u0000\u0000\u0000\u0a3b"+
		"\u0a3e\u0001\u0000\u0000\u0000\u0a3c\u0a3d\u0005P\u0000\u0000\u0a3d\u0a3f"+
		"\u0003\u00d0h\u0000\u0a3e\u0a3c\u0001\u0000\u0000\u0000\u0a3e\u0a3f\u0001"+
		"\u0000\u0000\u0000\u0a3f\u0a40\u0001\u0000\u0000\u0000\u0a40\u0a41\u0005"+
		"Q\u0000\u0000\u0a41\u0af1\u0001\u0000\u0000\u0000\u0a42\u0a43\u0005#\u0000"+
		"\u0000\u0a43\u0a45\u0003\u00d0h\u0000\u0a44\u0a46\u0003\u0102\u0081\u0000"+
		"\u0a45\u0a44\u0001\u0000\u0000\u0000\u0a46\u0a47\u0001\u0000\u0000\u0000"+
		"\u0a47\u0a45\u0001\u0000\u0000\u0000\u0a47\u0a48\u0001\u0000\u0000\u0000"+
		"\u0a48\u0a4b\u0001\u0000\u0000\u0000\u0a49\u0a4a\u0005P\u0000\u0000\u0a4a"+
		"\u0a4c\u0003\u00d0h\u0000\u0a4b\u0a49\u0001\u0000\u0000\u0000\u0a4b\u0a4c"+
		"\u0001\u0000\u0000\u0000\u0a4c\u0a4d\u0001\u0000\u0000\u0000\u0a4d\u0a4e"+
		"\u0005Q\u0000\u0000\u0a4e\u0af1\u0001\u0000\u0000\u0000\u0a4f\u0a50\u0007"+
		"\u001c\u0000\u0000\u0a50\u0a51\u0005\u0001\u0000\u0000\u0a51\u0a52\u0003"+
		"\u00d0h\u0000\u0a52\u0a53\u0005\u0018\u0000\u0000\u0a53\u0a54\u0003\u00f4"+
		"z\u0000\u0a54\u0a55\u0005\u0003\u0000\u0000\u0a55\u0af1\u0001\u0000\u0000"+
		"\u0000\u0a56\u0a57\u0005\u00e4\u0000\u0000\u0a57\u0a60\u0005\u0001\u0000"+
		"\u0000\u0a58\u0a5d\u0003\u00c4b\u0000\u0a59\u0a5a\u0005\u0002\u0000\u0000"+
		"\u0a5a\u0a5c\u0003\u00c4b\u0000\u0a5b\u0a59\u0001\u0000\u0000\u0000\u0a5c"+
		"\u0a5f\u0001\u0000\u0000\u0000\u0a5d\u0a5b\u0001\u0000\u0000\u0000\u0a5d"+
		"\u0a5e\u0001\u0000\u0000\u0000\u0a5e\u0a61\u0001\u0000\u0000\u0000\u0a5f"+
		"\u0a5d\u0001\u0000\u0000\u0000\u0a60\u0a58\u0001\u0000\u0000\u0000\u0a60"+
		"\u0a61\u0001\u0000\u0000\u0000\u0a61\u0a62\u0001\u0000\u0000\u0000\u0a62"+
		"\u0af1\u0005\u0003\u0000\u0000\u0a63\u0a64\u0005a\u0000\u0000\u0a64\u0a65"+
		"\u0005\u0001\u0000\u0000\u0a65\u0a68\u0003\u00d0h\u0000\u0a66\u0a67\u0005"+
		"r\u0000\u0000\u0a67\u0a69\u0005\u009e\u0000\u0000\u0a68\u0a66\u0001\u0000"+
		"\u0000\u0000\u0a68\u0a69\u0001\u0000\u0000\u0000\u0a69\u0a6a\u0001\u0000"+
		"\u0000\u0000\u0a6a\u0a6b\u0005\u0003\u0000\u0000\u0a6b\u0af1\u0001\u0000"+
		"\u0000\u0000\u0a6c\u0a6d\u0005\u0082\u0000\u0000\u0a6d\u0a6e\u0005\u0001"+
		"\u0000\u0000\u0a6e\u0a71\u0003\u00d0h\u0000\u0a6f\u0a70\u0005r\u0000\u0000"+
		"\u0a70\u0a72\u0005\u009e\u0000\u0000\u0a71\u0a6f\u0001\u0000\u0000\u0000"+
		"\u0a71\u0a72\u0001\u0000\u0000\u0000\u0a72\u0a73\u0001\u0000\u0000\u0000"+
		"\u0a73\u0a74\u0005\u0003\u0000\u0000\u0a74\u0af1\u0001\u0000\u0000\u0000"+
		"\u0a75\u0a76\u0005\u00b3\u0000\u0000\u0a76\u0a77\u0005\u0001\u0000\u0000"+
		"\u0a77\u0a78\u0003\u00d8l\u0000\u0a78\u0a79\u0005t\u0000\u0000\u0a79\u0a7a"+
		"\u0003\u00d8l\u0000\u0a7a\u0a7b\u0005\u0003\u0000\u0000\u0a7b\u0af1\u0001"+
		"\u0000\u0000\u0000\u0a7c\u0af1\u0003\u00dcn\u0000\u0a7d\u0af1\u0005\u011a"+
		"\u0000\u0000\u0a7e\u0a7f\u0003\u0112\u0089\u0000\u0a7f\u0a80\u0005\u0005"+
		"\u0000\u0000\u0a80\u0a81\u0005\u011a\u0000\u0000\u0a81\u0af1\u0001\u0000"+
		"\u0000\u0000\u0a82\u0a83\u0005\u0001\u0000\u0000\u0a83\u0a86\u0003\u00c4"+
		"b\u0000\u0a84\u0a85\u0005\u0002\u0000\u0000\u0a85\u0a87\u0003\u00c4b\u0000"+
		"\u0a86\u0a84\u0001\u0000\u0000\u0000\u0a87\u0a88\u0001\u0000\u0000\u0000"+
		"\u0a88\u0a86\u0001\u0000\u0000\u0000\u0a88\u0a89\u0001\u0000\u0000\u0000"+
		"\u0a89\u0a8a\u0001\u0000\u0000\u0000\u0a8a\u0a8b\u0005\u0003\u0000\u0000"+
		"\u0a8b\u0af1\u0001\u0000\u0000\u0000\u0a8c\u0a8d\u0005\u0001\u0000\u0000"+
		"\u0a8d\u0a8e\u0003,\u0016\u0000\u0a8e\u0a8f\u0005\u0003\u0000\u0000\u0a8f"+
		"\u0af1\u0001\u0000\u0000\u0000\u0a90\u0a91\u0003\u0110\u0088\u0000\u0a91"+
		"\u0a9d\u0005\u0001\u0000\u0000\u0a92\u0a94\u0003\u0098L\u0000\u0a93\u0a92"+
		"\u0001\u0000\u0000\u0000\u0a93\u0a94\u0001\u0000\u0000\u0000\u0a94\u0a95"+
		"\u0001\u0000\u0000\u0000\u0a95\u0a9a\u0003\u00d0h\u0000\u0a96\u0a97\u0005"+
		"\u0002\u0000\u0000\u0a97\u0a99\u0003\u00d0h\u0000\u0a98\u0a96\u0001\u0000"+
		"\u0000\u0000\u0a99\u0a9c\u0001\u0000\u0000\u0000\u0a9a\u0a98\u0001\u0000"+
		"\u0000\u0000\u0a9a\u0a9b\u0001\u0000\u0000\u0000\u0a9b\u0a9e\u0001\u0000"+
		"\u0000\u0000\u0a9c\u0a9a\u0001\u0000\u0000\u0000\u0a9d\u0a93\u0001\u0000"+
		"\u0000\u0000\u0a9d\u0a9e\u0001\u0000\u0000\u0000\u0a9e\u0a9f\u0001\u0000"+
		"\u0000\u0000\u0a9f\u0aa6\u0005\u0003\u0000\u0000\u0aa0\u0aa1\u0005_\u0000"+
		"\u0000\u0aa1\u0aa2\u0005\u0001\u0000\u0000\u0aa2\u0aa3\u0005\u010b\u0000"+
		"\u0000\u0aa3\u0aa4\u0003\u00d4j\u0000\u0aa4\u0aa5\u0005\u0003\u0000\u0000"+
		"\u0aa5\u0aa7\u0001\u0000\u0000\u0000\u0aa6\u0aa0\u0001\u0000\u0000\u0000"+
		"\u0aa6\u0aa7\u0001\u0000\u0000\u0000\u0aa7\u0aaa\u0001\u0000\u0000\u0000"+
		"\u0aa8\u0aa9\u0007\u001d\u0000\u0000\u0aa9\u0aab\u0005\u009e\u0000\u0000"+
		"\u0aaa\u0aa8\u0001\u0000\u0000\u0000\u0aaa\u0aab\u0001\u0000\u0000\u0000"+
		"\u0aab\u0aae\u0001\u0000\u0000\u0000\u0aac\u0aad\u0005\u00a9\u0000\u0000"+
		"\u0aad\u0aaf\u0003\u0108\u0084\u0000\u0aae\u0aac\u0001\u0000\u0000\u0000"+
		"\u0aae\u0aaf\u0001\u0000\u0000\u0000\u0aaf\u0af1\u0001\u0000\u0000\u0000"+
		"\u0ab0\u0ab1\u0003\u0118\u008c\u0000\u0ab1\u0ab2\u0005\b\u0000\u0000\u0ab2"+
		"\u0ab3\u0003\u00d0h\u0000\u0ab3\u0af1\u0001\u0000\u0000\u0000\u0ab4\u0ab5"+
		"\u0005\u0001\u0000\u0000\u0ab5\u0ab8\u0003\u0118\u008c\u0000\u0ab6\u0ab7"+
		"\u0005\u0002\u0000\u0000\u0ab7\u0ab9\u0003\u0118\u008c\u0000\u0ab8\u0ab6"+
		"\u0001\u0000\u0000\u0000\u0ab9\u0aba\u0001\u0000\u0000\u0000\u0aba\u0ab8"+
		"\u0001\u0000\u0000\u0000\u0aba\u0abb\u0001\u0000\u0000\u0000\u0abb\u0abc"+
		"\u0001\u0000\u0000\u0000\u0abc\u0abd\u0005\u0003\u0000\u0000\u0abd\u0abe"+
		"\u0005\b\u0000\u0000\u0abe\u0abf\u0003\u00d0h\u0000\u0abf\u0af1\u0001"+
		"\u0000\u0000\u0000\u0ac0\u0af1\u0003\u0118\u008c\u0000\u0ac1\u0ac2\u0005"+
		"\u0001\u0000\u0000\u0ac2\u0ac3\u0003\u00d0h\u0000\u0ac3\u0ac4\u0005\u0003"+
		"\u0000\u0000\u0ac4\u0af1\u0001\u0000\u0000\u0000\u0ac5\u0ac6\u0005[\u0000"+
		"\u0000\u0ac6\u0ac7\u0005\u0001\u0000\u0000\u0ac7\u0ac8\u0003\u0118\u008c"+
		"\u0000\u0ac8\u0ac9\u0005g\u0000\u0000\u0ac9\u0aca\u0003\u00d8l\u0000\u0aca"+
		"\u0acb\u0005\u0003\u0000\u0000\u0acb\u0af1\u0001\u0000\u0000\u0000\u0acc"+
		"\u0acd\u0007\u001e\u0000\u0000\u0acd\u0ace\u0005\u0001\u0000\u0000\u0ace"+
		"\u0acf\u0003\u00d8l\u0000\u0acf\u0ad0\u0007\u001f\u0000\u0000\u0ad0\u0ad3"+
		"\u0003\u00d8l\u0000\u0ad1\u0ad2\u0007 \u0000\u0000\u0ad2\u0ad4\u0003\u00d8"+
		"l\u0000\u0ad3\u0ad1\u0001\u0000\u0000\u0000\u0ad3\u0ad4\u0001\u0000\u0000"+
		"\u0000\u0ad4\u0ad5\u0001\u0000\u0000\u0000\u0ad5\u0ad6\u0005\u0003\u0000"+
		"\u0000\u0ad6\u0af1\u0001\u0000\u0000\u0000\u0ad7\u0ad8\u0005\u00f6\u0000"+
		"\u0000\u0ad8\u0ada\u0005\u0001\u0000\u0000\u0ad9\u0adb\u0007!\u0000\u0000"+
		"\u0ada\u0ad9\u0001\u0000\u0000\u0000\u0ada\u0adb\u0001\u0000\u0000\u0000"+
		"\u0adb\u0add\u0001\u0000\u0000\u0000\u0adc\u0ade\u0003\u00d8l\u0000\u0add"+
		"\u0adc\u0001\u0000\u0000\u0000\u0add\u0ade\u0001\u0000\u0000\u0000\u0ade"+
		"\u0adf\u0001\u0000\u0000\u0000\u0adf\u0ae0\u0005g\u0000\u0000\u0ae0\u0ae1"+
		"\u0003\u00d8l\u0000\u0ae1\u0ae2\u0005\u0003\u0000\u0000\u0ae2\u0af1\u0001"+
		"\u0000\u0000\u0000\u0ae3\u0ae4\u0005\u00ab\u0000\u0000\u0ae4\u0ae5\u0005"+
		"\u0001\u0000\u0000\u0ae5\u0ae6\u0003\u00d8l\u0000\u0ae6\u0ae7\u0005\u00b2"+
		"\u0000\u0000\u0ae7\u0ae8\u0003\u00d8l\u0000\u0ae8\u0ae9\u0005g\u0000\u0000"+
		"\u0ae9\u0aec\u0003\u00d8l\u0000\u0aea\u0aeb\u0005c\u0000\u0000\u0aeb\u0aed"+
		"\u0003\u00d8l\u0000\u0aec\u0aea\u0001\u0000\u0000\u0000\u0aec\u0aed\u0001"+
		"\u0000\u0000\u0000\u0aed\u0aee\u0001\u0000\u0000\u0000\u0aee\u0aef\u0005"+
		"\u0003\u0000\u0000\u0aef\u0af1\u0001\u0000\u0000\u0000\u0af0\u0a34\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a36\u0001\u0000\u0000\u0000\u0af0\u0a42\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a4f\u0001\u0000\u0000\u0000\u0af0\u0a56\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a63\u0001\u0000\u0000\u0000\u0af0\u0a6c\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a75\u0001\u0000\u0000\u0000\u0af0\u0a7c\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a7d\u0001\u0000\u0000\u0000\u0af0\u0a7e\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a82\u0001\u0000\u0000\u0000\u0af0\u0a8c\u0001"+
		"\u0000\u0000\u0000\u0af0\u0a90\u0001\u0000\u0000\u0000\u0af0\u0ab0\u0001"+
		"\u0000\u0000\u0000\u0af0\u0ab4\u0001\u0000\u0000\u0000\u0af0\u0ac0\u0001"+
		"\u0000\u0000\u0000\u0af0\u0ac1\u0001\u0000\u0000\u0000\u0af0\u0ac5\u0001"+
		"\u0000\u0000\u0000\u0af0\u0acc\u0001\u0000\u0000\u0000\u0af0\u0ad7\u0001"+
		"\u0000\u0000\u0000\u0af0\u0ae3\u0001\u0000\u0000\u0000\u0af1\u0afc\u0001"+
		"\u0000\u0000\u0000\u0af2\u0af3\n\b\u0000\u0000\u0af3\u0af4\u0005\t\u0000"+
		"\u0000\u0af4\u0af5\u0003\u00d8l\u0000\u0af5\u0af6\u0005\n\u0000\u0000"+
		"\u0af6\u0afb\u0001\u0000\u0000\u0000\u0af7\u0af8\n\u0006\u0000\u0000\u0af8"+
		"\u0af9\u0005\u0005\u0000\u0000\u0af9\u0afb\u0003\u0118\u008c\u0000\u0afa"+
		"\u0af2\u0001\u0000\u0000\u0000\u0afa\u0af7\u0001\u0000\u0000\u0000\u0afb"+
		"\u0afe\u0001\u0000\u0000\u0000\u0afc\u0afa\u0001\u0000\u0000\u0000\u0afc"+
		"\u0afd\u0001\u0000\u0000\u0000\u0afd\u00db\u0001\u0000\u0000\u0000\u0afe"+
		"\u0afc\u0001\u0000\u0000\u0000\u0aff\u0b0c\u0005\u009d\u0000\u0000\u0b00"+
		"\u0b0c\u0003\u00e6s\u0000\u0b01\u0b02\u0003\u0118\u008c\u0000\u0b02\u0b03"+
		"\u0005\u0122\u0000\u0000\u0b03\u0b0c\u0001\u0000\u0000\u0000\u0b04\u0b0c"+
		"\u0003\u011e\u008f\u0000\u0b05\u0b0c\u0003\u00e4r\u0000\u0b06\u0b08\u0005"+
		"\u0122\u0000\u0000\u0b07\u0b06\u0001\u0000\u0000\u0000\u0b08\u0b09\u0001"+
		"\u0000\u0000\u0000\u0b09\u0b07\u0001\u0000\u0000\u0000\u0b09\u0b0a\u0001"+
		"\u0000\u0000\u0000\u0b0a\u0b0c\u0001\u0000\u0000\u0000\u0b0b\u0aff\u0001"+
		"\u0000\u0000\u0000\u0b0b\u0b00\u0001\u0000\u0000\u0000\u0b0b\u0b01\u0001"+
		"\u0000\u0000\u0000\u0b0b\u0b04\u0001\u0000\u0000\u0000\u0b0b\u0b05\u0001"+
		"\u0000\u0000\u0000\u0b0b\u0b07\u0001\u0000\u0000\u0000\u0b0c\u00dd\u0001"+
		"\u0000\u0000\u0000\u0b0d\u0b0e\u0007\"\u0000\u0000\u0b0e\u00df\u0001\u0000"+
		"\u0000\u0000\u0b0f\u0b10\u0007#\u0000\u0000\u0b10\u00e1\u0001\u0000\u0000"+
		"\u0000\u0b11\u0b12\u0007$\u0000\u0000\u0b12\u00e3\u0001\u0000\u0000\u0000"+
		"\u0b13\u0b14\u0007%\u0000\u0000\u0b14\u00e5\u0001\u0000\u0000\u0000\u0b15"+
		"\u0b18\u0005|\u0000\u0000\u0b16\u0b19\u0003\u00e8t\u0000\u0b17\u0b19\u0003"+
		"\u00ecv\u0000\u0b18\u0b16\u0001\u0000\u0000\u0000\u0b18\u0b17\u0001\u0000"+
		"\u0000\u0000\u0b18\u0b19\u0001\u0000\u0000\u0000\u0b19\u00e7\u0001\u0000"+
		"\u0000\u0000\u0b1a\u0b1c\u0003\u00eau\u0000\u0b1b\u0b1d\u0003\u00eew\u0000"+
		"\u0b1c\u0b1b\u0001\u0000\u0000\u0000\u0b1c\u0b1d\u0001\u0000\u0000\u0000"+
		"\u0b1d\u00e9\u0001\u0000\u0000\u0000\u0b1e\u0b1f\u0003\u00f0x\u0000\u0b1f"+
		"\u0b20\u0003\u0118\u008c\u0000\u0b20\u0b22\u0001\u0000\u0000\u0000\u0b21"+
		"\u0b1e\u0001\u0000\u0000\u0000\u0b22\u0b23\u0001\u0000\u0000\u0000\u0b23"+
		"\u0b21\u0001\u0000\u0000\u0000\u0b23\u0b24\u0001\u0000\u0000\u0000\u0b24"+
		"\u00eb\u0001\u0000\u0000\u0000\u0b25\u0b28\u0003\u00eew\u0000\u0b26\u0b29"+
		"\u0003\u00eau\u0000\u0b27\u0b29\u0003\u00eew\u0000\u0b28\u0b26\u0001\u0000"+
		"\u0000\u0000\u0b28\u0b27\u0001\u0000\u0000\u0000\u0b28\u0b29\u0001\u0000"+
		"\u0000\u0000\u0b29\u00ed\u0001\u0000\u0000\u0000\u0b2a\u0b2b\u0003\u00f0"+
		"x\u0000\u0b2b\u0b2c\u0003\u0118\u008c\u0000\u0b2c\u0b2d\u0005\u00f0\u0000"+
		"\u0000\u0b2d\u0b2e\u0003\u0118\u008c\u0000\u0b2e\u00ef\u0001\u0000\u0000"+
		"\u0000\u0b2f\u0b31\u0007&\u0000\u0000\u0b30\u0b2f\u0001\u0000\u0000\u0000"+
		"\u0b30\u0b31\u0001\u0000\u0000\u0000\u0b31\u0b32\u0001\u0000\u0000\u0000"+
		"\u0b32\u0b33\u0007\'\u0000\u0000\u0b33\u00f1\u0001\u0000\u0000\u0000\u0b34"+
		"\u0b38\u0005a\u0000\u0000\u0b35\u0b36\u0005\u000f\u0000\u0000\u0b36\u0b38"+
		"\u0003\u0114\u008a\u0000\u0b37\u0b34\u0001\u0000\u0000\u0000\u0b37\u0b35"+
		"\u0001\u0000\u0000\u0000\u0b38\u00f3\u0001\u0000\u0000\u0000\u0b39\u0b3a"+
		"\u0005\u0017\u0000\u0000\u0b3a\u0b3b\u0005\u0114\u0000\u0000\u0b3b\u0b3c"+
		"\u0003\u00f4z\u0000\u0b3c\u0b3d\u0005\u0116\u0000\u0000\u0b3d\u0b68\u0001"+
		"\u0000\u0000\u0000\u0b3e\u0b3f\u0005\u0092\u0000\u0000\u0b3f\u0b40\u0005"+
		"\u0114\u0000\u0000\u0b40\u0b41\u0003\u00f4z\u0000\u0b41\u0b42\u0005\u0002"+
		"\u0000\u0000\u0b42\u0b43\u0003\u00f4z\u0000\u0b43\u0b44\u0005\u0116\u0000"+
		"\u0000\u0b44\u0b68\u0001\u0000\u0000\u0000\u0b45\u0b4c\u0005\u00e4\u0000"+
		"\u0000\u0b46\u0b48\u0005\u0114\u0000\u0000\u0b47\u0b49\u0003\u00fe\u007f"+
		"\u0000\u0b48\u0b47\u0001\u0000\u0000\u0000\u0b48\u0b49\u0001\u0000\u0000"+
		"\u0000\u0b49\u0b4a\u0001\u0000\u0000\u0000\u0b4a\u0b4d\u0005\u0116\u0000"+
		"\u0000\u0b4b\u0b4d\u0005\u0112\u0000\u0000\u0b4c\u0b46\u0001\u0000\u0000"+
		"\u0000\u0b4c\u0b4b\u0001\u0000\u0000\u0000\u0b4d\u0b68\u0001\u0000\u0000"+
		"\u0000\u0b4e\u0b4f\u0005|\u0000\u0000\u0b4f\u0b52\u0007(\u0000\u0000\u0b50"+
		"\u0b51\u0005\u00f0\u0000\u0000\u0b51\u0b53\u0005\u0096\u0000\u0000\u0b52"+
		"\u0b50\u0001\u0000\u0000\u0000\u0b52\u0b53\u0001\u0000\u0000\u0000\u0b53"+
		"\u0b68\u0001\u0000\u0000\u0000\u0b54\u0b55\u0005|\u0000\u0000\u0b55\u0b58"+
		"\u0007)\u0000\u0000\u0b56\u0b57\u0005\u00f0\u0000\u0000\u0b57\u0b59\u0007"+
		"*\u0000\u0000\u0b58\u0b56\u0001\u0000\u0000\u0000\u0b58\u0b59\u0001\u0000"+
		"\u0000\u0000\u0b59\u0b68\u0001\u0000\u0000\u0000\u0b5a\u0b65\u0003\u0118"+
		"\u008c\u0000\u0b5b\u0b5c\u0005\u0001\u0000\u0000\u0b5c\u0b61\u0005\u0126"+
		"\u0000\u0000\u0b5d\u0b5e\u0005\u0002\u0000\u0000\u0b5e\u0b60\u0005\u0126"+
		"\u0000\u0000\u0b5f\u0b5d\u0001\u0000\u0000\u0000\u0b60\u0b63\u0001\u0000"+
		"\u0000\u0000\u0b61\u0b5f\u0001\u0000\u0000\u0000\u0b61\u0b62\u0001\u0000"+
		"\u0000\u0000\u0b62\u0b64\u0001\u0000\u0000\u0000\u0b63\u0b61\u0001\u0000"+
		"\u0000\u0000\u0b64\u0b66\u0005\u0003\u0000\u0000\u0b65\u0b5b\u0001\u0000"+
		"\u0000\u0000\u0b65\u0b66\u0001\u0000\u0000\u0000\u0b66\u0b68\u0001\u0000"+
		"\u0000\u0000\u0b67\u0b39\u0001\u0000\u0000\u0000\u0b67\u0b3e\u0001\u0000"+
		"\u0000\u0000\u0b67\u0b45\u0001\u0000\u0000\u0000\u0b67\u0b4e\u0001\u0000"+
		"\u0000\u0000\u0b67\u0b54\u0001\u0000\u0000\u0000\u0b67\u0b5a\u0001\u0000"+
		"\u0000\u0000\u0b68\u00f5\u0001\u0000\u0000\u0000\u0b69\u0b6e\u0003\u00f8"+
		"|\u0000\u0b6a\u0b6b\u0005\u0002\u0000\u0000\u0b6b\u0b6d\u0003\u00f8|\u0000"+
		"\u0b6c\u0b6a\u0001\u0000\u0000\u0000\u0b6d\u0b70\u0001\u0000\u0000\u0000"+
		"\u0b6e\u0b6c\u0001\u0000\u0000\u0000\u0b6e\u0b6f\u0001\u0000\u0000\u0000"+
		"\u0b6f\u00f7\u0001\u0000\u0000\u0000\u0b70\u0b6e\u0001\u0000\u0000\u0000"+
		"\u0b71\u0b72\u0003\u00be_\u0000\u0b72\u0b75\u0003\u00f4z\u0000\u0b73\u0b74"+
		"\u0005\u009c\u0000\u0000\u0b74\u0b76\u0005\u009d\u0000\u0000\u0b75\u0b73"+
		"\u0001\u0000\u0000\u0000\u0b75\u0b76\u0001\u0000\u0000\u0000\u0b76\u0b78"+
		"\u0001\u0000\u0000\u0000\u0b77\u0b79\u0003*\u0015\u0000\u0b78\u0b77\u0001"+
		"\u0000\u0000\u0000\u0b78\u0b79\u0001\u0000\u0000\u0000\u0b79\u0b7b\u0001"+
		"\u0000\u0000\u0000\u0b7a\u0b7c\u0003\u00f2y\u0000\u0b7b\u0b7a\u0001\u0000"+
		"\u0000\u0000\u0b7b\u0b7c\u0001\u0000\u0000\u0000\u0b7c\u00f9\u0001\u0000"+
		"\u0000\u0000\u0b7d\u0b82\u0003\u00fc~\u0000\u0b7e\u0b7f\u0005\u0002\u0000"+
		"\u0000\u0b7f\u0b81\u0003\u00fc~\u0000\u0b80\u0b7e\u0001\u0000\u0000\u0000"+
		"\u0b81\u0b84\u0001\u0000\u0000\u0000\u0b82\u0b80\u0001\u0000\u0000\u0000"+
		"\u0b82\u0b83\u0001\u0000\u0000\u0000\u0b83\u00fb\u0001\u0000\u0000\u0000"+
		"\u0b84\u0b82\u0001\u0000\u0000\u0000\u0b85\u0b86\u0003\u0114\u008a\u0000"+
		"\u0b86\u0b89\u0003\u00f4z\u0000\u0b87\u0b88\u0005\u009c\u0000\u0000\u0b88"+
		"\u0b8a\u0005\u009d\u0000\u0000\u0b89\u0b87\u0001\u0000\u0000\u0000\u0b89"+
		"\u0b8a\u0001\u0000\u0000\u0000\u0b8a\u0b8c\u0001\u0000\u0000\u0000\u0b8b"+
		"\u0b8d\u0003*\u0015\u0000\u0b8c\u0b8b\u0001\u0000\u0000\u0000\u0b8c\u0b8d"+
		"\u0001\u0000\u0000\u0000\u0b8d\u00fd\u0001\u0000\u0000\u0000\u0b8e\u0b93"+
		"\u0003\u0100\u0080\u0000\u0b8f\u0b90\u0005\u0002\u0000\u0000\u0b90\u0b92"+
		"\u0003\u0100\u0080\u0000\u0b91\u0b8f\u0001\u0000\u0000\u0000\u0b92\u0b95"+
		"\u0001\u0000\u0000\u0000\u0b93\u0b91\u0001\u0000\u0000\u0000\u0b93\u0b94"+
		"\u0001\u0000\u0000\u0000\u0b94\u00ff\u0001\u0000\u0000\u0000\u0b95\u0b93"+
		"\u0001\u0000\u0000\u0000\u0b96\u0b98\u0003\u0118\u008c\u0000\u0b97\u0b99"+
		"\u0005\u000b\u0000\u0000\u0b98\u0b97\u0001\u0000\u0000\u0000\u0b98\u0b99"+
		"\u0001\u0000\u0000\u0000\u0b99\u0b9a\u0001\u0000\u0000\u0000\u0b9a\u0b9d"+
		"\u0003\u00f4z\u0000\u0b9b\u0b9c\u0005\u009c\u0000\u0000\u0b9c\u0b9e\u0005"+
		"\u009d\u0000\u0000\u0b9d\u0b9b\u0001\u0000\u0000\u0000\u0b9d\u0b9e\u0001"+
		"\u0000\u0000\u0000\u0b9e\u0ba0\u0001\u0000\u0000\u0000\u0b9f\u0ba1\u0003"+
		"*\u0015\u0000\u0ba0\u0b9f\u0001\u0000\u0000\u0000\u0ba0\u0ba1\u0001\u0000"+
		"\u0000\u0000\u0ba1\u0101\u0001\u0000\u0000\u0000\u0ba2\u0ba3\u0005\u010a"+
		"\u0000\u0000\u0ba3\u0ba4\u0003\u00d0h\u0000\u0ba4\u0ba5\u0005\u00ee\u0000"+
		"\u0000\u0ba5\u0ba6\u0003\u00d0h\u0000\u0ba6\u0103\u0001\u0000\u0000\u0000"+
		"\u0ba7\u0ba8\u0005\u010c\u0000\u0000\u0ba8\u0bad\u0003\u0106\u0083\u0000"+
		"\u0ba9\u0baa\u0005\u0002\u0000\u0000\u0baa\u0bac\u0003\u0106\u0083\u0000"+
		"\u0bab\u0ba9\u0001\u0000\u0000\u0000\u0bac\u0baf\u0001\u0000\u0000\u0000"+
		"\u0bad\u0bab\u0001\u0000\u0000\u0000\u0bad\u0bae\u0001\u0000\u0000\u0000"+
		"\u0bae\u0105\u0001\u0000\u0000\u0000\u0baf\u0bad\u0001\u0000\u0000\u0000"+
		"\u0bb0\u0bb1\u0003\u0114\u008a\u0000\u0bb1\u0bb2\u0005\u0018\u0000\u0000"+
		"\u0bb2\u0bb3\u0003\u0108\u0084\u0000\u0bb3\u0107\u0001\u0000\u0000\u0000"+
		"\u0bb4\u0be3\u0003\u0114\u008a\u0000\u0bb5\u0bb6\u0005\u0001\u0000\u0000"+
		"\u0bb6\u0bb7\u0003\u0114\u008a\u0000\u0bb7\u0bb8\u0005\u0003\u0000\u0000"+
		"\u0bb8\u0be3\u0001\u0000\u0000\u0000\u0bb9\u0bdc\u0005\u0001\u0000\u0000"+
		"\u0bba\u0bbb\u0005(\u0000\u0000\u0bbb\u0bbc\u0005 \u0000\u0000\u0bbc\u0bc1"+
		"\u0003\u00d0h\u0000\u0bbd\u0bbe\u0005\u0002\u0000\u0000\u0bbe\u0bc0\u0003"+
		"\u00d0h\u0000\u0bbf\u0bbd\u0001\u0000\u0000\u0000\u0bc0\u0bc3\u0001\u0000"+
		"\u0000\u0000\u0bc1\u0bbf\u0001\u0000\u0000\u0000\u0bc1\u0bc2\u0001\u0000"+
		"\u0000\u0000\u0bc2\u0bdd\u0001\u0000\u0000\u0000\u0bc3\u0bc1\u0001\u0000"+
		"\u0000\u0000\u0bc4\u0bc5\u0007+\u0000\u0000\u0bc5\u0bc6\u0005 \u0000\u0000"+
		"\u0bc6\u0bcb\u0003\u00d0h\u0000\u0bc7\u0bc8\u0005\u0002\u0000\u0000\u0bc8"+
		"\u0bca\u0003\u00d0h\u0000\u0bc9\u0bc7\u0001\u0000\u0000\u0000\u0bca\u0bcd"+
		"\u0001\u0000\u0000\u0000\u0bcb\u0bc9\u0001\u0000\u0000\u0000\u0bcb\u0bcc"+
		"\u0001\u0000\u0000\u0000\u0bcc\u0bcf\u0001\u0000\u0000\u0000\u0bcd\u0bcb"+
		"\u0001\u0000\u0000\u0000\u0bce\u0bc4\u0001\u0000\u0000\u0000\u0bce\u0bcf"+
		"\u0001\u0000\u0000\u0000\u0bcf\u0bda\u0001\u0000\u0000\u0000\u0bd0\u0bd1"+
		"\u0007,\u0000\u0000\u0bd1\u0bd2\u0005 \u0000\u0000\u0bd2\u0bd7\u0003b"+
		"1\u0000\u0bd3\u0bd4\u0005\u0002\u0000\u0000\u0bd4\u0bd6\u0003b1\u0000"+
		"\u0bd5\u0bd3\u0001\u0000\u0000\u0000\u0bd6\u0bd9\u0001\u0000\u0000\u0000"+
		"\u0bd7\u0bd5\u0001\u0000\u0000\u0000\u0bd7\u0bd8\u0001\u0000\u0000\u0000"+
		"\u0bd8\u0bdb\u0001\u0000\u0000\u0000\u0bd9\u0bd7\u0001\u0000\u0000\u0000"+
		"\u0bda\u0bd0\u0001\u0000\u0000\u0000\u0bda\u0bdb\u0001\u0000\u0000\u0000"+
		"\u0bdb\u0bdd\u0001\u0000\u0000\u0000\u0bdc\u0bba\u0001\u0000\u0000\u0000"+
		"\u0bdc\u0bce\u0001\u0000\u0000\u0000\u0bdd\u0bdf\u0001\u0000\u0000\u0000"+
		"\u0bde\u0be0\u0003\u010a\u0085\u0000\u0bdf\u0bde\u0001\u0000\u0000\u0000"+
		"\u0bdf\u0be0\u0001\u0000\u0000\u0000\u0be0\u0be1\u0001\u0000\u0000\u0000"+
		"\u0be1\u0be3\u0005\u0003\u0000\u0000\u0be2\u0bb4\u0001\u0000\u0000\u0000"+
		"\u0be2\u0bb5\u0001\u0000\u0000\u0000\u0be2\u0bb9\u0001\u0000\u0000\u0000"+
		"\u0be3\u0109\u0001\u0000\u0000\u0000\u0be4\u0be5\u0005\u00ba\u0000\u0000"+
		"\u0be5\u0bf5\u0003\u010c\u0086\u0000\u0be6\u0be7\u0005\u00cf\u0000\u0000"+
		"\u0be7\u0bf5\u0003\u010c\u0086\u0000\u0be8\u0be9\u0005\u00ba\u0000\u0000"+
		"\u0be9\u0bea\u0005\u001c\u0000\u0000\u0bea\u0beb\u0003\u010c\u0086\u0000"+
		"\u0beb\u0bec\u0005\u0013\u0000\u0000\u0bec\u0bed\u0003\u010c\u0086\u0000"+
		"\u0bed\u0bf5\u0001\u0000\u0000\u0000\u0bee\u0bef\u0005\u00cf\u0000\u0000"+
		"\u0bef\u0bf0\u0005\u001c\u0000\u0000\u0bf0\u0bf1\u0003\u010c\u0086\u0000"+
		"\u0bf1\u0bf2\u0005\u0013\u0000\u0000\u0bf2\u0bf3\u0003\u010c\u0086\u0000"+
		"\u0bf3\u0bf5\u0001\u0000\u0000\u0000\u0bf4\u0be4\u0001\u0000\u0000\u0000"+
		"\u0bf4\u0be6\u0001\u0000\u0000\u0000\u0bf4\u0be8\u0001\u0000\u0000\u0000"+
		"\u0bf4\u0bee\u0001\u0000\u0000\u0000\u0bf5\u010b\u0001\u0000\u0000\u0000"+
		"\u0bf6\u0bf7\u0005\u00fc\u0000\u0000\u0bf7\u0bfe\u0007-\u0000\u0000\u0bf8"+
		"\u0bf9\u0005:\u0000\u0000\u0bf9\u0bfe\u0005\u00ce\u0000\u0000\u0bfa\u0bfb"+
		"\u0003\u00d0h\u0000\u0bfb\u0bfc\u0007-\u0000\u0000\u0bfc\u0bfe\u0001\u0000"+
		"\u0000\u0000\u0bfd\u0bf6\u0001\u0000\u0000\u0000\u0bfd\u0bf8\u0001\u0000"+
		"\u0000\u0000\u0bfd\u0bfa\u0001\u0000\u0000\u0000\u0bfe\u010d\u0001\u0000"+
		"\u0000\u0000\u0bff\u0c04\u0003\u0112\u0089\u0000\u0c00\u0c01\u0005\u0002"+
		"\u0000\u0000\u0c01\u0c03\u0003\u0112\u0089\u0000\u0c02\u0c00\u0001\u0000"+
		"\u0000\u0000\u0c03\u0c06\u0001\u0000\u0000\u0000\u0c04\u0c02\u0001\u0000"+
		"\u0000\u0000\u0c04\u0c05\u0001\u0000\u0000\u0000\u0c05\u010f\u0001\u0000"+
		"\u0000\u0000\u0c06\u0c04\u0001\u0000\u0000\u0000\u0c07\u0c0c\u0003\u0112"+
		"\u0089\u0000\u0c08\u0c0c\u0005_\u0000\u0000\u0c09\u0c0c\u0005\u0086\u0000"+
		"\u0000\u0c0a\u0c0c\u0005\u00c8\u0000\u0000\u0c0b\u0c07\u0001\u0000\u0000"+
		"\u0000\u0c0b\u0c08\u0001\u0000\u0000\u0000\u0c0b\u0c09\u0001\u0000\u0000"+
		"\u0000\u0c0b\u0c0a\u0001\u0000\u0000\u0000\u0c0c\u0111\u0001\u0000\u0000"+
		"\u0000\u0c0d\u0c12\u0003\u0118\u008c\u0000\u0c0e\u0c0f\u0005\u0005\u0000"+
		"\u0000\u0c0f\u0c11\u0003\u0118\u008c\u0000\u0c10\u0c0e\u0001\u0000\u0000"+
		"\u0000\u0c11\u0c14\u0001\u0000\u0000\u0000\u0c12\u0c10\u0001\u0000\u0000"+
		"\u0000\u0c12\u0c13\u0001\u0000\u0000\u0000\u0c13\u0113\u0001\u0000\u0000"+
		"\u0000\u0c14\u0c12\u0001\u0000\u0000\u0000\u0c15\u0c16\u0003\u0118\u008c"+
		"\u0000\u0c16\u0c17\u0003\u0116\u008b\u0000\u0c17\u0115\u0001\u0000\u0000"+
		"\u0000\u0c18\u0c19\u0005\u0119\u0000\u0000\u0c19\u0c1b\u0003\u0118\u008c"+
		"\u0000\u0c1a\u0c18\u0001\u0000\u0000\u0000\u0c1b\u0c1c\u0001\u0000\u0000"+
		"\u0000\u0c1c\u0c1a\u0001\u0000\u0000\u0000\u0c1c\u0c1d\u0001\u0000\u0000"+
		"\u0000\u0c1d\u0c20\u0001\u0000\u0000\u0000\u0c1e\u0c20\u0001\u0000\u0000"+
		"\u0000\u0c1f\u0c1a\u0001\u0000\u0000\u0000\u0c1f\u0c1e\u0001\u0000\u0000"+
		"\u0000\u0c20\u0117\u0001\u0000\u0000\u0000\u0c21\u0c25\u0003\u011a\u008d"+
		"\u0000\u0c22\u0c23\u0004\u008c\u0010\u0000\u0c23\u0c25\u0003\u0124\u0092"+
		"\u0000\u0c24\u0c21\u0001\u0000\u0000\u0000\u0c24\u0c22\u0001\u0000\u0000"+
		"\u0000\u0c25\u0119\u0001\u0000\u0000\u0000\u0c26\u0c2d\u0005\u012c\u0000"+
		"\u0000\u0c27\u0c2d\u0003\u011c\u008e\u0000\u0c28\u0c29\u0004\u008d\u0011"+
		"\u0000\u0c29\u0c2d\u0003\u0122\u0091\u0000\u0c2a\u0c2b\u0004\u008d\u0012"+
		"\u0000\u0c2b\u0c2d\u0003\u0126\u0093\u0000\u0c2c\u0c26\u0001\u0000\u0000"+
		"\u0000\u0c2c\u0c27\u0001\u0000\u0000\u0000\u0c2c\u0c28\u0001\u0000\u0000"+
		"\u0000\u0c2c\u0c2a\u0001\u0000\u0000\u0000\u0c2d\u011b\u0001\u0000\u0000"+
		"\u0000\u0c2e\u0c2f\u0005\u012d\u0000\u0000\u0c2f\u011d\u0001\u0000\u0000"+
		"\u0000\u0c30\u0c32\u0004\u008f\u0013\u0000\u0c31\u0c33\u0005\u0119\u0000"+
		"\u0000\u0c32\u0c31\u0001\u0000\u0000\u0000\u0c32\u0c33\u0001\u0000\u0000"+
		"\u0000\u0c33\u0c34\u0001\u0000\u0000\u0000\u0c34\u0c5c\u0005\u0127\u0000"+
		"\u0000\u0c35\u0c37\u0004\u008f\u0014\u0000\u0c36\u0c38\u0005\u0119\u0000"+
		"\u0000\u0c37\u0c36\u0001\u0000\u0000\u0000\u0c37\u0c38\u0001\u0000\u0000"+
		"\u0000\u0c38\u0c39\u0001\u0000\u0000\u0000\u0c39\u0c5c\u0005\u0128\u0000"+
		"\u0000\u0c3a\u0c3c\u0004\u008f\u0015\u0000\u0c3b\u0c3d\u0005\u0119\u0000"+
		"\u0000\u0c3c\u0c3b\u0001\u0000\u0000\u0000\u0c3c\u0c3d\u0001\u0000\u0000"+
		"\u0000\u0c3d\u0c3e\u0001\u0000\u0000\u0000\u0c3e\u0c5c\u0007.\u0000\u0000"+
		"\u0c3f\u0c41\u0005\u0119\u0000\u0000\u0c40\u0c3f\u0001\u0000\u0000\u0000"+
		"\u0c40\u0c41\u0001\u0000\u0000\u0000\u0c41\u0c42\u0001\u0000\u0000\u0000"+
		"\u0c42\u0c5c\u0005\u0126\u0000\u0000\u0c43\u0c45\u0005\u0119\u0000\u0000"+
		"\u0c44\u0c43\u0001\u0000\u0000\u0000\u0c44\u0c45\u0001\u0000\u0000\u0000"+
		"\u0c45\u0c46\u0001\u0000\u0000\u0000\u0c46\u0c5c\u0005\u0123\u0000\u0000"+
		"\u0c47\u0c49\u0005\u0119\u0000\u0000\u0c48\u0c47\u0001\u0000\u0000\u0000"+
		"\u0c48\u0c49\u0001\u0000\u0000\u0000\u0c49\u0c4a\u0001\u0000\u0000\u0000"+
		"\u0c4a\u0c5c\u0005\u0124\u0000\u0000\u0c4b\u0c4d\u0005\u0119\u0000\u0000"+
		"\u0c4c\u0c4b\u0001\u0000\u0000\u0000\u0c4c\u0c4d\u0001\u0000\u0000\u0000"+
		"\u0c4d\u0c4e\u0001\u0000\u0000\u0000\u0c4e\u0c5c\u0005\u0125\u0000\u0000"+
		"\u0c4f\u0c51\u0005\u0119\u0000\u0000\u0c50\u0c4f\u0001\u0000\u0000\u0000"+
		"\u0c50\u0c51\u0001\u0000\u0000\u0000\u0c51\u0c52\u0001\u0000\u0000\u0000"+
		"\u0c52\u0c5c\u0005\u012a\u0000\u0000\u0c53\u0c55\u0005\u0119\u0000\u0000"+
		"\u0c54\u0c53\u0001\u0000\u0000\u0000\u0c54\u0c55\u0001\u0000\u0000\u0000"+
		"\u0c55\u0c56\u0001\u0000\u0000\u0000\u0c56\u0c5c\u0005\u0129\u0000\u0000"+
		"\u0c57\u0c59\u0005\u0119\u0000\u0000\u0c58\u0c57\u0001\u0000\u0000\u0000"+
		"\u0c58\u0c59\u0001\u0000\u0000\u0000\u0c59\u0c5a\u0001\u0000\u0000\u0000"+
		"\u0c5a\u0c5c\u0005\u012b\u0000\u0000\u0c5b\u0c30\u0001\u0000\u0000\u0000"+
		"\u0c5b\u0c35\u0001\u0000\u0000\u0000\u0c5b\u0c3a\u0001\u0000\u0000\u0000"+
		"\u0c5b\u0c40\u0001\u0000\u0000\u0000\u0c5b\u0c44\u0001\u0000\u0000\u0000"+
		"\u0c5b\u0c48\u0001\u0000\u0000\u0000\u0c5b\u0c4c\u0001\u0000\u0000\u0000"+
		"\u0c5b\u0c50\u0001\u0000\u0000\u0000\u0c5b\u0c54\u0001\u0000\u0000\u0000"+
		"\u0c5b\u0c58\u0001\u0000\u0000\u0000\u0c5c\u011f\u0001\u0000\u0000\u0000"+
		"\u0c5d\u0c5e\u0005\u00fa\u0000\u0000\u0c5e\u0c65\u0003\u00f4z\u0000\u0c5f"+
		"\u0c65\u0003*\u0015\u0000\u0c60\u0c65\u0003\u00f2y\u0000\u0c61\u0c62\u0007"+
		"/\u0000\u0000\u0c62\u0c63\u0005\u009c\u0000\u0000\u0c63\u0c65\u0005\u009d"+
		"\u0000\u0000\u0c64\u0c5d\u0001\u0000\u0000\u0000\u0c64\u0c5f\u0001\u0000"+
		"\u0000\u0000\u0c64\u0c60\u0001\u0000\u0000\u0000\u0c64\u0c61\u0001\u0000"+
		"\u0000\u0000\u0c65\u0121\u0001\u0000\u0000\u0000\u0c66\u0c67\u00070\u0000"+
		"\u0000\u0c67\u0123\u0001\u0000\u0000\u0000\u0c68\u0c69\u00071\u0000\u0000"+
		"\u0c69\u0125\u0001\u0000\u0000\u0000\u0c6a\u0c6b\u00072\u0000\u0000\u0c6b"+
		"\u0127\u0001\u0000\u0000\u0000\u01a3\u0131\u0134\u013e\u0143\u0149\u0162"+
		"\u0167\u016f\u0177\u0179\u018d\u0191\u0197\u019a\u019d\u01a4\u01a7\u01ab"+
		"\u01ae\u01b5\u01c0\u01c2\u01ca\u01cd\u01d1\u01d4\u01da\u01e5\u01eb\u01f0"+
		"\u0230\u0239\u023d\u0243\u0247\u024c\u0252\u025e\u0266\u026c\u0279\u027e"+
		"\u028e\u0295\u0299\u029f\u02ae\u02b2\u02b8\u02be\u02c1\u02c4\u02ca\u02ce"+
		"\u02d6\u02d8\u02e1\u02e4\u02ed\u02f2\u02f8\u02ff\u0302\u0308\u0313\u0316"+
		"\u031a\u031f\u0324\u032b\u032e\u0331\u0338\u033d\u0346\u034e\u0354\u0357"+
		"\u035a\u0360\u0364\u0368\u036c\u036e\u0376\u037e\u0384\u038a\u038d\u0391"+
		"\u0394\u0398\u03b4\u03b7\u03bb\u03c1\u03c4\u03c7\u03cd\u03d5\u03da\u03e0"+
		"\u03e6\u03ee\u03f5\u03fd\u040e\u041c\u041f\u0425\u042e\u0437\u043e\u0441"+
		"\u044d\u0451\u0458\u04cc\u04d4\u04dc\u04e5\u04ef\u04f3\u04f6\u04fc\u0502"+
		"\u050e\u051a\u051f\u0528\u0530\u0537\u0539\u053c\u0541\u0545\u054a\u054d"+
		"\u0552\u0557\u055a\u055f\u0563\u0568\u056a\u056e\u0577\u057f\u0588\u058f"+
		"\u0598\u059d\u05a0\u05b6\u05b8\u05c1\u05c8\u05cb\u05d2\u05d6\u05dc\u05e4"+
		"\u05ef\u05fa\u0601\u0607\u0614\u061b\u0622\u062e\u0636\u063c\u063f\u0648"+
		"\u064b\u0654\u0657\u0660\u0663\u066c\u066f\u0672\u0677\u0679\u0685\u068c"+
		"\u0693\u0696\u0698\u06a4\u06a8\u06ac\u06b2\u06b6\u06be\u06c2\u06c5\u06c8"+
		"\u06cb\u06cf\u06d3\u06d8\u06dc\u06df\u06e2\u06e5\u06e9\u06ee\u06f2\u06f5"+
		"\u06f8\u06fb\u06fd\u0703\u070a\u070f\u0712\u0715\u0719\u0723\u0727\u0729"+
		"\u072c\u0730\u0736\u073a\u0745\u074f\u075b\u076a\u076f\u0776\u0786\u078b"+
		"\u0798\u079d\u07a5\u07ab\u07af\u07b8\u07c2\u07d1\u07d6\u07d8\u07dc\u07e5"+
		"\u07f2\u07f7\u07fb\u0803\u0806\u080a\u0818\u0825\u082a\u082e\u0831\u0836"+
		"\u083f\u0842\u0847\u084e\u0851\u0856\u085c\u0862\u0866\u086c\u0870\u0873"+
		"\u0878\u087b\u0880\u0884\u0887\u088a\u0890\u0895\u089a\u08ac\u08ae\u08b1"+
		"\u08bc\u08c5\u08cc\u08d4\u08db\u08df\u08e7\u08ef\u08f5\u08fd\u0909\u090c"+
		"\u0912\u0916\u0918\u0921\u092d\u092f\u0936\u093d\u0943\u0949\u094b\u0952"+
		"\u095a\u0960\u0967\u096d\u0971\u0973\u097a\u0983\u098a\u0994\u0999\u099d"+
		"\u09a6\u09b3\u09b5\u09bd\u09bf\u09c3\u09cb\u09d4\u09da\u09e2\u09e7\u09f3"+
		"\u09f8\u09fb\u0a01\u0a05\u0a0a\u0a0f\u0a14\u0a1a\u0a2f\u0a31\u0a3a\u0a3e"+
		"\u0a47\u0a4b\u0a5d\u0a60\u0a68\u0a71\u0a88\u0a93\u0a9a\u0a9d\u0aa6\u0aaa"+
		"\u0aae\u0aba\u0ad3\u0ada\u0add\u0aec\u0af0\u0afa\u0afc\u0b09\u0b0b\u0b18"+
		"\u0b1c\u0b23\u0b28\u0b30\u0b37\u0b48\u0b4c\u0b52\u0b58\u0b61\u0b65\u0b67"+
		"\u0b6e\u0b75\u0b78\u0b7b\u0b82\u0b89\u0b8c\u0b93\u0b98\u0b9d\u0ba0\u0bad"+
		"\u0bc1\u0bcb\u0bce\u0bd7\u0bda\u0bdc\u0bdf\u0be2\u0bf4\u0bfd\u0c04\u0c0b"+
		"\u0c12\u0c1c\u0c1f\u0c24\u0c2c\u0c32\u0c37\u0c3c\u0c40\u0c44\u0c48\u0c4c"+
		"\u0c50\u0c54\u0c58\u0c5b\u0c64";
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