/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class CustomEditorIT extends AbstractSpreadsheetIT {

    private final String[] editorCellAddresses = { "B2", "C2", "D2", "E2",
            "F2" };

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.CustomEditor);
    }

    @Test
    public void setValueToTextField_valueAppliedToTextField() {
        clickToggleCellVisibleButton();
        String sampleText = "text";
        setEditorValue("B2", sampleText, "input");
        clickCell("B3");
        Assert.assertEquals(sampleText, getCellValue("B2"));
        clickCell("B2");
        Assert.assertEquals(sampleText,
                getEditorElement("input").getDomProperty("value"));
    }

    @Test
    public void setValueToTextFieldCell_setFormulaInAnotherCell_valueAppliedToToCell() {
        clickToggleCellVisibleButton();
        String sampleText = "text";
        setEditorValue("B2", sampleText, "input");
        setCellValue("B3", "=B2");
        Assert.assertEquals(sampleText, getCellValue("B3"));

        int sampleNumber = 42;
        setEditorValue("B2", Integer.toString(sampleNumber), "input");
        setCellValue("B3", "=B2*2");
        Assert.assertEquals(Integer.toString(sampleNumber * 2),
                getCellValue("B3"));
    }

    @Test
    public void toggleCheckboxValue_valueAppliedToCheck() {
        clickToggleCellVisibleButton();
        boolean sampleBoolean = true;
        Assert.assertEquals(!sampleBoolean,
                Boolean.valueOf(getCellValue("C2")));
        toggleCheckboxValue("C2");
        clickCell("C3");
        Assert.assertEquals(sampleBoolean, Boolean.valueOf(getCellValue("C2")));
    }

    @Test
    public void toggleCheckboxValue_setFormulaInAnotherCell_valueAppliedToToCell() {
        clickToggleCellVisibleButton();
        boolean sampleBoolean = true;
        Assert.assertEquals(!sampleBoolean,
                Boolean.valueOf(getCellValue("C2")));
        Assert.assertEquals(!sampleBoolean,
                Boolean.valueOf(getCellValue("C3")));
        toggleCheckboxValue("C2");
        setCellValue("C3", "=C2");
        Assert.assertEquals(sampleBoolean, Boolean.valueOf(getCellValue("C3")));
    }

    @Test
    public void setValueToDatePicker_valueAppliedToDatePicker() {
        clickToggleCellVisibleButton();
        LocalDate date = LocalDate.of(2000, 10, 10);
        String sampleLocalDateTime = date
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        setEditorValue("D2", sampleLocalDateTime, "input");
        clickCell("D3");
        Assert.assertEquals(date.format(DateTimeFormatter.ISO_DATE),
                getCellValue("D2"));
        clickCell("D2");
        Assert.assertEquals(sampleLocalDateTime,
                getEditorElement("input").getDomProperty("value"));
    }

    @Test
    public void setValueToDatePickerCell_setFormulaInAnotherCell_valueAppliedToToCell() {
        clickToggleCellVisibleButton();
        LocalDate date = LocalDate.of(2000, 10, 10);
        setEditorValue("D2",
                date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                "input");
        clickCell("D3");
        setCellValue("D3", "=D2");
        Assert.assertEquals(date.format(DateTimeFormatter.ISO_DATE),
                getCellValue("D3"));
    }

    @Test
    public void setValueToTextArea_valueAppliedToTextArea() {
        clickToggleCellVisibleButton();
        String sampleText = "text";
        setEditorValue("E2", sampleText, "textarea");
        clickCell("E3");
        Assert.assertEquals(sampleText, getCellValue("E2"));
        clickCell("E2");
        Assert.assertEquals(sampleText,
                getEditorElement("textarea").getDomProperty("value").trim());
    }

    @Test
    public void setValueToTextAreaCell_setFormulaInAnotherCell_valueAppliedToToCell() {
        clickToggleCellVisibleButton();
        String sampleText = "text";
        setEditorValue("E2", sampleText, "textarea");
        setCellValue("E3", "=E2");
        Assert.assertEquals(sampleText, getCellValue("E3"));

        int sampleNumber = 42;
        setEditorValue("E2", Integer.toString(sampleNumber), "textarea");
        setCellValue("E3", "=E2*2");
        Assert.assertEquals(Integer.toString(sampleNumber * 2),
                getCellValue("E3"));
    }

    @Test
    public void setValueToComboBox_valueAppliedToComboBox() {
        clickToggleCellVisibleButton();
        String sampleValue = "30";
        setEditorValue("F2", sampleValue, "input");
        clickCell("F3");
        Assert.assertEquals(sampleValue, getCellValue("F2"));
        clickCell("F2");
        Assert.assertEquals(sampleValue,
                getEditorElement("input").getDomProperty("value"));
    }

    @Test
    public void setValueToComboBoxCell_setFormulaInAnotherCell_valueAppliedToToCell() {
        clickToggleCellVisibleButton();
        String sampleValue = "30";
        setEditorValue("F2", sampleValue, "input");
        setCellValue("F3", "=F2");
        Assert.assertEquals(sampleValue, getCellValue("F3"));
    }

    @Test
    public void editorFocused_tabKeyPressed_nextCellFocused() {
        clickToggleCellVisibleButton();

        clickCell("F2");
        TestBenchElement editor = getEditorElement("input");
        editor.focus();
        editor.sendKeys(Keys.TAB);
        Assert.assertTrue(getSpreadsheet().getCellAt("G2").isCellSelected());
    }

    @Test
    public void editorFocused_shiftTabKeyPressed_previousCellFocused() {
        clickToggleCellVisibleButton();

        clickCell("B2");
        TestBenchElement editor = getEditorElement("input");
        editor.focus();
        editor.sendKeys(Keys.SHIFT, Keys.TAB);
        Assert.assertTrue(getSpreadsheet().getCellAt("A2").isCellSelected());
    }

    @Test
    public void cellWithEditor_F2Pressed_editorFocused() {
        clickToggleCellVisibleButton();
        selectCell("A2");
        getSpreadsheet().sendKeys(Keys.TAB);
        getSpreadsheet().sendKeys(Keys.F2);
        assertEditorInCellIsFocused("B2");
    }

    @Test
    public void cellWithEditor_enterPressed_editorFocused() {
        clickToggleCellVisibleButton();
        selectCell("A2");
        getSpreadsheet().sendKeys(Keys.TAB);
        getSpreadsheet().sendKeys(Keys.ENTER);
        assertEditorInCellIsFocused("B2");
    }

    @Test
    public void cellWithEditor_charPressed_editorFocused() {
        clickToggleCellVisibleButton();
        selectCell("A2");
        getSpreadsheet().sendKeys(Keys.TAB);
        getSpreadsheet().sendKeys("a");

        var input = getInputInCustomEditorFromCell("B2");
        Assert.assertTrue(input.isPresent());
        var inputElement = input.get();

        Assert.assertTrue(inputElement.isFocused());
        Assert.assertEquals("a", inputElement.getDomProperty("value"));
    }

    @Test
    public void focusedCustomEditor_ESCPressed_cellIsFocused() {
        clickToggleCellVisibleButton();
        selectCell("B2");

        var input = getInputInCustomEditorFromCell("B2");
        Assert.assertTrue(input.isPresent());
        var inputElement = input.get();

        inputElement.focus();
        inputElement.sendKeys(Keys.ESCAPE);

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
        waitUntil(driver -> !inputElement.isFocused());
    }

    @Test
    public void showCustomEditorOnFocus_editorsNotVisible() {
        clickToggleCellVisibleButton();

        // Check that the editors are visible
        for (String cellAddress : editorCellAddresses) {
            Assert.assertFalse(
                    getCustomEditorFromCell(cellAddress).isPresent());
        }

        clickToggleCellVisibleButton();

        // Check that the editors are not visible
        for (String cellAddress : editorCellAddresses) {
            Assert.assertTrue(getCustomEditorFromCell(cellAddress).isPresent());
        }

        clickToggleCellVisibleButton();

        // Check that the editors are visible again
        for (String cellAddress : editorCellAddresses) {
            Assert.assertFalse(
                    getCustomEditorFromCell(cellAddress).isPresent());
        }
    }

    @Test
    public void customEditorVisible_pressEnterKey_editorIsFocused() {
        clickToggleCellVisibleButton();
        selectCell("A2");

        for (String cellAddress : editorCellAddresses) {
            moveToNextCellAndAssertEditorInCellIsFocusedWithKeyPress(
                    cellAddress, Keys.ENTER);
        }
    }

    @Test
    public void customEditorVisible_pressF2Key_editorIsFocused() {
        clickToggleCellVisibleButton();
        selectCell("A2");

        for (String cellAddress : editorCellAddresses) {
            moveToNextCellAndAssertEditorInCellIsFocusedWithKeyPress(
                    cellAddress, Keys.F2);
        }
    }

    @Test
    public void customEditorVisible_pressCharKey_editorIsFocused() {
        clickToggleCellVisibleButton();
        selectCell("A2");

        for (String cellAddress : editorCellAddresses) {
            // Skip the C2 cell, as it contains a checkbox editor
            if (cellAddress.equals("C2")) {
                getActiveElement().sendKeys(Keys.TAB);
                continue;
            }
            moveToNextCellAndAssertEditorInCellIsFocusedWithKeyPress(
                    cellAddress, "a");
            var value = getActiveElement().getDomProperty("value");
            Assert.assertEquals("a", value);
        }
    }

    @Test
    public void customEditorVisible_editorIsFocused_ESCKeyFocusesCell() {
        clickToggleCellVisibleButton();
        selectCell("A2");

        for (String cellAddress : editorCellAddresses) {
            moveToNextCellAndAssertEditorInCellIsFocusedWithKeyPress(
                    cellAddress, Keys.ENTER);
            getActiveElement().sendKeys(Keys.ESCAPE);
            Assert.assertTrue(
                    getSpreadsheet().getCellAt(cellAddress).isCellSelected());
            // Checks that the editor is not removed when focus is moved away
            // from it
            var editor = getCustomEditorFromCell(cellAddress);
            Assert.assertTrue(editor.isPresent());
        }
    }

    @Test
    public void customEditorVisible_editorIsFocused_editorStaysOnEnter() {
        clickToggleCellVisibleButton();
        selectCell("A2");

        for (String cellAddress : editorCellAddresses) {
            moveToNextCellAndAssertEditorInCellIsFocusedWithKeyPress(
                    cellAddress, Keys.ENTER);
            getActiveElement().sendKeys(Keys.ENTER);
            // Checks that the editor is not removed when the user presses ENTER
            var editor = getCustomEditorFromCell(cellAddress);
            Assert.assertTrue(editor.isPresent());
        }
    }

    @Test
    public void customEditorIsVisible_sheetIsChanged_editorsRemoved() {
        getSpreadsheet().addSheet();

        // Check that the editors are not visible in the new sheet
        for (String cellAddress : editorCellAddresses) {
            Assert.assertFalse(
                    getCustomEditorFromCell(cellAddress).isPresent());
        }

        getSpreadsheet().selectSheet("Sheet1");

        // Check that the editors are visible when moving back to the first
        // sheet
        for (String cellAddress : editorCellAddresses) {
            Assert.assertTrue(getCustomEditorFromCell(cellAddress).isPresent());
        }
    }

    private void toggleCheckboxValue(String cellAddress) {
        clickCell(cellAddress);
        getEditorElement("input").click();
    }

    private void setEditorValue(String cellAddress, String value,
            String inputElementSelector) {
        clickCell(cellAddress);
        var inputOptional = getInputInCustomEditorFromCell(cellAddress,
                inputElementSelector);
        Assert.assertTrue(inputOptional.isPresent());
        var input = inputOptional.get();
        input.click();
        input.clear();
        input.click();
        input.sendKeys(value);
        input.sendKeys(Keys.ENTER);
        input.sendKeys(Keys.ESCAPE);
        waitUntil(driver -> !input.isFocused());
    }

    private TestBenchElement getEditorElement(String elementSelector) {
        return getSpreadsheet().findElement(By.cssSelector(elementSelector));
    }

    private Optional<TestBenchElement> getCustomEditorFromCell(
            String cellAddress) {
        var cell = getSpreadsheet().getCellAt(cellAddress);
        try {
            var slot = cell.findElement(By.tagName("slot"));
            var slotName = slot.getDomAttribute("name");
            var editor = getSpreadsheet()
                    .findElement(By.cssSelector("[slot='" + slotName + "']"));

            return Optional.of(editor);
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    private Optional<TestBenchElement> getInputInCustomEditorFromCell(
            String cellAddress) {
        return getInputInCustomEditorFromCell(cellAddress, "input");
    }

    private Optional<TestBenchElement> getInputInCustomEditorFromCell(
            String cellAddress, String inputElementSelector) {
        var editor = getCustomEditorFromCell(cellAddress);
        try {
            return editor.map(
                    e -> e.findElement(By.cssSelector(inputElementSelector)));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    private void moveToNextCellAndAssertEditorInCellIsFocusedWithKeyPress(
            String cellAddress, CharSequence key) {
        getActiveElement().sendKeys(Keys.TAB);
        getSpreadsheet().sendKeys(key);
        assertEditorInCellIsFocused(cellAddress);
    }

    private void assertEditorInCellIsFocused(String cellAddress) {
        var activeElement = getActiveElement();
        String slotName = activeElement.getDomAttribute("slot");
        if (!(slotName != null && slotName.startsWith("custom-editor"))) {
            var parentElement = getActiveElement().findElement(By.xpath(".."));
            slotName = parentElement.getDomAttribute("slot");
        }

        Assert.assertNotNull("Slot name is null", slotName);

        var result = getSpreadsheet().getCellAt(cellAddress)
                .findElements(By.cssSelector("slot[name='" + slotName + "']"));
        Assert.assertEquals(1, result.size());
    }

    private WebElement getActiveElement() {
        return getDriver().switchTo().activeElement();
    }

    private void clickToggleCellVisibleButton() {
        waitUntil(driver -> {
            var button = $(TestBenchElement.class)
                    .id("toggleCustomEditorVisibilityButton");
            return button.isDisplayed();
        });
        var toggleButton = $(TestBenchElement.class)
                .id("toggleCustomEditorVisibilityButton");
        toggleButton.click();
    }
}
