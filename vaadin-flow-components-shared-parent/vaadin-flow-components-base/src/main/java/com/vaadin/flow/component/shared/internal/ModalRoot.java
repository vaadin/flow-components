/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.shared.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The purpose of this annotation is to help {@code Popover} decide where to
 * auto-attach itself based on the target component.
 * <p>
 * Normally, {@code Popover} attaches to the UI's root element, using the
 * {@code UI#addToModalComponent} method. But that is problematic when the
 * target component is attached inside an element with modality, such as
 * {@code Dialog}. This prevents any events in the client-side fired from the
 * {@code Popover} component or any of its children from being listened to in
 * the server-side.
 * <p>
 * To solve this, {@code Popover} tries to find the closest parent component
 * that has this annotation, and attaches to that component instead of the UI
 * root.
 * <p>
 * <strong>
 * Internal use only. May be renamed or removed in a future release.
 * </strong>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModalRoot {

    /**
     * The slot to use when attaching to the modal root. If empty, no slot
     * attribute is set.
     */
    String slot() default "";
}
