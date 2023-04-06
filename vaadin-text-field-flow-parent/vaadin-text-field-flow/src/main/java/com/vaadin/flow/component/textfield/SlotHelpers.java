/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.dom.Element;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods for handling child elements inside slots.
 *
 * @author Vaadin Ltd
 */
class SlotHelpers {

    /**
     * Gets all the child elements of the parent that are in the specified slot.
     * 
     * @param parent
     *            the component to get children from, not {@code null}
     * @param slot
     *            the name of the slot inside the parent, not {@code null}
     * @return the child elements of the parent that are inside the slot
     */
    public static Stream<Element> getElementsInSlot(HasElement parent,
            String slot) {
        return parent.getElement().getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")));
    }

    /**
     * Removes every child element of the parent that are in the specified slot.
     * 
     * @param parent
     *            the component whose slot to clear
     * @param slot
     *            the name of the slot to clear
     */
    public static void clearSlot(HasElement parent, String slot) {
        getElementsInSlot(parent, slot).collect(Collectors.toList())
                .forEach(parent.getElement()::removeChild);
    }

    /**
     * Gets the first child component of the parent that is in the specified
     * slot.
     * 
     * @param parent
     *            the component to get child from, not {@code null}
     * @param slot
     *            the name of the slot inside the parent, not {@code null}
     * @return a child component of the parent in the specified slot, or
     *         {@code null} if none is found
     */
    public static Component getChildInSlot(HasElement parent, String slot) {
        Optional<Element> element = getElementsInSlot(parent, slot).findFirst();
        if (element.isPresent()) {
            return element.get().getComponent().get();
        }
        return null;
    }
}
