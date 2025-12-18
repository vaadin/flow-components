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
 * @deprecated Use {@link ContextMenuElement} instead.
 */
@Element("vaadin-context-menu")
@Deprecated(since = "25.0", forRemoval = true)
public class ContextMenuOverlayElement extends TestBenchElement {

    /**
     * Get the first menu item matching the caption.
     *
     * @return Optional menu item.
     * @deprecated Use {@link ContextMenuElement#getMenuItem(String)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public Optional<ContextMenuItemElement> getMenuItem(String caption) {
        return wrap(ContextMenuElement.class).getMenuItem(caption);
    }

    /**
     * Get the items of this context menu overlay.
     *
     * @return List of menu items.
     * @deprecated Use {@link ContextMenuElement#getMenuItems()} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public List<ContextMenuItemElement> getMenuItems() {
        return wrap(ContextMenuElement.class).getMenuItems();
    }
}
