package com.vaadin.flow.component.treegrid;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;

/**
 * Renders components as hierarchy column for tree grid. Basically puts
 * <code>flow-component-renderer</code> tag inside of
 * <code>vaadin-grid-tree-toggle</code>
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

        withProperty("expanded", item -> grid.isExpanded(item));

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
        // TODO: Make level / expanded... available in LitRenderer
        return "<vaadin-grid-tree-toggle @click=${onClick} class=${item.cssClassName} .leaf=${!item.children} .expanded=${item.expanded} level=${item.level}>"
                + super.getTemplateExpression() + "</vaadin-grid-tree-toggle>";
    }
}
