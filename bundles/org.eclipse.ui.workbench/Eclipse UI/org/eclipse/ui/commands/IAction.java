/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IAction {

	/**
	 * JAVADOC
	 */	
	void execute();

	/**
	 * TODO temporary method
	 */	
	void execute(Event event);

	//String getDescription();
	
	//String getName();
	
	//String getHelpId();

	/**
	 * JAVADOC
	 */	
	boolean isEnabled();
}
