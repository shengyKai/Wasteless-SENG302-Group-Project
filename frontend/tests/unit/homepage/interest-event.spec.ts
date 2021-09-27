import Vue from "vue";
import Vuetify from "vuetify";
import {createLocalVue, mount, Wrapper} from "@vue/test-utils";
import Vuex from "vuex";
import {Business} from "@/api/business";
import {Product} from "@/api/product";
import {InventoryItem} from "@/api/inventory";
import {Sale} from "@/api/sale";
import * as events from '@/api/events';
import InterestEvent from "@/components/home/newsfeed/InterestEvent.vue";
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";

Vue.use(Vuetify);

const business: Business = {
  images:[],
  primaryAdministratorId:2,
  address: {
    country:"Iceland",
    streetName:"Racheal Road",
    streetNumber:"711","city":"London",
    district:"New Plymouth District",
    postcode:"42105",
    region:"Southland"
  },
  created:"2021-09-14T01:29:16.496459Z",
  name:"Hillary Cresenct Jewelers",
  description:"Nihil, eveniet aliquid culpa officia aut! Impedit sit sunt quaerat, odit, tenetur error, harum nesciunt.",
  id:1,
  points: 5,
  rank: {
    name: 'bronze',
  },
  businessType:"Accommodation and Food Services"
};
const product: Product = {
  "countryOfSale":"Iceland",
  "images":[],
  "recommendedRetailPrice":842.92,
  "business":business,
  "created":"2021-09-14T01:29:16.497456Z",
  "name":"Complex Omelette",
  "description":"D quam  consequuntur! Commodi minima excepturi repudiandae velit hic maxime doloremque. Quaerat provident commodi consectetur veniam similique ad earum omnis ipsum saepe, volu.",
  "id":"FOXLVWMOW603801",
  "manufacturer":"Hoffman Incorporated"
};
const inventoryItem: InventoryItem = {
  "remainingQuantity":25,
  product,
  "expires":"2022-04-25",
  "quantity":125,
  "pricePerItem":821.43,
  "totalPrice":21357.18,
  "bestBefore":"2021-02-07",
  "sellBy":"2023-02-04",
  "id":1,
  "manufactured":"2022-12-23"
};


const saleItem: Sale = {
  inventoryItem,
  "quantity":1,
  "price":240.30,
  "created":"2021-06-17T08:47:20Z",
  "id":1,
  "moreInfo":"Es pariatur est explicabo fugiat, dolorum eligendi quam cupiditate excepturi mollitia maiores labore suscipit quas? Nulla,.",
  "closes":"2022-03-20",
  "interestCount": 7
};
const event: events.InterestEvent = {
  "id":1,
  "type":"InterestEvent",
  "created":"2021-09-14T01:29:19.731330Z",
  "tag":"none","status":"normal",
  "lastModified":"2021-09-14T01:29:41.214058Z",
  saleItem,
  "interested":true,
  "read":true
};
describe("InterestEvent.vue", () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    wrapper = mount(InterestEvent, {
      localVue,
      vuetify,
      propsData: {
        event,
      },
      stubs: ['FullSaleListing', 'router-link'],
    });
  });



  it("The title contains the product name", ()=>{
    expect(wrapper.text()).toContain("Complex Omelette");
  });

  it("The title contains the business name", ()=>{
    expect(wrapper.text()).toContain("Hillary Cresenct Jewelers");
  });

  it("When the event is not liked, the like count is shown", async ()=>{
    let newEvent = {...event};
    newEvent.interested = false;
    await wrapper.setProps({event:newEvent});
    expect(wrapper.text()).toContain("Like 7");
  });

  it("When the event is liked, the like count is shown", async ()=>{
    expect(wrapper.text()).toContain("Unlike 7");
  });

  const closesDays = [1, 2, 10, 35];

  it.each(closesDays)("Correctly displays the number of days remaining on the sale", async (day)=>{
    let closes = new Date();
    closes.setDate(closes.getDate() + day);
    saleItem.closes = closes.toISOString();
    const newEvent = {...event};
    await wrapper.setProps({
      event: newEvent
    });

    expect(wrapper.text()).toContain(`You have liked this listing which closes in ${day} days`);
  });
});