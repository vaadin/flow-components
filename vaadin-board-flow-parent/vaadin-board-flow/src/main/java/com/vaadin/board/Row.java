package com.vaadin.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.board.client.RowState;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;

public class Row extends AbstractComponentContainer {

    private final Board board;
    protected List<Component> components = new ArrayList<>();

    /**
     * Creates an empty row instance.
     * <p>
     * For internal use by Board. To create a new row, use
     * {@link Board#addRow(Component...)}
     *
     * @param board
     *            the board this row is connected to
     **/
    Row(Board board) {
        this.board = board;
    }

    /**
     * Adds the given component(s) to the row.
     * <p>
     * All added components are set to use 1 column. Use #setCols(Component,int)
     * to make a component span multiple columns.
     *
     * @param components
     *            the components to add
     * @throws IllegalStateException
     *             if adding the components would cause the row to have more
     *             than 4 child components
     **/
    @Override
    public void addComponents(Component... components) {
        // Overridden only for javadoc
        super.addComponents(components);
    }

    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        components.add(c);
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        components.remove(c);
    }

    /**
     * Adds the given component to the row using the given number of columns.
     *
     * @param component
     *            the component to add
     * @param cols
     *            the number of columns the component should use
     * @throws IllegalStateException
     *             if adding the components would cause the row to have more
     *             than 4 child components
     **/
    public void addComponent(Component component, int cols) {
        addComponent(component);
        setCols(component, cols);
    }

    /**
     * Gets the number of columns the given component spans.
     *
     * @param component
     *            the child component to get columns for
     * @return the number of columns the component spans, by default 1.
     **/
    public int getCols(Component component) {
        if (getState(false).cols.containsKey(component)) {
            return getState().cols.get(component);
        } else {
            if (component.getParent() != this) {
                throw new IllegalArgumentException(
                        "The given component is not a child of this row");
            } else {
                return 1;
            }
        }
    };

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
     *            the child component to set columns for
     * @param cols
     *            the number of columns the component spans
     * @throw IllegalArgumentException if the component is not a child component
     *        or if the number of columns is less than 1
     **/
    public void setCols(Component component, int cols) {
        if (component.getParent() != this) {
            throw new IllegalArgumentException(
                    "The given component is not a child of this row");
        }
        if (cols < 1 || cols > 4) {
            throw new IllegalArgumentException("Cols must be between 1 and 4");
        }

        if (cols == 1) {
            getState().cols.remove(component);
        } else {
            getState().cols.put(component, cols);
        }
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
