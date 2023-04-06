/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid;

import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater;

/**
 * Array update strategy aware class for TreeGrid.
 *
 * @author Vaadin Ltd
 *
 */
public interface TreeGridArrayUpdater
        extends GridArrayUpdater, HierarchicalArrayUpdater {

}
