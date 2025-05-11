# DeviceMatcher

## Overview

DeviceMatcher is a backend service designed to identify and track devices based on their User-Agent strings. 
The service extracts device information such as OS Name (e.g. Windows, Linux, iPhone, Android), OS Version, browser name (e.g. Firefox, Chrome) and browser version 
to create unique device profiles. These profiles are useful for analytics, fraud detection, and personalized user experiences.

## Features

- Device identification based on User-Agent headers
- Track device usage through hit counts
- Query devices by operating system name
- Manage device profiles with create, read, and delete operations
- Persistence using Aerospike database

## API Documentation

The API documentation is available at [Swagger UI](http://localhost:8080/swagger-ui/index.html).

The API endpoints is available at docs/devicematcher.postman_collection.json.

## Technologies

- Java 21
- Spring Boot
- Aerospike NoSQL Database
- Gradle
- Docker
- TestContainers

## Setup Instructions

### Running Locally

### Running Unit Tests

### Running Integration Tests

### Prerequisites

- JDK 21+
- Docker
- Aerospike server (can be run via Docker)
- Gradle

### Setting up Aerospike

The application requires an Aerospike database. You can run it using Docker:

```bash
docker run -d \
  --name aerospike \
  -p 3000:3000 \
  -p 3001:3001 \
  -p 3003:3003 \
  -v $(pwd)/config/aerospike.conf:/opt/aerospike/etc/aerospike.conf:ro \
   aerospike/aerospike-server \
  --config-file /opt/aerospike/etc/aerospike.conf