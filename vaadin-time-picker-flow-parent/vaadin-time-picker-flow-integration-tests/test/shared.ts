import './env-setup.js';
import '@vaadin/time-picker/src/vaadin-time-picker.js';
import '../frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.ts';
import type {} from '@web/test-runner-mocha';
import type { FlowTimePicker } from '../frontend/generated/jar-resources/vaadin-time-picker/vaadin-time-picker-types.js';

export type {
  FlowTimePicker,
  FlowTimePickerTime,
  TimePickerConnector
} from '../frontend/generated/jar-resources/vaadin-time-picker/vaadin-time-picker-types.js';

export const timepickerConnector = window.Vaadin.Flow.timepickerConnector;

export function init(timePicker: FlowTimePicker): void {
  timepickerConnector.initLazy(timePicker);
}

/**
 * Returns the text shown in the input element, with the non-breaking spaces
 * that some locales use around the AM/PM token replaced with normal spaces.
 */
export function getInputValue(timePicker: FlowTimePicker): string {
  return timePicker.inputElement!.value.replace(/[\u00a0\u202f]/g, ' ');
}
