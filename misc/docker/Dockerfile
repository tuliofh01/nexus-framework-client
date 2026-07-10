# syntax=docker/dockerfile:1

# JDK 26 + CMake + Ninja for desktop template validation and CLI generation.
FROM eclipse-temurin:26-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    cmake \
    ninja-build \
    git \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching.
COPY gradlew gradlew.bat settings.gradle.kts gradle.properties build.gradle.kts ./
COPY gradle/ gradle/
COPY misc/build-logic/ misc/build-logic/
COPY misc/core/ misc/core/
COPY misc/cli/ misc/cli/
COPY template/ template/

RUN chmod +x gradlew

# Warm dependency cache (compile only — no generation at image build time).
RUN ./gradlew :core:compileKotlin :cli:compileKotlin --no-daemon

ENTRYPOINT ["./gradlew", ":cli:run", "--no-daemon"]
CMD ["--args=help"]
