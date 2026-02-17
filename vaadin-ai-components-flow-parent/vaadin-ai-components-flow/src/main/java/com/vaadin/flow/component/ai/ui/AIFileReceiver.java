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

/**
 * Interface for file upload components that are used in an AI conversation.
 *
 * @author Vaadin Ltd
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
}
