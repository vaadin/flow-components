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

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
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

/**
 * MasterDetailLayout is a component for building UIs with a master (or primary)
 * area and a detail (or secondary) area that is displayed next to, or overlaid
 * on top of, the master area, depending on configuration and viewport size.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-master-detail-layout")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@NpmPackage(value = "@vaadin/master-detail-layout", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/master-detail-layout/src/vaadin-master-detail-layout.js")
public class MasterDetailLayout extends Component
        implements HasSize, RouterLayout {

    public static final String MASTER_SLOT = "";

    private HasElement detail;
    private PendingJavaScriptResult pendingDetailsUpdate;
    private boolean hasInitialized = false;
    private OverlayMode overlayMode;

    /**
     * Supported orientation values for {@link MasterDetailLayout}.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    /**
     * Supported containment values for {@link MasterDetailLayout}.
     */
    public enum Containment {
        LAYOUT, VIEWPORT
    }

    /**
     * Supported overlay mode values for {@link MasterDetailLayout}.
     */
    public enum OverlayMode {
        DRAWER, STACK
    }

    /**
     * Creates an empty Master Detail Layout.
     */
    public MasterDetailLayout() {
        setOverlayMode(OverlayMode.DRAWER);
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
     * Sets the component to be displayed in the detail area.
     *
     * @param component
     *            the component to display in the detail area, or {@code null}
     *            to clear the detail area
     */
    public void setDetail(Component component) {
        doSetDetail(component);
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
     * Sets the size of the master area in CSS length units. When specified, it
     * prevents the master area from growing or shrinking. If there is not
     * enough space to show master and detail areas next to each other, the
     * details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
     *
     * @param size
     *            the size of the master area in CSS length units
     */
    public void setMasterSize(String size) {
        getElement().setProperty("masterSize", size);
    }

    /**
     * Sets the size of the master area in CSS length units. When specified, it
     * prevents the master area from growing or shrinking. If there is not
     * enough space to show master and detail areas next to each other, the
     * details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
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
     * Gets the minimum size of the master area.
     *
     * @return the minimum size of the master area in CSS length units, or
     *         {@code null} if the minimum size is not set
     */
    public String getMasterMinSize() {
        return getElement().getProperty("masterMinSize");
    }

    /**
     * Sets the minimum size of the master area in CSS length units. When
     * specified, it prevents the master area from shrinking below this size. If
     * there is not enough space to show master and detail areas next to each
     * other, the details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
     *
     * @param minSize
     *            the minimum size of the master area in CSS length units
     */
    public void setMasterMinSize(String minSize) {
        getElement().setProperty("masterMinSize", minSize);
    }

    /**
     * Sets the minimum size of the master area in CSS length units. When
     * specified, it prevents the master area from shrinking below this size. If
     * there is not enough space to show master and detail areas next to each
     * other, the details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
     *
     * @param minSize
     *            the minimum size of the master area
     * @param unit
     *            the unit
     */
    public void setMasterMinSize(float minSize, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        getElement().setProperty("masterMinSize",
                HasSize.getCssSize(minSize, unit));
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
     * Sets the size of the detail area in CSS length units. When specified, it
     * prevents the detail area from growing or shrinking. If there is not
     * enough space to show master and detail areas next to each other, the
     * details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
     *
     * @param size
     *            the size of the detail area in CSS length units
     */
    public void setDetailSize(String size) {
        getElement().setProperty("detailSize", size);
    }

    /**
     * Sets the size of the detail area in CSS length units. When specified, it
     * prevents the detail area from growing or shrinking. If there is not
     * enough space to show master and detail areas next to each other, the
     * details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
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
     * Gets the minimum size of the detail area.
     *
     * @return the minimum size of the detail area in CSS length units, or
     *         {@code null} if the minimum size is not set
     */
    public String getDetailMinSize() {
        return getElement().getProperty("detailMinSize");
    }

    /**
     * Sets the minimum size of the detail area in CSS length units. When
     * specified, it prevents the detail area from shrinking below this size. If
     * there is not enough space to show master and detail areas next to each
     * other, the details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
     *
     * @param minSize
     *            the minimum size of the detail area in CSS length units
     */
    public void setDetailMinSize(String minSize) {
        getElement().setProperty("detailMinSize", minSize);
    }

    /**
     * Sets the minimum size of the detail area in CSS length units. When
     * specified, it prevents the detail area from shrinking below this size. If
     * there is not enough space to show master and detail areas next to each
     * other, the details are shown in an overlay, using the mode defined by
     * {@link #setOverlayMode(OverlayMode)}.
     *
     * @param minSize
     *            the minimum size of the detail area
     * @param unit
     *            the unit
     */
    public void setDetailMinSize(float minSize, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        getElement().setProperty("detailMinSize",
                HasSize.getCssSize(minSize, unit));
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
     * are shown next to each other, and whether size and min-size are applied
     * as width or height.
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
     * Gets the containment of the layout. Defaults to
     * {@link Containment#LAYOUT}.
     *
     * @return the containment
     */
    public Containment getContainment() {
        String containment = getElement().getProperty("containment");
        if (containment != null) {
            return Containment.valueOf(containment.toUpperCase());
        }
        return Containment.LAYOUT;
    }

    /**
     * Sets the containment of the layout.When set to
     * {@link Containment#LAYOUT}, the overlay is confined to the layout. When
     * set to {@link Containment#VIEWPORT}, the overlay is confined to the
     * browser's viewport.
     *
     * @param containment
     *            the containment
     */
    public void setContainment(Containment containment) {
        Objects.requireNonNull(containment, "Containment cannot be null");
        getElement().setProperty("containment",
                containment.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the overlay mode of the layout. Defaults to
     * {@link OverlayMode#DRAWER}.
     *
     * @return the overlay mode
     */
    public OverlayMode getOverlayMode() {
        return overlayMode;
    }

    /**
     * Sets the overlay mode of the layout. When set to
     * {@link OverlayMode#DRAWER}, the detail area is positioned on top of
     * master area and there is a backdrop that covers the remaining part of the
     * master area. When set to {@link OverlayMode#STACK}, the detail area fully
     * covers the master area.
     *
     * @param mode
     *            the overlay mode
     */
    public void setOverlayMode(OverlayMode mode) {
        Objects.requireNonNull(mode, "OverlayMode cannot be null");
        overlayMode = mode;
        getElement().setProperty("stackOverlay", mode == OverlayMode.STACK);
    }

    /**
     * Gets whether the layout overlay mode is enforced. The way how the overlay
     * is rendered is defined by {@link #setOverlayMode(OverlayMode)}.
     *
     * @return {@code true} if the overlay mode is enforced, {@code false}
     *         otherwise
     */
    public boolean isForceOverlay() {
        return getElement().getProperty("forceOverlay", false);
    }

    /**
     * Sets whether the layout overlay mode is enforced. The way how the overlay
     * is rendered is defined by {@link #setOverlayMode(OverlayMode)}.
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
}
