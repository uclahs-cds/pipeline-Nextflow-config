#!/bin/bash
set -euo pipefail

# Get the parent directory of this script, resolving any symlinks along the way
# https://stackoverflow.com/a/246128
SOURCE=${BASH_SOURCE[0]}
# Resolve $SOURCE until the file is no longer a symlink
while [ -L "$SOURCE" ]; do
    SCRIPT_DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )
    SOURCE=$(readlink "$SOURCE")
    # If $SOURCE was a relative symlink, we need to resolve it relative to
    # the path where the symlink file was located
    [[ $SOURCE != /* ]] && SOURCE=$SCRIPT_DIR/$SOURCE
done
SCRIPT_DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )

REPO_DIR=$(dirname "$SCRIPT_DIR")
IMAGE="tool-nextflow-config-tester"

docker build "$SCRIPT_DIR" -t "$IMAGE"

docker run \
    -it \
    --rm \
    -v "$REPO_DIR":/pipeline \
    -w /pipeline/tests \
    --entrypoint nextflow \
    "$IMAGE" \
    suite.groovy
