/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-picker/detach-attach")
public class DetachAttachIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setLocale_detach_reattach_localeCorrect() {
        Assert.assertEquals(
                "DatePicker displays unexpected value with French locale.",
                "13/06/1993", getInputValue());
        detach();
        attach();
        Assert.assertEquals(
                "DatePicker displays unexpected value with French locale after detach and reattach.",
                "13/06/1993", getInputValue());
    }

    @Test
    public void setI18N_detach_reattach_i18nCorrect() {
        Assert.assertEquals("peruuta", getCancelText());
        detach();
        attach();
        Assert.assertEquals("peruuta", getCancelText());
    }

    private void detach() {
        findElement(By.id("detach")).click();
    }

    private void attach() {
        findElement(By.id("attach")).click();
    }

    private String getInputValue() {
        return $(DatePickerElement.class).first()
                .$("vaadin-date-picker-text-field").first()
                .getPropertyString("value");
    }

    private String getCancelText() {
        return (String) executeScript("return arguments[0].i18n.cancel",
                $(DatePickerElement.class).first());
    }
}
