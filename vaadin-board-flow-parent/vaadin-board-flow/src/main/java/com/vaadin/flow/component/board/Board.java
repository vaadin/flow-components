package com.vaadin.flow.component.board;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * Vaadin Board allows creating responsive layouts in an easy way.
 * <p>
 * A Board consists of {@link Row}s where you can add any Vaadin component. Each
 * Row consists of four columns, and can contain up to four components taking
 * one column each, or fewer components with multiple columns each as long as
 * sum of columns stays less than or equal to four.
 * <p>
 * Here is a simple usage example:
 *
 * <pre>
 * Board board = new Board();
 * Label lbl1 = new Label("LABEL1");
 * Label lbl2 = new Label("LABEL2");
 * Label lbl3 = new Label("LABEL3");
 * Label lbl4 = new Label("LABEL4");
 * board.addRow(lbl1, lbl2, lbl3, lbl4);
 * </pre>
 */
@Tag("vaadin-board")
@HtmlImport("frontend://bower_components/vaadin-board/vaadin-board.html")
public class Board extends Component
        implements HasSize, HasStyle, HasOrderedComponents<Board> {

    /**
     * Creates an empty board.
     * <p>
     * Use {@link #addRow(Component...)} to add content to the board.
     **/
    public Board() {
        setWidth("100%");
    }

    /**
     * Creates a new row and adds the given components to the row.
     * <p>
     * All the added components have cols set to 1, i.e. use one slot in the
     * row. The number of slots in the row is the number of added components.
     *
     * @param components
     *            components to add, no more than 4
     * @throws IllegalArgumentException
     *             if there are more than 4 components
     * @return a row instance which can be used for further configuration
     **/
    public Row addRow(Component... components) {
        Row row = new Row(components);
        add(row);
        return row;
    }

    /**
     * Removes the given row from the board.
     *
     * @param row
     *            the row to be removed
     **/
    public void removeRow(Row row) {
        remove(row);
    }

}
