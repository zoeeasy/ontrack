#!/bin/bash

set -e -x

pushd repo-code
    ./gradlew \
        clean \
        versionDisplay \
        versionFile \
        test \
        build \
        -PbowerOptions='--allow-root' \
        -Dorg.gradle.jvmargs=-Xmx1536m \
        --info \
        --stacktrace \
        --profile \
        --console plain
popd
