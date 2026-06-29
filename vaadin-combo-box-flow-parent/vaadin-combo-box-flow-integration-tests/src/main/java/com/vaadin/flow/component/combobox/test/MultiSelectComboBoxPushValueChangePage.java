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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * Reproduces the scenario from
 * <a href="https://github.com/vaadin/flow-components/issues/9611">#9611</a>:
 * with push enabled, two separate push frames around a server-side
 * {@code setValue} must not relay a spurious client-initiated value change with
 * an empty value back to the server.
 */
@Route("vaadin-multi-select-combo-box/push-value-change")
public class MultiSelectComboBoxPushValueChangePage extends Div {
    public MultiSelectComboBoxPushValueChangePage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        Div log = new Div();
        log.setId("value-change-log");

        NativeButton runScenario = new NativeButton("Run #9611 scenario",
                event -> {
                    MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
                    comboBox.setItems(List.of("1", "2", "3"));

                    comboBox.addValueChangeListener(e -> {
                        Div entry = new Div();
                        entry.setText("isFromClient=%s old=%s new=%s value=%s"
                                .formatted(e.isFromClient(), e.getOldValue(),
                                        e.getValue(), comboBox.getValue()));
                        log.add(entry);
                    });

                    add(comboBox);

                    UI ui = UI.getCurrent();
                    ui.push();
                    comboBox.setValue(Set.of("1"));
                    ui.push();
                });
        runScenario.setId("run-scenario");

        add(runScenario, log);
    }
}
