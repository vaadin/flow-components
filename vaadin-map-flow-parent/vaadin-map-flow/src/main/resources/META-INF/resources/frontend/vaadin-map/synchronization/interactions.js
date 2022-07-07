import Collection from "ol/Collection";
import DragRotate from "ol/interaction/DragRotate";
import DoubleClickZoom from "ol/interaction/DoubleClickZoom";
import DragPan from "ol/interaction/DragPan";
import PinchRotate from "ol/interaction/PinchRotate";
import PinchZoom from "ol/interaction/PinchZoom";
import KeyboardPan from "ol/interaction/KeyboardPan";
import KeyboardZoom from "ol/interaction/KeyboardZoom";
import MouseWheelZoom from "ol/interaction/MouseWheelZoom";
import DragZoom from "ol/interaction/DragZoom";
import Translate from "ol/interaction/Translate";

function synchronizeInteraction(target, source, _context) {
  if (!target) {
    throw new Error("Can not instantiate base class: ol/interaction/Interaction");
  }

  target.setActive(source.active);

  return target;
}

export function synchronizeDragRotate(target, source, context) {
  if (!target) {
    target = new DragRotate();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizeDoubleClickZoom(target, source, context) {
  if (!target) {
    target = new DoubleClickZoom();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizeDragPan(target, source, context) {
  if (!target) {
    target = new DragPan();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizePinchRotate(target, source, context) {
  if (!target) {
    target = new PinchRotate();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizePinchZoom(target, source, context) {
  if (!target) {
    target = new PinchZoom();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizeKeyboardPan(target, source, context) {
  if (!target) {
    target = new KeyboardPan();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizeKeyboardZoom(target, source, context) {
  if (!target) {
    target = new KeyboardZoom();
  }

  synchronizeInteraction(target, source);

  return target;
}
export function synchronizeMouseWheelZoom(target, source, context) {
  if (!target) {
    target = new MouseWheelZoom();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizeDragZoom(target, source, context) {
  if (!target) {
    target = new DragZoom();
  }

  synchronizeInteraction(target, source);

  return target;
}

export function synchronizeTranslate(target, source, context) {
	
	console.log('--> synchronizeTranslate');

	if (!target) {

		var feature = context.lookup.get(source.feature);
		var map = context.lookup.get("ol/Map");

		target = new Translate({
			features: new Collection([feature]),
			// style: null,
			// pixelTolerance: 20
			// TODO read values from "Options"
		});

		// Translateend event 
		target.on('translateend', function(e) {
			console.log('--> Translateend');
			console.log("feature id is", feature.id);
			console.log("event id is", e.features.item(0).id);
			
			const TranslateendEvent = new CustomEvent('map-marker-drop', {
				detail: {
					featureId: feature.id,
					coordinate: feature.getGeometry().getCoordinates()
				}
			});

			map.dispatchEvent(TranslateendEvent);
			console.log('<-- Translateend');
		}, feature);
  }
  
  synchronizeInteraction(target, source);
  
  console.log('<-- synchronizeTranslate');
  
  return target;
}
