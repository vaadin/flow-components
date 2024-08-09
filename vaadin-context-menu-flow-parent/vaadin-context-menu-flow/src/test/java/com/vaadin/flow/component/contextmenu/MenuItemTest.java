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
package com.vaadin.flow.component.contextmenu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.HasAriaLabel;

/**
 * Unit tests for MenuItem.
 */
public class MenuItemTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @Before
    public void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
    }

    @Test(expected = IllegalStateException.class)
    public void nonCheckable_setChecked_throws() {
        item.setChecked(true);
    }

    @Test
    public void setCheckable_setChecked_isChecked() {
        item.setCheckable(true);
        Assert.assertFalse(item.isChecked());
        item.setChecked(true);
        Assert.assertTrue(item.isChecked());
    }

    @Test
    public void checked_setUnCheckable_unChecks() {
        item.setCheckable(true);
        item.setChecked(true);

        item.setCheckable(false);
        Assert.assertFalse(item.isCheckable());
    }

    @Test
    public void implementsHasAriaLabel() {
        Assert.assertTrue(item instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        item.setAriaLabel("aria-label");
        Assert.assertTrue(item.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", item.getAriaLabel().get());

        item.setAriaLabel(null);
        Assert.assertTrue(item.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        item.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(item.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", item.getAriaLabelledBy().get());

        item.setAriaLabelledBy(null);
        Assert.assertTrue(item.getAriaLabelledBy().isEmpty());
    }

}
