package com.vaadin.flow.component.spreadsheet.testbench;

import java.util.List;

import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.testbench.TestBenchElement;

/**
 * This is the base element class for accessing a Vaadin Spreadsheet component
 * for TestBench testing.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-spreadsheet")
public class SpreadsheetElement extends TestBenchElement {

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
        String cellSelector = String.format(".col%d.row%d.cell", column, row);
        // If there are multiple cells return the merged cell
        if (findElements(By.cssSelector(cellSelector)).size() > 1) {
            cellSelector += ".merged-cell";
        }
        TestBenchElement cell = (TestBenchElement) findElement(
                By.cssSelector(cellSelector));
        SheetCellElement cellElement = cell.wrap(SheetCellElement.class);
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
        TestBenchElement cell = (TestBenchElement) findElement(
                By.cssSelector(String.format(".rh.row%d", rowIndex)));
        return cell.wrap(SheetHeaderElement.class);
    }

    /**
     * Gets the column header element at the given index.
     *
     * @param columnIndex
     *            Index of target column, 1-based
     * @return Header of the column at the given index
     */
    public SheetHeaderElement getColumnHeader(int columnIndex) {
        TestBenchElement cell = (TestBenchElement) findElement(
                By.cssSelector(String.format(".ch.col%d", columnIndex)));
        return cell.wrap(SheetHeaderElement.class);
    }

    /**
     * Gets the address field. The address field contains the address of the
     * cell that was last clicked, or A1 if no clicks have yet been made.
     *
     * @return Address field element
     */
    public TestBenchElement getAddressField() {
        return (TestBenchElement) findElement(By.className("addressfield"));
    }

    /**
     * Gets the formula field. This field is where the user can input data for
     * the cell whose address the address field currently contains.
     *
     * @return Formula field element
     */
    public TestBenchElement getFormulaField() {
        return (TestBenchElement) findElement(By.className("functionfield"));
    }

    /**
     * Gets the info label. Info label is the small text at the bottom right
     * corner of the Spreadsheet.
     *
     * @return Info label element
     */
    public TestBenchElement getInfoLabel() {
        return (TestBenchElement) findElement(
                By.className("sheet-tabsheet-infolabel"));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.testbench.TestBenchElement#scroll(int)
     */
    @Override
    public void scroll(int scrollTop) {
        getBottomRightPane().scroll(scrollTop);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.testbench.TestBenchElement#scrollLeft(int)
     */
    @Override
    public void scrollLeft(int scrollLeft) {
        getBottomRightPane().scrollLeft(scrollLeft);
    }

    /**
     * Scrolls the sheet selector to the beginning.
     *
     * Has no effect if there are not enough sheets to require scrolling.
     */
    public void scrollSheetsToStart() {
        findElement(By.className("scroll-tabs-beginning")).click();
    }

    /**
     * Scrolls the sheet selector to the end.
     *
     * Has no effect if there are not enough sheets to require scrolling.
     */
    public void scrollSheetsToEnd() {
        findElement(By.className("scroll-tabs-end")).click();
    }

    /**
     * Scrolls the sheet selector left or right by the given amount.
     *
     * Has no effect if there are not enough sheets to require scrolling.
     *
     * @param amount
     *            Amount to scroll. Positive numbers scroll to the right and
     *            negative numbers scroll to the left.
     */
    public void scrollSheets(int amount) {
        WebElement target = findElement(By.className(
                amount > 0 ? "scroll-tabs-right" : "scroll-tabs-left"));
        for (int i = 0; i < amount; i++) {
            target.click();
        }
    }

    /**
     * Selects the sheet at the given index. Indexes are counted only for
     * visible sheets.
     *
     * @param sheetIndex
     *            Index of sheet to select, 0-based
     */
    public void selectSheetAt(int sheetIndex) {
        WebElement tabContainer = findElement(
                By.className("sheet-tabsheet-container"));
        List<WebElement> tabs = tabContainer.findElements(By.xpath(".//*"));
        WebElement target = tabs.get(sheetIndex);
        scrollSheetVisible(target);
        target.click();
    }

    /**
     * Selects the sheet with the given name. Only visible sheets can be
     * selected.
     *
     * @param sheetName
     *            Name of sheet to select
     */
    public void selectSheet(String sheetName) {
        WebElement tabContainer = findElement(
                By.className("sheet-tabsheet-container"));
        List<WebElement> tabs = tabContainer.findElements(By.xpath(".//*"));
        for (WebElement tab : tabs) {
            if (tab.getText().equals(sheetName)) {
                scrollSheetVisible(tab);
                tab.click();
                break;
            }
        }
    }

    /**
     * Adds a new sheet.
     */
    public void addSheet() {
        findElement(By.className("add-new-tab")).click();
    }

    /**
     * Fetches the context menu for the spreadsheet
     *
     * @return {@link SpreadsheetElement.ContextMenuElement}
     * @throws java.util.NoSuchElementException
     *             if the menu isn't open
     */
    public ContextMenuElement getContextMenu() {
        try {
            WebElement cm = getDriver()
                    .findElement(By.className("v-contextmenu"));
            return wrapElement(cm, getCommandExecutor())
                    .wrap(ContextMenuElement.class);
        } catch (WebDriverException e) {
            throw new NoSuchElementException("Context menu not found", e);
        }
    }

    public static class ContextMenuElement extends TestBenchElement {

        public WebElement getItem(String text) {
            return findElement(
                    By.xpath(".//table//tr[*]//td//div[contains(text(), \""
                            + text + "\")]"));
        }

    }

    private void scrollSheetVisible(WebElement targetSheet) {
        // Make sure the target sheet is visible
        if (!targetSheet.isDisplayed()) {
            scrollSheetsToStart();
            while (!targetSheet.isDisplayed()) {
                scrollSheets(1);
            }
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
        location.x += element.getSize().getWidth() / 2;
        location.y += element.getSize().getHeight() / 2;
        return isInSelection(location) || isNonCoherentlySelected(element);
    }

    private void findSelectionOutline() {
        // sometimes the spreadsheet takes so long to load that the selection
        // widget elements are not found
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions
                .presenceOfElementLocated(By.className("s-top")));
        sTop = findElement(By.className("s-top"));
        sBottom = findElement(By.className("s-bottom"));
        // Just to make sure the left element is present
        findElement(By.className("s-left"));
        sRight = findElement(By.className("s-right"));
    }

    private boolean isNonCoherentlySelected(WebElement element) {
        // an element is non-coherently selected if the class attribute
        // contains "cell-range"
        return element.getAttribute("class").contains("cell-range")
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

    private boolean isInSelection(Point location) {
        // Test top left corner
        if (location.getX() < sLocation.getX()
                || location.getY() < sLocation.getY()) {
            return false;
        }
        // Test lower right corner
        if (location.getX() - sLocation.getX() > sSize.getWidth()
                || location.getY() - sLocation.getY() > sSize.getHeight()) {
            return false;
        }
        // Everything is inside the selection
        return true;
    }

    public WebElement getCellValueInput() {
        return findElement(By.id("cellinput"));
    }

    /**
     * Determine if the pop-up of PopupuButton is currently visible
     */
    public boolean isPopupButtonPopupVisible() {
        List<WebElement> elements = getDriver().findElements(
                By.className("v-spreadsheet-popupbutton-overlay"));
        return !elements.isEmpty();
    }

    private TestBenchElement getBottomRightPane() {
        return wrapElement(findElement(By.className("bottom-right-pane")),
                getCommandExecutor());
    }
}
