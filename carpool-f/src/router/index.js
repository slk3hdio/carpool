import { createRouter, createWebHistory } from 'vue-router'

import Home from '../pages/Home.vue'
import Traffic from '../pages/Traffic.vue'
import Carpool from '../pages/Carpool.vue'
import User from '../pages/User.vue'
import RoadDemo from '../pages/RoadDemo.vue'
import HistoricalTraffic from '../pages/HistoricalTraffic.vue'
import Monitor from '../pages/Monitor.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/traffic', component: Traffic },
  { path: '/carpool', component: Carpool },
  { path: '/user', component: User },
  { path: '/demo', component: RoadDemo },
  { path: '/historical', component: HistoricalTraffic }
  ,{ path: '/monitor', component: Monitor }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
