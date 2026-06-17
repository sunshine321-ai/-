import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/pages/HomePage.vue'),
    },
    {
      path: '/study',
      name: 'study',
      component: () => import('@/pages/StudyPage.vue'),
    },
    {
      path: '/ai-tutor',
      name: 'ai-tutor',
      component: () => import('@/pages/AiTutorPage.vue'),
    },
    {
      path: '/playground',
      name: 'playground',
      component: () => import('@/pages/PlaygroundPage.vue'),
    },
    {
      path: '/showcase-3d',
      name: 'showcase-3d',
      component: () => import('@/pages/Showcase3DPage.vue'),
    },
    {
      path: '/data-viz',
      name: 'data-viz',
      component: () => import('@/pages/DataVizPage.vue'),
    },
    {
      path: '/calculator',
      name: 'calculator',
      component: () => import('@/pages/CalculatorPage.vue'),
    },
  ],
})

export default router
