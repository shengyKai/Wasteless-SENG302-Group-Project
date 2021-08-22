<template>
  <v-list-item>
    <v-row>
      <v-col cols="auto" class="px-0">
        <v-list-item-avatar size="50">
          <v-img :src="image" height="100%"/>
        </v-list-item-avatar>
      </v-col>
      <v-col class="pl-0">
        <v-list-item-content>
          <v-list-item-title>

            <a style="color: black;" @click="viewBusinessProfile">
              {{ business.name }}
            </a>

          </v-list-item-title>
          <v-list-item-subtitle> {{ business.businessType }} </v-list-item-subtitle>
          <v-list-item-subtitle> {{ insertAddress(business.address) }} </v-list-item-subtitle>
        </v-list-item-content>
      </v-col>
    </v-row>
  </v-list-item>
</template>

<script>
import convertAddressToReadableText from '../utils/Methods/convertAddressToReadableText';
import {imageSrcFromFilename} from "@/utils";

export default {
  props: {
    business: Object,
  },
  methods: {
    insertAddress(address) {
      return convertAddressToReadableText(address, "partial");
    },
    viewBusinessProfile() {
      this.$emit("view-profile");
    },
  },
  computed: {
    image() {
      let image = this.business.images[0];
      if (image === undefined) return undefined;

      let url = image.thumbnailFilename;
      if (url === undefined) {
        url = image.filename;
      }
      return imageSrcFromFilename(url);
    },
  },
};
</script>

<style scoped>
.link {
    color: black;
    text-decoration: none;
}

.link:hover {
    text-decoration: underline;
}

</style>