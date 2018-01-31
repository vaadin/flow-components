package com.vaadin.flow.component.board;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

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
@HtmlImport("frontend://bower_components/vaadin-board/vaadin-board-row.html")
public class Row extends Component implements HasComponents {

    private static final String COLSPAN_PROPERTY = "boardCols";

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
        // Overridden only for javadoc
        HasComponents.super.add(components);
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
        // if (!ComponentUtil.isDirectChild(this,component)) {
        // throw new IllegalArgumentException(
        // "The given component is not a child of this row");
        // }
        return component.getElement().getProperty(COLSPAN_PROPERTY, 1);
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
        // if (!ComponentUtil.isDirectChild(this,component)) {
        // throw new IllegalArgumentException(
        // "The given component is not a child of this row");
        // }
        component.getElement().setProperty(COLSPAN_PROPERTY, columns);
    }

}
