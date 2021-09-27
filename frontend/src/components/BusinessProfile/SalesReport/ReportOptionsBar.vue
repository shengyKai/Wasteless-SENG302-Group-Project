<template>
  <v-card color="secondary" dark>
    <v-card-actions>
      <v-row align="center" justify="center">
        <v-col cols="2">
          <v-menu
            ref="fromMenu"
            v-model="fromDateMenu"
            :return-value.sync="fromDate"
            :close-on-content-click="false"
            persistent
            offset-y
            min-width="auto"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                v-model="fromDate"
                label="From"
                prepend-inner-icon="mdi-calendar"
                readonly
                v-bind="attrs"
                v-on="on"
                outlined
              />
            </template>
            <v-date-picker
              v-model="fromDate"
              scrollable
            >
              <v-spacer/>
              <v-btn
                text
                color="primary"
                @click="fromDateMenu = false"
              >
                Cancel
              </v-btn>
              <v-btn
                text
                color="primary"
                @click="$refs.fromMenu.save(fromDate)"
              >
                OK
              </v-btn>
            </v-date-picker>
          </v-menu>
        </v-col>
        <v-col cols="2">
          <v-menu
            class="mb-0 pb-0"
            ref="toMenu"
            v-model="toDateMenu"
            :return-value.sync="toDate"
            :close-on-content-click="false"
            persistent
            offset-y
            min-width="auto"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                v-model="toDate"
                label="To"
                prepend-inner-icon="mdi-calendar"
                readonly
                v-bind="attrs"
                v-on="on"
                outlined
              />
            </template>
            <v-date-picker
              v-model="toDate"
              scrollable
            >
              <v-spacer/>
              <v-btn
                text
                color="primary"
                @click="toDateMenu = false"
              >
                Cancel
              </v-btn>
              <v-btn
                text
                color="primary"
                @click="$refs.toMenu.save(toDate)"
              >
                OK
              </v-btn>
            </v-date-picker>
          </v-menu>
        </v-col>
        <v-col cols="4">
          <v-select
            v-model="periodBefore"
            flat
            solo-inverted
            hide-details
            label="Preset periods"
            :items="periodBeforeOptions"
            item-text="periodLevel"
            item-value="periodValue"
            prepend-inner-icon="mdi-clock-time-four"
            color="secondary"
          />
        </v-col>
        <v-col cols="2">
          <v-select
            v-model="granularity"
            flat
            solo-inverted
            hide-details
            :items="granularityOptions"
            item-text="granularityLevel"
            item-value="granularityValue"
            color="secondary"
            label="Report Granularity"
          />
        </v-col>
        <v-col cols="auto">
          <v-btn
            color="primary">
            Generate
          </v-btn>
        </v-col>
      </v-row>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  name: "ReportGenerationBar",
  data() {
    return {
      periodBeforeOptions: [
        { periodLevel:'One day before', periodValue:'day' },
        { periodLevel:'One week before', periodValue:'week' },
        { periodLevel:'One month before', periodValue:'month' },
        { periodLevel:'One year before', periodValue:'year' }
      ],
      granularityOptions: [
        { granularityLevel:'Day', granularityValue:'day' },
        { granularityLevel:'Week', granularityValue:'week' },
        { granularityLevel:'Month', granularityValue:'month' },
        { granularityLevel:'Year', granularityValue:'year' }
      ]
    };
  }
};
</script>

<style scoped>
v-menu .v-text-field .v-text-field__details .v-messages{
  background: red;
}
</style>