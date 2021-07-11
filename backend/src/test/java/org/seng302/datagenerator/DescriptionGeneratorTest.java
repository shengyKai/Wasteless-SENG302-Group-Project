package org.seng302.datagenerator;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DescriptionGeneratorTest {
    DescriptionGenerator descGen = new DescriptionGenerator();

    @Test
    public void randomDescriptionTest() {
        String description = descGen.randomDescription();
        assert(description.length() < 200 && description.length() > 0);
    }
}
