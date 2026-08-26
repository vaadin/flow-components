import Attribution from 'ol/control/Attribution.js';
import ScaleLine from 'ol/control/ScaleLine.js';
import Zoom from 'ol/control/Zoom.js';
import type { Units } from 'ol/control/ScaleLine.js';
import type { MapChangeBase, ScaleLineChange } from './synchronization-types.js';
import { convertEnumValue, createOptions } from './util.ts';

export function synchronizeAttribution(target: Attribution | undefined, source: MapChangeBase): Attribution {
  if (!target) {
    target = new Attribution(createOptions(source) as object);
  }
  return target;
}

export function synchronizeScaleLine(target: ScaleLine | undefined, source: ScaleLineChange): ScaleLine {
  // Most properties are not mutable, so we recreate the control
  const options = createOptions({
    minWidth: source.minWidth,
    maxWidth: source.maxWidth,
    units: convertEnumValue(source.units) as Units,
    bar: source.displayMode === 'BAR',
    steps: source.scaleBarSteps,
    text: source.scaleBarRatioVisible
  });
  return new ScaleLine(options);
}

export function synchronizeZoom(target: Zoom | undefined, source: MapChangeBase): Zoom {
  if (!target) {
    target = new Zoom(createOptions(source) as object);
  }
  return target;
}
