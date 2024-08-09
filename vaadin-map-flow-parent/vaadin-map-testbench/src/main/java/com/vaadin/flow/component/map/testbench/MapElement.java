/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.testbench;

import java.util.List;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-map")
public class MapElement extends TestBenchElement {
    /**
     * Returns a {@link ConfigurationObjectReference} wrapper for the OpenLayers
     * map instance. Used to access nested configuration objects in the browser
     * and extract values from them to be used for assertions.
     */
    public MapReference getMapReference() {
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        return new MapReference(expressionExecutor, "map");
    }

    /**
     * Performs a native click at the specified map coordinates. The method will
     * convert the coordinates into pixel values, and perform a click on the map
     * at the calculated pixel offset.
     *
     * @param x
     * @param y
     */
    public void clickAtCoordinates(double x, double y) {
        PixelCoordinate pixelCoordinates = getPixelCoordinates(x, y, true);

        new Actions(getDriver())
                .moveToElement(this, pixelCoordinates.x, pixelCoordinates.y)
                .click().build().perform();
    }

    /**
     * Returns the pixel coordinates of the specified {@code x} and {@code y}
     * map coordinates.
     * <p>
     * The pixel coordinates are based from the top-left corner of the map.
     *
     * @param x
     *            the x map coordinate
     * @param y
     *            the y map coordinate
     * @return the equivalent pixel coordinate on the map element
     */
    public PixelCoordinate getPixelCoordinates(double x, double y) {
        return getPixelCoordinates(x, y, false);
    }

    /**
     * Returns the pixel coordinates of the specified {@code x} and {@code y}
     * map coordinates.
     * <p>
     * If {@code relativeToCenter} is {@code true}, then the pixel coordinates
     * are based from the center of the map. This is useful for Selenium mouse
     * actions, which are based off of the center of the element. If
     * {@code relativeToCenter} is {@code false}, then the pixel coordinates are
     * based from the top-left corner of the map.
     *
     * @param x
     *            the x map coordinate
     * @param y
     *            the y map coordinate
     * @param relativeToCenter
     *            whether to base the resulting pixel coordinates from the
     *            center of the map element
     * @return the equivalent pixel coordinate on the map element
     */
    @SuppressWarnings("unchecked")
    public PixelCoordinate getPixelCoordinates(double x, double y,
            boolean relativeToCenter) {
        List<Number> coordinateList = (List<Number>) executeScript(
                "return arguments[0].configuration.getPixelFromCoordinate([arguments[1], arguments[2]])",
                this, x, y);
        PixelCoordinate coordinate = new PixelCoordinate(
                (int) Math.round(coordinateList.get(0).doubleValue()),
                (int) Math.round(coordinateList.get(1).doubleValue()));

        if (!relativeToCenter) {
            return coordinate;
        }

        Rectangle mapRectangle = this.getRect();
        int startLeft = -mapRectangle.width / 2;
        int startTop = -mapRectangle.height / 2;

        return new PixelCoordinate(startLeft + coordinate.x,
                startTop + coordinate.y);
    }

    /**
     * Gets the attribution container div
     *
     * @return attribution container div
     */
    public TestBenchElement getAttributionContainer() {
        return $("div").attributeContains("class", "ol-attribution").first();
    }

    /**
     * Gets the list of attributions list items in the attribution container div
     *
     * @return list of list items
     */
    public List<TestBenchElement> getAttributionItems() {
        return getAttributionContainer().$("li").all();
    }

    /**
     * Disables all interactions that could interfere with a test, such as
     * double-click to zoom.
     */
    public void disableInteractions() {
        String script = "const interactions = arguments[0].configuration.getInteractions();"
                + "interactions.forEach(interaction => interaction.setActive && interaction.setActive(false));";
        executeScript(script, this);
    }

    private static class ExpressionExecutor {
        private final MapElement mapElement;

        private ExpressionExecutor(MapElement mapElement) {
            this.mapElement = mapElement;
        }

        public Object executeScript(String script) {
            return mapElement.executeScript(
                    "const map = arguments[0].configuration;" + script,
                    mapElement);
        }

        public Object executeExpression(String expression) {
            return mapElement.executeScript(
                    "const map = arguments[0].configuration; return "
                            + expression,
                    mapElement);
        }
    }

    /**
     * Abstract class for wrapping an in-browser OpenLayers configuration class
     * instance. The class holds a Javascript expression that defines the path
     * to the wrapped object, and provides methods for accessing properties of
     * the object (see {@link #get(String, Object...)} and related methods). All
     * properties are evaluated lazily using an {@link ExpressionExecutor},
     * which takes the path through the configuration hierarchy to that property
     * as Javascript expression and executes it through the Selenium API.
     */
    public static abstract class ConfigurationObjectReference {
        ExpressionExecutor executor;
        String expression;

        private ConfigurationObjectReference(ExpressionExecutor executor,
                String expression) {
            this.executor = executor;
            this.expression = expression;
        }

        /**
         * Creates a path to a nested object as Javascript expression, based on
         * the current path
         *
         * @param path
         *            the nested path
         * @param args
         *            variable arguments to be interpolated into the path, works
         *            like {@link String#format(String, Object...)}
         */
        public String path(String path, Object... args) {
            return this.expression + "." + String.format(path, args);
        }

        /**
         * Extracts a value from the wrapped object by executing a Selenium
         * Javascript call and returning the result. The return type depends on
         * how Selenium converts the Javascript values into Java types. See the
         * more specific methods for returning values in specific types.
         *
         * @param path
         *            the nested path to the value to extract
         * @param args
         *            variable arguments to be interpolated into the path, works
         *            like {@link String#format(String, Object...)}
         */
        public Object get(String path, Object... args) {
            String expression = path(path, args);
            return executor.executeExpression(expression);
        }

        public boolean getBoolean(String path, Object... args) {
            return (boolean) get(path, args);
        }

        public String getString(String path, Object... args) {
            return (String) get(path, args);
        }

        public Integer getInt(String path, Object... args) {
            Number number = (Number) get(path, args);
            return number != null ? number.intValue() : null;
        }

        public Long getLong(String path, Object... args) {
            Number number = (Number) get(path, args);
            return number != null ? number.longValue() : null;
        }

        public Float getFloat(String path, Object... args) {
            Number number = (Number) get(path, args);
            return number != null ? number.floatValue() : null;
        }

        public Double getDouble(String path, Object... args) {
            Number number = (Number) get(path, args);
            return number != null ? number.doubleValue() : null;
        }

        public String getTypeName() {
            return getString("typeName");
        }

        public boolean exists() {
            return (boolean) executor.executeExpression("!!" + expression);
        }
    }

    public static class Coordinate {
        private final double x;
        private final double y;

        public Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    public static class PixelCoordinate {
        private final int x;
        private final int y;

        public PixelCoordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static class MapReference extends ConfigurationObjectReference {
        private MapReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public ViewReference getView() {
            return new ViewReference(executor, path("getView()"));
        }

        public LayerCollectionReference getLayers() {
            return new LayerCollectionReference(executor, path("getLayers()"));
        }
    }

    public static class ViewReference extends ConfigurationObjectReference {
        private ViewReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public Coordinate getCenter() {
            return new Coordinate(getDouble("getCenter()[0]"),
                    getDouble("getCenter()[1]"));
        }

        public void setCenter(Coordinate coordinate) {
            executor.executeScript(path("setCenter([%s, %s])",
                    coordinate.getX(), coordinate.getY()));
        }

        public float getZoom() {
            return getFloat("getZoom()");
        }

        public void setZoom(float zoom) {
            executor.executeScript(path("setZoom(%s)", zoom));
        }

        public float getRotation() {
            return getFloat("getRotation()");
        }

        public void setRotation(float rotation) {
            executor.executeScript(path("setRotation(%s)", rotation));
        }
    }

    public static class LayerCollectionReference
            extends ConfigurationObjectReference {
        private LayerCollectionReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public long getLength() {
            return getLong("getLength()");
        }

        public LayerReference getLayer(int index) {
            return new LayerReference(executor, path("item(%s)", index));
        }

        public LayerReference getLayer(String id) {
            return new LayerReference(executor,
                    path("getArray().find(layer => layer.id === '%s')", id));
        }
    }

    public static class LayerReference extends ConfigurationObjectReference {
        private LayerReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public SourceReference getSource() {
            return new SourceReference(executor, path("getSource()"));
        }

        public boolean isVisible() {
            return getBoolean("getVisible()");
        }

        public float getOpacity() {
            return getFloat("getOpacity()");
        }

        public Long getZIndex() {
            return getLong("getZIndex()");
        }

        public long getRevision() {
            return getLong("getRevision()");
        }
    }

    public static class SourceReference extends ConfigurationObjectReference {
        private SourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public XyzSourceReference asXyzSource() {
            return new XyzSourceReference(executor, expression);
        }

        public TileWmsSourceReference asTileWmsSource() {
            return new TileWmsSourceReference(executor, expression);
        }

        public ImageWmsSourceReference asImageWmsSource() {
            return new ImageWmsSourceReference(executor, expression);
        }

        public VectorSourceReference asVectorSource() {
            return new VectorSourceReference(executor, expression);
        }
    }

    public static abstract class UrlTileSourceReference
            extends SourceReference {
        private UrlTileSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public String getPrimaryUrl() {
            return getString("getUrls()[0]");
        }
    }

    public static class XyzSourceReference extends UrlTileSourceReference {
        private XyzSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public boolean isTileLoaded(int z, int x, int y) {
            String tileKey = String.format("%s/%s/%s", z, x, y);
            return getBoolean("tileCache.containsKey('%s')", tileKey);
        }
    }

    public static class TileWmsSourceReference extends UrlTileSourceReference {
        private TileWmsSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public Object getParam(String name) {
            return get("params_['%s']", name);
        }

        public String getServerType() {
            return getString("serverType_");
        }
    }

    public static class ImageWmsSourceReference extends SourceReference {
        private ImageWmsSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public String getUrl() {
            return getString("url_");
        }

        public Object getParam(String name) {
            return get("params_['%s']", name);
        }

        public String getServerType() {
            return getString("serverType_");
        }

        public String getCrossOrigin() {
            return getString("crossOrigin_");
        }

        public float getRatio() {
            return getFloat("ratio_");
        }
    }

    public static class VectorSourceReference extends SourceReference {
        private VectorSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public FeatureCollectionReference getFeatures() {
            return new FeatureCollectionReference(executor,
                    path("getFeaturesCollection()"));
        }
    }

    public static class FeatureCollectionReference
            extends ConfigurationObjectReference {
        private FeatureCollectionReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public long getLength() {
            return getLong("getLength()");
        }

        public FeatureReference getFeature(int index) {
            return new FeatureReference(executor, path("item(%s)", index));
        }

        public FeatureReference getFeature(String featureId) {
            return new FeatureReference(executor,
                    path("getArray().find(feature => feature.id === '%s')",
                            featureId));
        }
    }

    public static class FeatureReference extends ConfigurationObjectReference {
        private FeatureReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public GeometryReference getGeometry() {
            return new GeometryReference(executor, path("getGeometry()"));
        }

        public StyleReference getStyle() {
            return new StyleReference(executor, path("getStyle()()"));
        }
    }

    public static class StyleReference extends ConfigurationObjectReference {
        private StyleReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public IconReference getImage() {
            return new IconReference(executor, path("getImage()"));
        }

        public TextReference getText() {
            return new TextReference(executor, path("getText()"));
        }
    }

    public static class GeometryReference extends ConfigurationObjectReference {
        private GeometryReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public Coordinate getCoordinates() {
            return new Coordinate(getDouble("getCoordinates()[0]"),
                    getDouble("getCoordinates()[1]"));
        }
    }

    public static class IconReference extends ConfigurationObjectReference {
        private IconReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public double getOpacity() {
            return getDouble("getOpacity()");
        }

        public double getRotation() {
            return getDouble("getRotation()");
        }

        public double getScale() {
            return getDouble("getScale()");
        }

        /**
         * Get color as rgb string, for example {@code rgb(0, 0, 255)}, or null
         * if there is no color
         */
        public String getColor() {
            if (get("getColor()") == null)
                return null;

            int r = getInt("getColor()[0]");
            int g = getInt("getColor()[1]");
            int b = getInt("getColor()[2]");
            return String.format("rgb(%s, %s, %s)", r, g, b);
        }

        public String getSrc() {
            return getString("getSrc()");
        }
    }

    public static class TextReference extends ConfigurationObjectReference {
        private TextReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public String getText() {
            return getString("getText()");
        }

        public String getFont() {
            return getString("getFont()");
        }

        public int getOffsetX() {
            return getInt("getOffsetX()");
        }

        public int getOffsetY() {
            return getInt("getOffsetY()");
        }

        public String getTextAlign() {
            return getString("getTextAlign()");
        }

        public String getTextBaseline() {
            return getString("getTextBaseline()");
        }

        public FillReference getFill() {
            return new FillReference(executor, path("getFill()"));
        }

        public StrokeReference getStroke() {
            return new StrokeReference(executor, path("getStroke()"));
        }

        public FillReference getBackgroundFill() {
            return new FillReference(executor, path("getBackgroundFill()"));
        }

        public StrokeReference getBackgroundStroke() {
            return new StrokeReference(executor, path("getBackgroundStroke()"));
        }
    }

    public static class FillReference extends ConfigurationObjectReference {
        private FillReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public String getColor() {
            return getString("getColor()");
        }
    }

    public static class StrokeReference extends ConfigurationObjectReference {
        private StrokeReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public String getColor() {
            return getString("getColor()");
        }

        public Number getWidth() {
            return getInt("getWidth()");
        }
    }
}
