<template>
  <v-card class="body">
    <div>
      <div class="profile-img">
        <v-avatar size="200px" color="indigo">
          <img src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg"/>
        </v-avatar>
      </div>
      <div class="names">
        <h1>
          {{ user.firstName }} {{ user.lastName }}
        </h1>
        <h2>
          <i>{{ user.nickname }}</i>
        </h2>
        <p><b>Member Since:</b> {{ createdMsg }}</p>
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
import { getUser } from '../api';

export default {
  name: 'ProfilePage',
  
  data() {
      return {
          user: {}
      };
  },

  mounted() {
    let id = this.$route.params.id;
    if (id === undefined || id == this.$store.state.user?.id) {
      this.user = this.$store.state.user;
    } else {
      getUser(id).then((value) => {
        if (typeof value === 'string') {
          // TODO Handle error properly
          console.warn(value);
        } else {
          this.user = value;
        }
      });
    }
  },

  computed: {
    createdMsg() {
      if (this.user.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.user.created);
      const parts = createdAt.toDateString().split(' ');
      
      const diffTime = now - createdAt;
      const diffMonths = Math.ceil(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    }
  }
}
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
