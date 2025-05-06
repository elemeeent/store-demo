#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/front"

echo "Starting frontend from directory ${SCRIPT_DIR} ..."

cd ${SCRIPT_DIR}

npm install --prefer-offline --force

npm run dev
