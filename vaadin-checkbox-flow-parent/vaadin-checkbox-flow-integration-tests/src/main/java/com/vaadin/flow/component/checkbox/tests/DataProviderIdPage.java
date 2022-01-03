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
package com.vaadin.flow.component.checkbox.tests;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/data-provider-id")
public class DataProviderIdPage extends Div {

    public static class Message {
        private String text;

        private int id;

        public Message(int id) {
            this.id = id;
        }

        public Message(int id, String text) {
            this(id);
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass().equals(Message.class)) {
                Message msg = (Message) obj;
                return Objects.equals(msg.text, text) && msg.id == id;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, text);
        }
    }

    public DataProviderIdPage() {
        CheckboxGroup<Message> idGroup = new CheckboxGroup<>();

        ListDataProvider<Message> provider = new ListDataProvider<Message>(
                Arrays.asList(new Message(1, "foo"), new Message(2, "bar"))) {
            @Override
            public Object getId(Message item) {
                return item.getId();
            }
        };
        idGroup.setItems(provider);
        idGroup.setItemLabelGenerator(Message::getText);
        idGroup.setId("id-data-provider");

        NativeButton selectById = new NativeButton(
                "Select via Data Provider ID", event -> idGroup.setValue(
                        Collections.singleton(new Message(2, "non-existent"))));

        selectById.setId("select-by-id");

        add(idGroup, selectById);

        CheckboxGroup<Message> equalsGroup = new CheckboxGroup<>();

        equalsGroup.setItems(new Message(1, "foo"), new Message(2, "bar"));
        equalsGroup.setItemLabelGenerator(Message::getText);
        equalsGroup.setId("standard-equals");

        NativeButton selectByEquals = new NativeButton(
                "Select via Data Provider ID", event -> equalsGroup.setValue(
                        Collections.singleton(new Message(2, "bar"))));

        selectByEquals.setId("select-by-equals");

        NativeButton noSelection = new NativeButton(
                "Select via Data Provider ID", event -> equalsGroup.setValue(
                        Collections.singleton(new Message(2, "non-existent"))));

        noSelection.setId("no-selection");

        add(equalsGroup, selectByEquals, noSelection);
    }
}
