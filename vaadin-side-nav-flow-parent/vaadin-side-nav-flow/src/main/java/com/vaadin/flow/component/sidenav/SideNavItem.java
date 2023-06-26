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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.Router;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
@NpmPackage(value = "@vaadin/side-nav", version = "24.2.0-alpha1")
@JsModule("@vaadin/side-nav/src/vaadin-side-nav-item.js")
public class SideNavItem extends SideNavItemContainer
        implements HasPrefix, HasSuffix {

    private Element labelElement;

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
        if (path == null) {
            getElement().removeAttribute("path");
        } else {
            getElement().setAttribute("path", path);
        }
    }

    /**
     * Sets the view this item links to.
     * <p>
     * Note: Vaadin Router will be used to determine the URL path of the view
     * and this URL will be then set to this navigation item using
     * {@link SideNavItem#setPath(String)}. Also, path aliases added to the view
     * via {@link com.vaadin.flow.router.RouteAlias} annotation will be
     * automatically added.
     *
     * @param view
     *            The view to link to. The view should be annotated with the
     *            {@link com.vaadin.flow.router.Route} annotation. Set to null
     *            to disable navigation for this item.
     *
     * @see SideNavItem#setPath(String)
     */
    public void setPath(Class<? extends Component> view) {
        if (view != null) {
            Router router = ComponentUtil.getRouter(this);
            String url = RouteConfiguration.forRegistry(router.getRegistry())
                    .getUrl(view);
            setPath(url);
            addPathAliases(view);
        } else {
            setPath((String) null);
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
     * Adds the specified path aliases to this item. The aliases act as
     * secondary paths when determining the active state of the item.
     *
     * @param aliases
     *            the path aliases to add to this item
     */
    public void addPathAliases(String... aliases) {
        Objects.requireNonNull(aliases, "Aliases to add should not be null");
        String updatedAliases = Stream
                .concat(getPathAliases().stream(), Arrays.stream(aliases))
                .map(alias -> Objects.requireNonNull(alias,
                        "Alias to add cannot be null"))
                .map(String::trim).distinct().collect(Collectors.joining(","));
        getElement().setProperty("pathAliases", updatedAliases);
    }

    /**
     * Adds the path aliases registered to the specified views to this item. The
     * aliases act as secondary paths when determining the active state of the
     * item.
     * <p>
     * Note: The views should be annotated with the
     * {@link com.vaadin.flow.router.RouteAlias} annotation.
     *
     * @param views
     *            The views containing the path aliases to add to this item.
     */
    @SafeVarargs
    public final void addPathAliases(Class<? extends Component>... views) {
        Objects.requireNonNull(views,
                "Views containing the path aliases to add should not be null");
        String[] aliases = Arrays.stream(views).map(view -> Objects.requireNonNull(
                view, "View containing the path aliases to add cannot be null"))
                .map(view -> view.getAnnotationsByType(RouteAlias.class))
                .flatMap(Arrays::stream).map(RouteAlias::value)
                .toArray(String[]::new);
        addPathAliases(aliases);
    }

    /**
     * Removes the specified path aliases from this item.
     *
     * @param aliases
     *            the path aliases to remove from this item
     *
     * @see SideNavItem#addPathAliases(String...)
     */
    public void removePathAliases(String... aliases) {
        Objects.requireNonNull(aliases, "Aliases to remove cannot be null");
        Set<String> aliasesToRemove = Arrays.stream(aliases)
                .map(alias -> Objects.requireNonNull(alias,
                        "Alias to remove cannot be null"))
                .map(String::trim).collect(Collectors.toSet());
        Set<String> updatedAliases = getPathAliases();
        updatedAliases.removeAll(aliasesToRemove);
        if (updatedAliases.isEmpty()) {
            clearPathAliases();
        } else {
            getElement().setProperty("pathAliases",
                    String.join(",", updatedAliases));
        }
    }

    /**
     * Removes the path aliases registered to the specified views from this
     * item.
     * <p>
     * Note: The views should be annotated with the
     * {@link com.vaadin.flow.router.RouteAlias} annotation.
     *
     * @param views
     *            the views containing the path aliases to remove from this item
     *
     * @see SideNavItem#addPathAliases(Class...)
     */
    @SafeVarargs
    public final void removePathAliases(Class<? extends Component>... views) {
        Objects.requireNonNull(views,
                "Views containing the path aliases to remove should not be null");
        removePathAlias(Arrays.stream(views).map(view -> Objects.requireNonNull(
                view,
                "View containing the path aliases to remove cannot be null"))
                .map(view -> view.getAnnotationsByType(RouteAlias.class))
                .flatMap(Arrays::stream).map(RouteAlias::value)
                .toArray(String[]::new));
    }

    /**
     * Clears any previously set path aliases.
     */
    public void clearPathAliases() {
        getElement().removeProperty("pathAliases");
    }

    /**
     * Gets the path aliases for this item.
     *
     * @return the path aliases for this item, empty if none
     */
    public Set<String> getPathAliases() {
        String aliases = getElement().getProperty("pathAliases");
        if (aliases == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(aliases.split(",")).map(String::trim)
                .collect(Collectors.toSet());
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
