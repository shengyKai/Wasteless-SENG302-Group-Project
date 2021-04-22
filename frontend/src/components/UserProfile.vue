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

      <!-- List of available actions -->
      <div class="action-menu">
        <v-dialog
          v-model="removeAdminDialog"
          persistent
          max-width="300"
        >
          <template #activator="{ on: dialog, attrs}">
            <v-tooltip bottom v-if="isActingAsBusiness && isViewingOwnProfile===false">
              <template #activator="{ on: tooltip}">
                <v-btn
                  icon
                  color="primary"
                  v-bind="attrs"
                  v-on="{...tooltip, ...dialog}"
                  :disabled="isUserAdminOfActiveBusiness === false"
                  ref="removeAdminButton"
                >
                  <v-icon>mdi-account-minus</v-icon>
                </v-btn>
              </template>
              <span> Remove administrator </span>
            </v-tooltip>
          </template>
          <v-card>
            <v-card-title class="headline">
              Are you sure?
            </v-card-title>
            <v-card-text>This user will no longer be able to operate this business as an administrator.</v-card-text>
            <v-card-actions>
              <v-spacer/>
              <v-btn
                color="green darken-1"
                text
                @click="removeAdminDialog = false"
              >
                Disagree
              </v-btn>
              <v-btn
                color="green darken-1"
                text
                @click="removeAdminDialog = false; removeUserAdmin()"
              >
                Agree
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog
          v-model="addAdminDialog"
          persistent
          max-width="300"
        >
          <template #activator="{ on: dialog, attrs}">
            <v-tooltip bottom v-if="isActingAsBusiness && isViewingOwnProfile===false">
              <template #activator="{ on: tooltip }">
                <v-btn
                  icon
                  color="primary"
                  v-bind="attrs"
                  v-on="{...dialog, ...tooltip}"
                  :disabled="isUserAdminOfActiveBusiness === true"
                  ref="addAdminButton"
                >
                  <v-icon>mdi-account-plus</v-icon>
                </v-btn>
              </template>
              <span> Add administrator </span>
            </v-tooltip>
          </template>
          <v-card>
            <v-card-title class="headline">
              Are you sure?
            </v-card-title>
            <v-card-text>This user will be able to act as an administrator for this business</v-card-text>
            <v-card-actions>
              <v-spacer/>
              <v-btn
                color="green darken-1"
                text
                @click="addAdminDialog = false"
              >
                Disagree
              </v-btn>
              <v-btn
                color="green darken-1"
                text
                @click="addAdminDialog = false; addUserAsAdmin()"
              >
                Agree
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
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
            <router-link :to="'/business/' + business.id">
              <v-chip color="primary" class="link-chip link"> {{ business.name }} </v-chip>
            </router-link>
          </span>
        </v-col>
      </v-row>

    </v-container>
  </v-card>
</template>

<script>
import { getUser, makeBusinessAdmin, removeBusinessAdmin } from '../api';
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
      removeAdminDialog: false,
      addAdminDialog: false,
    };
  },

  created() {
    if (this.$route.params.id === undefined) {
      this.user = this.$store.state.user;
      return;
    }

    const id = parseInt(this.$route.params.id);
    if (isNaN(id)) return;

    if (id === this.$store.state.user?.id) {
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
      // Temporarily adds the business to the list of administered businesses.
      this.user.businessesAdministered.push({ id: role.id });

      response = await getUser(this.user.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      // Updates the user properly
      this.user = response;
      if (this.user.id === this.$store.state.user?.id) {
        this.$store.commit('setUser', this.user);
      }
    },
    async removeUserAdmin() {
      const role = this.activeRole;
      if (!this.user || role?.type !== 'business') return;
      let response = await removeBusinessAdmin(role.id, this.user.id);

      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      this.user.businessesAdministered.filter(business => business.id !== role.id);

      response = await getUser(this.user.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      // Updates the user properly
      this.user = response;
      if (this.user.id === this.$store.state.user?.id) {
        this.$store.commit('setUser', this.user);
      }
    }
  },

  computed: {
    activeRole() {
      return this.$store.state.activeRole;
    },
    isActingAsBusiness() {
      return this.activeRole?.type === 'business';
    },
    isUserAdminOfActiveBusiness() {
      if (!this.isActingAsBusiness) return undefined;
      if (this.user === undefined) return undefined;

      return this.user.businessesAdministered.map(business => business.id).includes(this.activeRole.id);
    },
    isViewingOwnProfile() {
      const id = parseInt(this.$route.params.id);
      if (isNaN(id)) return undefined;
      return (this.user === this.$store.state.user);
    },
    createdMsg() {
      if (this.user.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.user.created);
      const parts = createdAt.toDateString().split(' ');

      const diffTime = now - createdAt;
      const diffMonths = Math.ceil(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    },
    businesses() {
      return this.user?.businessesAdministered;
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
