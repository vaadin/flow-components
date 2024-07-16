/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.openqa.selenium.support.ui.ExpectedConditions;

@TestPath("vaadin-grid/remove-sortable-column")
public class RemoveSortableColumnsIT extends AbstractComponentIT {

    @Test
    public void removeSortableColumn_shouldNotGenerateAnException() {
        By buttonLocator = By.id(RemoveSortableColumnPage.ID_SORT_BUTTON);

        open();
        waitUntil(ExpectedConditions.elementToBeClickable(buttonLocator));
        findElement(buttonLocator).click();
        SelectElement select = $(SelectElement.class).first();
        select.selectItemByIndex(2);
        Assert.assertThrows(NoSuchElementException.class,
                () -> findElement(By.id("error-handler-message")));
    }
}