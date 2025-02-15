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
package com.vaadin.flow.component.select.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-select/")
public class EmptySelectionItemIT extends AbstractSelectIT {

    @Test
    public void testEmptySelectionEnabled_userSelectsEmptySelection_nullValueInEvent() {
        page.toggleEmptySelectionEnabled(true);
        page.setEmptySelectionCaption("empty");

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items",
                getInitialNumberOfItems() + 1, items.size());
        verify.emptySelectionItemInDropDown("empty");

        for (int i = 1; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", i + "",
                    itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + (i - 1),
                    itemElement.getText());
        }

        // initial select doesn't change value, so no event
        verify.userSelectionDoesntFireEvent(0);
        verify.emptySelectionItemSelected();

        selectElement.selectItemByIndex(1);
        verify.selectedItem("Item-0");
        verify.valueChangeEvent("Item-0", "null", true, 0);

        selectElement.selectItemByIndex(0);
        verify.emptySelectionItemSelected();
        verify.valueChangeEvent("null", "Item-0", true, 1);

        selectElement.selectItemByIndex(2);
        verify.selectedItem("Item-1");
        verify.valueChangeEvent("Item-1", "null", true, 2);

        page.toggleEmptySelectionEnabled(false);
        selectElement.selectItemByIndex(0);
        verify.selectedItem("Item-0");
        verify.valueChangeEvent("Item-0", "Item-1", true, 3);
    }

    @Test
    public void testEmptySelectionCaption_customCaptionSet_customValueInDropDown() {
        page.toggleEmptySelectionEnabled(true);
        page.setEmptySelectionCaption("foobar");
        verify.emptySelectionItemInDropDown("foobar");

        page.setEmptySelectionCaption("another");
        verify.emptySelectionItemInDropDown("another");
    }

    @Test
    public void testEmptySelectionEnabled_userHasSelectedEmptySelection_disablingEmptySelectionDoesntFireEvent() {
        page.toggleEmptySelectionEnabled(true);
        page.setEmptySelectionCaption("empty");
        verify.userSelectionDoesntFireEvent(0);
        verify.selectedItem("empty");

        page.toggleEmptySelectionEnabled(false);
        verify.noItemSelected();

        selectElement.selectItemByIndex(0);
        verify.selectedItem("Item-0");
        verify.valueChangeEvent("Item-0", "null", true, 0);
    }

    @Test
    public void testEmptySelectionItem_whenSelected_correctSelectedItemText() {
        page.toggleEmptySelectionEnabled(true);
        selectElement.selectItemByIndex(0);
        verify.noItemSelected();

        page.setPlaceholder("placeholder");
        // the placeholder will be shown at this point
        verify.placeholderSelected("placeholder");

        page.setEmptySelectionCaption("caption");
        verify.selectedItem("caption", "caption");

        page.toggleItemLabelGenerator(true);
        // the selected label should be updated accordingly to replace the
        // caption
        verify.selectedItem("caption", "null-LABEL");

        page.toggleItemLabelGenerator(false);
        verify.selectedItem("caption", "caption");

        page.setEmptySelectionCaption("");
        verify.placeholderSelected("placeholder");
    }

    @Override
    protected int getInitialNumberOfItems() {
        return 5;
    }
}
