package com.vaadin.flow.component.customfield.test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ErrorIT extends AbstractParallelTest {
    @Before
    public void init() {
        getDriver().get(getBaseURL()+ "/error");
    }

    @Test
    public void checkProperties() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
            .first();
        Assert.assertEquals("My custom field", customField.getLabel());
        Assert.assertEquals("My error message", customField.getErrorMessage());

    }
}
