/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.customfield.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
