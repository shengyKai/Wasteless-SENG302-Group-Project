<template>
  <v-list-item>
    <v-list-item-avatar>
      <Avatar :user="user" size="medium" />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>
        <router-link :to="'profile/' + user.id" class="link">
          {{ user.firstName }} {{ user.lastName }}
        </router-link>
      </v-list-item-title>
      <v-list-item-subtitle> {{ user.email }} </v-list-item-subtitle>
      <v-list-item-subtitle> {{ insertAddress(user.homeAddress) }} </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>

<script>
import Avatar from '../utils/Avatar';
import convertAddressToReadableText from '../utils/Methods/convertAddressToReadableText';

export default {
  props: ['user'],
  components: {
    Avatar
  },
  methods: {
    //take a look at UserProfile.vue to know more about this
    insertAddress(address) {
      return convertAddressToReadableText(address, "partial");
    }
  }
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