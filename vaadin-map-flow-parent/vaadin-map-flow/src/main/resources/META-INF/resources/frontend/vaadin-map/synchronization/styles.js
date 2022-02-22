import Fill from "ol/style/Fill";
import Stroke from "ol/style/Stroke";
import Style from "ol/style/Style";
import { Icon } from "ol/style";
import {
  convertToCoordinateArray,
  convertToSizeArray,
  createOptions,
} from "./util";

export function synchronizeFill(target, source, _context) {
  if (!target) {
    target = new Fill();
  }

  target.setColor(source.color);

  return target;
}

export function synchronizeStroke(target, source, _context) {
  if (!target) {
    target = new Stroke();
  }

  target.setColor(source.color);
  target.setWidth(source.width);

  return target;
}

function synchronizeImageStyle(target, source, _context) {
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

  return target;
}
