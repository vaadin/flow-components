package com.vaadin.addon.board.testUI;

import com.vaadin.addon.board.examples.RowTypes;
import com.vaadin.server.VaadinRequest;

public class RowTypesUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        RowTypes rowTypes = new RowTypes();
        setContent(rowTypes);
    }

}
