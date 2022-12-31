/**
 * @license
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import Fill from "ol/style/Fill";
import Stroke from "ol/style/Stroke";
import Style from "ol/style/Style";
import Text from "ol/style/Text";
import { Icon } from "ol/style";
import {
  convertToCoordinateArray,
  convertToSizeArray,
  createOptions,
} from "./util";

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

export function synchronizeText(target, source, context) {
  if (!target) {
    target = new Text();
  }

  target.setFont( source.image ? context.lookup.get(source.image) : undefined );
  target.setOffsetX( source.offsetX );
  target.setOffsetY( source.offsetY );
  target.setOverflow( source.overflow );
  target.setPlacement( source.placement );
  target.setScale( source.scale );
  target.setRotateWithView( source.rotateWithView);
  target.setRotation( source.rotation);
  target.setText( source.text );
  target.setTextAlign( source.textAlign );
  target.setTextBaseline( source.textBaseline );
  target.setFill( source.fill ? context.lookup.get(source.fill) : undefined );
  target.setStroke( source.stroke ? context.lookup.get(source.stroke) : undefined );
  target.setBackgroundFill( source.backgroundFill ? context.lookup.get(source.backgroundFill) : undefined );
  target.setBackgroundStroke( source.backgroundStroke ? context.lookup.get(source.backgroundStroke) : undefined );

  return target;
}

function synchronizeImageStyle(target, source, context) {
  if (!target) {
    throw new Error("Can not instantiate base class: ol/style/Image");
  }

  target.setOpacity(source.opacity);
  target.setRotateWithView(source.rotateWithView);
  target.setRotation(source.rotation);
  target.setScale(source.scale);

  return target;
}

/**
 * Convert from Java enum value like `BOTTOM_LEFT` to OL anchor origin value like `bottom-left`
 * @param anchorOrigin
 * @returns {string}
 */
function convertAnchorOrigin(anchorOrigin) {
  return anchorOrigin.toLowerCase().replace(/_/, "-");
}

export function synchronizeIcon(target, source, context) {
  if (!target) {
    const src = source.img || source.src;
    target = new Icon(
      createOptions({
        ...source,
        img: undefined,
        src,
        imgSize: source.imgSize
          ? convertToSizeArray(source.imgSize)
          : undefined,
        anchor: source.anchor
          ? convertToCoordinateArray(source.anchor)
          : undefined,
        anchorOrigin: source.anchorOrigin
          ? convertAnchorOrigin(source.anchorOrigin)
          : undefined,
      })
    );
  }
  synchronizeImageStyle(target, source, context);

  context.connector.forceRender();

  return target;
}

export function synchronizeStyle(target, source, context) {
  if (!target) {
    target = new Style();
  }

  target.setImage(
    source.image
      ? context.lookup.get(source.image)
      : undefined
  );
  target.setFill(
    source.fill
      ? context.lookup.get(source.fill)
      : undefined
  );
  target.setStroke(
    source.stroke
      ? context.lookup.get(source.stroke)
      : undefined
  );
  
  target.setText(
	source.text
      ? context.lookup.get(source.text)
      : undefined
  );

  context.connector.forceRender();

  return target;
}
