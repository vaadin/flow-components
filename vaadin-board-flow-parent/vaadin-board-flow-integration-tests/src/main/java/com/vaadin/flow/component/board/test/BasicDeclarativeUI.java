package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.declarative.BasicDeclarative;
import com.vaadin.server.VaadinRequest;

public class BasicDeclarativeUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        BasicDeclarative content = new BasicDeclarative();
        setContent(content);
    }

}
