<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="400px"
    >
      <v-card>
        <v-card-title>
          {{ modalTitle }}
        </v-card-title>
        <v-container class="pa-6">
          <v-row>
            <v-select
              class="required"
              v-model="selectedSection"
              :items="sections"
              item-text="text"
              item-value="value"
              label="Select section"
              color="primary"
            />
          </v-row>
          <v-row class="justify-center">
            <v-col cols="auto">
              <v-card elevation="4" class="px-4 pb-2 d-flex flex-column" min-height="250px">
                <v-card-title class="my-n1 title">
                  <textarea rows="1" v-model="title" ref="titleField" class="field" placeholder="Insert Title"/>
                </v-card-title>
                <v-card-text class="my-n2 flex-grow-1 d-flex flex-column">
                  <strong>
                    {{ locationString }}
                  </strong>
                  <div class="d-flex justify-space-between flex-wrap">
                    <span class="mr-1">
                      <em v-if="creator">By {{ user.firstName }} {{ user.lastName }}</em>
                    </span>
                    <span>
                      Posted {{ creationString }}
                    </span>
                  </div>
                  <textarea ref="descriptionField" v-model="description" rows="3" class="field" placeholder="Insert Description"/>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
          <v-row>
            <div class="keyword">
              <v-select
                class="keyword-child"
                no-data-text="No keywords found"
                value = "keywords"
                v-model="selectedKeywords"
                :items="allKeywords"
                :hint="selectedKeywordsNames"
                label="Select keywords"
                item-text="name"
                item-value="id"
                multiple
                persistent-hint
                outlined
              >
                <template v-slot:prepend-item>
                  <v-list-item>
                    <v-list-item-content>
                      <v-text-field
                        label="Search for a keyword" v-model="keywordFilter"
                        :autofocus="true"
                        @input="searchKeywords()"
                        hint="Keyword name"
                      />
                    </v-list-item-content>
                  </v-list-item>
                </template>
              </v-select>
              <!-- Add new keyword -->
              <v-btn class="keyword-child" color="primary" @click="addNewKeyword" title="Can't find what you're looking for? Hit '+' to create a new keyword out of what you have currently typed">
                <v-icon>mdi-plus-box</v-icon>
              </v-btn>
            </div>
            <p class="error-text text-center" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            <v-card-actions>
              <v-spacer/>
              <div class="error--text" v-if="feedback !== undefined">{{ feedback }}</div>
              <v-btn text color="primary" :disabled="!valid" @click="submit">
                {{ submitText }}
              </v-btn>
              <v-btn text color="primary" @click="closeDialog">
                Cancel
              </v-btn>
            </v-card-actions>
          </v-row>
        </v-container>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>
import {createMarketplaceCard, modifyMarketplaceCard} from "@/api/internal-marketplace";
import {createNewKeyword, searchKeywords} from "@/api/internal-keyword";

export default {
  name: "MarketplaceCard",
  props: {
    user: Object,
    previousCard: Object,
  },
  data() {
    return {
      title: this.previousCard?.title ?? "",
      description: this.previousCard?.description ?? "",
      allKeywords: [],
      selectedKeywords: this.previousCard?.keywords ?? [],
      keywordFilter: "",
      dialog: true,
      errorMessage: undefined,
      sections: [{text: "For Sale", value: "ForSale"}, {text: "Wanted", value: "Wanted"}, {text: "Exchange", value: "Exchange"}],
      selectedSection: this.previousCard?.section ?? undefined,
      allowedCharsRegex: /^[\s\d\p{L}\p{P}]*$/u,
    };
  },
  mounted() {
    function OnInput() {
      this.style.height = "auto";
      this.style.height = (this.scrollHeight) + "px";
    }

    this.descriptionField.setAttribute("style", "height:" + (this.descriptionField.scrollHeight) + "px;overflow-y:hidden;");
    this.descriptionField.addEventListener("input", OnInput);

    this.titleField.setAttribute("style", "height:" + (this.titleField.scrollHeight) + "px;overflow-y:hidden;");
    this.titleField.addEventListener("input", OnInput);

    this.searchKeywords();

    if (this.previousCard) {
      const prevKeywordIds = [];
      for (let keyword of this.previousCard.keywords) {
        prevKeywordIds.push(keyword.id);
      }
      this.selectedKeywords = prevKeywordIds;
    }
  },
  computed: {
    selectedKeywordsNames() {
      let names = "";
      for (let i = 0; i < this.selectedKeywords.length; i++) {
        for (let j = 0; j < this.allKeywords.length; j++) {
          if (this.selectedKeywords[i] === this.allKeywords[j].id) {
            names += this.allKeywords[j].name + ', ';
          }
        }
      }
      return names.replace(/, +$/, "");
    },
    descriptionField() {
      return this.$refs.descriptionField;
    },
    titleField() {
      return this.$refs.titleField;
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
    valid() {
      return (this.validTitle && this.validDescription && this.validSection);
    },
    validTitle() {
      return (this.title && this.title.length > 0 && this.title.length < 50 && this.allowedCharsRegex.test(this.title));
    },
    validDescription() {
      if (!this.description) {
        return true;
      } else {
        return (this.description.length < 200 && this.allowedCharsRegex.test(this.description));
      }
    },
    validSection() {
      return !!this.selectedSection;
    },
    feedback() {
      if (!this.title || this.title.length === 0) {
        return 'Card title must be provided';
      }
      if (this.title.length > 50) {
        return 'Card title must not be longer than 50 characters';
      }
      if (!this.allowedCharsRegex.test(this.title)) {
        return 'Card title must only contain letters, numbers, punctuation and whitespace';
      }
      if (this.description && this.description.length > 200) {
        return 'Card description must not be longer than 200 characters';
      }
      if (this.description && !this.allowedCharsRegex.test(this.description)) {
        return 'Card description must only contain letters, numbers, punctuation and whitespace';
      }
      if (!this.selectedSection) {
        return 'Section must be selected';
      }
      return undefined;
    },
    isCreate() {
      return this.previousCard === undefined;
    },
    modalTitle() {
      if (this.isCreate) {
        return "Create Marketplace Card";
      }
      return "Modify Marketplace Card";
    },
    submitText() {
      if (this.isCreate) {
        return "Create Card";
      }
      return "Save Card";
    },
  },
  methods: {
    async addNewKeyword() {
      if (this.keywordFilter.length > 0) {
        let keyword = {
          name: this.keywordFilter
        };
        const result = await createNewKeyword(keyword);
        if (typeof result === 'string') {
          this.errorMessage = result;
        } else {
          await this.searchKeywords();
          this.selectedKeywords.push(result);
        }
      }
    },
    resetSearch() {
      this.keywordFilter = "";
      this.searchKeywords();
    },
    closeDialog() {
      this.$emit('closeDialog');
    },
    async submit() {
      this.errorMessage = undefined;
      let card = {
        section: this.selectedSection,
        title: this.title,
        description: this.description,
        keywordIds: this.selectedKeywords,
      };
      let response;
      if (this.isCreate) {
        card.creatorId = this.user.id;
        response = await createMarketplaceCard(card);
      } else {
        response = await modifyMarketplaceCard(this.previousCard.id, card);
      }
      if (typeof response === 'string') {
        this.errorMessage = response;
      } else {
        this.closeDialog();
        this.$router.go();
      }
    },
    async searchKeywords() {
      const filterText = this.keywordFilter ?? '';
      this.errorMessage = undefined;
      const response = await searchKeywords(filterText);
      if (typeof response === 'string') {
        this.errorMessage = response;
      } else {
        this.allKeywords = response;
      }
    }
  }
};
</script>


<style scoped>
.keyword {
  display: flex;
}
.keyword-child {
  margin: 0.5em;
}
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