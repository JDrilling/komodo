/*************************************************************************************
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
 ************************************************************************************/
package org.komodo.shell.commands.core;

import org.komodo.shell.BuiltInShellCommand;
import org.komodo.shell.Messages;
import org.komodo.shell.api.WorkspaceContext;
import org.komodo.shell.api.WorkspaceStatus;

/**
 * Displays a summary of the current status, including what repository the
 * user is currently connected to (if any).
 *
 */
public class StatusCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public StatusCommand() {
	}

	/**
	 * @see org.komodo.shell.api.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		WorkspaceStatus wsStatus = getWorkspaceStatus();

		String currentRepo = "local Repository"; //$NON-NLS-1$
		String currentServer = "[none : not connected]"; //$NON-NLS-1$
		WorkspaceContext currentContext = wsStatus.getCurrentContext();
		
		// Current Repository
		print(Messages.getString("StatusCommand.CurrentRepo", currentRepo)); //$NON-NLS-1$
		
		// Current Server
		print(Messages.getString("StatusCommand.CurrentServer", currentServer)); //$NON-NLS-1$
		
		// Current Context path
		print(Messages.getString("StatusCommand.CurrentContext", currentContext.getFullName())); //$NON-NLS-1$

		return true;
	}

}
