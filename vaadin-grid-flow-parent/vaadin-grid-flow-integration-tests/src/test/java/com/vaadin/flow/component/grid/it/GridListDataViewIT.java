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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static com.vaadin.flow.component.grid.it.GridListDataViewPage.FIRST_NAME_FILTER;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ITEM_COUNT;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ITEM_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ROW_SELECT;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SHOW_ITEM_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SHOW_NEXT_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SHOW_PREVIOUS_DATA;

@TestPath("gridlistdataviewpage")
public class GridListDataViewIT extends AbstractComponentIT {

    @Test
    public void gridDataViewReturnsExpectedData() {
        open();
        GridElement grid = $(GridElement.class).first();

        Assert.assertEquals("Item count not expected", "250",
                $("span").id(ITEM_COUNT).getText());

        Assert.assertEquals("Initial selection should be 0", "0",
                $(IntegerFieldElement.class).id(ROW_SELECT).getValue());

        Assert.assertFalse("Item row 0 should not have previous data.",
                $(ButtonElement.class).id(SHOW_PREVIOUS_DATA).isEnabled());
        Assert.assertTrue("Item row 0 has next data.",
                $(ButtonElement.class).id(SHOW_NEXT_DATA).isEnabled());

        $(ButtonElement.class).id(SHOW_ITEM_DATA).click();

        Assert.assertEquals("Item: Person 1",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_NEXT_DATA).click();
        Assert.assertEquals("Item: Person 2",
                $("span").id(ITEM_DATA).getText());

        $(IntegerFieldElement.class).id(ROW_SELECT).setValue("5");

        $(ButtonElement.class).id(SHOW_ITEM_DATA).click();
        Assert.assertEquals("Wrong row item", "Item: Person 6",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_NEXT_DATA).click();
        Assert.assertEquals("Wrong next item.", "Item: Person 7",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_PREVIOUS_DATA).click();
        Assert.assertEquals("Wrong previous item.", "Item: Person 5",
                $("span").id(ITEM_DATA).getText());

        // Sort highest first. NOTE! this means that we start with 99
        grid.getHeaderCell(0).$("vaadin-grid-sorter").first().click();
        grid.getHeaderCell(0).$("vaadin-grid-sorter").first().click();

        $(ButtonElement.class).id(SHOW_ITEM_DATA).click();
        Assert.assertEquals("Wrong row item for sorted data", "Item: Person 94",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_NEXT_DATA).click();
        Assert.assertEquals("Wrong next item for sorted data.", "Item: Person 93",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_PREVIOUS_DATA).click();
        Assert.assertEquals("Wrong previous item for sorted data.", "Item: Person 95",
                $("span").id(ITEM_DATA).getText());

        $(TextFieldElement.class).id(FIRST_NAME_FILTER).setValue("9");

        // There are 43 firstnames with a 9 in the set from 1-250
        Assert.assertEquals("Filtered size not as expected", "43",
                $("span").id(ITEM_COUNT).getText());
    }

}
