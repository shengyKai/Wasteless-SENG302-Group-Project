Feature: Product

  Scenario: Only compulsory data is given
    Given the business "Biz" exists
    When the product code "ABCD" and the name "Exploding Pineapples" is provided
    Then the product "ABCD" exists for the business
    And the time of "ABCD" created is set to now
    And the other fields are null

  Scenario: All data is given
    Given the business "Biz" exists
    When the product code "ABCD" and the name "Exploding Pineapples" is provided
    And the description "Monkeys in planes keep dropping them", manufacturer "NinjaKiwi", and retail price "400" is provided
    Then the product "ABCD" exists for the business
    And the time of "ABCD" created is set to now
    And all fields have a value

  Scenario: Compulsory field is invalid
    Given the business "Biz" exists
    When the product code "ab=57" and the name "Exploding Pineapples" is provided
    Then the product "ab=57" does not exist for the business

  Scenario: Optional field is invalid
    Given the business "Biz" exists
    When the product code "ABCD" and the name "Exploding Pineapples" is provided
    And the description "Monkeys in planes keep dropping them", manufacturer "NinjaKiwi", and retail price "five hundred million" is provided
    Then the product "ABCD" does not exist for the business

  Scenario: Business does not exist
    Given the business does not exist
    When the product code "ABCD" and the name "Exploding Pineapples" is provided
    Then the product "ABCD" does not exist for the business

  Scenario: Same product code, different business
    Given the business "Biz" exists
    And a second business "Biz2" exists
    And a business has a product "ABCD" with name "First name"
    When the second business creates a product "ABCD"
    Then the product "ABCD" exists for the business
    And the product "ABCD" exists for the second business

  Scenario: Same product code, same business
    Given the business "Biz" exists
    And a business has a product "ABCD" with name "First name"
    When the product code "ABCD" and the name "Different from before" is provided
    Then only the first product "ABCD" exists, not with name "Different from before"
