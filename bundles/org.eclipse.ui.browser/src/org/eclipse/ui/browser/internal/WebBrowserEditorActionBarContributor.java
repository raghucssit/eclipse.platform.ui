/**********************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.browser.internal;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
/**
 * ActionBarContributor for the Web browser.
 * Just adds cut, copy, paste actions.
 */
public class WebBrowserEditorActionBarContributor extends EditorActionBarContributor {
	protected WebBrowserEditor editor;
	protected Action back;
	protected Action forward;
	//protected BusyIndicator busy;
	protected Updater updater = new Updater();

	class Updater implements BrowserViewer.IBackNextListener {
		public void updateBackNextBusy() {
			if (back == null)
				return;
			back.setEnabled(getWebBrowser().getBrowser().isBackEnabled());
			forward.setEnabled(getWebBrowser().getBrowser().isForwardEnabled());
			//busy.setBusy(getWebBrowser().loading);
		}
	}

	/**
	 * WebBrowserEditorActionBarContributor constructor comment.
	 */
	public WebBrowserEditorActionBarContributor() {
		super();
	}

	/*
	 * Sets the active editor for the contributor.
	 */
	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof WebBrowserEditor) {
			editor = (WebBrowserEditor) targetEditor;
			WebBrowserEditorInput input = editor.getWebBrowserEditorInput();
			
			if (input.isLocationBarLocal()) {
				IActionBars actionBars = getActionBars();
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), editor.getCopyAction());
				actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), editor.getCutAction());
				actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), editor.getPasteAction());
			}
			
			//if (input.isToolbarGlobal())
			//	getWebBrowser().backNextListener = this.updater;
				
			//editor.updateActions();
		} else
			editor = null;
	}
	
	protected BrowserViewer getWebBrowser() {
		if (editor == null)
			return null;
		
		return editor.webBrowser; 
	}

	/*
    * Contributes to the given tool bar.
    */
   public void contributeToToolBar(IToolBarManager toolBarManager) {
   	if (editor == null)
   		return;
   	/*WebBrowserEditorInput input = editor.getWebBrowserEditorInput();
   	if (input.isLocationBarGlobal()) {
	   	final LocationContributionItem location = new LocationContributionItem();
	   	toolBarManager.add(location);
	   
	   	Action go = new Action() {
	   		public void run() {
	   			getWebBrowser().setURL(location.getText());
	   		}
	   	};
	   	go.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_GO));
	   	go.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_GO));
	   	go.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_GO));
	   	go.setToolTipText(WebBrowserUIPlugin.getResource("%actionWebBrowserGo"));
	   	toolBarManager.add(go);
   	}
   	
   	if (input.isLocationBarGlobal() && input.isToolbarGlobal()) {
   		toolBarManager.add(new Separator());
   	}
   	
   	if (input.isToolbarGlobal()) {
	   	Action favorites = new Action(WebBrowserUIPlugin.getResource("%actionWebBrowserFavorites"), IAction.AS_DROP_DOWN_MENU) {
	   		public void run() {
	   			getWebBrowser().addFavorite();
	   		}
	   		public IMenuCreator getMenuCreator() {
	   			return new IMenuCreator() {
						public void dispose() {
							// do nothing
						}
	
						public Menu getMenu(final Control parent) {
							Menu menu = new Menu(parent);
							
							// locked favorites
							Iterator iterator = WebBrowserUtil.getLockedFavorites().iterator();
							if (iterator.hasNext()) {
								while (iterator.hasNext()) {
									final Favorite f = (Favorite) iterator.next();
									MenuItem item = new MenuItem(menu, SWT.NONE);
									item.setText(f.getName());
									item.setImage(ImageResource.getImage(ImageResource.IMG_FAVORITE));
									item.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											getWebBrowser().setURL(f.getURL());
										}
									});
								}
								
								new MenuItem(menu, SWT.SEPARATOR);
							}
							
							iterator = WebBrowserPreference.getInternalWebBrowserFavorites().iterator();
							if (!iterator.hasNext()) {
								MenuItem item = new MenuItem(menu, SWT.NONE);
								item.setText(WebBrowserUIPlugin.getResource("%actionWebBrowserNoFavorites"));
							}
							while (iterator.hasNext()) {
								final Favorite f = (Favorite) iterator.next();
								MenuItem item = new MenuItem(menu, SWT.NONE);
								item.setText(f.getName());
								item.setImage(ImageResource.getImage(ImageResource.IMG_FAVORITE));
								item.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										getWebBrowser().setURL(f.getURL());
									}
								});
							}
							
							new MenuItem(menu, SWT.SEPARATOR);
					
							MenuItem item = new MenuItem(menu, SWT.NONE);
							item.setText(WebBrowserUIPlugin.getResource("%actionWebBrowserOrganizeFavorites"));
							item.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) {
									OrganizeFavoritesDialog dialog = new OrganizeFavoritesDialog(parent.getShell());
									dialog.open();
								}
							});
							return menu;
						}
	
						public Menu getMenu(final Menu parent) {
							Menu menu = new Menu(parent);
							
							// locked favorites
							Iterator iterator = WebBrowserUtil.getLockedFavorites().iterator();
							if (iterator.hasNext()) {
								while (iterator.hasNext()) {
									final Favorite f = (Favorite) iterator.next();
									MenuItem item = new MenuItem(menu, SWT.NONE);
									item.setText(f.getName());
									item.setImage(ImageResource.getImage(ImageResource.IMG_FAVORITE));
									item.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(SelectionEvent event) {
											getWebBrowser().setURL(f.getURL());
										}
									});
								}
								
								new MenuItem(menu, SWT.SEPARATOR);
							}
							
							iterator = WebBrowserPreference.getInternalWebBrowserFavorites().iterator();
							if (!iterator.hasNext()) {
								MenuItem item = new MenuItem(menu, SWT.NONE);
								item.setText(WebBrowserUIPlugin.getResource("%actionWebBrowserNoFavorites"));
							}
							while (iterator.hasNext()) {
								final Favorite f = (Favorite) iterator.next();
								MenuItem item = new MenuItem(menu, SWT.NONE);
								item.setText(f.getName());
								item.setImage(ImageResource.getImage(ImageResource.IMG_FAVORITE));
								item.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										getWebBrowser().setURL(f.getURL());
									}
								});
							}
							
							new MenuItem(menu, SWT.SEPARATOR);
					
							MenuItem item = new MenuItem(menu, SWT.NONE);
							item.setText(WebBrowserUIPlugin.getResource("%actionWebBrowserOrganizeFavorites"));
							item.addSelectionListener(new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) {
									OrganizeFavoritesDialog dialog = new OrganizeFavoritesDialog(parent.getShell());
									dialog.open();
								}
							});
							return menu;
						}
	   			};
	   		}
	   	};
	   	favorites.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_FAVORITES));
	   	favorites.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_FAVORITES));
	   	favorites.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_FAVORITES));
	   	favorites.setToolTipText(WebBrowserUIPlugin.getResource("%actionWebBrowserFavorites"));
	   	toolBarManager.add(favorites);
	   	
	   	back = new Action() {
	   		public void run() {
	   			getWebBrowser().back();
	   		}
	   	};
	   	back.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_BACKWARD));
			back.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_BACKWARD));
			back.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_BACKWARD));
			back.setToolTipText(WebBrowserUIPlugin.getResource("%actionWebBrowserBack"));
	   	toolBarManager.add(back);
	   	
	   	forward = new Action() {
	   		public void run() {
	   			getWebBrowser().forward();
	   		}
	   	};
	   	forward.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_FORWARD));
	   	forward.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_FORWARD));
			forward.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_FORWARD));
			forward.setToolTipText(WebBrowserUIPlugin.getResource("%actionWebBrowserForward"));
	   	toolBarManager.add(forward);
	   	
	   	Action stop = new Action() {
	   		public void run() {
	   			getWebBrowser().stop();
	   		}
	   	};
	   	stop.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_STOP));
	   	stop.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_STOP));
	   	stop.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_STOP));
	   	stop.setToolTipText(WebBrowserUIPlugin.getResource("%actionWebBrowserStop"));
	   	toolBarManager.add(stop);
	   	
	   	Action refresh = new Action() {
	   		public void run() {
	   			getWebBrowser().refresh();
	   		}
	   	};
	   	refresh.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_NAV_REFRESH));
	   	refresh.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_NAV_REFRESH));
	   	refresh.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_NAV_REFRESH));
	   	refresh.setToolTipText(WebBrowserUIPlugin.getResource("%actionWebBrowserRefresh"));
	   	toolBarManager.add(refresh);
   	}*/
   	
   	/*toolBarManager.add(new Separator());
   	
   	ControlContribution busyCont = new ControlContribution("webbrowser.busy") {
   	   protected Control createControl(Composite parent) {
   	   	busy = new BusyIndicator(parent, SWT.NONE);
   	   	return busy;
   	   }
   	};
   	toolBarManager.add(busyCont);*/
   }
}