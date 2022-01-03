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
package com.vaadin.flow.component.combobox.test.template;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * Test for https://github.com/vaadin/vaadin-combo-box-flow/issues/219
 */
@TestPath("vaadin-combo-box/validation-connector")
public class ValidationConnectorErrorIT extends AbstractComponentIT {

    @Test
    public void noClientSideConnectorError() {
        open();

        Assert.assertFalse(isElementPresent(By.className("v-system-error")));
        checkLogsForErrors();

        ComboBoxElement combo = $(ComboBoxElement.class).first();
        combo.openPopup();
        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                combo);
        Assert.assertEquals("1", combo.getProperty("value"));

        checkLogsForErrors();
    }
}
