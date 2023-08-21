# Docker application infrastructure

## Application
### Build docker image
```shell
./gradlew build

# for arm
docker build --platform linux/arm64 -f ./extra/deploy/app/Dockerfile -t markklim/go-feature:latest-arm64 .
docker run --platform linux/arm64 -p 8081:8080 -d --name go-feature markklim/go-feature:latest-arm64

# for amd
docker build -f ./extra/deploy/app/Dockerfile -t markklim/go-feature:latest .
docker run -p 8081:8080 -d --name go-feature markklim/go-feature:latest
```