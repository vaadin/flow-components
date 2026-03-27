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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class MenuItemSignalTest extends AbstractSignalsTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @BeforeEach
    void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
        ui.add(contextMenu);
    }

    @Test
    void bindEnabled_disableOnClickActive_throws() {
        item.setDisableOnClick(true);

        Assertions.assertThrows(IllegalStateException.class,
                () -> item.bindEnabled(new ValueSignal<>(true)));
    }

    @Test
    void setDisableOnClick_enabledBindingActive_throws() {
        item.bindEnabled(new ValueSignal<>(true));

        Assertions.assertThrows(IllegalStateException.class,
                () -> item.setDisableOnClick(true));
    }

    @Test
    void setDisableOnClickFalse_enabledBindingActive_doesNotThrow() {
        item.bindEnabled(new ValueSignal<>(true));

        item.setDisableOnClick(false);
    }

    @Test
    void bindEnabled_disableOnClickNotActive_doesNotThrow() {
        item.bindEnabled(new ValueSignal<>(true));
    }
}
