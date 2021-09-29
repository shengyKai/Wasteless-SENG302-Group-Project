<template>
  <v-container>
    <h2 class="font-weight-bold text-center">{{ title }}</h2>
    <ReportOptionsBar @sendRequestParams="generateFullReport"/>
    <SalesReportTable v-if="fullReport !== null" :fullReport="fullReport"/>
  </v-container>
</template>

<script>
import ReportOptionsBar from "./ReportOptionsBar.vue";
import SalesReportTable from "./SalesReportTable.vue";
import { generateReport } from "@/api/salesReport.ts";

export default {
  name: "ReportGenerationBar",
  components: {
    ReportOptionsBar,
    SalesReportTable
  },
  data(){
    return {
      businessName: "Sheep Biz",
      fullReport: null
    };
  },
  methods: {
    /**
     * Method to call the api endpoint, generateReport. Then breaks down the dates of the report data to make it suitable for viewing.
     * Finally adds in the report type
     */
    async generateFullReport(requestParams) {
      let reportData = await generateReport(requestParams.businessId, requestParams.fromDate, requestParams.toDate, requestParams.granularity);
      this.formatReportData(reportData);
      this.fullReport = {reportData: reportData, reportType: requestParams.granularity};
    },
    /**
     * Breakdown the dates retrieved from the backend so that it can be presented in the table later. This is done here
     * because we are not able to mutate the reportData as a prop later on in SalesReportTable.
     * If the results from the backend are omitting averageLikeCount or averageDaysToSell due to no values for them,
     * default those column values to 0.
     */
    formatReportData(reportData) {
      for (let row in reportData) {
        reportData[row]["day"] = new Date(reportData[row]["endDate"]).getDate();
        reportData[row]["week"] = this.getWeekNo(new Date(reportData[row]["endDate"]));
        reportData[row]["month"] = new Date(reportData[row]["endDate"]).toLocaleString('default', {month: 'long'});
        reportData[row]["year"] = new Date(reportData[row]["endDate"]).getFullYear();
        if (!Object.prototype.hasOwnProperty.call(reportData[row], "averageLikeCount")) {
          reportData[row]["averageLikeCount"] = "-";
        }
        if (!Object.prototype.hasOwnProperty.call(reportData[row], "averageDaysToSell")) {
          reportData[row]["averageDaysToSell"] = "-";
        }
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
  }
};

</script>