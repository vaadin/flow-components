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

import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadDropLabelTest {
    // Regression test for:
    // https://github.com/vaadin/flow-components/issues/3053
    @Test
    public void setLabelAndIcon_updateLabel_doesNotThrow() {
        UI ui = new UI();
        UI.setCurrent(ui);
        Upload upload = new Upload();
        upload.setDropLabel(new Span("Label"));
        upload.setDropLabelIcon(new Span("Icon"));
        upload.setDropLabel(new Span("Updated Label"));
    }
}
