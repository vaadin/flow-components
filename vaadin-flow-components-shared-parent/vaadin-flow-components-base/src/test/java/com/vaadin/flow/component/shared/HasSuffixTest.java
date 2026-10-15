/**
 * Copyright 2000-2024 Vaadin Ltd.
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

public class HasSuffixTest {

    @Tag("test")
    private static class TestComponent extends Component implements HasSuffix {
    }

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void getSuffix_noComponentByDefault() {
        Assert.assertNull(component.getSuffixComponent());
    }

    @Test
    public void setSuffix_replacesSuffix() {
        TestComponent foo = new TestComponent();
        component.setSuffixComponent(foo);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "suffix").count());
        Assert.assertEquals(foo, component.getSuffixComponent());

        TestComponent bar = new TestComponent();
        component.setSuffixComponent(bar);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "suffix").count());
        Assert.assertEquals(bar, component.getSuffixComponent());
    }

    @Test
    public void setSuffix_setSuffixNull_suffixRemoved() {
        component.setSuffixComponent(new TestComponent());
        component.setSuffixComponent(null);

        Assert.assertNull(component.getSuffixComponent());
        Assert.assertEquals(0,
                SlotUtils.getElementsInSlot(component, "suffix").count());
    }
}
