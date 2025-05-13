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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.UrlUtil;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.internal.ConfigureRoutes;
import com.vaadin.flow.router.internal.HasUrlParameterFormat;

import elemental.json.JsonArray;

/**
 * A menu item for the {@link SideNav} component.
 * <p>
 * Besides the target {@code path} it can contain a label, prefix and suffix
 * component, like icons or badges. You can create a hierarchical navigation
 * structure by adding other {@link SideNavItem} instances to this
 * {@link SideNavItem} instance via {@link #addItem(SideNavItem...)}.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-side-nav-item")
@NpmPackage(value = "@vaadin/side-nav", version = "24.8.0-alpha18")
@JsModule("@vaadin/side-nav/src/vaadin-side-nav-item.js")
public class SideNavItem extends Component
        implements HasSideNavItems, HasEnabled, HasPrefix, HasSuffix {

    private Element labelElement;

    private QueryParameters queryParameters;

    /**
     * Creates a menu item which does not link to any view but only shows the
     * given label.
     *
     * @param label
     *            the label for the item
     */
    public SideNavItem(String label) {
        setLabel(label);
    }

    /**
     * Creates a new menu item using the given label that links to the given
     * path.
     *
     * @param label
     *            the label for the item
     * @param path
     *            the path to link to
     */
    public SideNavItem(String label, String path) {
        setPath(path);
        setLabel(label);
    }

    /**
     * Creates a new menu item using the given label that links to the given
     * view.
     *
     * @param label
     *            the label for the item
     * @param view
     *            the view to link to
     */
    public SideNavItem(String label, Class<? extends Component> view) {
        setPath(view);
        setLabel(label);
    }

    /**
     * Creates a new menu item using the given label that links to the given
     * view, which must implement {@link HasUrlParameter}.
     *
     * @param label
     *            the label for the item
     * @param view
     *            the view to link to, must implement {@link HasUrlParameter}
     * @param parameter
     *            the URL parameter for the view
     * @param <T>
     *            the type of the URL parameter
     * @param <C>
     *            the type of the view
     */
    public <T, C extends Component & HasUrlParameter<T>> SideNavItem(
            String label, Class<? extends C> view, T parameter) {
        this(label, view, HasUrlParameterFormat.getParameters(parameter));
    }

    /**
     * Creates a new menu item using the given label that links to the given
     * view.
     *
     * @param label
     *            the label for the item
     * @param routeParameters
     *            the route parameters
     * @param view
     *            the view to link to
     */
    public SideNavItem(String label, Class<? extends Component> view,
            RouteParameters routeParameters) {
        setPath(view, routeParameters);
        setLabel(label);
    }

    /**
     * Creates a new menu item using the given label and prefix component (like
     * an icon) that links to the given path.
     *
     * @param label
     *            the label for the item
     * @param path
     *            the path to link to
     * @param prefixComponent
     *            the prefix component for the item (usually an icon)
     */
    public SideNavItem(String label, String path, Component prefixComponent) {
        setPath(path);
        setLabel(label);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Creates a new menu item using the given label and prefix component (like
     * an icon) that links to the given view.
     *
     * @param label
     *            the label for the item
     * @param view
     *            the view to link to
     * @param prefixComponent
     *            the prefixComponent for the item (usually an icon)
     */
    public SideNavItem(String label, Class<? extends Component> view,
            Component prefixComponent) {
        setPath(view);
        setLabel(label);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Creates a new menu item using the given label and prefix component (like
     * an icon) that links to the given view.
     *
     * @param label
     *            the label for the item
     * @param view
     *            the view to link to
     * @param routeParameters
     *            the route parameters
     * @param prefixComponent
     *            the prefixComponent for the item (usually an icon)
     */
    public SideNavItem(String label, Class<? extends Component> view,
            RouteParameters routeParameters, Component prefixComponent) {
        setPath(view, routeParameters);
        setLabel(label);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Gets the label of this menu item.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return labelElement == null ? null : labelElement.getText();
    }

    /**
     * Set a textual label for the item.
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
     * Sets the path in a form or a URL string this navigation item links to.
     * Note that there is also an alternative way of how to set the link path
     * via {@link SideNavItem#setPath(Class)}.
     *
     * @param path
     *            The path to link to. Set to null to disable navigation for
     *            this item.
     *
     * @see SideNavItem#setPath(Class)
     */
    public void setPath(String path) {
        if (path == null) {
            getElement().removeAttribute("path");
        } else {
            getElement().setAttribute("path",
                    sanitizePath(updateQueryParameters(path)));
        }
    }

    /**
     * Retrieves {@link com.vaadin.flow.router.Route} and
     * {@link com.vaadin.flow.router.RouteAlias} annotations from the specified
     * view, and then sets the corresponding path and path aliases for this
     * item.
     * <p>
     * Note: Vaadin Router will be used to determine the URL path of the view
     * and this URL will be then set to this navigation item using
     * {@link SideNavItem#setPath(String)}.
     *
     * @param view
     *            The view to link to. The view should be annotated with the
     *            {@link com.vaadin.flow.router.Route} annotation. Set to null
     *            to disable navigation for this item.
     *
     * @see SideNavItem#setPath(String)
     */
    public void setPath(Class<? extends Component> view) {
        setPath(view, RouteParameters.empty());
    }

    /**
     * Retrieves {@link com.vaadin.flow.router.Route} and
     * {@link com.vaadin.flow.router.RouteAlias} annotations from the specified
     * view, and then sets the corresponding path and path aliases for this
     * item.
     * <p>
     * Note: Vaadin Router will be used to determine the URL path of the view
     * and this URL will be then set to this navigation item using
     * {@link SideNavItem#setPath(String)}.
     *
     * @param view
     *            The view to link to. The view should be annotated with the
     *            {@link com.vaadin.flow.router.Route} annotation and must
     *            implement {@link HasUrlParameter}. Set to null to disable
     *            navigation for this item.
     * @param parameter
     *            the URL parameter for the view
     * @param <T>
     *            the type of the URL parameter
     * @param <C>
     *            the type of the view
     */
    public <T, C extends Component & HasUrlParameter<T>> void setPath(
            Class<? extends C> view, T parameter) {
        setPath(view, HasUrlParameterFormat.getParameters(parameter));
    }

    /**
     * Retrieves {@link com.vaadin.flow.router.Route} and
     * {@link com.vaadin.flow.router.RouteAlias} annotations from the specified
     * view, and then sets the corresponding path and path aliases for this
     * item.
     * <p>
     * Note: Vaadin Router will be used to determine the URL path of the view
     * and this URL will be then set to this navigation item using
     * {@link SideNavItem#setPath(String)}.
     *
     * @param view
     *            The view to link to. The view should be annotated with the
     *            {@link com.vaadin.flow.router.Route} annotation. Set to null
     *            to disable navigation for this item.
     * @param routeParameters
     *            the route parameters
     *
     * @see SideNavItem#setPath(String)
     * @see SideNavItem#setPathAliases(Set)
     */
    public void setPath(Class<? extends Component> view,
            RouteParameters routeParameters) {
        if (view == null) {
            setPath((String) null);
            setPathAliases(Collections.emptySet());
        } else {
            RouteConfiguration routeConfiguration = RouteConfiguration
                    .forRegistry(ComponentUtil.getRouter(this).getRegistry());
            setPath(routeConfiguration.getUrl(view, routeParameters));
            setPathAliases(getPathAliasesFromView(view, routeParameters));
        }
    }

    /**
     * Gets the path this navigation item links to.
     *
     * @return path this navigation item links to
     */
    public String getPath() {
        return getElement().getAttribute("path");
    }

    /**
     * Sets the {@link QueryParameters} of this item.
     * <p>
     * The query string will be generated from
     * {@link QueryParameters#getQueryString()} and will be appended to the
     * {@code path} attribute of this item.
     *
     * @param queryParameters
     *            the query parameters object, or {@code null} to remove
     *            existing query parameters
     */
    public void setQueryParameters(QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
        // Apply new query parameters to the path
        setPath(getPath());
    }

    /**
     * Gets the path aliases for this item.
     *
     * @return the path aliases for this item, empty if none
     */
    public Set<String> getPathAliases() {
        JsonArray pathAliases = (JsonArray) getElement()
                .getPropertyRaw("pathAliases");
        if (pathAliases == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(
                JsonSerializer.toObjects(String.class, pathAliases));
    }

    /**
     * Sets the specified path aliases to this item. The aliases act as
     * secondary paths when determining the active state of the item.
     * <p>
     * Note that it is allowed to pass {@code null} as value to clear the
     * selection.
     *
     * @param pathAliases
     *            the path aliases to set to this item
     */
    public void setPathAliases(Set<String> pathAliases) {
        if (pathAliases == null || pathAliases.isEmpty()) {
            getElement().removeProperty("pathAliases");
        } else {
            JsonArray aliasesAsJson = JsonSerializer.toJson(pathAliases.stream()
                    .map(alias -> Objects.requireNonNull(alias,
                            "Alias to set cannot be null"))
                    .map(this::updateQueryParameters).map(this::sanitizePath)
                    .collect(Collectors.toSet()));
            getElement().setPropertyJson("pathAliases", aliasesAsJson);
        }
    }

    /**
     * Gets the target of this item.
     *
     * @return the target of this item
     */
    public String getTarget() {
        return getElement().getProperty("target");
    }

    /**
     * Where to display the linked URL, as the name for a browsing context.
     * <p>
     * The following keywords have special meanings for where to load the URL:
     * <ul>
     * <li><code>_self</code>: the current browsing context. (Default)</li>
     * <li><code>_blank</code>: usually a new tab, but users can configure
     * browsers to open a new window instead.</li>
     * <li><code>_parent</code>: the parent browsing context of the current one.
     * If no parent, behaves as <code>_self</code>.</li>
     * <li><code>_top</code>: the topmost browsing context (the "highest"
     * context that’s an ancestor of the current one). If no ancestors, behaves
     * as <code>_self</code>.</li>
     * </ul>
     * </p>
     *
     * @param target
     *            the target of this item
     */
    public void setTarget(String target) {
        if (target == null) {
            getElement().removeProperty("target");
        } else {
            getElement().setProperty("target", target);
        }
    }

    /**
     * Gets whether this item also matches nested paths / routes.
     *
     * @return true if this item also matches nested paths / routes, false
     *         otherwise
     */
    public boolean isMatchNested() {
        return getElement().getProperty("matchNested", false);
    }

    /**
     * Sets whether to also match nested paths / routes. {@code false} by
     * default.
     * <p>
     * When enabled, an item with the path {@code /path} is considered current
     * when the browser URL is {@code /path}, {@code /path/child},
     * {@code /path/child/grandchild}, etc.
     * <p>
     * Note that this only affects matching of the URLs path, not the base
     * origin or query parameters.
     *
     * @param value
     *            true to also match nested paths / routes, false otherwise
     */
    public void setMatchNested(boolean value) {
        getElement().setProperty("matchNested", value);
    }

    /**
     * @return true if this item should be ignored by the Vaadin router and
     *         behave like a regular anchor.
     */
    public boolean isRouterIgnore() {
        return getElement().getProperty("routerIgnore", false);
    }

    /**
     * The routing mechanism in Vaadin by default intercepts all side nav items
     * with a relative URL. This method can be used to make the router ignore
     * this item. This makes it behave like a regular anchor, causing a full
     * page load.
     *
     * @param ignore
     *            true if this item should not be intercepted by the single-page
     *            web application routing mechanism in Vaadin.
     */
    public void setRouterIgnore(boolean ignore) {
        getElement().setProperty("routerIgnore", ignore);
    }

    /**
     * Sets whether the target URL should be opened in a new browser tab.
     * <p>
     * This is a convenience method for setting the target to
     * <code>_blank</code>. See {@link #setTarget(String)} for more information.
     * </p>
     *
     * @param openInNewBrowserTab
     *            true if the target URL should be opened in a new browser tab,
     *            false otherwise
     */
    public void setOpenInNewBrowserTab(boolean openInNewBrowserTab) {
        setTarget(openInNewBrowserTab ? "_blank" : null);
    }

    /**
     * Gets whether the target URL should be opened in a new browser tab.
     *
     * @return true if the target URL should be opened in a new browser tab,
     *         false otherwise
     */
    public boolean isOpenInNewBrowserTab() {
        return "_blank".equals(getTarget());
    }

    private Set<String> getPathAliasesFromView(Class<? extends Component> view,
            RouteParameters routeParameters) {
        RouteAlias[] routeAliases = view.getAnnotationsByType(RouteAlias.class);
        return Arrays.stream(routeAliases).map(RouteAlias::value).map(
                alias -> updateAliasWithRouteParameters(alias, routeParameters))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private String updateAliasWithRouteParameters(String alias,
            RouteParameters routeParameters) {
        if (!alias.contains(":")) {
            return alias;
        }
        ConfigureRoutes configuredAliases = new ConfigureRoutes();
        configuredAliases.setRoute(alias, getClass());
        return configuredAliases.getTargetUrl(getClass(),
                getRouteParametersForAlias(alias, routeParameters));
    }

    private RouteParameters getRouteParametersForAlias(String alias,
            RouteParameters routeParameters) {
        Map<String, String> parametersMapForAlias = routeParameters
                .getParameterNames().stream()
                .filter(paramName -> alias.contains(":" + paramName))
                .collect(Collectors.toMap(Function.identity(),
                        paramName -> routeParameters.get(paramName).get()));
        return new RouteParameters(parametersMapForAlias);
    }

    private String updateQueryParameters(String path) {
        int startOfQuery = path.indexOf('?');
        if (startOfQuery != -1) {
            path = path.substring(0, startOfQuery);
        }
        if (queryParameters != null) {
            path += '?' + queryParameters.getQueryString();
        }
        return path;
    }

    private String sanitizePath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return UrlUtil.encodeURI(path);
    }

    /**
     * Sets the expanded status of the item.
     *
     * @param expanded
     *            true to expand the item, false to collapse it
     */
    public void setExpanded(boolean expanded) {
        getElement().setProperty("expanded", expanded);
    }

    /**
     * Returns whether the side navigation menu item is expanded or collapsed.
     *
     * @return true if the item is expanded, false if collapsed
     */
    @Synchronize(property = "expanded", value = "expanded-changed")
    public boolean isExpanded() {
        return getElement().getProperty("expanded", false);
    }
}
