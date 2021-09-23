<template>
  <v-container>
    <!-- Red block to show this is the admin dashbaord/ panel -->
    <v-card color="grey lighten-3">
      <v-footer>
        <v-card height="70px" rounded width="100%" color="primary" class=" text-center font-weight-bold ">
          <v-divider/>

          <v-card-text class="white--text">
            {{ new Date().getFullYear() }} —
            <strong>Admin Dashboard</strong>
          </v-card-text>
        </v-card>
      </v-footer>
      <v-alert
        v-if="error !== undefined"
        type="error"
        dismissible
        @input="error = undefined"
      >
        {{ error }}
      </v-alert>

      <v-row justify="center">
        <v-col cols="4">
          <template>
            <v-simple-table
              fixed-header
              height="400px"
            >
              <template v-slot:default>
                <thead>
                  <tr>
                    <th id="name">
                      Keywords
                    </th>
                    <th id="delete"/>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="keyword in keywords"
                    :key="keyword.id"
                  >
                    <td>{{ keyword.name }}</td>
                    <td>
                      <v-icon ref="deleteButton"
                              color="primary"
                              @click.stop="keywordToDelete=keyword"
                      >
                        mdi-trash-can
                      </v-icon>
                    </td>
                  </tr>
                </tbody>
              </template>
            </v-simple-table>
          </template>
        </v-col>
      </v-row>
    </v-card>
    <template v-if="keywordToDelete">
      <DeleteKeyword :keyword="keywordToDelete" @closeDialog="keywordToDelete=undefined"/>
    </template>
  </v-container>
</template>

<script>
import DeleteKeyword from "./DeleteKeyword.vue";
import {searchKeywords} from "@/api/keyword";

export default {
  data () {
    return {
      keywords: [],
      error: undefined,
      keywordToDelete: undefined,
    };
  },
  methods: {
    async setKeywords() {
      this.error = undefined;
      const response = await searchKeywords("");
      if (typeof response === 'string') {
        this.error = response;
      } else {
        this.keywords = response;
      }
    },
  },
  mounted() {
    this.setKeywords();
  },
  components: {
    DeleteKeyword,
  },
  watch: {
    keywordToDelete() {
      if (!this.keywordToDelete) {
        this.setKeywords();
      }
    }
  },
};
</script>