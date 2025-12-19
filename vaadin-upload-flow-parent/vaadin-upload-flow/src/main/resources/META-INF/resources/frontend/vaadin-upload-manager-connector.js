import { UploadManager } from '@vaadin/upload/src/vaadin-upload-manager.js';

// Make UploadManager available globally for Flow to use
window.Vaadin = window.Vaadin || {};
window.Vaadin.Upload = window.Vaadin.Upload || {};
window.Vaadin.Upload.UploadManager = UploadManager;

// Registry to track UploadManager instances by unique ID
const uploadManagers = new Map();

/**
 * Creates a new UploadManager instance and registers it with the given ID.
 * @param {string} id - Unique identifier for the manager
 * @param {Object} options - Configuration options for the UploadManager
 * @param {HTMLElement} [eventTarget] - Element to dispatch events to for server communication
 * @returns {UploadManager} The created manager instance
 */
window.Vaadin.Upload.UploadManager.createUploadManager = function (id, options, eventTarget) {
  const manager = new UploadManager(options);
  uploadManagers.set(id, manager);

  // Forward events to the event target element for server-side handling
  if (eventTarget) {
    manager.addEventListener('file-remove', (e) => {
      eventTarget.dispatchEvent(
        new CustomEvent('upload-manager-file-remove', {
          detail: { managerId: id, fileName: e.detail.file?.name },
        }),
      );
    });

    manager.addEventListener('file-reject', (e) => {
      eventTarget.dispatchEvent(
        new CustomEvent('upload-manager-file-reject', {
          detail: {
            managerId: id,
            fileName: e.detail.file?.name,
            errorMessage: e.detail.error,
          },
        }),
      );
    });
  }

  return manager;
};

/**
 * Gets an UploadManager instance by its ID.
 * @param {string} id - The manager ID
 * @returns {UploadManager|undefined} The manager instance, or undefined if not found
 */
window.Vaadin.Upload.UploadManager.getUploadManager = function (id) {
  return uploadManagers.get(id);
};

/**
 * Removes and destroys an UploadManager instance by its ID.
 * @param {string} id - The manager ID
 */
window.Vaadin.Upload.UploadManager.removeUploadManager = function (id) {
  const manager = uploadManagers.get(id);
  if (manager) {
    manager.destroy();
    uploadManagers.delete(id);
  }
};
