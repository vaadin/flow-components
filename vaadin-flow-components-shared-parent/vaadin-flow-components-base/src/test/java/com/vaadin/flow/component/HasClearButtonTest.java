package com.vaadin.flow.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HasClearButtonTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialValue() {
        Assert.assertFalse(component.isClearButtonVisible());
    }

    @Test
    public void changeValue() {
        component.setClearButtonVisible(true);
        Assert.assertTrue(component.isClearButtonVisible());
        Assert.assertTrue(component.getElement()
                .getProperty("clearButtonVisible", false));

        component.setClearButtonVisible(false);
        Assert.assertFalse(component.isClearButtonVisible());
        Assert.assertFalse(component.getElement()
                .getProperty("clearButtonVisible", false));
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasClearButton {
    }
}
