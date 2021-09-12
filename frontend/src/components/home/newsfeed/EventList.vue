<template>
  <div class="mt-2">
    <template v-for="event in eventsPageWithSpacers">
      <div
        v-if="typeof event === 'string'"
        :key="event"
        class="full-width"
        style="position: relative; height: 20px"
      >
        <v-divider
          class="full-width center-vertical"
        />
        <div
          class="font-weight-medium center-horisontal secondary--text white"
        >
          {{ event }}
        </div>
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
        <InterestEvent v-else-if="event.type === 'InterestEvent'" :event="event"/>
        <Event v-else :title="event.type">
          <pre>{{ event }}</pre>
        </Event>
      </v-card>
    </template>
    <!--paginate results-->
    <v-pagination
      v-if="events.length !== 0"
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
</template>

<script>
import DeleteEvent from './DeleteEvent.vue';
import ExpiryEvent from './ExpiryEvent.vue';
import GlobalMessage from "./GlobalMessage.vue";
import KeywordCreated from './KeywordCreated.vue';
import MessageEvent from './MessageEvent.vue';
import InterestEvent from "@/components/home/newsfeed/InterestEvent";

export default {
  name: 'EventList',
  components: {InterestEvent, DeleteEvent, ExpiryEvent, GlobalMessage, KeywordCreated, MessageEvent},
  props: ['events', 'isFiltered'],
  data() {
    return {
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,
    };
  },
  computed: {
    /**
     * Total number of results from the store
     */
    totalResults() {
      if (this.events.length === undefined) return 0;
      return this.events.length;
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
        return 'No items in this feed';
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
     * The events list after pagination for each page
     */
    eventsPage() {
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      return this.events.slice(pageStartIndex, (pageStartIndex + this.resultsPerPage));
    },
  }
};
</script>
<style scoped>

pre {
  white-space: pre-wrap;
}

.newsfeed-item {
  margin-bottom: 10px;
}

.full-width {
  left: 0%;
  right: 0%;
}

.center-horisontal {
  position: absolute;
  left: 50%;
  transform: translate(-50%, 0);
}

.center-vertical {
  position: absolute;
  top: 50%;
  transform: translate(0, -50%);
}
</style>