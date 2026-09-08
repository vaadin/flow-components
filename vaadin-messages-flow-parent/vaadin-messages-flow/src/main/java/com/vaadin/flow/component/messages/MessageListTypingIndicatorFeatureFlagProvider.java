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
package com.vaadin.flow.component.messages;

import java.util.List;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlagProvider;

/**
 * Provides the Message List typing indicator feature flag, gating the
 * experimental typing indicator API of {@link MessageList}.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public class MessageListTypingIndicatorFeatureFlagProvider
        implements FeatureFlagProvider {

    /**
     * The id of the Message List typing indicator feature flag.
     */
    public static final String FEATURE_FLAG_ID = "messageListTypingIndicator";

    /**
     * The Message List typing indicator feature flag. When enabled, allows use
     * of the experimental typing indicator API of {@link MessageList}.
     */
    public static final Feature TYPING_INDICATOR = new Feature(
            "Message List typing indicator", // title
            FEATURE_FLAG_ID, // id
            "https://vaadin.com/docs/latest/components/message-list", // moreInfoLink
            false, // requiresServerRestart
            null); // componentClassName

    @Override
    public List<Feature> getFeatures() {
        return List.of(TYPING_INDICATOR);
    }
}
