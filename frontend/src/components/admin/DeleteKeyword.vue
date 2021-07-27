<template>
  <v-row justify="center">
    <v-dialog
      persistent
      max-width="400px"
      v-model="dialog"
    >
      <v-card>
        <v-card-title class="justify-center">
          Delete keyword "{{ keyword.name }}"?
        </v-card-title>
        <v-card-actions class="justify-center">
          <v-btn color="error">
            Delete
          </v-btn>
          <v-btn color="secondary" @click="closeDialog()">
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>
export default {
  props: {
    keyword: Object,
  },
  data() {
    return {
      dialog: true,
    };
  },
  methods: {
    closeDialog() {
      this.$emit('closeDialog');
    },
    deleteKeyword() {
      //TODO link to api endpoint
    }
  }
};
</script>
