Feature: UCM3 - Marketplace section display

  Background:
    Given Multiple cards of different owners exists in the section "Wanted"
    And I am logged into my account

  Scenario: AC1 - Upon navigation to a Marketplace section, cards are ordered appropriately
    Then the cards in the response should be ordered by last renewed date by default

  Scenario: AC3 - Display order can be changed to be ordered by card title
    When the cards are ordered by "title"
    Then the cards in the response should be ordered by their title

  Scenario: AC3 - Display order can be changed to be ordered by card title in reverse
    When the cards are ordered by "title" in reverse
    Then the cards in the response should be ordered by their title in reverse

  Scenario: AC3 - Display order can be changed to be ordered by location
    When the cards are ordered by "location"
    Then the cards in the response should be ordered by their location

  Scenario: AC3 - Display order can be changed to be ordered by location in reverse
    When the cards are ordered by "location" in reverse
    Then the cards in the response should be ordered by their location in reverse

# The tests below are not stated by the backlog, but there is implementation for it, so it will be tested
  Scenario: Display order can be changed to be ordered by card closing date
    When the cards are ordered by "closes"
    Then the cards in the response should be ordered by their closes

  Scenario: Display order can be changed to be ordered by card closing date in reverse
    When the cards are ordered by "closes" in reverse
    Then the cards in the response should be ordered by their closes in reverse

  Scenario: Display order can be changed to be ordered by author first name
    When the cards are ordered by "creatorFirstName"
    Then the cards in the response should be ordered by their creatorFirstName

  Scenario: Display order can be changed to be ordered by author first name in reverse
    When the cards are ordered by "creatorFirstName" in reverse
    Then the cards in the response should be ordered by their creatorFirstName in reverse

  Scenario: Display order can be changed to be ordered by author last name
    When the cards are ordered by "creatorLastName"
    Then the cards in the response should be ordered by their creatorLastName

  Scenario: Display order can be changed to be ordered by author last name in reverse
    When the cards are ordered by "creatorLastName" in reverse
    Then the cards in the response should be ordered by their creatorLastName in reverse

  Scenario: Display order can be changed to be ordered by last renewed date in reverse
    When the cards are ordered by "lastRenewed" in reverse
    Then the cards in the response should be ordered by their last renewed date in reverse