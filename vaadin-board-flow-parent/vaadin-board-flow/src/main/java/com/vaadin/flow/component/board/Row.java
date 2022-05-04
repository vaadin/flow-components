package com.vaadin.flow.component.board;

/*-
 * #%L
 * Vaadin Board for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.board.internal.FunctionCaller;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Row class to define rows used in a {@link Board} instance.
 * <p>
 * Each Row consists of four columns, and can contain up to four components
 * taking one column each, or fewer components with multiple columns each as
 * long as sum of columns stays less than or equal to four.
 *
 * <p>
 * One row might also contain a nested row as shown in the following example:
 *
 * <pre>
 * Board board = new Board();
 * Label lbl1 = createLabel("Label 1");
 * Label lbl2 = createLabel("Label 2");
 * Label lbl3 = createLabel("Label 3");
 *
 * Label inner1 = createLabel("Inner 1");
 * Label inner3 = createLabel("Inner 3");
 * Label inner4 = createLabel("Inner 4");
 * Label inner2 = createLabel("Inner 2");
 * Row innerRow = new Row(inner1, inner2, inner3, inner4);
 * Row outerRow = board.add(lbl1, lbl2, lbl3, innerRow);
 * </pre>
 */
@Tag("vaadin-board-row")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/board", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-board", version = "23.1.0-beta1")
@JsModule("@vaadin/board/vaadin-board-row.js")
public class Row extends Component
        implements HasStyle, HasSize, HasOrderedComponents {

    static final String COLSPAN_ATTRIBUTE = "board-cols";
    private boolean redrawTriggered;

    /**
     * Creates an empty row.
     * <p>
     * Use {@link #add(Component...)} or {@link #add(Component, int)} to add
     * content to the row.
     */
    public Row() {
        super();
    }

    /**
     * Creates an new row with the given components.
     *
     * @param components
     *            initial content of the row
     */
    public Row(Component... components) {
        super();
        add(components);
    }

    /**
     * Adds the given component(s) to the row.
     * <p>
     * All added components are set to use 1 column. Use
     * {@link #setComponentSpan(Component, int)} to make a component span
     * multiple columns.
     *
     * @param components
     *            the components to add
     **/
    @Override
    public void add(Component... components) {
        throwIfTooManyColumns(components.length);
        HasOrderedComponents.super.add(components);
    }

    /**
     * Adds the given component to the row using the given number of columns.
     *
     * @param component
     *            the component to add
     * @param cols
     *            the number of columns the component should use
     **/
    public void add(Component component, int cols) {
        add(component);
        setComponentSpan(component, cols);
    }

    /**
     * Adds the given row as a nested row to the current row.
     *
     * @param row
     *            the row to add as a nested row
     **/
    public void addNestedRow(Row row) {
        add(row, 1);
    }

    /**
     * Gets the number of columns the given component spans.
     *
     * @param component
     *            the child component to get columns for
     * @return the number of columns the component spans, by default 1.
     **/
    public int getComponentSpan(Component component) {
        throwIfNotChild(component);
        String attr = component.getElement().getAttribute(COLSPAN_ATTRIBUTE);
        if (attr == null) {

            return 1;
        } else {
            return Integer.parseInt(attr);
        }
    }

    /**
     * Sets the number of columns the given component spans.
     *
     * @param component
     *            the child component to set columns for
     * @param columns
     *            the number of columns the component spans
     **/
    public void setComponentSpan(Component component, int columns) {
        throwIfNotChild(component);
        throwIfTooManyColumns(columns - getComponentSpan(component));
        if (columns == 1) {
            component.getElement().removeAttribute(COLSPAN_ATTRIBUTE);
        } else {
            component.getElement().setAttribute(COLSPAN_ATTRIBUTE,
                    "" + columns);
        }
        // <vaadin-board> does not know then the attribute of a child has
        // changed so we need to call redraw()
        FunctionCaller.callOnceOnClientReponse(this, "redraw");
    }

    private void throwIfNotChild(Component component) {
        if (!isDirectChild(this, component)) {
            throw new IllegalArgumentException(
                    "The given component is not a child of this row");
        }
    }

    private void throwIfTooManyColumns(int aboutToBeAdded) {
        if (getColumns() + aboutToBeAdded > 4) {
            throw new IllegalArgumentException(
                    "A row can only contain 4 columns");
        }

    }

    /**
     * Gets the number of columns used, i.e. the total span sum for all
     * components.
     *
     * @return the number of used columns
     */
    private int getColumns() {
        return getChildren().map(this::getComponentSpan).reduce(Integer::sum)
                .orElse(0);
    }

    private static boolean isDirectChild(Component parent,
            Component component) {
        Optional<Component> componentParent = component.getParent();
        return componentParent.isPresent() && componentParent.get() == parent;
    }

}
