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
package com.vaadin.flow.component.formlayout.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.formlayout.FormLayout;

public class FormLayoutTest {

    @Test
    public void getResponsiveSteps_noInitialSteps_emptyListIsReturned() {
        FormLayout layout = new FormLayout();
        Assert.assertTrue(layout.getResponsiveSteps().isEmpty());
    }

    @Test
    public void create_FormLayout() {
        // Just testing that creating form layout actually compiles and doesn't
        // throw. Test is on purpose, so that the implementation not
        // accidentally removed.
        FormLayout formLayout = new FormLayout();
        formLayout.addClickListener(event -> {
        });
    }

    @Test
    public void verifyColspanElement() {
        FormLayout layout = new FormLayout();
        // using layouts as components to avoid importing dependencies.

        // verifying the colspan is correctly set in the element itself
        FormLayout comp1 = new FormLayout();
        layout.add(comp1, 2);
        String strColspan = comp1.getElement().getAttribute("colspan");
        Assert.assertEquals(Integer.parseInt(strColspan), 2);
    }

    @Test
    public void verifyColspanCodeBehaviour() {
        FormLayout layout = new FormLayout();
        // using layouts as components to avoid importing dependencies.

        // verifying normal use cases
        FormLayout comp1 = new FormLayout();
        layout.add(comp1, 2);
        Assert.assertEquals(layout.getColspan(comp1), 2);
        layout.setColspan(comp1, 1);
        Assert.assertEquals(layout.getColspan(comp1), 1);

        // verifying it correctly sets it to 1 if an number lower than 1 is
        // supplied
        FormLayout comp2 = new FormLayout();
        layout.add(comp2, -1);
        Assert.assertEquals(layout.getColspan(comp2), 1);

        // verifying it correctly gets 1 if invalid colspans are supplied
        // outside the API
        FormLayout compInvalid = new FormLayout();
        layout.add(compInvalid);
        compInvalid.getElement().setAttribute("colspan", "qsd4hdsj%f");
        Assert.assertEquals(layout.getColspan(compInvalid), 1);

        // verifying it correctly gets 1 if no colspan was set.
        FormLayout compUnset = new FormLayout();
        layout.add(compUnset);
        Assert.assertEquals(layout.getColspan(compUnset), 1);

    }

}
