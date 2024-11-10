#!/bin/bash

./gradlew clean bootJar
docker build -t helene-supplies-webapp .
