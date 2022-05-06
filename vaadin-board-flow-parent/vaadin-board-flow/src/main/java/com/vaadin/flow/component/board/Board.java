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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.board.internal.FunctionCaller;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/board", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-board", version = "23.1.0-beta1")
@JsModule("@vaadin/board/vaadin-board.js")
public class Board extends Component
        implements HasSize, HasStyle, HasOrderedComponents {

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

    /**
     * Forces the board to be redrawn.
     * <p>
     * This method typically only needs to be called if you change CSS (through
     * a variable or otherwise) which affects the size of the board or the
     * breakpoints used. Otherwise, the component will be redrawn automatically
     * when needed.
     */
    public void redraw() {
        FunctionCaller.callOnceOnClientReponse(this, "redraw");
    }

}
