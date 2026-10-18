/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The pictures and other overlays of the sheet shown: given to the client while
 * a pane shows them, where they start and how big they are, taken off when
 * scrolled away or another sheet is shown, read from POI again when sizes
 * change, and their components put on and taken off the sheet.
 */
class SpreadsheetOverlaysTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /** A picture of one pixel. */
    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final XSSFSheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    /** A picture over the cells, as "F12:G13", 8 pixels into the first. */
    private void picture(String cells) {
        CellRangeAddress range = CellRangeAddress.valueOf(cells);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(8 * Units.EMU_PER_PIXEL,
                8 * Units.EMU_PER_PIXEL, 0, 0, range.getFirstColumn(),
                range.getFirstRow(), range.getLastColumn(), range.getLastRow());
        drawing.createPicture(anchor,
                workbook.addPicture(PNG, Workbook.PICTURE_TYPE_PNG));
    }

    /**
     * Shows the sheet frozen at two rows and two columns, as the client shows
     * rows 10-20 of columns E-H.
     */
    private void show() {
        sheet.createFreezePane(2, 2);
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        showRows(10, 20);
    }

    private void showRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 5, \"lastRow\": " + last
                        + ", \"lastColumn\": 8}");
    }

    /** The cells the overlays the client has start at. */
    private List<String> startCells() {
        List<String> cells = new ArrayList<>();
        Map<String, OverlayInfo> overlays = spreadsheet.getOverlays();
        if (overlays != null) {
            for (OverlayInfo info : overlays.values()) {
                cells.add(new CellReference(info.row - 1, info.col - 1)
                        .formatAsString());
            }
        }
        return cells.stream().sorted().toList();
    }

    private OverlayInfo onlyOverlay() {
        assertEquals(1, spreadsheet.getOverlays().size());
        return spreadsheet.getOverlays().values().iterator().next();
    }

    @Test
    void thePicturesShown_areGivenToTheClient_inEveryPane() {
        picture("A1:A1");
        picture("F1:F1");
        picture("A12:A12");
        picture("F12:F12");
        picture("C5:C5");

        show();

        assertEquals(List.of("A1", "A12", "F1", "F12"), startCells());
    }

    @Test
    void thePicturesOnTheEdgesOfEachPane_areGivenToTheClient_andNoneBeyond() {
        for (String cell : List.of("B2", "H2", "B20", "H20", "E10", "I20",
                "H21", "C10", "E3")) {
            picture(cell + ":" + cell);
        }

        show();

        assertEquals(List.of("B2", "B20", "E10", "H2", "H20"), startCells());
    }

    @Test
    void aPictureLargerThanTheView_isGivenToTheClient_scrolledToItsMiddle() {
        // Both its corners are outside the rows and columns shown, which used
        // to count as not shown.
        picture("D5:K30");

        show();

        assertEquals(List.of("D5"), startCells());
    }

    @Test
    void aPictureLargerThanTheView_isTakenOff_scrolledPastIt() {
        picture("D5:K30");
        show();

        showRows(31, 40);

        assertEquals(List.of(), startCells());
    }

    @Test
    void aPictureShown_startsWhereItsAnchorDoes_withItsOffsetAndSize() {
        picture("F12:G13");

        show();

        OverlayInfo info = onlyOverlay();
        assertEquals(6, info.col);
        assertEquals(12, info.row);
        assertEquals(8f, info.dx);
        assertTrue(info.dy > 0, "dy " + info.dy);
        assertTrue(info.width > 0 && info.height > 0,
                info.width + "x" + info.height);
        assertEquals(OverlayInfo.Type.IMAGE, info.type);
    }

    @Test
    void aPictureShown_hasItsImageServed_untilItIsScrolledAway() {
        picture("F12:F12");
        show();
        String id = spreadsheet.getOverlays().keySet().iterator().next();
        assertTrue(spreadsheet.getResources().containsKey(id));

        showRows(30, 40);

        assertEquals(List.of(), startCells());
        assertFalse(spreadsheet.getResources().containsKey(id));
    }

    @Test
    void aPictureOverHiddenColumnsOrRowsOnly_isNotShown() {
        picture("F12:G12");
        picture("H15:H16");
        sheet.setColumnHidden(5, true);
        sheet.setColumnHidden(6, true);
        sheet.createRow(14).setZeroHeight(true);
        sheet.createRow(15).setZeroHeight(true);

        show();

        assertEquals(List.of(), startCells());
    }

    @Test
    void aPictureStartingInAHiddenColumn_startsInTheNextShown_atItsEdge() {
        picture("F12:G12");
        sheet.setColumnHidden(5, true);

        show();

        OverlayInfo info = onlyOverlay();
        assertEquals(7, info.col);
        assertEquals(0f, info.dx);
        assertTrue(info.dy > 0);
    }

    @Test
    void aPictureStartingInAHiddenRow_startsInTheNextShown_atItsEdge() {
        picture("F12:F13");
        sheet.createRow(11).setZeroHeight(true);

        show();

        OverlayInfo info = onlyOverlay();
        assertEquals(13, info.row);
        assertEquals(0f, info.dy);
        assertEquals(8f, info.dx);
    }

    @Test
    void aPictureNotSizedWithItsCells_isShownOverAHiddenColumn() {
        // An anchor without an end cell does not size with the cells.
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        drawing.createPicture(drawing.createAnchor(0, 0, 0, 0, 5, 11, 0, 0),
                workbook.addPicture(PNG, Workbook.PICTURE_TYPE_PNG));
        sheet.setColumnHidden(5, true);

        show();

        assertEquals(1, spreadsheet.getOverlays().size());
    }

    /** An overlay of its own, with a component. */
    private static class ComponentOverlay extends SheetOverlayWrapper {
        private final Component component = new Span();
        private OverlayChangeListener listener;
        private float width = 10;
        private final boolean withId;

        ComponentOverlay(XSSFClientAnchor anchor, boolean withId) {
            super(anchor);
            this.withId = withId;
        }

        @Override
        public Component getComponent(boolean init) {
            return component;
        }

        @Override
        public String getId() {
            return withId ? super.getId() : null;
        }

        @Override
        public float getWidth(Sheet sheet, int[] colW,
                int defaultColumnWidthPX) {
            return width;
        }

        @Override
        public void setOverlayChangeListener(OverlayChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public OverlayInfo.Type getType() {
            return OverlayInfo.Type.COMPONENT;
        }
    }

    private ComponentOverlay componentOverlayInF12(boolean withId) {
        return new ComponentOverlay(
                new XSSFClientAnchor(0, 0, 0, 0, 5, 11, 6, 12), withId);
    }

    private boolean onTheSheet(Component component) {
        return spreadsheet.getElement()
                .equals(component.getElement().getParent());
    }

    @Test
    void anOverlaysComponent_isOnTheSheet_whileTheOverlayIsShown() {
        show();
        ComponentOverlay overlay = componentOverlayInF12(true);
        spreadsheet.addSheetOverlay(overlay);
        showRows(11, 21);
        assertTrue(onTheSheet(overlay.component));
        assertTrue(overlay.isVisible());
        assertEquals(List.of("F12"), startCells());

        showRows(30, 40);

        assertFalse(onTheSheet(overlay.component));
        assertFalse(overlay.isVisible());
    }

    @Test
    void anOverlayAdded_staysThroughLaterLoads_afterTheSheetWasResized() {
        picture("G13:G13");
        show();
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [8], \"newWidths\": [80]}");
        ComponentOverlay overlay = componentOverlayInF12(true);
        spreadsheet.addSheetOverlay(overlay);

        showRows(11, 21);

        assertTrue(onTheSheet(overlay.component));
    }

    @Test
    void anOverlayChanged_isGivenToTheClientAgain() {
        show();
        ComponentOverlay overlay = componentOverlayInF12(true);
        spreadsheet.addSheetOverlay(overlay);
        showRows(11, 21);
        assertEquals(10f, onlyOverlay().width);

        overlay.width = 20;
        overlay.listener.overlayChanged();

        assertEquals(20f, onlyOverlay().width);
    }

    @Test
    void anOverlayWithoutAnId_hasOnlyItsComponentShown() {
        show();
        ComponentOverlay overlay = componentOverlayInF12(false);
        spreadsheet.addSheetOverlay(overlay);

        showRows(11, 21);

        assertTrue(onTheSheet(overlay.component));
        assertEquals(List.of(), startCells());
        assertEquals(null, overlay.listener);
    }

    @Test
    void anOverlayNotMovedOrSizedWithCells_isShownWhereverItIs() {
        CTPoint2D position = CTPoint2D.Factory.newInstance();
        position.setX(30L * Units.EMU_PER_PIXEL);
        position.setY(40L * Units.EMU_PER_POINT);
        CTPositiveSize2D size = CTPositiveSize2D.Factory.newInstance();
        size.setCx(50L * Units.EMU_PER_PIXEL);
        size.setCy(60L * Units.EMU_PER_POINT);
        XSSFClientAnchor anchor = new XSSFClientAnchor(sheet, position, size) {
        };
        anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        show();
        spreadsheet.addSheetOverlay(new ComponentOverlay(anchor, true));

        showRows(300, 310);

        OverlayInfo info = onlyOverlay();
        assertEquals(List.of(1, 1, 30, 40, 50, 60),
                List.of(info.col, info.row, (int) info.dx, (int) info.dy,
                        (int) info.width, (int) info.height));
    }

    @Test
    void aPictureOfAnOldWorkbookNotMovedWithItsCells_isShownAtItsCell() {
        HSSFWorkbook old = new HSSFWorkbook();
        HSSFSheet oldSheet = old.createSheet();
        HSSFPatriarch patriarch = oldSheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = patriarch.createAnchor(0, 0, 0, 0, 5, 11, 6,
                12);
        anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        patriarch.createPicture(anchor,
                old.addPicture(PNG, Workbook.PICTURE_TYPE_PNG));
        spreadsheet = new Spreadsheet(old);
        ui.add(spreadsheet);

        showRows(10, 20);

        assertEquals(List.of("F12"), startCells());
    }

    @Test
    void aPictureAddedInPoi_isShownOnceTheSheetHasNoOthers() {
        show();
        picture("F12:F12");

        showRows(11, 21);

        assertEquals(List.of("F12"), startCells());
    }

    @Test
    void aPictureAddedInPoi_isShownOnceTheSpreadsheetIsReloaded() {
        picture("F12:F12");
        show();
        picture("G13:G13");
        // A column resized sizes the pictures shown, rather than read them
        // all from POI again, which gave the client each under a new key.
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [8], \"newWidths\": [80]}");
        assertEquals(List.of("F12"), startCells());

        spreadsheet.reload();
        showRows(10, 20);

        assertEquals(List.of("F12", "G13"), startCells());
    }

    @Test
    void anotherSheetShown_takesThePicturesOff() {
        workbook.createSheet("Second");
        picture("F12:F12");
        show();
        String id = spreadsheet.getOverlays().keySet().iterator().next();

        spreadsheet.setActiveSheetIndex(1);

        assertEquals(List.of(), startCells());
        assertFalse(spreadsheet.getResources().containsKey(id));
    }
}
