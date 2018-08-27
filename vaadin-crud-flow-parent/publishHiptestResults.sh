#!/bin/bash

hiptest-publisher -c vaadin-crud-integration-tests/hiptest/hiptest-publisher.config -p "vaadin-crud-integration-tests/target/failsafe-reports/TEST-*hiptest.*.xml" --token=$1 --test-run-id=$2
