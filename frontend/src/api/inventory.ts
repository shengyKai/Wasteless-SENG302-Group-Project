import {is} from 'typescript-is';
import {Product} from "@/api/product";
import {MaybeError, SearchResults, instance} from "@/api/internal";

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

type InventoryOrderBy = 'name' | 'description' | 'manufacturer' | 'recommendedRetailPrice' | 'created' | 'quantity' |
  'pricePerItem' | 'totalPrice' | 'manufactured' | 'sellBy' | 'bestBefore' | 'expires' | 'productCode';

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