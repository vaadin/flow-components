package com.vaadin.addon.board.examples;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.FileResource;
import com.vaadin.ui.*;

import java.io.File;
import java.net.URL;

public class ImageCollage extends VerticalLayout {
    public ImageCollage() {
        Board board = new Board();


        //First row
        Row innerFirstRow = new Row();
        innerFirstRow.addComponents(createImageBox("02"), createImageBox("03"));

        board.addRow(createImageBox("01"), innerFirstRow);

        //Second row
        board.addRow(createImageBox("24"));

        //Third row
        board.addRow(createImageBox("05"), createImageBox("06"), createImageBox("07"), createImageBox("08"));

        //Fourth row
        Component twoColumnsFourthRow = createImageBox("10");
        Row fourthRow = board.addRow(createImageBox("09"), twoColumnsFourthRow, createImageBox("11"));
        fourthRow.setCols(twoColumnsFourthRow, 2);

        //Fifth row
        Component threeColumnsFifthRow = createImageBox("12");

        Row fifthRow = board.addRow(threeColumnsFifthRow, createImageBox("13"));
        fifthRow.setCols(threeColumnsFifthRow, 3);

        //Sixth row
        Row firstGroupSixthRow = new Row();
        firstGroupSixthRow.addComponents(createImageBox("14"), createImageBox("15"));

        Row secondGroupSixthRow = new Row();
        secondGroupSixthRow.addComponents(createImageBox("16"), createImageBox("17"));

        Row thirdGroupSixthRow = new Row();
        thirdGroupSixthRow.addComponents(createImageBox("18"), createImageBox("19"));

        board.addRow(firstGroupSixthRow, secondGroupSixthRow, thirdGroupSixthRow);

        //Seventh row
        Row firstGroupSeventhRow = new Row();
        firstGroupSeventhRow.addComponents(createImageBox("20"), createImageBox("21"));

        Component twoColumnsSeventhRow = createImageBox("23");

        Row seventhRow = board.addRow(firstGroupSeventhRow, twoColumnsSeventhRow);
        seventhRow.setCols(twoColumnsSeventhRow, 2);

        //Eighth row
        board.addRow(createImageBox("04"), createImageBox("25"));

        addComponent(board);
    }

    private Component createImageBox(String imageName) {
        CssLayout container = new CssLayout();
        container.setStyleName("image-collage-item");

        URL resource = ImageCollage.class.getResource("image-collage/small/" + imageName + ".jpg");
        FileResource fileResource = new FileResource(new File(resource.getFile()));

        Image image = new Image("", fileResource);
        image.setSizeFull();

        container.addComponents(image);

        return container;
    }
}
