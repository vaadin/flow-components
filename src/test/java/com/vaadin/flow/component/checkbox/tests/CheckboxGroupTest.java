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
package com.vaadin.flow.component.checkbox.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.checkbox.CheckboxGroup;

import elemental.json.Json;
import elemental.json.JsonArray;

public class CheckboxGroupTest {

    @Test
    public void setReadOnlyCheckboxGroup_groupIsReadOnlyAndDisabled() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
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
    public void setReadOnlyDisabledCheckboxGroup_groupIsDisabledAndReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(true);

        Assert.assertTrue(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void unsetReadOnlyDisabledCheckboxGroup_groupIsDisabledAndNotReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setEnabled(false);
        group.setReadOnly(false);

        Assert.assertFalse(group.isReadOnly());
        Assert.assertFalse(group.isEnabled());
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("disabled"));
    }

    @Test
    public void setReadOnlyEnabledCheckboxGroup_groupIsDisabledAndNotReadonly() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
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
    public void unsetReadOnlyEnabledCheckboxGroup_groupIsEnabled() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
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
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        group.addValueChangeListener(events::add);

        List<String> keys = group.getChildren().map(Component::getElement)
                .map(element -> element.getProperty("value"))
                .collect(Collectors.toList());
        String enabledKey = keys.get(0);
        String disabledKey = keys.get(1);

        JsonArray array = Json.createArray();
        array.set(0, disabledKey);

        group.getElement().setPropertyJson("value", array);
        Assert.assertThat(group.getValue(), IsEmptyCollection.empty());
        Assert.assertTrue(events.isEmpty());

        array = Json.createArray();
        array.set(0, enabledKey);

        group.getElement().setPropertyJson("value", array);
        Assert.assertEquals(Collections.singleton("enabled"), group.getValue());
        Assert.assertEquals(1, events.size());

        ValueChangeEvent<Set<String>> event = events.get(0);
        Assert.assertThat(event.getOldValue(), IsEmptyCollection.empty());
        Assert.assertEquals(Collections.singleton("enabled"), event.getValue());
    }

    @Test
    public void changeItems_selectionIsReset() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems("Foo", "Bar");

        AtomicReference<Set<String>> capture = new AtomicReference<>();
        checkboxGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        checkboxGroup.setValue(Collections.singleton("Foo"));

        Assert.assertEquals(Collections.singleton("Foo"), capture.get());

        Assert.assertEquals(Collections.singleton("Foo"),
                checkboxGroup.getValue());

        checkboxGroup.setItems("Foo", "Baz");

        Assert.assertThat(checkboxGroup.getValue(), IsEmptyCollection.empty());
        Assert.assertThat(capture.get(), IsEmptyCollection.empty());
    }
}
