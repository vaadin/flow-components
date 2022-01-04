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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Test view for {@link RadioButtonGroup}.
 */
@Route("vaadin-radio-button/detach-reattach")
public class DetachReattachPage extends Div {
    private final DetachReattachTemplate detachReattachTemplate;
    private final Div valueBlock;
    private String value;

    public DetachReattachPage() {
        this.detachReattachTemplate = new DetachReattachTemplate();
        this.valueBlock = new Div();
        valueBlock.setId("valueBlock");
        add(valueBlock);

        createGroupWithTemplate();
        createGroup();
    }

    private void createGroupWithTemplate() {
        NativeButton valueA = new NativeButton("Predefined value A",
                e -> value = "A");
        NativeButton valueB = new NativeButton("Predefined value B",
                e -> value = "B");

        valueA.setId("valueA");
        valueB.setId("valueB");

        NativeButton addGroup = new NativeButton("Add Group",
                e -> attachTemplate(value));
        NativeButton removeGroup = new NativeButton("Remove Group",
                e -> remove(detachReattachTemplate));

        addGroup.setId("addGroup");
        removeGroup.setId("removeGroup");

        NativeButton getValue = new NativeButton("Get Value Template",
                e -> valueBlock.setText(detachReattachTemplate.getRBGValue()));
        getValue.setId("getValueTemplate");

        add(valueA, valueB, addGroup, removeGroup, getValue);
    }

    private void createGroup() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setId("group");

        List<String> items = new LinkedList<>(
                Arrays.asList("foo", "bar", "baz"));
        group.setItems(new ListDataProvider<>(items));

        NativeButton detach = new NativeButton("detach", e -> remove(group));
        detach.setId("detach");

        NativeButton attach = new NativeButton("attach", e -> add(group));
        attach.setId("attach");

        NativeButton setValue = new NativeButton("set value",
                e -> group.setValue("foo"));
        setValue.setId("setValue");

        NativeButton getValue = new NativeButton("Get Value",
                e -> valueBlock.setText(group.getValue()));
        getValue.setId("getValue");

        add(group, detach, attach, setValue, getValue);
    }

    private void attachTemplate(String val) {
        detachReattachTemplate.setRBGValue(val);
        add(detachReattachTemplate);
    }
}
