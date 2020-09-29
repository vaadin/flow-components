/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.component.combobox.test.dataview;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.test.AbstractComboBoxIT;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.internal.Range;

public abstract class AbstractItemCountComboBoxIT extends AbstractComboBoxIT {

    // changing the dimension might get combo box change what it fetches and
    // how many items it shows, so changing this is a bad idea ...
    private static final Dimension TARGET_SIZE = new Dimension(1000, 900);
    protected ComboBoxElement comboBoxElement;
    protected int sizeIncreasePageCount = 4;
    protected int pageSize = new ComboBox<String>().getPageSize();

    @Override
    protected void open() {
        String url = getRootURL() + getTestPath();
        getDriver().get(url);

        getDriver().manage().window().setSize(TARGET_SIZE);
        comboBoxElement = $(ComboBoxElement.class).first();
    }

    protected void open(int size) {
        String url = getRootURL() + getTestPath() + "/" + size;
        getDriver().get(url);

        getDriver().manage().window().setSize(TARGET_SIZE);
        comboBoxElement = $(ComboBoxElement.class).first();
    }

    protected void doScroll(int itemToScroll, int expectedItems, int fetchIndex,
            int start, int end, String expectedItemText) {
        scrollToItem(comboBoxElement, itemToScroll);
        waitUntilTextInContent(expectedItemText);
        // TODO https://github.com/vaadin/vaadin-combo-box-flow/issues/390:
        //  implement this check after the scrolling issue has been fixed
        // verifyFetchForUndefinedSizeCallback(fetchIndex, Range.between(start,
        //        end));
        verifyItemsSize(expectedItems);
    }

    protected void setUnknownCount() {
        findElement(
                By.id(AbstractItemCountComboBoxPage.UNDEFINED_SIZE_BUTTON_ID))
                        .click();
    }

    protected void setCountCallback() {
        findElement(By.id(AbstractItemCountComboBoxPage.DEFINED_SIZE_BUTTON_ID))
                .click();
    }

    protected void setUnknownCountBackendSize(int size) {
        $(IntegerFieldElement.class).id(
                AbstractItemCountComboBoxPage.UNDEFINED_SIZE_BACKEND_SIZE_INPUT_ID)
                .setValue(size + "");
    }

    protected void setEstimateIncrease(int estimateIncrease) {
        $(IntegerFieldElement.class).id(
                AbstractItemCountComboBoxPage.ITEM_COUNT_ESTIMATE_INCREASE_INPUT)
                .setValue(estimateIncrease + "");
    }

    protected void setEstimate(int estimate) {
        $(IntegerFieldElement.class).id(
                AbstractItemCountComboBoxPage.ITEM_COUNT_ESTIMATE_INPUT)
                .setValue(estimate + "");
    }

    protected int getDefaultInitialItemCount() {
        return pageSize * sizeIncreasePageCount;
    }

    protected void verifyItemsSize(int size) {
        Assert.assertEquals("Item count doesn't match", size,
                getItems(comboBoxElement).size());
    }

    protected void verifyFetchForUndefinedSizeCallback(int index, Range range) {
        WebElement log = findElement(By.id("log-" + index));
        Assert.assertEquals("Invalid range for index " + index,
                index + ":" + range.toString(), log.getText());
    }

}
