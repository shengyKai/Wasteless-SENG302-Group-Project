<template>
  <v-app-bar max-height="64px">
    <div class="container-outer">
      <div class="container-inner">
        <h1>WASTELESS</h1>
      </div>
    </div>

    <!-- Search Bar component to perform search and show result -->
    <SearchBar v-if="$route.path !== '/search'" />
    <div class="text-center">
      <v-menu offset-y>
        <template v-slot:activator="{ on, attrs }">
          <v-btn icon v-bind="attrs" v-on="on">
            <v-avatar>
              <v-icon>
                mdi-account-circle
              </v-icon>
            </v-avatar>
          </v-btn>
        </template>
        <v-list>
          <v-list-item>
            <v-list-item-title class="link" @click="viewProfile">
              Profile
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="link" @click="logout">
              Logout
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="link" @click="viewAdmin">
              Test-Admin
            </v-list-item-title>
          </v-list-item>
          <v-list-item>

            <!-- Button to Create/Own Business, will pop up dialog (business register form) when clicked.

            #TODO, refactorize this chunk of code into utils
            -->
            <v-row justify="center">
              <v-dialog v-model="dialog" persistent max-width="600px">
                <template v-slot:activator="{ on, attrs }">
                  <v-btn color="primary" dark v-bind="attrs" v-on="on">
                    Create Now
                  </v-btn>
                </template>
                <v-card>
                  <v-card-title>
                    <span class="headline">Create Business</span>
                  </v-card-title>
                  <v-card-text>
                    <v-container>
                      <v-row>
                        <v-col>
                          <v-text-field
                            label="Name of business*"
                            hint="example of persistent helper (7char?)"
                            persistent-hint
                            required
                            outlined
                          />
                        </v-col>
                        <v-col cols="12">
                          <v-text-field
                            label="Description*"
                            hint="example of helper text only on focus (100char)"
                            required
                            outlined
                          />
                        </v-col>
                        <v-col cols="12">
                          <v-autocomplete
                            :items="[
                              'Accommodation and Food Services',
                              'Retail Trade',
                              'Charitable organization',
                              'Non-profit organization',
                            ]"
                            label="Business Type"
                            multiple
                            outlined
                          />
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <v-text-field
                            label="Street Address*"
                            required
                            outlined
                          />
                        </v-col>
                        <v-col cols="12">
                          <v-text-field
                            label="Apartment, Suite, Unit, Building, Floor*"
                            required
                            outlined
                          />
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <v-text-field
                            label="Distinct / City Area*"
                            required
                            outlined
                          />
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <v-text-field label="City*" required outlined />
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <v-text-field
                            label="State / Province / Region*"
                            required
                            outlined
                          />
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <v-text-field label="Country*" required outlined />
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <v-text-field label="Postcode*" required outlined />
                        </v-col>
                      </v-row>
                    </v-container>
                    <small class="red--text">All field is required</small>
                  </v-card-text>
                  <v-card-actions>
                    <v-spacer />
                    <v-btn color="blue darken-1" text @click="dialog = false">
                      Close
                    </v-btn>
                    <v-btn color="blue darken-1" text @click="dialog = false">
                      Save
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-row>
          </v-list-item>
        </v-list>
      </v-menu>
    </div>
  </v-app-bar>
</template>

<script>
import SearchBar from "./utils/SearchBar";

export default {
  name: "AppBar",
  components: {
    SearchBar,
  },
  methods: {
    viewProfile() {
      this.$router.push("/profile");
    },

    logout() {
      this.$store.commit("logoutUser");
      this.$router.push("/login");
    },
    viewAdmin() {
      this.$router.push("/admin");
    },
    viewCheckBusiness() {
      this.$router.push("/create_business");
    },
  },
  data: () => ({
    dialog: false,
  }),
};
</script>
