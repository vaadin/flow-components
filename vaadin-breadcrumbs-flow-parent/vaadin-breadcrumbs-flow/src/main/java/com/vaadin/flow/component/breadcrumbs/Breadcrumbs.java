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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponentsOfType;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.SignalBindingFeature;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouteReference;
import com.vaadin.flow.router.RouterState;
import com.vaadin.flow.shared.Registration;
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
@NpmPackage(value = "@vaadin/breadcrumbs", version = "25.2.0")
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

    private boolean internalChildUpdate;

    private BreadcrumbsI18n i18n;

    private Registration navigationRegistration;

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
     * @throws IllegalStateException
     *             if a children binding set via {@code bindChildren} is active;
     *             such a binding takes over the trail and cannot be handed back
     *             to component-controlled population
     */
    public void setMode(Mode newMode) {
        if (newMode == mode) {
            return;
        }
        if (hasChildrenBinding()) {
            throw new IllegalStateException(
                    "Cannot change the mode while a children binding is active.");
        }
        this.mode = newMode;
        // Clear the current trail so the new mode starts fresh.
        updateChildrenInternal(List.of());
        if (newMode == Mode.ROUTER) {
            // MANUAL -> ROUTER: start listening for navigation.
            if (isAttached()) {
                registerNavigationListener(getUI().orElseThrow());
            }
        } else {
            // ROUTER -> MANUAL: stop listening for navigation.
            unregisterNavigationListener();
        }
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
     * internal {@link #internalChildUpdate} flag is set (i.e. the change
     * originates from an internal child update such as a mode switch or a
     * router-driven rebuild via {@link #updateChildrenInternal(List)}).
     */
    private void checkManualMutationAllowed() {
        if (mode == Mode.ROUTER && !internalChildUpdate) {
            throw new IllegalStateException(
                    "Cannot modify breadcrumb items manually in Mode.ROUTER. "
                            + "Switch to Mode.MANUAL to manage items directly.");
        }
    }

    /**
     * Replaces the current children with the given trail, bypassing the
     * {@link Mode#ROUTER} mutation guard for the duration of the update. Used
     * both for the router-driven rebuild and for clearing children on a mode
     * switch.
     *
     * @param trail
     *            the breadcrumb items to set as the new children
     */
    void updateChildrenInternal(List<BreadcrumbsItem> trail) {
        internalChildUpdate = true;
        try {
            removeAll();
            add(trail.toArray(BreadcrumbsItem[]::new));
        } finally {
            internalChildUpdate = false;
        }
    }

    /**
     * Checks whether a children binding (set via {@code bindChildren}) is
     * currently active on this component's element. Mirrors the internal check
     * Flow core performs, reading the binding state from the element node.
     *
     * @return {@code true} if a children binding is active
     */
    private boolean hasChildrenBinding() {
        return getElement().getNode()
                .getFeatureIfInitialized(SignalBindingFeature.class)
                .map(feature -> feature
                        .hasBinding(SignalBindingFeature.CHILDREN))
                .orElse(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());

        if (mode == Mode.ROUTER) {
            registerNavigationListener(attachEvent.getUI());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        unregisterNavigationListener();
        super.onDetach(detachEvent);
    }

    /**
     * Registers the {@code AfterNavigationListener} that rebuilds the trail on
     * each navigation and performs an initial synchronous rebuild from the
     * current navigation state.
     *
     * @param ui
     *            the UI to register the listener on
     */
    private void registerNavigationListener(UI ui) {
        navigationRegistration = ui
                .addAfterNavigationListener(this::rebuildFromRouter);

        // Initial population: read the current navigation state from the UI for
        // the case where the breadcrumbs is attached to an already-rendered
        // view, so no navigation event is pending.
        rebuildFromRouter(ui.routerStateSignal().peek());
    }

    /**
     * Unregisters the {@code AfterNavigationListener} if it is currently
     * registered.
     */
    private void unregisterNavigationListener() {
        if (navigationRegistration != null) {
            navigationRegistration.remove();
            navigationRegistration = null;
        }
    }

    /**
     * Rebuilds the trail from the navigation state carried by an
     * {@link AfterNavigationEvent}. Registered as an
     * {@code AfterNavigationListener} while in {@link Mode#ROUTER}.
     *
     * @param event
     *            the navigation event
     */
    void rebuildFromRouter(AfterNavigationEvent event) {
        if (!isAttached()) {
            return;
        }
        // The active chain is ordered leaf-first, so the current view is the
        // first element.
        List<HasElement> activeChain = event.getActiveChain();
        HasElement currentView = activeChain.isEmpty() ? null
                : activeChain.get(0);
        Class<? extends Component> currentTarget = currentView instanceof Component
                ? ((Component) currentView).getClass()
                : null;
        rebuildTrail(currentTarget, event.getRouteParameters(),
                event.getLocation().getQueryParameters(), currentView);
    }

    /**
     * Rebuilds the trail from the given {@link RouterState}. Used for the
     * initial synchronous rebuild in {@link #onAttach(AttachEvent)} and on a
     * {@code MANUAL -> ROUTER} mode switch.
     *
     * @param state
     *            the current router state
     */
    void rebuildFromRouter(RouterState state) {
        if (!isAttached()) {
            return;
        }
        rebuildTrail(state.navigationTarget(), state.routeParameters(),
                state.location().getQueryParameters(),
                state.currentView().orElse(null));
    }

    /**
     * Builds the breadcrumb trail for the given current navigation target and
     * applies it via {@link #updateChildrenInternal(List)}.
     * <p>
     * The trail is produced by {@link RouteConfiguration#getRouteHierarchy}
     * (the breadcrumb does no walking of its own). Ancestor labels are resolved
     * without instantiating their views; the last (current) item prefers the
     * live {@link HasDynamicTitle} of the already-instantiated current view.
     * <p>
     * The query parameters of the current navigation are applied only when
     * resolving the current (last) item's title; ancestor titles and links are
     * resolved without query parameters, since query parameters describe the
     * current navigation as a whole and ancestor links never carry them.
     *
     * @param currentTarget
     *            the current navigation target class, or {@code null} if it
     *            cannot be resolved
     * @param parameters
     *            the route parameters of the current navigation
     * @param queryParameters
     *            the query parameters of the current navigation, applied to the
     *            current item's title resolution only
     * @param currentView
     *            the current view instance, or {@code null} if not available
     */
    private void rebuildTrail(Class<? extends Component> currentTarget,
            RouteParameters parameters, QueryParameters queryParameters,
            HasElement currentView) {
        if (currentTarget == null) {
            updateChildrenInternal(List.of());
            return;
        }

        RouteConfiguration routeConfiguration = RouteConfiguration
                .forRegistry(ComponentUtil.getRouter(this).getRegistry());
        List<RouteReference> hierarchy = routeConfiguration
                .getRouteHierarchy(currentTarget, parameters);

        List<BreadcrumbsItem> trail = new ArrayList<>(hierarchy.size());
        for (int i = 0; i < hierarchy.size(); i++) {
            RouteReference reference = hierarchy.get(i);
            boolean isLast = i == hierarchy.size() - 1;
            if (isLast) {
                trail.add(new BreadcrumbsItem(resolveCurrentTitle(reference,
                        queryParameters, currentView)));
            } else {
                String title = resolveTitle(reference, QueryParameters.empty());
                trail.add(
                        new BreadcrumbsItem(title, reference.navigationTarget(),
                                reference.routeParameters()));
            }
        }

        updateChildrenInternal(trail);
    }

    /**
     * Resolves the label of the current (last) route, preferring the live
     * {@link HasDynamicTitle} of the already-instantiated current view over the
     * instance-free title resolution. The given query parameters are passed to
     * the instance-free resolution so a query-parameter-dependent title
     * generator resolves correctly.
     */
    private String resolveCurrentTitle(RouteReference reference,
            QueryParameters queryParameters, HasElement currentView) {
        if (currentView instanceof HasDynamicTitle) {
            return ((HasDynamicTitle) currentView).getPageTitle();
        }
        return resolveTitle(reference, queryParameters);
    }

    /**
     * Resolves the label of a route without instantiating its view, using the
     * given query parameters and falling back to an empty string when the route
     * declares no title.
     */
    private String resolveTitle(RouteReference reference,
            QueryParameters queryParameters) {
        return ComponentUtil.getRouter(this)
                .resolvePageTitle(reference.navigationTarget(),
                        reference.routeParameters(), queryParameters)
                .orElse("");
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
