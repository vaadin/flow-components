package com.vaadin.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.board.client.BoardState;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

@HtmlImport("vaadin://bower_components/vaadin-board/vaadin-board.html")
@HtmlImport("vaadin://bower_components/vaadin-board/vaadin-board-row.html")
public class Board extends PolymerComponent implements HasComponents {

    private final List<Row> rows = new ArrayList<>();

    /**
     * Creates an empty board.
     *
     * Use #addRow(Row) to add content to the board.
     **/
    public Board() {
    }

    @Override
    public BoardState getState() {
        return (BoardState) super.getState();
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
        rows.add(row);
        row.setParent(this);

        row.addComponents(components);
        markAsDirty();
        return row;
    }

    @Override
    public Iterator<Component> iterator() {
        return Collections.unmodifiableCollection((List) rows).iterator();
    }

}
