/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ImageIT extends AbstractSpreadsheetIT {

    private SpreadsheetElement spreadsheet;

    @Before
    public void init() {
        getDriver().manage().window().setSize(WINDOW_SIZE_LARGE);
        open();
        loadFile("picture_sheet.xlsx");
        spreadsheet = $(SpreadsheetElement.class).first();
        waitForImages();
    }

    /**
     * Wait for the sheet's images to arrive.
     *
     * Loading a file returns once the cells are on screen; overlays are sent on
     * a later roundtrip, so nothing is there to measure yet.
     * {@code pictureType2_width} already waits like this after scrolling — the
     * same wait is needed after the initial load.
     */
    private void waitForImages() {
        waitUntil(d -> !findElementsInShadowRoot(
                By.cssSelector(".sheet-overlay-image")).isEmpty());
    }

    @Test
    public void pictureType0_width() {
        assertInRange(200, imageWidth("C2"), 260);
        assertInRange(240, imageWidth("G4"), 260);
        assertInRange(340, imageWidth("K2"), 360);
        assertInRange(15, imageWidth("R2"), 25);
    }

    @Test
    public void pictureType2_width() {
        spreadsheet.scroll(680);
        waitUntil(e -> findElementInShadowRoot(
                By.cssSelector(cellToCSS("G42") + " img")) != null);

        assertInRange(250, imageWidth("C38"), 280);
        assertInRange(240, imageWidth("G42"), 260);
        assertInRange(390, imageWidth("K38"), 420);
        assertInRange(15, imageWidth("R37"), 25);
    }

    @Test
    public void pictureType3_width_position() {
        spreadsheet.scroll(1300);

        // Get all type3 pictures (absolute position)
        var type3 = findElementsInShadowRoot(
                By.cssSelector(cellToCSS("A1") + " img")).stream()
                .map(image -> image.getRect())
                .collect(Collectors.toCollection(ArrayList::new));
        Assert.assertEquals(3, type3.size());

        // Sort the pictures by their position starting from the left
        type3.sort((a, b) -> a.x - b.x);

        // Test picture width
        assertInRange(200, type3.get(0).width, 300);
        assertInRange(240, type3.get(1).width, 270);
        assertInRange(340, type3.get(2).width, 400);

        // Test picture position
        var C72 = getCellElement("C72").getRect();
        assertInRange(C72.x, type3.get(0).x, C72.x + C72.width);
        assertInRange(C72.y, type3.get(0).y, C72.y + C72.height);

        var G76 = getCellElement("G76").getRect();
        // TODO: Image scaling in general has slight distortion in Spreadsheet.
        // Need to add some tolerance to the test until it's fixed.
        assertInRange(G76.x, type3.get(1).x, G76.x + G76.width + 100);
        assertInRange(G76.y, type3.get(1).y, G76.y + G76.height);

        var K72 = getCellElement("K72").getRect();
        assertInRange(K72.x, type3.get(2).x, K72.x + K72.width + 200);
        assertInRange(K72.y, type3.get(2).y, K72.y + K72.height);
    }

    @Test
    public void shouldUpdateImageURIOnReattach() {
        loadFile("group_image.xlsx");
        waitForImages();
        var image = findElementInShadowRoot(
                By.cssSelector(cellToCSS("D3") + " img"));
        var originalSrc = image.getAttribute("src");
        // Scroll to the bottom so the image is detached. The sheet keeps half
        // a screen loaded behind what it shows, and the image reaches far
        // enough down to stay in that at 1000 pixels.
        spreadsheet.scroll(10000);

        // Scroll back to top so the image is reattached
        spreadsheet.scroll(0);
        // The image element comes back as soon as the client repaints, still
        // carrying the URL of the registration the server dropped on the way
        // out. Waiting for the element alone samples that stale src; wait for
        // the re-registered URL to land instead.
        waitUntil(e -> {
            var found = findElementsInShadowRoot(
                    By.cssSelector(cellToCSS("D3") + " img"));
            return !found.isEmpty()
                    && !originalSrc.equals(found.get(0).getAttribute("src"));
        });
        image = findElementInShadowRoot(
                By.cssSelector(cellToCSS("D3") + " img"));

        var newSrc = image.getAttribute("src");
        Assert.assertNotEquals(
                "Image src should be updated on reattach to avoid broken images",
                originalSrc, newSrc);
    }

    @Test
    public void shouldLoadImageOnLockedCell() {
        loadFile("image-locked-cell.xlsx");
        // Wait for image to appear in the spreadsheet
        var image = waitUntil(
                e -> findElementInShadowRoot(By.cssSelector("img")));
        Assert.assertNotNull("Image should be present", image);
        // Verify no 404 errors in browser console (would indicate broken
        // stream resource)
        checkLogsForErrors();
    }

    private double imageWidth(String cell) {
        return findElementInShadowRoot(By.cssSelector(cellToCSS(cell) + " img"))
                .getSize().width;
    }
}
