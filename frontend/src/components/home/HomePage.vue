<template>
  <div class="page-container">
    <BusinessActionPanel v-if="isBusiness" />
    <UserActionPanel v-else />
    <div class="newsfeed">
      <!---Select component for the order in which the cards should be displayed--->
      <v-select
        color="primary"
        v-model="filterBy"
        flat
        solo-inverted
        hide-details
        :items="[
          { text: 'Tag filter 1', value: 'tag1', color: 'red'},
          { text: 'Tag filter 2', value: 'tag2', color: 'blue'},
          { text: 'Tag filter 3', value: 'tag3', color: 'green'},
          { text: 'Tag filter 4', value: 'tag4', color: 'red'},
          { text: 'Tag filter 5', value: 'tag5', color: 'blue'},
          { text: 'Tag filter 6', value: 'tag6', color: 'green'},
          { text: 'Tag filter 7', value: 'tag7', color: 'red'},
          { text: 'Tag filter 8', value: 'tag8', color: 'blue'},
          { text: 'Tag filter 9', value: 'tag9', color: 'green'},
          { text: 'Tag filter 0', value: 'tag0', color: 'red'},
        ]"
        prepend-inner-icon="mdi-sort-variant"
        label="Filter by"
        multiple
        chips
      >
        <template #selection="{ item }">
          <v-chip :color="item.color">{{item.text}}</v-chip>
        </template>
      </v-select>
      <!-- Newsfeed -->
      <div v-if="$store.getters.events.length === 0 || isBusiness" class="text-center">
        No items in your feed
      </div>
      <v-card
        v-else
        v-for="event in $store.getters.events"
        :key="event.id"
        outlined
        rounded="lg"
        class="newsfeed-item"
      >
        <GlobalMessage v-if="event.type === 'MessageEvent'" :event="event"/>
        <ExpiryEvent v-else-if="event.type === 'ExpiryEvent'" :event="event"/>
        <DeleteEvent v-else-if="event.type === 'DeleteEvent'" :event="event"/>
        <KeywordCreated v-else-if="event.type === 'KeywordCreatedEvent'" :event="event"/>
        <template v-else>
          <v-card-title>
            {{ event.type }}
          </v-card-title>
          <v-card-text>
            <pre>{{ event }}</pre>
          </v-card-text>
        </template>
      </v-card>
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

export default {
  components: {
    BusinessActionPanel,
    UserActionPanel,
    GlobalMessage,
    ExpiryEvent,
    DeleteEvent,
    KeywordCreated,
  },
  data() {
    return {
      filterBy: "",
      filterList: []
    };
  },
  computed: {
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