/**
 * @license
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import Fill from 'ol/style/Fill';
import Stroke from 'ol/style/Stroke';
import Style from 'ol/style/Style';
import Text from 'ol/style/Text';
import { Icon } from 'ol/style';
import { convertEnumValue, convertToCoordinateArray, convertToSizeArray, createOptions } from './util';

export function synchronizeFill(target, source, context) {
  if (!target) {
    target = new Fill();
  }

  target.setColor(source.color);

  context.connector.forceRender();

  return target;
}

export function synchronizeStroke(target, source, context) {
  if (!target) {
    target = new Stroke();
  }

  target.setColor(source.color);
  target.setWidth(source.width);

  context.connector.forceRender();

  return target;
}

function synchronizeImageStyle(target, source, _context) {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/style/Image');
  }

  target.setOpacity(source.opacity);
  target.setRotateWithView(source.rotateWithView);
  target.setRotation(source.rotation);
  target.setScale(source.scale);

  return target;
}

export function synchronizeIcon(target, source, context) {
  if (!target) {
    const src = source.img || source.src;
    target = new Icon(
      createOptions({
        ...source,
        img: undefined,
        src,
        imgSize: source.imgSize ? convertToSizeArray(source.imgSize) : undefined,
        anchor: source.anchor ? convertToCoordinateArray(source.anchor) : undefined,
        anchorOrigin: source.anchorOrigin ? convertEnumValue(source.anchorOrigin) : undefined
      })
    );
  }
  synchronizeImageStyle(target, source, context);

  context.connector.forceRender();

  return target;
}

export function synchronizeText(target, source, context) {
  if (!target) {
    target = new Text();
  }
  target.setFont(source.font);
  target.setOffsetX((source.offset && source.offset.x) || 0);
  target.setOffsetY((source.offset && source.offset.y) || 0);
  target.setScale(source.scale);
  target.setRotation(source.rotation);
  target.setRotateWithView(source.rotateWithView);
  target.setTextAlign(source.textAlign ? convertEnumValue(source.textAlign) : undefined);
  target.setTextBaseline(source.textBaseline ? convertEnumValue(source.textBaseline) : undefined);
  target.setFill(source.fill ? context.lookup.get(source.fill) : undefined);
  target.setStroke(source.stroke ? context.lookup.get(source.stroke) : undefined);
  target.setBackgroundFill(source.backgroundFill ? context.lookup.get(source.backgroundFill) : undefined);
  target.setBackgroundStroke(source.backgroundStroke ? context.lookup.get(source.backgroundStroke) : undefined);
  target.setPadding([source.padding, source.padding, source.padding, source.padding]);

  context.connector.forceRender();

  return target;
}

export function synchronizeStyle(target, source, context) {
  if (!target) {
    target = new Style();
  }

  target.setImage(source.image ? context.lookup.get(source.image) : undefined);
  target.setFill(source.fill ? context.lookup.get(source.fill) : undefined);
  target.setStroke(source.stroke ? context.lookup.get(source.stroke) : undefined);
  target.setText(source.text ? context.lookup.get(source.text) : undefined);

  context.connector.forceRender();

  return target;
}
