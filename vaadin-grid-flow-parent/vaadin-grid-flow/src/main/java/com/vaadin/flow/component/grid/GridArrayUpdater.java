/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.io.Serializable;

import com.vaadin.flow.component.treegrid.TreeGridArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Array update strategy aware class for Grid.
 *
 * @author Vaadin Ltd
 *
 */
public interface GridArrayUpdater extends ArrayUpdater {

    /**
     * Data object for {@link TreeGridArrayUpdater}.
     */
    public static class UpdateQueueData implements Serializable {
        private final Element element;

        private String uniqueKeyProperty;
        private SerializableSupplier<Boolean> hasExpandedItems;

        public UpdateQueueData(Element element, String uniqueKeyProperty) {
            this.element = element;
            this.uniqueKeyProperty = uniqueKeyProperty;
        }

        public Element getElement() {
            return element;
        }

        public String getUniqueKeyProperty() {
            return uniqueKeyProperty;
        }

        public void setUniqueKeyProperty(String uniqueKeyProperty) {
            this.uniqueKeyProperty = uniqueKeyProperty;
        }

        public SerializableSupplier<Boolean> getHasExpandedItems() {
            return hasExpandedItems;
        }

        public void setHasExpandedItems(
                SerializableSupplier<Boolean> hasExpandedItems) {
            this.hasExpandedItems = hasExpandedItems;
        }
    }

    /**
     * Sets {@link UpdateQueueData} for this array updater.
     * 
     * @param data
     *            the new {@link UpdateQueueData} object
     */
    void setUpdateQueueData(UpdateQueueData data);

    /**
     * Gets {@link UpdateQueueData} set for this array updater.
     * 
     * @return the new {@link UpdateQueueData} or null if not set.
     */
    UpdateQueueData getUpdateQueueData();
}
