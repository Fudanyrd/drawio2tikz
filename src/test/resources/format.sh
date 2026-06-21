#!/usr/bin/sh
set -e

config_file="$PWD/.clang-format"
test -f "$config_file" || { echo "Error: .clang-format file not found in $PWD"; exit 1; }

# run clang-format on all java source files.
clang-format -i --style="file:$config_file" Gallery.java

# main/*
clang-format -i --style="file:$config_file" src/main/java/edu/fudan/drawio2tikz/*.java

# test/*
clang-format -i --style="file:$config_file" src/test/java/edu/fudan/drawio2tikz/*.java
exit 0
