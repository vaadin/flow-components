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
package com.vaadin.flow.component.card.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-card")
public class CardPage extends Div {

    public CardPage() {
        Card card = new Card();

        card.setTitle(new Span("Title"));
        card.setSubtitle(new Span("Subtitle"));
        card.setMedia(
                new Image("https://vaadin.com/images/vaadin-logo.svg", ""));
        card.setHeaderPrefix(new Span("Header prefix"));
        card.setHeaderSuffix(new Span("Header suffix"));
        card.add(new Text(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
                        + "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim "
                        + "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex "
                        + "ea commodo consequat."),
                new NativeButton("Interactive Content"));
        card.addToFooter(new Div("Footer text"),
                new NativeButton("Interactive Footer Content"));

        card.getStyle().set("background-color", "lightblue");
        card.setMaxWidth("300px");
        card.setMaxHeight("500px");
        add(card);

        var setStringTitle = new NativeButton("Set string title",
                event -> card.setTitle("String title"));
        setStringTitle.setId("set-string-title");
        add(setStringTitle);

        var setTitleComponent = new NativeButton("Set title component",
                event -> card.setTitle(new Div("Title component")));
        setTitleComponent.setId("set-title-component");
        add(setTitleComponent);
    }
}
