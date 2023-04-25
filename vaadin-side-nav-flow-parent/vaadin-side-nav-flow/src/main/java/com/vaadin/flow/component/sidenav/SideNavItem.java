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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinService;

/**
 * A menu item for the {@link SideNav} component.
 * <p>
 * Can contain a label and/or an icon and links to a given {@code path}.
 */
@Tag("vaadin-side-nav-item")
@JsModule("@vaadin/side-nav/src/vaadin-side-nav-item.js")
// @NpmPackage(value = "@vaadin/side-nav", version = "24.1.0-alpha8")
public class SideNavItem extends Component {

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
     * Creates a new menu item using the given label and icon that links to the
     * given path.
     *
     * @param label
     *            the label for the item
     * @param path
     *            the path to link to
     * @param icon
     *            the icon for the item
     */
    public SideNavItem(String label, String path, Component icon) {
        setPath(path);
        setLabel(label);
        setIcon(icon);
    }

    /**
     * Creates a new menu item using the given label that links to the given
     * view.
     *
     * @param label
     *            the label for the item
     * @param view
     *            the view to link to
     * @param icon
     *            the icon for the item
     */
    public SideNavItem(String label, Class<? extends Component> view,
            Component icon) {
        setPath(view);
        setLabel(label);
        setIcon(icon);
    }

    /**
     * Adds menu item(s) inside this item, creating a hierarchy.
     *
     * @param sideNavItems
     *            the menu item(s) to add
     */
    public void addItem(SideNavItem... sideNavItems) {
        for (SideNavItem sideNavItem : sideNavItems) {
            sideNavItem.getElement().setAttribute("slot", "children");
            getElement().appendChild(sideNavItem.getElement());
        }
    }

    /**
     * Removes the given menu item from this item.
     * <p>
     * If the given menu item is not a child of this menu item, does nothing.
     *
     * @param sideNavItem
     *            the menu item to remove
     */
    public void removeItem(SideNavItem sideNavItem) {
        Optional<Component> parent = sideNavItem.getParent();
        if (parent.isPresent() && parent.get() == this) {
            getElement().removeChild(sideNavItem.getElement());
        }
    }

    /**
     * Removes all menu items from this item.
     */
    public void removeAllItems() {
        final List<Element> allNavItems = getElement()
                .getChildren().filter(element -> Objects
                        .equals(element.getAttribute("slot"), "children"))
                .toList();
        getElement().removeChild(allNavItems);
    }

    /**
     * Gets the label for the item.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return getExistingLabelElement().map(e -> e.getText()).orElse(null);
    }

    /**
     * Set a textual label for the item.
     * <p>
     * The label is also available for screen rader users.
     *
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        getLabelElement().setText(label);
    }

    private Optional<Element> getExistingLabelElement() {
        return getElement().getChildren()
                .filter(child -> !child.hasAttribute("slot")).findFirst();
    }

    private Element getLabelElement() {
        return getExistingLabelElement().orElseGet(() -> {
            Element element = Element.createText("");
            getElement().appendChild(element);
            return element;
        });
    }

    /**
     * Sets the path this item links to.
     *
     * @param path
     *            the path to link to
     */
    public void setPath(String path) {
        getElement().setAttribute("path", path);
    }

    /**
     * Sets the view this item links to.
     *
     * @param view
     *            the view to link to
     */
    public void setPath(Class<? extends Component> view) {
        String url = RouteConfiguration.forRegistry(getRouter().getRegistry())
                .getUrl(view);
        setPath(url);
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

    public String getPath() {
        return getElement().getAttribute("path");
    }

    private int getIconElementIndex() {
        for (int i = 0; i < getElement().getChildCount(); i++) {
            if (Objects.equals(getElement().getChild(i).getAttribute("slot"),
                    "prefix")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the icon for the item.
     * <p>
     * Can also be used to set a custom component to be shown in front of the
     * label.
     *
     * @param icon
     *            the icon to show
     */
    public void setIcon(Component icon) {
        icon.getElement().setAttribute("slot", "prefix");
        int iconElementIndex = getIconElementIndex();
        if (iconElementIndex != -1) {
            getElement().setChild(iconElementIndex, icon.getElement());
        } else {
            getElement().appendChild(icon.getElement());
        }
    }

    /**
     * Sets the expanded status of the item.
     *
     * @param value
     *            true to expand the item, false to collapse it
     */
    public void setExpanded(boolean value) {
        if (value) {
            getElement().setAttribute("expanded", "");
        } else {
            getElement().removeAttribute("expanded");
        }
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
