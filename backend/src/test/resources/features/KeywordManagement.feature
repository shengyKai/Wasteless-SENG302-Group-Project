
Feature: UCM6 - Keyword Management

  Background:
    Given A user exists

  Scenario: AC4 - Users can add new keywords
    Given I am logged into my account
    When I add a new keyword "Dance"
    Then The request succeeds and a entity is created
    And The keyword "Dance" is present

  Scenario: Users that are not logged in cannot create new keywords
    When I add a new keyword "Dance"
    Then The request fails due to not authorised
    And The keyword "Dance" is not present

  Scenario: AC5 - System administrators are notified a new keyword is added
    Given A admin exists with name "Bob"
    And   I am logged into "Bob" account
    When  A keyword "Dance" is created
    Then  I receive a notification
    And   The notification contains the keyword "Dance"

  Scenario: AC6 - System administrators can delete keywords
    Given The keyword "Dance" exists
    And A admin exists with name "Dave"
    And I am logged into "Dave" account
    When I try to delete the keyword "Dance"
    Then The request succeeds
    And The keyword "Dance" is not present

  Scenario: AC6 - Non-system admins cannot delete keywords
    Given The keyword "Dance" exists
    And I am logged into my account
    When I try to delete the keyword "Dance"
    Then The request fails due to forbidden
    And The keyword "Dance" is present

  Scenario: AC6 - Users that are not logged in cannot delete keywords
    Given The keyword "Dance" exists
    When I try to delete the keyword "Dance"
    Then The request fails due to not authorised
    And The keyword "Dance" is present

  Scenario: AC6 - Deleted keywords are removed from all cards that have them
    Given The keyword "Dance" exists
    And a card exists
    And The keyword "Dance" is added to the card
    And A admin exists with name "Dave"
    And I am logged into "Dave" account
    When I try to delete the keyword "Dance"
    Then The card does not have the keyword "Dance"