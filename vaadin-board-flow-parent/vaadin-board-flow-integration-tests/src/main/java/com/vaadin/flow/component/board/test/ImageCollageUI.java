package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.examples.ImageCollage;
import com.vaadin.flow.component.board.examples.RowTypes;
import com.vaadin.server.VaadinRequest;

public class ImageCollageUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        ImageCollage imageCollage = new ImageCollage();
        setContent(imageCollage);
    }

}
