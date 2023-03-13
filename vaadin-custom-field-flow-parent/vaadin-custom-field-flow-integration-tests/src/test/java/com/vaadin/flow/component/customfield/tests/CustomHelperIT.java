package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-custom-field/custom-helper")
public class CustomHelperIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void assertHelperText() {
        final CustomFieldElement customFieldHelper = $(CustomFieldElement.class)
                .id("custom-field-helper-text");

        Assert.assertEquals("Helper text", customFieldHelper.getHelperText());
        $("button").id("button-clear-helper").click();

        Assert.assertEquals("", customFieldHelper.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        final CustomFieldElement customFieldHelperComponent = $(
                CustomFieldElement.class).id("custom-field-helper-component");

        Assert.assertEquals("helper-component", customFieldHelperComponent
                .getHelperComponent().getAttribute("id"));

        $("button").id("button-clear-helper-component").click();

        Assert.assertEquals(
                "Removing the helper component should revert to helper text if set",
                "Your full first and last names",
                customFieldHelperComponent.getHelperComponent().getText());
    }

    @Test
    public void assertHelperComponentLazy() {
        final CustomFieldElement customFieldHelperComponent = $(
                CustomFieldElement.class)
                .id("custom-field-helper-component-lazy");

        $("button").id("button-add-helper-component").click();

        Assert.assertEquals("helper-component-lazy", customFieldHelperComponent
                .getHelperComponent().getAttribute("id"));
    }

}
