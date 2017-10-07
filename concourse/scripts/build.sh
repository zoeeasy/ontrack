#!/bin/bash

set -e -x

repository=$(pwd)/delivery-repository

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
        -Pdelivery.repository=${repository} \
        --info \
        --stacktrace \
        --profile \
        --console plain
popd
