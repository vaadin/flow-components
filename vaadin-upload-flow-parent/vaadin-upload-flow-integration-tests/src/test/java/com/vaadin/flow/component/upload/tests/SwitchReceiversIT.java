/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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

        $("button").id("set-multi-file-receiver-and-max-files").click();
        Assert.assertEquals(
                "The maxFiles property should equal 3 when multi file receiver and max files is set",
                3, (int) upload.getPropertyInteger("maxFiles"));
    }
}
