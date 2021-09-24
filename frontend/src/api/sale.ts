import {is} from 'typescript-is';
import {InventoryItem} from "@/api/inventory";
import {MaybeError, SearchResults, instance} from "@/api/internal";
import { User } from './user';
import { Product } from './product';

export type SaleInterest = {
  userId: number,
  interested: boolean,
}

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
  interestCount?: number,
};

export type BoughtSale = {
  id: number,
  buyer: User | null,
  product: Product,
  interestCount: number,
  price: number,
  quantity: number,
  saleDate: string,
  listingDate: string,
}

type SalesOrderBy = 'created' | 'closing' | 'productCode' | 'productName' | 'quantity' | 'price'

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

/**
 * Sets the interest state (liked/unliked) for the given user and listing combination
 * @param listingId Listing to update the interest state for
 * @param interestBody includes userId User that the new interest state is applied for and
 * interested New interest state for the listing (true=like, false=unlike)
 */
export async function setListingInterest(listingId: number, interestBody: SaleInterest): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/listings/${listingId}/interest`, interestBody);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Listing does not exist';

    return error.response?.data.message;
  }
  return undefined;
}

/**
 * Check the interest status of the current user on the selected Listing
 * @param listingId   Listing to check the interest state for
 * @param userId      User that the interest state is checking for
 */
export async function getListingInterest(listingId: number, userId: number): Promise<MaybeError<boolean>> {
  let response;
  try {
    response = await instance.get(`/listings/${listingId}/interest`, {
      params:{userId: userId}
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid user provided';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'Listing does not exist';

    return error.response?.data.message;
  }
  return response.data.isInterested;
}


