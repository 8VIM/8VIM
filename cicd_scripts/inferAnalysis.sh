#!/bin/sh
./gradlew clean
infer run -- ./gradlew build
