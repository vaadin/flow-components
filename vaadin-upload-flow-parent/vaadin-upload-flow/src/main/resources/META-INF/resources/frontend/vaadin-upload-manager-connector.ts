// @ts-expect-error
import { UploadManager } from '@vaadin/upload/vaadin-upload-manager.js';

/**
 * Connector element for UploadManager. This element is added as a virtual child
 * of the owner component and handles initialization of the client-side
 * UploadManager instance based on properties set from the server.
 *
 * Properties read from the element:
 * - managerId: Unique ID for the manager
 * - target: Upload URL (from attribute, auto-converted by Flow)
 * - maxFiles: Maximum number of files (optional)
 * - maxFileSize: Maximum file size in bytes (optional)
 * - accept: Accepted file types (optional)
 * - noAuto: Disable auto-upload (optional)
 *
 * Events dispatched to the owner (parent) element:
 * - upload-manager-file-remove: When a file is removed
 * - upload-manager-file-reject: When a file is rejected
 */
class UploadManagerConnector extends HTMLElement {
  public manager = new UploadManager();

  attributeChangedCallback(name: string, oldValue: string, newValue: string) {
    if (name === 'target' && oldValue !== newValue) {
      this.manager.target = newValue;
    }
  }

  static get observedAttributes() {
    return ['target'];
  }

  constructor() {
    super();
    // Get the owner element (parent in the DOM or the element that has this as virtual child)
    // For virtual children, we need to dispatch events to the owner component's element

    // Forward events to the owner element for server-side handling
    this.manager.addEventListener('file-remove', (e: CustomEvent) => {
      this.dispatchEvent(
        new CustomEvent('upload-manager-file-remove', {
          detail: { fileName: e.detail.file?.name },
          bubbles: false
        })
      );
    });

    this.manager.addEventListener('file-reject', (e: CustomEvent) => {
      this.dispatchEvent(
        new CustomEvent('upload-manager-file-reject', {
          detail: {
            fileName: e.detail.file?.name,
            errorMessage: e.detail.error
          },
          bubbles: false
        })
      );
    });
  }
}

customElements.define('vaadin-upload-manager-connector', UploadManagerConnector);
