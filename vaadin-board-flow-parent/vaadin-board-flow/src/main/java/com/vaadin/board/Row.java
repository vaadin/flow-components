package com.vaadin.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.board.client.RowState;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;

@HtmlImport("frontend://vaadin-board/vaadin-board-row.html")
public class Row extends AbstractComponentContainer {

    private void checkNewColValue(Component component, int cols) {
        Map<Connector, Integer> map = getState().cols;
        int colValueForComponent = map.getOrDefault(component, 0);
        int sum = getState().usedColAmount();
        if ((sum - colValueForComponent + cols) > 4)
            throw new IllegalStateException("new total amount of cols would be bigger 4");
    }

    private void checkIfContained(Component component, int cols) {
        Map<Connector, Integer> map = getState().cols;
        if (!map.containsKey(component)) {
            throw new IllegalStateException("try to modify a component that is not in row " + component);
        }
    }

    private void checkIfNotNegative(Component component, int cols) {
        if (cols < 1)
            throw new IllegalStateException("please , don´t try to add negative values or zero for cols");
    }

    private void checkIfValueSmallerOrEqualFour(Component component, int cols) {
        if (cols > 4)
            throw new IllegalStateException("max col value you can set is 4");
    }

    private final Board board;

    protected List<Component> components = new ArrayList<>();

    /**
     * Creates an empty row instance.
     * <p>
     * For internal use by Board. To create a new row, use
     * {@link Board#addRow(Component...)}
     *
     * @param board
     *     the board this row is connected to
     **/
    public Row(Board board) {
        this.board = board;
    }

    /**
     * Adds the given component(s) to the row.
     * <p>
     * All added components are set to use 1 column. Use #setCols(Component,int)
     * to make a component span multiple columns.
     *
     * @param components
     *     the components to add
     * @throws IllegalStateException
     *     if adding the components would cause the row to have more
     *     than 4 child components
     **/
    @Override
    public void addComponents(Component... components) {
        // Overridden only for javadoc
        super.addComponents(components);
    }

    @Override
    public void addComponent(Component c) {
        addComponent(c, 1);
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        if (components.contains(c)) {
            components.remove(c);
            getState(true).cols.remove(c);
        }
    }

    /**
     * Adds the given component to the row using the given number of columns.
     *
     * @param component
     *     the component to add
     * @param cols
     *     the number of columns the component should use
     * @throws IllegalStateException
     *     if adding the components would cause the row to have more
     *     than 4 child components
     **/
    public void addComponent(Component component, int cols) {
        checkIfValueSmallerOrEqualFour(component, cols);
        checkNewColValue(component, cols);
        checkIfNotNegative(component, cols);

        super.addComponent(component);
        components.add(component);
        getState(true).cols.put(component, 1);
        setCols(component, cols);
    }

    /**
     * Gets the number of columns the given component spans.
     *
     * @param component
     *     the child component to get columns for
     * @return the number of columns the component spans, by default 1.
     **/
    public int getCols(Component component) {
        if (getState().cols.containsKey(component)) {
            return getState().cols.get(component);
        } else {
            if (component.getParent() != this) {
                throw new IllegalArgumentException(
                    "The given component is not a child of this row");
            } else {
                return 1;
            }
        }
    }

    ;

    @Override
    protected RowState getState() {
        return (RowState) super.getState();
    }

    @Override
    protected RowState getState(boolean markAsDirty) {
        return (RowState) super.getState(markAsDirty);
    }

    /**
     * Sets the number of columns the given component spans.
     *
     * @param component
     *     the child component to set columns for
     * @param cols
     *     the number of columns the component spans
     * @throw IllegalArgumentException if the component is not a child component
     * or if the number of columns is less than 1
     **/
    public void setCols(Component component, int cols) {
        checkIfValueSmallerOrEqualFour(component, cols);
        checkNewColValue(component, cols);
        checkIfNotNegative(component, cols);

        getState(true).cols.put(component, cols);
    }

    @Override
    public void replaceComponent(Component oldComponent,
                                 Component newComponent) {
        throw new UnsupportedOperationException(
            "replaceComponent is not currently supported");
    }

    @Override
    public int getComponentCount() {
        return components.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return Collections.unmodifiableCollection(components).iterator();
    }

}
