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
package com.vaadin.flow.component.masterdetaillayout.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-master-detail-layout")
public class MasterDetailLayoutPage extends Div {
    public MasterDetailLayoutPage() {
        MasterDetailLayout layout = new MasterDetailLayout();

        Div master = new Div("Master content");
        layout.setMaster(master);

        Div detail = new Div("Detail content");
        layout.setDetail(detail);

        add(layout);
    }
}
