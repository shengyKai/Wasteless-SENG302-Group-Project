Feature: U10 - Modifying Users

  Background:
    Given A user exists with name "Dave" and password "superSecret1"

  Scenario: AC1 - As a user, I can update any of my attributes.
    Given I am logged into "Dave" account
    When I try to update the fields of the user "Dave" to:
      | firstName                | Mike              |
      | lastName                 | Jones             |
      | homeAddress.streetNumber | 10a               |
      | homeAddress.streetName   | Downing Street    |
      | homeAddress.district     | Westminster       |
      | homeAddress.city         | London            |
      | homeAddress.region       | England           |
      | homeAddress.country      | UK                |
      | homeAddress.postcode     | 9999              |
      | middleName               | King              |
      | nickname                 | The King          |
      | bio                      | welcome to my page|
      | phoneNumber              | 02 080356158       |
      | dateOfBirth              | 2001-03-12        |
      | email                    | dave@jones.com    |
      | password                 | superSecret1      |
      | newPassword              | confidential101   |
    Then The request succeeds
    And The user is updated
    
    Scenario: AC1 - As a user, I cannot update any attributes of another user
      Given A user exists with name "Joe"
      And I am logged into "Joe" account
      When I try to update the fields of the user "Dave" to:
        | firstName                | Mike              |
        | lastName                 | Jones             |
        | homeAddress.streetNumber | 10a               |
        | homeAddress.streetName   | Downing Street    |
        | homeAddress.district     | Westminster       |
        | homeAddress.city         | London            |
        | homeAddress.region       | England           |
        | homeAddress.country      | UK                |
        | homeAddress.postcode     | 9999              |
        | middleName               | King              |
        | nickname                 | The King          |
        | bio                      | ~ welcome ~       |
        | phoneNumber              | 02 080356158       |
        | dateOfBirth              | 2001-03-12        |
        | email                    | dave@jones.com    |
        | password                 | superSecret1      |
        | newPassword              | confidential101   |
      Then The request fails due to forbidden
      And The user is not updated

  Scenario: AC1 - As non logged in user I cannot modify any of the attributes
    When I try to update the fields of the user "Dave" to:
      | firstName                | Mike              |
      | lastName                 | Jones             |
      | homeAddress.streetNumber | 10a               |
      | homeAddress.streetName   | Downing Street    |
      | homeAddress.district     | Westminster       |
      | homeAddress.city         | London            |
      | homeAddress.region       | England           |
      | homeAddress.country      | UK                |
      | homeAddress.postcode     | 9999              |
      | middleName               | King              |
      | nickname                 | The King          |
      | bio                      | ~ welcome ~       |
      | phoneNumber              | 02 080356158       |
      | dateOfBirth              | 2001-03-12        |
      | email                    | dave@jones.com    |
      | password                 | superSecret1      |
      | newPassword              | confidential101   |
    Then The request fails due to not authorised

  Scenario: AC2: All validation rules still apply.
    Given I am logged into "Dave" account
    When I try to update the fields of the user "Dave" to:
      | firstName                | 😂                |
      | lastName                 | Jones~            |
      | homeAddress.streetNumber | 10a dd5 5         |
      | homeAddress.streetName   | Downing Street    |
      | homeAddress.district     | Westminster       |
      | homeAddress.city         | ""                |
      | homeAddress.region       | England           |
      | homeAddress.country      | UK                |
      | homeAddress.postcode     | 9999              |
      | middleName               | King              |
      | nickname                 | The King          |
      | bio                      | welcome to my page|
      | phoneNumber              | 0208              |
      | dateOfBirth              | 2018-03-12        |
      | email                    | davejones.com     |
      | password                 | superSecret1      |
      | newPassword              | confident         |
    Then The request fails due to bad request
    And The user is not updated

  Scenario: AC3 - Mandatory attributes must be provided
    Given I am logged into "Dave" account
    When I try to update the fields of the user "Dave" to:
      | firstName | Mikey |
    Then The request fails due to bad request
    And The user is not updated