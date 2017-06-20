package com.vaadin.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Element;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.board.client.BoardState;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.declarative.DesignContext;
import com.vaadin.ui.declarative.DesignException;

/**
 * Vaadin Board is a Vaadin Add-On Component which allows creating responsive
 * layouts.
 * <p>
 * Vaadin Board consists of {@link Row}s where you can add any Vaadin component.
 * Each Row consists of four columns, and can contain up to four components
 * taking one column each, or fewer components with multiple columns each as
 * long as sum of columns stays less than or equal to four.
 * 
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
 * <p>
 * See more examples in Vaadin Docs or the online demos.
 *
 * @see <a href=
 *      "https://vaadin.com/docs/-/part/board/board-overview.html">Vaadin
 *      Docs</a>
 * @see <a href="https://demo.vaadin.com/vaadin-board/">Vaadin Board Demo</a>
 *
 */
@HtmlImport("frontend://vaadin-board/vaadin-board.html")
@HtmlImport("frontend://vaadin-board/vaadin-board-row.html")
public class Board extends AbstractComponent implements HasComponents {

    private final List<Row> rows = new ArrayList<>();

    /**
     * Creates an empty board.
     * <p>
     * Use {@link #addRow(Component...)} to add content to the board.
     **/
    public Board() {
        this.setWidth(100, Unit.PERCENTAGE);
    }

    @Override
    public BoardState getState() {
        return (BoardState) super.getState();
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
        Row row = new Row();
        rows.add(row);
        row.setParent(this);

        row.addComponents(components);
        markAsDirty();
        return row;
    }

    /**
     * Removes the given row from the board.
     *
     * @param row
     *            to be removed
     **/
    public void removeRow(Row row) {
        if (rows.remove(row)) {
            row.setParent(null);
            markAsDirty();
        }
    }

    @Override
    public Iterator<Component> iterator() {
        return Collections.<Component> unmodifiableCollection(rows).iterator();
    }

    @Override
    public void readDesign(Element design, DesignContext designContext) {
        super.readDesign(design, designContext);
        // handle children
        for (Element childComponent : design.children()) {
            Component child = designContext.readDesign(childComponent);
            if (child instanceof Row) {
                Row row = (Row) child;
                rows.add(row);
                row.setParent(this);
            } else {
                throw new DesignException(
                        "<vaadin-board> can have only <vaadin-board-row> as a child component.");
            }
        }
    }

    @Override
    public void writeDesign(Element design, DesignContext designContext) {
        super.writeDesign(design, designContext);
        Iterator<Component> iter = iterator();
        while (iter.hasNext()) {
            Component comp = iter.next();
            if (comp instanceof Row) {
                Element childElement = designContext.createElement(comp);
                design.appendChild(childElement);
            } else {
                throw new DesignException(
                        "Board can have only Row as a child component.");
            }
        }
    }
}
