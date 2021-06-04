Feature: U17 - Modify catalogue entries
  Background:
    Given a user exists
    And the business "Biz" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | Fish  |
      | APPLE      | Apple |

  Scenario: AC1/AC3 - I can edit any of the catalogue entry attributes and the modification is saved
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property               | value            |
      | id                     | FISH             |
      | name                   | Fishn't          |
      | description            | This is not fish |
      | manufacturer           | Some guy         |
      | recommendedRetailPrice | 100              |
    Then The product is updated

  Scenario: AC2 - Product codes must stay unique
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property | value     |
      | id       | APPLE     |
      | name     | Not apple |
    Then The request fails due to bad request

  Scenario: AC2 - Product codes must stay valid
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property | value |
      | id       | FISh  |
      | name     | Fish  |
    Then The request fails due to bad request

  Scenario: AC2 - Product names must stay valid
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property | value |
      | id       | FISH  |
      | name     | 😂    |
    Then The request fails due to bad request

  Scenario: AC2 - Product descriptions must stay valid
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property    | value |
      | id          | FISH  |
      | name        | Fish  |
      | description | 😂    |
    Then The request fails due to bad request

  Scenario: AC2 - Product manufacturer must stay valid
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property     | value |
      | id           | FISH  |
      | name         | Fish  |
      | manufacturer | 😂    |
    Then The request fails due to bad request


  Scenario: AC2 - Product recommended retail price must stay valid
    Given I am an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property               | value |
      | id                     | FISH  |
      | name                   | Fish  |
      | recommendedRetailPrice | -1    |
    Then The request fails due to bad request


  Scenario: Non administrators cannot alter the product
    Given I am an not an administrator of the business
    And I am logged into my account
    When I update the fields of the "FISH" product to:
      | property               | value     |
      | id                     | APPLE     |
      | name                   | Not apple |
    Then The request fails due to forbidden