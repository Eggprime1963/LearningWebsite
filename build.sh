#!/bin/bash
# Vercel build script for Java application

echo "Building Java application..."

# Install OpenJDK 11
apt-get update
apt-get install -y openjdk-11-jdk maven

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Build the project
mvn clean package -DskipTests

# Create output directory for Vercel
mkdir -p public

# Extract WAR file contents to public directory
cd target
unzip -o learning-platform-1.0.0.war -d ../public/

echo "Build completed successfully!"
