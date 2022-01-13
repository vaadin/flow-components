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
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/template-renderer-event-handler")
public class TemplateRendererEventHandlerIT extends AbstractComponentIT {

    @Test
    public void eventHandlersWorkOnReattach() {
        open();

        waitUntil(driver -> $(GridElement.class).first().getRowCount() > 0);

        findElement(By.id("show-hide")).click();
        Assert.assertTrue(!isElementPresent(By.tagName("vaadin-grid")));

        findElement(By.id("show-hide")).click();
        GridElement grid = $(GridElement.class).first();
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTHTDElement cell = grid.getCell(0, 0);
        cell.$("button").first().click();

        List<WebElement> clickedPersons = findElements(
                By.className("clicked-person"));
        Assert.assertEquals(1, clickedPersons.size());
        Assert.assertEquals("John Doe", clickedPersons.get(0).getText());
    }
}
