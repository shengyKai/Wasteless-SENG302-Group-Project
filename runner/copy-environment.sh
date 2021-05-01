#!/bin/bash

# This script should be executed while in the /backend folder.

echo "Updating application.properties to the current environment"


sed -i -e "s/^spring.jpa.database-platform=.*$/spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect/" \
       -e "s/^spring.datasource.url=.*$/spring.datasource.url=$DB_URL/" \
       -e "s/^spring.datasource.username=.*$/spring.datasource.username=$DB_USERNAME/" \
       -e "s/^spring.datasource.password=.*$/spring.datasource.password=$DB_PASSWORD/" \
       -e "s/^dgaa.username=.*$/dgaa.username=$DGAA_EMAIL" \
       -e "s/^dgaa.password=.*$/dgaa.password=$DGAA_PASSWORD" \
       ./src/main/resources/application.properties