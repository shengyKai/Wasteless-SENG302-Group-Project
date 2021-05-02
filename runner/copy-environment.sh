#!/bin/bash

# This script should be executed while in the /backend folder.

echo "Updating application.properties to the current environment"


sed -i -e "s/^spring.jpa.database-platform=.*$/spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect/" \
       -e "s/^spring.datasource.url=.*$/spring.datasource.url=$(echo $DB_URL | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/" \
       -e "s/^spring.datasource.username=.*$/spring.datasource.username=$(echo $DB_USERNAME | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/" \
       -e "s/^spring.datasource.password=.*$/spring.datasource.password=$(echo $DB_PASSWORD | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/" \
       -e "s/^dgaa.username=.*$/dgaa.username=$(echo $DGAA_EMAIL | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/" \
       -e "s/^dgaa.password=.*$/dgaa.password=$(echo $DGAA_PASSWORD | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')/" \
       ./src/main/resources/application.properties