<template>
  <v-list-item>
    <v-row>
      <v-col cols="auto" class="pl-0" align-self="center">
        <Avatar :business="business" size="medium"/>
      </v-col>
      <v-col class="pl-0">
        <v-list-item-content>
          <v-list-item-title>

            <a class="mr-10" style="color: black;" @click="viewBusinessProfile">
              {{ business.name }}
            </a>

          </v-list-item-title>
          <v-list-item-subtitle>
            <label class="mr-10"><strong>Rank: </strong>Bronze (47 points)</label>
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            <label class="mr-10">{{ insertAddress(business.address) }}</label>
            <label class="mr-10">{{ business.businessType }}</label>
          </v-list-item-subtitle>

        </v-list-item-content>
      </v-col>
    </v-row>
  </v-list-item>
</template>

<script>
import Avatar from '../utils/Avatar.vue';
import convertAddressToReadableText from '../utils/Methods/convertAddressToReadableText';

export default {
  props: {
    business: Object,
  },
  components: {
    Avatar,
  },
  methods: {
    insertAddress(address) {
      return convertAddressToReadableText(address, "partial");
    },
    viewBusinessProfile() {
      this.$emit("view-profile");
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

.result-column {
  min-width: 500px;
}
</style>