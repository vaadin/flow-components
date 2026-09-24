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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.shared.HasSelection;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.tests.MockUIExtension;

class HasSelectionTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private TextField textField;

    @BeforeEach
    void setup() {
        textField = new TextField();
        ui.add(textField);
    }

    @Test
    void selectAll_setsRangeAndFocuses() {
        textField.selectAll();

        PendingJavaScriptInvocation call = lastInvocation();
        String js = call.getInvocation().getExpression();
        // Uses setSelectionRange rather than HTMLInputElement.select() — both
        // focus the input, but setSelectionRange keeps the apply-then-focus
        // ordering explicit and consistent with the other methods.
        Assertions.assertTrue(js.contains("i.setSelectionRange(0,"), js);
        Assertions.assertFalse(js.contains("i.select()"), js);
        // Wrapped in setTimeout to defer past any pending value/focus
        // reflection that would otherwise wipe the selection.
        Assertions.assertTrue(js.contains("setTimeout"), js);
        Assertions.assertTrue(js.contains("i.focus()"), js);
    }

    @Test
    void deselect_collapsesAtSelectionEndWithoutFocus() {
        textField.deselect();

        PendingJavaScriptInvocation call = lastInvocation();
        String js = call.getInvocation().getExpression();
        Assertions.assertTrue(js.contains("selectionEnd"), js);
        Assertions.assertTrue(js.contains("setSelectionRange"), js);
        Assertions.assertTrue(js.contains("setTimeout"), js);
        // deselect never moves focus.
        Assertions.assertFalse(js.contains("focus()"), js);
    }

    @Test
    void setSelectionRange_appliesRangeAndFocuses() {
        textField.setSelectionRange(3, 8);

        PendingJavaScriptInvocation call = lastInvocation();
        String js = call.getInvocation().getExpression();
        Assertions.assertTrue(js.contains("i.setSelectionRange($0, $1)"), js);
        Assertions.assertTrue(js.contains("setTimeout"), js);
        Assertions.assertTrue(js.contains("i.focus()"), js);
        Assertions.assertEquals(3, call.getInvocation().getParameters().get(0));
        Assertions.assertEquals(8, call.getInvocation().getParameters().get(1));
    }

    @Test
    void setCursorPosition_collapsesAtPositionAndFocuses() {
        textField.setCursorPosition(5);

        PendingJavaScriptInvocation call = lastInvocation();
        Assertions.assertEquals(5, call.getInvocation().getParameters().get(0));
        Assertions.assertEquals(5, call.getInvocation().getParameters().get(1));
    }

    @Test
    void textArea_implementsHasSelection() {
        TextArea area = new TextArea();
        ui.add(area);
        area.selectAll();
        Assertions.assertTrue(area instanceof HasSelection);
        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void passwordField_implementsHasSelection() {
        PasswordField password = new PasswordField();
        ui.add(password);
        password.setSelectionRange(1, 4);
        Assertions.assertTrue(password instanceof HasSelection);
        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void bigDecimalField_implementsHasSelection() {
        BigDecimalField field = new BigDecimalField();
        ui.add(field);
        field.setCursorPosition(2);
        Assertions.assertTrue(field instanceof HasSelection);
        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    /**
     * EmailField, NumberField, and IntegerField wrap input types ({@code email}
     * / {@code number}) where the browser disallows {@code selectionStart} /
     * {@code setSelectionRange} per the HTML spec — Chrome and Firefox throw
     * {@code InvalidStateError}. The mixin is therefore deliberately not
     * implemented on these classes so that the type system reflects what
     * actually works at runtime.
     */
    @Test
    void emailNumberInteger_doNotImplementHasSelection() {
        Assertions.assertFalse(new EmailField() instanceof HasSelection);
        Assertions.assertFalse(new NumberField() instanceof HasSelection);
        Assertions.assertFalse(new IntegerField() instanceof HasSelection);
    }

    private PendingJavaScriptInvocation lastInvocation() {
        List<PendingJavaScriptInvocation> calls = ui
                .dumpPendingJavaScriptInvocations();
        Assertions.assertFalse(calls.isEmpty(),
                "expected a pending JavaScript invocation");
        return calls.get(calls.size() - 1);
    }
}
