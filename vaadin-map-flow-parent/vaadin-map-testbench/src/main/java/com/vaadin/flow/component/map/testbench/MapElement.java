package com.vaadin.flow.component.map.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

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
     * Returns a Javascript expression that returns the feature layer of the
     * map.
     * <p>
     * Effectively this uses the first vector layer, which means that this
     * should only be used if no custom vector layers have been added.
     *
     * @return a Javascript expression evaluating the feature layer
     */
    public String getFeatureLayerExpression() {
        return "map.getLayers().getArray().find(layer => layer.typeName === 'ol/layer/Vector')";
    }

    /**
     * Returns a Javascript expression that evaluates the feature collection of
     * the feature layers vector source.
     * <p>
     * Effectively this uses the first vector layer, which means that this
     * should only be used if no custom vector layers have been added.
     *
     * @return a Javascript expression evaluating the feature collection of the
     *         feature layer's source
     */
    public String getFeatureCollectionExpression() {
        return getFeatureLayerExpression()
                + ".getSource().getFeaturesCollection()";
    }
}
