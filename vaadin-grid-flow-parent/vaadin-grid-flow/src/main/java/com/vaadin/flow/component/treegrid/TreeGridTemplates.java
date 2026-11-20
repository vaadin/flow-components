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
package com.vaadin.flow.component.treegrid;

import java.io.Serializable;

import com.vaadin.flow.data.renderer.CompiledLitTemplate;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The Lit templates of the hierarchy columns of {@link TreeGrid}, declared so
 * that the browser does not compile them from strings.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface TreeGridTemplates extends Serializable {

    /**
     * Returns the template of the renderer of
     * {@link TreeGrid#addHierarchyColumn}. It renders the {@code name} property
     * of the item in a {@code <vaadin-grid-tree-toggle>}, and a click calls the
     * {@code onClick} function of the renderer.
     *
     * @return the template of the renderer of
     *         {@link TreeGrid#addHierarchyColumn}
     */
    @JsExpression("""
            return html`<vaadin-grid-tree-toggle
                @click=${functions.onClick}
                .leaf=${!model.hasChildren}
                .expanded=${live(model.expanded)}
                .level=${model.level}>${item.name}</vaadin-grid-tree-toggle>`;
            """)
    CompiledLitTemplate hierarchyColumnRenderer();

    /**
     * Returns the template of {@link HierarchyColumnComponentRenderer}. It
     * renders the component of the item in a {@code <vaadin-grid-tree-toggle>},
     * and a click calls the {@code onClick} function of the renderer only when
     * the toggle canceled the click event.
     *
     * @return the template of {@link HierarchyColumnComponentRenderer}
     */
    @JsExpression("""
            return html`<vaadin-grid-tree-toggle
                @click=${(e) => requestAnimationFrame(() => { e.defaultPrevented && functions.onClick(e); })}
                class=${item.cssClassName}
                .leaf=${!model.hasChildren}
                .expanded=${live(model.expanded)}
                .level=${model.level}>${Vaadin.FlowComponentHost.getNode(appId, item.nodeid)}</vaadin-grid-tree-toggle>`;
            """)
    CompiledLitTemplate hierarchyColumnComponentRenderer();
}
