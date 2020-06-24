package com.vaadin.flow.component.gridpro.test;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.component.gridpro.testbench.GridTHTDElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BasicIT extends AbstractParallelTest {

    private GridProElement grid, beanGrid;

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        grid = $(GridProElement.class).waitForFirst();
        beanGrid = $(GridProElement.class).get(1);
    }

    @Test
    public void editColumnsAdded() {
        List<TestBenchElement> columns = grid.$("vaadin-grid-pro-edit-column").all();
        Assert.assertEquals(columns.size(), 4);
    }

    @Test
    public void columnIsRenderedInBeanGrid() {
        GridTHTDElement cell = beanGrid.getCell(0, 0);
        Assert.assertEquals("23", cell.getInnerHTML());
        AssertCellEnterEditModeOnDoubleClick(0, 0, "vaadin-grid-pro-edit-text-field", beanGrid, true);
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

        AssertCellEnterEditModeOnDoubleClick(1, 1, "vaadin-grid-pro-edit-text-field");
        TestBenchElement textField = grid.getCell(1, 1).$("vaadin-grid-pro-edit-text-field").first();

        textField.setProperty("value", "Person 999");
        textField.dispatchEvent("focusout");
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

        AssertCellEnterEditModeOnDoubleClick(0, 3, "vaadin-grid-pro-edit-checkbox");
        TestBenchElement checkbox = cell.$("vaadin-grid-pro-edit-checkbox").first();
        checkbox.click();
        checkbox.dispatchEvent("focusout");

        waitUntil(driver -> cell.$("span").exists());
        Assert.assertEquals("Yes", cell.$("span").first().getText());
    }

    @Test
    public void customComboBox_circularReferencesInData_isEdited() {
        GridTHTDElement cell = grid.getCell(0, 4);
        Assert.assertEquals("City 1", cell.$("span").first().getText());

        AssertCellEnterEditModeOnDoubleClick(0, 4, "vaadin-combo-box");
        TestBenchElement comboBox = cell.$("vaadin-combo-box").first();
        comboBox.getCommandExecutor().executeScript("arguments[0].open()",
                comboBox);
        waitUntil(driver -> {
            List comboItems = (List<?>) getCommandExecutor()
                    .executeScript("return arguments[0].filteredItems;", comboBox);

            return comboItems.size() > 0;
        });
        comboBox.setProperty("value", "2");
        comboBox.dispatchEvent("focusout");

        waitUntil(driver -> cell.$("span").exists());
        Assert.assertEquals("City 2", cell.$("span").first().getText());

        Assert.assertEquals("City{id=2, name='City 2', person='Person 1'}",
                getPanelText("prop-panel"));
    }

    @Test
    public void textEditorIsUsedForTextColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-grid-pro-edit-text-field");
    }

    @Test
    public void cellEditStartedListenerCalledOnce() {
        AssertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-combo-box");
        Assert.assertEquals("Person{id=1, age=23, name='Person 1', " +
                "isSubscriber=false, email='person1@vaadin.com', " +
                "department=sales, city='City 1'}", getPanelText("events" +
                "-panel"));
    }

    @Test
    public void customComboBoxIsUsedForEditColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-combo-box");
    }

    @Test
    public void customComboBoxIsGettingValue() {
        GridTHTDElement cell = grid.getCell(0, 2);
        AssertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-combo-box");
        TestBenchElement comboBox = cell.$("vaadin-combo-box").first();

        Assert.assertEquals("1", comboBox.getProperty("value"));
    }

    @Test
    public void checkboxEditorIsUsedForCheckboxColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 3, "vaadin-grid-pro-edit-checkbox");
    }

    @Test
    public void customTextFieldIsUsedForEditColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-text-field", beanGrid, true);
    }

    @Test
    public void customTextFieldIsGettingValue() {
        GridTHTDElement cell = beanGrid.getCell(0, 1);
        AssertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-text-field", beanGrid, true);
        TestBenchElement textField = cell.$("vaadin-text-field").first();

        Assert.assertEquals("Person 1", textField.getProperty("value"));
    }

    @Test
    public void selectEditorIsUsedForSelectColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-grid-pro-edit-select", beanGrid, true);
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
        AssertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-grid-pro-edit-text-field", grid, false);
    }

    private void AssertCellEnterEditModeOnDoubleClick(Integer rowIndex, Integer colIndex, String editorTag) {
        AssertCellEnterEditModeOnDoubleClick(rowIndex, colIndex, editorTag, grid, true);
    }

    private void AssertCellEnterEditModeOnDoubleClick(Integer rowIndex, Integer colIndex, String editorTag, GridProElement grid, boolean editingEnabled) {
        GridTHTDElement cell = grid.getCell(rowIndex, colIndex);

        // Not in edit mode initially
        Assert.assertFalse(cell.innerHTMLContains(editorTag));

        // Entering edit mode with double click
        // Workaround(yuriy-fix): doubleClick is not working on IE11
        executeScript("arguments[0].dispatchEvent(new CustomEvent('dblclick', {composed: true, bubbles: true}));", cell);
        Assert.assertEquals(editingEnabled, cell.innerHTMLContains(editorTag));
    }

    private String getPanelText(String id) {
        return $("div").onPage().id(id).getText();
    }
}
