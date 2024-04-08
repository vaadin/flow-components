/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/item-class-name")
public class ComboBoxItemClassNameIT extends AbstractComponentIT {

    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).first();
    }

    @Test
    public void noClassesOnItemsSetInitially() {
        comboBox.openPopup();

        assertItemClassNames("", "", "");
    }

    @Test
    public void setClassNameGenerator_classesGenerated() {
        click("set-generator");
        comboBox.openPopup();

        assertItemClassNames("item-foo", "item-bar", "item-baz");
    }

    @Test
    public void changeClassNameGeneratorToReturnNull_classesRemoved() {
        click("set-generator");
        click("reset-generator");
        comboBox.openPopup();

        assertItemClassNames("", "", "");
    }

    private void assertItemClassNames(String... expectedClassNames) {
        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-combo-box-item");

        for (int i = 0; i < expectedClassNames.length; i++) {
            Assert.assertEquals(items.get(i).getAttribute("class"),
                    expectedClassNames[i]);
        }
    }

    private void click(String id) {
        $(TestBenchElement.class).id(id).click();
    }
}
