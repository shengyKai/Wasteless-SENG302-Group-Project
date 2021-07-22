<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <v-card>
        <v-card-title>
          Create new keyword
        </v-card-title>
        <v-textarea v-model="keyword" placeholder="New keyword"/>
        <v-card-actions>
          <div v-if="error">
            {{errorMessage}}
          </div>
          <v-btn text color="primary" :disabled="!valid" @click="createKeyword">
            Create Keyword
          </v-btn>
          <v-btn text color="primary" @click="closeDialog">
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>

import {getKeywords} from "@/api/internal";

export default {
  name: "CreateKeyword",
  data() {
    return {
      keyword: "",
      error: false,
      dialog: true,
      errorMessage: "",
      allKeywords: []
    };
  },
  methods: {
    closeDialog() {
      this.$emit('closeDialog');
    },
    createKeyword() {
      const result = ''; //await createNewKeyword(this.keyword); <-- Task 219
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.closeDialog();
      }
    }
  },
  computed: {
    valid() {
      let unique = this.allKeywords.indexOf(this.keyword.toLowerCase()) === -1;
      let length = this.keyword.length > 0 && this.keyword.length < 25;
      let regex = this.keyword.matches("^[ \\p{L}]*$");
      return length && regex && unique;
    }
  },
  mounted() {
    getKeywords()
      .then((response) => {
        if (typeof response === 'string') {
          this.allKeywords = [];
        } else {
          this.allKeywords = response;
        }})
      .catch(() => (this.allKeywords = []));
  }
};
</script>