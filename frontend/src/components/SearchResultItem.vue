<template>
  <v-list-item>
    <v-list-item-avatar>
      <UserAvatar :user="user" size="small" />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>
        <router-link :to="'profile/' + user.id" class="link">
          {{ user.firstName }} {{ user.lastName }}
        </router-link>
      </v-list-item-title>
      <v-list-item-subtitle> {{ user.email }} </v-list-item-subtitle>
      <v-list-item-subtitle> {{ user.homeAddress }} </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>

<script>
import UserAvatar from './utils/UserAvatar';

export default {
  props: ['user'],

  components: {
    UserAvatar,
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