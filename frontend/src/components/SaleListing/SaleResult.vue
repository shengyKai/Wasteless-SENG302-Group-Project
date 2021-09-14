<template>
  <v-list-item>
    <v-list-item-avatar>
      <p>img</p>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>
        <label class="result-title">The Nathan Apple</label>
        <label class="result-title-join">From </label>
        <label class="result-title-business">Nathan Apple LTD</label>
      </v-list-item-title>
      <v-list-item-subtitle>
        <label>More Info: This is a Nathan Apple. The absolute rarest of its kind. You will not find a Nathan Apple anywhere else.</label>
      </v-list-item-subtitle>
      <v-list-item-subtitle>
        <label class="total-price-label">Total Price:</label>
        <label>$10.09</label>
        <label class="divider1"/>
        <label class="quantity-label">Quantity:</label>
        <label>109</label>
      </v-list-item-subtitle>
      <v-list-item-subtitle>
        <label class="creation-date-label">Creation Date:</label>
        <label>10 Jan 2021</label>
        <label class="divider2"/>
        <label class="closing-date-label">Closing Date:</label>
        <label>15 Jan 2021</label>
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>

  <!-- <v-card>
    <div class="sale-image">
      <p>img</p>
    </div>
    <div class="title">
      <v-card-title class="saleTitle">The Nathan Apple</v-card-title>
      <label class="saleTitleJoin"> From </label>
      <a class="businessLink">Nathan Apple LTD</a>
    </div>
    <div class="fields">
      <v-row no-gutters>
        <v-col cols="1">
          <label>Total Price:</label>
        </v-col>
        <v-col cols="1">
          <label>$10.09</label>
        </v-col>
        <div class="vertical-line"/>
        <v-col cols="1">
          <label>Date Created:</label>
        </v-col>
        <v-col cols="1">
          <label>10 Jan 2021</label>
        </v-col>
        <div class="vertical-line"/>

      </v-row>


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
  </v-card> -->
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
.result-title {
  font-size: 19px;
  font-weight: bold;
}

.result-title-join {
  font-size: 15px;
  margin-left: 10px;
}

.result-title-business {
  font-size: 15px;
}

.divider1 {
  margin-right: 64px;
}

.divider2 {
  margin-right: 30px;
}

.total-price-label {
  margin-right: 28px;
}

.quantity-label {
  margin-right: 37px;
}

.creation-date-label {
  margin-right: 10px;
}

.closing-date-label {
  margin-right: 10px;
}

.vertical-line {
  border-left: 1px solid black;
  margin-top: 3px;
  margin-right: 10px;
  height: 15px;
}
</style>