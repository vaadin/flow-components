/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/combo-box-polymer-wrapper-lit-view")
public class ComboBoxPolymerWrapperLitViewIT
        extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $("combo-box-polymer-wrapper-lit-view").first()
                .$("combo-box-polymer-wrapper").id("cbw1")
                .$(ComboBoxElement.class).id("cb");
    }

    /**
     * This particular test case, test when we use `combo-box` inside a
     * lit-template(the wrapper), and use the wrapped element in another
     * template and interact with those components via serverside Java code
     * (setting value, readonly, disabled ....)
     */
    @Test
    public void comboBoxInitialValue_correctlySet_nestedLitTemplate() {
        String label = "D";
        Assert.assertEquals(label, comboBox.getSelectedText());
    }

    @Test
    public void comboBox_retainValue_openClosed() {
        String labelValue = "1";
        comboBox.openPopup();
        comboBox.closePopup();

        Assert.assertEquals(labelValue, comboBox.getSelectedText());
    }
}
