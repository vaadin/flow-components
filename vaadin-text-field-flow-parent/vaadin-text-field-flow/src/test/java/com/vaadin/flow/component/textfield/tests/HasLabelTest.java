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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.Assert;
import org.junit.Test;

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
