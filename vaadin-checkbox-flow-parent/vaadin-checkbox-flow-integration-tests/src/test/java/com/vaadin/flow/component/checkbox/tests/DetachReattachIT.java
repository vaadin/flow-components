/*
 * Copyright 2000-2022 Vaadin Ltd.
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
        $("button").id(id).click();
    }

}
