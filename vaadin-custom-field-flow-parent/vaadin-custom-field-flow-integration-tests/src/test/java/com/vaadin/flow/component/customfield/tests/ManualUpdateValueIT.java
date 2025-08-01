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
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-custom-field/manual")
public class ManualUpdateValueIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void valueIsUpdated() {
        final CustomFieldElement customField = $(CustomFieldElement.class)
                .waitForFirst();
        Assert.assertEquals("", $("div").id("result").getText());

        Assert.assertEquals("0", $("div").id("updateValueCounter").getText());

        IntegerFieldElement field1 = getById(customField, "field1");
        field1.setValue("1");
        IntegerFieldElement field2 = getById(customField, "field2");
        field2.setValue("2");
        Assert.assertEquals("0", $("div").id("updateValueCounter").getText());
        $("button").waitForFirst().click();
        executeScript(
                "!!document.activeElement ? document.activeElement.blur() : 0");
        waitUntil(e -> "3".equals($("div").id("result").getText()));
        Assert.assertEquals("1", $("div").id("updateValueCounter").getText());
    }

    private IntegerFieldElement getById(CustomFieldElement customField,
                                                     String id) {
        return customField.$(IntegerFieldElement.class).id(id);
    }
}
