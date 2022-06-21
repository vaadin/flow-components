package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasAllowedCharPatternTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialValue() {
        Assert.assertEquals(component.getAllowedCharPattern(), "");
    }

    @Test
    public void changeValue() {
        component.setAllowedCharPattern("[-+\\d]");
        Assert.assertEquals(
                component.getElement().getProperty("allowedCharPattern"),
                "[-+\\d]");

        component.setAllowedCharPattern(null);
        Assert.assertEquals(
                component.getElement().getProperty("allowedCharPattern"), "");
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasAllowedCharPattern {
    }
}
