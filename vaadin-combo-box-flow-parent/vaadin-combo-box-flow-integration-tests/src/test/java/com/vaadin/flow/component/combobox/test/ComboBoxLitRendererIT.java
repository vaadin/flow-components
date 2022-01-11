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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/lit-renderer")
public class ComboBoxLitRendererIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void shouldRenderFirstItem() {
        combo.openPopup();
        assertHasItem("Lit", "Item 0");
    }

    @Test
    public void shouldRenderLastItem() {
        int itemCount = getItems(combo).size();
        combo.openPopup();
        scrollToItem(combo, itemCount - 1);
        assertHasItem("Lit", "Item " + (itemCount - 1));
    }

    @Test
    public void shouldSwitchToComponentRenderer() {
        clickElementWithJs("componentRendererButton");
        combo.openPopup();
        assertHasItem("Component", "Item 0");
    }

    @Test
    public void shouldSwitchBackToLitRenderer() {
        clickElementWithJs("componentRendererButton");
        clickElementWithJs("litRendererButton");
        combo.openPopup();
        assertHasItem("Lit", "Item 0");
    }

    private void assertHasItem(String type, String name) {
        Assert.assertTrue(getOverlayContents().stream().anyMatch(text -> {
            return text.contains(type) && text.contains(name);
        }));
    }
}
