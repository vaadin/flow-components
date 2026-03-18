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
package com.vaadin.flow.component.ai.ui;

import java.io.Serializable;

import com.vaadin.flow.function.SerializableConsumer;

/**
 * Interface for input components that are used in an AI conversation.
 *
 * @author Vaadin Ltd
 */
public interface AIInput extends Serializable {

    /**
     * Adds a listener for submit events.
     * <p>
     * The listener is notified with the submitted value when the user submits
     * input.
     *
     * @param listener
     *            the listener to add, not {@code null}
     */
    void addSubmitListener(SerializableConsumer<String> listener);
}
