import { Keyword, MarketplaceCard, MarketplaceCardSection, MaybeError, Message, User } from "./internal";
import axios from 'axios';

const SERVER_URL = process.env.VUE_APP_SERVER_ADD;

const instance = axios.create({
  baseURL: SERVER_URL,
  timeout: 5 * 1000,
  withCredentials: true,
});

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
  // After implementation on the backend, the bottom line should be uncommented to add the bottom attribute.
  // May not be the exact same name, but can be changed
  // isRead: boolean
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
  message: Message,
  participantType: 'buyer' | 'seller',
  conversation: {
    buyer: User,
    card: MarketplaceCard,
    id: number,
  },
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

/**
 * Updates an event as read. No body is needed in this case as the backend would only have to change
 * a boolean value, and subsequently, the event emitters will retrieve the events with the updated field.
 * @param eventId Event id
 */
export async function updateEventAsRead(eventId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/users/${eventId}`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Cannot update event as read: ' + error.response?.data.message;
    if (status === 406) return 'Event does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}