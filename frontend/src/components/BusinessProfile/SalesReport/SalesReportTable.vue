<template>
  <div class="text-h4 mb-3 mt-3">
    <strong>
      {{ reportTitle }}
    </strong>
    <v-data-table
      generateReport='0'
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
        { text: 'Year', value: 'year' },
        { text: 'No. of Unique Buyers', value: 'uniqueBuyers' },
        { text: 'No. of Unique Products', value: 'uniqueProducts' },
        { text: 'No. of Unique Listings', value: 'uniqueListingsSold' },
        { text: 'Average Time to Sell (days)', value: 'averageDaysToSell' },
        { text: 'Average Like Count', value: 'averageLikeCount' },
        { text: 'Total Quantity Sold', value: 'totalQuantitySold' },
        { text: 'Total Value of all Purchases ($)', value: 'totalPriceSold' },
      ],
      /**
       * These headers are the distinctive headers that differentiates between report types
       */
      distinctHeaders: {
        "monthly": [
          { text: 'Month', value: 'month' }
        ],
        "weekly": [
          { text: 'Week No.', value: 'week' },
          { text: 'Month', value: 'month' }
        ],
        "daily": [
          { text: 'Day of the Month', value: 'day' },
          { text: 'Week No.', value: 'week' },
          { text: 'Month', value: 'month' }
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
      if (this.fullReport.reportType === "yearly") {
        return this.baseHeaders;
      } else {
        return this.distinctHeaders[this.fullReport.reportType].concat(this.baseHeaders);
      }
    },
    /**
     * Generates the report title based on the reportType in the format '"reportType" Report'
     */
    reportTitle() {
      return `${this.fullReport.reportType.charAt(0).toUpperCase() + this.fullReport.reportType.slice(1)} Report`;
    },
  },
};
</script>
