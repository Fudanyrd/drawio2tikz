#!/usr/bin/env bash

set -e

config_file="$PWD/.clang-format"
test -f "$config_file" || { echo "Error: .clang-format file not found in $PWD"; exit 1; }

# load source map
source src/test/resources/config.sh

# run clang-format on all java source files.
for directory in $source_directories; do
  clang-format --style="file:$config_file" --dry-run --Werror "$directory"/*.java
done
for script in $scripts; do
  clang-format --style="file:$config_file" --dry-run --Werror "$script"
done

exit 0
