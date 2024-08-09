/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectionPreservingKeyMapperTest {

    private ComboBox<String> comboBox;
    private ComboBoxDataCommunicator.SelectionPreservingKeyMapper<String> keyMapper;

    @Before
    public void setup() {
        comboBox = new ComboBox<>();
        comboBox.setItems("1", "2", "3", "4", "5");
        keyMapper = new ComboBoxDataCommunicator.SelectionPreservingKeyMapper<>(
                comboBox);
    }

    @Test
    public void remove_doesNotRemoveSelectedItem() {
        // Generate some keys
        keyMapper.key("1");
        keyMapper.key("2");
        keyMapper.key("3");
        // Select "2"
        comboBox.setValue("2");
        // Request to remove items
        keyMapper.remove("1");
        keyMapper.remove("2");
        keyMapper.remove("3");
        // All items except "2" have been removed
        Assert.assertFalse(keyMapper.has("1"));
        Assert.assertTrue(keyMapper.has("2"));
        Assert.assertFalse(keyMapper.has("3"));
    }

    @Test
    public void key_preventsItemFromBeingPurged() {
        // Generate key for "2"
        keyMapper.key("2");
        // Select "2"
        comboBox.setValue("2");
        // Request to remove "2"
        // As item is selected it should now be marked for removal
        keyMapper.remove("2");
        // Generate key again, which means "2" is in use ("active") again
        keyMapper.key("2");
        // Purge
        keyMapper.purgeItems();
        // "2" should not have been purged as is has been activated again
        Assert.assertTrue(keyMapper.has("2"));
    }

    @Test
    public void purgeItems_doesNotRemoveSelectedItem() {
        // Generate key for "2"
        keyMapper.key("2");
        // Select "2"
        comboBox.setValue("2");
        // Request to remove "2"
        // As item is selected it should now be marked for removal
        keyMapper.remove("2");
        // Purge
        keyMapper.purgeItems();
        // "2" should not have been purged as it is still selected
        Assert.assertTrue(keyMapper.has("2"));
    }

    @Test
    public void purgeItems_removesUnselectedItem() {
        // Generate key for "2"
        keyMapper.key("2");
        // Select "2"
        comboBox.setValue("2");
        // Request to remove "2"
        // As item is selected it should now be marked for removal
        keyMapper.remove("2");
        // Select different item
        comboBox.setValue("1");
        // Purge
        keyMapper.purgeItems();
        // "2" should have been purged, as it is no longer selected
        Assert.assertFalse(keyMapper.has("2"));
    }
}
