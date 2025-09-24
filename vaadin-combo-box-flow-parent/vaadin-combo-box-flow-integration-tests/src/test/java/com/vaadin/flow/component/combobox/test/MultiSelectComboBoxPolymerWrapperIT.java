/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/polymer-wrapper")
public class MultiSelectComboBoxPolymerWrapperIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        TestBenchElement polymerWrapper = $(
                "multi-select-combo-box-polymer-wrapper").waitForFirst();
        comboBox = polymerWrapper.$(MultiSelectComboBoxElement.class).first();
    }

    @Test
    public void addWithPolymerTemplate_noErrors() {
        checkLogsForErrors();
    }

    // Basic smoke test to verify that connector works correctly
    @Test
    public void openPopup_showsAllItems() {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(100, options.size());
        Assert.assertTrue(
                options.containsAll(Set.of("Item 1", "Item 10", "Item 20")));
    }
}
