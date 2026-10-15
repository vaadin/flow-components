/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for components that have a prefix slot.
 *
 * @author Vaadin Ltd
 */
public interface HasPrefix extends HasElement {
    /**
     * Adds the given component into this field before the content, replacing
     * any existing prefix component.
     * <p>
     * This is most commonly used to add a simple icon or static text into the
     * field.
     *
     * @param component
     *            the component to set, can be {@code null} to remove existing
     *            prefix component
     */
    default void setPrefixComponent(Component component) {
        SlotUtils.setSlot(this, "prefix", component);
    }

    /**
     * Gets the component in the prefix slot of this field.
     *
     * @return the prefix component of this field, or {@code null} if no prefix
     *         component has been set
     * @see #setPrefixComponent(Component)
     */
    default Component getPrefixComponent() {
        return SlotUtils.getChildInSlot(this, "prefix");
    }
}
