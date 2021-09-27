<template>
  <v-data-table
    :headers="headers"
  >
    something
  </v-data-table>
</template>

<script>
export default {
  name: "SalesReportTable",
  data() {
    return {
      reportType: "daily",
      baseHeaders: [
        { text: 'Year', value: 'year' }, // TODO link up the year attribute here
        { text: 'No. of Listings Created', value: 'numberCreated' }, // TODO link up the numberCreated attribute here
        { text: 'No. of Expired Listings', value: 'numberExpired' }, // TODO link up the numberExpired attribute here
        { text: 'No. of Purchases', value: 'numberPurchases' }, // TODO link up the numberPurchases attribute here
        { text: 'Currency', value: 'currency' }, // TODO link up the currency attribute here
        { text: 'Total Value of all Purchases', value: 'total' }, // TODO link up the total attribute here
      ],
      distinctHeaders: {
        "monthly": [
          { text: 'Month', value: 'month' } // TODO link up the month attribute here
        ],
        "weekly": [
          { text: 'Week No.', value: 'week' }, // TODO link up the week attribute here
          { text: 'Month', value: 'month' }, // TODO link up the month attribute here
        ],
        "daily": [
          { text: 'Day of the Month', value: 'day' }, // TODO link up the day attribute here
          { text: 'Week No.', value: 'week' }, // TODO link up the week attribute here
          { text: 'Month', value: 'month' }, // TODO link up the month attribute here
        ]
      }
    };
  },
  computed: {
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
