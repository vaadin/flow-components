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
package com.vaadin.flow.component.shared;

/**
 * An exception which is thrown when somebody attempts to use AI component
 * features without activating the associated feature flag first.
 * <p>
 * AI component features include modular upload components (UploadManager,
 * UploadButton, UploadFileList), MessageListItem attachments, and
 * AiOrchestrator.
 *
 * @author Vaadin Ltd
 */
public class AiComponentsExperimentalFeatureException extends RuntimeException {

    /**
     * Creates a new exception with a default message.
     */
    public AiComponentsExperimentalFeatureException() {
        this(null, null);
    }

    /**
     * Creates a new exception with a message that includes the specific
     * component name, using the umbrella AI components feature flag.
     *
     * @param componentName
     *            the name of the component that requires the feature flag, or
     *            {@code null} for a generic message
     */
    public AiComponentsExperimentalFeatureException(String componentName) {
        this(componentName, null);
    }

    /**
     * Creates a new exception with a message that includes the specific
     * component name and feature flag ID. When a specific feature flag ID is
     * provided, the message will mention both the specific flag and the
     * umbrella AI components flag.
     *
     * @param componentName
     *            the name of the component that requires the feature flag, or
     *            {@code null} for a generic message
     * @param specificFeatureFlagId
     *            the specific feature flag ID that can enable this feature, or
     *            {@code null} to only mention the umbrella AI components flag
     */
    public AiComponentsExperimentalFeatureException(String componentName,
            String specificFeatureFlagId) {
        super(buildMessage(componentName, specificFeatureFlagId));
    }

    private static String buildMessage(String componentName,
            String specificFeatureFlagId) {
        String prefix = componentName != null
                ? "The " + componentName + " feature is"
                : "AI component features are";

        String flagInfo;
        if (specificFeatureFlagId != null) {
            flagInfo = "`com.vaadin.experimental." + specificFeatureFlagId
                    + "=true` or `com.vaadin.experimental."
                    + AiComponentsFeatureFlagProvider.FEATURE_FLAG_ID + "=true`";
        } else {
            flagInfo = "`com.vaadin.experimental."
                    + AiComponentsFeatureFlagProvider.FEATURE_FLAG_ID + "=true`";
        }

        return prefix + " currently experimental and needs to be explicitly "
                + "enabled. This feature can be enabled using Copilot, in the "
                + "experimental features tab, or by adding a "
                + "`src/main/resources/vaadin-featureflags.properties` file "
                + "with the following content: " + flagInfo;
    }
}
