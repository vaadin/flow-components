package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.server.StreamResource;

@SuppressWarnings("serial")
public abstract class SheetOverlayWrapper implements Serializable {

    public interface OverlayChangeListener extends Serializable {
        void overlayChanged();
    }

    private static long counter;

    private final ClientAnchor anchor;
    private final String id;

    private boolean visible;

    public SheetOverlayWrapper(ClientAnchor anchor) {
        this.anchor = anchor;
        id = "sheet-overlay-" + counter;
        counter++;
    }

    /**
     * If this overlay's state can be dynamically changed (like minimizing),
     * this method can inform the spreadsheet.
     *
     * @param listener
     */
    public void setOverlayChangeListener(OverlayChangeListener listener) {
        // NOP
    }

    /**
     * Determines if this image should be visible within the given visible area.
     *
     * @param r1
     *            Row index of topmost row, 1-based
     * @param c1
     *            Column index of leftmost column, 1-based
     * @param r2
     *            Row index of bottom-most row, 1-based
     * @param c2
     *            Column index of rightmost column, 1-based
     *
     * @return true if the image should be visible inside the range, false
     *         otherwise
     */
    public boolean isVisible(int r1, int c1, int r2, int c2) {
        int col1 = anchor.getCol1() + 1;
        int col2 = anchor.getCol2() + 1;
        int row1 = anchor.getRow1() + 1;
        int row2 = anchor.getRow2() + 1;

        // sanity check
        if (r2 - r1 < 0 || c2 - c1 < 0) {
            return false;
        }

        boolean col1isBetweenc1andc2 = c1 <= col1 && col1 <= c2;

        boolean col2isBetweenc1andc2 = c1 <= col2 && col2 <= c2;

        boolean inColumnRange = col1isBetweenc1andc2 || col2isBetweenc1andc2;

        boolean row1isBetweenr1andr2 = r1 <= row1 && row1 <= r2;

        boolean row2isBetweenr1andr2 = r1 <= row2 && row2 <= r2;

        boolean inRowRange = row1isBetweenr1andr2 || row2isBetweenr1andr2;

        return inColumnRange && inRowRange;
    }

    /**
     * Returns the coordinate of the left edge of the image inside the leftmost
     * column this image occupies. Value is converted to pixels.
     *
     * @param sheet
     *            The Sheet this image is in
     * @return coordinate of image's left edge in PX
     */
    float getDx1(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return (float) anchor.getDx1() / Units.EMU_PER_PIXEL;
        } else {
            return sheet.getColumnWidthInPixels(anchor.getCol1())
                    * anchor.getDx1() / 1023f;
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
    private float getDx2(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return (float) anchor.getDx2() / Units.EMU_PER_PIXEL;
        } else {
            return sheet.getColumnWidthInPixels(anchor.getCol2())
                    * anchor.getDx2() / 1023f;
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
    float getDy1(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return (float) anchor.getDy1() / Units.EMU_PER_POINT;
        } else {
            Row row = sheet.getRow(anchor.getRow1());
            return (row == null ? sheet.getDefaultRowHeightInPoints()
                    : row.getHeightInPoints()) * anchor.getDy1() / 255f;
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
    private float getDy2(Sheet sheet) {
        if (anchor instanceof XSSFClientAnchor) {
            return (float) anchor.getDy2() / Units.EMU_PER_POINT;
        } else {
            Row row = sheet.getRow(anchor.getRow2());
            return (row == null ? sheet.getDefaultRowHeightInPoints()
                    : row.getHeightInPoints()) * anchor.getDy2() / 255f;
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

    /**
     * Returns a unique ID of this overlay, used also as a resource key for
     * images.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the visibility state of this overlay in the current spreadsheet
     * view.
     *
     * @return true if overlay is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Marks this as image visible or hidden in the current spreadsheet view.
     * Only used for the spreadsheet to remember if data needs to be removed,
     * doesn't affect real visibility.
     *
     * @param visible
     *            true to set visible, false to set hidden
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets the anchor for this image within the sheet containing this image.
     *
     * @return Anchor for this image
     */
    public ClientAnchor getAnchor() {
        return anchor;
    }

    public StreamResource getResource() {
        return null;
    }

    /**
     * Returns the component contained in this wrapper if there is one.
     *
     * @param init
     *            false if you don't want to initialize the component, calling
     *            with true after the first time has no effect.
     */
    public Component getComponent(final boolean init) {
        return null;
    }

    public abstract OverlayInfo.Type getType();

    @Override
    public String toString() {
        String anchor = ", anchor=null";

        if (getAnchor() != null) {
            anchor = ", col1=" + getAnchor().getCol1() + ", col2="
                    + getAnchor().getCol2() + ", row1=" + getAnchor().getRow1()
                    + ", row2=" + getAnchor().getRow2() + ", dx1="
                    + getAnchor().getDx1() + ", dx2=" + getAnchor().getDx2()
                    + ", dy1=" + getAnchor().getDy1() + ", dy2="
                    + getAnchor().getDy2() + ", type="
                    + getAnchor().getAnchorType();
        }

        return "OverlayData [resourceKey=" + getId() + anchor + "]";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

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
        SheetOverlayWrapper other = (SheetOverlayWrapper) obj;
        if (getAnchor() == null) {
            if (other.getAnchor() != null) {
                return false;
            }
        } else if (!getAnchor().equals(other.getAnchor())) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}