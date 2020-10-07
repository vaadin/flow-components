import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { timeOut } from '@polymer/polymer/lib/utils/async.js';
import './comboBoxConnector.js';
import { ComboBoxPlaceholder } from '@vaadin/vaadin-combo-box/src/vaadin-combo-box-placeholder.js';

window.Vaadin.Flow.Legacy.Debouncer = Debouncer;
window.Vaadin.Flow.Legacy.timeOut = timeOut;

window.Vaadin.ComboBoxPlaceholder = ComboBoxPlaceholder;
