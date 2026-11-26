/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.user.client.impl;


/**
 * Native implementation associated with
 * {@link com.google.gwt.user.client.Window}.
 */
public class WindowImpl {

  public native String getHash() /*-{
    return $wnd.location.hash;
  }-*/;

  public native String getQueryString() /*-{
    return $wnd.location.search;
  }-*/;
  
  /**
   * Hooks the browser events that notify the {@code Window} closing and close
   * handlers. The close handlers run on {@code pagehide} when the page is not
   * kept in the back/forward cache.
   */
  public native void initWindowCloseHandler() /*-{
    var oldOnBeforeUnload = $wnd.onbeforeunload;
    
    // Old mozilla doesn't like $entry's explicit return statement and
    // will always pop up a confirmation dialog.  This is worked around by
    // just wrapping the call to onClosing(), which still has the semantics
    // that we want.
    $wnd.onbeforeunload = function(evt) {
      var ret, oldRet;
      try {
        ret = $entry(@com.google.gwt.user.client.Window::onClosing())();
      } finally {
        oldRet = oldOnBeforeUnload && oldOnBeforeUnload(evt);
      }
      // Avoid returning null as IE6 will coerce it into a string.
      // Ensure that "" gets returned properly.
      if (ret != null) {
        return ret;
      }
      if (oldRet != null) {
        return oldRet;
      }
      // returns undefined.
    };
    
    // spreadsheet: listen to pagehide instead of unload. Browsers are
    // removing support for unload, and Chrome reports a permissions policy
    // violation in the console for every page that registers an unload
    // handler.
    var onPageHide = $entry(function(evt) {
      if (evt.persisted) {
        // The page is kept in the back/forward cache and can be restored, so
        // the widgets must stay attached.
        return;
      }
      $wnd.removeEventListener('pagehide', onPageHide);
      try {
        @com.google.gwt.user.client.Window::onClosed()();
      } finally {
        $wnd.onresize = null;
        $wnd.onscroll = null;
        $wnd.onbeforeunload = null;
      }
    });
    $wnd.addEventListener('pagehide', onPageHide);
  }-*/;

  /**
   * Hooks the browser resize event to notify the {@code Window} resize handlers.
   */
  public native void initWindowResizeHandler() /*-{
    var oldOnResize = $wnd.onresize;
    $wnd.onresize = $entry(function(evt) {
      try {
        @com.google.gwt.user.client.Window::onResize()();
      } finally {
        oldOnResize && oldOnResize(evt);
      }
    });
  }-*/;

  /**
   * Hooks the browser scroll event to notify the {@code Window} scroll handlers.
   */
  public native void initWindowScrollHandler() /*-{
    var oldOnScroll = $wnd.onscroll;
    $wnd.onscroll = $entry(function(evt) {
      try {
        @com.google.gwt.user.client.Window::onScroll()();
      } finally {
        oldOnScroll && oldOnScroll(evt);
      }
    });
  }-*/;
}
