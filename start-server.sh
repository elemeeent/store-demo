#!/bin/bash

echo "Starting server from directory ${SCRIPT_DIR} ..."

./gradlew clean build bootRun
