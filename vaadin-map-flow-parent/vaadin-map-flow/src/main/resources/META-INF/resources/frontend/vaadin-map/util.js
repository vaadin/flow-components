import VectorSource from "ol/source/Vector";

/**
 * Searches an OpenLayers map instance for the layer whose source contains a specific feature
 * @param layers the array of layers configured in the map
 * @param feature the feature that should be contained in the layers source
 * @returns {*} the layer that contains the feature, or undefined
 */
export function getLayerForFeature(layers, feature) {
  return layers.find((layer) => {
    const source = layer.getSource && layer.getSource();
    const isVectorSource = source && source instanceof VectorSource;

    return isVectorSource && source.getFeatures().includes(feature);
  });
}

/**
 * Takes an array of features, and returns an array of { feature, layer } tuples,
 * where each tuple contains the first feature that occurred for a specific
 * layer in the given array of features.
 * @param layers the array of layers configured in the map
 * @param features the array of features for which to find the first feature per layer
 * @returns {{layer, feature}[]}
 */
export function findFirstFeaturePerLayer(layers, features) {
  const layerToFeaturesMap = {};

  features.forEach((feature) => {
    // First lookup the layer that the feature is in
    const layer = getLayerForFeature(layers, feature);
    // Skip if we already had a feature for that layer
    if (layerToFeaturesMap[layer.id]) return;
    // Otherwise, register that feature as the first one for that layer
    layerToFeaturesMap[layer.id] = { feature, layer };
  });

  return Object.values(layerToFeaturesMap);
}