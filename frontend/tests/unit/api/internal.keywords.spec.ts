import * as api from '@/api/internal';
import axios, { AxiosError, AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    post: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'post'>> = axios.instance;

describe("Test GET /keywords/search endpoint", () => {
  it("Get keywords", async () => {
    const defaultWords = [{
      id: 1,
      name: "Gluten Free",
      created: "2002-02-02"
    }];
    instance.get.mockResolvedValueOnce({
      data: defaultWords
    });
    const keywords = await api.getKeywords();
    expect(keywords).toEqual(defaultWords);
  });

  it("Backend couldn't be reached", async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const keywords = await api.getKeywords();
    expect(keywords).toEqual("Failed to reach backend");
  });

  it("Unauthorised call to keywords", async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const keywords = await api.getKeywords();
    expect(keywords).toEqual('You have been logged out. Please login again and retry');
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
    const createdKeyword = await api.createNewKeyword(keyword);
    let expectedResponse = {
      keywordId: 1
    };
    expect(createdKeyword).toEqual(expectedResponse);
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
    const createdKeyword = await api.createNewKeyword(keyword);
    expect(createdKeyword).toEqual("This keyword already exists");
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
    const createdKeyword = await api.createNewKeyword(keyword);
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
    const createdKeyword = await api.createNewKeyword(keyword);
    expect(createdKeyword).toEqual("Failed to reach backend");
  });
});