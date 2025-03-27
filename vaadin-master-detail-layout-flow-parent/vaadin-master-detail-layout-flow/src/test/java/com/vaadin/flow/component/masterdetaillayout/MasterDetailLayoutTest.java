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
package com.vaadin.flow.component.masterdetaillayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;

public class MasterDetailLayoutTest {
    private MasterDetailLayout layout;

    @Before
    public void init() {
        layout = new MasterDetailLayout();
    }

    @Test
    public void setMaster() {
        var master = new Div();
        layout.setMaster(master);

        assertMasterContent(master);
    }

    @Test
    public void getMaster() {
        var master = new Div();
        layout.setMaster(master);

        Assert.assertEquals(master, layout.getMaster());
    }

    @Test
    public void setMaster_replaceMaster() {
        var master = new Div();
        layout.setMaster(master);

        var newMaster = new Div();
        layout.setMaster(newMaster);

        assertMasterContent(newMaster);
        Assert.assertNull(master.getElement().getParent());
        Assert.assertNull(master.getElement().getAttribute("slot"));
    }

    @Test(expected = NullPointerException.class)
    public void setMasterNull_throws() {
        layout.setMaster(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMasterText_throws() {
        layout.setMaster(new Text("master"));
    }

    @Test
    public void setMaster_preservesDetail() {
        var detail = new Div();
        layout.setDetail(detail);
        var master = new Div();
        layout.setMaster(master);

        Assert.assertEquals(2, layout.getElement().getChildCount());
        assertMasterContent(master);
        assertDetailContent(detail);
    }

    @Test
    public void setDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        assertDetailContent(detail);
    }

    @Test
    public void getDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        Assert.assertEquals(detail, layout.getDetail());
    }

    @Test
    public void setDetail_replaceDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        var newDetail = new Div();
        layout.setDetail(newDetail);

        assertDetailContent(newDetail);
        Assert.assertNull(detail.getElement().getParent());
        Assert.assertNull(detail.getElement().getAttribute("slot"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDetailText_throws() {
        layout.setDetail(new Text("detail"));
    }

    @Test
    public void setDetail_preservesMaster() {
        var master = new Div();
        layout.setMaster(master);
        var detail = new Div();
        layout.setDetail(detail);

        Assert.assertEquals(2, layout.getElement().getChildCount());
        assertMasterContent(master);
        assertDetailContent(detail);
    }

    @Test
    public void setDetail_removeDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        layout.setDetail(null);

        Assert.assertEquals(0, layout.getElement().getChildCount());
        Assert.assertNull(detail.getElement().getParent());
        Assert.assertNull(detail.getElement().getAttribute("slot"));
    }

    @Test
    public void showRouterLayoutContent() {
        var detail = new Div();
        layout.showRouterLayoutContent(detail);

        assertDetailContent(detail);
        Assert.assertEquals(detail, layout.getDetail());
    }

    @Test
    public void updateRouterLayoutContent() {
        var detail = new Div();
        layout.showRouterLayoutContent(detail);

        var newDetail = new Div();
        layout.removeRouterLayoutContent(detail);
        layout.showRouterLayoutContent(newDetail);

        assertDetailContent(newDetail);
        Assert.assertNull(detail.getElement().getParent());
        Assert.assertNull(detail.getElement().getAttribute("slot"));
    }

    @Test
    public void setMasterSize_getMasterSize() {
        String size = "300px";
        layout.setMasterSize(size);
        Assert.assertEquals(size, layout.getMasterSize());
        Assert.assertEquals(size,
                layout.getElement().getProperty("masterSize"));
    }

    @Test
    public void setMasterSizeWithUnit_getMasterSize() {
        layout.setMasterSize(30, Unit.EM);
        Assert.assertEquals("30.0em", layout.getMasterSize());
        Assert.assertEquals("30.0em",
                layout.getElement().getProperty("masterSize"));
    }

    @Test
    public void setMasterMinSize_getMasterMinSize() {
        String minSize = "200px";
        layout.setMasterMinSize(minSize);
        Assert.assertEquals(minSize, layout.getMasterMinSize());
        Assert.assertEquals(minSize,
                layout.getElement().getProperty("masterMinSize"));
    }

    @Test
    public void setMasterMinSizeWithUnit_getMasterMinSize() {
        layout.setMasterMinSize(30, Unit.EM);
        Assert.assertEquals("30.0em", layout.getMasterMinSize());
        Assert.assertEquals("30.0em",
                layout.getElement().getProperty("masterMinSize"));
    }

    @Test
    public void setDetailSize_getDetailSize() {
        String size = "400px";
        layout.setDetailSize(size);
        Assert.assertEquals(size, layout.getDetailSize());
        Assert.assertEquals(size,
                layout.getElement().getProperty("detailSize"));
    }

    @Test
    public void setDetailSizeWithUnit_getDetailSize() {
        layout.setDetailSize(30, Unit.EM);
        Assert.assertEquals("30.0em", layout.getDetailSize());
        Assert.assertEquals("30.0em",
                layout.getElement().getProperty("detailSize"));
    }

    @Test
    public void setDetailMinSize_getDetailMinSize() {
        String minSize = "250px";
        layout.setDetailMinSize(minSize);
        Assert.assertEquals(minSize, layout.getDetailMinSize());
        Assert.assertEquals(minSize,
                layout.getElement().getProperty("detailMinSize"));
    }

    @Test
    public void setDetailMinSizeWithUnit_getDetailMinSize() {
        layout.setDetailMinSize(30, Unit.EM);
        Assert.assertEquals("30.0em", layout.getDetailMinSize());
        Assert.assertEquals("30.0em",
                layout.getElement().getProperty("detailMinSize"));
    }

    @Test
    public void setOrientation_getOrientation() {
        Assert.assertEquals(layout.getOrientation(),
                MasterDetailLayout.Orientation.HORIZONTAL);

        layout.setOrientation(MasterDetailLayout.Orientation.VERTICAL);

        Assert.assertEquals(layout.getOrientation(),
                MasterDetailLayout.Orientation.VERTICAL);
    }

    @Test
    public void setContainment_getContainment() {
        Assert.assertEquals(layout.getContainment(),
                MasterDetailLayout.Containment.LAYOUT);

        layout.setContainment(MasterDetailLayout.Containment.VIEWPORT);

        Assert.assertEquals(layout.getContainment(),
                MasterDetailLayout.Containment.VIEWPORT);
    }

    @Test
    public void setForceOverlay_isForceOverlay() {
        Assert.assertFalse(layout.isForceOverlay());
        Assert.assertFalse(
                layout.getElement().getProperty("forceOverlay", false));

        layout.setForceOverlay(true);

        Assert.assertTrue(layout.isForceOverlay());
        Assert.assertTrue(
                layout.getElement().getProperty("forceOverlay", false));
    }

    @Test
    public void setAnimationEnabled_isAnimationEnabled() {
        Assert.assertTrue(layout.isAnimationEnabled());
        Assert.assertFalse(
                layout.getElement().getProperty("noAnimation", false));

        layout.setAnimationEnabled(false);

        Assert.assertFalse(layout.isAnimationEnabled());
        Assert.assertTrue(
                layout.getElement().getProperty("noAnimation", false));
    }

    @Test
    public void setStackThreshold_getStackThreshold() {
        String threshold = "600px";
        layout.setStackThreshold(threshold);
        Assert.assertEquals(threshold, layout.getStackThreshold());
        Assert.assertEquals(threshold,
                layout.getElement().getProperty("stackThreshold"));
    }

    @Test
    public void setStackThresholdWithUnit_getStackThreshold() {
        layout.setStackThreshold(30, Unit.EM);
        Assert.assertEquals("30.0em", layout.getStackThreshold());
        Assert.assertEquals("30.0em",
                layout.getElement().getProperty("stackThreshold"));
    }

    private void assertMasterContent(Component component) {
        Assert.assertEquals("", component.getElement().getAttribute("slot"));
        Assert.assertEquals(layout.getElement(),
                component.getElement().getParent());
    }

    private void assertDetailContent(Component component) {
        Assert.assertEquals("detail",
                component.getElement().getAttribute("slot"));
        Assert.assertEquals(layout.getElement(),
                component.getElement().getParent());
    }
}
