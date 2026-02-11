import Attribution from 'ol/control/Attribution.js';
import ScaleLine from 'ol/control/ScaleLine.js';
import Zoom from 'ol/control/Zoom.js';
import { convertEnumValue, createOptions } from './util.js';

export function synchronizeAttribution(target, source) {
  if (!target) {
    target = new Attribution(createOptions(source));
  }
  return target;
}

export function synchronizeScaleLine(target, source) {
  // Most properties are not mutable, so we recreate the control
  const options = createOptions({
    minWidth: source.minWidth,
    maxWidth: source.maxWidth,
    units: convertEnumValue(source.units),
    bar: source.displayMode === 'BAR',
    steps: source.scaleBarSteps,
    text: source.scaleBarTextVisible
  });
  return new ScaleLine(options);
}

export function synchronizeZoom(target, source) {
  if (!target) {
    target = new Zoom(createOptions(source));
  }
  return target;
}
