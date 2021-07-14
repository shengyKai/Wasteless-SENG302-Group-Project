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

  Scenario: AC3 - Display order can be changed to be ordered by location
    When I request cards in the "Wanted" section
    And have them ordered by "location"
    Then the cards should be ordered by their "location"

# The tests below are not stated by the backlog, but there is implementation for it, so it will be tested
  Scenario: Display order can be changed to be ordered by card closing date
    When I request cards in the "Wanted" section
    And have them ordered by "closes"
    Then the cards should be ordered by their "closes"

  Scenario: Display order can be changed to be ordered by author first name
    When I request cards in the "Wanted" section
    And have them ordered by "creatorFirstName"
    Then the cards should be ordered by their "creatorFirstName"

  Scenario: Display order can be changed to be ordered by author last name
    When I request cards in the "Wanted" section
    And have them ordered by "creatorLastName"
    Then the cards should be ordered by their "creatorLastName"