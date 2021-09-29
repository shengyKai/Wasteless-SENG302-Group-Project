<template>
  <div>
    <div class="ml-2">
      <div class="float-left">{{points}}</div>
      <div v-if="threshold" class="float-left">/ {{threshold}}</div>
    </div>
    <div class="maxXP">
      <div v-bind:style="currentXP"/>
    </div>
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
      return this.points/this.threshold*600;
    },
    currentXP() {
      return {
        height: '20px',
        width: this.progress + 'px',
        backgroundColor: 'greenyellow',
      };
    }
  }
};
</script>

<style scoped>
.maxXP {
  height: 20px;
  width: 600px;
  background-color: darkgrey;
}
</style>