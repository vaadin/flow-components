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
package com.vaadin.flow.component.checkbox.testbench;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-checkbox&gt;</code>
 * element.
 */
@Element("vaadin-checkbox")
public class CheckboxElement extends TestBenchElement
        implements HasLabel, HasHelper {
    /**
     * Checks whether the checkbox is checked.
     *
     * @return <code>true</code> if the checkbox is checked, <code>false</code>
     *         if it is not checked or in indeterminate mode
     */
    public boolean isChecked() {
        return getPropertyBoolean("checked");
    }

    /**
     * Sets whether the checkbox is checked.
     *
     * @param checked
     *            <code>true</code> to check the checkbox, <code>false</code> to
     *            uncheck it
     */
    public void setChecked(boolean checked) {
        setProperty("checked", checked);
    }

    @Override
    public String getLabel() {
        return $("label").first().getPropertyString("textContent");
    }

}
