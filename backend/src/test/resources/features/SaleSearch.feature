Feature: U29 Sale Item Search
  Background:
    Given A user exists
    And the business "Biz" with the type "Charitable organisation" and location "23 Here St" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 1        | 2022-05-20 |
    And the business has the following products on sale:
      | product_id | quantity | price | closes     |
      | FISH       | 12       | 20    | 2022-01-18 |
      | APPLE      | 1        | 1     | 2022-05-19 |
    And the business "BizTwo" with the type "Retail Trade" and location "42 There Place" exists
    And the business has the following products in its catalogue:
      | product_id | name   |
      | CRAB       | crab   |
      | ORANGE     | orange |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | CRAB       | 12       | 2022-12-01 |
      | ORANGE     | 4        | 2022-07-14 |
    And the business has the following products on sale:
      | product_id | quantity | price | closes     |
      | CRAB       | 8        | 15    | 2022-11-30 |
      | ORANGE     | 2        | 3     | 2022-07-13 |

  Scenario: AC2 - No filtering options then all sale items returned
    When no filters are passed
    Then 4 sale items are returned

  Scenario: AC4 - Sale listings may be ordered in various ways, order by product name
    When orderBy is "name"
    Then products are in alphabetical order

  Scenario: AC4 - Order by business name
    When orderBy is "businessName"
    Then products are in business order

  Scenario: AC4 - Order by business location
    When orderBy is "location"
    Then products are in location order

  Scenario: AC4 - Order by quantity
    When orderBy is "quantity"
    Then products are in quantity order

  Scenario: AC4 - Order by price
    When orderBy is "price"
    Then products are in price order

  Scenario: AC4 - Order by created
    When orderBy is "created"
    Then products are in created date order

  Scenario: AC4 - Order by closes
    When orderBy is "closes"
    Then products are in close date order

  Scenario: AC5 - Limit results to particular business type
    When businessType is "Retail Trade"
    Then 2 sale items are returned

  Scenario: AC6 - Search by product name
    When search sale name is "Fish"
    Then 1 sale items are returned

  Scenario: AC7 - Limit results to within price range
    When search sale price is between 1 and 5
    Then 2 sale items are returned

  Scenario: AC8 - Search by business name
    When search sale business is "BizTwo"
    Then 2 sale items are returned

  Scenario: AC9 - Search by business location
    When search sale location is "There"
    Then 2 sale items are returned

  Scenario: AC10 - Limit results to within closing date range
    When search sale date is between "2022-05-01" and "2022-08-01"
    Then 2 sale items are returned

  Scenario: All three search types and three filtering options must work together
    When search sale name is "Fi"
    And search sale business is "Biz"
    And search sale location is "here"
    And search sale price is between 5 and 25
    And search sale date is between "2022-01-01" and "2022-02-01"
    And businessType is "Charitable organisation"
    And orderBy is "name"
    Then 1 sale items are returned