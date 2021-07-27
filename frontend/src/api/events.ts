import { Keyword, MarketplaceCard, MarketplaceCardSection, User } from "./internal";

const EMITTER_URL = process.env.VUE_APP_SERVER_ADD + '/events/emitter';

let eventSource: EventSource;
let lastErrorTime = Number.MIN_VALUE;

export type AnyEvent = MessageEvent | ExpiryEvent | DeleteEvent | KeywordCreatedEvent;

type BaseEvent<T extends string> = {
  id: number,
  created: string,
  type: T,
}

type MessageEvent = BaseEvent<'MessageEvent'> & {
  message: string
}

type ExpiryEvent = BaseEvent<'ExpiryEvent'> & {
  card: MarketplaceCard
}

type DeleteEvent = BaseEvent<'DeleteEvent'> & {
  title: string,
  section: MarketplaceCardSection
}

type KeywordCreatedEvent = BaseEvent<'KeywordCreatedEvent'> & {
  keyword: Keyword,
  creator: User
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