package com.vaadin.addon.spreadsheet.elements;

/*
 * #%L
 * Vaadin Spreadsheet Testbench API
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.openqa.selenium.By;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.AbstractElement;

/**
 * This class represents a header (either row- or column) within the currently
 * active sheet of a Spreadsheet.
 * 
 * @author Vaadin Ltd.
 */
public class SheetHeaderElement extends AbstractElement {

    public TestBenchElement getResizeHandle() {
        return wrapElement(
                findElement(By.className("header-resize-dnd-second")),
                getCommandExecutor());
    }
}
