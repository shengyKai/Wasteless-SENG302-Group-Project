package org.seng302.leftovers.persistence;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.seng302.leftovers.entities.Keyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeywordRepositoryTest {
    @Autowired
    private KeywordRepository keywordRepository;

    @BeforeAll
    void init() {
        keywordRepository.deleteAll();
    }


    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
        keywordRepository.deleteAll();
    }

    @Test
    void findByName_nameDoesNotExist_nothingReturned() {
        keywordRepository.save(new Keyword("This"));

        assertEquals(Optional.empty(), keywordRepository.findByName("That"));
    }

    @Test
    void findByName_nameExists_expectedKeywordFound() {
        keywordRepository.save(new Keyword("This"));
        Keyword keyword = keywordRepository.save(new Keyword("That"));

        Optional<Keyword> found = keywordRepository.findByName("That");
        assertTrue(found.isPresent());
        assertEquals(keyword.getID(), found.get().getID());
        assertEquals(keyword.getName(), found.get().getName());
    }
}
