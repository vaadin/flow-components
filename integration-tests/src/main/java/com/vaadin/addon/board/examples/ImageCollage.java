package com.vaadin.addon.board.examples;

import java.io.File;
import java.net.URL;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;

/**
 * This example requires image resources.
 * You need to have files with images in the resource folder:
 * /resources/packagename/image-collage/small/pictureIndex.jpg
 *
 * For example for the package my.example:
 * resources/my/package/image-collage/small/1.jpg
 * resources/my/package/image-collage/small/2.jpg
 * resources/my/package/image-collage/small/3.jpg
 * resources/my/package/image-collage/small/4.jpg
 * resources/my/package/image-collage/small/5.jpg
 */
public class ImageCollage extends VerticalLayout {
    private FileResource[] resources;
    public ImageCollage() {
        Board board = new Board();
        createResources();

        //First row
        Row innerFirstRow = new Row();
        innerFirstRow.addComponents(createImageBox(2), createImageBox(3));

        board.addRow(createImageBox(1), innerFirstRow);

        //Second row
        board.addRow(createImageBox(24));

        //Third row
        board.addRow(createImageBox(5), createImageBox(6), createImageBox(7), createImageBox(8));

        //Fourth row
        Component twoColumnsFourthRow = createImageBox(10);
        Row fourthRow = board.addRow(createImageBox(9), twoColumnsFourthRow, createImageBox(11));
        fourthRow.setComponentSpan(twoColumnsFourthRow, 2);

        //Fifth row
        Component threeColumnsFifthRow = createImageBox(12);

        Row fifthRow = board.addRow(threeColumnsFifthRow, createImageBox(13));
        fifthRow.setComponentSpan(threeColumnsFifthRow, 3);

        //Sixth row
        Row firstGroupSixthRow = new Row();
        firstGroupSixthRow.addComponents(createImageBox(14), createImageBox(15));

        Row secondGroupSixthRow = new Row();
        secondGroupSixthRow.addComponents(createImageBox(16), createImageBox(17));

        Row thirdGroupSixthRow = new Row();
        thirdGroupSixthRow.addComponents(createImageBox(18), createImageBox(19));

        board.addRow(firstGroupSixthRow, secondGroupSixthRow, thirdGroupSixthRow);

        //Seventh row
        Row firstGroupSeventhRow = new Row();
        firstGroupSeventhRow.addComponents(createImageBox(20), createImageBox(21));

        Component twoColumnsSeventhRow = createImageBox(23);

        Row seventhRow = board.addRow(firstGroupSeventhRow, twoColumnsSeventhRow);
        seventhRow.setComponentSpan(twoColumnsSeventhRow, 2);

        //Eighth row
        board.addRow(createImageBox(4), createImageBox(25));

        addComponent(board);
    }

    private void createResources() {
        final String IMAGE_PATH = "image-collage/small/";
        URL folderResource = ImageCollage.class.getResource(IMAGE_PATH);
        File folder = new File((folderResource.getFile()));
        String[] fileNames = folder.list();
        resources = new FileResource[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            URL resource = ImageCollage.class.getResource(IMAGE_PATH + fileNames[i]);
            resources[i] = new FileResource(new File(resource.getFile()));
        }
    }
    private Component createImageBox(int n) {
        CssLayout container = new CssLayout();
        container.setStyleName("image-collage-item");

        n = (n - 1) % resources.length;
        Image image = new Image("", resources[n]);
        image.setSizeFull();

        container.addComponents(image);

        return container;
    }
}
