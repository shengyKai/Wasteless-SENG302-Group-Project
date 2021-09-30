<template>
  <div class="foot">
    <v-divider/>
    <v-row justify="center" align="center" no-gutters>
      <v-icon x-large color="primary" class="body">
        mdi-sheep
      </v-icon>
      <div class="item">© 2021 - Team 'Internal Error'</div>
      <router-link class="item link" to="/about">About</router-link>
      <router-link class="item link" to="/contact">Contact</router-link>
      <v-icon x-large color="primary" class="body">
        mdi-sheep
      </v-icon>
    </v-row>
  </div>
</template>

<style scoped>

.item {
  padding: 8px;
  user-select: none;
}

.link {
  color: black;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.body {
  -webkit-animation:name 1s infinite;
}

@-webkit-keyframes name {
    0% {background-color:purple;}
    100% {background-color:rgb(150, 55, 55);}
}
</style>