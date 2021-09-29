<template>
  <v-container>
    <h2 class="font-weight-bold text-center">{{ title }}</h2>
    <ReportOptionsBar @retrievedData="passToTable"/>
    <SalesReportTable :reportData="reportData"/>
  </v-container>
</template>

<script>
import ReportOptionsBar from "./ReportOptionsBar.vue";
import SalesReportTable from "./SalesReportTable.vue";

export default {
  name: "ReportGenerationBar",
  components: {
    ReportOptionsBar,
    SalesReportTable
  },
  data(){
    return {
      businessName: "Sheep Biz",
      reportData: null
    };
  },
  methods: {
    passToTable(reportData) {
      this.reportData = reportData;
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