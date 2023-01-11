package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasValidationPropertiesTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialErrorMessage() {
        Assert.assertEquals(component.getErrorMessage(), "");
    }

    @Test
    public void changeErrorMessage() {
        component.setErrorMessage("This field is required");
        Assert.assertEquals(component.getElement().getProperty("errorMessage"),
                "This field is required");

        component.setErrorMessage(null);
        Assert.assertEquals(component.getElement().getProperty("errorMessage"),
                "");
    }

    @Test
    public void initialInvalid() {
        Assert.assertFalse(component.isInvalid());
    }

    @Test
    public void changeInvalid() {
        component.setInvalid(true);
        Assert.assertTrue(component.isInvalid());
        Assert.assertTrue(component.getElement().getProperty("invalid", false));

        component.setInvalid(false);
        Assert.assertFalse(component.isInvalid());
        Assert.assertFalse(
                component.getElement().getProperty("invalid", false));
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasValidationProperties {
    }
}
