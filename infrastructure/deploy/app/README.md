# Docker application infrastructure

## Application
### Build docker image
```shell
sdk use java 17.0.5-librca
./gradlew build

# for arm
docker build --platform linux/arm64 -f ./infrastructure/deploy/app/example-git-config/Dockerfile -t markklim/go-feature:latest-arm64 .
docker run --platform linux/arm64 -p 8081:8080 -d --name go-feature markklim/go-feature:latest-arm64
docker start go-feature
docker stop go-feature
docker rm go-feature

# for amd
docker build -f ./extra/deploy/app/Dockerfile -t markklim/go-feature:latest .
docker run -p 8081:8080 -d --name go-feature markklim/go-feature:latest
```