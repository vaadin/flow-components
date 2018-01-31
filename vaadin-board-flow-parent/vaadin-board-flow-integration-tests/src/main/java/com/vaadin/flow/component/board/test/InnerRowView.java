package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class InnerRowView extends AbstractView {

    public final static String BUTTON_ADD_ID = "btnadd";
    public final static String BUTTON_RMV_ID = "btnrmv";

    public InnerRowView() {
        VerticalLayout layout = new VerticalLayout();
        Board board = new Board();
        board.setSizeFull();
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");

        Row nestedRow = new Row();
        nestedRow.setWidth("100%");

        Button inner1 = new Button("Inner 1");
        Button inner2 = new Button("Inner 2");
        Button inner3 = new Button("Inner 3");
        Button inner4 = new Button("Inner 4");
        nestedRow.add(inner1, inner2, inner3, inner4);

        Row outterRow = board.addRow(btn1, btn2, btn3);

        Button button = new Button("Add Inner row");
        button.setId(BUTTON_ADD_ID);
        button.addClickListener(e -> outterRow.addNestedRow(nestedRow));

        Button buttonRmv = new Button("Remove Inner row");
        buttonRmv.setId(BUTTON_RMV_ID);
        buttonRmv.addClickListener(e -> outterRow.remove(nestedRow));

        layout.add(board, button, buttonRmv);
        add(layout);
    }

}
