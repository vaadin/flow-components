package com.vaadin.flow.component.board.test;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.flow.component.board.test.BasicBoard;

public class BasicScreenshotIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return BasicBoard.class;
    }

    @Test
    public void basicLayout_boardTabletSize_twoRowsAndTwoItemsInRow() throws IOException {
        openURL();
        compareScreen("basicUItest");
    }

}
