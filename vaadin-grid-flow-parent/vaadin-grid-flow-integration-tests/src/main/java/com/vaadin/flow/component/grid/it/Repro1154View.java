/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1154.
 *
 * Sorting a grid column rendered with addComponentColumn reportedly produces
 * much heavier update traffic (~28KB vs ~4KB for a plain value column with 100
 * items). Two identical grids: one with a plain id column, one with a
 * component id column — sort each and compare the UIDL response sizes.
 */
@Route("repro-1154")
public class Repro1154View extends Div {

    public static class SimpleEntity {
        private final int id;
        private final int col01;

        public SimpleEntity(int i) {
            this.id = i;
            this.col01 = (i * 31 + 7) % 100;
        }

        public int getId() {
            return id;
        }

        public int getCol01() {
            return col01;
        }
    }

    public Repro1154View() {
        List<SimpleEntity> dataList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            dataList.add(new SimpleEntity(i));
        }

        Grid<SimpleEntity> plainGrid = new Grid<>();
        plainGrid.getElement().setAttribute("id", "plain-grid");
        plainGrid.setItems(dataList);
        plainGrid.addColumn(SimpleEntity::getId).setHeader("Id")
                .setComparator(Comparator.comparingInt(SimpleEntity::getId))
                .setKey("idcol");
        plainGrid.addColumn(SimpleEntity::getCol01).setHeader("Col01")
                .setSortable(true).setKey("col01");

        Grid<SimpleEntity> componentGrid = new Grid<>();
        componentGrid.getElement().setAttribute("id", "component-grid");
        componentGrid.setItems(dataList);
        componentGrid
                .addComponentColumn(
                        entity -> new Text(String.valueOf(entity.getId())))
                .setHeader("Id")
                .setComparator(Comparator.comparingInt(SimpleEntity::getId))
                .setKey("idcol");
        componentGrid.addColumn(SimpleEntity::getCol01).setHeader("Col01")
                .setSortable(true).setKey("col01");

        add(plainGrid, componentGrid);
    }
}
