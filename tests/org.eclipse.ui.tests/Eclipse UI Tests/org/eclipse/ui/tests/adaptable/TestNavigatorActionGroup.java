package org.eclipse.ui.tests.adaptable;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.views.navigator.ResourceNavigatorMessages;

public class TestNavigatorActionGroup extends ActionGroup {

	private AdaptedResourceNavigator navigator;
	private AddBookmarkAction addBookmarkAction;
	private PropertyDialogAction propertyDialogAction;
	private NewWizardAction newWizardAction;

	public TestNavigatorActionGroup(AdaptedResourceNavigator navigator) {
		this.navigator = navigator;
	}

	protected void makeActions() {
		Shell shell = navigator.getSite().getShell();
		addBookmarkAction = new AddBookmarkAction(shell);
		newWizardAction = new NewWizardAction();
		propertyDialogAction =
			new PropertyDialogAction(shell, navigator.getViewer());
	}

	/**
	 * @see ActionGroup#fillContextMenu(IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		MenuManager newMenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.new")); //$NON-NLS-1$
		menu.add(newMenu);
		new NewWizardMenu(newMenu, navigator.getSite().getWorkbenchWindow(), false);
		
		//Update the selections of those who need a refresh before filling
		addBookmarkAction.selectionChanged(selection);
		menu.add(addBookmarkAction);

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		menu.add(new Separator());

		propertyDialogAction.selectionChanged(selection);
		if (propertyDialogAction.isApplicableForSelection())
			menu.add(propertyDialogAction);
	}

	/*
	 * @see ActionFactory#fillActionBarMenu(IMenuManager, IStructuredSelection)
	 */
	public void fillActionBarMenu(
		IMenuManager menu,
		IStructuredSelection selection) {
	}

	/**
	 * Updates the global actions with the given selection.
	 * Be sure to invoke after actions objects have updated, since can* methods delegate to action objects.
	 */
	public void updateGlobalActions(IStructuredSelection selection) {

	}

	/**
	* Contributes actions to the local tool bar and local pulldown menu.
	* @since 2.0
	*/
	public void fillActionBars(IStructuredSelection selection) {
	}

	/**
	* Update the selection for new selection.
	*/
	public void selectionChanged(IStructuredSelection selection) {}

}