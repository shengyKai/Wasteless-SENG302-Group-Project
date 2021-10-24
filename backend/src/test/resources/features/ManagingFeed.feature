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

  Scenario: AC2 - New event items are initially unread and I can mark an event item as read
    Given I am logged into my account
    And The default read status is false
    When I try to mark an event from my feed as read
    Then The event will be updated as read

  Scenario: Unauthorised users cannot mark items from my feed as read
    When I try to mark an event from my feed as read
    Then The request fails due to not authorised
    And The event is not marked as read

  Scenario: Different users cannot mark items from my feed as read
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I try to mark an event from my feed as read
    Then The request fails due to forbidden
    And The event is not marked as read

  Scenario: I cannot mark an item as read if it does not exist in my feed
    Given I am logged into my account
    When I try to mark an event that does not exist from my feed as read
    Then The request fails due to not acceptable

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

  Scenario: AC4 - I can archive an item from my feed
    Given I am logged into my account
    And the event status has been set to "normal"
    When I try to change the status of the event to "archived"
    Then The request succeeds
    And the event has status "archived"

  Scenario: AC5 - I can delete an archived item
    Given I am logged into my account
    And the event status has been set to "archived"
    When I try to delete an event from my feed
    Then The event is deleted

  Scenario: AC3 - I can star an item from my feed
    Given I am logged into my account
    And the event status has been set to "normal"
    When I try to change the status of the event to "starred"
    Then The request succeeds
    And the event has status "starred"

  Scenario: AC3 - I can un-star an item from my feed
    Given I am logged into my account
    And the event status has been set to "starred"
    When I try to change the status of the event to "normal"
    Then The request succeeds
    And the event has status "normal"
