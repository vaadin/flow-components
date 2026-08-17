import './env-setup.js';
import '@vaadin/time-picker/src/vaadin-time-picker.js';
import '../frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.js';
import type { TimePicker } from '@vaadin/time-picker/src/vaadin-time-picker.js';
import type {} from '@web/test-runner-mocha';

export type FlowTimePickerTime = {
  hours: number;
  minutes: number;
  seconds: number;
  milliseconds: number;
};

export type TimePickerConnector = {
  initLazy: (timePicker: TimePicker) => void;
  setLocale: (locale: string) => void;
};

export type FlowTimePicker = TimePicker & {
  $connector: TimePickerConnector;
};

type Vaadin = {
  Flow: {
    timepickerConnector: TimePickerConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;

export const timepickerConnector = Vaadin.Flow.timepickerConnector;

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
