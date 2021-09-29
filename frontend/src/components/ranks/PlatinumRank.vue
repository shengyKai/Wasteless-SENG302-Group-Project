<template>
  <v-tooltip top>
    <template #activator="{ on, attrs }">
      <img src="../../assets/platinum-rank-icon.png" v-bind="attrs" v-on="on" class="rank">
    </template>
    <span>Platinum Business Rank</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "PlatinumRank",
};
</script>

<style scoped>
.rank {
  height: 40px;
}
</style>