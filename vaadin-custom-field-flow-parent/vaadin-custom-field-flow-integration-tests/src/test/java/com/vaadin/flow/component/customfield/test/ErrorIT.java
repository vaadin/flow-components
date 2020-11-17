package com.vaadin.flow.component.customfield.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;

public class ErrorIT extends AbstractParallelTest {
    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-custom-field") + "/error";
        getDriver().get(url);
    }

    @Test
    public void checkProperties() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
            .waitForFirst();
        Assert.assertEquals("My custom field", customField.getLabel());
        Assert.assertEquals("My error message", customField.getErrorMessage());

    }
}
