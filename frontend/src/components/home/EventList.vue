<template>
  <div>
    <v-card
      v-for="event in events"
      :key="event.id"
      outlined
      rounded="lg"
      class="newsfeed-item"
    >
      <GlobalMessage v-if="event.type === 'GlobalMessageEvent'" :event="event"/>
      <ExpiryEvent v-else-if="event.type === 'ExpiryEvent'" :event="event"/>
      <DeleteEvent v-else-if="event.type === 'DeleteEvent'" :event="event"/>
      <KeywordCreated v-else-if="event.type === 'KeywordCreatedEvent'" :event="event"/>
      <MessageEvent v-else-if="event.type === 'MessageEvent'" :event="event"/>
      <template v-else>
        <v-card-title>
          {{ event.type }}
        </v-card-title>
        <v-card-text>
          <pre>{{ event }}</pre>
        </v-card-text>
      </template>
    </v-card>
  </div>
</template>
<script>
import DeleteEvent from './newsfeed/DeleteEvent.vue';
import ExpiryEvent from './newsfeed/ExpiryEvent.vue';
import GlobalMessage from "./newsfeed/GlobalMessage.vue";
import KeywordCreated from './newsfeed/KeywordCreated.vue';
import MessageEvent from './newsfeed/MessageEvent.vue';

export default {
  name: 'EventList',
  components: {DeleteEvent, ExpiryEvent, GlobalMessage, KeywordCreated, MessageEvent},
  props: {
    events: []
  }
};
</script>
<style scoped>

pre {
  white-space: pre-wrap;
}

@media all and (min-width: 992px) {
}

@media not all and (min-width: 992px) {
}

.newsfeed-item {
  margin-bottom: 10px;
}


@media not all and (min-width: 992px) {
}

</style>