Feature: U32 - Managing my feed
  Background:
    Given A user exists
    And An event is sent to the user

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
    And The event has the tag "none"

  Scenario: Different users cannot change my tags
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I try to change the event tag to "blue"
    Then The request fails due to forbidden
    And The event has the tag "none"