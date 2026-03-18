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
package com.vaadin.flow.component.masterdetaillayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

public class MasterDetailLayoutTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT);

    private final MasterDetailLayout layout = new MasterDetailLayout();

    @Before
    public void setup() {
        ui.add(layout);
    }

    @Test
    public void constructorWithSizesAndExpand() {
        var customLayout = new MasterDetailLayout("400px", "200px",
                MasterDetailLayout.Expand.MASTER);
        ui.add(customLayout);

        Assert.assertEquals("400px", customLayout.getMasterSize());
        Assert.assertEquals("200px", customLayout.getDetailSize());
        Assert.assertEquals(MasterDetailLayout.Expand.MASTER,
                customLayout.getExpand());
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
        assertSetDetailCall(detail, true);
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
        assertSetDetailCall(newDetail, true);
        Assert.assertEquals(newDetail, layout.getDetail());
        Assert.assertNull(detail.getElement().getParent());
    }

    @Test
    public void setDetail_removeDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        layout.setDetail(null);

        assertSetDetailCall(null, true);
        Assert.assertNull(layout.getDetail());
        Assert.assertNull(detail.getElement().getParent());
    }

    @Test
    public void setDetail_detach_attach() {
        var detail = new Div();
        layout.setDetail(detail);
        assertSetDetailCall(detail, true);

        ui.remove(layout);
        ui.add(layout);

        assertSetDetailCall(detail, true);
    }

    @Test
    public void setDetail_usesViewTransitionAfterFirstRoundtrip() {
        // No transition if component is attached in first roundtrip
        var detail = new Div();
        layout.setDetail(detail);
        assertSetDetailCall(detail, true);

        // Use transition when updating details in second roundtrip
        var newDetail = new Div();
        layout.setDetail(newDetail);
        assertSetDetailCall(newDetail, false);

        // No transition when detaching and reattaching the component
        ui.remove(layout);
        ui.add(layout);
        assertSetDetailCall(newDetail, true);
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
        assertSetDetailCall(detail, true);
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
        assertSetDetailCall(newDetail, true);
        Assert.assertEquals(newDetail, layout.getDetail());
        Assert.assertNull(detail.getElement().getParent());

        layout.removeRouterLayoutContent(newDetail);

        assertSetDetailCall(null, false);
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
    public void setOverlaySize_getOverlaySize() {
        String size = "500px";
        layout.setOverlaySize(size);
        Assert.assertEquals(size, layout.getOverlaySize());
        Assert.assertEquals(size,
                layout.getElement().getProperty("overlaySize"));
    }

    @Test
    public void setOverlaySize_resetToNull() {
        layout.setOverlaySize("500px");
        layout.setOverlaySize(null);
        Assert.assertNull(layout.getOverlaySize());
    }

    @Test
    public void setOverlaySizeWithUnit_getOverlaySize() {
        layout.setOverlaySize(100, Unit.PERCENTAGE);
        Assert.assertEquals("100.0%", layout.getOverlaySize());
        Assert.assertEquals("100.0%",
                layout.getElement().getProperty("overlaySize"));
    }

    @Test
    public void setOrientation_getOrientation() {
        Assert.assertEquals(MasterDetailLayout.Orientation.HORIZONTAL,
                layout.getOrientation());

        layout.setOrientation(MasterDetailLayout.Orientation.VERTICAL);

        Assert.assertEquals(MasterDetailLayout.Orientation.VERTICAL,
                layout.getOrientation());
        Assert.assertEquals("vertical",
                layout.getElement().getProperty("orientation"));
    }

    @Test(expected = NullPointerException.class)
    public void setOrientationNull_throws() {
        layout.setOrientation(null);
    }

    @Test
    public void setOverlayContainment_getOverlayContainment() {
        Assert.assertEquals(MasterDetailLayout.OverlayContainment.LAYOUT,
                layout.getOverlayContainment());

        layout.setOverlayContainment(
                MasterDetailLayout.OverlayContainment.VIEWPORT);

        Assert.assertEquals(MasterDetailLayout.OverlayContainment.VIEWPORT,
                layout.getOverlayContainment());
        Assert.assertEquals("viewport",
                layout.getElement().getProperty("overlayContainment"));
    }

    @Test(expected = NullPointerException.class)
    public void setOverlayContainmentNull_throws() {
        layout.setOverlayContainment(null);
    }

    @Test
    public void setExpand_getExpand() {
        Assert.assertEquals(MasterDetailLayout.Expand.BOTH, layout.getExpand());

        layout.setExpand(MasterDetailLayout.Expand.MASTER);

        Assert.assertEquals(MasterDetailLayout.Expand.MASTER,
                layout.getExpand());
        Assert.assertEquals("master",
                layout.getElement().getProperty("expand"));

        layout.setExpand(MasterDetailLayout.Expand.DETAIL);

        Assert.assertEquals(MasterDetailLayout.Expand.DETAIL,
                layout.getExpand());
        Assert.assertEquals("detail",
                layout.getElement().getProperty("expand"));
    }

    @Test(expected = NullPointerException.class)
    public void setExpandNull_throws() {
        layout.setExpand(null);
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
    public void addBackdropClickListener_verifyListenerTriggered() {
        @SuppressWarnings("unchecked")
        ComponentEventListener<MasterDetailLayout.BackdropClickEvent> listener = Mockito
                .mock(ComponentEventListener.class);

        layout.addBackdropClickListener(listener);

        MasterDetailLayout.BackdropClickEvent backdropClickEvent = new MasterDetailLayout.BackdropClickEvent(
                layout, true);
        ComponentUtil.fireEvent(layout, backdropClickEvent);

        Mockito.verify(listener).onComponentEvent(backdropClickEvent);
    }

    @Test
    public void addDetailEscapePressListener_verifyListenerTriggered() {
        @SuppressWarnings("unchecked")
        ComponentEventListener<MasterDetailLayout.DetailEscapePressEvent> listener = Mockito
                .mock(ComponentEventListener.class);

        layout.addDetailEscapePressListener(listener);

        MasterDetailLayout.DetailEscapePressEvent detailEscapePressEvent = new MasterDetailLayout.DetailEscapePressEvent(
                layout, true);
        ComponentUtil.fireEvent(layout, detailEscapePressEvent);

        Mockito.verify(listener).onComponentEvent(detailEscapePressEvent);
    }

    @Test
    public void implementsHasThemeVariant() {
        Assert.assertTrue(HasThemeVariant.class
                .isAssignableFrom(MasterDetailLayout.class));
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

    private void assertSetDetailCall(Component component,
            boolean skipTransition) {
        ui.fakeClientCommunication();
        var pendingJavaScriptInvocations = ui
                .dumpPendingJavaScriptInvocations();

        Assert.assertEquals(1, pendingJavaScriptInvocations.size());

        var pendingJavaScriptInvocation = pendingJavaScriptInvocations.get(0);
        var parameters = pendingJavaScriptInvocation.getInvocation()
                .getParameters();
        var element = component != null ? component.getElement() : null;

        Assert.assertEquals(
                "return (async function() { this._setDetail($0, $1)}).apply($2)",
                pendingJavaScriptInvocation.getInvocation().getExpression());
        Assert.assertEquals(element, parameters.get(0));
        Assert.assertEquals(skipTransition, parameters.get(1));
    }
}
