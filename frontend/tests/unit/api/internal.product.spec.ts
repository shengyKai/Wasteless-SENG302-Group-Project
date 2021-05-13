import { getProducts, Product, Image } from "@/api/internal";
import axios, { AxiosError, AxiosInstance } from 'axios';

describe('Test GET /businesses/:id/products endpoint', () => {

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
  const instance: Mocked<Pick<AxiosInstance, 'get' | 'post' | 'patch' | 'delete' | 'put'>> = axios.instance;
  
  const image : Image = {
    id: 1,
    filename: "",
    thumbnailFilename: ""
  }

  it('When response is a product array where product has no null attributes, getProducts returns the product array', async () => {
    const responseData = [
      {
        id: "",
        name: "",
        description: "",
        manufacturer: "",
        recommendedRetailPrice: "",
        created: "",
        images: [image],
        countryOfSale: "",
      }
    ]
    instance['get'].mockResolvedValueOnce(() => {
      data: responseData
    })
    const products = await getProducts(1, 1, 1, "", false);
    expect(products).toEqual(responseData);
  })

  it('When response is a product array where product\'s optional attributes aren\'t present, getProducts returns the product array', async () => {
    const responseData = [
      {
        id: "",
        name: "",
        images: [],
      }
    ]
    instance['get'].mockResolvedValueOnce(() => {
      data: responseData
    })
    const products = await getProducts(1, 1, 1, "", false);
    expect(products).toEqual(responseData);
  })
})