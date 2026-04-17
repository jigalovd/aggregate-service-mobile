#!/usr/bin/env bash
# Generate Kotlin Multiplatform DTOs from backend OpenAPI spec.
#
# Prerequisites: JDK 21+ on PATH or JAVA_HOME set
#
# Usage: ./scripts/generate-dtos.sh [path/to/openapi.json]
#
# Steps:
#   1. Run openapi-generator-cli (kotlin/multiplatform)
#   2. Post-process generated files for KMP compatibility
#   3. Copy to core/api-models module
set -euo pipefail

cd "$(git -C "$(dirname "$0")" rev-parse --show-toplevel 2>/dev/null || echo "$(dirname "$0")/..")"

SPEC="${1:-.openapi/openapi.json}"
OUT=".openapi/generated"
TARGET="core/api-models/src/commonMain/kotlin/com/aggregateservice/core/api/models"

if [ ! -f "$SPEC" ]; then
    echo "ERROR: Spec file not found: $SPEC"
    echo "Export from backend first: cd ../service-provider-aggregator/backend && make openapi-export"
    exit 1
fi

# Ensure JAVA_HOME
if [ -z "${JAVA_HOME:-}" ]; then
    # Try common locations
    for jdk in "/c/Users/jigal/.jdks/corretto-21.0.10" "/usr/lib/jvm/java-21" "/Library/Java/JavaVirtualMachines/jdk-21/Contents/Home"; do
        if [ -x "$jdk/bin/java" ] || [ -x "$jdk/bin/java.exe" ]; then
            export JAVA_HOME="$jdk"
            break
        fi
    done
fi

export PATH="${JAVA_HOME:?Set JAVA_HOME}/bin:$PATH"
echo "Using Java: $(java -version 2>&1 | head -1)"

# Generate
rm -rf "$OUT"
npx @openapitools/openapi-generator-cli@2.31.1 generate \
    -i "$SPEC" \
    -g kotlin \
    -o "$OUT" \
    --library multiplatform \
    --model-package com.aggregateservice.core.api.models \
    --additional-properties=serializableModel=true,dateLibrary=kotlinx-datetime,dateTimeLibrary=kotlinx.datetime \
    --skip-validate-spec \
    --global-property models="" \
    2>&1 | grep -E "(INFO|WARN|ERROR|Generating)" || true

# Post-process for KMP compatibility
SRC_DIR="$OUT/src/commonMain/kotlin/com/aggregateservice/core/api/models"
if [ ! -d "$SRC_DIR" ]; then
    echo "ERROR: Generated sources not found at $SRC_DIR"
    exit 1
fi

PROCESSED=0
for f in "$SRC_DIR"/*.kt; do
    [ -f "$f" ] || continue
    # Remove java.io.Serializable import and interface
    sed -i '/^import java\.io\.Serializable$/d' "$f"
    sed -i '/^import kotlinx\.serialization\.descriptors\./d' "$f"
    sed -i '/^import kotlinx\.serialization\.encoding\./d' "$f"
    # Remove @Required annotations
    sed -i 's/@Required //g' "$f"
    # Fix Null types (openapi-generator bug with anyOf + null)
    sed -i 's/: Null? = null/: kotlin.String? = null/g' "$f"
    sed -i 's/: Null = null/: kotlin.String? = null/g' "$f"
    # Remove Serializable interface
    sed -i 's/) : Serializable/)  /g' "$f"
    # Remove serialVersionUID companion object
    sed -i '/companion object {/,/}/{ /serialVersionUID/d; }' "$f"
    sed -i '/^[[:space:]]*companion object {$/{
        N
        /^[[:space:]]*companion object {\n[[:space:]]*}$/d
    }' "$f"
    # Fix kotlin.time.Instant -> kotlinx.datetime.Instant
    sed -i 's/kotlin\.time\.Instant/kotlinx.datetime.Instant/g' "$f"
    # Remove self-imports (same package)
    sed -i '/^import com\.aggregateservice\.core\.api\.models\./d' "$f"
    # Remove unnecessary empty lines (3+ consecutive -> 1)
    sed -i '/^[[:space:]]*$/N;/^\n[[:space:]]*$/D' "$f"
    PROCESSED=$((PROCESSED + 1))
done

echo "Post-processed $PROCESSED files"

# Copy to target
mkdir -p "$TARGET"
cp "$SRC_DIR"/*.kt "$TARGET/"
echo "Copied to $TARGET"

# Count
COUNT=$(ls "$TARGET"/*.kt 2>/dev/null | wc -l)
echo "Total DTOs: $COUNT"
echo "DONE"
