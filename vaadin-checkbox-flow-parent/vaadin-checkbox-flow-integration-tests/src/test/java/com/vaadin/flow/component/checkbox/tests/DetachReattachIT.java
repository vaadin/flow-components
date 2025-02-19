/*
 * Copyright 2000-2025 Vaadin Ltd.
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
