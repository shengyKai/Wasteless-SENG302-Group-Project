<template>
  <v-card width="300px">
    <v-card-title class="my-n1 title">
      {{ content.title }}
    </v-card-title>
    <v-card-text class="my-n4">
      <strong v-if="location">From {{ location.district }}, {{ location.city }} </strong>
      <br>
      <em v-if="creator">By {{ creator.firstName }} {{ creator.lastName }}</em>
      <br>
      {{ content.description }}
    </v-card-text>
    <v-card-actions>
      <div class="v-flex">
        <v-chip v-for="keyword in content.keywords" :key="keyword.id" small color="primary" class="mx-1 my-1">
          {{ keyword.name }}
        </v-chip>
      </div>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  name: "MarketplaceCard",
  props: {
    content: {
      id: Number,
      title: String,
      description: String,
      creator: Object,
      created: String,
      keywords: Array,
    }
  },

  computed: {
    creator() {
      return this.content.creator;
    },
    location() {
      return this.creator?.location;
    },
  }
};
</script>


<style scoped>

.title {
  line-height: 1.25;
  word-break: normal;
}

</style>