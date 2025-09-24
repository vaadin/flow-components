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
package com.vaadin.flow.component.select.tests;

import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-select/")
public class ItemLabelGeneratorIT extends AbstractSelectIT {

    @Test
    public void testItemLabelGenerator_setGenerator_updatesItemLabel() {
        page.toggleItemLabelGenerator(true);
        verifyItems("-LABEL");

        page.toggleItemLabelGenerator(false);
        verifyItems("");
    }

    private void verifyItems(String labelPostfix) {
        for (int i = 0; i < getInitialNumberOfItems(); i++) {
            selectElement.selectItemByIndex(i);
            verify.selectedItem("Item-" + i + labelPostfix,
                    "Item-" + i + labelPostfix);
        }
    }

    @Test
    public void testItemLabelGenerator_initialItemLabelGenerator_setsItemLabels() {
        openWithExtraParameter("itemLabelGenerator");
        verifyItems("-LABEL");
    }

    @Override
    protected int getInitialNumberOfItems() {
        return 10;
    }
}
