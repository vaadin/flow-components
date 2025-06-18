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
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-custom-field")
public class BasicIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void valueIsUpdated() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
                .waitForFirst();
        Assert.assertEquals("",
                $("div").attribute("id", "result").get(0).getText());
        TextFieldElement field1 = getById(customField, "field1");
        field1.setValue("1");
        TextFieldElement field2 = getById(customField, "field2");
        field2.setValue("2");
        $("button").waitForFirst().click();
        executeScript(
                "!!document.activeElement ? document.activeElement.blur() : 0");
        waitUntil(e -> "3"
                .equals($("div").attribute("id", "result").get(0).getText()));
    }

    private TextFieldElement getById(CustomFieldElement customField,
            String id) {
        return customField.$(TextFieldElement.class).attribute("id", id)
                .waitForFirst();
    }
}
