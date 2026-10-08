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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link RuleBorderTracker}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>Storage methods: clear, has*, get* on the topBorders / leftBorders
 * maps.</li>
 * <li>HSSF skip: non-XSSF rules return the input cssIndex unchanged.</li>
 * <li>Null borderFormatting: same — cssIndex unchanged, no maps touched.</li>
 * <li>Per-side emission: right + bottom emit into the CSS builder (this rule's
 * own borders); top + left register spillover entries (border-bottom for cell
 * above, border-right for cell to the left) and advance cssIndex.</li>
 * <li>BorderStyle.NONE on a "set" border emits a default 1px solid grey rule
 * instead of the rule's own style.</li>
 * </ul>
 *
 * <p>
 * Uses {@link MockedStatic} for {@link POIReflectionTools#isBorderSet}. Mocks
 * the POI border-formatting chain and the ColorConverter.
 */
class RuleBorderTrackerTest {

    @Mock
    private ColorConverter colorConverter;
    @Mock
    private ConditionalFormatting cf;
    @Mock
    private XSSFConditionalFormattingRule xssfRule;
    @Mock
    private XSSFBorderFormatting borderFormatting;

    private RuleBorderTracker tracker;
    private MockedStatic<POIReflectionTools> reflectionStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tracker = new RuleBorderTracker();
        lenient().when(xssfRule.getBorderFormatting())
                .thenReturn(borderFormatting);
        // Default: no border on any side.
        reflectionStatic = mockStatic(POIReflectionTools.class);
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                any(BorderSide.class))).thenReturn(false);
        // Default: all sides return SOLID_THIN if asked.
        lenient().when(borderFormatting.getBorderLeft())
                .thenReturn(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        lenient().when(borderFormatting.getBorderRight())
                .thenReturn(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        lenient().when(borderFormatting.getBorderTop())
                .thenReturn(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        lenient().when(borderFormatting.getBorderBottom())
                .thenReturn(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        lenient()
                .when(colorConverter.getBorderColorCSS(any(), any(String.class),
                        any(XSSFBorderFormatting.class)))
                .thenReturn("color:#000;");
    }

    @AfterEach
    void tearDown() {
        reflectionStatic.close();
    }

    // ----- storage methods --------------------------------------------------

    @Test
    void freshTracker_hasNoBorders() {
        ConditionalFormatting anyCf = mock(ConditionalFormatting.class);
        assertFalse(tracker.hasTopBorder(anyCf));
        assertFalse(tracker.hasLeftBorder(anyCf));
    }

    @Test
    void clear_emptiesBothMaps() {
        // Trigger a left + top border emission to populate both maps.
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                eq(BorderSide.LEFT))).thenReturn(true);
        reflectionStatic.when(
                () -> POIReflectionTools.isBorderSet(any(), eq(BorderSide.TOP)))
                .thenReturn(true);
        HashMap<Integer, String> styles = new HashMap<>();
        tracker.addBorderFormatting(cf, xssfRule, new StringBuilder(), 0,
                styles, colorConverter);
        assertTrue(tracker.hasTopBorder(cf));
        assertTrue(tracker.hasLeftBorder(cf));

        tracker.clear();

        assertFalse(tracker.hasTopBorder(cf));
        assertFalse(tracker.hasLeftBorder(cf));
    }

    @Test
    void getTopBorder_returnsRecordedCssIndex() {
        reflectionStatic.when(
                () -> POIReflectionTools.isBorderSet(any(), eq(BorderSide.TOP)))
                .thenReturn(true);
        HashMap<Integer, String> styles = new HashMap<>();
        tracker.addBorderFormatting(cf, xssfRule, new StringBuilder(), 42,
                styles, colorConverter);

        assertEquals(42, tracker.getTopBorder(cf));
    }

    // ----- HSSF skip --------------------------------------------------------

    @Test
    void addBorderFormatting_nonXSSFRule_returnsCssIndexUnchanged() {
        // Pin: HSSF rules are not supported. The cssIndex passes through.
        ConditionalFormattingRule plainRule = mock(
                ConditionalFormattingRule.class);
        HashMap<Integer, String> styles = new HashMap<>();
        StringBuilder css = new StringBuilder();

        int returned = tracker.addBorderFormatting(cf, plainRule, css, 7,
                styles, colorConverter);

        assertEquals(7, returned);
        assertEquals(0, css.length(), "CSS untouched");
        assertEquals(0, styles.size(), "No spillover entries");
    }

    @Test
    void addBorderFormatting_nullBorderFormatting_returnsCssIndexUnchanged() {
        when(xssfRule.getBorderFormatting()).thenReturn(null);
        HashMap<Integer, String> styles = new HashMap<>();
        StringBuilder css = new StringBuilder();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 5, styles,
                colorConverter);

        assertEquals(5, returned);
        assertFalse(tracker.hasTopBorder(cf));
        assertFalse(tracker.hasLeftBorder(cf));
    }

    // ----- per-side emission ------------------------------------------------

    @Test
    void rightBorderSet_emitsBorderRightCss() {
        // Pin: right-set → "border-right:<style><color>" into the rule's
        // own CSS. No spillover map entry, cssIndex unchanged.
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                eq(BorderSide.RIGHT))).thenReturn(true);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 0, styles,
                colorConverter);

        assertTrue(css.toString().startsWith("border-right:"));
        assertEquals(0, returned, "cssIndex unchanged for right/bottom");
    }

    @Test
    void bottomBorderSet_emitsBorderBottomCss() {
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                eq(BorderSide.BOTTOM))).thenReturn(true);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 0, styles,
                colorConverter);

        assertTrue(css.toString().contains("border-bottom:"));
        assertEquals(0, returned);
    }

    @Test
    void topBorderSet_registersSpillover_andAdvancesCssIndex() {
        // Pin: top borders are rendered on the cell ABOVE's bottom edge.
        // The tracker registers the cssIndex in topBorders + writes the
        // emitted CSS into the conditionalFormattingStyles map at that
        // index. cssIndex advances by 1.
        reflectionStatic.when(
                () -> POIReflectionTools.isBorderSet(any(), eq(BorderSide.TOP)))
                .thenReturn(true);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 10,
                styles, colorConverter);

        assertEquals(11, returned);
        assertTrue(tracker.hasTopBorder(cf));
        assertEquals(10, tracker.getTopBorder(cf));
        assertTrue(styles.containsKey(10));
        assertTrue(styles.get(10).startsWith("border-bottom:"),
                "Spillover CSS is border-BOTTOM (for the cell above)");
    }

    @Test
    void leftBorderSet_registersSpillover_andAdvancesCssIndex() {
        // Pin: left borders are rendered on the cell to the LEFT's right
        // edge. cssIndex advances by 1.
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                eq(BorderSide.LEFT))).thenReturn(true);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 10,
                styles, colorConverter);

        assertEquals(11, returned);
        assertTrue(tracker.hasLeftBorder(cf));
        assertEquals(10, tracker.getLeftBorder(cf));
        assertTrue(styles.get(10).startsWith("border-right:"),
                "Spillover CSS is border-RIGHT (for the cell to the left)");
    }

    @Test
    void topAndLeftBorderSet_advancesCssIndexTwice() {
        // Pin: top + left both set → cssIndex advances by 2. Top is
        // emitted first (gets index N), left gets index N+1.
        reflectionStatic.when(
                () -> POIReflectionTools.isBorderSet(any(), eq(BorderSide.TOP)))
                .thenReturn(true);
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                eq(BorderSide.LEFT))).thenReturn(true);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 5, styles,
                colorConverter);

        assertEquals(7, returned);
        assertEquals(5, tracker.getTopBorder(cf));
        assertEquals(6, tracker.getLeftBorder(cf));
    }

    @Test
    void rightBorderSet_butStyleIsNone_emitsDefaultGreyStyle() {
        // Pin the BorderStyle.NONE special-case: an explicitly-set
        // "none" border emits a default 1px solid grey CSS instead of
        // skipping. This matches Excel's "none overrides previous rules"
        // semantic.
        reflectionStatic.when(() -> POIReflectionTools.isBorderSet(any(),
                eq(BorderSide.RIGHT))).thenReturn(true);
        when(borderFormatting.getBorderRight())
                .thenReturn(org.apache.poi.ss.usermodel.BorderStyle.NONE);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        tracker.addBorderFormatting(cf, xssfRule, css, 0, styles,
                colorConverter);

        // Default style: 1pt solid #d6d6d6.
        String result = css.toString();
        assertTrue(result.contains("border-right:"));
        assertTrue(result.contains("1pt solid #d6d6d6"),
                "Default grey style emitted for explicit-NONE borders");
    }

    @Test
    void topBorderSet_butStyleIsNone_emitsDefaultIntoOwnCssNotSpillover() {
        // Pin a non-obvious quirk: when top is "set" but the style is
        // NONE, the default grey is appended to the OWN css (not the
        // spillover map) and the spillover map is NOT populated. cssIndex
        // is unchanged.
        reflectionStatic.when(
                () -> POIReflectionTools.isBorderSet(any(), eq(BorderSide.TOP)))
                .thenReturn(true);
        when(borderFormatting.getBorderTop())
                .thenReturn(org.apache.poi.ss.usermodel.BorderStyle.NONE);
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 10,
                styles, colorConverter);

        assertEquals(10, returned, "cssIndex NOT advanced for NONE-top");
        assertFalse(tracker.hasTopBorder(cf),
                "topBorders not populated for NONE-top");
        assertTrue(css.toString().contains("1pt solid #d6d6d6"));
    }

    @Test
    void noBorderSet_returnsCssIndexUnchanged() {
        // Default isBorderSet stub returns false for all sides. Nothing
        // emitted, cssIndex unchanged, no spillover.
        StringBuilder css = new StringBuilder();
        HashMap<Integer, String> styles = new HashMap<>();

        int returned = tracker.addBorderFormatting(cf, xssfRule, css, 99,
                styles, colorConverter);

        assertEquals(99, returned);
        assertEquals(0, css.length());
        assertEquals(0, styles.size());
        assertFalse(tracker.hasTopBorder(cf));
        assertFalse(tracker.hasLeftBorder(cf));
    }

    @Test
    void multipleRules_eachGetsOwnEntries() {
        // Pin: the topBorders / leftBorders maps are keyed by
        // ConditionalFormatting (the parent), so different CFs with the
        // same rule shape get separate entries.
        ConditionalFormatting cf2 = mock(ConditionalFormatting.class);
        reflectionStatic.when(
                () -> POIReflectionTools.isBorderSet(any(), eq(BorderSide.TOP)))
                .thenReturn(true);
        HashMap<Integer, String> styles = new HashMap<>();

        tracker.addBorderFormatting(cf, xssfRule, new StringBuilder(), 0,
                styles, colorConverter);
        tracker.addBorderFormatting(cf2, xssfRule, new StringBuilder(), 1,
                styles, colorConverter);

        assertEquals(0, tracker.getTopBorder(cf));
        assertEquals(1, tracker.getTopBorder(cf2));
    }
}
