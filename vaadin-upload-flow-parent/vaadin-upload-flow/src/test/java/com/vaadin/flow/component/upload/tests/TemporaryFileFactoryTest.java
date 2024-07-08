/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.receivers.TemporaryFileFactory;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;

public class TemporaryFileFactoryTest {

    @Test
    public void temporaryFileShouldNotContainFileName() throws IOException {
        TemporaryFileFactory temporaryFileFactory = new TemporaryFileFactory();
        File testFile = temporaryFileFactory.createFile("test");
        String fileName = testFile.getName();

        Assert.assertThat(fileName, IsNot.not(containsString("test")));
    }
}
