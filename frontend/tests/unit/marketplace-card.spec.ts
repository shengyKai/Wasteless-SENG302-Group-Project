
import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import MarketplaceCard from '@/components/cards/MarketplaceCard.vue';

import { deleteMarketplaceCard, messageConversation, MarketplaceCardSection, User } from '@/api/internal';
import { flushQueue } from './utils';
import { SECTION_NAMES } from '@/utils';

jest.mock('@/api/internal', () => ({
  deleteMarketplaceCard: jest.fn(),
  messageConversation: jest.fn(),
}));

Vue.use(Vuetify);

const localVue = createLocalVue();
localVue.use(Vuex);

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
  lastRenewed: '2021-03-10',
  title: 'test_card_title',
  description: 'test_card_description',
  keywords: [{id: 3, name: 'test_keyword_1'}, {id: 4, name: 'test_keyword_2'}],
};

describe('MarketplaceCard.vue', () => {
  let appWrapper: Wrapper<any>;
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  let getters: any;
  let state: Object;
  let store: any;

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
   * Finds the edit form dialog box upon clicking the edit button
   * @returns the edit confirmation dialog box
   */
  async function openEditConfirmationDialog() {
    const editButton = wrapper.findComponent({ ref: 'editButton' });
    await editButton.trigger('click');

    // This method might be alter in another task, juz setting up
    const dialogs = wrapper.findAllComponents({ name: "v-dialog" });
    return dialogs.at(0);
  }
  /**
   * Finds the delete confirmation dialog box upon clicking the delete button
   * @returns the delete confirmation dialog box
   */
  async function openDeleteConfirmationDialog() {
    const deleteButton = wrapper.findComponent({ ref: 'deleteButton' });
    await deleteButton.trigger('click');

    const dialogs = wrapper.findAllComponents({ name: "v-dialog" });
    return dialogs.at(0);
  }
  /**
   * Finds the message dialog box upon clicking the message button
   * @returns the message dialog box
   */
  async function openConversationDialog() {
    const messageButton = wrapper.findComponent({ref:'messageButton'});
    await messageButton.trigger('click');

    const dialogs = wrapper.findAllComponents({ name: "v-dialog" });
    return dialogs.at(1);
  }

  /**
   * Creates the environment used for testing. The marketplace card being viewed
   * can be altered by changing the contents of the card
   */
  function generateWrapper(properties?: Partial<{showActions: boolean, showSection: boolean}>) {
    // Creating wrapper around MarketplaceCard with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { MarketplaceCard },
      template: `
      <div data-app>
        <MarketplaceCard :content="testMarketplaceCard" :showActions="showActions" :showSection="showSection"/>
      </div>`
    });

    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      store,
      data() {
        return {
          testMarketplaceCard: testMarketplaceCard,
          showActions: true,
          showSection: false,
          ...properties
        };
      }
    });
    wrapper = appWrapper.getComponent(MarketplaceCard);
  }

  /**
   * Set up the store for testing so that the marketplace card can show the appropriate details.
   * @param userId ID of the current user that is currently logged in
   * @param userRole Role of the current user that is currently logged in
   */
  function setUpStore(userId: number, userRole: string) {
    // mocking the Vuex store user id such that it does not match the testMarketplaceCard object.
    state = {
      user: {
        id: userId,
        firstName: "John",
        lastName: "Smith",
        homeAddress: {
          district: "District",
          city: "City",
          country: "Country",
        }
      }
    };
    getters = {
      role: () => userRole
    };
    store = new Vuex.Store({
      getters,
      state
    });
  }

  beforeEach(() => {
    vuetify = new Vuetify();
    setUpStore(2, "user");
    generateWrapper();
  });

  /**
	 * Ensures the CreateSaleItem component is removed from the global document
	 */
  afterEach(() => {
    appWrapper.destroy();
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
    const deleteConfirmationDialog = await openDeleteConfirmationDialog();
    const dialogDeleteButton = findButton('Delete', deleteConfirmationDialog);
    await dialogDeleteButton.trigger("click");
    expect(deleteMarketplaceCard).toBeCalledWith(testMarketplaceCard.id);
    expect(wrapper.vm.deleteCardDialog).toBeFalsy();
  });

  it("The dialog box should not be visible if the cancel button is clicked in the confirmation dialog box", async () => {
    const deleteConfirmationDialog = await openDeleteConfirmationDialog();
    const dialogCancelButton = findButton('Cancel', deleteConfirmationDialog);
    await dialogCancelButton.trigger("click");
    expect(wrapper.vm.deleteCardDialog).toBeFalsy();
  });


  it("Must not be able to find the delete icon if the user is not the owner of the card", async () => {
    setUpStore(3, "user");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'deleteButton' });
    expect(buttons.length).toBe(0);
  });

  it('Must not be able to find the delete icon if the property "showActions" is false', async () => {
    generateWrapper({showActions: false});

    const buttons = wrapper.findAllComponents({ ref: 'deleteButton' });
    expect(buttons.length).toBe(0);
  });

  it("Must be able to find the delete icon if the user is not the owner of the card but is a DGAA", async () => {
    setUpStore(3, "defaultGlobalApplicationAdmin");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'deleteButton' });
    expect(buttons.length).toBe(1);
  });

  it("Must be able to find the delete icon if the user is not the owner of the card but is a GAA", async () => {
    setUpStore(3, "globalApplicationAdmin");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'deleteButton' });
    expect(buttons.length).toBe(1);
  });

  it("Must not be able to find the edit icon if the user is not the owner of the card", async () => {
    setUpStore(3, "user");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'editButton' });
    expect(buttons.length).toBe(0);
  });

  it('Must not be able to find the edit icon if the property "showActions" is false', async () => {
    generateWrapper({showActions: false});

    const buttons = wrapper.findAllComponents({ ref: 'editButton' });
    expect(buttons.length).toBe(0);
  });

  it("Must be able to find the edit icon if the user is not the owner of the card but is a DGAA", async () => {
    setUpStore(3, "defaultGlobalApplicationAdmin");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'editButton' });
    expect(buttons.length).toBe(1);
  });

  it("Must be able to find the edit icon if the user is not the owner of the card but is a GAA", async () => {
    setUpStore(3, "globalApplicationAdmin");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'editButton' });
    expect(buttons.length).toBe(1);
  });

  it.each(Object.keys(SECTION_NAMES) as MarketplaceCardSection[])(
    'Must contain the section name of "%s" if "showSection" is true',
    async (section: MarketplaceCardSection) => {
      generateWrapper({showSection: true});
      await wrapper.setData({
        content: {
          section
        }
      });
      await Vue.nextTick();
      expect(wrapper.text()).toContain(SECTION_NAMES[section]);
    });

  it.each(Object.keys(SECTION_NAMES) as MarketplaceCardSection[])(
    'Must not contain the section name of "%s" if "showSection" is false',
    async (section: MarketplaceCardSection) => {
      generateWrapper({showSection: false});
      await wrapper.setData({
        content: {
          section
        }
      });
      await Vue.nextTick();
      expect(wrapper.text()).not.toContain(SECTION_NAMES[section]);
    });

  it('Date string must contain only the created date if created and last renewed dates are the same', async() => {
    await wrapper.setData({
      content: {
        created: '2021-03-01',
        lastRenewed: '2021-03-01',
      }
    });
    expect(wrapper.vm.dateString).toBe('Posted 01 Mar 2021');
  });

  it('Date string must contain both created and renewed date if they are different', async() => {
    await wrapper.setData({
      content: {
        created: '2021-03-01',
        lastRenewed: '2021-04-01',
      }
    });
    expect(wrapper.vm.dateString).toBe('Posted 01 Mar 2021, Renewed 01 Apr 2021');
  });

  it("Must not be able to find the message icon if the user is the owner of the card", async () => {
    setUpStore(2, "user");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'messageButton' });
    expect(buttons.length).toBe(0);
  });

  it("Must be able to find the message icon if the user is not the owner of the card", async () => {
    setUpStore(3, "user");
    generateWrapper();
    const buttons = wrapper.findAllComponents({ ref: 'messageButton' });
    expect(buttons.length).toBe(1);
  });

  it("Must trigger message dialog box upon clicking message icon", async () => {
    setUpStore(3, 'user'); // must not be the owner
    generateWrapper();
    expect(wrapper.vm.messageOwnerDialog).toBeFalsy();
    //This button is an icon, so a reference is used to identify it instead of its button text
    const deleteButton = wrapper.findComponent({ ref: 'messageButton' });
    await deleteButton.trigger('click');
    expect(wrapper.vm.messageOwnerDialog).toBeTruthy();
  });

  it("The messageConversation method must be called and the dialog box should not be visible, upon clicking the send button in the message dialog box", async () => {
    setUpStore(3, 'user'); // must not be the owner
    generateWrapper();
    const messageDialog = await openConversationDialog();
    await wrapper.setData({
      directMessageValid: true
    });
    await Vue.nextTick();
    const dialogSendButton = findButton('Send', messageDialog);
    await dialogSendButton.trigger("click");
    expect(messageConversation).toBeCalledWith(1, 3, 3, "");
    expect(wrapper.vm.messageOwnerDialog).toBeFalsy();
  });

  it("The dialog box should not be visible if the cancel button is clicked in the conversation dialog box", async () => {
    setUpStore(3, 'user'); // must not be the owner
    generateWrapper();
    const messageDialog = await openConversationDialog();
    const dialogCancelButton = findButton('Cancel', messageDialog);
    await dialogCancelButton.trigger("click");
    expect(wrapper.vm.messageOwnerDialog).toBeFalsy();
  });

  it("The conversation message must be at least 1 character. The send button is disabled otherwise", async () => {
    setUpStore(3, 'user'); // must not be the owner
    generateWrapper();
    const messageDialog = await openConversationDialog();
    const dialogSendButton = findButton('Send', messageDialog);
    expect(wrapper.vm.directMessageValid).toBeFalsy();
    expect(dialogSendButton.props().disabled).toBeTruthy();
    await wrapper.setData({
      directMessageContent: "A long message"
    });
    await Vue.nextTick();
    expect(wrapper.vm.directMessageValid).toBeTruthy();
    expect(dialogSendButton.props().disabled).toBeFalsy();
  });

});
