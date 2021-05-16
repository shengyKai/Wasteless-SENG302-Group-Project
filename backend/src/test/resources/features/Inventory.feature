Feature: U19 - Create Inventory
  Scenario: AC1 - When logged in as a business administrator I can see my inventory. Regular users cannot see my inventory.
    Given A business exists with one inventory item and one administrator
    When The business administrator and regular user try to fetch inventory data
    Then The business administrator should be able to view the inventory and the regular user should receive an error

    #Can't do this until validation tasks are done
  Scenario: AC3 - Inventory items require quantity and expiry
    Given A business exists with a catalogue item with product code "BEANS"
    When I create an inventory item with product code "BEANS" and quantity "5" and expiry "21/08/2021"
    Then I expect the inventory item to be created

  Scenario: AC3 - Inventory items have additional fields
    Given A business exists with a catalogue item with product code "BEANS"
    When I create an inventory item with product code "BEANS" and quantity "5", expiry "21/08/2021", price per item "10" and total price "50"
    Then I expect the inventory item to be created
