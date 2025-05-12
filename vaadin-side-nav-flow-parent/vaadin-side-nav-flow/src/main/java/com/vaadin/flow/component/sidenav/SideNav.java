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
package com.vaadin.flow.component.sidenav;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JsonSerializer;

/**
 * A side navigation menu with support for hierarchical and flat menus.
 * <p>
 * Items can be added using {@link #addItem(SideNavItem...)} and hierarchy can
 * be created by adding {@link SideNavItem} instances to other
 * {@link SideNavItem} instances.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-side-nav")
@NpmPackage(value = "@vaadin/side-nav", version = "24.8.0-alpha18")
@JsModule("@vaadin/side-nav/src/vaadin-side-nav.js")
public class SideNav extends Component
        implements HasSideNavItems, HasSize, HasStyle {

    private Element labelElement;

    private SideNavI18n i18n;

    /**
     * Creates a new menu without any label.
     */
    public SideNav() {
    }

    /**
     * Creates a new menu with the given label.
     *
     * @param label
     *            the label to use
     */
    public SideNav(String label) {
        setLabel(label);
    }

    /**
     * Gets the label of this side navigation menu.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return labelElement == null ? null : labelElement.getText();
    }

    /**
     * Set a textual label for this side navigation menu.
     * <p>
     * This label can help the end user to distinguish groups of navigation
     * items. The label is also available for screen reader users.
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
        Element element = new Element("span");
        element.setAttribute("slot", "label");
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
     * Check if the end user is allowed to collapse/hide and expand/show the
     * navigation items.
     * <p>
     * NOTE: The navigation has to have a label for it to be collapsible.
     *
     * @return true if the menu is collapsible, false otherwise
     */
    public boolean isCollapsible() {
        return getElement().getProperty("collapsible", false);
    }

    /**
     * Allow the end user to collapse/hide and expand/show the navigation items.
     * <p>
     * NOTE: The navigation has to have a label for it to be collapsible.
     *
     * @param collapsible
     *            true to make the whole navigation component collapsible, false
     *            otherwise
     */
    public void setCollapsible(boolean collapsible) {
        getElement().setProperty("collapsible", collapsible);
    }

    /**
     * Returns whether the side navigation menu is expanded or collapsed.
     *
     * @return true if the side navigation menu is expanded, false if collapsed
     */
    @Synchronize(property = "collapsed", value = "collapsed-changed", allowInert = true)
    public boolean isExpanded() {
        return !getElement().getProperty("collapsed", false);
    }

    /**
     * Expands the side navigation menu.
     * <p>
     * If the side navigation menu does not have a label, does nothing.
     */
    public void setExpanded(boolean expanded) {
        getElement().setProperty("collapsed", !expanded);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using {@link #setI18n(SideNavI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public SideNavI18n getI18n() {
        return i18n;
    }

    /**
     * Updates the i18n settings in the web component. Merges the
     * {@link SideNavI18n} settings with the current / default settings of the
     * web component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(SideNavI18n i18n) {
        Objects.requireNonNull(i18n,
                "The i18N properties object should not be null");
        this.i18n = i18n;
        getElement().setPropertyJson("i18n", JsonSerializer.toJson(i18n));
    }

    /**
     * The internationalization properties for {@link SideNav}.
     */
    public static class SideNavI18n implements Serializable {
        private String toggle;

        /**
         * The text announced by screen readers when focusing the button for
         * toggling child items.
         *
         * @return the translated expression for toggling child items
         */
        public String getToggle() {
            return toggle;
        }

        /**
         * Sets the text announced by screen readers when focusing the button
         * for toggling child items.
         *
         * @param toggle
         *            the translated expression for toggling child items
         * @return this instance for method chaining
         */
        public SideNavI18n setToggle(String toggle) {
            this.toggle = toggle;
            return this;
        }
    }
}
