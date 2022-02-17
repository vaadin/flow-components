/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test.template;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/combo-box-in-template")
public class ComboBoxInTemplateIT extends AbstractComponentIT {

    private TestBenchElement message;
    private ComboBoxElement box1;
    private ComboBoxElement box2;

    @Before
    public void init() {
        open();
        message = $("label").id("message");
        box1 = $("wrapper-template").first().$("combo-box-in-a-template")
                .first().$(ComboBoxElement.class).first();
        box2 = $("wrapper-template").first().$("combo-box-in-a-template2")
                .first().$(ComboBoxElement.class).first();
    }

    @Test
    // Test for https://github.com/vaadin/flow/issues/4862
    public void twoLevelsOfTemplates_setValue_addValueChangeListener_noInitialValueChangeEvent() {
        Assert.assertEquals("Value change event should not be fired.", "-",
                message.getText());
    }

    @Test
    public void twoLevelsOfTemplates_valueChangeEventsFired() {
        box1.openPopup();
        box1.setProperty("value", "2");
        Assert.assertEquals("2", message.getText());

        box2.openPopup();
        box2.setProperty("value", "3");
        Assert.assertEquals("3", message.getText());

        box1.setProperty("value", "");
        Assert.assertEquals("null", message.getText());
    }

}
