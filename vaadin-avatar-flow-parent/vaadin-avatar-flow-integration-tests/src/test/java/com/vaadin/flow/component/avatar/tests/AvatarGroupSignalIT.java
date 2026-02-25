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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.avatar.testbench.AvatarGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-avatar/avatar-group-signal-test")
public class AvatarGroupSignalIT extends AbstractComponentIT {

    private AvatarGroupElement avatarGroup;

    @Before
    public void init() {
        open();
        avatarGroup = $(AvatarGroupElement.class).first();
    }

    @Test
    public void initialItems_avatarsRendered() {
        Assert.assertEquals(3, getItemsCount());
        Assert.assertEquals("Alice", getAvatarName(0));
        Assert.assertEquals("Bob", getAvatarName(1));
        Assert.assertEquals("Charlie", getAvatarName(2));
    }

    @Test
    public void addPerson_avatarAppended() {
        clickElementWithJs("addPerson");

        Assert.assertEquals(4, getItemsCount());
        Assert.assertEquals("User 4", getAvatarName(3));
    }

    @Test
    public void removeLast_avatarRemoved() {
        clickElementWithJs("removeLast");

        Assert.assertEquals(2, getItemsCount());
        Assert.assertEquals("Alice", getAvatarName(0));
        Assert.assertEquals("Bob", getAvatarName(1));
    }

    @Test
    public void renameFirst_avatarUpdated() {
        clickElementWithJs("renameFirst");

        Assert.assertEquals("Alice *", getAvatarName(0));
    }

    @Test
    public void addThenRemove_avatarsUpdated() {
        clickElementWithJs("addPerson");
        Assert.assertEquals(4, getItemsCount());

        clickElementWithJs("removeLast");
        Assert.assertEquals(3, getItemsCount());
    }

    private int getItemsCount() {
        return ((Number) executeScript("return arguments[0].items.length",
                avatarGroup)).intValue();
    }

    private String getAvatarName(int index) {
        return avatarGroup.getAvatarElement(index).getPropertyString("name");
    }
}
