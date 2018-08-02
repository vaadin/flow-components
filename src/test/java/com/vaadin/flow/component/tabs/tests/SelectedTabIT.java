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
}
