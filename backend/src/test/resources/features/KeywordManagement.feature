
Feature: UCM6 - Keyword Management

  Background:
    Given A user exists

  Scenario: AC6 - System administrators can delete keywords
    Given The keyword "Dance" exists
    And A admin exists with name "Dave"
    And I am logged into "Dave" account
    When I try to delete the keyword "Dance"
    Then The request succeeds
    And The keyword "Dance" does not exist

  Scenario: AC6 - Non-system admins cannot delete keywords
    Given The keyword "Dance" exists
    And I am logged into my account
    When I try to delete the keyword "Dance"
    Then The request fails due to forbidden
    And The keyword "Dance" exists

  Scenario: AC6 - Users that are not logged in cannot delete keywords
    Given The keyword "Dance" exists
    When I try to delete the keyword "Dance"
    Then The request fails due to not authorised
    And The keyword "Dance" exists

  Scenario: AC6 - Deleted keywords are removed from all cards that have them
    Given The keyword "Dance" exists
    And A card exists with the keyword "Dance"
    And A admin exists with name "Dave"
    And I am logged into "Dave" account
    When I try to delete the keyword "Dance"
    Then The card does not have the keyword "Dance"