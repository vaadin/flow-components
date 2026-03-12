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
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox/selection-preservation")
public class SelectionPreservationIT extends AbstractComponentIT {

    private CheckboxGroupElement group;

    @Before
    public void init() {
        open();
        group = $(CheckboxGroupElement.class).single();
    }

    @Test
    public void preserveAll_selectMultiple_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-preserve-all");

        group.selectByText("4");
        group.selectByText("5");
        group.selectByText("6");

        clickElementWithJs("refresh-all");

        Assert.assertEquals(List.of("4", "5", "6"), group.getSelectedTexts());
    }

    @Test
    public void preserveExisting_selectMultiple_refreshAll_selectionPreserved() {
        clickElementWithJs("mode-preserve-existing");

        group.selectByText("2");
        group.selectByText("7");

        clickElementWithJs("refresh-all");

        Assert.assertEquals(List.of("2", "7"), group.getSelectedTexts());
    }

    @Test
    public void preserveAll_removeSelectedItem_refreshAll_serverValueStillContainsRemovedItem() {
        clickElementWithJs("mode-preserve-all");

        group.selectByText("4");
        group.selectByText("5");
        group.selectByText("6");

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("4,5,6", getServerValue());
        Assert.assertEquals(List.of("4", "6"), group.getSelectedTexts());
    }

    @Test
    public void preserveExisting_removeSelectedItem_refreshAll_removedItemDeselected() {
        clickElementWithJs("mode-preserve-existing");

        group.selectByText("4");
        group.selectByText("5");
        group.selectByText("6");

        clickElementWithJs("remove-item-5");
        clickElementWithJs("refresh-all");

        Assert.assertEquals("4,6", getServerValue());
        Assert.assertEquals(List.of("4", "6"), group.getSelectedTexts());
    }

    @Test
    public void discard_selectMultiple_refreshAll_selectionCleared() {
        clickElementWithJs("mode-discard");

        group.selectByText("1");
        group.selectByText("3");

        clickElementWithJs("refresh-all");

        Assert.assertTrue(group.getSelectedTexts().isEmpty());
    }

    private String getServerValue() {
        clickElementWithJs("show-server-value");
        return $("span").id("server-value").getText();
    }
}
