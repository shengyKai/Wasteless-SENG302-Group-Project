Feature: Location

    Scenario: Create a valid location from an address
        Given the address "69 Riccarton Road, Christchurch, Canterbury, New Zealand, 8041" does not exist
        When the address "69 Riccarton Road, Christchurch, Canterbury, New Zealand, 8041" is created
        Then the address "69 Riccarton Road, Christchurch, Canterbury, New Zealand, 8041" exists
        Then the address "69 Riccarton Road, Christchurch, Canterbury, New Zealand, 8041" has the street number "69"
        And has the street name "Riccarton Road"
        And has the city name "Christchurch"
        And has the region name "Canterbury"
        And has the country name "New Zealand"
        And has the post code "8041"

    Scenario: Create a second valid location from an address
        Given the address "100 Ocean View Crescent, Auckland, Rakino Island, New Zealand, 1010" does not exist
        When the address "100 Ocean View Crescent, Auckland, Rakino Island, New Zealand, 1010" is created
        Then the address "100 Ocean View Crescent, Auckland, Rakino Island, New Zealand, 1010" exists
        Then the address "100 Ocean View Crescent, Auckland, Rakino Island, New Zealand, 1010" has the street number "100"
        And has the street name "Ocean View Crescent"
        And has the city name "Auckland"
        And has the region name "Rakino Island"
        And has the country name "New Zealand"
        And has the post code "1010"