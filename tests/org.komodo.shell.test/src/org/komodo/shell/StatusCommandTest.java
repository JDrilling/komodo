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
package org.komodo.shell;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.komodo.shell.api.WorkspaceStatus;

/**
 * Test Class to test StatusCommand
 * 
 */
public class StatusCommandTest extends AbstractCommandTest {

	private static final String STATUS_COMMANDS1 = "statusCommands1.txt"; //$NON-NLS-1$
	private static final String STATUS_COMMANDS2 = "statusCommands2.txt"; //$NON-NLS-1$
	private static final String STATUS_COMMANDS3 = "statusCommands3.txt"; //$NON-NLS-1$
	
	/**
	 * Test for StatusCommand
	 */
	public StatusCommandTest( ) {
		super();
	}
	
	/**
     * Status at the workspace context root
     */
    @Test
    public void testStatus1() {
    	WorkspaceStatus wsStatus = new TestWorkspaceStatus();
    	setup(STATUS_COMMANDS1,wsStatus);
    	
    	execute();
    	
    	String expectedOutput = "Current Repo    : local Repository\nCurrent Server  : [none : not connected]\nCurrent Context : root\n"; //$NON-NLS-1$
    	String writerOutput = getWriterOutput();
    	assertEquals(expectedOutput,writerOutput);
    	assertEquals("root", wsStatus.getCurrentContext().getFullName()); //$NON-NLS-1$
    	
    	teardown();
    }
    
	/**
     * Status at the workspace Project2 context
     */
    @Test
    public void testStatus2() {
    	WorkspaceStatus wsStatus = new TestWorkspaceStatus();
    	setup(STATUS_COMMANDS2,wsStatus);
    	
    	execute();
    	
    	String expectedOutput = "Current Repo    : local Repository\nCurrent Server  : [none : not connected]\nCurrent Context : root.Project2\n"; //$NON-NLS-1$
    	String writerOutput = getWriterOutput();
    	assertEquals(expectedOutput,writerOutput);
    	assertEquals("root.Project2", wsStatus.getCurrentContext().getFullName()); //$NON-NLS-1$
    	
    	teardown();
    }
    
	/**
     * Status at the workspace ViewModel1 context
     */
    @Test
    public void testStatus3() {
    	WorkspaceStatus wsStatus = new TestWorkspaceStatus();
    	setup(STATUS_COMMANDS3,wsStatus);

    	execute();
    	
    	String expectedOutput = "Current Repo    : local Repository\nCurrent Server  : [none : not connected]\nCurrent Context : root.Project2.ViewModel1\n"; //$NON-NLS-1$
    	String writerOutput = getWriterOutput();
    	assertEquals(expectedOutput,writerOutput);
    	assertEquals("root.Project2.ViewModel1", wsStatus.getCurrentContext().getFullName()); //$NON-NLS-1$
   	
    	teardown();
    }
   	
}
