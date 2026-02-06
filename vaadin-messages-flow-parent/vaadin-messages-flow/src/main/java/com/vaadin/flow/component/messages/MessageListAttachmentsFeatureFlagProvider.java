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
 * Provides the MessageList attachments feature flag for enabling attachments in
 * MessageListItem.
 *
 * @author Vaadin Ltd
 */
public class MessageListAttachmentsFeatureFlagProvider
        implements FeatureFlagProvider {

    /**
     * The feature flag ID for MessageList attachments.
     */
    public static final String FEATURE_FLAG_ID = "messageListAttachments";

    /**
     * The MessageList attachments feature flag. When enabled, allows use of
     * attachments in MessageListItem.
     */
    public static final Feature MESSAGE_LIST_ATTACHMENTS = new Feature(
            "MessageList Attachments", // title
            FEATURE_FLAG_ID, // id
            null, // moreInfoLink
            false, // requiresServerRestart
            null); // componentClassName

    @Override
    public List<Feature> getFeatures() {
        return List.of(MESSAGE_LIST_ATTACHMENTS);
    }
}
