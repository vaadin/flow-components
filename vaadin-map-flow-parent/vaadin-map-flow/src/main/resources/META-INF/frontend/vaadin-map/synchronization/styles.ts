/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
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
import ImageState from 'ol/ImageState';
import { Icon } from 'ol/style';
import type { IconOrigin } from 'ol/style/Icon';
import type { ColorLike } from 'ol/colorlike';
import type ImageStyle from 'ol/style/Image';
import type { Size } from 'ol/size';
import type { MapSyncContext } from '../vaadin-map-types.js';
import type {
  FillChange,
  IconChange,
  ImageStyleChange,
  StrokeChange,
  StyleChange,
  TextStyleChange
} from './synchronization-types.js';
import { convertEnumValue, convertToCoordinateArray, createOptions } from './util.ts';

export function synchronizeFill(target: Fill | undefined, source: FillChange, context: MapSyncContext): Fill {
  if (!target) {
    target = new Fill();
  }

  target.setColor(source.color ?? null);

  context.connector.forceRender();

  return target;
}

export function synchronizeStroke(target: Stroke | undefined, source: StrokeChange, context: MapSyncContext): Stroke {
  if (!target) {
    target = new Stroke();
  }

  // OL declares Stroke.setColor to require a color, but treats undefined as unsetting the color
  target.setColor((source.color ?? undefined) as ColorLike);
  target.setWidth(source.width ?? undefined);

  context.connector.forceRender();

  return target;
}

function synchronizeImageStyle(
  target: ImageStyle | undefined,
  source: ImageStyleChange,
  _context?: MapSyncContext
): ImageStyle {
  if (!target) {
    throw new Error('Can not instantiate base class: ol/style/Image');
  }

  target.setOpacity(source.opacity);
  target.setRotateWithView(source.rotateWithView);
  target.setRotation(source.rotation);
  target.setScale(source.scale);

  return target;
}

/**
 * The scale that was last requested for icons whose image is still loading,
 * and which therefore can not be sized yet
 */
const pendingIconScales = new WeakMap<Icon, number>();

function calculateIconScale(target: Icon, source: IconChange, scale: number): number | Size {
  const [imageWidth, imageHeight] = target.getSize();
  const scaleX = source.width != null ? source.width / imageWidth : source.height! / imageHeight;
  const scaleY = source.height != null ? source.height / imageHeight : scaleX;
  return scaleX === scaleY ? scaleX * scale : [scaleX * scale, scaleY * scale];
}

/**
 * Applies the icon's width and height by scaling the image to that size, with
 * the configured scale applied on top. OL's own width and height options can
 * not be combined with a scale, and only apply when the icon is created.
 */
function synchronizeIconScale(target: Icon, source: IconChange, context: MapSyncContext) {
  if (source.width == null && source.height == null) {
    return;
  }

  if (target.getImageState() === ImageState.LOADED) {
    target.setScale(calculateIconScale(target, source, source.scale));
    return;
  }

  // The image size is only known once the image has loaded, so remember the
  // latest scale and apply it when loading has finished
  const isLoading = pendingIconScales.has(target);
  pendingIconScales.set(target, source.scale);
  if (isLoading) {
    return;
  }
  const onImageChange = () => {
    const imageState = target.getImageState();
    if (imageState !== ImageState.LOADED && imageState !== ImageState.ERROR) {
      return;
    }
    target.unlistenImageChange(onImageChange);
    const scale = pendingIconScales.get(target)!;
    pendingIconScales.delete(target);
    if (imageState === ImageState.LOADED) {
      target.setScale(calculateIconScale(target, source, scale));
      context.connector.forceRender();
    }
  };
  target.listenImageChange(onImageChange);
  target.load();
}

export function synchronizeIcon(target: Icon | undefined, source: IconChange, context: MapSyncContext): Icon {
  if (!target) {
    const src = source.img || source.src;
    target = new Icon(
      createOptions({
        ...source,
        img: undefined,
        src: src ?? undefined,
        // Width and height are applied through the scale, see synchronizeIconScale
        width: undefined,
        height: undefined,
        anchor: source.anchor ? convertToCoordinateArray(source.anchor) : undefined,
        anchorOrigin: source.anchorOrigin ? (convertEnumValue(source.anchorOrigin) as IconOrigin) : undefined
      })
    );
  }
  synchronizeImageStyle(target, source, context);
  synchronizeIconScale(target, source, context);

  context.connector.forceRender();

  return target;
}

export function synchronizeText(target: Text | undefined, source: TextStyleChange, context: MapSyncContext): Text {
  if (!target) {
    target = new Text();
  }
  target.setFont(source.font ?? undefined);
  target.setOffsetX((source.offset && source.offset.x) || 0);
  target.setOffsetY((source.offset && source.offset.y) || 0);
  target.setScale(source.scale);
  target.setRotation(source.rotation);
  target.setRotateWithView(source.rotateWithView);
  target.setTextAlign(source.textAlign ? (convertEnumValue(source.textAlign) as CanvasTextAlign) : undefined);
  target.setTextBaseline(
    source.textBaseline ? (convertEnumValue(source.textBaseline) as CanvasTextBaseline) : undefined
  );
  target.setFill(source.fill ? context.lookup.get(source.fill) : undefined);
  target.setStroke(source.stroke ? context.lookup.get(source.stroke) : undefined);
  target.setBackgroundFill(source.backgroundFill ? context.lookup.get(source.backgroundFill) : undefined);
  target.setBackgroundStroke(source.backgroundStroke ? context.lookup.get(source.backgroundStroke) : undefined);
  target.setPadding([source.padding, source.padding, source.padding, source.padding]);

  context.connector.forceRender();

  return target;
}

export function synchronizeStyle(target: Style | undefined, source: StyleChange, context: MapSyncContext): Style {
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
