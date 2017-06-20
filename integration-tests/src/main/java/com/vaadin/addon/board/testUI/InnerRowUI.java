package com.vaadin.addon.board.testUI;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

public class InnerRowUI extends AbstractTestUI {

    public final static String BUTTON_ADD_ID="btnadd";
    public final static String BUTTON_RMV_ID="btnrmv";
    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        Board board = new Board();
        board.setSizeFull();
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");


        Row innerRow = new Row();
        innerRow.setWidth("100%");

        Button inner1 = new Button("Inner 1");
        Button inner2 = new Button("Inner 2");
        Button inner3 = new Button("Inner 3");
        Button inner4 = new Button("Inner 4");
        innerRow.addComponents(inner1, inner2, inner3, inner4);

        Row outterRow = board.addRow(btn1, btn2, btn3);

        Button button = new Button("Add Inner row");
        button.setId(BUTTON_ADD_ID);
        button.addClickListener(e-> outterRow.addComponent(innerRow));

        Button buttonRmv = new Button("Remove Inner row");
        buttonRmv.setId(BUTTON_RMV_ID);
        buttonRmv.addClickListener(e-> outterRow.removeComponent(innerRow));

        layout.addComponents(board, button, buttonRmv);
        setContent(layout);
    }

}
