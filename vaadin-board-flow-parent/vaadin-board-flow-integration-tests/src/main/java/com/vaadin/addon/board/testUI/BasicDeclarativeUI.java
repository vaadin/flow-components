package com.vaadin.addon.board.testUI;

import com.vaadin.addon.board.declarative.BasicDeclarative;
import com.vaadin.server.VaadinRequest;

public class BasicDeclarativeUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        BasicDeclarative content = new BasicDeclarative();
        setContent(content);
    }

}
