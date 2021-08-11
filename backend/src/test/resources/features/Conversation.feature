Feature: UCM8 Contacting other marketplace users
  Background:
    Given A user exists
    And a card exists
    And A user exists with name "Joe"
    And I am logged into my account

  Scenario: AC2 - I can send a message to a card creator
    Given I create a message for a given marketplace card owner
    When I send the message
    Then I expect the message to be sent