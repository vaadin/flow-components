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
package com.vaadin.flow.component.breadcrumbs;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponentsOfType;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Breadcrumbs is a component for displaying a navigation trail that shows the
 * user's location within a hierarchy of pages.
 * <p>
 * This component is experimental and needs to be enabled with the
 * {@code com.vaadin.experimental.breadcrumbsComponent} feature flag.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-breadcrumbs")
@NpmPackage(value = "@vaadin/breadcrumbs", version = "25.2.0-beta1")
@JsModule("@vaadin/breadcrumbs/src/vaadin-breadcrumbs.js")
public class Breadcrumbs extends Component implements HasSize, HasStyle,
        HasAriaLabel, HasComponentsOfType<BreadcrumbsItem> {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        if (!featureFlags.isEnabled(
                BreadcrumbsFeatureFlagProvider.BREADCRUMBS_COMPONENT)) {
            throw new ExperimentalFeatureException();
        }
    }
}
