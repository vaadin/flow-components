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
package com.vaadin.flow.component.shared.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Declares the npm packages that the Vaadin web components depend on and that
 * no Flow component declares itself, so that their versions are pinned along
 * with the components they are released with.
 * <p>
 * These are the base packages the web components are built on, released from
 * the same repository and at the same version as the components themselves. An
 * application never depends on them directly, it gets them through the web
 * components, but their versions still have to be pinned: without it, npm is
 * free to resolve a base package to a version the components were not built
 * against.
 * <p>
 * The annotations are read to generate the versions file this module ships,
 * which is what pins the packages. They are also picked up wherever Flow scans
 * the whole classpath for {@code @NpmPackage} instead of only the classes an
 * application reaches, as the development bundle build does, which adds the
 * packages to the {@code package.json} of the application. That is harmless:
 * the version is the one the web components depend on anyway, so it only writes
 * down what npm would install transitively.
 * <p>
 * Packages that are not released with the components, such as
 * {@code @vaadin/router} or {@code @vaadin/vaadin-usage-statistics}, have
 * versions of their own and are left to the platform to pin.
 * <p>
 * The class carries no state and is never instantiated. It is still
 * {@link Serializable}, as every class the components ship is expected to be.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@NpmPackage(value = "@vaadin/a11y-base", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/component-base", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/field-base", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/input-container", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/lit-renderer", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/overlay", version = "25.3.0-beta2")
final class TransitiveNpmPackages implements Serializable {

    /**
     * The npm packages of the React components, mapped to the packages they
     * bring themselves.
     * <p>
     * A React application installs the components through these instead of
     * installing the web components one by one, so the packages listed here are
     * left out of its {@code package.json} while their versions stay locked.
     * The versions file of this module declares them for the React mode, at the
     * version of the components, which the React components are released with.
     * <p>
     * They are not declared with {@link NpmPackage}, unlike the packages above:
     * that annotation adds the package to the {@code package.json} of every
     * application, whichever mode it uses, and a Lit application must not
     * install the React components.
     * <p>
     * The lists say which packages each of them brings today. Nothing derives
     * them from the components, so a component that is added has to be added
     * here as well.
     */
    static final Map<String, List<String>> REACT_COMPONENTS = Map.of(
            "@vaadin/react-components",
            List.of("@vaadin/accordion", "@vaadin/app-layout", "@vaadin/avatar",
                    "@vaadin/avatar-group", "@vaadin/badge",
                    "@vaadin/breadcrumbs", "@vaadin/button", "@vaadin/card",
                    "@vaadin/checkbox", "@vaadin/checkbox-group",
                    "@vaadin/combo-box", "@vaadin/component-base",
                    "@vaadin/confirm-dialog", "@vaadin/context-menu",
                    "@vaadin/custom-field", "@vaadin/date-picker",
                    "@vaadin/date-time-picker", "@vaadin/details",
                    "@vaadin/dialog", "@vaadin/email-field",
                    "@vaadin/field-highlighter", "@vaadin/form-layout",
                    "@vaadin/grid", "@vaadin/horizontal-layout", "@vaadin/icon",
                    "@vaadin/icons", "@vaadin/input-container",
                    "@vaadin/integer-field", "@vaadin/item", "@vaadin/list-box",
                    "@vaadin/lit-renderer", "@vaadin/login", "@vaadin/markdown",
                    "@vaadin/master-detail-layout", "@vaadin/menu-bar",
                    "@vaadin/message-input", "@vaadin/message-list",
                    "@vaadin/multi-select-combo-box", "@vaadin/notification",
                    "@vaadin/number-field", "@vaadin/overlay",
                    "@vaadin/password-field", "@vaadin/popover",
                    "@vaadin/progress-bar", "@vaadin/radio-group",
                    "@vaadin/scroller", "@vaadin/select", "@vaadin/side-nav",
                    "@vaadin/slider", "@vaadin/split-layout", "@vaadin/switch",
                    "@vaadin/tabs", "@vaadin/tabsheet", "@vaadin/text-area",
                    "@vaadin/text-field", "@vaadin/time-picker",
                    "@vaadin/tooltip", "@vaadin/upload",
                    "@vaadin/vertical-layout", "@vaadin/virtual-list"),
            "@vaadin/react-components-pro",
            List.of("@vaadin/board", "@vaadin/charts", "@vaadin/crud",
                    "@vaadin/dashboard", "@vaadin/grid-pro", "@vaadin/map",
                    "@vaadin/rich-text-editor"));

    private TransitiveNpmPackages() {
    }
}
