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

import com.vaadin.flow.component.checkbox.Checkbox;

public class CheckboxUnitTest {

    @Test
    public void initialValue() {
        Checkbox checkbox = new Checkbox();
        Assert.assertFalse(checkbox.getValue());

        checkbox = new Checkbox(true);
        Assert.assertTrue(checkbox.getValue());

        checkbox = new Checkbox(false);
        Assert.assertFalse(checkbox.getValue());
    }

    @Test
    public void testIndeterminate() {
        Checkbox checkbox = new Checkbox();
        Assert.assertFalse(checkbox.isIndeterminate());

        checkbox = new Checkbox(true);
        Assert.assertFalse(checkbox.isIndeterminate());

        checkbox.setIndeterminate(true);
        Assert.assertTrue(checkbox.getValue());
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.setValue(true);
        Assert.assertTrue(checkbox.getValue());
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.setValue(false);
        Assert.assertFalse(checkbox.getValue());
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.setIndeterminate(false);
        Assert.assertFalse(checkbox.getValue());
        Assert.assertFalse(checkbox.isIndeterminate());
    }

    @Test
    public void labelAndInitialValueCtor() {
        Checkbox checkbox = new Checkbox("foo", true);
        Assert.assertTrue(checkbox.getValue());
        Assert.assertEquals("foo", checkbox.getLabel());

        checkbox = new Checkbox("foo", false);
        Assert.assertFalse(checkbox.getValue());
        Assert.assertEquals("foo", checkbox.getLabel());
    }

    @Test
    public void setEnable() {
        Checkbox checkbox = new Checkbox("foo", true);
        checkbox.setEnabled(true);
        Assert.assertTrue(checkbox.isEnabled());
        checkbox.setEnabled(false);
        Assert.assertFalse(checkbox.isEnabled());
    }
}
