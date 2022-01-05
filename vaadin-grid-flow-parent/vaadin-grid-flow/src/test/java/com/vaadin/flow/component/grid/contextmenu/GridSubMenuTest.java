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
package com.vaadin.flow.component.grid.contextmenu;

import com.vaadin.flow.component.contextmenu.MenuManager;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableRunnable;

public class GridSubMenuTest {

    private MenuManager menuManager = Mockito.mock(MenuManager.class);

    private class TestContextMenu extends GridSubMenu<Object> {

        public TestContextMenu() {
            super(Mockito.mock(GridMenuItem.class),
                    Mockito.mock(SerializableRunnable.class));
        }

        @Override
        protected MenuManager<GridContextMenu<Object>, GridMenuItem<Object>, GridSubMenu<Object>> createMenuManager() {
            return menuManager;
        }
    }

    @Test
    public void addTextItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        menu.addItem("foo", null);

        Mockito.verify(menuManager).addItem("foo");
    }

    @Test
    public void addComponentItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        Component component = Mockito.mock(Component.class);
        menu.addItem(component, null);

        Mockito.verify(menuManager).addItem(component);
    }
}
