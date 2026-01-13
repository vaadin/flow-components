import { UploadManager } from '@vaadin/upload/src/vaadin-upload-manager.js';

// Make UploadManager available globally for Flow to use
window.Vaadin = window.Vaadin || {};
window.Vaadin.Upload = window.Vaadin.Upload || {};
window.Vaadin.Upload.UploadManager = UploadManager;

// Registry to track UploadManager instances by unique ID using WeakRef
// This allows managers to be garbage collected when no longer referenced
const uploadManagers = new Map();

// FinalizationRegistry to clean up the map entry when manager is GC'd
const registry = new FinalizationRegistry((id) => {
  uploadManagers.delete(id);
});

/**
 * Creates a new UploadManager instance and registers it with the given ID.
 * The manager is stored using WeakRef so it can be garbage collected when
 * no longer referenced by components.
 * @param {string} id - Unique identifier for the manager
 * @param {Object} options - Configuration options for the UploadManager
 * @param {HTMLElement} [eventTarget] - Element to dispatch events to for server communication
 * @returns {UploadManager} The created manager instance
 */
function createUploadManager(id, options, eventTarget) {
  const manager = new UploadManager(options);
  uploadManagers.set(id, new WeakRef(manager));
  registry.register(manager, id);

  // Forward events to the event target element for server-side handling
  if (eventTarget) {
    manager.addEventListener('file-remove', (e) => {
      eventTarget.dispatchEvent(
        new CustomEvent('upload-manager-file-remove', {
          detail: { managerId: id, fileName: e.detail.file?.name }
        })
      );
    });

    manager.addEventListener('file-reject', (e) => {
      eventTarget.dispatchEvent(
        new CustomEvent('upload-manager-file-reject', {
          detail: {
            managerId: id,
            fileName: e.detail.file?.name,
            errorMessage: e.detail.error
          }
        })
      );
    });
  }

  return manager;
}

/**
 * Gets an UploadManager instance by its ID.
 * @param {string} id - The manager ID
 * @returns {UploadManager|undefined} The manager instance, or undefined if not found or GC'd
 */
window.Vaadin.Upload.UploadManager.getUploadManager = function (id) {
  const ref = uploadManagers.get(id);
  return ref?.deref();
};
