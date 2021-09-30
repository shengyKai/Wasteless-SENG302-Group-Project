<template>
  <v-dialog
    ref="dialog"
    v-model="showDatePicker"
    width="300px"
    persistent
    :return-value.sync="date"
  >
    <template v-slot:activator="{ on, attrs }">
      <v-text-field
        :label="label"
        clearable
        prepend-inner-icon="mdi-calendar"
        readonly
        v-bind="attrs"
        v-on="on"
        outlined
        v-model="date"
      />
    </template>
    <v-date-picker scrollable v-model="date" :min="minDate" :max="maxDate">
      <v-spacer />
      <v-btn text color="primary" @click="showDatePicker=false">
        Cancel
      </v-btn>
      <v-btn
        text
        color="primary"
        @click="$refs.dialog.save(date)"
      >
        OK
      </v-btn>
    </v-date-picker>
  </v-dialog>
</template>

<script>
export default {
  name: "DatePickerDialog",
  data() {
    return {
      showDatePicker: false,
      newDate: undefined,
    };
  },
  props: {
    value: undefined,
    label: String,
    minDate: String,
    maxDate: String
  },
  computed: {
    date: {
      get() {
        return this.value;
      },
      set (value) {
        this.$emit('input', value);
      },
    },
  }
};

</script>
