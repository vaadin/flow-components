// Adds the gesture recognizer registry that is missing from the
// @vaadin/component-base gestures module's type declarations
import type {} from '@vaadin/component-base/src/gestures.js';

declare module '@vaadin/component-base/src/gestures.js' {
  /** The registered gesture recognizers by their event type, e.g. `tap` */
  export const gestures: Record<string, GestureRecognizer | undefined>;
}
