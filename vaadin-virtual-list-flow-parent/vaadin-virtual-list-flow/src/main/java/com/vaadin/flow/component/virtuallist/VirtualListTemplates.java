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
package com.vaadin.flow.component.virtuallist;

import com.vaadin.flow.data.renderer.CompiledLitTemplate;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;

/**
 * The Lit templates of {@link VirtualList}, declared so that the browser does
 * not compile them from strings.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface VirtualListTemplates extends JsSnippets {

    /**
     * Returns the template of the renderer of
     * {@link VirtualList#setRenderer(ValueProvider)}. It renders the
     * {@code label} property of the item as plain text, without a wrapping
     * element.
     *
     * @return the template of the renderer of
     *         {@link VirtualList#setRenderer(ValueProvider)}
     */
    @JsExpression("return html`${item.label}`;")
    CompiledLitTemplate labelRenderer();
}
