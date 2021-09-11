import {is} from 'typescript-is';
import {InventoryItem} from "@/api/internal-inventory";
import {MaybeError, SearchResults, instance} from "@/api/internal";

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