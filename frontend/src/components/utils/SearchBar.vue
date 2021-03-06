<template>
    <v-toolbar dense floating>
        <v-text-field
            hide-details
            single-line
        ></v-text-field>

        <v-btn icon @click="clickSearch">
            <v-icon>mdi-magnify</v-icon>
        </v-btn>
    </v-toolbar>
</template>

<script>
export default {
    name: "SearchBar",
    methods: {
        clickSearch() {
            this.$router.push('/search');
        },
    },
};
//hide-details - before icon
</script>
