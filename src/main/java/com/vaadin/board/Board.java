package com.vaadin.board;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.board.client.BoardState;
import com.vaadin.board.client.BoardState.RowState;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

@HtmlImport("vaadin://bower_components/vaadin-board/vaadin-board.html")
@HtmlImport("vaadin://bower_components/vaadin-board/vaadin-board-row.html")
public class Board extends PolymerComponent implements HasComponents {

    private final List<Row> rows = new ArrayList<>();

    @Override
    public BoardState getState() {
        return (BoardState) super.getState();
    }

    /**
     * Creates an empty board.
     *
     * Use #addRow(Row) to add content to the board.
     **/
    public Board() {
    }

    /**
     * Creates a new row and adds the given components to the row.
     *
     * All the added components have cols set to 1, i.e. use one slot in the
     * row. The number of slots in the row is the number of added components.
     *
     * @param the
     *            components to add, no more than 4
     * @throws IllegalArgumentException
     *             if there are more than 4 components
     * @returns a row instance which can be used for further configuration
     **/
    public Row addRow(Component... components) {
        Row row = new Row(this);
        getState().rows.add(new RowState());
        rows.add(row);
        row.addComponents(components);
        markAsDirty();
        return row;
    }

    @Override
    public Iterator<Component> iterator() {
        return rows.stream().flatMap(row -> row.getComponents().stream())
                .iterator();
    }

    /**
     * Gets the state object for a child row.
     *
     * @param row
     *            the row to fetch the state for
     * @return the state object for the row
     */
    protected RowState getState(Row row) {
        int rowIndex = rows.indexOf(row);
        return getState().rows.get(rowIndex);
    }
}
