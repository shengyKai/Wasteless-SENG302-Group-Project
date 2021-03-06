import Vue from 'vue';
import VueRouter from 'vue-router';

import Auth from "../components/Auth/index.vue";
import ProfilePage from "../components/ProfilePage.vue";
import SearchResults from '../components/SearchResults.vue';

Vue.use(VueRouter);

// Doesn't actually send status code 404
const NotFound = {
    template: '<h1> 404 Not Found - {{ $route.path }} </h1>'
};

const ProfileNotFound = {
    template: '<h1> 404 - Profile Not Found  </h1>'
};

const routes = [
    { path: '/', redirect: '/login'}, // TODO handle case when already logged in

    { path: '/login',   component: Auth },
<<<<<<< HEAD
    {   
        // for access path /profile -- or can direct to error page when ID not provided
        path: '/profile', 
        component: ProfilePage, 
        name: 'profile', 

        // for access path /profile/:id
        children: [
            {
            path: ':id',
            name: 'profileData',
            component: ProfilePage
            },
        ], 
    },
=======

    { path: '/profile/:id', component: ProfilePage },
    { path: '/profile', component: ProfileNotFound },
>>>>>>> 5ab54c97a5570c58798389e4088714566916fe6f
    
    { path: '/search',  component: SearchResults },
    { path: '*',        component: NotFound },
];

export default new VueRouter({
    mode: 'history',
    routes,
});