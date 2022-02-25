package com.vaadin.flow.component.map.testbench;

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

@Element("vaadin-map")
public class MapElement extends TestBenchElement {
    /**
     * Evaluates a Javascript expression against a vaadin-map's internal
     * OpenLayers map instance, and returns the result. The OpenLayers map
     * instance will be provided as the {@code map} variable to the expression.
     *
     * @param expression
     *            the Javascript expression to execute
     * @return result of the Javascript evaluation
     */
    public Object evaluateOLExpression(String expression) {
        return executeScript(
                "const map = arguments[0].configuration; return " + expression,
                getWrappedElement());
    }

    /**
     * Helper for building a Javascript expression that returns the type name of
     * an OpenLayers class instance
     *
     * @param jsExpression
     *            a Javascript expression that returns an OpenLayers class
     *            instance
     * @return the Javascript expression that evaluates the type name
     */
    public String getOLTypeNameExpression(String jsExpression) {
        return jsExpression + ".typeName";
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
        // Selenium click event offset starts from center, so we need to shift
        // the offset to the start of the element first
        Rectangle mapRectangle = this.getRect();
        int startLeft = -mapRectangle.width / 2;
        int startTop = -mapRectangle.height / 2;

        List<Number> pixelCoordinates = (List<Number>) executeScript(
                "return arguments[0].configuration.getPixelFromCoordinate([arguments[1], arguments[2]])",
                this, x, y);

        int clickX = startLeft + pixelCoordinates.get(0).intValue();
        int clickY = startTop + pixelCoordinates.get(1).intValue();
        new Actions(getDriver()).moveToElement(this, clickX, clickY).click()
                .build().perform();
    }

    public MapReference getMapReference() {
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        return new MapReference(expressionExecutor, "map");
    }

    /**
     * Returns a Javascript expression that returns the layer with the specified
     * ID.
     *
     * @return a Javascript expression evaluating the layer
     */
    public String getLayerExpression(String layerId) {
        return String.format(
                "map.getLayers().getArray().find(layer => layer.id === '%s')",
                layerId);
    }

    /**
     * Returns a Javascript expression that evaluates the feature collection of
     * the layer with the specified ID. Assumes that the layer is a vector layer
     * that has a feature collection.
     *
     * @return a Javascript expression evaluating the feature collection of the
     *         feature layer's source
     */
    public String getFeatureCollectionExpression(String layerId) {
        return getLayerExpression(layerId)
                + ".getSource().getFeaturesCollection()";
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

        public ExpressionExecutor(MapElement mapElement) {
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

    public static class ConfigurationObjectReference {
        ExpressionExecutor executor;
        String expression;

        public ConfigurationObjectReference(ExpressionExecutor executor,
                String expression) {
            this.executor = executor;
            this.expression = expression;
        }

        public String path(String path, Object... args) {
            return this.expression + "." + String.format(path, args);
        }

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

    public static class MapReference extends ConfigurationObjectReference {
        public MapReference(ExpressionExecutor executor, String expression) {
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
        public ViewReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public Coordinate getCenter() {
            return new Coordinate(getDouble("getCenter()[0]"),
                    getDouble("getCenter()[1]"));
        }

        public float getZoom() {
            return getFloat("getZoom()");
        }

        public float getRotation() {
            return getFloat("getRotation()");
        }
    }

    public static class LayerCollectionReference
            extends ConfigurationObjectReference {
        public LayerCollectionReference(ExpressionExecutor executor,
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
        public LayerReference(ExpressionExecutor executor, String expression) {
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
    }

    public static class SourceReference extends ConfigurationObjectReference {
        public SourceReference(ExpressionExecutor executor, String expression) {
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
        public UrlTileSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public String getPrimaryUrl() {
            return getString("getUrls()[0]");
        }
    }

    public static class XyzSourceReference extends UrlTileSourceReference {
        public XyzSourceReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }
    }

    public static class TileWmsSourceReference extends UrlTileSourceReference {
        public TileWmsSourceReference(ExpressionExecutor executor,
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
        public ImageWmsSourceReference(ExpressionExecutor executor,
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
        public VectorSourceReference(ExpressionExecutor executor,
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
        public FeatureCollectionReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public long getLength() {
            return getLong("getLength()");
        }

        public FeatureReference getFeature(int index) {
            return new FeatureReference(executor, path("item(%s)", index));
        }
    }

    public static class FeatureReference extends ConfigurationObjectReference {
        public FeatureReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public GeometryReference getGeometry() {
            return new GeometryReference(executor, path("getGeometry()"));
        }

        public StyleReference getStyle() {
            return new StyleReference(executor, path("getStyle()"));
        }
    }

    public static class StyleReference extends ConfigurationObjectReference {
        public StyleReference(ExpressionExecutor executor, String expression) {
            super(executor, expression);
        }

        public IconReference getImage() {
            return new IconReference(executor, path("getImage()"));
        }
    }

    public static class GeometryReference extends ConfigurationObjectReference {
        public GeometryReference(ExpressionExecutor executor,
                String expression) {
            super(executor, expression);
        }

        public Coordinate getCoordinates() {
            return new Coordinate(getDouble("getCoordinates()[0]"),
                    getDouble("getCoordinates()[1]"));
        }
    }

    public static class IconReference extends ConfigurationObjectReference {
        public IconReference(ExpressionExecutor executor, String expression) {
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
}
