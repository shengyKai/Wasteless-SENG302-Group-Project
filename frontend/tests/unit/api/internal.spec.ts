import axios, { AxiosError, AxiosInstance } from 'axios';

import { AxiosResponse } from 'axios';
import {SearchResults} from '@/api/internal';
import { castMock } from '../utils';
import { is, Reason } from 'typescript-is';
import {CreateUser, login, createUser} from "@/api/user";
import {CreateProduct, Product, createProduct, uploadProductImage, getProducts, modifyProduct} from "@/api/product";
import {InventoryItem} from "@/api/inventory";
import {Sale, getBusinessSales, setListingInterest, getListingInterest, purchaseListing } from "@/api/sale";
import {getMessagesInConversation, Message} from "@/api/marketplace";
import {generateReport, SaleRecord} from "@/api/salesReport";

const api = {
  login, createUser, createProduct, uploadProductImage, getProducts, modifyProduct,
  getBusinessSales, getMessagesInConversation, setListingInterest, getListingInterest, purchaseListing, generateReport
};

jest.mock('axios', () => ({
  create: jest.fn(function() {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    post: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
    put: jest.fn(),
  },
}));

jest.mock('typescript-is');

const mockedIs = castMock(is);
mockedIs.mockReturnValue(true);

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends {[k: string]: (...args: any[]) => any}> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>>}

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'post' | 'patch' | 'delete' | 'put'>> = axios.instance;

/**
 * Wraps a value inside an AxiosResponse.
 *
 * @param value The value inside the AxiosResponse
 * @param status The status for the response, if not provided then 200 is used
 * @returns An axios response with the value inside
 */
export function makeAxiosResponse<T>(value: T, status = 200): AxiosResponse<T> {
  return {
    data: value,
    status: status,
    statusText: 'Status ' + status,
    headers: {},
    config: {},
  };
}

/**
 * Makes an axios error corresponding to a request with no response.
 *
 * @returns An axios error with an empty response field.
 */
export function makeNoResponseError(): AxiosError<unknown> {
  return {
    name: 'test_no_response_name',
    message: 'test_no_response_message',
    isAxiosError: true,
    config: {},
    toJSON() {
      throw new Error('Not implemented');
    }
  };
}

/**
 * Creates an axios error with a value
 *
 * @param value The value inside the response.data field.
 * @param status The status code for this error
 * @returns An axios error
 */
export function makeAxiosError<T>(value: T, status = 400): AxiosError<T> {
  return {
    name: 'test_error_name',
    message: 'test_error_message',
    isAxiosError: true,
    config: {},
    response: makeAxiosResponse(value, status),
    toJSON() {
      throw new Error('Not implemented');
    }
  };
}

const testCreateUser: CreateUser = {
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  middleName: 'test_middlename',
  nickname: 'test_nickname',
  bio: 'test_bio',
  email: 'test_email',
  dateOfBirth: 'test_date_of_birth',
  phoneNumber: 'test_phone_number',
  homeAddress: { country: 'test_country'},
  password: 'test_password',
};

const testCreateProduct: CreateProduct = {
  id: 'ID-VALUE',
  name: 'test_name',
  description: 'test_description',
  manufacturer: 'test_manufacturer',
  recommendedRetailPrice: 100,
};

const testRecord: SaleRecord = {
  startDate: '2021-09-09',
  endDate: '2022-09-09',
  uniqueListingsSold: 12,
  uniqueBuyers: 12,
  uniqueProducts: 3,
  averageLikeCount: 40,
  averageDaysToSell: 5,
  totalPriceSold: 1032.10,
  totalQuantitySold: 3
};

const testProduct: Product = {
  id: 'ID-VALUE',
  name: 'test_name',
  images: []
};

const testInventoryItem: InventoryItem = {
  id: 7,
  product: testProduct,
  quantity: 100,
  remainingQuantity: 96,
  expires: '10-10-2021',
};

const testSaleItem: Sale = {
  id: 8,
  inventoryItem: testInventoryItem,
  quantity: 4,
  price: 1000,
  created: '1-1-1900',
  interestCount: 8
};

const testMessage: Message = {
  id: 7,
  created: '1-1-1900',
  senderId: 100,
  content: 'Hello world',
};

function searchResult<T>(list: T[]) : SearchResults<T> {
  return {
    count: list.length,
    results: list
  };
}

const testFile = new File([], 'test_file');
let testFormData = new FormData();
testFormData.append('file', testFile);

// Creates a type from the methods of T where the methods return promises
type PromiseMethods<T> = Pick<T, ({[P in keyof T]: T[P] extends (...args: any[]) => Promise<any> ? P : never })[keyof T]>;

// Creates a type where the promise around T is removed
type UnwrapPromise<T extends Promise<any>> = T extends Promise<infer U> ? U : never;

// All the api methods in internal.ts
type ApiMethods = PromiseMethods<typeof api>;

// Type of the autogenerated typescript-is error checker
type TypeChecker = (value: any) => null | { message: string, path: string[], reason: Reason };

// Object representing a call to an api method and the results of said call
type ApiCalls = {[k in keyof ApiMethods]: {
  parameters: Parameters<ApiMethods[k]>,            // The arguments that are provided
  httpMethod: keyof (typeof instance),              // The HTTP method to be used
  url: string,                                      // The expected url to be accessed
  body: any,                                        // The expected http body
  headers?: Record<string, string>,                 // The expected headers
  result: UnwrapPromise<ReturnType<ApiMethods[k]>>, // The expected function result if successful
  apiResult?: any,                                  // If the api returns a different value from the overall result then this should be used to specify that
  failedTypeCheckResponse?: string,                 // Error message string to return if the response is not the correct type
  extraStatusMessages: Record<number, string>,      // Mapping between statuses and their messages
  usesServerMessage: boolean,                       // Whether the "message" attribute in the response is used for unspecialised error messages
}};

const apiCalls: ApiCalls = {
  createProduct: {
    parameters: [
      7,
      testCreateProduct,
    ],
    httpMethod: 'post',
    url: '/businesses/7/products',
    body: testCreateProduct,
    result: undefined,
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'Operation not permitted',
      400: 'Invalid parameters',
      409: 'Product code unavailable',
    },
    usesServerMessage: false,
  },
  createUser: {
    parameters: [
      testCreateUser,
    ],
    httpMethod: 'post',
    url: '/users',
    body: testCreateUser,
    result: undefined,
    extraStatusMessages: {
      409: 'Email in use',
    },
    usesServerMessage: true,
  },
  login: {
    parameters: ['test_email', 'test_password'],
    httpMethod: 'post',
    url: '/login',
    body: { email: 'test_email', password: 'test_password' },
    result: 7,
    apiResult: { userId: 7 },
    failedTypeCheckResponse: 'Invalid response',
    extraStatusMessages: {
      400: 'Invalid credentials',
    },
    usesServerMessage: false,
  },
  uploadProductImage: {
    parameters: [100, 'TEST-PRODUCT', testFile],
    httpMethod: 'post',
    url: '/businesses/100/products/TEST-PRODUCT/images',
    body: testFormData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    result: undefined,
    extraStatusMessages: {
      400: 'Invalid image',
      401: 'You have been logged out. Please login again and retry',
      403: 'Operation not permitted',
      406: 'Product/Business not found',
      413: 'Image too large',
    },
    usesServerMessage: false,
  },
  getProducts: {
    parameters: [666, 3, 14, 'productCode', false],
    httpMethod: 'get',
    url: '/businesses/666/products',
    body: {
      params: {
        orderBy: "productCode",
        page: 3,
        resultsPerPage: 14,
        reverse: "false",
      }
    },
    result: { results: [testProduct], count: 77},
    failedTypeCheckResponse: 'Response is not product array',
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'Not an admin of the business',
      406: 'Business not found',
    },
    usesServerMessage: false,
  },
  getBusinessSales: {
    parameters: [666, 3, 13, 'closing', false],
    httpMethod: 'get',
    url: '/businesses/666/listings',
    body: {
      params: {
        page: 3,
        resultsPerPage: 13,
        orderBy: 'closing',
        reverse: false,
      }
    },
    result: searchResult([testSaleItem]),
    failedTypeCheckResponse: 'Response is not Sale array',
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      406: 'The given business does not exist',
    },
    usesServerMessage: false,
  },
  modifyProduct: {
    parameters: [888, 'FOO-BAR', testCreateProduct],
    httpMethod: 'put',
    url: '/businesses/888/products/FOO-BAR',
    body: testCreateProduct,
    result: undefined,
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'Operation not permitted',
      406: 'Product/Business not found',
      400: 'Invalid parameters',
      409: 'Product code unavailable',
    },
    usesServerMessage: false,
  },
  getMessagesInConversation: {
    parameters: [3, 5, 2, 10],
    httpMethod: 'get',
    url: '/cards/3/conversations/5',
    body: {
      params: {
        page: 2,
        resultsPerPage: 10,
      },
    },
    result: {
      count: 10,
      results: [testMessage],
    },
    failedTypeCheckResponse: 'Response is not page of messages',
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'You do not have permission to view this conversation',
      406: 'Unable to get messages, conversation does not exist',
    },
    usesServerMessage: true,
  },
  setListingInterest: {
    parameters: [7, {userId: 3, interested: false}],
    httpMethod: 'put',
    url: '/listings/7/interest',
    body: {
      userId: 3,
      interested: false,
    },
    result: undefined,
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'Operation not permitted',
      406: 'Listing does not exist',
    },
    usesServerMessage: true,
  },
  getListingInterest: {
    parameters: [7, 3],
    httpMethod: 'get',
    url: '/listings/7/interest',
    body: {
      params: {
        userId: 3,
      },
    },
    result:false,
    apiResult: {
      isInterested: false
    },
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'Operation not permitted',
      406: 'Listing does not exist',
    },
    usesServerMessage: true,
  },
  purchaseListing: {
    parameters: [6, 5],
    httpMethod: 'post',
    url: '/listings/6/purchase',
    body: {
      purchaserId: 5,
    },
    result: undefined,
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      406: 'Listing does not exist',
    },
    usesServerMessage: true,
  },
  generateReport: {
    parameters: [1, "2022-08-08", "2022-09-09", "daily"],
    httpMethod: 'get',
    url: "/businesses/1/reports",
    body: { params:{
      startDate: "2022-08-08",
      endDate: "2022-09-09",
      granularity: "daily"
    }},
    result: [testRecord],
    failedTypeCheckResponse: "Invalid response type",
    extraStatusMessages: {
      401: 'You have been logged out. Please login again and retry',
      403: 'You do not have permission to view reports for this business',
      406: 'Business not found',
    },
    usesServerMessage: false
  }
};


describe('api', () => {
  // Makes sure the the mocks are clean
  afterEach(() => {
    jest.clearAllMocks();
  });
  describe.each(Object.keys(apiCalls))('"%s" method', (key) => {
    // Assumes that we don't assign undefined into apiCalls
    let fields = apiCalls[key as keyof ApiCalls]!;

    if (fields.apiResult === undefined) fields.apiResult = fields.result;

    function doCall(): Promise<typeof fields.result> {
      // @ts-expect-error - Assuming that the construction of the ApiCalls type is correct then this is valid
      return api[key](...fields.parameters);
    }

    it('If the backend is unavailable then there should be a "Failed to reach backend" message', async () => {
      instance[fields.httpMethod].mockRejectedValueOnce(makeNoResponseError());

      let response = await doCall();

      expect(response).toBe('Failed to reach backend');
    });

    for (const statusString of Object.keys(fields.extraStatusMessages)) {
      const status = parseInt(statusString);
      const message = fields.extraStatusMessages[status];
      it(`If status ${status} is returned then response should be "${message}"`, async () => {
        instance[fields.httpMethod].mockRejectedValueOnce(makeAxiosError(undefined, status));

        let response = await doCall();
        expect(response).toBe(message);
      });
    }

    if (fields.usesServerMessage) {
      it('If an unknown status is returned then response should be the backend message', async () => {
        const statusCode = 875;
        instance[fields.httpMethod].mockRejectedValueOnce(makeAxiosError({message: 'test backend message'}, statusCode));

        let response = await doCall();

        expect(response).toBe('test backend message');
      });
    } else {
      it('If an unknown status is returned then response should be "Request failed: ?"', async () => {
        const statusCode = 875;
        instance[fields.httpMethod].mockRejectedValueOnce(makeAxiosError(undefined, statusCode));

        let response = await doCall();

        expect(response).toBe('Request failed: ' + statusCode);
      });
    }

    it('Method should be called with the expected url and body', async () => {
      instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(fields.apiResult));

      await doCall();

      const parameters: any[] = [fields.url];
      if (fields.body !== undefined) {
        parameters.push(fields.body);
      }
      if (fields.headers !== undefined) {
        parameters.push({headers: fields.headers });
      }

      expect(instance[fields.httpMethod]).toBeCalledWith(...parameters);
    });

    it('Method should return the correct value if successful', async () => {
      instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(fields.apiResult));

      let response = await doCall();
      expect(response).toBe(fields.result);
    });

    if (fields.failedTypeCheckResponse !== undefined) {
      it('Method should return correct messasge if type check fails', async () => {
        mockedIs.mockReturnValueOnce(false);
        instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(fields.apiResult));

        let response = await doCall();
        expect(response).toBe(fields.failedTypeCheckResponse);
      });
      it('Typechecker should pass with expected arguments', async () => {
        instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(fields.apiResult));

        await doCall();

        // @ts-expect-error - typescript-is generates a second argument that isn't in the function signature that does the validation
        const [input, checker]: [any, TypeChecker] = mockedIs.mock.calls[0];
        expect(checker(input)).toBeNull();
      });
    } else {
      it('Method should not type check response', async () => {
        instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(fields.apiResult));

        await doCall();

        expect(mockedIs).not.toBeCalled();
      });
    }
  });
});