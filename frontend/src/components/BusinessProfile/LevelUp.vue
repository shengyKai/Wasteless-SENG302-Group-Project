<template>
  <div>
    <v-progress-linear class="maxXP" height="25" :value="progress">
      <strong>
        <span>{{points}} </span>
        <span v-if="threshold">/ {{threshold}}</span>
      </strong>
    </v-progress-linear>
  </div>
</template>

<script>
export default {
  name: "LevelUp",
  props: {
    business: {
      type: Object,
      required: true,
    }
  },
  computed: {
    points() {
      return this.business.points;
    },
    threshold() {
      return this.business.rank.threshold;
    },
    progress() {
      if (this.threshold === null) return 100;
      return this.points/this.threshold*100;
    }
  }
};
</script>

<style scoped>
.maxXP {
  width: 500px;
  color: darkgreen;
  background-color: lightgrey;
}
</style>