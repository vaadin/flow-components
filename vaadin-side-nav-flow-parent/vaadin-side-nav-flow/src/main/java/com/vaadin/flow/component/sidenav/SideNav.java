package com.vaadin.flow.component.sidenav;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import java.util.Optional;

/**
 * A side navigation menu with support for hierarchical and flat menus.
 * <p>
 * Items can be added using {@link #addItem(SideNavItem...)} and hierarchy can be
 * created by adding {@link SideNavItem} instances to other {@link SideNavItem}
 * instances.
 */
// FIXME: Temporarily uses @vaadin-component-factory/vcf-nav until the vaadin-side-nav web-component is ready
@Tag("vcf-nav")
@JsModule("@vaadin-component-factory/vcf-nav")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
public class SideNav extends Component implements HasSize, HasStyle {

    /**
     * Creates a new menu without any label.
     */
    public SideNav() {
    }

    /**
     * Creates a new menu with the given label.
     *
     * @param label
     *            the label to use
     */
    public SideNav(String label) {
        setLabel(label);
    }

    /**
     * Adds menu item(s) to the menu.
     *
     * @param sideNavItems
     *            the menu item(s) to add
     * @return the menu for chaining
     */
    public SideNav addItem(SideNavItem... sideNavItems) {
        for (SideNavItem sideNavItem : sideNavItems) {
            getElement().appendChild(sideNavItem.getElement());
        }

        return this;
    }

    /**
     * Removes the menu item from the menu.
     * <p>
     * If the given menu item is not a child of this menu, does nothing.
     *
     * @param sideNavItem
     *            the menu item to remove
     * @return the menu for chaining
     */
    public SideNav removeItem(SideNavItem sideNavItem) {
        Optional<Component> parent = sideNavItem.getParent();
        if (parent.isPresent() && parent.get() == this) {
            getElement().removeChild(sideNavItem.getElement());
        }

        return this;
    }

    /**
     * Removes all menu items from this item.
     *
     * @return this item for chaining
     */
    public SideNav removeAllItems() {
        getElement().removeAllChildren();
        return this;
    }

    /**
     * Gets the textual label for the navigation.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return getExistingLabelElement().map(e -> e.getText()).orElse(null);
    }

    /**
     * Set a textual label for the navigation.
     * <p>
     * This can help the end user to distinguish groups of navigation items. The
     * label is also available for screen reader users.
     *
     * @param label
     *            the label to set
     * @return this instance for chaining
     */
    public SideNav setLabel(String label) {
        getLabelElement().setText(label);
        return this;
    }

    private Optional<Element> getExistingLabelElement() {
        return getElement().getChildren().filter(child -> "label".equals(child.getAttribute("slot"))).findFirst();
    }

    private Element getLabelElement() {
        return getExistingLabelElement().orElseGet(() -> {
            Element element = new Element("span");
            element.setAttribute("slot", "label");
            getElement().appendChild(element);
            return element;
        });
    }

    /**
     * Check if the end user is allowed to collapse/hide and expand/show the
     * navigation items.
     * <p>
     * NOTE: The navigation has to have a label for it to be collapsible.
     *
     * @return true if the menu is collapsible, false otherwise
     */
    public boolean isCollapsible() {
        return getElement().hasAttribute("collapsible");
    }

    /**
     * Allow the end user to collapse/hide and expand/show the navigation items.
     * <p>
     * NOTE: The navigation has to have a label for it to be collapsible.
     *
     * @param collapsible
     *            true to make the whole navigation component collapsible, false
     *            otherwise
     * @return this instance for chaining
     */
    public SideNav setCollapsible(boolean collapsible) {
        getElement().setAttribute("collapsible", "");
        return this;
    }

}
