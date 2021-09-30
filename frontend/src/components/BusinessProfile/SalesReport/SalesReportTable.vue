<template>
  <div class="text-h4 mb-3 mt-3">
    <strong>
      {{ reportTitle }}
    </strong>
    <v-data-table
      disable-sort
      :headers="headers"
      :items="fullReport.reportData"
    />
  </div>
</template>

<script>

export default {
  name: "SalesReportTable",
  props: {
    fullReport: Object
  },
  data() {
    return {
      /**
       * These headers are shared throughout report types
       */
      baseHeaders: [
        { text: 'No. of Unique Buyers', value: 'uniqueBuyers' },
        { text: 'No. of Unique Products', value: 'uniqueProducts' },
        { text: 'No. of Listings Sold', value: 'uniqueListingsSold' },
        { text: 'Average Time to Sell (days)', value: 'averageDaysToSell' },
        { text: 'Average Like Count', value: 'averageLikeCount' },
        { text: 'Total Quantity Sold', value: 'totalQuantitySold' },
        { text: 'Total Value of all Purchases ($)', value: 'totalPriceSold' },
      ],
      /**
       * These headers are the unique headers that differentiates between report types
       */
      uniqueHeaders: {
        none: [],
        yearly: [
          { text: 'Year', value: 'year' },
        ],
        monthly: [
          { text: 'Year', value: 'year' },
          { text: 'Month', value: 'month' },
        ],
        weekly: [
          { text: 'Year', value: 'year' },
          { text: 'Month', value: 'month' },
          { text: 'Week No.', value: 'week' },
        ],
        daily: [
          { text: 'Year', value: 'year' },
          { text: 'Month', value: 'month' },
          { text: 'Week No.', value: 'week' },
          { text: 'Day in Month', value: 'day' },
        ]
      },
    };
  },
  computed: {
    /**
     * The concatenation of the baseHeaders and distinctHeaders if the reportTypes are daily, weekly or monthly.
     * Otherwise, the headers are just the baseHeaders.
     */
    headers() {
      return this.uniqueHeaders[this.fullReport.reportType].concat(this.baseHeaders);
    },
    /**
     * Generates the report title based on the reportType in the format '"reportType" Report'
     */
    reportTitle() {
      const reportType = this.fullReport.reportType;
      let reportTypeName;
      if (reportType === 'none') {
        reportTypeName = 'Whole Period';
      } else {
        reportTypeName = this.fullReport.reportType.charAt(0).toUpperCase() + this.fullReport.reportType.slice(1);
      }

      return `${reportTypeName} Report`;
    },
  },
};
</script>
