/*
 * Copyright 2012 JBoss Inc
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
package org.komodo.shell.commands;

import org.komodo.shell.BuiltInShellCommand;
import org.komodo.shell.Messages;
import org.komodo.shell.Messages.SHELL;

/**
 * Implements the 'exit' command.
 * 
 * This class adapted from https://github.com/Governance/s-ramp/blob/master/s-ramp-shell
 * - altered to use different Message class
 * 
 * @author eric.wittmann@redhat.com
 */
public class ExitCommand extends BuiltInShellCommand {

	
	/**
	 * Constructor.
	 */
	public ExitCommand() {
	}

	/**
	 * @see org.komodo.shell.api.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("exit"); //$NON-NLS-1$
	}

	/**
	 * @see org.komodo.shell.api.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
	}

	/**
	 * @see org.komodo.shell.api.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		print(Messages.getString(SHELL.GOOD_BYE)); 
		System.exit(0);
        return true;
	}

}
