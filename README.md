# go-feature

## Start postgres for application
```shell
docker run --name go-feature-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres:15.2
```

## Build application
```shell
# or use other java 17 version
sdk use java 17.0.5-librca

./gradlew build
```

## Start application
```shell
java \
-Dapplication.loader.location=/Users/markklimenko/data \
-jar build/libs/go-feature-1.0.1.jar
```

## Documentation


## Functionality (TBD)
- config loader
- storage
- feature-toggle/find functionality
- headers X-B3-TraceId/X-B3-SpanId
- localization

## Feature TODO
+ обновить логгер
- запустить приложение в доккере при помощи ансибл или подобного
  + docker file
  + memory usage
  + properties
  + addition files
  - create extended dataset
- запустить инфру для тестирования
- снять метрики с приложения
- написать скрипт для нагрузочного тестирования и запустить