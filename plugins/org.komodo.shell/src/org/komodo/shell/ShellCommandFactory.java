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
package org.komodo.shell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.komodo.shell.api.ShellCommand;
import org.komodo.shell.api.ShellCommandProvider;
import org.komodo.shell.api.WorkspaceContext;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.shell.commands.CommandNotFoundCommand;
import org.komodo.shell.commands.ExitCommand;
import org.komodo.shell.commands.HelpCommand;
import org.komodo.shell.commands.core.CdCommand;
import org.komodo.shell.commands.core.ListCommand;
import org.komodo.shell.commands.core.StatusCommand;
import org.komodo.utils.FileUtils;

/**
 * Factory used to create shell commands.
 * 
 * This class adapted from https://github.com/Governance/s-ramp/blob/master/s-ramp-shell
 * - altered to use WorkspaceStatus.  additional methods added
 * - altered map usage to to change from Shell Context usage
 * 
 * @author eric.wittmann@redhat.com
 */
public class ShellCommandFactory {

	private static String HELP_CMD_NAME = "help"; //$NON-NLS-1$ 
	private static String EXIT_CMD_NAME = "exit"; //$NON-NLS-1$ 
	private static String QUIT_CMD_NAME = "quit"; //$NON-NLS-1$ 

	private WorkspaceStatus wsStatus;
	private Map<String, ShellCommand> commandMap;

	/**
	 * Constructor.
     * @param wsStatus the workspace context
	 */
	public ShellCommandFactory(WorkspaceStatus wsStatus) {
		this.wsStatus = wsStatus;
		registerCommands();
	}

	/**
	 * Registers all known commands.
	 */
	private void registerCommands() {
		commandMap = new HashMap<String, ShellCommand>();

		// commands
		List<WorkspaceContext.Type> allList = new ArrayList<WorkspaceContext.Type>(1);
		allList.add(WorkspaceContext.Type.ALL);
		
		StatusCommand statusCommand = new StatusCommand();
		statusCommand.initValidWsContextTypes();
		commandMap.put("status", statusCommand); //$NON-NLS-1$ 
		
		ListCommand listCommand = new ListCommand();
		listCommand.initValidWsContextTypes();
		commandMap.put("list", listCommand); //$NON-NLS-1$ 

		CdCommand cdCommand = new CdCommand();
		cdCommand.initValidWsContextTypes();
		commandMap.put("cd", cdCommand); //$NON-NLS-1$ 

		discoverContributedCommands();
	}

    /**
     * Discover any contributed commands, both on the classpath and registered
     * in the .komodo/commands.ini file in the user's home directory.
     */
    private void discoverContributedCommands() {
        List<ClassLoader> commandClassloaders = new ArrayList<ClassLoader>();
        commandClassloaders.add(Thread.currentThread().getContextClassLoader());

        // Register commands listed in the user's commands.ini config file
        String userHome = System.getProperty("user.home", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        String commandsDirName = System.getProperty("komodo.shell.commandsDir", //$NON-NLS-1$
                userHome + "/.komodo/commands"); //$NON-NLS-1$
        File commandsDir = new File(commandsDirName);
        if (!commandsDir.exists()) {
            commandsDir.mkdirs();
        }
        if (commandsDir.isDirectory()) {
            try {
            	Collection<File> jarFiles =  FileUtils.getFilesForPattern(commandsDir.getCanonicalPath(), "", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
                List<URL> jarURLs = new ArrayList<URL>(jarFiles.size());
                for (File jarFile : jarFiles) {
                    jarURLs.add(jarFile.toURI().toURL());
                }
                URL[] urls = jarURLs.toArray(new URL[jarURLs.size()]);
                ClassLoader extraCommandsCL = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                commandClassloaders.add(extraCommandsCL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Now that we have identified all ClassLoaders to check for commands, iterate
        // through them all and use the Java ServiceLoader mechanism to actually
        // load the commands.
        for (ClassLoader classLoader : commandClassloaders) {
            for (ShellCommandProvider provider : ServiceLoader.load(ShellCommandProvider.class, classLoader)) {
                Map<String, Class<? extends ShellCommand>> commands = provider.provideCommands();
                for (Map.Entry<String, Class<? extends ShellCommand>> entry : commands.entrySet()) {
                	Class<? extends ShellCommand> commandClass = entry.getValue();
        			if (commandClass != null) {
        				ShellCommand command;
						try {
							command = commandClass.newInstance();
	        				command.initValidWsContextTypes();
	            			commandMap.put(entry.getKey(), command);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        			}
                }
            }
        }
    }

	/**
	 * Called to create a shell command.
	 * @param commandName the name of the command
	 * @return the command
	 * @throws Exception the exception
	 */
	public ShellCommand getCommand(String commandName) throws Exception {
		ShellCommand command = null;
		if (commandName.equals(HELP_CMD_NAME)) {
            command = new HelpCommand(getCommands());
		} else if (commandName.equals(QUIT_CMD_NAME)) {
			command = new ExitCommand();
		} else if (commandName.equals(EXIT_CMD_NAME)) {
			command = new ExitCommand();
		} else {
			command = commandMap.get(commandName);
			if (command == null)
				return new CommandNotFoundCommand();
		}
		return command;
	}
	
	/**
	 * Get valid command names for the current context
	 * @return List<String> list of commands for current context
	 */
	public List<String> getCommandsForCurrentContext( ) {
		List<String> commandList = new ArrayList<String>();
		for(String mapKey : this.commandMap.keySet()) {
			ShellCommand command = this.commandMap.get(mapKey);
			if(command.isValidForWsContext(this.wsStatus.getCurrentContext().getType())) {
				commandList.add(mapKey);
			}
		}
		Collections.sort(commandList);
		return commandList;
	}

	/**
	 * Gets the available commands, ordered by command {@link QName}.
	 */
	private Map<String, ShellCommand> getCommands() {
		TreeMap<String, ShellCommand> treeMap = new TreeMap<String, ShellCommand>(new Comparator<String>() {
			@Override
			public int compare(String name1, String name2) {
				return name1.compareTo(name2);
			}
		});
		treeMap.putAll(this.commandMap);
		return treeMap;
	}
	
    /**
     * Returns fileArray {@code ArrayList} of {@code File} objects that match a pattern in the specified directory. 
     * @param folderToScan The path to look for the matching files
     * @param startWith The beginning portion of the file name
     * @param endsWith The ending portion of the file name (i.e. ".jar")
     * @return fileArray An ArrayList of 
     */
    public final static ArrayList<File> getFilesForPattern(File folderToScan, String startWith, String endsWith) {
	    String target_file ;  // fileThatYouWantToFilter
		File[] listOfFiles = folderToScan.listFiles();
		ArrayList<File> list = new ArrayList();
		
		for (File file:listOfFiles) {
	        if (file.isFile()) {
	            target_file = file.getName();
	            if (target_file.startsWith(startWith)
		                 && target_file.endsWith(endsWith)) {
	            	list.add(file);
		        }
		    }
		 }
		
		return list;    
    }

}
