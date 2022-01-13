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
package com.vaadin.flow.component.tabs.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-tabs/selected-tab")
public class SelectedTabIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void verifyTabIsSelected() {
        findElement(By.id("second")).click();
        findElement(By.id("show-selection")).click();

        List<WebElement> firstSelections = findElements(
                By.className("tab-first"));
        WebElement firstSelection = firstSelections
                .get(firstSelections.size() - 1);
        Assert.assertEquals("The first tab is selected: false",
                firstSelection.getText());

        List<WebElement> secondSelections = findElements(
                By.className("tab-second"));
        WebElement secondSelection = secondSelections
                .get(secondSelections.size() - 1);
        Assert.assertEquals("The second tab is selected: true",
                secondSelection.getText());
    }

    @Test
    public void removeSelectedTabFromServer_changeEventFromServer() {
        findElement(By.id("delete")).click();
        assertSelectionEvent(1, "bar server");
    }

    @Test
    public void selectSecondTab_eventFromClient_deleteFirstTab_noEvent() {
        findElement(By.id("second")).click();
        assertSelectionEvent(1, "bar client");
        findElement(By.id("delete-first")).click();
        assertSelectionEvent(1, "bar client");
    }

    @Test
    public void addTabAsFirst_noEvent() {
        findElement(By.id("add-first")).click();
        assertSelectionEvent(0, null);
    }

    @Test
    public void disabledTab_enableAndClick_noSelectionEvent() {
        List<TestBenchElement> tabs = $("vaadin-tabs").first().$("vaadin-tab")
                .all();
        TestBenchElement lastTab = tabs.get(tabs.size() - 1);

        getCommandExecutor().executeScript("arguments[0].disabled=false;",
                lastTab);

        lastTab.click();

        assertSelectionEvent(0, null);
    }

    @Test // https://github.com/vaadin/vaadin-tabs-flow/issues/69
    public void addTabAsFirstWithElementAPI_selectionIsChanged_eventFromClient() {
        findElement(By.id("add-first-with-element-api")).click();
        assertSelectionEvent(1, "asdf client");
    }

    @Test
    public void testUnselectingAndReselecting() {
        List<TestBenchElement> tabs = $("vaadin-tabs").first().$("vaadin-tab")
                .all();

        clickElementWithJs("unselect");
        assertSelectionEvent(1, "null server");

        tabs.get(0).click();
        assertSelectionEvent(2, "foo client");

        clickElementWithJs("unselect-with-index");
        assertSelectionEvent(3, "null server");

        clickElementWithJs("unselect");
        assertSelectionEvent(3, "null server"); // no event

        clickElementWithJs("set-selected-tab");
        assertSelectionEvent(4, "bar server");
    }

    private void assertSelectionEvent(int amountOfEvents,
            String expectedLatestMessage) {
        List<WebElement> selectionEventMessages = findElements(
                By.className("selection-event"));

        Assert.assertEquals(
                "Unexpected amount of selection events have been fired",
                amountOfEvents, selectionEventMessages.size());

        if (amountOfEvents == 0) {
            return;
        }

        WebElement lastMessage = selectionEventMessages
                .get(selectionEventMessages.size() - 1);

        Assert.assertEquals("Unexpected message for the latest selection event",
                expectedLatestMessage, lastMessage.getText());
    }
}
