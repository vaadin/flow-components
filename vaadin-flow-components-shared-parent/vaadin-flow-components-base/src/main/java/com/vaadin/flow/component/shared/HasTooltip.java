/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for components that have special handling for tooltips on the
 * Web Component level.
 * <p>
 * Components that implement this interface get a
 * <code>&lt;vaadin-tooltip slot="tooltip"&gt;</code> element added inside the
 * component's light DOM and are expected to handle it appropriately on the
 * client-side.
 * <p>
 * Use this interface only if you are implementing a new component that also has
 * a Web Component counterpart with a custom tooltip support. Otherwise, use
 * {@link Tooltip#forComponent(Component)} instead.
 *
 *
 * @author Vaadin Ltd
 */
public interface HasTooltip extends HasElement {

    /**
     * Sets a tooltip text for the component.
     *
     * @param text
     *            The tooltip text
     *
     * @return the tooltip handle
     */
    default Tooltip setTooltipText(String text) {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = Tooltip.forHasTooltip(this);
        }
        tooltip.setText(text);
        return tooltip;
    }

    /**
     * Gets the tooltip handle of the component.
     *
     * @return the tooltip handle
     */
    default Tooltip getTooltip() {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = setTooltipText(null);
        }
        return tooltip;
    }

}
