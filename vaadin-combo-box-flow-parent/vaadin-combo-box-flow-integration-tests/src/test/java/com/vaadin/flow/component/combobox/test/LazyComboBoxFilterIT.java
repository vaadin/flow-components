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

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-combo-box/lazy-combo-box-filter")
public class LazyComboBoxFilterIT extends AbstractComponentIT {

    @Test
    public void lazyComboBoxFilterFirstQuery() {
        open();
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        comboBox.sendKeys("1");
        comboBox.openPopup();

        WebElement query = findElement(By.id("query"));
        Assert.assertTrue(query.getText().contains("Filter: 1"));
        Assert.assertTrue(query.getText().contains("Count: 10"));
    }
}
