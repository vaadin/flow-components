import Attribution from 'ol/control/Attribution.js';
import ScaleLine from 'ol/control/ScaleLine.js';
import Zoom from 'ol/control/Zoom.js';
import { createOptions } from './util.js';

export function synchronizeAttribution(target, source) {
  if (!target) {
    target = new Attribution(createOptions(source));
  }
  return target;
}

export function synchronizeScaleLine(target, source) {
  if (!target) {
    target = new ScaleLine(createOptions(source));
  }
  return target;
}

export function synchronizeZoom(target, source) {
  if (!target) {
    target = new Zoom(createOptions(source));
  }
  return target;
}
