/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.demo;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

public class ValueChangeModeButtonProvider {
    public static final String TOGGLE_BUTTON_ID = "toggleValueChangeMode";

    private final HasValueChangeMode elementWithChangeMode;

    ValueChangeModeButtonProvider(
            HasValueChangeMode elementWithChangeMode) {
        this.elementWithChangeMode = elementWithChangeMode;
    }

    NativeButton getToggleValueSyncButton() {
        NativeButton toggleValueSync = new NativeButton(getToggleButtonText(
                elementWithChangeMode.getValueChangeMode()));
        toggleValueSync.setId(TOGGLE_BUTTON_ID);
        toggleValueSync.addClickListener(event -> {
            ValueChangeMode newMode = getDifferentMode(
                    elementWithChangeMode.getValueChangeMode());
            elementWithChangeMode.setValueChangeMode(newMode);
            toggleValueSync.setText(getToggleButtonText(newMode));
        });
        return toggleValueSync;
    }

    private ValueChangeMode getDifferentMode(ValueChangeMode valueChangeMode) {
        switch (valueChangeMode) {
            case EAGER:
                return ValueChangeMode.ON_CHANGE;
            case ON_CHANGE:
                return ValueChangeMode.EAGER;
            default:
                throw new IllegalArgumentException(
                        "Unexpected value change mode: " + valueChangeMode);
        }
    }

    private String getToggleButtonText(ValueChangeMode valueChangeMode) {
        switch (valueChangeMode) {
            case EAGER:
            return "Switch to sync value only on committed changes";
            case ON_CHANGE:
            return "Switch to sync value eagerly on each change";
            default:
                throw new IllegalArgumentException(
                        "Unexpected value change mode: " + valueChangeMode);
        }
    }
}
