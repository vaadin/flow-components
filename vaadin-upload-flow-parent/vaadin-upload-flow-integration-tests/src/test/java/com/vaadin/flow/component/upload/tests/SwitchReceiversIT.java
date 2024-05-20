/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-upload/switch-receivers")
public class SwitchReceiversIT extends AbstractUploadIT {

    private UploadElement upload;

    @Before
    public void init() {
        open();
        upload = $(UploadElement.class).first();
    }

    @Test
    public void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        $("button").id("set-single-file-receiver").click();
        Assert.assertEquals(
                "The maxFiles property should equal 1 when single file receiver is set",
                1, (int) upload.getPropertyInteger("maxFiles"));

        $("button").id("set-multi-file-receiver").click();
        Assert.assertTrue(
                "The maxFiles property should equal Infinity when multi file receiver is set",
                (boolean) executeScript(
                        "return arguments[0].maxFiles === Infinity", upload));

        $("button").id("set-single-file-receiver").click();
        Assert.assertEquals(
                "The maxFiles property should equal 1 when single file receiver is set",
                1, (int) upload.getPropertyInteger("maxFiles"));
    }
}
