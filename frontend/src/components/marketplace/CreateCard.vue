<template>
  <v-card min-height="250px" class="d-flex flex-column">
    <v-card-title class="my-n1 title">
      <textarea rows="1" v-model="title" ref="titleField" class="field"/>
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
          <span>
            Posted {{ creationString }}
          </span>
        </div>
        <textarea ref="descriptionField" v-model="description" class="field"/>
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
  </v-card>
</template>

<script>
import { getKeywords } from '../../api/internal.ts';

export default {
  name: "MarketplaceCard",
  props: {
    // content: {
    //   id: Number,
    //   title: String,
    //   description: String,
    //   creator: Object,
    //   created: String,
    //   keywords: Array,
    // },
    user: Object
  },
  data() {
    return {
      title: "",
      description: "",
      allKeywords: [],
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

    this.allKeywords = await getKeywords();
  },
  computed: {
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
  },
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