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
      businessReportTitle: "Sheep Biz"
    };
  },
  computed: {
    title() {
      return `Sales Report - ${this.businessReportTitle}`;
    }
  }
};

</script>