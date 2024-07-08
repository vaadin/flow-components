/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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

        group.setItems("foo", "bar", "baz");

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
