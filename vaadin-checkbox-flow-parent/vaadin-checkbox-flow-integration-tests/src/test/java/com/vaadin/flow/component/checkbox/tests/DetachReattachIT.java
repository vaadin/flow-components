/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-checkbox/detach-reattach")
public class DetachReattachIT extends AbstractComponentIT {

    @Test
    public void selectValue_detachCheckboxGroup_reattach_valuesChecked() {
        open();

        clickButton("setValue");
        List<Boolean> checkedBeforeDetach = getCheckboxesCheckedState();
        clickButton("detach");
        clickButton("attach");
        Assert.assertEquals("Checkboxes should remain checked on reattach",
                checkedBeforeDetach, getCheckboxesCheckedState());
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
                getCheckboxesCheckedState().stream()
                        .noneMatch(checked -> checked));
    }

    private List<Boolean> getCheckboxesCheckedState() {
        TestBenchElement group = $("vaadin-checkbox-group").first();
        return group.$(CheckboxElement.class).all().stream()
                .map(CheckboxElement::isChecked).collect(Collectors.toList());
    }

    private void clickButton(String id) {
        $("button").id(id).click();
    }

}
