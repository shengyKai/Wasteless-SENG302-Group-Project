<template>
  <v-container>
    <h2 class="font-weight-bold text-center">{{ title }}</h2>
    <ReportOptionsBar @sendRequestParams="generateFullReport"/>
    <p class="error-text text-center" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
    <SalesReportTable v-if="fullReport !== undefined" :fullReport="fullReport"/>
  </v-container>
</template>

<script>
import ReportOptionsBar from "./ReportOptionsBar.vue";
import SalesReportTable from "./SalesReportTable.vue";
import { generateReport } from "@/api/salesReport.ts";
import { getBusiness } from "@/api/business";

export default {
  name: "ReportGenerationBar",
  components: {
    ReportOptionsBar,
    SalesReportTable
  },
  data(){
    return {
      businessName: undefined,
      fullReport: undefined,
      errorMessage: undefined,
    };
  },
  methods: {
    /**
     * Method to call the api endpoint, generateReport. Then breaks down the dates of the report data to make it suitable for viewing.
     * Finally adds in the report type
     */
    async generateFullReport(requestParams) {
      this.errorMessage = undefined;
      let reportData = await generateReport(this.$route.params.id, requestParams.fromDate, requestParams.toDate, requestParams.granularity);
      if (typeof reportData === 'string') {
        this.errorMessage = reportData;
        this.fullReport = undefined;
      } else {
        this.formatReportData(reportData);
        this.fullReport = {reportData: reportData, reportType: requestParams.granularity};
      }
    },
    /**
     * Breakdown the dates retrieved from the backend so that it can be presented in the table later. This is done here
     * because we are not able to mutate the reportData as a prop later on in SalesReportTable.
     * If the results from the backend are omitting averageLikeCount or averageDaysToSell due to no values for them,
     * default those column values to 0.
     */
    formatReportData(reportData) {
      for (let row of reportData) {
        row.day = new Date(row.startDate).getDate();
        row.week = this.getWeekNo(new Date(row.startDate));
        row.month = new Date(row.startDate).toLocaleString('default', {month: 'long'});
        row.year = new Date(row.startDate).getFullYear();

        row.averageLikeCount = row.averageLikeCount?.toFixed(1) ?? '-';
        row.averageDaysToSell = row.averageDaysToSell?.toFixed(1) ?? '-';
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
  computed: {
    /**
     * Creates the main title of the sales report page by the format Sales Report - *Business name*
     */
    title() {
      return `Sales Report - ${this.businessName}`;
    }
  },
  async mounted() {
    let business = await getBusiness(this.$route.params.id);
    if (typeof business === 'string') {
      this.errorMessage = business;
    } else {
      this.businessName = business.name;
    }
  }
};

</script>