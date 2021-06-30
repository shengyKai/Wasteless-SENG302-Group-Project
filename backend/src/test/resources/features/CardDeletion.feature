Feature: UCM2 - Card creation

  Background:
    Given A user exists
    And a card exists

  Scenario: AC1 - I can delete my own card
    Given I am logged into my account
    When I try to delete the card
    Then The request succeeds
    And I expect the card to be deleted

  Scenario: AC1 - A user that is not logged in cannot delete my card
    When I try to delete the card
    Then The request fails due to not authorised
    And I expect the card to still exist

  Scenario: AC1 - A different user cannot delete my card
    Given A user exists with name "Dave"
    And I am logged into "Dave" account
    When I try to delete the card
    Then The request fails due to forbidden
    And I expect the card to still exist

  Scenario: AC2 - A system administrator can delete any card
    Given A admin exists with name "Tim"
    And I am logged into "Tim" account
    When I try to delete the card
    Then The request succeeds
    And I expect the card to be deleted