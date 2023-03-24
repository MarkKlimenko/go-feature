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
-jar build/libs/go-feature-1.0-SNAPSHOT.jar
```

## test request
```
curl --location --request POST 'localhost:8080/api/v1/feature-toggle/find' \
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
