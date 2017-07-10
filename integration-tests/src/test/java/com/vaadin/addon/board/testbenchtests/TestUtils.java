package com.vaadin.addon.board.testbenchtests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.stream.JsonReader;

public class TestUtils {

    public static final Dimension WINDOW_SIZE_LARGE = new Dimension(1920, 1080);
    public static final Dimension WINDOW_SIZE_MEDIUM = new Dimension(768, 1024);
    public static final Dimension WINDOW_SIZE_SMALL = new Dimension(375, 667);

    private static  DesiredCapabilities parseBrowser(String browser) {
        switch (browser) {
        case BrowserType.CHROME:
            return DesiredCapabilities.chrome();
        case BrowserType.IE:
            return DesiredCapabilities.internetExplorer();
        case BrowserType.FIREFOX:
            return DesiredCapabilities.firefox();
        default:
            return DesiredCapabilities.firefox();
        }
    }

    public  List<DesiredCapabilities> getCapabilitiesFromFile(String path)
        throws FileNotFoundException {
        URL url = getClass().getResource(path);
        FileReader config = new FileReader(new File(url.getPath()));
        return createCapabilities(config);

    }

    public List<DesiredCapabilities> createCapabilities(FileReader config) {
        List<DesiredCapabilities> dc = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader(config);
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("browsers")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        String browser = "";
                        String version = "";
                        String platform = "";
                        Map<String, String> noNameProps = new HashMap<>();
                        while (reader.hasNext()) {
                            String property = reader.nextName();
                            switch (property) {
                            case "browserName":
                                browser = reader.nextString();
                                break;
                            case "platform":
                                platform = reader.nextString();
                                break;
                            case "version":
                                version = reader.nextString();
                                break;
                            default:
                                noNameProps.put(property, reader.nextString());
                                break;
                            }

                        }
                        DesiredCapabilities cap = TestUtils.parseBrowser(browser);
                        cap.setPlatform(Platform.fromString(platform));
                        cap.setVersion(version);
                        for (Map.Entry<String, String> pair : noNameProps.entrySet()) {
                            cap.setCapability(pair.getKey(), pair.getValue());
                        }
                        reader.endObject();
                        dc.add(cap);
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dc;
    }
}
