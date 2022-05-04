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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.SlotUtils;

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
        SlotUtils.clearSlot(this, "prefix");

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
        return SlotUtils.getChildInSlot(this, "prefix");
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
        SlotUtils.clearSlot(this, "suffix");

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
        return SlotUtils.getChildInSlot(this, "suffix");
    }
}
