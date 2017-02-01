package com.vaadin.board;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.vaadin.board.client.BoardState.RowState;
import com.vaadin.ui.Component;

public class Row implements Serializable {

    private final Board board;

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
    public void addComponents(Component... components) {
        for (Component component : components) {
            addComponent(component, 1);
        }
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
        getState().components.add(component);
        component.setParent(board);
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
        return getState().cols.get(component);
    };

    private RowState getState() {
        return board.getState(this);
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
        if (cols < 1 || cols > 4) {
            throw new IllegalArgumentException("Cols must be between 1 and 4");
        }

        if (cols == 1) {
            getState().cols.remove(component);
        } else {
            getState().cols.put(component, cols);
        }
    }

    /**
     * Gets the components in this row in the order they appear.
     *
     * @return an unmodifiable list of child components
     */
    @SuppressWarnings("unchecked")
    public List<Component> getComponents() {
        return Collections.unmodifiableList((List) getState().components);
    };

}
