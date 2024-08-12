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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/detach-reattach")
public class DetachReattachIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void detachComboBox_reattach_noClientErrors() {
        clickButton("detach");
        clickButton("attach");
        checkLogsForErrors();
    }

    @Test
    public void detachComboBox_reattachRedetach_noClientErrors() {
        clickButton("detach");
        clickButton("attach-detach");
        checkLogsForErrors();
    }

    @Test
    public void openComboBox_detach_reattach_open_itemsLoaded() {
        combo.openPopup();
        assertRendered("foo");
        clickButton("detach");
        clickButton("attach");
        combo = $(ComboBoxElement.class).first();
        combo.openPopup();
        assertLoadedItemsCount("Expected 2 items to be loaded", 2, combo);
    }

    @Test
    public void setValueFromServer_selectOtherValue_detach_reattach_valueNotChanged() {
        clickButton("set-value");
        assertValueChanges("foo");

        combo.openPopup();
        combo.selectByText("bar");
        assertValueChanges("foo", "bar");

        clickButton("detach");
        clickButton("attach");
        assertValueChanges("foo", "bar");
    }

    @Test
    public void setValueFromClient_detach_reattach_hasSelectedItem() {
        combo.selectByText("foo");
        clickButton("detach");
        clickButton("attach");

        combo = $(ComboBoxElement.class).first();
        Assert.assertEquals("foo", combo.getInputElementValue());
    }

    @Test
    public void withComponentRenderer_renderComponentsInitially_detachAndReattach_componentRenderersRestored() {
        clickButton("set-component-renderer");

        combo.openPopup();
        combo.closePopup();

        clickButton("detach-attach");

        combo = $(ComboBoxElement.class).waitForFirst();
        combo.openPopup();

        TestBenchElement overlay = $("vaadin-combo-box-overlay").waitForFirst();
        List<TestBenchElement> items = overlay.$("vaadin-combo-box-item").all();
        items.forEach(item -> Assert.assertTrue(item.$("label").exists()));
    }

    private void assertValueChanges(String... expected) {
        String[] valueChanges = $("div").id("value-changes").$("p").all()
                .stream().map(TestBenchElement::getText).toArray(String[]::new);
        Assert.assertArrayEquals(expected, valueChanges);
    }

}
