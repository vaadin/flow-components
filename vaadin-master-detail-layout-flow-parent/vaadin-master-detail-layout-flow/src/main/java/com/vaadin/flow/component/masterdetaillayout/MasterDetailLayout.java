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

import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha3")
@NpmPackage(value = "@vaadin/master-detail-layout", version = "24.8.0-alpha3")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/master-detail-layout/src/vaadin-master-detail-layout.js")
public class MasterDetailLayout extends Component
        implements HasSize, RouterLayout {

    public static final String MASTER_SLOT = "";

    private HasElement detail;
    private PendingJavaScriptResult pendingDetailsUpdate;

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
        pendingDetailsUpdate = getElement().executeJs("this.setDetail($0)",
                detail != null ? detail.getElement() : null);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        updateDetails();
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
     * layout switches to the overlay mode.
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
     * layout switches to the overlay mode.
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
     * other, the layout switches to the overlay mode.
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
     * other, the layout switches to the overlay mode.
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
     * layout switches to the overlay mode.
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
     * layout switches to the overlay mode.
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
     * other, the layout switches to the overlay mode.
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
     * other, the layout switches to the overlay mode.
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

    @Override
    public void showRouterLayoutContent(HasElement content) {
        doSetDetail(content);
    }

    @Override
    public void removeRouterLayoutContent(HasElement oldContent) {
        doSetDetail(null);
    }
}
