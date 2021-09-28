<template>
  <v-container>
    <h2 class="font-weight-bold text-center">{{ title }}</h2>
    <ReportOptionsBar/>
    <SalesReportTable/>
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
      businessName: "Sheep Biz"
    };
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