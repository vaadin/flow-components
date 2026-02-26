#!/bin/bash
# Setup Maven/Gradle proxy authentication for Claude Code environments
# This script configures JVM build tools to work with authenticated proxies

# Only run in Claude Code environment (check for HTTPS_PROXY)
if [ -z "$HTTPS_PROXY" ]; then
    exit 0
fi

# Parse proxy URL to extract credentials
# Expected format: http://user:pass@host:port or https://user:pass@host:port
PROXY_URL="$HTTPS_PROXY"

# Extract username and password if present
if [[ "$PROXY_URL" =~ ://([^:]+):([^@]+)@([^:]+):([0-9]+) ]]; then
    PROXY_USER="${BASH_REMATCH[1]}"
    PROXY_PASS="${BASH_REMATCH[2]}"
    PROXY_HOST="${BASH_REMATCH[3]}"
    PROXY_PORT="${BASH_REMATCH[4]}"
elif [[ "$PROXY_URL" =~ ://([^:]+):([0-9]+) ]]; then
    PROXY_HOST="${BASH_REMATCH[1]}"
    PROXY_PORT="${BASH_REMATCH[2]}"
    PROXY_USER=""
    PROXY_PASS=""
else
    echo "Warning: Could not parse proxy URL format"
    exit 0
fi

# Create Maven settings.xml with proxy configuration
mkdir -p ~/.m2
cat > ~/.m2/settings.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <proxies>
    <proxy>
      <id>https-proxy</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>${PROXY_HOST}</host>
      <port>${PROXY_PORT}</port>
EOF

if [ -n "$PROXY_USER" ]; then
    cat >> ~/.m2/settings.xml << EOF
      <username>${PROXY_USER}</username>
      <password>${PROXY_PASS}</password>
EOF
fi

cat >> ~/.m2/settings.xml << EOF
    </proxy>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>${PROXY_HOST}</host>
      <port>${PROXY_PORT}</port>
EOF

if [ -n "$PROXY_USER" ]; then
    cat >> ~/.m2/settings.xml << EOF
      <username>${PROXY_USER}</username>
      <password>${PROXY_PASS}</password>
EOF
fi

cat >> ~/.m2/settings.xml << EOF
    </proxy>
  </proxies>
</settings>
EOF

# Create .mavenrc with wagon transport for Maven 3.9+ proxy auth compatibility
cat > ~/.mavenrc << 'EOF'
MAVEN_OPTS="$MAVEN_OPTS -Dmaven.resolver.transport=wagon"
EOF

# Configure Gradle proxy settings
mkdir -p ~/.gradle
cat > ~/.gradle/gradle.properties << EOF
systemProp.https.proxyHost=${PROXY_HOST}
systemProp.https.proxyPort=${PROXY_PORT}
systemProp.http.proxyHost=${PROXY_HOST}
systemProp.http.proxyPort=${PROXY_PORT}
EOF

if [ -n "$PROXY_USER" ]; then
    cat >> ~/.gradle/gradle.properties << EOF
systemProp.https.proxyUser=${PROXY_USER}
systemProp.https.proxyPassword=${PROXY_PASS}
systemProp.http.proxyUser=${PROXY_USER}
systemProp.http.proxyPassword=${PROXY_PASS}
EOF
fi

echo "JVM proxy configuration complete"
