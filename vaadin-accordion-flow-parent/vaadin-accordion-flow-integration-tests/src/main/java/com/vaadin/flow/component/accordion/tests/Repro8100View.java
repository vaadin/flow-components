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

/**
 * Repro for https://github.com/vaadin/flow-components/issues/8100
 *
 * AccordionPanel.setOpened(true) reportedly does not open the panel if the
 * panel's visibility was toggled to false in an earlier request and back to
 * true in the same request as setOpened(true).
 */
@Route("repro-8100")
public class Repro8100View extends Div {

    private final Accordion accordion = new Accordion();
    private final AccordionPanel panel1;
    private final AccordionPanel panel2;

    private final Accordion isoAccordion = new Accordion();
    private final AccordionPanel isoPanel;

    private final Span status = new Span();

    public Repro8100View() {
        accordion.setId("accordion");
        panel1 = accordion.add("panel1", new Div(new Span("content 1")));
        panel1.setId("panel1");
        panel2 = accordion.add("panel2", new Div(new Span("content 2")));
        panel2.setId("panel2");
        accordion.close();

        // Reporter scenario: mirrors combobox str1 / str2 branches.
        NativeButton str1 = new NativeButton("Select str1", e -> {
            accordion.close();
            panel2.setVisible(false);
            panel1.setVisible(true);
            panel1.setOpened(true);
            accordion.open(panel1);
            updateStatus();
        });
        str1.setId("str1");

        NativeButton str2 = new NativeButton("Select str2", e -> {
            accordion.close();
            panel1.setVisible(false);
            panel2.setVisible(true);
            panel2.setOpened(true);
            accordion.open(panel2);
            updateStatus();
        });
        str2.setId("str2");

        // Isolated minimal pair: single panel, no accordion index juggling.
        isoAccordion.setId("iso-accordion");
        isoPanel = isoAccordion.add("isoPanel",
                new Div(new Span("iso content")));
        isoPanel.setId("iso-panel");
        isoAccordion.close();

        NativeButton isoHide = new NativeButton("iso: hide (request 1)", e -> {
            isoPanel.setVisible(false);
            updateStatus();
        });
        isoHide.setId("iso-hide");

        NativeButton isoShowOpen = new NativeButton(
                "iso: show + setOpened(true) (request 2)", e -> {
                    isoPanel.setVisible(true);
                    isoPanel.setOpened(true);
                    updateStatus();
                });
        isoShowOpen.setId("iso-show-open");

        status.setId("status");
        updateStatus();

        add(accordion, new Div(str1, str2),
                new Span("--- isolated single panel ---"), isoAccordion,
                new Div(isoHide, isoShowOpen), new Div(status));
    }

    private void updateStatus() {
        status.setText(String.format(
                "p1[vis=%s,open=%s] p2[vis=%s,open=%s] iso[vis=%s,open=%s]",
                panel1.isVisible(), panel1.isOpened(), panel2.isVisible(),
                panel2.isOpened(), isoPanel.isVisible(), isoPanel.isOpened()));
    }
}
