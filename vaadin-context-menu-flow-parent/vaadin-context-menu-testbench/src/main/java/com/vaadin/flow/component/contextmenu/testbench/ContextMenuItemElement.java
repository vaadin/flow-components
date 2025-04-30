/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.contextmenu.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-context-menu-item&gt;</code> element.
 *
 * @author Vaadin Ltd
 *
 */
@Element("vaadin-context-menu-item")
public class ContextMenuItemElement extends TestBenchElement {

    /**
     * Open the potential sub menu of this item by hovering. If there is a sub
     * menu, it can be used to access the sub menu items after opening the last
     * {@code ContextMenuOverlayElement}.
     */
    public void openSubMenu() {
        hover();
    }

    /**
     * Check if the item is checked.
     *
     * @return {@code true} if the item is checked.
     */
    public boolean isChecked() {
        return hasAttribute("menu-item-checked");
    }
}
