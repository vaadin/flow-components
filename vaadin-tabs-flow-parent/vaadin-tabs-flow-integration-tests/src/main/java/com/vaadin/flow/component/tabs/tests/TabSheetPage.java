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

package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;

/**
 * Test page for {@link TabSheet}.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-tabs/tabsheet")
public class TabSheetPage extends Div {

    public TabSheetPage() {
        var tabsheet = new TabSheet();
        tabsheet.add("Tab one", new Span("Tab one content"));
        tabsheet.add("Tab two", new Span("Tab two content"));
        add(tabsheet);
    }

}
