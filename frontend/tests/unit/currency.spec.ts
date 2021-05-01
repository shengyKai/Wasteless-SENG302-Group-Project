import { Currency, currencyFromCountry, newZealandDollar } from "@/components/utils/Methods/currency";

describe('currency.ts', () => {

  beforeAll(() => {
    globalThis.fetch = jest.fn(() =>
      Promise.reject("API is down")
    );
  });

  it('Returns NZD when API can\'t be reached', async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.reject("API is down")
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toBe(newZealandDollar);
  });

  it('Returns NZD when 404 response recieved', async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.resolve({
        status: '404'
      }) as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toBe(newZealandDollar);
  });

});