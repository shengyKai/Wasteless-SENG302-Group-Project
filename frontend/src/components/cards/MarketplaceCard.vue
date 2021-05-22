<template>
  <v-card width="300px" height="200px">
    <v-card-title>
      Placeholder {{content.id}}
    </v-card-title>
  </v-card>
</template>

<script>
export default {
  name: "MarketplaceCard",
  props: {
    content: {
      id: Number
    }
  }
};
</script>
