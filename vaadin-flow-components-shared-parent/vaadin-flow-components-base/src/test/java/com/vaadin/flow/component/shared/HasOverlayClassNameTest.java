package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasOverlayClassNameTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialValue() {
        Assert.assertEquals(component.getOverlayClassName(), "");
    }

    @Test
    public void changeValue() {
        component.setOverlayClassName("foo bar");
        Assert.assertEquals(component.getElement().getProperty("overlayClass"),
                "foo bar");

        component.setOverlayClassName(null);
        Assert.assertEquals(component.getElement().getProperty("overlayClass"),
                "");
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasOverlayClassName {
    }
}
