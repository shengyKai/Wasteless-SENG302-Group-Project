Feature: U31 - Purchases

  Background:
    Given A user exists with name "Jeffrey"
    And the business "Amazon" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
    And the business is listing the following items
      | product_id | price | quantity |
      | FISH       | 10    | 5        |
    And A user exists with name "Alice"

  Scenario: AC1: When an item is purchased, any other users who have liked that item will be notified that it is unavailable
    Given I am logged into "Alice" account
    And I like the sale item
    When user "Jeffrey" has purchased the sale listing "fish" from business "Amazon"
    Then "Alice" will receive a notification stating that "fish" is no longer available

  Scenario: AC3: When an item is purchased, the seller's inventory is updated
    Given user "Alice" has purchased the sale listing "fish" from business "Amazon"
    And I am logged into "Jeffrey" account
    When I try to access the inventory of the business
    Then the inventory of the business is returned to me
    And the quantity of the inventory item "fish" will be 12

  Scenario: AC4: When an item is purchased, the item is removed from the sale listings
    Given user "Alice" has purchased the sale listing "fish" from business "Amazon"
    And I am logged into "Alice" account
    When I am viewing the sale listings for business "Amazon"
    Then the item "fish" is not present

  Scenario: AC5: When an item is purchased, information about the purchase is recorded in a sales history for the business
    Given I am logged into "Alice" account
    And I am viewing the sale listings for business "Amazon"
    When I try to purchase the most recent sale listing
    Then The request succeeds
    And A record of the purchase is added to the business's sale history

  Scenario: AC2: A notification appears on my home feed to remind me what I have purchased
    Given I am logged into "Alice" account
    And I am viewing the sale listings for business "Amazon"
    When I try to purchase the most recent sale listing
    And I check my notification feed
    Then I receive a notification