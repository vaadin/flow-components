/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.UrlUtil;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.internal.ConfigureRoutes;

import java.util.Optional;

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
@NpmPackage(value = "@vaadin/side-nav", version = "24.2.0-alpha2")
@JsModule("@vaadin/side-nav/src/vaadin-side-nav-item.js")
public class SideNavItem extends SideNavItemContainer
        implements HasPrefix, HasSuffix {

    private Element labelElement;

    private RouteParameters routeParameters;

    private QueryParameters queryParameters;

    private Class<? extends Component> view;

    private String customPath;

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

    @Override
    protected void setupSideNavItem(SideNavItem item) {
        item.getElement().setAttribute("slot", "children");
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
        this.view = null;
        this.customPath = path;
        doSetPath(path);
    }

    /**
     * Retrieves {@link com.vaadin.flow.router.Route} annotations from the
     * specified view, and then sets the corresponding path for this item.
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
     * Retrieves {@link com.vaadin.flow.router.Route} annotations from the
     * specified view, and then sets the corresponding path for this item.
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
     */
    public void setPath(Class<? extends Component> view,
            RouteParameters routeParameters) {
        this.view = view;
        this.customPath = null;
        this.routeParameters = routeParameters;
        if (view == null) {
            doSetPath(null);
        } else {
            RouteConfiguration routeConfiguration = RouteConfiguration
                    .forRegistry(ComponentUtil.getRouter(this).getRegistry());
            doSetPath(routeConfiguration.getUrl(view, routeParameters));
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

    private void doSetPath(String path) {
        if (path == null) {
            getElement().removeAttribute("path");
        } else {
            getElement().setAttribute("path",
                    sanitizePath(updateQueryParameters(path)));
        }
    }

    /**
     * Gets the {@link QueryParameters} of this item.
     *
     * @return an optional of {@link QueryParameters}, or an empty optional if
     *         there are no query parameters set
     * @see #setQueryParameters(QueryParameters)
     */
    public Optional<QueryParameters> getQueryParameters() {
        return Optional.ofNullable(queryParameters);
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
        refresh();
    }

    private void refresh() {
        if (view != null) {
            setPath(view, routeParameters);
        } else {
            setPath(customPath);
        }
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag();
    }

    /**
     * Checks whether the SideNavItem component feature flag is active. Succeeds
     * if the flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#SIDE_NAV_COMPONENT} feature is
     *             not enabled
     */
    private void checkFeatureFlag() {
        boolean enabled = getFeatureFlags()
                .isEnabled(FeatureFlags.SIDE_NAV_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Gets the feature flags for the current UI.
     * <p>
     * Extracted with protected visibility to support mocking
     *
     * @return the current set of feature flags
     */
    protected FeatureFlags getFeatureFlags() {
        return FeatureFlags
                .get(UI.getCurrent().getSession().getService().getContext());
    }
}
