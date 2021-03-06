import Vue from 'vue';
import VueRouter from 'vue-router';

import Auth from "../components/Auth";
import ProfilePage from "../components/ProfilePage";
import Search from '../components/Search';

Vue.use(VueRouter);

// Doesn't actually send status code 404
const NotFound = {
    template: '<h1> 404 Not Found - {{ $route.path }} </h1>'
};

const routes = [
    { path: '/', redirect: '/login'}, // TODO handle case when already logged in

    { path: '/login',   component: Auth },
    { path: '/profile', component: ProfilePage },
    { path: '/search',  component: Search },
    { path: '*',        component: NotFound },
];

export default new VueRouter({
    mode: 'history',
    routes,
});