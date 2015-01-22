package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.Arrays;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFShape;

import com.vaadin.server.StreamResource;

/**
 * SheetImageWrapper is an utility class of the Spreadsheet component. In
 * addition to the image resource, this wrapper contains the images visibility
 * state, position and size.
 * 
 * @author Vaadin Ltd.
 */
public class SheetImageWrapper {

    private StreamResource resource;

    private static long counter;

    private ClientAnchor anchor;
    private byte[] data;
    private String resourceKey;
    private boolean visible;

    private String MIMEType;

    public SheetImageWrapper() {
        // FIXME We probably need to use another way to give a unique resource
        // key, otherwise restarting server will start using old keys and thus
        // browsers might load the wrong image from cache.
        resourceKey = "sheet-image-" + counter;
        counter++;
    }

    /**
     * Determines if this image should be visible within the given visible area.
     * 
     * @param c1
     *            Column index of leftmost column, 1-based
     * @param c2
     *            Column index of rightmost column, 1-based
     * @param r1
     *            Row index of topmost row, 1-based
     * @param r2
     *            Row index of bottom-most row, 1-based
     * @return true if the image should be visible inside the range, false
     *         otherwise
     */
    public boolean isVisible(int c1, int c2, int r1, int r2) {
        int col1 = anchor.getCol1() + 1;
        int col2 = anchor.getCol2() + 1;
        int row1 = anchor.getRow1() + 1;
        int row2 = anchor.getRow2() + 1;
        return ((col1 >= c1 && c1 <= col2 || col2 >= c1 && col2 <= c2) && (row1 >= r1
                && row1 <= r2 || row2 >= r1 && row2 <= r2));
    }

    /**
     * Returns the coordinate of the left edge of the image inside the leftmost
     * column this image occupies. Value is converted to pixels.
     * 
     * @param sheet
     *            The Sheet this image is in
     * @return coordinate of image's left edge in PX
     */
    public float getDx1(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return anchor.getDx1() / XSSFShape.EMU_PER_PIXEL;
        } else {
            return ExcelToHtmlUtils.getColumnWidthInPx(sheet
                    .getColumnWidth(anchor.getCol1())) * anchor.getDx1() / 1023;
        }
    }

    /**
     * Returns the coordinate of the right edge of the image inside the
     * rightmost column this image occupies. Value is converted to pixels.
     * 
     * @param sheet
     *            The Sheet this image is in
     * @return coordinate of image's right edge in PX
     */
    public float getDx2(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return anchor.getDx2() / XSSFShape.EMU_PER_PIXEL;
        } else {
            return ExcelToHtmlUtils.getColumnWidthInPx(sheet
                    .getColumnWidth(anchor.getCol2())) * anchor.getDx2() / 1023;
        }
    }

    /**
     * Returns the coordinate of the top edge of the image inside the topmost
     * row this image occupies. Value is converted to pixels.
     * 
     * @param sheet
     *            The Sheet this image is in
     * @return coordinate of image's top edge in PX
     */
    public float getDy1(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return anchor.getDy1() / XSSFShape.EMU_PER_POINT;
        } else {
            Row row = sheet.getRow(anchor.getRow1());
            return (row == null ? sheet.getDefaultRowHeightInPoints() : row
                    .getHeightInPoints()) * anchor.getDy1() / 255;
        }
    }

    /**
     * Returns the coordinate of the bottom edge of the image inside the bottom
     * row this image occupies. Value is converted to pixels.
     * 
     * @param sheet
     *            The Sheet this image is in
     * @return coordinate of image's bottom edge in PX
     */
    public float getDy2(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return anchor.getDy2() / XSSFShape.EMU_PER_POINT;
        } else {
            Row row = sheet.getRow(anchor.getRow2());
            return (row == null ? sheet.getDefaultRowHeightInPoints() : row
                    .getHeightInPoints()) * anchor.getDy2() / 255;
        }
    }

    /**
     * Calculates the width of the image. Might not be 100% correct because of
     * bugs in POI (returns inconsistent values for Dx and Dy).
     * <p>
     * If the image doesn't have a specified width and should be sized to image
     * file size, -1 is returned.
     * 
     * @param sheet
     *            The sheet this image belongs to
     * @param colW
     *            Array of column widths in pixels
     * @param defaultColumnWidthPX
     *            Default column width in pixels
     * @return Width of the image in pixels, or -1 if image file width should be
     *         used
     */
    public float getWidth(Sheet sheet, int colW[], int defaultColumnWidthPX) {
        float width;
        short col1 = anchor.getCol1();
        short col2 = anchor.getCol2();
        width = getDx2(sheet) - getDx1(sheet);
        if (col1 < col2) {
            for (int i = col1; i < col2; i++) {
                if (!sheet.isColumnHidden(i)) {
                    if (i < colW.length) {
                        width += colW[i];
                    } else {
                        width += defaultColumnWidthPX;
                    }
                }
            }
        } else if (col1 > col2) {
            // for some reason POI (3.9) gives a col2 of 0 for certain
            // type of anchors ..
            width = -1.0F;
        } // else col1 == col2 -> keep dx2-dx1
        return width;
    }

    /**
     * Calculates the height of the image. Might not be 100% correct because of
     * bugs in POI (returns inconsistent values for Dx and Dy).
     * <p>
     * If the image doesn't have a specified height and should be sized to image
     * file size, -1 is returned.
     * 
     * @param sheet
     *            The sheet this image belongs to
     * @param rowH
     *            Array of row heights in points
     * @return Image height in points, or -1 if image file height should be
     *         used.
     */
    public float getHeight(Sheet sheet, float[] rowH) {
        float height;
        int row1 = anchor.getRow1();
        int row2 = anchor.getRow2();
        height = getDy2(sheet) - getDy1(sheet);
        if (row1 < row2) {
            for (int i = row1; i < row2; i++) {
                Row row = sheet.getRow(i);
                if (row == null || !row.getZeroHeight()) {
                    if (i < rowH.length) {
                        height += rowH[i];
                    } else {
                        height += sheet.getDefaultRowHeightInPoints();
                    }
                }
            }
        } else if (row1 > row2) {
            // for some reason POI (3.9) gives a rol2 of 0 for some
            // type of anchors..
            height = -1.0F;
        } // else row1 == row2 -> keep dy2-dy1
        return height;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((MIMEType == null) ? 0 : MIMEType.hashCode());
        result = prime * result + ((anchor == null) ? 0 : anchor.hashCode());
        result = prime * result + Arrays.hashCode(data);
        result = prime * result
                + ((resourceKey == null) ? 0 : resourceKey.hashCode());
        result = prime * result + (visible ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SheetImageWrapper other = (SheetImageWrapper) obj;
        if (MIMEType == null) {
            if (other.MIMEType != null) {
                return false;
            }
        } else if (!MIMEType.equals(other.MIMEType)) {
            return false;
        }
        if (anchor == null) {
            if (other.anchor != null) {
                return false;
            }
        } else if (!anchor.equals(other.anchor)) {
            return false;
        }
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        if (resourceKey == null) {
            if (other.resourceKey != null) {
                return false;
            }
        } else if (!resourceKey.equals(other.resourceKey)) {
            return false;
        }
        if (visible != other.visible) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ImageData [resourceKey=" + resourceKey + ", col1="
                + anchor.getCol1() + ", col2=" + anchor.getCol2() + ", row1="
                + anchor.getRow1() + ", row2=" + anchor.getRow2() + ", dx1="
                + anchor.getDx1() + "/" + anchor.getDx1() + ", dx2="
                + anchor.getDx2() + "/" + anchor.getDx2() + ", dy1="
                + anchor.getDy1() + "/" + anchor.getDy1() + ", dy2="
                + anchor.getDy2() + "/" + anchor.getDy2() + ", MIMEType="
                + MIMEType + ", type=" + anchor.getAnchorType() + "]";
    }

    /**
     * Gets the resource key of this image
     * 
     * @return Resource key
     */
    public String getResourceKey() {
        return resourceKey;
    }

    /**
     * Gets the visibility state of this image
     * 
     * @return true if image is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gets the resource containing this image
     * 
     * @return Image resource
     */
    public Object getResource() {
        return resource;
    }

    /**
     * Gets the image data as a byte array.
     * 
     * @return Image data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets this image visible or hidden
     * 
     * @param visible
     *            true to set visible, false to set hidden
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets the MIME type of this image
     * 
     * @return MIME type
     */
    public String getMIMEType() {
        return MIMEType;
    }

    /**
     * Sets the resource for this image
     * 
     * @param resource
     *            Image resource
     */
    public void setResource(StreamResource resource) {
        this.resource = resource;
    }

    /**
     * Gets the anchor for this image within the sheet containing this image.
     * 
     * @return Anchor for this image
     */
    public ClientAnchor getAnchor() {
        return anchor;
    }

    /**
     * Sets the anchor for this image within the sheet containing this image.
     * 
     * @param anchor
     *            Anchor for this image
     */
    public void setAnchor(ClientAnchor anchor) {
        this.anchor = anchor;
    }

    /**
     * Sets the MIME type of this image
     * 
     * @param mimeType
     *            MIME type of this image
     */
    public void setMIMEType(String mimeType) {
        MIMEType = mimeType;
    }

    /**
     * Sets the image data of this image.
     * 
     * @param data
     *            Image data
     */
    public void setData(byte[] data) {
        this.data = data;
    }
}