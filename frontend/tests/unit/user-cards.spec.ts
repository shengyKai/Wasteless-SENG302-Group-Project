import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import UserCards from '@/components/marketplace/UserCards.vue';

Vue.use(Vuetify);

describe('UserCards.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();

  beforeEach(() => {
    let vuetify = new Vuetify();
    wrapper = mount(UserCards, {
      stubs: ['MarketplaceCard', 'router-link', 'router-view'],
      localVue,
      vuetify,
      mocks: {
        $route: {
          params: {
            id: 1,
          }
        }
      },
    });
  });

  it('Cards should be shown correctly in MarketplaceCard subcomponents', async () => {
    const cards = Array.from({ length: 5 }, (_, i) => ({id: i}));

    await wrapper.setData({
      cards
    });

    let cardComponents = wrapper.findAllComponents({ name: 'MarketplaceCard' });
    for (let i = 0; i<5; i++) {
      let card = cardComponents.at(i);
      expect(card.props()).toStrictEqual({
        content: {id: i},
        showActions: true,
        showSection: true,
      });
    }
    expect(cardComponents.length).toBe(5);
  });
});