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

      <!-- List of avaialable actions -->
      <div class="action-menu">
        <v-tooltip bottom v-if="isActingAsBusiness">
          <template v-slot:activator="{ on, attrs }">
            <v-btn
              icon
              color="primary"
              v-bind="attrs"
              v-on="on"
              @click="addUserAsAdmin"
              :disabled="isUserAdminOfActiveBusiness === true"
              ref="addAdminButton"
            >
              <v-icon>mdi-account-plus</v-icon>
            </v-btn>
          </template>
          <span> Add adminstrator </span>
        </v-tooltip>
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
          {{ user.dateOfBirth }}
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
import { getBusiness, getUser, makeBusinessAdmin } from '../api';
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

      /**
       * Whether this profile's user is an administrator for the current active business.
       * This variable is undefined if this is not known.
       */
      isUserAdminOfActiveBusiness: undefined,
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

  methods: {
    async addUserAsAdmin() {
      const role = this.activeRole;
      if (!this.user || role?.type !== 'business') return;
      let response = await makeBusinessAdmin(role.id, this.user.id);

      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      this.isUserAdminOfActiveBusiness = true;
    }
  },

  computed: {
    activeRole() {
      return this.$store.state.activeRole;
    },
    isActingAsBusiness() {
      return this.activeRole?.type === 'business';
    },
    createdMsg() {
      if (this.user.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.user.created);
      const parts = createdAt.toDateString().split(' ');

      const diffTime = now - createdAt;
      const diffMonths = Math.ceil(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    }
  },
  watch: {
    async user() {
      this.businesses = [];
      const admins = this.user.businessesAdministered;

      if (!admins) return;

      const promises = admins.map(id => getBusiness(id));
      this.businesses = await Promise.all(promises);
    },

    activeRole: {
      async handler() {
        this.isUserAdminOfActiveBusiness = undefined;
        if (this.activeRole?.type !== 'business') {
          return;
        }

        const business = await getBusiness(this.activeRole.id);
        if (typeof business === 'string') {
          this.$store.commit('setError', business);
          return;
        }

        if (business.administrators === undefined) return;

        this.isUserAdminOfActiveBusiness = business.administrators.map(user => user.id).includes(this.user.id);
      },
      immediate: true,
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

.action-menu {
  display: flex;
  flex: 1;
  justify-content: flex-end;
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
