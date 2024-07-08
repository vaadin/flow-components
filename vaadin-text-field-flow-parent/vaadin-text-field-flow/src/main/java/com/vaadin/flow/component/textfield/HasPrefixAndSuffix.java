/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for text-field components that have prefix and suffix slots
 * for inserting components.
 *
 * @author Vaadin Ltd
 */
public interface HasPrefixAndSuffix extends HasElement {

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
        SlotHelpers.clearSlot(this, "prefix");

        if (component != null) {
            component.getElement().setAttribute("slot", "prefix");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Gets the component in the prefix slot of this field.
     *
     * @return the prefix component of this field, or {@code null} if no prefix
     *         component has been set
     * @see #setPrefixComponent(Component)
     */
    default Component getPrefixComponent() {
        return SlotHelpers.getChildInSlot(this, "prefix");
    }

    /**
     * Adds the given component into this field after the content, replacing any
     * existing suffix component.
     * <p>
     * This is most commonly used to add a simple icon or static text into the
     * field.
     *
     * @param component
     *            the component to set, can be {@code null} to remove existing
     *            suffix component
     */
    default void setSuffixComponent(Component component) {
        SlotHelpers.clearSlot(this, "suffix");

        if (component != null) {
            component.getElement().setAttribute("slot", "suffix");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Gets the component in the suffix slot of this field.
     *
     * @return the suffix component of this field, or {@code null} if no suffix
     *         component has been set
     * @see #setPrefixComponent(Component)
     */
    default Component getSuffixComponent() {
        return SlotHelpers.getChildInSlot(this, "suffix");
    }
}
