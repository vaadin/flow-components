package com.vaadin.flow.component.customfield.tests;

import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;

@TestPath("vaadin-custom-field/error")
public class ErrorIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void checkProperties() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
                .waitForFirst();
        Assert.assertEquals("My custom field", customField.getLabel());
        Assert.assertEquals("My error message", customField.getErrorMessage());

    }
}
