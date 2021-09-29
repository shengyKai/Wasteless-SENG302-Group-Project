<template>
  <v-tooltip top>
    <template #activator="{ on, attrs }">
      <img src="../../assets/gold-rank-icon.png" alt="gold star" v-bind="attrs" v-on="on" class="rank">
    </template>
    <span>Gold Business Rank</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "GoldRank",
};
</script>

<style scoped>
.rank {
  height: 40px;
}
</style>