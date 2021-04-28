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
 *
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.Collection;

@Route("vaadin-grid/recalculate-column-widths")
public class RecalculateColumnWidthsPage extends VerticalLayout {

    public RecalculateColumnWidthsPage() {
        ThreeString ts1 = new ThreeString("111", "222", "333");
        ThreeString ts2 = new ThreeString("444", "555", "667");

        Collection<ThreeString> itemList = new ArrayList<ThreeString>();
        itemList.add(ts1);
        itemList.add(ts2);

        Grid<ThreeString> grid1 = new Grid<>();
        grid1.setId("grid");
        grid1.setHeightByRows(true);
        grid1.setItems(itemList);

        grid1.addColumn(ThreeString::getA).setAutoWidth(true);
        grid1.addColumn(ThreeString::getB).setAutoWidth(true);
        grid1.addColumn(ThreeString::getC).setAutoWidth(true);

        add(grid1);

        // Ensure _recalculateColumnWidthOnceLoadingFinished flag is cleared, otherwise the flag
        // would trigger the column recalculation automatically when refreshing the data
        // The web component has some flaky behaviour where the flag is not always cleared
        // after the initial data load
        // See https://github.com/vaadin/web-components/issues/268
        grid1.getElement().executeJs(
                "$0._recalculateColumnWidthOnceLoadingFinished = false");

        Button button = new Button("Add Text");
        button.setId("change-data-button");
        button.addClickListener(event -> {

            ts2.setB(
                    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
            grid1.getDataProvider().refreshAll();
            grid1.recalculateColumnWidths();
        });

        add(button);
    }

    static class ThreeString {
        private String a;
        private String b;
        private String c;

        public ThreeString(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }
    }
}
