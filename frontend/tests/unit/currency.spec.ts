import {currencyFromCountry, defaultCurrency, queryCurrencyAPI, getCurrencyFromAPIResponse} from "@/api/currency";

describe('currency.ts', () => {

  let consoleOutput: string[];

  beforeEach(() => {
    consoleOutput = [];
    console.warn = (output: string) => consoleOutput.push(output);
  });

  it('Returns with an error message to the frontend when API can\'t find the country', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 404
    } as any
    );
    const response = await currencyFromCountry("Some wrong country");
    expect(response.errorMsg).toBe("No country with name Some wrong country was found, so no currency is shown");
  });

  it('Returns with a format error message to the frontend when API responds with a country', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{currencies: [{potato: 'potato'}]}]) as any
    } as any
    );
    const response = await currencyFromCountry("Australia");
    expect(response.errorMsg).toBe("There was a fault with the system, so no currency is shown");
  });

  it('Returns with no error message to the frontend when API responds with a country with correct format', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{currencies: [{
        code: 'AUD',
        name: 'Australian Dollar',
        symbol: '$'
      }]}]) as any
    });
    const response = await currencyFromCountry("Some correct country");
    expect(response.errorMsg).toBe(undefined);
  });

  /**
   * Test that when no response is recieved from the API, the currencyFromCountry will print a warning to the console
   * saying the API cannot be reached and will return no currency
   */
  it('Returns no currency to the frontend and outputs to the console when API can\'t be reached', async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.reject("API is down")
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "Failed to reach https://restcountries.eu/rest/v2/name/Australia?fullText=true&fields=currencies"
    ]);
  });

  /**
   * Test that when a 404 response is recieved from the RESTCounties API, the currencyFromCountry method print a
   * warning to the console which says that country cannot be found and will return no currency
   */
  it('Returns no currency to the frontend and outputs to the console when 404 response received', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 404
    } as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "No country with name Australia was found"
    ]);
  });

  /**
   * Test that when a 400 response is recieved from the RESTCounties API, the currencyFromCountry method print a
   * warning to the console with the error code 400 and will return no currency
   */
  it('Returns no currency to the frontend and outputs to the console when 400 response received', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 400
    } as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "Request failed: 400"
    ]);
  });

  /**
   * Test that when a 200 response is recieved from the RESTCounties API and that response does not have
   * the expected format, the currencyFromCountry method print a warning to the console which says it can't
   * read the resopnse and will return no currency
   */
  it('Returns no currency to the frontend and outputs to the console when 200 response received but response is not in expected format', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{currencies: [{potato: 'potato'}]}]) as any
    } as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "API response was not in readable format"
    ]);
  });

  /**
   * Test that the when a response containing a single valid currencies is returned from the RESTCounties API
   * the currencyFromCountries method will return that currency.
   */
  it('Return currency received from API when response list contains one currency', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{currencies: [{
        code: 'AUD',
        name: 'Australian Dollar',
        symbol: '$'
      }]}]) as any
    });
    const currency = await currencyFromCountry("Australia");
    expect(currency).toEqual({
      code: 'AUD',
      name: 'Australian Dollar',
      symbol: '$'
    });
    expect(consoleOutput).toEqual([]);
  });

  /**
   * Test that the when a response containing multiple valid currencies is returned from the RESTCounties API
   * the currencyFromCountries method will return the first of those currencies.
   */
  it('Return first currency received from API when response list contains multiple currencies', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
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
    });
    const currency = await currencyFromCountry("Bhutan");
    expect(currency).toEqual({
      "code": "BTN",
      "name": "Bhutanese ngultrum",
      "symbol": "Nu."
    });
    expect(consoleOutput).toEqual([]);
  });


});