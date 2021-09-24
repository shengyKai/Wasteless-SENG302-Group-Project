import {SearchResults} from "@/api/internal";
import axios, {AxiosInstance } from 'axios';
import {getProducts, Product, searchCatalogue} from "@/api/product";
import { Image } from '@/api/images';

jest.mock('axios', () => ({
  create: jest.fn(function() {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends {[k: string]: (...args: any[]) => any}> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>>}

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' >> = axios.instance;

describe('Test GET /businesses/:id/products endpoint', () => {

  const image : Image = {
    id: 1,
    filename: "",
    thumbnailFilename: ""
  };

  it('When response is a product array where product has no null attributes, getProducts returns the product array', async () => {
    const responseData:SearchResults<Product> = {
      results : [{
        id: "",
        name: "",
        description: "",
        manufacturer: "",
        recommendedRetailPrice: 10,
        created: "",
        images: [image],
        countryOfSale: "",

      }],
      count : 7,
    };

    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const products = await getProducts(1, 1, 1, "created", false);
    expect(products).toEqual(responseData);
  });

  it('When response is a product array where product\'s optional attributes aren\'t present, getProducts returns the product array', async () => {
    const responseData:SearchResults<Product> = {
      results : [{
        id: "",
        name: "",
        images: [image],

      }],
      count : 7,
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const products = await getProducts(1, 1, 1, "created", false);
    expect(products).toEqual(responseData);
  });

  it('When response does not contain the field id, getProducts returns an error message indicating that the response is not a product array', async () => {
    const responseData = {
      results : [{
        name: "",
        images: [image],

      }],
      count : 7,
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const products = await getProducts(1, 1, 1, "created", false);
    expect(products).toEqual('Response is not product array');
  });

  it('When response has a 401 status, getProducts returns an error message indicating that the user is not logged in', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('You have been logged out. Please login again and retry');
  });

  it('When response has a 403 status, getProducts returns an error message indicating that the user is not an admin of the business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 403,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Not an admin of the business');
  });

  it('When response has a 406 status, getProducts returns an error message indicating that buesiness does not exists', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Business not found');
  });

  it('When a response without a status is received, getProducts returns an error message indicating that the server could not be reached', async () => {
    instance.get.mockRejectedValueOnce("Server is down");
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Failed to reach backend');
  });

  it('When response has an error status that is not 401, 403 or 406, getProducts will return an error message with that status', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 732,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Request failed: 732');
  });
});

describe('Test GET /businesses/${businessId}/products/search endpoint', () => {
  const image : Image = {
    id: 1,
    filename: "",
    thumbnailFilename: ""
  };

  const responseData: SearchResults<Product> = {
    results : [{
      id: "",
      name: "",
      description: "",
      manufacturer: "",
      recommendedRetailPrice: 10,
      created: "",
      images: [image],
      countryOfSale: "",
    }],
    count : 7,
  };

  it('When response returns with a 200 response with valid parameters, searchCatalogue returns with a product array', async () => {
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const response = await searchCatalogue(1, "some query", 1, 10, ["name"], "description", true);
    expect(response).toEqual(responseData);
  });

  it('When response returns with a 200 response with multiple searchBy options, searchCatalogue returns with a product array', async () => {
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const response = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(response).toEqual(responseData);
  });

  it('When response returns with a 200 response with multiple searchBy options, searchCatalogue returns with a product array', async () => {
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const response = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(response).toEqual(responseData);
  });

  it('When response has a 401 status, searchCatalogue returns an error message indicating that the user is not logged in', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }});
    const message = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(message).toEqual('You have been logged out. Please login again and retry');
  });

  it('When response has a 403 status, searchCatalogue returns an error message indicating that the user is not an admin of the business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 403,
      }});
    const message = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(message).toEqual('You do not have permission to access this product catalogue');
  });

  it('When response has a 400 status, searchCatalogue returns an error message indicating that the search query is invalid', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          message: "some error"
        }
      }});
    const message = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(message).toEqual('Invalid search query: some error');
  });

  it('When response has a 403 status, searchCatalogue returns an error message indicating that the user is not an admin of the business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406,
      }});
    const message = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(message).toEqual('Business does not exist');
  });

  it('When a response without a status is received, searchCatalogue returns an error message indicating that the server could not be reached', async () => {
    instance.get.mockRejectedValueOnce("Server is down");
    const message = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(message).toEqual('Failed to reach backend');
  });

  it('When response has an error status that is not 401 or 403, searchCatalogue will return an error message with that status', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 732,
        data: {
          message: "some error"
        }
      }});
    const message = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(message).toEqual('Request failed: some error');
  });

  it('When response does not contain the required fields, searchCatalogue returns an error message indicating that the response is not a product array', async () => {
    const failedResponseData = {
      results : [{
        id: "",
        name: "",
        description: "",
        manufacturer: "",
        recommendedRetailPrice: 10
      }],
      count : 7,
    };
    instance.get.mockResolvedValueOnce({
      data: failedResponseData
    });
    const products = await searchCatalogue(1, "some query", 1, 10, ["name", "description"], "description", true);
    expect(products).toEqual('Response is not product array');
  });
});