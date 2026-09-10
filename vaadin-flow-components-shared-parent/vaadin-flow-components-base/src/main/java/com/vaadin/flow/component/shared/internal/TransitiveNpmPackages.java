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
 * The annotations are only read to generate the versions file this module
 * ships, which is what pins the packages. The class is not a component and is
 * not scanned for frontend dependencies, so it does not add any of them to the
 * {@code package.json} of an application.
 * <p>
 * Packages that are not released with the components, such as
 * {@code @vaadin/router} or {@code @vaadin/vaadin-usage-statistics}, have
 * versions of their own and are left to the platform to pin.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@NpmPackage(value = "@vaadin/a11y-base", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/component-base", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/field-base", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/input-container", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/lit-renderer", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/overlay", version = "25.3.0-beta2")
final class TransitiveNpmPackages {

    private TransitiveNpmPackages() {
    }
}
