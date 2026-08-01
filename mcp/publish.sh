#!/usr/bin/env bash
# Publishes the Shuttle MCP server to the MCP Registry.
#
# Usage (from the repo root):
#   ./mcp/publish.sh <version>
#   ./mcp/publish.sh 4.1.0
#
# Run this script every time SERVER_VERSION changes in ShuttleMcpServer.kt.
#
# Prerequisites:
#   - Docker Desktop is installed and running
#   - mcp-publisher is installed (brew install mcp-publisher)
#   - You are authenticated with GHCR (docker login ghcr.io -u grarcht)
#   - The ghcr.io/grarcht/shuttle-mcp package is set to Public on GitHub
#
# What this script does:
#   1. Updates mcp/server.json with the new version
#   2. Builds the Shuttle MCP shadow JAR
#   3. Builds and pushes a multi-platform (amd64 + arm64) Docker image to GHCR
#   4. Authenticates with the MCP Registry via GitHub
#   5. Publishes the server metadata to the MCP Registry

set -e

VERSION="$1"
IMAGE="ghcr.io/grarcht/shuttle-mcp"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ -z "$VERSION" ]; then
    echo "Error: version argument required."
    echo "Usage: $0 <version>"
    echo "Example: $0 4.1.0"
    exit 1
fi

echo "Publishing Shuttle MCP server version $VERSION"
echo ""
echo "Before continuing, confirm:"
echo "  1. SERVER_VERSION in mcp/src/main/kotlin/com/grarcht/shuttle/mcp/ShuttleMcpServer.kt is set to $VERSION"
echo "  2. Docker Desktop is running"
echo ""
read -rp "Press Enter to continue or Ctrl+C to cancel..."

# Update server.json version fields
echo ""
echo "Updating server.json..."
sed -i '' "s/\"version\": \".*\"/\"version\": \"$VERSION\"/" "$SCRIPT_DIR/server.json"
sed -i '' "s|\"$IMAGE:.*\"|\"$IMAGE:$VERSION\"|" "$SCRIPT_DIR/server.json"

# Build the shadow JAR
echo "Building shadow JAR..."
cd "$REPO_ROOT"
./gradlew :mcp:shadowJar

# Build and push multi-platform Docker image
echo "Building and pushing Docker image ($IMAGE:$VERSION)..."
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t "$IMAGE:$VERSION" \
    --push \
    "$SCRIPT_DIR"

# Authenticate with the MCP Registry and publish
echo "Authenticating with the MCP Registry..."
cd "$SCRIPT_DIR"
mcp-publisher login github

echo "Publishing to the MCP Registry..."
mcp-publisher publish

echo ""
echo "Done. Verify at:"
echo "  curl 'https://registry.modelcontextprotocol.io/v0.1/servers?search=io.github.grarcht/shuttle'"
