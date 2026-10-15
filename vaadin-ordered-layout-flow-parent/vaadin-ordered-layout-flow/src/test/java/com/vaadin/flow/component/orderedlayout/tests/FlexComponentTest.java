/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

public class FlexComponentTest {

    @Test
    public void setFlexShrink() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);
        component.setFlexShrink(2, div);

        Assert.assertEquals("should set flex-shrink",
                component.getFlexShrink(div), 2, 0);
    }

    @Test
    public void getFlexShrink_returnOneIfNotSet() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);

        Assert.assertEquals("should return 1 if flex-shirk not set",
                component.getFlexShrink(div), 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFlexShrink_throwExceptionIfNegative() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);
        component.setFlexShrink(-1, div);
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements FlexComponent {
    }
}
