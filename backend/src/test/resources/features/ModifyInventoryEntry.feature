Feature: U20 - Modify Inventory Entries

  Background:
    Given A user exists
    And the business "Nathan Apple Inc" exists
    And the business has the following products in its catalogue:
      | product_id | name    |
      | APPLE11    | apple   |
      | BANANA22   | banana  |
      | CABBAGE33  | cabbage |
    And the business has the following list of items in its inventory:
      | id | product_id | quantity | price_per_item | total_price | manufactured | sell_by    | best_before | expires    |
      | 1  | APPLE11    | 10       | 1.10           | 11.00       | 2021-07-17   | 2029-01-01 | 2029-04-04  | 2029-07-05 |
      | 2  | BANANA22   | 20       | 0.65           | 16.05       | 2021-07-18   | 2028-05-09 | 2028-10-19  | 2028-12-30 |
      | 3  | BANANA22   | 5        | 0.69           | 3.04        | 2021-07-19   | 2027-02-12 | 2027-03-13  | 2027-04-14 |
      | 4  | CABBAGE33  | 100      | 1.92           | 150.00      | 2021-07-20   | 2029-06-15 | 2029-09-27  | 2029-10-01 |
      | 5  | CABBAGE33  | 9        | 2.09           | 21.69       | 2021-07-21   | 2026-12-01 | 2027-10-01  | 2028-08-06 |
      | 6  | CABBAGE33  | 17       | 1.93           | 35.92       | 2021-07-22   | 2028-01-01 | 2028-01-11  | 2028-01-21 |

  Scenario: AC1 - I can modify the quantity of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the quantity to 4 for the inventory entry with the id 1
    Then the quantity of the inventory item with the id 1 will be 4
    
  Scenario: AC1 - I can modify the price per item of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the price per item to 1.20 for the inventory entry with the id 1
    Then the price per item of the inventory item with the id 1 will be 1.20

  Scenario: AC1 - I can modify the total price of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the total price to 15.05 for the inventory entry with the id 2
    Then the total price of the inventory item with the id 2 will be 15.05

  Scenario: AC1 - I can modify the manufactured date of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the manufactured date to "2021-06-17" for the inventory entry with the id 3
    Then the manufactured date of the inventory item with the id 3 will be "2021-06-17"

  Scenario: AC1 - I can modify the sell by date of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the sell by date to "2021-06-18" for the inventory entry with the id 4
    Then the sell by date of the inventory item with the id 4 will be "2021-06-18"

  Scenario: AC1 - I can modify the best before date of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the best before date to "2021-06-19" for the inventory entry with the id 5
    Then the best before date of the inventory item with the id 5 will be "2021-06-19"

  Scenario: AC1 - I can modify the expires date of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the expires date to "2021-06-20" for the inventory entry with the id 6
    Then the expires date of the inventory item with the id 6 will be "2021-06-20"

  Scenario: AC1 - I can modify the product of an inventory entry when logged in as the business administrator.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the product to the one with the product code "APPLE11" for the inventory entry with the id 6
    Then the product of the inventory item with the id 6 will have the product code "APPLE11"

  Scenario: AC1 - I cannot modify the quantity of an inventory entry when logged in as user that is not the business administrator.
    Given I am an not an administrator of the business
    And I am logged into my account
    When I try to modify the quantity to 4 for the inventory entry with the id 1
    Then the quantity of the inventory item with the id 1 will be 5

  Scenario: AC1 - I cannot modify the price_per_item of an inventory entry when logged in as user that is not the business administrator.
    Given I am an not an administrator of the business
    And I am logged into my account
    When I try to modify the price per item to 1.20 for the inventory entry with the id 1
    Then the price per item of the inventory item with the id 1 will be 1.10

  Scenario: AC2 - I cannot modify the quantity to be negative
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the quantity to -4 for the inventory entry with the id 1
    Then the quantity of the inventory item with the id 1 will be 5

  Scenario: AC2 - I cannot modify the quantity to be empty (null)
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the quantity to null for the inventory entry with the id 1
    Then the quantity of the inventory item with the id 1 will be 5

  Scenario: AC2 - I cannot modify the expires date to be empty (null)
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the expires date to null for the inventory entry with the id 6
    Then the expires date of the inventory item with the id 6 will be "2028-01-21"

  Scenario: AC2 - I cannot modify the expires date to be before the best_before date
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the expires date to "2025-10-10" for the inventory entry with the id 6
    Then the expires date of the inventory item with the id 6 will be "2028-01-21"

  Scenario: AC2 - I can modify the manufactured date to be empty (null)
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the manufactured date to null for the inventory entry with the id 3
    Then the manufactured date of the inventory item with the id 3 will be null

  Scenario: AC2 - I cannot modify the manufactured date to be after today
    Given I am an administrator of the business
    And I am logged into my account
    When I try to modify the manufactured date to "2025-06-17" for the inventory entry with the id 3
    Then the manufactured date of the inventory item with the id 3 will be "2021-07-19"

