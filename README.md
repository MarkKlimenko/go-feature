# go-feature

## start postgres
```
docker run --name go-feature-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres:15.2
```

## build application
```
./gradlew build
```

## start application
```
java \
-Dapplication.loader.location=/Users/markklimenko/data \
-Dapplication.loader.force-update=true \
-jar build/libs/go-feature-1.0-SNAPSHOT.jar
```