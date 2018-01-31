package com.vaadin.addon.board.testUI;

import com.vaadin.addon.board.examples.ImageCollage;
import com.vaadin.addon.board.examples.RowTypes;
import com.vaadin.server.VaadinRequest;

public class ImageCollageUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        ImageCollage imageCollage = new ImageCollage();
        setContent(imageCollage);
    }

}
