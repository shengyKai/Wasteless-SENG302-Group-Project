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
      <v-tabs
        v-model="tab">
        <v-tabs-slider/>
        <v-tab href="#all-events-tab">
          <v-icon>mdi-home</v-icon>
          All
        </v-tab>
        <v-tab href="#archived-events-tab">
          <v-icon>mdi-archive</v-icon>
          Archived
        </v-tab>
      </v-tabs>
      <!-- Tab content -->
      <v-tabs-items v-model="tab">
        <v-tab-item value="all-events-tab" :eager="true">
          <EventList :events="mainEvents" :is-filtered="isFiltered" ref="mainEvents"/>
        </v-tab-item>
        <v-tab-item value="archived-events-tab" :eager="true">
          <EventList :events="archivedEvents" :is-filtered="isFiltered" ref="archivedEvents"/>
        </v-tab-item>
      </v-tabs-items>
    </div>
  </div>
</template>

<script>
import BusinessActionPanel from "./BusinessActionPanel";
import UserActionPanel from "./UserActionPanel";
import EventList from "@/components/home/newsfeed/EventList";

export default {
  components: {
    BusinessActionPanel,
    UserActionPanel,
    EventList,
  },
  data() {
    return {
      /**
       * The list of colour attributes to filter by
       */
      filterBy: [],
      /**
       * A list for the v-select component so that the text and the value can be displayed appropriately
       */
      colours: [{text: "None", value: 'none'}, {text: "Red", value: 'red'}, {text: "Orange", value: 'orange'},
        {text: "Yellow", value: 'yellow'}, {text: "Green", value: 'green'}, {text: "Blue", value: 'blue'},
        {text: "Purple", value: 'purple'}],
      tab: "all-events-tab",
      /**
       * This attribute is used to set the polling interval when the component is created and stop it when the
       * page is destroyed.
       */
      polling: undefined,
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
      if (!this.isFiltered) return this.storeEvents;
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
     * Returns events that have been archived
     */
    archivedEvents() {
      return this.categorisedEvents.archived;
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
  },
  methods: {
    /**
     * Refresh the events for the user's newsfeed every 3 seconds.
     */
    startPolling() {
      this.$store.dispatch('refreshEventFeed');
      this.polling = setInterval(() => {
        this.$store.dispatch('refreshEventFeed');
      }, 7500);
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
  },
  /**
   * Initiate polling for the newsfeed events.
   */
  created() {
    this.startPolling();
  },
  /**
   * Stop polling for the newsfeed events.
   */
  beforeDestroy () {
    clearInterval(this.polling);
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



.newsfeed-item {
  margin-bottom: 10px;
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