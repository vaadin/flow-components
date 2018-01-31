package com.vaadin.addon.board.testUI;

import static java.lang.System.setProperty;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public interface UIFunctions {


     public static Properties readProperties(String filename) throws IOException {
        try (
            final FileInputStream fis = new FileInputStream(new File(filename));
            final BufferedInputStream bis = new BufferedInputStream(fis)) {
            final Properties properties = new Properties();
            properties.load(bis);

            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void readTestbenchProperties () throws IOException {
        Properties props = readProperties("config/testbench.properties");
        if (props != null) {
            props.forEach((key, value) -> setProperty((String) key, (String) value));
        }
    }
}
