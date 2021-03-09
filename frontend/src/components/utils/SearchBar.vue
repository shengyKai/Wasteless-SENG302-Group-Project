<template>
    <v-toolbar dense floating>
        <v-form @submit.prevent="submit">
            <v-text-field hide-details single-line v-model="query"></v-text-field>
        </v-form>
        <v-btn icon @click="submit">
            <v-icon>mdi-magnify</v-icon>
        </v-btn>
    </v-toolbar>
</template>

<script>
export default {
    name: "SearchBar",
    data() {
        return {
            query: ''
        };
    },
    methods: {
        submit() {
            this.$router.push({ path: "/search", query: {query: this.query} });
        },
    },
};
//hide-details - before icon
</script>
