#!/usr/bin/sh
set -e

config_file="$PWD/.clang-format"
test -f "$config_file" || { echo "Error: .clang-format file not found in $PWD"; exit 1; }

clang-format --style="file:$config_file" --dry-run --Werror Gallery.java \
  src/main/java/edu/fudan/drawio2tikz/*.java \
  src/test/java/edu/fudan/drawio2tikz/*.java

exit 0
