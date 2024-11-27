/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.virtuallist.VirtualList;

public class VirtualListNoneSelectionTest {

    private VirtualList<String> list;

    @Before
    public void setup() {
        list = new VirtualList<>();
        list.setItems("foo", "bar", "baz");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addSelectionListener_throws() {
        list.addSelectionListener(e -> {
        });
    }

    @Test(expected = IllegalStateException.class)
    public void asSingleSelect_throws() {
        list.asSingleSelect();
    }

    @Test(expected = IllegalStateException.class)
    public void asMultiSelect_throws() {
        list.asMultiSelect();
    }

    @Test
    public void select_getSelectedItems_empty() {
        list.select("foo");
        Assert.assertTrue(list.getSelectedItems().isEmpty());
    }

    @Test
    public void clientSelectionMode() {
        Assert.assertNull(list.getElement().getProperty("selectionMode"));
    }
}
