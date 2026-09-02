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
package com.vaadin.flow.component.shared;

/**
 * Defines how long a component stays disabled after it has been disabled on
 * click.
 *
 * @since 25.3
 */
public enum DisableOnClickMode {

    /**
     * The component stays disabled until it is enabled again by the
     * application, for example by calling {@code setEnabled(true)} from a click
     * listener. This is the default mode.
     */
    UNTIL_ENABLED,

    /**
     * The component is enabled again automatically with the next response the
     * server sends to the client after the click. Normally this is the response
     * to the request that delivered the click, so the component stays disabled
     * while the click listeners run and is enabled again once they are done.
     * This prevents accidental extra clicks while the server processes the
     * click, without requiring you to enable the component again from a click
     * listener. If the component is detached before that response, it is
     * enabled again when it is attached the next time.
     * <p>
     * If push is enabled and a click listener calls {@code UI.push()} while it
     * is still running, that push is the next response and enables the
     * component before the listener has finished. Use {@link #UNTIL_ENABLED}
     * when the component must stay disabled in that case.
     * <p>
     * If the component's enabled state is explicitly set while the click is
     * handled, for example by calling {@code setEnabled(false)} from a click
     * listener, the automatic enabling is skipped for that click.
     */
    UNTIL_RESPONSE;
}
