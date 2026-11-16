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
package com.vaadin.flow.component.checkbox;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class CheckboxGroupItemEnabledTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private CheckboxGroup<String> group;

    @BeforeEach
    void setup() {
        group = new CheckboxGroup<>();
        group.setItems("enabled", "disabled");
        ui.add(group);
        ui.dumpPendingJavaScriptInvocations();
    }

    @Test
    void setItemEnabledProvider_enabledItem_checkboxEnabled() {
        group.setItemEnabledProvider("enabled"::equals);

        assertLastSetDisabledCall(0, false);
    }

    @Test
    void setItemEnabledProvider_disabledItem_checkboxDisabled() {
        group.setItemEnabledProvider("enabled"::equals);

        assertLastSetDisabledCall(1, true);
    }

    @Test
    void disableGroup_checkboxDisabled() {
        group.setEnabled(false);

        assertLastSetDisabledCall(0, true);
    }

    @Test
    void enableGroup_itemDisabledByProvider_checkboxStaysDisabled() {
        group.setItemEnabledProvider("enabled"::equals);
        group.setEnabled(false);
        ui.dumpPendingJavaScriptInvocations();

        group.setEnabled(true);

        assertLastSetDisabledCall(1, true);
    }

    private void assertLastSetDisabledCall(int index, boolean disabled) {
        Element checkbox = group.getElement().getChild(index);
        List<JsCall> calls = ui.dumpJsCalls(checkbox, CheckboxGroupJs.class);

        Assertions.assertFalse(calls.isEmpty());
        JsCall lastCall = calls.get(calls.size() - 1);
        Assertions.assertEquals("setDisabled", lastCall.methodName());
        Assertions.assertEquals(List.of(disabled), lastCall.arguments());
    }
}
