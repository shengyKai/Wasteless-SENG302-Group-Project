<template>
    <v-card class="body">
        <div>
            <div class="profile-img">
                <v-avatar size="200px" color="indigo">
                    <img
                        src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg"
                    />
                </v-avatar>
            </div>
            <div class="names">
                <h1>{{ user.firstName }} {{ user.lastName }}</h1>
                <h2>
                    <i>
                        {{ user.nickname }}
                    </i>
                </h2>
            </div>
        </div>

        <div class="fields">
            <v-simple-table>
                <tbody>
                    <tr>
                        <td>
                            Email
                        </td>
                        <td>
                            {{ user.email }}
                        </td>
                        <td>
                            Date of Birth
                        </td>
                        <td>
                            {{ user.dateOfBirth }}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Phone Number
                        </td>
                        <td>
                            {{ user.phoneNumber }}
                        </td>
                        <td>
                            Home Address
                        </td>
                        <td>
                            {{ user.homeAddress }}
                        </td>
                    </tr>
                </tbody>
            </v-simple-table>
        </div>

        <div>
            {{ user.bio }}
        </div>
    </v-card>
</template>

<script>
export default {
    name: "ProfilePage",
    computed: {
        user() {
            return this.$store.state.user;
        },
    },
};
</script>

<style scoped>
.profile-img {
    float: left;
    margin-top: -116px;
    margin-right: 16px;
}

.body {
    padding: 16px;
    width: 100%;
    margin-top: 140px;
}

.names {
    margin-left: auto;
    margin-right: auto;
}

.fields {
    clear: both;
}

td:nth-child(1),
td:nth-child(3) {
    font-weight: bold;
    text-align: right;
}

tbody tr:hover {
    background-color: transparent !important;
}
</style>
