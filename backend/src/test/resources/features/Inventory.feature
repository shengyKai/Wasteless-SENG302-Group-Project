Feature: U19 - Create Inventory

  Background:
    Given a business exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 3        | 2021-12-04 |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 1        | 2022-05-20 |

  Scenario: AC1 - When logged in as a business administrator I can see my inventory.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to access the inventory of the business
    Then the inventory of the business is returned to me

  Scenario: AC1 - When logged in as a user who is not a business administrator I cannot see the business's inventory.
    Given I am an not an administrator of the business
    And I am logged into my account
    When I try to access the inventory of the business
    Then I cannot view the inventory

    #Can't do this until validation tasks are done
  @Ignore
  Scenario: AC3 - Inventory items require quantity and expiry
    Given A business exists with a catalogue item with product code "BEANS"
    When I create an inventory item with product code "BEANS" and quantity "5" and expiry "21/08/2021"
    Then I expect the inventory item to be created

  @Ignore
  Scenario: AC3 - Inventory items have additional fields
    Given A business exists with a catalogue item with product code "BEANS"
    When I create an inventory item with product code "BEANS" and quantity "5", expiry "21/08/2021", price per item "10" and total price "50"
    Then I expect the inventory item to be created

