/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.spreadsheet.testbench.AddressUtil;
import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractSpreadsheetIT extends AbstractComponentIT {

    private SpreadsheetElement spreadsheet;
    private static final String BACKGROUND_COLOR = "background-color";

    public static final Dimension WINDOW_SIZE_LARGE = new Dimension(1920, 1080);
    public static final Dimension WINDOW_SIZE_MEDIUM = new Dimension(768, 1024);
    public static final Dimension WINDOW_SIZE_SMALL = new Dimension(375, 667);

    @Before
    public void sharedInit() {
        getDriver().manage().window().setSize(WINDOW_SIZE_LARGE);
    }

    private TestBenchElement getSpreadsheetInShadowRoot() {
        var spreadsheet = $(SpreadsheetElement.class).first();
        return spreadsheet.$(DivElement.class).first();
    }

    /**
     * Slice 178: deep-search through the spreadsheet's nested shadow roots (the
     * Lit cell-grid, column-header, row-header, formula-bar, and sheet-tabs
     * each own their own shadow root). Maps the legacy GWT selectors to their
     * Lit-DOM equivalents on the fly so the existing IT call sites don't need
     * bulk rewriting.
     */
    protected WebElement findElementInShadowRoot(By by) {
        WebElement el = findInLitShadowRoots(toCssSelector(by));
        if (el == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "no such element in any spreadsheet shadow root: " + by);
        }
        return el;
    }

    protected List<WebElement> findElementsInShadowRoot(By by) {
        return findAllInLitShadowRoots(toCssSelector(by));
    }

    /**
     * Convert a Selenium {@link By} locator to its equivalent CSS selector
     * string. Selenium's {@code By.toString()} returns
     * {@code "By.cssSelector: <sel>"} or {@code "By.className: <name>"};
     * normalize both shapes to a plain CSS selector. Slice 178.
     */
    private String toCssSelector(By by) {
        String raw = by.toString();
        if (raw.startsWith("By.cssSelector: ")) {
            return raw.substring("By.cssSelector: ".length());
        }
        if (raw.startsWith("By.className: ")) {
            return "." + raw.substring("By.className: ".length());
        }
        if (raw.startsWith("By.tagName: ")) {
            return raw.substring("By.tagName: ".length());
        }
        return raw;
    }

    /**
     * Translate a legacy GWT-era CSS selector to its Lit-DOM equivalent.
     * Returns the input unchanged when no translation applies. Slice 178.
     */
    private String translateSelector(String selector) {
        // Headers — GWT used .ch / .rh class on header cells; Lit
        // uses .col-header-cell / .row-header-cell.
        selector = selector.replace(".ch.col", ".col-header-cell.col");
        selector = selector.replace(".rh.row", ".row-header-cell.row");
        // Sheet tab — GWT used .sheet-tabsheet for the strip +
        // .sheet-tabsheet-tab.selected-tab for the active tab.
        selector = selector.replace(".sheet-tabsheet-tab.selected-tab",
                ".sheet-tab.active");
        // Formula bar — GWT used input.addressfield + .functionfield.
        selector = selector.replace("input.addressfield",
                "[part~=\"address-field\"]");
        selector = selector.replace(".addressfield",
                "[part~=\"address-field\"]");
        selector = selector.replace(".functionfield",
                "[part~=\"formula-field\"]");
        // Cell editor input.
        selector = selector.replace("input[id='cellinput']",
                "[part~=\"cell-input\"]");
        // Selection corner drag handle — GWT called it .s-corner.
        selector = selector.replace(".s-corner", ".selection-corner");
        // Popup (filter) button on a table header cell.
        selector = selector.replace(".popupbutton", ".popup-button");
        // Cell comment indicator. The Lit element exposes it as a
        // documented part rather than a class.
        selector = selector.replace(".cell-comment-triangle",
                "[part~=\"comment-triangle\"]");
        // Column grouping outline. GWT rendered .grouping.plus /
        // .grouping.minus buttons inside a .col-group-pane strip; the
        // Lit column header renders one toggle per group carrying its
        // collapsed state as a data attribute.
        selector = selector.replace(".col-group-pane .grouping.plus",
                ".col-grouping-toggle[data-collapsed=\"true\"]");
        selector = selector.replace(".col-group-pane .grouping.minus",
                ".col-grouping-toggle[data-collapsed=\"false\"]");
        selector = selector.replace(".row-group-pane .grouping.plus",
                ".row-grouping-toggle[data-collapsed=\"true\"]");
        selector = selector.replace(".row-group-pane .grouping.minus",
                ".row-grouping-toggle[data-collapsed=\"false\"]");
        // Frozen-pane quadrants. GWT named them by position; the Lit grid
        // wraps each frozen quadrant in its own transform container. The
        // unfrozen quadrant has no wrapper — it is the grid's own cells — so
        // ".bottom-right-pane" has no equivalent and is left alone.
        selector = selector.replace(".top-left-pane",
                ".quadrant-wrapper-corner");
        selector = selector.replace(".top-right-pane", ".quadrant-wrapper-top");
        selector = selector.replace(".bottom-left-pane",
                ".quadrant-wrapper-left");
        // Sheet overlays. GWT rendered a div carrying the anchor cell as
        // .col<N>.row<N> classes; the Lit grid renders an <img> (or a
        // placeholder div for component overlays) carrying the anchor as
        // data attributes, matching the convention cells already use.
        selector = selector.replaceAll(
                "(?:div)?\\.sheet-image\\.col(\\d+)\\.row(\\d+)",
                ".sheet-overlay[data-col=\"$1\"][data-row=\"$2\"]");
        selector = selector.replaceAll("(?:div)?\\.sheet-image",
                ".sheet-overlay-image");
        // GWT nested the image inside its anchor cell, so tests look for
        // ".col<N>.row<N> img". The Lit grid renders overlays as siblings of
        // the cells, addressed by the same anchor.
        selector = selector.replaceAll("\\.col(\\d+)\\.row(\\d+)\\s+img",
                ".sheet-overlay-image[data-col=\"$1\"][data-row=\"$2\"]");
        return selector;
    }

    private WebElement findInLitShadowRoots(String cssSelector) {
        String translated = translateSelector(cssSelector);
        return (WebElement) ((org.openqa.selenium.JavascriptExecutor) getDriver())
                .executeScript("""
                        const host = arguments[0];
                        const selector = arguments[1];
                        function searchShadow(root) {
                            if (!root) return null;
                            const hit = root.querySelector(selector);
                            if (hit) return hit;
                            // Recurse into nested shadow roots of all
                            // descendants.
                            const children = root.querySelectorAll('*');
                            for (const c of children) {
                                if (c.shadowRoot) {
                                    const sub = searchShadow(c.shadowRoot);
                                    if (sub) return sub;
                                }
                            }
                            return null;
                        }
                        return searchShadow(host.shadowRoot);
                        """, $(SpreadsheetElement.class).first(), translated);
    }

    @SuppressWarnings("unchecked")
    private List<WebElement> findAllInLitShadowRoots(String cssSelector) {
        String translated = translateSelector(cssSelector);
        return (List<WebElement>) ((org.openqa.selenium.JavascriptExecutor) getDriver())
                .executeScript("""
                        const host = arguments[0];
                        const selector = arguments[1];
                        const out = [];
                        function searchShadow(root) {
                            if (!root) return;
                            out.push(...root.querySelectorAll(selector));
                            const children = root.querySelectorAll('*');
                            for (const c of children) {
                                if (c.shadowRoot) searchShadow(c.shadowRoot);
                            }
                        }
                        searchShadow(host.shadowRoot);
                        return out;
                        """, $(SpreadsheetElement.class).first(), translated);
    }

    public void selectCell(String address) {
        selectElement(getSpreadsheet().getCellAt(address), false, false);
    }

    public void selectCell(String address, boolean ctrl, boolean shift) {
        selectElement(getSpreadsheet().getCellAt(address), ctrl, shift);
    }

    public void selectRow(int row) {
        selectElement(getSpreadsheet().getRowHeader(row), false, false);
    }

    public void selectRow(int row, boolean ctrl, boolean shift) {
        selectElement(getSpreadsheet().getRowHeader(row), ctrl, shift);
    }

    protected void paste() {
        if (isMac()) {
            new Actions(getDriver()).keyDown(Keys.CONTROL).keyDown(Keys.COMMAND)
                    .sendKeys("v").keyUp(Keys.CONTROL).keyUp(Keys.COMMAND)
                    .build().perform();
        } else {
            new Actions(getDriver()).keyDown(Keys.CONTROL).sendKeys("v")
                    .keyUp(Keys.CONTROL).build().perform();
            getCommandExecutor().waitForVaadin();
        }
    }

    protected void copy() {
        if (isMac()) {
            new Actions(getDriver()).keyDown(Keys.CONTROL).keyDown(Keys.COMMAND)
                    .sendKeys("c").keyUp(Keys.CONTROL).keyUp(Keys.COMMAND)
                    .build().perform();
        } else {
            new Actions(getDriver()).keyDown(Keys.CONTROL).sendKeys("c")
                    .keyUp(Keys.CONTROL).build().perform();
        }
    }

    protected void undo() {
        if (isMac()) {
            new Actions(getDriver()).keyDown(Keys.CONTROL).keyDown(Keys.COMMAND)
                    .sendKeys("z").keyUp(Keys.CONTROL).keyUp(Keys.COMMAND)
                    .build().perform();
        } else {
            new Actions(getDriver()).keyDown(Keys.CONTROL).sendKeys("z")
                    .keyUp(Keys.CONTROL).build().perform();
        }
    }

    protected void redo() {
        if (isMac()) {
            new Actions(getDriver()).keyDown(Keys.CONTROL).keyDown(Keys.COMMAND)
                    .sendKeys("y").keyUp(Keys.CONTROL).keyUp(Keys.COMMAND)
                    .build().perform();
        } else {
            new Actions(getDriver()).keyDown(Keys.CONTROL).sendKeys("y")
                    .keyUp(Keys.CONTROL).build().perform();
        }
    }

    protected boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public void selectColumn(String column) {
        selectElement(
                getSpreadsheet().getColumnHeader(column.charAt(0) - 'A' + 1),
                false, false);
    }

    public void selectColumn(String column, boolean ctrl, boolean shift) {
        selectElement(
                getSpreadsheet().getColumnHeader(column.charAt(0) - 'A' + 1),
                ctrl, shift);
    }

    public void selectRegion(String from, String to) {
        new Actions(getDriver()).clickAndHold(getSpreadsheet().getCellAt(from))
                .moveToElement(getSpreadsheet().getCellAt(to)).release()
                .perform();
        getCommandExecutor().waitForVaadin();
    }

    private void selectElement(WebElement element, boolean ctrl,
            boolean shift) {
        Keys metaKey = isMac() ? Keys.COMMAND : Keys.CONTROL;
        if (ctrl) {
            new Actions(getDriver()).moveToElement(element).keyDown(metaKey)
                    .click().keyUp(metaKey).build().perform();
        } else if (shift) {
            new Actions(getDriver()).moveToElement(element).keyDown(Keys.SHIFT)
                    .click().keyUp(Keys.SHIFT).build().perform();
        } else {
            new Actions(getDriver()).moveToElement(element).click().build()
                    .perform();
        }
    }

    public void addSheet() {
        findElementInShadowRoot(By.className("add-new-tab")).click();
    }

    public void selectSheetAt(int sheetIndex) {
        getSpreadsheet().selectSheetAt(sheetIndex);
    }

    public String getSelectedSheetName() {
        WebElement selectedSheetTab = findElementInShadowRoot(
                By.cssSelector(".sheet-tabsheet-tab.selected-tab"));

        return selectedSheetTab.getText();
    }

    public List<String> getNamedRanges() {
        final List<WebElement> options = findElementInShadowRoot(
                By.className("namedrangebox"))
                .findElements(By.tagName("option"));

        return options.stream().map(WebElement::getText).toList();
    }

    public void selectNamedRange(String name) {
        TestBenchElement select = ((TestBenchElement) findElementInShadowRoot(
                By.className("namedrangebox")));
        select.setProperty("value", name);
        select.dispatchEvent("change");
    }

    public void clickCell(String address) {
        SheetCellElement cellElement = getSpreadsheet().getCellAt(address);
        new Actions(getDriver()).moveToElement(cellElement).click().build()
                .perform();
    }

    public void clickCell(String address, Keys... modifiers) {
        WebElement cell = getSpreadsheet().getCellAt(address);
        Actions actions = new Actions(driver);
        actions.moveToElement(cell, 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click();
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public String getCellContent(String address) {
        return getSpreadsheet().getCellAt(address).getValue();
    }

    public void clickOnFormulaField() {
        getFormulaField().click();
    }

    public void setFormulaFieldValue(String value) {
        WebElement formulaField = getFormulaField();
        formulaField.clear();
        formulaField.sendKeys(value);
    }

    public boolean isCellActiveWithinSelection(String address) {
        SheetCellElement cell = getSpreadsheet().getCellAt(address);
        return cell.isCellSelected()
                && !cell.getClassNames().contains("cell-range");
    }

    public void clickOnColumnHeader(String columnAddress, Keys... modifiers) {
        Point colPoint = AddressUtil.addressToPoint(columnAddress + "1");
        clickOnColumnHeader(colPoint.x, modifiers);
    }

    public void clickOnColumnHeader(int column, Keys... modifiers) {
        Actions actions = new Actions(driver);
        WebElement header = findElementInShadowRoot(
                By.cssSelector(".ch.col" + column));
        actions.moveToElement(header, 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click(
                findElementInShadowRoot(By.cssSelector(".ch.col" + column)));
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public void clickOnRowHeader(int row, Keys... modifiers) {
        Actions actions = new Actions(driver);
        actions.moveToElement(
                findElementInShadowRoot(By.cssSelector(".rh.row" + row)), 1, 1);
        for (Keys modifier : modifiers) {
            actions.keyDown(modifier);
        }
        actions.click(findElementInShadowRoot(By.cssSelector(".rh.row" + row)));
        for (Keys modifier : modifiers) {
            actions.keyUp(modifier);
        }
        actions.build().perform();
    }

    public void setSpreadsheet(SpreadsheetElement spreadsheet) {
        this.spreadsheet = spreadsheet;
        // Slice 178 gave the sheet its initial focus by clicking the first
        // sheet tab, standing in for the legacy `.sheet-tabsheet` click.
        // That is not a focus gesture: it selects a sheet. On a workbook
        // whose active sheet is not the first one it switched sheets behind
        // the test, and even on the first one the server answered with a
        // full `reloadActiveSheetData`, discarding the overlays, comments,
        // hyperlinks and popup buttons the load had just delivered. Focus
        // the scroll container directly instead — that is the element the
        // keyboard handlers live on (tabindex=0, slice 147).
        focusSheet();
    }

    /** Give the sheet keyboard focus without touching the selection. */
    protected void focusSheet() {
        WebElement scrollContainer = findInLitShadowRoots(".sheet-scroll");
        if (scrollContainer != null) {
            ((org.openqa.selenium.JavascriptExecutor) getDriver())
                    .executeScript("arguments[0].focus();", scrollContainer);
        }
    }

    public void setCellValue(String address, String value) {
        getSpreadsheet().getCellAt(address).setValue(value);
    }

    public SheetCellElement getCellAt(int col, int row) {
        return getSpreadsheet().getCellAt(row, col);
    }

    /**
     * Wait for a cell's invalid-formula indicator to appear.
     *
     * Committing a cell value returns as soon as the editor closes; whether the
     * formula was valid is decided on the server and pushed back afterwards, so
     * the marker cannot be there yet.
     *
     * @param cell
     *            the cell address, e.g. {@code "A1"}
     * @return the indicator element
     */
    public WebElement waitForInvalidFormulaIndicator(String cell) {
        waitUntil(d -> !getCellElement(cell)
                .findElements(By.className("invalidformula-indicator"))
                .isEmpty());
        return getCellElement(cell)
                .findElement(By.className("invalidformula-indicator"));
    }

    public WebElement getCellElement(String cell) {
        return findElementInShadowRoot(By.cssSelector(cellToCSS(cell)));
    }

    public String getCellValue(int col, int row) {
        return getCellAt(col, row).getText();
    }

    public String getCellValue(String address) {
        Point point = AddressUtil.addressToPoint(address);
        return getCellValue(point.x, point.y);
    }

    public void deleteCellValue(String cellAddress) {
        clickCell(cellAddress);
        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();
    }

    private void openInlineEditor(String cell) {
        var cellElement = getSpreadsheet().getCellAt(cell);
        new Actions(getDriver()).doubleClick(cellElement).build().perform();
    }

    public WebElement getInlineEditor(String cell) {
        openInlineEditor(cell);
        return findElementInShadowRoot(By.cssSelector("input"));
    }

    public SpreadsheetElement getSpreadsheet() {
        return spreadsheet;
    }

    public void createNewSpreadsheet() {
        WebElement createBtn = $("vaadin-button").id("createNewBtn");
        createBtn.click();

        setSpreadsheet($(SpreadsheetElement.class).first());
    }

    public void loadFile(String fileName) {
        ComboBoxElement testSheetSelect = $(ComboBoxElement.class)
                .id("testSheetSelect");
        testSheetSelect.selectByText(fileName);

        WebElement updateBtn = $("vaadin-button").id("update");
        updateBtn.click();

        setSpreadsheet($(SpreadsheetElement.class).first());
    }

    public void addFreezePane() {
        $("vaadin-button").id("freezePane").click();
        $("vaadin-button").id("submitValues").click();
        waitForDialogToClose();
    }

    public void addFreezePane(int horizontalSplitPosition,
            int verticalSplitPosition) {
        $("vaadin-button").id("freezePane").click();
        $(TextFieldElement.class).id("verticalSplitPosition")
                .setValue(String.valueOf(verticalSplitPosition));
        $(TextFieldElement.class).id("horizontalSplitPosition")
                .setValue(String.valueOf(horizontalSplitPosition));
        $("vaadin-button").id("submitValues").click();
        waitForDialogToClose();
    }

    /**
     * Waits until no dialog is open or still running its closing animation. The
     * freeze pane dialog is modal, so while it is closing its overlay still
     * covers the page and swallows clicks meant for the sheet.
     */
    private void waitForDialogToClose() {
        waitUntil(driver -> !$("vaadin-dialog").withAttribute("opened").exists()
                && !$("vaadin-dialog").withAttribute("closing").exists());
    }

    public void setLocale(Locale locale) {
        ComboBoxElement localeSelect = $(ComboBoxElement.class)
                .id("localeSelect");
        localeSelect.selectByText(locale.getDisplayName(Locale.ENGLISH));
    }

    public void loadTestFixture(TestFixtures fixture) {
        $(ComboBoxElement.class).id("fixtureSelect")
                .selectByText(fixture.toString());
        $("vaadin-button").id("loadFixtureBtn").click();

        // sanity check
        Assert.assertEquals("Fixture not loaded correctly", fixture.toString(),
                $(ComboBoxElement.class).id("fixtureSelect")
                        .getInputElementValue());
    }

    /**
     * State of the input element inside the custom editor rendered in a cell.
     *
     * @param value
     *            the text shown in the input
     * @param selectionStart
     *            start of the text selection
     * @param selectionEnd
     *            end of the text selection
     * @param focused
     *            whether the input holds the focus
     */
    public record EditorInputState(String value, int selectionStart,
            int selectionEnd, boolean focused) {

        /**
         * Returns whether the whole text is selected, which is what
         * {@code inputElement.select()} produces.
         *
         * @return {@code true} if the text is non-empty and fully selected
         */
        public boolean isFullySelected() {
            return !value.isEmpty() && selectionStart == 0
                    && selectionEnd == value.length();
        }
    }

    /**
     * Returns the name of the slot the given cell's editor is assigned to. A
     * cell knows only this name, not the editor itself, so editor lookups start
     * here.
     *
     * @param cellAddress
     *            address of the cell, e.g. "B2"
     * @return the slot name, or {@code null} if the cell renders no editor
     */
    private String getEditorSlotName(String cellAddress) {
        var slots = getSpreadsheet().getCellAt(cellAddress)
                .findElements(By.tagName("slot"));
        return slots.isEmpty() ? null : slots.get(0).getDomAttribute("name");
    }

    /**
     * Returns the custom editor rendered in the given cell, or {@code null} if
     * the cell renders none.
     *
     * @param <T>
     *            element type of the editor
     * @param cellAddress
     *            address of the cell, e.g. "B2"
     * @param editorType
     *            e.g. {@code ComboBoxElement.class}
     * @return the editor element, or {@code null} if there is none
     */
    protected <T extends TestBenchElement> T getCellEditor(String cellAddress,
            Class<T> editorType) {
        String slotName = getEditorSlotName(cellAddress);
        if (slotName == null) {
            return null;
        }
        var editors = getSpreadsheet().$(editorType).attribute("slot", slotName)
                .all();
        return editors.isEmpty() ? null : editors.get(0);
    }

    /**
     * Returns the input state of the custom editor rendered in the given cell,
     * or {@code null} if the cell renders no editor.
     *
     * @param cellAddress
     *            address of the cell, e.g. "B2"
     * @return the editor's input state, or {@code null} if there is none
     */
    @SuppressWarnings("unchecked")
    public EditorInputState getEditorInputState(String cellAddress) {
        String slotName = getEditorSlotName(cellAddress);
        if (slotName == null) {
            return null;
        }
        Object raw = executeScript("""
                const editor = arguments[0].querySelector(
                        '[slot="' + arguments[1] + '"]');
                if (!editor || !editor.inputElement) return null;
                const i = editor.inputElement;
                // document.activeElement stops at a shadow host, so descend.
                let active = document.activeElement;
                while (active && active.shadowRoot
                        && active.shadowRoot.activeElement) {
                  active = active.shadowRoot.activeElement;
                }
                return {
                  value: i.value == null ? '' : i.value,
                  selectionStart: i.selectionStart,
                  selectionEnd: i.selectionEnd,
                  focused: active === i
                };
                """, getSpreadsheet(), slotName);
        if (raw == null) {
            return null;
        }
        Map<String, Object> state = (Map<String, Object>) raw;
        return new EditorInputState(String.valueOf(state.get("value")),
                ((Number) state.get("selectionStart")).intValue(),
                ((Number) state.get("selectionEnd")).intValue(),
                Boolean.TRUE.equals(state.get("focused")));
    }

    public void assertNoErrorIndicatorDetected() {
        Assert.assertTrue("Error indicator detected when there should be none.",
                findElements(By.className("v-errorindicator")).isEmpty());
    }

    protected void assertAddressFieldValue(String expected) {
        try {
            waitUntil(
                    driver -> Objects.equals(expected, getAddressFieldValue()),
                    2);
        } catch (TimeoutException e) {
            var actual = getAddressFieldValue();
            Assert.fail("Expected " + expected + " on addressField, actual:"
                    + actual);
        }
    }

    protected void assertAddressFieldValue(String expected, String actual) {
        Assert.assertEquals(
                "Expected " + expected + " on addressField, actual:" + actual,
                expected, actual);
    }

    protected void assertSelectedCell(String cell) {
        try {
            waitUntil(driver -> isCellSelected(cell), 2);
        } catch (TimeoutException e) {
            Assert.fail("Cell " + cell + " should be the selected cell");
        }
    }

    protected void assertSelectedCell(String cell, boolean selected) {
        Assert.assertTrue("Cell " + cell + " should be the selected cell",
                selected);
    }

    protected void assertNotSelectedCell(String cell) {
        try {
            waitUntil(driver -> !isCellSelected(cell), 2);
        } catch (TimeoutException e) {
            Assert.fail("Cell " + cell + " should not be selected cell");
        }
    }

    protected void assertNotSelectedCell(String cell, boolean selected) {
        Assert.assertFalse("Cell " + cell + " should not be selected cell",
                selected);
    }

    protected void assertInRange(double from, double value, double to) {
        Assert.assertTrue("Value [" + value + "] is not in range: [" + from
                + " - " + to + "]", value >= from && value <= to);
    }

    public String getAddressFieldValue() {
        return getAddressField().getDomProperty("value");
    }

    public void setAddressFieldValue(String address) {
        WebElement addressField = getAddressField();
        addressField.clear();
        addressField.sendKeys(address + Keys.ENTER);
    }

    private WebElement getAddressField() {
        return findElementInShadowRoot(By.cssSelector("input.addressfield"));
    }

    private WebElement getFormulaField() {
        return findElementInShadowRoot(By.className("functionfield"));
    }

    public String getFormulaFieldValue() {
        return getFormulaField().getDomProperty("value");
    }

    public String getSelectionFormula() {
        final var sprElement = getSpreadsheet();

        TestBenchElement selection = sprElement.$("*")
                .withClassName("sheet-selection").first();
        final Set<String> classes = selection.getClassNames();

        int startRow = -1;
        int startColumn = -1;

        for (String c : classes) {
            if (c.startsWith("row")) {
                startRow = Integer.parseInt(c.substring(3));
            }
            if (c.startsWith("col")) {
                startColumn = Integer.parseInt(c.substring(3));
            }
        }

        int endRow = startRow + 1;
        while (sprElement.getCellAt(endRow, startColumn).isCellSelected()) {
            endRow++;
        }
        endRow--;

        int endColumn = startColumn;
        while (sprElement.getCellAt(startRow, endColumn).isCellSelected()) {
            endColumn++;
        }
        endColumn--;

        return new CellRangeAddress(startRow - 1, endRow - 1, startColumn - 1,
                endColumn - 1).formatAsString();
    }

    public void insertColumn(String[] values) {
        for (String value : values) {
            insertAndRet(value);
        }
    }

    private void insertAndRet(CharSequence k) {
        action(k);
        action(Keys.RETURN);
        action(Keys.ENTER);
    }

    public void action(CharSequence k) {
        new Actions(getDriver()).sendKeys(k).build().perform();
    }

    public boolean isCellSelected(String address) {
        return getSpreadsheet().getCellAt(address).isCellSelected();
    }

    public boolean isCellSelected(int col, int row) {
        return getSpreadsheet().getCellAt(row, col).isCellSelected();
    }

    public String getCellStyle(String cell, String propertyName) {
        return getSpreadsheet().getCellAt(cell).getCssValue(propertyName);
    }

    public String getCellColor(String address) {
        return getCellStyle(address, BACKGROUND_COLOR);
    }

    public void contextClickOnRowHeader(int i) {
        getSpreadsheet().getRowHeader(i).contextClick();
    }

    public void contextClickOnColumnHeader(char column) {
        getSpreadsheet().getColumnHeader(column - 'A' + 1).contextClick();
    }

    public void unhideRow(int i) {
        contextClickOnRowHeader(i + 1);
        clickItem("Unhide row " + i);
    }

    public void unhideColumn(char c) {
        contextClickOnColumnHeader((char) (c + 1));
        clickItem("Unhide column " + c);
    }

    private int[] numericCoordinates(String cell) {
        String alpha = "A";
        String number = "1";
        for (int i = 0; i < cell.length(); i++) {
            if (cell.charAt(i) < 65) {
                alpha = cell.substring(0, i);
                number = cell.substring(i);
                break;
            }
        }

        int col = 0;
        for (int i = 0; i < alpha.length(); i++) {
            char h = alpha.charAt(i);
            col = (h - 'A' + 1) + (col * 26);
        }
        int row = Integer.parseInt(number);

        int[] coordinates = new int[] { col, row };
        return coordinates;
    }

    public String cellToCSS(String cell) {
        int[] coordinates = numericCoordinates(cell);
        return ".col" + coordinates[0] + ".row" + coordinates[1];
    }

    public void dragFromCellToCell(String from, String to) {
        WebElement fromCell = getSpreadsheet().getCellAt(from);
        WebElement toCell = getSpreadsheet().getCellAt(to);

        new Actions(driver).dragAndDrop(fromCell, toCell).build().perform();
    }

    public String getMergedCellContent(String topLeftCell) {
        return findElementInShadowRoot(mergedCell(topLeftCell)).getText();
    }

    public By mergedCell(String topLeftCell) {
        int[] coordinates = numericCoordinates(topLeftCell);
        return By.cssSelector("div.col" + coordinates[0] + ".row"
                + coordinates[1] + ".merged-cell");
    }

    public void navigateToCell(String cell) {
        findElementInShadowRoot(By.cssSelector(".addressfield")).clear();
        findElementInShadowRoot(By.cssSelector(".addressfield")).sendKeys(cell);
        new Actions(getDriver()).sendKeys(Keys.RETURN).perform();
    }

    /**
     * Suppresses the overlay that appears when hovering over a cell with an
     * invalid formula. The overlay can appear when using setCellValue, and can
     * cause subsequent button clicks to fail. Can be used in tests that enter
     * invalid formulas in cells to test the invalid formular indicator (though
     * obviously not in tests that check the overlay itself).
     */
    public void suppressInvalidFormulaCommentOverlay() {
        executeScript("""
                arguments[0].addEventListener("mouseover", e => {
                  e.stopPropagation();
                }, true);
                """, getSpreadsheetInShadowRoot());
    }

    // Context menu helpers

    /**
     * Find a context-menu item by its caption. The menu lives in the
     * spreadsheet's nested shadow roots, and there is no TestBench query for
     * "element whose text is X", so this walks the roots in one script rather
     * than paying a round-trip per candidate.
     */
    private WebElement findMenuItem(String caption) {
        return (WebElement) ((org.openqa.selenium.JavascriptExecutor) getDriver())
                .executeScript("""
                        const host = arguments[0];
                        const caption = arguments[1];
                        function search(root) {
                            if (!root) return null;
                            for (const item of root.querySelectorAll('.item')) {
                                if (item.textContent.trim() === caption) {
                                    return item;
                                }
                            }
                            for (const c of root.querySelectorAll('*')) {
                                if (c.shadowRoot) {
                                    const sub = search(c.shadowRoot);
                                    if (sub) return sub;
                                }
                            }
                            return null;
                        }
                        return search(host.shadowRoot);
                        """, $(SpreadsheetElement.class).first(), caption);
    }

    public void clickItem(String caption) {
        WebElement item = findMenuItem(caption);
        if (item == null) {
            throw new RuntimeException("Menu item '" + caption + "' not found");
        }
        new Actions(getDriver()).click(item).perform();
    }

    public boolean hasOption(String caption) {
        return findMenuItem(caption) != null;
    }

    public List<WebElement> getGroupings() {
        return findElementsInShadowRoot(
                By.cssSelector(".col-group-pane .grouping.plus"));
    }

    protected double getSize(String size) {
        return Double.parseDouble(size.replaceAll("[^.0-9]", ""));
    }

    private static final String WEB_SOCKET_CONNECTION_ERROR_PREFIX = "WebSocket connection to ";

    // Helper adopted from TestBenchHelpers
    protected void checkLogsForErrors(
            Predicate<String> acceptableMessagePredicate) {
        getLogEntries(Level.WARNING).forEach(logEntry -> {
            if ((Objects.equals(logEntry.getLevel(), Level.SEVERE)
                    || logEntry.getMessage().contains(" 404 "))
                    && !logEntry.getMessage()
                            .contains(WEB_SOCKET_CONNECTION_ERROR_PREFIX)
                    && !acceptableMessagePredicate
                            .test(logEntry.getMessage())) {
                throw new AssertionError(String.format(
                        "Received error message in browser log console right after opening the page, message: %s",
                        logEntry));
            } else {
                LoggerFactory.getLogger(AbstractSpreadsheetIT.class.getName())
                        .warn("This message in browser log console may be a potential error: '{}'",
                                logEntry);
            }
        });
    }

    protected void checkLogsForErrors() {
        checkLogsForErrors(msg -> false);
    }

    protected List<LogEntry> getLogEntries(Level level) {
        // https://github.com/vaadin/testbench/issues/1233
        getCommandExecutor().waitForVaadin();

        return driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                .filter(logEntry -> logEntry.getLevel().intValue() >= level
                        .intValue())
                // we always have this error
                .filter(logEntry -> !logEntry.getMessage()
                        .contains("favicon.ico"))
                .toList();
    }
}
