<template>
  <v-card min-height="250px" class="d-flex flex-column">

    <v-card-title class="my-n1 title">
      <div class="mr-1">
        {{ content.title }}
      </div>
      <v-chip v-if="showSection" color="primary" small outlined>
        {{ section }}
      </v-chip>
    </v-card-title>
    <v-card-text class="my-n2 flex-grow-1 d-flex flex-column justify-space-between">
      <div>
        <strong v-if="location">
          {{ locationString }}
          <br>
        </strong>
        <div class="d-flex justify-space-between flex-wrap">
          <span class="mr-1">
            <em v-if="creator">By {{ creator.firstName }} {{ creator.lastName }}</em>
          </span>
          <span v-if="content.created !== undefined">
            {{ dateString }}
          </span>
        </div>
        {{ content.description }}
      </div>
      <div class="v-flex mx-n1">
        <v-chip
          v-for="keyword in content.keywords"
          :key="keyword.id"
          small
          color="primary"
          class="mx-1 my-1"
        >
          {{ keyword.name }}
        </v-chip>
      </div>
    </v-card-text>
    <v-divider/>
    <v-card-actions v-if="showActions">
      <v-spacer/>
      <v-icon v-if="isCardOwnerOrAdmin"
              ref="editButton"
              class="mr-2"
              color="primary"
              @click.stop="editCardDialog = true"
      >
        mdi-pencil
      </v-icon>

      <v-icon v-if="isCardOwnerOrAdmin"
              ref="deleteButton"
              color="primary"
              @click.stop="deleteCardDialog = true"
      >
        mdi-trash-can
      </v-icon>
      <v-dialog
        ref="deleteDialog"
        v-model="deleteCardDialog"
        max-width="300px"
      >
        <v-card>
          <v-card-title>
            Are you sure?
          </v-card-title>
          <v-card-text>
            Deleting "{{ content.title }}" will remove the card listing from the marketplace
          </v-card-text>
          <v-card-actions>
            <v-spacer/>
            <v-btn
              color="primary"
              text
              @click="deleteCard(content.id); deleteCardDialog = false;"
            >
              Delete
            </v-btn>
            <v-btn
              color="primary"
              text
              @click="deleteCardDialog = false"
            >
              Cancel
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <v-tooltip bottom>
        <template v-slot:activator="{on, attrs }">
          <v-icon v-if="!isCardOwner"
                  ref="messageButton"
                  color="primary"
                  @click.stop="messageOwnerDialog = true; directMessageContent=''"
                  v-bind="attrs"
                  v-on="on"
          >
            mdi-email
          </v-icon>
        </template>
        Ask this person a question
      </v-tooltip>
      <!-- Dialog for firstMessage(in primary colour), replyMessage(in secondary colour) -->
      <v-dialog ref="messageDialog"
                v-model="messageOwnerDialog"
                max-width="600px">
        <v-card>
          <!-- The 'TITLE' of the firstMessage component -->
          <v-card color='primary lighten-3'>
            <v-card-title>
              <strong>Send a message to {{content.creator.firstName}}</strong>
            </v-card-title>
            <v-card-subtitle>
              Your message will appear on their feed
            </v-card-subtitle>
          </v-card>
          <!-- The Message body input component -->
          <v-form v-model="directMessageValid" ref="directMessageForm">
            <v-card-text>
              <v-textarea
                solo
                outlined
                clearable
                prepend-inner-icon="mdi-comment"
                no-resize
                :counter="200"
                :rules="mandatoryRules.concat(maxCharRules())"
                v-model="directMessageContent"/>
            </v-card-text>
            <!-- Submit and Cancel button for the replyMessage component -->
            <v-card-actions>
              <v-alert v-if="directMessageError !== undefined" color="red" type="error" dense text>
                {{directMessageError}}
              </v-alert>
              <v-spacer/>
              <v-btn color="primary"
                     text
                     :disabled="!directMessageValid"
                     @click="sendMessage">
                Send
              </v-btn>
              <v-btn color="primary"
                     text
                     @click="messageOwnerDialog = false; directMessageError = undefined; directMessageContent=''">
                Cancel
              </v-btn>
            </v-card-actions>
          </v-form>
        </v-card>
      </v-dialog>
    </v-card-actions>
    <template v-if="editCardDialog">
      <MarketplaceCardForm :user="$store.state.user" :previousCard="content" @closeDialog="editCardDialog=false"/>
    </template>
  </v-card>
</template>

<script>
import {formatDate, maxCharRules, mandatoryRules, SECTION_NAMES} from '@/utils';
import MarketplaceCardForm from '../marketplace/MarketplaceCardForm.vue';
import {deleteMarketplaceCard, messageConversation} from "@/api/marketplace";

export default {
  name: "MarketplaceCard",
  data () {
    return {
      deleteCardDialog: false,
      editCardDialog: false,
      messageOwnerDialog: false,
      directMessageContent: '',
      directMessageError: undefined,
      directMessageValid: false,
      mandatoryRules,
      maxCharRules: () => maxCharRules(200),
    };
  },
  components: {
    MarketplaceCardForm,
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
    showSection: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    creator() {
      return this.content.creator;
    },
    location() {
      return this.creator?.homeAddress;
    },
    locationString() {
      if (this.location.district !== undefined && this.location.city !== undefined) {
        return `From ${this.location.district}, ${this.location.city}`;
      } else if (this.location.city !== undefined) {
        return `From ${this.location.city}, ${this.location.country}`;
      } else {
        return `From ${this.location.country}`;
      }
    },
    dateString() {
      let dateString = "Posted " + formatDate(this.content.created);
      if (this.content.lastRenewed !== this.content.created) {
        dateString += ", Renewed " + formatDate(this.content.lastRenewed);
      }
      return dateString;
    },
    // To ensure only the card owner, DGAA or GAA is able to execute an action relating to the marketplace card
    isCardOwnerOrAdmin() {
      return this.isCardOwner
            || (this.$store.getters.role === "defaultGlobalApplicationAdmin")
            || (this.$store.getters.role === "globalApplicationAdmin");
    },
    isCardOwner() {
      return (this.$store.state.user.id === this.content.creator.id);
    },
    section() {
      return SECTION_NAMES[this.content.section];
    },
  },

  methods: {
    formatDate,
    /**
     * Deletes the selected marketplace card and emits the response to the parent, Marketplace
     */
    async deleteCard(cardId) {
      let response = await deleteMarketplaceCard(cardId);
      this.$emit("delete-card", response);
    },
    async sendMessage() {
      this.directMessageError = undefined;
      let response = await messageConversation(this.content.id, this.$store.state.user.id, this.$store.state.user.id, this.directMessageContent);
      if (typeof response === 'string') {
        this.directMessageError = response;
      } else {
        this.messageOwnerDialog = false;
      }
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