Feature: UCM2 - Card creation

  Background:
    Given A user exists

  Scenario: AC1, AC3 - I can create a card with a section and title
    Given I am logged into my account
    When I try to create a card with the following properties:
      | section | Wanted |
      | title   | A nap  |
    Then The request succeeds and a entity is created
    And I expect the card to be saved to the application

  Scenario: AC1 - I cannot create a card without a section
    Given I am logged into my account
    When I try to create a card with the following properties:
      | title | a nap |
    Then The request fails due to bad request
    And I expect the card to not be created

  Scenario: AC3 - I cannot create a card without title
    Given I am logged into my account
    When I try to create a card with the following properties:
      | section | Wanted |
    Then The request fails due to bad request
    And I expect the card to not be created

  Scenario: AC4 - I can create a card with a description
    Given I am logged into my account
    When I try to create a card with the following properties:
      | section | Wanted |
      | title   | A nap  |
      | description | Will settle for a long blink |
    Then The request succeeds and a entity is created
    And I expect the card to be saved to the application

  Scenario: AC5 - I can create a card with associated keywords
    Given I am logged into my account
    And Keywords with the following names exist:
      | Vehicle     |
      | Free        |
      | Home Baking |
    When I try to create a card with the following properties:
      | section    | Wanted            |
      | title      | A nap             |
      | keywords   | Home Baking, Free |
    Then The request succeeds and a entity is created
    And I expect the card to be saved to the application