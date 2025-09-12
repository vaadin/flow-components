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
package com.vaadin.flow.component.breadcrumb;

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
 * A single item in a {@link Breadcrumb} component.
 * <p>
 * BreadcrumbItem can contain text and/or components. It supports navigation
 * through href property or by using Router navigation targets.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-breadcrumb-item")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb-item.js")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.0.0-alpha16")
public class BreadcrumbItem extends Component
        implements HasComponents, HasText, HasStyle, HasEnabled, HasTooltip {

    private static final PropertyDescriptor<String, String> hrefDescriptor = PropertyDescriptors
            .propertyWithDefault("href", "");

    private static final PropertyDescriptor<String, String> targetDescriptor = PropertyDescriptors
            .propertyWithDefault("target", "");

    private static final PropertyDescriptor<Boolean, Boolean> routerIgnoreDescriptor = PropertyDescriptors
            .propertyWithDefault("routerIgnore", false);

    /**
     * Constructs an empty breadcrumb item.
     */
    public BreadcrumbItem() {
        super();
    }

    /**
     * Constructs a breadcrumb item with the given text.
     *
     * @param text
     *            the text content
     */
    public BreadcrumbItem(String text) {
        this();
        setText(text);
    }

    /**
     * Constructs a breadcrumb item with the given text and href.
     *
     * @param text
     *            the text content
     * @param href
     *            the href to navigate to
     */
    public BreadcrumbItem(String text, String href) {
        this(text);
        setHref(href);
    }

    /**
     * Constructs a breadcrumb item with the given component.
     *
     * @param component
     *            the component to add
     */
    public BreadcrumbItem(Component component) {
        this();
        add(component);
    }

    /**
     * Constructs a breadcrumb item with the given component and href.
     *
     * @param component
     *            the component to add
     * @param href
     *            the href to navigate to
     */
    public BreadcrumbItem(Component component, String href) {
        this(component);
        setHref(href);
    }

    /**
     * Constructs a breadcrumb item with the given text and navigation target.
     *
     * @param text
     *            the text content
     * @param navigationTarget
     *            the navigation target class
     */
    public BreadcrumbItem(String text,
            Class<? extends Component> navigationTarget) {
        this(text);
        setRoute(navigationTarget);
    }

    /**
     * Constructs a breadcrumb item with the given text, navigation target, and
     * route parameters.
     *
     * @param text
     *            the text content
     * @param navigationTarget
     *            the navigation target class
     * @param routeParameters
     *            the route parameters
     */
    public BreadcrumbItem(String text,
            Class<? extends Component> navigationTarget,
            RouteParameters routeParameters) {
        this(text);
        setRoute(navigationTarget, routeParameters);
    }

    /**
     * Gets the href of this item.
     *
     * @return the href, or an empty string if no href is set
     */
    public String getHref() {
        return get(hrefDescriptor);
    }

    /**
     * Sets the href of this item.
     * <p>
     * The href is the URL that the item links to. Set to an empty string to
     * remove the href.
     *
     * @param href
     *            the href to set, or an empty string to remove
     */
    public void setHref(String href) {
        set(hrefDescriptor, href == null ? "" : href);
    }

    /**
     * Gets the target of this item's link.
     *
     * @return the target, or an empty string if no target is set
     */
    public String getTarget() {
        return get(targetDescriptor);
    }

    /**
     * Sets the target of this item's link.
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
     * Gets whether this item should be ignored by client-side routers.
     *
     * @return {@code true} if router should ignore this item, {@code false}
     *         otherwise
     */
    public boolean isRouterIgnore() {
        return get(routerIgnoreDescriptor);
    }

    /**
     * Sets whether this item should be ignored by client-side routers.
     * <p>
     * When set to {@code true}, clicking this item will cause a full page
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
     * Gets whether this item represents the current page.
     * <p>
     * This property is automatically updated based on the current URL.
     *
     * @return {@code true} if this item represents the current page,
     *         {@code false} otherwise
     */
    @Synchronize(property = "current", value = "current-changed")
    public boolean isCurrent() {
        return getElement().getProperty("current", false);
    }

    /**
     * Sets the navigation target for this item using a router class.
     *
     * @param navigationTarget
     *            the navigation target class
     */
    public void setRoute(Class<? extends Component> navigationTarget) {
        setRoute(navigationTarget, RouteParameters.empty());
    }

    /**
     * Sets the navigation target for this item using a router class and route
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
     * Sets the navigation target for this item using a router class, route
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
            RouteParameters routeParameters, QueryParameters queryParameters) {
        setRoute(getRouter(), navigationTarget, routeParameters,
                queryParameters);
    }

    /**
     * Sets the navigation target for this item using a specific router.
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
            RouteParameters routeParameters, QueryParameters queryParameters) {
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
