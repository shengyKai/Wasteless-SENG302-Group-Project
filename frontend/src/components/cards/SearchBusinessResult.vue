<template>
  <v-list-item>
    <v-row>
      <v-col cols="auto" class="pl-0" align-self="center">
        <Avatar :business="business" size="medium"/>
      </v-col>
      <v-col class="pl-0">
        <v-list-item-content>
          <v-list-item-title>

            <a class="link" @click="viewBusinessProfile">
              {{ business.name }}
            </a>
            <v-avatar
              :size="30"
              :color="badgeColour"
              class="headline">
              <v-icon small color="primary darken-4">
                mdi-vuetify
              </v-icon>
            </v-avatar>
          </v-list-item-title>
          <v-list-item-subtitle>{{ insertAddress(business.address) }}</v-list-item-subtitle>
          <v-list-item-subtitle>{{ business.businessType }}</v-list-item-subtitle>
          <v-list-item-subtitle><strong>Rank: </strong> {{business.rank.name}} ({{business.points}} points) </v-list-item-subtitle>
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
  computed: {
    /**
     * Changes the badge colour based on business rank
     */
    badgeColour() {
      if (this.business.rank.name === 'bronze') {
        return "brown lighten-2";
      }
      if (this.business.rank.name === 'silver') {
        return "grey";
      }
      if (this.business.rank.name === 'gold') {
        return "yellow darken-1";
      }
      else {
        return "secondary";
      }
    },
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
</style>