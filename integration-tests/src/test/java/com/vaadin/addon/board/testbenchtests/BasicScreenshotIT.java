package com.vaadin.addon.board.testbenchtests;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.board.testUI.BasicUI;

public class BasicScreenshotIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return BasicUI.class;
    }

    @Test
    public void basicLayout_boardTabletSize_twoRowsAndTwoItemsInRow() throws IOException {
        openURL();
        compareScreen("basicUItest");
    }

}
