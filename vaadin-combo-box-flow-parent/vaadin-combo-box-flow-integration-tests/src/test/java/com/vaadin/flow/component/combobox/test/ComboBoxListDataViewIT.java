/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.combobox.test;

import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.AGE_FILTER;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.ITEM_COUNT;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.ITEM_DATA;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.ITEM_SELECT;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.REMOVE_ITEM;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.REVERSE_SORTING;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.SHOW_ITEM_DATA;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.SHOW_NEXT_DATA;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.SHOW_PREVIOUS_DATA;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

@TestPath("combobox-list-data-view-page")
public class ComboBoxListDataViewIT extends AbstractComboBoxIT {

    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        comboBox = $(ComboBoxElement.class).first();
    }

    @Test
    public void getItemCount_showsInitialItemsCount() {
        verifyDataProviderItemCount("Expected initial item count = 250", 250);
    }

    @Test
    public void getItem_showsItemOnIndex() {
        verifyNoPersonsSelected();

        showSelectedPerson();

        verifySelectedPerson(0);
    }

    @Test
    public void getNextItem_showsNextItemFromSelected() {
        Assert.assertTrue("Next item button should be enabled for first item",
                isButtonEnabled(SHOW_NEXT_DATA));

        showNextPerson();

        verifySelectedPerson(1);

        selectItem(248);

        showSelectedPerson();

        verifySelectedPerson(248);

        showNextPerson();

        verifySelectedPerson(249);

        Assert.assertFalse("Next item button should be disabled for last item",
                isButtonEnabled(SHOW_NEXT_DATA));
    }

    @Test
    public void getPreviousItem_showsNextItem() {
        Assert.assertFalse(
                "Previous item button should be disabled for first item",
                isButtonEnabled(SHOW_PREVIOUS_DATA));

        selectItem(249);

        Assert.assertTrue(
                "Previous item button should be enabled for last item",
                isButtonEnabled(SHOW_PREVIOUS_DATA));

        showPreviousPerson();

        verifySelectedPerson(248);
    }

    @Test
    public void setFilter_clientAndServerSideFiltersSet_filtersAppliedToItems() {
        // Filter by Age (programmatic filter)
        setAgeFilter("50");

        // There are 2 persons with an age = 50 (Person 50 and Person 150)
        verifyDataProviderItemCount(
                "Expected size = 2 after applying programmatic filter", 2);

        comboBox.openPopup();
        assertLoadedItemsCount("Should be 2 persons after filtering", 2,
                comboBox);

        // Apply text filter
        comboBox.sendKeys("Person 50");

        // There are only 1 person with an age = 50 and name 'Person 50'
        waitForItems(comboBox, items -> items.size() == 1
                && "Person 50".equals(getItemLabel(items, 0)));

        // No item count change on server side, because the filtered items
        // count are 2 < page size.
        verifyDataProviderItemCount(
                "Expected no item count change on server side", 2);

        // Reset client filter
        resetTextFilter(comboBox);

        // Check there are 2 Persons after resetting the client filter
        waitForItems(comboBox, items -> items.size() == 2);

        // Reset server filter
        resetAgeFilter();

        // Check there are 250 Persons again after resetting all filters
        waitForItems(comboBox, items -> items.size() == 250);
    }

    @Test
    public void addItemCountChangeListener_newItemAdded_itemCountChanged() {
        // Add custom value
        comboBox.sendKeys("Person NEW", Keys.ENTER);
        verifyDataProviderItemCount(
                "Expected item count = 251 after adding a new item", 251);

        comboBox.openPopup();
        verifyDataProviderItemCount(
                "Expected item count = 251 after adding a new item and pop up",
                251);

        // Remove recently added item
        selectItem(250);
        removeItem();

        comboBox.openPopup();
        verifyDataProviderItemCount(
                "Expected item count = 250 after removing an item", 250);
    }

    @Test
    public void setSortOrder_itemsSortedByName() {
        reverseSorting();

        selectItem(0);
        showSelectedPerson();

        // Person 99 is the biggest string in terms of native string comparison
        verifySelectedPerson(99);
    }

    private void showSelectedPerson() {
        clickButton(SHOW_ITEM_DATA);
    }

    private void showNextPerson() {
        clickButton(SHOW_NEXT_DATA);
    }

    private void showPreviousPerson() {
        clickButton(SHOW_PREVIOUS_DATA);
    }

    private void removeItem() {
        clickButton(REMOVE_ITEM);
    }

    private void reverseSorting() {
        clickButton(REVERSE_SORTING);
    }

    private void verifySelectedPerson(int personIndex) {
        Assert.assertEquals("Item: Person " + personIndex,
                $("span").id(ITEM_DATA).getText());
    }

    private void setTextFilter(ComboBoxElement comboBox, String filter) {
        comboBox.setFilter(filter);
    }

    private void resetTextFilter(ComboBoxElement comboBox) {
        setTextFilter(comboBox, "");
    }

    private void selectItem(int index) {
        $(IntegerFieldElement.class).id(ITEM_SELECT)
                .setValue(String.valueOf(index));
    }

    private void setAgeFilter(String filter) {
        $(IntegerFieldElement.class).id(AGE_FILTER).setValue(filter);
    }

    private void resetAgeFilter() {
        setAgeFilter("");
    }

    private void verifyNoPersonsSelected() {
        Assert.assertEquals("Initial selection should be 0", "0",
                $(IntegerFieldElement.class).id(ITEM_SELECT).getValue());
    }

    private void verifyDataProviderItemCount(String message,
            int expectedItemCount) {
        Assert.assertEquals(message, String.valueOf(expectedItemCount),
                $("span").id(ITEM_COUNT).getText());
    }
}
