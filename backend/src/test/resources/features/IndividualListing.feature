Feature: U22 - List Sale

  Background:
    Given A user exists
    And the business "Biz" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
    And the business is listing the following items
      | product_id | price | quantity |
      | FISH       | 10    | 5        |


  Scenario: When not logged in I cannot like / unlike sale items
    When I like the sale item
    Then The request fails due to not authorised
    And The like count of the sale item is 0

  Scenario: AC3 - I can like a sale item
    Given I am logged into my account
    When I like the sale item
    Then The request succeeds
    And The like count of the sale item is 1

  Scenario: AC3 - Liking a sale item adds a message to my home feed
    Given I am logged into my account
    When I like the sale item
    And I check my notification feed
    Then I receive a notification
    And The notification is for liking the sale item

  Scenario: AC6 - Unliking a sale item updates the home feed message
    Given I am logged into my account
    When I like the sale item
    And I unlike the sale item
    And I check my notification feed
    Then I receive a notification
    And The notification is for unliking the sale item

  Scenario: AC5 - A user cannot like a sale item more than once
    Given I am logged into my account
    When I like the sale item
    And I like the sale item
    Then The request succeeds
    And The like count of the sale item is 1

  Scenario: AC6 - I can unlike a sale item
    Given I am logged into my account
    When I like the sale item
    And I unlike the sale item
    Then The request succeeds
    And The like count of the sale item is 0
