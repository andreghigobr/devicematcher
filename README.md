# DeviceMatcher

## Overview

DeviceMatcher is a backend service designed to identify and track devices based on their User-Agent strings. 
The service extracts device information such as OS Name (e.g. Windows, Linux, iPhone, Android), OS Version, browser name (e.g. Firefox, Chrome) and browser version 
to create unique device profiles. These profiles are useful for analytics, fraud detection, and personalized user experiences.

## Features

- Device identification based on User-Agent headers
- Track device usage through hit counts
- Query devices by operating system name
- Manage device profiles: 
  - match by User-Agent
  - Get device by ID
  - delete device by ID
- Persistence using Aerospike database

## API Documentation

- [Swagger UI](http://localhost:8080/swagger-ui/index.html).
- [Postman Collection](docs/devicematcher.postman_collection.json).

## Technologies

- Java 21
- Spring Boot
- [Aerospike NoSQL Database](https://aerospike.com/)
- Gradle
- Docker
- TestContainers

## Setup Instructions

### Prerequisites

- JDK 21+
- Gradle 8+
- Docker
- [Aerospike](https://aerospike.com/) server 8.0.0 (can be run via Docker)

### Setting up Aerospike

The application requires an Aerospike database. 

```bash
docker pull aerospike/aerospike-server:latest
```

You can run it using Docker:

- make sure to be in the root directory of the project
- make sure aerospike default ports (3000 3001 3003) are available. 

```bash
docker run -d \
  --name aerospike \
  -p 3000:3000 \
  -p 3001:3001 \
  -p 3003:3003 \
  -v $(pwd)/config/aerospike.conf:/opt/aerospike/etc/aerospike.conf:ro \
   aerospike/aerospike-server \
  --config-file /opt/aerospike/etc/aerospike.conf
```
Verify Aerospike is running and namespace `devicematcher` is created:

```bash
docker exec -it aerospike asinfo -v "namespaces"
```

### Running Locally

1. Build 

```bash
./gradlew clean build -x test
```
2. Test the application

2.1. Run Unit Tests
```bash
./gradlew unitTest
```

2.2. Run Integration Tests
```bash
./gradlew integrationTest
```

3. Run the application
```bash
./gradlew bootRun -Dspring.profiles.active=local
```

4. Access the application at [http://localhost:8080](http://localhost:8080)

### Docker Image

if needed, build application docker image

```bash
docker build -t devicematcher:latest .
```

run the application in docker container

```bash
docker run -d --name devicematcher \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  --network=host \
  devicematcher:latest
```

### Makefile

A Makefile is provided for convenience. You can use it to run common tasks:

```bash
make all
```
This will run all tests, build the application, and start the server.