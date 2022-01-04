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

package com.vaadin.flow.component.tabs.tests;

import org.junit.Test;

import com.vaadin.flow.component.tabs.Tab;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Vaadin Ltd.
 */
public class TabTest {

    private Tab tab = new Tab();

    @Test
    public void shouldCreateEmptyTabWithDefaultState() throws Exception {

        assertThat("Initial label is invalid", tab.getLabel(), is(""));
        assertThat("Initial flexGrow is invalid", tab.getFlexGrow(), is(0.0));
    }

    @Test
    public void shouldCreateTabWithLabel() throws Exception {
        String label = "A label";

        tab = new Tab(label);

        assertThat("Initial label is invalid", tab.getLabel(), is(label));
        assertThat("Initial flexGrow is invalid", tab.getFlexGrow(), is(0.0));
    }

    @Test
    public void shouldSetFlexGrow() throws Exception {
        tab.setFlexGrow(1);

        assertThat("flexGrow is invalid", tab.getFlexGrow(), is(1.0));
    }
}
