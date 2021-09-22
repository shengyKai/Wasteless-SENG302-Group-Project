import {is} from 'typescript-is';
import {Image, MaybeError, SearchResults, instance} from "@/api/internal";
import {Business} from "@/api/business";

export type Product = {
  id: string,
  name: string,
  description?: string,
  manufacturer?: string,
  recommendedRetailPrice?: number,
  created?: string,
  images: Image[],
  countryOfSale?: string,
  business?: Business
};

export type CreateProduct = Omit<Product, 'created' | 'images'>;
export type ProductSearchBy = 'name' | 'description' | 'manufacturer' | 'productCode';

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
export async function makeProductImagePrimary(businessId: number, productId: string, imageId: number): Promise<MaybeError<undefined>> {
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
  Promise<MaybeError<SearchResults<Product>>> {
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
 * Sends a search query to the backend to search a business's product catalogue.
 * @param businessId ID of the business whose product catalogue is about to be searched
 * @param query Query string to search for
 * @param pageIndex Index of page to start the results from (1 = first page)
 * @param resultsPerPage Number of results to return per page
 * @param searchBy List of product properties to search with
 * @param orderBy Specifies the method used to sort the results
 * @param reverse Specifies whether to reverse the search results (default order is descending for relevance and ascending for all other orders)
 */
export async function searchCatalogue(businessId: number, query: string, pageIndex: number, resultsPerPage: number, searchBy: ProductSearchBy[], orderBy: ProductOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<Product>>> {
  const params = new URLSearchParams();
  for (let field of searchBy) {
    params.append("searchBy", field);
  }
  params.append('searchQuery', query);
  params.append('page', pageIndex.toString());
  params.append('resultsPerPage', resultsPerPage.toString());
  params.append('orderBy', orderBy);
  params.append('reverse', reverse.toString());

  let response;
  try {
    response = await instance.get(`/businesses/${businessId}/products/search`, {
      params
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === 400) return 'Invalid search query: ' + error.response?.data.message;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'You do not have permission to access this product catalogue';
    if (status === 406) return 'Business does not exist';
    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${error.response?.data.message}`;
  }

  if (!is<SearchResults<Product>>(response.data)) {
    return 'Response is not product array';
  }

  return response.data;
}

/**
 * Deletes an image from a product
 * @param businessId The ID of the business that owns the product
 * @param productId The ID of the product that has the image
 * @param imageId The ID of the image
 */
export async function deleteProductImage(businessId: number, productId: string, imageId: number): Promise<MaybeError<undefined>> {
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