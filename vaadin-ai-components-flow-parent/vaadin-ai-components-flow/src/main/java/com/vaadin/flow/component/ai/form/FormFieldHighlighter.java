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

import com.vaadin.flow.component.fieldhighlighter.FieldHighlighterInitializer;
import com.vaadin.flow.dom.Element;

/**
 * Bridges {@link FormAIController#showHighlight} /
 * {@link FormAIController#hideHighlight} to the
 * {@code vaadin-field-highlighter} web component. Each controller passes its
 * own {@code userId} so the AI user does not collide with any other user the
 * application may keep on the same field.
 */
final class FormFieldHighlighter extends FieldHighlighterInitializer {

    private FormFieldHighlighter() {
    }

    /**
     * Adds the controller's AI user to the field. {@code addUser} is keyed by
     * id at the client, so repeated calls with the same id collapse to a single
     * entry — other users on the field stay untouched.
     */
    static void show(Element field, String userId) {
        field.executeJs(
                "customElements.get('vaadin-field-highlighter')"
                        + ".addUser(this, {id: $0, name: 'AI', colorIndex: 0})",
                userId);
    }

    /**
     * Removes the controller's AI user from the field. {@code removeUser}
     * matches by id and is a no-op when the AI user is not present. Other users
     * on the field stay highlighted.
     */
    static void hide(Element field, String userId) {
        field.executeJs("customElements.get('vaadin-field-highlighter')"
                + ".removeUser(this, {id: $0})", userId);
    }
}
