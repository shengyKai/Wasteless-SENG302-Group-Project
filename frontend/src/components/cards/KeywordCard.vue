<template>
  <v-card
    class="mx-auto"
    width="200"
    height="80"
  >
    <v-row>
      <v-col cols="auto">
        <v-card-title>
          {{ keyword.name }}
        </v-card-title>
      </v-col>
      <v-spacer/>
      <v-col cols="auto">
        <v-icon ref="deleteButton"
                large
                color="primary"
                @click.stop="deleteKeywordDialog=true"
        >
          mdi-trash-can
        </v-icon>

      </v-col>
    </v-row>
  </v-card>
</template>

<script>

export default {
  props: {
    keyword: Object,
  },
  data() {
    return {
      deleteKeywordDialog: false,
    };
  }
};
</script>
