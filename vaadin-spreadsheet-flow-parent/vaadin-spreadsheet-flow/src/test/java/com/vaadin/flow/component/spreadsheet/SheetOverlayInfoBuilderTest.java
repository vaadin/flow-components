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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;

import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo.Type;

/**
 * Building the client data for an overlay with {@link SheetOverlayInfoBuilder}.
 *
 * <p>
 * Pins the two paths of {@code build}:
 *
 * <ol>
 * <li>{@code DONT_MOVE_AND_RESIZE} on {@link XSSFClientAnchor} → absolute
 * pixel/EMU coordinates; col=1, row=1; dx/dy/width/height from
 * {@code xssfAnchor.getPosition()} / {@code getSize()}.</li>
 * <li>Regular cell-anchored case → skip leading hidden columns / rows from
 * {@code col1} / {@code row1}; compute width/height via
 * {@code overlayWrapper.getWidth} / {@code getHeight}; dx / dy set only if the
 * original leading column / row was NOT hidden.</li>
 * </ol>
 *
 * <p>
 * Mocks the parent {@link Spreadsheet}, the {@link SheetOverlayWrapper}, the
 * anchor types, and the XML-bean shape types ({@link CTPoint2D},
 * {@link CTPositiveSize2D}). Uses {@code lenient()} stubs because not every
 * test exercises every getter.
 */
class SheetOverlayInfoBuilderTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SheetOverlayWrapper overlay;
    @Mock
    private ClientAnchor anchor;
    @Mock
    private XSSFClientAnchor xssfAnchor;
    @Mock
    private Sheet sheet;

    private SheetOverlayInfoBuilder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        builder = new SheetOverlayInfoBuilder(spreadsheet);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.isColumnHidden(anyInt())).thenReturn(false);
        lenient().when(spreadsheet.isRowHidden(anyInt())).thenReturn(false);
        lenient().when(overlay.getType()).thenReturn(Type.IMAGE);
        lenient().when(overlay.getHeight(any(), any())).thenReturn(100f);
        lenient().when(overlay.getWidth(any(), any(), anyInt()))
                .thenReturn(150f);
        lenient().when(overlay.getDx1(any())).thenReturn(5f);
        lenient().when(overlay.getDy1(any())).thenReturn(7f);
    }

    // ----- XSSF DONT_MOVE_AND_RESIZE path -----------------------------------

    @Test
    void dontMoveAndResize_onXSSFAnchor_returnsAbsoluteCoordinates() {
        // Pin the early-return path (issue #3261 workaround). The XSSF
        // anchor's getPosition().getX/Y and getSize().getCx/getCy values
        // drive the dx / dy / width / height; col and row are forced to 1.
        when(overlay.getAnchor()).thenReturn(xssfAnchor);
        when(xssfAnchor.getAnchorType())
                .thenReturn(AnchorType.DONT_MOVE_AND_RESIZE);
        CTPoint2D position = mockPosition(914400L, 685800L);
        CTPositiveSize2D size = mockSize(1828800L, 1371600L);
        when(xssfAnchor.getPosition()).thenReturn(position);
        when(xssfAnchor.getSize()).thenReturn(size);

        OverlayInfo info = builder.build(overlay);

        assertEquals(1, info.col);
        assertEquals(1, info.row);
        // EMU_PER_PIXEL = 9525 — 914400/9525 = 96 (1 inch in pixels).
        assertEquals(914400L / Units.EMU_PER_PIXEL, info.dx);
        // dy uses EMU_PER_POINT, asymmetric with dx (which uses _PIXEL).
        // Pinned as-is — this asymmetry is intentional (matches the
        // client-side rendering expectation).
        assertEquals(685800L / Units.EMU_PER_POINT, info.dy);
        assertEquals(1828800L / Units.EMU_PER_PIXEL, info.width);
        assertEquals(1371600L / Units.EMU_PER_POINT, info.height);
    }

    @Test
    void dontMoveAndResize_onNonXSSFAnchor_takesRegularPath() {
        // Pin: the special-case is XSSF-only. A non-XSSF anchor with the
        // same anchor type goes through the cell-anchored path.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType())
                .thenReturn(AnchorType.DONT_MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 2);
        when(anchor.getRow1()).thenReturn(3);

        OverlayInfo info = builder.build(overlay);

        // 1-based: anchor col1=2 → info.col=3; row1=3 → info.row=4.
        assertEquals(3, info.col);
        assertEquals(4, info.row);
    }

    // ----- Regular cell-anchored path ---------------------------------------

    @Test
    void regularAnchor_setsColAndRow1Based() {
        // Pin the +1 conversion from POI's 0-based anchor col1/row1 to the
        // OverlayInfo's 1-based fields.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 4);
        when(anchor.getRow1()).thenReturn(7);

        OverlayInfo info = builder.build(overlay);

        assertEquals(5, info.col);
        assertEquals(8, info.row);
    }

    @Test
    void regularAnchor_skipsLeadingHiddenColumns() {
        // Pin: the col loop advances while the current column is hidden,
        // starting from anchor.col1. If col1=2 and columns 2+3 are hidden,
        // the visible col is 4 (POI 0-based) → 5 in OverlayInfo.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 2);
        when(anchor.getRow1()).thenReturn(0);
        when(spreadsheet.isColumnHidden(2)).thenReturn(true);
        when(spreadsheet.isColumnHidden(3)).thenReturn(true);

        OverlayInfo info = builder.build(overlay);

        assertEquals(5, info.col); // 4 + 1
    }

    @Test
    void regularAnchor_skipsLeadingHiddenRows() {
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 0);
        when(anchor.getRow1()).thenReturn(5);
        when(spreadsheet.isRowHidden(5)).thenReturn(true);
        when(spreadsheet.isRowHidden(6)).thenReturn(true);

        OverlayInfo info = builder.build(overlay);

        assertEquals(8, info.row); // 7 + 1
    }

    @Test
    void regularAnchor_dxSetIfLeadingColumnNotHidden() {
        // Pin the conditional dx assignment: dx comes from
        // overlayWrapper.getDx1(sheet) ONLY when the original column
        // wasn't shifted by the hidden-skip loop.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 3);
        when(anchor.getRow1()).thenReturn(2);
        when(overlay.getDx1(sheet)).thenReturn(11f);
        when(overlay.getDy1(sheet)).thenReturn(13f);

        OverlayInfo info = builder.build(overlay);

        assertEquals(11f, info.dx);
        assertEquals(13f, info.dy);
    }

    @Test
    void regularAnchor_dxIsZeroIfLeadingColumnHidden() {
        // Pin: if the leading column WAS shifted (because it was hidden),
        // dx stays at its default (0 from OverlayInfo's primitive float
        // field). dy is unaffected.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 3);
        when(anchor.getRow1()).thenReturn(2);
        when(spreadsheet.isColumnHidden(3)).thenReturn(true);
        when(overlay.getDy1(sheet)).thenReturn(13f);

        OverlayInfo info = builder.build(overlay);

        assertEquals(0f, info.dx,
                "Hidden leading column should leave dx at default 0");
        assertEquals(13f, info.dy);
    }

    @Test
    void regularAnchor_widthAndHeightFromWrapper() {
        // Pin: width and height come from the overlay's own measurement
        // methods, not from the anchor. The builder forwards the active
        // sheet + the spreadsheet's row-height / col-width helpers.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 0);
        when(anchor.getRow1()).thenReturn(0);
        when(overlay.getHeight(any(), any())).thenReturn(42f);
        when(overlay.getWidth(any(), any(), anyInt())).thenReturn(57f);

        OverlayInfo info = builder.build(overlay);

        assertEquals(42f, info.height);
        assertEquals(57f, info.width);
    }

    @Test
    void overlayType_propagatesIntoBuiltInfo() {
        // Pin: OverlayInfo.type comes from overlay.getType(). Non-IMAGE
        // types should flow through.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 0);
        when(anchor.getRow1()).thenReturn(0);
        when(overlay.getType()).thenReturn(Type.COMPONENT);

        OverlayInfo info = builder.build(overlay);

        assertEquals(Type.COMPONENT, info.type);
    }

    // ----- helpers ----------------------------------------------------------

    private CTPoint2D mockPosition(long x, long y) {
        CTPoint2D position = org.mockito.Mockito.mock(CTPoint2D.class);
        when(position.getX()).thenReturn(x);
        when(position.getY()).thenReturn(y);
        return position;
    }

    private CTPositiveSize2D mockSize(long cx, long cy) {
        CTPositiveSize2D size = org.mockito.Mockito
                .mock(CTPositiveSize2D.class);
        when(size.getCx()).thenReturn(cx);
        when(size.getCy()).thenReturn(cy);
        return size;
    }
}
