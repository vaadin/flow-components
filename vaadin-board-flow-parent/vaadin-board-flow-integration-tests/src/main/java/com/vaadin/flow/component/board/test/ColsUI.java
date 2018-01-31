package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 *
 */
public class ColsUI extends AbstractView {
    public ColsUI() {
        Board board = new Board();
        board.setSizeFull();

        Button btnA = new Button("Button A");
        Button btnB = new Button("Button B");
        Button btnC = new Button("Button C");
        Row row = board.addRow(btnA, btnB, btnC);
        row.setComponentSpan(btnA, 2);

        Button btnRemove = new Button("remove",
                e -> row.setComponentSpan(btnA, 1));
        Button btnException = new Button("exception",
                e -> row.setComponentSpan(btnA, 4));

        VerticalLayout layout = new VerticalLayout();
        layout.add(board, btnRemove, btnException);
        add(layout);
    }

}
