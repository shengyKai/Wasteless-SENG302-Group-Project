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
import { Tag } from './events';

const SERVER_URL = process.env.VUE_APP_SERVER_ADD;

const instance = axios.create({
  baseURL: SERVER_URL,
  timeout: 5 * 1000,
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

export type CreateSaleItem = {
  inventoryItemId: number,
  quantity: number,
  price: number,
  moreInfo?: string,
  closes?: string,
};

export type Sale = {
  id: number,
  inventoryItem: InventoryItem,
  quantity: number,
  price: number,
  moreInfo?: string,
  created: string,
  closes?: string,
};

export type InventoryItem = {
  id: number,
  product: Product,
  quantity: number,
  remainingQuantity: number,
  pricePerItem?: number,
  totalPrice?: number,
  manufactured?: string,
  sellBy?: string,
  bestBefore?: string,
  expires: string
};

export type MarketplaceCardSection = 'ForSale' | 'Wanted' | 'Exchange'

export type ModifyMarketplaceCard = {
  section: MarketplaceCardSection,
  title: string,
  description?: string,
  keywordIds: number[],
}

export type CreateMarketplaceCard = ModifyMarketplaceCard & {
  creatorId: number,
};

export type Keyword = {
  id: number,
  name: string,
  created: string
};

export type CreateKeyword = {
  name: string
}

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
}

export type CreateProduct = Omit<Product, 'created' | 'images'>;

type UserOrderBy = 'userId' | 'relevance' | 'firstName' | 'middleName' | 'lastName' | 'nickname' | 'email';
type BusinessOrderBy = 'created' | 'name' | 'location' | 'businessType';

export type SearchResults<T> = { results: T[], count: number }

export type ProductSearchBy = 'name' | 'description' | 'manufacturer' | 'product code';

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
export async function search(query: string, pageIndex: number, resultsPerPage: number, orderBy: UserOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<User>>> {
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
    return `Request failed: ${error.response.data.message}`;
  }

  if (!is<SearchResults<User>>(response.data)) {
    return 'Response is not user array';
  }

  return response.data;
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
    if (status === 401) return 'You have been logged out. Please login again and retry';
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
    if (status === 401) return 'You have been logged out. Please login again and retry';
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
    if (status === 401) return 'You have been logged out. Please login again and retry';

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
    await instance.post(`/businesses/${businessId}/products`, product);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 400) return 'Invalid parameters';
    if (status === 409) return 'Product code unavailable';

    return 'Request failed: ' + status;
  }
  return undefined;
}

/**
 * Updates an existing product's properties
 *
 * @param businessId The business for which the product belongs
 * @param productCode The product's product code
 * @param product The product's new properties
 * @return undefined if operation is successful, otherwise a string error
 */
export async function modifyProduct(businessId: number, productCode: string, product: CreateProduct): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}/products/${productCode}`, product);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Product/Business not found';
    if (status === 400) return 'Invalid parameters';
    if (status === 409) return 'Product code unavailable';

    return 'Request failed: ' + status;
  }
  return undefined;
}

/**
 * Adds a sale item to the business sales listing
 *
 * @param businessId Business id to identify with the database to add the sales item to the correct business
 * @param saleItem The properties to create a sales item with
 */
export async function createSaleItem(businessId: number, saleItem: CreateSaleItem): Promise<MaybeError<undefined>> {
  try {
    const response = await instance.post(`/businesses/${businessId}/listings`, saleItem);
    return response.data;
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid data with the Sale Item';
    if (status === 403) return 'Operation not permitted';

    return 'Request failed: ' + status;
  }
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
    if (status === 401) return 'You have been logged out. Please login again and retry';
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
export async function makeImagePrimary(businessId: number, productId: string, imageId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}/products/${productId}/images/${imageId}/makeprimary`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
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
export async function deleteImage(businessId: number, productId: string, imageId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.delete(`/businesses/${businessId}/products/${productId}/images/${imageId}`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Product/Business not found';

    return 'Request failed: ' + status;
  }
  return undefined;
}

type ProductOrderBy = 'name' | 'description' | 'manufacturer' | 'recommendedRetailPrice' | 'created' | 'productCode'

/**
 * Get all products for that business
 * @param businessId
 * @param page
 * @param resultsPerPage
 * @param orderBy
 * @param reverse
 * @return a list of products
 */
export async function getProducts(businessId: number, page: number, resultsPerPage: number, orderBy: ProductOrderBy, reverse: boolean):
  Promise<MaybeError<SearchResults<Product>>>{
  let response;
  try {
    response = await instance.get(`/businesses/${businessId}/products`, {
      params: {
        orderBy: orderBy,
        page: page,
        resultsPerPage: resultsPerPage,
        reverse: reverse.toString(),
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Not an admin of the business';
    if (status === 406) return 'Business not found';
    return 'Request failed: ' + status;
  }

  if (!is<SearchResults<Product>>(response.data)) {
    return 'Response is not product array';
  }
  return response.data;
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
    if (status === 406) return 'Business not found';

    return 'Request failed: ' + status;
  }

  return undefined;
}

type SalesOrderBy = 'created' | 'closing' | 'productCode' | 'productName' | 'quantity' | 'price'

/**
 * Fetches a page of sale listings for the given business
 * @param businessId The ID of the business
 * @param page Page to fetch (1 indexed)
 * @param resultsPerPage Maximum number of results per page
 * @param orderBy Parameter to order the results by
 * @param reverse Whether to reverse the results (default ascending)
 * @returns List of sales or a string error message
 */
export async function getBusinessSales(businessId: number, page: number, resultsPerPage: number, orderBy: SalesOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<Sale>>> {
  let response;
  try {
    response = await instance.get(`/businesses/${businessId}/listings`, {
      params: {
        orderBy,
        page,
        resultsPerPage,
        reverse,
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 406) return 'The given business does not exist';
    return 'Request failed: ' + status;
  }
  if (!is<SearchResults<Sale>>(response.data)) {
    return "Response is not Sale array";
  }
  return response.data;
}

type InventoryOrderBy = 'name' | 'description' | 'manufacturer' | 'recommendedRetailPrice' | 'created' | 'quantity' | 'pricePerItem' | 'totalPrice' | 'manufactured' | 'sellBy' | 'bestBefore' | 'expires' | 'productCode'

/**
 * Get all inventory items for that business
 *
 * @param businessId
 * @param page
 * @param resultsPerPage
 * @param orderBy
 * @param reverse
 * @return a list of inventory items
 */
export async function getInventory(businessId: number, page: number, resultsPerPage: number, orderBy: InventoryOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<InventoryItem>>> {
  let response;
  try {
    response = await instance.get(`/businesses/${businessId}/inventory`, {
      params: {
        orderBy: orderBy,
        page: page,
        resultsPerPage: resultsPerPage,
        reverse: reverse.toString(),
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Not an admin of the business';
    if (status === 406) return 'Business not found';
    return 'Request failed: ' + status;
  }

  if (!is<SearchResults<InventoryItem>>(response.data)) {
    return 'Response is not inventory array';
  }
  return response.data;
}

/**
 * Updates an existing inventory item's properties
 *
 * @param businessId The business for which the inventory item belongs
 * @param inventoryItemId The id number of the inventory item
 * @param inventoryItem The inventory item's new properties
 * @return undefined if operation is successful, otherwise a string error
 */
export async function modifyInventoryItem(businessId: number, inventoryItemId: number, inventoryItem: CreateInventoryItem): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/businesses/${businessId}/inventory/${inventoryItemId}`, inventoryItem);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'Missing/Invalid access token';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Inventory item/Business not found';
    if (status === 400) return 'Invalid parameters: ' + error.response?.data.message;

    return 'Request failed: ' + error.response?.status;
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

    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Create a card for the community marketplace
 *
 * @param marketplaceCard The attributes to use when creating the marketplace card
 * @return id of card if card is successfully created, an error string otherwise
 */
export async function createMarketplaceCard(marketplaceCard: CreateMarketplaceCard) : Promise<MaybeError<Number>> {
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

type CardOrderBy = 'created' | 'title' | 'closes' | 'creatorFirstName' | 'creatorLastName'

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
export async function deleteMarketplaceCard(marketplaceCardId: number) : Promise<MaybeError<undefined>> {
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
export async function extendMarketplaceCardExpiry(marketplaceCardId: number) : Promise<MaybeError<undefined>> {
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

/**
 * Retrieves all keywords which match the given query by name.
 * @param query The search term
 * @return A (possibly empty) list of keywords
 */
export async function searchKeywords(query: string) : Promise<MaybeError<Keyword[]>> {
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
export async function createNewKeyword(keyword: CreateKeyword) : Promise<MaybeError<number>> {
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
 * @param keyword The id of the community marketplace keyword
 */
export async function deleteKeyword(keywordId: number) : Promise<MaybeError<undefined>> {
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

/**
 * Deletes a notification from your feed
 * @param eventId The id of the notification to be deleted
 */
export async function deleteNotification(eventId: number) : Promise<MaybeError<undefined>> {
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
 * @param eventid The ID of the event
 * @param colour  The colour of the tag user wan to set
 */
export async function setEventTag(eventId: number, colour: Tag) : Promise<MaybeError<undefined>> {
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
export async function messageConversation(cardId: number, senderId: number, buyerId: number, message: string) : Promise<MaybeError<undefined>> {
  try {
    await instance.post(`/cards/${cardId}/conversations/${buyerId}`,{
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
 * Sends a search query to the backend to search a business's product catalogue.
 * @param businessId ID of the business whose product catalogue is about to be searched
 * @param query Query string to search for
 * @param pageIndex Index of page to start the results from (1 = first page)
 * @param resultsPerPage Number of results to return per page
 * @param searchBy List of product properties to search with
 * @param orderBy Specifies the method used to sort the results
 * @param reverse Specifies whether to reverse the search results (default order is descending for relevance and ascending for all other orders)
 */
 export async function searchCatalogue(businessId: number, query: string, pageIndex: number, resultsPerPage: number, searchBy: Array<ProductSearchBy>, orderBy: UserOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<Product>>> {
  let response;
  try {
    response = await instance.get(`/businesses/${businessId}/products/search`, {
      params: {
        searchQuery: query,
        page: pageIndex,
        resultsPerPage: resultsPerPage,
        searchBy: searchBy,
        reverse: reverse.toString(),
        orderBy: orderBy,
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'You do not have permission to access this product catalogue';
    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${error.response.data.message}`;
  }

  if (!is<SearchResults<Product>>(response.data)) {
    return 'Response is not product array';
  }

  return response.data;
}