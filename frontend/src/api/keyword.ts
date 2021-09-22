import {is} from 'typescript-is';
import {MaybeError, instance} from "@/api/internal";

export type Keyword = {
  id: number,
  name: string,
  created: string
};

export type CreateKeyword = {
  name: string
}

/**
 * Retrieves all keywords which match the given query by name.
 * @param query The search term
 * @return A (possibly empty) list of keywords
 */
export async function searchKeywords(query: string): Promise<MaybeError<Keyword[]>> {
  let response;
  try {
    response = await instance.get('/keywords/search', {
      params: {
        searchQuery: query,
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    return 'Request failed: ' + status;
  }
  if (!is<Keyword[]>(response.data)) {
    return "Response is not Keyword array";
  }
  return response.data;
}

/**
 * Creates a new keyword to associate with marketplace cards
 * @param keyword string to set as new marketplace card keyword object
 * @returns keyword id
 */
export async function createNewKeyword(keyword: CreateKeyword): Promise<MaybeError<number>> {
  let response;
  try {
    response = await instance.post(`/keywords`, keyword);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'This keyword already exists or is of invalid format';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    return 'Request failed: ' + error.response?.data.message;
  }
  return response.data.keywordId;
}

/**
 * Deletes a keyword from the keyword list
 * @param keywordId The id of the community marketplace keyword
 */
export async function deleteKeyword(keywordId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.delete(`/keywords/${keywordId}`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Invalid authorization for keyword deletion';
    if (status === 406) return 'Keyword not found';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}