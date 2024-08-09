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
package com.vaadin.flow.component.accordion.tests;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-accordion/template-support")
@Tag("accordion-app")
@JsModule("./accordion-in-template.js")
public class TemplateSupportView extends LitTemplate {

    @Id
    private Accordion accordion;

    @Id
    private VerticalLayout events;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        accordion.addOpenedChangeListener(e -> e.getSource().getElement()
                .executeJs("const summary = $0 >= 0 ? "
                        + "this.querySelectorAll('span[slot=\"summary\"]')[$0].textContent + ' opened' : "
                        + "'Accordion closed';"
                        + "const newEvent = document.createElement('span');"
                        + "newEvent.textContent = summary;"
                        + "$1.appendChild(newEvent);",
                        e.getOpenedIndex().orElse(-1), events.getElement()));
    }
}
