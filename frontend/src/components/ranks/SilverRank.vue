<template>
  <v-tooltip top>
    <template #activator="{ on, attrs }">
      <img src="../../assets/silver-rank-icon.png" v-bind="attrs" v-on="on" class="rank">
    </template>
    <span>Silver Business Rank</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "SilverRank",
};
</script>

<style scoped>
.rank {
  height: 40px;
}
</style>