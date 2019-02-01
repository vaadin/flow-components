package com.vaadin.flow.component.gridpro;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

public class GridProEditColumnConfiguratorTest {

    EditColumnConfigurator configurator;
    ItemUpdater testItemUpdater;
    List<String> listOptions;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        testItemUpdater = (item, newValue) -> {};
        listOptions = new ArrayList<>();
        listOptions.add("foo");
        listOptions.add("bar");
        listOptions.add("baz");
    }

    @Test
    public void shouldConfigureTextEditColumnPreset() {
        configurator = EditColumnConfigurator.text(testItemUpdater);

        Assert.assertEquals(configurator.getType(), EditorType.TEXT);
        Assert.assertEquals(configurator.getItemUpdater(), testItemUpdater);
    }

    @Test
    public void shouldConfigureCheckboxEditColumnPreset() {
        Object initialItem = new Object();
        String initialValue = "true";

        testItemUpdater = (item, newValue) -> {
            Assert.assertEquals(initialItem, item);
            Assert.assertTrue(Boolean.parseBoolean(initialValue));
        };

        configurator = EditColumnConfigurator.checkbox(testItemUpdater);
        Assert.assertEquals(configurator.getType(), EditorType.CHECKBOX);
        configurator.getItemUpdater().accept(initialItem, initialValue);
    }

    @Test
    public void shouldConfigureSelectEditColumnPreset() {
        configurator = EditColumnConfigurator.select(testItemUpdater, listOptions);

        Assert.assertEquals(configurator.getType(), EditorType.SELECT);
        Assert.assertEquals(configurator.getItemUpdater(), testItemUpdater);
        Assert.assertEquals(configurator.getOptions(), listOptions);
    }

    @Test
    public void shouldConfigureSelectEditColumnEnumPreset() {
        configurator = EditColumnConfigurator.select(testItemUpdater, "foo", "bar", "baz");

        Assert.assertEquals(configurator.getType(), EditorType.SELECT);
        Assert.assertEquals(configurator.getItemUpdater(), testItemUpdater);
        Assert.assertEquals(configurator.getOptions(), listOptions);
    }

    @Test
    public void shouldConfigureSelectEditColumnEnumToStringPreset() {
        Object initialItem = new Object();
        String initialValue = "bar";

        testItemUpdater = (item, newValue) -> {
            Assert.assertEquals(initialItem, item);
            Assert.assertEquals(initialValue, ((testEnum) newValue).getStringRepresentation());
        };
        configurator = EditColumnConfigurator.select(testItemUpdater, testEnum.class, testEnum::getStringRepresentation);

        Assert.assertEquals(configurator.getType(), EditorType.SELECT);
        Assert.assertEquals(configurator.getOptions(), listOptions);
        configurator.getItemUpdater().accept(initialItem, initialValue);
    }

    @Test
    public void shouldConfigureSelectEditColumnEnumDefaultPreset() {
        Object initialItem = new Object();
        String initialValue = "bar";

        testItemUpdater = (item, newValue) -> {
            Assert.assertEquals(initialItem, item);
            Assert.assertEquals(initialValue, ((testEnum) newValue).getStringRepresentation());
        };
        configurator = EditColumnConfigurator.select(testItemUpdater, testEnum.class);

        Assert.assertEquals(configurator.getType(), EditorType.SELECT);
        Assert.assertEquals(configurator.getOptions(), listOptions);
        configurator.getItemUpdater().accept(initialItem, initialValue);
    }

    private enum testEnum {
        FOO("foo"), BAR("bar"), BAZ("baz");

        private String stringRepresentation;

        testEnum(String stringRepresentation) {
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
