import random as r
import os

FMNAMES = ["Connor", "Josh", "Ella", "Henry", "Kai", "Ben", "Edward", "April", "May", "June", "Emila", "Frank", "Fergus", "Rose", "Jacob", "Jack", "Danielle"]
LNAMES = ["Jordan", "Mungus", "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Davis", "Thomas", "Taylor", "Lee", "Jackson", "Lewis"]
NICKNAMES = ["Nathan Apple", "EDDDD", "Get Some Sleep", "Protractor", "Cat", "Dog", "Gugu", "Believer", "Posh Petrol Head"]
BIOS = ["I enjoy running on the weekends", "Beaches are fun", "Got to focus on my career", "If only I went to a better university", "Read documentation yeah right", "My cats keep me going", "All I need is food"]

def GenerateDOB():
    """ Randomly generates the user's date of birth """
    day = str(r.randint(1, 28))
    month = str(r.randint(1, 12))
    year = str(r.randint(1900, 2010))
    return year + "-" + month + "-" + day

def GeneratePhNum():
    """ Randomly generates the user's phone number """
    return "027" + str(r.randint(1000000, 9999999))

def GeneratePassword():
    """ Randomly generates the user's password """
    return r.randbytes(4)

def GenerateEmail():
    """ Randomly generates the user's email """
    suffixes = ["@gmail.com", "@hotmail.com", "@yahoo.com", "@uclive.ac.nz", "@xtra.co.nz"]
    return r.choice(FMNAMES) + r.choice(LNAMES) + r.choice(suffixes)

def GenerateAddress():
    """ Randomly generates the user's address """
    streetNum = r.randint(0, 999)
    streetNames = ["Hillary Cresenct", "Elizabeth Street", "Alice Avenue", "Racheal Road", "Peveral Street", "Moorhouse Avenue", "Riccarton Road", "Clyde Road", "Angelic Avenue"]
    cities = ["Dunedin", "Nightcaps", "Gore", "Tapanui", "Wellington", "Christchurch", "Auckland", "Melbourne", "Brisbance", "Sydeny", "Perth", "Darwin", "Alice Springs"]
    regions = ["Otago", "Southland", "Canterbury", "Victoria", "Tasman", "Upper Hutt"]
    countries = ["New Zealand", "Zealand", "Australia", "England", "United Kingdom", "Japan", "Korea", "Singapore", "France", "Germany", "Norway"]
    postcode = r.randint(1000, 99999)
    districts = ["Alpha", "Beta", "Charlie", "Delta", "Echo", "Foxtrot"]
    return streetNum, r.choice(streetNames), r.choice(cities), r.choice(regions), r.choice(countries), postcode, r.choice(districts)

def clear():
    """ Clears console """
    os.system('cls') # clear on windows
    #os.system('clear') # clear on linux

def createInsertAddressSQL():
    """ Creates the sql statement for inserting an address into the database """
    streetNum, streetName, city, region, country, postcode, district = GenerateAddress()
    insertSQL = "INSERT INTO location (street_number, street_name, city, region, country, post_code, district)\n"
    valuesSQL = "VALUES ('{0}', '{1}', '{2}', '{3}', '{4}', '{5}', '{6}');\n".format(streetNum, streetName, city, region, country, postcode, district)
    return streetNum, streetName, insertSQL + valuesSQL

def createInsertAccountSQL():
    """ Creates the sql statement for inserting an account into the database """
    email = GenerateEmail()
    role = "user"
    insertSQL = "INSERT INTO account (email, role, authentication_code)\n"
    valuesSQL = "VALUES ('{0}', '{1}', 'authcode');\n".format(email, role)
    return email, insertSQL + valuesSQL

def createInsertUserSQL(streetNum, streetName, email):
    """ Creates the sql statement for inserting a user into the database """
    first_name, middle_name, last_name = r.choice(FMNAMES), r.choice(FMNAMES), r.choice(LNAMES)
    nickname, bio = r.choice(NICKNAMES), r.choice(BIOS)
    phNum = GeneratePhNum()
    dob = GenerateDOB()
    insertSQL = "INSERT INTO user (first_name, middle_name, last_name, nickname, ph_num, dob, bio, userid, address_id)\n"
    selectSQL = "SELECT '{0}', '{1}', '{2}', '{3}', '{4}', '{5}', '{6}', account.userid, location.id\n".format(first_name, middle_name, last_name, nickname, phNum, dob, bio)
    fromSQL = "FROM account, location\n"
    whereSQL = "WHERE account.email = '{0}' AND location.street_number = '{1}' AND location.street_name = '{2}';\n".format(email, streetNum, streetName)
    return insertSQL + selectSQL + fromSQL + whereSQL

def getUsersFromInput():
    """ Asks the user how many users should be added to the database sql script """
    users = 0
    while users <= 0:
        clear()
        try:
            print("------------------------------------")
            print("How many users do you want generated")
            print("and put into the database?")
            users = int(input(""))
            print("------------------------------------")
        except:
            print("Please enter a number!")
    return users

if __name__ == "__main__":
    users = getUsersFromInput()
    clear()
    file = open("insertUsersScript.sql", "w")
    streetNum, streetName, insertAddressSQL = createInsertAddressSQL()
    file.write(insertAddressSQL)
    file.write('\n')

    for i in range(users):
        clear()
        print("Creating User {0} / {1}".format(i+1, users))
        print("Progress: {:.2f}%".format(((i+1)/users) * 100))
        email, insertAccountSQL = createInsertAccountSQL()
        insertUserSQL = createInsertUserSQL(streetNum, streetName, email)
        file.write(insertAccountSQL)
        file.write(insertUserSQL)
        file.write('\n')
    
    file.close()






