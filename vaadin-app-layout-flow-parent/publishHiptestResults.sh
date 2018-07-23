#!/bin/bash

hiptest-publisher -c vaadin-app-layout-integration-tests/hiptest/hiptest-publisher.config -p "vaadin-app-layout-integration-tests/target/failsafe-reports/TEST-*hiptest.*.xml" --token=$1 --test-run-id=$2
