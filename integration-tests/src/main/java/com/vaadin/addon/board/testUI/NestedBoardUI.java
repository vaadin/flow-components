package com.vaadin.addon.board.testUI;

import com.vaadin.board.Board;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;

//This UI is causing an exception.
//Add test for it when the problem is fixed.
public class NestedBoardUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        Board board = new Board();
        board.setWidth(1200, Unit.PIXELS);
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");


        Board nestedBoard = new Board();
        nestedBoard.setSizeFull();

        Button nested1 = new Button("Nested 1");
        Button nested2 = new Button("Nested 2");
        Button nested3 = new Button("Nested 3");
        Button nested4 = new Button("Nested 4");
       nestedBoard.addRow(nested1, nested2, nested3, nested4);

        board.addRow(btn1, btn2, btn3, nestedBoard);


        setContent(board);
    }

}
