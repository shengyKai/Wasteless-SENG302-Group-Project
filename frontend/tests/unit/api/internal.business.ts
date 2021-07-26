import * as api from '@/api/internal';
import axios, { AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' >> = axios.instance;

describe('Test GET businesses/search endpoind', () => {

  let validBusiness: api.Business = {
    id: 88,
    primaryAdministratorId: 50,
    name: "Valid business",
    address: {
      streetNumber: "309",
      streetName: "Tudor Street",
      city: "Hamilton",
      region: "Waikato",
      country: "New Zealand",
      postcode: "7777"
    },
    businessType: "Accommodation and Food Services"
  };

  const invalidBusiness: any  = {
    potato: "Potato",
  };

  const validBusinessList: api.Business[] = [];
  for (let i = 0; i < 10; i++) {
    validBusiness.id = i;
    validBusinessList.push(validBusiness);
  }

  const invalidBusinessList: any[] = validBusinessList.concat([invalidBusiness]);

  it('When API request is successfully resolved and contains a valid list of businesses, returns that list', async () => {
    instance.get.mockResolvedValueOnce({
      data: {
        validBusinessList,
      }
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual(validBusinessList);
  });

  it('When API request is successfully resolved and contains a list which is not in a valid format, returns a message saying the format is invalid', async () => {
    instance.get.mockResolvedValueOnce({
      data: {
        invalidBusinessList,
      }
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual("Response was not a business array");
  });

  it('When API request is successfully resolved and contains an empty body, returns an empty list', async () => {
    const emptyList: any[] = [];
    instance.get.mockResolvedValueOnce({
      data: {
        emptyList,
      }
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual([]);
  });

  it('When API request is unsuccessfully resolved with a 400 status code, returns an error with the message received from the backend', async () => {
    instance.get.mockRejectedValueOnce({
      status: 400,
      data: {
        message: "Query too long",
      }
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual("Invalid search query: Query too long");
  });

  it('When API request is unsuccessfully resolved with a 401 status code, returns an error with a message that the user has been logged out', async () => {
    instance.get.mockRejectedValueOnce({
      status: 401,
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When API request is unsuccessfully resolved with any other status code, returns and error message with that status code', async () => {
    instance.get.mockRejectedValueOnce({
      status: 999,
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual("Request failed: 999");
  });

  it('When API request is resolved with an undefined status message, failed to reach backend error message is returned', async () => {
    instance.get.mockRejectedValueOnce({
      status: undefined,
    });
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual("Failed to reach backend");
  });

  it('When API request is resolved without a status code, failed to reach backend error message is returned', async () => {
    instance.get.mockRejectedValueOnce({});
    const response = await api.searchBusinesses('Query', 2, 10, "created", false);
    expect(response).toEqual("Failed to reach backend");
  });

});