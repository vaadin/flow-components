/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid.SelectionMode;

public class AbstractGridMultiSelectionModelTest {

    @Test
    public void select_singleItemSignature_sendToClientSide() {
        Set<String> selected = new HashSet<>();
        Grid<String> grid = new Grid<String>() {
            @Override
            void doClientSideSelection(Set<String> items) {
                selected.addAll(items);
            }
        };
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");
        Assert.assertEquals(1, selected.size());
        Assert.assertEquals("foo", selected.iterator().next());
    }

    @Test
    public void select_singleItemSignature_selectFormClient_dontSendToClientSide() {
        Set<String> selected = new HashSet<>();
        Grid<String> grid = new Grid<String>() {
            @Override
            void doClientSideSelection(Set<String> items) {
                selected.addAll(items);
            }
        };
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.getSelectionModel().selectFromClient("foo");
        Assert.assertEquals(0, selected.size());
    }

    @Test
    public void deselect_singleItemSignature_sendToClientSide() {
        Set<String> deselected = new HashSet<>();
        Grid<String> grid = new Grid<String>() {
            @Override
            void doClientSideDeselection(Set<String> items) {
                deselected.addAll(items);
            }
        };
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.deselect("foo");
        Assert.assertEquals(1, deselected.size());
        Assert.assertEquals("foo", deselected.iterator().next());
    }

    @Test
    public void singleItemSignature_deselectFormClient_dontSendToClientSide() {
        Set<String> deselected = new HashSet<>();
        Grid<String> grid = new Grid<String>() {
            @Override
            void doClientSideDeselection(Set<String> items) {
                deselected.addAll(items);
            }
        };
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.getSelectionModel().deselectFromClient("foo");
        Assert.assertEquals(0, deselected.size());
    }
}
