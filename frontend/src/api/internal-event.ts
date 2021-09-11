import {is} from 'typescript-is';
import {MaybeError, SearchResults, instance} from "@/api/internal";
import {EventTag} from "@/api/events";

export type Message = {
  id: number,
  created: string,
  senderId: number,
  content: string,
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
    }
    );
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

/**
 * Adds a message to a conversation about a marketplace card
 * @param cardId The ID of the card
 * @param senderId The ID of the sender of the message
 * @param buyerId The ID of the prospective buyer in the conversation
 * @param message The contents of the message
 */
export async function messageConversation(cardId: number, senderId: number, buyerId: number, message: string): Promise<MaybeError<undefined>> {
  try {
    await instance.post(`/cards/${cardId}/conversations/${buyerId}`, {
      senderId,
      message
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'You do not have permission to edit this conversation';
    if (status === 406) return 'Unable to post message, the card does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Gets a page of messages in a convesation about a marketplace card.
 * @param cardId The ID of the card
 * @param buyerId The ID of the prospective buyer in the conversation
 * @param pageIndex Index of page to start the results from (1 = first page)
 * @param resultsPerPage Number of results to return per page
 * @returns Page of messages within the convesation or else a string error
 */
export async function getMessagesInConversation(cardId: number, buyerId: number, pageIndex: number, resultsPerPage: number): Promise<MaybeError<SearchResults<Message>>> {
  let response;
  try {
    response = await instance.get(`/cards/${cardId}/conversations/${buyerId}`, {
      params: {
        page: pageIndex,
        resultsPerPage,
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'You do not have permission to view this conversation';
    if (status === 406) return 'Unable to get messages, conversation does not exist';
    return error.response?.data.message;
  }

  if (!is<SearchResults<Message>>(response.data)) {
    return "Response is not page of messages";
  }
  return response.data;
}