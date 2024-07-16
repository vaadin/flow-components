/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import '@polymer/polymer/lib/elements/custom-style.js';
const $_container = document.createElement('template');

$_container.innerHTML = `<style>
.image-collage-item {
  height: 100%;
  width: 100%;
  min-height: 300px;
  overflow: hidden;
  padding: 10px;
  background: #dedede;
  border: 5px solid #FFF;
  object-fit: cover;
}
</style>`;

document.head.appendChild($_container.content);