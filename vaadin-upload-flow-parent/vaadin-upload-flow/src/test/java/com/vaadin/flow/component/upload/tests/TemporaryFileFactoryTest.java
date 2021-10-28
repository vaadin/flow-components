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
