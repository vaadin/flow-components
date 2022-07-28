package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HasInputValueTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialValue() {
        Assert.assertFalse(component.isInputValuePopulated());
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasInputValue {
    }
}
