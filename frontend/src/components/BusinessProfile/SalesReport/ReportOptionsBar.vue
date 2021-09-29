<template>
  <v-card color="secondary" dark>
    <v-card-actions>
      <v-row align="center" justify="center">
        <v-col cols="12" md="2" sm="12" class="mt-1">
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
              :max="maxFromDate"
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
        <v-col cols="12" md="2" sm="12">
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
              :max="maxToDate"
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
        <v-col cols="12" md="1" sm="12">
          <h4 class="font-weight-bold text-center">
            OR
          </h4>
        </v-col>
        <v-col cols="12" md="3" sm="12">
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
        <v-divider vertical dark/>
        <v-col cols="12" md="2" sm="12">
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
        <v-col cols="12" md="1" sm="12" class="text-center">
          <v-btn
            color="primary"
            @click="getFullReport"
          >
            Generate
          </v-btn>
        </v-col>
      </v-row>
    </v-card-actions>
  </v-card>
</template>

<script>
import { generateReport } from "@/api/salesReport.ts";

export default {
  name: "ReportGenerationBar",
  data() {
    return {
      fromDate: null,
      toDate: null,
      fromDateMenu: false,
      toDateMenu: false,
      periodBefore: null,
      granularity: "year",
      periodBeforeOptions: [
        { periodLevel:'One day before', periodValue:'day' },
        { periodLevel:'One week before', periodValue:'week' },
        { periodLevel:'One month before', periodValue:'month' },
        { periodLevel:'One year before', periodValue:'year' }
      ],
      granularityOptions: [
        { granularityLevel:'Day', granularityValue:'daily' },
        { granularityLevel:'Week', granularityValue:'weekly' },
        { granularityLevel:'Month', granularityValue:'monthly' },
        { granularityLevel:'Year', granularityValue:'yearly' }
      ]
    };
  },
  computed: {
    /**
     * Max date of the fromDate has to be the same as the toDate or the present day
     */
    maxFromDate() {
      if (this.toDate !== null) {
        return this.toDate;
      } else {
        return new Date().toISOString().slice(0, 10);
      }
    },
    /**
     * Max date of the toDate is the present day
     */
    maxToDate() {
      return new Date().toISOString().slice(0, 10);
    }
  },
  methods: {
    /**
     * Method to call the api endpoint, generateReport, and breaks down the report dates into the appropriate fields.
     * Finally emits the data to the SalesReportPage
     */
    async getFullReport() {
      /** TODO replace the business id below with the correct id */
      let reportData = await generateReport(1, this.fromDate, this.toDate, this.granularity);
      this.breakDownDate(reportData);
      this.$emit("retrievedData", {reportData: reportData, reportType: this.granularity});
    },
    /**
     * Breakdown the dates retrieved from the backend so that it can be presented in the table later. This is done here
     * because we are not able to mutate the reportData as a prop later on in SalesReportTable.
     */
    breakDownDate(reportData) {
      for (let row in reportData) {
        reportData[row]["day"] = new Date(reportData[row]["endDate"]).getDate();
        reportData[row]["week"] = this.getWeekNo(new Date(reportData[row]["endDate"]));
        reportData[row]["month"] = new Date(reportData[row]["endDate"]).toLocaleString('default', {month: 'long'});
        reportData[row]["year"] = new Date(reportData[row]["endDate"]).getFullYear();
      }
    },
    /**
     * Gets the week number from a given date object.
     */
    getWeekNo(date) {
      const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
      const pastDaysOfYear = (date - firstDayOfYear) / 86400000;
      return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
    }
  },
  watch: {
    /**
     * If the fromDate value is null and a value is being set for the toDate, the fromDate value is also set to the toDate value.
     * This is to ensure both fields are filled in at all times.
     * If toDate is not null, periodBefore must be null as both options cannot be used at the same time.
     * If the toDate date is before the fromDate date, change the fromDate to that value
     */
    toDate(value) {
      if (this.fromDate === null) {
        this.fromDate = value;
      }
      if (value !== null) {
        this.periodBefore = null;
      }
      if (new Date(value) < new Date(this.fromDate)) {
        this.fromDate = value;
      }
    },
    /**
     * If the toDate value is null and a value is being set for the fromDate, the toDate value is also set to the fromDate value.
     * This is to ensure both fields are filled in at all times.
     * If fromDate is not null, periodBefore must be null as both options cannot be used at the same time.
     * If the fromDate date is after the toDate date, change the toDate to that value
     * Technically this situation should not have a chance to occur, but acts as a sanity check
     */
    fromDate(value) {
      if (this.toDate === null) {
        this.toDate = value;
      }
      if (value !== null) {
        this.periodBefore = null;
      }
      if (new Date(value) > new Date(this.toDate)) {
        this.toDate = value;
      }
    },
    /**
     * This watches the periodBefore variable so that if the period is not null, the toDate and fromDate
     * must be null, as both options cannot be used at the same time.
     */
    periodBefore(value) {
      if (value !== null) {
        this.toDate = null;
        this.fromDate = null;
      }
    }
  }
};
</script>

<style scoped>
/*
  The bottom two css styles had to be done because by Vueitfy's default implementation, there are sometimes some components
  which are created which causes the layout to be unorganised. The >>> symbol is a deep selector symbol which targets the specific
  Vue/Vueitfy class when you provide a parent class. Try uncommenting the bottom two styles to see the difference
 */
.v-input >>> .v-text-field__details{
  margin-bottom: 0;
  display: none;
}
.v-input >>> .v-input__slot{
  margin-bottom: 0;
}
</style>