/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/** How long a press stays put to be a long press, as it has been in earlier versions. */
export const LONG_PRESS_MS = 750;

/** How far a finger may drift during a long press, in CSS pixels. */
const DRIFT_PX = 10;

/**
 * Makes a long press on the target a `contextmenu` event at the point pressed,
 * since Safari on iOS opens no menu of its own: one finger that stays put for
 * {@link LONG_PRESS_MS}. A press that moves, lifts, gets a second finger, or
 * meets a context menu event of the system's own is not one, so the menu never
 * opens twice.
 *
 * @returns a function that stops listening
 */
export function listenForLongPress(target: EventTarget, delayMs: number = LONG_PRESS_MS): () => void {
  let timer: ReturnType<typeof setTimeout> | null = null;
  let start: { x: number; y: number } | null = null;

  const cancel = (): void => {
    if (timer !== null) {
      clearTimeout(timer);
      timer = null;
    }
    start = null;
  };

  const onStart = (e: Event): void => {
    cancel();
    const touches = (e as TouchEvent).touches;
    if (touches.length !== 1) {
      return;
    }
    const pressed = e.composedPath()[0];
    const { clientX, clientY, screenX, screenY } = touches[0];
    start = { x: clientX, y: clientY };
    timer = setTimeout(() => {
      timer = null;
      start = null;
      pressed.dispatchEvent(
        new MouseEvent('contextmenu', {
          bubbles: true,
          composed: true,
          cancelable: true,
          clientX,
          clientY,
          screenX,
          screenY,
          button: 2
        })
      );
    }, delayMs);
  };

  const onMove = (e: Event): void => {
    if (start === null) {
      return;
    }
    const touch = (e as TouchEvent).touches[0];
    if (touch === undefined || Math.hypot(touch.clientX - start.x, touch.clientY - start.y) > DRIFT_PX) {
      cancel();
    }
  };

  // The system's own menu for the press, where there is one.
  const onMenu = (e: Event): void => {
    if (e.isTrusted) {
      cancel();
    }
  };

  const passive = { passive: true };
  target.addEventListener('touchstart', onStart, passive);
  target.addEventListener('touchmove', onMove, passive);
  target.addEventListener('touchend', cancel, passive);
  target.addEventListener('touchcancel', cancel, passive);
  target.addEventListener('contextmenu', onMenu);
  return () => {
    cancel();
    target.removeEventListener('touchstart', onStart);
    target.removeEventListener('touchmove', onMove);
    target.removeEventListener('touchend', cancel);
    target.removeEventListener('touchcancel', cancel);
    target.removeEventListener('contextmenu', onMenu);
  };
}
