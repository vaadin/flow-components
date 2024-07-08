/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-date-picker/helpers-view")
public class DatePickerHelpersIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertHelperText() {
        DatePickerElement dataPickerHelperText = $(DatePickerElement.class)
                .id("data-picker-helper-text");
        Assert.assertEquals("Helper text",
                dataPickerHelperText.getHelperText());

        $("button").id("button-clear-text").click();
        Assert.assertEquals("", dataPickerHelperText.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        DatePickerElement dataPickerHelperComponent = $(DatePickerElement.class)
                .id("data-picker-helper-component");
        Assert.assertEquals("helper-component", dataPickerHelperComponent
                .getHelperComponent().getAttribute("id"));

        $("button").id("button-clear-component").click();
        Assert.assertNull(dataPickerHelperComponent.getHelperComponent());
    }
}
