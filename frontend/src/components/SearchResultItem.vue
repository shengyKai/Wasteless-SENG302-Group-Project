<template>
    <v-list-item>
        <v-list-item-avatar color="primary" class="white--text headline">
            {{ user.firstName[0].toUpperCase() }}{{ user.lastName[0].toUpperCase() }}
        </v-list-item-avatar>
        <v-list-item-content>
            <v-list-item-title>
                <!--TODO Wait for vue-router to be set up correctly -->
                <!--<router-link :to="{ name: 'profile', params: { 'id': user.id }}"> -->
                    {{ user.firstName }} {{ user.lastName }} 
                <!-- </router-link> -->
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