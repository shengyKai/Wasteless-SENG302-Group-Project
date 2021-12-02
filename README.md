# LEFT_OVERS
## Team Internal Error (500) 

^^Yes that was our team name ^^\
An application for buying and selling products which are close to their expiration date.

## Deployed Application

The application is running at https://csse-s302g5.canterbury.ac.nz/prod.

You can create and account or login to the example account with the following credentials: \
Username: example@seng302.com \
Password: password123

You can also find the DGAA credentials on the git repository https://eng-git.canterbury.ac.nz/seng302-2021/team-500 by navigating to Settings -> CI/CD -> Variables. You must have maintainer access to the repository to do this.

## Database Setup

The application uses a Maria DB database. The database can be accessed by navigating to https://dbadmin.csse.canterbury.ac.nz/ and logging in with the username seng302-team500 and the server db2.csse.canterbury.ac.nz. Contact SENG302 Team 500 via s302team500@cosc.canterbury.ac.nz for the password. The database seng302-2021-team500-prod is used for the deployed application, while the database seng302-2021-team500-test is used for testing.


## Basic Project Structure

A frontend sub-project (web GUI):

- `frontend/src` Frontend source code
- `frontend/src/api` Endpoints for backend and external apis (Typescript)
- `frontend/src/components` Vue components of application UI (Vue.js)
- `frontend/src/plugins` State control and navigation (Typescript)
- `frontend/tests/unit` Jest tests (Typescript)
- `frontend/public` Publicly accessible web assets (e.g., icons, images, style sheets)
- `frontend/dist` Frontend production build

A backend sub-project (business logic and persistence server):

- `backend/src` Backend source code (Java - Spring)
- `backend/src/main/controllers` Controllers for API endpoints
- `backend/src/main/entities` The data model classes
- `backend/src/main/persistence` Classes for interfacing with the database
- `backend/src/main/service` Implementation of Spring services
- `backend/src/main/tools` Perform operations such as processing and validation
- `backend/src/test` JUnit and Cucumber tests for the Java application
- `backend/out` Backend production build

## How to run

### Frontend / GUI

    $ cd frontend
    $ npm install
    $ npm run serve

Running on: http://localhost:9500/ by default

### Backend / server

    $ cd backend
    $ ./gradlew bootRun

Running on: http://localhost:9499/ by default

## Contributors

- SENG302 Team 500
    - Connor Hitchcock
    - Edward Wong
    - Ella Johnson
    - Henry Barrett
    - Josh Egan
    - Nathan Smithies
    - Sheng-He Phua
  
## Sources for Example Data
 
first-names-female.txt, first-names-male.txt, last-names.txt \
Derived from data published by the US Census Bureau https://www.census.gov/topics/population/genealogy/data/1990_census/1990_census_namefiles.html \
Used under the Freedom of Information Act of the United States of America

nicknames.csv \
Derived from this dataset https://github.com/carltonnorthern/nickname-and-diminutive-names-lookup \
Used under the Apache-2.0 License https://github.com/carltonnorthern/nickname-and-diminutive-names-lookup/blob/master/License.txt

card-titles.txt \
Derived in part from "Jabberwocky" by Lewis Carroll, which is in the public domain.

## Generating Example Data

Generate data by sending a post request to the API endpoint `https://csse-s302g5.canterbury.ac.nz/prod/api/demo/generate`. Modify the body to decide how many of each entity to create.

example:
```
{
    "userCount"             : 10000,
    "businessCount"         : 1000,
    "productCount"          : 10000,
    "inventoryItemCount"    : 10000,
    "saleItemCount"         : 10000,
    "cardCount"             : 10000,
    "generateImages"        : true
    "generateBusinessImages": true,
    "businessImageMin": 1,
    "businessImageMax": 2
}
```

## User Manual
https://eng-git.canterbury.ac.nz/seng302-2021/team-500/-/wikis/uploads/c4f9dcb745da516d7e9f9cd6deade23f/LEFT_OVERS_User_Manual.docx
