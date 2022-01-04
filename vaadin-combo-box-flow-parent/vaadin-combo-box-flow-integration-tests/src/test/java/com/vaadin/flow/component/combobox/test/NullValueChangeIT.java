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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/null-value-change")
public class NullValueChangeIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void setValue_selectionTextShouldBeEmpty() {
        open();
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();

        comboBox.openPopup();

        List<Map<String, ?>> items = (List<Map<String, ?>>) executeScript(
                "return arguments[0].filteredItems", comboBox);

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                comboBox);

        Assert.assertEquals("_inputElementValue must be empty.", "",
                comboBox.getAttribute("_inputElementValue"));
    }

}
