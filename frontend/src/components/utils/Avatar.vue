<template>
  <v-avatar
    :size="sizeInPixels"
    :color="user !== undefined ? 'primary' : 'secondary'"
    class="white--text headline"
    :style="`font-size: ${fontSize}px !important`"
  >
    <template v-if="image === undefined">{{ initials }}</template>
    <v-img v-else :src="image" height="100%"/>
  </v-avatar>
</template>

<script>
import { imageSrcFromFilename } from '@/utils';

export default {
  name: 'Avatar',
  props: {
    /**
     * User to make avatar for.
     * Could be aquired from the 'getUser' function in api.ts
     */
    user: Object,

    /**
     * Business to make avatar for.
     */
    business: Object,

    /**
     * Product to make avatar for.
     */
    product: Object,

    /**
     * The size of the avatar
     * The small and medium sizes use the thumbnail sized avatar image, large uses full size.
     * @values small, medium, large
     */
    size: {
      validator: (s) => ['small', 'medium', 'medium-large', 'large'].includes(s),
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
      case 'medium-large':
        return 80;
      case "large":
        return 200;
      default:
        throw new Error(`Invalid size "${this.size}"`);
      }
    },
    fontSize() {
      return this.sizeInPixels * 0.5;
    },
    initials() {
      if (this.user !== undefined) {
        return this.user.firstName[0].toUpperCase() + this.user.lastName[0].toUpperCase();
      }
      let target = this.business;
      if (target === undefined) target = this.product;
      if (target === undefined) throw new Error('Either "user", "business" or "product" must be provided.');

      let pieces = target.name.split(/\s/);
      if (pieces.length === 2) {
        return pieces[0][0].toUpperCase() + pieces[1][0].toUpperCase();
      }
      return target.name[0].toUpperCase();
    },
    image() {
      let image;
      if (this.user !== undefined) {
        image = undefined; // User images not implemented
      } else if (this.business !== undefined) {
        image = this.business.images[0];
      } else if (this.product !== undefined) {
        image = this.product.images[0];
      } else {
        throw new Error('Either "user", "business" or "product" must be provided.');
      }

      if (image === undefined) return undefined;

      let url = image.thumbnailFilename;
      if (url === undefined || url === null) {
        url = image.filename;
      }
      return imageSrcFromFilename(url);
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