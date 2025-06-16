/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.upload;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableConsumer;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadTest {

    @Test
    public void uploadHandlerSet_generatedUrlEndsWithUpload() {
        UI original = UI.getCurrent();
        UI ui = Mockito.mock(UI.class);
        UI.setCurrent(ui);
        Mockito.when(ui.getUIId()).thenReturn(5);

        try {
            Upload testUpload = new Upload((event) -> {
            }) {
                @Override
                void runBeforeClientResponse(SerializableConsumer<UI> command) {
                    command.accept(Mockito.mock(UI.class));
                }
            };

            testUpload.setUploadHandler(event -> {
            });

            String targetUploadUrl = testUpload.getElement()
                    .getAttribute("target");
            Assert.assertTrue("Upload url should end with 'upload'",
                    targetUploadUrl.endsWith("upload"));
        } finally {
            UI.setCurrent(original);
        }

    }

}
