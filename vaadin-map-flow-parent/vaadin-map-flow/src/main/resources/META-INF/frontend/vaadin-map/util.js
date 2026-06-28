/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import VectorSource from 'ol/source/Vector';

/**
 * Simple lookup for OL instances that are used by Map.
 * This implementation will never release references, which means that OL
 * instances in the lookup can never be garbage collected as long as the
 * Map element exists.
 * This should only be used as a fallback if a browser does not support
 * weak references and finalization registry.
 */
class SimpleLookup {
  constructor() {
    this.map = new Map();
  }

  get(id) {
    return this.map.get(id);
  }

  put(id, instance) {
    this.map.set(id, instance);
  }
}

/**
 * Lookup for OL instances that uses weak references for storing objects.
 * Using weak references allows the browser to garbage collect the OL
 * instances that are not used anymore in the Map. The lookup also uses a
 * finalization registry to remove weak references from the lookup when
 * instances are garbage collected.
 */
class WeakReferenceLookup {
  constructor() {
    this.map = new Map();
    // Create registry that notifies when a reference is garbage collected,
    // the callback removes the WeakRef entry from the map
    this.registry = new FinalizationRegistry((id) => {
      this.map.delete(id);
    });
  }

  get(id) {
    const weakRef = this.map.get(id);
    return weakRef ? weakRef.deref() : undefined;
  }

  put(id, instance) {
    // Check if there's an existing entry
    const existingRef = this.map.get(id);
    if (existingRef) {
      // Unregister the old instance to prevent its cleanup callback from
      // removing the new instance once the old one is garbage collected
      this.registry.unregister(existingRef);
    }
    // Store weak reference in map
    const ref = new WeakRef(instance);
    this.map.set(id, ref);
    // Track reference for garbage collection, so that we can clean up the map entry.
    // Use the WeakRef as the unregister token so we can cancel the callback if the
    // entry is overwritten later.
    this.registry.register(instance, id, ref);
  }
}

const supportsWeakReferenceLookup = window.WeakRef && window.FinalizationRegistry;

/**
 * Creates a lookup that is supported by the browser
 * @returns {WeakReferenceLookup|SimpleLookup}
 */
export function createLookup() {
  return supportsWeakReferenceLookup ? new WeakReferenceLookup() : new SimpleLookup();
}

/**
 * Returns information about a feature within an OpenLayers map instance.
 * Includes whether the feature is a cluster or a single feature, and
 * which layer and source it belongs to.
 * @param map
 * @param feature
 * @returns {{feature: *, layer: *, source: *, isCluster: boolean}}
 */
export function getFeatureInfo(map, feature) {
  const layer = map
    .getLayers()
    .getArray()
    .find((layer) => {
      const source = layer.getSource && layer.getSource();
      const isVectorSource = source && source instanceof VectorSource;
      return isVectorSource && source.getFeatures().includes(feature);
    });
  const source = layer && layer.getSource();

  // Unwrap single feature from cluster
  const clusterFeatures = feature.get('features');
  if (Array.isArray(clusterFeatures) && clusterFeatures.length === 1) {
    feature = clusterFeatures[0];
  }

  const isCluster = Array.isArray(clusterFeatures) && clusterFeatures.length > 1;

  return {
    feature,
    layer,
    source,
    isCluster
  };
}
