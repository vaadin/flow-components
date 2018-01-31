package com.vaadin.addon.board.testbenchtests;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.board.testUI.BasicBoard;

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
