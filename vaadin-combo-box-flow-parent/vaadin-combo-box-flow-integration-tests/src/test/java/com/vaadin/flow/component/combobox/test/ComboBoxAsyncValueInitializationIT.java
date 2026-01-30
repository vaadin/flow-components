/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

// Regression test for https://github.com/vaadin/flow-components/issues/8549
@TestPath("vaadin-combo-box/combo-box-async-value-initialization")
public class ComboBoxAsyncValueInitializationIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void initializesWithAsyncValue() {
        var comboBox = $(ComboBoxElement.class).first();

        Assert.assertEquals("Item 1", comboBox.getSelectedText());
    }

    @Test
    public void initializesWithSingleValueChangeEventFromServer() {
        var log = $("div").id("value-log");

        Assert.assertEquals("Value: Item 1, isFromClient: false",
                log.getText());
    }
}
