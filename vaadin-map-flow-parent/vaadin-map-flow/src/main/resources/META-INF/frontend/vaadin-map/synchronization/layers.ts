/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import ImageLayer from 'ol/layer/Image';
import TileLayer from 'ol/layer/Tile';
import VectorLayer from 'ol/layer/Vector';
import type Layer from 'ol/layer/Layer';
import type ImageSource from 'ol/source/Image';
import type { MapSyncContext } from '../vaadin-map-types.js';
import type {
  FeatureLayerChange,
  ImageLayerChange,
  LayerChange,
  TileLayerChange,
  VectorLayerChange
} from './synchronization-types.js';
import { createOptions } from './util.ts';

function synchronizeLayer(target: Layer | undefined, source: LayerChange): Layer {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/layer/Layer');
  }

  target.setOpacity(source.opacity);
  target.setVisible(source.visible);
  // OL declares setZIndex to require a number, but treats undefined as unsetting the z-index
  target.setZIndex((source.zIndex || undefined) as unknown as number);
  target.setMinZoom(source.minZoom || -Infinity);
  target.setMaxZoom(source.maxZoom || Infinity);
  target.setBackground(source.background || undefined);

  return target;
}

export function synchronizeTileLayer(
  target: TileLayer | undefined,
  source: TileLayerChange,
  context: MapSyncContext
): TileLayer {
  if (!target) {
    target = new TileLayer(
      createOptions({
        ...source,
        source: context.lookup.get(source.source)
      })
    );
  }

  synchronizeLayer(target, source);
  target.setSource(context.lookup.get(source.source));

  return target;
}

export function synchronizeVectorLayer(
  target: VectorLayer | undefined,
  source: VectorLayerChange,
  context: MapSyncContext
): VectorLayer {
  if (!target) {
    target = new VectorLayer(
      createOptions({
        ...source,
        source: context.lookup.get(source.source),
        style: undefined
      })
    );
  }

  synchronizeLayer(target, source);
  target.setSource(context.lookup.get(source.source));

  return target;
}

export function synchronizeFeatureLayer(
  target: VectorLayer | undefined,
  source: FeatureLayerChange,
  context: MapSyncContext
): VectorLayer {
  target = synchronizeVectorLayer(target, source, context);

  const clusterStyle = context.lookup.get(source.clusterStyle!);

  target.setStyle((feature) => {
    const size = feature.get('features').length;

    // When rendering a single feature, use the feature's style
    if (size === 1) {
      const originalFeature = feature.get('features')[0];
      let originalStyle = originalFeature.getStyle();
      if (typeof originalStyle === 'function') {
        originalStyle = originalStyle(originalFeature);
      }
      if (originalStyle) {
        return originalStyle;
      }
    }

    // Multiple features indicate a cluster
    const textStyle = clusterStyle ? clusterStyle.getText() : null;
    if (textStyle) {
      // Override the text to show the number of features in the cluster
      textStyle.setText(size.toString());
    }

    return clusterStyle;
  });

  return target;
}

export function synchronizeImageLayer(
  target: ImageLayer<ImageSource> | undefined,
  source: ImageLayerChange,
  context: MapSyncContext
): ImageLayer<ImageSource> {
  if (!target) {
    target = new ImageLayer(
      createOptions({
        ...source,
        source: context.lookup.get(source.source)
      })
    );
  }

  synchronizeLayer(target, source);
  target.setSource(context.lookup.get(source.source));

  return target;
}
