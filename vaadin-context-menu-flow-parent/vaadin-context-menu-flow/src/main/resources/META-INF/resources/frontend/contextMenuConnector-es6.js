import {GestureEventListeners} from '@polymer/polymer/lib/mixins/gesture-event-listeners.js';
import * as Gestures from '@polymer/polymer/lib/utils/gestures.js';
import './contextMenuConnector.js';

window.Vaadin.Flow.Legacy.GestureEventListeners = GestureEventListeners;
window.Vaadin.Flow.Legacy.Gestures = Gestures;
