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
package com.vaadin.flow.component.radiobutton;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class RadioButtonGroupSignalTest extends AbstractSignalsUnitTest {
    private final RadioButtonGroup<String> group = new RadioButtonGroup<>();

    @Test
    public void bindReadOnly_disablesUncheckedButtons() {
        ValueSignal<Boolean> readOnlySignal = new ValueSignal<>(false);

        group.setItems("One", "Two", "Three");
        group.setValue("One");
        group.bindReadOnly(readOnlySignal);
        UI.getCurrent().add(group);

        readOnlySignal.set(true);

        List<RadioButton<String>> buttons = getRadioButtons();
        Assert.assertTrue("Selected button should remain enabled",
                buttons.get(0).isEnabled());
        Assert.assertFalse("Unchecked button should be disabled",
                buttons.get(1).isEnabled());
        Assert.assertFalse("Unchecked button should be disabled",
                buttons.get(2).isEnabled());
    }

    @SuppressWarnings("unchecked")
    private List<RadioButton<String>> getRadioButtons() {
        return group.getChildren().filter(RadioButton.class::isInstance)
                .map(child -> (RadioButton<String>) child)
                .collect(Collectors.toList());
    }
}
