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

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class MenuItemSignalTest extends AbstractSignalsUnitTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @Before
    public void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
        ui.add(contextMenu);
    }

    @Test(expected = IllegalStateException.class)
    public void bindEnabled_disableOnClickActive_throws() {
        item.setDisableOnClick(true);

        item.bindEnabled(new ValueSignal<>(true));
    }

    @Test(expected = IllegalStateException.class)
    public void setDisableOnClick_enabledBindingActive_throws() {
        item.bindEnabled(new ValueSignal<>(true));

        item.setDisableOnClick(true);
    }

    @Test
    public void setDisableOnClickFalse_enabledBindingActive_doesNotThrow() {
        item.bindEnabled(new ValueSignal<>(true));

        item.setDisableOnClick(false);
    }

    @Test
    public void bindEnabled_disableOnClickNotActive_doesNotThrow() {
        item.bindEnabled(new ValueSignal<>(true));
    }
}
