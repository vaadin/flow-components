package com.vaadin.flow.component.board.test;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.flow.component.board.test.BasicBoard;

public class BasicScreenshotIT extends AbstractParallelTest {

    @Test
    public void basicLayout_boardTabletSize_twoRowsAndTwoItemsInRow()
            throws IOException {
        open(BasicBoard.class);
        compareScreen("basicUItest");
    }

}
