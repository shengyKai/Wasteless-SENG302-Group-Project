Feature: U28 Product Search
  Background:
    Given A user exists
    And I am logged into my account
    And the business "Biz" exists
    And a business has a product "EXPL" with name "Example Product"
    And a business has a product "MAGE" with name "Wizard Sauce"
    And a business has a product "POPS" with name "Sharp Things"

  Scenario: AC2 - I can search products in a business I am the admin for
    When I search the catalogue for "Example"
    Then 1 products are returned

  Scenario: AC3 - Concatenated search terms AND
    When I search the catalogue for "Example AND Sharp"
    Then 0 products are returned

  Scenario: AC3 - Concatenated search terms OR
    When I search the catalogue for "Example OR Sharp"
    Then 2 products are returned

  Scenario: AC4 - No fields selected, searches all
    And No product fields are selected
    When I search the catalogue for "EXPL"
    Then 1 products are returned

  Scenario: AC4 - Only selected fields are searched single
    Given product fields are selected:
      | name |
    When I search the catalogue for "EXPL"
    Then 0 products are returned

  Scenario: AC4 - Only selected fields are searched multiple
    Given product fields are selected:
      | name        |
      | productCode |
    When I search the catalogue for "EXPL"
    Then 1 products are returned

  # Run this one last because it uses a different user than the Background
  Scenario: AC2 - I cannot search products in a business I am not the admin for
    Given A user exists with name "John" and password "1234abcd"
    And I am logged into my account
    When I search the catalogue for "EXPL"
    Then The request fails due to forbidden
