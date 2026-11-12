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
package com.vaadin.flow.component.combobox;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.server.CspMode;
import com.vaadin.tests.MockUIExtension;

/**
 * Covers that {@link ComboBox} and {@link MultiSelectComboBox} only run
 * declared JavaScript, so that they work with a strict Content Security Policy.
 * In the strict mode, sending JavaScript as a string throws.
 * <p>
 * Custom renderers are not covered, as their templates are still sent as
 * strings.
 */
class ComboBoxStrictCspTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @BeforeEach
    void enableStrictCsp() {
        ui.setCspMode(CspMode.STRICT);
    }

    @Test
    void attach_initializesConnector() {
        ComboBox<String> comboBox = new ComboBox<>();

        ui.add(comboBox);

        List<JsCall> calls = ui.dumpJsCalls(comboBox.getElement(),
                ComboBoxJs.class);
        Assertions.assertEquals(List.of("initConnector"),
                calls.stream().map(JsCall::methodName).toList());
    }

    @Test
    void setItems_resetsConnectorThroughPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        ui.add(comboBox);
        ui.dumpPendingJavaScriptInvocations();

        comboBox.setItems("a", "b");

        List<JsCall> calls = ui.dumpJsCalls(ui.getUI().getElement(),
                ComboBoxJs.class);
        Assertions.assertFalse(calls.isEmpty());
        for (JsCall call : calls) {
            Assertions.assertEquals("resetConnector", call.methodName());
            Assertions.assertEquals(List.of(comboBox.getElement()),
                    call.arguments());
        }
    }

    @Test
    void setValueAndClear_setInputElementValue() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("a", "b");
        comboBox.setItemLabelGenerator(item -> "Label " + item);
        ui.add(comboBox);
        ui.dumpPendingJavaScriptInvocations();

        comboBox.setValue("b");
        comboBox.clear();

        List<JsCall> calls = ui.dumpJsCalls(comboBox.getElement(),
                ComboBoxJs.class);
        Assertions.assertEquals(2, calls.size());
        Assertions.assertEquals("setInputElementValue",
                calls.get(0).methodName());
        Assertions.assertEquals(List.of("Label b"), calls.get(0).arguments());
        Assertions.assertEquals("setInputElementValue",
                calls.get(1).methodName());
        Assertions.assertEquals(List.of(""), calls.get(1).arguments());
    }

    @Test
    void multiSelectComboBox_commonFeatures_onlyDeclaredJavaScript() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("a", "b");
        comboBox.setItemLabelGenerator(item -> "Label " + item);
        ui.add(comboBox);
        ui.dumpPendingJavaScriptInvocations();

        comboBox.setValue(Set.of("a"));
        comboBox.setItems("a", "b", "c");
        comboBox.clear();

        List<String> methodNames = ui
                .dumpJsCalls(ui.getUI().getElement(), ComboBoxJs.class).stream()
                .map(JsCall::methodName).distinct().toList();
        Assertions.assertEquals(List.of("resetConnector"), methodNames);
    }
}
