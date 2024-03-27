/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.StateNode;
import elemental.json.JsonArray;

import java.util.HashSet;
import java.util.Set;

/**
 * Customized data communicator that uses a custom key mapper for preserving
 * keys of selected items when using lazy-loading.
 *
 * @param <TItem>
 *            The type of the item selectable in the combo box
 */
public class ComboBoxDataCommunicator<TItem> extends DataCommunicator<TItem> {

    /**
     * Customized key mapper that does not remove key for an item as long as it
     * is selected.
     *
     * @param <TItem>
     *            The type of the item selectable in the combo box
     */
    protected static class SelectionPreservingKeyMapper<TItem>
            extends KeyMapper<TItem> {

        private final ComboBoxBase<?, TItem, ?> comboBox;

        private final Set<TItem> itemsMarkedForRemoval = new HashSet<>();

        public SelectionPreservingKeyMapper(
                ComboBoxBase<?, TItem, ?> comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public void remove(TItem item) {
            // Do not remove keys for selected items
            if (!comboBox.isSelected(item)) {
                super.remove(item);
            } else {
                // Mark item for removal as soon as it is not selected anymore
                itemsMarkedForRemoval.add(item);
            }
        }

        @Override
        public String key(TItem item) {
            // Unmark item when it becomes active again
            itemsMarkedForRemoval.remove(item);
            return super.key(item);
        }

        public void purgeItems() {
            // Try purging items that we were not able to remove before
            HashSet<TItem> itemsToRemove = new HashSet<>(itemsMarkedForRemoval);
            itemsToRemove.forEach(item -> {
                if (!comboBox.isSelected(item)) {
                    super.remove(item);
                    itemsMarkedForRemoval.remove(item);
                }
            });
        }
    }

    public ComboBoxDataCommunicator(ComboBoxBase<?, TItem, ?> comboBox,
            DataGenerator<TItem> dataGenerator, ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            boolean fetchEnabled) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode,
                fetchEnabled);

        setKeyMapper(new SelectionPreservingKeyMapper<>(comboBox));
    }

    public void notifySelectionChanged() {
        ((SelectionPreservingKeyMapper<TItem>) getKeyMapper()).purgeItems();
    }
}
