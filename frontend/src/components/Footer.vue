<template>
    <div class="foot">
        <v-divider/>
        <v-row justify="center" align="center" no-gutters>
            <div class="item">© 2021 - BENK</div>
            <div class="item clickable" @click=about>About</div>
            <div class="item clickable" @click=contact>Contact</div>
        </v-row>
    </div>
</template>

<script>
export default {
  name: "footer",

  methods: {
      about() {
          alert('TODO');
      },
      contact() {
          alert('TODO');
      }
  }
};
</script>

<style scoped >

.item {
    padding: 8px;
    user-select: none;
}

.clickable {
    cursor: pointer;
}

</style>