<template>
  <div class="text-h4 mb-3 mt-3">
    <strong>
      {{ reportTitle }}
    </strong>
    <v-data-table
      :headers="headers"
      :items="reportDetails.reportData"
    />
  </div>
</template>

<script>

export default {
  name: "SalesReportTable",
  props: {
    reportDetails: Object
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
        { text: 'Total Quantity Sold', value: 'totalQuantitySold' },
        { text: 'Average Like Count', value: 'averageLikeCount' },
        { text: 'Total Value of all Purchases ($)', value: 'totalPriceSold' },
      ],
      /**
       * These headers are the distinctive headers that differentiates between report types
       */
      distinctHeaders: {
        "month": [
          { text: 'Month', value: 'month' }
        ],
        "week": [
          { text: 'Week No.', value: 'week' },
          { text: 'Month', value: 'month' }
        ],
        "day": [
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
      if (this.reportDetails.reportType === "year") {
        return this.baseHeaders;
      } else {
        return this.distinctHeaders[this.reportDetails.reportType].concat(this.baseHeaders);
      }
    },
    /**
     * Generates the report title based on the reportType in the format '"reportType" Report'
     */
    reportTitle() {
      if (this.reportDetails.reportType === "day") {
        return `Daily Report`;
      } else {
        return `${this.reportDetails.reportType.charAt(0).toUpperCase() + this.reportDetails.reportType.slice(1)}ly Report`;
      }
    },
  },
  mounted() {
    console.log(this.reportDetails.reportData);
  }
};
</script>
