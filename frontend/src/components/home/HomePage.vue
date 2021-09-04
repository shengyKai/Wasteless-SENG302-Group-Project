<template>
  <div class="page-container">
    <BusinessActionPanel v-if="isBusiness" />
    <UserActionPanel v-else />
    <div class="newsfeed" v-if="!isBusiness">
      <!---Select component for the order in which the cards should be displayed--->
      <v-select
        color="primary"
        v-model="filterBy"
        class="mb-1"
        flat
        solo-inverted
        hide-details
        :items="colours"
        prepend-inner-icon="mdi-sort-variant"
        label="Filter by"
        multiple
        chips
      >
        <!--Allows to access each chip's property in the v-select so that manipulation of the chip's color is possible -->
        <template #selection="{ item }">
          <v-chip :color="item.value"
                  label
                  text-color="white">
            <v-icon left>
              mdi-label
            </v-icon>
          </v-chip>
        </template>
      </v-select>
      <!-- Newsfeed -->
      <!-- Example message - move inside for loop once message is an event type -->
      <template v-for="(event, index) in eventsPageWithSpacers">
        <div v-if="typeof event === 'string'" :key="event">
          <v-divider class="py-1" v-if="index !== 0"/>
          <div class="text--secondary subtitle-1">{{ event }}</div>
        </div>
        <v-card
          v-else
          :key="event.id"
          outlined
          rounded="lg"
          class="newsfeed-item"
        >
          <GlobalMessage v-if="event.type === 'GlobalMessageEvent'" :event="event"/>
          <ExpiryEvent v-else-if="event.type === 'ExpiryEvent'" :event="event"/>
          <DeleteEvent v-else-if="event.type === 'DeleteEvent'" :event="event"/>
          <KeywordCreated v-else-if="event.type === 'KeywordCreatedEvent'" :event="event"/>
          <MessageEvent v-else-if="event.type === 'MessageEvent'" :event="event"/>
          <Event v-else :title="event.type">
            <pre>{{ event }}</pre>
          </Event>
        </v-card>
      </template>
      <!--paginate results-->
      <v-pagination
        v-if="mainEvents.length !== 0 || !isBusiness"
        v-model="currentPage"
        :total-visible="10"
        :length="totalPages"
        circle
      />
      <!--Text to display range of results out of total number of results-->
      <v-row justify="center" no-gutters>
        {{ resultsMessage }}
      </v-row>
    </div>
  </div>
</template>

<script>
import BusinessActionPanel from "./BusinessActionPanel";
import UserActionPanel from "./UserActionPanel";
import GlobalMessage from "./newsfeed/GlobalMessage.vue";
import ExpiryEvent from './newsfeed/ExpiryEvent.vue';
import DeleteEvent from './newsfeed/DeleteEvent.vue';
import KeywordCreated from './newsfeed/KeywordCreated.vue';
import MessageEvent from './newsfeed/MessageEvent.vue';
import Event from './newsfeed/Event.vue';

export default {
  components: {
    BusinessActionPanel,
    UserActionPanel,
    GlobalMessage,
    ExpiryEvent,
    DeleteEvent,
    KeywordCreated,
    MessageEvent,
    Event,
  },
  data() {
    return {
      /**
       * The list of colour attributes to filter by
       */
      filterBy: [],
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,
      /**
       * A list for the v-select component so that the text and the value can be displayed appropriately
       */
      colours: [{text: "None", value: 'none'}, {text: "Red", value: 'red'}, {text: "Orange", value: 'orange'},
        {text: "Yellow", value: 'yellow'}, {text: "Green", value: 'green'}, {text: "Blue", value: 'blue'},
        {text: "Purple", value: 'purple'}]
    };
  },
  computed: {
    /**
     * The events retrieved from the store
     */
    storeEvents() {
      return this.$store.getters.events;
    },
    /**
     * The events list which is filtered after retrieving from the store
     */
    filteredEvents() {
      if (this.filterBy.length === 0) return this.storeEvents;
      return this.storeEvents.filter(event => {
        return this.filterBy.includes(event.tag);
      });
    },
    /**
     * Events categorised by type (normal, starred, archived) and ordered by creation date
     */
    categorisedEvents() {
      let events = {normal: [], starred: [], archived: []};
      for (let event of this.filteredEvents) {
        events[event.status].push(event);
      }
      return events;
    },
    /**
     * Events that should be shown in the main flow of events
     * All events that are not archived with the starred events first
     */
    mainEvents() {
      return [...this.categorisedEvents.starred, ...this.categorisedEvents.normal];
    },
    /**
     * The events list after pagination for each page
     */
    eventsPage() {
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      return this.mainEvents.slice(pageStartIndex, (pageStartIndex + this.resultsPerPage));
    },
    /**
     * Page of events with additional string elements between transitions in event status
     */
    eventsPageWithSpacers() {
      let prevEvent = undefined;
      let events = [];
      for (let event of this.eventsPage) {
        if (prevEvent?.status !== event.status) {
          if (event.status === 'normal') {
            events.push('Unstarred');
          } else if (event.status === 'starred') {
            events.push('Starred');
          }
        }
        events.push(event);
        prevEvent = event;
      }
      return events;
    },
    /**
     * An attribute to check if the events list is a filtered events list or not
     */
    isFiltered() {
      return this.filterBy.length !== 0;
    },
    /**
     * Current active user role
     */
    role() {
      return this.$store.state.activeRole;
    },
    /**
     * Is the current user acting as a business
     */
    isBusiness() {
      return this.role?.type === "business";
    },
    /**
     * Total number of results from the store
     */
    totalResults() {
      if (this.mainEvents.length === undefined) return 0;
      return this.mainEvents.length;
    },
    /**
     * The total number of pages required to show all the events
     * May be 0 if there are no results
     */
    totalPages () {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many events there are
     */
    resultsMessage() {
      if (this.totalResults === 0 && !this.isFiltered) {
        return 'No items in your feed';
      } else if (this.totalResults === 0 && this.isFiltered) {
        return 'No items matches the filter';
      }
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      let pageEndIndex = pageStartIndex + this.resultsPerPage;
      if (pageEndIndex > this.totalResults) {
        pageEndIndex = pageStartIndex + (this.totalResults % this.resultsPerPage);
      }
      return `Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
  },
  watch: {
    /**
     * Watch the changes in filterBy because if the user is in the second page while the page is filtered, the currentPage will
     * not be reverted back to the first page, which will cause the events to not be sliced/filtered properly.
     */
    filterBy: function() {
      this.currentPage = 1;
    },
    /**
     * Ensures that the current page is at least 1 and less than or equal to the total number of pages.
     */
    totalPages: function() {
      this.currentPage = Math.max(Math.min(this.currentPage, this.totalPages), 1);
    }
  }
};
</script>

<style scoped>

pre {
  white-space: pre-wrap;
}

.page-container {
  display: flex;
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  max-width: 900px;
}

@media all and (min-width: 992px) {
  .page-container {
    padding: 10px;
  }
}

@media not all and (min-width: 992px) {
  .page-container {
    padding-right: 5px;
    padding-top: 10px;
  }
}

.newsfeed {
  flex: 1;
  min-width: 260px;
}

.inventory-container {
  display: flex;
  flex-flow: row wrap;
}

.inventory-item {
  max-width: 100px;
  margin: 5px;
}

.newsfeed-item {
  margin-bottom: 10px;
}

.action-pane {
  margin-right: 10px;
  max-height: 400px;
}


@media not all and (min-width: 992px) {
  .action-pane {
    display: block;
  }
}

.action-button {
  display: block;
  margin: 10px;
}

</style>