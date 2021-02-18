/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;

public class SelectDuringDataProviderChangeIt extends AbstractComponentIT {

    @Test(expected = NoSuchElementException.class)
    public void removeSortableColumn() {
        open();
        GridElement grid = $(GridElement.class).first();
        ButtonElement button = $(ButtonElement.class).first();
        // Trigger data provider change
        testBench().disableWaitForVaadin();
        button.click();
        // Click the second row
        grid.getRow(1).click();
        // This is not found as selection event wont be triggered
        testBench().enableWaitForVaadin();
        findElement(By.id("ready"));
    }
}
