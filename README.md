# LEFT_OVERS
## Team Internal Error (500)

An application for buying and selling products which are close to their expiration date.

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

    cd backend
    ./gradlew bootRun

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