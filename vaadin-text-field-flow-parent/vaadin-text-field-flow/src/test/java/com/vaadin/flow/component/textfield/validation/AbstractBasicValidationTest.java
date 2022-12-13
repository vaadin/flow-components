package com.vaadin.flow.component.textfield.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;

public abstract class AbstractBasicValidationTest<T extends Component & HasValidation> {
    protected T testField;

    @Before
    public void setup() {
        testField = createTestField();
    }

    @Test
    public void setErrorMessage_getErrorMessage() {
        Assert.assertEquals(null, testField.getErrorMessage());
        Assert.assertEquals(null, testField.getElement().getProperty("errorMessage"));

        testField.setErrorMessage("Error");

        Assert.assertEquals("Error", testField.getErrorMessage());
        Assert.assertEquals("Error", testField.getElement().getProperty("errorMessage"));
    }

    @Test
    public void setInvalid_isInvalid() {
        Assert.assertEquals(false, testField.isInvalid());
        Assert.assertEquals("false", testField.getElement().getProperty("invalid"));

        testField.setInvalid(true);

        Assert.assertEquals(true, testField.isInvalid());
        Assert.assertEquals("true", testField.getElement().getProperty("invalid"));
    }

    abstract protected T createTestField();
}
