/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;

public class HasLabelTest {

    @Test
    public void checkbox() {
        Checkbox c = new Checkbox();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void checkboxGroup() {
        CheckboxGroup<String> c = new CheckboxGroup<>();
        Assert.assertTrue(c instanceof HasLabel);
    }

}
