/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.form;

import java.io.Serializable;

/**
 * Listener for {@link FieldValueChangeEvent}s fired by a
 * {@link FormAIController} after a successful AI turn. One event is delivered
 * per changed field, in document order.
 *
 * @since 25.2
 */
@FunctionalInterface
public interface FieldValueChangeListener extends Serializable {

    /**
     * Invoked once per changed field after the AI turn has settled.
     *
     * @param event
     *            the event describing the change, never {@code null}
     */
    void onFieldValueChange(FieldValueChangeEvent event);
}
