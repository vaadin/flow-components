package com.vaadin.flow.component.customfield.test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NameFieldViewIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-custom-field") + "/custom-field-helper";
        getDriver().get(url);
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

        Assert.assertEquals("helper-component",
              customFieldHelperComponent.getHelperComponent()
                    .getAttribute("id"));
        $("button").id("button-clear-helper-component").click();

        Assert.assertNull(customFieldHelperComponent.getHelperComponent());
    }

}