/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component;

import com.vaadin.flow.dom.Element;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Util methods for handling child elements inside slots.
 *
 * @author Vaadin Ltd
 */
public class SlotUtils {

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
        return element.flatMap(Element::getComponent).orElse(null);
    }
}
