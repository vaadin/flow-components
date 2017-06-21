package com.vaadin.addon.board.examples;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


public class RowTypes extends VerticalLayout {
    public RowTypes() {
        Board board = new Board();

        //First row
        board.addRow(
            createBox("1", "1/4"),
            createBox("2", "1/4"),
            createBox("3", "1/4"),
            createBox("4", "1/4")
        );

        //Second row
        Component twoColumnsAtSecondRow = createBox("1", "2/4");

        Row secondRow = board.addRow(
            twoColumnsAtSecondRow,
            createBox("2", "1/4"),
            createBox("3", "1/4")
        );
        secondRow.setComponentSpan(twoColumnsAtSecondRow, 2);

        //Third row
        Component twoColumnsAtThirdRow = createBox("3", "2/4");

        Row thirdRow = board.addRow(
            createBox("1", "1/4"),
            createBox("2", "1/4"),
            twoColumnsAtThirdRow
        );
        thirdRow.setComponentSpan(twoColumnsAtThirdRow, 2);

        //Fourth row
        Component twoColumnsAtFourthRow = createBox("2", "2/4");

        Row fourthRow = board.addRow(
            createBox("1", "1/4"),
            twoColumnsAtFourthRow,
            createBox("3", "1/4")
        );
        fourthRow.setComponentSpan(twoColumnsAtFourthRow, 2);

        //Fifth row
        board.addRow(
            createBox("1", "2/4"),
            createBox("2", "2/4")
        );

        //Sixth row
        Component threeColumnsAtSixthRow = createBox("1", "3/4");

        Row sixthRow = board.addRow(
            threeColumnsAtSixthRow,
            createBox("2", "1/4")
        );
        sixthRow.setComponentSpan(threeColumnsAtSixthRow, 3);

        //Seventh row
        Component threeColumnsAtSeventhRow = createBox("2", "3/4");

        Row seventhRow = board.addRow(
                createBox("1", "1/4"),
                threeColumnsAtSeventhRow
        );
        seventhRow.setComponentSpan(threeColumnsAtSeventhRow, 3);

        //Eighth row
        board.addRow(
                createBox("1", "1/3"),
                createBox("2", "1/3"),
                createBox("3", "1/3")
        );

        //Ninth row
        Component twoColumnsAtNinthRow = createBox("1", "2/3");

        Row ninthRow = board.addRow(
                twoColumnsAtNinthRow,
                createBox("2", "1/3")
        );
        ninthRow.setComponentSpan(twoColumnsAtNinthRow, 2);

        //Tenth row
        Component twoColumnsAtTenthRow = createBox("2", "2/3");

        Row tenthRow = board.addRow(
                createBox("1", "1/3"),
                twoColumnsAtTenthRow
        );
        tenthRow.setComponentSpan(twoColumnsAtTenthRow, 2);

        this.addComponent(board);
    }

    public Component createBox(String number, String size) {
        CssLayout boxContainer = new CssLayout();

        Label numberLbl = new Label(number);
        numberLbl.addStyleName("box__number");

        Label sizeLbl = new Label(size);
        sizeLbl.addStyleName("box__size");

        boxContainer.addComponents(numberLbl, sizeLbl);
        boxContainer.addStyleName("box");

        return boxContainer;
    }
}
