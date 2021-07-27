Feature: UCM9 - Edit card
  Background:
    Given Keywords with the following names exist:
      | Fresh       |
      | Free        |
    And A user exists with name "Tim"
    And a card exists

  Scenario: AC1 - Only I can edit the card I created
    Given A user exists with name "Timmy"
    And I am logged into "Timmy" account
    When I try to updated the fields of the card to:
      | section     | ForSale     |
      | title       | Fresh Apple |
      | description | Wow, cool   |
      | keywords    | Fresh       |
    Then The request fails due to forbidden
    And The card is not updated

  Scenario: AC2 - I can change the title, description and keywords of the card
    Given I am logged into "Tim" account
    When I try to updated the fields of the card to:
      | section     | ForSale     |
      | title       | Fresh Apple |
      | description | Wow, cool   |
      | keywords    | Fresh       |
    Then The request succeeds
    And The card is updated

  Scenario: I cannot change the section to an invalid value
    Given I am logged into "Tim" account
    When I try to updated the fields of the card to:
      | section     | NotForSale  |
      | title       | Fresh Apple |
      | description | Wow, cool   |
      | keywords    | Fresh       |
    Then The request fails due to bad request
    And The card is not updated

  Scenario: I cannot change the title to an invalid value
    Given I am logged into "Tim" account
    When I try to updated the fields of the card to:
      | section     | ForSale     |
      | title       | 😂          |
      | description | Wow, cool   |
      | keywords    | Fresh       |
    Then The request fails due to bad request
    And The card is not updated

  Scenario: I cannot change the description to an invalid value
    Given I am logged into "Tim" account
    When I try to updated the fields of the card to:
      | section     | NotForSale  |
      | title       | Fresh Apple |
      | description | 😂          |
      | keywords    | Fresh       |
    Then The request fails due to bad request
    And The card is not updated

  Scenario: I cannot change the keywords to an invalid value
    Given I am logged into "Tim" account
    When I try to updated the fields of the card to:
      | section     | NotForSale  |
      | title       | Fresh Apple |
      | description | Wow, cool   |
      | keywords    | Invalid     |
    Then The request fails due to bad request
    And The card is not updated

  Scenario: AC4 - An admin can edit any card
    Given A admin exists with name "Dave"
    And I am logged into "Dave" account
    When I try to updated the fields of the card to:
      | section     | ForSale     |
      | title       | Fresh Apple |
      | description | Wow, cool   |
      | keywords    | Fresh       |
    Then The request succeeds
    And The card is updated

  Scenario: A user that is not logged in cannot modify any cards
    When I try to updated the fields of the card to:
      | creator     | Tim         |
      | section     | ForSale     |
      | title       | Fresh Apple |
      | description | Wow, cool   |
      | keywords    | Fresh       |
    Then The request fails due to not authorised
    And The card is not updated
