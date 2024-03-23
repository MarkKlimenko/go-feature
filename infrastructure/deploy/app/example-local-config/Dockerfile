FROM amazoncorretto:17.0.8-alpine

COPY /build/libs/app.jar ./
COPY /infrastructure/deploy/app/example-local-config/application.yml ./config/application.yml
COPY /infrastructure/deploy/app/example-local-config/data ./data

ENV JAVA_TOOL_OPTIONS "-Xms64M -Xmx256M"

CMD ["java","-jar","app.jar"]
