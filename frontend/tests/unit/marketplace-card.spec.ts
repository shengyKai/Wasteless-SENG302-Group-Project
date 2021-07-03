
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import MarketplaceCard from '@/components/cards/MarketplaceCard.vue';

import { deleteMarketplaceCard, User } from '@/api/internal';

jest.mock('@/api/internal', () => ({
  deleteMarketplaceCard: jest.fn(),
}));

Vue.use(Vuetify);

const localVue = createLocalVue();

const testUser: User = {
  id: 2,
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  email: 'test_email',
  homeAddress: { country: 'test_country', city: 'test_city', district: 'test_district'},
};

const testMarketplaceCard = {
  id: 1,
  creator: testUser,
  section: 'ForSale',
  created: '2021-03-10',
  title: 'test_card_title',
  description: 'test_card_description',
  keywords: [{id: 3, name: 'test_keyword_1'}, {id: 4, name: 'test_keyword_2'}],
};

describe('MarketplaceCard.vue', () => {
  let appWrapper: Wrapper<any>;
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  /**
   * Finds the required button in the MarketplaceCard card by specifying the
   * button text in the button.
   * @returns A Wrapper around the required button
   */
  function findButton(buttonText: string, specifiedWrapper: Wrapper<any>) {
    const buttons = specifiedWrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes(buttonText));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the delete confirmation dialog box upon clicking the delete button
   * @returns the delete confirmation dialog box
   */
  async function findDeleteConfirmationDialog() {
    const deleteButton = wrapper.findComponent({ ref: 'deleteButton' });
    await deleteButton.trigger('click');

    const dialogs = wrapper.findAllComponents({ name: "v-dialog" });
    return dialogs.at(0)
  }

  beforeEach(() => {
    vuetify = new Vuetify();

    // Creating wrapper around MarketplaceCard with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { MarketplaceCard },
      template: `
      <div data-app>
        <MarketplaceCard :content="testMarketplaceCard"/>
      </div>`
    });

    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      data() {
        return {
          testMarketplaceCard: testMarketplaceCard
        };
      }
    });

    wrapper = appWrapper.getComponent(MarketplaceCard);
  });

  it('Must match snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });

  it("Must contain the creator name", () => {
    expect(wrapper.text()).toContain('By test_firstname test_lastname');
  });

  it("Must contain the creator address with suburb and city if provided", () => {
    expect(wrapper.text()).toContain('From test_district, test_city');
  });

  it("Must contain the creator address with city and country if provided", async () => {
    await wrapper.setData({
      content: {
        creator: {
          homeAddress: {
            district: undefined,
            city: 'test_city',
            country: 'test_country',
          }
        }
      },
    });
    await Vue.nextTick();
    expect(wrapper.text()).toContain('From test_city, test_country');
  });

  it("Must contain the creator country if only field provided", async () => {
    await wrapper.setData({
      content: {
        creator: {
          homeAddress: {
            district: undefined,
            city: undefined,
            country: 'test_country',
          }
        }
      },
    });
    await Vue.nextTick();
    expect(wrapper.text()).toContain('From test_country');
  });

  it("Must contain the title", () => {
    expect(wrapper.text()).toContain('test_card_title');
  });

  it("Must contain the description", () => {
    expect(wrapper.text()).toContain('test_card_description');
  });

  it("Must contain keyword names", () => {
    expect(wrapper.text()).toContain("test_keyword_1");
    expect(wrapper.text()).toContain("test_keyword_2");
  });

  it("Must contain posted date", () => {
    expect(wrapper.text()).toContain('Posted 10 Mar 2021');
  });

  it("Must trigger delete confirmation dialog box upon clicking delete icon", async () => {
    expect(wrapper.vm.deleteCardDialog).toBeFalsy();
    //This button is an icon, so a reference is used to identify it instead of its button text
    const deleteButton = wrapper.findComponent({ ref: 'deleteButton' });
    await deleteButton.trigger('click');
    expect(wrapper.vm.deleteCardDialog).toBeTruthy();
  });

  it("The deleteMarketplaceCard method must be called and the dialog box should not be visible, upon clicking the delete button in the confirmation dialog box", async () => {
    const deleteConfirmationDialog = await findDeleteConfirmationDialog();
    const dialogDeleteButton = findButton('Delete', deleteConfirmationDialog);
    await dialogDeleteButton.trigger("click");
    expect(deleteMarketplaceCard).toBeCalledWith(testMarketplaceCard.id);
    expect(wrapper.vm.deleteCardDialog).toBeFalsy();
  });

  it("The dialog box should not be visible if the cancel button is clicked in the confirmation dialog box", async () => {
    const deleteConfirmationDialog = await findDeleteConfirmationDialog();
    const dialogCancelButton = findButton('Cancel', deleteConfirmationDialog);
    await dialogCancelButton.trigger("click");
    expect(wrapper.vm.deleteCardDialog).toBeFalsy();
  });

  
});
