/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import Collection from 'ol/Collection';
import Cluster from 'ol/source/Cluster';
import ImageWMS from 'ol/source/ImageWMS';
import OSM, { ATTRIBUTION as OSM_ATTRIBUTION } from 'ol/source/OSM';
import TileWMS from 'ol/source/TileWMS';
import VectorSource from 'ol/source/Vector';
import XYZ from 'ol/source/XYZ';
import type Feature from 'ol/Feature';
import type Source from 'ol/source/Source';
import type TileSource from 'ol/source/Tile';
import type UrlTile from 'ol/source/UrlTile';
import type TileImage from 'ol/source/TileImage';
import type ImageSource from 'ol/source/Image';
import type { MapSyncContext } from '../vaadin-map-types.js';
import type {
  ClusterSourceChange,
  ImageWMSSourceChange,
  OSMSourceChange,
  SourceChange,
  TileWMSSourceChange,
  UrlTileSourceChange,
  VectorSourceChange,
  XYZSourceChange
} from './synchronization-types.js';
import { createOptions, synchronizeCollection } from './util.ts';

function synchronizeSource(target: Source | undefined, source: SourceChange, _context?: MapSyncContext): Source {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/source/Source');
  }

  target.setAttributions(source.attributions ?? undefined);

  return target;
}

function synchronizeTileSource(
  target: TileSource | undefined,
  source: SourceChange,
  context: MapSyncContext
): TileSource {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/source/Tile');
  }
  synchronizeSource(target, source, context);

  return target;
}

function synchronizeUrlTileSource(
  target: UrlTile | undefined,
  source: UrlTileSourceChange,
  context: MapSyncContext
): UrlTile {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/source/UrlTile');
  }
  synchronizeTileSource(target, source, context);
  // Setting null URL is not supported. While not an actual use-case, it is useful to prevent errors here in order
  // to keep the URL empty in integration tests
  if (source.url) {
    target.setUrl(source.url);
  }

  return target;
}

function synchronizeTileImageSource(
  target: TileImage | undefined,
  source: UrlTileSourceChange,
  context: MapSyncContext
): TileImage {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/source/TileImage');
  }
  synchronizeUrlTileSource(target, source, context);

  return target;
}

export function synchronizeTileWMSSource(
  target: TileWMS | undefined,
  source: TileWMSSourceChange,
  context: MapSyncContext
): TileWMS {
  if (!target) {
    target = new TileWMS(createOptions(source));
  }
  synchronizeTileImageSource(target, source, context);

  return target;
}

export function synchronizeXYZSource(target: XYZ | undefined, source: XYZSourceChange, context: MapSyncContext): XYZ {
  if (!target) {
    target = new XYZ(createOptions(source));
  }
  synchronizeTileImageSource(target, source, context);

  return target;
}

export function synchronizeOSMSource(target: OSM | undefined, source: OSMSourceChange, context: MapSyncContext): OSM {
  if (!target) {
    target = new OSM(createOptions(source));
  }

  // For OSM source use default attributions as fallback
  if (!source.attributions) {
    source.attributions = OSM_ATTRIBUTION;
  }
  synchronizeXYZSource(target, source, context);

  return target;
}

function synchronizeImageSource(
  target: ImageSource | undefined,
  source: SourceChange,
  context: MapSyncContext
): ImageSource {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/source/Image');
  }
  synchronizeSource(target, source, context);

  return target;
}

export function synchronizeImageWMSSource(
  target: ImageWMS | undefined,
  source: ImageWMSSourceChange,
  context: MapSyncContext
): ImageWMS {
  if (!target) {
    target = new ImageWMS(createOptions(source));
  }
  synchronizeImageSource(target, source, context);
  // Setting null URL is not supported. While not an actual use-case, it is useful to prevent errors here in order
  // to keep the URL empty in integration tests
  if (source.url) {
    target.setUrl(source.url);
  }

  return target;
}

export function synchronizeVectorSource(
  target: VectorSource | undefined,
  source: VectorSourceChange,
  context: MapSyncContext
): VectorSource {
  if (!target) {
    target = new VectorSource(
      createOptions({
        ...source,
        features: new Collection<Feature>()
      })
    );
  }
  synchronizeSource(target, source, context);
  synchronizeCollection(target.getFeaturesCollection()!, source.features, context);

  return target;
}

export function synchronizeCluster(
  target: Cluster | undefined,
  source: ClusterSourceChange,
  context: MapSyncContext
): Cluster {
  if (!target) {
    target = new Cluster(
      createOptions({
        ...source,
        source: new VectorSource({ features: new Collection<Feature>() })
      })
    );
  }
  target.setDistance(source.distance);
  target.setMinDistance(source.minDistance);
  synchronizeCollection(target.getSource()!.getFeaturesCollection()!, source.features, context);

  return target;
}
