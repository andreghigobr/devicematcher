.PHONY: help build clean test unit-test integration-test run run-uat run-local aerospike-start aerospike-stop docker-build docker-run docker-stop

# Variables
AEROSPIKE_CONTAINER=aerospike
AEROSPIKE_PORT=3000
AEROSPIKE_NAMESPACE=devicematcher
AEROSPIKE_CONFIG_FILE=config/aerospike.conf
APP_PORT=8080
APP_DOCKER_PORT=8080
APP_CONTAINER=devicematcher
SPRING_PROFILES_ACTIVE=local

# Default target
help:
	@echo "Available commands:"
	@echo "  make all              - clean, build, test, aerospike-start docker-run"
	@echo "  make build            - Build the application"
	@echo "  make clean            - Clean build artifacts"
	@echo "  make test             - Run tests"
	@echo "  make unit-test        - Run unit tests only"
	@echo "  make integration-test - Run integration tests only"
	@echo "  make aerospike-start  - Start Aerospike in Docker"
	@echo "  make aerospike-stop   - Stop Aerospike container"
	@echo "  make run              - Run application with default profile"
	@echo "  make run-uat          - Run application with UAT profile"
	@echo "  make run-local        - Run application locally"
	@echo "  make docker-build     - Build Docker image for the app"
	@echo "  make docker-run       - Start application in Docker container"
	@echo "  make docker-stop      - Stop application Docker container"

# Run all common tasks in sequence
all: clean build test aerospike-start docker-run
 @echo "Completed all tasks: clean, build, test, aerospike-start docker-run"

# Build the application
build:
	./gradlew build -x test

# Clean build artifacts
clean:
	./gradlew clean

# Run tests
test: aerospike-start
	./gradlew test

# Run unit tests only
unit-test:
	./gradlew unitTest

# Integration tests only
integration-test: aerospike-start
	./gradlew integrationTest

# Start Aerospike in Docker
aerospike-start:
	@echo "Starting Aerospike container..."
	@if [ "$$(docker ps -q -f name=$(AEROSPIKE_CONTAINER))" ]; then \
		echo "Aerospike is already running"; \
	else \
		if [ "$$(docker ps -aq -f status=exited -f name=$(AEROSPIKE_CONTAINER))" ]; then \
			docker start $(AEROSPIKE_CONTAINER); \
		else \
			docker run -d --name $(AEROSPIKE_CONTAINER) \
				-p $(AEROSPIKE_PORT):$(AEROSPIKE_PORT) \
				-e "NAMESPACE=$(AEROSPIKE_NAMESPACE)" \
				-v $$(pwd)/$(AEROSPIKE_CONFIG_FILE):/opt/aerospike/etc/aerospike.conf:ro \
				aerospike/aerospike-server; \
		fi; \
		echo "Waiting for Aerospike to start..."; \
		sleep 5; \
	fi

# Stop Aerospike container
aerospike-stop:
	@echo "Stopping Aerospike container..."
	@if [ "$$(docker ps -q -f name=$(AEROSPIKE_CONTAINER))" ]; then \
		docker stop $(AEROSPIKE_CONTAINER); \
	else \
		echo "Aerospike is not running"; \
	fi

# Run application with default profile
run: aerospike-start
	./gradlew bootRun

# Run application with UAT profile
run-uat: aerospike-start
	./gradlew bootRun --args="--spring.profiles.active=$(SPRING_PROFILES_ACTIVE)"

# Run application locally (alias for run)
run-local: run

# Build Docker image for the app
docker-build: build
	docker build -t $(APP_CONTAINER) .

# Start application in Docker container
docker-run: aerospike-start
	@echo "Starting application container..."
	@if [ "$$(docker ps -q -f name=$(APP_CONTAINER))" ]; then \
		echo "Application container is already running"; \
	else \
		if [ "$$(docker ps -aq -f status=exited -f name=$(APP_CONTAINER))" ]; then \
			docker start $(APP_CONTAINER); \
		else \
			docker run -d --name $(APP_CONTAINER) \
				-p $(APP_PORT):$(APP_DOCKER_PORT) \
				--network host \
				-e "SPRING_PROFILES_ACTIVE=$(SPRING_PROFILES_ACTIVE)" \
				$(APP_CONTAINER); \
		fi; \
	fi

# Stop application Docker container
docker-stop:
	@echo "Stopping application container..."
	@if [ "$$(docker ps -q -f name=$(APP_CONTAINER))" ]; then \
		docker stop $(APP_CONTAINER); \
	else \
		echo "Application container is not running"; \
	fi