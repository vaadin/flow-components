import './env-setup.js';
import '@vaadin/date-picker/vaadin-date-picker.js';
import '../frontend/generated/jar-resources/datepickerConnector.js';
import type { DatePicker } from '@vaadin/date-picker';
export { extractDateParts } from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';
import type {} from '@web/test-runner-mocha';

export type FlowDatePickerI18n = {
  dateFormats: string[];
  referenceDate?: string;
};

export type DatePickerConnector = {
  initLazy: (datePicker: DatePicker) => void;
  updateI18n: (locale: string, i18n: FlowDatePickerI18n) => void;
};

export type FlowDatePicker = DatePicker & {
  $connector: DatePickerConnector;
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
