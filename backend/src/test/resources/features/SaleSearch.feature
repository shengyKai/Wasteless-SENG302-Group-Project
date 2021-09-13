Feature: U29 Sale Item Search
  Background:
    Given A user exists
    And the business "Biz" with the type "Charitable organisation" and location "23, Here St, Bob, Bob, Bob, Bob, 1234" exists
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
    And the business "BizTwo" with the type "Retail Trade" and location "42, There Place, Steve, Steve, Steve, Steve, 2345" exists
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
    Given I am logged into my account
    When I search for sale items
    Then 4 sale items are returned

  Scenario: AC4 - Sale listings may be ordered in various ways, order by product name
    Given I am logged into my account
    When orderBy is "name"
    When I search for sale items
    Then first product is "APPLE"

  Scenario: AC4 - Order by business name
    Given I am logged into my account
    When orderBy is "businessName"
    When I search for sale items
    Then first product is from "Biz"

  Scenario: AC4 - Order by business location
    Given I am logged into my account
    When orderBy is "location"
    When I search for sale items
    Then first product is from "Biz"

  Scenario: AC4 - Order by quantity
    Given I am logged into my account
    When orderBy is "quantity"
    When I search for sale items
    Then first product is "FISH"

  Scenario: AC4 - Order by price
    Given I am logged into my account
    When orderBy is "price"
    When I search for sale items
    Then first product is "FISH"

  Scenario: AC4 - Order by created
    Given I am logged into my account
    When orderBy is "created"
    When I search for sale items
    Then first product is "FISH"

  Scenario: AC4 - Order by closes
    Given I am logged into my account
    When orderBy is "closes"
    When I search for sale items
    Then first product is "FISH"

  Scenario: AC5 - Limit results to particular business type
    Given I am logged into my account
    When businessType is "Retail Trade"
    When I search for sale items
    Then 2 sale items are returned

  Scenario: AC6 - Search by product name
    Given I am logged into my account
    When search sale name is "Fish"
    When I search for sale items
    Then 1 sale items are returned

  Scenario: AC7 - Limit results to within price range
    Given I am logged into my account
    When search sale price is between 1 and 5
    When I search for sale items
    Then 2 sale items are returned

  Scenario: AC8 - Search by business name
    Given I am logged into my account
    When search sale business is "BizTwo"
    When I search for sale items
    Then 2 sale items are returned

  Scenario: AC9 - Search by business location
    Given I am logged into my account
    When search sale location is "There"
    When I search for sale items
    Then 2 sale items are returned

  Scenario: AC10 - Limit results to within closing date range
    Given I am logged into my account
    When search sale date is between "2022-05-01" and "2022-08-01"
    When I search for sale items
    Then 2 sale items are returned

  Scenario: All three search types and three filtering options must work together
    Given I am logged into my account
    When search sale name is "Fi"
    And search sale business is "Biz"
    And search sale location is "here"
    And search sale price is between 5 and 25
    And search sale date is between "2022-01-01" and "2022-02-01"
    And businessType is "Charitable organisation"
    And orderBy is "name"
    When I search for sale items
    Then 1 sale items are returned

  Scenario: Basic search
    Given I am logged into my account
    When I search sale basic "Biz"
    Then 4 sale items are returned

  Scenario: Not logged in - Unauthorised error
    When I search for sale items
    Then The request fails due to not authorised