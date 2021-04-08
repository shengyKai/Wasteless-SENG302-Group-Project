<template>
  <div class="container">
    <div class="newsfeed">
      <!-- Inventory items -->
      <v-card
        v-if="isBusiness"
        outlined
        rounded="lg"
        class="inventory-card"
      >
        <v-card-title>
          Inventory
        </v-card-title>
        <v-card-text class="inventory-container">
          <v-card
            v-for="(item, index) in inventoryItems"
            :key="index"
            class="inventory-item"
          >
            <v-img
              src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg"
              height="50px"
            />
            {{ item }}
          </v-card>
        </v-card-text>
      </v-card>

      <!-- Newsfeed -->
      <v-card
        v-for="(item, index) in newsfeedItems"
        :key="index"
        outlined
        rounded="lg"
        class="newsfeed-item"
      >
        <v-card-title>
          {{ item.title }}
        </v-card-title>
        <v-card-text>
          <pre>{{ item.content }}</pre>
        </v-card-text>
      </v-card>
    </div>
    <v-card
      rounded="lg"
      class="action-pane"
    >
      <!-- User actions -->
      <v-card-text>
        <v-list>
          <v-list-item-group>
            <v-list-item @click="viewProfile">
              <v-list-item-icon>
                <v-icon>mdi-account-circle</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Profile</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewCreateBusiness">
              <v-list-item-icon>
                <v-icon>mdi-briefcase-plus</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Add Business</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card-text>
    </v-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      newsfeedItems: [
        {
          title: "Somebody commented on your post",
          content: "Hey this is a comment",
        },
        {
          title: "10 Shocking FACTS, Number 7 will...",
          content:
            "1. The sky is blue\n2. All squares have 4 corners\n3. Exposure to PHP decreases lifespan by 10 years\n4. I'm out of ideas",
        },
      ],
    };
  },
  methods: {
    viewProfile() {
      // TODO It would probably be quite nice to merge this with the appbar implementation, maybe by emitting something
      switch (this.role.type) {
      case "user":
        this.$router.push("/profile");
        break;
      case "business":
        this.$router.push("/business/" + this.$store.state.activeRole.id);
        break;
      default:
        this.$router.push("/profile");
      }
    },

    viewCreateBusiness() {
      // TODO Implement
      console.warn('Not implemented');
    }
  },
  computed: {
    role() {
      return this.$store.state.activeRole;
    },
    isBusiness() {
      return this.role?.type === "business";
    },
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

.container {
  display: flex;
  max-width: 900px;
  padding: 10px;
}

.newsfeed {
  flex: 2;
  min-width: 260px;
}

.inventory-card {
  margin-bottom: 10px;
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
  margin-left: 10px;
  flex: 1;
}
</style>