Feature: UCM8 Contacting other marketplace users
  Background:
    Given A user exists with name "Doris"
    And a card exists with title "Pet rock" and creator "Doris"
    And A user exists with name "Joe"

  Scenario: AC2 - I can send a message to a card creator
    Given I am logged into "Joe" account
    And I create a message for a given marketplace card owner
    When I send the message
    Then I expect the message to be sent

  Scenario: AC3 - Sending a message results in a new item on the recipient's newsfeed
    Given user "Joe" has sent a message regarding card "Pet rock"
    And I am logged into "Doris" account
    When I check my notification feed
    Then I have received a notification from a conversation that I am involved in
    And the card title "Pet rock" is included in the notification
    And the buyer name "Joe" is included in the message

  Scenario: Sending a message results in a new item on the senders newsfeed
    Given user "Joe" has sent a message regarding card "Pet rock"
    And I am logged into "Joe" account
    When I check my notification feed
    Then I have received a notification from a conversation that I am involved in
    And the card title "Pet rock" is included in the notification
    And the card owner name "Doris" is included in the notification

  Scenario: AC5 - I can reply to a message in my newsfeed
    Given user "Joe" has sent a message regarding card "Pet rock"
    And I am logged into "Doris" account
    And I have received a notification from user "Joe" regarding the card "Pet rock"
    When I reply to the message with "Thanks for your interest"
    Then the message "Thanks for your interest" is added to the conversation with "Joe"

  Scenario: AC5 - The original sender receives a notification when I reply to a message
    Given user "Joe" has sent a message regarding card "Pet rock"
    And user "Doris" has sent a reply
    And I am logged into "Joe" account
    When I check my notification feed
    Then I have received a notification from a conversation that I am involved in
    And the card title "Pet rock" is included in the notification
    And the card owner name "Doris" is included in the notification
