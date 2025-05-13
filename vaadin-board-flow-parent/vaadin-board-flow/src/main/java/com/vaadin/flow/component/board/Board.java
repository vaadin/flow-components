/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.board;

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
 */
@Tag("vaadin-board")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/board", version = "24.8.0-alpha18")
@JsModule("@vaadin/board/src/vaadin-board.js")
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
