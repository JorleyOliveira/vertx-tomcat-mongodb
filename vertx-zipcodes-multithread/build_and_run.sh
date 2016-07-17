#!/usr/bin/env bash
mvn package -DskipTests; java -jar target/vertx-zipcodes-multithread-1.0-SNAPSHOT-fat.jar
