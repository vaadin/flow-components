/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
