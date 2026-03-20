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
package com.vaadin.flow.component.contextmenu;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasAriaLabel;

/**
 * Unit tests for MenuItem.
 */
class MenuItemTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @BeforeEach
    void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
    }

    @Test
    void nonCheckable_setChecked_throws() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> item.setChecked(true));
    }

    @Test
    void setCheckable_setChecked_isChecked() {
        item.setCheckable(true);
        Assertions.assertFalse(item.isChecked());
        item.setChecked(true);
        Assertions.assertTrue(item.isChecked());
    }

    @Test
    void checked_setUnCheckable_unChecks() {
        item.setCheckable(true);
        item.setChecked(true);

        item.setCheckable(false);
        Assertions.assertFalse(item.isCheckable());
    }

    @Test
    void implementsHasAriaLabel() {
        Assertions.assertTrue(item instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        item.setAriaLabel("aria-label");
        Assertions.assertTrue(item.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", item.getAriaLabel().get());

        item.setAriaLabel(null);
        Assertions.assertTrue(item.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        item.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(item.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                item.getAriaLabelledBy().get());

        item.setAriaLabelledBy(null);
        Assertions.assertTrue(item.getAriaLabelledBy().isEmpty());
    }

    @Test
    void disableOnClick_click_componentIsDisabled() {
        AtomicBoolean itemIsEnabled = new AtomicBoolean(true);

        item = contextMenu.addItem("foo",
                event -> itemIsEnabled.set(event.getSource().isEnabled()));
        item.setDisableOnClick(true);
        clickMenuItem(item);

        Assertions.assertFalse(itemIsEnabled.get());
    }

    @Test
    void disableOnClick_clickRevertsDisabled_componentIsEnabled() {
        item = contextMenu.addItem("foo",
                event -> event.getSource().setEnabled(true));
        item.setDisableOnClick(true);
        clickMenuItem(item);
        Assertions.assertTrue(item.isEnabled());
    }

    private static void clickMenuItem(MenuItem menuItem) {
        ComponentUtil.fireEvent(menuItem, new ClickEvent<>(menuItem, false, 0,
                0, 0, 0, 0, 0, false, false, false, false));
    }
}
