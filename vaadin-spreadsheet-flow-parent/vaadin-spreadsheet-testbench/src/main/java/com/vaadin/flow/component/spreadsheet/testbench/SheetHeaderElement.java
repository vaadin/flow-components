/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.testbench;

import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.By;

/**
 * This class represents a header (either row- or column) within the currently
 * active sheet of a Spreadsheet.
 *
 * @author Vaadin Ltd.
 */
public class SheetHeaderElement extends TestBenchElement {

    public TestBenchElement getResizeHandle() {
        return wrapElement(
                findElement(By.className("header-resize-dnd-second")),
                getCommandExecutor());
    }
}
