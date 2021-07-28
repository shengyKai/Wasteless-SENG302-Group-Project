<template>
  <v-list-item>
    <v-list-item-content>
      <v-list-item-title>
        <a style="color: black;" @click="viewBusinessProfile">
          {{ business.name }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle> {{ business.businessType }} </v-list-item-subtitle>
      <v-list-item-subtitle> {{ insertAddress(business.address) }} </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>

<script>
import convertAddressToReadableText from '../utils/Methods/convertJsonAddressToReadableText';

export default {
  props: {
    business: Object,
  },
  methods: {
    insertAddress(address) {
      return convertAddressToReadableText(address, "partial");
    },
    viewBusinessProfile() {
      this.$router.push("/business/" + this.business.id);
    }
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