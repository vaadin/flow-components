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

import java.util.Optional;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinService;

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
@JsModule("@vaadin/side-nav/src/vaadin-side-nav-item.js")
// @NpmPackage(value = "@vaadin/side-nav", version = "24.1.0-alpha8")
public class SideNavItem extends SideNavItemContainer
        implements HasPrefix, HasSuffix {

    private static final PropertyDescriptor<String, Optional<String>> expandedDescriptor = PropertyDescriptors
            .optionalAttributeWithDefault("expanded", "false");

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

    private boolean isLabelElement(Element child) {
        return !child.hasAttribute("slot");
    }

    /**
     * Gets the label of this menu item.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return searchLabelElement().map(Element::getText).orElse(null);
    }

    /**
     * Set a textual label for the item.
     * <p>
     * The label is also available for screen reader users.
     *
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        getLabelElement().setText(label);
    }

    private Optional<Element> searchLabelElement() {
        return getElement().getChildren().filter(this::isLabelElement)
                .findFirst();
    }

    private Element getLabelElement() {
        return searchLabelElement()
                .orElseGet(this::createAndAppendLabelElement);
    }

    private Element createAndAppendLabelElement() {
        Element element = Element.createText("");
        getElement().appendChild(element);
        return element;
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
     * {@link SideNavItem#setPath(String)}
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
            String url = RouteConfiguration
                    .forRegistry(getRouter().getRegistry()).getUrl(view);
            setPath(url);
        } else {
            setPath((String) null);
        }
    }

    private Router getRouter() {
        Router router = null;
        if (getElement().getNode().isAttached()) {
            StateTree tree = (StateTree) getElement().getNode().getOwner();
            router = tree.getUI().getInternals().getRouter();
        }
        if (router == null) {
            router = VaadinService.getCurrent().getRouter();
        }
        if (router == null) {
            throw new IllegalStateException(
                    "Implicit router instance is not available. "
                            + "Use overloaded method with explicit router parameter.");
        }
        return router;
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
     * Sets the expanded status of the item.
     *
     * @param value
     *            true to expand the item, false to collapse it
     */
    public void setExpanded(boolean value) {
        set(expandedDescriptor, value ? "" : "false");
    }

    /**
     * @return Returns if the item is expanded or not
     */
    public boolean isExpanded() {
        return get(expandedDescriptor).isPresent();
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
