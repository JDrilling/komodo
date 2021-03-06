/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.relational;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.komodo.relational.constants.RelationalConstants;
import org.komodo.relational.model.Schema;
import org.komodo.relational.model.Table;
import org.komodo.spi.outcome.IOutcome;

/**
 * Test Class to test Table
 * 
 */
public class TestSchema {
	
	private static final String SCHEMA_NAME = "mySchema";  //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public TestSchema( ) {
		super();
	}
	
	/**
     * Test simple creation
     */
    @Test
    public void testCreate() {
    	Schema schema = RelationalUtil.createSchema(SCHEMA_NAME);
    	String tName = schema.getName();
    	int type = schema.getType();
    	
    	assertEquals(SCHEMA_NAME, tName);
    	assertEquals(type, RelationalConstants.TYPES.SCHEMA);
    }
    
	/**
     * Test adding columns
     */
    @Test
    public void testAddColumns() {
    	Schema schema = RelationalUtil.createSchema(SCHEMA_NAME);

    	schema.addTable(RelationalUtil.createTable("table1")); //$NON-NLS-1$
    	
    	assertEquals(1, schema.getTables().size());
    }

	/**
     * Test adding columns
     */
    @Test
    public void testAddRemoveColumns() {
    	Schema schema = RelationalUtil.createSchema(SCHEMA_NAME);

    	Table table1 = RelationalUtil.createTable("table1"); //$NON-NLS-1$
    	
    	schema.addTable(table1);
    	
    	schema.removeTable(table1);
    	
    	assertEquals(0, schema.getTables().size());
    }
    
	/**
     * Test validation.  expected result - warning : no columns defined
     */
    @Test
    public void testValidate1() {
    	Schema schema = RelationalUtil.createSchema(SCHEMA_NAME);
    	
    	IOutcome outcome = schema.validate();
    	
    	assertEquals(IOutcome.Level.OK, outcome.getLevel());
//    	assertEquals("No columns defined for table", outcome.getMessage()); //$NON-NLS-1$
    }
        
}
