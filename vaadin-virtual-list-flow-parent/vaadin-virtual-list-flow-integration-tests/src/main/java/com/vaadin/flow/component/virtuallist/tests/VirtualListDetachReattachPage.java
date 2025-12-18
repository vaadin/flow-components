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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link VirtualList}
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-virtual-list/detach-reattach")
public class VirtualListDetachReattachPage extends Div {

    private static final List<String> ITEMS;
    static {
        ITEMS = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            ITEMS.add("Item " + (i + 1));
        }
    }

    public static class Person {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Creates all the components needed for the tests.
     */
    public VirtualListDetachReattachPage() {
        Div container1 = new Div(new Text("Container 1"));
        container1.setId("container-1");

        Div container2 = new Div(new Text("Container 2"));
        container2.setId("container-2");

        VirtualList<Person> list = new VirtualList<>();
        list.setId("list");

        list.setItems(createPeople(20));
        list.setRenderer(Person::getName);
        container1.add(list);
        add(container1);

        NativeButton detach = new NativeButton("Detach list",
                e -> list.getParent().ifPresent(
                        parent -> ((HasComponents) parent).remove(list)));
        detach.setId("list-detach");

        NativeButton detachAndReattach = new NativeButton(
                "Detach and re-attach",
                e -> list.getParent().ifPresent(parent -> {
                    ((HasComponents) parent).remove(list);
                    ((HasComponents) parent).add(list);
                }));
        detachAndReattach.setId("list-detach-and-reattach");

        NativeButton attach1 = new NativeButton("Attach list to container 1",
                e -> container1.add(list));
        attach1.setId("list-attach-1");

        NativeButton attach2 = new NativeButton("Attach list to container 2",
                e -> container2.add(list));
        attach2.setId("list-attach-2");

        NativeButton invisible = new NativeButton("Set list invisible",
                e -> list.setVisible(false));
        invisible.setId("list-invisible");

        NativeButton visible = new NativeButton("Set list visible",
                e -> list.setVisible(true));
        visible.setId("list-visible");

        NativeButton useComponentRenderer = new NativeButton(
                "Use component renderer", e -> list.setRenderer(
                        new ComponentRenderer<Div, Person>(person -> {
                            Div text = new Div(new Text(person.getName()));
                            text.addClassName("component-rendered");
                            return text;
                        })));
        useComponentRenderer.setId("list-use-component-renderer");

        add(container1, container2, detach, detachAndReattach, attach1, attach2,
                invisible, visible, useComponentRenderer);
    }

    private List<Person> createPeople(int amount) {
        List<Person> people = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            Person person = new Person();
            person.setName("Person " + (i + 1));
            people.add(person);
        }
        return people;
    }
}
