import csv
import random
#list of country names to be randomized
COUNTRY_LIST = ["New Zealand", "Singapore", "Malaysia", "China", 
                "United States of America", "Sweden", "Norway", "Indonesia",
                "Canada", "Japan", "Korea", "Iceland", "Kenya", "Thailand", 
                "Ireland", "Belgium", "Italy", "Argentina", "Brazil",
                "Australia"]
#list of state names around the world to be randomized
STATE_LIST = ["Alaska", "California", "Florida", "Canterbury", "Auckland",
              "Bay of Plenty", "Hubei Province", "Fujian Province",
              "Hainan Province", "Munster", "Ulster", "Leinster", "Bangkok",
              "Krabi", "Phuket", "Tokyo", "Kyoto", "Okinawa", "Nelson", "Otago"]
#list of district names around the world to be randomized
DISTRICT_LIST = ["", "Kaipara District", "New Plymouth District",
                 "Carterton District", "Beaver County", "Big Lakes County",
                 "Camrose County", "Cardston County", "Gambir", "Menteng",
                 "Senen", "Johor Bahru District", "Jeli District", 
                 "Raub District", "Ruapehu District", "South Wairarapa District",
                 "Hastings District", "Manawatu District", "Southland District",
                 "Tasman District"]

with open('nz-street-address.csv', encoding="utf8") as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    chosen_row = random.choice(list(csv_reader))
    print(", ".join(chosen_row))
    #line_count = 0
    #for row in csv_reader:
        #if line_count == 0:
            #print(f'Column names are {", ".join(row)}')
            #line_count += 1
            #break;
        #else:
            #print(f'\t{row[0]} works in the {row[1]} department, and was born in {row[2]}.')
            #line_count += 1
    #print(f'Processed {line_count} lines.')