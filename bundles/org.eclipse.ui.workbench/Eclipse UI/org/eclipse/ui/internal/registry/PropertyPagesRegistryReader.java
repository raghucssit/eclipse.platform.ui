/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;

/**
 * This class loads property pages from the registry.
 */
public class PropertyPagesRegistryReader extends CategorizedPageRegistryReader {
    public static final String ATT_NAME_FILTER = "nameFilter";//$NON-NLS-1$

    public static final String ATT_FILTER_NAME = "name";//$NON-NLS-1$

    public static final String ATT_FILTER_VALUE = "value";//$NON-NLS-1$

    public static final String ATT_CLASS = "class";//$NON-NLS-1$

    private static final String TAG_PAGE = "page";//$NON-NLS-1$

    public static final String TAG_FILTER = "filter";//$NON-NLS-1$

    public static final String ATT_NAME = "name";//$NON-NLS-1$

    private static final String ATT_ID = "id";//$NON-NLS-1$

    public static final String ATT_ICON = "icon";//$NON-NLS-1$

    public static final String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$

    public static final String ATT_ADAPTABLE = "adaptable";//$NON-NLS-1$
    
	private static final String TAG_KEYWORD_REFERENCE = "keywordReference"; //$NON-NLS-1$

    private static final String P_TRUE = "true";//$NON-NLS-1$

    private HashMap filterProperties;
    
    private Collection pages = new ArrayList();

    private PropertyPageContributorManager manager;
    
    class PropertyCategoryNode extends CategoryNode{
    	
    	RegistryPageContributor page;
    	
    	/**
    	 * Create a new category node on the given reader for
    	 * the property page.
    	 * @param reader
    	 * @param propertyPage
    	 */
    	PropertyCategoryNode(CategorizedPageRegistryReader reader, RegistryPageContributor propertyPage){
    		super(reader);
    		page = propertyPage;
    	}
    	/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText()
		 */
		String getLabelText() {
			return page.getPageName();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText(java.lang.Object)
		 */
		String getLabelText(Object element) {
			return ((RegistryPageContributor)element).getPageName();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getNode()
		 */
		Object getNode() {
			return page;
		}
    }

    /**
     * The constructor.
     */
    public PropertyPagesRegistryReader(PropertyPageContributorManager manager) {
        this.manager = manager;
    }

    /**
     * Reads static property page specification.
     */
    private void processPageElement(IConfigurationElement element) {
    	String pageId = element.getAttribute(ATT_ID);
        String pageClassName = element.getAttribute(ATT_CLASS);
        String objectClassName = element.getAttribute(ATT_OBJECTCLASS);
        Collection keywordReferences = readKeywordReferences(element); 

        if (pageId == null) {
            logMissingAttribute(element, ATT_ID);
            return;
        }
        if (objectClassName == null) {
            logMissingAttribute(element, ATT_OBJECTCLASS);
            return;
        }
        if (pageClassName == null) {
            logMissingAttribute(element, ATT_CLASS);
            return;
        }

        RegistryPageContributor contributor = new RegistryPageContributor(
                pageId, element, keywordReferences);
        registerContributor(contributor, objectClassName);
    }

    /**
	 * Read the pages for the receiver from element.
	 * @param element
	 * @return Collection the ids of the children
	 */
	private static Collection readKeywordReferences(IConfigurationElement element) {
		IConfigurationElement[] references = element.getChildren(TAG_KEYWORD_REFERENCE);
		HashSet list = new HashSet();
		for (int i = 0; i < references.length; i++) {
			IConfigurationElement page = references[i];
			String id = page.getAttribute(ATT_ID);
			if (id != null)
				list.add(id);
		}

		return list;
	}

	/**
     * Reads the next contribution element.
     * 
     * public for dynamic UI
     */
    public boolean readElement(IConfigurationElement element) {
        if (element.getName().equals(TAG_PAGE)) {
            processPageElement(element);
            readElementChildren(element);
            return true;
        }
        if (element.getName().equals(TAG_FILTER)) {
            return true;
        }

        return false;
    }

    /**
     * Creates object class instance and registers the contributor with the
     * property page manager.
     */
    private void registerContributor(RegistryPageContributor contributor, String objectClassName) {
        manager.registerContributor(contributor, objectClassName);
        pages.add(contributor);
    }

    /**
     *	Reads all occurances of propertyPages extension in the registry.
     */
    public void registerPropertyPages(IExtensionRegistry registry) {
        readRegistry(registry, PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_PROPERTY_PAGES);
        processNodes();
    }    
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#add(java.lang.Object, java.lang.Object)
	 */
	void add(Object parent, Object node) {
	  ((RegistryPageContributor) parent).addSubPage((RegistryPageContributor)node);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#createCategoryNode(org.eclipse.ui.internal.registry.CategorizedPageRegistryReader, java.lang.Object)
	 */
	CategoryNode createCategoryNode(CategorizedPageRegistryReader reader, Object object) {
		return new PropertyCategoryNode(reader,(RegistryPageContributor) object);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.Object, java.lang.String)
	 */
	Object findNode(Object parent, String currentToken) {
		return ((RegistryPageContributor) parent).getChild(currentToken);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.String)
	 */
	Object findNode(String id) {
		Iterator iterator = pages.iterator();
		while(iterator.hasNext()){
			RegistryPageContributor next = (RegistryPageContributor) iterator.next();
			if(next.getPageId().equals(id))
				return next;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getCategory(java.lang.Object)
	 */
	String getCategory(Object node) {
		return ((RegistryPageContributor) node).getCategory();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getFavoriteNodeId()
	 */
	String getFavoriteNodeId() {
		return null;//properties do not support favorites
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getNodes()
	 */
	Collection getNodes() {
		return pages;
	}
}