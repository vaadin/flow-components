/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.icon.tests;

import com.vaadin.flow.component.icon.SvgIcon;
import org.junit.Assert;
import org.junit.Test;

public class SvgIconTest {
    @Test
    public void emptyConstructor_hasNoSource() {
        var icon = new SvgIcon();
        Assert.assertNull(icon.getSource());
        Assert.assertNull(icon.getElement().getProperty("src"));
    }

    @Test
    public void sourceConstructor_hasSource() {
        var path = "path/to/file.svg";
        var icon = new SvgIcon(path);
        Assert.assertEquals(path, icon.getSource());
        Assert.assertEquals(path, icon.getElement().getProperty("src"));
    }

    @Test
    public void setSource_hasSource() {
        var icon = new SvgIcon();
        var path = "path/to/file.svg";
        icon.setSource(path);
        Assert.assertEquals(path, icon.getSource());
        Assert.assertEquals(path, icon.getElement().getProperty("src"));
    }

    @Test
    public void modifySource_hasModifiedSource() {
        var icon = new SvgIcon("path/to/file.svg");
        var newPath = "path/to/new/file.svg";
        icon.setSource(newPath);

        Assert.assertEquals(newPath, icon.getSource());
        Assert.assertEquals(newPath, icon.getElement().getProperty("src"));
    }

    @Test
    public void setColor_hasColor() {
        var icon = new SvgIcon();
        icon.setColor("red");
        Assert.assertEquals("red", icon.getColor());
        Assert.assertEquals("red", icon.getStyle().get("fill"));
    }

    @Test
    public void removeColor_hasNoColor() {
        var icon = new SvgIcon();
        icon.setColor("red");
        icon.setColor(null);
        Assert.assertNull(icon.getColor());
        Assert.assertNull(icon.getStyle().get("fill"));
    }
}
