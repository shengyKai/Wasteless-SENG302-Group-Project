<template>
  <v-container>
    <div class="text-h3 text-center mb-2">
      <strong>
        {{ title }}
      </strong>
    </div>
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