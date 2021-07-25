import * as api from '@/api/internal';
import axios, { AxiosError, AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get'>> = axios.instance;

describe("Test GET /cards endpoint", () => {
  it('When the api call is made with all valid parameters, a list of cards is returned ', async () => {
    const responseData: api.SearchResults<api.MarketplaceCard> = {
      results: [{
        id: 1,
        creator: {
          id: 1,
          firstName: "somefirstname",
          lastName: "somelastname",
          email: "someemail",
          homeAddress: {
            country: "somecountry",
          }
        },
        section: "ForSale",
        created: "somedate",
        lastRenewed: "someotherdate",
        title: "sometitle",
        keywords: [
          {
            id: 1,
            name: "somename",
            created: "somedate"
          }
        ]
      }],
      count: 50,
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const card = await api.getMarketplaceCardsBySection("ForSale", 0, 0, "created", true);
    expect(card).toEqual(responseData);
  });

  it('When api call cannot reach the backend, the response will be an error message stating the failed attempt', async () => {
    instance.get.mockRejectedValueOnce({
      response: {}
    });
    const errorMessage = await api.getMarketplaceCardsBySection("ForSale", 0, 0, "created", true);
    expect(errorMessage).toEqual('Failed to reach backend');
  });

  it('When api call returns a 400 error status, the response will be a 400 error message', async () => {
    instance.get.mockRejectedValueOnce({
      response: {status: 400}
    });
    const errorMessage = await api.getMarketplaceCardsBySection("ForSale", 0, 0, "created", true);
    expect(errorMessage).toEqual('The given section does not exist');
  });

  it('When api call returns a 401 error status, the response will be a 401 error message', async () => {
    instance.get.mockRejectedValueOnce({
      response: {status: 401}
    });
    const errorMessage = await api.getMarketplaceCardsBySection("ForSale", 0, 0, "created", true);
    expect(errorMessage).toEqual('You have been logged out. Please login again and retry');
  });

  it('When api call returns any other error status, the response will be an error message with that status', async () => {
    instance.get.mockRejectedValueOnce({
      response: {status: 500}
    });
    const errorMessage = await api.getMarketplaceCardsBySection("ForSale", 0, 0, "created", true);
    expect(errorMessage).toEqual('Request failed: 500');
  });

  it('When api call returns any other error status, the response will be an error message with that status', async () => {
    const responseData =
      {
        results: [{id: 1}],
        count: 100,
      };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const errorMessage = await api.getMarketplaceCardsBySection("ForSale", 0, 0, "created", true);
    expect(errorMessage).toEqual("Response is not card array");
  });
});