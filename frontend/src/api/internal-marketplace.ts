import {is} from 'typescript-is';
import {User} from "@/api/internal-user";
import {MaybeError, SearchResults, instance} from "@/api/internal";
import {Keyword} from "@/api/internal-keyword";

export type MarketplaceCardSection = 'ForSale' | 'Wanted' | 'Exchange';
type CardOrderBy = 'created' | 'title' | 'closes' | 'creatorFirstName' | 'creatorLastName';

export type ModifyMarketplaceCard = {
  section: MarketplaceCardSection,
  title: string,
  description?: string,
  keywordIds: number[],
};

export type CreateMarketplaceCard = ModifyMarketplaceCard & {
  creatorId: number,
};

export type MarketplaceCard = {
  id: number,
  creator: User,
  section: MarketplaceCardSection,
  created: string,
  lastRenewed: string,
  displayPeriodEnd?: string,
  title: string,
  description?: string,
  keywords: Keyword[]
};

/**
 * Create a card for the community marketplace
 *
 * @param marketplaceCard The attributes to use when creating the marketplace card
 * @return id of card if card is successfully created, an error string otherwise
 */
export async function createMarketplaceCard(marketplaceCard: CreateMarketplaceCard): Promise<MaybeError<number>> {
  let response;
  try {
    response = await instance.post('/cards', marketplaceCard);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Incorrect marketplace card format: ' + error.response?.data.message;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'A user cannot create a marketplace card for another user';

    return 'Request failed: ' + error.response?.data.message;
  }
  if (!is<number>(response.data.cardId)) {
    return 'Invalid response format';
  }
  return response.data.cardId;
}

export async function modifyMarketplaceCard(cardId: number, marketplaceCard: ModifyMarketplaceCard) {
  try {
    await instance.put(`/cards/${cardId}`, marketplaceCard);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Marketplace card not found';
    if (status === 400) return 'Invalid parameters: ' + error.response?.data.message;

    return 'Request failed: ' + status;
  }
  return undefined;
}

/**
 * Fetches a page of cards by section in the marketplace
 * @param section The ID of the business
 * @param page Page to fetch (1 indexed)
 * @param resultsPerPage Maximum number of results per page
 * @param orderBy Parameter to order the results by
 * @param reverse Whether to reverse the results (default ascending)
 * @returns List of marketplace cards and the count or a string error message
 */
export async function getMarketplaceCardsBySection(section: MarketplaceCardSection, page: number, resultsPerPage: number, orderBy: CardOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<MarketplaceCard>>> {
  let response;
  try {
    response = await instance.get(`/cards`, {
      params: {
        section,
        page,
        resultsPerPage,
        orderBy,
        reverse
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'The given section does not exist';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    return 'Request failed: ' + status;
  }
  if (!is<SearchResults<MarketplaceCard>>(response.data)) {
    return "Response is not card array";
  }
  return response.data;
}

/**
 * Fetches a page of cards by section in the marketplace
 * @param keywordIds The list of keyword IDs to match
 * @param section The section to search in
 * @param union Whether or not to match ANY keyword or ALL keywords (true = ANY)
 * @param page Page to fetch (1 indexed)
 * @param resultsPerPage Maximum number of results per page
 * @param orderBy Parameter to order the results by
 * @param reverse Whether to reverse the results (default ascending)
 * @returns List of marketplace cards and the count or a string error message
 */
export async function getMarketplaceCardsBySectionAndKeywords(keywordIds: number[], section: MarketplaceCardSection, union: boolean, page: number, resultsPerPage: number, orderBy: CardOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<MarketplaceCard>>> {
  let response;
  try {
    const params = new URLSearchParams();
    for (let id of keywordIds) {
      params.append("keywordIds", id.toString());
    }
    params.append('section', section);
    params.append('union', union.toString());
    params.append('page', page.toString());
    params.append('resultsPerPage', resultsPerPage.toString());
    params.append('orderBy', orderBy);
    params.append('reverse', reverse.toString());

    response = await instance.get(`/cards/search`, {
      params: params
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'The given section does not exist';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    return 'Request failed: ' + error.response?.data.message;
  }
  if (!is<SearchResults<MarketplaceCard>>(response.data)) {
    return "Response is not card array";
  }
  return response.data;
}

/**
 * Deletes a card from the community marketplace
 * @param marketplaceCardId The id of the community marketplace card
 */
export async function deleteMarketplaceCard(marketplaceCardId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.delete(`/cards/${marketplaceCardId}`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Invalid authorization for card deletion';
    if (status === 406) return 'Marketplace card not found';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Extends a marketplace card expiry date such that the card can be displayed for another two weeks
 * @param marketplaceCardId The id of the community marketplace card
 */
export async function extendMarketplaceCardExpiry(marketplaceCardId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/cards/${marketplaceCardId}/extenddisplayperiod`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Invalid authorization for card expiry extension';
    if (status === 406) return 'Marketplace card not found';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Retrieves all the marketplace cards that are created by a user.
 * @param userId id of the requested user to identify the cards
 * @param resultsPerPage Maximum number of results per page
 * @param page Page to fetch (1 indexed)
 * @returns List of marketplace cards and the count or a string error message
 */
export async function getMarketplaceCardsByUser(userId: number, resultsPerPage: number, page: number): Promise<MaybeError<SearchResults<MarketplaceCard>>> {
  let response;
  try {
    response = await instance.get(`/users/${userId}/cards`, {
      params: {
        userId,
        page,
        resultsPerPage
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'The page does not exist';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 406) return 'The user does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
  if (!is<SearchResults<MarketplaceCard>>(response.data)) {
    return "Response is not card array";
  }
  return response.data;
}