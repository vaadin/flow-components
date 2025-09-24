/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.component.gridpro.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-pro")
public class BasicIT extends AbstractComponentIT {

    private GridProElement grid, beanGrid;

    @Before
    public void init() {
        open();
        grid = $(GridProElement.class).waitForFirst();
        beanGrid = $(GridProElement.class).get(1);
    }

    @Test
    public void editColumnsAdded() {
        List<TestBenchElement> columns = grid.$("vaadin-grid-pro-edit-column")
                .all();
        Assert.assertEquals(columns.size(), 6);
    }

    @Test
    public void columnIsRenderedInBeanGrid() {
        GridTHTDElement cell = beanGrid.getCell(0, 0);
        Assert.assertEquals("23", cell.getInnerHTML());
        assertCellEnterEditModeOnDoubleClick(0, 0,
                "vaadin-grid-pro-edit-text-field", beanGrid, true);
    }

    @Test
    public void headerSorterIsRendered() {
        GridTHTDElement headerCell = grid.getHeaderCell(1);
        Assert.assertTrue(headerCell.$("vaadin-grid-sorter").exists());
    }

    @Test
    public void headerSorterCanBeToggled() {
        GridTHTDElement headerCell = grid.getHeaderCell(1);
        GridTHTDElement bodyCell = grid.getCell(0, 1);

        TestBenchElement sorter = headerCell.$("vaadin-grid-sorter").first();
        Assert.assertEquals("Person 1", bodyCell.getInnerHTML());
        sorter.click();
        sorter.click();
        Assert.assertEquals("Person 99", bodyCell.getInnerHTML());
    }

    @Test
    public void canBeSortedAfterEditing() {
        GridTHTDElement headerCell = grid.getHeaderCell(1);
        GridTHTDElement bodyCell = grid.getCell(0, 1);

        TestBenchElement sorter = headerCell.$("vaadin-grid-sorter").first();

        assertCellEnterEditModeOnDoubleClick(1, 1,
                "vaadin-grid-pro-edit-text-field");
        TestBenchElement textField = grid.getCell(1, 1)
                .$("vaadin-grid-pro-edit-text-field").first();

        textField.setProperty("value", "Person 999");
        executeScript("arguments[0].blur();", textField);
        sorter.click();
        sorter.click();
        Assert.assertEquals("Person 999", bodyCell.getInnerHTML());
    }

    @Test
    public void customRepresentationIsRendered() {
        GridTHTDElement cell = grid.getCell(0, 3);
        Assert.assertEquals("No", cell.$("span").first().getText());
    }

    @Test
    public void customRepresentationIsEdited() {
        GridTHTDElement cell = grid.getCell(0, 3);
        Assert.assertEquals("No", cell.$("span").first().getText());

        assertCellEnterEditModeOnDoubleClick(0, 3,
                "vaadin-grid-pro-edit-checkbox");
        TestBenchElement checkbox = cell.$("vaadin-grid-pro-edit-checkbox")
                .first();
        checkbox.click();
        executeScript("arguments[0].blur();", checkbox);

        waitUntil(driver -> cell.$("span").exists());
        Assert.assertEquals("Yes", cell.$("span").first().getText());
    }

    @Test
    public void customComboBox_circularReferencesInData_isEdited() {
        GridTHTDElement cell = grid.getCell(0, 4);
        Assert.assertEquals("City 1", cell.$("span").first().getText());

        assertCellEnterEditModeOnDoubleClick(0, 4, "vaadin-combo-box");

        waitUntil(e -> cell.$("vaadin-combo-box").exists());
        TestBenchElement comboBox = cell.$("vaadin-combo-box").first();
        comboBox.getCommandExecutor().executeScript("arguments[0].open()",
                comboBox);
        waitUntil(driver -> {
            List comboItems = (List<?>) getCommandExecutor().executeScript(
                    "return arguments[0].filteredItems;", comboBox);

            return comboItems.size() > 0;
        });
        comboBox.setProperty("value", "2");
        executeScript("arguments[0].blur();", comboBox);

        waitUntil(driver -> cell.$("span").exists());
        Assert.assertEquals("City 2", cell.$("span").first().getText());

        Assert.assertEquals("City{id=2, name='City 2', person='Person 1'}",
                getPanelText("prop-panel"));
    }

    private String LOADING_EDITOR_ATTRIBUTE = "loading-editor";

    @Test
    public void customComboBox_loadingEditorStateOnEdit() {
        var cell = grid.getCell(0, 4);

        var hasLoadingStateOnEditStart = (Boolean) executeScript(
                """
                            const [cell, grid, attribute] = arguments;

                            // Enter edit mode with double click
                            cell.dispatchEvent(new CustomEvent('dblclick', {composed: true, bubbles: true}));

                            return grid.hasAttribute(attribute);
                        """,
                cell, grid, LOADING_EDITOR_ATTRIBUTE);
        // Expect the editor to be hidden when the edit starts
        Assert.assertTrue(hasLoadingStateOnEditStart);

        // After the round trip to the server...
        var editor = cell.$("vaadin-combo-box").first();
        // The editor should be visible
        Assert.assertFalse(grid.hasAttribute(LOADING_EDITOR_ATTRIBUTE));
        // The editor should have focus
        Assert.assertTrue("Editor should have focus",
                (Boolean) executeScript(
                        "return arguments[0].contains(document.activeElement)",
                        editor));
    }

    @Test
    public void customComboBox_loadingEditorStateClearedOnEditStop() {
        var cell = grid.getCell(0, 4);
        var nonCustomEditorCell = grid.getCell(0, 1);

        var hasLoadingStateAttribute = (Boolean) executeScript(
                """
                            const [cell, nonCustomEditorCell, grid, attribute] = arguments;

                            // Enter edit mode with double click
                            cell.dispatchEvent(new CustomEvent('dblclick', {composed: true, bubbles: true}));
                            await new Promise(resolve => requestAnimationFrame(resolve));

                            // Focus another cell
                            nonCustomEditorCell.focus();
                            await new Promise(resolve => requestAnimationFrame(resolve));

                            return grid.hasAttribute(attribute);

                        """,
                cell, nonCustomEditorCell, grid, LOADING_EDITOR_ATTRIBUTE);

        Assert.assertFalse(hasLoadingStateAttribute);
    }

    @Test
    public void textEditorIsUsedForTextColumn() {
        assertCellEnterEditModeOnDoubleClick(0, 1,
                "vaadin-grid-pro-edit-text-field");
    }

    @Test
    public void cellEditStartedListenerCalledOnce() {
        assertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-combo-box");
        String eventsPanelText = getPanelText("events-panel");
        Assert.assertEquals(1,
                eventsPanelText.split("CellEditStarted").length - 1);
        Assert.assertTrue(eventsPanelText
                .contains("Person{id=1, age=23, name='Person 1', "
                        + "isSubscriber=false, email='person1@vaadin.com', "
                        + "department=sales, city='City 1', employmentYear=2019}"));
    }

    @Test
    public void itemClickListenerListenerCalledOnce() {
        GridTHTDElement cell = grid.getCell(0, 1);
        cell.click(10, 10);

        String eventsPanelText = getPanelText("events-panel");
        Assert.assertEquals(1, eventsPanelText.split("ItemClicked").length - 1);
        Assert.assertTrue(eventsPanelText
                .contains("Person{id=1, age=23, name='Person 1', "
                        + "isSubscriber=false, email='person1@vaadin.com', "
                        + "department=sales, city='City 1', employmentYear=2019}"));
    }

    @Test
    public void columnUsesFocusButtonMode_itemClickListenerListenerCalledOnce() {
        GridTHTDElement cell = grid.getCell(0, 2);
        cell.click(10, 10);

        String eventsPanelText = getPanelText("events-panel");
        Assert.assertEquals(1, eventsPanelText.split("ItemClicked").length - 1);
        Assert.assertTrue(eventsPanelText
                .contains("Person{id=1, age=23, name='Person 1', "
                        + "isSubscriber=false, email='person1@vaadin.com', "
                        + "department=sales, city='City 1', employmentYear=2019}"));
    }

    @Test
    public void customComboBoxIsUsedForEditColumn() {
        assertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-combo-box");
    }

    @Test
    public void customComboBoxIsGettingValue() {
        GridTHTDElement cell = grid.getCell(0, 2);
        assertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-combo-box");
        TestBenchElement comboBox = cell.$("vaadin-combo-box").first();

        Assert.assertEquals("1", comboBox.getProperty("value"));
    }

    @Test
    public void checkboxEditorIsUsedForCheckboxColumn() {
        assertCellEnterEditModeOnDoubleClick(0, 3,
                "vaadin-grid-pro-edit-checkbox");
    }

    @Test
    public void customTextFieldIsUsedForEditColumn() {
        assertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-text-field",
                beanGrid, true);
    }

    @Test
    public void customTextFieldIsGettingValue() {
        GridTHTDElement cell = beanGrid.getCell(0, 1);
        assertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-text-field",
                beanGrid, true);
        TestBenchElement textField = cell.$("vaadin-text-field").first();

        Assert.assertEquals("Person 1", textField.getProperty("value"));
    }

    @Test
    public void selectEditorIsUsedForSelectColumn() {
        assertCellEnterEditModeOnDoubleClick(0, 2,
                "vaadin-grid-pro-edit-select", beanGrid, true);
    }

    @Test
    public void selectEditorOptionsAreSet() {
        GridTHTDElement cell = beanGrid.getCell(0, 2);
        ArrayList optionsList = cell.getColumn().getOptionsList();
        Assert.assertTrue(optionsList.contains("Services"));
        Assert.assertTrue(optionsList.contains("Marketing"));
        Assert.assertTrue(optionsList.contains("Sales"));
    }

    @Test
    public void disabledGridShouldNotBeActivatedByDoubleClick() {
        $("vaadin-button").id("disable-grid-id").click();
        assertCellEnterEditModeOnDoubleClick(0, 1,
                "vaadin-grid-pro-edit-text-field", grid, false);
    }

    @Test
    public void customEditorValueIsUpdatedByLeavingEditorWithTab() {
        GridTHTDElement cell = grid.getCell(0, 5);
        Assert.assertEquals("person1@vaadin.com", cell.getText());

        assertCellEnterEditModeOnDoubleClick(0, 5, "input");
        TestBenchElement input = cell.$("input").first();
        // Clearing the field before sending keys
        input.setProperty("value", "");
        input.sendKeys("newperson1@vaadin.com");
        input.sendKeys(Keys.TAB);

        Assert.assertEquals("newperson1@vaadin.com", cell.getText());
    }

    @Test
    public void customEditorValueIsUpdatedByLeavingEditorWithEnter() {
        GridTHTDElement cell = grid.getCell(0, 5);
        Assert.assertEquals("person1@vaadin.com", cell.getText());

        assertCellEnterEditModeOnDoubleClick(0, 5, "input");
        TestBenchElement input = cell.$("input").first();
        input.setProperty("value", "");
        input.sendKeys("newperson1@vaadin.com");
        input.sendKeys(Keys.ENTER);

        Assert.assertEquals("newperson1@vaadin.com", cell.getText());
    }

    @Test
    public void customEditorOpened_gridIsScrolled_editorIsClosed() {
        GridTHTDElement cell = grid.getCell(8, 5);
        Assert.assertEquals("person9@vaadin.com", cell.getText());

        assertCellEnterEditModeOnDoubleClick(8, 5, "input");

        // Test to cover the bug where, after scrolling a bit, the component was
        // receiving the focus again and the grid would scroll back to the row
        // with the editor opened
        // https://github.com/vaadin/flow-components/issues/2253
        grid.scrollToRow(10);
        Assert.assertEquals(10, grid.getFirstVisibleRowIndex());

        grid.scrollToRow(30);
        Assert.assertFalse(cell.innerHTMLContains("input"));
    }

    @Test
    public void customTextFieldWithCustomValueProviderIsGettingConvertedValue() {
        GridTHTDElement cell = grid.getCell(0, 6);
        assertCellEnterEditModeOnDoubleClick(0, 6, "vaadin-text-field", grid,
                true);
        TestBenchElement textField = cell.$("vaadin-text-field").first();
        // should have converted integer model value into string editor value
        Assert.assertEquals("2019", textField.getProperty("value"));
    }

    @Test
    public void gridWithCustomEditors_navigateToWithTabKey_textIsSelected() {
        var cell_0_4 = grid.getCell(0, 4);
        assertCellEnterEditModeOnDoubleClick(0, 4, "vaadin-combo-box");

        waitUntil(e -> cell_0_4.$("vaadin-combo-box").exists());
        var input = cell_0_4.$("vaadin-combo-box").first();
        input.sendKeys(Keys.TAB);

        var selectedText = (String) getCommandExecutor()
                .executeScript("return document.getSelection().toString()");
        Assert.assertEquals("person1@vaadin.com", selectedText);

        var cell_0_5 = grid.getCell(0, 5);
        input = cell_0_5.$("input").first();
        input.sendKeys(Keys.TAB);
        selectedText = (String) getCommandExecutor()
                .executeScript("return document.getSelection().toString()");
        Assert.assertEquals("2019", selectedText);
    }

    @Test
    public void customField_startEditing_doNotChangeValue_itemPropertyChangeListenerNotCalled() {
        GridTHTDElement cell = grid.getCell(0, 6);
        assertCellEnterEditModeOnDoubleClick(0, 6, "vaadin-text-field", grid,
                true);

        TestBenchElement input = cell.$("input").first();
        input.sendKeys(Keys.ENTER);

        Assert.assertFalse(
                getPanelText("events-panel").contains("ItemPropertyChanged"));
    }

    @Test
    public void columnWithManualRefresh_updateProperty_propertyUpdatedCorrectly() {
        Assert.assertEquals("Person 1", grid.getCell(0, 1).getInnerHTML());

        assertCellEnterEditModeOnDoubleClick(0, 1,
                "vaadin-grid-pro-edit-text-field");

        var textField = grid.getCell(0, 1).$("vaadin-grid-pro-edit-text-field")
                .first();

        textField.setProperty("value", "Updated Person 1");
        textField.sendKeys(Keys.ENTER);

        Assert.assertEquals("Updated Person 1",
                grid.getCell(0, 1).getInnerHTML());
    }

    private void assertCellEnterEditModeOnDoubleClick(Integer rowIndex,
            Integer colIndex, String editorTag) {
        assertCellEnterEditModeOnDoubleClick(rowIndex, colIndex, editorTag,
                grid, true);
    }

    private void assertCellEnterEditModeOnDoubleClick(Integer rowIndex,
            Integer colIndex, String editorTag, GridProElement grid,
            boolean editingEnabled) {
        GridTHTDElement cell = grid.getCell(rowIndex, colIndex);

        // Not in edit mode initially
        Assert.assertFalse(cell.innerHTMLContains(editorTag));

        // Entering edit mode with double click
        // Workaround(yuriy-fix): doubleClick is not working on IE11
        executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('dblclick', {composed: true, bubbles: true}));",
                cell);
        Assert.assertEquals(editingEnabled, cell.innerHTMLContains(editorTag));
    }

    private String getPanelText(String id) {
        return $("div").onPage().id(id).getText();
    }
}
