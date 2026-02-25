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
package com.vaadin.flow.component.avatar.tests;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("vaadin-avatar/avatar-group-signal-test")
public class AvatarGroupSignalPage extends Div {

    private final List<ValueSignal<AvatarGroupItem>> itemSignals = new ArrayList<>();
    private final ValueSignal<List<ValueSignal<AvatarGroupItem>>> listSignal;

    public AvatarGroupSignalPage() {
        itemSignals.add(new ValueSignal<>(new AvatarGroupItem("Alice")));
        itemSignals.add(new ValueSignal<>(new AvatarGroupItem("Bob")));
        itemSignals.add(new ValueSignal<>(new AvatarGroupItem("Charlie")));
        listSignal = new ValueSignal<>(List.copyOf(itemSignals));

        var group = new AvatarGroup(listSignal);

        var addButton = new NativeButton("Add person", e -> {
            var name = "User " + (itemSignals.size() + 1);
            itemSignals.add(new ValueSignal<>(new AvatarGroupItem(name)));
            listSignal.set(List.copyOf(itemSignals));
        });
        addButton.setId("addPerson");

        var removeLastButton = new NativeButton("Remove last", e -> {
            if (!itemSignals.isEmpty()) {
                itemSignals.removeLast();
                listSignal.set(List.copyOf(itemSignals));
            }
        });
        removeLastButton.setId("removeLast");

        var renameFirstButton = new NativeButton("Rename first", e -> {
            if (!itemSignals.isEmpty()) {
                var current = itemSignals.getFirst().peek();
                itemSignals.getFirst()
                        .set(new AvatarGroupItem(current.getName() + " *"));
            }
        });
        renameFirstButton.setId("renameFirst");

        add(group, new Div(addButton, removeLastButton, renameFirstButton));
    }
}
