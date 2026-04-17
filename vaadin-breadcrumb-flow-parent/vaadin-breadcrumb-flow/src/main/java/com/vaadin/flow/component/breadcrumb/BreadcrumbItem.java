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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.dom.Element;

/**
 * An individual item in a {@link Breadcrumb} trail.
 * <p>
 * Each item can have a label, a navigation path, and a prefix component (such
 * as an icon). Items can be marked as the current page in the trail.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-breadcrumb-item")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.2.0-alpha7")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb-item.js")
public class BreadcrumbItem extends Component
        implements HasPrefix, HasTooltip, HasEnabled {

    private Element labelElement;

    /**
     * Creates a breadcrumb item with the given label and no navigation path.
     *
     * @param label
     *            the label for the item
     */
    public BreadcrumbItem(String label) {
        setLabel(label);
    }

    /**
     * Creates a breadcrumb item with the given label that links to the given
     * path.
     *
     * @param label
     *            the label for the item
     * @param path
     *            the path to link to
     */
    public BreadcrumbItem(String label, String path) {
        setPath(path);
        setLabel(label);
    }

    /**
     * Creates a breadcrumb item with the given label and prefix component that
     * links to the given path.
     *
     * @param label
     *            the label for the item
     * @param path
     *            the path to link to
     * @param prefixComponent
     *            the prefix component for the item (usually an icon)
     */
    public BreadcrumbItem(String label, String path,
            Component prefixComponent) {
        setPath(path);
        setLabel(label);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Gets the label of this breadcrumb item.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return labelElement == null ? null : labelElement.getText();
    }

    /**
     * Sets the textual label for the item.
     * <p>
     * The label is also available for screen reader users.
     *
     * @param label
     *            the label text to set; or null to remove the label
     */
    public void setLabel(String label) {
        if (label == null) {
            removeLabelElement();
        } else {
            if (labelElement == null) {
                labelElement = createAndAppendLabelElement();
            }
            labelElement.setText(label);
        }
    }

    private Element createAndAppendLabelElement() {
        Element element = Element.createText("");
        getElement().appendChild(element);
        return element;
    }

    private void removeLabelElement() {
        if (labelElement != null) {
            getElement().removeChild(labelElement);
            labelElement = null;
        }
    }

    /**
     * Sets the path this breadcrumb item links to.
     *
     * @param path
     *            the path to link to, or null to make this item a non-link
     */
    public void setPath(String path) {
        if (path == null) {
            getElement().removeProperty("path");
        } else {
            getElement().setProperty("path", path);
        }
    }

    /**
     * Gets the path this breadcrumb item links to.
     *
     * @return the path, or null if no path has been set
     */
    public String getPath() {
        return getElement().getProperty("path");
    }

    /**
     * Sets whether this item represents the current page in the breadcrumb
     * trail.
     * <p>
     * When set to true, the item is rendered as non-interactive text and
     * receives {@code aria-current="page"} for accessibility.
     *
     * @param current
     *            true if this item is the current page, false otherwise
     */
    public void setCurrent(boolean current) {
        getElement().setProperty("current", current);
    }

    /**
     * Gets whether this item represents the current page in the breadcrumb
     * trail.
     *
     * @return true if this item is the current page, false otherwise
     */
    public boolean isCurrent() {
        return getElement().getProperty("current", false);
    }

    /**
     * Fluent convenience method that sets this item as the current page and
     * returns this instance.
     *
     * @return this item
     */
    public BreadcrumbItem asCurrent() {
        setCurrent(true);
        return this;
    }
}
