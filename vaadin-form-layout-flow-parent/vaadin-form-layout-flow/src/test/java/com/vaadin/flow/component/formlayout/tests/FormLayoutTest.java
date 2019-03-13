/*
 * Copyright 2000-2018 Vaadin Ltd.
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
        formLayout.addClickListener(event -> {});
    }
}
