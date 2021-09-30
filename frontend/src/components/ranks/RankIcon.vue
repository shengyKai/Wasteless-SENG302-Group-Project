<template>
  <v-tooltip top>
    <template #activator="{ on, attrs }">
      <v-img v-if="rankName === 'silver'" :max-height="sizeHeight" :max-width="sizeWidth" class="ml-2 mb-5 d-inline-block rank" src="../../assets/silver-rank-icon.png" alt="silver star" v-bind="attrs" v-on="on"/>
      <v-img v-if="rankName === 'gold'" :max-height="sizeHeight" :max-width="sizeWidth" class="ml-2 mb-5 d-inline-block rank" src="../../assets/gold-rank-icon.png" alt="gold star" v-bind="attrs" v-on="on"/>
      <v-img v-if="rankName === 'platinum'" :max-height="sizeHeight" :max-width="sizeWidth" class="ml-2 mb-5 d-inline-block rank" src="../../assets/platinum-rank-icon.png" alt="platinum star" v-bind="attrs" v-on="on"/>
    </template>
    <span>Business Rank</span>
  </v-tooltip>
</template>

<script>
export default {
  name: "RankIcon",
  props: {
    rankName: String,
    size: {
      validator: (s) => ['small', 'medium'].includes(s),
      default: 'medium'
    }
  },
  computed: {
    sizeHeight() {
      switch (this.size) {
      case "small":
        return 30;
      case "medium":
        return 50;
      default:
        throw new Error(`Invalid size "${this.size}"`);
      }
    },
    sizeWidth() {
      switch (this.size) {
      case "small":
        return 20;
      case "medium":
        return 40;
      default:
        throw new Error(`Invalid size "${this.size}"`);
      }
    }
  }
};
</script>

<style scoped>
.rank {
  position: fixed;
}
</style>