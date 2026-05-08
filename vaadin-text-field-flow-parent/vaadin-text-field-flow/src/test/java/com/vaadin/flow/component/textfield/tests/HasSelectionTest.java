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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
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
    void selectAll_executesInputElementSelect() {
        textField.selectAll();

        PendingJavaScriptInvocation call = lastInvocation();
        Assertions.assertTrue(
                call.getInvocation().getExpression()
                        .contains("this.inputElement.select()"),
                call.getInvocation().getExpression());
        Assertions.assertEquals(textField.getElement(),
                call.getInvocation().getParameters().get(0));
    }

    @Test
    void deselect_executesCollapseAtSelectionEnd() {
        textField.deselect();

        PendingJavaScriptInvocation call = lastInvocation();
        String js = call.getInvocation().getExpression();
        Assertions.assertTrue(js.contains("selectionEnd"), js);
        Assertions.assertTrue(js.contains("setSelectionRange"), js);
    }

    @Test
    void setSelectionRange_executesWithStartAndEndArguments() {
        textField.setSelectionRange(3, 8);

        PendingJavaScriptInvocation call = lastInvocation();
        String js = call.getInvocation().getExpression();
        Assertions.assertTrue(
                js.contains("this.inputElement.setSelectionRange($0, $1)"), js);
        Assertions.assertEquals(3, call.getInvocation().getParameters().get(0));
        Assertions.assertEquals(8, call.getInvocation().getParameters().get(1));
    }

    @Test
    void setCursorPosition_collapsesAtPosition() {
        textField.setCursorPosition(5);

        PendingJavaScriptInvocation call = lastInvocation();
        Assertions.assertEquals(5, call.getInvocation().getParameters().get(0));
        Assertions.assertEquals(5, call.getInvocation().getParameters().get(1));
    }

    @Test
    void textArea_inheritsHasSelection() {
        TextArea area = new TextArea();
        ui.add(area);
        area.selectAll();
        // No exception, invocation queued
        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void emailField_inheritsHasSelection() {
        EmailField email = new EmailField();
        ui.add(email);
        email.setSelectionRange(1, 4);
        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void numberField_inheritsHasSelection() {
        NumberField number = new NumberField();
        ui.add(number);
        number.setCursorPosition(2);
        Assertions.assertFalse(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    private PendingJavaScriptInvocation lastInvocation() {
        List<PendingJavaScriptInvocation> calls = ui
                .dumpPendingJavaScriptInvocations();
        Assertions.assertFalse(calls.isEmpty(),
                "expected a pending JavaScript invocation");
        return calls.get(calls.size() - 1);
    }
}
