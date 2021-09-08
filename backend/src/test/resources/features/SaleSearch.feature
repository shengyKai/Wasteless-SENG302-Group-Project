Feature: U29 Sale Item Search
  Background:
    Given A user exists
    And the business "Biz" with the type "Charitable organisation" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 1        | 2022-05-20 |
    And the business "BizTwo" with the type "Retail Trade" exists
    And the business has the following products in its catalogue:
      | product_id | name   |
      | CRAB       | crab   |
      | ORANGE     | orange |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | CRAB       | 12       | 2022-12-01 |
      | ORANGE     | 4        | 2022-07-14 |

  Scenario: AC2 - No filtering options then all sale items returned

  Scenario: AC4 - Sale listings may be ordered in various ways, order by product name

  Scenario: AC4 - Order by business name

  Scenario: AC4 - Order by business location

  Scenario: AC4 - Order by quantity

  Scenario: AC4 - Order by price

  Scenario: AC4 - Order by created

  Scenario: AC4 - Order by closes

  Scenario: AC5 - Limit results to particular business type

  Scenario: AC6 - Search by product name

  Scenario: AC7 - Limit results to within price range

  Scenario: AC8 - Search by business name

  Scenario: AC9 - Search by business location

  Scenario: AC10 - Limit results to within closing date range

  Scenario: All three search types and three filtering options must work together