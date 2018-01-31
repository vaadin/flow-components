package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.examples.RowTypes;
import com.vaadin.server.VaadinRequest;

public class RowTypesUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        RowTypes rowTypes = new RowTypes();
        setContent(rowTypes);
    }

}
