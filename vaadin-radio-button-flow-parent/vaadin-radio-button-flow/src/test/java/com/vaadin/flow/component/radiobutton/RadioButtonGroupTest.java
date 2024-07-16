/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import org.junit.Assert;
import org.junit.Test;

public class RadioButtonGroupTest {

    private static final String OUTER_HTML = "<vaadin-radio-button>\n <span>%s</span>\n</vaadin-radio-button>";

    @Test
    public void setReadOnlyRadioGroup_groupIsReadOnly() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        Assert.assertTrue(group.isReadOnly());

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(group.getChildren().count(), disabledChildCount);
    }

    @Test
    public void setReadOnlyRadioGroup_checkedButtonIsEnabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setValue("foo");
        group.setReadOnly(true);

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(1, disabledChildCount);
    }

    @Test
    public void setReadOnlyRadioGroup_checkedButtonIsEnabled2() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar");
        group.setReadOnly(true);
        group.setValue("foo");

        long disabledChildCount = group.getChildren().filter(
                child -> child.getElement().getProperty("disabled", false))
                .count();
        Assert.assertEquals(1, disabledChildCount);
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
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getElement().getProperty("readonly"));

        group.setReadOnly(false);

        Assert.assertTrue(group.isEnabled());
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("disabled"));
        Assert.assertEquals(Boolean.FALSE.toString(),
                group.getElement().getProperty("readonly"));
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

    @Test
    public void disabledItems_itemEnabledProvider_stayDisabled() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled");
        group.setItemEnabledProvider("enabled"::equals);

        List<RadioButton<String>> children = group.getChildren()
                .map(child -> (RadioButton<String>) child)
                .collect(Collectors.toList());

        Assert.assertTrue(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());

        group.setEnabled(false);
        Assert.assertFalse(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());

        group.setEnabled(true);
        Assert.assertTrue(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());

        group.setEnabled(false);
        Assert.assertFalse(children.get(0).isEnabled());
        Assert.assertFalse(children.get(1).isEnabled());
    }

    @Test
    public void changeItems_selectionIsReset() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setItems("Foo", "Bar");

        AtomicReference<String> capture = new AtomicReference<>();
        radioButtonGroup
                .addValueChangeListener(event -> capture.set(event.getValue()));

        radioButtonGroup.setValue("Foo");

        Assert.assertEquals("Foo", capture.get());

        Assert.assertEquals("Foo", radioButtonGroup.getValue());

        radioButtonGroup.setItems("Foo", "Baz");

        Assert.assertEquals(null, radioButtonGroup.getValue());
        Assert.assertEquals(null, capture.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResetAllItems() {
        RadioButtonGroup<ItemHelper> group = new RadioButtonGroup<ItemHelper>();
        ItemHelper item1 = new ItemHelper("foo", "01");
        ItemHelper item2 = new ItemHelper("baz", "02");

        group.setItems(item1, item2);

        item1.setName("zoo");
        item2.setName("bar");
        group.getDataProvider().refreshAll();

        List<Component> components = group.getChildren()
                .collect(Collectors.toList());
        RadioButton<ItemHelper> radioZoo = (RadioButton<ItemHelper>) components
                .get(0);
        RadioButton<ItemHelper> radioBar = (RadioButton<ItemHelper>) components
                .get(1);

        Assert.assertEquals(String.format(OUTER_HTML, "zoo"),
                radioZoo.getElement().getOuterHTML());
        Assert.assertEquals(String.format(OUTER_HTML, "bar"),
                radioBar.getElement().getOuterHTML());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResetSingleItem() {
        RadioButtonGroup<ItemHelper> group = new RadioButtonGroup<ItemHelper>();
        ItemHelper item1 = new ItemHelper("foo", "01");
        ItemHelper item2 = new ItemHelper("baz", "02");

        group.setItems(item1, item2);

        item1.setName("zoo");
        item2.setName("bar");
        group.getDataProvider().refreshItem(item2);

        List<Component> components = group.getChildren()
                .collect(Collectors.toList());
        RadioButton<ItemHelper> radioFoo = (RadioButton<ItemHelper>) components
                .get(0);
        RadioButton<ItemHelper> radioBar = (RadioButton<ItemHelper>) components
                .get(1);

        Assert.assertEquals(String.format(OUTER_HTML, "foo"),
                radioFoo.getElement().getOuterHTML());
        Assert.assertEquals(String.format(OUTER_HTML, "bar"),
                radioBar.getElement().getOuterHTML());
    }

    @Test
    public void addNullOption_setValue() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("enabled", "disabled", null);
        group.setValue(null);
        Assert.assertEquals(group.getValue(), null);
    }
}
