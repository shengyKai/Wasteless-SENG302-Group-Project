<template>
    <div>

        <!-- Temporary stand in for search bar component -->
        <v-text-field v-model="searchQuery"></v-text-field>

        <v-alert v-if="error !== undefined" type="error"> {{ error }} </v-alert>
        <v-list v-if="users !== undefined" three-line>
            <template v-for="(user, index) in sortedUsers">
                <v-divider v-if="user === undefined" :key="index"/>
                <v-list-item v-else :key="user.id">
                    <v-list-item-avatar color="primary" class="white--text headline">
                        {{ user.firstName[0].toUpperCase() }}{{ user.lastName[0].toUpperCase() }}
                    </v-list-item-avatar>
                    <v-list-item-content>
                        <v-list-item-title> {{ user.firstName }} {{ user.lastName }} </v-list-item-title>
                        <v-list-item-subtitle> {{ user.email }} </v-list-item-subtitle>
                    </v-list-item-content>
                </v-list-item>
            </template>
        </v-list>
    </div>
</template>

<script>
import Api from '../Api';
import util from '../util';

// TODO Delete this
const MOCK_USERS = [
    {
        id: 0,
        firstName: 'Tim',
        lastName: 'Tam',
        email: 'tim.tam@hotmail.com',
    },
    {
        id: 1,
        firstName: 'Rick',
        lastName: 'Mayo',
        email: 'rick.mayo@hotmail.com',
    },
    {
        id: 2,
        firstName: 'Danny',
        lastName: 'Blast',
        email: 'danny.blast@gmail.com',
    },
    {
        id: 3,
        firstName: 'Barack',
        lastName: 'Obama',
        email: 'barack.obama@gmail.com',
    }
];

function addSeparators(array, separator) {
    let result = [];
    for (let elem of array) {
        result.push(elem);
        result.push(separator);
    }
    result.pop();
    return result;
}


const FIRST_NAME_COMPARATOR = (a, b) => a.firstName.localeCompare(b.firstName);
//const LAST_NAME_COMPARATOR  = (a, b) => a.lastName.localeCompare(b.lastName);

export default {
    data: function () {
        return {
            searchQuery: '',
            users: MOCK_USERS,
            error: undefined,
            comparator: FIRST_NAME_COMPARATOR,
            isSortDescending: false,
        };
    },

    computed: {
        sortedUsers() {
            if (this.users === undefined) return undefined;
            let result = Array.from(this.users).sort(this.comparator);
            if (this.isSortDescending) result.reverse();
            return addSeparators(result, undefined);
        }
    },

    created() {
        this.debouncedDoQuery = util.debounce(() => {
            Api.search(this.searchQuery).then((value) => {
                if (typeof value === 'string') {
                    this.users = undefined;
                    this.error = value;
                } else {
                    this.users = value;
                    this.error = undefined;
                }
            })
        }, 500);
    },

    watch: {
        searchQuery() {
            this.debouncedDoQuery();
        }
    }
}
</script>