import Vue from "vue";
import Vuex from "vuex";
import Vuetify from "vuetify";
import { createLocalVue, Wrapper, mount } from "@vue/test-utils";
import CreateSaleItem from "@/components/BusinessProfile/CreateSaleItem.vue";
import { castMock, flushQueue } from "./utils";
import { getStore, resetStoreForTesting } from "@/store";
import * as api from "@/api/internal";

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  createSaleItem: jest.fn(),
}));
const createSaleItem = castMock(api.createSaleItem);
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

describe("CreateSaleItem.vue", () => {
  // Container for the wrapper around CreateSaleItem
  let appWrapper: Wrapper<any>;

  // Container for the CreateSaleItem under test
  let wrapper: Wrapper<any>;

  /**
	 * Sets up the test CreateSaleItem instance
	 */
  beforeEach(() => {
    localVue.use(Vuex);
    const vuetify = new Vuetify();
    jest.clearAllMocks();

    // Creating wrapper around CreateSaleItem with data-app appease vuetify
    const App = localVue.component("App", {
      components: { CreateSaleItem },
      template: "<div data-app><CreateSaleItem/></div>",
    });

    // Put the CreateSaleItem component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement("div");
    document.body.appendChild(elem);

    const testProducts = 
      {
        id: "WATT-420-BEANS",
        name: "Watties Baked Beans - 420g can",
        description: "Baked Beans as they should be.",
        manufacturer: "Heinz Wattie's Limited",
        recommendedRetailPrice: 2.2,
        created: "2021-05-22T02:06:27.767Z",
        images: [
          {
            id: 1234,
            filename: "/media/images/23987192387509-123908794328.png",
            thumbnailFilename: "/media/images/23987192387509-123908794328_thumbnail.png"
          }
        ]
      };

    resetStoreForTesting();
    let store = getStore();
    store.state.createInventoryDialog = 90;
    store.state.createSaleItemDialog = {businessId: 1, inventoryItem: {id: 1, product: testProducts, quantity: 5, remainingQuantity: 3, expires: "somedate"}};

    appWrapper = mount(App, {
      localVue,
      vuetify,
      store: store,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(CreateSaleItem);
  });

  /**
	 * Ensures the CreateSaleItem component is removed from the global document
	 */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
	 * Populates the required fields within the CreateSaleItem form
	 */
  async function populateRequiredFields() {
    await wrapper.setData({
      //TODO: the inventory item will need to be provided
      quantity: 3,
      price: "10" //Need to retrieve this from the inventory item
    });
  }

  /**
	 * Populates all the fields within the CreateSaleItem form
	 */
  async function populateAllFields() {
    await populateRequiredFields();
    await wrapper.setData({
      info: "Today is gonna be a ehhh maybe hopefully a good day",
      closes: "2030-06-21",
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
    async (price) => {
      await populateAllFields();
      await wrapper.setData({
        price,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

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

  it.each(invalidCharacters.concat(whitespaceCharacters).concat(validHugePriceCharacters))(
    'Invalid if PRICE PER ITEM contain space, tab, symbol, other language, number  >= 10000, PRICE PER ITEM = "%s"', async (price) => {
      await populateAllFields();
      await wrapper.setData({
        price,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeFalsy();
    });

  it.each(invalidCharacters.concat(whitespaceCharacters))(
    'Invalid if TOTAL PRICE contain space, tab, symbol, other language, TOTAL PRICE = "%s"',
    async (price) => {
      await populateAllFields();
      await wrapper.setData({
        price,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeFalsy();
    }
  );

  it("calls api endpoint with value when create button pressed", async ()=> {
    await populateRequiredFields();
    await findCreateButton().trigger('click');
    await Vue.nextTick();
    expect(createSaleItem).toBeCalledTimes(1);
  });

  it("Closes the dialog when close button pressed", async ()=> {
    await findCloseButton().trigger('click');
    await  Vue.nextTick();
    expect(createSaleItem).toBeCalledTimes(0);
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });
});