/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton.tests;

import static com.vaadin.flow.component.radiobutton.tests.RadioButtonGroupBindItemsPage.ADD_ITEM_BUTTON;
import static com.vaadin.flow.component.radiobutton.tests.RadioButtonGroupBindItemsPage.ITEM_COUNT_SPAN;
import static com.vaadin.flow.component.radiobutton.tests.RadioButtonGroupBindItemsPage.RADIO_GROUP_ID;
import static com.vaadin.flow.component.radiobutton.tests.RadioButtonGroupBindItemsPage.REMOVE_ITEM_BUTTON;
import static com.vaadin.flow.component.radiobutton.tests.RadioButtonGroupBindItemsPage.SELECTED_VALUE_SPAN;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-radio-button/radio-button-group-bind-items")
public class RadioButtonGroupBindItemsIT extends AbstractComponentIT {

    private RadioButtonGroupElement radioGroup;

    @Before
    public void init() {
        open();
        radioGroup = $(RadioButtonGroupElement.class).id(RADIO_GROUP_ID);
    }

    @Test
    public void bindItems_initialItemsDisplayed() {
        Assert.assertEquals("Initial item count should be 3", "3",
                $("span").id(ITEM_COUNT_SPAN).getText());

        List<String> options = radioGroup.getOptions();
        Assert.assertEquals("Radio group should have 3 options", 3,
                options.size());

        Assert.assertEquals("First option should be 'Option 1'", "Option 1",
                options.get(0));
        Assert.assertEquals("Second option should be 'Option 2'", "Option 2",
                options.get(1));
        Assert.assertEquals("Third option should be 'Option 3'", "Option 3",
                options.get(2));
    }

    @Test
    public void bindItems_addItem_radioGroupUpdated() {
        Assert.assertEquals("Initial radio button count", 3,
                radioGroup.getOptions().size());

        $(TestBenchElement.class).id(ADD_ITEM_BUTTON).click();

        Assert.assertEquals("Item count should be 4", "4",
                $("span").id(ITEM_COUNT_SPAN).getText());

        List<String> options = radioGroup.getOptions();
        Assert.assertEquals("Radio group should have 4 options", 4,
                options.size());

        Assert.assertEquals("New option should be 'Option 4'", "Option 4",
                options.get(3));
    }

    @Test
    public void bindItems_removeItem_radioGroupUpdated() {
        Assert.assertEquals("Initial radio button count", 3,
                radioGroup.getOptions().size());

        $(TestBenchElement.class).id(REMOVE_ITEM_BUTTON).click();

        Assert.assertEquals("Item count should be 2", "2",
                $("span").id(ITEM_COUNT_SPAN).getText());

        List<String> options = radioGroup.getOptions();
        Assert.assertEquals("Radio group should have 2 options", 2,
                options.size());

        Assert.assertEquals("First option still is 'Option 1'", "Option 1",
                options.get(0));
        Assert.assertEquals("Second option still is 'Option 2'", "Option 2",
                options.get(1));
    }

    @Test
    public void bindItems_multipleAdds_radioGroupUpdatesCorrectly() {
        $(TestBenchElement.class).id(ADD_ITEM_BUTTON).click();
        $(TestBenchElement.class).id(ADD_ITEM_BUTTON).click();

        Assert.assertEquals("Item count should be 5", "5",
                $("span").id(ITEM_COUNT_SPAN).getText());

        List<String> options = radioGroup.getOptions();
        Assert.assertEquals("Radio group should have 5 options", 5,
                options.size());

        Assert.assertEquals("Fourth option", "Option 4", options.get(3));
        Assert.assertEquals("Fifth option", "Option 5", options.get(4));
    }

    @Test
    public void bindItems_addThenRemove_radioGroupCorrect() {
        $(TestBenchElement.class).id(ADD_ITEM_BUTTON).click();
        Assert.assertEquals("After add: 4 options", 4,
                radioGroup.getOptions().size());

        $(TestBenchElement.class).id(REMOVE_ITEM_BUTTON).click();
        Assert.assertEquals("After remove: 3 options", 3,
                radioGroup.getOptions().size());

        // Verify original options are still there
        List<String> options = radioGroup.getOptions();
        Assert.assertEquals("Option 1", options.get(0));
        Assert.assertEquals("Option 2", options.get(1));
        Assert.assertEquals("Option 3", options.get(2));
    }

    @Test
    public void bindItems_selectOption_valueUpdated() {
        radioGroup.selectByText("Option 2");

        Assert.assertEquals("Selected value should be 'Option 2'", "Option 2",
                $("span").id(SELECTED_VALUE_SPAN).getText());
    }
}
