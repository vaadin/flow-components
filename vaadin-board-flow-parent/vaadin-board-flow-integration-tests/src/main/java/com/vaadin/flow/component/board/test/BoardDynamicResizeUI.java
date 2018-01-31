package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BoardDynamicResizeUI extends AbstractView {

    public BoardDynamicResizeUI() {
        VerticalLayout layout = new VerticalLayout();
        Board board = new Board();
        board.setSizeFull();

        Label lbl1 = new Label("Label 1");
        Label lbl2 = new Label("Label 2");
        Label lbl3 = new Label("Label 3");
        board.addRow(lbl1, lbl2, lbl3);

        Button button = new Button("resize");
        button.addClickListener(e -> {
            board.setWidth("300px");
        });
        layout.add(board, button);
        add(layout);
    }

}
