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
package com.vaadin.flow.component.grid.editor;

import java.io.Serializable;

import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link EditorRenderer} runs in the browser, for
 * {@link com.vaadin.flow.dom.Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface EditorJs extends Serializable {

    /**
     * Patches the renderer of a column so that it renders the editor component
     * of the item while the item is being edited, and the original renderer
     * otherwise. Called on the column element.
     *
     * @param appId
     *            the id of the application the column belongs to
     * @param columnInternalId
     *            the internal id of the column
     */
    @JsExpression("""
            const originalRender = this.renderer;
            this.renderer = (root, container, model) => {
                const editingChanged = root.__editing !== model.item._editing;
                root.__editing = model.item._editing;

                // If the editing state changed, the root needs to be cleared
                if (editingChanged) {
                    delete root._$litPart$; root.innerHTML = '';
                }

                // If editing, render the editor, otherwise use the original renderer
                if (root.__editing) {
                    Vaadin.FlowComponentHost.setChildNodes($0, [model.item['_' + $1 + '_editor']], root);
                } else if (originalRender) {
                    originalRender(root, container, model);
                } else {
                    this._defaultRenderer(root, container, model);
                }
            };
            """)
    void patchRenderer(String appId, String columnInternalId);
}
