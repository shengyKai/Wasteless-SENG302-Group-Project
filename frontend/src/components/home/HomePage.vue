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
      <!-- Tab selection -->
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
        <v-tab-item value="all-events-tab">
          <EventList :events="eventsPage"/>
        </v-tab-item>
        <v-tab-item value="archived-events-tab">
          <EventList :events="archivedEventsPage"/>
        </v-tab-item>
      </v-tabs-items>
      <!--paginate results-->
      <v-pagination
        v-if="storeEvents.length !== 0 || !isBusiness"
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
import EventList from "@/components/home/newsfeed/EventList";

export default {
  components: {
    EventList,
    BusinessActionPanel,
    UserActionPanel
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
        {text: "Purple", value: 'purple'}],
      /**
       * The index of the current tab
       */
      tab: 0,
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
     * List of inventory items to be shown
     */
    inventoryItems() {
      if (!this.isBusiness) return undefined;
      return [...Array(10).keys()].map(i => `Item ${i}`);
    },
    /**
     * Total number of results from the store
     */
    totalResults() {
      if (this.events.length === undefined) return 0;
      switch (this.tab) {
      case "archived-events-tab":
        return this.archivedEvents.length;
      default:
        return this.events.length;
      }
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
        switch (this.tab) {
        case "archived-events-tab":
          return "No archived events in your feed";
        default:
          return 'No items in your feed';
        }
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
    /**
     * The events list which is filtered after retrieving from the store.
     * Archived events are filtered out.
     */
    events() {
      if (this.filterBy.length === 0) return this.storeEvents.filter(event => {
        return event.status !== 'archived';
      });
      return this.storeEvents.filter(event => {
        return this.filterBy.includes(event.tag)
            && event.status !== 'archived';
      });
    },
    /**
     * The list of events which are archived. Event filter still applies
     */
    archivedEvents() {
      if (this.filterBy.length === 0) return this.storeEvents.filter(event => {
        return event.status === 'archived';
      });
      return this.events.filter(event => {
        return this.filterBy.includes(event.tag)
            && event.status === 'archived';
      });
    },
    /**
     * The events list after pagination for each page
     */
    eventsPage() {
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      return this.events.slice(pageStartIndex, (pageStartIndex + this.resultsPerPage));
    },
    /**
     * The archived events list after pagination for each page
     */
    archivedEventsPage() {
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      return this.archivedEvents.slice(pageStartIndex, (pageStartIndex + this.resultsPerPage));
    },
    /**
     * An attribute to check if the events list is a filtered events list or not
     */
    isFiltered() {
      return this.filterBy.length !== 0;
    }
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
    },
    /**
     * Watch changes in the current selected tab so that the current page can be reset
     */
    tab: function() {
      this.currentPage = 1;
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