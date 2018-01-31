package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;

public class RemoveComponentView extends AbstractView {

    public RemoveComponentView() {
        Board board = new Board();
        board.setWidth("700px");
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");
        Button btn4 = new Button("Button 4");
        Row row = board.addRow(btn1, btn2, btn3, btn4);
        row.remove(btn2);
        row.remove(btn4);
        add(board);
    }

}
