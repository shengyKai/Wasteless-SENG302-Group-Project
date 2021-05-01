import {currencyFromCountry, newZealandDollar} from "@/components/utils/Methods/currency";

describe('currency.ts', () => {

  let consoleOutput: string[];

  beforeEach(() => {
    consoleOutput = [];
    console.warn = (output: string) => consoleOutput.push(output);
  });

  it('Returns NZD when API can\'t be reached', async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.reject("API is down")
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toBe(newZealandDollar);
    expect(consoleOutput).toEqual([
      "Failed to reach https://restcountries.eu/rest/v2/name/Australia?fullText=true&fields=currencies"
    ]);
  });

  it('Returns NZD when 404 response received', async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.resolve({
        status: 404
      }) as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toBe(newZealandDollar);
    expect(consoleOutput).toEqual([
      "No country with name Australia was found"
    ]);
  });

  it('Return NZD when 400 response received', async () => {
    globalThis.fetch = jest.fn(() =>
        Promise.resolve({
          status: 400
        }) as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toBe(newZealandDollar);
    expect(consoleOutput).toEqual([
      "Request failed: 400"
    ]);
  });

  it('Return NZD when 200 response received but response is not in expected format', async () => {
    globalThis.fetch = jest.fn(() =>
        Promise.resolve({
          status: 200,
          json: () => Promise.resolve([{currencies: [{potato: 'potato'}]}]) as any
        }) as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toBe(newZealandDollar);
    expect(consoleOutput).toEqual([
      "API response was not in readable format"
    ]);
  });

  it('Return currency received from API when response list contains one currency', async () => {
    globalThis.fetch = jest.fn(() =>
        Promise.resolve({
          status: 200,
          json: () => Promise.resolve([{currencies: [{
            code: 'AUD',
            name: 'Australian Dollar',
            symbol: '$'
          }]}]) as any
        }) as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(currency).toEqual({
      code: 'AUD',
      name: 'Australian Dollar',
      symbol: '$'
    });
    expect(consoleOutput).toEqual([]);
  });

  it('Return first currency received from API when response list contains multiple currencies', async () => {
    globalThis.fetch = jest.fn(() =>
        Promise.resolve({
          status: 200,
          json: () => Promise.resolve([{"currencies": [
            {
              "code": "BTN",
              "name": "Bhutanese ngultrum",
              "symbol": "Nu."
            },
            {
              "code": "INR",
              "name": "Indian rupee",
              "symbol": "₹"
            }
          ]}]) as any
        }) as any
    );
    const currency = await currencyFromCountry("Bhutan");
    expect(currency).toEqual({
      "code": "BTN",
      "name": "Bhutanese ngultrum",
      "symbol": "Nu."
    });
    expect(consoleOutput).toEqual([]);
  });


});