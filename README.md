# go-feature

## Start postgres for application
```
docker run --name go-feature-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres:15.2
```

## Build application
```
// or use other java 17 version
sdk use java 17.0.5-librca

./gradlew build
```

## Start application
```
java \
-Dapplication.loader.location=/Users/markklimenko/data \
-jar build/libs/go-feature-1.0-SNAPSHOT.jar
```

## Test request
```
curl --location --request POST 'localhost:8080/api/v1/features/search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "namespace": "default",
    "data": [
        {
            "parameter": "os",
            "value": "ios"
        },
        {
            "parameter": "userName",
            "value": "patrik"
        },
        {
            "parameter": "osVersion",
            "value": "13"
        }
    ]
}'

>>
{
    "features": [
        "enablePayments",
        "enableAdvancedScroll"
    ]
}
```

## Functionality (TBD)
- config loader
- storage
- feature-toggle/find functionality
