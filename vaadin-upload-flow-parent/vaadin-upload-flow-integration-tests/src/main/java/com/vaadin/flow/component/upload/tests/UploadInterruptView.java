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
package com.vaadin.flow.component.upload.tests;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("vaadin-upload/interrupt")
public class UploadInterruptView extends Div {

    public UploadInterruptView() {
        Div output = new Div();
        output.setId("test-output");
        Div eventsOutput = new Div();
        eventsOutput.setId("test-events-output");

        MultiFileMemoryBuffer buffer = new SlowMultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".txt");
        upload.addStartedListener(event -> {
            if (isInterruptableFile(event.getFileName())) {
                event.getUpload().interruptUpload();
            }
        });
        upload.addFailedListener(event -> {
            eventsOutput.add("-failed");
            output.add("FAILED:" + event.getFileName() + ","
                    + event.getReason().getMessage());
        });
        upload.addSucceededListener(event -> eventsOutput.add("-succeeded"));
        upload.addAllFinishedListener(event -> eventsOutput.add("-finished"));

        add(upload, output, eventsOutput);
    }

    private static boolean isInterruptableFile(String fileName) {
        return fileName != null && fileName.endsWith(".interrupt.txt");
    }

    // Returns an OutputStream that delays write operations for uploads. The
    // delay ensures that the interruption flag is set before uploads completion
    // so that the test can verify all uploads failed.
    private static class SlowMultiFileMemoryBuffer
            extends MultiFileMemoryBuffer {
        @Override
        public OutputStream receiveUpload(String fileName, String MIMEType) {
            OutputStream outputStream = super.receiveUpload(fileName, MIMEType);
            // Also delay the interrupted file to allow other uploads to start
            int delay = isInterruptableFile(fileName) ? 500 : 1000;
            return new SlowOutputStream(outputStream, delay);
        }
    }

    private static class SlowOutputStream extends FilterOutputStream {

        private final int delay;

        SlowOutputStream(OutputStream delegate, int delay) {
            super(delegate);
            this.delay = delay;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted", e);
            }
            super.write(b, off, len);
        }
    }
}
