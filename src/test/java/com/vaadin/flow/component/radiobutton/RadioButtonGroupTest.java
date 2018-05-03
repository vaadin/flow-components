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
package com.vaadin.flow.component.radiobutton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;

public class RadioButtonGroupTest {

    @Test
    public void setReadOnlyRadioGroup_groupIsReadOnlyAndDisabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        Assert.assertTrue(group.isReadOnly());

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(group.getChildren().count(), disabledChildCount);
    }

    @Test
    public void setReadOnlyDisabledRadioGroup_groupIsDisabledAndReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void unsetReadOnlyDisabledRadioGroup_groupIsDisabledAndNotReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(false);

        Assert.assertFalse(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void setReadOnlyEnabledRadioGroup_groupIsDisabledAndNotReadonly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setReadOnly(true);
        group.setEnabled(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));

        group.setReadOnly(false);

        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void unsetReadOnlyEnabledRadioGroup_groupIsEnabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);
        group.setEnabled(true);

        group.setReadOnly(false);

        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void selectDisabledItem_noRedundantEvent() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> keys = group.getChildren().map(Component::getElement)
                .map(element -> element.getProperty("value"))
                .collect(Collectors.toList());
        String enabledKey = keys.get(0);
        String disabledKey = keys.get(1);

        group.getElement().setProperty("value", disabledKey);
        Assert.assertNull(group.getValue());
        Assert.assertTrue(events.isEmpty());

        group.getElement().setProperty("value", enabledKey);
        Assert.assertEquals("enabled", group.getValue());
        Assert.assertEquals(1, events.size());

        ValueChangeEvent<String> event = events.get(0);
        Assert.assertNull(event.getOldValue());
        Assert.assertEquals("enabled", event.getValue());
    }

}
