<template>
  <v-container>
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
  }
};

</script>
