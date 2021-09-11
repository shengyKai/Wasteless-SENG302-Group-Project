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
          <v-btn color="error"  @click="deleteKeyword">
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
import {deleteKeyword} from "@/api/internal-keyword";

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
    async deleteKeyword() {

      let response = await deleteKeyword(this.keyword.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }
      for (let event of this.$store.getters.events) {
        if (event.type === 'KeywordCreatedEvent' && event.keyword.id === this.keyword.id) {
          this.$store.commit("removeEvent", event.id);
          break;
        }
      }


      this.$emit('keywordDeleted');
      this.$emit('closeDialog');
    },
  }
};
</script>
