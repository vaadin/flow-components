/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.customfield.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
                .getHelperComponent().getDomAttribute("id"));

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
                .getHelperComponent().getDomAttribute("id"));
    }

}
