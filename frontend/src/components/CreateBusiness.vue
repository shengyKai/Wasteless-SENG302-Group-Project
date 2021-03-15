<template>
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
                    'Skiing',
                    'Ice hockey',
                    'Soccer',
                    'Basketball',
                    'Hockey',
                    'Reading',
                    'Writing',
                    'Coding',
                    'Basejump',
                  ]"
                  label="Business Type"
                  multiple
                  outlined
                />
              </v-col>
              <v-col cols="12" sm="6" md="6">
                <v-text-field label="Street Address*" required outlined />
              </v-col>
              <v-col cols="12">
                <v-text-field
                  label="Apartment, Suite, Unit, Building, Floor*"
                  required
                  outlined
                />
              </v-col>
              <v-col cols="12" sm="6" md="6">
                <v-text-field label="Distinct / City Area*" required outlined />
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
</template>

<script>
export default {
  data: () => ({
    dialog: false,
  }),
};
</script>
