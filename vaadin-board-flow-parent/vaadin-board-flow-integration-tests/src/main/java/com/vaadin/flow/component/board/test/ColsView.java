package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ColsView extends AbstractView {
    public ColsView() {
        Board board = new Board();
        board.setSizeFull();

        Button btnA = new Button("Button A");
        Button btnB = new Button("Button B");
        Button btnC = new Button("Button C");
        btnA.setId("A");
        btnB.setId("B");
        btnC.setId("C");
        Row row = board.addRow(btnA, btnB, btnC);
        row.setComponentSpan(btnA, 2);

        Button btnRemove = new Button("remove",
                e -> row.setComponentSpan(btnA, 1));
        btnRemove.setId("remove");
        Button btnException = new Button("exception",
                e -> row.setComponentSpan(btnA, 4));
        btnException.setId("exception");

        VerticalLayout layout = new VerticalLayout();
        layout.add(board, btnRemove, btnException);
        add(layout);
    }

}
