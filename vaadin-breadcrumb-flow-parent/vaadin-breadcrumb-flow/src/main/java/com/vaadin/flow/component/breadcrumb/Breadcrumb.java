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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.Registration;

@Tag("vaadin-breadcrumb")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.2.0-alpha7")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb.js")
public class Breadcrumb extends Component
        implements HasBreadcrumbItems, HasSize, HasStyle {

    private static final String SEPARATOR_SLOT_NAME = "separator";

    private BreadcrumbI18n i18n;

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

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(BreadcrumbI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public BreadcrumbI18n getI18n() {
        return i18n;
    }

    /**
     * Updates the i18n settings in the web component. Merges the
     * {@link BreadcrumbI18n} settings with the current / default settings of
     * the web component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(BreadcrumbI18n i18n) {
        Objects.requireNonNull(i18n,
                "The i18N properties object should not be null");
        this.i18n = i18n;
        getElement().setPropertyJson("i18n", JacksonUtils.beanToJson(i18n));
    }

    /**
     * Adds a listener for {@link NavigateEvent} events fired when a breadcrumb
     * item is activated.
     *
     * @param listener
     *            the listener to add
     * @return a registration for removing the listener
     */
    public Registration addNavigateListener(
            ComponentEventListener<NavigateEvent> listener) {
        return ComponentUtil.addListener(this, NavigateEvent.class, listener);
    }

    /**
     * Event fired when a breadcrumb item is activated by the user.
     */
    @DomEvent("navigate")
    public static class NavigateEvent extends ComponentEvent<Breadcrumb> {

        private final String path;
        private final boolean current;

        /**
         * Creates a new navigate event.
         *
         * @param source
         *            the breadcrumb that fired the event
         * @param fromClient
         *            whether the event originated from the client
         * @param path
         *            the path of the activated breadcrumb item
         * @param current
         *            whether the activated item is the current item
         */
        public NavigateEvent(Breadcrumb source, boolean fromClient,
                @EventData("event.detail.path") String path,
                @EventData("event.detail.current") boolean current) {
            super(source, fromClient);
            this.path = path;
            this.current = current;
        }

        /**
         * Gets the path of the activated breadcrumb item.
         *
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * Checks whether the activated item is the current item.
         *
         * @return {@code true} if the activated item is current
         */
        public boolean isCurrent() {
            return current;
        }
    }

    /**
     * The internationalization properties for {@link Breadcrumb}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BreadcrumbI18n implements Serializable {
        private String navigationLabel;
        private String overflow;

        /**
         * Gets the label for the breadcrumb navigation landmark.
         *
         * @return the navigation label
         */
        public String getNavigationLabel() {
            return navigationLabel;
        }

        /**
         * Sets the label for the breadcrumb navigation landmark.
         *
         * @param navigationLabel
         *            the navigation label
         * @return this instance for method chaining
         */
        public BreadcrumbI18n setNavigationLabel(String navigationLabel) {
            this.navigationLabel = navigationLabel;
            return this;
        }

        /**
         * Gets the label for the overflow button.
         *
         * @return the overflow label
         */
        public String getOverflow() {
            return overflow;
        }

        /**
         * Sets the label for the overflow button.
         *
         * @param overflow
         *            the overflow label
         * @return this instance for method chaining
         */
        public BreadcrumbI18n setOverflow(String overflow) {
            this.overflow = overflow;
            return this;
        }
    }
}
