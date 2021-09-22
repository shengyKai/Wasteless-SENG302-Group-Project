import axios, {AxiosInstance } from 'axios';
import {createNewKeyword, deleteKeyword, searchKeywords} from "@/api/keyword";

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
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'post' | 'delete'>> = axios.instance;

describe("Test GET /keywords/search endpoint", () => {
  const invalidKeyword = {
    thing: "woop",
  };
  const invalidKeywordList: any[] = [invalidKeyword];
  it("When response is resolved with 200, response contains list of keywords", async () => {
    const defaultWords = [{
      id: 1,
      name: "Gluten Free",
      created: "2002-02-02"
    }];
    instance.get.mockResolvedValueOnce({
      data: defaultWords
    });
    const keywords = await searchKeywords("query");
    expect(keywords).toEqual(defaultWords);
  });

  it("When API request is resolved without a status code, failed to reach backend error message is returned", async () => {
    instance.get.mockRejectedValueOnce({});
    const keywords = await searchKeywords("query");
    expect(keywords).toEqual("Failed to reach backend");
  });

  it("Response contains 401 error code, error informing user to log in", async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const keywords = await searchKeywords("query");
    expect(keywords).toEqual('You have been logged out. Please login again and retry');
  });

  it("When API request is unsuccessfully resolved with any other status code, returns an error message with that status code", async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 999,
      }
    });
    const keywords = await searchKeywords("query");
    expect(keywords).toEqual('Request failed: 999');
  });

  it("When API request is successfully resolved and returns data which is not keywords, returns a message saying format is invalid", async () => {
    instance.get.mockResolvedValueOnce({
      data: {
        invalidKeywordList
      }
    });
    const keywords = await searchKeywords("query");
    expect(keywords).toEqual('Response is not Keyword array');
  });
});

describe("Test POST /keywords endpoint", () => {
  it("Create keyword", async () => {
    instance.post.mockResolvedValueOnce({
      data: {
        keywordId: 1
      }
    });
    let keyword = {
      name: "New keyword"
    };
    const createdKeyword = await createNewKeyword(keyword);
    expect(createdKeyword).toEqual(1);
  });

  it("Keyword already exists", async () => {
    instance.post.mockRejectedValue({
      response: {
        status: 400
      }
    });
    let keyword = {
      name: "New keyword"
    };
    const createdKeyword = await createNewKeyword(keyword);
    expect(createdKeyword).toEqual("This keyword already exists or is of invalid format");
  });

  it("Unauthorised call to create keyword", async () => {
    instance.post.mockRejectedValue({
      response: {
        status: 401
      }
    });
    let keyword = {
      name: "New keyword"
    };
    const createdKeyword = await createNewKeyword(keyword);
    expect(createdKeyword).toEqual("You have been logged out. Please login again and retry");
  });

  it("Backend couldn't be reached", async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    let keyword = {
      name: "New keyword"
    };
    const createdKeyword = await createNewKeyword(keyword);
    expect(createdKeyword).toEqual("Failed to reach backend");


  });


  describe("Test DELETE /keywords/id endpoint", () => {
    it('When provided with a keyword id which exists and a keyword is successfully deleted on the backend, there will be no return message', async ()=>{
      instance.delete.mockResolvedValueOnce({
        response: {
          status: 200
        }
      });
      const response = await deleteKeyword(1);
      expect(response).toEqual(undefined);
    });

    it('When API deletion request is successfully resolved but there is an invalid access token, a message is returned about the missing/invalid token', async ()=>{
      instance.delete.mockRejectedValueOnce({
        response: {
          status: 401
        }
      });
      const response = await deleteKeyword(1);
      expect(response).toEqual('You have been logged out. Please login again and retry');
    });

    it('When API deletion request is successfully resolved but there is an invalid authorisation, a message is returned about the invalid authorisation', async ()=>{
      instance.delete.mockRejectedValueOnce({
        response: {
          status: 403
        }
      });
      const response = await deleteKeyword(1);
      expect(response).toEqual('Invalid authorization for keyword deletion');
    });

    it('When API deletion request is successfully resolved but the keyword id does not exist, a message is returned about the unfindable keyword', async ()=>{
      instance.delete.mockRejectedValueOnce({
        response: {
          status: 406
        }
      });
      const response = await deleteKeyword(1);
      expect(response).toEqual('Keyword not found');
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
      const response = await deleteKeyword(1);
      expect(response).toEqual('Request failed: Some error message');
    });
  });
});