package com.vaadin.flow.component.charts.util;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2021 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.server.frontend.FrontendUtils;

public class SVGGenerator {

    private SVGGenerator() {
        // avoid making instances of this class
    }

    public static String generateSVG(Configuration chartConfiguration) {
        if (chartConfiguration == null) {
            throw new IllegalArgumentException(
                    "Chart configuration must not be null");
        }
        // TODO handle JSON serialization errors
        String chartConfigurationString = ChartSerialization
                .toJSON(chartConfiguration);

        try {
            Path tempDir = Files.createTempDirectory("svg-export");
            tempDir.toFile().deleteOnExit();
            runExportServer(chartConfigurationString, "svg",
                    tempDir.toFile().getAbsolutePath());
            File chartFile = tempDir.resolve("chart.svg").toFile();
            chartFile.deleteOnExit();
            byte[] data = Files
                    .readAllBytes(Paths.get(chartFile.getAbsolutePath()));
            return new String(data, StandardCharsets.UTF_8);
        } catch (InterruptedException | IOException e) {
            // TODO check if highcharts-export-server is installed, show error
            // message if not
            System.err.println("MESSAGE: " + e.getMessage());
            e.printStackTrace();
            LoggerFactory.getLogger(SVGGenerator.class).info("error");
        }
        return null;
    }

    private static int runExportServer(String options, String type,
            String tempDir) throws InterruptedException, IOException {
        List<String> args = new ArrayList<>(
                Arrays.asList("highcharts-export-server", "--type", type,
                        "--options", options));
        if (tempDir != null) {
            args.add("--tempDir");
            args.add(tempDir);
            args.add("--outfile");
            args.add(tempDir + "/chart.svg");
        }
        ProcessBuilder builder = FrontendUtils.createProcessBuilder(args);
        builder.inheritIO();
        return builder.start().waitFor();
    }

}