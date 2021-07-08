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

  Scenario: AC 3 - I receive a notification when my card is within 24 hours of expiring
    Given The card expiry is changed to less than a day from now
    And I am logged into my account
    And The system has performed its scheduled check for cards that are close to expiry
    When I check my notification feed
    Then I have received a message telling me the card is about to expire

  Scenario: AC3 - I can extend the display period of one of my cards if the expiry is within 1 day
    Given The card expiry is changed to less than a day from now
    And I am logged into my account
    When I try to extend the display period of my card
    Then The request succeeds
    And I expect the display period of my card to be extended

  Scenario: AC3 - I cannot extend the display period if it is more then one day before expiry
    Given I am logged into my account
    When I try to extend the display period of my card
    Then The request fails due to bad request
    And I expect the display period of my card to not be extended

  Scenario: AC3 - A user that is not logged in cannot extend the expiry of my card
    Given The card expiry is changed to less than a day from now
    When I try to extend the display period of my card
    Then The request fails due to not authorised
    And I expect the display period of my card to not be extended

  Scenario: AC3 - A different user cannot extend the expiry of my card
    Given The card expiry is changed to less than a day from now
    And A user exists with name "Dave"
    And I am logged into "Dave" account
    When I try to extend the display period of my card
    Then The request fails due to forbidden
    And I expect the display period of my card to not be extended

  Scenario: AC4 - If no action is taken within 24 hours of the notification, the card will be deleted automatically
    Given The card expiry is changed to less than a day from now
    When The notification period is over without any action taken
    And The system has performed its scheduled check for cards that are expired
    Then The card will be removed from the marketplace

