<template>
  <div class="text-h4 mb-3 mt-3">
    <strong>
      {{ reportTitle }}
    </strong>
    <v-data-table
      :headers="headers"
      :items="reportValues"
    />
  </div>
</template>

<script>

export default {
  name: "SalesReportTable",
  props: {
    reportData: Array
  },
  data() {
    return {
      reportType: "daily", // TODO Should be replaced with a value retrieved from the backend
      /**
       * These headers are shared throughout report types
       */
      baseHeaders: [
        { text: 'Year', value: 'year' },
        { text: 'No. of Unique Buyers', value: 'uniqueBuyers' },
        { text: 'No. of Unique Products', value: 'uniqueProducts' },
        { text: 'No. of Unique Listings', value: 'uniqueListingsSold' },
        { text: 'Average Time to Sell (days)', value: 'averageDaysToSell' },
        { text: 'Average Like Count', value: 'averageLikes' },
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
      /**
       * This list stores all the values depending on the report type and displays it on the table.
       * TODO hard coded for now, should be replaced with a value from the backend
       */
      reportValues: [
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 20,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 21,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 22,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 24,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 25,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 26,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 27,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          uniqueBuyers: 3,
          uniqueProducts: 4,
          uniqueListingsSold: 5,
          averageDaysToSell: 1,
          averageLikes: 20,
          totalPriceSold: 300,
          day: 28,
          week: 23,
          month: 5
        },
      ]
    };
  },
  computed: {
    /**
     * The concatenation of the baseHeaders and distinctHeaders if the reportTypes are daily, weekly or monthly.
     * Otherwise, the headers are just the baseHeaders.
     */
    headers() {
      if (this.reportType === "yearly" || this.reportType === "periodic") {
        return this.baseHeaders;
      } else {
        return this.distinctHeaders[this.reportType].concat(this.baseHeaders);
      }
    },
    /**
     * Generates the report title based on the reportType in the format '"reportType" Report'
     */
    reportTitle() {
      return `${this.reportType.charAt(0).toUpperCase() + this.reportType.slice(1)} Report`;
    }
  },
  mounted() {
    console.log(this.reportData);
  }
};
</script>
