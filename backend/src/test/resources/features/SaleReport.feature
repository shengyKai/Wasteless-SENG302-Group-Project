Feature: U41 - Sales Report
  Background:
    Given A user exists with name "Dave"
    And the business "Biz" with the type "Retail Trade" and location "42,There Place,Steve,Steve,Steve,Steve,2345" exists
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

  Scenario: AC4 - I can select view the report over the whole period
    Given I am logged into "Dave" account
    When I view the report from "2012-12-21" to "2021-10-10" with "none" granularity
    Then 1 report segments are returned

  Scenario: AC4 - I can select view the total number of purchases
    Given I am logged into "Dave" account
    And I try to purchase the most recent sale listing
    And I try to purchase the most recent sale listing
    When I view the report from "2012-12-21" to "9999-01-01" with "none" granularity
    Then The total number of purchases is 2

  Scenario: AC4 - I can select view the total value of purchases
    Given I am logged into "Dave" account
    And I try to purchase the most recent sale listing
    And I try to purchase the most recent sale listing
    When I view the report from "2012-12-21" to "9999-01-01" with "none" granularity
    Then The total value of purchases is 18

  Scenario: AC5 - I can select view the report over a monthly period
    Given I am logged into "Dave" account
    When I view the report from "2021-08-21" to "2021-10-10" with "monthly" granularity
    Then 3 report segments are returned
    
  Scenario: Not logged in - Cannot view business report
    When I view the report from "2021-01-01" to "2021-10-10" with "yearly" granularity
    Then The request fails due to not authorised

  Scenario: Logged in a non-owner - Cannot view business report
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I view the report from "2021-01-01" to "2021-10-10" with "yearly" granularity
    Then The request fails due to forbidden
    
    