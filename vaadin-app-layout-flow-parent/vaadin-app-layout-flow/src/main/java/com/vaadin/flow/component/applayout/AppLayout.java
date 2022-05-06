package com.vaadin.flow.component.applayout;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/*
 * #%L
 * Vaadin App Layout
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.router.RouterLayout;

import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * App Layout is a component for building common application layouts.
 * <p>
 * The layout consists of three sections: a horizontal navigation bar (navbar),
 * a collapsible navigation drawer (drawer) and a content area. An application’s
 * main navigation blocks should be positioned in the navbar and/or drawer while
 * views are rendered in the content area.
 * <p>
 * App Layout is responsive and adjusts automatically to fit desktop, tablet,
 * and mobile screen sizes.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-app-layout")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/app-layout", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-app-layout", version = "23.1.0-beta1")
@JsModule("@vaadin/app-layout/src/vaadin-app-layout.js")
public class AppLayout extends Component implements RouterLayout, HasStyle {
    private static final PropertyDescriptor<String, String> primarySectionProperty = PropertyDescriptors
            .propertyWithDefault("primarySection",
                    Section.NAVBAR.toWebcomponentValue());
    private static final PropertyDescriptor<Boolean, Boolean> overlayProperty = PropertyDescriptors
            .propertyWithDefault("overlay", false);

    private Component content;

    private AppLayoutI18n i18n;

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the i18n object that is returned from this method will not
     * update the the component, unless it is set again using
     * {@link AppLayout#setI18n(AppLayoutI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, if the i18n
     *         properties are not set.
     */
    public AppLayoutI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(AppLayoutI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;

        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                setI18nWithJS();
            }
        });
    }

    private void setI18nWithJS() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(this.i18n);

        // Remove properties with null values to prevent errors in web
        // component
        removeNullValuesFromJsonObject(i18nJson);

        // Assign new I18N object to WC, by merging the existing
        // WC I18N, and the values from the new AppLayoutI18n instance,
        // into an empty object
        getElement().executeJs("this.i18n = Object.assign({}, this.i18n, $0);",
                i18nJson);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Element state is not persisted across attach/detach
        if (this.i18n != null) {
            setI18nWithJS();
        }
    }

    private void removeNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * @see #setPrimarySection(Section)
     * @return value for the primarySection property. Default is
     *         {@link Section#NAVBAR}.
     */
    @Synchronize("primary-section-changed")
    public Section getPrimarySection() {
        return Section.fromWebcomponentValue(primarySectionProperty.get(this));
    }

    /**
     * Defines whether navbar or drawer will come first visually.
     *
     * <ul>
     * <li>If {@link Section#NAVBAR}, the navbar takes the full available width
     * and moves the drawer down. This is the default.</li>
     * <li>If {@link Section#DRAWER} is set, then the drawer will move the
     * navbar, taking the full available height.</li>
     * </ul>
     *
     * @param primarySection
     *            new value for the primarySection property. Not {@code null}.
     * @throws NullPointerException
     *             if primarySection is {@code null}.
     */
    public void setPrimarySection(Section primarySection) {
        Objects.requireNonNull(primarySection,
                "primary section must not be null");
        primarySectionProperty.set(this, primarySection.toWebcomponentValue());
    }

    /**
     * Whether the drawer is opened (visible) or not. Its default value depends
     * on the viewport:
     * <ul>
     * <li>{@code true} for desktop size views</li>
     * <li>{@code false} for mobile size views</li>
     * </ul>
     *
     * @return {@code true} if the drawer is opened (visible). {@code false}
     *         otherwise.
     */
    @Synchronize("drawer-opened-changed")
    public boolean isDrawerOpened() {
        return getElement().getProperty("drawerOpened", true);
    }

    /**
     * Server-side API for showing and hiding the drawer.
     *
     * @param drawerOpened
     *            new value for the drawerOpened property.
     * @see #isDrawerOpened
     * @see DrawerToggle for a component that allows the user to open and close
     *      the drawer.
     */
    public void setDrawerOpened(boolean drawerOpened) {
        getElement().setProperty("drawerOpened", drawerOpened);
    }

    /**
     * <strong>Note:</strong> This property is controlled via CSS and can not be
     * changed directly.
     *
     * @return {@code true} if drawer is an overlay on top of the content.
     *         {@code false} otherwise.
     */
    @Synchronize("overlay-changed")
    public boolean isOverlay() {
        return overlayProperty.get(this);
    }

    /**
     * @return the displayed content
     */
    public Component getContent() {
        return content;
    }

    /**
     * Sets the displayed content.
     *
     * @param content
     *            {@link Component} to display in the content area
     */
    public void setContent(Component content) {

        removeContent();

        if (content != null) {
            this.content = content;
            content.getElement().removeAttribute("slot");
            add(content);
        }
    }

    /**
     * Adds the components to the <em>drawer</em> slot of this AppLayout.
     *
     * @param components
     *            Components to add to the drawer slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToDrawer(Component... components) {
        addToSlot("drawer", components);
    }

    /**
     * Adds the components to the <em>navbar</em> slot of this AppLayout.
     *
     * @param components
     *            Components to add to the navbar slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToNavbar(Component... components) {
        final boolean touchOptimized = false;
        addToNavbar(touchOptimized, components);
    }

    /**
     * Adds the components to the <em>navbar</em> slot of this AppLayout.
     *
     * @param touchOptimized
     *            if true, the components will be moved to the bottom navbar
     *            area on mobile devices.
     * @param components
     *            Components to add to the navbar slot.
     * @throws NullPointerException
     *             if any of the components is null or if the components array
     *             is null.
     */
    public void addToNavbar(boolean touchOptimized, Component... components) {
        final String slot = "navbar"
                + (touchOptimized ? " touch-optimized" : "");
        addToSlot(slot, components);
    }

    /**
     * Removes the child components from the parent. Components can be in any
     * slot or be the main content.
     *
     * @param components
     *            Components to remove.
     */
    public void remove(Component... components) {
        for (Component component : components) {
            if (this.content != null && this.content.equals(component)) {
                this.content = null;
            }
            remove(component);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     *             if content is not a {@link Component}
     */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        Component target = null;
        if (content != null) {
            target = content.getElement().getComponent()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "AppLayout content must be a Component"));
        }
        setContent(target);
        afterNavigation();
    }

    /**
     * Called after a navigation event. The default behaviour is to close the
     * drawer on mobile devices after a navigation event.
     */
    protected void afterNavigation() {
        // Close drawer after navigation on mobile devices.
        if (isOverlay()) {
            setDrawerOpened(false);
        }
    }

    private void addToSlot(String slot, Component... components) {
        for (Component component : components) {
            setSlot(component, slot);
            add(component);
        }
    }

    private void add(Component component) {
        getElement().appendChild(component.getElement());
    }

    private static void setSlot(Component component, String slot) {
        component.getElement().setAttribute("slot", slot);
    }

    /**
     * Removes the displayed content.
     */
    private void removeContent() {
        remove(this.content);
        this.content = null;
    }

    private void remove(Component component) {
        if (component != null) {
            component.getElement().removeFromParent();
        }
    }

    /**
     * Sections in the component that can be used as primary.
     *
     * @see #setPrimarySection(Section)
     */
    public enum Section {
        NAVBAR, DRAWER;

        public String toWebcomponentValue() {
            return this.name().toLowerCase(Locale.ENGLISH);
        }

        public static Section fromWebcomponentValue(String webcomponentValue) {
            return webcomponentValue != null
                    ? valueOf(webcomponentValue.toUpperCase())
                    : null;
        }
    }

    /**
     * The internationalization properties for {@link AppLayout}
     */
    public static class AppLayoutI18n implements Serializable {
        private String drawer;

        /**
         * Gets the text for the `aria-label` attribute on the drawer.
         *
         * @return the drawer aria-label
         */
        public String getDrawer() {
            return drawer;
        }

        /**
         * Sets the text for the `aria-label` attribute on the drawer. The
         * attribute is set when the drawer is in the overlay mode and announced
         * once the drawer is opened.
         *
         * @param drawer
         *            the drawer aria-label
         * @return this instance for method chaining
         */
        public AppLayoutI18n setDrawer(String drawer) {
            this.drawer = drawer;
            return this;
        }
    }
}
