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
package com.vaadin.flow.component.accordion.tests;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("repro-1638")
public class Repro1638View extends Div {

    public Repro1638View() {
        // Failing case: setOpened(false) on the first panel at creation
        Accordion panelClosed = createAccordion("panel-closed");
        AccordionPanel first = panelClosed.getChildren()
                .map(AccordionPanel.class::cast).findFirst().orElseThrow();
        first.setOpened(false);

        // Control: accordion.close() at creation
        Accordion accordionClosed = createAccordion("accordion-closed");
        accordionClosed.close();

        // Later call: setOpened(false) from a button click
        Accordion later = createAccordion("later");
        AccordionPanel laterFirst = later.getChildren()
                .map(AccordionPanel.class::cast).findFirst().orElseThrow();
        NativeButton closeLater = new NativeButton(
                "setOpened(false) on later[0]",
                e -> laterFirst.setOpened(false));
        closeLater.setId("close-later");

        Span log = new Span();
        log.setId("log");
        NativeButton logState = new NativeButton("log server state",
                e -> log.setText(state(panelClosed) + " | "
                        + state(accordionClosed) + " | " + state(later)));
        logState.setId("log-state");

        add(panelClosed, accordionClosed, later, new Div(closeLater, logState),
                log);
    }

    private static Accordion createAccordion(String id) {
        Accordion accordion = new Accordion();
        accordion.setId(id);
        accordion.add("Panel 1 " + id, new Span("Content 1"));
        accordion.add("Panel 2 " + id, new Span("Content 2"));
        return accordion;
    }

    private static String state(Accordion accordion) {
        return accordion.getId().orElse("") + ": openedIndex="
                + accordion.getOpenedIndex() + " panels="
                + accordion.getChildren().map(AccordionPanel.class::cast)
                        .map(p -> String.valueOf(p.isOpened())).toList();
    }
}
