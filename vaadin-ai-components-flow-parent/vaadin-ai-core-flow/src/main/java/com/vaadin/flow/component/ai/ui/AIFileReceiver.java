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
package com.vaadin.flow.component.ai.ui;

import java.io.Serializable;
import java.util.List;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;

/**
 * Interface for file upload components that are used in an AI conversation.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public interface AIFileReceiver extends Serializable {

    /**
     * Returns all accumulated attachments and clears the internal state.
     * <p>
     * After this method returns, the receiver is ready to accept new files. The
     * UI file list is also cleared.
     *
     * @return an unmodifiable list of attachments, or an empty list if none are
     *         pending
     */
    List<AIAttachment> takeAttachments();

    /**
     * Returns whether a file is still being uploaded to this receiver. While
     * this returns {@code true}, the {@link AIOrchestrator} ignores prompts
     * that would take the attachments from this receiver, so that the file is
     * not left out of the prompt. The default implementation returns
     * {@code false}.
     *
     * @return {@code true} if a file is being uploaded, {@code false} otherwise
     * @since 25.3
     */
    default boolean isUploading() {
        return false;
    }
}
