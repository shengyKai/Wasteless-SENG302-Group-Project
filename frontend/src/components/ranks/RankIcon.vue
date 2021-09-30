<template>
  <v-tooltip top>
    <template #activator="{ on, attrs }">
      <v-img v-if="rankName === 'silver'" max-height="40" max-width="30" class="ml-2 mb-5 d-inline-block" src="../../assets/silver-rank-icon.png" alt="silver star" v-bind="attrs" v-on="on"/>
      <v-img v-if="rankName === 'gold'" max-height="40" max-width="30" class="ml-2 mb-5 d-inline-block" src="../../assets/gold-rank-icon.png" alt="gold star" v-bind="attrs" v-on="on"/>
      <v-img v-if="rankName === 'platinum'" max-height="40" max-width="30" class="ml-2 mb-5 d-inline-block" src="../../assets/platinum-rank-icon.png" alt="platinum star" v-bind="attrs" v-on="on"/>
    </template>
    <span>Business Rank</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "RankIcon",
  props: {
    rankName: String,
  },
};
</script>