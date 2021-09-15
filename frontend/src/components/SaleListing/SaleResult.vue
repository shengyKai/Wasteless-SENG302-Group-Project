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