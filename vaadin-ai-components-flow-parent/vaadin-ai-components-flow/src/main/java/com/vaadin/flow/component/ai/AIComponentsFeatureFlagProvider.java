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
package com.vaadin.flow.component.ai;

import java.util.List;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlagProvider;

/**
 * Provides the AI components feature flag for AI-related features such as
 * AIOrchestrator, modular upload components, and MessageListItem attachments.
 *
 * @author Vaadin Ltd
 */
public class AIComponentsFeatureFlagProvider implements FeatureFlagProvider {

    /**
     * The feature flag ID for AI components.
     */
    public static final String FEATURE_FLAG_ID = "aiComponents";

    /**
     * The AI components feature flag. When enabled, allows use of AI-related
     * features including AIOrchestrator, modular upload components, and
     * MessageListItem attachments.
     */
    public static final Feature AI_COMPONENTS = new Feature("AI Components", // title
            FEATURE_FLAG_ID, // id
            null, // moreInfoLink
            false, // requiresServerRestart
            null); // componentClassName

    @Override
    public List<Feature> getFeatures() {
        return List.of(AI_COMPONENTS);
    }
}
