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
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.Keys;

@TestPath("combobox-list-data-view-page")
public class ComboBoxListDataViewIT extends AbstractComboBoxIT {

    @Test
    public void comboBoxDataViewReturnsExpectedData() {
        open();
        ComboBoxElement comboBox = getComboBox();

        verifyDataProviderItemCount("Expected initial item count = 250", 250);

        verifyNoPersonsSelected();

        Assert.assertFalse("Item row 0 should not have previous data.",
                isButtonEnabled(SHOW_PREVIOUS_DATA));
        Assert.assertTrue("Item row 0 has next data.",
                isButtonEnabled(SHOW_NEXT_DATA));

        showSelectedPerson();

        verifySelectedPerson(1);

        showNextPerson();

        verifySelectedPerson(2);

        selectItem(5);

        showSelectedPerson();

        verifySelectedPerson(6);

        showNextPerson();

        verifySelectedPerson(7);

        showPreviousPerson();

        verifySelectedPerson(5);

        // Filter by Age (programmatic filter)
        setAgeFilter("50");

        // There are 3 persons with an age = 50
        verifyDataProviderItemCount(
                "Expected size = 3 after applying " + "programmatic filter", 3);

        comboBox.openPopup();
        assertLoadedItemsCount("Should be 3 persons after filtering", 3,
                comboBox);

        // Apply text filter
        comboBox.sendKeys("Person 50");

        // There are only 1 person with an age = 50 and name 'Person 50'
        waitForItems(comboBox, items -> items.size() == 1
                && "Person 50".equals(getItemLabel(items, 0)));

        // Item Count Event should be also triggered with value = 1
        verifyDataProviderItemCount("Expected item count = 1 after applying "
                + "the programmatic filter and text filter", 1);

        // Reset Filters
        resetTextFilter(comboBox);
        resetAgeFilter();

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

        // Sorting
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

    private ComboBoxElement getComboBox() {
        return $(ComboBoxElement.class).first();
    }

    private void verifyDataProviderItemCount(String message,
            int expectedItemCount) {
        Assert.assertEquals(message, String.valueOf(expectedItemCount),
                $("span").id(ITEM_COUNT).getText());
    }
}
