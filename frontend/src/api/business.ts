import {is} from 'typescript-is';
import {Location, MaybeError, SearchResults, instance} from "@/api/internal";
import {User} from "@/api/user";
import { Image } from '@/api/images';

export type BusinessType = 'Accommodation and Food Services' | 'Retail Trade' | 'Charitable organisation' | 'Non-profit organisation';
export const BUSINESS_TYPES: BusinessType[] = ['Accommodation and Food Services', 'Retail Trade', 'Charitable organisation', 'Non-profit organisation'];
type BusinessOrderBy = 'created' | 'name' | 'location' | 'businessType';

export type Business = {
  id: number,
  primaryAdministratorId: number,
  administrators?: User[],
  name: string,
  description?: string,
  address: Location,
  businessType: BusinessType,
  created?: string,
  images?: Image[],
};

export type CreateBusiness = {
  primaryAdministratorId: number,
  name: string,
  description?: string,
  address: Location,
  businessType: BusinessType,
};

export type ModifyBusiness = {
  primaryAdministratorId: number,
  name: string,
  description?: string,
  address: Location,
  businessType: BusinessType,
  updateProductCountry: boolean
}

/**
 * Creates a business
 *
 * @param business The properties to create the business with
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function createBusiness(business: CreateBusiness): Promise<MaybeError<undefined>> {
  try {
    await instance.post('/businesses', business);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';

    return error.response.data.message;
  }

  return undefined;
}

/**
 * Fetches a business with the given id.
 *
 * @param businessId Business id to fetch
 * @returns The requested business if operation is successful, otherwise a string error.
 */
export async function getBusiness(businessId: number): Promise<MaybeError<Business>> {
  let response;
  try {
    response = await instance.get(`/businesses/${businessId}`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 406) return 'Business not found';

    return 'Request failed: ' + status;
  }

  if (!is<Business>(response.data)) {
    return 'Invalid response type';
  }

  return response.data;
}

/**
 * Sends a business search query to the backend. Can specify query, business type or both.
 *
 * @param query Query string to search for
 * @param businessType Business type to filter search results by
 * @param pageIndex Index of page to start the results from (1 = first page)
 * @param resultsPerPage Number of results to return per page
 * @param orderBy Specifies the method used to sort the results
 * @param reverse Specifies whether to reverse the search results (default order is descending for relevance and ascending for all other orders)
 * @returns List of business infos for the current page or an error message
 */
export async function searchBusinesses(query: string | undefined, businessType: BusinessType | undefined, pageIndex: number, resultsPerPage: number, orderBy: BusinessOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<Business>>> {
  let response;
  try {
    response = await instance.get('/businesses/search', {
      params: {
        searchQuery: query,
        businessType: businessType,
        page: pageIndex,
        resultsPerPage,
        orderBy,
        reverse: reverse.toString(),
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid search query: ' + error.response?.data.message;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    return `Request failed: ${status}`;
  }

  if (!is<SearchResults<Business>>(response.data)) {
    return 'Response is not business array';
  }

  return response.data;
}

/**
 * Makes the provided user an administrator of the provided business.
 *
 * @param businessId Business id to add an administrator to
 * @param userId User id to add to the provided business
 * @returns undefined if operation is successful, otherwise a string error message
 */
export async function makeBusinessAdmin(businessId: number, userId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}/makeAdministrator`, {
      userId,
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'User doesn\'t exist or is already an admin';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Current user cannot perform this action';
    if (status === 406) return 'Business not found';

    return 'Request failed: ' + status;
  }

  return undefined;
}

/**
 * Removes the administrator status of a user from a business
 *
 * @param businessId Business id to remove an administrator from
 * @param userId User id to remove from the provided business
 * @returns undefined if operation is successful, otherwise a string error message
 */
export async function removeBusinessAdmin(businessId: number, userId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}/removeAdministrator`, {
      userId,
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'User doesn\'t exist or is not an admin';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Current user cannot perform this action';
    if (status === 406) return 'The new business admin should be at least 16 years old';

    return 'Request failed: ' + status;
  }

  return undefined;
}

/**
 * Modifies a business given a business id and an updated business object
 * @param businessId Id of the business which is to be updated
 * @param business Business object with all the fields to be updated.
 */
export async function modifyBusiness(businessId: number, business: ModifyBusiness): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}`, business);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid details entered: ' + error.response?.data.message;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Invalid authorization for modifying this business';
    if (status === 406) return 'Business does not exist';

    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}