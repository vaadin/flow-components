import './env-setup.js';
import '@vaadin/date-picker/src/vaadin-date-picker.js';
import '../frontend/generated/jar-resources/vaadin-date-picker/datepickerConnector.ts';
export { extractDateParts } from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';
import type {} from '@web/test-runner-mocha';
import type { FlowDatePicker } from '../frontend/generated/jar-resources/vaadin-date-picker/vaadin-date-picker-types.js';

export type {
  DatePickerConnector,
  DatePickerServer,
  DateMetadataConfig as FlowDatePickerDateMetadataConfig,
  FlowDatePicker,
  FlowDatePickerI18n
} from '../frontend/generated/jar-resources/vaadin-date-picker/vaadin-date-picker-types.js';

export const datepickerConnector = window.Vaadin.Flow.datepickerConnector;

export function init(datePicker: FlowDatePicker): void {
  datepickerConnector.initLazy(datePicker);
}
