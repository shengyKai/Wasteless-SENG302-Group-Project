Feature: U23 Search for Business

  Background:
    Given A user exists
    And I am logged into my account

  Scenario: AC2 - I can search for a business using part of a name
    Given the business "New World" exists
    When I search with query "New"
    Then I expect business "New World" to be returned

  Scenario: AC2 - I can search for a business using a full name
    Given the business "New World" exists
    When I search with query "New World"
    Then I expect business "New World" to be returned

  Scenario: AC2 - Terms that are quoted must be matched exactly
    Given the business "New World" exists
    When I search with query "'New'"
    Then I don't expect business "New World" to be returned

  Scenario: AC2 - Terms that are quoted must be matched exactly
    Given the business "New World" exists
    When I search with query "'New World'"
    Then I expect business "New World" to be returned

  Scenario: AC2 - If I enter more than one term, then I can specify results match both terms
    Given the business "New World" exists
    When I search with query "New AND World"
    Then I expect business "New World" to be returned

  Scenario: AC2 - If I enter more than one term, then I can specify results match both terms
    Given the business "New World" exists
    When I search with query "New AND England"
    Then I don't expect business "New World" to be returned

  Scenario: AC2 - If I enter more than one term, then I can specify results match any terms
    Given the business "New World" exists
    And the business "PaknSave" exists
    When I search with query "New OR Pakn"
    Then I expect business "New World" to be returned
    And I expect business "PaknSave" to be returned