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
package com.vaadin.flow.component.treegrid;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;

/**
 * Renders components as hierarchy column for tree grid.
 *
 * @param <COMPONENT>
 *            the type of the output component
 * @param <SOURCE>
 *            the type of the input model object
 */
public class HierarchyColumnComponentRenderer<COMPONENT extends Component, SOURCE>
        extends ComponentRenderer<COMPONENT, SOURCE> {

    public HierarchyColumnComponentRenderer(
            ValueProvider<SOURCE, COMPONENT> componentProvider,
            TreeGrid<SOURCE> grid) {
        super(componentProvider);

        withFunction("onClick", item -> {
            if (grid.isExpanded(item)) {
                grid.collapse(List.of(item), true);
            } else {
                grid.expand(List.of(item), true);
            }
        });
    }

    @Override
    protected String getTemplateExpression() {
        // The click listener needs to check if the event gets canceled (by
        // vaadin-grid-tree-toggle) and only invoke the callback if it does.
        // vaadin-grid-tree-toggle will cancel the event if the user clicks on
        // a non-focusable element inside the toggle.
        var clickListener = "e => requestAnimationFrame(() => { e.defaultPrevented && onClick(e) })";

        return "<vaadin-grid-tree-toggle @click=${" + clickListener
                + "} class=${item.cssClassName} .leaf=${!model.hasChildren} .expanded=${live(model.expanded)} .level=${model.level}>"
                + super.getTemplateExpression() + "</vaadin-grid-tree-toggle>";
    }
}
