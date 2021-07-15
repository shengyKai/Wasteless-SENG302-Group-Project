Feature: UCM6 - Keyword Management

  Background:
    Given A user exists

  Scenario: AC4 - Users can add new keywords
    Given I am logged into my account
    When I add a new keyword "Dance"
    Then The keyword "Dance" exists

  Scenario: Users that are not logged in cannot create new keywords
    When I add a new keyword "Dance"
    Then The request fails due to not authorised
    And The keyword "Dance" does not exist