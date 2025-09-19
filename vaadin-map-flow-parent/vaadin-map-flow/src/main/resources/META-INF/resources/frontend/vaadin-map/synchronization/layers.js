/**
 * @license
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import ImageLayer from 'ol/layer/Image';
import TileLayer from 'ol/layer/Tile';
import VectorLayer from 'ol/layer/Vector';
import Style from 'ol/style/Style';
import Circle from 'ol/style/Circle';
import Fill from 'ol/style/Fill';
import Stroke from 'ol/style/Stroke';
import Text from 'ol/style/Text';
import { createOptions } from './util.js';

const defaultClusterStyle = [
  // Outline circle
  new Style({
    image: new Circle({
      radius: 16,
      stroke: new Stroke({
        color: '#00000022',
        width: 2
      })
    })
  }),
  // Cluster circle
  new Style({
    image: new Circle({
      radius: 15,
      stroke: new Stroke({
        color: '#fff',
        width: 2
      }),
      fill: new Fill({
        color: '#1676f3'
      })
    }),
    text: new Text({
      text: '',
      font: 'bold 12px sans-serif',
      fill: new Fill({
        color: '#fff'
      })
    })
  })
];

function synchronizeLayer(target, source, _context) {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/layer/Layer');
  }

  target.setOpacity(source.opacity);
  target.setVisible(source.visible);
  target.setZIndex(source.zIndex || undefined);
  target.setMinZoom(source.minZoom || -Infinity);
  target.setMaxZoom(source.maxZoom || Infinity);
  target.setBackground(source.background || undefined);

  return target;
}

export function synchronizeTileLayer(target, source, context) {
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

export function synchronizeVectorLayer(target, source, context) {
  if (!target) {
    target = new VectorLayer(
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

export function synchronizeClusterLayer(target, source, context) {
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

  const style = context.lookup.get(source.style);

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
    // Use custom cluster style if available
    if (style) {
      const textStyle = style.getText();
      if (textStyle) {
        textStyle.setText(size.toString());
      }
      return style;
    }

    // Fallback to default style
    defaultClusterStyle[1].getText().setText(size.toString());

    return defaultClusterStyle;
  });

  return target;
}

export function synchronizeImageLayer(target, source, context) {
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
