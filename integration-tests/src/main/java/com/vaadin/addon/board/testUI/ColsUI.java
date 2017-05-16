package com.vaadin.addon.board.testUI;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
public class ColsUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {

        Board board = new Board();
        board.setSizeFull();

        Button btnA = new Button("Button A");
        Button btnB = new Button("Button B");
        Button btnC = new Button("Button C");
        Row row = board.addRow(btnA, btnB, btnC);
        row.setCols(btnA, 2);

        Button btnRemove = new Button("remove", e -> row.setCols(btnA, 1));
        Button btnException = new Button("exception", e -> row.setCols(btnA, 4));

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(board, btnRemove, btnException);
        setContent(layout);
    }

}
