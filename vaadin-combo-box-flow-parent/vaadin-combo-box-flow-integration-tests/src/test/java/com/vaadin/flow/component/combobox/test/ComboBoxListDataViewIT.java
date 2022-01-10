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

package com.vaadin.flow.component.combobox.test;

import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.AGE_FILTER;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.FIRST_COMBO_BOX_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.ITEM_COUNT;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.ITEM_DATA;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.ITEM_SELECT;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.NEW_PERSON_NAME;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.REMOVE_ITEM;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.REVERSE_SORTING;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.SECOND_COMBO_BOX_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.SHOW_ITEMS;
import static com.vaadin.flow.component.combobox.test.ComboBoxListDataViewPage.SHOW_ITEM_COUNT;
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

@TestPath("vaadin-combo-box/combobox-list-data-view-page")
public class ComboBoxListDataViewIT extends AbstractComboBoxIT {

    private ComboBoxElement firstComboBox;
    private ComboBoxElement secondComboBox;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        firstComboBox = $(ComboBoxElement.class).id(FIRST_COMBO_BOX_ID);
        secondComboBox = $(ComboBoxElement.class).id(SECOND_COMBO_BOX_ID);
    }

    @Test
    public void getItemCount_showsInitialItemsCount() {
        verifyNotifiedItemCount("Expected initial item count = 250", 250);
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
        verifyNotifiedItemCount(
                "Expected size = 2 after applying programmatic filter", 2);

        firstComboBox.openPopup();
        assertLoadedItemsCount("Should be 2 persons after filtering", 2,
                firstComboBox);

        // Verify that the second combo box has not been impacted by filtering
        Assert.assertEquals(
                "Second combo box should not be impacted by "
                        + "the programmatic filter applied to first combo box",
                250, getItems(secondComboBox).size());

        // Apply text filter
        firstComboBox.sendKeys("Person 50");

        // There are only 1 person with an age = 50 and name 'Person 50'
        waitForItems(firstComboBox, items -> items.size() == 1
                && "Person 50".equals(getItemLabel(items, 0)));

        // No item count change on server side, because the filtered items
        // count are 2 < page size.
        verifyNotifiedItemCount("Expected no item count change on server side",
                2);

        // Reset client filter
        resetTextFilter(firstComboBox);

        // Check there are 2 Persons after resetting the client filter
        waitForItems(firstComboBox, items -> items.size() == 2);

        // Reset server filter
        resetAgeFilter();

        // Check there are 250 Persons again after resetting all filters
        waitForItems(firstComboBox, items -> items.size() == 250);
    }

    @Test
    public void addItemCountChangeListener_newItemAdded_itemCountChanged() {
        // Add custom value
        firstComboBox.sendKeys(NEW_PERSON_NAME, Keys.ENTER);
        verifyNotifiedItemCount(
                "Expected item count = 251 after adding a new item", 251);
        // Erase input field's text, because it can be treated as a filter
        firstComboBox.selectByText("");

        firstComboBox.openPopup();
        verifyNotifiedItemCount(
                "Expected item count = 251 after adding a new item and pop up",
                251);

        Assert.assertEquals(
                "A new Person is expected to be added to a second combo box also",
                251, getItems(secondComboBox).size());

        // Remove recently added item
        firstComboBox.selectByText(NEW_PERSON_NAME);
        removeItem();

        firstComboBox.openPopup();
        verifyNotifiedItemCount(
                "Expected item count = 250 after removing an item", 250);

        Assert.assertEquals(
                "The last Person is expected to be removed from a second "
                        + "combo box also",
                250, getItems(secondComboBox).size());
    }

    @Test
    public void setSortOrder_itemsSortedByName() {
        reverseSorting();

        selectItem(0);
        showSelectedPerson();

        // Person 99 is the biggest string in terms of native string comparison
        verifySelectedPerson(99);

        // Verify that the second combo box has not impacted by the sorting
        assertItem(getItems(secondComboBox), 0, "Person 0 lastName");
    }

    @Test
    public void getItemCount_withClientSideFilter_returnsItemFromNotFilteredSet() {
        firstComboBox.setFilter("222");

        waitForItems(firstComboBox, items -> items.size() == 1
                && "Person 222".equals(getItemLabel(items, 0)));

        clickButton(SHOW_ITEM_COUNT);
        Assert.assertEquals("The client filter shouldn't impact the item count",
                "250", getItemCount());

        firstComboBox.openPopup();

        waitForItems(firstComboBox,
                items -> items.size() == 250
                        && "Person 0".equals(getItemLabel(items, 0))
                        && "Person 49".equals(getItemLabel(items, 49)));

        clickButton(SHOW_ITEM_COUNT);
        Assert.assertEquals("The client filter shouldn't impact the item count",
                "250", getItemCount());
    }

    @Test
    public void getItem_withClientSideFilter_returnsItemFromNotFilteredSet() {
        firstComboBox.setFilter("222");

        waitForItems(firstComboBox, items -> items.size() == 1
                && "Person 222".equals(getItemLabel(items, 0)));

        selectItem(0);
        showSelectedPerson();
        verifySelectedPerson(0);

        selectItem(249);
        showSelectedPerson();
        verifySelectedPerson(249);

        firstComboBox.openPopup();

        waitForItems(firstComboBox,
                items -> items.size() == 250
                        && "Person 0".equals(getItemLabel(items, 0))
                        && "Person 49".equals(getItemLabel(items, 49)));

        selectItem(0);
        showSelectedPerson();
        verifySelectedPerson(0);

        selectItem(249);
        showSelectedPerson();
        verifySelectedPerson(249);
    }

    @Test
    public void getItems_withClientSideFilter_returnsNotFilteredItems() {
        firstComboBox.setFilter("222");

        waitForItems(firstComboBox, items -> items.size() == 1
                && "Person 222".equals(getItemLabel(items, 0)));

        clickButton(SHOW_ITEMS);

        // Checks the filter has been cleared after closing the drop down
        // ComboBox clears the cache after closing, so the item's values are
        // not checked here
        waitForItems(firstComboBox, items -> items.size() == 250);

        Assert.assertTrue("The client filter shouldn't impact the items",
                getItemData().startsWith("Person 0 lastName,Person 1 lastName")
                        && getItemData().endsWith("Person 249 lastName"));

        firstComboBox.openPopup();

        waitForItems(firstComboBox,
                items -> items.size() == 250
                        && "Person 0".equals(getItemLabel(items, 0))
                        && "Person 49".equals(getItemLabel(items, 49)));

        clickButton(SHOW_ITEMS);

        Assert.assertTrue("The client filter shouldn't impact the items",
                getItemData().startsWith("Person 0 lastName,Person 1 lastName")
                        && getItemData().endsWith("Person 249 lastName"));
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
        Assert.assertEquals("Item: Person " + personIndex, getItemData());
    }

    private String getItemData() {
        return $("span").id(ITEM_DATA).getText();
    }

    private String getItemCount() {
        return $("span").id(ITEM_COUNT).getText();
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

    private void verifyNotifiedItemCount(String message,
            int expectedItemCount) {
        Assert.assertEquals(message, String.valueOf(expectedItemCount),
                $("span").id(ITEM_COUNT).getText());
    }
}
