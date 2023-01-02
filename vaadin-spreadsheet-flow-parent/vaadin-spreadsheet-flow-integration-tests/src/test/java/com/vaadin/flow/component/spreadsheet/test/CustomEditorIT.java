package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@TestPath("vaadin-spreadsheet")
public class CustomEditorIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.CustomEditor);
    }

    @Test
    public void setValueToTextField_valueAppliedToTextField() {
        String sampleText = "text";
        setEditorValue("B2", sampleText, "input");
        clickCell("B3");
        Assert.assertEquals(sampleText, getCellValue("B2"));
        clickCell("B2");
        Assert.assertEquals(sampleText,
                getEditorElement("input").getAttribute("value"));
    }

    @Test
    public void setValueToTextFieldCell_setFormulaInAnotherCell_valueAppliedToToCell() {
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
        boolean sampleBoolean = true;
        Assert.assertEquals(!sampleBoolean,
                Boolean.valueOf(getCellValue("C2")));
        toggleCheckboxValue("C2");
        clickCell("C3");
        Assert.assertEquals(sampleBoolean, Boolean.valueOf(getCellValue("C2")));
    }

    @Test
    public void toggleCheckboxValue_setFormulaInAnotherCell_valueAppliedToToCell() {
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
        LocalDateTime now = LocalDateTime.now();
        String sampleLocalDateTime = now
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        setEditorValue("D2", sampleLocalDateTime, "input");
        clickCell("D3");
        Assert.assertEquals(now.format(DateTimeFormatter.ISO_DATE),
                getCellValue("D2"));
        clickCell("D2");
        Assert.assertEquals(sampleLocalDateTime,
                getEditorElement("input").getAttribute("value"));
    }

    @Test
    public void setValueToDatePickerCell_setFormulaInAnotherCell_valueAppliedToToCell() {
        LocalDateTime now = LocalDateTime.now();
        setEditorValue("D2",
                now.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), "input");
        clickCell("D3");
        setCellValue("D3", "=D2");
        Assert.assertEquals(now.format(DateTimeFormatter.ISO_DATE),
                getCellValue("D3"));
    }

    @Test
    public void setValueToTextArea_valueAppliedToTextArea() {
        String sampleText = "text";
        setEditorValue("E2", sampleText, "textarea");
        clickCell("E3");
        Assert.assertEquals(sampleText, getCellValue("E2"));
        clickCell("E2");
        Assert.assertEquals(sampleText,
                getEditorElement("textarea").getAttribute("value").trim());
    }

    @Test
    public void setValueToTextAreaCell_setFormulaInAnotherCell_valueAppliedToToCell() {
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
        String sampleValue = "30";
        setEditorValue("F2", sampleValue, "input");
        clickCell("F3");
        Assert.assertEquals(sampleValue, getCellValue("F2"));
        clickCell("F2");
        Assert.assertEquals(sampleValue,
                getEditorElement("input").getAttribute("value"));
    }

    @Test
    public void setValueToComboBoxCell_setFormulaInAnotherCell_valueAppliedToToCell() {
        String sampleValue = "30";
        setEditorValue("F2", sampleValue, "input");
        setCellValue("F3", "=F2");
        Assert.assertEquals(sampleValue, getCellValue("F3"));
    }

    private void toggleCheckboxValue(String cellAddress) {
        clickCell(cellAddress);
        getEditorElement("input").click();
    }

    private void setEditorValue(String cellAddress, String value,
            String inputElementSelector) {
        clickCell(cellAddress);
        TestBenchElement input = getEditorElement(inputElementSelector);
        input.click();
        input.clear();
        input.click();
        input.sendKeys(value);
        input.sendKeys(Keys.ENTER);
    }

    private TestBenchElement getEditorElement(String elementSelector) {
        return getSpreadsheet().findElement(By.cssSelector(elementSelector));
    }
}
