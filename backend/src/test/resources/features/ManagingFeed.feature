Feature: U32 - Managing my feed
  Background:
    Given A user exists
    And An event is sent to the user

  Scenario: AC1 - I can delete any item from my feed
    Given I am logged into my account
    When I try to delete an event from my feed
    Then The event is deleted

  Scenario: Unauthorised users cannot delete my items from my feed
    When I try to delete an event from my feed
    Then The request fails due to not authorised
    And The event is not deleted from my feed

  Scenario: Different users cannot delete items from my feed
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I try to delete an event from my feed
    Then The request fails due to forbidden
    And The event is not deleted from my feed

  Scenario: AC6 - I can add a tag to a item
    Given I am logged into my account
    When I try to change the event tag to "blue"
    Then The request succeeds
    And The event has the tag "blue"

  Scenario: AC6 - I can remove a tag from a item
    Given I am logged into my account
    When I try to change the event tag to "blue"
    Then The request succeeds
    When I try to change the event tag to "none"
    Then The request succeeds
    And The event has the tag "none"

  Scenario: Unauthorised users cannot change my tags
    When I try to change the event tag to "blue"
    Then The request fails due to not authorised
    Then The event has the tag "none"

  Scenario: Different users cannot change my tags
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I try to change the event tag to "blue"
    Then The request fails due to forbidden
    And The event has the tag "none"