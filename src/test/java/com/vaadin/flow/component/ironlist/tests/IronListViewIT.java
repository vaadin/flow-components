/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.ironlist.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.TabbedComponentDemoTest;

import elemental.json.JsonArray;

/**
 * Smoke tests for the demo page.
 * <p>
 * Core feature tests are covered at the {@code com.vaadin.ui.iron.list.it}
 * package.
 * 
 * @author Vaadin Ltd.
 */
public class IronListViewIT extends TabbedComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/";
    }

    @Test
    public void stringList() {
        openTabAndCheckForErrors("");
        validateListSize("list-of-strings", 3);
    }

    @Test
    public void stringListWithDataProvider() {
        openTabAndCheckForErrors("");
        validateListSize("list-of-strings-with-dataprovider", 1000);
    }

    @Test
    public void chuckNorrisFacts() {
        openTabAndCheckForErrors("using-templates");
        validateListSize("chuck-norris-facts", 1000);
    }

    @Test
    public void peopleListWithDataProvider() {
        openTabAndCheckForErrors("using-templates");
        validateListSize("list-of-people-with-dataprovider", 50);
    }

    @Test
    public void rankedListWithEventHandling() {
        openTabAndCheckForErrors("using-templates");
        validateListSize("using-events-with-templates", 29);
    }

    private void validateListSize(String listId, int expectedSize) {
        WebElement list = findElement(By.id(listId));
        JsonArray items = IronListIT.getItems(getDriver(), list);
        Assert.assertEquals("There should be " + expectedSize
                + " items in the '" + listId + "' iron-list", expectedSize,
                items.length());
    }

}
