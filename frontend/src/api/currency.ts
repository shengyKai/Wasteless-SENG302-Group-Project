import { MaybeError } from "@/api/internal";

/**
 * Currency information for a specific country.
 */
export type Currency = {
  code: string,
  name: string,
  symbol: string
};

type CurrencyOrError = Currency | { errorMessage: string }

/**
 * An object which only has the attribute 'currencies', which is a list of Currency objects. The API response is expected
 * to contain an array of objects of this type
 */
type CurrenciesContainer = {
  currencies: Currency[];
};

/**
 * Determines if the given object is an instance of Currency.
 * @param obj The object to be checked.
 * @returns True if the object has the attributes of a Currency object, false otherwise.
 */
function isCurrency(obj: any): obj is Currency {
  if (obj === null || typeof obj !== 'object') return false;
  if (typeof obj.code !== 'string') return false;
  if (typeof obj.name !== 'string') return false;
  if (typeof obj.symbol !== 'string') return false;
  return true;
}

/**
 * Determines if the given object is an array of object of type Currency.
 * @param obj The object to be checked.
 * @returns True if the object is an array where all elements are Currency objects, false otherwise.
 */
function isCurrencyArray(obj: any): obj is Currency[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (!isCurrency(elem)) return false;
  }
  return true;
}

/**
 * Check that the body of the API response has the expected format. It should be an array of length 1, where they
 * entry in the array is a JSON with a "currencies" field, and that field contains an array of currency objects.
 * @param response The body of the response received from the API.
 * @returns True if the response is in the expected format, false otherwise.
 */
function currencyResponseHasExpectedFormat(response: any): response is [CurrenciesContainer] {
  if (!Array.isArray(response)) return false;
  if (response.length !== 1) return false;
  const country = response[0];
  if (!isCurrencyArray(country.currencies)) return false;
  return true;
}

/**
 * Make a request to the RESTCounties API to find the currency associated with the given country name.
 * If the request is successful the currency from the API will be returned. If it is unsuccessful then
 * a default currency object with blank code, name and symbol fields will be returned.
 * @param country The name of a country to use in the API request for the currency.
 * @returns A list containing the Currency of the country(defaultCurrency if any errors occur) at index 0,
 * and an error message at index 1 if any errors occur.
 */
export async function currencyFromCountry(country: string): Promise<CurrencyOrError> {

  const response = await queryCurrencyAPI(country);

  if (typeof response === 'string') {
    console.warn(response);
    return { errorMessage: response };
  }

  const currency = await getCurrencyFromAPIResponse(response);

  if (typeof currency === 'string') {
    console.warn(currency);
    return { errorMessage: currency };
  }

  return currency;
}

/**
 * This method takes a string with the name of a country and queries the RESTCountries API to find the
 * currency associated with that country. The API's response will be returned if a response with status
 * code 200 is received, otherwise an error message will be returned.
 * @param country The name of the country to query the API for.
 * @return the response received from the RESTCounties API or a string error message.
 */
export async function queryCurrencyAPI(country: string): Promise<MaybeError<Response>> {

  const queryUrl = `https://restcountries.eu/rest/v2/name/${country}?fullText=true&fields=currencies`;

  const response = await fetch(queryUrl)
    .catch(error => {
      return `Failed to reach ${queryUrl}`;
    });
  if (typeof response === 'string') {
    return response;
  }
  if (response.status === 404) {
    return `No country with name ${country} was found`;
  }
  if (response.status !== 200) {
    return `Request failed: ${response.status}`;
  }
  return response;
}

/**
 * This method checks the format of the API response and extracts a currency object from the JSON body
 * if the response format is correct. If it is not correct then an error message is returned.
 * @param response A currency object extracted from the response body or an error message.
 */
export async function getCurrencyFromAPIResponse(response: Response): Promise<MaybeError<Currency>> {
  const responseBody = await response.json();

  if (!currencyResponseHasExpectedFormat(responseBody)) {
    return 'API response was not in readable format';
  }

  return responseBody[0].currencies[0];
}