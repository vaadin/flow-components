import './env-setup.js';
import '@vaadin/date-picker/src/vaadin-date-picker.js';
import '../frontend/generated/jar-resources/datepickerConnector.js';
import type { DatePicker, DatePickerDateMetadata } from '@vaadin/date-picker/src/vaadin-date-picker.js';
export { extractDateParts } from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';
import type {} from '@web/test-runner-mocha';

export type FlowDatePickerI18n = {
  dateFormats: string[];
  referenceDate?: string;
};

export type FlowDatePickerDateMetadataConfig = {
  /** Disabled dates as `[year, month, day]` triples, with a 0-based month. */
  disabledDates?: [number, number, number][];
  /** Disabled weekdays as ISO weekday numbers, Monday = 1 ... Sunday = 7. */
  disabledWeekdays?: number[];
  /** Whether a date metadata provider is set on the server. */
  hasProvider?: boolean;
};

export type DatePickerConnector = {
  initLazy: (datePicker: DatePicker) => void;
  updateI18n: (locale: string, i18n: FlowDatePickerI18n) => void;
  setDateMetadataConfig: (config: FlowDatePickerDateMetadataConfig) => void;
};

export type DatePickerServer = {
  /** Months are 0-based, matching what the web component passes to the provider. */
  requestDateMetadata: (
    startYear: number,
    startMonth: number,
    startDay: number,
    endYear: number,
    endMonth: number,
    endDay: number
  ) => Promise<DatePickerDateMetadata[]>;
};

export type FlowDatePicker = DatePicker & {
  $connector: DatePickerConnector;
  // Optional, so that tests can cover the connector running before the server connection is ready.
  $server?: DatePickerServer;
};

type Vaadin = {
  Flow: {
    datepickerConnector: DatePickerConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;

export const datepickerConnector = Vaadin.Flow.datepickerConnector;

export function init(datePicker: FlowDatePicker): void {
  datepickerConnector.initLazy(datePicker);
}
