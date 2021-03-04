<template>
    <div> {{ test }} </div>
</template>

<script>
import Api from '../Api';

export default {
    data: function () {
        return {
            test: ''
        };
    },

    mounted() {
        Api.search('Some Guy').then((value) => this.test = value);
    },
}
</script>