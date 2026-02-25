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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class AvatarGroupSignalTest extends AbstractSignalsUnitTest {

    private AvatarGroup avatarGroup;

    @Before
    public void setup() {
        avatarGroup = new AvatarGroup();
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        avatarGroup = new AvatarGroup(listSignal);
        ui.add(avatarGroup);

        Assert.assertEquals(2, avatarGroup.getItems().size());
        Assert.assertEquals("Alice", avatarGroup.getItems().get(0).getName());
        Assert.assertEquals("Bob", avatarGroup.getItems().get(1).getName());
    }

    @Test
    public void signalConstructor_updatesWhenSignalChanges() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup = new AvatarGroup(listSignal);
        ui.add(avatarGroup);

        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        listSignal.set(List.of(item1Signal, item2Signal));

        Assert.assertEquals(2, avatarGroup.getItems().size());
    }

    @Test(expected = BindingActiveException.class)
    public void signalConstructor_setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup = new AvatarGroup(listSignal);
        ui.add(avatarGroup);

        avatarGroup.setItems(List.of(new AvatarGroupItem("Bob")));
    }

    @Test
    public void bindItems_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assert.assertEquals(2, avatarGroup.getItems().size());
        Assert.assertEquals("Alice", avatarGroup.getItems().get(0).getName());
        Assert.assertEquals("Bob", avatarGroup.getItems().get(1).getName());
    }

    @Test
    public void bindItems_updatesWhenListSignalChanges() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assert.assertEquals(1, avatarGroup.getItems().size());

        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var item3Signal = new ValueSignal<>(new AvatarGroupItem("Charlie"));
        listSignal.set(List.of(item1Signal, item2Signal, item3Signal));

        Assert.assertEquals(3, avatarGroup.getItems().size());
        Assert.assertEquals("Charlie", avatarGroup.getItems().get(2).getName());
    }

    @Test
    public void bindItems_updatesWhenItemSignalChanges() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assert.assertEquals("Alice", avatarGroup.getItems().get(0).getName());

        item1Signal.set(new AvatarGroupItem("Updated Alice"));

        Assert.assertEquals("Updated Alice",
                avatarGroup.getItems().get(0).getName());
    }

    @Test
    public void bindItems_notAttached_bindingInactiveUntilAttach() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        avatarGroup.bindItems(listSignal);

        Assert.assertEquals(0, avatarGroup.getItems().size());

        ui.add(avatarGroup);

        Assert.assertEquals(2, avatarGroup.getItems().size());
    }

    @Test(expected = BindingActiveException.class)
    public void setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        avatarGroup.setItems(List.of(new AvatarGroupItem("Bob")));
    }

    @Test(expected = BindingActiveException.class)
    public void addWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        avatarGroup.add(new AvatarGroupItem("Bob"));
    }

    @Test(expected = BindingActiveException.class)
    public void removeWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        avatarGroup.remove(avatarGroup.getItems().get(0));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_calledTwice_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        avatarGroup.bindItems(listSignal);
    }

    @Test(expected = NullPointerException.class)
    public void bindItems_nullSignal_throws() {
        avatarGroup.bindItems(null);
    }
}
