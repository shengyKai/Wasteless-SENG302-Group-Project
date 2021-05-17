import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SaleItem from "@/components/cards/SaleItem.vue";
import FullProductDescription from "@/components/utils/FullProductDescription.vue";

import * as api from "@/api/internal";
import { castMock, flushQueue } from './utils';
import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

