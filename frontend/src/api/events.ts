import {MaybeError} from "./internal";
import axios from 'axios';
import {User} from "@/api/user";
import {MarketplaceCard, MarketplaceCardSection, Message} from "@/api/marketplace";
import {Keyword} from "@/api/keyword";
import {BoughtSale, Sale} from "@/api/sale";
import {Product} from "@/api/product";
import {is} from 'typescript-is';

const SERVER_URL = process.env.VUE_APP_SERVER_ADD;

const instance = axios.create({
  baseURL: SERVER_URL,
  timeout: 5 * 1000,
  withCredentials: true,
});

export type AnyEvent = GlobalMessageEvent | ExpiryEvent | DeleteEvent | KeywordCreatedEvent | MessageEvent | InterestEvent | PurchasedEvent | InterestPurchasedEvent;

export type EventTag = 'none' | 'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple'
export type EventStatus = 'normal' | 'starred' | 'archived'

type BaseEvent<T extends string> = {
  id: number,
  created: string,
  type: T,
  tag: EventTag,
  status: EventStatus,
  read: boolean,
  lastModified: string
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
export type InterestEvent = BaseEvent<'InterestEvent'> & {
  saleItem: Sale,
  interested: boolean,
}

export type PurchasedEvent = BaseEvent<'PurchasedEvent'> & {
  boughtSaleItem: {
    id: number,
    buyer: User,
    product: Product
    interestCount: number,
    price: number,
    quantity: number,
    saleDate: string,
    listingDate: string,
  }
}

export type InterestPurchasedEvent = BaseEvent<'InterestPurchasedEvent'> & {
  boughtSaleItem: BoughtSale,
}

/**
 * Updates an event as read. No body is needed in this case as the backend would only have to change
 * a boolean value, and subsequently, the event emitters will retrieve the events with the updated field.
 * @param eventId Event id of the event to mark as read
 */
export async function updateEventAsRead(eventId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/feed/${eventId}/read`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return "Invalid authorization for marking this event";
    if (status === 406) return 'Event does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Updates an event to a selected status with overwriting strategy. Overwriting the event's status
 * Archive is a one way action, no UI component available for overwriting archived event
 * @param eventId Event id of the event to be overwriting
 * @param eventStatus  The desire status to be updated for the event
 */
export async function updateEventStatus(eventId: number, eventStatus: EventStatus): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/feed/${eventId}/status`, {
      value: eventStatus
    }
    );
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return "Invalid authorization for modifying event status";
    if (status === 406) return 'Event does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}
/**
 * Retrieve events for the newsfeed of the user with the given id. If a modifedSince timestamp is provided,
 * only events that have been after this timestamp will be retrieved. If no timestamp is provided, all events
 * associated with the user will be retrieved.
 * @param userId The ID number of the user to retrieve newsfeed events for.
 * @param modifiedSince A datetime string in UTC format. If this string is provided, only events modified after
 * this timestamp will be retrieved.
 * @returns An array of events to be displayed in the user's newsfeed.
 */
export async function getEvents(userId: number, modifiedSince: string | undefined): Promise<MaybeError<AnyEvent[]>> {
  const params = new URLSearchParams();
  if (modifiedSince) {
    params.append("modifiedSince", modifiedSince);
  }
  try {
    const response = await instance.get(`/users/${userId}/feed`, {
      params: params
    });
    if (!is<AnyEvent[]>(response.data)) {
      return 'Response is not an event array';
    }
    return response.data;
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid \'modified since\' date';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return `Invalid authorisation for getting events associated with user ${userId}`;
    if (status === 406) return 'User does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
}

/**
 * Deletes a notification from your feed
 * @param eventId The id of the notification to be deleted
 */
export async function deleteNotification(eventId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.delete(`/feed/${eventId}`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Invalid authorization for notification removal';
    // If the notification is not found on the backend, respond the same way as if it was successfully deleted.
    if (status === 406) return undefined;
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Tag an event with a coloured tag
 * @param eventId The ID of the event
 * @param colour  The colour of the tag user wan to set
 */
export async function setEventTag(eventId: number, colour: EventTag): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/feed/${eventId}/tag`, {
      value: colour
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Invalid authorization for Event tagging';
    if (status === 406) return 'Event not found';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}