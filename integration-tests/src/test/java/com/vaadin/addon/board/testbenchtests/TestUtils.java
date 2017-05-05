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

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.stream.JsonReader;

public class TestUtils {

    private static  DesiredCapabilities parseBrowser(String browser) {
        if (browser.equals("Chrome")) {
            return DesiredCapabilities.chrome();
        } else if (browser.equals("Internet Explorer")) {
            return DesiredCapabilities.internetExplorer();
        } else if (browser.equals("Firefox")) {
            return DesiredCapabilities.firefox();
        } else {
            return DesiredCapabilities.firefox();
        }
    }

    public  List<DesiredCapabilities> getCapabilitiesFromFile(String path)
        throws FileNotFoundException {
        FileReader config = null;
        URL url = getClass().getResource(path);
        config = new FileReader(new File(url.getPath()));
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
                            if (property.equals("browserName")) {
                                browser = reader.nextString();
                            } else if (property.equals("platform")) {
                                platform = reader.nextString();
                            } else if (property.equals("version")) {
                                version = reader.nextString();
                            } else {
                                noNameProps.put(property, reader.nextString());
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
