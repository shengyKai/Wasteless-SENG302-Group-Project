/*
 * Created on Wed Feb 10 2021
 *
 * The Unlicense
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
 * this software, either in source code form or as a compiled binary, for any
 * purpose, commercial or non-commercial, and by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this
 * software dedicate any and all copyright interest in the software to the public
 * domain. We make this dedication for the benefit of the public at large and to
 * the detriment of our heirs and successors. We intend this dedication to be an
 * overt act of relinquishment in perpetuity of all present and future rights to
 * this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */

/**
 * Declare all available services here
 */
import axios from 'axios';
const SERVER_URL = process.env.VUE_APP_SERVER_ADD;

const instance = axios.create({
  baseURL: SERVER_URL,
  timeout: 2000,
  withCredentials: true,
});

type MaybeError<T> = T | string;

export type User = {
  id: number,
  firstName: string,
  lastName: string,
  middleName?: string,
  nickname?: string,
  bio?: string,
  email: string,
  dateOfBirth: string,
  phoneNumber?: string,
  homeAddress: Location,
  created?: string,
  role?: "user" | "globalApplicationAdmin" | "defaultGlobalApplicationAdmin",
  businessesAdministered?: Business[],
};

export type Location = {
  streetNumber?: string,
  streetName?: string,
  city?: string,
  region?: string,
  country: string,
  postcode?: string,
};

export type CreateUser = {
  firstName: string,
  lastName: string,
  middleName?: string,
  nickname?: string,
  bio?: string,
  email: string,
  dateOfBirth: string,
  phoneNumber?: string,
  homeAddress: Location,
  password: string,
};

export type BusinessType = 'Accommodation and Food Services' | 'Retail Trade' | 'Charitable organisation' | 'Non-profit organisation';

export const BUSINESS_TYPES: BusinessType[] = ['Accommodation and Food Services', 'Retail Trade', 'Charitable organisation', 'Non-profit organisation'];

export type Business = {
  id: number,
  administrators?: User[],
  name: string,
  description?: string,
  address: string,
  businessType: BusinessType,
  created?: string,
};

export type CreateBusiness = {
  name: string,
  description?: string,
  address: string,
  businessType: BusinessType,
};

export type Image = {
  id: number,
  filename: string,
  thumbnailFilename: string,
};

export type Product = {
  id: string,
  name: string,
  description?: string,
  manufacturer?: string,
  recommendedRetailPrice?: number,
  created?: string,
  images: Image[],
};

export type CreateProduct = Omit<Product, 'created' | 'images'>;

function isLocation(obj: any): obj is Location {
  if (obj === null || typeof obj !== 'object') return false;
  if (obj.streetNumber !== undefined && typeof obj.streetNumber !== 'string') return false;
  if (obj.streetName !== undefined && typeof obj.streetName !== 'string') return false;
  if (obj.city !== undefined && typeof obj.city !== 'string') return false;
  if (obj.region !== undefined && typeof obj.region !== 'string') return false;
  if (typeof obj.country !== 'string') return false;
  if (obj.postcode !== undefined && typeof obj.postcode !== 'string') return false;

  return true;
}

function isUser(obj: any): obj is User {
  if (obj === null || typeof obj !== 'object') return false;
  if (typeof obj.id !== 'number') return false;
  if (typeof obj.firstName !== 'string') return false;
  if (typeof obj.lastName !== 'string') return false;
  if (obj.middleName !== undefined && typeof obj.middleName !== 'string') return false;
  if (obj.nickname !== undefined && typeof obj.nickname !== 'string') return false;
  if (obj.bio !== undefined && typeof obj.bio !== 'string') return false;
  if (typeof obj.email !== 'string') return false;
  if (typeof obj.dateOfBirth !== 'string') return false;
  if (obj.phoneNumber !== undefined && typeof obj.phoneNumber !== 'string') return false;
  if (!isLocation(obj.homeAddress)) return false;
  if (obj.created !== undefined && typeof obj.created !== 'string') return false;
  if (obj.role !== undefined && !['user', 'globalApplicationAdmin', 'defaultGlobalApplicationAdmin'].includes(obj.role))
    return false;
  if (obj.businessesAdministered !== undefined && !isBusinessArray(obj.businessesAdministered)) return false;

  return true;
}

function isBusiness(obj: any): obj is Business {
  if (obj === null || typeof obj !== 'object') return false;
  if (typeof obj.id !== 'number') return false;
  if (obj.administrators !== undefined && !isUserArray(obj.administrators)) return false;
  if (typeof obj.name !== 'string') return false;
  if (obj.description !== undefined && typeof obj.description !== 'string') return false;
  if (typeof obj.address !== 'object') return false;
  if (!BUSINESS_TYPES.includes(obj.businessType)) return false;
  if (obj.created !== undefined && typeof obj.created !== 'string') return false;

  return true;
}

function isNumberArray(obj: any): obj is number[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (typeof elem !== 'number') return false;
  }
  return true;
}

function isUserArray(obj: any): obj is User[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (!isUser(elem)) return false;
  }
  return true;
}

function isBusinessArray(obj: any): obj is Business[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (!isBusiness(elem)) return false;
  }
  return true;
}

type OrderBy = 'userId' | 'relevance' | 'firstName' | 'middleName' | 'lastName' | 'nickname' | 'email' | 'address';

/**
 * Sends a search query to the backend.
 *
 * @param query Query string to search for
 * @param pageIndex Index of page to start the results from (1 = first page)
 * @param resultsPerPage Number of results to return per page
 * @param orderBy Specifies the method used to sort the results
 * @param reverse Specifies whether to reverse the search results (default order is descending for relevance and ascending for all other orders)
 * @returns List of user infos for the current page or an error message
 */
export async function search(query: string, pageIndex: number, resultsPerPage: number, orderBy: OrderBy, reverse: boolean): Promise<MaybeError<User[]>> {
  let response;
  try {
    response = await instance.get('/users/search', {
      params: {
        searchQuery: query,
        page: pageIndex,
        resultsPerPage,
        orderBy,
        reverse: reverse.toString(),
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${status}`;
  }

  if (!isUserArray(response.data)) {
    return 'Response is not user array';
  }

  return response.data;
}

/**
 * Sends a query for the number of search results for a given query to the backend.
 *
 * @param query Query string to search for
 * @returns Number of search results or an error message
 */
export async function getSearchCount(query: string): Promise<MaybeError<number>> {
  let response;
  try {
    response = await instance.get('/users/search/count', {
      params: {
        searchQuery: query,
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${status}`;
  }

  if (typeof response.data?.count !== 'number') {
    return 'Response is not number';
  }

  return response.data.count;
}

/**
 * Queries the backend for a specific user by their id.
 *
 * @param id User id, if ommitted then fetch the logged in user's info
 * @returns User info for the given id or an error message
 */
export async function getUser(id: number): Promise<MaybeError<User>> {
  let response;
  try {
    response = await instance.get('/users/' + id);
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${status}`;
  }

  if (!isUser(response.data)) {
    return 'Response is not user';
  }

  return response.data;
}

/**
 * Logs in to the given user account by setting the authentication cookie.
 *
 * @param email User email
 * @param password User password
 * @returns The now logged in user ID if operation is successful, otherwise a string error.
 */
export async function login(email: string, password: string): Promise<MaybeError<number>> {
  let response;
  try {
    response = await instance.post('/login', {
      email: email,
      password: password,
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid credentials';
    return `Request failed: ${status}`;
  }
  let id = response.data.userId;
  if (typeof id !== 'number') return 'Invalid response';

  return id;
}

/**
 * Creates a user with the given properties.
 * Note: This doesn't automatically log the user in.
 *
 * @param user Initial user properties
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function createUser(user: CreateUser): Promise<MaybeError<undefined>> {
  try {
    await instance.post('/users', user);
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid create user request';
    if (status === 409) return 'Email in use';
    return 'Request failed: ' + status;
  }

  return undefined;
}

/**
 * Makes the given user a GAA, if the current user is a DGAA.
 *
 * @param userId User to make GAA
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function makeAdmin(userId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.post(`/users/${userId}/makeAdmin`);
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'User does not exist';

    return 'Request failed: ' + status;
  }
  return undefined;
}

/**
 * Revokes the given user's admin rights
 *
 * @param userId User to revoke permissions
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function revokeAdmin(userId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.post(`/users/${userId}/revokeAdmin`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'User does not exist';

    return 'Request failed: ' + status;
  }
  return undefined;
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
    if (status === 401) return 'Missing/Invalid access token';

    return 'Request failed: ' + status;
  }

  return undefined;
}

/**
 * Add a product to a businesses catalogue.
 *
 * @param businessId The business to add the product to
 * @param product The properties to create a product with
 * @return undefined if operation is successful, otherwise a string error
 */
export async function createProduct(businessId: number, product: CreateProduct): Promise<MaybeError<undefined>> {
  try {
    await  instance.post(`/businesses/${businessId}/products`, product);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';

    if (status === 400) {
      // TODO Not sure exactly how the backend is going to communicate with us that the product code
      // is unavailable.

      // eslint-disable-next-line no-constant-condition
      if (false) {
        return 'Product code unavailable';
      }

      return 'Invalid parameters';
    }

    return 'Request failed: ' + status;
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
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 406) return 'Business not found';

    return 'Request failed: ' + status;
  }

  if (!isBusiness(response.data)) {
    return 'Invalid response type';
  }

  return response.data;
}