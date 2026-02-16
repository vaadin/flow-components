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
package com.vaadin.flow.component.ai.orchestrator;

import java.util.function.Consumer;

import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Wrapper for Flow UploadManager component to implement AIFileReceiver
 * interface.
 */
record UploadManagerWrapper(
        UploadManager uploadManager) implements AIFileReceiver {

    @Override
    public void setUploadHandler(UploadHandler uploadHandler) {
        uploadManager.setUploadHandler(uploadHandler);
    }

    @Override
    public void addFileRemovedListener(Consumer<String> listener) {
        uploadManager.addFileRemovedListener(
                event -> listener.accept(event.getFileName()));
    }

    @Override
    public void clearFileList() {
        uploadManager.clearFileList();
    }
}
