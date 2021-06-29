<template>
  <div class="page-container">
    <BusinessActionPanel v-if="isBusiness" />
    <UserActionPanel v-else />
    <div class="newsfeed">
      <!-- Newsfeed -->
      <v-card
        v-for="event in newsfeedItems"
        :key="event.id"
        outlined
        rounded="lg"
        class="newsfeed-item"
      >
        <template v-if="event.type == 'demo'">
          <GlobalMessage :event="event"/>
        </template>
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
import { addEventMessageHandler, initialiseEventSourceForUser } from '@/api/events';

export default {
  data() {
    return {
      newsfeedItems: [],
    };
  },
  components: {
    BusinessActionPanel,
    UserActionPanel,
    GlobalMessage,
  },
  created() {
    initialiseEventSourceForUser(this.$store.state.user.id);
    addEventMessageHandler(event => {
      this.newsfeedItems.push(event);
    });
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
  },
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