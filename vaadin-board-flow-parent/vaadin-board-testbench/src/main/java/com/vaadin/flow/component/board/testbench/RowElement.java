package com.vaadin.flow.component.board.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * This class represents one row inside the Vaadin Board.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-board-row")
public class RowElement extends TestBenchElement {

    /**
     * Returns all the immediate child elements of this row.
     *
     * @return all immediate child elements of this row
     */
    public List<TestBenchElement> getChildren() {
        return getPropertyElements("children");
    }
}
