package com.vaadin.flow.component.gridpro;

import com.vaadin.flow.function.SerializableBiConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

public class GridProEditColumnConfiguratorTest {

    EditColumnConfigurator configurator;
    SerializableBiConsumer testConsumer;
    List<String> listOptions;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        testConsumer = (modifiedItem, columnPath) -> {};
        listOptions = new ArrayList<String>();
        listOptions.add("foo");
        listOptions.add("bar");
        listOptions.add("baz");
    }

    @Test
    public void setConfiguratorPreserveEditMode_getConfiguratorPreserveEditMode() {
        configurator = EditColumnConfigurator.text(testConsumer);

        configurator.setPreserveEditMode(true);
        Assert.assertEquals(configurator.getPreserveEditMode(), true);
    }

    @Test
    public void setConfiguratorAllowEnterRowChange_getConfiguratorAllowEnterRowChange() {
        configurator = EditColumnConfigurator.text(testConsumer);

        configurator.setAllowEnterRowChange(true);
        Assert.assertEquals(configurator.getAllowEnterRowChange(), true);
    }

    @Test
    public void shouldConfigureTextEditColumnPreset() {
        configurator = EditColumnConfigurator.text(testConsumer);

        Assert.assertEquals(configurator.getType(), EditorType.TEXT);
        Assert.assertEquals(configurator.getHandler(), testConsumer);
    }

    @Test
    public void shouldConfigureCheckboxEditColumnPreset() {
        configurator = EditColumnConfigurator.checkbox(testConsumer);

        Assert.assertEquals(configurator.getType(), EditorType.CHECKBOX);
        Assert.assertEquals(configurator.getHandler(), testConsumer);
    }

    @Test
    public void shouldConfigureSelectEditColumnPreset() {
        configurator = EditColumnConfigurator.select(testConsumer, listOptions);

        Assert.assertEquals(configurator.getType(), EditorType.SELECT);
        Assert.assertEquals(configurator.getHandler(), testConsumer);
        Assert.assertEquals(configurator.getOptions(), listOptions);
    }
}
