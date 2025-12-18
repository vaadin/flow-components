import { UploadOrchestrator } from '@vaadin/upload/src/vaadin-upload-orchestrator.js';

// Make UploadOrchestrator available globally for Flow to use
window.Vaadin = window.Vaadin || {};
window.Vaadin.UploadOrchestrator = UploadOrchestrator;
