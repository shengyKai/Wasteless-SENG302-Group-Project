Feature: UCM7 - Keyword search
  Background:
    Given A user exists
    And Keywords with the following names exist:
      | Vehicle     |
      | Free        |
      | Home Baking |
    And The user has the following cards:
      | title     | section  | keywords      |
      | Apricot   | Wanted   | Free          |
      | Cucumber  | Wanted   | Free, Vehicle |
      | Apple     | Wanted   | Vehicle       |
      | Pear      | Wanted   | Home Baking   |
      | Orange    | ForSale  | Home Baking   |
      | Pineapple | Exchange | Home Baking   |

  Scenario: AC1 - When searching in section "Wanted" only cards from "Wanted" are returned
    And I am logged into my account
    When I try to search for cards in the section "Wanted" with all of the keywords:
      | Home Baking |
    Then The request succeeds
    And I expect the cards to be returned:
      | Pear  |

  Scenario: AC1 - When searching in section "ForSale" only cards from "ForSale" are returned
    And I am logged into my account
    When I try to search for cards in the section "ForSale" with all of the keywords:
      | Home Baking |
    Then The request succeeds
    And I expect the cards to be returned:
      | Orange |

  Scenario: AC1 - When searching in section "Exchange" only cards from "Exchange" are returned
    And I am logged into my account
    When I try to search for cards in the section "Exchange" with all of the keywords:
      | Home Baking |
    Then The request succeeds
    And I expect the cards to be returned:
      | Pineapple |

  Scenario: AC2 - I can filter by cards that have any of the selected keywords:
    And I am logged into my account
    When I try to search for cards in the section "Wanted" with any of the keywords:
      | Free    |
      | Vehicle |
    Then The request succeeds
    And I expect the cards to be returned:
      | Apricot  |
      | Cucumber |
      | Apple    |

  Scenario: AC2 - I can filter by cards that have all of the selected keywords:
    And I am logged into my account
    When I try to search for cards in the section "Wanted" with all of the keywords:
      | Free    |
      | Vehicle |
    Then The request succeeds
    And I expect the cards to be returned:
      | Cucumber |

  Scenario: An unauthorised user cannot search cards
    When I try to search for cards in the section "Wanted" with any of the keywords:
      | Free |
    Then The request fails due to not authorised