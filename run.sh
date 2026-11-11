#!/usr/bin/env bash
# Compile + test + demo. Nol dependency: cukup JDK 17+ di PATH.
set -euo pipefail
cd "$(dirname "$0")"

OUT=build/classes
mkdir -p "$OUT" build
find src -name "*.java" ! -name "*JUnitTest.java" > build/sources.txt

echo "[1/3] compile ($(wc -l < build/sources.txt) file java)"
javac -encoding UTF-8 -Xlint:all -d "$OUT" @build/sources.txt

echo
echo "[2/3] test aturan antrean"
java -Dstdout.encoding=UTF-8 -cp "$OUT" id.nxtup.queue.QueueEngineTest

echo
echo "[3/3] simulasi sesi mabar"
java -Dstdout.encoding=UTF-8 -cp "$OUT" id.nxtup.queue.Demo
