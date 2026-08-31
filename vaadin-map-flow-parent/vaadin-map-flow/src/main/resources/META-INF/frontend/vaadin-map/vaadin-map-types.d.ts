// Types for the Flow-specific map API. The public API types come from the
// @vaadin and ol npm packages, resolved from the integration tests module's
// node_modules (see tsconfig.json in the module root).
import type { Map as VaadinMap } from '@vaadin/map/src/vaadin-map.js';
import type Feature from 'ol/Feature';
import type Layer from 'ol/layer/Layer';
import type Source from 'ol/source/Source';
import type { MapConnector } from './mapConnector.js';

export type { MapConnector };

/** The Flow map element */
export type FlowMap = VaadinMap & {
  $connector: MapConnector;
};

/** An OpenLayers feature extended with the Flow-specific drag and drop marker */
export type FlowFeature = Feature & {
  draggable?: boolean;
};

/** A configuration object from the server, to be synchronized into an OL instance */
export interface MapSyncChange {
  type: string;
  id: string;
  [key: string]: unknown;
}

/** Options for the `zoomToFit` method, passed to the OL `View.fit` method */
export interface MapZoomToFitOptions {
  padding?: number;
  duration?: number;
}

/** Information about a feature within the map, as returned by `getFeatureInfo` */
export interface MapFeatureInfo {
  feature: Feature;
  layer: Layer | undefined;
  source: Source | undefined;
  isCluster: boolean;
}

declare global {
  // Augments the global Vaadin interface declared by @vaadin/component-base
  // with the Flow namespace used by the connector. The namespace is a shared
  // interface that each module merges its own members into, so that connectors
  // from different modules can be type-checked in the same program.
  interface Vaadin {
    Flow: VaadinFlow;
  }

  interface VaadinFlow {
    mapConnector: {
      init(mapElement: FlowMap): void;
      setUserProjection(projection: string): void;
      defineProjection(projection: string, wksDefinition: string): void;
    };
  }
}
