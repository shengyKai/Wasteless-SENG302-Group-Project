import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import MessageEvent from '@/components/home/newsfeed/MessageEvent.vue';

import Vuex, {Store} from 'vuex';
import {getStore, resetStoreForTesting, StoreData} from '@/store';
import {castMock, makeTestUser, findButtonWithText} from '../utils';
import {
  getMessagesInConversation as getMessagesInConversation1,
  MarketplaceCard,
  messageConversation as messageConversation1
} from "@/api/marketplace";
import * as events from '@/api/events';

Vue.use(Vuetify);

jest.mock('@/api/marketplace', () => ({
  messageConversation: jest.fn(),
  getMessagesInConversation: jest.fn(),
}));

jest.mock('@/api/events', () => ({
  getEvents: jest.fn(),
  updateEventAsRead: jest.fn(),
}));

const messageConversation = castMock(messageConversation1);
const getMessagesInConversation = castMock(getMessagesInConversation1);
const getEvents = castMock(events.getEvents);

const sellerUser = makeTestUser(100);
const buyerUser = makeTestUser(50);

const testCard: MarketplaceCard = {
  id: 1,
  creator: sellerUser,
  section: 'ForSale',
  created: "01/01/2020",
  lastRenewed: "02/01/2020",
  displayPeriodEnd: "05/01/2020",
  title: "test_title",
  description: "test_desription",
  keywords: []
};

describe('MessageEvent.vue', () => {
  let wrapper: Wrapper<any>;
  let eventWrapper: Wrapper<any>;
  let vuetify: Vuetify;
  // The global store to be used
  let store: Store<StoreData>;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = buyerUser;

    wrapper = mount(MessageEvent, {
      localVue,
      vuetify,
      store,
      propsData: {
        event: {
          type: 'MessageEvent',
          id: 7,
          tag: 'none',
          created: '2021-01-02T11:00:00Z',
          message: {
            id: 9,
            senderId: buyerUser.id,
            created: '2021-01-02T11:00:00Z',
            content: 'test message 1',
          },
          conversation: {
            card: testCard,
            buyer: buyerUser,
            id: 3,
          },
          participantType: 'seller',
        },
      }
    });
    eventWrapper = wrapper.findComponent({name: 'Event'});
    getEvents.mockResolvedValue([]);
  });

  afterEach(() => {
    wrapper.destroy();
  });

  /**
   * Finds the button that controls sending a new message
   * @returns A wrapper around the send button
   */
  const findSendButton = () => findButtonWithText(wrapper, 'Send');

  /**
   * Finds the button that controls loading more messages
   * @returns A wrapper around the load more button
   */
  const findLoadMoreButton = () => findButtonWithText(wrapper, 'Load more');

  it('If new message is from other participant then title should be "New message from..."', () => {
    expect(eventWrapper.vm.title).toBe('Conversation with ' + sellerUser.firstName);
  });

  it('If new message is from self participant then title should be "Conversation with..."', async () => {
    await wrapper.setData({
      event: {
        message: {
          senderId: sellerUser.id,
        },
      },
    });
    await Vue.nextTick();
    expect(eventWrapper.vm.title).toBe('New message from: ' + sellerUser.firstName);
  });

  it('If no message is provided then the send button is enabled until a message tries to be sent', async () => {
    expect(findSendButton().props().disabled).toBeFalsy();

    await findSendButton().trigger('click');
    await Vue.nextTick();

    expect(findSendButton().props().disabled).toBeTruthy();
    expect(messageConversation).not.toBeCalled();
  });

  it('If the message is too long then the send button is disabled', async () => {
    await wrapper.setData({
      directMessageContent: 'a'.repeat(201),
    });
    await Vue.nextTick();
    expect(findSendButton().props().disabled).toBeTruthy();
  });

  it('If a message is provided then the send button is enabled', async () => {
    await wrapper.setData({
      directMessageContent: 'This is a message',
    });
    await Vue.nextTick();
    expect(findSendButton().props().disabled).toBeFalsy();
  });

  it('If the send button is pressed then the message should be sent', async () => {
    messageConversation.mockResolvedValueOnce(undefined);

    await wrapper.setData({
      directMessageContent: 'This is a message',
    });
    await Vue.nextTick();
    await findSendButton().trigger('click');
    await Vue.nextTick();

    expect(messageConversation).toBeCalledWith(testCard.id, buyerUser.id, buyerUser.id, 'This is a message');
    expect(eventWrapper.props().error).toBe(undefined); // No error is shown
    expect(wrapper.vm.directMessageContent).toBe(''); // Textbox is cleared
  });


  it('If the sending a message results in an error then it should be shown', async () => {
    messageConversation.mockResolvedValueOnce('test_error_message');

    await wrapper.setData({
      directMessageContent: 'This is a message',
    });
    await Vue.nextTick();
    await findSendButton().trigger('click');
    await Vue.nextTick();

    expect(eventWrapper.props().error).toBe('test_error_message');
    expect(wrapper.vm.directMessageContent).toBe('This is a message'); // Textbox is not cleared
  });

  it('Initial message should be displayed', () => {
    expect(wrapper.text()).toContain('test message 1');
  });

  it('Clicking load more will request more messages and displayed', async () => {
    getMessagesInConversation.mockResolvedValueOnce({
      count: 100,
      results: [
        {
          id: 6,
          senderId: buyerUser.id,
          created: new Date().toString(),
          content: 'test message 1',
        },
        {
          id: 4,
          senderId: buyerUser.id,
          created: new Date().toString(),
          content: 'test message 2',
        },
      ],
    });

    await findLoadMoreButton().trigger('click');

    await Vue.nextTick();

    expect(getMessagesInConversation).toBeCalledWith(testCard.id, buyerUser.id, 1, 1 + 10);
    expect(eventWrapper.props().error).toBe(undefined); // No error is shown

    expect(wrapper.text()).toContain('test message 1');
    expect(wrapper.text()).toContain('test message 2');
  });

  it('If load more results in an error then it is shown', async () => {
    getMessagesInConversation.mockResolvedValueOnce('test_error_message');

    await findLoadMoreButton().trigger('click');
    await Vue.nextTick();

    expect(eventWrapper.props().error).toBe('test_error_message');
  });

  it('If load more does not return any extra results, then the load more button is hidden', async () => {
    getMessagesInConversation.mockResolvedValueOnce({
      count: 1,
      results: [{
        id: 9,
        senderId: buyerUser.id,
        created: '2021-01-02T11:00:00Z',
        content: 'test message 1',
      }],
    });

    await findLoadMoreButton().trigger('click');
    await Vue.nextTick();

    expect(findLoadMoreButton().exists()).toBeFalsy();
  });

  it('Matches snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });
});
