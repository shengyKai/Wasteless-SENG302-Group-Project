package org.seng302.Controllers;

import org.junit.runner.RunWith;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BusinessRepository businessRepository;

    /**
     * Tests to create:
     *
     * - Retrieve a catalogue with several products (code 200)
     * - Retrieve a catalogue with zero products (code 200)
     * - Check the when an invalid auth token is provided permission is denied (code 401)
     * - Check when the business does not exist there a exception thrown (code 406)
     * - Check when the user is not an admin a forbidden is thrown (code 403)
     * - Check a DGAA can retrieve catalogues
     */


}
