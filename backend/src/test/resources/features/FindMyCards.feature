Feature: UCM5 - Find my cards

  Background:
    Given A user exists with name "Dave"
    And "Dave" has the cards
      | title  | description   | section |
      | Card 1 | This is 1     | Wanted  |
      | Card 2 | This is not 1 | Wanted  |
    And A user exists with name "Tim"
    And "Tim" has the cards
      | title  | description        | section  |
      | Card A | This is A          | Wanted   |
      | Card B | This is not B      | ForSale  |
      | Card C | This is not A or B | Exchange |

    Scenario: AC2 - I can request cards from myself
      Given I am logged into "Dave" account
      When I request cards by "Dave"
      Then The request succeeds
      And I expect the cards for "Dave" to be returned

    Scenario: AC2 - I can request cards from other users
      Given I am logged into "Tim" account
      When I request cards by "Dave"
      Then The request succeeds
      And I expect the cards for "Dave" to be returned

    Scenario: AC3 - I can find all the active cards created by a user with card types together
      Given I am logged into "Dave" account
      When I request cards by "Tim"
      Then The request succeeds
      And I expect the cards for "Tim" to be returned

    Scenario: Users that are not logged in cannot request cards from a user
      When I request cards by "Dave"
      Then The request fails due to not authorised