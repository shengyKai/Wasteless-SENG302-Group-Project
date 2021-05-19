Feature: U22 - List Sale

  Background:
    Given a user exists
    And the business "Biz" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 1        | 2022-05-20 |

  Scenario: AC2 - When logged in as a business administrator I can add a sale item
    Given I am an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity 10, price 100.0
    Then I expect the sale item to be created

  Scenario: AC2 - When not logged in as a business administrator I cannot add a sale item
    Given I am an not an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity 10, price 100.0
    Then I expect the sale item not to be created

  Scenario: AC2 - When logged in as a business administrator I can add a sale item with more info and a closing time
    Given I am an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity 10, price 100.0, more info "This is fish", closing "2022-01-10"
    Then I expect the sale item to be created