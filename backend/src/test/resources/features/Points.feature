Feature: UE1 - Points system
  Background:
    Given A user exists
    And the business "Megafresh market" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 20       | 2022-05-20 |

  Scenario: AC1 - I have points associated with my business.
    Given I am logged into my account
    When I view the business "Megafresh market"
    Then I am able to see the points for the business

  Scenario: AC1 - The business acquires 1 point whenever a new sale listing is created.
    Given I am logged into my account
    And I am an administrator of the business
    And The business has 20 points
    When I create a sale item for product code "FISH", quantity 10, price 100.0
    And I create a sale item for product code "APPLE", quantity 15, price 3.0
    Then I expect the business to have 22 points