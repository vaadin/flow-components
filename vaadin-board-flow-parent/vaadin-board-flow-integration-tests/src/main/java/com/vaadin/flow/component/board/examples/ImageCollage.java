package com.vaadin.flow.component.board.examples;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;

/**
 * This example requires image resources. You need to have files with images in
 * the webapp folder: <code>src/main/webapp/image-collage/small</code>
 */
@Route("ImageCollage")
@BodySize
@CssImport("styles.css")
public class ImageCollage extends Div {
    private static final String IMAGE_PATH = "image-collage/small/";
    private List<String> imageUrls = new ArrayList<>();

    public ImageCollage() {
        Board board = new Board();
        findImages();

        // First row
        Row innerFirstRow = new Row();
        innerFirstRow.add(createImageBox(2), createImageBox(3));

        board.addRow(createImageBox(1), innerFirstRow);

        // Second row
        board.addRow(createImageBox(24));

        // Third row
        board.addRow(createImageBox(5), createImageBox(6), createImageBox(7),
                createImageBox(8));

        // Fourth row
        Component twoColumnsFourthRow = createImageBox(10);
        Row fourthRow = board.addRow(createImageBox(9), twoColumnsFourthRow,
                createImageBox(11));
        fourthRow.setComponentSpan(twoColumnsFourthRow, 2);

        // Fifth row
        Component threeColumnsFifthRow = createImageBox(12);

        Row fifthRow = board.addRow(threeColumnsFifthRow, createImageBox(13));
        fifthRow.setComponentSpan(threeColumnsFifthRow, 3);

        // Sixth row
        Row firstGroupSixthRow = new Row();
        firstGroupSixthRow.add(createImageBox(14), createImageBox(15));

        Row secondGroupSixthRow = new Row();
        secondGroupSixthRow.add(createImageBox(16), createImageBox(17));

        Row thirdGroupSixthRow = new Row();
        thirdGroupSixthRow.add(createImageBox(18), createImageBox(19));

        board.addRow(firstGroupSixthRow, secondGroupSixthRow,
                thirdGroupSixthRow);

        // Seventh row
        Row firstGroupSeventhRow = new Row();
        firstGroupSeventhRow.add(createImageBox(20), createImageBox(21));

        Component twoColumnsSeventhRow = createImageBox(23);

        Row seventhRow = board.addRow(firstGroupSeventhRow,
                twoColumnsSeventhRow);
        seventhRow.setComponentSpan(twoColumnsSeventhRow, 2);

        // Eighth row
        board.addRow(createImageBox(4), createImageBox(25));

        add(board);
    }

    private void findImages() {
        URL folderResource;
        try {
            folderResource = VaadinServlet.getCurrent().getServletContext()
                    .getResource("/" + IMAGE_PATH);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        File folder = new File((folderResource.getFile()));
        String[] fileNames = folder.list();
        Arrays.sort(fileNames);
        for (int i = 0; i < fileNames.length; i++) {
            imageUrls.add(IMAGE_PATH + fileNames[i]);
        }
    }

    private Component createImageBox(int n) {
        n = (n - 1) % imageUrls.size();

        Image image = new Image();
        image.setSrc(imageUrls.get(n));
        image.setSizeFull();
        image.addClassName("image-collage-item");

        // IE11 has an issue for calculating flex-basis if element has margin,
        // padding or border
        // Adding a wrapper fixes the issue
        Div container = new Div();
        container.add(image);
        return container;
    }
}
