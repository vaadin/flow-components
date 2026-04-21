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

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.Registration;

/**
 * MasterDetailLayout is a component for building UIs with a master (or primary)
 * area and a detail (or secondary) area that is displayed next to, or overlaid
 * on top of, the master area, depending on configuration and viewport size.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-master-detail-layout")
@NpmPackage(value = "@vaadin/master-detail-layout", version = "25.2.0-alpha8")
@JsModule("@vaadin/master-detail-layout/src/vaadin-master-detail-layout.js")
public class MasterDetailLayout extends Component
        implements HasSize, RouterLayout {

    public static final String MASTER_SLOT = "";
    public static final String DETAIL_PLACEHOLDER_SLOT = "detail-placeholder";

    private HasElement detail;
    private PendingJavaScriptResult pendingDetailsUpdate;
    private boolean hasInitialized = false;

    /**
     * Supported orientation values for {@link MasterDetailLayout}.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    /**
     * Supported overlay containment values for {@link MasterDetailLayout}.
     */
    public enum OverlayContainment {
        LAYOUT, PAGE
    }

    /**
     * Creates an empty Master Detail Layout.
     */
    public MasterDetailLayout() {
    }

    /**
     * Creates a Master Detail Layout with the given master and detail sizes.
     *
     * @param masterSize
     *            the size of the master area in CSS length units
     * @param detailSize
     *            the size of the detail area in CSS length units
     */
    public MasterDetailLayout(String masterSize, String detailSize) {
        setMasterSize(masterSize);
        setDetailSize(detailSize);
    }

    /**
     * Creates a Master Detail Layout with the given master and detail sizes.
     *
     * @param masterSize
     *            the size of the master area
     * @param masterUnit
     *            the unit for the master size
     * @param detailSize
     *            the size of the detail area
     * @param detailUnit
     *            the unit for the detail size
     */
    public MasterDetailLayout(float masterSize, Unit masterUnit,
            float detailSize, Unit detailUnit) {
        setMasterSize(masterSize, masterUnit);
        setDetailSize(detailSize, detailUnit);
    }

    /**
     * Gets the component currently in the master area.
     *
     * @return the component in the master area, or {@code null} if there is no
     *         component in the master area
     */
    public Component getMaster() {
        return SlotUtils.getElementsInSlot(this, "").findFirst()
                .flatMap(Element::getComponent).orElse(null);
    }

    /**
     * Sets the component to be displayed in the master area.
     *
     * @param component
     *            the component to display in the master area, not {@code null}
     */
    public void setMaster(Component component) {
        Objects.requireNonNull(component, "Master component cannot be null");
        SlotUtils.clearSlot(this, MASTER_SLOT);
        SlotUtils.addToSlot(this, MASTER_SLOT, component);
    }

    /**
     * Gets the component currently in the detail area.
     *
     * @return the component in the detail area, or {@code null} if there is no
     *         component in the detail area
     */
    public Component getDetail() {
        return Optional.ofNullable(detail)
                .flatMap(hasElement -> hasElement.getElement().getComponent())
                .orElse(null);
    }

    /**
     * Sets the component to be displayed in the detail area. The details area
     * is automatically shown when the detail component is set, and hidden when
     * the detail component is removed by setting it to {@code null}.
     *
     * @param component
     *            the component to display in the detail area, or {@code null}
     *            to clear the detail area
     */
    public void setDetail(Component component) {
        doSetDetail(component);
    }

    /**
     * Gets the component currently in the detail placeholder area.
     *
     * @return the component in the detail placeholder area, or {@code null} if
     *         there is no component in the detail placeholder area
     */
    public Component getDetailPlaceholder() {
        return SlotUtils.getElementsInSlot(this, DETAIL_PLACEHOLDER_SLOT)
                .findFirst().flatMap(Element::getComponent).orElse(null);
    }

    /**
     * Sets the component to be displayed in the detail placeholder area. The
     * placeholder is shown when no detail content is set, and is hidden when
     * the layout is in overlay mode. Unlike the detail content, the placeholder
     * does not become an overlay when there is not enough space.
     *
     * @param component
     *            the component to display in the detail placeholder area, or
     *            {@code null} to clear the detail placeholder area
     */
    public void setDetailPlaceholder(Component component) {
        SlotUtils.clearSlot(this, DETAIL_PLACEHOLDER_SLOT);
        if (component != null) {
            SlotUtils.addToSlot(this, DETAIL_PLACEHOLDER_SLOT, component);
        }
    }

    private void doSetDetail(HasElement hasElement) {
        if (hasElement != null && hasElement instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as a slot content is not supported. "
                            + "Consider wrapping the Text inside a Div.");
        }

        if (detail != null) {
            getElement().removeVirtualChild(detail.getElement());
        }
        detail = hasElement;
        if (detail != null) {
            getElement().appendVirtualChild(detail.getElement());
        }
        updateDetails();
    }

    private void updateDetails() {
        if (pendingDetailsUpdate != null) {
            pendingDetailsUpdate.cancelExecution();
        }
        boolean skipTransition = !hasInitialized;
        pendingDetailsUpdate = getElement().executeJs("this._setDetail($0, $1)",
                detail != null ? detail.getElement() : null, skipTransition);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
        updateDetails();
        attachEvent.getUI().beforeClientResponse(this, executionContext -> {
            this.hasInitialized = true;
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.hasInitialized = false;
    }

    /**
     * Gets the size of the master area.
     *
     * @return the size of the master area in CSS length units, or {@code null}
     *         if the size is not set
     */
    public String getMasterSize() {
        return getElement().getProperty("masterSize");
    }

    /**
     * Sets the size of the master area in CSS length units. If there is not
     * enough space to show master and detail areas next to each other, the
     * detail area is shown as an overlay. Defaults to 30em.
     *
     * @param size
     *            the size of the master area in CSS length units
     */
    public void setMasterSize(String size) {
        getElement().setProperty("masterSize", size);
    }

    /**
     * Sets the size of the master area in CSS length units. If there is not
     * enough space to show master and detail areas next to each other, the
     * detail area is shown as an overlay. Defaults to 30em.
     *
     * @param size
     *            the size of the master area
     * @param unit
     *            the unit
     */
    public void setMasterSize(float size, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        getElement().setProperty("masterSize", HasSize.getCssSize(size, unit));
    }

    /**
     * Sets the size of the master area in CSS length units and whether the
     * master area expands to fill available space.
     *
     * @param size
     *            the size of the master area in CSS length units
     * @param expand
     *            {@code true} to expand the master area, {@code false}
     *            otherwise
     * @see #setMasterSize(String)
     * @see #setExpandMaster(boolean)
     */
    public void setMasterSize(String size, boolean expand) {
        setMasterSize(size);
        setExpandMaster(expand);
    }

    /**
     * Sets the size of the master area in CSS length units and whether the
     * master area expands to fill available space.
     *
     * @param size
     *            the size of the master area
     * @param unit
     *            the unit
     * @param expand
     *            {@code true} to expand the master area, {@code false}
     *            otherwise
     * @see #setMasterSize(float, Unit)
     * @see #setExpandMaster(boolean)
     */
    public void setMasterSize(float size, Unit unit, boolean expand) {
        setMasterSize(size, unit);
        setExpandMaster(expand);
    }

    /**
     * Gets the size of the detail area.
     *
     * @return the size of the detail area in CSS length units, or {@code null}
     *         if the size is not set
     */
    public String getDetailSize() {
        return getElement().getProperty("detailSize");
    }

    /**
     * Sets the size of the detail area in CSS length units. When there is not
     * enough space to show master and detail areas next to each other, the
     * detail area is shown as an overlay.
     * <p>
     * If not specified, the size is determined automatically by measuring the
     * detail content whenever new content is provided via {@link #setDetail}.
     * The measured intrinsic size is then cached until the next
     * {@link #setDetail} call.
     *
     * @param size
     *            the size of the detail area in CSS length units
     */
    public void setDetailSize(String size) {
        getElement().setProperty("detailSize", size);
    }

    /**
     * Sets the size of the detail area in CSS length units. When there is not
     * enough space to show master and detail areas next to each other, the
     * detail area is shown as an overlay.
     * <p>
     * If not specified, the size is determined automatically by measuring the
     * detail content whenever new content is provided via {@link #setDetail}.
     * The measured intrinsic size is then cached until the next
     * {@link #setDetail} call.
     *
     * @param size
     *            the size of the detail area
     * @param unit
     *            the unit
     */
    public void setDetailSize(float size, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        getElement().setProperty("detailSize", HasSize.getCssSize(size, unit));
    }

    /**
     * Sets the size of the detail area in CSS length units and whether the
     * detail area expands to fill available space.
     *
     * @param size
     *            the size of the detail area in CSS length units
     * @param expand
     *            {@code true} to expand the detail area, {@code false}
     *            otherwise
     * @see #setDetailSize(String)
     * @see #setExpandDetail(boolean)
     */
    public void setDetailSize(String size, boolean expand) {
        setDetailSize(size);
        setExpandDetail(expand);
    }

    /**
     * Sets the size of the detail area in CSS length units and whether the
     * detail area expands to fill available space.
     *
     * @param size
     *            the size of the detail area
     * @param unit
     *            the unit
     * @param expand
     *            {@code true} to expand the detail area, {@code false}
     *            otherwise
     * @see #setDetailSize(float, Unit)
     * @see #setExpandDetail(boolean)
     */
    public void setDetailSize(float size, Unit unit, boolean expand) {
        setDetailSize(size, unit);
        setExpandDetail(expand);
    }

    /**
     * Sets the size of the detail area and the size of the detail area when
     * shown as an overlay, both in CSS length units.
     *
     * @param size
     *            the size of the detail area in CSS length units
     * @param overlaySize
     *            the overlay size in CSS length units
     * @see #setDetailSize(String)
     * @see #setOverlaySize(String)
     */
    public void setDetailSize(String size, String overlaySize) {
        setDetailSize(size);
        setOverlaySize(overlaySize);
    }

    /**
     * Sets the size of the detail area and the size of the detail area when
     * shown as an overlay, both in CSS length units.
     *
     * @param size
     *            the size of the detail area
     * @param unit
     *            the unit for the detail size
     * @param overlaySize
     *            the overlay size
     * @param overlayUnit
     *            the unit for the overlay size
     * @see #setDetailSize(float, Unit)
     * @see #setOverlaySize(float, Unit)
     */
    public void setDetailSize(float size, Unit unit, float overlaySize,
            Unit overlayUnit) {
        setDetailSize(size, unit);
        setOverlaySize(overlaySize, overlayUnit);
    }

    /**
     * Sets the size of the detail area in CSS length units, whether the detail
     * area expands to fill available space, and the size of the detail area
     * when shown as an overlay.
     *
     * @param size
     *            the size of the detail area in CSS length units
     * @param expand
     *            {@code true} to expand the detail area, {@code false}
     *            otherwise
     * @param overlaySize
     *            the overlay size in CSS length units
     * @see #setDetailSize(String)
     * @see #setExpandDetail(boolean)
     * @see #setOverlaySize(String)
     */
    public void setDetailSize(String size, boolean expand, String overlaySize) {
        setDetailSize(size);
        setExpandDetail(expand);
        setOverlaySize(overlaySize);
    }

    /**
     * Sets the size of the detail area, whether the detail area expands to fill
     * available space, and the size of the detail area when shown as an
     * overlay.
     *
     * @param size
     *            the size of the detail area
     * @param unit
     *            the unit for the detail size
     * @param expand
     *            {@code true} to expand the detail area, {@code false}
     *            otherwise
     * @param overlaySize
     *            the overlay size
     * @param overlayUnit
     *            the unit for the overlay size
     * @see #setDetailSize(float, Unit)
     * @see #setExpandDetail(boolean)
     * @see #setOverlaySize(float, Unit)
     */
    public void setDetailSize(float size, Unit unit, boolean expand,
            float overlaySize, Unit overlayUnit) {
        setDetailSize(size, unit);
        setExpandDetail(expand);
        setOverlaySize(overlaySize, overlayUnit);
    }

    /**
     * Gets the orientation of the layout. Defaults to
     * {@link Orientation#HORIZONTAL}.
     *
     * @return the orientation
     */
    public Orientation getOrientation() {
        String orientation = getElement().getProperty("orientation");
        if (orientation != null) {
            return Orientation.valueOf(orientation.toUpperCase());
        }
        return Orientation.HORIZONTAL;
    }

    /**
     * Sets the orientation of the layout. Defines how master and detail areas
     * are shown next to each other, and whether size is applied as width or
     * height.
     *
     * @param orientation
     *            the orientation
     */
    public void setOrientation(Orientation orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        getElement().setProperty("orientation",
                orientation.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the overlay containment of the layout. Defaults to
     * {@link OverlayContainment#LAYOUT}.
     *
     * @return the overlay containment
     */
    public OverlayContainment getOverlayContainment() {
        String overlayContainment = getElement()
                .getProperty("overlayContainment");
        if (overlayContainment != null) {
            return OverlayContainment.valueOf(overlayContainment.toUpperCase());
        }
        return OverlayContainment.LAYOUT;
    }

    /**
     * Sets the containment of the detail area when the layout is in overlay
     * mode. When set to {@link OverlayContainment#LAYOUT}, the overlay is
     * confined to the layout. When set to {@link OverlayContainment#PAGE}, the
     * overlay is confined to the browser's viewport. Defaults to
     * {@link OverlayContainment#LAYOUT}.
     *
     * @param overlayContainment
     *            the overlay containment
     */
    public void setOverlayContainment(OverlayContainment overlayContainment) {
        Objects.requireNonNull(overlayContainment,
                "OverlayContainment cannot be null");
        getElement().setProperty("overlayContainment",
                overlayContainment.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the size of the detail area when shown as an overlay.
     *
     * @return the overlay size in CSS length units, or {@code null} if the
     *         overlay size is not set
     */
    public String getOverlaySize() {
        return getElement().getProperty("overlaySize");
    }

    /**
     * Sets the size of the detail area when shown as an overlay. When not set,
     * falls back to the detail size. Set to {@code "100%"} to make the detail
     * cover the full layout.
     *
     * @param size
     *            the overlay size in CSS length units
     */
    public void setOverlaySize(String size) {
        getElement().setProperty("overlaySize", size);
    }

    /**
     * Sets the size of the detail area when shown as an overlay. When not set,
     * falls back to the detail size. Set to {@code 100} with {@link Unit#PCT}
     * to make the detail cover the full layout.
     *
     * @param size
     *            the overlay size
     * @param unit
     *            the unit
     */
    public void setOverlaySize(float size, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        getElement().setProperty("overlaySize", HasSize.getCssSize(size, unit));
    }

    /**
     * Gets whether the master area expands to fill available space. Defaults to
     * {@code false}.
     *
     * @return {@code true} if the master area expands, {@code false} otherwise
     */
    public boolean isExpandMaster() {
        return getElement().getProperty("expandMaster", false);
    }

    /**
     * Sets whether the master area expands to fill available space. When both
     * {@link #setExpandMaster(boolean)} and {@link #setExpandDetail(boolean)}
     * are set to {@code true}, the master and detail areas share the available
     * space equally.
     *
     * @param expandMaster
     *            {@code true} to expand the master area, {@code false}
     *            otherwise
     */
    public void setExpandMaster(boolean expandMaster) {
        getElement().setProperty("expandMaster", expandMaster);
    }

    /**
     * Gets whether the detail area expands to fill available space. Defaults to
     * {@code false}.
     *
     * @return {@code true} if the detail area expands, {@code false} otherwise
     */
    public boolean isExpandDetail() {
        return getElement().getProperty("expandDetail", false);
    }

    /**
     * Sets whether the detail area expands to fill available space. When both
     * {@link #setExpandMaster(boolean)} and {@link #setExpandDetail(boolean)}
     * are set to {@code true}, the master and detail areas share the available
     * space equally.
     *
     * @param expandDetail
     *            {@code true} to expand the detail area, {@code false}
     *            otherwise
     */
    public void setExpandDetail(boolean expandDetail) {
        getElement().setProperty("expandDetail", expandDetail);
    }

    /**
     * Gets whether the layout forces the detail area to be shown as an overlay,
     * even if there is enough space for master and detail to be shown next to
     * each other using the default (split) mode.
     *
     * @return {@code true} if the overlay mode is enforced, {@code false}
     *         otherwise
     */
    public boolean isForceOverlay() {
        return getElement().getProperty("forceOverlay", false);
    }

    /**
     * Sets whether the layout forces the detail area to be shown as an overlay,
     * even if there is enough space for master and detail to be shown next to
     * each other using the default (split) mode.
     *
     * @param forceOverlay
     *            {@code true} if the overlay mode is enforced, {@code false}
     *            otherwise
     */
    public void setForceOverlay(boolean forceOverlay) {
        getElement().setProperty("forceOverlay", forceOverlay);
    }

    /**
     * Gets whether the layout animation is enabled.
     *
     * @return {@code true} if the animation is enabled, {@code false} otherwise
     */
    public boolean isAnimationEnabled() {
        return !getElement().getProperty("noAnimation", false);
    }

    /**
     * Sets whether the layout animation is enabled.
     *
     * @param enabled
     *            {@code true} if the animation is enabled, {@code false}
     *            otherwise
     */
    public void setAnimationEnabled(boolean enabled) {
        getElement().setProperty("noAnimation", !enabled);
    }

    /**
     * Adds a listener for when the backdrop of the details overlay is clicked.
     * The backdrop is the area outside the detail area when it is shown in
     * overlay mode. Can be used to hide the details when the backdrop is
     * clicked.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addBackdropClickListener(
            ComponentEventListener<BackdropClickEvent> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        return ComponentUtil.addListener(this, BackdropClickEvent.class,
                listener);
    }

    /**
     * Adds a listener for when the Escape key is pressed within the details
     * area. Can be used to hide the details when the Escape key is pressed.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addDetailEscapePressListener(
            ComponentEventListener<DetailEscapePressEvent> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        return ComponentUtil.addListener(this, DetailEscapePressEvent.class,
                listener);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        doSetDetail(content);
    }

    @Override
    public void removeRouterLayoutContent(HasElement oldContent) {
        doSetDetail(null);
    }

    /**
     * Checks whether the Master Detail Layout component feature flag is active.
     * Succeeds if the flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#MASTER_DETAIL_LAYOUT_COMPONENT}
     *             feature is not enabled
     */
    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        boolean enabled = featureFlags
                .isEnabled(FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Event that is fired when the backdrop of the details overlay is clicked.
     * The backdrop is the area outside the detail area when it is shown in
     * overlay mode. Can be used to hide the details when the backdrop is
     * clicked.
     */
    @DomEvent("backdrop-click")
    public static class BackdropClickEvent
            extends ComponentEvent<MasterDetailLayout> {
        public BackdropClickEvent(MasterDetailLayout source,
                boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event that is fired when the Escape key is pressed within the details
     * area. Can be used to hide the details when the Escape key is pressed.
     */
    @DomEvent("detail-escape-press")
    public static class DetailEscapePressEvent
            extends ComponentEvent<MasterDetailLayout> {
        public DetailEscapePressEvent(MasterDetailLayout source,
                boolean fromClient) {
            super(source, fromClient);
        }
    }
}
