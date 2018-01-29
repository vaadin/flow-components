package com.vaadin.addon.board.testUI;

import com.vaadin.board.Board;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("Basic")
public class BasicBoard extends Board {

    public BasicBoard() {
        setWidth("100%");
        NativeButton btn1 = new NativeButton("Button 1");
        NativeButton btn2 = new NativeButton("Button 2");
        NativeButton btn3 = new NativeButton("Button 3");
        NativeButton btn4 = new NativeButton("Button 4");
        btn1.setWidth("100%");
        btn2.setWidth("100%");
        btn3.setWidth("100%");
        btn4.setWidth("100%");
        add(btn1, btn2, btn3, btn4);
    }

}
