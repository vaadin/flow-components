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
package com.vaadin.flow.component.stepper;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinService;

/**
 * A single step in a {@link Stepper} component.
 * <p>
 * Step can contain text and/or components. It supports navigation through href
 * property or by using Router navigation targets. Steps can have different
 * states to indicate progress through the stepper process.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-step")
@JsModule("@vaadin/stepper/src/vaadin-step.js")
@NpmPackage(value = "@vaadin/stepper", version = "25.0.0-dev")
public class Step extends Component
        implements HasComponents, HasText, HasStyle, HasEnabled, HasTooltip {

    private static final PropertyDescriptor<String, String> hrefDescriptor = PropertyDescriptors
            .propertyWithDefault("href", "");

    private static final PropertyDescriptor<String, String> targetDescriptor = PropertyDescriptors
            .propertyWithDefault("target", "");

    private static final PropertyDescriptor<String, String> labelDescriptor = PropertyDescriptors
            .propertyWithDefault("label", "");

    private static final PropertyDescriptor<String, String> descriptionDescriptor = PropertyDescriptors
            .propertyWithDefault("description", "");

    private static final PropertyDescriptor<String, String> stateDescriptor = PropertyDescriptors
            .propertyWithDefault("state", State.INACTIVE.getValue());

    private static final PropertyDescriptor<Boolean, Boolean> routerIgnoreDescriptor = PropertyDescriptors
            .propertyWithDefault("routerIgnore", false);

    /**
     * Enumeration of possible step states.
     */
    public enum State {
        ACTIVE("active"), COMPLETED("completed"), ERROR("error"), INACTIVE(
                "inactive");

        private final String value;

        State(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static State fromValue(String value) {
            for (State state : values()) {
                if (state.getValue().equals(value)) {
                    return state;
                }
            }
            return INACTIVE;
        }
    }

    /**
     * Constructs an empty step.
     */
    public Step() {
        super();
    }

    /**
     * Constructs a step with the given label.
     *
     * @param label
     *            the label text
     */
    public Step(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs a step with the given label and description.
     *
     * @param label
     *            the label text
     * @param description
     *            the description text
     */
    public Step(String label, String description) {
        this(label);
        setDescription(description);
    }

    /**
     * Constructs a step with the given component.
     *
     * @param component
     *            the component to add
     */
    public Step(Component component) {
        this();
        add(component);
    }

    /**
     * Creates a step with the given label and href.
     *
     * @param label
     *            the label text
     * @param href
     *            the href to navigate to
     * @return a new step with the specified label and href
     */
    public static Step withHref(String label, String href) {
        Step step = new Step(label);
        step.setHref(href);
        return step;
    }

    /**
     * Creates a step with the given component and href.
     *
     * @param component
     *            the component to add
     * @param href
     *            the href to navigate to
     * @return a new step with the specified component and href
     */
    public static Step withHref(Component component, String href) {
        Step step = new Step(component);
        step.setHref(href);
        return step;
    }

    /**
     * Constructs a step with the given label and navigation target.
     *
     * @param label
     *            the label text
     * @param navigationTarget
     *            the navigation target class
     */
    public Step(String label, Class<? extends Component> navigationTarget) {
        this(label);
        setRoute(navigationTarget);
    }

    /**
     * Constructs a step with the given label, navigation target, and route
     * parameters.
     *
     * @param label
     *            the label text
     * @param navigationTarget
     *            the navigation target class
     * @param routeParameters
     *            the route parameters
     */
    public Step(String label, Class<? extends Component> navigationTarget,
            RouteParameters routeParameters) {
        this(label);
        setRoute(navigationTarget, routeParameters);
    }

    /**
     * Gets the href of this step.
     *
     * @return the href, or an empty string if no href is set
     */
    public String getHref() {
        return get(hrefDescriptor);
    }

    /**
     * Sets the href of this step.
     * <p>
     * The href is the URL that the step links to. Set to an empty string to
     * remove the href.
     *
     * @param href
     *            the href to set, or an empty string to remove
     */
    public void setHref(String href) {
        set(hrefDescriptor, href == null ? "" : href);
    }

    /**
     * Gets the target of this step's link.
     *
     * @return the target, or an empty string if no target is set
     */
    public String getTarget() {
        return get(targetDescriptor);
    }

    /**
     * Sets the target of this step's link.
     * <p>
     * The target attribute specifies where to display the linked URL. Common
     * values are "_blank" to open in a new tab, "_self" to open in the same
     * frame, "_parent" to open in the parent frame, or "_top" to open in the
     * full window.
     *
     * @param target
     *            the target to set, or an empty string to remove
     */
    public void setTarget(String target) {
        set(targetDescriptor, target == null ? "" : target);
    }

    /**
     * Gets the label of this step.
     *
     * @return the label text
     */
    public String getLabel() {
        return get(labelDescriptor);
    }

    /**
     * Sets the label of this step.
     *
     * @param label
     *            the label text to set
     */
    public void setLabel(String label) {
        set(labelDescriptor, label == null ? "" : label);
    }

    /**
     * Gets the description of this step.
     *
     * @return the description text
     */
    public String getDescription() {
        return get(descriptionDescriptor);
    }

    /**
     * Sets the description of this step.
     *
     * @param description
     *            the description text to set
     */
    public void setDescription(String description) {
        set(descriptionDescriptor, description == null ? "" : description);
    }

    /**
     * Gets the state of this step.
     *
     * @return the current state
     */
    @Synchronize(property = "state", value = "state-changed")
    public State getState() {
        return State.fromValue(get(stateDescriptor));
    }

    /**
     * Sets the state of this step.
     *
     * @param state
     *            the state to set
     */
    public void setState(State state) {
        set(stateDescriptor, state == null ? State.INACTIVE.getValue()
                : state.getValue());
    }

    /**
     * Gets whether this step should be ignored by client-side routers.
     *
     * @return {@code true} if router should ignore this step, {@code false}
     *         otherwise
     */
    public boolean isRouterIgnore() {
        return get(routerIgnoreDescriptor);
    }

    /**
     * Sets whether this step should be ignored by client-side routers.
     * <p>
     * When set to {@code true}, clicking this step will cause a full page
     * reload instead of client-side navigation.
     *
     * @param routerIgnore
     *            {@code true} to ignore client-side routing, {@code false}
     *            otherwise
     */
    public void setRouterIgnore(boolean routerIgnore) {
        set(routerIgnoreDescriptor, routerIgnore);
    }

    /**
     * Gets whether this step represents the current page.
     * <p>
     * This property is automatically updated based on the current URL.
     *
     * @return {@code true} if this step represents the current page,
     *         {@code false} otherwise
     */
    @Synchronize(property = "current", value = "current-changed")
    public boolean isCurrent() {
        return getElement().getProperty("current", false);
    }

    /**
     * Gets whether this step is completed.
     *
     * @return {@code true} if the step is completed, {@code false} otherwise
     */
    public boolean isCompleted() {
        return getState() == State.COMPLETED;
    }

    /**
     * Sets this step as completed.
     */
    public void setCompleted() {
        setState(State.COMPLETED);
    }

    /**
     * Gets whether this step is active.
     *
     * @return {@code true} if the step is active, {@code false} otherwise
     */
    public boolean isActive() {
        return getState() == State.ACTIVE;
    }

    /**
     * Sets this step as active.
     */
    public void setActive() {
        setState(State.ACTIVE);
    }

    /**
     * Gets whether this step has an error.
     *
     * @return {@code true} if the step has an error, {@code false} otherwise
     */
    public boolean isError() {
        return getState() == State.ERROR;
    }

    /**
     * Sets this step as having an error.
     */
    public void setError() {
        setState(State.ERROR);
    }

    /**
     * Gets whether this step is inactive.
     *
     * @return {@code true} if the step is inactive, {@code false} otherwise
     */
    public boolean isInactive() {
        return getState() == State.INACTIVE;
    }

    /**
     * Sets this step as inactive.
     */
    public void setInactive() {
        setState(State.INACTIVE);
    }

    /**
     * Sets the navigation target for this step using a router class.
     *
     * @param navigationTarget
     *            the navigation target class
     */
    public void setRoute(Class<? extends Component> navigationTarget) {
        setRoute(navigationTarget, RouteParameters.empty());
    }

    /**
     * Sets the navigation target for this step using a router class and route
     * parameters.
     *
     * @param navigationTarget
     *            the navigation target class
     * @param routeParameters
     *            the route parameters
     */
    public void setRoute(Class<? extends Component> navigationTarget,
            RouteParameters routeParameters) {
        setRoute(getRouter(), navigationTarget, routeParameters,
                QueryParameters.empty());
    }

    /**
     * Sets the navigation target for this step using a router class, route
     * parameters, and query parameters.
     *
     * @param navigationTarget
     *            the navigation target class
     * @param routeParameters
     *            the route parameters
     * @param queryParameters
     *            the query parameters
     */
    public void setRoute(Class<? extends Component> navigationTarget,
            RouteParameters routeParameters,
            QueryParameters queryParameters) {
        setRoute(getRouter(), navigationTarget, routeParameters,
                queryParameters);
    }

    /**
     * Sets the navigation target for this step using a specific router.
     *
     * @param router
     *            the router to use, or {@code null} to use the default
     * @param navigationTarget
     *            the navigation target class
     * @param routeParameters
     *            the route parameters
     * @param queryParameters
     *            the query parameters
     */
    public void setRoute(Router router,
            Class<? extends Component> navigationTarget,
            RouteParameters routeParameters,
            QueryParameters queryParameters) {
        if (router == null) {
            router = getRouter();
        }

        if (navigationTarget != null) {
            String url = RouteConfiguration.forRegistry(router.getRegistry())
                    .getUrl(navigationTarget, routeParameters);

            if (!queryParameters.getParameters().isEmpty()) {
                url = url + "?" + queryParameters.getQueryString();
            }

            setHref(url);
        } else {
            setHref("");
        }
    }

    private Router getRouter() {
        Router router = null;
        if (getElement().getNode().isAttached()) {
            router = getUI().map(ui -> ui.getInternals().getRouter())
                    .orElse(null);
        }
        if (router == null) {
            router = VaadinService.getCurrent().getRouter();
        }
        if (router == null) {
            throw new IllegalStateException(
                    "Cannot find a router to use for navigation");
        }
        return router;
    }
}