package cucumber.stepDefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.seng302.entities.Business;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static cucumber.stepDefinitions.UserStepDefinition.getLastUser;

public class BusinessStepDefinition {

    private static final Map<String, Business> businessMap = new HashMap<>();
    private static Business lastBusiness = null;

    @Autowired
    private BusinessRepository businessRepository;

    @Before
    public void setup() {
        businessMap.clear();
        lastBusiness = null;
    }

    public static Business getLastBusiness() {
        return lastBusiness;
    }

    public static Business getBusinessByName(String name) {
        return businessMap.get(name);
    }

    @Given("the business {string} exists")
    public void businessExists(String name) throws ParseException {

        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(getLastUser())
                .build();
        lastBusiness = businessRepository.save(business);
        businessMap.put(name, lastBusiness);
    }
}
