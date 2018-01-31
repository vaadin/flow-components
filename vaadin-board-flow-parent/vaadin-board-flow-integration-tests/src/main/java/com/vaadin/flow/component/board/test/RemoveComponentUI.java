package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;

public class RemoveComponentUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        Board board = new Board();
        board.setWidth(700, Unit.PIXELS);
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");
        Button btn4 = new Button("Button 4");
        Row row = board.addRow(btn1, btn2, btn3, btn4);
        row.removeComponent(btn2);
        row.removeComponent(btn4);
        setContent(board);
    }

}
