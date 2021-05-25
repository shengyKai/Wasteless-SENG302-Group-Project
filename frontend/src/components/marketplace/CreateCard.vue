<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <v-card min-height="250px" class="d-flex flex-column">
        <v-card-title class="my-n1 title">
          <textarea rows="1" v-model="title" ref="titleField" class="field"/>
        </v-card-title>
        <v-card-text class="my-n2 flex-grow-1 d-flex flex-column justify-space-between">
          <div>
            <strong>
              {{ locationString }}
              <br>
            </strong>
            <div class="d-flex justify-space-between flex-wrap">
              <span class="mr-1">
                <em v-if="creator">By {{ user.firstName }} {{ user.lastName }}</em>
              </span>
              <span>
                Posted {{ creationString }}
              </span>
            </div>
            <textarea ref="descriptionField" v-model="description" class="field"/>
          </div>
          <v-select
            v-model="selectedKeywords"
            :items="allKeywords"
            item-text="name"
            item-value="id"
            label="Select keywords"
            multiple
            small-chips
            color="primary"
          />
          <v-select
            v-model="selectedSection"
            :items="sections"
            item-text="text"
            item-value="value"
            label="Select section"
            color="primary"
          />
          <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
        </v-card-text>
        <v-card-actions>
          <v-btn text color="primary" @click="createCard">
            Create Card
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
import { createMarketplaceCard } from '../../api/internal';
import { getKeywords } from '../../api/internal.ts';

export default {
  name: "MarketplaceCard",
  data() {
    return {
      title: "",
      description: "",
      allKeywords: [],
      selectedKeywords: [],
      dialog: true,
      errorMessage: undefined,
      sections: [{text: "For Sale", value: "ForSale"}, {text: "Wanted", value: "Wanted"}, {text: "Exchange", value: "Exchange"}],
      selectedSection: undefined,
    };
  },
  async mounted() {
    function OnInput() {
      this.style.height = "auto";
      this.style.height = (this.scrollHeight) + "px";
    }

    this.descriptionField.setAttribute("style", "height:" + (this.descriptionField.scrollHeight) + "px;overflow-y:hidden;");
    this.descriptionField.addEventListener("input", OnInput);

    this.titleField.setAttribute("style", "height:" + (this.titleField.scrollHeight) + "px;overflow-y:hidden;");
    this.titleField.addEventListener("input", OnInput);

    console.log(this.locationString);
    this.allKeywords = await getKeywords();
  },
  computed: {
    descriptionField() {
      return this.$refs.descriptionField;
    },
    titleField() {
      return this.$refs.titleField;
    },
    user() {
      if (this.$store.state.createMarketplaceCardDialog !== undefined) {
        return this.$store.state.createMarketplaceCardDialog;
      } else {
        return undefined;
      }
    },
    creator() {
      return this.user.firstName + ' ' + this.user.lastName;
    },
    location() {
      return this.creator?.homeAddress;
    },
    locationString() {
      if (this.user.homeAddress.district !== undefined && this.user.homeAddress.city !== undefined) {
        return `From ${this.user.homeAddress.district}, ${this.user.homeAddress.city}`;
      } else if (this.user.homeAddress.city !== undefined) {
        return `From ${this.user.homeAddress.city}, ${this.user.homeAddress.country}`;
      } else {
        return `From ${this.user.homeAddress.country}`;
      }
    },
    creationString() {
      return new Date().toLocaleDateString();
    },
  },
  methods: {
    closeDialog() {
      this.$emit('closeDialog');
    },
    async createCard() {
      this.errorMessage = undefined;
      console.log(this.selectedKeywords);
      console.log(this.selectedSection);
      let card = {
        creatorId: this.user.id,
        section: this.selectedSection,
        title: this.title,
        description: this.description,
        keywordIds: this.selectedKeywords,
      };
      let response = await createMarketplaceCard(card);
      if (typeof response === 'string') {
        this.errorMessage = response;
      } else {
        this.closeDialog();
        this.$router.go();
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
.field {
  resize: none;
  width: 100%;
  color: inherit;
}

.field:focus {
  outline: none !important;
  background-color: rgba(0,0,0,0.1);
}
</style>