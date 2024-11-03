# Introduction

Welcome to Cymo Academy.

This code is the code base of the two-day Cymo Kafka 101 developer course

# How to get started

Each `scenario` is completely isolated and work independent of each other.  
Even if your previous scenario was not successful, it will have no impact on the next one.

## Requirements

* Maven 3+
* Java 17+
* Docker (optional)

## Day 1

Focus on Spring Kafka

| Scenario   | Optional | Starting point               | 
|------------|----------|------------------------------|
| Scenario 1 |          | [pom.xml](scenario1/pom.xml) |
| Scenario 2 |          | [pom.xml](scenario2/pom.xml) |
| Scenario 3 |          | [pom.xml](scenario3/pom.xml) |
| Scenario 4 | X        | [pom.xml](scenario4/pom.xml) |

## Day 2

Focus on Kafka Streams

| Scenario   | Optional | Starting point               | 
|------------|----------|------------------------------|
| Scenario 5 |          | [pom.xml](scenario5/pom.xml) |
| Scenario 6 |          | [pom.xml](scenario6/pom.xml) |
| Scenario 7 |          | [pom.xml](scenario7/pom.xml) |
| Scenario 8 | X        | [pom.xml](scenario8/pom.xml) |


# Extra tools for local development

In case you want to continue developing with these scenario's, you can use the `docker-compose.yml`.

All the required dependencies are mapped into a single `docker-compose.yml`

Make sure you are in the `root folder` of the `academy-developer` project

```shell
docker compose up -d
```

All the dependencies & helper-tools should be available & running with the following settings:

| Tools              | accessible     | url                            |
|--------------------|----------------|--------------------------------|
| redpanda-console   | localhost:8080 | http://localhost:8080/overview |
| schema-registry    | localhost:8081 | -                              |
| brokers (/w kraft) | localhost:9092 | -                              |