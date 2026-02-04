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
  const isBarMode = source.displayMode === 'BAR';

  if (!target) {
    const options = createOptions({
      minWidth: source.minWidth,
      maxWidth: source.maxWidth,
      units: convertEnumValue(source.units),
      bar: isBarMode,
      steps: source.scaleBarSteps,
      text: source.scaleBarTextVisible
    });
    return new ScaleLine(options);
  }

  target.values_['units'] = convertEnumValue(source.units);
  target.maxWidth_ = source.maxWidth || undefined;
  target.minWidth_ = source.minWidth;
  target.scaleBar_ = isBarMode;
  target.scaleBarSteps_ = source.scaleBarSteps;
  target.scaleBarText_ = source.scaleBarTextVisible;

  target.element.classList.toggle('ol-scale-line', !isBarMode);
  target.element.classList.toggle('ol-scale-bar', isBarMode);
  target.innerElement_.classList.toggle('ol-scale-line-inner', !isBarMode);
  target.innerElement_.classList.toggle('ol-scale-bar-inner', isBarMode);
  target.updateElement_();

  return target;
}

export function synchronizeZoom(target, source) {
  if (!target) {
    target = new Zoom(createOptions(source));
  }
  return target;
}
