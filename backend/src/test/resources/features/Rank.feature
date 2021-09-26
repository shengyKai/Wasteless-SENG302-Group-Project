Feature: UE2 - User Interface
  Background:
    Given A user exists
    And the business "Megafresh market" exists


  Scenario: AC1 - Newly created businesses have the lowest rank
    Then I expect the business to have "bronze" rank

  Scenario: AC4 - Users can see the current rank of a business
    Given I am logged into my account
    When I view the business "Megafresh market"
    Then I am able to see the rank of the business