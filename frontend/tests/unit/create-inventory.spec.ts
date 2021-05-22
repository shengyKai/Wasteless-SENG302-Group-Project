import Vue from "vue";
import Vuex from "vuex";
import Vuetify from "vuetify";
import { createLocalVue, Wrapper, mount } from "@vue/test-utils";
import CreateInventory from "@/components/BusinessProfile/CreateInventory.vue";
import { castMock, flushQueue } from "./utils";
import { getStore, resetStoreForTesting } from "@/store";
import * as api from '@/api/internal';

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  createInventoryItem: jest.fn(),
  getProducts: jest.fn()
}));
const createInventoryItem = castMock(api.createInventoryItem);
const getProducts = castMock(api.getProducts);
// Characters that are in the set of letters, numbers, spaces and punctuation.
const validQuantityCharacters = [
  "0",
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
// Characters that are whitespace not including the space character.
const whitespaceCharacters = ["\n", "\t"];

const localVue = createLocalVue();

describe("CreateInventory.vue", () => {
  // Container for the wrapper around CreateInventory
  let appWrapper: Wrapper<any>;

  // Container for the CreateInventory under test
  let wrapper: Wrapper<any>;

  /**
     * Sets up the test CreateInventory instance
     *
     * Because the element we're testing has a v-dialog we need to take some extra sets to make it
     * work.
     */
  beforeEach(() => {
    localVue.use(Vuex);
    const vuetify = new Vuetify();
    jest.clearAllMocks();

    // Creating wrapper around CreateInventory with data-app to appease vuetify
    const App = localVue.component("App", {
      components: { CreateInventory },
      template: "<div data-app><CreateInventory/></div>",
    });

    // Put the CreateInventory component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement("div");
    document.body.appendChild(elem);

    resetStoreForTesting();
    let store = getStore();
    store.state.createInventoryDialog = 90;

    appWrapper = mount(App, {
      localVue,
      vuetify,
      store: store,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(CreateInventory);
  });

  /**
     * Executes after every test case.
     *
     * This function makes sure that the CreateInventory component is removed from the global document
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
      productCode: "ABC-XYZ-012-789",
      quantity: 2,
      expires: "2030-05-17",
    });
  }

  /**
     * Populates all fields of the CreateInventory form
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
  // `findClose and findCreate` function will only be used when api is implemented
  /**
     * Finds the close button in the CreateProduct form
     *
     * @returns A Wrapper around the close button
     */
  function findCloseButton() {
    const buttons = wrapper.findAllComponents({ name: "v-btn" });
    const filtered = buttons.filter((button) =>
      button.text().includes("Close")
    );
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
     * Finds the create button in the CreateProduct form
     *
     * @returns A Wrapper around the create button
     */
  function findCreateButton() {
    const buttons = wrapper.findAllComponents({ name: "v-btn" });
    const filtered = buttons.filter((button) =>
      button.text().includes("Create")
    );
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

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

  it("calls api endpoint with value when create button pressed", async ()=> {
    await populateRequiredFields();
    createInventoryItem.mockResolvedValue(undefined); // pretend everything is A-OK
    await Vue.nextTick();
    await findCreateButton().trigger('click');
    await Vue.nextTick();

    expect(createInventoryItem).toBeCalledWith(90, {
      productId: "ABC-XYZ-012-789",
      quantity: 2,
      expires: "2030-05-17",
    });
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  it("Closes the dialog when close button pressed", async ()=> {
    await findCloseButton().trigger('click');
    await  Vue.nextTick();
    expect(createInventoryItem).toBeCalledTimes(0);
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  it("displays an error code when an error is raised", async ()=>{
    await populateRequiredFields();
    createInventoryItem.mockResolvedValue("Hey there was an error");
    await Vue.nextTick();
    await findCreateButton().trigger('click');
    await Vue.nextTick();
    expect(appWrapper.text()).toContain("Hey there was an error");
    expect(wrapper.emitted().closeDialog).toBeFalsy();
  });

  // it("Product dropdown contains a list of products", async ()=>{
  //   const value = [
  //     {
  //       "id": "WATT-420-BEANS",
  //       "name": "Watties Baked Beans - 420g can",
  //       "description": "Baked Beans as they should be.",
  //       "manufacturer": "Heinz Wattie's Limited",
  //       "recommendedRetailPrice": 2.2,
  //       "created": "2021-05-22T02:06:27.767Z",
  //       "images": [
  //         {
  //           "id": 1234,
  //           "filename": "/media/images/23987192387509-123908794328.png",
  //           "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
  //         }
  //       ]
  //     }
  //   ];
  // });
});
