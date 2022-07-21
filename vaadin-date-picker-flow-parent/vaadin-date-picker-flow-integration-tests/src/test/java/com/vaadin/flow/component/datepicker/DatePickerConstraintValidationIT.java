package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@TestPath("vaadin-date-picker/constraint-validation")
public class DatePickerConstraintValidationIT extends AbstractComponentIT {

    DatePickerElement datePickerElement;

    @Before
    public void init() {
        open();
        datePickerElement = $(DatePickerElement.class).first();
    }

    @Test
    public void setRequired_focusAndBlurField_fieldIsInvalid() {
        clickButton(DatePickerConstraintValidationPage.REQUIRED_BUTTON);
        datePickerElement.focus();
        datePickerElement.sendKeys(Keys.TAB);

        assertClientInvalid();
        assertServerInvalid();
    }

    @Test
    public void setRequired_fillWithValidValue_fieldIsValid() {
        clickButton(DatePickerConstraintValidationPage.REQUIRED_BUTTON);
        datePickerElement.setInputValue("01/01/2022");

        assertClientValid();
        assertServerValid();
    }

    @Test
    public void setRequired_fillAndEmptyValue_fieldIsInvalid() {
        clickButton(DatePickerConstraintValidationPage.REQUIRED_BUTTON);
        datePickerElement.setInputValue("01/01/2022");

        datePickerElement.clear();
        assertClientInvalid();
        assertServerInvalid();
    }

    @Test
    public void setMinDate_enterDateLessThanMin_fieldIsInvalid() {
        clickButton(DatePickerConstraintValidationPage.MIN_DATE_BUTTON);
        String value = dateToString(
                DatePickerConstraintValidationPage.CONSTRAINT_DATE_VALUE
                        .minusDays(1));
        datePickerElement.setInputValue(value);

        assertClientInvalid();
        assertServerInvalid();
    }

    @Test
    public void setMaxDate_enterDateGreaterThanMax_fieldIsInvalid() {
        clickButton(DatePickerConstraintValidationPage.MAX_DATE_BUTTON);
        String value = dateToString(
                DatePickerConstraintValidationPage.CONSTRAINT_DATE_VALUE
                        .plusDays(1));
        datePickerElement.setInputValue(value);

        assertClientInvalid();
        assertServerInvalid();
    }

    @Test
    public void emptyField_invalidDateFormat_fieldIsInvalid() {
        datePickerElement.setInputValue("invalid_format");

        assertClientInvalid();
        assertServerInvalid();
    }

    @Test
    public void fieldInvalid_validDateAdded_fieldIsValid() {
        datePickerElement.setInputValue("invalid_format");
        datePickerElement.setInputValue("01/01/2022");

        assertClientValid();
        assertServerValid();
    }

    private void assertServerInvalid() {
        Assert.assertEquals("Server should be invalid", "true",
                getValidityState());
    }

    private void assertServerValid() {
        Assert.assertEquals("Server should be valid", "false",
                getValidityState());
    }

    private String getValidityState() {
        clickButton(
                DatePickerConstraintValidationPage.SERVER_VALIDITY_STATE_BUTTON);
        return findElement(
                By.id(DatePickerConstraintValidationPage.VALIDITY_STATE))
                        .getText();
    }

    private void assertClientInvalid() {
        Assert.assertTrue("Client should be invalid", isClientInvalid());
    }

    private void assertClientValid() {
        Assert.assertFalse("Client should be valid", isClientInvalid());
    }

    private boolean isClientInvalid() {
        return datePickerElement.getPropertyBoolean("invalid");
    }

    private void setValue(String value) {
        datePickerElement.sendKeys(value);
        datePickerElement.sendKeys(Keys.ENTER);
    }

    private String dateToString(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();

    }
}
