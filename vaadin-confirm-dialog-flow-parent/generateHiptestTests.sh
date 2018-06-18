#!/bin/bash

hiptest-publisher -c vaadin-confirm-dialog-integration-tests/hiptest/hiptest-publisher.config --only=tests --overriden-templates=vaadin-confirm-dialog-integration-tests/hiptest/templates --filename-pattern=%sIT.java --token=$1 --test-run-id=$2
