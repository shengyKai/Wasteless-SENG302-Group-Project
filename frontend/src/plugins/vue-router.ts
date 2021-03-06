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

    { path: '/profile/:id', component: ProfilePage },
    { path: '/profile', component: ProfilePage },
    
    { path: '/search',  component: SearchResults },
    { path: '*',        component: NotFound },
];

export default new VueRouter({
    mode: 'history',
    routes,
});