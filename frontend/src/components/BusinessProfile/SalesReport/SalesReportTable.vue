<template>
  <v-data-table
    :headers="headers"
    :items="reportValues"
  />
</template>

<script>
export default {
  name: "SalesReportTable",
  data() {
    return {
      reportType: "daily", // TODO Should be replaced with a value retrieved from the backend
      /**
       * These headers are shared throughout report types
       */
      baseHeaders: [
        { text: 'Year', value: 'year' },
        { text: 'No. of Listings Created', value: 'numberCreated' },
        { text: 'No. of Expired Listings', value: 'numberExpired' },
        { text: 'No. of Purchases', value: 'numberPurchases' },
        { text: 'Currency', value: 'currency' },
        { text: 'Total Value of all Purchases ($)', value: 'totalValue' },
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
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 20,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 21,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 22,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 24,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 25,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 26,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
          day: 27,
          week: 23,
          month: 5
        },
        {
          year: 2021,
          numberCreated: 3,
          numberExpired: 4,
          numberPurchases: 5,
          currency: "NZD",
          totalValue: 300,
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
      if (this.reportType === ("yearly" || "periodically")) {
        return this.baseHeaders;
      } else {
        return this.distinctHeaders[this.reportType].concat(this.baseHeaders);
      }
    }
  }
};
</script>
