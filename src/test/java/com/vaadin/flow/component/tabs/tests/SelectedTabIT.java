/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.tabs.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("selected-tab")
public class SelectedTabIT extends AbstractComponentIT {

    @Test
    public void verifyTabIsSelected() {
        open();

        findElement(By.id("second")).click();
        findElement(By.id("show-selection")).click();

        List<WebElement> firstSelections = findElements(By.className("first"));
        WebElement firstSelection = firstSelections
                .get(firstSelections.size() - 1);
        Assert.assertEquals("The first tab is selected: false",
                firstSelection.getText());

        List<WebElement> secondSelections = findElements(
                By.className("second"));
        WebElement secondSelection = secondSelections
                .get(secondSelections.size() - 1);
        Assert.assertEquals("The second tab is selected: true",
                secondSelection.getText());
    }

    @Test
    public void selectionEventOnItemsChange() {
        open();

        findElement(By.id("delete")).click();

        WebElement selectionEvent = findElement(By.id("selection-event"));

        Assert.assertEquals("bar", selectionEvent.getText());

        findElement(By.id("add")).click();

        Assert.assertEquals("baz", selectionEvent.getText());
    }

    @Test
    public void selectDisabledTab_selectionIsResetAndRedisabled() {
        open();

        List<TestBenchElement> tabs = $("vaadin-tabs").first().$("vaadin-tab")
                .all();
        TestBenchElement lastTab = tabs.get(tabs.size() - 1);

        getCommandExecutor().executeScript("arguments[0].disabled=false;",
                lastTab);

        lastTab.click();

        WebElement selectionEvent = findElement(By.id("selection-event"));
        Assert.assertEquals("foo", selectionEvent.getText());

        Assert.assertEquals(Boolean.TRUE.toString(),
                lastTab.getAttribute("disabled"));
    }
}
