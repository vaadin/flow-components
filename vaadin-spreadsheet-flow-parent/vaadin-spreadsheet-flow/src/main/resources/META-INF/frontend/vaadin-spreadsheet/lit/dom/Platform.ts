/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * What the sheet asks about the platform it runs on.
 */

/** Whether the page runs on a Mac, where shortcuts take Cmd rather than Ctrl. */
export function isMacPlatform(): boolean {
  return /Mac|iPhone|iPad|iPod/.test(navigator.platform);
}

/** The parts of the browser {@link isIOS} looks at. */
export interface PlatformInfo {
  userAgent: string;
  maxTouchPoints: number;
}

/**
 * Whether this is iOS or iPadOS, where Safari opens no context menu on a
 * long press: it fires no `contextmenu` event. iPadOS presents itself as a
 * Mac, and only its touch points tell it apart.
 */
export function isIOS(platform: PlatformInfo = navigator): boolean {
  return (
    /iPad|iPhone|iPod/.test(platform.userAgent) || (/Macintosh/.test(platform.userAgent) && platform.maxTouchPoints > 1)
  );
}
