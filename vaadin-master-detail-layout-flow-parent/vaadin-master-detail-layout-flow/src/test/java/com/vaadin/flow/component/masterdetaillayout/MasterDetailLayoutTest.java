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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class MasterDetailLayoutTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT);

    private final MasterDetailLayout layout = new MasterDetailLayout();

    @BeforeEach
    void setup() {
        ui.add(layout);
    }

    @Test
    void constructorWithSizesAndExpand() {
        var customLayout = new MasterDetailLayout("400px", "200px",
                MasterDetailLayout.ExpandingArea.MASTER);
        ui.add(customLayout);

        Assertions.assertEquals("400px", customLayout.getMasterSize());
        Assertions.assertEquals("200px", customLayout.getDetailSize());
        Assertions.assertEquals(MasterDetailLayout.ExpandingArea.MASTER,
                customLayout.getExpandingArea());
    }

    @Test
    void constructorWithSizesUnitsAndExpandingArea() {
        var customLayout = new MasterDetailLayout(30, Unit.EM, 15, Unit.EM,
                MasterDetailLayout.ExpandingArea.DETAIL);
        ui.add(customLayout);

        Assertions.assertEquals("30.0em", customLayout.getMasterSize());
        Assertions.assertEquals("15.0em", customLayout.getDetailSize());
        Assertions.assertEquals(MasterDetailLayout.ExpandingArea.DETAIL,
                customLayout.getExpandingArea());
    }

    @Test
    void setMaster() {
        var master = new Div();
        layout.setMaster(master);

        assertMasterContent(master);
    }

    @Test
    void getMaster() {
        var master = new Div();
        layout.setMaster(master);

        Assertions.assertEquals(master, layout.getMaster());
    }

    @Test
    void setMaster_replaceMaster() {
        var master = new Div();
        layout.setMaster(master);

        var newMaster = new Div();
        layout.setMaster(newMaster);

        assertMasterContent(newMaster);
        Assertions.assertNull(master.getElement().getParent());
        Assertions.assertNull(master.getElement().getAttribute("slot"));
    }

    @Test
    void setMasterNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.setMaster(null));
    }

    @Test
    void setMasterText_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> layout.setMaster(new Text("master")));
    }

    @Test
    void setDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        assertDetailContent(detail);
        assertSetDetailCall(detail, true);
    }

    @Test
    void getDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        Assertions.assertEquals(detail, layout.getDetail());
    }

    @Test
    void setDetail_replaceDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        var newDetail = new Div();
        layout.setDetail(newDetail);

        assertDetailContent(newDetail);
        assertSetDetailCall(newDetail, true);
        Assertions.assertEquals(newDetail, layout.getDetail());
        Assertions.assertNull(detail.getElement().getParent());
    }

    @Test
    void setDetail_removeDetail() {
        var detail = new Div();
        layout.setDetail(detail);

        layout.setDetail(null);

        assertSetDetailCall(null, true);
        Assertions.assertNull(layout.getDetail());
        Assertions.assertNull(detail.getElement().getParent());
    }

    @Test
    void setDetail_detach_attach() {
        var detail = new Div();
        layout.setDetail(detail);
        assertSetDetailCall(detail, true);

        ui.remove(layout);
        ui.add(layout);

        assertSetDetailCall(detail, true);
    }

    @Test
    void setDetail_usesViewTransitionAfterFirstRoundtrip() {
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

    @Test
    void setDetailText_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> layout.setDetail(new Text("detail")));
    }

    @Test
    void setDetailPlaceholder() {
        var placeholder = new Div();
        layout.setDetailPlaceholder(placeholder);

        Assertions.assertEquals("detail-placeholder",
                placeholder.getElement().getAttribute("slot"));
        Assertions.assertEquals(layout.getElement(),
                placeholder.getElement().getParent());
    }

    @Test
    void getDetailPlaceholder() {
        var placeholder = new Div();
        layout.setDetailPlaceholder(placeholder);

        Assertions.assertEquals(placeholder, layout.getDetailPlaceholder());
    }

    @Test
    void setDetailPlaceholder_replacePlaceholder() {
        var placeholder = new Div();
        layout.setDetailPlaceholder(placeholder);

        var newPlaceholder = new Div();
        layout.setDetailPlaceholder(newPlaceholder);

        Assertions.assertEquals(newPlaceholder, layout.getDetailPlaceholder());
        Assertions.assertNull(placeholder.getElement().getParent());
    }

    @Test
    void setDetailPlaceholderNull_removesPlaceholder() {
        var placeholder = new Div();
        layout.setDetailPlaceholder(placeholder);

        layout.setDetailPlaceholder(null);

        Assertions.assertNull(layout.getDetailPlaceholder());
        Assertions.assertNull(placeholder.getElement().getParent());
    }

    @Test
    void showRouterLayoutContent() {
        var detail = new Div();
        layout.showRouterLayoutContent(detail);

        assertDetailContent(detail);
        assertSetDetailCall(detail, true);
        Assertions.assertEquals(detail, layout.getDetail());
    }

    @Test
    void updateRouterLayoutContent() {
        var detail = new Div();
        layout.showRouterLayoutContent(detail);

        var newDetail = new Div();
        layout.removeRouterLayoutContent(detail);
        layout.showRouterLayoutContent(newDetail);

        assertDetailContent(newDetail);
        assertSetDetailCall(newDetail, true);
        Assertions.assertEquals(newDetail, layout.getDetail());
        Assertions.assertNull(detail.getElement().getParent());

        layout.removeRouterLayoutContent(newDetail);

        assertSetDetailCall(null, false);
        Assertions.assertNull(layout.getDetail());
        Assertions.assertNull(newDetail.getElement().getParent());
    }

    @Test
    void setMasterSize_getMasterSize() {
        String size = "300px";
        layout.setMasterSize(size);
        Assertions.assertEquals(size, layout.getMasterSize());
        Assertions.assertEquals(size,
                layout.getElement().getProperty("masterSize"));
    }

    @Test
    void setMasterSizeWithUnit_getMasterSize() {
        layout.setMasterSize(30, Unit.EM);
        Assertions.assertEquals("30.0em", layout.getMasterSize());
        Assertions.assertEquals("30.0em",
                layout.getElement().getProperty("masterSize"));
    }

    @Test
    void setDetailSize_getDetailSize() {
        String size = "400px";
        layout.setDetailSize(size);
        Assertions.assertEquals(size, layout.getDetailSize());
        Assertions.assertEquals(size,
                layout.getElement().getProperty("detailSize"));
    }

    @Test
    void setDetailSizeWithUnit_getDetailSize() {
        layout.setDetailSize(30, Unit.EM);
        Assertions.assertEquals("30.0em", layout.getDetailSize());
        Assertions.assertEquals("30.0em",
                layout.getElement().getProperty("detailSize"));
    }

    @Test
    void setOverlaySize_getOverlaySize() {
        String size = "500px";
        layout.setOverlaySize(size);
        Assertions.assertEquals(size, layout.getOverlaySize());
        Assertions.assertEquals(size,
                layout.getElement().getProperty("overlaySize"));
    }

    @Test
    void setOverlaySize_resetToNull() {
        layout.setOverlaySize("500px");
        layout.setOverlaySize(null);
        Assertions.assertNull(layout.getOverlaySize());
    }

    @Test
    void setOverlaySizeWithUnit_getOverlaySize() {
        layout.setOverlaySize(100, Unit.PERCENTAGE);
        Assertions.assertEquals("100.0%", layout.getOverlaySize());
        Assertions.assertEquals("100.0%",
                layout.getElement().getProperty("overlaySize"));
    }

    @Test
    void setOrientation_getOrientation() {
        Assertions.assertEquals(MasterDetailLayout.Orientation.HORIZONTAL,
                layout.getOrientation());

        layout.setOrientation(MasterDetailLayout.Orientation.VERTICAL);

        Assertions.assertEquals(MasterDetailLayout.Orientation.VERTICAL,
                layout.getOrientation());
        Assertions.assertEquals("vertical",
                layout.getElement().getProperty("orientation"));
    }

    @Test
    void setOrientationNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.setOrientation(null));
    }

    @Test
    void setOverlayContainment_getOverlayContainment() {
        Assertions.assertEquals(MasterDetailLayout.OverlayContainment.LAYOUT,
                layout.getOverlayContainment());

        layout.setOverlayContainment(
                MasterDetailLayout.OverlayContainment.VIEWPORT);

        Assertions.assertEquals(MasterDetailLayout.OverlayContainment.VIEWPORT,
                layout.getOverlayContainment());
        Assertions.assertEquals("viewport",
                layout.getElement().getProperty("overlayContainment"));
    }

    @Test
    void setOverlayContainmentNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.setOverlayContainment(null));
    }

    @Test
    void setExpandingArea_getExpandingArea() {
        Assertions.assertEquals(MasterDetailLayout.ExpandingArea.BOTH,
                layout.getExpandingArea());

        layout.setExpandingArea(MasterDetailLayout.ExpandingArea.MASTER);

        Assertions.assertEquals(MasterDetailLayout.ExpandingArea.MASTER,
                layout.getExpandingArea());
        Assertions.assertEquals("master",
                layout.getElement().getProperty("expand"));

        layout.setExpandingArea(MasterDetailLayout.ExpandingArea.DETAIL);

        Assertions.assertEquals(MasterDetailLayout.ExpandingArea.DETAIL,
                layout.getExpandingArea());
        Assertions.assertEquals("detail",
                layout.getElement().getProperty("expand"));
    }

    @Test
    void setExpandingAreaNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.setExpandingArea(null));
    }

    @Test
    void setAnimationEnabled_isAnimationEnabled() {
        Assertions.assertTrue(layout.isAnimationEnabled());
        Assertions.assertFalse(
                layout.getElement().getProperty("noAnimation", false));

        layout.setAnimationEnabled(false);

        Assertions.assertFalse(layout.isAnimationEnabled());
        Assertions.assertTrue(
                layout.getElement().getProperty("noAnimation", false));
    }

    @Test
    void addBackdropClickListener_verifyListenerTriggered() {
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
    void addDetailEscapePressListener_verifyListenerTriggered() {
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
    void implementsHasThemeVariant() {
        Assertions.assertTrue(HasThemeVariant.class
                .isAssignableFrom(MasterDetailLayout.class));
    }

    private void assertMasterContent(Component component) {
        Assertions.assertEquals("",
                component.getElement().getAttribute("slot"));
        Assertions.assertEquals(layout.getElement(),
                component.getElement().getParent());
    }

    private void assertDetailContent(Component component) {
        Assertions.assertEquals(layout.getElement(),
                component.getElement().getParent());
        Assertions.assertTrue(component.getElement().isVirtualChild());
    }

    private void assertSetDetailCall(Component component,
            boolean skipTransition) {
        ui.fakeClientCommunication();
        var pendingJavaScriptInvocations = ui
                .dumpPendingJavaScriptInvocations();

        Assertions.assertEquals(1, pendingJavaScriptInvocations.size());

        var pendingJavaScriptInvocation = pendingJavaScriptInvocations.get(0);
        var parameters = pendingJavaScriptInvocation.getInvocation()
                .getParameters();
        var element = component != null ? component.getElement() : null;

        Assertions.assertEquals(
                "return (async function() { this._setDetail($0, $1)}).apply($2)",
                pendingJavaScriptInvocation.getInvocation().getExpression());
        Assertions.assertEquals(element, parameters.get(0));
        Assertions.assertEquals(skipTransition, parameters.get(1));
    }
}
