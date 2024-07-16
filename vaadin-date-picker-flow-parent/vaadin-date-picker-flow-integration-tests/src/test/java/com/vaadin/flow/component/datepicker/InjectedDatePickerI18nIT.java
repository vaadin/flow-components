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
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-date-picker/injected-datepicker")
public class InjectedDatePickerI18nIT extends AbstractComponentIT {

    @Test
    public void checkInitialI18n() {
        open();

        $("injected-datepicker-i18n").first().$("vaadin-date-picker").first()
                .$("vaadin-date-picker-text-field").first().click();

        TestBenchElement cancelButton = $("vaadin-date-picker-overlay").first()
                .$("div").id("content").$("vaadin-date-picker-overlay-content")
                .first().$("vaadin-button").id("cancelButton");

        Assert.assertEquals("peruuta", cancelButton.getText());
    }
}
