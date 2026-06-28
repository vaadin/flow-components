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
package com.vaadin.flow.component.breadcrumbs.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-breadcrumbs&gt;</code>
 * element.
 */
@Element("vaadin-breadcrumbs")
public class BreadcrumbsElement extends TestBenchElement {

    /**
     * Gets all breadcrumb items in the trail.
     *
     * @return list of all breadcrumb item elements
     */
    public List<BreadcrumbsItemElement> getItems() {
        return $(BreadcrumbsItemElement.class).all();
    }

    /**
     * Gets the item that represents the current page.
     *
     * @return the current breadcrumb item, or {@code null} if none
     */
    public BreadcrumbsItemElement getCurrentItem() {
        return getItems().stream().filter(BreadcrumbsItemElement::isCurrent)
                .findFirst().orElse(null);
    }

    /**
     * Gets the breadcrumb item whose visible text matches the given string.
     *
     * @param text
     *            the text to search for
     * @return the matching breadcrumb item, or {@code null} if not found
     */
    public BreadcrumbsItemElement getItemByText(String text) {
        return getItems().stream().filter(item -> text.equals(item.getText()))
                .findFirst().orElse(null);
    }

    /**
     * Gets the breadcrumb item whose path matches the given path.
     *
     * @param path
     *            the path to search for
     * @return the matching breadcrumb item, or {@code null} if not found
     */
    public BreadcrumbsItemElement getItemByPath(String path) {
        return getItems().stream().filter(item -> path.equals(item.getPath()))
                .findFirst().orElse(null);
    }
}
