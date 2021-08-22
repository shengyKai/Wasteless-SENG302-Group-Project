Feature: U11 - Modifying Businesses
  Background:
    Given A user exists with name "Dave"
    And the business "Dave's playhouse" exists

  Scenario: AC1 - As an administrator I can modify any of the attributes
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | Dave's Prison     |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | false             |
    Then The request succeeds
    And The business is updated

  Scenario: AC1 - As non administrator I cannot modify any of the attributes
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | Dave's Prison     |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | false             |
    Then The request fails due to forbidden
    And The business is not updated

  Scenario: AC1 - As non logged in user I cannot modify any of the attributes
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | Dave's Prison     |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | false             |
    Then The request fails due to not authorised
    And The business is not updated

  Scenario: AC2 - Mandatory attributes must be provided
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
     | description | Hey |
    Then The request fails due to bad request
    And The business is not updated

  Scenario: AC2 - Name must remain valid
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | 😂😂              |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | false             |
    Then The request fails due to bad request
    And The business is not updated

  Scenario: AC2 - Description must remain valid
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | Dave's Prison     |
      | description          | 😂😂              |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | false             |
    Then The request fails due to bad request
    And The business is not updated

  Scenario: AC2 - Business type must remain valid
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | Dave's Prison     |
      | description          | 😂😂              |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Traders    |
      | updateProductCountry | false             |
    Then The request fails due to bad request
    And The business is not updated

  Scenario: AC2 - Address must remain valid
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave              |
      | name                 | Dave's Prison     |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | 😂😂              |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Traders    |
      | updateProductCountry | false             |
    Then The request fails due to bad request
    And The business is not updated

  Scenario: AC2 - I cannot change the business owner to a user that I do not have permissions for
    Given A user exists with name "Tim"
    And I am logged into "Dave" account
    And There are products related to the business
    When I try to updated the fields of the business to:
      | primaryAdministrator | Tim               |
      | name                 | Dave's Prison     |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | false             |
    Then The request fails due to bad request
    And The business is not updated

  Scenario: AC3 - I choose to update the currency of all of my business products based on the new address
    Given I am logged into "Dave" account
    When I try to updated the fields of the business to:
      | primaryAdministrator | Dave               |
      | name                 | Dave's Prison     |
      | description          | Wow, really cool  |
      | address.streetNumber | 10                |
      | address.streetName   | Downing Street    |
      | address.district     | Westminster       |
      | address.city         | London            |
      | address.region       | England           |
      | address.country      | UK                |
      | address.postcode     | 9999              |
      | businessType         | Retail Trade      |
      | updateProductCountry | true              |
    Then The all of the business product's country of sale is updated

  Scenario: AC1 - A non logged in user cannot upload images
    When I try to upload the image "point.png" to the business
    Then The request fails due to not authorised
    And The business has no images

  Scenario: AC1 - A user that is not a business admin cannot upload images
    Given A user exists with name "Tim"
    And I am logged into "Tim" account
    When I try to upload the image "point.png" to the business
    Then The request fails due to forbidden
    And The business has no images

  Scenario: AC4 - I can upload a .png image to the business
    Given I am logged into "Dave" account
    When I try to upload the image "point.png" to the business
    Then The request succeeds and a entity is created
    And The business has one image

  Scenario: AC4 - I can upload a .jpg image to the business
    Given I am logged into "Dave" account
    When I try to upload the image "point.jpg" to the business
    Then The request succeeds and a entity is created
    And The business has one image

  Scenario: AC4 - I cannot upload a non-image as an image
    Given I am logged into "Dave" account
    When I try to upload the image "point.txt" to the business
    Then The request fails due to bad request
    And The business has no images

  Scenario: AC1 - A user that is not a business admin cannot set the primary image
    Given A user exists with name "Joe"
    And I am logged into "Joe" account
    And The business "Dave's playhouse" has primary image "point.jpg"
    And The business "Dave's playhouse" has image "apple.jpg"
    When I try to set the primary image for "Dave's playhouse" to "apple.jpg"
    Then The request fails due to forbidden
    And Image "point.jpg" is the primary image for "Dave's playhouse"

  Scenario: AC5 - I can set the primary image for my business
    Given I am logged into "Dave" account
    And The business "Dave's playhouse" has primary image "point.jpg"
    And The business "Dave's playhouse" has image "apple.jpg"
    When I try to set the primary image for "Dave's playhouse" to "apple.jpg"
    Then The request succeeds
    And Image "apple.jpg" is the primary image for "Dave's playhouse"