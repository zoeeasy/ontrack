#!/bin/bash

set -e -x

pushd repo-code
    ./gradlew \
        clean \
        versionDisplay \
        versionFile \
        test \
        integrationTest \
        build \
        -PbowerOptions='--allow-root' \
        -Dorg.gradle.jvmargs=-Xmx1536m \
        -Pdocumentation \
        --info \
        --stacktrace \
        --profile \
        --console plain
popd
