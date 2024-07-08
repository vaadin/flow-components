/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.gridpro.GridPro.EditColumn;

import java.util.ArrayList;
import java.util.List;

public class GridProEditColumnConfiguratorTest {

    EditColumnConfigurator<Person> configurator;
    ItemUpdater testItemUpdater;
    List<String> listOptions;
    EditColumn<Person> column;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        GridPro<Person> grid = new GridPro<>();
        configurator = grid.addEditColumn(value -> value);
        column = (EditColumn<Person>) configurator.getColumn();

        testItemUpdater = (item, newValue) -> {
        };
        listOptions = new ArrayList<>();
        listOptions.add("foo");
        listOptions.add("bar");
        listOptions.add("baz");
    }

    @Test
    public void shouldConfigureTextEditColumnPreset() {
        configurator.text(testItemUpdater);

        Assert.assertEquals(column.getEditorType(),
                EditorType.TEXT.getTypeName());
        Assert.assertEquals(column.getItemUpdater(), testItemUpdater);
    }

    @Test
    public void shouldConfigureCheckboxEditColumnPreset() {
        Person initialItem = new Person(null, 0);
        String initialValue = "true";

        testItemUpdater = (item, newValue) -> {
            Assert.assertEquals(initialItem, item);
            Assert.assertTrue(Boolean.parseBoolean(initialValue));
        };

        configurator.checkbox(testItemUpdater);
        Assert.assertEquals(column.getEditorType(),
                EditorType.CHECKBOX.getTypeName());
        column.getItemUpdater().accept(initialItem, initialValue);
    }

    @Test
    public void shouldConfigureSelectEditColumnPreset() {
        configurator.select(testItemUpdater, listOptions);

        Assert.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assert.assertEquals(column.getItemUpdater(), testItemUpdater);
        Assert.assertEquals(column.getOptions(), listOptions);
    }

    @Test
    public void shouldConfigureSelectEditColumnEnumPreset() {
        configurator.select(testItemUpdater, "foo", "bar", "baz");

        Assert.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assert.assertEquals(column.getItemUpdater(), testItemUpdater);
        Assert.assertEquals(column.getOptions(), listOptions);
    }

    @Test
    public void shouldConfigureSelectEditColumnEnumToStringPreset() {
        Person initialItem = new Person(null, 0);
        String initialValue = "bar";

        testItemUpdater = (item, newValue) -> {
            Assert.assertEquals(initialItem, item);
            Assert.assertEquals(initialValue,
                    ((TestEnum) newValue).getStringRepresentation());
        };
        configurator.select(testItemUpdater, TestEnum.class,
                TestEnum::getStringRepresentation);

        Assert.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assert.assertEquals(column.getOptions(), listOptions);
        column.getItemUpdater().accept(initialItem, initialValue);
    }

    @Test
    public void shouldConfigureSelectEditColumnEnumDefaultPreset() {
        Person initialItem = new Person(null, 0);
        String initialValue = "bar";

        testItemUpdater = (item, newValue) -> {
            Assert.assertEquals(initialItem, item);
            Assert.assertEquals(initialValue,
                    ((TestEnum) newValue).getStringRepresentation());
        };
        configurator.select(testItemUpdater, TestEnum.class);

        Assert.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assert.assertEquals(column.getOptions(), listOptions);
        column.getItemUpdater().accept(initialItem, initialValue);
    }

    private enum TestEnum {
        FOO("foo"), BAR("bar"), BAZ("baz");

        private String stringRepresentation;

        TestEnum(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return stringRepresentation;
        }

        @Override
        public String toString() {
            return getStringRepresentation();
        }
    }

}
