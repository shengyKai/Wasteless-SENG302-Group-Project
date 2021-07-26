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
    <v-card-actions v-if="isCardOwnerOrDGAA && showActions">
      <v-icon ref="deleteButton"
              color="primary"
              @click.stop="deleteCardDialog = true"
      >
        mdi-trash-can
      </v-icon>
      <v-dialog
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
      <v-btn
        @click="editCardDialog=true">
        Temporary edit button
      </v-btn>
      <template v-if="editCardDialog">
        <MarketplaceCardForm :user="$store.state.user" :previousCard="content" @closeDialog="editCardDialog=false"/>
      </template>
    </v-card-actions>
  </v-card>
</template>

<script>
import { formatDate, SECTION_NAMES } from '@/utils';
import { deleteMarketplaceCard } from '../../api/internal.ts';
import MarketplaceCardForm from '../marketplace/MarketplaceCardForm.vue';

export default {
  name: "MarketplaceCard",
  data () {
    return {
      deleteCardDialog: false,
      editCardDialog: false
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
    isCardOwnerOrDGAA() {
      return (this.$store.state.user.id === this.content.creator.id)
            || (this.$store.getters.role === "defaultGlobalApplicationAdmin")
            || (this.$store.getters.role === "globalApplicationAdmin");
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
    }
  }
};
</script>


<style scoped>
.title {
  line-height: 1.25;
  word-break: break-word;
}
</style>