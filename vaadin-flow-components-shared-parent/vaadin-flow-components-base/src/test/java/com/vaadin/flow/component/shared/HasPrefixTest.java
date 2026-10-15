/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasPrefixTest {

    @Tag("test")
    private static class TestComponent extends Component implements HasPrefix {
    }

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void getPrefix_noComponentByDefault() {
        Assert.assertNull(component.getPrefixComponent());
    }

    @Test
    public void setPrefix_replacesPrefix() {
        TestComponent foo = new TestComponent();
        component.setPrefixComponent(foo);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "prefix").count());
        Assert.assertEquals(foo, component.getPrefixComponent());

        TestComponent bar = new TestComponent();
        component.setPrefixComponent(bar);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "prefix").count());
        Assert.assertEquals(bar, component.getPrefixComponent());
    }

    @Test
    public void setPrefix_setPrefixNull_prefixRemoved() {
        component.setPrefixComponent(new TestComponent());
        component.setPrefixComponent(null);

        Assert.assertNull(component.getPrefixComponent());
        Assert.assertEquals(0,
                SlotUtils.getElementsInSlot(component, "prefix").count());
    }
}
