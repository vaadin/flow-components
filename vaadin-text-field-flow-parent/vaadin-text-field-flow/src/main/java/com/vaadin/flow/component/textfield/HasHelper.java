package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for text-field components that have helper slots for
 * inserting components.
 *
 * @author Vaadin Ltd
 */
public interface HasHelper extends HasElement {

    /**
     * Adds the given component into helper slot of component, replacing any
     * existing helper component.
     * 
     * @param component
     *            the component to set, can be {@code null} to remove existing
     *            helper component
     */
    default void setHelperComponent(Component component) {
        SlotHelpers.clearSlot(this, "helper");

        if (component != null) {
            component.getElement().setAttribute("slot", "helper");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Gets the component in the helper slot of this field.
     * 
     * @return the helper component of this field, or {@code null} if no helper
     *         component has been set
     * @see #setHelperComponent(Component)
     */
    default Component getHelperComponent() {
        return SlotHelpers.getChildInSlot(this, "helper");
    }
}
