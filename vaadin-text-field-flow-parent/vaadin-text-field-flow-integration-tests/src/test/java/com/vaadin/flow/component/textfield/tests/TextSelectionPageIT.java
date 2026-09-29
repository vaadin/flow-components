/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-text-field/text-selection")
public class TextSelectionPageIT extends AbstractComponentIT {

    private TextFieldElement textField;
    private TextAreaElement textArea;
    private PasswordFieldElement passwordField;

    @Before
    public void init() {
        open();
        textField = $(TextFieldElement.class).id("text-field");
        textArea = $(TextAreaElement.class).id("text-area");
        passwordField = $(PasswordFieldElement.class).id("password-field");
    }

    @Test
    public void textField_selectAll_selectsValueAndFocuses() {
        clickElementWithJs("text-field-select-all");

        assertSelection(textField, 0, "Hello world".length());
        assertFocused(textField);
    }

    @Test
    public void textField_setSelectionRange_selectsRangeAndFocuses() {
        clickElementWithJs("text-field-set-range");

        assertSelection(textField, 2, 7);
        assertFocused(textField);
    }

    @Test
    public void textField_autoselect_setSelectionRange_selectsRangeAndFocuses() {
        clickElementWithJs("text-field-enable-autoselect");
        clickElementWithJs("text-field-set-range");

        assertSelection(textField, 2, 7);
        assertFocused(textField);
    }

    @Test
    public void textField_setCursorPosition_collapsesSelectionAndFocuses() {
        clickElementWithJs("text-field-set-cursor");

        assertSelection(textField, 4, 4);
        assertFocused(textField);
    }

    @Test
    public void textField_selectAll_focusIsFromServer() {
        clickElementWithJs("text-field-select-all");

        assertTextFieldFocusFromServer();
    }

    @Test
    public void textField_setSelectionRange_focusIsFromServer() {
        clickElementWithJs("text-field-set-range");

        assertTextFieldFocusFromServer();
    }

    @Test
    public void textArea_setSelectionRange_selectsRangeAndFocuses() {
        clickElementWithJs("text-area-set-range");

        assertSelection(textArea, 2, 7);
        assertFocused(textArea);
    }

    @Test
    public void passwordField_setSelectionRange_selectsRangeAndFocuses() {
        clickElementWithJs("password-field-set-range");

        assertSelection(passwordField, 2, 7);
        assertFocused(passwordField);
    }

    private void assertSelection(TestBenchElement field, int start, int end) {
        Object selection = executeScript(
                "const input = arguments[0].inputElement;"
                        + "return [input.selectionStart, input.selectionEnd];",
                field);
        Assert.assertEquals(List.of((long) start, (long) end), selection);
    }

    private void assertTextFieldFocusFromServer() {
        TestBenchElement focusFromClient = $("div")
                .id("text-field-focus-from-client");
        waitUntil(driver -> !focusFromClient.getText().isEmpty());
        Assert.assertEquals("Is focus from client: false",
                focusFromClient.getText());
    }

    private void assertFocused(TestBenchElement field) {
        Object focused = executeScript(
                "return document.activeElement === arguments[0].inputElement;",
                field);
        Assert.assertEquals("Field should be focused", true, focused);
        Assert.assertTrue("Field should have focus-ring",
                field.hasAttribute("focus-ring"));
    }
}
