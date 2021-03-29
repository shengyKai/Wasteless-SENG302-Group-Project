<template>
  <v-avatar
    :size="sizeInPixels"
    color="primary"
    class="white--text headline"
    :style="`font-size: ${fontSize}px !important`"
  >
    {{ user.firstName[0].toUpperCase() }}{{ user.lastName[0].toUpperCase() }}
    <template v-if="false">
      <!--In future when we have image support for users we will put it in here-->
      <img class="profile-image" src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg">
    </template>
  </v-avatar>
</template>

<script>

export default{
  name: 'UserAvatar',
  props: {
    user: {},
    size: {
      validator: (s) => ['small', 'medium', 'large'].includes(s),
      default: 'medium',
    },
  },

  computed: {
    sizeInPixels() {
      switch (this.size) {
      case "small":
        return 30;
      case "medium":
        return 40;
      case "large":
        return 200;
      default:
        return 0.0;
      }
    },
    fontSize() {
      return this.sizeInPixels * 0.5;
    }
  }
};
</script>

<style scoped>
.profile-image {
  margin: auto;
  position: absolute;
  top: 0; left: 0; bottom: 0; right: 0;
}
</style>