import axios, { AxiosInstance } from 'axios';
import {User} from "@/api/user";
import {
  CreateMarketplaceCard,
  createMarketplaceCard,
  deleteMarketplaceCard,
  extendMarketplaceCardExpiry, getMarketplaceCardsBySectionAndKeywords,
  getMarketplaceCardsByUser, MarketplaceCard, ModifyMarketplaceCard, modifyMarketplaceCard
} from "@/api/marketplace";

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    post: jest.fn(),
    delete: jest.fn(),
    put: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'post' | 'delete' | 'put' | 'get' >> = axios.instance;

describe("Test POST /cards endpoint", () => {

  const marketplaceCard : CreateMarketplaceCard = {
    creatorId: 1,
    section: "Exchange",
    title: "Title",
    keywordIds: [1],
  };

  it('When API request is successfully resolved and contains cardId field which has a number, response is the integer value of cardId', async ()=>{
    instance.post.mockResolvedValueOnce({
      data: {
        cardId: 15
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual(15);
  });

  it('When API request is successfully resolved does not contain cardId field, message is returned saying response format was invalid', async ()=>{
    instance.post.mockResolvedValueOnce({
      data: {
        notCardId: 15
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual('Invalid response format');
  });

  it('When API request is successfully resolved and has cardId field which is not a number, message is returned saying response format was invalid', async ()=>{
    instance.post.mockResolvedValueOnce({
      data: {
        cardId: "Not a number"
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual('Invalid response format');
  });

  it('When API response has a 400 status code, an error message saying the request was incorrectly formatted will be returned', async () =>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          message: "Title too long"
        }
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Incorrect marketplace card format: Title too long");
  });

  it('When API response has a 401 status code, an error message saying the token was invalid will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When API response has a 403 status code, an error message saying a card cannot be created for another user will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: 403,
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("A user cannot create a marketplace card for another user");
  });

  it('When response is undefined status, an error message containing the message from the response will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: 738,
        data: {
          message: "The server is having a bad day"
        }
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Request failed: The server is having a bad day");
  });

  it('When response is undefined status, an error message stating unable to reach backend will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const response = await createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Failed to reach backend");
  });
});

describe("Test DELETE /cards/{id} endpoint", () => {
  it('When provided with a card id which exists and a card is successfully deleted on the backend, there will be no return message', async ()=>{
    instance.delete.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual(undefined);
  });

  it('When API deletion request is successfully resolved but there is an invalid access token, a message is returned about the missing/invalid token', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('You have been logged out. Please login again and retry');
  });

  it('When API deletion request is successfully resolved but there is an invalid authorisation, a message is returned about the invalid authorisation', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('Invalid authorization for card deletion');
  });

  it('When API deletion request is successfully resolved but the card id does not exist, a message is returned about the unfindable card', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('Marketplace card not found');
  });

  it('When API deletion request is successfully resolved but an unspecified error in the backend occurs, a message is returned about the error', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 500,
        data: {
          message: "Some error message"
        }
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('Request failed: Some error message');
  });
});

describe("Test PUT /cards/{id}/extenddisplayperiod endpoint", () => {
  it('When provided with a card id which exists and the card expiry is successfully extended on the backend, there will be no return message', async ()=>{
    instance.put.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await extendMarketplaceCardExpiry(1);
    expect(response).toEqual(undefined);
  });

  it('When API extension request is successfully resolved but there is an invalid access token, a message is returned about the missing/invalid token', async ()=>{
    instance.put.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await extendMarketplaceCardExpiry(1);
    expect(response).toEqual('You have been logged out. Please login again and retry');
  });

  it('When API extension request is successfully resolved but there is an invalid authorisation, a message is returned about the invalid authorisation', async ()=>{
    instance.put.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await extendMarketplaceCardExpiry(1);
    expect(response).toEqual('Invalid authorization for card expiry extension');
  });

  it('When API extension request is successfully resolved but the card id does not exist, a message is returned about the unfindable card', async ()=>{
    instance.put.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await extendMarketplaceCardExpiry(1);
    expect(response).toEqual('Marketplace card not found');
  });

  it('When API extension request is successfully resolved but an unspecified error in the backend occurs, a message is returned about the error', async ()=>{
    instance.put.mockRejectedValueOnce({
      response: {
        status: 500,
        data: {
          message: "Some error message"
        }
      }
    });
    const response = await extendMarketplaceCardExpiry(1);
    expect(response).toEqual('Request failed: Some error message');
  });
});

describe("Test DELETE /cards/{id} endpoint", () => {
  it('When provided with a card id which exists and a card is successfully deleted on the backend, there will be no return message', async ()=>{
    instance.delete.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual(undefined);
  });

  it('When API deletion request is successfully resolved but there is an invalid access token, a message is returned about the missing/invalid token', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('You have been logged out. Please login again and retry');
  });

  it('When API deletion request is successfully resolved but there is an invalid authorisation, a message is returned about the invalid authorisation', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('Invalid authorization for card deletion');
  });

  it('When API deletion request is successfully resolved but the card id does not exist, a message is returned about the unfindable card', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('Marketplace card not found');
  });

  it('When API deletion request is successfully resolved but an unspecified error in the backend occurs, a message is returned about the error', async ()=>{
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 500,
        data: {
          message: "Some error message"
        }
      }
    });
    const response = await deleteMarketplaceCard(1);
    expect(response).toEqual('Request failed: Some error message');
  });
});

describe("Test GET /users/{userId}/cards endpoint", () => {
  const user : User = {
    id: 1,
    firstName: "some firstName",
    lastName: "some lastName",
    email: "some email",
    homeAddress: {
      country: "some country"
    }
  };

  const marketplaceCard : MarketplaceCard = {
    id: 2,
    creator: user,
    section: "Exchange",
    created: "some date",
    title: "some title",
    lastRenewed: "some date",
    keywords: []
  };

  it('When the api get request is successfully resolved, it will return all the cards related to that user', async ()=>{
    const responseData = {
      count: 1,
      results: [marketplaceCard]
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const response = await getMarketplaceCardsByUser(1, 12, 1);
    expect(response).toEqual(responseData);
  });

  it('When the api get request fails and returns a 400 error, it will return an error message stating the page does not exist', async ()=>{
    instance.get.mockRejectedValueOnce({
      response: {
        status: 400
      }
    });
    const response = await getMarketplaceCardsByUser(1, 12, 1);
    expect(response).toEqual("The page does not exist");
  });

  it('When the api get request fails and returns a 401 error, it will return an error message stating the user has not logged in', async ()=>{
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await getMarketplaceCardsByUser(1, 12, 1);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When the api get request fails and returns a 406 error, it will return an error message stating the user does not exist', async ()=>{
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await getMarketplaceCardsByUser(1, 12, 1);
    expect(response).toEqual("The user does not exist");
  });

  it('When the api get request fails and returns an unidentified error, it will return an error message stating the error that occured in the backend', async ()=>{
    instance.get.mockRejectedValueOnce({
      response: {
        status: 500,
        data: {
          message: "Some error message"
        }
      }
    });
    const response = await getMarketplaceCardsByUser(1, 12, 1);
    expect(response).toEqual("Request failed: Some error message");
  });

  it('When the api get request resolves but the return value is not an array of marketplace cards, it will return an error message stating the error', async ()=>{
    instance.get.mockResolvedValueOnce({
      response: {}
    });
    const response = await getMarketplaceCardsByUser(1, 12, 1);
    expect(response).toEqual("Response is not card array");
  });

  describe("Test PUT /cards/:id endpoint", () => {

    const card: ModifyMarketplaceCard = {
      section: "ForSale",
      title: "1982 Lada Samara",
      description: "Beige, suitable for a hen house. Fair condition. Some rust. As is, where is. Will swap for budgerigar.",
      keywordIds: [20, 15, 600],
    };

    it('When response has a 200 status code, undefined is returned', async () => {
      instance.put.mockResolvedValueOnce({
        response: {
          status: 200,
        }
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toBe(undefined);
    });

    it('When response is undefined status, the result will be an error message stating unable to reach backend', async () => {
      instance.put.mockRejectedValueOnce({
        response: {
          status: undefined,
        }
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("Failed to reach backend");
    });

    it('When response is 401 status, the result will be an error message stating the access token is invalid/missing', async () => {
      instance.put.mockRejectedValueOnce({
        response: {
          status: 401,
        }
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("You have been logged out. Please login again and retry");
    });

    it('When response is 403 status, the result will be an error message stating the operation is not permitted', async () => {
      instance.put.mockRejectedValueOnce({
        response: {
          status: 403,
        }
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("Operation not permitted");
    });

    it('When response is 406 status, the result will be an error message stating there is no such marketplace card', async () => {
      instance.put.mockRejectedValueOnce({
        response: {
          status: 406,
        }
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("Marketplace card not found");
    });

    it('When response is 400 status, the result contain the error message received in the response', async () => {
      instance.put.mockRejectedValueOnce({
        response: {
          status: 400,
          data: {
            message: "Title too long"
          }
        },
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("Invalid parameters: Title too long");
    });

    it('When response is any other error status number, the result will be an error message stating the request failed', async () => {
      instance.put.mockRejectedValueOnce({
        response: {
          status: 999,
        }
      });
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("Request failed: 999");
    });

    it('When a response without a status is received, the result returns an error message indicating that the server could not be reached', async () => {
      instance.put.mockRejectedValueOnce("Server is down");
      const message = await modifyMarketplaceCard(7, card);
      expect(message).toEqual("Failed to reach backend");
    });

  });

  describe("Test GET /cards/search endpoint", () => {
    const user : User = {
      id: 1,
      firstName: "some firstName",
      lastName: "some lastName",
      email: "some email",
      homeAddress: {
        country: "some country"
      }
    };
    const marketplaceCard : MarketplaceCard = {
      id: 2,
      creator: user,
      section: "Exchange",
      created: "some date",
      title: "some title",
      lastRenewed: "some date",
      keywords: []
    };

    it('When the api get request is successfully resolved, it will return all the cards related to that section', async ()=>{
      const responseData = {
        count: 1,
        results: [marketplaceCard]
      };
      instance.get.mockResolvedValueOnce({
        data: responseData
      });
      const response = await getMarketplaceCardsBySectionAndKeywords([],'Wanted', false, 1, 10, "created", false);
      expect(response).toEqual(responseData);
    });

    it('When the api get request fails and returns a 400 error, it will return an error message stating the page does not exist', async ()=>{
      instance.get.mockRejectedValueOnce({
        response: {
          status: 400
        }
      });
      const response = await getMarketplaceCardsBySectionAndKeywords([],'Wanted', false, 1, 10, "created", false);
      expect(response).toEqual("The given section does not exist");
    });

    it('When the api get request fails and returns a 401 error, it will return an error message stating the user has not logged in', async ()=>{
      instance.get.mockRejectedValueOnce({
        response: {
          status: 401
        }
      });
      const response = await getMarketplaceCardsBySectionAndKeywords([],'Wanted', false, 1, 10, "created", false);
      expect(response).toEqual("You have been logged out. Please login again and retry");
    });

    it('When the api get request fails and returns an unidentified error, it will return an error message stating the error that occured in the backend', async ()=>{
      instance.get.mockRejectedValueOnce({
        response: {
          status: 500,
          data: {
            message: "Some error message"
          }
        }
      });
      const response = await getMarketplaceCardsBySectionAndKeywords([],'Wanted', false, 1, 10, "created", false);
      expect(response).toEqual("Request failed: Some error message");
    });

    it('When the api get request resolves but the return value is not an array of marketplace cards, it will return an error message stating the error', async ()=>{
      instance.get.mockResolvedValueOnce({
        response: {}
      });
      const response = await getMarketplaceCardsBySectionAndKeywords([],'Wanted', false, 1, 10, "created", false);
      expect(response).toEqual("Response is not card array");
    });


  });

});