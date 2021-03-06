/*
 * JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
 */
package org.komodo.relational.constants;

/**
 * 
 *
 *
 */
@SuppressWarnings("javadoc")
public interface RelationalConstants {
    final static String XMI_EXT = ".xmi"; //$NON-NLS-1$

    public static final String PLUGIN_ID = "org.komodo.relational"; //$NON-NLS-1$
    
    /**
     * Relational model object types
     */
    class TYPES {
        public static final int UNDEFINED = -1;
        public static final int MODEL = 0;
        public static final int SCHEMA = 1;
        public static final int CATALOG = 2;
        public static final int TABLE = 3;
        public static final int VIEW = 4;
        public static final int PROCEDURE = 5;
        public static final int PARAMETER = 6;
        public static final int COLUMN = 7;
        public static final int PK = 8;
        public static final int FK = 9;
        public static final int UC = 10;
        public static final int AP = 11;
        public static final int RESULT_SET = 12;
        public static final int INDEX = 13;
    }
    
    /**
     *  relational model object type names
     */
    final static String[] TYPE_NAMES = {
    	"Model", //$NON-NLS-1$
    	"Schema", //$NON-NLS-1$
    	"Catalog", //$NON-NLS-1$
    	"Table", //$NON-NLS-1$
    	"View", //$NON-NLS-1$
    	"Procedure", //$NON-NLS-1$
    	"Parameter", //$NON-NLS-1$
    	"Column", //$NON-NLS-1$
    	"Primary Key", //$NON-NLS-1$
    	"Foreign Key", //$NON-NLS-1$
    	"Unique Constraint", //$NON-NLS-1$
    	"Access Pattern", //$NON-NLS-1$
    	"Result Set", //$NON-NLS-1$
    	"Index" //$NON-NLS-1$
    };
    
    /**
     * relational object type literal strings
     */
    class TYPES_LITERAL {
        public static final String UNDEFINED = "UNDEFINED"; //$NON-NLS-1$
        public static final String MODEL = "MODEL"; //$NON-NLS-1$
        public static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$
        public static final String CATALOG = "CATALOG"; //$NON-NLS-1$
        public static final String TABLE = "TABLE"; //$NON-NLS-1$
        public static final String VIEW = "VIEW"; //$NON-NLS-1$
        public static final String PROCEDURE = "PROCEDURE"; //$NON-NLS-1$
        public static final String PARAMETER = "PARAMETER"; //$NON-NLS-1$
        public static final String COLUMN = "COLUMN"; //$NON-NLS-1$
        public static final String PRIMARYKEY = "PRIMARY-KEY"; //$NON-NLS-1$
        public static final String FOREIGNKEY = "FOREIGN-KEY"; //$NON-NLS-1$
        public static final String UNIQUECONSTRAINT = "UNIQUE-CONSTRAINT"; //$NON-NLS-1$
        public static final String ACCESSPATTERN = "ACCESS-PATTERN"; //$NON-NLS-1$
        public static final String RESULTSET = "RESULT-SET"; //$NON-NLS-1$
        public static final String INDEX = "INDEX"; //$NON-NLS-1$
    }
    
    // ALL_EXCEPT_LIKE, LIKE_ONLY, SEARCHABLE, UNSEARCHABLE

    class SEARCHABILITY {
        public static final String ALL_EXCEPT_LIKE = "ALL_EXCEPT_LIKE"; //$NON-NLS-1$
        public static final String LIKE_ONLY = "LIKE_ONLY"; //$NON-NLS-1$
        public static final String SEARCHABLE = "SEARCHABLE"; //$NON-NLS-1$
        public static final String UNSEARCHABLE = "UNSEARCHABLE"; //$NON-NLS-1$
        public static final String[] AS_ARRAY = { ALL_EXCEPT_LIKE, LIKE_ONLY, SEARCHABLE, UNSEARCHABLE };
    }
    
    // NO_NULLS, NULLABLE, NULLABLE_UNKNOWN
    class NULLABLE {
        public static final String NO_NULLS = "NO_NULLS"; //$NON-NLS-1$
        public static final String NULLABLE = "NULLABLE"; //$NON-NLS-1$
        public static final String NULLABLE_UNKNOWN = "NULLABLE_UNKNOWN"; //$NON-NLS-1$
        public static final String[] AS_ARRAY = { NO_NULLS, NULLABLE, NULLABLE_UNKNOWN };
        public static final String DEFAULT_VALUE = NULLABLE;
    }
    
    // MANY, ONE, UNSPECIFICIED, ZERO_TO_MANY, ZERO_TO_ONE
    class MULTIPLICITY {
        public static final String MANY = "MANY"; //$NON-NLS-1$
        public static final String ONE = "ONE"; //$NON-NLS-1$
        public static final String UNSPECIFICIED = "UNSPECIFICIED"; //$NON-NLS-1$
        public static final String ZERO_TO_MANY = "ZERO_TO_MANY"; //$NON-NLS-1$
        public static final String ZERO_TO_ONE = "ZERO_TO_ONE"; //$NON-NLS-1$
        public static final String[] AS_ARRAY = { MANY, ONE, UNSPECIFICIED, ZERO_TO_MANY, ZERO_TO_ONE };
    }
    
    // AUTO, MULTIPLE, ONE, ZERO
	class UPDATE_COUNT {
        public static final String AUTO = "AUTO"; //$NON-NLS-1$
        public static final String MULTIPLE = "MULTIPLE"; //$NON-NLS-1$
        public static final String ONE = "ONE"; //$NON-NLS-1$
        public static final String ZERO = "ZERO"; //$NON-NLS-1$
        public static final String[] AS_ARRAY = {AUTO, MULTIPLE, ONE, ZERO};
        public static final String DEFAULT_VALUE = AUTO;
    }
    
    // IN, IN_OUT, OUT, RETURN, UNKNOWN
    class DIRECTION {
        public static final String IN = "IN"; //$NON-NLS-1$
        public static final String IN_OUT = "IN_OUT"; //$NON-NLS-1$
        public static final String OUT = "OUT"; //$NON-NLS-1$
        public static final String RETURN = "RETURN"; //$NON-NLS-1$
        public static final String UNKNOWN = "UNKNOWN"; //$NON-NLS-1$
        public static final String[] AS_ARRAY = {IN, IN_OUT, OUT, RETURN, UNKNOWN};
        public static final String[] AS_ARRAY_SOURCE_FUNCTION_OPTIONS = {IN, RETURN};
        public static final String DEFAULT_VALUE = IN;
    }
    
    class BASE_TABLE_EXT_PROPERTIES {
        public static final String NATIVE_QUERY = "relational:native-query"; //$NON-NLS-1$
        public static final String VIEW_TABLE_GLOBAL_TEMP_TABLE = "relational:global-temp-table"; //$NON-NLS-1$
    }
    
    class FOREIGN_KEY_EXT_PROPERTIES {
        public static final String ALLOW_JOIN = "relational:allow-join"; //$NON-NLS-1$
    }
    
    class PROCEDURE_EXT_PROPERTIES {
        public static final String AGGREGATE = "relational:aggregate"; //$NON-NLS-1$
        public static final String ALLOWS_ORDER_BY = "relational:allows-orderby"; //$NON-NLS-1$
        public static final String ALLOWS_DISTINCT = "relational:allows-distinct"; //$NON-NLS-1$
        public static final String ANALYTIC = "relational:analytic"; //$NON-NLS-1$
        public static final String DECOMPOSABLE = "relational:decomposable"; //$NON-NLS-1$
        public static final String DETERMINISTIC = "relational:deterministic"; //$NON-NLS-1$
        public static final String NATIVE_QUERY = "relational:native-query"; //$NON-NLS-1$
        public static final String NON_PREPARED = "relational:non-prepared"; //$NON-NLS-1$
        public static final String USES_DISTINCT_ROWS = "relational:uses-distinct-rows"; //$NON-NLS-1$
        public static final String VARARGS = "relational:varargs"; //$NON-NLS-1$
        public static final String NULL_ON_NULL = "relational:null-on-null"; //$NON-NLS-1$
        public static final String JAVA_CLASS = "relational:java-class"; //$NON-NLS-1$
        public static final String JAVA_METHOD = "relational:java-method"; //$NON-NLS-1$
        public static final String UDF_JAR_PATH = "relational:udfJarPath"; //$NON-NLS-1$
        public static final String FUNCTION_CATEGORY = "relational:function-category"; //$NON-NLS-1$
    }

}
