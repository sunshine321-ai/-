import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomePage.vue'),
    },
    {
      path: '/study',
      name: 'study',
      component: () => import('@/views/StudyPage.vue'),
    },
    {
      path: '/ai-tutor',
      name: 'ai-tutor',
      component: () => import('@/views/AiTutorPage.vue'),
    },
    {
      path: '/playground',
      name: 'playground',
      component: () => import('@/views/PlaygroundPage.vue'),
    },
    {
      path: '/showcase-3d',
      name: 'showcase-3d',
      component: () => import('@/views/Showcase3DPage.vue'),
    },
    {
      path: '/data-viz',
      name: 'data-viz',
      component: () => import('@/views/DataVizPage.vue'),
    },
    {
      path: '/calculator',
      name: 'calculator',
      component: () => import('@/views/CalculatorPage.vue'),
    },
  ],
})

export default router
