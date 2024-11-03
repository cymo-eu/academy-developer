Welcome to the Cymo Academy course.

This code is the code base of the two-day Cymo Academy Developer course

# How to get started

All the dependencies are mapped into a single `docker-compose.yml`

Make sure you are in the `root folder` of the `academy-developer` project

```shell
docker compose up -d
```

All the dependencies should be available & running with the following settings:

| Tools            | accessible     | url                            |
|------------------|----------------|--------------------------------|
| redpanda-console | localhost:8080 | http://localhost:8080/overview |
| schema-registry  | localhost:8081 | -                              |
| brokers          | localhost:9092 | -                              |
