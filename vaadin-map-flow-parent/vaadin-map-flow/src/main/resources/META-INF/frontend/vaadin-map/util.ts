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
import type Feature from 'ol/Feature';
import type { FeatureLike } from 'ol/Feature';
import type OlMap from 'ol/Map';
import type Layer from 'ol/layer/Layer';
import type { MapFeatureInfo, MapLookup } from './vaadin-map-types.js';

/**
 * Simple lookup for OL instances that are used by Map.
 * This implementation will never release references, which means that OL
 * instances in the lookup can never be garbage collected as long as the
 * Map element exists.
 * This should only be used as a fallback if a browser does not support
 * weak references and finalization registry.
 */
class SimpleLookup implements MapLookup {
  private map = new Map<string, object>();

  get(id: string): any {
    return this.map.get(id);
  }

  put(id: string, instance: object): void {
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
class WeakReferenceLookup implements MapLookup {
  private map = new Map<string, WeakRef<object>>();

  // Create registry that notifies when a reference is garbage collected,
  // the callback removes the WeakRef entry from the map
  private registry = new FinalizationRegistry<string>((id) => {
    this.map.delete(id);
  });

  get(id: string): any {
    const weakRef = this.map.get(id);
    return weakRef ? weakRef.deref() : undefined;
  }

  put(id: string, instance: object): void {
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

const supportsWeakReferenceLookup = typeof WeakRef !== 'undefined' && typeof FinalizationRegistry !== 'undefined';

/**
 * Creates a lookup that is supported by the browser
 */
export function createLookup(): MapLookup {
  return supportsWeakReferenceLookup ? new WeakReferenceLookup() : new SimpleLookup();
}

/**
 * Returns information about a feature within an OpenLayers map instance.
 * Includes whether the feature is a cluster or a single feature, and
 * which layer and source it belongs to.
 */
export function getFeatureInfo(map: OlMap, feature: FeatureLike): MapFeatureInfo {
  const layer = map
    .getLayers()
    .getArray()
    .find((candidate) => {
      const source = (candidate as Layer).getSource && (candidate as Layer).getSource();
      const isVectorSource = source && source instanceof VectorSource;
      return isVectorSource && source.getFeatures().includes(feature as Feature);
    }) as Layer | undefined;
  const source = (layer && layer.getSource()) ?? undefined;

  // Unwrap single feature from cluster
  let resolvedFeature = feature as Feature;
  const clusterFeatures = resolvedFeature.get('features') as Feature[] | undefined;
  if (Array.isArray(clusterFeatures) && clusterFeatures.length === 1) {
    resolvedFeature = clusterFeatures[0];
  }

  const isCluster = Array.isArray(clusterFeatures) && clusterFeatures.length > 1;

  return {
    feature: resolvedFeature,
    layer,
    source,
    isCluster
  };
}
