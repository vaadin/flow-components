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
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class RadioButtonGroupSignalTest extends AbstractSignalsUnitTest {
    private final RadioButtonGroup<String> group = new RadioButtonGroup<>();
    private final ValueSignal<Boolean> readonlySignal = new ValueSignal<>(
            false);

    @Test
    public void bindReadOnly_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);

        Assert.assertFalse(group.isReadOnly());

        readonlySignal.set(true);
        Assert.assertTrue(group.isReadOnly());
    }

    @Test
    public void bindReadOnly_elementNotAttached_bindingInactive_untilAttach() {
        readonlySignal.set(true);
        group.bindReadOnly(readonlySignal);

        Assert.assertFalse(group.isReadOnly());

        UI.getCurrent().add(group);
        Assert.assertTrue(group.isReadOnly());
    }

    @Test(expected = BindingActiveException.class)
    public void setReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);
        group.setReadOnly(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindReadOnly_whileBound_throwsException() {
        UI.getCurrent().add(group);
        group.bindReadOnly(readonlySignal);
        group.bindReadOnly(new ValueSignal<>(true));
    }

    @Test
    public void bindReadOnly_disablesUncheckedButtons() {
        group.setItems("One", "Two", "Three");
        group.setValue("One");
        group.bindReadOnly(readonlySignal);
        UI.getCurrent().add(group);

        readonlySignal.set(true);

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

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var listSignal = new ListSignal<String>();
        listSignal.insertLast("One");
        listSignal.insertLast("Two");

        RadioButtonGroup<String> group = new RadioButtonGroup<>("Options",
                listSignal);
        UI.getCurrent().add(group);

        List<String> items = group.getGenericDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("One", items.get(0));
        Assert.assertEquals("Two", items.get(1));
        Assert.assertEquals("Options", group.getLabel());

        listSignal.insertLast("Three");

        items = group.getGenericDataView().getItems().toList();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Three", items.get(2));
    }
}
