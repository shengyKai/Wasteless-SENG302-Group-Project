Feature: User

    Scenario: Create a user account
        Given the user with the email "connor@gmail.com" does not exist
        When a user is created where their first name is "Fergus"
        And their middle name is "Connor"
        And their last name is "Hitchcock"
        And their nickname is "Con"
        And their email is "connor@gmail.com"
        And their password is "gottaGetGoodGrad3$"
        And their bio is "your average fourth year software engineer"
        And their dob is "17/7/1999"
        And their phone number is "0271234567"
        And their address is "69 Riccarton Road, Christchurch, Canterbury, New Zealand, 8041"
        Then a user with the email "connor@gmail.com" exists
        And their first name is "Fergus"
        And their middle name is "Connor"
        And their last name is "Hitchcock"
        And their nickname is "Con"
        And their email is "connor@gmail.com"
        And their password is "gottaGetGoodGrad3$"
        And their bio is "Your average fourth year software engineer"
        And their dob is "17/07/1999"
        And their phone number is "0271234567"
        And their address is "69 Riccarton Road, Christchurch, Canterbury, New Zealand, 8041"

    Scenario: Create a second user account
        Given the user with the email "roseyrose@gmail.com" does not exist
        When a user is created where their first name is "Rosey"
        And their middle name is "Red"
        And their last name is "Rose"
        And their nickname is "Peach"
        And their email is "roseyrose@gmail.com"
        And their password is "prettyRoseSweetP3@ch"
        And their bio is "My friends say I am as sweet as a peach with the looks of a rose"
        And their dob is "1/3/1998"
        And their phone number is "0279876543"
        And their address is "100 Ocean View Crescent, Auckland, Rakino Island, New Zealand, 1010"
        Then a user with the email "roseyrose@gmail.com" exists
        And their first name is "Rosey"
        And their middle name is "Red"
        And their last name is "Rose"
        And their nickname is "Peach"
        And their email is "roseyrose@gmail.com"
        And their password is "prettyRoseSweetP3@ch"
        And their bio is "My friends say I am as sweet as a peach with the looks of a rose"
        And their dob is "1/3/1998"
        And their phone number is "0279876543"
        And their address is "100 Ocean View Crescent, Auckland, Rakino Island, New Zealand, 1010"