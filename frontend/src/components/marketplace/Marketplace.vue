<template>
  <div>
    <v-container fluid>
      <v-row align="center">
        <v-col
          class="d-flex"
          cols="6"
          sm="4"
        >
          <v-select
            v-model="orderBy"
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added',                 value: 'created'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col cols="1">
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col
          class="d-flex"
          cols="6"
          sm="4">
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Keywords"
            autofocus
          />
        </v-col>
        <v-col cols="1" class="text-right" >
          <v-btn type="button" color="primary" rounded>
            Create card
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <v-tabs
      v-model="tab"
      grow
    >
      <v-tab
        v-for="section in sections"
        :key="section"
      >
        {{ section }}
      </v-tab>
      <v-tabs-items>
        <v-tab-item
          v-for="section in sections"
          :key="section"
        >
          <v-card/>
        </v-tab-item>
      </v-tabs-items>
    </v-tabs>
  </div>
</template>

<script>
export default {
  data() {
    return {
      sections: ["For sale", "Wanted", "Exchange"]
    };
  }
};
</script>
