package org.seng302.Controllers;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Entities.DefaultGlobalApplicationAdmin;
import org.seng302.Persistence.DGAARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DGAAControllerTest {

    @Autowired
    private DGAARepository dgaaRepository;

    @BeforeEach
    public void clean() {
        dgaaRepository.deleteAll();
    }

    /**
     * DGAA repo is empty, ensure a new DGAA is generated
     */
    @Test @Ignore
    public void dgaaNotPresent() {
        DGAAController.checkDGAA(dgaaRepository);
        assert(dgaaRepository.findByEmail("wasteless@seng302.com") != null);
    }

    /**
     * DGAA already exists, no need for new one
     */
    @Test @Ignore
    public void dgaaPresent() {
        DefaultGlobalApplicationAdmin dgaa = new DefaultGlobalApplicationAdmin();
        dgaaRepository.save(dgaa);
        DGAAController.checkDGAA(dgaaRepository);
        // DGAA in repo, check that .checkDGAA() doesn't make a duplicate
        Iterable<DefaultGlobalApplicationAdmin> dgaas = dgaaRepository.findAll();
        int count = 0;
        for (DefaultGlobalApplicationAdmin ignored : dgaas) {
            count++;
        }
        assert(count == 1);
    }
}
