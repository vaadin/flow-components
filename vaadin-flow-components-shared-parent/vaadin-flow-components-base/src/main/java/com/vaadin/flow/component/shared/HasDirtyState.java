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
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Synchronize;

/**
 * Mixin interface for field components that support the dirty state.
 *
 * @author Vaadin Ltd
 */
public interface HasDirtyState extends HasElement {
    /**
     * Sets the dirty state for the component.
     *
     * @param dirty
     *              whether the field is dirty.
     */
    default void setDirty(boolean dirty) {
        getElement().setProperty("dirty", dirty);
    }

    /**
     * Gets the dirty state of the component.
     * <p>
     * The component is automatically marked as dirty once the user triggers an
     * `input` or `change` event. Additionally, the component can be manually
     * marked as dirty with {@link #setDirty(boolean)}.
     *
     * @return whether the field is dirty.
     */
    @Synchronize("dirty-changed")
    default boolean isDirty() {
        return getElement().getProperty("dirty", false);
    }
}
