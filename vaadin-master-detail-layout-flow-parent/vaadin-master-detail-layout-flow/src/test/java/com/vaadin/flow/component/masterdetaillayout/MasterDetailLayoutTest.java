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
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class MasterDetailLayoutTest {
    private UI ui;
    private MasterDetailLayout layout;

    @Before
    public void init() {
        ui = new UI();
        UI.setCurrent(ui);
        var session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        var service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        layout = new MasterDetailLayout();
        ui.add(layout);
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
    public void setDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        assertDetailContent(detail);
        assertSetDetailCall(detail);
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
        assertSetDetailCall(newDetail);
        Assert.assertEquals(newDetail, layout.getDetail());
        Assert.assertNull(detail.getElement().getParent());
    }

    @Test
    public void setDetail_removeDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        layout.setDetail(null);

        assertSetDetailCall(null);
        Assert.assertNull(layout.getDetail());
        Assert.assertNull(detail.getElement().getParent());
    }

    @Test
    public void setDetail_detach_attach() {
        var detail = new Div();
        layout.setDetail(detail);
        fakeClientCommunication();
        ui.getInternals().dumpPendingJavaScriptInvocations();

        ui.remove(layout);
        ui.add(layout);

        assertSetDetailCall(detail);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDetailText_throws() {
        layout.setDetail(new Text("detail"));
    }

    @Test
    public void showRouterLayoutContent() {
        var detail = new Div();
        layout.showRouterLayoutContent(detail);

        assertDetailContent(detail);
        assertSetDetailCall(detail);
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
        assertSetDetailCall(newDetail);
        Assert.assertEquals(newDetail, layout.getDetail());
        Assert.assertNull(detail.getElement().getParent());

        layout.removeRouterLayoutContent(newDetail);

        assertSetDetailCall(null);
        Assert.assertNull(layout.getDetail());
        Assert.assertNull(newDetail.getElement().getParent());
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

    private void assertMasterContent(Component component) {
        Assert.assertEquals("", component.getElement().getAttribute("slot"));
        Assert.assertEquals(layout.getElement(),
                component.getElement().getParent());
    }

    private void assertDetailContent(Component component) {
        Assert.assertEquals(layout.getElement(),
                component.getElement().getParent());
        Assert.assertTrue(component.getElement().isVirtualChild());
    }

    private void assertSetDetailCall(Component component) {
        fakeClientCommunication();
        var pendingJavaScriptInvocations = ui.getInternals()
                .dumpPendingJavaScriptInvocations();

        Assert.assertEquals(1, pendingJavaScriptInvocations.size());

        var pendingJavaScriptInvocation = pendingJavaScriptInvocations.get(0);
        var parameters = pendingJavaScriptInvocation.getInvocation()
                .getParameters();
        var element = component != null ? component.getElement() : null;

        Assert.assertEquals(
                "return (async function() { this.setDetail($0)}).apply($1)",
                pendingJavaScriptInvocation.getInvocation().getExpression());
        Assert.assertEquals(element, parameters.get(0));
    }

    protected void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
