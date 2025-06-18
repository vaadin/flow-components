/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

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
