#!/bin/bash

hiptest-publisher -c hiptest-publisher.config --without=actionwords --overriden-templates=templates --filename-patter=%sIT.java --token=$1 --test-run-id=$2
