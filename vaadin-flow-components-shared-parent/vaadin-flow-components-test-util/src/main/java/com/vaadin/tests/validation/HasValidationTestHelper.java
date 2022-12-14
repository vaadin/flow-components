package com.vaadin.tests.validation;

import org.junit.Assert;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;

/**
 * Helper class that provides methods for testing that a component correctly
 * implements the {@link HasValidation} interface. Individual component tests
 * are supposed to have test methods calling the methods in here to run the test
 * logic.
 */
public class HasValidationTestHelper {
    public static <F extends Component & HasValidation> void setErrorMessage_getErrorMessage(
            F field) {
        Assert.assertNull(field.getErrorMessage());
        Assert.assertNull(field.getElement().getProperty("errorMessage"));

        field.setErrorMessage("Error");

        Assert.assertEquals("Error", field.getErrorMessage());
        Assert.assertEquals("Error",
                field.getElement().getProperty("errorMessage"));
    }

    public static <F extends Component & HasValidation> void setInvalid_isInvalid(
            F field) {
        Assert.assertFalse(field.isInvalid());
        Assert.assertFalse(field.getElement().getProperty("invalid", false));

        field.setInvalid(true);

        Assert.assertTrue(field.isInvalid());
        Assert.assertTrue(field.getElement().getProperty("invalid", false));
    }
}
