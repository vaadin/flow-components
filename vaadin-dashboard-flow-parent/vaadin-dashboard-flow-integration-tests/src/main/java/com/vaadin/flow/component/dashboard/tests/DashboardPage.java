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
package com.vaadin.flow.component.dashboard.tests;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-dashboard")
public class DashboardPage extends Div {
    public DashboardPage() {
        var db = new Dashboard<MyWidget>();

        var salesWidget = new SalesWidget();
        var npmWidget = new NpmWidget();
        var kpiWidget = new KpiWidget("2");

        db.setWigets(salesWidget, npmWidget, kpiWidget);

        db.addDragendListener(e -> {
            System.out.println("New widget order:");
            for (MyWidget widget : e.getWidgets()) {
                System.out.println(widget.getType());
                widget.updateWidget();
            }
        });

        add(db);
    }

    public abstract class MyWidget extends DashboardWidget {

        abstract String getType();

        abstract void updateWidget();

    }

    // Custom widget class
    public class SalesWidget extends MyWidget {
        private Div content = new Div();

        public SalesWidget() {
            super.setTitle("Sales");
            content.setText("Sales chart");
            super.setContent(content);
        }

        @Override
        String getType() {
            return "Sales";
        }

        public void updateWidget() {

        }

    }

    // Custom widget class
    public class NpmWidget extends MyWidget {
        private Div content = new Div();

        public NpmWidget() {
            super.setTitle("Npm");
            content.setText("Npm statistics");
            super.setContent(content);
        }

        @Override
        String getType() {
            return "Npm";
        }

        @Override
        void updateWidget() {

        }
    }

    // Custom widget class
    public class KpiWidget extends MyWidget {
        private Div content = new Div();

        public KpiWidget(String id) {
            super.setTitle("KPI");
            content.setText("KPI chart");
            super.setContent(content);
        }

        @Override
        String getType() {
            return "KPI";
        }

        @Override
        void updateWidget() {

        }
    }

}
