package com.vaadin.flow.component.spreadsheet.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * This class represents one cell within the currently active sheet of a
 * Spreadsheet.
 *
 * @author Vaadin Ltd.
 */
public class SheetCellElement extends TestBenchElement {

    private SpreadsheetElement parent;

    /**
     * Sets the given value to this cell. This method does not support setting
     * values to cells containing custom editors. To set values to custom
     * editors, you should directly set the value to the custom editor which is
     * a child of this element.
     *
     * Note: Calling this method will set the current selection to this cell.
     *
     * @param newValue
     *            Value to set.
     */
    public void setValue(String newValue) {
        if (isNormalCell()) {
            waitUntil(driver -> {
                doubleClick();
                return parent.getCellValueInput().isDisplayed();
            });
            WebElement cellValueInput = parent.getCellValueInput();
            executeScript("arguments[0].value=''",
                    ((TestBenchElement) cellValueInput).getWrappedElement());
            cellValueInput.sendKeys(newValue);
            cellValueInput.sendKeys(Keys.TAB);
            getCommandExecutor().waitForVaadin();
        }
    }

    /**
     * Gets the current value of this cell as a string.
     *
     * @return Current value
     */
    public String getValue() {
        return getText();
    }

    /**
     * Finds out if this cell is selected.
     *
     * @return True if cell selected, false otherwise.
     */
    public boolean isCellSelected() {
        return parent.isElementSelected(this);
    }

    /**
     * Determines if this cell is normal in the sense that it uses the default
     * in-line editor for editing the cell value.
     *
     * @return true if this cell uses the normal in-line editor, false if this
     *         cell has e.g. a custom editor component.
     */
    public boolean isNormalCell() {
        List<WebElement> children = findElements(By.xpath(".//*"));
        // might have an inner div for example when content is overflowing, cell
        // has a comment
        // or cell contains an invalid formula
        return noneOfTheElementsIsWidget(children);
    }

    private boolean noneOfTheElementsIsWidget(List<WebElement> children) {
        for (WebElement e : children) {
            if (e.getAttribute("class").contains("v-widget")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if this cell has a PopupButton set.
     *
     * @return true if this cell has a pop-up button, false otherwise
     */
    public boolean hasPopupButton() {
        List<WebElement> buttons = findElements(By.className("popupbutton"));
        return !buttons.isEmpty();
    }

    /**
     * Click the PopupButton in the cell.
     *
     * @throws IllegalArgumentException
     *             if the cell doesn't contain a PopupButton
     */
    public void popupButtonClick() {
        if (!hasPopupButton()) {
            throw new IllegalStateException(
                    "This cell doesn't have a PopupuButton");
        }

        findElement(By.className("popupbutton")).click();
    }

    void setParent(SpreadsheetElement parent) {
        this.parent = parent;
    }

    /**
     * Determines if the cell has invalid formula indicator
     *
     * @return true if this cell has a invalid formula indicator
     */
    public boolean hasInvalidFormulaIndicator() {
        List<WebElement> indicators = findElements(
                By.className("cell-invalidformula-triangle"));
        return !indicators.isEmpty();
    }

    /**
     * Determines if the cell has comment indicator
     *
     * @return true if this cell has a comment indicator
     */
    public boolean hasCommentIndicator() {
        List<WebElement> indicators = findElements(
                By.className("cell-comment-triangle"));
        return !indicators.isEmpty();
    }

}
