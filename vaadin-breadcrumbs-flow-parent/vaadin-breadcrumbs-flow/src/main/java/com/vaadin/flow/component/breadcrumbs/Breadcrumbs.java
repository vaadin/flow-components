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
package com.vaadin.flow.component.breadcrumbs;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponentsOfType;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.signals.Signal;

/**
 * Breadcrumbs is a component for displaying a navigation trail that shows the
 * user's location within a hierarchy of pages.
 * <p>
 * This component is experimental and needs to be enabled with the
 * {@code com.vaadin.experimental.breadcrumbsComponent} feature flag.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-breadcrumbs")
@NpmPackage(value = "@vaadin/breadcrumbs", version = "25.2.0-beta2")
@JsModule("@vaadin/breadcrumbs/src/vaadin-breadcrumbs.js")
public class Breadcrumbs extends Component implements HasSize, HasStyle,
        HasAriaLabel, HasComponentsOfType<BreadcrumbsItem>,
        HasThemeVariant<BreadcrumbsVariant> {

    /**
     * The mode that determines how the breadcrumb trail is populated.
     */
    public enum Mode {
        /**
         * The trail is populated automatically from the active route hierarchy.
         */
        ROUTER,
        /**
         * The trail is populated manually by the application through
         * {@code add} / {@code remove} methods.
         */
        MANUAL
    }

    private Mode mode;

    private boolean routerUpdateInProgress;

    private BreadcrumbsI18n i18n;

    /**
     * Creates a new breadcrumbs component in {@link Mode#ROUTER} mode.
     */
    public Breadcrumbs() {
        this(Mode.ROUTER);
    }

    /**
     * Creates a new breadcrumbs component in the given mode.
     *
     * @param mode
     *            the mode that determines how the trail is populated, not
     *            {@code null}
     */
    public Breadcrumbs(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets the mode that determines how the breadcrumb trail is populated.
     *
     * @return the current mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the mode that determines how the breadcrumb trail is populated.
     * <p>
     * Switching to a different mode discards the existing children: both the
     * {@code ROUTER -> MANUAL} and {@code MANUAL -> ROUTER} transitions clear
     * the current trail so the new mode can start fresh. Setting the mode to
     * its current value is a no-op and leaves the children untouched.
     *
     * @param newMode
     *            the mode that determines how the trail is populated, not
     *            {@code null}
     */
    public void setMode(Mode newMode) {
        if (newMode == mode) {
            return;
        }
        this.mode = newMode;
        // Listener register/unregister and the initial router rebuild are
        // deferred to a later task; for now just clear the existing children.
        updateChildrenInternal(List.of());
    }

    @Override
    public void add(BreadcrumbsItem... components) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.add(components);
    }

    @Override
    public void add(Collection<BreadcrumbsItem> components) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.add(components);
    }

    @Override
    public void remove(BreadcrumbsItem... components) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.remove(components);
    }

    @Override
    public void remove(Collection<BreadcrumbsItem> components) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.remove(components);
    }

    @Override
    public void removeAll() {
        checkManualMutationAllowed();
        HasComponentsOfType.super.removeAll();
    }

    @Override
    public void addComponentAsFirst(BreadcrumbsItem component) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.addComponentAsFirst(component);
    }

    @Override
    public void addComponentAtIndex(int index, BreadcrumbsItem component) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.addComponentAtIndex(index, component);
    }

    @Override
    public void replace(BreadcrumbsItem oldComponent,
            BreadcrumbsItem newComponent) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.replace(oldComponent, newComponent);
    }

    @Override
    public <V extends @Nullable Object, S extends Signal<V>> void bindChildren(
            Signal<List<S>> list,
            SerializableFunction<S, BreadcrumbsItem> childFactory) {
        checkManualMutationAllowed();
        HasComponentsOfType.super.bindChildren(list, childFactory);
    }

    /**
     * Throws if manual child mutation is not allowed in the current mode.
     * <p>
     * Mutating children is rejected while in {@link Mode#ROUTER}, unless the
     * internal {@link #routerUpdateInProgress} flag is set (i.e. the change
     * originates from the router-driven rebuild via
     * {@link #updateChildrenInternal(List)}).
     */
    private void checkManualMutationAllowed() {
        if (mode == Mode.ROUTER && !routerUpdateInProgress) {
            throw new IllegalStateException(
                    "Cannot modify breadcrumb items manually in Mode.ROUTER. "
                            + "Switch to Mode.MANUAL to manage items directly.");
        }
    }

    /**
     * Replaces the current children with the given router-derived trail,
     * bypassing the {@link Mode#ROUTER} mutation guard for the duration of the
     * update.
     *
     * @param trail
     *            the breadcrumb items to set as the new children
     */
    void updateChildrenInternal(List<BreadcrumbsItem> trail) {
        routerUpdateInProgress = true;
        try {
            removeAll();
            add(trail.toArray(BreadcrumbsItem[]::new));
        } finally {
            routerUpdateInProgress = false;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        if (!featureFlags.isEnabled(
                BreadcrumbsFeatureFlagProvider.BREADCRUMBS_COMPONENT)) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(BreadcrumbsI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public BreadcrumbsI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(BreadcrumbsI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        getElement().setPropertyJson("i18n", JacksonUtils.beanToJson(i18n));
    }

    /**
     * The internationalization properties for {@link Breadcrumbs}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BreadcrumbsI18n implements Serializable {
        private String moreItems;

        /**
         * Gets the accessible label announced by screen readers for the
         * overflow button that reveals the hidden breadcrumb items.
         *
         * @return the translated label for the overflow button
         */
        public String getMoreItems() {
            return moreItems;
        }

        /**
         * Sets the accessible label announced by screen readers for the
         * overflow button that reveals the hidden breadcrumb items.
         *
         * @param moreItems
         *            the translated label for the overflow button
         * @return this instance for method chaining
         */
        public BreadcrumbsI18n setMoreItems(String moreItems) {
            this.moreItems = moreItems;
            return this;
        }
    }
}
