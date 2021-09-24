import Vue from "vue";
import Vuetify from "vuetify";
import {createLocalVue, mount, Wrapper} from "@vue/test-utils";
import Vuex from "vuex";
import InterestPurchasedEvent from "@/components/home/newsfeed/InterestPurchasedEvent.vue";
import { Business } from "@/api/business";
import { Product } from "@/api/product";
import { BoughtSale } from "@/api/sale";
import * as events from '@/api/events';

Vue.use(Vuetify);

const business: Business = {
  images:[],
  primaryAdministratorId:2,
  address:{
    country:"Iceland",
    streetName:"Racheal Road",
    streetNumber:"711","city":"London",
    district:"New Plymouth District",
    postcode:"42105",
    region:"Southland"},
  created:"2021-09-14T01:29:16.496459Z",
  name:"Hillary Cresenct Jewelers",
  description:"Nihil, eveniet aliquid culpa officia aut! Impedit sit sunt quaerat, odit, tenetur error, harum nesciunt.",
  id:1,
  businessType:"Accommodation and Food Services"
};
const product: Product = {
  countryOfSale: "Iceland",
  images:[],
  recommendedRetailPrice:842.92,
  business,
  created:"2021-09-14T01:29:16.497456Z",
  name:"Complex Omelette",
  description:"D quam  consequuntur! Commodi minima excepturi repudiandae velit hic maxime doloremque. Quaerat provident commodi consectetur veniam similique ad earum omnis ipsum saepe, volu.",
  id:"FOXLVWMOW603801",
  manufacturer:"Hoffman Incorporated"
};

const boughtSaleItem: BoughtSale = {
  id: 2,
  buyer: null,
  interestCount: 100,
  quantity: 7,
  product,
  price: 50,
  listingDate: "2021-09-14T01:29:16.497456Z",
  saleDate: "2021-09-14T01:29:16.496459Z",
};

const event: events.InterestPurchasedEvent = {
  id: 1,
  type: "InterestPurchasedEvent",
  created: "2021-09-14T01:29:19.731330Z",
  tag: "none",
  status: "normal",
  lastModified: "2021-09-14T01:29:41.214058Z",
  read: true,
  boughtSaleItem,
};

describe("InterestEvent.vue", () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    wrapper = mount(InterestPurchasedEvent, {
      localVue,
      vuetify,
      propsData: {
        event,
      },
      stubs: ['router-link'],
    });
  });

  it("The event contains the product name", () => {
    expect(wrapper.text()).toContain("Complex Omelette");
  });

  it("The event contains the business name", () => {
    expect(wrapper.text()).toContain("Hillary Cresenct Jewelers");
  });

  it('Matches snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });
});