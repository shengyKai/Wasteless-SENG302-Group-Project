import { getCookie, getLocalDate, trimToLength } from "@/utils";


describe('utils.ts', () => {
  it('getLocalDate returns expected value', () => {
    const local = '2012-12-21';
    expect(getLocalDate(new Date(local))).toBe(local);
  });

  it.each([
    ['Hello world', 4, 'Hell' ],
    ['Hello world', 8, 'Hello' ],
    ['Hello world', 50, 'Hello world' ],
    ['a'.repeat(10), 5, 'a'.repeat(5) ],
    ['', 5, '' ],
    ['Hello world', 0, '' ],
  ])('Trimming "%s" to %i characters should return "%s"', (input, length, expected) => {
    const actual = trimToLength(input, length);
    expect(actual).toBe(expected);
  });

  describe('cookie tests', () => {
    const cookieString = 'test=7;test2=value;test3=that';
    const cookieStringWithSpaces = 'test=7; test2=value; test3=that';
    const cookieMapping: [string, string | null][] = [
      ['test', 'test=7'],
      ['test2', 'test2=value'],
      ['test3', 'test3=that'],
      ['nowhere', null],
    ];

    let originalDocument: Document;
    const cookieGetter = jest.fn();
    const cookieSetter = jest.fn();

    /**
     * Replaces the document with a mock version
     */
    beforeAll(() => {
      originalDocument = globalThis.document;
      globalThis.document = {} as Document;

      Object.defineProperty(globalThis.document, 'cookie', {
        get: cookieGetter,
        set: cookieSetter,
      });
    });

    /**
     * Restores the document
     */
    afterAll(() => {
      globalThis.document = originalDocument;
    });

    /**
     * Makes sure the mocks are clean
     */
    beforeEach(() => {
      jest.resetAllMocks();
    });

    it.each(cookieMapping)('Cookie with key "%s" should be parsed from a cookie string without spaces', (key: string, value: string | null) => {
      cookieGetter.mockReturnValue(cookieString);
      expect(getCookie(key)).toBe(value);
      expect(cookieGetter).toBeCalled();
      expect(cookieSetter).not.toBeCalled();
    });

    it.each(cookieMapping)('Cookie with key "%s" should be parsed from a cookie string without spaces', (key: string, value: string | null) => {
      cookieGetter.mockReturnValue(cookieStringWithSpaces);
      expect(getCookie(key)).toBe(value);
      expect(cookieGetter).toBeCalled();
      expect(cookieSetter).not.toBeCalled();
    });
  });
});