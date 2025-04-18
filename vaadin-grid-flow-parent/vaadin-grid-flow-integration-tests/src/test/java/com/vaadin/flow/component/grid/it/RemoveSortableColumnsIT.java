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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/remove-sortable-column")
public class RemoveSortableColumnsIT extends AbstractComponentIT {

    @Test
    public void removeSortableColumn_shouldNotGenerateAnException() {
        By buttonLocator = By.id(RemoveSortableColumnPage.ID_SORT_BUTTON);

        open();
        waitUntil(ExpectedConditions.elementToBeClickable(buttonLocator));
        findElement(buttonLocator).click();
        SelectElement select = $(SelectElement.class).first();
        select.selectItemByIndex(2);
        Assert.assertThrows(NoSuchElementException.class,
                () -> findElement(By.id("error-handler-message")));
    }
}
