/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/template-renderer-event-handler")
public class TemplateRendererEventHandlerPage extends Div {

    public TemplateRendererEventHandlerPage() {
        TemplateRenderer<Person> renderer = TemplateRenderer.<Person> of(
                "<div>[[item.name]] <button on-click='clicked'>Click</button></div>")
                .withProperty("name", Person::getFirstName)
                .withEventHandler("clicked", this::clicked);

        Person person = new Person("John Doe", 1981);

        Grid<Person> personGrid = new Grid<>();
        personGrid.addColumn(renderer);
        personGrid.setItems(person);

        NativeButton showHideGrid = new NativeButton("Toggle Grid Containment",
                e -> {
                    if (personGrid.getParent().isPresent()) {
                        remove(personGrid);
                    } else {
                        add(personGrid);
                    }
                });
        showHideGrid.setId("show-hide");
        add(showHideGrid);
        add(personGrid);
    }

    private void clicked(Person person) {
        Div message = new Div();
        message.addClassName("clicked-person");
        message.setText(person.getFirstName());
        add(message);
    }
}
