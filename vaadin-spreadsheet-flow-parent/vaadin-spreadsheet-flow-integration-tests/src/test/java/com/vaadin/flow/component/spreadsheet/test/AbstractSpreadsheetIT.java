package com.vaadin.flow.component.spreadsheet.test;

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
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class AbstractSpreadsheetIT extends AbstractComponentIT {

    // Should be COMMAND for macOS
    private Keys metaKey = Keys.CONTROL;
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

    protected TestBenchElement findElementInShadowRoot(By by) {
        return getSpreadsheetInShadowRoot().findElement(by);
    }

    protected List<WebElement> findElementsInShadowRoot(By by) {
        return getSpreadsheetInShadowRoot().findElements(by);
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
        if (ctrl) {
            new Actions(getDriver()).moveToElement(element).keyDown(metaKey)
                    .click().keyUp(metaKey).build().perform();
        } else if (shift) {
            new Actions(getDriver()).moveToElement(element).keyDown(Keys.SHIFT)
                    .click().keyUp(Keys.SHIFT).build().perform();
        } else if (ctrl && shift) {
            new Actions(getDriver()).moveToElement(element).keyDown(Keys.SHIFT)
                    .keyDown(metaKey).click().keyUp(Keys.SHIFT).keyUp(metaKey)
                    .build().perform();
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

        return options.stream().map(WebElement::getText)
                .collect(Collectors.toList());
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
                && !cell.getAttribute("class").contains("cell-range");
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
        // Force sheet initial focus
        findElementInShadowRoot(By.className("sheet-tabsheet")).click();
    }

    public void setCellValue(String address, String value) {
        getSpreadsheet().getCellAt(address).setValue(value);
    }

    public SheetCellElement getCellAt(int col, int row) {
        return getSpreadsheet().getCellAt(row, col);
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
    }

    public void addFreezePane(int horizontalSplitPosition,
            int verticalSplitPosition) {
        $("vaadin-button").id("freezePane").click();
        $(TextFieldElement.class).id("verticalSplitPosition")
                .setValue(String.valueOf(verticalSplitPosition));
        $(TextFieldElement.class).id("horizontalSplitPosition")
                .setValue(String.valueOf(horizontalSplitPosition));
        $("vaadin-button").id("submitValues").click();
    }

    public void setLocale(Locale locale) {
        ComboBoxElement localeSelect = $(ComboBoxElement.class)
                .id("localeSelect");
        localeSelect.selectByText(locale.getDisplayName());
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

    public void assertNoErrorIndicatorDetected() {
        Assert.assertTrue("Error indicator detected when there should be none.",
                findElements(By.className("v-errorindicator")).isEmpty());
    }

    protected void assertAddressFieldValue(String expected, String actual) {
        Assert.assertEquals(
                "Expected " + expected + " on addressField, actual:" + actual,
                expected, actual);
    }

    protected void assertSelectedCell(String cell) {
        assertSelectedCell(cell, isCellSelected(cell));
    }

    protected void assertSelectedCell(String cell, boolean selected) {
        Assert.assertTrue("Cell " + cell + " should be the selected cell",
                selected);
    }

    protected void assertNotSelectedCell(String cell) {
        assertNotSelectedCell(cell, isCellSelected(cell));
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
        return getAddressField().getAttribute("value");
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
        return getFormulaField().getAttribute("value");
    }

    public String getSelectionFormula() {
        final var sprElement = getSpreadsheet();

        WebElement selection = findElementInShadowRoot(
                org.openqa.selenium.By.className("sheet-selection"));
        final String[] classes = selection.getAttribute("class").split(" ");

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

    // Context menu helpers

    public void clickItem(String caption) {
        try {
            new Actions(getDriver()).click(getDriver().findElement(
                    By.xpath("//div[@class='popupContent']//*[text()='"
                            + caption + "']")))
                    .perform();
        } catch (NoSuchElementException ex) {
            throw new RuntimeException("Menu item '" + caption + "' not found",
                    ex);
        }
    }

    public boolean hasOption(String caption) {
        return getDriver().findElements(By.xpath(
                "//div[@class='popupContent']//*[text()='" + caption + "']"))
                .size() != 0;
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
                .collect(Collectors.toList());
    }
}
