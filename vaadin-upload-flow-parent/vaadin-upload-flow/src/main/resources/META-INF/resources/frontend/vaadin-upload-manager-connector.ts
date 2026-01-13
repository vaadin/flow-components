// @ts-expect-error
import { UploadManager } from '@vaadin/upload/vaadin-upload-manager.js';

/**
 * Connector element for UploadManager. This element is added as a virtual child
 * of the owner component and handles initialization of the client-side
 * UploadManager instance based on properties set from the server.
 *
 * Properties read from the element:
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

  set maxFiles(value: number) {
    this.manager.maxFiles = value;
  }

  set maxFileSize(value: number) {
    this.manager.maxFileSize = value;
  }

  set accept(value: string) {
    this.manager.accept = value;
  }

  set noAuto(value: boolean) {
    this.manager.noAuto = value;
  }

  constructor() {
    super();

    // Forward events to the connector element for server-side handling
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
