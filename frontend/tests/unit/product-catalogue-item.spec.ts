//!!!NOTICE!!!
//ProductImageCarousel will not be tested yet because its part of another task for future stories.
//Decided not to test it for now because whoever that is going to do that task in the future may want
//to redo some details if they desire. If you are doing that task, please refer to ProductCatalogueItem
//and ProductImageCarousel for more details.
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import ProductCatalogueItem from '@/components/ProductCatalogueItem.vue';
// import ProductImageCarousel from "@/components/utils/ProductImageCarousel.vue";
import FullProductDescription from "@/components/utils/FullProductDescription.vue";

Vue.use(Vuetify);

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      symbol: 'Currency symbol'
    };
  })
}));

describe('ProductCatalogueItem.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  /**
   * Set up to test the routing and whether the Product Catalogue item component shows what is required
   */
  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append (app);

    wrapper = mount(ProductCatalogueItem, {
      localVue,
      vuetify,
      components: {
        //ProductImageCarousel will not be tested yet because its part of another task.
        // ProductImageCarousel,
        FullProductDescription
      },
      stubs: {
        //stub ProductImageCarousel because its not related to this test for now.
        //remove stub if testing ProductCatalogueItem as a whole
        ProductImageCarousel: true
      },
      //Sets up each test case with some values to ensure the Product Catalogue item component works as intended
    });
    await wrapper.setProps({
      product: {
          name: "Some Product",
          description: "Some description",
          created: "Some Date Added",
          manufacturer: "Some Manufacturer",
          recommendedRetailPrice: 100,
          id: "Some Code",
          readMoreActivated: false
      }
    });
    await wrapper.setData({
      currency: {
        code: "Currency code",
        symbol: "Currency symbol"
      },
    })
  });

  /**
  * Tests that the same product name exists as per the set data above
  */
  it("Must contain the product name", () => {
    expect(wrapper.text()).toContain('Some Product');
  });

  /**
  * Tests that the same product description exists as per the set data above
  */
  it("Must contain the product description", () => {
    expect(wrapper.text()).toContain('Some description');
  });

  /**
   * Tests the full user sequence when product description is above 50 characters
   */
  it("Must open dialog box with full product description upon clicking 'Read more...'", async () => {
    await wrapper.setProps({
      product: {
        description: "Some super long description Some super long description Some super long description Some super long description"
      }
    });
    //the description will cut off at the 50th character
    expect(wrapper.text()).toContain(wrapper.vm.product.description.slice(0,50));
    //Full description should not exist
    expect(wrapper.text()).not.toContain(wrapper.vm.product.description);
    Vue.nextTick(() => {
      let productDescriptionComponent = wrapper.findComponent(FullProductDescription);
      //if the component found is not null, means the component exists to be able to read the full description
      expect(productDescriptionComponent).not.toBeNull();
      //value of dialog should be false initially
      expect(productDescriptionComponent.vm.$data.dialog).toBeFalsy();
      //at index 0, the link is the "Read more..." link
      productDescriptionComponent.findAll('a').at(0).trigger("click");
      //wait to let the dialog box load
      Vue.nextTick(() => {
        //now the dialog should be true
        expect(productDescriptionComponent.vm.$data.dialog).toBeTruthy();
        expect(productDescriptionComponent.text()).toContain(wrapper.vm.product.description);
        //at index 1, the link is the "return" link
        productDescriptionComponent.findAll('a').at(1).trigger("click");
        expect(productDescriptionComponent.vm.$data.dialog).toBeFalsy();
      });
    });
  });

  /**
  * Tests that the same product date added exists as per the set data above
  */
  it("Must contain the product date added", () => {
    expect(wrapper.text()).toContain('Some Date Added');
  });

  /**
  * Tests that the same product manufacturer exists as per the set data above
  */
  it("Must contain the product manufacturer", () => {
    expect(wrapper.text()).toContain('Some Manufacturer');
  });

  /**
  * Tests that the same product RRP exists as per the set data above
  */
  it("Must contain the product RRP", () => {
    expect(wrapper.text()).toContain(100);
  });

  /**
   * Test that the product RRP is formatted with the currency symbol and code
   */
  it("RRP must be formatted with symbol and code", () => {
    expect(wrapper.text()).toContain("Currency symbol100 Currency code");
  });

  /**
  * Tests that the same product code exists as per the set data above
  */
  it("Must contain the product code", () => {
    expect(wrapper.text()).toContain("Some Code");
  });
});