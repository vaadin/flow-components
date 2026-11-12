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
     * The template of {@link TreeGrid#addHierarchyColumn}, which shows the
     * <code>name</code> property in a tree toggle.
     */
    @JsExpression("""
            return html`<vaadin-grid-tree-toggle
                @click=${functions.onClick}
                .leaf=${!model.hasChildren}
                .expanded=${live(model.expanded)}
                .level=${model.level}>${item.name}</vaadin-grid-tree-toggle>`;
            """)
    CompiledLitTemplate hierarchy();

    /**
     * The template of {@link HierarchyColumnComponentRenderer}, which shows a
     * component in a tree toggle.
     * <p>
     * The click listener checks whether the event is canceled (by
     * vaadin-grid-tree-toggle) and only invokes the callback if it is.
     * vaadin-grid-tree-toggle cancels the event if the user clicks on a
     * non-focusable element inside the toggle.
     */
    @JsExpression("""
            return html`<vaadin-grid-tree-toggle
                @click=${(e) => requestAnimationFrame(() => { e.defaultPrevented && functions.onClick(e); })}
                class=${item.cssClassName}
                .leaf=${!model.hasChildren}
                .expanded=${live(model.expanded)}
                .level=${model.level}>${Vaadin.FlowComponentHost.getNode(appId, item.nodeid)}</vaadin-grid-tree-toggle>`;
            """)
    CompiledLitTemplate hierarchyComponent();
}
