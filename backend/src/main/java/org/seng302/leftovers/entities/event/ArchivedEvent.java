package org.seng302.leftovers.entities.event;

import javax.persistence.*;

@Entity
public class ArchivedEvent{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Event event;

    protected ArchivedEvent() {} // Required by JPA

    public ArchivedEvent(Event event) {
        this.event = event;
    }
}
