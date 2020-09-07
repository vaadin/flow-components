package com.vaadin.flow.component.board.examples;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;

@Route("vaadin-board/RowTypes")
@StyleSheet("rowtypes.css")
@JsModule("rowtypes.js")
@BodySize
public class RowTypes extends Div {
    public RowTypes() {
        Board board = new Board();

        // First row
        board.addRow(createBox("1", "1/4"), createBox("2", "1/4"),
                createBox("3", "1/4"), createBox("4", "1/4"));

        // Second row
        Component twoColumnsAtSecondRow = createBox("1", "2/4");

        Row secondRow = board.addRow(twoColumnsAtSecondRow,
                createBox("2", "1/4"), createBox("3", "1/4"));
        secondRow.setComponentSpan(twoColumnsAtSecondRow, 2);

        // Third row
        Component twoColumnsAtThirdRow = createBox("3", "2/4");

        Row thirdRow = board.addRow(createBox("1", "1/4"),
                createBox("2", "1/4"), twoColumnsAtThirdRow);
        thirdRow.setComponentSpan(twoColumnsAtThirdRow, 2);

        // Fourth row
        Component twoColumnsAtFourthRow = createBox("2", "2/4");

        Row fourthRow = board.addRow(createBox("1", "1/4"),
                twoColumnsAtFourthRow, createBox("3", "1/4"));
        fourthRow.setComponentSpan(twoColumnsAtFourthRow, 2);

        // Fifth row
        board.addRow(createBox("1", "2/4"), createBox("2", "2/4"));

        // Sixth row
        Component threeColumnsAtSixthRow = createBox("1", "3/4");

        Row sixthRow = board.addRow(threeColumnsAtSixthRow,
                createBox("2", "1/4"));
        sixthRow.setComponentSpan(threeColumnsAtSixthRow, 3);

        // Seventh row
        Component threeColumnsAtSeventhRow = createBox("2", "3/4");

        Row seventhRow = board.addRow(createBox("1", "1/4"),
                threeColumnsAtSeventhRow);
        seventhRow.setComponentSpan(threeColumnsAtSeventhRow, 3);

        // Eighth row
        board.addRow(createBox("1", "1/3"), createBox("2", "1/3"),
                createBox("3", "1/3"));

        // Ninth row
        Component twoColumnsAtNinthRow = createBox("1", "2/3");

        Row ninthRow = board.addRow(twoColumnsAtNinthRow,
                createBox("2", "1/3"));
        ninthRow.setComponentSpan(twoColumnsAtNinthRow, 2);

        // Tenth row
        Component twoColumnsAtTenthRow = createBox("2", "2/3");

        Row tenthRow = board.addRow(createBox("1", "1/3"),
                twoColumnsAtTenthRow);
        tenthRow.setComponentSpan(twoColumnsAtTenthRow, 2);

        add(board);
    }

    public Component createBox(String number, String size) {
        // IE11 has an issue for calculating flex-basis if element has margin,
        // padding or border
        // Adding a wrapper fixes the issue
        Div boxContainer = new Div();

        Div innerContainer = new Div();

        Label numberLbl = new Label(number);
        numberLbl.addClassName("box__number");

        Label sizeLbl = new Label(size);
        sizeLbl.addClassName("box__size");

        innerContainer.add(numberLbl, sizeLbl);
        innerContainer.addClassName("box");

        boxContainer.add(innerContainer);

        return boxContainer;
    }
}
