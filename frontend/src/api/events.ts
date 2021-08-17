import { Keyword, MarketplaceCard, MarketplaceCardSection, Message, User } from "./internal";

const EMITTER_URL = process.env.VUE_APP_SERVER_ADD + '/events/emitter';

let eventSource: EventSource;
let lastErrorTime = Number.MIN_VALUE;

export type AnyEvent = GlobalMessageEvent | ExpiryEvent | DeleteEvent | KeywordCreatedEvent | MessageEvent;

export type Tag = 'none' | 'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple'

type BaseEvent<T extends string> = {
  id: number,
  created: string,
  tag: Tag,
  type: T,
}

export type GlobalMessageEvent = BaseEvent<'GlobalMessageEvent'> & {
  message: string
}

export type ExpiryEvent = BaseEvent<'ExpiryEvent'> & {
  card: MarketplaceCard
}

export type DeleteEvent = BaseEvent<'DeleteEvent'> & {
  title: string,
  section: MarketplaceCardSection
}

export type KeywordCreatedEvent = BaseEvent<'KeywordCreatedEvent'> & {
  keyword: Keyword,
  creator: User
}
export type MessageEvent = BaseEvent<'MessageEvent'> & {
  buyer: User,
  card: MarketplaceCard,
  participantType: string,
  message: Message,
}

/**
 * Starts the global event listener.
 * This method is expected to be called immediately after the user is logged in, so that they can receive their newsfeed items
 * @param userId user to try listening events from
 */
export function initialiseEventSourceForUser(userId: number): void {
  eventSource?.close();

  eventSource = new EventSource(EMITTER_URL + "?userId=" + encodeURIComponent(userId), {
    withCredentials: true,
  });
  eventSource.addEventListener("error", (event) => {
    if (eventSource.readyState === EventSource.CLOSED) {
      return;
    }

    let errorTime = Date.now();
    if (lastErrorTime + 10 > errorTime) { // Been less than 10s since last error
      console.error("Error occured", event);
      eventSource.close(); // Give up
    }
  });
}

/**
 * Add a handler for whenever a newsfeed event arrives
 * @param handler Event handler
 */
export function addEventMessageHandler(handler: (event: AnyEvent) => void): void {
  eventSource.addEventListener('newsfeed' as any, (event) => handler(JSON.parse(event.data)));
}