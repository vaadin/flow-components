package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import elemental.css.CSSStyleDeclaration.Unit;

public class RowRemoveView extends AbstractView {

    public final static String RMV_BUTTON_ID = "rmvbutton";

    public RowRemoveView() {
        VerticalLayout layout = new VerticalLayout();
        Board board = new Board();
        board.setWidth("700px");
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");
        Button btn4 = new Button("Button 4");

        Row row1 = board.addRow(btn1, btn2);
        board.addRow(btn3, btn4);

        Button rmvButton = new Button("Remove row", e -> {
            board.removeRow(row1);
        });
        rmvButton.setId(RMV_BUTTON_ID);
        layout.add(board, rmvButton);
        add(layout);
    }

}
