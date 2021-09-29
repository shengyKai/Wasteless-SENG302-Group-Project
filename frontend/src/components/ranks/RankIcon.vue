<template>
  <v-tooltip top>
    <template #activator="{ on, attrs }">
      <v-img max-height="40" max-width="30" class="ml-2 mb-5 d-inline-block" src="../../assets/gold-rank-icon.png" v-bind="attrs" v-on="on"/>
    </template>
    <span>Business Rank</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "RankIcon",
};
</script>