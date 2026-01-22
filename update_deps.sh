#!/bin/bash

# this needs jq installed

./gradlew dependencyUpdates

# all dependencies
# find . -type f -path '*dependencyUpdates/report.json' -exec jq '.outdated.dependencies' {} \;

# filter out dependencies from plugins
find . -type f -path '*dependencyUpdates/report.json' \
-exec jq '.outdated.dependencies[]' {} \;
