import { Business, searchBusinesses, ModifyBusiness, modifyBusiness, uploadBusinessImage } from '@/api/internal';
import axios, { AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    put: jest.fn(),
    post: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'post' | 'put' >> = axios.instance;

describe('Test GET businesses/search endpoint', () => {
  let validBusiness: Business = {
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

  const validBusinessList: Business[] = [];
  for (let i = 0; i < 10; i++) {
    validBusiness.id = i;
    validBusinessList.push(validBusiness);
  }

  const invalidBusinessList: any[] = validBusinessList.concat([invalidBusiness]);

  it('When API request is successfully resolved and contains a valid list of businesses, returns that list', async () => {
    const body = {
      count: validBusinessList.length,
      results: validBusinessList,
    };
    instance.get.mockResolvedValueOnce({
      data: body
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual(body);
  });

  it('When API request is successfully resolved and contains a list which is not in a valid format, returns a message saying the format is invalid', async () => {
    const responseData = {
      results: invalidBusinessList,
      count: 10
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual("Response is not business array");
  });

  it('When API request is successfully resolved and contains an empty body, returns an empty list', async () => {
    const emptyList: any[] = [];
    const responseData = {
      results: emptyList,
      count: 0
    };
    instance.get.mockResolvedValueOnce({
      data: {
        results: emptyList,
        count: 0
      }
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual({results:[],count:0});
  });

  it('When API request is unsuccessfully resolved with a 400 status code, returns an error with the message received from the backend', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          message: "Query too long",
        }
      }
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual("Invalid search query: Query too long");
  });

  it('When API request is unsuccessfully resolved with a 401 status code, returns an error with a message that the user has been logged out', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When API request is unsuccessfully resolved with any other status code, returns and error message with that status code', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 999,
      }
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual("Request failed: 999");
  });

  it('When API request is resolved with an undefined status message, failed to reach backend error message is returned', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual("Failed to reach backend");
  });

  it('When API request is resolved without a status code, failed to reach backend error message is returned', async () => {
    instance.get.mockRejectedValueOnce({
      response: {}
    });
    const response = await searchBusinesses('Query', 'Accommodation and Food Services', 2, 10, "created", false);
    expect(response).toEqual("Failed to reach backend");
  });

});

describe('Test PUT /businesses/${businessId} endpoint', () => {
  let business: ModifyBusiness = {
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
    businessType: "Accommodation and Food Services",
    updateProductCountry: true,
    primaryImageId: 1
  };

  it('When API request is successfully resolved, returns undefined', async () => {
    instance.put.mockResolvedValueOnce({
      response: {
        status: 200,
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual(undefined);
  });

  it('When API request is unsuccessful and gives an undefined error, returns a message stating failed to reach backend', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: undefined
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual("Failed to reach backend");
  });

  it('When API request is unsuccessful and gives a 401 error, returns a message saying the details are invalid along with a reason from the backend', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          message: 'Name too long',
        }
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual("Invalid details entered: Name too long");
  });

  it('When API request is unsuccessful and gives a 401 error, returns a message stating user has been logged out', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When API request is unsuccessful and gives a 403 error, returns a message stating user does not have the authorization', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual("Invalid authorization for modifying this business");
  });

  it('When API request is unsuccessful and gives a 406 error, returns a message saying the business does not exist', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual("Business does not exist");
  });


  it('When API request is unsuccessful and gives an uncaught error status, returns a message stating that error status and a message', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 999,
        data: {
          message: "some error"
        }
      }
    });
    const response = await modifyBusiness(88, business);
    expect(response).toEqual("Request failed: some error");
  });
});

describe("POST /businesses/{businessId}/images", ()=>{
  const demoFIle = new File([], 'test_file');

  it('When the API request is successfully resolved, nothing is returned', async ()=>{
    instance.post.mockResolvedValueOnce({
      response: {
        status: 201
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual(undefined);
  });

  it('When the response is 400, a message says image is invalid', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 400
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("Invalid image");
  });

  it('When the session is invalid, message teeling user to log back in', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When the user has invalid permission, permission error', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("Operation not permitted");
  });

  it('When business does not exists, business not found error', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("Business not found");
  });

  it('When image too large, image too large error', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 413
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("Image too large");
  });

  it('When response is undefined, failed to reach backend', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: undefined
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("Failed to reach backend");
  });
  it('Any other response, message should be displayed', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 123,
        data: {
          message: "message from server"
        }
      }
    });
    const response = await uploadBusinessImage(1, demoFIle);
    expect(response).toEqual("Request failed: message from server");
  });

});
