<template>
  <v-card class="body" v-if="user">
    <div class="top-section">
      <div class="profile-img">
        <UserAvatar :user="user" size="large" />
      </div>

      <div>
        <h1>
          {{ user.firstName }} {{ user.lastName }}
        </h1>
        <h2>
          <i>{{ user.nickname }}</i>
        </h2>
        <p><b>Member Since:</b> {{ createdMsg }}</p>
      </div>
    </div>

    <v-container fluid>
      <v-row>
        <v-col cols="12" sm="6">
          <h4>Email</h4>
          {{ user.email }}
        </v-col>
        <v-col cols="12" sm="6">
          <h4>Date of Birth</h4>
          {{ dateOfBirth }}
        </v-col>

        <v-col cols="12" sm="6">
          <h4>Phone Number</h4>
          {{ user.phoneNumber }}
        </v-col>
        <v-col cols="12" sm="6">
          <h4>Home Address</h4>
          {{ user.homeAddress }}
        </v-col>

        <v-col cols="12">
          <h4>Bio</h4>
          {{ user.bio }}
        </v-col>
        <v-col cols="12">
          <h4>Businesses</h4>
          <span v-for="business in businesses" :key="business.id">
            <template v-if="typeof business === 'string'">
              <v-chip color="error" class="link-chip link"> {{ business }} </v-chip>
            </template>
            <template v-else>
              <router-link :to="'/business/' + business.id">
                <v-chip color="primary" class="link-chip link"> {{ business.name }} </v-chip>
              </router-link>
            </template>
          </span>
        </v-col>
      </v-row>

    </v-container>
  </v-card>
</template>

<script>
import { getBusiness, getUser } from '../api';
import UserAvatar from './utils/UserAvatar';

export default {
  name: 'ProfilePage',

  data() {
    return {
      /**
       * The user that this profile is for.
       * If null then no profile is displayed
       */
      user: null,
      /**
       * The businesses that this user administers.
       * Also contains strings that are error messages for businesses that failed to be retreived.
       */
      businesses: [],
    };
  },

  created() {
    if (this.$route.params.id === undefined) {
      this.user = this.$store.state.user;
      return;
    }

    const id = parseInt(this.$route.params.id);
    if (isNaN(id)) return;

    if (id !== this.$store.state.user?.id) {
      this.user = this.$store.state.user;
    } else {
      getUser(id).then((value) => {
        if (typeof value === 'string') {
          this.$store.commit('setError', value);
        } else {
          this.user = value;
        }
      });
    }
  },

  computed: {
    createdMsg() {
      if (this.user.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.user.created);
      const parts = createdAt.toDateString().split(' ');

      const diffTime = now - createdAt;
      const diffMonths = Math.ceil(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    },
    /**
     * Construct a representation of the user's date of birth to display on the profile
     */
    dateOfBirth() {
      if (this.user.dateOfBirth === undefined) return '';

      const dateOfBirth = new Date(this.user.dateOfBirth);
      const parts = dateOfBirth.toDateString().split(' ');
      return `${parts[2]} ${parts[1]} ${parts[3]}`;
    }
  },
  watch: {
    async user() {
      this.businesses = [];
      const admins = this.user.businessesAdministered;

      if (!admins) return;

      const promises = admins.map(id => getBusiness(id));
      this.businesses = await Promise.all(promises);
    }
  },
  components: {
    UserAvatar,
  },
};
</script>

<style scoped>
.profile-img {
  margin-top: -116px;
  margin-right: 16px;
}

.body {
  padding: 16px;
  width: 100%;
  margin-top: 140px;
  /* text-align: center; */
}

.top-section {
  display: flex;
  flex-wrap: wrap;
  /* justify-content: center; */
}

.link-chip {
  margin-right: 4px;
}
</style>
