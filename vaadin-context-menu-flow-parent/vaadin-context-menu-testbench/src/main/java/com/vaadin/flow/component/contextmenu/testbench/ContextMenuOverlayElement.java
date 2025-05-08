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

import java.util.List;
import java.util.Optional;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-context-menu-overlay&gt;</code> element.
 *
 * @author Vaadin Ltd
 *
 */
@Element("vaadin-context-menu-overlay")
public class ContextMenuOverlayElement extends TestBenchElement {

    /**
     * Get the first menu item matching the caption.
     *
     * @return Optional menu item.
     */
    public Optional<ContextMenuItemElement> getMenuItem(String caption) {
        return getMenuItems().stream()
                .filter(item -> item.getText().equals(caption)).findFirst();
    }

    /**
     * Get the items of this context menu overlay.
     *
     * @return List of menu items.
     */
    public List<ContextMenuItemElement> getMenuItems() {
        return $(ContextMenuItemElement.class).all();
    }
}
