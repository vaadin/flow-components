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
package com.vaadin.flow.component.details.tests;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-details")
public class Home extends Div {

    public Home() {
        Div info = new Div();
        info.setId("info");

        Details details = new Details();
        details.setSummary(new Span("Some summary"));
        details.add("Some content");

        Details detailsDisabled = new Details();
        detailsDisabled.setOpened(true);
        detailsDisabled.setEnabled(false);
        detailsDisabled.setSummaryText("Disabled heading");
        detailsDisabled.add(new H3("Disabled content"));
        detailsDisabled.add(new Span("Always visible content"));

        add(details, detailsDisabled, info);
    }
}
