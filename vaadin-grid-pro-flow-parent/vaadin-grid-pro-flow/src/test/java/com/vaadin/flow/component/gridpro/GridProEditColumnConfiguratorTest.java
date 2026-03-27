/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.gridpro.GridPro.EditColumn;

class GridProEditColumnConfiguratorTest {

    EditColumnConfigurator<Person> configurator;
    ItemUpdater testItemUpdater;
    List<String> listOptions;
    EditColumn<Person> column;

    @BeforeEach
    void setup() {
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
    void shouldConfigureTextEditColumnPreset() {
        configurator.text(testItemUpdater);

        Assertions.assertEquals(column.getEditorType(),
                EditorType.TEXT.getTypeName());
        Assertions.assertEquals(column.getItemUpdater(), testItemUpdater);
    }

    @Test
    void shouldConfigureCheckboxEditColumnPreset() {
        Person initialItem = new Person(null, 0);
        String initialValue = "true";

        testItemUpdater = (item, newValue) -> {
            Assertions.assertEquals(initialItem, item);
            Assertions.assertTrue(Boolean.parseBoolean(initialValue));
        };

        configurator.checkbox(testItemUpdater);
        Assertions.assertEquals(column.getEditorType(),
                EditorType.CHECKBOX.getTypeName());
        column.getItemUpdater().accept(initialItem, initialValue);
    }

    @Test
    void shouldConfigureSelectEditColumnPreset() {
        configurator.select(testItemUpdater, listOptions);

        Assertions.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assertions.assertEquals(column.getItemUpdater(), testItemUpdater);
        Assertions.assertEquals(column.getOptions(), listOptions);
    }

    @Test
    void shouldConfigureSelectEditColumnEnumPreset() {
        configurator.select(testItemUpdater, "foo", "bar", "baz");

        Assertions.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assertions.assertEquals(column.getItemUpdater(), testItemUpdater);
        Assertions.assertEquals(column.getOptions(), listOptions);
    }

    @Test
    void shouldConfigureSelectEditColumnEnumToStringPreset() {
        Person initialItem = new Person(null, 0);
        String initialValue = "bar";

        testItemUpdater = (item, newValue) -> {
            Assertions.assertEquals(initialItem, item);
            Assertions.assertEquals(initialValue,
                    ((TestEnum) newValue).getStringRepresentation());
        };
        configurator.select(testItemUpdater, TestEnum.class,
                TestEnum::getStringRepresentation);

        Assertions.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assertions.assertEquals(column.getOptions(), listOptions);
        column.getItemUpdater().accept(initialItem, initialValue);
    }

    @Test
    void shouldConfigureSelectEditColumnEnumDefaultPreset() {
        Person initialItem = new Person(null, 0);
        String initialValue = "bar";

        testItemUpdater = (item, newValue) -> {
            Assertions.assertEquals(initialItem, item);
            Assertions.assertEquals(initialValue,
                    ((TestEnum) newValue).getStringRepresentation());
        };
        configurator.select(testItemUpdater, TestEnum.class);

        Assertions.assertEquals(column.getEditorType(),
                EditorType.SELECT.getTypeName());
        Assertions.assertEquals(column.getOptions(), listOptions);
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
