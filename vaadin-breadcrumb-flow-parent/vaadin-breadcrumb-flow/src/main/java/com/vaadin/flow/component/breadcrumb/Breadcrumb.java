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
package com.vaadin.flow.component.breadcrumb;

import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.SlotUtils;

@Tag("vaadin-breadcrumb")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.2.0-alpha7")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb.js")
public class Breadcrumb extends Component
        implements HasBreadcrumbItems, HasSize, HasStyle {

    private static final String SEPARATOR_SLOT_NAME = "separator";

    /**
     * Replaces all current breadcrumb items with the given items.
     *
     * @param items
     *            the breadcrumb items to set
     */
    public void setItems(BreadcrumbItem... items) {
        Objects.requireNonNull(items, "Items must not be null");
        removeAll();
        addItem(items);
    }

    /**
     * Replaces all current breadcrumb items with the given list of items.
     *
     * @param items
     *            the breadcrumb items to set
     */
    public void setItems(List<BreadcrumbItem> items) {
        Objects.requireNonNull(items, "Items must not be null");
        removeAll();
        addItem(items.toArray(new BreadcrumbItem[0]));
    }

    /**
     * Sets a custom separator component to be used between breadcrumb items.
     * The component is placed in the {@code separator} slot.
     * <p>
     * Passing {@code null} removes any existing separator.
     *
     * @param separator
     *            the separator component, or {@code null} to remove
     */
    public void setSeparator(Component separator) {
        SlotUtils.setSlot(this, SEPARATOR_SLOT_NAME, separator);
    }

    /**
     * Gets the current separator component.
     *
     * @return the separator component, or {@code null} if none is set
     */
    public Component getSeparator() {
        return SlotUtils.getChildInSlot(this, SEPARATOR_SLOT_NAME);
    }
}
