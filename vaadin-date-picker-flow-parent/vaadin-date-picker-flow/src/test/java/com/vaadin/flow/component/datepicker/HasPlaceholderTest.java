/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasPlaceholder;

public class HasPlaceholderTest {

    @Test
    public void datePicker() {
        DatePicker c = new DatePicker();
        Assert.assertTrue(c instanceof HasPlaceholder);
    }

}
