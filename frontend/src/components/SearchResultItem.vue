<template>
    <v-list-item>
        <v-list-item-avatar color="primary" class="white--text headline">
            {{ user.firstName[0].toUpperCase() }}{{ user.lastName[0].toUpperCase() }}
        </v-list-item-avatar>
        <v-list-item-content>
            <v-list-item-title>
                <router-link :to="'profile/' + user.id" class="link">
                    {{ user.firstName }} {{ user.lastName }} 
                </router-link>
            </v-list-item-title>
            <v-list-item-subtitle> {{ user.email }} </v-list-item-subtitle>
            <v-list-item-subtitle> {{ user.homeAddress }} </v-list-item-subtitle>
        </v-list-item-content>
    </v-list-item>
</template>

<script>
export default {
    props: ['user'],
}
</script>

<style scoped>
.link {
    color: black;
    text-decoration: none;
}

.link:hover {
    text-decoration: underline;
}

</style>