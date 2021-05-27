package com.vaadin.flow.component.charts.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.charts.model.Configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SVGGeneratorTest {

    private SVGGenerator svgGenerator;

    @Before
    public void setup() throws IOException {
        svgGenerator = new SVGGenerator();
    }

    @After
    public void cleanup() throws IOException {
        if (!svgGenerator.isClosed()) {
            svgGenerator.close();
        }
    }

    @Test(expected = NullPointerException.class)
    public void chartConfigurationMustNotBeNull() throws IOException, InterruptedException {
        svgGenerator.generate(null);
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionOnClosedGenerator() throws IOException, InterruptedException {
        svgGenerator.close();
        // it should check to see if the generator is closed before it checks if the config is null
        svgGenerator.generate(null);
    }

    @Test
    public void shouldKnowWhenItIsClosed() throws IOException {
        assertFalse(svgGenerator.isClosed());
        svgGenerator.close();
        assertTrue(svgGenerator.isClosed());
    }

    @Test
    public void generateSVGFromAnEmptyConfiguration() throws IOException, InterruptedException {
        Configuration configuration = new Configuration();
        String svg = svgGenerator.generate(configuration);
        fail("Test to be implemented.");
    }

    // test to generate an SVG from a complete configuration instance
    @Test
    public void generateSVGFromValidConfiguration() throws IOException, InterruptedException {
        fail("Test to be implemented.");

    }

    // test to check the chart file doesn't exists after the generate method returns

}
