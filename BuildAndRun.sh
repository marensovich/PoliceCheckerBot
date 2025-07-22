#!/bin/bash

IMAGE_NAME="police_checker_bot:1.1"
CONTAINER_NAME="PoliceCheckerBot"

docker build -t $IMAGE_NAME .

docker rm -f $CONTAINER_NAME 2>/dev/null || true
docker run --name $CONTAINER_NAME $IMAGE_NAME