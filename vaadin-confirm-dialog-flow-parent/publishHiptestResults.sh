#!/bin/bash

hiptest-publisher -c vaadin-confirm-dialog-integration-tests/hiptest/hiptest-publisher.config -p "vaadin-confirm-dialog-integration-tests/target/failsafe-reports/TEST-*hiptest.*.xml" --token=$1 --test-run-id=$2
