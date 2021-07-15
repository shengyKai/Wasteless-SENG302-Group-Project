Feature: UCM3 - Marketplace section display

  Background:
    Given Multiple cards of different owners exists in the section "Wanted"
    And I am logged into my account

  Scenario: AC1 - Upon navigation to a Marketplace section, cards are ordered appropriately
    When I request cards in the "Wanted" section
    Then the cards should be ordered by "created" by default

  Scenario: AC3 - Display order can be changed to be ordered by card title
    When I request cards in the "Wanted" section
    And have them ordered by "title"
    Then the cards should be ordered by their "title"

  Scenario: AC3 - Display order can be changed to be ordered by card title in reverse
    When I request cards in the "Wanted" section
    And have them ordered by "title" in reverse
    Then the cards should be ordered by their "title" in reverse

  Scenario: AC3 - Display order can be changed to be ordered by location
    When I request cards in the "Wanted" section
    And have them ordered by "location"
    Then the cards should be ordered by their "location"

  Scenario: AC3 - Display order can be changed to be ordered by location in reverse
    When I request cards in the "Wanted" section
    And have them ordered by "location" in reverse
    Then the cards should be ordered by their "location" in reverse

# The tests below are not stated by the backlog, but there is implementation for it, so it will be tested
  Scenario: Display order can be changed to be ordered by card closing date
    When I request cards in the "Wanted" section
    And have them ordered by "closes"
    Then the cards should be ordered by their "closes"

  Scenario: Display order can be changed to be ordered by card closing date in reverse
    When I request cards in the "Wanted" section
    And have them ordered by "closes" in reverse
    Then the cards should be ordered by their "closes" in reverse

  Scenario: Display order can be changed to be ordered by author first name
    When I request cards in the "Wanted" section
    And have them ordered by "creatorFirstName"
    Then the cards should be ordered by their "creatorFirstName"

  Scenario: Display order can be changed to be ordered by author first name in reverse
    When I request cards in the "Wanted" section
    And have them ordered by "creatorFirstName" in reverse
    Then the cards should be ordered by their "creatorFirstName" in reverse

  Scenario: Display order can be changed to be ordered by author last name
    When I request cards in the "Wanted" section
    And have them ordered by "creatorLastName"
    Then the cards should be ordered by their "creatorLastName"

  Scenario: Display order can be changed to be ordered by author last name in reverse
    When I request cards in the "Wanted" section
    And have them ordered by "creatorLastName" in reverse
    Then the cards should be ordered by their "creatorLastName" in reverse

  Scenario: Display order can be changed to be ordered by creation in reverse
    When I request cards in the "Wanted" section
    And have them ordered by "created" in reverse
    Then the cards should be ordered by their "created" in reverse