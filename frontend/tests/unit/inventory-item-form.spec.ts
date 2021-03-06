import Vue from "vue";
import Vuex from "vuex";
import Vuetify from "vuetify";
import {createLocalVue, mount, Wrapper} from "@vue/test-utils";
import InventoryItemForm from "@/components/BusinessProfile/InventoryItemForm.vue";
import {castMock, flushQueue, todayPlusYears, findButtonWithText} from "./utils";
import {getProducts as getProducts1} from "@/api/product";
import {
  createInventoryItem as createInventoryItem1,
  modifyInventoryItem as modifyInventoryItem1
} from "@/api/inventory";

Vue.use(Vuetify);

// Makes sure that fetching the currency doesn't crash
jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn().mockResolvedValue({
    code: 'NZD',
    name: 'New Zealand dollar',
    symbol: '$',
  }),
}));

jest.mock('@/api/inventory', () => ({
  createInventoryItem: jest.fn(),
  modifyInventoryItem: jest.fn(),
}));
jest.mock('@/api/product', () => ({
  getProducts: jest.fn(),
}));
jest.mock('@/api/business', () => ({
  getBusiness: jest.fn().mockReturnValue({address: {}}), // Makes sure that fetching the currency doesn't crash
}));
const createInventoryItem = castMock(createInventoryItem1);
const modifyInventoryItem = castMock(modifyInventoryItem1);
const getProducts = castMock(getProducts1);

// Characters that are in the set of letters, numbers, spaces and punctuation.
const validQuantityCharacters = [
  "11",
  "2123",
  "1231423",
  "987654321",
  "123",
  "21233",
  "412345",
  "98765432",
];
// Characters that are in the set of number, decimal, number with decimal
const validPriceCharacters = [
  "0",
  "0.00",
  "1",
  "1.11",
  "99.99",
  "100",
  "9999",
  "001",
];
// Characters that are in the set of number, decimal, number with decimal
const validHugePriceCharacters = [
  "10000",
  "999999",
  "123765",
  "23546.00",
  "888888.12",
];
// Characters that are not a letter, number, space or punctuation.
const invalidCharacters = [
  "\uD83D\uDE02",
  "♔",
  " ",
  ":",
  ",",
  "é",
  "树",
  "A",
  "-1",
  "-99",
];
const testProducts = [
  {
    "id": "WATT-420-BEANS",
    "name": "Watties Baked Beans - 420g can",
    "description": "Baked Beans as they should be.",
    "manufacturer": "Heinz Wattie's Limited",
    "recommendedRetailPrice": 2.2,
    "created": "2021-05-22T02:06:27.767Z",
    "images": [
      {
        "id": 1234,
        "filename": "/media/images/23987192387509-123908794328.png",
        "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
      }
    ]
  }
];
// Characters that are whitespace not including the space character.
const whitespaceCharacters = ["\n", "\t"];

getProducts.mockResolvedValue({results: testProducts, count: testProducts.length});

const localVue = createLocalVue();

describe("InventoryItemForm.vue", () => {
  // Container for the wrapper around InventoryItemForm
  let appWrapper: Wrapper<any>;

  // Container for the InventoryItemForm under test
  let wrapper: Wrapper<any>;

  /**
     * Sets up the test InventoryItemForm instance
     *
     * Because the element we're testing has a v-dialog we need to take some extra sets to make it
     * work.
     */
  beforeEach(() => {
    localVue.use(Vuex);
    const vuetify = new Vuetify();

    // Creating wrapper around InventoryItemForm with data-app to appease vuetify
    const App = localVue.component("App", {
      components: { InventoryItemForm },
      template: "<div data-app><InventoryItemForm :businessId=\"90\"/></div>",
    });

    // Put the InventoryItemForm component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement("div");
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(InventoryItemForm);
  });

  /**
     * Executes after every test case.
     *
     * This function makes sure that the ItemFormInventory component is removed from the global document
     */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
     * Adds all the fields that are required for the create Inventory form to be valid
     *
     * These are:
     * - Inventory shortcode
     * - Item quantity
     * - Item expires
     */
  async function populateRequiredFields() {
    await wrapper.setData({
      productCode: "WATT-420-BEANS",
      quantity: 2,
      expires: "2030-05-17",
    });
  }

  /**
     * Populates all fields of the InventoryItem form
     *
     * Which include the inventory's:
     * - Inventory shortcode
     * - Item quantity
     * - Item expires
     * - PricePerItem
     * - TotalPrice
     * - Manufactured
     * - Sell By
     * - Best Before
     */
  async function populateAllFields() {
    await populateRequiredFields();
    await wrapper.setData({
      pricePerItem: "12",
      totalPrice: "20",
      manufactured: "2020-05-17",
      sellBy: "2030-05-17",
      bestBefore: "2030-05-17",
    });
  }

  /**
     * Finds the create button in the inventory item form
     *
     * @returns A Wrapper around the create button
     */
  const findCreateButton = () => findButtonWithText(wrapper, 'Create');

  /**
     * Finds the save button in the inventory item form
     *
     * @returns A Wrapper around the save button
     */
  const findSaveButton = () => findButtonWithText(wrapper, 'Save');

  /**
   * Finds the product select dropdown
   *
   * @returns A Wrapper around the product select dropdown
   */
  function findProductSelect() {
    const select = wrapper.findAllComponents({name:"v-select"});
    expect(select.length).toBe(1);
    return select.at(0);
  }

  it("Valid if all required fields are provided", async () => {
    await populateRequiredFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it("Valid if all fields are provided", async () => {
    await populateAllFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  //TRUTHY section follow by ORDER : QUANTITY, PRICE PER ITEM , TOTAL PRICE

  it.each(validQuantityCharacters)(
    'Valid when quantity contain numbers from 1 to 9 digit, QUANTITY = "%s"',
    async (quantity) => {
      await populateAllFields();
      await wrapper.setData({
        quantity,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

  it('Invalid when quantity is 0', async () => {
    await populateAllFields();
    await wrapper.setData({
      quantity: 0
    });
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid when quantity is -1', async () => {
    await populateAllFields();
    await wrapper.setData({
      quantity: -1
    });
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid when quantity is -10000', async () => {
    await populateAllFields();
    await wrapper.setData({
      quantity: -10000
    });
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it.each(validPriceCharacters)(
    'Valid when PRICE PER ITEM contain valid price [e.g 999 or 999.99] & <10000, Price per Item =  "%s"',
    async (pricePerItem) => {
      await populateAllFields();
      await wrapper.setData({
        pricePerItem,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

  it.each(validPriceCharacters.concat(validHugePriceCharacters))(
    'Valid when TOTAL PRICE contain valid price [e.g 999 or 999.99] & <10000, TOTAL PRICE =  "%s"',
    async (totalPrice) => {
      await populateAllFields();
      await wrapper.setData({
        totalPrice,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

  //FALSY section follow by ORDER : QUANTITY, PRICE PER ITEM, TOTAL PRICE

  it.each(invalidCharacters.concat(whitespaceCharacters))(
    'invalid if QUANTITY contain space, tab, symbol, other language QUANTITY = "%s"',
    async (quantity) => {
      await populateAllFields();
      await wrapper.setData({
        quantity,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeFalsy();
    }
  );
  it.each(
    invalidCharacters
      .concat(whitespaceCharacters)
      .concat(validHugePriceCharacters)
  )('Invalid if PRICE PER ITEM contain space, tab, symbol, other language, number  >= 10000, PRICE PER ITEM = "%s"', async (pricePerItem) => {
    await populateAllFields();
    await wrapper.setData({
      pricePerItem,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it.each(invalidCharacters.concat(whitespaceCharacters))(
    'Invalid if TOTAL PRICE contain space, tab, symbol, other language, TOTAL PRICE = "%s"',
    async (totalPrice) => {
      await populateAllFields();
      await wrapper.setData({
        totalPrice,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeFalsy();
    }
  );

  it('Currency contains an error message if product code is not set', () => {
    expect(wrapper.vm.productCode).toBeFalsy();
    expect(wrapper.vm.currency).toStrictEqual({errorMessage: "Currency not available"});
  });

  it('Currency contains the result of a call to currencyFromCountry() if product code is set', async () => {
    await wrapper.setData({
      productCode: "WATT-420-BEANS"
    });
    await Vue.nextTick();
    expect(wrapper.vm.currency).toStrictEqual({
      code: 'NZD',
      name: 'New Zealand dollar',
      symbol: '$',
    });
  });

  describe("Date Validation", () => {
    it("Valid when all date fields are today", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(0);
      let sellByDate = todayPlusYears(0);
      let bestBeforeDate = todayPlusYears(0);
      let expiresDate = todayPlusYears(0);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate,
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Valid when manufactured date before today", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(-1);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when manufactured date after today", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(1);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when manufactured date before sell by date", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(-1);
      let sellByDate = todayPlusYears(1);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when manufactured date after sell by date", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(-1);
      let sellByDate = todayPlusYears(-2);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when manufactured date before 1000 AD", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(-1500);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when manufactured date after 10000 AD", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(10000);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when sell by date after today", async () => {
      await populateRequiredFields();
      let sellByDate = todayPlusYears(1);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when sell by date before today", async () => {
      await populateRequiredFields();
      let sellByDate = todayPlusYears(-1);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when sell by date after manufactured date", async () => {
      await populateRequiredFields();
      let manufacturedDate = todayPlusYears(-1);
      let sellByDate = todayPlusYears(1);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Valid when sell by date before best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(2);
      let sellByDate = todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when sell by date after best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(1);
      let sellByDate = todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when sell by date before expiry date", async () => {
      await populateRequiredFields();
      let expiryDate = todayPlusYears(2);
      let sellByDate = todayPlusYears(1);
      await wrapper.setData({
        expires: expiryDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when sell by date after expiry date", async () => {
      await populateRequiredFields();
      let expiryDate = todayPlusYears(1);
      let sellByDate = todayPlusYears(2);
      await wrapper.setData({
        expires: expiryDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when sell by date before 1000 AD", async () => {
      await populateRequiredFields();
      let sellByDate = todayPlusYears(-1500);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when sell by date after 10000 AD", async () => {
      await populateRequiredFields();
      let sellByDate = todayPlusYears(10000);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when best before date after today", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when best before date before today", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(-1);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when best before date after sell by date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(2);
      let sellByDate = todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when best before date before sell by date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(1);
      let sellByDate = todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when best before date before expires date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(1);
      let expiresDate = todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when best before date after expires date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(2);
      let expiresDate = todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when best before date before 1000 AD", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(-1500);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when best before date after 10000 AD", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(10000);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when expires date after today", async () => {
      await populateRequiredFields();
      let expiresDate = todayPlusYears(1);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when expires date before today", async () => {
      await populateRequiredFields();
      let expiresDate = todayPlusYears(-1);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when expires date after best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(1);
      let expiresDate = todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when expires date before best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = todayPlusYears(2);
      let expiresDate = todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when expires date before 1000 AD", async () => {
      await populateRequiredFields();
      let expiresDate = todayPlusYears(-1500);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when expires date after 10000 AD", async () => {
      await populateRequiredFields();
      let expiresDate = todayPlusYears(10000);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkAllDatesValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });
  });

  //Needs addressed with bug associated with t170
  it("Product dropdown contains a list of products", async ()=>{
    await wrapper.setData({productList:testProducts});
    await Vue.nextTick();
    const selectbox = findProductSelect();
    (selectbox.vm as any).activateMenu();
    await flushQueue();
    expect(appWrapper.text()).toContain("Watties");
  });

  it("Product dropdown contains a search box", async ()=>{
    const selectbox = findProductSelect();
    (selectbox.vm as any).activateMenu();
    await Vue.nextTick();
    const input = selectbox.findComponent({name:"v-text-field"});
    expect(input.exists()).toBeTruthy();
  });

  //Associated with bug on t170
  it('Product search limits results', async ()=>{
    await wrapper.setData({productList:testProducts});
    // getProducts.mockResolvedValue(testProducts);
    const selectbox = findProductSelect();
    (selectbox.vm as any).activateMenu();
    await flushQueue();
    expect(appWrapper.text()).toContain("Watties");
    await wrapper.setData({productFilter: "Not watties"});
    await Vue.nextTick();
    expect(appWrapper.text()).not.toContain("Watties");
  });

  describe('Form is being used to create an inventory item', () => {

    beforeEach(() => {
      expect(wrapper.props().previousItem).toBe(undefined);
      expect(wrapper.vm.isCreate).toBe(true);
    });

    it('Quantity will be valid', async () => {
      await populateRequiredFields();
      expect(wrapper.vm.checkQuantityValid()).toBeTruthy();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it('createInventoryItem called when create button pressed', async () => {
      const formData = {
        productCode: "WATT-420-BEANS",
        quantity: 4,
        pricePerItem: "3.00",
        totalPrice: "12.50",
        manufactured: "2020-07-11",
        sellBy: "2030-07-11",
        bestBefore: "2030-07-11",
        expires: "2030-07-11"
      };
      await wrapper.setData(formData);
      await Vue.nextTick();
      await findCreateButton().trigger('click');
      expect(createInventoryItem.mock.calls.length).toBe(1);
    });

    it('createInventoryItem called with data entered into form as arguments when create button pressed', async () => {
      const formData = {
        productCode: "WATT-420-BEANS",
        quantity: 4,
        pricePerItem: "3.00",
        totalPrice: "12.50",
        manufactured: "2020-07-11",
        sellBy: "2030-07-11",
        bestBefore: "2030-07-11",
        expires: "2030-07-11"
      };
      const expectedData = {
        productId: formData.productCode,
        quantity: formData.quantity,
        pricePerItem: formData.pricePerItem,
        totalPrice: formData.totalPrice,
        manufactured: formData.manufactured,
        sellBy: formData.sellBy,
        bestBefore: formData.bestBefore,
        expires: formData.expires,
      };
      await wrapper.setData(formData);
      await Vue.nextTick();
      await findCreateButton().trigger('click');
      expect(createInventoryItem.mock.calls[0][0]).toBe(90);
      expect(createInventoryItem.mock.calls[0][1]).toStrictEqual(expectedData);
    });

    it('Error message is shown if API request is unsuccessful', async () => {
      createInventoryItem.mockResolvedValueOnce('ERROR!');
      await populateAllFields();
      await Vue.nextTick();
      await findCreateButton().trigger('click');
      await Vue.nextTick();
      expect(wrapper.vm.errorMessage).toBe('ERROR!');
      expect(wrapper.emitted().closeDialog).toBeFalsy();
    });

    it('Dialog is closed if API request is successful', async() => {
      createInventoryItem.mockResolvedValueOnce(undefined);
      await populateAllFields();
      await Vue.nextTick();
      await findCreateButton().trigger('click');
      await Vue.nextTick();
      expect(wrapper.vm.errorMessage).toBe(undefined);
      expect(wrapper.emitted().closeDialog).toBeTruthy();
    });

  });

  describe('Form is being used to modify an inventory item', () => {

    const formData = {
      productCode: "WATT-420-BEANS",
      quantity: 77,
      pricePerItem: "3.00",
      totalPrice: "12.50",
      manufactured: "2020-07-11",
      sellBy: "2030-07-11",
      bestBefore: "2030-07-11",
      expires: "2030-07-11",
    };

    beforeEach(async () => {
      const vuetify = new Vuetify();
      // Creating wrapper around InventoryItemForm with data-app to appease vuetify
      const App = localVue.component("App", {
        components: { InventoryItemForm },
        template: "<div data-app><InventoryItemForm :businessId=\"businessId\" :previousItem=\"previousItem\"/></div>",
      });

      // Put the InventoryItemForm component inside a div in the global document,
      // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
      const elem = document.createElement("div");
      document.body.appendChild(elem);

      appWrapper = mount(App, {
        localVue,
        vuetify,
        attachTo: elem,
        data() {
          return {
            businessId: 90,
            previousItem: {
              id: 33,
              product: {
                id: "WATT-420-BEANS",
              },
              quantity: 47,
              remainingQuantity: 20,
              pricePerItem: "3.00",
              totalPrice: "12.50",
              manufactured: "2020-07-11",
              sellBy: "2030-07-11",
              bestBefore: "2030-07-11",
              expires: "2030-07-11",
            }
          };
        }
      });

      wrapper = appWrapper.getComponent(InventoryItemForm);

      await Vue.nextTick();
      expect(wrapper.vm.isCreate).toBe(false);

      await wrapper.setData(formData);
      await Vue.nextTick();
    });

    afterEach(() => {
      appWrapper.destroy();
    });

    it('Quantity will be valid if it is greater than the amount used in sale listings for this inventory item', async () => {
      await wrapper.setData({
        previousItem: {
          quantity: 47,
          remainingQuantity: 20,
        },
        quantity: 60,
      });
      expect(wrapper.vm.checkQuantityValid()).toBeTruthy();
      expect(findSaveButton().props().disabled).toBeFalsy();
    });

    it('Quantity will be valid if it is equal to the amount used in sale listings for this inventory item', async () => {
      await wrapper.setData({
        previousItem: {
          quantity: 47,
          remainingQuantity: 20,
        },
        quantity: 27,
      });
      expect(wrapper.vm.checkQuantityValid()).toBeTruthy();
      expect(findSaveButton().props().disabled).toBeFalsy();
    });

    it('Quantity will be invalid if it is less than the amount used in sale listings for this inventory item', async () => {
      await wrapper.setData({
        previousItem: {
          quantity: 47,
          remainingQuantity: 20,
        },
        quantity: 26,
      });
      expect(wrapper.vm.checkQuantityValid()).toEqual("Must be at least 27");
      expect(findSaveButton().props().disabled).toBeTruthy();
    });

    it('modifyInventoryItem called when save button pressed', async () => {
      await findSaveButton().trigger('click');
      expect(modifyInventoryItem.mock.calls.length).toBe(1);
    });

    it('modifyInventoryItem called with data entered into form as arguments when save button pressed', async () => {
      const expectedData = {
        productId: formData.productCode,
        quantity: formData.quantity,
        pricePerItem: formData.pricePerItem,
        totalPrice: formData.totalPrice,
        manufactured: formData.manufactured,
        sellBy: formData.sellBy,
        bestBefore: formData.bestBefore,
        expires: formData.expires,
      };
      await findSaveButton().trigger('click');
      expect(modifyInventoryItem.mock.calls[0][0]).toBe(90);
      expect(modifyInventoryItem.mock.calls[0][1]).toBe(33);
      expect(modifyInventoryItem.mock.calls[0][2]).toStrictEqual(expectedData);
    });

    it('Error message is shown if API request is unsuccessful', async () => {
      modifyInventoryItem.mockResolvedValueOnce('ERROR!');
      await findSaveButton().trigger('click');
      await Vue.nextTick();
      expect(wrapper.vm.errorMessage).toBe('ERROR!');
      expect(wrapper.emitted().closeDialog).toBeFalsy();
    });

    it('Dialog is closed if API request is successful', async() => {
      modifyInventoryItem.mockResolvedValueOnce(undefined);
      await findSaveButton().trigger('click');
      await Vue.nextTick();
      expect(wrapper.vm.errorMessage).toBe(undefined);
      expect(wrapper.emitted().closeDialog).toBeTruthy();
    });

  });
});
