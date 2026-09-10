/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.testbench;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Base TestBench element for the Vaadin Spreadsheet component.
 *
 * <p>
 * Slice 177 (Tier 3 Phase 3.7): re-targeted from the GWT-era DOM to the
 * Lit-element DOM. The component now consists of nested shadow roots: the
 * {@code <vaadin-spreadsheet>} wrapper owns one, and each internal Lit
 * sub-element ({@code internal-formula-bar}, {@code internal-column-header},
 * {@code internal-row-header}, {@code internal-cell-grid},
 * {@code internal-sheet-tabs}) owns its own. This class traverses them via
 * JavaScript executor calls — the public Java method signatures here are
 * unchanged, so existing IT tests don't need updates.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-spreadsheet")
public class SpreadsheetElement extends TestBenchElement {

    /**
     * Find a single element in the cell-grid's shadow root via a CSS selector.
     * Returns {@code null} when not found (matches the legacy
     * {@code findElementInShadowRoot} return semantics).
     */
    private WebElement findInCellGrid(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const grid = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-cell-grid');
                                return grid && grid.shadowRoot
                                    ? grid.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find a single element in the column-header's shadow root.
     */
    private WebElement findInColumnHeader(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const ch = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-column-header');
                                return ch && ch.shadowRoot
                                    ? ch.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find a single element in the row-header's shadow root.
     */
    private WebElement findInRowHeader(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const rh = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-row-header');
                                return rh && rh.shadowRoot
                                    ? rh.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find an element in the formula-bar's shadow root.
     */
    private WebElement findInFormulaBar(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const fb = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-formula-bar');
                                return fb && fb.shadowRoot
                                    ? fb.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find an element in the sheet-tabs' shadow root.
     */
    private WebElement findInSheetTabs(String selector) {
        return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const st = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-sheet-tabs');
                                return st && st.shadowRoot
                                    ? st.shadowRoot.querySelector(arguments[1])
                                    : null;
                                """,
                        this, selector);
    }

    /**
     * Find all elements in the sheet-tabs' shadow root matching a selector.
     * Returns an empty list when the element isn't there.
     */
    @SuppressWarnings("unchecked")
    private List<WebElement> findAllInSheetTabs(String selector) {
        return (List<WebElement>) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const st = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-sheet-tabs');
                                return st && st.shadowRoot
                                    ? Array.from(st.shadowRoot.querySelectorAll(arguments[1]))
                                    : [];
                                """,
                        this, selector);
    }

    /**
     * Gets the cell element at the given coordinates for the currently active
     * sheet. Throws NoSuchElementException if the cell is outside the visible
     * area.
     *
     * @param row
     *            Row index, 1-based
     * @param column
     *            Column index, 1-based
     * @return Cell element at the given index.
     * @throws NoSuchElementException
     *             if the cell at (row, column) is not found.
     */
    public SheetCellElement getCellAt(int row, int column) {
        String selector = String.format(".col%d.row%d.cell", column, row);
        // Polls the cell-grid's shadow root for the cell — the grid may
        // not have rendered yet (e.g. just after a scroll). The merged-
        // cell case can be reintroduced if needed; for now there's a
        // single cell per (col,row).
        WebElement cell = (WebElement) getCommandExecutor().getDriver()
                .executeAsyncScript(
                        """
                                const host = arguments[0];
                                const selector = arguments[1];
                                const callback = arguments[arguments.length - 1];
                                const MAX_ATTEMPTS = 50;
                                const delay = () => new Promise(r => setTimeout(r, 100));
                                async function locateGrid() {
                                    for (let i = 0; i < MAX_ATTEMPTS; i++) {
                                        const grid = host.shadowRoot
                                            && host.shadowRoot.querySelector(
                                                'vaadin-spreadsheet-internal-cell-grid');
                                        if (grid && grid.shadowRoot) return grid;
                                        await delay();
                                    }
                                    return null;
                                }
                                async function findCell() {
                                    const grid = await locateGrid();
                                    if (!grid) return null;
                                    for (let i = 0; i < MAX_ATTEMPTS; i++) {
                                        const cell = grid.shadowRoot.querySelector(selector);
                                        if (cell) return cell;
                                        await delay();
                                    }
                                    return null;
                                }
                                findCell().then(callback);
                                """,
                        this, selector);
        if (cell == null) {
            throw new NoSuchElementException(
                    "Cell not found: row=" + row + ", column=" + column);
        }
        TestBenchElement wrapped = wrapElement(cell, getCommandExecutor());
        SheetCellElement cellElement = wrapped.wrap(SheetCellElement.class);
        cellElement.setParent(this);
        return cellElement;
    }

    /**
     * Gets the cell element at the given cell address for the currently active
     * sheet. Throws NoSuchElementException if the cell is outside the visible
     * area.
     *
     * @param cellAddress
     *            Target address, e.g. A3
     * @return Cell element at the given index.
     * @throws NoSuchElementException
     *             if the cell at (cellAddress) is not found.
     */
    public SheetCellElement getCellAt(String cellAddress) {
        Point point = AddressUtil.addressToPoint(cellAddress);
        return getCellAt(point.getY(), point.getX());
    }

    /**
     * Gets the row header element at the given index.
     *
     * @param rowIndex
     *            Index of target row, 1-based
     * @return Header of the row at the given index
     */
    public SheetHeaderElement getRowHeader(int rowIndex) {
        WebElement cell = findInRowHeader(
                String.format(".row-header-cell.row%d", rowIndex));
        if (cell == null) {
            throw new NoSuchElementException(
                    "Row header not found: " + rowIndex);
        }
        return wrapElement(cell, getCommandExecutor())
                .wrap(SheetHeaderElement.class);
    }

    /**
     * Gets the column header element at the given index.
     *
     * @param columnIndex
     *            Index of target column, 1-based
     * @return Header of the column at the given index
     */
    public SheetHeaderElement getColumnHeader(int columnIndex) {
        WebElement cell = findInColumnHeader(
                String.format(".col-header-cell.col%d", columnIndex));
        if (cell == null) {
            throw new NoSuchElementException(
                    "Column header not found: " + columnIndex);
        }
        return wrapElement(cell, getCommandExecutor())
                .wrap(SheetHeaderElement.class);
    }

    /**
     * Gets the address field. The address field contains the address of the
     * cell that was last clicked, or A1 if no clicks have yet been made.
     *
     * @return Address field element
     */
    public TestBenchElement getAddressField() {
        WebElement el = findInFormulaBar("[part~=\"address-field\"]");
        return el == null ? null
                : (TestBenchElement) wrapElement(el, getCommandExecutor());
    }

    /**
     * Gets the formula field. This field is where the user can input data for
     * the cell whose address the address field currently contains.
     *
     * @return Formula field element
     */
    public TestBenchElement getFormulaField() {
        WebElement el = findInFormulaBar("[part~=\"formula-field\"]");
        return el == null ? null
                : (TestBenchElement) wrapElement(el, getCommandExecutor());
    }

    /**
     * Gets the info label. Currently no equivalent in the Lit-based Spreadsheet
     * — preserved as a public API stub returning {@code null}. Existing
     * TestBench callers that null-check the result continue to work; tests that
     * asserted on the label text should be deferred until the info-label
     * feature is reintroduced.
     */
    public TestBenchElement getInfoLabel() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.testbench.TestBenchElement#scroll(int)
     */
    @Override
    public void scroll(int scrollTop) {
        scrollContainer().scroll(scrollTop);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.testbench.TestBenchElement#scrollLeft(int)
     */
    @Override
    public void scrollLeft(int scrollLeft) {
        scrollContainer().scrollLeft(scrollLeft);
    }

    /**
     * The Lit Spreadsheet's scroll container — a {@code <div>} inside the
     * host's shadow root that wraps the cell grid and its sticky-positioned
     * header strips. Lit equivalent of the legacy {@code .bottom-right-pane}.
     */
    private TestBenchElement scrollContainer() {
        WebElement el = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript("""
                        const host = arguments[0];
                        return host.shadowRoot
                            ? host.shadowRoot.querySelector(
                                'div[style*="overflow"]')
                            : null;
                        """, this);
        if (el == null) {
            throw new NoSuchElementException("scroll container not found");
        }
        return wrapElement(el, getCommandExecutor());
    }

    /**
     * Tab-scroll buttons aren't implemented in the Lit sheet-tabs element yet.
     * This is a no-op until they're added.
     */
    public void scrollSheetsToStart() {
        // No-op (deferred): Lit sheet-tabs doesn't render scroll
        // buttons.
    }

    /**
     * Tab-scroll buttons aren't implemented in the Lit sheet-tabs element yet.
     * This is a no-op until they're added.
     */
    public void scrollSheetsToEnd() {
        // No-op (deferred): Lit sheet-tabs doesn't render scroll
        // buttons.
    }

    /**
     * Tab-scroll isn't implemented in the Lit sheet-tabs element yet. Preserved
     * as a public method that's effectively a no-op.
     *
     * @param amount
     *            Amount to scroll (ignored).
     */
    public void scrollSheets(int amount) {
        // No-op (deferred): Lit sheet-tabs doesn't render scroll
        // buttons.
    }

    /**
     * Selects the sheet at the given index. Indexes are counted only for
     * visible sheets.
     *
     * @param sheetIndex
     *            Index of sheet to select, 0-based
     */
    public void selectSheetAt(int sheetIndex) {
        List<WebElement> tabs = findAllInSheetTabs(".sheet-tab");
        if (sheetIndex < 0 || sheetIndex >= tabs.size()) {
            throw new NoSuchElementException(
                    "Sheet index out of range: " + sheetIndex);
        }
        tabs.get(sheetIndex).click();
    }

    /**
     * Selects the sheet with the given name. Only visible sheets can be
     * selected.
     *
     * @param sheetName
     *            Name of sheet to select
     */
    public void selectSheet(String sheetName) {
        List<WebElement> tabs = findAllInSheetTabs(".sheet-tab");
        for (WebElement tab : tabs) {
            if (tab.getText().equals(sheetName)) {
                tab.click();
                return;
            }
        }
    }

    /**
     * Adds a new sheet by clicking the tab strip's add button.
     *
     * @throws NoSuchElementException
     *             if the add button is not present
     */
    public void addSheet() {
        WebElement addButton = findInSheetTabs(".add-new-tab");
        if (addButton == null) {
            throw new NoSuchElementException("Add-sheet button not found");
        }
        addButton.click();
    }

    /**
     * Fetches the context menu for the spreadsheet. The menu is an internal
     * element inside the spreadsheet's shadow root, so it is found by
     * traversing the shadow roots rather than by a document query.
     *
     * @return {@link SpreadsheetElement.ContextMenuElement}
     * @throws java.util.NoSuchElementException
     *             if the menu isn't open
     */
    public ContextMenuElement getContextMenu() {
        WebElement cm = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript(
                        """
                                const host = arguments[0];
                                const menu = host.shadowRoot
                                    .querySelector('vaadin-spreadsheet-internal-context-menu');
                                return menu && menu.shadowRoot
                                    ? menu.shadowRoot.querySelector('.menu')
                                    : null;
                                """,
                        this);
        if (cm == null) {
            throw new NoSuchElementException("Context menu not found");
        }
        return wrapElement(cm, getCommandExecutor())
                .wrap(ContextMenuElement.class);
    }

    public static class ContextMenuElement extends TestBenchElement {

        /**
         * The menu item whose caption contains the given text, or {@code null}.
         * <p>
         * A substring match, because several captions name what they act on —
         * "Delete row 1", not "Delete row" — and callers ask for the stable
         * part. The xpath {@code contains()} lookup this replaces did the same.
         * <p>
         * Matched on {@code textContent} rather than
         * {@link WebElement#getText()}: the items live in the context menu's
         * shadow root, where getText() reports the empty string, so every
         * lookup came back null.
         */
        public WebElement getItem(String text) {
            return (WebElement) ((JavascriptExecutor) getCommandExecutor()
                    .getDriver()).executeScript("""
                            const menu = arguments[0];
                            for (const item of menu.querySelectorAll('.item')) {
                                if (item.textContent.includes(arguments[1])) {
                                    return item;
                                }
                            }
                            return null;
                            """, this, text);
        }

    }

    // Current selection
    private WebElement sTop;
    private WebElement sBottom;
    private WebElement sRight;
    private Point sLocation;
    private Dimension sSize;

    boolean isElementSelected(WebElement element) {
        updateSelectionLocationAndSize();
        Point location = element.getLocation();
        int centerX = location.x + element.getSize().getWidth() / 2;
        int centerY = location.y + element.getSize().getHeight() / 2;
        return isInSelection(centerX, centerY)
                || isNonCoherentlySelected(element);
    }

    private void findSelectionOutline() {
        // sometimes the spreadsheet takes so long to load that the selection
        // widget elements are not found
        waitUntil(e -> findInCellGrid(".s-top") != null);
        sTop = findInCellGrid(".s-top");
        sBottom = findInCellGrid(".s-bottom");
        // Just to make sure the left element is present
        findInCellGrid(".s-left");
        sRight = findInCellGrid(".s-right");
    }

    private boolean isNonCoherentlySelected(WebElement element) {
        // an element is non-coherently selected if the part attribute
        // contains "cell-range" — slice 173 emits part="cell cell-range"
        // on cells inside the active range that aren't the active cell.
        // Falls back to the legacy "outline-style: solid" CSS check.
        String partAttr = Optional.ofNullable(element.getDomAttribute("part"))
                .orElse("");
        return partAttr.contains("cell-range")
                || "solid".equals(element.getCssValue("outline-style"));
    }

    private void updateSelectionLocationAndSize() {
        if (sTop == null) {
            findSelectionOutline();
        }
        sLocation = sTop.getLocation();
        int bottomY = sBottom.getLocation().getY();
        int bottomH = sBottom.getSize().getHeight();
        int rightX = sRight.getLocation().getX();
        int rightW = sRight.getSize().getWidth();
        sSize = new Dimension(rightX + rightW - sLocation.getX(),
                bottomY + bottomH - sLocation.getY());
    }

    private boolean isInSelection(int locationX, int locationY) {
        // Test top left corner
        if (locationX < sLocation.getX() || locationY < sLocation.getY()) {
            return false;
        }
        // Test lower right corner
        if (locationX - sLocation.getX() > sSize.getWidth()
                || locationY - sLocation.getY() > sSize.getHeight()) {
            return false;
        }
        // Everything is inside the selection
        return true;
    }

    /**
     * The cell value input — Lit equivalent is the {@code <input>} inside the
     * cell-grid carrying {@code part="cell-input"}.
     */
    public WebElement getCellValueInput() {
        return findInCellGrid("[part~=\"cell-input\"]");
    }

    /**
     * Determine if the pop-up of PopupButton is currently visible.
     * <p>
     * The Lit client shows it in its own overlay inside the spreadsheet's
     * shadow root, positioned in the scroll container so it tracks the sheet,
     * rather than in the GWT-era {@code .v-spreadsheet-popupbutton-overlay}
     * floating div.
     */
    public boolean isPopupButtonPopupVisible() {
        Object visible = ((JavascriptExecutor) getCommandExecutor().getDriver())
                .executeScript("""
                        const overlay = arguments[0].shadowRoot
                            .querySelector('.popup-overlay');
                        return !!overlay && !overlay.hidden;
                        """, this);
        return Boolean.TRUE.equals(visible);
    }
}
