/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class HasLabelTest {

    @Test
    public void bigDecimalField() {
        BigDecimalField c = new BigDecimalField();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void emailField() {
        EmailField c = new EmailField();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void integerField() {
        IntegerField c = new IntegerField();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void numberField() {
        NumberField c = new NumberField();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void passwordField() {
        TextField c = new TextField();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void textArea() {
        TextArea c = new TextArea();
        Assert.assertTrue(c instanceof HasLabel);
    }

    @Test
    public void textField() {
        TextField c = new TextField();
        Assert.assertTrue(c instanceof HasLabel);
    }

}
