/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-radio-button/selection-preservation")
public class SelectionPreservationIT extends AbstractComponentIT {

    private RadioButtonGroupElement group;

    @Before
    public void init() {
        open();
        group = $(RadioButtonGroupElement.class).single();
    }

    @Test
    public void preserveAll_selectItem_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-preserve-all");

        group.selectByText("5");

        clickElementWithJs("refresh-all");

        Assert.assertEquals("5", group.getSelectedText());
    }

    @Test
    public void preserveExisting_selectItem_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-preserve-existing");

        group.selectByText("3");

        clickElementWithJs("refresh-all");

        Assert.assertEquals("3", group.getSelectedText());
    }

    @Test
    public void preserveAll_removeSelectedItem_refreshAll_serverValueStillPreserved() {
        clickElementWithJs("mode-preserve-all");

        group.selectByText("5");

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("5", getServerValue());
        Assert.assertNull(group.getSelectedText());
    }

    @Test
    public void preserveExisting_removeSelectedItem_refreshAll_selectionCleared() {
        clickElementWithJs("mode-preserve-existing");

        group.selectByText("5");

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("", getServerValue());
        Assert.assertNull(group.getSelectedText());
    }

    @Test
    public void discard_selectItem_refreshAll_selectionCleared() {
        clickElementWithJs("mode-discard");

        group.selectByText("7");

        clickElementWithJs("refresh-all");

        Assert.assertNull(group.getSelectedText());
    }

    private String getServerValue() {
        clickElementWithJs("show-server-value");
        return $("span").id("server-value").getText();
    }
}
