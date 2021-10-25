package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.receivers.TemporaryFileFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TemporaryFileFactoryTest {

    @Test
    public void temporaryFileShouldNotContainFileName() throws IOException{
        TemporaryFileFactory temporaryFileFactory = new TemporaryFileFactory();
        File testFile = temporaryFileFactory.createFile("test");
        System.out.println(testFile.getName());
        Assert.assertFalse(testFile.getName().contains("test"));
    }
}
