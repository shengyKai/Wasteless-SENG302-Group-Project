<template>
  <div>
    Hello world
    <div v-for="(event, index) in events" :key="index">
      ID: {{ event.lastEventId}}, Data: {{ event.data }}
    </div>
  </div>
</template>

<script>
import { addEventMessageHandler, initialiseEventSourceForUser } from '@/api/events';

export default {
  name: 'EventDemo',
  data() {
    return {
      events: [],
    };
  },
  created() {
    initialiseEventSourceForUser(this.$store.state.user.id);
    addEventMessageHandler((event) => {
      this.events.push(event);
    });
  },
};
</script>