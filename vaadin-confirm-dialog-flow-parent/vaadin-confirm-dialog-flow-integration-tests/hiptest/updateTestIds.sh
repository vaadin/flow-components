#!/bin/bash

hiptest-publisher -c hiptest-publisher.config --without=actionwords --overriden-templates=templates --token=$1 --test-run-id=$2
