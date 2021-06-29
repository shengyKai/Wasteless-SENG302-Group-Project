import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Class responsible for generating random person names for use in example data.
 */
class PersonNameGenerator {

    private static PersonNameGenerator instance;

    private static final String EXAMPLE_DATA_FILE_PATH = "./example-data/";
    private static final String NICKNAME_FILE = "nicknames.csv";
    private static final String FEMALE_FIRST_NAMES_FILE = "first-names-female.txt";
    private static final String MALE_FIRST_NAMES_FILE = "first-names-male.txt";
    private static final String LAST_NAMES_FILE = "last-names.txt";

    private Map<String, List<String>> nicknameLookup;
    private List<String> femaleFirstNames;
    private List<String> maleFirstNames;
    private List<String> lastNames;

    private Random random;

    /**
     * Private constructor. Sets up hashmap of potential nicknames and lists of potential first and last names.
     * Also sets up instance of random class for generating random values.
     */
    private PersonNameGenerator() {
        readNicknameFile();
        femaleFirstNames = readNameFile(FEMALE_FIRST_NAMES_FILE);
        maleFirstNames = readNameFile(MALE_FIRST_NAMES_FILE);
        lastNames = readNameFile(LAST_NAMES_FILE);
        random = new Random();
    }

    /**
     * Reads a CSV file where the first column of each line is a name and all other columns are potential nicknames
     * for someone with that name. Generates nicknameLookup HashMap from this data with a name as a key and a list
     * of nicknames as a value.
     */
    private void readNicknameFile() {
        nicknameLookup = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(EXAMPLE_DATA_FILE_PATH + NICKNAME_FILE))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitLine = line.strip().split(",");
                String name = splitLine[0];
                String[] nicknames = new String[splitLine.length - 1];
                System.arraycopy(splitLine, 1, nicknames, 0, splitLine.length - 1);
                nicknameLookup.put(name, Arrays.asList(nicknames));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a text file with a list of names and returns a list where each entry in the list is a name from the file.
     * @param filename The name of the file to be read.
     * @return A list of all names in the file.
     */
    private List<String> readNameFile(String filename) {
        List<String> names = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(EXAMPLE_DATA_FILE_PATH + filename))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String name = line.strip();
                names.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * This function randomly selects and returns either the list of female first names or the list of male first names.
     * There is a 50% chance of either being selected.
     * @return The randomly selected list of names.
     */
    private List<String> selectFirstNameList() {
        List<String> firstNameList;
        if (random.nextBoolean()) {
            firstNameList = femaleFirstNames;
        } else {
            firstNameList = maleFirstNames;
        }
        return firstNameList;
    }

    /**
     * This function randomly selects and returns a name from the given list of names, with uniform probability of any
     * name from the list being selected.
     * @param names A list of names.
     * @return A random name from the list.
     */
    private String randomNameFromList(List<String> names) {
        int index = random.nextInt(names.size());
        return names.get(index);
    }

    /**
     * This function randomly generates a middle name string from the given list of first names. The middle name string
     * can contain be empty, contain one middle name, or contain two middle names seperated by a space.
     * @param names The list of names to select middle name from.
     * @return A string of 0, 1 or 2 names from the list.
     */
    private String randomMiddleName(List<String> names) {
        int numMiddleNames = random.nextInt(3);
        List<String> middleNames = new ArrayList<>();
        for (int i = 0; i < numMiddleNames; i++) {
            middleNames.add(randomNameFromList(names));
        }
        return String.join(" ", middleNames);
    }

    /**
     * This method returns either a randomly selected nickname from the potential nicknames for the given first name,
     * or an empty string. If the given first name exists in the provided nickname file, then there is a 50% chance that
     * a nickname will be randomly selected from the list of nickname for that name, and a 50% chance that the empty
     * string will be returned. If the first name does not exist in the provided nickname file, the empty string will
     * be returned.
     * @param firstName A first name to randomly select a nickname for.
     * @return A nickname corresponding to the given first name or the empty string.
     */
    private String nicknameFromFirstName(String firstName) {
        if (random.nextBoolean() && nicknameLookup.containsKey(firstName)) {
                List<String> potentialNicknames = nicknameLookup.get(firstName);
                return randomNameFromList(potentialNicknames);
            }
        }
        return "";
    }

    /**
     * This method randomly generates a person's full name (firstName, middleName, lastName, nickname).
     * First and last name contain one name from the first and last name files, middle name contains 0-2 names
     * from the first name files and nickname contains 0-1 name from the nickname file.
     * @return A FullName object with randomly generated values.
     */
    public FullName generateName() {

        List<String> firstNameList = selectFirstNameList();

        String firstName = randomNameFromList(firstNameList);
        String middleName = randomMiddleName(firstNameList);
        String lastName = randomNameFromList(lastNames);
        String nickname = nicknameFromFirstName(firstName);

        return new FullName(firstName, middleName, lastName, nickname);
    }

    /**
     * This method creates (if necessary) and returns the singleton instance of the PersonNameGenerator class.
     * @return The PersonNameGenerator singleton.
     */
    public static PersonNameGenerator getInstance() {
        if (instance == null) {
            instance = new PersonNameGenerator();
        }
        return instance;
    }

    /**
     * A class to make it easier to access the fields of the full name returned by the generateName method.
     */
    public class FullName {

        private FullName(String firstName, String middleName, String lastName, String nickname) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.nickname = nickname;
        }

        private String firstName;
        private String middleName;
        private String lastName;
        private String nickname;

        /**
         * @return The first name attribute (a single name).
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * @return The middle name attribute (0-2 names).
         */
        public String getMiddleName() {
            return middleName;
        }

        /**
         * @return The last name attribute (a single name).
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @return The nickname attribute (0-1 names).
         */
        public String getNickname() {
            return nickname;
        }

        /**
         * This method formats the first, middle, last and nickname fields as a comma seperated list to make it easier
         * to see which value belongs to which field.
         * @return A comma seperated list representing the full name.
         */
        @Override
        public String toString() {
            String[] namelist = new String[] {firstName, middleName, lastName, nickname};
            return String.join(",", namelist);
        }
    }

    /**
     * This method exists only for the purpose of manual testing and reviewing. Remove before merging to dev.
     */
    public static void main(String[] args) {

        PersonNameGenerator nameGenerator = PersonNameGenerator.getInstance();
        Instant startTime = Instant.now();

        List<FullName> randomNames = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            randomNames.add(nameGenerator.generateName());
        }

        Instant endTime = Instant.now();

        System.out.println("Execution time: " + Duration.between(startTime, endTime).getNano());

        for (int i = 0; i < 20; i++) {
            System.out.println(randomNames.get(i));
        }
    }

}