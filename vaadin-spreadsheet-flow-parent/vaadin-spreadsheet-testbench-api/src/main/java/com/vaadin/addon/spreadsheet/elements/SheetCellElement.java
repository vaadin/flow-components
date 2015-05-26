package com.vaadin.addon.spreadsheet.elements;

import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.By;
import com.vaadin.testbench.elementsbase.AbstractElement;

/**
 * This class represents one cell within the currently active sheet of a
 * Spreadsheet.
 * 
 * @author Vaadin Ltd.
 */
public class SheetCellElement extends AbstractElement {

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
            click();
            WebElement functionFieldInput = parent.getFunctionFieldInput();
            functionFieldInput.clear();
            functionFieldInput.sendKeys(newValue);
            functionFieldInput.sendKeys(Keys.RETURN);
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
        return children.isEmpty();
    }

    void setParent(SpreadsheetElement parent) {
        this.parent = parent;
    }
}
