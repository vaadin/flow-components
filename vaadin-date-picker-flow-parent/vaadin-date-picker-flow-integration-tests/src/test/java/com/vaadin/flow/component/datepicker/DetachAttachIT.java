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

package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

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
    public void clientSideValidationIsOverriddenOnAttach() {
        open();

        assertDatePickerIsValidOnTab();

        // Detaching and attaching date picker
        detach();
        attach();

        assertDatePickerIsValidOnTab();
    }

    private void assertDatePickerIsValidOnTab() {
        WebElement datePickerElement = $(DatePickerElement.class).first();
        datePickerElement.sendKeys(Keys.TAB);
        Assert.assertFalse("Date picker should be valid after Tab", Boolean
                .parseBoolean(datePickerElement.getAttribute("invalid")));
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
        return $(DatePickerElement.class).first().$("input").first()
                .getPropertyString("value");
    }

    private String getCancelText() {
        return (String) executeScript("return arguments[0].i18n.cancel",
                $(DatePickerElement.class).first());
    }
}
