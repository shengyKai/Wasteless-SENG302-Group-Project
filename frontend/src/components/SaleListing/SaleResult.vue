<template>
  <v-card>
    <div class="title">
      <h5>Image Placeholder</h5>
      <v-card-title class="saleTitle">The Nathan Apple</v-card-title>
      <label class="saleTitleJoin"> From </label>
      <a class="businessLink">Nathan Apple LTD</a>
    </div>
    <div class="fields">
      <v-row no-gutters>
        <v-col class="column" cols="4">
          <label class="field-label">More Info:</label>
        </v-col>
        <v-col class="column" cols="8">
          <label class="field-element">This is a Nathan Apple. The absolute rarest of its kind. You will not find a Nathan Apple anywhere else.</label>
        </v-col>
        <v-col class="column" cols="4">
          <label class="field-label">Total Price:</label>
        </v-col>
        <v-col class="column" cols="8">
          <label class="field-element">$10.09</label>
        </v-col>
        <v-col class="column" cols="4">
          <label class="field-label">Quantity:</label>
        </v-col>
        <v-col class="column" cols="8">
          <label class="field-element">109</label>
        </v-col>
        <v-col class="column" cols="4">
          <label class="field-label">Date Created:</label>
        </v-col>
        <v-col class="column" cols="8">
          <label class="field-element">9 Jan 2021</label>
        </v-col>
        <v-col class="column" cols="4">
          <label class="field-label">Closing Date:</label>
        </v-col>
        <v-col class="column" cols="8">
          <label class="field-element">10 Jan 2021</label>
        </v-col>
        <v-col class="column" cols="4">
          <label class="field-label">Expiry Date:</label>
        </v-col>
        <v-col class="column" cols="8">
          <label class="field-element">11 Jan 2021</label>
        </v-col>
      </v-row>
    </div>
  </v-card>
</template>

<script>
export default {
  name: "SaleResult",
  data() {
    return {
    };
  }
};
</script>

<style scoped>
.saleTitle {
  display: inline;
}

.saleTitleJoin {
  display: inline;
  font-size: 12px;
  margin-left: -10px;
}

.businessLink {
  display: inline;
  font-size: 15px;
  text-decoration: underline;
  color: grey;
}
</style>