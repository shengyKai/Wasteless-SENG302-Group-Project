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
import { is } from 'typescript-is';

const SERVER_URL = process.env.VUE_APP_SERVER_ADD;

const instance = axios.create({
  baseURL: SERVER_URL,
  timeout: 2000,
  withCredentials: true,
});

export type MaybeError<T> = T | string;

export type User = {
  id: number,
  firstName: string,
  lastName: string,
  middleName?: string,
  nickname?: string,
  bio?: string,
  email: string,
  dateOfBirth?: string, // TODO This should actually be a required field (according to the spec)
  phoneNumber?: string,
  homeAddress: Location,
  created?: string,
  role?: "user" | "globalApplicationAdmin" | "defaultGlobalApplicationAdmin",
  businessesAdministered?: Business[],
};

export type Location = {
  streetNumber?: string,
  streetName?: string,
  district?: string,
  city?: string,
  region?: string,
  country: string,
  postcode?: string
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
  primaryAdministratorId: number,
  administrators?: User[],
  name: string,
  description?: string,
  address: Location,
  businessType: BusinessType,
  created?: string,
};

export type CreateBusiness = {
  primaryAdministratorId: number,
  name: string,
  description?: string,
  address: Location,
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
  countryOfSale?: string,
};

export type CreateInventoryItem = {
  productId: string,
  quantity: number,
  pricePerItem?: number,
  totalPrice?: number,
  manufactured?: string,
  sellBy?: string,
  bestBefore?: string,
  expires: string
};

export type CreateProduct = Omit<Product, 'created' | 'images'>;

function isCreateInventoryItem(obj: any): obj is CreateInventoryItem {
  if (obj === null || typeof obj !== 'object') return false;
  if (typeof obj.productId !== 'string') return false;
  if (typeof obj.quantity !== 'number') return false;
  if (obj.pricePerItem !== undefined && typeof obj.pricePerItem !== 'number') return false;
  if (obj.totalPrice !== undefined && typeof obj.totalPrice !== 'number') return false;
  if (obj.manufactured !== undefined && typeof obj.manufactured !== 'string') return false;
  if (obj.sellBy !== undefined && typeof obj.sellBy !== 'string') return false;
  if (obj.bestBefore !== undefined && typeof obj.bestBefore !== 'string') return false;
  if (typeof obj.expires !== 'string') return false;
  return true;
}

type OrderBy = 'userId' | 'relevance' | 'firstName' | 'middleName' | 'lastName' | 'nickname' | 'email';

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

  if (!is<User[]>(response.data)) {
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

  if (!is<number>(response.data.count)) {
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

  if (!is<User>(response.data)) {
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
  if (!is<number>(id)) return 'Invalid response';

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
    await instance.put(`/users/${userId}/makeAdmin`);
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
    await instance.put(`/users/${userId}/revokeAdmin`);
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

    return error.response.data.message;
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
    if (status === 400) return 'Invalid parameters';
    if (status === 409) return 'Product code unavailable';

    return 'Request failed: ' + status;
  }
  return undefined;
}


/**
 * Add a product image to the given product
 *
 * @param businessId The business for which the product belongs
 * @param productCode The product's product code
 * @param file Image file to add
 */
export async function uploadProductImage(businessId: number, productCode: string, file: File): Promise<MaybeError<undefined>> {
  try {
    let formData = new FormData();
    formData.append('file', file);
    await instance.post(`/businesses/${businessId}/products/${productCode}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid image';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Product/Business not found';
    if (status === 413) return 'Image too large';
    return 'Request failed: ' + status;
  }

  return undefined;
}

/**
 * Sets an image as the primary image for a product
 * @param businessId The ID of the business that owns the product
 * @param productId The ID of the product that has the image
 * @param imageId The ID of the image
 */
export async function makeImagePrimary(businessId: number, productId: string, imageId: number) : Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}/products/${productId}/images/${imageId}/makeprimary`);
  } catch ( error ) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Product/Business not found';

    return 'Request failed: ' + status;
  }
  return undefined;
}

/**
 * Deletes an image from a product
 * @param businessId The ID of the business that owns the product
 * @param productId The ID of the product that has the image
 * @param imageId The ID of the image
 */
export async function deleteImage(businessId: number, productId: string, imageId: number) : Promise<MaybeError<undefined>> {
  try {
    await instance.delete(`/businesses/${businessId}/products/${productId}/images/${imageId}`);
  } catch ( error ) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Product/Business not found';

    return 'Request failed: ' + status;
  }
  return undefined;
}


/**
 * Get all products for that business
 * @param businessId
 * @param page
 * @param resultsPerPage
 * @param orderBy
 * @param reverse
 * @return a list of products
 */
export async function getProducts(buisnessId: number, page: number, resultsPerPage: number, orderBy: string, reverse: boolean): Promise<MaybeError<Product[]>> {
  let response;
  try {
    response = await instance.get(`/businesses/${buisnessId}/products`, {
      params: {
        orderBy: orderBy,
        page : page,
        resultsPerPage : resultsPerPage,
        reverse: reverse.toString(),
      }});
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Not an admin of the business';
    if (status === 406) return 'Business not found';
    return 'Request failed: ' + status;
  }

  if (!is<Product[]>(response.data)) {
    return 'Response is not product array';
  }
  return response.data;
}

/**
 * Sends a query for the total number of products in the business
 *
 * @param buisnessId Business id to identify with the database to retrieve the product count
 * @returns Number of products or an error message
 */
export async function getProductCount(buisnessId: number): Promise<MaybeError<number>> {
  let response;
  try {
    response = await instance.get(`/businesses/${buisnessId}/products/count`);
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

  if (!is<Business>(response.data)) {
    return 'Invalid response type';
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
    if (status === 401) return 'Missing/Invalid access token';
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
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Current user cannot perform this action';
    if (status === 406) return 'Business not found';

    return 'Request failed: ' + status;
  }

  return undefined;
}

/**
 * Add an inventory item to the business inventory.
 *
 * @param businessId Business id to identify with the database to add the inventory to the correct business
 * @param inventoryItem The properties to create a inventory with
 */
export async function createInventoryItem(businessId: number, inventoryItem: CreateInventoryItem): Promise<MaybeError<undefined>> {
  try {
    await instance.post(`/businesses/${businessId}/inventory`, inventoryItem);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 403) return 'Operation not permitted';
    console.log(error.response);

    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}
