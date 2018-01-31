package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;

public class BoardColsUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        //First Button will take 50 % of available space
        Board board = new Board();
        board.setWidth(1400, Unit.PIXELS);
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");

        Row row = board.addRow(btn1, btn2, btn3);
        row.setComponentSpan(btn1, 2);
        setContent(board);
    }

}
