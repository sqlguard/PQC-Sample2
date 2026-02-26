/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

import java.io.IOException;

import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;

/**
 * Represents a Command such as an entry from COMMAND_TABLE
 * 
 * Every command is known by its name and optionally, an operation to distinguish between multiple commands with the same name
 * 
 * Typically the name matches how the command is invoked on the system, but not necessarily.
 * 
 * @author unknown
 *
 */
public abstract class AbstractCommand {
	public final String name;
	public final String operation;
	
	protected AbstractCommand(String name) {
		this(name, null);
	}
	
	protected AbstractCommand(String name, String operation) {
		this.name = name;
		this.operation = operation;
	}
	
	public boolean hasOperation() {
		return !Check.isEmpty(operation);
	}
	
	public CommandExecutor getExecutor() {
		return getExecutor(null);
	}
	
	public abstract CommandExecutor getExecutor(String trailingArgs);
	
	protected abstract void logActivity(String userName);
	
	public int execute(String trailingArgs, String userName) {
		CommandExecutor executor = getExecutor(trailingArgs);
		if(executor != null) {
			logActivity(userName);
			try {
				return executor.exec();
			} catch (IOException | InterruptedException e) {
				AdHocLogger.logException(e);
			}
		}
		return -1;
	}
	
	public int execute(String userName) {
		return execute(null, userName);
	}
	
	public int execute() {
		return execute(null, null);
	}
	
	@Override
	public String toString() {
		return name + ' ' + operation;
	}
}
