/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.Expression;

/**
 * @since 3.3
 * 
 */
public class ServiceData {
	private final String id;

	private final Expression visibleWhenExpression;

	/**
	 * @param id
	 * @param visibleWhen
	 */
	public ServiceData(String id, Expression visibleWhen) {
		this.id = id;
		this.visibleWhenExpression = visibleWhen;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the visible.
	 */
	public Expression getVisibleWhen() {
		return visibleWhenExpression;
	}
}
