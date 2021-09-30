<template>
  <v-card color="secondary" dark>
    <v-card-actions>
      <v-row align="center" justify="center">
        <v-col cols="12" md="2">
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
        <v-col cols="12" md="2">
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
              :min="minToDate"
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
        <v-col cols="12" md="3">
          <v-select
            v-model="presetPeriodUserString"
            :items="presetPeriods.map(period => period.level)"
            flat
            solo-inverted
            hide-details
            label="Preset periods"
            prepend-inner-icon="mdi-clock-time-four"
            color="secondary"
          />
        </v-col>
        <v-divider vertical dark/>
        <v-col cols="12" md="2">
          <v-select
            v-model="granularity"
            flat
            solo-inverted
            hide-details
            :items="granularityOptions"
            item-text="level"
            item-value="value"
            color="secondary"
            label="Report Granularity"
          />
        </v-col>
        <v-col cols="12" md="2" class="text-center">
          <v-btn
            color="primary"
            @click="sendReportSpecifications"
          >
            Generate
          </v-btn>
        </v-col>
      </v-row>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  name: "ReportOptionsBar",
  data() {
    return {
      fromDate: undefined,
      toDate: undefined,
      fromDateMenu: false,
      toDateMenu: false,
      presetPeriodUserString: undefined,
      granularity: "none",
      presetPeriods: [
        { level: 'Today',          value:'day' },
        { level: 'Previous Week',  value:'week' },
        { level: 'Previous Month', value:'month' },
        { level: 'Previous Year',  value:'year' }
      ],
      granularityOptions: [
        { level: 'Day',   value: 'daily'   },
        { level: 'Week',  value: 'weekly'  },
        { level: 'Month', value: 'monthly' },
        { level: 'Year',  value: 'yearly'  },
        { level: 'Whole', value: 'none'    },
      ]
    };
  },
  computed: {
    /**
     * Internal name of the period
     */
    presetPeriod() {
      for (let period of this.presetPeriods) {
        if (period.level === this.presetPeriodUserString) return period.value;
      }
      return undefined;
    },
    /**
     * Dates for the given period if one is selected, otherwise undefined
     */
    presetDates() {
      if (this.presetPeriod === undefined) return undefined;

      let now = new Date();
      let start = new Date(now);
      if (this.presetPeriod === 'day') {
        // End day is already correct
      } else if (this.presetPeriod === 'week') {
        start.setDate(start.getDate() - 6);
      } else if (this.presetPeriod === 'month') {
        start.setMonth(start.getMonth() - 1);
      } else if (this.presetPeriod === 'year') {
        start.setFullYear(start.getFullYear() - 1);
      }
      return {
        fromDate: start.toISOString().slice(0, 10),
        toDate: now.toISOString().slice(0, 10)
      };
    },
    /**
     * Max date of the fromDate has to be the same as the toDate or the present day
     */
    maxFromDate() {
      if (this.toDate !== undefined) {
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
    },
    /**
     * Min date of the toDate is the fromDate
     */
    minToDate() {
      return this.fromDate;
    },
  },
  methods: {
    /**
     * Method to send the report generation specifications to the parent, SalesReportPage
     */
    async sendReportSpecifications() {
      this.$emit("sendRequestParams", {fromDate: this.fromDate, toDate: this.toDate, granularity: this.granularity});
    },
    /**
     * Clear the current preset display
     */
    clearPresetPeriod() {
      this.presetPeriodUserString = undefined;
    },
  },
  watch: {
    /**
     * When the presetDates change, then update the from/to dates accordingly
     */
    presetDates: {
      handler() {
        if (this.presetDates === undefined) return;

        this.fromDate = this.presetDates.fromDate;
        this.toDate = this.presetDates.toDate;
      },
      deep: true,
    },
    /**
     * When the fromDate changes check if it is different from the presetDates
     */
    fromDate() {
      if (this.fromDate !== this.presetDates?.fromDate) {
        this.clearPresetPeriod();
      }
    },
    /**
     * When the toDate changes check if it is different from the presetDates
     */
    toDate() {
      if (this.toDate !== this.presetDates?.toDate) {
        this.clearPresetPeriod();
      }
    },
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