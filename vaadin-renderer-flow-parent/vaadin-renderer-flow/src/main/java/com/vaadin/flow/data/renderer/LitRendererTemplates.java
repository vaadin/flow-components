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
package com.vaadin.flow.data.renderer;

import java.io.Serializable;

import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The templates of the built-in renderers, declared so that the browser does
 * not compile them from strings. Each one renders the same as the
 * <code>getTemplateExpression()</code> of its renderer.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface LitRendererTemplates extends Serializable {

    /** The template of {@link ComponentRenderer}. */
    @JsExpression("return html`${Vaadin.FlowComponentHost.getNode(appId, item.nodeid)}`;")
    CompiledLitTemplate componentRenderer();

    /** The template of {@link BasicRenderer}. */
    @JsExpression("return html`${item.label}`;")
    CompiledLitTemplate basicRenderer();

    /** The template of {@link TextRenderer}. */
    @JsExpression("return html`<span>${item.label}</span>`;")
    CompiledLitTemplate textRenderer();

    /** The template of {@link NativeButtonRenderer}. */
    @JsExpression("return html`<button @click=${functions.click} .disabled=${item.disabled}>${item.label}</button>`;")
    CompiledLitTemplate nativeButtonRenderer();
}
