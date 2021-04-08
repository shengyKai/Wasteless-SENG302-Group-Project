<template>
  <div class="container">
    <div class="pane">Left pane</div>
    <div class="newsfeed">
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
    <div class="pane">Right pane</div>
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
          title: "10 Shocking facts, number 7 will...",
          content:
            "1. The sky is blue\n2. All squares have 4 corners\n3. Exposure to PHP is decreases lifespan by 10 years\n4. I'm out of ideas",
        },
      ],
    };
  },

  computed: {
    isBusiness() {
      return this.$store.state.activeRole?.type === "business";
    },
  },
};
</script>

<style scoped>
.container {
  display: flex;
}

.newsfeed {
  max-width: 600px;
  flex: 2;
}

.newsfeed-item {
  margin-top: 10px;
}

.pane {
  flex: 1;
}
</style>