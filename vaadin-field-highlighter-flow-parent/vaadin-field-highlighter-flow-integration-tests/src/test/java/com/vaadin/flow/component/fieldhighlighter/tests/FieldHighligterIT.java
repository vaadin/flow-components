/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.fieldhighlighter.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-field-highlighter")
public class FieldHighligterIT extends AbstractComponentIT {

    @Test
    public void callInitMethod_TextFieldHasAttribute() {
        open();
        ButtonElement button = $(ButtonElement.class).id("call-init");
        TextFieldElement textField = $(TextFieldElement.class)
                .id("tf-with-highlighter");

        Assert.assertFalse(
                "TextField should not have the has-highlighter attribute before calling initializing.",
                textField.hasAttribute("has-highlighter"));

        button.click();
        Assert.assertTrue(
                "TextField should have the has-highlighter attribute after calling initializing.",
                textField.hasAttribute("has-highlighter"));
    }
}
