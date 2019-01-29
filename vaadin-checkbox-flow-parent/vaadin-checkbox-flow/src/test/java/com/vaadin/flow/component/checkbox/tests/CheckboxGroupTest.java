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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.vaadin.flow.data.provider.ListDataProvider;
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

    @Test
    public void singleDataRefreshEvent() {
        Wrapper item1 = new Wrapper(1, "foo");
        Wrapper item2 = new Wrapper(2, "bar");

        List<Wrapper> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CheckboxGroup<Wrapper> checkboxGroup = getRefreshEventCheckboxGroup(items);

        assertCheckboxLabels(checkboxGroup, "foo", "bar");

        item1.setLabel("etc");
        item2.setLabel("opt");
        checkboxGroup.getDataProvider().refreshItem(item1);
        assertCheckboxLabels(checkboxGroup, "etc", "bar");

    }

    @Test
    public void singleDataRefreshEvent_overrideDataProviderGetId() {
        Wrapper item1 = new Wrapper(1, "foo");
        Wrapper item2 = new Wrapper(2, "bar");

        List<Wrapper> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CheckboxGroup<Wrapper> checkboxGroup =
                getRefreshEventCheckboxGroupWithCustomDataProvider(items);

        assertCheckboxLabels(checkboxGroup, "foo", "bar");

        item1.setLabel("etc");
        item2.setLabel("opt");
        checkboxGroup.getDataProvider().refreshItem(new Wrapper(1));
        assertCheckboxLabels(checkboxGroup, "etc", "bar");


    }

    @Test
    public void allDataRefreshEvent() {
        Wrapper item1 = new Wrapper(1, "foo");
        Wrapper item2 = new Wrapper(2, "bar");

        List<Wrapper> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CheckboxGroup<Wrapper> checkboxGroup = getRefreshEventCheckboxGroup(items);

        assertCheckboxLabels(checkboxGroup, "foo", "bar");

        item1.setLabel("etc");
        item2.setLabel("opt");
        checkboxGroup.getDataProvider().refreshAll();
        assertCheckboxLabels(checkboxGroup, "etc", "opt");

    }


    private CheckboxGroup<Wrapper> getRefreshEventCheckboxGroup(List<Wrapper> items) {
        CheckboxGroup<Wrapper> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItemLabelGenerator(Wrapper::getLabel);
        ListDataProvider<Wrapper> dataProvider = new ListDataProvider<>(items);
        checkboxGroup.setDataProvider(dataProvider);
        return checkboxGroup;
    }

    private CheckboxGroup<Wrapper> getRefreshEventCheckboxGroupWithCustomDataProvider(List<Wrapper> items) {
        CheckboxGroup<Wrapper> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItemLabelGenerator(Wrapper::getLabel);
        ListDataProvider<Wrapper> dataProvider = new CustomDataProvider(items);
        checkboxGroup.setDataProvider(dataProvider);
        return checkboxGroup;
    }

    private void assertCheckboxLabels(CheckboxGroup<Wrapper> checkboxGroup, String firstLabel, String secondLabel) {
        List<Component> components = checkboxGroup.getChildren().collect(Collectors.toList());
        Assert.assertEquals(2, components.size());
        Assert.assertEquals(firstLabel, components.get(0).getElement().getText());
        Assert.assertEquals(secondLabel, components.get(1).getElement().getText());
    }

    /**
     * Used in the tests {@link #singleDataRefreshEvent()} and {@link #allDataRefreshEvent()}
     */
    private class Wrapper {

        private int id;
        private String label;

        public Wrapper(int id) {
            this.id = id;
        }

        Wrapper(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        String getLabel() {
            return label;
        }

        void setLabel(String label) {
            this.label = label;
        }
    }

    private class CustomDataProvider extends ListDataProvider<Wrapper> {

        /**
         * Constructs a new ListDataProvider.
         * <p>
         * No protective copy is made of the list, and changes in the provided
         * backing Collection will be visible via this data provider. The caller
         * should copy the list if necessary.
         *
         * @param items the initial data, not null
         */
        public CustomDataProvider(Collection<Wrapper> items) {
            super(items);
        }

        /**
         * Gets an identifier for the given Wrapper. This identifier is used by the
         * framework to determine equality between two Wrappers.
         *
         * @param wrapper the Wrapper to get identifier for; not {@code null}
         * @return the identifier for given wrapper; not {@code null}
         */
        @Override
        public Object getId(Wrapper wrapper) {
            return wrapper.getId();
        }
    }
}
