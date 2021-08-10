<template>
  <v-row justify="center" >
    <v-dialog
      v-model="dialog"
      persistent
      max-width="400px"
      color="red"
    >
      <v-card class="purple accent-1">
        <v-card-title >
          Replying to that motheryucker
        </v-card-title>
        <v-container class="pa-6 grey lighten-5" >
          <v-row class="justify-center" max-width="400px">
            <v-col cols="auto" >
              <v-card elevation="4" class="px-4 pb-2 d-flex flex-column" min-height="250px" min-width="300px">
                <v-card-title class="my-n1 title" >
                  <textarea rows="1" v-model="title" ref="titleField" class="field" placeholder="To: HAHA"/>
                </v-card-title>
                <v-card-text class="my-n2 flex-grow-1 d-flex flex-column">
                  <div class="d-flex justify-space-between flex-wrap">
                    <span class="mb-7">
                      Posted on B.C 600
                    </span>
                  </div>
                  <v-card min-height="130px" min-width="300px" color="green lighten-4">
                    <textarea ref="descriptionField" v-model="description" rows="2" class="field mt-8 ml-3" placeholder="Your message.."/>
                  </v-card>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </v-card>
      <v-card-actions color="red" class="justify-right">
        <v-icon ref="editButton"
                class="mr-2"
                color="black"
                @click.stop="editCardDialog = true"
        >
          mdi-cloud-upload
        </v-icon>

        <v-icon ref="deleteButton"
                color="black"
                @click.stop="deleteCardDialog = true"
        >
          mdi-trash-can
        </v-icon>
      </v-card-actions>
    </v-dialog>

  </v-row>
</template>

<script>
import { formatDate} from '@/utils';

export default {
  name: "ConversationReply",
  data () {
    return {
      title: this.previousCard?.title ?? "",
      description: this.previousCard?.description ?? "",
      dialog: true,
      deleteCardDialog: false,
      editCardDialog: false,
      errorMessage: undefined,
    };
  },
  props: {
    content: {
      id: Number,
      title: String,
      description: String,
      creator: Object,
      created: String,
      lastRenewed: String,
      keywords: Array,
    },
    showActions: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    dateString() {
      let dateString = "Posted " + formatDate(this.content.created);
      if (this.content.lastRenewed !== this.content.created) {
        dateString += ", Renewed " + formatDate(this.content.lastRenewed);
      }
      return dateString;
    },

  },

  methods: {
    formatDate,
    closeDialog() {
      this.$emit('closeDialog');
    },
  }
};
</script>


<style scoped>
.title {
  line-height: 1.25;
  word-break: break-word;
}
</style>