/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A widget group that displays path variables. 
 * Includes buttons to edit, remove existing variables and create new ones.
 */
public class PathVariablesBlock {
	// sizing constants
	private static final int SIZING_SELECTION_PANE_WIDTH = 400;

	// parent shell
	private Shell shell;
	
	private Table variableTable;
	private Button addButton;
	private Button editButton;
	private Button removeButton;

	// used to compute layout sizes
	private FontMetrics fontMetrics;

	// create a multi select table
	private boolean multiSelect;
	// IResource.FILE and/or IResource.FOLDER
	private int variableType;
	// temporary collection for keeping currently defined variables
	private SortedMap tempPathVariables;
	// set of removed variables' names
	private Set removedVariableNames;
	// reference to the workspace's path variable manager
	private IPathVariableManager pathVariableManager;
	
	// file image
	private final Image FILE_IMG = WorkbenchImages.getImage(ISharedImages.IMG_OBJ_FILE);
	// folder image
	private final Image FOLDER_IMG = WorkbenchImages.getImage(ISharedImages.IMG_OBJ_FOLDER);

	/**
	 * Creates a new PathVariablesBlock.
	 *
	 * @param multiSelect create a multi select tree
	 * @param variableType the type of variables that are displayed in 
	 * 	the widget group. <code>IResource.FILE</code> and/or <code>IResource.FOLDER</code>
	 * 	logically ORed together.
	 */
	public PathVariablesBlock(boolean multiSelect, int variableType) {
		this.multiSelect = multiSelect;
		this.variableType = variableType;
		pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		removedVariableNames = new HashSet();
		tempPathVariables = new TreeMap();
		// initialize internal model
		initTemporaryState();
	}
	/**
	 * Opens a dialog for creating a new variable.
	 */
	private void addNewVariable() {
		// constructs a dialog for editing the new variable's current name and value
		PathVariableDialog dialog =
			new PathVariableDialog(
				shell, 
				PathVariableDialog.NEW_VARIABLE, 
				variableType, 
				pathVariableManager, 
				tempPathVariables.keySet());

		// opens the dialog - just returns if the user cancels it
		if (dialog.open() == Window.CANCEL)
			return;

		// otherwise, adds the new variable (or updates an existing one) in the
		// temporary collection of currently defined variables
		String newVariableName = dialog.getVariableName();
		IPath newVariableValue = new Path(dialog.getVariableValue());
		tempPathVariables.put(newVariableName, newVariableValue);

		// the UI must be updated
		updateWidgetState(newVariableName);
	}
	/**
	 * Creates the widget group.
	 * 
	 * @param parent the widget parent
	 * @return container of the widgets 
	 */
	public Control createContents(Composite parent) {
		Font font = parent.getFont();

		initializeDialogUnits(parent);
		shell = parent.getShell();		

		// define container & its layout
		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		pageComponent.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_SELECTION_PANE_WIDTH;
		pageComponent.setLayoutData(data);
		pageComponent.setFont(font);

		// layout the table & its buttons
		Label variableLabel = new Label(pageComponent, SWT.LEFT);
		variableLabel.setText(WorkbenchMessages.getString("PathVariablesBlock.variablesLabel")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		variableLabel.setLayoutData(data);
		variableLabel.setFont(font);

		int tableStyle = SWT.BORDER | SWT.FULL_SELECTION;
		if (multiSelect) {
			tableStyle |= SWT.MULTI;
		}
		variableTable = new Table(pageComponent, tableStyle);
		variableTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
			}
		});
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = variableTable.getItemHeight() * 7;
		variableTable.setLayoutData(data);
		variableTable.setFont(font);

		createButtonGroup(pageComponent);
		// populate table with current internal state and set buttons' initial state
		updateWidgetState(null);

		return pageComponent;
	}
	/**
	 * Opens a dialog for editing an existing variable.
	 *
	 * @see PathVariableDialog
	 */
	private void editSelectedVariable() {
		// retrieves the name and value for the currently selected variable
		TableItem item = variableTable.getItem(variableTable.getSelectionIndex());
		String variableName = (String) item.getData();
		IPath variableValue = (IPath) tempPathVariables.get(variableName);

		// constructs a dialog for editing the variable's current name and value
		PathVariableDialog dialog =
			new PathVariableDialog(
				shell, 
				PathVariableDialog.EXISTING_VARIABLE, 
				variableType,
				pathVariableManager, 
				tempPathVariables.keySet());
		dialog.setVariableName(variableName);
		dialog.setVariableValue(variableValue.toString());

		// opens the dialog - just returns if the user cancels it
		if (dialog.open() == Window.CANCEL)
			return;

		// the name can be changed, so we remove the current variable definition...
		removedVariableNames.add(variableName);
		tempPathVariables.remove(variableName);

		String newVariableName = dialog.getVariableName();
		IPath newVariableValue = new Path(dialog.getVariableValue());

		// and add it again (maybe with a different name)
		tempPathVariables.put(newVariableName, newVariableValue);

		// now we must refresh the UI state
		updateWidgetState(newVariableName);

	}
	/**
	 * Returns the names of the selected variables.
	 *  
	 * @return the names of the selected variables. Returns an empty 
	 * 	array if the widget group has not been created yet by calling 
	 * 	<code>createContents</code>
	 */
	public String[] getSelection() {
		if (variableTable == null) {
			return new String[0];
		}
		TableItem[] items = variableTable.getSelection();
		String[] variableNames = new String[items.length];
		
		for (int i = 0; i < items.length; i++) {
			variableNames[i] = (String) items[i].getData();
		}
		return variableNames;
	}
	/**
	 * Creates the add/edit/remove buttons
	 * 
	 * @param parent the widget parent
	 */
	private void createButtonGroup(Composite parent) {
		Font font = parent.getFont();
		Composite groupComponent = new Composite(parent, SWT.NULL);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);
		groupComponent.setFont(font);

		addButton = new Button(groupComponent, SWT.PUSH);
		addButton.setText(WorkbenchMessages.getString("PathVariablesBlock.addVariableButton")); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addNewVariable();
			}
		});
		addButton.setFont(font);
		setButtonLayoutData(addButton);

		editButton = new Button(groupComponent, SWT.PUSH);
		editButton.setText(WorkbenchMessages.getString("PathVariablesBlock.editVariableButton")); //$NON-NLS-1$
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelectedVariable();
			}
		});
		editButton.setFont(font);
		setButtonLayoutData(editButton);

		removeButton = new Button(groupComponent, SWT.PUSH);
		removeButton.setText(WorkbenchMessages.getString("PathVariablesBlock.removeVariableButton")); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedVariables();
			}
		});
		removeButton.setFont(font);
		setButtonLayoutData(removeButton);
	}
	/**
	 * Initializes the computation of horizontal and vertical dialog units
	 * based on the size of current font.
	 * <p>
	 * This method must be called before <code>setButtonLayoutData</code> 
	 * is called.
	 * </p>
	 *
	 * @param control a control from which to obtain the current font
	 */
	protected void initializeDialogUnits(Control control) {
		// Compute and store a font metric
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
	}
	/**
	 * (Re-)Initialize collections used to mantain temporary variable state.
	 */
	private void initTemporaryState() {
		String[] varNames = pathVariableManager.getPathVariableNames();

		tempPathVariables.clear();		
		for (int i = 0; i < varNames.length; i++) {
			IPath value = pathVariableManager.getValue(varNames[i]);
			
			// the value may not exist any more
			if (value != null) {
				boolean isFile = value.toFile().isFile();
				if ((isFile && (variableType & IResource.FILE) != 0) ||
				 	(isFile == false && (variableType & IResource.FOLDER) != 0)) {
			 
					tempPathVariables.put(varNames[i], value);
				}
			}
		}
		removedVariableNames.clear();
	}
	/**
	 * Updates button enabled state, depending on the number of currently selected
	 * variables in the table.
	 */
	private void updateEnabledState() {
		int itemsSelectedCount = variableTable.getSelectionCount();
		editButton.setEnabled(itemsSelectedCount == 1);
		removeButton.setEnabled(itemsSelectedCount > 0);
	}
	/**
	 * Rebuilds table widget state with the current list of variables (reflecting
	 * any changes, additions and removals), and selects the item corresponding to
	 * the given variable name. If the variable name is <code>null</code>, the
	 * first item (if any) will be selected.
	 * 
	 * @param selectedVarName the name for the variable to be selected (may be
	 * <code>null</code>)
	 * @see IPathVariableManager#getPathVariableNames()
	 * @see IPathVariableManager#getValue(String)
	 */
	private void updateVariableTable(String selectedVarName) {
		variableTable.removeAll();
		int selectedVarIndex = 0;
		for (Iterator varNames = tempPathVariables.keySet().iterator(); varNames.hasNext();) {
			TableItem item = new TableItem(variableTable, SWT.NONE);
			String varName = (String) varNames.next();
			IPath value = (IPath) tempPathVariables.get(varName);
			item.setText(varName + " - " + value.toString()); //$NON-NLS-1$ 
			// the corresponding variable name is stored in each table widget item
			item.setData(varName);
			item.setImage(value.toFile().isFile() ? FILE_IMG : FOLDER_IMG);
			if (varName.equals(selectedVarName))
				selectedVarIndex = variableTable.getItemCount() - 1;
		}
		if (variableTable.getItemCount() > selectedVarIndex)
			variableTable.setSelection(selectedVarIndex);
	}
	/**
	 * Commits the temporary state to the path variable manager in response to user
	 * confirmation.
	 *
	 * @see IPathVariableManager#setValue(String, IPath)
	 */
	public boolean performOk() {
		try {
			// first process removed variables  
			for (Iterator removed = removedVariableNames.iterator(); removed.hasNext();) {
				String removedVariableName = (String) removed.next();
				// only removes variables that have not been added again
				if (!tempPathVariables.containsKey(removedVariableName))
					pathVariableManager.setValue(removedVariableName, null);
			}

			// then process the current collection of variables, adding/updating them
			for (Iterator current = tempPathVariables.entrySet().iterator(); current.hasNext();) {
				Map.Entry entry = (Map.Entry) current.next();
				String variableName = (String) entry.getKey();
				IPath variableValue = (IPath) entry.getValue();
				pathVariableManager.setValue(variableName, variableValue);
			}
			// re-initialize temporary state
			initTemporaryState();

			// performOk accepted
			return true;
		} catch (CoreException ce) {
			ErrorDialog.openError(shell, null, null, ce.getStatus());
		}
		return false;
	}
	/**
	 * Removes the currently selected variables.
	 */
	private void removeSelectedVariables() {
		// remove each selected element
		int[] selectedIndices = variableTable.getSelectionIndices();
		for (int i = 0; i < selectedIndices.length; i++) {
			TableItem selectedItem = variableTable.getItem(selectedIndices[i]);
			String varName = (String) selectedItem.getData();
			removedVariableNames.add(varName);
			tempPathVariables.remove(varName);
		}
		updateWidgetState(null);
	}
	/**
	 * Sets the <code>GridData</code> on the specified button to
	 * be one that is spaced for the current dialog page units. The
	 * method <code>initializeDialogUnits</code> must be called once
	 * before calling this method for the first time.
	 *
	 * @param button the button to set the <code>GridData</code>
	 * @return the <code>GridData</code> set on the specified button
	 */
	private GridData setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_HEIGHT);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		return data;
	}
	/**
	 * Updates the widget's current state: refreshes the table with the current 
	 * defined variables, selects the item corresponding to the given variable 
	 * (selects the first item if <code>null</code> is provided) and updates 
	 * the enabled state for the Add/Remove/Edit buttons.
	 * 
	 * @param selectedVarName the name of the variable to be selected (may be null)
	 */
	private void updateWidgetState(String selectedVarName) {
		updateVariableTable(selectedVarName);
		updateEnabledState();
	}
}
