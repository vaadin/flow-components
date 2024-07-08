/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.List;
import java.util.stream.Collectors;

@TestPath("vaadin-checkbox/detach-reattach")
public class DetachReattachIT extends AbstractComponentIT {

    @Test
    public void selectValue_detachCheckboxGroup_reattach_valuesChecked() {
        open();

        clickButton("setValue");
        List checkedBeforeDetach = getCheckboxexCheckedState();
        clickButton("detach");
        clickButton("attach");
        Assert.assertEquals("Checkboxes should remain checked on reattach",
                checkedBeforeDetach, getCheckboxexCheckedState());
    }

    @Test
    public void selectValue_detachCheckboxGroup_deselectAll_reattach_valuesNotChecked() {
        open();

        clickButton("setValue");
        clickButton("detach");
        clickButton("deselectAll");
        clickButton("attach");
        Assert.assertTrue(
                "Checkboxes should not be checked after deselectAll on reattach",
                getCheckboxexCheckedState().stream()
                        .allMatch(checked -> checked == null));
    }

    private List getCheckboxexCheckedState() {
        TestBenchElement group = $("vaadin-checkbox-group").first();
        return group.findElements(By.tagName("vaadin-checkbox")).stream()
                .map(checkbox -> checkbox.getAttribute("checked"))
                .collect(Collectors.toList());
    }

    private void clickButton(String id) {
        $("#" + id).first().click();
    }

}
