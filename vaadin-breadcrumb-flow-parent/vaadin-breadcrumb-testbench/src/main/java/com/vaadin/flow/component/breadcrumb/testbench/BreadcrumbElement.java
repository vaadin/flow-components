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
package com.vaadin.flow.component.breadcrumb.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the {@code <vaadin-breadcrumb>} component.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-breadcrumb")
public class BreadcrumbElement extends TestBenchElement {

    /**
     * Gets all breadcrumb items in this breadcrumb.
     *
     * @return a list of all breadcrumb items
     */
    public List<BreadcrumbItemElement> getItems() {
        return $(BreadcrumbItemElement.class).all();
    }

    /**
     * Gets the breadcrumb item at the given index.
     *
     * @param index
     *            the index of the item to get
     * @return the breadcrumb item at the given index
     */
    public BreadcrumbItemElement getItem(int index) {
        return $(BreadcrumbItemElement.class).get(index);
    }

    /**
     * Gets the number of breadcrumb items.
     *
     * @return the number of items
     */
    public int getItemCount() {
        return getItems().size();
    }

    /**
     * Clicks on the breadcrumb item at the given index.
     *
     * @param index
     *            the index of the item to click
     */
    public void clickItem(int index) {
        getItem(index).click();
    }

    /**
     * Gets the text of the breadcrumb item at the given index.
     *
     * @param index
     *            the index of the item
     * @return the text of the item
     */
    public String getItemText(int index) {
        return getItem(index).getText();
    }

    /**
     * Checks if the breadcrumb has the given theme variant.
     *
     * @param variant
     *            the theme variant to check
     * @return {@code true} if the breadcrumb has the variant, {@code false}
     *         otherwise
     */
    public boolean hasThemeVariant(String variant) {
        String theme = getAttribute("theme");
        return theme != null && theme.contains(variant);
    }
}
