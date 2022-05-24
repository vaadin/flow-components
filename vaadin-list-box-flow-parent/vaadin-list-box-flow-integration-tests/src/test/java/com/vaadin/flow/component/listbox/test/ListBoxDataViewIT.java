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

package com.vaadin.flow.component.listbox.test;

import java.util.List;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.CURRENT_ITEM_SPAN;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.DATA_VIEW_UPDATE_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.HAS_NEXT_ITEM_SPAN;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.HAS_PREV_ITEM_SPAN;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_FOR_ADD_TO_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_FOR_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_FOR_FILTER_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_FOR_LIST_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_FOR_SORT_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_SELECTED_IDS_SPAN;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_SELECTION_BY_ID_AND_NAME_UPDATE_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_BOX_SELECTION_BY_ID_UPDATE_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_ADD_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_ADD_FILTER_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_NEXT_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_PREV_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_REMOVE_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_REMOVE_FILTER_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_SET_FILTER_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_SORT_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.LIST_DATA_VIEW_UPDATE_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.MULTI_SELECT_LIST_BOX_SELECTED_IDS_SPAN;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.MULTI_SELECT_LIST_BOX_SELECTION_BY_ID_AND_NAME_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.MULTI_SELECT_LIST_BOX_SELECTION_UPDATE_BUTTON;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.OTHER_LIST_BOX_FOR_ADD_TO_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.OTHER_LIST_BOX_FOR_FILTER_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.OTHER_LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW;
import static com.vaadin.flow.component.listbox.test.ListBoxDataViewPage.OTHER_LIST_BOX_FOR_SORT_DATA_VIEW;

@TestPath("vaadin-list-box/list-box-data-view")
public class ListBoxDataViewIT extends AbstractComponentIT {

    private static final String CHANGED_1 = "changed-1";
    private static final String VAADIN_ITEM = "vaadin-item";
    private static final String FIRST = "first";
    private static final String SECOND = "second";

    @Before
    public void openPage() {
        open();
    }

    @Test
    public void testGenericDataView_refreshSingleItem_onlyReflectChangesOfThatItem() {

        findElement(By.id(DATA_VIEW_UPDATE_BUTTON)).click();

        WebElement listBox = findElement(By.id(LIST_BOX_FOR_DATA_VIEW));
        List<WebElement> items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 2, items.size());

        Assert.assertEquals("First item should be updated to", CHANGED_1,
                items.get(0).getText());

        Assert.assertEquals("Second item should still hold the old value",
                SECOND, items.get(1).getText());
    }

    @Test
    public void testListDataView_refreshSingleItem_onlyReflectChangesOfThatItem() {

        findElement(By.id(LIST_DATA_VIEW_UPDATE_BUTTON)).click();

        WebElement listBox = findElement(By.id(LIST_BOX_FOR_LIST_DATA_VIEW));
        List<WebElement> items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 2, items.size());

        Assert.assertEquals("First item should be updated to", CHANGED_1,
                items.get(0).getText());

        Assert.assertEquals("Second item should still hold the old value",
                SECOND, items.get(1).getText());
    }

    @Test
    public void testListDataView_addItem_shouldAddOneAndOnlyOneItem() {

        WebElement listBox = findElement(By.id(LIST_BOX_FOR_ADD_TO_DATA_VIEW));
        List<WebElement> items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 1, items.size());

        findElement(By.id(LIST_DATA_VIEW_ADD_BUTTON)).click();

        waitForElementPresent(By.tagName(VAADIN_ITEM));

        listBox = findElement(By.id(LIST_BOX_FOR_ADD_TO_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 2, items.size());

        Assert.assertEquals("First item should have the text", FIRST,
                items.get(0).getText());

        Assert.assertEquals("Second item should have the text", SECOND,
                items.get(1).getText());

        listBox = findElement(By.id(OTHER_LIST_BOX_FOR_ADD_TO_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 2, items.size());
    }

    @Test
    public void testListDataView_removeItem_shouldRemoveOneAndOnlyOneItem() {

        WebElement listBox = findElement(
                By.id(LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW));
        List<WebElement> items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 2, items.size());

        findElement(By.id(LIST_DATA_VIEW_REMOVE_BUTTON)).click();

        waitForElementPresent(By.tagName(VAADIN_ITEM));

        listBox = findElement(By.id(LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 1, items.size());

        Assert.assertEquals("First item should have the text", FIRST,
                items.get(0).getText());

        listBox = findElement(By.id(OTHER_LIST_BOX_FOR_REMOVE_FROM_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("Unexpected item count after removing one item "
                + "for second ListBox", 1, items.size());
    }

    @Test
    public void testListDataView_addAndRemoveFilters_shouldProduceCorrectNumberOfItems() {

        WebElement listBox = findElement(By.id(LIST_BOX_FOR_FILTER_DATA_VIEW));
        List<WebElement> items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 10, items.size());

        findElement(By.id(LIST_DATA_VIEW_SET_FILTER_BUTTON)).click();
        waitForElementPresent(By.tagName(VAADIN_ITEM));

        listBox = findElement(By.id(LIST_BOX_FOR_FILTER_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 5, items.size());

        findElement(By.id(LIST_DATA_VIEW_ADD_FILTER_BUTTON)).click();
        waitForElementPresent(By.tagName(VAADIN_ITEM));

        listBox = findElement(By.id(LIST_BOX_FOR_FILTER_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 4, items.size());

        listBox = findElement(By.id(OTHER_LIST_BOX_FOR_FILTER_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("Unexpected filtering for second ListBox", 10,
                items.size());

        findElement(By.id(LIST_DATA_VIEW_REMOVE_FILTER_BUTTON)).click();
        waitForElementPresent(By.tagName(VAADIN_ITEM));

        listBox = findElement(By.id(LIST_BOX_FOR_FILTER_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("ListBox should have items", 10, items.size());
    }

    @Test
    public void testListDataView_nextAndPreviousItem_shouldComplyWithOriginalOrder() {

        WebElement currentItemSpan = findElement(By.id(CURRENT_ITEM_SPAN));
        WebElement hasNextItemSpan = findElement(By.id(HAS_NEXT_ITEM_SPAN));
        WebElement hasPrevItemSpan = findElement(By.id(HAS_PREV_ITEM_SPAN));

        Assert.assertEquals("Current Item's text should be", "second",
                currentItemSpan.getText());
        Assert.assertEquals("HasNextItem's text should be", "true",
                hasNextItemSpan.getText());
        Assert.assertEquals("HasPreviousItem's text should be", "true",
                hasPrevItemSpan.getText());

        findElement(By.id(LIST_DATA_VIEW_NEXT_BUTTON)).click();
        currentItemSpan = findElement(By.id(CURRENT_ITEM_SPAN));
        hasNextItemSpan = findElement(By.id(HAS_NEXT_ITEM_SPAN));
        hasPrevItemSpan = findElement(By.id(HAS_PREV_ITEM_SPAN));

        Assert.assertEquals("Current Item's text should be", "third",
                currentItemSpan.getText());
        Assert.assertEquals("HasNextItem's text should be", "false",
                hasNextItemSpan.getText());
        Assert.assertEquals("HasPreviousItem's text should be", "true",
                hasPrevItemSpan.getText());

        findElement(By.id(LIST_DATA_VIEW_PREV_BUTTON)).click();
        findElement(By.id(LIST_DATA_VIEW_PREV_BUTTON)).click();
        currentItemSpan = findElement(By.id(CURRENT_ITEM_SPAN));
        hasNextItemSpan = findElement(By.id(HAS_NEXT_ITEM_SPAN));
        hasPrevItemSpan = findElement(By.id(HAS_PREV_ITEM_SPAN));

        Assert.assertEquals("Current Item's text should be", "first",
                currentItemSpan.getText());
        Assert.assertEquals("HasNextItem's text should be", "true",
                hasNextItemSpan.getText());
        Assert.assertEquals("HasPreviousItem's text should be", "false",
                hasPrevItemSpan.getText());
    }

    @Test
    public void testListDataView_setSortComparator_shouldSortTheItems() {

        WebElement listBox = findElement(By.id(LIST_BOX_FOR_SORT_DATA_VIEW));
        List<WebElement> items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("first rendered item's text should be", "third",
                items.get(0).getText());
        Assert.assertEquals("second rendered item's text should be", "first",
                items.get(1).getText());
        Assert.assertEquals("third rendered item's text should be", "second",
                items.get(2).getText());

        findElement(By.id(LIST_DATA_VIEW_SORT_BUTTON)).click();
        waitForElementPresent(By.tagName(VAADIN_ITEM));

        listBox = findElement(By.id(LIST_BOX_FOR_SORT_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertEquals("first rendered item's text should be", "first",
                items.get(0).getText());
        Assert.assertEquals("second rendered item's text should be", "second",
                items.get(1).getText());
        Assert.assertEquals("third rendered item's text should be", "third",
                items.get(2).getText());

        listBox = findElement(By.id(OTHER_LIST_BOX_FOR_SORT_DATA_VIEW));
        items = listBox.findElements(By.tagName(VAADIN_ITEM));

        Assert.assertArrayEquals("Unexpected sorting for second ListBox",
                new String[] { "third", "first", "second" },
                items.stream().map(WebElement::getText).toArray());
    }

    @Test
    public void setIdentifierProviderForMultiSelectListBox_setItem_shouldSelectCorrectItemsBasedOnIdentifier() {
        WebElement selectedIdsSpan = findElement(
                By.id(MULTI_SELECT_LIST_BOX_SELECTED_IDS_SPAN));
        Assert.assertEquals("Selected item ids should be", "1, 3",
                selectedIdsSpan.getText());

        findElement(By.id(MULTI_SELECT_LIST_BOX_SELECTION_UPDATE_BUTTON))
                .click();

        selectedIdsSpan = findElement(
                By.id(MULTI_SELECT_LIST_BOX_SELECTED_IDS_SPAN));
        Assert.assertEquals("Selected item ids should be", "2, 4",
                selectedIdsSpan.getText());

        findElement(
                By.id(MULTI_SELECT_LIST_BOX_SELECTION_BY_ID_AND_NAME_BUTTON))
                        .click();

        selectedIdsSpan = findElement(
                By.id(MULTI_SELECT_LIST_BOX_SELECTED_IDS_SPAN));
        Assert.assertEquals("Selected item ids should be", "1, 3",
                selectedIdsSpan.getText());

    }

    @Test
    public void setIdentifierProviderForListBox_setItem_shouldSelectCorrectItemBasedOnIdentifier() {
        WebElement selectedIdsSpan = findElement(
                By.id(LIST_BOX_SELECTED_IDS_SPAN));
        Assert.assertEquals("Selected item id should be", "3",
                selectedIdsSpan.getText());

        findElement(By.id(LIST_BOX_SELECTION_BY_ID_UPDATE_BUTTON)).click();

        selectedIdsSpan = findElement(By.id(LIST_BOX_SELECTED_IDS_SPAN));
        Assert.assertEquals("Selected item ids should be", "2",
                selectedIdsSpan.getText());

        findElement(By.id(LIST_BOX_SELECTION_BY_ID_AND_NAME_UPDATE_BUTTON))
                .click();

        selectedIdsSpan = findElement(By.id(LIST_BOX_SELECTED_IDS_SPAN));
        Assert.assertEquals("Selected item ids should be", "3",
                selectedIdsSpan.getText());
    }
}
